package com.onmobile.apps.ringbacktones.webservice.implementation.vodafone;


import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.grameen.GrameenRBTProcessor;

public class VodafoneSpainRBTProcessor extends GrameenRBTProcessor
{

	@Override
	public String deleteSetting(WebServiceContext task) {

		String subscriberID = task.getString(param_subscriberID);
		String refId = task.getString(param_refID);
		int count = 0;
		SubscriberStatus setting = rbtDBManager.getSelectionByRefId(subscriberID, refId);

		if (setting == null || refId == null) {
			return super.deleteSetting(task);
		}

		SubscriberStatus[] activeSettings = rbtDBManager.getAllActiveSubscriberSettings(subscriberID);
		
		if (setting != null && refId != null) {
			boolean isShuffle = Utility.isShuffleCategory(setting.categoryType());
			if (isShuffle) {
				task.put(param_categoryID, setting.categoryID());
			}

			for (SubscriberStatus activesetting : activeSettings) {
				if ((!isShuffle && !Utility.isShuffleCategory(activesetting.categoryType()) && activesetting.subscriberFile().equals(setting.subscriberFile()))
					    || (isShuffle && activesetting.categoryID() == setting.categoryID())) {

					if (count > 1)
						break;
					else
						count++;
				}
			}
		}
		
		if (count > 1) {
			return super.deleteSetting(task);
		} else {
			super.deleteSetting(task);
			task.put(param_action, action_deleteTone);
			task.put(param_rbtFile, setting.subscriberFile());
			return super.deleteTone(task);
		}

	}
	
}

