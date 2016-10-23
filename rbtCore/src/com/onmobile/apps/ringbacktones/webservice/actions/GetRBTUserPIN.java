package com.onmobile.apps.ringbacktones.webservice.actions;

import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicXMLElementGenerator;

/**
 * @author sridhar.sindiri
 *
 */
public class GetRBTUserPIN extends AbstractUserPINAction
{
	private static Logger logger = Logger.getLogger(GetRBTUserPIN.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		RBTLoginUser rbtLoginUser = null;
		try
		{
			validateParameters(webServiceContext);

			String userID = webServiceContext.getString(param_userID);
			String subscriberID = webServiceContext.getString(param_subscriberID);
			String type = webServiceContext.getString(param_type);
			boolean encryptPassword = webServiceContext.containsKey(param_encryptPassword)
					&& webServiceContext.getString(param_encryptPassword).equalsIgnoreCase(YES);

			rbtLoginUser = RBTDBManager.getInstance().getRBTLoginUser(userID, null, subscriberID, type, null, encryptPassword);
			validateUserPIN(webServiceContext, rbtLoginUser);

			response = SUCCESS;
		}
		catch (WebServiceException we)
		{
			response = we.getResponseString();
			logger.debug(we.getMessage());
		}
		catch (Exception e)
		{
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response, rbtLoginUser);
	}

	/**
	 * @param webServiceContext
	 * @param rbtLoginUser
	 * @throws WebServiceException
	 */
	private void validateUserPIN(WebServiceContext webServiceContext, RBTLoginUser rbtLoginUser) throws WebServiceException
	{
		if (rbtLoginUser == null)
		{
			throw new WebServiceException("No pins are available", 0, NO_PINS_AVAILABLE);
		}

		Date creationTime = rbtLoginUser.creationTime();
		int expiryMins = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "PASSWORD_EXPIRY_TIME_IN_MINS", 15);
		if (rbtLoginUser.password() == null
				|| (creationTime != null && ((System.currentTimeMillis() - creationTime.getTime()) > (expiryMins * 60 * 1000))))
		{
			throw new WebServiceException("Pin is expired", 0, PIN_EXPIRED);
		}
	}

	/**
	 * @param response
	 * @param rbtLoginUser
	 * @return
	 */
	private WebServiceResponse getWebServiceResponse(String response, RBTLoginUser rbtLoginUser)
	{
		Document document = Utility.getResponseDocument(response);
		Element element = document.getDocumentElement();
		
		if (response.equals(SUCCESS))
		{
			Element applicationDetailsElem = document.createElement(APPLICATION_DETAILS);
			element.appendChild(applicationDetailsElem);
			Element rbtLoginUsersElement = document.createElement(RBT_LOGIN_USERS);
			applicationDetailsElem.appendChild(rbtLoginUsersElement);

			Element rbtLoginUserElement = BasicXMLElementGenerator.generateRBTLoginUserElement(document, rbtLoginUser);
			if (rbtLoginUserElement != null)
				rbtLoginUsersElement.appendChild(rbtLoginUserElement);
		}

		WebServiceResponse webServiceResponse = Utility.getWebServiceResponseXML(document);
		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
}
