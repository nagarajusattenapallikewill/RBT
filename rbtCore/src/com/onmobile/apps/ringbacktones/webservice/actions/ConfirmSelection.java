package com.onmobile.apps.ringbacktones.webservice.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.RBTProcessor;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author sridhar.sindiri
 *
 */
public class ConfirmSelection implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(ConfirmSelection.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext) 
	{
		WebServiceResponse webServiceResponse = null;
		try
		{
			String subscriberID = webServiceContext.getString(param_subscriberID);
			String smsID = webServiceContext.getString(param_smsID);
			if (subscriberID == null)
			{
				return getWebServiceResponse(INVALID_PARAMETER, webServiceContext);
			}

			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
			boolean toBeConsiderUds4NewUser = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "IS_CONSIDER_UDS_FOR_NEW_USER", "FALSE");

			if (toBeConsiderUds4NewUser || (subscriber != null && (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
					|| subscriber.subYes().equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
					|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_PENDING)
					|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_GRACE))))
			{
				int duration = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "SEL_WAIT_TIME_DOUBLE_CONFIRMATION", 30);

				ViralSMSTable viralSMS = null;
				if (smsID == null)
				{
					ViralSMSTable[] viralSMSes = RBTDBManager.getInstance().getLatestViralSMSesByTypeSubscriberAndTime(subscriberID, "SELCONFPENDING", duration);
					if (viralSMSes != null && viralSMSes.length > 0)
						viralSMS = viralSMSes[0];
				}
				else
					viralSMS = RBTDBManager.getInstance().getViralSMS(Long.parseLong(smsID));

				if (viralSMS == null
						|| !viralSMS.subID().equalsIgnoreCase(subscriberID)
						|| !viralSMS.type().equalsIgnoreCase("SELCONFPENDING")
						|| ((System.currentTimeMillis() - duration * 60 * 1000) > viralSMS.sentTime().getTime()))
				{
					return getWebServiceResponse(NO_PENDING_REQUESTS, webServiceContext);
				}

				if (viralSMS.callerID() != null)
					webServiceContext.put(param_callerID, viralSMS.callerID());

				webServiceContext.put(param_clipID, viralSMS.clipID());
				webServiceContext.put(param_allowPremiumContent, YES);

				Document document = XMLUtils.getDocumentFromString(viralSMS.extraInfo());
				Map<String, String> infoMap = null;
				if (document != null)
					infoMap = getAttributesMap(document.getDocumentElement());

				if (infoMap != null) {
					if(infoMap.containsKey("CATEGORY_ID"))
						webServiceContext.put(param_categoryID,
								infoMap.get("CATEGORY_ID"));
					if (infoMap.containsKey("SEL_INFO"))
						webServiceContext.put(param_modeInfo,
								infoMap.get("SEL_INFO"));
					if (infoMap.containsKey("COS_ID"))
						webServiceContext.put(param_cosID,
								infoMap.get("COS_ID"));
					if (infoMap.containsKey("SUBSCRIPTION_CLASS"))
						webServiceContext.put(param_subscriptionClass,
								infoMap.get("SUBSCRIPTION_CLASS"));
					if (infoMap.containsKey("UDS_OPTIN"))
						webServiceContext.put(param_selectionInfo +"_UDS_OPTIN",
								infoMap.get("UDS_OPTIN"));
				}

				RBTProcessor rbtProcessor = RBTAdminFacade.getRBTProcessorObject(webServiceContext);
				String selectionResponse = rbtProcessor.processSelection(webServiceContext);
				if (selectionResponse.equalsIgnoreCase(SUCCESS))
				{
					RBTDBManager.getInstance().deleteViralPromotionBySMSID(viralSMS.getSmsId());
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
		WebServiceResponse webServiceResponse = Utility.getWebServiceResponseXML(document);
		
		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);
		
		return webServiceResponse;
	}

	/**
	 * @param element
	 * @return
	 */
	private Map<String, String> getAttributesMap(Element element)
	{
		Map<String, String> attributeMap = new HashMap<String, String>();
		if (element != null)
		{
			NamedNodeMap namedNodeMap = element.getAttributes();
			for (int i = 0; i < namedNodeMap.getLength(); i++)
			{
				Attr attr = (Attr) namedNodeMap.item(i);
				attributeMap.put(attr.getName(), attr.getValue());
			}
		}

		return attributeMap;
	}
}
