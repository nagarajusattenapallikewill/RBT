package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category.CategoryInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryInfo;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CircleCategoryMap;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.RBTContentUtils;

public class CategoriesDAO {

	private static Logger basicLogger = Logger.getLogger(CategoriesDAO.class);

	public static Category saveCategory(Category category) throws DataAccessException {
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			String update_mm_number = RBTContentJarParameters.getInstance().getParameter("update_mm_number");
			if("true".equalsIgnoreCase(update_mm_number) &&
					(category.getCategoryTpe() == 0 || category.getCategoryTpe() == 13)){
				category.setMmNumber(category.getCategoryPromoId());
			}
			session.save(category);
			tx.commit();
			//	        System.out.println("Time: " + (System.currentTimeMillis() - start));
			return category;
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

	public static List<Category> getAllActiveCategories() throws DataAccessException {
		return getAllActiveCategories(null);
	}
	
	public static List<Category> getAllActiveCategories(String language) throws DataAccessException {

		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Category.class);
			criteria = criteria.add(Restrictions.lt("categoryStartTime", new Date()));
			criteria = criteria.add(Restrictions.gt("categoryEndTime", new Date()));
			//do i have to add the clip id in (select distinct clip id from category_clip_map)

			
			List<Category> result1 = (List<Category>)criteria.list();
			Set catSet = new LinkedHashSet<Category>(result1);
			List<Category> result = new ArrayList<Category>(catSet);

			tx.commit();
			for(int i=0; i<result.size(); i++){
				Category category = result.get(i);
				category = getCategoryForLanguage(category, language);
				result.remove(i);
				result.add(i, category);
			}
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

	public static List<Category> getAllCategories() throws DataAccessException {
		return getAllCategories(null);
	}
	
	public static List<Category> getAllCategories(String language) throws DataAccessException {

		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Category.class);
			
		    List<Category> result1 = (List<Category>)criteria.list();
			Set catSet = new LinkedHashSet<Category>(result1);
			List<Category> result = new ArrayList<Category>(catSet);
			tx.commit();
			
			if(language!=null && !language.equals("")){
				for(int i=0; i<result.size(); i++){
					Category category = result.get(i);
					category = getCategoryForLanguage(category, language);
					result.remove(i);
					result.add(i, category);
				}
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

	private static List<Integer> getParentCategoryIds(Category category) throws DataAccessException {
		basicLogger.debug("Inside getParentCategoryIds");
		List<Integer> parentCatIds = null;
		if (category == null) {
			basicLogger.debug("Category is: "+category);
			return parentCatIds;
		}
		parentCatIds = new LinkedList<Integer>();
		int catId = category.getCategoryId();
		String parentCatIdName = "parentCategoryId";
		String catIdName = "categoryId";
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		Criteria criteria = null;
		try {
			tx = session.beginTransaction();
			criteria = session.createCriteria(CircleCategoryMap.class);
			criteria.add(Restrictions.eq(catIdName, catId))
			.setProjection(Projections.projectionList()
					.add(Projections.property(parentCatIdName)));
			
			
			List result = criteria.list();
			tx.commit();
			Iterator iterator =  result.iterator();
			while (iterator.hasNext()) {
				int parentCatId = (Integer) iterator.next();
				parentCatIds.add(parentCatId);
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
		return parentCatIds;
	}
	
	private static void updateParentCategories(int catId) throws DataAccessException {
		basicLogger.debug("Inside updateParentCategories");
		Category category = getCategory(catId+"");
		if (category == null) {
			basicLogger.debug("Parent Category: "+category);
			return;
		}
		basicLogger.debug("Parent Category: "+category);
		category.setLastModifiedTime(new Date());
		
		Session session = HibernateUtil.getSession();
		Transaction tx = null;		
		try {
			tx = session.beginTransaction();
			session.update(category);
			tx.commit();
			return;
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
	
	
	public static void updateCategory(Category category) throws DataAccessException {
		long start = System.currentTimeMillis();
		//RBT-14540
		if (RBTContentUtils.isParentToBeUpdated()) {
			basicLogger.debug("Parent shud be updated");
			List<Integer> parentCatIds = getParentCategoryIds(category);
			if (parentCatIds != null)
				for (Integer parentCatId : parentCatIds) {
					basicLogger.debug("Parent_CATEGORY_ID: "+parentCatId);
					updateParentCategories(parentCatId);
				}
		} 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			String update_mm_number = RBTContentJarParameters.getInstance().getParameter("update_mm_number");
			if("true".equalsIgnoreCase(update_mm_number) &&
					(category.getCategoryTpe() == 0 || category.getCategoryTpe() == 13)){
				category.setMmNumber(category.getCategoryPromoId());
			}
			//category.setLastModifiedTime(new Date());
			session.update(category);
			tx.commit();
			//	        System.out.println("Time: " + (System.currentTimeMillis() - start));
			return;
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

	public static Category getCategory(String categoryId) throws DataAccessException {
		return getCategory(categoryId, null);
	}
	
	public static Category getCategory(String categoryId, String language) throws DataAccessException {
		//		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Category category = (Category)session.get(Category.class, Integer.parseInt(categoryId));
			tx.commit();
			category = getCategoryForLanguage(category, language);
			return category;
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
	public static Category getCatgegoryByPromoId(String promoId) throws DataAccessException {
		return getCatgegoryByPromoId(promoId, null);
	}
	
	@SuppressWarnings("unchecked")
	public static Category getCatgegoryByPromoId(String promoId, String language) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		Category category = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Category.class);
//			criteria = criteria.add(Restrictions.eq("categoryPromoId", promoId));
			criteria = criteria.add(Restrictions.ilike("categoryPromoId", promoId, MatchMode.ANYWHERE));
			List<Category> result = (List<Category>)criteria.list();
			if(null != result && result.size() > 0) {
				for (Category tempCategory : result) {
					String[] categoryPromoCode = tempCategory.getCategoryPromoId().split(",");
					for (int index = 0; index < categoryPromoCode.length; index++) {
						if (categoryPromoCode[index].equalsIgnoreCase(promoId)) {
							category = tempCategory;
							break;
						}
					}
				}
//				category = result.get(0);
			}
			tx.commit();
			category = getCategoryForLanguage(category, language);
			return category;
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
	public static Category getCategoryByMMNumber(String mmNumber) throws DataAccessException {
		return getCategoryByMMNumber(mmNumber, null);
	}
	
	@SuppressWarnings("unchecked")
	public static Category getCategoryByMMNumber(String mmNumber, String language) throws DataAccessException {
		//		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		Category category = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Category.class);
			criteria = criteria.add(Restrictions.eq("mmNumber", mmNumber));
			List<Category> result = (List<Category>)criteria.list();
			if(null != result && result.size() > 0) {
				category = result.get(0);
			}
			tx.commit();
			category = getCategoryForLanguage(category, language);
			return category;
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

	public static Category getCategoryBySmsAlias(String smsAlias) throws DataAccessException {
		return getCategoryBySmsAlias(smsAlias, null);
	}

	/**
	 * In database, SmsAlias can be configured as 'meet,meeting,meets'. The
	 * record fetching happens on like restriction. Here, ilike will work for
	 * case insensitive.
	 * 
	 * @param smsAlias
	 * @param language
	 * @return
	 * @throws DataAccessException
	 */
	public static Category getCategoryBySmsAlias(String smsAlias, String language) throws DataAccessException {
		//		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		Category category = null;
		StringBuffer sb = new StringBuffer("%%");
		sb.insert(1, smsAlias);
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Category.class);
			criteria = criteria.add(Restrictions.ilike("categorySmsAlias", sb.toString()));
			List<Category> result = (List<Category>)criteria.list();
			if(null != result && result.size() > 0) {
				category = result.get(result.size()-1);
			}
			tx.commit();
			category = getCategoryForLanguage(category, language);
			return category;
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

	public static Category getCategoryByName(String catName) throws DataAccessException {
		return getCategoryByName(catName, null);
	}
	
	public static Category getCategoryByName(String catName, String language) throws DataAccessException {
		//		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		Category category = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Category.class);
			criteria = criteria.add(Restrictions.eq("categoryName", catName));
			List<Category> result = (List<Category>)criteria.list();
			if(null != result && result.size() > 0) {
				category = result.get(result.size()-1);
			}
			tx.commit();
			category = getCategoryForLanguage(category, language);
			return category;
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

	public static List<Category> getCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String language) throws DataAccessException {
		return getCategoriesInCircle(circleId,parentCategoryId, prepaidYes, language, null );
	}
	
	public static List<Category> getCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String language, String categoryLanguage) throws DataAccessException {
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		List<Category> result = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			String hql = "SELECT category FROM Category AS category, CircleCategoryMap AS categoryMap"
				+ " WHERE categoryMap.parentCategoryId = " + parentCategoryId
				+ " AND categoryMap.circleId = '" + circleId + "'"
				+ " AND categoryMap.prepaidYes = '" + prepaidYes + "'";
			if(language != null)	
				hql += " AND categoryMap.categoryLanguage = '" + language + "'";
			
			hql += " AND categoryMap.categoryId = category.categoryId"
					+ " ORDER BY categoryMap.categoryIndex";

			Query query = session.createQuery(hql);

			List<Category> result1 = (List<Category>)query.list();
			Set catSet = new LinkedHashSet<Category>(result1);
			result = new ArrayList<Category>(catSet);

			tx.commit();
			for(int i=0; i<result.size(); i++){
				Category category = result.get(i);
				category = getCategoryForLanguage(category, categoryLanguage);
				result.remove(i);
				result.add(i, category);
			}
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got categories in parentCategoryId "
						+ parentCategoryId + ", circleId = " + circleId
						+ " and prepaidYes = " + prepaidYes + " in "
						+ (System.currentTimeMillis() - start));
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

	public static List<Category> getActiveCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String language) throws DataAccessException {
		return getActiveCategoriesInCircle(circleId, parentCategoryId,prepaidYes,  language, null);
	}
	
	public static List<Category> getActiveCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String language, String categoryLanguage) throws DataAccessException {
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		List<Category> result = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			String hql = "SELECT category FROM Category AS category, CircleCategoryMap AS categoryMap"
				+ " WHERE categoryMap.parentCategoryId = " + parentCategoryId
				+ " AND categoryMap.circleId = '" + circleId + "'"
				+ " AND categoryMap.prepaidYes = '" + prepaidYes + "'";
			if(language != null)
				hql += " AND categoryMap.categoryLanguage = '" + language + "'";
			
			hql += " AND categoryMap.categoryId = category.categoryId"
					+ " AND category.categoryStartTime < :sysdate AND category.categoryEndTime > :sysdate"
					+ " ORDER BY categoryMap.categoryIndex";

			Query query = session.createQuery(hql);
			query.setTimestamp("sysdate", new Date());

			
			List<Category> result1 = (List<Category>)query.list();
			Set catSet = new LinkedHashSet<Category>(result1);
			result = new ArrayList<Category>(catSet);

			tx.commit();
			for(int i=0; i<result.size(); i++){
				Category category = result.get(i);
				category = getCategoryForLanguage(category, categoryLanguage);
				result.remove(i);
				result.add(i, category);
			}
			
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got categories in parentCategoryId "
						+ parentCategoryId + ", circleId = " + circleId
						+ " and prepaidYes = " + prepaidYes + " in "
						+ (System.currentTimeMillis() - start));
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

	public static List<Category> getCategoryType(String categoryType) throws DataAccessException{
		return getCategoryType(categoryType, null);
	}
	
	public static List<Category> getCategoryType(String categoryType, String language) throws DataAccessException{
		
		List<Category> result = null;
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			String hql = "from Category where categoryTpe = " +categoryType;		

			Query query = session.createQuery(hql);

			List<Category> result1 = (List<Category>)query.list();
			Set catSet = new LinkedHashSet<Category>(result1);
			result = new ArrayList<Category>(catSet);

			tx.commit();
			for(int i=0; i<result.size(); i++){
				Category category = result.get(i);
				category = getCategoryForLanguage(category, language);
				result.remove(i);
				result.add(i, category);
			}
			
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got categories in category type "
						+ categoryType + " in "
						+ (System.currentTimeMillis() - start));
			}
			RBTCache.getMemCachedClient().set(RBTCacheKey.getCategoryTypeCacheKey(categoryType), result);
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
	
	
	public static List<Category> getCategoryType(String circleId, char prepaidYes,String categoryType) throws DataAccessException{
		return getCategoryType(circleId, prepaidYes, categoryType, null);
	}
	
	public static List<Category> getCategoryType(String circleId, char prepaidYes,String categoryType, String language) throws DataAccessException{
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		List<Category> result = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			String hql = "SELECT category FROM Category AS category, CircleCategoryMap AS categoryMap"
				+ " WHERE categoryMap.circleId = '" + circleId + "'"
				+ " AND categoryMap.prepaidYes = '" + prepaidYes + "'"
				+ " AND category.categoryTpe = " + categoryType
				+ " AND categoryMap.categoryId = category.categoryId";


			Query query = session.createQuery(hql);

			List<Category> result1 = (List<Category>)query.list();
			Set catSet = new LinkedHashSet<Category>(result1);
			result = new ArrayList<Category>(catSet);

			tx.commit();
			for(int i=0; i<result.size(); i++){
				Category category = result.get(i);
				category = getCategoryForLanguage(category, language);
				result.remove(i);
				result.add(i, category);
			}
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got categories in circleId = " + circleId
						+ " and prepaidYes = " + prepaidYes
						+ " and categoryType = " + categoryType + " in "
						+ (System.currentTimeMillis() - start));
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

	public static List<Category> getCategoryByPromoId(String circleId, char prepaidYes,String promoId) throws DataAccessException{
		return getCategoryByPromoId(circleId, prepaidYes, promoId, null);
	}
	
	public static List<Category> getCategoryByPromoId(String circleId, char prepaidYes,String promoId, String language) throws DataAccessException{
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		List<Category> result = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

//			String hql = "SELECT DISTINCT category FROM Category AS category, CircleCategoryMap AS categoryMap"
//				+ " WHERE categoryMap.circleId = '" + circleId + "'"
//				+ " AND categoryMap.prepaidYes = '" + prepaidYes + "'"
//				+ " AND lower(category.categoryPromoId) = '" + promoId.toLowerCase() + "'"
//				+ " AND categoryMap.categoryId = category.categoryId";

			String hql = "SELECT DISTINCT category FROM Category AS category, CircleCategoryMap AS categoryMap"
				+ " WHERE categoryMap.circleId = '" + circleId + "'"
				+ " AND categoryMap.prepaidYes = '" + prepaidYes + "'"
				+ " AND lower(category.categoryPromoId) like '%" + promoId.toLowerCase() + "%'"
				+ " AND categoryMap.categoryId = category.categoryId";
			
			Query query = session.createQuery(hql);

			List<Category> tempResult = (List<Category>)query.list();
			Set catSet = new LinkedHashSet<Category>(tempResult);
			result = new ArrayList<Category>(catSet);
			tempResult = null;
			tempResult = new ArrayList<Category>();
			tx.commit();
			for (int i = 0; i < result.size(); i++) {
				Category category = result.get(i);
//				category = getCategoryForLanguage(category, language);
//				result.remove(i);
//				result.add(i, category);
				String[] categoryPromoCode = category.getCategoryPromoId().split(",");
				for (int index = 0; index < categoryPromoCode.length; index++) {
					if (categoryPromoCode[index].equalsIgnoreCase(promoId)) {
						category = getCategoryForLanguage(category, language);
						result.remove(i);
						tempResult.add(category);
						break;
					}
				}
			}
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got categories in circleId = " + circleId
						+ " and prepaidYes = " + prepaidYes
						+ " and categoryPromoId = " + promoId + " in "
						+ (System.currentTimeMillis() - start));
			}
			return tempResult;
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

	
	public static List<Category> getCategoryByCategoryType(String circleId, String prepaidYes) throws DataAccessException {
		return getCategoryByCategoryType(circleId, prepaidYes, null );
	}
	
	public static List<Category> getCategoryByCategoryType(String circleId, String prepaidYes, String language) throws DataAccessException {
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		List<Category> result = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			String hql = "SELECT category FROM Category AS category, CircleCategoryMap AS categoryMap"
				+ " WHERE categoryMap.circleId = '" + circleId + "'"
				+ " AND categoryMap.prepaidYes = '" + prepaidYes + "'"
				+ " AND categoryMap.categoryId = category.categoryId";

			Query query = session.createQuery(hql);

			List<Category> result1 = (List<Category>)query.list();
			Set catSet = new LinkedHashSet<Category>(result1);
			result = new ArrayList<Category>(catSet);

			tx.commit();
			for(int i=0; i<result.size(); i++){
				Category category = result.get(i);
				category = getCategoryForLanguage(category, language);
				result.remove(i);
				result.add(i, category);
			}
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got categories in circleId = " + circleId
						+ " and prepaidYes = " + prepaidYes + " in "
						+ (System.currentTimeMillis() - start));
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

	
	public static void main(String[] args) throws DataAccessException {
		
		Category[] cat = new Category[1];
		Category category1 = CategoriesDAO.getCategory("766");
		//Category category2 = CategoriesDAO.getCategory("766");
		//System.out.println(category);
		cat[0] = category1;
		//cat[1] = category2;
		cat[0].setCategoryName("Halla Bol");
		//cat[1].setCategoryName("BBAA");
		Set<Integer> set = new HashSet<Integer>();
		CategoriesDAO.saveOrUpdateCatgory(cat,set);
	}

	public static Category[] saveOrUpdateCatgory(Category[] category, Set<Integer> set) throws DataAccessException{
		long start = System.currentTimeMillis(); 
		
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		List<Integer> parentCatIds = null;
		boolean isParentToBeUpdated = RBTContentUtils.isParentToBeUpdated();
		Set<Integer> setOfParentCatIds = null;
		if (isParentToBeUpdated)
			setOfParentCatIds = new HashSet<Integer>();
		try {
			tx = session.beginTransaction();
			for(int i =0;i<category.length;i++){			
				Category cat = CategoriesDAO.getCategory(Integer.toString(category[i].getCategoryId()));
				String update_mm_number = RBTContentJarParameters.getInstance().getParameter("update_mm_number");
				if(cat == null){
					cat = category[i];
					if("true".equalsIgnoreCase(update_mm_number) &&
							(cat.getCategoryTpe() == 0 || cat.getCategoryTpe() == 13)){
						cat.setMmNumber(cat.getCategoryPromoId());
					}
					session.save(category[i]);
					basicLogger.info("Successfully saved category " + category[i].toString());
				}
				else{

					if("true".equalsIgnoreCase(update_mm_number) &&
							(category[i].getCategoryTpe() == 0 || category[i].getCategoryTpe() == 13)){
						category[i].setMmNumber(cat.getCategoryPromoId());
					}
					//RBT-14540
					if (isParentToBeUpdated) {
						parentCatIds = getParentCategoryIds(category[i]);
						if (parentCatIds != null)
							setOfParentCatIds.addAll(parentCatIds);
					} 		
					session.update(category[i]);
					basicLogger.info("Successfully updated category " + category[i].toString());
				}
				set.add(category[i].getCategoryId());
				if((i % 100) == 0) {
					session.flush();
					session.clear();
					
				}
			}
			
			tx.commit();
			
			if (setOfParentCatIds != null)
				for (Integer parentCatId : setOfParentCatIds) {
					updateParentCategories(parentCatId);
				}
			
			return category;
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

	public static List<Category> getCategories(List<Integer> categoryIds) throws DataAccessException {
		return getCategories(categoryIds, null);
	}
	
	public static List<Category> getCategories(List<Integer> categoryIds, String language) throws DataAccessException {
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("FROM Category WHERE categoryId IN (:categoryId)");
			query.setParameterList("categoryId", categoryIds);
			
			List<Category> result1 = (List<Category>)query.list();
			Set catSet = new LinkedHashSet<Category>(result1);
			List<Category> rsList = new ArrayList<Category>(catSet);

			tx.commit();
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got all the clips in " + (System.currentTimeMillis() - start));
			}
			Map<Integer,Category> categoryMap = new HashMap<Integer, Category>();
			int size = rsList.size();
			for(int i=0;i<size;i++){
				Category category = rsList.get(i);
				categoryMap.put(category.getCategoryId(),category);
			}
			size = categoryIds.size();
			List<Category> result = new ArrayList<Category>();
			for(int i=0;i<size;i++){
				if(categoryMap.containsKey(categoryIds.get(i))){
					result.add(i,categoryMap.get(categoryIds.get(i)));
				}
				else{
					result.add(i,null);
				}
			}
			for(int i=0; i<result.size(); i++){
				Category category = result.get(i);
				category = getCategoryForLanguage(category, language);
				result.remove(i);
				result.add(i, category);
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
	
	public static Category getCategoryForLanguage(Category category, String language){
		if(category==null)
			return null;
		String defaultLanguage = RBTContentJarParameters.getInstance().getParameter("default_language");
		String[] supportedLanguages = null;
		if(RBTContentJarParameters.getInstance().getParameter("supported_languages")!=null && !(RBTContentJarParameters.getInstance().getParameter("supported_languages").equals(""))){
			supportedLanguages = RBTContentJarParameters.getInstance().getParameter("supported_languages").split(",");
		}
		String origCategoryName = category.getCategoryName();
		String origCategoryGrammar = category.getCategoryGrammar();
		String origCategoryInfo = category.getCategoryInfo();
        ArrayList<String> keysList = new ArrayList<String>();
        String origContentType = null;
        String origKey = null;
        CategoryInfoKeys[] keys = CategoryInfoKeys.values();
        for(int i=0;i<keys.length;i++)
        {
                keysList.add(keys[i].toString());
                basicLogger.info("adding the key to key list"+keys[i].toString());
        }
		//-------- Set a temp variable for categoryInfoSet
		Set<CategoryInfo> tempCategoryInfoSet = category.getCategoryInfoSet();
//		if(language==null || language.equals("") || language.equalsIgnoreCase(defaultLanguage)){
//			//-----------Return for default language
//			category.setCategoryInfoSet(null);
//			return category;
//		}
		
		category.setCategoryLanguage(defaultLanguage);
		if(tempCategoryInfoSet == null && (language==null || language.equals("") || language.equalsIgnoreCase(defaultLanguage) || 
				(supportedLanguages!=null && supportedLanguages.length>0 && !RBTContentJarParameters.getInstance().getParameter("supported_languages").contains(language)))){
			category.setCategoryInfoSet(null);
			return category;
		}
		/* Set for all languages as well as particular language
		*  For all languages the key will be categoryid_<CATEGORY_ID>_ALL, in this case the set will be null
		*  For particular language the key will be categoryid_<CATEGORY_ID>_LANG, in this case the map will be null
		*/
		category.setCategoryInfoSet(tempCategoryInfoSet);
		
		//------Populate the CategoryInfoMap to get all language info
		if(category.getCategoryInfoSet()!=null && category.getCategoryInfoSet().size()>0){
			Iterator<CategoryInfo> itr = category.getCategoryInfoSet().iterator();
			while(itr.hasNext()){
				CategoryInfo categoryInfo = itr.next();
				Map<String, String> categoryInfoMap = category.getCategoryInfoMap();
				if(categoryInfoMap==null)
					categoryInfoMap = new HashMap<String, String>();
				categoryInfoMap.put(categoryInfo.getName(), categoryInfo.getValue());
				category.setCategoryInfoMap(categoryInfoMap);
			}
			/* Set for all languages
			*  CatetgoryInfoset is not required in case of all languages
			*/
			 Map<String, String> categoryInfoMap = category.getCategoryInfoMap();
             Map<String, String> categoryMap = new HashMap<String,String>();
             if(categoryInfoMap!=null){
            	 for(int j=0;j<keysList.size();j++)
            	 {
            		 if(categoryInfoMap.containsKey(keysList.get(j))){
            			 origContentType = categoryInfoMap.get(keysList.get(j));
            			 origKey = keysList.get(j);
            			 if(origKey!=null&&origContentType!=null){
            				 categoryMap.put(origKey, origContentType);
            			 }
            		 }
            		 basicLogger.debug("adding into the map " + origKey+" " + origContentType );

            	 }
             }
			category.setCategoryInfoSet(null);
			if(language!=null && language.equalsIgnoreCase("ALL")){
				return category;
			}
			
			 if(language==null || language.equals("") || language.equalsIgnoreCase(defaultLanguage) || language.equalsIgnoreCase("ALL") ||
                      (supportedLanguages!=null && supportedLanguages.length>0 &&
                                      !RBTContentJarParameters.getInstance().getParameter("supported_languages").contains(language))){
              return category;

			 }
			//-------- Again set the CatetgoryInfoSet
			category.setCategoryInfoSet(tempCategoryInfoSet);
			//--------- CatetgoryInfoMap is not required in case of specific languages
            category.setCategoryInfoMap(categoryMap);
			//------- Set for supported languages
			if(supportedLanguages!=null && supportedLanguages.length>0){
				for(int i=0; i<supportedLanguages.length; i++){
					Iterator<CategoryInfo> categoryInfoitr = category.getCategoryInfoSet().iterator();
					//-------- Set information on the Catetgory bean for the passed language from the CatetgoryInfoSet
					category.setCategoryName(null);
					category.setCategoryGrammar(null);
					category.setCategoryInfo(null);
					category.setCategoryLanguage(defaultLanguage);
					while(categoryInfoitr.hasNext()){
						CategoryInfo categoryInfo = categoryInfoitr.next();
//						if(!categoryInfo.getName().endsWith(supportedLanguages[i].trim().toUpperCase())){
//							continue;
//						}
						if(categoryInfo.getName().equalsIgnoreCase(RBTCacheKey.getCategoryNameLanguageKey(supportedLanguages[i].toUpperCase()))){
							category.setCategoryName(categoryInfo.getValue());
						}
						if(categoryInfo.getName().equalsIgnoreCase(RBTCacheKey.getCategoryGrammarLanguageKey(supportedLanguages[i].toUpperCase()))){
							category.setCategoryGrammar(categoryInfo.getValue());
						}
						if(categoryInfo.getName().equalsIgnoreCase(RBTCacheKey.getCategoryInfoLanguageKey(supportedLanguages[i].toUpperCase()))){
							category.setCategoryInfo(categoryInfo.getValue());
						}
						if(categoryInfo.getName().equalsIgnoreCase(RBTCacheKey.getCategoryInfoDescKey(supportedLanguages[i].toUpperCase()))){
							categoryMap.put(Category.CategoryInfoKeys.CAT_DESC.name(), categoryInfo.getValue());
						}
					}
					
					boolean isSupportLanguageAvail = true;
					
					if(category.getCategoryName()==null || category.getCategoryName().equals("")) {
						category.setCategoryName(origCategoryName);
						isSupportLanguageAvail = false;
					}
					if(category.getCategoryGrammar()==null || category.getCategoryGrammar().equals("")) category.setCategoryGrammar(origCategoryGrammar);
					if(category.getCategoryInfo()==null || category.getCategoryInfo().equals("")) category.setCategoryInfo(origCategoryInfo);
					if(language!=null && language.equalsIgnoreCase(supportedLanguages[i])) {
						if(isSupportLanguageAvail)
							category.setCategoryLanguage(supportedLanguages[i]);
						return category;
					}
				}
			}
		}
		return category;
	}

}
