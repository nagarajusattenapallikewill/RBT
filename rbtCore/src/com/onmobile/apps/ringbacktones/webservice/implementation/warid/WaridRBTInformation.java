/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.warid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceCopyData;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;

/**
 * @author vinayasimha.patil
 *
 */
public class WaridRBTInformation extends BasicRBTInformation
{
	private static Logger logger = Logger.getLogger(WaridRBTInformation.class);

	/**
	 * @throws ParserConfigurationException
	 */
	public WaridRBTInformation() throws ParserConfigurationException
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

		boolean addBookMarks = canAllow && isValidPrefix
		&& !(subscriberStatus.equalsIgnoreCase(DEACT_PENDING) || subscriberStatus.equalsIgnoreCase(SUSPENDED));
		if (addBookMarks)
		{
			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] bookmarks = rbtDBManager.getSubscriberBookMarks(subscriberID);
			WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = getWebServiceSubscriberBookMarkObjects(task, bookmarks);
			Element bookMarksElement = getSubscriberBookMarksElement(document, task, webServiceSubscriberBookMarks, bookmarks);

			Element subDetailsElem = (Element)document.getElementsByTagName(SUBSCRIBER_DETAILS).item(0);
			subDetailsElem.appendChild(bookMarksElement);
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
		
		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(task, downloads);
		Element downloadsElem = getSubscriberDownloadsElement(document, task, webServiceSubscriberDownloads, downloads);

		element.appendChild(downloadsElem);

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
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberDownloadContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload)
	 */
	@Override
	protected Element getSubscriberDownloadContentElement(Document document,
			WebServiceContext task, WebServiceSubscriberDownload webServiceSubscriberDownload)
	{
		Element element = super.getSubscriberDownloadContentElement(document, task, webServiceSubscriberDownload);

		Date contentEndTime = null;
		String toneType = webServiceSubscriberDownload.getToneType();
		String browsingLanguage = task.getString(param_browsingLanguage);
		if (toneType.equalsIgnoreCase(CATEGORY_SHUFFLE))
		{
			Category category = rbtCacheManager.getCategory(webServiceSubscriberDownload.getCategoryID(), browsingLanguage);
			if (category != null)
				contentEndTime = category.getCategoryEndTime();
		}
		else if (toneType.equalsIgnoreCase(CLIP))
		{
			Clip clip = rbtCacheManager.getClip(webServiceSubscriberDownload.getToneID(), browsingLanguage);
			if (clip != null)
				contentEndTime = clip.getClipEndTime();
		}

		if (contentEndTime != null)
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			Utility.addPropertyElement(document, element, CONTENT_END_TIME, DATA, dateFormat.format(contentEndTime));
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
				ArrayList<SubscriberStatus> specialCallerList = new ArrayList<SubscriberStatus>();
				for (SubscriberStatus setting : settings)
				{
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
					String browsingLanguage = task.getString(param_browsingLanguage);
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
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#canBeGifted(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected String canBeGifted(String subscriberID, String callerID, String contentID)
	{
		String isValidResponse = super.canBeGifted(subscriberID, callerID, contentID);
		if (!isValidResponse.equals(VALID))
			return isValidResponse;

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

		Subscriber caller = rbtDBManager.getSubscriber(callerID);
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
		return VALID;
	}
}
