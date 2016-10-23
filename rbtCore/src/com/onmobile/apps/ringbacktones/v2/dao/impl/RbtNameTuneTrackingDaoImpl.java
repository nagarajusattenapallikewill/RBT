package com.onmobile.apps.ringbacktones.v2.dao.impl;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;
import com.onmobile.apps.ringbacktones.v2.dao.RbtNameTuneTrackingDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking;

/**
 * @author lakka.rameswarareddy
 */
public class RbtNameTuneTrackingDaoImpl extends FactoryDaoImpl implements RbtNameTuneTrackingDao {

	private static Logger logger = Logger.getLogger(RbtNameTuneTrackingDaoImpl.class);
	public static RbtNameTuneTrackingDaoImpl instance = null;

	private RbtNameTuneTrackingDaoImpl() {

	}

	public static RbtNameTuneTrackingDaoImpl getInstance() {
		synchronized (RbtNameTuneTrackingDaoImpl.class) {
			if (instance == null) {
				instance = new RbtNameTuneTrackingDaoImpl();
			}
		}
		return instance;
	}

	}
