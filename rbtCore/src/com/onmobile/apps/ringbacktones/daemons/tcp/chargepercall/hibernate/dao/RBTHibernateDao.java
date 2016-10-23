package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.dao;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCall;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallLog;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallPrismRetry;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallTxn;

public class RBTHibernateDao {

	private static final String CHARGEPERCALL_HIBERNATE_CFG_XML = "chargepercall_hibernate.cfg.xml";
	private static final String retryTime = "retryTime";
	private static final Logger logger = Logger
			.getLogger(RBTHibernateDao.class);
	private static SessionFactory sessionFactory = null;
	private static RBTHibernateDao rbtHibernateDao = null;
	
	static {
		sessionFactory = new Configuration().configure(
				CHARGEPERCALL_HIBERNATE_CFG_XML).buildSessionFactory();
		if (logger.isDebugEnabled()) {
			logger.debug("RBTHibernateDao loaded successfully...");
		}
	}

	private RBTHibernateDao() {
		if (null != rbtHibernateDao) {
			throw new RuntimeException("Singleton Class.");
		}
	}

	public static RBTHibernateDao getInstance() {
		if (null == rbtHibernateDao) {
			synchronized (RBTHibernateDao.class) {
				if (null == rbtHibernateDao) {
					return new RBTHibernateDao();
				}
			}
		}
		return rbtHibernateDao;
	}

	/**
	 * Persists the given RBTChargePerCall into database
	 * 
	 * @param rbtChargePerCall
	 */
	public boolean save(RBTChargePerCall rbtChargePerCall) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving chargePerCall: " + rbtChargePerCall);
		}

		Session session = null;
		Transaction tx = null;
		boolean isSaved = false;
		try {
			RBTChargePerCallLog chargePerCallLog = (RBTChargePerCallLog) get(rbtChargePerCall);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if (null == chargePerCallLog) {
				session.save(rbtChargePerCall);
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully saved chargePerCall: "
							+ rbtChargePerCall);
				}
				isSaved = true;
			} else {
				logger.warn("Already inserted chargePerCall: "
						+ rbtChargePerCall);
			}
		} catch (Exception e) {
			logger.error("Failed to save rbtChargePerCallLog: "
					+ rbtChargePerCall + ", error: " + e.getMessage(), e);
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);

		}
		return isSaved;
	}

	public boolean save(RBTChargePerCallPrismRetry rbtChargePerCallPrismRetry) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving rbtChargePerCallPrismRetry: " + rbtChargePerCallPrismRetry);
		}
		Session session = null;
		Transaction tx = null;
		boolean isSaved = false;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
				session.save(rbtChargePerCallPrismRetry);
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully saved chargePerCall: "
							+ rbtChargePerCallPrismRetry);
				}
				isSaved = true;
		} catch (Exception e) {
			logger.error("Failed to save rbtChargePerCallPrismRetry: "
					+ rbtChargePerCallPrismRetry + ", error: " + e.getMessage(), e);
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);
		}
		return isSaved;
	}

	
	public void delete(RBTChargePerCall rbtChargePerCall) {
		if (logger.isDebugEnabled()) {
			logger.debug("Deleting chargePerCall: " + rbtChargePerCall);
		}
		Session session = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			session.delete(rbtChargePerCall);
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully deleted chargePerCall: "
						+ rbtChargePerCall);
			}
		} catch (Exception e) {
			logger.error("Failed to delete rbtChargePerCall: "
					+ rbtChargePerCall + ", error: " + e.getMessage(), e);
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);
		}
	}
	
	public boolean deleteRBTChargePerCallPrismRetry(String calledId, String refId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Deleting rbtChargePerCallPrismRetry with calledId: "
							+ calledId + ", refId: " + refId);
		}
		Session session = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			String hql = "delete from rbt_charge_per_call_prism_retry where called_id= :calledId and ref_id = :refId";
			SQLQuery query = session.createSQLQuery(hql);
			query.setString("calledId", calledId);
			query.setString("refId", refId);
			int updateCount = query.executeUpdate();
			if (updateCount > 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully deleted rbtChargePerCallPrismRetry with calledId: "
							+ calledId + ", refId: " + refId);
				}
				return true;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("No records found with calledId: "
							+ calledId + ", refId: " + refId);
				}
				return false;
			}
		} catch (Exception e) {
			logger.error("Failed to delete rbtChargePerCallPrismRetry with calledId: "
							+ calledId + ", refId: " + refId +". error: " + e.getMessage(), e);
			rollback(tx);
			return false;
		} finally {
			commit(tx);
			disconnectSession(session);
		}
	}

	/**
	 * Update number of calls made in RBTChargePerCallLog
	 * 
	 * @param rbtChargePerCallLog
	 */
	public void updateCallsMade(RBTChargePerCallLog rbtChargePerCallLog) {
		try {
			int i = update(rbtChargePerCallLog);
			if (i == 0) {
				save(rbtChargePerCallLog);
			}
		} catch (Exception e) {
			logger.error("Updating again, since it is failed to save. rbtChargePerCallLog: "
					+ rbtChargePerCallLog);
			update(rbtChargePerCallLog);
		}
	}

	public int update(RBTChargePerCallLog rbtChargePerCallLog) {
		int updated = 0;
		Session session = null;
		Transaction tx = null;
		try {
			RBTChargePerCallLog chargePerCallLog = (RBTChargePerCallLog) get(rbtChargePerCallLog);
			if (null != chargePerCallLog) {
				session = sessionFactory.openSession();
				tx = session.beginTransaction();

				chargePerCallLog = (RBTChargePerCallLog) session.get(
						RBTChargePerCallLog.class, chargePerCallLog.getId());

				chargePerCallLog.setNoOfCallsMade(chargePerCallLog
						.getNoOfCallsMade() + 1);
				if (logger.isDebugEnabled()) {
					logger.debug("Before update, synchronzed rbtChargePerCallLog: "
							+ chargePerCallLog
							+ ", lock mode: "
							+ session.getCurrentLockMode(chargePerCallLog));
				}
				session.update(chargePerCallLog);
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully updated rbtChargePerCallLog1: "
							+ chargePerCallLog);
				}
				updated++;
			} else {
				logger.warn("Could not update, rbtChargePerCallLog is null");
			}
		} catch (Exception e) {
			logger.error("Failed to update chargePerCallLog: "
					+ rbtChargePerCallLog);
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);
		}
		return updated;
	}

	public void update(RBTChargePerCall rbtChargePerCall) {
		if (logger.isDebugEnabled()) {
			logger.debug("Updating chargePerCall: " + rbtChargePerCall);
		}
		Session session = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			session.update(rbtChargePerCall);
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully updated chargePerCall: "
						+ rbtChargePerCall);
			}
		} catch (Exception e) {
			logger.error("Failed to update rbtChargePerCall: "
					+ rbtChargePerCall + ", error: " + e.getMessage(), e);
			rollback(tx);
		} finally {
			System.out.println(tx.toString());
			commit(tx);
			disconnectSession(session);
		}
	}

	/**
	 * Fetch the records arrived lesser then the given time.
	 * 
	 * @param date
	 * @return List of RBTChargePerCall records
	 */
	public List<RBTChargePerCallTxn> findUnProcessed() {

		Session session = sessionFactory.openSession();
		List<RBTChargePerCallTxn> list = null;

		Criteria criteria = session.createCriteria(RBTChargePerCallTxn.class);

		Transaction tx = session.beginTransaction();

		list = criteria.list();
		tx.commit();

		if (logger.isDebugEnabled()) {
			logger.debug("ChargePerCall unprocessed list: " + list);
		}
		session.disconnect();
		session.close();
		return list;
	}

	/**
	 * Fetch the records arrived lesser then the given time.
	 * 
	 * @param date
	 * @return List of RBTChargePerCall records
	 */
	public int deleteOldLog(int minutes) {

		Session session = sessionFactory.openSession();

		StringBuffer sb = new StringBuffer("delete from "
				+ RBTChargePerCallLog.class.getName()
				+ " where calledTime < timestampadd(MINUTE,:called_time,now())");

		Transaction tx = session.beginTransaction();

		Query query = session.createQuery(sb.toString());
		query.setParameter("called_time", -minutes);
		int deleted = query.executeUpdate();
		tx.commit();

		if (logger.isDebugEnabled()) {
			logger.debug("cleaned: " + deleted);
		}
		session.disconnect();
		session.close();
		return deleted;
	}

	/**
	 * Returns the number of calls made from caller to called.
	 * 
	 * @param callerId
	 * @param calledId
	 * @return number of calls made in integer
	 */
	public RBTChargePerCall get(RBTChargePerCall chargePerCall) {
		if (null == chargePerCall) {
			logger.warn("Could not proceed with null object: " + chargePerCall);
			return chargePerCall;
		}

		RBTChargePerCallLog chargePerCallLog = null;
		Session session = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			chargePerCallLog = fetch(session, chargePerCall);
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully fetched chargePerCallLog: "
						+ chargePerCallLog);
			}
		} catch (Exception e) {
			logger.error("Unable to fetch calls made for callerId: "
					+ chargePerCall.getCallerId() + ", calledId: "
					+ chargePerCall.getCalledId());
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);
		}
		return chargePerCallLog;
	}

	private RBTChargePerCallLog fetch(Session session,
			RBTChargePerCall chargePerCall) {
		RBTChargePerCallLog newChargePerCall = null;
		Criteria criteria = session.createCriteria(RBTChargePerCallLog.class);
		criteria.add(Restrictions.like("callerId", chargePerCall.getCallerId()));
		criteria.add(Restrictions.like("calledId", chargePerCall.getCalledId()));
		List list = criteria.list();
		if (list.size() > 0) {
			newChargePerCall = ((RBTChargePerCallLog) list.get(0));
		}
		return newChargePerCall;
	}

	private void commit(Transaction tx) {
		if (null != tx) {
			tx.commit();
		}
	}

	private void rollback(Transaction tx) {
		if (null != tx) {
			tx.rollback();
		}
	}

	private void disconnectSession(Session session) {
		if (null != session) {
			session.disconnect();
		}
	}

	/**
	 * 
	 * @return List of RBTChargePerCallPrismRetry records
	 */
	public List<RBTChargePerCallPrismRetry> getPrismRetryRecords(int interval, int recordLimit) {
		Session session = sessionFactory.openSession();
		Criteria criteria = session.createCriteria(RBTChargePerCallPrismRetry.class);
		if (interval > 0) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, -1 * interval);
			criteria.add(Restrictions.ge(retryTime, c.getTime()));
		}
		if (recordLimit > 0) {
			criteria.setMaxResults(recordLimit);
		}
		Transaction tx = session.beginTransaction();
		
		@SuppressWarnings("unchecked")
		List<RBTChargePerCallPrismRetry> list = criteria.list();
		tx.commit();

		if (logger.isDebugEnabled()) {
			logger.debug("Interval in minutes: " + interval + ", recordLimit: "+ recordLimit + ", Retrieved RBTChargePerCallPrismRetry list: " + list);
		}
		session.disconnect();
		session.close();
		return list;
	}
	
	/**
	 * 
	 * @return RBTChargePerCallPrismRetry record
	 */
	public RBTChargePerCallPrismRetry getPrismRetryRecord(String calledId) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Criteria criteria = session.createCriteria(RBTChargePerCallPrismRetry.class);
		criteria.add(Restrictions.like("calledId", calledId));
		@SuppressWarnings("unchecked")
		List<RBTChargePerCallPrismRetry> list = criteria.list();
		RBTChargePerCallPrismRetry rbtChargePerCallPrismRetry = null;
		if (list.size() > 0) {
			rbtChargePerCallPrismRetry = list.get(0);
		}
		tx.commit(); 

		if (logger.isDebugEnabled()) {
			logger.debug("CalledId: " + calledId + ", Retrieved RBTChargePerCallPrismRetry: " + rbtChargePerCallPrismRetry);
		}
		session.disconnect();
		session.close();
		return rbtChargePerCallPrismRetry;
	}
}
