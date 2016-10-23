/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.vivabahrain;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.implementation.telefonica.TelefonicaRBTInformation;

public class VivaRBTInformation extends TelefonicaRBTInformation {
	public VivaRBTInformation() throws ParserConfigurationException {
		super();
	}

	@Override
	public Document getSpecificRBTInformationDocument(WebServiceContext task) {
		Document document = super.getSpecificRBTInformationDocument(task);
		Element subscriberElem = (Element) document.getElementsByTagName(
				SUBSCRIBER).item(0);
		Subscriber sub = rbtDBManager.getSubscriber(task
				.getString(param_subscriberID));
		WebServiceSubscriber webServiceSubscriber = getWebServiceSubscriberObject(
				task, sub);
		if (webServiceSubscriber != null) {
			if (Utility.isUserActive(webServiceSubscriber.getStatus())) {
				String nextBillingDate = Utility.getNextBillingDateOfServices(
						task).get(webServiceSubscriber.getRefID());
				if (nextBillingDate != null) {
					if (SUBSCRIBER.equalsIgnoreCase(task.getString(param_info))) {
						subscriberElem.setAttribute(NEXT_BILLING_DATE,
								nextBillingDate);
					}
				}
			}
		}
		return document;

	}

	@Override
	public Document getRBTInformationDocument(WebServiceContext task) {
		Document document = super.getRBTInformationDocument(task);

		Element subscriberElem = (Element) document.getElementsByTagName(
				SUBSCRIBER).item(0);
		Subscriber sub = (Subscriber) task.get(param_subscriber);
		WebServiceSubscriber webServiceSubscriber = getWebServiceSubscriberObject(
				task, sub);
		if (webServiceSubscriber != null) {
			if (Utility.isUserActive(webServiceSubscriber.getStatus())) {
				String nextBillingDate = Utility.getNextBillingDateOfServices(
						task).get(webServiceSubscriber.getRefID());
				if (nextBillingDate != null) {
					subscriberElem.setAttribute(NEXT_BILLING_DATE,
							nextBillingDate);
				}
			}
		}
		return document;
	}

	@Override
	protected Element getSubscriberDownloadContentElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberDownload webServiceSubscriberDownload) {
		Element element = super.getSubscriberDownloadContentElement(document,
				task, webServiceSubscriberDownload);
		if (webServiceSubscriberDownload != null) {
			if (Utility.isUserActive(webServiceSubscriberDownload
					.getDownloadStatus())) {
				String nextBillingDate = Utility.getNextBillingDateOfServices(
						task).get(webServiceSubscriberDownload.getRefID());
				if (nextBillingDate != null) {
					Utility.addPropertyElement(document, element,
							NEXT_BILLING_DATE, DATA, nextBillingDate);
				}
			}
		}
		return element;
	}
}
