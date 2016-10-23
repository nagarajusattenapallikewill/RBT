package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.dao.CosDetailsDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.CacheNamesEnum;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * A singleton class which is injected into the CacheManagerUtil class by spring
 * injection. Users cannot directly use this class to access the cache. They
 * should get the instance from CacheManagerUtil class.
 * 
 * @author bikash.panda
 */

public class CosDetailsCacheManager
{
	private static Logger logger = Logger.getLogger(SubscriptionClassCacheManager.class);

	private IGenericCache genericCache;

	private CosDetailsCacheManager(IGenericCache genericCache)
	{
		this.genericCache = genericCache;
	}

	/**
	 * @param cosID
	 * @return List<CosDetails>
	 */
	@SuppressWarnings("unchecked")
	public List<CosDetails> getCosDetails(String cosID)
	{
		logger.info("cosID" + cosID);

		List<CosDetails> cosDetailsList = null;
		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, cosID);
		if (object != null)
			cosDetailsList = (List<CosDetails>) object;

		return cosDetailsList;
	}

	/**
	 * @return List<CosDetails>
	 */
	public List<CosDetails> getAllCosDetails()
	{
		List<CosDetails> cosDetailsList = new ArrayList<CosDetails>();

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			if (object != null && object instanceof List<?>)
			{
				@SuppressWarnings("unchecked")
				List<CosDetails> list = (List<CosDetails>) object;
				cosDetailsList.addAll(list);
			}
			else
			{
				cosDetailsList.add((CosDetails) object);
			}
		}

		return cosDetailsList;
	}

	/**
	 * Add cosDetail to RBT_COS_DETAIL table and cache
	 * 
	 * @param cosDetails
	 * @return void
	 */
	public boolean addCosDetail(CosDetails cosDetails)
	{
		logger.info("cosDetails: " + cosDetails);

		if (cosDetails == null)
			return false;

		try
		{
			String cacheName = CacheNamesEnum.COS_CACHE.toString();
			Object object = genericCache.getFromCache(cacheName, cosDetails.getCosId());
			if (object == null)
			{
				CosDetailsDao cosDetailsDao = new CosDetailsDao();
				cosDetailsDao.insertCosDetails(cosDetails);

				Object objectList = genericCache.getFromCache(cacheName, cosDetails.getCosId());
				@SuppressWarnings("unchecked")
				List<CosDetails> cosDetailsList = (List<CosDetails>) objectList;
				if (cosDetailsList != null) {
					cosDetailsList = new ArrayList<CosDetails>(1);
				}
				cosDetailsList.add(cosDetails);
				genericCache.updateToCache(cacheName, cosDetails.getCosId(), cosDetailsList);
				return true;
			} else {
				return false;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public boolean updateCosDetail(CosDetails cosDetails)
	{
		logger.info("cosDetails: " + cosDetails);

		if (cosDetails == null)
			return false;

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, cosDetails.getCosId());
		try
		{
			if (object != null)
			{
				CosDetails tempCosDetails = null;
				if (object instanceof List<?>)
				{
					@SuppressWarnings("unchecked")
					List<CosDetails> cosDetailsList = (List<CosDetails>) object;
					if (cosDetailsList.size() != 0)
					{
						for (int i = 0; i < cosDetailsList.size(); i++)
						{
							tempCosDetails = cosDetailsList.get(i);
							if (tempCosDetails != null
									&& tempCosDetails.getCosId().equalsIgnoreCase(cosDetails.getCosId())
									&& tempCosDetails.getAccessMode().equalsIgnoreCase(cosDetails.getAccessMode())
									&& tempCosDetails.getPrepaidYes().equalsIgnoreCase(cosDetails.getPrepaidYes())
									&& tempCosDetails.getCircleId().equalsIgnoreCase(cosDetails.getCircleId()))
							{
								CosDetailsDao cosDetailsDao = new CosDetailsDao();
								cosDetailsDao.updateCosDetails(cosDetails);
								cosDetailsList.remove(i);
								cosDetailsList.add(i, cosDetails);
								genericCache.updateToCache(cacheName, cosDetails.getCosId(), cosDetailsList);
								return true;
							}
						}
					}
				}
				else
				{
					tempCosDetails = (CosDetails) object;
					if (tempCosDetails.getCosId().equalsIgnoreCase(cosDetails.getCosId())
							&& tempCosDetails.getAccessMode().equalsIgnoreCase(cosDetails.getAccessMode())
							&& tempCosDetails.getPrepaidYes().equalsIgnoreCase(cosDetails.getPrepaidYes())
							&& tempCosDetails.getCircleId().equalsIgnoreCase(cosDetails.getCircleId()))
					{
						CosDetailsDao cosDetailsDao = new CosDetailsDao();
						cosDetailsDao.updateCosDetails(cosDetails);

						genericCache.updateToCache(cacheName, cosDetails.getCosId(), cosDetails);
						return true;
					}
				}
			}
			else
			{
				logger.info("cosDetail does not exist in the Cache. Could'nt update CosDetail");
				return false;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return false;
	}

	/**
	 * remove CosDetail from RBT_COS_DETAIL table and cache
	 * 
	 * @param cosDetails
	 * @return
	 */
	public boolean removeCosDetail(CosDetails cosDetails)
	{
		logger.info("cosDetalis: " + cosDetails);

		if (cosDetails == null)
			return false;

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, cosDetails.getCosId());
		if (object != null)
		{
			if (object instanceof List<?>)
			{
				@SuppressWarnings("unchecked")
				List<CosDetails> cosDetailsList = (List<CosDetails>) object;

				try
				{
					for (CosDetails cosDetailsObj : cosDetailsList)
					{
						if (cosDetailsObj != null
								&& cosDetailsObj.getCosId().equalsIgnoreCase(cosDetails.getCosId())
								&& cosDetailsObj.getAccessMode().equalsIgnoreCase(cosDetails.getAccessMode())
								&& cosDetailsObj.getPrepaidYes().equalsIgnoreCase(cosDetails.getPrepaidYes())
								&& cosDetailsObj.getCircleId().equalsIgnoreCase(cosDetails.getCircleId()))
						{
							CosDetailsDao cosDetailsDao = new CosDetailsDao();
							cosDetailsDao.removeCosDetails(cosDetailsObj);
						}
					}

					genericCache.removeFromCache(cacheName, cosDetails.getCosId());
					return true;
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
					return false;
				}
			}
			else
			{
				try
				{
					CosDetails cosDetailsObj = (CosDetails) object;
					if (cosDetailsObj.getCosId().equalsIgnoreCase(cosDetails.getCosId())
							&& cosDetailsObj.getAccessMode().equalsIgnoreCase(cosDetails.getAccessMode())
							&& cosDetailsObj.getPrepaidYes().equalsIgnoreCase(cosDetails.getPrepaidYes())
							&& cosDetailsObj.getCircleId().equalsIgnoreCase(cosDetails.getCircleId()))
					{
						CosDetailsDao cosDetailsDao = new CosDetailsDao();
						cosDetailsDao.removeCosDetails(cosDetailsObj);
					}

					genericCache.removeFromCache(cacheName, cosDetails.getCosId());
					return true;
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
					return false;
				}
			}
		}
		else
			return false;
	}

	/**
	 * Get all CosDetails from RBT_COS_DETAIL table with given circleId and prepaid status
	 * 
	 * @param circleID 
	 * @param prepaidYes
	 * @return List<CosDetails>
	 */
	public List<CosDetails> getAllCosDetails(String circleID, String prepaidYes)
	{
		logger.info("circleID: " + circleID + ",  prepaidYes: " + prepaidYes);

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<CosDetails> cosList = new ArrayList<CosDetails>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			try
			{
				@SuppressWarnings("unchecked")
				List<CosDetails> list = (List<CosDetails>) object;
				if (list != null)
				{
					for (CosDetails cosDetails : list)
					{
						if ((cosDetails.getCircleId().equalsIgnoreCase(circleID)
								|| cosDetails.getCircleId().equalsIgnoreCase("ALL"))
								&& (cosDetails.getPrepaidYes().equalsIgnoreCase(prepaidYes)
										|| cosDetails.getPrepaidYes().equalsIgnoreCase("b")))
							cosList.add(cosDetails);
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		return cosList;
	}

	/**
	 * Get all active CosDetails from RBT_COS_DETAIL table with given circleId and prepaid status
	 * 
	 * @param circleID 
	 * @param prepaidYes
	 * @return List<CosDetails>
	 */
	public List<CosDetails> getAllActiveCosDetails(String circleID, String prepaidYes)
	{
		logger.info("circleID: " + circleID + ",  prepaidYes: " + prepaidYes);

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<CosDetails> cosList = new ArrayList<CosDetails>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			try
			{
				@SuppressWarnings("unchecked")
				List<CosDetails> list = (List<CosDetails>) object;
				if (list != null)
				{
					Date currentDate = new Date();
					for (CosDetails cosDetails : list)
					{
						if ((cosDetails.getCircleId().equalsIgnoreCase(circleID)
								|| cosDetails.getCircleId().equalsIgnoreCase("ALL"))
								&& (cosDetails.getPrepaidYes().equalsIgnoreCase(prepaidYes)
										|| cosDetails.getPrepaidYes().equalsIgnoreCase("b"))
										&& cosDetails.getEndDate() != null
										&& cosDetails.getEndDate().after(currentDate)
										&& cosDetails.getStartDate() != null
										&& cosDetails.getStartDate().before(currentDate))
							cosList.add(cosDetails);
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		return cosList;
	}

	/**
	 * Get CosDetail with given value of cosID
	 * 
	 * @param cosID
	 * @return CosDetails
	 */
	public CosDetails getCosDetail(String cosID)
	{
		return getCosDetail(cosID, null);
	}

	/**
	 * Get CosDetail with given value of cosID and circleID
	 * 
	 * @param cosID
	 * @param circleID
	 * @return CosDetails
	 */
	public CosDetails getCosDetail(String cosID, String circleID)
	{
		logger.info("cosID: " + cosID + ", circleID: " + circleID);

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<CosDetails> cosList = new ArrayList<CosDetails>();

		Object object = genericCache.getFromCache(cacheName, cosID);
		CosDetails retCosDetails  = null;
		if (object != null)
		{
			if (object instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<CosDetails> cosDetailsList = (List<CosDetails>) object;
				for (CosDetails cosDetails : cosDetailsList)
				{
					if (circleID != null)
					{
						if (cosDetails.getCircleId().equalsIgnoreCase(circleID)
								|| cosDetails.getCircleId().equalsIgnoreCase("ALL"))
							cosList.add(cosDetails);
					}
					else
					{
						cosList = cosDetailsList;
						break;
					}
	
				}
				if (cosList != null && cosList.size() > 0) {
					retCosDetails = cosList.get(0);
				}
			} else {
				retCosDetails = (CosDetails) object;
			}
		}
		return retCosDetails;
	}

	/**
	 * Get active CosDetail with given values of cosID and circleID
	 * 
	 * @param cosID
	 * @param circleID
	 * @return CosDetail
	 */
	public CosDetails getActiveCosDetail(String cosID, String circleID)
	{
		logger.info("cosID: " + cosID + ", circleID: " + circleID);

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<CosDetails> cosList = new ArrayList<CosDetails>();

		Object object = genericCache.getFromCache(cacheName, cosID);
		@SuppressWarnings("unchecked")
		List<CosDetails> cosDetailsList = (List<CosDetails>) object;
		if (cosDetailsList != null)
		{
			Date currentDate = new Date();
			for (CosDetails cosDetails : cosDetailsList)
			{
				if (circleID == null)
				{
					if (cosDetails.getEndDate() != null
							&& cosDetails.getEndDate().after(currentDate)
							&& cosDetails.getStartDate() != null
							&& cosDetails.getStartDate().before(currentDate))
						cosList.add(cosDetails);
				}
				else
				{
					if ((cosDetails.getCircleId() != null
							&& (cosDetails.getCircleId().equalsIgnoreCase(circleID)
									|| cosDetails.getCircleId().equalsIgnoreCase("ALL"))) 
									&& cosDetails.getEndDate() != null
									&& cosDetails.getEndDate().after(currentDate)
									&& cosDetails.getStartDate() != null
									&& cosDetails.getStartDate().before(currentDate))
						cosList.add(cosDetails);
				}
			}
		}

		if (cosList.size() > 0)
			return cosList.get(0);
		else
			return null;
	}

	/**
	 * Get all active CosDetail
	 * 
	 * @param
	 * @return List<CosDetails>
	 */
	public List<CosDetails> getCosForPromoUpdate()
	{
		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<CosDetails> cosList = new ArrayList<CosDetails>();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -1);
		Date date = calendar.getTime();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			@SuppressWarnings("unchecked")
			List<CosDetails> cosDetailsList = (List<CosDetails>) object;
			if (cosDetailsList != null)
			{
				Date currentDate = new Date();
				for (CosDetails cosDetails : cosDetailsList)
				{
					if (cosDetails.getEndDate() != null
							&& cosDetails.getEndDate().after(date)
							&& cosDetails.getEndDate() != null
							&& cosDetails.getEndDate().before(currentDate))
						cosList.add(cosDetails);
				}
			}
		}

		return cosList;
	}

	/**
	 * Get active CosDetail with given values of cosID and circleID
	 * 
	 * @param
	 * @return List<CosDetails>
	 */
	public List<CosDetails> getCosDetailsByCosType(String cosType, String circleID, String prepaidYes)
	{
		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<CosDetails> cosList = new ArrayList<CosDetails>();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -1);

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			@SuppressWarnings("unchecked")
			List<CosDetails> cosDetailsList = (List<CosDetails>) object;
			if (cosDetailsList != null)
			{
				for (CosDetails cosDetails : cosDetailsList)
				{
					if (cosDetails.getCosType() !=null
							&& cosDetails.getCosType().equalsIgnoreCase(cosType)
							&& (cosDetails.getCircleId() != null
									&& (cosDetails.getCircleId().equalsIgnoreCase(circleID)
											|| cosDetails.getCircleId().equals("ALL")))
											&& cosDetails.getPrepaidYes() != null
											&& (cosDetails.getPrepaidYes().equals(prepaidYes)
													|| cosDetails.getPrepaidYes().equals("b")))
						cosList.add(cosDetails);
				}
			}
		}

		return cosList;
	}

	/**
	 * @param circleID
	 * @param prepaidyes
	 * @return CosDetails
	 */
	public CosDetails getDefaultCosDetail(String circleID, String prepaidYes)
	{
		logger.info("circlID: " + circleID + ",  prepaidYes: " + prepaidYes);

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<CosDetails> cosList = new ArrayList<CosDetails>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			@SuppressWarnings("unchecked")
			List<CosDetails> cosDetailsList = (List<CosDetails>) object;
			if (cosDetailsList != null)
			{
				Date currentDate = new Date();
				for (CosDetails cosDetails : cosDetailsList)
				{
					if ((cosDetails.getCircleId() != null
							&& (cosDetails.getCircleId().equalsIgnoreCase(circleID)
									|| cosDetails.getCircleId().equals("ALL")))
									&& cosDetails.getPrepaidYes() != null
									&& (cosDetails.getPrepaidYes().equals(prepaidYes)
											|| cosDetails.getPrepaidYes().equals("b"))
											&& cosDetails.getEndDate() != null
											&& cosDetails.getEndDate().after(currentDate)
											&& cosDetails.getIsDefault() != null
											&& cosDetails.getIsDefault().equalsIgnoreCase("y"))
						cosList.add(cosDetails);
				}
			}
		}

		if (cosList.size() > 0)
			return cosList.get(0);
		else
			return null;
	}

	/**
	 * get cosDetails
	 * 
	 * @param smsKeyword
	 * @param circleID
	 * @param prepaidYes
	 * @return CosDetails
	 */
	public CosDetails getSmsKeywordCosDetail(String smsKeyword, String circleID, String prepaidYes)
	{
		logger.info("smsKeyword: " + smsKeyword + ", circleID: " + circleID + ", prepaidYes:" + prepaidYes);

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<CosDetails> cosList = new ArrayList<CosDetails>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			@SuppressWarnings("unchecked")
			List<CosDetails> cosDetailsList = (List<CosDetails>) object;
			if (cosDetailsList != null)
			{
				for (CosDetails cosDetails : cosDetailsList)
				{
					if (cosDetails.getCircleId() != null
							&& (cosDetails.getCircleId().equals(circleID)
									|| cosDetails.getCircleId().equals("ALL"))
									&& cosDetails.getPrepaidYes() != null
									&& (cosDetails.getPrepaidYes().equals(prepaidYes)
											|| cosDetails.getPrepaidYes().equals("b"))
											&& cosDetails.getSmsKeyword() != null
											&& cosDetails.getSmsKeyword().equalsIgnoreCase(smsKeyword))
						cosList.add(cosDetails);
				}
			}
		}

		if (cosList.size() > 0)
			return cosList.get(0);
		else
			return null;
	}

	/**
	 * Check whether WTPacks available
	 * 
	 * @param circleID
	 * @param prepaidYes
	 * @return boolean
	 */
	public boolean isWTPacksAvailable(String circleID, String prepaidYes)
	{
		logger.info("circleID: " + circleID + ", prepaidYes: " + prepaidYes);

		if (circleID == null)
			return false;

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);

		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			@SuppressWarnings("unchecked")
			List<CosDetails> cosDetailsList = (List<CosDetails>) object;
			if (cosDetailsList != null)
			{
				Date currentDate = new Date();
				for (CosDetails cosDetails : cosDetailsList)
				{
					if (cosDetails.getCircleId() != null
							&& (cosDetails.getCircleId().equalsIgnoreCase(circleID)
									|| cosDetails.getCircleId().equalsIgnoreCase("ALL"))
									&& cosDetails.getPrepaidYes() != null
									&& (cosDetails.getPrepaidYes().equalsIgnoreCase(prepaidYes)
											|| cosDetails.getPrepaidYes().equalsIgnoreCase("b"))
											&& cosDetails.getAccessMode() != null
											&& cosDetails.getAccessMode().equalsIgnoreCase("WTPACKS")
											&& cosDetails.getEndDate() != null
											&& cosDetails.getEndDate().after(currentDate)
											&& cosDetails.getStartDate() != null
											&& cosDetails.getStartDate().before(currentDate))
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Check whether TRIALPacks available
	 * 
	 * @param circleID
	 * @param prepaidYes
	 * @return boolean
	 */
	public boolean isTrialAvailable(String circleID, String prepaidYes)
	{
		logger.info("circleID: " + circleID + ", prepaidYes: " + prepaidYes);
		if (circleID == null)
			return false;

		String cacheName = CacheNamesEnum.COS_CACHE.toString();
		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			@SuppressWarnings("unchecked")
			List<CosDetails> cosDetailsList = (List<CosDetails>) object;
			if (cosDetailsList != null)
			{
				Date currentDate = new Date();
				for (CosDetails cosDetails : cosDetailsList)
				{
					if (cosDetails.getCircleId() != null
							&& (cosDetails.getCircleId().equalsIgnoreCase(circleID)
									|| cosDetails.getCircleId().equalsIgnoreCase("ALL"))
									&& cosDetails.getPrepaidYes() != null
									&& (cosDetails.getPrepaidYes().equalsIgnoreCase(prepaidYes)
											|| cosDetails.getPrepaidYes().equalsIgnoreCase("b"))
											&& cosDetails.getAccessMode() != null
											&& cosDetails.getAccessMode().equalsIgnoreCase("VUI,SMS")
											&& cosDetails.getEndDate() != null
											&& cosDetails.getEndDate().after(currentDate)
											&& cosDetails.getStartDate() != null
											&& cosDetails.getStartDate().before(currentDate)
											&& cosDetails.getIsDefault() != null
											&& cosDetails.getIsDefault().equalsIgnoreCase("n"))
					{
						return true;
					}

				}
			}
		}

		return false;
	}
}
