/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.reliance;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.executor.Command;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtProvisioningRequestCommand extends Command
{
	private static Logger logger = Logger
			.getLogger(RbtProvisioningRequestCommand.class);

	private final ProvisioningRequests provisioningRequest;

	/**
	 * @param executor
	 * @param provisioningRequest
	 */
	public RbtProvisioningRequestCommand(RbtThreadPoolExecutor executor,
			ProvisioningRequests provisioningRequest)
	{
		super(executor);

		if (provisioningRequest == null)
		{
			throw new IllegalArgumentException(
					"ProvisioningRequest can not be null");
		}

		this.provisioningRequest = provisioningRequest;
	}

	/**
	 * @return the provisioningRequest
	 */
	public ProvisioningRequests getProvisioningRequest()
	{
		return provisioningRequest;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if (logger.isInfoEnabled())
			logger.info("Processing : " + provisioningRequest);

		try
		{
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();

			String subscriberID = provisioningRequest.getSubscriberId();
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);

			if (rbtDBManager.isSubscriberDeactivated(subscriber))
				RelianceShazamsInterface.downloadTune(this);
			else
				addSubscriberSelections(subscriber);

			ProvisioningRequestsDao.removeByRequestId(subscriberID, provisioningRequest
					.getRequestId());
			setCommandStatus(CommandStatus.EXECUTED);
		}
		catch (RelianceShazamsRetriableException e)
		{
			logger.debug(e.getMessage(), e);
			setCommandStatus(CommandStatus.RETRIABLE);
		}
		catch (RelianceShazamsException e)
		{
			logger.debug(e.getMessage(), e);
			// Request will be retried in next iteration
			setCommandStatus(CommandStatus.REJECTED);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			setCommandStatus(CommandStatus.EXECUTED);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.reliance.Command#getUniqueName ()
	 */
	@Override
	public String getUniqueName()
	{
		return provisioningRequest.getSubscriberId();
	}

	private boolean addSubscriberSelections(Subscriber subscriber)
	{
		if (logger.isInfoEnabled())
			logger.info("Subscriber is active inserting selection into rbt db");

		String subscriberStatus = Utility.getSubscriberStatus(subscriber);
		boolean isSubscriberActive = Utility.isUserActive(subscriberStatus);
		if (!isSubscriberActive)
		{
			writeEventLog(subscriber.subID(), subscriberStatus);
			return false;
		}

		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(provisioningRequest.getExtraInfo());
		String callerID = extraInfoMap
				.get(ProvisioningRequests.ExtraInfoKey.CALLER_ID.toString());
		if (callerID != null
				&& callerID.equalsIgnoreCase(WebServiceConstants.ALL))
			callerID = null;

		int categoryID = Integer
				.parseInt(extraInfoMap
						.get(ProvisioningRequests.ExtraInfoKey.CATEGORY_ID
								.toString()));

		int clipID = Integer.parseInt(extraInfoMap
				.get(ProvisioningRequests.ExtraInfoKey.CLIP_ID.toString()));

		String selectedBy = provisioningRequest.getMode();
		String selectionInfo = provisioningRequest.getModeInfo();
		String classType = provisioningRequest.getChargingClass();

		String subscriberID = subscriber.subID();
		boolean isPrepaid = subscriber.prepaidYes();
		String subYes = subscriber.subYes();
		String circleID = subscriber.circleID();

		Calendar endCal = Calendar.getInstance();
		endCal.set(2037, 0, 1, 0, 0, 0);
		Date endDate = endCal.getTime();
		Date startDate = null;

		int status = 1;
		int fromTime = 0;
		int toTime = 2359;
		boolean changeSubType = true;
		String messagePath = null;
		boolean useSubManager = true;
		String chargingPackage = null;
		String transID = null;
		boolean inLoop = false;
		int selectionType = 0;
		String interval = null;
		HashMap<String, String> selectionInfoMap = null;
		boolean useUIChargeClass = false;

		Category category = RBTCacheManager.getInstance().getCategory(
				categoryID);
		Categories categoriesObj = CategoriesImpl.getCategory(category);

		Clip clip = RBTCacheManager.getInstance().getClip(clipID);
		HashMap<String, Object> clipMap = new HashMap<String, Object>();
		clipMap.put("CLIP_CLASS", clip.getClassType());
		clipMap.put("CLIP_END", clip.getClipEndTime());
		clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
		clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
		clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
		clipMap.put("CLIP_NAME", clip.getClipName());

		String response = RBTDBManager.getInstance().addSubscriberSelections(
				subscriberID, callerID, categoriesObj, clipMap, null,
				startDate, endDate, status, selectedBy, selectionInfo, 0,
				isPrepaid, changeSubType, messagePath, fromTime, toTime,
				classType, useSubManager, true, "VUI", chargingPackage, subYes,
				null, circleID, true, false, transID, false, false, inLoop,
				subscriber.subscriptionClass(), subscriber, selectionType,
				interval, selectionInfoMap, useUIChargeClass, null, false);

		writeEventLog(subscriberID, response);

		logger.info("Add Subscriber Selection response: " + response);
		return (response.contains("SUCCESS"));
	}

	private void writeEventLog(String subscriberID, String response)
	{
		/*
		 * Log Format: subscriberID,action,actionResponse,URL,urlResponse|
		 * statusCode|_status,hitTime,responseTime
		 */
		StringBuilder builder = new StringBuilder();
		builder.append(subscriberID).append(",");
		builder.append(ProvisioningRequests.Type.SELECTION).append(",");
		builder.append(response).append(",,,,");

		RBTEventLogger.logEvent(RBTEventLogger.Event.ARBT, builder.toString());
	}
}
