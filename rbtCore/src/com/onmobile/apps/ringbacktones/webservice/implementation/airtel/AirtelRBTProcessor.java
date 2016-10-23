/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.airtel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.features.airtel.UserSelectionRestrictionBasedOnSubClass;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class AirtelRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(AirtelRBTProcessor.class);

	private static final String VP = "IVR";

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processActivation(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processActivation(WebServiceContext task)
	{
		String subscriberID = task.getString(param_subscriberID);
		//Getting offer is added by Sreekar
		String acmpPreProcess = acmpSubscriptionPreProcess(task);
		if (acmpPreProcess != null)
			return acmpPreProcess;
		//End of ACWM feature changes

		String response = super.processActivation(task);
		if (response.equalsIgnoreCase(SUCCESS))
		{
			if (task.containsKey(param_mmContext))
			{
				String[] mmContext = task.getString(param_mmContext).split("\\|");
				if (mmContext[0].equalsIgnoreCase("RBT_EASY_CHARGE"))
				{
					ViralSMSTable[] retailRequests = rbtDBManager.getViralSMSesByType(subscriberID, "EC");
					for (ViralSMSTable retailRequest : retailRequests)
					{
						if (retailRequest.clipID() == null)
						{
							rbtDBManager.updateViralPromotion(retailRequest.subID(), retailRequest.callerID(),
									retailRequest.sentTime(), "EC", "EC_PROCESSED", new Date(), null, null);
							break;
						}
					}
				}
			}

			if (task.containsKey(param_scratchCardNo))
			{
				String context = "scratchcard";
				if (task.containsKey(param_context))
					context = task.getString(param_context);

				String scratchCardNo = task.getString(param_scratchCardNo);
				TransData transData = rbtDBManager.getTransData(scratchCardNo, context);
				if (transData != null && transData.transDate() == null)
					rbtDBManager.removeTransData(scratchCardNo, context);
			}
		}

		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processSelection(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processSelection(WebServiceContext task)
	{
		String subscriberID = task.getString(param_subscriberID);
		String browsingLanguage = task.getString(param_browsingLanguage);
		String toneId = task.getString(param_clipID);
		
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		//RBT-18975 //sequence?
		UserSelectionRestrictionBasedOnSubClass userRestriction = null;
		try {
			userRestriction = (UserSelectionRestrictionBasedOnSubClass) ConfigUtil
					.getBean(BeanConstant.AIRTEL_USER_SELECTION_RESTRICT_BASED_ON_SUBCLASS);
		} catch (Exception e) {
			logger.error("Exception Occurred while initialising "
					+ BeanConstant.AIRTEL_USER_SELECTION_RESTRICT_BASED_ON_SUBCLASS);
		}
		String mode = task.getString(param_mode);
		if (userRestriction != null) {
			Clip tone = null;
			userRestriction.setSubscriber(subscriber);
			if (!task.containsKey(param_rentalPack)
					&& userRestriction.restrictUserSelection()) {
				// send sms
				if (userRestriction.isSmsToBeSentForMode(mode)) {
					String senderID = RBTParametersUtils.getParamAsString(
							iRBTConstant.WEBSERVICE, "ACK_SMS_SENDER_NO", null);
					task.put(param_senderID, senderID);
					task.put(param_receiverID, subscriber.subID());
					String smsText = null;
					try {
						smsText = CacheManagerUtil.getSmsTextCacheManager()
								.getSmsText(
										"SELECTION_NOT_ALLOWED_"
												+ subscriber
														.subscriptionClass()
														.toUpperCase(), null,
										null);
						if (smsText != null) {
							if (smsText.indexOf("%SONG_NAME%") != -1) {
								tone = rbtCacheManager.getClip(toneId);
								smsText = smsText.replace("%SONG_NAME%",
										tone != null ? tone.getClipName() : "");
							}
						}

						if (smsText == null) {
							smsText = CacheManagerUtil.getSmsTextCacheManager()
									.getSmsText("SELECTION_NOT_ALLOWED", null,
											null);
							if (smsText != null) {
								smsText = smsText.replace(
										"%SUBSCRIPTION_CLASS%",
										subscriber.subscriptionClass());
								if (smsText.indexOf("%SONG_NAME%") != -1) {
									tone = rbtCacheManager.getClip(toneId);
									smsText = smsText.replace("%SONG_NAME%",
											tone != null ? tone.getClipName()
													: "");
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						smsText = CacheManagerUtil
								.getSmsTextCacheManager()
								.getSmsText("SELECTION_NOT_ALLOWED", null, null);
					}
					task.put(param_smsText, smsText);
					logger.debug("SMS Text to be sent " + smsText);
					sendSMS(task);
				}
				return Constants.SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE;
			}
		}
		
		if (task.containsKey(param_mmContext))
		{
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			if (mmContext[0].equalsIgnoreCase("RBT_EASY_CHARGE"))
			{
				ViralSMSTable[] retailRequests = rbtDBManager.getViralSMSesByType(subscriberID, "EC");
				retailRequests = reOrderRetailerRequests(retailRequests);

				Parameters ecCategoryParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "EC_CATEGORY", null);
				int categoryID = Integer.parseInt(ecCategoryParam.getValue().trim());

				Calendar endCal = Calendar.getInstance();
				endCal.set(2037, 0, 1);
				Date endDate = endCal.getTime();

				subscriber = rbtDBManager.getSubscriber(subscriberID);
				String subYes = subscriber.subYes();
				boolean isPrepaid = subscriber.prepaidYes();

				boolean changeSubType = true;
				boolean useSubManager = true;
				String messagePath = null;
				Parameters messagePathParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "MESSAGE_PATH", null);
				if (messagePathParam != null)
					messagePath = messagePathParam.getValue().trim();

				boolean success = false;
				for (ViralSMSTable retailRequest : retailRequests)
				{
					String strClip = retailRequest.clipID();
					if (strClip == null)
						continue;

					int clipID = Integer.parseInt(strClip);
					Clip clips = rbtCacheManager.getClip(clipID, browsingLanguage);
					if(clips != null)
						task.put(param_chargeClass, clips.getClassType());
					if(clips != null && clips.getContentType()!=null && clips.getContentType().equalsIgnoreCase("UGCCLIP"))
					{
						task.put(param_selectionInfo+"_"+ "CONTENT_TYPE", "UGCCLIP");
						if(clips.getClipGrammar() == null){
							clips.setClipGrammar("TO_BE_DOWNLOADED");
							try{
								ClipsDAO.updateClip(clips);
							}
							catch(Throwable t){
								logger.error("", t);
							}
						}
					}
					String clipWavFile = clips.getClipRbtWavFile();
					//if the clip is a ugc clip(CONTENT_TYPE=UGCCLIP)
					String classType = null;
					HashMap<String,String> selectionInfoMap = new HashMap<String,String>();
					if(clips.getClipInfo()!=null && clips.getContentType().equals("UGCCLIP"))
					{
						classType = clips.getClassType();
						selectionInfoMap.put("CONTENT_TYPE", "UGCCLIP");
					}else
					{
						selectionInfoMap  = null;
					}
					logger.info("selectionInfoMap->"+selectionInfoMap);
					if(getParamAsBoolean(iRBTConstant.COMMON,iRBTConstant.SUPPORT_SMCLIENT_API,"FALSE")){
						HashMap<String, String> responseParams = new HashMap<String, String>();
						Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
						classType = "DEFAULT";
						if( category!= null)
							 classType = category.getClassType();
						String baseOfferID = "-2";
						String selOfferID = "-2";
						selectionInfoMap.put(param_offerID, selOfferID);
						String selectionInfo = VP + ":" + retailRequest.subID();
						logger.info("selectionInfoMap->"+selectionInfoMap);	
						boolean isSuccess = rbtDBManager.smAddSubscriberSelections(subscriberID, null, categoryID, clipWavFile, null, null, endDate, 1,
								"EC", selectionInfo, 0, isPrepaid, changeSubType, messagePath, 0, 2359, classType,
								useSubManager, true, "VUI", null, subYes, null, true, false, false, null, subscriber, null, selectionInfoMap, responseParams);
						logger.info("selectionInfoMap->"+selectionInfoMap);
						if(isSuccess){							
							try{
								String selectionRefID = "";
								if (responseParams.containsKey("REF_ID"))
								{
									selectionRefID = responseParams.get(REF_ID);
								}
								HashMap<String, String> extraParams = getSelectionExtraParams(subscriber, clips, category, "all", selectionInfo, selectionInfoMap);
								isSuccess = smClientRquestForSelection(task, subscriberID, subscriber, classType, baseOfferID, selOfferID, "EC", selectionRefID, isPrepaid, -1, extraParams);
							}
							catch (Exception e){
								logger.error("", e);
								isSuccess = false;
							}							
						}						
					}
					else{
						logger.info("selectionInfoMap->"+selectionInfoMap);
						rbtDBManager.addSubscriberSelections(subscriberID, null, categoryID, clipWavFile, null, null, endDate, 1,
								"EC", VP + ":" + retailRequest.subID(), 0, isPrepaid, changeSubType, messagePath, 0, 2359, classType,
								useSubManager, true, "VUI", null, subYes, null, true, false, false, null, subscriber, null,selectionInfoMap);
					}

					rbtDBManager.updateViralPromotion(retailRequest.subID(), retailRequest.callerID(),
							retailRequest.sentTime(), "EC", "EC_PROCESSED", new Date(), null, null);
					success = true;
				}

				if (success)
					return SUCCESS;

				return FAILED;
			}
			else if (mmContext[0].equalsIgnoreCase("RBT_TNB"))
			{
				rbtDBManager.addToTNBBlackList(subscriberID);
				rbtDBManager.removeSubscriberPromo(subscriberID, null, "TNB");
			}
		}


		//Getting offer is added by Sreekar
		/**
		 * We need to call the SM URL only if the subscriber is already activated on this particular
		 * offer (ACWM)
		 */
		
		
		String offerID = parametersCacheManager.getParameter(iRBTConstant.COMMON, iRBTConstant.ACWM_OFFER_ID, "-100").getValue();

		// if subscriber is not active we should get the subscription offer here
		if (subscriber == null || subscriber.subYes().equalsIgnoreCase("X"))
		{
			String acmpSubscriptionPreProcess = acmpSubscriptionPreProcess(task);
			if (acmpSubscriptionPreProcess != null)
				return acmpSubscriptionPreProcess;
		}

		HashMap<String, String> xtraInfoMap = rbtDBManager.getExtraInfoMap(subscriber);
		String offerIDInTask = null;
		if(task.containsKey(param_userInfo + "_" + iRBTConstant.EXTRA_INFO_OFFER_ID))
			offerIDInTask = task.getString(param_userInfo + "_" + iRBTConstant.EXTRA_INFO_OFFER_ID);

		if (((subscriber == null || subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED)) && offerIDInTask != null && offerID.equals(offerIDInTask))
				||(subscriber != null && !subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED) && xtraInfoMap != null && offerID.equals(xtraInfoMap.get(iRBTConstant.EXTRA_INFO_OFFER_ID))) 
				|| task.containsKey(param_selectionInfo + "_"+iRBTConstant.EXTRA_INFO_IMEI_NO))
		{
			int categoryID = Integer.parseInt(task.getString(param_categoryID));
			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
			
			if (category != null && (category.getCategoryTpe() != 5 && category.getCategoryTpe() != 7))
			{
				logger.info("RBT::ACWM user, not adding selection, categotyType: " + category.getCategoryTpe());
				return ERROR; // if it is not a normal song selection we will not accept the request
			}

			try
			{
				HashMap<String,String> infoMap = new HashMap<String, String>();
				if (task.containsKey(param_selectionInfo + "_"+iRBTConstant.EXTRA_INFO_IMEI_NO))
					infoMap.put(iRBTConstant.EXTRA_INFO_IMEI_NO, task.getString(param_selectionInfo + "_"+iRBTConstant.EXTRA_INFO_IMEI_NO));

				Offer selOffer = RBTSMClientHandler.getInstance().getSelectionOffer(subscriberID,
						task.getString(param_mode), Utility.getSubscriberType(task), infoMap)[0];
				logger.info("RBT::got sel offer->" + selOffer);
				task.put(param_chargeClass, selOffer.getSrvKey());
				task.put(param_selectionInfo + "_" + iRBTConstant.EXTRA_INFO_OFFER_ID, selOffer.getOfferID());
			}
			catch (Exception e)
			{
				logger.error("", e);
				return ERROR;
			}
		}

		//if user is subscribed with the ACWM offer he should be charged for the selection
		if (subscriber != null && subscriber.maxSelections() >= 1 && xtraInfoMap != null
				&& offerID.equals(xtraInfoMap.get(iRBTConstant.EXTRA_INFO_OFFER_ID)))
			task.put(param_chargeClass, "DEFAULT");
		// end of Offer ACWM changes
		if(toneId!=null)
		{
			Clip tone = rbtCacheManager.getClip(toneId);
			if(tone != null && tone.getContentType()!=null && tone.getContentType().equalsIgnoreCase("UGCCLIP"))
			{
				task.put(param_selectionInfo+"_"+ "CONTENT_TYPE", "UGCCLIP");
				if(tone.getClipGrammar() == null){
					tone.setClipGrammar("TO_BE_DOWNLOADED");
					try{
						ClipsDAO.updateClip(tone);
					}
					catch(Throwable t){
						logger.error("", t);
					}
				}
			}
		}
		
		String response = super.processSelection(task);
		if (response.equalsIgnoreCase(SUCCESS))
		{
			if (task.containsKey(param_scratchCardNo))
			{
				String context = "scratchcard";
				if (task.containsKey(param_context))
					context = task.getString(param_context);

				String scratchCardNo = task.getString(param_scratchCardNo);
				TransData transData = rbtDBManager.getTransData(scratchCardNo, context);
				if (transData != null)
				{
					if (transData.transDate() == null)
						rbtDBManager.removeTransData(scratchCardNo, context);
					else
					{
						String transCount = transData.accessCount();
						int accessCount = 0;
						int downloadCount = 0;
						if (transCount != null)
						{
							String[] transCounts = transCount.split(":");
							if (transCounts.length > 0)
								accessCount = Integer.parseInt(transCounts[0]);
							if (transCounts.length > 1)
								downloadCount = Integer.parseInt(transCounts[1]);
						}

						downloadCount++;
						rbtDBManager.updateTransData(scratchCardNo, context, subscriberID, null, accessCount + ":" + downloadCount);
					}
				}
			}
		}

		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processData(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processData(WebServiceContext task)
	{
		String response = ERROR;

		try
		{
			String type = task.getString(param_type);
			if (type == null || !type.equalsIgnoreCase("EC"))
				response = super.processData(task);
			else
			{
				String subscriberID = task.getString(param_subscriberID);
				String callerID = task.getString(param_callerID);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				Date sentTime = dateFormat.parse(task.getString(param_sentTime));
				String clipID = task.getString(param_clipID);

				ViralSMSTable viralData = rbtDBManager.getViralPromotion(subscriberID, callerID, sentTime, type, clipID);
				if (viralData == null)
				{
					logger.info("RBT:: response: " + NO_DATA);
					return NO_DATA;
				}

				HashMap<String, String> requestParams = new HashMap<String, String>();
				requestParams.put(param_subscriberID, callerID);
				requestParams.put(param_mode, "EC");

				String modeInfo = getMode(task) + ":" + subscriberID;
				requestParams.put(param_modeInfo, modeInfo);

				if (clipID == null)
				{
					requestParams.put(param_action, action_activate);

					Parameters ecSubClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "EC_SUB_CLASS", "EC");
					String subscriptionClass = ecSubClassParam.getValue().trim();
					requestParams.put(param_subscriptionClass, subscriptionClass);

					WebServiceContext tempTask = Utility.getTask(requestParams);
					response = processActivation(tempTask);
				}
				else
				{
					Subscriber subscriber = rbtDBManager.getSubscriber(callerID);
					if (rbtDBManager.isSubscriberDeactivated(subscriber))
					{
						logger.info("RBT:: response: " + USER_NOT_ACTIVE);
						return USER_NOT_ACTIVE;
					}

					requestParams.put(param_action, action_set);
					requestParams.put(param_callerID, ALL);
					requestParams.put(param_clipID, clipID);

					Parameters ecCategoryParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "EC_CATEGORY", null);
					String categoryID = ecCategoryParam.getValue().trim();
					requestParams.put(param_categoryID, categoryID);

					WebServiceContext tempTask = Utility.getTask(requestParams);
					response = processSelection(tempTask);
				}

				if (response.equalsIgnoreCase(SUCCESS))
					rbtDBManager.updateViralPromotion(subscriberID, callerID, sentTime, "EC", "EC_PROCESSED", new Date(), null, null, clipID);
			}
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
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getClip(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Clip getClip(WebServiceContext task)
	{
		String browsingLanguage = task.getString(param_browsingLanguage);
		String vCode = task.getString(param_clipVcode);
		if (vCode != null)
		{
			Clip clip = rbtCacheManager.getClipByRbtWavFileName("rbt_" + vCode + "_rbt", browsingLanguage);
			return clip;
		}
		
		Clip clip = super.getClip(task);
		if (clip == null && task.containsKey(param_clipID))
		{
			String clipIDStr = task.getString(param_clipID);
			clip = rbtCacheManager.getClipByRbtWavFileName("rbt_" + clipIDStr + "_rbt", browsingLanguage);
		}

		return clip;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getMode(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected String getMode(WebServiceContext task)
	{
		if (task.containsKey(param_mode))
		{
			logger.info("RBT:: response: " + task.getString(param_mode));
			return task.getString(param_mode);
		}

		if (task.containsKey(param_mmContext))
		{
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			if (mmContext[0].equalsIgnoreCase("RBT_EASY_CHARGE"))
			{
				logger.info("RBT:: response: EC");
				return "EC";
			}
		}

		String calledNo = "";
		if (task.containsKey(param_calledNo))
			calledNo = task.getString(param_calledNo);

		String mode = VP;

		Parameters callbackBaseNumbersParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "CALLBACK_BASENUMBERS", null);
		if (callbackBaseNumbersParam != null)
		{
			String[] ivrNumbers = callbackBaseNumbersParam.getValue().trim().split(",");
			for (String ivrNumber : ivrNumbers)
			{
				if (calledNo.startsWith(ivrNumber))
				{
					mode = VP + "-SC";
					break;
				}
			}
		}

		logger.info("RBT:: response: " + mode);
		return mode;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getModeInfo(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected String getModeInfo(WebServiceContext task)
	{
		String modeInfo = VP;
		if (task.containsKey(param_modeInfo))
			modeInfo = task.getString(param_modeInfo);

		if (task.containsKey(param_mmContext))
		{
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			if (mmContext[0].equalsIgnoreCase("RBT_EASY_CHARGE") && mmContext[1].equalsIgnoreCase("ACTIVATION") )
			{
				String subscriberID = task.getString(param_subscriberID);
				ViralSMSTable[] retailRequests = rbtDBManager.getViralSMSesByType(subscriberID, "EC");
				for (ViralSMSTable retailRequest : retailRequests)
				{
					if (retailRequest.clipID() == null)
						modeInfo += ":" + retailRequest.subID();
				}
			}
		}

		if (task.getString(param_action).equalsIgnoreCase(action_acceptGift))
			modeInfo = "GIFT:" + task.getString(param_gifterID);
		else if (task.containsKey(param_scratchCardNo))
			modeInfo = "scratchcard:" + task.getString(param_scratchCardNo);
		else if (task.containsKey(param_calledNo))
			modeInfo = task.getString(param_calledNo);
		else if (task.containsKey(param_ipAddress))
			modeInfo += ":" + task.getString(param_ipAddress);

		if (task.containsKey(param_dontSMSInBlackOut)
				&& task.getString(param_dontSMSInBlackOut).equalsIgnoreCase(YES))
			modeInfo = "BULK:" + modeInfo;

		logger.info("RBT:: response: " + modeInfo);
		return modeInfo;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getCos(com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber)
	{
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getSubscriptionClass(com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails)
	 */
	@Override
	protected String getSubscriptionClass(WebServiceContext task, CosDetails cos)
	{
		String subscriptionClass = null;

		String action = task.getString(param_action);

		if (task.containsKey(param_subscriptionClass))
			subscriptionClass = task.getString(param_subscriptionClass);
		else if (task.containsKey(param_rentalPack))
			subscriptionClass = task.getString(param_rentalPack);
		else if (action.equalsIgnoreCase(action_acceptGift))
		{
			Parameters giftSubClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "GIFT_SUBSCRIPTION_CLASS", "GIFT");
			subscriptionClass = giftSubClassParam.getValue().trim();
		}
		else if (task.containsKey(param_mmContext))
		{
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			if (mmContext[0].equalsIgnoreCase("RBT_PROMOTION"))
			{
				subscriptionClass = mmContext[1];
			}
			else if (mmContext[0].equalsIgnoreCase("RBT_TNB"))
			{
				Parameters tnbSubClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "TNB_SUBSCRIPTION_CLASS", "TNB");
				subscriptionClass = tnbSubClassParam.getValue().trim();
			}
			else if (mmContext[0].equalsIgnoreCase("RBT_SCRATCHCARD"))
			{
				Parameters scratchCardSubClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "SCRATCHCARD_SUBSCRIPTION_CLASS", "SCRATCHCARD");
				subscriptionClass = scratchCardSubClassParam.getValue().trim();
			}
			else if (mmContext[0].equalsIgnoreCase("RBT_ALBUM"))
			{
				Parameters albumSubClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "ALBUM_SUBSCRIPTION_CLASS", "ALBUM");
				subscriptionClass = albumSubClassParam.getValue().trim();
			}
			else if (mmContext[0].startsWith("RBT_EASY_CHARGE"))
			{
				Parameters ecSubClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "EC_SUB_CLASS", "EC");
				subscriptionClass = ecSubClassParam.getValue().trim();
			}
		}

		if (cos != null && subscriptionClass == null)
			subscriptionClass = cos.getSubscriptionClass();
		if (subscriptionClass == null)
			subscriptionClass = "DEFAULT";

		logger.info("RBT:: response: " + subscriptionClass);
		return subscriptionClass;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getChargeClass(com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.content.Subscriber, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip)
	 */
	@Override
	protected String getChargeClass(WebServiceContext task, Subscriber subscriber, Category category, Clip clip)
	{
		String chargeClass = null;

		String action = task.getString(param_action);

		if (task.containsKey(param_chargeClass))
			chargeClass = task.getString(param_chargeClass);
		else if (action.equalsIgnoreCase(action_acceptGift))
		{
			Parameters giftChargeClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "GIFT_CHARGE_CLASS", "FREE");
			chargeClass = giftChargeClassParam.getValue().trim();
		}
		else if (task.containsKey(param_mmContext))
		{
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			if (mmContext[0].equalsIgnoreCase("RBT_PROMOTION"))
			{
				chargeClass = mmContext[2];
			}
			else if (mmContext[0].equalsIgnoreCase("RBT_TNB"))
			{
				Parameters tnbChargeClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "TNB_CHARGE_CLASS", "FREE");
				chargeClass = tnbChargeClassParam.getValue().trim();
			}
			else if (mmContext[0].equalsIgnoreCase("RBT_SCRATCHCARD"))
			{
				if (rbtDBManager.isSubscriberDeactivated(subscriber))
				{
					Parameters scratcCardChargeClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "SCRATCHCARD_CHARGE_CLASS_NONHTUSER", "FREE");
					chargeClass = scratcCardChargeClassParam.getValue().trim();
				}
				else
				{
					Parameters scratcCardChargeClassParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "SCRATCHCARD_CHARGE_CLASS_HTUSER", "FREE");
					chargeClass = scratcCardChargeClassParam.getValue().trim();
				}
			}
			else if (subscriber != null)
			{
				SubscriptionClass subscriptionClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subscriber.subscriptionClass());
				if (subscriptionClass != null && subscriptionClass.getFreeSelections() > 0
						&& subscriber.maxSelections() < subscriptionClass.getFreeSelections())
					chargeClass = "FREE";
			}
		}

		logger.info("RBT:: response: " + chargeClass);
		return chargeClass;
	}

	/**
	 * Airtel Comes With Music changes
	 * @param task
	 * @return
	 */
	private String acmpSubscriptionPreProcess(WebServiceContext task)
	{
		try
		{
			String subscriberID = task.getString(param_subscriberID);
			String mode = task.getString(param_mode);

			HashMap<String,String> infoMap = new HashMap<String, String>();
			if (task.containsKey(param_userInfo + "_"+iRBTConstant.EXTRA_INFO_IMEI_NO))
				infoMap.put(iRBTConstant.EXTRA_INFO_IMEI_NO, task.getString(param_userInfo + "_"+iRBTConstant.EXTRA_INFO_IMEI_NO));
			else if(task.containsKey(param_selectionInfo + "_"+iRBTConstant.EXTRA_INFO_IMEI_NO))
				infoMap.put(iRBTConstant.EXTRA_INFO_IMEI_NO, task.getString(param_selectionInfo + "_"+iRBTConstant.EXTRA_INFO_IMEI_NO));

			String offerID = parametersCacheManager.getParameter(iRBTConstant.COMMON, iRBTConstant.ACWM_OFFER_ID, "-100").getValue();
			Offer[] allOffers = RBTSMClientHandler.getInstance().getSubscriptionOffer(subscriberID, mode, Utility.getSubscriberType(task), infoMap);
			if(allOffers == null)
				return null;
			Offer offer = allOffers[0];
			logger.info("RBT:: got sub offer: " + offer);

			//If ACWM offer id configured is same as the one returned by SM 
			if (offerID.equals(offer.getOfferID()))
			{
				// Checking if user requested for advance rental pack
				// We should not allow upgradations/activations for advance rental packs
				if (task.containsKey(param_subscriptionClass))
				{
					String subClass = task.getString(param_subscriptionClass);
					if (rbtDBManager.isAdvanceRentalSubClass(subClass))
						return ERROR;
				}
				task.put(param_subscriptionClass, offer.getSrvKey());
				task.put(param_userInfo + "_" + iRBTConstant.EXTRA_INFO_OFFER_ID, offer.getOfferID());
			}
			else if (task.containsKey(param_userInfo + "_" + iRBTConstant.EXTRA_INFO_IMEI_NO)
						|| task.containsKey(param_selectionInfo + "_"+ iRBTConstant.EXTRA_INFO_IMEI_NO))
				{
					task.put(param_subscriptionClass, offer.getSrvKey());
					task.put(param_userInfo + "_" + iRBTConstant.EXTRA_INFO_OFFER_ID, offer.getOfferID());
				}
		}
		catch (Exception e)
		{
			logger.error("", e);
			return ERROR;
		}

		return null;
	}

	private ViralSMSTable[] reOrderRetailerRequests(ViralSMSTable[] retailRequests)
	{
		ViralSMSTable viralSMSTable = null; 
		for (int i = 0; i < retailRequests.length; i++)
		{
			for (int j = i; j < retailRequests.length; j++)
			{
				if (retailRequests[i].sentTime().compareTo(retailRequests[j].sentTime()) > 0)
				{
					viralSMSTable = retailRequests[i];
					retailRequests[i] = retailRequests[j];
					retailRequests[j] = viralSMSTable;
				}
			}
		}

		return retailRequests;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#deleteSetting(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public String deleteSetting(WebServiceContext task)
	{
		String vCode = task.getString(param_clipVcode);
		if (vCode != null)
		{
			task.put(param_rbtFile, "rbt_" + vCode + "_rbt");
		}

		return super.deleteSetting(task);
	}
	
	@Override
	public String processGiftRequest(WebServiceContext task) {
		
		if(task.containsKey(param_isConsentFlow) && task.getString(param_isConsentFlow).equalsIgnoreCase(YES)) {
			
			String gifterID = task.getString(param_gifterID);
			String consentUniqueId = com.onmobile.apps.ringbacktones.services.common.Utility.generateConsentIdRandomNumber(gifterID);
			if (consentUniqueId == null) {
				consentUniqueId = UUID.randomUUID().toString();
			}
			
			String gifteeID = task.getString(param_gifteeID);
			String toneID = task.getString(param_toneID);
			String subscriptionClass = task.getString(param_subscriptionClass);
			String chargeClass = task.getString(param_chargeClass);
			String mode = task.getString(param_mode);
			
			if (toneID != null) {
				task.put(param_clipID, toneID);
				Clip clip = getClip(task);
				if (clip == null || clip.getClipEndTime().before(new Date()) || clip.getClipStartTime().after(new Date())) {
					logger.info("clip: " + clip + " response: " + CLIP_NOT_EXISTS);
					return CLIP_NOT_EXISTS;
				}

				toneID = String.valueOf(clip.getClipId());
			}
			
			if(subscriptionClass == null) {
				if(m_modeSubClassMap != null && mode != null && m_modeSubClassMap.containsKey(mode)) {
					subscriptionClass = (String)m_modeSubClassMap.get(mode);
				}
			}
			
			if(chargeClass == null) {
				chargeClass = getParamAsString("DAEMON", "NORMAL_GIFT_CHRG_CLASS", "DEFAULT");
			}
			
			
			Map<String, String> infoMap = getInfoMap(task);
			infoMap.put(param_isConsentFlow, "true");
			String extraInfo = DBUtility.getAttributeXMLFromMap(infoMap);
			
			
			
			boolean isSuccess = rbtDBManager.makeEntryInConsent(consentUniqueId, gifterID, gifteeID, null, subscriptionClass, mode, null, null, 1, chargeClass, null, 
					null, toneID, null, 0, 2359, null, 0, false, null, false, 7, null, true, null,
					null, 1, null, null, new Date(), extraInfo, "GIFT",	1);
			
			String response = null;
			if(isSuccess) {				
				task.put("CONSENTID", consentUniqueId);
				task.put("CONSENTCLASSTYPE", chargeClass);
				task.put("CONSENTSUBCLASS", subscriptionClass);				
				response  = SUCCESS;
			}
			else {
				response = ERROR;
			}
			logger.info("response: " + response);
			return response;
		}
		
		return super.processGiftRequest(task);
	}
}
