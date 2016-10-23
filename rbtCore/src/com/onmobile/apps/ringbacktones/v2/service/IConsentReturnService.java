package com.onmobile.apps.ringbacktones.v2.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

/**
 * 
 * @author md.alam
 *
 */
 
public interface IConsentReturnService {
	
	public StatusResponse getStatusResponse(String operatorName, String circleId, String callerId, HttpServletRequest request,HttpServletResponse response) throws UserException;

}
