package com.onmobile.apps.ringbacktones.webservice.implementation.util;

import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.dao.RBTHibernateDao;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;

public class RBTProtocolDao {

	private static final String CHARGEPERCALL_HIBERNATE_CFG_XML = "protocol_gen_hibernate.cfg.xml";

	private static final Logger logger = Logger
			.getLogger(RBTHibernateDao.class);
	private static Configuration configuration = null;
	private static SessionFactory sessionFactory = null;
	private static RBTProtocolDao rbtProtocolDao = null;

	static {
		configuration = new Configuration().configure(
				CHARGEPERCALL_HIBERNATE_CFG_XML);
		// Changes done for URL Encryption and Decryption
		ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
		try {
			if (resourceBundle.getString("ENCRYPTION_MODEL") != null
					&& resourceBundle.getString("ENCRYPTION_MODEL")
							.equalsIgnoreCase("yes")) {
				String connection_url = configuration
						.getProperty("hibernate.connection.url");
				String uName = configuration
						.getProperty("hibernate.connection.username");
				String password = configuration
						.getProperty("hibernate.connection.password");
				if (connection_url != null
						&& (connection_url.contains("user") || connection_url
								.contains("password"))) {
					connection_url = URLEncryptDecryptUtil
							.decryptAndMerge(connection_url);
					configuration.setProperty("hibernate.connection.url",
							connection_url);
				} else {
					if (uName != null) {
						uName = URLEncryptDecryptUtil
								.decryptUserNamePassword(uName);
						configuration.setProperty(
								"hibernate.connection.username", uName);
					}
					if (password != null) {
						password = URLEncryptDecryptUtil
								.decryptUserNamePassword(password);
						configuration.setProperty(
								"hibernate.connection.password", password);
					}
				}
			}
		} catch (MissingResourceException e) {
			logger.error("resource bundle exception: ENCRYPTION_MODEL");
		}
		// End of URL Encryption and Decryption
		sessionFactory = configuration.buildSessionFactory();
		if (logger.isDebugEnabled()) {
			logger.debug("RBTHibernateDao loaded successfully...");
		}
	}

	private RBTProtocolDao() {
		if (null != rbtProtocolDao) {
			throw new RuntimeException("Singleton Class.");
		}
	}

	public static RBTProtocolDao getInstance() {
		if (null == rbtProtocolDao) {
			synchronized (RBTProtocolDao.class) {
				if (null == rbtProtocolDao) {
					return new RBTProtocolDao();
				}
			}
		}
		return rbtProtocolDao;
	}

	public Long save(RBTProtocol rbtProtocol) {
		Session session = null;
		Transaction tx = null;
		Long id = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			id = (Long) session.save(rbtProtocol);
			session.flush();
			tx.commit();
		} catch (Exception e) {
			logger.error(
					"Unable to save: " + rbtProtocol + ", Exception: "
							+ e.getMessage(), e);
			tx.rollback();
		} finally {
		}
		logger.info("Returning id: " + id + ", for rbtProtocol: " + rbtProtocol);
		return id;
	}

	/**
	 * @param protocolId
	 * @param subscriberId
	 * @return
	 */
	public RBTProtocol get(String protocolId, String subscriberId) {
		logger.info("Fetching subscriberId for protocolId: " + protocolId
				+ ", subscriberId: " + subscriberId);
		if (null == subscriberId && null == protocolId) {
			logger.info("Mandatory parameters are missing");
			return null;
		}
		Session session = null;
		Transaction tx = null;
		RBTProtocol rbtProtocol = null;

		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			// rbtProtocol = (RBTProtocol) session.get(RBTProtocol.class,
			// Long.parseLong(protocolId));

			Criteria criteria = session.createCriteria(RBTProtocol.class);

			// To get records matching with OR condistions
			if (null != protocolId && null != subscriberId) {
				Criterion pId = Restrictions.eq("protocolId",
						Long.parseLong(protocolId));
				Criterion sId = Restrictions.eq("subscriberId", subscriberId);
				LogicalExpression andExp = Restrictions.and(pId, sId);
				criteria.add(andExp);
			} else if (null != protocolId) {
				criteria.add(Restrictions.eq("protocolId",
						Long.parseLong(protocolId)));
			} else {
				criteria.add(Restrictions.eq("subscriberId", subscriberId));
			}

			List<RBTProtocol> list = criteria.list();
			if (list.size() > 0) {
				rbtProtocol = list.get(0);
			}
			session.flush();
			tx.commit();
		} catch (Exception e) {
			logger.error("Unable to fetch protocolId: " + protocolId
					+ ", Exception: " + e.getMessage(), e);
			tx.rollback();
		} finally {

		}
		logger.info("Returning rbtProtocol: " + rbtProtocol + ", protocolId: "
				+ protocolId);
		return rbtProtocol;
	}
	
	public static void main(String[] args) {

		RBTProtocol rbtProtocol = new RBTProtocol();
//		rbtProtocol.setProtocolId(8L);
		rbtProtocol.setSubscriberId("9986012333");
		System.out.println(" >>> "
				+ RBTProtocolDao.getInstance().save(rbtProtocol));
	}

}
