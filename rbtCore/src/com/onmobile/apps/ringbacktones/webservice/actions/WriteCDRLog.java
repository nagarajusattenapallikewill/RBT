package com.onmobile.apps.ringbacktones.webservice.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.bean.LoggerBean;
import com.onmobile.apps.ringbacktones.webservice.bean.SatPushLoggerBean;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * For getting next charge class for the subscriber
 * 
 * @author sridhar.sindiri
 */
public class WriteCDRLog implements WebServiceAction, WebServiceConstants {
	private static Logger logger = Logger.getLogger(WriteCDRLog.class);
	private static Logger cdr_logger = Logger.getLogger("CDR_LOGGER");

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#
	 * processAction(com.onmobile.apps.ringbacktones.webservice.common.
	 * WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		//TIMESTAMP, TRANSACTION_TYPE, CIRCLE, MISIDN, REQEUST_TYPE, REQUEST_MODE, REQUEST_ID, SERVICE_ID, SERVICE_CLASS, SUBSCRIPTION_CLASS, CHARGE_CLASS, PROMO_ID, MODE_INFO
		String response = "FALIURE";
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(iRBTConstant.kDateFormatwithTime);
			String msisdn = webServiceContext.getString(param_subscriberID);
			String requestMode = webServiceContext.getString(param_mode);
			SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(msisdn, requestMode));
			if (subscriberDetail.isValidSubscriber()) {
				String currentTime = dateFormat.format(new Date());
				String transactionType = "transaction";
				String circleID = subscriberDetail.getCircleID();
				String modeInfoParam = webServiceContext.getString(param_modeInfo);
				String modeInfo = "NA";
				String twShortCodeValue = getTwShortCodeFromMode(modeInfoParam);
				if (twShortCodeValue != null) {
					modeInfo = twShortCodeValue;
				}
				cdr_logger.info(currentTime + "," + transactionType + "," + circleID + "," + msisdn + ",," + requestMode
						+ ",,,,,,," + modeInfo);
				response = "SUCCESS";
			}
		}
		catch (Exception e) {
			logger.info("Exception occured while writing CDR log. " + e);
			response = "FALIURE";
		}
		return getWebServiceResponse(response);
	}

	/**
	 * @param response
	 * @param chargeClassesMap
	 * @return
	 */
	private WebServiceResponse getWebServiceResponse(String response) {
		Document document = Utility.getResponseDocument(response);
		WebServiceResponse webServiceResponse = Utility.getWebServiceResponseXML(document);
		logger.info("returning webservice response in getWebServiceResponse(): " + webServiceResponse);
		return webServiceResponse;
	}

	public static String getTwShortCodeFromMode(String modeinfo) {
		try {
			if (modeinfo != null && modeinfo.contains("twshortcode")) {
				modeinfo = modeinfo.substring(modeinfo.indexOf("twshortcode"));
				String modeinfoLast = modeinfo.substring(modeinfo.indexOf("twshortcode") + 12);
				int semiIndex = modeinfoLast.indexOf(";") + 12;
				int colonIndex = modeinfoLast.indexOf(":") + 12;
				int lastindex = modeinfo.length();

				if (semiIndex > 12 && colonIndex < 12) {
					lastindex = semiIndex;
				}
				if (semiIndex < 12 && colonIndex > 12) {
					lastindex = colonIndex;
				}

				if (semiIndex > 12 && colonIndex > 12) {
					if (semiIndex > colonIndex)
						lastindex = colonIndex;
					if (colonIndex > semiIndex)
						lastindex = semiIndex;
				}
				String twshortCodeValue = modeinfo.substring(modeinfo.indexOf("twshortcode") + 12, lastindex);
				if (twshortCodeValue != null && !twshortCodeValue.trim().equals("")
						&& !twshortCodeValue.trim().equals("null"))
					return twshortCodeValue;
			}
		}
		catch (Exception e) {
			logger.info("Exception occured while getting modeinfo." + e);
		}
		return null;
	}

	public static void writeSatPushCDRLog(SatPushLoggerBean satPushLoggerBean) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(iRBTConstant.kDateFormatwithTime);
			String currentTime = dateFormat.format(satPushLoggerBean.getRequestSentDate().getTime());
			cdr_logger.info(currentTime + ", " + satPushLoggerBean.getSubscriberId() + ", "
					+ (satPushLoggerBean.getRequestUrl()) + ", " + satPushLoggerBean.getResponse() + ", "
					+ satPushLoggerBean.getTimeTaken());
		}
		catch (Exception e) {
			logger.info("Exception occured while writing satPush CDR logs." + e);
		}
	}

	public static void writeSatPushCDRLog(LoggerBean loggerBean) {
		try {
			cdr_logger.info(loggerBean.getSubscriberId() + ", " + loggerBean.getTimestamp() + ", "
					+ loggerBean.getLdapResponse() + ", " + loggerBean.getDeactivationMode() + ", "
					+ loggerBean.getTefResponse() + ", " + loggerBean.getDeactivationStatus());
		}
		catch (Exception e) {
			logger.info("Exception occured while writing satPush CDR logs." + e);
		}
	}
}