/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.airtel;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

/**
 * @author vinayasimha.patil
 *
 */
public class AirtelUSSDRBTProcessor extends AirtelRBTProcessor
{
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.airtel.AirtelRBTProcessor#processActivation(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processActivation(WebServiceContext task)
	{
		String subscriberID = task.getString(param_subscriberID);
		if (task.containsKey(param_rentalPack))
		{
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (!rbtDBManager.isSubscriberDeactivated(subscriber))
			{
				if (Utility.isAdvanceRentalPack(subscriber.subscriptionClass()))
					return NOT_ALLOWED;

				SubscriberStatus[] settings = rbtDBManager.getAllActiveSubscriberSettings(subscriberID);
				if (settings != null)
				{
					for (SubscriberStatus setting : settings)
					{
						if (setting.callerID() == null && setting.status() == 1 && setting.categoryType() == iRBTConstant.SHUFFLE)
							return NOT_ALLOWED;
					}
				}
			}
		}

		String response = super.processActivation(task);

		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.airtel.AirtelRBTProcessor#processSelection(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processSelection(WebServiceContext task)
	{
		String subscriberID = task.getString(param_subscriberID);
		String callerID = (!task.containsKey(param_callerID) || 
				task.getString(param_callerID).equalsIgnoreCase(ALL)) ? null
						: task.getString(param_callerID);

		if (callerID == null)
		{
			SubscriberStatus[] settings = rbtDBManager.getAllActiveSubscriberSettings(subscriberID);

			if (settings != null)
			{
				for (SubscriberStatus setting : settings)
				{
					// If user has Album selection, then user not allowed to make ALL caller selection
					if (setting.callerID() == null && setting.status() == 1
							&& setting.categoryType() == iRBTConstant.SHUFFLE)
						return NOT_ALLOWED;
				}
			}
		}

		String response = super.processSelection(task);
		if (response.equalsIgnoreCase(SELECTION_OVERLIMIT)
				|| response.equalsIgnoreCase(PERSONAL_SELECTION_OVERLIMIT)
				|| response.equalsIgnoreCase(LOOP_SELECTION_OVERLIMIT))
			return NOT_ALLOWED;

		return response;
	}
}
