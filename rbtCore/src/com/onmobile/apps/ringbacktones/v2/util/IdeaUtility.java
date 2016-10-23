package com.onmobile.apps.ringbacktones.v2.util;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.wcg.utility.decryption.agc.AESEncryptionDecryptionUtil;
import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

@Service(value = BeanConstant.OPERATOR_IDEA)
@Scope(value = Constants.SCOPE_PROTOTYPE)
public class IdeaUtility extends AbstractOperatorUtility {

	private static Logger logger = Logger.getLogger(IdeaUtility.class);
	
	private static final String CONSENT_MODIFIED_SERVICE_KEY = "A";
	private static final String CONSENT_SUBTYPE = "B";
	private static final String CONSENT_REF_ID = "C";
	private static final String CONSENT_INFO = "D";
	private static final String CONSENT_PRECHARGE = "E";
	private static final String CONSENT_ORIGINATOR = "F";
	private static final String CONSENT_CLIP_NAME = "G";
	private static final String CONSENT_REQ_TIME = "H";
	private static final String CONSENT_IMAGEURL = "I";
	private static final String CONSENT_USERID = "J";
	private static final String CONSENT_PASSWORD = "K";
	protected static ResourceBundle resourceBundle;
	private static Set<String> cvCircleId = null;
	
	static {
		try {
			resourceBundle = ResourceBundle.getBundle("idea_config");
			initilizeCVCircle();
		} catch(MissingResourceException e) {
			logger.error("Exception Occured: "+e,e);
		}
	}
	
	private static void initilizeCVCircle()
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
		boolean makeEntryInDb = ConsentPropertyConfigurator.isConsentFlowMakeEntryInDB();
		if (ConsentPropertyConfigurator.isConsentFlowIsGenerateRefId()) {
			selectionRequest.setGenerateRefId(true);
		}
		selectionRequest.setMakeEntryInDB(makeEntryInDb);
		Rbt rbt = RBTClient.getInstance().addSubscriberConsentSelection(selectionRequest);
		return rbt;
	}

	@Override
	public Rbt addSubscriberConsentDownload(SelectionRequest selectionRequest) {
		boolean makeEntryInDb = ConsentPropertyConfigurator.isConsentFlowMakeEntryInDB();
		if (ConsentPropertyConfigurator.isConsentFlowIsGenerateRefId()) {
			selectionRequest.setGenerateRefId(true);
		}
		selectionRequest.setMakeEntryInDB(makeEntryInDb);
		logger.info("selectionRequest: " + selectionRequest);
		Rbt rbt = RBTClient.getInstance().addSubscriberDownload(selectionRequest);	
		return rbt;
	}

	@Override
	public String makeConsentCgUrl() {
		
		String cgUrl = getValueFromResourceBundle(resourceBundle, "idea_cg_url", null);
		
		logger.info(consentProcessBean);
		Consent consent = consentProcessBean.getConsent();
		String response = consentProcessBean.getResponse();
		
		RBTCacheManager cacheManager = RBTCacheManager.getInstance();
		
	
		String consentClipId = null;
		String promo_id = null;
		String consentId = null;
		if (cgUrl != null && consent != null && response.equalsIgnoreCase("success")) {
			try {
				consentClipId = consent.getClipId();
				promo_id = consent.getPromoId();
				Clip clip = null;
				if (consentClipId != null && consentClipId.length() > 0) {
					clip = cacheManager.getClip(consentClipId);
				} else if (promo_id != null && promo_id.length() > 0) {
					clip = cacheManager.getClipByPromoId(promo_id);
				}
				String clipName = null;
				if (clip == null) { //Added for Record My Own feature
					clipName = consentClipId;
				} else {
					clipName = clip.getClipName();
				}

				cgUrl = cgUrl + "&" + CONSENT_MODIFIED_SERVICE_KEY + "=" 
						+ ServiceUtil.getURLEncodedValue(AESEncryptionDecryptionUtil.encrypt(getModifiedServieKey(consent)));
				cgUrl = cgUrl + "&" + CONSENT_SUBTYPE + "=" 
						+ ServiceUtil.getURLEncodedValue(getConsentSubtype(consent));
				consentId = ServiceUtil.getURLEncodedValue(getModifiedConsentRefId(consent));
				cgUrl = cgUrl + "&" + CONSENT_REF_ID + "=" + consentId;
				cgUrl = cgUrl + "&" + CONSENT_INFO + "=" 
						+ ServiceUtil.getURLEncodedValue(getConsentInfo(consent, clip, consentClipId));
				cgUrl = cgUrl + "&" + CONSENT_PRECHARGE + "=" 
						+ ServiceUtil.getURLEncodedValue(getConsentPrecharge());
				cgUrl = cgUrl + "&" + CONSENT_ORIGINATOR + "=" 
						+ ServiceUtil.getURLEncodedValue(getConsentOriginator());
				cgUrl = cgUrl + "&" + CONSENT_CLIP_NAME + "=" 
						+ ServiceUtil.getURLEncodedValue(clipName);
				cgUrl = cgUrl + "&" + CONSENT_REQ_TIME + "=" 
						+ ServiceUtil.getURLEncodedValue(getConsentReqTime());
				cgUrl = cgUrl + "&" + CONSENT_IMAGEURL + "=" 
						+ ServiceUtil.getURLEncodedValue(getImageUrl(clip)); 
				cgUrl = cgUrl + "&" + CONSENT_USERID + "=" 
						+ ServiceUtil.getURLEncodedValue(AESEncryptionDecryptionUtil.encrypt(getConsentUserId()));
				cgUrl = cgUrl + "&" + CONSENT_PASSWORD + "=" 
						+ ServiceUtil.getURLEncodedValue(AESEncryptionDecryptionUtil.encrypt(getConsentPassword()));
				
						
				logger.info("INITIAL URL IS:" + cgUrl);
			} catch (Throwable t) {
				logger.error("Throwable caught! " + t, t);
				return null;
			}
		}
		setTransId(consentId);
		return cgUrl;
	
	}
	
	public String getModifiedServieKey(Consent consent) {
		String consentModifiedServId = null;
		String consenServIdForSubClass = "";
		String consenServIdForChargeClass = "";
		String subClass = consent.getSubClass();
		logger.debug("SUBSCRIPTION CLASS IS:" + subClass);
		String chargeClass = consent.getChargeclass();
		logger.debug("CHARGE CLASS IS:" + chargeClass);
		if (subClass != null && !subClass.trim().isEmpty()) {
			String servIdRbtActprefix = ConsentPropertyConfigurator.getConsentRefIdPrefixRbtAct(); 
			consenServIdForSubClass = servIdRbtActprefix + subClass;
			logger.info("SERVICE ID WITH RBT ACT :" + consenServIdForSubClass);
			return consenServIdForSubClass;
		}
		if (chargeClass != null && !chargeClass.trim().isEmpty()) {
			String servIdRbtSelPrefix =  ConsentPropertyConfigurator.getConsentRefIdPrefixRbtSel();
			consenServIdForChargeClass = servIdRbtSelPrefix + chargeClass;
			logger.info("SERVICE ID WITH RBT SEL :"
					+ consenServIdForChargeClass);
			return consenServIdForChargeClass;
		}
		return consentModifiedServId;
	}
	
	
	private String getConsentSubtype(Consent consent) {
		String subClass = consent.getSubClass();
		String chargeClass = consent.getChargeclass();
		logger.debug("SubClass :" + subClass + " ChargeClass :" + chargeClass);
		if ((subClass != null && subClass.trim().length() > 0)
				&& (chargeClass != null && chargeClass.trim().length() > 0)) {
			logger.info("Consent SubType: C");
			return "C";
		}
		logger.info("Consent SubType: R");
		return "R";
	}

	
	public String getModifiedConsentRefId(Consent consent) {
		String refID = consent.getRefId();
		String linkedRefID = consent.getLinkedRefId();
		String subClass = consent.getSubClass();
		String chargeClass = consent.getChargeclass();
		logger.debug("CONSENT TRANS ID:" + refID);
		String refIdPrefix = ConsentPropertyConfigurator.getConsentRefIdPrefixCPId();
		logger.debug("REF ID PREFIX THAT IS CPID:" + refIdPrefix);
		String modifiedRefId = null;
		if (null != linkedRefID && !"".equals(linkedRefID)) {
			if ((null != subClass && !subClass.trim().isEmpty())
					&& (null != chargeClass && !chargeClass.trim().isEmpty())) {
				modifiedRefId = refIdPrefix + linkedRefID;
			}
		} else if (null != refID && !refID.trim().isEmpty()) {
			if (null != chargeClass && !chargeClass.trim().isEmpty()) {
				modifiedRefId = refIdPrefix + refID;
			}
		}
		logger.info("Modified ref id is :" + modifiedRefId);
		return modifiedRefId;
	}

	
	public String getConsentInfo(Consent consent, Clip clip, String consentClipId) {
		String consentInfo = "";

		if (null != consent) {
			boolean isComboRequest = false;
			boolean isSelRequest = false;
			String chargeClass = consent.getChargeclass();
			String subClass = consent.getSubClass();
			String clipId = null;
			Map<String, String> clipInfoMap = null;

			if (clip == null) { //Added for record my own
				logger.info("Clip is null. Returning consentClipId as clipId");
				clipId = consentClipId;
			} else {
				clipId = String.valueOf(clip.getClipId());
				clipInfoMap = getClipInfoMap(clip.getClipInfo());
			}

			if ((chargeClass != null && !"".equals(chargeClass))
					&& (subClass != null && !"".equals(subClass))) {
				consentInfo = ConsentPropertyConfigurator.getConsentInfoCombo();
				isComboRequest = true;
			} else if (subClass != null && !subClass.trim().isEmpty()) {
				consentInfo = ConsentPropertyConfigurator.getConsentInfoBase();
			} else {
				isSelRequest = true;
				consentInfo = ConsentPropertyConfigurator.getConsentInfoSel();
			}
			logger.info("consent info string obtained as:" + consentInfo);

			String atlantisCpId = null;
			String cgCpId = null;
			String cgCpName = null;
			if (clipInfoMap != null) {
				atlantisCpId = clipInfoMap.get("CPID");
				if (atlantisCpId != null) {
					cgCpId = ConsentPropertyConfigurator.getCgCpId(atlantisCpId);
					cgCpName = ConsentPropertyConfigurator.getCgCpName(atlantisCpId);
				}
			}
			if (cgCpId == null) {
				logger.debug("cgCpId null.");
				cgCpId = ConsentPropertyConfigurator.getDefaultCgCpId();
			}
			if (cgCpName == null) {
				logger.debug("cgCpName null.");
				cgCpName = ConsentPropertyConfigurator.getDefaultCgCpName();
			}

			consentInfo = consentInfo.replace("$SONG-ID$", clipId);
			consentInfo = consentInfo.replace("$CG-CP-ID$", cgCpId);
			consentInfo = consentInfo.replace("$CG-CP-NAME$", cgCpName);
			if (isComboRequest || isSelRequest) {
				consentInfo = consentInfo.replace("$CHILD-NAME$",
						consent.getChargeclass());
				consentInfo = consentInfo.replace("$CHILD-REF-ID$",
						getChildNameRefId(consent));
			}
		}
		return consentInfo;
	}

	private String getConsentPrecharge() {
		return ConsentPropertyConfigurator.getConsentPrecharge();
	}

	public String getConsentOriginator() {
		return ConsentPropertyConfigurator.getConsentOriginator();
	}
	
	public String getImageUrl(Clip clip) {
		String image = null;
		if (clip != null) {
			image = clip.getClipInfo(Clip.ClipInfoKeys.IMG);
		}
		logger.info("Clip image found from clipInfo is:" + image);
		String prefixImageUrl = ConsentPropertyConfigurator.getConsentImagePrefixUrl();
		if (null == image) {
			image = ConsentPropertyConfigurator.getConsentDefaultImagePath();
			if (image == null) {
				return "";
			}
		}
		image = prefixImageUrl + image;
		logger.info("CONSENT IMAGE WITH PREFIX:" + image);
		return image;
	}
	
	public String getConsentUserId() {
		String consentUserId = "";
		consentUserId = ConsentPropertyConfigurator.getConsentUserId();
		logger.info("CONSENT USER ID:" + consentUserId);
		return consentUserId;
	}

	public String getConsentPassword() {
		String consentPassword = "";
		consentPassword = ConsentPropertyConfigurator.getConsentPassword();
		logger.info("CONSENT PASSWORD ID:" + consentPassword);
		return consentPassword;
	}

	private Map<String, String> getClipInfoMap(String clipInfo) {
		logger.info("clipinfo " + clipInfo);
		Map<String, String> map = new HashMap<String, String>();
		if(clipInfo == null) {
			return map;
		}
		String[] list = clipInfo.split("\\|");		
		for (String token : list) {
			String[] st = token.split(":");
			if( st.length > 1) {
				map.put(st[0], st[1]);
			}
		}
		return map;
	}
	
	public String getChildNameRefId(Consent consent) {
		String consentRefID = consent.getRefId();
		String refIDPrefix = ConsentPropertyConfigurator.getConsentRefIdPrefixCPId();
		logger.debug("REF ID PREFIX THAT IS CPID:" + consentRefID);
		consentRefID = refIDPrefix + consentRefID;
		logger.info("CONSENT REFID WITH CPID:" + consentRefID);
		return consentRefID;
	}
	
	@Override
	public String makeRUrl() {
		String rUrl = getValueFromResourceBundle(resourceBundle, "idea_rUrl", null);
		if(rUrl != null && !rUrl.isEmpty()) {
			if(rUrl.contains("?"))
				rUrl = rUrl.replace("?", "/"+operatorName+"?");
						
		}	
		return rUrl;
	}

	@Override
	public Rbt activateSubscriber(SubscriptionRequest subscriptionRequest) {
		return RBTClient.getInstance().activateSubscriberPreConsent(subscriptionRequest);
	}


	@Override
	public Set<String> getCvCircleId() {
		return cvCircleId;
	}


	@Override
	public StatusResponse getStatusResponse(String circleId, String callerId, Map<String, String> requestParamMap,HttpServletRequest request,HttpServletResponse response) {
		String consent = requestParamMap.get("consent");
		StatusResponse statusResponse = null;
		if (consent != null && consent.equalsIgnoreCase("YES")) {
			statusResponse = new StatusResponse();
			statusResponse.setStatusCode(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_SUCCESS_CODE, null));
			statusResponse.setMessage(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_SUCCESS_MESSAGE, null));
		} else {
			statusResponse = new StatusResponse();
			statusResponse.setStatusCode(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_FAILURE_CODE, null));
			
			statusResponse.setMessage(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_FAILURE_MESSAGE, null));
		}
		return statusResponse;
	}
}
