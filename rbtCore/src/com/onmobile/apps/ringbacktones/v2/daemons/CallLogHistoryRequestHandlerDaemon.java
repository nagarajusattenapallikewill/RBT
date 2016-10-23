package com.onmobile.apps.ringbacktones.v2.daemons;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.tcp.MessageType;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.ViralPromotionRequest;
import com.onmobile.apps.ringbacktones.daemons.tcp.supporters.ViralPromotion;
import com.onmobile.apps.ringbacktones.v2.dto.CallLogDTO;
import com.onmobile.apps.ringbacktones.v2.dto.RBTViralPromotion;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.SendHandler;

public class CallLogHistoryRequestHandlerDaemon implements Runnable {
	private static Logger logger = Logger.getLogger(CallLogHistoryRequestHandlerDaemon.class);
	private final CallLogDTO[] callLogList;
	private final String operatorName;
	private static ConcurrentHashMap<String, String> waveFileMap = new ConcurrentHashMap<String, String>();

	public CallLogHistoryRequestHandlerDaemon(CallLogDTO[] callLogList, String operatorName) {
		this.callLogList = callLogList;
		this.operatorName = operatorName;
	}

	@Override
	public void run() {
		logger.info("Inside storeCallLog : CallLogDTO size" + callLogList.length);
		try {
			waveFileMap = LoadWavFileMappingToMapping.getWaveFileMap();
			if (callLogList != null && callLogList.length > 0) {
				for (int i = 0; i <= (callLogList.length - 1); i++) {
					CallLogDTO callLogDTO = callLogList[i];
					String wavFile = callLogDTO.getWavFile();
					String wavFile2_0 = null;
					String key = wavFile + "_" + operatorName;
					if (wavFile != null && operatorName != null)
						wavFile2_0 = waveFileMap.get(key.toUpperCase());
					if (wavFile2_0 != null && !wavFile2_0.isEmpty()) {
						wavFile = wavFile2_0;
						callLogDTO.setWavFile(wavFile);
					}
					logger.info("Inside storeCallLog : key value is: " + key + " mapped waveFile for 1.0 wavfile is :"
							+ wavFile + ", " + wavFile2_0);
					String messageTypeID = callLogDTO.getType();
					
					//(Senthil)To do: refine code later
					if (messageTypeID != null && (messageTypeID.equalsIgnoreCase("1"))) {
						logger.info("Inside ViralPromotionRequest sendPromotion");
						ViralPromotionRequest viralPromotionRequest = convertToViralPromotionRequest(callLogDTO, MessageType.VIRAL_PROMOTION);
						ViralPromotion.sendPromotion(viralPromotionRequest);
						logger.info("Processes ViralPromotionRequest");
					} else if(messageTypeID != null && (messageTypeID.equalsIgnoreCase("3") || messageTypeID.equalsIgnoreCase("calllog"))){
						CurrentPlayingSongBean bean = new CurrentPlayingSongBean();
						bean.setCalledId(callLogDTO.getCalledId());
						bean.setCallerId(callLogDTO.getCallerId());
						bean.setWavFileName(wavFile);
						bean.setCategoryId(callLogDTO.getCategoryId());
						try {
							SendHandler.addToMemcache(bean);
						} catch (Exception e) {
							logger.debug("Exception while entrying record:" + e.getMessage());
						} catch (Throwable e) {
							logger.debug("Exception while entrying record:" + e.getMessage());
						}
						callLogDTO = null;
						bean = null;
					}else{
						logger.debug("Type nmot supported Type:" + messageTypeID);
						
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Processing failed Exception occured sendPromotion :" + e.getMessage());
		}
	}

	private ViralPromotionRequest convertToViralPromotionRequest(CallLogDTO callLogDTO, MessageType messageType) throws UserException {
		logger.info("Inside ViralPromotionRequest convertToViralPromotionRequest");
		try {
			ViralPromotionRequest viralPromotionRequest = new ViralPromotionRequest();
			viralPromotionRequest.setCallDuration(callLogDTO.getCallDuration());
			viralPromotionRequest.setCalledID(callLogDTO.getCalledId());
			viralPromotionRequest.setCalledTime(callLogDTO.getCalledTime());
			viralPromotionRequest.setCallerID(callLogDTO.getCallerId());
			viralPromotionRequest.setCallerLanguage(callLogDTO.getCallerLanguage());
			viralPromotionRequest.setMessageType(messageType);
			viralPromotionRequest.setRbtWavFile(callLogDTO.getWavFile());
			viralPromotionRequest.setValidationRequired(callLogDTO.isValidationRequired());
			return viralPromotionRequest;
		} catch (Exception e) {
			throw new UserException("INVALIDPARAMETER", e.getMessage());
		}

	}

}
