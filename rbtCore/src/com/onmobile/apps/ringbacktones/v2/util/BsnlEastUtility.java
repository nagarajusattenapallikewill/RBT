package com.onmobile.apps.ringbacktones.v2.util;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;

@Service(value = BeanConstant.OPERATOR_BSNL_EAST)
@Scope(value = Constants.SCOPE_PROTOTYPE)
public class BsnlEastUtility extends DefaultOperatorUtility {

	private static Set<String> cvCircleId = null;

	private static Logger logger = Logger.getLogger(BsnlEastUtility.class);

	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;
	
	static {
		try {
			resourceBundle = ResourceBundle.getBundle("bsnl_east_config");
			initilizeCVCircle(cvCircleId);

		} catch (MissingResourceException e) {
			logger.error("Exception Occured: " + e, e);
		}
	}

	public Set<String> getCvCircleId() {
		return cvCircleId;
	}

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
	
	
	@Override
	public StatusResponse getStatusResponse(String circleId, String callerId, Map<String, String> requestParamMap,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
			
		StatusResponse statusResponse = new StatusResponse();
		statusResponse.setMessage(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_SUCCESS_MESSAGE, null));
		statusResponse.setStatusCode(getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_SUCCESS_CODE, null));
		return statusResponse;
	}
	
}
