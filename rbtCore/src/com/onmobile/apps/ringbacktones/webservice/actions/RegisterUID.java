/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.actions;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

/**
 * @author vinayasimha.patil
 * 
 */
public class RegisterUID implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(RegisterUID.class);

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#
	 * processAction
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		try
		{
			validateRequest(webServiceContext);

			String uid = webServiceContext.getString(param_uid);
			String subscriberID = webServiceContext
					.getString(param_subscriberID);
			String type = webServiceContext.getString(param_type);

			// Removing old UID for the subscriber
			RBTDBManager.getInstance().deleteRBTLoginUserBySubscriberID(
					subscriberID, type);

			String[] uidTokens = uid.trim().split(" ");
			// Considering only last token as UID, and ignoring all SMS
			// keywords.
			uid = uidTokens[uidTokens.length - 1];

			// And registering with new UID
			RBTLoginUser rbtLoginUser = RBTDBManager
					.getInstance()
					.addRBTLoginUser(uid, null, subscriberID, type, null, false);
			if (rbtLoginUser != null)
				response = SUCCESS;
			else
				response = FAILED;
		}
		catch (WebServiceException e)
		{
			response = e.getResponseString();
			logger.debug(e.getMessage());
		}
		catch (Exception e)
		{
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response);
	}

	protected void validateRequest(WebServiceContext webServiceContext)
			throws WebServiceException
	{
		String uid = webServiceContext.getString(param_uid);
		String subscriberID = webServiceContext.getString(param_subscriberID);
		String type = webServiceContext.getString(param_type);
		if (uid == null || subscriberID == null || type == null)
		{
			throw new WebServiceException(
					"uid or subscriberID or type parameter is missing", 0,
					INVALID_PARAMETER);
		}

		Subscriber subscriber = null;
		try {
			subscriber = DataUtils.getSubscriber(webServiceContext);
		} catch (RBTException e) {
			logger.error(e.getMessage(), e);
		}

		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("MOBILEAPP", "TO_VALID_PREFIX","TRUE");
		String response = DataUtils.isValidUser(webServiceContext, subscriber);
		if(parameter.getValue().equalsIgnoreCase("TRUE")) {
			if (!response.equals(VALID)) {
				logger.info("response: " + response);
				throw new WebServiceException(
						"invalid number", 0, response);
			}
		} 

		String[] uidTokens = uid.trim().split(" ");
		// Considering only last token as UID, and ignoring all SMS keywords.
		uid = uidTokens[uidTokens.length - 1];
		RBTLoginUser rbtLoginUser = RBTDBManager.getInstance().getRBTLoginUser(
				uid, null, null, type, null, false);
		if (rbtLoginUser != null) { 
			parameter = CacheManagerUtil.getParametersCacheManager().getParameter("MOBILEAPP", "UPGRADE_REGISTRATION_ALLOWED","TRUE");
		    if(parameter.getValue().equalsIgnoreCase("TRUE")) {
			   RBTDBManager.getInstance().updateRBTLoginUser(uid, null, null, subscriberID, type,
							null, null, false, null, null);
			} else {
			throw new WebServiceException(
					"uid already registered for subscriber "
							+ rbtLoginUser.subscriberID(), 0, ALREADY_USED);
			}
		}
	}

	private WebServiceResponse getWebServiceResponse(String response)
	{
		WebServiceResponse webServiceResponse = new WebServiceResponse(response);
		webServiceResponse.setContentType("text/plain; charset=utf-8");
		ResponseWriter responseWriter = WebServiceResponseFactory
				.getResponseWriter(StringResponseWriter.class);
		webServiceResponse.setResponseWriter(responseWriter);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
}
