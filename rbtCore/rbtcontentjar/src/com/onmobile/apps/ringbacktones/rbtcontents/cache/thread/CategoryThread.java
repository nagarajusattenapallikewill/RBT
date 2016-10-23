package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryClipMap;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryInfo;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category.CategoryInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoryClipMapDAO;

public class CategoryThread extends GenericCacheThread {
	
	private static final Logger log = Logger.getLogger(CategoryThread.class);

	private List<Category> categoryList = null;
	
	CategoryThread(String name, List<Category> categoryList) {
		// here run method is overridden so no need to pass list here
		super(name, null);
		this.categoryList = categoryList;
	}
	
	public void run() {
		try {
			long l1 = System.currentTimeMillis();
			int count = 0;
			while (categoryList.size() != 0) {
				// stop thread, if any of the child threads throws exception
				if (stop) {
					log.error("Forcibly stopping " + getName() + " because some other thread got error.");
					return;
				}
				Category category = null;
				synchronized (categoryList) {
					if (categoryList.size() == 0) {
						break;
					}
					category = categoryList.remove(0);
				}
				count++;
				processRecord(category);
			}
			long l2 = System.currentTimeMillis();
			log.info(getName() + " is successfully finished processing " + count
					+ " categories. TimeTaken: " + (l2 - l1) + "ms");
		} catch (Exception e) {
			setException(e);
			stopThreads();
			log.error("Exception occurred in " + getName() + " while adding records to cache", e);
		}
	}

	@Override
	public void processRecord(Object obj) throws Exception {
		String defaultLanguage = RBTContentJarParameters.getInstance().getParameter("default_language");
		String[] supportedLanguages = null;
		if(RBTContentJarParameters.getInstance().getParameter("supported_languages")!=null && !(RBTContentJarParameters.getInstance().getParameter("supported_languages").equals(""))){
			supportedLanguages = RBTContentJarParameters.getInstance().getParameter("supported_languages").split(",");
		}
		Category category = (Category) obj;
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
			
		}
		/* Set for all languages as well as particular language
		*  For all languages the key will be categoryid_<CATEGORY_ID>_ALL, in this case the set will be null
		*  For particular language the key will be categoryid_<CATEGORY_ID>_LANG, in this case the map will be null
		*/
		//-------- Set a temp variable for categoryInfoSet
		Set<CategoryInfo> tempCategoryInfoSet = category.getCategoryInfoSet();
		category.setCategoryInfoSet(null);
		category.setCategoryLanguage(defaultLanguage);
		//-----------Set the cache for default language
		mc.set(RBTCacheKey.getCategoryIdLanguageCacheKey(category.getCategoryId(), defaultLanguage), category);
		mc.set(RBTCacheKey.getCategoryNameCacheKey(category.getCategoryName()), "" + category.getCategoryId());
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
			}
			}
			String origCatDesc = categoryInfoMap.get(Category.CategoryInfoKeys.CAT_DESC.name());
			/* Set for all languages
			*  CatetgoryInfoset is not required in case of all languages
			*/
			category.setCategoryInfoSet(null);
			mc.set(RBTCacheKey.getCategoryIdLanguageCacheKey(category.getCategoryId(),"ALL"), category);
			//-------- Again set the CatetgoryInfoSet
			category.setCategoryInfoSet(tempCategoryInfoSet);
			//--------- CatetgoryInfoMap is not required in case of specific languages
			category.setCategoryInfoMap(categoryMap);
			
			//-----------Set the cache for default language
			mc.set(RBTCacheKey.getCategoryIdLanguageCacheKey(category.getCategoryId(), defaultLanguage), category);
			//------- Set for supported languages
			if(supportedLanguages!=null && supportedLanguages.length>0){
				for(int i=0; i<supportedLanguages.length; i++){
					Iterator<CategoryInfo> categoryInfoitr = category.getCategoryInfoSet().iterator();
					category.setCategoryName(null);
					category.setCategoryGrammar(null);
					category.setCategoryInfo(null);
					category.setCategoryLanguage(defaultLanguage);
					//-------- Set information on the Category bean for the passed language from the CatetgoryInfoSet
					Map<String, String> langCategoryMap = new HashMap<String,String>(categoryMap);
					boolean isLangDescAvail = false;
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
							isLangDescAvail = true;
							langCategoryMap.put(Category.CategoryInfoKeys.CAT_DESC.name(), categoryInfo.getValue());
						}
					}
					
					boolean isSupportedLangAvil = isLangDescAvail;
					
					if(category.getCategoryName()==null || category.getCategoryName().equals("")) {
						category.setCategoryName(origCategoryName);
					}
					else{
						isSupportedLangAvil = true;
					}
					
					if(category.getCategoryGrammar()==null || category.getCategoryGrammar().equals("")) category.setCategoryGrammar(origCategoryGrammar);
					if(category.getCategoryInfo()==null || category.getCategoryInfo().equals("")) category.setCategoryInfo(origCategoryInfo);
					
					String catDesc = langCategoryMap.get(Category.CategoryInfoKeys.CAT_DESC.name());
					if (catDesc == null || catDesc.equals("")) {
						langCategoryMap.put(Category.CategoryInfoKeys.CAT_DESC.name(), origCatDesc);
					}
//					else {
//						isSupportedLangAvil = true;
//					}
					
					category.setCategoryInfoMap(langCategoryMap);
					
					log.info("DEBUG1********* " + isSupportedLangAvil + " langCategoryMap: " + langCategoryMap + " supportedLanguages[i]" + supportedLanguages[i]);
					
					//If not supported, we don't add to the memcache.
					if(isSupportedLangAvil) {
						category.setCategoryLanguage(supportedLanguages[i]);
						log.info("DEBUG1********* " + isSupportedLangAvil + " Category: " + category);
						mc.set(RBTCacheKey.getCategoryIdLanguageCacheKey(category.getCategoryId(), supportedLanguages[i]), category);
						mc.set(RBTCacheKey.getCategoryNameCacheKey(category.getCategoryName()), "" + category.getCategoryId());
					}
					
				}
			}
		}
		
		mc.set(RBTCacheKey.getCategoryNameCacheKey(category.getCategoryName()), ""
				+ category.getCategoryId());

		if (category.getCategoryPromoId() != null && category.getCategoryPromoId().length() > 0) {
			String[] categoryPromoCode = category.getCategoryPromoId().split(",");
			for (int index = 0; index < categoryPromoCode.length; index++) {
				mc.set(RBTCacheKey.getCategoryPromoIdCacheKey(categoryPromoCode[index].trim()), ""
						+ category.getCategoryId());
			}
//			mc.set(RBTCacheKey.getCategoryPromoIdCacheKey(category.getCategoryPromoId()), ""
//					+ category.getCategoryId());
		}

		if (category.getMmNumber() != null && category.getMmNumber().length() > 0) {
			mc.set(RBTCacheKey.getCategoryMMNumberCacheKey(category.getMmNumber()), ""
					+ category.getCategoryId());
		}

		if (category.getCategorySmsAlias() != null && category.getCategorySmsAlias().length() > 0) {
			String[] smsAlias = RBTCache.getMultipleSmsAlias(category.getCategorySmsAlias());
			for(int i=0; i<smsAlias.length; i++){
				mc.set(RBTCacheKey.getCategorySmsAliasCacheKey(smsAlias[i]), "" + category.getCategoryId());
			}
//			mc.set(RBTCacheKey.getCategorySmsAliasCacheKey(category.getCategorySmsAlias()), ""
//					+ category.getCategoryId());
		}

		//get clips mapped in the circle
		if (log.isDebugEnabled()) {
			log.debug("Initializing clips in category cache: " + category.getCategoryName());
		}
		List<CategoryClipMap> clipsInCategory = CategoryClipMapDAO.getClipsInCategory(category
				.getCategoryId());
		putCategoryClipInCache(clipsInCategory, category.getCategoryId());
		if (log.isDebugEnabled()) {
			log.debug("Initialized the clips in category cache");
		}
		
	}

	private void putCategoryClipInCache(List<CategoryClipMap> clipsInCategory, int categoryId) {
		Date now = new Date();
		List<String> allClipResult = new ArrayList<String>(clipsInCategory.size());
		List<String> activeClipResult = new ArrayList<String>(clipsInCategory.size());
		for (int i = 0; i < clipsInCategory.size(); i++) {
			CategoryClipMap clipInCategory = clipsInCategory.get(i);
			String key = RBTCacheKey.getClipIdCacheKey(clipInCategory.getClipId());
			Clip clip = (Clip) RBTCache.getMemCachedClient().get(key);
			if (null != clip) {
				if (clip.getClipStartTime().before(now)) {
					allClipResult.add(key);
				}
				if (clip.getClipStartTime().before(now) && clip.getClipEndTime().after(now)) {
					activeClipResult.add(key);
				}
			}
		}
		if (allClipResult != null && allClipResult.size() > 0) {
			mc.set(RBTCacheKey.getClipsInCategoryCacheKey(categoryId), allClipResult
					.toArray(new String[0]));
		}

		if (activeClipResult != null && activeClipResult.size() > 0) {
			mc.set(RBTCacheKey.getActiveClipsInCategoryCacheKey(categoryId), activeClipResult
					.toArray(new String[0]));
		}

	}

	@Override
	public void finalProcess() throws Exception {
		// nothing to do
		// implementation is different
	}

}
