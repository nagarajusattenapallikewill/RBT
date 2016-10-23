package com.onmobile.android.utils.comparator;

import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.log4j.Logger;

import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.Utility;

public class ComparatorUtility {

	private static Logger logger = Logger.getLogger(ComparatorUtility.class);

	public static Locale getDefaultLocale () {
		return new Locale("en","US");
	}
	
	public static boolean isLocaleValid(Locale locale) {
		if (locale == null) {
			return false;
		}
		try {
			return locale.getISO3Language() != null && locale.getISO3Country() != null;
		} catch (MissingResourceException e) {
			return false;
		}
	}

	public static Locale getLocaleFromConfig() {
		Locale locale = null;
		String param = PropertyConfigurator.getLocaleConfig();
		if (Utility.isStringValid(param)) {
			String[] parts = param.split("_");
			switch (parts.length) {
			case 3:
				locale = new Locale(parts[0], parts[1], parts[2]);
				break;
			case 2:
				locale = new Locale(parts[0], parts[1]);
				break;
			case 1:
				locale = new Locale(parts[0]);
				break;
			default:
				logger.error("Invalid locale value!");
			}
			logger.info("Locale: " + locale);
		}
		return locale;
	}
}
