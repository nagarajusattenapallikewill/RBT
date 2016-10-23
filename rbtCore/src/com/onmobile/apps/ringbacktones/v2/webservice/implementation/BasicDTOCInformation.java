package com.onmobile.apps.ringbacktones.v2.webservice.implementation;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicCCCRBTInformation;

public class BasicDTOCInformation extends BasicCCCRBTInformation{

	/**
	 * @throws ParserConfigurationException
	 */
	public BasicDTOCInformation() throws ParserConfigurationException
	{
		super();
	}
	
	@Override
	protected Element getSubscriberElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber)
	{
		Element element = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);
		if(element.getAttribute(NEXT_BILLING_DATE) == null || element.getAttribute(NEXT_BILLING_DATE).trim().length() == 0){
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			SubscriptionClass subscriptionClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(webServicesubscriber.getSubscriptionClass());
			String subscriptionPeriod = subscriptionClass  == null ? null : subscriptionClass.getSubscriptionPeriod();
			Date nextBillingDate = webServicesubscriber.getNextChargingDate() != null? Utility.getNextDate(subscriptionPeriod, webServicesubscriber.getNextChargingDate()) : null;
			if(nextBillingDate != null) {
				element.setAttribute(NEXT_BILLING_DATE, dateFormat.format(nextBillingDate));
			}
		}
		
		CosDetails cosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(webServicesubscriber.getCosID());
		
		String cosType = cosDetail != null ? cosDetail.getCosType() : null;
		
		if(cosType == null || (cosType = cosType.trim()).length() == 0) {
			cosType = "NORMAL";
		}
		
		element.setAttribute(SUBSCRIBER_TYPE, cosType);

		return element;
	}
}
