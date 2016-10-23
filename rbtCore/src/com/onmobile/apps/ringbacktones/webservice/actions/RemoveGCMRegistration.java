package com.onmobile.apps.ringbacktones.webservice.actions;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author sridhar.sindiri
 *
 */
public class RemoveGCMRegistration implements WebServiceAction, WebServiceConstants {

	private static final Logger logger = Logger.getLogger(RemoveGCMRegistration.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		String response = ERROR;
		try {
			String regID = webServiceContext.getString(param_userID);
			String subID = webServiceContext.getString(param_subscriberID);
			if (regID == null || subID == null) {
				throw new WebServiceException(
						"userID or type parameter is missing", 0,
						INVALID_PARAMETER);
			}

			boolean isSuccess = RBTDBManager.getInstance().deleteGCMRegistrationBySubIDAndRegID(regID, subID);

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
