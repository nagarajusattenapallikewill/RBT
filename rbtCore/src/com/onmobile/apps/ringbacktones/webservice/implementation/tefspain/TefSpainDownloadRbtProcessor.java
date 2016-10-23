package com.onmobile.apps.ringbacktones.webservice.implementation.tefspain;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

public class TefSpainDownloadRbtProcessor extends BasicRBTProcessor {
	private static Logger logger = Logger.getLogger(TefSpainRBTProcessor.class);

	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber) {
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}

	@Override
	public String deleteSetting(WebServiceContext task) {
		String modesForDeleteSetting = RBTParametersUtils.getParamAsString("COMMON", "MODES_FOR_DELETE_SETTING", null);
		List<String> modeForDeleteSetting = new ArrayList<String>();
		if (modesForDeleteSetting != null && !modesForDeleteSetting.isEmpty()) {
			modeForDeleteSetting = Arrays.asList(modesForDeleteSetting.split(","));
		}
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = null;
		int categoryType;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		else
			subscriber = rbtDBManager.getSubscriber(subscriberID);
		String mode = getMode(task);
		String deactivationModeInfo = getModeInfo(task);
		String response = ERROR;
		String categoryId = task.getString(param_categoryID);
		int categoryID;
		boolean isDirectDeactivation = YES.equalsIgnoreCase(task.getString(param_isDirectDeactivation));

		String subStatus = Utility.getSubscriberStatus(subscriber);
		if (subStatus.equals(LOCKED)) {
			writeEventLog(subscriberID, getMode(task), "201", PURCHASE, getClip(task), getCriteria(task));
			return subStatus;
		}
		String browsingLanguage = task.getString(param_browsingLanguage);
		String promoID = null;
		if (task.containsKey(param_clipID) && categoryId != null) {
			Category category = rbtCacheManager.getCategory(Integer.parseInt(categoryId));
			if (category != null && category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
				logger.info("Clip Based Deleting Setting not allowed for ODA_SHUFFLE");
				return NOT_ALLOWED;
			}
		} else if (categoryId != null
				&& rbtCacheManager.getCategory(Integer.parseInt(categoryId)).getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
			if (modeForDeleteSetting != null && !modeForDeleteSetting.isEmpty() && modeForDeleteSetting.contains(mode)) {
				logger.info("deactivating pack for : " + subscriberID + " catId= " + categoryId);

				List<ProvisioningRequests> activeProvisioningRequests = rbtDBManager.getActiveODAPackBySubscriberIDAndType(
						subscriberID, Integer.parseInt(categoryId));
				if (activeProvisioningRequests != null && !activeProvisioningRequests.isEmpty()) {
					HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();
					ProvisioningRequests activeProvisioningRequest = activeProvisioningRequests.get(0);
					packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE, mode);
					packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO, deactivationModeInfo);
					packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME, new Date().toString());
					String extraInfo = activeProvisioningRequest.getExtraInfo();
					HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
					if (xtraInfoMap == null)
						xtraInfoMap = new HashMap<String, String>();
					xtraInfoMap.putAll(packExtraInfoMap);
					String extraInfoString = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
					logger.debug("provisioningRequest: " + activeProvisioningRequest);
					rbtDBManager.updateProvisioningRequestsStatusAndExtraInfo(subscriberID,
							activeProvisioningRequest.getTransId(), 41, extraInfoString);
					response = "SUCCESS";

				}
			} else {
				response = super.deactivateODAPack(task);
			}

			return response;
		}

		//
		if (modeForDeleteSetting != null && !modeForDeleteSetting.isEmpty() && modeForDeleteSetting.contains(mode)) {
			if (task.containsKey(param_clipID)) {
				int clipID = Integer.parseInt(task.getString(param_clipID));
				Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);
				if (clip == null) {
					logger.info("Clip not in download or wrong clip ID: response : " + FAILED);
					return FAILED;
				}
				promoID = clip.getClipRbtWavFile();
			} else if (task.containsKey(param_rbtFile)) {
				String rbtFile = task.getString(param_rbtFile);
				if (rbtFile.toLowerCase().endsWith(".wav"))
					promoID = rbtFile.substring(0, rbtFile.length() - 4);
				else
					promoID = rbtFile;
			}
			logger.info("deactivating download  for : " + subscriberID + " catId= " + categoryId + "Clip Id :" + promoID);

			boolean toBeSupportDctReqForActPendingUser = RBTParametersUtils.getParamAsBoolean(iRBTConstant.WEBSERVICE,
					"TO_BE_SUPPORT_SONG_DCT_REQ_FOR_ACT_PENDING_USER", "false");

			if (toBeSupportDctReqForActPendingUser && subscriber != null
					&& rbtDBManager.isSubscriberActivationPending(subscriber) || rbtDBManager.isSubscriberInGrace(subscriber)) {
				logger.info("Dont accept deactivaion record becuase subscriberId: " + subscriberID + " subscriber is in : "
						+ subStatus);
				return FAILED;
			}

			categoryID = (task.containsKey(param_categoryID)) ? Integer.parseInt(task.getString(param_categoryID)) : -1;

			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
			categoryType = (category != null) ? category.getCategoryTpe() : -1;
			String deactivatedBy = getMode(task);
			String deselectionInfo = task.getString(param_modeInfo);

			String selType = task.getString(param_selectionType);
			if (selType == null || !selType.equals("2")) {
				SubscriberStatus[] subscriberStatus = rbtDBManager.getActiveSelectionsByType(subscriberID, 2);
				if (subscriberStatus != null) {
					for (SubscriberStatus selection : subscriberStatus) {
						if (selection.subscriberFile().equalsIgnoreCase(promoID))
							return FAILED;
					}
				}
			}

			SubscriberDownloads subDownload = rbtDBManager.getDownloadToBeDeactivated(subscriberID, promoID, categoryID,
					categoryType);
			String dwnExtraInfo = null;
			if (subDownload != null)
				dwnExtraInfo = subDownload.extraInfo();
			HashMap<String, String> attributeMapFromXML = DBUtility.getAttributeMapFromXML(dwnExtraInfo);
			if (attributeMapFromXML == null) {
				attributeMapFromXML = new HashMap<String, String>();
			}
			Set<Entry<String, Object>> entrySet = task.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				if (entry.getKey().startsWith(param_selectionInfo + "_")) {
					attributeMapFromXML.put(entry.getKey().substring(entry.getKey().indexOf('_') + 1), (String) entry.getValue());
				}
			}
			dwnExtraInfo = DBUtility.getAttributeXMLFromMap(attributeMapFromXML);

			// RBT-10785
			boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean("WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
			if (addProtocolNumber) {
				deselectionInfo = appendProtocolNumber(subscriberID, deselectionInfo);
			}
			Map<String, String> whereClauseMap = new HashMap<String, String>();
			boolean result = false;
			whereClauseMap.put("SUBSCRIBER_WAV_FILE", promoID);
			whereClauseMap.put("CATEGORY_ID", categoryId + "");

			if (subDownload != null) {
				if (dwnExtraInfo != null) {
					result = rbtDBManager.expireSubscriberDownloadAndUpdateExtraInfo(subscriberID, promoID, categoryID,
							categoryType, deactivatedBy, deselectionInfo, dwnExtraInfo, isDirectDeactivation);
				} else {
					result = rbtDBManager.expireSubscriberDownload(subscriberID, promoID, categoryID, categoryType,
							deactivatedBy, deselectionInfo, isDirectDeactivation);
				}
			}

			if (result) {
				writeEventLog(subscriberID, getMode(task), "0", PURCHASE, getClip(task), getCriteria(task));
				response = SUCCESS;
				if (rbtDBManager.isDownloadActivationPending(subDownload) || rbtDBManager.isDownloadGrace(subDownload)) {
					Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subDownload.extraInfo());
					if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.PACK)) {
						String cosID = extraInfoMap.get(iRBTConstant.PACK);
						CosDetails packCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
						// Parameters muiscPackCosIdParam =
						// CacheManagerUtil.getParametersCacheManager()
						// .getParameter("COMMON",
						// "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
						//
						// List<String> musicPackCosIdList = null;
						//
						// if(muiscPackCosIdParam != null) {
						// musicPackCosIdList =
						// ListUtils.convertToList(muiscPackCosIdParam.getValue(),
						// ",");
						// }

						List<ProvisioningRequests> provRequest = ProvisioningRequestsDao
								.getBySubscriberIDTypeAndNonDeactivatedStatus(subscriber.subID(), Integer.parseInt(cosID));
						if (provRequest != null
								&& (packCos == null || !packCos.getCosType().equalsIgnoreCase(
										iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT))) {
							rbtDBManager.decrementNumMaxSelectionsForPack(subscriberID, cosID);
						}
					} else {
						rbtDBManager.decrementNumMaxSelections(subscriberID);
					}
				}
			} else {
				writeEventLog(subscriberID, getMode(task), "201", PURCHASE, getClip(task), getCriteria(task));
				response = FAILED;
			}

			if (response.equalsIgnoreCase(SUCCESS)) {
				sendAcknowledgementSMS(task, "DELETE_TONE");
			}

		} else {
			response = super.deleteSetting(task);
		}
		return response;

	}

	// RBT-12419
	@Override
	public String processSelection(WebServiceContext task) {

		String processSelectionResult = super.processSelection(task);
		String subId = (String) task.get(param_subscriberID);
		String clipId = (String) task.get(param_clipID);
		String catId = (String) task.get(param_categoryID);
		HashMap<String, String> whereClauseMap = new HashMap<String, String>();
		Clip clip = null;
		if (clipId != null) {
			clip = RBTCacheManager.getInstance().getClip(Integer.parseInt(clipId));
		}
		logger.info("in processselection subId= " + subId + " , clipId= " + clipId + " catId= " + catId);
		if (RBTParametersUtils.getParamAsBoolean("COMMON", "SELECTION_MODEL_PARAMETER", "FALSE")) {
			if (processSelectionResult != null && processSelectionResult.equalsIgnoreCase(CLIP_EXPIRED) && clip != null
					&& catId != null) {
				processSelectionResult = CLIP_EXPIRED;
				whereClauseMap.put("SUBSCRIBER_WAV_FILE", clip.getClipRbtWavFile());
				SubscriberStatus subSatus = rbtDBManager.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subId,
						whereClauseMap);

				if (subSatus == null) {
					boolean removeFromDownload = rbtDBManager.removeSubscriberDownloadBySubIdAndWavFileAndCatId(subId,
							clip.getClipRbtWavFile(), Integer.parseInt(catId));
					if (removeFromDownload) {
						processSelectionResult = CLIP_EXPIRED_DOWNLOAD_DELETED;
					}
				}

			} else if (processSelectionResult != null && processSelectionResult.equalsIgnoreCase(CATEGORY_EXPIRED)
					&& catId != null) {
				processSelectionResult = CATEGORY_EXPIRED;
				whereClauseMap.put("CATEGORY_ID", catId);
				SubscriberStatus subSatus = rbtDBManager.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subId,
						whereClauseMap);
				if (subSatus == null) {
					boolean removeFromDownload = rbtDBManager.removeSubscriberDownloadBySubIdAndWavFileAndCatId(subId, null,
							Integer.parseInt(catId));
					if (removeFromDownload) {
						processSelectionResult = CATEGORY_EXPIRED_DOWNLOAD_DELETED;
					}
				}

			}
		}
		return processSelectionResult;
	}

	@Override
	protected String deactivateODAPack(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = task.getString(param_subscriberID);
		String internalRefId = null;
		Subscriber subscriber;
		String categoryID = task.getString(param_categoryID);
		// String fromTime = task.getString(param_fromTime);
		// String fromTimeMinutes = task.getString(param_fromTimeMinutes);
		// if(fromTime == null){
		// fromTime = "0";
		// }else if(fromTime.startsWith("0")){
		// fromTime = fromTime.substring(1);
		// }

		// String toTime = task.getString(param_toTime);
		// String toTimeMinutes = task.getString(param_toTimeMinutes);
		// if(toTime == null){
		// toTime = "2359";
		// }else if(toTime.startsWith("0")){
		// toTime = toTime.substring(1);
		// }

		int fromHrs = 0;
		int toHrs = 23;
		int fromMinutes = 0;
		int toMinutes = 59;

		boolean toBeConsiderFromTimeBased = false;
		boolean toBeConsiderToTimeBased = false;
		boolean toBeConsiderStatus = false;
		boolean toBeConsiderInterval = false;

		DecimalFormat decimalFormat = new DecimalFormat("00");
		int defaultFromTime = Integer.parseInt(fromHrs + decimalFormat.format(fromMinutes));
		int defaultToTime = Integer.parseInt(toHrs + decimalFormat.format(toMinutes));

		if (task.containsKey(param_fromTime)) {
			toBeConsiderFromTimeBased = true;
			fromHrs = Integer.parseInt(task.getString(param_fromTime));
		}
		if (task.containsKey(param_toTime)) {
			toBeConsiderToTimeBased = true;
			toHrs = Integer.parseInt(task.getString(param_toTime));
		}
		if (task.containsKey(param_toTimeMinutes)) {
			toMinutes = Integer.parseInt(task.getString(param_toTimeMinutes));
		}
		if (task.containsKey(param_fromTimeMinutes)) {
			fromMinutes = Integer.parseInt(task.getString(param_fromTimeMinutes));
		}

		String callerId = task.getString(param_callerID);

		int reqStatus = -1;
		String interval = null;

		if (task.containsKey(param_status)) {
			toBeConsiderStatus = true;
			reqStatus = Integer.parseInt(task.getString(param_status));
		}

		if (task.containsKey(param_interval)) {
			toBeConsiderInterval = true;
			interval = task.getString(param_interval);
		}

		int fromTime = Integer.parseInt(fromHrs + decimalFormat.format(fromMinutes));
		int toTime = Integer.parseInt(toHrs + decimalFormat.format(toMinutes));

		if (task.containsKey(param_internalRefId)) {
			internalRefId = task.getString(param_internalRefId);
		}

		try {
			subscriber = DataUtils.getSubscriber(task);
			String subStatus = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriberStatus(subscriber);
			if (subStatus.equals(LOCKED)) {
				writeEventLog(subscriber.subID(), getMode(task), "404", CUSTOMIZATION, getClip(task), getCriteria(task));
				return subStatus;
			}
			Set<String> refIdList = new HashSet<String>();

			com.onmobile.apps.ringbacktones.cache.content.Category category = rbtDBManager.getCategory(Integer
					.parseInt(categoryID));
			List<ProvisioningRequests> provisioningRequests = rbtDBManager.getBySubscriberIDAndType(subscriberID,
					Integer.parseInt(categoryID));
			List<ProvisioningRequests> activeProvisioningRequests = rbtDBManager.getActiveODAPackBySubscriberIDAndType(
					subscriberID, Integer.parseInt(categoryID));

			logger.debug("refIdList: " + refIdList);
			List<String> selectionsRefIdToDeactivate = new ArrayList<String>();
			boolean deleteDownloads = false;
			String refIdToDelete = null;
			String allCallerRefId = null;

			if (provisioningRequests != null && !provisioningRequests.isEmpty()) {
				for (ProvisioningRequests provisioningRequest : provisioningRequests) {
					logger.debug("subscriberDownload: " + provisioningRequest.getExtraInfo());

					String xtraInfo = provisioningRequest.getExtraInfo();
					HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(xtraInfo);
					if (!xtraInfoMap.containsKey("CALLER_ID") && !xtraInfoMap.containsKey("SEL_INTERVAL")
							&& xtraInfoMap.get("FROM_TIME").equals(defaultFromTime + "")
							&& xtraInfoMap.get("TO_TIME").equals(defaultToTime + "") && xtraInfoMap.get("STATUS").equals(1 +"")) {
						allCallerRefId = provisioningRequest.getTransId();
					}
					if (callerId != null && !callerId.equalsIgnoreCase("all")) {
						if (xtraInfoMap.containsKey("CALLER_ID")
								&& xtraInfoMap.get("CALLER_ID").equals(callerId)

								&& xtraInfoMap.get("FROM_TIME").equals(fromTime + "")
								&& xtraInfoMap.get("TO_TIME").equals(toTime + "")
								&& ((interval != null && xtraInfoMap.containsKey("SEL_INTERVAL") && interval.equals(xtraInfoMap
										.get("SEL_INTERVAL"))) || (interval == null && !xtraInfoMap.containsKey("SEL_INTERVAL")))) {
							refIdToDelete = provisioningRequest.getTransId();

						}

					} else {
						if (!xtraInfoMap.containsKey("CALLER_ID")
								&& xtraInfoMap.get("FROM_TIME").equals(fromTime + "")
								&& xtraInfoMap.get("TO_TIME").equals(toTime + "")
								&& ((interval != null && xtraInfoMap.containsKey("SEL_INTERVAL") && interval.equals(xtraInfoMap
										.get("SEL_INTERVAL"))) || (interval == null && !xtraInfoMap.containsKey("SEL_INTERVAL")))) {
							refIdToDelete = provisioningRequest.getTransId();

						}

					}
				}

			}

			Map<String, String> whereClauseMap = new HashMap<String, String>();
			if (activeProvisioningRequests != null && !activeProvisioningRequests.isEmpty()) {
				if (activeProvisioningRequests.size() == 1) {
					deleteDownloads = true;
				} else {

					List<SubscriberStatus> subscriberStatuses = rbtDBManager.getSubscriberActiveSelections(subscriberID,
							whereClauseMap);
					for (SubscriberStatus subscriberStatus : subscriberStatuses) {
						String xtraInfo = subscriberStatus.extraInfo();
						HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(xtraInfo);
						if (xtraInfoMap.containsKey("PROV_REF_ID") && xtraInfoMap.get("PROV_REF_ID").equals(refIdToDelete)) {
							selectionsRefIdToDeactivate.add(subscriberStatus.refID());
						}
					}
				}
			} else {
				refIdToDelete = allCallerRefId;
				deleteDownloads = true;
			}

			logger.debug("refIdToDelete: " + refIdToDelete);

			
			
			
			logger.debug("selectionsRefIdToDeactivate: " + selectionsRefIdToDeactivate);

			String deactivationMode = getMode(task);
			String deactivationModeInfo = getModeInfo(task);
			HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();

			packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE, deactivationMode);
			packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO, deactivationModeInfo);
			packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME, new Date().toString());
			boolean isPackDeactivated = false;

			if (refIdToDelete != null ) {
				
					ProvisioningRequests provisioningRequest = rbtDBManager.getProvisioningRequestFromRefId(subscriberID, refIdToDelete);
					String extraInfo = provisioningRequest.getExtraInfo();
					HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
					if (xtraInfoMap == null)
						xtraInfoMap = new HashMap<String, String>();
					xtraInfoMap.putAll(packExtraInfoMap);
					String extraInfoString = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
					logger.debug("provisioningRequest: " + provisioningRequest);
					if(deleteDownloads){
						rbtDBManager.updateProvisioningRequestsStatusAndExtraInfo(subscriberID, refIdToDelete, 41,
								extraInfoString);
						response = "SUCCESS" ;

					}else {
						RBTDBManager.getInstance().updateProvisioningRequestsStatusAndExtraInfo(subscriberID, refIdToDelete, 43,
								extraInfoString);
						for(String refId : selectionsRefIdToDeactivate){
							rbtDBManager.expireSubscriberSelection(subscriberID, refId, "SM");
						}
						
						response = "SUCCESS" ;
					}
					logger.debug("isPackDeactivated: " + isPackDeactivated);
					if (isPackDeactivated) {
						response = "SUCCESS";
					}
				}
			
			response = Utility.getResponseString(response);

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;

	}

	@Override
	public String deleteTone(WebServiceContext task) {
		String response = ERROR;
		String categoryId = task.getString(param_categoryID);
		if (task.containsKey(param_clipID) && categoryId != null) {
			Category category = rbtCacheManager.getCategory(Integer.parseInt(categoryId));
			if (category != null && category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
				logger.info("Clip Based Deleting Setting not allowed for ODA_SHUFFLE");
				return NOT_ALLOWED;
			}
		} else if (categoryId != null
				&& rbtCacheManager.getCategory(Integer.parseInt(categoryId)).getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
			response = deactivateODAPack(task);
			return response;
		}

		try {
			String action = task.getString(param_action);
			String subscriberID = task.getString(param_subscriberID);
			String browsingLanguage = task.getString(param_browsingLanguage);
			String refId = task.getString(param_refID);

			boolean isDirectDeactivation = YES.equalsIgnoreCase(task.getString(param_isDirectDeactivation));

			String promoID = null;
			int categoryID;
			int categoryType;

			Subscriber subscriber = null;
			if (task.containsKey(param_subscriber))
				subscriber = (Subscriber) task.get(param_subscriber);
			else
				subscriber = rbtDBManager.getSubscriber(subscriberID);
			String subStatus = Utility.getSubscriberStatus(subscriber);
			if (subStatus.equals(LOCKED)) {
				writeEventLog(subscriberID, getMode(task), "201", PURCHASE, getClip(task), getCriteria(task));
				return subStatus;
			}

			if (action.equalsIgnoreCase(action_deleteTone) || action.equalsIgnoreCase(action_deleteMultipleTones)) {

				boolean toBeSupportDctReqForActPendingUser = RBTParametersUtils.getParamAsBoolean(iRBTConstant.WEBSERVICE,
						"TO_BE_SUPPORT_SONG_DCT_REQ_FOR_ACT_PENDING_USER", "false");

				if (toBeSupportDctReqForActPendingUser && subscriber != null
						&& rbtDBManager.isSubscriberActivationPending(subscriber) || rbtDBManager.isSubscriberInGrace(subscriber)) {
					logger.info("Dont accept deactivaion record becuase subscriberId: " + subscriberID + " subscriber is in : "
							+ subStatus);
					return FAILED;
				}

				if (task.containsKey(param_clipID)) {
					int clipID = Integer.parseInt(task.getString(param_clipID));
					Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);
					if (clip == null) {
						logger.info("Clip not in download or wrong clip ID: response : " + FAILED);
						return FAILED;
					}
					promoID = clip.getClipRbtWavFile();
				} else if (task.containsKey(param_rbtFile)) {
					String rbtFile = task.getString(param_rbtFile);
					if (rbtFile.toLowerCase().endsWith(".wav"))
						promoID = rbtFile.substring(0, rbtFile.length() - 4);
					else
						promoID = rbtFile;
				}

				categoryID = (task.containsKey(param_categoryID)) ? Integer.parseInt(task.getString(param_categoryID)) : -1;
			} else {
				SubscriberDownloads subscriberDownload = rbtDBManager.getOldestActiveSubscriberDownload(subscriberID);
				if (subscriberDownload == null) {
					logger.info("response: " + NO_DOWNLOADS);
					writeEventLog(subscriberID, getMode(task), "203", PURCHASE, getClip(task), getCriteria(task));
					return NO_DOWNLOADS;
				}

				promoID = subscriberDownload.promoId();
				categoryID = subscriberDownload.categoryID();
			}

			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
			categoryType = (category != null) ? category.getCategoryTpe() : -1;
			String deactivatedBy = getMode(task);
			String deselectionInfo = task.getString(param_modeInfo);

			String selType = task.getString(param_selectionType);
			if (selType == null || !selType.equals("2")) {
				SubscriberStatus[] subscriberStatus = rbtDBManager.getActiveSelectionsByType(subscriberID, 2);
				if (subscriberStatus != null) {
					for (SubscriberStatus selection : subscriberStatus) {
						if (selection.subscriberFile().equalsIgnoreCase(promoID))
							return FAILED;
					}
				}
			}
			
			Category selectionCategory = null ;
			SubscriberStatus requestedSubscriberSelections = null;
			if( refId != null   && !refId.isEmpty()){
			requestedSubscriberSelections = rbtDBManager.getSelectionByRefId(subscriberID, refId);
			 selectionCategory = rbtCacheManager.getCategory(requestedSubscriberSelections.categoryID()) ;
			}else{
			 selectionCategory = rbtCacheManager.getCategory(category.getCategoryId()) ;
			}
			SubscriberDownloads subDownload = rbtDBManager.getDownloadToBeDeactivated(subscriberID, promoID, selectionCategory.getCategoryId(),
					categoryType);
			categoryId = selectionCategory.getCategoryId() + "" ;
			String dwnExtraInfo = null;
			if (subDownload != null)
				dwnExtraInfo = subDownload.extraInfo();
			HashMap<String, String> attributeMapFromXML = DBUtility.getAttributeMapFromXML(dwnExtraInfo);
			if (attributeMapFromXML == null) {
				attributeMapFromXML = new HashMap<String, String>();
			}
			Set<Entry<String, Object>> entrySet = task.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				if (entry.getKey().startsWith(param_selectionInfo + "_")) {
					attributeMapFromXML.put(entry.getKey().substring(entry.getKey().indexOf('_') + 1), (String) entry.getValue());
				}
			}
			dwnExtraInfo = DBUtility.getAttributeXMLFromMap(attributeMapFromXML);

			// RBT-10785
			boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean("WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
			if (addProtocolNumber) {
				deselectionInfo = appendProtocolNumber(subscriberID, deselectionInfo);
			}
			Map<String, String> whereClauseMap = new HashMap<String, String>();
			boolean result = false;
			whereClauseMap.put("SUBSCRIBER_WAV_FILE", promoID);
			whereClauseMap.put("CATEGORY_ID", categoryId + "");
			SubscriberDownloads downLoadTrackingEntry = rbtDBManager
					.getSubscriberActiveDownloadsByDownloadStatusAndCategoryAndPromoId(subscriberID, categoryID, categoryType,
							"t", promoID);
			List<SubscriberStatus> subscriberStatus = rbtDBManager.getSubscriberActiveSelections(subscriberID, whereClauseMap);
			if (subscriberStatus != null && subscriberStatus.size() > 1) {
				if (refId != null && !refId.isEmpty()) {
					result = rbtDBManager.expireSubscriberSelection(subscriberID, refId, "SM");
					if (requestedSubscriberSelections.status() == 1) {
						rbtDBManager.deleteDownloadwithTstatus(subscriberID, promoID, categoryId);

					}
				} else {
					rbtDBManager.deleteDownloadwithTstatus(subscriberID, promoID, categoryId);
				}
				subDownload = null;
			}else if(subscriberStatus != null && subscriberStatus.size() == 1){
				if(downLoadTrackingEntry != null && requestedSubscriberSelections != null && requestedSubscriberSelections.status() != 1){
					result = rbtDBManager.expireSubscriberSelection(subscriberID, refId, "SM");
					subDownload = null;
				}else if( downLoadTrackingEntry != null && requestedSubscriberSelections == null ){
					rbtDBManager.deleteDownloadwithTstatus(subscriberID, promoID, categoryId);
					subDownload = null;
				}
			}

			if (subDownload != null) {
				if (dwnExtraInfo != null) {
					result = rbtDBManager.expireSubscriberDownloadAndUpdateExtraInfo(subscriberID, promoID, categoryID,
							categoryType, deactivatedBy, deselectionInfo, dwnExtraInfo, isDirectDeactivation);
				} else {
					result = rbtDBManager.expireSubscriberDownload(subscriberID, promoID, categoryID, categoryType,
							deactivatedBy, deselectionInfo, isDirectDeactivation);
				}
			}

			if (result) {
				writeEventLog(subscriberID, getMode(task), "0", PURCHASE, getClip(task), getCriteria(task));
				response = SUCCESS;
				if (rbtDBManager.isDownloadActivationPending(subDownload) || rbtDBManager.isDownloadGrace(subDownload)) {
					Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subDownload.extraInfo());
					if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.PACK)) {
						String cosID = extraInfoMap.get(iRBTConstant.PACK);
						CosDetails packCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
						// Parameters muiscPackCosIdParam =
						// CacheManagerUtil.getParametersCacheManager()
						// .getParameter("COMMON",
						// "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
						//
						// List<String> musicPackCosIdList = null;
						//
						// if(muiscPackCosIdParam != null) {
						// musicPackCosIdList =
						// ListUtils.convertToList(muiscPackCosIdParam.getValue(),
						// ",");
						// }

						List<ProvisioningRequests> provRequest = ProvisioningRequestsDao
								.getBySubscriberIDTypeAndNonDeactivatedStatus(subscriber.subID(), Integer.parseInt(cosID));
						if (provRequest != null
								&& (packCos == null || !packCos.getCosType().equalsIgnoreCase(
										iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT))) {
							rbtDBManager.decrementNumMaxSelectionsForPack(subscriberID, cosID);
						}
					} else {
						rbtDBManager.decrementNumMaxSelections(subscriberID);
					}
				}
			} else {
				writeEventLog(subscriberID, getMode(task), "201", PURCHASE, getClip(task), getCriteria(task));
				response = FAILED;
			}

			if (response.equalsIgnoreCase(SUCCESS)) {
				sendAcknowledgementSMS(task, "DELETE_TONE");
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

}
