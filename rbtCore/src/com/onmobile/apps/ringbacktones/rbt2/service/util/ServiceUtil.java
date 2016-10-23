package com.onmobile.apps.ringbacktones.rbt2.service.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.CallingParty;
import com.livewiremobile.store.storefront.dto.rbt.CallingParty.CallingPartyType;
import com.livewiremobile.store.storefront.dto.rbt.DateRange;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleInfo;
import com.livewiremobile.store.storefront.dto.rbt.Schedule;
import com.livewiremobile.store.storefront.dto.rbt.Schedule.ScheduleType;
import com.livewiremobile.store.storefront.dto.rbt.TimeRange;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.common.SelectionStatus;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPContentMap;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.factory.BuildAssetFactory;
import com.onmobile.apps.ringbacktones.v2.factory.BuildCallingParty;
import com.onmobile.apps.ringbacktones.v2.factory.BuildPlayRule;
import com.onmobile.apps.ringbacktones.v2.factory.BuildPlayRuleInfo;
import com.onmobile.apps.ringbacktones.v2.factory.DateRangeBuilder;
import com.onmobile.apps.ringbacktones.v2.factory.ScheduleBuilder;
import com.onmobile.apps.ringbacktones.v2.factory.TimeRangeBuilder;
import com.onmobile.apps.ringbacktones.v2.service.AssetTypeAdapter;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class ServiceUtil {
	
	private static Logger logger = Logger.getLogger(ServiceUtil.class);
	
	public static void throwCustomUserException(ResponseErrorCodeMapping errorCodeMapping,String response, String responseMsg) throws UserException{
		String code = errorCodeMapping.getErrorCode(response.toLowerCase()).getCode();
		int statusCode = errorCodeMapping.getErrorCode(response.toLowerCase()).getStatusCode();
		UserException userException = new UserException();
		userException.setCode(code);
		userException.setStatusCode(statusCode);
		userException.setResponse(responseMsg != null ? (responseMsg+response.toLowerCase()) : response.toLowerCase());
		throw userException;	
	}
	
	public static <T> List<T> paginatedSubList(List<T> fullList, int pagesize, int offset){
		
		
		//offset == 0,, pagezie == 0
		if(fullList == null || fullList.size() == 0) {
			return new ArrayList<T>(0);
		}
		
		int listSize = fullList.size();
		
		if(offset <= -1) {
			offset = 0;
		}
		
		if(pagesize <= 0) {
			pagesize = listSize;
		}
		
		if(offset >= listSize) {
			return new ArrayList<T>(0);
		}
		
		if((offset * pagesize) > listSize){
			return new ArrayList<T>(0);
		}
		
		List<T> subList = new ArrayList<T>(0);
		int start = (pagesize*offset);
		int end = start+pagesize;
		if(end > fullList.size()){
			end = fullList.size();
		}
		
		if(!(start >= fullList.size() || end > fullList.size())){
			subList = fullList.subList(start, end);
		}
		
		return subList;
		
	}
	

	public static List<PlayRule> getPlayRules(List<Setting> settingList) {
		List<PlayRule> playRules = new ArrayList<PlayRule>();

		for (Setting setting : settingList) {
			PlayRule playRule = null;
			if (setting.getCallerID() != null&&!setting.getCallerID().equalsIgnoreCase("all")) {
				if (setting.getCallerID().startsWith("G")) {
					playRule = getPlayRule(setting, CallingPartyType.GROUP);
				} else {
					playRule = getPlayRule(setting, CallingPartyType.CALLER);					
				}
			} else {
				playRule = getPlayRule(setting, CallingPartyType.DEFAULT);
			}
			playRules.add(playRule);
		}
		return playRules;
	}
	
	private static PlayRule getPlayRule (Setting setting, CallingPartyType type) {
		
		Asset asset = getAsset(setting);
	
		Schedule schedule = getSchedule(setting);
		BuildCallingParty callingPartyBuilder = new BuildCallingParty();
		CallingParty callingParty = (CallingParty) callingPartyBuilder.getCaller(type);
		if (callingParty != null) {
			if (type.toString().equalsIgnoreCase(CallingPartyType.CALLER.toString())){
				if(setting.getCallerID().equalsIgnoreCase("private")) {
					callingParty.setId(-1);
				}
				else{
					callingParty.setId(Long.parseLong(setting.getCallerID()));
				}
			}				
			else if (type.toString().equalsIgnoreCase(CallingPartyType.GROUP.toString()))
				callingParty.setId(Long.parseLong(setting.getCallerID().substring(1)));
			
			callingParty.setType(type);
		}
		
		String selectionStatus = setting.getSelectionStatus();
		int playCount = -1;
		Map<String, String> extraInfo = setting.getSelectionInfoMap();
		if (extraInfo != null) {
			if (extraInfo.containsKey("PLAYCOUNT")) {
				playCount = Integer.parseInt(setting.getSelectionInfoMap().get("PLAYCOUNT"));
			}

			if (extraInfo.containsKey(WebServiceConstants.CALLER_FIRST_NAME)) {
				callingParty.setFirstname(extraInfo.get(WebServiceConstants.CALLER_FIRST_NAME));
			}

			if (setting.getSelectionInfoMap().containsKey(WebServiceConstants.CALLER_LAST_NAME)) {
				callingParty.setLastname(extraInfo.get(WebServiceConstants.CALLER_LAST_NAME));
			}
		}
		//chaged for ephemeral status system_deactivated and deactivated
		String smDaemonMode = RBTParametersUtils.getParamAsString("DAEMON", "MODE_FOR_EXPIRE_SELECTION", "SMDaemon");
		if(playCount > 0 && selectionStatus.equals("deactive") && setting.getDeselectedBy().equals(smDaemonMode)){
			selectionStatus = "systemdeactive";
		}
		//setting.gets
		
		//changed for ephemeral rbt
		PlayRuleInfo playruleinfo = getPlayRuleInfo(playCount);
		BuildPlayRule playRuleBuilder = new BuildPlayRule().setCallingParty(callingParty).setId(setting.getRefID())
				.setAsset(asset).setSchedule(schedule).setPlayRuleInfo(playruleinfo ).setStatus(SelectionStatus.getSelectionStatus(selectionStatus));	
		
		return playRuleBuilder.buildPlayRule();
	}
	
	
	private static Asset getAsset(Setting setting) {

		return BuildAssetFactory.createBuildAssetFactory().buildAssetFactoryFromSetting(setting);	
	}	
	
	
	public static String getCircleId(Subscriber subscriber) {
		String circleId = null;
		if(subscriber != null)
			circleId = subscriber.circleID().trim().split("_")[1];
		return circleId;
	}
	
	public static String getOperatorName(Subscriber subscriber) {
		String operator = "";
		
		if(subscriber != null){
			operator = subscriber.operatorName();
//			if(!operator.isEmpty()){
//				return operator;
//			}
			
			if(StringUtils.trimToNull(operator) != null){
				return operator;
			}
			
			
		if(!subscriber.circleID().trim().contains("_"))
		     operator=	ConsentPropertyConfigurator.getOperatorFormConfig();
			else
			 operator = subscriber.circleID().trim().split("_")[0];
		
			
		}
		return operator;
	}
	
	public static String getOperatorName(com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber) {
		String operator = "";
		
		if(subscriber != null){
		if(!subscriber.getCircleID().trim().contains("_"))
		     operator=	ConsentPropertyConfigurator.getOperatorFormConfig();
			else
			 operator = subscriber.getCircleID().trim().split("_")[0];
		
		}
		return operator;
	}
	
	public static String getClipTransferUrl(String operatorName) {
		PropertyConfig config = (PropertyConfig) ConfigUtil.getBean(BeanConstant.PROPERTY_CONFIG);
		return config.getValueFromResourceBundle("GRIFF_CLIP_TRANSFER_URL_"+operatorName.toUpperCase());		
	}
	
	public static String getThirdPartyUrl(String configName) {
		return ((PropertyConfig) ConfigUtil.getBean(BeanConstant.PROPERTY_CONFIG)).getValueFromResourceBundle(configName);
	}
	
	
	private static Schedule getSchedule(Setting setting) {
		
		Schedule schedule = null;		
		
		DateRange dateRange = getDateRangeObj(setting);		
		
		TimeRange timeRange = getTimeRange(setting);
		
		ScheduleBuilder scheduleBuilder = new ScheduleBuilder();
		
		ScheduleType scheduleType = ScheduleType.DATETIMERANGE;
		if(setting.getFromTime() == 0 && setting.getToTime() == 23 && setting.getFromTimeMinutes() == 0 && setting.getToTimeMinutes() == 59){
			scheduleType = ScheduleType.DATETIMECONTINUOUSRANGE;
		}
		scheduleBuilder.setType(scheduleType).setDateRange(dateRange).setTimeRange(timeRange)
					   .setId(1).setDescription("Default");
		
		schedule = scheduleBuilder.buildSchedule();
		
		return schedule;
	}
	
	private static DateRange getDateRangeObj(Setting setting) {
		
		Date startDate = setting.getStartTime();
		Date endDate = setting.getEndTime();		
		
		DateRangeBuilder dateRangeBuilder = new DateRangeBuilder();
		dateRangeBuilder.setStartDate(startDate).setEndDate(endDate);
		return dateRangeBuilder.buildDateRange();
	}
	
	private static TimeRange getTimeRange(Setting setting) {
		
		if(setting.getFromTime() == 0 && setting.getToTime() == 23 && setting.getFromTimeMinutes() == 0 && setting.getToTimeMinutes() == 59){
			return getTimeRangeFromStartEndDate(setting);
		}
		
		String fromTimeInString = new StringBuffer().append(setting.getFromTime()).append(":").append(setting.getFromTimeMinutes()).append(":").append("0").toString();
		String toTimeInString = new StringBuffer().append(setting.getToTime()).append(":").append(setting.getToTimeMinutes()).append(":").append("0").toString();
		String timePattern = "HH:mm:ss";
		Date fromTimeObj = convertToDateObj(fromTimeInString, timePattern);
		Date toTimeObj = convertToDateObj(toTimeInString, timePattern);

		TimeRangeBuilder timeRangeBuilder = new TimeRangeBuilder();
		timeRangeBuilder.setFromTime(fromTimeObj).setToTime(toTimeObj);
		
		return timeRangeBuilder.buildTimeRange();
	}

	private static TimeRange getTimeRangeFromStartEndDate(Setting setting) {
		
		Date startTime = setting.getStartTime();
		Date endTime = setting.getEndTime();

		TimeRangeBuilder timeRangeBuilder = new TimeRangeBuilder();
		timeRangeBuilder.setFromTime(startTime).setToTime(endTime);
		
		return timeRangeBuilder.buildTimeRange();	
		
	}
	
	public static Date convertToDateObj(String dateInString, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		Date date = null;
		try {
			date = formatter.parse(dateInString);
		} catch (ParseException e) {
			logger.error("Exception Occurred: "+e, e);
		}
		
		logger.info("Time: "+dateInString+" Converted to Date: "+date);
		return date;
	}
	
	public static int getHrs(String timeInString) {
		if(timeInString.length() == 4) {
			return Integer.parseInt(timeInString.substring(0, 2).intern());
		}
		else {
			return Integer.parseInt(timeInString.substring(0, 1).intern());
		}
	}
	
	public static int getMins(String timeInString) {

		try {
			if(timeInString.length() == 1)
				return Integer.parseInt(timeInString);

			if(timeInString.length() == 4) {
				return Integer.parseInt(timeInString.substring(2, 4).intern());
			}
			else {
				return Integer.parseInt(timeInString.substring(1, 3).intern());
			}
		} catch (Exception e) {
			logger.error("Exception Occure: "+e);
			return 0;
		}
	}
	
	public static String getTime(int hrs,int mins) {
		DecimalFormat decimalFormat = new DecimalFormat("00");
		int time = Integer.parseInt(hrs
				+ decimalFormat.format(mins));
		return time+"";
		
	}
	
	
	public static List<Clip> getClipsFromUDPMap(List<UDPContentMap> contentMaps) throws DataAccessException {
		List<Clip> clips = null;
		for(UDPContentMap contentMap : contentMaps) {			
			
			String type = contentMap.getContentKeys().getType().toString();
			long toneId = contentMap.getContentKeys().getClipId();
			Clip clip = getClip(toneId, type);
			
			if(clips == null)
				clips = new ArrayList<Clip>(contentMaps.size());
			
			if(clip != null)
				clips.add(clip);
			
		}
		return clips;
	}
	
	public static Clip getClip(long toneId, String type) throws DataAccessException {
		Clip clip = null;
		if(type != null && type.equalsIgnoreCase("RBTUGC")) {
			clip = getRBTUGCClip(toneId, type);
		} else if(type.equalsIgnoreCase("SONG")){			
			clip = RBTCacheManager.getInstance().getClip((int) toneId);
		}	
		if(clip == null)
			throw new DataAccessException(Constants.CLIP_NOT_EXIST);
		return clip;
	}
	
	public static Clip getRBTUGCClip(long ugcId, String type) throws DataAccessException {
		IRbtUgcWavfileDao rbtUgcWavfileDao = null;
		Clip clip = null;
		try {
			rbtUgcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
			RBTUgcWavfile rbtUgcWavfile = rbtUgcWavfileDao.getUgcWavFile(ugcId);
			clip = new Clip();
			clip.setClipId((int)ugcId);
			clip.setAlbum(type);
			clip.setClipRbtWavFile(rbtUgcWavfile.getUgcWavFile());
		} catch (NoSuchBeanDefinitionException e) {
			logger.error(e.getBeanName()+" is not configured, returning clip as null");
		}
		return clip;
	}
	
	public static String getAssetType(int categoryType) throws Exception {
		String assetType = null;
		try {
			AssetTypeAdapter assetTypeAdapter = (AssetTypeAdapter) ConfigUtil.getBean(BeanConstant.ASSET_TYPE_ADAPTER);
			assetType = assetTypeAdapter.getAssetType(categoryType);
		} catch (NoSuchBeanDefinitionException e) {
			throw new UserException(e.getBeanName()+" is not configured");
		}
		return assetType;
	}
	
	// Added for getting latest active Selection
	public static SubscriberStatus getSubscriberLatestSelection(String subscriberID, Map<String, String> whereClauseMap){
		List<SubscriberStatus> subscriberActiveSelections = RBTDBManager.getInstance().getSubscriberActiveSelections(subscriberID, whereClauseMap);
		if(subscriberActiveSelections != null && subscriberActiveSelections.size() > 0){
			return subscriberActiveSelections.get(0);
		}
		return null;
	}
	
	
	// Added for getting latest active Selection
	public static Setting getSubscriberLatestSelectionFromSetting(Settings settings, Map<String, String> whereClauseMap){
		
		List<Setting> lastestsettings  = new ArrayList<Setting>();
		
		if(settings != null){
			
			Setting[] setting = settings.getSettings();
			
			if(setting != null && setting.length >0){
				for(Setting set : setting){
					Setting selection = null;
					
					if (whereClauseMap != null &&  whereClauseMap.containsKey("CALLER_ID") 
							&& (set.getCallerID().equals(whereClauseMap.get("CALLER_ID")) || (set.getCallerID().equals("all") && whereClauseMap.get("CALLER_ID") == null)))
					{
						selection = set;
					}
					
					if (selection != null && whereClauseMap != null && whereClauseMap.containsKey("CATEGORY_ID"))
					{
						if(selection.getCategoryID() != Integer.parseInt(whereClauseMap.get("CATEGORY_ID"))){
							selection = null;
						}
					}
					else if (selection != null  && whereClauseMap != null &&  whereClauseMap.containsKey("SUBSCRIBER_WAV_FILE"))
					{
						if(!selection.getRbtFile().equals((String)whereClauseMap.get("SUBSCRIBER_WAV_FILE"))){
							selection = null;
						}
					}
					else if (selection != null  && whereClauseMap != null &&  whereClauseMap.containsKey("UDP_ID"))
					{
						if(selection.getUdpId() != Integer.parseInt(whereClauseMap.get("UDP_ID"))){
							selection = null;
						}
					}
					
					if(selection != null){
						lastestsettings.add(selection);
					}
					
				}
			}
			
		}
		
		if(lastestsettings != null && lastestsettings.size() > 0){
			return lastestsettings.get(lastestsettings.size() -1);
		}
		return null;
	}
	
	private static PlayRuleInfo getPlayRuleInfo(int playcount) {
		
		BuildPlayRuleInfo buildPlayRuleInfo = new BuildPlayRuleInfo();
		
		buildPlayRuleInfo.setPlaycount(playcount);
		
		return buildPlayRuleInfo.buildPlayRuleInfo();
	}
	
	public static boolean isStringValid(String str) {
		if (str == null || str.trim().isEmpty()) {
			return false;
		}
		return true;
	}
	
	public static String getURLEncodedValue(String param) {
		if (param == null) {
			return null;
		}
		try {
			return URLEncoder.encode(param, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Encoding error. " + e, e);
			return null;
		}
	}
	
	public static String replaceStringInString (String fullString, String toBeReplaced, String newString) {
		if (newString != null) {
			try {
				return fullString.replaceAll(toBeReplaced, URLEncoder.encode(newString, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("UnsupportedEncodingException " + e, e);
				return null;
			}
		} else {
			return fullString.replaceAll(toBeReplaced, "");
		}
	}
	
	public static boolean isSubscriberActive(String subscriberID) {
		
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		boolean status = false;

		if (subscriber != null
				&& (rbtDBManager.isSubscriberActivated(subscriber)
				|| rbtDBManager.isSubscriberActivationPending(subscriber))) {
			status = true;
		}
		logger.info("isSubscriberActive is returning :"+status);
		return status;
	}
	
	public static Map<String, String> getRequestParamsMap(HttpServletRequest request) {
		Map<String, String> requestParams = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String key = params.nextElement();
			String value = request.getParameter(key).trim();
			requestParams.put(key, value);
		}
		return requestParams;
	}
	
	public static String[] getOperatorAndCircleId(String circleId, OperatorUserDetails operatorUserDetails) {
		try {
			String[] operator_circle = null;
			
			
			if (operatorUserDetails != null && (operatorUserDetails.serviceKey()
					.equalsIgnoreCase(OperatorUserTypes.LEGACY.getDefaultValue())
					|| operatorUserDetails.serviceKey().equalsIgnoreCase(OperatorUserTypes.LEGACY_FREE_TRIAL.getDefaultValue()))) {
				operator_circle = new String[2];
				operator_circle[0] = operatorUserDetails.operatorName();
				operator_circle[1] = operatorUserDetails.circleId();
			}
			
			
			if (circleId != null) {
				operator_circle = circleId.split("_");

			}
			if (operator_circle != null && operator_circle.length == 2) {
				return operator_circle;
			}
		} catch (Exception e) {
			logger.error("Exception occured: " + e.getMessage());
		}
		return null;
	}
	
	public static String replaceStringInStringWithOutEncoding(String fullString, String toBeReplaced, String newString) {
		if (newString != null) {
			return fullString.replaceAll(toBeReplaced, newString);
		} else {
			return fullString.replaceAll(toBeReplaced, "");
		}
	}
	
}
