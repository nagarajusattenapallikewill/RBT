package com.onmobile.apps.ringbacktones.provisioning.implementation.service.tatagsm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author sridhar.sindiri
 *
 */
public class TataGsmServiceProcessor extends ServiceProcessor
{
	private static final Logger logger = Logger.getLogger(TataGsmServiceProcessor.class);

	private Map<String, String> platformMap = new HashMap<String, String>();
	private Map<String, String> statusMap = new HashMap<String, String>();
	private Map<String, String> subClassMap = new HashMap<String, String>();
	private Map<String, String> modeMap = new HashMap<String, String>();
	private Map<String, String> serviceTypeMap = new HashMap<String, String>();
	private Map<String, String> contentTypeCodeMap = new HashMap<String, String>();
	private Map<String, String> languageMap = new HashMap<String, String>();

	private DateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	/**
	 * @throws Exception
	 */
	public TataGsmServiceProcessor() throws Exception
	{
		super();

		init();
	}

	/**
	 * 
	 */
	private void init()
	{
		platformMap = new HashMap<String, String>();
		String[] tokens = RBTParametersUtils.getParamAsString("COMMON", "SUB_STATUS_PLATFORM_MAP", "CRBT:5,RRBT:7").split(",");
		for (String eachToken : tokens)
		{
			if (eachToken.trim().length() == 0)
				continue;

			String[] s = eachToken.split(":");
			platformMap.put(s[0].trim(), s[1].trim());
		}

		statusMap = new HashMap<String, String>();
		tokens = RBTParametersUtils.getParamAsString("COMMON", "SUB_STATUS_STATUS_MAP", "").split(",");
		for (String eachToken : tokens)
		{
			if (eachToken.trim().length() == 0)
				continue;

			String[] s = eachToken.split(":");
			statusMap.put(s[0].trim(), s[1].trim());
		}

		subClassMap = new HashMap<String, String>();
		tokens = RBTParametersUtils.getParamAsString("COMMON", "SUB_STATUS_SUB_CLASS_MAP", "").split(",");
		for (String eachToken : tokens)
		{
			if (eachToken.trim().length() == 0)
				continue;

			String[] s = eachToken.split(":");
			subClassMap.put(s[0].trim(), s[1].trim());
		}

		modeMap = new HashMap<String, String>();
		tokens = RBTParametersUtils.getParamAsString("COMMON", "SUB_STATUS_MODE_MAP", "").split(",");
		for (String eachToken : tokens)
		{
			if (eachToken.trim().length() == 0)
				continue;

			String[] s = eachToken.split(":");
			modeMap.put(s[0].trim(), s[1].trim());
		}

		serviceTypeMap = new HashMap<String, String>();
		tokens = RBTParametersUtils.getParamAsString("COMMON", "SUB_STATUS_SERVICE_TYPE_MAP", "").split(",");
		for (String eachToken : tokens)
		{
			if (eachToken.trim().length() == 0)
				continue;

			String[] s = eachToken.split(":");
			serviceTypeMap.put(s[0].trim(), s[1].trim());
		}

		contentTypeCodeMap = new HashMap<String, String>();
		tokens = RBTParametersUtils.getParamAsString("COMMON", "SUB_STATUS_CONTENT_TYPE_MAP", "").split(",");
		for (String eachToken : tokens)
		{
			if (eachToken.trim().length() == 0)
				continue;

			String[] s = eachToken.split(":");
			contentTypeCodeMap.put(s[0].trim(), s[1].trim());
		}

		languageMap = new HashMap<String, String>();
		tokens = RBTParametersUtils.getParamAsString("COMMON", "SUB_STATUS_LANGUAGE_MAP", "").split(",");
		for (String eachToken : tokens)
		{
			if (eachToken.trim().length() == 0)
				continue;

			String[] s = eachToken.split(":");
			languageMap.put(s[0].trim(), s[1].trim());
		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceProcessor#processSubStatusRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processSubStatusRequest(Task task)
	{
		Document document = XMLUtils.newDocument();
		Element element = document.createElement("ROOT");
		document.appendChild(element);

		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
		rbtDetailsRequest.setMode("CCC");

		subscriber = rbtClient.getSubscriber(rbtDetailsRequest);

		Library library = rbtClient.getLibraryHistory(rbtDetailsRequest);
		Setting[] settings = null;
		if (library != null && library.getSettings() != null)
			settings = library.getSettings().getSettings();

		
		if(subscriber == null || subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER)) {
			logger.info("processSubStatusRequest : " + XMLUtils.getStringFromDocument(document));
			task.setObject(param_response, XMLUtils.getStringFromDocument(document));
			return;
		}
		
		
		if (settings == null || settings.length == 0)
		{
			Element selectionElement = getSelectionElement(document, task, null, subscriber);
			if (selectionElement != null)
				element.appendChild(selectionElement);
		}
		else {
			for (Setting setting : settings)
			{
				Element selectionElement = getSelectionElement(document, task, setting, subscriber);
				if (selectionElement != null)
					element.appendChild(selectionElement);
			}
		}

		task.setObject(param_response, XMLUtils.getStringFromDocument(document));
		return;
	}

	/**
	 * @param document
	 * @param task
	 * @param setting
	 * @param subscriber
	 * @return
	 */
	private Element getSelectionElement(Document document, Task task,
			Setting setting, Subscriber subscriber)
	{
		if (subscriber == null)
			return null;

		Element serviceElement = document.createElement("SERVICE");

		Element serviceNameElem = createTextElement(document, "SERVICE_NAME", subscriber.getSubscriptionClass());
		serviceElement.appendChild(serviceNameElem);

		String platformCode = "";
		if (RBTDeploymentFinder.isRRBTSystem())
			platformCode = platformMap.get("RRBT");
		else
			platformCode = platformMap.get("CRBT");

		Element platformElem = createTextElement(document, "PLATFORM", platformCode);
		serviceElement.appendChild(platformElem);

		Element statusElem = createTextElement(document, "STATUS", statusMap.get(subscriber.getStatus()));
		serviceElement.appendChild(statusElem);

		String planCode = subClassMap.get(subscriber.getSubscriptionClass());
		if (planCode == null)
			planCode = "1";
		Element planElem = createTextElement(document, "PLAN", planCode);
		serviceElement.appendChild(planElem);

		String actDateStr = "";
		if (subscriber.getActivationDate() != null)
			actDateStr = sdf.format(subscriber.getActivationDate());
		Element actDateElem = createTextElement(document, "ACTIVATION_DATE", actDateStr);
		serviceElement.appendChild(actDateElem);

		Element actModeElem = createTextElement(document, "ACTIVATION_MODE", modeMap.get(subscriber.getActivatedBy()));
		serviceElement.appendChild(actModeElem);

		SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subscriber.getSubscriptionClass());
		String subscriptionAmount = subClass.getSubscriptionAmount();
		int amtInPaisa = (int)Double.parseDouble(subscriptionAmount) * 100;
		Element actChargesElem = createTextElement(document, "ACTIVATION_CHARGES", String.valueOf(amtInPaisa));
		serviceElement.appendChild(actChargesElem);

		String nextBillingDateStr = "";
		if (subscriber.getNextBillingDate() != null)
			nextBillingDateStr = sdf.format(subscriber.getNextBillingDate());
		Element serviceValidityElem = createTextElement(document, "SERVICE_VALIDITY", nextBillingDateStr);
		serviceElement.appendChild(serviceValidityElem);

		String lastChargedDateStr = "";
		if (subscriber.getNextChargingDate() != null)
			lastChargedDateStr = sdf.format(subscriber.getNextChargingDate());
		Element lastChargedDateElem = createTextElement(document, "LAST_CHARGED_DATE", lastChargedDateStr);
		serviceElement.appendChild(lastChargedDateElem);

		int lastAmtInPaisa = (int)((subscriber.getLastChargeAmount() != null) ?
				Double.parseDouble(subscriber.getLastChargeAmount()) * 100 : -1);
		Element lastChargeAmountElem = createTextElement(document, "LAST_RENEWAL_CHARGED", (lastAmtInPaisa != -1) ? String.valueOf(lastAmtInPaisa) : "");
		serviceElement.appendChild(lastChargeAmountElem);

		String serviceType = serviceTypeMap.get("onetime");
		if (subClass.getSubscriptionRenewal().equalsIgnoreCase("y"))
			serviceType = serviceTypeMap.get("renewal");

		Element serviceTypeElem = createTextElement(document, "SERVICE_TYPE", serviceType);
		serviceElement.appendChild(serviceTypeElem);

		String deactDateStr = "";
		if (subscriber.getStatus().equals(WebServiceConstants.DEACTIVE) || subscriber.getStatus().equals(WebServiceConstants.DEACT_PENDING))
			deactDateStr = sdf.format(subscriber.getEndDate());
		Element deactDateElem = createTextElement(document, "DEACTIVATION_DATE", deactDateStr);
		serviceElement.appendChild(deactDateElem);

		Element deactModeElem = createTextElement(document, "REASON_FOR_DEACT", modeMap.get(subscriber.getDeactivatedBy()));
		serviceElement.appendChild(deactModeElem);

		String autoRenewal = "0";
		if (subClass.getSubscriptionRenewal().equalsIgnoreCase("y"))
			autoRenewal = "1";
		Element autoRenewalElem = createTextElement(document, "AUTO_RENEWAL", autoRenewal);
		serviceElement.appendChild(autoRenewalElem);
		
		String simType = "1";
		if (subscriber.isPrepaid())
			simType = "0";
		Element simTypeElem = createTextElement(document, "SIM_TYPE", simType);
		serviceElement.appendChild(simTypeElem);
		
		getContentElement(document, task, setting, serviceElement);		
		
		Element futureInfo1Elem = createTextElement(document, "FUTURE_INFO_1", "");
		serviceElement.appendChild(futureInfo1Elem);

		Element futureInfo2Elem = createTextElement(document, "FUTURE_INFO_2", "");
		serviceElement.appendChild(futureInfo2Elem);

		Element futureInfo3Elem = createTextElement(document, "FUTURE_INFO_3", "");
		serviceElement.appendChild(futureInfo3Elem);

		return serviceElement;
	}
	
	
	/**
	 * @param document
	 * @param task
	 * @param setting
	 * @param serviceElement
	 * @return
	 */
	private void getContentElement(Document document, Task task, Setting setting, Element serviceElement)
	{
		if(setting != null) {
			ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(setting.getChargeClass());
			String chargeAmount = chargeClass.getAmount();
			int chrgAmtInPaisa = (int)Double.parseDouble(chargeAmount) * 100;
			Element contentChargesElem = createTextElement(document, "CONTENT_CHARGES", String.valueOf(chrgAmtInPaisa));
			serviceElement.appendChild(contentChargesElem);
	
			String selNextBillingDateStr = "";
			if (setting.getNextBillingDate() != null)
				selNextBillingDateStr = sdf.format(setting.getNextBillingDate());
			Element contentValidityElem = createTextElement(document, "CONTENT_VALIDITY", selNextBillingDateStr);
			serviceElement.appendChild(contentValidityElem);
	
			int lastSelAmtInPaisa = (int)(setting.getLastChargeAmount() != null ?
					Double.parseDouble(setting.getLastChargeAmount()) * 100 : -1);
			Element contentLastChargeAmountElem = createTextElement(document, "LAST_CONTENT_RENEWAL_CHARGED", (lastSelAmtInPaisa != -1) ? String.valueOf(lastSelAmtInPaisa) : "");
			serviceElement.appendChild(contentLastChargeAmountElem);
	
			String contentTypeCode = contentTypeCodeMap.get("tone");
			Category category = RBTCacheManager.getInstance().getCategory(setting.getCategoryID());
			if (Utility.isShuffleCategory(category.getCategoryTpe()))
				contentTypeCode = contentTypeCodeMap.get("album");
	
			Element contentTypeElem = createTextElement(document, "CONTENT_TYPE_DOWNLOADED", contentTypeCode);
			serviceElement.appendChild(contentTypeElem);
	
			Clip clip = RBTCacheManager.getInstance().getClip(setting.getToneID());
			Element contentLanguageElem = createTextElement(document, "CONTENT_LANGUAGE", languageMap.get(clip.getLanguage()));
			serviceElement.appendChild(contentLanguageElem);
		}
		else {
			serviceElement.appendChild(createTextElement(document, "CONTENT_CHARGES", ""));
			serviceElement.appendChild(createTextElement(document, "CONTENT_VALIDITY", ""));
			serviceElement.appendChild(createTextElement(document, "LAST_CONTENT_RENEWAL_CHARGED", ""));
			serviceElement.appendChild(createTextElement(document, "CONTENT_TYPE_DOWNLOADED", ""));
			serviceElement.appendChild(createTextElement(document, "CONTENT_LANGUAGE", ""));
		}
	}


	/**
	 * @param document
	 * @param name
	 * @param value
	 * @return
	 */
	private Element createTextElement(Document document, String name, String value)
	{
		Element elem = document.createElement(name);

		Text text;
		if (value != null)
			text = document.createTextNode(value);
		else
			text = document.createTextNode("");

		elem.appendChild(text);
		return elem;
	}
}
