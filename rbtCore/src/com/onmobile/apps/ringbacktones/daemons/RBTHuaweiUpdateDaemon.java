package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.daemons.implementation.VodaRomaniaPlayerImpl;
import com.onmobile.apps.ringbacktones.daemons.interfaces.PlayerThread;
import com.onmobile.apps.ringbacktones.daemons.interfaces.iPlayerModel;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;

/**
 * This class is the interface between OnMobile's RBT System and Huawei in Voda-Romania. Only the
 * 	setting requests and Overlay feature requests are updated to Huawei by this daemon. Rest all will
 * 	be intimated by SM.
 * 
 * @author Sreekar
 * @Date 2009-01-02
 */
public class RBTHuaweiUpdateDaemon extends PlayerThread implements iRBTConstant {
	private static Logger logger = Logger.getLogger(RBTHuaweiUpdateDaemon.class);
	
	private ArrayList<Subscriber> _updateSubs = new ArrayList<Subscriber>();
	private ArrayList<SubscriberStatus> _updateSels = new ArrayList<SubscriberStatus>();
	private ArrayList<SubscriberStatus> _deleteSels = new ArrayList<SubscriberStatus>();
	private ArrayList<Groups> _addGroups = new ArrayList<Groups>();
	private ArrayList<Groups> _delGroups = new ArrayList<Groups>();
	private ArrayList<GroupMembers> _addGroupMembers = new ArrayList<GroupMembers>();
	private ArrayList<GroupMembers> _delGroupMembers = new ArrayList<GroupMembers>();
	
	public RBTHuaweiUpdateDaemon(RBTDaemonManager daemonManager) throws RBTException {
		super(daemonManager);
	}
	
	public RBTHuaweiUpdateDaemon() throws RBTException {
		super();
	}
	
	protected void init() throws RBTException {
		_playerModel = new VodaRomaniaPlayerImpl(_dbManager);
		try {
			_sleepTime = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON", "HUAWEI_DAEMON_SLEEP_MIN", 5+"").getValue());
		}
		catch (Exception e) {
		}
		try {
			_fetchSize = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON", "FETCH_SIZE", 5000+"").getValue());
		}
		catch (Exception e) {
		}
		logger.info("RBT::_sleepTime is - " + _sleepTime + ", _fetchSize is "+ _fetchSize);
	}
	
	protected boolean updateQueue() {
		boolean b = updateSubscriptionQueue();
		b = updateSelectionQueue() || b;
		b = updateGroupsQueue() || b;
		return b;
	}
	
	/**
	 * Updates groups to be added, groups to be deleted, group members to be added and group members
	 * to be deleted
	 * 
	 * @return true if anything related to groups to inform Huawei has been added to queue
	 */
	private boolean updateGroupsQueue() {
		boolean b = updateAddGroupsQueue();
		b = updateDelGroupsQueue() || b;
		b = updateAddGroupMemebersQueue() || b;
		b = updateDelGroupMemebersQueue() || b;
		return b;
	}
	
	private boolean updateAddGroupsQueue() {
		if(_addGroups.isEmpty()) {
			ArrayList<Groups> addGroups = _dbManager.playerGetAddGroups();
			if(addListToQueueList(_addGroups, addGroups)) {
				logger.info("RBT::Added " + addGroups.size() + " records");
				return true;
			}
		}
		else
			logger.info("RBT::Still " + _addGroups.size()
					+ " records to be processed");
		return false;
	}
	
	private boolean updateDelGroupsQueue() {
		if(_delGroups.isEmpty()) {
			ArrayList<Groups> delGroups = _dbManager.playerGetDelGroups();
			if(addListToQueueList(_delGroups, delGroups)) {
				logger.info("RBT::Added " + delGroups.size() + " records");
				return true;
			}
		}
		else
			logger.info("RBT::Still " + _delGroups.size()
					+ " records to be processed");
		return false;
	}
	
	private boolean updateAddGroupMemebersQueue() {
		if(_addGroupMembers.isEmpty()) {
			ArrayList<GroupMembers> addGroupMembers = _dbManager.playerGetAddGroupMembers();
			if(addListToQueueList(_addGroupMembers, addGroupMembers)) {
				logger.info("RBT::Added " + addGroupMembers.size()
								+ " records");
				return true;
			}
		}
		else
			logger.info("RBT::Still " + _addGroupMembers.size()
					+ " records to be processed");
		return false;
	}
	
	private boolean updateDelGroupMemebersQueue() {
		if(_delGroupMembers.isEmpty()) {
			ArrayList<GroupMembers> delGroupMembers = _dbManager.playerGetDelGroupMembers();
			if(addListToQueueList(_delGroupMembers, delGroupMembers)) {
				logger.info("RBT::Added " + delGroupMembers.size()
								+ " records");
				return true;
			}
		}
		else
			logger.info("RBT::Still " + _delGroupMembers.size()
					+ " records to be processed");
		return false;
	}
	
	private boolean updateSelectionQueue() {
		boolean b = updateAddSelQueue();
		b = b || updateDelSelQueue();
		return b;
	}
	
	private boolean updateDelSelQueue() {
		if(_deleteSels.isEmpty()) {
			ArrayList<SubscriberStatus> removeSels = _dbManager.playerGetRemovedSels(_fetchSize);
			if(addListToQueueList(_deleteSels, removeSels)) {
				logger.info("RBT::Added " + removeSels.size() + " records");
				return true;
			}
		}
		else
			logger.info("RBT::Still " + _deleteSels.size()
					+ " records to be processed");
		return false;
	}
	
	private boolean updateAddSelQueue() {
		if(_updateSels.isEmpty()) {
			ArrayList<SubscriberStatus> addSels = _dbManager.playerGetActivatedSels(_fetchSize);
			if(addListToQueueList(_updateSels, addSels)) {
				logger.info("RBT::Added " + addSels.size() + " records");
				return true;
			}
		}
		else
			logger.info("RBT::Still " + _updateSels.size()
					+ " records to be processed");
		return false;
	}
	
	private boolean updateSubscriptionQueue() {
		if(_updateSubs.isEmpty()) {
			ArrayList<Subscriber> overlaySubs = _dbManager.playerGetActivatedSubs(_fetchSize);
			if(addListToQueueList(_updateSubs, overlaySubs)) {
				logger.info("RBT::Added " + overlaySubs.size() + " records");
				return true;
			}
		}
		else
			logger.info("RBT::Still " + _updateSubs.size()
					+ " records to be processed");
		return false;
	}
	
	/**
	 * Adds new pending list to the processing list.
	 * 
	 * @param originalList base list to which the new queue has to be added
	 * @param newList
	 * @return true if list is updated
	 * 
	 */
	private boolean addListToQueueList(ArrayList originalList, ArrayList newList) {
		if(newList != null && !newList.isEmpty()) {
			synchronized(_syncObj) {
				originalList.addAll(newList);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param sleep
	 * @return
	 */
	protected long getSleepTime() {
		Calendar now = Calendar.getInstance();
		now.setTime(new java.util.Date(System.currentTimeMillis()));
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		long nexttime = now.getTime().getTime();
		long nowTime = System.currentTimeMillis();
		while (nexttime < nowTime) {
			nexttime = nexttime + (_sleepTime * 60 * 1000);
		}
		Date nextDate = new Date(nexttime);
        logger.info("RBT::getnexttime - " + nextDate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(nextDate);
		long diffTime = calendar.getTime().getTime() - Calendar.getInstance().getTime().getTime();
		if(diffTime <= 0) {
			logger.info("RBT::diff time is " + diffTime + ". Sleeping for "
					+ _sleepTime + " min");
			diffTime = _sleepTime * 60 * 1000;
		}
		return diffTime;
	}
	
	protected PlayerTask getTask() {
		try {
			synchronized(_syncObj) {
				PlayerTask task = null;
				if((task = createPlayerTask(_updateSubs, iPlayerModel.TASK_UPDATE_SUBSCRIBER)) != null)
					return task;
				else _updateSubs.clear();

				if((task = createPlayerTask(_deleteSels, iPlayerModel.TASK_DELETE_SETTING)) != null)
					return task;
				else _deleteSels.clear();

				if((task = createPlayerTask(_delGroupMembers, iPlayerModel.TASK_DELETE_GROUP_MEMBER)) != null)
					return task;
				else _delGroupMembers.clear();

				if((task = createPlayerTask(_delGroups, iPlayerModel.TASK_DELETE_GROUP)) != null)
					return task;
				else _delGroups.clear();

				if((task = createPlayerTask(_addGroups, iPlayerModel.TASK_ADD_GROUP)) != null)
					return task;
				else _addGroups.clear();

				if((task = createPlayerTask(_addGroupMembers, iPlayerModel.TASK_ADD_GROUP_MEMBER)) != null)
					return task;
				else _addGroupMembers.clear();

				if((task = createPlayerTask(_updateSels, iPlayerModel.TASK_UPDATE_SETTING)) != null)
					return task;
				else _updateSels.clear();
			}
			synchronized(_syncObj) {
				logger.info("RBT::before wait...");
				_syncObj.wait();
				logger.info("RBT::after wait...");
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return null;
	}
	
	private PlayerTask createPlayerTask(ArrayList<?> list, int taskType) {
		for(int index = 0; index < list.size(); index++) {
			try {
				Object obj = list.get(index);
				if(addToProcessingMap(obj)) {
					list.remove(index);
					return new PlayerTask(obj, taskType);
				}
			}
			catch(RBTException e) {
				logger.error("", e);
			}
		} // end of for loop
		return null;
	}
}