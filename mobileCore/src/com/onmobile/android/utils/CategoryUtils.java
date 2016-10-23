package com.onmobile.android.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.CategoryBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.apps.ringbacktones.lucene.LuceneCategory;
import com.onmobile.apps.ringbacktones.lucene.msearch.SearchResponse;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category.CategoryInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;

public class CategoryUtils {

	private static Logger logger = Logger.getLogger(CategoryUtils.class);
	private static RBTCacheManager cacheManager = RBTCacheManager.getInstance();

	private static Map<String, String> categoryTypeMap = null;

	static {
		categoryTypeMap = PropertyConfigurator.getCategoryTypeMapping();
		logger.info("categoryTypeMap: " + categoryTypeMap);
	}
	/**
	 * Utility method for returning categoryBean from categoryId and language
	 * @param catId
	 * @param language 
	 * @return
	 */
	public static CategoryBean getCategoryBean(int catId, String browsingLanguage, String appName) {
		logger.info("catId:" + catId + ", browsingLanguage: " + browsingLanguage);
		Category category = cacheManager.getCategory(catId, browsingLanguage, appName);
		logger.debug("category: " + category);
		CategoryBean categoryBean = getCategoryBean(category, browsingLanguage, appName);
		return categoryBean;
	}

	/**
	 * Utility method for returning categoryBean from category object and language
	 * @param category
	 * @param appName 
	 * @param language
	 * @return
	 */
	public static CategoryBean getCategoryBean(Category category, String browsingLanguage, String appName) {
		CategoryBean categoryBean = null;
		if (category == null) {
			logger.info("Category object null");
			return null;
		}
		int clipCount = cacheManager.getClipsCountInCategory(category.getCategoryId());
		logger.debug("clipCount: " + clipCount);
		categoryBean = new CategoryBean(category, clipCount);
		String categoryImagePath = PropertyConfigurator.getCategoryImagePath(String.valueOf(category.getCategoryId()));
		if (categoryImagePath == null) {
			categoryImagePath = category.getCategoryInfo(CategoryInfoKeys.IMG_URL);
			if (categoryImagePath == null) {
				categoryImagePath = category.getCategoryInfo(CategoryInfoKeys.IMG);
			}
		}
		if (categoryImagePath == null) {
			logger.debug("categoryImagePath is null. Fetching from first clip.");
			Clip[] clips = cacheManager.getActiveClipsInCategory(category.getCategoryId(), browsingLanguage);
			if (clips != null && clips.length > 0) {
				Clip clip = clips[0];
				if (clip != null) {
					categoryImagePath = clip.getClipInfo(Clip.ClipInfoKeys.IMG_URL);
					logger.debug("categoryImagePath: " + categoryImagePath);
				}
			}
		}
		logger.debug("categoryImagePath: " + categoryImagePath);
		categoryBean.setCategoryPath(categoryImagePath);
		categoryBean.setCategoryType(category.getCategoryTpe());
		String categoryDescription = category.getCategoryInfo(CategoryInfoKeys.CAT_DESC);
		if (categoryDescription != null) {
			categoryBean.setDescription(categoryDescription);
		}
		categoryBean.setCategoryLanguage(category.getCategoryLanguage());
		logger.info("Returning categoryBean: "+categoryBean);
		return categoryBean;
	}

	public static List<CategoryBean> getCategoryBeanListFromSearchResponse(
			SearchResponse searchResponse) {
		List<CategoryBean> categoryList = new ArrayList<CategoryBean>();
		if (searchResponse != null) {
			List<LuceneCategory> luceneCatList = searchResponse.getResults();
			if (luceneCatList != null && luceneCatList.size() > 0) {
				for (LuceneCategory luceneCategory : luceneCatList) {
					CategoryBean catBean = getCategoryBean(luceneCategory, null, null);
					categoryList.add(catBean);
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("categoryList: " + categoryList);
		}
		return categoryList;
	}

	public static String getCategoryTypeStringValue(int categoryType) {
		String categoryTypeString = null;
		if (categoryTypeMap != null) {
			categoryTypeString = categoryTypeMap.get(String.valueOf(categoryType));
		}
		return categoryTypeString;
	}
	
	//RBT-14624 Signal app - RBT tone play notification feature
	public static CategoryBean getDummyCategory(int categoryId){		
			Category category = new Category();
			category.setCategoryId(categoryId);
			CategoryBean categoryBean = new CategoryBean(category, 0);
			categoryBean.setCategoryName("-1");
			categoryBean.setCategoryType(-1);
			categoryBean.setDescription("NA");
			return categoryBean; 
	}
}