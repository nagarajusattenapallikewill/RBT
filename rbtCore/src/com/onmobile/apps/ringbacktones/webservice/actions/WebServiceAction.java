package com.onmobile.apps.ringbacktones.webservice.actions;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author sridhar.sindiri
 *
 */
public interface WebServiceAction 
{
	public WebServiceResponse processAction(WebServiceContext webServiceContext);
}
