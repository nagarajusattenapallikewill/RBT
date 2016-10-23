package com.onmobile.apps.ringbacktones.v2.controller;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.onmobile.apps.ringbacktones.v2.dto.RBTViralPromotion;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.impl.RBTViralPromotionRequestResolver;

@RestController
@RequestMapping("/viral")
public class ViralPromotionController {
	private static Logger logger = Logger.getLogger(ViralPromotionController.class);

	@RequestMapping(method = RequestMethod.POST)
	public void addRBTViralPromotionRequest(@RequestBody(required = true) RBTViralPromotion rbtViralPromotion)
			throws UserException {
		logger.info("Received request for RBTViralPromotion: " + rbtViralPromotion);
		RBTViralPromotionRequestResolver promotionRequestResolver = new RBTViralPromotionRequestResolver();
		promotionRequestResolver.sendPromotion(rbtViralPromotion);
	}

}
