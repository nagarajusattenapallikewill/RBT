package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.dto.SelRequestDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.handler.IComboReqHandler;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

/**
 * 
 * @author koyel.mahata
 *
 */

public class ComboRequestResolver extends AbstractComboRequestResolver {
	
	@Autowired
	@Qualifier(value = BeanConstant.SUB_PURCHASE_SET_COMBO_REQUEST_HANDLER)
	private IComboReqHandler comboReqHandler;
	@Autowired
	private ApplicationContext applicationContext;
	//@Autowired
	//private StoreIdModeMappingBean storeIdModeMapping;
	private Set<String> operatorsAllowed;
	private static Logger logger = Logger.getLogger(ComboRequestResolver.class);
	
	@Override
	public PurchaseCombo processComboReq(String msisdn, PurchaseCombo purchaseCombo, String mode) throws UserException {
		logger.info("Processing Combo Request");
		String firstName = null;
		String lastName = null;
		Asset asset = purchaseCombo.getAsset();
		if(asset == null)
			ServiceUtil.throwCustomUserException(errorCodeMapping, ASSET_IS_REQUIRED, COMBO_REQUEST_MESSAGE_FOR);
		
		if(purchaseCombo.getPurchase() == null)
			ServiceUtil.throwCustomUserException(errorCodeMapping, PURCHASE_IS_REQUIRED, COMBO_REQUEST_MESSAGE_FOR);
		
		logger.info("Going to hit RBTClient to get Subscriber Detail");
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(new RbtDetailsRequest(msisdn));
		
		PlayRule playRule = purchaseCombo.getPlayrule();
		if(playRule!=null && playRule.getCallingparty()!=null){
			firstName = playRule.getCallingparty().getFirstname();
			lastName =  playRule.getCallingparty().getLastname();
		}
		
		String callerId = getCallerId(playRule);
		SelRequestDTO selRequestDTO = new SelRequestDTO(playRule, subscriber, mode, callerId, subscriber.getSubscriberID(),firstName,lastName);
		selRequestDTO.setSubscription(purchaseCombo.getSubscription());
		selRequestDTO.setAsset(asset);
		
		// added for RBT-18005	Combo api for ephermal all caller selection->Selection is allowed for FREE pack user
		validateEphemeralSelectionForCaller(playRule);
		SelectionRequest selectionRequest = getSelectionRequest(selRequestDTO);
		comboReqHandler.applyRequest(subscriber, purchaseCombo, selectionRequest);
		
		return purchaseCombo;
	}

	public void setOperatorsAllowed(Set<String> operatorsAllowed) {
		this.operatorsAllowed = operatorsAllowed;
	}

}
