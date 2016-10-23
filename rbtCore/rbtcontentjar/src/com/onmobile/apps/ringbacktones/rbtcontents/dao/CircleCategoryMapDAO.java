package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.CircleCategoryMap;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;

public class CircleCategoryMapDAO {

	private static Logger basicLogger = Logger.getLogger(CircleCategoryMapDAO.class);
	
	public static List<CircleCategoryMap> getAllCircleCategoryMaps() throws DataAccessException {
		
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(CircleCategoryMap.class);
	    	List<CircleCategoryMap> result = (List<CircleCategoryMap>)criteria.list();
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

//	public List<Category> getCategoriesInCircle(String circleId) throws DataAccessException {
//		String hql = "select category from CircleCategoryMap as circleCategoryMap, Category as categories " +
//				"where circleId=:circleId and circleCategoryMap.categoryId=categories.categoryId";
//		
//		long start = System.currentTimeMillis(); 
//    	Session session = HibernateUtil.getSession();
//    	Transaction tx = null;
//		try {
//	    	tx = session.beginTransaction();
//	    	Query query = session.createQuery(hql);
//	    	List<Category> result = query.list();
//	    	System.out.println(result);
//	        tx.commit();
//	        System.out.println("Time: " + (System.currentTimeMillis() - start));
//	        return result;
//		} catch(HibernateException he) {
//			if(null != tx) {
//				tx.rollback();
//			}
//			throw new DataAccessException(he);
//		} finally {
//			session.close();
//		}	
//	}

	public static List<CircleCategoryMap> getCategoriesInCircle(String circleId) throws DataAccessException {
		
		String hql = "from CircleCategoryMap where circleId=:circleId order by prepaidYes, categoryLanguage, parentCategoryId, categoryIndex";
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Query query = session.createQuery(hql);
	    	query.setString("circleId", circleId);
	    	List<CircleCategoryMap> result = (List<CircleCategoryMap>)query.list();
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
	
	public static List<CircleCategoryMap> getCategoriesInCircle(int parentCategoryId, String circleId) throws DataAccessException {
		
		String hql = "from CircleCategoryMap where parentCategoryId=:parentCategoryId and circleId=:circleId order by prepaidYes, categoryLanguage, parentCategoryId, categoryIndex";
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Query query = session.createQuery(hql);
	    	query.setInteger("parentCategoryId", parentCategoryId);
	    	query.setString("circleId", circleId);
	    	List<CircleCategoryMap> result = (List<CircleCategoryMap>)query.list();
	        tx.commit();
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
	
	public static void main(String[] args) {
		try {
			System.out.println(getCategoriesInCircle(103, "bangalore"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String[]> getGroupByPrepaidYesCircleId() throws DataAccessException, SQLException{
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
//		List<CircleCategoryMap> result = null;
		List<String[]> result = null;
//		Transaction tx = null;
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql_query = "SELECT DISTINCT PREPAID_YES,CIRCLE_ID FROM RBT_CATEGORY_CIRCLE_MAP";
		try {
			connection = session.connection();
			stmt = connection.createStatement();
			rs  = stmt.executeQuery(sql_query);
			result = new ArrayList<String[]>();
			while(rs.next()){
				String[] resultString = new String[2];
				resultString[0] = rs.getString("PREPAID_YES");
				resultString[1] = rs.getString("CIRCLE_ID");
				result.add(resultString);
				
			}
				
			
//			tx = session.beginTransaction();
//
//			String hql = "SELECT categoryMap FROM CircleCategoryMap AS categoryMap"
//				+ " GROUP BY categoryMap.prepaidYes, categoryMap.circleId";
//			
//			Query query = session.createQuery(hql);
// 
//			result = query.list();
//			tx.commit();
			return result;
		} catch(SQLException sqle) {
			basicLogger.error("",sqle);
			throw sqle;
		} finally {
			if(null != rs){
				rs.close();
				rs = null;
			}
			if(null != stmt){
				stmt.close();
				stmt = null;
			}
			if(null !=  connection){
				connection.close();
				connection = null;
			}
			session.close();
		}
	}

	
	public static List<CircleCategoryMap> getCircleCategoryMap(String categoryId, String parentCategoryId, String cirleId, String prepaidYes) throws DataAccessException{
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(CircleCategoryMap.class);
	    	criteria.add(Restrictions.eq("categoryId", categoryId));
	    	criteria.add(Restrictions.eq("parentCategoryId", parentCategoryId));
	    	criteria.add(Restrictions.eq("circleId", cirleId));
	    	criteria.add(Restrictions.eq("prepaidYes", prepaidYes));
	    	List<CircleCategoryMap> result = (List<CircleCategoryMap>)criteria.list();
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

	/**
	 * Saves the circleCategoryMap in RBT_CATEGORY_CIRCLE_MAP table.
	 * @param circleCategoryMap
	 * @return the CircleCategoryMap object saved in the DB. 
	 * @throws DataAccessException
	 */
	public static CircleCategoryMap saveCircleCategoryMap(CircleCategoryMap circleCategoryMap) throws DataAccessException {

		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(circleCategoryMap);
			tx.commit();
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Saved circleCategoryMap " + circleCategoryMap + " in " + (System.currentTimeMillis() - start));
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
		start = System.currentTimeMillis();
		try {
//			RBTCache.putClipInCache(circleCategoryMap);
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated cache in " + (System.currentTimeMillis() - start));
			}
		} catch(Exception e) {
			//safety check
			basicLogger.error("Error updating the clip in cache " + circleCategoryMap);
		}
		return circleCategoryMap;
	}


	/**
	 * Updates the circleCategoryMap in RBT_CATEGORY_CIRCLE_MAP table.
	 * @param circleCategoryMap
	 * @return the CircleCategoryMap object updated in the DB. 
	 * @throws DataAccessException
	 */
	public static CircleCategoryMap updateCircleCategoryMap(CircleCategoryMap circleCategoryMap) throws DataAccessException {

		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(circleCategoryMap);
			tx.commit();
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated circleCategoryMap " + circleCategoryMap + " in " + (System.currentTimeMillis() - start));
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
		start = System.currentTimeMillis();
		try {
//			RBTCache.putClipInCache(circleCategoryMap);
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated cache in " + (System.currentTimeMillis() - start));
			}
		} catch(Exception e) {
			//safety check
			basicLogger.error("Error updating the clip in cache " + circleCategoryMap);
		}
		return circleCategoryMap;
	}

	/**
	 * Deletes the circleCategoryMap from RBT_CATEGORY_CIRCLE_MAP table.
	 * @param circleCategoryMap
	 * @return the CircleCategoryMap object deleted in the DB. 
	 * @throws DataAccessException
	 */
	public static CircleCategoryMap deleteCircleCategoryMap(CircleCategoryMap circleCategoryMap) throws DataAccessException {

		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(circleCategoryMap);
			tx.commit();
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Deleted circleCategoryMap " + circleCategoryMap + " in " + (System.currentTimeMillis() - start));
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
		start = System.currentTimeMillis();
		try {
//			RBTCache.putClipInCache(circleCategoryMap);
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated cache in " + (System.currentTimeMillis() - start));
			}
		} catch(Exception e) {
			//safety check
			basicLogger.error("Error updating the clip in cache " + circleCategoryMap);
		}
		return circleCategoryMap;
	}

	
	public static CircleCategoryMap[] saveOrUpdateCircleCategoryMap(CircleCategoryMap[] circleCategoryMap, Set<Integer> set, Set<String> circleSet) throws DataAccessException {

		long start = System.currentTimeMillis();		
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			for(int i=0;i<circleCategoryMap.length;i++){
				session.saveOrUpdate(circleCategoryMap[i]);
				set.add(circleCategoryMap[i].getCategoryId());
				circleSet.add(circleCategoryMap[i].getCircleId());
				if(basicLogger.isInfoEnabled()){
					basicLogger.info("Saved or Updated " + circleCategoryMap[i].toString());
				}
				if((i % 100)  == 0){
					session.flush();
					session.clear();
				}
			}			
			tx.commit();
			
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Saved circleCategoryMap " + circleCategoryMap + " in " + (System.currentTimeMillis() - start));
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
		return circleCategoryMap;
	}

	public static CircleCategoryMap[] deleteCircleCategoryMap(CircleCategoryMap[] deleteCircleCategoryMap, Set<Integer> set, Set<String> circleSet) throws DataAccessException {

		long start = System.currentTimeMillis();		
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
		
			for(int i = 0; i < deleteCircleCategoryMap.length; i++){
				session.delete(deleteCircleCategoryMap[i]);
				set.add(deleteCircleCategoryMap[i].getCategoryId());
				circleSet.add(deleteCircleCategoryMap[i].getCircleId());
				if(basicLogger.isInfoEnabled()){
					basicLogger.info("Deleted " + deleteCircleCategoryMap[i].toString());
				}
				if((i % 100)  == 0){
					session.flush();
					session.clear();
				}
			}
			
			tx.commit();
			
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Deleted circleCategoryMap  in " + (System.currentTimeMillis() - start));
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
		return deleteCircleCategoryMap;
	}

	public static List<CircleCategoryMap> getCircleCategoryMap(String catId) throws DataAccessException{
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(CircleCategoryMap.class);
	    	criteria.add(Restrictions.eq("categoryId", Integer.parseInt(catId)));
	    	List<CircleCategoryMap> result = (List<CircleCategoryMap>)criteria.list();
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
	/**
	 * Can add, update or delete a DB entry. Deletion is done if circleCategoryMap.deleteMap is set to true.
	 * Addition is done if the same entry is not present already and updation is done if it is already present.
	 * @param circleCategoryMap
	 * @throws DataAccessException
	 */
	public static void performDBOperations(CircleCategoryMap circleCategoryMap) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			if (circleCategoryMap.isDeleteMap()) {
				session.delete(circleCategoryMap);
				if(basicLogger.isInfoEnabled()){
					basicLogger.debug("Successfully Deleted from circleCategoryMap " + circleCategoryMap);
				}
			} else {
				session.saveOrUpdate(circleCategoryMap);
				if(basicLogger.isInfoEnabled()){
					basicLogger.debug("Successfully Saved/Updated circleCategoryMap " + circleCategoryMap);
				}
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
