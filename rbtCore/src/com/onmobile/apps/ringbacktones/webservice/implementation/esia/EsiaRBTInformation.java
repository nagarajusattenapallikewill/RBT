/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.esia;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.ChargePromoTypeMap;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassMap;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;

/**
 * @author vinayasimha.patil
 *
 */
public class EsiaRBTInformation extends BasicRBTInformation
{

	/**
	 * @throws ParserConfigurationException
	 */
	public EsiaRBTInformation() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServicesubscriber,
			Subscriber subscriber)
	{
		Element element = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);
		
		boolean userDelayedDeact = DataUtils.isUserInDelayedDeactivationState(subscriber);
		if (userDelayedDeact)
			element.setAttribute(DELAYED_DEACT, YES);

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

		ChargeClassMap chargeClassMaps[] = EsiaUtility.getChargeClassMapsForFinalClassType(webServiceSubscriberSetting.getChargeClass(), "VUI");
		String[] chargeOptModel = EsiaUtility.getChargeOptModel(chargeClassMaps);
		if (chargeOptModel != null && chargeOptModel.length >= 2)
		{
			Utility.addPropertyElement(document, element, CHARGING_MODEL, DATA, chargeOptModel[0]);
			Utility.addPropertyElement(document, element, OPTINOUT_MODEL, DATA, chargeOptModel[1]);
		}

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

		ChargePromoTypeMap[] chargePromoTypeMaps = EsiaUtility.getChargePromoTypeMaps();
		if (chargePromoTypeMaps != null && chargePromoTypeMaps.length > 0)
		{
			Element chargingModelelem = document.createElement(CHARGING_MODELS);

			String chargingModes = "";
			for (ChargePromoTypeMap chargePromoTypeMap : chargePromoTypeMaps)
				chargingModes += chargePromoTypeMap.promoType() + ",";
			chargingModes = chargingModes.substring(0, chargingModes.length()-1);

			chargingModelelem.setAttribute(CHARGING_MODELS, chargingModes);
			element.appendChild(chargingModelelem);
		}

		return element;
	}
}
