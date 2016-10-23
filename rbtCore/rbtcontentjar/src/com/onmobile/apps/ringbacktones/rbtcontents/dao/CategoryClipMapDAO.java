package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryClipMap;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.TPHitUtils;

public class CategoryClipMapDAO {

	private static Logger basicLogger = Logger.getLogger(CategoryClipMap.class);
	
	/**
	 * Saves the category and clip mapping in RBT_CATEGORY_CLIP_MAP table.
	 * if clip index is <= 0, it is set to 998
	 * @param categoryClipMap
	 * @return CategoryClipMap object
	 * @throws DataAccessException
	 */
	public static CategoryClipMap saveCategoryClipMap(CategoryClipMap categoryClipMap) throws DataAccessException {
		long start = System.currentTimeMillis();
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	if(categoryClipMap.getClipIndex() <= 0) {
	    		categoryClipMap.setClipIndex(998);
	    	}
	    	session.save(categoryClipMap);
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Saved CategoryClipMap " + categoryClipMap + " in " + (System.currentTimeMillis() - start));
	        }
			updateCache(categoryClipMap);
			return categoryClipMap;
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
	 * Updates the CategoryClipMap.
	 * @param categoryClipMap
	 * @return
	 * @throws DataAccessException
	 * @throws {@link IllegalArgumentException} if the parameter categoryClipMap object is null or if the category id or clip id <= 0
	 */
	public static CategoryClipMap updateCategoryClipMap(CategoryClipMap categoryClipMap) throws DataAccessException {
		//validate the input parameter
		if(null == categoryClipMap) {
			throw new IllegalArgumentException("Param categoryClipMap cant be null");
		}
		if(categoryClipMap.getCategoryId() <= 0) {
			throw new IllegalArgumentException("categoryId should be > 0");
		}
		if(categoryClipMap.getClipId() <= 0) {
			throw new IllegalArgumentException("clipId should be > 0");
		}
		long start = System.currentTimeMillis();
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	if(categoryClipMap.getClipIndex() <= 0) {
	    		categoryClipMap.setClipIndex(998);
	    	}
	    	session.update(categoryClipMap);
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Updated CategoryClipMap " + categoryClipMap + " in " + (System.currentTimeMillis() - start));
	        }
			updateCache(categoryClipMap);
			return categoryClipMap;
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
	 * Delete the CategoryClipMap.
	 * @param categoryClipMap
	 * @return
	 * @throws DataAccessException
	 * @throws {@link IllegalArgumentException} if the parameter categoryClipMap object is null or if the category id or clip id <= 0
	 */
	public static CategoryClipMap deleteCategoryClipMap(CategoryClipMap categoryClipMap) throws DataAccessException {
		//validate the input parameter
		if(null == categoryClipMap) {
			throw new IllegalArgumentException("Param categoryClipMap cant be null");
		}
		if(categoryClipMap.getCategoryId() <= 0) {
			throw new IllegalArgumentException("categoryId should be > 0");
		}
		if(categoryClipMap.getClipId() <= 0) {
			throw new IllegalArgumentException("clipId should be > 0");
		}
		long start = System.currentTimeMillis();
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	if(categoryClipMap.getClipIndex() <= 0) {
	    		categoryClipMap.setClipIndex(998);
	    	}
	    	session.delete(categoryClipMap);
//	    	RBTCache.getMemCachedClient().delete(key)
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Deleted CategoryClipMap " + categoryClipMap + " in " + (System.currentTimeMillis() - start));
	        }
	        removeFromCache(categoryClipMap);
			return categoryClipMap;
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
	 * Gets the CategoryClipMap associated with categoryId and clipId
	 * @param categoryId
	 * @param clipId
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static CategoryClipMap getCategoryClipMap(int categoryId, int clipId) throws DataAccessException {
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	List<CategoryClipMap> result = null;
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(CategoryClipMap.class);
	    	criteria.add(Restrictions.eq("categoryId", new Integer(categoryId)));
	    	criteria.add(Restrictions.eq("clipId", new Integer(clipId)));
	    	result = (List<CategoryClipMap>)criteria.list();
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("CategoryClipMap object for category id " + categoryId + " clip id " + clipId + " in " + (System.currentTimeMillis() - start));
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
	/**
	 * Gets all the clips mapped in the category
	 * @param categoryId
	 * @return the list of CategoryClipMap objects
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static List<CategoryClipMap> getClipsInCategory(int categoryId) throws DataAccessException {
//		String query = "from CategoryClipMap where categoryId=:categoryId order by clipIndex";
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	List<CategoryClipMap> result = null;
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(CategoryClipMap.class);
	    	criteria.add(Restrictions.eq("categoryId", new Integer(categoryId)));
//	    	criteria.add(Restrictions.gt("clipIndex", 0));
	    	criteria.addOrder(Order.desc("clipInList"));
	    	criteria.addOrder(Order.asc("clipIndex"));
	    	result = (List<CategoryClipMap>)criteria.list();
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Got clips in category " + categoryId + " in " + (System.currentTimeMillis() - start));
	        }
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
	 * Gets all the categories in which this clip is present or mapped to. 
	 * @param clipId
	 * @return list of CategoryClipMap objects
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static List<CategoryClipMap> getCategoriesMappedToClip(int clipId) throws DataAccessException {
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	List<CategoryClipMap> result = null;
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Criteria criteria = session.createCriteria(CategoryClipMap.class);
	    	criteria.add(Restrictions.eq("clipId", new Integer(clipId)));
	    	result = (List<CategoryClipMap>)criteria.list();
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Got categories mapped to clip " + clipId + " in " + (System.currentTimeMillis() - start));
	        }
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

	private static void updateCache(CategoryClipMap categoryClipMap) {
		try {
			long start = System.currentTimeMillis();
			String[] clipsIdArray = (String [])RBTCache.getMemCachedClient().get(RBTCacheKey.getClipsInCategoryCacheKey(categoryClipMap.getCategoryId()));
			if(null == clipsIdArray || clipsIdArray.length <= 0) {
				return;
			}
			String[] newClipIdArray = new String[clipsIdArray.length+1];
			//put the clips in the first position
			newClipIdArray[0] = Integer.toString(categoryClipMap.getClipId());
			System.arraycopy(clipsIdArray, 0, newClipIdArray, 1, clipsIdArray.length);
//			System.arraycopy(clipsIdArray, offset, dest, 0, (endIndex - offset));
			RBTCache.getMemCachedClient().set(RBTCacheKey.getClipsInCategoryCacheKey(categoryClipMap.getCategoryId()), newClipIdArray);
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated cache in " + (System.currentTimeMillis() - start));
			}
		} catch(Exception e) {
			//safety check
			basicLogger.error("Error updating the CategoryClipMap in cache " + categoryClipMap, e);
		}

	}

	private static void updateCache(CategoryClipMap categoryClipMap[]) {
		try {
			long start = System.currentTimeMillis();
			if(categoryClipMap == null){
				return;
			}
			
			Set<String> set = new HashSet<String>();
			for(int i=0;i<categoryClipMap.length;i++){
				String catId = String.valueOf(categoryClipMap[i].getCategoryId());
				set.add(catId);
			}
			Iterator<String> iterator = set.iterator();
			while(iterator.hasNext()){
				String catId = iterator.next();
				RBTCache.refreshCategory(catId);
			}
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated cache in " + (System.currentTimeMillis() - start));
			}
		} catch(Exception e) {
			//safety check
			basicLogger.error("Error updating the CategoryClipMap in cache " + categoryClipMap, e);
		}

	}
	
	public static void main(String[] args) {
//		testGetCategoryClipMap(1001, 1002);
//		testSaveCategoryClipMap();
		
//		testUpdateCategoryClipMap();
		try {
			System.out.println(getClipsInCategory(1024).size());
		} catch(DataAccessException dae) {
			dae.printStackTrace();
		}
	}
	
//	private static void testSaveCategoryClipMap() {
//		try {
//			CategoryClipMap categoryClipMap = new CategoryClipMap();
//			categoryClipMap.setCategoryId(1001);
//			categoryClipMap.setClipId(1004);
//			saveCategoryClipMap(categoryClipMap);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
	private static void testGetCategoryClipMap(int categoryId, int clipId) {
		try {
			CategoryClipMap categoryClipMap = getCategoryClipMap(categoryId, clipId);
//			System.out.println(categoryClipMap);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	private static void removeFromCache(CategoryClipMap categoryClipMap) {
		try {
			long start = System.currentTimeMillis();
			String[] clipsIdArray = (String [])RBTCache.getMemCachedClient().get(RBTCacheKey.getClipsInCategoryCacheKey(categoryClipMap.getCategoryId()));
			if(null == clipsIdArray || clipsIdArray.length <= 0) {
				return;
			}
			String[] newClipIdArray = new String[clipsIdArray.length-1];
			//put the clips in the first position
			boolean status = false;
			String clipId = RBTCacheKey.getClipIdCacheKey(categoryClipMap.getClipId());
			int j = 0;
			try{
				for(int i=0;i<clipsIdArray.length;i++){
					if(clipsIdArray[i].equals(clipId)){
						status = true;
					}
					else{						
						newClipIdArray[j] = clipsIdArray[i];
						j++;
					}
				}
			}
			catch(Exception e){
				status = false;
			}
			if(status){
				RBTCache.getMemCachedClient().set(RBTCacheKey.getClipsInCategoryCacheKey(categoryClipMap.getCategoryId()), newClipIdArray);
			}
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated cache in " + (System.currentTimeMillis() - start));
			}
		} catch(Exception e) {
			//safety check
			basicLogger.error("Error deleting the CategoryClipMap in cache " + categoryClipMap, e);
		}		
	}

	private static void removeCategoryClipFromCache(int categoryId){
		try {
			long start = System.currentTimeMillis();
			RBTCache.getMemCachedClient().delete(RBTCacheKey.getClipsInCategoryCacheKey(categoryId));
			RBTCache.getMemCachedClient().delete(RBTCacheKey.getActiveClipsInCategoryCacheKey(categoryId));
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated cache in " + (System.currentTimeMillis() - start));
			}
		} catch(Exception e) {
			//safety check
			basicLogger.error("Error deleting the Category from ClipMap in cache " + categoryId, e);
		}
	}
	
	/**
	 * delete all the clips mapped in the category
	 * @param categoryId
	 * @return the list of CategoryClipMap objects
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static List<CategoryClipMap> deleteAllCategoryClipMap(int categoryId) throws DataAccessException {
		String hql = "delete from RBT_CATEGORY_CLIP_MAP where CATEGORY_ID="+categoryId;//categoryId=:categoryId";
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	List<CategoryClipMap> result = null;
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Query query = session.createSQLQuery(hql);
//	    	query.setInteger(1, categoryId);
	    	query.executeUpdate();
//	    	Criteria criteria = session.createCriteria(CategoryClipMap.class);
//	    	criteria.add(Restrictions.eq("categoryId", new Integer(categoryId)));
//	    	result = (List<CategoryClipMap>)criteria.list();
	        tx.commit();
	        removeCategoryClipFromCache(categoryId);
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Remove cateogry in clip map " + categoryId + " in " + (System.currentTimeMillis() - start));
	        }
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
	 * Saves the category and clips mapping in RBT_CATEGORY_CLIP_MAP table.
	 * if clip index is <= 0, it is set to 998
	 * @param categoryClipMap[]
	 * @throws DataAccessException
	 */
	
	public static CategoryClipMap[] saveCategoryClipMap(CategoryClipMap[] catClipMap) throws DataAccessException {
		long start = System.currentTimeMillis();
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	for(int i=0;catClipMap != null && i<catClipMap.length;i++){
	    		CategoryClipMap categoryClipMap = catClipMap[i];
		    	if(categoryClipMap.getClipIndex() <= 0) {
		    		categoryClipMap.setClipIndex(998);
		    	}		    	
		    	if(basicLogger.isDebugEnabled()) {
		        	basicLogger.debug("Saved CategoryClipMap " + categoryClipMap + " in " + (System.currentTimeMillis() - start));
		        }
		    	session.save(categoryClipMap);
	    	}
	        tx.commit();
	        
			updateCache(catClipMap);
			return catClipMap;
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

	public static void updateCategoryClipMap(String hqlQuery) throws DataAccessException{
		long start = System.currentTimeMillis(); 
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	Query query = session.createSQLQuery(hqlQuery);
	    	query.executeUpdate();
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Upldate clip in list value as null where category clip index is null in " + (System.currentTimeMillis() - start));
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

	public static CategoryClipMap[] saveOrUpdateCategoryClipMap(CategoryClipMap[] catClipMap, Set<Integer> set) throws DataAccessException {
		long start = System.currentTimeMillis();
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	for(int i=0;catClipMap != null && i<catClipMap.length;i++){
	    		CategoryClipMap categoryClipMap = catClipMap[i];
		    	if(categoryClipMap.getClipIndex() <= 0) {
		    		categoryClipMap.setClipIndex(998);
		    	}		    	
		    	session.saveOrUpdate(categoryClipMap);
		    	if(basicLogger.isInfoEnabled()){
		    		basicLogger.info("Successfully save or updated CategoryClipMap " + categoryClipMap);
		    	}
		    	if((i % 100) == 0){
		    		session.flush();
		    		session.clear();
		    	}
		    	set.add(categoryClipMap.getCategoryId());
	    	}
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Saved bulk CategoryClipMap in " + (System.currentTimeMillis() - start));
	        }
			return catClipMap;
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

	public static CategoryClipMap[] deleteCategoryClipMap(CategoryClipMap[] catClipMap, Set<Integer> set) throws DataAccessException {
		long start = System.currentTimeMillis();
    	Session session = HibernateUtil.getSession();
    	Transaction tx = null;
		try {
	    	tx = session.beginTransaction();
	    	for(int i=0;catClipMap != null && i<catClipMap.length;i++){
	    		CategoryClipMap categoryClipMap = catClipMap[i];		    			    	
		    	session.delete(categoryClipMap);
		    	if(basicLogger.isInfoEnabled()){
		    		basicLogger.debug("Successfully Deleted from CategoryClipMap " + categoryClipMap);
		    	}
		    	if((i % 100) == 0){
		    		session.flush();
		    		session.clear();
		    	}
		    	set.add(categoryClipMap.getCategoryId());
	    	}
	        tx.commit();
	        if(basicLogger.isDebugEnabled()) {
	        	basicLogger.debug("Deleted bulk CategoryClipMap in " + (System.currentTimeMillis() - start));
	        }
			return catClipMap;
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
	 * Can add, update or delete a DB entry. Deletion is done if categoryClipMap.deleteMap is set to true.
	 * Addition is done if the same entry is not present already and updation is done if it is already present.
	 * @param categoryClipMap
	 * @throws DataAccessException
	 */
	public static void performDBOperations(CategoryClipMap categoryClipMap) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			if (categoryClipMap.isDeleteMap()) {
				session.delete(categoryClipMap);
				if(basicLogger.isInfoEnabled()){
					basicLogger.debug("Successfully Deleted from CategoryClipMap " + categoryClipMap);
				}
			} else {
				session.saveOrUpdate(categoryClipMap);
				if(basicLogger.isInfoEnabled()){
					basicLogger.debug("Successfully Saved/Updated CategoryClipMap " + categoryClipMap);
				}
			}
			tx.commit();
			TPHitUtils.updateCorrespondingCategorySetForTPHit(categoryClipMap);
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
	//	
//	private static void testUpdateCategoryClipMap() {
//		try {
//			CategoryClipMap categoryClipMap = new CategoryClipMap();
//			categoryClipMap.setCategoryId(1001);
//			categoryClipMap.setClipId(0);
//			categoryClipMap.setClipIndex(999);
//			categoryClipMap.setClipInList('y');
//			updateCategoryClipMap(categoryClipMap);
//			System.out.println(categoryClipMap);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
}
