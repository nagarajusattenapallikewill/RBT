package com.onmobile.apps.ringbacktones.rbtcontents.cache.multi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.RBTContentUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.MemcacheContentRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * This class provides all the required functionality for the RBT WEB, CCC GUI.
 * 
 * @author ganipisetty
 * 
 */
public class RBTMultiCacheManager {

	private static Logger basicLogger = Logger
			.getLogger(RBTMultiCacheManager.class);
	private static RBTMultiCacheManager instance = null;
	private static boolean initialized = false;
	private static String fallbackLogic = null;

	private static String DB_FALLBACK = "DB";
	private static String WEBSERVICE_FALLBACK = "WEBSERVICE";

	/**
	 * Constructor.
	 */
	protected RBTMultiCacheManager() {

	}

	/**
	 * Initilize the RBTMultiCacheManager
	 */
	private synchronized static void init() {
		if (initialized) {
			return;
		}
		instance = new RBTMultiCacheManager();
		fallbackLogic = RBTContentJarParameters.getInstance().getParameter(
				"fallback_logic");
		initialized = true;
	}

	public static RBTMultiCacheManager getInstance() {
		if (null == instance) {
			init();
		}
		return instance;
	}

	public String getClipPath(Clip clip, String operatorName) {
		String clipname = clip.getClipNameWavFile() + ".mp3";
		int clipId = clip.getClipId();
		String hex = Integer.toHexString(clipId);
		char[] hexString = hex.toCharArray();
		String folderPath = "";
		for (int j = 0; j < hexString.length; j++) {
			folderPath = folderPath + File.separator + hexString[j];
			folderPath = folderPath + File.separator + clipname;
		}
		return folderPath;
	}

	/**
	 * Gets the clip mapped with the clip id from cache.
	 * 
	 * @param clipId
	 * @return
	 */
	public Clip getClip(String circleName, int clipId) {
		return getClip(circleName, Integer.toString(clipId));
	}

	/**
	 * Gets the clip mapped with the clip id from cache.
	 * 
	 * @param clipId
	 * @return
	 */
	public Clip getClip(String circleName, int clipId, String language) {
		return getClip(circleName, Integer.toString(clipId), language);
	}

	/**
	 * Gets the clip in any other language, If null passed the default language
	 * meta data is returned
	 * 
	 * @param clipId
	 * @param language
	 * @return Clip
	 */
	public Clip getClip(String circleName, String clipId, String language) {
		long now = System.currentTimeMillis();

		Clip clip = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			clip = (Clip) getCachedData(circleName,
					RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
			if (clip == null)
				clip = (Clip) getCachedData(circleName,
						RBTCacheKey.getClipIdCacheKey(clipId));
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCLIPID, clipId);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> clipList = RBTClient.getInstance()
					.getClipFromWebservice(memcacheContentRequest);
			if (clipList != null && clipList.size() > 0) {
				clip = clipList.get(0);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		basicLogger.info("Clip Id " + clipId + " Time taken: "
				+ (System.currentTimeMillis() - now) + " Clip: " + clip);
		return clip;
	}

	/**
	 * Gets the clip mapped with the clip id from cache.
	 * 
	 * @param clipId
	 * @return
	 */
	public Clip getClip(String circleName, String clipId) {
		return getClip(circleName, clipId, null);
	}

	/**
	 * Gets the clip mapped with the promo id from cache in any of the language,
	 * if null passsed default language info is returned
	 * 
	 * @param promoId
	 * @return Clip
	 */
	public Clip getClipByPromoId(String circleName, String promoId,
			String language) {
		long now = System.currentTimeMillis();

		Clip clip = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String clipId = (String) getCachedData(circleName,
					RBTCacheKey.getPromoIdCacheKey(promoId));
			if (null != clipId && clipId.length() > 0) {
				clip = (Clip) getCachedData(circleName,
						RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if (clip == null)
					clip = (Clip) getCachedData(circleName,
							RBTCacheKey.getClipIdCacheKey(clipId));
			} else {
				basicLogger.warn("Clip not available in cache with promo id "
						+ promoId);
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCLIPPROMOID, promoId);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> clipList = RBTClient.getInstance()
					.getClipFromWebservice(memcacheContentRequest);
			if (clipList != null && clipList.size() > 0) {
				clip = clipList.get(0);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		basicLogger.info("Clip Promo Id " + promoId + " Time taken: "
				+ (System.currentTimeMillis() - now) + " Clip: " + clip);
		return clip;
	}

	/**
	 * Gets the clip mapped with the promo id from cache.
	 * 
	 * @param promoId
	 * @return
	 */
	public Clip getClipByPromoId(String circleName, String promoId) {
		return getClipByPromoId(circleName, promoId, null);
	}

	/**
	 * Gets the clip mapped with the RBT WAV file name from cache.
	 * 
	 * @param rbtWavFileName
	 * @return
	 */
	public Clip getClipByRbtWavFileName(String circleName, String rbtWavFileName) {
		return getClipByRbtWavFileName(circleName, rbtWavFileName, null);
	}

	/**
	 * Gets the clip mapped with the RBT WAV file name from cache based on the
	 * language passed
	 * 
	 * @param rbtWavFileName
	 * @return
	 */
	public Clip getClipByRbtWavFileName(String circleName,
			String rbtWavFileName, String language) {
		long now = System.currentTimeMillis();

		Clip clip = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		boolean tosupportClipVcode = Boolean
				.parseBoolean(RBTContentJarParameters.getInstance()
						.getParameter("clip_vcode"));
		if (cacheAlive && cacheInitialized) {
			String clipId = (String) getCachedData(circleName,
					RBTCacheKey.getRbtWavFileCacheKey(rbtWavFileName));

			basicLogger.debug("the tosupportClipVcode is configured to :"
					+ tosupportClipVcode);
			if (clipId == null && tosupportClipVcode) {
				String vcode = rbtWavFileName;
				if (rbtWavFileName.indexOf("rbt_") != -1
						&& rbtWavFileName.lastIndexOf("_rbt") != -1) {
					vcode = rbtWavFileName.substring("rbt_".length(),
							rbtWavFileName.lastIndexOf("_rbt"));
				}
				basicLogger.info("rbtwavefile is :" + rbtWavFileName
						+ " vcode is :" + vcode);
				clipId = (String) getCachedData(circleName,
						RBTCacheKey.getVcodeCacheKey(vcode));
			}

			if (null != clipId && clipId.length() > 0) {
				clip = (Clip) getCachedData(circleName,
						RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if (clip == null)
					clip = (Clip) getCachedData(circleName,
							RBTCacheKey.getClipIdCacheKey(clipId));
			} else {
				basicLogger
						.warn("Clip not available in cache with RBT WAV file "
								+ rbtWavFileName);
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCLIPWAVFILE, rbtWavFileName);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> clipList = RBTClient.getInstance()
					.getClipFromWebservice(memcacheContentRequest);
			if (clipList != null && clipList.size() > 0) {
				clip = clipList.get(0);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		basicLogger.info("Clip RBT WAV file name: " + rbtWavFileName
				+ " Time taken: " + (System.currentTimeMillis() - now)
				+ " Clip: " + clip);
		return clip;
	}

	/**
	 * Gets the clip mapped with the SMS alias from cache.
	 * 
	 * @param smsAlias
	 * @return
	 */
	public Clip getClipBySMSAlias(String circleName, String smsAlias) {
		return getClipBySMSAlias(circleName, smsAlias, null);
	}

	/**
	 * Gets the clip mapped with the SMS alias from cache based on language
	 * passed
	 * 
	 * @param smsAlias
	 * @return
	 */
	public Clip getClipBySMSAlias(String circleName, String smsAlias,
			String language) {
		long now = System.currentTimeMillis();

		Clip clip = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String clipId = (String) getCachedData(circleName,
					RBTCacheKey.getSmsAliasCacheKey(smsAlias));
			if (null != clipId && clipId.length() > 0) {
				clip = (Clip) getCachedData(circleName,
						RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if (clip == null)
					clip = (Clip) getCachedData(circleName,
							RBTCacheKey.getClipIdCacheKey(clipId));
			} else if (fallbackLogic != null
					&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
				basicLogger
						.warn("Getting data from Webservice. Reason: cacheAlive? "
								+ cacheAlive
								+ " cacheInitialized? "
								+ cacheInitialized);
				MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
						WebServiceConstants.BYCLIPALIAS, smsAlias);
				memcacheContentRequest.setLanguage(language);
				memcacheContentRequest.setOperatroID(circleName);
				List<Clip> clipList = RBTClient.getInstance()
						.getClipFromWebservice(memcacheContentRequest);
				if (clipList != null && clipList.size() > 0) {
					clip = clipList.get(0);
				}
			} else {
				basicLogger.warn("Clip not available in cache with SMS alias "
						+ smsAlias);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		basicLogger.info("Clip SMS alias: " + smsAlias + " Time taken: "
				+ (System.currentTimeMillis() - now) + " Clip: " + clip);
		return clip;
	}

	/**
	 * Gets the category mapped with the category id from cache.
	 * 
	 * @param categoryId
	 * @return
	 */
	public Category getCategory(String circleName, int categoryId) {
		return getCategory(circleName, categoryId, null);
	}

	/**
	 * Gets the category mapped with the category id from cache in a specific
	 * language
	 * 
	 * @param categoryId
	 * @return
	 */
	public Category getCategory(String circleName, int categoryId,
			String language) {
		long now = System.currentTimeMillis();
		Category category = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			category = (Category) getCachedData(circleName,
					RBTCacheKey.getCategoryIdLanguageCacheKey(categoryId,
							language));
			if (category == null)
				category = (Category) getCachedData(circleName,
						RBTCacheKey.getCategoryIdCacheKey(categoryId));
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATID, categoryId + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> objectList = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (objectList != null && objectList.size() > 0) {
				category = objectList.get(0);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		basicLogger
				.info("Category id: " + categoryId + " Time taken: "
						+ (System.currentTimeMillis() - now) + " Category: "
						+ category);
		return category;
	}

	/**
	 * Gets the category mapped with the promo id from cache.
	 * 
	 * @param promoId
	 * @return
	 */
	public Category getCategoryByPromoId(String circleName, String promoId) {
		return getCategoryByPromoId(circleName, promoId, null);
	}

	/**
	 * Gets the category mapped with the promo id from cache in a specific
	 * language
	 * 
	 * @param promoId
	 * @return
	 */
	public Category getCategoryByPromoId(String circleName, String promoId,
			String language) {
		long now = System.currentTimeMillis();

		Category category = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String categoryId = (String) getCachedData(circleName,
					RBTCacheKey.getCategoryPromoIdCacheKey(promoId));
			if (null != categoryId && categoryId.length() > 0) {
				category = (Category) getCachedData(
						circleName,
						RBTCacheKey.getCategoryIdLanguageCacheKey(
								Integer.parseInt(categoryId), language));
				if (category == null)
					category = (Category) getCachedData(circleName,
							RBTCacheKey.getCategoryIdCacheKey(Integer
									.parseInt(categoryId)));
			} else {
				basicLogger
						.warn("Category not available in cache with Promo id "
								+ promoId);
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATPROMOID, promoId);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> objectList = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (objectList != null && objectList.size() > 0) {
				category = objectList.get(0);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		basicLogger
				.info("Category Promo id: " + promoId + " Time taken: "
						+ (System.currentTimeMillis() - now) + " Category: "
						+ category);
		return category;
	}

	/**
	 * Gets the category mapped with the MMNumber from cache.
	 * 
	 * @param promoId
	 * @return
	 */
	public Category getCategoryByMmNumber(String circleName, String mmNumber) {
		return getCategoryByMmNumber(circleName, mmNumber, null);
	}

	/**
	 * Gets the category mapped with the MMNumber from cache in a specific
	 * language
	 * 
	 * @param promoId
	 * @return
	 */
	public Category getCategoryByMmNumber(String circleName, String mmNumber,
			String language) {
		long now = System.currentTimeMillis();

		Category category = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String categoryId = (String) getCachedData(circleName,
					RBTCacheKey.getCategoryMMNumberCacheKey(mmNumber));
			if (null != categoryId && categoryId.length() > 0) {
				category = (Category) getCachedData(
						circleName,
						RBTCacheKey.getCategoryIdLanguageCacheKey(
								Integer.parseInt(categoryId), language));
				if (category == null)
					category = (Category) getCachedData(circleName,
							RBTCacheKey.getCategoryIdCacheKey(Integer
									.parseInt(categoryId)));
			} else {
				basicLogger
						.warn("Category not available in cache with MM number "
								+ mmNumber);
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATMMNUM, mmNumber);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> objectList = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (objectList != null && objectList.size() > 0) {
				category = objectList.get(0);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		basicLogger
				.info("Category MM number: " + mmNumber + " Time taken: "
						+ (System.currentTimeMillis() - now) + " Category: "
						+ category);
		return category;
	}

	/**
	 * Gets all the clips mapped in the category with category id
	 * 
	 * @param categoryId
	 * @param offset
	 *            The start index of the clips. Starts from 0.
	 * @param rowCount
	 *            The total values to return.
	 * @return
	 */
	public Clip[] getClipsInCategory(String circleName, int categoryId,
			int offset, int rowCount) {
		return getClipsInCategory(circleName, categoryId, offset, rowCount,
				null);
	}

	/**
	 * Gets all the clips mapped in the category with category id in the
	 * language passed
	 * 
	 * @param categoryId
	 * @param offset
	 *            The start index of the clips. Starts from 0.
	 * @param rowCount
	 *            The total values to return.
	 * @return
	 */
	public Clip[] getClipsInCategory(String circleName, int categoryId,
			int offset, int rowCount, String language) {
		basicLogger
				.info("Inside getClipsInCategory(int categoryId, int offset, int rowCount, String language)");
		Clip[] result = null;

		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getClipsInCategoryCacheKey(categoryId));
			if (null == clipsIdArray || clipsIdArray.length == 0) {
				if (basicLogger.isInfoEnabled()) {
					basicLogger
							.warn("Clips are not found in cache under category id "
									+ categoryId);
				}
				return null;
			}

			if (offset >= clipsIdArray.length) {
				basicLogger
						.warn("Index out of range. StartIndex value should be less than  "
								+ clipsIdArray.length);
				return null;
			}

			if (rowCount == -1) {
				rowCount = clipsIdArray.length;
			}

			int endIndex = offset + rowCount;
			if (endIndex > clipsIdArray.length)
				endIndex = clipsIdArray.length;
			// -------Get the clipkeys in language passed
			for (int i = 0; i < clipsIdArray.length; i++) {
				String[] clipId = clipsIdArray[i].split("_");
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					clipsIdArray[i] = RBTCacheKey.getClipIdLanguageCacheKey(
							Integer.parseInt(clipId[1]), language);
					if (clipsIdArray[i] == null)
						clipsIdArray[i] = RBTCacheKey.getClipIdCacheKey(Integer
								.parseInt(clipId[1]));
				} catch (Exception e) {
					basicLogger.error("Exception");
					continue;
				}
			}
			String[] dest = new String[endIndex - offset];
			System.arraycopy(clipsIdArray, offset, dest, 0, (endIndex - offset));
			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(dest);
			objects = getResultArrayForNullObject(circleName, objects, dest,
					false);
			if (null == objects) {
				basicLogger.warn("No clips found for category " + categoryId);
				return null;
			}
			int cnt = 0;
			result = new Clip[objects.length];
			for (int i = 0; i < objects.length; i++) {

				if (objects[i] != null) {
					result[i] = (Clip) objects[i];
					cnt++;
				}

			}
			if (cnt == 0) {
				result = getClipsInCategory(circleName, categoryId);
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCLIPSINCATEGORY, categoryId + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(
					memcacheContentRequest);
			if (list != null) {
				result = list.toArray(new Clip[0]);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return result;
	}

	/**
	 * Gets all the clips mapped in the category with category id
	 * 
	 * @param categoryId
	 * @return
	 */
	public Clip[] getClipsInCategory(String circleName, int categoryId) {
		return getClipsInCategory(circleName, categoryId, 0, -1);
	}

	/**
	 * Gets all the clips mapped in the category with category id in the
	 * language passed
	 * 
	 * @param categoryId
	 * @return
	 */
	public Clip[] getClipsInCategory(String circleName, int categoryId,
			String language) {
		return getClipsInCategory(circleName, categoryId, 0, -1, language);
	}

	/**
	 * Gets all the categories mapped in the circle.
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleName, String circleId,
			int parentCategoryId, char prepaidYes) {
		return getCategoriesInCircle(circleName, circleId, parentCategoryId,
				prepaidYes, null, 0, -1);
	}

	/**
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleName, String circleId,
			int parentCategoryId, char prepaidYes, int offset, int rowCount) {
		return getCategoriesInCircle(circleName, circleId, parentCategoryId,
				prepaidYes, null, offset, rowCount);
	}

	/**
	 * Gets all the categories mapped in the circle.
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage
	 *            using for Category Browsing
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleName, String circleId,
			int parentCategoryId, char prepaidYes, String browsingLanguage) {
		return getCategoriesInCircle(circleName, circleId, parentCategoryId,
				prepaidYes, browsingLanguage, 0, -1);
	}

	/**
	 * Gets all the categories mapped in the circle.
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage
	 *            using for Category Browsing
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleName, String circleId,
			int parentCategoryId, char prepaidYes, String browsingLanguage,
			int offset, int rowCount) {
		return getCategoriesInCircle(circleName, circleId, parentCategoryId,
				prepaidYes, null, offset, rowCount, browsingLanguage);
	}

	/**
	 * Gets All Categories mapped in the circle in specific browsingLanguage
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language
	 *            using for category circle map language
	 * @param browsingLanguage
	 *            using for Category Browsing
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleName, String circleId,
			int parentCategoryId, char prepaidYes, String language,
			String browsingLanguage) {
		return getCategoriesInCircle(circleName, circleId, parentCategoryId,
				prepaidYes, language, 0, -1, browsingLanguage);
	}

	/**
	 * Gets all the categories mapped in the circle in specific language
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language
	 * @param offset
	 * @param rowCount
	 * @param browsingLanguage
	 *            using for Category Browsing
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleName, String circleId,
			int parentCategoryId, char prepaidYes, String language, int offset,
			int rowCount, String browsingLanguage) {
		Category[] result = null;

		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getCategoriesInCircleCacheKey(circleId,
							parentCategoryId, prepaidYes, language));
			if (null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger
						.warn("Categories are not found in cache for parentCategoryId "
								+ parentCategoryId
								+ " under circle id "
								+ circleId);
				return null;
			}

			if (offset >= categoryIdsArray.length) {
				basicLogger
						.warn("Index out of range. StartIndex value should be less than  "
								+ categoryIdsArray.length);
				return null;
			}
			if (rowCount == -1) {
				rowCount = categoryIdsArray.length;
			}

			int endIndex = offset + rowCount;
			if (endIndex > categoryIdsArray.length)
				endIndex = categoryIdsArray.length;
			// -------Get the categorykeys in language passed
			for (int i = 0; i < categoryIdsArray.length; i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey
							.getCategoryIdLanguageCacheKey(
									Integer.parseInt(categoryId[1]),
									browsingLanguage);
				} catch (Exception e) {
					basicLogger.error("Exception");
					continue;
				}
			}

			String[] dest = new String[endIndex - offset];
			System.arraycopy(categoryIdsArray, offset, dest, 0,
					(endIndex - offset));

			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(dest);
			objects = getResultArrayForNullObject(circleName, objects, dest,
					true);
			if (null == objects) {
				basicLogger
						.warn("Categories are not found in cache for parentCategoryId "
								+ parentCategoryId
								+ " under circle id "
								+ circleId);
				return null;
			}
			result = new Category[objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = (Category) objects[i];
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATINCIRCLE, parentCategoryId + "");
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			memcacheContentRequest.setBrowsingLanguage(browsingLanguage);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list != null) {
				result = list.toArray(new Category[0]);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return result;
	}

	/**
	 * Gets All Categories mapped in the circle in specific browsingLanguage
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage
	 *            using for Category Browsing
	 * @return
	 */
	public Category[] getCategoriesInCircleByLanguage(String circleName,
			String circleId, int parentCategoryId, char prepaidYes,
			String browsingLanguage) {
		return getCategoriesInCircle(circleName, circleId, parentCategoryId,
				prepaidYes, null, 0, -1, browsingLanguage);
	}

	public Object getCachedData(String circleName, String key) {
		// System.out.println("Getting from the memcache");
		basicLogger.info("Getting from the memcache");
		return RBTMultiCache.getMemCachedClient(circleName).get(key);
	}

	public static void main(String args[]) throws Exception {
		System.out.println(RBTMultiCacheManager.getInstance().getClipByPromoId(
				"AP", "15009184"));
	}

	/**
	 * Gets Active clips mapped in the category with category id
	 * 
	 * @param categoryId
	 * @param offset
	 *            The start index of the clips. Starts from 0.
	 * @param rowCount
	 *            The total values to return.
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(String circleName, int categoryId,
			int offset, int rowCount) {
		return getActiveClipsInCategory(null, circleName, categoryId, offset,
				rowCount);
	}

	/**
	 * Gets Active clips mapped in the category with category id
	 * 
	 * @param categoryId
	 * @param offset
	 *            The start index of the clips. Starts from 0.
	 * @param rowCount
	 *            The total values to return.
	 * @param subscriberId
	 *            Subscriber id.
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(String subscriberId,
			String circleName, int categoryId, int offset, int rowCount) {
		Clip[] result = null;

		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);

		Category category = getCategory(circleName, categoryId);
		List<String> categoryTypesList = RBTContentUtils
				.getBICategoryTypeList(null);
		if (category != null
				&& categoryTypesList != null
				&& categoryTypesList.contains(Integer.toString(category
						.getCategoryTpe()))) {
			StringBuffer strBuff = new StringBuffer();
			RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
			result = rbtCacheManager.getClipsFromBI(category, subscriberId,
					null, offset, rowCount, strBuff, false, null, null);
		} else if (cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getActiveClipsInCategoryCacheKey(categoryId));

			if (null == clipsIdArray || clipsIdArray.length == 0) {
				basicLogger
						.warn("Clips are not found in cache under category id "
								+ categoryId);
				return null;
			}

			if (offset >= clipsIdArray.length) {
				basicLogger
						.warn("Index out of range. StartIndex value should be less than  "
								+ clipsIdArray.length);
				return null;
			}

			if (rowCount == -1) {
				rowCount = clipsIdArray.length;
			}

			int endIndex = offset + rowCount;
			if (endIndex > clipsIdArray.length)
				endIndex = clipsIdArray.length;

			// -------Get the clipkeys in language passed
			for (int i = 0; i < clipsIdArray.length; i++) {
				String[] clipId = clipsIdArray[i].split("_");
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					clipsIdArray[i] = RBTCacheKey.getClipIdCacheKey(Integer
							.parseInt(clipId[1]));

				} catch (Exception e) {
					basicLogger.error("Exception");
					continue;
				}
			}

			String[] dest = new String[endIndex - offset];
			System.arraycopy(clipsIdArray, offset, dest, 0, (endIndex - offset));

			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(dest);
			if (null == objects) {
				return null;
			}

			result = new Clip[objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = (Clip) objects[i];
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYACTIVECLIPSINCATEGORY, categoryId
							+ "");
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(
					memcacheContentRequest);
			if (list != null) {
				result = list.toArray(new Clip[0]);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return result;
	}

	/**
	 * Gets Active clips mapped in the category with category id in a specific
	 * language
	 * 
	 * @param categoryId
	 * @param offset
	 *            The start index of the clips. Starts from 0.
	 * @param rowCount
	 *            The total values to return.
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(String circleName, int categoryId,
			int offset, int rowCount, String language) {
		Clip[] result = null;

		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getActiveClipsInCategoryCacheKey(categoryId));

			if (null == clipsIdArray || clipsIdArray.length == 0) {
				basicLogger
						.warn("Clips are not found in cache under category id "
								+ categoryId);
				return null;
			}

			if (offset >= clipsIdArray.length) {
				basicLogger
						.warn("Index out of range. StartIndex value should be less than  "
								+ clipsIdArray.length);
				return null;
			}

			if (rowCount == -1) {
				rowCount = clipsIdArray.length;
			}

			int endIndex = offset + rowCount;
			if (endIndex > clipsIdArray.length)
				endIndex = clipsIdArray.length;

			// -------Get the clipkeys in language passed
			for (int i = 0; i < clipsIdArray.length; i++) {
				String[] clipId = clipsIdArray[i].split("_");
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					clipsIdArray[i] = RBTCacheKey.getClipIdLanguageCacheKey(
							Integer.parseInt(clipId[1]), language);
				} catch (Exception e) {
					basicLogger.error("Exception");
					continue;
				}
			}

			String[] dest = new String[endIndex - offset];
			System.arraycopy(clipsIdArray, offset, dest, 0, (endIndex - offset));

			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(dest);
			objects = getResultArrayForNullObject(circleName, objects, dest,
					false);
			if (null == objects) {
				return null;
			}

			result = new Clip[objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = (Clip) objects[i];
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYACTIVECLIPSINCATEGORY, categoryId
							+ "");
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(
					memcacheContentRequest);
			if (list != null) {
				result = list.toArray(new Clip[0]);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return result;
	}

	private Object[] getResultArrayForNullObject(String circleName,
			Object[] objects, String[] destKey, boolean isCategory) {
		String language = RBTContentJarParameters.getInstance().getParameter(
				"default_language");
		boolean isNullObject = false;
		for (int index = 0; index < objects.length; index++) {
			if (objects[index] == null) {
				isNullObject = true;
				String[] arrays = destKey[index].split("_");
				if (isCategory) {
					destKey[index] = RBTCacheKey.getCategoryIdLanguageCacheKey(
							Integer.parseInt(arrays[1]), language);
				} else {
					destKey[index] = RBTCacheKey.getClipIdLanguageCacheKey(
							Integer.parseInt(arrays[1]), language);
				}
			}
		}
		if (isNullObject) {
			objects = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(destKey);
		}

		return objects;
	}

	/**
	 * Gets Active clips mapped in the category with category id
	 * 
	 * @param categoryId
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(String circleName, int categoryId) {
		return getActiveClipsInCategory(circleName, categoryId, null);
	}

	/**
	 * Gets Active clips mapped in the category with category id
	 * 
	 * @param categoryId
	 * @return
	 */
	public Clip[] getActiveClipsInCategory(String circleName, int categoryId,
			String language) {
		return getActiveClipsInCategory(circleName, categoryId, 0, -1, language);
	}

	/**
	 * Get Active clip count in category
	 * 
	 * @param categoryid
	 * @return
	 */

	public int getActiveClipsCountInCategory(String circleName, int categoryId) {
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getActiveClipsInCategoryCacheKey(categoryId));
			if (null == clipsIdArray || clipsIdArray.length == 0) {
				basicLogger
						.warn("Clips are not found in cache under category id "
								+ categoryId);
				return 0;
			}
			return clipsIdArray.length;
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYACTIVECLIPSINCATEGORY, categoryId
							+ "");
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(
					memcacheContentRequest);
			if (list != null) {
				return list.size();
			} else {
				basicLogger
						.warn("Clips are not found in webservice under category id "
								+ categoryId);
				return 0;
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return -1;
	}

	/**
	 * Get All the clip count in category
	 * 
	 * @param categoryid
	 * @return
	 */
	public int getClipsCountInCategory(String circleName, int categoryId) {
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getClipsInCategoryCacheKey(categoryId));
			if (null == clipsIdArray || clipsIdArray.length == 0) {
				basicLogger
						.warn("Clips are not found in cache under category id "
								+ categoryId);
				return 0;
			}
			return clipsIdArray.length;
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCLIPSINCATEGORY, categoryId + "");
			memcacheContentRequest.setOffSet(circleName);
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(
					memcacheContentRequest);
			if (list != null) {
				return list.size();
			} else {
				basicLogger
						.warn("Clips are not found in webservice under category id "
								+ categoryId);
				return 0;
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return -1;
	}

	public int getCategoriesCountInCircle(String circleName, String circleId,
			int parentCategoryId, char prepaidYes) {
		return getCategoriesCountInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null);
	}

	/**
	 * Get All the category count in circle
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language
	 *            for CategoryCircleMap
	 * @return
	 */
	public int getCategoriesCountInCircle(String circleName, String circleId,
			int parentCategoryId, char prepaidYes, String language) {
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getCategoriesInCircleCacheKey(circleId,
							parentCategoryId, prepaidYes, language));
			if (null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger
						.warn("Categories are not found in cache under circle id "
								+ circleId);
				return 0;
			}
			return categoryIdsArray.length;
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATINCIRCLE, parentCategoryId + "");
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list == null) {
				basicLogger
						.warn("Categories are not found in webservice under circle id "
								+ circleId);
				return 0;
			}
			return list.size();
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return -1;
	}

	/**
	 * Gets Active categories mapped in the circle.
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleName,
			String circleId, int parentCategoryId, char prepaidYes) {
		return getActiveCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null, 0, -1);
	}

	/**
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleName,
			String circleId, int parentCategoryId, char prepaidYes, int offset,
			int rowCount) {
		return getActiveCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null, offset, rowCount);
	}

	/**
	 * Gets Active categories mapped in the circle.
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage
	 *            only for category browsing
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleName,
			String circleId, int parentCategoryId, char prepaidYes,
			String browsingLanguage) {
		return getActiveCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, browsingLanguage, 0, -1);
	}

	/**
	 * Gets Active categories mapped in the circle.
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param browsingLanguage
	 *            only for category browsing
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleName,
			String circleId, int parentCategoryId, char prepaidYes,
			String browsingLanguage, int offset, int rowCount) {
		return getActiveCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null, offset, rowCount,
				browsingLanguage);
	}

	/**
	 * Gets Active categories mapped in the circle in specific categoryLanguage
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language
	 *            using for category circle map language
	 * @param categoryLangauge
	 *            using for Category Browsing
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleName,
			String circleId, int parentCategoryId, char prepaidYes,
			String language, String browsingLanguage) {
		return getActiveCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, language, 0, -1, browsingLanguage);
	}

	/**
	 * Gets Active categories mapped in the circle in specific categoryLanguage
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param language
	 *            using for category circle map language
	 * @param offset
	 * @param rowCount
	 * @param categoryLangauge
	 *            using for Category Browsing
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleName,
			String circleId, int parentCategoryId, char prepaidYes,
			String language, int offset, int rowCount, String browsingLanguage) {
		Category[] result = null;

		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getActiveCategoriesInCircleCacheKey(circleId,
							parentCategoryId, prepaidYes, language));
			if (null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger
						.warn("Categories are not found in cache under circle id "
								+ circleId);
				return null;
			}

			if (offset >= categoryIdsArray.length) {
				basicLogger
						.warn("Index out of range. StartIndex value should be less than  "
								+ categoryIdsArray.length);
				return null;
			}
			if (rowCount == -1) {
				rowCount = categoryIdsArray.length;
			}

			int endIndex = offset + rowCount;
			if (endIndex > categoryIdsArray.length)
				endIndex = categoryIdsArray.length;
			// -------Get the categorykeys in language passed
			for (int i = 0; i < categoryIdsArray.length; i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey
							.getCategoryIdLanguageCacheKey(
									Integer.parseInt(categoryId[1]),
									browsingLanguage);
				} catch (Exception e) {
					basicLogger.error("Exception");
					continue;
				}
			}
			String[] dest = new String[endIndex - offset];
			System.arraycopy(categoryIdsArray, offset, dest, 0,
					(endIndex - offset));

			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(dest);
			objects = getResultArrayForNullObject(circleName, objects, dest,
					true);
			if (null == objects) {
				return null;
			}
			result = new Category[objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = (Category) objects[i];
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYACTCATINCIRCLE, parentCategoryId + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			memcacheContentRequest.setBrowsingLanguage(browsingLanguage);
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list != null) {
				result = list.toArray(new Category[0]);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		return result;
	}

	public int getActiveCategoriesCountInCircle(String circleName,
			String circleId, int parentCategoryId, char prepaidYes) {
		return getActiveCategoriesCountInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null);
	}

	/**
	 * Get Active category count in circle
	 * 
	 * @param circleId
	 * @param parentCategoryId
	 * @param prepaidYes
	 * @param langauge
	 *            for Category circleMap
	 * @return
	 */
	public int getActiveCategoriesCountInCircle(String circleName,
			String circleId, int parentCategoryId, char prepaidYes,
			String language) {
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getActiveCategoriesInCircleCacheKey(circleId,
							parentCategoryId, prepaidYes, language));
			if (null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger
						.warn("Categories are not found in cache under circle id "
								+ circleId);
				return 0;
			}
			return categoryIdsArray.length;
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYACTCATINCIRCLE, parentCategoryId + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list != null) {
				return list.size();
			} else {
				return 0;
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return -1;
	}

	/**
	 * Get the Category mapped with Category SMS Alias
	 * 
	 * @param categorySMSAlias
	 * @return
	 */
	public Category getCategoryBySMSAlias(String circleName,
			String categorySMSAlias) {
		return getCategoryBySMSAlias(categorySMSAlias, null);
	}

	/**
	 * Get the Category mapped with Category SMS Alias in categorySpecific
	 * language
	 * 
	 * @param categorySMSAlias
	 * @return
	 */
	public Category getCategoryBySMSAlias(String circleName,
			String categorySMSAlias, String language) {
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String categoryId = (String) getCachedData(circleName,
					RBTCacheKey.getCategorySmsAliasCacheKey(categorySMSAlias));
			if (null != categoryId && categoryId.length() > 0) {
				return (Category) getCategory(circleName,
						Integer.parseInt(categoryId), language);
			}
			basicLogger.warn("Category not found in cache with SmsAlias "
					+ categorySMSAlias);
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATALIAS, categorySMSAlias);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list != null && list.size() > 0) {
				return list.get(0);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return null;
	}

	/**
	 * Get the Category mapped with the category name
	 * 
	 * @param categoryName
	 * @return
	 */
	public Category getCategoryByName(String circleName, String categoryName) {
		return getCategoryByName(categoryName, null);
	}

	/**
	 * Get the Category mapped with the category name by specific language
	 * 
	 * @param categoryName
	 * @return
	 */
	public Category getCategoryByName(String circleName, String categoryName,
			String language) {
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String categoryId = (String) getCachedData(circleName,
					RBTCacheKey.getCategoryNameCacheKey(categoryName));
			if (null != categoryId && categoryId.length() > 0) {
				return (Category) getCategory(circleName,
						Integer.parseInt(categoryId), language);
			}
			basicLogger.warn("Category not found in cache with categoryName "
					+ categoryName);
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATNAME, categoryName);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list != null && list.size() > 0) {
				return list.get(0);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return null;
	}

	/**
	 * Get the clips mapped with Clip Album (valid subscriber id)
	 * 
	 * @param subscriberID
	 * @return
	 */
	public Clip[] getClipsByAlbum(String circleName, String subscriberID) {
		return getClipsByAlbum(subscriberID, null);
	}

	/**
	 * Get the clips mapped with Clip Album (valid subscriber id) in specific
	 * language
	 * 
	 * @param subscriberID
	 * @return
	 */
	public Clip[] getClipsByAlbum(String circleName, String subscriberID,
			String language) {
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String clipIdArray = (String) getCachedData(circleName,
					RBTCacheKey.getAlbumCacheKey(subscriberID));
			if (null == clipIdArray || clipIdArray.length() == 0) {
				basicLogger.warn("Clips are not found in cache under Album"
						+ subscriberID);
				return null;
			}

			String[] clipIds = clipIdArray.split("\\,");

			Set<String> clipIdSet = new TreeSet<String>();
			for (int i = 0; i < clipIds.length; i++) {
				clipIdSet.add(clipIds[i]);
			}

			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(clipIdSet.toArray(new String[0]));
			if (null == objects) {
				return null;
			}
			Clip[] result = new Clip[objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = (Clip) objects[i];
			}
			return result;

		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCLIPALBUM, subscriberID);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(
					memcacheContentRequest);
			if (list != null) {
				return list.toArray(new Clip[0]);
			}

		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return null;
	}

	/**
	 * Get the Clip from PromoMaster
	 * 
	 * @param promoType
	 * @param promoCode
	 * @return
	 */
	public Clip getClipFromPromoMaster(String circleName, String promoType,
			String promoCode) {
		return getClipFromPromoMaster(circleName, promoType, promoCode, null);
	}

	/**
	 * Get the Clip from PromoMaster of a specific language
	 * 
	 * @param promoType
	 * @param promoCode
	 * @return
	 */
	public Clip getClipFromPromoMaster(String circleName, String promoType,
			String promoCode, String language) {
		Clip clip = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String clipId = (String) getCachedData(circleName,
					RBTCacheKey.getPromoMasterCacheKey(promoCode, promoType));
			if (null != clipId && clipId.length() > 0) {
				// return (Clip)getClipByPromoId(clipId);
				clip = (Clip) getCachedData(circleName,
						RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if (clip == null)
					clip = (Clip) getCachedData(circleName,
							RBTCacheKey.getClipIdCacheKey(clipId));
				return clip;
			}
			basicLogger
					.warn("Clip not found in cache with promo master promocode[ "
							+ promoCode + "] promo type[" + promoType + "]");
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return null;
	}

	/**
	 * Get the Clip from PromoMaster
	 * 
	 * @param promoCode
	 * @return
	 */
	public Clip getClipFromPromoMaster(String circleName, String promoCode) {
		return getClipFromPromoMasterByLanguage(circleName, promoCode, null);
	}

	/**
	 * Get the Clip from PromoMaster
	 * 
	 * @param promoCode
	 * @return
	 */
	public Clip getClipFromPromoMasterByLanguage(String circleName,
			String promoCode, String language) {
		Clip clip = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String clipId = (String) getCachedData(circleName,
					RBTCacheKey.getPromoCodeCacheKey(promoCode));
			if (null != clipId && clipId.length() > 0) {
				// return (Clip)getClipByPromoId(clipId);
				clip = (Clip) getCachedData(circleName,
						RBTCacheKey.getClipIdLanguageCacheKey(clipId, language));
				if (clip == null)
					clip = (Clip) getCachedData(circleName,
							RBTCacheKey.getClipIdCacheKey(clipId));
				return clip;
			}
			basicLogger
					.warn("Clip not found in cache with promo master promocode[ "
							+ promoCode + "]");
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return null;
	}

	/**
	 * Get the Array of Category by category type
	 * 
	 * @param categoryTYpe
	 * @return Category[]
	 */
	public Category[] getCategoryByType(String circleName, String categoryType) {
		return getCategoryByType(circleName, categoryType, null);
	}

	/**
	 * Get the Array of Category by category type in specific language
	 * 
	 * @param categoryTYpe
	 * @return Category[]
	 */
	public Category[] getCategoryByType(String circleName, String categoryType,
			String language) {
		Category[] result = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getCategoryTypeCacheKey(categoryType));
			if (null == categoryIdsArray || categoryIdsArray.length == 0) {
				basicLogger
						.warn("Categories are not found in cache for categoryType "
								+ categoryType);
				return null;
			}
			// -------Get the categorykeys in language passed
			for (int i = 0; i < categoryIdsArray.length; i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey
							.getCategoryIdLanguageCacheKey(
									Integer.parseInt(categoryId[1]), language);
				} catch (Exception e) {
					basicLogger.error("Exception");
					continue;
				}
			}
			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(categoryIdsArray);
			if (null == objects) {
				return null;
			}
			result = new Category[objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = (Category) objects[i];
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATTYPE, categoryType);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list != null) {
				result = list.toArray(new Category[0]);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		return result;
	}

	/**
	 * Get the Array of Category by circleId, prepaidYes, categoryType
	 * 
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryType
	 * @return
	 */
	public Category[] getCategoryByType(String circleName, String circleId,
			char prepaidYes, String categoryType) {
		return getCategoryByType(circleName, circleId, prepaidYes,
				categoryType, null);
	}

	/**
	 * Get the Array of Category by circleId, prepaidYes, categoryType in
	 * specific language
	 * 
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryType
	 * @param language
	 * @return
	 */
	public Category[] getCategoryByType(String circleName, String circleId,
			char prepaidYes, String categoryType, String language) {
		Category[] result = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getTypePrepadiCircleIdCacheKey(circleId,
							prepaidYes, categoryType));
			if (null == categoryIdsArray || categoryIdsArray.length == 0) {
				if (basicLogger.isInfoEnabled()) {
					basicLogger
							.info("Categories are not found in db for circle Id "
									+ circleId
									+ " , prepaidYes "
									+ prepaidYes
									+ " , categoryType " + categoryType);
				}
				return null;
			}
			// -------Get the categorykeys in language passed
			for (int i = 0; i < categoryIdsArray.length; i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey
							.getCategoryIdLanguageCacheKey(
									Integer.parseInt(categoryId[1]), language);
				} catch (Exception e) {
					basicLogger.error("Exception");
					continue;
				}
			}
			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(categoryIdsArray);
			if (null == objects) {
				return null;
			}
			result = new Category[objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = (Category) objects[i];
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATTYPE, categoryType);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list != null) {
				result = list.toArray(new Category[0]);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		return result;
	}

	private Category[] categoryTypeNotRequired(Category[] category,
			List<Integer> categoryTypesNotRequired, int offset, int rowCount) {
		List<Category> categoryList = new ArrayList<Category>();
		for (int i = 0; i < category.length; i++) {
			if (!categoryTypesNotRequired
					.contains(category[i].getCategoryTpe())) {
				categoryList.add(category[i]);
			}
		}
		category = categoryList.toArray(new Category[0]);
		if (offset >= category.length) {
			basicLogger
					.warn("Index out of range. StartIndex value should be less than  "
							+ category.length);
			return null;
		}
		if (rowCount == -1) {
			rowCount = category.length;
		}

		int endIndex = offset + rowCount;
		if (endIndex > category.length)
			endIndex = category.length;

		Category[] dest = new Category[endIndex - offset];
		System.arraycopy(category, offset, dest, 0, (endIndex - offset));

		return dest;
	}

	/**
	 * Get the Array of active Categories from circle with the circleId,
	 * prepaidYes, parentCategoryId, list of category type which in not required
	 * 
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleName,
			String circleId, char prepaidYes, int parentCategoryId,
			List<Integer> categoryTypesNotRequired) {
		Category[] category = getActiveCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null, 0, -1);
		return categoryTypeNotRequired(category, categoryTypesNotRequired, 0,
				-1);
	}

	/**
	 * Get the Array of all categories from circle with circleId, prepaidYes,
	 * parentCategoryId, list of category type which is not required
	 * 
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleName, String circleId,
			char prepaidYes, int parentCategoryId,
			List<Integer> categoryTypesNotRequired) {
		Category[] category = getCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null, 0, -1);
		if (category == null) {
			if (basicLogger.isInfoEnabled()) {
				basicLogger
						.info("Categories are not found in db for parentCategoryId "
								+ parentCategoryId
								+ " under circle id "
								+ circleId);
			}
			return null;
		}
		if (categoryTypesNotRequired == null) {
			if (basicLogger.isInfoEnabled()) {
				basicLogger.info("category type list is null");
			}
			return null;
		}
		return categoryTypeNotRequired(category, categoryTypesNotRequired, 0,
				-1);

	}

	/**
	 * Get the Array of all categories from circle with circleId, prepaidYes,
	 * parentCategoryId, list of category type which is not required by language
	 * 
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleName, String circleId,
			char prepaidYes, int parentCategoryId,
			List<Integer> categoryTypesNotRequired, String language) {
		Category[] category = getCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null, 0, -1);
		if (category == null) {
			if (basicLogger.isInfoEnabled()) {
				basicLogger
						.info("Categories are not found in db for parentCategoryId "
								+ parentCategoryId
								+ " under circle id "
								+ circleId);
			}
			return null;
		}
		if (categoryTypesNotRequired == null) {
			if (basicLogger.isInfoEnabled()) {
				basicLogger.info("category type list is null");
			}
			return null;
		}
		return categoryTypeNotRequired(category, categoryTypesNotRequired, 0,
				-1);

	}

	/**
	 * Get the Array of active categories from circle with circleId, prepaidYes,
	 * parentCategoryId, list of category type which is not required
	 * 
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getActiveCategoriesInCircle(String circleName,
			String circleId, char prepaidYes, int parentCategoryId,
			List<Integer> categoryTypesNotRequired, int offset, int rowCount) {
		Category[] category = getActiveCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null, 0, -1);
		return categoryTypeNotRequired(category, categoryTypesNotRequired,
				offset, rowCount);
	}

	/**
	 * Get the Array of all categories from circle with circleId, prepaidYes,
	 * parentCategoryId, list of category type which is not required
	 * 
	 * @param circleId
	 * @param prepaidYes
	 * @param parentCategoryId
	 * @param categoryTypesNotRequired
	 * @param offset
	 * @param rowCount
	 * @return
	 */
	public Category[] getCategoriesInCircle(String circleName, String circleId,
			char prepaidYes, int parentCategoryId,
			List<Integer> categoryTypesNotRequired, int offset, int rowCount) {
		Category[] category = getCategoriesInCircle(circleName, circleId,
				parentCategoryId, prepaidYes, null, 0, -1);
		if (category == null) {
			if (basicLogger.isInfoEnabled()) {
				basicLogger
						.info("Categories are not found in db for parentCategoryId "
								+ parentCategoryId
								+ " under circle id "
								+ circleId);
			}
			return null;
		}
		if (categoryTypesNotRequired == null) {
			if (basicLogger.isInfoEnabled()) {
				basicLogger.info("category type list is null");
			}
			return null;
		}
		return categoryTypeNotRequired(category, categoryTypesNotRequired,
				offset, rowCount);

	}

	/**
	 * Get the array of category with promoId
	 * 
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryPromoId
	 * @return
	 */
	public Category[] getCategoryByPromoId(String circleName, String circleId,
			char prepaidYes, String categoryPromoId) {
		return getCategoryByPromoId(circleName, circleId, prepaidYes,
				categoryPromoId, null);
	}

	/**
	 * Get the array of category with promoId in specific language
	 * 
	 * @param circleId
	 * @param prepaidYes
	 * @param categoryPromoId
	 * @return
	 */
	public Category[] getCategoryByPromoId(String circleName, String circleId,
			char prepaidYes, String categoryPromoId, String language) {
		Category[] result = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] categoryIdsArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getPromoIdPrepadiCircleIdCacheKey(circleId,
							prepaidYes, categoryPromoId));
			if (null == categoryIdsArray || categoryIdsArray.length == 0) {
				if (basicLogger.isInfoEnabled()) {
					basicLogger
							.info("Categories are not found in db for circle Id "
									+ circleId
									+ " , prepaidYes "
									+ prepaidYes
									+ " , categoryPromoId " + categoryPromoId);
				}
				return null;
			}
			// -------Get the categorykeys in language passed
			for (int i = 0; i < categoryIdsArray.length; i++) {
				String[] categoryId = categoryIdsArray[i].split("_");
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					categoryIdsArray[i] = RBTCacheKey
							.getCategoryIdLanguageCacheKey(
									Integer.parseInt(categoryId[1]), language);
				} catch (Exception e) {
					basicLogger.error("Exception");
					continue;
				}
			}
			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(categoryIdsArray);
			if (null == objects) {
				return null;
			}
			result = new Category[objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = (Category) objects[i];
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATARRPROMOID, categoryPromoId);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setCircleID(circleId);
			memcacheContentRequest.setPrepaidYes(prepaidYes + "");
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list != null) {
				result = list.toArray(new Category[0]);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		return result;
	}

	/**
	 * To get the clips from cache
	 * 
	 * @param clipIds
	 * @return Clip[]
	 */
	public Clip[] getClips(String circleName, String[] clipIds) {
		return getClips(circleName, clipIds, null);
	}

	/**
	 * To get the clips from cache in language passed
	 * 
	 * @param clipIds
	 * @param language
	 *            for Browsing clip language
	 * @return Clip[]
	 */
	public Clip[] getClips(String circleName, String[] clipIds, String language) {
		if (clipIds == null || clipIds.length == 0) {
			basicLogger.warn("Array is null or doesn't contain clipids");
			return null;
		}
		Clip[] clipObjects = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			for (int i = 0; i < clipIds.length; i++) {
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					clipIds[i] = RBTCacheKey.getClipIdLanguageCacheKey(
							Integer.parseInt(clipIds[i]), language);
				} catch (Exception e) {
					continue;
				}
			}
			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(clipIds);
			objects = getResultArrayForNullObject(circleName, objects, clipIds,
					false);
			clipObjects = new Clip[objects.length];
			for (int i = 0; i < objects.length; i++) {
				clipObjects[i] = (Clip) objects[i];
			}
			return clipObjects;
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			String contentIds = "";
			for (String clipId : clipIds) {
				contentIds += clipId + ",";
			}
			contentIds = contentIds.substring(0, contentIds.length() - 1);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCLIPS, contentIds);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(
					memcacheContentRequest);
			if (list != null) {
				return list.toArray(new Clip[0]);
			} else {
				return null;
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		return clipObjects;
	}

	/**
	 * To get the categories from cache
	 * 
	 * @param categoryIds
	 * @return Category[]
	 */
	public Category[] getCategories(String circleName, String[] categoryIds) {
		return getCategories(circleName, categoryIds, null);
	}

	/**
	 * To get the categories from cache in specific language
	 * 
	 * @param categoryIds
	 * @param language
	 *            for Browsing category language
	 * @return Category[]
	 */
	public Category[] getCategories(String circleName, String[] categoryIds,
			String language) {
		if (categoryIds == null || categoryIds.length == 0) {
			basicLogger.warn("Array is null or doesn't contain categoryIds");
			return null;
		}
		Category[] categoryObjects = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			for (int i = 0; i < categoryIds.length; i++) {
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					categoryIds[i] = RBTCacheKey.getCategoryIdLanguageCacheKey(
							Integer.parseInt(categoryIds[i]), language);
				} catch (Exception e) {
					continue;
				}
			}
			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(categoryIds);
			objects = getResultArrayForNullObject(circleName, objects,
					categoryIds, true);
			categoryObjects = new Category[objects.length];
			for (int i = 0; i < objects.length; i++) {
				categoryObjects[i] = (Category) objects[i];
			}
			return categoryObjects;
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			String contentIds = "";
			for (String clipId : categoryIds) {
				contentIds += clipId + ",";
			}
			contentIds = contentIds.substring(0, contentIds.length() - 1);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYCATIDS, contentIds);
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Category> list = RBTClient.getInstance()
					.getCategoryFromWebservice(memcacheContentRequest);
			if (list != null) {
				return list.toArray(new Category[0]);
			} else {
				return null;
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}
		return categoryObjects;
	}

	public boolean isClipMappedToCatgeory(String circleName, String clipId,
			String categoryId) {
		boolean isMapped = false;
		basicLogger
				.info("Inside isClipMappedToCatgeory(String clipId, String categoryId)");
		int categoryID;
		try {
			categoryID = Integer.parseInt(categoryId);
		} catch (NumberFormatException e) {
			basicLogger.error(e);
			basicLogger
					.info("Inside getClipsInCategory : Error while conver the categoryID from string to int [ categorYId - "
							+ categoryId + "]");
			return false;
		}

		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		if (cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getClipsInCategoryCacheKey(categoryID));
			if (null == clipsIdArray || clipsIdArray.length == 0) {
				if (basicLogger.isInfoEnabled()) {
					basicLogger
							.warn("Clips are not found in cache under category id "
									+ categoryId);
				}
				return false;
			}

			for (int i = 0; i < clipsIdArray.length; i++) {
				String[] tempClipID = clipsIdArray[i].split("_");
				try {
					if (tempClipID[1].equals(clipId)) {
						isMapped = true;
						break;
					}
				} catch (ArrayIndexOutOfBoundsException ae) {
					continue;
				}
			}

			if (isMapped) {
				basicLogger.info("Inside getClipsInCategory : clip ID ("
						+ clipId + ") is mapped to this category ("
						+ categoryId + ")");
			} else {
				basicLogger.info("Inside getClipsInCategory : clip ID ("
						+ clipId + ") is not mapped to this category ("
						+ categoryId + ")");
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		return isMapped;
	}

	public Clip[] getActiveClipsInCategory(String circleName, int categoryId,
			int offset, int rowCount, String language, String subscriberId,
			StringBuffer activeClipCount) {
		basicLogger.warn("Getting clips for circleName: " + circleName
				+ ", categoryId: " + categoryId + ", offset: " + offset
				+ ", rowCount: " + rowCount + ", language: " + language
				+ ", subscriberId: " + subscriberId + ", activeClipCount: "
				+ activeClipCount);
		Clip[] result = null;
		boolean cacheAlive = RBTMultiCache.isCacheAlive(circleName);
		boolean cacheInitialized = RBTMultiCache.isCacheInitialized(circleName);
		Category category = getCategory(circleName, categoryId);
		List<String> categoryTypesList = RBTContentUtils
				.getBICategoryTypeList(null);
		if (category != null
				&& categoryTypesList != null
				&& categoryTypesList.contains(Integer.toString(category
						.getCategoryTpe()))) {
			StringBuffer strBuff = new StringBuffer();
			RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
			result = rbtCacheManager.getClipsFromBI(category, subscriberId,
					null, offset, rowCount, strBuff, false, null, null);
		} else if (cacheAlive && cacheInitialized) {
			String[] clipsIdArray = (String[]) getCachedData(circleName,
					RBTCacheKey.getActiveClipsInCategoryCacheKey(categoryId));

			if (null == clipsIdArray || clipsIdArray.length == 0) {
				basicLogger
						.warn("Clips are not found in cache under category id "
								+ categoryId);
				return null;
			}

			if (offset >= clipsIdArray.length) {
				basicLogger
						.warn("Index out of range. StartIndex value should be less than  "
								+ clipsIdArray.length);
				return null;
			}

			if (rowCount == -1) {
				rowCount = clipsIdArray.length;
			}

			int endIndex = offset + rowCount;
			if (endIndex > clipsIdArray.length)
				endIndex = clipsIdArray.length;

			// -------Get the clipkeys in language passed
			for (int i = 0; i < clipsIdArray.length; i++) {
				String[] clipId = clipsIdArray[i].split("_");
				try {
					// doing safety check if the client is passing some
					// alphabets in the cateory ids
					clipsIdArray[i] = RBTCacheKey.getClipIdLanguageCacheKey(
							Integer.parseInt(clipId[1]), language);
				} catch (Exception e) {
					basicLogger.error("Exception");
					continue;
				}
			}

			String[] dest = new String[endIndex - offset];
			System.arraycopy(clipsIdArray, offset, dest, 0, (endIndex - offset));

			Object objects[] = RBTMultiCache.getMemCachedClient(circleName)
					.getMultiArray(dest);
			objects = getResultArrayForNullObject(circleName, objects, dest,
					false);
			if (null == objects) {
				return null;
			}

			result = new Clip[objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = (Clip) objects[i];
			}
		} else if (fallbackLogic != null
				&& fallbackLogic.equalsIgnoreCase(WEBSERVICE_FALLBACK)) {
			basicLogger
					.warn("Getting data from Webservice. Reason: cacheAlive? "
							+ cacheAlive + " cacheInitialized? "
							+ cacheInitialized);
			MemcacheContentRequest memcacheContentRequest = new MemcacheContentRequest(
					WebServiceConstants.BYACTIVECLIPSINCATEGORY, categoryId
							+ "");
			memcacheContentRequest.setOffSet(offset + "");
			memcacheContentRequest.setRowCount(rowCount + "");
			memcacheContentRequest.setLanguage(language);
			memcacheContentRequest.setOperatroID(circleName);
			List<Clip> list = RBTClient.getInstance().getClipFromWebservice(
					memcacheContentRequest);
			if (list != null) {
				result = list.toArray(new Clip[0]);
			}
		} else {
			basicLogger.warn("Cache manager not found/not alive for circle "
					+ circleName);
		}

		if (activeClipCount != null) {
			activeClipCount.append(getActiveClipsCountInCategory(circleName,
					categoryId));
		}

		basicLogger.warn("Returning result: " + result + ", for circle "
				+ circleName + ", categoryId: " + categoryId + ", offset: "
				+ offset + ", rowCount: " + rowCount + ", language: "
				+ language + ", subscriberId: " + subscriberId
				+ ", activeClipCount: " + activeClipCount);
		return result;
	}

	/*
	 * Commented as there will not be any database public ClipRating
	 * getClipRating(String circleName, int clipId) { long now =
	 * System.currentTimeMillis();
	 * 
	 * ClipRating clipRating = null; try { clipRating =
	 * ClipRatingDAO.getClipRating(clipId); } catch(DataAccessException dae) {
	 * basicLogger.error(dae); }
	 * 
	 * basicLogger.info("ClipRating " + clipRating + " Time taken: " +
	 * (System.currentTimeMillis() - now) + " ClipRating: " + clipRating);
	 * return clipRating; }
	 * 
	 * public List<ClipRating> getClipsRating(String circleName, List<Integer>
	 * clipIds) { long now = System.currentTimeMillis();
	 * 
	 * List<ClipRating> clipsRating = null; try { clipsRating =
	 * ClipRatingDAO.getClipsRating(clipIds); } catch(DataAccessException dae) {
	 * basicLogger.error(dae); }
	 * 
	 * basicLogger.info("ClipsRating " + clipsRating + " Time taken: " +
	 * (System.currentTimeMillis() - now) + " ClipsRating: " + clipsRating);
	 * return clipsRating; }
	 * 
	 * public ClipRating rateClip(String circleName, int clipId, int rating) {
	 * ClipRating clipRating = null;
	 * 
	 * ClipRatingTransaction clipRatingTransaction = new
	 * ClipRatingTransaction();
	 * 
	 * clipRatingTransaction.setClipId(clipId);
	 * clipRatingTransaction.setNoOfVotes(1);
	 * clipRatingTransaction.setRatingDate(new Date());
	 * clipRatingTransaction.setSumOfRatings(rating);
	 * 
	 * try {
	 * ClipRatingTransactionDAO.saveClipRatingTransaction(clipRatingTransaction
	 * ); clipRating = ClipRatingDAO.getClipRating(clipId); } catch
	 * (ConstraintViolationException cve) { try {
	 * ClipRatingTransactionDAO.updateClipRatingTransaction(clipId, rating);
	 * clipRating = ClipRatingDAO.getClipRating(clipId); } catch
	 * (DataAccessException dae) { basicLogger.error(dae); } } catch
	 * (DataAccessException dae) { basicLogger.error(dae); } return clipRating;
	 * }
	 */
}