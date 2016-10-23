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

public class CGRealtimeSelHandler {
	private static Logger logger = Logger.getLogger(CGRealtimeSelHandler.class);
	@Autowired
	private CGInlineHelper cgHelper;
	private RBTDBManager rbtDBManager;
	private DoubleConfirmationContentProcessUtils dcUtils;
	
	public CGRealtimeSelHandler() {
		rbtDBManager = RBTDBManager.getInstance();
		dcUtils = new DoubleConfirmationContentProcessUtils();
	}

	public void handle(Message<Object> springMessage) {
		if (logger.isDebugEnabled()) {
			logger.debug("Inside CGRealtimeSelHandler::handle()");
		}
		
		DoubleConfirmationRequestBean requestBean = null;
		try {
			if (springMessage.getPayload() != null) {
				requestBean = (DoubleConfirmationRequestBean)(((GenericMessage<?>)(springMessage.getPayload())).getPayload());
				
				if(requestBean == null) {
					logger.error("Spring message is not DoubleConfirmationRequestBean object " + springMessage.getPayload());
					return;
				}
				//Also while picking from consent for status 2 have flag logic for daemon: Done
				cgHelper.processConsent(requestBean); //Also update consent flag to 1
				logger.info("CGR hit for selection act is successful for subscriber " + requestBean.getSubscriberID());
				DoubleConfirmationRequestBean updatedReqBean = rbtDBManager.getConsentRecordForMsisdnNTransId(requestBean.getSubscriberID(), requestBean.getTransId());
				String response = dcUtils.processRecord(updatedReqBean); //entry in selection
				if(!response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
					throw new OnMobileException(WebServiceConstants.CONSENT_PROCESSING_RECORD_FAILURE);
				logger.info("Process record for CGR is successful for subscriber " + requestBean.getSubscriberID());
				//passing to QueueProvisioning based on srv key conf will be done by BasicRBTProcessor/RBTDbManager
				return;
			} else {
				logger.info("DoubleConfirmationRequestBean Payload inside SpringMessage is coming null!!!");
			}
		} catch (OnMobileException oe) {
			logger.error("Error while handling CGR sel" + requestBean + oe);
		} catch (Exception e) {
			logger.error("Error while handling CGR sel" + requestBean + e);
		}
		
		if(requestBean == null) {
			logger.error("DoubleConfirmationRequestBean is null " + springMessage.getPayload());
			return;
		}
		if(Utility.resetInlineFlag(requestBean))
			logger.info("Falling back to daemon approach for CGR selection is successful for : " + requestBean.getSubscriberID());
		else
			logger.info("Falling back to daemon approach for CGR selection is failed for: " + requestBean.getSubscriberID());
	}

	public CGInlineHelper getCgHelper() {
		return cgHelper;
	}

	public void setCgHelper(CGInlineHelper cgHelper) {
		this.cgHelper = cgHelper;
	}
}
