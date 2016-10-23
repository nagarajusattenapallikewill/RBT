package com.onmobile.apps.ringbacktones.webservice.actions;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;

/**
 * @author sridhar.sindiri
 *
 */
public abstract class AbstractUserPINAction implements WebServiceAction, WebServiceConstants
{

	/**
	 * @param webServiceContext
	 * @throws WebServiceException
	 */
	protected void validateParameters(WebServiceContext webServiceContext) throws WebServiceException
	{
		String userID = webServiceContext.getString(param_userID);
		String type = webServiceContext.getString(param_type);
		if (userID == null || type == null)
		{
			throw new WebServiceException("userID or type parameter is missing", 0, INVALID_PARAMETER);
		}
	}
}
