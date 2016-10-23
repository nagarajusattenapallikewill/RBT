package com.onmobile.apps.ringbacktones.freemium;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class ConfigurationParameter {

	private static ResourceBundle resourceBundle;
	private static Logger logger = Logger.getLogger(ConfigurationParameter.class);
	
	
	static {
		try {
			resourceBundle = ResourceBundle.getBundle("freemium");
		}
		catch(MissingResourceException t) {
			logger.error("Exception in loding propertied file. Please check properties file is exit in class path or not",t);
		}
	}
	
	public static String getParameterValue(String key, String defaultValue) {
		try {
			if (null == resourceBundle) {
				logger.info("resourceBundle is not there for freemium");
				return defaultValue;
			}
			return resourceBundle.getString(key);
		}
		catch(MissingResourceException e) {
			logger.error(key + " configuration not exist in properties file", e);
		}
		catch(NullPointerException e) {
			logger.error("Trying to get parameter withe null Key value", e);
		}
		return defaultValue;
	}
	
	public static String getParameterValue(String key) {
		return getParameterValue(key, null);
	}
	
}
