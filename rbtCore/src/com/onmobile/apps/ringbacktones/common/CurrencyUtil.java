package com.onmobile.apps.ringbacktones.common;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;

public class CurrencyUtil
{
	static NumberFormat defaultCurrencyFormatter = null;
	static Logger logger = Logger.getLogger(CurrencyUtil.class);
	
	static 
	{
		String localeLangStr = "en";
		String localeCountryStr = "IN";
		localeLangStr = RBTParametersUtils.getParamAsString("COMMON", "LOCALE_LANG", localeLangStr);
		localeCountryStr = RBTParametersUtils.getParamAsString("COMMON", "LOCALE_COUNTRY", localeCountryStr);
		// Start:RBT-12510 TEFCZ - Disabling Content Currency in Press * to Copy
		// SMS Get the available locale and compare with configured one
		// if it matches then get the currency object else
		// return the null value.
		Locale[] availabelLocales = Locale.getAvailableLocales();
		Locale locale = new Locale(localeLangStr, localeCountryStr);
		for (Locale configuredLocale : availabelLocales) {
			if (configuredLocale.getLanguage().equalsIgnoreCase(
					locale.getLanguage())
					|| configuredLocale.getCountry().equalsIgnoreCase(
							locale.getCountry())) {
				defaultCurrencyFormatter = NumberFormat.getCurrencyInstance(locale);
				logger.info("localeLangStr=" + localeLangStr
						+ ", localeCountryStr=" + localeCountryStr
						+ ", locale=" + locale + " is available in the list");
				break;
			}
		}
		//End:RBT-12510 
		logger.info("localeLangStr="+localeLangStr+", localeCountryStr="+localeCountryStr+", locale="+locale+", defaultCurrenctFormatter="+defaultCurrencyFormatter);
	}
	
	
	static public String getFormattedCurrency(Locale currentLocale, double value)
	{
		NumberFormat currencyFormatter;
        if(currentLocale != null)
    	  currencyFormatter = NumberFormat.getCurrencyInstance(currentLocale);
        else
	      currencyFormatter = defaultCurrencyFormatter;
		// RBT-12510 TEFCZ - Disabling Content Currency in Press * to Copy SMS
		// null pointer validation if the currency object is null
		// then the amount will not be formatted it will return the same value.
		String currencyOut = String.valueOf(value);
		if (null != currencyFormatter) {
			currencyOut = currencyFormatter.format(value);
		} else {
			logger.debug("Configured locale is not in available locale list"
					+ "so it will return the same format");
		}
        
        if(logger.isDebugEnabled()) {
        	logger.debug("Input currency as double: " + value + " currencyOut: "+currencyOut);
        }
        return currencyOut;
    }
	
	static public String getFormattedCurrency(Locale currentLocale, String value) {
		NumberFormat currencyFormatter;
		if (currentLocale != null)
			currencyFormatter = NumberFormat.getCurrencyInstance(currentLocale);
		else
			currencyFormatter = defaultCurrencyFormatter;
		// RBT-12510 TEFCZ - Disabling Content Currency in Press * to Copy SMS
		// null pointer validation if the currency object is null
		// then the amount will not be formatted it will return the same value.
		String currencyOut = value;
		if (null != currencyFormatter) {
			String specialAmtChar = CacheManagerUtil
					.getParametersCacheManager().getParameterValue(
							iRBTConstant.COMMON,
							"SPECIAL_CHAR_CONF_FOR_AMOUNT", ".");
			double amount = Double.parseDouble(value.replace(specialAmtChar,
					"."));
			currencyOut = currencyFormatter.format(amount);
		} else {
			logger.debug("Configured locale is not in available locale list"
					+ "so it will return the same format");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Input currency as String: " + value
					+ " currencyOut: " + currencyOut);
		}
		return currencyOut;
	}

	static public String getFormattedCurrency(Locale currentLocale, long value)
	{
		NumberFormat currencyFormatter;
        if(currentLocale != null)
    	  currencyFormatter = NumberFormat.getCurrencyInstance(currentLocale);
      else
	      currencyFormatter = defaultCurrencyFormatter;	  
		// RBT-12510 TEFCZ - Disabling Content Currency in Press * to Copy SMS
		// null pointer validation if the currency object is null
		// then the amount will not be formatted it will return the same value.
		String currencyOut = String.valueOf(value);
		if (null != currencyFormatter) {
			currencyOut = currencyFormatter.format(value);
		} else {
			logger.debug("Configured locale is not in available locale list "
					+ "so it will return the same format");
		}
		
        if(logger.isDebugEnabled()) {
        	logger.debug("Input currency as long: " + value + " currencyOut: "+currencyOut);
        }
        return currencyOut;
    }
}
