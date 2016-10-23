package com.onmobile.apps.ringbacktones.rbtcontents.cache;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Circle;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipBoundary;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.PromoMaster;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.CategoryThreadManager;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.CircleThreadManager;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.ClipThreadManager;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.GenericCacheThread;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.PromoMasterThreadManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoriesDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoryClipMapDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CircleDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.PromoMasterDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.ContentRefreshTCPClient;

public class RBTMultiThreadInitCache {

	public static Logger log = Logger.getLogger(RBTMultiThreadInitCache.class);
	
	private static final String Q_UPDATE_MAP_IN_LIST = 
		"UPDATE RBT_CATEGORY_CLIP_MAP SET CLIP_IN_LIST = 'n' WHERE CLIP_IN_LIST IS NULL";
	
	private static final String Q_UPDATE_MAP_INDX = 
		"UPDATE RBT_CATEGORY_CLIP_MAP SET CATEGORY_CLIP_INDEX = 999 WHERE CATEGORY_CLIP_INDEX IS NULL";
	
	// parameter to specify maximum number of clips to be read from DB at a time
	private static String NO_OF_CLIPS_PER_ITERATION = "no_of_clips_per_iteration";

	// parameter to specify number of threads to be created to update the memcache
	private static String NO_OF_THREADS = "no_of_threads";

	// maximum number of clips to be read from DB at a time
	private static int noOfClipsPerIteration = 100000;

	// number of threads to be created to update the memcache
	private static int noOfThreads = 5;
	
	// to make sure at a time only one
	private static boolean isProcessing = false;

	/**
	 * Initializes memcache with multiple threads
	 * @throws Exception
	 */
	public static void init() throws Exception {
		initConfig();
		try {
			isProcessing = true;
			CategoryClipMapDAO.updateCategoryClipMap(Q_UPDATE_MAP_IN_LIST);
			CategoryClipMapDAO.updateCategoryClipMap(Q_UPDATE_MAP_INDX);
			log.info("Initializing the clips cache...");
			long l1 = System.currentTimeMillis();
			initClipsCache();
			long l2 = System.currentTimeMillis();
			log.info("Initializing the categories cache...");
			initCategoryCache();
			long l3 = System.currentTimeMillis();
			log.info("Initializing the category clip map cache...");
			initCircleCategoryCache();
			long l4 = System.currentTimeMillis();
			log.info("Initializing the promo master cache...");
			initPromoMasterCache();
			long l5 = System.currentTimeMillis();
			RBTCache.getMemCachedClient().set(RBTCache.MC_IS_CACHE_INITIALIZED_FLAG, System.currentTimeMillis());
			log.info("Done");
//			RBTCache.shutDown();
			long l6 = System.currentTimeMillis();
			if(null!=RBTContentJarParameters.getInstance().getParameter("artist.Indexing")){
				System.out.println("Initializing the clips artist cache...");
				log.info("Got required parameter for initializing clip artist cache...");
				initClipsArtistCache();
			}
			log.info("Total time: TT: " + (l6 - l1) + " Clips:" + (l2 - l1) + " Categories:"
					+ (l3 - l2) + " Circles:" + (l4 - l3) + " PromoMaster:" + (l5 - l4));
			ContentRefreshTCPClient.sendMessageToAllClients(null);
		} catch (Exception e) {
			log.error("Error while caching the records", e);
			throw e;
		} finally {
			isProcessing = false;
		}
	}

	public static void initConfig() {
		try {
			noOfClipsPerIteration = Integer.parseInt(RBTContentJarParameters.getInstance()
					.getParameter(NO_OF_CLIPS_PER_ITERATION));
		} catch (NumberFormatException nfe) {
			log.error("Invalid value entered for parameter '" + NO_OF_CLIPS_PER_ITERATION + "'");
			throw nfe;
		}
		try {
			noOfThreads = Integer.parseInt(RBTContentJarParameters.getInstance().getParameter(NO_OF_THREADS));
		} catch (NumberFormatException nfe) {
			log.error("Invalid value entered for parameter '" + NO_OF_THREADS + "'. So using default 5.");
		}
	}

	/**
	 * Initializes clips cache
	 * @throws Exception
	 */
	public static void initClipsCache() throws Exception {
		long now = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("calculating clip boundaries..");
		}
		// get the clip-id boundaries.
		// requirement is that to get limited number of rows. sapdb will not
		// support this, so defining some boundaries
		TreeSet<ClipBoundary> clipBoundaries = ClipsDAO.getClipBoundariesUsingBinaryAlg(noOfClipsPerIteration);
		clipBoundaries = mergeClipBoundaries(clipBoundaries);
		if(log.isInfoEnabled()) {
			log.info("ClipBoundaries: " + clipBoundaries);
			log.info("Initializing the clips cache..");
		}
		long bds = System.currentTimeMillis();
		Iterator<ClipBoundary> itr = clipBoundaries.iterator();
		ClipThreadManager ctm = new ClipThreadManager(noOfThreads);
		while (itr.hasNext()) {
			ClipBoundary clipBoundary = itr.next();
			log.info("Processing clip boundary " + clipBoundary);
			ctm.setClipBoundary(clipBoundary);
			try {
				ctm.startThreads();
			} catch (Exception e) {
				GenericCacheThread.stopThreads();
				log.error("Exception while starting the ClipThreads or processing");
				throw e;
			}
		}
		long last = System.currentTimeMillis();
		log.info("Clips cache time:" + (last-now) + " boundaries time:" + (bds-now) + " caching time:" + (last-bds));
		if(log.isInfoEnabled()) {
			log.info("Initialized the clips cache in " + (System.currentTimeMillis() - now) + "ms.");
		}
	}
	
	/**
	 * Merges the consecutive clips boundaries if the sum of the ranges is less than allowed clips per iteration. 
	 * @param clipBoundaries
	 * @return
	 */
	private static TreeSet<ClipBoundary> mergeClipBoundaries(TreeSet<ClipBoundary> clipBoundaries) {
		TreeSet<ClipBoundary> result = new TreeSet<ClipBoundary>(ClipBoundary.getClipBoundaryComparator());
		ClipBoundary cbPrev = null;
		for (ClipBoundary cb: clipBoundaries) {
			if(cbPrev != null) {
				if (noOfClipsPerIteration >= (cbPrev.getCount() + cb.getCount())) {
					cbPrev.setEndIndex(cb.getEndIndex());
					cbPrev.setCount(cbPrev.getCount() + cb.getCount());
					clipBoundaries.remove(cb);
				} else {
					cbPrev = cb;
					result.add(cb);
				}
			} else {
				cbPrev = cb;
				result.add(cb);
			}
		}
		return result;
	}
	
	/**
	 * Initializes the categories cache. Also initializes the clips mapped to the category.
	 * @throws Exception 
	 */
	public static void initCategoryCache() throws Exception {

		long now = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("Getting the categories from db..");
		}
		List<Category> categories = CategoriesDAO.getAllCategories();
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
	
	public static void initClipsArtistCache() throws DataAccessException {

		long now = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("Getting the clips from db..");
		}
		List<String> clipsArtistList = ClipsDAO.getAllClipsArtist();
		long dbtask = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("Total no of artists found: " + clipsArtistList.size());
			log.info("Initializing the clips cache..");
		}
	
		putClipArtistInCache(clipsArtistList);
		
		long last = System.currentTimeMillis();
		log.info("TT: " + (last-now) + " db:" + (dbtask-now) + " cache:" + (last-dbtask));
		if(log.isInfoEnabled()) {
			log.info("Initialized the clips cache in " + (System.currentTimeMillis() - now) + "ms.");
		}
	}
	
	public static void putClipArtistInCache(List<String> clipArtistList) {
		if(null!=clipArtistList && clipArtistList.size()>0){
			RBTCache.getMemCachedClient().set("ARTIST_LIST", clipArtistList);
		}
		
		for(String artist : clipArtistList) {
			if(null==artist || (artist = artist.trim()).length()==0){
				continue;
			}
			Character cr = artist.trim().charAt(0);
			String artistInitialKey = RBTCacheKey.getArtistNameInitialCacheKey(cr);
			for(char i=65;i<=90;i++){
				log.info("In For Alphabet:"+i);
				String listMappedAlphas = RBTContentJarParameters.getInstance().getParameter("label.map.alphabet."+i);
				if(listMappedAlphas!=null){
					if (listMappedAlphas.indexOf(cr) != -1) {
						log.info("Going to hit for character:"+cr);
						TreeSet<String> artistIndexSet = (TreeSet<String>)RBTCache.getMemCachedClient().get(artistInitialKey);	
						if(artistIndexSet==null){
							artistIndexSet = new TreeSet<String>();		
						}
						artistIndexSet.add(artist);
						RBTCache.getMemCachedClient().set(artistInitialKey, artistIndexSet);
						break;
					}
				}
			}
		}
	}


	/**
	 * Initializes the categories mapped in a circle
	 * @throws Exception 
	 */
	public static void initCircleCategoryCache() throws Exception {

		long now = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("Getting the circles from db..");
		}
		// get the circles from db
		List<Circle> circles = CircleDAO.getAllCircles();

		String rrbtCircleIdStr = RBTContentJarParameters.getInstance().getParameter("RRBT_CONTENT_CIRCLE_IDS");
		if (rrbtCircleIdStr != null)
		{
			String[] circleIDs = rrbtCircleIdStr.trim().split(",");
			for (String circleID : circleIDs)
			{
				Circle circle = new Circle();
				circle.setCircleId(circleID.trim());
				circle.setCircleName(circleID.trim());

				circles.add(circle);
			}
		}

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

	/**
	 * Initializes the promo master table
	 * @throws Exception 
	 */
	public static void initPromoMasterCache() throws Exception {

		long now = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("Getting the promo master from db..");
		}
		List<PromoMaster> promoMasters = PromoMasterDAO.getAllPromoMasters();
		if(log.isInfoEnabled()) {
			log.info("Total no of promoMasters: " + promoMasters.size());
			log.info("Initializing the promoMasters in cache..");
		}
		PromoMasterThreadManager pmtm = new PromoMasterThreadManager(promoMasters, noOfThreads);
		try {
			pmtm.startThreads();
		} catch (Exception e) {
			GenericCacheThread.stopThreads();
			log.error("Exception while starting the PromoMasterThreads or processing");
			throw e;
		}
		if(log.isInfoEnabled()) {
			log.info("Initialized the promo master cache in " + (System.currentTimeMillis() - now) + "ms.");
		}
	}
	
	public static boolean isUnderProcess() {
		return isProcessing;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		long l1 = System.currentTimeMillis();
		init();
		RBTCache.shutDown();
		log.info("Total time: " + (System.currentTimeMillis() - l1) + "ms");
	}
	
}