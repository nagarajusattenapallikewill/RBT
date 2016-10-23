package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.PromoMaster;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;

public class PromoMasterDAO {

	private static Logger basicLogger = Logger.getLogger(PromoMasterDAO.class);
	
	public static List<PromoMaster> getAllPromoMasters() throws DataAccessException {
		
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(PromoMaster.class);
	    	List<PromoMaster> result = (List<PromoMaster>)criteria.list();
//	    	System.out.println(result);
	        tx.commit();
//	        System.out.println("Time: " + (System.currentTimeMillis() - start));
	        return result;
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
	
	@SuppressWarnings("unchecked")
	public static PromoMaster getPromoMaster(String promoType, String promoCode) throws DataAccessException {
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(PromoMaster.class);
	    	criteria = criteria.add(Restrictions.eq("promoType", promoType));
	    	criteria = criteria.add(Restrictions.eq("promoCode", promoCode));
	    	List<PromoMaster> result = (List<PromoMaster>)criteria.list();
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Got the PromoMaster in " + (System.currentTimeMillis() - start));
	        }
	        return result.size()>0?result.get(0):null;
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

	public static PromoMaster getPromoMaster(String promoCode) throws DataAccessException {
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(PromoMaster.class);
	    	criteria = criteria.add(Restrictions.eq("promoCode", promoCode));
	    	List<PromoMaster> result = (List<PromoMaster>)criteria.list();
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Got the PromoMaster in " + (System.currentTimeMillis() - start));
	        }
	        return result.size()>0?result.get(0):null;
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
	
	
	@SuppressWarnings("unchecked")
	public static PromoMaster getPromoMasterByClipPromoId(String clipPromoId) throws DataAccessException {
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(PromoMaster.class);
	    	criteria = criteria.add(Restrictions.eq("clipPromoId", clipPromoId));
	    	List<PromoMaster> result = (List<PromoMaster>)criteria.list();
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Got the PromoMaster in " + (System.currentTimeMillis() - start));
	        }
	        return result.size()>0?result.get(0):null;
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
	
	public static PromoMaster savePromoMaster(PromoMaster promoMaster) throws DataAccessException {
		long start = System.currentTimeMillis();
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	session.save(promoMaster);
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Saved PromoMaster " + promoMaster + " in " + (System.currentTimeMillis() - start));
	        }
			return promoMaster;
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

	public static PromoMaster updatePromoMaster(PromoMaster promoMaster) throws DataAccessException {
		long start = System.currentTimeMillis();
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	session.update(promoMaster);
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Updated PromoMaster " + promoMaster + " in " + (System.currentTimeMillis() - start));
	        }
			return promoMaster;
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

	public static PromoMaster[] saveOrUpdatePromoMaster(PromoMaster[] promoMasters) throws DataAccessException {
		long start = System.currentTimeMillis();
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	for(int i = 0; i< promoMasters.length; i++){
	    		session.saveOrUpdate(promoMasters[i]);
	    		if((i % 100) == 0){
	    			session.flush();
	    			session.clear();
	    		}
	    	}
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Successfully Saved or Updated PromoMaster  in " + (System.currentTimeMillis() - start));
	        }
	        
			return promoMasters;
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

	public static void deletePromoMasterByClipPromoId(String clipPromoId) throws DataAccessException {
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
    	List<PromoMaster> result = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(PromoMaster.class);
	    	criteria.add(Restrictions.eq("clipPromoId", clipPromoId));
	    	result = (List<PromoMaster>)criteria.list();
	    	int size = result.size();
	    	for(int i=0;i<size;i++){
	    		PromoMaster promoMaster = result.get(i);	    		
	    		RBTCache.getMemCachedClient().delete(RBTCacheKey.getPromoMasterCacheKey(promoMaster.getPromoCode(), promoMaster.getPromoType()));
	    		RBTCache.getMemCachedClient().delete(RBTCacheKey.getPromoCodeCacheKey(promoMaster.getPromoCode()));
	    		session.delete(promoMaster);
	    	}
	    	tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Deleted the PromoMaster in " + (System.currentTimeMillis() - start));
	        }
	        
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
	
//	public static void main(String[] args) {
////		testSavePromoMaster();
//		testGetPromoMaster();
////		testUpdatePromoMaster();
//	}
//	
//	private static void testUpdatePromoMaster() {
//		PromoMaster promoMaster = new PromoMaster();
//		promoMaster.setClipPromoId("12345");
//		promoMaster.setPromoType("Esia");
//		promoMaster.setPromoCode("2345");
//		try {
//			updatePromoMaster(promoMaster);
//		} catch (DataAccessException e) {
//			e.printStackTrace();
//		}
//	}
//	private static void testSavePromoMaster() {
//		PromoMaster promoMaster = new PromoMaster();
//		promoMaster.setClipPromoId("1234");
//		promoMaster.setPromoType("Esia");
//		promoMaster.setPromoCode("2345");
//		try {
//			savePromoMaster(promoMaster);
//		} catch (DataAccessException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static void testGetPromoMaster() {
//		try {
//			PromoMaster promoMaster = getPromoMaster("Esia", "2345");
//			System.out.println(promoMaster);
//		} catch (DataAccessException e) {
//			e.printStackTrace();
//		}
//	}
}
