package com.onmobile.android.utils.consent;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.wcg.utility.decryption.agc.AESEncryptionDecryptionUtil;
import com.onmobile.android.beans.ConsentProcessBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class IdeaConsentUtility extends ConsentUtility {

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

	private static Logger logger = Logger.getLogger(IdeaConsentUtility.class);

	@Override
	public Rbt addSubscriberConsentSelection(SelectionRequest selectionRequest) {
		boolean makeEntryInDb = PropertyConfigurator.isConsentFlowMakeEntryInDB();
		if (PropertyConfigurator.isConsentFlowIsGenerateRefId()) {
			selectionRequest.setGenerateRefId(true);
		}
		selectionRequest.setMakeEntryInDB(makeEntryInDb);
		logger.info("selectionRequest: " + selectionRequest);
		Rbt rbt = RBTClient.getInstance().addSubscriberConsentSelection(selectionRequest);
		return rbt;
	}

	@Override
	public String makeConsentCgUrl(ConsentProcessBean consentProcessBean) {
		logger.info(consentProcessBean);
		Consent consent = consentProcessBean.getConsent();
		String response = consentProcessBean.getResponse();
		
		RBTCacheManager cacheManager = RBTCacheManager.getInstance();
		
		String cgUrl = PropertyConfigurator.getConsentURL();
		String consentClipId = null;
		String promo_id = null;
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
						+ Utility.getURLEncodedValue(AESEncryptionDecryptionUtil.encrypt(getModifiedServieKey(consent)));
				cgUrl = cgUrl + "&" + CONSENT_SUBTYPE + "=" 
						+ Utility.getURLEncodedValue(getConsentSubtype(consent));
				cgUrl = cgUrl + "&" + CONSENT_REF_ID + "=" 
						+ Utility.getURLEncodedValue(getModifiedConsentRefId(consent));
				cgUrl = cgUrl + "&" + CONSENT_INFO + "=" 
						+ Utility.getURLEncodedValue(getConsentInfo(consent, clip, consentClipId));
				cgUrl = cgUrl + "&" + CONSENT_PRECHARGE + "=" 
						+ Utility.getURLEncodedValue(getConsentPrecharge());
				cgUrl = cgUrl + "&" + CONSENT_ORIGINATOR + "=" 
						+ Utility.getURLEncodedValue(getConsentOriginator());
				cgUrl = cgUrl + "&" + CONSENT_CLIP_NAME + "=" 
						+ Utility.getURLEncodedValue(clipName);
				cgUrl = cgUrl + "&" + CONSENT_REQ_TIME + "=" 
						+ Utility.getURLEncodedValue(getConsentReqTime());
				cgUrl = cgUrl + "&" + CONSENT_IMAGEURL + "=" 
						+ Utility.getURLEncodedValue(getImageUrl(clip)); 
				cgUrl = cgUrl + "&" + CONSENT_USERID + "=" 
						+ Utility.getURLEncodedValue(AESEncryptionDecryptionUtil.encrypt(getConsentUserId()));
				cgUrl = cgUrl + "&" + CONSENT_PASSWORD + "=" 
						+ Utility.getURLEncodedValue(AESEncryptionDecryptionUtil.encrypt(getConsentPassword()));
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
			String servIdRbtActprefix = PropertyConfigurator.getConsentRefIdPrefixRbtAct(); 
			consenServIdForSubClass = servIdRbtActprefix + subClass;
			logger.info("SERVICE ID WITH RBT ACT :" + consenServIdForSubClass);
			return consenServIdForSubClass;
		}
		if (chargeClass != null && !chargeClass.trim().isEmpty()) {
			String servIdRbtSelPrefix =  PropertyConfigurator.getConsentRefIdPrefixRbtSel();
			consenServIdForChargeClass = servIdRbtSelPrefix + chargeClass;
			logger.info("SERVICE ID WITH RBT SEL :"
					+ consenServIdForChargeClass);
			return consenServIdForChargeClass;
		}
		return consentModifiedServId;
	}

	public String getConsentSubtype(Consent consent) {
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
		String refIdPrefix = PropertyConfigurator.getConsentRefIdPrefixCPId();
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
				consentInfo = PropertyConfigurator.getConsentInfoCombo();
				isComboRequest = true;
			} else if (subClass != null && !subClass.trim().isEmpty()) {
				consentInfo = PropertyConfigurator.getConsentInfoBase();
			} else {
				isSelRequest = true;
				consentInfo = PropertyConfigurator.getConsentInfoSel();
			}
			logger.info("consent info string obtained as:" + consentInfo);

			String atlantisCpId = null;
			String cgCpId = null;
			String cgCpName = null;
			if (clipInfoMap != null) {
				atlantisCpId = clipInfoMap.get("CPID");
				if (atlantisCpId != null) {
					cgCpId = PropertyConfigurator.getCgCpId(atlantisCpId);
					cgCpName = PropertyConfigurator.getCgCpName(atlantisCpId);
				}
			}
			if (cgCpId == null) {
				logger.debug("cgCpId null.");
				cgCpId = PropertyConfigurator.getDefaultCgCpId();
			}
			if (cgCpName == null) {
				logger.debug("cgCpName null.");
				cgCpName = PropertyConfigurator.getDefaultCgCpName();
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
		String refIDPrefix = PropertyConfigurator.getConsentRefIdPrefixCPId();
		logger.debug("REF ID PREFIX THAT IS CPID:" + consentRefID);
		consentRefID = refIDPrefix + consentRefID;
		logger.info("CONSENT REFID WITH CPID:" + consentRefID);
		return consentRefID;
	}

	private String getConsentPrecharge() {
		return PropertyConfigurator.getConsentPrecharge();
	}

	public String getConsentOriginator() {
		return PropertyConfigurator.getConsentOriginator();
	}

	public String getImageUrl(Clip clip) {
		String image = null;
		if (clip != null) {
			image = clip.getClipInfo(Clip.ClipInfoKeys.IMG);
		}
		logger.info("Clip image found from clipInfo is:" + image);
		String prefixImageUrl = PropertyConfigurator.getConsentImagePrefixUrl();
		if (null == image) {
			image = PropertyConfigurator.getConsentDefaultImagePath();
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
		consentUserId = PropertyConfigurator.getConsentUserId();
		logger.info("CONSENT USER ID:" + consentUserId);
		return consentUserId;
	}

	public String getConsentPassword() {
		String consentPassword = "";
		consentPassword = PropertyConfigurator.getConsentPassword();
		logger.info("CONSENT PASSWORD ID:" + consentPassword);
		return consentPassword;
	}
}