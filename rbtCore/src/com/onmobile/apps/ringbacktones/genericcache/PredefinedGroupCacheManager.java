/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.PredefinedGroup;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.dao.PredefinedGroupDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.CacheNamesEnum;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * @author vinayasimha.patil
 *
 */
public class PredefinedGroupCacheManager
{
	private static Logger logger = Logger.getLogger(PredefinedGroupCacheManager.class);

	private IGenericCache genericCache;

	private PredefinedGroupCacheManager(IGenericCache genericCache)
	{
		this.genericCache = genericCache;
	}

	/**
	 * @param preGroupID
	 * @return PredefinedGroup
	 */
	public PredefinedGroup getPredefinedGroup(String preGroupID)
	{
		String cacheName = CacheNamesEnum.PREDEFINED_GROUP.toString();
		PredefinedGroup predefinedGroup = (PredefinedGroup) genericCache.getFromCache(cacheName, preGroupID);

		return predefinedGroup;
	}

	/**
	 * @param preGroupID
	 * @param language
	 * @return PredefinedGroup
	 */
	public PredefinedGroup getPredefinedGroup(String preGroupID, String language)
	{
		PredefinedGroup predefinedGroup = getPredefinedGroup(preGroupID);
		predefinedGroup = updateLanguageSpecificInfo(predefinedGroup, language);

		return predefinedGroup;
	}

	/**
	 * @return List<PredefinedGroup>
	 */
	public List<PredefinedGroup> getAllPredefinedGroups()
	{
		String cacheName = CacheNamesEnum.PREDEFINED_GROUP.toString();
		List<PredefinedGroup> predefinedGroupList = new ArrayList<PredefinedGroup>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			if (object == null)
				continue;

			predefinedGroupList.add((PredefinedGroup) object);
		}

		return predefinedGroupList;
	}

	public List<PredefinedGroup> getAllPredefinedGroups(String language)
	{
		List<PredefinedGroup> langSpecificPredefinedGroupList = new ArrayList<PredefinedGroup>();  
		
		List<PredefinedGroup> predefinedGroupList =  getAllPredefinedGroups();
		for (PredefinedGroup predefinedGroup : predefinedGroupList)
		{
			langSpecificPredefinedGroupList.add(updateLanguageSpecificInfo(predefinedGroup, language));
		}

		return langSpecificPredefinedGroupList;
	}

	private PredefinedGroup updateLanguageSpecificInfo(PredefinedGroup predefinedGroup, String language)
	{
		PredefinedGroup newPredefinedGroup = predefinedGroup;
		if (predefinedGroup != null)
		{
			
			String preGroupID = predefinedGroup.getPreGroupID();

			RBTText rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("PRE_GROUP_NAME", preGroupID, language);
			if (rbtText != null)
			{
				try
				{
					newPredefinedGroup = predefinedGroup.clone();
					newPredefinedGroup.setPreGroupName(rbtText.getText());
				}
				catch (CloneNotSupportedException e)
				{
				}
			}
		}

		return newPredefinedGroup;
	}
	
	public boolean addPredefinedGroup(PredefinedGroup predefinedGroup)
	{
		logger.info("predefinedGroup: " + predefinedGroup);
		String cacheName = CacheNamesEnum.PREDEFINED_GROUP.toString();
		boolean added = false;

		String key = predefinedGroup.getPreGroupID();
		Object object = genericCache.getFromCache(cacheName, key);
		if (object == null)
		{
			PredefinedGroupDao predefinedGroupDao = new PredefinedGroupDao();
			predefinedGroupDao.insertPredefinedGroup(predefinedGroup);
			genericCache.updateToCache(cacheName, key, predefinedGroup);
			added = true;
		}
		else
		{
			added = false;
			logger.info("The PredefinedGroup already exists in the Cache. Couldnt add PredefinedGroup");
		}

		return added;
	}

	public boolean updatePredefinedGroup(PredefinedGroup predefinedGroup)
	{
		logger.info("predefinedGroup: " + predefinedGroup);
		String cacheName = CacheNamesEnum.PREDEFINED_GROUP.toString();
		boolean updated = false;

		String key = predefinedGroup.getPreGroupID();
		Object object = genericCache.getFromCache(cacheName, key);
		if (object != null)
		{
			PredefinedGroupDao predefinedGroupDao = new PredefinedGroupDao();
			predefinedGroupDao.updatePredefinedGroup(predefinedGroup);
			genericCache.updateToCache(cacheName, key, predefinedGroup);
			updated = true;
		}
		else
		{
			updated = false;
			logger.info("The PredefinedGroup does not exist in the Cache. Couldnt update PredefinedGroup");
		}

		return updated;
	}

	public boolean removePredefinedGroup(String predefinedGroupID)
	{
		logger.info("predefinedGroupID: " + predefinedGroupID);
		String cacheName = CacheNamesEnum.PREDEFINED_GROUP.toString();

		PredefinedGroup predefinedGroup = (PredefinedGroup) genericCache.getFromCache(cacheName, predefinedGroupID);
		if (predefinedGroup == null)
		{
			logger.info("PredefinedGroup doesnot exist");
			return false;
		}

		PredefinedGroupDao predefinedGroupDao = new PredefinedGroupDao();
		predefinedGroupDao.removePredefinedGroup(predefinedGroup);
		genericCache.removeFromCache(cacheName, predefinedGroupID);
		return true;
	}
}
