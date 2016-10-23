package com.onmobile.apps.ringbacktones.v2.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;

@Service(value = BeanConstant.OPERATOR_VODAFONE)
@Scope(value = Constants.SCOPE_PROTOTYPE)
public class VodafoneUtility extends DefaultOperatorUtility {

	private static Logger logger = Logger.getLogger(DefaultOperatorUtility.class);
	private static Set<String> cvCircleId = null;

	static {
		try {
			resourceBundle = ResourceBundle.getBundle("vodafone_config");
			initilizeCVCircle(cvCircleId);
		} catch (MissingResourceException e) {
			logger.error("Exception Occured: " + e, e);
		}
	}

	public Set<String> getCvCircleId() {
		return cvCircleId;
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
				
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%CALLER_ID%", consent.getCallerId());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%CATEGORY_ID%", consent.getCatId());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%CHARGE_CLASS%", consent.getChargeclass());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%CLIP_ID%", consent.getClipId());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%CLIP_INFO%", consent.getClipInfo());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%LINKED_REF_ID%", consent.getLinkedRefId());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%MODE%", consent.getMode());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%MSISDN%", msisdn);
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%PRICE%", consent.getPrice());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%PROMO_ID%", consent.getPromoId());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%REF_ID%", consent.getRefId());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%REQ_TYPE%", consent.getReqType());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%SONG_NAME%", consent.getSongname());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%SRV_CLASS%", consent.getSrvClass());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%SRV_ID%", consent.getSrvId());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%SUB_CLASS%", consent.getSubClass());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%TRANS_ID%", consent.getTransId());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%VALIDITY%", consent.getValidity());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%SESSION_ID%", consent.getSessionId());
				
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%CIRCLE_ID%",circleId);
				
				//cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%CIRCLE_ID%", getThirdPartyCircleIdMap().get(subscriber.getCircleID()));
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%REQUEST_TIME%", getConsentReqTime());
				cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%LOGINID%", ConsentPropertyConfigurator.getConsentUserId());
				if(consent.getClipId() != null && !consent.getClipId().isEmpty() && consent.getCatId() != null && !consent.getCatId().isEmpty()){
					String resp = getCGImageURL(consent.getClipId(),consent.getCatId());
					if (resp != null && !resp.isEmpty()) {
						logger.info("Encoded Image Url : " + resp);
						resp = URLDecoder.decode(resp, "UTF-8");
						logger.info("Decoded Image Url : " + resp);
						cgUrl = ServiceUtil.replaceStringInStringWithOutEncoding(cgUrl, "%IMG_URL%", resp);
					}
				}
				
				cgUrl = com.onmobile.apps.ringbacktones.webservice.common.Utility.getEncryptedUrl(cgUrl);
				String password = getVfIndiaConsentEncryptedPassword(
						ConsentPropertyConfigurator.getConsentUserId(),
						ConsentPropertyConfigurator.getConsentPassword());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%PASSWORD%",password);
				// add param 3
				
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

}
