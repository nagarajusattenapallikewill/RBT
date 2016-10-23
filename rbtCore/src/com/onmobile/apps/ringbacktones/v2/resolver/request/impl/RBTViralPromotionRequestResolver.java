package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.tcp.MessageType;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.ViralPromotionRequest;
import com.onmobile.apps.ringbacktones.daemons.tcp.supporters.ViralPromotion;
import com.onmobile.apps.ringbacktones.v2.dto.RBTViralPromotion;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public class RBTViralPromotionRequestResolver {

	private static Logger logger = Logger.getLogger(RBTViralPromotionRequestResolver.class);

	public void sendPromotion(RBTViralPromotion rbtViralPromotion) throws UserException {
		try {
			logger.info("Inside ViralPromotionRequest sendPromotion");
			ViralPromotionRequest viralPromotionRequest = convertToViralPromotionRequest(rbtViralPromotion);
			ViralPromotion.sendPromotion(viralPromotionRequest);
			logger.info("Processes ViralPromotionRequest");
		} catch (Throwable e) {
			logger.error("Exception occured sendPromotion :" + e.getMessage());
			throw new UserException(e.getMessage());
		}
	}

	private ViralPromotionRequest convertToViralPromotionRequest(RBTViralPromotion rbtViralPromotion)
			throws UserException {
		logger.info("Inside ViralPromotionRequest convertToViralPromotionRequest");
		if (rbtViralPromotion == null) {
			logger.info("rbtViralPromotion is null");
			throw new UserException("INVALIDPARAMETER", "RBTVIRALPROMOTION_NOT_AVAILABLE");
		}
		try {
			ViralPromotionRequest viralPromotionRequest = new ViralPromotionRequest();
			viralPromotionRequest.setCallDuration(rbtViralPromotion.getCallDuration());
			viralPromotionRequest.setCalledID(rbtViralPromotion.getCalledId());
			viralPromotionRequest.setCalledTime(rbtViralPromotion.getCalledTime());
			viralPromotionRequest.setCallerID(rbtViralPromotion.getCallerId());
			viralPromotionRequest.setCallerLanguage(rbtViralPromotion.getCallerLanguage());
			viralPromotionRequest.setCircleID(rbtViralPromotion.getCircleId());
			if (rbtViralPromotion.getMessageType() != null
					&& rbtViralPromotion.getMessageType().equals(MessageType.VIRAL_PROMOTION.toString())) {
				viralPromotionRequest.setMessageType(MessageType.VIRAL_PROMOTION);
			}
			viralPromotionRequest.setRbtWavFile(rbtViralPromotion.getRbtWavFile());
			viralPromotionRequest.setValidationRequired(rbtViralPromotion.isValidationRequired());
			return viralPromotionRequest;
		} catch (Exception e) {
			throw new UserException("INVALIDPARAMETER", e.getMessage());
		}

	}
}
