package com.onmobile.mobileapps.actions;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.onmobile.android.beans.SubInfo;
import com.onmobile.android.beans.SubscriptionDetailsBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.factory.ResponseFactory;
import com.onmobile.android.interfaces.SubscriberResponse;
import com.onmobile.android.managers.SubscriberManager;
import com.onmobile.android.utils.ObjectGsonUtils;
import com.onmobile.android.utils.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class SubscriberAction extends DispatchAction{
	public static Logger logger = Logger.getLogger(SubscriberAction.class);

	public ActionForward getSubscriberId(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SubscriberAction, getSubscriberId method");

		@SuppressWarnings("unchecked")
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			logger.info("Header name: " + headerName + ", Header value: " + request.getHeader(headerName));
		}

		String UID = request.getParameter("uniqueId");
		String circleId = request.getParameter("circleId");
		String getCircleIdString = request.getParameter("getCircleId");
		String userAgent = request.getHeader("user-agent");
		boolean getCircleId = Boolean.valueOf(getCircleIdString);
		String reqMsisdn = null;
		String registrationsource = null;

		List<String> strHeaderNames = PropertyConfigurator.getMsisdnHeadersName();
		if(strHeaderNames != null && strHeaderNames.size() > 0) {
			for(String headerName : strHeaderNames) {
				String temp = request.getHeader(headerName);
				if(temp != null) {
					reqMsisdn = temp;
					break;
				}
			}
		}
		if (null != reqMsisdn) {
			registrationsource = "GPRS";
		} else {
			registrationsource = "WIFI";
		}
		logger.info("uniqueId: " + UID + ", circleId: " + circleId
				+ ", subscriberId: " + reqMsisdn + ", getCircleId: "
				+ getCircleId + ", registrationsource: " + registrationsource
				+ ", userAgent: " + userAgent);
		String s2 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL)
				.getMSISDN(UID, reqMsisdn, circleId, getCircleId, registrationsource, userAgent);
		request.setAttribute("response", s2);
		logger.info("response: " + s2);
		return mapping.findForward("success"); 
	}

	public ActionForward isSubscribed(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SubscriberAction, isSubscribed method");
		String subId = request.getParameter("subscriberId");
		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getSubscriberStatus(subId);
		request.setAttribute("response", s2);
		logger.info("response: " + s2);
		return mapping.findForward("success"); 
	}

	public ActionForward isVoluntarilySuspended(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SubscriberAction, isVoluntarilySuspended");
		String subId = request.getParameter("subscriberId");
		String s2 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getVoluntarySuspendedStatus(subId);
		request.setAttribute("response", s2);
		logger.info("response: " + s2);
		return mapping.findForward("success"); 
	}

	public ActionForward suspendSubscription(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SubscriberAction, suspendSubscription");
		String subId = request.getParameter("subscriberId");
		String s2 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).processSuspension(subId, true);
		request.setAttribute("response", s2);
		logger.info("response: " + s2);
		return mapping.findForward("success"); 
	}

	public ActionForward resumeSubscription(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SubscriberAction, resumeSubscription");
		String subId = request.getParameter("subscriberId");
		String s2 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).processSuspension(subId, false);
		request.setAttribute("response", s2);
		logger.info("response: " + s2);
		return mapping.findForward("success"); 
	}

	public ActionForward getSubscriberInfo(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SubscriberAction, getSubscriberId method");
		String subId = request.getParameter("subscriberId");
		String uid = request.getParameter("uniqueId");
		String circleId = request.getParameter("circleID");
		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getSubscriberInfo(subId, uid, circleId);
		request.setAttribute("response", s2);
		logger.info("response: " + s2);
		return mapping.findForward("success"); 
	}

	public ActionForward isProfilePackActivated(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SubscriberAction, isProfileActivatedMethod method");
		String subId = request.getParameter("subscriberId");
		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).isProfilePackActivated(subId);
		request.setAttribute("response", s2);
		logger.info("response: " + s2);
		return mapping.findForward("success"); 
	}

	public ActionForward isPackActivated(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws OMAndroidException {
		logger.info("Inside SubscriberAction, isPackActivated method");
		String subId = request.getParameter("subscriberId");
		String s2 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL)
				.isPackActivated(subId);
		request.setAttribute("response", s2);
		logger.info("response: " + s2);
		return mapping.findForward("success");
	}

	public ActionForward getHomePageDetails(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{

		logger.info("Inside getHomePageDetails");

		String subId = request.getParameter("subscriberId");
		String osType = request.getParameter("os_type");
		String language = request.getParameter("language");
		language = language != null ? language.trim() : null;
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		String maxResultsString = request.getParameter("maxResults");
		int maxResults = -1;
		if (maxResultsString != null) {
			try {
				maxResults = Integer.parseInt(maxResultsString);
			} catch (NumberFormatException e) {
				logger.info("subscriberId: " + subId + ". NumberFormatException caught. maxResultsString: " + maxResultsString);
				maxResults = -1;
			}
		}
		
		logger.info("subscriberId: " + subId + ", os_type: " + osType
				+ ", language: " + language + ", browsingLanguage: "
				+ browsingLanguage + ", appName: " + appName + ", maxResults: " + maxResults);
		List<SubInfo> subInfoList = new ArrayList<SubInfo>();

		String s2 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getCountOfAvailableMusicPackDownloads(subId);
		String giftInboxCountResponse=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getGiftInboxCount(subId);
		String deafaultCategory = ResponseFactory.getContentResponse(ResponseFactory.RESPONSE_TYPE_JSON).getMainCategory(subId, language, browsingLanguage, appName);
		String clipRowCount = PropertyConfigurator.getClipROwCount();
		String musicPackAmount = PropertyConfigurator.getMusicPackAmount();
		Boolean isHidePath = PropertyConfigurator.isHidePath();
		String imagePath = PropertyConfigurator.getImagePath();
		String previewPath = PropertyConfigurator.getPreviewPath();
		String giftAmount = PropertyConfigurator.getGiftAmount();
		Map<String, String> osTypeModeMap = PropertyConfigurator.getOsTypeModeMap();
		String currentVersion = PropertyConfigurator.getCurrentVersion();
		String mandatoryToUpgrade = PropertyConfigurator.getMandatoryToUpgrade();
		String consentReturnYesUrl = PropertyConfigurator.getConsentReturnYesUrl();
		String consentReturnNoUrl = PropertyConfigurator.getConsentReturnNoUrl();
		
		Subscriber subscriber = Utility.getSubscriber(subId);
		SubscriptionDetailsBean subscriptionDetailsBean = new SubscriberManager()
				.getSubscriptionDetails(subscriber, subId);
		String subscriptionAmount = subscriptionDetailsBean.getAmount();
		String subscriptionPeriod = subscriptionDetailsBean.getPeriod();
		String playlistsCategories = ResponseFactory.getContentResponse(ResponseFactory.RESPONSE_TYPE_JSON).getPlaylistsCategories(subId, browsingLanguage, appName);
		String otherPlaylistsCategory = ResponseFactory.getContentResponse(ResponseFactory.RESPONSE_TYPE_JSON).getOtherPlaylistsCategory(subId, browsingLanguage, appName);
		String freemiumCategory = null;
		String freemiumClips = null;
		if (subscriber != null && subscriber.isFreemiumSubscriber()) {
			freemiumCategory = ResponseFactory.getContentResponse(ResponseFactory.RESPONSE_TYPE_JSON).getFreemiumCategory(subId, browsingLanguage, appName);
			freemiumClips = ResponseFactory.getContentResponse(ResponseFactory.RESPONSE_TYPE_JSON).getFreemiumClips(subId, browsingLanguage, appName, maxResults);
		}
		logger.info("deafaultCategory=="+deafaultCategory);
		SubInfo subInfo = new SubInfo();
		try
		{
			if(s2 != null && s2.length() != 0) {
				subInfo.setAvailableDownloads(Integer.parseInt(s2));
			}
			if(clipRowCount !=null && clipRowCount.length() != 0) {
				subInfo.setClipRowCount(Integer.parseInt(clipRowCount));
			}
			subInfo.setDefaultCategory(deafaultCategory);
			if(giftInboxCountResponse != null && giftInboxCountResponse.length() !=0 ) {
				subInfo.setGiftInboxCount(Integer.parseInt(giftInboxCountResponse));
			}
			if (isHidePath != null && !isHidePath) {
				subInfo.setImagePath(imagePath);
				subInfo.setPreviewPath(previewPath);
			}
			subInfo.setGiftAmount(giftAmount);
			if(musicPackAmount != null)
				subInfo.setMusicPackAmount(Integer.parseInt(musicPackAmount));
			if (osType != null && osTypeModeMap != null && osTypeModeMap.containsKey(osType)) {
				subInfo.setMode(osTypeModeMap.get(osType));
			} else {
				subInfo.setMode(SubscriberResponse.CHANNEL);
			}
			subInfo.setCurrentVersion(currentVersion);
			subInfo.setMandatoryToUpgrade(mandatoryToUpgrade);
			subInfo.setConsentReturnYesUrl(consentReturnYesUrl);
			subInfo.setConsentReturnNoUrl(consentReturnNoUrl);
			subInfo.setSubscriptionAmount(subscriptionAmount);
			subInfo.setSubscriptionPeriod(subscriptionPeriod);
			subInfo.setPlaylistsCategories(playlistsCategories);
			subInfo.setOtherPlaylistsCategory(otherPlaylistsCategory);
			subInfo.setFreemiumCategory(freemiumCategory);
			subInfo.setFreemiumClips(freemiumClips);
		}
		catch(Exception e)
		{
			logger.error(e, e);
		}
		subInfoList.add(subInfo);
		String responseStr =  (ObjectGsonUtils.objectToGson(subInfoList));
		logger.info("responseStr="+responseStr);
		request.setAttribute("response", responseStr);
		return mapping.findForward("success"); 

	}

	public ActionForward deactivateProfilePack(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{

		logger.info("Inside deactivate Profile Pack");
		String subscriberId = request.getParameter("subscriberId");
		String deactivatePackResp=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).deactivateProfilePack(subscriberId);
		request.setAttribute("response", deactivatePackResp);
		logger.info("deactivatePackResponse " + deactivatePackResp);
		return mapping.findForward("success"); 
	}

	public ActionForward deactivateMusicPack(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside deactivate Music Pack");
		String subscriberId = request.getParameter("subscriberId");
		String deactivatePackResp=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).deactivateMusicPack(subscriberId);
		request.setAttribute("response", deactivatePackResp);
		logger.info("deactivatePackResponse " + deactivatePackResp);
		return mapping.findForward("success"); 
	}
}
