/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.esia;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassMap;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientResponse;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class EsiaRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(EsiaRBTProcessor.class);
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processActivation(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public String processActivation(WebServiceContext webServiceContext)
	{
		String response = ERROR;

		try
		{
			String subscriberID = webServiceContext.getString(param_subscriberID);
			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
			boolean userDelayedDeact = DataUtils.isUserInDelayedDeactivationState(subscriber);
			if (userDelayedDeact)
			{
				String deactivatedBy = "NULL";
				
				Calendar calendar =  Calendar.getInstance();
				calendar.set(2037, Calendar.DECEMBER, 31, 0, 0, 0);
				Date endDate = calendar.getTime();
				
				boolean updated = rbtDBManager.updateSubscriber(subscriberID,
						null, deactivatedBy, null, endDate, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null,
						null, null, null, null);
				
				if (!webServiceContext.containsKey(param_rentalPack))
				{
					response = FAILED;
					if (updated)
					{
						webServiceContext.put(param_activatedNow, YES);
						response = SUCCESS;
						subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID, true);
						webServiceContext.put(param_subscriber, subscriber);
					}

					logger.info("response: " + response);
					return response;
				}
			}

			return super.processActivation(webServiceContext);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processDeactivation(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public String processDeactivation(WebServiceContext webServiceContext)
	{
		String response = ERROR;

		try
		{
			int delayedDeactivationHours = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DELAYED_DEACTIVATION_HOURS", 0);
			if (delayedDeactivationHours <= 0)
				return super.processDeactivation(webServiceContext);
			
			List<String> nonDelayedDeactivationModes = Arrays
					.asList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
									"NON_DELAYED_DEACTIVATION_MODES", "")
							.toUpperCase().split(","));

			String deactivatedBy = getMode(webServiceContext);
			if (nonDelayedDeactivationModes.contains(deactivatedBy.toUpperCase()))
				return super.processDeactivation(webServiceContext);

			String subscriberID = webServiceContext.getString(param_subscriberID);
			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
			String status = USER_NOT_EXISTS;
			if (subscriber != null)
				status = Utility.getSubscriberStatus(subscriber);
			if (subscriber == null || status.equalsIgnoreCase(DEACTIVE)
					|| status.equalsIgnoreCase(ACT_PENDING)
					|| status.equalsIgnoreCase(DEACT_PENDING))
			{
				logger.info("response: " + status);
				return status;
			}
			if (status.equalsIgnoreCase(GRACE))
			{
				Parameters parameter = parametersCacheManager.getParameter(iRBTConstant.COMMON, "ALLOW_DEACTIVATION_FOR_GRACE_USERS", "TRUE");
				boolean allowDeactivationForGraceUsers = parameter.getValue().equalsIgnoreCase("TRUE");
				if (!allowDeactivationForGraceUsers)
				{
					logger.info("response: " + NOT_ALLOWED_FOR_GRACE_USER);
					return NOT_ALLOWED_FOR_GRACE_USER;
				}
			}
			
			boolean forceDeactivation = webServiceContext.containsKey(param_forceDeactivation)
					&& webServiceContext.getString(param_forceDeactivation).equalsIgnoreCase(YES);
			boolean userDelayedDeact = DataUtils.isUserInDelayedDeactivationState(subscriber);

			if (forceDeactivation)
			{
				if (userDelayedDeact)
					return super.processDeactivation(webServiceContext);
				else
				{
					logger.info("response: " + NOT_DELAYED_DEACTIVATION);
					return NOT_DELAYED_DEACTIVATION;
				}
			}
			
			if (userDelayedDeact)
			{
				// Ignoring multiple deactivation requests
				logger.info("response: " + SUCCESS);
				return SUCCESS;
			}
			
			Calendar calendar =  Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, delayedDeactivationHours);
			Date endDate = calendar.getTime();
			
			boolean updated = rbtDBManager.updateSubscriber(subscriberID,
					null, deactivatedBy, null, endDate, null, null, null, null,
					null, null, null, null, null, null, null, null, null, null,
					null, null, null, null);
			
			response = FAILED;
			if (updated)
			{
				response = SUCCESS;
				subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID, true);
				webServiceContext.put(param_subscriber, subscriber);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processSelection(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processSelection(WebServiceContext task)
	{
		if (task.containsKey(param_mmContext) 
				&& task.getString(param_mmContext).equalsIgnoreCase(EMOTION_RBT))
		{
			if (task.containsKey(param_callerID) && !task.getString(param_callerID).equalsIgnoreCase(ALL))
			{
				logger.info("RBT:: response: " + EMOTION_RBT_NOT_ALLOWED_FOR_SPECIFIC_CALLER);
				return EMOTION_RBT_NOT_ALLOWED_FOR_SPECIFIC_CALLER;
			}

			String subscriberID = task.getString(param_subscriberID);
			String browsingLanguage = task.getString(param_browsingLanguage);
			
			SubscriberStatus[] settings = rbtDBManager.getAllActiveSubscriberSettings(subscriberID);
			String clipIDStr = null;
			String chargeClass = null;
			String categoryID = null;
			Parameters parameter = parametersCacheManager.getParameter("COMMON", "EMOTION_RBT_DEFAULT_CONFIG");
			if (parameter != null)
			{
				String emotionConfig = parameter.getValue();
				String[] tokens = emotionConfig.split(",");
				clipIDStr = tokens[0];

				if (tokens.length >= 2)
					chargeClass = tokens[1];
				if (tokens.length >= 3)
					categoryID = tokens[2];

			}
			boolean isDefaultEmotionExists = false;
			if (settings != null)
			{
				String clipWavFile = null;
				Clip clip = null;
				try
				{
					int clipID = Integer.parseInt(clipIDStr);
					clip = rbtCacheManager.getClip(clipID, browsingLanguage);
					if (clip != null)
						clipWavFile = clip.getClipRbtWavFile();
				}
				catch (NumberFormatException e)
				{
				}

				for (SubscriberStatus subscriberStatus : settings) 
				{
					if (clipWavFile!= null && subscriberStatus.subscriberFile().equalsIgnoreCase(clipWavFile))
						isDefaultEmotionExists = true;
				}
			}

			if (!isDefaultEmotionExists)
			{
				WebServiceContext tempTask = new WebServiceContext();
				tempTask.putAll(task);
				tempTask.put(param_categoryID, categoryID);
				tempTask.put(param_clipID, clipIDStr);
				tempTask.put(param_chargeClass, chargeClass);
				tempTask.put(param_inLoop, YES); // Default song will be added in Loop

				//tempTask.remove(param_status);
				tempTask.put(param_status, String.valueOf(0)); // Deafult song status will be 0
				tempTask.remove(param_profileHours);

				String response = super.processSelection(tempTask);
				// ClipID not exists only in case of base Emotion activation. Example user sends EMOTION to 888 
				if (!response.contains(SUCCESS) || !task.containsKey(param_clipID))
					return response;
			}
			task.remove(param_chargingModel); 
			
			int clipID = Integer.parseInt(task.getString(param_clipID));
			Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);
			if (clip != null)
			{
				task.put(param_useUIChargeClass, YES);
				task.put(param_categoryID, categoryID);
				task.put(param_inLoop, YES);
				task.put(param_chargeClass, clip.getClassType());
			}
		}
		return super.processSelection(task);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#deleteSetting(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String deleteSetting(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		
		int delayedDeactivationHours = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DELAYED_DEACTIVATION_HOURS", 0);
		if (delayedDeactivationHours > 0)
		{
			String subscriberID = webServiceContext.getString(param_subscriberID);
			SubscriberStatus[] settings = rbtDBManager.getAllActiveSubscriberSettings(subscriberID);
			if (settings != null && settings.length == 1)
			{
				String browsingLanguage = webServiceContext.getString(param_browsingLanguage);
				if (webServiceContext.containsKey(param_clipID))
				{
					int clipID = Integer.parseInt(webServiceContext.getString(param_clipID));
					Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);
					if (clip == null)
					{
						logger.info("response: " + CLIP_NOT_EXISTS);
						return CLIP_NOT_EXISTS;
					}

					if (!clip.getClipRbtWavFile().equalsIgnoreCase(settings[0].subscriberFile()))
						return FAILED;
				}
				else if (webServiceContext.containsKey(param_rbtFile))
				{
					String rbtFile = webServiceContext.getString(param_rbtFile);
					if (rbtFile.toLowerCase().endsWith(".wav"))
						rbtFile = rbtFile.substring(0, rbtFile.length() - 4);

					if (!rbtFile.equalsIgnoreCase(settings[0].subscriberFile()))
						return FAILED;
				}

				if (webServiceContext.containsKey(param_categoryID))
				{
					int categoryID = Integer.parseInt(webServiceContext.getString(param_categoryID));
					if (categoryID != settings[0].categoryID())
						return FAILED;
				}

				if (webServiceContext.containsKey(param_callerID))
				{
					String callerID = webServiceContext.getString(param_callerID);
					callerID = (callerID == null || callerID.equalsIgnoreCase(ALL)) ? null : callerID;

					if ((callerID == null && settings[0].callerID() != null)
							|| (callerID != null && !callerID.equals(settings[0].callerID())))
						return FAILED;
				}
				
				if (webServiceContext.containsKey(param_status))
				{
					int status = Integer.parseInt(webServiceContext.getString(param_status));
					if (status != settings[0].status())
						return FAILED;
				}

				DecimalFormat decimalFormat = new DecimalFormat("00");
				if (webServiceContext.containsKey(param_fromTime))
				{
					String fromHrs = webServiceContext.getString(param_fromTime);
					int fromTimeMinutes = 0;
					if (webServiceContext.containsKey(param_fromTimeMinutes))
						fromTimeMinutes = Integer.parseInt(webServiceContext.getString(param_fromTimeMinutes));

					int fromTime = Integer.parseInt(fromHrs + decimalFormat.format(fromTimeMinutes));
					if (fromTime != settings[0].fromTime())
						return FAILED;
				}
				if (webServiceContext.containsKey(param_toTime))
				{
					String toTimeHrs = webServiceContext.getString(param_toTime);
					int toTimeMinutes = 59;
					if (webServiceContext.containsKey(param_toTimeMinutes))
						toTimeMinutes = Integer.parseInt(webServiceContext.getString(param_toTimeMinutes));
					
					int toTime = Integer.parseInt(toTimeHrs + decimalFormat.format(toTimeMinutes));
					if (toTime != settings[0].toTime())
						return FAILED;
				}
				
				if (webServiceContext.containsKey(param_interval))
				{
					String selInterval = webServiceContext.getString(param_interval);
					if (!selInterval.equalsIgnoreCase(settings[0].selInterval()))
						return FAILED;
				}
				
				if (webServiceContext.containsKey(param_selectionType))
				{
					int selType = Integer.parseInt(webServiceContext.getString(param_selectionType));
					if (selType != settings[0].selType())
						return FAILED;
				}
				
				response = processDeactivation(webServiceContext);
				response = response.equalsIgnoreCase(SUCCESS) ? DELAYED_DEACT_SUCCESS : FAILED;

				logger.info("response: " + response);
				return response;
			}
		}

		if (webServiceContext.containsKey(param_mmContext) 
				&& webServiceContext.getString(param_mmContext).equalsIgnoreCase(EMOTION_RBT))
		{
			Parameters parameter = parametersCacheManager.getParameter("COMMON", "EMOTION_RBT_DEFAULT_CONFIG");
			if (parameter != null)
			{
				String emotionConfig = parameter.getValue();
				String[] tokens = emotionConfig.split(",");
				String clipIDStr = tokens[0];

				webServiceContext.put(param_clipID, clipIDStr);

				// Deactivating the user from Emotion Rbt Service by deactivating the default Emotion song
				response = super.deleteSetting(webServiceContext);

				if (response.equals(SUCCESS))
				{
					// Deactivation from Emotion Rbt Service is Success. 
					// Deactivating the other status 94 (emotion selection) also
					if (webServiceContext.containsKey(param_clipID))
						webServiceContext.remove(param_clipID);
					webServiceContext.put(param_status, String.valueOf(94));
					super.deleteSetting(webServiceContext);
				}
			}
		}
		else
			response = super.deleteSetting(webServiceContext);

		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#updateSelection(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public String updateSelection(WebServiceContext task)
	{
		String response = ERROR;

		try
		{
			String info = task.getString(param_info);
			if (info != null && info.equalsIgnoreCase(WEEKLY_TO_MONTHLY))
			{
				String subscriberID = task.getString(param_subscriberID);

				int rbtType = 0;
				if (task.containsKey(param_rbtType))
					rbtType = Integer.parseInt(task.getString(param_rbtType));

				boolean converted = false;

				List<ChargeClassMap> chargeClassMapsList = CacheManagerUtil.getChargeClassMapCacheManager().getChargeClassMapsForModeType("SMS", "CONVERT");
				if (chargeClassMapsList != null)
				{
					for (ChargeClassMap chargeClassMap : chargeClassMapsList)
					{
						String accessMode = chargeClassMap.getAccessMode();
						if (accessMode != null && (accessMode.equalsIgnoreCase("ALL") || accessMode.equalsIgnoreCase("SMS")))
						{
							String fromClass = chargeClassMap.getChargeClass();
							String toClass = chargeClassMap.getFinalClasstype();

							boolean result = false;
//							if (getParamAsBoolean(iRBTConstant.COMMON, iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE")) {
							if (isSupportSMClientModel(task,SELECTION_OFFERTYPE)) 
							{
								if(smUpgardeSelection(task, subscriberID, fromClass, toClass, rbtType)){
									converted = true;
								}
							}
							else{
								result = rbtDBManager.convertWeeklySelectionsClassTypeToMonthly(subscriberID, fromClass, toClass, rbtType);
								if (result)
									converted = true;
							}
							
						}
					}
				}

				if (converted)
					response = SUCCESS;
				else
					response = FAILED;
			}
			else
				response = super.updateSelection(task);
		}
		catch (Exception e)
		{
			logger.error("", e);
			response = ERROR;
		}

		logger.info("RBT:: response: " + response);
		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getSubscriptionClass(com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.content.CosDetail)
	 */
	@Override
	protected String getSubscriptionClass(WebServiceContext task, CosDetails cos)
	{
		try
		{
			if (task.getString(param_action).equalsIgnoreCase(action_acceptGift))
			{
				ViralSMSTable gift = null;
				if (task.containsKey(param_viralData))
					gift = (ViralSMSTable) task.get(param_viralData);
				else
				{
					String gifterID = task.getString(param_gifterID);
					String subscriberID = task.getString(param_subscriberID);
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					Date sentTime = dateFormat.parse(task.getString(param_giftSentTime));

					gift = rbtDBManager.getViralPromotion(gifterID, subscriberID, sentTime, "GIFTED");
				}

				if (gift != null)
				{
					task.put(param_viralData, gift);

					HashMap<String, String> giftExtraInfoMap = DBUtility.getAttributeMapFromXML(gift.extraInfo());
					if (giftExtraInfoMap != null)
					{
						String subscriptionClass = giftExtraInfoMap.get(iRBTConstant.SUBSCRIPTION_CLASS);

						logger.info("RBT:: response: " + subscriptionClass);
						return subscriptionClass;
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return super.getSubscriptionClass(task, cos);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getChargeClass(com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.content.Subscriber, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip)
	 */
	@Override
	protected String getChargeClass(WebServiceContext task, Subscriber subscriber, Category category, Clip clip)
	{
		try
		{
			if (task.getString(param_action).equalsIgnoreCase(action_acceptGift))
			{
				ViralSMSTable gift = null;
				if (task.containsKey(param_viralData))
					gift = (ViralSMSTable) task.get(param_viralData);
				else
				{
					String gifterID = task.getString(param_gifterID);
					String subscriberID = task.getString(param_subscriberID);
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					Date sentTime = dateFormat.parse(task.getString(param_giftSentTime));

					gift = rbtDBManager.getViralPromotion(gifterID, subscriberID, sentTime, "GIFTED");
				}

				if (gift != null)
				{
					task.put(param_viralData, gift);

					HashMap<String, String> giftExtraInfoMap = DBUtility.getAttributeMapFromXML(gift.extraInfo());
					if (giftExtraInfoMap != null)
					{
						String chargeClass = giftExtraInfoMap.get(iRBTConstant.CHARGE_CLASS);

						logger.info("RBT:: response: " + chargeClass);
						return chargeClass;
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return super.getChargeClass(task, subscriber, category, clip);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getChargingPackage(com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.content.Subscriber, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip)
	 */
	@Override
	protected String getChargingPackage(WebServiceContext task, Subscriber subscriber, Category category, Clip clip)
	{
		String chargingPackage = null;

		String chargingModel = task.getString(param_chargingModel);
		String optInOutModel = task.getString(param_optInOutModel);

		chargingPackage = getPromoType(chargingModel, optInOutModel);

		logger.info("RBT:: response: " + chargingPackage);
		return chargingPackage;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getUserInfoMap(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected HashMap<String, String> getUserInfoMap(WebServiceContext task)
	{
		HashMap<String, String> userInfoMap = super.getUserInfoMap(task);

		if (task.containsKey(param_viralData) && task.get(param_viralData) != null)
		{
			ViralSMSTable gift = (ViralSMSTable) task.get(param_viralData);

			HashMap<String, String> giftExtraInfoMap = DBUtility.getAttributeMapFromXML(gift.extraInfo());
			if (giftExtraInfoMap != null && giftExtraInfoMap.get(iRBTConstant.GIFT_TRANSACTION_ID) != null)
				userInfoMap.put(iRBTConstant.GIFT_TRANSACTION_ID, giftExtraInfoMap.get(iRBTConstant.GIFT_TRANSACTION_ID));
		}

		logger.info("RBT:: response: "+ userInfoMap);
		return userInfoMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getSelectionInfoMap(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected HashMap<String, String> getSelectionInfoMap(WebServiceContext task)
	{
		HashMap<String, String> selectionInfoMap = super.getSelectionInfoMap(task);

		if (task.containsKey(param_viralData) && task.get(param_viralData) != null)
		{
			ViralSMSTable gift = (ViralSMSTable) task.get(param_viralData);

			HashMap<String, String> giftExtraInfoMap = DBUtility.getAttributeMapFromXML(gift.extraInfo());
			if (giftExtraInfoMap != null && giftExtraInfoMap.get(iRBTConstant.GIFT_TRANSACTION_ID) != null)
				selectionInfoMap.put(iRBTConstant.GIFT_TRANSACTION_ID, ""+giftExtraInfoMap.get(iRBTConstant.GIFT_TRANSACTION_ID));
		}

		logger.info("RBT:: response: "+ selectionInfoMap);
		return selectionInfoMap;
	}

	private String getPromoType(String chargingModel, String optInOutModel)
	{
		String strPromoType = "";    	
		if (chargingModel != null)
			strPromoType = strPromoType + " " + chargingModel.trim().toUpperCase();
		else
			strPromoType = strPromoType + " MONTHLY";
		if (optInOutModel != null)
			strPromoType = strPromoType + " " + optInOutModel.trim().toUpperCase();
		else
			strPromoType = strPromoType + " OPTOUT";
		return strPromoType.trim();
	}

	private boolean smUpgardeSelection(WebServiceContext task, String subscriberID, String fromClass, String toClass, int rbtType) throws Exception{		
		boolean converted = false;
		boolean result = false;
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		SubscriberStatus[] subscriberStatus = rbtDBManager.getSubscriberSelections(subscriberID, fromClass, toClass, rbtType);
		if(subscriberStatus == null || subscriberStatus.length == 0){
			return false;
		}
		String oldOfferId = null;
		String offerId = null;
		String mode = null;
		int size = subscriberStatus.length;
		for(int i = 0; i < size; i++){
			SubscriberStatus subStatus = subscriberStatus[i];
			String selExtraInfo = subStatus.extraInfo();
			HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(selExtraInfo);
			oldOfferId = extraInfoMap.get(param_offerID);
			offerId = getOfferID(task, SELECTION_OFFERTYPE);
			if(oldOfferId == null || oldOfferId.trim().equals("")){
				oldOfferId = "-1";
			}
			extraInfoMap.put(param_offerID, offerId);
			extraInfoMap.put(param_old_offerid, oldOfferId);
			selExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			String selInterVal = subStatus.selInterval();
			mode = subStatus.deSelectedBy();
			result = rbtDBManager.convertWeeklySelectionsClassTypeToMonthly(subscriberID, fromClass, toClass, rbtType, selExtraInfo, selInterVal);
			if(result){
				converted = true;
			}
		}
		if(converted){
			StringBuilder builder = new StringBuilder("RBT:: SM client request for upgrade weekly to monthly ");
			builder.append("[ subID " + subscriberID);
			builder.append(", prepaidYes " + subscriber.prepaidYes());
			builder.append(", OldClassType " + fromClass);
			builder.append(", NewClassType " + toClass);
			builder.append(", Selectin OldOfferID " + oldOfferId);
			builder.append(", Selection NewOfferID " + offerId);
			builder.append(", selectedBy - " + mode);
			builder.append(", extraParams - " + null);
			logger.info(builder.toString());
			RBTSMClientResponse smClientResponse = RBTSMClientHandler.getInstance().upgradeSelection(subscriberID, subscriber.prepaidYes(), mode, fromClass, toClass, offerId, oldOfferId, null);
			logger.info("RBT:: SMClient Response for upgrade weekly to monthly" + smClientResponse.toString());
			if(!smClientResponse.getResponse().equalsIgnoreCase(SUCCESS)){
				for(int i = 0; i < size; i++){
					SubscriberStatus subStatus = subscriberStatus[i];
					String selExtraInfo = subStatus.extraInfo();
					HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(selExtraInfo);
					oldOfferId = extraInfoMap.get(param_old_offerid);
					offerId = getOfferID(task, SELECTION_OFFERTYPE);
					if(oldOfferId == null || oldOfferId.trim().equals("")){
						oldOfferId = "-1";
					}
					extraInfoMap.put(param_offerID, oldOfferId);
					extraInfoMap.put(param_old_offerid, "-1");
					selExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					String selInterVal = subStatus.selInterval();
					mode = subStatus.deSelectedBy();
					rbtDBManager.updateSubscriberSelection(subscriberID, subStatus.classType(), subStatus.oldClassType(), subStatus.selStatus(), rbtType, selExtraInfo, selInterVal);
				}
			}
		}
		return converted;
	}
}
