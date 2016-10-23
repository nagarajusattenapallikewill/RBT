/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.vodacom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTBulkUploadTaskDAO;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
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
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;

/**
 * @author vinayasimha.patil
 *
 */
public class VodacomRBTInformation extends BasicRBTInformation
{
	private static Logger logger = Logger.getLogger(VodacomRBTInformation.class);

	/**
	 * @throws ParserConfigurationException
	 */
	public VodacomRBTInformation() throws ParserConfigurationException
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
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSpecificRBTInformationDocument(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getSpecificRBTInformationDocument(WebServiceContext task)
	{
		String info = task.getString(param_info);
		if (info.equalsIgnoreCase(DOWNLOADS))
		{
			Document document = documentBuilder.newDocument();
			Element element = document.createElement(RBT);
			document.appendChild(element);

			Element responseElem = Utility.getResponseElement(document, SUCCESS);
			element.appendChild(responseElem);

			Element libraryElem = document.createElement(LIBRARY);

			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] downloads = rbtDBManager.getSubscriberDownloads(subscriberID);
			if (downloads != null)
			{
				List<SubscriberDownloads> list = new ArrayList<SubscriberDownloads>();

				for (SubscriberDownloads subscriberDownload : downloads)
				{
					String downloadStatus = Utility.getSubscriberDownloadStatus(subscriberDownload);
					if (!downloadStatus.equalsIgnoreCase(DEACTIVE))
						list.add(subscriberDownload);
				}

				if (list.size() > 0)
					downloads = list.toArray(new SubscriberDownloads[0]);
				else
					downloads = null;
			}

			WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(task, downloads);
			Element downloadsElem = getSubscriberDownloadsElement(document, task, webServiceSubscriberDownloads, downloads);
			libraryElem.appendChild(downloadsElem);

			element.appendChild(libraryElem);

			return document;
		}

		return super.getSpecificRBTInformationDocument(task);
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
			downloads = rbtDBManager.getSubscriberDownloads(subscriberID);
		else
			downloads = (SubscriberDownloads[])task.get(param_downloads);
		
		if (downloads != null)
		{
			List<SubscriberDownloads> list = new ArrayList<SubscriberDownloads>();

			for (SubscriberDownloads subscriberDownload : downloads)
			{
				String downloadStatus = Utility.getSubscriberDownloadStatus(subscriberDownload);
				if (!downloadStatus.equalsIgnoreCase(DEACTIVE))
					list.add(subscriberDownload);
			}

			if (list.size() > 0)
				downloads = list.toArray(new SubscriberDownloads[0]);
			else
				downloads = null;
		}

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

		// Adding nextBillingDate property only if it is not library history request.
		// Because for library history request nextBillingDate property is added in the basic implementation.
		if (!task.containsKey(param_info) || !task.getString(param_info).contains(LIBRARY_HISTORY))
		{
			String nextBillingDate = null;
			
			HashMap<String, String> downloadExtraInfo = DBUtility.getAttributeMapFromXML(webServiceSubscriberDownload.getDownloadInfo());
			
			if (downloadExtraInfo != null && downloadExtraInfo.containsKey(CAMPAIGN_ID))
			{
				//update the next billing date with the campaign end time
				SimpleDateFormat rbtDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				RBTBulkUploadTask rbtBulkUploadTask = RBTBulkUploadTaskDAO.getRBTBulkUploadTask(Integer.parseInt(downloadExtraInfo.get(CAMPAIGN_ID)));
				nextBillingDate = rbtDateFormat.format(rbtBulkUploadTask.getEndTime());
			}
			else
			{
				//get next billing date from the SM 
				nextBillingDate = Utility.getNextBillingDateOfServices(task).get(webServiceSubscriberDownload.getRefID());
			}
		
			Utility.addPropertyElement(document, element, NEXT_BILLING_DATE, DATA, nextBillingDate);
		}

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
		Subscriber fromSubscriberObj = rbtDBManager.getSubscriber(fromSubscriber);
		String browsingLanguage = task.getString(param_browsingLanguage);
		boolean isShuffleOrLoop = false;

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
				logger.info("Subscriber Status length is : " + settings.length);
				for (SubscriberStatus setting : settings)
				{
					if (setting.selType() == 2)
						continue; // Ignoring corporate selections
					
					if(setting.callerID() == null || setting.callerID().equalsIgnoreCase(subscriberID))
					{
						if(Utility.isShuffleCategory(setting.categoryType()) ||(settings.length > 1 && ( setting.loopStatus()== 'l' || setting.loopStatus()== 'L' || setting.loopStatus()== 'A')))
							isShuffleOrLoop = true;
					}
					if (setting.callerID() == null)
						list.add(setting);
					else if (setting.callerID().equalsIgnoreCase(subscriberID))
						specialCallerList.add(setting);
				}

				if (specialCallerList.size() > 0)
				{
					isShuffleOrLoop = false ;
					for (SubscriberStatus subscriberStatus : specialCallerList)
					{
						if(subscriberStatus.callerID() == null || subscriberStatus.callerID().equalsIgnoreCase(subscriberID))
						{
							if(Utility.isShuffleCategory(subscriberStatus.categoryType()) ||( specialCallerList.size() > 1 &&( subscriberStatus.loopStatus()== 'l' || subscriberStatus.loopStatus()== 'L' || subscriberStatus.loopStatus()== 'A')))
								isShuffleOrLoop = true;
						}
					}

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
							webServiceCopyData.setShuffleOrLoop(isShuffleOrLoop);

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
			webServiceCopyData.setShuffleOrLoop(isShuffleOrLoop);

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
	protected String canBeGifted(String subscriberID,String callerID,String contentID)
	{	
		SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
		if (subscriberDetail != null && subscriberDetail.isValidSubscriber())
			return VALID;

		return INVALID;
	}
}
