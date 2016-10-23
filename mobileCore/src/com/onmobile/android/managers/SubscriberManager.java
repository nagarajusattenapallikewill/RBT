package com.onmobile.android.managers;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.ConsentProcessBean;
import com.onmobile.android.beans.ExtendedClipBean;
import com.onmobile.android.beans.ExtendedDownloadBean;
import com.onmobile.android.beans.ExtendedGiftBean;
import com.onmobile.android.beans.ExtendedGroupBean;
import com.onmobile.android.beans.ExtendedSelectionBean;
import com.onmobile.android.beans.ExtendedSubscriberBean;
import com.onmobile.android.beans.ExtendedSubscriberIdRespBean;
import com.onmobile.android.beans.GetCurrentPlayingSongBean;
import com.onmobile.android.beans.OfferBean;
import com.onmobile.android.beans.SelectionDetailsBean;
import com.onmobile.android.beans.SubscriptionDetailsBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.CategoryUtils;
import com.onmobile.android.utils.ClipUtils;
import com.onmobile.android.utils.CurrentPlayingSongUtility;
import com.onmobile.android.utils.ParamForTypeUtils;
import com.onmobile.android.utils.StringConstants;
import com.onmobile.android.utils.Utility;
import com.onmobile.android.utils.comparator.ExtendedGroupBeanNameComparator;
import com.onmobile.android.utils.comparator.GroupMemberNameComparator;
import com.onmobile.android.utils.comparator.PredefinedGroupNameComparator;
import com.onmobile.android.utils.consent.AirtelConsentUtility;
import com.onmobile.android.utils.consent.ConsentUtility;
import com.onmobile.android.utils.consent.ConsentUtilityFactory;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Bookmark;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Bookmarks;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ComvivaConsent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Cos;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Gift;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GiftInbox;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Group;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PredefinedGroup;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RBTLoginUser;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPack;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.BookmarkRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CallLogRestRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GroupRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongWSResponseBean;

public class SubscriberManager implements StringConstants{

	private static Logger logger = Logger.getLogger(SubscriberManager.class);
	private static RBTClient client = RBTClient.getInstance();
	private static RBTCacheManager cacheManager = RBTCacheManager.getInstance();
	private ContentManager contentManager = new ContentManager();
	private static SimpleDateFormat giftDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	
	public ExtendedSubscriberIdRespBean getSubscriberMSISDN(String UID,
			String reqMsisdn, String circleId, String registrationsource, String userAgent) {
		ExtendedSubscriberIdRespBean respBean = null;
		
		ApplicationDetailsRequest request = new ApplicationDetailsRequest();
		if (Utility.isStringValid(UID)) {
			request.setUserID(UID);
		} 
		request.setSubscriberID(reqMsisdn);	
		request.setCircleID(circleId);
		request.setType(USER_TYPE);
		RBTLoginUser loginUser = null;

		if(reqMsisdn != null) {
			request.setNewUserID(UID);
			request.setMode("MOBILEAPP");
			request.setDoSubscriberValidation(true);
			HashMap<String, String> userInfoMap = new HashMap<String, String>();
			userInfoMap.put("skipSendingSMS", "true");
			request.setUserInfo(userInfoMap);
			loginUser = client.setRBTLoginUser(request);
			// This logs has  to be written only for the first time registering users.
			// we need to check how do we get the user is first time registering user.
			if (null != loginUser && loginUser.getNewUser()) {
				Utility.writeRegistrationLogs(circleId,
						loginUser.getSubscriberID(), registrationsource, userAgent);
			}
		} else {
			loginUser = client.getRBTLoginUser(request);
		}

		if (!request.getResponse().equalsIgnoreCase("SUCCESS")) {
			logger.error("Login failed. Reason: " + request.getResponse());
			String rbtResponse = request.getResponse().toLowerCase();
			String response = PropertyConfigurator.getResponseForGetSubscriberIdErrorCase(rbtResponse);
			if (response == null) {
				logger.debug("UID: " + UID + ", subscriberId: " + reqMsisdn + ". No configuration found for webservice response: " + rbtResponse);
				response = rbtResponse;
			}
			respBean = new ExtendedSubscriberIdRespBean(response, null, null);
			return respBean;
		}
		if (loginUser != null) {
			if (circleId == null) {
				circleId = Utility.getCircleId(loginUser.getSubscriberID());
			}
			respBean = new ExtendedSubscriberIdRespBean(request.getResponse(), loginUser.getSubscriberID(), circleId);
		}
		return respBean;
	}

	public boolean getSubscriberStatus(String subId) {
		boolean subscribed = false;
		String response = null;
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subId,null);
		Subscriber subscriber = client.getSubscriber(rbtRequest);
		if(subscriber!=null){
			response = subscriber.getStatus();
		}

		logger.info("subscriber status response:" + response);
		if(response!=null &&( !response.equalsIgnoreCase("active") && !response.equalsIgnoreCase("act_pending") 
				&& !response.equalsIgnoreCase("grace") && (response.equalsIgnoreCase("new_user") || response.equalsIgnoreCase("deact_pending") ) )){
			subscribed = false;
		}else if(response!=null && response.equalsIgnoreCase("deactive")){
			subscribed = false;
		}else{
			subscribed = true;
		}
		logger.info("isSubscribed: " + subscribed );
		return subscribed;
	}

	public boolean getVoluntarySuspendedStatus(String subId) {
		boolean isSuspended = false;
		String status = null;
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subId,null);
		Subscriber subscriber = client.getSubscriber(rbtRequest);
		if (subscriber!=null) {
			status = subscriber.getStatus();
		}

		Map<String, String> extraInfo = subscriber.getUserInfoMap();
		logger.info("subscriber status response:" + status);
		if (extraInfo != null
				&& extraInfo.containsKey(iRBTConstant.VOLUNTARY)
				&& extraInfo.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase(
						"TRUE")) {
			isSuspended = true;
		}

		logger.info("isSuspended: " + isSuspended );
		return isSuspended;
	}

	public String processSuspension(String subId, boolean suspend) {
		UtilsRequest utilsRequest = new UtilsRequest(subId, suspend, "MOBILEAPP");
		client.suspension(utilsRequest);

		return utilsRequest.getResponse();
	}

	public ExtendedSubscriberBean getSubscriberInfo(String subId, String uid, String circleId) {
		logger.debug("Inside getSubscriberInfo. subId: " + subId + ", Uid: "
				+ uid + ", CircleId: " + circleId);
		Subscriber subscriber = null;
		Rbt rbt = null;
		Library library = null;
		Settings settings = null;
		ExtendedSubscriberBean extendedSubscriber = null;
		if (subId != null) {
			logger.debug("subId: "
					+ subId
					+ ", Uid: "
					+ uid
					+ ". SubscriberId present in the request. Retrieving subscriber info..");
			RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subId, null);
			rbtRequest.setInfo(WebServiceConstants.SUBSCRIBER + ","
					+ WebServiceConstants.LIBRARY);
			rbtRequest.setCircleID(circleId);
			rbt = client.getRBTUserInformation(rbtRequest);
		} else if (uid != null) {
			ApplicationDetailsRequest request = new ApplicationDetailsRequest();
			request.setUserID(uid);
			request.setType(USER_TYPE);
			RBTLoginUser loginUser = client.getRBTLoginUser(request);
			if (loginUser == null) {
				logger.info("Uid: " + uid
						+ ". User not registered. Returning null.");
				return null;
			}
			logger.debug("Uid: " + uid
					+ ". Retrieving subscriber details now..");
			RbtDetailsRequest rbtRequest = new RbtDetailsRequest(
					loginUser.getSubscriberID(), null);
			rbtRequest.setInfo(WebServiceConstants.SUBSCRIBER + ","
					+ WebServiceConstants.LIBRARY);
			rbtRequest.setCircleID(circleId);
			rbt = client.getRBTUserInformation(rbtRequest);
		}
		int noOfSpecialSettings = 0;
		if (rbt != null) {
			subscriber = rbt.getSubscriber();
			library = rbt.getLibrary();
		}
		settings = library.getSettings();
		if (settings != null) {
			Setting[] setting = settings.getSettings();
			if(setting != null){
			for (int i = 0; i < setting.length; i++) {

				if (!(setting[i].getStatus() == 1 || setting[i].getStatus() == 99)
						|| !(setting[i].getCallerID() == null || setting[i].getCallerID().equalsIgnoreCase("all"))) {
					noOfSpecialSettings++;
				}
			}
		  }
		}

		

		if (subscriber != null && subscriber.getSubscriptionClass() != null) {
			ApplicationDetailsRequest appreq = new ApplicationDetailsRequest();
			appreq.setName(subscriber.getSubscriptionClass());
			SubscriptionClass subclass = client.getSubscriptionClass(appreq);
			logger.info("The subscription class is " + subclass);
			if (subclass != null && subclass.getAmount() != null) {
				subscriber.setChargeDetails(subclass.getAmount());
			}

		}
		
		extendedSubscriber = new ExtendedSubscriberBean(subscriber,
				noOfSpecialSettings);
		logger.info("Returning subscriber: " + extendedSubscriber);
		return extendedSubscriber;
	}

	public boolean isProfilePackActivated(String subId) {
		boolean isProfilePackActivated = false;
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subId,null);
		rbtRequest.setInfo(WebServiceConstants.SUBSCRIBER_PACKS);
		Rbt rbt = client.getRBTUserInformation(rbtRequest);
		if(rbt!=null){
			SubscriberPack[] packs = rbt.getSubscriberPacks();
			for(int i=0; packs!=null && i<packs.length; i++){
				String packCostType = packs[i].getCosType();
				if(packCostType!=null && packCostType.equalsIgnoreCase("PROFILE")){
					isProfilePackActivated = true;
					break;
				}
			}
		}
		return isProfilePackActivated;
	}

	public boolean isPackActivated(String subId) {
		boolean isPackActivated = false;
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subId, null);
		rbtRequest.setInfo(WebServiceConstants.SUBSCRIBER_PACKS);
		Rbt rbt = client.getRBTUserInformation(rbtRequest);
		if (rbt != null) {
			SubscriberPack[] packs = rbt.getSubscriberPacks();
			for (int i = 0; packs != null && i < packs.length; i++) {
				String packCosType = packs[i].getCosType();
				if (packCosType != null
						&& (packCosType.equalsIgnoreCase(iRBTConstant.LIMITED_DOWNLOADS)
								|| packCosType.equalsIgnoreCase(iRBTConstant.UNLIMITED_DOWNLOADS)
								|| packCosType.equalsIgnoreCase(iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE)
								|| packCosType.equalsIgnoreCase(iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)
								|| packCosType.equalsIgnoreCase(iRBTConstant.SONG_PACK))) {
					isPackActivated = true;
					break;
				}
			}
		}
		return isPackActivated;
	}

	public String addSubscriberDownload(String subscriberId, String clipId, String categoryId, String channel, String musicPack) {
		String retString = null;
		SelectionRequest selRequest=new SelectionRequest(subscriberId, null, categoryId, clipId, null, null);
		selRequest.setMode(channel);
		selRequest.setModeInfo(channel);
		// RBT-6497:-Handset Client- First song download via app for free
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subscriberId,null);
		Subscriber subscriber = client.getSubscriber(rbtRequest);
		HashMap<String, String> extraInfoMap =  (subscriber.getUserInfoMap() == null) ? new HashMap<String, String>() : subscriber.getUserInfoMap();

		if(PropertyConfigurator.getFirstFreeSelModeList().contains(channel) && !extraInfoMap.containsKey("MOBILE_APP_FREE")) {
			extraInfoMap.put("MOBILE_APP_FREE", Boolean.TRUE.toString());
			selRequest.setUserInfoMap(extraInfoMap);
			selRequest.setUseUIChargeClass(true);
			selRequest.setChargeClass(PropertyConfigurator.getFirstFreeChargeClass());
		}

		try {
			boolean isMusicPack = Boolean.parseBoolean(musicPack);
			if (isMusicPack) {
				selRequest.setCosID(Integer.parseInt(PropertyConfigurator
						.getMusicPackCosId()));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		client.addSubscriberDownload(selRequest);
		retString=selRequest.getResponse();
		return retString;
	}

	public List<ExtendedDownloadBean>  getDownloads(String subscriberId){
		RbtDetailsRequest detailRequest=new RbtDetailsRequest(subscriberId, null);
		Library library = client.getLibrary(detailRequest);
		Downloads download = null;
		Download[] downloads = null;
		List<ExtendedDownloadBean> downloadList =  new ArrayList<ExtendedDownloadBean>();
		if(library!=null){
			download = library.getDownloads();
			if(download!=null){
				downloads=download.getDownloads();

				for(int i=0; i < downloads.length; i++) {
					ExtendedClipBean clipObj = ClipUtils.getExtendedClipByClipId(String.valueOf(downloads[i].getToneID()), null, null);
					ExtendedDownloadBean extDownloadBean = new ExtendedDownloadBean(downloads[i]);
					extDownloadBean.setAlbum(clipObj.getAlbum());
					extDownloadBean.setArtist(clipObj.getArtist());
					extDownloadBean.setImageFilePath(clipObj.getClipInfo(Clip.ClipInfoKeys.IMG_URL));
					extDownloadBean.setClipRbtWavFile(clipObj.getClipRbtWavFile());
					downloadList.add(extDownloadBean);
				}
				//super class
				//List<Download> downloadList =  Arrays.asList(downloads);
				//List<? super ExtendedDownloadBean> extDownloadBeanList = downloadList;
				//Iterator<? super ExtendedDownloadBean> itrBeanObj = extDownloadBeanList.iterator();
				/*for(int i=0; i < downloads.length; i++){
				ExtendedClipBean clipObj = contentManager.getExtendedClipByClipId(String.valueOf(downloads[i].getToneID()));
				 while (itrBeanObj.hasNext()) {
					 ExtendedDownloadBean extDownloadBean = (ExtendedDownloadBean) itrBeanObj.next();
					 if(extDownloadBean.getToneID() == downloads[i].getToneID()) {
						 extDownloadBean.setAlbum(clipObj.getAlbum());
						 extDownloadBean.setArtist(clipObj.getArtist());
						 extDownloadBean.setImageFilePath(clipObj.getClipInfo(Clip.ClipInfoKeys.IMG_URL));
						 extDownloadBean.setClipRbtWavFile(clipObj.getClipRbtWavFile());
					 }

				 }
				}*/
			}
		}
		return  downloadList;
	}

	public String removeSubscriberDownload(String subId, String clipId, String categoryId, String channel) {
		String retString;
		SelectionRequest selectionRequest = new SelectionRequest(subId);
		selectionRequest.setCategoryID(categoryId);
		if (clipId != null
				&& (clipId.endsWith(".3gp") || clipId.endsWith(".wav") || clipId
						.contains("_"))) {
			selectionRequest.setRbtFile(clipId);
		} else {
			selectionRequest.setClipID(clipId);
		}
		selectionRequest.setMode(channel);
		client.deleteSubscriberDownload(selectionRequest);
		retString = selectionRequest.getResponse();
		return retString;
	}

	public String getCountOfAvailableMusicPackDownloads(String subId)
	{
		String retString = "0";
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subId,null);
		Subscriber subscriber = client.getSubscriber(rbtRequest);
		if(subscriber == null)
			return retString;
		String cosId = subscriber.getCosID();
		int maxSelections = subscriber.getTotalDownloads();
		if(cosId == null)
			return retString;
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest(subscriber.getCircleID(), Integer.parseInt(subscriber.getCosID()));
		applicationDetailsRequest.setIsPrepaid(subscriber.isPrepaid());
		Cos subCos = client.getCos(applicationDetailsRequest);
		if(subCos == null || subCos.getChargeClass() == null || subCos.getChargeClass().trim().length() == 0 || subCos.isDefault())
			return retString;
		String cosChargeClasses = subCos.getChargeClass();
		StringTokenizer stk = new StringTokenizer(cosChargeClasses, ",");
		int totalCount = 0;
		while(stk.hasMoreTokens())
		{
			String token = stk.nextToken().trim();
			StringTokenizer innerTokenizer = new StringTokenizer(token, "*");

			if(innerTokenizer.countTokens() == 1)
			{
				totalCount++;
				continue;
			}
			else if(innerTokenizer.countTokens() == 2)
			{
				innerTokenizer.nextToken(); // this is charge class;
				try
				{
					totalCount += Integer.parseInt(innerTokenizer.nextToken().trim());
				}
				catch(NumberFormatException nfe)
				{
					logger.error(nfe);
				}
			}
		}
		totalCount = totalCount*subCos.getNoOfFreeSongs();
		logger.debug("totalCount: " + totalCount + ", maxSelections: " + maxSelections + ", totalCount: " + totalCount);
		int availableDownloads = 0;
		if (totalCount != 0) {
			availableDownloads = totalCount - (maxSelections % totalCount);
		} else {
			logger.warn("totalCount is zero");
		}
		if(availableDownloads < 0)
			availableDownloads = 0;
		return String.valueOf(availableDownloads) ;
	}

	public  String addSubscriberBookMark(String subscriberId, String clipId, String categoryId, String channel){
		String retString=null;
		BookmarkRequest bookmark=new BookmarkRequest(subscriberId, Integer.parseInt(clipId), Integer.parseInt(categoryId));
		bookmark.setMode(channel);

		client.addBookmark(bookmark);
		retString=bookmark.getResponse();
		return retString;
	}

	public  String removeSubscriberBookMark(String subscriberId, String clipId, String categoryId, String channel){
		String retString=null;
		BookmarkRequest bookmark=new BookmarkRequest(subscriberId, Integer.parseInt(clipId), Integer.parseInt(categoryId));
		bookmark.setMode(channel);
		client.removeBookmark(bookmark);
		retString=bookmark.getResponse();
		return retString;
	}

	public List<ExtendedClipBean> getBookmarks(String subscriberId){
		logger.info("inside getBookmarks");
		List<ExtendedClipBean> extendedBookmarkedClips = new ArrayList<ExtendedClipBean>();
		RbtDetailsRequest detailRequest=new RbtDetailsRequest(subscriberId, null);
		Bookmarks bookmarks=client.getBookmarks(detailRequest);
		if(bookmarks!=null){
			logger.info("bookmarks != null");
			Bookmark bookmarksArray[] = bookmarks.getBookmarks();
			List<Integer> bookmarkedIntClipIds = new ArrayList<Integer>();
			if(bookmarksArray!=null && bookmarksArray.length!=0){
				logger.info("bookmarks array length: " + bookmarksArray.length);
				String[] bookmarkedStrClipIds = new String[bookmarksArray.length]; 
				for(int i=0;i<bookmarksArray.length; i++){
					bookmarkedIntClipIds.add(bookmarksArray[i].getToneID());
					bookmarkedStrClipIds[i] = String.valueOf(bookmarksArray[i].getToneID());
				}
				Clip[] bookmarkedClips = cacheManager.getClips(bookmarkedStrClipIds);

				List<ClipRating> bookmarkedClipRatings = contentManager.getClipRatings(bookmarkedIntClipIds);
				if(bookmarkedClipRatings!=null && bookmarkedClips!=null && bookmarkedClipRatings.size()==bookmarkedClips.length){
					logger.info("bookmarkedClipRatings and bookmarkedClips size: " + bookmarkedClips.length);
					for(int i=0; i<bookmarkedClips.length; i++){
						ExtendedClipBean extendedBookmarkedClip = new ExtendedClipBean(bookmarkedClips[i],bookmarkedClipRatings.get(i));
						extendedBookmarkedClips.add(extendedBookmarkedClip);
					}
				}
				else {
					logger.info("Clip rating not obtained just obtaing the clip without rating");
					for(int i=0; i<bookmarkedClips.length; i++){
						ExtendedClipBean extendedBookmarkedClip = new ExtendedClipBean(bookmarkedClips[i],null);
						extendedBookmarkedClips.add(extendedBookmarkedClip);
					}
				}
			}
		}
		return extendedBookmarkedClips;
	}



	public String addGift(String subscriberId, String contentId, String giftee, String channel, StringBuffer response, String chargeClass, String categoryId){
		logger.info("subscriberId: " + subscriberId + ", contentId: "
				+ contentId + ", giftee: " + giftee + ", channel: " + channel
				+ ", chargeClass: " + chargeClass + ", categoryId: "
				+ categoryId);
		GiftRequest giftRequest = null;
		if(!Utility.isStringValid(contentId) && !Utility.isStringValid(categoryId)){
			logger.info("Both contentId and categoryId are not present.");
			giftRequest=new GiftRequest(subscriberId,giftee, null, channel);
		} else if (Utility.isStringValid(categoryId)){
			logger.info("categoryId present.");
			giftRequest=new GiftRequest(subscriberId, giftee, null, contentId, categoryId, channel);
		} else {
			logger.info("categoryId not present.");
			giftRequest=new GiftRequest(subscriberId, giftee, null, contentId, null, channel);
		}
		giftRequest.setIsPostMethod(true);
		client.sendGift(giftRequest);
		logger.info("Gift response "+giftRequest.getResponse());
		return giftRequest.getResponse();
	}

	public String activateSubscriber(String subscriberId, String channel, String subscriptionClass, String baseOfferId) throws Exception{
		SubscriptionRequest subRequest=new SubscriptionRequest(subscriberId,new Boolean(true),null,null,channel);
		subRequest.setModeInfo(channel);
		if (Utility.isStringValid(baseOfferId) && Utility.isStringValid(subscriptionClass)) {
			logger.debug("Valid baseOfferId: " + baseOfferId + " and subscriptionClass: " + subscriptionClass);
			subRequest.setOfferID(baseOfferId);
			subRequest.setSubscriptionClass(subscriptionClass);
		} else if (Utility.isStringValid(subscriptionClass)) {
			subRequest.setSubscriptionClass(subscriptionClass);
		}
		client.activateSubscriber(subRequest);
		logger.info("subRequest response: "+subRequest.getResponse());
		return subRequest.getResponse();
	}

	public String activateSubscriber(String subscriberId,Integer cosId, String channel) throws Exception{
		SubscriptionRequest subRequest=new SubscriptionRequest(subscriberId,new Boolean(true),null,null,channel);
		subRequest.setCosID(cosId);
		subRequest.setModeInfo(channel);
		client.activateSubscriber(subRequest);
		logger.info("subRequest response: "+subRequest.getResponse());
		return subRequest.getResponse();
	}

	public String deactivateSubscriber(String subscriberId, String channel) throws Exception{
		SubscriptionRequest unsubRequest=new SubscriptionRequest(subscriberId,new Boolean(true),null,null,channel);
		client.deactivateSubscriber(unsubRequest);
		logger.info("unsubRequest response:"+unsubRequest.getResponse());
		return unsubRequest.getResponse();
	}

	public String addSubscriberSelection(final String subscriberId,
			final String caller, final String clipId, String categoryId, String channel, String chargeClass, String musicPack, String inLoop, boolean isConsent, 
			String subscriptionClass, String baseOfferId, String selOfferId, boolean useUIChargeClass,String smOfferType,String circleID , boolean isUdsOptIn) throws Exception{
		SelectionRequest selectionRequest = new SelectionRequest(subscriberId,new Boolean(true),caller,channel,categoryId,clipId,null,null);
		selectionRequest.setModeInfo(channel);
		selectionRequest.setUseUIChargeClass(useUIChargeClass);
		selectionRequest.setCircleID(circleID);
		if (smOfferType != null) {
			HashMap<String, String> selInfoMap = new HashMap<String, String>();
			selInfoMap.put("OFFER_TYPE", smOfferType);
			selectionRequest.setSelectionInfoMap(selInfoMap);
		}

		//Consent param A R changes
		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		Subscriber subscriber = null;
		Configurations conf = new Configurations();
		boolean isComvivaCircle = conf.getCvCircleId() != null && conf.getCvCircleId().contains(circleID.toUpperCase());
			if(!isComvivaCircle){
			// RBT-6497:-Handset Client- First song download via app for free
			RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subscriberId,null);
			subscriber = client.getSubscriber(rbtRequest);
			if(subscriber.getUserInfoMap() != null)
				extraInfoMap = subscriber.getUserInfoMap();
		}
		if(isUdsOptIn){
		extraInfoMap.put("UDS_OPTIN", isUdsOptIn+"");
		}
		if (Utility.isStringValid(baseOfferId) && Utility.isStringValid(subscriptionClass)) {
			logger.debug("Valid baseOfferId: " + baseOfferId + " and subscriptionClass: " + subscriptionClass);
			selectionRequest.setSubscriptionOfferID(baseOfferId);
			selectionRequest.setSubscriptionClass(subscriptionClass);
		} else if (Utility.isStringValid(subscriptionClass)) {
			selectionRequest.setSubscriptionClass(subscriptionClass);
		}

		if (Utility.isStringValid(selOfferId) && Utility.isStringValid(chargeClass)) { //This has the highest priority
			logger.debug("Valid selOfferId: " + selOfferId + " and chargeClass: " + chargeClass);
			selectionRequest.setOfferID(selOfferId);
			selectionRequest.setChargeClass(chargeClass);
			selectionRequest.setUseUIChargeClass(true);
		} else if(PropertyConfigurator.getFirstFreeSelModeList().contains(channel) && extraInfoMap != null && !extraInfoMap.containsKey("MOBILE_APP_FREE")) {
			extraInfoMap.put("MOBILE_APP_FREE", Boolean.TRUE.toString());
			selectionRequest.setUseUIChargeClass(true);
			selectionRequest.setChargeClass(PropertyConfigurator.getFirstFreeChargeClass());
		} else {
			selectionRequest.setChargeClass(chargeClass);
		}
		if(extraInfoMap != null && !extraInfoMap.isEmpty()){
		selectionRequest.setUserInfoMap(extraInfoMap);
		}
		//		selectionRequest.setRentalPack(musicPack);
		try{
			boolean isMusicPack = Boolean.parseBoolean(musicPack);
			if (isMusicPack)
				selectionRequest.setCosID(Integer.parseInt(PropertyConfigurator.getMusicPackCosId()));
		}catch(Exception e){
		}

		if(inLoop != null && inLoop.equalsIgnoreCase("y")) {
			selectionRequest.setInLoop(true);
		}
		if(!isConsent) {
			client.addSubscriberSelection(selectionRequest);
		}
		else {
			Rbt rbt = ConsentUtilityFactory.getConsentUtlityObject().
					addSubscriberConsentSelection(selectionRequest);
			String response = selectionRequest.getResponse();
			logger.info("ConsentSelectionIntegration :: Rbt = " + rbt);
			Consent consent = null;
			if (rbt != null) {
				consent = rbt.getConsent();
			}
			if (consent != null && response.equalsIgnoreCase("success")) {
				ConsentProcessBean consentProcessBean = new ConsentProcessBean();
				if(consent.getClipId() == null || consent.getClipId().equalsIgnoreCase(""))
					consent.setClipId(selectionRequest.getClipID());
				if(consent.getCatId() == null || consent.getCatId().equalsIgnoreCase(""))
					consent.setCatId(selectionRequest.getCategoryID());
				consentProcessBean.setConsent(consent);
				consentProcessBean.setResponse(response);
				consentProcessBean.setSubscriber(subscriber);
				consentProcessBean.setSubscriberId(subscriberId);
				if(isComvivaCircle){
				  consentProcessBean.setCircleID(circleID);
				}
				//Changed for AT-103588
				ConsentUtility consentUtlityObj = ConsentUtilityFactory.getConsentUtlityObject();
				if(consent.getClass() == ComvivaConsent.class && consentUtlityObj.getClass() == AirtelConsentUtility.class){
					consentUtlityObj = ConsentUtilityFactory.getComvivaConsentUtlityObject();
				}
				response = consentUtlityObj.makeConsentCgUrl(consentProcessBean);
			}
			return response;
		}
		return selectionRequest.getResponse();
	}

	public String getSelectionAmount(final String subscriberId,final String clipId, String categoryId, String channel, String chargeClass, String musicPack, String profilePack, boolean useUIChargeClass) throws Exception{
		SelectionRequest selectionRequest = new SelectionRequest(subscriberId,new Boolean(true),null,channel,categoryId,clipId,null,null);
		selectionRequest.setChargeClass(chargeClass);
		selectionRequest.setUseUIChargeClass(useUIChargeClass);

		// added this check for webservice to distinguish the recorded clips and return configured amount.
		if (clipId != null && (clipId.endsWith(".3gp") || clipId.endsWith(".wav")))
		{
			selectionRequest.setSelectionType(99);
		}
		try{
			boolean isMusicPack = Boolean.parseBoolean(musicPack);
			boolean isProfilePack = Boolean.parseBoolean(profilePack);
			if (isMusicPack)
				selectionRequest.setCosID(Integer.parseInt(PropertyConfigurator.getMusicPackCosId()));
			else if (isProfilePack)
			{
				String chargeClassForProfile = PropertyConfigurator.getChargeClassForProfile();
				logger.info("chargeClassForProfile :==>"+chargeClassForProfile);
				if(chargeClassForProfile != null && chargeClassForProfile.length() >0) {
					selectionRequest.setUseUIChargeClass(true);
					selectionRequest.setChargeClass(chargeClassForProfile);
					logger.info("if block for charge class");
				} else {
					if (PropertyConfigurator.getProfileCosId() != null)
						selectionRequest.setCosID(Integer.parseInt(PropertyConfigurator.getProfileCosId()));
					else if (PropertyConfigurator.getNormalProfileCosId() != null)
						selectionRequest.setCosID(Integer.parseInt(PropertyConfigurator.getNormalProfileCosId()));
				} 	
			}

		}catch(Exception e){
		}
		logger.info("getNextChargeClassForRMO request :=="+selectionRequest);
		ChargeClass chargeclass = (ChargeClass)client.getNextChargeClassForRMO(selectionRequest);
		String amount = PropertyConfigurator.getDefaultPriceAmount();
		if (chargeclass != null)
			amount = chargeclass.getAmount();
		return amount;
	}

	public String getGiftAmount(final String subscriberId,final String clipId, String categoryId, String channel, String chargeClass) throws Exception{
		SelectionRequest selectionRequest = new SelectionRequest(subscriberId,new Boolean(true),null,channel,categoryId,clipId,null,null);
		selectionRequest.setChargeClass(chargeClass);
		//ExtendedClipBean extendedClip = new ExtendedClipBean();
		try{
			Clip clipDetails = RBTCacheManager.getInstance().getClip(clipId);
			logger.info("Clip Details "+clipDetails);
			if(clipDetails!=null ){
				//				extendedClip = new ExtendedClipBean(clipDetails, ContentManager.getClipRating(Integer.parseInt(clipId)));
			}
		}catch(Exception e){
		}
		ChargeClass chargeclass = (ChargeClass)client.getNextChargeClass(selectionRequest);
		return chargeclass.getAmount();
	}

	public String addSubscriberSelection(String subId, String callerId,
			String clipId, String catId, Integer status, boolean isPrepaid,
			String mode, String modeInfo, String lang, Integer startTimeHrs,
			Integer startTimeMins, Integer endTimeHrs, Integer endTimeMins,
			String interval, Integer cosId, String gifterId, String giftSentTime, String selStartTime, String selEndTime, String inLoop, boolean isConsent, 
			String subscriptionClass, String baseOfferId, String chargeClass, String selOfferId, boolean useUIChargeClass, String smOfferType) {
		String calledNo = null;
		SelectionRequest selectionRequest = new SelectionRequest(subId, isPrepaid, callerId, mode, catId, clipId,
				lang, calledNo, startTimeHrs, endTimeHrs, status, interval);
		selectionRequest.setChargeClass(chargeClass);
		selectionRequest.setUseUIChargeClass(useUIChargeClass);
		if (smOfferType != null) {
			HashMap<String, String> selInfoMap = new HashMap<String, String>();
			selInfoMap.put("OFFER_TYPE", smOfferType);
			selectionRequest.setSelectionInfoMap(selInfoMap);
		}
			
		

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		Date startDate = null;
		Date endDate = null;
		if(selStartTime!=null && selEndTime!=null){
			try {
				if (selStartTime.length() > 12)
					selStartTime = selStartTime.substring(0, 12);
				if (selEndTime.length() > 12)
					selEndTime = selEndTime.substring(0, 12);

				startDate = dateFormat.parse(selStartTime);
				endDate = dateFormat.parse(selEndTime);

				if (status < 2)
					status = 2;

				if (endDate.before(startDate) || endDate.before(new Date()))
				{
					logger.info("selectionEndTime is before current time or before selectionStartTime");
					return StringConstants.FAILURE;
				}
			} catch (ParseException e) {
				logger.info("sselectionStartTime or selectionEndTime not in proper format(yyyyMMddHHmm)");
				e.printStackTrace();
				return StringConstants.FAILURE;
			}
		}
		selectionRequest.setSelectionStartTime(startDate);
		selectionRequest.setSelectionEndTime(endDate);

		selectionRequest.setModeInfo(modeInfo);
		selectionRequest.setCosID(cosId);
		selectionRequest.setStatus(status);
		selectionRequest.setFromTimeMinutes(startTimeMins);
		selectionRequest.setToTimeMinutes(endTimeMins);

		if (Utility.isStringValid(baseOfferId) && Utility.isStringValid(subscriptionClass)) {
			logger.debug("Valid baseOfferId: " + baseOfferId + " and subscriptionClass: " + subscriptionClass);
			selectionRequest.setSubscriptionOfferID(baseOfferId);
			selectionRequest.setSubscriptionClass(subscriptionClass);
		} else if (Utility.isStringValid(subscriptionClass)) {
			selectionRequest.setSubscriptionClass(subscriptionClass);
		}

		if (Utility.isStringValid(selOfferId) && Utility.isStringValid(chargeClass)) {
			logger.debug("Valid selOfferId: " + selOfferId + " and chargeClass: " + chargeClass);
			selectionRequest.setOfferID(selOfferId);
			selectionRequest.setChargeClass(chargeClass);
			selectionRequest.setUseUIChargeClass(true);
		}
		if(inLoop != null && inLoop.equalsIgnoreCase("y")) {
			selectionRequest.setInLoop(true);
		}
		if(gifterId != null && giftSentTime != null)
		{
			try
			{
				selectionRequest.setGifterID(gifterId);
				selectionRequest.setGiftSentTime(giftDateFormat.parse(giftSentTime));
				client.acceptGift(selectionRequest);
			}
			catch (ParseException pe)
			{
				return "error";
			}
		}
		else {
			if(!isConsent) {
				client.addSubscriberSelection(selectionRequest);
			}
			else {
				Rbt rbt = ConsentUtilityFactory.getConsentUtlityObject().
						addSubscriberConsentSelection(selectionRequest);
				String response = selectionRequest.getResponse();
				logger.info("ConsentSelectionIntegration :: Rbt = " + rbt);
				Consent consent = null;
				if (rbt != null) {
					consent = rbt.getConsent();
				}
				if (consent != null && response.equalsIgnoreCase("success")) {
					ConsentProcessBean consentProcessBean = new ConsentProcessBean();
					if(consent.getClipId() == null || consent.getClipId().equalsIgnoreCase(""))
						consent.setClipId(selectionRequest.getClipID());
					if(consent.getCatId() == null || consent.getCatId().equalsIgnoreCase(""))
						consent.setCatId(selectionRequest.getCategoryID());
					consentProcessBean.setConsent(consent);
					consentProcessBean.setResponse(response);
					consentProcessBean.setSubscriberId(subId);
					response = ConsentUtilityFactory.getConsentUtlityObject()
							.makeConsentCgUrl(consentProcessBean);
				}
				return response;
			}
		}
		return selectionRequest.getResponse();
	}

	//	gives both downloads and selections for the subscriber ( as per downloads model)
	public List<ExtendedSelectionBean> getSubscriberSelections(String subscriberId, boolean isDownloadsMerged, String browsingLangauge, String appName, String circleId) {
		List<ExtendedSelectionBean> subscriberSelections = new ArrayList<ExtendedSelectionBean>();
		try {
			logger.info("mobile for RBTDetailrequest " + subscriberId);
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId,null);
			Library library = null;

//			rbtDetailsRequest.setMode("CCC");
			String mode = PropertyConfigurator.getSelectionsMode();
			logger.info("PropertyConfigurator information get selections mode :-->"
					+ mode);
			rbtDetailsRequest.setMode(mode);

			rbtDetailsRequest.setMode("CCC");
			if(circleId != null && !circleId.isEmpty())
				rbtDetailsRequest.setCircleID(circleId);
			//RBT_AT-103588_comvivaInt
			library = client.getLibrary(rbtDetailsRequest);

			if(library != null) {
				Settings settings = library.getSettings();
				Setting[] settingArray = null;
				if (settings != null) {
					settingArray = settings.getSettings();
					logger.info("settings length" + settingArray!= null?settingArray.length:0);
				}else{
					logger.info("setting null");
				}
				Downloads downloads = library.getDownloads();
				Download[] downloadArray = null;
				if (downloads != null) {
					downloadArray = downloads.getDownloads();
					logger.info("download length" + downloadArray.length);
				}else{
					logger.info("download null");
				}
				logger.debug("selection information:-->"+library +"--<setting info:--->"+settingArray+"--<download info:-->"+downloadArray );
				
				List<ExtendedSelectionBean> settingClips = contentManager.getExtendedSelectionBeanList(settingArray,browsingLangauge,appName);
				Map<String, List<ExtendedSelectionBean>> selBeanMap = getSelectionBeanMap(settingClips);
				Map<String, List<ExtendedSelectionBean>> ugcSelBeanMap = getUgcSelectionBeanMap(settingClips);
				
				logger.info("PropertyConfigurator information :-->" +PropertyConfigurator.isDownloadsModel());
				if ((isDownloadsMerged && PropertyConfigurator.isMergeDownloadAndSelection()) || PropertyConfigurator.isDownloadsModel())
				{
					if (downloadArray != null && downloadArray.length > 0)
					{
						logger.info("1 :-->" +downloadArray);
						for (int i = 0; i < downloadArray.length; i++) {
							logger.info(selBeanMap
									+ "selection downloadArray:-->"
									+ downloadArray
									+ "downloadArray[i].getToneID():"
									+ downloadArray[i].getToneID());
							String dateformat =  "yyyyMMddHHmmss";
							if(PropertyConfigurator.getDateFormat() != null && PropertyConfigurator.getDateFormat().length()>0) {
								dateformat = PropertyConfigurator.getDateFormat();
							}
							SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
							String nextChargingDate = null;
							logger.debug("DOWNLOAD information:-->"+library +"--<setting info:--->"+settingArray+"--<download info:-->"+downloadArray );
							if(null != downloadArray[i].getNextBillingDate()) {
								nextChargingDate = sdf.format(downloadArray[i].getNextBillingDate());
							}
							String selectedBy = downloadArray[i].getSelectedBy();
							String ugcRbtFileName = downloadArray[i]
									.getUgcRbtFile();
							if (null != ugcRbtFileName) {
								if (ugcRbtFileName.endsWith(".3gp")) {
									ugcRbtFileName = ugcRbtFileName.substring(
											0, ugcRbtFileName.indexOf(".3gp"));
								} else if (ugcRbtFileName.endsWith(".wav")) {
									ugcRbtFileName = ugcRbtFileName.substring(
											0, ugcRbtFileName.indexOf(".wav"));
								}
							}
							if (!selBeanMap.containsKey(String
									.valueOf(downloadArray[i].getToneID()))
									&& !ugcSelBeanMap
											.containsKey(ugcRbtFileName)) {
								logger.info("if cont downloadArray :-->" +downloadArray);
								int catID = downloadArray[i].getCategoryID();
								Category cat = cacheManager.getCategory(catID,browsingLangauge,appName);
								String catName = null;
								if (cat != null && Utility.isShuffleCategory(cat.getCategoryTpe()))
									catName = cat.getCategoryName();

								ExtendedClipBean selectionSong = ClipUtils.getExtendedClipByClipId(downloadArray[i].getToneID() + "", browsingLangauge, appName);
								ExtendedSelectionBean selBean = new ExtendedSelectionBean(selectionSong, null, null, null, 0, downloadArray[i].getDownloadStatus(), catID, null, null, null, catName, null);
								selBean.setNextChargingDate(nextChargingDate);
								selBean.setSelectedBy(selectedBy);
								if (null != ugcRbtFileName)
									selBean.setUgcRbtFile(ugcRbtFileName);
								logger.debug("DOWNLOAD informationf:-->"+nextChargingDate +"--<setting info:--->"+settingArray+"--<download info:-->"+downloadArray );
								subscriberSelections.add(selBean);
							}
							else
							{
								logger.info("1::-->else" );
								List<ExtendedSelectionBean> selBeanList = selBeanMap.get(String.valueOf(downloadArray[i].getToneID()));
								if (null == selBeanList
										|| selBeanList.isEmpty()) {
									selBeanList = ugcSelBeanMap
											.get(ugcRbtFileName);
								}
								logger.info("2::-->selBeanList" +selBeanList);
								List<ExtendedSelectionBean> selListObj = new ArrayList<ExtendedSelectionBean>();
								for(ExtendedSelectionBean selBean : selBeanList) {
									selBean.setNextChargingDate(nextChargingDate);
									selBean.setSelectedBy(selectedBy);
									logger.info(nextChargingDate+"3::-->selBean" +selBean);
									selListObj.add(selBean);
								}

								subscriberSelections.addAll(selBeanList);
								logger.info("4::-->subscriberSelections" +subscriberSelections);
							}
						}
					}
				}
				else
				{
					subscriberSelections.addAll(settingClips);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getSubscriberSelections:", e);
		}
		return subscriberSelections;
	}

	private Map<String, List<ExtendedSelectionBean>> getSelectionBeanMap(
			List<ExtendedSelectionBean> settingClips)
			{
		Map<String, List<ExtendedSelectionBean>> selBeanMap = new HashMap<String, List<ExtendedSelectionBean>>();
		for (ExtendedSelectionBean extendedSelectionBean : settingClips)
		{
			String clipID = String.valueOf(extendedSelectionBean.getClipId());
			List<ExtendedSelectionBean> selList = selBeanMap.get(clipID);
			if (selList == null)
				selList = new ArrayList<ExtendedSelectionBean>();

			selList.add(extendedSelectionBean);
			selBeanMap.put(clipID, selList);
		}

		return selBeanMap;
			}

	private Map<String, List<ExtendedSelectionBean>> getUgcSelectionBeanMap(
			List<ExtendedSelectionBean> settingClips) {
		Map<String, List<ExtendedSelectionBean>> selBeanMap = new HashMap<String, List<ExtendedSelectionBean>>();
		for (ExtendedSelectionBean extendedSelectionBean : settingClips) {
			String clipID = extendedSelectionBean.getUgcRbtFile();
			if (null != clipID && !clipID.isEmpty()) {
				List<ExtendedSelectionBean> selList = selBeanMap.get(clipID);
				if (selList == null)
					selList = new ArrayList<ExtendedSelectionBean>();

				selList.add(extendedSelectionBean);
				selBeanMap.put(clipID, selList);
			}
		}
		return selBeanMap;
	}

	public List<ExtendedSelectionBean> getSubscriberProfileSelections(String subscriberId) {
		List<ExtendedSelectionBean> subscriberSelections = new ArrayList<ExtendedSelectionBean>();
		try {
			logger.info("mobile for RBTDetailrequest " + subscriberId);
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId,null);
			rbtDetailsRequest.setMode(StringConstants.CHANNEL);
			logger.info("rbtDetails request created");
			Library library = client.getLibraryHistory(rbtDetailsRequest);
			if(library != null){
				Settings settings = library.getSettings();
				logger.info("Profile settings returned");

				Setting[] settingArray = null;
				if (settings != null) {
					settingArray = settings.getSettings();
					logger.info("settings length" + settingArray!= null?settingArray.length:0);
				}else{
					logger.info("setting null");
				}
				List<ExtendedSelectionBean> settingClips = contentManager.getExtendedProfileSelectionBeanList(settingArray);
				subscriberSelections.addAll(settingClips);

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getSubscriberSelections:", e);
		}
		return subscriberSelections;
	}

	/*private List<String> getSettingClipIds(Setting[] settingArray) {
		List<String> settingClipIds = new ArrayList<String>();
		for(int i=0; settingArray!=null && i<settingArray.length; i++){
			settingClipIds.add(String.valueOf(settingArray[i].getToneID()));
		}
		return settingClipIds;
	}*/

	/*private List<String> getSettingClipCallerIds(Setting[] settingArray) {
		List<String> settingCallerIds = new ArrayList<String>();
		for(int i=0; i<settingArray.length; i++){
			settingCallerIds.add(settingArray[i].getCallerID());
		}
		return settingCallerIds;
	}


	private List<ExtendedSelectionBean> getSelectionBeanList(Setting[] settingArray) {
		List<ExtendedSelectionBean> settingSelectionBeans = new ArrayList<ExtendedSelectionBean>();
		if(settingArray != null){
			String[] settingClipIdArray = new String[settingClipIds.size()];
			settingClipIds.toArray(settingClipIdArray);
			settingSelectionBeans = contentManager.getExtendedSelectioBeanListByClipIds(settingClipIdArray, settingClipCallerIds);
		}
		return settingSelectionBeans;
	}*/



	public String removeSubscriberSelection(String subscriberId, String clipId, String caller, String channel, String selStartTime, String selEndTime, String rbtWavFile, String circleId) {
		logger.info("clip ID "+clipId+" channel "+channel+"caller "+caller);
		SelectionRequest selRequest = new SelectionRequest(subscriberId,clipId);
		selRequest.setMode(channel);
		selRequest.setCallerID(caller);
		if (clipId.equals("0"))
		{
			selRequest.setRbtFile(rbtWavFile);
			selRequest.setClipID(null);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date startDate = null;
		Date endDate = null;
		if(selStartTime!=null && selEndTime!=null){
			try {
				startDate = dateFormat.parse(selStartTime);
				endDate = dateFormat.parse(selEndTime);

				selRequest.setSelectionStartTime(startDate);
				selRequest.setSelectionEndTime(endDate);
			} catch (ParseException e) {
				logger.info("sselectionStartTime or selectionEndTime not in proper format(yyyyMMddHHmmss)");
				logger.error(e.getMessage(), e);
				return StringConstants.FAILURE;
			}
		}
		if(circleId != null && !circleId.isEmpty())
			selRequest.setCircleID(circleId);
		client.deleteSubscriberSelection(selRequest);
		logger.info("RBT:: getRemoveSelectionResponse "+selRequest.getResponse());	
		return selRequest.getResponse();
	}

	public ExtendedGiftBean[] getGiftInbox(String subId) {
		logger.info("inside getGiftInbox with subId " + subId );
		ExtendedGiftBean[] giftInboxSongs = null;
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subId);
		GiftInbox giftInbox = client.getGiftInbox(rbtDetailsRequest);
		if(giftInbox!=null){
			Gift[] gifts = giftInbox.getGifts();
			if (gifts != null && gifts.length > 0)
			{
				giftInboxSongs = new ExtendedGiftBean[gifts.length];
				for (int i = 0; i < gifts.length; i++)
				{
					String rbtFile = gifts[i].getRbtFile();
					if (rbtFile.endsWith(".wav")) {
						rbtFile = rbtFile.substring(0, rbtFile.indexOf(".wav"));
					}
					String previewFile = gifts[i].getPreviewFile();
					if (previewFile.endsWith(".wav")) {
						previewFile = previewFile.substring(0, previewFile.indexOf(".wav"));
					}
					gifts[i].setRbtFile(rbtFile);
					gifts[i].setPreviewFile(previewFile);
					giftInboxSongs[i] = new ExtendedGiftBean(gifts[i]);
				}
			}
		}
		return giftInboxSongs;
	}

	public String rejectGift(String gifterId, String gifteeId, String giftSentTime)
	{
		String response = null;
		try
		{
			logger.info("inside rejectGift with gifterId=" + gifterId +", gifteeId="+gifteeId+", giftSentTime="+giftSentTime);
			GiftRequest giftRequest = new GiftRequest(gifterId, gifteeId, giftDateFormat.parse(giftSentTime));
			client.rejectGift(giftRequest);
			response = giftRequest.getResponse();
		}
		catch(ParseException pe)
		{
			response = "error";
		}
		return response;
	}

	public List<ExtendedGroupBean> getGroups(String subscriberId) {
		RbtDetailsRequest groupRequest = new RbtDetailsRequest(subscriberId);
		GroupDetails grpDet = client.getGroupDetails(groupRequest);
		Group[] groups = null;
		List<ExtendedGroupBean> groupList = new ArrayList<ExtendedGroupBean>();
		if (grpDet != null && grpDet.getGroups() != null && grpDet.getGroups().length > 0) {
			logger.info("getting groups -->" + grpDet);
			groups = grpDet.getGroups();
			logger.info("getting groups -->" + groups);
			for (Group group : groups) {
				if(group.getPredefinedGroupID()!= null && !group.getPredefinedGroupID().equals("98")) {
					if (group.getGroupMembers() != null && group.getGroupMembers().length > 0) {
						GroupMemberNameComparator comparator = new GroupMemberNameComparator();
						Arrays.sort(group.getGroupMembers(), comparator);
					}
					ExtendedGroupBean groupExt = new ExtendedGroupBean(group, null);
					groupList.add(groupExt);
				}
			}
		}
		ExtendedGroupBeanNameComparator comparator = new ExtendedGroupBeanNameComparator();
		Collections.sort(groupList, comparator);
		return groupList;
	}

	public PredefinedGroup[] getPredefinedGroups() {
		ApplicationDetailsRequest predefinedGroupRequest = new ApplicationDetailsRequest();
		PredefinedGroup[] predefinedGroupsArray = client.getPredefinedGroups(predefinedGroupRequest);
		int len = predefinedGroupsArray.length;

		PredefinedGroup[] predefinedGroupsArrayObj = new PredefinedGroup[len];
		int j =0;
		for(int i = 0 ; i< len; i++) {
			if(!predefinedGroupsArray[i].getGroupID().equals("98")) {
				predefinedGroupsArrayObj[j] = predefinedGroupsArray[i];
				j++;
			}
		}
		PredefinedGroup[] resultGroupsArrayObj = new PredefinedGroup[j];
		System.arraycopy(predefinedGroupsArrayObj, 0, resultGroupsArrayObj, 0, j);
		if (resultGroupsArrayObj != null && resultGroupsArrayObj.length > 0) {
			PredefinedGroupNameComparator comparator = new PredefinedGroupNameComparator(); 
			Arrays.sort(resultGroupsArrayObj, comparator);
		}
		logger.info("mobile for RBTDetailrequest " + resultGroupsArrayObj);
		return resultGroupsArrayObj ;
	}

	public PredefinedGroup[] getAllPredefinedGroups() {
		ApplicationDetailsRequest predefinedGroupRequest = new ApplicationDetailsRequest();
		PredefinedGroup[] predefinedGroupsArray = client.getPredefinedGroups(predefinedGroupRequest);
		return predefinedGroupsArray;
	}

	public List<ExtendedSelectionBean> getCopySelections(String copyMobileNumber, String subscriberId) {
		List<ExtendedSelectionBean> copySelections = new ArrayList<ExtendedSelectionBean>();
		try {
			logger.info("mobile for RBTDetailrequest " + copyMobileNumber);
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(copyMobileNumber,null);
			logger.info("rbtDetails request created");
			Library library = client.getLibrary(rbtDetailsRequest);
			logger.info("library returned");
			if(library!=null){
				logger.info("library is not null");
				Settings settings = library.getSettings();
				Setting[] settingArray = null;
				if (settings != null) {
					settingArray = settings.getSettings();
					logger.info("settings length" + settingArray.length);
				}else{
					logger.info("setting null");
				}

				List<ExtendedSelectionBean> settingClips = contentManager.getExtendedSelectionBeanList(settingArray,null,null);
				List<ExtendedSelectionBean> newSettingClips = new ArrayList<ExtendedSelectionBean>();
				for(ExtendedSelectionBean clip : settingClips) {
					String result = Utility.getChargeClass(subscriberId, String.valueOf(clip.getClipId()),
							PropertyConfigurator.getCatIdForChargeClass());
					if(result != null) {
						clip.setPeriod(result.split(":")[0]);
						clip.setAmount(result.split(":")[1]);
						clip.setRenewalPeriod(result.split(":")[2]);
						clip.setRenewalAmount(result.split(":")[3]);
					}
					newSettingClips.add(clip);
				} 
				copySelections.addAll(newSettingClips);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getSubscriberSelections:", e);
		}
		return copySelections;
	}

	public String addSubscriberProfileSelection(String subscriberId,
			String caller, String clipId, String categoryId, String channel,
			String selectionStartTime, String selectionEndTime, String rbtWavFile, boolean isConsent, 
			String subscriptionClass, String baseOfferId, String chargeClass, String selOfferId, String profileHours, boolean useUIChargeClass,String smOfferType) {

		SelectionRequest selectionRequest = new SelectionRequest(subscriberId,new Boolean(true),caller,channel,categoryId,clipId,null,null);
		selectionRequest.setUseUIChargeClass(useUIChargeClass);
		selectionRequest.setChargeClass(chargeClass);
		if (smOfferType != null) {
			HashMap<String, String> selInfoMap = new HashMap<String, String>();
			selInfoMap.put("OFFER_TYPE", smOfferType);
			selectionRequest.setSelectionInfoMap(selInfoMap);
		}

		if (clipId.equals("0"))
		{
			selectionRequest.setClipID(rbtWavFile);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		Date startDate = null;
		Date endDate = null;
		Integer packCosId = null;
		Integer selectionType = 99;

		if (Utility.isStringValid(baseOfferId) && Utility.isStringValid(subscriptionClass)) {
			logger.debug("Valid baseOfferId: " + baseOfferId + " and subscriptionClass: " + subscriptionClass);
			selectionRequest.setSubscriptionOfferID(baseOfferId);
			selectionRequest.setSubscriptionClass(subscriptionClass);
		} else if (Utility.isStringValid(subscriptionClass)) {
			selectionRequest.setSubscriptionClass(subscriptionClass);
		}

		String chargeClassForProfile = PropertyConfigurator.getChargeClassForProfile();
		if (Utility.isStringValid(selOfferId) && Utility.isStringValid(chargeClass)) { //Highest priority
			logger.debug("Valid selOfferId: " + selOfferId + " and chargeClass: " + chargeClass);
			selectionRequest.setOfferID(selOfferId);
			selectionRequest.setChargeClass(chargeClass);
			selectionRequest.setUseUIChargeClass(true);
		} else if(chargeClassForProfile != null && chargeClassForProfile.length() >0) {
			selectionRequest.setUseUIChargeClass(true);
			selectionRequest.setChargeClass(chargeClassForProfile);
			logger.info("if block for charge class");
		} else {
			logger.info("else block for cos id");
			if(!isProfilePackActivated(subscriberId)){
				try{
					packCosId = Integer.parseInt(PropertyConfigurator.getProfileCosId());
					selectionRequest.setPackCosId(packCosId);
				}catch(NumberFormatException nfe){
					if (PropertyConfigurator.getNormalProfileCosId() == null)
					{
						logger.info("mobileapp.profile.cos.id not configure in the parameters table with type MOBILEAPP");
						nfe.printStackTrace();
						return StringConstants.FAILURE;
					}
					else
					{
						selectionRequest.setCosID(Integer.parseInt(PropertyConfigurator.getNormalProfileCosId()));
					}
				}
			}
		}
		if(selectionStartTime!=null && selectionEndTime!=null){
			try {
				if (selectionStartTime.length() > 12)
					selectionStartTime = selectionStartTime.substring(0, 12);
				if (selectionEndTime.length() > 12)
					selectionEndTime = selectionEndTime.substring(0, 12);

				startDate = dateFormat.parse(selectionStartTime);
				endDate = dateFormat.parse(selectionEndTime);

				if (endDate.before(startDate) || endDate.before(new Date()))
				{
					logger.info("selectionEndTime is before current time or before selectionStartTime");
					return StringConstants.FAILURE;
				}

			} catch (ParseException e) {
				logger.info("sselectionStartTime or selectionEndTime not in proper format(yyyyMMddHHmm)");
				e.printStackTrace();
				return StringConstants.FAILURE;
			}
		}

		selectionRequest.setSelectionStartTime(startDate);
		selectionRequest.setSelectionEndTime(endDate);
		selectionRequest.setSelectionType(selectionType);
		selectionRequest.setMode(channel);
		selectionRequest.setModeInfo(channel);
		selectionRequest.setProfileHours(profileHours);
		logger.info("selectionRequest :---" + selectionRequest);
		if(!isConsent) {
			client.addSubscriberSelection(selectionRequest);
		}
		else {
			Rbt rbt = ConsentUtilityFactory.getConsentUtlityObject().
					addSubscriberConsentSelection(selectionRequest);
			String response = selectionRequest.getResponse();
			logger.info("ConsentSelectionIntegration :: Rbt = " + rbt);
			Consent consent = null;
			if (rbt != null) {
				consent = rbt.getConsent();
			}
			if (consent != null && response.equalsIgnoreCase("success")) {
				ConsentProcessBean consentProcessBean = new ConsentProcessBean();
				if(consent.getClipId() == null || consent.getClipId().equalsIgnoreCase(""))
					consent.setClipId(selectionRequest.getClipID());
				if(consent.getCatId() == null || consent.getCatId().equalsIgnoreCase(""))
					consent.setCatId(selectionRequest.getCategoryID());
				consentProcessBean.setConsent(consent);
				consentProcessBean.setResponse(response);
				consentProcessBean.setSubscriberId(subscriberId);
				response = ConsentUtilityFactory.getConsentUtlityObject()
						.makeConsentCgUrl(consentProcessBean);
			}
			return response;
		}
		return selectionRequest.getResponse();
	}

	public String deactivateProfilePack(String subscriberId) {
		SubscriptionRequest subReq = new SubscriptionRequest(subscriberId);
		Integer packCosId = Integer.parseInt(PropertyConfigurator.getProfileCosId());
		subReq.setPackCosId(packCosId);
		subReq.setMode(StringConstants.CHANNEL);
		subReq.setModeInfo(StringConstants.CHANNEL);
		client.deactivatePack(subReq);
		return subReq.getResponse();
	}

	public String deactivateMusicPack(String subscriberId) {
		SubscriptionRequest subReq = new SubscriptionRequest(subscriberId);
		Integer packCosId = Integer.parseInt(PropertyConfigurator.getMusicPackCosId());
		subReq.setPackCosId(packCosId);
		subReq.setMode(StringConstants.CHANNEL);
		subReq.setModeInfo(StringConstants.CHANNEL);
		client.deactivatePack(subReq);
		return subReq.getResponse();
	}

	public String subscribeUser(String subscriberId,String type,String password,Boolean isResetPassword, String uid, String appName) {
		SubscriptionRequest subReq = new SubscriptionRequest(subscriberId);
		subReq.setType(type);
		subReq.setInfo(password);
		subReq.setResetPassword(isResetPassword);
		HashMap<String, String> userInfoMap = null;

		if (Utility.isStringValid(uid)) {
			userInfoMap = new HashMap<String, String>();
			userInfoMap.put("uid", uid);
		}
		if (Utility.isStringValid(appName)) {
			if (userInfoMap == null) {
				userInfoMap = new HashMap<String, String>();
			}
			userInfoMap.put(WebServiceConstants.param_appName, appName);
		}

		if (userInfoMap != null) {
			subReq.setUserInfoMap(userInfoMap);
		}
		client.subscribeUser(subReq);
		String rbtResponse = subReq.getResponse();
		if (rbtResponse != null && !rbtResponse.equalsIgnoreCase("SUCCESS")) {
			logger.error("Registration failed. Reason: " + rbtResponse);
			rbtResponse = subReq.getResponse().toLowerCase();
			String response = PropertyConfigurator.getResponseForGetRegistrationErrorCase(rbtResponse);
			if (response == null) {
				logger.debug("UID: " + uid + ", subscriberId: " + subscriberId + ". No configuration found for webservice response: " + rbtResponse);
				response = rbtResponse;
			}
			return response;
		}
		return subReq.getResponse();
	}

	public String getParameterValue(String param) {
		return PropertyConfigurator.getParameterValue(param);
	}

	public String addGCMRegistration(String regId, String subId, String os_type) {
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setUserID(regId);
		applicationDetailsRequest.setSubscriberID(subId);
		applicationDetailsRequest.setType(os_type);
		return client.addGCMRegistration(applicationDetailsRequest);
	}

	public String removeGCMRegistration(String regId, String subId) {
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setUserID(regId);
		applicationDetailsRequest.setSubscriberID(subId);

		return client.removeGCMRegistration(applicationDetailsRequest);
	}

	public String addContactMemebers(String subscriberId, String callerId,
			String callerName, String predefinedGroupId, String groupId, String mode) {
		GroupRequest groupRequest = null;
		if (!Utility.isStringValid(groupId)) {
			logger.debug("groupId empty or null. Obtaing groupId for the predefinedGroupId: "
					+ predefinedGroupId);
			// RBT-15174
			String grpName = null;
			if (predefinedGroupId != null && predefinedGroupId.trim().equals("99")) {
				grpName = "BLOCKED_GROUP";
			} else {
				ApplicationDetailsRequest predefinedGroupRequest = new ApplicationDetailsRequest();
				PredefinedGroup[] predefinedGroupsArray = client
						.getPredefinedGroups(predefinedGroupRequest);
				logger.info("predefinedGroupId: " + predefinedGroupId);

				for (PredefinedGroup predefinedGroup : predefinedGroupsArray) {
					if (predefinedGroupId == null|| predefinedGroupId.length() == 0) {				if (predefinedGroup.getGroupID().equals("98")) {
							grpName = predefinedGroup.getGroupName();
							predefinedGroupId = "98";
						}
					} else {
						if (predefinedGroup.getGroupID().equals(predefinedGroupId)) {
							grpName = predefinedGroup.getGroupName();
						}
					}
				}
			}
			groupRequest = new GroupRequest(subscriberId, grpName,
					predefinedGroupId, callerId, callerName);
		} else {
			groupRequest = new GroupRequest(subscriberId, groupId, callerId, callerName);
		}
		client.addGroupMember(groupRequest);
		logger.info("addContactMembers response: " + groupRequest.getResponse());
		return groupRequest.getResponse();
	}

	public String addMultipleContactMemebers(String subscriberId, String callerId,
			String callerName, String predefinedGroupId, String groupId, String mode) {
		GroupRequest groupRequest = null;
		if (!Utility.isStringValid(groupId)) {
			logger.debug("groupId empty or null. Obtaing groupId for the predefinedGroupId: " + predefinedGroupId);
			ApplicationDetailsRequest predefinedGroupRequest = new ApplicationDetailsRequest();
			PredefinedGroup[] predefinedGroupsArray = client
					.getPredefinedGroups(predefinedGroupRequest);
			String grpName = null;
			logger.info("predefinedGroupId: " + predefinedGroupId);
			for (PredefinedGroup predefinedGroup : predefinedGroupsArray) {
				if (predefinedGroupId == null || predefinedGroupId.length() == 0) {
					if (predefinedGroup.getGroupID().equals("98")) {
						grpName = predefinedGroup.getGroupName();
						predefinedGroupId = "98";
					}
				} else {
					if (predefinedGroup.getGroupID().equals(predefinedGroupId)) {
						grpName = predefinedGroup.getGroupName();
					}
				}
			}
			groupRequest = new GroupRequest(subscriberId, grpName,
					predefinedGroupId, callerId, callerName);
		} else {
			groupRequest = new GroupRequest(subscriberId, groupId, callerId, callerName);
		}

		client.addGroupMultipleMember(groupRequest);
		logger.info("addMultipleContactMemebers response: " + groupRequest.getResponse());
		return groupRequest.getResponse();
	}

	public List<ExtendedGroupBean> getAllContactMemebers(String subscriberId, String predefinedGroupId, String mode) {
		ApplicationDetailsRequest predefinedGroupRequest = new ApplicationDetailsRequest();
		PredefinedGroup[] predefinedGroupsArray = client.getPredefinedGroups(predefinedGroupRequest);
		String preGrpName = null;
		List<ExtendedGroupBean> extendedGrpList = new ArrayList<ExtendedGroupBean>();
		for(PredefinedGroup predefinedGroup : predefinedGroupsArray) {
			if(predefinedGroupId == null || predefinedGroupId.length()==0) {
				preGrpName = predefinedGroup.getGroupName();
				predefinedGroupId = "98";
				extendedGrpList = listAllPrdefinedGroupwithMember(subscriberId, "98", preGrpName);
			} else if(predefinedGroup.getGroupID().equals(predefinedGroupId)) {
				preGrpName = predefinedGroup.getGroupName();
				logger.info(preGrpName);
				extendedGrpList = listAllPrdefinedGroupwithMember(subscriberId, predefinedGroupId, preGrpName);
			}  else if(predefinedGroupId.equalsIgnoreCase("all")) {
				extendedGrpList = listAllPrdefinedGroupwithMember(subscriberId, null, null);
			}
		}


		return extendedGrpList;
	}

	/**
	 * To get all the contacts from all groups. The response list would be sorted in ascending order of contact names.
	 */
	public List<GroupMember> getAllContactsFromAllGroups(String subscriberId, String mode) {
		logger.debug("preDefinedGroupId set as -1, subscriberId: " + subscriberId);
		List<GroupMember> groupMemberList = new ArrayList<GroupMember>();
		GroupRequest groupRequest = new GroupRequest(subscriberId, null, "-1"); //If predefinedGroupId is set as -1 it will return all the contacts from all the groups
		Group[] groupObjects = client.getGroups(groupRequest);
		if (groupObjects != null) {
			for (Group group : groupObjects) {
				if (group.getGroupMembers() != null && group.getGroupMembers().length > 0) {
					groupMemberList.addAll(Arrays.asList(group.getGroupMembers()));
				}
			}
		}
		GroupMemberNameComparator comparator = new GroupMemberNameComparator();
		Collections.sort(groupMemberList, comparator);
		logger.debug("Returning groupMemberList: " + groupMemberList);
		return groupMemberList;
	}

	public String removeContactMemebers(String subscriberId, String callerId, String predefinedGroupId, String groupId, String mode) {
		if(!Utility.isStringValid(predefinedGroupId)) {
			logger.debug("predefinedGroupId empty or null. So set as 98.");
			predefinedGroupId = "98";
		}
		if (!Utility.isStringValid(groupId)) {
			logger.debug("groupId empty or null. Obtaing groupId for the predefinedGroupId: " + predefinedGroupId);
			GroupRequest getGroupFromPredfinedGroupIdRequest = new GroupRequest(subscriberId, null, predefinedGroupId);
			Group groupObj = client.getGroup(getGroupFromPredfinedGroupIdRequest);
			groupId = groupObj.getGroupID();
			logger.debug("groupId: " + groupId);
		}
		GroupRequest removeContactRequest = new GroupRequest(subscriberId, null, predefinedGroupId, callerId, null);
		removeContactRequest.setGroupID(groupId);
		client.removeGroupMember(removeContactRequest);
		logger.info("removeContactRequest response: " + removeContactRequest.getResponse());
		if(predefinedGroupId.equals("98")) {
			SelectionRequest selRequest = new SelectionRequest(subscriberId);
			selRequest.setMode(mode);
			selRequest.setCallerID(callerId);
			logger.info("selRequest::::::"+selRequest);
			client.deleteSubscriberSelection(selRequest);
			logger.info("Delete selection response: " + selRequest.getResponse());
		}
		return removeContactRequest.getResponse();
	}


	public String removeMultipleContactMemebers(String subscriberId, String callerId, String predefinedGroupId, String groupId, String mode) {
		GroupRequest groupRequest = null;
		if(!Utility.isStringValid(predefinedGroupId)) {
			logger.debug("predefinedGroupId empty or null. So set as 98.");
			predefinedGroupId = "98";
		}
		if (!Utility.isStringValid(groupId)) {
			logger.debug("groupId empty or null. Obtaing groupId for the predefinedGroupId: " + predefinedGroupId);
			GroupRequest groupRequest1 = new GroupRequest(subscriberId, null, predefinedGroupId);
			Group groupObj = client.getGroup(groupRequest1);

			groupRequest = new GroupRequest(subscriberId, null, predefinedGroupId, callerId, null);
			groupId = groupObj.getGroupID();
			logger.debug("groupId: " + groupId);
		} else {
			logger.debug("groupId: " + groupId);
			groupRequest = new GroupRequest(subscriberId, groupId, callerId, null);
		}
		client.removeGroupMultipleMember(groupRequest);
		if(predefinedGroupId.equals("98")) {
			SelectionRequest selRequest = new SelectionRequest(subscriberId);
			selRequest.setMode(mode);
			selRequest.setCallerID(callerId);
			logger.info("selRequest::::::"+selRequest);
			client.deleteSubscriberSelection(selRequest);
		}
		return groupRequest.getResponse();
	}


	public String moveContactMemebers(String subscriberId, String callerId, String sourcePreGroupId, String sourceGroupId, String destPreGroupId, String destGroupId, String mode) {
		PredefinedGroup[] predefinedGroups = getAllPredefinedGroups();
		String srcGrpName = "";
		String dstGrpName = "";
		logger.info("predefinedGroups:::::---->"+predefinedGroups);
		for(PredefinedGroup predefinedGroup : predefinedGroups) {
			if(predefinedGroup.getGroupID().equals(sourcePreGroupId)) {
				srcGrpName = predefinedGroup.getGroupName();
			} else if(predefinedGroup.getGroupID().equals(destPreGroupId)) {
				dstGrpName = predefinedGroup.getGroupName();
			}
		}
		Group destGroupObj = null;
		Group srcGroupObj = null;
		logger.info("srcGrpName::::::"+srcGrpName + "dstGrpName::::::"+dstGrpName);

		if (!Utility.isStringValid(destGroupId)) {
			logger.debug("Null or empty destGroupId, using destPreGroupId.");
			GroupRequest grpRequestDst = new GroupRequest(subscriberId, dstGrpName, destPreGroupId);
			if(destPreGroupId.equals("98")) {
				destGroupObj = client.getGroup(grpRequestDst);
				logger.info("DestGroupId: " + destGroupObj);
			} else {
				client.addGroup(grpRequestDst);
				logger.info("Predefined group Id is non-98.");
				if(grpRequestDst.getResponse().equalsIgnoreCase("success") || 
						grpRequestDst.getResponse().equalsIgnoreCase("ALREADY_EXISTS")) {
					destGroupObj = client.getGroup(grpRequestDst);
					logger.info("destGroupObj: " + destGroupObj);
				}
			}
		} else {
			logger.debug("Non-empty destGroupId.");
			GroupRequest grpRequestDst = new GroupRequest(subscriberId, destGroupId);
			destGroupObj = client.getGroup(grpRequestDst);
		}


		if (!Utility.isStringValid(sourceGroupId)) {
			logger.debug("Null or empty sourceGroupId, using sourcePreGroupId.");
			GroupRequest srcGrpRequest = new GroupRequest(subscriberId, srcGrpName, sourcePreGroupId);
			if(sourcePreGroupId.equals("98")) {
				srcGroupObj = client.getGroup(srcGrpRequest);
				logger.info("98 src group id::::::"+srcGroupObj);
			} else {
				logger.debug("Valid destGroupId.");
				client.addGroup(srcGrpRequest);
				logger.info("other 98 src group id::::::");
				if(srcGrpRequest.getResponse().equalsIgnoreCase("success") || 
						srcGrpRequest.getResponse().equalsIgnoreCase("ALREADY_EXISTS")) {
					srcGroupObj = client.getGroup(srcGrpRequest);
					logger.info("other src group id::::::"+srcGroupObj);
				}
			}
		} else {
			logger.debug("Non-empty sourceGroupId.");
			GroupRequest srcGrpRequest = new GroupRequest(subscriberId, sourceGroupId);
			srcGroupObj = client.getGroup(srcGrpRequest);
		}

		logger.info("destGroupObj: " + destGroupObj);
		logger.info("srcGroupObj: " + srcGroupObj);

		String destGrpId = null;
		if(destGroupObj != null) {
			destGrpId = destGroupObj.getGroupID();
			logger.info("destGrpId:" + destGrpId);
		} 
		String srcGrpId = null;
		if(srcGroupObj != null) {
			srcGrpId = srcGroupObj.getGroupID();
			logger.info("srcGrpId: " + srcGrpId);
		} 
		GroupRequest groupRequest = new GroupRequest(subscriberId, srcGrpId, null, null, callerId, null, destGrpId);
		groupRequest.setMode(mode);
		GroupDetails groupDetails = client.moveGroupMember(groupRequest);
		logger.info("groupDetails: " + groupDetails);

		if(sourcePreGroupId != null && sourcePreGroupId.equals("98") && groupRequest.getResponse().equalsIgnoreCase("SUCCESS")) {
			SelectionRequest deleteSelRequest = new SelectionRequest(subscriberId);
			deleteSelRequest.setMode(mode);
			deleteSelRequest.setCallerID(callerId);
			logger.info("deleteSubscriberSelectionRequest: "+deleteSelRequest);
			client.deleteSubscriberSelection(deleteSelRequest);
			logger.info("deleteSubscriberSelectionRequest response: " + deleteSelRequest.getResponse());
			logger.info("deleteSubscriberSelectionRequest response: " + deleteSelRequest.getResponse());
		}
		logger.info("groupRequest response: " + groupRequest.getResponse());
		return groupRequest.getResponse();
	}

	public List<ExtendedGroupBean> listAllPrdefinedGroupwithMember(String subscriberId, String groupId, String preGrpName) {
		PredefinedGroup[] predefinedGroups = getAllPredefinedGroups();
		GroupRequest groupRequest = new GroupRequest(subscriberId, preGrpName, groupId);
		Group groupObj = client.getGroup(groupRequest);
		List<ExtendedGroupBean> extendedGrpList = new ArrayList<ExtendedGroupBean>();
		if (predefinedGroups != null) {
			for(PredefinedGroup predefinedGroup :predefinedGroups) {
				logger.info("groupObj---->"+groupObj);
				if(predefinedGroup.getGroupID().equals(groupId)) {
					if (groupObj != null && groupObj.getGroupMembers() != null && groupObj.getGroupMembers().length > 0) {
						GroupMemberNameComparator comparator = new GroupMemberNameComparator();
						Arrays.sort(groupObj.getGroupMembers(), comparator);
					}
					ExtendedGroupBean groupExt = new ExtendedGroupBean(predefinedGroup, groupObj);
					extendedGrpList.add(groupExt);
				}
			}
		}
		logger.info("extendedGrpList----->"+extendedGrpList);
		return extendedGrpList;
	}


	/*public List<ExtendedGroupBean> listAllPrdefinedGroupwithMember(String subscriberId, String groupId) {
		PredefinedGroup[] predefinedGroups = getPredefinedGroups();
		RbtDetailsRequest groupRequest = new RbtDetailsRequest(subscriberId);
		GroupDetails grpDet = client.getGroupDetails(groupRequest);
		List<ExtendedGroupBean> extendedGrpList = new ArrayList<ExtendedGroupBean>();
		Group[] groups = null;
		List<Group> grpList = new ArrayList<Group>();
		if (grpDet != null && grpDet.getGroups() != null && grpDet.getGroups().length > 0) {
		     groups = grpDet.getGroups();
		     grpList =  Arrays.asList(groups);
		 }
		 for(PredefinedGroup predefinedGroup :predefinedGroups) {
			 ExtendedGroupBean groupExt = new ExtendedGroupBean(predefinedGroup);
			 for(Group group : grpList) {
				 if(predefinedGroup.getGroupID().equals(group.getGroupID())) {
					 groupExt.setGroupMembers(group.getGroupMembers());
				     grpList.remove(group);
				  }
			  }
			 extendedGrpList.add(groupExt);
		 }
		return extendedGrpList;
	}
	 */	

	public String getNotificationStatus(String subscriberId, String os_type, String regId) {
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setSubscriberID(subscriberId);
		applicationDetailsRequest.setInfo(WebServiceConstants.action_get);
		applicationDetailsRequest.setType(os_type);
		applicationDetailsRequest.setUserID(regId);
		String response = client.getOrSetNotificationStatus(applicationDetailsRequest);
		if (response.equals(WebServiceConstants.ERROR)) {
			response = "failure";
		}
		return response;
	}

	public String setNotificationStatus(String subscriberId, Boolean status, String os_type, String regId) {
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setSubscriberID(subscriberId);
		applicationDetailsRequest.setInfo(WebServiceConstants.action_set);
		applicationDetailsRequest.setValue(status.toString());
		applicationDetailsRequest.setType(os_type);
		applicationDetailsRequest.setUserID(regId);
		String response = client.getOrSetNotificationStatus(applicationDetailsRequest);
		if (response.equals(WebServiceConstants.ERROR)) {
			response = "failure";
		}
		return response;
	}

	public OfferBean getOffer(String subscriberId, String clipId,
			String offerType, String mode, String browsingLangauge , String appName) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);		
		rbtDetailsRequest.setOfferType(offerType);
		rbtDetailsRequest.setMode(mode);
		Offer[] offers = null;
		if (PropertyConfigurator.getPackageOfferSupported() != null && PropertyConfigurator.getPackageOfferSupported().equalsIgnoreCase("TRUE")) {
			// RBT-15120
			rbtDetailsRequest.setClipID(clipId);
			offers = client.getPackageOffer(rbtDetailsRequest);
		} else {
			// RBT-15120
			HashMap<String, String> extraInfoMap = new HashMap<String, String>();
			extraInfoMap.put("CLIP_ID", clipId);
			rbtDetailsRequest.setExtraInfoMap(extraInfoMap);
			offers = client.getOffers(rbtDetailsRequest);			
		}
		logger.info("Offers: " + offers);
		if (offers != null && offers.length > 0) {
			Offer offer = offers[0];
			if (offer != null) {
				OfferBean offerBean = new OfferBean();
				offerBean.setSubscriberId(subscriberId);
				offerBean.setOfferId(offer.getOfferID());
				offerBean.setSrvKey(offer.getSrvKey());
				offerBean.setAmount(offer.getAmount());
				offerBean.setDescription(PropertyConfigurator.getOfferDescription(offerType, offer.getSrvKey(), browsingLangauge , appName));
				offerBean.setOfferValidity(offer.getOfferValidity());
				offerBean.setOfferRenewalAmount(offer.getOfferRenewalAmount());
				offerBean.setOfferRenewalValidity(offer.getOfferRenewalValidity());
				offerBean.setSmOfferType(offer.getSmOfferType());
				logger.info("offerBean: " + offerBean);
				return offerBean;
			}
		}
		logger.info("offerBean: " + null);
		return null;
	}

	public SelectionDetailsBean getSelectionDetails(String subscriberId, String clipId,
			String categoryId, String channel, String chargeClassString,
			String musicPack, String profilePack, boolean useUIChargeClass) {
		SelectionRequest selectionRequest = new SelectionRequest(subscriberId,new Boolean(true),null,channel,categoryId,clipId,null,null);
		selectionRequest.setChargeClass(chargeClassString);
		selectionRequest.setUseUIChargeClass(useUIChargeClass);

		// added this check for webservice to distinguish the recorded clips and return configured amount.
		if (clipId.endsWith(".3gp") || clipId.endsWith(".wav")) {
			selectionRequest.setSelectionType(99);
		}
		try {
			boolean isMusicPack = Boolean.parseBoolean(musicPack);
			boolean isProfilePack = Boolean.parseBoolean(profilePack);
			if (isMusicPack) {
				String musicPackCosId = PropertyConfigurator.getMusicPackCosId();
				logger.debug("cosId set as musicPackCosId: " + musicPackCosId);
				selectionRequest.setCosID(Integer.parseInt(musicPackCosId));
			} else if (isProfilePack) {
				String chargeClassForProfile = PropertyConfigurator.getChargeClassForProfile();
				logger.info("chargeClassForProfile :==>"+chargeClassForProfile);
				if(chargeClassForProfile != null && chargeClassForProfile.length() > 0) {
					selectionRequest.setUseUIChargeClass(true);
					selectionRequest.setChargeClass(chargeClassForProfile);
				} else {
					String profileCosId = PropertyConfigurator.getProfileCosId();
					String normalProfileCosId = PropertyConfigurator.getNormalProfileCosId();
					if (profileCosId != null) {
						logger.debug("cosId in selectionRequest set as profileCosId: " + profileCosId);
						selectionRequest.setCosID(Integer.parseInt(profileCosId));
					} else if (normalProfileCosId != null) {
						logger.debug("cosId in selectionRequest set as normalProfileCosId: " + normalProfileCosId);
						selectionRequest.setCosID(Integer.parseInt(normalProfileCosId));
					}
				} 	
			}
		} catch(Exception e){
			logger.error("Error caught: " + e, e);
		}
		logger.info("getNextChargeClassForRMO selection request : " + selectionRequest);
		ChargeClass chargeClass = (ChargeClass)client.getNextChargeClassForRMO(selectionRequest);
		logger.info("chargeClass: " + chargeClass);
		String amount = PropertyConfigurator.getDefaultPriceAmount();
		String period = PropertyConfigurator.getDefaultSelectionPeriod();
		if (chargeClass != null) {
			amount = chargeClass.getAmount();
			period = chargeClass.getPeriod();
			chargeClassString = chargeClass.getChargeClass();
		} else {
			logger.debug("chargeClass is null. Hence using the default valuse configured.");
		}
		SelectionDetailsBean selectionDetailsBean = new SelectionDetailsBean();
		selectionDetailsBean.setAmount(amount);
		selectionDetailsBean.setChargeClass(chargeClassString);
		selectionDetailsBean.setPeriod(period);
		String periodDescription = PropertyConfigurator.getSelectionPeriodDescription(period);
		if (periodDescription == null) {
			logger.debug("No configuration found for webservice period response: " + periodDescription);
			periodDescription = period;
		}
		selectionDetailsBean.setPeriodDescription(periodDescription);
		logger.info("subscriberId: " + subscriberId + ", clipId: " + clipId + ", categoryId: " + categoryId + ", selectionDetailsBean: " + selectionDetailsBean);
		return selectionDetailsBean;
	}


	public SubscriptionDetailsBean getSubscriptionDetails(Subscriber subscriber, String subscriberId) {
		logger.info("getSubscriptionDetails call. subscriberId: " + subscriberId + ", subscriber: " + subscriber);
		SubscriptionDetailsBean subscriptionDetailsBean = new SubscriptionDetailsBean();
		String circleId = null;
		String amount = null;
		String period = null;
		if (subscriber == null && subscriberId != null) {
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
			subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
			logger.info("subscriber: " + subscriber);
		}
		if (subscriber != null) {
			circleId = subscriber.getCircleID();
		}
		if (Utility.isStringValid(circleId)) {
			amount = PropertyConfigurator.getSubscriptionAmount(circleId);
			period = PropertyConfigurator.getSubscriptionPeriod(circleId);
		} 

		if (!Utility.isStringValid(amount)) {
			logger.debug("subscriberId: " + subscriberId + ". Configured amount is empty/null. Using default configuration.");
			amount = PropertyConfigurator.getSubscriptionAmount();
		}
		if (!Utility.isStringValid(period)) {
			logger.debug("subscriberId: " + subscriberId + ". Configured period is empty/null. Using default configuration.");
			period = PropertyConfigurator.getSubscriptionPeriod();
		}
		subscriptionDetailsBean.setAmount(amount);
		subscriptionDetailsBean.setPeriod(period);

		logger.info("subscriberId: " + subscriberId + ", subscriptionDetailsBean: " + subscriptionDetailsBean);
		return subscriptionDetailsBean;
	}

	public GetCurrentPlayingSongBean getCurrentPlayingSong(String subscriberId,
			String callerId, String browsingLanguage, String appName,
			String mode, String callType, StringBuffer responseCode) {
		logger.info("subscriberId: " + subscriberId + ", callerId: " + callerId
				+ ", browsingLanguage: " + browsingLanguage + ", appName: "
				+ appName + ", mode: " + mode + ", callType: " + callType);
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setSubscriberID(subscriberId);
		applicationDetailsRequest.setCallerID(callerId);
		applicationDetailsRequest.setInfo(callType);
		CurrentPlayingSongWSResponseBean responseBean = client.getCurrentPlayingSong(applicationDetailsRequest);
		logger.info("responseBean: " + responseBean);
		String responseStatus = responseBean.getResponseStr();
		GetCurrentPlayingSongBean currentPlayingSongbean = null;
		
		List<String> nonSupportedFeatures =  new ArrayList<String>();
		nonSupportedFeatures = PropertyConfigurator.getClipTypes(); 
		if(nonSupportedFeatures.contains(responseBean.getWavFileName().toLowerCase())){
			currentPlayingSongbean = 	CurrentPlayingSongUtility.getDummyCurrentPlayingResponse(responseBean.getWavFileName(),-1);
			responseCode.append("200");
		}
		else if (responseStatus != null && responseStatus.equalsIgnoreCase("SUCCESS")) {
			currentPlayingSongbean = CurrentPlayingSongUtility.getCurrentPlayingSongResponse(responseBean.getWavFileName(), browsingLanguage, appName,responseBean.getCategoryId());
			if (currentPlayingSongbean != null
					&& currentPlayingSongbean.getClip() != null
					&& currentPlayingSongbean.getCategory() != null) {
				responseCode.append("200");
			}
		} else {
			responseCode.append(responseBean.getResponseCode());
		}
		logger.info("currentPlayingSongbean: " + currentPlayingSongbean);
		return currentPlayingSongbean;
	}
	
	public ExtendedClipBean getCurrentPlayingSongOnly(String subscriberId,
			String callerId, String browsingLanguage, String appName,
			String mode, String callType, StringBuffer responseCode) {
		logger.info("subscriberId: " + subscriberId + ", callerId: " + callerId
				+ ", browsingLanguage: " + browsingLanguage + ", appName: "
				+ appName + ", mode: " + mode + ", callType: " + callType);
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setSubscriberID(subscriberId);
		applicationDetailsRequest.setCallerID(callerId);
		applicationDetailsRequest.setInfo(callType);
		CurrentPlayingSongWSResponseBean responseBean = client
				.getCurrentPlayingSong(applicationDetailsRequest);
		String responseStatus = applicationDetailsRequest.getResponse();
		ExtendedClipBean clip = null;
		List<String> nonSupportedFeatures = new ArrayList<String>();
		nonSupportedFeatures = PropertyConfigurator.getClipTypes();
		if (nonSupportedFeatures.contains(responseBean.getWavFileName()
				.toLowerCase())) {
			clip = ClipUtils.getDummyClipByResponseString("-1",
					responseBean.getWavFileName(), "-1", "");
			responseCode.append("200");
		} else if (responseStatus != null
				&& responseStatus.equalsIgnoreCase("SUCCESS")
				&& Utility.isStringValid(responseBean.getWavFileName())) {
			clip = CurrentPlayingSongUtility.getCurrentPlayingSongResponse(
					responseBean.getWavFileName(), browsingLanguage, appName);
			if (clip != null && clip.getClipName() != null) {
				responseCode.append("200");
			}
		} else {
			responseCode.append(responseBean.getResponseCode());
		}
		return clip;
	}

	/**
	 * RBT-14051:: Mobile app server: API to retrieve active downloads along with their active setting details done for tef-spain
	 * @param subscriberId
	 * @return
	 */
	public List<ExtendedDownloadBean> getDownloadsWithSelections(String subscriberId) {
		logger.info("subscriberId: " + subscriberId);
		RbtDetailsRequest detailRequest = new RbtDetailsRequest(subscriberId, null);
		Library library = client.getLibrary(detailRequest);
		Downloads downloads = null;
		Settings settings = null;
		List<ExtendedDownloadBean> downloadList =  new ArrayList<ExtendedDownloadBean>();
		if (library != null) {
			downloads = library.getDownloads();
			settings = library.getSettings();
			Map<Integer, List<Setting>> playlistSettingsMap = new HashMap<Integer, List<Setting>>();
			Map<String, List<Setting>> nonPlaylistSettingsMap = new HashMap<String, List<Setting>>();
			if (settings != null && settings.getSettings() != null) {
				for (Setting setting : settings.getSettings()) {
					int categoryId = setting.getCategoryID();
					Category category = RBTCacheManager.getInstance().getCategory(categoryId);
					if (category != null
							&& (category.getCategoryTpe() == iRBTConstant.ODA_SHUFFLE || 
							category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE)) {
						List<Setting> playlistSettingList = playlistSettingsMap.get(categoryId);
						if (playlistSettingList == null) {
							playlistSettingList = new ArrayList<Setting>();
							playlistSettingsMap.put(categoryId, playlistSettingList);
						}
						playlistSettingList.add(setting);
					} else {
						String subscriberWavFile = setting.getRbtFile();
						List<Setting> nonPlayistSettingList = nonPlaylistSettingsMap.get(subscriberWavFile);
						if (nonPlayistSettingList == null) {
							nonPlayistSettingList = new ArrayList<Setting>();
							nonPlaylistSettingsMap.put(subscriberWavFile, nonPlayistSettingList);
						}
						nonPlayistSettingList.add(setting);
					}
				}
			}
			logger.debug("playListSettingsMap: " + playlistSettingsMap);
			logger.debug("nonPlayListSettingsMap: " + nonPlaylistSettingsMap);

			if (downloads != null && downloads.getDownloads() != null ) {
				for (Download download : downloads.getDownloads()) {
					ExtendedDownloadBean extDownloadBean = new ExtendedDownloadBean(download, true);
					ExtendedClipBean clipObj = ClipUtils.getExtendedClipByClipId(String.valueOf(download.getToneID()), null, null);
					extDownloadBean.setAlbum(clipObj.getAlbum());
					extDownloadBean.setArtist(clipObj.getArtist());
					//RBT-14626
					extDownloadBean.setClipInfo(clipObj.getClipInfo());
					downloadList.add(extDownloadBean);

					int categoryId = download.getCategoryID(); 
					Category category = RBTCacheManager.getInstance().getCategory(categoryId);
					String imagePath = Utility.getImagePath(category, clipObj);
					extDownloadBean.setImageFilePath(imagePath);
					if (category != null
							&& (category.getCategoryTpe() == iRBTConstant.ODA_SHUFFLE || 
							category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE)) {
						extDownloadBean.setToneType(CategoryUtils.getCategoryTypeStringValue(category.getCategoryTpe()));
						//RBT-14626
						Clip[] activeClipsInCategory = RBTCacheManager.getInstance().getActiveClipsInCategory(categoryId);
						extDownloadBean.setODP(String.valueOf(activeClipsInCategory.length));
						
						if (playlistSettingsMap.containsKey(categoryId)) {
							List<Setting> playlistSettings = playlistSettingsMap.get(categoryId);
							for (Setting playListSetting : playlistSettings) {
								Utility.addSettingToDownloadBean(extDownloadBean, playListSetting);
							}
							playlistSettingsMap.remove(categoryId);
						}			
					} else {
						extDownloadBean.setToneType(CategoryUtils.getCategoryTypeStringValue(category.getCategoryTpe()));
						if (nonPlaylistSettingsMap.containsKey(download.getRbtFile())) {
							List<Setting> nonPlaylistSettings = nonPlaylistSettingsMap.get(download.getRbtFile());
							for (Setting nonPlaylistSetting : nonPlaylistSettings) {
								Utility.addSettingToDownloadBean(extDownloadBean, nonPlaylistSetting);
							}
							nonPlaylistSettingsMap.remove(download.getRbtFile());
						}
					}
					logger.debug("extDownloadBean: " + extDownloadBean);
				}
				logger.debug("playListSettingsMap (after downloads have been considered): " + playlistSettingsMap);
				logger.debug("nonPlayListSettingsMap (after downloads have been considered): " + nonPlaylistSettingsMap);

				if (!playlistSettingsMap.isEmpty()) {
					for (List<Setting> settingList : playlistSettingsMap.values()) {
						ExtendedDownloadBean extDownloadBean = new ExtendedDownloadBean(settingList.get(0));
						ExtendedClipBean clipObj = ClipUtils.getExtendedClipByClipId(String.valueOf(settingList.get(0).getToneID()), null, null);
						extDownloadBean.setAlbum(clipObj.getAlbum());
						extDownloadBean.setArtist(clipObj.getArtist());
						int categoryId = settingList.get(0).getCategoryID();
						Category category = RBTCacheManager.getInstance().getCategory(categoryId);
						//RBT-14626
						Clip[] activeClipsInCategory = RBTCacheManager.getInstance().getActiveClipsInCategory(categoryId);
						extDownloadBean.setODP(String.valueOf(activeClipsInCategory.length));
						
						String imagePath = Utility.getImagePath(category, clipObj);
						extDownloadBean.setImageFilePath(imagePath);
						extDownloadBean.setToneType(CategoryUtils.getCategoryTypeStringValue(category.getCategoryTpe()));
						for (Setting setting : settingList) {
							Utility.addSettingToDownloadBean(extDownloadBean, setting);
						}
						downloadList.add(extDownloadBean);
						logger.debug("extDownloadBean from nonPlayListSettingsMap: " + extDownloadBean);
					}
				}

				if (!nonPlaylistSettingsMap.isEmpty()) {
					for (List<Setting> settingList : nonPlaylistSettingsMap.values()) {
						ExtendedDownloadBean extDownloadBean = new ExtendedDownloadBean(settingList.get(0));
						ExtendedClipBean clipObj = ClipUtils.getExtendedClipByClipId(String.valueOf(settingList.get(0).getToneID()), null, null);
						extDownloadBean.setAlbum(clipObj.getAlbum());
						extDownloadBean.setArtist(clipObj.getArtist());
						//RBT-14626
						extDownloadBean.setClipInfo(clipObj.getClipInfo());
						int categoryId = settingList.get(0).getCategoryID();
						Category category = RBTCacheManager.getInstance().getCategory(categoryId);
						String imagePath = Utility.getImagePath(category, clipObj);
						extDownloadBean.setImageFilePath(imagePath);
						extDownloadBean.setToneType(CategoryUtils.getCategoryTypeStringValue(category.getCategoryTpe()));
						for (Setting setting : settingList) {
							Utility.addSettingToDownloadBean(extDownloadBean, setting);
						}
						downloadList.add(extDownloadBean);
						logger.debug("extDownloadBean from nonPlayListSettingsMap: " + extDownloadBean);
					}
				}
			}
		}
		logger.info("downloadList" + downloadList);
		return downloadList;
	}

	public Map<String, String> getParamForType(String type, String param) {
		Map<String, String> paramMap = new HashMap<String, String>();
		List<String> supportedParamTypes = PropertyConfigurator.getSupportedParamTypes();
		if (Utility.isStringValid(type)
				&& supportedParamTypes.contains(type)) {
			if (!Utility.isStringValid(param) || param.equalsIgnoreCase("all")) {
				paramMap = ParamForTypeUtils.getAllPropertiesForType(type);
			} else {
				paramMap = ParamForTypeUtils.getPropertyOfType(type, param);
			}
		} else {
			logger.info("Invalid type value. type: " + type + ", supported param types: " + supportedParamTypes);
		}
		return paramMap;
	}
	
	public boolean refreshAllParamsInMemcache() {
		List<String> supportedParamTypes = PropertyConfigurator.getSupportedParamTypes();
		for (String type : supportedParamTypes) {
			Map<String,String> paramMap = ParamForTypeUtils.hitWebServiceForParams(type);
			boolean isUpdated = ParamForTypeUtils.addParamMapToMemcache(type, paramMap);
			if (!isUpdated) {
				return false;
			}
		}
		return true;
	}
	
		
	//RBT-14626	Signal app requirement - Mobile app server API enhancement (phase 2)
	public String removeGroup(String subscriberId, String groupId, String mode) {
		if (!Utility.isStringValid(groupId)) {
			logger.info("groupId is not valid: " + groupId);
			return "faliure";
		}
		GroupRequest removeGroup = new GroupRequest(subscriberId, groupId);
		client.removeGroup(removeGroup);
		logger.info("removeGroup response: " + removeGroup.getResponse());
		return removeGroup.getResponse();
	}
	//RBT-14626	Signal app requirement - Mobile app server API enhancement (phase 2)
	public String getCallLogHistory(String subscriberId, String mode,
			String callType, String pageSize, String offset){
		CallLogRestRequest callLogRestRequest = new CallLogRestRequest(subscriberId);
		callLogRestRequest.setCallType(callType);
		if(pageSize !=null){
		 callLogRestRequest.setPageSize(Integer.parseInt(pageSize));
		} 
		if(offset != null){
		 callLogRestRequest.setOffSet(Integer.parseInt(offset));
		}
		callLogRestRequest.setRestRequest(true);
		return client.getCallLogHistory(callLogRestRequest);
	}
	
	//RBT-16263	Unable to remove selection for local/site RBT user
	public static SubscriberManager getSubscriberManagerObj(){
		boolean rdcParam = PropertyConfigurator.getRDCParam();
		if(rdcParam){
			return new ExtentedSubscriberManager();
		}
		return new SubscriberManager();
	}
	
	
	public String removeSubscriberDownload(String subscriberId, String clipId, String caller, String channel, String fromTime,
			String toTime, String circleId, String refID, String catId, String fromTimeMinutes,
			String toTimeMinutes) {
		logger.info("clip ID " + clipId + " channel " + channel + "caller " + caller);
		SelectionRequest selRequest = new SelectionRequest(subscriberId, clipId);
		selRequest.setMode(channel);
		selRequest.setCallerID(caller);
		if (clipId != null
				&& (clipId.endsWith(".3gp") || clipId.endsWith(".wav") || clipId
						.contains("_"))) {
			selRequest.setRbtFile(clipId);
		} else {
			selRequest.setClipID(clipId);
		}

		if (fromTime != null && !fromTime.isEmpty()) {
			selRequest.setFromTime(Integer.parseInt(fromTime));
		}

		if (toTime != null && !toTime.isEmpty()) {
			selRequest.setToTime(Integer.parseInt(toTime));
		}

		if (fromTimeMinutes != null && !fromTimeMinutes.isEmpty()) {
			selRequest.setFromTimeMinutes(Integer.parseInt(fromTimeMinutes));
		}

		if (toTimeMinutes != null && !toTimeMinutes.isEmpty()) {
			selRequest.setToTimeMinutes(Integer.parseInt(toTimeMinutes));
		}

		if (circleId != null && !circleId.isEmpty())
			selRequest.setCircleID(circleId);
		// RBT-16263 Unable to remove selection for local/site RBT user
		selRequest.setRedirectionRequired(true);
		selRequest.setCategoryID(catId);
		if (refID != null && !refID.isEmpty()) {
			selRequest.setRefID(refID);
		}

		client.deleteSubscriberDownload(selRequest);
		logger.info("RBT:: deleteSubscriberDownload " + selRequest.getResponse());
		return selRequest.getResponse();
	}
}