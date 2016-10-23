package com.onmobile.mobileapps.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.exceptions.OMAndroidException;

public class FeedbackAction extends Action {

	public static Logger logger = Logger.getLogger(FeedbackAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws OMAndroidException {
		Enumeration<String> requestParamsNames = request.getParameterNames();
		String url = PropertyConfigurator.getUbonaFeedbackUrl();
		String responseString = "FAILURE";
		if (url != null) {
			HashMap<String, String> requestMap = new HashMap<String, String>();
			while (requestParamsNames.hasMoreElements()) {
				String requestParam = (String) requestParamsNames.nextElement();
				requestMap
						.put(requestParam, request.getParameter(requestParam));

			}

			Gson gson = new Gson();
			String json = gson.toJson(requestMap);
			PostMethod method = new PostMethod();
			method.setRequestHeader("Content-Type", "application/json");
			method.setRequestHeader("Accept", "application/json");
			try {
				method.setURI(new URI(url, true));
				method.setRequestEntity(new StringRequestEntity(json,
						"application/json", "UTF-8"));
				responseString = executeMethod(method);
			} catch (Exception e) {
				logger.info("Exception : " + e);
			}

			logger.info("Reccomendation Url reponse : " + responseString);
		} else {
			logger.info("Recommendation Url is not configured " + url);

		}

		request.setAttribute("response", responseString);
		return mapping.findForward("success");

	}

	private String executeMethod(HttpMethod method) throws HttpException,
			IOException {
		String response = null;
		HttpClient httpClient = new HttpClient();
		try {
			httpClient.executeMethod(method);
			ByteArrayOutputStream byteArrayOutputStream = null;
			InputStream inputStream = null;
			try {
				byteArrayOutputStream = new ByteArrayOutputStream();
				inputStream = method.getResponseBodyAsStream();
				byte[] buffer = new byte[1024];
				int readCount = 0;
				while (inputStream != null
						&& (readCount = inputStream.read(buffer)) > 0) {
					byteArrayOutputStream.write(buffer, 0, readCount);
				}
			} catch (IOException e) {
				throw e;
			} finally {
				if (byteArrayOutputStream != null)
					byteArrayOutputStream.close();
				if (inputStream != null)
					inputStream.close();
			}
			byte[] responseBody = null;
			if (byteArrayOutputStream != null)
				responseBody = byteArrayOutputStream.toByteArray();
			if (responseBody != null && responseBody.length > 0)
				response = new String(responseBody, "UTF-8");
		} catch (Exception e) {
			logger.error("error while making request to IP2MSISDN api: ", e);
		} finally {
			try {
				method.releaseConnection();
			} catch (Exception e) {
			}
		}
		return response;
	}

}
