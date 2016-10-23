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
 * Class to restrict all songs other than configured category Id & ephemeral 
 */
public class SpecificContentNotRestrictCommand extends FeatureListRestrictionCommand implements iRBTConstant {
	private static com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager rbtCacheManager = null;
	private Object categoryId;
	private static Logger logger = Logger.getLogger(SpecificContentNotRestrictCommand.class);

	@Override
	public void executeCalback(String msisdn) {

		logger.info("Inside executeCalback method");
		String deSelectedBy = "SYSTEM";
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
		if (subscriber != null) {
			deSelectedBy = subscriber.activatedBy();
		}
		String response = RBTDBManager.getInstance().deactivateSubscriberRecordsByNotCategoryIdNotStatus(msisdn, getCategoryId(),200
				,deSelectedBy);

		logger.info("response of deactivating selection other than ( CategoryId" + getCategoryId() +"AND Status = 200 ) "+ "response=" + response);

	}

	@Override
	public String executeInlineCall(SelectionRequest selectionRequest, String clipID) throws RestrictionException {

		if(selectionRequest!=null&&selectionRequest.getStatus()!=null&&selectionRequest.getStatus()==200)
		{
			logger.info(":--> ephmeral selection");
			return "SUCCESS";
		}
		
		logger.info("Inside executeInlineCall method , categoryId in Cofig:" + getCategoryId() + ",clipId passed:"
				+ clipID);
		try {
			rbtCacheManager = RBTCacheManager.getInstance();
			Clip[] clips = rbtCacheManager.getActiveClipsInCategory(getCategoryId());

			for (Clip clip : clips) {
				logger.info(" clip present in category  " + clip.getClipId());
				if (clip.getClipId() == Integer.parseInt(clipID)) {
					logger.info(":--> free song selection");
					return "SUCCESS";
				}
			}

		} catch (Exception e) {

			logger.info("Exception in executeInlineCall " + e.getMessage());
		}

		throw new RestrictionException(Constants.CONTENT_SELECTION_NOT_SUPPORTED);

	}

	public int getCategoryId() {

		if (categoryId != null) {
			return Integer.parseInt(categoryId.toString());
		}
		return 103; // By default

	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

}
