package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadSubscriber;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTBulkUploadSubscriberDAO;
import com.onmobile.apps.ringbacktones.content.database.RBTBulkUploadTaskDAO;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.BulkSelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vasipalli.sreenadh
 * Thread processes Corporate tasks i.e Starting new campaign, Deactivating campaign, updating the edited campaigns etc. 
 */
public class RBTCorporateProcessor extends Thread implements WebServiceConstants
{
	private static Logger logger = Logger.getLogger(RBTCorporateProcessor.class);

	private RBTDaemonManager mainDaemonThread = null;
	ParametersCacheManager rbtParamCacheManager = null;
	RBTDBManager rbtDBManager = null;
	RBTClient rbtClient = null;
	RBTCacheManager rbtCacheManager = null;

	private final String TASK_TYPE 	= "CORPORATE";
	String m_sdrWorkingDir = ".";
	private static WriteDailyTrans m_callBackTrans = null;

	protected RBTCorporateProcessor(RBTDaemonManager mainDaemonThread)
	{
		try
		{
			setName("RBTCorporateProcessor");
			this.mainDaemonThread = mainDaemonThread;
			init();
		}
		catch(Exception e)
		{
			logger.error("Issue in creating RBTCorporateProcessor", e);
		}
	}

	public void init()
	{
		rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		rbtClient = RBTClient.getInstance();
		rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
		
		m_sdrWorkingDir = getParamAsString("DAEMON", "SDR_WORKING_DIR", ".");
		
		ArrayList<String> headers = new ArrayList<String> ();
    	headers.add("TYPE");
    	headers.add("SUBSCRIBER_ID");
    	headers.add("UPDATED_VALUES");
    	headers.add("IS_SUB_DEACTIVATED");
    	headers.add("RESPONSE");
    	m_callBackTrans = new WriteDailyTrans(m_sdrWorkingDir, "CORPORATE_PROCESSOR", headers);
	}

	public void run()
	{
		while(mainDaemonThread != null && mainDaemonThread.isAlive()) {
			deactivateCampaigns();
			startNewCampaigns();
			processEditedCampaigns();
			processFailureRecordsOfLiveCampaigns();
			try
			{
				logger.info("RBTCorporateProcessor Thread Sleeping for 5 minutes............");
				Thread.sleep(getParamAsInt("SLEEP_INTERVAL_MINUTES", 5) * 60 * 1000);
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}

	}	

	/**
	 * Get all campaigns whose taskStatus is 0 (NEW) and taskType is CORPORATE
	 * Process each task & write response back to TRANS file 
	 */
	private void startNewCampaigns()
	{
		List<RBTBulkUploadTask> rbtBulkUploadTasks = RBTBulkUploadTaskDAO.getRBTBulkTasks(BULKTASK_STATUS_NEW, TASK_TYPE, null, null);
		String response = ERROR;
		if (rbtBulkUploadTasks.size() == 0)
		{
			logger.info("No New camapaigns to start");
			return;
		}

		for (RBTBulkUploadTask rbtBulkUploadTask : rbtBulkUploadTasks) 
		{
			if (isTobeDeactivatedCampaignsExists())
			{
				logger.info("There are campaigns to be deactivated. Lets deactivate those first before starting New campaigns");
				return;
			}
			
			File file = null;
			Date currentTime = new Date();
			if (rbtBulkUploadTask.getEndTime() != null && currentTime.after(rbtBulkUploadTask.getProcessTime()))
			{
				try
				{
					BulkSelectionRequest bulkSelectionRequest = new BulkSelectionRequest();
					bulkSelectionRequest.setTaskID(String.valueOf(rbtBulkUploadTask.getTaskId()));

					file = RBTClient.getInstance().processBulkTask(bulkSelectionRequest);
					if (file != null)
						file.delete();

					if (bulkSelectionRequest.getResponse().equalsIgnoreCase(SUCCESS))
						logger.info("Task ID :"+rbtBulkUploadTask.getTaskId() +" executed Successfully");
					response = bulkSelectionRequest.getResponse();
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
				finally
				{
					writeTrans("NEW CAMPAIGN", rbtBulkUploadTask.getTaskMode(), null, false, response);
				}
			}
		}
	}

	/**
	  Get all campaigns whose taskType is CORPORATE ,taskStatus is 1 and endDate < sysdate
	  For each campaign get all active subscribers from RBT_BULK_UPLOAD_SUBSCRIBERS table 
	  Loop through the subscribers and initiate corporate deactivation request (
	  	If subscriber has only corporate song, subscriber will be deactivated, else only corporate song will be deactivated
	  )
	  If yes, deactivate only corporate selection 
	     else deactivate subscriber
	 */
	private void deactivateCampaigns()
	{
		List<RBTBulkUploadTask> rbtBulkUploadTasks = RBTBulkUploadTaskDAO.getTobeDeactivatedBulkTasks(BULKTASK_STATUS_SUCCESS , TASK_TYPE);

		if (rbtBulkUploadTasks.size() == 0)
		{
			logger.info("No Camapaigns to deactivate");
			return;
		}
		for (RBTBulkUploadTask rbtBulkUploadTask : rbtBulkUploadTasks) 
		{
			boolean response = false;
			
			long endTime = rbtBulkUploadTask.getEndTime().getTime(); // End time of campaign
			if((System.currentTimeMillis() - endTime) > 24*60*60*1000)
			{
				// Do not retry number after 24 hour period
				logger.info("24 hours period is over & Deactivating the campaign permanently");
				response = true;
			}
			else
			{	
				// Getting active numbers of live campaign
				List<RBTBulkUploadSubscriber> bulkUploadSubscribers = RBTBulkUploadSubscriberDAO.getRBTBulkUploadSubscribers(rbtBulkUploadTask.getTaskId(), 1);
				response = processDeleteCampaign(rbtBulkUploadTask, bulkUploadSubscribers, "DEACTIVATING CAMPAIGN");
			}
			
			if (response)
			{	
				rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_CAMPAIGN_ENDED); 
				RBTBulkUploadTaskDAO.updateRBTBulkUploadTask(rbtBulkUploadTask);
			}
		}
	}

	/**
	 *  Get all campaigns whose taskStatus is 4 (EDITED) & taskType is CORPORATE
	 *  Get subscribers belong to each TaskID from RBT_BULK_UPLOAD_SUBSCRIBERS table
	 *  Check edited values & process each subscriber
	 *  Write trans log for each subscriber
	 */
	private void processEditedCampaigns()
	{
		List<RBTBulkUploadTask> rbtBulkUploadTasks = RBTBulkUploadTaskDAO.getRBTBulkTasks(BULKTASK_STATUS_EDITED, TASK_TYPE, null, null);
		if (rbtBulkUploadTasks.size() == 0)
		{
			logger.info("No Edited Camapaigns to process");
			return;
		}
				
		for (RBTBulkUploadTask rbtBulkUploadTask : rbtBulkUploadTasks) 
		{
			try
			{
				HashMap<String, String> taskInfoMap = DBUtility.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
				if (taskInfoMap != null && taskInfoMap.containsKey(EDIT_TASK))
				{
					int tasksEdited = Integer.parseInt(taskInfoMap.get(EDIT_TASK));
					logger.info("task edited  value >"+ tasksEdited);
					if (tasksEdited == 0)
						continue;

					if (isTaskEdited(tasksEdited, CAMPAIGN_DELETE))
					{
						List<RBTBulkUploadSubscriber> bulkUploadSubscribers = RBTBulkUploadSubscriberDAO.getRBTBulkUploadSubscribers(rbtBulkUploadTask.getTaskId());
						boolean response = processDeleteCampaign(rbtBulkUploadTask, bulkUploadSubscribers, "DELETING CAMPAIGN");
						if (response)
						{	
							rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_CAMPAIGN_ENDED); 
							RBTBulkUploadTaskDAO.updateRBTBulkUploadTask(rbtBulkUploadTask);
						}
						continue;
					}
					if (isTaskEdited(tasksEdited, CAMPAIGN_ADD_SUBSCRIBERS))
					{
						// Activates given numbers 
						List<RBTBulkUploadSubscriber> addSubscriberList = RBTBulkUploadSubscriberDAO.getRBTBulkUploadSubscribers(rbtBulkUploadTask.getTaskId(), BULKTASK_STATUS_NEW);
						addNumbersToLiveCampaign(rbtBulkUploadTask, addSubscriberList, true);
					}
					if (isTaskEdited(tasksEdited, CAMPAIGN_DELETE_SUBSCRIBERS))
					{	
						// deletes the given numbers
						List<RBTBulkUploadSubscriber> deleteSubscriberList = RBTBulkUploadSubscriberDAO.getRBTBulkUploadSubscribers(rbtBulkUploadTask.getTaskId(), BULKTASK_SUBSCRIBER_DELETE);
						processDeleteCampaign(rbtBulkUploadTask, deleteSubscriberList, "DELETING NUMBERS");
					}
					if (isTaskEdited(tasksEdited, CAMPAIGN_UPDATED))
					{
						processUpdatedCompaign(rbtBulkUploadTask);
					}
					rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_SUCCESS); 
					RBTBulkUploadTaskDAO.updateRBTBulkUploadTask(rbtBulkUploadTask);
				}
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
	}

	private boolean isTaskEdited(int editValue, int taskValue)
	{
		boolean isEdited = ((editValue & taskValue) == taskValue);
		return isEdited;
	}

	private String processUpdatedCompaign(RBTBulkUploadTask rbtBulkUploadTask)
	{
		String response = SUCCESS;
		int fromTimeHrs = 0;
		int fromTimeMinutes = 0;
		int toTimeHrs = 23;
		int toTimeMinutes = 59;
		String dayOfWeek = null;
		String clipID = null;
		String campaignID = null;
        
		campaignID = String.valueOf(rbtBulkUploadTask.getTaskId());
		HashMap<String, String> taskInfoMap = DBUtility.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());

		if (taskInfoMap == null)
			return response;
		
		if (taskInfoMap.containsKey(param_fromTime))
			fromTimeHrs = Integer.parseInt(taskInfoMap.get(param_fromTime));
		if (taskInfoMap.containsKey(param_fromTimeMinutes))
			fromTimeMinutes = Integer.parseInt(taskInfoMap.get(param_fromTimeMinutes));
		if (taskInfoMap.containsKey(param_toTime))
			toTimeHrs = Integer.parseInt(taskInfoMap.get(param_toTime));
		if (taskInfoMap.containsKey(param_toTimeMinutes))
			toTimeMinutes = Integer.parseInt(taskInfoMap.get(param_toTimeMinutes));

		if (taskInfoMap.containsKey(param_interval))
			dayOfWeek = taskInfoMap.get(param_interval);
		if (taskInfoMap.containsKey(param_clipID))
			clipID = taskInfoMap.get(param_clipID);
		
		//If PromoId = y , then promoId comes in clipId parameter.So , again assigning to clipId.
		if(taskInfoMap.containsKey(param_isPromoID)){
			String promoId = taskInfoMap.get(param_clipID);
			Clip clip = rbtCacheManager.getClipByPromoId(promoId);
			if(clip != null)
				clipID = String.valueOf(clip.getClipId());
		}
			

		String editString = "(FromTime:"+fromTimeHrs+fromTimeMinutes+",ToTime:"+toTimeHrs+toTimeMinutes+",ClipID:"+clipID+",Interval:"+dayOfWeek+")";

		List<RBTBulkUploadSubscriber> bulkUploadSubscribers = RBTBulkUploadSubscriberDAO
		.getRBTBulkUploadSubscribers(rbtBulkUploadTask.getTaskId());

		for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkUploadSubscribers) 
		{
			try
			{
				SubscriberStatus[] subscriberStatus = RBTDBManager
				.getInstance().getAllActiveSubSelectionRecords(rbtBulkUploadSubscriber.getSubscriberId(), 0);

				if (subscriberStatus == null || subscriberStatus.length == 0)
				{
					response = "NO ACTIVE SELECTION TO UPDATE";
					logger.info("No active selection to update");
					continue;
				}

				for (SubscriberStatus subStatus : subscriberStatus) 
				{
					if (subStatus.selType() != 2 && subStatus.status() != 80) // If its not corporateSelection , continue;
						continue;

					HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subStatus.extraInfo());
					if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.CAMPAIGN_ID)
							&& campaignID.equalsIgnoreCase(extraInfoMap.get(iRBTConstant.CAMPAIGN_ID)))
					{
						SelectionRequest selectionRequest = new SelectionRequest(rbtBulkUploadSubscriber.getSubscriberId());
						selectionRequest.setFromTime(fromTimeHrs);
						selectionRequest.setFromTimeMinutes(fromTimeMinutes);
						selectionRequest.setToTime(toTimeHrs);
						selectionRequest.setToTimeMinutes(toTimeMinutes);
						if (dayOfWeek != null)
							selectionRequest.setInterval(dayOfWeek);
						if (clipID != null)
							selectionRequest.setClipID(clipID);

						selectionRequest.setInfo("MODIFY");
						selectionRequest.setRefID(subStatus.refID());
						rbtClient.updateSubscriberSelection(selectionRequest);
						response = selectionRequest.getResponse();
					}
				}
			}
			catch(Exception e)
			{
				response = ERROR;
				logger.error("", e);
			}
			finally
			{
				writeTrans("UPDATE NUMBERS", rbtBulkUploadSubscriber.getSubscriberId(), editString, false, response);
			}
		}
		return response;
	}

	private boolean processDeleteCampaign(RBTBulkUploadTask rbtBulkUploadTask, List<RBTBulkUploadSubscriber> bulkUploadSubscribers, String type)
	{
		boolean campaignEnded = true;
		
		if (bulkUploadSubscribers == null || bulkUploadSubscribers.size() == 0)
		{
			logger.info("No numbers to process");
			return campaignEnded;
		}
		
		for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkUploadSubscribers) 
		{
			if  (rbtBulkUploadSubscriber.getStatus() == BULKTASK_SUBSCRIBER_DELETED)
			{
				if (logger.isDebugEnabled())
					logger.debug("Subscriber already deactivated : " + rbtBulkUploadSubscriber);

				continue;
			}

			boolean isSubscriberDeactivated = true; 
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(rbtBulkUploadSubscriber.getSubscriberId());
			try
			{
				subscriptionRequest.setMode("CORPORATE");
				if (RBTParametersUtils.getParamAsBoolean("DAEMON", "COMPLETE_DEACTIVATION_FOR_CORPORATE_ALLOWED", "FALSE")) {
					subscriptionRequest.setIsDeactCorporateUser(true);
				}
				subscriptionRequest.setIsCorporateDeactivation(true);
				subscriptionRequest.setCheckSubscriptionClass(false); // Don't need to check subscription class in case of corporate

				rbtClient.deactivateSubscriber(subscriptionRequest);
				isSubscriberDeactivated = (subscriptionRequest.getResponse().equals(SUCCESS)
						|| subscriptionRequest.getResponse().equals(USER_NOT_EXISTS)
						|| subscriptionRequest.getResponse().equals(DEACTIVE) || subscriptionRequest.getResponse().equals(DEACT_PENDING));

				if (isSubscriberDeactivated)
				{	
					// Updates the subscriber to DELETED
					rbtBulkUploadSubscriber.setStatus(BULKTASK_SUBSCRIBER_DELETED);  
					rbtBulkUploadSubscriber.setReason("Deleted from live campaign");
					RBTBulkUploadSubscriberDAO.updateRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);
				}
			}
			catch(Exception e)
			{
				isSubscriberDeactivated = false;
				logger.error("", e);
			}
			finally
			{
				writeTrans(type, rbtBulkUploadSubscriber.getSubscriberId(), null, true, subscriptionRequest.getResponse());
			}

			campaignEnded = campaignEnded && isSubscriberDeactivated;
		}
		return campaignEnded;
		
	}
	private void addNumbersToLiveCampaign(RBTBulkUploadTask rbtBulkUploadTask, List<RBTBulkUploadSubscriber> bulkUploadSubscribers, boolean isEditedNumberList)
	{
		String response = SUCCESS;
		if (bulkUploadSubscribers == null || bulkUploadSubscribers.size() == 0)
		{
			logger.info("No numbers to process");
			return;
		}
		
		HashMap<String, String> taskInfoMap = DBUtility.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
		
		for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkUploadSubscribers) 
		{
			try
			{
				String clipID = null;
				String shuffleID = null;
				
				SelectionRequest selectionRequest = new SelectionRequest(rbtBulkUploadSubscriber.getSubscriberId());
				selectionRequest.setMode(rbtBulkUploadTask.getActivatedBy());
				selectionRequest.setModeInfo(rbtBulkUploadTask.getActInfo());
				selectionRequest.setChargeClass(rbtBulkUploadTask.getSelectionClass());
				selectionRequest.setSubscriptionClass(rbtBulkUploadTask.getActivationClass());
				selectionRequest.setStatus(rbtBulkUploadTask.getSelectionType());
				selectionRequest.setIsPrepaid(String.valueOf(rbtBulkUploadSubscriber.getSubscriberType()).equalsIgnoreCase(YES));
			
				if(taskInfoMap.containsKey(param_fromTime))
					selectionRequest.setFromTime(Integer.parseInt(taskInfoMap.get(param_fromTime)));
				if(taskInfoMap.containsKey(param_fromTimeMinutes))
					selectionRequest.setFromTimeMinutes(Integer.parseInt(taskInfoMap.get(param_fromTimeMinutes)));
				if(taskInfoMap.containsKey(param_toTime))
					selectionRequest.setToTime(Integer.parseInt(taskInfoMap.get(param_toTime)));
				if(taskInfoMap.containsKey(param_toTimeMinutes))
					selectionRequest.setToTimeMinutes(Integer.parseInt(taskInfoMap.get(param_toTimeMinutes)));
				if(taskInfoMap.containsKey(param_interval))
					taskInfoMap.put(param_interval, taskInfoMap.get(param_interval));
				if(taskInfoMap.containsKey(param_callerID))
					selectionRequest.setCallerID(taskInfoMap.get(param_callerID));
				if(taskInfoMap.containsKey(param_categoryID))
					selectionRequest.setCategoryID(taskInfoMap.get(param_categoryID));
				if(taskInfoMap.containsKey(param_ignoreActiveUser))
					selectionRequest.setIgnoreActiveUser(taskInfoMap.get(param_ignoreActiveUser).equalsIgnoreCase(YES));
				if(taskInfoMap.containsKey(param_dontSMSInBlackOut))
					selectionRequest.setDontSMSInBlackOut(taskInfoMap.get(param_dontSMSInBlackOut).equalsIgnoreCase(YES));
				if(taskInfoMap.containsKey(param_removeExistingSetting))
					selectionRequest.setRemoveExistingSetting(taskInfoMap.get(param_removeExistingSetting).equalsIgnoreCase(YES));
				
				clipID = rbtBulkUploadSubscriber.getContentId();
				if(clipID ==  null) 
				{
					clipID = taskInfoMap.get(param_clipID);	
				}
				
				if (clipID != null) {
					if (clipID.charAt(0) == 'S' || clipID.charAt(0) == 's') {
						shuffleID = clipID.substring(1);
						if (taskInfoMap.containsKey(param_isPromoID)) {
							com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = rbtCacheManager
									.getCategoryByPromoId(shuffleID);// category
							if (category != null)
								shuffleID = String.valueOf(category
										.getCategoryId());
						}
						selectionRequest.setCategoryID(shuffleID); 
					} else {
						if (taskInfoMap.containsKey(param_isPromoID)) {
							Clip clip = rbtCacheManager
									.getClipByPromoId(clipID);
							if (clip != null)
								clipID = String.valueOf(clip.getClipId());
						}
						selectionRequest.setClipID(clipID);
					}
				}
				
				
				// Set rbtType to 2 if taskType is CORPORATE
				selectionRequest.setSelectionType(2);
				
				HashMap<String, String> userInfoMap = new HashMap<String, String>();
				HashMap<String, String> selectionInfoMap = new HashMap<String, String>();
				Set<String> keySet = taskInfoMap.keySet();
				for (String key : keySet)
				{
					if (key.startsWith(param_userInfo + "_"))
						userInfoMap.put(key, taskInfoMap.get(key));
					if (key.startsWith(param_selectionInfo + "_"))
						selectionInfoMap.put(key, taskInfoMap.get(key));
				}
				selectionInfoMap.put(CAMPAIGN_ID, String.valueOf(rbtBulkUploadTask.getTaskId()));
				selectionRequest.setUserInfoMap(userInfoMap);
				selectionRequest.setSelectionInfoMap(selectionInfoMap);
				
				rbtClient.addSubscriberSelection(selectionRequest);
				
				rbtBulkUploadSubscriber.setReason(SUCCESS);
				if(selectionRequest.getResponse().equalsIgnoreCase(SUCCESS))
					rbtBulkUploadSubscriber.setStatus(BULKTASK_STATUS_SUCCESS);
				else
					rbtBulkUploadSubscriber.setStatus(BULKTASK_STATUS_FAILURE);
	
				RBTBulkUploadSubscriberDAO.updateRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);
				
				response = selectionRequest.getResponse();
			}
			catch(Exception e)
			{
				response = ERROR;
				logger.error("", e);
			}
			finally
			{
				writeTrans((isEditedNumberList ? "ADD NUMBERS" : "FAILED NUMBERS REPROCESSING"), rbtBulkUploadSubscriber.getSubscriberId(), null, false, response);
			}
		}
	}
	
	public boolean isTobeDeactivatedCampaignsExists()
	{
		List<RBTBulkUploadTask> rbtBulkUploadTasks = RBTBulkUploadTaskDAO.getTobeDeactivatedBulkTasks(BULKTASK_STATUS_SUCCESS , TASK_TYPE);
		return (rbtBulkUploadTasks.size() == 0 ? false:true);
	}
	
	
	/**
	 * While processing new campaigns, if download with same wav file (i.e current corporate wav)is in deact pending, record will be rejected (reason: deact pending).
	 * RBT has to process these record again. Record will be retried to process for max 24 hours. After that it will be permanently rejected.
	 */
	public void processFailureRecordsOfLiveCampaigns()
	{
		List<RBTBulkUploadTask> rbtBulkUploadTasks = RBTBulkUploadTaskDAO.getRBTBulkTasks(BULKTASK_STATUS_SUCCESS, TASK_TYPE, null, null);

		for (RBTBulkUploadTask rbtBulkUploadTask : rbtBulkUploadTasks) 
		{
			long startTime = rbtBulkUploadTask.getProcessTime().getTime(); // Start time of campaign
			
			if((System.currentTimeMillis() - startTime) > 24*60*60*1000)
			{
				// Do not retry number after 24 hour period
				logger.info("24 hours period is over no need to reprocess the failed records");
				continue;
			}
			
			List<RBTBulkUploadSubscriber> failedBulkSubscriberList = new ArrayList<RBTBulkUploadSubscriber>();
			
			List<RBTBulkUploadSubscriber> bulkUploadSubscribers = RBTBulkUploadSubscriberDAO.getRBTBulkUploadSubscribers(rbtBulkUploadTask.getTaskId(), 2); // Gets all failed numbers 
			for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkUploadSubscribers) 
			{
				failedBulkSubscriberList.add(rbtBulkUploadSubscriber);
			}
			addNumbersToLiveCampaign(rbtBulkUploadTask, failedBulkSubscriberList, false);
		}
	}

	public boolean writeTrans(String type, String subscriberID, String updatedValues, boolean isSubDeactivation, String response)
	{
			HashMap<String,String> h = new HashMap<String,String> ();
			h.put("TYPE", type);
			h.put("SUBSCRIBER_ID", subscriberID);
			h.put("UPDATED_VALUES", updatedValues);
			h.put("IS_SUB_DEACTIVATED", isSubDeactivation+"");
			h.put("RESPONSE", response);
			
			if(m_callBackTrans != null)
			{
				m_callBackTrans.writeTrans(h);
				return true;
			}
			return false;
	}
	
	private int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = rbtParamCacheManager.getParameter("DAEMON", param, defaultVal+"").getValue();
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}
	private String getParamAsString(String type, String param, String defaultVal)
	{
		try{
			return rbtParamCacheManager.getParameter(type, param, defaultVal).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}
	
}
