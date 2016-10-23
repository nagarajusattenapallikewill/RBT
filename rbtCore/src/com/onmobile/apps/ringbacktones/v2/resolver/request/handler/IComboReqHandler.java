package com.onmobile.apps.ringbacktones.v2.resolver.request.handler;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

/**
 * 
 * @author md.alam
 *
 */
 
public interface IComboReqHandler {
	
	public PurchaseCombo applyRequest(Subscriber subscriber, PurchaseCombo purchaseCombo, SelectionRequest selectionRequest) throws UserException;

}