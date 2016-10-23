package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;

public class CategoryThreadManager {
	
	private static final Logger log = Logger.getLogger(CategoryThreadManager.class);
	
	List<Category> categoryList = new ArrayList<Category>();
	
	int noOfThreads = -1;
	
	public CategoryThreadManager(List<Category> categoryList, int noOfThreads) {
		this.categoryList = categoryList;
		this.noOfThreads = noOfThreads;
	}

	public void startThreads() throws InterruptedException, MultiThreadCacheInitException {
		ArrayList<GenericCacheThread> threadsList = new ArrayList<GenericCacheThread>(noOfThreads); 
		// separately caching categorytype-categorid. Because multithreading may lost few catIds
		// 
		CategoryTypeThread cctThread = new CategoryTypeThread("CategoryTypeThread",
				new ArrayList<Category>(categoryList));
		cctThread.start();
		threadsList.add(cctThread);
		log.info(cctThread.getName() + " is started with categories " + categoryList.size());
		for (int it = 0; it < noOfThreads; it++) {
			CategoryThread ccThread = new CategoryThread("CategoriesThread" + (it+1), categoryList);
			ccThread.start();
			log.info(ccThread.getName() + " is started...");
			threadsList.add(ccThread);
		}
		log.info("Waiting to finish category threads.");
		ThreadManagerUtils.joinAndCheckThreadsStatus(threadsList);
//		log.info("Getting category infor for category 3 from cache ");
//		Category dummy = (Category)RBTCache.getMemCachedClient().get(RBTCacheKey.getCategoryIdLanguageCacheKey(3, null));
//		log.info("Got category infor for category 3 from cache "+dummy.getCategoryName());
	}
	
}
