package com.onmobile.apps.ringbacktones.v2.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.DefaultOperatorUtility;
import com.onmobile.apps.ringbacktones.v2.util.IOperatorUtility;

/**
 * 
 * @author md.alam
 *
 */
 
@Service(value = BeanConstant.CONSENT_RETURN_SERVICE)
public class ConsentReturnServiceImpl implements IConsentReturnService {
	
	private Logger logger = Logger.getLogger(ConsentReturnServiceImpl.class);
	
	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;

	@Override
	public StatusResponse getStatusResponse(String operatorName,
			String circleId, String callerId, HttpServletRequest request,HttpServletResponse response) throws UserException {
		Map<String, String> requestParamMap = ServiceUtil.getRequestParamsMap(request);
		IOperatorUtility consentUtility = (IOperatorUtility) getConsentObject(operatorName);
		try {
		return consentUtility.getStatusResponse(circleId, callerId, requestParamMap,request,response);
		}
		catch(Exception e) {
			ServiceUtil.throwCustomUserException(getErrorCodeMapping(), e.getMessage(), null);
			
		}
		return null; 
		
	}
	
	private IOperatorUtility getConsentObject(String operatorName) {
		
		IOperatorUtility consentUtility = null;
		
		try {
			consentUtility = (IOperatorUtility) ConfigUtil.getBean(operatorName.toLowerCase());
		} catch(NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e.getMessage()+", returning Default ConsentUtility Object");
			consentUtility = new DefaultOperatorUtility();
		}
		return consentUtility;
	}

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}

	
	
}
