package com.onmobile.apps.ringbacktones.provisioning.implementation.service.vodafone;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author sridhar.sindiri
 *
 */
public class VodafoneServiceProcessor extends ServiceProcessor {

	private static HashMap<String, String> statusMap = null;
	/**
	 * @throws Exception
	 */
	public VodafoneServiceProcessor() throws Exception 
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceProcessor#processSubStatusRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	
	public void processComboSubStatusRequest(Task task)
	{
		Document document = XMLUtils.newDocument();
		Element element = document.createElement("Response");
		document.appendChild(element);

		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		if (!subscriber.isCanAllow())
		{
			Text text = document.createTextNode(Resp_BlackListedNo);
			element.appendChild(text);
			task.setObject(param_response, XMLUtils.getStringFromDocument(document));
			return;
		}
		
		Element serviceElement = document.createElement("Service");
		element.appendChild(serviceElement);

		getComboSubscriberStatusElement(document, serviceElement, task);
		if (!subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER))
		{
			String subscriberID = task.getString(param_subscriberID);

			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
			rbtDetailsRequest.setMode(task.getString(param_MODE));

			Library library = rbtClient.getLibraryHistory(rbtDetailsRequest);

			Element activeSelectionsElement = getActiveSelectionsElement(document, task, library);
			if (activeSelectionsElement != null)
				serviceElement.appendChild(activeSelectionsElement);

			Element deactiveSelectionsElement = getDeactiveSelectionsElement(document, task, library);
			if (deactiveSelectionsElement != null)
				serviceElement.appendChild(deactiveSelectionsElement);
		}

		logger.info("processSubStatusRequest" + XMLUtils.getStringFromDocument(document));
		task.setObject(param_response, XMLUtils.getStringFromDocument(document));
	}

	public void processSubStatusRequest(Task task)
	{
		Document document = XMLUtils.newDocument();
		Element element = document.createElement("Response");
		document.appendChild(element);

		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		if (!subscriber.isCanAllow())
		{
			Text text = document.createTextNode(Resp_BlackListedNo);
			element.appendChild(text);
			task.setObject(param_response, XMLUtils.getStringFromDocument(document));
			return;
		}
		
		Element serviceElement = document.createElement("Service");
		element.appendChild(serviceElement);

		Element subscriberStatusElement = getSubscriberStatusElement(document, task);
		serviceElement.appendChild(subscriberStatusElement);

		if (!subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER))
		{
			String subscriberID = task.getString(param_subscriberID);

			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
			rbtDetailsRequest.setMode(task.getString(param_MODE));

			Library library = rbtClient.getLibraryHistory(rbtDetailsRequest);

			Element activeSelectionsElement = getActiveSelectionsElement(document, task, library);
			if (activeSelectionsElement != null)
				serviceElement.appendChild(activeSelectionsElement);

			Element deactiveSelectionsElement = getDeactiveSelectionsElement(document, task, library);
			if (deactiveSelectionsElement != null)
				serviceElement.appendChild(deactiveSelectionsElement);
		}

		logger.info("processSubStatusRequest" + XMLUtils.getStringFromDocument(document));
		task.setObject(param_response, XMLUtils.getStringFromDocument(document));
	}

	private Element getSubscriberStatusElement(Document document, Task task)
	{
		Element subscriberStatusElement = document.createElement("SubscriberStatus");

		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber)task.getObject(param_subscriber);
		else
			subscriber = getSubscriber(task);

		Element subscriberIDElem = createTextElement(document, "SubscriberID", subscriber.getSubscriberID());
		subscriberStatusElement.appendChild(subscriberIDElem);

		String startDate = null;
		if (subscriber.getStartDate() != null)
		{
			DateFormat formatter = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
			startDate = formatter.format(subscriber.getStartDate());
		}
		Element subscribedOnElem = createTextElement(document, "SubscribedOn", startDate);
		subscriberStatusElement.appendChild(subscribedOnElem);

		Element activatedByElem = createTextElement(document, "ActivatedBy", subscriber.getActivatedBy());
		subscriberStatusElement.appendChild(activatedByElem);

		String status = getStatusTextString(subscriber.getStatus());
		Element statusElem = createTextElement(document, "Status", status);
		subscriberStatusElement.appendChild(statusElem);

		String actInfo = "-";
		String copyInfo = getCopyInfo(subscriber.getActivationInfo());
		if (copyInfo != null)
			actInfo = "COPY:" + copyInfo;
		Element infoElem = createTextElement(document, "Info", actInfo);
		subscriberStatusElement.appendChild(infoElem);

		return subscriberStatusElement;
	}

	private void getComboSubscriberStatusElement(Document document, Element element, Task task)
	{
		try
		{
			String url = param("COMMON", dbparam_VODACRM_INTEGRATION_SM_DETAILS_URL, null);
			if(url == null || url.trim().length() == 0)
				return ;
			
			Subscriber subscriber = null;
			if (task.containsKey(param_subscriber))
				subscriber = (Subscriber)task.getObject(param_subscriber);
			else
				subscriber = getSubscriber(task);
	
			String finalUrl = url.replaceAll("<MSISDN>", subscriber.getSubscriberID());
			String srvkey = "RBT_ACT_DEFAULT";
			if(subscriber.getSubscriptionClass() != null)
				srvkey = "RBT_ACT_"+subscriber.getSubscriptionClass();
			finalUrl = finalUrl.replaceAll("<srvkey>", srvkey);
			HttpParameters httpParameters = new HttpParameters(finalUrl);
			Logger.getLogger(AdminFacade.class).info("RBT:: httpParameters: " + httpParameters);
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			Logger.getLogger(AdminFacade.class).info("RBT:: httpResponse: " + httpResponse);
	
			String response = httpResponse.getResponse();
			Document smDocument = XMLUtils.getDocumentFromString(response);
			
			NodeList transactionNodeList = smDocument.getDocumentElement().getChildNodes();
			for (int i = 0; i < transactionNodeList.getLength(); i++)
			{
				if(!"Service".equalsIgnoreCase(transactionNodeList.item(i).getNodeName()))
					continue;
				Element subscriberStatusElement = document.createElement("SubscriberStatus");
				Element transactionElem = (Element) transactionNodeList.item(i);
				for(int j = 0; j < transactionElem.getChildNodes().getLength(); j++)
				{
					Element subscriberIDElem = createTextElement(document, transactionElem.getChildNodes().item(j).getNodeName(), transactionElem.getChildNodes().item(j).getTextContent());
					subscriberStatusElement.appendChild(subscriberIDElem);
				}
				element.appendChild(subscriberStatusElement);
			}	
		}
		catch (Exception e)
		{
			Logger.getLogger(AdminFacade.class).error("RBT:: " + e.getMessage(), e);
		}
	}

	/**
	 * @param document
	 * @param task
	 * @param library TODO
	 * @return
	 */
	private Element getActiveSelectionsElement(Document document, Task task, Library library)
	{
		Element activeSelectionsElement = document.createElement("ActiveSelections");

		Setting[] settings = null;
		if (library != null && library.getSettings() != null)
			settings = library.getSettings().getSettings();

		if (settings == null || settings.length == 0)
			return null;

		for (Setting setting : settings)
		{
			if (setting.getSelectionStatus().equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)
					|| setting.getSelectionStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE)
					|| setting.getSelectionStatus().equalsIgnoreCase(WebServiceConstants.DEACT_ERROR))
				continue;

			Element selectionElem = document.createElement("Selection");

			Element callerIDElem = createTextElement(document, "CallerID", setting.getCallerID().toUpperCase());
			selectionElem.appendChild(callerIDElem);

			Category category = rbtCacheManager.getCategory(setting.getCategoryID());
			String categoryName = null;
			if (category != null)
				categoryName = category.getCategoryName();
			Element categoryNameElem = createTextElement(document, "CategoryName", categoryName);
			selectionElem.appendChild(categoryNameElem);

			Element clipNameElem = createTextElement(document, "ClipName", setting.getToneName());
			selectionElem.appendChild(clipNameElem);

			String promoID = null;
			String toneType = setting.getToneType();
			if (!toneType.equalsIgnoreCase(WebServiceConstants.CATEGORY_SHUFFLE)
					&& !toneType.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)
					&& !toneType.equalsIgnoreCase(WebServiceConstants.CATEGORY_KARAOKE))
			{
				Clip clip = rbtCacheManager.getClip(setting.getToneID());
				if (clip != null)
					promoID = clip.getClipPromoId();
			}
			Element promoIDElem = createTextElement(document, "PromoID", promoID);
			selectionElem.appendChild(promoIDElem);

			Element selectionTypeElem = createTextElement(document, "SelectionType", setting.getChargeClass());
			selectionElem.appendChild(selectionTypeElem);

			String setDate = null;
			if (setting.getSetTime() != null)
			{
				DateFormat formatter = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
				setDate = formatter.format(setting.getSetTime());
			}
			Element setTimeElem = createTextElement(document, "SetTime", setDate);
			selectionElem.appendChild(setTimeElem);

			Element selectedByElem = createTextElement(document, "SelectedBy", setting.getSelectedBy());
			selectionElem.appendChild(selectedByElem);

			String lastChargedDate = null;
			if (setting.getNextChargingDate() != null && setting.getNextChargingDate().before(new Date()))
			{
				DateFormat formatter = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
				lastChargedDate = formatter.format(setting.getNextChargingDate());
			}
			Element lastChargedDateElem = createTextElement(document, "LastChargedDate", lastChargedDate);
			selectionElem.appendChild(lastChargedDateElem);

			String selectionStatus = getStatusTextString(setting.getSelectionStatus());
			Element selectionStatusElem = createTextElement(document, "SelectionStatus", selectionStatus);
			selectionElem.appendChild(selectionStatusElem);

			String selInfo = "-";
			String copyInfo = getCopyInfo(setting.getSelectionInfo());
			if (copyInfo != null)
				selInfo = "COPY:" + copyInfo;
			Element infoElem = createTextElement(document, "Info", selInfo);
			selectionElem.appendChild(infoElem);

			activeSelectionsElement.appendChild(selectionElem);
		}

		return activeSelectionsElement;
	}

	/**
	 * @param document
	 * @param task
	 * @param library TODO
	 * @return
	 */
	private Element getDeactiveSelectionsElement(Document document, Task task, Library library)
	{
		Element deactivatedSelectionsElement = document.createElement("DeActivatedSelections");

		Setting[] settings = null;
		if (library != null && library.getSettings() != null)
			settings = library.getSettings().getSettings();

		if (settings == null || settings.length < 1)
			return null;

		for (Setting setting : settings)
		{
			if (setting.getSelectionStatus().equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)
					|| setting.getSelectionStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE)
					|| setting.getSelectionStatus().equalsIgnoreCase(WebServiceConstants.DEACT_ERROR))
			{
				Element selectionElem = document.createElement("Selection");

				Element callerIDElem = createTextElement(document, "CallerID", setting.getCallerID().toUpperCase());
				selectionElem.appendChild(callerIDElem);

				Category category = rbtCacheManager.getCategory(setting.getCategoryID());
				String categoryName = null;
				if (category != null)
					categoryName = category.getCategoryName();
				Element categoryNameElem = createTextElement(document, "CategoryName", categoryName);
				selectionElem.appendChild(categoryNameElem);

				Element clipNameElem = createTextElement(document, "ClipName", setting.getToneName());
				selectionElem.appendChild(clipNameElem);

				String promoID = null;
				String toneType = setting.getToneType();
				if (!toneType.equalsIgnoreCase(WebServiceConstants.CATEGORY_SHUFFLE)
						&& !toneType.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)
						&& !toneType.equalsIgnoreCase(WebServiceConstants.CATEGORY_KARAOKE))
				{
					Clip clip = rbtCacheManager.getClip(setting.getToneID());
					if (clip != null)
						promoID = clip.getClipPromoId();
				}
				Element promoIDElem = createTextElement(document, "PromoID", promoID);
				selectionElem.appendChild(promoIDElem);

				Element selectionTypeElem = createTextElement(document, "SelectionType", setting.getChargeClass());
				selectionElem.appendChild(selectionTypeElem);

				String endDate = null;
				if (setting.getEndTime() != null)
				{
					DateFormat formatter = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
					endDate = formatter.format(setting.getEndTime());
				}
				Element endTimeElem = createTextElement(document, "EndTime", endDate);
				selectionElem.appendChild(endTimeElem);

				String selectionStatus = getStatusTextString(setting.getSelectionStatus());
				Element selectionStatusElem = createTextElement(document, "SelectionStatus", selectionStatus);
				selectionElem.appendChild(selectionStatusElem);

				String selInfo = "-";
				String copyInfo = getCopyInfo(setting.getSelectionInfo());
				if (copyInfo != null)
					selInfo = "COPY:" + copyInfo;
				Element infoElem = createTextElement(document, "Info", selInfo);
				selectionElem.appendChild(infoElem);

				deactivatedSelectionsElement.appendChild(selectionElem);
			}
		}

		return deactivatedSelectionsElement;
	}

	private Element createTextElement(Document document, String name, String value)
	{
		Element elem = document.createElement(name);

		Text text;
		if (value != null)
			text = document.createTextNode(value);
		else
			text = document.createTextNode("-");

		elem.appendChild(text);
		return elem;
	}

	private String getStatusTextString(String status)
	{
		if (statusMap == null)
		{
			statusMap = new HashMap<String, String>();
			
			statusMap.put(WebServiceConstants.NEW_USER, "New User");
			statusMap.put(WebServiceConstants.ACT_PENDING, "Activation Pending");
			statusMap.put(WebServiceConstants.GRACE, "Activation Grace");
			statusMap.put(WebServiceConstants.ACT_ERROR, "Activation Error");
			statusMap.put(WebServiceConstants.SUSPENDED, "Suspended");
			statusMap.put(WebServiceConstants.ACTIVE, "Active");
			statusMap.put(WebServiceConstants.DEACT_PENDING, "Deactivation Pending");
			statusMap.put(WebServiceConstants.DEACTIVE, "Deactive");
		}
		
		if (statusMap.containsKey(status))
			status = statusMap.get(status);

		return status;
	}

	private String getCopyInfo(String info)
	{
		if (info == null || info.length() <= 0)
			return null;

		String copyInfo = null;
		int firstIndex = info.indexOf("|CP:");
		int secondIndex = info.indexOf(":CP|");
		if (firstIndex > -1 && secondIndex > firstIndex)
			copyInfo = info.substring(firstIndex + 4, secondIndex);
		
		return copyInfo;
	} 
}
