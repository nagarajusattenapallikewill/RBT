package com.onmobile.apps.ringbacktones.daemons.multioperator;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

public class RBTMultiOpCopyHibernateDao {

	private static final String MULTI_OP_COPY_HIBERNATE_CFG_XML = "multi_op_copy_hibernate.cfg.xml";
	private static final Logger logger = Logger
			.getLogger(RBTMultiOpCopyHibernateDao.class);
	private static SessionFactory sessionFactory = null;
	private static RBTMultiOpCopyHibernateDao rbtHibernateDao = null;

	static {
		sessionFactory = new Configuration().configure(
				MULTI_OP_COPY_HIBERNATE_CFG_XML).buildSessionFactory();
		if (logger.isDebugEnabled()) {
			logger.debug("RBTMultiOpCopyHibernateDao loaded successfully...");
		}
	}

	private RBTMultiOpCopyHibernateDao() {
		if (null != rbtHibernateDao) {
			throw new RuntimeException("Singleton Class.");
		}
	}

	public static RBTMultiOpCopyHibernateDao getInstance() {
		if (null == rbtHibernateDao) {
			synchronized (RBTMultiOpCopyHibernateDao.class) {
				if (null == rbtHibernateDao) {
					return new RBTMultiOpCopyHibernateDao();
				}
			}
		}
		return rbtHibernateDao;
	}

	/**
	 * Persists the given RBTMultiOpCopyRequest into database
	 * 
	 * @param rbtMultiOpCopyRequest
	 */
	public boolean save(RBTMultiOpCopyRequest rbtMultiOpCopyRequest) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving multiOpCopyRequest: " + rbtMultiOpCopyRequest);
		}

		Session session = null;
		Transaction tx = null;
		boolean isSaved = false;
		try {
			RBTMultiOpCopyRequest rBTMultiOpCopyRequest = (RBTMultiOpCopyRequest) get(rbtMultiOpCopyRequest);
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			if (null == rBTMultiOpCopyRequest) {
				session.save(rbtMultiOpCopyRequest);
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully saved multiOpCopyRequest: "
							+ rbtMultiOpCopyRequest);
				}
				isSaved = true;
			} else {
				logger.warn("Already inserted multiOpCopyRequest: "
						+ rbtMultiOpCopyRequest);
			}
		} catch (Exception e) {
			logger.error("Failed to save rbtChargePerCall: "
					+ rbtMultiOpCopyRequest + ", error: " + e.getMessage(), e);
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);
		}
		return isSaved;
	}

	public void delete(RBTMultiOpCopyRequest rbtMultiOpCopyRequest) {
		if (logger.isDebugEnabled()) {
			logger.debug("Deleting multiOpCopyRequest: "
					+ rbtMultiOpCopyRequest);
		}
		Session session = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			session.delete(rbtMultiOpCopyRequest);
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully deleted rbtMultiOpCopyRequest: "
						+ rbtMultiOpCopyRequest);
			}
		} catch (Exception e) {
			logger.error("Failed to delete rbtMultiOpCopyRequest: "
					+ rbtMultiOpCopyRequest + ", error: " + e.getMessage(), e);
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);
		}
	}

	/**
	 * Returns the number of calls made from caller to called.
	 * 
	 * @param callerId
	 * @param calledId
	 * @return number of calls made in integer
	 */
	public List<RBTMultiOpCopyRequest> fetch(int fetchFrom, int fetchLimit,
			int status) {

		Session session = null;
		Transaction tx = null;
		List<RBTMultiOpCopyRequest> list = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session
					.createCriteria(RBTMultiOpCopyRequest.class);
			criteria.add(Restrictions.like("status", status));
			criteria.setFirstResult(fetchFrom);
			criteria.setMaxResults(fetchLimit);
			list = criteria.list();

		} catch (Exception e) {
			logger.error("Unable to fetch RBTMultiOpCopyRequest. fetchFrom: "
					+ fetchFrom + ", fetchLimit: " + fetchLimit);
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);
		}
		logger.info("Fetched number of records: " + list.size() + ", list: "
				+ list);
		return list;
	}

	/**
	 * Returns the number of calls made from caller to called.
	 * 
	 * @param callerId
	 * @param calledId
	 * @return number of calls made in integer
	 */
	public RBTMultiOpCopyRequest get(RBTMultiOpCopyRequest rbtMultiOpCopyRequest) {
		if (null == rbtMultiOpCopyRequest) {
			logger.warn("Could not proceed with null object: "
					+ rbtMultiOpCopyRequest);
			return rbtMultiOpCopyRequest;
		}

		RBTMultiOpCopyRequest rBTMultiOpCopyRequest = null;
		Session session = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session
					.createCriteria(RBTMultiOpCopyRequest.class);
			criteria.add(Restrictions.like("copyId",
					rbtMultiOpCopyRequest.getCopyId()));
			List list = criteria.list();
			logger.info("list: " + list + ", copy id:"
					+ rbtMultiOpCopyRequest.getCopyId());
			if (list.size() > 0) {
				rBTMultiOpCopyRequest = ((RBTMultiOpCopyRequest) list.get(0));
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Successfully fetched multiOpCopyRequest: "
						+ rBTMultiOpCopyRequest);
			}
		} catch (Exception e) {
			logger.error("Unable to fetch calls made for copyId: "
					+ rBTMultiOpCopyRequest);
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);
		}
		return rBTMultiOpCopyRequest;
	}

	public void update(RBTMultiOpCopyRequest rbtMultiOpCopyRequest) {

		Session session = null;
		Transaction tx = null;

		try {

			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			session.update(rbtMultiOpCopyRequest);

			if (logger.isDebugEnabled()) {
				logger.debug("Successfully updated rbtMultiOpCopyRequest: "
						+ rbtMultiOpCopyRequest);
			}

		} catch (Exception e) {
			logger.error("Unable to update rbtMultiOpCopyRequest: "
					+ rbtMultiOpCopyRequest + ", Exception: " + e.getMessage(),
					e);
			rollback(tx);
		} finally {
			commit(tx);
			disconnectSession(session);
		}

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
}
