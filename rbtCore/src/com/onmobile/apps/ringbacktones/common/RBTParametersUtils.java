package com.onmobile.apps.ringbacktones.common;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;

public class RBTParametersUtils {

	private static Logger logger = Logger.getLogger(RBTParametersUtils.class);
	private static ParametersCacheManager parametersCacheManager = CacheManagerUtil.getParametersCacheManager();

	private RBTParametersUtils() {
	}

	public static String getParamAsString(String type, String param, String defaultVal) {
		try {
			return parametersCacheManager.getParameter(type, param, defaultVal).getValue();
		}
		catch (Exception e) {
			logger.warn("Unable to get param: " + param + " type: " + type);
			return defaultVal;
		}
	}

	public static boolean getParamAsBoolean(String type, String param, String defaultVal) {
		try {
			return parametersCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
		}
		catch (Exception e) {
			logger.warn("Unable to get param: " + param + " type: " + type);
			return "TRUE".equalsIgnoreCase(defaultVal);
		}
	}

	public static int getParamAsInt(String type, String param, int defaultVal) {
		try {
			String paramVal = parametersCacheManager.getParameter(type, param, String.valueOf(defaultVal)).getValue();
			return Integer.parseInt(paramVal);
		}
		catch (Exception e) {
			logger.warn("Unable to get param: " + param + " type: " + type);
			return defaultVal;
		}
	}

	public static long getParamAsLong(String type, String param, long defaultVal) {
		try {
			String paramVal = parametersCacheManager.getParameter(type, param, String.valueOf(defaultVal)).getValue();
			return Long.parseLong(paramVal);
		}
		catch (Exception e) {
			logger.warn("Unable to get param: " + param + " type: " + type);
			return defaultVal;
		}
	}

	public static boolean addParameter(String type, String paramName, String value, String paramInfo) {
		boolean inserted = false;
		try {
			inserted = parametersCacheManager.addParameter(type, paramName, value, paramInfo);
		}
		catch (Exception e) {
			logger.warn("Unable to insert param: " + paramName + " type: " + type);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Added parameter. param: " + paramName + " type: " + type + ", status: " + inserted);
		}
		return inserted;
	}

	public static boolean updateParameter(String type, String paramName, String value, String paramInfo) {
		boolean inserted = false;
		try {
			inserted = parametersCacheManager.updateParameter(type, paramName, value, paramInfo);
		}
		catch (Exception e) {
			logger.warn("Unable to insert param: " + paramName + " type: " + type);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Added parameter. param: " + paramName + " type: " + type + ", status: " + inserted);
		}
		return inserted;
	}

	public static boolean deleteParameter(String type, String paramName) {
		boolean removed = false;
		try {
			removed = parametersCacheManager.removeParameter(type, paramName);
		}
		catch (Exception e) {
			logger.warn("Unable to delete param: " + paramName + " type: " + type);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Removed parameter. param: " + paramName + " type: " + type + ", status: " + removed);
		}
		return removed;
	}

	/**
	 * Extract and return MSISDN from |CP:PRESSSTAR-9886010929:CP|
	 * 
	 * @param selectionInfo
	 * @return MSISDN in string format
	 */
	public static String getMsisdnFromSelectionInfo(String selectionInfo) {
		logger.debug("Fetching msisdn from selection info. selectionInfo: " + selectionInfo);
		if (selectionInfo.indexOf("|CP:") > -1 && selectionInfo.indexOf(":CP|") > -1) {
			int stIndex = selectionInfo.indexOf("-");
			int endIndex = selectionInfo.lastIndexOf(":");
			if (stIndex > -1 && endIndex > -1) {
				String msisdn = selectionInfo.substring(stIndex + 1, endIndex);
				logger.info("Found msisdn from selection info. msisdn: " + msisdn);
				return msisdn;
			}
		}
		logger.info("Not found msisdn from selection info: " + selectionInfo + " returning null");
		return null;
	}
}