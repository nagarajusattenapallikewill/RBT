package com.onmobile.apps.ringbacktones.daemons;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

import qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.CompositeCmsInterfaceStub;
import qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.CompositeCmsInterfaceStub.CampaignType;
import qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.CompositeCmsInterfaceStub.Description_type1;
import qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.CompositeCmsInterfaceStub.ErrorType;
import qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.CompositeCmsInterfaceStub.UpdateProvisioningStatus;
import qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.CompositeCmsInterfaceStub.UpdateProvisioningStatusE;
import qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.CompositeCmsInterfaceStub.UpdateProvisioningStatusResponse;
import qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.CompositeCmsInterfaceStub.UpdateProvisioningStatusResponseE;
import qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.CompositeCmsInterfaceStub.UpdateProvisioningType;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;

public class IBMStatusUpdateDaemon extends TimerTask {

	public static int intervalTime = 5;
	public static int retryCountAllowed = 5;
	private static final String DAEMON = "DAEMON";
	private static final String SUCCESS = "SUCCESS";
	private static final String FAILURE = "FAILURE";
	private static final String RETRY_COUNT_EXCEEDED = "RETRY_COUNT_EXCEEDED";
	private static String INTERVAL_TIME_FOR_IBM_CALLBACK_DAEMON = "INTERVAL_TIME_FOR_IBM_CALLBACK_DAEMON";
	private static String IBM_CALLBACK_RETRY_COUNT_ALLOWED = "IBM_CALLBACK_RETRY_COUNT_ALLOWED";
	private static Logger logger = Logger.getLogger(IBMStatusUpdateDaemon.class);
    private static Logger callbackLogger = Logger.getLogger(IBMStatusUpdateDaemon.class.getName()+".BackUp");
    
	IBMStatusUpdateDaemon() {
		intervalTime = RBTParametersUtils.getParamAsInt(DAEMON,
				INTERVAL_TIME_FOR_IBM_CALLBACK_DAEMON, 5);
		retryCountAllowed =  RBTParametersUtils.getParamAsInt(DAEMON,
				IBM_CALLBACK_RETRY_COUNT_ALLOWED, 5);
	}

	public void start() {
		Timer timer = new Timer(IBMStatusUpdateDaemon.class.getSimpleName());
		// converting to milliseconds
		long intervalTimeInMilli = intervalTime * 60 * 1000L;
		timer.scheduleAtFixedRate(this, 0L, intervalTimeInMilli);
	}

	@Override
	public void run() {
		logger.info("Started " + IBMStatusUpdateDaemon.class.getSimpleName());
		List<RBTCallBackEvent> rbtCallBackSuccessEventList = new RBTCallBackEvent()
				.getCallbackEventsOfModule(RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
						RBTCallBackEvent.SM_SUCCESS_CALLBACK_RECEIVED);
		logger.info("No of records IBM Status to be processed == "+rbtCallBackSuccessEventList);
		List<RBTCallBackEvent> rbtCallBackFailureEventList = new RBTCallBackEvent()
		        .getCallbackEventsOfModule(RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
				        RBTCallBackEvent.SM_FAILURE_CALLBACK_RECEIVED);
		logger.info("No of records IBM Status to be processed == "+rbtCallBackFailureEventList);
        List<RBTCallBackEvent> rbtCallBackEventList = new ArrayList<RBTCallBackEvent>();
        rbtCallBackEventList.addAll(rbtCallBackSuccessEventList);
        rbtCallBackEventList.addAll(rbtCallBackFailureEventList);
		if (rbtCallBackEventList == null || rbtCallBackEventList.isEmpty()) {
			logger.info("No records to process.");
		} else {
			logger.info(rbtCallBackEventList.size() + " record(s) to be processed.");
			for (RBTCallBackEvent rbtCallBackEvent : rbtCallBackEventList) {
				String message = rbtCallBackEvent.getMessage();
				try {
					String response = process(rbtCallBackEvent);
					if (response.equalsIgnoreCase(SUCCESS)
							|| response.equalsIgnoreCase(RETRY_COUNT_EXCEEDED)) {
						callbackLogger.info(rbtCallBackEvent+",status="+response);
						rbtCallBackEvent.delete();
						logger.info("Deleted IBM Integration rbtCallBackEvent: " + rbtCallBackEvent);
					} else {
						rbtCallBackEvent.update();
						logger.info("Updated IBM Integration rbtCallBackEvent: " + rbtCallBackEvent);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			logger.info("Finished procressing all records.");
		}

	}

	public String process(RBTCallBackEvent rbtCallBackEvent) {
		String msg = rbtCallBackEvent.getMessage();
		String campaignCode = null;
		String treatmentCode = null;
		String offerCode = null;
		int retryCount = 0;
		if (msg != null) {
			String str[] = msg.split(",");
			if (str.length == 4) {
				campaignCode = str[0].split("=")[1].trim();
				treatmentCode = str[1].split("=")[1].trim();
				offerCode = str[2].split("=")[1].trim();
				retryCount = Integer.parseInt(str[3].split("=")[1].trim());
			}
		}
		if(retryCount >= retryCountAllowed){
			return RETRY_COUNT_EXCEEDED;
		}
		String msg1 = iRBTConstant.CAMPAIGN_CODE + "=" + campaignCode + ","
				+ iRBTConstant.TREATMENT_CODE + "=" + treatmentCode + ","
				+ iRBTConstant.OFFER_CODE + "=" + offerCode + ","
				+ iRBTConstant.RETRY_COUNT + "="+(retryCount+1);
		rbtCallBackEvent.setMessage(msg1);
		int status = rbtCallBackEvent.getEventType();
		try {
			String targetEndPoint = RBTParametersUtils.getParamAsString("DAEMON", "IBM_WEBSERVICE_ENDPOINT_URL", null);
			CompositeCmsInterfaceStub cmsInterfaceStub = null;
			logger.info("TargetEndPoint = "+targetEndPoint);
			if(targetEndPoint!=null){
				cmsInterfaceStub = new CompositeCmsInterfaceStub(targetEndPoint);
			}else{
				cmsInterfaceStub = new CompositeCmsInterfaceStub();
			}
			UpdateProvisioningStatusE updateProvisioningStatusE = new UpdateProvisioningStatusE();
			UpdateProvisioningStatus updateProvisioningStatus = new UpdateProvisioningStatus();
			UpdateProvisioningType updateProvisioningType = new UpdateProvisioningType();
			if(status == RBTCallBackEvent.SM_SUCCESS_CALLBACK_RECEIVED){
			     updateProvisioningType.setStatus("5");
			     updateProvisioningType.setStatusDescription("OK");
			}else{
				updateProvisioningType.setStatus("4");
			    updateProvisioningType.setStatusDescription("Activation Failed");
			}
			CampaignType campaignType = new CampaignType();
			campaignType.setCampaignCode(campaignCode);
			campaignType.setTreatmentCode(treatmentCode);
            campaignType.setOfferCode(offerCode);
			updateProvisioningStatus.setProvisioning(updateProvisioningType);
			updateProvisioningStatus.setCampaign(campaignType); 
			updateProvisioningStatus.setMSISDN(rbtCallBackEvent.getSubscriberID());
			updateProvisioningStatus.setSource(rbtCallBackEvent.getSelectedBy());
			updateProvisioningStatusE.setUpdateProvisioningStatus(updateProvisioningStatus);
			UpdateProvisioningStatusResponseE updateProvisioningStatusResponse = cmsInterfaceStub
					.updateProvisioningStatus(updateProvisioningStatusE);
			UpdateProvisioningStatusResponse updateProvisioningStatusResponse2 = updateProvisioningStatusResponse
					.getUpdateProvisioningStatusResponse();
			ErrorType errorType = updateProvisioningStatusResponse2.getError();
			logger.info("IBMStatusUpdateDaemon Error Type ="+errorType);
			if (errorType != null) {
				Description_type1 description = errorType.getDescription();
				if (description != null) {
					String callbackUpdationResult = description.getDescription_type0();
					logger.info("IBMStatusUpdateDaemon callbackUpdationResult ="+callbackUpdationResult);
					if (callbackUpdationResult == null || callbackUpdationResult.trim() == null) {
						return SUCCESS;
					} else {
						return FAILURE;
					}
				}
			}else{
				return SUCCESS;
			}
		} catch (AxisFault e) {
			e.printStackTrace();
			logger.info("Exception1 = "+ e);
			logger.info("Stack trace1  ==== "+e.getStackTrace());
			logger.info("Fill stack trace1 = "+e.fillInStackTrace());
			logger.info("LocalizedMessage1 = "+e.getLocalizedMessage());
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.info("Exception2 = "+ e);
			logger.info("Stack trace2  ==== "+e.getStackTrace());
			logger.info("Fill stack trace2 = "+e.fillInStackTrace());
			logger.info("LocalizedMessage2 = "+e.getLocalizedMessage());
		} catch (Error e) {
			e.printStackTrace();
			logger.info("Exception3 = "+ e);
			logger.info("Stack trace3  ==== "+e.getStackTrace());
			logger.info("Fill stack trace3 = "+e.fillInStackTrace());
			logger.info("LocalizedMessage3 = "+e.getLocalizedMessage());
		} catch (qa.vodafone.www.egate.cms.composite_cms.service.v1_0_0.Error e) {
			e.printStackTrace();
			logger.info("Exception4 = "+ e);
			logger.info("Stack trace4  ==== "+e.getStackTrace());
			logger.info("Fill stack trace4 = "+e.fillInStackTrace());
			logger.info("LocalizedMessage4 = "+e.getLocalizedMessage());
		}
		
		return FAILURE;

	}

}
