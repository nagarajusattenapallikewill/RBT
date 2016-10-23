/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.virgin;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicXMLElementGenerator;

/**
 * @author vinayasimha.patil
 *
 */
public class VirginRBTInformation extends BasicRBTInformation
{
	/**
	 * @throws ParserConfigurationException
	 */
	public VirginRBTInformation() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getRBTInformationDocument(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getRBTInformationDocument(WebServiceContext task)
	{
		Document document = super.getRBTInformationDocument(task);

		Element subscriberElem = (Element)document.getElementsByTagName(SUBSCRIBER).item(0);
		boolean canAllow = subscriberElem.getAttribute(CAN_ALLOW).equalsIgnoreCase(YES) ? true : false;
		boolean isValidPrefix = subscriberElem.getAttribute(IS_VALID_PREFIX).equalsIgnoreCase(YES) ? true : false;
		String subscriberStatus = subscriberElem.getAttribute(STATUS);

		boolean addBookMarks = canAllow && isValidPrefix
		&& !(subscriberStatus.equalsIgnoreCase(DEACT_PENDING) || subscriberStatus.equalsIgnoreCase(SUSPENDED));
		if (addBookMarks)
		{
			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] bookmarks = rbtDBManager.getSubscriberBookMarks(subscriberID);
			WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = getWebServiceSubscriberBookMarkObjects(task, bookmarks);
			Element bookMarksElement = getSubscriberBookMarksElement(document, task, webServiceSubscriberBookMarks, bookmarks);

			Element subDetailsElem = (Element)document.getElementsByTagName(SUBSCRIBER_DETAILS).item(0);
			subDetailsElem.appendChild(bookMarksElement);
		}

		return document;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberBookMarkContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark)
	 */
	@Override
	protected Element getSubscriberBookMarkContentElement(Document document,
			WebServiceSubscriberBookMark webServiceSubscriberBookMark,WebServiceContext task)
	{
		Element element = super.getSubscriberBookMarkContentElement(document, webServiceSubscriberBookMark, task);

		Clip clip = rbtCacheManager.getClip(webServiceSubscriberBookMark.getToneID());
		Category category = rbtCacheManager.getCategory(webServiceSubscriberBookMark.getCategoryID());

		String amount = "0";
		String validity = "";

		String parentClassType = "DEFAULT";
		if (category != null)
			parentClassType = category.getClassType();

		String childClassType = clip.getClassType();

		ChargeClass chargeClass = DataUtils.getValidChargeClass(parentClassType, childClassType);
		if (chargeClass != null)
		{
			amount = chargeClass.getAmount();
			validity = chargeClass.getSelectionPeriod();
		}

		Utility.addPropertyElement(document, element, AMOUNT, DATA, amount);
		Utility.addPropertyElement(document, element, PERIOD, DATA, validity);

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getCallDetailsElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber)
	 */
	@Override
	protected Element getCallDetailsElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServiceSubscriber, Subscriber subscriber)
	{
		Element element = super.getCallDetailsElement(document, task, webServiceSubscriber, subscriber);

		Element exitSMSElem = BasicXMLElementGenerator.generateExitSMSElement(document, task, webServiceSubscriber);
		element.appendChild(exitSMSElem);

		return element;
	}
}
