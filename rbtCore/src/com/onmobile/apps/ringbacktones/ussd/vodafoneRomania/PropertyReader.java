package com.onmobile.apps.ringbacktones.ussd.vodafoneRomania;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

public class PropertyReader {
	private static Logger logger = Logger.getLogger(PropertyReader.class);
	private ResourceBundle bundle = null;
	public PropertyReader() {
		try {
			bundle = ResourceBundle.getBundle("ApplicationResources");
		} catch (MissingResourceException e) {
			logger.error("",e);
			e.printStackTrace();
		}
	}

	public String getPropertyValue(String propertyName) {
		String propertyValue = null;
		try {
			propertyValue = bundle.getString(propertyName);
		} catch (MissingResourceException e) {
			logger.error("",e);
			e.printStackTrace();
		}
		return propertyValue;
	}

//	public static void main(String[] args) {
//		PropertyReader pr = new PropertyReader();
//		String propertyName = "set.futuredate.value";
//		System.out.println(pr.getPropertyValue(propertyName));
//	}
}
