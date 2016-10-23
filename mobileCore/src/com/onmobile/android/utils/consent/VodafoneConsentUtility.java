package com.onmobile.android.utils.consent;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.vodafone.encrytion.EncryptionDecryptionUtil;
import com.onmobile.android.beans.ConsentProcessBean;
import com.onmobile.android.configuration.HttpConfigurations;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.Utility;
import com.onmobile.android.utils.http.HttpUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category.CategoryInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip.ClipInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;

public class VodafoneConsentUtility extends ConsentUtility {
	public static final String KEY_FILESYSTEM_IMAGE_PATH = "filesystem.image.path";
	public static final String KEY_FILESYSTEM_CATEGORY_IMAGE_PATH = "filesystem.category.image.path";
	public static final String KEY_DEFAULT_IMAGE_PATH="default.image";
	public static final String KEY_IMAGE_PREVIEW_PATH = "image.preview.path";
	public static final String KEY_BASE_IMAGE_URL="base.image.url";
	public static final String KEY_CATEGORY_IMAGE_PREVIEW_PATH = "cateogry.image.preview.path";
	public static final String KEY_IMAGE_STREAM_URL = "image.streaming.url";
	private static Logger logger = Logger.getLogger(VodafoneConsentUtility.class);
	private static Map<String,String> thirdPartyCircleIdMap = new HashMap<String, String>();
	static {
		String thirdPartyCircleIdString = PropertyConfigurator.getThirdPartyCircleIdMapping();
		thirdPartyCircleIdMap = MapUtils.convertIntoMap(thirdPartyCircleIdString, ",", ":", null);
	}

	@Override
	public Rbt addSubscriberConsentSelection(SelectionRequest selectionRequest) {
		logger.info("selectionRequest: " + selectionRequest);
		Rbt rbt = RBTClient.getInstance().addSubscriberSelection(selectionRequest);
		return rbt;
	}

	@Override
	public String makeConsentCgUrl(ConsentProcessBean consentProcessBean) {
		Consent consent = consentProcessBean.getConsent();
		Subscriber subscriber = consentProcessBean.getSubscriber();
		if (subscriber == null) {
			String subscriberId = consentProcessBean.getSubscriberId();
			RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subscriberId,null);
			subscriber = RBTClient.getInstance().getSubscriber(rbtRequest);
		}
		String response = consentProcessBean.getResponse();
		//VD-106918
		String cgUrl = null;
		if (consent != null) {
			cgUrl = PropertyConfigurator.getConsentURL(consent.getMode());
		}
		//VD-106918ee
		if (cgUrl != null && consent != null && response != null && response.equalsIgnoreCase("success")) {
			try {
				String consentMsisdnPrefix = PropertyConfigurator.getConsentMsisdnPrefix();
				String msisdn = subscriber.getSubscriberID();
				if(consentMsisdnPrefix != null) {
					msisdn = consentMsisdnPrefix + msisdn;
				}
				
				cgUrl = Utility.replaceStringInString(cgUrl, "%CALLER_ID%", consent.getCallerId());
				cgUrl = Utility.replaceStringInString(cgUrl, "%CATEGORY_ID%", consent.getCatId());
				cgUrl = Utility.replaceStringInString(cgUrl, "%CHARGE_CLASS%", consent.getChargeclass());
				cgUrl = Utility.replaceStringInString(cgUrl, "%CLIP_ID%", consent.getClipId());
				cgUrl = Utility.replaceStringInString(cgUrl, "%CLIP_INFO%", consent.getClipInfo());
				cgUrl = Utility.replaceStringInString(cgUrl, "%LINKED_REF_ID%", consent.getLinkedRefId());
				cgUrl = Utility.replaceStringInString(cgUrl, "%MODE%", consent.getMode());
				cgUrl = Utility.replaceStringInString(cgUrl, "%MSISDN%", msisdn);
				cgUrl = Utility.replaceStringInString(cgUrl, "%PRICE%", consent.getPrice());
				cgUrl = Utility.replaceStringInString(cgUrl, "%PROMO_ID%", consent.getPromoId());
				cgUrl = Utility.replaceStringInString(cgUrl, "%REF_ID%", consent.getRefId());
				cgUrl = Utility.replaceStringInString(cgUrl, "%REQ_TYPE%", consent.getReqType());
				cgUrl = Utility.replaceStringInString(cgUrl, "%SONG_NAME%", consent.getSongname());
				cgUrl = Utility.replaceStringInString(cgUrl, "%SRV_CLASS%", consent.getSrvClass());
				cgUrl = Utility.replaceStringInString(cgUrl, "%SRV_ID%", consent.getSrvId());
				cgUrl = Utility.replaceStringInString(cgUrl, "%SUB_CLASS%", consent.getSubClass());
				cgUrl = Utility.replaceStringInString(cgUrl, "%TRANS_ID%", consent.getTransId());
				cgUrl = Utility.replaceStringInString(cgUrl, "%VALIDITY%", consent.getValidity());
				cgUrl = Utility.replaceStringInString(cgUrl, "%CIRCLE_ID%", thirdPartyCircleIdMap.get(subscriber.getCircleID()));
				cgUrl = Utility.replaceStringInString(cgUrl, "%REQUEST_TIME%", getConsentReqTime());
				cgUrl = Utility.replaceStringInString(cgUrl, "%LOGINID%", PropertyConfigurator.getConsentUserId());
				// add param 3
				String imageUrl = getCGImageURL(consent.getClipId(),consent.getCatId());
				logger.info("Encoded Image Url : " + imageUrl);
				if(imageUrl != null && !imageUrl.isEmpty()){
				imageUrl = URLDecoder.decode(imageUrl, "UTF-8");
				}
				logger.info("Decoded Image Url : " + imageUrl);
				cgUrl = Utility.replaceStringInString(cgUrl, "%IMG_URL%",imageUrl);
				cgUrl = com.onmobile.apps.ringbacktones.webservice.common.Utility.getEncryptedUrl(cgUrl);
				String password  = getVfIndiaConsentEncryptedPassword(
						PropertyConfigurator.getConsentUserId(),
						PropertyConfigurator.getConsentPassword());
				if(password != null && !password.isEmpty()){
					password = 	URLEncoder.encode(password,"UTF-8");					
				}
				cgUrl = Utility.replaceStringInString(cgUrl, "%PASSWORD%",password);
				logger.info("INITIAL URL IS:" + cgUrl);
				JsonArray jsonArray = new JsonArray();
				JsonObject json = new JsonObject();
				json.addProperty("ur", cgUrl);
				jsonArray.add(json);
				logger.info(jsonArray.toString());
				cgUrl = jsonArray.toString();
			} catch (Throwable t) {
				logger.error("Throwable caught! " + t, t);
				return null;
			}
		}
		logger.info("CGUrl: " + cgUrl);
		return cgUrl;
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

	private String getCGImageURL(String clipId, String catId){
		String finalImagePath = null;
		Clip clip = RBTCacheManager.getInstance().getClip(Integer.parseInt(clipId));
		Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(catId));
		
		if(category != null && Utility.isShuffleCategory(category.getCategoryTpe())){
			String catImageUrl = PropertyConfigurator.getParameterValue(KEY_FILESYSTEM_CATEGORY_IMAGE_PATH);
			String catImagePath = category.getCategoryInfo(CategoryInfoKeys.IMG_URL);
			if (catImagePath != null) {
				File file = new File(catImageUrl, catImagePath);
				if(file.exists()) {
					finalImagePath =   PropertyConfigurator.getParameterValue(KEY_CATEGORY_IMAGE_PREVIEW_PATH) + catImagePath;
				}
				logger.info("File exists " + finalImagePath);
			}
			
			if(finalImagePath == null){
				Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(Integer.parseInt(catId));
				if(clips != null && clips.length > 0){
					String imageUrl =PropertyConfigurator.getParameterValue(KEY_FILESYSTEM_IMAGE_PATH);
					String imagePath = clips[0].getClipInfo(ClipInfoKeys.IMG_URL);
						if (imagePath != null) {
							File file = new File(imageUrl, imagePath);
							if(file.exists()){
								finalImagePath =  PropertyConfigurator.getParameterValue(KEY_IMAGE_PREVIEW_PATH) +  imagePath;
							}
						logger.info("File exists " + finalImagePath);
					}
				}	
			}
		}else{		
			String imageUrl =PropertyConfigurator.getParameterValue(KEY_FILESYSTEM_IMAGE_PATH);
			String imagePath = clip.getClipInfo(ClipInfoKeys.IMG_URL);
				if (imagePath != null) {
					File file = new File(imageUrl, imagePath);
					if(file.exists()){
						finalImagePath =PropertyConfigurator.getParameterValue(KEY_IMAGE_PREVIEW_PATH) + imagePath;
					}
				logger.info("File exists " + finalImagePath);
			}
		}	
		if(finalImagePath == null){
			String defaultImage = PropertyConfigurator.getParameterValue(KEY_DEFAULT_IMAGE_PATH);
			finalImagePath =  PropertyConfigurator.getParameterValue(KEY_IMAGE_PREVIEW_PATH) + defaultImage;
			logger.info("Image does not exists reteriving default image");
		}
		
		String baseUrl =PropertyConfigurator.getParameterValue(KEY_BASE_IMAGE_URL);
		String imageStreamUrl = PropertyConfigurator.getParameterValue(KEY_IMAGE_STREAM_URL);
		finalImagePath = baseUrl+imageStreamUrl+finalImagePath;
		HttpParameters httpParameters = new HttpParameters(HttpConfigurations.isUseProxy(), HttpConfigurations.getProxyHost(),
				HttpConfigurations.getProxyPort(), HttpConfigurations.getHttpConnectionTimeout(), HttpConfigurations.getHttpSoTimeout(),
				HttpConfigurations.getMaxTotalHttpConnections(), HttpConfigurations.getMaxHostHttpConnections());
		httpParameters.setUrl(finalImagePath);
		return HttpUtils.makeRequest(null, httpParameters, null,false);
	}
}