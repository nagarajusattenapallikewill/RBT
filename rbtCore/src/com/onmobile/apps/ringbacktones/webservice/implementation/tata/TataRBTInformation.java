/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.tata;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.services.common.MNPConstants;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicXMLElementGenerator;

/**
 * @author vinayasimha.patil
 *
 */
public class TataRBTInformation extends BasicRBTInformation
{
	private static Logger logger = Logger.getLogger(TataRBTInformation.class);

	/**
	 * @throws ParserConfigurationException
	 */
	public TataRBTInformation() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getRBTInformationDocument(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getRBTInformationDocument(WebServiceContext task)
	{
		Document document = super.getRBTInformationDocument(task);

		Element libraryElem = (Element) document.getElementsByTagName(LIBRARY).item(0);
		if (libraryElem != null)
		{
			Attr totalDownloadsAttr = libraryElem.getAttributeNode(TOTAL_DOWNLOADS);
			if (totalDownloadsAttr == null)
				libraryElem.setAttribute(TOTAL_DOWNLOADS, "0");
		}

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

		if (subscriber != null)
		{
			element.setAttribute(SUBSCRIPTION_STATE, subscriber.subYes());
			element.setAttribute(TOTAL_DOWNLOADS, String.valueOf(subscriber.maxSelections()));
		}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getWebServiceSubscriberObject(com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected WebServiceSubscriber getWebServiceSubscriberObject(WebServiceContext task, Subscriber subscriber)
	{
		WebServiceSubscriber webServiceSubscriber = super.getWebServiceSubscriberObject(task, subscriber);
		try
		{
			if (task.getString(param_api).equalsIgnoreCase(api_Rbt))
			{
				String status = Utility.getSubscriberStatus(subscriber);

				// Hitting WDS if user is not NEW_USER and DEACTIVE,
				// because for these two states WDS hit will happen in basic implementation.
				// And for Tata CDMA WDS hit has to be done for all calls irrespective of the status of user.
				if (!status.equals(NEW_USER) && !status.equals(DEACTIVE))
				{
					SubscriberDetail subscriberDetail = DataUtils.getSubscriberDetail(task);
					if (subscriberDetail != null)
					{
						webServiceSubscriber.setValidPrefix(subscriberDetail.isValidSubscriber());
						webServiceSubscriber.setCircleID(subscriberDetail.getCircleID());
						webServiceSubscriber.setPrepaid(subscriberDetail.isPrepaid());

						HashMap<String, String> subscriberDetailsMap = subscriberDetail.getSubscriberDetailsMap();
						if (subscriberDetailsMap != null)
						{
							if (subscriberDetailsMap.containsKey(MNPConstants.STATUS))
								webServiceSubscriber.setStatus(subscriberDetailsMap.get(MNPConstants.STATUS));
							if (subscriberDetailsMap.containsKey(MNPConstants.OPERATOR_USER_INFO))
								webServiceSubscriber.setOperatorUserInfo(subscriberDetailsMap.get(MNPConstants.OPERATOR_USER_INFO));
						}
					}
				}

				if (webServiceSubscriber.isValidPrefix())
				{
					boolean subscriberStatusUpdated = TataUtility.syncSubscriberStatus(task, subscriber);
					if (subscriberStatusUpdated)
					{
						subscriber = rbtDBManager.getSubscriber(task.getString(param_subscriberID));
						task.put(param_subscriber, subscriber);

						webServiceSubscriber.setStatus(Utility.getSubscriberStatus(subscriber));

						if (subscriber != null)
						{
							webServiceSubscriber.setPrepaid(subscriber.prepaidYes());
							webServiceSubscriber.setAccessCount(subscriber.noOfAccess());
							webServiceSubscriber.setSubscriptionYes(subscriber.subYes());
							webServiceSubscriber.setLanguage(subscriber.language());
							webServiceSubscriber.setSubscriptionClass(subscriber.subscriptionClass());
							webServiceSubscriber.setActivatedBy(subscriber.activatedBy());
							webServiceSubscriber.setActivationInfo(subscriber.activationInfo());
							webServiceSubscriber.setDeactivatedBy(subscriber.deactivatedBy());
							webServiceSubscriber.setLastDeactivationInfo(subscriber.lastDeactivationInfo());
							webServiceSubscriber.setStartDate(subscriber.startDate());
							webServiceSubscriber.setEndDate(subscriber.endDate());
							webServiceSubscriber.setNextChargingDate(subscriber.nextChargingDate());
							webServiceSubscriber.setLastDeactivationDate(subscriber.lastDeactivationDate());
							webServiceSubscriber.setActivationDate(subscriber.activationDate());
							webServiceSubscriber.setCosID(subscriber.cosID());
							webServiceSubscriber.setUserInfo(subscriber.extraInfo());

							// User type is added to task, so that no need query the
							// subscriber object for getting user type in preparing other parts of the XML
							task.put(param_isPrepaid, subscriber.prepaidYes() ? YES : NO);
						}
						else
						{
							// Clearing attributes if subscriber deactivated.
							webServiceSubscriber.setAccessCount(0);
							webServiceSubscriber.setSubscriptionYes(null);
							webServiceSubscriber.setLanguage(null);
							webServiceSubscriber.setSubscriptionClass(null);
							webServiceSubscriber.setActivatedBy(null);
							webServiceSubscriber.setActivationInfo(null);
							webServiceSubscriber.setDeactivatedBy(null);
							webServiceSubscriber.setLastDeactivationInfo(null);
							webServiceSubscriber.setStartDate(null);
							webServiceSubscriber.setEndDate(null);
							webServiceSubscriber.setNextChargingDate(null);
							webServiceSubscriber.setLastDeactivationDate(null);
							webServiceSubscriber.setActivationDate(null);
							webServiceSubscriber.setCosID(null);
							webServiceSubscriber.setUserInfo(null);
						}
					}

					if (task.containsKey(param_subscriberStatus))
						webServiceSubscriber.setStatus(task.getString(param_subscriberStatus));
				}
			}

			if (webServiceSubscriber.getCosID() == null)
			{
				CosDetails cos = TataUtility.getSubscriberCOS(task, subscriber);
				webServiceSubscriber.setCosID(cos.getCosId());
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		logger.info("RBT:: webServiceSubscriber: " + webServiceSubscriber);
		return webServiceSubscriber;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberLibraryElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryElement(Document document, WebServiceContext task)
	{
		try
		{
			String subscriberID = task.getString(param_subscriberID);
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);

			SubscriberDownloads[] downloads = rbtDBManager.getActiveSubscriberDownloads(subscriberID);
			SubscriberStatus[] settings = rbtDBManager.getAllSubscriberSelectionRecords(subscriberID, null);

			boolean syncLibrary = task.containsKey(param_action) && task.getString(param_action).equalsIgnoreCase(action_get);
			if (!syncLibrary && task.getString(param_api).equalsIgnoreCase(api_Rbt) && task.containsKey(param_mmContext))
			{
				String[] mmContext = task.getString(param_mmContext).split("\\|");
				for (String context : mmContext)
				{
					if (context.equalsIgnoreCase("SYNC_LIBRARY"))
					{
						syncLibrary = true;
						break;
					}
				}
			}

			if (syncLibrary)
			{
				TataUtility.syncSbscriberLibrary(task, subscriber, downloads, settings);
				downloads = rbtDBManager.getActiveSubscriberDownloads(subscriberID);
				settings = rbtDBManager.getAllSubscriberSelectionRecords(subscriberID, null);
			}

			Element element = document.createElement(LIBRARY);

			WebServiceSubscriberSetting[] webServiceSubscriberSettings = getWebServiceSubscriberSettingObjects(task, settings);
			Element settingsElem = getSubscriberSettingsElement(document, task, webServiceSubscriberSettings, settings);
			element.appendChild(settingsElem);

			WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(task, downloads);
			Element downloadsElem = getSubscriberDownloadsElement(document, task, webServiceSubscriberDownloads, downloads);
			element.appendChild(downloadsElem);

			String provideDefaultLoopOption = NO;
			if (TataUtility.getNonDefaultSongs(subscriberID, settings, downloads) != null)
				provideDefaultLoopOption = YES;
			element.setAttribute(PROVIDE_DEFAULT_LOOP_OPTION, provideDefaultLoopOption);

			SubscriberStatus[] pendingSettings = rbtDBManager.getAllPendingSettings(subscriberID);
			String isMusicboxDefaultSettingPending = NO;
			String isClipDefaultSettingPending = NO;
			if (rbtDBManager.checkMBSettingForDefaultPending(pendingSettings))
				isMusicboxDefaultSettingPending = YES;
			else if (rbtDBManager.checkClipSettingForDefaultPending(pendingSettings))
				isClipDefaultSettingPending = YES;

			element.setAttribute(IS_MUSICBOX_DEFAULT_SETTING_PENDING, isMusicboxDefaultSettingPending);
			element.setAttribute(IS_CLIP_DEFAULT_SETTING_PENDING, isClipDefaultSettingPending);

			int totalDownloads = 0;
			if (subscriber != null)
				totalDownloads = subscriber.maxSelections();
			element.setAttribute(TOTAL_DOWNLOADS, String.valueOf(totalDownloads));

			return element;
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberLibraryHistoryElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryHistoryElement(Document document, WebServiceContext task)
	{
		Element element = super.getSubscriberLibraryHistoryElement(document, task);

		SubscriberDownloads[] subscriberDownloads = DataUtils.getFilteredDownloadHistory(task);
		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(task, subscriberDownloads);
		Element downloadsElem = getSubscriberDownloadsElement(document, task, webServiceSubscriberDownloads, subscriberDownloads);

		element.appendChild(downloadsElem);
		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getCallDetailsElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber)
	 */
	@Override
	protected Element getCallDetailsElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber)
	{
		Element element = super.getCallDetailsElement(document, task,
				webServicesubscriber, subscriber);

		CosDetails cos = TataUtility.getSubscriberCOS(task, subscriber);
		Element cosElement = BasicXMLElementGenerator.generateCosElement(document, cos);
		element.appendChild(cosElement);

		return element;
	}
}
