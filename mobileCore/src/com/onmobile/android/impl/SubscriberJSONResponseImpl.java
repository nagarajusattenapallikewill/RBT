package com.onmobile.android.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.ExtendedClipBean;
import com.onmobile.android.beans.ExtendedDownloadBean;
import com.onmobile.android.beans.ExtendedGiftBean;
import com.onmobile.android.beans.ExtendedGroupBean;
import com.onmobile.android.beans.ExtendedLoginUserBean;
import com.onmobile.android.beans.ExtendedSelectionBean;
import com.onmobile.android.beans.ExtendedSubscriberBean;
import com.onmobile.android.beans.ExtendedSubscriberIdRespBean;
import com.onmobile.android.beans.GetCurrentPlayingSongBean;
import com.onmobile.android.beans.OfferBean;
import com.onmobile.android.beans.SelectionDetailsBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.interfaces.SubscriberResponse;
import com.onmobile.android.managers.SubscriberManager;
import com.onmobile.android.utils.AESUtils;
import com.onmobile.android.utils.ObjectGsonUtils;
import com.onmobile.android.utils.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PredefinedGroup;


public class SubscriberJSONResponseImpl implements SubscriberResponse{

	//private RBTClient client = RBTClient.getInstance();
	private SubscriberManager subscriberManager = SubscriberManager.getSubscriberManagerObj();
	public static Logger logger = Logger.getLogger(SubscriberJSONResponseImpl.class);
	@SuppressWarnings("unused")
	private String channel;

	public SubscriberJSONResponseImpl(String channel){
		this.channel = channel;
	}

	public String getMSISDN(String UID, String reqMsisdn, String circleId,
			boolean getCircleId, String registrationsource, String userAgent) {
		ExtendedSubscriberIdRespBean respBean = null;
		String responseString = null;
		if (Utility.isStringValid(UID)) {
			if (!Utility.isStringValid(reqMsisdn)) {
				reqMsisdn = null;
			}
			respBean = subscriberManager.getSubscriberMSISDN(UID, reqMsisdn, circleId, registrationsource, userAgent);
		}
		logger.info("UID: " + UID + ", ExtendedSubscriberIdRespBean returned: " + respBean);
		if (respBean != null) {
			if (PropertyConfigurator.isEncryptionEnabled()
					&& respBean.getSubscriberId() != null) {
				respBean.setSubscriberId(AESUtils.encrypt(respBean
						.getSubscriberId(), PropertyConfigurator
						.getResponseSubscriberIdEncryptionKey()));
			}
			if (getCircleId || PropertyConfigurator.isCircleIdRequiredInGetSubscriberIdResp()) {
				responseString =  ObjectGsonUtils.objectToGson(respBean);
			} else {
				if (respBean.getSubscriberId() != null) {
					responseString = respBean.getSubscriberId();
				} else {
					responseString = respBean.getResponse();
				}
			}
		}
		logger.info("responseString: " + responseString);
		return responseString;
	}

	public String getSubscriberStatus(String subId) {
		boolean isSubscribed = subscriberManager.getSubscriberStatus(subId);
		logger.info("subscriber status: " + isSubscribed);
		return String.valueOf(isSubscribed);
	}

	public String getVoluntarySuspendedStatus(String subId)
	{
		boolean isSuspended = subscriberManager.getVoluntarySuspendedStatus(subId);
		logger.info("subscriber voluntary subscription status: " + isSuspended);
		return String.valueOf(isSuspended);
	}

	public String processSuspension(String subId, boolean suspend){
		String response = subscriberManager.processSuspension(subId, suspend);
		logger.info("subscriber voluntary subscription response: " + response);
		return response;
	}

	public String getSubscriberInfo(String subId, String uid, String circleId) {
		ExtendedSubscriberBean subscriber = subscriberManager.getSubscriberInfo(subId, uid, circleId);
		return ObjectGsonUtils.objectToGson(subscriber);
	}

	public String giftSong(String subscriberId, String contentId,
			String giftee, String channel, StringBuffer response ,String chargeClass, String categoryId) {
		String giftResponse = subscriberManager.addGift(subscriberId, contentId, giftee, channel, response, chargeClass, categoryId);
		logger.info("Gift Song response" + giftResponse);
		return giftResponse;
	}

	@Override
	public String getGiftInbox(String subId) {
		ExtendedGiftBean[] giftInboxResponse = subscriberManager.getGiftInbox(subId);
		logger.info("GiftInbox response" + giftInboxResponse);
		return ObjectGsonUtils.objectToGson(giftInboxResponse);
	}

	@Override
	public String getGiftInboxCount(String subId) {
		ExtendedGiftBean[] giftInboxResponse = subscriberManager.getGiftInbox(subId);
		int giftInboxCount = 0;
		if(giftInboxResponse != null)
			giftInboxCount = giftInboxResponse.length;
		logger.info("getGiftInboxCount response" + giftInboxCount);
		return ObjectGsonUtils.objectToGson(giftInboxCount);
	}

	public String rejectGift(String gifterId, String gifteeId, String giftSentTime){	
		String  rejectGiftResponse = subscriberManager.rejectGift(gifterId, gifteeId, giftSentTime);
		logger.info("rejectGift response" + rejectGiftResponse);
		return ObjectGsonUtils.objectToGson(rejectGiftResponse);
	}


	public String activate_Subscriber(String subscriberId, String channel, String subscriptionClass, String baseOfferId) {
		String s1=null;
		try {
			s1=subscriberManager.activateSubscriber(subscriberId, channel, subscriptionClass, baseOfferId);
		} catch (Exception e) {
			logger.error("Exception Caught in activateSubscriber. " + e,  e);
		}
		return s1;
	}

	public String deactivate_Subscriber(String subscriberId, String channel) {
		String response = null;
		try {
			response = subscriberManager.deactivateSubscriber(subscriberId, channel);
		} catch (Exception e) {
			logger.error("Exception Caught in deactivateSubscriber. " + e,  e);
		}
		return response;
	}

	@Override
	public String setSubscriberSelection(String subscriberId, String caller, String clipId,
			String categoryId, String channel, String chargeClass, String musicPack, String inLoop, boolean isConsent
			, String subscriptionClass, String baseOfferId, String selOfferId, boolean useUIChargeClass,String smOfferType,String circleID, boolean isUdsOptIn) {
		String s=null;
		try {
			s=subscriberManager.addSubscriberSelection(subscriberId, caller, clipId, categoryId, channel, chargeClass, musicPack, inLoop, isConsent
					, subscriptionClass, baseOfferId, selOfferId, useUIChargeClass,smOfferType,circleID,isUdsOptIn);

		} catch (Exception e) {
			logger.error("Exception in addSubscriberSelection: " + e, e);
			e.printStackTrace();
		}
		return s;
	}

	public String getSelectionAmount(String subscriberId, String clipId,
			String categoryId, String channel, String chargeClass, String musicPack, String profilePack, boolean useUIChargeClass) {
		String s=null;
		try {
			s=subscriberManager.getSelectionAmount(subscriberId, clipId, categoryId, channel, chargeClass, musicPack, profilePack, useUIChargeClass);

		} catch (Exception e) {
			logger.error("Error:", e);
			e.printStackTrace();
		}
		return s;
	}

	public String getGiftAmount(String subscriberId, String clipId,
			String categoryId, String channel, String chargeClass) {
		String s=null;
		try {
			s=subscriberManager.getGiftAmount(subscriberId, clipId, categoryId, channel, chargeClass);

		} catch (Exception e) {
			logger.error("Error:", e);
			e.printStackTrace();
		}
		return s;
	}

	public String addSubscriberDownload(String subscriberId, String clipId,
			String categoryId, String channel, String isMusicPack) {
		String retString = subscriberManager.addSubscriberDownload(subscriberId, clipId, categoryId, channel, isMusicPack);
		return retString;
	}

	public  String addSubscriberBookMark(String subscriberId, String clipId, String categoryId, String channel){
		String retString= subscriberManager.addSubscriberBookMark(subscriberId, clipId, categoryId, channel);
		return retString;
	}

	public String getListDownloads(String subscriberId) {
		List<ExtendedDownloadBean> downloads = subscriberManager.getDownloads(subscriberId);
		return (ObjectGsonUtils.objectToGson(downloads));
	}

	public String getListBookmarks(String subscriberId) {
		List<ExtendedClipBean> extendedBookmarkedClips = new ArrayList<ExtendedClipBean>();
		extendedBookmarkedClips = subscriberManager.getBookmarks(subscriberId);
		return (ObjectGsonUtils.objectToGson(extendedBookmarkedClips));
	}

	public String getListSelections(String subscriberId,  boolean isDownloadsMerged, String browsingLangauge, String appName, String circleId) {
		List<ExtendedSelectionBean> extendedSubsriberLibraryClips = new ArrayList<ExtendedSelectionBean>();
		extendedSubsriberLibraryClips = subscriberManager.getSubscriberSelections(subscriberId, isDownloadsMerged, browsingLangauge, appName, circleId);
		return (ObjectGsonUtils.objectToGson(extendedSubsriberLibraryClips));
	}

	@Override
	public String getListProfileSelections(String subscriberId) {
		List<ExtendedSelectionBean> extendedSubsriberLibraryClips = new ArrayList<ExtendedSelectionBean>();
		extendedSubsriberLibraryClips = subscriberManager.getSubscriberProfileSelections(subscriberId);
		return (ObjectGsonUtils.objectToGson(extendedSubsriberLibraryClips));
	}

	public String removeSubscriberSelection(String subscriberId, String clipId,
			String caller, String channel, String selStartTime, String selEndTime, String rbtWavFile, String circleId) {
		String retString = subscriberManager.removeSubscriberSelection(subscriberId, clipId, caller, channel, selStartTime, selEndTime, rbtWavFile, circleId);
		return retString;
	}

	public String removeSubscriberBookMark(String subscriberId, String clipId, String categoryId, String channel) {
		String retString= subscriberManager.removeSubscriberBookMark(subscriberId, clipId, categoryId, channel);
		return retString;
	}

	public String removeSubscriberDownload(String subId, String clipId, String categoryId, String channel) {
		String retString= subscriberManager.removeSubscriberDownload(subId, clipId, categoryId, channel);
		return retString;
	}

	public String getCountOfAvailableMusicPackDownloads(String subId)
	{
		String retString= subscriberManager.getCountOfAvailableMusicPackDownloads(subId);
		return retString;
	}


	@Override
	public String getGroups(String subscriberId) {
		List<ExtendedGroupBean> groups = new ArrayList<ExtendedGroupBean>();
		groups = subscriberManager.getGroups(subscriberId);
		return (ObjectGsonUtils.objectToGson(groups));
	}

	@Override
	public String getPredefinedGroups() {
		PredefinedGroup[] groups = null;
		groups = subscriberManager.getPredefinedGroups();
		return (ObjectGsonUtils.objectToGson(groups));
	}

	@Override
	public String getCopySelections(String copyMobileNumber, String subscriberId) {
		List<ExtendedSelectionBean> extendedSubsriberLibraryClips = new ArrayList<ExtendedSelectionBean>();
		extendedSubsriberLibraryClips = subscriberManager.getCopySelections(copyMobileNumber, subscriberId);
		return (ObjectGsonUtils.objectToGson(extendedSubsriberLibraryClips));
	}

	@Override
	public String setSubscriberSelection(String subId, String callerId,
			String clipId, String catId, Integer status, boolean isPrepaid,
			String mode, String modeInfo, String lang, Integer startTimeHrs,
			Integer startTimeMins, Integer endTimeHrs, Integer endTimeMins,
			String interval, Integer cosId, String gifterId, String giftSentTime, String selStartTime, String selEndTime,String inLoop, boolean isConsent
			, String subscriptionClass, String baseOfferId, String chargeClass, String selOfferId, boolean useUIChargeClass,String smOfferType) {
		String s=null;
		try {
			s=subscriberManager.addSubscriberSelection(subId, callerId, clipId, catId, status, isPrepaid, mode, modeInfo, lang, startTimeHrs,
					startTimeMins, endTimeHrs, endTimeMins, interval, cosId, gifterId, giftSentTime, selStartTime, selEndTime, inLoop, isConsent,
					subscriptionClass, baseOfferId, chargeClass, selOfferId, useUIChargeClass,smOfferType);

		} catch (Exception e) {
			logger.error("Error:", e);
			e.printStackTrace();
		}
		return s;
	}

	@Override
	public String isProfilePackActivated(String subId) {
		boolean isProfilePackAcivated = subscriberManager.isProfilePackActivated(subId);
		logger.info("isProfilePackActivated: " + isProfilePackAcivated);
		return String.valueOf(isProfilePackAcivated);
	}

	@Override
	public String isPackActivated(String subId) {
		boolean isPackAcivated = subscriberManager.isPackActivated(subId);
		logger.info("isPackActivated: " + isPackAcivated);
		return String.valueOf(isPackAcivated);
	}

	@Override
	public String setSubscriberProfileSelection(String subscriberId,
			String caller, String clipId, String categoryId, String channel,
			String selectionStartTime, String selectionEndTime, String rbtWavFile, boolean isConsent
			, String subscriptionClass, String baseOfferId, String chargeClass, String selOfferId, String profileHours, boolean useUIChargeClass,String smOfferType) {
		String s=null;
		try {
			s=subscriberManager.addSubscriberProfileSelection(subscriberId, caller, clipId, categoryId, channel, selectionStartTime, selectionEndTime, rbtWavFile, 
					isConsent, subscriptionClass, baseOfferId, chargeClass, selOfferId, profileHours, useUIChargeClass,smOfferType);

		} catch (Exception e) {
			logger.error("Error:", e);
			e.printStackTrace();
		}
		return s;
	}

	@Override
	public String deactivateProfilePack(String subscriberId) {
		String s=null;
		try {
			s=subscriberManager.deactivateProfilePack(subscriberId);

		} catch (Exception e) {
			logger.error("Error:", e);
			e.printStackTrace();
		}
		return s;
	}

	@Override
	public String deactivateMusicPack(String subscriberId) {
		String s=null;
		try {
			s=subscriberManager.deactivateMusicPack(subscriberId);

		} catch (Exception e) {
			logger.error("Error:", e);
			e.printStackTrace();
		}
		return s;
	}

	@Override
	public String subscribeUser(String subscriberId, String type,
			String password, Boolean isResetPassword, String uid,
			String appName, boolean getCircleId, String registrationsource,
			String userAgent) {
		String s = null;
		try {
			s = subscriberManager.subscribeUser(subscriberId, type, password,
					isResetPassword, uid, appName);
			if  (type.equalsIgnoreCase("login") && s.equalsIgnoreCase("success")) {
				String circleId = Utility.getCircleId(subscriberId);
				Utility.writeRegistrationLogs(circleId, subscriberId, registrationsource, userAgent);
				if (getCircleId || PropertyConfigurator.isCircleIdRequiredInLoginUserResp()) {
					ExtendedLoginUserBean respBean = new ExtendedLoginUserBean(s, circleId);
					logger.info("ExtendedLoginUserBean: " + respBean);
					return ObjectGsonUtils.objectToGson(respBean);
				}
			}
		} catch (Exception e) {
			logger.error("Error:", e);
			e.printStackTrace();
		}
		return s;
	}

	@Override
	public String getParameterValue(String param) {
		String s=null;
		try {
			s = subscriberManager.getParameterValue(param);

		} catch (Exception e) {
			logger.error("Error:", e);
		}
		return s;
	}

	@Override
	public String addGCMRegistration(String regId, String subID, String os_type) {
		return subscriberManager.addGCMRegistration(regId, subID, os_type);
	}

	@Override
	public String removeGCMRegistration(String regId, String subID) {
		return subscriberManager.removeGCMRegistration(regId, subID);
	}

	@Override
	public String addContactMemebers(String subscriberId, String callerId,
			String callerName, String preGroupId, String groupId, String mode) {
		String response = subscriberManager.addContactMemebers(subscriberId, callerId, callerName, preGroupId, groupId, mode);

		return response;
	}

	@Override
	public String addMultipleContactMemebers(String subscriberId, String callerId,
			String callerName, String preGroupId, String groupId, String mode) {
		String response = subscriberManager.addMultipleContactMemebers(subscriberId, callerId, callerName, preGroupId, groupId, mode);

		return response;
	}

	@Override
	public String getAllContactMemebers(String subscriberId, String groupId,String mode) {
		List<ExtendedGroupBean> groupDetails = subscriberManager.getAllContactMemebers(subscriberId, groupId, mode);
		return (ObjectGsonUtils.objectToGson(groupDetails));
	}

	@Override
	public String getAllContactsFromAllGroups(String subscriberId, String mode) {
		List<GroupMember> groupsMembers = subscriberManager.getAllContactsFromAllGroups(subscriberId, mode);
		return (ObjectGsonUtils.objectToGson(groupsMembers));
	}

	@Override
	public String removeContactMemebers(String subscriberId, String callerId,
			String predefinedGroupId, String groupId, String mode) {
		String response = subscriberManager.removeContactMemebers(subscriberId, callerId, predefinedGroupId, groupId, mode);
		return response;
	}

	@Override
	public String removeMultipleContactMemebers(String subscriberId, String callerId,
			String predefinedGroupId, String groupId, String mode) {
		String response = subscriberManager.removeMultipleContactMemebers(subscriberId, callerId, predefinedGroupId, groupId, mode);
		return response;
	}

	@Override
	public String moveContactMemebers(String subscriberId, String callerId,
			String sourcePreGroupId, String sourceGroupId, String destPreGroupId, String destGroupId, String mode) {
		String response = subscriberManager.moveContactMemebers(subscriberId, callerId, sourcePreGroupId, sourceGroupId, destPreGroupId, destGroupId, mode);
		return response;
	}

	@Override
	public String getNotificationStatus(String subscriberId, String os_type, String regId) {
		String response = subscriberManager.getNotificationStatus(subscriberId, os_type, regId);
		return response;
	}

	@Override
	public String setNotificationStatus(String subscriberId, Boolean status, String os_type, String regId) {
		String response = subscriberManager.setNotificationStatus(subscriberId, status, os_type, regId);
		return response;
	}

	@Override
	public String getOffer(String subscriberId, String clipId,
			String offerType, String mode ,String browsingLangauge , String appName) {
		OfferBean offerBean = subscriberManager.getOffer(subscriberId, clipId, offerType, mode, browsingLangauge ,  appName);
		if (offerBean == null) {
			logger.info("No offers found. Returning dummy offerBean with offerId set as -1. subscriberId: " 
					+ subscriberId + ", clipId: " + clipId + ", offerType: " + offerType + ", mode: " + mode);
			offerBean = new OfferBean();
			offerBean.setSubscriberId(subscriberId);
			offerBean.setOfferId("-1");
			offerBean.setDescription(PropertyConfigurator.getOfferDescription(offerType, "DEFAULT",browsingLangauge ,appName));
		}
		return ObjectGsonUtils.objectToGson(offerBean);
	}

	@Override
	public String getSelectionDetails(String subscriberId, String clipId,
			String categoryId, String mode, String chargeClass,
			String isMusicPack, String isProfilePack, boolean useUIChargeClass) {
		SelectionDetailsBean selectionDetailsBean = null;
		try {
			selectionDetailsBean = subscriberManager.getSelectionDetails(subscriberId, clipId, categoryId, mode, chargeClass, isMusicPack, isProfilePack, useUIChargeClass);
		} catch (Exception e) {
			logger.error("Error in getSelectionDetails: " + e, e);
		}
		return  ObjectGsonUtils.objectToGson(selectionDetailsBean);
	}

	@Override
	public String getCurrentPlayingSong(String subscriberId, String callerId,
			String browsingLanguage, String appName, String mode,
			String callType, StringBuffer responseCode, boolean addCatObj) {
		GetCurrentPlayingSongBean currentPlaylingSongBean = null;
		ExtendedClipBean extClip = null;
		String reponse = null;
		try {
			if (addCatObj) {
				currentPlaylingSongBean = subscriberManager
						.getCurrentPlayingSong(subscriberId, callerId,
								browsingLanguage, appName, mode, callType,
								responseCode);
				if (currentPlaylingSongBean == null) {
					return "FAILURE";
				}
				reponse = ObjectGsonUtils.objectToGson(currentPlaylingSongBean);
			} else {
				extClip = subscriberManager.getCurrentPlayingSongOnly(
						subscriberId, callerId, browsingLanguage, appName,
						mode,callType,responseCode);
				if (extClip == null) {
					return "FAILURE";
				}
				reponse = ObjectGsonUtils.objectToGson(extClip);
			}

		} catch (Throwable t) {
			logger.error("Error in getCurrentPlayingSong: " + t, t);
		}
		return reponse;
	}

	@Override
	public String getDownloadsWithSelections(String subscriberId) {
		List<ExtendedDownloadBean> downloadBeanList = null;
		try {
			downloadBeanList = subscriberManager.getDownloadsWithSelections(subscriberId);
		} catch (Throwable t) {
			logger.error("Error in getDownloadsWithSelections: " + t, t);
			return "FAILURE";
		}
		return ObjectGsonUtils.objectToGson(downloadBeanList);
	}

	@Override
	public String getParamForType(String type, String param) {
		Map<String, String> paramMap = new HashMap<String, String>();
		try {
			paramMap = subscriberManager.getParamForType(type, param);
		} catch (Throwable t) {
			logger.error("Error in getParamForType: " + t, t);
		}
		return ObjectGsonUtils.objectToGson(paramMap);
	}

	@Override
	public String refreshAllParamsInMemcache() {
		Boolean isUpdated = false;
		Map<String,Boolean> dummyMap = new HashMap<String,Boolean>();
		try {
			isUpdated = subscriberManager.refreshAllParamsInMemcache();
		} catch (Throwable t) {
			logger.error("Error in paramRefresh: " + t, t);
		}
		dummyMap.put("isUpdated", isUpdated);
		return ObjectGsonUtils.objectToGson(dummyMap);
	}	
	
	//RBT-14626	Signal app requirement - Mobile app server API enhancement (phase 2)
	@Override
	public String removeGroup(String subscriberId, String groupId, String mode) {
		Map<String,String> dummyMap = new HashMap<String,String>();
		String response = subscriberManager.removeGroup(subscriberId, groupId, mode);
		if(response.contains("success") || response.contains("SUCCESS")){
			response = "success";
		}else{
			response = "failure";
		}
		dummyMap.put("response", response);
		return ObjectGsonUtils.objectToGson(dummyMap);
	}

	//RBT-14626	Signal app requirement - Mobile app server API enhancement (phase 2)
	@Override
	public String getCallLogHistory(String subscriberId, String mode,
			String callType, String pageSize, String offset) {
		Map<String,String> dummyMap = new HashMap<String,String>();
		return subscriberManager.getCallLogHistory(subscriberId, mode, callType,pageSize, offset);
	}
	
	
	public String removeSubscriberDownload(String subscriberId, String clipId, String caller, String channel, String fromTime,
			String toTime, String circleId, String refID, String catId, String fromTimeMinutes,
			String toTimeMinutes) {
		String retString = subscriberManager.removeSubscriberDownload(subscriberId, clipId, caller, channel, fromTime,
				toTime, circleId, refID, catId,fromTimeMinutes,toTimeMinutes);
		return retString;
	}
}
