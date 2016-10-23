package com.onmobile.apps.ringbacktones.v2.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbt2.bean.ConsentProcessBean;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;


public abstract class AbstractOperatorUtility implements IOperatorUtility {
	
	private static Logger logger = Logger.getLogger(AbstractOperatorUtility.class);
	protected String operatorName;
	protected ConsentProcessBean consentProcessBean;
	protected String transId;
	
	
	
	protected  SimpleDateFormat getCGUrlDateFormat() {
		String timeStampFormat = /*PropertyConfigurator.getConsentURLTimeStampFormat()*/ "";
		String defaultTimestampFormat = "yyyyMMddHHmmSS";
		SimpleDateFormat dateFormat = null;
		if (!ServiceUtil.isStringValid(timeStampFormat)) {
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
		String timeZone = /*PropertyConfigurator.getConsentURLTimeStampTimeZone()*/ "";
		if (!ServiceUtil.isStringValid(timeZone)) {
			logger.debug("CGUrl timestamp timezone not configured or is empty. Using default timezone.");
		} else {
			TimeZone timeZoneObj = TimeZone.getTimeZone(timeZone);
			logger.debug("timeZoneObj: " + timeZoneObj);
			dateFormat.setTimeZone(timeZoneObj);
		}
		return dateFormat;
	}
	
	protected String getConsentReqTime() {
		Date currentDate = new Date();
		logger.info("CURRENT DATE IS:" + currentDate);
		SimpleDateFormat  sdf = getCGUrlDateFormat();
		String consenrReqTime = sdf.format(currentDate);
		logger.info("CURRENT TIME IS:" + consenrReqTime);
		return consenrReqTime;
	}
	
	protected static String getValueFromResourceBundle(ResourceBundle resourceBundle, String key, String defaultValue) {
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

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public ConsentProcessBean getConsentProcessBean() {
		return consentProcessBean;
	}

	public void setConsentProcessBean(ConsentProcessBean consentProcessBean) {
		this.consentProcessBean = consentProcessBean;
	}

	public String getTransId() {
		return transId;
	}

	public void setTransId(String transId) {
		this.transId = transId;
	}
	
}
