package com.onmobile.apps.ringbacktones.rbt2.service;

import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NextServiceCharge;

public interface  INextChargeClassService {
	
	public NextServiceCharge getNextChargeAndServiceClass(String subscriberId,
			String chargeClass, String categoryID, String clipID,
			String subscriptionClass,  String mode) throws UserException;
	
	
}
