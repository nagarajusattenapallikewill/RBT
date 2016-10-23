package com.onmobile.apps.ringbacktones.webservice.implementation.grameen;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicCCCRBTInformation;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicCCCXMLElementGenerator;

public class GrameenPhoneCCCRBTInformation extends GrameenPhoneRBTInformation{

	public GrameenPhoneCCCRBTInformation() throws ParserConfigurationException {
		super();
	}

	protected WebServiceSubscriber getWebServiceSubscriberObject(WebServiceContext task, Subscriber subscriber)
	{
	
		WebServiceSubscriber webServiceSubscriber = super.getWebServiceSubscriberObject(task, subscriber);
		HashMap<String, String> extraInfo = rbtDBManager.getExtraInfoMap(subscriber);
		String status = webServiceSubscriber.getStatus();
		
		//If subscriber is voluntary suspension
		if (status.equalsIgnoreCase(ACTIVE) && extraInfo != null && extraInfo.containsKey(iRBTConstant.VOLUNTARY)&&extraInfo.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("TRUE"))
			status = PAUSED;
		
		webServiceSubscriber.setStatus(status);
		
		return webServiceSubscriber;
		
	}	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.telefonica.TelefonicaRBTInformation#getRBTInformationDocument(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getRBTInformationDocument(WebServiceContext task)
	{
		Document document = super.getRBTInformationDocument(task);

		Element subscriberElem = (Element)document.getElementsByTagName(SUBSCRIBER).item(0);
		boolean canAllow = subscriberElem.getAttribute(CAN_ALLOW).equalsIgnoreCase(YES) ? true : false;
		boolean isValidPrefix = subscriberElem.getAttribute(IS_VALID_PREFIX).equalsIgnoreCase(YES) ? true : false;
		String subscriberStatus = subscriberElem.getAttribute(STATUS);

		boolean addDetails = canAllow && isValidPrefix && !(subscriberStatus.equalsIgnoreCase(DEACT_PENDING)
				|| subscriberStatus.equalsIgnoreCase(SUSPENDED));
		if (addDetails)
		{
			Element subDetailsElem = (Element)document.getElementsByTagName(SUBSCRIBER_DETAILS).item(0);
			Element groupDetailsElem = getSubscriberGroupDetailsElement(document, task);
			subDetailsElem.appendChild(groupDetailsElem);
		}

		return document;
	}
	
	@Override
	protected Element getSubscriberLibraryElement(Document document,
			WebServiceContext task)
	{
		return super.getSubscriberLibraryElement(document, task);
	}
	
	@Override
	protected Element getSubscriberLibraryHistoryElement(Document document,
			WebServiceContext task)
	{
		return super.getSubscriberLibraryHistoryElement(document, task);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.romania.RomaniaRBTInformation#getSubscriberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber)
	{
		Element element = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);
		element = BasicCCCXMLElementGenerator.generateSubscriberElement(document, task, element, webServicesubscriber);
		String nextChargingDate = Utility.getNextBillingDateOfServices(task).get(webServicesubscriber.getRefID() + "_lastChargingDate");
		if(nextChargingDate != null) {
			element.setAttribute(NEXT_CHARGING_DATE, nextChargingDate);
		}
		String lastAmountCharged = Utility.getNextBillingDateOfServices(task).get(webServicesubscriber.getRefID() + "_lastAmountCharged");
		if(lastAmountCharged!=null){
			element.setAttribute(LAST_CHARGE_AMOUNT, lastAmountCharged);
		}
		String lastTransactionType = Utility.getNextBillingDateOfServices(task).get(webServicesubscriber.getRefID() + "_lastTransactionType");
		if(lastTransactionType!=null){
			element.setAttribute(LAST_TRANSACTION_TYPE, lastTransactionType);
		}
		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberSettingContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting)
	 */
	@Override
	protected Element getSubscriberSettingContentElement(Document document,
			WebServiceContext task, WebServiceSubscriberSetting webServiceSubscriberSetting)
	{
		Element element = super.getSubscriberSettingContentElement(document, task, webServiceSubscriberSetting);
		element = BasicCCCXMLElementGenerator.generateSubscriberSettingContentElement(document, task, element, webServiceSubscriberSetting);

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberDownloadContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload)
	 */
	@Override
	protected Element getSubscriberDownloadContentElement(Document document,
			WebServiceContext task, WebServiceSubscriberDownload webServiceSubscriberDownload)
	{
		Element element = super.getSubscriberDownloadContentElement(document, task, webServiceSubscriberDownload);
		element = BasicCCCXMLElementGenerator.generateSubscriberDownloadContentElement(document, task, element, webServiceSubscriberDownload);

		return element;
	}

	

}
