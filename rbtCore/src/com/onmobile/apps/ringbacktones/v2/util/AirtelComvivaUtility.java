package com.onmobile.apps.ringbacktones.v2.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;

import com.onmobile.apps.ringbacktones.webservice.client.ComVivaSAXParser;
import com.onmobile.apps.ringbacktones.webservice.client.Parser;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ComvivaConsent;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.ComVivaConfigurations;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.URLBuilder;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

@Service(value = BeanConstant.OPERATOR_AIRTEL_COMVIVA)
@Scope(value = Constants.SCOPE_PROTOTYPE)
public class AirtelComvivaUtility extends AirtelUtility{

	private static Set<String> northHubCircleId = null;
	private static Set<String> eastHubCircleId = null;
	private static Set<String> westHubCircleId = null;
	private static Logger logger = Logger.getLogger(AirtelComvivaUtility.class);
	
	static {
		initializeEastHub();
		initializeNorthHub();
		initializeWestHub();
		
	}
	
	private static void initializeNorthHub() {
		String circleIds = getValueFromResourceBundle(resourceBundle,"CV_CIRCLE_ID_NORTH",null);
		if (circleIds != null && !circleIds.isEmpty()) {
			northHubCircleId = new HashSet<String>();
			String[] arrCircledId = circleIds.split(",");
			for (String circleId : arrCircledId) {
				northHubCircleId.add(circleId);
			}
		}
	}

	private static void initializeEastHub() {
		String circleIds = getValueFromResourceBundle(resourceBundle,"CV_CIRCLE_ID_EAST",null);
		if (circleIds != null && !circleIds.isEmpty()) {
			eastHubCircleId = new HashSet<String>();
			String[] arrCircledId = circleIds.split(",");
			for (String circleId : arrCircledId) {
				eastHubCircleId.add(circleId);
			}
		}
	}

	private static void initializeWestHub() {
		String circleIds = getValueFromResourceBundle(resourceBundle,"CV_CIRCLE_ID_WEST",null);
		if (circleIds != null && !circleIds.isEmpty()) {
			westHubCircleId = new HashSet<String>();
			String[] arrCircledId = circleIds.split(",");
			for (String circleId : arrCircledId) {
				westHubCircleId.add(circleId);
			}
		}
	}
	

	@Override
	public String makeConsentCgUrl() {
		
		String cgUrl = getUrl( "airtel_comviva_cg_url", consentProcessBean.getCircleID());
		

		logger.info(consentProcessBean);
		ComvivaConsent consent = (ComvivaConsent) consentProcessBean.getConsent();
		String response = consentProcessBean.getResponse();
		String subscriberId = consentProcessBean.getSubscriberId();
		RBTCacheManager cacheManager = RBTCacheManager.getInstance();
		
		//String cgUrl = ComVivaConfigurations.getInstance().getUrl(StringConstants.PARAM_MOBILEAPP_CONSENT_CONSENT_CG_URL, consentProcessBean.getCircleID());
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

			cgUrl = cgUrl.replaceAll("%m%", ServiceUtil.getURLEncodedValue(subscriberId));
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
			cgUrl = cgUrl.replaceAll("%son%", ServiceUtil.getURLEncodedValue(clipName));
			cgUrl = cgUrl.replaceAll("%sovc%", ServiceUtil.getURLEncodedValue(vCode));
			consentId = consent.getCpTransactionId() != null ? consent.getCpTransactionId():consentId;
			cgUrl = cgUrl.replaceAll("%cpt%", ServiceUtil.getURLEncodedValue(consentId));
			if(callerId == null || callerId.equalsIgnoreCase("ALL")){
				callerId = "all";
			}
			cgUrl = cgUrl.replaceAll("%md%",ServiceUtil.getURLEncodedValue(callerId));

			SimpleDateFormat dateFormatter = getCGUrlDateFormat();
			cgUrl = cgUrl.replaceAll("%timestamp%", ServiceUtil.getURLEncodedValue(dateFormatter.format(new Date())));
			cgUrl = cgUrl.replaceAll("%pp%",price != null ? ServiceUtil.getURLEncodedValue(price) : "");
			cgUrl = cgUrl.replaceAll("%pv%",validity != null ? ServiceUtil.getURLEncodedValue(validity) : "" );
			
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
			
			cgUrl = cgUrl.replaceAll("%sbid%",Sbid != null ? ServiceUtil.getURLEncodedValue(Sbid) : "" );
			cgUrl = cgUrl.replaceAll("%sopId%",sopId != null ? ServiceUtil.getURLEncodedValue(sopId) : "" );
			cgUrl = cgUrl.replaceAll("%soci%",soci != null ? ServiceUtil.getURLEncodedValue(soci) : "" );
			cgUrl = cgUrl.replaceAll("%crid%",crid != null ? ServiceUtil.getURLEncodedValue(crid) : "" );
			cgUrl = cgUrl.replaceAll("%mr%",mr != null ? ServiceUtil.getURLEncodedValue(mr) : "" );
			cgUrl = cgUrl.replaceAll("%mc%",mc != null ? ServiceUtil.getURLEncodedValue(mc) : "" );
			cgUrl = cgUrl.replaceAll("%pu%",pu != null ? ServiceUtil.getURLEncodedValue(pu) : "" );
			cgUrl = cgUrl.replaceAll("%soc%",soc != null ? ServiceUtil.getURLEncodedValue(soc) : "" );
			cgUrl = cgUrl.replaceAll("%opt1%",opt1 != null ? ServiceUtil.getURLEncodedValue(opt1) : "" );
			cgUrl = cgUrl.replaceAll("%opt2%",opt2 != null ? ServiceUtil.getURLEncodedValue(opt2) : "" );
			cgUrl = cgUrl.replaceAll("%opt3%",opt3 != null ? ServiceUtil.getURLEncodedValue(opt3) : "" );
			cgUrl = cgUrl.replaceAll("%opt4%",opt4 != null ? ServiceUtil.getURLEncodedValue(opt4) : "" );
			cgUrl = cgUrl.replaceAll("%opt5%",opt5 != null ? ServiceUtil.getURLEncodedValue(opt5) : "" );
		}
		setTransId(consentId);
		logger.info("cgUrl: " + cgUrl);
		return cgUrl;
	}
	
	@Override
	public String makeRUrl() {
		String rUrl = getValueFromResourceBundle(resourceBundle, "airtel_comviva_rUrl", null);
		if(rUrl != null && !rUrl.isEmpty()) {
			if(rUrl.contains("?"))
				rUrl = rUrl.replace("?", "/"+operatorName+"_comviva/"+consentProcessBean.getCircleID()+"/"+consentProcessBean.getCallerId()+"?");
						
		}	
		return rUrl;
	}
	
	public StatusResponse getStatusResponse(String circleId,
			String callerId, Map<String, String> requestParamMap) throws Exception{
		
		StatusResponse statusResponse = null;
		try {
			String consentResponse = null; 
			String consentUrl  = null;
			String responseCode = getCode(requestParamMap);
			String vCode = getVCode(requestParamMap);
			boolean isSuccessCode = false;

			logger.info("Airtel Comviva Request");
			String subscriberID = requestParamMap.get("msisdn");
			//String circleId = operatorName.split("_")[2].trim();
			String url = ComVivaConfigurations.getInstance().getUrl(WebServiceConstants.param_comviva_consent_url,
					circleId);

			String consentParam = null;
			//String callerId = operatorName.split("_")[3].trim();
			String uCode = null;

			if(callerId != null && !callerId.isEmpty() && !callerId.equalsIgnoreCase("ALL"))
				uCode = "1";
			else
				uCode = "0";

			if (responseCode != null
					&& (Arrays.asList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
							"RESPONSE_CODES_FOR_COMVIVA", "1000").split(",")).contains(responseCode))) {
				consentParam = "A";
				isSuccessCode = true;
			} else
				consentParam = "R";

			URLBuilder urlBuilder = new URLBuilder(url);

			urlBuilder = urlBuilder.replaceMsisdn(subscriberID)
					.replaceVCode(vCode).replaceUCode(uCode)
					.replaceCallerId(callerId).replaceConsentParam(consentParam).replaceCptId(requestParamMap.get("cptid"));
			consentUrl = urlBuilder.buildUrl();
			logger.info("Comviva final Url: "+consentUrl);
			HttpParameters httpParameters = new HttpParameters(configurations.isUseProxy(), configurations.getProxyHost(),
					configurations.getProxyPort(), configurations.getHttpConnectionTimeout(), configurations.getHttpSoTimeout(),
					configurations.getMaxTotalHttpConnections(), configurations.getMaxHostHttpConnections());
			httpParameters.setUrl(consentUrl);
			logger.info("httpParameters: " + httpParameters);
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, requestParamMap);
			consentResponse = httpResponse.getResponse();					
			logger.info("ConsentResponse HTTP response: " + consentResponse);
			Parser parser = new Parser();
			parser.setResponse(consentResponse);
			parser.setParser(new ComVivaSAXParser());
			parser.setRequest(new SubscriptionRequest(subscriberID));
			parser.getParser().getRBT(parser);					
			consentResponse = parser.getRequest().getResponse();
			logger.info("Code (1000 = success and 3404 = low balance): " +responseCode + ", isSuccessCode: " + isSuccessCode + ", consentResponse: " + consentResponse);
			
			statusResponse = getResponse(requestParamMap, consentResponse, isSuccessCode);
		} catch (Exception e) {
			logger.error("Exception Occurred: "+e,e);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		
		}
		
		return statusResponse;
	}
	
	private String getUrl(String urlKey, String circleId) {
		String url = null;
		if(circleId != null && !circleId.isEmpty()) {
			if(northHubCircleId.contains(circleId.toUpperCase())) {
				url = getValueFromResourceBundle(resourceBundle,urlKey+".NORTH",null);
			} else if(eastHubCircleId.contains(circleId.toUpperCase())) {
				url = getValueFromResourceBundle(resourceBundle,urlKey+".EAST",null);
			} else if(westHubCircleId.contains(circleId.toUpperCase())) {
				url = getValueFromResourceBundle(resourceBundle, urlKey+".WEST",null);
			}
		}
		return url;
	}
}