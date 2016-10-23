package com.onmobile.apps.ringbacktones.cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMap;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.ClipsImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

/**
 * @author Sreekar
 * @date 20/06/2008
 * @description This class stores the 3 types of Clip cache promoId-clipId
 *              cache, clipId-clipMinimal cache and originalPromoID/clipID
 *              cache(only for tata)
 */
public class ClipCache extends CacheModule {
	private static Logger logger = Logger.getLogger(ClipCache.class);
	private String _cacheMode;
	private Hashtable<String, String> _promoIDCache = new Hashtable<String, String>();
	private Hashtable<String, String> _clipIDCache = new Hashtable<String, String>();
	private Hashtable<String, ArrayList<ClipMap>> _clipMapCache = new Hashtable<String, ArrayList<ClipMap>>();
	private Hashtable<String, ClipMinimal> _clipMinimalCache = new Hashtable<String, ClipMinimal>();
	private Hashtable<String,String> _clipWavFileCache = new Hashtable<String,String>();
	private Hashtable<String,String> _sortedClipMaps = new Hashtable<String,String>();
	// following variable is only for TATA _promoIDCache stores the
	// WTCode/ClipID and
	// _actualPromoIDCache stores the HuaweiCodes/ClipID
	private Hashtable<String, String> _actualPromoIDCache = new Hashtable<String, String>();
	private static ClipCache _instance = null;
	private static Object _syncObj = new Object();
	private static boolean _updatingCache = false;
	private static Date _lastUpdatedTime = null;
	RBTDBManager dbManager = null;

	private ClipCache(String cacheMode) {
		logger.info("RBT:: starting ClipCache with mode " + cacheMode);
		_cacheMode = cacheMode;
		refreshCache(false);
	}

	protected static CacheModule getInstance(String cacheMode) {
		synchronized (_syncObj) {
			if(_instance == null)
				_instance = new ClipCache(cacheMode);
		}
		return _instance;
	}

	protected boolean refreshCache(boolean checkNeeded) {
		dbManager = RBTDBManager.getInstance();
		boolean refreshCache = false;
		if(checkNeeded) {
			Date lastDBUpdatedTime = getClipRefDate(dbManager);
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
			logger.info("RBT:: refreshing cache @ " + new Date());
			if(RBTCacheManager.CACHE_MODE_TATA.equals(_cacheMode)) {
				if(!_updatingCache) {
					synchronized (_syncObj) {
						_updatingCache = true;
						cacheTata(dbManager);
						_updatingCache = false;
					}
				}
			}
			else if(RBTCacheManager.CACHE_MODE_NON_TATA.equals(_cacheMode)) {
				if(!_updatingCache) {
					synchronized (_syncObj) {
						_updatingCache = true;
						cacheNonTata(dbManager);
						_updatingCache = false;
					}
				}
			}
		}
		return refreshCache;
	}

	private Date getClipRefDate(RBTDBManager dbManager) {
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

	private void cacheTata(RBTDBManager dbManager) {
		_promoIDCache = new Hashtable<String, String>();
		_actualPromoIDCache = new Hashtable<String, String>();
		_clipIDCache = new Hashtable<String, String>();
		_clipMinimalCache = new Hashtable<String, ClipMinimal>();
		_clipMapCache = new Hashtable<String, ArrayList<ClipMap>>();
		_clipWavFileCache = new Hashtable<String,String>();
		_sortedClipMaps.clear();
		dbManager.getTataClipPromoIdCache(_promoIDCache);
		dbManager.getClipCache(_actualPromoIDCache, _clipMinimalCache, _clipIDCache, _clipMapCache,
				_clipWavFileCache);
	}

	private void cacheNonTata(RBTDBManager dbManager) {
		_promoIDCache = new Hashtable<String, String>();
		_clipIDCache = new Hashtable<String, String>();
		_clipMinimalCache = new Hashtable<String, ClipMinimal>();
		_clipMapCache = new Hashtable<String, ArrayList<ClipMap>>();
		_clipWavFileCache = new Hashtable<String,String>();
		_sortedClipMaps.clear();
		dbManager.getClipCache(_promoIDCache, _clipMinimalCache, _clipIDCache, _clipMapCache,
				_clipWavFileCache);

		logger.info(" Cached sizes _promoIDCache "+ _promoIDCache.size() + " _clipMinimalCache "+ _clipMinimalCache.size() + " _clipIDCache " + _clipIDCache.size() + " _clipMapCache " + _clipMapCache.size() + " _clipWavFileCache " + _clipWavFileCache.size());
	}

	protected int getClipIDForPromoId(String promoID, boolean checkMap) {
		int clipId = -1;
		if(!_updatingCache && _promoIDCache != null) {
			String clipIdStr = _promoIDCache.get(promoID);
			if(checkMap && clipIdStr != null && _clipIDCache.containsKey(clipIdStr))
				clipIdStr = _promoIDCache.get(promoID);
			else if(!checkMap)
				clipIdStr = _promoIDCache.get(promoID);
			else
				clipIdStr = null;
		
			if(clipIdStr == null && !_promoIDCache.containsKey(promoID))
			{
				if(updateCache(2, promoID))
					getClipIDForPromoId(promoID, checkMap);
			}

			try {
				clipId = Integer.parseInt(clipIdStr);
			}
			catch (Exception e) {

			}
		}
		return clipId;
	}
	
	protected int getClipIDForWavFile(String wavFile, boolean checkMap) {
		int clipId = -1;
		if(!_updatingCache && _clipWavFileCache != null && _clipWavFileCache.containsKey(wavFile)) {
			String clipIdStr = (String)_clipWavFileCache.get(wavFile);
			
			if(checkMap && clipIdStr != null && _clipIDCache.containsKey(clipIdStr))
				clipIdStr = (String)_clipWavFileCache.get(wavFile);
			else if(!checkMap)
			
				clipIdStr = (String)_clipWavFileCache.get(wavFile);
			else
				clipIdStr = null;
			
			try {
				clipId = Integer.parseInt(clipIdStr);
			}
			catch (Exception e) {
				System.out.println("i m in catch");
			}
		}
		return clipId;
	}

	protected ClipMinimal getClipSMSAlias(String smsAlias) {
		if(smsAlias == null || smsAlias.equals(""))
			return null;
		ClipMinimal clipMinimal = null;
		if(!_updatingCache && _clipMinimalCache != null) {
			Iterator<String> itr = _clipMinimalCache.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				ClipMinimal temp = _clipMinimalCache.get(key);
				if(_clipIDCache.containsKey(key) && temp.getSMSAlias() != null
						&& temp.getSMSAlias().equalsIgnoreCase(smsAlias)) {
					clipMinimal = temp;
					break;
				}
			}
		}
		return clipMinimal;
	}

	// the following mehod is used for TATA
	protected int getActualClipIDForPromoId(String promoID) {
		int clipId = -1;
		if(!_updatingCache && _actualPromoIDCache != null) {
			String clipIdStr = _actualPromoIDCache.get(promoID);
			try {
				clipId = Integer.parseInt(clipIdStr);
			}
			catch (Exception e) {

			}
		}
		return clipId;
	}

	protected ClipMinimal getClip(int clipID, boolean checkForMap) {
		String clipIDStr = String.valueOf(clipID);
		ClipMinimal retVal = null;
		if(!_updatingCache && _clipMinimalCache.containsKey(clipIDStr)) {
			if(checkForMap && _clipIDCache.containsKey(clipIDStr))
				retVal = _clipMinimalCache.get(clipIDStr);
			else if(!checkForMap)
				retVal = _clipMinimalCache.get(clipIDStr);
		}
		else if(!_updatingCache )
		{
			if(updateCache(0, clipID+""))
				getClip(clipID, checkForMap);
		}

		return retVal;
	}
	
	protected ClipMinimal[] getClipsByName(String start) {
		if(!_updatingCache) {
			ArrayList<ClipMinimal> al = new ArrayList<ClipMinimal>();
			Iterator<String> itr = _clipMinimalCache.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String)itr.next();
				ClipMinimal clipMinimal = (ClipMinimal)_clipMinimalCache.get(key);
				if(clipMinimal.getStartingLetter().equalsIgnoreCase(start)
						&& _clipIDCache.containsKey(key))
					al.add(clipMinimal);
			}
			if(al.size() > 0) {
				Collections.sort(al, ClipMinimal.NAME_COMPARATOR);
				return al.toArray(new ClipMinimal[0]);
			}
		}// end of if not updating cache
		return null;
	}
	
	protected ClipMinimal getClipByName(String clipName, boolean checkMap) {
		if(!_updatingCache) {
			Iterator<String> itr = _clipMinimalCache.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				ClipMinimal clipMinimal = _clipMinimalCache.get(key);
				if(checkMap && _clipIDCache.containsKey(key)
						&& clipMinimal.getClipName().equalsIgnoreCase(clipName))
					return clipMinimal;
				else if(!checkMap && clipMinimal.getClipName().equalsIgnoreCase(clipName))
					return clipMinimal;
			}
		}// end of if not updating cache
		return null;
	}
	
	protected ClipMinimal getClipRBT(String wavFile, boolean checkMap) {
			ClipMinimal clipMinimal = null;
			if(!_updatingCache) {
			Iterator<String> itr = _clipMinimalCache.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				clipMinimal = _clipMinimalCache.get(key);
				if(checkMap && _clipIDCache.containsKey(key)
						&& clipMinimal.getWavFile().equalsIgnoreCase(wavFile))
					return clipMinimal;
				else if(!checkMap && clipMinimal.getWavFile().equalsIgnoreCase(wavFile))
					return clipMinimal;
			}
			if(clipMinimal == null && !_clipWavFileCache.containsKey(wavFile) )
				if(updateCache(1, wavFile))
					getClipRBT(wavFile, checkMap);
		}
		return null;
	}
	
	//sending all the clips now as we are caching only mapped clips
    protected SortedMap<String, String> getSMSPromoClips() {
		if(!_updatingCache) {
			Iterator<String> iter = _clipMinimalCache.keySet().iterator();
			SortedMap<String, String> clipsMap = new TreeMap<String, String>();
			while (iter.hasNext()) {
				String key = iter.next();
				ClipMinimal clip = (ClipMinimal)_clipMinimalCache.get(key);
				if(_clipIDCache.containsKey(key))
					clipsMap.put(clip.getClipName(), clip.getWavFile());
			}
			return clipsMap;
		}
		return null;
	}
    
    protected int[] getClipIDsInCategory(int catID) {
		if(!_updatingCache) {
			String key = String.valueOf(catID);
			if(_clipMapCache.containsKey(key)) {
				ArrayList<ClipMap> clipMapList = _clipMapCache.get(key);
				int[] allClipIDs = new int[clipMapList.size()];
				for(int i = 0; i < clipMapList.size(); i++) {
					allClipIDs[i] = ((ClipMap)clipMapList.get(i)).getClipID();
				}
				return allClipIDs;
			}
		}
		return null;
	}
    
    protected int getCatIDsForClipId(int clipID) {
    	int categoryID = -1;
		if(!_updatingCache) {
			String key = String.valueOf(clipID);
			if(_clipIDCache.containsKey(key)) {
				String catID = _clipIDCache.get(key);
				try
				{
					categoryID = Integer.parseInt(catID);
				}
				catch(Exception e)
				{
					categoryID = -1;
				}
				return categoryID;
			}
		}
		return categoryID;
	}
    
    protected ClipMinimal[] getAllActiveClips() {
    	if(!_updatingCache) {
    		ArrayList<ClipMinimal> clipsList = new ArrayList<ClipMinimal>();
    		Iterator<String> itr = _clipMinimalCache.keySet().iterator();
    		Date nowDate = new Date();
    		while(itr.hasNext()) {
    			ClipMinimal clip = (ClipMinimal)_clipMinimalCache.get((String)itr.next());
    			if(clip.getEndTime().after(nowDate))
    				clipsList.add(clip);
    		}
    		if(clipsList.size() > 0)
    			return clipsList.toArray(new ClipMinimal[0]);
    	}
    	return null;
    }
    
    protected String[] getClipsNotInCategories(String categories) {
    	logger.info("inside getClipsNotInCategories");
    	if(!_updatingCache && categories != null) {
    		logger.info("RBT::in for categories " + categories);
    		StringTokenizer stk = new StringTokenizer(categories, ",");
    		HashMap<String, String> cats = new HashMap<String, String>();
    		while(stk.hasMoreTokens())
    			cats.put(stk.nextToken(), "1");
    		
    		ArrayList<String> clipsList = new ArrayList<String>();
    		logger.info("The size of clipMapCache: "+_clipMapCache.size());
    		Iterator<String> itr = _clipMapCache.keySet().iterator();
    		while(itr.hasNext()) {
    			String key = itr.next();
    			if(cats.containsKey(key))
    				continue;
    			
    			ArrayList<ClipMap> clips = _clipMapCache.get(key);
    			for(int index = 0; (clips != null && index < clips.size()); index++) {
    				ClipMap map = clips.get(index);
    				if(clipsList != null && map != null && !clipsList.contains(String.valueOf(map.getClipID())))
						clipsList.add(String.valueOf(map.getClipID()));
    			}
    		}
    		if(clipsList.size() > 0) {
    			logger.info("RBT::Found " + clipsList.size() + " clips");
    			return clipsList.toArray(new String[0]);
    		}
    	}
    	return null;
    }
    
    protected Clips[] getClipsInCategories(String categories) {
    	if(!_updatingCache && categories != null) {
    		StringTokenizer stk = new StringTokenizer(categories, ",");

    		ArrayList<Clips> clipsList = new ArrayList<Clips>();
    		while(stk.hasMoreTokens()) {
    			String key = stk.nextToken();
    			if(_clipMapCache.containsKey(key)) {
    				ArrayList<ClipMap> maps = _clipMapCache.get(key);
    				for(int index = 0; index < maps.size(); index++)
    					clipsList.add(getClip(maps.get(index).getClipID(), false).getClipsObj());
    			}
    		}
    		if(clipsList.size() > 0)
    			return clipsList.toArray(new Clips[0]);
    	}
    	return null;
    }
    
    protected Clips[] getActiveCategoryClips(int categoryID, String chargeClasses, char clipInYes) {
    	if(!_updatingCache) {
    		logger.info("RBT::in for category " + categoryID);
    		ArrayList<Clips> clipsList = new ArrayList<Clips>();
    		String catIDStr = String.valueOf(categoryID);
    		ArrayList<ClipMap> clipMap = null;
    		if(_clipMapCache.containsKey(catIDStr))
    			clipMap = _clipMapCache.get(catIDStr);
    		if(clipMap == null || clipMap.isEmpty()) {
    			logger.info("RBT::No Clips mapped for category " + categoryID);
    			return null;
    		}
    		//sorting the clips based on category_clip_index
    		if(!_sortedClipMaps.containsKey(catIDStr)) {
    			Collections.sort(clipMap, ClipMap.INDEX_COMPARATOR);
    			_clipMapCache.put(catIDStr, clipMap);
    			_sortedClipMaps.put(catIDStr, "1");
    		}
    		HashMap<String, String> chargeClassMap = new HashMap<String, String>();
    		if(chargeClasses != null) {
    			StringTokenizer stk = new StringTokenizer(chargeClasses, ",");
    			while(stk.hasMoreTokens())
    				chargeClassMap.put(stk.nextToken(), "1");
    		}
    		for(int index = 0; index < clipMap.size(); index++) {
    			ClipMap map = clipMap.get(index);
    			ClipMinimal clip = getClip(map.getClipID(), false);
    			if(clip != null && (clipInYes == 'b' || clipInYes == map.getClipInList())) {
    				if(chargeClasses == null || chargeClassMap.containsKey(clip.getClassType())) {
						ClipsImpl clips = clip.getClipsObj();
						clips.setClipInList(String.valueOf(map.getClipInList()));
						clips.setPlayTime(String.valueOf(map.getPlayTime()));
						clipsList.add(clips);
					}
    			}
    		}// end of for all clipMap
    		if(clipsList.size() > 0) {
    			logger.info("RBT::Found " + clipsList.size()
						+ " clips for category " + categoryID);
    			return clipsList.toArray(new Clips[0]);
    		}
    		else
    			logger.info("RBT:: no clips for category " + categoryID);
    	}
    	return null;
    }
    
    protected Clips[] getClipsByAlbum(String album) {
		if(!_updatingCache) {
			ArrayList<Clips> clipsList = new ArrayList<Clips>();
			Iterator<String> itr = _clipMinimalCache.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				ClipMinimal clipMinimal = _clipMinimalCache.get(key);
				if(clipMinimal.getAlbum() != null && clipMinimal.getAlbum().equalsIgnoreCase(album))
					clipsList.add(clipMinimal.getClipsObj());
			}
			if(clipsList.size() > 0)
				return clipsList.toArray(new Clips[0]);
			else
				logger.info("RBT::No clips with Album - ");
		}// end of if not updating cache
		return null;
	}

	protected boolean updateCache(int type, String attr)
	{
		return dbManager.getClipCacheForAttribute(type, attr, _promoIDCache,
			 _clipMinimalCache,  _clipIDCache,
			 _clipMapCache, _clipWavFileCache);
	}
}