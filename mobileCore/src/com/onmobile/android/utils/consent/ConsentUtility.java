package com.onmobile.android.utils.consent;

import java.text.SimpleDateFormat;
	import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.ConsentProcessBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public abstract class ConsentUtility {

	private static Logger logger = Logger.getLogger(ConsentUtility.class);
	
	public abstract Rbt addSubscriberConsentSelection(SelectionRequest selectionRequest);
	
	public abstract String makeConsentCgUrl(ConsentProcessBean consentProcessBean);
	
	public static SimpleDateFormat getCGUrlDateFormat() {
		String timeStampFormat = PropertyConfigurator.getConsentURLTimeStampFormat();
		String defaultTimestampFormat = "yyyyMMddHHmmSS";
		SimpleDateFormat dateFormat = null;
		if (!Utility.isStringValid(timeStampFormat)) {
			logger.debug("CGUrl time stamp not configured or is empty. Using default timestamp format: " + defaultTimestampFormat);
			dateFormat = new SimpleDateFormat(defaultTimestampFormat);
		} else {
			try {
				dateFormat = new SimpleDateFormat(timeStampFormat);
			} catch (IllegalArgumentException e) {
				logger.error("Invalid Timestamp format configured: " + timeStampFormat + ". Using default time stamp format: " + defaultTimestampFormat + " " + e, e);
				dateFormat = new SimpleDateFormat(defaultTimestampFormat);
			}
		}
		String timeZone = PropertyConfigurator.getConsentURLTimeStampTimeZone();
		if (!Utility.isStringValid(timeZone)) {
			logger.debug("CGUrl timestamp timezone not configured or is empty. Using default timezone.");
		} else {
			TimeZone timeZoneObj = TimeZone.getTimeZone(timeZone);
			logger.debug("timeZoneObj: " + timeZoneObj);
			dateFormat.setTimeZone(timeZoneObj);
		}
		return dateFormat;
	}

	public String getConsentReqTime() {
		Date currentDate = new Date();
		logger.info("CURRENT DATE IS:" + currentDate);
		SimpleDateFormat  sdf = getCGUrlDateFormat();
		String consenrReqTime = sdf.format(currentDate);
		logger.info("CURRENT TIME IS:" + consenrReqTime);
		return consenrReqTime;
	}
}
