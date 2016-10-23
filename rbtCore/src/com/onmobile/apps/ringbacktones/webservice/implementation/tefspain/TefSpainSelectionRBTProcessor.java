package com.onmobile.apps.ringbacktones.webservice.implementation.tefspain;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.ExtraInfoKey;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Status;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Type;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberStatusImpl;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientResponse;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.filters.RbtFilterParser;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

public class TefSpainSelectionRBTProcessor extends BasicRBTProcessor {
	private static Logger logger = Logger.getLogger(TefSpainSelectionRBTProcessor.class);

	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber) {
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}

	@Override
	public String deleteSetting(WebServiceContext task) {
		
		String response = ERROR;
		String categoryID = task.getString(param_categoryID);
		if (task.containsKey(param_clipID) && categoryID != null) {
			Category category = rbtCacheManager.getCategory(Integer.parseInt(categoryID));
			if (category != null && category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
				logger.info("Clip Based Deleting Setting not allowed for ODA_SHUFFLE");
				return NOT_ALLOWED;
			}
		} else if (categoryID != null
				&& rbtCacheManager.getCategory(Integer.parseInt(categoryID)).getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
			response = deactivateODAPack(task);
			return response;
		}
		response = super.deleteSetting(task);
		return response;

	}

//	private String deactivateODAPack(WebServiceContext task) {
//		String response = ERROR;
//		String subscriberID = task.getString(param_subscriberID);
//		String internalRefId = null;
//		Subscriber subscriber;
//		String categoryID = task.getString(param_categoryID);
//        String fromTime = task.getString(param_fromTime);
//        if(fromTime == null){
//			fromTime = "0";
//        }else if(fromTime.startsWith("0")){
//        	fromTime = fromTime.substring(1);
//        }
//        
//        String toTime = task.getString(param_toTime);
//        if(toTime == null){
//			toTime = "2359";
//        }else if(toTime.startsWith("0")){
//        	toTime = toTime.substring(1);
//        }
//
//        if (task.containsKey(param_internalRefId)) {
//			internalRefId = task.getString(param_internalRefId);
//		}
//        
//		try {
//			subscriber = DataUtils.getSubscriber(task);
//			String subStatus = com.onmobile.apps.ringbacktones.webservice.common.Utility
//					.getSubscriberStatus(subscriber);
//			if (subStatus.equals(LOCKED)) {
//				writeEventLog(subscriber.subID(), getMode(task), "404", CUSTOMIZATION,
//						getClip(task), getCriteria(task));
//				return subStatus;
//			}
//			Set<String> refIdList = new HashSet<String>();
//			String callerId = task.getString(param_callerID);
//			SubscriberStatus[] selectionRecords = rbtDBManager.getAllActiveSubSelectionRecords(
//					subscriberID, 0);
//			if (selectionRecords != null && selectionRecords.length > 0) {
//				for (SubscriberStatus selection : selectionRecords) {
//					int selFromTime = selection.fromTime();
//					int selToTime = selection.toTime();
//					String selCallerId = selection.callerID();
//					if (selCallerId == null)
//						selCallerId = "ALL";
//					String xtraInfo = selection.extraInfo();
//					HashMap<String, String> xtraInfoMap = DBUtility
//							.getAttributeMapFromXML(xtraInfo);
//					String provRefId = null;
//					if (xtraInfoMap != null) {
//						provRefId = xtraInfoMap.get("PROV_REF_ID");
//					}
//					if ((callerId == null || callerId.equalsIgnoreCase(selCallerId))
//							&& provRefId != null && fromTime.equalsIgnoreCase(selFromTime + "")
//							&& toTime.equalsIgnoreCase(selToTime + "")) {
//						refIdList.add(provRefId);
//					}
//				}
//			}
//            
//			Set<String> refIdToCheckList = new HashSet<String>();
//            refIdToCheckList.addAll(refIdList);
//			if (refIdList.size() > 0) {
//				for (String refId : refIdList) {
//					ProvisioningRequests provisioningRequest= rbtDBManager
//							.getProvisioningRequestFromRefId(subscriberID, refId);
//					int status = provisioningRequest.getStatus();
//					if (!(status == 30 || status == 31 || status == 32 || status == 33
//							|| status == 50)) {
//						refIdToCheckList.remove(refId);			
//					}
//					if (refIdToCheckList.size() == 0) {
//						logger.info("ODA Pack is already deactivated");
//						return PACK_ALREADY_DEACTIVE;
//					}
//				}
//			}
//
//			String deactivationMode = getMode(task);
//			String deactivationModeInfo = getModeInfo(task);
//			HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();
//			
//			packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE, deactivationMode);
//			packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO,
//					deactivationModeInfo);
//			packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME,
//					new Date().toString());
//			boolean isPackDeactivated = false;
//			
//			if (refIdToCheckList.size() > 0) {
//				for (String refId : refIdToCheckList) {
//					ProvisioningRequests provisioningRequest= rbtDBManager
//							.getProvisioningRequestFromRefId(subscriberID, refId);
//					String extraInfo = provisioningRequest.getExtraInfo();
//					HashMap<String,String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
//					if(xtraInfoMap == null)
//						xtraInfoMap = new HashMap<String,String>();
//					xtraInfoMap.putAll(packExtraInfoMap);
//					isPackDeactivated = rbtDBManager.deactivateODAPack(subscriber.subID(), categoryID,
//							refId, xtraInfoMap, null);
//				}
//			}
//
//			if (isPackDeactivated) {
//				response = "SUCCESS";
//			}
//			response = Utility.getResponseString(response);
//
//		} catch (Exception e) {
//			logger.error("", e);
//			response = ERROR;
//		}
//
//		logger.info("response: " + response);
//		return response;
//
//	}

	@Override
	public String deactivatePack(WebServiceContext task) {

		String response = ERROR;
		String subscriberID = task.getString(param_subscriberID);
		String packCosId = null;
		String internalRefId = null;
		Subscriber subscriber;
		CosDetails packCosDetails = null;
		if (task.containsKey(param_packCosId)) {
			packCosId = task.getString(param_packCosId);
			packCosDetails = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(packCosId, DataUtils.getUserCircle(task));
			if (packCosDetails == null) {
				return INVALID_PACK_COS_ID;
			}
		}else if(task.containsKey(param_categoryID)){
			String categoryID = task.getString(param_categoryID);
			if (categoryID != null
					&& rbtCacheManager.getCategory(Integer.parseInt(categoryID)).getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
				String deactivateODAPackResponse = deactivateODAPack(task); 
				return deactivateODAPackResponse;
			}

		}

		if (task.containsKey(param_internalRefId)) {
			internalRefId = task.getString(param_internalRefId);
		}

		if (packCosId == null && internalRefId == null) {
			return INVALID_PARAMETER;
		}

		try {
			subscriber = DataUtils.getSubscriber(task);
			if (rbtDBManager.isAutoDownloadPackActivated(subscriber)) {
				return DCT_NOT_ALLOWED;
			}

			String status = USER_NOT_EXISTS;
			if (subscriber != null)
				status = Utility.getSubscriberStatus(subscriber);
			String subStatus = com.onmobile.apps.ringbacktones.webservice.common.Utility
					.getSubscriberStatus(subscriber);
			if (subStatus.equals(LOCKED)) {
				writeEventLog(subscriber.subID(), getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return subStatus;
			}

			if (!rbtDBManager.isPackActivated(subscriber, packCosDetails)) {
				return PACK_ALREADY_DEACTIVE;
			}

			String deactivationMode = getMode(task);
			String deactivationModeInfo = getModeInfo(task);
			HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();
			Set<String> fileSet = new HashSet<String>();

			String browsingLanguage = task.getString(param_browsingLanguage);

			packExtraInfoMap.put(
					iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE,
					deactivationMode);
			packExtraInfoMap.put(
					iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO,
					deactivationModeInfo);
			packExtraInfoMap.put(
					iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME,
					new Date().toString());
			boolean isPackDeactivated = rbtDBManager.deactivatePack(subscriber,
					packCosDetails, internalRefId, packExtraInfoMap);

			if (isPackDeactivated)
				deleteSettingsInSet(fileSet, subscriberID, deactivationMode,
						browsingLanguage, null, "99");

			if (isPackDeactivated) {
				response = "SUCCESS";
			}
			response = Utility.getResponseString(response);

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
		}
	
	
	//RBT-12419
	@Override
	public String processSelection(WebServiceContext task) {
		
		String processSelectionResult = super.processSelection(task);
		String  subId = (String) task.get(param_subscriberID);
		String clipId=(String)task.get(param_clipID);
		String  catId = (String) task.get(param_categoryID);
		HashMap<String, String> whereClauseMap=new HashMap<String, String>();
		Clip clip=null;
		if(clipId!=null) {
			clip=RBTCacheManager.getInstance().getClip(Integer.parseInt(clipId));
		}
		logger.info("in processselection subId= "+subId+" , clipId= "+clipId+ " catId= "+catId);
		if(RBTParametersUtils.getParamAsBoolean("COMMON", "SELECTION_MODEL_PARAMETER", "FALSE")){
			if(processSelectionResult!=null && processSelectionResult.equalsIgnoreCase(CLIP_EXPIRED) && clip!=null && catId!=null) {
				processSelectionResult = CLIP_EXPIRED;
				whereClauseMap.put("SUBSCRIBER_WAV_FILE", clip.getClipRbtWavFile());
				SubscriberStatus subSatus = rbtDBManager.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subId, whereClauseMap);
				
				if(subSatus==null) {
					boolean removeFromDownload = rbtDBManager.removeSubscriberDownloadBySubIdAndWavFileAndCatId(subId, clip.getClipRbtWavFile(), Integer.parseInt(catId));
					if(removeFromDownload) {
						processSelectionResult = CLIP_EXPIRED_DOWNLOAD_DELETED;
					}
				}
				
			}else if(processSelectionResult!=null && processSelectionResult.equalsIgnoreCase(CATEGORY_EXPIRED) && catId!=null) {
				processSelectionResult = CATEGORY_EXPIRED;
				whereClauseMap.put("CATEGORY_ID", catId);
				SubscriberStatus subSatus =rbtDBManager.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subId, whereClauseMap);
				if(subSatus==null) {
					boolean removeFromDownload=rbtDBManager.removeSubscriberDownloadBySubIdAndWavFileAndCatId(subId, null, Integer.parseInt(catId));
					if(removeFromDownload) {
						processSelectionResult = CATEGORY_EXPIRED_DOWNLOAD_DELETED;
					}
				}
				
			  }	
		}
		return processSelectionResult;
	}
	
}
