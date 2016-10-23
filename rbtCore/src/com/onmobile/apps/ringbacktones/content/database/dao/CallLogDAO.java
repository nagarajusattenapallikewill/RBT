package com.onmobile.apps.ringbacktones.content.database.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import voldemort.client.StoreClient;
import voldemort.versioning.Versioned;

import com.onmobile.apps.ringbacktones.callLog.beans.HelperCallLogBean;
import com.onmobile.apps.ringbacktones.callLog.utils.CallLogUtils;
import com.onmobile.apps.ringbacktones.callLog.utils.StoreClientFactoryInstance;

public class CallLogDAO {

	private static Logger logger = Logger.getLogger(CallLogDAO.class);

	public static List<Map<String,Object>> get(String subscriberId,String callType){
		StoreClient<String, List<Map<String,Object>>> storeClient = null;
		List<Map<String,Object>> callLogHistory = null;
		try {
			storeClient = StoreClientFactoryInstance.getStoreClientInstance();
			logger.info("Store_Client: "+storeClient);
			if (storeClient != null) {
				callLogHistory = getCallLogHistory(storeClient, callType, subscriberId);
				logger.info("Call_LOG_HISTORY: ");//+callLogHistory);
			} else {
				logger.error("Store Name or Bootstrap Url for Voldemort is not configured in DB");
			}
		} catch (Throwable t) {
			logger.error("Exception Occured: "+t.getMessage(),t);
		} 

		return callLogHistory;
	}

	public static void save(HelperCallLogBean helperCallLogBean,String type) {
		System.out.println("...");
		logger.info("SaveCallLogHistoryToVoldemort method is Called");
		StoreClient<String, List<Map<String, Object>>> storeClient = null;
		try {
			storeClient = StoreClientFactoryInstance.getStoreClientInstance();
			logger.info("Store_Client: "+storeClient);
			if (storeClient != null ) {

				List<Map<String,Object>> callLogHistoryList = null;
				Versioned<List<Map<String,Object>>> versioned = null;
				String key = CallLogUtils.getKeyToSaveCallLogHistory(
						helperCallLogBean.getCallerId(),
						helperCallLogBean.getCalledId(), type);
				try {
					versioned = storeClient.get(key);
				} catch (Throwable t) {
					logger.error("Exception Occured: for key is: " + key);
					throw new Throwable(
							"Exception Occured: for key is: " + key, t);
				}
				Map<String, Object> calledLogHistoryMap = CallLogUtils.convertToMap(helperCallLogBean);
				callLogHistoryList = CallLogUtils.getCallLogHistoryList(versioned, calledLogHistoryMap);
				try {
					storeClient.put(key, callLogHistoryList);
				} catch (Throwable t) {
					logger.error("Exception Occured for key is: "
							+ key
							+ " for callLogHistoryList size : "
							+ (null != callLogHistoryList ? callLogHistoryList
									.size() : null));
					throw new Throwable(
							"Exception Occured: for key is: " + key, t);
				}
				logger.info("SAVING DATA: " + key);
				//logger.info("SAVING DATA:: "+key+"   "+callLogHistoryList);
			} 
			else {
				logger.error("Store Name or Bootstrap Url for Voldemort is not configured in DB");
				return;
			}
		} catch (Throwable t) {
			logger.error("Exception Occured: " + t, t);
		} 
	}


	private static  List<Map<String,Object>> getCallLogHistory(StoreClient<String, List<Map<String,Object>>> storeClient,String callType,String subscriberId) {
		List<Map<String,Object>> callLogHistory = new ArrayList<Map<String,Object>>();
		Versioned<List<Map<String,Object>>> versioned = null;
		String key = null;
		if (callType.equalsIgnoreCase("ALL")) {
			key = "callerId_"+subscriberId;
			versioned = storeClient.get(key);
			if (versioned != null) {
				List<Map<String,Object>> updatedCallLogHistoryList = updateCallLogHistory(storeClient,versioned,key);
				if (updatedCallLogHistoryList != null)
					callLogHistory.addAll(versioned.getValue());

			}
			key = "calledId_"+subscriberId;
			versioned = storeClient.get(key);
			if (versioned != null) {
				List<Map<String,Object>> updatedCallLogHistoryList = updateCallLogHistory(storeClient,versioned,key);
				if (updatedCallLogHistoryList != null)
					callLogHistory.addAll(versioned.getValue());

			}
		} else {
			key = CallLogUtils.getKeyToGetCallLogHistory(subscriberId, callType);
			versioned = storeClient.get(key);
			if (versioned != null) {
				List<Map<String,Object>> updatedCallLogHistoryList = updateCallLogHistory(storeClient,versioned,key);
				if (updatedCallLogHistoryList != null)
					callLogHistory.addAll(updatedCallLogHistoryList);

			}
		}		

		return callLogHistory;
	}



	public static List<Map<String,Object>> updateCallLogHistory(StoreClient<String, List<Map<String,Object>>> storeClient, Versioned<List<Map<String,Object>>> versioned,String key) {

		List<Map<String,Object>> expiredCallLogRecords = CallLogUtils.getExpiredCallLogRecords(versioned);
		List<Map<String, Object>> liveCallLogRecords = null;


		if (expiredCallLogRecords != null) {
			if (expiredCallLogRecords.size() != versioned.getValue().size()) {
				liveCallLogRecords = versioned.getValue();
				liveCallLogRecords.removeAll(expiredCallLogRecords);
				updateCallLogRecords(storeClient,liveCallLogRecords, key);
			} else {
				deleteCallLog(storeClient,key);
			}

		} else {
			liveCallLogRecords = versioned.getValue();
		}
		return liveCallLogRecords;
	}

	public static void deleteCallLog(StoreClient<String, List<Map<String,Object>>> storeClient, String key) {
		if (storeClient != null)
			storeClient.delete(key);
	}

	public static void updateCallLogRecords(StoreClient<String, List<Map<String,Object>>> storeClient,List<Map<String,Object>> updatedDetails,String key) {

		if (storeClient!= null ) {
			storeClient.put(key, updatedDetails);
		}

	}


}
