package com.onmobile.apps.ringbacktones.v2.resolver.request.handler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.ThirdPartyConsent;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

/**
 * 
 * @author md.alam
 *
 */
 
@Service(value = BeanConstant.SUB_PURCHASE_COMBO_REQUEST_HANDLER)
public class SubsriptionPurchaseComboReqHandler extends AbstractComboReqHandler {

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	@Qualifier(value = BeanConstant.PURCHASE_SET_COMBO_REQUEST_HANDLER)
	private IComboReqHandler comboReqHandler;
	private static Logger logger = Logger.getLogger(SubsriptionPurchaseComboReqHandler.class);
	
	@Override
	public PurchaseCombo applyRequest(Subscriber subscriber, PurchaseCombo purchaseCombo, SelectionRequest selectionRequest) throws UserException {
		
		logger.info("Applying Combo Request for Subscription and Purchase");
		
		if(purchaseCombo.getSubscription() != null && purchaseCombo.getPurchase() != null) {
			String operatorName=ServiceUtil.getOperatorName(subscriber);
			
			AbstractOperatorUtility operatorUtility = (AbstractOperatorUtility) getOperatorUtilityObject(operatorName);
			Rbt	rbt = makePurchase(operatorUtility, selectionRequest);
			responseValidation(selectionRequest.getResponse());
		//	selectionRequest.SUBC
			String cgUrl = makeConsentCGUrl(operatorUtility, rbt, selectionRequest, operatorName, subscriber);
			String rUrl = makeRUrl(operatorUtility);
			
			ThirdPartyConsent thirdPartyConsent = buildThirdPartyConsent(operatorUtility, cgUrl, rUrl);
			if(thirdPartyConsent != null)			
				purchaseCombo.setThirdpartyconsent(thirdPartyConsent);
			
		} else {
			logger.info("Not a combo request for Subscription and Purchase");
			comboReqHandler.applyRequest(subscriber, purchaseCombo, selectionRequest);
		}
		return purchaseCombo;		
	}

}
