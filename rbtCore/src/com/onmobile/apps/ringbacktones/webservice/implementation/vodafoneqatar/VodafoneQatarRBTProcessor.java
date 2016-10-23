/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.vodafoneqatar;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author sahidul.karim
 *
 */
public class VodafoneQatarRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(VodafoneQatarRBTProcessor.class);

	@Override
	public String processSelection(WebServiceContext task) {
		logger.info("task object"+task); 
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		if(subscriber == null || subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_DEACTIVATED)) {
			int selType = -1;
			if (task.containsKey(param_selectionType)) {
				String strSelType = task.getString(param_selectionType);
				try {
					selType = Integer.parseInt(strSelType);
				} catch (NumberFormatException ne) {
				}
			}
			int status = 1;
			if (task.containsKey(param_status)) {
				status = Integer.parseInt(task.getString(param_status));
			}	
			if(task.containsKey(param_cricketPack) || task.containsKey(param_profileHours)
			  || selType == iRBTConstant.PROFILE_SEL_TYPE || status == iRBTConstant.PROFILE_SEL_TYPE) {
				return NOT_ALLOWED;
			}
		}
		return super.processSelection(task);
	}

	
}
