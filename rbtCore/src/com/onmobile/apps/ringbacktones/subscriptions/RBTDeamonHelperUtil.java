package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.v2.common.Constants;

public class RBTDeamonHelperUtil {
	private static Logger logger = Logger.getLogger(RBTDeamonHelperUtil.class);

	private RBTDBManager m_rbtDBManager = null;
	private String status = null;
	private int defWaitTime = 0;

	public RBTDeamonHelperUtil() {
		m_rbtDBManager = RBTDBManager.getInstance();
		if (status == null) {
			status = Constants.SERVEY_STATUS;
		}
		if (defWaitTime == 0) {
			defWaitTime = Constants.DEF_WAIT_TIME_FOR_SURVEY;
		}
	}

	public RBTDeamonHelperUtil(String status) {
		this();
		this.status = status;
	}

	public RBTDeamonHelperUtil(String status, Integer defWaitTime) {
		this();
		this.status = status;
		this.defWaitTime = defWaitTime;
	}

	public void addTransDataOfAdPartner(String strSubID, String transId) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, defWaitTime);
		Date transDate = cal.getTime();

		logger.debug("subscriberId: " + strSubID + ", transId : " + transId + " , status:" + status + " , Trans Date:"
				+ transDate);
		m_rbtDBManager.addTransData(transId, strSubID, status, transDate);
		logger.debug("TRANS DATA ADDED SUCCESSFULLY");
	}
}
