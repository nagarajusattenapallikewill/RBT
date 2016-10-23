/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: thribhuvan.hl $
 * $Id: BasicXMLElementGenerator.java,v 1.175 2015/05/06 08:49:31 thribhuvan.hl Exp $
 * $Revision: 1.175 $
 * $Date: 2015/05/06 08:49:31 $
 */
package com.onmobile.apps.ringbacktones.webservice.implementation;

import static org.grep4j.core.Grep4j.constantExpression;
import static org.grep4j.core.Grep4j.grep;
import static org.grep4j.core.fluent.Dictionary.on;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.grep4j.core.model.Profile;
import org.grep4j.core.model.ProfileBuilder;
import org.grep4j.core.result.GrepResult;
import org.grep4j.core.result.GrepResults;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.FeedStatus;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.Retailer;
import com.onmobile.apps.ringbacktones.content.Scratchcard;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.OperatorUserDetailsImpl;
import com.onmobile.apps.ringbacktones.content.database.PendingConfirmationsReminderTableImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTBulkUploadTaskDAO;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.features.airtel.UserSelectionRestrictionBasedOnSubClass;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.PredefinedGroup;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.beans.RbtSupport;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.webservice.client.XMLParser;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SMSHistory;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPack;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
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
import com.onmobile.apps.ringbacktones.webservice.content.RBTContentProviderFactory;

/**
 * @author vinayasimha.patil
 *
 */
public class BasicXMLElementGenerator implements WebServiceConstants
{
	
	

	private static Logger logger = Logger.getLogger(BasicXMLElementGenerator.class);

	protected static RBTCacheManager rbtCacheManager = null;
	protected static ParametersCacheManager parametersCacheManager = null;
    protected static List<String> freemiumSubClassList = null;
    
    protected static Map<String, String> freemiumCosIdNumMaxSelectionMap = new HashMap<String, String>();
    
	static
	{
		rbtCacheManager = RBTCacheManager.getInstance();
		parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		freemiumSubClassList = Arrays.asList(RBTParametersUtils.getParamAsString(
				iRBTConstant.COMMON, "FREEMIUM_SUB_CLASSES", "").split(","));
		freemiumCosIdNumMaxSelectionMap = MapUtils.convertIntoMap(
				RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
						"FREEMIUM_COSID_NUM_MAX_SELECTIONS_MAP", null), ",",
				":", null);
		
	}

	public static Element generateSubscriberElement(Document document,
			WebServiceSubscriber webServicesubscriber)
	{
		Element element = document.createElement(SUBSCRIBER);
		element.setAttribute(SUBSCRIBER_ID, webServicesubscriber.getSubscriberID());
		element.setAttribute(IS_VALID_PREFIX, (webServicesubscriber.isValidPrefix() ? YES : NO));
		element.setAttribute(CAN_ALLOW, (webServicesubscriber.isCanAllow() ? YES : NO));
		element.setAttribute(IS_PREPAID, (webServicesubscriber.isPrepaid() ? YES : NO));
		element.setAttribute(ACCESS_COUNT, String.valueOf(webServicesubscriber.getAccessCount()));
		element.setAttribute(STATUS, webServicesubscriber.getStatus());
		if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
				"SHOW_SUBSCRIBER_TYPE_ATTRIBUTE", "TRUE")) {
			element.setAttribute(SUBSCRIBER_TYPE,
					webServicesubscriber.getSubscriberType());
			element.setAttribute(NUM_MAX_SELECTIONS,
					webServicesubscriber.getNumMaxSelections() + "");
		}
		
		String subscriberStatus = webServicesubscriber.getStatus();
		if (webServicesubscriber.getCircleID() != null)
			element.setAttribute(CIRCLE_ID, webServicesubscriber.getCircleID());
		if (webServicesubscriber.getLanguage() != null)
			element.setAttribute(LANGUAGE, webServicesubscriber.getLanguage());
		if (webServicesubscriber.getSubscriptionClass() != null)		
			element.setAttribute(SUBSCRIPTION_CLASS, webServicesubscriber.getSubscriptionClass());
		if (webServicesubscriber.getCosID() != null)
			element.setAttribute(COS_ID, webServicesubscriber.getCosID());
		if (webServicesubscriber.getRefID() != null)
			element.setAttribute(REF_ID, webServicesubscriber.getRefID());
		if (webServicesubscriber.getOperatorUserInfo() != null)
			element.setAttribute(OPERATOR_USER_INFO, webServicesubscriber.getOperatorUserInfo());
		if (webServicesubscriber.getPack() != null)
			element.setAttribute(PACK, webServicesubscriber.getPack());
		if (webServicesubscriber.getPca() != null)
			element.setAttribute(PCA, webServicesubscriber.getPca());
		if (webServicesubscriber.getActivatedBy() != null)
			element.setAttribute(ACTIVATED_BY, webServicesubscriber.getActivatedBy());
        if(webServicesubscriber.isSubConsentInserted()){
        	element.setAttribute(iRBTConstant.param_isSubConsentInserted, "true");
        }
        if(webServicesubscriber.getProtocolNo() != null){
        	element.setAttribute(iRBTConstant.param_protocolNo, webServicesubscriber.getProtocolNo());
        }
        if(webServicesubscriber.getProtocolStaticText() != null){
        	element.setAttribute(iRBTConstant.param_protocolStaticText, webServicesubscriber.getProtocolStaticText());
        }
        if(webServicesubscriber.getActivationInfo() != null) {
        	element.setAttribute(ACTIVATION_INFO, webServicesubscriber.getActivationInfo());
        }
        if(webServicesubscriber.getOperatorName() != null) {
        	element.setAttribute(XML_OPERATOR_NAME, webServicesubscriber.getOperatorName());
        }
        
        IUserDetailsService operatorUserDetailsService = null;
		if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "SHOW_SUBSCRIBER_TYPE_ATTRIBUTE", "TRUE")) {
			try {
				element.setAttribute(OPERATOR_USER_TYPE, OperatorUserTypes.NEW_USER.getDefaultValue());
				// Getting user details from B2B db cache
				operatorUserDetailsService = (IUserDetailsService) ConfigUtil.getBean(BeanConstant.USER_DETAIL_BEAN);

				OperatorUserDetails operatorUserDetails = null;

				if (operatorUserDetailsService != null) {
					operatorUserDetails = (OperatorUserDetailsImpl) operatorUserDetailsService
							.getUserDetails(webServicesubscriber.getSubscriberID());
				}

				if (operatorUserDetails != null && operatorUserDetails.serviceKey() != null) {
					element.setAttribute(OPERATOR_USER_TYPE, operatorUserDetails.serviceKey());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
				
		String blockedSubscriptionClass = parametersCacheManager
				.getParameterValue(iRBTConstant.COMMON,
						"BLOCKED_SUBSCRIPTION_CLASS", null);
		if (blockedSubscriptionClass != null) {
			List blockedSubscriptionClassList = Arrays
					.asList(blockedSubscriptionClass.split(","));
			if (blockedSubscriptionClassList != null
					&& webServicesubscriber.getSubscriptionClass() != null) {
				element.setAttribute(IS_SERVICE_KEY_BLOCKED_FOR_SELECTION,
						blockedSubscriptionClassList
								.contains(webServicesubscriber
										.getSubscriptionClass()) ? "y" : "n");
			}
		}				
        
        boolean isFreemiumUser = false;
        
        String cosId = webServicesubscriber.getCosID();
        boolean isNewUser= false;
        boolean isFreemium = false;
        int noOfFreeSongsLeft = 0;
        String cosFreeSelection = cosId != null ? freemiumCosIdNumMaxSelectionMap.get(cosId) : null;
		if (subscriberStatus != null
				&& (subscriberStatus.equalsIgnoreCase(NEW_USER) || subscriberStatus
						.equalsIgnoreCase(DEACTIVE)))
			isNewUser = true;
		if (!isNewUser
				&& freemiumSubClassList.contains(webServicesubscriber
						.getSubscriptionClass()) && cosFreeSelection != null) {
			
			isFreemium = true;
			
			try {
				int iCosFreeSelection = Integer.parseInt(cosFreeSelection);
				noOfFreeSongsLeft = iCosFreeSelection - webServicesubscriber.getNumMaxSelections();
				if(noOfFreeSongsLeft <= 0) {
					noOfFreeSongsLeft = 0;
				} 
				
				if (webServicesubscriber.getNumMaxSelections() < iCosFreeSelection) {
					isFreemiumUser = true;
				}
			} catch (NumberFormatException e) {
			}
		}
		
        
        if(isFreemiumUser){
        	element.setAttribute(IS_FREEMIUM_SUBSCRIBER, "true");
        }
        
        if(isFreemium) {
        	element.setAttribute(IS_FREEMIUM, "true");
        }
        
        if(noOfFreeSongsLeft > 0) {
        	element.setAttribute(NUM_OF_FREE_SONGS_LEFT, String.valueOf(noOfFreeSongsLeft));
        }
        
		HashMap<String, String> userInfoMap = DBUtility.getAttributeMapFromXML(webServicesubscriber.getUserInfo());
		
		//Added by SenthilRaja for TNB user
		String userType = webServicesubscriber.getUserType();
		if(userInfoMap != null 
				&& userInfoMap.containsKey(iRBTConstant.TNB_USER) 
				&& userInfoMap.get(iRBTConstant.TNB_USER).equalsIgnoreCase("TRUE")) {
			userType = TNB;
		}
		element.setAttribute(USER_TYPE, userType);
		if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "SHOW_VOLUNTARY_ATTRIBUTE", "TRUE")){
		      element.setAttribute("VOLUNTARY", webServicesubscriber.getVoluntary());
		}
		//JIRA-ID: RBT-13626:
		String premiumChargeClass = Utility.isUDSUser(userInfoMap,true);
		boolean isUDSUser = (premiumChargeClass != null);
		if (isUDSUser)
			element.setAttribute(IS_UDS_ON, YES);

		if (!webServicesubscriber.getStatus().equalsIgnoreCase(ACT_PENDING))
		{
			if (userInfoMap != null
					&& userInfoMap.containsKey(iRBTConstant.REFUND)
					&& !userInfoMap.containsKey(iRBTConstant.REFUNDED))
				element.setAttribute(IS_REFUNDABLE, YES);
		}
		//Added by Sreekar for offer management
		String userInfo = webServicesubscriber.getUserInfo();
		if (userInfo != null) element.setAttribute(USER_INFO, userInfo);
		if (userInfoMap != null && userInfoMap.get(iRBTConstant.EXTRA_INFO_TOBE_ACT_OFFER_ID) != null)
			element.setAttribute(TOBE_ACT_OFFER_ID, userInfoMap.get(iRBTConstant.EXTRA_INFO_TOBE_ACT_OFFER_ID));
		else if (userInfoMap != null && userInfoMap.get(iRBTConstant.EXTRA_INFO_OFFER_ID) != null)
			element.setAttribute(OFFER_ID, userInfoMap.get(iRBTConstant.EXTRA_INFO_OFFER_ID));
		//end of changes for offer management

		if (userInfoMap != null && userInfoMap.get(iRBTConstant.EXTRA_INFO_IMEI_NO) != null)
			element.setAttribute(IMEI_NO, userInfoMap.get(iRBTConstant.EXTRA_INFO_IMEI_NO));
		if (userInfoMap != null && userInfoMap.get("SCRN") != null)
			element.setAttribute("scrn", userInfoMap.get("SCRN"));
		if (userInfoMap != null && userInfoMap.get("SCRS") != null)
			element.setAttribute("scrs", userInfoMap.get("SCRS"));
		
		//Added by Sreekar for Vf-Spain
		if (userInfoMap != null && userInfoMap.get(iRBTConstant.DELAY_DEACT) != null)
			element.setAttribute(iRBTConstant.DELAY_DEACT, userInfoMap.get(iRBTConstant.DELAY_DEACT));
		

		return element;
	}

	public static Element generateSubscriberGiftContentElement(
			Document document, WebServiceGift webServiceGift)
	{
		return generateSubscriberGiftContentElement(document, webServiceGift, null);
	}
	
	public static Element generateSubscriberGiftContentElement(
			Document document, WebServiceGift webServiceGift, WebServiceContext task)
	{
		String toneType = webServiceGift.getToneType();

		Element element = document.createElement(CONTENT);
		element.setAttribute(ID, String.valueOf(webServiceGift.getToneID()));
		element.setAttribute(NAME, webServiceGift.getToneName());
		element.setAttribute(TYPE, toneType);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		
		String format= null;
		if(task != null)
			 format = task.getString(param_format);
		
		Utility.addPropertyElement(document, element, SENDER, DATA, webServiceGift.getSender());
		Utility.addPropertyElement(document, element, RECEIVER, DATA, webServiceGift.getReceiver());
		Utility.addPropertyElement(document, element, CATEGORY_ID, DATA, String.valueOf(webServiceGift.getCategoryID()));
		Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(webServiceGift.getPreviewFile(),format));
		Utility.addPropertyElement(document, element, RBT_FILE, PROMPT, Utility.getPromptName(webServiceGift.getRbtFile(),format));
		if(DBConfigTools.getParameter(iRBTConstant.COMMON, iRBTConstant.ADD_VCODE_IN_XML_RESPONSE, false)){
		    Utility.addPropertyElement(document, element, VCODE, DATA, webServiceGift.getClipVcode());
		}
		Utility.addPropertyElement(document, element, SENT_TIME, DATA, dateFormat.format(webServiceGift.getSentTime()));
		Utility.addPropertyElement(document, element, VALIDITY, DATA, dateFormat.format(webServiceGift.getValidity()));
		if (webServiceGift.getGiftExtraInfo() != null)
			Utility.addPropertyElement(document, element, EXTRA_INFO, DATA, webServiceGift.getGiftExtraInfo());
		if (webServiceGift.getSelectedBy() != null)
			Utility.addPropertyElement(document, element, SELECTED_BY, DATA, webServiceGift.getSelectedBy());

		return element;
	}

	public static Element generateSubscriberSettingContentElement(Document document,
			WebServiceContext task, WebServiceSubscriberSetting webServiceSubscriberSetting)
	{
		String toneType = webServiceSubscriberSetting.getToneType();

		Element element = document.createElement(CONTENT);
		element.setAttribute(ID, String.valueOf(webServiceSubscriberSetting.getToneID()));
		element.setAttribute(NAME, webServiceSubscriberSetting.getToneName());
		element.setAttribute(TYPE, toneType);

		Utility.addPropertyElement(document, element, CALLER_ID, DATA, webServiceSubscriberSetting.getCallerID());

		String[] previewFiles = webServiceSubscriberSetting.getPreviewFiles();
		String[] rbtFiles = webServiceSubscriberSetting.getRbtFiles();
		
		String format= null;
		if(task != null)
			 format = task.getString(param_format);
		if (toneType.equalsIgnoreCase(CATEGORY_BOUQUET) || toneType.equalsIgnoreCase(CATEGORY_SHUFFLE))
		{
			Utility.addPropertyElement(document, element, CATEGORY_PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[1],format));
			Utility.addPropertyElement(document, element, CATEGORY_NAME_FILE, PROMPT, Utility.getPromptName(rbtFiles[1],format));
			Utility.addPropertyElement(document, element, SHUFFLE_ID, DATA, String.valueOf(webServiceSubscriberSetting.getShuffleID()));
			Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0],format));
			Utility.addPropertyElement(document, element, RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[0],format));
		}
		else if (toneType.equalsIgnoreCase(CATEGORY_RECORD) || toneType.equalsIgnoreCase(CATEGORY_KARAOKE))
		{
			Utility.addPropertyElement(document, element, UGC_PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0],format));
			Utility.addPropertyElement(document, element, UGC_RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[0],format));
		}
		else
		{
			Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0],format));
			Utility.addPropertyElement(document, element, RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[0],format));
			if(DBConfigTools.getParameter(iRBTConstant.COMMON, iRBTConstant.ADD_VCODE_IN_XML_RESPONSE, false)){
			   Utility.addPropertyElement(document, element, VCODE, DATA, webServiceSubscriberSetting.getClipVcode());
			}
		} 

		Utility.addPropertyElement(document, element, FROM_TIME, DATA, String.valueOf(webServiceSubscriberSetting.getFromTime()));
		Utility.addPropertyElement(document, element, FROM_TIME_MINUTES, DATA, String.valueOf(webServiceSubscriberSetting.getFromTimeMinutes()));
		Utility.addPropertyElement(document, element, TO_TIME, DATA, String.valueOf(webServiceSubscriberSetting.getToTime()));
		Utility.addPropertyElement(document, element, TO_TIME_MINUTES, DATA, String.valueOf(webServiceSubscriberSetting.getToTimeMinutes()));
		Utility.addPropertyElement(document, element, STATUS, DATA, String.valueOf(webServiceSubscriberSetting.getStatus()));
		Utility.addPropertyElement(document, element, SELECTION_STATUS, DATA, webServiceSubscriberSetting.getSelectionStatus());
		Utility.addPropertyElement(document, element, SELECTION_TYPE, DATA, webServiceSubscriberSetting.getSelectionType());
		Utility.addPropertyElement(document, element, INTERVAL, DATA, webServiceSubscriberSetting.getSelInterval());

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		Utility.addPropertyElement(document, element, CATEGORY_ID, DATA, String.valueOf(webServiceSubscriberSetting.getCategoryID()));
		Utility.addPropertyElement(document, element, SET_TIME, DATA, dateFormat.format(webServiceSubscriberSetting.getSetTime()));
		Utility.addPropertyElement(document, element, CHARGE_CLASS, DATA, webServiceSubscriberSetting.getChargeClass());
		Utility.addPropertyElement(document, element, REF_ID, DATA, webServiceSubscriberSetting.getRefID());
		Utility.addPropertyElement(document, element, START_TIME, DATA, dateFormat.format(webServiceSubscriberSetting.getStartTime()));
		Utility.addPropertyElement(document, element, END_TIME, DATA, dateFormat.format(webServiceSubscriberSetting.getEndTime()));
		Utility.addPropertyElement(document, element, LOOP_STATUS, DATA, webServiceSubscriberSetting.getLoopStatus());
		
		if(webServiceSubscriberSetting.getNextChargingDate() != null){
			Utility.addPropertyElement(document, element, NEXT_CHARGING_DATE, DATA, dateFormat.format(webServiceSubscriberSetting.getNextChargingDate()));
		}
		
		String amountChargedKey = webServiceSubscriberSetting.getRefID() + "_lastAmountCharged";
		String lastChargedAmount = Utility.getNextBillingDateOfServices(task).get(amountChargedKey);
		if(lastChargedAmount != null){
			Utility.addPropertyElement(document, element, LAST_CHARGE_AMOUNT, DATA, lastChargedAmount);
		}
		
		String lastTransactionTypeKey = webServiceSubscriberSetting.getRefID() + "_lastTransactionType";
		String lastTransactionType = Utility.getNextBillingDateOfServices(task).get(lastTransactionTypeKey);
		if(lastTransactionType != null){
			Utility.addPropertyElement(document, element, LAST_TRANSACTION_TYPE, DATA, lastTransactionType);
		}

		// Adding nextBillingDate property only if requested for library history
		//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
		if (task.containsKey(param_info) && task.getString(param_info).contains(LIBRARY_HISTORY) 
				|| (task.containsKey(param_showNextBillDate) && task.getString(param_showNextBillDate).contains("true"))) {
			String nextBillingDate = Utility.getNextBillingDateOfServices(task).get(webServiceSubscriberSetting.getRefID());
			Utility.addPropertyElement(document, element, NEXT_BILLING_DATE, DATA, nextBillingDate);
		} 
		
		if(task.containsKey(param_mode) && task.getString(param_mode).contains("CCC")) {
			Utility.addPropertyElement(document, element, SELECTION_STATUS_ID, DATA, webServiceSubscriberSetting.getSelectionStatusID());
		}

		if (webServiceSubscriberSetting.getIsCurrentSetting() != null) { //RBT-12247
			Utility.addPropertyElement(document, element, IS_CURRENT_SETTING, DATA, String.valueOf(webServiceSubscriberSetting.getIsCurrentSetting()));
		}
		
		//Added for UDP_ID
		if (webServiceSubscriberSetting.getUdpId() != null) {
			Utility.addPropertyElement(document, element,param_udpId, DATA, webServiceSubscriberSetting.getUdpId());
		}
		
		if (webServiceSubscriberSetting.getDeselectedBy() != null) {
			Utility.addPropertyElement(document, element,param_deselectedBy, DATA, webServiceSubscriberSetting.getDeselectedBy());
		}
		
		if(webServiceSubscriberSetting.getSelectionExtraInfo() != null && !RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DISABLE_SELECTION_EXTRA_INFO", "FALSE")){
		  Utility.addPropertyElement(document, element, SELECTION_EXTRA_INFO, DATA, webServiceSubscriberSetting.getSelectionExtraInfo());
		}
		
		if (webServiceSubscriberSetting.getCutRBTStartTime() != null) {
			Utility.addPropertyElement(document, element,CUT_RBT_START_TIME, DATA, webServiceSubscriberSetting.getCutRBTStartTime());
		}
		
		return element;
	}

	public static Element generateSubscriberDownloadContentElement(Document document,
			WebServiceContext task, WebServiceSubscriberDownload webServiceSubscriberDownload)
	{
		String toneType = webServiceSubscriberDownload.getToneType();

		Element element = document.createElement(CONTENT);
		element.setAttribute(ID, String.valueOf(webServiceSubscriberDownload.getToneID()));
		element.setAttribute(NAME, webServiceSubscriberDownload.getToneName());
		element.setAttribute(TYPE, toneType);

		String[] previewFiles = webServiceSubscriberDownload.getPreviewFiles();
		String[] rbtFiles = webServiceSubscriberDownload.getRbtFiles();
		
		String format= null;
		if(task != null)
			 format = task.getString(param_format);
		
		if (toneType.equalsIgnoreCase(CATEGORY_BOUQUET) || toneType.equalsIgnoreCase(CATEGORY_SHUFFLE))
		{
			Utility.addPropertyElement(document, element, CATEGORY_PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[1],format));
			Utility.addPropertyElement(document, element, CATEGORY_NAME_FILE, PROMPT, Utility.getPromptName(rbtFiles[1],format));
			Utility.addPropertyElement(document, element, SHUFFLE_ID, DATA, String.valueOf(webServiceSubscriberDownload.getShuffleID()));
			Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0],format));
			Utility.addPropertyElement(document, element, RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[0],format));
		}
		else if (toneType.equalsIgnoreCase(CATEGORY_RECORD) || toneType.equalsIgnoreCase(CATEGORY_KARAOKE))
		{
			Utility.addPropertyElement(document, element, UGC_PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0],format));
			Utility.addPropertyElement(document, element, UGC_RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[0],format));
		}
		else
		{
			Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0],format));
			Utility.addPropertyElement(document, element, RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[0],format));
			if(DBConfigTools.getParameter(iRBTConstant.COMMON, iRBTConstant.ADD_VCODE_IN_XML_RESPONSE, false)){
			    Utility.addPropertyElement(document, element, VCODE, DATA, webServiceSubscriberDownload.getClipVcode());
			}
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		Utility.addPropertyElement(document, element, DOWNLOAD_STATUS, DATA, webServiceSubscriberDownload.getDownloadStatus());
		Utility.addPropertyElement(document, element, DOWNLOAD_TYPE, DATA, webServiceSubscriberDownload.getDownloadType());
		Utility.addPropertyElement(document, element, CATEGORY_ID, DATA, String.valueOf(webServiceSubscriberDownload.getCategoryID()));
		Utility.addPropertyElement(document, element, SET_TIME, DATA, dateFormat.format(webServiceSubscriberDownload.getSetTime()));
		Utility.addPropertyElement(document, element, SELECTED_BY, DATA, webServiceSubscriberDownload.getSelectedBy());
		Utility.addPropertyElement(document, element, CHARGE_CLASS, DATA, webServiceSubscriberDownload.getChargeClass());
		Utility.addPropertyElement(document, element, REF_ID, DATA, webServiceSubscriberDownload.getRefID());
		Utility.addPropertyElement(document, element, SELECTION_INFO, DATA, webServiceSubscriberDownload.getSelectionInfo());
		Utility.addPropertyElement(document, element, END_TIME, DATA, dateFormat.format(webServiceSubscriberDownload.getEndTime()));
		
		if(webServiceSubscriberDownload.getLastChargedDate() != null){
			Utility.addPropertyElement(document, element, LAST_CHARGED_DATE, DATA, dateFormat.format(webServiceSubscriberDownload.getLastChargedDate()));
		}
		
		Map<String, String> chargingDetailsMap = Utility.getNextBillingDateOfServices(task);
		String amountChargedKey = webServiceSubscriberDownload.getRefID() + "_lastAmountCharged";
		String lastChargedAmount = chargingDetailsMap.get(amountChargedKey);
		if(lastChargedAmount != null){
			Utility.addPropertyElement(document, element, LAST_CHARGE_AMOUNT, DATA, lastChargedAmount);
		}
		
		if(webServiceSubscriberDownload.getNextBillingDate()!=null){
			Utility.addPropertyElement(document, element, NEXT_BILLING_DATE, DATA, dateFormat.format(webServiceSubscriberDownload.getNextBillingDate()));
		}else{
		// Adding nextBillingDate property only if requested for library history
		//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
		if ((task.containsKey(param_info) && task.getString(param_info).contains(LIBRARY_HISTORY)) 
				|| (task.containsKey(param_showNextBillDate) && task.getString(param_showNextBillDate).contains("true")))
		{
			String nextBillingDate = null;
			
			HashMap<String, String> downloadExtraInfo = DBUtility.getAttributeMapFromXML(webServiceSubscriberDownload.getDownloadInfo());
			if (downloadExtraInfo != null && downloadExtraInfo.containsKey(CAMPAIGN_ID))
			{
				// If download is of corporate, return campaign end time in next billing date 
				SimpleDateFormat rbtDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				RBTBulkUploadTask rbtBulkUploadTask = RBTBulkUploadTaskDAO.getRBTBulkUploadTask(Integer.parseInt(downloadExtraInfo.get(CAMPAIGN_ID)));
				nextBillingDate = rbtDateFormat.format(rbtBulkUploadTask.getEndTime());
			}
			else		
				nextBillingDate = chargingDetailsMap.get(webServiceSubscriberDownload.getRefID());
			
			Utility.addPropertyElement(document, element, NEXT_BILLING_DATE, DATA, nextBillingDate);

			if (nextBillingDate != null)
			{
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				Calendar cal = Calendar.getInstance();
				try
				{
					int selPeriod =  0;
					boolean isAllowed = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
							"ENABLE_LAST_BILL_DATE_CALCULATION_ON_NUMBER_OF_DAYS", "FALSE");
					if (isAllowed) {
						String selectionPeriod = CacheManagerUtil.getChargeClassCacheManager()
								.getChargeClass(webServiceSubscriberDownload.getChargeClass())
								.getSelectionPeriod();
						cal.setTime(formatter.parse(nextBillingDate));
						if (selectionPeriod.startsWith("D")) {
							selPeriod = Integer.parseInt(selectionPeriod.substring(1));
							cal.add(Calendar.DAY_OF_YEAR, -(selPeriod));
						} else if (selectionPeriod.startsWith("M")) {
							String noOfMonths = selectionPeriod.substring(1);
							cal.add(Calendar.MONTH, -(Integer.parseInt(noOfMonths)));
						} else {
							selPeriod = Integer.parseInt(selectionPeriod);
							cal.add(Calendar.DAY_OF_YEAR, -(selPeriod));
						}
					} else {
						selPeriod = CacheManagerUtil.getChargeClassCacheManager()
								.getChargeClass(webServiceSubscriberDownload.getChargeClass())
								.getSelectionPeriodInDays();
						cal.setTime(formatter.parse(nextBillingDate));
						cal.add(Calendar.DAY_OF_YEAR, -(selPeriod));
					}

					String nextChargingDate = formatter.format(cal.getTime());
					Element nextChargingDateElem = Utility.getPropertyElement(element, NEXT_CHARGING_DATE);
					if (nextChargingDateElem == null && nextChargingDate != null)
						Utility.addPropertyElement(document, element, NEXT_CHARGING_DATE, DATA, nextChargingDate);
				}
				catch (ParseException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
		

		return element;
	}

	public static Element generateSubscriberBookMarkContentElement(Document document,
			WebServiceSubscriberBookMark webServiceSubscriberBookMark)
	{
		return generateSubscriberBookMarkContentElement(document, webServiceSubscriberBookMark, null);
	}

	public static Element generateSubscriberBookMarkContentElement(Document document,
			WebServiceSubscriberBookMark webServiceSubscriberBookMark,WebServiceContext task)
	{
		String toneType = webServiceSubscriberBookMark.getToneType();

		Element element = document.createElement(CONTENT);
		element.setAttribute(ID, String.valueOf(webServiceSubscriberBookMark.getToneID()));
		element.setAttribute(NAME, webServiceSubscriberBookMark.getToneName());
		element.setAttribute(TYPE, toneType);

		String[] previewFiles = webServiceSubscriberBookMark.getPreviewFiles();
		String[] rbtFiles = webServiceSubscriberBookMark.getRbtFiles();
		
		String format= null;
		if(task!= null)
			 format = task.getString(param_format);
		
		if (toneType.equalsIgnoreCase(CATEGORY_BOUQUET) || toneType.equalsIgnoreCase(CATEGORY_SHUFFLE))
		{
			Utility.addPropertyElement(document, element, CATEGORY_PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0],format));
			Utility.addPropertyElement(document, element, CATEGORY_NAME_FILE, PROMPT, Utility.getPromptName(rbtFiles[0],format));
			Utility.addPropertyElement(document, element, SHUFFLE_ID, DATA, String.valueOf(webServiceSubscriberBookMark.getShuffleID()));
			Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[1],format));
			Utility.addPropertyElement(document, element, RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[1],format));
		}
		else if (toneType.equalsIgnoreCase(CATEGORY_RECORD) || toneType.equalsIgnoreCase(CATEGORY_KARAOKE))
		{
			Utility.addPropertyElement(document, element, UGC_PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0],format));
			Utility.addPropertyElement(document, element, UGC_RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[0],format));
		}
		else
		{
			Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0],format));
			Utility.addPropertyElement(document, element, RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[0],format));
			if(DBConfigTools.getParameter(iRBTConstant.COMMON, iRBTConstant.ADD_VCODE_IN_XML_RESPONSE, false)){
			   Utility.addPropertyElement(document, element, VCODE, DATA, webServiceSubscriberBookMark.getClipVcode());
			}
		}

		Utility.addPropertyElement(document, element, CATEGORY_ID, DATA, String.valueOf(webServiceSubscriberBookMark.getCategoryID()));

		return element;
	}
	
	public static Element generateGroupContentElement(Document document, WebServiceGroup webServiceGroup)
	{
		return generateGroupContentElement(document, webServiceGroup, null);
	}

	public static Element generateGroupContentElement(Document document, WebServiceGroup webServiceGroup,WebServiceContext task)
	{
		Element element = document.createElement(CONTENT);
		element.setAttribute(ID, webServiceGroup.getGroupID());
		element.setAttribute(NAME, webServiceGroup.getGroupName());
		
		String format= null;
		if(task != null)
			 format = task.getString(param_format);
		
		Utility.addPropertyElement(document, element, GROUP_PROMO_ID, DATA, webServiceGroup.getGroupPromoID());
		Utility.addPropertyElement(document, element, PREDEFINED_GROUP_ID, DATA, webServiceGroup.getPredefinedGroupID());
		Utility.addPropertyElement(document, element, GROUP_STATUS, DATA, webServiceGroup.getGroupStatus());
		Utility.addPropertyElement(document, element, GROUP_NAME_PROMPT, PROMPT, Utility.getPromptName(webServiceGroup.getGroupNamePrompt(),format));
		Utility.addPropertyElement(document, element, NO_OF_MEMBERS, DATA, String.valueOf(webServiceGroup.getNoOfMembers()));

		return element;
	}
	
	public static Element generateGroupMemberContentElement(Document document, WebServiceGroupMember webServiceGroupMember)
	{
		Element element = document.createElement(CONTENT);
		element.setAttribute(ID, webServiceGroupMember.getMemberID());
		element.setAttribute(NAME, webServiceGroupMember.getMemberName());

		Utility.addPropertyElement(document, element, MEMBER_STATUS, DATA, webServiceGroupMember.getMemberStatus());

		return element;
	}

	public static Element generateCopyDetailsContentElement(Document document,
			WebServiceCopyData webServiceCopyData)
	{
		return generateCopyDetailsContentElement(document, webServiceCopyData, null);
	}

	public static Element generateCopyDetailsContentElement(Document document,
			WebServiceCopyData webServiceCopyData, WebServiceContext task)
	{
		String toneType = webServiceCopyData.getToneType();

		Element element = document.createElement(CONTENT);
		element.setAttribute(ID, String.valueOf(webServiceCopyData.getToneID()));
		element.setAttribute(NAME, webServiceCopyData.getToneName());
		element.setAttribute(TYPE, toneType);

		Utility.addPropertyElement(document, element, CATEGORY_ID, DATA, String.valueOf(webServiceCopyData.getCategoryID()));
		Utility.addPropertyElement(document, element, STATUS, DATA, String.valueOf(webServiceCopyData.getStatus()));

		String prevFilePropertyName = PREVIEW_FILE;
		if (toneType.equalsIgnoreCase(CATEGORY_SHUFFLE))
			prevFilePropertyName = CATEGORY_PREVIEW_FILE;

		String format= null;
		if(task != null)
			 format = task.getString(param_format);
		
		Utility.addPropertyElement(document, element, prevFilePropertyName, PROMPT, Utility.getPromptName(webServiceCopyData.getPreviewFile(),format));

		if (webServiceCopyData.getChargeClass() != null)
		{
			Utility.addPropertyElement(document, element, CHARGE_CLASS, DATA, webServiceCopyData.getChargeClass());
			Utility.addPropertyElement(document, element, AMOUNT, DATA, webServiceCopyData.getAmount());
			Utility.addPropertyElement(document, element, PERIOD, DATA, webServiceCopyData.getPeriod());
		}
		Utility.addPropertyElement(document, element, ISSHUFFLEORLOOP, DATA, webServiceCopyData.isShuffleOrLoop()+"");

		return element;
	}

	
	public static Element generateWCHistoryElement(Document document, WebServiceContext task){
		Element element = document.createElement(WC_HISTORY);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		String subscriberID = task.getString(param_subscriberID);
		Parameters wcFilePathParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "WC_LOGGING_DIR", ".");
		String wcFilePath = wcFilePathParam.getValue().trim();

		/*Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		Date date = calendar.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String strDate = dateFormat.format(date);
		String strYear = strDate.substring(0, 4);
		String strMonth = strDate.substring(4, 6);*/
		//String dirName = wcFilePath + File.separator + strYear + File.separator + strMonth;
		
		
		Date endDate = null;
		Date startDate = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		
		if(task.containsKey(param_startDate)) {
			try {
				startDate = dateFormat.parse(task.getString(param_startDate));
			}
			catch(Exception e) {}
		}
		
		if(task.containsKey(param_endDate)) {
			try {
				endDate = dateFormat.parse(task.getString(param_endDate));
			}
			catch(Exception e) {}
		}
		
		
		Calendar caleddar = Calendar.getInstance();
		if(endDate == null) {
			endDate = caleddar.getTime();
		}		
		if(startDate == null) {
			caleddar.add(Calendar.DATE, -RBTParametersUtils.getParamAsInt("COMMON", "WC_HISTORY_OF_LAST_N_DAYS", 30));
			startDate = caleddar.getTime();
		}
		
		final Date fendDate = endDate;
		final Date fstartDate = startDate; 

		FileFilter fileFilter = new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				if (pathname.isDirectory())
					return false;

				
//				Calendar caleddar = Calendar.getInstance();
//				Date endDate = caleddar.getTime();
//				caleddar.add(Calendar.DATE, -RBTParametersUtils.getParamAsInt("COMMON", "WC_HISTORY_OF_LAST_N_DAYS", 30));
//				Date startDate = caleddar.getTime();
				

				Date filelastModifiedDate = new Date(pathname.lastModified());
				if (filelastModifiedDate.equals(fstartDate)
						|| filelastModifiedDate.equals(fendDate)
						|| (filelastModifiedDate.after(fstartDate) && filelastModifiedDate.before(fendDate)))
				{
					logger.info("RBT:: FileFilter accepting file: " + pathname.getAbsolutePath());
					return true;
				}

				return false;
			}
		};

		File wcDirectory = new File(wcFilePath );
		File[] currentMonthFiles = wcDirectory.listFiles(fileFilter);

		for (File file : currentMonthFiles)
		{
			FileReader fileReader = null;
			BufferedReader bufferedReader = null;
			try
			{
				fileReader = new FileReader(file);
				bufferedReader = new BufferedReader(fileReader);
				String line;
				int lineNumber = 0;
				while ((line = bufferedReader.readLine()) != null)
				{   
					lineNumber++;
					if(lineNumber == 1)
						continue;      //for header in the log file.
					if(line.indexOf(subscriberID) == -1) // WC history for a subscriber.
						continue;
                    String modeInfo = null;
                    String retailerID = null;
                    String mode = null;
                    String requestDate = null;
                    String action = null;

					String []str = line.split(",");
                    if(str!=null && str.length == 6){
                    	  requestDate = str[0]; 
                    	  action = str[2];
                    	  modeInfo = str[3];
                    	  retailerID = str[4];
                    	  mode = str[5];

                    }
					Element contentElem = document.createElement(CONTENT);
					contentElem.setAttribute(WC_REQUEST_DATE, requestDate);
					contentElem.setAttribute(WC_ACTION, action);
					contentElem.setAttribute(WC_MODE_INFO, modeInfo);
					contentElem.setAttribute(WC_RETAILER_ID, retailerID);
					contentElem.setAttribute(WC_MODE, mode);
					contentsElem.appendChild(contentElem);

                    
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}

		return element;
		
	}

	public static Element generateSMSHistoryElement(Document document, WebServiceContext task)
	{
		Element element = document.createElement(SMS_HISTORY);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		String subscriberID = task.getString(param_subscriberID);

		Parameters sdrFilePathParam = parametersCacheManager.getParameter(iRBTConstant.SMS, "SDR_WORKING_DIR", ".");
		String sdrFilePath = sdrFilePathParam.getValue().trim();

		Parameters supportGrep4j = parametersCacheManager.getParameter(iRBTConstant.COMMON, "SUPPORT_GREP_FOR_SMS_HISTORY", null);
		boolean isSupportGrep4j = false;
		if(supportGrep4j != null) {
			isSupportGrep4j = supportGrep4j.getValue().equalsIgnoreCase("TRUE");
		}
		
		if(isSupportGrep4j) {
			processSmsHistoryGrep4j(subscriberID, sdrFilePath, document, contentsElem, null);
		}
       else {
            
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH, -1);
				Date date = calendar.getTime();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				String strDate = dateFormat.format(date);
				String strYear = strDate.substring(0, 4);
				String strMonth = strDate.substring(4, 6);
				String dirName = sdrFilePath + File.separator + strYear
						+ File.separator + strMonth;

				FileFilter fileFilter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						if (pathname.isDirectory())
							return false;

						Calendar caleddar = Calendar.getInstance();
						Date endDate = caleddar.getTime();
						caleddar.add(Calendar.DATE, -RBTParametersUtils
								.getParamAsInt("COMMON",
										"SMS_HISTORY_OF_LAST_N_DAYS", 30));
						Date startDate = caleddar.getTime();

						Date filelastModifiedDate = new Date(
								pathname.lastModified());
						if (filelastModifiedDate.equals(startDate)
								|| filelastModifiedDate.equals(endDate)
								|| (filelastModifiedDate.after(startDate) && filelastModifiedDate
										.before(endDate))) {
							logger.info("RBT:: FileFilter accepting file: "
									+ pathname.getAbsolutePath());
							return true;
						}

						return false;
					}
				};

				File lastMonthSDRDirectory = new File(dirName);
				File[] lastMonthFiles = null;
				if (lastMonthSDRDirectory.exists())
					lastMonthFiles = lastMonthSDRDirectory
							.listFiles(fileFilter);

				File sdrDirectory = new File(sdrFilePath);
				File[] currentMonthFiles = sdrDirectory.listFiles(fileFilter);

				int allFilesCount = 0;
				if (lastMonthFiles != null)
					allFilesCount = lastMonthFiles.length;
				if (currentMonthFiles != null)
					allFilesCount += currentMonthFiles.length;

				File[] allFiles = new File[allFilesCount];

				int destPos = 0;
				if (lastMonthFiles != null) {
					System.arraycopy(lastMonthFiles, 0, allFiles, destPos,
							lastMonthFiles.length);
					destPos = lastMonthFiles.length;
				}
				if (currentMonthFiles != null)
					System.arraycopy(currentMonthFiles, 0, allFiles, destPos,
							currentMonthFiles.length);

				for (File file : allFiles) {
					processSmsHistoryFile(subscriberID, file, document,
							contentsElem);
				}
			}
		
		   	String startDateStr = task.getString("startDate");
		    String endDateStr = task.getString("endDate");
		    java.util.Date startDate = null;
		    java.util.Date endDate = null;
		    if ((startDateStr != null) && (endDateStr != null)) {
		      try {
		        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		        startDate = dateFormat.parse(startDateStr);
		        endDate = dateFormat.parse(endDateStr);
		      }
		      catch (Exception e)
		      {
		    	  logger.info("Exception while parsing start date and end date."+e);
		      }
		}
		
		contentsElem = generateSMSHistoryNewTransElement(document, task, contentsElem, startDate, endDate);

		return element;

	}

	public static Element generateSMSHistoryNewTransElement(Document document, WebServiceContext task, Element contentsElem, Date startDate, Date endDate)
	{
		String subscriberID = task.getString(param_subscriberID);

		Parameters sdrFilePathParam = parametersCacheManager.getParameter(iRBTConstant.SMS, "SDR_WORKING_DIR", ".");
		String sdrFilePath = sdrFilePathParam.getValue().trim();
		
		Parameters supportGrep4j = parametersCacheManager.getParameter(iRBTConstant.COMMON, "SUPPORT_GREP_FOR_SMS_HISTORY", null);
		boolean isSupportGrep4j = false;
		if(supportGrep4j != null) {
			isSupportGrep4j = supportGrep4j.getValue().equalsIgnoreCase("TRUE");
		}
		

		SortedMap<String, List<Element>> smsTransactionSortedMap = new TreeMap<String, List<Element>>();
		if(isSupportGrep4j) {
			processSmsHistoryGrep4j(subscriberID, sdrFilePath, document, contentsElem, smsTransactionSortedMap);
		}
		else {

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -1);
			Date date = calendar.getTime();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			String strDate = dateFormat.format(date);
			String strYear = strDate.substring(0, 4);
			String strMonth = strDate.substring(4, 6);
			String dirName = sdrFilePath + File.separator + strYear + File.separator + strMonth;
	
			FileFilter fileFilter = new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					if (pathname.isDirectory())
						return false;
	
					Calendar caleddar = Calendar.getInstance();
					Date endDate = caleddar.getTime();
					caleddar.add(Calendar.DATE, -RBTParametersUtils.getParamAsInt("COMMON", "SMS_HISTORY_OF_LAST_N_DAYS", 30));
					Date startDate = caleddar.getTime();
	
					Date filelastModifiedDate = new Date(pathname.lastModified());
					if (filelastModifiedDate.equals(startDate)
							|| filelastModifiedDate.equals(endDate)
							|| (filelastModifiedDate.after(startDate) && filelastModifiedDate.before(endDate)))
					{
						logger.info("RBT:: FileFilter accepting file: " + pathname.getAbsolutePath());
						return true;
					}
	
					return false;
				}
			};
	
			File lastMonthSDRDirectory = new File(dirName);
			File[] lastMonthFiles = null;
			if (lastMonthSDRDirectory.exists())
				lastMonthFiles = lastMonthSDRDirectory.listFiles(fileFilter);
	
			File sdrDirectory = new File(sdrFilePath);
			File[] currentMonthFiles = sdrDirectory.listFiles(fileFilter);
	
			int allFilesCount = 0;
			if (lastMonthFiles != null)
				allFilesCount = lastMonthFiles.length;
			if (currentMonthFiles != null)
				allFilesCount += currentMonthFiles.length;
	
			File[] allFiles = new File[allFilesCount];
	
			int destPos = 0; 
			if (lastMonthFiles != null)
			{
				System.arraycopy(lastMonthFiles, 0, allFiles, destPos, lastMonthFiles.length);
				destPos = lastMonthFiles.length;
			}
			if (currentMonthFiles != null)
				System.arraycopy(currentMonthFiles, 0, allFiles, destPos, currentMonthFiles.length);
	
			
			for (File file : allFiles)
			{
				FileReader fileReader = null;
				BufferedReader bufferedReader = null;
				try
				{
					fileReader = new FileReader(file);
					bufferedReader = new BufferedReader(fileReader);
					// Start: New line char ahs been handled RBT-15913-
					// vivo-ccc:issues in production.
					String line = "";
					String previousLine = "";
					while ((line = bufferedReader.readLine()) != null) {
						if (!(line.contains("MSISDN=")
								|| line.contains("msisdn=") || line
									.contains("SUB_ID"))) {
							previousLine = previousLine + " " + line;
							continue;
						} else {
							if (null != previousLine
									&& !previousLine.equalsIgnoreCase(line)) {
								createNewSMSHistoryContentElement(previousLine,
										subscriberID, smsTransactionSortedMap,
										document, startDate, endDate);
							}
						}
						createNewSMSHistoryContentElement(line, subscriberID,
								smsTransactionSortedMap, document, startDate,
								endDate);
						previousLine =line;
						// End: New line char ahs been handled RBT-15913-
						// vivo-ccc:issues in production.
					}
				}
				catch (FileNotFoundException e)
				{
					logger.error("", e);
				}
				catch (Exception e)
				{
					logger.error("", e);
				}
				finally
				{
					try
					{
						if (fileReader != null)
							fileReader.close();
						if (bufferedReader != null)
							bufferedReader.close();
					}
					catch (Exception e)
					{
						logger.error("", e);
					}
				}
			}
	
			
		}

		if (smsTransactionSortedMap.size() > 0)
		{
			Set<String> keySet = smsTransactionSortedMap.keySet();
			for (String key : keySet)
			{
				List<Element> elementList = smsTransactionSortedMap.get(key);
				for (Element element : elementList)
				{
					contentsElem.appendChild(element);
				}
			}
		}
		return contentsElem;
	}

	public static Element generateScratchCardElement(Document document,
			Scratchcard scratch)
	{
		Element element = document.createElement(SCRATCHCARD);
		if(scratch == null) {
			return element;
		}
		element.setAttribute("SCRATCH_CARD_NUMBER", scratch.getScratchNo());
		element.setAttribute("SUB_CLASS", scratch.getSubClass());
		element.setAttribute("CHARGE_CLASS", scratch.getChargeClass());
		element.setAttribute("START_DATE", scratch.getStartDate().toString());
		element.setAttribute("END_DATE", scratch.getEndDate().toString());	
		element.setAttribute("STATE", scratch.getState());
		element.setAttribute("PRECHARGE", scratch.getPrecharge());
		element.setAttribute("CONTENT_ID", scratch.getContentid());
		element.setAttribute("CONTENT_TYPE", scratch.getContentType());
		return element;

	}

	public static Element generateTransactionHistoryElement(Document document,
			Document transactionDetailsDocument, WebServiceContext task)
	{
		logger.debug("Generating transaction history element. task: " + task);
		Element element = document.createElement(TRANSACTION_HISTORY);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (transactionDetailsDocument != null)
		{
			/* (non-Javadoc)
			 * API: GetTransactions
			 * Format of transaction XML:
			 *	<?xml version="1.0" encoding="UTF-8"?>
			 *	<subscriber msisdn="msisdn">
			 *		<transaction amount="amount" chargetype="chargetype" refund_amt="4.00" contentid="contentid" date="yyyyMMddHHmmss" mode="mode" service="service" subscription_id="subscription_id"/>
			 *		<transaction amount="amount" chargetype="chargetype" refund_amt="4.00" contentid="contentid" date="yyyyMMddHHmmss" mode="mode" service="service" subscription_id="subscription_id"/>
			 *		.
			 *		.
			 *		<transaction amount="amount" chargetype="chargetype" refund_amt="4.00" contentid="contentid" date="yyyyMMddHHmmss" mode="mode" service="service" subscription_id="subscription_id"/>
			 *	</subscriber>
			 */

			NodeList transactionNodeList = transactionDetailsDocument.getElementsByTagName("transaction");
			for (int i = 0; i < transactionNodeList.getLength(); i++)
			{
				Element transactionElem = (Element) transactionNodeList.item(i);

				String service = transactionElem.getAttribute("service");

				logger.debug("Checking RRBT system: "
						+ RBTDeploymentFinder.isRRBTSystem() + ", service: "
						+ service);
				if (RBTDeploymentFinder.isRRBTSystem()) {
					if (null != service && service.endsWith("_RRBT")) {
						service = service.substring(0,
								service.lastIndexOf("_RRBT"));
						logger.debug("Updated service value. " + "service: "
								+ service);
					}
				}

				String chargetype = transactionElem.getAttribute("chargetype");
				String amount = transactionElem.getAttribute("amount");
				String mode = transactionElem.getAttribute("mode");
				String date = transactionElem.getAttribute("date");
                String refundAmount = transactionElem.getAttribute("refund_amt");
                String nextBillingDate = transactionElem.getAttribute("nbd");
                String reason = transactionElem.getAttribute("reason");
                String validity = transactionElem.getAttribute("validity");
                String contentId = null;
                try {
                	contentId = transactionElem.getAttribute("contentid");
                }
                catch(Exception e) {}
                
                boolean isMPPackReq = false;
                if(service!=null && service.contains("SEL")){
                   String str = service.replaceAll("RBT_SEL_", "").trim();
					if (Arrays.asList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
									"SERVICE_CLASSES_FOR_PACKS", "")).contains(str)) {
						isMPPackReq = true;
                   }
                }
                
				String type = null;
				String serviceType = null;
				if (chargetype.equalsIgnoreCase("D"))
				{
					type = "De-Provisioning";
					amount = "0.00";
				}
				else if(chargetype.equalsIgnoreCase("R"))
				{
					type = "Refund";
				}
				else if(chargetype.equalsIgnoreCase("E")) {
					type = "MOSMS-Charging";
				}
				else {
					if (service.contains("GIFT"))
					 	type = "Gift Subscription and/or Tone Charges";
					else if (service.contains("ACT") || service.contains("BASE") || service.contains("PACK"))
						type = "Provisioning Charges";
					else if (service.contains("SEL") && isMPPackReq)
					 	type = "Tone Charges(MP)";
					else if (service.contains("SEL"))
					 	type = "Tone Charges";
					else if (service.contains("EVENT"))
					 	type = "MOSMS";
				}

				if (service != null && service.contains("GIFT"))
					serviceType = "GIFT";
				else if (service != null && (service.contains("ACT") || service.contains("BASE")))
					serviceType = "ACT";
				else if (service != null && service.contains("SEL"))
					serviceType = "SEL";
				else if (service != null && service.contains("EVENT"))
					serviceType = "EVENT";
				else if (service != null && service.contains("PACK"))
					serviceType = "PACK";

				String subType = chargetype;
				if (serviceType != null)
					subType += "_" + serviceType;

				String subscriberID = task.getString(param_subscriberID);
				Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
				RBTText rbtText = CacheManagerUtil.getRbtTextCacheManager()
						.getRBTText("WEBSERVICE", subType,
								(subscriber != null) ? subscriber.language() : null);
				if (rbtText != null && rbtText.getText() != null)
					type = rbtText.getText();
				
				String agentId = null;
				String songId = null;
				String catId = null;
				String songName = null;
				String protocolNumber = null;
				String cosId = null;
				if(contentId != null) {
					
					if (contentId.indexOf("packid:") != -1) {
						cosId = contentId.substring(contentId
								.indexOf("packid:"));
						cosId = cosId.substring("packid:".length(),
								cosId.indexOf(","));
					}
					
					if (contentId.indexOf("contentid=") != -1) {
						songId = contentId.substring(contentId
								.indexOf("contentid="));
						songId = songId.substring("contentid=".length(),
								songId.indexOf(","));
						String language = "eng";
						if (subscriber != null)
							language = subscriber.language();
						Clip clip = rbtCacheManager.getClip(songId, language);
						if (clip != null)
							songName = clip.getClipName();
					}
					if(contentId.indexOf("AGENT_ID=") != -1) {
						agentId = contentId.substring(contentId.indexOf("AGENT_ID="));
						agentId = agentId.substring("AGENT_ID=".length());						
					}
					else if(contentId.indexOf("agentId;") != -1) {
						agentId = contentId.substring(contentId.indexOf("agentId;"));
						agentId = agentId.substring("agentId;".length());
					}
					
					//Get the protocol number from Transaction History
					String tempContentId = contentId;
					if(chargetype.equalsIgnoreCase("D")) {
						if(contentId.indexOf("DCT;") != -1){
							tempContentId = tempContentId.substring(contentId.indexOf("DCT;"));
						}
						else{
							tempContentId = "";
						}
							
					}
					if(tempContentId.indexOf("protocolnumber=") != -1){
						protocolNumber = tempContentId.substring(tempContentId
								.indexOf("protocolnumber=")+"protocolnumber=".length());						
					}
					else if(tempContentId.indexOf("protocolnumber;") != -1) {
						protocolNumber = tempContentId.substring(tempContentId
								.indexOf("protocolnumber;")+"protocolnumber;".length());
					}
					if(protocolNumber != null) {
						if(protocolNumber.indexOf("/") != -1) {
							protocolNumber = protocolNumber.substring(0,
									protocolNumber.indexOf("/"));
						}
						else if(protocolNumber.indexOf(",") != -1) {
							protocolNumber = protocolNumber.substring(0,
									protocolNumber.indexOf(","));
						}
						else if(protocolNumber.indexOf(";") != -1) {
							protocolNumber = protocolNumber.substring(0,
									protocolNumber.indexOf(";"));
						}
						else if(protocolNumber.indexOf("|") != -1) {
							protocolNumber = protocolNumber.substring(0,
									protocolNumber.indexOf("|"));
						}
						else {
							protocolNumber = protocolNumber.substring(0);			
						}
					}
					
					
					if(chargetype.equalsIgnoreCase("D")) {
						agentId = null;
						int startIndex = contentId.indexOf("DCT;");
						if(startIndex != -1 && contentId.indexOf("AGENT_ID=", startIndex) != -1) {
							agentId = contentId.substring(contentId.indexOf("AGENT_ID=", startIndex));
							agentId = agentId.substring("AGENT_ID=".length());						
						}
						else if(startIndex != -1 && contentId.indexOf("agentId;", startIndex) != -1) {
							agentId = contentId.substring(contentId.indexOf("agentId;", startIndex));
							agentId = agentId.substring("agentId;".length());
						}
					}
					
					if(agentId != null ) {
						if(agentId.indexOf(";") != -1) {
							agentId = agentId.substring(0, agentId.indexOf(";"));
						}
						else if(agentId.indexOf(",") != -1) {
							agentId = agentId.substring(0, agentId.indexOf(","));
						}
						else if(agentId.indexOf("|") != -1) {
							agentId = agentId.substring(0, agentId.indexOf("|"));
						}
					}
					
					if(cosId != null ) {
						if(cosId.indexOf(";") != -1) {
							cosId = cosId.substring(0, cosId.indexOf(";"));
						}
						else if(cosId.indexOf(",") != -1) {
							cosId = cosId.substring(0, cosId.indexOf(","));
						}
						else if(cosId.indexOf("|") != -1) {
							cosId = cosId.substring(0, cosId.indexOf("|"));
						}
					}
					if (contentId.indexOf("catid=") != -1) {
						catId = contentId.substring(contentId
								.indexOf("catid="));
						catId = catId.substring("catid=".length(),
								catId.indexOf(","));
					}
				}

				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(TYPE, type);
				contentElem.setAttribute(AMOUNT, amount);
				contentElem.setAttribute(MODE, mode);
				contentElem.setAttribute(DATE, date);
				contentElem.setAttribute(REF_AMOUNT, refundAmount);
				contentElem.setAttribute(VALIDITY, validity);
				contentElem.setAttribute(PROTOCOL_NO,protocolNumber);
				String chargeClass = null;
			  if(!serviceType.equals("EVENT")) {
				if(serviceType.equals("GIFT")) {
				  contentElem.setAttribute(SERVICEKEY, (service.split("_GIFT")[0]).split("SEL_")[1]);
				  chargeClass = (service.split("_GIFT")[0]).split("SEL_")[1];
				} else if(service.equals("BASE"))  {
					 contentElem.setAttribute(SERVICEKEY, service.split("BASE"+"_")[1]);
				} else {
				  contentElem.setAttribute(SERVICEKEY, service.split(serviceType+"_")[1]);
				  chargeClass = service.split(serviceType+"_")[1];
				}
			  }
				if(agentId != null) {
					contentElem.setAttribute(AGENT_ID, agentId);
				}
				if(cosId != null){
					contentElem.setAttribute(COS_ID, cosId);
				}
				if(catId != null) {
					contentElem.setAttribute(CATEGORY_ID, catId);
				}
				if(songId != null) {
					contentElem.setAttribute(CLIP_ID, songId);
				}
				if(songName != null) {
					contentElem.setAttribute(SONG_NAME, songName);
				}
				if(nextBillingDate != null && !nextBillingDate.contains("0000000000")) {
					contentElem.setAttribute(NEXT_CHARGING_DATE, nextBillingDate);
				}
				if (Arrays.asList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
						"SERVICE_CLASSES_FOR_CRICKET", "").split(",")).contains(chargeClass) && reason != null) {
					contentElem.setAttribute(REASON, reason+"_"+chargetype+"_"+serviceType+"_CRICKET");
				}else if(reason != null) {
					contentElem.setAttribute(REASON, reason+"_"+chargetype+"_"+serviceType);
				}
				
				contentsElem.appendChild(contentElem);
			}
		}
		logger.debug("Returning element: " + element);
		return element;
	}

	public static Element generateCallDetailsElement(Document document, WebServiceContext task, WebServiceSubscriber webServiceSubscriber)
	{
		Element element = document.createElement(CALL_DETAILS);

		Element languagesElem = generateLanguagesElement(document, webServiceSubscriber);
		element.appendChild(languagesElem);

		Element mmRequestElem = generateMultiModelRequestElement(document, task, webServiceSubscriber);
		if (mmRequestElem != null)
			element.appendChild(mmRequestElem);

		if (mmRequestElem == null)
		{
			Element mmConetentElem = generateContentCallBackElement(document, task, webServiceSubscriber);
			if (mmConetentElem != null)
				element.appendChild(mmConetentElem);
		}

		Element pickOfTheDayElem = generatePickOfTheDayElement(document, task, webServiceSubscriber);
		if (pickOfTheDayElem != null)
			element.appendChild(pickOfTheDayElem);

		Element hotSongElem = generateHotSongElement(document, task, webServiceSubscriber.getCircleID());
		if (hotSongElem != null)
			element.appendChild(hotSongElem);

		return element;
	}

	public static Element generateLanguagesElement(Document document,
			WebServiceSubscriber webServiceSubscriber)
	{
		SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(webServiceSubscriber.getCircleID());
		
		if(sitePrefix == null && webServiceSubscriber.getOperatorName() != null) {
			sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(webServiceSubscriber.getOperatorName());
		}
		
		if(sitePrefix == null) {
			logger.error("Siteprefix cannot be null, please check site prefix table");
		}
		
		String languages = sitePrefix.getSupportedLanguage();
		Element languagesElem = document.createElement(LANGUAGES);

		if (languages != null)
			languagesElem.setAttribute(LANGUAGES, languages);

		String subscriberStatus = webServiceSubscriber.getStatus();
		String askLanguage = NO;
		if (subscriberStatus.equalsIgnoreCase(NEW_USER)
				|| subscriberStatus.equalsIgnoreCase(DEACTIVE)
				|| webServiceSubscriber.getLanguage() == null)
			askLanguage = YES;
		languagesElem.setAttribute(ASK_LANGUAGE, askLanguage);

		return languagesElem;
	}

	public static Element generateMultiModelRequestElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber)
	{
		if (!task.containsKey(param_mmContext))
			return null;

		Element element = null;

		String[] mmContext = task.getString(param_mmContext).split("\\|");

		String subscriberID = task.getString(param_subscriberID);
		String circleID = webServiceSubscriber.getCircleID();
		String browsingLanguage = task.getString(param_browsingLanguage);

		if (mmContext[0].equalsIgnoreCase("RBT_CATEGORY"))
		{
			int categoryID = Integer.parseInt(mmContext[1]);
			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);

			element = document.createElement(MM_CONTENT);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			if (category != null)
			{
				Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, category, task);
				contentsElem.appendChild(contentElem);
			}
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_CLIP"))
		{
			int categoryID = Integer.parseInt(mmContext[1]);
			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);

			int clipID = Integer.parseInt(mmContext[2]);
			Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);

			element = document.createElement(MM_CONTENT);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			if (category != null && clip != null)
			{
				Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getClipContentElement(document, category, clip, task);
				contentsElem.appendChild(contentElem);

				Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(categoryID));
			}
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_CATEGORIES"))
		{
			element = document.createElement(MM_CONTENT);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			if (task.containsKey(param_calledNo))
			{
				int categoryID = Integer.parseInt(mmContext[1]);
				Category parentCategory = rbtCacheManager.getCategory(categoryID, browsingLanguage);

				String subscriberStatus = webServiceSubscriber.getStatus();
				String statusString = "SUB"; 
				if (subscriberStatus.equalsIgnoreCase(NEW_USER) || subscriberStatus.equalsIgnoreCase(DEACTIVE))
					statusString = "UNSUB";

				Date curDate = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				String dateStr = dateFormat.format(curDate);
				String paramType = "MULTIMODAL_" + circleID.toUpperCase();
				String language = task.getString(param_language);
				if (language == null)
					language = webServiceSubscriber.getLanguage();
				//RBT-12490
				if(language == null){
					Parameters lang = parametersCacheManager.getParameter("ALL", "DEFAULT_LANGUAGE", "eng");
					if(lang!=null)
						language=lang.getValue();
				}
				if (language != null)
					language = language.toUpperCase();
				
				String paramName = task.getString(param_calledNo) + "_" + language + "_" + statusString + "_" + dateStr;
				Parameters multimodalParam = parametersCacheManager.getParameter(paramType, paramName, null);
				if (multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0)
				{
					paramName = task.getString(param_calledNo) + "_" + language + "_" + statusString;
					multimodalParam = parametersCacheManager.getParameter(paramType, paramName, null);
				}
				if (multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0)
				{
					paramName = task.getString(param_calledNo);
					multimodalParam = parametersCacheManager.getParameter(paramType, paramName, null);
				}
				
				//RBT-12630
				String paramTypeAll = "MULTIMODAL_ALL";
				if(multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0) 
				{
					paramName = task.getString(param_calledNo) + "_" + language + "_" + statusString + "_" + dateStr;
					multimodalParam = parametersCacheManager.getParameter(paramTypeAll, paramName, null);
				}
				if (multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0)
				{
					paramName = task.getString(param_calledNo) + "_" + language + "_" + statusString;
					multimodalParam = parametersCacheManager.getParameter(paramTypeAll, paramName, null);
				}
				if (multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0)
				{
					paramName = task.getString(param_calledNo);
					multimodalParam = parametersCacheManager.getParameter(paramTypeAll, paramName, null);
				}
				

				if (multimodalParam != null && parentCategory != null)
				{
					String[] categoryPromoIDs = multimodalParam.getValue().trim().split(",");

					int noOfContents = 0;
					for (String categoryPromoID : categoryPromoIDs)
					{
						Category category = rbtCacheManager.getCategoryByPromoId(categoryPromoID, browsingLanguage);

						if (category != null && category.getCategoryEndTime().after(curDate))
						{
							Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, parentCategory, category, task);
							contentsElem.appendChild(contentElem);

							Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(categoryID));

							noOfContents++;
						}
					}

					contentsElem.setAttribute(NO_OF_CONTENTS, String.valueOf(noOfContents));
				}
			}
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_CLIPS"))
		{
			element = document.createElement(MM_CONTENT);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			if (task.containsKey(param_calledNo))
			{
				int categoryID = Integer.parseInt(mmContext[1]);
				Category parentCategory = rbtCacheManager.getCategory(categoryID, browsingLanguage);

				String subscriberStatus = webServiceSubscriber.getStatus();
				String statusString = "SUB"; 
				if (subscriberStatus.equalsIgnoreCase(NEW_USER) || subscriberStatus.equalsIgnoreCase(DEACTIVE))
					statusString = "UNSUB";

				Date curDate = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				String dateStr = dateFormat.format(curDate);
				String paramType = "MULTIMODAL_" + circleID.toUpperCase();
				String language = task.getString(param_language);
				if (language == null)
					language = webServiceSubscriber.getLanguage();
				//RBT-12490
				if(language == null){
					Parameters lang = parametersCacheManager.getParameter("ALL", "DEFAULT_LANGUAGE", "eng");
					if(lang!=null)
						language=lang.getValue();
				}
				if (language != null)
					language = language.toUpperCase();

				String paramName = task.getString(param_calledNo) + "_" + language + "_" + statusString + "_" + dateStr;
				Parameters multimodalParam = parametersCacheManager.getParameter(paramType, paramName, null);
				if (multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0)
				{
					paramName = task.getString(param_calledNo) + "_" + language + "_" + statusString;
					multimodalParam = parametersCacheManager.getParameter(paramType, paramName, null);
				}
				if (multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0)
				{
					paramName = task.getString(param_calledNo);
					multimodalParam = parametersCacheManager.getParameter(paramType, paramName, null);
				}
				
				//RBT-12490
				String paramTypeAll = "MULTIMODAL_ALL";
				if(multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0) 
				{
					paramName = task.getString(param_calledNo) + "_" + language + "_" + statusString + "_" + dateStr;
					multimodalParam = parametersCacheManager.getParameter(paramTypeAll, paramName, null);
				}
				if (multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0)
				{
					paramName = task.getString(param_calledNo) + "_" + language + "_" + statusString;
					multimodalParam = parametersCacheManager.getParameter(paramTypeAll, paramName, null);
				}
				if (multimodalParam == null || multimodalParam.getValue() == null || multimodalParam.getValue().trim().length() == 0)
				{
					paramName = task.getString(param_calledNo);
					multimodalParam = parametersCacheManager.getParameter(paramTypeAll, paramName, null);
				}

				if (multimodalParam != null && parentCategory != null)
				{
					String[] clipPromoIDs = multimodalParam.getValue().trim().split(",");

					int noOfContents = 0;
					for (String clipPromoID : clipPromoIDs)
					{
						Clip clip = rbtCacheManager.getClipByPromoId(clipPromoID, browsingLanguage);

						if (clip != null && clip.getClipEndTime().after(curDate))
						{
							Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getClipContentElement(document, parentCategory, clip, task);
							contentsElem.appendChild(contentElem);

							Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(categoryID));

							noOfContents++;
						}
					}

					contentsElem.setAttribute(NO_OF_CONTENTS, String.valueOf(noOfContents));
				}
			}
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_CRICKET"))
		{
			element = document.createElement(CRICKET_DETAILS);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			Element contentElem = document.createElement(CONTENT);
			contentElem.setAttribute(ID, "DEFAULT");
			contentsElem.appendChild(contentElem);

			Parameters categoryParm = parametersCacheManager.getParameter(iRBTConstant.COMMON, "CRICKET_CATEGORY", "10");
			int categoryID = Integer.parseInt(categoryParm.getValue());

			Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(categoryID));

			String isCicketUser = NO;
			SubscriberStatus subscriberStatus = RBTDBManager.getInstance().getActiveSubscriberRecord(subscriberID, null, 90, 0, 2359);
			if (subscriberStatus != null)
				isCicketUser = YES;
			element.setAttribute(IS_CRICKET_USER, isCicketUser);
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_PROFILES"))
		{
			int categoryID = 99;
			if (mmContext.length > 1)
				categoryID = Integer.parseInt(mmContext[1]);

			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);

			element = document.createElement(PROFILES);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			if (category != null)
			{
				Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, category, task);
				contentElem.setAttribute(TYPE, CATEGORY_PROFILE_CLIPS);
				contentsElem.appendChild(contentElem);
			}
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_UGC_CLIPS"))
		{
			element = document.createElement(MM_CONTENT);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			Element contentElem = document.createElement(CONTENT);
			element.setAttribute(ID, mmContext[1]);
			element.setAttribute(NAME, "RBT_UGC_CLIPS");
			element.setAttribute(TYPE, UGC_CLIPS);
			contentsElem.appendChild(contentElem);
		}

		return element;
	}

	public static Element generateContentCallBackElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber)
	{
		Element element = document.createElement(MM_CONTENT);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		String promoNo = null;

		String calledNo = task.getString(param_calledNo);
		Parameters baseNumbersParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "BASENUMBERS", null);
		if (baseNumbersParam != null && calledNo != null)
		{
			String ivrNo = null;

			List<String> baseNumbersList = Arrays.asList(baseNumbersParam.getValue().trim().split(","));

			if (baseNumbersList.contains(calledNo))
				return null;

			for (String baseNumber : baseNumbersList)
			{
				if (calledNo.startsWith(baseNumber))
				{
					ivrNo = baseNumber;
					promoNo = calledNo.substring(calledNo.indexOf(ivrNo) + ivrNo.length());
					break;
				}
			}

			if (promoNo != null)
			{
				Date currentDate = new Date();
				String browsingLanguage = task.getString(param_browsingLanguage);

				Clip clip = rbtCacheManager.getClipByPromoId(promoNo, browsingLanguage);
				if (clip != null && clip.getClipEndTime().after(currentDate))
				{
					int categoryID = -1;
					Parameters callbackCategoriesParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "CALLBACK_CATEGORIES", null);
					if (callbackCategoriesParam != null)
					{
						String[] callbackCategories = callbackCategoriesParam.getValue().trim().split(",");
						for (String callbackCategory : callbackCategories)
						{
							String[] values = callbackCategory.split(":");
							if (values[0].equals(ivrNo))
							{
								categoryID = Integer.parseInt(values[1]);
								break;
							}
						}
					}
					Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);

					Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getClipContentElement(document, category, clip, task);
					contentsElem.appendChild(contentElem);

					Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(categoryID));

					return element;
				}

				Category category = rbtCacheManager.getCategoryByMmNumber(promoNo, browsingLanguage);
				if (category != null && category.getCategoryEndTime().after(currentDate))
				{
					Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, category, task);
					contentsElem.appendChild(contentElem);

					return element;
				}
			}
		}

		return null;
	}

	public static Element generatePickOfTheDayElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber)
	{
		String circleID = webServiceSubscriber.getCircleID();
		
		PickOfTheDay[] pickOfTheDays = DataUtils.getPickOfTheDay(task, circleID, 'b', null);
		if (pickOfTheDays == null || pickOfTheDays.length == 0)
			return null;
		
		Element element = document.createElement(PICK_OF_THE_DAY);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);
		
		String browsingLanguage = task.getString(param_browsingLanguage);
		for (PickOfTheDay pickOfTheDay : pickOfTheDays)
		{
			if (pickOfTheDay != null)
			{
				Clip clip = rbtCacheManager.getClip(pickOfTheDay.clipID(), browsingLanguage);
				Category category = rbtCacheManager.getCategory(pickOfTheDay.categoryID(), browsingLanguage);

				if (clip != null && category != null)
				{

					Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getClipContentElement(document, category, clip, task);
					Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(pickOfTheDay.categoryID()));
					contentsElem.appendChild(contentElem);
					Utility.addPropertyElement(document, contentElem, PLAY_DATE, DATA, pickOfTheDay.playDate());
					Utility.addPropertyElement(document, contentElem, CIRCLE_ID, DATA, pickOfTheDay.circleID());
					Utility.addPropertyElement(document, contentElem, PROFILE, DATA, pickOfTheDay.profile());
					Utility.addPropertyElement(document, contentElem, LANGUAGE, DATA, pickOfTheDay.language());
					contentsElem.appendChild(contentElem);
				}
			}
		}

		return element;
	}

	public static Element generateHotSongElement(Document document, WebServiceContext task,
			String circleID)
	{
		PickOfTheDay[] hotSongs = DataUtils.getPickOfTheDay(task, circleID, 'b', "HOT_SONG");
		PickOfTheDay hotSong = null;
		if (hotSongs.length > 0)
			hotSong = hotSongs[0];
		
		if (hotSong != null)
		{
			Element element = document.createElement(HOT_SONG);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);
			String browsingLanguage = task.getString(param_browsingLanguage);

			String format= null;
			if(task != null)
				 format = task.getString(param_format);
			
			Category category = rbtCacheManager.getCategory(hotSong.categoryID(), browsingLanguage);
			if (hotSong.clipID() == 0)
			{
				if (category != null)
				{
					Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, category, task);
					Utility.addPropertyElement(document, contentElem, CATEGORY_PREVIEW_FILE, PROMPT, Utility.getPromptName(category.getCategoryPreviewWavFile(),format));

					contentsElem.appendChild(contentElem);
					element.setAttribute(TYPE, CATEGORY);
					return element;
				}
			}
			else
			{
				Clip songOfTheDay = rbtCacheManager.getClip(hotSong.clipID(), browsingLanguage);
				if (songOfTheDay != null && category != null)
				{
					Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getClipContentElement(document, category, songOfTheDay, task);
					contentsElem.appendChild(contentElem);
					element.setAttribute(TYPE, CLIP);
					return element;
				}
			}
		}

		return null;
	}

	public static Element generateCosElement(Document document, CosDetails cos)
	{
		Element element = document.createElement(COS);
		element.setAttribute(ID, cos.getCosId());
		element.setAttribute(IS_DEFAULT, cos.isDefaultCos() ? YES : NO);
		element.setAttribute(FREE_SONGS, String.valueOf(cos.getFreeSongs()));
		element.setAttribute(FREE_MUSICBOXES, String.valueOf(cos.getFreeMusicboxes()));

		String actCOSPrompt = cos.getActivationPrompt();
		String selCOSPrompt = cos.getSelectionPrompt();

		String activationIntroPrompt = null;
		String activationPrompt = null;
		if (actCOSPrompt != null)
		{
			activationIntroPrompt = actCOSPrompt;
			if (actCOSPrompt.indexOf("|") >= 0)
			{
				activationIntroPrompt = actCOSPrompt.substring(0, actCOSPrompt.indexOf("|"));
				activationPrompt = actCOSPrompt.substring(actCOSPrompt.indexOf("|") + 1);
			}
		}

		String clipDownloadPrompt = null;
		String musicboxDownloadPrompt = null;
		if (selCOSPrompt != null)
		{
			if (selCOSPrompt.indexOf("|") >= 0)
			{
				clipDownloadPrompt = selCOSPrompt.substring(0, selCOSPrompt.indexOf("|"));
				musicboxDownloadPrompt = selCOSPrompt.substring(selCOSPrompt.indexOf("|") + 1);
			}
			else
			{
				clipDownloadPrompt = selCOSPrompt;
				musicboxDownloadPrompt = selCOSPrompt;
			}
		}

		if (activationIntroPrompt != null && !activationIntroPrompt.equalsIgnoreCase("null"))
			element.setAttribute(ACTIVATION_INTRO_PROMPT, Utility.getPromptName(activationIntroPrompt));
		if (activationPrompt != null && !activationPrompt.equalsIgnoreCase("null"))
			element.setAttribute(ACTIVATION_PROMPT, Utility.getPromptName(activationPrompt));
		if (clipDownloadPrompt != null && !clipDownloadPrompt.equalsIgnoreCase("null"))
			element.setAttribute(CLIP_DOWNLOAD_PROMPT, Utility.getPromptName(clipDownloadPrompt));
		if (musicboxDownloadPrompt != null && !musicboxDownloadPrompt.equalsIgnoreCase("null"))
			element.setAttribute(MUSICBOX_DOWNLOAD_PROMPT, Utility.getPromptName(musicboxDownloadPrompt));

		return element;
	}

	public static Element generateExitSMSElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber)
	{
		Element element = document.createElement(SMS);
		String sendSMS = NO;

		String exitSMS = CacheManagerUtil.getSmsTextCacheManager().getSmsText("EXIT_SMS", "EXIT_SMS", webServiceSubscriber.getLanguage());
		if (exitSMS != null)
		{
			sendSMS = YES;

			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			if (exitSMS.indexOf("%SONGNAME%") != 0 || exitSMS.indexOf("%SONGCODE%") != 0)
			{
				String circleID = webServiceSubscriber.getCircleID();
				char isPrepaid = webServiceSubscriber.isPrepaid() ? 'y' : 'n';
				
				PickOfTheDay[] pickOfTheDays = DataUtils.getPickOfTheDay(task, circleID, isPrepaid, "EXIT_SMS");
				PickOfTheDay pickOfTheDay = null;
				if (pickOfTheDays.length > 0)
					pickOfTheDay = pickOfTheDays[0];
				
				if (pickOfTheDay != null)
				{
					Clip clip = rbtCacheManager.getClip(pickOfTheDay.clipID(), webServiceSubscriber.getLanguage());
					exitSMS = exitSMS.replaceAll("%SONGNAME%", clip.getClipName());
					exitSMS = exitSMS.replaceAll("%SONGCODE%", clip.getClipPromoId());
				}
			}

			element.setAttribute(SENDER, rbtDBManager.getSMSSenderID());
			element.setAttribute(RECEIVER, webServiceSubscriber.getSubscriberID());
			element.setAttribute(SMS_TEXT, exitSMS);
		}

		element.setAttribute(SEND_SMS, sendSMS);

		return element;
	}

	public static Element generateSubscriberPromoElement(Document document, SubscriberPromo subscriberPromo)
	{
		if (subscriberPromo == null)
			return null;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		Element element = document.createElement(SUBSCRIBER_PROMO);
		element.setAttribute(SUBSCRIBER_ID, subscriberPromo.subID());
		element.setAttribute(IS_PREPAID, (subscriberPromo.isPrepaid() ? YES : NO));
		element.setAttribute(FREE_DAYS, String.valueOf(subscriberPromo.freedays()));
		element.setAttribute(ACTIVATED_BY, subscriberPromo.activatedBy());
		element.setAttribute(SUBSCRIPTION_TYPE, subscriberPromo.subType());

		if (subscriberPromo.startDate() != null)
			element.setAttribute(START_DATE, dateFormat.format(subscriberPromo.startDate()));
		if (subscriberPromo.endDate() != null)
			element.setAttribute(END_DATE, dateFormat.format(subscriberPromo.endDate()));

		return element;
	}

	public static Element generateParametersElement(Document document, Parameters[] parameters)
	{
		Element element = document.createElement(PARAMETERS);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (parameters != null)
		{
			HashMap<String, List<Parameters>> parametersMap = new HashMap<String, List<Parameters>>();
			for (Parameters parameter : parameters)
			{
				List<Parameters> list = null;
				if (parametersMap.containsKey(parameter.getType()))
					list = parametersMap.get(parameter.getType());
				else
				{
					list = new ArrayList<Parameters>();
					parametersMap.put(parameter.getType(), list);
				}

				list.add(parameter);
			}

			Set<String> keySet = parametersMap.keySet();
			for (String type : keySet)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(TYPE, type);
				contentsElem.appendChild(contentElem);

				List<Parameters> list = parametersMap.get(type);
				for (Parameters parameter : list)
				{
					Utility.addPropertyElement(document, contentElem, parameter.getParam(), null, parameter.getValue());
				}
			}
		}

		return element;
	}

	public static Element generateSubscriptionClassesElement(Document document, SubscriptionClass[] subscriptionClasses)
	{
		Element element = document.createElement(SUBSCRIPTION_CLASSES);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (subscriptionClasses != null)
		{
			for (SubscriptionClass subscriptionClass : subscriptionClasses)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, subscriptionClass.getSubscriptionClass());
				contentElem.setAttribute(AMOUNT, subscriptionClass.getSubscriptionAmount());
				contentElem.setAttribute(PERIOD, subscriptionClass.getSubscriptionPeriod());
				contentElem.setAttribute(RENEWAL_AMOUNT, subscriptionClass.getRenewalAmount());
				contentElem.setAttribute(RENEWAL_PERIOD, subscriptionClass.getRenewalPeriod());
				contentElem.setAttribute(SHOW_ON_GUI, (subscriptionClass.getShowOnGui().equalsIgnoreCase("y") ? YES : NO));
				contentsElem.appendChild(contentElem);
			}
		}

		return element;
	}

	public static Element generateChargeClassesElement(Document document, ChargeClass[] chargeClasses)
	{
		Element element = document.createElement(CHARGE_CLASSES);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (chargeClasses != null)
		{
			for (ChargeClass chargeClass : chargeClasses)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, chargeClass.getChargeClass());
				contentElem.setAttribute(AMOUNT, chargeClass.getAmount());
				contentElem.setAttribute(PERIOD, chargeClass.getSelectionPeriod());
				contentElem.setAttribute(RENEWAL_AMOUNT, chargeClass.getRenewalAmount());
				contentElem.setAttribute(RENEWAL_PERIOD, chargeClass.getRenewalPeriod());
				contentElem.setAttribute(SHOW_ON_GUI, (chargeClass.getShowonGui().equalsIgnoreCase("y") ? YES : NO));

				if (chargeClass.getOperatorCode1() != null)
					contentElem.setAttribute(OPERATOR_CODE_1, chargeClass.getOperatorCode1());

				contentsElem.appendChild(contentElem);
			}
		}

		return element;
	}

	public static Element generateSMSTextsElement(Document document, BulkPromoSMS[] bulkPromoSMSes)
	{
		Element element = document.createElement(SMS_TEXTS);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (bulkPromoSMSes != null)
		{
			TreeMap<String, List<BulkPromoSMS>> smsTextsMap = new TreeMap<String, List<BulkPromoSMS>>();
			for (BulkPromoSMS bulkPromoSMS : bulkPromoSMSes)
			{
				List<BulkPromoSMS> list = null;
				if (smsTextsMap.containsKey(bulkPromoSMS.getBulkpromoID()))
					list = smsTextsMap.get(bulkPromoSMS.getBulkpromoID());
				else
				{
					list = new ArrayList<BulkPromoSMS>();
					smsTextsMap.put(bulkPromoSMS.getBulkpromoID(), list);
				}

				list.add(bulkPromoSMS);
			}

			Set<String> keySet = smsTextsMap.keySet();
			for (String type : keySet)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(TYPE, type);
				contentsElem.appendChild(contentElem);

				List<BulkPromoSMS> list = smsTextsMap.get(type);
				for (BulkPromoSMS bulkPromoSMS : list)
				{
					Utility.addPropertyElement(document, contentElem, bulkPromoSMS.getSmsDate(), null, bulkPromoSMS.getSmsText());
				}
			}
		}

		return element;
	}

	public static Element generatePickOfTheDaySElement(Document document,
			WebServiceContext task, PickOfTheDay[] pickOfTheDays)
	{
		Element element = document.createElement(PICK_OF_THE_DAYS);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (pickOfTheDays != null)
		{
			String browsingLanguage = task.getString(param_browsingLanguage);
			for (PickOfTheDay pickOfTheDay : pickOfTheDays)
			{
				Clip clip = rbtCacheManager.getClip(pickOfTheDay.clipID(), browsingLanguage);
				Category category = rbtCacheManager.getCategory(pickOfTheDay.categoryID(), browsingLanguage);

				if (clip != null && category != null)
				{
					Element contentElem = document.createElement(CONTENT);
					contentElem.setAttribute(ID, String.valueOf(clip.getClipId()));
					contentElem.setAttribute(NAME, clip.getClipName());
					contentsElem.appendChild(contentElem);

					Utility.addPropertyElement(document, contentElem, PLAY_DATE, DATA, pickOfTheDay.playDate());
					Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(pickOfTheDay.categoryID()));
					Utility.addPropertyElement(document, contentElem, CIRCLE_ID, DATA, pickOfTheDay.circleID());

					String userType = BOTH;
					if (pickOfTheDay.prepaidYes() == 'y')
						userType = PREPAID;
					else if (pickOfTheDay.prepaidYes() == 'n')
						userType = POSTPAID;

					Utility.addPropertyElement(document, contentElem, USER_TYPE, DATA, userType);
					Utility.addPropertyElement(document, contentElem, PROFILE, DATA, pickOfTheDay.profile());
					Utility.addPropertyElement(document, contentElem, LANGUAGE, DATA, pickOfTheDay.language());

					contentsElem.appendChild(contentElem);
				}
			}
		}

		return element;
	}

	public static Element generateRBTLoginUserElement(Document document, RBTLoginUser rbtLoginUser) {
		return generateRBTLoginUserElement(document, rbtLoginUser, null);
	}
	public static Element generateRBTLoginUserElement(Document document, RBTLoginUser rbtLoginUser, Boolean newUser)
	{
		if (rbtLoginUser == null)
			return null;

		Element element = document.createElement(RBT_LOGIN_USER);
		element.setAttribute(USER_ID, rbtLoginUser.userID());
		element.setAttribute(PASSWORD, rbtLoginUser.password());
		element.setAttribute(SUBSCRIBER_ID, rbtLoginUser.subscriberID());
		element.setAttribute(TYPE, rbtLoginUser.type());
		if (newUser != null) {
			element.setAttribute(NEW_USER, String.valueOf(newUser));
		}
		
		String passwordExpiryDays = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "LOGIN_USER_PASSWORD_EXPIRY_DAYS", null);		
		if(passwordExpiryDays != null) {
			int priorNotificationDays = 0;
			int ipasswordExpiryDays = 0;
			String[] token = passwordExpiryDays.split("\\,");			
			if(token.length > 1) {
				try {
					ipasswordExpiryDays = Integer.parseInt(token[0]);
					priorNotificationDays = Integer.parseInt(token[1]);
				}
				catch(NumberFormatException nfe) {}
			}
			Date updateTime = rbtLoginUser.updateTime();
			int passwordUpdateDays = (int)( (System.currentTimeMillis() - updateTime.getTime()) / (1000 * 60 * 60 * 24) );
			if(passwordUpdateDays >=  (ipasswordExpiryDays - priorNotificationDays)) {
				element.setAttribute(PASSWORD_DAYS_LEFT, (ipasswordExpiryDays - passwordUpdateDays) + "");
			}			
			if((passwordUpdateDays - ipasswordExpiryDays) >= 0) {
				element.setAttribute(PASSWORD_EXPIRED, "TRUE");
			}
		}

		HashMap<String, String> userInfo = rbtLoginUser.userInfo();
		if (userInfo != null)
		{
			Set<String> keySet = userInfo.keySet();
			for (String key : keySet)
				element.setAttribute(key, userInfo.get(key));
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		element.setAttribute(CREATION_TIME, dateFormat.format(rbtLoginUser.creationTime()));

		return element;
	}

	public static Element generateSitesElement(Document document, SitePrefix[] sitePrefixes)
	{
		Element element = document.createElement(SITES);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (sitePrefixes != null)
		{
			for (SitePrefix prefix : sitePrefixes)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, prefix.getCircleID());
				contentElem.setAttribute(NAME, prefix.getSiteName());
				contentsElem.appendChild(contentElem);

				Utility.addPropertyElement(document, contentElem, SITE_PREFIX, null, prefix.getSitePrefix());
				Utility.addPropertyElement(document, contentElem, SITE_URL, null, prefix.getSiteUrl());
				Utility.addPropertyElement(document, contentElem, ACCESS_ALLOWED, null, (prefix.getAccessAllowed().equalsIgnoreCase("y") ? YES : NO));
				Utility.addPropertyElement(document, contentElem, SUPPORTED_LANGUAGES, null, prefix.getSupportedLanguage());
				Utility.addPropertyElement(document, contentElem, PLAYER_URL, null, prefix.getPlayerUrl());

				String playUncharged = prefix.getPlayerUncharged();
				String playUnchargedFor = BOTH;
				if (playUncharged.equalsIgnoreCase("p"))
					playUnchargedFor = PREPAID;
				else if (playUncharged.equalsIgnoreCase("b"))
					playUnchargedFor = POSTPAID;

				Utility.addPropertyElement(document, contentElem, PLAY_UNCHARGED_FOR, null, playUnchargedFor);
			}
		}

		return element;
	}

	public static Element generateChargeSMSElement(Document document, ChargeSMS[] chargeSmses)
	{
		Element element = document.createElement(CHARGE_SMS);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (chargeSmses != null)
		{
			for (ChargeSMS chargeSms : chargeSmses)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, chargeSms.getChargeClass());
				contentElem.setAttribute(TYPE, chargeSms.getClassType());
				contentsElem.appendChild(contentElem);

				Utility.addPropertyElement(document, contentElem, PREPAID_SUCCESS_SMS, null, chargeSms.getPrepaidSuccess());
				Utility.addPropertyElement(document, contentElem, PREPAID_FAILURE_SMS, null, chargeSms.getPrepaidFailure());
				Utility.addPropertyElement(document, contentElem, POSTPAID_SUCCESS_SMS, null, chargeSms.getPostpaidSuccess());
				Utility.addPropertyElement(document, contentElem, POSTPAID_FAILURE_SMS, null, chargeSms.getPostpaidFailure());
				Utility.addPropertyElement(document, contentElem, PREPAID_NEF_SUCCESS_SMS, null, chargeSms.getPrepaidNEFSuccess());
				Utility.addPropertyElement(document, contentElem, PREPAID_RENEWAL_SUCCESS_SMS, null, chargeSms.getPrepaidRenewalSuccess());
				Utility.addPropertyElement(document, contentElem, PREPAID_RENEWAL_FAILURE_SMS, null, chargeSms.getPrepaidRenewalFailure());
				Utility.addPropertyElement(document, contentElem, POSTPAID_RENEWAL_SUCCESS_SMS, null, chargeSms.getPostpaidRenewalSuccess());
				Utility.addPropertyElement(document, contentElem, POSTPAID_RENEWAL_FAILURE_SMS, null, chargeSms.getPostpaidRenewalFailure());
			}
		}

		return element;
	}

	public static Element generateCosDetailsElement(Document document, CosDetails[] cosDetails)
	{
		Element element = document.createElement(COS_DETAILS);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		if (cosDetails != null)
		{
			for (CosDetails cosDetail : cosDetails)
			{
				String userType = BOTH;
				if (cosDetail.getPrepaidYes().equalsIgnoreCase("y"))
					userType = PREPAID;
				else if (cosDetail.getPrepaidYes().equalsIgnoreCase("n"))
					userType = POSTPAID;

				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, cosDetail.getCosId());
				contentElem.setAttribute(START_DATE, dateFormat.format(cosDetail.getStartDate()));
				contentElem.setAttribute(END_DATE, dateFormat.format(cosDetail.getEndDate()));
				contentElem.setAttribute(CIRCLE_ID, cosDetail.getCircleId());
				contentElem.setAttribute(USER_TYPE, userType);
				contentElem.setAttribute(VALID_DAYS, String.valueOf(cosDetail.getValidDays()));
				contentElem.setAttribute(FREE_SONGS, String.valueOf(cosDetail.getFreeSongs()));
				contentElem.setAttribute(FREE_MUSICBOXES, String.valueOf(cosDetail.getFreeMusicboxes()));
				contentElem.setAttribute(IS_DEFAULT, (cosDetail.isDefaultCos() ? YES : NO));
				contentElem.setAttribute(ACCESS_MODE, cosDetail.getAccessMode());

				if (cosDetail.getSubscriptionClass() != null)
					contentElem.setAttribute(SUBSCRIPTION_CLASS, cosDetail.getSubscriptionClass());
				if (cosDetail.getFreechargeClass() != null)
					contentElem.setAttribute(CHARGE_CLASS, cosDetail.getFreechargeClass());
				if (cosDetail.getSmspromoClips() != null)
					contentElem.setAttribute(PROMO_CLIPS, cosDetail.getSmspromoClips());
				if (cosDetail.getSmsKeyword() != null)
					contentElem.setAttribute(SMS_KEYWORDS, cosDetail.getSmsKeyword());
				if (cosDetail.getOperator() != null)
					contentElem.setAttribute(OPERATOR_CODE, cosDetail.getOperator());

				contentsElem.appendChild(contentElem);
			}
		}

		return element;
	}

	public static Element generateRetailerElement(Document document, Retailer[] retailers)
	{
		Element element = document.createElement(RETAILER);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (retailers != null)
		{
			for (Retailer retailer : retailers)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, retailer.subID());
				contentElem.setAttribute(NAME, retailer.name());
				contentElem.setAttribute(TYPE, retailer.type());
				contentsElem.appendChild(contentElem);
			}
		}

		return element;
	}

	public static Element generateFeedStatusElement(Document document, FeedStatus feedStatus)
	{
		return generateFeedStatusElement(document, feedStatus, null);
	}

	public static Element generateFeedStatusElement(Document document, FeedStatus feedStatus, WebServiceContext task)
	{
		Element element = document.createElement(FEED_STATUS);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (feedStatus != null)
		{
			Element contentElem = document.createElement(CONTENT);
			contentElem.setAttribute(TYPE, feedStatus.type());
			contentsElem.appendChild(contentElem);

			String format= null;
			if(task != null)
				 format = task.getString(param_format);
			
			Utility.addPropertyElement(document, contentElem, STATUS, DATA, feedStatus.status());
			Utility.addPropertyElement(document, contentElem, FEED_FILE, PROMPT, Utility.getPromptName(feedStatus.file(),format));
			Utility.addPropertyElement(document, contentElem, SMS_KEYWORDS, DATA, feedStatus.smsKeyword());
			Utility.addPropertyElement(document, contentElem, SUB_KEYWORDS, DATA, feedStatus.subKeyword());
			Utility.addPropertyElement(document, contentElem, FEED_ON_SUCCESS_SMS, DATA, feedStatus.smsFeedOnSuccess());
			Utility.addPropertyElement(document, contentElem, FEED_ON_FAILURE_SMS, DATA, feedStatus.smsFeedOnFailure());
			Utility.addPropertyElement(document, contentElem, FEED_OFF_SUCCESS_SMS, DATA, feedStatus.smsFeedOffSuccess());
			Utility.addPropertyElement(document, contentElem, FEED_OFF_FAILURE_SMS, DATA, feedStatus.smsFeedOffFailure());
			Utility.addPropertyElement(document, contentElem, FEED_FAILURE_SMS, DATA, feedStatus.smsFeedFailure());
			Utility.addPropertyElement(document, contentElem, FEED_NON_ACTIVE_USER_SMS, DATA, feedStatus.smsFeedNonActiveSub());
		}

		return element;
	}

	
	public static Element generateFeedDetailsElement(Document document, FeedSchedule[] feedSchedules)
	{
		Element element = document.createElement(FEED_DETAILS);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		if (feedSchedules != null)
		{
			for (FeedSchedule feedSchedule : feedSchedules)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, feedSchedule.subKeyword());
				contentElem.setAttribute(NAME, feedSchedule.name());
				contentElem.setAttribute(TYPE, feedSchedule.type());
				contentsElem.appendChild(contentElem);

				Utility.addPropertyElement(document, contentElem, START_DATE, DATA, dateFormat.format(feedSchedule.startTime()));
				Utility.addPropertyElement(document, contentElem, END_DATE, DATA, dateFormat.format(feedSchedule.endTime()));
				Utility.addPropertyElement(document, contentElem, CHARGE_CLASS, DATA, feedSchedule.classType());
				Utility.addPropertyElement(document, contentElem, FEED_ON_SUCCESS_SMS, DATA, feedSchedule.smsFeedOnSuccess());
				Utility.addPropertyElement(document, contentElem, FEED_ON_FAILURE_SMS, DATA, feedSchedule.smsFeedOnFailure());
			}
		}

		return element;
	}

	public static Element generatePredefinedGroupsElement(Document document, PredefinedGroup[] predefinedGroups)
	{
		return generatePredefinedGroupsElement(document, predefinedGroups, null);
	}

	public static Element generatePredefinedGroupsElement(Document document, PredefinedGroup[] predefinedGroups, WebServiceContext task)
	{
		Element element = document.createElement(PREDEFINED_GROUPS);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		int noOfGroups = 0;

		String format= null;
		if(task != null)
			 format = task.getString(param_format);
		
		if (predefinedGroups != null)
		{
			noOfGroups = predefinedGroups.length;
			for (PredefinedGroup preDefinedGroup : predefinedGroups)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, preDefinedGroup.getPreGroupID());
				contentElem.setAttribute(NAME, preDefinedGroup.getPreGroupName());
				contentsElem.appendChild(contentElem);

				String groupNamePrompt = preDefinedGroup.getPreGroupName().replaceAll(" ", "_");
				Utility.addPropertyElement(document, contentElem, GROUP_NAME_PROMPT, PROMPT, Utility.getPromptName(groupNamePrompt,format));
			}
		}

		element.setAttribute(NO_OF_GROUPS, String.valueOf(noOfGroups));

		return element;
	}

	public static Element generateViralDataElement(Document document, WebServiceContext task, ViralSMSTable[] viralSMSTableData)
	{
		Element element = document.createElement(VIRAL_DATA);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		String format= null;
		if(task != null)
			 format = task.getString(param_format);
		
		if (viralSMSTableData != null)
		{
			for (ViralSMSTable viralSMSTable : viralSMSTableData)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(TYPE, viralSMSTable.type());
				contentsElem.appendChild(contentElem);

				Utility.addPropertyElement(document, contentElem, SUBSCRIBER_ID, DATA, viralSMSTable.subID());
				Utility.addPropertyElement(document, contentElem, CALLER_ID, DATA, viralSMSTable.callerID());
				Utility.addPropertyElement(document, contentElem, COUNT, DATA, String.valueOf(viralSMSTable.count()));
				Utility.addPropertyElement(document, contentElem, CLIP_ID, DATA, viralSMSTable.clipID());

				if (viralSMSTable.sentTime() != null)
					Utility.addPropertyElement(document, contentElem, SENT_TIME, DATA, dateFormat.format(viralSMSTable.sentTime()));

				if (viralSMSTable.setTime() != null)
					Utility.addPropertyElement(document, contentElem, SET_TIME, DATA, dateFormat.format(viralSMSTable.setTime()));

				Utility.addPropertyElement(document, contentElem, SELECTED_BY, DATA, viralSMSTable.selectedBy());					
				Utility.addPropertyElement(document, contentElem, INFO, DATA, viralSMSTable.extraInfo());
				Utility.addPropertyElement(document, contentElem, SMS_ID, DATA, String.valueOf(viralSMSTable.getSmsId()));

				String wavFile = viralSMSTable.clipID();
				Clip clip = null;

				if(viralSMSTable.clipID() != null && viralSMSTable.clipID().indexOf(":") > -1)
				{
					wavFile = viralSMSTable.clipID().substring(0, viralSMSTable.clipID().indexOf(":"));
				}
				String browsingLanguage = task.getString(param_browsingLanguage);
				clip = rbtCacheManager.getClipByRbtWavFileName(wavFile, browsingLanguage);

				if(clip != null)
				{
					contentElem.setAttribute(ID, String.valueOf(clip.getClipId()));
					contentElem.setAttribute(NAME, clip.getClipName());

					Utility.addPropertyElement(document, contentElem, PREVIEW_FILE, PROMPT, Utility.getPromptName(clip.getClipPreviewWavFile(),format));
					Utility.addPropertyElement(document, contentElem, RBT_FILE, PROMPT, Utility.getPromptName(clip.getClipRbtWavFile(),format));
				}
			}
		}

		return element;
	}
	
	public static Element generatePendingConfirmationReminderElement(
			Document document, WebServiceContext task,
			PendingConfirmationsReminderTableImpl[] pendingConfirmationsRemainders) {
		Element element = document.createElement(PENDING_CONFIRMATIONS_REMINDER_DATA);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		if (pendingConfirmationsRemainders != null) {
			for (PendingConfirmationsReminderTableImpl pendingConfirmationsReminder : pendingConfirmationsRemainders) {
				Element contentElem = document.createElement(CONTENT);
				contentsElem.appendChild(contentElem);
				
				Utility.addPropertyElement(document, contentElem,
						SUBSCRIBER_ID, DATA, pendingConfirmationsReminder.getSubscriberId());
				Utility.addPropertyElement(document, contentElem, REMINDERS_LEFT,
						DATA, String.valueOf(pendingConfirmationsReminder.getRemindersLeft()));

				if (pendingConfirmationsReminder.getLastReminderSent() != null) {
					Utility.addPropertyElement(document, contentElem,
							LAST_REMINDER_SENT, DATA, dateFormat.format(pendingConfirmationsReminder
									.getLastReminderSent()));
				}

				if (pendingConfirmationsReminder.getSmsReceivedTime() != null) {
					Utility.addPropertyElement(document, contentElem, SMS_RECEIVED_TIME,
							DATA, dateFormat.format(pendingConfirmationsReminder.getSmsReceivedTime()));
				}

				Utility.addPropertyElement(document, contentElem, REMINDER_TEXT,
						DATA, pendingConfirmationsReminder.getReminderText());
				Utility.addPropertyElement(document, contentElem, SENDER, DATA,
						String.valueOf(pendingConfirmationsReminder.getSender()));
				Utility.addPropertyElement(document, contentElem, SMS_ID, DATA,
						String.valueOf(pendingConfirmationsReminder.getSmsId()));
			}
		}

		return element;
	}

	public static Element generateTransDataElement(Document document, TransData[] transDatas)
	{
		Element element = document.createElement(TRANS_DATA);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		if (transDatas != null)
		{
			for (TransData transData : transDatas)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, transData.transID());
				contentElem.setAttribute(TYPE, transData.type());
				contentsElem.appendChild(contentElem);

				Utility.addPropertyElement(document, contentElem, SUBSCRIBER_ID, null, transData.subscriberID());

				if (transData.transDate() != null)
					Utility.addPropertyElement(document, contentElem, DATE, null, dateFormat.format(transData.transDate()));

				Utility.addPropertyElement(document, contentElem, ACCESS_COUNT, null, transData.accessCount());
			}
		}

		return element;
	}
	
	public static Element generateRbtSupportDataElement(Document document, RbtSupport[] rbtSupportDatas)
	{
		Element element = document.createElement(RBTSUPPORT_DATA);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		if (rbtSupportDatas != null)
		{
			for (RbtSupport rbtSupport : rbtSupportDatas)
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, String.valueOf(rbtSupport.getId()));
				contentElem.setAttribute(TYPE, String.valueOf(rbtSupport.getType()));
				contentsElem.appendChild(contentElem);

				Utility.addPropertyElement(document, contentElem, SUBSCRIBER_ID, null, String.valueOf(rbtSupport.getSubscriberId()));
				Utility.addPropertyElement(document, contentElem, CALLER_ID, null, String.valueOf(rbtSupport.getCallerId()));
				Utility.addPropertyElement(document, contentElem, CLIP_ID, null, String.valueOf(rbtSupport.getClipId()));
				Utility.addPropertyElement(document, contentElem, INFO, null, rbtSupport.getExtraInfo());

				if (rbtSupport.getRequestDate() != null)
					Utility.addPropertyElement(document, contentElem, DATE, null, dateFormat.format(rbtSupport.getRequestDate()));
			}
		}

		return element;
	}

	public static Element generateBulkTaskElement(Document document, List<RBTBulkUploadTask> bulkTaskList)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		Element bulkTasksElem = document.createElement(BULK_TASKS);

		for (RBTBulkUploadTask rbtBulkUploadTask : bulkTaskList) 
		{
			Element taskElement = document.createElement(CONTENT);

			taskElement.setAttribute(BULK_TASK_ID, String.valueOf(rbtBulkUploadTask.getTaskId()));
			taskElement.setAttribute(BULK_TASK_TYPE, rbtBulkUploadTask.getTaskType());
			taskElement.setAttribute(SELECTION_TYPE, String.valueOf(rbtBulkUploadTask.getSelectionType()));
			taskElement.setAttribute(BULK_TASK_STATUS, String.valueOf(rbtBulkUploadTask.getTaskStatus()));

			if (rbtBulkUploadTask.getCircleId() != null)
				taskElement.setAttribute(CIRCLE_ID, rbtBulkUploadTask.getCircleId());

			if (rbtBulkUploadTask.getTaskName() != null)
				taskElement.setAttribute(BULK_TASK_NAME, rbtBulkUploadTask.getTaskName());

			if (rbtBulkUploadTask.getActivationClass() != null)
				taskElement.setAttribute(SUBSCRIPTION_CLASS, rbtBulkUploadTask.getActivationClass());

			if (rbtBulkUploadTask.getSelectionClass() != null)
				taskElement.setAttribute(CHARGE_CLASS, rbtBulkUploadTask.getSelectionClass());

			if (rbtBulkUploadTask.getActivatedBy() != null)
				taskElement.setAttribute(ACTIVATED_BY, rbtBulkUploadTask.getActivatedBy());

			if (rbtBulkUploadTask.getActInfo() != null)
				taskElement.setAttribute(ACTIVATION_INFO, rbtBulkUploadTask.getActInfo());

			if (rbtBulkUploadTask.getUploadTime() != null)
				taskElement.setAttribute(BULK_UPLOAD_TIME, dateFormat.format(rbtBulkUploadTask.getUploadTime()));

			if (rbtBulkUploadTask.getProcessTime() != null)
				taskElement.setAttribute(BULK_PROCESS_TIME, dateFormat.format(rbtBulkUploadTask.getProcessTime()));

			if(rbtBulkUploadTask.getEndTime() != null)
				taskElement.setAttribute(BULK_END_TIME, dateFormat.format(rbtBulkUploadTask.getEndTime()));

			if(rbtBulkUploadTask.getTaskInfo() != null)
				taskElement.setAttribute(BULK_TASK_INFO, rbtBulkUploadTask.getTaskInfo());

			if(rbtBulkUploadTask.getTaskMode() != null)
				taskElement.setAttribute(BULK_TASK_MODE, rbtBulkUploadTask.getTaskMode());

			bulkTasksElem.appendChild(taskElement);
		}

		return bulkTasksElem;
	}

	public static Element generateOfferElement(Document document, Offer[] offers)
	{
		Element allOffersElement = document.createElement(OFFERS);
		if (offers != null)
		{
			for (Offer offer : offers)
			{
				Element thisOfferElement = document.createElement(OFFER);
				Utility.addPropertyElement(document, thisOfferElement, OFFER_ID, DATA, offer.getOfferID());
				Utility.addPropertyElement(document, thisOfferElement, OFFER_DESC, DATA, offer.getOfferDescription());
				Utility.addPropertyElement(document, thisOfferElement, OFFER_TYPE, DATA, String.valueOf(offer.getOfferType()));
				Utility.addPropertyElement(document, thisOfferElement, OFFER_SRVKEY, DATA, offer.getSrvKey());
				Utility.addPropertyElement(document, thisOfferElement, OFFER_AMOUNT, DATA, String.valueOf(offer.getAmount()));
				Utility.addPropertyElement(document, thisOfferElement, OFFER_VALID_DAYS, DATA, String.valueOf(offer.getValidityDays()));
				Utility.addPropertyElement(document, thisOfferElement, OFFER_STATUS, DATA, offer.getOfferStatus());
				Utility.addPropertyElement(document, thisOfferElement, OFFER_SM_OFFER_TYPE, DATA, offer.getSmOfferType());
				Utility.addPropertyElement(document, thisOfferElement, OFFER_SM_RATE, DATA, offer.getSmRate());
				Utility.addPropertyElement(document, thisOfferElement, OFFER_CREDITS_AVAILABLE, DATA, offer.getCreditsAvailable()+"");
				Utility.addPropertyElement(document, thisOfferElement, OFFER_TYPE_VALUE, DATA, offer.getOfferTypeValue()+"");
				//TO get offer validity, offer renewal amout and offer renewal validity.if offer type is 1get it from subscription class else if 2 from  chagre class
				if(offer.getOfferType() == 1) {
					SubscriptionClass SubClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(offer.getSrvKey());
					if( SubClass != null){
						Utility.addPropertyElement(document, thisOfferElement, OFFER_VALIDITY, DATA, SubClass.getSubscriptionPeriod());
						Utility.addPropertyElement(document, thisOfferElement, OFFER_RENEWAL_AMOUNT, DATA,SubClass.getRenewalAmount());
						Utility.addPropertyElement(document, thisOfferElement, OFFER_RENEWAL_VALIDITY, DATA,SubClass.getRenewalPeriod());
					}
				}else if(offer.getOfferType() == 2){
					ChargeClass chargeClass =  CacheManagerUtil.getChargeClassCacheManager().getChargeClass(offer.getSrvKey());
					if(chargeClass !=null){
						Utility.addPropertyElement(document, thisOfferElement, OFFER_VALIDITY, DATA, chargeClass.getSelectionPeriod());
						Utility.addPropertyElement(document, thisOfferElement, OFFER_RENEWAL_AMOUNT, DATA, chargeClass.getRenewalAmount());
						Utility.addPropertyElement(document, thisOfferElement, OFFER_RENEWAL_VALIDITY, DATA, chargeClass.getRenewalPeriod());
					}
				}

				allOffersElement.appendChild(thisOfferElement);
			}
		}
		return allOffersElement;
	}
	
  // RBT-8199:Need to include all SMS logs in CCC GUI as part of SMS logs	
  public static Element generateSMSHistoryElement(Document document, SMSHistory[] smsHistoryArr) {
      Element allSMSHistoryUMPElement = document.createElement(SMS_HISTORY_FROM_UMP);
      Element contentsElem = document.createElement(CONTENTS);

	  if(smsHistoryArr != null) {
		 for (SMSHistory smsHistory : smsHistoryArr) {
			 Element smsHistoryUMPElement = document.createElement(CONTENT);
			 smsHistoryUMPElement.setAttribute(TYPE, smsHistory.getSmsType());
			 smsHistoryUMPElement.setAttribute(SMS_TEXT, smsHistory.getSmsText());
			 smsHistoryUMPElement.setAttribute(SENT_TIME, smsHistory.getSentTime());
			 contentsElem.appendChild(smsHistoryUMPElement);
		  }
		}
	  allSMSHistoryUMPElement.appendChild(contentsElem);
	return allSMSHistoryUMPElement;
  }

	public static Element generateBIDownloadHistoryElement(Document document,
			Document downloadHistoryDetailsDocument, WebServiceContext task)
	{
		Element element = document.createElement(BI_DOWNLOAD_HISTORY);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		if (downloadHistoryDetailsDocument != null)
		{
			/* (non-Javadoc)
			 * API: BIHistoryDownload
			 * Format of transaction XML:
			 *	<?xml version="1.0" encoding="UTF-8"?>
			 *	<BiResponse>
			 *		<rc1>1480445</rc1>
			 *		<rc2>1679072</rc2>
			 *		<rc3>1469955</rc3>
			 *		<rc4>11061</rc4>
			 *		<rc5>1808280</rc5>
			 *		<rc6>2877</rc6>
			 *		<rc7>10613910</rc7>
			 *		<rc8>10036634</rc8>
			 *		<rc9>10037137</rc9>
			 *		<rc10>10034522</rc10>
			 *	</BiResponse>
			 */

			
			List<String> promoIdList = null;
			try
			{
				Element responseElem = (Element) downloadHistoryDetailsDocument.getElementsByTagName(BIRESPONSE).item(0);
				promoIdList = XMLParser.getBiRespose(responseElem);
			}
			catch(Exception e)
			{
				logger.error("Error while parsing BI donwload History response : " + e.getMessage());
			}
			
			if(promoIdList != null && promoIdList.size() > 0){
				int size = promoIdList.size();
				for(int i = 0; i < size; i++)
				{
					Element contentElem = document.createElement(CONTENT);
					contentElem.setAttribute(VALUE, promoIdList.get(i));
					contentsElem.appendChild(contentElem);
				}
			}
		}
		return element;
	}
	
	public static Element generateSubscriberPackElement(Document document,
			WebServiceContext task, SubscriberPack subscriberPack)
	{

		Element element = document.createElement(SUBSCRIBERPACK);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		
		Utility.addPropertyElement(document, element, PACK_CHARGE_CLASS, DATA, subscriberPack.getPackChargeClass());
		Utility.addPropertyElement(document, element, PACK_COS_ID, DATA, subscriberPack.getCosId());
		Utility.addPropertyElement(document, element, PACK_COS_TYPE, DATA, subscriberPack.getCosType());
		if(subscriberPack.getDeactivateMode() != null)
			Utility.addPropertyElement(document, element, PACK_DEACTIVATE_MODE, DATA, subscriberPack.getDeactivateMode());
		if(subscriberPack.getDeactivateModeInfo() != null)
			Utility.addPropertyElement(document, element, PACK_DEACTIVATE_MODE_INFO, DATA, subscriberPack.getDeactivateModeInfo());
		if(subscriberPack.getDeactivateDate() != null)
			Utility.addPropertyElement(document, element, PACK_DEACTIVATE_TIME, DATA, dateFormat.format(subscriberPack.getDeactivateDate()));
		if(subscriberPack.getLastChargingDate() != null)
			Utility.addPropertyElement(document, element, PACK_LAST_CHARGING_TIME, DATA, dateFormat.format(subscriberPack.getLastChargingDate()));
		Utility.addPropertyElement(document, element, PACK_MODE, DATA, subscriberPack.getPackMode());
		Utility.addPropertyElement(document, element, PACK_MODE_INFO, DATA, subscriberPack.getPackModeInfo());
		Utility.addPropertyElement(document, element, PACK_REF_ID, DATA, subscriberPack.getIntRefId());
		Utility.addPropertyElement(document, element, PACK_START_TIME, DATA, dateFormat.format(subscriberPack.getCreationTime()));
		Utility.addPropertyElement(document, element, PACK_STATUS, DATA, subscriberPack.getStatus());
		Utility.addPropertyElement(document, element, PACK_NUM_MAX_SELECTIONS, DATA, String.valueOf(subscriberPack.getNumMaxSelections()));
		Map<String, String> chargingDetailsMap = Utility.getNextBillingDateOfServices(task);
		String amountChargedKey = subscriberPack.getIntRefId() + "_lastAmountCharged";
		String lastChargedAmount = chargingDetailsMap.get(amountChargedKey);
		if(lastChargedAmount != null){
			Utility.addPropertyElement(document, element, LAST_CHARGE_AMOUNT, DATA, lastChargedAmount);
		}
		String nextBillingDate = Utility.getNextBillingDateOfServices(task).get(subscriberPack.getIntRefId());
		String lastChargedDate = Utility.getNextBillingDateOfServices(task).get(subscriberPack.getIntRefId() + "_lastChargingDate");
		String transactionType = Utility.getNextBillingDateOfServices(task).get(subscriberPack.getIntRefId() + "_lastTransactionType");
		Utility.addPropertyElement(document, element, NEXT_BILLING_DATE, DATA, nextBillingDate);
		Utility.addPropertyElement(document, element, PACK_LAST_CHARGING_TIME, DATA, lastChargedDate);
		Utility.addPropertyElement(document, element, LAST_TRANSACTION_TYPE, DATA, transactionType);
		return element;
	}
	
	private static void processSmsHistoryFile(String subscriberID, File file, Document document, Element contentsElem) {

		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try
		{
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null)
			{
				if (line.indexOf(subscriberID) == -1)
					continue;

				createSmsHistoryContentElement(line, document, contentsElem);
			}
		}
		catch (FileNotFoundException e)
		{
			logger.error("", e);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (fileReader != null)
					fileReader.close();
				if (bufferedReader != null)
					bufferedReader.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
	
	}

	private static void createSmsHistoryContentElement(String line, Document document, Element contentsElem) throws Exception{
		String smsType = null;
		String smsText = null;
		String sentTime = null;
		if (line.startsWith("SMS-REQUEST"))
		{
			if (!line.contains("RBT") || !line.contains("NA")
					|| line.indexOf("RBT") >= line.indexOf("NA")
					|| (line.substring(line.lastIndexOf(",") + 1)).length() <= 14) {
				return;
			}

			smsType = "SMS-REQUEST";
			smsText = line.substring(line.indexOf("RBT") + 4, line.indexOf("NA") - 1);
			sentTime = line.substring(line.lastIndexOf(",") + 1, line.lastIndexOf(",") + 15);
		}
		else if (line.startsWith("SMS-RESPONSE"))
		{
			java.net.InetAddress inetAddress = java.net.InetAddress.getLocalHost();
			String ipAddress = inetAddress.getHostAddress();

			if (!line.contains(ipAddress)
					|| line.indexOf(ipAddress) >= line.lastIndexOf(",")
					|| (line.substring(line.lastIndexOf(",") + 1)).length() <= 14)
				return;

			smsType = "SMS-RESPONSE";
			smsText = line.substring(line.indexOf(ipAddress) + 12, line.lastIndexOf(","));
			sentTime = line.substring(line.lastIndexOf(",") + 1, line.lastIndexOf(",") + 15);
		}

		if (smsType != null)
		{
			Element contentElem = document.createElement(CONTENT);
			contentElem.setAttribute(TYPE, smsType);
			contentElem.setAttribute(SMS_TEXT, smsText);
			contentElem.setAttribute(SENT_TIME, sentTime);
			contentsElem.appendChild(contentElem);
		}

	}
	
	private static void processSmsHistoryGrep4j(String subscriberID, String sdrFilePath, Document document, Element contentsElem, SortedMap<String, List<Element>> smsTransactionSortedMap) {

		int historyOfLastNDays = RBTParametersUtils.getParamAsInt("COMMON", "SMS_HISTORY_OF_LAST_N_DAYS", 30);
		
		Calendar currentMonth = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String strDate = dateFormat.format(currentMonth.getTime());
		int currYear = Integer.parseInt(strDate.substring(0, 4));
		int currMonth = Integer.parseInt(strDate.substring(4, 6));
		int currDay = Integer.parseInt(strDate.substring(6, 8));
		
		currentMonth.add(Calendar.DATE, -historyOfLastNDays);
		strDate = dateFormat.format(currentMonth.getTime());
		int lastYear = Integer.parseInt(strDate.substring(0, 4));
		int lastMonth = Integer.parseInt(strDate.substring(4, 6));
		int lastDay = Integer.parseInt(strDate.substring(6, 8));
		
		int tempMonth = lastMonth;
		
		while(true) {
			
			String dirName = sdrFilePath + File.separator + lastYear + File.separator + lastMonth;
			File file = new File(dirName);
			
			if(!file.exists()) {
				break;
			}
			
			if(tempMonth == lastMonth) {
				int count  = 0;
				int d = lastDay / 10;
				int r = lastDay % 10;
				int endRange = 9;
				while(true) {
					String tempDirName =  dirName + File.separator + "SMS_REQUEST_" + d + "[" + r + "-" + endRange + "]" + lastMonth+lastYear+".LOG";
					try {
						processSmsHistoryFileByUserGrep4j(subscriberID, tempDirName, document, contentsElem, smsTransactionSortedMap);
					}
					catch(Exception e) {
						logger.error("Exception while grep: " + e.getMessage());
						//Ignore
					}
					d++;
					r = 0;
					if(d == 4) {
						break;
					}
					count++;
				}
			}
			else{
				dirName = dirName + File.separator + "*.LOG";
				processSmsHistoryFileByUserGrep4j(subscriberID, dirName, document, contentsElem, smsTransactionSortedMap);				
			}
			lastMonth++;
			if(lastMonth == 13 && currYear != lastYear) {
				lastYear++;
				lastMonth = 1;
				
			}
		}
		
		processSmsHistoryFileByUserGrep4j(subscriberID, sdrFilePath + File.separator + "*.LOG", document, contentsElem, smsTransactionSortedMap);
		processSmsHistoryFileByUserGrep4j(subscriberID, sdrFilePath + File.separator + "SMS_REQUEST", document, contentsElem, smsTransactionSortedMap);
		
	}
	
	
	private static void processSmsHistoryFileByUserGrep4j(String subscriberID, String filePath, Document document, Element contentsElem, SortedMap<String, List<Element>> smsTransactionSortedMap) {

		logger.info("Run the grep: " + filePath);
		long startTimeMillis = System.currentTimeMillis();		
		Profile localProfile = ProfileBuilder.newBuilder()
                .name("Local Service log")
                .filePath(filePath)
                .onLocalhost()
                .build();
		boolean supportNewSmsHistory = smsTransactionSortedMap != null ? true : false;
		String expression = subscriberID;
		if(supportNewSmsHistory) {
			expression = "SUB_ID="+subscriberID;
		}
		
		GrepResults results = grep(constantExpression(expression), on(localProfile));
		for (GrepResult singleResult : results) {       
		   ByteArrayInputStream in = null;
		   BufferedReader br = null;
		   
		   try {
			   in = new ByteArrayInputStream(singleResult.getText().getBytes());
			   br = new BufferedReader(new InputStreamReader(in));
			   String line = null;
			   while((line = br.readLine()) != null) {
				   logger.info("Grep line: " + line);
				   if(!supportNewSmsHistory) {
					   createSmsHistoryContentElement(line, document, contentsElem);
				   }
				   else {
					   createNewSMSHistoryContentElement(line, subscriberID, smsTransactionSortedMap, document,null,null);
				   }
			   }
		   }
		   catch (FileNotFoundException e)
			{
				logger.error("", e);
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
			finally
			{
				try
				{
					if (br != null)
						br.close();
					if (in != null)
						in.close();
				}
				catch (Exception e)
				{
					logger.error("", e);
				}
			}
		}
		
		logger.info("Grep processing time taken: " + (System.currentTimeMillis() - startTimeMillis) + " ms");
	}
	
	private static void createNewSMSHistoryContentElement(String line, String subscriberID, SortedMap<String, List<Element>> smsTransactionSortedMap, Document document, Date filterStartDate, Date filterEndDate) throws Exception{

		String token = null;
		String token1 = null;
		String request = null;
		String response = null;
		SimpleDateFormat sdfStarttime = new SimpleDateFormat("yyyyMMddHHmmss");
		String requestTime = null;
		String responseTime = null;
		boolean subscriberFound = true;
		if (line.contains("MSISDN=") || line.contains("msisdn=") || line.contains("SUB_ID"))
		{
			StringTokenizer tokenizer = new StringTokenizer(line, "|");
			if (tokenizer.hasMoreTokens())
			{
				token = tokenizer.nextToken();
				StringTokenizer tokenizer1 = new StringTokenizer(token, "&");
				while(tokenizer1.hasMoreTokens())
				{
					token1 = tokenizer1.nextToken();

					if (token1.startsWith("MSISDN=") || token1.startsWith("msisdn=") || token1.startsWith("SUB_ID="))
					{
						if (token1.indexOf(subscriberID) == -1)
						{
							subscriberFound = false;
							break;
						}
					}

					if (token1.startsWith("MESSAGE=") || token1.startsWith("SMS_TEXT=") || token1.startsWith("msg="))
					{
						request = token1.substring(token1.indexOf("=") + 1);
						URLCodec decoder = new URLCodec();
						request = decoder.decode(request, "UTF-8");
					}

					if (token1.startsWith("startTime"))
					{
						try
						{
							long startTime = Long.parseLong(token1.substring(token1.indexOf("=") + 1));
							Date startDate = new Date(startTime);
							requestTime = sdfStarttime.format(startDate);
						}
						catch(Exception e)
						{
							requestTime = null;
						}
					}
				}
			}
			if (!subscriberFound)
			{
				return;
			}
			if (tokenizer.hasMoreTokens())
			{
				response = tokenizer.nextToken();
			}

			while(tokenizer.hasMoreTokens())
			{
				responseTime = tokenizer.nextToken();
			}

			if (requestTime == null)
			{
				requestTime = responseTime;
			}

			List<Element> transactionElementList = new ArrayList<Element>();

			Element requestContentElem = document.createElement(CONTENT);
			requestContentElem.setAttribute(TYPE, "SMS-REQUEST");
			requestContentElem.setAttribute(SMS_TEXT, request);
			requestContentElem.setAttribute(SENT_TIME, requestTime);
			if ((filterStartDate != null) && (filterEndDate != null)) {
				Date requestDate = sdfStarttime.parse(requestTime);
				if ((filterStartDate.equals(requestDate))
						|| (filterEndDate.equals(requestDate))
						|| ((filterStartDate.before(requestDate)) && (filterEndDate
								.after(requestDate)))) {
					transactionElementList.add(requestContentElem);
				}
			} else {
				transactionElementList.add(requestContentElem);
			}
			Element responseContentElem = document.createElement(CONTENT);
			responseContentElem.setAttribute(TYPE, "SMS-RESPONSE");
			responseContentElem.setAttribute(SMS_TEXT, response);
			responseContentElem.setAttribute(SENT_TIME, responseTime);
			if ((filterStartDate != null) && (filterEndDate != null)) {
				Date responseDate = sdfStarttime.parse(responseTime);
				if ((filterStartDate.equals(responseDate))
						|| (filterEndDate.equals(responseDate))
						|| ((filterStartDate.before(responseDate)) && (filterEndDate
								.after(responseDate)))) {
					transactionElementList.add(responseContentElem);
				}
			} else {
				transactionElementList.add(responseContentElem);
			}
			if (null != requestTime) {
				smsTransactionSortedMap
						.put(requestTime, transactionElementList);
			}
		}
	
	}

}