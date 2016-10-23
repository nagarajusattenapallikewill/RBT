package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
//import com.onmobile.apps.ringbacktones.content.ToBeDeletedSelections;
//import com.onmobile.apps.ringbacktones.content.ToBeDeletedSettings;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class TATARBTDaemonController extends Thread implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(TATARBTDaemonController.class);
	
	private Object m_process_lock = new Object ();
	private int totalCountAddedtoQueue = 0;

	private static int m_nConn=4;
	private int m_thread_mode; // PRIMARY or SECONDARY

	private TaskDetail activationTask = null;
	private TaskDetail deactivationTask = null;
	private TaskDetail activationPollingTaskPost = null;
	private TaskDetail activationPollingTaskPre = null;
	private TaskDetail deactivationPollingTaskPost = null;
	private TaskDetail deactivationPollingTaskPre = null;
	private TaskDetail addSettingTaskPost = null;
	private TaskDetail addSettingTaskPre = null;
	private TaskDetail deleteSettingTask = null;
	private TaskDetail deleteSelectionTask = null;
	private TaskDetail updateToDeactivateTask = null;
	private TaskDetail updateToToBeDeletedTask = null;
	private TaskDetail activationGraceTask = null; 
    private TaskDetail selectionGraceTask = null; 


	boolean sentCCBulkDeactivationSMS = false;

	private static HashMap m_hash = new HashMap();
	private TATARBTDaemonController secondDeamon = null;

	private TATARBTDaemonOzonized m_mainInstance = null;

	private static ParametersCacheManager parameterCacheManager = null;
	
	public TATARBTDaemonController(TATARBTDaemonOzonized ozoneDaemon)
	{
		parameterCacheManager = CacheManagerUtil.getParametersCacheManager();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.SMS, "NUM_CONN", "4");
		String s= null;
		//RBTSMSConfig.getInstance().getParameter("NUM_CONN");
		if(parameter != null && parameter.getValue() != null)
		{
			s = parameter.getValue();
			try
			{
				m_nConn = Integer.parseInt(s);
			}
			catch(Exception e)
			{
				m_nConn =4;
			}
		}
		m_mainInstance = ozoneDaemon;
		getResourceValues();
		m_thread_mode = RBT_THREAD_PRIMARY; 
		logger.info("exiting");
	}

	public TATARBTDaemonController(int mode)
	{
		parameterCacheManager = CacheManagerUtil.getParametersCacheManager();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.SMS, "NUM_CONN", "4");
		String s= null;
		//RBTSMSConfig.getInstance().getParameter("NUM_CONN");
		if(parameter != null && parameter.getValue() != null)
		{
			s = parameter.getValue();
			try
			{
				m_nConn = Integer.parseInt(s);
			}
			catch(Exception e)
			{
				m_nConn =4;
			}
		}
		if (mode == RBT_THREAD_SECONDARY) 
			m_thread_mode = RBT_THREAD_SECONDARY;
		else
		{
			m_thread_mode = RBT_THREAD_PRIMARY;
			getResourceValues();
		}
		logger.info("exiting ad thread mode = " + m_thread_mode + " called mode = " + mode);
	}

	private static void getResourceValues()
	{
		try
		{
			
		}
		catch(Exception e)
		{
			logger.info("Exception : "+e.getMessage());
		}
	}

	private DaemonTask getTask (int tasktype, ArrayList al)
	{
		DaemonTask task;
		try
		{
			if(al != null && al.size() > 0)
			{
				logger.info("RBT::Getting Task, " + getTaskName (tasktype) + " from array, size = " + al.size());
				switch (tasktype)
				{
				case ACTIVATION_TASK:
				case ACTIVATION_POLLING_TASK_POSTPAID:
				case ACTIVATION_POLLING_TASK_PREPAID:
				case DEACTIVATION_TASK:
				case DEACTIVATION_POLLING_TASK_POSTPAID:
				case DEACTIVATION_POLLING_TASK_PREPAID:
				case UPDATE_TO_DEACTIVATE:
				case ACTIVATION_GRACE_TASK:
					Subscriber subscriber = getSubscriberFromList(al);
					if(subscriber != null)
					{
						task = new DaemonTask(tasktype, subscriber);
						return task;
					}
					break;
				case ADD_SETTING_TASK_POSTPAID:
				case ADD_SETTING_TASK_PREPAID:
				case SELECTION_GRACE_TASK:
					SubscriberStatus subSelection = getSubscriberStatusFromList(al);
					if(subSelection != null)
					{
						task = new DaemonTask(tasktype, subSelection);
						//m_hash.put(subSelection.subID(), "true");
						return task;
					}
					break;
				case DELETE_SETTING_TASK:
					SubscriberStatus delSetting = getSubscriberStatusFromList(al);
					if(delSetting != null)
					{
						task = new DaemonTask(tasktype, delSetting);
						//m_hash.put(delSetting.subID(), "true");
						return task;
					}
					break;
				case DELETE_SELECTION_TASK:
					//ToBeDeletedSelections delSelection = getToBeDeletedSelectionsFromList(al);
					SubscriberDownloads delSelection = getToBeDeletedSelectionsFromList(al);
					if(delSelection != null)
					{
						task = new DaemonTask(tasktype, delSelection);
						//m_hash.put(delSelection.subID(), "true");
						return task;
					}
					break;
				case UPDATE_TO_TO_BE_DELETED: 
                    SubscriberDownloads download = getSubscriberDowloadsFromList(al); 
                    if (download != null) { 
                            task = new DaemonTask(tasktype, download); 
                            return task; 

                    }
                    break;
				}
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		return null;
	}

	public DaemonTask getTask()
	{
		DaemonTask dt = null;

		if(m_thread_mode == RBT_THREAD_PRIMARY)
		{
			if ((dt  = getTask (ACTIVATION_TASK, activationTask.m_arrayList)) != null) return dt; else activationTask.m_arrayList.clear();
			if ((dt  = getTask (DEACTIVATION_TASK, deactivationTask.m_arrayList)) != null) return dt; else deactivationTask.m_arrayList.clear();
			if ((dt  = getTask (DELETE_SETTING_TASK, deleteSettingTask.m_arrayList)) != null) return dt; else deleteSettingTask.m_arrayList.clear();
			if ((dt  = getTask (DELETE_SELECTION_TASK, deleteSelectionTask.m_arrayList)) != null) return dt; else deleteSelectionTask.m_arrayList.clear();
			if ((dt  = getTask (ADD_SETTING_TASK_POSTPAID, addSettingTaskPost.m_arrayList)) != null) return dt; else addSettingTaskPost.m_arrayList.clear();
			if ((dt  = getTask (ADD_SETTING_TASK_PREPAID, addSettingTaskPre.m_arrayList)) != null) return dt; else addSettingTaskPre.m_arrayList.clear();
			if ((dt  = getTask (UPDATE_TO_DEACTIVATE, updateToDeactivateTask.m_arrayList)) != null) return dt; else updateToDeactivateTask.m_arrayList.clear();
			if ((dt  = getTask (UPDATE_TO_TO_BE_DELETED, updateToToBeDeletedTask.m_arrayList)) != null) return dt; else updateToToBeDeletedTask.m_arrayList.clear();
			if ((dt  = getTask (ACTIVATION_GRACE_TASK, activationGraceTask.m_arrayList)) != null) return dt; else activationGraceTask.m_arrayList.clear(); 
            if ((dt  = getTask (SELECTION_GRACE_TASK, selectionGraceTask.m_arrayList)) != null) return dt; else selectionGraceTask.m_arrayList.clear(); 
		}
		else
		{
			if ((dt  = getTask (ACTIVATION_POLLING_TASK_POSTPAID, activationPollingTaskPost.m_arrayList)) != null) return dt; else  activationPollingTaskPost.m_arrayList.clear();
			if ((dt  = getTask (ACTIVATION_POLLING_TASK_PREPAID, activationPollingTaskPre.m_arrayList)) != null) return dt; else activationPollingTaskPre.m_arrayList.clear();
			if ((dt  = getTask (DEACTIVATION_POLLING_TASK_POSTPAID, deactivationPollingTaskPost.m_arrayList)) != null) return dt; else deactivationPollingTaskPost.m_arrayList.clear();
			if ((dt  = getTask (DEACTIVATION_POLLING_TASK_PREPAID, deactivationPollingTaskPre.m_arrayList)) != null) return dt; else deactivationPollingTaskPre.m_arrayList.clear();
		}

		try
		{
			synchronized (m_process_lock)
			{
				logger.info("RBT::before wait");
				m_process_lock.wait ();
				logger.info("RBT::returning null task after wait");
				return null;
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		logger.info("RBT::returning null task");
		return null;
	}

	private Subscriber getSubscriberFromList(ArrayList list)
	{
		synchronized (m_process_lock) {
			for(int i = 0; i < list.size(); i++)
			{
				Subscriber sub = (Subscriber)list.get(i);
				if(!m_hash.containsKey(sub.subID()))
				{
					list.remove(i);
					m_hash.put(sub.subID(), "true");
					return sub;
				}
			}
		}
		return null;
	}
	
	private SubscriberDownloads getSubscriberDowloadsFromList(ArrayList list) { 
        synchronized (m_process_lock) { 
                for(int i = 0; i < list.size(); i++) 
                { 
                        SubscriberDownloads download = (SubscriberDownloads)list.get(i); 
                        if(!m_hash.containsKey(download.subscriberId())) 
                        { 
                                list.remove(i); 
                                m_hash.put(download.subscriberId(), "true"); 
                                return download; 
                        } 
                } 
        } 
        return null; 
	} 

	private SubscriberDownloads getToBeDeletedSelectionsFromList(ArrayList list)
	{
		synchronized (m_process_lock) {
			for(int i = 0; i < list.size(); i++)
			{
				SubscriberDownloads tobeDeleteSelection = (SubscriberDownloads)list.get(i);
				if(!m_hash.containsKey(tobeDeleteSelection.subscriberId()))
				{
					list.remove(i);
					m_hash.put(tobeDeleteSelection.subscriberId(), "true");
					return tobeDeleteSelection;
				}
			}
		}
		return null;
	}

//	private ToBeDeletedSettings getToBeDeletedSettingsFromList(ArrayList list)
//	{
//		synchronized (m_process_lock) {
//			for(int i = 0; i < list.size(); i++)
//			{
//				ToBeDeletedSettings tobeDeleteSetting = (ToBeDeletedSettings)list.get(i);
//				if(!m_hash.containsKey(tobeDeleteSetting.subID()))
//				{
//					list.remove(i);
//					m_hash.put(tobeDeleteSetting.subID(), "true");
//					return tobeDeleteSetting;
//				}
//			}
//		}
//		return null;
//	}

	private SubscriberStatus getSubscriberStatusFromList(ArrayList list)
	{
		synchronized (m_process_lock) {
			for(int i = 0; i < list.size(); i++)
			{
				SubscriberStatus subStatus = (SubscriberStatus)list.get(i);
				if(!m_hash.containsKey(subStatus.subID()))
				{
					list.remove(i);
					m_hash.put(subStatus.subID(), "true");
					return subStatus;
				}
			}
		}
		return null;
	}

	public void removeHashmap(String key)
	{
		if(m_hash.containsKey(key))
			m_hash.remove(key);
		return;
	}

	/**
	 * Starts primary task threads, secondary threads and bulk thread
	 */
	void startPrimaryThreads()
	{
		this.setName("Primary");
		TaskThread tt;
		
		/* Starts 'n' primary task threads
		 * These threads will perform following tasks
		 * 	 	a)ACT_TASK
		 * 		b)DCT-task
		 * 		c)ADD_SETTING_TASK_POST
		 * 		d)ADD_SETTING_TASK_PRE
		 * 		e)DELETE_SETTING_TASK
		 * 		f)DELETE_SELECTION_TASK
		 * 		g)UPDATE_TO_DCT_TASK
		 * 		h)UPDATE_TO_TO_BE_DELETED
		 * 		i)ACT_GRACE_TASK
		 * 		j)SEL_GRACE_TASK
		 */		
		try
		{
			Parameters param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"NUMBER_OF_THREADS","2");
			int numberOfThreads = Integer.parseInt(param.getValue());
			for(int i = 0; i < numberOfThreads; i++)
			{
				tt = new TaskThread(this);
				tt.setName("PTask-" + i);
				tt.start();
				logger.info("RBT::Started thread PTask-" + i);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
			return;
		}
		// Following code will start secondary threads.
		try
		{
			secondDeamon = new TATARBTDaemonController(RBT_THREAD_SECONDARY);
			logger.info("RBTTest::Starting secondary daemon and thread mode = " + m_thread_mode);
			secondDeamon.setName ("Secondary");
			secondDeamon.start();
		}
		catch(Exception e)
		{
			logger.error("", e);
			return;
		}

		// Following code will start the bulk thread
		try
		{
			if(m_mainInstance.m_InterfaceHelper != null)
			{
				BulkPromoTaskThread bulkThread = BulkPromoTaskThread.getInstance(m_mainInstance);
				bulkThread.setName ("BulkThread");
				bulkThread.start();
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
			return;
		}
	}

	/**
	 * starts secondary task threads (polling threads)
	 */
	public void startSecondaryThreads()
	{
		TaskThread tt;
		/* Starts 'n' secondary task threads(polling threads)
		 * These threads will perform following tasks
		 *		a)ACT_POLLING_TASK_POST
		 * 		b)ACT_POLLING_TASK_PRE
		 * 		c)DCT_POLLING_TASK_POST
		 * 		d)DCT_POLLING_TASK_PRE
		 */		
		try
		{
			Parameters param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"NUMBER_OF_THREADS","2");
			int numberOfThreads = Integer.parseInt(param.getValue());
			for(int i = 0; i < numberOfThreads; i++)
			{
				tt = new TaskThread(this);
				tt.setName("STask-" + i);
				tt.start();
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
			return;
		}
	}

	/**
	 * Initializes all TaskDetail Objects. 
	 */
	private void initializeTasks()
	{
		Parameters param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"SLEEP_MINUTES","");
		int sleepMinutes;
		try{
			sleepMinutes = Integer.parseInt(param.getValue());
		}catch (NumberFormatException e) {
			sleepMinutes = 0;
		}
		activationTask = new TaskDetail(ACTIVATION_TASK, 0, sleepMinutes);
		deactivationTask = new TaskDetail(DEACTIVATION_TASK, 0, sleepMinutes);
		
		param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"TIME_INTERVAL_FOR_PENDING_REQUEST_POSTPAID_IN_MINUTES","15");
		int intervalForPostpaidPendingQuery;
		try{
			intervalForPostpaidPendingQuery = Integer.parseInt(param.getValue());
		}catch (NumberFormatException e) {
			intervalForPostpaidPendingQuery = 15;
		}
		activationPollingTaskPost = new TaskDetail(ACTIVATION_POLLING_TASK_POSTPAID, intervalForPostpaidPendingQuery, sleepMinutes);
		
		param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"TIME_INTERVAL_FOR_PENDING_REQUEST_PREPAID_IN_MINUTES","240");
		int intervalForPrepaidPendingQuery;
		try{
			intervalForPrepaidPendingQuery = Integer.parseInt(param.getValue());
		}catch (NumberFormatException e) {
			intervalForPrepaidPendingQuery = 4*60;
		}
		
		activationPollingTaskPre = new TaskDetail(ACTIVATION_POLLING_TASK_PREPAID, intervalForPrepaidPendingQuery, sleepMinutes);
		
		param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"TIME_INTERVAL_FOR_DEACT_PENDING_REQUEST_POSTPAID_IN_MINUTES","15");
		int intervalForPostpaidDeactPendingQuery;
		try{
			intervalForPostpaidDeactPendingQuery = Integer.parseInt(param.getValue());
		}catch (NumberFormatException e) {
			intervalForPostpaidDeactPendingQuery = 15;
		}
		deactivationPollingTaskPost = new TaskDetail(DEACTIVATION_POLLING_TASK_POSTPAID, intervalForPostpaidDeactPendingQuery, sleepMinutes);
		
		param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"TIME_INTERVAL_FOR_DEACT_PENDING_REQUEST_PREPAID_IN_MINUTES","240");
		int intervalForPrepaidDeactPendingQuery;
		try{
			intervalForPrepaidDeactPendingQuery = Integer.parseInt(param.getValue());
		}catch (NumberFormatException e) {
			intervalForPrepaidDeactPendingQuery = 4*60;
		}
		deactivationPollingTaskPre = new TaskDetail(DEACTIVATION_POLLING_TASK_PREPAID, intervalForPrepaidDeactPendingQuery, sleepMinutes);
		
		param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"TIME_INTERVAL_FOR_SETTING_REQUEST_POSTPAID_IN_MINUTES","5");
		int intervalForPostpaidSetting = Integer.parseInt(param.getValue());
		addSettingTaskPost = new TaskDetail(ADD_SETTING_TASK_POSTPAID, intervalForPostpaidSetting, sleepMinutes);
		
		param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"TIME_INTERVAL_FOR_SETTING_REQUEST_PREPAID_IN_MINUTES","240");
		int intervalForPrepaidSetting;
		try{
			intervalForPrepaidSetting = Integer.parseInt(param.getValue());
		}catch (NumberFormatException e) {
			intervalForPrepaidSetting = 5;
		}
		addSettingTaskPre = new TaskDetail(ADD_SETTING_TASK_PREPAID, intervalForPrepaidSetting, sleepMinutes);
		deleteSettingTask = new TaskDetail(DELETE_SETTING_TASK, 0, sleepMinutes);
		deleteSelectionTask = new TaskDetail(DELETE_SELECTION_TASK, 0, sleepMinutes);
		updateToDeactivateTask = new TaskDetail(UPDATE_TO_DEACTIVATE, 0, sleepMinutes);
		updateToToBeDeletedTask = new TaskDetail(UPDATE_TO_TO_BE_DELETED, 0, sleepMinutes);
		activationGraceTask = new TaskDetail(ACTIVATION_GRACE_TASK, 0, sleepMinutes); 
        selectionGraceTask = new TaskDetail(SELECTION_GRACE_TASK, 0, sleepMinutes); 

	}

	protected static String getTaskName(int taskType)
	{
		switch(taskType)
		{
			case ACTIVATION_TASK: return "ACT_TASK";
			case DEACTIVATION_TASK: return "DCT_TASK";
			case ACTIVATION_POLLING_TASK_POSTPAID: return "ACT_POLLING_TASK_POST";
			case ACTIVATION_POLLING_TASK_PREPAID: return "ACT_POLLING_TASK_PRE";
			case DEACTIVATION_POLLING_TASK_POSTPAID: return "DCT_POLLING_TASK_POST";
			case DEACTIVATION_POLLING_TASK_PREPAID: return "DCT_POLLING_TASK_PRE";
			case ADD_SETTING_TASK_POSTPAID: return "ADD_SETTING_TASK_POST";
			case ADD_SETTING_TASK_PREPAID: return "ADD_SETTING_TASK_PRE";
			case DELETE_SETTING_TASK: return "DELETE_SETTING_TASK";
			case DELETE_SELECTION_TASK: return "DELETE_SELECTION_TASK";
			case UPDATE_TO_DEACTIVATE: return "UPDATE_TO_DCT_TASK";
			case UPDATE_TO_TO_BE_DELETED: return "UPDATE_TO_TO_BE_DELETED";
			case ACTIVATION_GRACE_TASK: return "ACT_GRACE_TASK"; 
            case SELECTION_GRACE_TASK: return "SEL_GRACE_TASK"; 

		}
		return "NULL [" + taskType + "]";
	}

	public void copyList(TaskDetail taskDetail, ArrayList al)
	{
		if(al != null)
		{
			int iCount = taskDetail.size();
			synchronized(m_process_lock)
			{
				taskDetail.m_arrayList.addAll(al);
			}
			int fCount = taskDetail.size();
			taskDetail.updateCounts(iCount, fCount);

			logger.info("RBT::created list for task " + taskDetail.getName() + " -  Processed = " + taskDetail.getProcessedCount() + ", Remaining = " + taskDetail.getInitialCount() + ", Added = " + taskDetail.getIncrementedCount());
		}
		else
		{
			taskDetail.updateCounts();
			logger.info("RBT::no pending records in db for task " + taskDetail.getName() + " -  Processed = " + taskDetail.getProcessedCount() + ", Remaining = " + taskDetail.getInitialCount() + ", Added = 0");
		}
	}

	private void updateCountsForTask(TaskDetail taskDetail)
	{
		taskDetail.updateCounts();
		logger.info("RBT::not adding records for task " + taskDetail.getName() + " -  Processed = " + taskDetail.getProcessedCount() + ", Remaining = " + taskDetail.getInitialCount() + ", Added = 0");
	}

	private boolean canAddGraceTasks() {
		Calendar nowCal = Calendar.getInstance();
		Calendar graceStart = Calendar.getInstance();
		Calendar graceEnd = Calendar.getInstance();
		Parameters param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"GRACE_START_TIME","1400");
		String  graceStartTime = param.getValue();
		graceStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(graceStartTime.substring(0, 2)));
		graceStart.set(Calendar.MINUTE, Integer.parseInt(graceStartTime.substring(2)));
		param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"GRACE_END_TIME","1800");
		String graceEndTime = param.getValue();
		graceEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(graceEndTime.substring(0, 2)));
		graceEnd.set(Calendar.MINUTE, Integer.parseInt(graceEndTime.substring(2)));

		if(nowCal.compareTo(graceStart) >= 0 && nowCal.compareTo(graceEnd) <= 0)
		return true;
		else
		return false;
	}

	private void refreshPrimaryLists()
	{
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Calendar calPresent = Calendar.getInstance();
		ArrayList al;
		Parameters param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"FETCH_SIZE","1000");
		int fetchSize;
		try{
			fetchSize = Integer.parseInt(param.getValue());
		}catch (Exception e) {
			fetchSize = 1000;
		}
		if(activationTask.canAddToList(calPresent))
		{
			al = rbtDBManager.smGetActivatedSubscribersAsList(fetchSize);
			copyList(activationTask, al);
		}
		else
			updateCountsForTask(activationTask);

		if(deactivationTask.canAddToList(calPresent))
		{
			al = rbtDBManager.smGetDeactivatedSubscribersAsList(fetchSize);
			copyList(deactivationTask, al);
		}
		else
			updateCountsForTask(deactivationTask);

		if(deleteSettingTask.canAddToList(calPresent))
		{
			al = rbtDBManager.smGetSettingsToBeDeleted(fetchSize);
			copyList(deleteSettingTask, al);
		}
		else
			updateCountsForTask(deleteSettingTask);

		if(deleteSelectionTask.canAddToList(calPresent))
		{
			al = rbtDBManager.getSelectionsToBeDeleted(fetchSize);
			copyList(deleteSelectionTask, al);
		}
		else
			updateCountsForTask(deleteSelectionTask);

		if(addSettingTaskPost.canAddToList(calPresent))
		{
			addSettingTaskPost.incrementCalendar();
			al = rbtDBManager.smGetActivatedSelectionsPostAsList(fetchSize);
			copyList(addSettingTaskPost, al);
		}
		else
			updateCountsForTask(addSettingTaskPost);

		if(addSettingTaskPre.canAddToList(calPresent))
		{
			addSettingTaskPre.incrementCalendar();
			al = rbtDBManager.smGetActivatedSelectionsPreAsList(fetchSize);
			copyList(addSettingTaskPre, al);
		}
		else
			updateCountsForTask(addSettingTaskPre);
		
		if(updateToDeactivateTask.canAddToList(calPresent))
		{
			updateToDeactivateTask.incrementCalendar();
			al = rbtDBManager.getUpdateToDeactivateSubscribers(fetchSize);
			copyList(updateToDeactivateTask, al);
		}
		else
			updateCountsForTask(updateToDeactivateTask);

		/*totalCountAddedtoQueue = activationTask.size() + deactivationTask.size() + deleteSettingTask.size() + deleteSelectionTask.size() 
		+ addSettingTaskPost.size() + addSettingTaskPre.size() + updateToDeactivateTask.size();
		*/
		if(updateToToBeDeletedTask.canAddToList(calPresent)){
			updateToToBeDeletedTask.incrementCalendar(); 
            al = rbtDBManager.getUpdateToToBeDeletedDownloads(fetchSize); 
            copyList(updateToToBeDeletedTask, al); 
		}
		else 
            updateCountsForTask(updateToToBeDeletedTask); 

		boolean addGraceTask = canAddGraceTasks();
		if(addGraceTask && activationGraceTask.canAddToList(calPresent)) 
        { 
                activationGraceTask.incrementCalendar(); 
                al = rbtDBManager.getActivationGraceRecords(fetchSize); 
                copyList(activationGraceTask, al); 
        } 
        else 
                updateCountsForTask(activationGraceTask); 

        if(addGraceTask && selectionGraceTask.canAddToList(calPresent)) 
        { 
                selectionGraceTask.incrementCalendar(); 
                al = rbtDBManager.getSelectionGraceRecords(fetchSize); 
                copyList(selectionGraceTask, al); 
        } 
        else 
                updateCountsForTask(selectionGraceTask); 
		totalCountAddedtoQueue = activationTask.size() + deactivationTask.size() 
                    + deleteSettingTask.size() + deleteSelectionTask.size() + addSettingTaskPost.size() 
                    + addSettingTaskPre.size() + updateToDeactivateTask.size() 
                    + updateToToBeDeletedTask.size() + activationGraceTask.size() 
                    + selectionGraceTask.size();  

	}
	private void refreshSecondaryLists()
	{
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Calendar calPresent = Calendar.getInstance();
		ArrayList al;
		Parameters param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"FETCH_SIZE","1000");
		int fetchSize;
		try{
			fetchSize = Integer.parseInt(param.getValue());
		}catch (Exception e) {
			fetchSize = 1000;
		}
		if(activationPollingTaskPost.canAddToList(calPresent))
		{
			activationPollingTaskPost.incrementCalendar();
			al = rbtDBManager.smGetActivationPendingSubscribersPost(fetchSize);
			copyList(activationPollingTaskPost, al);
		}
		else
			updateCountsForTask(activationPollingTaskPost);

		if(activationPollingTaskPre.canAddToList(calPresent))
		{
			activationPollingTaskPre.incrementCalendar();
			al = rbtDBManager.smGetActivationPendingSubscribersPre(fetchSize);
			copyList(activationPollingTaskPre, al);
		}
		else
			updateCountsForTask(activationPollingTaskPre);

		if(deactivationPollingTaskPost.canAddToList(calPresent))
		{
			deactivationPollingTaskPost.incrementCalendar();
			al = rbtDBManager.smGetDeactivationPendingSubscribersPost(fetchSize);
			copyList(deactivationPollingTaskPost, al);
		}
		else
			updateCountsForTask(deactivationPollingTaskPost);

		if(deactivationPollingTaskPre.canAddToList(calPresent))
		{
			deactivationPollingTaskPre.incrementCalendar();
			al = rbtDBManager.smGetDeactivationPendingSubscribersPre(fetchSize);
			copyList(deactivationPollingTaskPre, al);
		}
		else
			updateCountsForTask(deactivationPollingTaskPre);

		totalCountAddedtoQueue = activationPollingTaskPost.size() + activationPollingTaskPre.size() 
		+ deactivationPollingTaskPost.size() + deactivationPollingTaskPre.size();
	}

	private String formRTIInfo( TaskDetail taskDetail)
	{
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("<" + taskDetail.getName());
		strBuffer.append(" processed_lastHour=" + taskDetail.getProcessedInHourCount());
		strBuffer.append(" processed_lastRun=" + taskDetail.getProcessedCount());
		strBuffer.append(" added=" + taskDetail.getIncrementedCount() + ">");
		strBuffer.append("</" + taskDetail.getName() + ">\n");

		return strBuffer.toString();
	}

	private void publishRTI()
	{

		StringBuffer sb = new StringBuffer();

		sb.append(formRTIInfo(activationTask));
		sb.append(formRTIInfo(deactivationTask));
		sb.append(formRTIInfo(secondDeamon.activationPollingTaskPost));
		sb.append(formRTIInfo(secondDeamon.activationPollingTaskPre));
		sb.append(formRTIInfo(secondDeamon.deactivationPollingTaskPost));
		sb.append(formRTIInfo(secondDeamon.deactivationPollingTaskPre));
		sb.append(formRTIInfo(addSettingTaskPost));
		sb.append(formRTIInfo(addSettingTaskPre));
		sb.append(formRTIInfo(deleteSettingTask));
		sb.append(formRTIInfo(deleteSelectionTask));
		sb.append(formRTIInfo(updateToDeactivateTask));
		sb.append(formRTIInfo(updateToToBeDeletedTask));
		 sb.append(formRTIInfo(activationGraceTask));
          sb.append(formRTIInfo(selectionGraceTask));
		m_mainInstance.publishRuntimeInfo(sb.toString());
	}

	public void run()
	{
		String method = "run";
		logger.info("RBT::inside run");

		initializeTasks();

		//If thread mode is primary, it starts primary threads, otherwise starts secondary threads. 
		if (m_thread_mode == RBT_THREAD_PRIMARY)
			startPrimaryThreads();
		else
			startSecondaryThreads();

		try
		{
			while(TATARBTDaemonOzonized.isOzoneThreadLive()) 
			{
				totalCountAddedtoQueue = 0;

				//logger.info("RBT::DBURL is " + db_url);

				if (m_thread_mode == RBT_THREAD_PRIMARY) refreshPrimaryLists();
				else refreshSecondaryLists();

				if(totalCountAddedtoQueue > 0)
				{
					logger.info("RBT::totalCountAddedtoQueue = " + totalCountAddedtoQueue + ". notifying all threads");
					synchronized (m_process_lock)
					{
						m_process_lock.notifyAll ();
					}
					logger.info("RBT::totalCountAddedtoQueue = " + totalCountAddedtoQueue + ". notified all threads");
				}

				if(m_thread_mode == RBT_THREAD_PRIMARY)
					publishRTI();
				int actualSleeepTime = 0;
				Parameters param = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,"SLEEP_MINUTES","");
				int sleepMinutes = Integer.parseInt(param.getValue());
				actualSleeepTime = sleepMinutes;

				if(actualSleeepTime > 0)
				{
					try
					{
						logger.info("RBT::inside try to sleep, for " + actualSleeepTime + " minutes for thread mode = " + m_thread_mode);
						Thread.sleep(1000*60*actualSleeepTime);
						logger.info("RBT::inside try after sleep, for " + actualSleeepTime + " minutes for thread mode = " + m_thread_mode);
					}
					catch(Exception e)
					{
						logger.info("RBT::exception " + e);
					}
				}
			}
			synchronized (m_process_lock)
			{
				m_process_lock.notifyAll ();
			}
			if(secondDeamon != null)
			{
				secondDeamon.interrupt();
				try
				{
					logger.info("RBT::calling join for the second thread");
					secondDeamon.join();
				}
				catch(Exception e)
				{

				}
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}
}
