package com.onmobile.mobileapps.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.ObjectGsonUtils;
import com.onmobile.android.utils.Utility;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.webservice.client.ComVivaSAXParser;
import com.onmobile.apps.ringbacktones.webservice.client.Parser;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.ComVivaConfigurations;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.URLBuilder;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class ConsentReturnServlet extends HttpServlet {

	private static final long serialVersionUID = -6141730256460811803L;
	private static Logger logger = Logger.getLogger(ConsentReturnServlet.class);

	private static final String SUCCESS = "Success";
	private static final String FAILURE = "Failure";
	private static final String ERROR = "Error";
	private static final String LOW_BALANCE = "Low Balance";
	private static final String TECHNICAL_DIFFICULTY = "Technical Difficulty";
	private static final String IDEA = "IDEA";
	private static final String VODAFONE = "VODAFONE";


	private static final String YES = "YES";

	private Configurations configurations = null;

	@Override
	public void init() {
		this.configurations = new Configurations();
		this.configurations.getLogger().info(
				"configurations: " + this.configurations);
	}
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ConsentReturnServletResponse respObject = new ConsentReturnServletResponse();;

		try {
			Map<String, String> requestParamsMap = Utility.getRequestParamsMap(request);
			logger.info("requestParamsMap: " + requestParamsMap);

			String uri = request.getRequestURI();
			logger.debug("URI: " + uri);
			String operatorName = uri.substring(uri.lastIndexOf('/') + 1);
			logger.debug("operatorId: " + operatorName);

			if (operatorName.equalsIgnoreCase(IDEA)) {
				String consent = request.getParameter("consent");
				if (consent != null && consent.equalsIgnoreCase(YES)) {
					respObject.setResponseStr(SUCCESS);
					respObject.setMessage(PropertyConfigurator.getConsentReturnSuccessMessage());
				} else {
					respObject.setResponseStr(FAILURE);
					respObject.setMessage(PropertyConfigurator.getConsentReturnFailureMessage());
				}

			} else if (operatorName.equalsIgnoreCase(VODAFONE)) {
				String cgStatus = request.getParameter("CGStatus");
				if (cgStatus != null && cgStatus.equalsIgnoreCase(SUCCESS)) {
					respObject.setResponseStr(SUCCESS);
					respObject.setMessage(PropertyConfigurator.getConsentReturnSuccessMessage());
				} else {
					respObject.setResponseStr(FAILURE);
					String respCode = request.getParameter("CGStatusCode");
					String respMessage = null;
					if (respCode != null) {
						respMessage = PropertyConfigurator.getConsentReturnFailureMessage(respCode);
					}
					if (respMessage == null) {
						logger.debug("Failure message config missing for respCode: " + respCode);
						respMessage = PropertyConfigurator.getConsentReturnFailureMessage();
					}
					respObject.setMessage(respMessage);
				}
			} else {	//Airtel flow
				String consentResponse = null; 
				String consentUrl  = null;
				
				//Added for msisdn change for consent reject
				if (requestParamsMap.get("msisdn").contains(":")) {
					String[] msisdnCptIDAr = requestParamsMap.get("msisdn").split(":");
					if (msisdnCptIDAr != null) {
						requestParamsMap.put("msisdn", msisdnCptIDAr[0]);
						if (msisdnCptIDAr.length > 1) {
							requestParamsMap.put("cptid", msisdnCptIDAr[1]);
						}
					}
				}
				
				boolean isSuccessCode = false;
				
				String vCode = requestParamsMap.get("vcode");
				String responseCode = requestParamsMap.get("code");
				if(operatorName.toLowerCase().contains("airtel_comviva")) {
					logger.info("Airtel Comviva Request");
					String subscriberID = requestParamsMap.get("msisdn");
					String circleId = operatorName.split("_")[2].trim();
					String url = ComVivaConfigurations.getInstance().getUrl(WebServiceConstants.param_comviva_consent_url,
									circleId);
					
					String consentParam = null;
					String callerId = operatorName.split("_")[3].trim();
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
							.replaceCallerId(callerId).replaceConsentParam(consentParam).replaceCptId(requestParamsMap.get("cptid"));
					consentUrl = urlBuilder.buildUrl();
					logger.info("Comviva final Url: "+consentUrl);
					HttpParameters httpParameters = new HttpParameters(configurations.isUseProxy(), configurations.getProxyHost(),
							configurations.getProxyPort(), configurations.getHttpConnectionTimeout(), configurations.getHttpSoTimeout(),
							configurations.getMaxTotalHttpConnections(), configurations.getMaxHostHttpConnections());
					httpParameters.setUrl(consentUrl);
					logger.info("httpParameters: " + httpParameters);
					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
							httpParameters, requestParamsMap);
					consentResponse = httpResponse.getResponse();					
					logger.info("ConsentResponse HTTP response: " + consentResponse);
					Parser parser = new Parser();
					parser.setResponse(consentResponse);
					parser.setParser(new ComVivaSAXParser());
					parser.setRequest(new SubscriptionRequest(subscriberID));
					parser.getParser().getRBT(parser);					
					consentResponse = parser.getRequest().getResponse();
					logger.info("Code (1000 = success and 3404 = low balance): " + request.getParameter("code") + ", isSuccessCode: " + isSuccessCode + ", consentResponse: " + consentResponse);
				}
				else {
					
					if (responseCode != null
							&& (Arrays.asList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
									"RESPONSE_CODES_FOR_COMVIVA", "1000").split(",")).contains(responseCode))){
						isSuccessCode = true;
					}
					consentUrl = PropertyConfigurator.getHTConsentUrl();
					if (!Utility.isStringValid(consentUrl)) {
						throw new IOException("Invalid htConsent url configured. htConsentUrl: " + consentUrl);
					}
					
					HttpParameters httpParameters = new HttpParameters(configurations.isUseProxy(), configurations.getProxyHost(),
							configurations.getProxyPort(), configurations.getHttpConnectionTimeout(), configurations.getHttpSoTimeout(),
							configurations.getMaxTotalHttpConnections(), configurations.getMaxHostHttpConnections());
					httpParameters.setUrl(consentUrl);
					logger.info("httpParameters: " + httpParameters);
					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
							httpParameters, requestParamsMap);
					consentResponse = httpResponse.getResponse();
					logger.info("ConsentResponse HTTP response: " + consentResponse);
				}
				
				String code = request.getParameter("code");
				String defaultResponse = TECHNICAL_DIFFICULTY;
				String defaultMessage = PropertyConfigurator.getConsentReturnTechnicalDifficultyMessage();
				
				logger.info("Code (1000 = success and 3404 = low balance): " + code);
				if (consentResponse != null && consentResponse.equalsIgnoreCase(SUCCESS) && ((code != null && code.equals("1000")) || isSuccessCode)) {
					defaultResponse = SUCCESS;
					defaultMessage = PropertyConfigurator.getConsentReturnSuccessMessage();
					
				} else if (code != null && code.equals("3404")) {
					defaultResponse = LOW_BALANCE;
					defaultMessage = PropertyConfigurator.getConsentReturnLowBalanceMessage();
				}else if (code != null && PropertyConfigurator.getErrorCodeBasedMessage(code) != null) {
					String errorRespAndMessage = PropertyConfigurator.getErrorCodeBasedMessage(code);
					List<String> respAndMessage = Arrays.asList(errorRespAndMessage.split(":"));
					if(respAndMessage != null && respAndMessage.size() == 2){
						defaultResponse = respAndMessage.get(0);
						defaultMessage  = respAndMessage.get(1);
					}
					
				}
				respObject.setResponseStr(defaultResponse);
				respObject.setMessage(defaultMessage);
				
			}
		} catch(Throwable t) {
			respObject.setResponseStr(ERROR);
			respObject.setMessage(PropertyConfigurator.getConsentReturnTechnicalDifficultyMessage());
			logger.error("Error caught! " + t, t);
		}
		PrintWriter out = response.getWriter();
		response.setContentType("text/json");
		logger.info("respObject: " + respObject);
		out.println(ObjectGsonUtils.objectToGson(respObject));
	}

	class ConsentReturnServletResponse {
		String responseStr;
		String message;
		public String getResponseStr() {
			return responseStr;
		}
		public void setResponseStr(String responseStr) {
			this.responseStr = responseStr;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConsentReturnServletResponse [responseStr=");
			builder.append(responseStr);
			builder.append(", message=");
			builder.append(message);
			builder.append("]");
			return builder.toString();
		}
	}
}
