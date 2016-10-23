package com.onmobile.apps.ringbacktones.subscriptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.RBTLogin;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

/**
 * This class implements all the TATA CDMA Huawei API. These are to be provided
 * for CRM and other interfaces
 * 
 * @author Sreekar (2009-02-07)
 * @version 1.0
 * 
 * @changed Sreekar (2009-03-01), added transID in openaccount
 */
public class RBTTataGSMImpl implements iRBTConstant {
	
	private static Logger logger = Logger.getLogger(RBTTataGSMImpl.class);
	
	private static final Object _syncObj = new Object();

	private static RBTTataGSMImpl _instance = null;
	private static RBTDBManager _dbManager = null;
	private final SimpleDateFormat _formatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// Request Types
	private final int REQUEST_SUBSCRIBER_STATUS = 1;
	private final int REQUEST_OPEN_ACCOUNT = 2;
	private final int REQUEST_DELETE_ACCOUNT = 3;
	// private final int REQUEST_DOWNLOAD_TONE = 4;//not needed
	// private final int REQUEST_DOWNLOAD_TONEBOX = 5;//not needed
	// private final int REQUEST_SUSPEND_RESUME = 6;//not needed
	// private final int REQUEST_DELETE_SONG_MB = 7;//not needed
	// private final int REQUEST_QUERY_SONGS_FROM_LIB = 8;//not needed
	// private final int REQUEST_QUERY_MB_FROM_LIB = 9;//not needed
	private final int REQUEST_QUERY_SETTING = 10;
	private final int REQUEST_ADD_SETTING = 11;
	private final int REQUEST_DELETE_SONG_SETTING = 12;
	private final int REQUEST_DELETE_MB_SETTING = 13;
	// private final int REQUEST_MODIFY_SONG_SETTING = 14;//not needed (not
	// supported)
	// private final int REQUEST_MODIFY_MB_SETTING = 15;//not needed (not
	// supported)
	// private final int REQUEST_GROUP_ADD = 16;//not needed
	// private final int REQUEST_GROUP_DELETE = 17;//not needed
	// private final int REQUEST_GROUP_QUERY_GROUPS = 18;//not needed
	// private final int REQUEST_GROUP_ADD_NUMBER = 19;//not needed
	// private final int REQUEST_GROUP_QUERY = 20;//not needed
	// private final int REQUEST_GROUP_DELETE_NUMBER = 21;//not needed
	private final int REQUEST_SONG_QUERY = 22;
	private final int REQUEST_MB_QUERY = 23;
	// private final int REQUEST_QUERY_DOWNLOAD_STATUS = 24;//not needed
	// private final int REQUEST_GIFT_SERVICE = 25;//not needed (not supported)
	private final int REQUEST_GIFT_SONG = 26;
	private final int REQUEST_RETAILER_SONG_SETTING = 27;

	// Request Parameters
	/*
	 * if(!map.containsKey(PARAM_PHONE_NUMBER) ||
	 * !map.containsKey(PARAM_SPECIAL_PHONE) ||
	 * !map.containsKey(PARAM_START_TIME) || !map.containsKey(PARAM_SET_TYPE) ||
	 * !map.containsKey(PARAM_END_TIME) || !map.containsKey(PARAM_TIME_TYPE) ||
	 * !map.containsKey(PARAM_TONE_CODE) || !map.containsKey(PARAM_FLAG) ||
	 * !map.containsKey(PARAM_TONE_FLAG))
	 */
	private final String PARAM_REQUEST = "request";
	private final String PARAM_OPERATOR = "operator";
	private final String PARAM_OPERATOR_ACCOUNT = "operatoraccount";
	private final String PARAM_OPERATOR_PWD = "operatorpwd";
	private final String PARAM_PHONE_NUMBER = "phonenumber";
	private final String PARAM_SET_TYPE = "settype";
	private final String PARAM_COS_ID = "cosid";
	private final String PARAM_SPECIAL_PHONE = "specialphone";
	private final String PARAM_START_TIME = "starttime";
	private final String PARAM_END_TIME = "endtime";
	private final String PARAM_TIME_TYPE = "timetype";
	private final String PARAM_TONE_CODE = "tonecode";
	private final String PARAM_FLAG = "flag";
	private final String PARAM_TONE_FLAG = "toneflag";
	private final String PARAM_TONE_GROUP_ID = "tonegroupid";
	private final String PARAM_LOOP_NO = "loopno";
	private final String PARAM_SETTING_ID = "settingid";
	private final String PARAM_SONG_NAME = "songname";
	private final String PARAM_MB_NAME = "musicboxname";
	private final String PARAM_ACCEPT_PHONE_NUMBER = "acceptphonenumber";
	private final String PARAM_TRANS_ID = "transid";
	private final String PARAM_RETAILER_ID = "retailerid";
	private final String PARAM_ISUPGRADE = "false";

	// Response values
	// Generic
	private final String RESPONSE_OPERATOR_INVALID = "1";
	private final String RESPONSE_PARAMS_INVALID = "2";
	private final String RESPONSE_SUB_INVALID = "3";
	private final String RESPONSE_SUBSCRIBER_INVALID = "17";
	private final String RESPONSE_PORTAL_ERROR = "8";

	public HashMap<String, String> m_userPwdMap = new HashMap<String, String>();

	/**
	 * This class is supposed to be singleton, that is y the constructor is
	 * private
	 * 
	 * @throws RBTException
	 */
	private RBTTataGSMImpl() throws RBTException {
		init();
	}


	
	/**
	 * This is the method to be called to create the instance of this class.
	 * This method creates a new instance if this method is called for the first
	 * time otherwise will return the already created instance
	 * 
	 * @return the singleton instance of the class
	 * @throws RBTException
	 *             if not able to create the instance
	 */
	public static RBTTataGSMImpl getInstance() throws RBTException {
		if (_instance == null) {
			synchronized (_syncObj) {
				if (_instance == null) {
					_instance = new RBTTataGSMImpl();
				}
			}
		}
		if (_instance == null)
			throw new RBTException("Cannot init RBTTataGSMImpl");
		return _instance;
	}

	/**
	 * This method instantiates all the basic parameters needed for processing
	 * 
	 * @throws RBTException
	 *             if not able to init DBMgr
	 */
	private void init() throws RBTException {
		_dbManager = RBTDBManager.getInstance();
		if (_dbManager == null)
			throw new RBTException("failed initing DBMgr");

		RBTLogin[] rbtLogin = _dbManager.getLogins();
		logger.info("No of logins found "
				+ rbtLogin.length);
		if (rbtLogin != null && rbtLogin.length > 0) {
			logger.info("initing user and password hashmap");
			for (int i = 0; i < rbtLogin.length; i++) {
				if (rbtLogin[i].userType().trim().equals("10")) {
					logger.info("record found with usertype 10");
					if (rbtLogin[i].user() != null && rbtLogin[i].pwd() != null) {
						logger.info("putting record in hashmap username "
										+ rbtLogin[i].user() + " and passwd "
										+ rbtLogin[i].pwd());
						m_userPwdMap.put(rbtLogin[i].user().trim(), rbtLogin[i]
								.pwd().trim());
					}
				}
			}
		}
	}

	/**
	 * This method will process the request
	 * 
	 * @param map
	 *            having the parameter name value pairs
	 * @return Returns the response to be sent to the requester
	 */
	public String processRequest(Map<String, String[]> paramMap) {
		HashMap<String, String> map = new HashMap<String, String>();
		Iterator<String> itr = paramMap.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value = null;
			String[] values = paramMap.get(key);
			if (values != null && values.length > 0 && values[0] != null
					&& !values[0].equals("")) {
				value = values[0];
				map.put(key, value);
			}
		}
		logger.info("RBT:: Params are - " + map);

		if (!map.containsKey(PARAM_REQUEST)) {
			logger.info("RBT::no " + PARAM_REQUEST
					+ " parameter");
			return RESPONSE_PORTAL_ERROR;
		}

		String requestStr = map.get(PARAM_REQUEST);
		int request = -1;
		try {
			request = Integer.parseInt(requestStr);
		} catch (Exception e) {
			logger.error("", e);
		}

		if (request == -1)
			return RESPONSE_PORTAL_ERROR;

		if (!validOperatorParams(map))
			return RESPONSE_OPERATOR_INVALID;
		if (map.containsKey(PARAM_PHONE_NUMBER)
				&& !_dbManager.isValidPrefix(map.get(PARAM_PHONE_NUMBER)))
			return RESPONSE_SUB_INVALID;

		try {
			
			String phoneNumber = map.get(PARAM_PHONE_NUMBER);
			if(!(request == REQUEST_DELETE_ACCOUNT||request == REQUEST_QUERY_SETTING)){
			   if(phoneNumber!=null && _dbManager.isTotalBlackListSub(phoneNumber)){
				   return RESPONSE_SUB_INVALID;
			   }
		     }
			
			switch (request) {
			case REQUEST_SUBSCRIBER_STATUS:
				return processSubscriberStatus(map);
			case REQUEST_OPEN_ACCOUNT:
				return processRegestration(map);
			case REQUEST_DELETE_ACCOUNT:
				return processDeregestration(map);
			case REQUEST_QUERY_SETTING:
				return processQuerySetting(map);
			case REQUEST_ADD_SETTING:
				return processAddSetting(map);
			case REQUEST_DELETE_SONG_SETTING:
				return processDeleteSongSetting(map);
			case REQUEST_DELETE_MB_SETTING:
				return processDeleteMBSetting(map);
			case REQUEST_SONG_QUERY:
				return processSongRequest(map);
			case REQUEST_MB_QUERY:
				return processMBRequest(map);
			case REQUEST_GIFT_SONG:
				return processGiftSong(map);
			case REQUEST_RETAILER_SONG_SETTING:
				return processRetailerSongRequest(map);
			default:
				logger.info("RBT::Invalid status - "
						+ request);
				return RESPONSE_PORTAL_ERROR;
			}
		} catch (Exception e) {
			logger.error("", e);
			return RESPONSE_PORTAL_ERROR;
		}
	}

	// Subscriber-Status
	private final String RESPONSE_SUB_GIFT_PENDING = "6";
	private final String RESPONSE_SUB_NEW = "7";
	private final String RESPONSE_SUB_SUSPENDED = "9";
	private final String RESPONSE_SUB_RENEWAL_PENDING = "19";
	private final String RESPONSE_SUB_PRE = "1";
	private final String RESPONSE_SUB_POST = "0";
	private final String RESPONSE_SUB_ACT_INIT = "1";
	private final String RESPONSE_SUB_ACTIVE = "2";
	private final String RESPONSE_SUB_DEACTIVE = "4";
	private final String RESPONSE_SUB_DEACT_INIT = "5";
	private final String RESPONSE_SUB_ACT_PENDING = "6";
	private final String RESPONSE_SUB_DEACT_PENDING = "7";

	/**
	 * 
	 * @param map
	 *            The parameter name value pairs
	 * @return Returns the subscriber status in the format
	 *         status|userType|corpID|cosID for valid subscriber.
	 * 
	 *         Status: 1 - Before Open state 2 - normal 4 - unregistered 5 -
	 *         deactivate state(user initiated temporary deactivate state) 6 -
	 *         Opening (In case of Prepaid Before CRM sends the confirmation for
	 *         the registration) 7 - Before closing state
	 * 
	 *         UserType: 0 - Postpaid 1 - Prepaid
	 * 
	 *         Length of result code being 1 indicates error respose and the
	 *         differer error codes are 1 - Invalid operator params 2 - Input
	 *         parameter format error 3 - Invalid user phone number 5 -
	 *         Subscriber is in black list 6 - User's gift is under processing 7
	 *         - New user to the system 8 - Portal Error 9 - Suspended
	 *         user(owing to conditions like low balance etc.)
	 */
	private String processSubscriberStatus(HashMap<String, String> map) {
		if (!map.containsKey(PARAM_PHONE_NUMBER))
			return RESPONSE_PARAMS_INVALID;
		String phoneNumber = map.get(PARAM_PHONE_NUMBER);
		logger.info("RBT:: in for subscriber - "
				+ phoneNumber);

		Subscriber subscriber = _dbManager.getSubscriber(phoneNumber);
		if (subscriber == null)
			return RESPONSE_SUB_NEW;
		if (_dbManager.isSubscriberSuspended(subscriber))
			return RESPONSE_SUB_SUSPENDED;

		ViralSMSTable[] viralEntries = _dbManager
				.getViralSMSByCaller(phoneNumber);
		for (int i = 0; viralEntries != null && i < viralEntries.length; i++) {
			String type = viralEntries[i].type();
			if (!_dbManager.isSubActive(subscriber)
					&& (type.equals(GIFT) || type.equals(GIFTCHRGPENDING)
							|| type.equals(GIFT_CHARGED)
							|| type.equals(ACCEPT_PRE) || type
							.equals(ACCEPT_ACK)))
				return RESPONSE_SUB_GIFT_PENDING;
		}

		StringBuffer sb = new StringBuffer();
		String subStatus = null;
		if (subscriber.subYes().equals(STATE_TO_BE_ACTIVATED)
				|| subscriber.subYes().equals(STATE_CHANGE))
			subStatus = RESPONSE_SUB_ACT_INIT;
		else if (subscriber.subYes().equals(STATE_ACTIVATION_PENDING))
			subStatus = RESPONSE_SUB_ACT_PENDING;
		else if (subscriber.subYes().equals(STATE_ACTIVATED))
			subStatus = RESPONSE_SUB_ACTIVE;
		else if (subscriber.subYes().equals(STATE_TO_BE_DEACTIVATED))
			subStatus = RESPONSE_SUB_DEACT_INIT;
		else if (subscriber.subYes().equals(STATE_DEACTIVATION_PENDING)
				|| subscriber.subYes().equals(STATE_DEACTIVATED_INIT))
			subStatus = RESPONSE_SUB_DEACT_PENDING;
		else if (subscriber.subYes().equals(STATE_DEACTIVATED))
			subStatus = RESPONSE_SUB_DEACTIVE;
		else if (subscriber.subYes().equals(STATE_ACTIVATION_ERROR)
				|| subscriber.subYes().equals(STATE_DEACTIVATION_ERROR))
			return RESPONSE_PORTAL_ERROR;

		sb.append(subStatus + "|");

			if (subscriber.prepaidYes())
				sb.append(RESPONSE_SUB_PRE + "|notincorp|");
			else
				sb.append(RESPONSE_SUB_POST + "|notincorp|");
		if (subscriber.cosID() != null)
			sb.append(subscriber.cosID());
		else
			sb.append("-1");

		return sb.toString();
	}

	// Open-Account
	private final String RESPONSE_UPGRADE_SUCCESS = "25";
	private final String RESPONSE_ACTIVATION_ACCEPTED = "99";
	private final String RESPONSE_SUB_ALREADY_ACTIVE = "4";
	private final String RESPONSE_SUB_ALREADY_ACT_PENDING = "5";
	private final String RESPONSE_SUB_ALREADY_GIFT_PENDING = "13";
	private final String RESPONSE_COS_INVALID = "14";
	private final String RESPONSE_RETAILER_INVALID = "20";
	private final String RESPONSE_BALANCE_INSUFFICIENT = "15";
	//Added by Sreekar for openaccount
	private final String RESPONSE_WDS_NOT_ACCESSIBLE = "10";
	private final String RESPONSE_NOT_PRESENT_IN_WDS = "11";

	/**
	 * This method processes the activation (openAccount/registration) request
	 * 
	 * @param map
	 *            The parameter name value pairs
	 * 
	 * @return 99 - Registration is to be processed (Indicates provisional
	 *         success, user should wait for SMS confirmation) 1 - Invalid
	 *         Operator Parameter(s). 2 - Input Parameter(s) format error 3 -
	 *         Invalid user phonenumber (indicates user is in Blacklist) 4 - The
	 *         same user phonenumber already exists and cannot re-open the
	 *         account 5 - Registration is already under processing 8 - Portal
	 *         error 12 - Maximum number of registration for the day has reached
	 *         13 - The user is been Presented(gifted) with the Coloring service
	 *         and is under process 14 - Invalid COS(COS not allowed) 15 -
	 *         Insufficient balance(used when real time charging is enabled for
	 *         prepaid)
	 * 
	 */
	private String processRegestration(HashMap<String, String> map) {
        String isUpgrade=CacheManagerUtil.getParametersCacheManager().getParameterValue("INTERFACE","ISUPGRADE","TRUE");
		if (!map.containsKey(PARAM_PHONE_NUMBER))
			return RESPONSE_PARAMS_INVALID;
		String phoneNumber = map.get(PARAM_PHONE_NUMBER);
		logger.info("RBT:: in for subscriber - "
				+ phoneNumber);
		boolean isPrepaid = true;
//		String mappedCircleID = null;
//		String m_class = "RBTTataGSMImpl";


		//boolean isUpgrade=false; 

		/*if (map.containsKey(PARAM_ISUPGRADE)&&map.get(PARAM_ISUPGRADE).equalsIgnoreCase("true")){ 
			isUpgrade=true; 
		} */


		Subscriber subscriber = _dbManager.getSubscriber(phoneNumber);
		if (subscriber != null) {
			/*if ((subscriber.subYes().equals(STATE_ACTIVATED)&&!isUpgrade)
					|| subscriber.subYes().equals(STATE_CHANGE))
				return RESPONSE_SUB_ALREADY_ACTIVE;*/
			 if(isUpgrade.equalsIgnoreCase("true")){
				 if (subscriber.subYes().equals(STATE_CHANGE))
						return RESPONSE_SUB_ALREADY_ACTIVE;
			 }else{
				 if ((subscriber.subYes().equals(STATE_ACTIVATED))
					|| subscriber.subYes().equals(STATE_CHANGE))
				return RESPONSE_SUB_ALREADY_ACTIVE;
			 }
		}
		if (_dbManager.isSubscriberActivationPending(subscriber))
			return RESPONSE_SUB_ALREADY_ACT_PENDING;

		if (_dbManager.isSubscriberDeactivationPending(subscriber))
			return RESPONSE_SUB_DEACT_PENDING;

		if (_dbManager.isSubscriberSuspended(subscriber))
				//|| _dbManager.isSubscriberInGrace(subscriber))
			return RESPONSE_BALANCE_INSUFFICIENT;

//		if (_dbManager.isViralBlackListSub(phoneNumber))
//			return RESPONSE_SUB_INVALID;

		/*ViralSMSTable[] viralEntries = _dbManager
				.getViralSMSByCaller(phoneNumber);
		for (int i = 0; viralEntries != null && i < viralEntries.length; i++) {
			String type = viralEntries[i].type();
			if (!_dbManager.isSubActive(subscriber)
					&& (type.equals(GIFT) || type.equals(GIFTCHRGPENDING)
							|| type.equals(GIFT_CHARGED)
							|| type.equals(ACCEPT_PRE) || type
							.equals(ACCEPT_ACK)))
				return RESPONSE_SUB_ALREADY_GIFT_PENDING;
		}*/

		String circleID = null;
		String wdsResult=null;
		String operator = map.get(PARAM_OPERATOR);
		String cosID = null;
		CosDetails cos = null;

		SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(phoneNumber));
		HashMap<String,String> subscriberInfo= subscriberDetail.getSubscriberDetailsMap();
		String wdsStatus = null;
		if(subscriberInfo != null)
			wdsStatus = subscriberInfo.get("WDS_STATUS");
		if(wdsStatus != null) {
			if(wdsStatus.equalsIgnoreCase("WDS_ERROR"))
				return RESPONSE_WDS_NOT_ACCESSIBLE;
			if(wdsStatus.equalsIgnoreCase("WDS_ERROR_RESPONSE"))
				return RESPONSE_NOT_PRESENT_IN_WDS;
		}
		if (subscriberDetail != null && !subscriberDetail.isValidSubscriber())
			return RESPONSE_SUB_INVALID;
		if (subscriberDetail != null && subscriberDetail.getCircleID() != null)
			circleID = subscriberDetail.getCircleID();
		if (subscriberInfo.containsKey("OPERATOR_USER_INFO") && subscriberInfo.get("OPERATOR_USER_INFO")!=null)
			wdsResult=subscriberInfo.get("OPERATOR_USER_INFO");
		if (subscriberDetail != null)
			isPrepaid=subscriberDetail.isPrepaid();

		String prepaidYes = "y";
		if(!isPrepaid)
			prepaidYes = "n";

		
		cosID = map.get(PARAM_COS_ID);
		List<CosDetails> list= (List<CosDetails>) CacheManagerUtil.getCosDetailsCacheManager().getCosDetails(cosID);
		if(list!=null){
			for(int i=0;i<list.size();i++){
				if(list.get(i)!=null&&(list.get(i).getCircleId().equalsIgnoreCase("ALL")||list.get(i).getCircleId().equalsIgnoreCase(circleID))){
					cos=list.get(i);
				}
			}
			logger.info("RBT:: List is not null :");
		}
		logger.info("RBT:: cos :"+cos);
        
		HashMap<String, String> extraInfo= new HashMap<String, String>();
		extraInfo.put(EXTRA_INFO_WDS_QUERY_RESULT,wdsResult);
		/*if (map.containsKey(PARAM_COS_ID)) {
			cosID = map.get(PARAM_COS_ID);
			cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID, circleID);
			CosDetails cosCheck= _dbManager.getCos(phoneNumber, subscriber, circleID, isPrepaid?"y":"n", operator);

			if (cosCheck!=null)
				if (!cosID.equals(cosCheck.getCosId()))
					return RESPONSE_COS_INVALID;
			if (cos == null || cosCheck==null)
				return RESPONSE_COS_INVALID;
		}*/

		String transID = null;
		String actInfo = operator;
		if (map.containsKey(PARAM_TRANS_ID)) {
			transID = map.get(PARAM_TRANS_ID);
			actInfo = actInfo + ":trxid:" + transID + ":";
		}

		String retID = null;
		if (map.containsKey(PARAM_RETAILER_ID)) {
			retID = map.get(PARAM_RETAILER_ID);
			actInfo = actInfo + ":retid:" + retID + ":";
		}
		
	

		if(subscriber!=null&&subscriber.subYes().equals(STATE_ACTIVATED)&&isUpgrade.equalsIgnoreCase("true")){ 
			logger.info("RBT:: going to upgrade");
			
			if(cos==null)
				return RESPONSE_PORTAL_ERROR; 
			logger.info("RBT:: subclass  :"+cos.getSubscriptionClass());
			RBTClient rbtClient=RBTClient.getInstance(); 
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber.subID()); 
			com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber sub = rbtClient.getSubscriber(rbtDetailsRequest); 

			if(sub!=null){ 
				isPrepaid=sub.isPrepaid(); 
				circleID=sub.getCircleID(); 
			} 
			SubscriptionRequest subscriptionRequest=new SubscriptionRequest(subscriber.subID()); 
			subscriptionRequest.setCircleID(circleID); 
			subscriptionRequest.setIsPrepaid(isPrepaid); 
			subscriptionRequest.setMode(operator); 

			//subscriptionRequest.setCosID(cosId); 
			subscriptionRequest.setRentalPack(cos.getSubscriptionClass()); 
			//subscriptionRequest.setSubscriptionClass(subscriptionClass) 
			subscriptionRequest.setModeInfo(actInfo); 
			logger.info("RBT:: subrequest - "+subscriptionRequest);
			rbtClient.activateSubscriber(subscriptionRequest); 
			logger.info("RBT:: response - "+subscriptionRequest.getResponse());
			if(subscriptionRequest.getResponse()!=null&&subscriptionRequest.getResponse().equalsIgnoreCase("success")){ 
				return RESPONSE_UPGRADE_SUCCESS; 
			}else 
				return RESPONSE_PORTAL_ERROR; 
		} 

        if(cos==null || isUpgrade.equalsIgnoreCase("false")){
        	cos = CacheManagerUtil.getCosDetailsCacheManager().getDefaultCosDetail(circleID, prepaidYes);
        }
      //RBT-9873 Added null for xtraParametersMap for CG flow
		subscriber = _dbManager.activateSubscriber(phoneNumber, operator, null,
				null, isPrepaid, 0, 0, actInfo, cos.getSubscriptionClass(), true, cos,
				false, 0, extraInfo, circleID, null, false, null);

		if (subscriber == null) {
			logger.info("RBT::not able to activate subscriber - " + phoneNumber);
			return RESPONSE_PORTAL_ERROR;
		}
		
			return RESPONSE_ACTIVATION_ACCEPTED;
	}

	// Delete Account
	private final String RESPONSE_SUB_ALREADY_DEACTIVE = "5";
	private final String RESPONSE_DEACTIVATION_ACCEPTED = "99";

	/**
	 * This method processes the de-activation request of a user
	 * 
	 * @param map
	 *            The parameter name value pairs
	 * @return 99 - Deregistration is to be Processed(provisional
	 *         successResponse) 1 - Invalid Operator Parameter(s). 2 - Input
	 *         Parameter(s) format error 3 - Invalid user phonenumber(indicates
	 *         user is not a registered CRBT user) 5 - Deregistration is already
	 *         under processing. 8 - Portal error 9 - The user is a member of a
	 *         corp, Can't be deregistered
	 */
	private String processDeregestration(HashMap<String, String> map) {
		if (!map.containsKey(PARAM_PHONE_NUMBER))
			return RESPONSE_PARAMS_INVALID;
		String phoneNumber = map.get(PARAM_PHONE_NUMBER);
		Subscriber subscriber = _dbManager.getSubscriber(phoneNumber);
		if (subscriber == null || subscriber.subYes().equals(STATE_DEACTIVATED))
			return RESPONSE_SUB_INVALID;
		if (_dbManager.isSubscriberActivationPending(subscriber)
				|| subscriber.subYes().equals(STATE_CHANGE))
			return RESPONSE_SUB_ACT_PENDING;
		if (_dbManager.isSubDeactive(subscriber))
			return RESPONSE_SUB_ALREADY_DEACTIVE;

		String operator = map.get(PARAM_OPERATOR);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(phoneNumber);
		subscriptionRequest.setMode(operator);
		RBTClient.getInstance().deactivateSubscriber(subscriptionRequest);
		String result = subscriptionRequest.getResponse();
//		 _dbManager.deactivateSubscriber(phoneNumber, operator, null, true, true, true, false);
		logger.info("RBT::result for sub - " + phoneNumber
				+ " is - " + result);
		if (!result.equalsIgnoreCase("SUCCESS"))
			return RESPONSE_PORTAL_ERROR;
		return RESPONSE_DEACTIVATION_ACCEPTED;
	}

	// Query Setting
	private final String RESPONSE_LIBRARY_EMPTY = "4";

	/**
	 * @param map
	 *            The parameter name value pairs
	 * @return If the flag is "1", then the record in the Return string follows
	 *         the following format(indicates tone setting)
	 *         (flag|settingID|specialphone
	 *         |timetype|starttime|endtime|tonecode|loopno
	 *         |tonegroupid|tonevalidday|groupname) If the flag is "2", then the
	 *         record in the Return string follows the following format.
	 *         (indicates music box setting)
	 *         (flag|settingID|specialphone|timetype
	 *         |starttime|endtime|tonegroupid|nameindex|infoindex|groupname)
	 */
	private String processQuerySetting(HashMap<String, String> map) {
		if (!map.containsKey(PARAM_PHONE_NUMBER)
				|| !map.containsKey(PARAM_SET_TYPE))
			return RESPONSE_PARAMS_INVALID;
		String phoneNumber = map.get(PARAM_PHONE_NUMBER);
		String setTypeStr = map.get(PARAM_SET_TYPE);
		logger.info("RBT::phonenumber - " + phoneNumber
				+ ", settype - " + setTypeStr);

		Subscriber subscriber = _dbManager.getSubscriber(phoneNumber);
		if (subscriber == null || !subscriber.subYes().equals(STATE_ACTIVATED))
			return RESPONSE_SUB_INVALID;

		int setType = -1;
		try {
			setType = Integer.parseInt(setTypeStr);
		} catch (Exception e) {
			return RESPONSE_PARAMS_INVALID;
		}
		StringBuffer sb = new StringBuffer();
		SubscriberStatus[] allSel = _dbManager
				.getAllActiveSubSelectionRecords(phoneNumber);
		if (allSel == null)
			return RESPONSE_LIBRARY_EMPTY;
		switch (setType) {
		case 1:
			for (int i = 0; i < allSel.length; i++) {
				SubscriberStatus sel = allSel[i];
				if (sel.selStatus().equals(STATE_ACTIVATED)
						&& sel.callerID() == null)
					sb.append(getSettingString(sel));
			}
			break;
		case 2:
			for (int i = 0; i < allSel.length; i++) {
				SubscriberStatus sel = allSel[i];
				if (sel.selStatus().equals(STATE_ACTIVATED)
						&& sel.callerID() != null)
					sb.append(getSettingString(sel));
			}
			break;
		default:
			logger.info("RBT::invalid setType - "
					+ setType);
		return RESPONSE_PARAMS_INVALID;
		}
		if (sb.length() == 0)// it means there were no selections corresponding
			// to passes setType
			return RESPONSE_LIBRARY_EMPTY;
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * This method actually formats the selection into string as indicated in
	 * the return tag
	 * 
	 * @param sel
	 * @return If the flag is "1", then the record in the Return string follows
	 *         the following format(indicates tone setting)
	 *         (flag|settingID|specialphone
	 *         |timetype|starttime|endtime|tonecode|loopno
	 *         |tonegroupid|tonevalidday|groupname) If the flag is "2", then the
	 *         record in the Return string follows the following format.
	 *         (indicates music box setting)
	 *         (flag|settingID|specialphone|timetype
	 *         |starttime|endtime|tonegroupid|nameindex|infoindex|groupname)
	 * 
	 *         settingID = subID if setting is for all else subID+calledID
	 *         loopNo = clipid tonegroupID = settingID
	 */
	private String getSettingString(SubscriberStatus sel) {
		StringBuffer sb = new StringBuffer();
		try {
			int flag = 1;
			if (sel.categoryType() == SHUFFLE)
				flag = 2;
			String splPhone = (sel.callerID() == null) ? "" : sel.callerID();
			String settingID = getSettingID(sel);

			sb.append(flag);
			sb.append("|");
			sb.append(settingID);
			sb.append("|");
			sb.append(splPhone);
			sb.append("|");
			sb.append("0|2003-01-01 00:00:00|2003-01-01 23:59:59|");
			if (flag == 1) {
				ClipMinimal clip = _dbManager.getClipRBT(sel.subscriberFile());
				sb.append(clip.getClipId());
				sb.append("|");
				// loopNo is also clip id
				sb.append(clip.getClipId());
				sb.append("|");
				// tone group id is same as setting id
				sb.append(settingID);
				sb.append("|");
				sb.append(_formatter.format(clip.getEndTime()));
				sb.append("|&");
				// group-name can be ignored
			} else if (flag == 2) {
				sb.append(sel.categoryID());
				sb.append("|0|0|&");
			}
		} catch (Exception e) {
			logger.error("", e);
			// return "&"; Document does not say to return & in case of error
		}
		return sb.toString();
	}

	/**
	 * this methods calculates the settingID for a selections entry
	 * 
	 * @param sel
	 *            Setting for which id has to be made
	 * @return subID if callerID = null, (subID+callerID).subString(0, 20)
	 */
	private String getSettingID(SubscriberStatus sel) {
		String splPhone = (sel.callerID() == null) ? "" : sel.callerID();
		String settingID = sel.subID() + splPhone;
		if (settingID.length() > 18) {
			int endIndex = (settingID.length() > 20) ? 20 : settingID.length();
			settingID = settingID.substring(2, endIndex);
		}
		return settingID;
	}

	// Add Setting
	private final String RESPONSE_ADD_SETTING_SUCCESS = "0";
	private final String RESPONSE_TONE_CODE_INVALID = "4";
	private final String RESPONSE_SETTING_EXISTS = "9";

	/**
	 * This method adds selection entry to the table after validating all the
	 * parameters.
	 * 
	 * @param map
	 *            The parameter name value pairs
	 * @return 1 - Invalid Operator Parameter(s). 2 - Input Parameter(s) format
	 *         error 3 - Invalid user phone number. (User is not a registered
	 *         CRBT user ) 4 - Invalid tone group id/tone code. 5 - The time
	 *         segment for the new setting overlaps with the time segment of the
	 *         existing setting. 6 - The start time is lesser than the current
	 *         time 7 - The start time is greater than the end time 8 - Portal
	 *         error 9 - The (same) song is already set for the same time
	 *         segment. 10 - Maximum number of songs have already been added to
	 *         the ring loop.Cannot add more songs to the ring Loop. 11 -
	 *         Invalid group number. 12 - Maximum number of settings For the
	 *         user have been made. Cannot add more settings. 14 - Disabled in
	 *         user's COS.
	 */
	private String processAddSetting(HashMap<String, String> map) {
		logger.info("RBT::map-" + map);
		if (!map.containsKey(PARAM_PHONE_NUMBER)
				|| !map.containsKey(PARAM_SET_TYPE)
				|| !map.containsKey(PARAM_TONE_CODE)
				|| !map.containsKey(PARAM_TONE_FLAG)) {
			logger.info("RBT::invalid params");
			return RESPONSE_PARAMS_INVALID;
		}

		String setTypeStr = map.get(PARAM_SET_TYPE);
		int setType = -1;
		try {
			setType = Integer.parseInt(setTypeStr);
		} catch (Exception e) {
			setType = 1;
		}
		if (setType != 1 && setType != 2)
			return RESPONSE_PARAMS_INVALID;

		String phoneNumber = map.get(PARAM_PHONE_NUMBER);
		if (!_dbManager.isValidPrefix(phoneNumber))
			return RESPONSE_SUB_INVALID;
		Subscriber subscriber = _dbManager.getSubscriber(phoneNumber);
		if (subscriber == null || !subscriber.subYes().equals(STATE_ACTIVATED))
			return RESPONSE_SUBSCRIBER_INVALID;

		HashMap<String,String> subscriberInfo=_dbManager.getSubscriberInfo(phoneNumber);
		HashMap<String, String> extraInfo=new HashMap<String, String>();

//		String circleID=null;
//		boolean isPrepaid=false;
		String wdsResult=null;
		if (subscriberInfo.containsKey("STATUS") && subscriberInfo.get("STATUS")!=null && !subscriberInfo.get("STATUS").equalsIgnoreCase("VALID"))
			return RESPONSE_SUB_INVALID;
//		if (subscriberInfo.containsKey("CIRCLE_ID") && subscriberInfo.get("CIRCLE_ID")!=null)
//			circleID=subscriberInfo.get("CIRCLE_ID");
		if (subscriberInfo.containsKey("WDS_RESPONSE") && subscriberInfo.get("WDS_RESPONSE")!=null){
			wdsResult=subscriberInfo.get("WDS_RESPONSE");
			extraInfo.put(EXTRA_INFO_WDS_QUERY_RESULT, wdsResult);
		}
//		if (subscriberInfo.containsKey("USER_TYPE") && subscriberInfo.get("USER_TYPE")!=null)
//			isPrepaid=subscriberInfo.get("USER_TYPE").equalsIgnoreCase("PREPAID");
		
		String toneFlagStr = map.get(PARAM_TONE_FLAG);
		int toneFlag = -1;
		try {
			toneFlag = Integer.parseInt(toneFlagStr);
		} catch (Exception e) {
		}
		if (toneFlag != 0 && toneFlag != 1)
			return RESPONSE_PARAMS_INVALID;

		String toneCode = map.get(PARAM_TONE_CODE);
		ClipMinimal clip = null;
		Category category = null;
		try {
			if (toneFlag == 0) {
				clip = _dbManager.getClipMinimalPromoID(toneCode, false);
				if (clip == null)
					return RESPONSE_TONE_CODE_INVALID;
			} else {
				category = _dbManager.getCategoryPromoID(toneCode);
				if (category == null)
					return RESPONSE_TONE_CODE_INVALID;
			}
		} catch (Exception e) {
		}

		String splPhone = map.get(PARAM_SPECIAL_PHONE);
		if (setType == 1)
			splPhone = null;

		String operator = map.get(PARAM_OPERATOR);
		String subWavFile = null;
		if (clip != null)
			subWavFile = clip.getWavFile();
		else if (category != null) {
			Clips[] clips = _dbManager.getAllClips(category.getID());
			if (clips != null && clips.length > 0)
				subWavFile = clips[0].wavFile();
		}

		String transID = null;
		String selectionInfo = operator;
		if (map.containsKey(PARAM_TRANS_ID)) {
			transID = map.get(PARAM_TRANS_ID);
			selectionInfo = selectionInfo + ":trxid:" + transID + ":";
		}
		String retID = null;
		if (map.containsKey(PARAM_RETAILER_ID)) {
			retID = map.get(PARAM_RETAILER_ID);
			selectionInfo = selectionInfo + ":retid:" + retID + ":";
		}

		boolean result = _dbManager.addSubscriberSelections(phoneNumber,
				splPhone, (category == null) ? 3 : category.getID(),
				subWavFile, null, null, null, 1, operator, selectionInfo, 0,
				true, false, null, 0, 2359, null, true, false, null, null,
				subscriber.subYes(), null, true, false, true, subscriber
						.subscriptionClass(), subscriber, null,extraInfo);

		if (result)
			return RESPONSE_ADD_SETTING_SUCCESS;
		return RESPONSE_SETTING_EXISTS;
	}

	// Delete Song Setting
	private final String RESPONSE_DELETE_SONG_SETTING_SUCCESS = "0";
	private final String RESPONSE_INVALID_SETTING = "4";

	/**
	 * This method processes song setting deletion
	 * 
	 * @param map
	 *            The parameter name value pairs
	 * @return 0 - Operation succeeded. Chosen setting is deleted. 1 - Invalid
	 *         operator parameter(s). 2 - Input Parameter(s) format error 3 -
	 *         Invalid user phone number.(User is not a registered CRBT user) 4
	 *         - Invalid setting (tone group id / loop no) 5 - Tone code
	 *         invalid/ Tone code does not belong to tone group id 8 - Portal
	 *         error
	 */
	private String processDeleteSongSetting(HashMap<String, String> map) {
		if (!map.containsKey(PARAM_PHONE_NUMBER)
				|| !map.containsKey(PARAM_TONE_GROUP_ID)
				|| !map.containsKey(PARAM_LOOP_NO))
			return RESPONSE_PARAMS_INVALID;

		String phoneNumber = map.get(PARAM_PHONE_NUMBER);
		if (!_dbManager.isValidPrefix(phoneNumber))
			return RESPONSE_SUB_INVALID;
		Subscriber subscriber = _dbManager.getSubscriber(phoneNumber);
		if (subscriber == null || !subscriber.subYes().equals(STATE_ACTIVATED))
			return RESPONSE_SUB_INVALID;

		String toneGroupID = map.get(PARAM_TONE_GROUP_ID);
		String loopNo = map.get(PARAM_LOOP_NO);

		ClipMinimal clip = null;
		try {
			clip = _dbManager.getClipById(Integer.parseInt(loopNo));
		} catch (Exception e) {
		}
		if (clip == null) {
			logger.info("RBT::invalid loopNo (clipID) - "
					+ loopNo);
			return RESPONSE_INVALID_SETTING;
		}

		String operator = map.get(PARAM_OPERATOR);
		SubscriberStatus[] allSel = _dbManager
				.getAllActiveSubSelectionRecords(phoneNumber);
		if (allSel == null)
			return RESPONSE_INVALID_SETTING;

		for (int i = 0; i < allSel.length; i++) {
			SubscriberStatus sel = allSel[i];
			ClipMinimal thisClip = _dbManager.getClipRBT(sel.subscriberFile());
			String settingID = getSettingID(sel);
			if (settingID.equals(toneGroupID)
					&& (thisClip.getClipId() == clip.getClipId())) {
				logger.info("RBT::found song setting to delete.settingID - "
								+ settingID + ", clipID - " + loopNo);
				if (_dbManager.deactivateSubscriberRecordWavFile(phoneNumber,
						sel.callerID(), sel.status(), sel.fromTime(), sel
								.toTime(), true, operator,
						sel.subscriberFile(), sel.selInterval()))
					return RESPONSE_DELETE_SONG_SETTING_SUCCESS;
			}
		}
		return RESPONSE_INVALID_SETTING;
	}

	// Delete Song Setting
	private final String RESPONSE_DELETE_MB_SETTING_SUCCESS = "0";
	private final String RESPONSE_INVALID_SETTING_ID = "4";

	/**
	 * This method processes MB setting deletion
	 * 
	 * @param map
	 *            The parameter name value pairs
	 * @return 0 - Operation succeeded. Chosen setting is deleted. 1 - Invalid
	 *         operator parameter(s). 2 - Input Parameter(s) format error 3 -
	 *         Invalid user phone number. (User is not a registered CRBT user) 4
	 *         - Invalid settingID 8 - Portal error
	 */
	private String processDeleteMBSetting(HashMap<String, String> map) {
		if (!map.containsKey(PARAM_PHONE_NUMBER)
				|| !map.containsKey(PARAM_SETTING_ID))
			return RESPONSE_PARAMS_INVALID;

		String phoneNumber = map.get(PARAM_PHONE_NUMBER);
		if (!_dbManager.isValidPrefix(phoneNumber))
			return RESPONSE_SUB_INVALID;
		Subscriber subscriber = _dbManager.getSubscriber(phoneNumber);
		if (subscriber == null || !subscriber.subYes().equals(STATE_ACTIVATED))
			return RESPONSE_SUB_INVALID;

		String operator = map.get(PARAM_OPERATOR);
		String settingID = map.get(PARAM_SETTING_ID);

		SubscriberStatus[] allSel = _dbManager
				.getAllActiveSubSelectionRecords(phoneNumber);
		if (allSel == null)
			return RESPONSE_INVALID_SETTING_ID;

		for (int i = 0; i < allSel.length; i++) {
			SubscriberStatus sel = allSel[i];
			String thisSettingID = getSettingID(sel);
			if (thisSettingID.equals(settingID)) {
				logger.info("RBT::found song setting to delete.settingID - "
								+ settingID);
				if (_dbManager.deactivateSubscriberRecordWavFile(phoneNumber,
						sel.callerID(), sel.status(), sel.fromTime(), sel
								.toTime(), true, operator,
						sel.subscriberFile(), sel.selInterval()))
					return RESPONSE_DELETE_MB_SETTING_SUCCESS;
			}
		}
		return RESPONSE_INVALID_SETTING_ID;
	}

	// Query Song
	private final String RESPONSE_SONG_NOT_FOUND = "3";
	private final String RESPONSE_SONG_FOUND = "0";

	/**
	 * Queries the song from DB based on song name
	 * 
	 * @param map
	 *            The parameter name value pairs
	 * @return 0 Success 1 Invalid Operator Parameter(s). 2 Input Parameter(s)
	 *         format error). 3 Song not found 4 More than one song found
	 *         (latest uploaded one will be returned) 8 internal system error
	 * 
	 *         if a valid song is found info is sent in the following format
	 *         (along with the above code)
	 * 
	 *         
	 *         Tonecode|Singer|Toneinfo|Price|AdminOperationTime|Status|Refusereason
	 *         |Spid| SPName|Remark|Expiry
	 *         date|Uploadtime|Filmname|Directorname|musiccompany|
	 *         Relativedate|WebCategory|WebSub-category
	 */
	private String processSongRequest(HashMap<String, String> map) {
		if (!map.containsKey(PARAM_SONG_NAME))
			return RESPONSE_PARAMS_INVALID;

		String songName = map.get(PARAM_SONG_NAME);
		ClipMinimal clip = _dbManager.getClipByName(songName);
		if (clip == null) {
			logger.info("RBT::no clip with name - "
					+ songName);
			return RESPONSE_SONG_NOT_FOUND;
		} else
			logger.info("RBT::clip id - "
					+ clip.getClipId() + " for songName - " + songName);

		StringBuffer sb = new StringBuffer(RESPONSE_SONG_FOUND + "|");
		sb.append(getStringOrEmpty(clip.getClipId()));
		sb.append("|");
		sb.append(getStringOrEmpty(clip.getArtist()));
		sb.append("|");
		sb.append("|");// tone info is empty always
		ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(clip.getClassType());
		sb.append(getStringOrEmpty(chargeClass.getAmount()));
		sb.append("|");
		sb.append(_formatter.format(clip.getSmsTime()));
		sb.append("|");
		int status = 1;
		if (clip.getEndTime().before(new Date()))
			status = 2;
		sb.append(status);
		sb.append("|");// status
		sb.append("|");// refuse reason is empty
		sb.append("|");// SP id is empty
		sb.append("|");// SP name is empty
		sb.append("0|");// remark
		sb.append(_formatter.format(clip.getEndTime()));
		sb.append("|");
		sb.append(_formatter.format(clip.getSmsTime()));
		sb.append("|");
		sb.append(getStringOrEmpty(clip.getAlbum()));
		sb.append("|");
		sb.append("|");// director is empty
		sb.append("|");// music company is empty
		sb.append("|");// relative is empty
		sb.append("Batch|Batch");// web cat and web-sub cat

		return sb.toString();
	}

	private String processRetailerSongRequest(HashMap<String, String> map) {
		logger.info("RBT::entered process retailer request");
		if (!map.containsKey(PARAM_COS_ID)
				|| !map.containsKey(PARAM_TRANS_ID)) {
			return RESPONSE_PARAMS_INVALID;
		}

		if (_dbManager.m_doRetailerCheck) {
			boolean canProceed = false;
			canProceed = retailerCheck((String) map.get(PARAM_RETAILER_ID));
			if (!canProceed)
				return RESPONSE_RETAILER_INVALID;
		}

		if (!map.containsKey(PARAM_PHONE_NUMBER)) {
			return RESPONSE_PARAMS_INVALID;
		}

		if (!map.containsKey(PARAM_TONE_CODE)
				|| map.get(PARAM_TONE_CODE) == null || ((String) map.get(PARAM_TONE_CODE)).equals("null")) {
			String result = processRegestration(map);
			if (result.equalsIgnoreCase("4"))
				result = "6";
			if (result.equalsIgnoreCase("5"))
				result = "7";
			if (result.equalsIgnoreCase("7"))
				result = "16";

			return result;
		}

		if (map.containsKey(PARAM_TONE_CODE)) {

			String promoId = null;
			promoId = map.get(PARAM_TONE_CODE);
			Clips clip = _dbManager.getClipByPromoID(promoId);
			if (clip == null) {
				return RESPONSE_TONE_CODE_INVALID;
			}

			map.put(PARAM_SPECIAL_PHONE, null);
			map.put(PARAM_START_TIME, null);
			map.put(PARAM_END_TIME, null);
			map.put(PARAM_TIME_TYPE, null);
			map.put(PARAM_SET_TYPE, null);
			map.put(PARAM_FLAG, null);
			map.put(PARAM_TONE_FLAG, "0");

			String result = processAddSetting(map);
			return result;
		}

		return null;
	}

	// Query Musicbox
	private final String RESPONSE_MB_FOUND = "0";
	private final String RESPONSE_MB_NOT_FOUND = "3";

	/**
	 * This method queries for a category by name
	 * 
	 * @param map
	 *            The parameter name value pairs
	 * @return 0 - Success(in this case parameter below will be returned) 1 -
	 *         Invalid Operator Parameter(s). 2 - Input Parameter(s) format
	 *         error 3 - Music box not found 4 - More than one music box found 8
	 *         - internal system error
	 * 
	 *         Tonegroupid|Creator|Price|Status|Uploadtime|Creator
	 *         type|refusereason|Description|NoOfSong|Tonecode
	 */
	private String processMBRequest(HashMap<String, String> map) {
		if (!map.containsKey(PARAM_MB_NAME))
			return RESPONSE_PARAMS_INVALID;

		String catName = map.get(PARAM_MB_NAME);
		Category category = _dbManager.getCategoryByName(catName);
		if (category == null) {
			logger.info("RBT::No category by name - "
					+ catName);
			return RESPONSE_MB_NOT_FOUND;
		} else
			logger.info("RBT::MB id - " + category.getID()
					+ " for - " + catName);
		StringBuffer sb = new StringBuffer();
		sb.append(RESPONSE_MB_FOUND);
		sb.append("|");
		sb.append(category.getID());
		sb.append("|");
		sb.append("OnMobile");
		sb.append("|");// creator is always OM
		ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(category
				.getClassType());
		sb.append(getStringOrEmpty(chargeClass.getAmount()));
		sb.append("|");
		sb.append("1|");// status - normal
		sb.append("2|");// creator type
		sb.append("|");// refuse reason is empty
		sb.append("|");// description is empty
		Clips[] clips = _dbManager.getAllClips(category.getID());
		int length = 0;
		String toneCode = "";
		;
		if (clips != null) {
			length = clips.length;
			StringBuffer clipIDs = new StringBuffer();
			for (int i = 0; i < clips.length; i++)
				clipIDs.append(clips[i].id());
			toneCode = clipIDs.toString();
		}
		sb.append(length);
		sb.append("|");
		sb.append(toneCode);
		sb.append("|");
		return sb.toString();
	}

	// Gift Song
	private final String RESPONSE_GIFT_ACCEPTED = "99";
	private final String RESPONSE_ACCEPTOR_INVALID = "4";
	private final String RESPONSE_GIFT_SONG_INVALID = "7";
	private final String RESPONSE_GIFT_SONG_ALREADY_PENDING = "9";
	private final String RESPONSE_GIFT_SONG_ALREADY_DOWNLOADED = "10";
	// private final String RESPONSE_ACCEPTOR_LIBRARY_FULL = "11";//not
	// supported
	private final String RESPONSE_GIFTER_ACCEPTOR_SAME = "16";

	/**
	 * Takes the gift request for a song + subscription (if not subscribed)
	 * 
	 * @param map
	 *            The parameter name value pairs
	 * @return 0 Succeess 99 Request is accepted 1 Invalid Operator
	 *         Parameter(s). 2 Input Parameter(s) format error 3 Invalid user
	 *         phonenumber. (A party number is not a CRBT User or Blacklist
	 *         user) 5 User B is not a CRBT (user B is non CRBT user when
	 *         Automatic service gifting not allowed i.e. flag is 1 (or ) B
	 *         status in t_userinfo(6,7,9) (or) service gifting is under
	 *         processing) 4 Invalid user B ( e.g. B party is not a TTSL
	 *         subscriber ) 6 User B is blacklisted user 7 Song does not exist 9
	 *         The song being presented has already been downloaded by the
	 *         prepaid acceptor. (The prepaid acceptor's download song is in
	 *         under process.) 10 Accepter has already downloaded the song 11
	 *         Personal favorite library of the accepter is full 12 Sender is a
	 *         Corp member(Corp member can not Gift song) 13 Max no of
	 *         registrations for the day reached. 8 Portal error. 14 Disabled in
	 *         user's COS. 16 Sender and accepter phonenumbers are same.(cannot
	 *         gift service or song to self) 15 Insufficient balance(used when
	 *         real time charging is enabled for prepaid)
	 */
	private String processGiftSong(HashMap<String, String> map) {
		if (!map.containsKey(PARAM_PHONE_NUMBER)
				|| !map.containsKey(PARAM_ACCEPT_PHONE_NUMBER)
				|| !map.containsKey(PARAM_TONE_CODE)
				|| !map.containsKey(PARAM_FLAG))
			return RESPONSE_PARAMS_INVALID;

		String phoneNumber = map.get(PARAM_PHONE_NUMBER);
		String acceptPhoneNumber = map.get(PARAM_ACCEPT_PHONE_NUMBER);
		String toneCodeStr = map.get(PARAM_TONE_CODE);
		String flagStr = map.get(PARAM_FLAG);

		if (phoneNumber.equals(acceptPhoneNumber))
			return RESPONSE_GIFTER_ACCEPTOR_SAME;
		if (!_dbManager.isValidPrefix(phoneNumber))
			return RESPONSE_SUB_INVALID;
		if (!_dbManager.isValidPrefix(acceptPhoneNumber))
			return RESPONSE_ACCEPTOR_INVALID;

		int flag = -1;
		try {
			flag = Integer.parseInt(flagStr);
		} catch (Exception e) {
		}
		if (flag != 0 && flag != 1)
			return RESPONSE_PARAMS_INVALID;

		if (toneCodeStr == null)
			return RESPONSE_PARAMS_INVALID;

		Subscriber subscriber = _dbManager.getSubscriber(phoneNumber);
		if (subscriber == null || !subscriber.subYes().equals(STATE_ACTIVATED))
			return RESPONSE_SUB_INVALID;

		ClipMinimal clip = _dbManager.getClipMinimalPromoID(toneCodeStr, false);
		if (clip == null)
			return RESPONSE_GIFT_SONG_INVALID;

		String operator = map.get(PARAM_OPERATOR);

		Subscriber acceptSubscriber = _dbManager
				.getSubscriber(acceptPhoneNumber);
		if (acceptSubscriber != null) {
			SubscriberStatus[] sels = _dbManager
					.getAllActiveSubSelectionRecords(acceptPhoneNumber);
			for (int i = 0; sels != null && i < sels.length; i++) {
				SubscriberStatus thisSel = sels[i];
				int clipID = -1;
				if (thisSel.callerID() == null
						&& thisSel.categoryType() != SHUFFLE) {
					ClipMinimal thisClip = _dbManager.getClipRBT(thisSel
							.subscriberFile());
					if (thisClip != null)
						clipID = thisClip.getClipId();
				}
				if (clipID == clip.getClipId()) {
					if (thisSel.selStatus().equals(STATE_ACTIVATED))
						return RESPONSE_GIFT_SONG_ALREADY_DOWNLOADED;
					else
						return RESPONSE_GIFT_SONG_ALREADY_PENDING;
				}
			}
		}

		ViralSMSTable gift = _dbManager.insertViralSMSTableMap(phoneNumber,
				new Date(), "GIFT", acceptPhoneNumber, clip.getClipId() + "",
				0, operator, null, null);
		if (gift != null)
			return RESPONSE_GIFT_ACCEPTED;
		else
			return RESPONSE_PORTAL_ERROR;
	}

	private String getStringOrEmpty(int i) {
		return i + "";
	}

	private String getStringOrEmpty(String str) {
		if (str == null)
			return "";
		return str;
	}

	private boolean validOperatorParams(HashMap<String, String> map) {

		String userName = null;
		String password = null;
		if (!map.containsKey(PARAM_OPERATOR)
				|| !map.containsKey(PARAM_OPERATOR_ACCOUNT)
				|| !map.containsKey(PARAM_OPERATOR_PWD))
			return false;

		userName = map.get(PARAM_OPERATOR_ACCOUNT).trim();
		password = map.get(PARAM_OPERATOR_PWD).trim();
		logger.info("The username and password from parameters " + userName
						+ " and " + password);
		logger.info("The hashmap m_userPwdMap " + m_userPwdMap.size()
						+ " and password in map " + m_userPwdMap.get(userName));
		if (m_userPwdMap != null && m_userPwdMap.containsKey(userName)) {
			if (m_userPwdMap.get(userName) != null
					&& m_userPwdMap.get(userName).equals(password))
				return true;
		} else {
			return false;
		}
		return false;
	}

	public boolean retailerCheck(String retailerID) {
		return _dbManager.retailerCheck(retailerID);
	}

}