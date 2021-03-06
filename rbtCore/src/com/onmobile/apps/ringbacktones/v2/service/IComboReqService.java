package com.onmobile.apps.ringbacktones.v2.service;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

/**
 * 
 * @author md.alam
 *
 */
 
public interface IComboReqService {

	
	public PurchaseCombo processComboReq(String msisdn, PurchaseCombo purchaseCombo, String mode) throws UserException;
}
