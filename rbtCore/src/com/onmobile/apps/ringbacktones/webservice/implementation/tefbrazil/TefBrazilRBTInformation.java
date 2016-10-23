/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.tefbrazil;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;

/**
 * @author vinayasimha.patil
 */
public class TefBrazilRBTInformation extends BasicRBTInformation
{
	/**
	 * @throws ParserConfigurationException
	 */
	public TefBrazilRBTInformation() throws ParserConfigurationException
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #
	 * getRBTInformationDocument(com.onmobile.apps.ringbacktones.webservice.common
	 * .Task)
	 */
	@Override
	public Document getRBTInformationDocument(WebServiceContext task)
	{
		Document document = super.getRBTInformationDocument(task);

		Element subscriberElem = (Element) document.getElementsByTagName(
				SUBSCRIBER).item(0);
		boolean canAllow = subscriberElem.getAttribute(CAN_ALLOW)
				.equalsIgnoreCase(YES) ? true : false;
		boolean isValidPrefix = subscriberElem.getAttribute(IS_VALID_PREFIX)
				.equalsIgnoreCase(YES) ? true : false;
		String subscriberStatus = subscriberElem.getAttribute(STATUS);

		boolean addBookMarks = canAllow
				&& isValidPrefix
				&& !(subscriberStatus.equalsIgnoreCase(DEACT_PENDING) || subscriberStatus
						.equalsIgnoreCase(SUSPENDED));
		if (addBookMarks)
		{
			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] bookmarks = rbtDBManager
					.getSubscriberBookMarks(subscriberID);
			WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = getWebServiceSubscriberBookMarkObjects(
					task, bookmarks);
			Element bookMarksElem = getSubscriberBookMarksElement(document,
					task, webServiceSubscriberBookMarks, bookmarks);

			Element subDetailsElem = (Element) document.getElementsByTagName(
					SUBSCRIBER_DETAILS).item(0);
			subDetailsElem.appendChild(bookMarksElem);
		}

		Element libraryElem = (Element) document.getElementsByTagName(LIBRARY)
				.item(0);
		if (libraryElem != null)
		{
			Attr nextChargeClassAttr = libraryElem
					.getAttributeNode(NEXT_SELECTION_AMOUNT);
			if (nextChargeClassAttr == null)
			{
				subscriberElem = (Element) document.getElementsByTagName(
						SUBSCRIBER).item(0);
				subscriberStatus = subscriberElem.getAttribute(STATUS);

				Subscriber subscriber = (Subscriber) task.get(param_subscriber);
				String nextChargeClass = null;
				if (subscriberStatus.equalsIgnoreCase(NEW_USER)
						|| subscriberStatus.equalsIgnoreCase(DEACTIVE))
				{
					CosDetails cos = DataUtils.getCos(task, subscriber);
					nextChargeClass = rbtDBManager
							.getChargeClassFromCos(cos, 0);
				}
				else
				{
					nextChargeClass = rbtDBManager
							.getNextChargeClass(subscriber);
				}

				if (nextChargeClass != null
						&& !nextChargeClass.equalsIgnoreCase("DEFAULT"))
				{
					ChargeClass chargeClass = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									nextChargeClass);
					libraryElem.setAttribute(NEXT_SELECTION_AMOUNT,
							chargeClass.getAmount());
				}
			}
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #getSubscriberElement(org.w3c.dom.Document,
	 * com.onmobile.apps.ringbacktones.webservice.common.Task,
	 * com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber,
	 * com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber)
	{
		Element subscriberElem = super.getSubscriberElement(document, task,
				webServicesubscriber, subscriber);
		if (Utility.isUserActive(webServicesubscriber.getStatus()))
		{
			String nextBillingDate = Utility.getNextBillingDateOfServices(task)
					.get(webServicesubscriber.getRefID());
			if (nextBillingDate != null)
				subscriberElem.setAttribute(NEXT_BILLING_DATE, nextBillingDate);
		}

		return subscriberElem;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #getSubscriberLibraryElement(org.w3c.dom.Document,
	 * com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryElement(Document document, WebServiceContext task)
	{
		Element element = super.getSubscriberLibraryElement(document, task);

		String subscriberID = task.getString(param_subscriberID);
		SubscriberDownloads[] downloads = null;
		if (!task.containsKey(param_downloads))
			downloads = rbtDBManager.getActiveSubscriberDownloads(subscriberID);
		else
			downloads = (SubscriberDownloads[]) task.get(param_downloads);

		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(
				task, downloads);
		Element downloadsElem = getSubscriberDownloadsElement(document, task,
				webServiceSubscriberDownloads, downloads);

		element.appendChild(downloadsElem);

		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		else
			subscriber = rbtDBManager.getSubscriber(task
					.getString(param_subscriberID));

		String nextChargeClass = rbtDBManager.getNextChargeClass(subscriber);
		if (nextChargeClass != null
				&& !nextChargeClass.equalsIgnoreCase("DEFAULT"))
		{
			ChargeClass chargeClass = CacheManagerUtil
					.getChargeClassCacheManager().getChargeClass(
							nextChargeClass);
			element.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
		}

		return element;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #getSubscriberLibraryHistoryElement(org.w3c.dom.Document,
	 * com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryHistoryElement(Document document,
			WebServiceContext task)
	{
		Element element = super.getSubscriberLibraryHistoryElement(document,
				task);

		SubscriberDownloads[] subscriberDownloads = DataUtils
				.getFilteredDownloadHistory(task);
		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(
				task, subscriberDownloads);
		Element downloadsElem = getSubscriberDownloadsElement(document, task,
				webServiceSubscriberDownloads, subscriberDownloads);

		element.appendChild(downloadsElem);
		return element;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #getSubscriberDownloadContentElement(org.w3c.dom.Document,
	 * com.onmobile.apps.ringbacktones.webservice.common.Task,
	 * com.onmobile.apps.
	 * ringbacktones.webservice.common.WebServiceSubscriberDownload)
	 */
	@Override
	protected Element getSubscriberDownloadContentElement(Document document,
			WebServiceContext task, WebServiceSubscriberDownload webServiceSubscriberDownload)
	{
		Element downloadElem = super.getSubscriberDownloadContentElement(
				document, task, webServiceSubscriberDownload);
		
		String nextBillingDate = Utility.getNextBillingDateOfServices(task)
				.get(webServiceSubscriberDownload.getRefID());
		if (nextBillingDate != null)
		{
			Element nextBillingDateElem = Utility.getPropertyElement(
					downloadElem, NEXT_BILLING_DATE);
			if (nextBillingDateElem == null)
				Utility.addPropertyElement(document, downloadElem,
						NEXT_BILLING_DATE, DATA, nextBillingDate);
		}

		return downloadElem;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #canBeGifted(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected String canBeGifted(String subscriberID, String callerID,
			String contentID)
	{
		SubscriberDetail subscriberDetail = RbtServicesMgr
				.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
		if (subscriberDetail != null && subscriberDetail.isValidSubscriber())
			return VALID;

		return INVALID;
	}
}
