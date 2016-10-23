package com.onmobile.apps.ringbacktones.webservice.actions;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

public class GetOrSetNotificationStatusAction implements WebServiceAction, WebServiceConstants{

	private static final Logger logger = Logger.getLogger(GetOrSetNotificationStatusAction.class);

	private static final String ENABLED = "ENABLED";
	private static final String DISABLED = "DISABLED";

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		String response = ERROR;

		String action = webServiceContext.getString(param_info);
		String subcriberId = webServiceContext.getString(param_subscriberID);
		String reqStatus = webServiceContext.getString(param_value);
		String osType = webServiceContext.getString(param_type);
		String regID = webServiceContext.getString(param_userID);
		Boolean respStatus = null;

		logger.info("Parameters:- " + param_info + ": " + action + ", "+ param_subscriberID +": " + subcriberId + ", " + param_value +
				": " + reqStatus + ", " + param_type + ": " + osType);
		if (action != null && action.equals(action_get)) {
			respStatus = RBTDBManager.getInstance().getNotificationStatus(subcriberId, osType);
			if (respStatus != null && respStatus) {
				return getWebServiceResponse(ENABLED);
			}
			else {
				return getWebServiceResponse(DISABLED);
			}
		} else if (reqStatus != null){
			String oldRegistrationId = RBTDBManager.getInstance().getRegistrationIdBySubscriberIdAndType(subcriberId, osType);
			if(oldRegistrationId != null ) {
				boolean toUpdateRegId = true;
				if(oldRegistrationId.equals(regID)) {
					toUpdateRegId = false;
				}
				respStatus = RBTDBManager.getInstance().setNotificationStatus(subcriberId, reqStatus, osType, toUpdateRegId, regID);
			}
			else {
				respStatus = RBTDBManager.getInstance().insertGCMRegistration(regID, subcriberId, osType, reqStatus);
			}
			if (respStatus != null && respStatus) {
				return getWebServiceResponse(SUCCESS);
			}
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
