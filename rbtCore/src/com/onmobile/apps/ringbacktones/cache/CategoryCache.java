package com.onmobile.apps.ringbacktones.cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.CategoryMap;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
/**
 * @author Sreekar
 * @date 07/08/2008
 */
public class CategoryCache extends CacheModule {
	private static Logger logger = Logger.getLogger(CategoryCache.class);
	
	private static CategoryCache _instance = null;
	//cache refresh variables
	private static Object _syncObj = new Object();
	private static Date _lastUpdatedTime = null;
	private static boolean _updatingCache = false;
	//cache variables
	private static Hashtable _categoryTable;
	private static Hashtable _mapTable;
	
	private static RBTDBManager dbManager = null;

	private CategoryCache() {
		refreshCache(false);
	}
	
	protected static CategoryCache getInstance() {
		if(_instance == null)
			_instance = new CategoryCache();
		return _instance;
	}
	
	protected boolean refreshCache(boolean checkNeeded) {
		boolean refreshCache = false;

		dbManager = RBTDBManager.getInstance();
		
		if(checkNeeded) {
			Date lastDBUpdatedTime = getCategoryRefDate();
			if(_lastUpdatedTime == null)
				_lastUpdatedTime = lastDBUpdatedTime;
			else if(lastDBUpdatedTime.after(_lastUpdatedTime)) {
				_lastUpdatedTime = lastDBUpdatedTime;
				refreshCache = true;
			}
		}
		else
			refreshCache = true;
		
		if(refreshCache) {
			logger.info("RBT::refreshing cache @ " + new Date());
			if (!_updatingCache) {
				synchronized (_syncObj) {
					_updatingCache = true;
					cacheCategories(dbManager);
					_updatingCache = false;
				}
			}
		}
		return refreshCache;
	}
	
	private void cacheCategories(RBTDBManager dbManager) {
		_categoryTable = new Hashtable();
		_mapTable = new Hashtable();
		dbManager.cacheCategories(_categoryTable, _mapTable);
		if(_categoryTable == null)
			_categoryTable = new Hashtable();
		if(_mapTable == null)
			_mapTable = new Hashtable();
		logger.info("RBT::categories cached is " + _categoryTable.size()
				+ ", maps cached is " + _mapTable.size());
		logger.info("Sree:: all categories cached " + _categoryTable);
	}
	
	private Date getCategoryRefDate() {
		Date clipDate = dbManager.getMaxClipStartTime();
		Date categoryDate = dbManager.getMaxCategoryStartTime();

		Calendar clipCal = Calendar.getInstance();
		Calendar catCal = Calendar.getInstance();
		clipCal.setTime(clipDate);
		catCal.setTime(categoryDate);

		Date date = null;
		if(clipCal != null && catCal != null) {
			if(clipCal.before(catCal))
				date = catCal.getTime();
			else
				date = clipCal.getTime();
		}
		return date;
	}
	
	protected Category getCategory(int catID) {
		if(!_updatingCache) {
			Integer key = new Integer(catID);
			if(_categoryTable.containsKey(key))
				return (Category)_categoryTable.get(key);
		}
		return null;
	}
	
	protected Category getCategoryByName(String name) {
		if(!_updatingCache) {
			Iterator itr = _categoryTable.keySet().iterator();
			while(itr.hasNext()) {
				Category cat = (Category)_categoryTable.get(itr.next());
				if(cat.getName().equalsIgnoreCase(name))
					return cat;
			}
		}
		return null;
	}
	
	protected Categories getCategory(int catID, String circleID, char prepaidYes) {
		String method = "getCategory";
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null category");
			return null;
		}
		if(!_updatingCache) {
			Integer catIDInteger = new Integer(catID);
			if(_categoryTable.containsKey(catIDInteger)) {
				if(prepaidYes != 'n' && prepaidYes != 'y')
					prepaidYes = 'b';

				String key = circleID + ":" + prepaidYes;
				CategoryMap mapEntry = null;
				ArrayList al = null;
				ArrayList a2 = null;
				if(_mapTable.containsKey(key))
					al = (ArrayList)_mapTable.get(key);
				if(_mapTable.containsKey(circleID + ":b"))
					a2 = (ArrayList)_mapTable.get(circleID + ":b");

				for(int c = 0; al != null && c < al.size(); c++) {
					CategoryMap temp = (CategoryMap)al.get(c);
					if(temp.getID() == catID)
						mapEntry = temp;
				}
				if(mapEntry == null)
				{
					for(int c = 0; a2 != null && c < a2.size(); c++) {
						CategoryMap temp = (CategoryMap)a2.get(c);
						if(temp.getID() == catID)
							mapEntry = temp;
					}
				}
				return getCategoriesObj((Category)_categoryTable.get(catIDInteger), mapEntry);
			}
		}
		return null;
	}
	
	protected Categories getCategory(String catName, String circleID, char prepaidYes) {
		String method = "getCategory";
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null category");
			return null;
		}
		if(!_updatingCache) {
			Iterator itr = _categoryTable.keySet().iterator();
			while(itr.hasNext()) {
				Integer integer = (Integer)itr.next();
				int catID = integer.intValue();
				Category category = (Category)_categoryTable.get(integer);
				if(category.getName() != null
						&& category.getName().equalsIgnoreCase(catName))
					return getCategory(catID, circleID, prepaidYes);
			}
		}
		return null;
	}
	
	protected Categories getCategoryPromoID(String catPromoID, String circleID, char prepaidYes) {
		String method = "getCategoryPromoID";
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null category");
			return null;
		}
		if(!_updatingCache) {
			Iterator itr = _categoryTable.keySet().iterator();
			while(itr.hasNext()) {
				Integer integer = (Integer)itr.next();
				int catID = integer.intValue();
				Category category = (Category)_categoryTable.get(integer);
				if(category.getPromoID() != null
						&& category.getPromoID().equalsIgnoreCase(catPromoID))
					return getCategory(catID, circleID, prepaidYes);
			}
		}
		return null;
	}
	
	protected Category getCategoryPromoID(String catPromoID) {
		if(!_updatingCache) {
			Iterator itr = _categoryTable.keySet().iterator();
			while(itr.hasNext()) {
				Integer integer = (Integer)itr.next();
				Category category = (Category)_categoryTable.get(integer);
				if(category.getPromoID() != null
						&& category.getPromoID().equalsIgnoreCase(catPromoID))
					return category;
			}
		}
		return null;
	}
	
	protected Categories getCategoryMMNumber(String mmNumber, String circleID, char prepaidYes) {
		String method = "getCategoryMMNumber";
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null category");
			return null;
		}
		if(!_updatingCache) {
			Iterator itr = _categoryTable.keySet().iterator();
			while(itr.hasNext()) {
				Integer integer = (Integer)itr.next();
				int catID = integer.intValue();
				Category category = (Category)_categoryTable.get(integer);
				if(category.getMMNumber() != null
						&& category.getMMNumber().equalsIgnoreCase(mmNumber))
					return getCategory(catID, circleID, prepaidYes);
			}
		}
		return null;
	}
	
	protected Categories getCategoryAlias(String smsAlias, String circleID, char prepaidYes) {
		String method = "getCategoryMMNumber";
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null category");
			return null;
		}
		if(!_updatingCache) {
			Iterator itr = _categoryTable.keySet().iterator();
			while(itr.hasNext()) {
				Integer integer = (Integer)itr.next();
				int catID = integer.intValue();
				Category category = (Category)_categoryTable.get(integer);
				if(category.getSMSAlias() != null
						&& category.getSMSAlias().equalsIgnoreCase(smsAlias))
					return getCategory(catID, circleID, prepaidYes);
			}
		}
		return null;
	}
	
	protected Categories[] getAllCategoriesForCircle(String circleID, boolean dispSubCat) {
		String method = "getAllCategoriesForCircle";
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null category");
			return null;
		}
		
		if(!_updatingCache) {
			ArrayList catList = new ArrayList();
			ArrayList catIDList = new ArrayList();
			Iterator itr = _mapTable.keySet().iterator();
			while(itr.hasNext()) {
				String key = (String)itr.next();
				if(key.indexOf(circleID) != -1) {
					ArrayList mapList = (ArrayList)_mapTable.get(key);
					for(int i = 0; i < mapList.size(); i++) {
						CategoryMap catMap = (CategoryMap)mapList.get(i);
						if(dispSubCat || (catMap != null && catMap.getParentCategoryID() == 0))
						{
							Categories c = getCategoriesObj((Category)_categoryTable.get(new Integer(
									catMap.getID())), catMap);
							if(dispSubCat || (c != null && !catIDList.contains(c.id())))
							{	
								catList.add(c);
								catIDList.add(c.id());
							}
						}
					}
				}// end of if this is a circle key
			}//end of while all keys
			if(catList.size() > 0)
				return (Categories[])catList.toArray(new Categories[0]);
		}
		return null;
	}
	
	protected Categories[] getCategories(String categoryIDs, String circleID, char prepaidYes) {
		String method = "getCategories";
		logger.info("RBT::circleID - " + circleID + ", prepaidYes - "
				+ prepaidYes + ", categoryIDs - " + categoryIDs);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null category");
			return null;
		}
		
		if(!_updatingCache) {
			ArrayList catList = new ArrayList();
			StringTokenizer stk = new StringTokenizer(categoryIDs, ",");
			while(stk.hasMoreTokens()) {
				String thisToken = stk.nextToken();
				try {
					Categories category = getCategory(Integer.parseInt(thisToken), circleID,
							prepaidYes);
					if(category != null)
						catList.add(category);
				}
				catch(Exception e) {
					logger.info("RBT::Exception " + e.getMessage()
							+ " for token " + thisToken);
				}
			}
			if(catList.size() > 0) {
				Collections.sort(catList, CategoriesImpl.INDEX_COMPARATOR);
				return (Categories[])catList.toArray(new Categories[0]);
			}
		}
		return null;
	}
	
	protected Categories[] getActiveCategories(String circleID, char prepaidYes) {
		return getActiveCategories(circleID, prepaidYes, null);
	}
	
	protected Categories[] getActiveCategories(String circleID, char prepaidYes, String language) {
		String method = "getActiveCategories";
		logger.info("RBT::circleID - " + circleID + ", prepaidYes - "
				+ prepaidYes + ", language - " + language);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null categories");
			return null;
		}
		
		if(!_updatingCache) {
			if(prepaidYes != 'n' && prepaidYes != 'y')
				prepaidYes = 'b';
			
			String key = circleID + ":" + prepaidYes;
			ArrayList catList = new ArrayList();
			ArrayList allCircleList = null;
			if(_mapTable.containsKey(key))
				allCircleList = (ArrayList)_mapTable.get(key);
			else if (_mapTable.containsKey(circleID + ":b"))
				allCircleList = (ArrayList)_mapTable.get(circleID + ":b");

			if(allCircleList != null) {
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					Date curDate = new Date();
					try {
						if(category.getEndTime().after(curDate)
								&& category.getStartTime().before(curDate)
								&& category.getType() > 0 && map.getParentCategoryID() == 0) {
							if(language == null)
								catList.add(getCategoriesObj(category, map));
							else if(map.getLanguage() != null
									&& map.getLanguage().equalsIgnoreCase(language))
								catList.add(getCategoriesObj(category, map));
						}
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					Collections.sort(catList, CategoriesImpl.INDEX_COMPARATOR);
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of if has circle key
			else
				logger.info("RBT::no key " + key + " in map cache");
		}
		return null;
	}
	
	protected Categories[] getActiveCategories() {
		String method = "getActiveCategories";
		if(!_updatingCache) {
			ArrayList catList = new ArrayList();
			Iterator itr = _mapTable.keySet().iterator();
			while(itr.hasNext()) {
				String key = (String)itr.next();
				ArrayList allCircleList = (ArrayList)_mapTable.get(key);
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					Date curDate = new Date();
					try {
						if(category.getEndTime().after(curDate)
								&& category.getStartTime().before(curDate)
								&& category.getType() > 0 && map.getParentCategoryID() == 0)
							catList.add(getCategoriesObj(category, map));
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					Collections.sort(catList, CategoriesImpl.INDEX_COMPARATOR);
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of all circles in map cache
		}
		return null;
	}
	
	protected Categories[] getGUIActiveCategories(String circleID, char prepaidYes) {
		String method = "getGUIActiveCategories";
		logger.info("RBT::circleID - " + circleID + ", prepaidYes - "
				+ prepaidYes);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null categories");
			return null;
		}
		
		if(!_updatingCache) {
			if(prepaidYes != 'n' && prepaidYes != 'y')
				prepaidYes = 'b';
			
			String key = circleID + ":" + prepaidYes;
			ArrayList catList = new ArrayList();
			ArrayList allCircleList = null;
			if(_mapTable.containsKey(key))
				allCircleList = (ArrayList)_mapTable.get(key);
			else if (_mapTable.containsKey(circleID + ":b"))
				allCircleList = (ArrayList)_mapTable.get(circleID + ":b");
				
			if(allCircleList != null){
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					Date curDate = new Date();
					try {
						if(category.getStartTime().before(curDate) && category.getType() > 0
								&& map.getParentCategoryID() == 0)
							catList.add(getCategoriesObj(category, map));
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					Collections.sort(catList, CategoriesImpl.INDEX_COMPARATOR);
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of if has circle key
			else
				logger.info("RBT::no key " + key + " in map cache");
		}
		return null;
	}
	
	protected Categories[] getAllCategories(String circleID, char prepaidYes) {
		String method = "getAllCategories";
		logger.info("RBT::circleID - " + circleID + ", prepaidYes - "
				+ prepaidYes);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null categories");
			return null;
		}
		
		if(!_updatingCache) {
			if(prepaidYes != 'n' && prepaidYes != 'y')
				prepaidYes = 'b';
			
			String key = circleID + ":" + prepaidYes;
			ArrayList catList = new ArrayList();
			ArrayList allCircleList = null;
			if(_mapTable.containsKey(key))
				allCircleList = (ArrayList)_mapTable.get(key);
			else if (_mapTable.containsKey(circleID + ":b"))
				allCircleList = (ArrayList)_mapTable.get(circleID + ":b");
				
			if (allCircleList != null) {
				for (int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap) allCircleList.get(i);
					Category category = (Category) _categoryTable.get(new Integer(map.getID()));
					try {
						int type = category.getType();
						if (type != 4 && type != 6 && type != 8)
							catList.add(getCategoriesObj(category, map));
					}
					catch (Exception e) {
						if (category != null)
							logger.info("RBT::Exception " + e.getMessage()
									+ " for category " + category.getID());
						else
							logger.info("RBT::no category entry for id "
									+ map.getID());
						logger.error("", e);
					}
				}
				if (catList.size() > 0) {
					return (Categories[]) catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of if has circle key
			else
				logger.info("RBT::no key " + key + " in map cache");
		}
		return null;
	}
	
	protected Categories[] getAllCategories() {
		String method = "getAllCategories";
		
		if(!_updatingCache) {
			ArrayList catList = new ArrayList();
			Iterator itr = _mapTable.keySet().iterator();
			while(itr.hasNext()) {
				String key = (String)itr.next();
				ArrayList allCircleList = (ArrayList)_mapTable.get(key);
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					if(category == null)
					{
						logger.info("category Not got "+map.getID());
						continue;
					}
					try {
						int type = category.getType();
						if(type != 4 && type != 6 && type != 8)
							catList.add(getCategoriesObj(category, map));
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// while all entries in map table
		}
		return null;
	}
	
	protected Categories[] getSubCategories(String circleID, char prepaidYes, int parentCategoryID,
			String language) {
		String method = "getSubCategories";
		logger.info("RBT::circleID - " + circleID + ", prepaidYes - "
				+ prepaidYes + ", parentCategoryID - " + parentCategoryID + ", language - "
				+ language);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null categories");
			return null;
		}
		
		if(!_updatingCache) {
			if(prepaidYes != 'n' && prepaidYes != 'y')
				prepaidYes = 'b';
			
			String key = circleID + ":" + prepaidYes;
			ArrayList catList = new ArrayList();
			ArrayList allCircleList = null;
			if(_mapTable.containsKey(key))
				allCircleList = (ArrayList)_mapTable.get(key);
			else if (_mapTable.containsKey(circleID + ":b"))
				allCircleList = (ArrayList)_mapTable.get(circleID + ":b");
				
			if(allCircleList != null){
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					Date curDate = new Date();
					try {
						if (map.getParentCategoryID() == parentCategoryID
								&& category.getEndTime().after(curDate)
								&& category.getStartTime().before(curDate)
								&& category.getType() > 0) {
							if(language == null)
								catList.add(getCategoriesObj(category, map));
							else if(map.getLanguage() != null
									&& map.getLanguage().equalsIgnoreCase(language))
								catList.add(getCategoriesObj(category, map));
						}
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					Collections.sort(catList, CategoriesImpl.INDEX_COMPARATOR);
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of if has circle key
			else
				logger.info("RBT::no key " + key + " in map cache");
		}
		return null;
	}
	
	protected Categories[] getGUISubCategories(String circleID, char prepaidYes, int parentCategoryID) {
		String method = "getGUISubCategories";
		logger.info("RBT::circleID - " + circleID + ", prepaidYes - "
				+ prepaidYes + ", parentCategoryID - " + parentCategoryID);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null categories");
			return null;
		}
		
		if(!_updatingCache) {
			if(prepaidYes != 'n' && prepaidYes != 'y')
				prepaidYes = 'b';
			
			String key = circleID + ":" + prepaidYes;
			ArrayList catList = new ArrayList();
			ArrayList allCircleList = null;
			if(_mapTable.containsKey(key))
				allCircleList = (ArrayList)_mapTable.get(key);
			else if (_mapTable.containsKey(circleID + ":b"))
				allCircleList = (ArrayList)_mapTable.get(circleID + ":b");
				
			if(allCircleList != null){
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					Date curDate = new Date();
					try {
						if(category.getStartTime().before(curDate) && category.getType() > 0
								&& map.getParentCategoryID() == 0
								&& map.getParentCategoryID() == parentCategoryID)
							catList.add(getCategoriesObj(category, map));
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					Collections.sort(catList, CategoriesImpl.INDEX_COMPARATOR);
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of if has circle key
			else
				logger.info("RBT::no key " + key + " in map cache");
		}
		return null;
	}
	
	protected Categories[] getActiveBouquet(int parent, String circleID, char prepaidYes,
			String language) {
		String method = "getActiveBouquet";
		logger.info("RBT::parent -  " + parent + ", circleID - " + circleID
				+ ", prepaidYes - " + prepaidYes + ", language - " + language);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null categories");
			return null;
		}
		
		if(!_updatingCache) {
			if(prepaidYes != 'n' && prepaidYes != 'y')
				prepaidYes = 'b';
			
			String key = circleID + ":" + prepaidYes;
			ArrayList catList = new ArrayList();
			ArrayList allCircleList = null;
			if(_mapTable.containsKey(key))
				allCircleList = (ArrayList)_mapTable.get(key);
			else if (_mapTable.containsKey(circleID + ":b"))
				allCircleList = (ArrayList)_mapTable.get(circleID + ":b");
				
			if(allCircleList != null){
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					Date curDate = new Date();
					try {
						if(category.getEndTime().after(curDate)
								&& category.getStartTime().before(curDate)
								&& category.getType() == 0 && map.getParentCategoryID() == parent) {
							if(language == null)
								catList.add(getCategoriesObj(category, map));
							else if(map.getLanguage() != null
									&& map.getLanguage().equalsIgnoreCase(language))
								catList.add(getCategoriesObj(category, map));
						}
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					Collections.sort(catList, CategoriesImpl.INDEX_COMPARATOR);
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of if has circle key
			else
				logger.info("RBT::no key " + key + " in map cache");
		}
		return null;
	}
	
	protected Categories[] getBouquet(String circleID, char prepaidYes) {
		String method = "getBouquet";
		logger.info("RBT::circleID - " + circleID + ", prepaidYes - "
				+ prepaidYes);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null categories");
			return null;
		}
		
		if(!_updatingCache) {
			if(prepaidYes != 'n' && prepaidYes != 'y')
				prepaidYes = 'b';
			
			String key = circleID + ":" + prepaidYes;
			ArrayList catList = new ArrayList();
			ArrayList allCircleList = null;
			if(_mapTable.containsKey(key))
				allCircleList = (ArrayList)_mapTable.get(key);
			else if (_mapTable.containsKey(circleID + ":b"))
				allCircleList = (ArrayList)_mapTable.get(circleID + ":b");
				
			if(allCircleList != null){
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					try {
						if(category.getType() == 0)
							catList.add(getCategoriesObj(category, map));
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					Collections.sort(catList, CategoriesImpl.INDEX_COMPARATOR);
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of if has circle key
			else
				logger.info("RBT::no key " + key + " in map cache");
		}
		return null;
	}
	
	protected HashMap getMBMapByStartTime(Date startDate, Date endDate) {
		HashMap finalMap = new HashMap();
		
		HashMap categoryIDMap = new HashMap();
		Iterator categoryItr = _categoryTable.keySet().iterator();
		while(categoryItr.hasNext()) {
			Integer key = (Integer)categoryItr.next();
			Category category = (Category)_categoryTable.get(key);
			Date curDate = new Date();
			if(category.getType() == 0 && category.getStartTime() != null
					&& category.getEndTime() != null && category.getStartTime().before(curDate)
					&& category.getEndTime().after(curDate))
				categoryIDMap.put(key, category);
		}//end of while all categories in categories table
		
		Iterator mapIterator = _mapTable.keySet().iterator();
		while(mapIterator.hasNext()) {
			String key = (String)mapIterator.next();
			ArrayList al = (ArrayList)_mapTable.get(key);
			for(int i = 0; i < al.size(); i++) {
				CategoryMap map = (CategoryMap)al.get(i);
				Integer catIDKey = new Integer(map.getID());
				if(categoryIDMap.containsKey(catIDKey)) {
					Category category = (Category)categoryIDMap.get(catIDKey);
					finalMap.put(String.valueOf(map.getParentCategoryID()), category.getName());
				}
			}//end of for all maps for a circle-prepYes
		}//end of all entries in map table
		return finalMap;
	}
	
	protected Categories[] getActiveOtherThanUGCCategories(String circleID, char prepaidYes,
			String categoryIDs) {
		String method = "getActiveOtherThanUGCCategories";
		logger.info("RBT::circleID - " + circleID + ", prepaidYes - "
				+ prepaidYes + ", categoryIDs - " + categoryIDs);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null categories");
			return null;
		}
		
		if(!_updatingCache) {
			if(prepaidYes != 'n' && prepaidYes != 'y')
				prepaidYes = 'b';
			
			String key = circleID + ":" + prepaidYes;
			ArrayList catList = new ArrayList();
			ArrayList allCircleList = null;
			if(_mapTable.containsKey(key))
				allCircleList = (ArrayList)_mapTable.get(key);
			else if (_mapTable.containsKey(circleID + ":b"))
				allCircleList = (ArrayList)_mapTable.get(circleID + ":b");
				
			if(allCircleList != null){
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					Date curDate = new Date();
					try {
						if(category.getEndTime().after(curDate)
								&& category.getStartTime().before(curDate)
								&& category.getType() > 0 && map.getParentCategoryID() == 0
								&& categoryIDs.indexOf(category.getID()) == -1)
							catList.add(getCategoriesObj(category, map));
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					Collections.sort(catList, CategoriesImpl.INDEX_COMPARATOR);
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of if has circle key
			else
				logger.info("RBT::no key " + key + " in map cache");
		}
		return null;
	}
	
	protected Categories[] getActiveCategoriesbyCircleId() {
		String method = "getActiveCategoriesbyCircleId";
		if(!_updatingCache) {
			ArrayList catList = new ArrayList();
			Iterator itr = _mapTable.keySet().iterator();
			while(itr.hasNext()) {
				String key = (String)itr.next();
				ArrayList allCircleList = (ArrayList)_mapTable.get(key);
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					Date curDate = new Date();
					try {
						if(category.getEndTime().after(curDate)
								&& category.getStartTime().before(curDate)
								&& category.getType() > 0 && map.getParentCategoryID() == 0)
							catList.add(getCategoriesObj(category, map));
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0) {
					Collections.sort(catList, CategoriesImpl.CIRCLE_ID_INDEX_COMPARATOR);
					return (Categories[])catList.toArray(new Categories[0]);
				}
				else
					logger.info("RBT:: no active categories");
			}// end of while all circle maps
		}
		return null;
	}
	
	protected ArrayList getShuffleCategoryIDs(boolean onlyActive) {
		if(!_updatingCache) {
			ArrayList shuffleList = new ArrayList();
			Iterator itr = _categoryTable.keySet().iterator();
			while(itr.hasNext()) {
				Integer key = (Integer)itr.next();
				Category category = (Category)_categoryTable.get(key);
				if(category.getType() == 0) {
					boolean addShuffle = true;
					if(onlyActive && category.getEndTime().before(new Date()))
						addShuffle = false;
					if(addShuffle)
						shuffleList.add(key);
				}
			}
			if(shuffleList.size() > 0)
				return shuffleList;
			return null;
		}
		return null;
	}
	
	protected Categories[] getOverrideShuffles() {
		String method = "getOverrideShuffles";
		if(!_updatingCache) {
			ArrayList shuffleList = new ArrayList();
			Iterator itr = _mapTable.keySet().iterator();
			while(itr.hasNext()) {
				String key = (String)itr.next();
				ArrayList allCircleList = (ArrayList)_mapTable.get(key);
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					if(category == null)
					{
						logger.info("category Not got "+map.getID());
						continue;
					}

					Date curDate = new Date();
					try {
						if(category.getEndTime().after(curDate) && category.getType() == 0)
							shuffleList.add(getCategoriesObj(category, map));
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
			}
			
			if(shuffleList.size() > 0)
				return (Categories[])shuffleList.toArray(new Categories[0]);
		}
		return null;
	}
	
	protected Categories[] getOverrideShuffles(String circleID, char prepaidYes) {
		String method = "getOverrideShuffles";
		logger.info("RBT::circleID - " + circleID + ", prepaidYes - "
				+ prepaidYes);
		if(circleID == null) {
			logger.info("RBT::circleID is null, returning null categories");
			return null;
		}
		
		if(!_updatingCache) {
			String key = circleID + ":" + prepaidYes;
			ArrayList catList = new ArrayList();
			ArrayList allCircleList = null;
			if(_mapTable.containsKey(key))
				allCircleList = (ArrayList)_mapTable.get(key);
			else if (_mapTable.containsKey(circleID + ":b"))
				allCircleList = (ArrayList)_mapTable.get(circleID + ":b");

			Date nowDate = new Date();
			if(allCircleList != null){
				for(int i = 0; i < allCircleList.size(); i++) {
					CategoryMap map = (CategoryMap)allCircleList.get(i);
					Category category = (Category)_categoryTable.get(new Integer(map.getID()));
					if(category == null)
					{
						logger.info("category Not got "+map.getID());
						continue;
					}
					try {
						if(category.getType() == 10 && category.getEndTime().after(nowDate))
							catList.add(getCategoriesObj(category, map));
					}
					catch(Exception e) {
						logger.info("RBT::Exception " + e.getMessage()
								+ " for category " + category.getID());
						logger.error("", e);
					}
				}
				if(catList.size() > 0)
					return (Categories[])catList.toArray(new Categories[0]);
				else
					logger.info("RBT:: no categories available");
			}// end of if has circle key
			else
				logger.info("RBT::no key " + key + " in map cache");
		}
		return null;
	}
	
	private Categories getCategoriesObj(Category category, CategoryMap map) {
		if(category == null || map == null)
			return null;

		return new CategoriesImpl(category.getID(), category.getName(), category.getNameWavFile(),
				category.getPreviewWavFile(), category.getGrammar(), category.getType(), map
						.getIndex(), String.valueOf(category.getaskMobileNumber()), category
						.getGreeting(), category.getStartTime(), category.getEndTime(), map
						.getParentCategoryID(), category.getClassType(), category.getPromoID(), map
						.getCircleID(), map.getPrepaidYes(), category.getSMSAlias(), category
						.getMMNumber(), map.getLanguage(), category.getLanguageGrammarMap());
	}
}