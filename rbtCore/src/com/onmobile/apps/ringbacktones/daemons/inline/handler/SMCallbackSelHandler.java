package com.onmobile.apps.ringbacktones.daemons.inline.handler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.inline.SMInlineHelper;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.common.exception.OnMobileException;

public class SMCallbackSelHandler {
	private static Logger logger = Logger.getLogger(SMCallbackSelHandler.class);
	@Autowired
	private SMInlineHelper smHelper;
	private RBTDBManager rbtDBManager;
	
	public SMCallbackSelHandler() {
		rbtDBManager = RBTDBManager.getInstance();
	}

	public void handleSelAct(Message<Object> springMessage) {
		if (logger.isDebugEnabled()) {
			logger.debug("Inside SMCallbackSelHandler::handleSelAct()");
		}
		SubscriberStatus ss = null;
		try {
			boolean result = false;
			
			if (springMessage.getPayload() != null) {
				ss = (SubscriberStatus)(((GenericMessage<?>)(springMessage.getPayload())).getPayload());
				if(ss == null) {
					logger.error("Spring message is not Subscriber status object " + springMessage.getPayload());
					return;
				}
				result = smHelper.processSelAct(ss);
				if(result) {
					//or update it under SMInlineHelper??
					rbtDBManager.updateSubscriberSelectionInlineDaemonFlag(ss.subID(), ss.refID(), 2);
					if(logger.isDebugEnabled()) {
						logger.debug("SM hit for selection act is successful for subscriber " + ss.subID());
					}
					return;
				}
			} else{
				logger.info("SubscriberStatus Payload inside SpringMessage is coming null!!!");
			}
		} catch (OnMobileException oe) {
			logger.error("Error while handling selections act" + ss + oe);
		} catch (Exception e) {
			logger.error("Error while handling selections act" + ss + e);
		}
		
		if(ss == null) {
			logger.error("Subscriber status is null " + springMessage.getPayload());
			return;
		}
		if(Utility.resetInlineFlag(ss))
			logger.info("Falling back to daemon approach for subscriber selection act is successful for : " + ss.subID());
		else
			logger.info("Falling back to daemon approach for subscriber selection act is failed for: " + ss.subID());
	}
	
	public void handleSelDct(Message<Object> springMessage) {
		if (logger.isDebugEnabled()) {
			logger.debug("Inside SMCallbackSelHandler::handleSelDct()");
		}
		SubscriberStatus ss = null;
		try {
			boolean result = false;
			if (springMessage.getPayload() != null) {
				ss = (SubscriberStatus)(((GenericMessage<?>)(springMessage.getPayload())).getPayload());
				if(ss == null) {
					logger.error("Spring message is not Subscriber status object " + springMessage.getPayload());
					return;
				}
				result = smHelper.processSelDct(ss);
				if(result) {
					if(logger.isDebugEnabled()) {
						logger.debug("SM hit for selection dct is successful for subscriber " + ss.subID());
					}
					return;
				}
			}else {
				logger.info("SubscriberStatus Payload inside SpringMessage is coming null!!!");
			}
		} catch (OnMobileException oe) {
			logger.error("Error while handling selections dct" + ss + oe);
		} catch (Exception e) {
			logger.error("Error while handling selections dct" + ss + e);
		}
		
		if(ss == null) {
			logger.error("Subscriber status is null " + springMessage.getPayload());
			return;
		}
		if(Utility.resetInlineFlag(ss))
			logger.info("Falling back to daemon approach for subscriber selection dct is successful for : " + ss.subID());
		else
			logger.info("Falling back to daemon approach for subscriber selection dct is failed for: " + ss.subID());
	}

	public SMInlineHelper getSmHelper() {
		return smHelper;
	}

	public void setSmHelper(SMInlineHelper smHelper) {
		this.smHelper = smHelper;
	}
}
