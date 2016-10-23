package com.onmobile.apps.ringbacktones.v2.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;



/* 
 * 1. Making Default utility common for BSNL/Vodafone/Tata 
 * 2. Implementing Tata by extending defaultutility
 * 3. Overriding getstatusresponse forTata
 * 4. Hit the DoubleConfirmationCallbackServlet by creating the object of servlet/ or by including the request by req dispacter.
 * 5. Capture the response by creating the Wrapper.
 * 6. Call service method of DoubleConfirmationCallbackServlet
 * 7. Based on response will configure tata_config for msg and code
 * 8. Send Status response based on configuration
 *  
 */
@Service(value = BeanConstant.OPERATOR_TATA)
@Scope(value = Constants.SCOPE_PROTOTYPE)

public class TataUtility extends DefaultOperatorUtility {

	private static final String HTTP_METHOD_GET = "GET";
	//protected static ResourceBundle resourceBundle;
	private static Set<String> cvCircleId = null;
	
	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;

	private static Logger logger = Logger.getLogger(TataUtility.class);

	static {
		try {
			
			resourceBundle = ResourceBundle.getBundle("tata_config");
			initilizeCVCircle(cvCircleId);

		} catch (MissingResourceException e) {
			logger.error("Exception Occured: " + e, e);
		}
	}

	public StatusResponse getStatusResponse(String circleId, String callerId, Map<String, String> requestParamMap,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (request.getMethod().equalsIgnoreCase(HTTP_METHOD_GET)) {

			logger.info("inside getstatus of tata ***********************");
			// DoubleConfirmationCallbackServlet doubobj = new
			// DoubleConfirmationCallbackServlet();

			HttpServletResponse httpResponse = response;
			MyHttpServletResponseWrapper wrapper = new MyHttpServletResponseWrapper(httpResponse);
			// doubobj.service(request, wrapper);
			RequestDispatcher dispatch = request.getRequestDispatcher("/consentCallback.do");
			dispatch.include(request, wrapper);
			String res = wrapper.toString().toUpperCase();
			StatusResponse statusResponse = new StatusResponse();
			statusResponse.setStatusCode(
					getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_CODE + res, "999"));
			statusResponse.setMessage(getValueFromResourceBundle(resourceBundle,
					Constants.PARAM_CONSENT_RETURN_MESSAGE + res, "defaultmsg"));
			return statusResponse;
		} else {

			logger.info("inside post***v");
			throw new Exception(Constants.FAILURE);

		}

	}

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
	
	public Set<String> getCvCircleId() {
		return cvCircleId;
	}
	
	
	
	
	
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
		String eventType = "2";

		
		if (cgUrl != null && consent != null && response != null && response.equalsIgnoreCase("success")) {
			try {
				String consentMsisdnPrefix = ConsentPropertyConfigurator.getConsentMsisdnPrefix();
				String circleId="";
				String opt1=null;
				String networkID=null;
				if(subscriber.getCircleID().contains("_"))
				circleId= getThirdPartyCircleIdMap().get(subscriber.getCircleID().split("_")[1]);
				else
				circleId= getThirdPartyCircleIdMap().get(subscriber.getCircleID());
	
				
				String msisdn = subscriber.getSubscriberID();
				if(consentMsisdnPrefix != null) {
					msisdn = consentMsisdnPrefix + msisdn;
				}
				
				String wdsResponse = queryWDS(msisdn);
				
				String tokens[] = null; 
				if(wdsResponse != null) {
					tokens = wdsResponse.split("\\|");
				}

				if (tokens != null && tokens.length > 9) {
					networkID = tokens[7];
					
				}
				
				
				cgUrl = ServiceUtil.replaceStringInString(cgUrl,"%REQ_TYPE%",consent.getReqType());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%MODE%", consent.getMode());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%MSISDN%", msisdn);
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%PRICE%", consent.getPrice());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%TRANS_ID%", consent.getTransId());
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%VALIDITY%", consent.getValidity());
				
				cgUrl = ServiceUtil.replaceStringInString(cgUrl, "%CIRCLE_ID%",circleId);
				
				
				
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

private String queryWDS(String subscriberID) {
	String wdsResult = null;
	try {
		String wdsHttpQuery = null;
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter(iRBTConstant.COMMON, "WDS_HTTP_LINK");
		if(param == null)
			return null;
		wdsHttpQuery = param.getValue().trim();
		logger.info("query is " + wdsHttpQuery);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("MDN", subscriberID);
		com.onmobile.apps.ringbacktones.common.HttpParameters httpParameters = new com.onmobile.apps.ringbacktones.common.HttpParameters(wdsHttpQuery);
		File[] files=null;
		wdsResult = RBTHTTPProcessing.postFile(httpParameters, params, files);
		logger.info("result for " + subscriberID + "is " + wdsResult);
	} catch (Exception e) {
		logger.error("", e);
		wdsResult = null;
	}
	// result = "9030055076|2|PREPAID|2|1|013DE58A|-|GCMO|AP|SLEE02||";
	return wdsResult;
}

	static class MyHttpServletResponseWrapper extends HttpServletResponseWrapper {

		private StringWriter sw = new StringWriter(10000);

		public MyHttpServletResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		public PrintWriter getWriter() throws IOException {
			return new PrintWriter(sw);
		}

		public ServletOutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}

		public String toString() {
			return sw.toString();
		}
	}

}