package com.onmobile.apps.ringbacktones.rbt2.processor;

import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NextServiceCharge;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public interface INextChargeClassProcessor {
	public NextServiceCharge getNextChargeAndServiceClass(WebServiceContext webServiceContext) throws UserException;

}
