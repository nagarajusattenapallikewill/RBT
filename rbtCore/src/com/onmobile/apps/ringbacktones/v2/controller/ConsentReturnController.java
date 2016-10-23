package com.onmobile.apps.ringbacktones.v2.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.v2.service.IConsentReturnService;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

/**
 * 
 * @author md.alam
 * @modified vikrant
 *
 */

@RestController
@RequestMapping(value = "/thirdparty/consent")
public class ConsentReturnController {
	
	@Autowired
	private IConsentReturnService consentReturnService;
	
	private static Logger logger = Logger.getLogger(ConsentReturnController.class);
	
	
	@RequestMapping(value = "/callback/{operatorName}")
	public StatusResponse consentReturn(@PathVariable(value = "operatorName") String operatorName,
			HttpServletRequest request,HttpServletResponse response) throws UserException {
		
		String circleId = null;
		String callerId = null;
		
		return consentReturnService.getStatusResponse(operatorName,circleId, callerId, request,response);
	}
	
	
	//Below Mapping is for Airtel-comviva
	@RequestMapping(value = "/callback/{operatorName}/{circleId}/{callerId}")
	public StatusResponse consentReturn(@PathVariable(value = "operatorName") String operatorName,
			@PathVariable(value = "circleId") String circleId,
			@PathVariable(value = "callerId") String callerId, 
			HttpServletRequest request,HttpServletResponse response) throws UserException {
		
		return consentReturnService.getStatusResponse(operatorName,circleId, callerId, request,response);
	}

}
