package com.onmobile.apps.ringbacktones.daemons.inline.handler;

import org.apache.log4j.Logger;
import org.springframework.messaging.Message;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.daemons.inline.TPInlineHelper;

public class TPHandler {
	private static Logger logger = Logger.getLogger(TPHandler.class);
	
	public TPHandler() {
	}

	public void handleAct(Message<Object> springMessage) {
		if (logger.isDebugEnabled()) {
			logger.debug("Inside TPHandler::handleAct()");
		}
		Subscriber subscriber = null;
		try {
			if (springMessage.getPayload() != null) {
				subscriber = (Subscriber)(springMessage.getPayload());
				if(subscriber == null) {
					logger.error("Spring message is not Subscriber object " + springMessage.getPayload());
					return;
				}
				TPInlineHelper.addSelectionsToplayer(subscriber);
			} else{
				logger.info("Subscriber payload inside SpringMessage is coming null!!!");
			}
		} catch (Exception e) {
			logger.error("Error while handling TP act" + subscriber + e);
		}
	}
	
	public void handleDct(Message<Object> springMessage) {
		if (logger.isDebugEnabled()) {
			logger.debug("Inside TPHandler::handleDct()");
		}
		Subscriber subscriber = null;
		try {
			if (springMessage.getPayload() != null) {
				subscriber = (Subscriber)(springMessage.getPayload());
				if(subscriber == null) {
					logger.error("Spring message is not Subscriber object " + springMessage.getPayload());
					return;
				}
				TPInlineHelper.removeSelectionsFromplayer(subscriber);
			} else{
				logger.info("Subscriber payload inside SpringMessage is coming null!!!");
			}
		} catch (Exception e) {
			logger.error("Error while handling TP dct" + subscriber + e);
		}
	}
}
