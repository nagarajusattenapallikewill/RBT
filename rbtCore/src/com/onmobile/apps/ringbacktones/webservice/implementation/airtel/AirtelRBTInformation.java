/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.airtel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.TransData;
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
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;


/**
 * @author vinayasimha.patil
 *
 */
public class AirtelRBTInformation extends BasicRBTInformation
{
	private static Logger logger = Logger.getLogger(AirtelRBTInformation.class);

	/**
	 * @throws ParserConfigurationException
	 */
	public AirtelRBTInformation() throws ParserConfigurationException
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

		boolean isAlbumUser = false;
		Element libraryElem = (Element) document.getElementsByTagName(LIBRARY).item(0);
		if (libraryElem != null)
		{
			Attr isAlbumUserAttr = libraryElem.getAttributeNode(IS_ALBUM_USER);
			if (isAlbumUserAttr != null)
				isAlbumUser = libraryElem.getAttribute(IS_ALBUM_USER).equalsIgnoreCase(YES);
			else	
				libraryElem.setAttribute(IS_ALBUM_USER, NO);
		}

		if (isAlbumUser)
		{
			// Album are not allowed for Advance Rental upgradation.
			Element advanceRentalDetailsElement = (Element) document.getElementsByTagName(ADVANCE_RENTAL_PACKS).item(0);
			if (advanceRentalDetailsElement != null)
				advanceRentalDetailsElement.setAttribute(CAN_ALLOW, NO);
		}

		if (libraryElem != null)
		{
			Element subscriberElem = (Element)document.getElementsByTagName(SUBSCRIBER).item(0);
			String subscriberStatus = subscriberElem.getAttribute(STATUS);

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
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSelectionResponseDocument(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getSelectionResponseDocument(WebServiceContext task)
	{
		Document document = super.getSelectionResponseDocument(task);

		String action = task.getString(param_action);
		if (action.equalsIgnoreCase(action_set) || action.equalsIgnoreCase(action_acceptGift))
		{
			Element smsElement = document.createElement(SMS);

			String response = task.getString(param_response);
			boolean sendSMS = response.equalsIgnoreCase(SUCCESS);

			if (task.containsKey(param_mmContext))
			{
				String[] mmContext = task.getString(param_mmContext).split("\\|");
				if (mmContext[0].equalsIgnoreCase("RBT_EASY_CHARGE") || mmContext[0].equalsIgnoreCase("RBT_CRICKET"))
					sendSMS = false;
			}

			if (task.containsKey(param_cricketPack) || !task.containsKey(param_clipID))
				sendSMS = false;

			if (sendSMS)
			{
				String subscriberID = task.getString(param_subscriberID);
				int categoryID = Integer.parseInt(task.getString(param_categoryID));
				String browsingLanguage = task.getString(param_browsingLanguage);
				
				Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
				if (category.getCategoryTpe() == iRBTConstant.DTMF_CATEGORY)
				{
					int clipID = Integer.parseInt(task.getString(param_clipID));
					Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);

					if (clip != null && clip.getClipPromoId() != null)
					{
						Parameters odsParam = parametersCacheManager.getParameter(iRBTConstant.SMS, "BASE_NUMBER_ODS", "");
						String odsNumber = odsParam.getValue().trim();

						Parameters senderParam = parametersCacheManager.getParameter(iRBTConstant.SMS, "SENDER_NUMBER", "543211");
						String senderNumber = senderParam.getValue().trim();

						String callNumber = odsNumber + clip.getClipPromoId();
						String smsText ="To listen to the complete song " + clip.getClipName() + ", call " + callNumber;

						smsElement.setAttribute(SENDER, senderNumber);
						smsElement.setAttribute(RECEIVER, subscriberID);
						smsElement.setAttribute(SMS_TEXT, smsText);
					}
					else
						sendSMS = false;
				}
				else
					sendSMS = false;
			}
			smsElement.setAttribute(SEND_SMS, (sendSMS ? YES : NO));

			Element element = (Element)document.getElementsByTagName(RBT).item(0);
			element.appendChild(smsElement);
		}

		return document;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getWebServiceSubscriberObject(com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected WebServiceSubscriber getWebServiceSubscriberObject(WebServiceContext task, Subscriber subscriber)
	{
		WebServiceSubscriber webServiceSubscriber = super.getWebServiceSubscriberObject(task, subscriber);

		if (task.containsKey(param_mmContext))
		{
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			if (mmContext[0].equalsIgnoreCase("RBT_ALBUM") && !rbtDBManager.isSubscriberDeactivated(subscriber))
			{
				String subscriptionClass = subscriber.subscriptionClass();

				// Advance Rental Users are not allowed for Album Selection.
				if (Utility.isAdvanceRentalPack(subscriptionClass))
					webServiceSubscriber.setCanAllow(false);

				logger.info("RBT:: webServiceSubscriber: " + webServiceSubscriber);
			}
		}

		return webServiceSubscriber;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberLibraryElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryElement(Document document, WebServiceContext task)
	{
		Element element = super.getSubscriberLibraryElement(document, task);

		String isAlbumUser = NO;
		Element settingsElem = (Element) element.getElementsByTagName(SETTINGS).item(0);
		if (settingsElem != null)
		{
			NodeList contentNodeList = settingsElem.getChildNodes().item(0).getChildNodes();
			for (int i = 0; i < contentNodeList.getLength(); i++)
			{
				Element contentElem = (Element) contentNodeList.item(i);

				if (contentElem.getAttribute(TYPE).equalsIgnoreCase(CATEGORY_SHUFFLE))
				{
					isAlbumUser = YES;
					break;
				}
			}
		}
		element.setAttribute(IS_ALBUM_USER, isAlbumUser);

		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		else
			subscriber = rbtDBManager.getSubscriber(task.getString(param_subscriberID));

		String nextChargeClass = rbtDBManager.getNextChargeClass(subscriber);
		if (nextChargeClass != null && !nextChargeClass.equalsIgnoreCase("DEFAULT"))
		{
			ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(nextChargeClass);
			if(chargeClass != null){
			element.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
			}
			}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberSettingsElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting[], com.onmobile.apps.ringbacktones.content.SubscriberStatus[])
	 */
	@Override
	protected Element getSubscriberSettingsElement(Document document, WebServiceContext task,
			WebServiceSubscriberSetting[] webServiceSubscriberSettings,
			SubscriberStatus[] settings)
	{
		Element element = super.getSubscriberSettingsElement(document, task,
				webServiceSubscriberSettings, settings);

		if (settings == null || settings.length == 0)
			return element;

		ArrayList<String> callerIDList = new ArrayList<String>(); 
		for (SubscriberStatus subscriberStatus : settings)
		{
			if (subscriberStatus.callerID() == null)
				continue;

			if (!callerIDList.contains(subscriberStatus.callerID()))
				callerIDList.add(subscriberStatus.callerID());
		}
		element.setAttribute(NO_OF_SPECIAL_SETTINGS, String.valueOf(callerIDList.size()));

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

		Parameters useProxyParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "USE_PROXY", "FALSE");
		boolean useProxy = useProxyParam.getValue().trim().equalsIgnoreCase("TRUE");

		Parameters proxyServerPortParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "PROXY_SERVER_PORT", "8000");
		String proxyServerPort = proxyServerPortParam.getValue().trim();

		String result = ERROR;
		Clip copyTune = null;
		int categoryID = 0;
		int toneID = 0;
		String toneName = "";
		String toneType = CLIP;
		int status = 1;
		String previewFile = null;

		try
		{
			String browsingLanguage = task.getString(param_browsingLanguage);
			String response = rbtDBManager.getSubscriberVcode(fromSubscriber, subscriberID, useProxy, proxyServerPort);
			if (response.equalsIgnoreCase("NOT_VALID"))
				result = INVALID;
			else if (response.equalsIgnoreCase("NOT_FOUND"))
				result = NOT_RBT_USER;
			else if (response.equalsIgnoreCase("ALBUM"))
				result = ALBUM_RBT;
			else if (response.equalsIgnoreCase("ERROR"))
				result = ERROR;
			else if (response.equalsIgnoreCase("DEFAULT"))
			{
				result = DEFAULT_RBT;
				Parameters defaultClipParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "DEFAULT_CLIP", null);
				copyTune = rbtCacheManager.getClip(Integer.parseInt(defaultClipParam.getValue()), browsingLanguage);
				toneID = copyTune.getClipId();
				toneName = copyTune.getClipName();
				previewFile = copyTune.getClipPreviewWavFile();
			}
			else
			{
				result = SUCCESS;
				String[] values = response.split(":");
				String subscriberFile = values[0];
				categoryID = Integer.parseInt(values[1].trim());
				copyTune = rbtCacheManager.getClipByRbtWavFileName(subscriberFile, browsingLanguage);
				toneID = copyTune.getClipId();
				toneName = copyTune.getClipName();
				previewFile = copyTune.getClipPreviewWavFile();

				Parameters blockedClipIDsParam = parametersCacheManager.getParameter(iRBTConstant.GATHERER, "COPY_BLOCKED_CLIP_IDS", null);
				if (blockedClipIDsParam != null)
				{
					List<String> blockedClipIDsList = Arrays.asList(blockedClipIDsParam.getValue().trim().split(","));
					if (blockedClipIDsList.contains(String.valueOf(toneID)))
						result = NOT_ALLOWED;
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
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

		logger.info("RBT:: webServiceCopyData: " + webServiceCopyData);

		WebServiceCopyData[] webServiceCopyDatas = {webServiceCopyData};
		return webServiceCopyDatas;
	}

	@Override
	protected String canBeGifted(String subscriberID, String callerID,
			String contentID) {
		SubscriberDetail subscriberDetail = RbtServicesMgr
				.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
		if (subscriberDetail != null && subscriberDetail.getCircleID() != null) {
			return VALID;
		} else {
			Parameters nonOMPrefixParam = parametersCacheManager.getParameter(iRBTConstant.GATHERER, "NON_ONMOBILE_PREFIX", null);
			String telPrefix = rbtDBManager.subID(callerID).substring(0, 4);
			if (nonOMPrefixParam != null && nonOMPrefixParam.getValue().indexOf(telPrefix) >= 0) {
				return VALID;
			}
		}
		return INVALID;
	}
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getValidateNumberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getValidateNumberElement(Document document, WebServiceContext task)
	{
		Element element = super.getValidateNumberElement(document, task);

		String action = task.getString(param_action);
		String subscriberID = task.getString(param_subscriberID);
		String number = task.getString(param_number);

		Text responseText = (Text) element.getFirstChild();
		String response = responseText.getNodeValue();

		if (action.equalsIgnoreCase(action_scratchCard))
		{
			String scratchCardPeriod = task.getString(param_period);
			Date scratchCardEndDate = null;

			if (scratchCardPeriod != null)
			{
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_YEAR, Utility.getValidityPeriod(scratchCardPeriod));
				scratchCardEndDate = calendar.getTime();
			}

			response = INVALID;

			String context = "scratchcard";
			if (task.containsKey(param_context))
				context = task.getString(param_context);

			TransData transData = rbtDBManager.getTransData(number, context);

			if (transData != null)
			{
				Date transDate = transData.transDate();
				if (transDate == null || transDate.after(new Date()))
				{
					if (transDate != null)
						scratchCardEndDate = null;

					boolean checkMaxCount = true;
					if (transDate != null || scratchCardEndDate != null)
						checkMaxCount = false;

					Parameters maxAccessParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "MAX_SCRATCHCARD_ACCESS", "3");
					int maxAccess = Integer.parseInt(maxAccessParam.getValue().trim());

					Parameters maxDownloadsParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "MAX_SCRATCHCARD_DOWNLOADS", "30");
					int maxDownloads = Integer.parseInt(maxDownloadsParam.getValue().trim());

					int accessCount = 0;
					int downloadCount = 0;

					String transSubscriberID = transData.subscriberID();
					if (transSubscriberID == null)
					{
						response = VALID;
					}
					else
					{
						String transCount = transData.accessCount();
						if (transCount != null)
						{
							String[] transCounts = transCount.split(":");
							if (transCounts.length > 0)
								accessCount = Integer.parseInt(transCounts[0]);
							if (transCounts.length > 1)
								downloadCount = Integer.parseInt(transCounts[1]);
						}

						if (transSubscriberID.equals(subscriberID))
						{
							if (downloadCount >= maxDownloads)
								response = OVERLIMIT;
							else
								response = VALID;
						}
						else
							response = ALREADY_USED;
					}

					if (response.equalsIgnoreCase(VALID))
					{
						if (checkMaxCount && accessCount >= maxAccess - 1)
							rbtDBManager.removeTransData(number, context);
						else
						{
							accessCount++;
							rbtDBManager.updateTransData(number, context, subscriberID, scratchCardEndDate, accessCount + ":" + downloadCount);
						}
					}
				}
			}

			responseText.setNodeValue(response);
		}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getCallDetailsElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber)
	 */
	@Override
	protected Element getCallDetailsElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServiceSubscriber, Subscriber subscriber)
	{
		Element element = AirtelXMLElementGenerator.generateCallDetailsElement(document, task, webServiceSubscriber);
		//Element offersElement = getOfferElement(document, task);
		//element.appendChild(offersElement);
		return element;
	}
}
