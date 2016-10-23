package com.onmobile.apps.ringbacktones.v2.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

@Service(value = BeanConstant.OPERATOR_AIRTEL)
@Scope(value = Constants.SCOPE_PROTOTYPE)
public class AirtelUtility extends AbstractOperatorUtility {

	private static Logger logger = Logger.getLogger(AirtelUtility.class);
	protected static ResourceBundle resourceBundle;
	protected static Map<String, Parameter> paramMap;
	protected Configurations configurations = new Configurations();
	private static Set<String> cvCircleId = null;
	
	static {
		try {
			
			resourceBundle = ResourceBundle.getBundle("airtel_config");
			initilizeCVCircle();
			initializeParamMap();
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
	
	private static void initializeParamMap() {

		RBTClient client = RBTClient.getInstance();
		ApplicationDetailsRequest request = new ApplicationDetailsRequest();
		request.setType("MOBILEAPP");
		paramMap = new HashMap<String, Parameter>();
		Parameter[] parameters = client.getParameters(request);
		for(Parameter parameter: parameters){
			paramMap.put(parameter.getName(),parameter);
		}
		if(resourceBundle != null) {
			Enumeration<String> keys = resourceBundle.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				if (paramMap.containsKey(key)) {
					logger.warn("Duplicate property in DB and in properties file. Preference given to the property in the latter. Key: " + key);
				}
				Parameter param = new Parameter("MOBILEAPP", key, resourceBundle.getString(key));
				paramMap.put(key, param);
			}
		}
	}
	
	@Override
	public Rbt addSubscriberConsentSelection(SelectionRequest selectionRequest) {
		selectionRequest.setIsFollowSameRbtResponse(true);
		logger.info("selectionRequest: " + selectionRequest);
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
		
		String cgUrl = getValueFromResourceBundle(resourceBundle, "airtel_cg_url", null);


		logger.info(consentProcessBean);
		Consent consent = consentProcessBean.getConsent();
		String response = consentProcessBean.getResponse();
		String subscriberId = consentProcessBean.getSubscriber().getSubscriberID();
		RBTCacheManager cacheManager = RBTCacheManager.getInstance();
		
		
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
			consentId = consentId != null ? consentId : "";
			cgUrl = cgUrl.replaceAll("%cpt%", ServiceUtil.getURLEncodedValue(consentId));
			if(callerId == null || callerId.equalsIgnoreCase("ALL")){
				callerId = "all";
			}
			cgUrl = cgUrl.replaceAll("%md%",ServiceUtil.getURLEncodedValue(callerId));

			SimpleDateFormat dateFormatter = getCGUrlDateFormat();
			cgUrl = cgUrl.replaceAll("%timestamp%", ServiceUtil.getURLEncodedValue(dateFormatter.format(new Date())));
			cgUrl = cgUrl.replaceAll("%pp%",price != null ? ServiceUtil.getURLEncodedValue(price) : "");
			cgUrl = cgUrl.replaceAll("%pv%",validity != null ? ServiceUtil.getURLEncodedValue(validity) : "" );
			
		}
		logger.info("cgUrl: " + cgUrl);
		setTransId(consentId);
		return cgUrl;
	}

	@Override
	public String makeRUrl() {
		String rUrl = getValueFromResourceBundle(resourceBundle, "airtel_rUrl", null);
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
		return cvCircleId;
	}


	public StatusResponse getStatusResponse() {
		return null;
	}
	
	protected Map<String, String> msisdnChangeConsentReject(Map<String, String> requestParamMap) {
		if (requestParamMap.get("msisdn").contains(":")) {
			String[] msisdnCptIDAr = requestParamMap.get("msisdn").split(":");
			if (msisdnCptIDAr != null) {
				requestParamMap.put("msisdn", msisdnCptIDAr[0]);
				if (msisdnCptIDAr.length > 1) {
					requestParamMap.put("cptid", msisdnCptIDAr[1]);
				}
			}
		}
		
		return requestParamMap;
	}
	
	protected String getVCode(Map<String, String> requestParamMap) {
		return requestParamMap.get("vcode");
		
	}
	
	protected String getCode(Map<String, String> requestParamMap) {
		
		return requestParamMap.get("code");
	}

	@Override
	public StatusResponse getStatusResponse(String circleId,
			String callerId, Map<String, String> requestParamMap,HttpServletRequest request,HttpServletResponse response) throws Exception{
		StatusResponse statusResponse = null;
		try {
			requestParamMap = msisdnChangeConsentReject(requestParamMap);
			String consentResponse = null; 
			String consentUrl = null;
			String respCode = getCode(requestParamMap);
			boolean isSuccessCode = false;

			if(respCode != null &&
					(Arrays.asList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "RESPONSE_CODES_FOR_COMVIVA", "1000").split(",")).contains(respCode))) {
				isSuccessCode = true;
			}

			consentUrl = getValueFromParamMap(Constants.PARAM_HT_CONSENT_URL);
			if(!ServiceUtil.isStringValid(consentUrl)) {
				//throw new IOException("Invalid htConsent url configured. htConsentUrl: " + consentUrl);
			}

			HttpParameters httpParameters = new HttpParameters(configurations.isUseProxy(), configurations.getProxyHost(),
					configurations.getProxyPort(), configurations.getHttpConnectionTimeout(), configurations.getHttpSoTimeout(),
					configurations.getMaxTotalHttpConnections(), configurations.getMaxHostHttpConnections());

			httpParameters.setUrl(consentUrl);
			logger.info("httpParameters: " + httpParameters);
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, requestParamMap);

			consentResponse = httpResponse.getResponse();
			logger.info("ConsentResponse HTTP response: " + consentResponse);

			statusResponse = getResponse(requestParamMap, consentResponse, isSuccessCode);
		} catch (Exception e) {
			logger.error("Exception Occured: "+e,e);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		}
		return statusResponse;
	}
	
	protected StatusResponse getResponse(Map<String, String> requestParamMap, String consentResponse, boolean isSuccessCode) {
		
		String code = getCode(requestParamMap);
		StatusResponse statusResponse = null;
		if (consentResponse != null && consentResponse.equalsIgnoreCase(Constants.SUCCESS) && ((code != null && code.equals("1000")) || isSuccessCode)) {
			statusResponse = new StatusResponse();
			statusResponse.setMessage(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_SUCCESS_MESSAGE, null));
			
		} else if(code != null && code.equals("3404")) {
			statusResponse = new StatusResponse();
			
		statusResponse.setMessage(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_LOW_BALANCE_MESSAGE, null));
		}
		else if(code != null )
			{ 
				String errorRespAndMessage = getValueFromResourceBundle(resourceBundle, Constants.PARAM_ERROR_CODE_MESSAGE+"."+code, null);
				if(errorRespAndMessage!=null)
				{
					List<String> respAndMessage = Arrays.asList(errorRespAndMessage.split(":"));
					if(respAndMessage != null && respAndMessage.size() == 2)
						{
						statusResponse = new StatusResponse();
						statusResponse.setMessage(respAndMessage.get(1));
			            }	   
				}		
		    }
		else{
			statusResponse = new StatusResponse();
			statusResponse.setMessage(getValueFromResourceBundle(resourceBundle, Constants.PARAM_ERROR_CODE_MESSAGE, null));
			
			
		    }
		statusResponse.setStatusCode(code);
		return statusResponse;
	}
	
	protected String getValueFromParamMap(String key) {
		String value = null;
		Parameter param = paramMap.get(key);
		logger.info("param :==>" + param);
		if(param != null) {
			value = param.getValue();
		}
		return value;
	}

}
