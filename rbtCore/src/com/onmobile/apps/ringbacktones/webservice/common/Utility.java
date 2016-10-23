/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.vodafone.encrytion.EncryptionDecryptionUtil;
import com.jspsmart.upload.SRequest;
import com.jspsmart.upload.SmartUpload;
import com.jspsmart.upload.SmartUploadException;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.GCMRegistration;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.ExtraInfoKey;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.tcp.supporters.ViralPromotion;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.RBTProcessor;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

/**
 * @author vinayasimha.patil
 *
 */
public class Utility implements WebServiceConstants
{
	private static final Map<String, String> dbMgrResponseWSResponseMap = new HashMap<String, String>();
	
	private static Logger logger = Logger.getLogger(Utility.class);
	private static Logger renewalLogger=Logger.getLogger("renewalLogger");
	protected static RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
	
	//CategoryTypes for which start date needs to be updated to CategoryStartTime.
	private static List<String> festivalAndBoxOfficeShuffles = new ArrayList<String>();
	private static ParametersCacheManager m_rbtParamCacheManager = null;
	
	private static final String errorXML = "<" + RBT + "><" + RESPONSE + ">" + ERROR + "</" + RESPONSE + "></" + RBT + ">";
	
	private static Map<String,List<String>> modeIpAddressMapping = null;
	private static final Object object = new Object();
	public static final String WEBSERVICE = "WEBSERVICE";
	private static Set<String> consentConfModes = new HashSet<String>();
	//RBT-9213
	private static Set<String> consentConfVendors = new HashSet<String>();
	private static Map<String,String> returnCodeToStringMap = new HashMap<String,String>();
	private static 	List<String> cosOverrideChargeClassList = new ArrayList<String>();
	private static List<String> musicPackCosTypesForSelectionsLoopList = new ArrayList<String>();
	//JIRA-ID: RBT-13626
	private static Map<String, String> premiumChargeClassMap = new HashMap<String, String>();
	private static String premiumChargeClass = RBTParametersUtils.getParamAsString(
			iRBTConstant.COMMON, PREMIUM_SELECTION_CHARGE_CLASS, null);
	static List<String> configuredAppNamesForTPSupport = null;
	//AT-104100
	private static List<List<Integer>> blackoutTimesList = null;
	enum OperatorSupportedLang {ENG,BEN,URD};
	
	static {
		m_rbtParamCacheManager =  CacheManagerUtil.getParametersCacheManager();
		
		//CategoryTypes for which start date needs to be updated to CategoryStartTime.
		festivalAndBoxOfficeShuffles = Arrays.asList(getParamAsString("COMMON", "OVERRIDE_SHUFFLE_CATEGORY_TYPES","10").trim().split(","));
		
		 String cosOverrideChargeClass = m_rbtParamCacheManager.getParameterValue(
				"COMMON", "COS_OVERRIDE_CHARGE_CLASS", null);

		if (cosOverrideChargeClass != null) {
			cosOverrideChargeClassList = Arrays.asList(cosOverrideChargeClass
					.split(","));
		}
		
		String musicPackCosTypesForSelectionsLoopString = m_rbtParamCacheManager
				.getParameterValue(iRBTConstant.COMMON,iRBTConstant.MUSIC_PACK_COS_TYPES_FOR_SELECTIONS_LOOP, null);
		logger.info("Parameter MUSIC_PACK_COS_TYPES_FOR_SELECTIONS_LOOP: " + musicPackCosTypesForSelectionsLoopString);
		musicPackCosTypesForSelectionsLoopList = ListUtils.convertToList(musicPackCosTypesForSelectionsLoopString, ",");	
		logger.info("musicPackCosTypesForSelectionsLoopList: " + musicPackCosTypesForSelectionsLoopList);		
		String premiumChargeClassMapStr = RBTParametersUtils.getParamAsString(
				iRBTConstant.COMMON, PREMIUM_SELECTION_CHARGE_CLASS_MAP, null);
		if (null != premiumChargeClassMapStr) {
			premiumChargeClassMap = MapUtils.convertToMap(
					premiumChargeClassMapStr.toUpperCase(), ",", "=", null);
		}
		logger.info("premiumChargeClassMap: " + premiumChargeClassMap);
		
		String configuredAppNamesForTPSupportString = RBTParametersUtils
				.getParamAsString("MOBILEAPP",
						WebServiceConstants.APP_NAMES_FOR_REAL_TP_SUPPORT, null);
		logger.info("APP_NAMES_FOR_REAL_TP_SUPPORT: " + configuredAppNamesForTPSupportString);
		configuredAppNamesForTPSupport = ListUtils.convertToList(configuredAppNamesForTPSupportString, ",");
		logger.info("configuredAppNamesForTPSupport: " + configuredAppNamesForTPSupport);
		
		logger.info("initializing BlackOut time");
		initializeBlackOut();

	}
		
	/**
	 * Checks if the language is configured then returns the corresponding language code else returns default one.
	 * @param language
	 * @return language code
	 */
	public static String getLanguageCode(String language){
		if( null ==  language ){
			language  =  RBTParametersUtils.getParamAsString( "ALL", "DEFAULT_LANGUAGE", "eng" );
		}
		
		for (OperatorSupportedLang lang : OperatorSupportedLang.values()) {
			if (language.equals(lang.toString())) {
				language = lang.toString();
				break;
			}
		}
		return language;
	}
	
	public static String getPlanId( String subscriptionClass ){
		String serviceKeyPlainIdMapping = RBTParametersUtils.getParamAsString( iRBTConstant.CONSENT, "BASE_SERVICE_KEY_PLAN_ID_MAPPING", null );
		
		Map<String,String> serviceKeyPlanidMap = MapUtils.convertIntoMap( serviceKeyPlainIdMapping, ";", ":", null );
	    return serviceKeyPlanidMap.get( subscriptionClass );
	}
	
	public static String getCosOverrideClass(Clip clip, boolean useUIChargeClass, Subscriber subscriber) {

		String clipChargeClass = null;
		if (clip != null && !useUIChargeClass) {
			clipChargeClass = clip.getClassType();
		}

		String cosType = null;
		if (subscriber != null) {
			String cosId = subscriber.cosID();
			if (cosId != null) {
				CosDetails cosDetails = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosId);
				if (cosDetails != null) {
					cosType = cosDetails.getCosType();
					logger.debug("cosType: " + cosType);
				}
			}
		}
		if (cosType == null || !cosType.equals(iRBTConstant.COS_TYPE_PPU)) {
			if (cosOverrideChargeClassList != null
					&& cosOverrideChargeClassList.contains(clipChargeClass)) {
				logger.info("CosOverrideClass: " + clipChargeClass);
				return clipChargeClass;
			}
		}
		logger.info("CosOverrideClass: null");
		return null;

	}
	public static boolean isValidSuspensionRequestDownloads(
			SubscriberDownloads downloads, boolean suspend,
			ArrayList<String> corpCatIDs)
	{
		boolean returnFlag = false;
		if (suspend)
		{
			if (corpCatIDs != null && !corpCatIDs.contains("" + downloads.categoryID()))
			{
				String extraInfo = downloads.extraInfo();
				HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				if (extraInfoMap != null && extraInfoMap.containsKey("VOLUNTARY"))
				{
					String value = extraInfoMap.get("VOLUNTARY");
					if (value.equalsIgnoreCase("TRUE"))
						return false;
				}

				returnFlag = true;
			}
		}
		else
		{
			returnFlag = true;
		}

		return returnFlag;
	}

	public static boolean isValidIP(String ipAddress)
	{
		if (ipAddress == null)
		{
			logger.info("RBT:: IP: " + ipAddress + " isValidIP: " + true);
			return true;
		}

		Parameters validIPsParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "VALID_IP", "127.0.0.1");
		String validIPs = validIPsParam.getValue().trim();

		List<String> validIPList = Arrays.asList(validIPs.split(","));
		boolean valid = validIPList.contains(ipAddress);

		if(!valid)
		{ //log the IP only if IP is not valid 
			logger.info("RBT:: IP: " + ipAddress + " isValidIP: " + valid);
		}
		return valid;
	}

	public static String getInvalidIPXML()
	{
		return getResponseXML(INVALID_IP);
	}

	public static String getRequestPendingXML()
	{
		return getResponseXML(REQUEST_PENDING);
	}

	public static String getResponseXML(String response)
	{
		Document document = getResponseDocument(response);
		return XMLUtils.getStringFromDocument(document);
	}
	
	public static Document getResponseDocument(String response)
	{
		DocumentBuilder documentBuilder = XMLUtils.getDocumentBuilder();

		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		Element invalidIPElem = getResponseElement(document, response);
		element.appendChild(invalidIPElem);
		return document;
	}
	
	public static Document getMobileAppNotificationDocument(String response, GCMRegistration[] gcmRegistrations, String smsText)
	{
		DocumentBuilder documentBuilder = XMLUtils.getDocumentBuilder();

		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		Element invalidIPElem = getResponseElement(document, response);
		element.appendChild(invalidIPElem);
		
//		Element notificationSmsElement = document.createElement(NOTIFICATION_SMS);
//		Text text = document.createTextNode(smsText);
//		notificationSmsElement.appendChild(text);
//		element.appendChild(notificationSmsElement);
		
		
		Element registerIdsElem = getMobileAppNotificationElement(document, gcmRegistrations);
		element.appendChild(registerIdsElem);
		return document;
	}
	
	private static Element getMobileAppNotificationElement(Document document, GCMRegistration[] gcmRegistrations) {		
		Element element = document.createElement(REGISTERIDS);
		
		for(GCMRegistration gcmRegistration : gcmRegistrations) {
			Element ele = document.createElement(REGISTERID);
			Text text = document.createTextNode(gcmRegistration.registrationID());
			ele.appendChild(text);
			ele.setAttribute(SUBSCRIBER_ID, gcmRegistration.subscriberID());
			element.appendChild(ele);
		}
		
		return element;
	}

	public static Element getResponseElement(Document document, String response)
	{
		Element element = document.createElement(RESPONSE);
		Text text = document.createTextNode(response);
		element.appendChild(text);

		return element;
	}

	public static HashMap<String,String>  getMapFromXML(String rootName ,String xml)
	{
		HashMap<String,String> xmlMap = new HashMap<String,String>() ;
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();

			InputSource input = new InputSource(new StringReader(xml));
			Document document = builder.parse(input);

			NodeList nodeList = document.getElementsByTagName(rootName);

			NodeList n = nodeList.item(0).getChildNodes();
			System.out.println(n.getLength());
			for (int i = 0; i < n.getLength(); i++)
			{
				Node node = n.item(i);
				xmlMap.put(node.getNodeName(), node.getTextContent());
			}
			
			return xmlMap;

		} catch (ParserConfigurationException e) {
			logger.error("",e);
		} catch (SAXException e) {
			logger.error("",e);
		} catch (IOException e) {
			logger.error("",e);
		}
		return xmlMap;

	}
	
	public static String getErrorXML()
	{
		return errorXML;
	}
	
	public static WebServiceResponse getWebServiceResponseXML(Document document)
	{
		String response = XMLUtils.getStringFromDocument(document);

		WebServiceResponse webServiceResponse = new WebServiceResponse(response);
		webServiceResponse.setContentType("text/xml; charset=utf-8");
		ResponseWriter responseWriter = WebServiceResponseFactory
				.getResponseWriter(StringResponseWriter.class);
		webServiceResponse.setResponseWriter(responseWriter);
		
		return webServiceResponse;
	}

	public static void addPropertyElement(Document document, Element parentElem,
			String propertyName, String propertyType, String propertyValue)
	{
		if (propertyValue == null)
			return;

		Element propertyElem = document.createElement(PROPERTY);

		propertyElem.setAttribute(NAME, propertyName);

		if (propertyType != null)
			propertyElem.setAttribute(TYPE, propertyType);

		propertyElem.setAttribute(VALUE, propertyValue);

		parentElem.appendChild(propertyElem);
	}

	public static Element getPropertyElement(Element element, String property)
	{
		NodeList contentNodeList = element.getChildNodes();
		for (int i = 0; i < contentNodeList.getLength(); i++)
		{
			Element propertyElem = (Element) contentNodeList.item(i);
			String name = propertyElem.getAttribute(NAME);
			if (name != null && name.equals(property))
				return propertyElem;
		}

		return null;
	}

	public static String getPromptName(String prompt)
	{
		return getPromptName(prompt,null);
	}
	
	public static String getPromptName(String prompt, String format) {
		if (prompt == null)
			return null;
		String formatString  = "wav";
		if(format != null && (format = format.trim()).length() != 0) {
			formatString = format;
		}
		String returnString = prompt;
		if (prompt.endsWith(".wav")){
			returnString = prompt.substring(0, prompt.lastIndexOf(".wav"));
		}
		return (returnString + "." + formatString);
	}
	
	public static boolean isCategoryType(String contentType)
	{
		if (contentType.equalsIgnoreCase(CATEGORY)
				|| contentType.equalsIgnoreCase(CATEGORY_PARENT)
				|| contentType.equalsIgnoreCase(CATEGORY_BOUQUET))
			return true;

		return false;
	}

	public static String getCategoryType(int categoryType)
	{
		String contentType = CATEGORY_PARENT;
		if (categoryType == iRBTConstant.SHUFFLE)
			contentType = CATEGORY_SHUFFLE;
		else if (categoryType == iRBTConstant.LIST)
			contentType = CATEGORY_LIST_CLIPS;
		else if (categoryType == iRBTConstant.SOUNDS)
			contentType = CATEGORY_SOUNDS;
		else if (categoryType == iRBTConstant.BOUQUET)
			contentType = CATEGORY_BOUQUET;
		else if (categoryType == iRBTConstant.RECORD)
			contentType = CATEGORY_RECORD;
		else if (categoryType == iRBTConstant.SONGS)
			contentType = CATEGORY_SONGS;
		else if (categoryType == iRBTConstant.PARENT)
			contentType = CATEGORY_PARENT;
		else if (categoryType == iRBTConstant.DTMF_CATEGORY)
			contentType = CATEGORY_DTMF_CLIPS;
		else if (categoryType == iRBTConstant.KARAOKE)
			contentType = CATEGORY_KARAOKE;
		else if (categoryType == iRBTConstant.INFO_CATEGORY)
			contentType = CATEGORY_INFO;
		else if (categoryType == iRBTConstant.DYNAMIC_SHUFFLE)
			contentType = CATEGORY_DYNAMIC_SHUFFLE;
		else if (categoryType == iRBTConstant.FEED_CATEGORY)
			contentType = CATEGORY_FEED;
		else if (categoryType == iRBTConstant.ODA_SHUFFLE)
			contentType = CATEGORY_ODA_SHUFFLE;
		else if (categoryType == iRBTConstant.BOX_OFFICE_SHUFFLE)
			contentType = CATEGORY_BOX_OFFICE_SHUFFLE;
		else if (categoryType == iRBTConstant.FESTIVAL_SHUFFLE)
			contentType = CATEGORY_FESTIVAL_SHUFFLE;
		else if (categoryType == iRBTConstant.FEED_SHUFFLE)
			contentType = CATEGORY_FEEED_SHUFFLE;
		else if (categoryType == iRBTConstant.DAILY_SHUFFLE)
			contentType = CATEGORY_DAILY_SHUFFLE;

		return contentType;
	}

	public static boolean isShuffleCategory(int categoryType)
	{
		return (categoryType == iRBTConstant.SHUFFLE
				|| categoryType == iRBTConstant.WEEKLY_SHUFFLE
				|| categoryType == iRBTConstant.DAILY_SHUFFLE
				|| categoryType == iRBTConstant.MONTHLY_SHUFFLE
				|| categoryType == iRBTConstant.TIME_OF_DAY_SHUFFLE
				|| categoryType == iRBTConstant.ODA_SHUFFLE
				|| categoryType == iRBTConstant.BOX_OFFICE_SHUFFLE
				|| categoryType == iRBTConstant.FESTIVAL_SHUFFLE
				|| categoryType == iRBTConstant.FEED_SHUFFLE
				|| categoryType == iRBTConstant.MONTHLY_ODA_SHUFFLE
				|| categoryType == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE
				|| categoryType == iRBTConstant.OVERRIDE_MONTHLY_SHUFFLE
				|| categoryType == iRBTConstant.PHONE_RADIO_SHUFFLE
				|| categoryType == iRBTConstant.FESTIVAL_NAMETUNES_SHUFFLE
				|| categoryType == iRBTConstant.PLAYLIST_ODA_SHUFFLE); 
	}

	public static boolean isCallerIDSame(String callerID1,String callerID2)
	{
		if(callerID1 == null)
			callerID1 = "ALL";
		if(callerID2 == null)
			callerID2 = "ALL";
		if(callerID1.equalsIgnoreCase(callerID2))
			return true;
		
		return false;
	}
	
	public static String getSubscriberStatus(Subscriber subscriber)
	{
		return getSubscriberStatus(subscriber, false);
	}
	
	public static String getSubscriberStatus(Subscriber subscriber, boolean considerErrorState)
	{
		String status = ERROR;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (subscriber == null)
			status = NEW_USER;
		else if (rbtDBManager.isSubscriberActivationPending(subscriber))
		{
			if (considerErrorState && subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_ACTIVATION_ERROR))
				status = ACT_ERROR;
			else
				status = ACT_PENDING;
		}
		else if (rbtDBManager.isSubscriberActivated(subscriber))
		{
			// Check if the user is locked
			HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
			if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_LOCK_USER)
					&& extraInfoMap.get(iRBTConstant.EXTRA_INFO_LOCK_USER).equalsIgnoreCase(iRBTConstant.EXTRA_INFO_LOCK_USER_TRUE))
				status = LOCKED;
			else
				status = ACTIVE;
		}
		else if (rbtDBManager.isSubscriberRenewalPending(subscriber))
			status = RENEWAL_PENDING;
		else if (rbtDBManager.isSubscriberDeactivationPending(subscriber))
		{
			if (considerErrorState && subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_DEACTIVATION_ERROR))
				status = DEACT_ERROR;
			else
				status = DEACT_PENDING;
		}
		else if (rbtDBManager.isSubscriberDeactivated(subscriber))
			status = DEACTIVE;
		else if (rbtDBManager.isSubscriberInGrace(subscriber))
			status = GRACE;
		else if (rbtDBManager.isSubscriberSuspended(subscriber))
		{
			// Voluntarily Suspended users will be considered as ACTIVE users
			HashMap<String, String> extraInfo = rbtDBManager.getExtraInfoMap(subscriber);
			if (extraInfo != null && extraInfo.containsKey(iRBTConstant.VOLUNTARY) && extraInfo.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("TRUE"))
				status = ACTIVE;
			else if(extraInfo == null || (!extraInfo.containsKey(iRBTConstant.VOLUNTARY) || extraInfo.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("SM_SUSPENDED")))
				status = SUSPENDED;

			if (subscriber.nextChargingDate() == null)
				status = ACTIVATION_SUSPENDED;
		}

		return status;
	}

	public static String getSubscriberSettingStatus(SubscriberStatus setting)
	{
		return getSubscriberSettingStatus(setting, false);
	}
	
	public static String getSubscriberSettingStatus(SubscriberStatus setting, boolean considerErrorState)
	{
		String status = ERROR;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (rbtDBManager.isSelectionActivationPending(setting))
		{
			if (considerErrorState && setting.selStatus().equalsIgnoreCase(iRBTConstant.STATE_ACTIVATION_ERROR))
				status = ACT_ERROR;
			else
				status = ACT_PENDING;
		}
		else if (rbtDBManager.isSelectionActivated(setting))
			status = ACTIVE;
		else if (rbtDBManager.isSelectionDeactivationPending(setting))
		{
			if (considerErrorState && setting.selStatus().equalsIgnoreCase(iRBTConstant.STATE_DEACTIVATION_ERROR))
				status = DEACT_ERROR;
			else
				status = DEACT_PENDING;
		}
		else if (rbtDBManager.isSelectionDeactivated(setting))
			status = DEACTIVE;
		else if (rbtDBManager.isSelectionSuspended(setting))
			status = SUSPENDED;
		else if (rbtDBManager.isSelectionGrace(setting))
			status = GRACE;

		return status;
	}

	public static String getSubscriberDownloadStatus(SubscriberDownloads download)
	{
		String status = ERROR;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (rbtDBManager.isDownloadActivationPending(download))
			status = ACT_PENDING;
		else if (rbtDBManager.isDownloadActivated(download))
			status = ACTIVE;
		else if (rbtDBManager.isDownloadDeactivationPending(download)
				&& (download.selectionInfo() != null)
				&& download.selectionInfo().toLowerCase().contains(
						"refund:true"))
			status = REFUND_PENDING;
		else if (rbtDBManager.isDownloadDeactivated(download)
				&& (download.selectionInfo() != null)
				&& download.selectionInfo().toLowerCase().contains(
						"refund:true"))
			status = REFUNDED;
		else if (rbtDBManager.isDownloadDeactivationPending(download))
			status = DEACT_PENDING;
		else if (rbtDBManager.isDownloadDeactivated(download))
			status = DEACTIVE;
		else if (rbtDBManager.isDownloadSuspended(download))
			status = SUSPENDED;
		else if (rbtDBManager.isDownloadGrace(download))
			status = GRACE;
		

		return status;
	}

	public static String getSubscriberSettingType(SubscriberStatus setting)
	{
		String type = NORMAL;

		if (setting.selType() == 2)
			type = CORPORATE;

		return type;
	}

	public static String getSubscriberDownloadType(SubscriberDownloads download)
	{
		String type = NORMAL;

		String extraInfo = download.extraInfo();
		if (extraInfo != null && extraInfo.contains(CAMPAIGN_ID))
			type = CORPORATE;

		return type;
	}

	public static boolean isUserActive(String subscriberStatus)
	{
		if (subscriberStatus.equalsIgnoreCase(ACT_PENDING)
				|| subscriberStatus.equalsIgnoreCase(ACTIVE)
				|| subscriberStatus.equalsIgnoreCase(RENEWAL_PENDING)
				|| subscriberStatus.equalsIgnoreCase(GRACE)
				|| subscriberStatus.equalsIgnoreCase(SUSPENDED))
			return true;

		return false;
	}
	
	public static boolean isPackActive(int packStatus)
	{
		if (packStatus==iRBTConstant.PACK_ACTIVATION_PENDING
				|| packStatus==iRBTConstant.PACK_ACTIVATED
				|| packStatus==iRBTConstant.PACK_TO_BE_ACTIVATED
				|| packStatus==iRBTConstant.BASE_ACTIVATION_PENDING)
			return true;

		return false;
	}

	public static int getValidityPeriod(String period)
	{
		boolean isDefaultFormat = false;

		int multiplierFactor = 1;
		char ch = period.charAt(0);
		if (ch == 'D' || ch == 'd')
			multiplierFactor = 1;
		else if (ch == 'M' || ch == 'm')
			multiplierFactor = 30;
		else if (ch == 'Y' || ch == 'y')
			multiplierFactor = 365;
		else
			isDefaultFormat = true;

		int validityPeriod = Integer.parseInt(isDefaultFormat ? period : period.substring(1)) * multiplierFactor;

		logger.info("RBT:: period: " + period + " validityPeriod: " + validityPeriod);
		return validityPeriod;
	}

	public static boolean isAdvanceRentalPack(String subscriptionClass)
	{
		Parameters advanceRentalPacksParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "ADVANCE_PACKS", null);
		if (advanceRentalPacksParam != null)
		{
			List<String> advanceRentalPackList = Arrays.asList(advanceRentalPacksParam.getValue().split(","));
			return advanceRentalPackList.contains(subscriptionClass);
		}

		return false;
	}

	public static boolean isValidSelectionInterval(String interval)
	{
		if (interval == null)
			return true;

		String[] intervals = interval.split(",");

		// Checking for duplicate
		List<String> intervalList = new ArrayList<String>();
		for (String intervalToken : intervals)
		{
			if (intervalList.contains(intervalToken))
				return false;

			intervalList.add(intervalToken);
		}

		if (interval.startsWith("W"))
		{
			// Day of The Week

			for (String intervalToken : intervals)
			{
				if (intervalToken.length() != 2 || !intervalToken.startsWith("W"))
					return false;

				try
				{
					int dayOfTheWeek = Integer.parseInt(intervalToken.substring(1));
					if (dayOfTheWeek < Calendar.SUNDAY || dayOfTheWeek > Calendar.SATURDAY)
						return false;
				}
				catch (NumberFormatException e)
				{
					return false;
				}
			}
		}
		else if (interval.startsWith("M"))
		{
			// Day of The Month
			//RBT-9999	Added for validating month based selections		
			if(isMonthBasedInterval(interval) && isMonthBasedDateValid(interval)) {
				logger.info("Returning true for interval: "+interval);
				return true;
			}
			for (String intervalToken : intervals) {
				if (intervalToken.length() < 2 || intervalToken.length() > 3)
					return false;
				if (intervalToken.charAt(1) == '0'
						|| !intervalToken.startsWith("M"))
					return false;

				try {
					int dayOfTheMonth = Integer.parseInt(intervalToken
							.substring(1));
					if (dayOfTheMonth < 1 || dayOfTheMonth > 31)
						return false;
				} catch (NumberFormatException e) {
					return false;
				}
			}

		}
		else if (interval.startsWith("Y"))
		{
			// Special Date

			for (String intervalToken : intervals)
			{
				if ((intervalToken.length() != 5 && intervalToken.length() != 9) || !intervalToken.startsWith("Y"))
					return false;

				try
				{
					SimpleDateFormat dateFormat = null;
					if (intervalToken.length() == 9)
					{
						dateFormat = new SimpleDateFormat("ddMMyyyy");
						Date currentDate = new Date();
						Date parseDate = dateFormat.parse(intervalToken.substring(1));
						if (parseDate.before(currentDate) || parseDate.equals(currentDate))
						{
							return false;
						}
					}
					else
						dateFormat = new SimpleDateFormat("ddMM");

					// For supporting recursive selections on Feb29th date
					if (!intervalToken.equals("Y2902"))
					{
						dateFormat.setLenient(false);
						dateFormat.parse(intervalToken.substring(1));
					}
				}
				catch (ParseException e)
				{
					return false;
				}
			}
		}
		else {
			logger.warn("Invalid interval passed: " + interval);
			return false;
		}

		return true;
	}

	public static String getResponseString(String string)
	{
		String response = ERROR;

		try
		{
			if (dbMgrResponseWSResponseMap.size() == 0)
			{
				//General responses
				dbMgrResponseWSResponseMap.put("SUCCESS", SUCCESS);
				dbMgrResponseWSResponseMap.put("FAILED", FAILED);
				dbMgrResponseWSResponseMap.put("ALREADY_EXISTS", ALREADY_EXISTS);

				//Deactivation responses
				dbMgrResponseWSResponseMap.put("ACT_PENDING", ACT_PENDING);
				dbMgrResponseWSResponseMap.put("DCT_NOT_ALLOWED", DCT_NOT_ALLOWED);

				//Add Selection responses
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_SUCCESS, SUCCESS);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_SUBSCRIBER_SUSPENDED, SUSPENDED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED, SELECTION_SUSPENDED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_NULL_WAV_FILE, CLIP_NOT_EXISTS);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_CLIP_EXPIRED, CLIP_EXPIRED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_CATEGORY_EXPIRED, CATEGORY_EXPIRED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_TNB_TO_DEFAULT_FAILED, FAILED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_SELECTION_OVERLAP, ALREADY_EXISTS);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_INTERNAL_ERROR, TECHNICAL_DIFFICULTIES);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_CALLER_ALREADY_IN_GROUP, ALREADY_MEMBER_OF_GROUP);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_ADRBT_FOR_PROFILES_OR_CORPORATE, NOT_ALLOWED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_ADRBT_FOR_SHUFFLES, NOT_ALLOWED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_ADRBT_FOR_SPECIFIC_CALLER, NOT_ALLOWED);
				dbMgrResponseWSResponseMap.put("SELECTION_FAILED_SELECTION_DOES_NOT_EXIST", NOT_EXISTS);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_OWN_NUMBER, OWN_NUMBER);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_CALLER_BLOCKED, CALLER_BLOCKED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS, SUCCESS_DOWNLOAD_EXISTS);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_SELECTION_LIMIT_REACHED, SELECTION_OVERLIMIT);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_CALLERID_LIMIT_REACHED, PERSONAL_SELECTION_OVERLIMIT);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SELECTION_FAILED_LOOP_SELECTION_LIMIT_REACHED, LOOP_SELECTION_OVERLIMIT);
				dbMgrResponseWSResponseMap.put(iRBTConstant.RBT_CORPORATE_NOTALLOW_SELECTION, RBT_CORPORATE_NOTALLOW_SELECTION);

				//Add Download responses
				dbMgrResponseWSResponseMap.put("SUCCESS:DOWNLOAD_ADDED", SUCCESS);
				dbMgrResponseWSResponseMap.put("SUCCESS:DOWNLOAD_REACTIVATED", SUCCESS);
				dbMgrResponseWSResponseMap.put("SUCCESS:DOWNLOAD_ALREADY_ACTIVE", ALREADY_ACTIVE);
				dbMgrResponseWSResponseMap.put("SUCCESS:DOWNLOAD_PENDING_ACTIAVTION", ACT_PENDING);
				dbMgrResponseWSResponseMap.put("FAILURE:DOWNLOAD_DEACT_PENDING", DEACT_PENDING);
				dbMgrResponseWSResponseMap.put("FAILURE:DOWNLOAD_OVERLIMIT", OVERLIMIT);
				dbMgrResponseWSResponseMap.put("FAILURE:TECHNICAL_FAULT", TECHNICAL_DIFFICULTIES);
				dbMgrResponseWSResponseMap.put("FAILURE:DOWNLOAD_ERROR", ERROR_STATE);
				dbMgrResponseWSResponseMap.put("FAILURE:DOWNLOAD_SUSPENDED", DOWNLOAD_SUSPENDED);
				dbMgrResponseWSResponseMap.put("SUCCESS:DOWNLOAD_GRACE", DOWNLOAD_GRACE);
				dbMgrResponseWSResponseMap.put(iRBTConstant.DOWNLOAD_FAILED_CLIP_EXPIRED, CLIP_EXPIRED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.DOWNLOAD_FAILED_CATEGORY_EXPIRED, CATEGORY_EXPIRED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.PACK_DOWNLOAD_LIMIT_REACHED, PACK_DOWNLOAD_LIMIT_REACHED);
				
				//Add Group responses
				dbMgrResponseWSResponseMap.put(iRBTConstant.GROUP_ADDED_SUCCESFULLY, SUCCESS);
				dbMgrResponseWSResponseMap.put(iRBTConstant.GROUP_ADD_FAILED_GROUPNAME_NULL, INVALID_PARAMETER);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SAME_PREGROUP_EXISTS_FOR_CALLER, ALREADY_EXISTS);
				dbMgrResponseWSResponseMap.put(iRBTConstant.SAME_GROUP_NAME_EXISTS_FOR_CALLER, ALREADY_EXISTS);
				dbMgrResponseWSResponseMap.put(iRBTConstant.MAX_GROUP_PRESENT_FOR_SUBSCRIBER, OVERLIMIT);
				dbMgrResponseWSResponseMap.put(iRBTConstant.GROUP_ADD_FAILED_INTERNAL_ERROR, TECHNICAL_DIFFICULTIES);
				dbMgrResponseWSResponseMap.put(iRBTConstant.USER_NOT_ACTIVE, USER_NOT_ACTIVE);

				//Add Group Member responses
				dbMgrResponseWSResponseMap.put(iRBTConstant.CALLER_ADDED_TO_GROUP, SUCCESS);
				dbMgrResponseWSResponseMap.put(iRBTConstant.CALLER_ALREADY_PRESENT_IN_GROUP, ALREADY_MEMBER_OF_GROUP);
				dbMgrResponseWSResponseMap.put(iRBTConstant.ALREADY_BLOCKED, ALREADY_BLOCKED);
				dbMgrResponseWSResponseMap.put(iRBTConstant.ALREADY_PERSONALIZED_SELECTION_FOR_CALLER, SETTING_EXISTS_FOR_MEMBER);
				dbMgrResponseWSResponseMap.put(iRBTConstant.MAX_CALLER_PRESENT_IN_GROUP, OVERLIMIT);
				dbMgrResponseWSResponseMap.put(iRBTConstant.CALLER_NOT_ADDED_INTERNAL_ERROR, TECHNICAL_DIFFICULTIES);
				
				//Add bookmark responses
				dbMgrResponseWSResponseMap.put("ALREADY_DOWNLOADED", ALREADY_DOWNLOADED);
				dbMgrResponseWSResponseMap.put("DOWNLOAD_MONTHLY_LIMIT_REACHED", DOWNLOAD_MONTHLY_LIMIT_REACHED);

			}

			response = dbMgrResponseWSResponseMap.get(string);
			if (null == response)
				response = ERROR;
		}
		catch (Exception e)
		{
			logger.error("getResponseString", e);
		}

		logger.info("RBT:: string = " + string + ", response = " + response);
		return response;
	}
	
	public static void sqlInjectionInRequestParam(HashMap<String , String> requestParams){
	    Set<Entry<String , String>> entrySet = requestParams.entrySet();  
	    for(Entry<String , String> entry : entrySet){
	    	String value = entry.getValue();
	    
			if (value != null)
		      {
				  StringBuilder stringBuilder = new StringBuilder();
		          int from = 0;
		          int next;
		          while ((next = value.indexOf('\'', from)) != -1)
		          {
		              stringBuilder.append(value.substring(from, next + 1));
		              stringBuilder.append('\'');
		              from = next + 1;
		          }
	
		          if (from < value.length()) {
		              stringBuilder.append(value.substring(from));
		          }
		          entry.setValue(stringBuilder.toString());
		    }
	   }

	}

	public static HashMap<String, String> getRequestParamsMap(
			ServletConfig servletConfig, HttpServletRequest request,
			HttpServletResponse response, String api)
			{
		HashMap<String, String> requestParams = new HashMap<String, String>();

		if (isMultipartContent(request))
		{
			try
			{
				SmartUpload smartUpload = new SmartUpload();
				smartUpload.initialize(servletConfig, request, response);
				smartUpload.setTotalMaxFileSize(20000000);
				smartUpload.upload();

				for (int i = 0; i < smartUpload.getFiles().getCount(); i++)
				{
					com.jspsmart.upload.File file = smartUpload.getFiles().getFile(i);
					if (file.getSize() > 0)
					{			
						String fieldName = file.getFieldName();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
						String fileName = "BulkTask-" + dateFormat.format(new Date()) + ".txt";
						String tmpDir = System.getProperty("java.io.tmpdir");
						File savedFile = new File(tmpDir, fileName);
						file.saveAs(savedFile.getAbsolutePath());

						requestParams.put(fieldName, savedFile.getAbsolutePath());
					}
				}

				SRequest smartUploadRequest = smartUpload.getRequest();

				@SuppressWarnings("unchecked")
				Enumeration<String> params = smartUploadRequest.getParameterNames();
				while (params.hasMoreElements())
				{
					String key = params.nextElement();
					String value = smartUploadRequest.getParameter(key).trim();
					requestParams.put(key, value);
				}

			}
			catch (SmartUploadException e)
			{
				logger.error("getRequestParamsMap", e);
			}
			catch (IOException e)
			{
				logger.error("getRequestParamsMap", e);
			}
			catch (ServletException e)
			{
				logger.error("getRequestParamsMap", e);
			}
		}
		else
		{
			Enumeration<String> params = request.getParameterNames();
			while (params.hasMoreElements())
			{
				String key = params.nextElement();
				String value = request.getParameter(key).trim();
				requestParams.put(key, value);
			}
		}

		requestParams.put(param_api, api);

		Parameters validateIPParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "VALIDATE_IP", "TRUE");
		boolean validateIP = validateIPParam.getValue().trim().equalsIgnoreCase("TRUE");

		if (validateIP)
		{
			String ipAddress = request.getRemoteAddr();
			requestParams.put(param_ipAddress, ipAddress);
		}

		return requestParams;
	}

	public static boolean isMultipartContent(HttpServletRequest request)
	{
		if (!request.getMethod().toLowerCase().equals("post"))
			return false;

		String contentType = request.getContentType();
		if (contentType == null)
			return false;

		return (contentType.toLowerCase().startsWith("multipart/"));
	}

	public static WebServiceContext getTask(HashMap<String, String> requestParams)
	{
		Parameters trimCountryPrefixParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.WEBSERVICE, "TRIM_COUNTRY_PREFIX", "TRUE");
		if (trimCountryPrefixParam.getValue().equalsIgnoreCase("TRUE"))
		{
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			if (requestParams.containsKey(param_subscriberID))
				requestParams.put(param_subscriberID, rbtDBManager.subID(requestParams.get(param_subscriberID)));
			if (requestParams.containsKey(param_callerID))
				requestParams.put(param_callerID, rbtDBManager.subID(requestParams.get(param_callerID)));
			if (requestParams.containsKey(param_gifterID))
				requestParams.put(param_gifterID, rbtDBManager.subID(requestParams.get(param_gifterID)));
			if (requestParams.containsKey(param_gifteeID))
				requestParams.put(param_gifteeID, rbtDBManager.subID(requestParams.get(param_gifteeID)));
			if (requestParams.containsKey(param_fromSubscriber))
				requestParams.put(param_fromSubscriber, rbtDBManager.subID(requestParams.get(param_fromSubscriber)));
			if (requestParams.containsKey(param_retailerID))
				requestParams.put(param_retailerID, rbtDBManager.subID(requestParams.get(param_retailerID)));
			if (requestParams.containsKey(param_newCallerID))
				requestParams.put(param_newCallerID, rbtDBManager.subID(requestParams.get(param_newCallerID)));
			if (requestParams.containsKey(param_receiverID))
				requestParams.put(param_receiverID, rbtDBManager.subID(requestParams.get(param_receiverID)));
			if (requestParams.containsKey(param_memberID))
				requestParams.put(param_memberID, rbtDBManager.subID(requestParams.get(param_memberID)));
		}

		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);

		List<String> unwantedKeysList = new ArrayList<String>();
		Set<Entry<String, Object>> entrySet = taskSession.entrySet();
		for (Entry<String,Object> entry : entrySet)
		{
			String value = (String) entry.getValue();
			if (value == null || value.length() == 0 || value.equalsIgnoreCase("null"))
				unwantedKeysList.add(entry.getKey());
							
		}

		for (String key : unwantedKeysList)
		{
			taskSession.remove(key);
		}
		
		WebServiceContext task = new WebServiceContext(taskSession);
		return task;
	}

	public static void setSubMgrProxy(HttpParameters httpParameters)
	{
		if (httpParameters == null)
			return;

		Parameters subMgrProxyParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "SUBMGR_PROXY");
		if (subMgrProxyParam != null)
		{
			httpParameters.setUseProxy(true);

			String[] proxyIPnPort = subMgrProxyParam.getValue().split(":");
			httpParameters.setProxyHost(proxyIPnPort[0]);
			httpParameters.setProxyPort(Integer.parseInt(proxyIPnPort[1]));
		}
	}

	public static HashMap<String, String> getNextBillingDateOfServices(WebServiceContext task)
	{
		if (task.containsKey(param_serviceNextBillingDateMap))
		{
			@SuppressWarnings("unchecked")
			HashMap<String, String> serviceNextBillingDateMap = (HashMap<String, String>) task.get(param_serviceNextBillingDateMap);
			return serviceNextBillingDateMap;
		}

		String subscriberID = task.getString(param_subscriberID);
		HashMap<String, String> serviceNextBillingDateMap = getNextBillingDateOfServices(subscriberID);

		task.put(param_serviceNextBillingDateMap, serviceNextBillingDateMap);
		logger.info("RBT:: serviceNextBillingDateMap: " + serviceNextBillingDateMap);
		return serviceNextBillingDateMap;
	}

	public static HashMap<String, String> getNextBillingDateOfServices(String subscriberID) {
		HashMap<String, String> serviceNextBillingDateMap = new HashMap<String, String>();

		try
		{
			logger.info("Get next billing date for subscriber: " + subscriberID);
			Parameters listSubscriptionsParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "SUBMGR_URL_FOR_LIST_SUBSCRIPTIONS", null);
			if (listSubscriptionsParam != null)
			{
				String url = listSubscriptionsParam.getValue().trim();
				url = url.replaceAll("%SUBSCRIBER_ID%", subscriberID);

				HttpParameters httpParameters = new HttpParameters(url);
				setSubMgrProxy(httpParameters);
				logger.info("RBT:: httpParameters: " + httpParameters);

				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
				logger.info("RBT:: httpResponse: " + httpResponse);

				if (httpResponse != null && httpResponse.getResponse() != null)
				{
					/* (non-Javadoc)
					 * API: ListSubscriptions 
					 * Response Format:
					 *	<?xml version="1.0" encoding="UTF-8"?>
					 *	<Personnel>
					 *		<ROOT>
					 *			<SERVICE>
					 *				<SVCID>KEYWORD</SVCID>
					 *				<SVCDESC>DESC</SVCDESC>
					 *				<STATUS>STATUS</STATUS>
					 *				<LASTTRANSACTIONTYPE>RENEWAL</LASTTRANSACTIONTYPE>
					 *				<REFID>REFID</REFID>
					 *				<NEXTCHARGEDATE>yyyy-MM-dd HH:mm:ss</NEXTCHARGEDATE>
					 *	`			<LASTCHARGEAMOUNT>5</LASTCHARGEAMOUNT>
					 *				<CHARGEDETAILS>M=100,BucketId=20,BucketId=30</CHARGEDETAILS>
					 *			</SERVICE>
					 *			.
					 *			.
					 *			<SERVICE>
					 *				<SVCID>KEYWORD</SVCID>
					 *				<SVCDESC>DESC</SVCDESC>
					 *				<STATUS>STATUS</STATUS>
					 *				<LASTTRANSACTIONTYPE>RENEWAL</LASTTRANSACTIONTYPE>
					 *				<REFID>REFID</REFID>
					 *				<NEXTCHARGEDATE>yyyy-MM-dd HH:mm:ss</NEXTCHARGEDATE>
					 *				<LASTCHARGEAMOUNT>5</LASTCHARGEAMOUNT>
					 *				<CHARGEDETAILS>M=100,BucketId=20,BucketId=30</CHARGEDETAILS>
					 *			</SERVICE>
					 *		</ROOT>
					 *	</Personnel>
					 */

					Document document = XMLUtils.getDocumentFromString(httpResponse.getResponse());

					SimpleDateFormat rbtDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					SimpleDateFormat subMgrDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					NodeList serviceNodeList = document.getElementsByTagName("SERVICE");
					for (int i = 0; i < serviceNodeList.getLength(); i++)
					{
						Element serviceElement = (Element) serviceNodeList.item(i); 

						String refID = null;
						Element refIDElem = (Element) serviceElement.getElementsByTagName("REFID").item(0);
						if (refIDElem != null && refIDElem.getChildNodes().getLength() > 0)
						{
							Text nextChargeDateText = (Text) refIDElem.getFirstChild();
							refID = nextChargeDateText.getNodeValue();
						}
						
						
						Element srvIDElem = (Element) serviceElement.getElementsByTagName("SVCID").item(0);
						String srvId = "";
						if (srvIDElem != null && srvIDElem.getChildNodes().getLength() > 0)
						{
							Text srvIdTextElem = (Text) srvIDElem.getFirstChild();
							srvId = srvIdTextElem.getNodeValue().trim().toUpperCase();
						}
						
						String nextBillingDate = null;
						Element nextChargeDateElem = (Element) serviceElement.getElementsByTagName("NEXTCHARGEDATE").item(0);
						if (nextChargeDateElem != null)
						{
							Text nextChargeDateText = (Text) nextChargeDateElem.getFirstChild();
							nextBillingDate = nextChargeDateText.getNodeValue();
						}

						if (nextBillingDate != null)
						{
							if (refID != null)
								refID = refID.trim();

							nextBillingDate = rbtDateFormat.format(subMgrDateFormat.parse(nextBillingDate));

							serviceNextBillingDateMap.put(refID, nextBillingDate);
							
							if(srvId.startsWith("RBT_ACT")){
								serviceNextBillingDateMap.put(subscriberID, nextBillingDate);
							}

						}
						
						String chargeDetails = null;
						Element chargeDetailsElem = (Element) serviceElement.getElementsByTagName("CHARGEDETAILS").item(0);
						if (chargeDetailsElem != null)
						{
							Text chargeDetailsText = (Text) chargeDetailsElem.getFirstChild();
							if (chargeDetailsText != null)
								chargeDetails = chargeDetailsText.getNodeValue();
						}

						if (chargeDetails != null)
						{
							if (refID != null)
								refID = refID.trim();

							serviceNextBillingDateMap.put(refID + "_chargeDetails", chargeDetails);

							if(srvId.startsWith("RBT_ACT")){
								serviceNextBillingDateMap.put(subscriberID + "_chargeDetails", chargeDetails);
							}
						}

						String lastAmountCharged = null;
						Element lastChargeAmountElem = (Element) serviceElement.getElementsByTagName("LASTCHARGEAMOUNT").item(0);
						if (lastChargeAmountElem != null)
						{
							Text lastChargeAmountText = (Text) lastChargeAmountElem.getFirstChild();
							if (lastChargeAmountText != null)
								lastAmountCharged = lastChargeAmountText.getNodeValue();
						}
						if (lastAmountCharged != null)
						{
							if (refID != null)
								refID = refID.trim();

							serviceNextBillingDateMap.put(refID + "_lastAmountCharged", lastAmountCharged);
							
							if(srvId.startsWith("RBT_ACT")){
								serviceNextBillingDateMap.put(subscriberID + "_lastAmountCharged", lastAmountCharged);
							}
						}
						
						String lastTransactionType=null;
						Element lastTransactionTypeElem = (Element) serviceElement.getElementsByTagName("LASTTRANSACTIONTYPE").item(0);
						if (lastTransactionTypeElem != null)
						{
							Text lastTransactionTypeText = (Text) lastTransactionTypeElem.getFirstChild();
							if (lastTransactionTypeText != null)
								lastTransactionType = lastTransactionTypeText.getNodeValue();
						}
						if (lastTransactionType != null)
						{
							if (refID != null)
								refID = refID.trim();

							serviceNextBillingDateMap.put(refID + "_lastTransactionType", lastTransactionType);
							
							if(srvId.startsWith("RBT_ACT")){
								serviceNextBillingDateMap.put(subscriberID + "_lastTransactionType", lastTransactionType);
							}
						}
						
						// Encrypt LASTCHARGEDATE from PRISM response.
						String lastChargeDate = null;
						Element lastChargeDateElem = (Element) serviceElement.getElementsByTagName("LASTCHARGEDDATE").item(0);
						if (lastChargeDateElem != null)
						{
							Text lastChargeDateText = (Text) lastChargeDateElem.getFirstChild();
							if(lastChargeDateText!=null){
							     lastChargeDate = lastChargeDateText.getNodeValue();
							}
						}

						if (lastChargeDate != null)
						{
							if (refID != null)
								refID = refID.trim();

							lastChargeDate = rbtDateFormat.format(subMgrDateFormat.parse(lastChargeDate));

							serviceNextBillingDateMap.put(refID + "_lastChargingDate", lastChargeDate);
							
							if(srvId.startsWith("RBT_ACT")){
								serviceNextBillingDateMap.put(subscriberID + "_lastChargingDate", lastChargeDate);
							}

						}
						
						// Encrypt LASTCHARGEDATE from PRISM response.
						String subStatus = null;
						Element subStatusElem = (Element) serviceElement.getElementsByTagName("STATUS").item(0);
						if (subStatusElem != null)
						{
							Text lastChargeDateText = (Text) subStatusElem.getFirstChild();
							subStatus = lastChargeDateText.getNodeValue();
						}

						if (subStatus != null)
						{
							if (refID != null)
								refID = refID.trim();							

							serviceNextBillingDateMap.put(refID + "_substatus", subStatus);
							
							if(srvId.startsWith("RBT_ACT")){
								serviceNextBillingDateMap.put(subscriberID + "_substatus", subStatus);
							}

						}
					}
				}
			}
		}
		catch (Exception e)
		{ 		
			logger.error("getNextBillingDateOfServices");
            logger.info(e.getMessage(), e); 
			e.printStackTrace();
		}
		logger.info("Returning serviceNextBillingDateMap: "
				+ serviceNextBillingDateMap + ", subscriberID: " + subscriberID);
		return serviceNextBillingDateMap;
	}
	
	
	public static String upgradeSubscriptionValidity(WebServiceContext webServiceContext, Subscriber subscriber)
	{
		String response = ERROR;
		List<String> allowedStatusForUpgradationList = Arrays.asList(CacheManagerUtil
				.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
						"STATUS_ALLOWED_FOR_UPGRADE_VALIDITY", "B").split(","));
		if (allowedStatusForUpgradationList.contains(subscriber.subYes()))
		{
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "VALIDITY_EXTENSION_NOT_SUPPORTED");
			boolean isUpdationSupported = true;
			if (parameter != null && parameter.getValue() != null)
			{
				String oldClassType = subscriber.subscriptionClass();
				
				String[] nonSupportedClassTypes = parameter.getValue().split(",");
				for (String classType : nonSupportedClassTypes)
				{
					if (oldClassType.equalsIgnoreCase(classType))
					{
						isUpdationSupported = false;
						break;
					}
				}
			}
			if (isUpdationSupported)
				response = Utility.sendRenewalRequestToSubMgr(webServiceContext);
		}
		else
		{
			response = getSubscriberStatus(subscriber);
			if (response.equalsIgnoreCase(ACTIVE))
			{
				// Pack upgradation pending(STATE_CHANGE) and voluntarily suspended users are considered as active users.
				// But not allowed to for subscription upgradation, that's why returning response as ACT_PENDING.
				response = ACT_PENDING;
			}
		}

		return response;
	}

	public static String sendRenewalRequestToSubMgr(WebServiceContext task)
	{
		String response = ERROR;
		try
		{
			String subscriberID = task.getString(param_subscriberID);
			String preCharged = task.containsKey(param_preCharged) ? task.getString(param_preCharged) : NO;
			
			Subscriber subscriber = (Subscriber) task.get(param_subscriber);
			String newSrvKey = "RBT_ACT_" + task.getString(param_subscriptionClass);
			//get the current subscription class
			String currentSrvKey = "RBT_ACT_" + ((subscriber != null) ? subscriber.subscriptionClass() : null);

			String refID = task.getString(param_refID);
			if (refID == null)
				refID = UUID.randomUUID().toString();
			else
			{
				SubscriberDownloads download = (SubscriberDownloads) task.get(param_subscriberDownloads);
				if(download != null) {
					currentSrvKey = "RBT_SEL_" + download.classType();
					newSrvKey = "RBT_SEL_" + download.classType();
				}
				else {
					SubscriberStatus subscriberStatus = (SubscriberStatus) task.get(param_subscriberStatus);
					currentSrvKey = "RBT_SEL_" + subscriberStatus.classType();
					newSrvKey = "RBT_SEL_" + task.getString(param_chargeClass);
				}
			}

			Parameters renewalTriggerUrlParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "RENEWAL_TRIGGER_URL", null);

			String url = renewalTriggerUrlParam.getValue().trim();
			url = url.replaceAll("%SUBSCRIBER_ID%", subscriberID);
			url = url.replaceAll("%SERVICE_KEY%", currentSrvKey);
			//Not sure if SM uses this parameter
			url = url.replaceAll("%PRE_CHARGED%", preCharged);
			url = url.replaceAll("%TRIGGER_KEY%", newSrvKey);
			url = url.replaceAll("%REQ_REFID%", refID);

			logger.info("RBT:: SM URL: " + url);
			HttpParameters httpParameters = new HttpParameters(url);
			logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("RBT:: httpResponse: " + httpResponse);

			String[] status = httpResponse.getResponse().trim().split("\\|");
			response = status[0];
			logRenewal(task,subscriber);
		}
		catch (Exception e)
		{
			logger.error("sendRenewalRequestToSubMgr", e);
		}

		return response;
	}
	static void logRenewal(WebServiceContext task, Subscriber subscriber)
	{
		if (subscriber == null)
			return;

		String MSISDN = task.getString(param_subscriberID);
		String circleId = subscriber.circleID();
		String pack = task.getString(param_subscriptionClass);
		String mode = task.getString(param_mode);
		String modeInfo = task.getString(param_modeInfo);
		if(pack == null){
			pack = "DEFAULT";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		logger.info("Logging with MSISDN=" + MSISDN + ", circleId=" + circleId
				+ ", pack=" + pack + "mode=" + mode + "modeinfo= " + modeInfo);
		renewalLogger.info(sdf.format(new Date())+","+MSISDN + "," + circleId + "," + pack + "," + mode + "," + modeInfo);
	}
	

	/**
	 * 
	 * @param task
	 * @return returns p if the user is prepaid or b if postpaid
	 */
	public static String getSubscriberType(WebServiceContext task)
	{
		return DataUtils.isUserPrepaid(task) ? "p" : "b";
	}

	public static void copyFile(File source, File destination) throws IOException
	{
		FileChannel sourceFileChannel = null;
		FileChannel destinationFileChannel = null;
		try
		{
			sourceFileChannel = (new FileInputStream(source)).getChannel();
			destinationFileChannel = (new FileOutputStream(destination)).getChannel();
			sourceFileChannel.transferTo(0, source.length(), destinationFileChannel);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (sourceFileChannel != null)
					sourceFileChannel.close();
				if (destinationFileChannel != null)
					destinationFileChannel.close();
			}
			catch (IOException e)
			{
			}
		}
	}
	
	public static void sendErrorSMSForBulkRequestFailure(WebServiceContext task, String subscriberID, String webserviceResponse, RBTProcessor processor){
		if(!task.containsKey(param_sendsms) || !task.getString(param_sendsms).equalsIgnoreCase("TRUE")) {
			return;
		}
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		String failureResponse = getSelectionFailureResponseFor3rdParties(webserviceResponse, task);
		String mode = null;
		if(task.containsKey(param_mode)){
			mode = task.getString(param_mode).trim();
		}
		String reqType = task.getString(param_taskType);
		//assuming mode is always passed
		String type = (mode + "_" + reqType).toUpperCase();
		String lang = null;
		if(null != subscriber){
			lang = subscriber.language();
		}
		logger.info("Get the sms text for type:" + type
				+ " resp:" + failureResponse.toUpperCase() + " lang:" + lang);
		String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(type, failureResponse.toUpperCase(), lang);
		
		if (isStringEmpty(smsText)) {
			logger.info("Getting the FAILURE SMS");
			smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(type, "FAILURE", lang);
			if (isStringEmpty(smsText)) {
				return;
			}							
		}
		
		String songName = "";
		if(task.containsKey(session_clip)){
			Clip clip = (Clip) task.get(session_clip);
			if(clip != null){
				songName = clip.getClipName();
			}
		}
		smsText = smsText.replaceAll("%SONG_NAME", songName);
		String senderId = null;
		if (task.containsKey(Constants.SENDER_ID)){
			senderId = task.getString(Constants.SENDER_ID).trim();
		}
		else{
			Parameters sender = CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "SENDER_NUMBER");
			senderId = sender.getValue();
		}
		
		logger.info("Get the sms text for type:" + type
				+ " resp:" + failureResponse.toUpperCase() + " SmsText : " + smsText + " senderId : " + senderId + " subscriberID : " + subscriberID);

		task.put(param_senderID,senderId);
		task.put(param_receiverID,subscriberID);
		task.put(param_smsText, smsText);
		String status = processor.sendSMS(task);
		logger.info("send sms " + status);
	}
	
	private static String getSelectionFailureResponseFor3rdParties(String response, WebServiceContext task)
	{
		String finalResp = Constants.Resp_Success;
		if(response.equals(WebServiceConstants.CLIP_NOT_EXISTS))
			finalResp = Constants.Resp_ClipNotAvailable;
		else if(response.equals(WebServiceConstants.CLIP_EXPIRED))
			finalResp = Constants.Resp_ClipExpired;
		else if(response.equals(WebServiceConstants.ALREADY_EXISTS))
			finalResp = Constants.Resp_songSetSelectionAlreadyExists;
		else if(response.equals(WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS))
			finalResp = Constants.Resp_songSetDownloadAlreadyExists;
		else if(response.equals(WebServiceConstants.TECHNICAL_DIFFICULTIES))
			finalResp = Constants.Resp_Err;
		else if(response.equals(WebServiceConstants.ERROR))
			finalResp = Constants.Resp_Err;
		else if(response.equals(WebServiceConstants.SELECTION_OVERLIMIT) ||
				 response.equals(WebServiceConstants.LOOP_SELECTION_OVERLIMIT))
			finalResp = Constants.Resp_selectionLimit;
		else if(response.equals(WebServiceConstants.PERSONAL_SELECTION_OVERLIMIT))
			finalResp = Constants.Resp_maxCallerSel;
		else if(response.equals(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED))
			finalResp = Constants.Resp_liteUserPremiumBlocked;
		else if(response.equals(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED))
			finalResp = Constants.Resp_liteUserPremiumNotProcessed;
		else if(response.equalsIgnoreCase(WebServiceConstants.ACTIVE))
			finalResp = Constants.Resp_SuspendedNo;
		else if(response.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
			finalResp = Constants.Resp_SuspendedNo;
		else if (response.equalsIgnoreCase(WebServiceConstants.ACT_PENDING))
			finalResp = Constants.Resp_ActPending;
		else if (response.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING))
			finalResp = Constants.Resp_DeactPending;
		else if (response.equalsIgnoreCase(WebServiceConstants.GIFTING_PENDING))
			finalResp = Constants.Resp_giftActPending;
		else if (response.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING))
			finalResp = Constants.Resp_RenewalPending;
		else if (response.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED))
			finalResp = Constants.Resp_BlackListedNo;
		else if (response.equalsIgnoreCase(WebServiceConstants.INVALID_PREFIX))
			finalResp = Constants.Resp_InvalidPrefix;
		else if (response.equalsIgnoreCase(WebServiceConstants.COPY_PENDING))
			finalResp = Constants.Resp_CopyPending;
		//RBT-12419
		else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_EXPIRED_DOWNLOAD_DELETED))
			finalResp = Constants.Resp_ClipExpiredDwnDeleted;
		else if (response.equalsIgnoreCase(WebServiceConstants.CATEGORY_EXPIRED_DOWNLOAD_DELETED))
			finalResp = Constants.Resp_CatExpiredDwnDeleted;
		else if(response.equalsIgnoreCase(WebServiceConstants.CATEGORY_EXPIRED))
			finalResp = Constants.Resp_CatExpired;
		
		else
			finalResp = Constants.Resp_Failure;
		return finalResp;
	}

	public static boolean isStringEmpty(String s) {
		if(s == null || s.trim().length() <=0 ) {
			return true;
		}
		return false;
	}
	public static boolean isStringNotEmpty(String s) {
		return !isStringEmpty(s);
	}

	public static String sendChangeMsisdnRequestToSubMgr(WebServiceContext task)
	{
		String response = ERROR;
		try
		{
			String subscriberID = task.getString(param_subscriberID);
			String newSubscriberID = task.getString(param_newSubscriberID);

			Subscriber subscriber = (Subscriber)task.get(param_subscriber);
			String refID = subscriber.refID();
			String mode = subscriber.activatedBy();

			Parameters changeMsisdnUrlParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "CHANGE_MSISDN_URL", null);
			if (changeMsisdnUrlParam == null || changeMsisdnUrlParam.getValue() == null)
			{
				logger.info("RBT:: ChangeMSISDN SM URL not configured");
				return ERROR;
			}

			String url = changeMsisdnUrlParam.getValue().trim();
			url = url.replaceAll("%msisdn%", subscriberID.trim());
			url = url.replaceAll("%newmsisdn%", newSubscriberID.trim());
			
			if (refID != null)
				url = url.replaceAll("%refid%", refID);
			else
				url = url.replaceAll("%refid%", "");
			
			url = url.replaceAll("%mode%", mode);

			logger.info("RBT:: SM URL: " + url);
			HttpParameters httpParameters = new HttpParameters(url);
			logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("RBT:: httpResponse: " + httpResponse);

			response = httpResponse.getResponse().trim();
			response = response.replaceAll("\\|", ":");
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return response;
	}
	
	public static boolean isSelectionOverlap(Clip clip, Category category, Subscriber subscriber)
	{
		if(clip == null || category == null || subscriber == null)
			return false;
		String subscriberID = subscriber.subID();
		List<ProvisioningRequests> provList = ProvisioningRequestsDao.getBySubscriberId(subscriberID);
		logger.info("RBT:: List<ProvisioningRequests>: " + provList);
		if(provList == null || provList.size() == 0)
			return false;
		for(ProvisioningRequests thisRequest : provList)
		{
			String extraInfo = thisRequest.getExtraInfo();
			String clipId = null;
			String categoryId = null;
			boolean isShuffle = false;
			HashMap<String, String> attrMap = DBUtility.getAttributeMapFromXML(extraInfo);
			if(attrMap == null || attrMap.size() == 0)
				continue;
			
			categoryId = attrMap.get(ExtraInfoKey.CATEGORY_ID.toString());
			clipId = attrMap.get(ExtraInfoKey.CLIP_ID.toString());
			if(categoryId != null)
			{
				Category thisCategory = rbtCacheManager.getCategory(Integer.parseInt(categoryId));
				isShuffle = isShuffleCategory(thisCategory.getCategoryTpe());
				if(isShuffle && categoryId.equals(category.getCategoryId()))
					return true;
			}
			if(!isShuffle && clipId != null)
			{
				if(clipId.equals(clip.getClipId()))
					return true;
			}	
		}
		return false;
	}

	public static boolean isNavCat(int categoryType) 
    {
    	return festivalAndBoxOfficeShuffles.contains(String.valueOf(categoryType));
	}
	
	private static String getParamAsString(String type, String param, String defaultVal)
    {
    	try{
    		return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue();
    	}catch(Exception e){
    		logger.warn("Unable to get param ->"+param +"  type ->"+type);
    		return defaultVal;
    	}
    }
	
	private static String getFinalSms(String smsText, Map<String,String> map) {
    	
    	smsText = smsText.replaceAll("%SONG", map.get("SONG") != null ? map.get("SONG") : "");
    	smsText = smsText.replaceAll("%PROMOCODE", map.get("PROMOCODE") != null ? map.get("PROMOCODE") : "");
    	
    	return smsText;
    }
	
	public static String getSmsTextForDeactivationSelection(String language, List<String> songWavFilesList) {
		String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(com.onmobile.apps.ringbacktones.provisioning.common.Constants.SEND_OLD_SELECTION_IN_LIBRARY, language);
		
		if(smsText == null || songWavFilesList == null || songWavFilesList.size() == 0) 
			return null;
		
		
		List<String> songNameList = new ArrayList<String>();
		List<String> promoIdList = new ArrayList<String>();
		for(String wavFileName : songWavFilesList) {
			Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFileName, language) ;
			if(clip != null) {
				songNameList.add(clip.getClipName());
				promoIdList.add(clip.getClipPromoId());
			}
		}
		
		
		String songNames = "";
		String clipPromoIds = "";
		for(String clipName : songNameList) {
			songNames += clipName + ",";
		}
		if(songNames.length() > 1)
			songNames = songNames.substring(0, songNames.length() - 1);
		
		for(String promoId : promoIdList) {
			clipPromoIds += promoId + ",";
		}
		if(clipPromoIds.length() > 1)
			clipPromoIds = clipPromoIds.substring(0, clipPromoIds.length() - 1);
		
		Map<String,String> map = new HashMap<String, String>();
		map.put("SONG", songNames);
		map.put("PROMOCODE", clipPromoIds);
		
		smsText = getFinalSms(smsText, map); 
		logger.info("SmsText: " + smsText);
		return smsText;
	}
	
	public static String getSubscriberPackStatus(ProvisioningRequests provReq)
	{
		return getSubscriberPackStatus(provReq, false);
	}
	
	public static String getSubscriberPackStatus(ProvisioningRequests provReq, boolean considerErrorState)
	{
		String status = ERROR;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (provReq == null)
			status = PACK_NEW_USER;
		else if (rbtDBManager.isSubscriberPackActivationPending(provReq))
		{
			if (considerErrorState && provReq.getStatus() == iRBTConstant.PACK_ACTIVATION_ERROR)
				status = ACT_ERROR;
			else
				status = ACT_PENDING;
		}
		else if (rbtDBManager.isSubscriberPackActivated(provReq))
		{
				status = ACTIVE;
		}
		else if (rbtDBManager.isSubscriberPackRenewalPending(provReq))
			status = RENEWAL_PENDING;
		else if (rbtDBManager.isSubscriberPackDeactivationPending(provReq))
		{
			if (considerErrorState && provReq.getStatus() == iRBTConstant.PACK_DEACTIVATION_ERROR)
				status = DEACT_ERROR;
			else
				status = DEACT_PENDING;
		}
		else if (rbtDBManager.isSubscriberPackDeactivated(provReq))
			status = DEACTIVE;
		else if (rbtDBManager.isSubscriberPackInGrace(provReq))
			status = GRACE;
		else if (rbtDBManager.isSubscriberPackSuspended(provReq))
		{
				status = SUSPENDED;
		}

		return status;
	}

	/**
	 * @param webServiceContext
	 * @param subscriber
	 * @param category
	 * @param clip
	 * @return
	 * @throws RBTException 
	 */
	public static String isPreviousSelPending(
			WebServiceContext webServiceContext, String subscriberID,
			Category category, Clip clip) throws RBTException {
		String blockedClassesStr = RBTParametersUtils.getParamAsString(
				"COMMON",
				"CHARGE_CLASSES_TO_BE_BLOCKED_IF_PREV_SEL_IS_PENDING", "");
		if (blockedClassesStr == null || blockedClassesStr.length() == 0) {
			return null;
		}

		Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
		ChargeClass nextChargeClass = DataUtils
				.getNextChargeClassForSubscriber(webServiceContext, subscriber,
						category, clip);
		String nextClass = nextChargeClass.getChargeClass();
		List<String> blockedChargeClasses = Arrays.asList(blockedClassesStr
				.split(","));
		if (blockedChargeClasses.contains(nextClass)) {
			if (RBTParametersUtils.getParamAsBoolean("COMMON",
					"ADD_TO_DOWNLOADS", "FALSE")) {
				SubscriberDownloads[] subDownloads = RBTDBManager.getInstance()
						.getSubscriberDownloads(subscriber.subID());
				if (subDownloads != null && subDownloads.length > 0) {
					SubscriberDownloads latestDownload = subDownloads[subDownloads.length - 1];
					if (logger.isDebugEnabled()) {
						logger.debug("Latest download object : "
								+ latestDownload);
					}

					if (RBTDBManager.getInstance().isDownloadActivationPending(latestDownload)
							|| RBTDBManager.getInstance().isDownloadGrace(latestDownload)) {
						return PREVIOUS_DOWNLOAD_PENDING;
					}
				}
			} else {
				SubscriberStatus[] settings = RBTDBManager.getInstance()
						.getSubscriberRecords(subscriber.subID());
				if (settings != null && settings.length > 0) {
					SubscriberStatus latestSetting = settings[settings.length - 1];
					if (logger.isDebugEnabled())
						logger.debug("Latest setting object : " + latestSetting);

					if (RBTDBManager.getInstance().isSelectionActivationPending(latestSetting)
							|| RBTDBManager.getInstance().isSelectionGrace(latestSetting)) {
						return PREVIOUS_SELECTION_PENDING;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param webServiceContext
	 * @param subscriber
	 * @param category
	 * @param clip
	 * @return
	 * @throws RBTException 
	 */
	public static String isPreviousSelPendingWithSameChargeClass(
			WebServiceContext webServiceContext, String subscriberID,
			Category category, Clip clip) throws RBTException {
		String blockedClassesStr = RBTParametersUtils.getParamAsString(
				"COMMON",
				"SAME_CHARGE_CLASSES_TO_BE_BLOCKED_IF_PREV_SEL_PENDING", "");
		if (blockedClassesStr == null || blockedClassesStr.length() == 0) {
			return null;
		}

		Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
		ChargeClass nextChargeClass = DataUtils
				.getNextChargeClassForSubscriber(webServiceContext, subscriber,
						category, clip);
		String nextClass = nextChargeClass.getChargeClass();
		List<String> blockedChargeClasses = Arrays.asList(blockedClassesStr
				.split(","));
		if (blockedChargeClasses.contains(nextClass)) {
			if (RBTParametersUtils.getParamAsBoolean("COMMON",
					"ADD_TO_DOWNLOADS", "FALSE")) {
				SubscriberDownloads[] subDownloads = RBTDBManager.getInstance()
						.getSubscriberDownloads(subscriber.subID());
				if (subDownloads != null && subDownloads.length > 0) {
					for (SubscriberDownloads subscriberDownload : subDownloads) {

						if (subscriberDownload.classType().equalsIgnoreCase(nextClass)
								&& (RBTDBManager.getInstance().isDownloadActivationPending(subscriberDownload)
								|| RBTDBManager.getInstance().isDownloadGrace(subscriberDownload))) {
							return PREVIOUS_DOWNLOAD_PENDING;
						}
						
					}

				}
			} else {
				SubscriberStatus[] settings = RBTDBManager.getInstance()
						.getSubscriberRecords(subscriber.subID());
				if (settings != null && settings.length > 0) {
					for (SubscriberStatus setting : settings) {

						if (setting.classType().equalsIgnoreCase(nextClass) &&
								(RBTDBManager.getInstance().isSelectionActivationPending(setting)
								|| RBTDBManager.getInstance().isSelectionGrace(setting))) {
							return PREVIOUS_SELECTION_PENDING;
						}
					}

				}
			}
		}
		return null;
	}
	
	public static boolean isValidModeIPConfigured(String mode, String ipAddress) {
		boolean isValid = false;
		if(modeIpAddressMapping == null) {
			synchronized (object) {
				if(modeIpAddressMapping == null) {
					Parameters modeIPParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.CONSENT, "MODE_IP_MAPPING_FOR_CONSENT", null);
					modeIpAddressMapping = new HashMap<String, List<String>>();
					if(modeIPParam != null) {
						String[] modeIpMapStrArr = modeIPParam.getValue().split("\\|");
						for(String temp : modeIpMapStrArr) {
							String[] modeAndIpStrArr = temp.split(":");
							if(modeAndIpStrArr == null || modeAndIpStrArr.length != 2) {
								continue;
							}
							List<String> ipList = Arrays.asList(modeAndIpStrArr[0].split(","));
							String[] modes = modeAndIpStrArr[1].split(",");
							for(String tempMode : modes) {
								List<String> tempList = modeIpAddressMapping.get(tempMode);
								if(tempList == null) {
									tempList = new ArrayList<String>();
								}
								tempList.addAll(ipList);
								modeIpAddressMapping.put(tempMode, tempList);
							}
						}
					}
				}
			}			
		}
		
		if(modeIpAddressMapping.size() == 0 || ipAddress == null) {
			logger.info("RBT:: IP: " + ipAddress + " mode: " + mode + " isValidIP&Mode: " + true);
			return true;			
		}
		if(mode == null) {
			mode = "VP";
		}
		List<String> ipList = modeIpAddressMapping.get(mode);
		if(ipList != null && ipList.contains(ipAddress)) {
			isValid = true;
		}
		logger.info("RBT:: IP: " + ipAddress + " mode: " + mode + " isValidIP&Mode: " + isValid);
		return isValid;
	}
	
	public static boolean isModeConfiguredForIdeaConsent(String mode) {
		if (consentConfModes.size() == 0) {
			Parameters responseEncoderParam = CacheManagerUtil
					.getParametersCacheManager().getParameter(
							iRBTConstant.PROVISIONING,
							"CONFIGURED_MODES_FOR_CONSENT_REQUEST", null);
			if (responseEncoderParam != null) {
				String confMode = responseEncoderParam.getValue();
				logger.info("CONFIGURED_MODES_FOR_CONSENT_REQUEST == "
						+ confMode);
				String str[] = confMode.split(",");
				for (String modeStr : str)
					consentConfModes.add(modeStr);
			}
		}
		boolean response = consentConfModes.contains(mode);
		return response;
	}
	
	//RBT-9999 Added for checking valid month for month based interval selection 
	public static boolean isMonthBasedInterval(String selInterval) {
		try {
		if (selInterval != null) {
			String[] intervals = selInterval.split(",");
			int month = 0;
			if (intervals != null && intervals.length == 2) {
				if (intervals[0].startsWith("M")
						&& intervals[1].startsWith("M")
						&& (intervals[0].length() == 4 || intervals[0].length() == 5)
						&& (intervals[1].length() == 4 || intervals[1].length() == 5)) {
					for (int i = 0; i < 2; i++) {
						if (intervals[i].length() == 5) {
							month = Integer.parseInt(intervals[i].substring(1,
									3));
						} else if (intervals[i].length() == 4) {
							month = Integer.parseInt(intervals[i].substring(1,
									2));
						}
						if (month < 0 || month > 12)
							return false;
					}
					logger.info("Returning true for: " + selInterval);
					return true;
				}
			}
		}
		}catch(Exception e) {
			logger.info("Exception occured:"+e);
		}
		logger.info("Returning false for selInterval: " + selInterval);
		return false;
	}
	
	public static String getYearBasedInterval(String selInterval) {
		String[] intervals = selInterval.split(",");
		StringBuffer interval = new StringBuffer();
		if (selInterval.startsWith("Y")) {
			// Special Date
			List<Date> dateList = new ArrayList<Date>();
			SimpleDateFormat dateFormat = null;
			for (String intervalToken : intervals) {
				if ((intervalToken.length() != 5 && intervalToken.length() != 9)
						|| !intervalToken.startsWith("Y")){
					logger.warn("Invalid interval passed: with intervalToken so interval is passed as null" + intervalToken);					
					return null;
				}

				try {
					if (intervalToken.length() == 9) {
						dateFormat = new SimpleDateFormat("ddMMyyyy");
						Date parseDate = dateFormat.parse(intervalToken
								.substring(1));
						dateList.add(parseDate);
					} else {
						dateFormat = new SimpleDateFormat("ddMM");

						// For supporting recursive selections on Feb29th date
						if (!intervalToken.equals("Y2902")) {
							dateFormat.setLenient(false);
							Date parseDate = dateFormat.parse(intervalToken.substring(1));
							dateList.add(parseDate);
						}
					}
					
				} catch (ParseException e) {
					logger.warn("Invalid interval passed: " + e.getMessage());					
					return null;
				}
			}
			Collections.sort(dateList);
			for (int i = 0; i < dateList.size(); i++) {
				interval.append("Y" + dateFormat.format(dateList.get(i)));
				if(dateList.size() > i+1) interval.append(",");
			}
		} else {
			logger.warn("Invalid interval passed: as it doesn't start with Y :" + selInterval);
			return null;
		}
		
		logger.info("year interval :" + interval.toString());
		return interval.length() > 0 ? interval.toString() : null;
	}
	
	//RBT-9999 Added for checking valid date for month based interval selection  
	public static boolean isMonthBasedDateValid(String intervals) {
		boolean flag = true;
		try {
			String[] interval = intervals.split(",");
			for (String intervalToken : interval) {
				int dayOfTheMonth = Integer.parseInt(intervalToken
						.substring(intervalToken.length() - 2));
				if (dayOfTheMonth < 1 || dayOfTheMonth > 31) {
					flag = false;
					break;
				}
			}

		} catch (Exception e) {
			logger.info("Exception occured while checking date in isMonthBasedDateValid:"
					+ e);
			return false;
		}
		logger.info("isMonthBasedDateValid is returning: " + flag);
		return flag;
	}
	
	//RBT-9213 
	public static Map<String,String> getCodeToStringResponseMap(){
		//3007|BLACK_LISTED:BLACK_LISTED;3008|DEACT_PENDING:DEACT_PENDING
		if(returnCodeToStringMap.size()==0) {
			String returnCodeToStringConfig = RBTParametersUtils.getParamAsString(
					iRBTConstant.COMMON, "RETURN_CODE_TO_STRING_MAPPING", null);
			if (returnCodeToStringConfig != null) {
				 //changed for bug
				returnCodeToStringMap = MapUtils.convertToMap(returnCodeToStringConfig, ";", ":", "|");
			}
		}
		return returnCodeToStringMap;
	}
	
	//RBT-12835 - Loop Feature required in Song Pack- ZM
	public static boolean isCosTypeConfiguredForSelectionLoop(String cosType) {
		boolean response = false;
		if (cosType != null && musicPackCosTypesForSelectionsLoopList.contains(cosType)) {
			response = true;
		}
		logger.debug("cosType: " + cosType + ", isCosTypeConfiguredForSelectionLoop: " + response);
		return response;
	}

	//JIRA-ID: RBT-13626: This function will validate the User UDS type and send the 
	//Premium charge class from the map.for other cases based on UDS true or false
	//it will send the UDS charge class dummy value.
	public static String isUDSUser(HashMap<String, String> subExtraInfoMap,
			boolean donotValidatePremiumParam) {
		return isUDSUser(subExtraInfoMap, donotValidatePremiumParam, null);
	}
	
	public static String isUDSUser(HashMap<String, String> subExtraInfoMap,
			boolean donotValidatePremiumParam, Map<String, String> selectionInfoMap){
		String returnString = null;
		String UDSType = getUdsType(subExtraInfoMap, selectionInfoMap);
		if (UDSType == null) {
			return null;
		}
		if (!premiumChargeClassMap.isEmpty()) {
			UDSType = UDSType.toUpperCase();
			returnString = premiumChargeClassMap.get(UDSType);
		} else {
			if (subExtraInfoMap != null
					&& !subExtraInfoMap.get(iRBTConstant.UDS_OPTIN)
							.equalsIgnoreCase("true")) {
				return null;
			}
			// Here we are returning the null string because for other
			// operators there shouldn't be any validation happen
			// so we should consider that as true.
			returnString = donotValidatePremiumParam || premiumChargeClass == null ? "NULL" : premiumChargeClass;
		}
		return returnString;
	}

	/**
	 * @param subExtraInfoMap
	 * @param selectionInfoMap
	 * @return
	 */
	public static String getUdsType(HashMap<String, String> subExtraInfoMap,
			Map<String, String> selectionInfoMap) {
		if ((null == subExtraInfoMap || subExtraInfoMap.isEmpty() || !subExtraInfoMap
				.containsKey(iRBTConstant.UDS_OPTIN))
				&& (selectionInfoMap == null || !selectionInfoMap
						.containsKey(iRBTConstant.UDS_OPTIN))) {
			return null;
		}
		String UDSType = (selectionInfoMap != null ? selectionInfoMap
				.get(iRBTConstant.UDS_OPTIN) : null);
		if (UDSType == null || UDSType.isEmpty()) {
			UDSType = subExtraInfoMap.get(iRBTConstant.UDS_OPTIN);
		}
		return UDSType;
	}
	
	public static RBTLoginUser getRBTLoginUserBasedOnAppName(String subscriberId, String userId) {
		logger.info("subscriberId: " + subscriberId + ", userId: " + userId);
		RBTLoginUser user = null;
		
		//RBT-14624	Signal app - RBT tone play notification feature null check added
		if(subscriberId == null){
			return user;
		}
		
		if (configuredAppNamesForTPSupport == null || configuredAppNamesForTPSupport.isEmpty()) {
			logger.info("APP_NAMES_FOR_REAL_TP_SUPPORT is not configured or is empty. Returning null");
		} else {
			for (String appName : configuredAppNamesForTPSupport) {
				String type = getMobileClientTypeWithAppName(appName); 
				user = RBTDBManager.getInstance().getRBTLoginUser(userId, null, subscriberId, type, null, false);
				if (user != null) {
					logger.debug("User found with appName: " + appName);
					break;
				}
			}
		}
		logger.info("user: " + user);
		return user;
	}
	
	public static String getMobileClientTypeWithAppName(String appName) {
		return "MOBILECLIENT_" + appName.toUpperCase();
	}
	
	public static String getAppNameFromType(String type) {
		String appName = null;
		if (type != null && !type.isEmpty() && type.indexOf("MOBILECLIENT_") != -1) {
			appName = type.substring(13).toUpperCase();
		}
		logger.debug("appName: " + appName);
		return appName;
	}
	
	
	//RBT-14497 - Tone Status Check
	public static boolean isNoDownloadDeactSub(Subscriber subscriber) {
		boolean noDownloadDeactSub = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.DAEMON, "NO_DOWNLOAD_DEACT_SUB", "FALSE");
		if (!noDownloadDeactSub) {
			String songBaseCosId = getParamAsString(iRBTConstant.COMMON,
					"SONG_BASED_COS_ID", null);
			if (songBaseCosId != null) {
				logger.info("Song based cos id is enabled. subscriber: "
						+ subscriber + " songBaseCosId: " + songBaseCosId);
				List<String> cosIdList = Arrays.asList(songBaseCosId.split("\\,"));
				if (cosIdList.contains(subscriber.cosID())) {
					logger.debug("Cos is a song_based_cos_id for subscriber: " + subscriber + ". Returning true.");
					noDownloadDeactSub = true;
				}
			}
			String migratedUserSubscriptionClassString = RBTParametersUtils.getParamAsString(WEBSERVICE,
					"MIGRATED_USER_SUBSCRIPTION_CLASSES", null);
			if (!noDownloadDeactSub && migratedUserSubscriptionClassString != null) {
				logger.info("MIGRATED_USER_SUBSCRIPTION_CLASSES is enabled. subscriber: "
						+ subscriber + " migratedUserSubscriptionClassList: " + migratedUserSubscriptionClassString);
				List<String> migratedUserSubscriptionClassList = Arrays.asList(migratedUserSubscriptionClassString.split("\\,"));
				if (migratedUserSubscriptionClassList.contains(subscriber.subscriptionClass())) {
					logger.debug("Subscription class is a migrated user subscription class for subscriber: " + subscriber + ". Returning noDownloadDeactSub as true.");
					noDownloadDeactSub = true;
				}
			}
		}
		logger.debug("noDownloadDeactSub:" + noDownloadDeactSub);
		return noDownloadDeactSub;
	}

	public static Date getNextDate(String chargeperiod, Date lastChargingDate) {
		if (chargeperiod == null)
			chargeperiod = "M1";
		int type = 0;
		int number = 0;
		Calendar calendar1 = Calendar.getInstance();
		if(lastChargingDate != null) {
			calendar1.setTime(lastChargingDate);
		}
		if (chargeperiod.startsWith("D"))
			type = 0;
		else if (chargeperiod.startsWith("W"))
			type = 1;
		else if (chargeperiod.startsWith("M"))
			type = 2;
		else if (chargeperiod.startsWith("Y"))
			type = 3;
		else if (chargeperiod.startsWith("B"))
			type = 4;
		else if (chargeperiod.startsWith("O"))
			type = 5;

		logger.info("*** getNextDate::type " + type + " for " + chargeperiod);

		if (type != 4 && type != 5) {
			try {
				number = Integer.parseInt(chargeperiod.substring(1));
			} catch (Exception e) {
				type = 2;
				number = 1;
			}
		}

		switch (type) {
		case 0:
			calendar1.add(Calendar.DAY_OF_YEAR, number);
			break;
		case 1:
			calendar1.add(Calendar.WEEK_OF_YEAR, number);
			break;
		case 2:
			calendar1.add(Calendar.MONTH, number);
			break;
		case 3:
			calendar1.add(Calendar.YEAR, number);
			break;
		case 4:
			calendar1.add(Calendar.YEAR, 50);
			break;
		case 5:
			calendar1.add(Calendar.YEAR, 50);
			break;
		default:
			calendar1.add(Calendar.MONTH, 1);
			break;
		}

		calendar1.add(Calendar.DAY_OF_YEAR, -1);
		logger.info("*** getNextDate::type " + calendar1.getTime());
		return calendar1.getTime();
	}
	//Changes are done for handling the voldemort issues.
	public static List<String> getConfiguredAppNamesForTPSupport() {
		return configuredAppNamesForTPSupport;
	}

	/**
	 * Method converts Subscription Period to number of days.
	 * @param subscriptionPeriod
	 * @return
	 */
	public static String getSubscriptionPeriodInDays(String subscriptionPeriod) {
		if(subscriptionPeriod.contains("M")) {
			return (Integer.parseInt(subscriptionPeriod.substring(1))*30+"");
		} else if(subscriptionPeriod.contains("D")) {
			return (subscriptionPeriod.substring(1));
		}
		return subscriptionPeriod;
	}
	
	public static boolean isDNDEnabled(String msnidn){
		String umpDNDUrl = RBTParametersUtils.getParamAsString("VIRAL", "UMP_DND_URL_FOR_VIRAL_PROMOTION", null); 
		if (umpDNDUrl != null)
		{
			umpDNDUrl = umpDNDUrl.replaceFirst("%SUBSCRIBER_ID%", msnidn);
			StringBuffer response = new StringBuffer();
			boolean success = Tools.callURL(umpDNDUrl, new Integer(-1), response, false, null,
					-1, false, 2000);

			if (response.toString().trim().equalsIgnoreCase("TRUE") || !success)
			{
				// if UMP url returns 'TRUE' or error status code or if UMP server is down, number is considered as DND 
				logger.info("Not promoting as the number is DND in UMP");
				return true;
			}
		}
		return false; 
	}
	
	public static boolean isBlackOutPeriodNow()
	{
		Calendar calendar = Calendar.getInstance();
		List<Integer> blackout = blackoutTimesList.get(calendar
				.get(Calendar.DAY_OF_WEEK));

		if (logger.isDebugEnabled())
			logger.debug("BlackOut checked against " + blackout);

		return (blackout.contains(calendar.get(Calendar.HOUR_OF_DAY)));
	}

	private static void initializeBlackOut()
	{
		blackoutTimesList = new ArrayList<List<Integer>>();
		for (int i = 0; i <= 7; i++)
			blackoutTimesList.add(new ArrayList<Integer>());

		String blackoutTimes = RBTParametersUtils.getParamAsString("VIRAL",
				"BLACK_OUT_PERIOD", null);
		if (blackoutTimes == null)
		{
			logger.info("No BlackOut Configured");
			return;
		}

		String[] blackoutTokens = blackoutTimes.split(",");
		for (String blackout : blackoutTokens)
		{
			if (!blackout.contains("["))
			{
				logger.info("No BlackOut Time Configured" + blackout);
				continue;
			}

			List<Integer> days = getDays(blackout.substring(0,
					blackout.indexOf("[")));
			if (days != null && days.size() > 0)
			{
				List<Integer> times = getTimes(blackout.substring(blackout
						.indexOf("[")));
				for (int j = 0; j < days.size(); j++)
					blackoutTimesList.set(days.get(j).intValue(), times);
			}
		}

		logger.info("blackoutTimesList initialized " + blackoutTimesList);
	}
	
	private static List<Integer> getDays(String string)
	{
		List<Integer> daysList = new ArrayList<Integer>();

		Map<String, Integer> days = new HashMap<String, Integer>();
		days.put("SUN", 1);
		days.put("MON", 2);
		days.put("TUE", 3);
		days.put("WED", 4);
		days.put("THU", 5);
		days.put("FRI", 6);
		days.put("SAT", 7);

		if (string.contains("-"))
		{
			try
			{
				String day1 = string.substring(0, string.indexOf("-"));
				String day2 = string.substring(string.indexOf("-") + 1);
				if (!days.containsKey(day1) || !days.containsKey(day2))
				{
					logger.info("Invalid week specified !!!!" + string);
					return null;
				}

				int startDay = days.get(day1);
				int endDay = days.get(day2);

				if (endDay > startDay)
				{
					for (int t = startDay; t <= endDay; t++)
						daysList.add(t);
				}
				else
				{
					for (int t = startDay; t <= 7; t++)
						daysList.add(t);

					for (int t = 1; t <= endDay; t++)
						daysList.add(t);
				}
			}
			catch (Throwable e)
			{
				logger.debug(e.getMessage(), e);
			}
		}
		else
		{
			if (days.containsKey(string))
				daysList.add(days.get(string));
			else
				logger.info("Invalid week specified !!!!" + string);
		}

		logger.info("DaysList" + daysList);
		return daysList;
	}

	private static List<Integer> getTimes(String string)
	{
		List<Integer> timesList = new ArrayList<Integer>();

		string = string.substring(1, string.length() - 1);
		String[] tokens = string.split(";");
		for (String token : tokens)
		{
			if (token.contains("-"))
			{
				try
				{
					int startTime = Integer.parseInt(token.substring(0,
							token.indexOf("-")));
					int endTime = Integer.parseInt(token.substring(token
							.indexOf("-") + 1));

					if (startTime > 23 || endTime > 23)
					{
						logger.info("Invalid time specified !!!!" + string);
						continue;
					}
					else if (endTime > startTime)
						for (int t = startTime; t <= endTime; t++)
							timesList.add(t);
					else
					{
						for (int t = startTime; t <= 23; t++)
							timesList.add(t);

						for (int t = 0; t <= endTime; t++)
							timesList.add(t);
					}
				}
				catch (Throwable e)
				{
					logger.debug(e.getMessage(), e);
				}
			}
			else
			{
				try
				{
					int n = Integer.parseInt(token);
					if (n >= 0 && n <= 23)
						timesList.add(n);
					else
						logger.info("Invalid time specified !!!!" + string);
				}
				catch (Throwable t)
				{
					logger.info("Invalid time specified !!!!" + string);
				}
			}
		}

		return timesList;
	}

	public static String listToStringWithQuots(List<String> list){
		
		String finalStr = "";
		if(null!=list && !list.isEmpty()){
			for (int i = 0; i < list.size(); i++) {
				String concatStr = "'" + list.get(i) + "',";
				finalStr += concatStr;
			}
			//To remove extra cama (,) at which is at the end of the string
			finalStr = finalStr.substring(0, finalStr.length() - 1);
		}
		
		return finalStr;
	}
	
	
	public static String getEncryptedString(String parameter) {
		logger.debug("parameter to be encrypted:" + parameter);
		if(parameter != null && !parameter.isEmpty()){
		String encryptedParameter = EncryptionDecryptionUtil
				.doEncrypt(parameter);
		logger.debug("ENCRYPTED parameter:" + encryptedParameter);
		try {
			// URL-Encoding is done twice due to some limitation at IBM side.
			encryptedParameter = URLEncoder.encode(encryptedParameter, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Unsupported encoding exception." + e, e);
		}
		return encryptedParameter;
		}
		return parameter;
	}
	
	public static String getEncryptedUrl(String url) {
		if (url != null && !url.isEmpty()) {
			List<String> urlCut = Arrays.asList(url.split("\\?"));
			if (urlCut != null && !urlCut.isEmpty() && urlCut.size() >= 2) {
				HashMap<String, String> keyValue = (HashMap<String, String>) MapUtils
						.convertIntoMap(urlCut.get(1), "&", "=", null);
				String finalUrl = "";
				if (keyValue != null && !keyValue.isEmpty()) {
					for (Entry<String, String> params : keyValue.entrySet()) {
						finalUrl += params.getKey() + "=";
						String value = params.getValue();
						if (value != null
								&& !value.isEmpty()
								&& !(value.startsWith("%") && value
										.endsWith("%"))) {
							value = getEncryptedString(value);
						}
						finalUrl += value + "&";
					}
				}
				finalUrl = urlCut.get(0)+"?"
						+ finalUrl.substring(0, finalUrl.length() - 1);
				return finalUrl;
			}
		}
		return url;

	}
	
	
	// Added for selection model support
	public static boolean isConsentRequest(WebServiceContext task){
		String consentId = task.getString("CONSENTID");
		
		
		
		String consentClassType = task.getString("CONSENTCLASSTYPE");
		String consentSubClass = task.getString("CONSENTSUBCLASS");
		//RBT-9873 Added for bypassing CG flow
		boolean isChargeClassConfForNotConsentFlow = false;
		if (com.onmobile.apps.ringbacktones.services.common.Utility
				.isChargeClassConfiguredForNotCGFlow(consentClassType)) {
			isChargeClassConfForNotConsentFlow = true;
		}
		boolean isSubClassConfForNotConsentFlow = false;
		if (com.onmobile.apps.ringbacktones.services.common.Utility
				.isSubscriptionClassConfiguredForNotCGFlow(consentSubClass)) {
			isSubClassConfForNotConsentFlow = true;
		}
		logger.info("consentId = " + consentId + " consentClassType= " + consentClassType
				+ " consentSubClass= " + consentSubClass +" task: "+task);
		logger.info("isChargeClassConfForNotConsentFlow== " + isChargeClassConfForNotConsentFlow
				+ " isSubClassConfForNotConsentFlow= " + isSubClassConfForNotConsentFlow);
		
		
		boolean isConsentBypass = consentId != null
				&& ((!isChargeClassConfForNotConsentFlow && !isSubClassConfForNotConsentFlow)
						|| (!isChargeClassConfForNotConsentFlow && isSubClassConfForNotConsentFlow)
                        || (isChargeClassConfForNotConsentFlow && !isSubClassConfForNotConsentFlow)
                        || task.containsKey(param_upgrade_consent_flow));
		return isConsentBypass;
	}
	
}