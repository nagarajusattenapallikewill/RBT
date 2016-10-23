package com.onmobile.apps.ringbacktones.webservice.actions;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author sridhar.sindiri
 * 
 */
public class AddGCMRegistration implements WebServiceAction, WebServiceConstants {

	private static final Logger logger = Logger.getLogger(AddGCMRegistration.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		String response = ERROR;
		try {
			String regID = webServiceContext.getString(param_userID);
			String subID = webServiceContext.getString(param_subscriberID);
			String type = webServiceContext.getString(param_type);
			if (regID == null || subID == null) {
				throw new WebServiceException(
						"userID or subscriberID parameter is missing", 0,
						INVALID_PARAMETER);
			}

			Subscriber subscriber = null;
			try {
				subscriber = DataUtils.getSubscriber(webServiceContext);
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
			}

			response = DataUtils.isValidUser(webServiceContext, subscriber);
			if (!response.equals(VALID)) {
				logger.info("response: " + response);
				throw new WebServiceException("invalid number", 0, response);
			}
			boolean isSuccess = false;
			
			String oldRegistrationId = RBTDBManager.getInstance().getRegistrationIdBySubscriberIdAndType(subID, type);
			if (oldRegistrationId == null) {
				logger.info("SubscriberId-osType combination doesn't exist in the DB. Inserting now.");
				isSuccess = RBTDBManager.getInstance().insertGCMRegistration(regID, subID, type);
			} else if (!oldRegistrationId.equals(regID)){
				logger.info("SubscriberId-osType combination already existed and registration Id is different. Updating with new registration Id.");
				isSuccess = RBTDBManager.getInstance().updateRegistrationIdBySubscriberIdAndType(subID, type, regID);
			} else {
				logger.info("SubscriberId-osType combination already existed and registration Id is same as that already present in DB. So doing no operation on DB.");
				isSuccess = true;
			}

			if (isSuccess)
				response = SUCCESS;
			else
				response = FAILED;
		} catch (WebServiceException we) {
			response = we.getResponseString();
			logger.debug(we.getMessage());
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
		Document document = Utility.getResponseDocument(response);
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
}
