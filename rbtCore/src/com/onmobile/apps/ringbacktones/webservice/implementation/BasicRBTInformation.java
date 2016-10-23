/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: balachandar.p $
 * $Id: BasicRBTInformation.java,v 1.291 2015/05/21 13:23:12 balachandar.p Exp $
 * $Revision: 1.291 $
 * $Date: 2015/05/21 13:23:12 $
 */
package com.onmobile.apps.ringbacktones.webservice.implementation;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.common.QRCodeGenerator;
import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.FeedStatus;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.RDCGroupMembers;
import com.onmobile.apps.ringbacktones.content.RDCGroups;
import com.onmobile.apps.ringbacktones.content.Retailer;
import com.onmobile.apps.ringbacktones.content.Scratchcard;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.PendingConfirmationsReminderTableImpl;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTBulkUploadTaskDAO;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.genericcache.BulkPromoSMSCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ChargeClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SubscriptionClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.PredefinedGroup;
import com.onmobile.apps.ringbacktones.genericcache.beans.RbtSupport;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.common.MNPConstants;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.webservice.RBTInformation;
import com.onmobile.apps.ringbacktones.webservice.actions.WriteCDRLog;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SMSHistory;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPack;
import com.onmobile.apps.ringbacktones.webservice.client.beans.TopLikeSong;
import com.onmobile.apps.ringbacktones.webservice.client.beans.TopLikeSubscriberSong;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.SocialRBTEventLogger;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceCopyData;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceGift;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceGroup;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceGroupMember;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
import com.onmobile.apps.ringbacktones.webservice.implementation.unitel.UnitelXMLElementGenerator;
import com.onmobile.apps.ringbacktones.webservice.implementation.util.RBTProtocol;
import com.onmobile.common.exception.OnMobileException;

/**
 * @author vinayasimha.patil
 * 
 */
public class BasicRBTInformation implements RBTInformation, WebServiceConstants {
	private static Logger logger = Logger.getLogger(BasicRBTInformation.class);
	private static Logger cdr_logger = Logger.getLogger("CDR_LOGGER");

	protected DocumentBuilder documentBuilder;

	protected RBTDBManager rbtDBManager = null;
	protected RBTCacheManager rbtCacheManager = null;
	protected ParametersCacheManager parametersCacheManager = null;
	private List<String> m_tnbSubClassesList = new ArrayList<String>();
	protected List<String> migratedSubClassList = null;
    protected List<String> migratedOIModesList = null;
	/**
	 * @throws ParserConfigurationException
	 *  
	 */
	public BasicRBTInformation() throws ParserConfigurationException {
		documentBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
		parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		String tnbSubscriptionClasses = RBTParametersUtils.getParamAsString("COMMON",
				"TNB_SUBSCRIPTION_CLASSES", "");
		m_tnbSubClassesList = Arrays.asList(tnbSubscriptionClasses
				.toUpperCase().split(","));
		String migratedSubClass = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE,
				"MIGRATED_USER_SUBSCRIPTION_CLASSES", null);
		if (migratedSubClass != null) {
			migratedSubClassList = Arrays.asList(migratedSubClass.split(","));
		}
		String migratedOIModes = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE,
				"MIGRATED_USER_OI_MODES", "");

		migratedOIModesList = Arrays.asList(migratedOIModes.split(","));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getRBTInformationDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getRBTInformationDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		Element responseElem = Utility.getResponseElement(document, SUCCESS);
		element.appendChild(responseElem);

		Element subDetailsElem = document.createElement(SUBSCRIBER_DETAILS);
		element.appendChild(subDetailsElem);

		String subscriberID = task.getString(param_subscriberID);

		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		task.put(param_subscriber, subscriber);

		WebServiceSubscriber webServiceSubscriber = getWebServiceSubscriberObject(
				task, subscriber);

		Element subscriberElem = getSubscriberElement(document, task,
				webServiceSubscriber, subscriber);
		subDetailsElem.appendChild(subscriberElem);

		updateAccessCount(task, subscriber);

		if (!webServiceSubscriber.isCanAllow()
				|| !webServiceSubscriber.isValidPrefix())
			return document;

		String subscriberStatus = webServiceSubscriber.getStatus();
		if (subscriberStatus.equalsIgnoreCase(DEACT_PENDING)
				|| subscriberStatus.equalsIgnoreCase(SUSPENDED)
				|| subscriberStatus.equalsIgnoreCase(ERROR))
			return document;

		ViralSMSTable[] gifts = null;
		if (!subscriberStatus.equalsIgnoreCase(ACT_PENDING)
				&& !subscriberStatus.equalsIgnoreCase(GRACE)) {
			String smsTypeStr = RBTParametersUtils.getParamAsString("COMMON", "GIFT_INBOX_TO_DISPLAY_BEFORE_CHARGING", null);
			if(smsTypeStr == null) {
				smsTypeStr = RBTParametersUtils.getParamAsString("COMMON", "GIFT_INBOX_SMS_TYPE_" + task.getString(param_mode), null);
			}
			if (smsTypeStr != null) {
				String[] smsTypes = smsTypeStr.split("\\,");
				gifts = rbtDBManager
						.getViralSMSesByTypes(subscriberID, smsTypes);
			}
			else {
				gifts = rbtDBManager.getViralSMSesByType(subscriberID, "GIFTED");
			}
		}
			

		WebServiceGift[] webServiceGifts = getWebServiceGiftObjects(task, gifts);
		Element giftInboxElem = getSubscriberGiftInboxElement(document, task,
				webServiceGifts, gifts);
		subDetailsElem.appendChild(giftInboxElem);

		if (subscriberStatus.equalsIgnoreCase(NEW_USER)
				|| subscriberStatus.equalsIgnoreCase(DEACTIVE)) {
			Element libraryElem = document.createElement(LIBRARY);
			subDetailsElem.appendChild(libraryElem);
		} else {
			String mode = task.getString(param_mode);
			if(mode != null && mode.equalsIgnoreCase("CCC")) {
				task.put("returnDctRecord", "true");
			}
			Element libraryElem = getSubscriberLibraryElement(document, task);
			subDetailsElem.appendChild(libraryElem);
		}

		Element callDetailsElem = getCallDetailsElement(document, task,
				webServiceSubscriber, subscriber);
		element.appendChild(callDetailsElem);

		Element subscriberPackEle = getSubscriberPacks(document, task);
		element.appendChild(subscriberPackEle);

		if (task.getString(param_chrgDetailsReq) != null && task.getString(param_chrgDetailsReq).equalsIgnoreCase("TRUE")) {
			Element subClassEle = getRBTInfoSubClassElement(document, task, webServiceSubscriber);
			element.appendChild(subClassEle);

			Element chargeClassEle = getRBTInfoChargeClassElement(document, task, webServiceSubscriber);
			element.appendChild(chargeClassEle);
		}

		return document;
	}

	private Element getRBTInfoSubClassElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber) {
		String subClassName = null;
		String subscriberStatus = webServiceSubscriber.getStatus(); 
		if (subscriberStatus.equalsIgnoreCase(NEW_USER)
				|| subscriberStatus.equalsIgnoreCase(DEACTIVE)) {
			subClassName = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE, RBT_INFO_DEFAULT_SUBSCRIPTION_CLASS, "DEFAULT");
		} else {
			subClassName = webServiceSubscriber.getSubscriptionClass();
		}
		logger.debug("subClassName: " + subClassName);
		task.put(param_name, subClassName);
		Element subClassElement = getSubscriptionClassesElement(document, task);
		return subClassElement;
	}

	private Element getRBTInfoChargeClassElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber) {
		String chargeClassName = null;
		String subscriberStatus = webServiceSubscriber.getStatus(); 
		if (subscriberStatus.equalsIgnoreCase(NEW_USER)
				|| subscriberStatus.equalsIgnoreCase(DEACTIVE)) {
			chargeClassName = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE, RBT_INFO_DEFAULT_CHARGE_CLASS, "DEFAULT");
		} else {
			SelectionRequest selectionRequest = new SelectionRequest(webServiceSubscriber.getSubscriberID());
			com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeClass = RBTClient.getInstance().getNextChargeClass(selectionRequest);
			if (chargeClass != null) {
				chargeClassName = chargeClass.getChargeClass(); 
			}
		}
		logger.debug("chargeClassName: " + chargeClassName);
		task.put(param_name, chargeClassName);
		Element chargeClassElement = getChargeClassesElement(document, task);
		return chargeClassElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getSpecificRBTInformationDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getSpecificRBTInformationDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		Element responseElem = Utility.getResponseElement(document, SUCCESS);
		element.appendChild(responseElem);

		String subscriberID = task.getString(param_subscriberID);
		String protocolNo = task.getString(param_protocolNo);
		String mode = task.getString(param_mode);
		String userID = task.getString(param_userId);
		int modeVal = SRBTUtility.getSocialRBTMode(mode);

		String info = task.getString(param_info);
		String[] infoArray = info.split(",");
		for (String information : infoArray) {
			information = information.trim();
			if (information.equalsIgnoreCase(SUBSCRIBER)) {
				Subscriber subscriber = null;
				if (null != protocolNo) {
					RBTProtocol rbtProtocol = rbtDBManager
							.getSubscriberByProtocolId(protocolNo, subscriberID);
					if (null != rbtProtocol) {
						subscriberID = rbtProtocol.getSubscriberId();
						task.put(param_protocolNo,
								String.valueOf(rbtProtocol.getProtocolId()));
						task.put(param_protocolStaticText,
								rbtProtocol.getStaticText());
						task.put(param_subscriberID, subscriberID);
						subscriber = rbtDBManager.getSubscriber(subscriberID);
					} else {
						logger.warn("Subscriber not found by protocolId: "
								+ protocolNo);
					}
				} else {
					subscriber = rbtDBManager.getSubscriber(subscriberID);
				}
				WebServiceSubscriber webServicesubscriber = getWebServiceSubscriberObject(
						task, subscriber);

				Element subscriberElem = getSubscriberElement(document, task,
						webServicesubscriber, subscriber);
				if (subscriberElem != null) {
					element.appendChild(subscriberElem);
					if (mode != null && mode.indexOf("social_") != -1) {
						String canAllowStr = subscriberElem
								.getAttribute(CAN_ALLOW);
						if (canAllowStr != null
								&& canAllowStr.equalsIgnoreCase(YES)) {
							SocialRBTEventLogger.userValidationEventLog(userID,
									subscriberID, mode, modeVal, "Y");
						} else if (canAllowStr != null
								&& canAllowStr.equalsIgnoreCase(NO)) {
							SocialRBTEventLogger.userValidationEventLog(userID,
									subscriberID, mode, modeVal, "N");
						}
					}
				}
			}
			if (information.equalsIgnoreCase(GIFT_INBOX)) {
				ViralSMSTable[] gifts = null;
				String smsTypeStr = RBTParametersUtils.getParamAsString("COMMON", "GIFT_INBOX_TO_DISPLAY_BEFORE_CHARGING", null);
				if(smsTypeStr == null) {
					smsTypeStr = RBTParametersUtils.getParamAsString("COMMON", "GIFT_INBOX_SMS_TYPE_" + task.getString(param_mode), null);
				}
				if (smsTypeStr != null) {
					String[] smsTypes = smsTypeStr.split("\\,");
					gifts = rbtDBManager
							.getViralSMSesByTypes(subscriberID, smsTypes);
				}
				else {
					gifts = rbtDBManager.getViralSMSesByType(
							subscriberID, "GIFTED");
				}
							
				WebServiceGift[] webServiceGifts = getWebServiceGiftObjects(
						task, gifts);

				Element giftInboxElem = getSubscriberGiftInboxElement(document,
						task, webServiceGifts, gifts);
				if (giftInboxElem != null)
					element.appendChild(giftInboxElem);
			}
			if (information.equalsIgnoreCase(GIFT_OUTBOX)) {
				String[] smsTypes = { "GIFTED", "ACCEPT_ACK", "REJECT_ACK",
						"ACCEPT_PRE", "GIFTCOPY_SUCCESS" };
				String smsTypeStr = RBTParametersUtils.getParamAsString("COMMON", "GIFT_OUTBOX_SMS_TYPE_" + task.getString(param_mode), null);
				if(smsTypeStr != null) {
					smsTypes = smsTypeStr.split("\\,");
				}
				
				String giftOutBoxConfig = RBTParametersUtils.getParamAsString("COMMON", "RETURN_GIFT_OUTBOX_ONLY_MODES", null);
				String giftOutBoxMode = null;
				if(giftOutBoxConfig != null && mode != null) {
					if(Arrays.asList(giftOutBoxConfig.split(",")).contains(mode)) {
						giftOutBoxMode = mode;
					}
				}
				
				ViralSMSTable[] gifts = rbtDBManager
						.getViralSMSByTypesForSubscriber(subscriberID, smsTypes, giftOutBoxMode);
				WebServiceGift[] webServiceGifts = getWebServiceGiftObjects(
						task, gifts);

				Element giftOutboxElem = getSubscriberGiftOutboxElement(
						document, task, webServiceGifts, gifts);
				if (giftOutboxElem != null)
					element.appendChild(giftOutboxElem);
			}
			if (information.equalsIgnoreCase(LIBRARY)) {
				// downloads + selections
				
				//Tef spain RBT-11113 if subscriber is not active, then should not should library
				boolean showLibraryForInactiveUser = parametersCacheManager.getParameter(iRBTConstant.COMMON, "SHOW_LIBRARY_FOR_INACTIVE_USER", "TRUE").getValue().equalsIgnoreCase("TRUE");
				Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
				if(!showLibraryForInactiveUser && !(subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_ACTIVATED))) {
					Element libraryElem = document.createElement(LIBRARY);
					element.appendChild(libraryElem);
				}
				else {
					Element libraryElem = getSubscriberLibraryElement(document,
							task);
					if (libraryElem != null)
						element.appendChild(libraryElem);
				}
			}
			if (information.equalsIgnoreCase(MI_PLAYLIST)) {
				Element miPlaylistElm = document.createElement(MI_PLAYLIST);

				SubscriberDownloads[] downloads = rbtDBManager.getSubscriberDownloadsByDownloadStatus(subscriberID,"t");

				WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(
						task, downloads);
				if(webServiceSubscriberDownloads != null) {
						Element contentElem = getSubscriberMIPlayListElement(
										document, task, webServiceSubscriberDownloads);
						miPlaylistElm.appendChild(contentElem);
				}
				element.appendChild(miPlaylistElm);
			}
			if (information.equalsIgnoreCase(SETTINGS)) {
				
				// only selections
				
				Element libraryElem = document.createElement(LIBRARY);
				String selstatus=null;
				String rbtType=null;
				String id=null;
				SubscriberStatus[] settings = null;
				if(task.containsKey(param_selstatus))
				{  
					logger.info("-->Inside if task contains selstatus");
					if(task.containsKey(param_type))
					rbtType=task.getString(param_type);
					if(task.containsKey(param_calledNo))
					id=task.getString(param_calledNo);
					selstatus=task.getString(param_selstatus);
					settings = rbtDBManager.getAllActiveSubscriberSettingsbyStatus(subscriberID, rbtType,selstatus,id);
					
				}
				else
				{
					logger.info("-->Inside else task doesnot contains selstatus");
					settings = rbtDBManager
							.getAllActiveSubscriberSettings(subscriberID);
				}

				settings = getActiveSettingsTobeDisplayed(settings);
				
				// Added for RBT 2.0 Skipping Paid selection for free user
				settings = getSettingsForFreeUser(settings, subscriberID);
				
				mode = "VP";
				if (task.containsKey(param_mode))
					mode = task.getString(param_mode);

				// If condition added for DTOC RBT-19125 issue. logics inside the method is not related to DTOC
				if(!task.containsKey(param_selstatus)){
					String visibleStatusTypes = task.getString(param_status);
					settings = DataUtils.getRecentActiveSettings(rbtDBManager,
							settings, mode, visibleStatusTypes);
				}
				
				WebServiceSubscriberSetting[] webServiceSubscriberSettings = getWebServiceSubscriberSettingObjects(
						task, settings);

				Element settingsElem = getSubscriberSettingsElement(document,
						task, webServiceSubscriberSettings, settings);
				libraryElem.appendChild(settingsElem);

				element.appendChild(libraryElem);
			}
			if (information.equalsIgnoreCase(DOWNLOADS)) {
				// only dowloads
				Element libraryElem = document.createElement(LIBRARY);

				SubscriberDownloads[] downloads = rbtDBManager
							.getActiveSubscriberDownloads(subscriberID);
				
				downloads = getActiveDownloadsToBedisplayed(downloads);
				
				WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(
						task, downloads);
				Element downloadsElem = getSubscriberDownloadsElement(document,
						task, webServiceSubscriberDownloads, downloads);
				libraryElem.appendChild(downloadsElem);

				element.appendChild(libraryElem);
			}
			if (information.equalsIgnoreCase(LIBRARY_HISTORY)) {
				Element libraryHistoryElem = getSubscriberLibraryHistoryElement(
						document, task);
				if (libraryHistoryElem != null)
					element.appendChild(libraryHistoryElem);
			}
			if (information.equalsIgnoreCase(REFUNDABLE_SELECTIONS)) {
				Element refundableSelectionsElem = getSubscriberRefundableSelectionsElement(
						document, task);
				if (refundableSelectionsElem != null)
					element.appendChild(refundableSelectionsElem);
			}
			if (information.equalsIgnoreCase(BOOKMARKS)) {
				SubscriberDownloads[] bookmarks = rbtDBManager
						.getSubscriberBookMarks(subscriberID);
				WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = getWebServiceSubscriberBookMarkObjects(
						task, bookmarks);
				Element bookMarksElem = getSubscriberBookMarksElement(document,
						task, webServiceSubscriberBookMarks, bookmarks);
				if (bookMarksElem != null)
					element.appendChild(bookMarksElem);
			}
			if (information.equalsIgnoreCase(GROUP_DETAILS)) {
				Element groupDetailsElem = getSubscriberGroupDetailsElement(
						document, task);
				if (groupDetailsElem != null)
					element.appendChild(groupDetailsElem);
			}
			if (information.equalsIgnoreCase(SMS_HISTORY)) {
				Element smsHistoryElem = getSubscriberSMSHistoryElement(
						document, task);
				if (smsHistoryElem != null)
					element.appendChild(smsHistoryElem);
			}
			
			// RBT-8199:Need to include all SMS logs in CCC GUI as part of SMS logs
			if (information.equalsIgnoreCase(SMS_HISTORY_FROM_UMP)) {
				Element smsHistoryFromUMPElem = getSMSHistoryFromUMPElement(
						document, task);
				if (smsHistoryFromUMPElem != null)
					element.appendChild(smsHistoryFromUMPElem);
			}
			if (information.equalsIgnoreCase(TRANSACTION_HISTORY)) {
				Element chargeHistoryElem = getSubscriberTransactionHistoryElement(
						document, task);
				if (chargeHistoryElem != null)
					element.appendChild(chargeHistoryElem);
			}
			if (information.equalsIgnoreCase(WC_HISTORY)) {
				Element wcHistoryElem = getSubscriberWCHistoryElement(
						document, task);
				if (wcHistoryElem != null)
					element.appendChild(wcHistoryElem);
			}

			if (information.equalsIgnoreCase(CALL_DETAILS)) {
				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				WebServiceSubscriber webServicesubscriber = getWebServiceSubscriberObject(
						task, subscriber);

				Element callDetailsElem = getCallDetailsElement(document, task,
						webServicesubscriber, subscriber);
				if (callDetailsElem != null)
					element.appendChild(callDetailsElem);
			}
			if (information.equalsIgnoreCase(SUBSCRIBER_PROMO)) {
				Element subscriberPromoElem = getSubscriberPromoElement(
						document, task);
				if (subscriberPromoElem != null)
					element.appendChild(subscriberPromoElem);
			}
			if (information.equalsIgnoreCase(OPERATOR_USER_INFO)) {
				Element operatorUserInfoElem = getOperatorUserInfoElement(
						document, task);
				if (operatorUserInfoElem != null)
					element.appendChild(operatorUserInfoElem);
			}
			if (information.equalsIgnoreCase(BI_DOWNLOAD_HISTORY_INFO)) {
				Element operatorUserInfoElem = getDownloadHistoryFromBI(
						document, task);
				if (operatorUserInfoElem != null)
					element.appendChild(operatorUserInfoElem);
			}
			// JIRA-RBT-6194:Search based on songs in Query Gallery API 
			if (information.equalsIgnoreCase(SEARCH_GALLERY)) {
				Element downloadElement = document.createElement(DOWNLOADS);
				SubscriberDownloads subscriberDownload = getDownloadByPromoID(task);
				if(subscriberDownload != null) {
				SubscriberDownloads[] downloads =  new SubscriberDownloads[1];
				downloads[0] = subscriberDownload;
				WebServiceSubscriberDownload[] webServiceSubscriberDownloads = getWebServiceSubscriberDownloadObjects(
						task, downloads);
					if (webServiceSubscriberDownloads != null && webServiceSubscriberDownloads.length > 0 && webServiceSubscriberDownloads[0] != null) {
						Element contentsElem = document.createElement(CONTENTS);
						downloadElement.appendChild(contentsElem);	
						Element contentElem = getSubscriberDownloadContentElement(
									document, task, webServiceSubscriberDownloads[0]);
							contentsElem.appendChild(contentElem);
					} 
				} 
				 element.appendChild(downloadElement);
			}
			if (information.equalsIgnoreCase(SUBSCRIBER_PACKS)) {
				Element operatorUserInfoElem = getSubscriberPacks(document,
						task);
				if (operatorUserInfoElem != null)
					element.appendChild(operatorUserInfoElem);
				
			}
			
			if(information.equalsIgnoreCase(CONSENT_EXPIRED)){
				Element consentExpiredInfoElem = getConsentExpiredSubscriberInfo(document,
						task);
				if(consentExpiredInfoElem!=null)
					element.appendChild(consentExpiredInfoElem);
			}
			
			if(information.equalsIgnoreCase(CONSENT_PENDING)){
				Element consentPendingInfoElem = getConsentPendingRecord(document,
						task);
				if(consentPendingInfoElem!=null)
					element.appendChild(consentPendingInfoElem);
			}
			
			if(information.equalsIgnoreCase(CONSENT_SELECTION_UNPROCESSED_RECORDS)){
				Element consentSelectionUnprocessedInfoElem = getConSelUnprocessedRecordElement(document,
						task);
				if(consentSelectionUnprocessedInfoElem!=null)
					element.appendChild(consentSelectionUnprocessedInfoElem);
				
			}//RBT-14671 - # like
			if (information.equalsIgnoreCase(TOP_LIKE_SONG)) {
				Element contentsForTopLikeSong = getTopLikeSongDetails(
						document, task);
				element.appendChild(contentsForTopLikeSong);
			}
			if (information.equalsIgnoreCase(TOP_LIKE_SUBSCRIBER_SONG)) {
				Element contentsForTopLikeSong = getTopLikeSubscriberSongDetails(
						document, task);
				element.appendChild(contentsForTopLikeSong);
			}
			
		}

		return document;
	}

	//RBT-14671 - # like
	public Element getTopLikeSongDetails(Document document,
			WebServiceContext task) {
		List<TopLikeSong> topLikeSongLst;
		Element contentsElem = document.createElement(CONTENTS);
		String strLimit = task.getString("limit");
		int limit = RBTParametersUtils.getParamAsInt("COMMON",
				"TOP_LIKE_SONG_LIMIT", 20);
		Clip clip = null;
		if (strLimit != null && !strLimit.trim().isEmpty()) {
			limit = (Integer.parseInt(strLimit) > limit || Integer
					.parseInt(strLimit) <= 0) ? limit : Integer
					.parseInt(strLimit);
		}
		// songname= artist= vcode= promoid= clipID= wavfilename= count=
		topLikeSongLst = rbtDBManager.getLikedSongDetails(limit);
		logger.info("topLikeSongLst size: "+topLikeSongLst.size());
		if (topLikeSongLst != null && !topLikeSongLst.isEmpty()) {
			for (TopLikeSong topLikeSong : topLikeSongLst) {
				clip = rbtCacheManager.getClip(topLikeSong.getClipId());
				Element contentElement = createLikeContentTags(document, clip);
				contentElement.setAttribute("count",
						String.valueOf(topLikeSong.getCount()));
				contentsElem.appendChild(contentElement);
			}
		}
		return contentsElem;
	}
	//RBT-14671 - # like
	public Element getTopLikeSubscriberSongDetails(Document document,
			WebServiceContext task) {
		List<TopLikeSubscriberSong> topLikeSongLst;
		Element contentsElem = document.createElement(CONTENTS);
		String strLimit = task.getString("limit");
		int limit = RBTParametersUtils.getParamAsInt("COMMON",
				"TOP_LIKE_SUBSCRIBER_SONG_LIMIT", 20);
		Clip clip = null;
		String subscriberID = task.getString(param_subscriberID);
		if (strLimit != null && !strLimit.trim().isEmpty()) {
			limit = (Integer.parseInt(strLimit) > limit || Integer
					.parseInt(strLimit) <= 0) ? limit : Integer
					.parseInt(strLimit);
		}
		// songname= artist= vcode= promoid= clipID= wavfilename= count=
		topLikeSongLst = rbtDBManager.getLikedSubscriberSongDetails(limit,subscriberID);
		logger.info("topLikeSongLst size: "+topLikeSongLst.size());
		if (topLikeSongLst != null && !topLikeSongLst.isEmpty()) {
			for (TopLikeSubscriberSong topLikeSong : topLikeSongLst) {
				clip = rbtCacheManager.getClip(topLikeSong.getClipId());
				Element contentElement = createLikeContentTags(document, clip);
				contentElement.setAttribute("count",
						String.valueOf(topLikeSong.getCount()));
				contentsElem.appendChild(contentElement);
			}
		}
		return contentsElem;
	}

	/**
	 * @param document
	 * @param clip
	 * @return
	 */
	protected Element createLikeContentTags(Document document, Clip clip) {
		Element contentElement = document.createElement(CONTENT);
		if (clip != null) {
			contentElement.setAttribute("songname", clip.getAlbum());
			contentElement.setAttribute("artist", clip.getAlbum());
			contentElement.setAttribute("vcode", clip.getClipRbtWavFile()
					.replaceAll("rbt_", "").replaceAll("_rbt", ""));
			contentElement.setAttribute("promoid",
					String.valueOf(clip.getClipPromoId()));
			contentElement.setAttribute("clipId",
					String.valueOf(clip.getClipId()));
			contentElement
					.setAttribute("wavfilename", clip.getClipRbtWavFile());
		}
		return contentElement;
	}

	public Element getConSelUnprocessedRecordElement(Document document,
			WebServiceContext task) {
		// Document document = documentBuilder.newDocument();
		String subscriberID = task.getString(param_subscriberID);
		String requestFromTime = task.getString("requestFromTime");
		String requestToTime = task.getString("requestToTime");
		List<DoubleConfirmationRequestBean> consentPendingRecordListByMsisdnNType = null;
		try {
			consentPendingRecordListByMsisdnNType = rbtDBManager
					.getPendingRequestsByMsisdnNTypeNRequestTime(subscriberID,
							"SEL", requestFromTime, requestToTime);
		} catch (OnMobileException e) {

			logger.info("exceptin while getting record for consent and processed record");
		}
		Element consents  = document.createElement("consents");
		if (consentPendingRecordListByMsisdnNType != null
				&& consentPendingRecordListByMsisdnNType.size() > 0) {
			for (DoubleConfirmationRequestBean dbReqBean : consentPendingRecordListByMsisdnNType) {
				String subId = dbReqBean.getSubscriberID();
				String callerId = dbReqBean.getCallerID();
				Integer categoryId = dbReqBean.getCategoryID();
				String mode = dbReqBean.getMode();
				Date startTime = dbReqBean.getStartTime();
				Date endTime = dbReqBean.getEndTime();
				Integer status = dbReqBean.getStatus();
				String classType = dbReqBean.getClassType();
				Integer cosId = dbReqBean.getCosId();
				Integer packCosId = dbReqBean.getPackCosID();
				Integer clipId = dbReqBean.getClipID();
				String selInterval = dbReqBean.getSelInterval();
				Integer fromTime = dbReqBean.getFromTime();
				Integer toTime = dbReqBean.getToTime();
				String selectionInfo = dbReqBean.getSelectionInfo();
				Integer selType = dbReqBean.getSelType();
				String inLoop = dbReqBean.getInLoop();
				String purchaseType = dbReqBean.getPurchaseType();
				String useUiChargeClass = dbReqBean.getUseUIChargeClass();
				String categoryType = dbReqBean.getCategoryType();
				String profileHrs = dbReqBean.getProfileHrs();
				String prepaidYes = dbReqBean.getPrepaidYes();
				String feedType = dbReqBean.getFeedType();
				String wavFileName = dbReqBean.getWavFileName();
				Integer rbtType = dbReqBean.getRbtType();
				String circleId = dbReqBean.getCircleId();
				String language = dbReqBean.getLanguage();
				Date requestTime = dbReqBean.getRequestTime();
				String extraInfo = dbReqBean.getExtraInfo();
				String requestType = dbReqBean.getRequestType();
				Integer consentStatus = dbReqBean.getConsentStatus();
				String transId = dbReqBean.getTransId();

				Element consentElement = document.createElement("consent");

				if (subId != null)
					consentElement.setAttribute("subId", subId);
				if (callerId != null)
					consentElement.setAttribute("callerId", callerId);
				if (categoryId != null)
					consentElement.setAttribute("categoryId",
							String.valueOf(categoryId));
				if (mode != null)
					consentElement.setAttribute("mode", mode);
				if (startTime != null)
					consentElement.setAttribute("startTime",
							String.valueOf(startTime));
				if (endTime != null)
					consentElement.setAttribute("endTime",
							String.valueOf(endTime));
				if (status != null)
					consentElement.setAttribute("status",
							String.valueOf(status));
				if (classType != null)
					consentElement.setAttribute("classType", classType);
				if (cosId != null)
					consentElement.setAttribute("cosId", String.valueOf(cosId));
				if (packCosId != null)
					consentElement.setAttribute("packCosId",
							String.valueOf(packCosId));
				if (clipId != null)
					consentElement.setAttribute("clipId",
							String.valueOf(clipId));
				if (selInterval != null)
					consentElement.setAttribute("selInterval", selInterval);
				if (fromTime != null)
					consentElement.setAttribute("fromTime",
							String.valueOf(fromTime));
				if (toTime != null)
					consentElement.setAttribute("toTime",
							String.valueOf(toTime));
				if (selectionInfo != null)
					consentElement.setAttribute("selectionInfo", selectionInfo);
				if (selType != null)
					consentElement.setAttribute("selType",
							String.valueOf(selType));
				if (inLoop != null)
					consentElement.setAttribute("inLoop", inLoop);
				if (purchaseType != null)
					consentElement.setAttribute("purchaseType", purchaseType);
				if (useUiChargeClass != null)
					consentElement.setAttribute("useUiChargeClass",
							useUiChargeClass);
				if (categoryType != null)
					consentElement.setAttribute("categoryType", categoryType);
				if (profileHrs != null)
					consentElement.setAttribute("profileHrs", profileHrs);
				if (prepaidYes != null)
					consentElement.setAttribute("prepaidYes", prepaidYes);
				if (feedType != null)
					consentElement.setAttribute("feedType", feedType);
				if (wavFileName != null)
					consentElement.setAttribute("wavFileName", wavFileName);
				if (rbtType != null)
					consentElement.setAttribute("rbtType",
							String.valueOf(rbtType));
				if (circleId != null)
					consentElement.setAttribute("circleId",
							String.valueOf(circleId));
				if (language != null)
					consentElement.setAttribute("language", language);
				if (requestTime != null)
					consentElement.setAttribute("requestTime",
							String.valueOf(requestTime));
				if (extraInfo != null)
					consentElement.setAttribute("extraInfo", extraInfo);
				if (requestType != null)
					consentElement.setAttribute("requestType", requestType);
				if (consentStatus != null)
					consentElement.setAttribute("consentStatus",
							String.valueOf(consentStatus));
				if (transId != null)
					consentElement.setAttribute("transId", transId);

				consents.appendChild(consentElement);
			}
			
		}
		return consents;
	}
	
	@Override
	public Document getSubscriptionPreConsentResponseDocument(WebServiceContext task){
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);
		if (response.equalsIgnoreCase(SUCCESS)||(response.indexOf(SUCCESS)!=-1)) {
			String msisdn = task.getString(param_subscriberID);
		    String mode = 	task.getString(param_mode);
		    String sub_class = task.getString(srvkey);
		    String trans_id = task.getString(param_transID);
		    Element consentElem = document.createElement(param_consent);
		    consentElem.setAttribute(param_msisdn, msisdn);
		    consentElem.setAttribute(param_mode, mode);
		    consentElem.setAttribute("sub_class", sub_class);
		    consentElem.setAttribute("trans_id", trans_id);
		    element.appendChild(consentElem);
		}
		
		return document;
	}

	@Override
	public Document getSelIntegrationPreConsentResponseDocument(WebServiceContext task){
		String response = task.getString(param_response);
		String msisdn = task.getString(param_subscriberID);
		String mode = 	task.getString(param_mode);
		String clip_id = task.getString(param_clipID);
		String promo_id = task.getString(WebServiceConstants.param_promoID);
		String cat_id = task.getString(WebServiceConstants.param_categoryID);
		String trans_id = task.getString("CONSENTID");
		String chargeclass = task.getString("CONSENTCLASSTYPE");
		String sub_class = task.getString("CONSENTSUBCLASS");
		String callerId = task.getString(param_callerID);
		String pricePoint = task.getString("price");
		String priceValidity = task.getString("priceValidity");
        logger.info("getSelIntegrationPreConsentResponseDocument task ::= "+task);
        Document document = documentBuilder.newDocument();
        Element element = document.createElement(RBT);
        document.appendChild(element);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (response.indexOf(SUCCESS)!=-1 && !task.containsKey("USER_ACTIVE_SEL_CON_INT")) {
				Element consentElem = document.createElement(param_consent);
				consentElem.setAttribute(param_msisdn, msisdn);
				if (mode == null)
					mode = "VP";
				consentElem.setAttribute("mode", mode);
				if (sub_class != null && sub_class.length() > 0) {
					consentElem.setAttribute("sub_class", sub_class);
				}
				consentElem.setAttribute("trans_id", trans_id);
				if (clip_id != null && clip_id.length() > 0)
					consentElem.setAttribute("clip_id", clip_id);
				if (promo_id != null && promo_id.length() > 0)
					consentElem.setAttribute("promoId", promo_id);
				if (chargeclass != null && chargeclass.length() > 0)
					consentElem.setAttribute("chargeclass", chargeclass);
				if (cat_id != null && cat_id.length() > 0)
					consentElem.setAttribute("catId", cat_id);
				if(callerId != null && callerId.length() > 0)
					consentElem.setAttribute("caller_id", callerId);
				if(pricePoint != null && pricePoint.length() > 0) 
					consentElem.setAttribute("price", pricePoint);
				if(priceValidity != null && priceValidity.length() > 0) 
					consentElem.setAttribute("value", priceValidity);
				element.appendChild(consentElem);
				return document;
		}
			
		return document;
	}
	
	@Override
	public Document getSelectionPreConsentResponseDocument(WebServiceContext task){
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);
		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);
		if (response.equalsIgnoreCase(SUCCESS) && !task.containsKey(param_byPassConsent)) { 
			  String msisdn = task.getString(param_subscriberID);
			  String mode = task.getString(param_mode);
			  String sub_class = task.getString(srvkey);
			  String trans_id = task.getString(param_transID);
			  String clip_id = task.getString(param_clipID);
			  String promo_id = task.getString(WebServiceConstants.param_promoID);
			  String chargeclass = task.getString(WebServiceConstants.param_chargeClass);
			  String cat_id = task.getString(WebServiceConstants.param_categoryID);

				if (mode == null) {
					mode = "VP";
				}
				Element consentElem = document.createElement(param_consent);
				consentElem.setAttribute(param_msisdn, msisdn);
				consentElem.setAttribute("mode", mode);
				if (sub_class != null && sub_class.length() > 0) {
					consentElem.setAttribute("sub_class", sub_class);
				}
				
				boolean isTNBNewFlow = RBTParametersUtils.getParamAsString(
						"DAEMON", ConstantsTools.SUPPORT_TNB_NEW_FLOW, "FALSE")
						.equalsIgnoreCase("TRUE");

				if (isTNBNewFlow) { // Supports TNB new flow
					try {
						Subscriber subscriber = DataUtils.getSubscriber(task);
					if (subscriber != null) {
						String subClassType = subscriber.subscriptionClass();
						if (m_tnbSubClassesList.contains(subClassType)) { // Subscriber
							// is a
							// TNB
							// Subscriber
							ArrayList<String> tnbUpgradeSubClassLst = DBConfigTools.getParameter(
									"COMMON", "TNB_UPGRADE_SUBSCRIPTION_CLASSES", "", ",");
							String newSubClassType = null;

							ArrayList<String> tnbFreeChargeClass = DBConfigTools.getParameter(
									"DAEMON", "TNB_FREE_CHARGE_CLASS", ConstantsTools.FREE, ",");
							if (chargeclass == null
									|| (!tnbFreeChargeClass.contains(chargeclass) && !chargeclass
											.startsWith("FREE"))) {
								String modeAndSubClass = mode + "_" + subClassType;
								for (String tnbUpgradeSubClass : tnbUpgradeSubClassLst) {
									String[] split = tnbUpgradeSubClass.split("\\:");
									if (split == null || split.length != 2) {
										continue;
									}

									if (modeAndSubClass.equalsIgnoreCase(split[0])) {
										newSubClassType = split[1];
										logger.debug("Mode based new subscription class is found from TNB_UPGRADE_SUBSCRIPTION_CLASSES. modeAndSubClass key: "
												+ modeAndSubClass + ", newSubClassType: " + newSubClassType);
										break;
									} else {
										logger.debug("Mode based new subscription class is not found from TNB_UPGRADE_SUBSCRIPTION_CLASSES. modeAndSubClass key: "
												+ modeAndSubClass + " from " + tnbUpgradeSubClass);
									}
								}

								if (null == newSubClassType) {
									for (String tnbUpgradeSubClass : tnbUpgradeSubClassLst) {
										String[] split = tnbUpgradeSubClass.split("\\:");
										if (split == null || split.length != 2) {
											continue;
										}
										if (subClassType.equalsIgnoreCase(split[0])) {
											newSubClassType = split[1];
											break;
										}
									}
								}
								if (newSubClassType != null) {
									consentElem.setAttribute("upgrade_srv_key", newSubClassType);
								}
							}
						}
					}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}

				if (trans_id != null) {
					consentElem.setAttribute("trans_id", trans_id);
				}
				if (clip_id != null && clip_id.length() > 0)
					consentElem.setAttribute("clip_id", clip_id);
				if (promo_id != null && promo_id.length() > 0)
					consentElem.setAttribute("promoId", promo_id);
				if (chargeclass != null && chargeclass.length() > 0)
					consentElem.setAttribute("chargeclass", chargeclass);
				if (cat_id != null && cat_id.length() > 0)
					consentElem.setAttribute("catId", cat_id);


				String linkedRefId = task.getString(param_linkedRefId);
				String refId = task.getString(param_refID);
				String clipInfo = task.getString(param_clipInfo);
				if (linkedRefId != null && linkedRefId.length() > 0) {
					consentElem.setAttribute(param_linkedRefId, linkedRefId);
				}
				if (refId != null && refId.length() > 0) {
					consentElem.setAttribute(param_refID, refId);
				}
				if (clipInfo != null && clipInfo.length() > 0) {
					consentElem.setAttribute(param_clipInfo, clipInfo);
				}

				element.appendChild(consentElem);
		}
		 
		return document;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getSubscriptionResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getSubscriptionResponseDocument(WebServiceContext task) {

		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (response.equalsIgnoreCase(SUCCESS)) {
			String subscriberID = task.getString(param_subscriberID);

			String action = task.getString(param_action);
			if (action.equalsIgnoreCase(action_addSubscriberPromo)) {
				// If its add Subscriber Promo request then returning newly
				// added Subscriber Promo as response
				Element subscriberPromoElem = getSubscriberPromoElement(
						document, task);
				if (subscriberPromoElem != null)
					element.appendChild(subscriberPromoElem);
			} else if (!action.equalsIgnoreCase(action_removeSubscriberPromo)) {
				// For remove Subscriber Promo request only process response
				// will be returned
				// Else Subscriber details will be returned
				Subscriber subscriber = null;
				if (task.containsKey(param_subscriber))
					subscriber = (Subscriber) task.get(param_subscriber);
				else
					subscriber = rbtDBManager.getSubscriber(subscriberID);

				WebServiceSubscriber webServicesubscriber = getWebServiceSubscriberObject(
						task, subscriber);

				Element subscriberElem = getSubscriberElement(document, task,
						webServicesubscriber, subscriber);
				element.appendChild(subscriberElem);

				if (action.equalsIgnoreCase(action_activate)
						|| action.equalsIgnoreCase(action_acceptGift))
					updateAccessCount(task, subscriber);

				if (action.equalsIgnoreCase(action_acceptGift)) {
					ViralSMSTable[] gifts = rbtDBManager.getViralSMSesByType(
							subscriberID, "GIFTED");
					WebServiceGift[] webServiceGifts = getWebServiceGiftObjects(
							task, gifts);

					Element giftInboxElem = getSubscriberGiftInboxElement(
							document, task, webServiceGifts, gifts);
					element.appendChild(giftInboxElem);
				}

				if (task.containsKey(param_packCosId)) {
					Element subscriberPackElem = getSubscriberPacks(document,
							task);
					element.appendChild(subscriberPackElem);
				}
				
				// Third Party confirmation chages 
				if (null != subscriber) {
					String extraInfo = subscriber.extraInfo();
					String mode = task.getString(param_mode) == null? "VP": task.getString(param_mode);
					boolean isTPCRequired = com.onmobile.apps.ringbacktones.services.common.Utility
							.isThirdPartyConfirmationRequired(mode, extraInfo);
					//RBT-9873 Added for bypassing CG flow
					String subscriptionClass = subscriber
							.subscriptionClass();
					boolean isSubClassConfForNotConsentFlow = false;
					if (com.onmobile.apps.ringbacktones.services.common.Utility
							.isSubscriptionClassConfiguredForNotCGFlow(subscriptionClass)) {
						isSubClassConfForNotConsentFlow = true;
					}
					
					logger.info(task);
					if(task.containsKey("CONSENTSTATUS"))
					{
						logger.info("--> Getting consentstatus from task object"+task.getString("CONSENTSTATUS"));
					}
					
					if (task.containsKey("CONSENTSTATUS")&&task.getString("CONSENTSTATUS").equals("1")&&(isTPCRequired && !isSubClassConfForNotConsentFlow )|| task.containsKey("CONSENTSTATUS")&&task.getString("CONSENTSTATUS").equals("1")&&task.containsKey(param_upgrade_consent_flow)) {
						String VfUpgradeFeatureClass = CacheManagerUtil
								.getParametersCacheManager().getParameterValue(
										iRBTConstant.COMMON,
										"CREATE_CLASS_FOR_VF_UPGRADE_FEATURE", null);
						String upgradeModes = CacheManagerUtil.getParametersCacheManager()
								.getParameterValue(iRBTConstant.COMMON,
										"VODAFONE_UPGRADE_CONSENT_MODES", null);
						boolean modeCheckForVfUpgrade = false;
						if (VfUpgradeFeatureClass != null ) {
							List<String> modesList = upgradeModes != null ? Arrays.asList(upgradeModes.split(",")) : null;
							modeCheckForVfUpgrade = (modesList == null || modesList
									.isEmpty() || !modesList.contains(mode));						
						}
						
						if (task.containsKey(param_upgrade_consent_flow)
								&& !modeCheckForVfUpgrade) {
							return document;
						}
						String transId = null;
						Map<String,String> extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
						boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils
								.getParamAsBoolean(iRBTConstant.COMMON, "CG_INTEGRATION_FLOW_FOR_BSNL_EAST",
										"FALSE");
						
						if(extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
							transId = extraInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
						}
						if(transId == null) {
							transId = subscriber.refID();
						}
						Element consentElem = document
								.createElement(param_consent);
						// Added for passing mode in create subscription API
						consentElem.setAttribute("mode",mode);
						
						if (task.containsKey(param_subscriber_consent)) {
							Subscriber consentSubscriber = (Subscriber) task
									.get(param_subscriber_consent);
							transId = consentSubscriber.refID();
							consentElem.setAttribute("mode",
									consentSubscriber.activatedBy());
						}
						logger.debug("Third party confirmation is required. refId: "
								+ transId + ", subscriptionClass: "
								+ subscriptionClass);

						consentElem.setAttribute(param_transID, transId);
						String srvId = null;
						String srvClass = null;
						if (null != subscriptionClass) {
							srvId = DoubleConfirmationConsentPushThread.getServiceValue(
									"SERVICE_ID", subscriber.subscriptionClass(), null,
									subscriber.circleID(), true, false, false);
							srvClass = DoubleConfirmationConsentPushThread.getServiceValue(
									"SERVICE_CLASS", subscriber.subscriptionClass(), null,
									subscriber.circleID(), true, false, false);
							if(srvId!=null){
							   consentElem.setAttribute(param_srvId, srvId);
							}
							if(srvClass!=null){
							   consentElem.setAttribute(param_SrvClass, srvClass);
							}
							String price = com.onmobile.apps.ringbacktones.services.common.Utility
									.getTPCGSubClassPrice(subscriptionClass);
							String validity = com.onmobile.apps.ringbacktones.services.common.Utility
									.getTPCGSubClassValidity(subscriptionClass);
							if (null != price) {
								consentElem.setAttribute(param_price, price);
							}
							if (null != validity) {
								consentElem.setAttribute(param_value, validity);
							}
							
							String language = null, planId = null, consentClassType = null, eventType = null,consentId = null;
							
							eventType = task.getString("EVENT_TYPE");
							if( null == eventType){
								eventType = "1";
							}
							
							planId  = task.getString("PLAN_ID");
							
							consentClassType = task.getString("CONSENTCLASSTYPE");
							
							consentId = task.getString("CONSENTID");
							
							language = task.getString("LANGUAGE_ID");
							
							consentElem.setAttribute(event_type,eventType);
							
							if( null != planId ){
								consentElem.setAttribute(plan_id,planId);
							} 
							
							if( null != language){
								consentElem.setAttribute(LANGUAGE,language);	
							}
							
							if( null != consentClassType ){
								consentElem.setAttribute( consent_class_type, consentClassType );
							}
							
							if( null != consentId ){
								consentElem.setAttribute( consent_id, consentClassType );
							}
							
							
						}
						//CG Integration Flow - Jira -12806
						consentElem = addConsentAttributes(consentElem,
								transId, subscriberID, false);
						
						if (isCGIntegrationFlowForBsnlEast) {
							consentElem.setAttribute(DESCRIPTION, task.getString("DESCRIPTION"));
						}
						element.appendChild(consentElem);
						//RBT-13537
						String requestType = task.containsKey(param_upgrade_consent_flow)? "UPGRAGE":"ACT";
						//RBT-14384 - This change for not allow the CDR logs for deactivation request
						if (task.containsKey("CONSENT_SUBSCRIPTION_INSERT")
								&& task.getString("CONSENT_SUBSCRIPTION_INSERT") != null
								&& task.getString(
										"CONSENT_SUBSCRIPTION_INSERT")
										.equalsIgnoreCase("true")) {
							writeCDRLog(subscriber, task, transId, null,
									subscriptionClass, requestType, srvId,
									srvClass);
						}
					}
				}
			}
		}  else if (task.containsKey("CGURL")
				&& task.containsKey("CGHttpResponse")) {
					String requestType = task.containsKey(param_upgrade_consent_flow)? "UPGRAGE":"ACT";
			writeCDRLog(null, task, null, null, null, requestType, null, null);
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getSelectionResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getSelectionResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		String subscriberID = task.getString(param_subscriberID);
		if (response.equalsIgnoreCase(SUCCESS)
				|| response.equalsIgnoreCase(SUCCESS_DOWNLOAD_EXISTS)) {
			if (task.containsKey(param_activatedNow)
					|| task.containsKey(param_modifiedSubscriber)) {
				Subscriber subscriber = (Subscriber) task.get(param_subscriber);
				WebServiceSubscriber webServiceSubscriber = getWebServiceSubscriberObject(
						task, subscriber);

				Element subscriberElem = getSubscriberElement(document, task,
						webServiceSubscriber, subscriber);
				element.appendChild(subscriberElem);

				if (task.containsKey(param_activatedNow))
					updateAccessCount(task, subscriber);
			}

			if (task.getString(param_action)
					.equalsIgnoreCase(action_acceptGift)) {
				ViralSMSTable[] gifts = rbtDBManager.getViralSMSesByType(
						subscriberID, "GIFTED");
				WebServiceGift[] webServiceGifts = getWebServiceGiftObjects(
						task, gifts);

				Element giftInboxElem = getSubscriberGiftInboxElement(document,
						task, webServiceGifts, gifts);
				element.appendChild(giftInboxElem);
			}

			Element libraryElem = getSubscriberLibraryElement(document, task);
			element.appendChild(libraryElem);

			if (task.containsKey(param_packCosId)) {
				Element subscriberPack = getSubscriberPacks(document, task);
				element.appendChild(subscriberPack);
			}
		} else if (task.getString(param_action).equalsIgnoreCase(
				action_acceptGift)
				&& (response.equals(ALREADY_EXISTS) || response
						.equals(ALREADY_ACTIVE))) {
			ViralSMSTable[] gifts = rbtDBManager.getViralSMSesByType(
					subscriberID, "GIFTED");
			WebServiceGift[] webServiceGifts = getWebServiceGiftObjects(task,
					gifts);

			Element giftInboxElem = getSubscriberGiftInboxElement(document,
					task, webServiceGifts, gifts);
			element.appendChild(giftInboxElem);
		} else if (response.equals(LITE_USER_PREMIUM_BLOCKED) && task.containsKey(VIRAL_SMS_TABLE_ARRAY)) {
			Element viralDataElement = BasicXMLElementGenerator.generateViralDataElement(document, task, (ViralSMSTable[]) task.get(VIRAL_SMS_TABLE_ARRAY));
			element.appendChild(viralDataElement);
		}
		
		// Third Party confirmation chages 
		Subscriber subscriber = (Subscriber) task.get(param_subscriber);
		String mode = task.getString(param_mode);
		String extraInfo = null;
		if(subscriber != null) {
			extraInfo = subscriber.extraInfo();
		}
		
		if(task.containsKey("CONSENTSTATUS"))
		{
			logger.info("--> Getting consentstatus from task object"+task.getString("CONSENTSTATUS"));
		}
		
		logger.info("--> Getting combo from task object"+task.getString("COMBO"));
		boolean combo=false;
		if(task.getString("COMBO")!=null)
			combo=Boolean.valueOf(task.getString("COMBO"));
		String consentId = task.getString("CONSENTID");
		String consentstatus="1";
		if(task.getString("CONSENTSTATUS")!=null)
		consentstatus=task.getString("CONSENTSTATUS");
		String consentClassType = task.getString("CONSENTCLASSTYPE");
		String consentSubClass = task.getString("CONSENTSUBCLASS");
		logger.info("--> -outsideif");
		//task.put(param_upgrade_consent_flow,"true");
		if (!combo&&!consentstatus.equals("0")&&Utility.isConsentRequest(task)) {
			logger.info("--> -insideif");
			boolean isPriceValSepAllowed = RBTParametersUtils.getParamAsBoolean(
					iRBTConstant.COMMON, "SEPARATE_PRICE AND VALIDITY_FOR_COMBO_ENABLED", "FALSE");
			boolean isActivatedNow = task.containsKey(param_activatedNow);
			String isActivated = task.getString(param_activatedNow);
			logger.debug("Consent is present in the request. consentId: " + consentId
					+ ", task contains activated now: " + task.containsKey(param_activatedNow)
					+ ", task activatednow: " + task.getString(param_activatedNow));

			Element consentElem = document.createElement(param_consent);
			String songPrice = com.onmobile.apps.ringbacktones.services.common.Utility
					.getTPCGChargeClassPrice(consentClassType);
			String subClassPrice = com.onmobile.apps.ringbacktones.services.common.Utility
					.getTPCGSubClassPrice(consentSubClass);
			String validity = com.onmobile.apps.ringbacktones.services.common.Utility
					.getTPCGChargeClassValidity(consentClassType);
			String dwnValidity = validity;
			boolean isComboReq = false;
			String baseValidity = null;
			if (consentSubClass != null) {
				logger.debug("Getting validity in consent. isActivatedNow: " + isActivatedNow
						+ ", isActivated: " + isActivated + ", consentSubClass: " + consentSubClass);
				// if the subscriber is activated now, it mean that it is a
				// combo(base + subscription) request.
				// For the combo request, the validity will be the base validity
				// not the selection validity.
				if (isActivatedNow && YES.equalsIgnoreCase(isActivated)) {
					validity = com.onmobile.apps.ringbacktones.services.common.Utility
							.getTPCGSubClassValidity(consentSubClass);
					isComboReq = true;
					baseValidity = validity;
					logger.debug("Since the request is combo, sending subscription class validity. validity: "
							+ validity);
				} else {
					validity = com.onmobile.apps.ringbacktones.services.common.Utility
							.getTPCGChargeClassValidity(consentSubClass);
				}
			}
			double price = 0;
			double basePrice = 0;
			double dwnPrice = 0;
			
			try {
				if (songPrice != null && songPrice.trim().length() != 0) {
					if(songPrice.contains(".")){
						price = Double.parseDouble(songPrice);
						dwnPrice = price;
					} else {
						price = price + Integer.parseInt(songPrice);
						dwnPrice = price;
					}
				}
				if (subClassPrice != null && subClassPrice.trim().length() != 0) {
					if (subClassPrice.contains(".")) {
						price = price + Double.parseDouble(subClassPrice);
						basePrice = Double.parseDouble(subClassPrice);
					} else {
						price = price + Integer.parseInt(subClassPrice);
						basePrice = Integer.parseInt(subClassPrice);
					}
				}
			} catch (Exception e) {
			}
			consentElem.setAttribute(param_transID, consentId);
			if (task.containsKey("CONSENT_MODE")) {
				consentElem.setAttribute("mode", task.getString("CONSENT_MODE"));
			}
			if(task.containsKey("BSNL_CONSENT_SESSION_ID")) {
				consentElem.setAttribute("consent_session_id", task.getString("BSNL_CONSENT_SESSION_ID"));
			}
			if (task.containsKey(param_subscriber_consent)) {
				Subscriber consentSubscriber = (Subscriber) task
						.get(param_subscriber_consent);
				consentElem.setAttribute(param_transID,
						consentSubscriber.refID());
				consentElem.setAttribute("mode",
						consentSubscriber.activatedBy());
			}
			if (isPriceValSepAllowed && isComboReq) {
				if (songPrice.contains(".") || subClassPrice.contains(".")) {
					consentElem.setAttribute(param_price, basePrice + "|"
							+ dwnPrice);
				} else {
					String baseValue = String.valueOf(basePrice);
					String dwnValue = String.valueOf(dwnPrice);
					consentElem.setAttribute(
							param_price,
							baseValue.substring(0, baseValue.indexOf("."))
									+ "|"
									+ dwnValue.substring(0,
											dwnValue.indexOf(".")));
				}
				consentElem.setAttribute(param_value, baseValidity + "|" + dwnValidity);
				consentElem.setAttribute(param_reqType, "Subscription|SongDownload");
				String clipID = task.getString(param_clipID);
				Clip clip = rbtCacheManager.getClip(clipID);
				if (clip != null) {
					consentElem.setAttribute(param_songname, clip.getClipName());
				}
			} else {
				if (songPrice.contains(".") || subClassPrice.contains(".")) {
					consentElem.setAttribute(param_price, price + "");
				} else {
					String priceValue = String.valueOf(basePrice);
					consentElem.setAttribute(param_price,
							priceValue.substring(0, priceValue.indexOf(".")));
				}
				consentElem.setAttribute(param_value, validity);
			}
			//CG Integration Flow - Jira -12806
			consentElem = addConsentAttributes(consentElem, consentId,
					subscriberID, isComboReq);
			boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils
					.getParamAsBoolean(iRBTConstant.COMMON, "CG_INTEGRATION_FLOW_FOR_BSNL_EAST",
							"FALSE");
			if (isCGIntegrationFlowForBsnlEast) {
				consentElem.setAttribute(DESCRIPTION, task.getString("DESCRIPTION"));
			}
			consentElem.setAttribute(param_srvId, task.getString("CONSENT_SERVICE_ID"));
			consentElem.setAttribute(param_SrvClass, task.getString("CONSENT_SERVICE_CLASS"));
			
			String language = task.getString("LANGUAGE_ID");
			String eventType = task.getString("EVENT_TYPE");
			String planId = task.getString("PLAN_ID");
			
			consentElem.setAttribute(LANGUAGE, language);
			consentElem.setAttribute(event_type, eventType);
			consentElem.setAttribute(plan_id,planId);
			
			element.appendChild(consentElem);
			
			//RBT-13537 VF IN:: 2nd Consent Reporting for RBT
			String requestType = isComboReq? "COMBO":"SEL";
			writeCDRLog(subscriber, task, consentId, consentClassType, consentSubClass, requestType, task.getString("CONSENT_SERVICE_ID"),task.getString("CONSENT_SERVICE_CLASS"));
		} else if (task.containsKey("CGURL")
				&& task.containsKey("CGHttpResponse") && task.getString("CGURL") != null) {
			
			writeCDRLog(null, task, null, null, null, "SEL", null, null);
			
		}
		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getBookMarkResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getBookMarkResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (response.equalsIgnoreCase(SUCCESS)) {
			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] bookmarks = rbtDBManager
					.getSubscriberBookMarks(subscriberID);
			WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = getWebServiceSubscriberBookMarkObjects(
					task, bookmarks);

			Element bookMarkElem = getSubscriberBookMarksElement(document,
					task, webServiceSubscriberBookMarks, bookmarks);
			element.appendChild(bookMarkElem);
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getGroupResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getGroupResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (response.equalsIgnoreCase(SUCCESS)) {
			Element groupDetailsElem = getSubscriberGroupDetailsElement(
					document, task);
			element.appendChild(groupDetailsElem);
		}

		return document;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getAffiliateGroupResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getAffiliateGroupResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (response.equalsIgnoreCase(SUCCESS)) {
			Element groupDetailsElem = getSubscriberAffiliateGroupDetailsElement(
					document, task);
			element.appendChild(groupDetailsElem);
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getCopyResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getCopyResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String action = task.getString(param_action);
		String response = SUCCESS;
		if (!action.equalsIgnoreCase(action_get))
			response = task.getString(param_response);

		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (action.equalsIgnoreCase(action_get)) {
			String fromSubscriberID = task.getString(param_fromSubscriber);
			SubscriberDetail subscriberDetail = RbtServicesMgr
					.getSubscriberDetail(new MNPContext(fromSubscriberID,
							"COPY"));

			if (subscriberDetail.isValidSubscriber()) {
				Element copyElem = getCopyDetailsElement(document, task);
				element.appendChild(copyElem);
			} else {
				String responseXML = null;
				try {
					String url = "";
					if (subscriberDetail.getCircleID().equalsIgnoreCase(
							"CENTRAL")) {
						Parameters parameter = parametersCacheManager
								.getParameter("GATHERER", "REDIRECT_NATIONAL");
						if (parameter != null) {
							String[] urlParams = parameter.getValue()
									.split(",");
							url = urlParams[1];
						}
					} else {
						String circleID = subscriberDetail.getCircleID();
						SitePrefix sitePrefix = CacheManagerUtil
								.getSitePrefixCacheManager().getSitePrefixes(
										circleID);
						url = sitePrefix.getSiteUrl();
					}
					url = url.replaceAll("rbt_sms.jsp", "");
					url = url.replaceAll("\\?", "");
					url = url + "Copy.do";

					HttpParameters httpParameters = new HttpParameters(url);
					HashMap<String, String> requestParams = new HashMap<String, String>();
					requestParams.put(param_subscriberID, task
							.getString(param_subscriberID));
					requestParams.put(param_fromSubscriber, fromSubscriberID);
					requestParams.put(param_action, action_get);

					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
							httpParameters, requestParams);
					responseXML = httpResponse.getResponse();

				} catch (Exception e) {
					logger.error("", e);
					responseXML = Utility.getErrorXML();
				}

				document = XMLUtils.getDocumentFromString(responseXML);
			}
		}

		return document;
	}

	@Override
	public Document getScratchCardResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);
		Scratchcard scratch = (Scratchcard) task.get(param_ScratchCard);
		Element scratchElem = getScratchCardElement(document, task, scratch);
		element.appendChild(scratchElem);
		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getGiftResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getGiftResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (task.getString(param_action).equalsIgnoreCase(action_rejectGift)) {
			task.put(param_subscriberID, task.getString(param_gifteeID));
			String subscriberID = task.getString(param_subscriberID);
			ViralSMSTable[] gifts = rbtDBManager.getViralSMSesByType(
					subscriberID, "GIFTED");
			WebServiceGift[] webServiceGifts = getWebServiceGiftObjects(task,
					gifts);

			Element giftInboxElem = getSubscriberGiftInboxElement(document,
					task, webServiceGifts, gifts);
			element.appendChild(giftInboxElem);
		}
		
		if (task.getString(param_action).equalsIgnoreCase(action_sendGift) && task.containsKey(param_isConsentFlow) && task.getString(param_isConsentFlow).equalsIgnoreCase(YES)) {
			getCGWAPConsentElement(document, task, element);
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getValidateNumberResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getValidateNumberResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		Element validateNoElem = getValidateNumberElement(document, task);
		element.appendChild(validateNoElem);

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getSetSubscriberDetailsResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getSetSubscriberDetailsResponseDocument(
			WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (response.equalsIgnoreCase(SUCCESS)) {
			String subscriberID = task.getString(param_subscriberID);
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);

			WebServiceSubscriber webServicesubscriber = getWebServiceSubscriberObject(
					task, subscriber);

			Element subscriberElem = getSubscriberElement(document, task,
					webServicesubscriber, subscriber);
			element.appendChild(subscriberElem);
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getUtilsResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getUtilsResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getApplicationDetailsDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getApplicationDetailsDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		Element applicationDetailsElem = document
				.createElement(APPLICATION_DETAILS);
		element.appendChild(applicationDetailsElem);

		if (response.equalsIgnoreCase(SUCCESS)
				&& !task.getString(param_action)
						.equalsIgnoreCase(action_remove)) {
			String info = task.getString(param_info);
			if (info == null || info.equalsIgnoreCase(PARAMETERS)) {
				Element parametersElem = getParametersElement(document, task);
				applicationDetailsElem.appendChild(parametersElem);
			}
			if (info == null || info.equalsIgnoreCase(SUBSCRIPTION_CLASSES)) {
				Element subscriptionClassesElem = getSubscriptionClassesElement(
						document, task);
				applicationDetailsElem.appendChild(subscriptionClassesElem);
			}
			if (info == null || info.equalsIgnoreCase(CHARGE_CLASSES)) {
				Element chargeClassesElem = getChargeClassesElement(document,
						task);
				applicationDetailsElem.appendChild(chargeClassesElem);
			}
			if (info != null && info.equalsIgnoreCase(SMS_TEXTS)) {
				Element smsTextsElem = getSMSTextsElement(document, task);
				applicationDetailsElem.appendChild(smsTextsElem);
			}
			if (info != null && info.equalsIgnoreCase(PICK_OF_THE_DAYS)) {
				Element pickOfTheDaysElem = getPickOfTheDaysElement(document,
						task);
				applicationDetailsElem.appendChild(pickOfTheDaysElem);
			}
			if (info != null && info.equalsIgnoreCase(RBT_LOGIN_USER)) {
				Element rbtLogInUserElem = getRBTLoginUserElement(document,
						task);
				if (rbtLogInUserElem != null)
					applicationDetailsElem.appendChild(rbtLogInUserElem);
			}
			if (info != null && info.equalsIgnoreCase(RBT_OTP_LOGIN)) {
				Element rbtLogInUserElem = getRBTLoginUserElement(document,
						task);
				if (rbtLogInUserElem != null)
					applicationDetailsElem.appendChild(rbtLogInUserElem);
			}
			if (info != null && info.equalsIgnoreCase(SITES)) {
				Element sitesElem = getSitesElement(document, task);
				applicationDetailsElem.appendChild(sitesElem);
			}
			if (info != null && info.equalsIgnoreCase(CHARGE_SMS)) {
				Element chargeSMSElem = getChargeSMSElement(document, task);
				applicationDetailsElem.appendChild(chargeSMSElem);
			}
			if (info != null && info.equalsIgnoreCase(COS_DETAILS)) {
				Element cosDetailsElem = getCosDetailsElement(document, task);
				applicationDetailsElem.appendChild(cosDetailsElem);
			}
			if (info != null && info.equalsIgnoreCase(RETAILER)) {
				Element retailerElem = getRetailerElement(document, task);
				applicationDetailsElem.appendChild(retailerElem);
			}
			if (info != null && info.equalsIgnoreCase(FEED_STATUS)) {
				Element feedStatusElem = getFeedStatusElement(document, task);
				applicationDetailsElem.appendChild(feedStatusElem);
			}
			if (info != null && info.equalsIgnoreCase(FEED_DETAILS)) {
				Element feedDetailsElem = getFeedDetailsElement(document, task);
				applicationDetailsElem.appendChild(feedDetailsElem);
			}
			if (info != null && info.equalsIgnoreCase(PREDEFINED_GROUPS)) {
				Element predefinedGroupsElem = getPredefinedGroupsElement(
						document, task);
				applicationDetailsElem.appendChild(predefinedGroupsElem);
			}
			if (info != null && info.equalsIgnoreCase(RBT_LOGIN_USERS)) {
				Element rbtLogInUserElem = getAllRBTLoginUsersElement(document,
						task);
				if (rbtLogInUserElem != null)
					applicationDetailsElem.appendChild(rbtLogInUserElem);
			}
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTInformation#getDataDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getDataDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (task.containsKey(param_onlyResponse)
				&& task.getString(param_onlyResponse).equalsIgnoreCase(YES))
			return document;

		if (response.equalsIgnoreCase(SUCCESS)
				&& !task.getString(param_action)
						.equalsIgnoreCase(action_remove)) {
			String info = task.getString(param_info);
			if (info != null && info.equalsIgnoreCase(VIRAL_DATA)) {
				Element viralDataElem = getViralDataElement(document, task);
				element.appendChild(viralDataElem);
			}
			if (info != null && task.getString(param_action)
					.equalsIgnoreCase(action_get)
					&& info
							.equalsIgnoreCase(PENDING_CONFIRMATIONS_REMINDER_DATA)) {
				Element pendingConfirmationsRemainderEle = getPendingConfirmationsRemainderDataElement(
						document, task);
				element.appendChild(pendingConfirmationsRemainderEle);
			} else if (info != null && info.equalsIgnoreCase(TRANS_DATA)) {
				Element transDataElem = getTransDataElement(document, task);
				element.appendChild(transDataElem);
			} else if (info != null && info.equalsIgnoreCase(RBTSUPPORT_DATA)) {
				Element transDataElem = getRbtSupportDataElement(document, task);
				element.appendChild(transDataElem);
			}
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTInformation#getBulkUploadTasks
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getBulkUploadTasks(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		if (response.equalsIgnoreCase(SUCCESS)
				&& !task.getString(param_action).equalsIgnoreCase(
						action_deleteTask)
				&& !task.getString(param_action).equalsIgnoreCase(
						action_removeTask)) {
			List<RBTBulkUploadTask> bulkTaskList = new ArrayList<RBTBulkUploadTask>();
			if (task.containsKey(param_taskID)) {
				bulkTaskList.add(RBTBulkUploadTaskDAO
						.getRBTBulkUploadTask(Integer.parseInt(task
								.getString(param_taskID))));
			} else {
				int taskStatus = BULKTASK_STATUS_NEW;
				if (task.containsKey(param_taskStatus))
					taskStatus = Integer.parseInt(task
							.getString(param_taskStatus));

				String taskType = task.getString(param_taskType);
				String circleID = task.getString(param_circleID);
				String taskMode = task.getString(param_taskMode);

				bulkTaskList = RBTBulkUploadTaskDAO.getRBTBulkTasks(taskStatus,
						taskType, circleID, taskMode);
			}
			Element tasksElement = BasicXMLElementGenerator
					.generateBulkTaskElement(document, bulkTaskList);
			element.appendChild(tasksElement);
		}

		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onmobile.apps.ringbacktones.webservice.RBTInformation#
	 * getSngResponseDocument
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getSngResponseDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = task.getString(param_response);
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		return document;
	}

	protected Element getSubscriberElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServicesubscriber,
			Subscriber subscriber) {
		Element element = BasicXMLElementGenerator.generateSubscriberElement(
				document, webServicesubscriber);
		return element;
	}

	protected WebServiceSubscriber getWebServiceSubscriberObject(
			WebServiceContext task, Subscriber subscriber) {
		String subscriberID = task.getString(param_subscriberID);
		String protocolId = task.getString(param_protocolNo);
		String protocolStaticText = task.getString(param_protocolStaticText);

		boolean canAllow = !(rbtDBManager.isTotalBlackListSub(subscriberID));

		boolean isValidPrefix = false;
		String circleID = null;
		boolean isPrepaid = false;
		String operatorUserInfo = task.getString(param_operatorUserInfo); // If
																			// UI
																			// passes
																			// the
																			// operatorUserInfo,
																			// same
																			// will
																			// be
																			// considered.

		int accessCount = 0;
		String subscriptionYes = null;
		String language = null;
		String subscriptionClass = null;
		String userType = NORMAL;
		String activatedBy = null;
		String activationInfo = null;
		String deactivatedBy = null;
		String lastDeactivationInfo = null;
		Date startDate = null;
		Date endDate = null;
		Date nextChargingDate = null;
		Date lastDeactivationDate = null;
		Date activationDate = null;
		String cosID = null;
		String refID = null;
		String userInfo = null;
		String cosType = null;
		int numMaxSelections = 0;
		String subscriberType = POSTPAID;
		String 	operatorName = null;
		String status = Utility.getSubscriberStatus(subscriber);
		String voluntary = "OFF";
		if (subscriber != null) {
			HashMap<String, String> extraInfo = DBUtility.getAttributeMapFromXML(subscriber
					.extraInfo());
			// If subscriber is voluntary suspension
			if ((status.equalsIgnoreCase(ACTIVE) || status.equalsIgnoreCase(SUSPENDED))
					&& (extraInfo != null && extraInfo.containsKey(iRBTConstant.VOLUNTARY) && (extraInfo
							.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("TRUE") || extraInfo.get(
							iRBTConstant.VOLUNTARY).equalsIgnoreCase("SM_SUSPENDED"))))
				voluntary = "ON";
			
			//RBT-12942
			if (RBTParametersUtils.getParamAsBoolean("COMMON",iRBTConstant.IS_RENEWAL_GRACE_ENABLED, "FALSE")
					&& !status.equals(DEACT_ERROR)
					&& !status.equals(DEACT_PENDING)
					&& !status.equals(DEACTIVE)
					&& extraInfo != null
					&& extraInfo.containsKey(RENEWAL_GRACE)
					&& ((String)extraInfo.get(RENEWAL_GRACE)).equalsIgnoreCase("TRUE")) {
				status = GRACE;
			}
		}
		

		if(subscriber!=null && subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_ACTIVATION_PENDING)){
			String configuredModesToStatus = null;
			if(subscriber.oldClassType()== null){
			    configuredModesToStatus = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "MODES_TO_STATUS_MAPPING_FOR_NULL_OLDCLASS",null);
			}else{
				configuredModesToStatus = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "MODES_TO_STATUS_MAPPING_FOR_NOTNULL_OLDCLASS",null);
			}

			if(configuredModesToStatus!=null && configuredModesToStatus.indexOf(":")!=-1){
			    String mode = task.getString(param_mode);
			    HashMap<String,String> map = new HashMap<String,String>();
			    String []modeStatusArray = configuredModesToStatus.split(",");
			    if(modeStatusArray!=null){
			    	for(int i=0;i<modeStatusArray.length;i++){
			    		String str[] = modeStatusArray[i].split(":");
			    		if(str.length == 2)
			    		   map.put(str[0], str[1]);
			    	}
		       }
			    if(map.containsKey(mode))
			    	status = map.get(mode);
		     }
		}
		if (status.equals(NEW_USER) || status.equals(DEACTIVE)) {
			boolean isActPendingStatusSupp = Arrays.asList(RBTParametersUtils.getParamAsString("COMMON",
							"MODES_FOR_CONSENT_STATUS_FOR_NEW_USER", "").split(",")).contains(task.getString(param_mode));
			if((task.containsKey(param_preConsent) && "TRUE".equalsIgnoreCase((String)task.get(param_preConsent)))||isActPendingStatusSupp) {
				List<DoubleConfirmationRequestBean> consentRequestBeanList = null;
				try {
					consentRequestBeanList = rbtDBManager.getConsentPendingRecordListByMsisdnNType(subscriberID, "ACT");
				} catch (OnMobileException e) {
					logger.debug("Exception while getting act consent pending records from DB", e);
				}
				if(consentRequestBeanList != null && consentRequestBeanList.size() > 0) {
					status = ACT_CONSENT_PENDING;
				}
			}
			if (migratedOIModesList.contains(task.getString(param_mode))||migratedOIModesList.contains("ALL")) {
				Offer offer[] = null;
				try{
				    offer = RBTSMClientHandler.getInstance()
						.getOffer(subscriberID, task.getString(param_mode),
								Offer.OFFER_TYPE_SUBSCRIPTION, null, null, null);
				}catch(Exception ex){
					logger.info("Exception while getting offers for OI Subscriber = "+ex);
					ex.printStackTrace();
				}
				if (offer != null && offer.length > 0) {
					userType = "free";
				} else {
					userType = "no_free";
				}
			}
			SubscriberDetail subscriberDetail = DataUtils
					.getSubscriberDetail(task);
			if (subscriberDetail != null) {
				isValidPrefix = subscriberDetail.isValidSubscriber();
				circleID = subscriberDetail.getCircleID();
				isPrepaid = subscriberDetail.isPrepaid();

				HashMap<String, String> subscriberDetailsMap = subscriberDetail
						.getSubscriberDetailsMap();
				if (subscriberDetailsMap != null) {
					if (subscriberDetailsMap.containsKey(MNPConstants.STATUS))
						status = subscriberDetailsMap.get(MNPConstants.STATUS);
					if (subscriberDetailsMap
							.containsKey(MNPConstants.OPERATOR_USER_INFO))
						operatorUserInfo = subscriberDetailsMap
								.get(MNPConstants.OPERATOR_USER_INFO);
				}
			}
		} else {
			isValidPrefix = true;
			circleID = subscriber.circleID();
			isPrepaid = subscriber.prepaidYes();
		}

		if (subscriber != null) {
			accessCount = subscriber.noOfAccess();
			subscriptionYes = subscriber.subYes();
			language = subscriber.language();
			subscriptionClass = subscriber.subscriptionClass();
			numMaxSelections = subscriber.maxSelections();

			if (subscriber.cosID() != null)
				cosType = CacheManagerUtil.getCosDetailsCacheManager()
						.getCosDetail(subscriber.cosID()).getCosType();
			//Changes done for RBT-17389
			//if (migratedOIModesList.contains(task.getString(param_mode))||migratedOIModesList.contains("ALL")) {
			   //if (subscriber.endDate()!=null && subscriber.endDate().after(new Date())) {
				//String subClass = subscriber.subscriptionClass();
				//	if(migratedSubClassList!=null && migratedSubClassList.size()>0){
				//	if (migratedSubClassList.contains(subClass)) {
				//		userType = "migrated";
				//	} else {
				//		userType = "normal";
					//}
				//	} 
				//}
			//} 
			if (subscriber.rbtType() == 1)
				userType = AD_RBT;
			else if (subscriber.rbtType() == 2)
				userType = REVERSE_RBT;
			else if (subscriber.rbtType() == 3)
				userType = NORMAL_REVERSE_RBT;
			else if (subscriber.rbtType() == 4)
				userType = AD_REVERSE_RBT;
			else if (subscriber.rbtType() == 10)
				userType = LANDLINE;
			else {
				if (subscriber.endDate()!=null && subscriber.endDate().after(new Date())) {
					String subClass = subscriber.subscriptionClass();
					if(migratedSubClassList!=null && migratedSubClassList.size()>0){
					if (migratedSubClassList.contains(subClass)) {
						userType = "migrated";
					} else {
						userType = "normal";
					}
					} 
				}
				boolean isCorporateUser = false;
				boolean isEmotionalRbtUser = false;
				SubscriberStatus[] settings = rbtDBManager
						.getAllActiveSubscriberSettings(subscriberID);
				task.put(param_settings, settings);
				if (settings != null) {
					String clipWavFile = null;
					Parameters parameter = parametersCacheManager.getParameter(
							"COMMON", "EMOTION_RBT_DEFAULT_CONFIG");
					if (parameter != null) {
						String emotionConfig = parameter.getValue();
						String[] tokens = emotionConfig.split(",");
						String clipIDStr = tokens[0];
						try {
							String browsingLanguage = task
									.getString(param_browsingLanguage);
							Clip clip = rbtCacheManager.getClip(Integer
									.parseInt(clipIDStr), browsingLanguage);
							clipWavFile = clip.getClipRbtWavFile();
						} catch (Exception e) {
						}
					}

					for (SubscriberStatus subscriberStatus : settings) {
						if (subscriberStatus.selType() == 2) {
							isCorporateUser = true;
							break;
						}
						if (clipWavFile != null
								&& subscriberStatus.subscriberFile()
										.equalsIgnoreCase(clipWavFile)) {
							isEmotionalRbtUser = true;
						}
					}
				}
				if (isCorporateUser)
					userType = CORPORATE;
				else if (isEmotionalRbtUser)
					userType = EMOTION_RBT_USER;
				else {
					if (cosType != null
							&& cosType.equalsIgnoreCase(COS_TYPE_LITE))
						userType = RBT_LITE_USER;
				}
			}

			userInfo = subscriber.extraInfo();
			if (userType.equalsIgnoreCase(CORPORATE)) {
				Parameters parameter = parametersCacheManager.getParameter(
						iRBTConstant.COMMON, "ADD_TO_DOWNLOADS", "FALSE");
				boolean isDownloadsModel = parameter.getValue()
						.equalsIgnoreCase("TRUE");
				if (isDownloadsModel) {
					SubscriberDownloads[] downloads = rbtDBManager
							.getActiveSubscriberDownloads(subscriberID);
					task.put(param_downloads, downloads);
					if (downloads != null) {
						for (SubscriberDownloads download : downloads) {
							HashMap<String, String> extraInfo = DBUtility
									.getAttributeMapFromXML(download
											.extraInfo());
							if (extraInfo != null
									&& extraInfo
											.containsKey(iRBTConstant.VOLUNTARY))
								userInfo = DBUtility.setXMLAttribute(userInfo,
										iRBTConstant.VOLUNTARY, "TRUE");
						}
					}
				} else {
					SubscriberStatus[] settings = null;
					if (!task.containsKey(param_settings)) {
						settings = rbtDBManager
								.getAllActiveSubscriberSettings(subscriberID);
						task.put(param_settings, settings);
					} else
						settings = (SubscriberStatus[]) task
								.get(param_settings);

					if (settings != null) {
						for (SubscriberStatus setting : settings) {
							HashMap<String, String> extraInfo = DBUtility
									.getAttributeMapFromXML(setting.extraInfo());
							if (extraInfo != null
									&& extraInfo
											.containsKey(iRBTConstant.VOLUNTARY))
								userInfo = DBUtility.setXMLAttribute(userInfo,
										iRBTConstant.VOLUNTARY, "TRUE");
						}
					}
				}
			}

			if(subscriber.prepaidYes()){
				subscriberType = PREPAID;
			}
			if(subscriber.extraInfo()!= null && subscriber.extraInfo().contains(iRBTConstant.HYBRID_SUBSCRIBER_TYPE)){
				subscriberType = HYBRID;
			}
			
			activatedBy = subscriber.activatedBy();
			activationInfo = subscriber.activationInfo();
			deactivatedBy = subscriber.deactivatedBy();
			lastDeactivationInfo = subscriber.lastDeactivationInfo();
			startDate = subscriber.startDate();
			endDate = subscriber.endDate();
			nextChargingDate = subscriber.nextChargingDate();
			lastDeactivationDate = subscriber.lastDeactivationDate();
			activationDate = subscriber.activationDate();
			cosID = subscriber.cosID();
			refID = subscriber.refID();
		}
		
		//below config is for B2B
		Parameters isD2CParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "DTOC_DEPLOYED",null);

		if (isD2CParam != null) {
			if (isD2CParam.getValue() != null && isD2CParam.getValue().equalsIgnoreCase("TRUE")) {
				operatorName = ConsentPropertyConfigurator.getOperatorFormConfig();
			}
		} else {// Added for D2C
			if (circleID != null && circleID.split("_").length > 1) {
				operatorName = circleID.split("_")[0];
				circleID = circleID.split("_")[1];
			} else {
				try {
					operatorName = ConsentPropertyConfigurator.getOperatorFormConfig();
				} catch (Exception e) {

				}
			}
		}

		WebServiceSubscriber webServiceSubscriber = createWebServiceSubscriberObject();
		webServiceSubscriber.setSubscriberID(subscriberID);
		webServiceSubscriber.setValidPrefix(isValidPrefix);
		webServiceSubscriber.setVoluntary(voluntary);
		webServiceSubscriber.setCanAllow(canAllow);
		webServiceSubscriber.setPrepaid(isPrepaid);
		webServiceSubscriber.setAccessCount(accessCount);
		webServiceSubscriber.setSubscriptionYes(subscriptionYes);
		webServiceSubscriber.setStatus(status);
		webServiceSubscriber.setLanguage(language);
		webServiceSubscriber.setCircleID(circleID);
		webServiceSubscriber.setSubscriptionClass(subscriptionClass);
		webServiceSubscriber.setUserType(userType);
		webServiceSubscriber.setActivatedBy(activatedBy);
		webServiceSubscriber.setActivationInfo(activationInfo);
		webServiceSubscriber.setDeactivatedBy(deactivatedBy);
		webServiceSubscriber.setLastDeactivationInfo(lastDeactivationInfo);
		webServiceSubscriber.setStartDate(startDate);
		webServiceSubscriber.setEndDate(endDate);
		webServiceSubscriber.setNextChargingDate(nextChargingDate);
		webServiceSubscriber.setLastDeactivationDate(lastDeactivationDate);
		webServiceSubscriber.setActivationDate(activationDate);
		webServiceSubscriber.setCosID(cosID);
		webServiceSubscriber.setRefID(refID);
		webServiceSubscriber.setUserInfo(userInfo);
		webServiceSubscriber.setOperatorUserInfo(operatorUserInfo);
		webServiceSubscriber.setPack(cosType);
		webServiceSubscriber.setProtocolNo(protocolId);
		webServiceSubscriber.setProtocolStaticText(protocolStaticText);
		webServiceSubscriber.setNumMaxSelections(numMaxSelections);
		webServiceSubscriber.setSubscriberType(subscriberType);
		webServiceSubscriber.setOperatorName(operatorName);

		if(task.containsKey(iRBTConstant.param_isSubConsentInserted)){
			webServiceSubscriber.setSubConsentInserted(true);
		}
		boolean isRRBTSystem = RBTDeploymentFinder.isRRBTSystem();
		boolean isAnnouncementsSupported = parametersCacheManager.getParameter(
				iRBTConstant.COMMON, iRBTConstant.PROCESS_ANNOUNCEMENTS,
				"FALSE").getValue().equalsIgnoreCase("TRUE");
		if (isRRBTSystem && isAnnouncementsSupported) {
			SubscriberAnnouncements[] subscriberAnnouncements = rbtDBManager
					.getActiveSubscriberAnnouncemets(subscriberID);
			if (subscriberAnnouncements != null
					&& subscriberAnnouncements.length != 0) {
				String pcaClipName = null;
				SubscriberAnnouncements announcement = subscriberAnnouncements[subscriberAnnouncements.length - 1];
				int clipID = announcement.clipId();
				Clip clip = rbtCacheManager.getClip(clipID, null);
				if (clip != null)
					pcaClipName = clip.getClipName();

				webServiceSubscriber.setPca(pcaClipName);
			}
		}

		// User Circle ID is added to task, so that no need query the
		// DB for getting Circle ID in preparing other parts of the XML
		task.put(param_circleID, circleID);

		// User type is added to task, so that no need query the
		// subscriber object for getting user type in preparing other parts of
		// the XML
		task.put(param_isPrepaid, isPrepaid ? YES : NO);

		if (logger.isDebugEnabled())
			logger.debug("RBT:: webServiceSubscriber: " + webServiceSubscriber);
		return webServiceSubscriber;
	}

	protected WebServiceSubscriber createWebServiceSubscriberObject() {
		WebServiceSubscriber webServiceSubscriber = new WebServiceSubscriber();
		return webServiceSubscriber;
	}

	protected Element getSubscriberGiftInboxElement(Document document,
			WebServiceContext task, WebServiceGift[] webServiceGifts,
			ViralSMSTable[] gifts) {
		Element element = document.createElement(GIFT_INBOX);
		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (webServiceGifts == null)
			element.setAttribute(NO_OF_GIFTS, "0");
		else {
			element.setAttribute(NO_OF_GIFTS, String
					.valueOf(webServiceGifts.length));

			for (WebServiceGift webServiceGift : webServiceGifts) {
				Element contentElem = getSubscriberGiftContentElement(document,
						webServiceGift,task);
				contentsElem.appendChild(contentElem);
			}
		}

		return element;
	}

	protected Element getSubscriberGiftOutboxElement(Document document,
			WebServiceContext task, WebServiceGift[] webServiceGifts,
			ViralSMSTable[] gifts) {
		Element element = document.createElement(GIFT_OUTBOX);
		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (webServiceGifts == null)
			element.setAttribute(NO_OF_GIFTS, "0");
		else {
			element.setAttribute(NO_OF_GIFTS, String
					.valueOf(webServiceGifts.length));

			for (WebServiceGift webServiceGift : webServiceGifts) {
				Element contentElem = getSubscriberGiftContentElement(document,
						webServiceGift, task);
				Utility.addPropertyElement(document, contentElem, STATUS, DATA,
						webServiceGift.getStatus());

				contentsElem.appendChild(contentElem);
			}
		}

		return element;
	}

	protected Element getSubscriberGiftContentElement(Document document,
			WebServiceGift webServiceGift) {
		return getSubscriberGiftContentElement(document, webServiceGift, null);
	}
	
	protected Element getSubscriberGiftContentElement(Document document,
			WebServiceGift webServiceGift, WebServiceContext task) {
		Element element = BasicXMLElementGenerator
				.generateSubscriberGiftContentElement(document, webServiceGift,task);
		return element;
	}
	protected WebServiceGift[] getWebServiceGiftObjects(WebServiceContext task,
			ViralSMSTable[] gifts) {
		if (gifts == null || gifts.length == 0)
			return null;

		Parameters giftCategoryParam = parametersCacheManager.getParameter(
				iRBTConstant.COMMON, "GIFT_CATEGORY", "23");
		int giftCategoryID = Integer.parseInt(giftCategoryParam.getValue());

		String browsingLanguage = task.getString(param_browsingLanguage);
		int validityInt = RBTParametersUtils.getParamAsInt("GATHERER", "OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2);

		WebServiceGift[] webServiceGifts = new WebServiceGift[gifts.length];
		for (int i = 0; i < gifts.length; i++) {
			String sender = gifts[i].subID();
			String receiver = gifts[i].callerID();
			Date sentTime = gifts[i].sentTime();
			String status = gifts[i].type();
			String extraInfo = gifts[i].extraInfo();
			String selectedBy = gifts[i].selectedBy();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(sentTime);
			cal.add(Calendar.DATE, validityInt);
			Date validity = cal.getTime();
			
			String giftTone = gifts[i].clipID();
			int categoryID = giftCategoryID;
			int toneID = 0;
			String toneName = "";
			String toneType = CLIP;
			String previewFile = "";
			String rbtFile = "";
			String vcode = null;
			if (giftTone == null) {
				toneType = SERVICE;
				previewFile = "rbt_service_gift";
				rbtFile = "rbt_service_gift";
			} else if (giftTone.startsWith("C")) {
				categoryID = Integer.parseInt(giftTone.substring(1).trim());
				Category category = rbtCacheManager.getCategory(categoryID,
						browsingLanguage);
				if (category == null)
					throw new NullPointerException(
							"Category does not exist: categoryID " + categoryID);

				Clip[] clips = rbtCacheManager.getActiveClipsInCategory(
						categoryID, browsingLanguage);
				if (clips == null || clips.length == 0)
					throw new NullPointerException(
							"No clip under category: categoryID " + categoryID);
  
				vcode = clips[0].getClipRbtWavFile().replaceAll("rbt_", "").replaceAll("_rbt", "");
				toneID = clips[0].getClipId();
				toneName = category.getCategoryName();
				toneType = CATEGORY_SHUFFLE;
				previewFile = category.getCategoryPreviewWavFile();
				rbtFile = category.getCategoryNameWavFile();
			} else {
				toneID = Integer.parseInt(giftTone.trim());
				Clip clip = rbtCacheManager.getClip(toneID, browsingLanguage);
				if (clip == null)
					throw new NullPointerException(
							"Clip does not exist: clipID " + toneID);

				toneName = clip.getClipName();
				previewFile = clip.getClipPreviewWavFile();
				rbtFile = clip.getClipRbtWavFile();
				vcode = clip.getClipRbtWavFile().replaceAll("rbt_", "").replaceAll("_rbt", "");
			}

			WebServiceGift webServiceGift = createWebServiceGiftObject();
			webServiceGift.setSender(sender);
			webServiceGift.setReceiver(receiver);
			webServiceGift.setCategoryID(categoryID);
			webServiceGift.setToneID(toneID);
			webServiceGift.setToneName(toneName);
			webServiceGift.setToneType(toneType);
			webServiceGift.setPreviewFile(previewFile);
			webServiceGift.setRbtFile(rbtFile);
			webServiceGift.setSentTime(sentTime);
			webServiceGift.setStatus(status);
			webServiceGift.setGiftExtraInfo(extraInfo);
			webServiceGift.setSelectedBy(selectedBy);
			webServiceGift.setValidity(validity);
			webServiceGift.setClipVcode(vcode);
			webServiceGifts[i] = webServiceGift;
		}

		if (logger.isDebugEnabled())
			logger.debug("RBT:: webServiceGifts: "
					+ Arrays.toString(webServiceGifts));
		return webServiceGifts;
	}

	protected Element getScratchCardElement(Document document,
			WebServiceContext task, Scratchcard scratch) {
		Element element = BasicXMLElementGenerator.generateScratchCardElement(
				document, scratch);
		return element;
	}

	protected WebServiceGift createWebServiceGiftObject() {
		WebServiceGift webServiceGift = new WebServiceGift();
		return webServiceGift;
	}

	protected Element getSubscriberLibraryElement(Document document,
			WebServiceContext task) {
		Element element = document.createElement(LIBRARY);

		if(task.containsKey(iRBTConstant.param_isSelConsentInserted)){
			element.setAttribute(IS_RECENT_SEL_CONSENT, "true");
		}

		String subscriberID = task.getString(param_subscriberID);

		SubscriberStatus[] settings = null;
		if (!task.containsKey(param_settings)) {
			settings = rbtDBManager
					.getAllActiveSubscriberSettings(subscriberID);
			task.put(param_settings, settings);
		} else
			settings = (SubscriberStatus[]) task.get(param_settings);

		String mode = "VP";
		if (task.containsKey(param_mode))
			mode = task.getString(param_mode);
		
		//Added for Vf-Spain by Sreekar
		settings = getActiveSettingsTobeDisplayed(settings);
		
		settings = DataUtils.getRecentActiveSettings(rbtDBManager, settings,
				mode, null);
		WebServiceSubscriberSetting[] webServiceSubscriberSettings = getWebServiceSubscriberSettingObjects(
				task, settings);

		Element settingsElem = getSubscriberSettingsElement(document, task,
				webServiceSubscriberSettings, settings);
		element.appendChild(settingsElem);

		return element;
	}
	
	protected SubscriberDownloads[] getActiveDownloadsToBedisplayed(SubscriberDownloads[] downloads) {
		
		/**
		 * Added by Sreekar for Vf-Spain
		 */
		if (parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "DISPLAY_ONLY_ACTIVE_LIBRARY", "FALSE").getValue()
				.equalsIgnoreCase("true") && downloads != null) {
			List<SubscriberDownloads> newList = new ArrayList<SubscriberDownloads>();
			for(SubscriberDownloads download : downloads) {
				if(download.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_ACTIVATED) {
					newList.add(download);
				}
			}
			
			if(newList.size() > 0) {
				downloads = newList.toArray(new SubscriberDownloads[0]);
			}
			else {
				downloads = null;
			}
		}
		//End of Vf-Spain changes to display only active downloads
		return downloads;
	}

	protected SubscriberStatus[] getActiveSettingsTobeDisplayed(SubscriberStatus[] settings) {
		/**
		 * Added by Sreekar for Vf-Spain
		 */
		if (parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "DISPLAY_ONLY_ACTIVE_LIBRARY", "FALSE").getValue()
				.equalsIgnoreCase("true") && settings != null) {
			List<SubscriberStatus> newList = new ArrayList<SubscriberStatus>();
			for(SubscriberStatus setting : settings) {
				if (setting.selStatus().equals(iRBTConstant.STATE_ACTIVATED)
						|| (parametersCacheManager.getParameter(iRBTConstant.COMMON, "ADD_TO_DOWNLOADS", "FALSE").getValue()
						.equalsIgnoreCase("true") && setting.selStatus().equals(iRBTConstant.STATE_TO_BE_ACTIVATED))) {
					newList.add(setting);
				}
			}
			
			if(newList.size() > 0) {
				settings = newList.toArray(new SubscriberStatus[0]);
			}
			else {
				settings = null;
			}
		}
		//End of Vf-Spain changes to display only active downloads
		return settings;
	}
	
	protected Element getSubscriberLibraryHistoryElement(Document document,
			WebServiceContext task) {
		Element element = document.createElement(LIBRARY);

		SubscriberStatus[] settings = DataUtils
				.getFilteredSettingsHistory(task);
		WebServiceSubscriberSetting[] webServiceSubscriberSettings = getWebServiceSubscriberSettingObjects(
				task, settings);

		Element settingsElem = getSubscriberSettingsElement(document, task,
				webServiceSubscriberSettings, settings);
		element.appendChild(settingsElem);

		return element;
	}

	protected Element getSubscriberRefundableSelectionsElement(
			Document document, WebServiceContext task) {
		Element element = document.createElement(LIBRARY);

		String subscriberID = task.getString(param_subscriberID);

		Parameters refundablePeriodParam = parametersCacheManager.getParameter(
				iRBTConstant.COMMON, "REFUNDABLE_PERIOD", "24");
		int refundablePeriod = Integer.parseInt(refundablePeriodParam
				.getValue().trim());

		SubscriberStatus[] settings = rbtDBManager
				.getSubscriberRecords(subscriberID);
		if (settings != null) {
			List<SubscriberStatus> list = new ArrayList<SubscriberStatus>();

			for (SubscriberStatus setting : settings) {
				if (rbtDBManager.isSelectionActivationPending(setting))
					continue;

				HashMap<String, String> selectionInfoMap = DBUtility
						.getAttributeMapFromXML(setting.extraInfo());
				if (selectionInfoMap == null
						|| !selectionInfoMap.containsKey(iRBTConstant.REFUND)
						|| selectionInfoMap.containsKey(iRBTConstant.REFUNDED))
					continue;

				Date setTime = setting.setTime();
				long timeElapsedHrs = (System.currentTimeMillis() - setTime
						.getTime())
						/ (60 * 60 * 1000);
				if (timeElapsedHrs > refundablePeriod)
					continue;

				list.add(setting);
			}

			if (list.size() > 0)
				settings = list.toArray(new SubscriberStatus[0]);
			else
				settings = null;
		}

		WebServiceSubscriberSetting[] webServiceSubscriberSettings = getWebServiceSubscriberSettingObjects(
				task, settings);

		Element settingsElem = getSubscriberSettingsElement(document, task,
				webServiceSubscriberSettings, settings);
		element.appendChild(settingsElem);

		return element;
	}

	protected Element getSubscriberSettingsElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberSetting[] webServiceSubscriberSettings,
			SubscriberStatus[] settings) {
		Element element = document.createElement(SETTINGS);
		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (webServiceSubscriberSettings == null) {
			element.setAttribute(NO_OF_SETTINGS, "0");
			element.setAttribute(NO_OF_DEFAULT_SETTINGS, "0");
			element.setAttribute(NO_OF_SPECIAL_SETTINGS, "0");
		} else if(webServiceSubscriberSettings[0].isDefaultMusic()) {
			element.setAttribute(NO_OF_SETTINGS, "0");
			element.setAttribute(NO_OF_DEFAULT_SETTINGS, "0");
			element.setAttribute(NO_OF_SPECIAL_SETTINGS, "0");
			Element contentElem = UnitelXMLElementGenerator.getSubscriberDefaultSettingContentElement(
					document, task, webServiceSubscriberSettings[0]);
			contentsElem.appendChild(contentElem);
			
		} else {
			element.setAttribute(NO_OF_SETTINGS, String
					.valueOf(webServiceSubscriberSettings.length));

			int noOfDefaultSettings = 0;
			for (WebServiceSubscriberSetting webServiceSubscriberSetting : webServiceSubscriberSettings) {
				HashMap<String,String> extraInfoMap = DBUtility.getAttributeMapFromXML(webServiceSubscriberSetting.getSelectionExtraInfo());
				if (RBTParametersUtils.getParamAsBoolean("WEBSERVICE", "DISPLAY_CALLER_ID_WITH_PREFIX", "FALSE")
						&& extraInfoMap != null && extraInfoMap.containsKey("CALLER_ID")) {
					webServiceSubscriberSetting.setCallerID(extraInfoMap.get("CALLER_ID"));
				}
				if (webServiceSubscriberSetting.getCallerID().equalsIgnoreCase(
						ALL))
					noOfDefaultSettings++;

				Element contentElem = getSubscriberSettingContentElement(
						document, task, webServiceSubscriberSetting);
				contentsElem.appendChild(contentElem);
			}

			element.setAttribute(NO_OF_DEFAULT_SETTINGS, String
					.valueOf(noOfDefaultSettings));
			element.setAttribute(NO_OF_SPECIAL_SETTINGS, String
					.valueOf(webServiceSubscriberSettings.length
							- noOfDefaultSettings));
		}

		return element;
	}

	protected Element getSubscriberSettingContentElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberSetting webServiceSubscriberSetting) {
		Element element = BasicXMLElementGenerator
				.generateSubscriberSettingContentElement(document, task,
						webServiceSubscriberSetting);
		return element;
	}

	protected WebServiceSubscriberSetting[] getWebServiceSubscriberSettingObjects(
			WebServiceContext task, SubscriberStatus[] settings) {
		//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
		String subscriberID = task.getString(param_subscriberID);
		if (settings == null || settings.length == 0) {
			WebServiceSubscriberSetting[] webServiceSubscriberSettings = new WebServiceSubscriberSetting[1];
			Parameters paramObj = parametersCacheManager.getParameter("WEBSERVICES", "DEFAULT_CLIP", null);
			if (paramObj != null) {
				Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
				Clip clipObj = rbtCacheManager.getClip(paramObj.getValue());
				String[] previewFiles = new String[2];
				String[] rbtFiles = new String[2];
				String amount = "0";
				ChargeClass chargeClassObj = 
						CacheManagerUtil.getChargeClassCacheManager().getChargeClass(clipObj.getClassType());
				if(chargeClassObj != null) {
					amount = chargeClassObj.getAmount();
				} 
				previewFiles[0] = clipObj.getClipPreviewWavFile();
				rbtFiles[0] = clipObj.getClipRbtWavFile();
				WebServiceSubscriberSetting webServiceSubscriberDefaultSetting = 
						createWebServiceSubscriberSettingObject();
				webServiceSubscriberDefaultSetting
						.setSubscriberID(subscriberID);
				webServiceSubscriberDefaultSetting.setCallerID("all");
				webServiceSubscriberDefaultSetting.setToneType("clip");
				webServiceSubscriberDefaultSetting.setToneID(clipObj
						.getClipId());
				webServiceSubscriberDefaultSetting.setToneName(clipObj
						.getClipName());
				webServiceSubscriberDefaultSetting.setClipVcode(clipObj.getClipRbtWavFile()
						.replaceAll("rbt_", "").replaceAll("_rbt", ""));
				webServiceSubscriberDefaultSetting
						.setPreviewFiles(previewFiles);
				webServiceSubscriberDefaultSetting.setRbtFiles(rbtFiles);
				webServiceSubscriberDefaultSetting.setArtistName(clipObj
						.getArtist());
				webServiceSubscriberDefaultSetting.setTonePrice(amount);
				webServiceSubscriberDefaultSetting.setSetTime(subscriber.startDate());
				webServiceSubscriberDefaultSetting.setStartTime(subscriber.startDate());
				webServiceSubscriberDefaultSetting.setEndTime(subscriber.endDate());
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

		//String subscriberID = task.getString(param_subscriberID);
	    String browsingLanguage = task.getString(param_browsingLanguage);
		int finalSettingsCount = 0;
	    //RBT-12247
	    String currentRefId = task.getString("CURRENT_REF_ID");
		for (int i = 0; i < settings.length; i++) {
			String callerID = settings[i].callerID() == null ? ALL
					: settings[i].callerID();
			int status = settings[i].status();

			int fromTime = settings[i].fromTime();
			int toTime = settings[i].toTime();

			DecimalFormat decimalFormat = new DecimalFormat("0000");
			String fromTimeStr = decimalFormat.format(fromTime);
			String toTimeStr = decimalFormat.format(toTime);

			int fromTimeHrs = Integer.parseInt(fromTimeStr.substring(0, 2));
			int fromTimeMinutes = Integer.parseInt(fromTimeStr.substring(2, 4));
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
				category = rbtCacheManager.getCategoryByPromoId(settings[i]
						.subscriberFile(), browsingLanguage);
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

			Clip clip = rbtCacheManager.getClipByRbtWavFileName(settings[i]
					.subscriberFile(), browsingLanguage);
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
			//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
			String amount = "0";
			Category categoryObj = rbtCacheManager.getCategory(settings[i].categoryID());
			ChargeClass chargeClassObj = 
					CacheManagerUtil.getChargeClassCacheManager().getChargeClass(settings[i].classType());
			if(chargeClassObj != null) {
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
			boolean isModeAllowedForUGC = isModeAllowedForUGC(task, toneType);
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
			webServiceSubscriberSetting.setSelectionStatusID(selectionStatusID);
			webServiceSubscriberSetting.setSelectedBy(selectedBy);
			webServiceSubscriberSetting.setSelectionInfo(selectionInfo);
			webServiceSubscriberSetting.setDeselectedBy(deselectedBy);
			webServiceSubscriberSetting.setSetTime(setTime);
			webServiceSubscriberSetting.setStartTime(startTime);
			webServiceSubscriberSetting.setEndTime(endTime);
			webServiceSubscriberSetting.setNextChargingDate(nextChargingDate);
			webServiceSubscriberSetting.setRefID(refID);
			webServiceSubscriberSetting.setSelectionType(selectionType);
			webServiceSubscriberSetting
					.setSelectionExtraInfo(selectionExtraInfo);
			webServiceSubscriberSetting.setLoopStatus(settings[i].loopStatus()+"");
			//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
			if(categoryObj != null) {
				webServiceSubscriberSetting.setCategoryName(categoryObj.getCategoryName());
			}
			webServiceSubscriberSetting.setArtistName((clip != null) ? clip.getArtist() : null);
			webServiceSubscriberSetting.setAlbumName((clip != null) ? clip.getAlbum() : null);
			webServiceSubscriberSetting.setTonePrice(amount);
			webServiceSubscriberSetting.setDefaultMusic(false);
			webServiceSubscriberSetting.setClipVcode(clipVcode);
			
			if (currentRefId != null && currentRefId.equalsIgnoreCase(refID)) {
				webServiceSubscriberSetting.setIsCurrentSetting(true);	
			}
			
			// Added for cut rbt support for dtoc
			if(settings[i].subscriberFile().contains("_cut_")){
				String cutSubscriberFileName = settings[i].subscriberFile();
				logger.info("webServiceSubscriberSetting.setCutRBTStartTime"+cutSubscriberFileName.substring(cutSubscriberFileName.lastIndexOf("_")+1));
				webServiceSubscriberSetting.setCutRBTStartTime(cutSubscriberFileName.substring(cutSubscriberFileName.lastIndexOf("_")+1));
			}
			
			//added for udp id 
			if(settings[i].udpId() != null && settings[i].udpId() != "-1"){
				webServiceSubscriberSetting.setUdpId(settings[i].udpId());
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

	protected WebServiceSubscriberSetting createWebServiceSubscriberSettingObject() {
		WebServiceSubscriberSetting webServiceSubscriberSetting = new WebServiceSubscriberSetting();
		return webServiceSubscriberSetting;
	}

	protected Element getSubscriberDownloadsElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberDownload[] webServiceSubscriberDownloads,
			SubscriberDownloads[] downloads) {
		Element element = document.createElement(DOWNLOADS);
		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (webServiceSubscriberDownloads == null) {
			element.setAttribute(NO_OF_DOWNLOADS, "0");
			element.setAttribute(NO_OF_ACTIVE_DOWNLOADS, "0");
		} else {
			element.setAttribute(NO_OF_DOWNLOADS, String
					.valueOf(webServiceSubscriberDownloads.length));

			int noOfActiveDownloads = 0;
			Set<String> provRefIdSet = new HashSet<String>();
			for (WebServiceSubscriberDownload webServiceSubscriberDownload : webServiceSubscriberDownloads) {
				if (webServiceSubscriberDownload.getDownloadStatus()
						.equalsIgnoreCase(ACTIVE))
					noOfActiveDownloads++;
				String downloadInfo = webServiceSubscriberDownload.getDownloadInfo();
				HashMap<String,String> downloadInfoMap = DBUtility.getAttributeMapFromXML(downloadInfo);
				String provRefId = null;
				if(downloadInfoMap!=null){
				     provRefId = downloadInfoMap.get("PROV_REF_ID");
				}
				if(provRefId!=null && provRefIdSet.contains(provRefId)){
					continue;
				}
				if(provRefId!=null){
					provRefIdSet.add(provRefId);
				}
				Element contentElem = getSubscriberDownloadContentElement(
						document, task, webServiceSubscriberDownload);
				contentsElem.appendChild(contentElem);
			}

			element.setAttribute(NO_OF_ACTIVE_DOWNLOADS, String
					.valueOf(noOfActiveDownloads));
		}

		return element;
	}
	protected Element getSubscriberRecentSelElement(Document document,
			WebServiceContext task) {
		Element element = document.createElement(RECENT_SELECTIONS);
		logger.info("Recent Class Type === "+task.getString("RECENT_CLASS_TYPE"));
        if(task.getString("RECENT_CLASS_TYPE")!=null)
		    element.setAttribute(CHARGE_CLASS,task.getString("RECENT_CLASS_TYPE"));
		return element;
	}

	protected Element getSubscriberDownloadContentElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberDownload webServiceSubscriberDownload) {
		Element element = BasicXMLElementGenerator
				.generateSubscriberDownloadContentElement(document, task,
						webServiceSubscriberDownload);
		return element;
	}

	protected WebServiceSubscriberDownload[] getWebServiceSubscriberDownloadObjects(
			WebServiceContext task, SubscriberDownloads[] downloads) {
		if (downloads == null || downloads.length == 0)
			return null;

		WebServiceSubscriberDownload[] webServiceSubscriberDownloads = new WebServiceSubscriberDownload[downloads.length];
		int finalSettingsCount = 0;
		String subscriberID = task.getString(param_subscriberID);
		String browsingLanguage = task.getString(param_browsingLanguage);

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
				category = rbtCacheManager.getCategoryByPromoId(downloads[i]
						.promoId(), browsingLanguage);
				if (category == null)
					throw new NullPointerException(
							"Category does not exist: categoryPromoID "
									+ downloads[i].promoId());
			} else if (Utility.isShuffleCategory(categoryType)) {
				category = rbtCacheManager.getCategory(downloads[i]
						.categoryID(), browsingLanguage);
				if (category == null)
					throw new NullPointerException(
							"Category does not exist: categoryID "
									+ downloads[i].categoryID());
			}

			Clip clip = rbtCacheManager.getClipByRbtWavFileName(downloads[i]
					.promoId(), browsingLanguage);
			if (clip != null) {
				toneID = clip.getClipId();
				toneName = clip.getClipName();
				previewFiles[0] = clip.getClipPreviewWavFile();
				rbtFiles[0] = clip.getClipRbtWavFile();
				vcode = clip.getClipRbtWavFile().replaceAll("rbt_", "").replaceAll("_rbt", "");
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
			//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
			String amount = "0";
			Category categoryObj = rbtCacheManager.getCategory(downloads[i].categoryID());
			ChargeClass chargeClassObj = 
					CacheManagerUtil.getChargeClassCacheManager().getChargeClass(downloads[i].classType());
			if(chargeClassObj != null) {
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
			Date nextBillingDate = downloads[i].nextBillingDate();
			boolean isModeAllowedForUGC = isModeAllowedForUGC(task, toneType);
			logger.info("isModeAllowedForUGC: " + isModeAllowedForUGC);
			if (!isModeAllowedForUGC) {
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
			//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
			if(categoryObj != null) {
			  webServiceSubscriberDownload.setCategoryName(categoryObj.getCategoryName());
			}
			webServiceSubscriberDownload.setArtistName((clip != null) ? clip.getArtist() : null);
			webServiceSubscriberDownload.setAlbumName((clip != null) ? clip.getAlbum() : null);
			webServiceSubscriberDownload.setTonePrice(amount);
			webServiceSubscriberDownload.setDefaultMusic(false);
			webServiceSubscriberDownload.setClipVcode(vcode);
			webServiceSubscriberDownload.setLastChargedDate(lastChargedDate);
			webServiceSubscriberDownload.setNextBillingDate(nextBillingDate);
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

	protected WebServiceSubscriberDownload createWebServiceSubscriberDownloadObject() {
		WebServiceSubscriberDownload webServiceSubscriberDownload = new WebServiceSubscriberDownload();
		return webServiceSubscriberDownload;
	}

	protected Element getSubscriberBookMarksElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks,
			SubscriberDownloads[] bookmarks) {
		Element element = document.createElement(BOOKMARKS);
		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (webServiceSubscriberBookMarks == null) {
			element.setAttribute(NO_OF_BOOKMARKS, "0");
		} else {
			element.setAttribute(NO_OF_BOOKMARKS, String
					.valueOf(webServiceSubscriberBookMarks.length));

			for (WebServiceSubscriberBookMark webServiceSubscriberBookMark : webServiceSubscriberBookMarks) {
				Element contentElem = getSubscriberBookMarkContentElement(
						document, webServiceSubscriberBookMark,task);
				contentsElem.appendChild(contentElem);
			}
		}

		return element;
	}

	protected Element getSubscriberBookMarkContentElement(Document document,
			WebServiceSubscriberBookMark webServiceSubscriberBookMark) {
		return getSubscriberBookMarkContentElement(document, webServiceSubscriberBookMark, null);
	}
	
	protected Element getSubscriberBookMarkContentElement(Document document,
			WebServiceSubscriberBookMark webServiceSubscriberBookMark,WebServiceContext task) {
		Element element = BasicXMLElementGenerator
				.generateSubscriberBookMarkContentElement(document,
						webServiceSubscriberBookMark,task);
		return element;
	}

	protected WebServiceSubscriberBookMark[] getWebServiceSubscriberBookMarkObjects(
			WebServiceContext task, SubscriberDownloads[] bookmarks) {
		if (bookmarks == null || bookmarks.length == 0)
			return null;

		WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = new WebServiceSubscriberBookMark[bookmarks.length];

		String subscriberID = task.getString(param_subscriberID);
		String browsingLanguage = task.getString(param_browsingLanguage);

		for (int i = 0; i < bookmarks.length; i++) {
			int toneID;
			int shuffleID = 0;
			String toneName = null;
			String toneType = null;
			String[] previewFiles = null;
			String[] rbtFiles = null;
            String vcode = null;
			int categoryType = bookmarks[i].categoryType();
			if (categoryType == iRBTConstant.BOUQUET) {
				toneType = CATEGORY_BOUQUET;
				Category category = rbtCacheManager.getCategoryByPromoId(
						bookmarks[i].promoId(), browsingLanguage);
				if (category == null)
					throw new NullPointerException(
							"Category does not exist: categoryPromoID "
									+ bookmarks[i].promoId());

				shuffleID = category.getCategoryId();
				toneName = category.getCategoryName();

				Clip[] clips = rbtCacheManager.getClipsInCategory(shuffleID,
						browsingLanguage);
				if (clips == null || clips.length == 0)
					throw new NullPointerException(
							"No clip under category: categoryID " + shuffleID);

				previewFiles = new String[clips.length + 1];
				rbtFiles = new String[clips.length + 1];

				previewFiles[0] = category.getCategoryPreviewWavFile();
				rbtFiles[0] = category.getCategoryNameWavFile();

				toneID = clips[0].getClipId();
				vcode = clips[0].getClipRbtWavFile().replaceAll("rbt_", "").replaceAll("_rbt", "");
				for (int j = 0; j < clips.length; j++) {
					previewFiles[j + 1] = clips[j].getClipPreviewWavFile();
					rbtFiles[j + 1] = clips[j].getClipRbtWavFile();
				}
			} else if (Utility.isShuffleCategory(categoryType)) {
				toneType = CATEGORY_SHUFFLE;
				shuffleID = bookmarks[i].categoryID();
				Category category = rbtCacheManager.getCategory(shuffleID,
						browsingLanguage);
				if (category == null)
					throw new NullPointerException(
							"Category does not exist: categoryID " + shuffleID);

				toneName = category.getCategoryName();

				Clip[] clips = rbtCacheManager.getClipsInCategory(shuffleID,
						browsingLanguage);
				if (clips == null || clips.length == 0)
					throw new NullPointerException(
							"No clip under category: categoryID " + shuffleID);

				previewFiles = new String[clips.length + 1];
				rbtFiles = new String[clips.length + 1];

				previewFiles[0] = category.getCategoryPreviewWavFile();
				rbtFiles[0] = category.getCategoryNameWavFile();

				toneID = clips[0].getClipId();
				vcode = clips[0].getClipRbtWavFile().replaceAll("rbt_", "").replaceAll("_rbt", "");
				for (int j = 0; j < clips.length; j++) {
					previewFiles[j + 1] = clips[j].getClipPreviewWavFile();
					rbtFiles[j + 1] = clips[j].getClipRbtWavFile();
				}
			} else {
				toneType = CLIP;
				Clip clip = rbtCacheManager.getClipByRbtWavFileName(
						bookmarks[i].promoId(), browsingLanguage);
				if (clip == null)
					throw new NullPointerException(
							"Clip does not exist: clipRBTWavFile "
									+ bookmarks[i].promoId());

				toneID = clip.getClipId();
				toneName = clip.getClipName();
				previewFiles = new String[1];
				previewFiles[0] = clip.getClipPreviewWavFile();
				rbtFiles = new String[1];
				rbtFiles[0] = clip.getClipRbtWavFile();
				vcode = clip.getClipRbtWavFile().replaceAll("rbt_", "").replaceAll("_rbt", "");
			}

			int categoryID = bookmarks[i].categoryID();

			WebServiceSubscriberBookMark webServiceSubscriberBookMark = createWebServiceSubscriberBookMarkObject();
			webServiceSubscriberBookMark.setSubscriberID(subscriberID);
			webServiceSubscriberBookMark.setToneID(toneID);
			webServiceSubscriberBookMark.setShuffleID(shuffleID);
			webServiceSubscriberBookMark.setToneName(toneName);
			webServiceSubscriberBookMark.setToneType(toneType);
			webServiceSubscriberBookMark.setPreviewFiles(previewFiles);
			webServiceSubscriberBookMark.setRbtFiles(rbtFiles);
			webServiceSubscriberBookMark.setCategoryID(categoryID);
			webServiceSubscriberBookMark.setClipVcode(vcode);
			webServiceSubscriberBookMarks[i] = webServiceSubscriberBookMark;
		}

		if (logger.isDebugEnabled())
			logger.debug("RBT:: webServiceSubscriberBookMarks: "
					+ Arrays.toString(webServiceSubscriberBookMarks));
		return webServiceSubscriberBookMarks;
	}

	protected WebServiceSubscriberBookMark createWebServiceSubscriberBookMarkObject() {
		WebServiceSubscriberBookMark webServiceSubscriberBookMark = new WebServiceSubscriberBookMark();
		return webServiceSubscriberBookMark;
	}

	protected Element getSubscriberGroupDetailsElement(Document document,
			WebServiceContext task) {
		Element element = document.createElement(GROUP_DETAILS);

		String subscriberID = task.getString(param_subscriberID);

		Groups[] groups = null;
		if (task.containsKey(param_action)
				&& task.getString(param_action).equalsIgnoreCase(action_get)) {
			
			Boolean isPredifinedGroupIdPresent = task.containsKey(param_predefinedGroupID);
			if (isPredifinedGroupIdPresent && task.get(param_predefinedGroupID).equals("-1")) { //param_predefinedGroupID will be -1
																								//when all groups' members are to be retrieved.
				groups = getGroupsForSubscriberID(subscriberID);
			} else {
				// Getting group based on groupID or predefinedGroupID
				// On other actions(add, remove etc) complete groups details will be
				// returned(else block)
				Groups group = null;
				if (task.containsKey(param_groupID)) {
					String groupIDStr = task.getString(param_groupID)
							.substring(1); // Trimming
											// 'G'
											// from
											// groupID
					int groupID = Integer.parseInt(groupIDStr);
					group = rbtDBManager.getGroup(groupID);
				} else if (isPredifinedGroupIdPresent) {
					String predefinedGroupID = task
							.getString(param_predefinedGroupID);
					group = rbtDBManager.getGroupByPreGroupID(
							predefinedGroupID, subscriberID);
				}
				if (group != null) {
					groups = new Groups[1];
					groups[0] = group;
				}
			}
		} else {
			groups = getGroupsForSubscriberID(subscriberID);
		}

		HashMap<String, GroupMembers[]> groupMembersMap = DataUtils
				.getGroupMembersByGroupID(groups);

		HashMap<String, WebServiceGroupMember[]> webServiceGroupMembersMap = getWebServiceGroupMemberObjects(
				task, groupMembersMap);
		WebServiceGroup[] webServiceGroups = getWebServiceGroupObjects(task,
				groups, webServiceGroupMembersMap);

		Element groupsElem = getSubscriberGroupsElement(document, task,
				webServiceGroups, groups);
		element.appendChild(groupsElem);

		Element groupMembersElem = getSubscriberGroupMembersElement(document,
				task, webServiceGroupMembersMap, groupMembersMap);
		element.appendChild(groupMembersElem);

		return element;
	}

	private Groups[] getGroupsForSubscriberID(String subscriberID) {
		Groups[] groups;
		groups = rbtDBManager.getGroupsForSubscriberID(subscriberID);
		if (groups != null && groups.length > 0) {
			List<Groups> groupsList = new ArrayList<Groups>();
			for (Groups group : groups) {
				if (group.preGroupID() == null
						|| !group.preGroupID().equals("99")) // Ignoring
																// Blocked
																// Callers
																// Group
					groupsList.add(group);
			}
			groups = groupsList.toArray(new Groups[0]);
		}
		return groups;
	}
	
	protected Element getSubscriberAffiliateGroupDetailsElement(Document document,
			WebServiceContext task) {
		Element element = document.createElement(GROUP_DETAILS);

		String subscriberID = task.getString(param_subscriberID);

		RDCGroups[] groups = null;
		if (task.containsKey(param_action)
				&& task.getString(param_action).equalsIgnoreCase(action_get)) {
			// Getting group based on groupID or predefinedGroupID
			// On other actions(add, remove etc) complete groups details will be
			// returned(else block)
			RDCGroups group = null;
			if (task.containsKey(param_groupID)) {
				String groupIDStr = task.getString(param_groupID).substring(1); // Trimming
																				// 'G'
																				// from
																				// groupID
				int groupID = Integer.parseInt(groupIDStr);
				group = rbtDBManager.getRDCGroup(groupID);
			} else if (task.containsKey(param_predefinedGroupID)) {
				String predefinedGroupID = task
						.getString(param_predefinedGroupID);
				group = rbtDBManager.getRDCGroupByPreGroupID(predefinedGroupID,
						subscriberID);
			}

			if (group != null) {
				groups = new RDCGroups[1];
				groups[0] = group;
			}
		} else if (task.containsKey("groupRefID")) {
			RDCGroups group = null;
			group = rbtDBManager.getRDCGroupByRefID(task.getString("groupRefID"));
			if (group != null) {
				groups = new RDCGroups[1];
				groups[0] = group;
			}
		} else {
			groups = rbtDBManager.getAffiliateGroupsForSubscriberID(subscriberID);
			if (groups != null && groups.length > 0) {
				List<RDCGroups> groupsList = new ArrayList<RDCGroups>();
				for (RDCGroups group : groups) {
					if (group.preGroupID() == null
							|| !group.preGroupID().equals("99")) // Ignoring
																	// Blocked
																	// Callers
																	// Group
						groupsList.add(group);
				}
				groups = groupsList.toArray(new RDCGroups[0]);
			}
		}

		HashMap<String, RDCGroupMembers[]> groupMembersMap = DataUtils
				.getAffiliateGroupMembersByGroupID(groups);

		HashMap<String, WebServiceGroupMember[]> webServiceGroupMembersMap = getWebServiceAffiliateGroupMemberObjects(
				task, groupMembersMap);
		WebServiceGroup[] webServiceGroups = getWebServiceAffiliateGroupObjects(task,
				groups, webServiceGroupMembersMap);

		Element groupsElem = getSubscriberAffiliateGroupsElement(document, task,
				webServiceGroups, groups);
		element.appendChild(groupsElem);

		Element groupMembersElem = getSubscriberAffiliateGroupMembersElement(document,
				task, webServiceGroupMembersMap, groupMembersMap);
		element.appendChild(groupMembersElem);

		return element;
	}

	protected Element getSubscriberGroupsElement(Document document,
			WebServiceContext task, WebServiceGroup[] webServiceGroups,
			Groups[] groups) {
		Element element = document.createElement(GROUPS);
		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (webServiceGroups == null) {
			element.setAttribute(NO_OF_GROUPS, "0");
			element.setAttribute(NO_OF_ACTIVE_GROUPS, "0");
		} else {
			element.setAttribute(NO_OF_GROUPS, String
					.valueOf(webServiceGroups.length));

			int noOfActiveGroups = 0;
			for (WebServiceGroup webServiceGroup : webServiceGroups) {
				if (webServiceGroup.getGroupStatus().equalsIgnoreCase(ACTIVE))
					noOfActiveGroups++;

				Element contentElem = getGroupContentElement(document,
						webServiceGroup,task);
				contentsElem.appendChild(contentElem);
			}

			element.setAttribute(NO_OF_ACTIVE_GROUPS, String
					.valueOf(noOfActiveGroups));
		}

		return element;
	}
	
	protected Element getSubscriberAffiliateGroupsElement(Document document,
			WebServiceContext task, WebServiceGroup[] webServiceGroups,
			RDCGroups[] groups) {
		Element element = document.createElement(GROUPS);
		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (webServiceGroups == null) {
			element.setAttribute(NO_OF_GROUPS, "0");
			element.setAttribute(NO_OF_ACTIVE_GROUPS, "0");
		} else {
			element.setAttribute(NO_OF_GROUPS, String
					.valueOf(webServiceGroups.length));

			int noOfActiveGroups = 0;
			for (WebServiceGroup webServiceGroup : webServiceGroups) {
				if (webServiceGroup.getGroupStatus().equalsIgnoreCase(ACTIVE))
					noOfActiveGroups++;

				Element contentElem = getGroupContentElement(document,
						webServiceGroup,task);
				contentsElem.appendChild(contentElem);
			}

			element.setAttribute(NO_OF_ACTIVE_GROUPS, String
					.valueOf(noOfActiveGroups));
		}

		return element;
	}

	protected Element getGroupContentElement(Document document,
			WebServiceGroup webServiceGroup) {
		return getGroupContentElement(document, webServiceGroup, null);
	}
	
	protected Element getGroupContentElement(Document document,
			WebServiceGroup webServiceGroup,WebServiceContext task) {
		Element element = BasicXMLElementGenerator.generateGroupContentElement(
				document, webServiceGroup, task);
		return element;
	}

	protected WebServiceGroup[] getWebServiceGroupObjects(
			WebServiceContext task, Groups[] groups,
			HashMap<String, WebServiceGroupMember[]> webServiceGroupMembersMap) {
		if (groups == null || groups.length == 0)
			return null;

		List<WebServiceGroup> list = new ArrayList<WebServiceGroup>();
		for (int i = 0; i < groups.length; i++) {
			String grpStatus = groups[i].status();
			if (grpStatus.equals(iRBTConstant.STATE_DEACTIVATED))
				continue;

			String subscriberID = groups[i].subID();
			String groupID = "G" + groups[i].groupID();
			String groupName = groups[i].groupName();
			String groupPromoID = groups[i].groupPromoID();
			String predefinedGroupID = groups[i].preGroupID();
			String groupStatus = null;

			if (grpStatus.equals(iRBTConstant.STATE_BASE_ACTIVATION_PENDING)
					|| grpStatus.equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
					|| grpStatus.equals(iRBTConstant.STATE_ACTIVATION_PENDING))
				groupStatus = ACT_PENDING;
			else if (grpStatus.equals(iRBTConstant.STATE_ACTIVATED))
				groupStatus = ACTIVE;
			else if (grpStatus.equals(iRBTConstant.STATE_TO_BE_DEACTIVATED)
					|| grpStatus
							.equals(iRBTConstant.STATE_DEACTIVATION_PENDING))
				groupStatus = DEACT_PENDING;
			else
				groupStatus = ERROR;

			String groupNamePrompt = groupName.replaceAll(" ", "_");

			int noOfMembers = 0;
			WebServiceGroupMember[] webServiceGroupMembers = webServiceGroupMembersMap
					.get(groupID);
			if (webServiceGroupMembers != null)
				noOfMembers = webServiceGroupMembers.length;

			WebServiceGroup webServiceGroup = createWebServiceGroupkObject();
			webServiceGroup.setSubscriberID(subscriberID);
			webServiceGroup.setGroupID(groupID);
			webServiceGroup.setGroupName(groupName);
			webServiceGroup.setGroupPromoID(groupPromoID);
			webServiceGroup.setPredefinedGroupID(predefinedGroupID);
			webServiceGroup.setGroupStatus(groupStatus);
			webServiceGroup.setGroupNamePrompt(groupNamePrompt);
			webServiceGroup.setNoOfMembers(noOfMembers);

			list.add(webServiceGroup);
		}

		if (logger.isDebugEnabled())
			logger.debug("RBT:: webServiceGroups: " + list);

		WebServiceGroup[] webServiceGroups = null;
		if (list.size() > 0)
			webServiceGroups = list.toArray(new WebServiceGroup[0]);

		return webServiceGroups;
	}
	
	protected WebServiceGroup[] getWebServiceAffiliateGroupObjects(
			WebServiceContext task, RDCGroups[] groups,
			HashMap<String, WebServiceGroupMember[]> webServiceGroupMembersMap) {
		if (groups == null || groups.length == 0)
			return null;

		List<WebServiceGroup> list = new ArrayList<WebServiceGroup>();
		for (int i = 0; i < groups.length; i++) {
			String grpStatus = groups[i].status();
			if (grpStatus.equals(iRBTConstant.STATE_DEACTIVATED))
				continue;

			String subscriberID = groups[i].subID();
			String groupID = "G" + groups[i].groupID();
			String groupName = groups[i].groupName();
			String groupPromoID = groups[i].groupPromoID();
			String predefinedGroupID = groups[i].preGroupID();
			String groupStatus = null;

			if (grpStatus.equals(iRBTConstant.STATE_BASE_ACTIVATION_PENDING)
					|| grpStatus.equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
					|| grpStatus.equals(iRBTConstant.STATE_ACTIVATION_PENDING))
				groupStatus = ACT_PENDING;
			else if (grpStatus.equals(iRBTConstant.STATE_ACTIVATED))
				groupStatus = ACTIVE;
			else if (grpStatus.equals(iRBTConstant.STATE_TO_BE_DEACTIVATED)
					|| grpStatus
							.equals(iRBTConstant.STATE_DEACTIVATION_PENDING))
				groupStatus = DEACT_PENDING;
			else
				groupStatus = ERROR;

			String groupNamePrompt = groupName.replaceAll(" ", "_");

			int noOfMembers = 0;
			WebServiceGroupMember[] webServiceGroupMembers = webServiceGroupMembersMap
					.get(groupID);
			if (webServiceGroupMembers != null)
				noOfMembers = webServiceGroupMembers.length;

			WebServiceGroup webServiceGroup = createWebServiceGroupkObject();
			webServiceGroup.setSubscriberID(subscriberID);
			webServiceGroup.setGroupID(groupID);
			webServiceGroup.setGroupName(groupName);
			webServiceGroup.setGroupPromoID(groupPromoID);
			webServiceGroup.setPredefinedGroupID(predefinedGroupID);
			webServiceGroup.setGroupStatus(groupStatus);
			webServiceGroup.setGroupNamePrompt(groupNamePrompt);
			webServiceGroup.setNoOfMembers(noOfMembers);

			list.add(webServiceGroup);
		}

		if (logger.isDebugEnabled())
			logger.debug("RBT:: webServiceGroups: " + list);

		WebServiceGroup[] webServiceGroups = null;
		if (list.size() > 0)
			webServiceGroups = list.toArray(new WebServiceGroup[0]);

		return webServiceGroups;
	}

	protected WebServiceGroup createWebServiceGroupkObject() {
		WebServiceGroup webServiceGroup = new WebServiceGroup();
		return webServiceGroup;
	}

	protected Element getSubscriberGroupMembersElement(Document document,
			WebServiceContext task,
			HashMap<String, WebServiceGroupMember[]> webServiceGroupMembersMap,
			HashMap<String, GroupMembers[]> groupMembersMap) {
		Element element = document.createElement(GROUP_MEMBERS);

		String allMembers = "";

		if (webServiceGroupMembersMap != null) {
			Set<String> keySet = webServiceGroupMembersMap.keySet();
			for (String groupID : keySet) {
				Element groupElem = document.createElement(GROUP + "_"
						+ groupID);
				element.appendChild(groupElem);

				Element contentsElem = document.createElement(CONTENTS);
				groupElem.appendChild(contentsElem);

				int noOfActiveMembers = 0;
				WebServiceGroupMember[] webServiceGroupMembers = webServiceGroupMembersMap
						.get(groupID);
				for (WebServiceGroupMember webServiceGroupMember : webServiceGroupMembers) {
					allMembers += webServiceGroupMember.getMemberID() + ",";

					if (webServiceGroupMember.getMemberStatus()
							.equalsIgnoreCase(ACTIVE))
						noOfActiveMembers++;

					Element contentElem = getGroupMemberContentElement(
							document, webServiceGroupMember);
					contentsElem.appendChild(contentElem);
				}

				groupElem.setAttribute(NO_OF_MEMBERS, String
						.valueOf(webServiceGroupMembers.length));
				groupElem.setAttribute(NO_OF_ACTIVE_MEMBERS, String
						.valueOf(noOfActiveMembers));
			}
		}

		if (!allMembers.equals("")) {
			allMembers = allMembers.substring(0, allMembers.lastIndexOf(","));
		}
		element.setAttribute(ALL_MEMBERS, allMembers);

		return element;
	}
	
	protected Element getSubscriberAffiliateGroupMembersElement(Document document,
			WebServiceContext task,
			HashMap<String, WebServiceGroupMember[]> webServiceGroupMembersMap,
			HashMap<String, RDCGroupMembers[]> groupMembersMap) {
		Element element = document.createElement(GROUP_MEMBERS);

		String allMembers = "";

		if (webServiceGroupMembersMap != null) {
			Set<String> keySet = webServiceGroupMembersMap.keySet();
			for (String groupID : keySet) {
				Element groupElem = document.createElement(GROUP + "_"
						+ groupID);
				element.appendChild(groupElem);

				Element contentsElem = document.createElement(CONTENTS);
				groupElem.appendChild(contentsElem);

				int noOfActiveMembers = 0;
				WebServiceGroupMember[] webServiceGroupMembers = webServiceGroupMembersMap
						.get(groupID);
				for (WebServiceGroupMember webServiceGroupMember : webServiceGroupMembers) {
					allMembers += webServiceGroupMember.getMemberID() + ",";

					if (webServiceGroupMember.getMemberStatus()
							.equalsIgnoreCase(ACTIVE))
						noOfActiveMembers++;

					Element contentElem = getGroupMemberContentElement(
							document, webServiceGroupMember);
					contentsElem.appendChild(contentElem);
				}

				groupElem.setAttribute(NO_OF_MEMBERS, String
						.valueOf(webServiceGroupMembers.length));
				groupElem.setAttribute(NO_OF_ACTIVE_MEMBERS, String
						.valueOf(noOfActiveMembers));
			}
		}

		if (!allMembers.equals("")) {
			allMembers = allMembers.substring(0, allMembers.lastIndexOf(","));
		}
		element.setAttribute(ALL_MEMBERS, allMembers);

		return element;
	}

	protected Element getGroupMemberContentElement(Document document,
			WebServiceGroupMember webServiceGroupMember) {
		Element element = BasicXMLElementGenerator
				.generateGroupMemberContentElement(document,
						webServiceGroupMember);
		return element;
	}

	protected HashMap<String, WebServiceGroupMember[]> getWebServiceGroupMemberObjects(
			WebServiceContext task,
			HashMap<String, GroupMembers[]> groupMembersMap) {
		if (groupMembersMap == null)
			return null;

		HashMap<String, WebServiceGroupMember[]> webServiceGroupMembersMap = new HashMap<String, WebServiceGroupMember[]>();

		Set<String> keySet = groupMembersMap.keySet();
		for (String groupID : keySet) {
			GroupMembers[] groupMembers = groupMembersMap.get(groupID);

			List<WebServiceGroupMember> list = new ArrayList<WebServiceGroupMember>();
			for (int i = 0; i < groupMembers.length; i++) {
				String mbrStatus = groupMembers[i].status();
				if (mbrStatus.equals(iRBTConstant.STATE_DEACTIVATED))
					continue;

				String memberID = groupMembers[i].callerID();
				String memberName = groupMembers[i].callerName();
				String memberStatus = null;

				if (mbrStatus
						.equals(iRBTConstant.STATE_BASE_ACTIVATION_PENDING)
						|| mbrStatus.equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
						|| mbrStatus
								.equals(iRBTConstant.STATE_ACTIVATION_PENDING))
					memberStatus = ACT_PENDING;
				else if (mbrStatus.equals(iRBTConstant.STATE_ACTIVATED))
					memberStatus = ACTIVE;
				else if (mbrStatus.equals(iRBTConstant.STATE_TO_BE_DEACTIVATED)
						|| mbrStatus
								.equals(iRBTConstant.STATE_DEACTIVATION_PENDING))
					memberStatus = DEACT_PENDING;
				else
					memberStatus = ERROR;

				WebServiceGroupMember webServiceGroupMember = createWebServiceGroupMemberkObject();
				webServiceGroupMember.setGroupID(groupID);
				webServiceGroupMember.setMemberID(memberID);
				webServiceGroupMember.setMemberName(memberName);
				webServiceGroupMember.setMemberStatus(memberStatus);

				list.add(webServiceGroupMember);
			}

			WebServiceGroupMember[] webServiceGroupMembers = null;
			if (list.size() > 0) {
				webServiceGroupMembers = list
						.toArray(new WebServiceGroupMember[0]);
				webServiceGroupMembersMap.put(groupID, webServiceGroupMembers);
				if (logger.isDebugEnabled())
					logger.debug("RBT:: webServiceGroupMembers of Group "
							+ groupID + ":  " + list);
			}
		}

		return webServiceGroupMembersMap;
	}

	protected HashMap<String, WebServiceGroupMember[]> getWebServiceAffiliateGroupMemberObjects(
			WebServiceContext task,
			HashMap<String, RDCGroupMembers[]> groupMembersMap) {
		if (groupMembersMap == null)
			return null;

		HashMap<String, WebServiceGroupMember[]> webServiceGroupMembersMap = new HashMap<String, WebServiceGroupMember[]>();

		Set<String> keySet = groupMembersMap.keySet();
		for (String groupID : keySet) {
			RDCGroupMembers[] groupMembers = groupMembersMap.get(groupID);

			List<WebServiceGroupMember> list = new ArrayList<WebServiceGroupMember>();
			for (int i = 0; i < groupMembers.length; i++) {
				String mbrStatus = groupMembers[i].status();
				if (mbrStatus.equals(iRBTConstant.STATE_DEACTIVATED))
					continue;

				String memberID = groupMembers[i].callerID();
				String memberName = groupMembers[i].callerName();
				String memberStatus = null;

				if (mbrStatus
						.equals(iRBTConstant.STATE_BASE_ACTIVATION_PENDING)
						|| mbrStatus.equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
						|| mbrStatus
								.equals(iRBTConstant.STATE_ACTIVATION_PENDING))
					memberStatus = ACT_PENDING;
				else if (mbrStatus.equals(iRBTConstant.STATE_ACTIVATED))
					memberStatus = ACTIVE;
				else if (mbrStatus.equals(iRBTConstant.STATE_TO_BE_DEACTIVATED)
						|| mbrStatus
								.equals(iRBTConstant.STATE_DEACTIVATION_PENDING))
					memberStatus = DEACT_PENDING;
				else
					memberStatus = ERROR;

				WebServiceGroupMember webServiceGroupMember = createWebServiceGroupMemberkObject();
				webServiceGroupMember.setGroupID(groupID);
				webServiceGroupMember.setMemberID(memberID);
				webServiceGroupMember.setMemberName(memberName);
				webServiceGroupMember.setMemberStatus(memberStatus);

				list.add(webServiceGroupMember);
			}

			WebServiceGroupMember[] webServiceGroupMembers = null;
			if (list.size() > 0) {
				webServiceGroupMembers = list
						.toArray(new WebServiceGroupMember[0]);
				webServiceGroupMembersMap.put(groupID, webServiceGroupMembers);
				if (logger.isDebugEnabled())
					logger.debug("RBT:: webServiceGroupMembers of Group "
							+ groupID + ":  " + list);
			}
		}

		return webServiceGroupMembersMap;
	}

	protected WebServiceGroupMember createWebServiceGroupMemberkObject() {
		WebServiceGroupMember webServiceGroupMember = new WebServiceGroupMember();
		return webServiceGroupMember;
	}

	protected Element getCopyDetailsElement(Document document,
			WebServiceContext task) {
		WebServiceCopyData[] webServiceCopyDatas = getWebServiceCopyDataObjects(task);

		Element element = document.createElement(COPY_DETAILS);
		element.setAttribute(RESULT, webServiceCopyDatas[0].getResult());
		element.setAttribute(SUBSCRIBER_ID, webServiceCopyDatas[0]
				.getSubscriberID());
		element.setAttribute(FROM_SUBSCRIBER, webServiceCopyDatas[0]
				.getFromSubscriber());

		if (task.containsKey(param_userHasMultipleSelections))
			element.setAttribute(USER_HAS_MULTIPLE_SELECTIONS, task
					.getString(param_userHasMultipleSelections));

		if (webServiceCopyDatas[0].getResult().equalsIgnoreCase(SUCCESS)
				|| webServiceCopyDatas[0].getResult().equalsIgnoreCase(
						DEFAULT_RBT)
				|| webServiceCopyDatas[0].getResult().equalsIgnoreCase(
						ALBUM_RBT)) {
			Element contentsElem = document.createElement(CONTENTS);
			contentsElem.setAttribute(NO_OF_CONTENTS, String
					.valueOf(webServiceCopyDatas.length));
			element.appendChild(contentsElem);

			for (WebServiceCopyData webServiceCopyData : webServiceCopyDatas) {
				Element contentElem = getCopyContentElement(document,
						webServiceCopyData,task);
				contentsElem.appendChild(contentElem);
			}
		}

		return element;
	}

	protected Element getCopyContentElement(Document document,
			WebServiceCopyData webServiceCopyData) {
		return getCopyContentElement(document, webServiceCopyData, null);
	}
	
	protected Element getCopyContentElement(Document document,
			WebServiceCopyData webServiceCopyData, WebServiceContext task) {
		Element element = BasicXMLElementGenerator
				.generateCopyDetailsContentElement(document, webServiceCopyData, task);
		return element;
	}
	
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
		String amount = "0";
		String period = "";
		String chargeClass = "DEFAULT";

		if (rbtDBManager.isSubscriberDeactivated(fromSubscriberObj)
				|| rbtDBManager.isSubscriberSuspended(fromSubscriberObj)) {
			result = NOT_RBT_USER;
		} else {
			boolean useSubManager = true;
			SubscriberStatus[] subscriberStatuses = rbtDBManager
					.getSubscriberRecords(fromSubscriber, "VUI", useSubManager);

			if (subscriberStatuses == null) {
				result = DEFAULT_RBT;
			} else {
				ArrayList<SubscriberStatus> defaultCallerList = new ArrayList<SubscriberStatus>();
				ArrayList<SubscriberStatus> specialCallerList = new ArrayList<SubscriberStatus>();
				for (SubscriberStatus subscriberStatus : subscriberStatuses) {
					if (subscriberStatus.selType() == 2)
						continue; // Ignoring corporate selections

					if (subscriberStatus.callerID() == null)
						defaultCallerList.add(subscriberStatus);
					else if (subscriberStatus.callerID().equalsIgnoreCase(
							subscriberID))
						specialCallerList.add(subscriberStatus);
				}

				SubscriberStatus subStatus = null;
				if (specialCallerList.size() > 0) {
					subStatus = specialCallerList.get(0);

					if (specialCallerList.size() > 1)
						task.put(param_userHasMultipleSelections, YES);
				} else if (defaultCallerList.size() > 0) {
					subStatus = defaultCallerList.get(0);

					if (defaultCallerList.size() > 1)
						task.put(param_userHasMultipleSelections, YES);
				}

				if (subStatus != null) {
					Category category = rbtCacheManager.getCategory(subStatus
							.categoryID(), browsingLanguage);
					status = subStatus.status();

					if (category != null) {
						if (category.getCategoryTpe() == iRBTConstant.RECORD
								|| category.getCategoryTpe() == iRBTConstant.KARAOKE) {
							result = PERSONAL_MESSAGE;
						} else {
							Clip clip = rbtCacheManager
									.getClipByRbtWavFileName(subStatus
											.subscriberFile(), browsingLanguage);
							if (clip != null) {
								result = SUCCESS;
								categoryID = category.getCategoryId();
								toneID = clip.getClipId();
								toneName = clip.getClipName();

								if (Utility.isShuffleCategory(category
										.getCategoryTpe())) {
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
												.valueOf(toneID)))
											result = NOT_ALLOWED;
									}

									Category copyCategory = rbtCacheManager
											.getCategory(26, browsingLanguage);
									if (copyCategory != null)
										categoryID = 26;
								}

								if (category.getClassType() != null) {
									chargeClass = category.getClassType();
									ChargeClass classType = CacheManagerUtil
											.getChargeClassCacheManager()
											.getChargeClass(chargeClass);
									if (classType != null) {
										amount = classType.getAmount();
										period = classType.getSelectionPeriod();
									}
								}
								if (Utility.isShuffleCategory(category
										.getCategoryTpe()))
									previewFile = category
											.getCategoryPreviewWavFile();
								else
									previewFile = clip.getClipPreviewWavFile();
							}
						}
					}
				} else {
					result = DEFAULT_RBT;
				}
			}
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
		webServiceCopyData.setAmount(amount);
		webServiceCopyData.setPeriod(period);
		webServiceCopyData.setChargeClass(chargeClass);

		if (logger.isDebugEnabled())
			logger.debug("RBT:: webServiceCopyData: " + webServiceCopyData);

		WebServiceCopyData[] webServiceCopyDatas = { webServiceCopyData };
		return webServiceCopyDatas;
	}

	protected WebServiceCopyData createWebServiceCopyDataObject() {
		WebServiceCopyData webServiceCopyData = new WebServiceCopyData();
		return webServiceCopyData;
	}

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
		} else if (action.equalsIgnoreCase(action_gift) || action.equalsIgnoreCase(action_sendGift) ) {
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
				}  else {
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

		Element element = Utility.getResponseElement(document, response);
		if (status != null && !status.equalsIgnoreCase(VALID)
				&& !status.equalsIgnoreCase(INVALID)) {
			element.setAttribute(STATUS, status);
		}
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

	protected Element getSubscriberSMSHistoryElement(Document document,
			WebServiceContext task) {
		Element element = BasicXMLElementGenerator.generateSMSHistoryElement(
				document, task);
		return element;
	}

	protected Element getSubscriberWCHistoryElement(Document document,
			WebServiceContext task) {
		Element element = BasicXMLElementGenerator.generateWCHistoryElement(
				document, task);
		return element;
	}

	protected Element getSubscriberTransactionHistoryElement(Document document,
			WebServiceContext task) {
		Document transactionDetailsDocument = null;

		try {
			String subscriberID = task.getString(param_subscriberID);

			Parameters subMgrTransDetailsURLParam = parametersCacheManager
					.getParameter(iRBTConstant.COMMON,
							"SUBMGR_URL_FOR_TRANSACTION_DETAILS");
			String url = subMgrTransDetailsURLParam.getValue().trim();
			url = url.replaceAll("%SUBSCRIBER_ID%", subscriberID);

			HttpParameters httpParameters = new HttpParameters(url);
			Utility.setSubMgrProxy(httpParameters);
			logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			logger.info("RBT:: httpResponse: " + httpResponse);

			if (httpResponse != null && httpResponse.getResponse() != null)
				transactionDetailsDocument = XMLUtils
						.getDocumentFromString(httpResponse.getResponse());
		} catch (Exception e) {
			logger.error("", e);
		}

		Element element = BasicXMLElementGenerator
				.generateTransactionHistoryElement(document,
						transactionDetailsDocument, task);
		return element;
	}

	protected Element getCallDetailsElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber,
			Subscriber subscriber) {
		Element element = BasicXMLElementGenerator.generateCallDetailsElement(
				document, task, webServiceSubscriber);
		return element;
	}

	protected Element getSubscriberPromoElement(Document document,
			WebServiceContext task) {
		SubscriberPromo subscriberPromo = null;
		if (task.containsKey(param_subscriberPromo))
			subscriberPromo = (SubscriberPromo) task.get(param_subscriberPromo);
		else {
			String subscriberID = task.getString(param_subscriberID);
			String activatedBy = task.getString(param_mode);
			String type = task.getString(param_type);

			subscriberPromo = rbtDBManager.getSubscriberPromo(subscriberID,
					activatedBy, type);
		}

		Element subscriberPromoElem = BasicXMLElementGenerator
				.generateSubscriberPromoElement(document, subscriberPromo);
		return subscriberPromoElem;
	}

	protected Element getOperatorUserInfoElement(Document document,
			WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		boolean isPrepaid = DataUtils.isUserPrepaid(task);
		String status = rbtDBManager.getBackEndSubscriberStatus(subscriberID,
				isPrepaid);

		Element operatorUserInfoElem = document
				.createElement(OPERATOR_USER_INFO);
		operatorUserInfoElem.setAttribute("STATUS", status);

		return operatorUserInfoElem;
	}

	protected Element getParametersElement(Document document,
			WebServiceContext task) {
		String type = task.getString(param_type);
		String name = task.getString(param_name);

		Parameters[] parameters = null;
		if (type == null) {
			List<Parameters> parametersList = parametersCacheManager
					.getAllParameters();
			if (parametersList != null && parametersList.size() > 0)
				parameters = parametersList.toArray(new Parameters[0]);
		} else if (name != null) {
			Parameters parameter = parametersCacheManager.getParameter(type,
					name, null);
			if (parameter != null) {
				parameters = new Parameters[1];
				parameters[0] = parameter;
			}
		} else {
			List<Parameters> parametersList = parametersCacheManager
					.getParameters(type);
			if (parametersList != null && parametersList.size() > 0)
				parameters = parametersList.toArray(new Parameters[0]);
		}

		Element element = BasicXMLElementGenerator.generateParametersElement(
				document, parameters);
		return element;
	}

	protected Element getSubscriptionClassesElement(Document document,
			WebServiceContext task) {
		String name = task.getString(param_name);

		SubscriptionClassCacheManager subscriptionClassCacheManager = CacheManagerUtil
				.getSubscriptionClassCacheManager();
		SubscriptionClass[] subscriptionClasses = null;
		if (name != null) {
			SubscriptionClass subscriptionClass = subscriptionClassCacheManager
					.getSubscriptionClass(name);
			if (subscriptionClass != null) {
				subscriptionClasses = new SubscriptionClass[1];
				subscriptionClasses[0] = subscriptionClass;
			}
		} else {
			List<SubscriptionClass> subscriptionClassList = subscriptionClassCacheManager
					.getAllSubscriptionClasses();
			if (subscriptionClassList != null)
				subscriptionClasses = subscriptionClassList
						.toArray(new SubscriptionClass[0]);
		}

		Element element = BasicXMLElementGenerator
				.generateSubscriptionClassesElement(document,
						subscriptionClasses);
		return element;
	}

	protected Element getChargeClassesElement(Document document,
			WebServiceContext task) {
		String name = task.getString(param_name);

		ChargeClassCacheManager chargeClassCacheManager = CacheManagerUtil
				.getChargeClassCacheManager();
		ChargeClass[] chargeClasses = null;
		if (name != null) {
			ChargeClass chargeClass = chargeClassCacheManager
					.getChargeClass(name);
			if (chargeClass != null) {
				chargeClasses = new ChargeClass[1];
				chargeClasses[0] = chargeClass;
			}
		} else {
			List<ChargeClass> chargeClassList = chargeClassCacheManager
					.getAllChargeClass();
			if (chargeClassList != null)
				chargeClasses = chargeClassList.toArray(new ChargeClass[0]);
		}

		Element element = BasicXMLElementGenerator
				.generateChargeClassesElement(document, chargeClasses);
		return element;
	}

	protected Element getSMSTextsElement(Document document,
			WebServiceContext task) {
		String type = task.getString(param_type);
		String name = task.getString(param_name);

		BulkPromoSMSCacheManager bulkPromoSMSCacheManager = CacheManagerUtil
				.getBulkPromoSMSCacheManager();
		BulkPromoSMS[] bulkPromoSMSes = null;
		if (name != null && type != null) {
			BulkPromoSMS bulkPromoSMS = bulkPromoSMSCacheManager
					.getBulkPromoSMSForDate(type, name);
			if (bulkPromoSMS != null) {
				bulkPromoSMSes = new BulkPromoSMS[1];
				bulkPromoSMSes[0] = bulkPromoSMS;
			}
		} else if (type != null) {
			List<BulkPromoSMS> bulkPromoSMSList = bulkPromoSMSCacheManager
					.getBulkPromoSMSes(type);
			if (bulkPromoSMSList != null)
				bulkPromoSMSes = bulkPromoSMSList.toArray(new BulkPromoSMS[0]);
		} else {
			List<BulkPromoSMS> bulkPromoSMSList = bulkPromoSMSCacheManager
					.getAllBulkPromoSMS();
			if (bulkPromoSMSList != null)
				bulkPromoSMSes = bulkPromoSMSList.toArray(new BulkPromoSMS[0]);
		}

		Element element = BasicXMLElementGenerator.generateSMSTextsElement(
				document, bulkPromoSMSes);
		return element;
	}

	protected Element getPickOfTheDaysElement(Document document,
			WebServiceContext task) {
		String range = task.getString(param_range);
		String circleID = task.getString(param_circleID);
		PickOfTheDay[] pickOfTheDays = rbtDBManager.getPickOfTheDays(range,
				circleID);

		Element element = BasicXMLElementGenerator
				.generatePickOfTheDaySElement(document, task, pickOfTheDays);
		return element;
	}

	protected Element getRBTLoginUserElement(Document document,
			WebServiceContext task) {
		Element element = document.createElement(RBT_LOGIN_USERS);
		String userID = task.getString(param_userID);
		String password = task.getString(param_password);
		String subscriberID = task.getString(param_subscriberID);
		String type = task.getString(param_type);
		Boolean newUser = Boolean.valueOf(task.getString("IS_NEW_USER"));
		boolean encryptPassword = task.containsKey(param_encryptPassword)
				&& task.getString(param_encryptPassword).equalsIgnoreCase(YES);
		
		String info = task.getString(param_info);
		if(info.equalsIgnoreCase(RBT_OTP_LOGIN)) {
			encryptPassword = ("n".equalsIgnoreCase(task.getString(param_encryptPassword))) ? false : true;
		}

		HashMap<String, String> userInfo = new HashMap<String, String>();
		Set<String> keySet = task.keySet();
		for (String key : keySet) {
			if (key.startsWith(param_userInfo))
				userInfo.put(key.substring(key.indexOf('_') + 1), task
						.getString(key));
		}

		RBTLoginUser rbtLoginUser = rbtDBManager.getRBTLoginUser(userID,
				password, subscriberID, type, userInfo, encryptPassword);
		Element rbtLoginUserelement = BasicXMLElementGenerator
				.generateRBTLoginUserElement(document, rbtLoginUser, newUser);
		if (rbtLoginUserelement != null)
			element.appendChild(rbtLoginUserelement);

		return element;
	}

	protected Element getSitesElement(Document document, WebServiceContext task) {
		String circleID = task.getString(param_circleID);
		SitePrefix[] sitePrefixes = null;
		if (circleID != null) {
			SitePrefix sitePrefix = CacheManagerUtil
					.getSitePrefixCacheManager().getSitePrefixes(circleID);
			if (sitePrefix != null) {
				sitePrefixes = new SitePrefix[1];
				sitePrefixes[0] = sitePrefix;
			}
		} else {
			List<SitePrefix> sitePrefixList = CacheManagerUtil
					.getSitePrefixCacheManager().getAllSitePrefix();
			if (sitePrefixList != null)
				sitePrefixes = sitePrefixList.toArray(new SitePrefix[0]);
		}

		Element element = BasicXMLElementGenerator.generateSitesElement(
				document, sitePrefixes);
		return element;
	}

	protected Element getChargeSMSElement(Document document,
			WebServiceContext task) {
		String type = task.getString(param_type);
		String name = task.getString(param_name);

		ChargeSMS[] chargeSmses = null;
		if (type != null && name != null) {
			ChargeSMS chargeSms = CacheManagerUtil.getChargeSMSCacheManager()
					.getChargeSMS(name, type);
			if (chargeSms != null) {
				chargeSmses = new ChargeSMS[1];
				chargeSmses[0] = chargeSms;
			}
		} else {
			List<ChargeSMS> chargeSMSList = CacheManagerUtil
					.getChargeSMSCacheManager().getChargeSMSes(type);
			if (chargeSMSList != null && chargeSMSList.size() > 0)
				chargeSmses = chargeSMSList.toArray(new ChargeSMS[0]);
		}

		Element element = BasicXMLElementGenerator.generateChargeSMSElement(
				document, chargeSmses);
		return element;
	}

	protected Element getCosDetailsElement(Document document,
			WebServiceContext task) {
		String cosID = task.getString(param_cosID);
		String type = task.getString(param_type);
		String pack = task.getString(param_pack);
		String circleID = task.getString(param_circleID);
		String isPrepaid = task.getString(param_isPrepaid);
		String mode = task.getString(param_mode);

		if (isPrepaid == null)
			isPrepaid = "b";

		CosDetails[] cosDetails = null;
		if (cosID != null) {
			CosDetails cosDetail = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(cosID, circleID);
			if (cosDetail != null) {
				cosDetails = new CosDetails[1];
				cosDetails[0] = cosDetail;
			}
		} else if (type != null) {
			List<CosDetails> cosList = CacheManagerUtil
					.getCosDetailsCacheManager().getCosDetailsByCosType(type,
							circleID, isPrepaid);
			if (mode != null) {
				List<CosDetails> cosDetailsList = new ArrayList<CosDetails>();
				for (CosDetails cosDetail : cosList) {
					String accessModes = cosDetail.getAccessMode();
					List<String> accessModeList = Arrays.asList(accessModes
							.split(","));
					if (accessModeList.contains(mode)
							|| accessModeList.contains("ALL"))
						cosDetailsList.add(cosDetail);
				}

				cosDetails = cosDetailsList.toArray(new CosDetails[0]);
			} else
				cosDetails = cosList.toArray(new CosDetails[0]);
		} else if (pack != null) {
			CosDetails cosDetail = CacheManagerUtil.getCosDetailsCacheManager()
					.getSmsKeywordCosDetail(pack, circleID, isPrepaid);
			if (cosDetail != null) {
				cosDetails = new CosDetails[1];
				cosDetails[0] = cosDetail;
			}
		}

		Element element = BasicXMLElementGenerator.generateCosDetailsElement(
				document, cosDetails);
		return element;
	}

	protected Element getRetailerElement(Document document,
			WebServiceContext task) {
		String retailerID = task.getString(param_retailerID);

		Retailer[] retailers = null;
		if (retailerID != null) {
			Retailer retailer = rbtDBManager.getRetailer(retailerID, null);
			if (retailer != null) {
				retailers = new Retailer[1];
				retailers[0] = retailer;
			}
		} else
			retailers = rbtDBManager.getRetailers();

		Element element = BasicXMLElementGenerator.generateRetailerElement(
				document, retailers);
		return element;
	}

	protected Element getFeedStatusElement(Document document,
			WebServiceContext task) {
		String type = task.getString(param_type);

		FeedStatus feedStatus = rbtDBManager.getFeedStatus(type);
		Element element = BasicXMLElementGenerator.generateFeedStatusElement(
				document, feedStatus, task);
		return element;
	}

	protected Element getFeedDetailsElement(Document document,
			WebServiceContext task) {
		String type = task.getString(param_type);
		String name = task.getString(param_name);

		Parameters feedIntervalParm = parametersCacheManager.getParameter(
				iRBTConstant.COMMON, type + "_INTERVAL", "2");
		int feedInterval = Integer.parseInt(feedIntervalParm.getValue().trim());

		FeedSchedule[] feedSchedules = rbtDBManager.getActiveFeedSchedules(
				type, name, feedInterval);
		List<String> feedKeywordList = new ArrayList<String>();
		List<FeedSchedule> feedList = new ArrayList<FeedSchedule>();
		if (feedSchedules != null) {
			for (FeedSchedule feedSchedule : feedSchedules) {
				if (feedKeywordList.contains(feedSchedule.subKeyword()))
					continue;

				feedKeywordList.add(feedSchedule.subKeyword());
				feedList.add(feedSchedule);
			}
		}
		feedSchedules = feedList.toArray(new FeedSchedule[0]);

		Element element = BasicXMLElementGenerator.generateFeedDetailsElement(
				document, feedSchedules);
		return element;
	}

	protected Element getPredefinedGroupsElement(Document document,
			WebServiceContext task) {
		List<PredefinedGroup> predefinedGroups = CacheManagerUtil
				.getPredefinedGroupCacheManager().getAllPredefinedGroups();
		List<PredefinedGroup> preDefinedGroupsList = new ArrayList<PredefinedGroup>();
		if (predefinedGroups != null && predefinedGroups.size() > 0) {
			for (PredefinedGroup preDefinedGroup : predefinedGroups) {
				if (!preDefinedGroup.getPreGroupID().equals("99")) // Ignoring
																	// Blocked
																	// Callers
																	// Group
					preDefinedGroupsList.add(preDefinedGroup);
			}
		}

		Element element = BasicXMLElementGenerator
				.generatePredefinedGroupsElement(document, preDefinedGroupsList
						.toArray(new PredefinedGroup[0]), task);
		return element;
	}

	protected Element getViralDataElement(Document document,
			WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		String callerID = task.getString(param_callerID);
		String type = task.getString(param_type);
		String clipID = task.getString(param_clipID);
		String singleDoubleConfirmation = RBTParametersUtils.getParamAsString("COMMON",
				"SMS_DOUBLE_CONFIRMATION_TYPES",null);
		Date sentTime = null;
		if (task.containsKey(param_sentTime)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS");
			try {
				sentTime = dateFormat.parse(task.getString(param_sentTime));
			} catch (ParseException e) {
				logger.error("", e);
			}
		}

		ViralSMSTable[] viralSMSTableData = null;
		if (task.containsKey(param_viralData)) {
			viralSMSTableData = new ViralSMSTable[1];
			viralSMSTableData[0] = (ViralSMSTable) task.get(param_viralData);
		} else if (!task.containsKey(param_subscriberID)
				&& !task.containsKey(param_callerID)
				&& !task.containsKey(param_smsID)
				&& task.containsKey(param_type)) {
			viralSMSTableData = rbtDBManager.getViralSMSByType(type); 
		} else if (task.containsKey(param_smsID)) {
			long smsID = Long.parseLong(task.getString(param_smsID));
			ViralSMSTable viralSMS = rbtDBManager.getViralSMS(smsID);
			if (viralSMS != null) {
				viralSMSTableData = new ViralSMSTable[1];
				viralSMSTableData[0] = viralSMS;
			}
		} else if (task.containsKey(param_type)
				&& (type.split(",").length > 1) || (singleDoubleConfirmation != null && !singleDoubleConfirmation.isEmpty())) {
			// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
			if (singleDoubleConfirmation == null
					|| singleDoubleConfirmation.isEmpty()) {
				String[] smsTypes = type.split(",");
				ViralSMSTable[] viralSMS = rbtDBManager
						.getViralSMSByTypesForSubscriber(subscriberID, smsTypes);
				if (viralSMS != null) {
					viralSMSTableData = new ViralSMSTable[1];
					viralSMSTableData[0] = viralSMS[0];
				}
			} else{
				int duration = 30;
				Parameters param = parametersCacheManager.getParameter(
						iRBTConstant.SMS, "WAIT_TIME_FOR_SINGLE_DOUBLE_CONFIRMATION");
				if (param != null) {
					try {
						duration = Integer.parseInt(param.getValue());
					} catch (Exception e) {
						duration = 30;
					}
				}
				boolean order = RBTParametersUtils.getParamAsBoolean(
						iRBTConstant.COMMON, "GET_VIRAL_DATA_IN_ASCENDING_ORDER_FOR_SINGLE_DOUBLE_CONFIRMATION","TRUE");
				ViralSMSTable viralSMS = rbtDBManager.getAllViralSMS(subscriberID,
						type.split(","), duration, order);
				if (viralSMS != null) {
					viralSMSTableData = new ViralSMSTable[1];
					viralSMSTableData[0] = viralSMS;
				}
			}
		} else if (task.containsKey(param_type)
				&& task.getString(param_type).equals("SMSCONFPENDING")) {
			int duration = 30;
			Parameters param = parametersCacheManager.getParameter(
					iRBTConstant.SMS, "WAIT_TIME_DOUBLE_CONFIRMATION");
			if (param != null) {
				try {
					duration = Integer.parseInt(param.getValue());
				} catch (Exception e) {
					duration = 30;
				}
			}

			ViralSMSTable viralSMS = rbtDBManager
					.getLatestViralSMSByTypeSubscriberAndTime(subscriberID,
							type, duration);
			if (viralSMS != null) {
				viralSMSTableData = new ViralSMSTable[1];
				viralSMSTableData[0] = viralSMS;
			}

		} else if (task.containsKey(param_type)
				&& task.getString(param_type).equals("COPYCONFPENDING")) {
			int duration = 30;
			Parameters param = parametersCacheManager.getParameter(
					iRBTConstant.GATHERER, "WAIT_TIME_DOUBLE_CONFIRMATION");
			if (param != null) {
				try {
					duration = Integer.parseInt(param.getValue());
				} catch (Exception e) {
					duration = 30;
				}
			}

			ViralSMSTable viralSMS = rbtDBManager
					.getLatestViralSMSByTypeAndTime(callerID, type, duration);
			if (viralSMS != null) {
				viralSMSTableData = new ViralSMSTable[1];
				viralSMSTableData[0] = viralSMS;
			}

		} else if (type != null && type.equals("SELCONFPENDING")) {
			int duration = RBTParametersUtils.getParamAsInt(
					iRBTConstant.COMMON, "SEL_WAIT_TIME_DOUBLE_CONFIRMATION",
					30);

			ViralSMSTable[] viralSMS = rbtDBManager
					.getLatestViralSMSesByTypeSubscriberAndTime(subscriberID,
							type, duration);
			viralSMSTableData = viralSMS;
        } else if (type != null && type.equals("SMS_DCT_MANAGE")) {
			int duration = RBTParametersUtils.getParamAsInt(
             iRBTConstant.COMMON, "DCT_MANAGE_WAIT_TIME_FOR_BASE_DCT",
             30);

			 ViralSMSTable[] viralSMS = rbtDBManager
                 .getLatestViralSMSesByTypeSubscriberAndTime(subscriberID,
                   type, duration);
	         List<ViralSMSTable> list = viralSMS != null ? Arrays.asList(viralSMS) : null;
		     viralSMSTableData = list != null ? list.toArray(new ViralSMSTable[0]) : viralSMS;
		} else if (type != null && type.equals("SMS_DCT_SONG_MANAGE")) {
			int duration = RBTParametersUtils.getParamAsInt(
		             iRBTConstant.COMMON, "DCT_MANAGE_WAIT_TIME_FOR_SONG_DCT",
		             30);

					 ViralSMSTable[] viralSMS = rbtDBManager
		                 .getLatestViralSMSesByTypeSubscriberAndTime(subscriberID,
		                   type, duration);
			         List<ViralSMSTable> list = viralSMS != null ? Arrays.asList(viralSMS) : null;
				     viralSMSTableData = list != null ? list.toArray(new ViralSMSTable[0]) : viralSMS;
		} else
			viralSMSTableData = rbtDBManager.getViralSMSes(subscriberID,
					callerID, type, clipID, sentTime);

		Element element = BasicXMLElementGenerator.generateViralDataElement(
				document, task, viralSMSTableData);
		return element;
	}

	protected Element getPendingConfirmationsRemainderDataElement(
			Document document, WebServiceContext task) {
		String recordsFrom = task.getString(param_recordsFrom);
		String numOfRecords = task.getString(param_numOfRecords);
		String delayInSentTime = task.getString(param_delayInSentTime);
		
		PendingConfirmationsReminderTableImpl[] pendingConfirmationsRemainders = rbtDBManager
				.getPendingConfirmationReminders(delayInSentTime, recordsFrom, numOfRecords);

		Element element = BasicXMLElementGenerator
				.generatePendingConfirmationReminderElement(document, task,
						pendingConfirmationsRemainders);
		return element;
	}

	protected Element getTransDataElement(Document document,
			WebServiceContext task) {
		String transID = task.getString(param_transID);
		String subscriberID = task.getString(param_subscriberID);
		String type = task.getString(param_type);

		TransData[] transDatas = null;
		if (task.containsKey(param_transData)) {
			transDatas = new TransData[1];
			transDatas[0] = (TransData) task.get(param_transData);
		} else if (transID != null) {
			TransData transData = rbtDBManager.getTransData(transID, type);
			if (transData != null) {
				transDatas = new TransData[1];
				transDatas[0] = transData;
			}
		} else if (subscriberID != null) {
			transDatas = rbtDBManager.getTransDataBySubscriberID(subscriberID,
					type);
		}

		Element element = BasicXMLElementGenerator.generateTransDataElement(
				document, transDatas);
		return element;
	}

	protected Element getRbtSupportDataElement(Document document,
			WebServiceContext task) {
		RbtSupport[] rbtSupportDatas = null;
		if (task.containsKey(param_rbtSupportData)) {
			rbtSupportDatas = new RbtSupport[1];
			rbtSupportDatas[0] = (RbtSupport) task.get(param_rbtSupportData);
		}

		Element element = BasicXMLElementGenerator
				.generateRbtSupportDataElement(document, rbtSupportDatas);
		return element;
	}

	protected void updateAccessCount(WebServiceContext task,
			Subscriber subscriber) {
		if (subscriber != null)
			rbtDBManager.setAccessCount(subscriber.subID(), subscriber
					.noOfAccess() + 1);
	}

	/**
	 * This method
	 * 
	 * @author Sreekar
	 */
	@Override
	public Document getOfferDocument(WebServiceContext task) {
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String response = SUCCESS;
		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		populateParamMode(task);
		Element offerElement = getOfferElement(document, task);
		element.appendChild(offerElement);

		return document;
	}

	private void populateParamMode(WebServiceContext task) {
		if (task.containsKey(param_mode))
			return;
		task.put(param_mode, "VP");
	}

	protected Element getOfferElement(Document document, WebServiceContext task) {
		String action = task.getString(param_action);
		Offer[] offers = null;
			
		if(action!=null && action.equalsIgnoreCase(action_offerFromBI)){
			  offers = getOffersFromBI(task);
		}else if(action != null && action.equalsIgnoreCase(action_getPackageOffer)) {
			offers = getPackageOffers(task);
		}else{
		    offers = getOffers(task);
		}
		Element offerElement = BasicXMLElementGenerator.generateOfferElement(
				document, offers);
		return offerElement;
	}
	
	protected Offer[] getOffersFromBI(WebServiceContext task){
		String subscriberID = task.getString(param_subscriberID);
		List<Offer> offersList = new ArrayList<Offer>();
         try {
        	 String offerUrl = null;
        	 
        	 Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
        	 
             if(offerUrl == null) { 
            	 offerUrl = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "URL_FOR_OFFER_FROM_BI", null);
             }
             logger.info("offerUrl: " + offerUrl);
             if(offerUrl == null) {
            	 return null;
             }
			
            String loginDetails = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "LOGIN_DETAILS_FOR_OFFER_FROM_BI", null);
			if(offerUrl.contains("%USERNAME%") && loginDetails != null) {
			  offerUrl = offerUrl.replaceAll("%USERNAME%", loginDetails.split(":")[0]);
			}
			if(offerUrl.contains("%PASSWORD%")  && loginDetails != null) {
			  offerUrl = offerUrl.replaceAll("%PASSWORD%", loginDetails.split(":")[1]);
			}
			if(offerUrl.contains("%CIRCLE%")) {
				  offerUrl = offerUrl.replaceAll("%CIRCLE%", subscriber.circleID());
			}
			offerUrl = offerUrl.replaceAll("%MSISDN%", subscriberID);
			HttpParameters httpParameters = new HttpParameters(offerUrl);
			logger.info("RBT:: httpParameters: " + httpParameters);
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("---------"+httpResponse.getResponse());
			Document document = XMLUtils.getDocumentFromString(httpResponse.getResponse());
			NodeList nodeList = (document != null) ? document.getElementsByTagName("offer") : null;
			NodeList nodeListPack = (document != null) ? document.getElementsByTagName("songpack") : null;
			if(nodeList!=null){
			  extractOffers(nodeList, offersList, "RBT_ACT_", Offer.OFFER_TYPE_SUBSCRIPTION);
			}
			if(nodeListPack!=null){
			   extractOffers(nodeListPack, offersList, "RBT_SEL_", Offer.OFFER_TYPE_SELECTION);
			}
		
		}catch(Exception e){
			logger.info("Exception ::"+e);
		}
		
		return offersList.toArray(new Offer[0]);
	}

	// RBT-8199:Need to include all SMS logs in CCC GUI as part of SMS logs
	protected Element getSMSHistoryFromUMPElement(Document document, WebServiceContext task) {
		SMSHistory[] smsHistoryArr = getSMSHistoryFromURL(task);;
		Element smsHistoryElement = BasicXMLElementGenerator.generateSMSHistoryElement(
				document, smsHistoryArr);
		return smsHistoryElement;
	}
	
	protected SMSHistory[] getSMSHistoryFromURL(WebServiceContext task){
		String subscriberID = task.getString(param_subscriberID);
		String startDate = task.getString(param_startDate);
		String endDate = task.getString(param_endDate);
		 SimpleDateFormat dFormat = new SimpleDateFormat("yyyMMddHH:mm:ss");
		 SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		 List<SMSHistory> smsHistoryList = new ArrayList<SMSHistory>();
         try {
             String smsHistoryUrl = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "URL_FOR_SMS_HISTORY", null);
             logger.info("smsHistoryUrl: " + smsHistoryUrl);
             if(smsHistoryUrl == null) {
            	 return null;
             }
             String dateFormatForSMSLog = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DATE_FORMAT_FOR_SMS_LOG", null);
             logger.info("dateFormatForSMSLog: " + dateFormatForSMSLog);
             if(dateFormatForSMSLog != null && dateFormatForSMSLog.length()>0) {
            	 dFormat = new SimpleDateFormat(dateFormatForSMSLog);
             }
             
             startDate = dFormat.format(sd.parse(startDate));
             endDate = dFormat.format(sd.parse(endDate));
            String loginDetails = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "LOGIN_DETAILS_FOR_SMS_HISTORY", null);
			if(smsHistoryUrl.contains("%USERNAME%") && loginDetails != null) {
			  smsHistoryUrl = smsHistoryUrl.replaceAll("%USERNAME%", loginDetails.split(":")[0]);
			}
			if(smsHistoryUrl.contains("%PASSWORD%")  && loginDetails != null) {
			  smsHistoryUrl = smsHistoryUrl.replaceAll("%PASSWORD%", loginDetails.split(":")[1]);
			}
			if(smsHistoryUrl.contains("%START_TIME%")) {
				  smsHistoryUrl = smsHistoryUrl.replaceAll("%START_TIME%", startDate);
			}
			if(smsHistoryUrl.contains("%END_TIME%")) {
				  smsHistoryUrl = smsHistoryUrl.replaceAll("%END_TIME%", endDate);
			}
			smsHistoryUrl = smsHistoryUrl.replaceAll("%MSISDN%", subscriberID);
			HttpParameters httpParameters = new HttpParameters(smsHistoryUrl);
			logger.info("RBT:: httpParameters: " + httpParameters);
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("---------"+httpResponse.getResponse());
			Document document = XMLUtils.getDocumentFromString(httpResponse.getResponse());
			NodeList nodeList = (document != null) ? document.getElementsByTagName("RECORD") : null;
			for(int i = 0; i<nodeList.getLength();i++) {
				SMSHistory smsHistory = new SMSHistory();
			    Element element = (Element)nodeList.item(i);
			    String timeStamp =  element.getAttribute("TIMESTAMP");
			    smsHistory.setSentTime(timeStamp);
			   // NodeList srvNodeList = element.getElementsByTagName("SERVICE");
			    NodeList transNodeList = element.getElementsByTagName("TRANSTYPE");
			    NodeList messageNodeList = element.getElementsByTagName("MESSAGE");
			    if(transNodeList != null && transNodeList.item(0) != null){
				   String transType = ((Element)transNodeList.item(0)).getTextContent();
				   smsHistory.setSmsType(transType);
				 }
			    if(messageNodeList != null && messageNodeList.item(0) != null){
				   String message = ((Element)messageNodeList.item(0)).getTextContent();
				   smsHistory.setSmsText(message);
				 }
			  smsHistoryList.add(smsHistory);
			}
		}catch(Exception e){
			logger.info("Exception ::"+e);
		}
		
		return smsHistoryList.toArray(new SMSHistory[0]);
	}
	
	private void extractOffers(NodeList nodeList, List<Offer> offersList,
			String srvKeyPrefix, int offerType) {
		
		for(int i = 0; i<nodeList.getLength();i++){
			  Offer offer = new Offer();
			  Element element = (Element)nodeList.item(i);
			  NodeList srvNodeList = element.getElementsByTagName("keyword");
			  NodeList messNodeList = element.getElementsByTagName("message");
			  if(srvNodeList!=null && srvNodeList.item(0)!=null){
				  String srvKey = ((Element)srvNodeList.item(0)).getTextContent();
				  srvKey = (srvKey!=null) ? srvKey.replaceAll(srvKeyPrefix, ""):null;
				  offer.setSrvKey(srvKey);
				  offer.setOfferTypeValue(offerType);
			  }
			  if(messNodeList!=null && messNodeList.item(0)!=null){
				  String message = ((Element)messNodeList.item(0)).getTextContent();
				  offer.setOfferDesc(message);
			  }
		      offersList.add(offer);
		  }
	}

	protected String canBeGifted(String subscriberID, String callerID,
			String contentID) {
		SubscriberDetail subscriberDetail = RbtServicesMgr
				.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
		if (subscriberDetail != null && subscriberDetail.getCircleID() != null)
			return VALID;

		return INVALID;
	}

	protected Offer[] getOffers(WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		String offerTypeStr = task.getString(param_offerType);
		String classType = task.getString(param_classType);
		String mode = task.getString(param_mode);
		logger.info("Getting offers for subscriberId: " + subscriberID
				+ ", offerTypeStr: " + offerTypeStr + ", classType: "
				+ classType + ", mode: " + mode);
		Offer[] offers = null;
		if (task.containsKey(param_subscriberID)
				&& task.containsKey(param_offerID)) {
			String offerID = task.getString(param_offerID);
			try {
				offers = RBTSMClientHandler.getInstance().getOffers(
						subscriberID, "RBT", offerID);
			} catch (RBTException e) {
				e.printStackTrace();
			}
		} else if (task.containsKey(param_offerID)) {
			String offerID = task.getString(param_offerID);
			try {
				offers = RBTSMClientHandler.getInstance().getOffer("RBT",
						offerID);
			} catch (RBTException e) {
				e.printStackTrace();
			}
		}
		if (offers != null) {
			logger.info("Retuning offers. subscriberId: " + subscriberID
					+ ", offers: " + offers);
			return offers;
		}

		if (offerTypeStr == null)
			offerTypeStr = task.getString(param_type);
		ArrayList<Integer> offerTypes = new ArrayList<Integer>();
		if (offerTypeStr == null) {
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (subscriber == null
					|| subscriber.subYes().equalsIgnoreCase(
							iRBTConstant.STATE_DEACTIVATED))
				offerTypes.add(Offer.OFFER_TYPE_SUBSCRIPTION);
			else
				offerTypes.add(Offer.OFFER_TYPE_SELECTION);
		} else {
			StringTokenizer stk = new StringTokenizer(offerTypeStr, ",");
			while (stk.hasMoreTokens())
				addIntOfferTypeToArrayList(stk.nextToken(), offerTypes);
		}
		String userType = Utility.getSubscriberType(task);
		HashMap<String, String> extraInfo = getExtraInfoMap(task);
		try {
			/*
			 * This below block is added because
			 * 
			 * If clipID is passed from UIs and the parameter
			 * CLIP_INFO_FIELDS_FOR_OFFER is configured, then we send the
			 * details from the clipInfo corresponding to all the keys
			 * configured in the parameter.
			 */
			//Start: RBT-12303 - Apply the Free RBT hour promotional concept - Egypt Offer.
			boolean selectionOffer = RBTParametersUtils.getParamAsBoolean(
					iRBTConstant.COMMON,
					"SUPPORT_HAPPY_HOURS_ONLY_SELECTIONS_OFFER", "FALSE");
			boolean allowselOffer = RBTParametersUtils.getParamAsBoolean("COMMON",
					iRBTConstant.ALLOW_SEL_OFFER, "FALSE");
			if ((allowselOffer||selectionOffer)
					&& (offerTypes.toArray(new Integer[0])[0]
							.equals(Offer.OFFER_TYPE_SELECTION))) {
				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				boolean isUserACT = rbtDBManager.isSubscriberActive(subscriber)
						|| rbtDBManager
								.isSubscriberActivationPending(subscriber)
						|| rbtDBManager.isSubscriberInGrace(subscriber)
						|| rbtDBManager.isSubscriberSuspended(subscriber);
				if (isUserACT && (subscriber.maxSelections() > 0)) {
					classType = "DEFAULT";
				} else {
					classType = "FIRST";
				}
			} else {//End: RBT-12303 - Apply the Free RBT hour promotional concept - Egypt Offer
				if (extraInfo.containsKey("CLIP_ID")) {
					String clipID = extraInfo.get("CLIP_ID");
					extraInfo.remove("CLIP_ID");
					Clip clip = rbtCacheManager.getClip(clipID);
					Map<String, String> clipInfoMap = getMapFromClipInfoStr(clip
							.getClipInfo());

					String offerClassType = null;

					if (clip != null) {
						List<String> clipInfoList = Arrays
								.asList(RBTParametersUtils.getParamAsString(
										"COMMON", "CLIP_INFO_FIELDS_FOR_OFFER",
										"").split(","));

						logger.info("ClipInfo map : " + clipInfoMap);

						Set<Entry<String, String>> entriesSet = (clipInfoMap != null) ? clipInfoMap
								.entrySet() : null;
						if (entriesSet != null) {
							for (Entry<String, String> eachEntry : entriesSet) {
								if (clipInfoList.contains(eachEntry.getKey()))
									extraInfo.put(eachEntry.getKey(),
											eachEntry.getValue());
							}
						}

						offerClassType = checkModeBasedCharging(classType,
								mode, clipID, clipInfoMap);
						if (null != offerClassType) {
							extraInfo.put("EXT_SRVKEY", offerClassType);
							classType = offerClassType;
						}

					}
				}
			}//End: RBT-12303 - Apply the Free RBT hour promotional concept - Egypt Offer.
			
			logger.info("Getting offers. subscriberID: " + subscriberID
					+ ", mode: " + mode + ", userType: " + userType
					+ ", offerTypes: " + offerTypes + ", extraInfo: "
					+ extraInfo + ", classType: " + classType);
			offers = RBTSMClientHandler.getInstance().getMultipleTypeOffers(
					subscriberID, mode, userType,
					offerTypes.toArray(new Integer[0]), extraInfo, classType);
		} catch (RBTException e) {
			logger.error("", e);
		}

		if (logger.isDebugEnabled())
			logger.debug("RBT:: offers: " + Arrays.toString(offers));
		return offers;
	}

	private String checkModeBasedCharging(String classType, String mode,
			String clipID, Map<String, String> clipInfoMap) {
		String key = "MODE_BASED_CHARGING_".concat(mode);
		String clipIdsListStr = RBTParametersUtils.getParamAsString(
				"COMMON", key, null);
		boolean isModeConfigured = (null != clipIdsListStr) ? true
				: false; 
		if (isModeConfigured) {
			logger.debug("Mobe based charging is enabled for mode: "
					+ mode + ", clipIdsListStr: " + clipIdsListStr);
			boolean isEnableModeBasedCharging = false;
			if ("all".equalsIgnoreCase(clipIdsListStr)) {
				isEnableModeBasedCharging = true;
			} else {
				List<String> clipIdsList = ListUtils.convertToList(
						clipIdsListStr, ",");
				boolean isClipExistsInClipInfo = clipIdsList.contains(clipID);
				logger.debug("Checking clipId: " + clipID
						+ ", from clipIdsList: " + clipIdsList
						+ ", isClipExistsInClipInfo: "
						+ isClipExistsInClipInfo);
				if (isClipExistsInClipInfo) {
					isEnableModeBasedCharging = true;
				}
			}
			
			if(isEnableModeBasedCharging) {
				String chargeClass = clipInfoMap.get("FREE_EXT_SRVKEY");
				logger.debug("Checking clipInfoMap contains FREE_EXT_SRVKEY key. clipInfoMap: "
						+ clipInfoMap);
				if (null != chargeClass) {
					classType = chargeClass;
					logger.info("Mode based charging is enabled, clipInfo"
							+ " contains FREE_EXT_SRVKEY. "
							+ "Updated chargeType classType: " + classType);
				}
			} else {
				logger.warn("ClipId not configured for mode: " + mode
						+ ", clipIdsListStr: " + clipIdsListStr);
			}
		} else {
			logger.debug("Mobe based charging is not configured for mode: "
					+ mode + ", parameter: " + key);
		}
		logger.info("Retuning classType: " + classType);
		return classType;
	}
	
	protected Offer[] getPackageOffers(WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		String offerTypeStr = task.getString(param_offerType);
		String classType = task.getString(param_classType);
		String clipId = task.getString(param_clipID);
		String offerID = task.getString(param_offerID);
        String mode = task.getString(param_mode);
        
		ArrayList<Integer> offerTypes = new ArrayList<Integer>();
		if (offerTypeStr == null) {
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (subscriber == null
					|| subscriber.subYes().equalsIgnoreCase(
							iRBTConstant.STATE_DEACTIVATED))
				offerTypes.add(Offer.OFFER_TYPE_SUBSCRIPTION);
			else
				offerTypes.add(Offer.OFFER_TYPE_SELECTION);
		} else {
			StringTokenizer stk = new StringTokenizer(offerTypeStr, ",");
			while (stk.hasMoreTokens())
				addIntOfferTypeToArrayList(stk.nextToken(), offerTypes);
		}
		
		String clipClassType = null;
		if (offerTypes != null
				&& offerTypes.get(0) == Offer.OFFER_TYPE_SUBSCRIPTION) {
			clipClassType = task.getString(param_classType);
		}
		if(clipId != null) {
			Clip clip = RBTCacheManager.getInstance().getClip(clipId);
			if(clip != null) {
				if (clip.getClipInfo() != null) {
					String[] clipInfos = clip.getClipInfo().split("\\|");
					for (String clipInfo : clipInfos) {
						if (clipInfo.toLowerCase().indexOf("assetid") != -1) {
							String[] assetIdArr = clipInfo.split("\\=");
							clipId = assetIdArr[1];
							break;
						}
					}
				}
				clipClassType = clip.getClassType();
			}
		}
		
		Offer[] offers = null;
		try {
			offers = RBTSMClientHandler.getInstance().getPackageOffer(subscriberID, offerID, offerTypes.get(0), clipId, clipClassType,mode);
		} catch (RBTException e) {
			e.printStackTrace();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("RBT:: offers: " + Arrays.toString(offers));
		}
		return offers;
	}

	private Map<String, String> getMapFromClipInfoStr(String clipInfo)
	{
		if (clipInfo == null)
			return null;

		Map<String, String> clipInfoMap = new HashMap<String, String>();
		String[] clipInfoTokens = clipInfo.split("\\|");
		for (String eachToken : clipInfoTokens)
		{
			if (eachToken.contains("="))
			{
				String[] tokens = eachToken.split("=", -1);
				clipInfoMap.put(tokens[0].trim(), tokens[1].trim());
			}
		}

		return clipInfoMap;
	}

	private void addIntOfferTypeToArrayList(String offerType,
			ArrayList<Integer> arrayList) {
		int type;
		try {
			type = Integer.parseInt(offerType);
		} catch (Exception e) {
			type = getIntOfferTypeForStr(offerType);
		}

		arrayList.add(type);
	}

	private int getIntOfferTypeForStr(String offerType) {
		if (offerType
				.equals(com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_SELECTION_STR))
			return com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_SELECTION;
		else if (offerType
				.equals(com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_COMBO_STR))
			return com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_COMBO;
		else if (offerType
				.equals(com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_ADVANCE_RENTAL_STR))
			return com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_ADVANCE_RENTAL;
		else if (offerType
				.equals(com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_CHURN_STR))
			return com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_CHURN;

		// if no proper type return subscription type
		return com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_SUBSCRIPTION;
	}

	private HashMap<String, String> getExtraInfoMap(WebServiceContext task) {
		Set<Entry<String, Object>> entrySet = task.entrySet();
		HashMap<String, String> extraParams = new HashMap<String, String>();
		for (Entry<String, Object> entry : entrySet) {
			String key = entry.getKey().toString();
			if (key.startsWith(param_extraInfo + "_"))
				extraParams.put(key.substring(key.indexOf('_') + 1),
						(String) entry.getValue());
		}

		return extraParams;
	}

	protected Element getAllRBTLoginUsersElement(Document document,
			WebServiceContext task) {
		Element element = document.createElement(RBT_LOGIN_USERS);

		String type = task.getString(param_type);
		RBTLoginUser[] rbtLoginUsers = rbtDBManager.getRBTLoginUsers(type);

		if (rbtLoginUsers == null)
			return element;

		for (RBTLoginUser rbtLoginUser : rbtLoginUsers) {
			Element rbtLoginUserElem = BasicXMLElementGenerator
					.generateRBTLoginUserElement(document, rbtLoginUser);
			element.appendChild(rbtLoginUserElem);
		}

		return element;
	}

	@Override
	public BufferedImage getQRCodeImage(WebServiceContext task) {
		BufferedImage bufferedImage = null;

		String type = task.getString(param_QRCodeType);
		if (type == null)
			return null;

		String number = task.getString(param_QRCodeNumber);
		String data = task.getString(param_data);

		if (type.equalsIgnoreCase(param_sms)) {
			if (number == null || data == null)
				return null;

			try {
				bufferedImage = QRCodeGenerator.generateQRCodeImageForSMS(
						number, data);
			} catch (RBTException e) {
				logger.error("", e);
			}
		} else if (type.equalsIgnoreCase(param_url)) {
			if (data == null)
				return null;

			try {
				bufferedImage = QRCodeGenerator.generateQRCodeImageForURL(data);
			} catch (RBTException e) {
				logger.error("", e);
			}
		} else if (type.equalsIgnoreCase(param_phoneNumber)) {
			if (number == null)
				return null;

			try {
				bufferedImage = QRCodeGenerator
						.generateQRCodeImageForPhoneNumber(number);
			} catch (RBTException e) {
				logger.error("", e);
			}
		}

		return bufferedImage;
	}

	protected Element getDownloadHistoryFromBI(Document document,
			WebServiceContext task) {
		Document biHistoryDownloadDetailsDocument = null;
		try {
			Parameters biURLParam = parametersCacheManager.getParameter(
					iRBTConstant.BI, "BI_DOWNLOAD_HISTORY_URL", null);
			String url = biURLParam.getValue().trim();
			String subscriberID = task.getString(param_subscriberID);
			String mode = task.getString(param_mode);
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (subscriber == null) {
				return null;
			}
			String cirtleId = subscriber.circleID();
			url = url.replace("%SUBSCRIBER_ID%", task
					.getString(param_subscriberID));
			url = url.replace("%CHANNEL_NAME%", mode);
			url = url.replace("%CIRCLE_ID%", cirtleId);
			HttpParameters httpParameters = new HttpParameters(url);
			logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			logger.info("RBT:: httpResponse: " + httpResponse);

			if (httpResponse != null && httpResponse.getResponse() != null) {
				biHistoryDownloadDetailsDocument = XMLUtils
						.getDocumentFromString(httpResponse.getResponse());
			}
		} catch (Exception e) {
			logger.error("Exception ", e);
		}
		Element element = BasicXMLElementGenerator
				.generateBIDownloadHistoryElement(document,
						biHistoryDownloadDetailsDocument, task);
		return element;
	}

	protected Element getConsentExpiredSubscriberInfo(Document document,
			WebServiceContext task){
		String subscriberID = task.getString(param_subscriberID);
		String expiredHours = RBTParametersUtils.getParamAsString("DAEMON",
				"CONSENT_EXPIRED_HOURS_BEFORE_CONFIRMATION", "2");
		int limit = RBTParametersUtils.getParamAsInt("DAEMON",
				"CONSENT_EXPIRED_FETCH_LIMIT", 50);
		String modeStr = RBTParametersUtils.getParamAsString("DAEMON",
				"CONSENT_EXPIRED_MODE", null);
		String mode = splitAndAppendSingleQuotes(modeStr);
		int startingFrom = 0;
		List<DoubleConfirmationRequestBean> xpiredRecordsList = rbtDBManager
				.getExpiredConsentRecords(expiredHours, "1", startingFrom, limit, mode);
						
		Element element = getXpiredConsentRecordSubscriberElement(document,task,xpiredRecordsList);
		return element;
	}
	
	protected Element getConsentPendingRecord(Document document, WebServiceContext task) {
	    String subscriberID = task.getString(param_subscriberID);
	    String agentId = task.getString(param_agentId);
		List<DoubleConfirmationRequestBean> baseConsentPendingRecordList = null; 
		try {
			if(agentId !=null){
				baseConsentPendingRecordList = rbtDBManager.getConsentPendingRecordListByAgentId(agentId,subscriberID);
			}else{
				baseConsentPendingRecordList = rbtDBManager.getConsentPendingRecordListByMsisdnNType(
					subscriberID, "ACT");
			}
		} catch (Exception ex) {
			logger.info("Exception while getting the pending records");
		}
		Element element = null;
		if (baseConsentPendingRecordList != null && baseConsentPendingRecordList.size()>0) {
			DoubleConfirmationRequestBean dbReqBean = baseConsentPendingRecordList.get(0);
			String baseSrvKey = dbReqBean.getSubscriptionClass();
			String baseTransId = dbReqBean.getTransId();
			String extraInfo = dbReqBean.getExtraInfo();
			String chargeClass = null;
			String wavFileName = null;
			Integer clipID = null;
			String songName = null;
			String vcode = null;
			String mode = dbReqBean.getMode();
			//<consents><consent srvKey="<song srvkey>" baseSrvKey="<base srv key>" wavFile="<wav file name>" 
			//songName=�<songName>� vcode=�<vcode>� transId=�<transId>�></consent></consents>
			if(extraInfo!=null && extraInfo.indexOf("TRANS_ID")!=-1){
				HashMap<String,String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				String selTransId = xtraInfoMap.get("TRANS_ID");
				List<DoubleConfirmationRequestBean> selConsentPendingRecordList = null;
				if(agentId !=null){
					selConsentPendingRecordList = rbtDBManager.getConsentPendingRequests(selTransId, subscriberID);
				}else{
					selConsentPendingRecordList = rbtDBManager.getConsentRequestForCallBack(selTransId, subscriberID);
				}
				DoubleConfirmationRequestBean selDbReqBean = selConsentPendingRecordList.get(0);
				chargeClass = selDbReqBean.getClassType();
				wavFileName = selDbReqBean.getWavFileName();
				clipID = selDbReqBean.getClipID();
				Clip clip =RBTCacheManager.getInstance().getClip(clipID);
				if(clip == null && wavFileName != null){
					clip =RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFileName);
				}
				if(clip!=null){
					clipID = clip.getClipId();
					songName = clip.getClipName();
					if(wavFileName.indexOf("rbt_") != -1 && wavFileName.indexOf("_rbt") != -1) {
						vcode = wavFileName.substring("rbt_".length(),wavFileName.indexOf("_rbt"));
					}
					
				}
			}else{
				Parameters parameter = CacheManagerUtil.getParametersCacheManager()
						.getParameter("COMMON", "DEFAULT_DOWNLOAD");
				if (parameter != null && parameter.getValue() != null) {
					List<String> m_DEFAULT_DOWNLOAD = Arrays.asList(parameter
							.getValue().split(","));
					Clip clip = rbtCacheManager.getClip(m_DEFAULT_DOWNLOAD.get(0));
					if (clip != null) {
					  songName = clip.getClipName();
					  String wavfile = clip.getClipRbtWavFile();
					  if (wavfile != null)
						vcode = wavfile.replaceAll("rbt_", "").replaceAll("_rbt","");
					}
					Parameters chargeClassParameter = CacheManagerUtil.getParametersCacheManager()
							.getParameter("COMMON", "DEFAULT_SELECTION_CHARGE_CLASS");
					chargeClass = "DEFAULT";
					if (chargeClassParameter != null)
						chargeClass = chargeClassParameter.getValue();

				}
			}
			
			element = document.createElement("consents");
			Element consentElement = document.createElement("consent");
			if(chargeClass!=null)
				consentElement.setAttribute("srvKey", chargeClass);
			if(baseSrvKey!=null)
			    consentElement.setAttribute("baseSrvKey", baseSrvKey);
			if(wavFileName!=null)
			    consentElement.setAttribute("wavFile", wavFileName);
			if(songName!=null)
			    consentElement.setAttribute("songName", songName);
			if(vcode!=null)
			    consentElement.setAttribute("vcode", vcode);
			if(baseTransId!=null)
			    consentElement.setAttribute("transId", baseTransId);
			if(mode != null)
				consentElement.setAttribute("mode", mode);
			if(clipID !=null)
				consentElement.setAttribute(CLIP_ID, String.valueOf(clipID));
			if(dbReqBean.getSubscriberID() !=null)
				consentElement.setAttribute(SUBSCRIBER_ID, dbReqBean.getSubscriberID());
			if(dbReqBean.getCircleId() != null){
				consentElement.setAttribute(CIRCLE_ID, dbReqBean.getCircleId());
			}
			if(dbReqBean.getLanguage() != null){
				consentElement.setAttribute(LANGUAGE, dbReqBean.getLanguage());
			}else{
				String defLang = RBTParametersUtils.getParamAsString("ALL","DEFAULT_LANGUAGE", "eng");
				consentElement.setAttribute(LANGUAGE, defLang);
			}
			
			if(dbReqBean.getSubscriptionClass()!=null || chargeClass!=null){
				float subcriptionPrice = 0;
				float chargeClsPrice = 0;
				if(dbReqBean.getSubscriptionClass()!=null){
					SubscriptionClass subscriptionClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(dbReqBean.getSubscriptionClass());
					if(subscriptionClass.getSubscriptionPeriod()!=null){
						int validityInDays = Utility.getValidityPeriod(subscriptionClass.getSubscriptionPeriod());
						consentElement.setAttribute(VALID_DAYS, String.valueOf(validityInDays));
					}
					
					if(subscriptionClass!=null && subscriptionClass.getSubscriptionAmount()!=null){
						try{
							subcriptionPrice = Float.parseFloat(subscriptionClass.getSubscriptionAmount());
						}catch(Exception e){
							logger.error("UNABLE TO PARSE STRING TO NUMBER:"+subscriptionClass.getSubscriptionAmount());
						}
					}
				}
				if(chargeClass!= null){
					ChargeClass chargeCls = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass);
					if(chargeCls!=null){
						try{
							chargeClsPrice = Float.parseFloat(chargeCls.getAmount());
						}catch(Exception e){
							logger.error("UNABLE TO PARSE STRING TO NUMBER:"+chargeCls.getAmount());
						}
					}
				}
				float price = subcriptionPrice + chargeClsPrice;
				boolean displayAmtInPaise = RBTParametersUtils.getParamAsBoolean("WEBSERVICE","DISPLAY_AMOUNT_IN_PAISE", "FALSE");
				if(displayAmtInPaise){
					price = price * 100;
				}
				consentElement.setAttribute(PRICE, String.valueOf(price));
			}
			
			element.appendChild(consentElement);
		}
		
		return element;
	}
	
	private String splitAndAppendSingleQuotes(String str) {
		if(null != str) {
			String[] arr = str.split(",");
			StringBuilder sb = new StringBuilder("'");
			for(int i = 0; i< arr.length;i++) {
				sb.append(arr[i].trim());
				sb.append("'");
				if(arr.length - 1 > i) {
					sb.append(",'");	
				}
			}
			logger.info("Replaced quotes and returning mode: " + sb.toString());
			return sb.toString();
		}
		logger.info("Returning mode: " + str);
		return str;
	}

	private Element getXpiredConsentRecordSubscriberElement(Document document,
			WebServiceContext task , List<DoubleConfirmationRequestBean> xpiredRecordsList) {
		// <consents><consent srvKey="<song srvkey>" baseSrvKey="<base srv key>"
		// wavFile="<wav file name>"></consent></consents>
		Element element = document.createElement("consents");
		String subscriberID = task.getString(param_subscriberID);
		List<String> comboTransIDList = new ArrayList<String>();

		if (xpiredRecordsList != null && xpiredRecordsList.size() > 0) {
			for (DoubleConfirmationRequestBean bean : xpiredRecordsList) {
				Element consentElement = document.createElement("consent");
				String transId = bean.getTransId();
				if (comboTransIDList.contains(transId)) {
					comboTransIDList.remove(transId);
					continue;
				}
				String requestType = bean.getRequestType();
				String wavFile = bean.getWavFileName();
				String srvKey = bean.getClassType();
				String baseSrvKey = bean.getSubscriptionClass();
				String xtraInfo = bean.getExtraInfo();
				Map<String, String> xtraInfoMap = DBUtility
						.getAttributeMapFromXML(xtraInfo);

				if (xtraInfoMap != null && xtraInfoMap.containsKey("TRANS_ID")) {
					String transID = xtraInfoMap.get("TRANS_ID");
					comboTransIDList.add(transID);
					List<DoubleConfirmationRequestBean> dbConfirmReqBeanList = rbtDBManager
							.getDoubleConfirmationRequestBeanForStatus(null,
									transID, subscriberID, null, false);
					if (dbConfirmReqBeanList != null
							&& dbConfirmReqBeanList.size() > 0) {
						DoubleConfirmationRequestBean reqBean = dbConfirmReqBeanList
								.get(0);
						if (reqBean != null) {
							srvKey = reqBean.getSubscriptionClass();
							wavFile = reqBean.getWavFileName();
						}
					}

				}

				if (baseSrvKey != null) {
					consentElement.setAttribute("baseSrvKey", baseSrvKey);
				}
				if (srvKey != null) {
					consentElement.setAttribute("srvKey", srvKey);
				}
				if (wavFile != null) {
					consentElement.setAttribute("wavFile", wavFile);
				}
				element.appendChild(consentElement);
			}
		}

		return element;
	}
	
	protected Element getSubscriberPacks(Document document,
			WebServiceContext task) {
		List<SubscriberPack> subscriberPacksList = getWebServiceSubscriberPacks(
				document, task);
		Element packElement = getSubscriberPacksElement(document, task,
				subscriberPacksList);

		return packElement;
	}

	private List<SubscriberPack> getWebServiceSubscriberPacks(
			Document document, WebServiceContext task) {

		String subscriberID = task.getString(param_subscriberID);
		logger.info("Getting packs for subscriberID: " + subscriberID);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		Parameters parameter = parametersCacheManager.getParameter(
				iRBTConstant.COMMON, "STATUS_FOR_SUBSCRIBER_PACKS",
				null);
		String status = (null != parameter) ? parameter.getValue() : null;
		
		List<ProvisioningRequests> provList = null;
		if (null != status) {
			provList = ProvisioningRequestsDao.getBySubscriberIdAndStatus(
					subscriberID, status);
		} else {
			provList = ProvisioningRequestsDao.getBySubscriberId(subscriberID);
		}
		
		List<ProvisioningRequests> provReqList = new ArrayList<ProvisioningRequests>();
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);

		if (subscriber == null) {
			logger.warn("Returning null, subscriber does not exists");
			return null;
		}

		String subscriberExtraInfo = subscriber.extraInfo();
		HashMap<String, String> subExtraInfo = new HashMap<String, String>();
		if (subscriberExtraInfo != null) {
			subExtraInfo = DBUtility
					.getAttributeMapFromXML(subscriberExtraInfo);
		}
		if (!subExtraInfo.containsKey(iRBTConstant.EXTRA_INFO_PACK)) {
			logger.warn("Returning null, subscriber is not activated"
					+ " into any pack. subscriberID: " + subscriberID
					+ ", extra info: " + subscriberExtraInfo
					+ ", does not have PACK attribute.");
			return null;
		}

		String[] packCosIds = subExtraInfo.get(iRBTConstant.EXTRA_INFO_PACK)
				.split("\\,");

		if (packCosIds.length == 0) {
			logger.warn("Returning null, subscriber pack cosids are empty"
					+ ", subscriberID: " + subscriberID + ", extra info: "
					+ subscriberExtraInfo);
			return null;
		}

		List<Integer> packCosIdLists = new ArrayList<Integer>();
		for (String packCosId : packCosIds) {
			try {
				packCosIdLists.add(Integer.parseInt(packCosId));
			} catch (Exception e) {
				continue;
			}
		}

		if (provList != null) {
			for (ProvisioningRequests provReq : provList) {
				if (packCosIdLists.contains(provReq.getType())) {
					provReqList.add(provReq);
				}
			}
		}

		if (provReqList == null || provReqList.size() == 0) {
			logger.warn("Returning null, subscriber pack cosids"
					+ " and provisioning request"
					+ "cos ids are not matching, subscriberID: " + subscriberID
					+ ", extra info: " + subscriberExtraInfo);
			return null;
		}

		List<SubscriberPack> subscriberPacksList = new ArrayList<SubscriberPack>();

		for (ProvisioningRequests provReq : provReqList) {
			SubscriberPack subscriberPack = new SubscriberPack();
			subscriberPack.setIntRefId(provReq.getTransId());
			subscriberPack.setCreationTime(provReq.getCreationTime());
			subscriberPack.setPackChargeClass(provReq.getChargingClass());
			subscriberPack.setPackModeInfo(provReq.getModeInfo());
			subscriberPack.setPackMode(provReq.getMode());
			subscriberPack.setNumMaxSelections(provReq.getNumMaxSelections());

			CosDetails cosDetails = CacheManagerUtil
					.getCosDetailsCacheManager().getCosDetail(
							provReq.getType() + "", subscriber.circleID());
			if (cosDetails == null) {
				continue;
			}

			subscriberPack.setCosId(cosDetails.getCosId());
			subscriberPack.setCosType(cosDetails.getCosType());

			String extraInfo = provReq.getExtraInfo();
			HashMap<String, String> provReqExtraInofMap = null;
			if (extraInfo != null && !(extraInfo = extraInfo.trim()).equals("")) {
				provReqExtraInofMap = DBUtility
						.getAttributeMapFromXML(extraInfo);
			}

			if (provReqExtraInofMap != null) {

				if (provReqExtraInofMap.containsKey(PACK_DEACTIVATE_MODE))
					subscriberPack.setDeactivateMode(provReqExtraInofMap
							.get(PACK_DEACTIVATE_MODE));
				if (provReqExtraInofMap.containsKey(PACK_DEACTIVATE_MODE_INFO))
					subscriberPack.setDeactivateModeInfo(provReqExtraInofMap
							.get(PACK_DEACTIVATE_MODE_INFO));

				try {
					if (provReqExtraInofMap.containsKey(PACK_DEACTIVATE_TIME))
						subscriberPack.setDeactivateDate(dateFormat
								.parse(provReqExtraInofMap
										.get(PACK_DEACTIVATE_TIME)));
				} catch (Exception e) {
				}
			}
			subscriberPack.setStatus(Utility.getSubscriberPackStatus(provReq));
			subscriberPack.setLastChargingDate(provReq.getNextRetryTime());

			subscriberPacksList.add(subscriberPack);

		}
		logger.info("Returning subscriberPacksList: " + subscriberPacksList
				+ ", subscriberID: " + subscriberID);
		return subscriberPacksList;
	}

	protected Element getSubscriberPacksElement(Document document,
			WebServiceContext task, List<SubscriberPack> subPackLists) {
		Element element = document.createElement(SUBSCRIBERPACKS);

		if (subPackLists == null) {
			element.setAttribute(NO_OF_PACKS, "0");
			element.setAttribute(NO_OF_ACTIVE_PACKS, "0");
		} else {
			element.setAttribute(NO_OF_PACKS, String.valueOf(subPackLists
					.size()));

			int noOfActiveDownloads = 0;
			for (SubscriberPack subPack : subPackLists) {
				if (subPack.getStatus().equalsIgnoreCase(ACTIVE))
					noOfActiveDownloads++;

				//RBT-11390
				if(!(task.get("extraInfo_GET_DEACTIVATE_PACKS")!=null && task.get("extraInfo_GET_DEACTIVATE_PACKS").toString().equals("yes"))) {
					if (!subPack.getStatus().equalsIgnoreCase(ACTIVE)
							&& !subPack.getStatus().equalsIgnoreCase(ACT_PENDING)
							&& !subPack.getStatus().equalsIgnoreCase(GRACE)
							&& !subPack.getStatus().equalsIgnoreCase(SUSPENDED))
						continue;
				}
				Element contentElem = getSubscriberPackElement(document, task,
						subPack);
				element.appendChild(contentElem);
			}

			element.setAttribute(NO_OF_ACTIVE_PACKS, String
					.valueOf(noOfActiveDownloads));
		}

		return element;
	}

	protected Element getSubscriberPackElement(Document document,
			WebServiceContext task, SubscriberPack subPack) {
		Element element = BasicXMLElementGenerator
				.generateSubscriberPackElement(document, task, subPack);
		return element;
	}

	// JIRA-RBT-6194:Search based on songs in Query Gallery API 
	private SubscriberDownloads getDownloadByPromoID(WebServiceContext task) {
		SubscriberDownloads subscriberDownload = null;
		try {
			String subscriberID = task.getString(param_subscriberID);
			String wavFileName = "";
			Clip clipObj = null;
			if(task.getString(param_clipID) != null) {
			   clipObj =  rbtCacheManager.getClip(task.getString(param_clipID));
			   wavFileName = clipObj.getClipRbtWavFile();
			}
			if(task.getString(param_promoID) != null) {
				clipObj = rbtCacheManager.getClipByPromoId(task.getString(param_promoID));
				wavFileName = clipObj.getClipRbtWavFile();
			}
			logger.info("param: " + task.getString(param_song));
			if(task.getString(param_song) != null) {
				SubscriberDownloads [] subscriberdwnloads = rbtDBManager.getSubscriberDownloads(subscriberID);
				if(subscriberdwnloads != null) {
					String song_search = task.getString(param_song);
					int downlength = subscriberdwnloads.length;
					logger.info("subscriberdwnloads size: " + downlength);
					for(int i =0 ; i <downlength; i++) {
						logger.info("subscriberdwnloads[i].promoId(): " + subscriberdwnloads[i].promoId());
					   clipObj = rbtCacheManager.getClipByRbtWavFileName(subscriberdwnloads[i].promoId());
					  logger.info("clipObj: " + clipObj);
					  if(clipObj.getClipName().equalsIgnoreCase(song_search)) {
						  wavFileName = clipObj.getClipRbtWavFile();
						  logger.info("wavFileName: : :" + wavFileName);
						 break;
					   }				
					}
				}
			}
		      subscriberDownload = rbtDBManager.getSubscriberDownloadsByPromoId(subscriberID, wavFileName);
		} catch (Exception e) {
			logger.error("", e);
		}

		logger.info("subscriberDownload: " + subscriberDownload);
		return subscriberDownload;
	}
	
	public Element getCGWAPConsentElement(Document document, WebServiceContext task, Element element) {
		
		String response = task.getString(param_response);
		String msisdn = task.getString(param_subscriberID);
		String mode = 	task.getString(param_mode);
		String clip_id = task.getString(param_clipID);
		String promo_id = task.getString(WebServiceConstants.param_promoID);
		String cat_id = task.getString(WebServiceConstants.param_categoryID);
		String trans_id = task.getString("CONSENTID");
		String chargeclass = task.getString("CONSENTCLASSTYPE");
		String sub_class = task.getString("CONSENTSUBCLASS");
		String callerId = task.getString(param_callerID);
		String pricePoint = task.getString("price");
		String priceValidity = task.getString("priceValidity");
        logger.info("getSelIntegrationPreConsentResponseDocument task ::= "+task);

		if (response.indexOf(SUCCESS)!=-1 && !task.containsKey("USER_ACTIVE_SEL_CON_INT")) {
				Element consentElem = document.createElement(param_consent);
				consentElem.setAttribute(param_msisdn, msisdn);
				if (mode == null)
					mode = "VP";
				consentElem.setAttribute("mode", mode);
				if (sub_class != null && sub_class.length() > 0) {
					consentElem.setAttribute("sub_class", sub_class);
				}
				consentElem.setAttribute("trans_id", trans_id);
				if (clip_id != null && clip_id.length() > 0)
					consentElem.setAttribute("clip_id", clip_id);
				if (promo_id != null && promo_id.length() > 0)
					consentElem.setAttribute("promoId", promo_id);
				if (chargeclass != null && chargeclass.length() > 0)
					consentElem.setAttribute("chargeclass", chargeclass);
				if (cat_id != null && cat_id.length() > 0)
					consentElem.setAttribute("catId", cat_id);
				if(callerId != null && callerId.length() > 0)
					consentElem.setAttribute("caller_id", callerId);
				if(pricePoint != null && pricePoint.length() > 0) 
					consentElem.setAttribute("price", pricePoint);
				if(priceValidity != null && priceValidity.length() > 0) 
					consentElem.setAttribute("value", priceValidity);
				element.appendChild(consentElem);
		}
		return element;
	}
	//CG Integration Flow - Jira -12806
	private Element addConsentAttributes(Element consentElem, String consentId,
			String subscriberID, boolean isComboReq) {
		boolean checkCGFlowForBSNL = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.DOUBLE_CONFIRMATION,
				"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
		boolean checkCGFlowForBSNLEast = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.COMMON,
				"CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE");
		if (checkCGFlowForBSNL || checkCGFlowForBSNLEast) {
			/*consentElem.setAttribute(IS_COMBO_REQUEST,
					String.valueOf(isComboReq));*/
			List<DoubleConfirmationRequestBean> consentRecordList = null;
			consentRecordList = rbtDBManager.getConsentRequestForCallBack(
					consentId, subscriberID);
			String requestDate= null;
			SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (null != consentRecordList && !consentRecordList.isEmpty()) {
				DoubleConfirmationRequestBean consentRecord = consentRecordList
						.get(0);				
				if(isComboReq){
					String xtraInfo = consentRecord.getExtraInfo();
					Map<String, String> xtraInfoMap = null;
					String transId = null;					
					if (xtraInfo != null && !xtraInfo.equalsIgnoreCase("null")) {
						xtraInfoMap = DBUtility.getAttributeMapFromXML(xtraInfo);
						transId = xtraInfoMap.get("TRANS_ID");
						consentRecordList = rbtDBManager
								.getConsentRequestForCallBack(transId, subscriberID);
					}
					xtraInfoMap = null;
					consentElem.setAttribute(SELECTION_TYPE, "0");
					if (null != consentRecord.getRequestTime()) {
						requestDate=dFormat.format(consentRecord.getRequestTime());
					}
					consentElem.setAttribute(REQUEST_TIME_STAMP, requestDate);
				}
				if (null != consentRecordList && !consentRecordList.isEmpty()) {
					consentRecord = consentRecordList.get(0);
					if (null != consentRecord && !isComboReq) {
						if (null != consentRecord.getInLoop()
								&& !consentRecord.getInLoop().equalsIgnoreCase(
										"")) {
							if (consentRecord.getInLoop().equalsIgnoreCase("o")) {
								consentElem.setAttribute(SELECTION_TYPE, "1");

							} else if (consentRecord.getInLoop()
									.equalsIgnoreCase("l")) {
								consentElem.setAttribute(SELECTION_TYPE, "2");

							}
						} else {
							consentElem.setAttribute(SELECTION_TYPE, "0");
						}
						if (null != consentRecord.getRequestTime()) {
							requestDate=dFormat.format(consentRecord.getRequestTime());
							consentElem.setAttribute(REQUEST_TIME_STAMP, requestDate);
						} else {
							consentElem.setAttribute(REQUEST_TIME_STAMP, "");
						}
						
					}
				}
			}
			else {
				requestDate=dFormat.format(new Date());
				consentElem.setAttribute(REQUEST_TIME_STAMP, requestDate);
			}
		}
		return consentElem;
	}
	
	//RBT-13537 VF IN:: 2nd Consent Reporting for RBT
	private void writeCDRLog(Subscriber subscriber, WebServiceContext task, String consentId, String consentClassType, String consentSubClass,String requestType, String srvId, String srvclass) {
		//TIMESTAMP, TRANSACTION_TYPE, CIRCLE, MISIDN, REQEUST_TYPE, REQUEST_MODE, REQUEST_ID, SERVICE_ID, SERVICE_CLASS, SUBSCRIPTION_CLASS, CHARGE_CLASS, PROMO_ID, MODE_INFO 
		SimpleDateFormat dateFormat = new SimpleDateFormat(iRBTConstant.kDateFormatwithTime);
		String currentTime = dateFormat.format(new Date());
		String transactionType = "request";
		String circleId = subscriber!=null?subscriber.circleID():"";
		String subID = subscriber != null ? subscriber.subID() : rbtDBManager
				.subID(task.getString(param_subscriberID));
		String promoId="";
		String clipID = task.getString(param_clipID);
		String catIdStr = task.getString(param_categoryID);
		Category category =null; 
		if(catIdStr != null) {
		 int catId = Integer.parseInt(catIdStr);
		 category = rbtCacheManager.getCategory(catId);
		}
		if(category != null && Utility.isShuffleCategory(category.getCategoryTpe())) {
			promoId = category.getCategoryPromoId();
		  
		}else if(clipID != null){
			 Clip clip = rbtCacheManager.getClip(clipID);
			 if(clip != null)
			  promoId =  clip.getClipPromoId();
		}
		
		String requestMode = task.getString(param_mode);
		String modeInfoParam = task.getString(param_modeInfo);
		String modeInfo = "NA";
		//RBT-13642
		String response = task.getString("CGHttpResponse");
		String cgURL = task.getString("CGURL");
		String twShortCodeValue = WriteCDRLog.getTwShortCodeFromMode(modeInfoParam);
		if(twShortCodeValue != null) {
			modeInfo = twShortCodeValue;	
		}
		if(response != null && cgURL != null){
			cdr_logger.info(currentTime + "," + transactionType + "," + circleId
					+ "," + subID + "," + requestType + ","
					+ requestMode + "," + consentId + "," + srvId + "," + srvclass
					+ "," + consentSubClass + "," + consentClassType + ","
					+ promoId + "," + modeInfo + "," + response + "," + cgURL);	
		} else {
			cdr_logger.info(currentTime + "," + transactionType + "," + circleId
					+ "," + subID + "," + requestType + ","
					+ requestMode + "," + consentId + "," + srvId + "," + srvclass
					+ "," + consentSubClass + "," + consentClassType + ","
					+ promoId + "," + modeInfo);	
		}
	}
	
	protected Element getSubscriberMIPlayListElement(Document document,
			WebServiceContext task,
			WebServiceSubscriberDownload[] webServiceSubscriberDownloads) {
		
		    Element contentsElem = document.createElement(CONTENTS);
			for (WebServiceSubscriberDownload webServiceSubscriberDownload : webServiceSubscriberDownloads) {
				Element contentElem = getSubscriberDownloadContentElement(
						document, task, webServiceSubscriberDownload);
				contentsElem.appendChild(contentElem);
			}


		return contentsElem;
	}

	public static boolean isModeAllowedForUGC(WebServiceContext task,
			String toneType) {
		String modesCofig = null;
		String mode = task.getString(param_mode);
		boolean isUGCClip = (toneType.equalsIgnoreCase(CATEGORY_RECORD) || toneType
				.equalsIgnoreCase(CATEGORY_KARAOKE));
		Parameters modeConfParam = BasicXMLElementGenerator.parametersCacheManager
				.getParameter(iRBTConstant.COMMON,
						"ALLOWED_MODES_TO_ADD_UGC_CLIPS_DETAILS", null);
		logger.info("Inside isModeAllowedForUGC mode: " + mode
				+ ", isUGCClip: " + isUGCClip + ", modeConfParam: "
				+ modeConfParam);
		if (isUGCClip && mode != null && modeConfParam != null) {
			modesCofig = modeConfParam.getValue();
			if (null != modesCofig && !modesCofig.isEmpty()) {
				List<String> allowedModesToAddUGC = ListUtils.convertToList(
						modesCofig, ",");
				if (!allowedModesToAddUGC.contains(mode)) {
					return false;
				} else {
					return true;
				}
			}
		}
		return true;
	}
	
	// Added for Skipping Paid selection for free user
	private SubscriberStatus[] getSettingsForFreeUser(
			SubscriberStatus[] settings, String subscriberId) {

		logger.info("Inside getSettingsForFreeUser for : "+ subscriberId);
		String freeCatId = null;

		try {
			IUserDetailsService operatorUserDetailsService = null;
			operatorUserDetailsService = (IUserDetailsService) ConfigUtil
					.getBean(BeanConstant.USER_DETAIL_BEAN);
			OperatorUserDetails operatorDetails = (OperatorUserDetails) operatorUserDetailsService
					.getUserDetails(subscriberId);
			if (operatorDetails != null) {
				logger.info(":---> operatorDetails.serviceKey():  "
						+ operatorDetails.serviceKey());
			}

			if ((operatorUserDetailsService != null && operatorDetails != null)
					&& ((operatorDetails.serviceKey().equalsIgnoreCase(
							OperatorUserTypes.PAID_APP_USER_LOW_BALANCE
									.getDefaultValue()) || operatorDetails
							.serviceKey().equalsIgnoreCase(
									OperatorUserTypes.FREE_APP_USER
											.getDefaultValue())))) {

				freeCatId = ConsentPropertyConfigurator.getFreeClipCategoryID();
				logger.info("FreeclipCategoryId: " + freeCatId);
			}
		} catch (Exception e) {
			logger.info("Error occured while getting operator user type: ", e);
		}
		
		if(freeCatId != null && !freeCatId.isEmpty()) {
			ArrayList<SubscriberStatus> returnSettings = null;
			for (int i = 0; settings != null && i < settings.length; i++) {
				SubscriberStatus thisSelection = settings[i];
				logger.info(" :---> thisSelection.subscriberFile() :"+thisSelection.subscriberFile());
				if (thisSelection.categoryID() != Integer.parseInt(freeCatId)
						&& thisSelection.status() != 200) {
					logger.info(":---> Skipping the selection as Free user can't have song other than configured category and ephemeral");
					continue;
				}
				/*else if(thisSelection.status() != 200 && thisSelection.subscriberFile().contains("_cut_")
						|| (thisSelection.categoryID() != Integer.parseInt(freeCatId) && thisSelection.subscriberFile().contains("_cut_")))
				{
					logger.info(
							":---> Skipping the selection as Free user cant have cut rbt song");
					continue;
				}*/
				else {
					if(returnSettings == null){
						returnSettings = new ArrayList<SubscriberStatus>();
					}
					returnSettings.add(settings[i]);
				}

			}
			logger.info("returning settings by skipping ephemeral and specific content and cutrbt.");
			return returnSettings.toArray(new SubscriberStatus[0]);
		}
		return settings;
	}

}