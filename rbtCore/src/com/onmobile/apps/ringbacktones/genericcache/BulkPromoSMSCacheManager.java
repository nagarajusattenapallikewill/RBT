package com.onmobile.apps.ringbacktones.genericcache;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.dao.BulkPromoDao;
import com.onmobile.apps.ringbacktones.genericcache.dao.BulkPromoSMSDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.CacheNamesEnum;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * A singleton class which is injected into the CacheManagerUtil class by spring
 * injection. Users cannot directly use this class to access the cache. They should
 * get the instance from CacheManagerUtil class.
 * 
 * @author bikash.panda
 */

public class BulkPromoSMSCacheManager
{
	private static Logger logger = Logger.getLogger(SubscriptionClassCacheManager.class);

	private IGenericCache genericCache;

	private BulkPromoSMSCacheManager(IGenericCache genericCache)
	{
		this.genericCache = genericCache;
	}

	/**
	 * @param promoID
	 * @param smsDate
	 * @return BulkPromoSMS
	 */
	public BulkPromoSMS getBulkPromoSMS(String promoID, String smsDate)
	{
		logger.info("promoId: " + promoID + ", smsDate: " + smsDate);

		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, promoID + ":" + smsDate);
		BulkPromoSMS bulkPromoSMS = (BulkPromoSMS) object;

		logger.info("bulkPromoSMS: " + bulkPromoSMS);
		return bulkPromoSMS;
	}

	/**
	 * @param
	 * @return List< BulkPromoSMS>
	 */
	public List<BulkPromoSMS> getAllBulkPromoSMS()
	{
		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		List<BulkPromoSMS>  smsList = new ArrayList<BulkPromoSMS>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			BulkPromoSMS bulkPromoSMS = (BulkPromoSMS) object;
			smsList.add(bulkPromoSMS);
		}

		return smsList;
	}

	/**
	 * Add to RBT_BULK_PROMO_SMS table
	 * 
	 * @param promoID
	 * @param smsDate
	 * @param smsText
	 * @param smsSent
	 * @return
	 */
	public boolean addBulkPromoSmS(String promoID, String smsDate, String smsText, String smsSent)
	{
		boolean added = false;
		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		try
		{
			if (smsDate == null || smsDate.length() == 0)
				smsDate = " ";
			logger.info("promoID: " + promoID + ", smsDate:" + smsDate + ", smsText:" + smsText + ", smsSent:" + smsSent);

			Object object = genericCache.getFromCache(cacheName, promoID + ":" + smsDate);
			if (object == null)
			{
				// checking for empty string
				object = genericCache.getFromCache(cacheName, promoID + ":");
				if (object == null)
				{
					BulkPromoSMS bulkPromoSMS = new BulkPromoSMS(promoID, smsDate, smsText, smsSent);

					BulkPromoSMSDao bulkPromoSMSDao = new BulkPromoSMSDao();
					bulkPromoSMSDao.insertBulkPromoSMS(bulkPromoSMS);

					genericCache.updateToCache(cacheName, promoID + ":" + smsDate, bulkPromoSMS);

					added = true;
					logger.info("Bulk Promosms added.");
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			added = false;
		}

		return added;
	}

	/**
	 * update RBT_BULK_PROMO_SMS table with given values
	 * 
	 * @param promoID
	 * @param smsDate
	 * @param smsText
	 * @param smsSent
	 * @return
	 */
	public boolean update(String promoID, String smsDate, String smsText, String smsSent)
	{
		boolean updated = false;
		if (smsDate == null || smsDate.length() == 0)
			smsDate = " ";
		logger.info("promoID: " + promoID + ", smsDate:" + smsDate + ", smsText:" + smsText + ", smsSent:" + smsSent);

		if (promoID == null)
			return false;

		BulkPromoSMS bulkPromoSMS = new BulkPromoSMS(promoID, smsDate, smsText, smsSent);
		if (!smsDate.trim().equalsIgnoreCase("null"))
			bulkPromoSMS.setSmsDate(smsDate);
		else
			bulkPromoSMS.setSmsDate(null);

		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, promoID + ":" + smsDate);
		logger.info("update BulkPromoSMS class object: " + object);
		if (object != null)
		{
			if (object instanceof BulkPromoSMS)
			{
				BulkPromoSMSDao bulkPromoSMSDao = new BulkPromoSMSDao();
				bulkPromoSMSDao.updateBulkPromoSMS(bulkPromoSMS);

				genericCache.updateToCache(cacheName, promoID + ":" + smsDate, bulkPromoSMS);
				updated = true;
			}
		}
		else
		{
			updated = false;
			logger.info("The BulkPromoSMS does not exist in the Cache. Could'nt update BulkPromoSMS");
		}

		return updated;
	}

	/**
	 * Add to RBT_BULK_PROMO_SMS table
	 * 
	 * @param promoID
	 * @param smsDay
	 * @param smsSent
	 * @return
	 */
	public void updateSMSSent(String promoID, int smsDay, String smsSent)
	{
		logger.info("promoID: " + promoID + ", smsDay: " + smsDay + ", smsSent: " + smsSent);

		BulkPromoDao bulkPromo = new BulkPromoDao();
		Date promoStartDate = bulkPromo.getBulkPromoStartDate(promoID);

		String smsDate = "";
		if (smsDay == -1)
			smsDate = "Welcome";
		else if (smsDay == -2)
			smsDate = "Termination";
		else
		{

			if (promoStartDate != null)
			{
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				Date smsdate = addDaysToDate(promoStartDate, smsDay - 1);
				smsDate = dateFormat.format(smsdate);
			}

			logger.info("smsDate : " + smsDate);
		}

		if (promoID == null || smsDate == null)
			return;

		BulkPromoSMS bulkPromoSMS = new BulkPromoSMS(promoID, smsDate, null, smsSent);

		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, promoID + ":" + smsDate);

		logger.info("update bulkpromo class object: " + object);
		if (object != null)
		{
			if (object instanceof BulkPromoSMS)
			{
				BulkPromoSMS tempBulkPromoSMS = (BulkPromoSMS) object;
				bulkPromoSMS.setSmsText(tempBulkPromoSMS.getSmsText());

				BulkPromoSMSDao bulkPromoSMSDao = new BulkPromoSMSDao();
				bulkPromoSMSDao.updateBulkPromoSMS(bulkPromoSMS);

				genericCache.updateToCache(cacheName, promoID + ":" + smsDate, bulkPromoSMS);
			}
		}
		else
		{
			logger.info("The BulkPromoSMS does not exist in the Cache. Could'nt update BulkPromoSMS");
		}
	}

	/**
	 * Update smsSent field in RBT_BULK_PROMO_SMS table
	 * 
	 * @param promoID
	 * @param smsDay
	 * @param smsSent
	 * @return boolean
	 */
	public boolean updateSMSSent(String promoID, String smsDay, String smsSent)
	{
		logger.info("promoID: " + promoID + ", smsDay: " + smsDay + ", smsSent: " + smsSent);

		String smsDate = smsDay;

		if (promoID == null || smsDate == null)
			return false;

		BulkPromoSMS bulkPromoSMS = new BulkPromoSMS(promoID, smsDate, null, smsSent);

		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, promoID + ":" + smsDate);

		logger.info("update BulkPromoSMS object: " + object);
		if (object != null)
		{
			if (object instanceof BulkPromoSMS)
			{
				BulkPromoSMS tempBulkPromoSMS = (BulkPromoSMS) object;
				bulkPromoSMS.setSmsText(tempBulkPromoSMS.getSmsText());

				BulkPromoSMSDao bulkPromoSMSDao = new BulkPromoSMSDao();
				bulkPromoSMSDao.updateBulkPromoSMS(bulkPromoSMS);

				genericCache.updateToCache(cacheName, promoID + ":" + smsDate, bulkPromoSMS);
			}
		}
		else
		{
			logger.info("The BulkPromoSMS does not exist in the Cache. Could'nt update BulkPromoSMS");
		}
		return true;
	}

	/**
	 * Get BulkPromoSMS
	 * 
	 * @param smsDate
	 * @return List<BulkPromoSMS>
	 */
	public List<BulkPromoSMS> getBulkPromoSMSForDate(Date smsDate)
	{
		if (smsDate == null)
			smsDate = new Date();
		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		String smsDateString = format.format(smsDate);

		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		List<BulkPromoSMS> smsList = new ArrayList<BulkPromoSMS>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			BulkPromoSMS bulkPromoSMS = (BulkPromoSMS) object;
			if (bulkPromoSMS.getSmsDate().equalsIgnoreCase(smsDateString)
					&& bulkPromoSMS.getSmsSent().equalsIgnoreCase("n"))
			{
				smsList.add(bulkPromoSMS);
			}
		}

		return smsList;
	}

	/**
	 * Get BulkPromoSMS
	 * 
	 * @param promoID
	 * @param smsDay
	 * @return BulkPromoSMS
	 */
	public BulkPromoSMS getBulkPromoSMS(String promoID, int smsDay)
	{
		String smsDate = null;
		BulkPromoDao bulkPromo = new BulkPromoDao();
		Date promoStartDate = bulkPromo.getBulkPromoStartDate(promoID);

		if (smsDay == -1)
			smsDate = "Welcome";
		else if (smsDay == -2)
			smsDate = "Termination";
		else
		{
			if (promoStartDate != null)
			{
				DateFormat format = new SimpleDateFormat("yyyyMMdd");
				Date smsdate = addDaysToDate(promoStartDate, smsDay - 1);
				smsDate = format.format(smsdate);
			}

			logger.info("updateSMSSent start smsDate : " + smsDate);
		}
		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, promoID + ":" + smsDate);
		BulkPromoSMS bulkPromoSMS = (BulkPromoSMS) object;
		return bulkPromoSMS;
	}

	/**
	 * Get BulkPromoSMS for a given promoID
	 * 
	 * @param promoID
	 * @return List<BulkPromoSMS>
	 */
	public List<BulkPromoSMS> getAllPromoIDSMSes(String promoID)
	{
		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		List<BulkPromoSMS> smsList = new ArrayList<BulkPromoSMS>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			if (object != null)
			{
				BulkPromoSMS bulkPromoSMS = (BulkPromoSMS) object;
				if (bulkPromoSMS.getBulkpromoID().equalsIgnoreCase(promoID))
					smsList.add(bulkPromoSMS);
			}
		}

		return smsList;
	}

	/**
	 * Get BulkPromoSMS order by promoID
	 * 
	 * @param
	 * @return List<BulkPromoSMS>
	 */
	public List<BulkPromoSMS> getBulkPromoSMSes()
	{
		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		List<BulkPromoSMS> smsList = new ArrayList<BulkPromoSMS>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			BulkPromoSMS bulkPromoSMS = (BulkPromoSMS) object;
			smsList.add(bulkPromoSMS);
		}

		ComparatorUtil comparatorUtil = new ComparatorUtil();
		smsList = comparatorUtil.sortByPromoID(smsList, true);

		return smsList;
	}

	/**
	 * Get BulkPromoSMS for a given promoID
	 * 
	 * @param promoID
	 * @return List<BulkPromoSMS>
	 */
	public List<BulkPromoSMS> getBulkPromoSMSes(String promoID)
	{
		return getAllPromoIDSMSes(promoID);
	}

	/**
	 * Get BulkPromoSMS for a given promoId and smsDate
	 * 
	 * @param promoID
	 * @param smsDate
	 * @return BulkPromoSMS
	 */
	public BulkPromoSMS getBulkPromoSMSForDate(String bulkPromoID, String smsDate)
	{
		if (smsDate == null || smsDate.length() == 0)
			smsDate = " ";

		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			BulkPromoSMS bulkPromoSMS = (BulkPromoSMS) object;
			if (bulkPromoSMS.getBulkpromoID().equalsIgnoreCase(bulkPromoID)
					&& (bulkPromoSMS.getSmsDate() == null || bulkPromoSMS.getSmsDate().equals(smsDate)))
			{
				bulkPromoSMS.setSmsSent("y");
				return bulkPromoSMS;
			}
		}

		return null;
	}

	public boolean removeBulkPromoSMS(String promoID, String smsDate)
	{
		if (smsDate == null || smsDate.length() == 0)
			smsDate = " ";

		logger.info("promoID: " + promoID + ", smsDate : " + smsDate);

		String cacheName = CacheNamesEnum.BULK_PROMO_SMS_CACHE.toString();
		BulkPromoSMS bulkPromoSMSObject = (BulkPromoSMS) genericCache.getFromCache(cacheName, promoID + ":" + smsDate);
		if (bulkPromoSMSObject == null)
			return false;
		try
		{
			BulkPromoSMSDao bulkPromoSMSDao = new BulkPromoSMSDao();
			bulkPromoSMSDao.removeBulkPromoSMS(bulkPromoSMSObject);

			genericCache.removeFromCache(cacheName, promoID + ":" + smsDate);

			logger.info("BulkPromoSMS removed successfully");
			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	private static Date addDaysToDate(Date oldDate, int days)
	{
		Calendar cal = Calendar.getInstance();

		cal.setTime(oldDate);

		cal.add(Calendar.DATE, days);

		Date newDate = cal.getTime();

		return newDate;
	}
}
