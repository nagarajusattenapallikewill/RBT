package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall;

import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.dao.RBTHibernateDao;

public class RBTChargePerCallLogDBCleaner implements Runnable {

	private static final Logger logger = Logger
			.getLogger(RBTChargePerCallLogDBCleaner.class);

	private int minutes;

	public RBTChargePerCallLogDBCleaner(int minutes) {
		if(logger.isDebugEnabled()) {
			logger.debug("Stated Cleaner: "+new Date());
		}
		this.minutes = minutes;
	}

	@Override
	public void run() {
		int cleaned = RBTHibernateDao.getInstance().deleteOldLog(minutes);
		if(logger.isDebugEnabled()) {
			logger.debug("Current time: "+new Date()+", cleaned: "+cleaned);
		}
	}
}
