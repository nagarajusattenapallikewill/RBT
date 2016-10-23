package com.onmobile.apps.ringbacktones.daemons.implementation;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

import com.huawei.ivas.BaseEvt;
import com.huawei.ivas.BaseQueryEvt;
import com.huawei.ivas.BaseUpdateEvt;
import com.huawei.ivas.Response;
import com.huawei.ivas.info.SettingInfo;
import com.huawei.ivas.info.ToneInfo;
import com.huawei.ivas.info.UserInfo;
import com.huawei.ivas.usermanage.UserManage;
import com.huawei.ivas.usermanage.UserManageServiceLocator;
import com.huawei.ivas.usermanage.event.EditUserEvt;
import com.huawei.ivas.usermanage.event.QueryUserEvt;
import com.huawei.ivas.usermanage.response.QueryUserResp;
import com.huawei.ivas.usertonemanage.UserToneManage;
import com.huawei.ivas.usertonemanage.UserToneManageServiceLocator;
import com.huawei.ivas.usertonemanage.event.AddGroupEvt;
import com.huawei.ivas.usertonemanage.event.AddGroupMemberEvt;
import com.huawei.ivas.usertonemanage.event.AddTbToneEvt;
import com.huawei.ivas.usertonemanage.event.AddToneBoxEvt;
import com.huawei.ivas.usertonemanage.event.DelGroupEvt;
import com.huawei.ivas.usertonemanage.event.DelGroupMemberEvt;
import com.huawei.ivas.usertonemanage.event.DelTbToneEvt;
import com.huawei.ivas.usertonemanage.event.EditToneBoxEvt;
import com.huawei.ivas.usertonemanage.event.QuerySettingEvt;
import com.huawei.ivas.usertonemanage.event.QueryTbToneEvt;
import com.huawei.ivas.usertonemanage.event.SetToneEvt;
import com.huawei.ivas.usertonemanage.response.AddGroupResp;
import com.huawei.ivas.usertonemanage.response.AddToneBoxResp;
import com.huawei.ivas.usertonemanage.response.QuerySettingResp;
import com.huawei.ivas.usertonemanage.response.QueryTbToneResp;
import com.huawei.ivas.usertonemanage.response.SetToneResp;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.interfaces.iPlayerModel;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;

/**
 * SOAP Interface implementation to interface with Voda Romania Huawei server
 * @author Sreekar
 * @Date 2009-01-04
 * 
 * @edited Sreekar, added groups feature
 * @Date 2009-02-24
 */
public class VodaRomaniaPlayerImpl implements iPlayerModel, iRBTConstant {
	private static Logger logger = Logger.getLogger(VodaRomaniaPlayerImpl.class);
	
	//Configuration parameters
	protected static final String HTTP_URL = "HTTP_URL_VR";
	protected static final String SUBSCRIBER_URL = "SUBSCRIBER_URL_VR";
	protected static final String SETTING_URL = "SETTING_URL_VR";
	protected static final String PORTAL_ACCOUNT = "PORTAL_ACCOUNT_VR";
	protected static final String PORTAL_PWD = "PORTAL_PWD_VR";
	protected static final String PORTAL_TYPE = "PORTAL_TYPE_VR";
	
	//Constants
	protected static final String URL = "URL";
	protected static final String MODE = "MODE";
	protected static final String PHONE_NUMBER = "PHONE_NUMBER";
	protected static final String QUERY_TYPE = "QUERY_TYPE";
	protected static final String START_RECORD_NUMBER = "START_RECORD_NUMBER";
	protected static final String END_RECORD_NUMBER = "END_RECORD_NUMBER";
	private static final int SETTING_ERROR = 0;
	private static final int SETTING_EXISTS = 1;
	private static final int SETTING_NOT_EXISTS = 2; //setting for the caller exists, but not for this song
	private static final int SETTING_NEW = 3; //setting for the caller doesn't exist
	private static final String SETTING_TYPE = "SETTING_TYPE";
	private static final String TONEBOX_ID = "TONEBOX_ID";
	/*
	 * Huawei setTypeCodes
	 */
	private static final int SET_TYPE_DEFAULT = 2;
	private static final int SET_TYPE_GROUP = 3;
	private static final int SET_TYPE_PERSONAL = 4;
	
	private RBTDBManager _dbManager; 
	private ParametersCacheManager paramCacheManager;
	
//	private Hashtable<String, String> _daemonParameterMap;
	private HttpParameters _httpParams = null;
	private Hashtable<String, String> _portalTypeMap = new Hashtable<String, String>();
	
	//Huawei SOAP Objects
	private UserManage _userManage = null;
	private UserToneManage _userToneManage = null;


	public VodaRomaniaPlayerImpl(RBTDBManager dbManager)
			throws RBTException {
		_dbManager = dbManager;
		paramCacheManager = CacheManagerUtil.getParametersCacheManager();
		init();
	}
	
	private void init() throws RBTException 
	{
		String portalType = getParamAsString("DAEMON", PORTAL_TYPE, null);
		if (portalType != null) {
			logger.info("RBT::" + PORTAL_TYPE + " - " + portalType);
			StringTokenizer stk = new StringTokenizer(portalType, ";");
			while (stk.hasMoreTokens()) {
				String token = stk.nextToken();
				try {
					_portalTypeMap.put(token.substring(0, token.indexOf(",")), token
							.substring(token.indexOf(",") + 1));
				}
				catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		else
			logger.info("RBT::Parameter " + PORTAL_TYPE + " not defined");
		
		//Initing Huawei objects
		initUserManage();
		initUserToneManage();
	}
	
	/**
	 * This method queries the subscriber status at Huawei and returns the same
	 * 
	 * @param subscriberID
	 * @return Subscriber status at Huawei
	 */
	public String getSubscriberStatus(String subscriberID, boolean isPrepaid) {
		subscriberID = _dbManager.subID(subscriberID);
		QueryUserEvt queryUserEvt = new QueryUserEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		setBaseParams(queryUserEvt, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		queryUserEvt.setPhoneNumber(subscriberID);
		sb.append("&phoneNumber=" + subscriberID);
		
		QueryUserResp response = null;
		Date requestTime = new Date();
		try {
			response = _userManage.query(queryUserEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		String returnCode = null;
		String status = null;
		if(response != null) {
			returnCode = response.getReturnCode();
			UserInfo[] userInfo = response.getUserInfos();
			if(userInfo != null && userInfo.length > 0)
				status = userInfo[0].getStatus();
		}
		writeSDR(new SDR("QUERY_SUBSCRIBER", subscriberID, isPrepaid, requestTime, responseTime, sb
				.toString(), returnCode + "," + status));
		if(status != null)
			return getSubStatusFromInfo(subscriberID, response);
		else
			return "New User";
	}
	
	public String getSubStatusFromInfo(String subscriberID, QueryUserResp response) {
		if(response == null)
			return TECHNICAL_DIFFICULTIES;
		
		UserInfo[] userInfo = response.getUserInfos();
		if (userInfo == null) {
			if (userInfo.length == 0)
				return "New User";
			if (userInfo.length < 0 || (userInfo[0].getStatus() == null))
				logger.info("RBT::Invalid response " + response.getReturnCode()
						+ " for " + subscriberID);
			return TECHNICAL_DIFFICULTIES;
		}
		String status = userInfo[0].getStatus();
		try {
			int userStatus = Integer.parseInt(status);
			switch(userStatus) {
				case 1:
					return "Before creating";
				case 2:
					return "Created";
				case 3:
					return "Before deleting";
				case 4:
					return "Deleted";
				case 5:
					return "Suspended";
				case 6:
					return "Being created";
				case 7:
					return "Being deleted";
				case 8:
					return "DP due to fee due";
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		logger.info("RBT::status is " + status);
		return ERROR;
	}
	
	/**
	 * Creates a setting Object similar to Huawei's setting Object
	 * @param setting setting object
	 * @return return's the Setting object
	 * 
	 * @see Setting
	 */
	private Setting getChotaSetting(SubscriberStatus setting) {
		String loopType = getLoopType(setting.loopStatus());
		String resourceType = setting.categoryType() == 0 ? "2" : "1";
		String timeType = getTimeType(setting.status());
		String startTime = getHuaweiStartDate(setting);
		String endTime = getHuaweiEndDate(setting);
		String callerID = setting.callerID();
		if(callerID != null && callerID.startsWith("G"))
			callerID = callerID.substring(1);
		Setting chotaSetting = new Setting(callerID, resourceType, timeType, startTime,
				endTime, setting.subscriberFile(), loopType);
		return chotaSetting;
	}
	
	/**
	 * 
	 * @param chotaSetting
	 * @param allSettings
	 * @return HashMap containing the SettingType for chotaSetting like
	 *         new/existing in Key SETTING_TYPE and ToneBoxID if setting Exists in Key TONEBOX_ID
	 */
	private HashMap<String, String> checkSettingExists(Setting chotaSetting,
			SettingInfo[] allSettings, SubscriberStatus setting) {
		// checking if it has to be a new setting
		int settingStatus = SETTING_NEW;
		HashMap<String, ToneInfo[]> toneBoxes = new HashMap<String, ToneInfo[]>();
		int totalSettings = allSettings.length;
		logger.info("RBT::totalSettings in huawei -> " + totalSettings);
		logger.info("RBT::caller-" + chotaSetting.caller() + ", resourceType-"
				+ chotaSetting.resourceType() + ", timeType-" + chotaSetting.timeType() + ", startTime-"
				+ chotaSetting.startTime() + ", endTime-" + chotaSetting.endTime()
				+ ", resourceCode-" + chotaSetting.resourceCode() + ", loopType-"
				+ chotaSetting.loopType());
		for(int c = 0; allSettings != null && c < totalSettings; c++) {
			int compareSettingRes = compareSetting(allSettings[c], chotaSetting, toneBoxes, setting);
			if(compareSettingRes == SETTING_ERROR) {
				settingStatus = compareSettingRes;
				break;
			}
			if(compareSettingRes != SETTING_NEW) {
				settingStatus = compareSettingRes;
				if(settingStatus == SETTING_EXISTS)
					break;
			}
		}
		logger.info("RBT:: number of TB matches - " + toneBoxes.size()
				+ ", settingStatus - " + settingStatus);
		String toneBoxID = null;
		if(toneBoxes.size() > 0)
			toneBoxID = toneBoxes.keySet().iterator().next();
		HashMap<String, String> returnMap = new HashMap<String, String>();
		returnMap.put(SETTING_TYPE, settingStatus+"");
		returnMap.put(TONEBOX_ID, toneBoxID);
		return returnMap;
	}

	/**
	 * Updates setting status to Huawei. First the setting has to be queried for existence and if
	 * not present then the Tonebox has to be either added or updated and then perform setTone.
	 * Tonebox will be added if setting doesn't exist, if setting exists then we have to do
	 * addTbTone and then setTone
	 */
	public String updateSetting(SubscriberStatus setting) {
		String settingStr = getSettingString(setting);
		
		QuerySettingResp querySettingResult = querySetting(setting);
		if(querySettingResult == null) {
			logger.info("RBT::null query setting result returning failure");
			return FAILURE;
		}

		SettingInfo[] allSettings = querySettingResult.getSettingInfos();
		Setting chotaSetting = getChotaSetting(setting);
		HashMap<String, String> compareResult = checkSettingExists(chotaSetting, allSettings, setting);
		int settingStatus = Integer.parseInt(compareResult.get(SETTING_TYPE));
		String toneBoxID = compareResult.get(TONEBOX_ID);

		if(settingStatus == SETTING_NOT_EXISTS && toneBoxID != null) {
			// Do a addTbTone as selection is in loop
			// Only normal selections or TOD selections can be looped
			if(chotaSetting.loopType() != null && (setting.status() == 1 || setting.status() == 80))
				toneBoxID = addTbTone(setting, toneBoxID, settingStr);
			// In override case we have to change the ToneBox tones
			// Make the current song as the only song in the TB
			else
				toneBoxID = editToneBox(setting, toneBoxID, settingStr);
		}
		// do a addToneBox
		else if(settingStatus == SETTING_NEW)
			toneBoxID = addToneBox(setting, settingStr);

		if(toneBoxID != null) {
			if(settingStatus == SETTING_NEW)
				setTone(setting, toneBoxID, chotaSetting, settingStr);
			else
				updateLoopStatusToSuccess(setting, null, settingStr);
		}
		else {
			logger.info("RBT::no ToneBox identified");
			return FAILURE;
		}
		return SUCCESS;
	}
	
	/**
	 * This method is used to add the setting for a downloaded tone already present in a ToneBox
	 * @param setting
	 * @param toneBoxID
	 * @param chotaSetting
	 * @return success/failure status of the request
	 */
	private String setTone(SubscriberStatus setting, String toneBoxID, Setting chotaSetting,
			String settingStr) {
		logger.info("RBT::in for setting - " + settingStr);
		
		SetToneEvt setToneEvt = new SetToneEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, setting.subID());
		map.put(MODE, setting.selectedBy());
		setBaseParams(setToneEvt, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		setToneEvt.setCalledUserType("1");
		sb.append("&calledUserType=1");
		setToneEvt.setCalledUserID(setting.subID());
		sb.append("&calledUserID=" + setting.subID());
		setToneEvt.setToneBoxID(toneBoxID);
		sb.append("&toneBoxID=" + toneBoxID);
		setToneEvt.setResourceType(String.valueOf(chotaSetting.resourceType()));
		sb.append("&resourceType=" + chotaSetting.resourceType());
		int setType = getSetType(setting.callerID());
		setToneEvt.setSetType(String.valueOf(setType));
		sb.append("&setType=" + setType);
		if(setType != SET_TYPE_DEFAULT) {
			String callerID = setting.callerID();
			if(setType == SET_TYPE_GROUP) {
				/*int groupID = -1;
				try {
					groupID = Integer.parseInt(callerID.substring(1));
					Groups thisGrp = _dbManager.getGroup(groupID);
					callerID = thisGrp.groupPromoID();
				}
				catch(Exception e) {
					logger.info("RBT::invalid group number " + callerID);
				}*/
				callerID = callerID.substring(1);
			}
			setToneEvt.setCallerNumber(callerID);
			sb.append("&callerNumber=" + callerID);
		}
		if(chotaSetting.loopType() != null) {
			setToneEvt.setLoopType("1");//always the loop type is to be set as cyclic
			sb.append("&loopType=1");
		}
		setToneEvt.setTimeType(String.valueOf(chotaSetting.timeType()));
		sb.append("&timeType=" + chotaSetting.timeType());
		setToneEvt.setStartTime(chotaSetting.startTime());
		sb.append("&startTime=" + chotaSetting.startTime());
		setToneEvt.setEndTime(chotaSetting.endTime());
		sb.append("&endTime=" + chotaSetting.endTime());
		
		Date requestTime = new Date();
		SetToneResp response = null;
		String settingID = null;
		try {
			response = _userToneManage.setTone(setToneEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(response != null) {
			returnCode = response.getReturnCode();
			settingID = response.getSettingID();
		}
		writeSDR(new SDR("SET_TONE", setting.subID(), setting.prepaidYes(), requestTime,
				responseTime, sb.toString(), returnCode + "," + settingID));
		
		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return FAILURE;
		}
		
		logger.info("RBT::response for - " + settingStr + " is - "
				+ returnCode);
		
		if(returnCode.equals("000000") || returnCode.equals("000001"))
			updateLoopStatusToSuccess(setting, response.getSettingID(), settingStr);
		else {
			updateLoopStatusToError(setting);
			return FAILURE;
		}
		
		return SUCCESS;
	}
	
	/**
	 * returns the ToneBoxID freshly created @ Huawei. Returns null for error responses.
	 * @param setting
	 * @return ToneBoxID
	 */
	private String addToneBox(SubscriberStatus setting, String settingStr) {
		logger.info("RBT::in for setting " + settingStr);
		AddToneBoxEvt addToneBoxEvt = new AddToneBoxEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, setting.subID());
		map.put(MODE, setting.selectedBy());
		setBaseParams(addToneBoxEvt, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		addToneBoxEvt.setName(settingStr);
		sb.append("&name=" + settingStr);
		addToneBoxEvt.setToneCode(new String[] {setting.subscriberFile()});
		sb.append("&toneCode=" + setting.subscriberFile());
		
		AddToneBoxResp response = null;
		String toneboxID = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.addToneBox(addToneBoxEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(response != null) {
			returnCode = response.getReturnCode();
			toneboxID = response.getToneBoxID();
		}
		writeSDR(new SDR("ADD_TONEBOX", setting.subID(), setting.prepaidYes(), requestTime,
				responseTime, sb.toString(), returnCode + "," + toneboxID));
		
		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return null;
		}
		
		logger.info("RBT::response for - " + settingStr + " is - "
				+ returnCode);
		String toneBoxID = null;
		if(returnCode.equals("000000") || returnCode.equals("303031")) {
			toneBoxID = response.getToneBoxID();
			logger.info("RBT:: got toneBoxID as - " + toneBoxID);
		}
		if(toneBoxID == null && (returnCode.startsWith("2") || returnCode.startsWith("3"))) {
			logger.info("RBT::updating loopstatus to error");
			updateLoopStatusToError(setting);
		}
		return toneBoxID;
	}
	
	/**
	 * This method adds the current setting to the ToneBox
	 * @param setting
	 * @param toneBoxID
	 * @return same toneBoxID is returned if update is successful, null for any other issues
	 */
	private String addTbTone(SubscriberStatus setting, String toneBoxID, String settingStr) {
		logger.info("RBT::in for setting " + settingStr);
		AddTbToneEvt addTbToneEvt = new AddTbToneEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, setting.subID());
		map.put(MODE, setting.selectedBy());
		setBaseParams(addTbToneEvt, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		addTbToneEvt.setToneBoxID(toneBoxID);
		sb.append("&toneBoxID=" + toneBoxID);
		addTbToneEvt.setToneCode(new String[] {setting.subscriberFile()});
		sb.append("&toneCode=" + setting.subscriberFile());
		
		Response response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.addTbTone(addTbToneEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();
		writeSDR(new SDR("ADD_TB_TONE", setting.subID(), setting.prepaidYes(), requestTime,
				responseTime, sb.toString(), returnCode));
		
		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return null;
		}
		logger.info("RBT::response for - " + settingStr + " is - "
						+ returnCode);
		
		if(!(returnCode.equals("000000") || returnCode.equals("000001")
				|| returnCode.equals("302001")))
			toneBoxID = null;
		if(toneBoxID == null && (returnCode.startsWith("2") || returnCode.startsWith("3"))) {
			logger.info("RBT::updating loopstatus to error");
			updateLoopStatusToError(setting);
		}
		return toneBoxID;
	}
	
	/**
	 * This method makes this song as the only setting for the caller for this time frame. it also
	 * moves the selection into error for non-rety error
	 * @param setting
	 * @param toneBoxID
	 * @return same toneBoxID is returned if update is successful, null for any other issues
	 */
	private String editToneBox(SubscriberStatus setting, String toneBoxID, String settingStr) {
		logger.info("RBT::in for setting " + settingStr);
		EditToneBoxEvt editToneBoxEvt = new EditToneBoxEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, setting.subID());
		map.put(MODE, setting.selectedBy());
		setBaseParams(editToneBoxEvt, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		String portalType = getPortalType(setting.selectedBy());
		editToneBoxEvt.setPortalType(portalType);
		sb.append("portalType=" + portalType);
		editToneBoxEvt.setToneBoxID(toneBoxID);
		sb.append("&toneBoxID=" + toneBoxID);
		editToneBoxEvt.setToneCode(new String[] {setting.subscriberFile()});
		sb.append("&toneCode=" + setting.subscriberFile());
		
		Response response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.editToneBox(editToneBoxEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();
		writeSDR(new SDR("EDIT_TONEBOX", setting.subID(), setting.prepaidYes(), requestTime,
				responseTime, sb.toString(), returnCode));
		
		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return null;
		}
		logger.info("RBT::response for - " + settingStr + " is - "
						+ returnCode);
		
		if(!returnCode.equals("000000") && !returnCode.equals("000001"))
			toneBoxID = null;
		//all codes starting with 3 are erroneous and need to be looked manually 
		if(toneBoxID == null) {
			logger.info("RBT::updating loopstatus to error");
			updateLoopStatusToError(setting);
		}
		
		return toneBoxID;
	}
	
	private boolean updateLoopStatusToError(SubscriberStatus setting) {
		return _dbManager.updateLoopStatus(setting, LOOP_STATUS_ERROR, null);
	}
	
	private boolean updateLoopStatusToSuccess(SubscriberStatus setting, String settingID,
			String settingStr) {
		char newStatus = LOOP_STATUS_LOOP_FINAL;
		if(setting.loopStatus() == LOOP_STATUS_OVERRIDE
				|| setting.loopStatus() == LOOP_STATUS_OVERRIDE_INIT
				|| setting.loopStatus() == LOOP_STATUS_OVERRIDE_FINAL)
			newStatus = LOOP_STATUS_OVERRIDE_FINAL;
		String newSelInfo = null;
		if(setting != null) {
			if(settingID == null) {
				logger.info("RBT::null SID for setting - " + settingStr
						+ ", probably looped setting");
				newSelInfo = setting.selectionInfo();
			}
			else
				newSelInfo = setting.selectionInfo() + ":SID" + settingID + ";";
		}
		return _dbManager.updateLoopStatus(setting, newStatus, newSelInfo);
	}
	
	private QuerySettingResp querySetting(SubscriberStatus setting) {
		logger.info("RBT::Querying setting for " + setting.subID());
		QuerySettingEvt querySettingEvt = new QuerySettingEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MODE, setting.selectedBy());
		setBaseParams(querySettingEvt, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		querySettingEvt.setCalledUserID(setting.subID());
		sb.append("&calledUserID=" + setting.subID());
		int setType = getSetType(setting.callerID());
		querySettingEvt.setSetType(String.valueOf(setType));
		sb.append("&setType=" + setType);

		QuerySettingResp response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.querySetting(querySettingEvt);
		}
		catch (RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();
		writeSDR(new SDR("QUERY_SETTING", setting.subID(), setting.prepaidYes(), requestTime,
				responseTime, sb.toString(), returnCode));
		
		if(response == null || returnCode == null)
			logger.info("RBT::null response");
		
		return response;
	}
	
	//this method shud also return the ToneBox id
	private int compareSetting(SettingInfo settingInfo, Setting chotaSetting,
			HashMap<String, ToneInfo[]> toneBoxes, SubscriberStatus setting) {
		boolean retVal = true;
		logger.info("RBT::comparing with huawei setting, caller-"
				+ settingInfo.getCallerNumber() + ", resourceType-" + settingInfo.getResourceType()
				+ ", timeType-" + settingInfo.getTimeType() + ", startTime-"
				+ settingInfo.getStartTime() + ", endTime-" + settingInfo.getEndTime());
		//comparing caller
		boolean callerComp = (settingInfo.getCallerNumber() == null && chotaSetting.caller() == null)
				|| (settingInfo.getCallerNumber() != null && settingInfo.getCallerNumber().equals(
						chotaSetting.caller()));
		retVal = retVal && callerComp;
		if (!retVal)
			return SETTING_NEW;
		logger.info("RBT::caller same");
		//resource type comparision
		boolean resourceTypeComp = settingInfo.getResourceType() != null
				&& settingInfo.getResourceType().equalsIgnoreCase(chotaSetting.resourceType());
		retVal = retVal && resourceTypeComp;
		if (!retVal)
			return SETTING_NEW;
		logger.info("RBT::resource type too same");
		//comparing time type
		boolean timeTypeComp = settingInfo.getTimeType().equals(chotaSetting.timeType());
		retVal = retVal && timeTypeComp;
		if (!retVal)
			return SETTING_NEW;
		logger.info("RBT::time type too same");
		String endTime=chotaSetting.endTime();
		String startTime=chotaSetting.startTime();
		if(chotaSetting.timeType().equalsIgnoreCase("2"))
		{
			endTime="2003-01-01 "+endTime;
			startTime="2003-01-01 "+startTime;
		}
		
		//comparing start and end times
		boolean startTimeComp = (settingInfo.getStartTime() == null && chotaSetting.startTime() == null)
				|| (settingInfo.getStartTime() != null && settingInfo.getStartTime().equals(startTime));
		boolean endTimeComp = (settingInfo.getEndTime() == null && chotaSetting.endTime() == null)
				|| (settingInfo.getEndTime() != null && settingInfo.getEndTime().equals(endTime));
		retVal = retVal && startTimeComp && endTimeComp;
		if (!retVal)
			return SETTING_NEW;

		logger.info("RBT::start and end times too same");
		/*
		 * If all the above conditions match setting for the caller for this time frame exists
		 * will query the ToneBox if the clip is already set or not.
		 */
		if(!toneBoxes.containsKey(settingInfo.getToneBoxID())) {
			QueryTbToneResp queryTbToneResponse = queryTbTone(settingInfo, setting);
			if(queryTbToneResponse == null)
				return SETTING_ERROR;
			if(queryTbToneResponse.getToneInfos() != null)
				toneBoxes.put(settingInfo.getToneBoxID(), queryTbToneResponse.getToneInfos());
		}
		
		if(toneBoxes.containsKey(settingInfo.getToneBoxID())) {
			ToneInfo[] tones = toneBoxes.get(settingInfo.getToneBoxID());
			for(int i = 0; tones != null && i < tones.length; i++) {
				if(chotaSetting.resourceCode().equals(tones[i].getToneCode()))
					return SETTING_EXISTS;
			}
		}
		return SETTING_NOT_EXISTS;
	}
	
	private QueryTbToneResp queryTbTone(SettingInfo settingInfo, SubscriberStatus setting) {
		QueryTbToneEvt queryTbToneEvt = new QueryTbToneEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MODE, setting.selectedBy());
		setBaseParams(queryTbToneEvt, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		queryTbToneEvt.setToneBoxID(settingInfo.getToneBoxID());
		sb.append("&toneBoxID=" + settingInfo.getToneBoxID());
		
		QueryTbToneResp response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.queryTbTone(queryTbToneEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();
		writeSDR(new SDR("QUERY_TB_TONE", setting.subID(), setting.prepaidYes(), requestTime,
				responseTime, sb.toString(), returnCode));
		
		if(response == null || returnCode == null)
			logger.info("RBT::Got null reponse for toneBoxID - "
					+ settingInfo.getToneBoxID());
		return response;
	}
	
	/**
	 * 
	 * @param setting
	 * @return subscriber-caller-fromTime:toTime|interval
	 */
	private String getSettingString(SubscriberStatus setting) {
		try {
			String caller = (setting.callerID() == null) ? "All" : setting.callerID();
			StringBuffer sb = new StringBuffer(setting.subID());
			sb.append("-");
			sb.append(caller);
			sb.append("-");
			sb.append(setting.fromTime());
			sb.append(":");
			sb.append(setting.toTime());
			sb.append("-");
			sb.append(setting.selInterval());
			return sb.toString();
		}
		catch(Exception e) {
			logger.info("RBT::" + e.getMessage());
		}
		return null;
	}

	/**
	 * Updates the overlay status of a subscriber to Huawei
	 * @throws RBTException
	 */
	public String updateSubscriber(Subscriber subscriber) throws RBTException {
		logger.info("RBT::sub " + subscriber.subID());
		if(!subscriber.subYes().equals(STATE_ACTIVATED)) {
			logger.info("RBT::" + subscriber.subID() + " is in "
					+ subscriber.subYes() + " state. Cannot be sent to Huawei");
			_dbManager.updateSubUpdatedAtPlayer(subscriber.subID());
			return "not-updated-at-huawei";
		}

		HashMap<String, String> map = new HashMap<String, String>();
		EditUserEvt editUser = new EditUserEvt();
		map.put(PHONE_NUMBER, subscriber.subID());
		map.put(MODE, subscriber.activatedBy());
		setBaseParams(editUser, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		
		HashMap<String, String> extraInfo = _dbManager.getExtraInfoMap(subscriber);
		/*
		 * Huawei Code Description
		 * 	0 - Don't play BG Tone, 1 - Play BG Tone
		 * OnMobile Code Description
		 * 	1 - Don't play BG Tone, 0 - Play BG Tone
		 */
		String overlayFlag = "1";
		if(extraInfo != null && extraInfo.containsKey(EXTRA_INFO_INTRO_OVERLAY_FLAG)) {
			String dbOverlayFlag = extraInfo.get(EXTRA_INFO_INTRO_OVERLAY_FLAG);
			if(dbOverlayFlag != null && dbOverlayFlag.equals("1"))
				overlayFlag = "0";
		}
		editUser.setPhoneNumber(subscriber.subID());
		sb.append("&phoneNumber=" + subscriber.subID());
		editUser.setBgTone(overlayFlag);
		sb.append("&bgTone=" + overlayFlag);
		
		Response userEditResp = null;
		Date requestTime = new Date();
		try {
			userEditResp = _userManage.edit(editUser);
		}
		catch (RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(userEditResp != null)
			returnCode = userEditResp.getReturnCode();
		writeSDR(new SDR("UPDATE_SUBSCRIBER", subscriber.subID(), subscriber.prepaidYes(), requestTime,
				responseTime, sb.toString(), returnCode));
		
		logger.info("RBT:: returnCode for sub " + subscriber.subID() + " - "
				+ returnCode);
		
		if(userEditResp == null) {
			logger.info("RBT::got null response");
			return FAILURE;
		}
		
		if(returnCode != null)
			_dbManager.updateSubUpdatedAtPlayer(subscriber.subID());
		else
			return FAILURE;
		
		return SUCCESS;
	}
	
	//Not needed in Voda Romania
	public String deleteSubscriber(Subscriber subscriber) throws RBTException {
		return null;
	}

	//Not needed in Voda Romania
	public String deleteDownload(SubscriberDownloads download) throws RBTException {
		return null;
	}
	
	/**
	 * This method updated the deletion status of a selection to Huawei. to delete a setting in
	 * Huawei we have to invoke their DelTbTone API and not DelSetting
	 * 
	 * @param setting
	 * @return success/failure
	 */
	public String deleteSetting(SubscriberStatus setting) throws RBTException {
		String settingStr = getSettingString(setting);
		logger.info("RBT::in for setting - " + settingStr);

		QuerySettingResp querySettingResult = querySetting(setting);
		if (querySettingResult == null) {
			logger.info("RBT::null query setting result returning failure");
			return FAILURE;
		}

		SettingInfo[] allSettings = querySettingResult.getSettingInfos();
		Setting chotaSetting = getChotaSetting(setting);
		HashMap<String, String> compareResult = checkSettingExists(chotaSetting, allSettings,
				setting);
		String toneBoxID = compareResult.get(TONEBOX_ID);
		if(toneBoxID == null) {
			logger.info("RBT::got null toneBoxID for - " + settingStr);
			updateLoopStatusToExpired(setting);
			return SUCCESS;
		}

		DelTbToneEvt delTbToneEvt = new DelTbToneEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, setting.subID());
		map.put(MODE, setting.deSelectedBy());
		setBaseParams(delTbToneEvt, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		delTbToneEvt.setToneBoxID(toneBoxID);
		sb.append("&toneBoxID=" + toneBoxID);
		delTbToneEvt.setToneCode(new String[] {setting.subscriberFile()});
		sb.append("&toneCode=" + setting.subscriberFile());
		
		Response response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.delTbTone(delTbToneEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();
		writeSDR(new SDR("DEL_TB_TONE", setting.subID(), setting.prepaidYes(), requestTime,
				responseTime, sb.toString(), returnCode));
		
		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return FAILURE;
		}
		
		logger.info("RBT::response for - " + settingStr + " is - "
						+ returnCode);
		if (returnCode != null
				&& (returnCode.equals("000000") || returnCode.equals("302003")
						|| returnCode.equals("303032") || returnCode.equals("302017")))
			updateLoopStatusToExpired(setting);

		return SUCCESS;
	}
	/*public String deleteSetting(SubscriberStatus setting) throws RBTException {
		String method = "deleteSetting";
		String settingStr = getSettingString(setting);
		logger.info("RBT::in for setting - " + settingStr);
		
		String settingID = null;
		String selInfo = setting.selectionInfo();
		try {
			int sidIdx = selInfo.indexOf(":SID");
			if(sidIdx != -1) {
				settingID = selInfo.substring(sidIdx + 4, selInfo.indexOf(';', sidIdx));
			}
		}
		catch(Exception e) {

		}
		
		if(settingID == null) {
			logger.info("RBT::no settingID for sel - " + settingStr);
			updateLoopStatusToError(setting);
			return null;
		}
		
		DelSettingEvt delSettingEvt = new DelSettingEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, setting.subID());
		map.put(MODE, setting.deSelectedBy());
		setBaseParams(delSettingEvt, map);
		StringBuffer sb = new StringBuffer(map.get(URL));
		delSettingEvt.setCalledUserID(setting.subID());
		sb.append("&calledUserID=" + setting.subID());
		delSettingEvt.setSettingID(settingID);
		sb.append("&settingID=" + settingID);
		
		Response response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.delSetting(delSettingEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();
		writeSDR(new SDR("DEL_SETTING", setting.subID(), setting.prepaidYes(), requestTime,
				responseTime, sb.toString(), returnCode));
		
		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return FAILURE;
		}
		
		logger.info("RBT::response for - " + settingStr + " is - "
						+ returnCode);
		if(returnCode != null)
			updateLoopStatusToExpired(setting);
		
		return SUCCESS;
	}*/
	
	private boolean updateLoopStatusToExpired(SubscriberStatus setting) {
		return _dbManager.updateLoopStatus(setting, LOOP_STATUS_EXPIRED, null);
	}
	
	private String getSubscriberEP() throws RBTException {
		StringBuffer sb = new StringBuffer();
		sb.append(getBaseURL());
		sb.append(getSubscriberURL());
		return sb.toString();
	}
	
	private String getSettingEP() throws RBTException {
		StringBuffer sb = new StringBuffer();
		sb.append(getBaseURL());
		sb.append(getSettingURL());
		return sb.toString();
	}
	
	private String getBaseURL() throws RBTException {
		String httpConfig = getParamAsString("DAEMON",HTTP_URL, null);
		
		if(_httpParams == null && httpConfig != null) {
			logger.info("RBT::inting with URL config - " + httpConfig);
			_httpParams = Tools.getHttpParamsForURL(httpConfig, null);
			return _httpParams.getUrl();
		}
		else if (_httpParams != null)
			return _httpParams.getUrl();
		else
			throw new RBTException("no Huawei base url configured");
	}
	
	private String getSubscriberURL() {
		return getParamAsString("DAEMON", SUBSCRIBER_URL, "");
	}
	
	private String getSettingURL() {
		return getParamAsString("DAEMON", SETTING_URL, "");
	}
	
	private void setBaseParams(Object obj, HashMap<String, String> map) {
		if(obj instanceof BaseQueryEvt)
			setBaseQueryEvtParams((BaseQueryEvt)obj, map);
		else if(obj instanceof BaseUpdateEvt)
			setBaseUpdateEvtParams((BaseUpdateEvt)obj, map);
		else if (obj instanceof BaseEvt)
			setBaseEvtParams((BaseEvt)obj, map);
	}
	
	private void setBaseEvtParams(BaseEvt baseEvt, HashMap<String, String> map) {
		StringBuffer sb = new StringBuffer();
		String portalAccount = getParamAsString("DAEMON",PORTAL_ACCOUNT, null);
		String portalPwd = getParamAsString("DAEMON", PORTAL_PWD, null); 
		baseEvt.setPortalAccount(portalAccount);
		sb.append("portalAccount=" + portalAccount);
		baseEvt.setPortalPwd(portalPwd);
		sb.append("&portalPwd=" + portalPwd);
		if(map != null && map.containsKey(MODE)) {
			String mode = map.get(MODE);
			String portalType = getPortalType(mode);
			baseEvt.setPortalType(portalType);
			sb.append("&portalType=" + portalType);	
		}
		map.put(URL, sb.toString());
	}
	
	private void setBaseQueryEvtParams(BaseQueryEvt baseQueryEvt, HashMap<String, String> map) {
		setBaseEvtParams(baseQueryEvt, map);
		if(map != null) {
			StringBuffer sb = new StringBuffer(map.get(URL));
			if(map.containsKey(QUERY_TYPE)) {
				baseQueryEvt.setQueryType(map.get(QUERY_TYPE));
				sb.append("&queryType=" + map.get(QUERY_TYPE));
			}
			if(map.containsKey(START_RECORD_NUMBER)) {
				baseQueryEvt.setStartRecordNum(map.get(START_RECORD_NUMBER));
				sb.append("&startRecordNum=" + map.get(START_RECORD_NUMBER));
			}
			if(map.containsKey(END_RECORD_NUMBER)) {
				baseQueryEvt.setEndRecordNum(map.get(END_RECORD_NUMBER));
				sb.append("&endRecordNum=" + map.get(END_RECORD_NUMBER));
			}
			map.put(URL, sb.toString());
		}
	}
	
	private void setBaseUpdateEvtParams(BaseUpdateEvt baseUpdateEvt, HashMap<String, String> map) {
		setBaseEvtParams(baseUpdateEvt, map);
		if(map != null) {
			StringBuffer sb = new StringBuffer(map.get(URL));
			baseUpdateEvt.setRole("1");
			sb.append("&role=1");
			baseUpdateEvt.setRoleCode(map.get(PHONE_NUMBER));
			sb.append("&roleCode=" + map.get(PHONE_NUMBER));
			map.put(URL, sb.toString());
		}
	}
	
	private String getPortalType(String mode) {
		if(mode != null &&_portalTypeMap.containsKey(mode))
			return _portalTypeMap.get(mode);
		logger.info("RBT::PortalType not defined for mode " + mode + ", returning 0");
		return String.valueOf(0);
	}
	
	private void initUserManage() throws RBTException {
		if(_userManage == null) {
			String endPoint = getSubscriberEP();
			UserManageServiceLocator userManageServiceLocator = new UserManageServiceLocator();
			try {
				_userManage = userManageServiceLocator.getUserManage(new URL(endPoint));
			}
			catch(MalformedURLException e) {
				throw new RBTException("Cannot init UserManage. MalformedURLException:"
						+ e.getMessage());
			}
			catch(ServiceException e) {
				throw new RBTException("Cannot init UserManage. ServiceException:" + e.getMessage());
			}
		}
		if(_userManage == null)
			throw new RBTException("Cannot init UserManage");
	}
	
	private void initUserToneManage() throws RBTException {
		if(_userToneManage == null) {
			String endPoint = getSettingEP();
			UserToneManageServiceLocator userManageServiceLocator = new UserToneManageServiceLocator();
			try {
				_userToneManage = userManageServiceLocator.getUserToneManage(new URL(endPoint));
			}
			catch(MalformedURLException e) {
				throw new RBTException("Cannot init UserToneManage.MalformedURLException:"
						+ e.getMessage());
			}
			catch(ServiceException e) {
				throw new RBTException("Cannot init UserToneManage.ServiceException:"
						+ e.getMessage());
			}
		}
		if(_userToneManage == null)
			throw new RBTException("Cannot init UserToneManage");
	}
	
	private String getLoopType(char loopStatus) {
		if (loopStatus == LOOP_STATUS_LOOP_INIT || loopStatus == LOOP_STATUS_LOOP
				|| loopStatus == LOOP_STATUS_LOOP_FINAL)
			return "1";
		return null;
	}
	
	private String getTimeType(int status) {
		switch (status) {
			case 95://future date
				return "5";//time segment of a year
			case 80://TOD
				return "2";//time segment of a day
			case 75://DOW
				return "3";//time segment of a week
			default:
				return "1";
		}
	}
	
	private int getSetType(String callerID) {
		if(callerID == null || callerID.equals(""))
			return SET_TYPE_DEFAULT;
		if(callerID.length() < 7 || callerID.startsWith("G"))
			return SET_TYPE_GROUP;
		else if(callerID.length() >= 7 || callerID.length() <= 15)
			return SET_TYPE_PERSONAL;
		return SET_TYPE_DEFAULT;
	}
	
	private String getHuaweiStartDate(SubscriberStatus setting) {
		String startTime = "2003-01-01 00:00:00";
		try {
			int timeType = Integer.parseInt(getTimeType(setting.status()));

			switch (timeType) {
				case 2:
					//startTime = "2003-01-01 " + formatDigit(setting.fromTime(), 2) + ":00:00";
					startTime = formatDigit(setting.fromTime(), 4) + ":00";
					break;
				case 3:
					startTime = "2003-01-0" + getHuaweiWeek(setting.selInterval()) + " 00:00:00";
					break;
				case 5:
					startTime = "2003-" + getHuaweiYearSetting(setting.selInterval()) + " 00:00:00";
					break;
			}
		}
		catch (Exception e) {
			logger.error("", e);
			startTime = "2003-01-01 00:00:00";
		}

		logger.info("RBT::returning " + startTime + " for selection "
				+ getSettingString(setting));
		return startTime;
	}
	
	private String getHuaweiEndDate(SubscriberStatus setting) {
		String endTime = "2003-01-01 00:00:00";
		try {
			int timeType = Integer.parseInt(getTimeType(setting.status()));
			switch (timeType) {
			case 2:
				// endTime = "2003-01-01 " + formatDigit(setting.toTime(), 2) + ":00:00";
				endTime = formatDigit(setting.toTime(), 4) + ":00";
				break;
			case 3:
				endTime = "2003-01-0" + getHuaweiWeek(setting.selInterval()) + " 23:59:59";
				break;
			case 5:
				endTime = "2003-" + getHuaweiYearSetting(setting.selInterval()) + " 23:59:59";
				break;
			}
		}
		catch (Exception e) {
			logger.error("", e);
			endTime = "2003-01-01 00:00:00";
		}
		logger.info("RBT::returning " + endTime + " for selection "
				+ getSettingString(setting));
		return endTime;
	}
	
	private String getHuaweiYearSetting(String selInterval) {
		if(!selInterval.startsWith("Y")) {
			logger.info("RBT::invalid selInterval " + selInterval);
			return "01-01";
		}
		return selInterval.substring(3, 5) + "-" + selInterval.substring(1, 3);
	}
	
	private int getHuaweiWeek(String selInterval) {
		if(!selInterval.startsWith("W")) {
			logger.info("RBT::invalid selInterval " + selInterval);
			return 1;
		}
		/*
		 * Huawei week codes
		 * 1 - 7 Monday to Sunday
		 * OnMobile week codes
		 * 1 - 7 Sunday to Saturday
		 */
		int omWeek = Character.getNumericValue(selInterval.charAt(1));
		/*int huaweiWeek = omWeek - 1;
		return (huaweiWeek > 0) ? 7 : huaweiWeek;*/
		int huaweiWeek = omWeek - 1;
		return (huaweiWeek <= 0) ? 7 : huaweiWeek;
	}
	
	private String formatDigit(int digit, int finalLength) {
		String digitStr = String.valueOf(digit);
		if(digitStr.length() == finalLength)
		{
			return digitStr.substring(0,2) + ":" + digitStr.substring(2);
		}
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < (finalLength - digitStr.length()); i++)
			sb.append("0");
		sb.append(digit);
		return sb.toString().substring(0,2) + ":" + sb.toString().substring(2);
	}
	
	private void writeSDR(SDR sdr) {
		String response = sdr.response();
		if(response == null)
			response = "null";
		WriteSDR.addToAccounting(getParamAsString("DAEMON", "PLAYER_SDR_WORKING_DIR", null), getParamAsInt("DAEMON", "ROTATION_SIZE", 8000), sdr.eventType(), sdr.subID(), sdr
				.subType(), "-", "-", sdr.requestTime(), sdr.duration(), "-", sdr.httpUrl(), response);
	}
	
	class SDR  {
		private String _eventType;
		private String _subID;
		private String _subType;
		private String _requestTime;
		private String _duration;
		private String _httpUrl;
		private String _response;
		
		SimpleDateFormat _formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		
		SDR(String eventType, String subID, boolean prepYes, Date requestTime, Date responseTime,
				String httpUrl, String response) {
			_eventType = eventType;
			_subID = subID;
			_subType = prepYes ? "PRE_PAID" : "POST_PAID";
			_requestTime = _formatter.format(requestTime);
			_duration = String.valueOf(responseTime.getTime() - requestTime.getTime());
			_httpUrl = httpUrl;
			_response = response;
		}
		
		String eventType() {
			return _eventType;
		}
		
		String subID() {
			return _subID;
		}
		
		String subType() {
			return _subType;
		}
		
		String requestTime() {
			return _requestTime;
		}
		
		String duration() {
			return _duration;
		}
		
		String httpUrl() {
			return _httpUrl;
		}
		
		String response() {
			return _response;
		}
	}
	
	class Setting {
		private String _caller;
		private String _resourceType;
		private String _timeType;
		private String _startTime;
		private String _endTime;
		private String _resourceCode;
		private String _loopType;

		Setting(String caller, String resourceType, String timeType, String startTime, String endTime,
				String resourceCode, String loopType) {
			_caller = caller;
			_resourceType = resourceType;
			_timeType = timeType;
			_startTime = startTime;
			_endTime = endTime;
			_resourceCode = resourceCode;
			_loopType = loopType;
		}

		String caller() {
			return _caller;
		}

		String resourceType() {
			return _resourceType;
		}

		String timeType() {
			return _timeType;
		}

		String startTime() {
			return _startTime;
		}

		String endTime() {
			return _endTime;
		}

		String resourceCode() {
			return _resourceCode;
		}

		String loopType() {
			return _loopType;
		}
	}

	public String addGroup(Groups group) throws RBTException {
		int groupID = group.groupID();
		logger.info("RBT::in for group - " + groupID);

		String subID = group.subID();
		Subscriber subscriber = _dbManager.getSubscriber(subID);
		if(!subscriber.subYes().equalsIgnoreCase(STATE_ACTIVATED)) {
			logger.info("RBT::" + subID + " is in " + subscriber.subYes()
					+ ", not adding group " + groupID);
			return ERROR;
		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, subID);
		AddGroupEvt addGroupEvt = new AddGroupEvt();
		setBaseParams(addGroupEvt, map);
		StringBuffer url = new StringBuffer(map.get(URL));
		addGroupEvt.setPhoneNumber(subID);
		url.append("&phoneNumber=" + subID);
		String groupCode = group.groupID()+"";
		addGroupEvt.setGroupCode(groupCode);
		url.append("&groupCode=" + groupCode);

		AddGroupResp response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.addGroup(addGroupEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();

		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();
		writeSDR(new SDR("ADD_GROUP", subID, subscriber.prepaidYes(), requestTime, responseTime,
				url.toString(), returnCode));

		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return FAILURE;
		}
		if(returnCode.equals("000000") || returnCode.equals("303001")) {
			String groupPromoID = response.getGroupID();
			if(groupPromoID == null) {
				logger.info("RBT::Got null Huawei group id for " + groupID);
				return ERROR;
			}
			_dbManager.updateGroupStatus(groupID, STATE_ACTIVATED, groupPromoID);
			_dbManager.updateGroupMembersStatusForGroup(groupID, STATE_TO_BE_ACTIVATED,
					STATE_BASE_ACTIVATION_PENDING);
			return SUCCESS;
		}
		else if(returnCode.equals("303003") || returnCode.equals("301001")) {
			_dbManager.updateGroupStatus(groupID, STATE_DEACTIVATED);
			return ERROR;
		}
		return FAILURE;
	}

	public String addGroupMember(GroupMembers groupMember) throws RBTException {
		logger.info("RBT::in for group - " + groupMember.groupID()
				+ ", caller - " + groupMember.callerID());

		int groupID = groupMember.groupID();
		String callerNumber = groupMember.callerID();
		Groups group = _dbManager.getGroup(groupID);
		if(group == null) {
			logger.info("RBT::Invalid group id - " + groupID);
			_dbManager.updateGroupMemberStatus(groupID, callerNumber, STATE_ACTIVATION_ERROR);
			return ERROR;
		}
		String subID = group.subID();
		Subscriber sub = _dbManager.getSubscriber(subID);
		if(!sub.subYes().equalsIgnoreCase(STATE_ACTIVATED)) {
			logger.info("RBT::" + subID + " is in " + sub.subYes()
					+ ", not adding member " + callerNumber + " to group " + groupID);
			return ERROR;
		}

		String groupPromoID = group.groupPromoID();
		String callerName = groupMember.callerName();

		AddGroupMemberEvt addGroupMemberEvt = new AddGroupMemberEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, subID);
		setBaseParams(addGroupMemberEvt, map);
		StringBuffer url = new StringBuffer(map.get(URL));
		addGroupMemberEvt.setPhoneNumber(subID);
		url.append("&phoneNumber=" + subID);
		addGroupMemberEvt.setGroupID(groupPromoID);
		url.append("&groupID=" + groupPromoID);
		addGroupMemberEvt.setMemberNumber(callerNumber);
		url.append("&memberNumber=" + callerNumber);
		if(callerName != null)
			url.append("&memberName=" + callerName);

		Response response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.addGroupMember(addGroupMemberEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();

		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();

		writeSDR(new SDR("ADD_GROUP_MEMBER", subID, sub.prepaidYes(), requestTime, responseTime,
				url.toString(), returnCode));

		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return FAILURE;
		}
		if(returnCode.equals("000000") || returnCode.equals("303011")) {
			_dbManager.updateGroupMemberStatus(groupID, callerNumber, STATE_ACTIVATED);
			return SUCCESS;
		}
		return FAILURE;
	}

	public String deleteGroup(Groups group) throws RBTException {
		logger.info("RBT::in for group " + group.groupID());
		
		String subID = group.subID();
		int groupID = group.groupID();
		Subscriber sub = _dbManager.getSubscriber(subID);
		if(!sub.subYes().equals(STATE_ACTIVATED)) {
			logger.info("RBT::" + subID + " is in " + sub.subYes()
					+ ", not adding group " + groupID);
			return ERROR;
		}
		
		DelGroupEvt delGroupEvt = new DelGroupEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, subID);
		setBaseParams(delGroupEvt, map);
		StringBuffer url = new StringBuffer(map.get(URL));
		delGroupEvt.setPhoneNumber(subID);
		url.append("&phoneNumber=" + subID);
		delGroupEvt.setGroupID(group.groupPromoID());
		url.append("&groupID=" + group.groupPromoID());
		
		Response response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.delGroup(delGroupEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();
		
		writeSDR(new SDR("DEL_GROUP", subID, sub.prepaidYes(), requestTime, responseTime,
				url.toString(), returnCode));
		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return FAILURE;
		}
		if(returnCode.equals("000000") || returnCode.equals("303002")) {
			clearGroupInDB(group.subID(), groupID);
			return SUCCESS;
		}
		return FAILURE;
	}
	
	private void clearGroupInDB(String subscriberID, int groupID) {
		_dbManager.updateGroupStatus(groupID, STATE_DEACTIVATED);
		_dbManager.deactivateSubscriberRecords(subscriberID, "G" + groupID);
	}

	public String deleteGroupMember(GroupMembers groupMember) throws RBTException {
		logger.info("RBT::in for group - " + groupMember.groupID()
				+ ", caller - " + groupMember.callerID());
		
		int groupID = groupMember.groupID();
		String callerNumber = groupMember.callerID();
		Groups group = _dbManager.getGroup(groupID);
		if(group == null) {
			logger.info("RBT::Invalid group id - " + groupID);
			_dbManager.updateGroupMemberStatus(groupID, callerNumber, STATE_DEACTIVATION_ERROR);
			return ERROR;
		}
		String subID = group.subID();
		Subscriber sub = _dbManager.getSubscriber(subID);
		if(!sub.subYes().equalsIgnoreCase(STATE_ACTIVATED)) {
			logger.info("RBT::" + subID + " is in " + sub.subYes()
					+ ", not adding member " + callerNumber + " to group " + groupID);
			return ERROR;
		}
		
		String groupPromoID = group.groupPromoID();
		
		DelGroupMemberEvt delGroupMemberEvt = new DelGroupMemberEvt();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(PHONE_NUMBER, subID);
		setBaseParams(delGroupMemberEvt, map);
		StringBuffer url = new StringBuffer(map.get(URL));
		delGroupMemberEvt.setPhoneNumber(subID);
		url.append("&phoneNumber=" + subID);
		delGroupMemberEvt.setGroupID(groupPromoID+"");
		url.append("&groupID=" + groupPromoID);
		delGroupMemberEvt.setMemberNumber(new String[] {callerNumber});
		url.append("&memberNumber=" + callerNumber);
		
		Response response = null;
		Date requestTime = new Date();
		try {
			response = _userToneManage.delGroupMember(delGroupMemberEvt);
		}
		catch(RemoteException e) {
			logger.error("", e);
		}
		Date responseTime = new Date();
		
		String returnCode = null;
		if(response != null)
			returnCode = response.getReturnCode();
		
		writeSDR(new SDR("DEL_GROUP_MEMBER", subID, sub.prepaidYes(), requestTime, responseTime,
				url.toString(), returnCode));
		
		if(response == null || returnCode == null) {
			logger.info("RBT::null response");
			return FAILURE;
		}
		if(returnCode.equals("000000")) {
			_dbManager.updateGroupMemberStatus(groupID, callerNumber, STATE_DEACTIVATED);
			return SUCCESS;
		}
		if(returnCode.equals("301001") || returnCode.equals("303002")) {
			_dbManager.updateGroupMemberStatus(groupID, callerNumber, STATE_DEACTIVATION_ERROR);
			return ERROR;
		}
		return FAILURE;
	}

	//not needed in Voda-Romania
	public String editGroup(Groups group) throws RBTException {
		return null;
	}

	//not needed in Voda-Romania
	public String editGroupMember(GroupMembers groupMember) throws RBTException {
		return null;
	}
	
	public String getParamAsString(String type, String param, String defualtVal)
	{
		try{
			return paramCacheManager.getParameter(type, param, defualtVal).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defualtVal;
		}
	}
	public boolean getParamAsBoolean(String type, String param, String defaultVal)
	{
		try{
			return paramCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
	public int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = paramCacheManager.getParameter(type, param, defaultVal+"").getValue();
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal;
		}
	}


}