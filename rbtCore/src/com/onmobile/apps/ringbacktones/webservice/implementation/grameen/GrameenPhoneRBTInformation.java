package com.onmobile.apps.ringbacktones.webservice.implementation.grameen;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;

public class GrameenPhoneRBTInformation extends BasicRBTInformation{

	public GrameenPhoneRBTInformation() throws ParserConfigurationException {
		super();
	}
	
	@Override
	protected String canBeGifted(String subscriberID, String callerID, String contentID) {
		

		SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
		if (subscriberDetail == null || !subscriberDetail.isValidSubscriber())
			return INVALID;

		Subscriber caller = rbtDBManager.getSubscriber(callerID);
		if (rbtDBManager.isSubscriberActivationPending(caller))
			return GIFTEE_ACT_PENDING;
		if (rbtDBManager.isSubscriberDeactivationPending(caller))
			return GIFTEE_DEACT_PENDING;

		if (caller != null
				&& (caller.subYes().equals(iRBTConstant.STATE_ACTIVATION_GRACE)
						|| caller.subYes().equals(iRBTConstant.STATE_SUSPENDED_INIT)
						|| caller.subYes().equals(iRBTConstant.STATE_SUSPENDED)))
			return TECHNICAL_DIFFICULTIES;

		if (contentID == null && DataUtils.isUserActivatedByGift(caller))
			return GIFTEE_GIFT_IN_USE;

		ViralSMSTable[] viralSMSEntries = rbtDBManager.getViralSMSByCaller(callerID);
		if (viralSMSEntries != null)
		{
			for (ViralSMSTable viralSMSEntry : viralSMSEntries)
			{
				if (contentID == null)
				{
					if (DataUtils.isServiceGiftInPending(viralSMSEntry, caller))
						return GIFTEE_GIFT_ACT_PENDING;

					if (DataUtils.isServiceGiftInUse(viralSMSEntry, caller))
						return GIFTEE_GIFT_IN_USE;
				}
				else
				{
					if (DataUtils.isSongGiftInPending(contentID, viralSMSEntry, caller))
						return EXISTS_IN_GIFTEE_LIBRAY;

					if (rbtDBManager.isSubscriberDeactivated(caller) && DataUtils.isServiceGiftInPending(viralSMSEntry, caller))
						return GIFTEE_GIFT_ACT_PENDING;
				}
			}
		}

		boolean isClip = true;
		String clipRbtWavFile = null;
		int categoryID = -1;
		if (contentID != null)
		{
			if (contentID.startsWith("C"))
			{
				isClip = false;
				contentID = contentID.substring(1);
				categoryID = Integer.parseInt(contentID);
			}
			else
			{
				int clipID = Integer.parseInt(contentID);
				Clip clip = rbtCacheManager.getClip(clipID);
				clipRbtWavFile = clip.getClipRbtWavFile();
			}	
		}

		if (contentID != null && !rbtDBManager.isSubscriberDeactivated(caller))
		{
			SubscriberDownloads[] subscriberDownloads = rbtDBManager.getSubscriberAllActiveDownloads(callerID);
			if (subscriberDownloads != null)
			{
				for (SubscriberDownloads subscriberDownload : subscriberDownloads)
				{
					if ((isClip && subscriberDownload.promoId().equals(clipRbtWavFile)) || subscriberDownload.categoryID() == categoryID)
					{
						char downloadStatus = subscriberDownload.downloadStatus();
						if (downloadStatus == 'n' || downloadStatus == 'p'
							|| downloadStatus == 'y'
								|| downloadStatus == 'd'
									|| downloadStatus == 's'
										|| downloadStatus == 'e'
											|| downloadStatus == 'f')
						{
							return EXISTS_IN_GIFTEE_LIBRAY;
						}
					}
				}
			}
			Parameters giftLimitParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "MAX_DOWNLOADS_ALLOWED");
			int noOfDownloadsLimit = 0;
			if(giftLimitParam!=null && giftLimitParam.getValue()!=null){
				try{
			       noOfDownloadsLimit = Integer.parseInt(giftLimitParam.getValue());
				}catch(Exception e){
					return ERROR;
				}
			}
			if(giftLimitParam!=null && subscriberDownloads!=null && subscriberDownloads.length >= noOfDownloadsLimit){
				return LIMIT_EXCEEDED;
			}
		}

		if (rbtDBManager.isSubscriberDeactivated(caller))
			return GIFTEE_NEW_USER;

		return GIFTEE_ACTIVE;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getRBTInformationDocument(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getRBTInformationDocument(WebServiceContext task)
	{
		Document document = super.getRBTInformationDocument(task);

		Element subscriberElem = (Element)document.getElementsByTagName(SUBSCRIBER).item(0);
		boolean canAllow = subscriberElem.getAttribute(CAN_ALLOW).equalsIgnoreCase(YES) ? true : false;
		boolean isValidPrefix = subscriberElem.getAttribute(IS_VALID_PREFIX).equalsIgnoreCase(YES) ? true : false;
		String subscriberStatus = subscriberElem.getAttribute(STATUS);

		boolean addBookMarks = canAllow && isValidPrefix
		&& !(subscriberStatus.equalsIgnoreCase(DEACT_PENDING) || subscriberStatus.equalsIgnoreCase(SUSPENDED));
		if (addBookMarks)
		{
			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] bookmarks = rbtDBManager.getSubscriberBookMarks(subscriberID);
			WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = getWebServiceSubscriberBookMarkObjects(task, bookmarks);
			Element bookMarksElem = getSubscriberBookMarksElement(document, task, webServiceSubscriberBookMarks, bookmarks);

			Element subDetailsElem = (Element)document.getElementsByTagName(SUBSCRIBER_DETAILS).item(0);
			subDetailsElem.appendChild(bookMarksElem);
		}

		Element libraryElem = (Element) document.getElementsByTagName(LIBRARY).item(0);
		if (libraryElem != null)
		{
			Attr nextChargeClassAttr = libraryElem.getAttributeNode(NEXT_SELECTION_AMOUNT);
			if (nextChargeClassAttr == null)
			{
				subscriberElem = (Element)document.getElementsByTagName(SUBSCRIBER).item(0);
				subscriberStatus = subscriberElem.getAttribute(STATUS);

				Subscriber subscriber = (Subscriber) task.get(param_subscriber);
				String nextChargeClass = null;
				if (subscriberStatus.equalsIgnoreCase(NEW_USER) || subscriberStatus.equalsIgnoreCase(DEACTIVE))
				{
					CosDetails cos = DataUtils.getCos(task, subscriber);
					nextChargeClass = rbtDBManager.getChargeClassFromCos(cos, 0);
				}
				else
				{
					nextChargeClass = rbtDBManager.getNextChargeClass(subscriber);
				}

				if (nextChargeClass != null && !nextChargeClass.equalsIgnoreCase("DEFAULT"))
				{
					ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(nextChargeClass);
					libraryElem.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
				}
			}
		}

		return document;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberLibraryElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryElement(Document document, WebServiceContext task)
	{
		Element element = super.getSubscriberLibraryElement(document, task);

		String subscriberID = task.getString(param_subscriberID);
		SubscriberDownloads[] downloads = null;
		if (!task.containsKey(param_downloads))
			downloads = rbtDBManager.getActiveSubscriberDownloads(subscriberID);
		else
			downloads = (SubscriberDownloads[])task.get(param_downloads);

		//Added for Vf-Spain by Sreekar
		downloads = getActiveDownloadsToBedisplayed(downloads);
		
		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(task, downloads);
		Element downloadsElem = getSubscriberDownloadsElement(document, task, webServiceSubscriberDownloads, downloads);

		element.appendChild(downloadsElem);
		Element recentSelectionElem = getSubscriberRecentSelElement(document, task);

		element.appendChild(recentSelectionElem);

		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		else
			subscriber = rbtDBManager.getSubscriber(task.getString(param_subscriberID));

		String nextChargeClass = rbtDBManager.getNextChargeClass(subscriber);
		if (nextChargeClass != null && !nextChargeClass.equalsIgnoreCase("DEFAULT"))
		{
			ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(nextChargeClass);
			element.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
		}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberLibraryHistoryElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryHistoryElement(Document document,
			WebServiceContext task)
	{
		Element element = super.getSubscriberLibraryHistoryElement(document, task);

		SubscriberDownloads[] subscriberDownloads = DataUtils.getFilteredDownloadHistory(task);
		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(task, subscriberDownloads);
		Element downloadsElem = getSubscriberDownloadsElement(document, task, webServiceSubscriberDownloads, subscriberDownloads);

		element.appendChild(downloadsElem);
		return element;
	}


	protected WebServiceSubscriber getWebServiceSubscriberObject(WebServiceContext task, Subscriber subscriber)
	{
	
		WebServiceSubscriber webServiceSubscriber = super.getWebServiceSubscriberObject(task, subscriber);
		HashMap<String, String> extraInfo = rbtDBManager.getExtraInfoMap(subscriber);
		String status = webServiceSubscriber.getStatus();
		String voluntary = "OFF";
		
		//If subscriber is voluntary suspension
		if ((status.equalsIgnoreCase(ACTIVE) || status.equalsIgnoreCase(SUSPENDED)) && 
				(extraInfo != null && extraInfo.containsKey(iRBTConstant.VOLUNTARY) && 
				(extraInfo.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("TRUE") || extraInfo.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("SM_SUSPENDED"))))
			voluntary = "ON";
		
		webServiceSubscriber.setVoluntary(voluntary);
		
		return webServiceSubscriber;
		
	}
	
	protected Element getSubscriberElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServicesubscriber,
			Subscriber subscriber) {
		Element element = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);
		
		element.setAttribute("VOLUNTARY", webServicesubscriber.getVoluntary());
		return element;
	}
		
}


