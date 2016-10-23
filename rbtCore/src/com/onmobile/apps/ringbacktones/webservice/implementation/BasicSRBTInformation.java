package com.onmobile.apps.ringbacktones.webservice.implementation;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;

public class BasicSRBTInformation extends BasicRBTInformation{

	/**
	 * @throws ParserConfigurationException
	 */
	public BasicSRBTInformation() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.telefonica.TelefonicaRBTInformation#getRBTInformationDocument(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getRBTInformationDocument(WebServiceContext task)
	{
		Document document = super.getRBTInformationDocument(task);

		Parameters addGroupDetailsForSRBTParam = parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "ADD_GROUP_DETAILS_FOR_SRBT", "FALSE");

		if (addGroupDetailsForSRBTParam.getValue().equalsIgnoreCase("TRUE"))
		{
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
		}
		addCharegeClassElementInLibrary(document, task);
		return document;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.telefonica.TelefonicaRBTInformation#getRBTInformationDocument(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getSpecificRBTInformationDocument(WebServiceContext task)
	{
		Document document = super.getSpecificRBTInformationDocument(task);

		Parameters addGroupDetailsForSRBTParam = parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "ADD_GROUP_DETAILS_FOR_SRBT", "FALSE");

		if (addGroupDetailsForSRBTParam.getValue().equalsIgnoreCase("TRUE"))
		{
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
		}

		addCharegeClassElementInLibrary(document, task);
		
		return document;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber)
	{
		Element element = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);
		element = BasicSRBTXMLElementGenerator.generateSubscriberElement(document, task, element, webServicesubscriber);
		String nextChargeClass = rbtDBManager.getNextChargeClass(subscriber);
		if (nextChargeClass != null)
		{
			ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(nextChargeClass);
			if (chargeClass != null)
			{
				element.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
				element.setAttribute(NEXT_CHARGE_CLASS, chargeClass.getChargeClass());
			}
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
		if (task.containsKey(param_info) && task.getString(param_info).contains(LIBRARY_HISTORY))   
        {   
                task.remove(param_info);   
        } 

		Element element = super.getSubscriberSettingContentElement(document, task, webServiceSubscriberSetting);
		element = BasicSRBTXMLElementGenerator.generateSubscriberSettingContentElement(document, task, element, webServiceSubscriberSetting);

		return element;
	}
	
	protected void addCharegeClassElementInLibrary(Document document, WebServiceContext task){
		Element libraryElem = (Element) document.getElementsByTagName(LIBRARY).item(0);
		if (libraryElem != null)
		{
			Attr nextChargeClassAttr = libraryElem.getAttributeNode(NEXT_CHARGE_CLASS);
			if (nextChargeClassAttr == null)
			{
				Element subscriberElem = (Element)document.getElementsByTagName(SUBSCRIBER).item(0);
				String subscriberStatus = null;
				if(subscriberElem != null)
					subscriberStatus = subscriberElem.getAttribute(STATUS);

				Subscriber subscriber = (Subscriber) task.get(param_subscriber);
				String nextChargeClass = null;
				if (subscriberStatus != null && (subscriberStatus.equalsIgnoreCase(NEW_USER) || subscriberStatus.equalsIgnoreCase(DEACTIVE)))
				{
					CosDetails cos = DataUtils.getCos(task, subscriber);
					nextChargeClass = rbtDBManager.getChargeClassFromCos(cos, 0);
				}
				else
				{
					nextChargeClass = rbtDBManager.getNextChargeClass(subscriber);
				}

				if (nextChargeClass != null)
				{
					ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(nextChargeClass);
					libraryElem.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
					libraryElem.setAttribute(NEXT_CHARGE_CLASS, chargeClass.getChargeClass());
				}
			}
		}
	}

}
