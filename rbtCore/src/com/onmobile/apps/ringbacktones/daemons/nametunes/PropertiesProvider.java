package com.onmobile.apps.ringbacktones.daemons.nametunes;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * <p>This is a common file to read any property file
 * </p>
 * 
 * @author RAMRESWARA REDDY
 * 
 */
public class PropertiesProvider {

	/**
	 * Preparing logger object
	 */
	
	private static Logger logger = Logger.getLogger(PropertiesProvider.class);
	private static Resource resource;
	private static Properties properties;
	

	public PropertiesProvider(String propFile) {
		try {
			resource = new ClassPathResource(propFile);
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException exception) {
			logger.error("Unable to load properties file:"+propFile+" , Exception Message:"+exception.getMessage()
					+ ExceptionUtils.getFullStackTrace(exception));
		}
	}

	public static Properties readAllProperties() {
		return properties;
	}

	public static String getPropertyValue(String key) {
		String value = null;
		if (key != null) {
			key = key.trim();
		}
		value = properties.getProperty(key);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

	public static Integer getPropIntValue(String key) {
		String strVal=null;
		Integer intVal=null;
		try {
			strVal=getPropertyValue(key);
			if(strVal!=null)
			intVal = Integer.parseInt(strVal);
		} catch (NumberFormatException e) {
			logger.error("UNABLE TO PARSE STRING TO INTEGER:"+strVal);
			return 0;
		}
		return intVal;
	}

	
	public static boolean getPropBooleanValue(String key) {
		String strVal=null;
		Boolean boolVal = null;
		try {
			strVal = getPropertyValue(key);
			if(strVal!=null)
				boolVal = new Boolean(strVal);
		} catch (Exception e) {
			logger.error("UNABLE TO PARSE STRING TO BOOLEAN :"+strVal);
		}
		return boolVal;
	}

}
