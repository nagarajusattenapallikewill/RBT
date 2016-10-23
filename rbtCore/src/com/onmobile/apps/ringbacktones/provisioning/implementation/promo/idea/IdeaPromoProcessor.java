package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.idea;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class IdeaPromoProcessor extends PromoProcessor{

	public IdeaPromoProcessor() throws RBTException {
		super();
	}

	public void processSubStatusRequest(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = null;
		if(subscriber == null)
		{
			status = "SUBSCRIBER_NOT_EXIST";
		}
		else
		{
			if(subscriber.isValidPrefix())
				status = subscriber.getStatus().toUpperCase();
			else
				status = "INVALID";
		}
		logger.info("RBT::subscriber status is : " + status);		
		task.setObject(param_response,getParamAsString(PROMOTION,status,status) );
	}

}
