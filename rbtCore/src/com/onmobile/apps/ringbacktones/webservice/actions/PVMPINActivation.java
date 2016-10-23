package com.onmobile.apps.ringbacktones.webservice.actions;

import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RBTSubscriberPIN;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.RBTSubscriberPINDao;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.RBTProcessor;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author sridhar.sindiri
 *
 */
public class PVMPINActivation implements WebServiceAction, WebServiceConstants
{
	
	private static Logger logger = Logger.getLogger(PVMPINActivation.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.factory.WebServiceActionProcessor#processAction(com.onmobile.apps.ringbacktones.webservice.factory.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext) 
	{
		WebServiceResponse webServiceResponse = null;
		try
		{
			String pinID = webServiceContext.getString(param_pinID);
			String subscriberID = webServiceContext.getString(param_subscriberID);
			if (pinID == null || subscriberID == null)
			{
				return getWebServiceResponse(INVALID_PARAMETER, webServiceContext);
			}

			String response = DataUtils.isValidPIN(pinID, subscriberID);
			if (!response.equalsIgnoreCase(VALID))
			{
				return getWebServiceResponse(response, webServiceContext);
			}

			RBTSubscriberPIN rbtSubscriberPIN = RBTSubscriberPINDao.getRBTSubsriberPIN(subscriberID, pinID);
			if (rbtSubscriberPIN != null && rbtSubscriberPIN.getActivationsCount() >= 1)
			{
				logger.warn("PIN: " + pinID + " is already used for activation by subscriber: " + subscriberID);
				return getWebServiceResponse(PIN_LIMIT_EXCEEDED, webServiceContext);
			}
			else if (rbtSubscriberPIN != null && rbtSubscriberPIN.getSelectionsCount() >= 1) 
			{
				logger.warn("PIN: " + pinID + " is already used for selection by subscriber: " + subscriberID + ". Activation is not allowed again");
				return getWebServiceResponse(PIN_LIMIT_EXCEEDED, webServiceContext);
			}

			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);

			String activationResponse = null;
			if (subscriber == null || subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED))
			{
				RBTProcessor rbtProcessor = RBTAdminFacade.getRBTProcessorObject(webServiceContext);
				webServiceContext.put(param_subscriptionClass, "FREE");
				activationResponse = rbtProcessor.processActivation(webServiceContext);
				if (activationResponse.equalsIgnoreCase(SUCCESS))
				{
					boolean success = false;
					Date currentDate = new Date();
					if (rbtSubscriberPIN != null)
					{
						//this will never happen. just a safety check
						int activationsCount = rbtSubscriberPIN.getActivationsCount();
						rbtSubscriberPIN.setActivationsCount(activationsCount + 1);
						rbtSubscriberPIN.setUpdationTime(currentDate);
						try 
						{
							success = RBTSubscriberPINDao.updateRBTSubscriberPIN(rbtSubscriberPIN);
						}
						catch(SQLException sqle) 
						{
							success = false;
						}
					}
					else
					{
						rbtSubscriberPIN = new RBTSubscriberPIN(subscriberID, pinID, 1, 0);
						rbtSubscriberPIN.setCreationTime(currentDate);
						rbtSubscriberPIN.setUpdationTime(currentDate);
						success = RBTSubscriberPINDao.createRBTSubscriberPIN(rbtSubscriberPIN);
					}

					if (success)
					{
						boolean isBlacklistSuccess = DataUtils.blacklistPIN(pinID, subscriberID);
						if (isBlacklistSuccess)
							logger.info("Successfully blacklisted the PIN : " + pinID + ", subscriberID : " + subscriberID);
						else
							logger.info("Failed blacklisting the PIN : " + pinID + ", subscriberID : " + subscriberID);
					}
				}

				webServiceResponse = getWebServiceResponse(activationResponse, webServiceContext);
			}
			else
			{
				webServiceResponse = getWebServiceResponse(ALREADY_ACTIVE, webServiceContext);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			webServiceResponse = getWebServiceResponse(ERROR, webServiceContext);
		}

		return webServiceResponse;
	}
	
	/**
	 * @param response
	 * @param webServiceContext
	 * @return
	 */
	private WebServiceResponse getWebServiceResponse(String response, WebServiceContext webServiceContext)
	{
		logger.info("Getting xml response for the response : " + response);

		webServiceContext.put(param_response, response);
		Document document = RBTAdminFacade.getRBTInformationObject(webServiceContext).getSelectionResponseDocument(webServiceContext);
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
}
