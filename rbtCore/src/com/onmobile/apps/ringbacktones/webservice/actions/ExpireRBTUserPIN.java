package com.onmobile.apps.ringbacktones.webservice.actions;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author sridhar.sindiri
 *
 */
public class ExpireRBTUserPIN extends AbstractUserPINAction
{
	private static Logger logger = Logger.getLogger(ExpireRBTUserPIN.class);

	private static SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		try
		{
			validateParameters(webServiceContext);
			String userID = webServiceContext.getString(param_userID);
			String type = webServiceContext.getString(param_type);

			boolean expired = RBTDBManager.getInstance().expireUserPIN(userID, type);
			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append(userID).append(",EXPIRE").append(",,").append(logDateFormat.format(new Date()));
			RBTEventLogger.logEvent(RBTEventLogger.Event.LOGINUSERPIN, logBuilder.toString());

			if (expired)
				response = SUCCESS;
			else
				response = FAILED;
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

		return getWebServiceResponse(response);
	}

	/**
	 * @param response
	 * @param rbtLoginUser
	 * @return
	 */
	private WebServiceResponse getWebServiceResponse(String response)
	{
		Document document = Utility.getResponseDocument(response);

		WebServiceResponse webServiceResponse = Utility.getWebServiceResponseXML(document);
		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
}
