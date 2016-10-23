package com.onmobile.apps.ringbacktones.daemons.inline;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public interface IProvisioning {

	public void provision(Object obj, WebServiceContext parameters) throws Throwable;
}
