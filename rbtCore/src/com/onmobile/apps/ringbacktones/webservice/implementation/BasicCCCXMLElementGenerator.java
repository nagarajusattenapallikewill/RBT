/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;

/**
 * @author vinayasimha.patil
 *
 */
public class BasicCCCXMLElementGenerator implements WebServiceConstants
{
	private static Logger logger = Logger
			.getLogger(BasicCCCXMLElementGenerator.class);

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
		if (startDate != null) subscriberElem.setAttribute(START_DATE, dateFormat.format(startDate));
		if (endDate != null) subscriberElem.setAttribute(END_DATE, dateFormat.format(endDate));
		if (nextChargingDate != null) subscriberElem.setAttribute(NEXT_CHARGING_DATE, dateFormat.format(nextChargingDate));
		if (lastDeactivationDate != null) subscriberElem.setAttribute(LAST_DEACTIVATION_DATE, dateFormat.format(lastDeactivationDate));
		if (activationDate != null) subscriberElem.setAttribute(ACTIVATION_DATE, dateFormat.format(activationDate));
		if(webServicesubscriber.getProtocolNo() != null){
			subscriberElem.setAttribute(iRBTConstant.param_protocolNo, webServicesubscriber.getProtocolNo());
        }
        if(webServicesubscriber.getProtocolStaticText() != null){
        	subscriberElem.setAttribute(iRBTConstant.param_protocolStaticText, webServicesubscriber.getProtocolStaticText());
        }
        
        
		if (webServicesubscriber.getStatus().equalsIgnoreCase(ACTIVE) || webServicesubscriber.getStatus().equalsIgnoreCase(SUSPENDED))
		{
			HashMap<String, String> resultMap = Utility.getNextBillingDateOfServices(task);
			String nextBillingDate = resultMap.get(webServicesubscriber.getRefID());
			String chargeDetails = resultMap.get(webServicesubscriber.getRefID() + "_chargeDetails");
			String lastChargeAmount = resultMap.get(webServicesubscriber.getRefID() + "_lastAmountCharged");
			String lastTransactionType = resultMap.get(webServicesubscriber.getRefID() + "_lastTransactionType");
			
			if(nextBillingDate == null) {
				nextBillingDate = Utility.getNextBillingDateOfServices(task).get(webServicesubscriber.getSubscriberID());
			}
			
			if(chargeDetails == null) {
				chargeDetails = Utility.getNextBillingDateOfServices(task).get(webServicesubscriber.getSubscriberID() + "_chargeDetails");
			}
			
			if(lastChargeAmount == null) {
				lastChargeAmount = Utility.getNextBillingDateOfServices(task).get(webServicesubscriber.getSubscriberID() + "_lastAmountCharged");
			}
			
			if (lastTransactionType == null){
				lastTransactionType = Utility.getNextBillingDateOfServices(task).get(webServicesubscriber.getSubscriberID() + "_lastTransactionType");
			}
			if (nextBillingDate != null)
			{
				subscriberElem.setAttribute(NEXT_BILLING_DATE, nextBillingDate);

				try
				{
					long minRenewalGraceMinutes = RBTParametersUtils.getParamAsLong(iRBTConstant.COMMON, "MIN_RENEWAL_GRACE_MINUTES", 0);

					SimpleDateFormat rbtDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					Date nextBillingDateObj = rbtDateFormat.parse(nextBillingDate);
					if (nextBillingDateObj.getTime() < (System.currentTimeMillis() - (minRenewalGraceMinutes * 60 * 1000)))
						subscriberElem.setAttribute(STATUS, RENEWAL_GRACE);
				}
				catch (ParseException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
			
			if(chargeDetails != null)
			{
				subscriberElem.setAttribute(CHARGE_DETAILS, chargeDetails);
			}

			if (lastChargeAmount != null)
			{
				subscriberElem.setAttribute(LAST_CHARGE_AMOUNT, lastChargeAmount);
			}
			
			if (lastTransactionType != null){
				subscriberElem.setAttribute(LAST_TRANSACTION_TYPE, lastTransactionType);
			}
		}else{
			//for only if feature enabled
			boolean confirmationSmsEnabled = RBTParametersUtils.getParamAsBoolean("SMS","OPT_IN_RESUBSCRIPTION_SMS_ENABLED", "false");
			if (confirmationSmsEnabled) {
				String nextBillingDate = Utility.getNextBillingDateOfServices(task).get(webServicesubscriber.getRefID());
				if (nextBillingDate != null) {
					subscriberElem.setAttribute(NEXT_BILLING_DATE,nextBillingDate);
				}
			}
		}

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

		if (webServiceSubscriberSetting.getStartTime() != null)
			Utility.addPropertyElement(document, settingElem, START_TIME, DATA, dateFormat.format(webServiceSubscriberSetting.getStartTime()));
		
		if (webServiceSubscriberSetting.getEndTime() != null)
			Utility.addPropertyElement(document, settingElem, END_TIME, DATA, dateFormat.format(webServiceSubscriberSetting.getEndTime()));
		
		if (webServiceSubscriberSetting.getNextChargingDate() != null)
			Utility.addPropertyElement(document, settingElem, NEXT_CHARGING_DATE, DATA, dateFormat.format(webServiceSubscriberSetting.getNextChargingDate()));

		if(webServiceSubscriberSetting.getSelectionExtraInfo() != null && !RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DISABLE_SELECTION_EXTRA_INFO", "FALSE")){
			Utility.addPropertyElement(document, settingElem, SELECTION_EXTRA_INFO, DATA, webServiceSubscriberSetting.getSelectionExtraInfo());
		}
		String nextBillingDate = Utility.getNextBillingDateOfServices(task).get(webServiceSubscriberSetting.getRefID());
		if (nextBillingDate != null)
		{
			Element nextBillingDateElem = Utility.getPropertyElement(settingElem, NEXT_BILLING_DATE);
			if (nextBillingDateElem == null)
				Utility.addPropertyElement(document, settingElem, NEXT_BILLING_DATE, DATA, nextBillingDate);
		}

		String lastChargeAmount = Utility.getNextBillingDateOfServices(task).get(webServiceSubscriberSetting.getRefID() + "_lastAmountCharged");
		if (lastChargeAmount != null)
		{
			Element lastChargeAmountElem = Utility.getPropertyElement(settingElem, LAST_CHARGE_AMOUNT);
			if (lastChargeAmountElem == null)
				Utility.addPropertyElement(document, settingElem, LAST_CHARGE_AMOUNT, DATA, lastChargeAmount);
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
		Utility.addPropertyElement(document, downloadElem, SELECTION_INFO, DATA, webServiceSubscriberDownload.getSelectionInfo());
		
		String lastTransactionStatus = Utility.getNextBillingDateOfServices(task).get(webServiceSubscriberDownload.getRefID() + "_lastTransactionType");
		if(lastTransactionStatus != null){
			Utility.addPropertyElement(document, downloadElem, LAST_TRANSACTION_TYPE, DATA, lastTransactionStatus);
		}
		String nextBillingDate = Utility.getNextBillingDateOfServices(task).get(webServiceSubscriberDownload.getRefID());
		if (nextBillingDate != null)
		{
			Element nextBillingDateElem = Utility.getPropertyElement(downloadElem, NEXT_BILLING_DATE);
			if (nextBillingDateElem == null)
				Utility.addPropertyElement(document, downloadElem, NEXT_BILLING_DATE, DATA, nextBillingDate);

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			Calendar cal = Calendar.getInstance();
			try
			{
				int selPeriod =  0;
				boolean isAllowed = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
						"ENABLE_LAST_BILL_DATE_CALCULATION_ON_NUMBER_OF_DAYS", "FALSE");
				if (isAllowed) {
					String selectionPeriod = CacheManagerUtil.getChargeClassCacheManager()
							.getChargeClass(webServiceSubscriberDownload.getChargeClass())
							.getSelectionPeriod();
					cal.setTime(formatter.parse(nextBillingDate));
					if (selectionPeriod.startsWith("D")) {
						selPeriod = Integer.parseInt(selectionPeriod.substring(1));
						cal.add(Calendar.DAY_OF_YEAR, -(selPeriod));
					} else if (selectionPeriod.startsWith("M")) {
						String noOfMonths = selectionPeriod.substring(1);
						cal.add(Calendar.MONTH, -(Integer.parseInt(noOfMonths)));
					} else {
						selPeriod = Integer.parseInt(selectionPeriod);
						cal.add(Calendar.DAY_OF_YEAR, -(selPeriod));
					}
				} else {
					selPeriod = CacheManagerUtil.getChargeClassCacheManager()
							.getChargeClass(webServiceSubscriberDownload.getChargeClass())
							.getSelectionPeriodInDays();
					cal.setTime(formatter.parse(nextBillingDate));
					cal.add(Calendar.DAY_OF_YEAR, -(selPeriod));
				}

				String nextChargingDate = formatter.format(cal.getTime());
				Element nextChargingDateElem = Utility.getPropertyElement(downloadElem, NEXT_CHARGING_DATE);
				if (nextChargingDateElem == null && nextChargingDate != null)
					Utility.addPropertyElement(document, downloadElem, NEXT_CHARGING_DATE, DATA, nextChargingDate);
			}
			catch (ParseException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		String lastChargeAmount = Utility.getNextBillingDateOfServices(task).get(webServiceSubscriberDownload.getRefID() + "_lastAmountCharged");
		if (lastChargeAmount != null)
		{
			Element lastChargeAmountElem = Utility.getPropertyElement(downloadElem, LAST_CHARGE_AMOUNT);
			if (lastChargeAmountElem == null)
				Utility.addPropertyElement(document, downloadElem, LAST_CHARGE_AMOUNT, DATA, lastChargeAmount);
		}
		
		return downloadElem;
	}
}
