package com.onmobile.apps.ringbacktones.v2.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.IComboRequest;

/**
 * 
 * @author md.alam
 *
 */
 

@RestController
@RequestMapping(value = "/combo")
public class ComboRequestRestController {
	
	@Autowired
	private IComboRequest comboRequest;
	private static Logger logger = Logger.getLogger(ComboRequestRestController.class);
	
	
	@RequestMapping(value = "/purchase", method = RequestMethod.POST)
	public PurchaseCombo comboReqHandler(@RequestBody PurchaseCombo purchaseCombo, 
			@RequestParam(value="subscriberId", required = true) String msisdn,
			@RequestParam(value = "mode", required = true) String mode) throws UserException {
		logger.info("Combo purchase request...");
		return comboRequest.processComboReq(msisdn, purchaseCombo, mode);
		
	}

}
