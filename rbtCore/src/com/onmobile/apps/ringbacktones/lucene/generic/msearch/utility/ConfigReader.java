package com.onmobile.apps.ringbacktones.lucene.generic.msearch.utility;

import java.util.ResourceBundle;

public class ConfigReader {

	private static ResourceBundle configResourceBundle = null;
	static {
		try {
			configResourceBundle = ResourceBundle.getBundle("mSearch");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getParameter(String parameter, String defaultValue) {
		if (parameter == null)
			return defaultValue;
		String value = null;
		try {
			value = configResourceBundle.getString(parameter);
		} catch (Exception e) {
			value = defaultValue;
			e.printStackTrace();
		}
		return value;
	}

}
