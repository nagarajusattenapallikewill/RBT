package com.onmobile.apps.ringbacktones.v2.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.livewiremobile.store.storefront.dto.calllog.CallLog;
import com.livewiremobile.store.storefront.dto.calllog.LogEntry;
import com.livewiremobile.store.storefront.dto.calllog.LogEntry.CallType;
import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.Caller;
import com.onmobile.apps.ringbacktones.callLog.CallLogHistory;
import com.onmobile.apps.ringbacktones.callLog.beans.HelperCallLogBean;
import com.onmobile.apps.ringbacktones.callLog.utils.CallLogUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.dao.CallLogDAO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.bean.AssetBean;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.factory.BuildAssetFactory;
import com.onmobile.apps.ringbacktones.webservice.actions.GetCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;

public class CallLogVoltronImpl  implements CallLogHistory,Constants{
	
	Logger logger = Logger.getLogger(CallLogVoltronImpl.class);
	private ResponseErrorCodeMapping errorCodeMapping;

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}

	@Override
	public void save(CurrentPlayingSongBean currentPlayingSongBean, String type) {
		logger.warn("Method not implemented");
		
	}

	@Override
	public void saveCallLogHistory(HelperCallLogBean helperCallLogBean,String type) {
		logger.warn("Method not implemented");
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public CallLog getCallLogHistory(String subscriberId,
			String callType, int offSet, int pageSize) {
		logger.debug("getCallLogHistory entred");
		
		List<Map<String,Object>> callLogHistoryList = CallLogDAO.get(subscriberId, callType);
		CallLog callLog = new CallLog();
		Gson gson = new Gson();
		
		if(callLogHistoryList == null) {
			logger.error("No CallLog");
			return callLog;
		}
		
		int size = callLogHistoryList.size();
		int start = (offSet != -1 && pageSize != 0) ? (offSet * pageSize) : (offSet != -1) ? offSet : 0;
		start = (size-1) - start;
		int end = pageSize == 0? -1 : (start - pageSize);
		
		List<LogEntry> logEntryList = new ArrayList<LogEntry>();
		if(callLogHistoryList !=  null) {
			for(int i = (start); i >= 0 && i > end; i--) {
				Map<String, Object> callLogHistory = callLogHistoryList.get(i);
				String json = CallLogUtils.objectToGson(callLogHistory);
//				HelperCallLogBean helperCallLogBean = gson.fromJson(json,HelperCallLogBean.class);
				HelperCallLogBean helperCallLogBean = CallLogUtils.jsonToBean(json);
				Caller caller = new Caller();
				
				String calledId = helperCallLogBean.getCalledId();
				String callerId = helperCallLogBean.getCallerId();
				String calledType =  helperCallLogBean.getCallType();
				
				if(calledType.equalsIgnoreCase("INCOMING")) {
					caller.setId(Long.parseLong(callerId));
				}
				else{
					caller.setId(Long.parseLong(calledId));
				}
				
				Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(helperCallLogBean.getWavFileName());
				if(clip == null) {
					continue;
				}
				
				AssetBean assetBean = null;
				String wavFileName = helperCallLogBean.getWavFileName();
				if(wavFileName != null && wavFileName.contains(".wav")){
					wavFileName = wavFileName.replace(".wav", "");
				}
				if (wavFileName != null && wavFileName.contains("_cut_")) {
					String cutrbtduration = wavFileName.substring(wavFileName.lastIndexOf("_")+1);
					logger.info(":---> CutRbtDuration" + cutrbtduration);
					assetBean = new AssetBean(clip.getClipId(), null, clip.getClipName(), helperCallLogBean.getCategoryId(), null, cutrbtduration);

				}else{
					 assetBean = new AssetBean(clip.getClipId(), null, clip.getClipName(), helperCallLogBean.getCategoryId(), null);
				}
				
				Asset asset = null;
				
				
				try {
					asset = BuildAssetFactory.createBuildAssetFactory().buildAssetFactory(assetBean);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage(), e);
				}
				
				if(asset == null) {
					continue;
				}
				
				LogEntry logEntry = new LogEntry(caller, false, helperCallLogBean.getTimeOfCall(), asset, CallType.valueOf(calledType.toUpperCase()));
				
				logEntryList.add(logEntry);
			}
		}
		
		callLog.setLogEntries(logEntryList);
		callLog.setOffset(offSet);
		callLog.setTotalItemCount(callLogHistoryList.size());
		callLog.setItemCount(logEntryList.size());
		
		return callLog;
	}

	@Override
	public LogEntry getCurrentPlayingSong(String subscriberID, String type) throws UserException{
		GetCurrentPlayingSong getCurrentPlayingSong = new GetCurrentPlayingSong();
		LogEntry logEntry = null;
		WebServiceContext webServiceContext = new WebServiceContext();
		webServiceContext.put(WebServiceConstants.param_subscriberID, subscriberID);
		webServiceContext.put(WebServiceConstants.param_info, type);
		try {
			if (type.equalsIgnoreCase("incoming")) {
				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
				com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = RBTClient.getInstance()
						.getSubscriber(rbtDetailsRequest);
				if (!Utility.isUserActive(subscriber.getStatus())) {
					throw new UserException(SUB_DONT_EXIST);
				}
			}
			WebServiceResponse webServiceResponse = getCurrentPlayingSong.processAction(webServiceContext);
			if (webServiceResponse == null) {
				logger.error("webServiceResponse from getCurrentPlayingSong is null");
				throw new UserException(UNKNOWN_ERROR);
			}

			Document document = XMLUtils.getDocumentFromString(webServiceResponse.getResponse());
			String resonse = document.getElementsByTagName("response").item(0).getFirstChild().getNodeValue();
			Clip clip = null;
			logger.info("GetCurrentPlayingSong response:" + resonse);
			if (resonse.equalsIgnoreCase("success")) {
				String wavFileName = document.getElementsByTagName("wav_file").item(0).getFirstChild().getNodeValue();
				String categoryId = document.getElementsByTagName("category_id").item(0).getFirstChild().getNodeValue();
				String callerId = null;
				if (document.getElementsByTagName("caller_id") != null)
					callerId = document.getElementsByTagName("caller_id").item(0).getFirstChild().getNodeValue();

				clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFileName);
				Caller caller = new Caller();
				caller.setId(Long.parseLong(callerId));
				if (clip == null) {
					throw new UserException(CLIP_NOT_EXIST);
				}

				AssetBean assetBean = null;
				if(wavFileName != null && wavFileName.contains(".wav")){
					wavFileName = wavFileName.replace(".wav", "");
				}
				if (wavFileName != null && wavFileName.contains("_cut_")) {
					String cutrbtduration = wavFileName.substring(wavFileName.lastIndexOf("_")+1);
					logger.info(":---> CutRbtDuration" + cutrbtduration);
					assetBean = new AssetBean(clip.getClipId(), null, clip.getClipName(), Integer.parseInt(categoryId), null, cutrbtduration);

				}else{
				    assetBean = new AssetBean(clip.getClipId(), null, clip.getClipName(),
						Integer.parseInt(categoryId), null);
				}
				Asset asset = BuildAssetFactory.createBuildAssetFactory().buildAssetFactory(assetBean);

				logEntry = new LogEntry(caller, false, new Date(), asset, CallType.valueOf(type.toUpperCase()));

			} else {
				logger.error("No Log");
				throw new UserException(LOG_NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			ServiceUtil.throwCustomUserException(errorCodeMapping, e.getMessage(), MessageResource.CALLLOG_MESSAGE_FOR);
		}
		return logEntry;
	}

}
