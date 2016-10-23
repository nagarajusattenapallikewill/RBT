package com.onmobile.apps.ringbacktones.rbt2.command;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.exception.RestrictionException;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

/**
 * @author vikrant.verma
 * 
 * Class to restrict CutRbt
 */
public class CutRbtRestrictCommand extends FeatureListRestrictionCommand implements iRBTConstant {
	private static com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager rbtCacheManager = null;
	private Object categoryId;
	private static Logger logger = Logger.getLogger(CutRbtRestrictCommand.class);

	@Override
	public void executeCalback(String msisdn) {
/*
		logger.info("Inside executeCalback method ");
		String deSelectedBy = "SYSTEM";
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
		if (subscriber != null) {
			deSelectedBy = subscriber.activatedBy();
		}
		String response = RBTDBManager.getInstance().deactivateSubscriberCutRbtRecords(msisdn, deSelectedBy);
		

	
*/
	}
	@Override
	public String executeInlineCall(SelectionRequest selectionRequest, String clipID) throws RestrictionException {

		logger.info("Inside Inline CutRbt restrict");
		if(selectionRequest!=null)
		{
			logger.info("selectionRequest : "+selectionRequest.toString());
			logger.info("selectionRequest getRbtFile : "+selectionRequest.getRbtFile());
			if (selectionRequest.getRbtFile() != null && selectionRequest.getRbtFile().contains("_cut_")) {
				throw new RestrictionException(Constants.CUT_RBT_NOT_SUPPORTED);

			}
			
		}
		return "SUCCESS";

	}
	
}

	