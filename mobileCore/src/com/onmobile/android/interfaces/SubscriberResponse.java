package com.onmobile.android.interfaces;

import com.onmobile.android.utils.StringConstants;

public interface SubscriberResponse {
	public final String CHANNEL = StringConstants.CHANNEL;

	public String addSubscriberDownload(String subscriberId, String clipId,
			String categoryId, String channel, String isMusicPack);

	public String addSubscriberBookMark(String subscriberId, String clipId,
			String categoryId, String channel);

	public String removeSubscriberBookMark(String subscriberId, String clipId,
			String categoryId, String channel);

	public String getMSISDN(String UID, String reqMsisdn, String circleId, boolean getCircleId, String registrationsource, String userAgent);

	public String giftSong(String subscriberId, String contentId,
			String giftee, String channel, StringBuffer response,
			String chargeClass, String categoryId);

	public String activate_Subscriber(String subscriberId, String channel, String subscriptionClass, String baseOfferId);

	public String deactivate_Subscriber(String subscriberId, String channel);

	public String setSubscriberSelection(String subscriberId, String caller,
			String clipId, String categoryId, String channel,
			String chargeClass, String musicPack, String inLoop, boolean isConsent, String subscriptionClass, String baseOfferId, String selOfferId, boolean useUIChargeClass,String smOfferType,String circleID,boolean isUdsOptIn);

	public String getSelectionAmount(String subscriberId, String clipId,
			String categoryId, String channel, String chargeClass,
			String musicPack, String profilePack, boolean useUIChargeClass);

	public String getGiftAmount(String subscriberId, String clipId,
			String categoryId, String channel, String chargeClass);

	public String setSubscriberProfileSelection(String subscriberId,
			String caller, String clipId, String categoryId, String channel,
			String selectionStartTime, String selectionEndTime, String rbtWavFile, boolean isConsent, 
			String subscriptionClass, String baseOfferId, String chargeClass, String selOfferId, String profileHours, boolean useUIChargeClass, String smOfferType);

	public String getListDownloads(String subscriberId);

	public String getListBookmarks(String subscriberId);

	public String getListSelections(String subscriberId, boolean isDownloadsMerged, String browsingLangauge, String appName, String circleId);

	public String removeSubscriberSelection(String subscriberId, String clipId,
			String caller, String channel, String selStartTime, String selEndTime, String rbtWavFile, String circleId);

	public String removeSubscriberDownload(String subId, String clipId,
			String categoryId, String channel);

	public String getCountOfAvailableMusicPackDownloads(String subId);

	public String getSubscriberStatus(String subId);
	
	public String getVoluntarySuspendedStatus(String subId);
	
	public String processSuspension(String subId, boolean suspend);

	public String getSubscriberInfo(String subId, String uid, String circleId);

	public String getGiftInbox(String subId);

	public String getGiftInboxCount(String subId);

	public String rejectGift(String gifterId, String gifteeId,
			String giftSentTime);

	public String getGroups(String subscriberId);

	public String getCopySelections(String copyMobileNumber, String subscriberId);

	public String setSubscriberSelection(String subId, String callerId,
			String clipId, String catId, Integer status, boolean b,
			String mode, String modeInfo, String lang, Integer startTimeHrs,
			Integer startTimeMins, Integer endTimeHrs, Integer endTimeMins,
			String interval, Integer cosId, String gifterId, String giftSentTime, String selStartTime, String selEndTime, String inLoop, boolean isConsent
			, String subscriptionClass, String baseOfferId, String chargeClass, String selOfferId, boolean useUIChargeClass,String smOfferType);

	public String getPredefinedGroups();

	public String isProfilePackActivated(String subId);

	public String isPackActivated(String subId);

	public String deactivateProfilePack(String subscriberId);
	
	public String deactivateMusicPack(String subscriberId);

	public String subscribeUser(String subscriberId, String type,
			String password, Boolean isResetPassword, String uid,
			String appName, boolean getCircleId, String registrationsource,
			String userAgent);

	public String getListProfileSelections(String subId);

	public String getParameterValue(String param);

	public String addGCMRegistration(String regId, String subID, String os_type);
	public String removeGCMRegistration(String regId, String subID);
	
	public String addContactMemebers(String subscriberId, String callerId, String callerName, String predefinedGroupId, String groupId, String mode);
	public String addMultipleContactMemebers(String subscriberId, String callerId, String callerName, String predefinedGroupId, String groupId, String mode);
	public String getAllContactMemebers(String subscriberId, String predefinedGroupId, String mode);
	public String removeContactMemebers(String subscriberId, String callerId, String predefinedGroupId, String groupId, String mode);
	public String removeMultipleContactMemebers(String subscriberId, String callerId, String predefinedGroupId, String groupId, String mode);
	public String moveContactMemebers(String subscriberId, String callerId, String sourcePreGroupId, String sourceGroupId, String destPreGroupId, String destGroupId, String mode);

	/**
	 * To get all the contacts from all groups. The response list would be sorted in ascending order of contact names.
	 */
	public String getAllContactsFromAllGroups(String subscriberId, String mode);
	
	/**
	 * To get the current notification status for a subscriberId-osType combination
	 * 
	 */
	public String getNotificationStatus(String subscriberId, String os_type, String regId);
	
	/**
	 * To set the notification status for a subscriberId-osType combination
	 */
	public String setNotificationStatus(String subscriberId, Boolean status, String os_type, String regId);

	/**
	 * To get Offer details
	 * @param subscriberId
	 * @param clipId
	 * @param offerType
	 * @param mode
	 * @return
	 */
	public String getOffer(String subscriberId, String clipId,
			String offerType, String mode,String browsingLangauge ,String appName);

	/**
	 * To get the selection details.
	 * @param subId
	 * @param clipId
	 * @param categoryId
	 * @param mode
	 * @param chargeClass
	 * @param isMusicPack
	 * @param isProfilePack
	 * @param useUIChargeClass 
	 * @return
	 */
	public String getSelectionDetails(String subId, String clipId,
			String categoryId, String mode, String chargeClass,
			String isMusicPack, String isProfilePack, boolean useUIChargeClass);

	/**
	 * RBT-13660: Real time tone playing information to be shown
	 * 
	 * @param subscriberId
	 * @param callerId
	 * @param browsingLanguage
	 * @param appName
	 * @param mode
	 * @param responseCode
	 * @param addCatObj
	 * @return
	 */
	public String getCurrentPlayingSong(String subscriberId, String callerId,
			String browsingLanguage, String appName, String mode,
			String callType, StringBuffer responseCode, boolean addCatObj);

	/**
	 * 
	 * @param subscriberId
	 * @return
	 */
	public String getDownloadsWithSelections(String subscriberId);

	/**
	 * 
	 * @param type
	 * @param param
	 * @return
	 */
	public String getParamForType(String type, String param);

	public String refreshAllParamsInMemcache();

	//RBT-14626	Signal app requirement - Mobile app server API enhancement (phase 2)
	public String removeGroup(String subscriberId,String groupId, String mode);
	
	public String getCallLogHistory(String subscriberId, String mode, String callType, String pageSize, String offset);

	public String removeSubscriberDownload(String subId, String clipId, String caller, String mode, String selStartTime,
			String selEndTime, String circleId, String refId , String catId ,String fromTimeMinutes,
			String toTimeMinutes);
}
