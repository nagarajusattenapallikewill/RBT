package com.onmobile.apps.ringbacktones.rbt2.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbt2.service.IOperatorUserDetailsCallbackService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;

public class OperatorUserDetailsCallbackServiceImpl implements IOperatorUserDetailsCallbackService {
	private static Logger logger = Logger.getLogger(OperatorUserDetailsCallbackServiceImpl.class);

	public void setOperatorUserInfo(String subscriberId, String serviceKey, String status, String operatorName, String circleID) throws Throwable {

		try {
			logger.info("setting  OperatorUserInfo ");
			if(circleID == null || serviceKey == null || status == null || operatorName == null){
				logger.info("cannot update as some param are null");
				return;
			}
			String url = ConsentPropertyConfigurator.getRBTOperatorUserInfoURLFormConfig();
			HttpClient client = new DefaultHttpClient();
			url = url.replaceFirst("%MSISDN%", subscriberId);
			url = url.replaceFirst("%SERVICE_KEY%", serviceKey);
			url = url.replaceFirst("%STATUS%", status);
			url = url.replaceFirst("%OPERATOR_NAME%", operatorName);
			url = url.replaceFirst("%CIRCLE_ID%", circleID);
			HttpPost request = new HttpPost(url);
			HttpResponse response = client.execute(request);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && response.getEntity().getContent() != null ){
				logger.info("setting user info success");
			}
		} catch (Throwable ce) {
			logger.error("setOperatorUserInfo failed:  " + ce.getMessage());
		}
	}

	public void removeOperatorUserInfo(String subscriberId) throws Throwable {

		try {
			logger.info("deleting  OperatorUserInfo ");
			String url = ConsentPropertyConfigurator.getRBTOperatorUserInfoURLFormConfig();
			url = url.replaceFirst("%MSISDN%", subscriberId);
			url = url.replaceFirst("%SERVICE_KEY%", "");
			url = url.replaceFirst("%STATUS%", "");
			url = url.replaceFirst("%OPERATOR_NAME%", "");
			url = url.replaceFirst("%CIRCLE_ID%", "");
			HttpClient client = new DefaultHttpClient();
			HttpDelete request = new HttpDelete(url);
			HttpResponse response = client.execute(request);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && response.getEntity().getContent() != null ){
				logger.info("deleting user info success");
			}
		} catch (Throwable ce) {
			logger.error("setOperatorUserInfo failed:  " + ce.getMessage());
		}
	}
	
	public void updateOperatorUserInfo(String subscriberId, String serviceKey, String status, String operatorName, String circleID) throws Throwable {

		try {
			logger.info("updating OperatorUserInfo ");
			if(circleID == null || serviceKey == null || status == null || operatorName == null){
				logger.info("cannot update as some param are null");
				return;
			}
			String url = ConsentPropertyConfigurator.getRBTOperatorUserInfoURLFormConfig();
			HttpClient client = new DefaultHttpClient();
			url = url.replaceFirst("%MSISDN%", subscriberId);
			url = url.replaceFirst("%SERVICE_KEY%", serviceKey);
			url = url.replaceFirst("%STATUS%", status);
			url = url.replaceFirst("%OPERATOR_NAME%", operatorName);
			url = url.replaceFirst("%CIRCLE_ID%", circleID);
			HttpPut request = new HttpPut(url);
			HttpResponse response = client.execute(request);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && response.getEntity().getContent() != null ){
				logger.info("updating  user info success");
			}
		} catch (Throwable ce) {
			logger.error("updateOperatorUserInfo failed:  " + ce.getMessage());
		}
	}

}
