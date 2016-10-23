package com.onmobile.apps.ringbacktones.daemons;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * Picks up records in RBT_CALL_BACK_EVENT and makes hit to the URL present in MESSAGE field of the record after updating the request params
 * with the required values. After the hit is made, the record is deleted from the table.
 * <br>
 * <a href=https://jira.onmobile.com/browse/RBT-10520>https://jira.onmobile.com/browse/RBT-10520</a>
 * <br>
 * <a href=https://athene.onmobile.com/display/RBT4/RBT+Ad2C+integration>https://athene.onmobile.com/display/RBT4/RBT+Ad2C+integration</a>
 * @author rony.gregory
 */
public class Ad2cDaemon extends TimerTask {

	private static final Logger logger = Logger.getLogger(Ad2cDaemon.class);

	private static final String DAEMON = "DAEMON";
	private static final String AD2C_DAEMON_INTERVAL_TIME_BETWEEN_NEXT_RUN = "AD2C_DAEMON_INTERVAL_TIME_BETWEEN_NEXT_RUN";
	private static final String AD2C_DAEMON_ATYPE_VALUE = "AD2C_DAEMON_ATYPE_VALUE";
	private static final String AD2C_DAEMON_CTYPE_VALUE = "AD2C_DAEMON_CTYPE_VALUE";

	private static enum params {		//Parameters that would be present in the ad2c url to be hit by this daemon
		atype,
		ctype,
		cpp,
		arpu
	};

	private String atypeValue;
	private String ctypeValue;

	private int intervalTime = 0;

	public Ad2cDaemon() {
		// Default interval is 1hr (60 * 60 * 1000)
		String intervalTimeStr = RBTParametersUtils.getParamAsString(DAEMON,
				AD2C_DAEMON_INTERVAL_TIME_BETWEEN_NEXT_RUN, "3600000");
		if (intervalTimeStr != null) {
			intervalTime = Integer.parseInt(intervalTimeStr);
		} else {
			logger.warn("Parameter " + AD2C_DAEMON_INTERVAL_TIME_BETWEEN_NEXT_RUN
					+ " not configured. So taking default as 1 hour");
		}

		// Default action-type considering as DWL
		atypeValue = RBTParametersUtils.getParamAsString(DAEMON,
				AD2C_DAEMON_ATYPE_VALUE, "DWL");
		// Default content-type considering as CRBT
		ctypeValue = RBTParametersUtils.getParamAsString(DAEMON,
				AD2C_DAEMON_CTYPE_VALUE, "CRBT");
	}

	public void start() {
		logger.info("Scheduling " + Ad2cDaemon.class.getSimpleName());
		Timer timer = new Timer(Ad2cDaemon.class.getSimpleName());
		// converting to milliseconds
		long intervalTimeInMilli = intervalTime * 60 * 1000;
		timer.scheduleAtFixedRate(this, 0L, intervalTimeInMilli);
		logger.info("Ad2cDaemon has been scheduled");
	}

	@Override
	public void run() {
		logger.info("Started " + Ad2cDaemon.class.getSimpleName());
		List<RBTCallBackEvent> rbtCallBackEventList = new RBTCallBackEvent()
		.getCallbackEventsOfModule(RBTCallBackEvent.MODULE_ID_AD2C,
				RBTCallBackEvent.AD2C_TO_BE_SENT);
		if (rbtCallBackEventList == null || rbtCallBackEventList.isEmpty()) {
			logger.info ("No records to process.");
		} else {
			logger.info(rbtCallBackEventList.size() + " record(s) to be processed.");
			for (RBTCallBackEvent rbtCallBackEvent : rbtCallBackEventList) {
				process(rbtCallBackEvent);
				rbtCallBackEvent.delete();
				logger.info("Deleted rbtCallBackEvent: " + rbtCallBackEvent);
			}
			logger.info("Finished procressing all records.");
		} 
	}

	/**
	 * Processes each rbtCallbackEvent.
	 * @param rbtCallBackEvent
	 */
	private void process(RBTCallBackEvent rbtCallBackEvent) {
		String ad2cUrl = getAd2cUrlWithParams(rbtCallBackEvent);
		HttpParameters httpParameters = new HttpParameters(ad2cUrl);
		HashMap<String, String> requestParams = new HashMap<String, String>();
		HttpResponse httpResponse = null;
		try {
			httpResponse = RBTHttpClient.makeRequestByPost(httpParameters, requestParams, null);
			RBTEventLogger.logEvent(RBTEventLogger.Event.AD2C_TRANS_LOG, ad2cUrl + "|" + httpResponse.getResponseCode() + "|" + httpResponse.getResponse());
		} catch (HttpException e) {
			logger.error("HttpException in making request: " + ad2cUrl, e);
			RBTEventLogger.logEvent(RBTEventLogger.Event.AD2C_TRANS_LOG, ad2cUrl + "|" + e.getMessage());
		} catch (IOException e) {
			logger.error("IOException in making request: " + ad2cUrl, e);
			RBTEventLogger.logEvent(RBTEventLogger.Event.AD2C_TRANS_LOG, ad2cUrl + "|" + e.getMessage());
		}  catch (Exception e) {
			logger.error("Exception in making request: " + ad2cUrl, e);
			RBTEventLogger.logEvent(RBTEventLogger.Event.AD2C_TRANS_LOG, ad2cUrl + "|" + e.getMessage());
		}
		logger.info("Redirection response received. httpResponse: " + httpResponse);
	}
	/**
	 * 
	 * @param rbtCallBackEvent
	 * @return Returns ad2c url with the requestParams set with the required values.
	 */
	private String getAd2cUrlWithParams(RBTCallBackEvent rbtCallBackEvent) {
		String ad2cUrl = rbtCallBackEvent.getMessage();
		try {
			String price = rbtCallBackEvent.getClassType().split("=")[1];
			String tokens[] = ad2cUrl.split("\\?");
			String requestUrl = tokens[0];
			String queryString = tokens[1];
			String[] reqParams = queryString.split("&");
			String finalQueryString = "";
			for (String reqParam : reqParams) {
				String paramValuePair[] = null;
				if (reqParam.indexOf("=") != -1) {
					paramValuePair = reqParam.split("=");
				} else {
					paramValuePair = new String[1];
					paramValuePair[0] = reqParam;
				}
				if (paramValuePair[0].trim().equals(params.atype.name())) {
					finalQueryString = finalQueryString + "&" + params.atype.name() + "=" + atypeValue;
				} else if (paramValuePair[0].trim().equals(params.ctype.name())) {
					finalQueryString = finalQueryString + "&" + params.ctype.name() + "=" + ctypeValue;
				} else if (paramValuePair[0].trim().equals(params.cpp.name())) {
					finalQueryString = finalQueryString + "&" + params.cpp.name() + "=" + price;
				} else if (paramValuePair[0].trim().equals(params.arpu.name())) {
					finalQueryString = finalQueryString + "&" + params.arpu.name() + "=" + price;
				} else {
					finalQueryString = finalQueryString + "&" + reqParam;
				}
			}
			ad2cUrl = requestUrl + "?" + finalQueryString;
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("Invalid ad2cUrl: " + ad2cUrl + " or invalid classType: " + rbtCallBackEvent.getClassType(), e);
		}
		return ad2cUrl;
	}

	public void stop() {
		this.cancel();
	}

	public static void main(String[] args) {
		new Ad2cDaemon().start();
	}
}