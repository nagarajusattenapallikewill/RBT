package com.onmobile.apps.ringbacktones.v2.util;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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

@Service(value = BeanConstant.OPERATOR_BSNL_SOUTH)
@Scope(value = Constants.SCOPE_PROTOTYPE)
public class BsnlSouthUtility extends DefaultOperatorUtility {

	private static Set<String> cvCircleId = null;

	private static Logger logger = Logger.getLogger(BsnlSouthUtility.class);

	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;
	private static Cipher dcipher;
	private static String keyPassword = null;
	private static AlgorithmParameterSpec paramSpec = null;
	private static SecretKeySpec key = null;

	static {
		try {
			resourceBundle = ResourceBundle.getBundle("bsnl_south_config");
			keyPassword = getValueFromResourceBundle(resourceBundle, Constants.PARAM_BSNL_SOUTH_KEYPASSWORD, "");
			paramSpec = new IvParameterSpec(keyPassword.getBytes());
			key = new SecretKeySpec(keyPassword.getBytes(), "DES");
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

		StatusResponse statusResponse = null;
		try {
			String dcyQuery = decrypt(requestParamMap.get("q"));
			String dcyResponse = getResponseFromQuery(dcyQuery);
			if (dcyResponse == null) {
				statusResponse = new StatusResponse();
				statusResponse.setMessage(getValueFromResourceBundle(resourceBundle,
						Constants.PARAM_CONSENT_RETURN_FAILURE_MESSAGE, null));
				statusResponse.setStatusCode(
						getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_FAILURE_CODE, null));
			} else if (dcyResponse.equalsIgnoreCase(Constants.SUCCESS)) {
				statusResponse = new StatusResponse();
				statusResponse.setMessage(getValueFromResourceBundle(resourceBundle,
						Constants.PARAM_CONSENT_RETURN_SUCCESS_MESSAGE, null));
				statusResponse.setStatusCode(
						getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_SUCCESS_CODE, null));
			} else if (dcyResponse != null) {
				statusResponse = new StatusResponse();
				statusResponse.setMessage(getValueFromResourceBundle(resourceBundle,
						Constants.PARAM_CONSENT_RETURN_MESSAGE + dcyResponse.toLowerCase(), null));
				statusResponse.setStatusCode(
						getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_FAILURE_CODE, null));
			}
		} catch (Exception e) {
			statusResponse = new StatusResponse();
			statusResponse.setMessage(
					getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_FAILURE_MESSAGE, null));
			statusResponse.setStatusCode(
					getValueFromResourceBundle(resourceBundle, Constants.PARAM_CONSENT_RETURN_FAILURE_CODE, null));
		}

		return statusResponse;
	}

	private static String decrypt(String str) throws Exception {
		dcipher = Cipher.getInstance("DES/CFB8/NoPadding");
		dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
		byte[] utf8 = dcipher.doFinal(dec);
		return new String(utf8, "UTF8");
	}

	private String getResponseFromQuery(String dcyQuery) {
		if (dcyQuery == null) {
			return null;
		}
		for (String params : dcyQuery.split("\\&")) {
			if (params.contains("status=")) {
				return params.substring(params.indexOf("status=") + 7);
			}
		}
		return null;
	}
}
