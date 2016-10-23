package com.onmobile.apps.ringbacktones.rbtcontents.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryInfo;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Circle;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.CategoryThreadManager;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.CircleThreadManager;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.GenericCacheThread;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoriesDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CircleDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.ContentRefreshTCPClient;

public class RBTContentRefresh {

	public static Logger log = Logger.getLogger(RBTContentRefresh.class);
	
	private static String NO_OF_THREADS = "no_of_threads";


	// number of threads to be created to update the memcache
	private static int noOfThreads = 5;
	

	/**
	 * Initializes memcache with multiple threads
	 * @throws Exception
	 */
	public static void refershContent(Set<Integer> categoryIds, Set<String> circleIds) throws Exception {
		log.info("refreshContent started");
		try {
			noOfThreads = Integer.parseInt(RBTContentJarParameters.getInstance().getParameter(NO_OF_THREADS));
		} catch (NumberFormatException nfe) {
			log.error("Invalid value entered for parameter '" + NO_OF_THREADS + "'. So using default 5.");
		}
		try {
			int categoryIdsSize = 0;
			int circleIdsSize = 0;
			if(categoryIds == null || categoryIds.size() == 0){
				log.info("List of category id size is zero");
			}
			else{
				categoryIdsSize = categoryIds.size();
			}
			if(circleIds == null || circleIds.size() == 0){
				log.info("List of circle id size is zero");
			}
			else{
				circleIdsSize = circleIds.size();
			}
			
			List<Category> categories = new ArrayList<Category>();
			if(categoryIdsSize > 0){
				Iterator<Integer> itr = categoryIds.iterator();
				while(itr.hasNext()){
					Category category = CategoriesDAO.getCategory(itr.next().toString());
					if(category != null){
//						System.out.println(category.toString());
						Set<CategoryInfo> info = category.getCategoryInfoSet();
						if(info == null){
//							System.out.println("CategoryInfo object is null");
							return;			
						}
						
						Iterator<CategoryInfo> itrcatinfo = info.iterator();
						while(itrcatinfo.hasNext()){
							CategoryInfo obj = itrcatinfo.next();
//							System.out.println(obj.toString());
						}
						categories.add(category);
					}
				}
			}
			
			List<Circle> circles = new ArrayList<Circle>();
			if(circleIdsSize > 0){
				Iterator<String> itr = circleIds.iterator();
				while(itr.hasNext()){
					Circle circle = CircleDAO.getCircle(itr.next());
					if(circle != null){
						circles.add(circle);
					}
				}
			}
			
			
			long l2 = System.currentTimeMillis();
			log.info("Initializing the categories cache...");
			initCategoryCache(categories);
			long l3 = System.currentTimeMillis();
			log.info("Initializing the category clip map cache...");
			initCircleCategoryCache(circles);
			long l4 = System.currentTimeMillis();
			RBTCache.getMemCachedClient().set(RBTCache.MC_IS_CACHE_INITIALIZED_FLAG, System.currentTimeMillis());
			log.info("Done");
			long l6 = System.currentTimeMillis();
			log.info("Total time: TT: " + (l6 - l2) + " Categories:"
					+ (l3 - l2) + " Circles:" + (l4 - l3) );
			ContentRefreshTCPClient.sendMessageToAllClients(null);
		} catch (Exception e) {
			log.error("Error while caching the records", e);
			throw e;
		} finally {
			try{
				RBTCache.shutDown();
			}
			catch(Exception e){
				
			}
		}
	}

	/**
	 * Initializes the categories cache. Also initializes the clips mapped to the category.
	 * @throws Exception 
	 */
	private static void initCategoryCache(List<Category> categories) throws Exception {

		if(categories == null){
			log.info("Category list is Null");
		}
		
		long now = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("Getting the categories from db..");
		}
	
		if(log.isInfoEnabled()) {
			log.info("Total no of categories: " + categories.size());
			log.info("Initializing the categories cache..");
		}
		
		
		
		CategoryThreadManager ctm = new CategoryThreadManager(categories, noOfThreads);
		try {
			ctm.startThreads();
		} catch (Exception e) {
			GenericCacheThread.stopThreads();
			log.error("Exception while starting the CategoryThreads or processing");
			throw e;
		}
		if(log.isInfoEnabled()) {
			log.info("Initialized the categories cache in " + (System.currentTimeMillis() - now) + "ms.");
		}
	}

	/**
	 * Initializes the categories mapped in a circle
	 * @throws Exception 
	 */
	private static void initCircleCategoryCache(List<Circle> circles) throws Exception {

		if(circles == null){
			log.info("Circle list is Null");
		}
		long now = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("Getting the circles from db..");
		}
		// get the circles from db
		if(log.isInfoEnabled()) {
			log.info("Total no of circles: " + circles.size());
			log.info("Initializing the categories in circle cache..");
		}
		
		CircleThreadManager ctm = new CircleThreadManager(circles, noOfThreads);
		try {
			ctm.startThreads();
		} catch (Exception e) {
			GenericCacheThread.stopThreads();
			log.error("Exception while starting the CircleThreads or processing");
			throw e;
		}

		if(log.isInfoEnabled()) {
			log.info("Initialized the circle categories cache in " + (System.currentTimeMillis() - now) + "ms.");
		}
	}

}
