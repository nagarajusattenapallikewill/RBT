package com.onmobile.apps.ringbacktones.rbtcontents.v2.utils;

import org.apache.log4j.Logger;

public class UtilsFactory {
	private static Logger logger = Logger.getLogger(UtilsFactory.class);
	private static String responseHandlerClassStr;
	private static String defaultResponseHandler = "com.onmobile.apps.ringbacktones.rbtcontents.v2.utils.implementation.MultipleTPHitUtilsImpl";
	private static Class<?> responseHandlerClass = null;
	private static ITPHitUtils responseHandler;

	public ITPHitUtils getTPHitUtils(String source) {
		if (source == null) {
			return null;
		}
		responseHandlerClassStr = source;

		if (null == responseHandlerClassStr || responseHandlerClassStr.trim().length() == 0) {
			responseHandlerClassStr = defaultResponseHandler;
		}
		try {

			responseHandlerClass = Class.forName(responseHandlerClassStr);
			responseHandler = (ITPHitUtils) responseHandlerClass.newInstance();

		} catch (Exception e) {
			logger.error("Exception while getting TP response class: ", e);
			return null;
		}
		return responseHandler;

	}

}
