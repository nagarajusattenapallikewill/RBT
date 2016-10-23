package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;

public class CategoryTypeThread extends GenericCacheThread {
	
//	private static final Logger log = Logger.getLogger(CacheCategoryType.class);

	HashMap<String, Set<String>> categoryTypeMap = new HashMap<String, Set<String>>();

	public CategoryTypeThread(String name, List records) {
		super(name, records);
	}

	@Override
	public void processRecord(Object obj) {
		// caching in memory
		Category category = (Category) obj;
		String categoryType = Integer.toString(category.getCategoryTpe()).trim();
		if (categoryTypeMap.containsKey(categoryType)) {
			Set<String> set = categoryTypeMap.get(categoryType);
			set.add(RBTCacheKey.getCategoryIdCacheKey(category.getCategoryId()));
			categoryTypeMap.put(categoryType, set);
		} else {
			Set<String> set = new HashSet<String>();
			set.add(RBTCacheKey.getCategoryIdCacheKey(category.getCategoryId()));
			categoryTypeMap.put(categoryType, set);
		}
	}

	@Override
	public void finalProcess() throws Exception {
		// moving records from memory to memcache
		Iterator<String> iterator = categoryTypeMap.keySet().iterator();
		while(iterator.hasNext()){
			String categoryType = iterator.next();
			Set<String> set = categoryTypeMap.get(categoryType);
			mc.set(RBTCacheKey.getCategoryTypeCacheKey(categoryType), (String[]) set.toArray(new String[0]));
		}
	}

}
