package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;

public class RBTConsentCleanUp {

	private static String getTypeforCleanUp = null;
	private static String noOfHoursAfterWhichToDelete = null;
    private static Logger logger = Logger.getLogger(RBTConsentCleanUp.class);
    static String loggerName = "CLEAN_EXPIRED_PROCESSED_CONSENT_RECORDS"; 
    private static Logger cleanUpLogger = RbtLogger.createRollingFileLogger(RbtLogger.reporterStatisticsPrefix+loggerName, ROLLING_FREQUENCY.DAILY);
    private static boolean log_service_class_id = false;


	static {
		getTypeforCleanUp = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION", "CRITERIA_FOR_CLEAN_UP",
				"CONSENT-STATUS");

		noOfHoursAfterWhichToDelete = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION", "HOURS_AFTER_TO_DELETE_CONSENT_RECORD",
				"5");
		
		log_service_class_id = RBTParametersUtils.getParamAsBoolean(
				"DOUBLE_CONFIRMATION", "LOG_SERVICE_CLASS_AND_ID",
				"false");
	}

	public RBTConsentCleanUp() {

	}

	public static int processConsentCleanUp() {
         int count = 0;
		if (getTypeforCleanUp.equalsIgnoreCase("CONSENT-STATUS")) {
			List<DoubleConfirmationRequestBean> reqBeanList = RBTDBManager
					.getInstance().getDoubleConfirmationRequestBeanForStatus(
							"3", null, null, null, false);
			count = processCleanUp(reqBeanList, "COMPLETELY PROCESSED");
			List<DoubleConfirmationRequestBean> reqBeanList1 = RBTDBManager
					.getInstance().getDoubleConfirmationRequestBeanForStatus(
							"4", null, null, null, false);

			count += processCleanUp(reqBeanList1, "COMPLETELY PROCESSED");
		} else if (getTypeforCleanUp.equalsIgnoreCase("REQUEST-TIME")) {
			List<DoubleConfirmationRequestBean> reqBeanList = RBTDBManager
					.getInstance().getRecordsBeforeConfRequestTime(
							noOfHoursAfterWhichToDelete);
			count = processCleanUp(reqBeanList, "EXPIRED");
		} else if (getTypeforCleanUp.equalsIgnoreCase("STATUS-TIME")) {
			List<DoubleConfirmationRequestBean> reqBeanList = RBTDBManager
					.getInstance().getDoubleConfirmationRequestBeanForStatus(
							"3", null, null, null, false);
			count = processCleanUp(reqBeanList, "COMPLETELY PROCESSED");
			List<DoubleConfirmationRequestBean> reqBeanList1 = RBTDBManager
					.getInstance().getDoubleConfirmationRequestBeanForStatus(
							"4", null, null, null, false);
			count += processCleanUp(reqBeanList1, "COMPLETELY PROCESSED");

			List<DoubleConfirmationRequestBean> reqBeanList2 = RBTDBManager
					.getInstance().getRecordsBeforeConfRequestTime(
							noOfHoursAfterWhichToDelete);
			count += processCleanUp(reqBeanList2, "EXPIRED");

		}

		
		return count;
	}

	private static int processCleanUp(List<DoubleConfirmationRequestBean> reqBeanList,String reason) {
		logger.debug("Consent clean up records == "+reqBeanList);
		if (reqBeanList == null || reqBeanList.size() == 0)
			return 0;
		int count = 0;
		Iterator<DoubleConfirmationRequestBean> reqBeanIterator = reqBeanList
				.iterator();
		// RBT-12312 Vodafone IN:-Consent cleanup daemon is not writing
		// serviceid and service class in trans logs.
		List<String> lstTransId= new ArrayList<String>();
		while (reqBeanIterator.hasNext()) {
			DoubleConfirmationRequestBean reqBean = reqBeanIterator.next();
			logger.info("Consent record to be deleted == "+reqBean.toString());
			boolean success = RBTDBManager.getInstance().deleteConsentRecord(
					reqBean.getTransId(), null, null, null,
					reqBean.getSubscriberID(),false);
			logger.debug("Result of delete consent record ="+success);
			String reqBeanString = null;
			if(success){
				count++;
				//String reqBeanString = reqBean.toString();
				// RBT-12312 Vodafone IN:-Consent cleanup daemon is not writing
				// serviceid and service class in trans logs.
				String subClass = reqBean.getSubscriptionClass();
				String chargeClass = reqBean.getClassType();
				//Consent Service key and Class
				if (log_service_class_id) {
					boolean isActRequest = false;
					boolean isSelRequest = false;
					boolean isActAndSelRequest = false;
					boolean isSelTransId = false;
					if (reqBean.getRequestType().equalsIgnoreCase("SEL")) {
						isSelRequest = true;
						// Start:RBT-12312 Vodafone IN:-Consent cleanup daemon
						// is not writing serviceid and service class in trans
						// logs.
						if (lstTransId.size() > 0) {
							for (String transID : lstTransId) {
								if (reqBean.getTransId().equalsIgnoreCase(
										transID)) {
									isSelTransId = true;
									break;
								}
							}
							if(isSelTransId){
								continue;
							}							
						}
						// End:RBT-12312 Vodafone IN:-Consent cleanup daemon is
						// not writing serviceid and service class in trans
						// logs.
					}
					
					if (reqBean.getRequestType().equalsIgnoreCase("ACT")) {
						isActRequest = true;
						Map<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(reqBean.getExtraInfo());
						if (extraInfoMap != null
								&& extraInfoMap.containsKey("TRANS_ID")) {
							String selTrasId = extraInfoMap.get("TRANS_ID");
							// RBT-12312 Vodafone IN:-Consent cleanup daemon is
							// not writing serviceid and service class in trans
							// logs.
							lstTransId.add(selTrasId);
							List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = RBTDBManager
									.getInstance()
									.getDoubleConfirmationRequestBeanForStatus(
											null, selTrasId,
											reqBean.getSubscriberID(), null,
											true);
							if (doubleConfirmReqBeans != null
									&& doubleConfirmReqBeans.size() > 0) {
								isActAndSelRequest = true;
								// RBT-12312 Vodafone IN:-Consent cleanup daemon
								// is not writing serviceid and service class in
								// trans logs.
								chargeClass = doubleConfirmReqBeans.get(0)
										.getClassType();
								String wavFileName = doubleConfirmReqBeans.get(
										0).getWavFileName();
								Integer clipID = doubleConfirmReqBeans.get(0)
										.getClipID();
								reqBean.setClassType(chargeClass);
								reqBean.setRequestType("COMBO");
								reqBean.setWavFileName(wavFileName);
								reqBean.setClipID(clipID);
							}
						}
					}
					String circleId = reqBean.getCircleId();
					String srvKey = DoubleConfirmationConsentPushThread
							.getServiceValue("SERVICE_ID", subClass,
									chargeClass, circleId, isActRequest,
									isSelRequest, isActAndSelRequest);
					String srvClass = DoubleConfirmationConsentPushThread
							.getServiceValue("SERVICE_CLASS", subClass,
									chargeClass, circleId, isActRequest,
									isSelRequest, isActAndSelRequest);
					StringBuilder moreLogInfo = new StringBuilder();
					reqBeanString = reqBean.toString();
					moreLogInfo.append(", serviceKey=");
					moreLogInfo.append(srvKey);
					moreLogInfo.append(", serviceClass=");
					moreLogInfo.append(srvClass);
					moreLogInfo.append("]");
					reqBeanString = reqBeanString.substring(0, reqBeanString
							.lastIndexOf("]"))
							+ moreLogInfo.toString();
				}
				cleanUpLogger.info(reason + " : " + reqBeanString);
			}
		}
       return count;
	}
}
