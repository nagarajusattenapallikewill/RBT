package com.onmobile.apps.ringbacktones.rbt2.command;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.exception.RestrictionException;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class SpecialSelectionRestrictCommand extends FeatureListRestrictionCommand implements iRBTConstant{

	private static Logger logger = Logger.getLogger(SpecialSelectionRestrictCommand.class);
	
	@Override
	public void executeCalback(String msisdn) {
		String deSelectedBy = "SYSTEM";
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
		if(subscriber != null){
			deSelectedBy = subscriber.activatedBy();
		}
		String response = RBTDBManager.getInstance().deactivateSubscriberRecordsByStatus(msisdn, 80, deSelectedBy);
		logger.info("response of deactivating TOD selctions is : "+response);
		response = RBTDBManager.getInstance().deactivateSubscriberRecordsByStatus(msisdn, 75, deSelectedBy);
		logger.info("response of deactivating DOW selctions is : "+response);
	}
	
	@Override
	public String executeInlineCall(SelectionRequest selectionRequest, String clipID) throws RestrictionException {
		logger.info("Inside  executeInlineCall method in SpecialSelectionRestrictCommand : "+selectionRequest);
		if((selectionRequest.getStatus() == null || selectionRequest.getStatus() == 80 ||  selectionRequest.getStatus() == 75)
				&& (selectionRequest.getFromTime() != null || selectionRequest.getToTime() != null)){
		  throw new RestrictionException(Constants.SPECIAL_SELECTION_NOT_SUPPORTED);
	 }
	 return "SUCCESS";
		
	}

}
