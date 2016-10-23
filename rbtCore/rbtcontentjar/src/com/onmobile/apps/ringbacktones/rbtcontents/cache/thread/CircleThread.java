package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Circle;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CircleCategoryMap;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CircleCategoryMapDAO;

public class CircleThread extends GenericCacheThread {

	private static final Logger log = Logger.getLogger(CircleThread.class);

	HashMap<String, Set<String>> catTypeCircleIdMap = new HashMap<String, Set<String>>();

	HashMap<String, Set<String>> catPromoIdCircleIdMap = new HashMap<String, Set<String>>();
	
	private List<Circle> circleList = null;

	public CircleThread(String name, List<Circle> circleList) {
		// here run method is overridden so no need to pass list here
		super(name, null);
		this.circleList = circleList;
	}

	public void run() {
		try {
			long l1 = System.currentTimeMillis();
			while (circleList.size() != 0) {
				// stop thread here if any exception occurs
				if (stop) {
					log.error("Forcibly stopping " + getName() + " because some other thread got error.");
					return;
				}
				Circle circle = null;
				synchronized (circleList) {
					if (circleList.size() == 0) {
						break;
					}
					circle = circleList.remove(0);
				}
				processRecord(circle);
			}
			finalProcess();
			long l2 = System.currentTimeMillis();
			log.info(getName() + " is successfully finished processing. TimeTaken: " + (l2 - l1) + "ms");
		} catch (Exception e) {
			setException(e);
			stopThreads();
			log.error("Exception occurred in " + getName() + " while adding records to cache", e);
		}
	}

	public void processRecord(Object obj) throws Exception {
		Circle circle = (Circle) obj;
		mc.set(RBTCacheKey.getCircleIdCacheKey(circle.getCircleId()), circle);
		if(log.isDebugEnabled()) {
			log.debug("Initializing the categories in circle cache: " + circle.getCircleId());
		}
		List<CircleCategoryMap> categoriesInCircle = CircleCategoryMapDAO.getCategoriesInCircle(circle.getCircleId());
		if(null == categoriesInCircle) {
			log.error("There are no categories in circle " + circle.getCircleId());
			return;
		}
		cacheCircleCategoryMap(categoriesInCircle);
		if(log.isDebugEnabled()) {
			log.debug("Initialized the categories in circle cache for " + circle.getCircleName());
		}
	}

	private void cacheCircleCategoryMap(List<CircleCategoryMap> categoriesInCircle) {

		HashMap<String, List<String>> result = new HashMap<String, List<String>>();

		Date date = new Date();
		for(int i=0; i<categoriesInCircle.size(); i++) {
			CircleCategoryMap circleCategoryMap = categoriesInCircle.get(i);
//			if(circleCategoryMap == null){
//				continue;
//			}
			Category category = (Category) RBTCache.getMemCachedClient().get(
					RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));
			
			if(category != null){

				//Make the circleID, prepaidYes, category type cache
				String catTypeCircleIdKey = RBTCacheKey.getTypePrepadiCircleIdCacheKey(
						circleCategoryMap.getCircleId(), circleCategoryMap.getPrepaidYes(), Integer
								.toString(category.getCategoryTpe()));
				
				Set<String> catIdSet1 = catTypeCircleIdMap.get(catTypeCircleIdKey);
//				if(!catTypeCircleIdMap.containsKey(catTypeCircleIdKey)){
				if(catIdSet1 == null) {
					catIdSet1 = new HashSet<String>();
					catTypeCircleIdMap.put(catTypeCircleIdKey, catIdSet1);
				}
				catIdSet1.add(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));

				if (null != category.getCategoryPromoId() && category.getCategoryPromoId().length() > 0) {
					//make the circleId, prepaidYes, categoryPromoId cache 
					String[] categoryPromoCode = category.getCategoryPromoId().split(",");
					for (int index = 0; index < categoryPromoCode.length; index++) {
						String catPromoIdCircleIdKey = RBTCacheKey.getPromoIdPrepadiCircleIdCacheKey(
								circleCategoryMap.getCircleId(), circleCategoryMap.getPrepaidYes(),
								categoryPromoCode[index]);
						
						Set<String> catIdSet2 = catPromoIdCircleIdMap.get(catPromoIdCircleIdKey);
						if(catIdSet2 == null){
							catIdSet2 = new HashSet<String>();
							catPromoIdCircleIdMap.put(catPromoIdCircleIdKey, catIdSet2);
						}
						catIdSet2.add(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));
					}
//					String catPromoIdCircleIdKey = RBTCacheKey.getPromoIdPrepadiCircleIdCacheKey(
//							circleCategoryMap.getCircleId(), circleCategoryMap.getPrepaidYes(),
//							category.getCategoryPromoId());
//					
//					Set<String> catIdSet2 = catPromoIdCircleIdMap.get(catPromoIdCircleIdKey);
////					if(!catPromoIdCircleIdMap.containsKey(catPromoIdCircleIdKey)){
//					if(catIdSet2 == null){
//						catIdSet2 = new HashSet<String>();
//						catPromoIdCircleIdMap.put(catPromoIdCircleIdKey, catIdSet2);
//					}
//					catIdSet2.add(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));
				}
				
				String key = RBTCacheKey.getCategoriesInCircleCacheKey(circleCategoryMap.getCircleId(), 
								circleCategoryMap.getParentCategoryId(), 
								circleCategoryMap.getPrepaidYes(),
								circleCategoryMap.getCategoryLanguage());

				List<String> values = result.get(key);
				if(null == values) {
					values = new ArrayList<String>();
					result.put(key, values);
				}
				values.add(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));
				if(category.getCategoryStartTime().before(date) && category.getCategoryEndTime().after(date)){
					String key1 = RBTCacheKey.getActiveCategoriesInCircleCacheKey(circleCategoryMap.getCircleId(), 
							circleCategoryMap.getParentCategoryId(), 
							circleCategoryMap.getPrepaidYes(),
							circleCategoryMap.getCategoryLanguage());
					List<String> activeCategoryValues = result.get(key1);
					if(null == activeCategoryValues) {
						activeCategoryValues = new ArrayList<String>();
						result.put(key1, activeCategoryValues);
					}
					activeCategoryValues.add(RBTCacheKey.getCategoryIdCacheKey(circleCategoryMap.getCategoryId()));
				}
			}
		}
		// convert value from list to array and set in memcache
		Iterator<String> keysIterator = result.keySet().iterator();
		while(keysIterator.hasNext()) {
			String keyTemp = keysIterator.next();
			List<String> vauesTemp = result.get(keyTemp);
			mc.set(keyTemp, vauesTemp.toArray(new String[]{}));
		}
	}

	@Override
	public void finalProcess() throws Exception {
		//Put the category-ids based on category type, prepaid yes, circle id.
		putMapInCache(catTypeCircleIdMap);
		//Put the category-ids based on category promoid, prepaid yes, circle id.
		putMapInCache(catPromoIdCircleIdMap);
	}

	private void putMapInCache(Map<String,Set<String>> catTypeCircleIdMap){
		
		Iterator<String> iterator = catTypeCircleIdMap.keySet().iterator();
		
		while(iterator.hasNext()){
			String key = iterator.next();
			mc.set(key, (String[])catTypeCircleIdMap.get(key).toArray(new String[0]));
		}		
	}

}
