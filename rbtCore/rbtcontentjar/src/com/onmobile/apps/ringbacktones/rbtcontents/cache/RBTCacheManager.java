package com.onmobile.apps.ringbacktones.rbtcontents.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfoAction;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRatingTransaction;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.PromoMaster;
import com.onmobile.apps.ringbacktones.rbtcontents.bi.BIInterface;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoriesDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipRatingDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipRatingTransactionDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.PromoMasterDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.CaseInsensitiveStringComparator;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.RBTContentUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.MemcacheContentRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * This class provides all the required functionality for the RBT WEB, CCC GUI. 
 * @author ganipisetty
 *
 */
public class RBTCacheManager {

	private static Logger basicLogger = Logger.getLogger(RBTCacheManager.class);
	private static RBTCacheManager instance = null;
	private static boolean initialized = false;
	
	//private static String DB_FALLBACK = "DB";
	private static String WEBSERVICE_FALLBACK = "WEBSERVICE";
	
	private static String fallbackLogic = null;
	

	/**
	 * Constructor.
	 */
	protected RBTCacheManager() {
		File hibernateConfigFile = new File("hibernate.cfg.xml");
		if(hibernateConfigFile.exists()) {
			basicLogger.warn("Fall back to memcache is set as DB..");
		} else {
			basicLogger.info("No fallback to memcache..");
		}
	}

	
	/**
	 * Initilize the RBTCacheManager
	 */
	private synchronized static void init() {
		if(initialized) {
			return;
		}
		instance = new RBTCacheManager();
		initialized = true;
		
		fallbackLogic = RBTContentJarParameters.getInstance().getParameter("fallback_logic");
	}

	public static RBTCacheManager getInstance() {
		if(null == instance) {
			init();
		}
		return instance;
	}

	/**
	 * Gets the clip mapped with the clip id from cache.
	 * @param clipId
	 * @return
	 */
	public Clip getClip(int clipId) {
		return getClip(Integer.toString(clipId));
	}
	
	/**
	 * Gets the clip mapped with the clip id from cache.
	 * @param clipId
	 * @return
	 */
	public Clip getClip(int clipId, String language) {
		return getClip(Integer.toString(clipId), language);
	}
	
	/**
	 * Gets the clip in any other language, If null passed the default language meta data is returned
	 * @param clipId
	 * @param language
	 * @return Clip
	 */
	public Clip getClip(String clipId, String language){
		return getClip(clipId, language, null);
	}
	
	/**
	 * Gets the clip in any other language, If null passed the default language meta data is returned
	 * @param clipId
	 * @param language
	 * @param appName used for app specific default language
	 * @return Clip
	 */
	public Clip getClip(String clipId, String language, String appName){
		//XXX
		long now = System.currentTimeMillis();

		Clip clip = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();  
		if(cacheAlive && cacheInitialized) {
			clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, language, appName));
			if (clip == null) {					
				clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, null, appName));
				if (clip == null) {
					clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, null, null));
				}
			}
		} else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCLIPID, clipId);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setAppName(appName);
			List<Clip> clipList = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(clipList != null && clipList.size() > 0) {
				clip = clipList.get(0);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				clip = ClipsDAO.getClip(clipId, language);
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			} catch(Exception nfe) {
				basicLogger.error(nfe);
			}

		} 
		basicLogger.info("Clip Id " + clipId + " Time taken: " + (System.currentTimeMillis() - now) + " Clip: " + clip);
		return clip;
	}
	
	/**
	 * Gets the clip mapped with the clip id from cache.
	 * @param clipId
	 * @return
	 */
	public Clip getClip(String clipId) {
		return getClip(clipId, null);
	}
	
	/**
	 * Gets the clip mapped with the promo id from cache in any of the language, if null passsed default language info
	 * is returned
	 * @param promoId
	 * @return Clip
	 */
	public Clip getClipByPromoId(String promoId, String language) {
		long now = System.currentTimeMillis();

		Clip clip = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();  
		if(cacheAlive && cacheInitialized) {
			String clipId = (String)getCachedData(RBTCacheKey.getPromoIdCacheKey(promoId));
			if(null != clipId && clipId.length() > 0) {
				clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if(clip==null)
					clip = (Clip)getCachedData(RBTCacheKey.getClipIdCacheKey(clipId));
			} else {
				basicLogger.warn("Clip not available in cache with promo id " + promoId);
			}
		} else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCLIPPROMOID, promoId);
			memcacheContentRequest.setLanguage(language);
			List<Clip> clipList = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(clipList != null && clipList.size() > 0) {
				clip = clipList.get(0);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				clip = ClipsDAO.getClipByPromoId(promoId, language);
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		} 
		basicLogger.info("Clip Promo Id " + promoId + " Time taken: " + (System.currentTimeMillis() - now) + " Clip: " + clip);
		return clip;
	}

	

	/**
	 * Gets the clip mapped with the promo id from cache.
	 * @param promoId
	 * @return
	 */
	public Clip getClipByPromoId(String promoId) {
		return getClipByPromoId(promoId, null);
	}


	/**
	 * Gets the clip mapped with the RBT WAV file name from cache.
	 * @param rbtWavFileName
	 * @return
	 */
	public Clip getClipByRbtWavFileName(String rbtWavFileName) {
		return getClipByRbtWavFileName(rbtWavFileName, null);
	}
	
	/**
	 * Gets the clip mapped with the RBT WAV file name from cache based on the language passed
	 * @param rbtWavFileName
	 * @param language
	 * @return
	 */
	public Clip getClipByRbtWavFileName(String rbtWavFileName, String language) {
		return getClipByRbtWavFileName(rbtWavFileName, language, null);
	}
	/**
	 * Gets the clip mapped with the RBT WAV file name from cache based on the language passed
	 * @param rbtWavFileName
	 * @param language
	 * @param appName
	 * @return
	 */
	public Clip getClipByRbtWavFileName(String rbtWavFileName, String language, String appName) {
		//XXX
		long now = System.currentTimeMillis();
		
		// Added for cut rbt
		if(rbtWavFileName.indexOf("_cut") != -1){
			rbtWavFileName = rbtWavFileName.substring(0, rbtWavFileName.indexOf("_cut"));
		}	

		Clip clip = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		boolean tosupportClipVcode = Boolean.parseBoolean(RBTContentJarParameters.getInstance().getParameter("clip_vcode"));
		if(cacheAlive && cacheInitialized) {
			String clipId = (String)getCachedData(RBTCacheKey.getRbtWavFileCacheKey(rbtWavFileName));
			
			basicLogger.info("the tosupportClipVcode is configured to :" + tosupportClipVcode);
			if(clipId == null && tosupportClipVcode) {
					String vcode = rbtWavFileName;
					if(rbtWavFileName.indexOf("rbt_") != -1 && rbtWavFileName.lastIndexOf("_rbt") != -1) {
						vcode = rbtWavFileName.substring("rbt_".length(), rbtWavFileName.lastIndexOf("_rbt"));
					}
					basicLogger.info("rbtwavefile is :" + rbtWavFileName + " vcode is :" + vcode );
					clipId = (String)getCachedData(RBTCacheKey.getVcodeCacheKey(vcode));
					basicLogger.info("clipId formed using vcode is:" + clipId);
			}
			if(null != clipId && clipId.length() > 0) {
				clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if(clip==null) {
					clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, null, appName));
					if (clip == null) {
						clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, null, null));
					}
				}
			} else {
				basicLogger.warn("Clip not available in cache with RBT WAV file " + rbtWavFileName);
			}
		} else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCLIPWAVFILE, rbtWavFileName);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setLanguage(appName);
			List<Clip> clipList = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(clipList != null && clipList.size() > 0) {
				clip = clipList.get(0);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				clip = ClipsDAO.getClipByRbtWavFileName(rbtWavFileName, language);
				basicLogger.info("the tosupportClipVcode is configured to :" + tosupportClipVcode);
				if(clip == null && tosupportClipVcode) {
						String vcode = rbtWavFileName;
						if(rbtWavFileName.indexOf("rbt_") != -1 && rbtWavFileName.lastIndexOf("_rbt") != -1) {
							vcode = rbtWavFileName.substring("rbt_".length() , rbtWavFileName.lastIndexOf("_rbt"));
						}
						basicLogger.info("rbtwavefile is :" + rbtWavFileName + " vcode is :" + vcode );
						clip = ClipsDAO.getClipByVcode(vcode, language);
				}
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		basicLogger.info("Clip RBT WAV file name: " + rbtWavFileName + " Time taken: " + (System.currentTimeMillis() - now) + " Clip: " + clip);
		return clip;
	}

	/**
	 * Gets the clip mapped with the SMS alias from cache.
	 * @param smsAlias
	 * @return
	 */
	public Clip getClipBySMSAlias(String smsAlias) {
		return getClipBySMSAlias(smsAlias, null);
	}
	
	/**
	 * Gets the clip mapped with the SMS alias from cache based on language passed
	 * @param smsAlias
	 * @return
	 */
	public Clip getClipBySMSAlias(String smsAlias, String language) {
		long now = System.currentTimeMillis();

		Clip clip = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();  
		if(cacheAlive && cacheInitialized) {
			String clipId = (String)getCachedData(RBTCacheKey.getSmsAliasCacheKey(smsAlias));
			if(null != clipId && clipId.length() > 0) {
				clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if(clip==null)
					clip = (Clip)getCachedData(RBTCacheKey.getClipIdCacheKey(clipId));
			} else {
				basicLogger.warn("Clip not available in cache with SMS alias " + smsAlias);
			}
		} else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCLIPALIAS, smsAlias);
			memcacheContentRequest.setLanguage(language);
			List<Clip> clipList = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(clipList != null && clipList.size() > 0) {
				clip = clipList.get(0);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				clip = ClipsDAO.getClipBySMSAlias(smsAlias, language);
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		basicLogger.info("Clip SMS alias: " + smsAlias + " Time taken: " + (System.currentTimeMillis() - now) + " Clip: " + clip);
		return clip;
	}

	/**
	 * Gets the category mapped with the category id from cache.
	 * @param categoryId
	 * @return
	 */
	public Category getCategory(int categoryId) {
		return getCategory(categoryId, null);
	}
	
	/**
	 * Gets the category mapped with the category id from cache in a specific language
	 * @param categoryId
	 * @param language
	 * @return
	 */
	public Category getCategory(int categoryId, String language) {
		return getCategory(categoryId, language, null);
	}
	/**
	 * Gets the category mapped with the category id from cache in a specific language
	 * @param categoryId
	 * @param language
	 * @param appName used for app specific default language
	 * @return
	 */
	public Category getCategory(int categoryId, String language, String appName) {
		//XXX
		long now = System.currentTimeMillis();
		Category category = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();  
		if(cacheAlive && cacheInitialized) {
			category = (Category)getCachedData(RBTCacheKey.getCategoryIdLanguageCacheKey(categoryId, language, appName));
			if (category == null) {
				category = (Category)getCachedData(RBTCacheKey.getCategoryIdLanguageCacheKey(categoryId, null, appName));
				if (category == null) {
					category = (Category)getCachedData(RBTCacheKey.getCategoryIdLanguageCacheKey(categoryId, null, null));
				}
			}
		} else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATID, categoryId + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setAppName(appName);
			List<Category> objectList = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(objectList != null && objectList.size() > 0) {
				category = objectList.get(0);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				category = CategoriesDAO.getCategory(Integer.toString(categoryId), language);
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		basicLogger.info("Category id: " + categoryId + " Time taken: " + (System.currentTimeMillis() - now) + " Category: " + category);
		return category;
	}
	
	

	/**
	 * Gets the category mapped with the promo id from cache.
	 * @param promoId
	 * @return
	 */
	public Category getCategoryByPromoId(String promoId){
		return getCategoryByPromoId(promoId, null);
	}
	
	/**
	 * Gets the category mapped with the promo id from cache in a specific language
	 * @param promoId
	 * @return
	 */
	public Category getCategoryByPromoId(String promoId, String language){
		long now = System.currentTimeMillis();

		Category category = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();  
		if(cacheAlive && cacheInitialized) {
			String categoryId = (String)getCachedData(RBTCacheKey.getCategoryPromoIdCacheKey(promoId));
			if(null != categoryId && categoryId.length() > 0) {
				category = (Category)getCachedData(RBTCacheKey.getCategoryIdLanguageCacheKey(Integer.parseInt(categoryId), language) );
				if(category==null)
					category = (Category)getCachedData(RBTCacheKey.getCategoryIdCacheKey(Integer.parseInt(categoryId)));
			} else {
				basicLogger.warn("Category not available in cache with Promo id " + promoId);
			}
		} else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATPROMOID, promoId);
			memcacheContentRequest.setLanguage(language);
			List<Category> objectList = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(objectList != null && objectList.size() > 0) {
				category = objectList.get(0);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				category = CategoriesDAO.getCatgegoryByPromoId(promoId.toLowerCase(), language);
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		basicLogger.info("Category Promo id: " + promoId + " Time taken: " + (System.currentTimeMillis() - now) + " Category: " + category);
		return category;
	}

	/**
	 * Gets the category mapped with the MMNumber from cache.
	 * @param promoId
	 * @return
	 */
	public Category getCategoryByMmNumber(String mmNumber){
		return getCategoryByMmNumber(mmNumber, null);
	}
	
	/**
	 * Gets the category mapped with the MMNumber from cache in a specific language
	 * @param promoId
	 * @return
	 */
	public Category getCategoryByMmNumber(String mmNumber, String language){
		long now = System.currentTimeMillis();

		Category category = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();  
		if(cacheAlive && cacheInitialized) {
			String categoryId = (String)getCachedData(RBTCacheKey.getCategoryMMNumberCacheKey(mmNumber));
			if(null != categoryId && categoryId.length() > 0) {
				category = (Category)getCachedData(RBTCacheKey.getCategoryIdLanguageCacheKey(Integer.parseInt(categoryId), language));
				if(category==null)
					category = (Category)getCachedData(RBTCacheKey.getCategoryIdCacheKey(Integer.parseInt(categoryId)));
			} else {
				basicLogger.warn("Category not available in cache with MM number " + mmNumber);
			}
		} else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATMMNUM, mmNumber);
			memcacheContentRequest.setLanguage(language);
			List<Category> objectList = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(objectList != null && objectList.size() > 0) {
				category = objectList.get(0);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				category = CategoriesDAO.getCategoryByMMNumber(mmNumber, language);
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		basicLogger.info("Category MM number: " + mmNumber + " Time taken: " + (System.currentTimeMillis() - now) + " Category: " + category);
		return category;
	}

	/**
	 * Gets all the clips mapped in the category with category id
	 * @param categoryId
	 * @param offset The start index of the clips. Starts from 0.
	 * @param rowCount The total values to return.
	 * @return
	 */
	public Clip[] getClipsInCategory(int categoryId, int offset, int rowCount) {
		return getClipsInCategory(categoryId, offset, rowCount, null);
	}
	
	
	/**
	 * Gets all the clips mapped in the category with category id in the language passed
	 * @param categoryId
	 * @param offset The start index of the clips. Starts from 0.
	 * @param rowCount The total values to return.
	 * @return
	 */
	public Clip[] getClipsInCategory(int categoryId, int offset, int rowCount, String language) {
		return getClipsInCategory(categoryId, offset, rowCount, language, null, null);
	}
	
	public Clip[] getClipsInNonBICategory(int categoryId, int offset, int rowCount, String language) {
		basicLogger.info("Inside getClipsInCategory(int categoryId, int offset, int rowCount, String language)");
		Clip[] result = null; 

		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String [])getCachedData(RBTCacheKey.getClipsInCategoryCacheKey(categoryId));		
			if(null == clipsIdArray || clipsIdArray.length == 0) {
				if(basicLogger.isInfoEnabled()) {
					basicLogger.warn("Clips are not found in cache under category id " + categoryId);
				}
				return null;
			}

			if(offset >= clipsIdArray.length) {
				basicLogger.warn("Index out of range. StartIndex value should be less than  " + clipsIdArray.length);				
				return null;
			}

			if(rowCount == -1) {
				rowCount = clipsIdArray.length;
			}

			int endIndex = offset + rowCount;
			if(endIndex > clipsIdArray.length)
				endIndex = clipsIdArray.length;
			//-------Get the clipkeys in language passed
			for(int i=0;i<clipsIdArray.length;i++) {
				String[] clipId = clipsIdArray[i].split("_");
				try {
					//doing safety check if the client is passing some alphabets in the cateory ids
					clipsIdArray[i] = RBTCacheKey.getClipIdLanguageCacheKey(Integer.parseInt(clipId[1]), language);
					if(clipsIdArray[i]==null)
						clipsIdArray[i] = RBTCacheKey.getClipIdCacheKey(Integer.parseInt(clipId[1]));
				}
				catch(Exception e) {
					basicLogger.error("Exception");
				   continue;	
				}
			}
			String[] dest = new String[endIndex - offset];
			System.arraycopy(clipsIdArray, offset, dest, 0, (endIndex - offset));
			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(dest);
			objects = getResultArrayForNullObject(objects, dest, false);
			if(null == objects) {
				basicLogger.warn("No clips found for category "+ categoryId);
				return null;
			}
			int cnt=0;
			result = new Clip[objects.length];
			for(int i=0; i<objects.length; i++) {
				
				if(objects[i]!=null){
					result[i] = (Clip)objects[i];
					cnt++;
				}
				
			}
			if(cnt==0){
				result=getClipsInCategory(categoryId);
			}
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCLIPSINCATEGORY, categoryId + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(list != null) {
				result = list.toArray(new Clip[0]);
			}
		} 
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Clip> list = ClipsDAO.getClipsInCategory(categoryId, language);

				if(null == list || list.size() == 0) {
					basicLogger.warn("Clips are not found in db under category id " + categoryId);
					return null;
				}

				if(offset >= list.size()) {
					basicLogger.warn("Index out of range. StartIndex value should be less than  " + list.size());				
					return null;
				}

				if(rowCount == -1) {
					rowCount = list.size();
				}

				int endIndex = offset + rowCount;
				if(endIndex > list.size())
					endIndex = list.size();

				result = new Clip[endIndex - offset];
				for(int i=offset, j = 0; i<endIndex; i++, j++) {
					result[j] = list.get(i);
				}

			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return result;
	}

	/**
	 * Gets all the clips mapped in the category with category id
	 * @param categoryId
	 * @return
	 */
	public Clip[] getClipsInCategory(int categoryId) {
		return getClipsInCategory(categoryId, 0, -1);
	}
	
	/**
	 * Gets all the clips mapped in the category with category id in the language passed
	 * @param categoryId
	 * @return
	 */
	public Clip[] getClipsInCategory(int categoryId, String language) {
		return getClipsInCategory(categoryId, 0, -1, language);
	}

	/**
	 * Gets all the categories mapped in the circle.
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes) {
		return getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1);
	}
	
	/**
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, int offset, int rowCount) {
		return getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, offset, rowCount);
	}
	
	/**
	 * Gets all the categories mapped in the circle.
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage using for Category Browsing
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String browsingLanguage) {
		return getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, browsingLanguage, 0, -1);
	}
	
	/**
	 * Gets all the categories mapped in the circle.
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage using for Category Browsing
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String browsingLanguage, int offset, int rowCount) {
		return getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, offset, rowCount, browsingLanguage);
	}
	
	/**
	 * Gets All Categories mapped in the circle in specific browsingLanguage
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language using for category circle map language
	 * @param browsingLanguage using for Category Browsing 
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String language, String browsingLanguage) {
		return getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language, 0, -1, browsingLanguage);
	}

	/**
	 * Gets all the categories mapped in the circle in specific language
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language
	 * @param offset
	 * @param rowCount
	 * @param browsingLanguage using for Category Browsing
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String language, int offset, int rowCount, String browsingLanguage) {
		Category[] categories = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		boolean isFromCategories = false;
		//RBT-12024
		String supportPlatformConsolidation = RBTContentJarParameters.getInstance().getParameter("PLATFORM_CONSOLIDATION_FEATURE_ENABLE");
		boolean isSupportPlatforConsolidation = supportPlatformConsolidation != null && supportPlatformConsolidation.equalsIgnoreCase("true");
		if(isSupportPlatforConsolidation && circleId!=null && circleId.contains("_")) {
				circleId=circleId.substring(circleId.indexOf("_")+1);
		}
		
		//Get BI sub categories types of parent category id from configuration
		String categoriesTypesFromConfig = RBTContentJarParameters.getInstance().getParameter("BI_CATEGORIES_CATEGORY_TYPES");
		List<String> categoriesTypesList = RBTContentUtils.getBICategoryTypeList(categoriesTypesFromConfig);
		
		Category category = getCategory(parentCategoryId, language);
		
		if(category != null && null != categoriesTypesList && !categoriesTypesList.isEmpty()
			&&	categoriesTypesList.contains(Integer.toString(category.getCategoryTpe()))) {
			isFromCategories = true;
			basicLogger.info("subcategory search flow");
			categories = getCategoriesFromBI(category, language,false, circleId, isFromCategories, offset, rowCount);
			List<Category> categoriesLst = Arrays.asList(categories);
			return RBTContentUtils.getPagination(categoriesLst, offset, rowCount);
		}
		
		else if(cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String [])getCachedData(RBTCacheKey.getCategoriesInCircleCacheKey(circleId, parentCategoryId, prepaidYes, language));
			if(null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger.warn("Categories are not found in cache for parentCategoryId " + parentCategoryId + " under circle id " + circleId);
				return null;
			}

			if(offset >= categoryIdsArray.length){
				basicLogger.warn("Index out of range. StartIndex value should be less than  " + categoryIdsArray.length);				
				return null;
			}
			if(rowCount == -1){
				rowCount = categoryIdsArray.length;
			}

			int endIndex = offset + rowCount;
			if(endIndex > categoryIdsArray.length)
				endIndex = categoryIdsArray.length;
			//-------Get the categorykeys in language passed
			for(int i=0;i<categoryIdsArray.length;i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					//doing safety check if the client is passing some alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey.getCategoryIdLanguageCacheKey(Integer.parseInt(categoryId[1]), browsingLanguage);
				}
				catch(Exception e) {
					basicLogger.error("browsing Langaue: " + browsingLanguage + " Exception",e);
				   continue;	
				}
			}
			
			String[] dest = new String[endIndex - offset];
			System.arraycopy(categoryIdsArray, offset, dest, 0, (endIndex - offset));

			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(dest);
			objects = getResultArrayForNullObject(objects, dest, true);
			if(null == objects) {
				basicLogger.warn("Categories are not found in cache for parentCategoryId "
						+ parentCategoryId + " under circle id " + circleId);
				return null;
			}
			categories = new Category[objects.length];
			for(int i=0; i<objects.length; i++) {
				categories[i] = (Category)objects[i];
			}
		} 
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATINCIRCLE, parentCategoryId + "");
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			memcacheContentRequest.setBrowsingLanguage(browsingLanguage);			
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list != null) {
				categories = list.toArray(new Category[0]);
			}
		} 
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Category> list = CategoriesDAO.getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language, browsingLanguage);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Categories are not found in db for parentCategoryId " + parentCategoryId + " under circle id " + circleId);
					}
					return null;
				}
				
				return RBTContentUtils.getPagination(list, offset, rowCount);
				
			 } catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return categories;
	}

	/**
	 * Gets All Categories mapped in the circle in specific browsingLanguage
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage using for Category Browsing 
	 * @return
	 */
	public Category[] getCategoriesInCircleByLanguage(String circleId, int parentCategoryId, char prepaidYes, String browsingLanguage) {
		return getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1, browsingLanguage);
	}
	
	/**
	 * Gets Active Categories mapped in the circle in specific browsingLanguage
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage using for Category Browsing 
	 * @return
	 */
	public Category[] getActiveCategoriesInCircleByLanguage(String circleId, int parentCategoryId, char prepaidYes, String browsingLanguage) {
		return getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1, browsingLanguage);
	}
	
	public Object getCachedData(String key) {
//		System.out.println("Getting from the memcache");
		basicLogger.info("Getting from the memcache");
		if(key == null || key.length() == 0 || key.length() > 250)
			return null;
		return RBTCache.getMemCachedClient().get(key);
	}
	
	public Clip[] getActiveClipsInCategory(int categoryId, int offset, int rowCount, String language, String subscriberId, StringBuffer activeClipCount) {
		
		return getActiveClipsInCategory(categoryId, offset, rowCount,  language, subscriberId, activeClipCount, null);
				
	}
	
//	public Clip[] getActiveClipsInCategoryFromRE(String subscriberId, int categoryId, String circleId) {
//		Clip[] clips = null;
//		Category category = getCategory(categoryId, null);
//		List<String> reCategoryTypesList = RBTContentUtils.getRECategoryTypeList();
//		if (category != null && reCategoryTypesList != null && reCategoryTypesList.contains(Integer.toString(category.getCategoryTpe()))) {
//			clips = getClipsFromRE(subscriberId, categoryId+"", circleId);
//		}
//		return clips;
//	}
	
	
	public Clip[] getActiveClipsInCategory(int categoryId, String language, String subscriberId, StringBuffer activeClipCount) {
		return getActiveClipsInCategory(categoryId, 0, -1, language, subscriberId, activeClipCount);
		
	}
	
	public Clip[] getClipsInCategory(int categoryId, int offset, int rowCount, String language, String subscriberId, StringBuffer activeClipCount) {
		Clip[] clips = null;
		//Get BI category types from configuration
		
		//Get Category from Memcache
		Category category = getCategory(categoryId, language);
//
//		List<String> categoryTypesList = RBTContentUtils.getBICategoryTypeList();
//	
//		if(category != null && categoryTypesList != null && categoryTypesList.contains(Integer.toString(category.getCategoryTpe()))) {
		if(isBICategoryType(categoryId, language)){
			clips = getClipsFromBI(category, subscriberId, language, offset, rowCount, activeClipCount, false, null, null);			
		}		
		else{
			clips = getClipsInNonBICategory(categoryId, offset, rowCount, language);
			if(activeClipCount != null) {
				activeClipCount.append(getClipsCountInCategory(categoryId));
			}
		}
		
		return clips;
	}
	
	public Clip[] getClipsInCategory(int categoryId, String language, String subscriberId, StringBuffer activeClipCount) {
		return getClipsInCategory(categoryId, 0, -1, language, subscriberId, activeClipCount);
	}
	
	public Category[] getCategoriesFromBI(Category category,String language,boolean isActiveCategory,String circleId, boolean isFromCategory, int offset, int rowCount ) {
		Category[] subCategories = null;
		if(language == null) {
			language = RBTContentJarParameters.getInstance().getParameter("default_language");
		}
		String className = RBTContentJarParameters.getInstance().getParameter("BI_CLASS_"+ category.getCategoryTpe());
		if(className == null) {
			basicLogger.debug("Implementation class is not configured. configuration name " + "BI_CLASS_"+ category.getCategoryTpe());
			return null;
		}
		BIInterface bi = null;
		try {
			bi = (BIInterface) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			basicLogger.error("Exception: while instantiate BI implementation class. Please check configuration: " + "BI_CLASS_"+ category.getCategoryTpe(),e);
		} catch (IllegalAccessException e) {
			basicLogger.error("Exception: while instantiate BI implementation class. Please check configuration: " + "BI_CLASS_"+ category.getCategoryTpe(),e);
		} catch (ClassNotFoundException e) {
			basicLogger.error("Exception: while instantiate BI implementation class. Please check configuration: " + "BI_CLASS_"+ category.getCategoryTpe(),e);
		}
		if(bi == null) {
			return null;			
		}
		
		subCategories = (Category[]) bi.process(category, null, circleId,isActiveCategory, language, null, isFromCategory , null);
		
		basicLogger.info("sub categories length" + subCategories.length + " of parent categoryId " + category.getCategoryId());
		
		if(null == subCategories || subCategories.length == 0) {
			basicLogger.error("No Cetogories are being mapped");
			return null;
		}
		return subCategories;
	}
	
	public Clip[] getClipsFromBI(Category category, String subscriberId, String language, int offset, int rowCount, StringBuffer activeClipCount,boolean doReturnActiveClips, String circleId, String appName) {
		Clip[] clips = null;
	
		if(language == null) {
			language = RBTContentJarParameters.getInstance().getParameter("default_language");
		}
		
		//Implement new code
		String className = RBTContentJarParameters.getInstance().getParameter("BI_CLASS_"+ category.getCategoryTpe());
		if(className == null) {
			basicLogger.debug("Implementation class is not configured. configuration name " + "BI_CLASS_"+ category.getCategoryTpe());
			return null;
		}
		BIInterface bi = null;
		try {
			bi = (BIInterface) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			basicLogger.error("Exception: while instantiate BI implementation class. Please check configuration: " + "BI_CLASS_"+ category.getCategoryTpe(),e);
		} catch (IllegalAccessException e) {
			basicLogger.error("Exception: while instantiate BI implementation class. Please check configuration: " + "BI_CLASS_"+ category.getCategoryTpe(),e);
		} catch (ClassNotFoundException e) {
			basicLogger.error("Exception: while instantiate BI implementation class. Please check configuration: " + "BI_CLASS_"+ category.getCategoryTpe(),e);
		}
		if(bi == null) {
			return null;			
		}
		if(subscriberId == null) {
			basicLogger.info("Taking subscriberId from Configuration, because client doesn't pass subscriberId");
			subscriberId = RBTContentJarParameters.getInstance().getParameter("BI_DEFAULT_SUBSCRIBERID");
		}
		clips = (Clip[])bi.process(category, subscriberId, circleId, doReturnActiveClips, language, appName, false , null);
		
		if(clips == null || clips.length == 0) {
			basicLogger.error("No Clips are being mapped");
			return null;
		}
		
		int clipSize = clips.length;
		if(activeClipCount != null ) {
			activeClipCount.append(clipSize);
		}
		
		if(offset >= clipSize) {
			basicLogger.warn("Index out of range. StartIndex value should be less than  " + clipSize);				
			return null;
		}

		if(rowCount == -1){
			rowCount = clipSize;
		}

		int endIndex = offset + rowCount;
		if(endIndex > clipSize)
			endIndex = clipSize;

		
		Clip[] tempClip = new Clip[endIndex - offset];
		System.arraycopy(clips, offset, tempClip, 0, (endIndex - offset));
		return tempClip;

	}
	
	
	public Clip[] getActiveClipsInCategory(int categoryId, int offset, int rowCount, String language, String subscriberId, StringBuffer activeClipCount, String circleId, String appName) {
//		Clip[] clips = null;
//		
//		clips = REOnmobileRecommendation.getClipsFromRE(subscriberId, catId, circleId);
//		return clips;
		
		Clip[] clips = null;
		
		//Get Category from Memcache
		Category category = getCategory(categoryId, language);

		//Get BI category types from configuration
		List<String> categoryTypesList = RBTContentUtils.getBICategoryTypeList(null);
		
		if(category != null && categoryTypesList != null && categoryTypesList.contains(Integer.toString(category.getCategoryTpe()))) {
			// Get clips from BI 
			clips = getClipsFromBI(category, subscriberId, language, offset, rowCount, activeClipCount, true, circleId, appName);
		} 
		else{
			//Get clips form Memcache
			clips = getActiveClipsInNonBICategory(categoryId, offset, rowCount, language, appName);
			if(activeClipCount != null) {
				activeClipCount.append(getActiveClipsCountInCategory(categoryId));
			}
		}
		
		return clips;

	}
	
	/**
	 * Gets Active clips mapped in the category with category id
	 * @param categoryId
	 * @param offset The start index of the clips. Starts from 0.
	 * @param rowCount The total values to return.
	 * @param language
	 * @param subscriberId
	 * @param activeClipCount
	 * @param circleId
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(int categoryId, int offset, int rowCount, String language, String subscriberId, StringBuffer activeClipCount, String circleId) {
		return getActiveClipsInCategory(categoryId, offset, rowCount, language, subscriberId, activeClipCount, circleId, null);
	}
	/**
	 * Gets Active clips mapped in the category with category id
	 * @param categoryId
	 * @param offset The start index of the clips. Starts from 0.
	 * @param rowCount The total values to return.
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(int categoryId, int offset, int rowCount) {
		return getActiveClipsInCategory(categoryId, offset, rowCount, null, null, null);
	}
	
	/**
	 * Gets Active clips mapped in the category with category id in a specific language
	 * @param categoryId
	 * @param offset The start index of the clips. Starts from 0.
	 * @param rowCount The total values to return.
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(int categoryId, int offset, int rowCount, String language) {
		return getActiveClipsInCategory(categoryId, offset, rowCount, language, null, null);
	}
	
	
	private Clip[] getActiveClipsInNonBICategory(int categoryId, int offset, int rowCount, String language, String appName) {
		//XXX
		Clip[] result = null; 

		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String [])getCachedData(RBTCacheKey.getActiveClipsInCategoryCacheKey(categoryId));		

			if(null == clipsIdArray || clipsIdArray.length == 0) {
				basicLogger.warn("Clips are not found in cache under category id " + categoryId);
				return null;
			}

			if(offset >= clipsIdArray.length) {
				basicLogger.warn("Index out of range. StartIndex value should be less than  " + clipsIdArray.length);				
				return null;
			}

			if(rowCount == -1){
				rowCount = clipsIdArray.length;
			}

			int endIndex = offset + rowCount;
			if(endIndex > clipsIdArray.length)
				endIndex = clipsIdArray.length;
			
			//-------Get the clipkeys in language passed
			for(int i=0;i<clipsIdArray.length;i++) {
				String[] clipId = clipsIdArray[i].split("_");
				try {
					//doing safety check if the client is passing some alphabets in the cateory ids
					clipsIdArray[i] = RBTCacheKey.getClipIdLanguageCacheKey(Integer.parseInt(clipId[1]), language, appName);
				}
				catch(Exception e) {
					basicLogger.error("Exception");
				   continue;	
				}
			}
			
			String[] dest = new String[endIndex - offset];
			System.arraycopy(clipsIdArray, offset, dest, 0, (endIndex - offset));

			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(dest);
			objects = getResultArrayForNullObject(objects, dest, false, appName);
			if(null == objects) {
				return null;
			}
			

			result = new Clip[objects.length];
			for(int i=0; i<objects.length; i++) {
				result[i] = (Clip)objects[i];
			}
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYACTIVECLIPSINCATEGORY, categoryId + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setAppName(appName);
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(list != null) {
				result = list.toArray(new Clip[0]);
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Clip> list = ClipsDAO.getActiveClipsInCategory(categoryId, language);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Clips are not found in db under category id " + categoryId);
					}
					return null;
				}

				if(offset >= list.size()) {
					basicLogger.warn("Index out of range. StartIndex value should be less than  " + list.size());				
					return null;
				}

				if(rowCount == -1) {
					rowCount = list.size();
				}

				int endIndex = offset + rowCount;
				if(endIndex > list.size())
					endIndex = list.size();

				result = new Clip[endIndex - offset];
				for(int i=offset, j = 0; i<endIndex; i++, j++) {
					result[j] = list.get(i);
				}

			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return result;
	}
	private Object[] getResultArrayForNullObject(Object[] objects,String[] destKey, boolean isCategory){
		return getResultArrayForNullObject(objects, destKey, isCategory, null);	
	}
	private Object[] getResultArrayForNullObject(Object[] objects,String[] destKey, boolean isCategory, String appName){
		boolean isNullObject = false;
		for(int index =0; index < objects.length; index++){
			if(objects[index] == null){
				isNullObject = true;
				String[] arrays = destKey[index].split("_");
				if (isCategory) {
					destKey[index] = RBTCacheKey.getCategoryIdLanguageCacheKey(Integer.parseInt(arrays[1]), null, appName);
				}
				else{
					destKey[index] = RBTCacheKey.getClipIdLanguageCacheKey(Integer.parseInt(arrays[1]), null, appName);
				}
			}
		}
		if(isNullObject){
			objects = RBTCache.getMemCachedClient().getMultiArray(destKey);
		}
		if (appName != null) {
			objects = getResultArrayForNullObject(objects, destKey, isCategory, null);
		}
		
		return objects;
	}

	/**
	 * Gets Active clips mapped in the category with category id
	 * @param categoryId
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(int categoryId) {
		return getActiveClipsInCategory(categoryId, null);
	}
	
	/**
	 * Gets Active clips mapped in the category with category id
	 * @param categoryId
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(int categoryId, String language, String appName) {
		return getActiveClipsInCategory(categoryId, 0, -1, language, null, null, null, appName);
	}
	
	/**
	 * Gets Active clips mapped in the category with category id
	 * @param categoryId
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(int categoryId, String language) {
		return getActiveClipsInCategory(categoryId, 0, -1, language);
	}

	/**
	 * Get Active clip count in category
	 * @param categoryid
	 * @return
	 */

	public int getActiveClipsCountInCategory(int categoryId) {
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String [])getCachedData(RBTCacheKey.getActiveClipsInCategoryCacheKey(categoryId));		
			if(null == clipsIdArray || clipsIdArray.length == 0) {
				basicLogger.warn("Clips are not found in cache under category id " + categoryId);
				return 0;
			}
			return clipsIdArray.length;
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYACTIVECLIPSINCATEGORY, categoryId + "");			
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(list != null) {
				return list.size();
			}
			else{
				basicLogger.warn("Clips are not found in webservice under category id " + categoryId);
				return 0;
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Clip> list = ClipsDAO.getActiveClipsInCategory(categoryId);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Clips are not found in db under category id " + categoryId);
					}
					return 0;
				}
				return list.size();
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return -1;
	}

	/**
	 * Get All the clip count in category
	 * @param categoryid
	 * @return
	 */
	public int getClipsCountInCategory(int categoryId){
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String [])getCachedData(RBTCacheKey.getClipsInCategoryCacheKey(categoryId));		
			if(null == clipsIdArray || clipsIdArray.length == 0) {
				basicLogger.warn("Clips are not found in cache under category id " + categoryId);
				return 0;
			}
			return clipsIdArray.length;
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCLIPSINCATEGORY, categoryId + "");
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(list != null) {
				return list.size();
			}
			else {
				basicLogger.warn("Clips are not found in webservice under category id " + categoryId);
				return 0;
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Clip> list = ClipsDAO.getClipsInCategory(categoryId);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Clips are not found in db under category id " + categoryId);
					}
					return 0;
				}
				return list.size();
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return -1;
	}
	
	public int getCategoriesCountInCircle(String circleId, int parentCategoryId, char prepaidYes) {
		return getCategoriesCountInCircle(circleId, parentCategoryId, prepaidYes, null);
	}

	/**
	 * Get All the category count in circle
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language for CategoryCircleMap
	 * @return
	 */
	public int getCategoriesCountInCircle(String circleId, int parentCategoryId, char prepaidYes, String language) {
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		//RBT-12024
		String supportPlatformConsolidation = RBTContentJarParameters.getInstance().getParameter("PLATFORM_CONSOLIDATION_FEATURE_ENABLE");
		boolean isSupportPlatforConsolidation = supportPlatformConsolidation != null && supportPlatformConsolidation.equalsIgnoreCase("true");
		if(isSupportPlatforConsolidation && circleId!=null && circleId.contains("_")) {
				circleId=circleId.substring(circleId.indexOf("_")+1);
		}
		if(cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String [])getCachedData(RBTCacheKey.getCategoriesInCircleCacheKey(circleId, parentCategoryId, prepaidYes, language));
			if(null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger.warn("Categories are not found in cache under circle id " + circleId);
				return 0;
			}
			return categoryIdsArray.length;
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATINCIRCLE, parentCategoryId + "");
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			memcacheContentRequest.setLanguage(language);
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list == null) {
				basicLogger.warn("Categories are not found in webservice under circle id " + circleId);
				return 0;
			}
			return list.size();
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Category> list = CategoriesDAO.getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Categories are not found in db for parentCategoryId " + parentCategoryId + " under circle id " + circleId);
					}
					return 0;
				}
				return list.size();
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return -1;
	}

	/**
	 * Gets Active categories mapped in the circle.
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes) {
		return getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1);
	}
	
	/**
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, int offset, int rowCount) {
		return getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, offset, rowCount);
	}

	/**
	 * Gets Active categories mapped in the circle.
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage only for category browsing 
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String browsingLanguage) {
		return getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, browsingLanguage, 0, -1);
	}
	
	/**
	 * Gets Active categories mapped in the circle.
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage only for category browsing 
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String browsingLanguage, int offset, int rowCount) {
		return getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, offset, rowCount, browsingLanguage);
	}

	/**
	 * Gets Active categories mapped in the circle in specific categoryLanguage
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language using for category circle map language
	 * @param categoryLangauge using for Category Browsing 
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String language, String browsingLanguage) {
		return getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language, 0, -1, browsingLanguage);
	}
	
	
	/**
	 * Gets Active categories mapped in the circle in specific categoryLanguage
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language using for category circle map language
	 * @param categoryLangauge using for Category Browsing 
	 * @param appName used for app specific default language
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId, int parentCategoryId, char prepaidYes, String language, String browsingLanguage, String appName) {
		return getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language, 0, -1, browsingLanguage, appName);
	}
	
	
	/**
	 * Gets Active categories mapped in the circle in specific categoryLanguage
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language using for category circle map language
	 * @param categoryLangauge using for Category Browsing 
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId,
			int parentCategoryId, char prepaidYes, String language, int offset,
			int rowCount, String browsingLanguage) {
		return getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language, offset, rowCount, browsingLanguage, null);
	}
	
	//RBT-14540
	/**
	 * Gets active sub-categories which were modified after modifiedSince date.
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language
	 * @param browsingLanguage
	 * @param appName
	 * @param modifiedSince
	 * @return
	 */
	public Set<Category> getActiveCategoriesModified(String circleId,
			int parentCategoryId, char prepaidYes, String language, String browsingLanguage, String appName, Date modifiedSince) {
		return getActiveCategoriesModified(circleId, parentCategoryId, prepaidYes, language, browsingLanguage, appName, modifiedSince, null);
	}
	
	public Set<Category> getActiveCategoriesModified(String circleId,
			int parentCategoryId, char prepaidYes, String language, String browsingLanguage, String appName, Date modifiedSince,int offset, int maxResults) {
		return getActiveCategoriesModified(circleId, parentCategoryId, prepaidYes, language, browsingLanguage, appName, modifiedSince, null,offset,maxResults);
	}
	
	
	//RBT-14540
	private Set<Category> getActiveCategoriesModified(String circleId,
			int parentCategoryId, char prepaidYes, String language, String browsingLanguage, String appName, Date modifiedSince, Set<Category> resultCategories) {
		if (resultCategories == null) {
			// In the first call of the recursion resultCategories will be null.
			resultCategories = new LinkedHashSet<Category>();
		}
		
		// Get the active sub-categories in the parentCategory
		Category[] arrOfCategories = getActiveCategoriesInCircle(circleId,
				parentCategoryId, prepaidYes, language, 0, -1, browsingLanguage, appName);

		if (arrOfCategories == null) {
			return null;
		}
		// Iterate over the obtained sub-categories. If a sub-category is modified, it is
		// added to the resultSet. Else recursive call is done on it.
		for (int i = 0; i < arrOfCategories.length; i++) {
			Category category = arrOfCategories[i];
			if (modifiedSince.compareTo(category.getLastModifiedTime()) == -1
					|| modifiedSince.compareTo(category.getLastModifiedTime()) == 0) {
				resultCategories.add(category);
			} else {
				getActiveCategoriesModified(circleId, category.getCategoryId(),
						prepaidYes, language,
						browsingLanguage, appName, modifiedSince,
						resultCategories);
			}
		}
		return resultCategories;
	}

	
	private Set<Category> getActiveCategoriesModified(String circleId,
			int parentCategoryId, char prepaidYes, String language, String browsingLanguage, String appName, Date modifiedSince, Set<Category> resultCategories,int offset,int maxResults) {
		if (resultCategories == null) {
			// In the first call of the recursion resultCategories will be null.
			resultCategories = new LinkedHashSet<Category>();
		}
		
		// Get the active sub-categories in the parentCategory
		Category[] arrOfCategories = getActiveCategoriesInCircle(circleId,
				parentCategoryId, prepaidYes, language, offset, maxResults, browsingLanguage, appName);

		if (arrOfCategories == null) {
			return null;
		}
		// Iterate over the obtained sub-categories. If a sub-category is modified, it is
		// added to the resultSet. Else recursive call is done on it.
		for (int i = 0; i < arrOfCategories.length; i++) {
			Category category = arrOfCategories[i];
			if (modifiedSince.compareTo(category.getLastModifiedTime()) == -1
					|| modifiedSince.compareTo(category.getLastModifiedTime()) == 0) {
				resultCategories.add(category);
			} else {
				getActiveCategoriesModified(circleId, category.getCategoryId(),
						prepaidYes, language,
						browsingLanguage, appName, modifiedSince,
						resultCategories,offset,maxResults);
			}
		}
		return resultCategories;
	}
	/**
	 * Gets Active categories mapped in the circle in specific categoryLanguage
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language using for category circle map language
	 * @param offset
	 * @param rowCount
	 * @param categoryLangauge using for Category Browsing 
	 * @param appName used for app specific default language
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId,
			int parentCategoryId, char prepaidYes, String language, int offset,
			int rowCount, String browsingLanguage, String appName) {
		//XXX
		Category[] categories = null;

		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		boolean isFromCategories = false;
		//RBT-12024
		String supportPlatformConsolidation = RBTContentJarParameters.getInstance().getParameter("PLATFORM_CONSOLIDATION_FEATURE_ENABLE");
		boolean isSupportPlatforConsolidation = supportPlatformConsolidation != null && supportPlatformConsolidation.equalsIgnoreCase("true");
		if(isSupportPlatforConsolidation && circleId!=null && circleId.contains("_")) {
			circleId=circleId.substring(circleId.indexOf("_")+1);
		}
		
		//Get BI sub categories types of parent category id from configuration
		String categoriesTypesFromConfig = RBTContentJarParameters.getInstance().getParameter("BI_CATEGORIES_CATEGORY_TYPES");
		List<String> categoriesTypesList = RBTContentUtils.getBICategoryTypeList(categoriesTypesFromConfig);
		
		
		Category category = getCategory(parentCategoryId, language);
		
		if(category != null && null != categoriesTypesList && !categoriesTypesList.isEmpty()
			&&	categoriesTypesList.contains(Integer.toString(category.getCategoryTpe()))) {
			isFromCategories = true;
			basicLogger.info("subcategory BI flow");
			categories = getCategoriesFromBI(category, language,true, circleId, isFromCategories, offset, rowCount);
			List<Category> categoriesLst = Arrays.asList(categories);
			return RBTContentUtils.getPagination(categoriesLst, offset, rowCount);
		}
		
		else if(cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String [])getCachedData(RBTCacheKey.getActiveCategoriesInCircleCacheKey(circleId, parentCategoryId, prepaidYes, language));
			if(null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger.warn("Categories are not found in cache under circle id " + circleId);
				return null;
			}

			if(offset >= categoryIdsArray.length){
				basicLogger.warn("Index out of range. StartIndex value should be less than  " + categoryIdsArray.length);				
				return null;
			}
			if(rowCount == -1){
				rowCount = categoryIdsArray.length;
			}

			int endIndex = offset + rowCount;
			if(endIndex > categoryIdsArray.length)
				endIndex = categoryIdsArray.length;
			//-------Get the categorykeys in language passed
			for(int i=0;i<categoryIdsArray.length;i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					//doing safety check if the client is passing some alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey.getCategoryIdLanguageCacheKey(Integer.parseInt(categoryId[1]), browsingLanguage, appName);
				}
				catch(Exception e) {
					basicLogger.error("browsing: " + browsingLanguage + " Exception",e);
				   continue;	
				}
			}
			String[] dest = new String[endIndex - offset];
			System.arraycopy(categoryIdsArray, offset, dest, 0, (endIndex - offset));

			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(dest);
			objects = getResultArrayForNullObject(objects, dest, true, appName);
			if(null == objects) {
				return null;
			}
			categories = new Category[objects.length];
			for(int i=0; i<objects.length; i++) {
				categories[i] = (Category)objects[i];
			}
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYACTCATINCIRCLE, parentCategoryId + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setAppName(appName);
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			memcacheContentRequest.setBrowsingLanguage(browsingLanguage);
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list != null) {
				categories = list.toArray(new Category[0]);
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Category> list = CategoriesDAO.getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language, browsingLanguage);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Categories are not found in db for parentCategoryId " + parentCategoryId + " under circle id " + circleId);
					}
					return null;
				}
				return RBTContentUtils.getPagination(list, offset, rowCount);

			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		return categories;
	}
	
	
	/**
	 * Gets Artist Index based on character input
	 * For this api to work memcache should be initialized at all times
	 * API supports pagination and can be used for all SF's
	 * @param character
	 * @param offset
	 * @param rowCount
	 * @return the String array of artists
	 * @author Ankit.Kanchan
	 */
	public String[] getClipArtistIndex(Character c, int offset, int rowCount) {
		String[] result = null;

		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			//String[] categoryIdsArray = (String [])getCachedData(RBTCacheKey.getActiveCategoriesInCircleCacheKey(circleId, parentCategoryId, prepaidYes, language));
			basicLogger.info("Cache is alive and initialized...appropriate time for fetching artist data");
			TreeSet<String> artistNameSet = (TreeSet<String>)getCachedData(RBTCacheKey.getArtistNameInitialCacheKey(c));
			String[] artistIndexArray  = artistNameSet.toArray(new String[artistNameSet.size()]);
			if(null == artistNameSet || artistNameSet.size() == 0) {
				basicLogger.warn("Artist Index not found for " + c);
				return null;
			}

			if(offset >= artistIndexArray.length){
				basicLogger.warn("Index out of range. StartIndex value should be less than  " + artistIndexArray.length);				
				return null;
			}
			if(rowCount == -1){
				rowCount = artistIndexArray.length;
			}

			int endIndex = offset + rowCount;
			if(endIndex > artistIndexArray.length)
				endIndex = artistIndexArray.length;
			
			
			String[] dest = new String[endIndex - offset];
			System.arraycopy(artistIndexArray, offset, dest, 0, (endIndex - offset));

			/*Object objects[] = RBTCache.getMemCachedClient().getMultiArray(dest);
			objects = getResultArrayForNullObject(objects, dest, true);
			if(null == objects) {
				return null;
			}
			result = new String[objects.length];
			for(int i=0; i<objects.length; i++) {
				result[i] = (String)objects[i];
			}*/
			result = new String[dest.length];
			for(int i=0;i<dest.length;i++){
				result[i] = dest[i];
			}
		}
		return result;
	}

	
	public int getActiveCategoriesCountInCircle(String circleId, int parentCategoryId, char prepaidYes) {
		return getActiveCategoriesCountInCircle(circleId, parentCategoryId, prepaidYes, null);
	}

	/**
	 * Get Active category count in circle
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param langauge for Category circleMap
	 * @return
	 */
	public int getActiveCategoriesCountInCircle(String circleId, int parentCategoryId, char prepaidYes, String language) {
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		//RBT-12024
		String supportPlatformConsolidation = RBTContentJarParameters.getInstance().getParameter("PLATFORM_CONSOLIDATION_FEATURE_ENABLE");
		boolean isSupportPlatforConsolidation = supportPlatformConsolidation != null && supportPlatformConsolidation.equalsIgnoreCase("true");
		if(isSupportPlatforConsolidation && circleId!=null && circleId.contains("_")) {
				circleId=circleId.substring(circleId.indexOf("_")+1);
		}
		if(cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String [])getCachedData(RBTCacheKey.getActiveCategoriesInCircleCacheKey(circleId, parentCategoryId, prepaidYes, language));
			if(null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger.warn("Categories are not found in cache under circle id " + circleId);
				return 0;
			}
			return categoryIdsArray.length;
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYACTCATINCIRCLE, parentCategoryId + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list != null) {
				return list.size();
			}
			else {
				return 0;
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Category> list = CategoriesDAO.getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Categories are not found in db for parentCategoryId " + parentCategoryId + " under circle id " + circleId);
					}
					return 0;
				}
				return list.size();
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return -1;
	}

	/**
	 * Get the Category mapped with Category SMS Alias
	 * @param categorySMSAlias
	 * @return
	 */
	public Category getCategoryBySMSAlias(String categorySMSAlias){
		return getCategoryBySMSAlias(categorySMSAlias, null);
	}
	
	/**
	 * Get the Category mapped with Category SMS Alias in categorySpecific language
	 * @param categorySMSAlias
	 * @return
	 */
	public Category getCategoryBySMSAlias(String categorySMSAlias, String language){
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String categoryId = (String)getCachedData(RBTCacheKey.getCategorySmsAliasCacheKey(categorySMSAlias));
			if(null != categoryId && categoryId.length() > 0) {
				return getCategory(Integer.parseInt(categoryId), language);
			}
			basicLogger.warn("Category not found in cache with SmsAlias " + categorySMSAlias);
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATALIAS, categorySMSAlias);
			memcacheContentRequest.setLanguage(language);
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list != null && list.size() > 0) {
				return list.get(0);
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				Category category = CategoriesDAO.getCategoryBySmsAlias(categorySMSAlias, language);
				if(null == category) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("category not found in db with sms alias " + categorySMSAlias);
					}
				}

				return category;
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return null;
	}

	/**
	 * Get the Category mapped with the category name
	 * @param categoryName
	 * @return
	 */
	public Category getCategoryByName(String categoryName) {
		return getCategoryByName(categoryName, null);
	}
	
	/**
	 * Get the Category mapped with the category name by specific language
	 * @param categoryName
	 * @return
	 */
	public Category getCategoryByName(String categoryName, String language) {
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String categoryId = (String)getCachedData(RBTCacheKey.getCategoryNameCacheKey(categoryName));
			if(null != categoryId && categoryId.length() > 0) {
				return getCategory(Integer.parseInt(categoryId), language);
			}
			basicLogger.warn("Category not found in cache with categoryName " + categoryName);
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATNAME, categoryName);
			memcacheContentRequest.setLanguage(language);
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list != null && list.size() > 0) {
				return list.get(0);
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				Category category = CategoriesDAO.getCategoryByName(categoryName, language);
				if(null == category) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("category not found in db with categoryName " + categoryName);
					}
				}

				return category;
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return null;
	}

	/**
	 * Get the clips mapped with Clip Album (valid subscriber id)
	 * @param subscriberID
	 * @return
	 */
	public Clip[] getClipsByAlbum(String subscriberID) {
		return getClipsByAlbum(subscriberID, null);
	}
	
	/**
	 * Get the artist index mapped with the starting name
	 *
	 */
	public Set<String> getClipArtistIndex(Character initLetter){
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			basicLogger.info("Cache is alive and initialized...appropriate time for fetching artist data");
			TreeSet<String> artistNameSet = (TreeSet<String>)getCachedData(RBTCacheKey.getArtistNameInitialCacheKey(initLetter));
			return artistNameSet;
		} else{
		  basicLogger.warn("Cache is not initialized and running, Please check..");	
		}
		return null;
	}
	
	/**
	 * Get the clips mapped with Clip Album (valid subscriber id) in specific language
	 * @param subscriberID
	 * @return
	 */
	public Clip[] getClipsByAlbum(String subscriberID, String language) {
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String clipIdArray = (String)getCachedData(RBTCacheKey.getAlbumCacheKey(subscriberID));
			if(null == clipIdArray || clipIdArray.length() == 0) {
				basicLogger.warn("Clips are not found in cache under Album" + subscriberID);
				return null;
			}

			String[] clipIds = clipIdArray.split("\\,");

			Set<String> clipIdSet = new TreeSet<String>();
			for(int i=0; i<clipIds.length ; i++) {
				clipIdSet.add(clipIds[i]);
			}

			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(clipIdSet.toArray(new String[0]));
			if(null == objects) {
				return null;
			}
			Clip[] result = new Clip[objects.length];
			for(int i=0; i<objects.length; i++) {
				result[i] = (Clip)objects[i];
			}
			return result;

		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCLIPALBUM, subscriberID);
			memcacheContentRequest.setLanguage(language);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(list != null) {
				return list.toArray(new Clip[0]);
			}
			
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Clip> clipList = ClipsDAO.getClipsByAlbum(subscriberID, language);
				if(clipList != null && clipList.size() > 0){
					return (clipList.toArray(new Clip[0]));
				}

				if(basicLogger.isInfoEnabled()) {
					basicLogger.info("clips not found in db with album " + subscriberID);
				}
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return null;
	}

	/**
	 * Get the Clip from PromoMaster
	 * @param promoType
	 * @param promoCode
	 * @return
	 */
	public Clip getClipFromPromoMaster(String promoType, String promoCode){
		return getClipFromPromoMaster(promoType, promoCode, null);
	}
	
	/**
	 * Get the Clip from PromoMaster of a specific language
	 * @param promoType
	 * @param promoCode
	 * @return
	 */
	public Clip getClipFromPromoMaster(String promoType, String promoCode, String language){
		Clip clip = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String clipId = (String)getCachedData(RBTCacheKey.getPromoMasterCacheKey(promoCode, promoType));
			if(null != clipId && clipId.length() > 0) {
//				return (Clip)getClipByPromoId(clipId);
				clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if(clip==null)
					clip = (Clip)getCachedData(RBTCacheKey.getClipIdCacheKey(clipId));
				return clip;
			}
			basicLogger.warn("Clip not found in cache with promo master promocode[ " + promoCode
					+ "] promo type[" + promoType + "]");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				PromoMaster promoMaster = PromoMasterDAO.getPromoMaster(promoType, promoCode);		
				if(promoMaster == null) {
					basicLogger.info("Promomaster not found DB promocode[ " + promoCode 
							+ "] promo type[" + promoType + "]");
				} else {
//					clip = ClipsDAO.getClipByPromoId(promoMaster.getClipPromoId());
					clip = ClipsDAO.getClip(promoMaster.getClipId(), language);
				}
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
			return clip;
		}

		return null;
	}

	/**
	 * Get the Clip from PromoMaster
	 * @param promoCode
	 * @return
	 */
	public Clip getClipFromPromoMaster(String promoCode){
		return getClipFromPromoMasterByLanguage(promoCode, null);
	}
	
	/**
	 * Get the Clip from PromoMaster
	 * @param promoCode
	 * @return
	 */
	public Clip getClipFromPromoMasterByLanguage(String promoCode, String language){
		Clip clip = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String clipId = (String)getCachedData(RBTCacheKey.getPromoCodeCacheKey(promoCode));
			if(null != clipId && clipId.length() > 0) {
//				return (Clip)getClipByPromoId(clipId);
				clip = (Clip)getCachedData(RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if(clip==null)
					clip = (Clip)getCachedData(RBTCacheKey.getClipIdCacheKey(clipId));
				return clip;
			}
			basicLogger.warn("Clip not found in cache with promo master promocode[ " + promoCode + "]");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				PromoMaster promoMaster = PromoMasterDAO.getPromoMaster(promoCode);
				if(promoMaster == null){
					basicLogger.warn("Promomaster not found DB promocode[ " + promoCode + "]");
				} else {
//					clip = ClipsDAO.getClipByPromoId(promoMaster.getClipPromoId());
					clip = ClipsDAO.getClip(promoMaster.getClipId(), language);
				}
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
			return clip;
		}

		return null;
	}
	
	

	/**
	 * Get the Array of Category by category type
	 * @param categoryTYpe
	 * @return Category[]
	 */
	public Category[] getCategoryByType(String categoryType){
		return getCategoryByType(categoryType, null);
	}
	
	/**
	 * Get the Array of Active Category by category type
	 * @param categoryTYpe
	 * @return Category[]
	 */
	public Category[] getActiveCategoryByType(String categoryType){
		return getActiveCategoryByType(categoryType, null);
	}
	
	/**
	 * Get the Array of Active Category by category type in specific language
	 * @param categoryTYpe
	 * @return Category[]
	 */
	public Category[] getActiveCategoryByType(String categoryType, String language){
		Category[] allCategories = getCategoryByType(categoryType, language);
		List<Category> categoryList = new ArrayList<Category>();
		Date date = new Date();
		for(Category category : allCategories) {
			
			if(category != null &&( category.getCategoryStartTime().before(date) && category.getCategoryEndTime().after(date))) {
				categoryList.add(category);
			}			
		}
		return categoryList.toArray(new Category[0]);
	}
	
	/**
	 * Get the Array of Category by category type in specific language
	 * @param categoryTYpe
	 * @return Category[]
	 */
	public Category[] getCategoryByType(String categoryType, String language){
		Category[] result = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String [])getCachedData(RBTCacheKey.getCategoryTypeCacheKey(categoryType));
			if(null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger.warn("Categories are not found in cache for categoryType " + categoryType);
				return null;
			}
			//-------Get the categorykeys in language passed
			for(int i=0;i<categoryIdsArray.length;i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					//doing safety check if the client is passing some alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey.getCategoryIdLanguageCacheKey(Integer.parseInt(categoryId[1]), language);
				}
				catch(Exception e) {
					basicLogger.error("Exception");
				   continue;	
				}
			}
			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(categoryIdsArray);
			if(null == objects) {
				return null;
			}
			result = new Category[objects.length];
			for(int i=0; i<objects.length; i++) {
				result[i] = (Category)objects[i];
			}
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATTYPE, categoryType);
			memcacheContentRequest.setLanguage(language);
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list != null) {
				result = list.toArray(new Category[0]);
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Category> list = CategoriesDAO.getCategoryType(categoryType, language);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Categories are not found in db for categoryType " + categoryType);
					}
					return null;
				}

				result = list.toArray(new Category[0]);
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		return result;
	}

	
	/**
	 * Get the Array of Category by circleId, prepaidYes, categoryType
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryType
	 * @return
	 */
	public Category[] getCategoryByType(String circleId, char prepaidYes, String categoryType){
		return getCategoryByType(circleId, prepaidYes, categoryType, null);
	}
	
	/**
	 * Get the Array of Active Category by circleId, prepaidYes, categoryType
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryType
	 * @return
	 */
	public Category[] getActiveCategoryByType(String circleId, char prepaidYes, String categoryType){
		return getActiveCategoryByType(circleId, prepaidYes, categoryType, null);
	}
	
	/**
	 * Get the Array of Active Category by circleId, prepaidYes, categoryType in specific language
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryType
	 * @param language
	 * @return
	 */
	public Category[] getActiveCategoryByType(String circleId, char prepaidYes, String categoryType, String language){
		Category[] categories = getCategoryByType(circleId, prepaidYes, categoryType, language);
		List<Category> categoryList = new ArrayList<Category>();
		Date date = new Date();
		for(Category category : categories) {
			
			if(category != null &&( category.getCategoryStartTime().before(date) && category.getCategoryEndTime().after(date))) {
				categoryList.add(category);
			}			
		}
		return categoryList.toArray(new Category[0]);
	}
	
	/**
	 * Get the Array of Category by circleId, prepaidYes, categoryType in specific language
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryType
	 * @param language
	 * @return
	 */
	public Category[] getCategoryByType(String circleId, char prepaidYes, String categoryType, String language){
		Category[] result = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String [])getCachedData(RBTCacheKey.getTypePrepadiCircleIdCacheKey(circleId, prepaidYes, categoryType));
			if(null == categoryIdsArray || categoryIdsArray.length == 0) {
				if(basicLogger.isInfoEnabled()) {
					basicLogger.info("Categories are not found in db for circle Id " + circleId + " , prepaidYes " + prepaidYes + " , categoryType " + categoryType);
				}
				return null;
			}
			//-------Get the categorykeys in language passed
			for(int i=0;i<categoryIdsArray.length;i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					//doing safety check if the client is passing some alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey.getCategoryIdLanguageCacheKey(Integer.parseInt(categoryId[1]), language);
				}
				catch(Exception e) {
					basicLogger.error("Exception");
				   continue;	
				}
			}
			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(categoryIdsArray);
			if(null == objects) {
				return null;
			}
			result = new Category[objects.length];
			for(int i=0; i<objects.length; i++) {
				result[i] = (Category)objects[i];
			}
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATTYPE, categoryType);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setCircleID(circleId);			
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list != null) {
				result = list.toArray(new Category[0]);
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Category> list = CategoriesDAO.getCategoryType(circleId, prepaidYes, categoryType, language);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Categories are not found in db for circle Id " + circleId + " , prepaidYes " + prepaidYes + " , categoryType " + categoryType);
					}
					return null;
				}

				result = list.toArray(new Category[0]);

			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		return result;
	}

	private Category[] categoryTypeNotRequired(Category[] category, List<Integer> categoryTypesNotRequired, int offset, int rowCount){
		List<Category> categoryList = new ArrayList<Category>();
		for(int i = 0; i < category.length; i++){
			if(!categoryTypesNotRequired.contains(category[i].getCategoryTpe())){
				categoryList.add(category[i]);
			}
		}
		category = categoryList.toArray(new Category[0]);
		if(offset >= category.length){
			basicLogger.warn("Index out of range. StartIndex value should be less than  " + category.length);				
			return null;
		}
		if(rowCount == -1){
			rowCount = category.length;
		}

		int endIndex = offset + rowCount;
		if(endIndex > category.length)
			endIndex = category.length;
		
		Category[] dest = new Category[endIndex - offset];
		System.arraycopy(category, offset, dest, 0, (endIndex - offset));

		return dest;
	}
	
	
	
	/**
	 * Get the Array of active Categories from circle with the circleId, prepaidYes, parentCategoryId, list of category type which in not required
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId, char prepaidYes, int parentCategoryId, List<Integer> categoryTypesNotRequired) {
		Category[] category = getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1);
		return categoryTypeNotRequired(category, categoryTypesNotRequired, 0 ,-1);
	}
	
	/**
	 * Get the Array of all categories from circle with circleId, prepaidYes, parentCategoryId, list of category type which is not required
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleId, char prepaidYes, int parentCategoryId, List<Integer> categoryTypesNotRequired) {
		Category[] category = getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1);
		if(category == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Categories are not found in db for parentCategoryId " + parentCategoryId + " under circle id " + circleId);
			}
			return null;
		}
		if(categoryTypesNotRequired == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("category type list is null");
			}
			return null;
		}
		return categoryTypeNotRequired(category, categoryTypesNotRequired, 0 , -1);
		
	}
	
	/**
	 * Get the Array of all categories from circle with circleId, prepaidYes, parentCategoryId, list of category type which is not required by language
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleId, char prepaidYes, int parentCategoryId, List<Integer> categoryTypesNotRequired, String language) {
		basicLogger.debug("CircleID: " + circleId + " prepaidYes: " + prepaidYes + " parentCategoryId: " + parentCategoryId + " categoryTypeNotRequired: " + categoryTypesNotRequired + " langage: " + language);
		Category[] category = getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1);
		if(category == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Categories are not found in db for parentCategoryId " + parentCategoryId + " under circle id " + circleId);
			}
			return null;
		}
		basicLogger.debug("Category size : " + category.length);
		if(categoryTypesNotRequired == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("category type list is null");
			}
			return null;
		}
		return categoryTypeNotRequired(category, categoryTypesNotRequired, 0 , -1);
		
	}
	
	
	/**
	 * Get the Array of all categories from circle with circleId, prepaidYes, parentCategoryId, list of category type which is not required by language
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId, char prepaidYes, int parentCategoryId, List<Integer> categoryTypesNotRequired, String language) {
		basicLogger.debug("CircleID: " + circleId + " prepaidYes: " + prepaidYes + " parentCategoryId: " + parentCategoryId + " categoryTypeNotRequired: " + categoryTypesNotRequired + " langage: " + language);
		Category[] category = getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1);
		if(category == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Categories are not found in db for parentCategoryId " + parentCategoryId + " under circle id " + circleId);
			}
			return null;
		}
		basicLogger.debug("Category size : " + category.length);
		if(categoryTypesNotRequired == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("category type list is null");
			}
			return null;
		}
		return categoryTypeNotRequired(category, categoryTypesNotRequired, 0 , -1);
		
	}
	
	/**
	 * Get the Array of active categories from circle with circleId, prepaidYes, parentCategoryId, list of category type which is not required
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleId, char prepaidYes, int parentCategoryId, List<Integer> categoryTypesNotRequired, int offset, int rowCount) {
		Category[] category = getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1);
		return categoryTypeNotRequired(category, categoryTypesNotRequired, offset, rowCount);
	}
	
	/**
	 * Get the Array of all categories from circle with circleId, prepaidYes, parentCategoryId, list of category type which is not required
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleId, char prepaidYes, int parentCategoryId, List<Integer> categoryTypesNotRequired, int offset, int rowCount) {
		Category[] category = getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, null, 0, -1);
		if(category == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Categories are not found in db for parentCategoryId " + parentCategoryId + " under circle id " + circleId);
			}
			return null;
		}
		if(categoryTypesNotRequired == null){
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("category type list is null");
			}
			return null;
		}
		return categoryTypeNotRequired(category, categoryTypesNotRequired, offset, rowCount);
		
	}

	/**
	 * Get the array of category with promoId
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryPromoId
	 * @return
	 */
	public Category[] getCategoryByPromoId(String circleId, char prepaidYes, String categoryPromoId){
		return getCategoryByPromoId(circleId, prepaidYes, categoryPromoId, null);
	}
	
	/**
	 * Get the array of category with promoId in specific language
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryPromoId
	 * @return
	 */
	public Category[] getCategoryByPromoId(String circleId, char prepaidYes, String categoryPromoId, String language){
		Category[] result = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String [])getCachedData(RBTCacheKey.getPromoIdPrepadiCircleIdCacheKey(circleId, prepaidYes, categoryPromoId));
			if(null == categoryIdsArray || categoryIdsArray.length == 0) {
				if(basicLogger.isInfoEnabled()) {
					basicLogger.info("Categories are not found in db for circle Id " + circleId + " , prepaidYes " + prepaidYes + " , categoryPromoId " + categoryPromoId);
				}
				return null;
			}
			//-------Get the categorykeys in language passed
			for(int i=0;i<categoryIdsArray.length;i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					//doing safety check if the client is passing some alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey.getCategoryIdLanguageCacheKey(Integer.parseInt(categoryId[1]), language);
				}
				catch(Exception e) {
					basicLogger.error("Exception");
				   continue;	
				}
			}
			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(categoryIdsArray);
			if(null == objects) {
				return null;
			}
			result = new Category[objects.length];
			for(int i=0; i<objects.length; i++) {
				result[i] = (Category)objects[i];
			}
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATARRPROMOID, categoryPromoId);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setCircleID(circleId);			
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list != null) {
				result = list.toArray(new Category[0]);
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Category> list = CategoriesDAO.getCategoryByPromoId(circleId, prepaidYes, categoryPromoId, language);

				if(null == list || list.size() == 0) {
					if(basicLogger.isInfoEnabled()) {
						basicLogger.info("Categories are not found in db for circle Id " + circleId + " , prepaidYes " + prepaidYes + " , categoryPromoId " + categoryPromoId);
					}
					return null;
				}

				result = list.toArray(new Category[0]);

			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		return result;
	}

	
	/**
	 * To get the clips from cache
	 * @param clipIds
	 * @return Clip[]
	 */
	public Clip[] getClips(String[] clipIds){
		return getClips(clipIds, null);
	}
	
	/**
	 * To get the clips from cache in language passed
	 * @param clipIds
	 * @param language for Browsing clip language
	 * @return Clip[]
	 */
	public Clip[] getClips(String[] clipIds, String language) {
		return getClips(clipIds, language, null);
	}
	/**
	 * To get the clips from cache in language passed
	 * @param clipIds
	 * @param language for Browsing clip language
	 * @param appName used for app specific default language
	 * @return Clip[]
	 */
	public Clip[] getClips(String[] clipIds, String language, String appName) {
		//XXX
		if(clipIds == null || clipIds.length == 0){
			basicLogger.warn("Array is null or doesn't contain clipids");
			return null;
		}
		Clip[] clipObjects = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			for(int i=0;i<clipIds.length;i++) {
				try {
					//doing safety check if the client is passing some alphabets in the cateory ids
					clipIds[i] = RBTCacheKey.getClipIdLanguageCacheKey(Integer.parseInt(clipIds[i]), language, appName);
				}
				catch(Exception e) {
				   continue;	
				}
			}
			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(clipIds);
			objects = getResultArrayForNullObject(objects, clipIds, false, appName);
			clipObjects = new Clip[objects.length];
			for(int i=0;i<objects.length;i++) {
				clipObjects[i] = (Clip)objects[i];
			}
			return clipObjects;
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			String contentIds = "";
			for(String clipId : clipIds) {
				contentIds += clipId + ",";
			}
			contentIds = contentIds.substring(0, contentIds.length() - 1);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCLIPS, contentIds);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setAppName(appName);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(memcacheContentRequest);
			if(list != null) {
				return list.toArray(new Clip[0]);
			}
			else {
				return null;
			}
		}
		else{
			try{
				List<Integer> clipIdlist = new ArrayList<Integer>();
				for(int i=0;i<clipIds.length;i++){
					try{
						//doing safety check if the client is passing some alphabets in the cateory ids
						clipIdlist.add(Integer.parseInt(clipIds[i]));
					}
					catch(Exception e){
						clipIdlist.add(-1);
					}
				}
				List<Clip> clipList = ClipsDAO.getClips(clipIdlist, language);
				clipObjects = clipList.toArray(new Clip[0]);
				return clipObjects;
			}
			catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		return clipObjects;
	}
	
	/**
	 * To get the categories from cache
	 * @param categoryIds
	 * @return Category[]
	 */
	public Category[] getCategories(String[] categoryIds){
		return getCategories(categoryIds, null);
	}
	
	/**
	 * To get the categories from cache in specific language
	 * @param categoryIds
	 * @param language for Browsing category language
	 * @return Category[]
	 */
	public Category[] getCategories(String[] categoryIds, String language){
		if(categoryIds == null || categoryIds.length == 0){
			basicLogger.warn("Array is null or doesn't contain categoryIds");
			return null;
		}
		Category[] categoryObjects = null;
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {			
			for(int i=0;i<categoryIds.length;i++){
				try {
					//doing safety check if the client is passing some alphabets in the cateory ids
					categoryIds[i] = RBTCacheKey.getCategoryIdLanguageCacheKey(Integer.parseInt(categoryIds[i]), language);
				}
				catch(Exception e){
					continue;
				}
			}
			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(categoryIds);
			objects = getResultArrayForNullObject(objects, categoryIds, true);
			categoryObjects = new Category[objects.length];
			for(int i=0;i<objects.length;i++){
				categoryObjects[i] = (Category)objects[i];
			}
			return categoryObjects;
		}
		else if(fallbackLogic != null && fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)){			
			basicLogger.warn("Getting data from Webservice. Reason: cacheAlive? " + cacheAlive + " cacheInitialized? " + cacheInitialized);
			String contentIds = "";
			for(String clipId : categoryIds) {
				contentIds += clipId + ",";
			}
			contentIds = contentIds.substring(0, contentIds.length() - 1);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(WebServiceConstants.BYCATIDS, contentIds);
			memcacheContentRequest.setLanguage(language);
			List<Category> list = RBTClient.getInstance().getCategoryFromWebservice(memcacheContentRequest);
			if(list != null) {
				return list.toArray(new Category[0]);
			}
			else {
				return null;
			}
		}
		else{
			try{
				List<Integer> categoryIdList = new ArrayList<Integer>();
				for(int i=0;i<categoryIds.length;i++){
					try {
						//doing safety check if the client is passing some alphabets in the cateory ids
						categoryIdList.add(Integer.parseInt(categoryIds[i]));
					}
					catch(Exception e){
						categoryIdList.add(-1);
					}
				}
				List<Category> categoryList = CategoriesDAO.getCategories(categoryIdList, language);
				categoryObjects = categoryList.toArray(new Category[0]);
				return categoryObjects;
			}
			catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		return categoryObjects;
	}

	public boolean isClipMappedToCatgeory(String clipId, String categoryId){
		boolean isMapped = false;
		basicLogger.info("Inside isClipMappedToCatgeory(String clipId, String categoryId)");
		int categoryID;
		try{
			categoryID = Integer.parseInt(categoryId);
		}
		catch(NumberFormatException e){
			basicLogger.error(e);
			basicLogger.info("Inside getClipsInCategory : Error while conver the categoryID from string to int [ categorYId - " + categoryId + "]");
			return false;
		}

		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if(cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String [])getCachedData(RBTCacheKey.getClipsInCategoryCacheKey(categoryID));		
			if(null == clipsIdArray || clipsIdArray.length == 0) {
				if(basicLogger.isInfoEnabled()) {
					basicLogger.warn("Clips are not found in cache under category id " + categoryId);
				}
				return false;
			}

			for(int i=0;i<clipsIdArray.length;i++) {
				String[] tempClipID = clipsIdArray[i].split("_");
				try{
					if(tempClipID[1].equals(clipId)){
						isMapped = true;
						break;
					}
				}
				catch(ArrayIndexOutOfBoundsException ae){
					continue;
				}
			}

			if(isMapped){
				basicLogger.info("Inside getClipsInCategory : clip ID (" + clipId + ") is mapped to this category (" + categoryId + ")");
			}
			else{
				basicLogger.info("Inside getClipsInCategory : clip ID (" + clipId + ") is not mapped to this category (" + categoryId + ")");
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Getting data from DB. Reason: cacheAlive? ").append(cacheAlive).append(" ");
			sb.append("cacheInitialized? ").append(cacheInitialized);
			basicLogger.warn(sb.toString());
			try {
				List<Clip> list = ClipsDAO.getClipsInCategory(categoryID);

				if(null == list || list.size() == 0) {
					basicLogger.warn("Clips are not found in db under category id " + categoryId);
					return false;
				}

				int listSize = list.size();				
				for(int i = 0; i < listSize; i++){
					Clip clip = list.get(i);
					if(clipId.equals(clip.getClipId()+"")){
						isMapped = true;
						break;
					}
				}
				
				if(isMapped){
					basicLogger.info("Inside getClipsInCategory : clip ID (" + clipId + ") is mapped to this category (" + categoryId + ")");
				}
				else{
					basicLogger.info("Inside getClipsInCategory : clip ID (" + clipId + ") is not mapped to this category (" + categoryId + ")");
				}
			} catch(DataAccessException dae) {
				basicLogger.error(dae);
			}
		}

		return isMapped;
	}
	
	public ClipRating getClipRating(int clipId)
	{
		long now = System.currentTimeMillis();

		ClipRating clipRating = null;
		try {
			clipRating = ClipRatingDAO.getClipRating(clipId);
		} 
		catch(DataAccessException dae) {
			basicLogger.error(dae);
		}

		if (basicLogger.isInfoEnabled())
			basicLogger.info("ClipRating " + clipRating + " Time taken: " + (System.currentTimeMillis() - now) + " ClipRating: " + clipRating);

		return clipRating;
	}
	
	public Map<Integer, ClipRating> getClipsRatings(List<Integer> clipIds)
	{
		long now = System.currentTimeMillis();

		Map<Integer, ClipRating> clipsRatings = null;
		try {
			clipsRatings = ClipRatingDAO.getClipsRatings(clipIds);
		} 
		catch(DataAccessException dae) {
			basicLogger.error(dae);
		}

		if (basicLogger.isInfoEnabled())
			basicLogger.info("ClipsRating " + clipsRatings + " Time taken: " + (System.currentTimeMillis() - now) + " ClipsRating: " + clipsRatings);

		return clipsRatings;
	}
	
	public ClipRating rateClip(int clipId, int rating)
	{
		ClipRating clipRating = null;

		ClipRatingTransaction clipRatingTransaction = new ClipRatingTransaction();

		clipRatingTransaction.setClipId(clipId);
		clipRatingTransaction.setNoOfVotes(1);
		clipRatingTransaction.setRatingDate(new Date());
		clipRatingTransaction.setSumOfRatings(rating);

		try {
			ClipRatingTransactionDAO.saveClipRatingTransaction(clipRatingTransaction);
			clipRating = ClipRatingDAO.getClipRating(clipId);
		} 
		catch (ConstraintViolationException cve) 
		{
			try {
				ClipRatingTransactionDAO.rateClip(clipId, rating);
				clipRating = ClipRatingDAO.getClipRating(clipId);
			}
			catch (DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		catch (DataAccessException dae) {
			basicLogger.error(dae);
		}
		return clipRating;
	}

	public ClipRating likeClip(int clipId)
	{
		ClipRating clipRating = null;

		ClipRatingTransaction clipRatingTransaction = new ClipRatingTransaction();

		clipRatingTransaction.setClipId(clipId);
		clipRatingTransaction.setLikeVotes(1);
		clipRatingTransaction.setRatingDate(new Date());

		try {
			ClipRatingTransactionDAO.saveClipRatingTransaction(clipRatingTransaction);
			clipRating = ClipRatingDAO.getClipRating(clipId);
		} 
		catch (ConstraintViolationException cve) 
		{
			try {
				ClipRatingTransactionDAO.likeClip(clipId);
				clipRating = ClipRatingDAO.getClipRating(clipId);
			}
			catch (DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		catch (DataAccessException dae) {
			basicLogger.error(dae);
		}
		return clipRating;
	}

	public ClipRating dislikeClip(int clipId)
	{
		ClipRating clipRating = null;

		ClipRatingTransaction clipRatingTransaction = new ClipRatingTransaction();

		clipRatingTransaction.setClipId(clipId);
		clipRatingTransaction.setDislikeVotes(1);
		clipRatingTransaction.setRatingDate(new Date());

		try {
			ClipRatingTransactionDAO.saveClipRatingTransaction(clipRatingTransaction);
			clipRating = ClipRatingDAO.getClipRating(clipId);
		} 
		catch (ConstraintViolationException cve) 
		{
			try {
				ClipRatingTransactionDAO.dislikeClip(clipId);
				clipRating = ClipRatingDAO.getClipRating(clipId);
			}
			catch (DataAccessException dae) {
				basicLogger.error(dae);
			}
		}
		catch (DataAccessException dae) {
			basicLogger.error(dae);
		}
		return clipRating;
	}
	
	/**
	 * To retrieve Set<String> of albumName based on language and starting letter of album. Supports pagination.
	 * @param language
	 * @param startingLetter
	 * @param offset
	 * @param rowCount To be set as -1 so as to return the full result.
	 * @return an ArrayList<Object> with 0th element returning the total size of the result set and 1st element a Set<String>.
	 */
	public ArrayList<Object> getAlbumNameByLanguage(String language, Character startingLetter, int offset, int rowCount) {
		if (basicLogger.isDebugEnabled()) {
			basicLogger.debug("language: " + language + ", startingLetter: " + startingLetter + ", offset: " + offset + ", rowCount: " + rowCount);
		}
		ArrayList<Object> result = new ArrayList<Object>();
		ArrayList<Object> nullResult = new ArrayList<Object>(Arrays.asList(0, null));
		Set<String> resultSet = null;
		
		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if (cacheAlive && cacheInitialized) {
			basicLogger.info("Cache is alive and initialized. ");
			@SuppressWarnings("unchecked")
			Set<String> albumNameSet = (Set<String>)getCachedData(RBTCacheKey.getAlbumNameByLanguageKey(language, startingLetter));
			if (albumNameSet == null || albumNameSet.size() == 0) {
				basicLogger.warn("Albums not found found for language: " + language + " and startingLetter: " + startingLetter);
				return nullResult;
			}
			if (offset >= albumNameSet.size()){
				basicLogger.warn("Index out of range. StartIndex value should be less than  " +  albumNameSet.size());				
				return nullResult;
			}
			String[] albumNameArray  = albumNameSet.toArray(new String[albumNameSet.size()]);
			CaseInsensitiveStringComparator caseInsensitiveStringComparator = new CaseInsensitiveStringComparator();
			Arrays.sort(albumNameArray, caseInsensitiveStringComparator);
			if (rowCount == -1) {
				rowCount = albumNameArray.length;
			}

			int endIndex = offset + rowCount;
			if(endIndex > albumNameArray.length) {
				endIndex = albumNameArray.length;
			}
			
			String[] dest = new String[endIndex - offset];
			System.arraycopy(albumNameArray, offset, dest, 0, (endIndex - offset));

			resultSet = new LinkedHashSet<String>();
			for (int i=0;i<dest.length;i++) {
				resultSet.add(dest[i]);
			}
			result.add(albumNameSet.size());
			result.add(resultSet);
		} else {
			basicLogger.warn("cache is not initialized and/or cache is not alive. cacheAlive: " + cacheAlive + ", cacheInitialized: " + cacheInitialized);
			return nullResult;
		}
		if (basicLogger.isDebugEnabled()) {
			basicLogger.debug("Result: " + result);
		}
		return result;
	}
	/**
	 * To retrieve Clip[] array based on albumName and language. Supports pagination.
	 * @param albumName
	 * @param language
	 * @param offset
	 * @param rowCount To be set as -1 so as to return the full result.
	 * @return an ArrayList<Object> with 0th element returning the total size of the result set and 1st element a Clip[] array.
	 */
	public ArrayList<Object> searchClipsByAlbumName(String albumName, String language, int offset, int rowCount) {
		if (basicLogger.isDebugEnabled()) {
			basicLogger.debug("albumname: " + albumName + ", language: " + language + ", offset: " + offset + ", rowCount: " + rowCount);
		}
		ArrayList<Object> result = new ArrayList<Object>();
		ArrayList<Object> nullResult = new ArrayList<Object>(Arrays.asList(0, null));
		
		Clip[] dest = null;
		List<Clip> clipList = new ArrayList<Clip>();

		boolean cacheAlive = RBTCache.isCacheAlive();
		boolean cacheInitialized = RBTCache.isCacheInitialized();
		if (cacheAlive && cacheInitialized) {
			basicLogger.info("Cache is alive and initialized. ");
			String clipIdArray = (String)getCachedData(RBTCacheKey.getAlbumCacheKey(albumName));
			if(clipIdArray == null|| clipIdArray.length() == 0) {
				basicLogger.warn("Clips not found found for language: " + language + " and albumName: " + albumName);
				return nullResult;
			}
			String[] clipIds = clipIdArray.split("\\,");

			Set<String> clipIdSet = new TreeSet<String>();
			for(int i=0; i<clipIds.length ; i++) {
				clipIdSet.add(clipIds[i]);
			}

			Object objects[] = RBTCache.getMemCachedClient().getMultiArray(clipIdSet.toArray(new String[0]));
			if(objects == null) {
				basicLogger.warn("Clips not found found for clipIdSet: " + clipIdSet);
				return nullResult;
			}
			for(int i = 0; i < objects.length; i++) {
				Clip clip = (Clip)objects[i];
				Date sysDate = new Date();
				if (clip != null && clip.getLanguage() != null
						&& clip.getLanguage().equalsIgnoreCase(language)
						&& clip.getClipStartTime().before(sysDate) 
						&& clip.getClipEndTime().after(sysDate)) {
					clipList.add(clip);
				}
			}

			if (clipList.size() == 0) {
				basicLogger.warn("Clips not found found for language: " + language + " and albumName: " + albumName);
				return nullResult;
			}
			if (offset >= clipList.size()){
				basicLogger.warn("Index out of range. StartIndex value should be less than  " + clipList.size());				
				return nullResult;
			}
			Clip[] clipArray  = clipList.toArray(new Clip[clipList.size()]);
			if (rowCount == -1) {
				rowCount = clipArray.length;
			}

			int endIndex = offset + rowCount;
			if(endIndex > clipArray.length) {
				endIndex = clipArray.length;
			}

			dest = new Clip[endIndex - offset];
			System.arraycopy(clipArray, offset, dest, 0, (endIndex - offset));
			result.add(clipList.size());
			result.add(dest);
		} else {
			basicLogger.warn("cache is not initialized and/or cache is not alive. cacheAlive: " + cacheAlive + ", cacheInitialized: " + cacheInitialized);
			return nullResult;
		}
		if (basicLogger.isDebugEnabled()) {
			basicLogger.debug("Result: " + result);
		}
		return result;
	}

	/**
	 * Returns the circleName->circleId map for sites with SiteUrl as null
	 * @return
	 */
	public static Map<String,String> getCircleMap() {
    	ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
    	Map<String, String> circleNameCircleIdMap = new HashMap<String, String>();
    	Site[] sites = RBTClient.getInstance().getSites(applicationDetailsRequest);
    	if (sites != null) {
    		for (Site site: sites) {
    			if (site.getSiteURL() == null) {
    				circleNameCircleIdMap.put(site.getSiteName(), site.getCircleID());
    			}
    		}
    	}
    	if (basicLogger.isDebugEnabled()) {
    		basicLogger.debug("circleNameCircleIdMap: " + circleNameCircleIdMap);
    	}
    	return circleNameCircleIdMap;
    }
	
	//RBT-12674-Ussd tones for you category is not displaying all 10 songs
	//This function will check the category clips are from BI or not.
	public boolean isBICategoryType(int categoryId, String language) {
		Category category = getCategory(categoryId, language);
		// Get BI category types from configuration
		List<String> categoryTypesList = RBTContentUtils
				.getBICategoryTypeList(null);
		if (category != null
				&& categoryTypesList != null
				&& categoryTypesList.contains(Integer.toString(category
						.getCategoryTpe()))) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String args[]) throws Exception {
		ArrayList<Object> set = RBTCacheManager.getInstance().getAlbumNameByLanguage("English", 'c', 1, 5);
		for (Object s: set) {
			System.out.println(s);
		}	
		
		/*ArrayList<Object> list = RBTCacheManager.getInstance().searchClipsByAlbumName("Club Hutch", "Hindi", 0, 5);
		System.out.println(list);
		for (Object s: list) {
			System.out.println(s);
		}
		Clip[] clips = (Clip[]) ((ArrayList<Object>)list).get(1);
		for (Clip clip : clips) {
			System.out.println(clip);
		}*/
	}
	
	public Clip[] getActiveClipsInCategory(int categoryId, int offset,
			int rowCount, String language, String subscriberId,
			StringBuffer activeClipCount, String circleId, String appName,
			boolean isUserLanguageSelected, boolean isSubscribed,
			int totalSize, String sessionID) {
		// Clip[] clips = null;
		//
		// clips = REOnmobileRecommendation.getClipsFromRE(subscriberId, catId,
		// circleId);
		// return clips;

		Clip[] clips = null;
		ClipInfoAction clipInfoAction = new ClipInfoAction(categoryId, offset,
				false, subscriberId, language, appName, circleId,
				isUserLanguageSelected, isSubscribed, totalSize, sessionID);

		// Get Category from Memcache
		Category category = getCategory(categoryId, language);

		// Get BI category types from configuration
		List<String> categoryTypesList = RBTContentUtils
				.getBICategoryTypeList(null);

		if (category != null
				&& categoryTypesList != null
				&& categoryTypesList.contains(Integer.toString(category
						.getCategoryTpe()))) {
			// Get clips from BI
			clips = getClipsFromBI(category, subscriberId, language, offset,
					rowCount, activeClipCount, true, circleId, appName ,clipInfoAction);
		} else {
			// Get clips form Memcache
			clips = getActiveClipsInNonBICategory(categoryId, offset, rowCount,
					language, appName);
			if (activeClipCount != null) {
				activeClipCount
						.append(getActiveClipsCountInCategory(categoryId));
			}
		}

		return clips;

	}
	
	public Clip[] getClipsFromBI(Category category, String subscriberId, String language, int offset, int rowCount, StringBuffer activeClipCount,boolean doReturnActiveClips, String circleId, String appName , ClipInfoAction clipInfoAction) {
		Clip[] clips = null;
	
		if(language == null) {
			language = RBTContentJarParameters.getInstance().getParameter("default_language");
		}
		
		//Implement new code
		String className = RBTContentJarParameters.getInstance().getParameter("BI_CLASS_"+ category.getCategoryTpe());
		if(className == null) {
			basicLogger.debug("Implementation class is not configured. configuration name " + "BI_CLASS_"+ category.getCategoryTpe());
			return null;
		}
		BIInterface bi = null;
		try {
			bi = (BIInterface) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			basicLogger.error("Exception: while instantiate BI implementation class. Please check configuration: " + "BI_CLASS_"+ category.getCategoryTpe(),e);
		} catch (IllegalAccessException e) {
			basicLogger.error("Exception: while instantiate BI implementation class. Please check configuration: " + "BI_CLASS_"+ category.getCategoryTpe(),e);
		} catch (ClassNotFoundException e) {
			basicLogger.error("Exception: while instantiate BI implementation class. Please check configuration: " + "BI_CLASS_"+ category.getCategoryTpe(),e);
		}
		if(bi == null) {
			return null;			
		}
		if(subscriberId == null) {
			basicLogger.info("Taking subscriberId from Configuration, because client doesn't pass subscriberId");
			subscriberId = RBTContentJarParameters.getInstance().getParameter("BI_DEFAULT_SUBSCRIBERID");
		}
		clips = (Clip[])bi.process(category, subscriberId, circleId, doReturnActiveClips, language, appName, false , clipInfoAction);
		
		if(clips == null || clips.length == 0) {
			basicLogger.error("No Clips are being mapped");
			return null;
		}
		
		int clipSize = clips.length;
		if(activeClipCount != null ) {
			activeClipCount.append(clipSize);
		}
		
		if(offset >= clipSize) {
			basicLogger.warn("Index out of range. StartIndex value should be less than  " + clipSize);				
			return null;
		}

		if(rowCount == -1){
			rowCount = clipSize;
		}

		int endIndex = offset + rowCount;
		if(endIndex > clipSize)
			endIndex = clipSize;

		
		Clip[] tempClip = new Clip[endIndex - offset];
		System.arraycopy(clips, offset, tempClip, 0, (endIndex - offset));
		return tempClip;

	}
}