package com.onmobile.apps.ringbacktones.rbt2.command;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class GroupFeatureRestrictCommand extends FeatureListRestrictionCommand implements iRBTConstant{

	private static Logger logger = Logger.getLogger(GroupFeatureRestrictCommand.class);
	
	@Override
	public void executeCalback(String msisdn) {
		// TODO Auto-generated method stub
		//remove group song selection
		//remove groups
		//remove group members
		
		logger.info("GroupFeatureRestrictCommand execute begins for subscriber: " + msisdn);
		SubscriberStatus[] subscriberSelections = RBTDBManager.getInstance().getAllActiveSubscriberSettings(msisdn);
		
		if(subscriberSelections != null && subscriberSelections.length != 0) {			
			for(SubscriberStatus subscriberSelection : subscriberSelections) {
				if(subscriberSelection.callerID() != null && subscriberSelection.callerID().startsWith("G")) {
					char oldLoopStatus = subscriberSelection.loopStatus();
					char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
					if (oldLoopStatus == LOOP_STATUS_EXPIRED)
						newLoopStatus = oldLoopStatus;
					else if (oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT
							|| oldLoopStatus == LOOP_STATUS_LOOP_INIT)
						newLoopStatus = LOOP_STATUS_EXPIRED;
					
					RBTDBManager.getInstance().smSelectionActivationRenewalFailure(msisdn, subscriberSelection.refID(), 
							"DAEMON", null, subscriberSelection.classType(), newLoopStatus, subscriberSelection.selType(), 
							subscriberSelection.extraInfo(), subscriberSelection.circleId());
				}
			}
			
			logger.info("Directly deactivated all group related song selecctions for the subscriber: " + msisdn);
		}
		
		//Remove Group members
		boolean isGroupMemberDeleted = RBTDBManager.getInstance().deleteGroupMembersOfSubscriber(msisdn);
		if(isGroupMemberDeleted) {
			logger.info("Removed all group members for the subscriber: " + msisdn);
		}
		else{
			logger.info("Not successfully removed all group members for the subscriber: " + msisdn);
		}

		//Remove groups
		boolean isDeleted = RBTDBManager.getInstance().deleteGroupsOfSubscriber(msisdn);
		if(isDeleted) {
			logger.info("Removed all groups for the subscriber: " + msisdn);
		}
		else{
			logger.info("Not successfully removed all groups for the subscriber: " + msisdn);
		}
		
		logger.info("GroupFeatureRestrictCommand execute ends for subscriber: " + msisdn);

	}

	@Override
	public String executeInlineCall(SelectionRequest selectionRequest, String clipID) {
	  return null;		
	}

}
