package com.onmobile.apps.ringbacktones.v2.controller;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livewiremobile.store.storefront.dto.calllog.CallLog;
import com.livewiremobile.store.storefront.dto.calllog.LogEntry;
import com.onmobile.apps.ringbacktones.callLog.CallLogHistory;
import com.onmobile.apps.ringbacktones.callLog.beans.CallLogHistoryBean;
import com.onmobile.apps.ringbacktones.callLogImpl.CallLogHistoryImpl;
import com.onmobile.apps.ringbacktones.v2.bean.ServiceResolver;
import com.onmobile.apps.ringbacktones.v2.bean.response.CallLogBatchResponse;
import com.onmobile.apps.ringbacktones.v2.daemons.CallLogHistoryRequestHandlerDaemon;
import com.onmobile.apps.ringbacktones.v2.daemons.CallLogHistoryRequestHandlerExecutor;
import com.onmobile.apps.ringbacktones.v2.dto.CallLogDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.service.CallLogVoltronImpl;

@RestController
@Scope(value = "session")
@RequestMapping("/callLog")
public class CallLogHistoryRestController implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2310313701287841692L;

	private static Logger logger = Logger
			.getLogger(CallLogHistoryRestController.class);
	private static ConcurrentHashMap<String, String> waveFileMap = new ConcurrentHashMap<String, String>();

	@Autowired
	private ServiceResolver serviceResolver;

	public void setServiceResolver(ServiceResolver serviceResolver) {
		this.serviceResolver = serviceResolver;
	}

	@RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST }, value = "/voltron")
	public CallLogBatchResponse storeCallLog(
			@RequestBody CallLogDTO[] callLogList,
			@RequestParam(value = "operatorName", required = true) String operatorName,
			HttpServletRequest request, HttpServletResponse response)
			throws Throwable {
		logger.info("Inside storeCallLog : CallLogDTO size"
				+ callLogList.length);
		int total_request = callLogList.length;
		int failure_count = 0;
		CallLogHistoryRequestHandlerDaemon callLogHisRequest = new CallLogHistoryRequestHandlerDaemon(
				callLogList, operatorName);
		CallLogHistoryRequestHandlerExecutor
				.assginCallLogRequestDetail(callLogHisRequest);
		CallLogBatchResponse batchResponse = new CallLogBatchResponse(
				total_request, total_request - failure_count, failure_count);
		logger.info("CallLog response: " + batchResponse.toString());
		callLogHisRequest = null;
		return (batchResponse);
		
		/*
		logger.info("Inside storeCallLog : CallLogDTO size"
				+ callLogList.length);
		Date requestedTimeStampStart = new Date();
		logger.info("Inside storeCallLog : requestedTimeStamp: "
				+ requestedTimeStampStart);
		// List<CallLogDTO> callLogDTOList = Arrays.asList(callLogList);		
		int total_request = callLogList.length;
		int failure_count = 0;
		waveFileMap = LoadWavFileMappingToMapping.getWaveFileMap();
		if (callLogList != null && callLogList.length > 0) {
			Date requestedTimeStampTemp = new Date();
			for (int i = 0; i <= (callLogList.length - 1); i++) {
				CurrentPlayingSongBean bean = new CurrentPlayingSongBean();
				CallLogDTO callLogDTO = callLogList[i];
				String wavFile = callLogDTO.getWavFile();
				String wavFile2_0 = null;
				
				 * IWavFileMappingDAO wavFileMappingDAO = (IWavFileMappingDAO)
				 * ConfigUtil .getBean(BeanConstant.WAV_FILE_MAPPING_DAO);
				 * WavFileMapping wavFileVerOne = wavFileMappingDAO
				 * .getWavFileVerTwo(wavFile, operatorName); if (wavFileVerOne
				 * != null && wavFileVerOne.getWavFileCompositeKey()
				 * .getWavFileVerTwo() != null &&
				 * !wavFileVerOne.getWavFileCompositeKey()
				 * .getWavFileVerTwo().isEmpty()) { wavFile =
				 * wavFileVerOne.getWavFileCompositeKey() .getWavFileVerTwo(); }
				 
				String key = wavFile + "_" + operatorName;
				if (wavFile != null && operatorName != null)
					wavFile2_0 = waveFileMap.get(key.toUpperCase());
				logger.info("Inside storeCallLog : key value is: " + key
						+ " mapped waveFile for 1.0 wavfile is :" + wavFile
						+ ", " + wavFile2_0);
				bean.setCalledId(callLogDTO.getCalledId());
				bean.setCallerId(callLogDTO.getCallerId());
				if (wavFile2_0 != null && !wavFile2_0.isEmpty())
					wavFile = wavFile2_0;
				bean.setWavFileName(wavFile);
				bean.setCategoryId(callLogDTO.getCategoryId());
				try {
					Date requestedTimeStamp = new Date();
					SendHandler.addToMemcache(bean);
					Date responseTimeStamp = new Date();
					long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp
							.getTime());
					logger.info("Inside storeCallLog : Insertion into memcache differenceTime for each record: "
							+ differenceTime);
				} catch (Exception e) {
					logger.debug("Exception while entrying record:"
							+ e.getMessage());
					failure_count++;
				} catch (Throwable e) {
					logger.debug("Exception while entrying record:"
							+ e.getMessage());
					failure_count++;
				}
				callLogDTO = null;
				bean = null;
			}
			callLogList = null;
			Date responseTimeStamp = new Date();
			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStampStart
					.getTime());

			logger.info("Inside storeCallLog : Before Starting the Loop differenceTime: "
					+ differenceTime);
			differenceTime = (responseTimeStamp.getTime() - requestedTimeStampTemp
					.getTime());
			logger.info("Inside storeCallLog : After Loop Completed differenceTime: "
					+ differenceTime);

		}
		callLogList = null;
		CallLogBatchResponse batchResponse = new CallLogBatchResponse(
				total_request, total_request - failure_count, failure_count);
		logger.info("CallLog response: " + batchResponse.toString());
		return (batchResponse);
	*/}

	@RequestMapping(method = RequestMethod.GET, value = "/voltron")
	public CallLog getCallersCallLogHistoryForVoltron(
			@RequestParam(value = "subscriberId") String msisdn,
			@RequestParam(value = "call_type", required = false, defaultValue = "ALL") String callType,
			@RequestParam(value = "mode", required = false, defaultValue = "WEB") String mode,
			@RequestParam(value = "offset", required = false, defaultValue = "-1") int offset,
			@RequestParam(value = "max", required = false, defaultValue = "0") int max,
			HttpServletRequest request, HttpServletResponse response) {

		CallLogHistory callLogHistory = new CallLogVoltronImpl();
		return ((CallLog) getCallLogHistoryBean(msisdn, callType,
				callLogHistory, offset, max));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/rbt")
	public CallLogHistoryBean getCallersCallLogHistory(
			@RequestParam(value = "subscriberID") String msisdn,
			@RequestParam(value = "callType", required = false, defaultValue = "ALL") String callType,
			@RequestParam(value = "mode", required = false, defaultValue = "WEB") String mode,
			@RequestParam(value = "offSet", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "pageSize", required = false, defaultValue = "0") int max,
			HttpServletRequest request, HttpServletResponse response) {

		CallLogHistory callLogHistory = new CallLogHistoryImpl();
		return (CallLogHistoryBean) getCallLogHistoryBean(msisdn, callType,
				callLogHistory, offset, max);

	}

	private Object getCallLogHistoryBean(String msisdn, String callType,
			CallLogHistory callLogHistory, int offset, int pageSize) {

		return callLogHistory.getCallLogHistory(msisdn, callType, offset,
				pageSize);

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{implPath}/currentPlayingSong")
	public LogEntry getCurrentPlayingSong(
			@RequestParam(value = "subscriberID") String subscriberID,
			@RequestParam(value = "callType") String type,
			@PathVariable(value = "implPath") String implpath,
			HttpServletRequest request, HttpServletResponse response)
			throws UserException {
		logger.info("Inside getCurrentPlayingSong, subscriberID: "
				+ subscriberID + ", type: " + type);

		CallLogHistory callLogHistory = (CallLogHistory) serviceResolver
				.getCallLogHistoryServiceImpl(implpath);
		if (callLogHistory == null) {
			throw new UserException("INVALIDPARAMETER", "SERVICE_NOT_AVAILABLE");
		}

		return callLogHistory.getCurrentPlayingSong(subscriberID, type);

	}

}
