/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.vodacom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;

/**
 * @author vinayasimha.patil
 *
 */
public class VodacomCCRBTInformation extends VodacomRBTInformation
{
	/**
	 * @throws ParserConfigurationException
	 */
	public VodacomCCRBTInformation() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber)
	{
		Element element = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);

		String deactivatedBy = webServicesubscriber.getDeactivatedBy();
		Date startDate = webServicesubscriber.getStartDate();
		Date endDate = webServicesubscriber.getEndDate();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		if (deactivatedBy != null) element.setAttribute(DEACTIVATED_BY, deactivatedBy);
		if (startDate != null) element.setAttribute(START_DATE, dateFormat.format(startDate));
		if (endDate != null) element.setAttribute(END_DATE, dateFormat.format(endDate));

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.vodacom.VodacomRBTInformation#getSubscriberDownloadContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload)
	 */
	@Override
	protected Element getSubscriberDownloadContentElement(Document document,
			WebServiceContext task, WebServiceSubscriberDownload webServiceSubscriberDownload)
	{
		Element element = super.getSubscriberDownloadContentElement(document, task, webServiceSubscriberDownload);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(webServiceSubscriberDownload.getChargeClass());

		Utility.addPropertyElement(document, element, AMOUNT, DATA, chargeClass.getAmount());

		if (webServiceSubscriberDownload.getEndTime() != null)
			Utility.addPropertyElement(document, element, END_TIME, DATA, dateFormat.format(webServiceSubscriberDownload.getEndTime()));

		if (webServiceSubscriberDownload.getSelectedBy().equalsIgnoreCase("GIFT"))
		{
			element.setAttribute(TYPE, GIFT);

			HashMap<String, String> downloadInfoMap = DBUtility.getAttributeMapFromXML(webServiceSubscriberDownload.getDownloadInfo());
			if (downloadInfoMap != null)
				Utility.addPropertyElement(document, element, SENDER, DATA, downloadInfoMap.get("GIFTER"));
		}

		return element;
	}
}
