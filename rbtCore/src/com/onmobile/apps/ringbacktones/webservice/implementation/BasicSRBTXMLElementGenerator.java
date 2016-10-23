package com.onmobile.apps.ringbacktones.webservice.implementation;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;

public class BasicSRBTXMLElementGenerator implements WebServiceConstants{

	public static Element generateSubscriberElement(Document document, WebServiceContext task, Element subscriberElem,
			WebServiceSubscriber webServicesubscriber)
	{
		String subscriptionYes = webServicesubscriber.getSubscriptionYes();
		if (subscriptionYes != null)
		{
			if (subscriptionYes.equalsIgnoreCase(iRBTConstant.STATE_ACTIVATION_ERROR))
				subscriberElem.setAttribute(STATUS, ACT_ERROR);
			else if (subscriptionYes.equalsIgnoreCase(iRBTConstant.STATE_DEACTIVATION_ERROR))
				subscriberElem.setAttribute(STATUS, DEACT_ERROR);
		}

		String activationInfo = webServicesubscriber.getActivationInfo();
		String deactivatedBy = webServicesubscriber.getDeactivatedBy();
		String lastDeactivationInfo = webServicesubscriber.getLastDeactivationInfo();
		Date startDate = webServicesubscriber.getStartDate();
		Date endDate = webServicesubscriber.getEndDate();
		Date nextChargingDate = webServicesubscriber.getNextChargingDate();
		Date lastDeactivationDate = webServicesubscriber.getLastDeactivationDate();
		Date activationDate = webServicesubscriber.getActivationDate();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		if (activationInfo != null) subscriberElem.setAttribute(ACTIVATION_INFO, activationInfo);
		if (deactivatedBy != null) subscriberElem.setAttribute(DEACTIVATED_BY, deactivatedBy);
		if (lastDeactivationInfo != null) subscriberElem.setAttribute(LAST_DEACTIVATION_INFO, lastDeactivationInfo);
		Attr startDateAttr = subscriberElem.getAttributeNode(START_DATE);
		if (startDateAttr == null)
		{
			if (startDate != null) subscriberElem.setAttribute(START_DATE, dateFormat.format(startDate));
		}
		if (endDate != null) subscriberElem.setAttribute(END_DATE, dateFormat.format(endDate));
		if (nextChargingDate != null) subscriberElem.setAttribute(NEXT_CHARGING_DATE, dateFormat.format(nextChargingDate));
		if (lastDeactivationDate != null) subscriberElem.setAttribute(LAST_DEACTIVATION_DATE, dateFormat.format(lastDeactivationDate));
		if (activationDate != null) subscriberElem.setAttribute(ACTIVATION_DATE, dateFormat.format(activationDate));

//		if (Utility.isUserActive(webServicesubscriber.getStatus()))
//		{
//			String nextBillingDate = Utility.getNextBillingDateOfServices(task).get(webServicesubscriber.getRefID());
//			if (nextBillingDate != null)
//				subscriberElem.setAttribute(NEXT_BILLING_DATE, nextBillingDate);
//		}

		return subscriberElem;
	}

	public static Element generateSubscriberSettingContentElement(Document document,
			WebServiceContext task, Element settingElem, WebServiceSubscriberSetting webServiceSubscriberSetting)
	{
		String selectionStatusID = webServiceSubscriberSetting.getSelectionStatusID();
		String newSelectionStatus = null;
		if (selectionStatusID.equalsIgnoreCase(iRBTConstant.STATE_ACTIVATION_ERROR))
			newSelectionStatus = ACT_ERROR;
		else if (selectionStatusID.equalsIgnoreCase(iRBTConstant.STATE_DEACTIVATION_ERROR))
			newSelectionStatus = DEACT_ERROR;

		if (newSelectionStatus != null)
		{
			Element selectionStatusElem = Utility.getPropertyElement(settingElem, SELECTION_STATUS);
			if (selectionStatusElem != null)
				selectionStatusElem.setAttribute(VALUE, newSelectionStatus);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		Utility.addPropertyElement(document, settingElem, SELECTED_BY, DATA, webServiceSubscriberSetting.getSelectedBy());
		Utility.addPropertyElement(document, settingElem, SELECTION_INFO, DATA, webServiceSubscriberSetting.getSelectionInfo());
		Utility.addPropertyElement(document, settingElem, DESELECTED_BY, DATA, webServiceSubscriberSetting.getDeselectedBy());

		if (webServiceSubscriberSetting.getEndTime() != null)
			Utility.addPropertyElement(document, settingElem, END_TIME, DATA, dateFormat.format(webServiceSubscriberSetting.getEndTime()));

		if (webServiceSubscriberSetting.getNextChargingDate() != null)
			Utility.addPropertyElement(document, settingElem, NEXT_CHARGING_DATE, DATA, dateFormat.format(webServiceSubscriberSetting.getNextChargingDate()));

		if(webServiceSubscriberSetting.getSelectionExtraInfo() != null && !RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DISABLE_SELECTION_EXTRA_INFO", "FALSE")){
			Utility.addPropertyElement(document, settingElem, SELECTION_EXTRA_INFO, DATA, webServiceSubscriberSetting.getSelectionExtraInfo());
		}

		return settingElem;
	}

	public static Element generateSubscriberDownloadContentElement(Document document,
			WebServiceContext task, Element downloadElem, WebServiceSubscriberDownload webServiceSubscriberDownload)
	{
		char downloadStatusID = webServiceSubscriberDownload.getDownloadStatusID();
		String newDownloadStatus = null;
		if (downloadStatusID == iRBTConstant.STATE_DOWNLOAD_ACT_ERROR)
			newDownloadStatus = ACT_ERROR;
		else if (downloadStatusID == iRBTConstant.STATE_DOWNLOAD_DEACT_ERROR)
			newDownloadStatus = DEACT_ERROR;

		if (newDownloadStatus != null)
		{
			Element downloadStatusElem = Utility.getPropertyElement(downloadElem, DOWNLOAD_STATUS);
			if (downloadStatusElem != null)
				downloadStatusElem.setAttribute(VALUE, newDownloadStatus);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		Utility.addPropertyElement(document, downloadElem, DESELECTED_BY, DATA, webServiceSubscriberDownload.getDeselectedBy());

		if (webServiceSubscriberDownload.getEndTime() != null)
			Utility.addPropertyElement(document, downloadElem, END_TIME, DATA, dateFormat.format(webServiceSubscriberDownload.getEndTime()));

		Utility.addPropertyElement(document, downloadElem, DOWNLOAD_INFO, DATA, webServiceSubscriberDownload.getDownloadInfo());

		return downloadElem;
	}
}
