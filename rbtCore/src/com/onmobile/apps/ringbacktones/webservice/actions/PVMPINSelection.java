package com.onmobile.apps.ringbacktones.webservice.actions;

import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RBTSubscriberPIN;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.RBTSubscriberPINDao;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.RBTProcessor;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

/**
 * @author sridhar.sindiri
 *
 */
public class PVMPINSelection implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(PVMPINSelection.class);

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
			if (rbtSubscriberPIN != null && (rbtSubscriberPIN.getActivationsCount() >= 1 ||  rbtSubscriberPIN.getSelectionsCount() >= 2))
			{
				return getWebServiceResponse(PIN_LIMIT_EXCEEDED, webServiceContext);
			}

			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);

			if (subscriber != null && (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
					|| subscriber.subYes().equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
					|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_PENDING)
					|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_GRACE)))
			{
				RBTProcessor rbtProcessor = RBTAdminFacade.getRBTProcessorObject(webServiceContext);
				webServiceContext.put(param_chargeClass, "FREE");
				webServiceContext.put(param_useUIChargeClass, YES);
				String selectionResponse = rbtProcessor.processSelection(webServiceContext);

				if (selectionResponse.equalsIgnoreCase(SUCCESS))
				{
					Date currentDate = new Date();
					if (rbtSubscriberPIN != null)
					{
						int selectionsCount = rbtSubscriberPIN.getSelectionsCount();
						rbtSubscriberPIN.setSelectionsCount(selectionsCount + 1);
						rbtSubscriberPIN.setUpdationTime(currentDate);
						try 
						{
							RBTSubscriberPINDao.updateRBTSubscriberPIN(rbtSubscriberPIN);
							if (rbtSubscriberPIN.getSelectionsCount() >= 2)
							{
								boolean isBlacklistSuccess = DataUtils.blacklistPIN(pinID, subscriberID);
								if (isBlacklistSuccess)
									logger.info("Successfully blacklisted the PIN : " + pinID + ", subscriberID : " + subscriberID);
								else
									logger.info("Failed blacklisting the PIN : " + pinID + ", subscriberID : " + subscriberID);
							}
						} 
						catch(SQLException sqle) 
						{
						}
					}
					else
					{
						rbtSubscriberPIN = new RBTSubscriberPIN(subscriberID, pinID, 0, 1);
						rbtSubscriberPIN.setCreationTime(currentDate);
						rbtSubscriberPIN.setUpdationTime(currentDate);
						RBTSubscriberPINDao.createRBTSubscriberPIN(rbtSubscriberPIN);
					}
				}
				webServiceResponse = getWebServiceResponse(selectionResponse, webServiceContext);
			}
			else
			{
				webServiceResponse = getWebServiceResponse(USER_NOT_ACTIVE, webServiceContext);
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
