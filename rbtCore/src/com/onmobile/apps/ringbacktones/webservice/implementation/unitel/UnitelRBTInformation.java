package com.onmobile.apps.ringbacktones.webservice.implementation.unitel;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicXMLElementGenerator;
import com.onmobile.apps.ringbacktones.webservice.implementation.telefonica.TelefonicaRBTInformation;
//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
public class UnitelRBTInformation extends TelefonicaRBTInformation
{
	/**
	 * @throws ParserConfigurationException
	 */
	public UnitelRBTInformation() throws ParserConfigurationException
	{
		super();
	}


	/* (non-Javadoc) for librabry
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberSettingContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberSettingContentElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberSetting webServiceSubscriberSetting) {
		task.put(param_showNextBillDate, "true");
		Element element = BasicXMLElementGenerator
				.generateSubscriberSettingContentElement(document, task,
						webServiceSubscriberSetting);
		
		Element elementforUnitel = UnitelXMLElementGenerator.
				addSubscriberSettingContentElement(document, element, task, webServiceSubscriberSetting);
		return elementforUnitel;
	}
	
	@Override
	protected Element getSubscriberDownloadContentElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberDownload webServiceSubscriberDownload) {
		task.put(param_showNextBillDate, "true");
		Element element = BasicXMLElementGenerator
				.generateSubscriberDownloadContentElement(document, task,
						webServiceSubscriberDownload);
		
		Element elementforUnitel = UnitelXMLElementGenerator.
				addSubscriberDownloadContentElement(document, element, task,
						webServiceSubscriberDownload);
		return elementforUnitel;
	}	
	
	@Override
	protected Element getSubscriberSettingsElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberSetting[] webServiceSubscriberSettings,
			SubscriberStatus[] settings) {
		Element element = document.createElement(SETTINGS);
		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (webServiceSubscriberSettings == null) {
			element.setAttribute(NO_OF_SETTINGS, "0");
			element.setAttribute(NO_OF_DEFAULT_SETTINGS, "0");
			element.setAttribute(NO_OF_SPECIAL_SETTINGS, "0");
		} else if(webServiceSubscriberSettings[0].isDefaultMusic()) {
			element.setAttribute(NO_OF_SETTINGS, "0");
			element.setAttribute(NO_OF_DEFAULT_SETTINGS, "0");
			element.setAttribute(NO_OF_SPECIAL_SETTINGS, "0");
			Element contentElem = UnitelXMLElementGenerator.getSubscriberDefaultSettingContentElement(
					document, task, webServiceSubscriberSettings[0]);
			contentsElem.appendChild(contentElem);
			
		} else {
			element.setAttribute(NO_OF_SETTINGS, String
					.valueOf(webServiceSubscriberSettings.length));

			int noOfDefaultSettings = 0;
			for (WebServiceSubscriberSetting webServiceSubscriberSetting : webServiceSubscriberSettings) {
				if (webServiceSubscriberSetting.getCallerID().equalsIgnoreCase(
						ALL))
					noOfDefaultSettings++;

				Element contentElem = getSubscriberSettingContentElement(
						document, task, webServiceSubscriberSetting);
				contentsElem.appendChild(contentElem);
			}

			element.setAttribute(NO_OF_DEFAULT_SETTINGS, String
					.valueOf(noOfDefaultSettings));
			element.setAttribute(NO_OF_SPECIAL_SETTINGS, String
					.valueOf(webServiceSubscriberSettings.length
							- noOfDefaultSettings));
		}

		return element;
	}
}

