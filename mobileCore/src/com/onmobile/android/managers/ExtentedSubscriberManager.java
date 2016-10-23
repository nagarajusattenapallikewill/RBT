package com.onmobile.android.managers;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.android.utils.StringConstants;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class ExtentedSubscriberManager extends SubscriberManager{
	
	private static Logger logger = Logger.getLogger(SubscriberManager.class);
	private static RBTClient client = RBTClient.getInstance();
	
	@Override
	public String removeSubscriberSelection(String subscriberId, String clipId, String caller, String channel, String selStartTime, String selEndTime, String rbtWavFile, String circleId) {
		logger.info("clip ID "+clipId+" channel "+channel+"caller "+caller);
		SelectionRequest selRequest = new SelectionRequest(subscriberId,clipId);
		selRequest.setMode(channel);
		selRequest.setCallerID(caller);
		if (clipId.equals("0"))
		{
			selRequest.setRbtFile(rbtWavFile);
			selRequest.setClipID(null);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date startDate = null;
		Date endDate = null;
		if(selStartTime!=null && selEndTime!=null){
			try {
				startDate = dateFormat.parse(selStartTime);
				endDate = dateFormat.parse(selEndTime);

				selRequest.setSelectionStartTime(startDate);
				selRequest.setSelectionEndTime(endDate);
			} catch (ParseException e) {
				logger.info("sselectionStartTime or selectionEndTime not in proper format(yyyyMMddHHmmss)");
				logger.error(e.getMessage(), e);
				return StringConstants.FAILURE;
			}
		}
		if(circleId != null && !circleId.isEmpty())
			selRequest.setCircleID(circleId);
		//RBT-16263	Unable to remove selection for local/site RBT user
		selRequest.setRedirectionRequired(true);
		client.deleteSubscriberSelection(selRequest);
		logger.info("RBT:: getRemoveSelectionResponse "+selRequest.getResponse());	
		return selRequest.getResponse();
	}

}