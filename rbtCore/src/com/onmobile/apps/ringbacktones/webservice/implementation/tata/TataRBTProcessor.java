/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.tata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class TataRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(TataRBTProcessor.class);

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

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			webServiceContext.put(param_subscriber, subscriber);

			response = isValidUser(webServiceContext, subscriber);
			if (!response.equals(VALID))
			{
				logger.info("response: " + response);
				return response;
			}
			
			if (webServiceContext.containsKey(param_rentalPack))
			{
				String cosID = webServiceContext.getString(param_cosID);
				if (cosID != null && subscriber != null && subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED))
				{
					CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
					response = TataUtility.upgradeSubscription(webServiceContext, subscriber, cos);
					if (response.equals(SUCCESS))
					{
						String activatedBy = null;
						String deactivatedBy = null;
						Date startDate = null;
						Date endDate = null;
						String prepaidYes = null;
						Date lastAccessDate = null;
						Date nextChargingDate = null;
						Integer noOfAccess = null;
						String activationInfo = null;
						String subscriptionClass = webServiceContext.getString(param_rentalPack);
						String subscriptionYes = null;
						String lastDeactivationInfo = null;
						Date lastDeactivationDate = null;
						Date activationDate = null;
						Integer maxSelections = null;
						String activatedCosID = null;
						String oldClassType = subscriber.subscriptionClass();
						Integer rbtType = null;
						String language = null;
						String playerStatus = null;
						HashMap<String, String> extraInfo = null;

						rbtDBManager.updateSubscriber(subscriberID,
								activatedBy, deactivatedBy, startDate, endDate,
								prepaidYes, lastAccessDate, nextChargingDate,
								noOfAccess, activationInfo, subscriptionClass,
								subscriptionYes, lastDeactivationInfo,
								lastDeactivationDate, activationDate,
								maxSelections,
								cosID, activatedCosID, oldClassType, rbtType,
								language, playerStatus, extraInfo);
						
						subscriber = rbtDBManager.getSubscriber(subscriberID);

						// Updated Subscriber object is storing in WebServiceContext & it will be used to build the response element
						webServiceContext.put(param_subscriber, subscriber);
					}

					return response;
				}
			}

			if (!rbtDBManager.isSubscriberDeactivated(subscriber))
			{
				String status = Utility.getSubscriberStatus(subscriber);
				logger.info("response: " + status);
				return status;
			}

			String activatedBy = getMode(webServiceContext);
			String activationInfo = getModeInfo(webServiceContext);
			CosDetails cos = TataUtility.getSubscriberCOS(webServiceContext, subscriber);
			String subscriptionClass = getSubscriptionClass(webServiceContext, cos);

			int activationPeriod = 0;

			String circleID = DataUtils.getUserCircle(webServiceContext);
			boolean isPrepaid = DataUtils.isUserPrepaid(webServiceContext);

			int offerType;
			if(webServiceContext.containsKey(param_requestFromSelection))
			{
				offerType = COMBO_SUB_OFFERTYPE;
			}
			else{
				offerType = BASE_OFFERTYPE;
			}
			if (isSupportSMClientModel(webServiceContext, offerType))
			{
				HashMap<String, String> userInfoMap = getUserInfoMap(webServiceContext);
				subscriber = smActivateSubscriber(webServiceContext, userInfoMap, subscriber, subscriberID, activatedBy, null, null, isPrepaid, 
						activationPeriod, 0, activationInfo, subscriptionClass, true, cos, 
						false, 0, circleID);
			}
			else {
				subscriber = rbtDBManager.activateSubscriber(subscriberID, activatedBy, null, isPrepaid, activationPeriod, 0,
						activationInfo, subscriptionClass, true, cos, 0, circleID);
			}
			// Activated Subscriber object is storing in WebServiceContext & it will be used to build the response element
			webServiceContext.put(param_subscriber, subscriber);

			if (subscriber != null)
			{
				response = SUCCESS;
				webServiceContext.put(param_activatedNow, YES);

				String language = webServiceContext.getString(param_language);
				if (language != null)
					rbtDBManager.setSubscriberLanguage(subscriberID, language);
			}
			else
				response = FAILED;
		}
		catch (Exception e)
		{
			logger.error("", e);
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
		String response = ERROR;
		String subscriberID = task.getString(param_subscriberID);
		String browsingLanguage = task.getString(param_browsingLanguage);

		String action = task.getString(param_action);

		int categoryID = Integer.parseInt(task.getString(param_categoryID));
		Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
		if (category == null)
		{
			logger.info("response: " + CATEGORY_NOT_EXISTS);
			return CATEGORY_NOT_EXISTS;
		}

		if (action.equalsIgnoreCase(action_overwrite))
		{
			SubscriberDownloads oldestDownload = rbtDBManager.getOldestActiveSubscriberDownload(
					subscriberID, category.getCategoryTpe());

			rbtDBManager.deactivateSubWavFile(subscriberID, oldestDownload.promoId(),
					iRBTConstant.STATE_DEACTIVATED, "VP", null);
			rbtDBManager.updateDownloadStatus(oldestDownload.subscriberId(), oldestDownload .promoId(), 'd');
		}

		if (action.equalsIgnoreCase(action_default))
		{
			SubscriberStatus[] allSubSelectionsFromDB = rbtDBManager.getAllSubscriberSelectionRecords(subscriberID, null);

			int clipID = Integer.parseInt(task.getString(param_clipID));

			String promoID = null;
			List<SubscriberStatus> subDefaultSelTobeDeleted = new ArrayList<SubscriberStatus>();
			int categoryType = category.getCategoryTpe();
			if (categoryType == iRBTConstant.BOUQUET)
			{
				Category shuffleCategory = rbtCacheManager.getCategory(clipID, browsingLanguage);
				if (shuffleCategory != null)
					promoID = shuffleCategory.getCategoryPromoId();
			}
			else
			{
				Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);
				if (clip != null)
					promoID = clip.getClipPromoId();
			}

			for (SubscriberStatus subSelectionFromDB : allSubSelectionsFromDB)
			{
				String callerID = subSelectionFromDB.callerID();
				String toneCode = subSelectionFromDB.subscriberFile();

				if (callerID == null && promoID != null && !promoID.equals(toneCode) && subSelectionFromDB.selStatus().equals("B"))
				{
					SubscriberDownloads favoriteSelection = rbtDBManager.getSubscriberDownload(
							subSelectionFromDB.subID(), toneCode, subSelectionFromDB.categoryID(), subSelectionFromDB.categoryType());
					if (favoriteSelection != null && favoriteSelection.downloadStatus() == 'd')
						continue;

					subDefaultSelTobeDeleted.add(subSelectionFromDB);
				}
			}

			for (int i = 0; i < subDefaultSelTobeDeleted.size() - 1; i++)
			{
				SubscriberStatus subscriberStatus = subDefaultSelTobeDeleted.get(i);
				if (subscriberStatus != null)
					rbtDBManager.deactivateSubWavFileForCaller(subscriberStatus.subID(),
							subscriberStatus.callerID(), subscriberStatus.subscriberFile(), "D",
							"VP", "n");
			}
			if (subDefaultSelTobeDeleted.size() > 0)
			{
				SubscriberStatus subscriberStatus = subDefaultSelTobeDeleted.get(subDefaultSelTobeDeleted.size() - 1);
				if (subscriberStatus != null)
					rbtDBManager.deactivateSubWavFileForCaller(subscriberStatus.subID(),
							subscriberStatus.callerID(), subscriberStatus.subscriberFile(), "D",
							"VP:" + promoID, "y");
			}
			SubscriberStatus[] allSubSelectionsFromDBForCS = rbtDBManager.getAllSubscriberSelectionRecords(subscriberID, null);
			if (allSubSelectionsFromDBForCS != null)
				response = SUCCESS;
			else
				response = FAILED;
		}
		else
		{
			try
			{
				task.put(WebServiceConstants.param_requestFromSelection,"true");
				response = processActivation(task);
				if (!response.equalsIgnoreCase(SUCCESS) && !Utility.isUserActive(response))
					return response;
				else if (Utility.isUserActive(response)
						&& task.containsKey(param_ignoreActiveUser)
						&& task.getString(param_ignoreActiveUser).equalsIgnoreCase(YES))
					return response;

				if (task.containsKey(param_removeExistingSetting)
						&& task.getString(param_removeExistingSetting).equalsIgnoreCase(YES))
				{
					HashMap<String, String> requestParams = new HashMap<String, String>();
					requestParams.put(param_action, action_deleteSetting);
					requestParams.put(param_subscriberID, subscriberID);
					requestParams.put(param_callerID, task.getString(param_callerID));
					requestParams.put(param_status, String.valueOf(1));
					requestParams.put(param_fromTime, String.valueOf(0));
					requestParams.put(param_toTime, String.valueOf(2359));

					WebServiceContext tempTask = Utility.getTask(requestParams);
					deleteSetting(tempTask);
				}

				// Subscriber object is stored in task by processActivation method.
				Subscriber subscriber = (Subscriber) task.get(param_subscriber);
				String circleID = DataUtils.getUserCircle(task);
				boolean isPrepaid = DataUtils.isUserPrepaid(task);

				String language = task.getString(param_language);
				if (language != null && (subscriber.language() == null || !subscriber.language().equalsIgnoreCase(language)))
					rbtDBManager.setSubscriberLanguage(subscriberID, language);

				String callerID = (!task.containsKey(param_callerID) || 
						task.getString(param_callerID).equalsIgnoreCase(ALL)) ? null
								: task.getString(param_callerID);
				int clipID = Integer.parseInt(task.getString(param_clipID));

				Categories categoriesObj = CategoriesImpl.getCategory(category);
				int categoryType = category.getCategoryTpe();

				String subscriberFile = null;
				Clip clip = null;
				Category musicbox = null;
				String classType = "DEFAULT";
				if (categoryType == iRBTConstant.DTMF_CATEGORY)
				{
					clip = rbtCacheManager.getClip(clipID, browsingLanguage);
					subscriberFile = clip.getClipRbtWavFile();
					classType = clip.getClassType();
				}
				else if (categoryType == iRBTConstant.BOUQUET)
				{
					musicbox = rbtCacheManager.getCategory(clipID, browsingLanguage);
					subscriberFile = musicbox.getCategoryPromoId();
					classType = musicbox.getClassType();
				}

				HashMap<String, Object> clipMap = new HashMap<String, Object>();
				if (clip != null)
				{
					clipMap.put("CLIP_CLASS", clip.getClassType());
					clipMap.put("CLIP_END", clip.getClipEndTime());
					clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
					clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
					clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
					clipMap.put("CLIP_NAME", clip.getClipName());
				}
				else
				{
					clipMap.put("CLIP_WAV", subscriberFile);
				}

				int maxSelections = subscriber.maxSelections();
				String selectedBy = getMode(task);
				String selectionInfo = getModeInfo(task) + "|" + maxSelections;

				CosDetails cos = TataUtility.getSubscriberCOS(task, subscriber); 
				if (categoryType == iRBTConstant.DTMF_CATEGORY && (cos != null && !cos.isDefaultCos() &&
						subscriber.maxSelections() < cos.getFreeSongs())) {
					selectedBy = selectedBy + "-COS" + cos.getCosId();
				}

				boolean useUIChargeClass = task.containsKey(param_useUIChargeClass)
						&& task.getString(param_useUIChargeClass).equalsIgnoreCase(YES);
				if (useUIChargeClass && task.containsKey(param_chargeClass))
				{
					classType = task.getString(param_chargeClass);
				}

				SubscriberDownloads download = rbtDBManager.getActiveSubscriberDownload(
						subscriberID, subscriberFile);
				if (download == null) {
					rbtDBManager.addSubscriberDownload(subscriberID, subscriberFile, categoryID,
							null, false, categoryType, classType, selectedBy, selectionInfo);
				}

				Calendar endCal = Calendar.getInstance();
				endCal.set(2037, 0, 1);
				Date endDate = endCal.getTime();

				int status = 1;
				boolean inLoop = false;
				if (task.containsKey(param_inLoop)
						&& task.getString(param_inLoop).equalsIgnoreCase(YES))
				{
					status = 2;
					inLoop = true;
				}

				boolean changeSubType = true;
				String messagePath = null;
				Parameters messagePathParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "MESSAGE_PATH", null);
				if (messagePathParam != null)
					messagePath = messagePathParam.getValue().trim();

				String selResponse = null;
//				if (getParamAsBoolean(iRBTConstant.COMMON, iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE")) 
				if (isSupportSMClientModel(task, SELECTION_OFFERTYPE))
				{
					HashMap<String, String> selectionInfoMap = getSelectionInfoMap(task);
					String baseOfferID = null;
					String selOfferID = null;
//					if(getParamAsBoolean(iRBTConstant.COMMON,iRBTConstant.SUPPORT_SMCLIENT_API,"FALSE")){
					baseOfferID = getOfferID(task, COMBO_SUB_OFFERTYPE);
					selOfferID = getOfferID(task, SELECTION_OFFERTYPE);
//					}
					selResponse = smAddSubScriberSelection(selectionInfoMap, selOfferID, classType, task, 
							selResponse, subscriberID, callerID, categoriesObj, null, endDate, status, selectedBy, 
							selectionInfo, isPrepaid, messagePath, 0, 2359, true, null, subscriber.subYes(), 
							circleID, null, subscriber, null, baseOfferID, -1, changeSubType, inLoop, clipMap, clip, category);
				}
				else
				{
					selResponse = rbtDBManager.addSubscriberSelections(subscriberID, callerID, categoriesObj, clipMap, null,
							null, endDate, status, selectedBy, selectionInfo, 0, isPrepaid, changeSubType, messagePath,
							0, 2359, classType, true, false, null, null, subscriber.subYes(), null, circleID,
							false, true, null, false, true, inLoop, null, subscriber, 0, null, null, useUIChargeClass, null, false);
				}
				response = Utility.getResponseString(selResponse);
			}
			catch (Exception e)
			{
				logger.error("", e);
				response = ERROR;
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#deleteTone(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String deleteTone(WebServiceContext task)
	{
		String response = ERROR;

		try
		{
			String browsingLanguage = task.getString(param_browsingLanguage);
			String subscriberID = task.getString(param_subscriberID);
			int categoryID = Integer.parseInt(task.getString(param_categoryID));	
			int clipID = Integer.parseInt(task.getString(param_clipID));

			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
			int categoryType = category.getCategoryTpe();

			String promoID = null;
			Clip clip = null;
			Category musicbox = null;
			if (categoryType == iRBTConstant.DTMF_CATEGORY)
			{
				clip  = rbtCacheManager.getClip(clipID, browsingLanguage);
				promoID = clip.getClipRbtWavFile();
			}
			else if (categoryType == iRBTConstant.BOUQUET)
			{
				musicbox = rbtCacheManager.getCategory(clipID, browsingLanguage);
				promoID = musicbox.getCategoryPromoId();
			}

			boolean delResponse =  rbtDBManager.updateDownloadStatus(subscriberID, promoID, 'd');
			if (delResponse)
				response = SUCCESS;
			else
				response = FAILED;
		}
		catch(Exception e)
		{
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#shuffleDownloads(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String shuffleDownloads(WebServiceContext task)
	{
		String response = ERROR;

		try
		{
			String subscriberID = task.getString(param_subscriberID);

			SubscriberDownloads[] subscriberDownloads = rbtDBManager.getSubscriberAllActiveDownloads(subscriberID, iRBTConstant.DTMF_CATEGORY);
			if (subscriberDownloads == null || subscriberDownloads.length == 0)
				return FAILED;

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			task.put(param_subscriber, subscriber);
			String circleID = DataUtils.getUserCircle(task);
			boolean isPrepaid = DataUtils.isUserPrepaid(task);

			String calledNo = task.getString(param_calledNo);
			int maxSelections = subscriber.maxSelections();

			for (SubscriberDownloads subscriberDownload : subscriberDownloads)
			{
				if (subscriberDownload.downloadStatus() == 'y')
				{
					SubscriberStatus[] settings = rbtDBManager.getSubscriberStatus(subscriberID, subscriberDownload.promoId());
					boolean isDefault = false;
					if (settings != null)
					{
						for (SubscriberStatus setting : settings)
						{
							String callerID = setting.callerID();
							if ((callerID == null || callerID.equalsIgnoreCase("null"))
									&& setting.endTime().getTime() >= System.currentTimeMillis())
							{
								isDefault = true;
								break;
							}
						}
					}

					if (!isDefault)
					{
						rbtDBManager.addSubscriberSelections(subscriberID, null, subscriberDownload.categoryID(),
								subscriberDownload.promoId(), null, null, subscriberDownload.endTime(), 2, "LOOP",
								calledNo + "|" + maxSelections, 0, isPrepaid, false, null, 0, 2359, null, true, false, null,
								null, subscriber.subYes(), null, circleID, false, true, true, subscriber, null);
					}
				}
			}

			response = SUCCESS;
		}
		catch(Exception e)
		{
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getMode(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected String getMode(WebServiceContext task)
	{
		String mode = super.getMode(task);

		String action = task.getString(param_action);
		if (action != null && action.equalsIgnoreCase(action_deactivate))
		{
			String subscriberID = task.getString(param_subscriberID);
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);

			if (subscriber != null && (subscriber.activatedBy().indexOf("PROMO")) >= 0)
				mode += "-PROMO";
		}

		return mode;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getCos(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber)
	{
		CosDetails cos = TataUtility.getSubscriberCOS(task, subscriber);

		logger.info("response: " + cos.getCosId());
		return cos;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#smAddSubScriberSelection(java.util.HashMap, java.lang.String, java.lang.String, com.onmobile.apps.ringbacktones.webservice.common.Task, java.lang.String, java.lang.String, java.lang.String, com.onmobile.apps.ringbacktones.content.Categories, java.util.Date, java.util.Date, int, java.lang.String, java.lang.String, boolean, java.lang.String, int, int, boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.onmobile.apps.ringbacktones.content.Subscriber, java.lang.String, java.lang.String, int, boolean, boolean, java.util.HashMap, com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category)
	 */
	@Override
	protected String smAddSubScriberSelection(HashMap<String,String> selectionInfoMap, String selOfferID, String classType, WebServiceContext task,
			String selResponse, String subscriberID, String callerID, Categories categoriesObj, Date startDate, Date endDate, int status,
			String selectedBy, String selectionInfo, boolean isPrepaid, String messagePath, int fromTime, int toTime, boolean useSubManager,
			String chargingPackage, String subYes, String circleID, String transID, Subscriber subscriber, String interval, String baseOfferID,
			int i, boolean changeSubType, boolean inLoop, HashMap<String, Object> clipMap, Clip clip, Category category) throws Exception{

		HashMap<String, String> responseParams = new HashMap<String, String>();
		selectionInfoMap.put(WebServiceConstants.param_offerID, selOfferID);
		if(task.getString(param_action).equalsIgnoreCase(action_acceptGift)){
			selOfferID = "-2";
		}
		else if(selOfferID.equals("-2")){
			classType = task.getString(WebServiceConstants.param_chargeClass);
		}
		selResponse = rbtDBManager.smAddSubscriberSelections(subscriberID, callerID, categoriesObj, clipMap, null,
						null, endDate, status, selectedBy, selectionInfo, 0, isPrepaid, changeSubType, messagePath,
						0, 2359, classType, true, false, null, null, subscriber.subYes(), null, circleID,
						false, true, null, false, true, inLoop, null, subscriber, 0, null, null, responseParams);
		if(selResponse.equals(iRBTConstant.SELECTION_SUCCESS))
		{
			String selectionRefID = "";
			if (responseParams.containsKey("REF_ID"))
			{
				selectionRefID = responseParams.get(REF_ID);
			}	
			HashMap<String, String> extraParams = getSelectionExtraParams(subscriber, clip, category, callerID, selectionInfo, selectionInfoMap);
			smClientRquestForSelection(task, subscriberID, subscriber, classType, baseOfferID, selOfferID, selectedBy, selectionRefID, isPrepaid, -1, extraParams);
		}
		return selResponse;
	}
}
