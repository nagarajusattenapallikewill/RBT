package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class HtGiftNotification extends HttpServlet {

	static Logger logger = Logger.getLogger(HtGiftNotification.class);

	
	@Override
	protected void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException, IOException {
		String response = "FAILURE";
		// For the redirection Concept IN Callback
		HashMap<String, String> requestParamsMap = Utility.getRequestParamsMap(
				getServletConfig(), httpRequest, httpResponse, Constants.api_consentCallback);
        
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParamsMap);
		Task task = new Task(null, taskSession);
		
		
		
//		msisdn=%s&transactionID=%s&cpTransactionID=%s&subProdID=%d&songProdID=%d&vCode=%s&msisdnGiftReceiver=%s&msisdnDedicatee=%s&errorCode=%s&errorDescription=%s&lowBalanceAmount=%lf&param1=%s&param2=%s&param3=%s&param4=%s&param5=%s&channelName=%s. 
		String gifteeId = task.getString("msisdnGiftReceiver");
		task.setObject(Constants.param_subscriberID, gifteeId);		
		String redirectionURL = Processor.getRedirectionURL(task);
		if (redirectionURL != null) {
			task.remove(Constants.param_subscriberID);
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				
				HttpResponse httpResponse1 = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParamsMap);
				logger.info("RBT:: httpResponse: " + httpResponse1);

				response = httpResponse1.getResponse();
			} catch (Exception e) {
				response = WebServiceConstants.TECHNICAL_DIFFICULTIES;
				logger.error("RBT:: " + e.getMessage(), e);
			}
		} else {
			response = processComvivaGiftRequest(task);
		}
		logger.info("Response: " + response);
		httpResponse.getWriter().write(response);
	}
	
	private String processComvivaGiftRequest(Task task) {
		
		
		String mode = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "HT_GIFT_NOTIFICATION_GIFT_MODE", "GIFT").getValue();
		
		String gifterID = task.getString("msisdn");
		String gifteeID = task.getString("msisdnGiftReceiver");
		String vcode = task.getString("vCode");
		String transactionID = task.getString("cpTransactionID");
		String paramMode = task.getString("mode");
		
		if(paramMode != null) {
			mode = paramMode;
		}
		
		GiftRequest giftRequest = new GiftRequest();
		giftRequest.setGifteeID(gifteeID);
		giftRequest.setGifterID(gifterID);
		giftRequest.setToneID(vcode);
		giftRequest.setMode(mode);
		
		HashMap<String,String> infoMap = new HashMap<String, String>();
		infoMap.put("TPCGID", transactionID);
		
		giftRequest.setInfoMap(infoMap);
		giftRequest.setIsGifterCharged(true);
		
		RBTClient.getInstance().sendGift(giftRequest);
		return giftRequest.getResponse();
		
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	

}

