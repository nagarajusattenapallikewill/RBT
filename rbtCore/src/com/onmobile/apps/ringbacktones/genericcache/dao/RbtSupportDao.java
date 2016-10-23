package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.RbtSupport;

public class RbtSupportDao {

	private static Logger logger = Logger.getLogger(RbtSupportDao.class);
	
	public static RbtSupport save(RbtSupport rbtSupport) throws RBTException{
		
		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            long id = (Long)session.save(rbtSupport);
            rbtSupport.setId(id);
            tx.commit();
            if (logger.isDebugEnabled()) {
            	logger.debug("Saved RbtSupport " + rbtSupport + " in "
                        + (System.currentTimeMillis() - start) + "ms");
            }
        } catch (HibernateException he) {
        	logger.error("", he);
            if (null != tx) {
                tx.rollback();
            }
            throw new RBTException(he.getMessage());
        } finally {
            session.close();
        }
		return rbtSupport;
	}
	
	public static void update(RbtSupport rbtSupport) throws RBTException{
		
		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(rbtSupport);
            tx.commit();
            if (logger.isDebugEnabled()) {
            	logger.debug("Updated RbtSupport " + rbtSupport + " in "
                        + (System.currentTimeMillis() - start) + "ms");
            }
        } catch (HibernateException he) {
        	logger.error("", he);
            if (null != tx) {
                tx.rollback();
            }
            throw new RBTException(he.getMessage());
        } finally {
            session.close();
        }
	}
	
	public static void delete(RbtSupport rbtSupport) throws RBTException{
		
		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(rbtSupport);
            tx.commit();
            if (logger.isDebugEnabled()) {
            	logger.debug("Deleted RbtSupport " + rbtSupport + " in "
                        + (System.currentTimeMillis() - start) + "ms");
            }
        } catch (HibernateException he) {
        	logger.error("", he);
            if (null != tx) {
                tx.rollback();
            }
            throw new RBTException(he.getMessage());
        } finally {
            session.close();
        }
	}
	
	public static List<RbtSupport> getRbtSupports(long sequenceId, int status, int maxResults) throws RBTException{
		
		List<RbtSupport> rbtSupportList = new ArrayList<RbtSupport>();
		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
        try {
            Criteria criteria = session.createCriteria(RbtSupport.class);
            criteria.add(Restrictions.eq("status", status));
            if(sequenceId != -1)
            	criteria.add(Restrictions.gt("id", sequenceId));
            
            if(maxResults != -1)
            	criteria.setMaxResults(maxResults);
            
            rbtSupportList =  (List<RbtSupport>)criteria.list();
            if (logger.isDebugEnabled()) {
            	logger.debug("Successfully Retrieved  " + rbtSupportList.size() + " records in "
                        + (System.currentTimeMillis() - start) + "ms");
            }
        } catch (HibernateException he) {
        	logger.error("", he);
            throw new RBTException(he.getMessage());
        } finally {
            session.close();
        }
		return rbtSupportList;
	}
}
