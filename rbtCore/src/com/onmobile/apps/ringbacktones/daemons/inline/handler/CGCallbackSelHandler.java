package com.onmobile.apps.ringbacktones.daemons.inline.handler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.DoubleConfirmationContentProcessUtils;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.daemons.inline.CGInlineHelper;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

public class CGCallbackSelHandler {
	private static Logger logger = Logger.getLogger(CGCallbackSelHandler.class);
	@Autowired
	private CGInlineHelper cgHelper;
	private DoubleConfirmationContentProcessUtils dcUtils;
	private RBTDBManager rbtDBManager;
	
	public CGCallbackSelHandler() {
		rbtDBManager = RBTDBManager.getInstance();
		dcUtils = new DoubleConfirmationContentProcessUtils();
	}
	
	public void handle(Message<Object> springMessage) {
		if (logger.isDebugEnabled()) {
			logger.debug("Inside CGCallbackSelHandler::handle()");
		}
		
		DoubleConfirmationRequestBean requestBean = null;
		try {
			if (springMessage.getPayload() != null) {
				
				requestBean = (DoubleConfirmationRequestBean)(((GenericMessage<?>)(springMessage.getPayload())).getPayload());
				if(requestBean == null) {
					logger.error("Spring message is not DoubleConfirmationRequestBean object " + springMessage.getPayload());
					return;
				} 
				cgHelper.processConsent(requestBean); //Also update consent flag to 1
				logger.info("CGC hit for selection act is successful for subscriber " + requestBean.getSubscriberID());
				return;
			} else {
				logger.info("DoubleConfirmationRequestBean Payload inside SpringMessage is coming null!!!");
			}
		} catch (OnMobileException oe) {
			logger.error("Error while handling CGC sel" + requestBean + oe);
		} catch (Exception e) {
			logger.error("Error while handling CGC sel" + requestBean + e);
		}
		
		if(requestBean == null) {
			logger.error("DoubleConfirmationRequestBean is null " + springMessage.getPayload());
			return;
		}
		if(Utility.resetInlineFlag(requestBean))
			logger.info("Falling back to daemon approach for CGC selection is successful for : " + requestBean.getSubscriberID());
		else
			logger.info("Falling back to daemon approach for CGC selection is failed for: " + requestBean.getSubscriberID());
	}
	
	public void processCGRecordIntoSelection(Message<Object> springMessage) {
		if (logger.isDebugEnabled()) {
			logger.debug("Inside CGCallbackSelHandler::processCGRecordIntoSelection()");
		}
		
		DoubleConfirmationRequestBean requestBean = null;
		try {
			if (springMessage.getPayload() != null) {
				requestBean = (DoubleConfirmationRequestBean)(springMessage.getPayload());
				
				if(requestBean == null) {
					logger.error("Spring message is not DoubleConfirmationRequestBean object " + springMessage.getPayload());
					return;
				}
				DoubleConfirmationRequestBean updatedReqBean = rbtDBManager.getConsentRecordForMsisdnNTransId(requestBean.getSubscriberID(), requestBean.getTransId());
				String response = dcUtils.processRecord(updatedReqBean); //entry in selection
				if(!response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
					throw new OnMobileException(WebServiceConstants.CONSENT_PROCESSING_RECORD_FAILURE);
				logger.info("Process record for CGC is successful for subscriber " + requestBean.getSubscriberID());
				return;
			} else {
				logger.info("DoubleConfirmationRequestBean Payload inside SpringMessage is coming null!!!");
			}
		} catch (OnMobileException oe) {
			logger.error("Error while handling CGC sel" + requestBean + oe);
		} catch (Exception e) {
			logger.error("Error while handling CGC sel" + requestBean + e);
		}
		
		if(requestBean == null) {
			logger.error("DoubleConfirmationRequestBean is null " + springMessage.getPayload());
			return;
		}
		if(Utility.resetInlineFlag(requestBean))
			logger.info("Falling back to daemon approach for CGC record is successful for : " + requestBean.getSubscriberID());
		else
			logger.info("Falling back to daemon approach for CGC record is failed for: " + requestBean.getSubscriberID());
	}

	public CGInlineHelper getCgHelper() {
		return cgHelper;
	}

	public void setCgHelper(CGInlineHelper cgHelper) {
		this.cgHelper = cgHelper;
	}
}
