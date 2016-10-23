package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallPrismRetry;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallTxn;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.dao.RBTHibernateDao;


/**
 * 
 * @author rony.gregory
 */
public class RBTChargePerCallPrismRetryDaemon implements Runnable {
	
	private static final String CHARGE_PER_CALL_PRISM_RETRY_INTERVAL_MINS_TO_PICK = "CHARGE_PER_CALL_PRISM_RETRY_INTERVAL_MINS_TO_PICK";
	private static final String CHARGE_PER_CALL_PRISM_RETRY_RECORD_LIMIT = "CHARGE_PER_CALL_PRISM_RETRY_RECORD_LIMIT";
	private static final Logger logger = Logger
			.getLogger(RBTChargePerCallPrismRetryDaemon.class);
	
	private static int intervalMinutes;
	private static int recordLimit;
	public RBTChargePerCallPrismRetryDaemon() {
		intervalMinutes = RBTParametersUtils.getParamAsInt("DAEMON",
				CHARGE_PER_CALL_PRISM_RETRY_INTERVAL_MINS_TO_PICK, 5);
		recordLimit = RBTParametersUtils.getParamAsInt("DAEMON",
				CHARGE_PER_CALL_PRISM_RETRY_RECORD_LIMIT, 20);
	}
	
	@Override
	public void run() {
		logger.debug("");
		
		List<RBTChargePerCallPrismRetry> list = RBTHibernateDao.getInstance().getPrismRetryRecords(intervalMinutes, recordLimit);
		RBTChargePerCallService rbtChargePerCallService = new RBTChargePerCallService();
		for (RBTChargePerCallPrismRetry record : list) {
			RBTChargePerCallTxn rbtChargePerCallTxn = new RBTChargePerCallTxn(record.getCallerId(), record.getCalledId(), record.getCalledTime(), record.getWavFile());
			rbtChargePerCallTxn.setCallDuration(record.getCallDuration());
			rbtChargePerCallService.process(rbtChargePerCallTxn, record.getRefId());
		}
	}
	
	public static void main(String[] args) {
		Thread t = new Thread(new RBTChargePerCallPrismRetryDaemon());
		t.start();
		/*RBTChargePerCallTxn rbtChargePerCallTxn = new RBTChargePerCallTxn("9845345682", "9845345681", new Date(), "rbt_nophone_pun_rbt");
		RBTChargePerCallService rbtChargePerCallService = new RBTChargePerCallService();
		rbtChargePerCallService.process(rbtChargePerCallTxn, null);*/
		
		/*RBTChargePerCallPrismRetry obj = new RBTChargePerCallPrismRetry("9845345682", "9845345681", new Date(), "rbt_nophone_pun_rbt", new Date(), "xyz", 0);
		RBTHibernateDao.getInstance().deleteRBTChargePerCallPrsimRetry(obj);
*/	}
}