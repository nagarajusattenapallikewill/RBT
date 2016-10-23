package com.onmobile.apps.ringbacktones.cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

/**
 * @author Sreekar
 * @date 20/06/2008
 */

public class RBTCacheManager {
	private static Logger logger = Logger.getLogger(RBTCacheManager.class);
	private String _class = "RBTCacheManager";

	protected static final String CACHE_MODE_TATA = "TATA";
	protected static final String CACHE_MODE_NON_TATA = "RW";

	private static final String CLIP_CACHE = "ClipCache";
	private static final String CATEGORY_CACHE = "CategoryCache";
	
	/*private static Date _clipDateRef = null;
	private static boolean _updatingClipCache = false;*/

	private String _cacheMode;
	String _cacheModules = null;
	private String allCacheModules = "CategoryCache,ClipCache";
	private Hashtable<String, CacheModule> _cacheMap = new Hashtable<String, CacheModule>();

	private static RBTCacheManager _instance = null;
	private static CacheRefreshThread _cacheRefeshThread = null;
	private RBTDBManager dbManager = null;

	private static Object m_syncObj = new Object();
	
	private boolean m_bInitialized = false;

	private RBTCacheManager() throws RBTException {
		initCache();
		logger.info("RBT::CacheManager announcing Cach Initialization Over!!!!!!!");
		m_bInitialized = true;
		_cacheRefeshThread = new CacheRefreshThread(this);
	}

	private boolean checkCacheMode(String mode) {
		if(mode.equals(CACHE_MODE_TATA) || mode.equals(CACHE_MODE_NON_TATA))
			return true;
		return false;
	}

	private void initCache() throws RBTException {
		String method = "initCache";
		initParams();
		logger.info("Starting the cache with modules --> " + _cacheModules);
		StringTokenizer stk = new StringTokenizer(_cacheModules, ",");
		while (stk.hasMoreTokens()) {
			String thisToken = stk.nextToken();
			if(thisToken.equals(CLIP_CACHE)) {
				ClipCache clipCache = (ClipCache)ClipCache.getInstance(_cacheMode);
				_cacheMap.put(thisToken, clipCache);
				logger.info("RBT::have put module " + thisToken
						+ " in cache map");
//				_clipDateRef = getClipRefDate();
			}
			else if(thisToken.equals(CATEGORY_CACHE)) {
				CategoryCache categoryCache = CategoryCache.getInstance();
				_cacheMap.put(thisToken, categoryCache);
				logger.info("RBT::have put module " + thisToken
						+ " in cache map");
			}
			else
				logger.info("RBT::Invalid cache module " + thisToken);
			/*try {
				logger.info("RBT::trying to instantiate module " + thisToken);
				Class c = Class.forName("com.onmobile.apps.ringbacktones.cache." + thisToken);
				Method[] allMethods = c.getMethods();

				if(allMethods == null)
					throw new RBTException("No methods for cache class " + thisToken);
				for(int i = 0; i < allMethods.length; i++) {
					if(allMethods[i].getName().equalsIgnoreCase("getInstance")) {
						Object[] paramArr = new String[] { _cacheMode };
						Object cacheObj = allMethods[i].invoke(null, paramArr);
						_cacheMap.put(thisToken, cacheObj);
						logger.info("RBT::have put module " + thisToken
								+ " in cache map");
						if(thisToken.equals(CLIP_CACHE))
							_clipDateRef = getClipRefDate();
					}
				}
			}
			catch (Exception e) {
				logger.info("Not able to instantiate the class "
						+ thisToken);
				Tools.logException(_class, method, e);
			}*/
		}
	}

	private boolean refreshCache(String cacheModule, boolean checkNeeded) throws RBTException {
		String method = "refreshCache";
		logger.info("RBT::refresing cache for module " + cacheModule);
		boolean refreshedCache = false;
		try {
			CacheModule mod = (CacheModule)_cacheMap.get(cacheModule);
			if (mod != null)
				refreshedCache = mod.refreshCache(checkNeeded);
		}
		catch (Exception e) {
			logger.info("Not able to refresh the class " + cacheModule);
			logger.error("", e);
		}
		return refreshedCache;
	}

	public boolean isCacheInitialized()
	{
		return m_bInitialized;
	}
	
	public boolean isClipCacheInitialized()
	{
		return (_cacheModules.contains(CLIP_CACHE));
	}
	
	public boolean isCategoryCacheInitialized()
	{
		return (_cacheModules.contains(CATEGORY_CACHE));
	}
	
	public boolean refreshCacheForModule(String module, boolean checkNeeded) {
		String method = "refreshCacheForModule";
		boolean retVal = false;
		if(_cacheModules != null && (_cacheModules.indexOf(module) > -1)) {
			try {
				retVal = refreshCache(module, checkNeeded);
			}
			catch (RBTException e) {
				logger.error("", e);
			}
		}
		else
			logger.info("RBT::" + module
					+ " is an invalid module. Not refreshing the cache");
		return retVal;
	}

	private void initParams() throws RBTException {
		dbManager = RBTDBManager.getInstance();
		ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
		String cacheMode = null;
		String cacheModule = null;
		try
		{
			if(resourceBundle != null)
			{
				cacheMode = resourceBundle.getString("CACHE_MODE").trim();
				cacheModule = resourceBundle.getString("CACHE_MODULES").trim();
			}	
		}
		catch(Exception e)
		{
			logger.info("cache Mode and cache Module not in rbt.properties");	
		}
		
		if(cacheMode == null)
		{
			Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("CACHE", "CACHE_MODE");
			if (param != null && param.getValue() != null)
				cacheMode = param.getValue().trim();
		}
		
		if (!checkCacheMode(cacheMode))
			throw new RBTException(
					"Cache mode not initialised/Invalid cache mode");
		
		if(cacheModule == null)
		{
			Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("CACHE", "CACHE_MODULES");
			if (param != null && param.getValue() != null)
				cacheModule = param.getValue().trim();
		}
		if(cacheMode == null)
			cacheMode= "RW";
		if(cacheModule == null)
			cacheModule = allCacheModules;
		_cacheMode = cacheMode;
		_cacheModules = cacheModule;
		logger.info("_cacheModules = "+ _cacheModules);
		logger.info("_cacheMode = "+_cacheMode);
	}
	
	private void refreshCache() throws RBTException {
		StringTokenizer stk = new StringTokenizer(_cacheModules, ",");
		boolean refreshedCache = false;
		m_bInitialized = false;

		while (stk.hasMoreTokens()) {
			refreshedCache = refreshCache(stk.nextToken(), true) || refreshedCache;
		}
		m_bInitialized = true;

		logger.info("RBT::CacheManager announcing Cach Refreshing Over!!!!!!!");
	}
	
	public void refreshCacheNoCheck() throws RBTException {
		StringTokenizer stk = new StringTokenizer(_cacheModules, ",");
		boolean refreshedCache = false;
		while (stk.hasMoreTokens()) {
			refreshedCache = refreshCache(stk.nextToken(), false) || refreshedCache;
		}
	}

	public static RBTCacheManager getInstance() throws RBTException {
		synchronized (m_syncObj) {
			if(_instance == null) {
				_instance = new RBTCacheManager();
				_cacheRefeshThread.start();
			}
		}
		return _instance;
	}
	
	private ClipCache getClipCache() {
		ClipCache clipCache = null;
		if(_cacheMap.containsKey(CLIP_CACHE))
			clipCache = (ClipCache)_cacheMap.get(CLIP_CACHE);
		if(clipCache == null)
			logger.info("RBT::clipcache is null");
		return clipCache;
	}

	// this uses ClipCache
	public int getClipIDForPromoID(String promoID, boolean checkMap) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return -1;
		return clipCache.getClipIDForPromoId(promoID, checkMap);
	}

	// this uses ClipCache
	public ClipMinimal getClip(int clipID, boolean checkMap) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getClip(clipID, checkMap);
	}

	// this uses ClipCache
	public ClipMinimal getClip(String clipPromoID, boolean checkMap) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		int clipID = clipCache.getClipIDForPromoId(clipPromoID.toLowerCase(), checkMap);
		if(clipID == -1)
			return null;
		return clipCache.getClip(clipID, true);
	}
	
	public ClipMinimal getClipByWavFile(String clipWavFile, boolean checkMap) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		int clipID = clipCache.getClipIDForWavFile(clipWavFile, checkMap);
//		System.out.println("The value of clipId in RBTCacheManger "+ clipID );
		if(clipID == -1)
			return null;
		return clipCache.getClip(clipID, false);
	}
	
	//this use ClipCache
	public ClipMinimal getClipRBT(String wavFile, boolean checkMap) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getClipRBT(wavFile, checkMap);
	}

	// this uses ClipCache
	public int getActualClipIDForPromoID(String promoID) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return -1;
		return clipCache.getActualClipIDForPromoId(promoID);
	}
	
	public ClipMinimal getClipSMSAlias(String smsAlias) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getClipSMSAlias(smsAlias);
	}

	// this uses ClipCache and used only in TATA
	public ClipMinimal getActualClip(String clipPromoID) {
		String method = "getActualClip";
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		int clipID = clipCache.getActualClipIDForPromoId(clipPromoID);
		if(clipID == -1) {
			logger.info("RBT::clip not present for promo id " + clipPromoID);
			return null;
		}
		return clipCache.getClip(clipID, true);
	}
	
	public ClipMinimal[] getClipsByName(String start) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getClipsByName(start);
	}
	
	public ClipMinimal getClipByName(String start, boolean checkMap) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getClipByName(start, checkMap);
	}
	
	public SortedMap<String, String> getSMSPromoClips() {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getSMSPromoClips();
	}
	
	public int[] getClipIDsInCategory(int catID) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getClipIDsInCategory(catID);
	}
	
	public int getCatIDsForClipId(int clipID) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return -1;
		return clipCache.getCatIDsForClipId(clipID);
	}
	
	public ClipMinimal[] getAllActiveClips() {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getAllActiveClips();
	}
	
	public String[] getClipsNotInCategories(String categories) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getClipsNotInCategories(categories);
	}
	
	public Clips[] getActiveCategoryClips(int categoryID, String chargeClasses, char clipInYes) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getActiveCategoryClips(categoryID, chargeClasses, clipInYes);
	}
	
	public Clips[] getClipsInCategory(String category) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getClipsInCategories(category);
	}
	
	public Clips getClip(int clipID) {
		ClipMinimal clip = getClip(clipID, false);
		if(clip != null)
			return clip.getClipsObj();
		return null;
	}
	
	public Clips getClipByName(String name) {
		ClipMinimal clip = getClipByName(name, false);
		if(clip != null)
			return clip.getClipsObj();
		return null;
	}
	
	public Clips getClipPromoID(String promoID) {
		ClipMinimal clip = getClip(promoID, false);
		if(clip != null)
			return clip.getClipsObj();
		return null;
	}
	
	public Clips getClipRBT(String wavFile) {
		ClipMinimal clip = getClipRBT(wavFile, false);
		if(clip != null)
			return clip.getClipsObj();
		return null;
	}
	
	public Clips[] getClipsByAlbum(String album) {
		ClipCache clipCache = getClipCache();
		if(clipCache == null)
			return null;
		return clipCache.getClipsByAlbum(album);
	}
	
	/**
	 * 
	 * @author vsreekar
	 * All methods of CategoryCache method
	 */
	public Category getCategory(int catID) {
		String method = "getCategory";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getCategory(catID);
	}

	public Category getCategoryByName(String name) {
		String method = "getCategory";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getCategoryByName(name);
	}
	
	public Categories getCategory(int catID, String circleID, char prepaidYes) {
		String method = "getCategory";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getCategory(catID, circleID, prepaidYes);
	}
	
	public Categories getCategory(String catName, String circleID, char prepaidYes) {
		String method = "getCategory";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getCategory(catName, circleID, prepaidYes);
	}
	
	public Categories getCategoryPromoID(String categoryPromoID, String circleID, char prepaidYes) {
		String method = "getCategoryPromoID";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getCategoryPromoID(categoryPromoID, circleID, prepaidYes);
	}
	
	public Category getCategoryPromoID(String categoryPromoID) {
		String method = "getCategoryPromoID";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getCategoryPromoID(categoryPromoID);
	}
	
	public Categories getCategoryMMNumber(String mmNumber, String circleID, char prepaidYes) {
		String method = "getCategoryMMNumber";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getCategoryMMNumber(mmNumber, circleID, prepaidYes);
	}
	
	public Categories getCategoryAlias(String smsAlias, String circleID, char prepaidYes) {
		String method = "getCategoryAlias";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getCategoryAlias(smsAlias, circleID, prepaidYes);
	}
	
	public Categories[] getAllCategoriesForCircle(String circleID, boolean disp) {
		String method = "getAllCategoriesForCircle";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getAllCategoriesForCircle(circleID, disp);
	}
	
	public Categories[] getCategories(String categoryIDs, String circleID, char prepaidYes) {
		String method = "getCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getCategories(categoryIDs, circleID, prepaidYes);
	}
	
	public Categories[] getActiveCategories(String circleID, char prepaidYes) {
		String method = "getActiveCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getActiveCategories(circleID, prepaidYes);
	}
	
	public Categories[] getActiveCategories(String circleID, char prepaidYes, String language) {
		String method = "getActiveCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getActiveCategories(circleID, prepaidYes, language);
	}
	
	public Categories[] getActiveCategoriesbyCircleId() {
		String method = "getActiveCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getActiveCategoriesbyCircleId();
	}
	
	public Categories[] getGUIActiveCategories(String circleID, char prepaidYes) {
		String method = "getGUIActiveCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getGUIActiveCategories(circleID, prepaidYes);
	}
	
	public Categories[] getAllCategories(String circleID, char prepaidYes) {
		String method = "getAllCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getAllCategories(circleID, prepaidYes);
	}
	
	public Categories[] getAllCategories() {
		String method = "getAllCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getAllCategories();
	}
	
	public Categories[] getSubCategories(String circleID, char prepaidYes, int parentCategoryID,
			String language) {
		String method = "getSubCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getSubCategories(circleID, prepaidYes, parentCategoryID, language);
	}
	
	public Categories[] getGUISubCategories(String circleID, char prepaidYes,
			int parentCategoryID) {
		String method = "getGUISubCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getGUISubCategories(circleID, prepaidYes, parentCategoryID);
	}
	
	public Categories[] getActiveBouquet(int parent, String circleID, char prepaidYes,
			String language) {
		String method = "getActiveBouquet";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getActiveBouquet(parent, circleID, prepaidYes, language);
	}
	
	public Categories[] getBouquet(String circleID, char prepaidYes) {
		String method = "getBouquet";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getBouquet(circleID, prepaidYes);
	}
	
	public HashMap getMBMapByStartTime(Date startDate, Date endDate) {
		String method = "getBouquet";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getMBMapByStartTime(startDate, endDate);
	}
	
	public Categories[] getActiveOtherThanUGCCategories(String circleID, char prepaidYes,
			String categoryIDs) {
		String method = "getActiveOtherThanUGCCategories";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getActiveOtherThanUGCCategories(circleID, prepaidYes, categoryIDs);
	}
	
	public ArrayList getShuffleCategoryIDs(boolean onlyActive) {
		String method = "getAllShuffleCategoryIDs";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getShuffleCategoryIDs(onlyActive);
	}
	
	public Categories[] getOverrideShuffles(String circleID, char prepaidYes) {
		String method = "getOverrideShuffles";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getOverrideShuffles(circleID, prepaidYes);
	}
	
	public Categories[] getOverrideShuffles() {
		String method = "getOverrideShuffles";
		CategoryCache categoryCache = null;
		if(_cacheMap.containsKey(CATEGORY_CACHE))
			categoryCache = (CategoryCache)_cacheMap.get(CATEGORY_CACHE);
		if(categoryCache == null) {
			logger.info("RBT::category cache is null");
			return null;
		}
		return categoryCache.getOverrideShuffles();
	}
	
	private class CacheRefreshThread extends Thread {
		private static final String _class = "CacheRefreshThread";
		private int _sleepTimeMin = 5;
		private RBTCacheManager _rcm = null;
		
		CacheRefreshThread (RBTCacheManager rcm ) {
			_rcm = rcm;
			this.setName(_class);
			try {
				Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("CACHE", "SLEEP_TIME_MIN");
				_sleepTimeMin = Integer.parseInt(param.getValue());
			}
			catch(Exception e) {
				
			}
		}

		public void run() {
			String method = "run";
			Calendar calendar;
			while (true) {
				try {
					long nexttime = getnexttime(_sleepTimeMin);
					calendar = Calendar.getInstance();
					calendar.setTime(new java.util.Date(nexttime));
					logger.info("RBT::Sleeping till "
									+ calendar.getTime()
									+ " for next processing !!!!!");
					long diff = (calendar.getTime().getTime() - Calendar
							.getInstance().getTime().getTime());
					try {
						if (diff > 0) {
							Thread.sleep(diff);
						} else
							Thread.sleep(_sleepTimeMin * 60 * 1000);
					} catch (InterruptedException e) {
						logger.error("", e);
					}
					_rcm.refreshCache();

				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}

		public long getnexttime(int sleep) {
			Calendar now = Calendar.getInstance();
			now.setTime(new java.util.Date(System.currentTimeMillis()));
			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.SECOND, 0);

			long nexttime = now.getTime().getTime();
			while (nexttime < System.currentTimeMillis()) {
				nexttime = nexttime + (sleep * 60 * 1000);
			}

			logger.info("RBT::getnexttime"
					+ new Date(nexttime));
			return nexttime;
		}
	}
}