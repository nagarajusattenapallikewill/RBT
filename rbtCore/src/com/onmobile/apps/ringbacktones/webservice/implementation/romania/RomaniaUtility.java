/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.romania;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 *
 */
public class RomaniaUtility implements WebServiceConstants
{
	public static boolean isSetForAll(SubscriberStatus[] settings, String rbtFile)
	{
		if (settings == null || settings.length == 0)
			return false;

		for (SubscriberStatus setting : settings)
		{
			if (setting.subscriberFile().equalsIgnoreCase(rbtFile) && setting.callerID() == null
					&& setting.status() == 1)
				return true;
		}

		return false;
	}
}
