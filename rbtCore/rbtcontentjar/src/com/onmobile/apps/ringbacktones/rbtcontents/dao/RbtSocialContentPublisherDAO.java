package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.RbtSocialContentPublisher;

public class RbtSocialContentPublisherDAO {

	private static Logger basicLogger = Logger.getLogger(RbtSocialContentPublisherDAO.class);
	
	/**
	 * Gets all the RbtSocialContentPublisher available in the RBT_SOCIAL_CONTENT_PUBLISHER table.
	 * As the complete table is transfered, use the API carefully. 
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static List<RbtSocialContentPublisher> load(int limit) throws DataAccessException {

		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		List<RbtSocialContentPublisher> objs = null;
		Transaction tx = null;
		String sql = "SELECT * FROM RBT_SOCIAL_CONTENT_PUBLISHER ORDER BY SEQUENCE_ID ASC LIMIT " + limit;
		try {
			tx = session.beginTransaction();
			objs = session.createSQLQuery(sql).addEntity("cust", RbtSocialContentPublisher.class).list();
			tx.commit();
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got all the clips in " + (System.currentTimeMillis() - start) + "ms");
			}
			return objs;
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	
	/**
	 * Delete RbtSocialContentPublisher available in the RBT_SOCIAL_CONTENT_PUBLISHER table.
	 * As the complete table is transfered, use the API carefully. 
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static void delete(List<RbtSocialContentPublisher> listObject) throws DataAccessException {

		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		List<RbtSocialContentPublisher> objs = null;
		Transaction tx = null;
		tx = session.beginTransaction();
		try {
			
			for(RbtSocialContentPublisher publisher : listObject){
				session.delete(publisher);				
			}
			tx.commit();
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	
}
