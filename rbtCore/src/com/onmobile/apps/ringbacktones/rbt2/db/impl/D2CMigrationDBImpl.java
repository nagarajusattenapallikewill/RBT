package com.onmobile.apps.ringbacktones.rbt2.db.impl;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTProvisioningRequests;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriber;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriberSelection;
import com.onmobile.apps.ringbacktones.v2.dao.util.HibernateUtil;

public class D2CMigrationDBImpl {

	private static Logger logger = Logger.getLogger(D2CMigrationDBImpl.class);

	public boolean saveSubscriber(RBTSubscriber subscriber) {
		logger.info("migrating subscriber");
		boolean isMigrated = false;
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getMigrationSession();
			tx = session.beginTransaction();
			session.save(subscriber);
			tx.commit();
			session.flush();
			isMigrated = true;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured: " + he, he);
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured: " + e, e);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}

		logger.info("ClipStatusMapping saved: " + isMigrated);
		return isMigrated;
	}

	public boolean saveSubscriberSelection(RBTSubscriberSelection rbtSubscriberSelection) {
		logger.info("migrating subscriber selections");
		boolean isMigrated = false;
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getMigrationSession();
			tx = session.beginTransaction();
			session.save(rbtSubscriberSelection);
			tx.commit();
			session.flush();
			isMigrated = true;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured: " + he, he);
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured: " + e, e);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}

		logger.info("subscriber selections saved: " + isMigrated);
		return isMigrated;
	}
	
	public boolean saveProv(RBTProvisioningRequests rbtProvisioningRequests ) {
		logger.info("migrating rbtProvisioningRequests");
		boolean isMigrated = false;
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getMigrationSession();
			tx = session.beginTransaction();
			session.save(rbtProvisioningRequests);
			tx.commit();
			session.flush();
			isMigrated = true;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured: " + he, he);
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured: " + e, e);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}

		logger.info("rbtProvisioningRequests saved:" + isMigrated);
		return isMigrated;
	}
}
