package com.onmobile.apps.ringbacktones.callLog.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import voldemort.versioning.Versioned;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onmobile.apps.ringbacktones.callLog.CallLogConstants;
import com.onmobile.apps.ringbacktones.callLog.beans.CallLog;
import com.onmobile.apps.ringbacktones.callLog.beans.CallLogHistoryBean;
import com.onmobile.apps.ringbacktones.callLog.beans.HelperCallLogBean;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;

public class CallLogUtils {


	public static String objectToGson(Object obj){
		String jsonObject;
		if(obj==null)
			return null;
		Gson gson = new GsonBuilder().setDateFormat(getDateFormat()).create();
		
		jsonObject= gson.toJson(obj);
		System.out.println("JSON "+jsonObject);
		return jsonObject;
	}


	

	public static Map<String,Object> convertToMap(HelperCallLogBean helperCallLogBean) {
		Map<String, Object> callLogHistoryMap = new HashMap<String, Object>();

			callLogHistoryMap.put("categoryId", helperCallLogBean.getCategoryId());
			callLogHistoryMap.put("callerId", helperCallLogBean.getCallerId());
			callLogHistoryMap.put("calledId", helperCallLogBean.getCalledId());
			callLogHistoryMap.put("callType", helperCallLogBean.getCallType());
			callLogHistoryMap.put("timeOfCall", helperCallLogBean.getTimeOfCall());
			callLogHistoryMap.put("wavFileName", helperCallLogBean.getWavFileName());
		
		return callLogHistoryMap;
	}

	public static HelperCallLogBean getHelperCallLogBean(
			CurrentPlayingSongBean currentPlayingSongBean, String callType) {

		HelperCallLogBean helperCallLogBean = null;

		if (currentPlayingSongBean != null) {
			helperCallLogBean = new HelperCallLogBean();
			helperCallLogBean.setCalledId(currentPlayingSongBean.getCalledId());
			helperCallLogBean.setCallerId(currentPlayingSongBean.getCallerId());
			helperCallLogBean.setCallType(getCallType(callType));
			helperCallLogBean.setCategoryId(currentPlayingSongBean
					.getCategoryId());
			helperCallLogBean.setTimeOfCall(new Date());
			helperCallLogBean.setWavFileName(currentPlayingSongBean
					.getWavFileName());

		}

		return helperCallLogBean;
	}

	
	public static String getDateFormat() {
		return "yyyy-MM-dd HH:mm:ss";
		
	}
	
	public static String getCallType(String type) {
		String callType = null;
		if (type.equalsIgnoreCase("calledId"))
			callType = "Incoming";
		else 
			callType = "Outgoing";
		
		return callType;
	}
	
	public static String getKeyToSaveCallLogHistory(String callerId, String calledId, String type) {
		if(type.equals("callerId")){
			return "callerId_"+callerId;
		}else{
			return "calledId_"+calledId;
		}
	}
	
	
	public static String getKeyToGetCallLogHistory(String subscriberId, String type) {
		if(type.equalsIgnoreCase("Outgoing")){
			return "callerId_"+subscriberId;
		}else{
			return "calledId_"+subscriberId;
		}
	}
	
	
	public static String getParameter(String key, String type) {
		String value = RBTParametersUtils.getParamAsString(type, key, null);
		return value;
		
	}
	
	
	public static CallLog getCallLog(HelperCallLogBean helperCallLogBean,String callType) {
		CallLog callLog = null;
		Category category = null;
		Clip clip = null;
		RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
		
		category = getCategory(rbtCacheManager, helperCallLogBean.getCategoryId());
		
		clip = getClip(rbtCacheManager, helperCallLogBean.getWavFileName());
		
		if (clip != null) {
			callLog = new CallLog();
			callLog.setCalledId(helperCallLogBean.getCalledId());
			callLog.setCallerId(helperCallLogBean.getCallerId());
			callLog.setCallType(helperCallLogBean.getCallType());
			callLog.setTimeOfCall(helperCallLogBean.getTimeOfCall());
			callLog.setClip(clip);
			callLog.setCategory(category);
		}

		return callLog;
	}
	
	
	public static CallLogHistoryBean getCallLogHistoryBean(List<Map<String,Object>> callLogHistory,String callType,int offSet,int pageSize) {
		List<CallLog> callLogs = null;
		int size = callLogHistory.size();
		int start = offSet * pageSize;
		int end = pageSize == 0? -1 : (size-1) - (start + pageSize);

		if(callLogHistory !=  null) {

			for(int i = ((size-1) - start); i >= 0 && i > end; i--) {
				Map<String, Object> map = callLogHistory.get(i);
				String json = CallLogUtils.objectToGson(map);
				HelperCallLogBean helperCallLogBean = jsonToBean(json);
				CallLog callLog = CallLogUtils.getCallLog(helperCallLogBean, callType);
				if (callLog != null) {
					if (callLogs == null)
						callLogs = new ArrayList<CallLog>();

					callLogs.add(callLog);
				}
			}
		}
		CallLogHistoryBean callLogHistoryBean = null;
		if (callLogs != null) {
			callLogHistoryBean = new CallLogHistoryBean();
			callLogHistoryBean.setCount(callLogs.size());
			callLogHistoryBean.setCallLog(callLogs);
		}

		return callLogHistoryBean;
	}
	
	

	public static List<Map<String,Object>> getCallLogHistoryList(Versioned<List<Map<String,Object>>> versioned,
			Map<String, Object> calledLogHistoryMap) {
		List<Map<String,Object>> callLogHistoryList = null;
		if (versioned != null && versioned.getValue() != null && !versioned.getValue().isEmpty()) {
			callLogHistoryList = versioned.getValue();
			callLogHistoryList.add(calledLogHistoryMap);
		} else {
			callLogHistoryList = new ArrayList<Map<String,Object>>();
			callLogHistoryList.add(calledLogHistoryMap);					
		}
		
		return callLogHistoryList;
	}
	
		
	
	public static List<Map<String,Object>> getExpiredCallLogRecords(Versioned<List<Map<String,Object>>> versioned) {
		
		List<Map<String,Object>> expiredCallLogHistoryList = null;
		
		for (Map<String, Object> map : versioned.getValue()) {
			String json = objectToGson(map);
			HelperCallLogBean callLogBean =jsonToBean(json);
			if (isNotALive(callLogBean)) {
				if (expiredCallLogHistoryList == null)
					expiredCallLogHistoryList = new ArrayList<Map<String,Object>>();
				expiredCallLogHistoryList.add(map);
			} else {
				break;
			}
			
		}
		return expiredCallLogHistoryList;
	}
	
	public static HelperCallLogBean jsonToBean(String jsonElement) {
		Gson gson = new GsonBuilder().setDateFormat(getDateFormat()).create();
		return gson.fromJson(jsonElement, HelperCallLogBean.class);
	}
	
	public static boolean isNotALive(HelperCallLogBean callLogBean) {
		boolean isNotAlive = false;
		int numOfDays = getNumOfDaysToBeAlive();
		Calendar expDate = GregorianCalendar.getInstance();
		expDate.setTime(callLogBean.getTimeOfCall());
		expDate.add(Calendar.DATE, -numOfDays);
		Calendar timeOfCall = GregorianCalendar.getInstance();
		timeOfCall.setTime(callLogBean.getTimeOfCall());
		
		if (timeOfCall.compareTo(expDate) == -1)
			isNotAlive = true;
		
		return isNotAlive;
		
	}
	
	public static int getNumOfDaysToBeAlive() {
		return RBTParametersUtils.getParamAsInt(CallLogConstants.PARAM_TYPE, CallLogConstants.NUM_OF_DAYS_TO_BE_EXPIRED, 7);
	}
	
	
	public static boolean isValidString(String value) {
		if (value == null || value.isEmpty())
			return false;
		return true;
	}
	
	
	public static Category getCategory(RBTCacheManager rbtCacheManager , int catId) {
		Category category = null;
		if (catId == -1) {
			category = new Category();
			category.setCategoryId(-1);
			category.setCategoryPromoId("DUMMY_PROMO_ID");
			category.setCategoryName("DUMMY_CATEGORY_NAME");
			category.setCategoryInfo("DUMMY_INFO");
			category.setCategoryLanguage("ENGLISH");
			category.setCategoryNameWavFile("DUMMY_CATEGORY_NAME_WAV_FILE");

		}				
		else {
			category = rbtCacheManager.getCategory(catId);
		}
		return category;
	}
	
	public static Clip getClip(RBTCacheManager rbtCacheManager, String wavFileName) {
		Clip clip = null;
		clip = rbtCacheManager.getClipByRbtWavFileName(wavFileName);
		if (clip == null) {
			List<String> configuredCategory = Arrays.asList((RBTParametersUtils
					.getParamAsString("MOBILEAPP",
							"REALTIME_SONG_NOTIFICATION_CONTENT_TYPES",
							"AZAAN,COPTIC,DUA,CRICKET,FOOTBALL,ADRBT,UGC"))
							.toLowerCase().split(","));
			
			Pattern	pattern = Pattern.compile("rbt_[0-9]+_[0-9]+_rbt");
			Matcher matcher = pattern.matcher(wavFileName);
			
			if (configuredCategory.contains(wavFileName.toLowerCase())) {
				clip = new Clip();
				clip.setAlbum("DUMMY");
				clip.setClipGrammar("DUMMY");
				clip.setClipId(-1);
				clip.setClipLanguage("ENGLISH");
				clip.setClipRbtWavFile(wavFileName);
				clip.setClipName("dummy");
			} else if (matcher.matches()) {
				clip = new Clip();
				clip.setAlbum("UGC");
				clip.setClipGrammar("UGC");
				clip.setClipId(-1);
				clip.setClipLanguage("UGC");
				clip.setClipRbtWavFile("UGC");
				clip.setClipName("ugc");
			}
		}
		return clip;
	}
	
}
