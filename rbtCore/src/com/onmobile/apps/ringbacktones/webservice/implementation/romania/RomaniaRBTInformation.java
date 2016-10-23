/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.romania;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberStatusImpl;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceCopyData;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;

/**
 * @author vinayasimha.patil
 *
 */
public class RomaniaRBTInformation extends BasicRBTInformation
{
	private static Logger logger = Logger.getLogger(RomaniaRBTInformation.class);

	/**
	 * @throws ParserConfigurationException
	 */
	public RomaniaRBTInformation() throws ParserConfigurationException
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

		Element subscriberElem = (Element)document.getElementsByTagName(SUBSCRIBER).item(0);
		boolean canAllow = subscriberElem.getAttribute(CAN_ALLOW).equalsIgnoreCase(YES) ? true : false;
		boolean isValidPrefix = subscriberElem.getAttribute(IS_VALID_PREFIX).equalsIgnoreCase(YES) ? true : false;
		String subscriberStatus = subscriberElem.getAttribute(STATUS);

		Element subDetailsElem = (Element)document.getElementsByTagName(SUBSCRIBER_DETAILS).item(0);
		// Library details will be added for deactive users also
		if (subscriberStatus.equalsIgnoreCase(DEACTIVE))
		{
			// Removing the blank Library details added by BasicRBTInformation
			NodeList nodeList = document.getElementsByTagName(LIBRARY);
			if (nodeList.getLength() > 0)
				subDetailsElem.removeChild(nodeList.item(0));

			// Adding full Library details
			Element libraryElem = getSubscriberLibraryElement(document, task);
			subDetailsElem.appendChild(libraryElem);
		}

		boolean addDetails = canAllow && isValidPrefix && !(subscriberStatus.equalsIgnoreCase(DEACT_PENDING)
				|| subscriberStatus.equalsIgnoreCase(SUSPENDED));
		if (addDetails)
		{
			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] bookmarks = rbtDBManager.getSubscriberBookMarks(subscriberID);
			WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = getWebServiceSubscriberBookMarkObjects(task, bookmarks);
			Element bookMarksElem = getSubscriberBookMarksElement(document, task, webServiceSubscriberBookMarks, bookmarks);
			subDetailsElem.appendChild(bookMarksElem);

			Element groupDetailsElem = getSubscriberGroupDetailsElement(document, task);
			subDetailsElem.appendChild(groupDetailsElem);
		}

		Element libraryElem = (Element) document.getElementsByTagName(LIBRARY).item(0);
		if (libraryElem != null)
		{
			Attr nextChargeClassAttr = libraryElem.getAttributeNode(NEXT_SELECTION_AMOUNT);
			if (nextChargeClassAttr == null)
			{
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
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber)
	{
		Element element = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);

		boolean isnewsLetterOn = false;
		HashMap<String, String> userInfoMap = DBUtility.getAttributeMapFromXML(webServicesubscriber.getUserInfo());
		if (userInfoMap != null)
		{
			String isnewsLetterOnStr = userInfoMap.get(iRBTConstant.IS_NEWSLETTER_ON);
			if (isnewsLetterOnStr != null && isnewsLetterOnStr.equalsIgnoreCase(iRBTConstant.NEWSLETTER_ON))
				isnewsLetterOn = true;
		}
		element.setAttribute(IS_NEWS_LETTER_ON, isnewsLetterOn ? YES : NO);

		if (webServicesubscriber.getStatus().equalsIgnoreCase(DEACTIVE))
		{
			long daysAfterDeactivation = (int) (System.currentTimeMillis() - subscriber.endDate().getTime()) / (1000 * 60 * 60 * 24);
			element.setAttribute(DAYS_AFTER_DEACTIVATION, String.valueOf(daysAfterDeactivation));
		}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberLibraryElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryElement(Document document, WebServiceContext task)
	{
		Element element = document.createElement(LIBRARY);

		String subscriberID = task.getString(param_subscriberID);
		SubscriberStatus[] settings = null;
		if (!task.containsKey(param_settings))
			settings = rbtDBManager.getAllActiveSubscriberSettings(subscriberID);
		else
			settings = (SubscriberStatus[])task.get(param_settings);
		
		String mode = "VP";
		if (task.containsKey(param_mode))
			mode = task.getString(param_mode);
		
		
		boolean displayDeactiveSel = task.containsKey("returnDctRecord") && task.getString("returnDctRecord").equalsIgnoreCase("true");
		
		settings = DataUtils.getRecentActiveSettings(rbtDBManager, settings, mode, null);
		//RBT-12597	VF-Ro | Migration of CC to CCC with customizations for VF-Ro
		if(RBTParametersUtils.getParamAsBoolean("CCC", "DEACTIVE_SONGS_DISPLAY_FEATURE", "false") && displayDeactiveSel) {
			settings=RBTDBManager.getInstance().getSubscriberRecords(subscriberID);
		}
		WebServiceSubscriberSetting[] webServiceSubscriberSettings = getWebServiceSubscriberSettingObjects(task, settings);

		Element settingsElem = getSubscriberSettingsElement(document, task, webServiceSubscriberSettings, settings);
		element.appendChild(settingsElem);

		SubscriberDownloads[] downloads = null;
		if (!task.containsKey(param_downloads))
			downloads = rbtDBManager.getActiveSubscriberDownloads(subscriberID);
		else
			downloads = (SubscriberDownloads[])task.get(param_downloads);
		
		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(task, downloads);
		Element downloadsElem = getSubscriberDownloadsElement(document, task, webServiceSubscriberDownloads, downloads);
		element.appendChild(downloadsElem);

		if (webServiceSubscriberDownloads != null)
		{
			NodeList nodeList = downloadsElem.getElementsByTagName(CONTENT);
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				Element contentElem = (Element) nodeList.item(i);
				NodeList propertyNodeList = contentElem.getChildNodes();
				String rbtFile = "";
				for (int j = 0; j < propertyNodeList.getLength(); j++)
				{
					String propertyName = ((Element)propertyNodeList.item(j)).getAttribute(NAME);
					if (propertyName.contains(RBT_FILE))
					{
						rbtFile = ((Element)propertyNodeList.item(j)).getAttribute(VALUE);
						break;
					}
				}

				rbtFile = rbtFile.substring(0, rbtFile.lastIndexOf(".wav"));
				String setForAll = RomaniaUtility.isSetForAll(settings, rbtFile) ? YES : NO;

				Utility.addPropertyElement(document, contentElem, IS_SET_FOR_ALL, DATA, setForAll);
			}
		}

		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		else
			subscriber = rbtDBManager.getSubscriber(subscriberID);

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
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberBookMarkContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark)
	 */
	@Override
	protected Element getSubscriberBookMarkContentElement(Document document,
			WebServiceSubscriberBookMark webServiceSubscriberBookMark,WebServiceContext task)
	{
		Element element = super.getSubscriberBookMarkContentElement(document, webServiceSubscriberBookMark, task);

		Clip clip = rbtCacheManager.getClip(webServiceSubscriberBookMark.getToneID());
		Date endDate = clip.getClipEndTime();

		SimpleDateFormat clipEndDateFormat = new SimpleDateFormat("ddMMyyyy");

		Utility.addPropertyElement(document, element, END_DATE, DATA, clipEndDateFormat.format(endDate));

		Category category = rbtCacheManager.getCategory(webServiceSubscriberBookMark.getCategoryID());

		String amount = "0";

		String parentClassType = "DEFAULT";
		if (category != null)
			parentClassType = category.getClassType();

		String childClassType = clip.getClassType();

		ChargeClass chargeClass = DataUtils.getValidChargeClass(parentClassType, childClassType);
		if (chargeClass != null)
			amount = chargeClass.getAmount();

		Utility.addPropertyElement(document, element, AMOUNT, DATA, amount);

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getCopyContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.WebServiceCopyData)
	 */
	@Override
	protected Element getCopyContentElement(Document document,
			WebServiceCopyData webServiceCopyData, WebServiceContext task)
	{
		Element element = super.getCopyContentElement(document, webServiceCopyData, task);

		Clip clip = rbtCacheManager.getClip(webServiceCopyData.getToneID());
		if (clip != null)
		{
			SimpleDateFormat clipEndDateFormat = new SimpleDateFormat("ddMMyyyy");

			Date endDate = clip.getClipEndTime();

			Utility.addPropertyElement(document, element, END_DATE, DATA, clipEndDateFormat.format(endDate));

			Category category = rbtCacheManager.getCategory(webServiceCopyData.getCategoryID());

			String amount = "0";

			String parentClassType = "DEFAULT";
			if (category != null)
				parentClassType = category.getClassType();

			String childClassType = clip.getClassType();

			ChargeClass chargeClass = DataUtils.getValidChargeClass(parentClassType, childClassType);
			if (chargeClass != null)
				amount = chargeClass.getAmount();

			Utility.addPropertyElement(document, element, AMOUNT, DATA, amount);
		}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getWebServiceCopyDataObjects(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected WebServiceCopyData[] getWebServiceCopyDataObjects(WebServiceContext task)
	{
		String subscriberID = task.getString(param_subscriberID);
		String fromSubscriber = task.getString(param_fromSubscriber);
		String browsingLanguage = task.getString(param_browsingLanguage);
		
		Subscriber fromSubscriberObj = rbtDBManager.getSubscriber(fromSubscriber);

		String result = INVALID;
		int categoryID = 0;
		int toneID = 0;
		String toneName = "";
		String toneType = CLIP;
		int status = 1;
		String previewFile = null;

		ArrayList<WebServiceCopyData> copyList = new ArrayList<WebServiceCopyData>();

		if (rbtDBManager.isSubscriberDeactivated(fromSubscriberObj) || rbtDBManager.isSubscriberSuspended(fromSubscriberObj))
		{
			result = NOT_RBT_USER;
		}
		else
		{
			SubscriberStatus[] settings = rbtDBManager.getAllActiveSubscriberSettings(fromSubscriber);
			settings = DataUtils.getRecentActiveSettings(rbtDBManager, settings, "VP", null);

			if (settings == null)
			{
				result = DEFAULT_RBT;
			}
			else
			{
				ArrayList<SubscriberStatus> list = new ArrayList<SubscriberStatus>();
				for (SubscriberStatus setting : settings)
				{
					if (setting.callerID() == null)
						list.add(setting);
				}

				if (list.size() == 0)
					result = DEFAULT_RBT;
				else
				{
					for (SubscriberStatus subscriberStatus : list)
					{
						result = SUCCESS;

						Clip clip = rbtCacheManager.getClipByRbtWavFileName(subscriberStatus.subscriberFile(), browsingLanguage);

						Parameters blockedClipIDsParam = parametersCacheManager.getParameter(iRBTConstant.GATHERER, "COPY_BLOCKED_CLIP_IDS", null);
						if (blockedClipIDsParam != null)
						{
							List<String> blockedClipIDsList = Arrays.asList(blockedClipIDsParam.getValue().trim().split(","));
							if (blockedClipIDsList.contains(String.valueOf(toneID)))
							{
								result = NOT_ALLOWED;
								continue;
							}
						}

						status = subscriberStatus.status();
						Category category = rbtCacheManager.getCategory(subscriberStatus.categoryID(), browsingLanguage);

						categoryID = category.getCategoryId();
						toneID = clip.getClipId();
						toneName = clip.getClipName();

						Category copyCategory = rbtCacheManager.getCategory(26, browsingLanguage);
						if (copyCategory != null)
							categoryID = 26;

						previewFile = clip.getClipPreviewWavFile();

						WebServiceCopyData webServiceCopyData = createWebServiceCopyDataObject();
						webServiceCopyData.setResult(result);
						webServiceCopyData.setSubscriberID(subscriberID);
						webServiceCopyData.setFromSubscriber(fromSubscriber);
						webServiceCopyData.setCategoryID(categoryID);
						webServiceCopyData.setToneID(toneID);
						webServiceCopyData.setToneName(toneName);
						webServiceCopyData.setToneType(toneType);
						webServiceCopyData.setStatus(status);
						webServiceCopyData.setPreviewFile(previewFile);

						copyList.add(webServiceCopyData);
					}
				}
			}
		}

		if (copyList.size() == 0)
		{
			WebServiceCopyData webServiceCopyData = createWebServiceCopyDataObject();
			webServiceCopyData.setResult(result);
			webServiceCopyData.setSubscriberID(subscriberID);
			webServiceCopyData.setFromSubscriber(fromSubscriber);
			webServiceCopyData.setCategoryID(categoryID);
			webServiceCopyData.setToneID(toneID);
			webServiceCopyData.setToneName(toneName);
			webServiceCopyData.setToneType(toneType);
			webServiceCopyData.setStatus(status);
			webServiceCopyData.setPreviewFile(previewFile);

			copyList.add(webServiceCopyData);
		}

		logger.info("RBT:: webServiceCopyData: " + copyList);
		WebServiceCopyData[] webServiceCopyDatas = copyList.toArray(new WebServiceCopyData[0]);
		return webServiceCopyDatas;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getCallDetailsElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber)
	 */
	@Override
	protected Element getCallDetailsElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServiceSubscriber, Subscriber subscriber)
	{
		Element element = RomaniaXMLElementGenerator.generateCallDetailsElement(document, task, webServiceSubscriber, subscriber);
		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#canBeGifted(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected String canBeGifted(String subscriberID, String callerID, String contentID)
	{
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		if (!rbtDBManager.isSubscriberActivated(subscriber))
			return GIFTER_NOT_ACT;

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

					if (!rbtDBManager.isSubscriberDeactivated(caller) && DataUtils.isServiceGiftInUse(viralSMSEntry, caller))
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

		if (contentID !=null && !rbtDBManager.isSubscriberDeactivated(caller))
		{
			SubscriberDownloads[] subscriberDownloads = rbtDBManager.getSubscriberDownloads(callerID);
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
		}

		if (rbtDBManager.isSubscriberDeactivated(caller))
			return GIFTEE_NEW_USER;

		return GIFTEE_ACTIVE;
	}
}
