package com.onmobile.apps.ringbacktones.daemons;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.webservice.bean.LoggerBean;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class AdPartnerRequestDeamon implements Callable<String>, iRBTConstant {

	private TransData transData;
	Map<String, String> adPartnerUrlMap = new HashMap<String, String>();
	private RBTHttpClient rbtHttpClient = null;
	static Logger logger = Logger.getLogger(AdPartnerRequestDeamon.class);
	private static Logger cdr_logger = Logger
			.getLogger("AD_PARTNER_CALL_BACK_CDR_LOGGER");

	public AdPartnerRequestDeamon(TransData transData,
			RBTHttpClient rbtHttpClient, Map<String, String> adPartnerUrlMap) {
		super();
		this.transData = transData;
		this.adPartnerUrlMap = adPartnerUrlMap;
		this.rbtHttpClient = rbtHttpClient;
	}

	@Override
	public String call() throws Exception {
		processAdPartenerCallBack(transData);
		return "DONE";
	}

	private void processAdPartenerCallBack(TransData transData) {
		if (transData != null) {
			String adPartnerType = transData.type();
			String url = null;
			String responseStr = null;
			url = adPartnerUrlMap.get(adPartnerType);
			logger.info("ad partner type :-->" + adPartnerType + ",url " + url);
			if (null != url) {
				LoggerBean loggerBean = new LoggerBean();
				url = url.replace("%TRANS_ID%", transData.transID());
				url = url.replace("%MSISDN%", transData.subscriberID());
				logger.info("ad partner url :-->" + url);
				loggerBean.setRequestSentDate(Calendar.getInstance());
				HttpResponse httpResponse = null;
				try {
					httpResponse = rbtHttpClient.makeRequestByGet(url, null);
				} catch (HttpException e) {
					logger.error("Faild to make HTTP call to Ad partner, "
							+ "Exception: " + e.getMessage(), e);
					responseStr = e.getMessage();
					if (responseStr == null) {
						responseStr = "Exception";
					}
				} catch (Exception e) {
					logger.error("ad partner url IO Exception:---> " + e);
					responseStr = "Exception";
				}
				logger.info("httpResponse : " + httpResponse);
				loggerBean.setSubscriberId(transData.subscriberID());
				loggerBean.setRequestUrl(url);
				loggerBean
						.setTimeTaken(""
								+ (Calendar.getInstance().getTimeInMillis() - loggerBean
										.getRequestSentDate().getTimeInMillis()));
				if (httpResponse != null && httpResponse.getResponse() != null) {
					responseStr = httpResponse.getResponse().trim();
					loggerBean.setResponseCode(httpResponse.getResponseCode());
				}
				loggerBean.setResponse(responseStr);
				logger.info("Response from the url : " + responseStr);
				writeSatPushCDRLog(loggerBean);
			}
		}

	}

	public static void writeSatPushCDRLog(LoggerBean loggerBean) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					iRBTConstant.kDateFormatwithTime);
			String currentTime = dateFormat.format(loggerBean
					.getRequestSentDate().getTime());
			cdr_logger.info(currentTime + ", " + loggerBean.getSubscriberId()
					+ ", " + (loggerBean.getRequestUrl()) + ", "
					+ loggerBean.getResponse() + ", "
					+ loggerBean.getResponseCode() + ", "
					+ loggerBean.getTimeTaken());
		} catch (Exception e) {
			logger.info("Exception occured while writing satPush CDR logs." + e);
		}
	}
}
