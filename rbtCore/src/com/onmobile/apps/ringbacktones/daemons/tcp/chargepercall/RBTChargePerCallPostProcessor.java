package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallTxn;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.dao.RBTHibernateDao;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.RBTChargePerCallService;

public class RBTChargePerCallPostProcessor implements Runnable {
	private static final Logger logger = Logger
			.getLogger(RBTChargePerCallService.class);


	@Override
	public void run() {

		List<RBTChargePerCallTxn> list = RBTHibernateDao.getInstance()
				.findUnProcessed();

		CopyOnWriteArrayList<RBTChargePerCallTxn> concurrentList = new CopyOnWriteArrayList<RBTChargePerCallTxn>(
				list);
		if (logger.isDebugEnabled()) {
			logger.debug("Processing failed requests: " + concurrentList);
		}

		RBTChargePerCallService rbtChargePerCallService = new RBTChargePerCallService();

		for (RBTChargePerCallTxn rbtChargePerCall : concurrentList) {
			rbtChargePerCallService.process(rbtChargePerCall, null);
			rbtChargePerCallService.delete(rbtChargePerCall);
		}
		
	}
}
