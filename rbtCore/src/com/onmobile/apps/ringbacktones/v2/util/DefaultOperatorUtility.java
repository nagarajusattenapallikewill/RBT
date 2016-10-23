package com.onmobile.apps.ringbacktones.v2.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ibm.vodafone.encrytion.EncryptionDecryptionUtil;
import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.http.HttpConfigurations;
import com.onmobile.apps.ringbacktones.rbt2.http.HttpHitUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category.CategoryInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip.ClipInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

public class DefaultOperatorUtility extends AbstractOperatorUtility {

	private static Logger logger = Logger.getLogger(DefaultOperatorUtility.class);
	private Map<String,String> thirdPartyCircleIdMap = null;
	public static final String KEY_FILESYSTEM_IMAGE_PATH = "filesystem.image.path";
	public static final String KEY_FILESYSTEM_CATEGORY_IMAGE_PATH = "filesystem.category.image.path";
	public static final String KEY_DEFAULT_IMAGE_PATH="default.image";
	public static final String KEY_IMAGE_PREVIEW_PATH = "image.preview.path";
	public static final String KEY_BASE_IMAGE_URL="base.image.url";
	public static final String KEY_CATEGORY_IMAGE_PREVIEW_PATH = "cateogry.image.preview.path";
	public static final String KEY_IMAGE_STREAM_URL = "image.streaming.url";
	protected static ResourceBundle resourceBundle;
	//private static Set<String> cvCircleId = null;
	
	/*static {
		try {
			//resourceBundle = ResourceBundle.getBundle("vodafone_config");
			initilizeCVCircle();
		} catch(MissingResourceException e) {
			logger.error("Exception Occured: "+e,e);
		}
	}
	*/
	protected static void initilizeCVCircle( Set<String> cvCircleId)
	{
		String circleIDs = getValueFromResourceBundle(resourceBundle,"CV_CIRCLE_ID", null);
		if (circleIDs != null && !circleIDs.isEmpty())
		{
			cvCircleId = new HashSet<String>();
			String[] circleIDArr = circleIDs.split(",");
			for (String circleId : circleIDArr)
			{
				cvCircleId.add(circleId);
			}
		}
	}
	
	
	@Override
	public Rbt addSubscriberConsentSelection(SelectionRequest selectionRequest) {
		Rbt rbt = RBTClient.getInstance().addSubscriberSelection(selectionRequest);
		return rbt;
	}

	@Override
	public Rbt addSubscriberConsentDownload(SelectionRequest selectionRequest) {
		logger.info("selectionRequest: " + selectionRequest);
		Rbt rbt = RBTClient.getInstance().addSubscriberDownload(selectionRequest);	
		return rbt;
	}

	@Override
	public String makeConsentCgUrl() {
		
		String cgUrl = getValueFromResourceBundle(resourceBundle, "cg_url", null);
		
		Consent consent = consentProcessBean.getConsent();
		Subscriber subscriber = consentProcessBean.getSubscriber();
		if (subscriber == null) {
			String subscriberId = consentProcessBean.getSubscriberId();
			RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subscriberId,null);
			subscriber = RBTClient.getInstance().getSubscriber(rbtRequest);
		}
		String response = consentProcessBean.getResponse();
		
		
		if (cgUrl != null && consent != null && response != null && response.equalsIgnoreCase("success")) {
			try {
				String consentMsisdnPrefix = ConsentPropertyConfigurator.getConsentMsisdnPrefix();
				String circleId="";
			
				if(subscriber.getCircleID().contains("_"))
				circleId= getThirdPartyCircleIdMap().get(subscriber.getCircleID().split("_")[1]);
				else
				circleId= getThirdPartyCircleIdMap().get(subscriber.getCircleID());
	
				
				String msisdn = subscriber.getSubscriberID();
				if(consentMsisdnPrefix != null) {
					msisdn = consentMsisdnPrefix + msisdn;
				}
				
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%CALLER_ID%", consent.getCallerId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%CATEGORY_ID%", consent.getCatId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%CHARGE_CLASS%", consent.getChargeclass());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%CLIP_ID%", consent.getClipId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%CLIP_INFO%", consent.getClipInfo());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%LINKED_REF_ID%", consent.getLinkedRefId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%MODE%", consent.getMode());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%MSISDN%", msisdn);
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%PRICE%", consent.getPrice());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%PROMO_ID%", consent.getPromoId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%REF_ID%", consent.getRefId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%REQ_TYPE%", consent.getReqType());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%SONG_NAME%", consent.getSongname());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%SRV_CLASS%", consent.getSrvClass());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%SRV_ID%", consent.getSrvId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%SUB_CLASS%", consent.getSubClass());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%TRANS_ID%", consent.getTransId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%VALIDITY%", consent.getValidity());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%SESSION_ID%", consent.getSessionId());
				
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%CIRCLE_ID%",circleId);
				
				//cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%CIRCLE_ID%", getThirdPartyCircleIdMap().get(subscriber.getCircleID()));
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%REQUEST_TIME%", getConsentReqTime());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%LOGINID%", ConsentPropertyConfigurator.getConsentUserId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%PASSWORD%",getVfIndiaConsentEncryptedPassword(
						ConsentPropertyConfigurator.getConsentUserId(),
						ConsentPropertyConfigurator.getConsentPassword()));
				// add param 3
				if(consent.getClipId() != null && !consent.getClipId().isEmpty() && consent.getCatId() != null && !consent.getCatId().isEmpty()){
					String resp = getCGImageURL(consent.getClipId(),consent.getCatId());
					if(resp != null) {
						cgUrl =	ServiceUtil.replaceStringInString(cgUrl, "%IMG_URL%",resp);
					}
				}
				
				
				logger.info("INITIAL URL IS:" + cgUrl);
				
				setTransId(consent.getTransId());
			} catch (Throwable t) {
				logger.error("Throwable caught! " + t, t);
				return null;
			}
		}
		logger.info("CGUrl: " + cgUrl);
		
		return cgUrl;
	}

	
	public Map<String, String> getThirdPartyCircleIdMap() {
		if(thirdPartyCircleIdMap != null)
			return thirdPartyCircleIdMap;
		thirdPartyCircleIdMap = new HashMap<String, String>();
		String thirdPartyCircleIdString = ConsentPropertyConfigurator.getThirdPartyCircleIdMapping();
		thirdPartyCircleIdMap = MapUtils.convertIntoMap(thirdPartyCircleIdString, ",", ":", null);
		return thirdPartyCircleIdMap;
	}
	
	// Encrypting password using loginId and systemtime
	public static String getVfIndiaConsentEncryptedPassword(String vodafoneConsentLoginID, String vodafoneConsentPassword) {
			String sysTime = ""+System.currentTimeMillis();
			String loginId = vodafoneConsentLoginID;
			logger.debug("LOGIN ID:" + loginId);
			String cofigPassword = vodafoneConsentPassword;
			logger.debug("CoNFIGURED PASSOWRD:" + cofigPassword);
			String password = sysTime + "|" + loginId + "|" + cofigPassword;
			logger.debug("PASSWORD:" + password);
			String encryptedPassword = EncryptionDecryptionUtil.doEncrypt(password);
			logger.debug("ENCRYPTED PASSWORD:" + encryptedPassword);
			try {
				//URL-Encoding is done twice due to some limitation at IBM side.
				encryptedPassword = URLEncoder.encode(encryptedPassword,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("Unsupported encoding exception." + e, e);
			}
			return encryptedPassword;
		}
	
	protected String getCGImageURL(String clipId, String catId){
		String finalImagePath = null;
		Clip clip = RBTCacheManager.getInstance().getClip(Integer.parseInt(clipId));
		Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(catId));
		
		if(category != null && Utility.isShuffleCategory(category.getCategoryTpe())){
			String catImageUrl = ConsentPropertyConfigurator.getParameterValue(KEY_FILESYSTEM_CATEGORY_IMAGE_PATH);
			String catImagePath = category.getCategoryInfo(CategoryInfoKeys.IMG_URL);
			if (catImagePath != null) {
				File file = new File(catImageUrl, catImagePath);
				if(file.exists()) {
					finalImagePath =   ConsentPropertyConfigurator.getParameterValue(KEY_CATEGORY_IMAGE_PREVIEW_PATH) + catImagePath;
				}
				logger.info("File exists " + finalImagePath);
			}
			
			if(finalImagePath == null){
				Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(Integer.parseInt(catId));
				if(clips != null && clips.length > 0){
					String imageUrl =ConsentPropertyConfigurator.getParameterValue(KEY_FILESYSTEM_IMAGE_PATH);
					String imagePath = clips[0].getClipInfo(ClipInfoKeys.IMG_URL);
						if (imagePath != null) {
							File file = new File(imageUrl, imagePath);
							if(file.exists()){
								finalImagePath =  ConsentPropertyConfigurator.getParameterValue(KEY_IMAGE_PREVIEW_PATH) +  imagePath;
							}
						logger.info("File exists " + finalImagePath);
					}
				}	
			}
		}else{		
			String imageUrl = ConsentPropertyConfigurator.getParameterValue(KEY_FILESYSTEM_IMAGE_PATH);
			String imagePath = clip.getClipInfo(ClipInfoKeys.IMG_URL);
				if (imagePath != null) {
					File file = new File(imageUrl, imagePath);
					if(file.exists()){
						finalImagePath = ConsentPropertyConfigurator.getParameterValue(KEY_IMAGE_PREVIEW_PATH) + imagePath;
					}
				logger.info("File exists " + finalImagePath);
			}
		}	
		if(finalImagePath == null){
			String defaultImage = ConsentPropertyConfigurator.getParameterValue(KEY_DEFAULT_IMAGE_PATH);
			finalImagePath =  ConsentPropertyConfigurator.getParameterValue(KEY_IMAGE_PREVIEW_PATH) + defaultImage;
			logger.info("Image does not exists reteriving default image");
		}
		
		String baseUrl = ConsentPropertyConfigurator.getParameterValue(KEY_BASE_IMAGE_URL);
		if(baseUrl == null ) {
			return null;
		}
		String imageStreamUrl = ConsentPropertyConfigurator.getParameterValue(KEY_IMAGE_STREAM_URL);
		finalImagePath = baseUrl+imageStreamUrl+finalImagePath;
		
		HttpParameters httpParameters = new HttpParameters(HttpConfigurations.isUseProxy(), HttpConfigurations.getProxyHost(),
				HttpConfigurations.getProxyPort(), HttpConfigurations.getHttpConnectionTimeout(), HttpConfigurations.getHttpSoTimeout(),
				HttpConfigurations.getMaxTotalHttpConnections(), HttpConfigurations.getMaxHostHttpConnections());
		httpParameters.setUrl(finalImagePath);
		return HttpHitUtil.makeRequest(null, httpParameters, null,false);
	}
	
	
	@Override
	public String makeRUrl() {
		String rUrl = getValueFromResourceBundle(resourceBundle, "rUrl", null);
		logger.info("rurl"+rUrl);
		if(rUrl != null && !rUrl.isEmpty()) {
			if(rUrl.contains("?"))
				rUrl = rUrl.replace("?", "/"+operatorName+"?");
						
		}	
		return rUrl;
	}

	@Override
	public Rbt activateSubscriber(SubscriptionRequest subscriptionRequest) {
		return RBTClient.getInstance().activateRbtSubscriber(subscriptionRequest);
	}


	
	public Set<String> getCvCircleId() {
		return null;
	}


	@Override
	public StatusResponse getStatusResponse(String circleId,
			String callerId, Map<String,String> requestParamMap,HttpServletRequest request,HttpServletResponse response) throws Exception {
		String cgStatus = requestParamMap.get("CGStatus");
		StatusResponse statusResponse = null;
		if (cgStatus != null && cgStatus.equalsIgnoreCase(Constants.SUCCESS)) {
			statusResponse = new StatusResponse();
			statusResponse.setMessage(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_SUCCESS_MESSAGE, null));
			statusResponse.setStatusCode(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_SUCCESS_CODE, null));

		} else {
			String respCode = requestParamMap.get("CGStatusCode");
			statusResponse = new StatusResponse();
			String respMessage = null;
			if (respCode != null) {
				respMessage = getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_FAILURE_MESSAGE+"."+respCode, null);
				statusResponse.setStatusCode(respCode);
			}
			else
			{
				statusResponse.setStatusCode(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_FAILURE_CODE, null));

			}
			if (respMessage == null) {
				logger.debug("Failure message config missing for respCode: " + respCode);
				respMessage = getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_FAILURE_MESSAGE, null);
			}
			statusResponse.setMessage(respMessage);
			
		}
		return statusResponse;
	}
	
	
}
