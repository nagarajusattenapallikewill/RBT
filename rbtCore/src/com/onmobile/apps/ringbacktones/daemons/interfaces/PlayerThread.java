package com.onmobile.apps.ringbacktones.daemons.interfaces;

import java.util.Calendar;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;

/**
 * All player start classes should extend this class. The same extended class should be configured
 * in RBT_PARAMTERES table as PLAYER_START_CLASS with type DAEMON
 * 
 * @author Sreekar 2009-01-12
 * 
 * @modified Sreekar 2009-01-19 
 * Made Multi-threaded
 */
public abstract class PlayerThread extends Thread implements iRBTConstant {
	
	private static final String _class = "PlayerThread";
	private static Logger logger = Logger.getLogger(PlayerThread.class);
	
	//This variable will be assigned if the daemon is started from the main Daemon
	public RBTDaemonManager _daemonManager;
	private boolean _continue = true;
	protected iPlayerModel _playerModel = null;//playerModel must be inited in init
	protected static final Object _syncObj = new Object();
	private HashMap<String, String> _processingMap = new HashMap<String, String>();

	protected RBTDBManager _dbManager;
	protected ParametersCacheManager paramCacheManager;
//	protected Hashtable<String, String>_daemonParamMap;

	protected int _sleepTime = 5;
	protected int _fetchSize = 5000;
	/**
	 * This constructor is used, when this daemon has been started by the main daemon
	 * @param mainDaemon
	 * @throws RBTException 
	 */
	public PlayerThread(RBTDaemonManager daemonManager) throws RBTException {
		_daemonManager = daemonManager;
		this.setName(_class);
		baseInit();
		init();
	}

	/**
	 * This constructor is used when the daemon has to be started individually
	 * @throws RBTException 
	 */	
	public PlayerThread() throws RBTException {
		this.setName(_class);
		Tools.init(_class, false);
		baseInit();
		init();
	}
	
	private void baseInit() throws RBTException {
		paramCacheManager = CacheManagerUtil.getParametersCacheManager();
    	_dbManager = RBTDBManager.getInstance();
		
		if(_dbManager == null)
			throw new RBTException("Cannot initialize DBManager");
	}
	
	/**
	 * This method is to be called by the getTask() method. Only 1 request per subscriber will be
	 * processed at a time. So whenever a task is being created, subscriber is t be added to a
	 * processing queue
	 * 
	 * @param obj
	 *            Object to be processed
	 * @return success if the object's MDN is added
	 * @throws RBTException
	 *             if the object passed is of unknown type
	 */
	protected boolean addToProcessingMap(Object obj) throws RBTException {
		synchronized(_syncObj) {
			HashMap<String, String> map = getKey(obj);
			String key = map.get("key");
			if(!canProcess(key))
				return false;
			String task = map.get("task");
			_processingMap.put(key, task);
		}
		return true;
	}
	
	private HashMap<String, String> getKey(Object obj) throws RBTException {
		String key = null;
		String task = null;
		HashMap<String, String> map = new HashMap<String, String>();
		if(obj instanceof Subscriber) {
			key = ((Subscriber)obj).subID();
			task = "SUB";
		}
		else if(obj instanceof SubscriberStatus) {
			key = ((SubscriberStatus)obj).subID();
			task = "SET";
		}
		else if(obj instanceof SubscriberDownloads) {
			key = ((SubscriberDownloads)obj).subscriberId();
			task = "DOWN";
		}
		else if(obj instanceof Groups) {
			key = ((Groups)obj).groupID()+"";
			task = "GRP";
		}
		else if(obj instanceof GroupMembers) {
			key = ((GroupMembers)obj).groupID()+"";
			task = "GRPMEM";
		}
		else
			throw new RBTException("Unknown object type to process");
		map.put("key", key);
		map.put("task", task);
		return map;
	}
	
	private void clearProcessingMap(String key) {
		synchronized(_syncObj) {
			_processingMap.remove(key);
		}
	}
	
	private boolean canProcess(String key) {
		synchronized(_syncObj) {
			if(key != null && _processingMap.containsKey(key))
				return false;
		}
		return true;
	}
	
	private boolean checkLoopCondition() {
		if(_daemonManager != null)
			_continue = _daemonManager.isAlive();
		return _continue;
	}
	
	public void stopDaemon() {
		_continue = false;
	}
	
	public void run() {
		logger.info("RBT::Starting TaskThreads");
		startTaskThreads();
		while(checkLoopCondition()) {
			try {
				if(updateQueue()) {
					synchronized(_syncObj) {
						logger.info("RBT::Notifying all threads");
						_syncObj.notifyAll();
						logger.info("RBT::Notified all threads");
					}
				}
				sleep();
			}
			catch(Throwable t) {
				logger.error("", t);
			}
		}
		logger.info("RBT::Stopping Player daemon");
	}
	
	private void startTaskThreads() {
		int poolSize = 1;
		try{
			poolSize = Integer.parseInt(paramCacheManager.getParameter("DAEMON","PLAYER_THREAD_POOL_SIZE", 1+"").getValue());
		}catch(Exception e)	{
			poolSize =1;
		}
		logger.info("RBT::PlayerPoolThreadSize is - " + poolSize);
		//creating TaskThreads
		for(int i = 0; i < poolSize; i++)
			startTaskThread(i);
	}
	
	protected void startTaskThread(int i) {
		new PlayerTaskThread("PLTask-" + i).start();
	}
	
	/**
	 * This method makes the thread sleep for configured amount of time
	 */
	private void sleep() {
		try {
			long sleepTime = getSleepTime();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MILLISECOND, new Long(sleepTime).intValue());
			logger.info("RBT::sleeping till " + cal.getTime());
			Thread.sleep(sleepTime);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	/**
	 * This method should calculate the time for which the daemon (refreshing tasks) has to sleep
	 * @return sleepTime in milliseconds
	 */
	protected abstract long getSleepTime();
	
	/**
	 * This method should init the iPlayerModel, start all the PlayerTaskThreads and read other
	 * general params like sleepTime etc
	 * 
	 * @throws RBTException
	 */
	protected abstract void init() throws RBTException;
	
	/**
	 * This method should return the task only if addToProcessingMap returns true, else return the
	 * next task. Tasks have to be prioritized properly.
	 * 
	 * @return PlayerTask
	 * 
	 * @see PlayerTask
	 */
	protected abstract PlayerTask getTask();
	
	/**
	 * Updates the queues to be processed
	 * @return returns true if anything is added to queue
	 */
	protected abstract boolean updateQueue();
	
	/**
	 * This class is the actual thread that performs tasks
	 * @author Sreekar 2009-01-19
	 *
	 * @edited Sreekar 2009-02-25, added group task handling
	 */
	protected class PlayerTaskThread extends Thread {
		private String _class;
		
		PlayerTaskThread(String name) {
			_class = name;
			this.setName(name);
		}
		
		public void run() {
			if(_playerModel == null) {
				logger.info("RBT::playerModel not inited, exiting");
				return;
			}
			while(_daemonManager.isAlive()) {
				PlayerTask task = getTask();
				if(task == null) {
					logger.info("RBT::got null task, continuing the loop");
					continue;
				}
				try {
					switch(task.getTaskType()) {
						case iPlayerModel.TASK_UPDATE_SUBSCRIBER:
							_playerModel.updateSubscriber((Subscriber) task.getObj());
							break;
						case iPlayerModel.TASK_UPDATE_SETTING:
							_playerModel.updateSetting((SubscriberStatus)task.getObj());
							break;
						case iPlayerModel.TASK_DELETE_SUBSCRIBER:
							_playerModel.deleteSubscriber((Subscriber) task.getObj());
							break;
						case iPlayerModel.TASK_DELETE_SETTING:
							_playerModel.deleteSetting((SubscriberStatus)task.getObj());
							break;
						case iPlayerModel.TASK_DELETE_DOWNLOAD:
							_playerModel.deleteDownload((SubscriberDownloads)task.getObj());
							break;
						case iPlayerModel.TASK_ADD_GROUP:
							_playerModel.addGroup((Groups)task.getObj());
							break;
						case iPlayerModel.TASK_DELETE_GROUP:
							_playerModel.deleteGroup((Groups)task.getObj());
							break;
						case iPlayerModel.TASK_ADD_GROUP_MEMBER:
							_playerModel.addGroupMember((GroupMembers)task.getObj());
							break;
						case iPlayerModel.TASK_DELETE_GROUP_MEMBER:
							_playerModel.deleteGroupMember((GroupMembers)task.getObj());
							break;
						default:
							logger.info("RBT::Unknown taskType "
									+ task.getTaskType());	
					}
				}
				catch(Exception e) {
					logger.error("", e);
				}
				finally {
					try {
						clearProcessingMap(getKey(task.getObj()).get("key"));
					}
					catch(Exception e) {
						logger.error("", e);
					}
				}
			}
			logger.info("RBT::Exiting PlayerTaskThread - " + _class);
		}
	}
	
	/**
	 * Object to be processed by the PlayerTaskThread will be converted to PlayerTask with
	 * appropriate task type
	 * 
	 * @author Sreekar 2009-01-19
	 * @see PlayerTaskThread
	 */
	public class PlayerTask {
		Object _obj;
		int _taskType;
		
		/**
		 * 
		 * @param obj to be processed
		 * @param type TaskType (Use constants in iPlayerModel)
		 * 
		 * @see iPlayerModel
		 */
		public PlayerTask(Object obj, int type) {
			_obj = obj;
			_taskType = type;
		}
		
		protected Object getObj() {
			return _obj;
		}
		
		protected int getTaskType() {
			return _taskType;
		}
	}
}