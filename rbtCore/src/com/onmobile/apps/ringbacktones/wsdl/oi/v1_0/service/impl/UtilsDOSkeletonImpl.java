package com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.impl;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Rbt_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.SendSms;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.SmsResponse;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.UtilsDOSkeleton;

public class UtilsDOSkeletonImpl extends UtilsDOSkeleton {
	Logger logger = Logger.getLogger(UtilsDOSkeletonImpl.class);
	@Override
	public SmsResponse sendSms(SendSms sendSms0) {
		Rbt_type0 rbtType = new Rbt_type0();
		SmsResponse response = new SmsResponse();
		UtilsRequest request = new UtilsRequest(sendSms0
				.getSenderID(), String.valueOf(sendSms0.getReceiverID()),
				sendSms0.getSmsText());
		request.setMode(sendSms0.getMode());
		RBTClient.getInstance().sendSMS(request);
		rbtType.setResponse(request.getResponse());
		logger.info("Webservice response: " + request.getResponse());
		response.setRbt(rbtType);
		return response;
	}

}
