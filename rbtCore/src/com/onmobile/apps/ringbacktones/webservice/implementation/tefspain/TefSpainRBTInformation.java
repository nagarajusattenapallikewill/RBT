package com.onmobile.apps.ringbacktones.webservice.implementation.tefspain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
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
import com.onmobile.apps.ringbacktones.webservice.implementation.romania.RomaniaUtility;

public class TefSpainRBTInformation extends BasicRBTInformation 
{
	private static Logger logger = Logger.getLogger(TefSpainRBTInformation.class);
	
	public TefSpainRBTInformation() throws ParserConfigurationException 
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

		return document;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber) 
	{
		Element subscriberElement = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);
		
		SubscriberDetail subscriberDetail = DataUtils.getSubscriberDetail(task);
		HashMap<String, String> subscriberDetailsMap = subscriberDetail.getSubscriberDetailsMap();
		if (subscriberDetailsMap != null)
		{
			if (subscriberDetailsMap.containsKey("SEGMENT-ORG") && subscriberDetailsMap.get("SEGMENT-ORG") != null)
				subscriberElement.setAttribute(SEGMENT, subscriberDetailsMap.get("SEGMENT-ORG"));
			
			if (subscriberDetailsMap.containsKey("AGE") && subscriberDetailsMap.get("AGE") != null)
				subscriberElement.setAttribute(AGE, subscriberDetailsMap.get("AGE"));
		}

		return subscriberElement;
	}

	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberLibraryElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryElement(Document document, WebServiceContext task)
	{
		Element element = document.createElement(LIBRARY);

		String subscriberID = task.getString(param_subscriberID);
		
		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber)task.get(param_subscriber);
		else
			subscriber = rbtDBManager.getSubscriber(subscriberID);

		if (subscriber != null && subscriber.cosID() != null)
		{
			CosDetails cosDetails = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.cosID());
			
			int freeSongsLeft = (cosDetails.getFreeSongs() - subscriber.maxSelections());
			if (freeSongsLeft < 0)
				freeSongsLeft = 0;
			element.setAttribute(NO_OF_FREE_SONGS_LEFT, String.valueOf(freeSongsLeft));
		}
		
		SubscriberStatus[] settings = null;
		if (!task.containsKey(param_settings))
			settings = rbtDBManager.getAllActiveSubscriberSettings(subscriberID);
		else
			settings = (SubscriberStatus[])task.get(param_settings);
		
		String mode = "VP";
		if (task.containsKey(param_mode))
			mode = task.getString(param_mode);

		settings = DataUtils.getRecentActiveSettings(rbtDBManager, settings, mode, null);
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
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getWebServiceCopyDataObjects(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected WebServiceCopyData[] getWebServiceCopyDataObjects(WebServiceContext task)
	{
		String subscriberID = task.getString(param_subscriberID);
		String fromSubscriber = task.getString(param_fromSubscriber);
		Subscriber fromSubscriberObj = rbtDBManager.getSubscriber(fromSubscriber);
		String browsingLanguage = task.getString(param_browsingLanguage);

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
				ArrayList<SubscriberStatus> specialCallerList = new ArrayList<SubscriberStatus>();
				for (SubscriberStatus setting : settings)
				{
					if (setting.selType() == 2)
						continue; // Ignoring corporate selections

					if (setting.callerID() == null)
						list.add(setting);
					else if (setting.callerID().equalsIgnoreCase(subscriberID))
						specialCallerList.add(setting);
				}

				if (specialCallerList.size() > 0)
				{
					list.clear();
					list.addAll(specialCallerList);
				}

				if (list.size() == 0)
					result = DEFAULT_RBT;
				else
				{
					for (SubscriberStatus subscriberStatus : list)
					{
						Category category = rbtCacheManager.getCategory(subscriberStatus.categoryID(), browsingLanguage);
						status = subscriberStatus.status();

						if (category != null)
						{
							if (category.getCategoryTpe() == iRBTConstant.RECORD || category.getCategoryTpe() == iRBTConstant.KARAOKE)
							{
								result = PERSONAL_MESSAGE;
								continue;
							}

							Clip clip = rbtCacheManager.getClipByRbtWavFileName(subscriberStatus.subscriberFile(), browsingLanguage);
							if (clip != null)
							{
								result = SUCCESS;
								categoryID = category.getCategoryId();
								toneID = clip.getClipId();
								toneName = clip.getClipName(); 

								if (category.getCategoryTpe() == iRBTConstant.SHUFFLE)
								{
									result = ALBUM_RBT;
									toneName = category.getCategoryName();
									toneType = CATEGORY_SHUFFLE;
								}
								else
								{
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

									Category copyCategory = rbtCacheManager.getCategory(26, browsingLanguage);
									if (copyCategory != null)
										categoryID = 26;
								}

								if (category.getCategoryTpe() == iRBTConstant.SHUFFLE)
									previewFile = category.getCategoryPreviewWavFile();
								else
									previewFile = clip.getClipPreviewWavFile();
							}

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
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getValidateNumberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	protected Element getValidateNumberElement(Document document, WebServiceContext task)
	{
		String action = task.getString(param_action);
		String subscriberID = task.getString(param_subscriberID);
		String number = task.getString(param_number);
		
		String response = VALID;
		String status = null;
		Set<String> validNumbers = new HashSet<String>();
		if (!com.onmobile.apps.ringbacktones.services.common.Utility.isValidNumber(subscriberID))
		{
			response = INVALID_PARAMETER;
		}
		else if (action.equalsIgnoreCase(action_personalize))
		{
			subscriberID = rbtDBManager.subID(subscriberID);
			number = rbtDBManager.subID(number);
			if (number.length() < 7)
				response = INVALID;
			else if (number.equalsIgnoreCase(subscriberID))
				response = OWN_NUMBER;
		}
		else if (action.equalsIgnoreCase(action_gift) || action.equalsIgnoreCase(action_sendGift))
		{
			
			boolean isNewCanBeGifted = parametersCacheManager.getParameter(iRBTConstant.COMMON, "NEW_USER_CAN_BE_GIFTED","FALSE").getValue().equalsIgnoreCase("TRUE");
			
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (!isNewCanBeGifted && (subscriber == null || rbtDBManager.isSubDeactive(subscriber)))
				response = INVALID;
			else {
				if (number.indexOf(",") != -1) { //The case when there are multiple giftees
					logger.debug("Comma-separated numbers. SubscriberId: " +subscriberID + ", numbers: " + number);
					String numbers[] = number.split(",");
					for (String localNumber : numbers) {
						localNumber = rbtDBManager.subID(localNumber);
						if (localNumber.equalsIgnoreCase(subscriberID)) {
							logger.warn("Invalid number. Its same as the subscriberId. SubscriberId: " +subscriberID + ", Number: " + localNumber);
						} else {
							String toneID = task.getString(param_toneID);
							if (task.containsKey(param_categoryID)) {
								String browsingLanguage = task
										.getString(param_browsingLanguage);
								int categoryID = Integer.parseInt(task
										.getString(param_categoryID));
								Category category = rbtCacheManager.getCategory(categoryID,
										browsingLanguage);
								if (Utility.isShuffleCategory(category.getCategoryTpe())) {
									toneID = "C" + categoryID;
								} else {
									logger.info("Category Type is not Shuffle. Fine if toneID param is present.");
								}
							}

							status = canBeGifted(subscriberID, localNumber, toneID);
							if (status.equals(VALID) || status.equals(GIFTEE_ACTIVE)
									|| status.equalsIgnoreCase(GIFTEE_NEW_USER)) {
								logger.info("Valid number. SubscriberId: " +subscriberID + ", Number: " + localNumber + ", Status: " + status);
								validNumbers.add(localNumber);
							} else if (status.equalsIgnoreCase(INVALID)) {
								logger.warn("Invalid number. SubscriberId: " +subscriberID + ", Number: " + localNumber + ", Status: " + status);
							} else {
								logger.warn("Invalid number. SubscriberId: " +subscriberID + ", Number: " + localNumber + ", Status: " + status);
							}
						}
					}
					if (!validNumbers.isEmpty()) {
						response = VALID;
						status = VALID;
					} else {
						response = INVALID;
						status = INVALID;
					}
				} else {
					number = rbtDBManager.subID(number);
					if (number.equalsIgnoreCase(subscriberID))
						response = OWN_NUMBER;
					else {
						String toneID = task.getString(param_toneID);
						if (task.containsKey(param_categoryID)) {
							String browsingLanguage = task
									.getString(param_browsingLanguage);
							int categoryID = Integer.parseInt(task
									.getString(param_categoryID));
							Category category = rbtCacheManager.getCategory(categoryID,
									browsingLanguage);
							if (Utility.isShuffleCategory(category.getCategoryTpe()))
								toneID = "C" + categoryID;
							else {
								logger.info("Category Type is not Shuffle. Fine if toneID param is present.");
							}
						}

						status = canBeGifted(subscriberID, number, toneID);
						if (status.equals(VALID) || status.equals(GIFTEE_ACTIVE)
								|| status.equalsIgnoreCase(GIFTEE_NEW_USER))
							response = VALID;
						else if (status.equalsIgnoreCase(INVALID))
							response = INVALID;
						else
							response = NOT_ALLOWED;
					}
				}
			}
		}

		Element element = Utility.getResponseElement(document, response);
		if (status != null && !status.equalsIgnoreCase(VALID) && !status.equalsIgnoreCase(INVALID))
			element.setAttribute(STATUS, status);
		
		if (!validNumbers.isEmpty()) {
			String validNumberString = StringUtils.join(validNumbers, ",");
			element.setAttribute(VALID_NUMBERS, validNumberString);
			logger.debug("SubscriberId: " +subscriberID + ", Number: " + number + ", validNumbers: " + validNumberString + ", Status: " + status + ", Response: " + response);
		} else {
			logger.debug("SubscriberId: " +subscriberID + ", Number: " + number + ", Status: " + status + ", Response: " + response);
		}
		task.put(RESPONSE, response);
		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#canBeGifted(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected String canBeGifted(String subscriberID, String callerID, String contentID) {
		SubscriberDetail callerIdDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
		if (callerIdDetail == null || !callerIdDetail.isValidSubscriber()) {
			return INVALID;
		}

		SubscriberDetail subscriberIdDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(subscriberID, "GIFT"));
		boolean isNewCanBeGifted = parametersCacheManager.getParameter(iRBTConstant.COMMON, "NEW_USER_CAN_BE_GIFTED","FALSE").getValue().equalsIgnoreCase("TRUE");
		if (!isNewCanBeGifted && (subscriberIdDetail == null || !subscriberIdDetail.isValidSubscriber())) {
			return INVALID;
		}
		
		Subscriber caller = rbtDBManager.getSubscriber(callerID);
		boolean isClip = true;
		String clipRbtWavFile = null;
		int categoryID = -1;
		if (contentID == null) {
			logger.error("contentID null.");
			return INVALID;
		}

		if (contentID.startsWith("C")) {
			isClip = false;
			contentID = contentID.substring(1);
			categoryID = Integer.parseInt(contentID);
			Category category = rbtCacheManager.getCategory(categoryID);
			if (category == null) {
				logger.error("Category is null");
				return CATEGORY_NOT_EXISTS;
			}
			if (category.getCategoryEndTime().before(new Date())) {
				logger.error("Category expired. Category end time: " + category.getCategoryEndTime());
				return CATEGORY_EXPIRED;
			}
			if (category.getCategoryStartTime().after(new Date())) {
				logger.error("Category invalid. Category start time: " + category.getCategoryStartTime());
				return INVALID;
			}
		} else {
			int clipID = Integer.parseInt(contentID);
			Clip clip = rbtCacheManager.getClip(clipID);
			if (clip == null) {
				logger.error("Clip is null");
				return CLIP_NOT_EXISTS;
			}
			if (clip.getClipEndTime().before(new Date())) {
				logger.error("Clip expired. Clip end time: " + clip.getClipEndTime());
				return CLIP_EXPIRED;
			}
			if (clip.getClipStartTime().after(new Date())) {
				logger.error("Clip invalid. Clip start time: " + clip.getClipEndTime());
				return INVALID;
			}
			clipRbtWavFile = clip.getClipRbtWavFile();
		}

		if (!rbtDBManager.isSubscriberDeactivated(caller)) {
			SubscriberDownloads[] subscriberDownloads = rbtDBManager.getSubscriberDownloads(callerID);
			if (subscriberDownloads != null) {
				for (SubscriberDownloads subscriberDownload : subscriberDownloads) {
					if ((isClip && subscriberDownload.promoId().equals(clipRbtWavFile)) || subscriberDownload.categoryID() == categoryID) {
						char downloadStatus = subscriberDownload.downloadStatus();
						if (downloadStatus == 'n' || downloadStatus == 'p'
								|| downloadStatus == 'y'
								|| downloadStatus == 'd'
								|| downloadStatus == 's'
								|| downloadStatus == 'e'
								|| downloadStatus == 'f') {
							return EXISTS_IN_GIFTEE_LIBRAY;
						}
					}
				}
			}
		}
		return VALID;
	}	
}