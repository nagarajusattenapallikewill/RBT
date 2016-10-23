package com.onmobile.android.utils.consent;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onmobile.android.beans.ConsentProcessBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class AirtelConsentUtility extends ConsentUtility {
	private static Logger logger = Logger.getLogger(AirtelConsentUtility.class);

	@Override
	public Rbt addSubscriberConsentSelection(SelectionRequest selectionRequest) {
		selectionRequest.setIsFollowSameRbtResponse(true);
		logger.info("selectionRequest: " + selectionRequest);
		Rbt rbt = RBTClient.getInstance().addSubscriberConsentSelectionInt(selectionRequest);
		return rbt;
	}
	
	@Override
	public String makeConsentCgUrl(ConsentProcessBean consentProcessBean) {
		logger.info(consentProcessBean);
		Consent consent = consentProcessBean.getConsent();
		String response = consentProcessBean.getResponse();
		String subscriberId = consentProcessBean.getSubscriber().getSubscriberID();
		RBTCacheManager cacheManager = RBTCacheManager.getInstance();
		
		String cgUrl = PropertyConfigurator.getConsentURL();
		String consentId = null;
		String consentClipId = null;
		String promo_id = null;
		String callerId = null;
		if (cgUrl != null && consent != null && response != null && response.equalsIgnoreCase("success")) {
			consentId = consent.getTransId();
			consentClipId = consent.getClipId();
			promo_id = consent.getPromoId();
			callerId = consent.getCallerId();			

			Clip clip = null;
			if (consentClipId != null && consentClipId.length() > 0) {
				clip = cacheManager.getClip(consentClipId);
			} else if (promo_id != null && promo_id.length() > 0) {
				clip = cacheManager.getClipByPromoId(promo_id);
			}

			cgUrl = cgUrl.replaceAll("%m%", Utility.getURLEncodedValue(subscriberId));
			String clipName = "";
			//String clipPromoId = "";
			String vCode = "";
			String price = consent.getPrice();
			String validity = consent.getValidity();
			String rbtWavFile = null;
			if (clip != null) {
				clipName = clip.getClipName() != null ? clip.getClipName() : "";
				//clipPromoId = clip.getClipPromoId() != null ? clip.getClipPromoId() : "";
				rbtWavFile = clip.getClipRbtWavFile();
				vCode = rbtWavFile!=null?rbtWavFile.replaceAll("rbt_", "").replaceAll("_rbt", ""):"";
			}
			cgUrl = cgUrl.replaceAll("%son%", Utility.getURLEncodedValue(clipName));
			cgUrl = cgUrl.replaceAll("%sovc%", Utility.getURLEncodedValue(vCode));
			consentId = consentId != null ? consentId : "";
			cgUrl = cgUrl.replaceAll("%cpt%", Utility.getURLEncodedValue(consentId));
			if(callerId == null || callerId.equalsIgnoreCase("ALL")){
				callerId = "all";
			}
			cgUrl = cgUrl.replaceAll("%md%",Utility.getURLEncodedValue(callerId));

			SimpleDateFormat dateFormatter = getCGUrlDateFormat();
			cgUrl = cgUrl.replaceAll("%timestamp%", Utility.getURLEncodedValue(dateFormatter.format(new Date())));
			cgUrl = cgUrl.replaceAll("%pp%",price != null ? Utility.getURLEncodedValue(price) : "");
			cgUrl = cgUrl.replaceAll("%pv%",validity != null ? Utility.getURLEncodedValue(validity) : "" );
			JsonArray jsonArray = new JsonArray();
			JsonObject json = new JsonObject();
			json.addProperty("ur", cgUrl);
			jsonArray.add(json);
			cgUrl = jsonArray.toString();
		}
		logger.info("cgUrl: " + cgUrl);
		return cgUrl;
	}
}