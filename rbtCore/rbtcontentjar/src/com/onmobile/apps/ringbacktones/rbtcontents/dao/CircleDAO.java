package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Circle;

public class CircleDAO {
	
	private static Logger log = Logger.getLogger(CircleDAO.class);
	
	public static Circle saveCircle(Circle circle) throws DataAccessException {
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	session.save(circle);
	        tx.commit();
//	        System.out.println("Time: " + (System.currentTimeMillis() - start));
			return circle;
		} catch(HibernateException he) {
			log.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	public static List<Circle> getAllCircles() throws DataAccessException {
		
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(Circle.class);
	    	List<Circle> result = (List<Circle>)criteria.list();
//	    	System.out.println(result);
	        tx.commit();
//	        System.out.println("Time: " + (System.currentTimeMillis() - start));
	        return result;
		} catch(HibernateException he) {
			log.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
    public static void updateCircle(Circle circle) throws DataAccessException {

    	long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	session.update(circle);
	    	tx.commit();
//	        System.out.println("Time: " + (System.currentTimeMillis() - start));
	    	return;
		} catch(HibernateException he) {
			log.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
    }
    
    public static Circle getCircle(String circleId) throws DataAccessException {
//		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Circle circle = (Circle)session.get(Circle.class, circleId);
	        tx.commit();
			return circle;
		} catch(HibernateException he) {
			log.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
    }
}
