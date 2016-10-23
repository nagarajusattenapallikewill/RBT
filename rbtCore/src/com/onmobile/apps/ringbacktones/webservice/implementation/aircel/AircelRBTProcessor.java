/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.aircel;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class AircelRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(AircelRBTProcessor.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processActivation(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processActivation(WebServiceContext task)
	{
		String response = null;

		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		boolean isLimitedPackRequest = false;

		response = isValidUser(task, subscriber);
		if (!response.equals(VALID) && !(task.containsKey(param_scratchCardNo) && response.equals(SUSPENDED)))
		{
			logger.info("RBT:: response: " + response);
			return response;
		}
		
		if(task.containsKey(param_cosID))
		{
		
			String cosID = task.getString(param_cosID);
			CosDetails cosDetails = CacheManagerUtil
			.getCosDetailsCacheManager().getCosDetail(cosID);

			if (cosDetails != null
					&& iRBTConstant.LIMITED_DOWNLOADS
					.equalsIgnoreCase(cosDetails.getCosType())) {
				isLimitedPackRequest = true;
			}
		}

		if (task.containsKey(param_scratchCardNo))
		{
			String scratchCardNo = task.getString(param_scratchCardNo);
			String context = "scratchcard";
			TransData transData = rbtDBManager.getTransData(scratchCardNo, context);
			if (transData == null)
			{
				logger.info("RBT:: response: " + INVALID);
				return INVALID;
			}

			task.put(param_transData, transData);
			if (!task.containsKey(param_subscriptionClass) && !task.containsKey(param_rentalPack) && transData.subscriberID() != null) {
				/*
				 * In trans data table, SUBSCRIBER_ID column will have subscription classes with comma separator  <active subscription class>,<in active subscription class>
				 * If user is new user, then will get in-active subscription class from subscriber id - index value 1.
				 * If user is active user, then will use same subscription class (index value 0) for upgrade.
				 * if new user subscription class is not configured, then will use same subscription class, what mapped with scratch card no in trans data.
				 */
				String[] subClasses = transData.subscriberID().split("\\,");
				String subClass = subClasses[0];
				if(!rbtDBManager.isSubActive(subscriber) && subClasses.length > 1) {
					subClass = subClasses[1];
				}
				task.put(param_rentalPack, subClass);				
			}

			String modeInfo = "scratchcard:" + task.getString(param_scratchCardNo);
			if (transData.accessCount() != null)
				modeInfo += "|refid:" + transData.accessCount();
			task.put(param_modeInfo, modeInfo);
		}

		if (task.containsKey(param_rentalPack))
		{
			if (subscriber != null && (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED) 
					|| subscriber.subYes().equals(iRBTConstant.STATE_SUSPENDED) || subscriber.subYes().equals(iRBTConstant.STATE_SUSPENDED_INIT)))
			{
				CosDetails cos = getCos(task, subscriber);
				String subscriptionClass = getSubscriptionClass(task, cos);
				String activatedBy = task.getString(param_mode);
				String activationInfo = task.getString(param_modeInfo);
				boolean success = false;
//				if (getParamAsBoolean(iRBTConstant.COMMON, iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE"))
				if (isSupportSMClientModel(task,BASE_OFFERTYPE))
				{
					try
					{
						success = smConvertSubscription(task, subscriptionClass, subscriberID, subscriber, activatedBy, activationInfo, isLimitedPackRequest);
					}
					catch(Exception e)
					{
						logger.error("", e);
						success = false;
					}
				}
				else
				{
					String subscriberActInfo = subscriber.activationInfo();
					String newActivationInfo = subscriberActInfo;

					int noOfScratchCardUsed = 0;
					int index = 0;
					while (true)
					{
						index = subscriberActInfo.indexOf("scratchcard", index);
						if (index < 0)
							break;

						index++;
						noOfScratchCardUsed++;
						if (noOfScratchCardUsed > 2)
							newActivationInfo = newActivationInfo.replaceFirst("scratchcard:[0-9]*\\|refid:[0-9]*\\|", "");
					}

					newActivationInfo += "|" + activationInfo;

					boolean concatActivationInfo = false;
					success = rbtDBManager.convertSubscriptionType(subscriberID, subscriber.subscriptionClass(),
							subscriptionClass, activatedBy, newActivationInfo, concatActivationInfo, 0, false, null, subscriber);
				}

				if(subscriber.subYes().equals(iRBTConstant.STATE_SUSPENDED) || subscriber.subYes().equals(iRBTConstant.STATE_SUSPENDED_INIT))
					rbtDBManager.updateExtraInfo(subscriberID, "IS_SUSPENDED", "TRUE");

				if (success)
				{
					response = SUCCESS;
					subscriber = rbtDBManager.getSubscriber(subscriberID);

					// Updated Subscriber object is storing in taskSession & it will be used to build the response element
					task.put(param_subscriber, subscriber);
				}
				else
					response = FAILED;

				logger.info("RBT:: response: " + response);

				if (response.equalsIgnoreCase(SUCCESS))
				{
					if (task.containsKey(param_scratchCardNo))
					{
						String scratchCardNo = task.getString(param_scratchCardNo);
						String context = "scratchcard";
						rbtDBManager.removeTransData(scratchCardNo, context);
					}
				}

				return response;
			}
		}

		response = super.processActivation(task);

		if (response.equalsIgnoreCase(SUCCESS))
		{
			if (task.containsKey(param_scratchCardNo))
			{
				String scratchCardNo = task.getString(param_scratchCardNo);
				String context = "scratchcard";
				rbtDBManager.removeTransData(scratchCardNo, context);
			}
		}

		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getModeInfo(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected String getModeInfo(WebServiceContext task)
	{
		if (task.containsKey(param_scratchCardNo))
		{
			String modeInfo = "scratchcard:" + task.getString(param_scratchCardNo);

			TransData transData = (TransData) task.get(param_transData);
			if (transData.accessCount() != null)
				modeInfo += "|refid:" + transData.accessCount();

			logger.info("RBT:: response: " + modeInfo);
			return modeInfo;
		}

		return super.getModeInfo(task);
	}
}
