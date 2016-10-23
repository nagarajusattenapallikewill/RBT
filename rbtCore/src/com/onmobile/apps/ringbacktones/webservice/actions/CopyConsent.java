package com.onmobile.apps.ringbacktones.webservice.actions;

import java.io.IOException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onmobile.apps.ringbacktones.Gatherer.RBTCopyProcessor;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.GCMRegistration;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;


public class CopyConsent extends HttpServlet implements WebServiceAction, WebServiceConstants, iRBTConstant{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5608193985852622377L;
	private static final Logger logger = Logger.getLogger(CopyConsent.class);
	
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		WebServiceResponse webServiceResponse = null;

		HashMap<String, String> requestParamsMap = Utility.getRequestParamsMap(
				getServletConfig(), request, response, api_WebService);

		WebServiceContext webServiceContext = Utility.getTask(requestParamsMap);

		// For the redirection Concept
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParamsMap);
		Task task = new Task(null, taskSession);
		task.setObject(Constants.param_URL, "CopyConsent.do");
		String redirectionURL = Processor.getRedirectionURL(task);
		if (redirectionURL != null) {
			String responseStr = null;
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParamsMap);
				logger.info("RBT:: httpResponse: " + httpResponse);

				responseStr = httpResponse.getResponse();
			} catch (Exception e) {
				responseStr = TECHNICAL_DIFFICULTIES;
				logger.error("RBT:: " + e.getMessage(), e);
			}
			webServiceResponse = getWebServiceResponse(responseStr);
		} else {
			webServiceResponse = processAction(webServiceContext);
		}
		if (CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.WEBSERVICE,
				"COPY_CONSENT_XML_RESPONSE_FORMAT", null) != null) {
			String newresponse = webServiceResponse.getResponse();
			if (newresponse != null) {
				newresponse = newresponse.replaceAll("<rbt>", "");
				newresponse = newresponse.replaceAll("</rbt>", "");
				newresponse = newresponse.replaceAll("<response>", "");
				newresponse = newresponse.replaceAll("</response>", "");
			}
			logger.info("New webServiceResponse : " + newresponse);
			response.getWriter().write(newresponse);
			return;
		}
		logger.info("Old webServiceResponse : " + webServiceResponse);
		webServiceResponse.getResponseWriter().writeResponse(
				webServiceResponse, response);

	}
	
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
	
//	private Sender sender = null;
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		String response = ERROR;
		try {

			String[] smsTypes = {COPY + "_CONSENT",COPYSTAR + "_CONSENT",RRBT_COPY + "_CONSENT"};			
			
			String subscriberId = webServiceContext.getString(param_msisdn);
			String  submitPacknotChosen = webServiceContext.getString(param_submitPacknotChosen);
			
			ViralSMSTable[] viralSmsTables = RBTDBManager.getInstance().getViralSMSesByTypesForCopyConsent(subscriberId, smsTypes);
			
			if(viralSmsTables == null || viralSmsTables.length == 0){
				response = ERROR;
				return getWebServiceResponse(response);
			}

			if(submitPacknotChosen.equalsIgnoreCase(SUCCESS) || submitPacknotChosen.equalsIgnoreCase("TRUE")){

				ViralSMSTable vst = null;
				String type = null;
				
				if(viralSmsTables != null && viralSmsTables.length != 0) {
					vst = viralSmsTables[viralSmsTables.length - 1];//taking only the last record of a particular subscriber
					type = vst.type();
					type = type.substring(0, type.indexOf("_CONSENT"));
				}
				
				if(vst != null) {
					logger.info(" vst :" + vst);
					logger.info("Request received with params: "
							+ webServiceContext);
					SelectionRequest selectionRequest = new SelectionRequest(
							vst.callerID());
					selectionRequest.setSetTime(vst.setTime());
					selectionRequest.setType(type);
					//selectionRequest.setCallerID(vst.callerID());
					String wavFile = null;
					String categoryID = null;
					if(vst.clipID() != null){
                      String str [] = vst.clipID().split(":");
                      wavFile = str[0];
                      categoryID = str[1];
                    }
					String virtualNumberCopyMode = CacheManagerUtil.getParametersCacheManager().getParameterValue("GATHERER",VIRTUAL_NUMBER_COPY_MODE
							+ "_" + vst.subID(),null);
					if (virtualNumberCopyMode == null)
						virtualNumberCopyMode = CacheManagerUtil.getParametersCacheManager().getParameterValue("GATHERER",VIRTUAL_NUMBER_COPY_MODE,null);

					String virtualNumberConfig =  CacheManagerUtil.getParametersCacheManager().getParameterValue("VIRTUAL_NUMBERS", vst.subID(), null);
					String subClassStr = null;
					String inactiveChargeClass = null;
                    if(virtualNumberConfig != null){
                    	String chargeClass = null;
						String circleID = null;
						//virtualNumberConfig = wavFile,SubscriptionClass,circleId,activechargeClass:inActivechargeClass
						String[] tokens = virtualNumberConfig.split(","); 
						if (tokens.length >= 2)
							subClassStr = tokens[1];
						if (tokens.length >= 3)
							circleID = tokens[2];
						if (tokens.length >= 4)
							chargeClass = tokens[3];
						
						if (chargeClass != null) {
							String[] chargeClassSplit = chargeClass.split(":");
							if(chargeClassSplit.length == 2){
								inactiveChargeClass = chargeClassSplit[1];
							}
						}
                    }
                    
                    if(subClassStr!=null){
                    	selectionRequest.setSubscriptionClass(subClassStr);
                    }
                    if(inactiveChargeClass!=null){
                    	selectionRequest.setChargeClass(inactiveChargeClass);
                    	selectionRequest.setUseUIChargeClass(true);
                    }
                    
                    String copytype = "";
                    if (RBTParametersUtils.getParamAsBoolean("GATHERER",
        					"ALLOW_SPECIAL_COPY_MODE", "FALSE")) {
        				copytype = RBTCopyProcessor.getSpecialCopyMode(getSubscriber(vst.callerID()),
        						getSubscriber(vst.subID()));
        			}
        			
        			String selInfo = copytype + "|CP:" + virtualNumberCopyMode + "-" + vst.subID()
        					+ ":CP|";
                    
					selectionRequest.setCategoryID(categoryID);
					selectionRequest.setRbtFile(wavFile);
					selectionRequest.setClipID(wavFile);
					selectionRequest.setMode(virtualNumberCopyMode);
					selectionRequest.setSelectionInfoMap(DBUtility
							.getAttributeMapFromXML(vst.extraInfo()));
					selectionRequest.setModeInfo(selInfo);
					
					logger.info("selectionRequest details :" + " categoryID:" + categoryID + " time:" + vst.setTime() + " type:" + vst.type() +" callerID :" + vst.callerID() + " clipID:" +vst.clipID() + " wavefile :" + wavFile + " selectedBy :" +vst.selectedBy() + " moreInfo:" + vst.extraInfo());
                    //Earlier flow
//					RBTDBManager.getInstance().insertViralSMSTableMap(vst.subID(), vst.sentTime(), type, vst.callerID(), vst.clipID(), vst.count(), vst.selectedBy(), vst.setTime(), DBUtility.getAttributeMapFromXML(vst.extraInfo()));
					Rbt rbt = RBTClient.getInstance()
							.addSubscriberSelection(
									selectionRequest);
					Consent consent = null;
					if(rbt!=null){
					    consent = rbt.getConsent();
					}
					String confXmlResponse = null;
					String vcode =  null;
					if(wavFile!=null){
						vcode =wavFile.replaceAll("rbt_", "").replaceAll("_rbt", "");
					}
					String transID = null;
					if(consent != null){
						transID = consent.getTransId();
						confXmlResponse = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.WEBSERVICE, "COPY_CONSENT_XML_RESPONSE_FORMAT", null);
						if (confXmlResponse != null) {
							if(transID!=null)
								confXmlResponse =  confXmlResponse.replaceAll("%TRANS_ID%", transID);
							if(vcode!=null)
								confXmlResponse = confXmlResponse.replaceAll("%VCODE%", vcode);
							if(virtualNumberCopyMode!=null)
								confXmlResponse = confXmlResponse.replaceAll("%MODE%", virtualNumberCopyMode); 
						}
					}
					logger.info("consent table Insertion Response = "+selectionRequest.getResponse()+" Trans_ID = "+transID + " WavFile = "+wavFile);
					if (selectionRequest.getResponse() != null
							&& selectionRequest.getResponse().indexOf("success") != -1) {
						RBTDBManager.getInstance().removeViralSMSesByTypeForCopyConsent(
										subscriberId, smsTypes);
					}
					response = selectionRequest.getResponse();
					if(confXmlResponse!=null){
					    response = confXmlResponse;
					}
	
				}	
			}else{
				 RBTDBManager.getInstance().removeViralSMSesByTypeForCopyConsent(subscriberId, smsTypes);
				 response = SUCCESS;
			}
			
			
		} catch (Exception e) {
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response);

	}
	
	/**
	 * @param response
	 * @return
	 */
	protected WebServiceResponse getWebServiceResponse(String response) {
//		Document document = Utility.getResponseDocument(response);
//		WebServiceResponse webServiceResponse = Utility
//				.getWebServiceResponseXML(document);
		
		WebServiceResponse webServiceResponse = new WebServiceResponse(response);
		webServiceResponse.setContentType("text/xml; charset=utf-8");
		ResponseWriter responseWriter = WebServiceResponseFactory
				.getResponseWriter(StringResponseWriter.class);
		webServiceResponse.setResponseWriter(responseWriter);
		
//		return webServiceResponse;

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
	
	/**
	 * @param response
	 * @return
	 */
	protected WebServiceResponse getWebServiceResponse(String response, GCMRegistration[] gcmRegistrations, String smsText) {
		Document document = Utility.getMobileAppNotificationDocument(response, gcmRegistrations, smsText);		
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
	
	public Subscriber getSubscriber(String strSubID) {
		return RBTConnector.getInstance().getSubscriberRbtclient().getSubscriber(strSubID,
				"GATHERER");
	}

}
