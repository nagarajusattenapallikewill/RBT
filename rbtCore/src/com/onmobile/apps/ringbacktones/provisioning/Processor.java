/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ChargeClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.CosDetailsCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SubscriptionClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.lucene.AbstractLuceneIndexer;
import com.onmobile.apps.ringbacktones.lucene.LuceneIndexerFactory;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Gift;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GiftInbox;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RecentSelection;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Retailer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SMSText;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPromo;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CopyRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;


/**
 * @author vinayasimha.patil
 *
 */
public abstract class Processor implements Constants,iRBTConstant
{	
	private String LeadingString=null;
	private boolean removeLeadingZero=false;
	protected static RBTClient rbtClient = null;
	protected static RBTCacheManager rbtCacheManager = null;
	protected static AbstractLuceneIndexer luceneIndexer = null;
	protected static Logger logger = Logger.getLogger(Processor.class);
	protected static Object syncObject = new Object();
	protected static SMSText[] smsTexts = null;
	private String[] LeadingStringArray=null;
	private Hashtable<String, String> m_smsTable = null;
	int STATUS_TECHNICAL_FAILURE = 5;
	//user invalid prefix error message
	String m_technicalFailureDefault = "I am sorry, we are having some technical difficulties. Please try later";
	public String invalidPrefixDefault = "You are not authorized to use this service. We apologize the inconvenience";
	public String invalidPrefix = invalidPrefixDefault;
	//user black-listed error message
	public String blackListedSMSTextDefault = "Your number is in the black list. Plz call Customer care for further details";
	public String blackListedSMSText = blackListedSMSTextDefault;
	//user activation-pending error message
	public String activationPendingDefault = "Your activation request is in pending state. Plz wait for the SMS confirmation";
	public String activationPending = activationPendingDefault;
	//user deactivation-pending error message
	public String deactivationPendingDefault = "Your deactivation request is in pending state. Plz wait for the SMS confirmation";
	public String deactivationPending = deactivationPendingDefault;
	//user express-copy-pending error message
	public String expressCopyPendingDefault = "Your express copy request is still pending. Plz wait for the SMS confirmation";
	public String expressCopyPending = expressCopyPendingDefault;
	//user gifting-pending error message
	public String giftingPendingDefault = "You have been gifted a CRBT by your friend. The request is still pending.";
	public String giftingPending = giftingPendingDefault;
	//user renewal-pending error message
	public String renewalPendingDefault = "Your request for renewal is under process. Please try after sometime";
	public String renewalPending = renewalPendingDefault;
	//user suspended error message
	public String suspendedDefault = "Dear subscriber, your account has been temporarily suspended";
	public String suspended = suspendedDefault;
	//technical failure error message
	public String technicalFailureDefault = "I am sorry, we are having some technical difficulties. Please try later";
	public String technicalFailure = technicalFailureDefault;
	//access failure error message
	public String accessFailureDefault = "Dear subscriber, you are not authorised to access the service.";
	public String accessFailure = accessFailureDefault;
	//not active user message
	public String notActiveTextDefault = "Dear User, You are not subscribed to Welcome Tunes service, to activate send SUB to 12800";
	public String notActiveText = notActiveTextDefault;
	//already active user message
	public String alreadyActiveDefault = "Dear User, You are already subscribed to Welcome Tunes service";
	public String alreadyActive = alreadyActiveDefault;
	//activation blocked
	public String activationBlocked = "activationBlocked";
	public String baseOfferNotFound = "baseOfferNotFound";

	/* Selection related errors */
	//clip expired message
	public String clipExpiredDefault = "Sorry! The requested song is no longer available";
	public String clipExpired = clipExpiredDefault;
	//clip not available message
	public String clipNotAvailableDefault = "Sorry!!! The requested song is not available";
	public String clipNotAvailable = clipNotAvailableDefault;
	//song already exist message
	public String selAlreadyExistsDefault = "The song you requested is already set at your welcome tune";
	public String selAlreadyExists = selAlreadyExistsDefault;

	//Activation success message
	public String activationSuccessDefault = "Your request has been received. You will be activated in the next 24 hours";
	public String activationSuccess = activationSuccessDefault;
	//Deactivation success message
	public String deactivationSuccessDefault = "Your request has been received. You will be deactivated in the next 24 hours";
	public String deactivationSuccess = deactivationSuccessDefault;
	//Selection success message
	public String selectionSuccessDefault = "The song you requested will be set as your welcome tune";
	public String selectionSuccess = selectionSuccessDefault;

	public int smsCategoryID = 3;

	/**
	 * @throws Exception 
	 * 
	 */

	public static ParametersCacheManager parameterCacheManager = null;
	public static SubscriptionClassCacheManager  subClassCacheManager = null;
	public static ChargeClassCacheManager  chargeClassCacheManager = null;
	public static CosDetailsCacheManager  cosDetailsCacheManager = null;
	private static String strCircleIdMap =null;
	private static Map<String,String> circleIdMap = null;
	
	static {
		parameterCacheManager = CacheManagerUtil.getParametersCacheManager();
		subClassCacheManager = CacheManagerUtil.getSubscriptionClassCacheManager();
		chargeClassCacheManager = CacheManagerUtil.getChargeClassCacheManager();
		cosDetailsCacheManager = CacheManagerUtil.getCosDetailsCacheManager();
	}

	public Processor() throws RBTException
	{
		//		parameterCacheManager = CacheManagerUtil.getParametersCacheManager();
		//		subClassCacheManager = CacheManagerUtil.getSubscriptionClassCacheManager();
		//		chargeClassCacheManager = CacheManagerUtil.getChargeClassCacheManager();
		//		cosDetailsCacheManager = CacheManagerUtil.getCosDetailsCacheManager();
		try
		{
			logger = Logger.getLogger(Processor.class);
			rbtClient = RBTClient.getInstance();
			rbtCacheManager = RBTCacheManager.getInstance();
			luceneIndexer = LuceneIndexerFactory.getInstance();
			String smsCatID = getSMSParameter("SMS_CATEGORY_ID");

			//Added to remove 0 ,if promocode starts with 0
			String removeZero=getSMSParameter("REMOVE_LEADING_ZERO");


			if(removeZero!=null&&removeZero.equalsIgnoreCase("TRUE"))
				removeLeadingZero=true;
			if(removeLeadingZero)
			{ LeadingString=getSMSParameter("LEADING_STRING");
			if(LeadingString!=null)
				LeadingStringArray=LeadingString.split(",");
			}
			if (smsCatID != null){
				int id;
				try{
					id = Integer.parseInt(smsCatID);
				}catch (Exception e){
					id = 3;
				}
				smsCategoryID = id;
			}
			strCircleIdMap = RBTParametersUtils.getParamAsString("COMMON",
					"CIRCLEID_MAPPING_FOR_THIRD_PARTY", null);
			circleIdMap = MapUtils.convertToMap(strCircleIdMap, ";", ":", null);
			logger.info("circleIdMap: " + circleIdMap);
		}
		catch(Exception e)
		{
			logger.error("Exception in Processor Init",e);
			throw new RBTException(e.getMessage());
		}
	}

	public abstract Task getTask(HashMap<String, String> requestParams);
	public abstract String validateParameters(Task task);
	public abstract boolean isValidPrefix(String subId);
	public void processHelpRequest(Task task){};
	public void processCategorySearch(Task task){};
	public void processRetailerRequest(Task task){};
	public void processRetailerSearch(Task task){};
	public void processRetailerAccept(Task task){};
	public abstract void processSelection(Task task);
	public void processFeed(Task task){};
	public abstract void processGiftAckRequest(Task task);
	public void processDefaultSearch(Task task){};//song movie artist search
	public void processClipByPromoID(Task task){};
	public void processCategoryByPromoID(Task task){};
	public void processCategoryByAlias(Task task){};
	public void processClipByAlias(Task task){};
	public void processProfile(Task task){};
	public void processListProfiles(Task task){};
	public void processRemoveProfile(Task task){};
	public void processLoop(Task task){};
	public void processDelete(Task task){};
	public void processTrial(Task task){};
	public void processTrialReply(Task task){};
	public void processOBDRequest(Task task){};
	public void processViralRequest(Task task){};
	public void processDisableUdsRequest(Task task){};
	public void processEnableUdsRequest(Task task){};
	public void processDownloadOptinRenewal(Task task){};
	public boolean isRemoteSub(String strSubID){return false;};
	public boolean deleteConsentRecord(Task task){return false;};
	//TODO: Add all action methods 
	//action methods argument should be Task object only

	public  static String param(String type, String paramName, String defaultVal) {
		Parameters param = parameterCacheManager.getParameter(type, paramName, defaultVal);
		if (param != null){
			String value = param.getValue();
			if (value != null) return value.trim();
		}
		return defaultVal;
	}

	public  static int param(String type, String paramName, int defaultVal)
	{
		Parameters param = parameterCacheManager.getParameter(type, paramName, String.valueOf(defaultVal));
		if (param != null){
			try
			{
				String value = param.getValue();
				if (value != null)
					return Integer.parseInt(value.trim());
			}
			catch(Exception e)
			{
				return defaultVal;
			}
		}
		return defaultVal;
	}

	public static boolean param(String type, String paramName, boolean defaultVal) {
		Parameters param = parameterCacheManager.getParameter(type, paramName, String.valueOf(defaultVal));
		if (param != null){
			String value = param.getValue();
			if (value != null)
			{
				value = value.trim();
				return (value.equalsIgnoreCase("TRUE") || value.equalsIgnoreCase("YES") || value.equalsIgnoreCase("ON")); 
			}	
		}
		return defaultVal;
	}

	public static SubscriptionClass getSubscriptionClass(String subClassType)
	{
		return subClassCacheManager.getSubscriptionClass(subClassType);
	}

	public static Subscriber getSubscriber(Task task)
	{
		String subscriberID = task.getString(param_subscriberID);
		if (subscriberID == null)
			subscriberID = task.getString(param_subID);
		if (subscriberID == null)
			subscriberID = task.getString(param_msisdn);
		if (subscriberID == null)
			subscriberID = task.getString(param_MSISDN);
		task.setObject(param_subscriberID, subscriberID);

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
		rbtDetailsRequest.setMode(task.getString(param_MODE));
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
		subscriber.setPrepaid(false);
		if(CacheManagerUtil.getParametersCacheManager().getParameterValue("SMS", "DEFAULT_SUB_TYPE", "POSTPAID").toUpperCase().startsWith("PRE"))
			subscriber.setPrepaid(true);
		String prepaidStr = task.getString(param_SUB_TYPE);
		if(prepaidStr != null)
		{
			if(prepaidStr.toLowerCase().startsWith("pre"))
				subscriber.setPrepaid(true);
			else if(prepaidStr.toLowerCase().startsWith("post"))
				subscriber.setPrepaid(false);
		}	
		task.setObject(param_subscriber, subscriber);
		task.setObject(param_subscriberID, subscriber.getSubscriberID());
		return subscriber;
	}

	public static Subscriber getSubscriber(String subscriberID)
	{
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
		return subscriber;
	}

	public static Subscriber getSubscriber(String subscriberID, String mode)
	{
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
		rbtDetailsRequest.setMode(mode);
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
		return subscriber;
	}
	
	// RBT-14301 Uninor changes.
	public static String getRedirectionURL(Task task) {
		String redirectURL = null;
		String circleID = null;
		Subscriber subscriber = getSubObject(task);
		if (task.containsKey(param_CIRCLE_ID)
				|| task.containsKey(WebServiceConstants.param_circleID)) {
			circleID = task.getString(param_CIRCLE_ID);
			circleID = (circleID == null) ? task
					.getString(WebServiceConstants.param_circleID) : circleID;
				circleID = (circleIdMap!=null && !circleIdMap.isEmpty() && circleIdMap.get(circleID) != null) ? circleIdMap
						.get(circleID) : circleID;
		} else if (!subscriber.isValidPrefix()) {
			circleID = subscriber.getCircleID();
		}
		if (circleID == null) {
			logger.info("RBT:: redirectURL: " + redirectURL);
			return redirectURL;
		}
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setCircleID(circleID);
		Site site = RBTClient.getInstance().getSite(applicationDetailsRequest);
		if (site != null) {
			redirectURL = site.getSiteURL();
			if (redirectURL != null && task.containsKey(param_URL)) {
				redirectURL = reconstructRedirectURL(redirectURL,
						task.getString(param_URL));
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("RBT:: redirectURL: " + redirectURL);
		return redirectURL;
	}

	public static String reconstructRedirectURL(String redirectURL,
			String redirectSource) {
		if (redirectURL.indexOf("/rbt_sms.jsp?") != -1)
			redirectURL = Utility.findNReplaceAll(redirectURL, "/rbt_sms.jsp?",
					"");
		if (redirectURL.indexOf("/rbt_sms.jsp") != -1)
			redirectURL = Utility.findNReplaceAll(redirectURL, "/rbt_sms.jsp",
					"");
		if (redirectURL.indexOf("/rbt_gift_acknowledge.jsp") != -1)
			redirectURL = Utility.findNReplaceAll(redirectURL,
					"/rbt_gift_acknowledge.jsp", "");
		if (redirectURL.indexOf("/rbt_copy.jsp?") != -1)
			redirectURL = Utility.findNReplaceAll(redirectURL,
					"/rbt_copy.jsp?", "");
		if (redirectURL.indexOf("/rbt_copy.jsp") != -1)
			redirectURL = Utility.findNReplaceAll(redirectURL, "/rbt_copy.jsp",
					"");

		redirectURL = redirectURL + "/" + redirectSource + "?";
		return redirectURL;
	}

	public Clip getClipById(String id,String language)
	{
		Clip clip = null;
		try
		{
			if(id != null)
			{
				//int clipID = Integer.parseInt(id);
				clip = rbtCacheManager.getClip(id,language);
			}
		}
		catch(Exception e)
		{
			logger.info("RBT:: wrong clip id: " + id);
		}
		return clip;
	}

	public Clip getClipById(String id)
	{
		Clip clip = null;
		try
		{
			if(id != null)
			{
				int clipID = Integer.parseInt(id);
				clip = rbtCacheManager.getClip(clipID);
			}
		}
		catch(Exception e)
		{
			logger.info("RBT:: wrong clip id: " + id);
		}
		return clip;
	}

	public Clip getClipByAlias(String smsAlias, String language) {
		Clip clip = rbtCacheManager.getClipBySMSAlias(smsAlias, language);
		return clip;
	}

	public Clip getClipByAlias(String smsAlias) {
		Clip clip = rbtCacheManager.getClipBySMSAlias(smsAlias);
		return clip;
	}


	public Clip getClipByPromoId(String promoId,String language)
	{
		Clip clip = null;
		//Added to remove leading 0 ,from promoid if it starts with 0
		if(LeadingStringArray!=null)
			for(int i=0;i<LeadingStringArray.length;i++)
			{
				if (removeLeadingZero && promoId != null
						&& promoId.startsWith(LeadingStringArray[i]))
					promoId = promoId.substring(LeadingStringArray[i].length());
			}

		if(promoId != null)
			clip = rbtCacheManager.getClipByPromoId(promoId,language);
		return clip;
	}
	public Clip getClipByPromoId(String promoId)
	{
		Clip clip = null;
		//Added to remove leading 0 ,from promoid if it starts with 0
		if(LeadingStringArray!=null)
			for(int i=0;i<LeadingStringArray.length;i++)
			{
				if (removeLeadingZero && promoId != null
						&& promoId.startsWith(LeadingStringArray[i]))
					promoId = promoId.substring(LeadingStringArray[i].length());
			}

		if(promoId != null)
			clip = rbtCacheManager.getClipByPromoId(promoId);
		return clip;
	}

	public Category getCategoryBySMSAlias(String catAlias,String language)
	{
		return rbtCacheManager.getCategoryBySMSAlias(catAlias,language);
	}

	public Category getCategoryBySMSAlias(String catAlias)
	{
		return rbtCacheManager.getCategoryBySMSAlias(catAlias);
	}

	public Clip[] getClipByAlbum(String subId)
	{
		Clip clip[] = null;
		if(subId != null)
			clip = rbtCacheManager.getClipsByAlbum(subId);
		return clip;
	}
	public Clip[] getClipsByCatId(int CatId,String language){
		Clip clip[]=null;
		clip=rbtCacheManager.getClipsInCategory(CatId,language);
		return clip;
	}

	public Clip[] getActiveClipsByCatId(int CatId,String language){
		Clip clip[]=null;
		clip=rbtCacheManager.getActiveClipsInCategory(CatId, language);
		return clip;
	}
	public Clip[] getClipsByCatId(int CatId){
		Clip clip[]=null;
		clip=rbtCacheManager.getClipsInCategory(CatId);
		return clip;
	}
	public Clip getClipByWavFile(String wavFile,String language)
	{
		Clip clip = null;
		if(wavFile != null)
			clip = rbtCacheManager.getClipByRbtWavFileName(wavFile,language);
		return clip;
	}
	public Clip getClipByWavFile(String wavFile)
	{
		Clip clip = null;
		if(wavFile != null)
			clip = rbtCacheManager.getClipByRbtWavFileName(wavFile);
		return clip;
	}
	public Category getCategory(String id,String language)
	{
		Category category = null;
		try
		{
			if(id != null)
			{
				int categoryID = Integer.parseInt(id);
				category = rbtCacheManager.getCategory(categoryID,language);
			}
		}
		catch(Exception e)
		{
			logger.info("RBT:: wrong category id: " + id);
		}
		return category;
	}
	public Category getCategory(String id)
	{
		Category category = null;
		try
		{
			if(id != null)
			{
				int categoryID = Integer.parseInt(id);
				category = rbtCacheManager.getCategory(categoryID);
			}
		}
		catch(Exception e)
		{
			logger.info("RBT:: wrong category id: " + id);
		}
		return category;
	}
	public Category getCategory(int id,String language)
	{
		return rbtCacheManager.getCategory(id,language);
	}

	public Category getCategory(int id)
	{
		return rbtCacheManager.getCategory(id);
	}
	public Category getCategoryByPromoId(String promoId,String language)
	{
		//Added to remove leading 0 ,from promoid if it starts with 0
		if(LeadingStringArray!=null)
			for(int i=0;i<LeadingStringArray.length;i++)
			{
				if (removeLeadingZero && promoId != null
						&& promoId.startsWith(LeadingStringArray[i]))
					promoId = promoId.substring(LeadingStringArray[i].length());
			}
		Category category = null;
		try
		{
			if(promoId != null)
			{
				category = rbtCacheManager.getCategoryByPromoId(promoId,language);
			}
		}
		catch(Exception e)
		{
			logger.info("RBT:: wrong category id: " + promoId);
		}
		return category;
	}
	public Category getCategoryByPromoId(String promoId)
	{
		//Added to remove leading 0 ,from promoid if it starts with 0
		if(LeadingStringArray!=null)
			for(int i=0;i<LeadingStringArray.length;i++)
			{
				if (removeLeadingZero && promoId != null
						&& promoId.startsWith(LeadingStringArray[i]))
					promoId = promoId.substring(LeadingStringArray[i].length());
			}
		Category category = null;
		try
		{
			if(promoId != null)
			{
				category = rbtCacheManager.getCategoryByPromoId(promoId);
			}
		}
		catch(Exception e)
		{
			logger.info("RBT:: wrong category id: " + promoId);
		}
		return category;
	}
	public Category getCategories(String id,String cirid,String language)
	{
		Category category = null;
		try
		{
			if(id != null)
			{
				int categoryID = Integer.parseInt(id.trim());
				logger.info("RBT:: category id: " + categoryID);
				//category = rbtCacheManager.getActiveCategoriesInCircle(cirid,categoryID, 'b');
				category=rbtCacheManager.getCategory(categoryID,language);
			}
		}
		catch(Exception e)
		{
			logger.info("RBT:: wrong category id: " + id);
		}
		return category;
	}
	public Category getCategories(String id,String cirid)
	{
		Category category = null;
		try
		{
			if(id != null)
			{
				int categoryID = Integer.parseInt(id.trim());
				logger.info("RBT:: category id: " + categoryID);
				//category = rbtCacheManager.getActiveCategoriesInCircle(cirid,categoryID, 'b');
				category=rbtCacheManager.getCategory(categoryID);
			}
		}
		catch(Exception e)
		{
			logger.info("RBT:: wrong category id: " + id);
		}
		return category;
	}
	public Category[] getCategoriesByType (String categoryType,String language)
	{
		return rbtCacheManager.getCategoryByType(categoryType,language);
	}

	public Category[] getCategoriesByType (String categoryType)
	{
		return rbtCacheManager.getCategoryByType(categoryType);
	}

	protected boolean isSubClassTypeValid(String subClassType) {

		return false;
	}

	public  CopyData[] getCopyData(Task task){
		CopyRequest copyRequest=new CopyRequest(task.getString(param_CALLERID),task.getString(param_subscriberID));
		CopyDetails copyDetails = rbtClient.getCopyData(copyRequest); 
		CopyData copydata[]=  copyDetails.getCopyData();
		return copydata;
	}
	protected static boolean cancelcopyViraldata(Task task)
	{
		boolean ret=false;
		DataRequest viraldataRequest=new DataRequest(task.getString(param_CALLER_ID),task.getString(param_SMSTYPE));
		viraldataRequest.setSubscriberID(task.getString(param_subscriberID));
		viraldataRequest.setDuration((Integer) task.getObject(param_WAITTIME));
		rbtClient.removeViralData(viraldataRequest);
		if(viraldataRequest.getResponse().equalsIgnoreCase("success"))
			ret=true;
		return ret;
	}

	public String removeViraldata(long smsID)
	{
		DataRequest dataRequest = new DataRequest(null);
		dataRequest.setSmsID(smsID);
		rbtClient.removeViralData(dataRequest);
		return dataRequest.getResponse();
	}

	public String removeViraldata(long smsID, String type)
	{
		DataRequest dataRequest = new DataRequest(null);
		dataRequest.setType(type);
		dataRequest.setSmsID(smsID);
		rbtClient.removeViralData(dataRequest);
		return dataRequest.getResponse();
	}

	public String removeViraldata(String subscriberID, String type)
	{
		DataRequest dataRequest = new DataRequest(null);
		dataRequest.setSubscriberID(subscriberID);
		dataRequest.setType(type);
		rbtClient.removeViralData(dataRequest);
		return dataRequest.getResponse();
	}

	protected String removeViraldata(String subscriberID, String callerID,
			String type)
	{
		DataRequest dataRequest = new DataRequest(callerID, type);
		dataRequest.setSubscriberID(subscriberID);
		rbtClient.removeViralData(dataRequest);
		return dataRequest.getResponse();
	}

	protected static ViralData addViraldata(Task task)
	{
		int count=0;
		DataRequest viraldataRequest=new DataRequest(task.getString(param_callerid),task.getString(param_SMSTYPE));
		viraldataRequest.setSubscriberID(task.getString(param_subscriberID));
		viraldataRequest.setClipID(task.getString(param_CLIPID));
		if(task.getString(param_SEARCHCOUNT)!=null)
			count=Integer.parseInt(task.getString(param_SEARCHCOUNT));
		viraldataRequest.setCount(count);
		viraldataRequest.setSentTime((Date)task.getObject(param_DATE));
		viraldataRequest.setMode(task.getString(param_SELECTED_BY));
		HashMap<String,String> extraInfoMap=DBUtility.getAttributeMapFromXML(task.getString(param_EXTRAINFO));
		viraldataRequest.setInfoMap(extraInfoMap);
		return rbtClient.addViralData(viraldataRequest);
	}

	protected static String updateViraldataType(String newType, String subscriberID, String smsType)
	{
		DataRequest viraldataRequest = new DataRequest(subscriberID, null,
				smsType);
		viraldataRequest.setNewType(newType);
		rbtClient.updateViralData(viraldataRequest);
		return viraldataRequest.getResponse();
	}

	protected static String updateViraldata(Task task)
	{
		int count=0;
		DataRequest viraldataRequest=new DataRequest(task.getString(param_CALLER_ID),task.getString(param_SMSTYPE));
		viraldataRequest.setSubscriberID(task.getString(param_subscriberID));
		viraldataRequest.setSentTime((Date)task.getObject(param_DATE));
		viraldataRequest.setNewType(task.getString(param_SMSTYPE));
		if(task.containsKey(param_SENT_TIME));
		viraldataRequest.setSentTime((Date)task.getObject(param_SENT_TIME));
		if(task.getString(param_SEARCHCOUNT)!=null)
		{
			count=Integer.parseInt(task.getString(param_SEARCHCOUNT));
			viraldataRequest.setCount(new Integer(count));
		}
		// Added to add copy confirm mode info
		if (task.containsKey(param_mode))
			if (task.getString(param_mode) != null) {
				viraldataRequest.setMode(task.getString(param_mode));
			}
		viraldataRequest.setNewType(task.getString(param_CHANGE_TYPE));
		viraldataRequest.setDuration((Integer) task.getObject(param_WAITTIME));
		String updateSMSIdStr = task.getString(param_update_sms_id);
		boolean isProfileMenuListingAllowed = param(SMS, PROFILE_SET_ALLOWED_BY_INDEX, false);
		if(isProfileMenuListingAllowed){
			String clipID = task.getString(param_clipid);
			logger.info("Viral clip Id = "+clipID);
			viraldataRequest.setClipID(clipID);
		}

		boolean updateSmsId = false;
		if(updateSMSIdStr != null)
			updateSmsId = updateSMSIdStr.equalsIgnoreCase("TRUE") || updateSMSIdStr.equalsIgnoreCase("YES");
		viraldataRequest.setUpdateSmsID(updateSmsId);
		rbtClient.updateViralData(viraldataRequest);
		return viraldataRequest.getResponse();
	}

	protected static String updateSubscriberSelection(Task task){
		SelectionRequest selectionRequest =new SelectionRequest(task.getString(param_SUBID));
		selectionRequest.setInfo("RENEW");
		selectionRequest.setClipID(task.getString(param_CLIPID));
		rbtClient.updateSubscriberSelection(selectionRequest);

		return selectionRequest.getResponse();
	}

	protected static String upgradeSelectionPack(Task task)
	{
		SelectionRequest selectionRequest = new SelectionRequest(task.getString(param_SUBID));
		selectionRequest.setClipID(task.getString(param_CLIPID));
		selectionRequest.setMode(task.getString(param_MODE));

		rbtClient.upgradeAllSelections(selectionRequest);

		return selectionRequest.getResponse();
	}

	protected String getParameter(String type,String paramName) {
		Parameters param = parameterCacheManager.getParameter(type, paramName);
		if (param != null){
			String value = param.getValue();
			if (value != null) return value.trim();
		}
		return null;
	}

	protected boolean getParamAsBoolean(String type,String param, String defaultVal) {
		try {
			return parameterCacheManager.getParameter(type,
					param, defaultVal).getValue().equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.info("getParameterAsBoolean unable to get param ->" + param
					+ " returning defaultVal >" + defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	protected String getParamAsString(String type, String param,
			String defaultValue) {
		try {
			Parameters parameter = parameterCacheManager.getParameter(type, param,
					defaultValue);
			if(parameter != null) 
				return parameter.getValue();
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type
					+ ". Returning defVal > " + defaultValue);			
		}
		return defaultValue;
	}

	protected void setParameter(String type,String paramName,String val) {
		parameterCacheManager.updateParameter(type, paramName, val);		
	}
	public  static String getSms(String type, String paramName, String defaultVal) {
		if(smsTexts == null)
			smsTexts = rbtClient.getSMSTexts(new ApplicationDetailsRequest());
		if(paramName != null)
			paramName = type+"_"+paramName;
		for(int i = 0; smsTexts != null && i < smsTexts.length; i++)
		{
			String bulkPromoId = smsTexts[i].getType();
			HashMap<String, String> conditionMap = smsTexts[i].getSmsConditionMap();
			if(conditionMap != null && conditionMap.size() > 0)
			{
				Iterator<String> it = conditionMap.keySet().iterator();
				while(it.hasNext())
				{
					String thisName = bulkPromoId;
					String key = it.next();
					String value = conditionMap.get(key);
					if(key != null && !key.equalsIgnoreCase(""))
						thisName += "_"+key;
					if(paramName.equalsIgnoreCase(thisName))
						return value;
				}
			}
		}
		return defaultVal;
	}

	public static Retailer retailer(Task task)
	{
		String retailerID = ((Subscriber)task.getObject(param_subscriber)).getSubscriberID();
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setRetailerID(retailerID);
		Retailer retailer = (Retailer) rbtClient.getRetailer(applicationDetailsRequest);
		task.setObject(param_retailer, retailer);
		return retailer;
	}

	public static Settings getSettings(Task task)
	{
		String subscriberID = ((Subscriber)task.getObject(param_subscriber)).getSubscriberID();
		RbtDetailsRequest rbtDetailsRequest  = new RbtDetailsRequest(subscriberID);
		Library library = rbtClient.getLibrary(rbtDetailsRequest);
		return library.getSettings();
	}

	public static Downloads getDownloads(Task task)
	{
		String subscriberID = ((Subscriber)task.getObject(param_subscriber)).getSubscriberID();
		RbtDetailsRequest rbtDetailsRequest  = new RbtDetailsRequest(subscriberID);
		rbtDetailsRequest.setMode(task.getString(param_mode));
		Library library = rbtClient.getLibrary(rbtDetailsRequest);
		return library.getDownloads();
	}

	public void sendSMS(Task task){
		UtilsRequest utilsRequest=new UtilsRequest(task.getString(param_Sender),task.getString(param_Reciver),task.getString(param_Msg));
		rbtClient.sendSMS(utilsRequest);
	}

	public String getSMSTextForID(Task task, String SMSID, String defaultText) {

		String revRBT = task.getString(param_revrbt);
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			SMSID = "REV_" + SMSID;
		/*String smsText = null;
		if (!m_smsTable.containsKey(SMSID))
			return defaultText;

		smsText = (String) m_smsTable.get(SMSID);

		if (smsText != null && smsText.length() > 0)
			return smsText;
		else
			return defaultText;*/
		String language=null;		
		String smsText=CacheManagerUtil.getSmsTextCacheManager().getSmsText(SMSID, language);
		if(smsText!=null)
			return smsText;
		else
			return defaultText;
	}

	public void loadSMSTexts() {

		Hashtable<String, String> smsTable = new Hashtable<String, String>();
		if(smsTexts == null)
			smsTexts = rbtClient.getSMSTexts(new ApplicationDetailsRequest());
		if (smsTexts!= null && smsTexts.length > 0) {

			for(int i = 0; smsTexts != null && i < smsTexts.length; i++)
			{
				String bulkPromoId = smsTexts[i].getType();
				HashMap<String, String> conditionMap = smsTexts[i].getSmsConditionMap();
				if(conditionMap != null && conditionMap.size() > 0)
				{
					Iterator<String> it = conditionMap.keySet().iterator();
					while(it.hasNext())
					{
						String thisName = bulkPromoId;
						String key = (String)it.next();
						String value = conditionMap.get(key);
						if(key != null && !key.equalsIgnoreCase(""))
							thisName += "_"+key;
						smsTable.put(thisName, value);
					}
				}
			}
			m_smsTable = smsTable;

		}
	}
	protected void getCategoryAndClipForID(String token, Task task) {

		Subscriber sub=(Subscriber)task.getObject(param_subscriber);
		Clip clip = null;
		Category category = null;
		Clip clipMinimal = null;
		if (token == null)
			return;
		if (token.startsWith("C")) {
			try {
				category =rbtCacheManager.getCategory(Integer.parseInt(token.substring(1)),sub.getLanguage());
				Clip[] categoryClips =rbtCacheManager.getActiveClipsInCategory(category.getCategoryId());
				clip = categoryClips[0];
			} catch (Exception e) {
			}
		} else
			clipMinimal = rbtCacheManager.getClip(token,sub.getLanguage());
		if (clipMinimal == null)
			clipMinimal = clip;
		task.setObject(CLIP_OBJ, clip);
		task.setObject(CAT_OBJ, category);
		task.setObject(param_catid, category.getCategoryId()+"");
		task.setObject(param_clipid, clip.getClipId()+"");
	}

	private boolean isSmsAliasConfigured(String smsAlias) {
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter(PROVISIONING, SMS_ALIAS_STRINGS);
		boolean aliasConfigured = false;
		if (param == null || param.getValue() == null || param.getValue().trim().equals("")) {
			aliasConfigured = false;
		} else {
			String[] tokens = param.getValue().toLowerCase().split(",");
			for (int indx = 0; indx < tokens.length; indx++) {
				if (tokens[indx].equals(smsAlias)) {
					aliasConfigured = true;
					break;
				}
			}
		}
		return aliasConfigured;
	}

	public boolean getCategoryAndClipForPromoID(Task task, String token)
	{
		//Added to remove leading 0 ,from promoid if it starts with 0
		if(token == null)
			return false;
		Subscriber sub = (Subscriber)task.getObject(param_subscriber);
		token = token.trim();
		if(token.length() > 2 && token.toLowerCase().startsWith("wt"))
			token = token.substring(2)	;
		Clip clip = getClipByPromoId(token,sub.getLanguage());
		if(clip == null && param(SMS,CHECK_CLIP_SMS_ALIAS,false))
		{
			//clip = getClipByAlias(token,sub.getLanguage());			
			String smsAliasLower = token.toLowerCase();
			if (isSmsAliasConfigured(smsAliasLower)) {
				String alias = smsAliasLower + sub.getCircleID().toLowerCase();
				clip = getClipByAlias(alias, sub.getLanguage());
				logger.debug("Fetch for SmsAlias: " + alias
						+ " and found Clip: " + clip);
			} else {
				clip = getClipByAlias(smsAliasLower, sub.getLanguage());
				logger.debug("Fetch for SmsAlias: " + smsAliasLower
						+ " and found Clip: " + clip);
			}

			/*
			 * Trim the SMS alias string with the configured list of strings
			 * one by one and search the clip. 
			 */
			if (null == clip) {
				String clipSmsAliasesStr = param(SMS, CLIP_SMS_ALIASES, null);
				if (null != clipSmsAliasesStr && clipSmsAliasesStr.length() > 0) {
					String[] clipSmsAliasesArr = clipSmsAliasesStr.split("\\,");
					for (String clipSmsAlias : clipSmsAliasesArr) {
						logger.debug("Fetching for clip Sms Alias: "
								+ clipSmsAlias);

						if (smsAliasLower.startsWith(clipSmsAlias)) {
							String s = smsAliasLower.replaceFirst(clipSmsAlias,
									"");
							clip = getClipByAlias(s.trim().toLowerCase(), sub
									.getLanguage());
							if (clip != null) {
								logger.debug("Found clip for Sms Alias: " + s);
								break;
							}
						}
					}
				} else {
					logger.debug("Could not check clip sms by aliases."
							+ " CLIP_SMS_ALIASES is null or empty");
				}
			}
		}

		if (clip == null)
		{

			Category category = getCategoryByPromoId(token,sub.getLanguage());
			if(category == null && param(SMS,CHECK_CATEGORY_SMS_ALIAS,false)) {
				category = getCategoryBySMSAlias(token,sub.getLanguage());

				//RBT-13056
				String CategorySmsAliasesStr = param(SMS, CATEGORY_SMS_ALIASES, null);
				if (category == null && CategorySmsAliasesStr != null && CategorySmsAliasesStr.length() > 0){
					String[] CategorySmsAliasesArr = CategorySmsAliasesStr.split("\\,");
					for (String CategorySmsAliases : CategorySmsAliasesArr){
						logger.debug("Fetching for category Sms Alias: "
								+ CategorySmsAliases );
						String smsAliasLower = token.toLowerCase();
						if (smsAliasLower.startsWith(CategorySmsAliases)){
							String s = smsAliasLower.replaceFirst(CategorySmsAliases,
									"");
							category = getCategoryBySMSAlias(s.trim().toLowerCase(), sub
									.getLanguage());
							if (category != null) {
								logger.debug("Found category for Sms Alias: " + s);
								break;
							}	
						}					
					}
				}	
			}

			if (category != null && ((category.getCategoryEndTime()!= null && category.getCategoryEndTime().getTime() > System.currentTimeMillis()) || RBTParametersUtils.getParamAsBoolean("COMMON",
					"SELECTION_MODEL_PARAMETER", "FALSE")))
			{
				Clip[] clips = getActiveClipsByCatId(category.getCategoryId(),sub.getLanguage());
				if (clips != null && clips[0] != null) {
					task.setObject(SG_CAT_ID, category.getCategoryId());
					task.setObject(CLIP_OBJ,clips[0]);
					task.setObject(CAT_OBJ, category);
					task.setObject(param_catid, category.getCategoryId()+"");
					task.setObject(param_clipid, clips[0].getClipId()+"");

					return true;
				}
			}
		}
		else
		{
			task.setObject(CLIP_OBJ,clip);
			task.setObject(param_clipid, clip.getClipId()+"");
			return true;
		}

		/*if (clip != null && param(SMS, CHECK_CLIP_PROMO_ID, false)) {
			task.setObject(CLIP_OBJ, clip);
			task.setObject(param_clipid, clip.getClipId() + "");
			return true;
		}*/
		return false;

	}
	public void confirmActNSel(Task task){}
	public void confirmRequestActNSel(Task task){}
	public void processDoubleConfirmation(Task task){}

	public Subscriber processActivation(Task task)
	{
		logger.debug("Process activation. task: " + task);
		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();

		String activatedBy = task.getString(param_actby);
		boolean isPackUpgradeReq = Boolean.parseBoolean(task.getString(param_upgrade));
		String modesStr = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REACTIVATION_SUPPORTED_MODES", "");
		List<String> supportedModes = Arrays.asList(modesStr.split(","));
		boolean allowUser = supportedModes.contains(activatedBy);

		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				||status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)  
				|| task.getString(param_scratchCardNo) != null || task.containsKey(param_isdirectact) || allowUser || isPackUpgradeReq){

			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());

			if(task.getString(param_scratchCardNo) == null && !task.containsKey(param_isdirectact))
			{
				String offerID = task.getString(param_offerID);
				if(offerID != null){
					subscriptionRequest.setOfferID(offerID);
				}
				else if(!task.containsKey(param_alreadyGetBaseOffer) && (param(COMMON, iRBTConstant.ALLOW_GET_OFFER, false) || param(COMMON, iRBTConstant.ALLOW_ONLY_BASE_OFFER, false)))
				{
					Offer offer = getBaseOffer(task);
					if(offer != null) {
						subscriptionRequest.setOfferID(offer.getOfferID());
						task.setObject(param_subclass, offer.getSrvKey());
						subscriptionRequest.setSubscriptionClass(offer.getSrvKey());
						//removing base cos id for JIRA RBT-7042
						if(param(COMMON, iRBTConstant.ALLOW_ONLY_BASE_OFFER, false)) {
							task.remove(param_cosid);
							task.remove(param_COSID);
						}
					}
				}
			}
			subscriptionRequest.setMode(task.getString(param_actby));
			subscriptionRequest.setIsPrepaid(task.getObject(param_isPrepaid) == null ? subscriber.isPrepaid() : (Boolean)task.getObject(param_isPrepaid));
			subscriptionRequest.setModeInfo(task.getString(param_actInfo));
			subscriptionRequest.setOperatorUserInfo(subscriber.getOperatorUserInfo());
			// RBT-14301: Uninor MNP changes.
			if (task.containsKey(param_CIRCLE_ID)) {
				subscriptionRequest
						.setCircleID(task.getString(param_CIRCLE_ID));
			} else {
				subscriptionRequest.setCircleID(subscriber.getCircleID());
			}
			subscriptionRequest.setRefId(task.getString(param_refID));
			String activationMode=task.getString(param_actMode);
			if(activationMode!=null&&!(activationMode.equals("")))
			{
				subscriptionRequest.setActivationMode(activationMode);
			}
			subscriptionRequest.setSubscriptionClass(task.getString(param_subclass));
			subscriptionRequest.setRbtType((Integer)task.getObject(param_rbttype));
			subscriptionRequest.setFreePeriod((Integer)task.getObject(param_freeperiod));
			if(task.getString(param_scratchCardNo) != null)
			{
				subscriptionRequest.setScratchCardNo(task.getString(param_scratchCardNo));	
				subscriptionRequest.setPreCharged(true);
			}

			if(task.getObject(param_COSID) != null){
				try{
					subscriptionRequest.setCosID(Integer.valueOf(task.getString(param_COSID)));
				}catch (Exception e) {
					subscriptionRequest.setSubscriptionClass(task.getString(param_subclass));
				}
			}
			else
				subscriptionRequest.setSubscriptionClass(task.getString(param_subclass));

			if (task.getObject(param_PACK_COSID) != null){
				subscriptionRequest.setPackCosId(Integer.valueOf(task.getString(param_PACK_COSID)));
			}

			if (task.getObject(param_rentalPack) != null) {
				String subClass = task.getString(param_rentalPack);
				subscriptionRequest.setRentalPack(subClass);
				logger.info("Upgrading subscriber to subscriptionClass: "
						+ subClass);
			}

			if(task.containsKey(param_userInfoMap))
				subscriptionRequest.setUserInfoMap((HashMap<String, String>)task.getObject(param_userInfoMap));
			if(task.containsKey(param_REFUND) && task.getString(param_REFUND).equalsIgnoreCase("true")) {
				HashMap<String, String> map = subscriptionRequest.getUserInfoMap();
				if(map == null)
					map = new HashMap<String, String>();
				map.put("REFUND", "TRUE");
				subscriptionRequest.setUserInfoMap(map);
			}
			if(task.containsKey(param_isdirectact) && task.getString(param_isdirectact).equalsIgnoreCase("true"))
				subscriptionRequest.setIsDirectActivation(true);

			HashMap<String, String> map = subscriptionRequest.getUserInfoMap();
			if(map == null) {
				map = new HashMap<String, String>();
			}

			if(task.containsKey(param_CONSENT_LOG)) {
				map.put("CONSENT_LOG", task.getString(param_CONSENT_LOG));
			}
			if(task.containsKey(EXTRA_INFO_OFFER_ID)) {
				map.put(EXTRA_INFO_OFFER_ID, task.getString(EXTRA_INFO_OFFER_ID));
			}
			if (task.containsKey(EXTRA_INFO_INTRO_PROMPT_FLAG)) {
				map.put(EXTRA_INFO_INTRO_PROMPT_FLAG, task.getString(EXTRA_INFO_INTRO_PROMPT_FLAG));
			}
			if (task.containsKey(CAMPAIGN_CODE)) {
				map.put(CAMPAIGN_CODE, task.getString(CAMPAIGN_CODE));
			}
			if (task.containsKey(TREATMENT_CODE)) {
				map.put(TREATMENT_CODE, task.getString(TREATMENT_CODE));
			}
			if (task.containsKey(OFFER_CODE)) {
				map.put(OFFER_CODE, task.getString(OFFER_CODE));
			}

			if(task.containsKey(EXTRA_INFO_TPCGID)) {
				map.put(EXTRA_INFO_TPCGID, task.getString(EXTRA_INFO_TPCGID));
			}

			String udsOn = task.getString(UDS_ON);
			//JIRA-ID: RBT-13626
			if (udsOn != null) {
				map.put(UDS_OPTIN, udsOn);
			}

			//Added for RBT-17883
			if(task.containsKey(param_ChargeMDN)){
				if(task.getString(param_ChargeMDN)!=null)
				map.put(EXTRA_INFO_CHARGE_MDN, task.getString(param_ChargeMDN));
			}

			if(map != null && map.size() > 0) {
				subscriptionRequest.setUserInfoMap(map);
			}

			if(task.containsKey(WebServiceConstants.param_isPreConsentBaseRequest)) {
				subscriptionRequest.setConsentInd(true);
				Rbt rbt = rbtClient.activateSubscriberPreConsent(subscriptionRequest);
				response = subscriptionRequest.getResponse();
				if(rbt != null)
					task.setObject("consentObj", rbt.getConsent());
			} else {
				subscriber=rbtClient.activateSubscriber(subscriptionRequest);
				response = subscriptionRequest.getResponse();
			}
			if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
				response = "success";
			}else if (response.equalsIgnoreCase(WebServiceConstants.INVALID)){
				response = "invalid";
			}else if (response.equalsIgnoreCase(WebServiceConstants.ACTIVATION_BLOCKED)){
				response = activationBlocked;
			}else if (response.equalsIgnoreCase(WebServiceConstants.BASE_OFFER_NOT_AVAILABLE)){
				response = baseOfferNotFound;
			}else{
				response = technicalFailure;
			}
		}else if(status.equalsIgnoreCase(WebServiceConstants.ACTIVE)){
			response = alreadyActive;
		}else if(status.equalsIgnoreCase(WebServiceConstants.SUSPENDED)){
			response = suspended;
		}else if (status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)){
			response = activationPending;	
		}else if (status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)){
			response = deactivationPending;
		}else if (status.equalsIgnoreCase(WebServiceConstants.GIFTING_PENDING)){
			response = giftingPending;
		}else if (status.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)){
			response = renewalPending;
		}else if (status.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED)){
			response = blackListedSMSText;
		}else if (status.equalsIgnoreCase(WebServiceConstants.INVALID_PREFIX)){
			response = invalidPrefix;
		}else if (status.equalsIgnoreCase(WebServiceConstants.COPY_PENDING)){
			response = expressCopyPending;
		}else{
			response = HELP;
		}

		task.setObject(param_response, response);
		return subscriber;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processDeactivation(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String processDeactivation(Task task)
	{
		logger.info("RBT:: processDeactivation : " + task);
		logger.info("RBT:: processDeactivation 2: " + task);
		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		logger.info("RBT:: processDeactivation status is : " + status);

		boolean isDirectDeactivation = false;
		if(task.containsKey(param_isdirectdct) && task.getString(param_isdirectdct).equalsIgnoreCase("true"))
			isDirectDeactivation = true;

		try{
			if (isDirectDeactivation || status.equalsIgnoreCase(WebServiceConstants.ACTIVE)
					|| status.equalsIgnoreCase(WebServiceConstants.LOCKED)
					|| status.equalsIgnoreCase(WebServiceConstants.SUSPENDED)
					|| status.equalsIgnoreCase(WebServiceConstants.ACTIVATION_SUSPENDED)
					|| status.equalsIgnoreCase(WebServiceConstants.GRACE)
					|| status.equalsIgnoreCase(WebServiceConstants.RENEWAL_GRACE)
					|| (CacheManagerUtil.getSmsTextCacheManager().getSmsText(
							"GRACE_DEACT_CONFIRM_SMS",
							subscriber.getLanguage()) != null && status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING))) {
				logger.info("RBT:: processDeactivation *** in loop 1 : " );
				SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());
				subscriptionRequest.setMode(task.getString(param_actby));
				String activationMode=task.getString(param_actMode);
				if(activationMode!=null&&!(activationMode.equals("")))
					subscriptionRequest.setMode(activationMode);

				subscriptionRequest.setIsPrepaid((Boolean)task.getObject(param_isPrepaid));
				subscriptionRequest.setModeInfo(task.getString(param_actInfo));
				subscriptionRequest.setSubscriptionClass(task.getString(param_subclass));
				subscriptionRequest.setRbtType((Integer)task.getObject(param_rbttype));
				subscriptionRequest.setFreePeriod((Integer)task.getObject(param_freeperiod));

				subscriptionRequest.setIsDirectDeactivation(isDirectDeactivation);
				if (getParamAsString(SMS, "SMS_CHURN_PORTAL_USING_UNSUB_DELAY",
						"FALSE").equalsIgnoreCase("TRUE")) {
					subscriptionRequest.setUnsubDelayDct(true);
				}
				if(task.containsKey(param_isDelayDeactForUpgrade)){ 
					subscriptionRequest.setDelayDeactForUpgardation((Boolean)task.getObject(param_isDelayDeactForUpgrade));
				}
				//Added extra info in the task to update the sr_id and originator info 
				// as per the jira id RBT-11962
				if(task.containsKey(param_userInfoMap)){
					subscriptionRequest.setUserInfoMap((HashMap<String, String>)task.getObject(param_userInfoMap));
				}
				rbtClient.deactivateSubscriber(subscriptionRequest);
				response = subscriptionRequest.getResponse();
				logger.info("RBT:: processDeactivation *** response is : " + response);
				if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
					response = "success";
				}else if (response.equalsIgnoreCase(WebServiceConstants.DCT_NOT_ALLOWED)){
					response = technicalFailure;
				}else if (response.equalsIgnoreCase("UPGRADE_SUCCESS_ON_DELAY_DCT")){
					response = "UPGRADE_SUCCESS";
				}else if (response.equalsIgnoreCase(WebServiceConstants.NOT_AN_ADRBT_USER)) {
					response = WebServiceConstants.NOT_AN_ADRBT_USER;
				}else if (response.equalsIgnoreCase(WebServiceConstants.ADRBT_USER)) {
					response = WebServiceConstants.ADRBT_USER;
				}
				else{
					response = technicalFailure;
				}
			}else if(status.equalsIgnoreCase(WebServiceConstants.NEW_USER)||status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)){
				response = notActiveText;
			}else if (status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)){
				response = activationPending;	
			}else if (status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)){
				response = deactivationPending;
			}else if (status.equalsIgnoreCase(WebServiceConstants.GIFTING_PENDING)){
				response = giftingPending;
			}else if (status.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)){
				response = renewalPending;
			}else if (status.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED)){
				response = blackListedSMSText;
			}else if (status.equalsIgnoreCase(WebServiceConstants.INVALID_PREFIX)){
				response = invalidPrefix;
			}else if (status.equalsIgnoreCase(WebServiceConstants.COPY_PENDING)){
				response = expressCopyPending;
			}else{
				response = HELP;
			}
		}
		catch(Exception e)
		{
			logger.info("RBT:: processDeactivation : " + e.getMessage());
		}
		logger.info("RBT:: processDeactivation *** response is : " + response);
		task.setObject(param_response, response);
		return response;
	}

	public void  processDelayDeactivation(Task task) {}

	public String processDeactivationPack(Task task)
	{
		logger.info("RBT:: processDeactivation : " + task);
		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		logger.info("RBT:: processDeactivationPack status is : " + status);

		try
		{
			logger.info("RBT:: processDeactivation *** in loop 1 : " );
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());
			subscriptionRequest.setMode(task.getString(param_actby));
			subscriptionRequest.setIsPrepaid((Boolean)task.getObject(param_isPrepaid));
			subscriptionRequest.setModeInfo(task.getString(param_actInfo));
			subscriptionRequest.setPackCosId((Integer)task.getObject(param_cosid));
			//Added for VB-380
			subscriptionRequest.setInternalRefId((String)task.getObject(param_refID));
			rbtClient.deactivatePack(subscriptionRequest);
			response = subscriptionRequest.getResponse();
			logger.info("RBT:: processDeactivationPack *** response is : " + response);
		}
		catch(Exception e)
		{
			logger.info("RBT:: processDeactivation : " + e.getMessage());
		}
		logger.info("RBT:: processDeactivation *** response is : " + response);
		task.setObject(param_response, response);
		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processSelection(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String processAddSelection(Task task)
	{
		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);

		SelectionRequest selectionRequest = new SelectionRequest(task.getString(param_subscriberID));
		selectionRequest.setMode(task.getString(param_actby));
		selectionRequest.setCallerID(task.getString(param_callerid));
		selectionRequest.setCategoryID(task.getString(param_catid));
		selectionRequest.setClipID(task.getString(param_clipid));
		selectionRequest.setIsPrepaid((Boolean)task.getObject(param_isPrepaid));
		selectionRequest.setModeInfo(task.getString(param_actInfo));
		selectionRequest.setSubscriptionClass(task.getString(param_subclass));
		selectionRequest.setOptInOutModel(task.getString(param_optin));
		//selectionRequest.setToTime(((Date)task.getObject(param_enddate)).toString());
		selectionRequest.setStatus((Integer)task.getObject(param_status));

		//promotype, song,maxselection, reqtype,substatus
		//selectionRequest.setCircleID(subscriber.getCircleID());
		// RBT-14301: Uninor MNP changes.
		if (task.containsKey(param_CIRCLE_ID)) {
			selectionRequest.setCircleID(task.getString(param_CIRCLE_ID));
		} else {
			selectionRequest.setCircleID(subscriber.getCircleID());
		}
		selectionRequest.setInLoop(true);
		rbtClient.addSubscriberSelection(selectionRequest);
		response = selectionRequest.getResponse();
		return response;
	}

	public void processDeleteSelection(Task task)
	{
		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());

		rbtClient.deleteSubscriberSelection(selectionRequest);
		response = selectionRequest.getResponse();
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
			response ="success";
		}else if (response.equalsIgnoreCase(WebServiceConstants.SUSPENDED) || 
				response.equalsIgnoreCase(WebServiceConstants.SELECTION_SUSPENDED)){
			response = suspended;
		}else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_NOT_EXISTS)){
			response = clipNotAvailable;
		}else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_EXPIRED)){
			response = clipExpired;
		}else if (response.equalsIgnoreCase(WebServiceConstants.FAILED)){
			response = technicalFailure;
		}else if (response.equalsIgnoreCase(WebServiceConstants.ALREADY_EXISTS)){
			response = selAlreadyExists;
		}else if (response.equalsIgnoreCase(WebServiceConstants.TECHNICAL_DIFFICULTIES)){
			response = technicalFailure;
		}else if (response.equalsIgnoreCase(WebServiceConstants.ALREADY_MEMBER_OF_GROUP)){
			response = technicalFailure;
		}else if (response.equalsIgnoreCase(WebServiceConstants.NOT_ALLOWED)){
			response = technicalFailure;
		}else{
			response = HELP;
		}

		task.setObject(param_response, response);
	}
	public static boolean isUserActive(String subscriberStatus)
	{
		if (subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.LOCKED)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.GRACE)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
			return true;

		return false;
	}

	protected String getSMSParameter(String paramName)
	{
		Parameters param = parameterCacheManager.getParameter(iRBTConstant.SMS,
				paramName);
		if (param != null)
		{
			String value = param.getValue();
			if (value != null)
				return value.trim();
		}
		return null;
	}

	protected static boolean updateSubscriber(Task task){
		boolean ret=false;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		SubscriptionRequest subreq=new SubscriptionRequest(subscriber.getSubscriberID());
		subreq.setSubscriptionClass(task.getString(param_subclass));
		subreq.setInfo(WebServiceConstants.TNB_TO_NORMAL);
		rbtClient.updateSubscription(subreq);
		if(subreq.getResponse().equalsIgnoreCase("success"))
			ret=true;
		return ret;
	}
	public void processDeactivateSubscriberRecords(Task task){
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
		selectionRequest.setMode(task.getString(param_actby));
		selectionRequest.setIsPrepaid((Boolean)task.getObject(param_isPrepaid));
		selectionRequest.setModeInfo(task.getString(param_actInfo));
		selectionRequest.setCallerID(task.getString(param_callerid));
		selectionRequest.setSubscriptionClass(task.getString(param_subclass));
		int status=Integer.parseInt(task.getString(param_status));
		selectionRequest.setStatus(status);
		rbtClient.deleteSubscriberSelection(selectionRequest);
	}
	//For SMS Viral START/STOP Feature
	public void processViralStart(Task task){};
	public void processViralStop(Task task){};

	//For Randomization and UnRandomization Process
	public void enableRandomization(Task task){};
	public void disableRandomization(Task task){};
	public void processSongCodeRequest(Task task){};
	public void processCopyRequest(Task task){};
	public void processCrossCopyRequest(Task task){};
	public void processMnpCrossCopyRequest(Task task){};
	public void processCrossCopyRdcRequest(Task task){};
	public void validateAndProcessCopyRequest(Task task){};

	public void processViralOptOutRequest(Task task){};
	public void processViralOptInRequest(Task task){};

	public void processInitRandomizeRequest(Task task){};

	public void processResubscriptionRequest(Task task){};
	public void processDownloadSetRequest(Task task){};

	public String processUGCRequest(Task task){return null;};

	public String processMeraHelloTuneRequest(Task task){return null;};

	public String processUGCRequestOthers(Task task){return null;};

	public String processRbtPlayerHelperReq(Task task){return null;};
	public String processESIAQuizForwarderReq(Task task){return null;};
	public void processToneCopyReq(Task task){};
	public void processCrossGiftReq(Task task){};
	public void processSubProfileRequest(Task task){};
	public void processSubStatusRequest(Task task){};
	public void processComboSubStatusRequest(Task task){};
	public void processViralData(Task task){};
	public void processSMSText(Task task){};
	public void processGiftRequest(Task task){};
	public String getSubscriberDefaultVcode(Task task){return null;};
	public void processThirdPartyRequest(Task task){};
	public void processSupressPreRenewalSmsRequest(Task task){};
	//TODO: Add all action methods 
	//action methods argument should be Task object only
	public static ArrayList<String> tokenizeArrayList(String stringToTokenize, String delimiter)
	{
		if (stringToTokenize == null)
			return null;
		String delimiterUsed = ",";

		if (delimiter != null)
			delimiterUsed = delimiter;

		ArrayList<String> result = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(stringToTokenize,
				delimiterUsed);
		while (tokens.hasMoreTokens())
			result.add(tokens.nextToken().replace("\n", "").replace("\r", "").toLowerCase());

		return result;
	}

	public Site getSite(Task task)
	{
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String circleID = subscriber.getCircleID();
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setCircleID(circleID);
		return rbtClient.getSite(applicationDetailsRequest);
	}

	public String processSetSelection(Task task)
	{

		String requestType = task.getString(param_requesttype);
		if(requestType != null && requestType.equals(type_content_validator))
		{
			ChargeClass chargeClass = getSelectionChargeClass(task);
			if( chargeClass != null)
				task.setObject(param_ocg_charge_id, "VALID:"+chargeClass.getOperatorCode1()+":"+chargeClass.getAmount());
			else
				task.setObject(param_ocg_charge_id, "NOTVALID");

			return "OCG";
		}

		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();

		SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());

		//Added by Sreekar in alapan's laptop
		if(task.getString(param_offerID) != null){
			logger.info("Setting the tnb offer id for voda india ");
			selectionRequest.setSubscriptionOfferID(task.getString(param_offerID));
		}

		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				||status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)  
				|| task.getString(param_scratchCardNo) != null){

			if(status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
					||status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
			{
				task.setObject(param_chrgAmtOnlyForActive, true); 
				if (!task.containsKey(param_alreadyGetBaseOffer)
						&& (param(COMMON, iRBTConstant.ALLOW_GET_OFFER, false) || param(COMMON,
								iRBTConstant.ALLOW_ONLY_BASE_OFFER, false)))				{
					Offer offer = getBaseOffer(task);
					if(offer != null) {
						selectionRequest.setSubscriptionOfferID(offer.getOfferID());
						task.setObject(param_subclass, offer.getSrvKey());
						selectionRequest.setSubscriptionClass(offer.getSrvKey());

						//removing base cos id for JIRA RBT-7042
						if(param(COMMON, iRBTConstant.ALLOW_ONLY_BASE_OFFER, false)) {
							task.remove(param_cosid);
							task.remove(param_COSID);
						}

					}
				}
			}

			//			if(task.getObject(param_COSID) != null){
			//				logger.info("Setting the cosid ");
			//				selectionRequest.setCosID(Integer.valueOf(task.getString(param_COSID)));
			//			}

			selectionRequest.setMode(task.getString(param_actby));
			selectionRequest.setModeInfo(task.getString(param_actInfo));
			selectionRequest.setOperatorUserInfo(subscriber.getOperatorUserInfo());
			selectionRequest.setCircleID(subscriber.getCircleID());

			String activationMode=task.getString(param_actMode);
			if(activationMode!=null&&!(activationMode.equals("")))
			{
				selectionRequest.setActivationMode(activationMode);
			}

			selectionRequest.setSubscriptionClass(task.getString(param_subclass));
			selectionRequest.setRbtType((Integer)task.getObject(param_rbttype));
			selectionRequest.setFreePeriod((Integer)task.getObject(param_freeperiod));
			//selectionRequest.setCircleID(subscriber.getCircleID());
			// RBT-14301: Uninor MNP changes.
			if (task.containsKey(param_CIRCLE_ID)) {
				selectionRequest
						.setCircleID(task.getString(param_CIRCLE_ID));
			} else {
				selectionRequest.setCircleID(subscriber.getCircleID());
			}
			if(task.getString(param_scratchCardNo) != null)
			{
				selectionRequest.setScratchCardNo(task.getString(param_scratchCardNo));	
				selectionRequest.setPreCharged(true);
			}

			//TS-1847 : When normal user subscribing the music pack through SMS store front then only song getting activated
			//			if(task.getObject(param_COSID) != null){
			//				try{
			//					selectionRequest.setCosID(Integer.valueOf(task.getString(param_COSID)));
			//				}catch (Exception e) {
			//					selectionRequest.setSubscriptionClass(task.getString(param_subclass));
			//				}
			//			}
			//			else
			//				selectionRequest.setSubscriptionClass(task.getString(param_subclass));

			if(task.containsKey(param_userInfoMap))
				selectionRequest.setUserInfoMap((HashMap<String, String>)task.getObject(param_userInfoMap));
			if(task.containsKey(param_REFUND) && task.getString(param_REFUND).equalsIgnoreCase("true")) {
				HashMap<String, String> map = selectionRequest.getUserInfoMap();
				if(map == null)
					map = new HashMap<String, String>();
				map.put("REFUND", "TRUE");
				selectionRequest.setUserInfoMap(map);
			}
			if(task.containsKey(param_isdirectact) && task.getString(param_isdirectact).equalsIgnoreCase("true"))
				selectionRequest.setIsDirectActivation(true);

			if(task.containsKey(param_CONSENT_LOG)) {
				HashMap<String, String> map = selectionRequest.getUserInfoMap();
				if(map == null)
					map = new HashMap<String, String>();
				map.put("CONSENT_LOG", task.getString(param_CONSENT_LOG));
				selectionRequest.setUserInfoMap(map);
			}
			if(task.containsKey(EXTRA_INFO_OFFER_ID)) {
				HashMap<String, String> map = selectionRequest.getUserInfoMap();
				if(map == null)
					map = new HashMap<String, String>();
				map.put(EXTRA_INFO_OFFER_ID, task.getString(EXTRA_INFO_OFFER_ID));
				selectionRequest.setUserInfoMap(map);
			}
		}

		if (!task.containsKey(param_alreadyGetSelOffer)
				&& param(COMMON, iRBTConstant.ALLOW_GET_OFFER, false)
				&& !task.containsKey(param_isMultiChargesRequest))
		{
			Offer offer = getSelOffer(task);
			if(offer != null) {
				selectionRequest.setOfferID(offer.getOfferID());
				task.setObject(param_chargeclass, offer.getSrvKey());
				selectionRequest.setChargeClass(offer.getSrvKey());
				//Added by Sreekar for Vf-Spain on 2013-01-26
				if(offer.getSmOfferType() != null) {
					HashMap<String, String> userInfoMap = selectionRequest.getUserInfoMap();
					if(userInfoMap == null)
						userInfoMap = new HashMap<String, String>();
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_TYPE, offer.getSmOfferType());
					selectionRequest.setSelectionInfoMap(userInfoMap);
				}
			}
		}

		if(task.containsKey(param_sel_offerID)) {
			selectionRequest.setOfferID(task.getString(param_sel_offerID));			
		}

		if(task.containsKey(WebServiceConstants.param_packOfferID)) {
			logger.info("Setting the pack offer id");
			selectionRequest.setPackOfferID(task.getString(WebServiceConstants.param_packOfferID));			
		}

		if(task.getObject(param_selectionType) != null)
		{
			selectionRequest.setSelectionType( new Integer ((String)task.getObject(param_selectionType)));
			logger.info("Setting the selection type to 2");
		}

		if(task.getObject(param_rentalPack) != null)
		{
			selectionRequest.setRentalPack((String)task.getObject(param_rentalPack));
			logger.info("Setting the rental pack for existing users");
		}

		if(task.containsKey(param_USE_UI_CHARGE_CLASS)) {
			Boolean b = (Boolean)task.getObject(param_USE_UI_CHARGE_CLASS);
			selectionRequest.setUseUIChargeClass(b);
		}

		if(task.getObject(param_COSID) != null){
			try{
				selectionRequest.setCosID(Integer.valueOf(task.getString(param_COSID)));
			}catch(Exception ex){
				logger.info("COSID IS NOT VALID OR IS NULL");
			}
		}
		selectionRequest.setCircleID(subscriber.getCircleID());
		selectionRequest.setCallerID(task.getString(param_callerid));
		selectionRequest.setCategoryID(task.getString(param_catid));
		selectionRequest.setClipID(task.getString(param_clipid));
		selectionRequest.setIsPrepaid(task.getObject(param_isPrepaid) == null ? subscriber.isPrepaid() : (Boolean)task.getObject(param_isPrepaid));

		selectionRequest.setMode(task.getString(param_actby));
		if(getParamAsString("SMS","CAPTURE_SMS_TEXT","FALSE").equalsIgnoreCase("TRUE")){
			selectionRequest.setSmsSent(task.getString(param_smsSent));
		}
		selectionRequest.setModeInfo(task.getString(param_actInfo));
		selectionRequest.setSubscriptionClass(task.getString(param_subclass));
		selectionRequest.setChargeClass(task.getString(param_chargeclass));
		selectionRequest.setOptInOutModel(task.getString(param_optin));
		selectionRequest.setChargingModel(task.getString(param_chargeModel));
		selectionRequest.setInLoop(task.getString(param_inLoop) == null ? false : task.getString(param_inLoop).equalsIgnoreCase("yes") );

		if(task.getObject(param_fromTime) != null)
			selectionRequest.setFromTime( new Integer ((String)task.getObject(param_fromTime)));
		if(task.getObject(param_toTime) != null)
			selectionRequest.setToTime( new Integer ((String)task.getObject(param_toTime)));
		if(task.getObject(param_fromTimeMins) != null)
			selectionRequest.setFromTimeMinutes( new Integer ((String)task.getObject(param_fromTimeMins)));
		if(task.getObject(param_toTimeMins) != null)
			selectionRequest.setToTimeMinutes( new Integer ((String)task.getObject(param_toTimeMins)));
		if(task.getObject(param_status) != null)
			selectionRequest.setStatus( new Integer ((String)task.getObject(param_status)));
		selectionRequest.setInterval(task.getString(param_interval));
		selectionRequest.setMmContext(task.getString(param_mmContext));

		selectionRequest.setProfileHours(task.getString(param_profile_hours));
		selectionRequest.setCricketPack(task.getString(param_cricket_pack));
		selectionRequest.setTransID(task.getString(param_transid));
		logger.info("selection rerquest is : "+selectionRequest);
		/*
		 * RBT-4539: The method addSubscriberSelection() is returning Rbt object
		 * and it contains information about Gift, Library, Bookmarks etc. So,
		 * instead of omitting Rbt object, it has been taken and placed into the
		 * task.
		 */
		Rbt rbt = rbtClient.addSubscriberSelection(selectionRequest);
		task.setObject(param_rbt_object, rbt);
		response = selectionRequest.getResponse();

		if (getParamAsBoolean(SMS, "SENDING_CONSENT_SELECTION_MESSAGE_ENABLED", "FALSE")
				&& rbt != null && rbt.getLibrary() != null
				&& rbt.getLibrary().isRecentSelConsent()) {
			task.setObject(param_isSelConsentInserted, "true");

			if(rbt.getSubscriber()!=null && rbt.getSubscriber().isSubConsentInserted())
				task.setObject(param_isSubConsentInserted, "true");
		}
		/*
		 * RBT-8737:[Vodafone Spain] SMS text based on 
		 * song price for active RBT subscribers 
		 */
		if (rbt != null && rbt.getLibrary() != null
				&& !task.containsKey(param_chrgAmtOnlyForActive) && param(SMS, "SONG_CHARGE_DISPLAY", false)) {
			RecentSelection recentSelection = rbt.getLibrary()
					.getRecentSelection();
			logger.info("Recent Selection = " + recentSelection);
			if (recentSelection != null
					&& recentSelection.getClassType() != null) {
				String classType = recentSelection.getClassType();
				String amount = CacheManagerUtil.getChargeClassCacheManager()
						.getChargeClass(classType).getAmount();
				task.setObject(param_song_chrg_amt, amount);
			}
		}
		return response;
	}

	public String processDeactivateSelection(Task task)
	{
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
		selectionRequest.setMode(task.getString(param_actby));

		selectionRequest.setCallerID(task.getString(param_callerid));
		//selectionRequest.setCategoryID(task.getString(param_catid));
		String categoryId = task.getString(param_catid);
		if(categoryId != null)
		{
			Category category = getCategory(categoryId);
			int categoryType = category.getCategoryTpe();
			if (category != null
					&& (com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isShuffleCategory(categoryType) || categoryType == iRBTConstant.DYNAMIC_SHUFFLE)) {
				selectionRequest.setCategoryID(categoryId);
			}
		}
		selectionRequest.setClipID(task.getString(param_clipid));

		if(task.getObject(param_fromTime) != null)
			selectionRequest.setFromTime( new Integer ((String)task.getObject(param_fromTime)));
		if(task.getObject(param_toTime) != null)
			selectionRequest.setToTime( new Integer ((String)task.getObject(param_toTime)));
		if(task.getObject(param_status) != null)
			selectionRequest.setStatus( new Integer ((String)task.getObject(param_status)));

		selectionRequest.setInterval(task.getString(param_interval));

		selectionRequest.setModeInfo(task.getString(param_actInfo));

		rbtClient.deleteSubscriberSelection(selectionRequest);
		return selectionRequest.getResponse();
	}

	protected ViralData[] getViraldata(Task task)
	{
		DataRequest dataRequest = new DataRequest(task.getString(param_subscriberID),task.getString(param_callerid), task.getString(param_SMSTYPE));
		return rbtClient.getViralData(dataRequest);
	}

	protected String addViraldata(String subscriberID,String callerId, String type, String clipId, String mode, int count, HashMap<String, String> infoMap)
	{
		DataRequest dataRequest = new DataRequest(subscriberID,callerId, type,clipId, mode, count);
		if (infoMap != null)
			dataRequest.setInfoMap(infoMap);

		rbtClient.addViralData(dataRequest);
		return dataRequest.getResponse();
	}

	protected String removeViraldata(Task task)
	{

		DataRequest viraldataRequest=new DataRequest(task.getString(param_CALLER_ID),task.getString(param_SMSTYPE));
		viraldataRequest.setSubscriberID(task.getString(param_subscriberID));
		viraldataRequest.setClipID(task.getString(param_CLIPID));
		viraldataRequest.setDuration((Integer) task.getObject(param_WAITTIME));
		rbtClient.removeViralData(viraldataRequest);
		return viraldataRequest.getResponse();
	}

	public String processGift(Task task)
	{
		String mode = (task.getString(param_mode)!=null)?task.getString(param_mode):"SMS";
		GiftRequest giftRequest = new GiftRequest();
		giftRequest.setMode(mode);
		giftRequest.setGifteeID(task.getString(param_callerid));
		giftRequest.setGifterID(((Subscriber)task.getObject(param_subscriber)).getSubscriberID());
		giftRequest.setToneID(task.getString(param_clipid));
		giftRequest.setCategoryID(task.getString(param_catid));

		if (task.containsKey(param_isGifterConfRequired) && task.getString(param_isGifterConfRequired).equalsIgnoreCase("y"))
			giftRequest.setIsGifterConfRequired(true);
		if (task.containsKey(param_isGifteeConfRequired) && task.getString(param_isGifteeConfRequired).equalsIgnoreCase("y"))
			giftRequest.setIsGifteeConfRequired(true);

		rbtClient.sendGift(giftRequest);
		return giftRequest.getResponse();
	} 
	public void processDeactivateDownload(Task task)
	{} 
	public void processRetailerActnSel(Task task)
	{}
	public void processConfirmSubscriptionNCopy(Task task)
	{}
	public void processActNSel(Task task)
	{}
	public void processNewFeed(Task task)
	{}
	public void viewSubscriptionStatistics(Task task)
	{}
	public void processPromotion1(Task task)
	{}
	public void processPromotion2(Task task)
	{}
	public void processSongPromotion1(Task task)
	{}
	public void processSongPromotion2(Task task)
	{}
	public void processSel1(Task task)
	{}
	public void processSel2(Task task)
	{}
	public void processremoveCallerIDSel(Task task)
	{}
	public void processRemoveTempOverride(Task task)
	{}
	public void processRemoveNavraatri(Task task)
	{}
	public void processManageRemoveSelection(Task task)
	{}
	public void processREQUEST(Task task)
	{}
	public void processCancelCopyRequest(Task task)
	{}
	public void processConfirmCopyRequest(Task task)
	{}
	public void processConfirmLikeRequest(Task task)
	{}
	public void processCancelOptInCopy(Task task)
	{}
	public void processCopy(Task task)
	{}
	public void processCOPY(Task task)
	{}
	public void getGift(Task task)
	{}
	public void proceesPOLLON(Task task)
	{}
	public void processPollOFF(Task task)
	{}
	public void setNewsletterOn(Task task)
	{}
	public void setNewsLetterOff(Task task)
	{}
	public void processDisableIntro(Task task)
	{}
	public void processDisableOverlay(Task task)
	{}
	public void processEnableOverlay(Task task)
	{}
	public void processTNB(Task task)
	{}
	public void processRenew(Task task)
	{}
	public void processWeekToMonthConversion(Task task)
	{}
	public void processViralAccept(Task task)
	{}
	public void processWebRequest(Task task)
	{}
	public void processMgmAccept(Task task)
	{}
	public void processListen(Task task)
	{}
	public void processHelp(Task task)
	{}
	public void processSongOfMonth(Task task)
	{}
	public void processDownloadsList(Task task)
	{}
	public void processTNBActivation(Task task)
	{}
	public void processInitGift(Task task)
	{}
	public void processInitGiftConfirm(Task task)
	{}
	public void processPreGift(Task task)
	{}
	public void processPreGiftConfirm(Task task)
	{}

	public void processLotteryListRequest(Task task)
	{}

	public void processManage(Task task)
	{}
	public void processListProfile(Task task)
	{}
	public void getMoreClips(Task task)
	{}

	public void getMoreAzaan(Task task) {
	}

	public void processInfluencerOptin(Task task)
	{}
	public void processCricket(Task task)
	{}

	public void processReferral(Task task)
	{}

	public void processMusicPack(Task task)
	{}
	public void processOptOutRequest(Task task){}; //FOR UPDATING_DND_OF_SUBSCRIBER_WITH_SM
	public void processHSBRequest(Task task){};
	//for Upgradation of base of a subscriber
	public void processBaseUpgradationRequest(Task task){};
	// Added for Voluntary Suspension
	public void processSuspensionRequest(Task task){};
	public void processResumptionRequest(Task task){};
	public void upgradeSubscription(Task task){};

	// Blacklisting and Removing from BlackList methods
	public void addToBlackList(Task task){};
	public void removeFromBlackList(Task task){};

	// Added for Block/Unblock feature
	public void processBlockRequest(Task task){};
	public void processUnblockRequest(Task task){};

	public void processSongPackRequest(Task task){};
	public void processSpecialSongPackRequest(Task task){};
	public void processTopupRequest(Task task){};
	//RBT-12195 - User block - unblock feature.
	public void processBlockSubRequest(Task task){};
	public void processUnBlockSubRequest(Task task){};
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public void processAzaanSearchRequest(Task task) {
	};
	public void processXbiPack(Task task)
	{};
	public ChargeClass getChargeClass(String className)
	{
		if(className == null)
			return null;
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setName(className);
		return rbtClient.getChargeClass(applicationDetailsRequest);
	}

	public ChargeClass getSelectionChargeClass(Task task)
	{
		logger.info("RBT:: getSelectionChargeClass 1: " + task);
		Category category = (Category)task.getObject(CAT_OBJ);
		Subscriber sub=(Subscriber)task.getObject(param_subscriber);
		if(category == null)
			category = getCategory(3,sub.getLanguage());

		Clip clip = (Clip)task.getObject(CLIP_OBJ);
		if(clip == null)
			return null;
		String classType = task.getString(param_chargeclass);
		logger.info("RBT:: getSelectionChargeClass classType 2 : " + classType);
		ChargeClass chargeClassFeature = getChargeClass(classType);
		logger.info("RBT:: getSelectionChargeClass chargeClassFeature 2 : " + chargeClassFeature);
		ChargeClass chargeClass = getChargeClass(category.getClassType());
		if(category != null && category.getClassType() != null && clip != null && clip.getClassType() != null && !clip.getClassType().equalsIgnoreCase("DEFAULT"))
		{
			logger.info("RBT:: getSelectionChargeClass in catNclip block : ");
			ChargeClass catChargeClass =  getChargeClass(category.getClassType());
			ChargeClass clipChargeClass =  getChargeClass(clip.getClassType());
			if(catChargeClass != null && clipChargeClass != null)
			{
				if(Integer.parseInt(catChargeClass.getAmount()) < Integer.parseInt(clipChargeClass.getAmount()))
					chargeClass = clipChargeClass;
				else
					chargeClass = catChargeClass;
			}	
		}
		if (chargeClass != null && chargeClassFeature != null)
		{
			logger.info("RBT:: getSelectionChargeClass in final block : ");
			if(Integer.parseInt(chargeClass.getAmount()) < Integer.parseInt(chargeClassFeature.getAmount()) ||  chargeClassFeature.getAmount().trim().equalsIgnoreCase("0") || classType.equalsIgnoreCase("DEFAULT"))
				chargeClass = chargeClassFeature;
		}
		logger.info("RBT:: getSelectionChargeClass chargeClass 4: "+chargeClass);
		return chargeClass;
	}
	public void processMGM(Task task)
	{}
	public void processRetailer(Task task)
	{}
	public void processScratchCard(Task task)
	{}
	public void processGiftCopy(Task task)
	{}

	/**
	 * 
	 * @param task
	 */

	public void processMeriDhun(Task task){} 

	public void processMeriDhunRequest(Task task){
		String subscriberID = task.getString(param_subscriberID);
		String clipID = task.getString(param_clipid);//clipID
		String categoryID = param("COMMON", "MERI_DHUN_CATEGORY", null); 
		task.setObject(param_catid, categoryID);
		Clip clip = rbtCacheManager.getClip(clipID);
		if (clip == null){
			task.setObject(param_response, CLIP_NOT_AVAILABLE);
			return;
		}
		String smsText = task.getString(param_smsText);
		String fileName = subscriberID+"-"+clipID+"-";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		Date time = new Date();
		String timestr =  sdf.format(time);
		task.setObject(param_clipid, fileName+timestr);
		String response = processSetSelection(task);
		if (response.equals(WebServiceConstants.SUCCESS)){
			fileName = fileName + timestr + ".txt";
			String localDir = getParameter(iRBTConstant.DAEMON, "LOCAL_DIR_FOR_UGC");
			if (localDir == null) localDir = ".";
			File file = new File(localDir);
			String fullFilePath = file.getAbsolutePath()+"\\"+fileName;
			FileWriter writer;
			try {
				writer = new FileWriter(fullFilePath);
				writer.write(smsText);
				writer.flush();
				writer.close();
				task.setObject(param_response, SUCCESS);
			} catch (IOException e) {
				logger.error(e.getMessage());
				task.setObject(param_response, FAILURE);
			}
		}else{
			task.setObject(param_response, FAILURE);
		}

	}

	public static String subID(String subscriberID) {
		if (subscriberID != null) {
			try {
				String m_countryCodePrefix = param(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
				StringTokenizer stk = new StringTokenizer(m_countryCodePrefix,",");
				while (stk.hasMoreTokens()) {
					String token = stk.nextToken();
					if (subscriberID.startsWith("00"))
						subscriberID = subscriberID.substring(2);
					if (subscriberID.startsWith("+")
							|| subscriberID.startsWith("0")|| subscriberID.startsWith("-"))
						subscriberID = subscriberID.substring(1);
					int m_minPhoneNumberLen = param(iRBTConstant.COMMON, "MIN_PHONE_NUMBER_LEN", 10);
					if (subscriberID.startsWith(token) && (subscriberID.length() >= (m_minPhoneNumberLen + token.length()))){
						subscriberID = subscriberID.substring(token.length());
						break;
					}
				}
			}
			finally {
				if (subscriberID.startsWith("00"))
					subscriberID = subscriberID.substring(2);
				if (subscriberID.startsWith("+")
						|| subscriberID.startsWith("0")|| subscriberID.startsWith("-"))
					subscriberID = subscriberID.substring(1);
			}
		}
		return subscriberID;
	}

	/**
	 * Process' the Ad-RBT Request
	 * 
	 * @author Sreekar
	 * @param task
	 */
	public void processAdRBTRequest(Task task) {}

	public String deleteSubscriberDownload(Task task)
	{

		SelectionRequest selectionRequest = new SelectionRequest(task.getString(param_subscriberID), task.getString(param_catid), task.getString(param_clipid));
		selectionRequest.setMode(task.getString(param_actby));
		selectionRequest.setModeInfo(task.getString(param_actInfo));
		rbtClient.deleteSubscriberDownload(selectionRequest);
		return selectionRequest.getResponse();
	}

	public String processOBDReq(Task task){return null;};
	public boolean isVodafoneOCGInvalidSMS(String sms){return false; };
	public void processStartCopyRequest(Task task){};
	public void processRdcToCgiSongSelectionRequest(Task task){};
	public void processDirectActivationRequest(Task task){};
	public void processUSSDSubscriptionRequest(Task task){};
	public void processChangeMsisdnRequest(Task task){};
	public String processVodaCTservice(Task task){return null;};
	public String processConsentCallback(Task task){return null;};
	public SubscriberPromo getSubscriberPromo(Task task, String type)
	{
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(task.getString(param_subscriberID));
		rbtDetailsRequest.setType(type);
		rbtDetailsRequest.setMode("SMS");
		return rbtClient.getSubscriberPromo(rbtDetailsRequest);
	}

	public String addSubscriberPromo(Task task, String type)
	{
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(task.getString(param_subscriberID));
		subscriptionRequest.setType(type);
		subscriptionRequest.setMode("SMS");
		rbtClient.addSubscriberPromo(subscriptionRequest);
		String response = subscriptionRequest.getResponse();
		return response;
	}


	public String deleteSubscriberPromo(Task task, String type)
	{
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(task.getString(param_subscriberID));
		subscriptionRequest.setType(type);
		subscriptionRequest.setMode("SMS");
		rbtClient.removeSubscriberPromo(subscriptionRequest);
		String response = subscriptionRequest.getResponse();
		return response;
	}
	public void getNextProfile(Task task){}

	//Added by Sreekar for Airtel comes with music opt-in feature
	public void processConfirmCharge(Task task) {}


	//Added for lock feature
	public void processLockRequest(Task task){}
	public void processUnlockRequest(Task task){}

	public void processActivationRequest(Task task){}

	// Added for SuperHit Album List
	public void processListCategories(Task task) {}

	public void processEmotionSongRequest(Task task) {}
	public void processDeactEmotionRbtService(Task task) {}
	public void processExtendEmotionRequest(Task task) {}


	public boolean isOverlap( Setting[] setting , String callerId, String clipId,String language)
	{
		logger.info("inside isOverlap with callerId "+callerId + " and clipId"+clipId);
		logger.info("inside isOverlap with setting length "+(setting != null ?setting.length : 0));
		if(setting == null || setting.length == 0)
			return false;

		if(clipId == null || clipId.trim().length() == 0)
			return false;
		Clip clip = rbtCacheManager.getClip(clipId,language);
		logger.info("inside selectionLimitExceeded.  clip is "+clip);
		if(clip == null || clip.getClipEndTime() == null || clip.getClipEndTime().getTime() < System.currentTimeMillis())
			return false;
		for (int i = 0; i < setting.length; i++)
		{
			logger.info("setting["+i+"] is "+setting[i]);
			String selectionWavFile = setting[i].getRbtFile();
			if(selectionWavFile == null)
				continue;
			if(selectionWavFile.endsWith(".wav"))
				selectionWavFile = selectionWavFile.substring(0,selectionWavFile.length()-4);
			Clip selectionClip = rbtCacheManager.getClipByRbtWavFileName(selectionWavFile,language);
			if(selectionClip == null)
				continue;
			if(selectionClip.getClipId() == clip.getClipId())
			{
				String selectionCaller = setting[i].getCallerID();
				if(selectionCaller.equalsIgnoreCase(WebServiceConstants.ALL))
					return true;
				else if(callerId != null && callerId.equalsIgnoreCase(selectionCaller))
					return true;
			}
		}
		logger.info("exiting isOverlap");
		return false;
	}
	public boolean isSelectionSuspended(Setting[] setting)
	{
		logger.info("inside isSelectionSuspended");
		if(setting == null || setting.length == 0)
			return false;
		for (int i = 0; i < setting.length; i++)
		{
			String selectionStatus = setting[i].getSelectionStatus();
			if(selectionStatus.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
				return true;
		}
		logger.info("exiting isSelectionSuspended");
		return false;

	}

	public boolean selectionLimitExceeded(Setting[] setting)
	{
		logger.info("inside selectionLimitExceeded");
		if(setting == null || setting.length == 0)
			return false;
		int maxAllowedSelection = param(COMMON,MAX_ALLOWED_SELECTION,0);
		if(maxAllowedSelection > 0 && setting.length >= maxAllowedSelection)
			return true;
		logger.info("exiting selectionLimitExceeded");
		return false;
	}
	public Offer getBaseOffer(Task task)
	{
		logger.info("inside getBaseOffer");
		String offerId = null;
		Offer offer = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		if(subscriber == null)
		{
			logger.error("inside getBaseOffer. Error case as subscriber is null");
			return null;
		}
		String subscriberID = subscriber.getSubscriberID();
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		String mode = task.getString(param_actby);
		boolean isPrepaid = subscriber.isPrepaid();
		int offerType = Offer.OFFER_TYPE_SUBSCRIPTION;
		rbtDetailsRequest.setMode(mode);
		rbtDetailsRequest.setIsPrepaid(isPrepaid);
		rbtDetailsRequest.setType(offerType+"");
		// Parameters to be set in hashmap are CONTENT_TYPE, RBT_TYPE, SUBSCRIPTION_CLASS
		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		String clipId = task.getString(param_clipid);
		String contentType = null;
		if(clipId != null)
		{
			Clip clip = getClipById(clipId);
			if(clip != null)
				contentType = clip.getContentType();
		}
		String rbtType = null;
		if(subscriber.getUserType().equalsIgnoreCase(WebServiceConstants.AD_RBT))
			rbtType = WebServiceConstants.AD_RBT;
		Integer rbtTypeInt = (Integer)task.getObject(param_rbttype);
		if(rbtTypeInt != null && rbtTypeInt.intValue() == 1)
			rbtType = WebServiceConstants.AD_RBT;

		String subscriptionClass = task.getString(param_subclass);
		extraInfoMap.put(Offer.SUBSCRIPTION_CLASS, subscriptionClass);
		extraInfoMap.put(Offer.RBT_TYPE, rbtType);
		extraInfoMap.put(Offer.CONTENT_TYPE, contentType);
		logger.info("ExtraInfo : " + extraInfoMap);
		rbtDetailsRequest.setExtraInfoMap(extraInfoMap);
		Offer[] offers = rbtClient.getOffers(rbtDetailsRequest);
		if(offers != null && offers.length > 0 && offers[0].getOfferID() != null) {
			offer = offers[0];
			offerId = offer.getOfferID();
		}
		logger.info("exiting getBaseOffer with offerId = "+offerId);
		return offer;
	}

	public Offer getSelOffer(Task task)
	{
		logger.info("inside getSelOffer");
		String offerId = null;
		Offer offer = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		if(subscriber == null)
		{
			logger.error("inside getSelOffer. Error case as subscriber is null");
			return null;
		}
		String subscriberID = subscriber.getSubscriberID();
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		String mode = task.getString(param_actby);
		boolean isPrepaid = subscriber.isPrepaid();
		int offerType = Offer.OFFER_TYPE_SELECTION;
		rbtDetailsRequest.setMode(mode);
		rbtDetailsRequest.setIsPrepaid(isPrepaid);
		rbtDetailsRequest.setType(offerType+"");
		// Parameters to be set in hashmap are CONTENT_TYPE, RBT_TYPE, SUBSCRIPTION_CLASS
		//CONTENT_TYPE, CLIP_CHARGE_CLASS, CATEGORY_CHARGE_CLASS, UI_CHARGE_CLASS, RBT_TYPE, LITE_USER

		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		String clipId = task.getString(param_clipid);
		String contentType = null;
		String clipChargeClass = null;
		if(clipId != null)
		{
			Clip clip = getClipById(clipId);
			if(clip != null)
			{
				contentType = clip.getContentType();
				clipChargeClass = clip.getClassType();
				extraInfoMap.put(Offer.CLIP_ID, clipId);
			}
		}
		String rbtType = null;
		if(subscriber.getUserType().equalsIgnoreCase(WebServiceConstants.AD_RBT))
			rbtType = WebServiceConstants.AD_RBT;
		String subscriptionClass = task.getString(param_subclass);
		extraInfoMap.put(Offer.SUBSCRIPTION_CLASS, subscriptionClass);
		extraInfoMap.put(Offer.RBT_TYPE, rbtType);
		extraInfoMap.put(Offer.CONTENT_TYPE, contentType);
		extraInfoMap.put(Offer.CLIP_CHARGE_CLASS, clipChargeClass);
		//ExtraInfo for Sel
		String lite_user = "FALSE";
		String cosId = subscriber.getCosID();
		if(cosId != null)
		{
			CosDetails cosDetail = cosDetailsCacheManager.getCosDetail(cosId);
			if(cosDetail != null)
			{
				if(cosDetail.getCosType() != null && cosDetail.getCosType().equalsIgnoreCase(WebServiceConstants.COS_TYPE_LITE))
					lite_user  = "TRUE";
			}
		}
		extraInfoMap.put(Offer.LITE_USER, lite_user);

		String catId = task.getString(param_catid);
		String categoryChargeClass = null;
		if(catId != null)
		{
			Category category = getCategory(catId);
			if(category != null && category.getClassType() != null)
				categoryChargeClass = category.getClassType();
		}
		extraInfoMap.put(Offer.CATEGORY_CHARGE_CLASS, categoryChargeClass);
		extraInfoMap.put(Offer.UI_CHARGE_CLASS, task.getString(param_chargeclass));		
		logger.info("ExtraInfo : " + extraInfoMap);
		rbtDetailsRequest.setExtraInfoMap(extraInfoMap);
		Offer[] offers = rbtClient.getOffers(rbtDetailsRequest);
		if(offers != null && offers.length > 0 && offers[0].getOfferID() != null) {
			offer = offers[0];
			offerId = offer.getOfferID();
		}
		logger.info("exiting getSelOffer with offerId = "+offerId);
		return offer;
	}

	protected Offer getPackOffer(Task task) {

		logger.info("To get pack offer, checking subscriber object");

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (subscriber == null) {
			logger.error("Could not get Pack offer. Subscriber is null");
			return null;
		}

		String subscriberID = subscriber.getSubscriberID();
		boolean isPrepaid = subscriber.isPrepaid();
		String mode = task.getString(param_actby);
		String musicPackOfferType = String.valueOf(com.onmobile.apps.ringbacktones.smClient.beans.Offer.OFFER_TYPE_PACK); 

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberID);
		rbtDetailsRequest.setMode(mode);
		rbtDetailsRequest.setIsPrepaid(isPrepaid);
		rbtDetailsRequest.setType(musicPackOfferType);

		logger.info("Getting offer for rbtDetailsRequest: "+rbtDetailsRequest);
		Offer[] offers = rbtClient.getOffers(rbtDetailsRequest);
		logger.info("Got response of offers: "+offers);

		Offer offer = null;
		if (offers != null && offers.length > 0
				&& offers[0].getOfferID() != null) {
			offer = offers[0];
		}

		logger.info("getPackOffer returns: " + offer);
		return offer;
	}

	public void getFeature(Task task)
	{
	}	

	public void processChurnOffer(Task task){};

	public void processRDCViralSelection(Task task){};

	public void processDiscountedSelection(Task task){};

	public void processChargingConsentRequest(Task task) {};

	public static Subscriber getSubObject(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if(subscriber == null)
			subscriber = getSubscriber(task);
		return subscriber;
	}

	public void processCPSelectionConfirm(Task task) {};

	public void processVoucherRequest(Task task) {};

	public String processUpgradeValidity(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());
		subscriptionRequest.setInfo(WebServiceConstants.UPGRADE_VALIDITY);
		subscriptionRequest.setMode(task.getString(param_MODE));
		subscriptionRequest.setSubscriptionClass(task.getString(param_subclass));

		subscriber = rbtClient.updateSubscription(subscriptionRequest);

		return subscriptionRequest.getResponse();
	}

	public void processUpgradeSelRequest(Task task) {};

	public void processGiftAccept(Task task) {};

	public void processGiftDownload(Task task) {};

	public void processGiftReject(Task task) {};

	public void processRegistraionSMS(Task task) {};

	protected Gift getLatestGiftFromGiftInbox(Task task, String promoId) {

		String subscriberId = task.getString(param_subscriberID);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		GiftInbox giftInbox = rbtClient.getGiftInbox(rbtDetailsRequest);

		Gift[] gifts = giftInbox.getGifts();

		if(gifts == null || gifts.length == 0) {
			return null;
		}

		int requestedClipId = -1;
		int requestedCatId = -1;

		if(promoId !=  null && task.containsKey(param_catid)) {
			requestedCatId = Integer.parseInt(task.getString(param_catid));
		}

		if(promoId != null && task.containsKey(param_clipid)) {
			requestedClipId = Integer.parseInt(task.getString(param_clipid));
		}

		Gift gift = null;

		if(promoId != null) {

			for(Gift giftLoop : gifts) {
				int categoryId = giftLoop.getCategoryID();
				int toneId = giftLoop.getToneID();

				/*
				 * if gifter has gifted shuffle category, the If block will get satisfied.
				 */
				if( requestedCatId == categoryId) {
					gift = giftLoop;
				}
				else if(requestedClipId == toneId) {
					gift = giftLoop;
				}
			}
		}
		else {
			gift = gifts[gifts.length - 1];
		}

		return gift;

	}

	protected com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass getChargeClassFromSettings(
			Task task, String viralDataCallerID, Clip clip) {
		boolean isDownloadsModel = RBTParametersUtils.getParamAsBoolean(
				"COMMON", "ADD_TO_DOWNLOADS", "FALSE");
		Rbt rbt = (Rbt) task.getObject(param_rbt_object);
		logger.info(" isDownloadsModel: " + isDownloadsModel + ", rbt object: "
				+ rbt);
		if (null == rbt) {
			logger.warn(" Since rbt object is null, returning charge class amount as null");
			return null;
		}
		// ClipRbtWav does not contains the extension
		String clipRbtWavFileName = clip.getClipRbtWavFile().concat(".wav");
		if (isDownloadsModel) {
			Download[] downloads = rbt.getLibrary().getDownloads()
					.getDownloads();
			logger.info(" Downloads: " + downloads + ", clipRbtWavFileName: "
					+ clipRbtWavFileName);
			if (null != downloads) {
				for (Download download : downloads) {
					logger.info(" download RbtFile: " + download.getRbtFile()
							+ ", clipRbtWavFileName: " + clipRbtWavFileName);
					if (download.getRbtFile().equals(clipRbtWavFileName)) {
						com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass chargeClass = CacheManagerUtil
								.getChargeClassCacheManager().getChargeClass(
										download.getChargeClass());
						logger.info("The wave file of download and"
								+ " viral data clip are matched. "
								+ " ChargeClass: " + chargeClass + ", amount: "
								+ chargeClass != null ? chargeClass.getAmount() : null);
						return chargeClass;
					}
				}
			}
		} else {
			Setting[] settings = rbt.getLibrary().getSettings().getSettings();
			logger.info(" settings : " + settings);
			if (null != settings) {
				for (Setting setting : settings) {
					String settingCallerID = setting.getCallerID();
					logger.info(" download RbtFile: " + setting.getRbtFile()
							+ ", clipRbtWavFileName: " + clipRbtWavFileName);
					if (setting.getRbtFile().equals(clipRbtWavFileName)
							&& ((viralDataCallerID == null && settingCallerID
									.equalsIgnoreCase("all")) || (viralDataCallerID != null && viralDataCallerID
									.equals(settingCallerID)))) {
						com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass chargeClass = CacheManagerUtil
								.getChargeClassCacheManager().getChargeClass(
										setting.getChargeClass());
						logger.info("The wave file of setting and"
								+ " viral data clip are matched. "
								+ " ChargeClass: " + chargeClass + ", amount: "
								+ chargeClass.getAmount());
						return chargeClass;
					}
				}
			}

		}
		logger.info("Could not get charge class from selections."
				+ " Returning null");
		return null;

	}

	protected String getChargeClassFromSelections(Task task,
			String viralDataCallerID, Clip clip) {
		com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass chargeClass = getChargeClassFromSettings(
				task, viralDataCallerID, clip);
		if (null != chargeClass)
			return chargeClass.getAmount();
		else
			return null;
	}

	/**
	 * @param subscriber
	 * @return
	 */
	public static boolean isPackActiveForSubscriber(Subscriber subscriber)
	{
		if (subscriber == null)
			return false;

		boolean isAnyPackActive = false;
		HashMap<String, String> extraInfoMap = subscriber.getUserInfoMap();
		if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_PACK))
		{
			String packStr = extraInfoMap.get(EXTRA_INFO_PACK);
			String[] packs = (packStr != null) ? packStr.trim().split(",")
					: null;
			for (int i = 0; packs != null && i < packs.length; i++)
			{
				String activePackCosId = packs[i];
				CosDetails cosDetails = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(activePackCosId);
				if (cosDetails != null
						&& (iRBTConstant.LIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails.getCosType())
								|| iRBTConstant.UNLIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails.getCosType())
								|| iRBTConstant.SONG_PACK.equalsIgnoreCase(cosDetails.getCosType())
								|| iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosDetails.getCosType())
								|| iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosDetails.getCosType())))
				{
					List<ProvisioningRequests> response = ProvisioningRequestsDao
							.getBySubscriberIDTypeAndNonDeactivatedStatus(
									subscriber.getSubscriberID(),
									Integer.parseInt(activePackCosId));
					isAnyPackActive = (response != null);
					if (isAnyPackActive)
						break;
				}
			}
		}

		return isAnyPackActive;
	}

	protected String getSubstituedSMS(String smsText, String str1, String str2,
			String str3, String actAmt, String selAmt) {
		if (smsText == null)
			return null;
		if (actAmt != null) {
			while (smsText.indexOf("%ACT_AMT") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%ACT_AMT"))
						+ actAmt
						+ smsText.substring(smsText.indexOf("%ACT_AMT") + 8);
			}
		}
		if (selAmt != null) {
			while (smsText.indexOf("%SEL_AMT") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%SEL_AMT"))
						+ selAmt
						+ smsText.substring(smsText.indexOf("%SEL_AMT") + 8);
			}
		}

		if (str2 == null) {
			if (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
		} else if (str3 == null) {
			while (smsText.indexOf("%S") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
						+ smsText.substring(smsText.indexOf("%S") + 2);
			}
			while (smsText.indexOf("%C") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
						+ smsText.substring(smsText.indexOf("%C") + 2);
			}
			while (smsText.indexOf("%L") != -1) {
				smsText = smsText.replace(" %L", "");
			}
		} else {
			while (smsText.indexOf("%S") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
						+ smsText.substring(smsText.indexOf("%S") + 2);
			}
			while (smsText.indexOf("%C") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
						+ smsText.substring(smsText.indexOf("%C") + 2);
			}
			while (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str3
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
		}

		return smsText;
	}


	public void processOUISmsRequest(Task task){};
	public void processCancelDeactvation(Task task){};
	public void processBaseSongUpgradationRequest(Task task){};
	public void processTimeBasedSettingRequest(Task task){};
	public void processSMSChurnOfferOrDeact(Task task){};
	public String processSelectionConsentIntegration(Task task){return null;};

	//Airtel wap gift consent flow
	public String processGiftConsentIntegration(Task task){return null;};
	public String processAcceptGiftConsentIntegration(Task task){return null;};

	//RBT-9213
	public Task getSDPDirectConsentTask(HashMap<String, String> requestParams) {return null;};
	public void processSDPDirectActivation(Task task) {};
	public void processSDPDirectSelection(Task task) {};
	public void processSDPIndirectActivation(Task task) {};
	public void processSDPIndirectSelection(Task task) {};

	public void processSMSRecommendSongs(Task task) {}

	public void processMultipleSelection(Task task) {}
	public void processCancellar(Task task){};
	public void processSongManageDeact(Task task){};
	public void processSongDeactivationConfirm(Task task){};
	public void getOnlyAllCallerSettings(Task task){}
	public void processPremiumSelectionConfirmation(Task task) {}
	public void processDoubleOptInConfirmation(Task task) {}
	//for Upgradation of base and cos of a subscriber
	public void processBaseAndCosUpgradationRequest(Task task){};
	
	public void processDeleteTone(Task task)
	{

		SelectionRequest selectionRequest = new SelectionRequest(task.getString(param_MSISDN), task.getString(param_TONE_ID), task.getString(param_CATEGORY_ID));
		selectionRequest.setMode(task.getString(param_DEACTIVATED_BY));
		String response = null;
		rbtClient.deleteSubscriberSelection(selectionRequest);
		response = selectionRequest.getResponse();
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
			response ="success";
		}else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_NOT_EXISTS)){
			response = clipNotAvailable;
		}else if (response.equalsIgnoreCase(WebServiceConstants.FAILED)){
			response = technicalFailure;
		}else if (response.equalsIgnoreCase(WebServiceConstants.ALREADY_EXISTS)){
			response = selAlreadyExists;
		}
		task.setObject(param_response, response);
	}
	//Added for VB-380
	public void processDeactivateAzaan(Task task){};
}