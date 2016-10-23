package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.warid;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class WaridPromoProcessor extends PromoProcessor{
	
	/**
	 * @throws RBTException
	 */
	public WaridPromoProcessor() throws RBTException
	{
		super();
	}
	
//	private String getParamAsString(String type,String param, String defaultVal) {
//		try {
//			return parameterCacheManager.getParameter(type,
//					param, defaultVal).getValue();
//		} catch (Exception e) {
//			logger.info("getParameterAsBoolean unable to get param ->" + param
//							+ " returning defaultVal >" + defaultVal);
//			return defaultVal;
//		}
//	}
	
	public void processSubStatusRequest(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = null;
		if(subscriber == null)
		{
			status = "INVALID";
		}
		else
		{
			if(subscriber.isValidPrefix())
				status = subscriber.getStatus().toUpperCase();
			else
				status = "INVALID";
		}
		logger.info("RBT::subscriber status is : " + status);
		
		
			task.setObject(param_response,getParamAsString(PROMOTION,status,"ERROR") );

	}
	}
	
	
