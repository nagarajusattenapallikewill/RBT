package com.onmobile.apps.ringbacktones.rbt2.service.util;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;

@Component(value = BeanConstant.PROPERTY_CONFIG)
public class PropertyConfig {
	
	private ResourceBundle resourceBundle = null;
	private static Logger logger = Logger.getLogger(PropertyConfig.class);
	
	
	public PropertyConfig() {
		resourceBundle = loadBundle("thirdPartyUrl");
	}
	
	
	public String getValueFromResourceBundle(String key) {
		String value = null;

		try {
			value = resourceBundle.getString(key).trim();
		} catch (MissingResourceException e) {
			logger.info("RBT:: " + e.getMessage());
		}

		return value;
	}
	
	public ResourceBundle loadBundle(String baseName) {
		ResourceBundle resourceBundle = null;
		try {
			resourceBundle = ResourceBundle.getBundle(baseName);
		} catch(MissingResourceException e) {
			logger.error("Exception Occured: "+e,e);
		}
		return resourceBundle;
	}
	
	public String getValueFromResourceBundle(ResourceBundle resourceBundle, String key, String defaultValue) {
		if(resourceBundle == null) {
			logger.info("ResourceBundle is null, so returning default value: "+defaultValue);
			return defaultValue;
		}
		String value = null;

		try {
			value = resourceBundle.getString(key).trim();
		} catch (MissingResourceException e) {
			logger.error("RBT:: " + e.getMessage());
			value = defaultValue;
		}

		return value;
	}

}
