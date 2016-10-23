package com.onmobile.apps.ringbacktones.webservice.actions;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.processor.impl.NextChargeClassAbstractProcessor;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NewChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NewSubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NextServiceCharge;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

public class GetNextServiceChargeClass implements WebServiceAction,
		WebServiceConstants {

	private static Logger logger = Logger
			.getLogger(GetNextServiceChargeClass.class);

	@Override
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		String response = ERROR;

		NextChargeClassAbstractProcessor nextChargeClassAbstractProcessor = (NextChargeClassAbstractProcessor) ConfigUtil
				.getBean(BeanConstant.CHARGE_CLASS_PROCESSOR);

		NextServiceCharge nextServiceCharge = null;
		try {
			nextServiceCharge = nextChargeClassAbstractProcessor
					.getNextChargeAndServiceClass(webServiceContext);
			if(nextServiceCharge != null){
				response = SUCCESS ;
			}
		} catch(UserException e)
		{
			response = e.getMessage();
			logger.debug(e.getMessage(), e);
		}
		catch(Exception e)
		{
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response, nextServiceCharge);

		
	}

	private WebServiceResponse getWebServiceResponse(String response,
			NextServiceCharge nextServiceCharge) {
		if (logger.isDebugEnabled())
			logger.debug("chargeClassesMap : " + nextServiceCharge);

		Document document = buildChargeClassesXML(response, nextServiceCharge);
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);
		return webServiceResponse;
	}

	/**
	 * @param response
	 * @param NextServiceCharge
	 * @return
	 */
	private Document buildChargeClassesXML(String response,
			NextServiceCharge nextServiceCharge) {
		Document document = Utility.getResponseDocument(response);
		Element element = document.getDocumentElement();

		if (!response.equals(SUCCESS))
			return document;

		Element chargeClassesElement = document
				.createElement(NEXTSERVICE_CHARGE);
		element.appendChild(chargeClassesElement);

		Element contentsElem = document.createElement(CONTENTS);
		chargeClassesElement.appendChild(contentsElem);

		NewChargeClass newChargeClass = nextServiceCharge.getChargeClass();
		NewSubscriptionClass newSubscriptionClass = nextServiceCharge
				.getSubscriptionClass();

		if (newChargeClass != null) {

			Element contentElem = document.createElement(NEWCHARGE_CLASS);
			contentElem.setAttribute(SERVICE_KEY,
					newChargeClass.getServiceKey());
			contentElem.setAttribute(AMOUNT, newChargeClass.getAmount());
			contentElem.setAttribute(VALIDITY, newChargeClass.getValiditiy());
			contentElem.setAttribute(IS_RENEWAL, newChargeClass.getIsRenewal()
					+ "");
			contentElem.setAttribute(RENEWAL_AMOUNT,
					newChargeClass.getRenewalAmount());
			contentElem.setAttribute(RENEWAL_VALIDITY,
					newChargeClass.getRenewalValidity());
			contentElem
					.setAttribute(OFFER_ID, newChargeClass.getOfferID() + "");

			contentsElem.appendChild(contentElem);

		}

		if (newSubscriptionClass != null) {

			Element contentElem = document.createElement(NEWSUBSCRIPTION_CLASS);
			contentElem.setAttribute(SERVICE_KEY,
					newSubscriptionClass.getServiceKey());
			contentElem.setAttribute(AMOUNT, newSubscriptionClass.getAmount());
			contentElem.setAttribute(VALIDITY,
					newSubscriptionClass.getValiditiy());
			contentElem.setAttribute(IS_RENEWAL,
					newSubscriptionClass.getIsRenewal() + "");
			contentElem.setAttribute(RENEWAL_AMOUNT,
					newSubscriptionClass.getRenewalAmount());
			contentElem.setAttribute(RENEWAL_VALIDITY,
					newSubscriptionClass.getRenewalValidity());
			contentElem.setAttribute(OFFER_ID,
					newSubscriptionClass.getOfferID() + "");

			contentsElem.appendChild(contentElem);

		}

		return document;
	}

}
