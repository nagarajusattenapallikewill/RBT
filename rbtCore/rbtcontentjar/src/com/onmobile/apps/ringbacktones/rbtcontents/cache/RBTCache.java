package com.onmobile.apps.ringbacktones.rbtcontents.cache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import com.onmobile.apps.ringbacktones.content.database.ClipStatusImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryClipMap;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Circle;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CircleCategoryMap;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.PromoMaster;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.RBTCacheRefreshThread;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoriesDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoryClipMapDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CircleCategoryMapDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CircleDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.PromoMasterDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.ContentRefreshTCPClient;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.TPHitUtils;

/**
 * @author ganipisetty
 * This is main class to create and update the cache.
 */
public class RBTCache {

	public static Logger basicLogger = Logger.getLogger(RBTCache.class);

	//	private static RBTCache instance = null;
	//	private static boolean initialized = false;
	private static MemCachedClient mc = null;
	private static SockIOPool pool = null; 
	private static boolean isCacheAlive = true;
	private static boolean isCacheInitialized = false;
	private static boolean shutdown = true;

	public static String MC_IS_CACHE_INITIALIZED_FLAG = "IsCacheInitialized";
	public static String MC_HEART_BEAT_FLAG = "RBTCacheHeartBeatThread";
	private static final String REFRESH_MEMCACHE = "refresh_memcache";

	private static RBTCacheHeartBeatThread heartBeatThread = null;

	private static HashMap<String, Set<String>> categoryTypeMap = new HashMap<String, Set<String>>();
	
	public static Set<String> odaCategoryIdsForTPHit = new HashSet<String>();
	public static Set<CategoryClipMap> radioCategoryClipMapsForTPHit = new HashSet<CategoryClipMap>();
	public static Set<CategoryClipMap> azaanCopticDoaaCategoryClipMapsForTPHit = new HashSet<CategoryClipMap>();
	public static Set<CategoryClipMap> festivalNameTuneCategoryClipMapsForTPHit = new HashSet<CategoryClipMap>();
	public static Set<Integer> playListCategoryIdsForTPHit = new HashSet<Integer>();
	
	static {
		try {
			initMemcachePool();
			String refreshMemcache = RBTContentJarParameters.getInstance().getParameter(REFRESH_MEMCACHE);
			if (refreshMemcache != null) {
				refreshMemcache = refreshMemcache.trim();
			}
			basicLogger.info("Flag " + REFRESH_MEMCACHE + " is set to '" + refreshMemcache + "'");
			if ("yes".equalsIgnoreCase(refreshMemcache)) {
				basicLogger.info("Getting RBTCacheRefreshThread instance.");
				// to refresh memcache in regular intervals
				RBTCacheRefreshThread refreshThread = RBTCacheRefreshThread.getInstance();
				basicLogger.info("RBTCacheRefreshThread going to start");
				refreshThread.start();
				basicLogger.info("RBTCacheRefreshThread has been started");
			}
		} catch (Throwable e) {
			basicLogger.error("Error while initializing the MemCachedClient", e);
			// to avoid class loading, throwing unchecked exception
			throw new RuntimeException(e);
		}
	}
	
	public static void initMemcachePool() throws IllegalAccessException {
		if (!shutdown) {
			throw new IllegalAccessException(
					"Reinitialzation is not acceptable without shutting down the existing connection pool");
		}
		basicLogger.warn("Initilizing the MemCachedClient object");
		String memCachedServerList = RBTContentJarParameters.getInstance().getParameter("memcached_serverlist");
		String minConn = RBTContentJarParameters.getInstance().getParameter("minimum_connections");
		String maxConn = RBTContentJarParameters.getInstance().getParameter("maximum_connections");
		String memCacheTimeout = RBTContentJarParameters.getInstance().getParameter("memcache_socket_timeout");
		String memCacheInitTimeout = RBTContentJarParameters.getInstance().getParameter("memcache_socket_init_timeout");
		
		String poolName = RBTContentJarParameters.getInstance().getParameter("pool_name");
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("MemCachedServer list " + memCachedServerList);
		}
		String[] serverlist = memCachedServerList.split(",");
		if(poolName != null)
			pool = SockIOPool.getInstance(poolName);
		else
			pool = SockIOPool.getInstance();
		pool.setServers(serverlist);
		pool.initialize();
		pool.setInitConn(Integer.parseInt(minConn));
		pool.setMinConn(Integer.parseInt(minConn));
		pool.setMaxConn(Integer.parseInt(maxConn));
		if(null != memCacheTimeout) {
			try {
				int socketTimeout = Integer.parseInt(memCacheTimeout);
				pool.setSocketTO(socketTimeout);
			} catch (Exception e) {
				basicLogger.error("Unable to parse memcache_socket_timeout."
						+ " Exception: " + e.getMessage(), e);
			}
		}
		if(null != memCacheInitTimeout) {
			try {
				int socketInitTimeout = Integer.parseInt(memCacheInitTimeout);
				pool.setSocketTO(socketInitTimeout);
			} catch (Exception e) {
				basicLogger.error("Unable to parse memcache_socket_init_timeout."
						+ " Exception: " + e.getMessage(), e);
			}
		}
		
		if(poolName != null)
			mc = new MemCachedClient(poolName);
		else
			mc = new MemCachedClient();
		//		mc.add(MC_HEART_BEAT_FLAG, MC_HEART_BEAT_FLAG);
		heartBeatThread = new RBTCacheHeartBeatThread();
		heartBeatThread.start();

		checkCacheInitialized();
		
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("MemCachedClient is initialized");
		}
		shutdown = false;
	}

	public static boolean isCacheAlive() {
		return isCacheAlive;
	}

	public static boolean isCacheInitialized() {
		return isCacheInitialized;
	}
	
	private static void checkCacheInitialized()
	{
		boolean success = RBTCache.getMemCachedClient().set(MC_HEART_BEAT_FLAG, MC_HEART_BEAT_FLAG);
		if(success) {
			RBTCache.isCacheAlive = true;
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Checking if cache is initialized");
			}
			Long isCacheInitialized = (Long)RBTCache.getMemCachedClient().get(RBTCache.MC_IS_CACHE_INITIALIZED_FLAG);
			if(null != isCacheInitialized) {
				RBTCache.isCacheInitialized = true;
				if(basicLogger.isDebugEnabled()) {
					basicLogger.debug("Cache is initialized");
				}
			} else {
				RBTCache.isCacheInitialized = false;
				if(basicLogger.isDebugEnabled()) {
					basicLogger.error("Cache is not initialized");
				}
			}
		} else {
			basicLogger.error("ContentCache is not up!!! Please check...");
			RBTCache.isCacheAlive = false;
		}
	}

	public static void shutDown() {
		basicLogger.warn("Stop down the MemCachedHeartBeatThread...");
		heartBeatThread.stopThread();
		basicLogger.warn("Closing the sockets of MemCached client...");
		pool.shutDown();
		basicLogger.warn("Shutdown normally");
		shutdown = true;
	}
	
	public static boolean isShutdown() {
		return shutdown;
	}

	protected RBTCache() {
		//		try {
		//			long now = System.currentTimeMillis();
		//			//cache the clips
		//			initClipsCache();
		//			//cache the categories & category, clip map
		//			initCategoryCache();
		//			//cache the circles
		//			initCircleCategoryCache();
		//			System.out.println("Cache is created in " + (System.currentTimeMillis() - now) + "ms");
		//		} catch(DataAccessException dae) {
		//			dae.printStackTrace();
		//		}
	}

	public static void initClipsCache() throws DataAccessException {

		long now = System.currentTimeMillis();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Getting the clips from db..");
		}
		List<Clip> clips = ClipsDAO.getAllClips();
		long dbtask = System.currentTimeMillis();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Total no of clips: " + clips.size());
			basicLogger.info("Initializing the clips cache..");
		}
		for(int i=0; i<clips.size(); i++) {
			Clip clip = clips.get(i);
			putClipInCache(clip);
		}
		long last = System.currentTimeMillis();
		basicLogger.info("TT: " + (last-now) + " db:" + (dbtask-now) + " cache:" + (last-dbtask));
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Initialized the clips cache in " + (System.currentTimeMillis() - now) + "ms.");
		}
	}
	
	public static void initClipsArtistCache() throws DataAccessException {

		long now = System.currentTimeMillis();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Getting the clips from db..");
		}
		List<String> clipsArtistList = ClipsDAO.getAllClipsArtist();
		long dbtask = System.currentTimeMillis();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Total no of artists found: " + clipsArtistList.size());
			basicLogger.info("Initializing the clips cache..");
		}
	
		putClipArtistInCache(clipsArtistList);
		
		long last = System.currentTimeMillis();
		basicLogger.info("TT: " + (last-now) + " db:" + (dbtask-now) + " cache:" + (last-dbtask));
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Initialized the clips cache in " + (System.currentTimeMillis() - now) + "ms.");
		}
	}

	public static Clip refreshClip(String clipId) throws DataAccessException {
		Clip clip = null;
		try {
			clip = ClipsDAO.getClip(clipId);
		} catch(DataAccessException dae) {
			basicLogger.error(dae);
		}
		if(null == clip) {
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Clip not found in db with clip id " + clipId);
			}
		} else {
			//put the clip in cache
			RBTCache.putClipInCache(clip);
		}
		return clip;
	}

	public static void putClipInCache(Clip clip) {
		if(null == clip) {
			throw new IllegalArgumentException("The parameter clip can't be null");
		}
		mc.set(RBTCacheKey.getClipIdCacheKey(clip.getClipId()), clip);
		if(null != clip.getClipPromoId() && clip.getClipPromoId().length() > 0) {
			mc.set(RBTCacheKey.getPromoIdCacheKey(clip.getClipPromoId()), "" + clip.getClipId());
		}
		if(null != clip.getClipRbtWavFile() && clip.getClipRbtWavFile().length() > 0) {
			mc.set(RBTCacheKey.getRbtWavFileCacheKey(clip.getClipRbtWavFile()), "" + clip.getClipId());
		}
		if(null != clip.getClipSmsAlias() && clip.getClipSmsAlias().length() > 0) {
//			mc.set(RBTCacheKey.getSmsAliasCacheKey(clip.getClipSmsAlias()), "" + clip.getClipId());
			String[] smsAlias = getMultipleSmsAlias(clip.getClipSmsAlias());
			for(int i=0; i<smsAlias.length; i++){
				mc.set(RBTCacheKey.getSmsAliasCacheKey(smsAlias[i]), "" + clip.getClipId());
			}
		}

		String clipAlbum = clip.getAlbum();
		
		//Only for Esia (UGC Clips) subscriber id will be stored in album column
		if(null != clipAlbum && clipAlbum.length() > 0) {
//			long lAlbum = 0l;
			try{
				Long.parseLong(clipAlbum);
				putClipInAlbumCache(clipAlbum,clip.getClipId());
			} catch(NumberFormatException ne){
				// ignore
			}			
		}

	}
	
	public static void putClipArtistInCache(List<String> clipArtistList) {
		if(null!=clipArtistList && clipArtistList.size()>0){
			mc.set("ARTIST_LIST", clipArtistList);
		}
		
		for(String artist : clipArtistList) {
			Character cr = artist.trim().charAt(0);
			String artistInitialKey = RBTCacheKey.getArtistNameInitialCacheKey(cr);
			for(char i=65;i<=90;i++){
				basicLogger.info("In For Alphabet:"+i);
				String listMappedAlphas = RBTContentJarParameters.getInstance().getParameter("label.map.alphabet."+i);
				if(listMappedAlphas!=null){
					if (listMappedAlphas.indexOf(cr) != -1) {
						basicLogger.info("Going to hit for character:"+String.valueOf(cr));
						@SuppressWarnings("unchecked")
						TreeSet<String> artistIndexSet = (TreeSet<String>)mc.get(artistInitialKey);	
						if(artistIndexSet==null){
							artistIndexSet = new TreeSet<String>();		
						}
						artistIndexSet.add(String.valueOf(cr).toUpperCase());
						mc.set(artistInitialKey, artistIndexSet);
						break;
					}
				}
			}
		}
	}

	public static void putClipInAlbumCache(String album,int clipId){
		String albumKey = RBTCacheKey.getAlbumCacheKey(album);
		String clipIds = (String) mc.get(albumKey);
		if(clipIds == null){
			mc.set(albumKey, RBTCacheKey.getClipIdCacheKey(clipId));
		}
		else{
			String sClipId = RBTCacheKey.getClipIdCacheKey(clipId);
			mc.set(albumKey, clipIds + "," + sClipId);
		}		
	}

	/**
	 * Initializes the categories cache. Also initializes the clips mapped to the category.
	 * @throws DataAccessException
	 */
	public static void initCategoryCache() throws DataAccessException {	

		basicLogger.info("Starting initCategoryCache.");
		
		RBTMultiThreadInitCache.initConfig();
		try {
			long l1 = System.currentTimeMillis();
			basicLogger.info("Initializing the categories cache...");
			RBTMultiThreadInitCache.initCategoryCache();
			long l2 = System.currentTimeMillis();
			basicLogger.info("Initializing the category clip map cache...");
			RBTMultiThreadInitCache.initCircleCategoryCache();
			long l3 = System.currentTimeMillis();
			basicLogger.info("Total time: TT: " + (l3 - l1) + " Categories:"
					+ (l2 - l1) + " Circles:" + (l3 - l1));
		} catch (Exception e) {
			basicLogger.error(e,e);
			throw new DataAccessException(e.getMessage());
		}
		
		/*		long now = System.currentTimeMillis();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Getting the categories from db..");
		}
		List<Category> categories = CategoriesDAO.getAllCategories();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Total no of categories: " + categories.size());
			basicLogger.info("Initializing the categories cache..");
		}

		for(int i=0; i<categories.size(); i++) {
			Category category = categories.get(i);
			putCategoryAndCategoryClipsInCache(category);
		}

		putCategoryTypeinCache(null);

		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Initialized the categories cache in " + (System.currentTimeMillis() - now) + "ms.");
		}*/

		basicLogger.info("Starting TP updations.");
		try {
			TPHitUtils.updateODACategoryInTP();
			updateClipsStatusForCategories();
			
			TPHitUtils.updateRadioCategoryInTP();
			TPHitUtils.updateAzaanCopticDoaaCategoryInTP();
			TPHitUtils.updateFestivalNameTuneCategoryInTP();
			TPHitUtils.updateProvisioningRequestsForODA();
		} catch (Exception e) {
			basicLogger.error("Exception caught!" + e, e);
		}
		odaCategoryIdsForTPHit.clear();
		radioCategoryClipMapsForTPHit.clear();
		azaanCopticDoaaCategoryClipMapsForTPHit.clear();
		festivalNameTuneCategoryClipMapsForTPHit.clear();
		playListCategoryIdsForTPHit.clear();
	}

	private static void updateClipsStatusForCategories() {
		String isCategoryToBeInsertedInClipStatusTable = RBTContentJarParameters
				.getInstance()
				.getParameter(
						"is_category_entry_to_be_inserted_in_clip_status_table");
		if (isCategoryToBeInsertedInClipStatusTable != null
				&& isCategoryToBeInsertedInClipStatusTable
						.equalsIgnoreCase("true")) {
			basicLogger.info("is_category_entry_to_be_inserted_in_clip_status_table is true");
			Map<String, String> pendingCircles = RBTDBManager.getInstance()
					.getPendingCirclesOfCategoryEntries();
			Set<String> keySet = pendingCircles.keySet();
			if (keySet != null) {
				for (String key : keySet) {
					String categoryId = key.substring(4); // length of "cat_" is 4.
					if (odaCategoryIdsForTPHit.contains(categoryId)) {
						String circleString = pendingCircles.get(key);
						String circles[] = circleString.split(",");
						for (String circle : circles) {
							Map<String, String> reverseCircleMap = ClipStatusImpl.getReverseCircleIdMap();
							if (reverseCircleMap != null) {
								circle = reverseCircleMap.get(circle);
							} else {
								basicLogger.error("Circle map config (CIRCLES_INTEGER_MAPPING_FOR_CLIP_STATUS) mising! Returning.");
								return;
							}
							Clip[] clips = RBTCacheManager.getInstance()
									.getActiveClipsInCategory(
											Integer.parseInt(categoryId));
							if (clips != null) {
								for (Clip clip : clips) {
									RBTDBManager.getInstance()
											.checkAndInsertClipWithStatus(
													clip.getClipRbtWavFile(),
													circle, 0);
								}
							} else {
								basicLogger.debug("No clips found within the categoryId: " + categoryId);
							}
						}
					}
				}
			} else {
				basicLogger.debug("No category entries found.");
			}
		} else {
			basicLogger.debug("is_category_entry_to_be_inserted_in_clip_status_table is either not present or false");
		}
	}

	public static void putCategoryAndCategoryClipsInCache(Category category) throws DataAccessException {
		mc.set(RBTCacheKey.getCategoryIdCacheKey(category.getCategoryId()), category);

		mc.set(RBTCacheKey.getCategoryNameCacheKey(category.getCategoryName()), ""+category.getCategoryId());

		if(category.getCategoryPromoId() != null && category.getCategoryPromoId().length() > 0){
			mc.set(RBTCacheKey.getCategoryPromoIdCacheKey(category.getCategoryPromoId()), "" + category.getCategoryId());
		}

		if(category.getMmNumber() != null && category.getMmNumber().length() > 0){
			mc.set(RBTCacheKey.getCategoryMMNumberCacheKey(category.getMmNumber()), "" + category.getCategoryId());
		}

		if(category.getCategorySmsAlias() != null && category.getCategorySmsAlias().length() > 0){
//			mc.set(RBTCacheKey.getCategorySmsAliasCacheKey(category.getCategorySmsAlias()), ""+category.getCategoryId());
			String[] smsAlias = getMultipleSmsAlias(category.getCategorySmsAlias());
			for(int i=0; i<smsAlias.length; i++){
				mc.set(RBTCacheKey.getCategorySmsAliasCacheKey(smsAlias[i]), "" + category.getCategoryId());
			}
		}

		//get clips mapped in the circle
		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Initializing clips in category cache: " + category.getCategoryName());
		}
		List<CategoryClipMap> clipsInCategory = CategoryClipMapDAO.getClipsInCategory(category.getCategoryId());
		putCategoryClipInCache(clipsInCategory, category.getCategoryId());
//		mc.set(RBTCacheKey.getClipsInCategoryCacheKey(category.getCategoryId()), CategoryClipMap.getClipIdsArray(clipsInCategory));
//		mc.set(RBTCacheKey.getActiveClipsInCategoryCacheKey(category.getCategoryId()), CategoryClipMap.getActiveClipIdsArray(clipsInCategory));
		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Initialized the clips in category cache");
		}
		
		String categoryType = Integer.toString(category.getCategoryTpe()).trim();
		if(categoryTypeMap.containsKey(categoryType)){
			Set<String> set = categoryTypeMap.get(categoryType);
			set.add(RBTCacheKey.getCategoryIdCacheKey(category.getCategoryId()));
			categoryTypeMap.put(categoryType, set);
		}
		else{
			Set<String> set = new HashSet<String>();
			set.add(RBTCacheKey.getCategoryIdCacheKey(category.getCategoryId()));
			categoryTypeMap.put(categoryType, set);
		}
			
	}

	/**
	 * Initializes the categories mapped in a circle
	 * @throws DataAccessException
	 */
	public static void initCircleCategoryCache() throws DataAccessException, SQLException {

		long now = System.currentTimeMillis();
		HashMap<String, Set<String>> catTypeCircleIdMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> catPromoIdCircleIdMap = new HashMap<String, Set<String>>();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Getting the circles from db..");
		}
		//		TODO: get the circles from db
		List<Circle> circles = CircleDAO.getAllCircles();
		//		List<Circle> circles = getCircles();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Total no of circles: " + circles.size());
			basicLogger.info("Initializing the categories in circle cache..");
		}
		for(int i=0; i<circles.size(); i++) {
			Circle circle = circles.get(i);
			putCircleAndCircleCategoryInCache(circle, catTypeCircleIdMap, catPromoIdCircleIdMap);
		}

		//Put the category ids based on category type, prepaid yes, circle id.
		putMapInCache(catTypeCircleIdMap);
		//Put the category ids based on category promoid, prepaid yes, circle id.
		putMapInCache(catPromoIdCircleIdMap);
		
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Initialized the categories cache in " + (System.currentTimeMillis() - now) + "ms.");
		}
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Initialized the categories in circle cache");
		}
	}

	public static void putCircleAndCircleCategoryInCache(Circle circle, Map<String, Set<String>> catTypeCircleIdMap, Map<String, Set<String>> catPromoIdCircleIdMap) throws DataAccessException {
		mc.set(RBTCacheKey.getCircleIdCacheKey(circle.getCircleId()), circle);
		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Initializing the categories in circle cache: " + circle.getCircleId());
		}
		List<CircleCategoryMap> categoriesInCircle = CircleCategoryMapDAO.getCategoriesInCircle(circle.getCircleId());
		if(null == categoriesInCircle) {
			basicLogger.warn("There are no categories in circle " + circle.getCircleId());
			return;
		}
		HashMap<String, String[]> result = prepareCircleCategoryDataForCaching(categoriesInCircle, catTypeCircleIdMap, catPromoIdCircleIdMap);
		Iterator<String> iterator = result.keySet().iterator();
		while(iterator.hasNext()) {
			String key = iterator.next();
			mc.set(key, result.get(key));
		}
		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Initialized the categories in circle cache");
		}
	}
	
	/**
	 * Initializes the promo master table
	 * @throws DataAccessException
	 */
	public static void initPromoMasterCache() throws DataAccessException {

		long now = System.currentTimeMillis();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Getting the promo master from db..");
		}
		List<PromoMaster> promoMasters = PromoMasterDAO.getAllPromoMasters();
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Total no of promoMasters: " + promoMasters.size());
			basicLogger.info("Initializing the promoMasters in cache..");
		}
		for(int i=0; i<promoMasters.size(); i++) {
			PromoMaster promoMaster = promoMasters.get(i);
			putPromoMasterInCache(promoMaster);
		}
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Initialized the categories cache in " + (System.currentTimeMillis() - now) + "ms.");
		}
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Initialized the categories in circle cache");
		}
	}
	
	public static void putPromoMasterInCache(PromoMaster promoMaster) throws DataAccessException {
		mc.set(RBTCacheKey.getPromoMasterCacheKey(promoMaster.getPromoCode(), promoMaster.getPromoType()), promoMaster.getClipId());
		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Initializing the promo master cache: " + RBTCacheKey.getPromoMasterCacheKey(promoMaster.getPromoCode(), promoMaster.getPromoType()));
		}
		mc.set(RBTCacheKey.getPromoCodeCacheKey(promoMaster.getPromoCode()), promoMaster.getClipId());
		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Initializing the promo master cache: " + RBTCacheKey.getPromoCodeCacheKey(promoMaster.getPromoCode()));
		}
	}

	private static HashMap<String, String[]> prepareCircleCategoryDataForCaching(List<CircleCategoryMap> categoriesInCircle, Map<String,Set<String>> catTypeCircleIdMap, Map<String, Set<String>> catPromoIdCircleIdMap) {
		HashMap<String, String[]> result = new HashMap<String, String[]>();

		HashMap<String, List<String>> resultTemp = new HashMap<String, List<String>>();
		//		String circleIdTemp = null;
		//		int parentCategoryIdTemp = -100;
		//		char prepaidYesTemp = 'a';

		String key = null;
		String key1 = null;
		Date date = new Date();
		//		List<String> values = null;
		for(int i=0; i<categoriesInCircle.size(); i++) {
			CircleCategoryMap circleCategoryMap = categoriesInCircle.get(i);
			//			if(! circleCategoryMap.getCircleId().equals(circleIdTemp) 
			//					|| ! (circleCategoryMap.getParentCategoryId() != parentCategoryIdTemp)
			//					|| ! (circleCategoryMap.getPrepaidYes() != prepaidYesTemp)) {
			//				if(null != key) {
			//					System.out.println("Key: " + key + " Values: " + values);
			//					result.put(key, values.toArray(new String[]{}));
			//				}
			//				//reinit the key and values;
			//				key = null;
			//				values = new ArrayList<String>();
			//			}
			//			Category category = RBTCacheManager.getInstance().getCategory(circleCategoryMap.getCategoryId());
			if(circleCategoryMap == null){
				continue;
			}
			Category category = (Category) RBTCache.getMemCachedClient().get(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));
			if(category != null){
				key = RBTCacheKey.getCategoriesInCircleCacheKey(circleCategoryMap.getCircleId(), 
						circleCategoryMap.getParentCategoryId(), 
						circleCategoryMap.getPrepaidYes(),
						circleCategoryMap.getCategoryLanguage());

				//Make the circleID, prepaidYes, category type cache
				String catTypeCircleIdKey = RBTCacheKey.getTypePrepadiCircleIdCacheKey(circleCategoryMap.getCircleId(), circleCategoryMap.getPrepaidYes(), Integer.toString(category.getCategoryTpe()));
				
				Set<String> set = catTypeCircleIdMap.get(catTypeCircleIdKey);
				if(!catTypeCircleIdMap.containsKey(catTypeCircleIdKey)){
					set = new HashSet<String>();
					catTypeCircleIdMap.put(catTypeCircleIdKey, set);
				}
				set.add(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));

				//make the circleId, prepaidYes, categoryPromoId cache 
				if(category.getCategoryPromoId() != null && category.getCategoryPromoId().length() > 0){
					String catPromoIdCircleIdKey = RBTCacheKey.getPromoIdPrepadiCircleIdCacheKey(circleCategoryMap.getCircleId(), circleCategoryMap.getPrepaidYes(), category.getCategoryPromoId());
					
					Set<String> catPromoIdSet = catPromoIdCircleIdMap.get(catPromoIdCircleIdKey);
					if(!catPromoIdCircleIdMap.containsKey(catPromoIdCircleIdKey)){
						catPromoIdSet = new HashSet<String>();
						catPromoIdCircleIdMap.put(catPromoIdCircleIdKey, catPromoIdSet);
					}
					catPromoIdSet.add(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));
				}

				List<String> values = resultTemp.get(key);
				if(null == values) {
					values = new ArrayList<String>();
					resultTemp.put(key, values);
				}
				values.add(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));
				if(category.getCategoryStartTime().before(date) && category.getCategoryEndTime().after(date)){
					key1 = RBTCacheKey.getActiveCategoriesInCircleCacheKey(circleCategoryMap.getCircleId(), 
							circleCategoryMap.getParentCategoryId(), 
							circleCategoryMap.getPrepaidYes(),
							circleCategoryMap.getCategoryLanguage());
					List<String> activeCategorievalues = resultTemp.get(key1);
					if(null == activeCategorievalues) {
						activeCategorievalues = new ArrayList<String>();
						resultTemp.put(key1, activeCategorievalues);
					}
					activeCategorievalues.add(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));
				}
			}
			//			circleIdTemp = circleCategoryMap.getCircleId();
			//			parentCategoryIdTemp = circleCategoryMap.getParentCategoryId();
			//			prepaidYesTemp = circleCategoryMap.getPrepaidYes();
		}
		//create key String, value String[]
		Iterator<String> keysIterator = resultTemp.keySet().iterator();
		while(keysIterator.hasNext()) {
			String keyTemp = keysIterator.next();
			List<String> vauesTemp = resultTemp.get(keyTemp);
			result.put(keyTemp, vauesTemp.toArray(new String[]{}));
		}
		return result;
	}

	//	private synchronized static void init() {
	//		if(initialized) {
	//			return;
	//		}
	//		instance = new RBTCache();
	//		initialized = true;
	//	}
	//	
	//	public static RBTCache getInstance() {
	//		if(null == instance) {
	//			init();
	//		}
	//		return instance;
	//	}

	//	public static void refreshClip(String clipId) {
	//refresh the clip id cache
	//refresh the promo id cache
	//refresh the rbtwav file cache
	//refresh the sms alias cache
	//	}

	/**
	 * Refreshes the category and clips in category cache also.
	 * @param categoryId
	 * @return
	 * @throws DataAccessException
	 */
	public static Category refreshCategory(String categoryId) throws DataAccessException{
		//refresh the category id cache
		Category category = null;
		try{
			category = CategoriesDAO.getCategory(categoryId);
		}
		catch(DataAccessException dae){
			basicLogger.error(dae);
		}		
		if(category == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Category not found in db with category id " + categoryId);
			}
		}
		else{
			//Put in category cache
			putCategoryAndCategoryClipsInCache(category);

			putCategoryTypeinCache(Integer.toString(category.getCategoryTpe()));
		
		}	

		return category;
	}

	//	public static String[] refreshClipsInCategory(String categoryId) throws DataAccessException{
	//						
	//		List<CategoryClipMap> categoryClipMap = CategoryClipMapDAO.getClipsInCategory(Integer.parseInt(categoryId));
	//		String[] clipIds = null;
	//		if(categoryClipMap == null){
	//			if(basicLogger.isInfoEnabled()) {
	//				basicLogger.info("Clips not found in db with category id " + categoryId);
	//			}
	//		}
	//		else{
	//			clipIds = CategoryClipMap.getClipIdsArray(categoryClipMap);
	//			mc.set(RBTCacheKey.getClipsInCategoryCacheKey(Integer.parseInt(categoryId)), clipIds);
	//		}
	//		return clipIds;
	//	}
	//	
	public static Circle refreshCircle(String circleId) throws DataAccessException {

		//		List<CircleCategoryMap> categoriesInCircle = CircleCategoryMapDAO.getCategoriesInCircle(circleId);
		//		if(categoriesInCircle == null){
		////			System.out.println("There are no categories in circle " + circleId);
		//			basicLogger.warn("There are no categories in circle " + circleId);
		//		}
		//		else{
		//			//Put circle in Circle Cache			
		//			HashMap<String, String[]> result = prepareCircleCategoryDataForCaching(categoriesInCircle);
		//			Iterator<String> iterator = result.keySet().iterator();
		//			while(iterator.hasNext()) {
		//				String key = iterator.next();
		//				mc.set(key, result.get(key));
		//			}
		//		}
		Circle circle = null;
		Map<String, Set<String>> catTypeCircleIdMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> catPromoIdCircleIdMap = new HashMap<String, Set<String>>();
		try {
			circle = CircleDAO.getCircle(circleId);
		} catch(DataAccessException dae) {
			basicLogger.error(dae);
		}
		if(null == circle) {
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Circle not found in db with circle id " + circleId);
			}
		} else {
			//put the clip in cache
			putCircleAndCircleCategoryInCache(circle, catTypeCircleIdMap, catPromoIdCircleIdMap);
			putMapInCache(catTypeCircleIdMap);
			putMapInCache(catPromoIdCircleIdMap);
		}
		return circle;
	}

	public void deleteClip(int clipId) throws DataAccessException{
		deleteClip(Integer.toString(clipId));
	}

	public void deleteClip(String clipId) throws DataAccessException{		
		Clip clip = null;

		clip = (Clip)RBTCache.getMemCachedClient().get(RBTCacheKey.getClipIdCacheKey(clipId));

		if(clip == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Clip not found in cache clipId : " + clipId);
			}
		}
		else{
			mc.delete(RBTCacheKey.getClipIdCacheKey(clip.getClipId()));

			if(null != clip.getClipPromoId() && clip.getClipPromoId().length() > 0 ){
				mc.delete(RBTCacheKey.getPromoIdCacheKey(clip.getClipPromoId()));
			}

			if(null != clip.getClipRbtWavFile() && clip.getClipRbtWavFile().length() > 0 ){
				mc.delete(RBTCacheKey.getRbtWavFileCacheKey(clip.getClipRbtWavFile()));
			}

			if(null != clip.getClipSmsAlias() && clip.getClipSmsAlias().length() > 0 ){
				mc.delete(RBTCacheKey.getSmsAliasCacheKey(clip.getClipSmsAlias()));
			}
		}
	}

	public void deleteCategory(String categoryId) throws DataAccessException{
		Category category = null;

		category = (Category)RBTCache.getMemCachedClient().get(RBTCacheKey.getCategoryIdCacheKey(Integer.parseInt(categoryId)));

		if(category == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Category not found in cache Category Id : " + categoryId);
			}
		}
		else{
			mc.delete(RBTCacheKey.getCategoryIdCacheKey(category.getCategoryId()));

			if(null == category.getCategoryPromoId() && category.getCategoryPromoId().length() > 0){
				mc.delete(RBTCacheKey.getCategoryPromoIdCacheKey(category.getCategoryPromoId()));
			}

			if(null == category.getMmNumber() && category.getMmNumber().length() >0){
				mc.delete(RBTCacheKey.getCategoryMMNumberCacheKey(category.getMmNumber()));
			}

			//delete the cache of category in clips
			mc.delete(RBTCacheKey.getClipsInCategoryCacheKey(category.getCategoryId()));
		}
	}


	public static MemCachedClient getMemCachedClient() {
		return mc;
	}


	public static void main(String[] args) {
		try {
			RBTMultiThreadInitCache.init();
			shutDown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void init() {
		try {
			long l1 = System.currentTimeMillis();
			CategoryClipMapDAO.updateCategoryClipMap("UPDATE RBT_CATEGORY_CLIP_MAP SET CLIP_IN_LIST = 'n' WHERE CLIP_IN_LIST IS NULL");
			CategoryClipMapDAO.updateCategoryClipMap("UPDATE RBT_CATEGORY_CLIP_MAP SET CATEGORY_CLIP_INDEX = 999 WHERE CATEGORY_CLIP_INDEX IS NULL");
			System.out.println("Initializing the clips cache...");
			initClipsCache();
			long l2 = System.currentTimeMillis();
			System.out.println("Initializing the categories cache...");
			initCategoryCache();
			long l3 = System.currentTimeMillis();
			System.out.println("Initializing the category clip map cache...");
			initCircleCategoryCache();
			long l4 = System.currentTimeMillis();
			System.out.println("Initializing the promo master cache...");
			initPromoMasterCache();
			long l5 = System.currentTimeMillis();
			getMemCachedClient().set(MC_IS_CACHE_INITIALIZED_FLAG, System.currentTimeMillis());
			if(null!=RBTContentJarParameters.getInstance().getParameter("artist.Indexing")){
				System.out.println("Initializing the clips artist cache...");
				basicLogger.info("Got required parameter for initializing clip artist cache...");
				initClipsArtistCache();
			}
			System.out.println("Done");
			shutDown();
			long l6 = System.currentTimeMillis();
			basicLogger.info("Total time: " + (l6 - l1) + " Clips:" + (l2 - l1) + " Categories:" + (l3 - l2)
					+ " Circles:" + (l4 - l3) + " PromoMaster:" + (l5 - l4));

			ContentRefreshTCPClient.sendMessageToAllClients(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Class which extends thread and checks the memcached heart beat.
	 * 
	 */
	protected static class RBTCacheHeartBeatThread extends Thread {

		// logger
//		private static Logger log = Logger.getLogger(RBTCacheHeartBeatThread.class.getName());

		//		private RBTCache cache;
		private long interval = 1000 * 60; // every 60 seconds
		private boolean stopThread = false;
		private boolean running;
//		private int counter = 0;

		protected RBTCacheHeartBeatThread() {
			//			this.cache = cache;
			this.setDaemon(true);
			this.setName("RBTCacheHeartBeatThread");
		}

		public void setInterval(long interval) { 
			this.interval = interval; 
		}

		public boolean isRunning() {
			return this.running;
		}

		/** 
		 * sets stop variable and interrupts any wait 
		 */
		public void stopThread() {
			this.stopThread = true;
			this.interrupt();
		}

		/** 
		 * Start the thread.
		 */
		public void run() {
			this.running = true;
			basicLogger.info("Starting the heartBeatThread");
//			System.out.println("Starting the heartBeatThread");
			MDC.clear();
			while(!this.stopThread) {
				try {
					checkCacheInitialized();
					
					Thread.sleep(interval);
				} catch (Exception e) {
					break;
				}
			}
			this.running = false;
		}
	}

	
	private static void putCategoryTypeinCache(String categoryType){
		if(categoryType == null){
			Iterator<String> iterator = categoryTypeMap.keySet().iterator();
			while(iterator.hasNext()){
				categoryType = iterator.next();
				Set<String> set = categoryTypeMap.get(categoryType);				
				mc.set(RBTCacheKey.getCategoryTypeCacheKey(categoryType), (String[]) set.toArray(new String[0]));
			}
		}
		else{
			Set<String> set = categoryTypeMap.get(categoryType);
			mc.set(RBTCacheKey.getCategoryTypeCacheKey(categoryType), (String[]) set.toArray(new String[0]));
		}
	}

	public static void putCategoryClipInCache(List<CategoryClipMap> clipsInCategory, int categoryId) {
		Date now = new Date();
		List<String> allClipResult = new ArrayList<String>(clipsInCategory.size()); 
		List<String> activeClipResult = new ArrayList<String>(clipsInCategory.size());
		for(int i=0; i<clipsInCategory.size(); i++) {
			CategoryClipMap clipInCategory = clipsInCategory.get(i);
			String key = RBTCacheKey.getClipIdCacheKey(clipInCategory.getClipId());
			Clip clip = (Clip)RBTCache.getMemCachedClient().get(key);
			if(null != clip){
				allClipResult.add(key);
				if(clip.getClipStartTime().before(now) && clip.getClipEndTime().after(now)){
					activeClipResult.add(key);
				}
			}
		}
		if(allClipResult != null && allClipResult.size() > 0){
			mc.set(RBTCacheKey.getClipsInCategoryCacheKey(categoryId), allClipResult.toArray(new String[0]));
		}
		
		if(activeClipResult != null && activeClipResult.size() > 0){
			mc.set(RBTCacheKey.getActiveClipsInCategoryCacheKey(categoryId), activeClipResult.toArray(new String[0]));
		}
		
	}

	private static void putMapInCache(Map<String,Set<String>> catTypeCircleIdMap){
		
		Iterator<String> iterator = catTypeCircleIdMap.keySet().iterator();
		
		while(iterator.hasNext()){
			String key = iterator.next();
			mc.set(key, (String[])catTypeCircleIdMap.get(key).toArray(new String[0]));
		}		
	}

	public static String[] getMultipleSmsAlias(String smsAlias){
		String[] temp = null;
		temp = smsAlias.trim().split("[\\,\\;]");
		return temp;
	}
}
