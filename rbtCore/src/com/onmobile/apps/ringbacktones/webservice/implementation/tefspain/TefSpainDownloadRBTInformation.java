package com.onmobile.apps.ringbacktones.webservice.implementation.tefspain;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
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
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceCopyData;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;
import com.onmobile.apps.ringbacktones.webservice.implementation.romania.RomaniaUtility;

public class TefSpainDownloadRBTInformation extends BasicRBTInformation {
	private static Logger logger = Logger
			.getLogger(TefSpainDownloadRBTInformation.class);

	public TefSpainDownloadRBTInformation() throws ParserConfigurationException {
		super();
	}

	@Override
	public Document getRBTInformationDocument(WebServiceContext task) {
		Document document = super.getRBTInformationDocument(task);

		Element subscriberElem = (Element) document.getElementsByTagName(
				SUBSCRIBER).item(0);
		boolean canAllow = subscriberElem.getAttribute(CAN_ALLOW)
				.equalsIgnoreCase(YES) ? true : false;
		boolean isValidPrefix = subscriberElem.getAttribute(IS_VALID_PREFIX)
				.equalsIgnoreCase(YES) ? true : false;
		String subscriberStatus = subscriberElem.getAttribute(STATUS);

		Element subDetailsElem = (Element) document.getElementsByTagName(
				SUBSCRIBER_DETAILS).item(0);

		boolean addDetails = canAllow
				&& isValidPrefix
				&& !(subscriberStatus.equalsIgnoreCase(DEACT_PENDING) || subscriberStatus
						.equalsIgnoreCase(SUSPENDED));
		if (addDetails) {
			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] bookmarks = rbtDBManager
					.getSubscriberBookMarks(subscriberID);
			WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = getWebServiceSubscriberBookMarkObjects(
					task, bookmarks);
			Element bookMarksElem = getSubscriberBookMarksElement(document,
					task, webServiceSubscriberBookMarks, bookmarks);
			subDetailsElem.appendChild(bookMarksElem);

			Element groupDetailsElem = getSubscriberGroupDetailsElement(
					document, task);
			subDetailsElem.appendChild(groupDetailsElem);
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #getSubscriberElement(org.w3c.dom.Document,
	 * com.onmobile.apps.ringbacktones.webservice.common.Task,
	 * com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber,
	 * com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServicesubscriber,
			Subscriber subscriber) {
		Element subscriberElement = super.getSubscriberElement(document, task,
				webServicesubscriber, subscriber);

		SubscriberDetail subscriberDetail = DataUtils.getSubscriberDetail(task);
		HashMap<String, String> subscriberDetailsMap = subscriberDetail
				.getSubscriberDetailsMap();
		if (subscriberDetailsMap != null) {
			if (subscriberDetailsMap.containsKey("SEGMENT-ORG")
					&& subscriberDetailsMap.get("SEGMENT-ORG") != null)
				subscriberElement.setAttribute(SEGMENT,
						subscriberDetailsMap.get("SEGMENT-ORG"));

			if (subscriberDetailsMap.containsKey("AGE")
					&& subscriberDetailsMap.get("AGE") != null)
				subscriberElement.setAttribute(AGE,
						subscriberDetailsMap.get("AGE"));
		}

		return subscriberElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #getSubscriberLibraryElement(org.w3c.dom.Document,
	 * com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryElement(Document document,
			WebServiceContext task) {
		Element element = document.createElement(LIBRARY);

		String subscriberID = task.getString(param_subscriberID);

		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		else
			subscriber = rbtDBManager.getSubscriber(subscriberID);

		if (subscriber != null && subscriber.cosID() != null) {
			CosDetails cosDetails = CacheManagerUtil
					.getCosDetailsCacheManager().getCosDetail(
							subscriber.cosID());

			int freeSongsLeft = (cosDetails.getFreeSongs() - subscriber
					.maxSelections());
			if (freeSongsLeft < 0)
				freeSongsLeft = 0;
			element.setAttribute(NO_OF_FREE_SONGS_LEFT,
					String.valueOf(freeSongsLeft));
		}

		SubscriberDownloads[] downloads = null;

		if (!task.containsKey(param_downloads))
			downloads = rbtDBManager.getActiveSubscriberDownloads(subscriberID);
		else
			downloads = (SubscriberDownloads[]) task.get(param_downloads);
		List<String> allCallerWavFile = new ArrayList<String>();
		if (downloads != null && downloads.length != 0) {
			for (int i = 0; i < downloads.length; i++) {
				if (downloads[i].downloadStatus() == 't') {
					allCallerWavFile.add(downloads[i].promoId());
				}
			}
		}

		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(
				task, downloads);
		Element downloadsElem = getSubscriberDownloadsElement(document, task,
				webServiceSubscriberDownloads, downloads);
		element.appendChild(downloadsElem);

		SubscriberStatus[] settings = null;
		if (!task.containsKey(param_settings))
			settings = rbtDBManager
					.getAllActiveSubscriberSettings(subscriberID);
		else
			settings = (SubscriberStatus[]) task.get(param_settings);

		String mode = "VP";
		if (task.containsKey(param_mode))
			mode = task.getString(param_mode);

		settings = DataUtils.getRecentActiveSettings(rbtDBManager, settings,
				mode, null);
		WebServiceSubscriberSetting[] webServiceSubscriberSettings = getWebServiceSubscriberSettingObjects(
				task, settings, webServiceSubscriberDownloads);

		Element settingsElem = getSubscriberSettingsElement(document, task,
				webServiceSubscriberSettings, settings);
		element.appendChild(settingsElem);

		if (webServiceSubscriberDownloads != null) {
			NodeList nodeList = downloadsElem.getElementsByTagName(CONTENT);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element contentElem = (Element) nodeList.item(i);
				NodeList propertyNodeList = contentElem.getChildNodes();
				String rbtFile = "";
				for (int j = 0; j < propertyNodeList.getLength(); j++) {
					String propertyName = ((Element) propertyNodeList.item(j))
							.getAttribute(NAME);
					if (propertyName.contains(RBT_FILE)) {
						rbtFile = ((Element) propertyNodeList.item(j))
								.getAttribute(VALUE);
						break;
					}
				}

				rbtFile = rbtFile.substring(0, rbtFile.lastIndexOf(".wav"));
				String setForAll = TefSpainDownloadUtility
						.isSetForAll(allCallerWavFile, rbtFile) ? YES : NO;

				Utility.addPropertyElement(document, contentElem,
						IS_SET_FOR_ALL, DATA, setForAll);
			}
		}

		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #getSubscriberLibraryHistoryElement(org.w3c.dom.Document,
	 * com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryHistoryElement(Document document,
			WebServiceContext task) {
		Element element = super.getSubscriberLibraryHistoryElement(document,
				task);

		SubscriberDownloads[] subscriberDownloads = DataUtils
				.getFilteredDownloadHistory(task);
		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(
				task, subscriberDownloads);
		Element downloadsElem = getSubscriberDownloadsElement(document, task,
				webServiceSubscriberDownloads, subscriberDownloads);

		element.appendChild(downloadsElem);
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #getWebServiceCopyDataObjects(com.onmobile.apps.ringbacktones.webservice.
	 * common.Task)
	 */
	@Override
	protected WebServiceCopyData[] getWebServiceCopyDataObjects(
			WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		String fromSubscriber = task.getString(param_fromSubscriber);
		Subscriber fromSubscriberObj = rbtDBManager
				.getSubscriber(fromSubscriber);
		String browsingLanguage = task.getString(param_browsingLanguage);

		String result = INVALID;
		int categoryID = 0;
		int toneID = 0;
		String toneName = "";
		String toneType = CLIP;
		int status = 1;
		String previewFile = null;

		ArrayList<WebServiceCopyData> copyList = new ArrayList<WebServiceCopyData>();

		if (rbtDBManager.isSubscriberDeactivated(fromSubscriberObj)
				|| rbtDBManager.isSubscriberSuspended(fromSubscriberObj)) {
			result = NOT_RBT_USER;
		} else {
			SubscriberStatus[] settings = rbtDBManager
					.getAllActiveSubscriberSettings(fromSubscriber);
			settings = DataUtils.getRecentActiveSettings(rbtDBManager,
					settings, "VP", null);

			if (settings == null) {
				result = DEFAULT_RBT;
			} else {
				ArrayList<SubscriberStatus> list = new ArrayList<SubscriberStatus>();
				ArrayList<SubscriberStatus> specialCallerList = new ArrayList<SubscriberStatus>();
				for (SubscriberStatus setting : settings) {
					if (setting.selType() == 2)
						continue; // Ignoring corporate selections

					if (setting.callerID() == null)
						list.add(setting);
					else if (setting.callerID().equalsIgnoreCase(subscriberID))
						specialCallerList.add(setting);
				}

				if (specialCallerList.size() > 0) {
					list.clear();
					list.addAll(specialCallerList);
				}

				if (list.size() == 0)
					result = DEFAULT_RBT;
				else {
					for (SubscriberStatus subscriberStatus : list) {
						Category category = rbtCacheManager
								.getCategory(subscriberStatus.categoryID(),
										browsingLanguage);
						status = subscriberStatus.status();

						if (category != null) {
							if (category.getCategoryTpe() == iRBTConstant.RECORD
									|| category.getCategoryTpe() == iRBTConstant.KARAOKE) {
								result = PERSONAL_MESSAGE;
								continue;
							}

							Clip clip = rbtCacheManager
									.getClipByRbtWavFileName(
											subscriberStatus.subscriberFile(),
											browsingLanguage);
							if (clip != null) {
								result = SUCCESS;
								categoryID = category.getCategoryId();
								toneID = clip.getClipId();
								toneName = clip.getClipName();

								if (category.getCategoryTpe() == iRBTConstant.SHUFFLE) {
									result = ALBUM_RBT;
									toneName = category.getCategoryName();
									toneType = CATEGORY_SHUFFLE;
								} else {
									Parameters blockedClipIDsParam = parametersCacheManager
											.getParameter(
													iRBTConstant.GATHERER,
													"COPY_BLOCKED_CLIP_IDS",
													null);
									if (blockedClipIDsParam != null) {
										List<String> blockedClipIDsList = Arrays
												.asList(blockedClipIDsParam
														.getValue().trim()
														.split(","));
										if (blockedClipIDsList.contains(String
												.valueOf(toneID))) {
											result = NOT_ALLOWED;
											continue;
										}
									}

									Category copyCategory = rbtCacheManager
											.getCategory(26, browsingLanguage);
									if (copyCategory != null)
										categoryID = 26;
								}

								if (category.getCategoryTpe() == iRBTConstant.SHUFFLE)
									previewFile = category
											.getCategoryPreviewWavFile();
								else
									previewFile = clip.getClipPreviewWavFile();
							}

							WebServiceCopyData webServiceCopyData = createWebServiceCopyDataObject();
							webServiceCopyData.setResult(result);
							webServiceCopyData.setSubscriberID(subscriberID);
							webServiceCopyData
									.setFromSubscriber(fromSubscriber);
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

		if (copyList.size() == 0) {
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
		WebServiceCopyData[] webServiceCopyDatas = copyList
				.toArray(new WebServiceCopyData[0]);
		return webServiceCopyDatas;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #getValidateNumberElement(org.w3c.dom.Document,
	 * com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	protected Element getValidateNumberElement(Document document,
			WebServiceContext task) {
		String action = task.getString(param_action);
		String subscriberID = task.getString(param_subscriberID);
		String number = task.getString(param_number);

		String response = VALID;
		String status = null;
		Set<String> validNumbers = new HashSet<String>();
		if (!com.onmobile.apps.ringbacktones.services.common.Utility
				.isValidNumber(subscriberID)) {
			response = INVALID_PARAMETER;
		} else if (action.equalsIgnoreCase(action_personalize)) {
			subscriberID = rbtDBManager.subID(subscriberID);
			number = rbtDBManager.subID(number);
			if (number.length() < 7)
				response = INVALID;
			else if (number.equalsIgnoreCase(subscriberID))
				response = OWN_NUMBER;
		} else if (action.equalsIgnoreCase(action_gift)
				|| action.equalsIgnoreCase(action_sendGift)) {

			boolean isNewCanBeGifted = parametersCacheManager
					.getParameter(iRBTConstant.COMMON,
							"NEW_USER_CAN_BE_GIFTED", "FALSE").getValue()
					.equalsIgnoreCase("TRUE");

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (!isNewCanBeGifted
					&& (subscriber == null || rbtDBManager
							.isSubDeactive(subscriber)))
				response = INVALID;
			else {
				if (number.indexOf(",") != -1) { // The case when there are
													// multiple giftees
					logger.debug("Comma-separated numbers. SubscriberId: "
							+ subscriberID + ", numbers: " + number);
					String numbers[] = number.split(",");
					for (String localNumber : numbers) {
						localNumber = rbtDBManager.subID(localNumber);
						if (localNumber.equalsIgnoreCase(subscriberID)) {
							logger.warn("Invalid number. Its same as the subscriberId. SubscriberId: "
									+ subscriberID + ", Number: " + localNumber);
						} else {
							String toneID = task.getString(param_toneID);
							if (task.containsKey(param_categoryID)) {
								String browsingLanguage = task
										.getString(param_browsingLanguage);
								int categoryID = Integer.parseInt(task
										.getString(param_categoryID));
								Category category = rbtCacheManager
										.getCategory(categoryID,
												browsingLanguage);
								if (Utility.isShuffleCategory(category
										.getCategoryTpe())) {
									toneID = "C" + categoryID;
								} else {
									logger.info("Category Type is not Shuffle. Fine if toneID param is present.");
								}
							}

							status = canBeGifted(subscriberID, localNumber,
									toneID);
							if (status.equals(VALID)
									|| status.equals(GIFTEE_ACTIVE)
									|| status.equalsIgnoreCase(GIFTEE_NEW_USER)) {
								logger.info("Valid number. SubscriberId: "
										+ subscriberID + ", Number: "
										+ localNumber + ", Status: " + status);
								validNumbers.add(localNumber);
							} else if (status.equalsIgnoreCase(INVALID)) {
								logger.warn("Invalid number. SubscriberId: "
										+ subscriberID + ", Number: "
										+ localNumber + ", Status: " + status);
							} else {
								logger.warn("Invalid number. SubscriberId: "
										+ subscriberID + ", Number: "
										+ localNumber + ", Status: " + status);
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
							Category category = rbtCacheManager.getCategory(
									categoryID, browsingLanguage);
							if (Utility.isShuffleCategory(category
									.getCategoryTpe()))
								toneID = "C" + categoryID;
							else {
								logger.info("Category Type is not Shuffle. Fine if toneID param is present.");
							}
						}

						status = canBeGifted(subscriberID, number, toneID);
						if (status.equals(VALID)
								|| status.equals(GIFTEE_ACTIVE)
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
		if (status != null && !status.equalsIgnoreCase(VALID)
				&& !status.equalsIgnoreCase(INVALID))
			element.setAttribute(STATUS, status);

		if (!validNumbers.isEmpty()) {
			String validNumberString = StringUtils.join(validNumbers, ",");
			element.setAttribute(VALID_NUMBERS, validNumberString);
			logger.debug("SubscriberId: " + subscriberID + ", Number: "
					+ number + ", validNumbers: " + validNumberString
					+ ", Status: " + status + ", Response: " + response);
		} else {
			logger.debug("SubscriberId: " + subscriberID + ", Number: "
					+ number + ", Status: " + status + ", Response: "
					+ response);
		}
		task.put(RESPONSE, response);
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #canBeGifted(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected String canBeGifted(String subscriberID, String callerID,
			String contentID) {
		SubscriberDetail callerIdDetail = RbtServicesMgr
				.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
		if (callerIdDetail == null || !callerIdDetail.isValidSubscriber()) {
			return INVALID;
		}

		SubscriberDetail subscriberIdDetail = RbtServicesMgr
				.getSubscriberDetail(new MNPContext(subscriberID, "GIFT"));
		boolean isNewCanBeGifted = parametersCacheManager
				.getParameter(iRBTConstant.COMMON, "NEW_USER_CAN_BE_GIFTED",
						"FALSE").getValue().equalsIgnoreCase("TRUE");
		if (!isNewCanBeGifted
				&& (subscriberIdDetail == null || !subscriberIdDetail
						.isValidSubscriber())) {
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
				logger.error("Category expired. Category end time: "
						+ category.getCategoryEndTime());
				return CATEGORY_EXPIRED;
			}
			if (category.getCategoryStartTime().after(new Date())) {
				logger.error("Category invalid. Category start time: "
						+ category.getCategoryStartTime());
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
				logger.error("Clip expired. Clip end time: "
						+ clip.getClipEndTime());
				return CLIP_EXPIRED;
			}
			if (clip.getClipStartTime().after(new Date())) {
				logger.error("Clip invalid. Clip start time: "
						+ clip.getClipEndTime());
				return INVALID;
			}
			clipRbtWavFile = clip.getClipRbtWavFile();
		}

		if (!rbtDBManager.isSubscriberDeactivated(caller)) {
			SubscriberDownloads[] subscriberDownloads = rbtDBManager
					.getSubscriberDownloads(callerID);
			if (subscriberDownloads != null) {
				for (SubscriberDownloads subscriberDownload : subscriberDownloads) {
					if ((isClip && subscriberDownload.promoId().equals(
							clipRbtWavFile))
							|| subscriberDownload.categoryID() == categoryID) {
						char downloadStatus = subscriberDownload
								.downloadStatus();
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

	public WebServiceSubscriberDownload[] getWebServiceSubscriberDownloadObjects(
			WebServiceContext task, SubscriberDownloads[] downloads) {
		if (downloads == null || downloads.length == 0)
			return null;

		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = new WebServiceSubscriberDownload[downloads.length];
		int finalSettingsCount = 0;
		String subscriberID = task.getString(param_subscriberID);
		String browsingLanguage = task.getString(param_browsingLanguage);
		List<ProvisioningRequests> provList = ProvisioningRequestsDao
				.getDeactivePendingODAPackBySubscriberID(subscriberID);
		List<Integer> deactivePendingODACatList = new ArrayList<Integer>();
		List<Integer> inActiveODACatList = new ArrayList<Integer>();
		if (provList != null) {
			for (ProvisioningRequests provReq : provList) {
				deactivePendingODACatList.add(provReq.getType());
			}
		}
		
		provList = ProvisioningRequestsDao
				.getDeactiveODAPackBySubscriberID(subscriberID);
		if (provList != null) {
			for (ProvisioningRequests provReq : provList) {
				inActiveODACatList.add(provReq.getType());
			}
		}

		for (int i = 0; i < downloads.length; i++) {
			int toneID;
			String toneName = null;
			String[] previewFiles = new String[2];
			String[] rbtFiles = new String[2];
			int categoryType = downloads[i].categoryType();
			String vcode = null;
			String toneType = null;
			if (Utility.isShuffleCategory(categoryType))
				toneType = CATEGORY_SHUFFLE;
			else if (categoryType == iRBTConstant.DYNAMIC_SHUFFLE)
				toneType = CATEGORY_DYNAMIC_SHUFFLE;
			else if (categoryType == iRBTConstant.RECORD)
				toneType = CATEGORY_RECORD;
			else if (categoryType == iRBTConstant.KARAOKE)
				toneType = CATEGORY_KARAOKE;
			else if (categoryType == iRBTConstant.FEED_CATEGORY)
				toneType = CATEGORY_FEED;
			else
				toneType = CLIP;

			Category category = null;
			if (categoryType == iRBTConstant.BOUQUET) {
				toneType = CATEGORY_BOUQUET;
				category = rbtCacheManager.getCategoryByPromoId(
						downloads[i].promoId(), browsingLanguage);
				if (category == null)
					throw new NullPointerException(
							"Category does not exist: categoryPromoID "
									+ downloads[i].promoId());
			} else if (Utility.isShuffleCategory(categoryType)) {
				category = rbtCacheManager.getCategory(
						downloads[i].categoryID(), browsingLanguage);
				if (category == null) {
					throw new NullPointerException(
							"Category does not exist: categoryID "
									+ downloads[i].categoryID());
				}
			}

			Clip clip = rbtCacheManager.getClipByRbtWavFileName(
					downloads[i].promoId(), browsingLanguage);
			if (clip != null) {
				toneID = clip.getClipId();
				toneName = clip.getClipName();
				previewFiles[0] = clip.getClipPreviewWavFile();
				rbtFiles[0] = clip.getClipRbtWavFile();
				vcode = clip.getClipRbtWavFile().replaceAll("rbt_", "")
						.replaceAll("_rbt", "");
			} else {
				toneID = downloads[i].categoryID();
				toneName = toneType;
				previewFiles[0] = downloads[i].promoId();
				rbtFiles[0] = downloads[i].promoId();
			}

			String shuffleID = null;
			if (category != null) {
				shuffleID = category.getCategoryPromoId();
				toneName = category.getCategoryName();
				previewFiles[1] = category.getCategoryPreviewWavFile();
				rbtFiles[1] = category.getCategoryNameWavFile();
			}
			// RBT-6459 : Unitel-Angola---- API Development for Online CRM
			// System
			String amount = "0";
			Category categoryObj = rbtCacheManager.getCategory(downloads[i]
					.categoryID());
			ChargeClass chargeClassObj = CacheManagerUtil
					.getChargeClassCacheManager().getChargeClass(
							downloads[i].classType());
			if (chargeClassObj != null) {
				amount = chargeClassObj.getAmount();
			}
			String downloadStatus = Utility
					.getSubscriberDownloadStatus(downloads[i]);
			String downloadType = Utility
					.getSubscriberDownloadType(downloads[i]);

			char downloadStatusID = downloads[i].downloadStatus();
			int categoryID = downloads[i].categoryID();
			String chargeClass = downloads[i].classType();
			String selectedBy = downloads[i].selectedBy();
			String deselectedBy = downloads[i].deactivatedBy();
			Date setTime = downloads[i].setTime();
			Date startTime = downloads[i].startTime();
			Date endTime = downloads[i].endTime();
			String refID = downloads[i].refID();
			String downloadInfo = downloads[i].extraInfo();
			Date lastChargedDate = downloads[i].lastChargedDate();
			boolean isModeAllowedForUGC = isModeAllowedForUGC(task, toneType);
			logger.info("isModeAllowedForUGC: " + isModeAllowedForUGC);
			if (!isModeAllowedForUGC) {
				continue;
			}
			if (categoryType == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
				if (inActiveODACatList.contains(categoryID)
						&& downloads[i].downloadStatus() != 't') {
					continue;
				} else if (deactivePendingODACatList.contains(categoryID)) {
					continue;
				} else {
					downloadStatus = ACTIVE;
				}
			} else if (categoryType != iRBTConstant.PLAYLIST_ODA_SHUFFLE
					&& downloads[i].downloadStatus() == iRBTConstant.STATE_DOWNLOAD_SEL_TRACK) {
				continue;
			}
			finalSettingsCount++;
			WebServiceSubscriberDownload webServiceSubscriberDownload = createWebServiceSubscriberDownloadObject();
			webServiceSubscriberDownload.setSubscriberID(subscriberID);
			webServiceSubscriberDownload.setToneID(toneID);
			webServiceSubscriberDownload.setShuffleID(shuffleID);
			webServiceSubscriberDownload.setToneName(toneName);
			webServiceSubscriberDownload.setToneType(toneType);
			webServiceSubscriberDownload.setPreviewFiles(previewFiles);
			webServiceSubscriberDownload.setRbtFiles(rbtFiles);
			webServiceSubscriberDownload.setDownloadStatus(downloadStatus);
			webServiceSubscriberDownload.setDownloadStatusID(downloadStatusID);
			webServiceSubscriberDownload.setCategoryID(categoryID);
			webServiceSubscriberDownload.setChargeClass(chargeClass);
			webServiceSubscriberDownload.setSelectedBy(selectedBy);
			webServiceSubscriberDownload.setDeselectedBy(deselectedBy);
			webServiceSubscriberDownload.setSetTime(setTime);
			webServiceSubscriberDownload.setStartTime(startTime);
			webServiceSubscriberDownload.setEndTime(endTime);
			webServiceSubscriberDownload.setRefID(refID);
			webServiceSubscriberDownload.setDownloadType(downloadType);
			webServiceSubscriberDownload.setDownloadInfo(downloadInfo);
			webServiceSubscriberDownload.setSelectionInfo(downloads[i]
					.selectionInfo());
			// RBT-6459 : Unitel-Angola---- API Development for Online CRM
			// System
			if (categoryObj != null) {
				webServiceSubscriberDownload.setCategoryName(categoryObj
						.getCategoryName());
			}
			webServiceSubscriberDownload.setArtistName((clip != null) ? clip
					.getArtist() : null);
			webServiceSubscriberDownload.setAlbumName((clip != null) ? clip
					.getAlbum() : null);
			webServiceSubscriberDownload.setTonePrice(amount);
			webServiceSubscriberDownload.setDefaultMusic(false);
			webServiceSubscriberDownload.setClipVcode(vcode);
			webServiceSubscriberDownload.setLastChargedDate(lastChargedDate);
			webServiceSubscriberDownloads[i] = webServiceSubscriberDownload;
		}
		if (finalSettingsCount != downloads.length) {
			WebServiceSubscriberDownload[] finalWebServiceSubscriberDownloads = new WebServiceSubscriberDownload[finalSettingsCount];
			int cnt = 0;
			for (WebServiceSubscriberDownload downloadObj : webServiceSubscriberDownloads) {
				if (null != downloadObj) {
					finalWebServiceSubscriberDownloads[cnt++] = downloadObj;
				}
			}
			if (logger.isDebugEnabled())
				logger.debug("RBT:: finalWebServiceSubscriberDownloads: "
						+ Arrays.toString(finalWebServiceSubscriberDownloads));
			return finalWebServiceSubscriberDownloads;
		}
		if (logger.isDebugEnabled())
			logger.debug("RBT:: webServiceSubscriberDownloads: "
					+ Arrays.toString(webServiceSubscriberDownloads));
		return webServiceSubscriberDownloads;
	}

	protected WebServiceSubscriberSetting[] getWebServiceSubscriberSettingObjects(
			WebServiceContext task, SubscriberStatus[] settings,
			WebServiceSubscriberDownload[] webServiceSubscriberDownloads) {
		// RBT-6459 : Unitel-Angola---- API Development for Online CRM System
		String subscriberID = task.getString(param_subscriberID);
		if (settings == null || settings.length == 0) {
			WebServiceSubscriberSetting[] webServiceSubscriberSettings = new WebServiceSubscriberSetting[1];
			Parameters paramObj = parametersCacheManager.getParameter(
					"WEBSERVICES", "DEFAULT_CLIP", null);
			if (paramObj != null) {
				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				Clip clipObj = rbtCacheManager.getClip(paramObj.getValue());
				String[] previewFiles = new String[2];
				String[] rbtFiles = new String[2];
				String amount = "0";
				ChargeClass chargeClassObj = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(
								clipObj.getClassType());
				if (chargeClassObj != null) {
					amount = chargeClassObj.getAmount();
				}
				previewFiles[0] = clipObj.getClipPreviewWavFile();
				rbtFiles[0] = clipObj.getClipRbtWavFile();
				WebServiceSubscriberSetting webServiceSubscriberDefaultSetting = createWebServiceSubscriberSettingObject();
				webServiceSubscriberDefaultSetting
						.setSubscriberID(subscriberID);
				webServiceSubscriberDefaultSetting.setCallerID("all");
				webServiceSubscriberDefaultSetting.setToneType("clip");
				webServiceSubscriberDefaultSetting.setToneID(clipObj
						.getClipId());
				webServiceSubscriberDefaultSetting.setToneName(clipObj
						.getClipName());
				webServiceSubscriberDefaultSetting.setClipVcode(clipObj
						.getClipRbtWavFile().replaceAll("rbt_", "")
						.replaceAll("_rbt", ""));
				webServiceSubscriberDefaultSetting
						.setPreviewFiles(previewFiles);
				webServiceSubscriberDefaultSetting.setRbtFiles(rbtFiles);
				webServiceSubscriberDefaultSetting.setArtistName(clipObj
						.getArtist());
				webServiceSubscriberDefaultSetting.setTonePrice(amount);
				webServiceSubscriberDefaultSetting.setSetTime(subscriber
						.startDate());
				webServiceSubscriberDefaultSetting.setStartTime(subscriber
						.startDate());
				webServiceSubscriberDefaultSetting.setEndTime(subscriber
						.endDate());
				webServiceSubscriberDefaultSetting.setAlbumName(clipObj
						.getAlbum());
				webServiceSubscriberDefaultSetting.setDefaultMusic(true);
				webServiceSubscriberSettings[0] = webServiceSubscriberDefaultSetting;
				return webServiceSubscriberSettings;
			} else {
				return null;
			}
		} else {

			WebServiceSubscriberSetting[] webServiceSubscriberSettings = new WebServiceSubscriberSetting[settings.length];

			// String subscriberID = task.getString(param_subscriberID);
			Map<String, List<Integer>> activeWavfilesToCategoriesListInDwd = new HashMap<String, List<Integer>>();
			String browsingLanguage = task.getString(param_browsingLanguage);
			if(webServiceSubscriberDownloads != null && webServiceSubscriberDownloads.length > 0){
			for (WebServiceSubscriberDownload webServiceSubscriberDownload : webServiceSubscriberDownloads) {
				if (!(webServiceSubscriberDownload.getDownloadStatus()
						.equalsIgnoreCase(DEACT_PENDING)
						&& webServiceSubscriberDownload.getDownloadStatus()
								.equalsIgnoreCase(DEACTIVE) && webServiceSubscriberDownload.getDownloadStatus()
								.equalsIgnoreCase(ERROR))) {
					String wavFile = webServiceSubscriberDownload.getRbtFiles()[0];
					List<Integer> categoriesList = null;
					if(activeWavfilesToCategoriesListInDwd.get(wavFile) != null){
						categoriesList = activeWavfilesToCategoriesListInDwd.get(wavFile);
					} else{
						categoriesList = new ArrayList<Integer>();
					}
					categoriesList.add(webServiceSubscriberDownload.getCategoryID());
					activeWavfilesToCategoriesListInDwd.put(wavFile, categoriesList);
				}
			}
			}
			
			int finalSettingsCount = 0;
			// RBT-12247
			String currentRefId = task.getString("CURRENT_REF_ID");
			for (int i = 0; i < settings.length; i++) {
				String selWavFileName = settings[i].subscriberFile();
				int catId = settings[i].categoryID();
				List<Integer> categoryIdList = null;
				
				//Check whether corresponding download is there in active status or not for this selection by comparing wav file name and category id
				if(activeWavfilesToCategoriesListInDwd != null && activeWavfilesToCategoriesListInDwd.get(selWavFileName) != null){
					categoryIdList = activeWavfilesToCategoriesListInDwd.get(selWavFileName);
				} else{
					continue;
				}
				if(categoryIdList != null && !categoryIdList.contains(catId)){
					continue;
				}
				String callerID = settings[i].callerID() == null ? ALL
						: settings[i].callerID();
				int status = settings[i].status();

				int fromTime = settings[i].fromTime();
				int toTime = settings[i].toTime();

				DecimalFormat decimalFormat = new DecimalFormat("0000");
				String fromTimeStr = decimalFormat.format(fromTime);
				String toTimeStr = decimalFormat.format(toTime);

				int fromTimeHrs = Integer.parseInt(fromTimeStr.substring(0, 2));
				int fromTimeMinutes = Integer.parseInt(fromTimeStr.substring(2,
						4));
				int toTimeHrs = Integer.parseInt(toTimeStr.substring(0, 2));
				int toTimeMinutes = Integer.parseInt(toTimeStr.substring(2, 4));

				int categoryType = settings[i].categoryType();
				String chargeClass = settings[i].classType();
				String selInterval = settings[i].selInterval();
				int categoryID = settings[i].categoryID();

				String toneType = null;
				if (status == 99)
					toneType = PROFILE;
				else if (status == 90)
					toneType = CRICKET;
				else if (Utility.isShuffleCategory(categoryType))
					toneType = CATEGORY_SHUFFLE;
				else if (categoryType == iRBTConstant.DYNAMIC_SHUFFLE)
					toneType = CATEGORY_DYNAMIC_SHUFFLE;
				else if (categoryType == iRBTConstant.RECORD)
					toneType = CATEGORY_RECORD;
				else if (categoryType == iRBTConstant.KARAOKE)
					toneType = CATEGORY_KARAOKE;
				else if (categoryType == iRBTConstant.FEED_CATEGORY)
					toneType = CATEGORY_FEED;
				else
					toneType = CLIP;

				int toneID;
				String toneName = null;
				String[] previewFiles = new String[2];
				String[] rbtFiles = new String[2];
				String clipVcode = null;
				Category category = null;
				if (categoryType == iRBTConstant.BOUQUET) {
					toneType = CATEGORY_BOUQUET;
					category = rbtCacheManager.getCategoryByPromoId(
							settings[i].subscriberFile(), browsingLanguage);
					if (category == null)
						throw new NullPointerException(
								"Category does not exist: categoryPromoID "
										+ settings[i].subscriberFile());
				} else if (Utility.isShuffleCategory(categoryType)) {
					category = rbtCacheManager.getCategory(
							settings[i].categoryID(), browsingLanguage);
					if (category == null)
						throw new NullPointerException(
								"Category does not exist: categoryID "
										+ settings[i].categoryID());
				}

				Clip clip = rbtCacheManager.getClipByRbtWavFileName(
						settings[i].subscriberFile(), browsingLanguage);
				if (clip != null) {
					toneID = clip.getClipId();
					toneName = clip.getClipName();
					previewFiles[0] = clip.getClipPreviewWavFile();
					rbtFiles[0] = clip.getClipRbtWavFile();
					clipVcode = clip.getClipRbtWavFile().replaceAll("rbt_", "")
							.replaceAll("_rbt", "");
				} else {
					toneID = settings[i].categoryID();
					toneName = toneType;
					previewFiles[0] = settings[i].subscriberFile();
					rbtFiles[0] = settings[i].subscriberFile();
				}

				String shuffleID = null;
				if (category != null) {
					shuffleID = category.getCategoryPromoId();
					toneName = category.getCategoryName();
					previewFiles[1] = category.getCategoryPreviewWavFile();
					rbtFiles[1] = category.getCategoryNameWavFile();
				}
				// RBT-6459 : Unitel-Angola---- API Development for Online CRM
				// System
				String amount = "0";
				Category categoryObj = rbtCacheManager.getCategory(settings[i]
						.categoryID());
				ChargeClass chargeClassObj = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(
								settings[i].classType());
				if (chargeClassObj != null) {
					amount = chargeClassObj.getAmount();
				}
				String selectionStatus = Utility
						.getSubscriberSettingStatus(settings[i]);
				String selectionType = Utility
						.getSubscriberSettingType(settings[i]);

				String selectionStatusID = settings[i].selStatus();
				String selectedBy = settings[i].selectedBy();
				String selectionInfo = settings[i].selectionInfo();
				String deselectedBy = settings[i].deSelectedBy();
				Date setTime = settings[i].setTime();
				Date startTime = settings[i].startTime();
				Date endTime = settings[i].endTime();
				Date nextChargingDate = settings[i].nextChargingDate();
				String refID = settings[i].refID();
				String selectionExtraInfo = settings[i].extraInfo();
				boolean isModeAllowedForUGC = isModeAllowedForUGC(task,
						toneType);
				logger.info("isModeAllowedForUGC: " + isModeAllowedForUGC);
				if (!isModeAllowedForUGC) {
					continue;
				}
				finalSettingsCount++;
				WebServiceSubscriberSetting webServiceSubscriberSetting = createWebServiceSubscriberSettingObject();
				webServiceSubscriberSetting.setSubscriberID(subscriberID);
				webServiceSubscriberSetting.setCallerID(callerID);
				webServiceSubscriberSetting.setToneID(toneID);
				webServiceSubscriberSetting.setShuffleID(shuffleID);
				webServiceSubscriberSetting.setToneName(toneName);
				webServiceSubscriberSetting.setToneType(toneType);
				webServiceSubscriberSetting.setPreviewFiles(previewFiles);
				webServiceSubscriberSetting.setRbtFiles(rbtFiles);
				webServiceSubscriberSetting.setFromTime(fromTimeHrs);
				webServiceSubscriberSetting.setFromTimeMinutes(fromTimeMinutes);
				webServiceSubscriberSetting.setToTime(toTimeHrs);
				webServiceSubscriberSetting.setToTimeMinutes(toTimeMinutes);
				webServiceSubscriberSetting.setStatus(status);
				webServiceSubscriberSetting.setChargeClass(chargeClass);
				webServiceSubscriberSetting.setSelInterval(selInterval);
				webServiceSubscriberSetting.setCategoryID(categoryID);
				webServiceSubscriberSetting.setSelectionStatus(selectionStatus);
				webServiceSubscriberSetting
						.setSelectionStatusID(selectionStatusID);
				webServiceSubscriberSetting.setSelectedBy(selectedBy);
				webServiceSubscriberSetting.setSelectionInfo(selectionInfo);
				webServiceSubscriberSetting.setDeselectedBy(deselectedBy);
				webServiceSubscriberSetting.setSetTime(setTime);
				webServiceSubscriberSetting.setStartTime(startTime);
				webServiceSubscriberSetting.setEndTime(endTime);
				webServiceSubscriberSetting
						.setNextChargingDate(nextChargingDate);
				webServiceSubscriberSetting.setRefID(refID);
				webServiceSubscriberSetting.setSelectionType(selectionType);
				webServiceSubscriberSetting
						.setSelectionExtraInfo(selectionExtraInfo);
				webServiceSubscriberSetting.setLoopStatus(settings[i]
						.loopStatus() + "");
				// RBT-6459 : Unitel-Angola---- API Development for Online CRM
				// System
				if (categoryObj != null) {
					webServiceSubscriberSetting.setCategoryName(categoryObj
							.getCategoryName());
				}
				webServiceSubscriberSetting.setArtistName((clip != null) ? clip
						.getArtist() : null);
				webServiceSubscriberSetting.setAlbumName((clip != null) ? clip
						.getAlbum() : null);
				webServiceSubscriberSetting.setTonePrice(amount);
				webServiceSubscriberSetting.setDefaultMusic(false);
				webServiceSubscriberSetting.setClipVcode(clipVcode);

				if (currentRefId != null
						&& currentRefId.equalsIgnoreCase(refID)) {
					webServiceSubscriberSetting.setIsCurrentSetting(true);
				}
				webServiceSubscriberSettings[i] = webServiceSubscriberSetting;
			}
			if (finalSettingsCount != settings.length) {
				WebServiceSubscriberSetting[] finalWebServiceSubscriberSettings = new WebServiceSubscriberSetting[finalSettingsCount];
				int cnt = 0;
				for (WebServiceSubscriberSetting settingObj : webServiceSubscriberSettings) {
					if (null != settingObj) {
						finalWebServiceSubscriberSettings[cnt++] = settingObj;
					}
				}
				if (logger.isDebugEnabled())
					logger.debug("RBT:: finalWebServiceSubscriberSettings: "
							+ Arrays.toString(finalWebServiceSubscriberSettings));
				return finalWebServiceSubscriberSettings;
			}
			if (logger.isDebugEnabled())
				logger.debug("RBT:: webServiceSubscriberSettings: "
						+ Arrays.toString(webServiceSubscriberSettings));
			return webServiceSubscriberSettings;
		}
	}

}
