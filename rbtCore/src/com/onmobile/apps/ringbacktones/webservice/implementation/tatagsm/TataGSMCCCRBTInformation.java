/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.tatagsm;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicCCCXMLElementGenerator;

/**
 * @author vinayasimha.patil
 *
 */
public class TataGSMCCCRBTInformation extends TataGSMRBTInformation
{

	/**
	 * @throws ParserConfigurationException
	 */
	public TataGSMCCCRBTInformation() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.tatagsm.TataGSMRBTInformation#getSubscriberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber)
	{
		Element element = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);
		element = BasicCCCXMLElementGenerator.generateSubscriberElement(document, task, element, webServicesubscriber);

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
}
