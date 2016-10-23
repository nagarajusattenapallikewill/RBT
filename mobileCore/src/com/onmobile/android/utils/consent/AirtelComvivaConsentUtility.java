package com.onmobile.android.utils.consent;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onmobile.android.beans.ConsentProcessBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.StringConstants;
import com.onmobile.android.utils.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ComvivaConsent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.ComVivaConfigurations;

public class AirtelComvivaConsentUtility extends AirtelConsentUtility {
	private static Logger logger = Logger.getLogger(ConsentUtilityFactory.class);

	@Override
	public String makeConsentCgUrl(ConsentProcessBean consentProcessBean) {
		logger.info(consentProcessBean);
		ComvivaConsent consent = (ComvivaConsent) consentProcessBean.getConsent();
		String response = consentProcessBean.getResponse();
		String subscriberId = consentProcessBean.getSubscriberId();
		RBTCacheManager cacheManager = RBTCacheManager.getInstance();
		
		String cgUrl = ComVivaConfigurations.getInstance().getUrl(StringConstants.PARAM_MOBILEAPP_CONSENT_CONSENT_CG_URL, consentProcessBean.getCircleID());
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
			consentId = consent.getCpTransactionId() != null ? consent.getCpTransactionId():consentId;
			cgUrl = cgUrl.replaceAll("%cpt%", Utility.getURLEncodedValue(consentId));
			if(callerId == null || callerId.equalsIgnoreCase("ALL")){
				callerId = "all";
			}
			cgUrl = cgUrl.replaceAll("%md%",Utility.getURLEncodedValue(callerId));

			SimpleDateFormat dateFormatter = getCGUrlDateFormat();
			cgUrl = cgUrl.replaceAll("%timestamp%", Utility.getURLEncodedValue(dateFormatter.format(new Date())));
			cgUrl = cgUrl.replaceAll("%pp%",price != null ? Utility.getURLEncodedValue(price) : "");
			cgUrl = cgUrl.replaceAll("%pv%",validity != null ? Utility.getURLEncodedValue(validity) : "" );
			
			String Sbid = consent.getSubProdId();
			String sopId = consent.getSongProdId();
			String soci = consent.getSongCpID();
			String crid = consent.getSongCopyRightID();
			String mr = consent.getGiftReceiverMsisdn();
			String mc = consent.getCopySongMsisdn();
			String pu = consent.getSubsOrSongPriceUnit();
			String soc = consent.getSongCategoryName();
			String opt1 = consent.getOpt1();
			String opt2 = consent.getOpt2();
			String opt3 = consent.getOpt3();
			String opt4 = consent.getOpt4();
			String opt5 = consent.getOpt5();
			
			cgUrl = cgUrl.replaceAll("%sbid%",Sbid != null ? Utility.getURLEncodedValue(Sbid) : "" );
			cgUrl = cgUrl.replaceAll("%sopId%",sopId != null ? Utility.getURLEncodedValue(sopId) : "" );
			cgUrl = cgUrl.replaceAll("%soci%",soci != null ? Utility.getURLEncodedValue(soci) : "" );
			cgUrl = cgUrl.replaceAll("%crid%",crid != null ? Utility.getURLEncodedValue(crid) : "" );
			cgUrl = cgUrl.replaceAll("%mr%",mr != null ? Utility.getURLEncodedValue(mr) : "" );
			cgUrl = cgUrl.replaceAll("%mc%",mc != null ? Utility.getURLEncodedValue(mc) : "" );
			cgUrl = cgUrl.replaceAll("%pu%",pu != null ? Utility.getURLEncodedValue(pu) : "" );
			cgUrl = cgUrl.replaceAll("%soc%",soc != null ? Utility.getURLEncodedValue(soc) : "" );
			cgUrl = cgUrl.replaceAll("%opt1%",opt1 != null ? Utility.getURLEncodedValue(opt1) : "" );
			cgUrl = cgUrl.replaceAll("%opt2%",opt2 != null ? Utility.getURLEncodedValue(opt2) : "" );
			cgUrl = cgUrl.replaceAll("%opt3%",opt3 != null ? Utility.getURLEncodedValue(opt3) : "" );
			cgUrl = cgUrl.replaceAll("%opt4%",opt4 != null ? Utility.getURLEncodedValue(opt4) : "" );
			cgUrl = cgUrl.replaceAll("%opt5%",opt5 != null ? Utility.getURLEncodedValue(opt5) : "" );
			
			String rUrl = PropertyConfigurator.getRURL();
			String[] arr = consentProcessBean.getCircleID().split("_");
			String circleId = null;
			
			if(arr.length > 1)
				circleId = arr[1].trim();
			else
				circleId = consentProcessBean.getCircleID();
			
			if(rUrl != null && !rUrl.isEmpty()) {
				if(rUrl.contains("airtel?"))
					rUrl = rUrl.replace("airtel?", "airtel_comviva_"+circleId+"_"+callerId+"?");
				if(cgUrl.contains("%rurl%"))
					cgUrl = cgUrl.replaceAll("%rurl%",rUrl != null ? rUrl : "" );
					
			}			

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