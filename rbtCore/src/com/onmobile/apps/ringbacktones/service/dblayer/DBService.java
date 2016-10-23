package com.onmobile.apps.ringbacktones.service.dblayer;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtPickOfTheDayDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtProvisioningRequestDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtSubscriberDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtSubscriberDownloadsDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtSubscriberSelectionDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtViralSmsDao;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;

public class DBService
{
	private static SessionFactory sessionFactory;
	private static Object isInitialized = null;
	private static Logger logger = Logger.getLogger(DBService.class);
	
	
	public Session openSession()
	{
		if(sessionFactory != null)
			return sessionFactory.openSession();
		synchronized (DBService.class)
		{
			if(sessionFactory != null)
				return sessionFactory.openSession();
			initialize();
			if(sessionFactory != null)
				return sessionFactory.openSession();
		}
		return null;
	}
	
	private void initialize()
	{
		try
		{
			if(isInitialized != null)
				return ;
			isInitialized = new Object();
			System.setProperty("RBT_DB_URL", getDBUrl());
			sessionFactory = new Configuration().configure("/refactorHibernate.cfg.xml").buildSessionFactory();
		}
		catch(Exception e)
		{
			logger.error("Exception in Hibernate sessionFactory initialization.", e);
		}
		
	}
	
	private static String getDBUrl()
	{
		ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
		String dbURL = resourceBundle.getString("DB_URL").replaceAll("amp;", "");
		// Changes done for URL Encryption and Decryption
		try {
			if (resourceBundle.getString("ENCRYPTION_MODEL") != null
					&& resourceBundle.getString("ENCRYPTION_MODEL")
							.equalsIgnoreCase("yes")) {
				dbURL = URLEncryptDecryptUtil.decryptAndMerge(resourceBundle
						.getString("DB_URL"));
			}
		} catch (MissingResourceException e) {
			logger.error("resource bundle exception: ENCRYPTION_MODEL");
		}
		// End of URL Encryption and Decryption
		if (!dbURL.startsWith("jdbc:"))
		{
			String dbType = resourceBundle.getString("DB_TYPE");
			if (dbType.equalsIgnoreCase("SAPDB"))
				dbURL = "jdbc:sapdb://" + dbURL;
			else
				dbURL = "jdbc:mysql://" + dbURL;
		}
		return dbURL;
	}
	
	public RbtPickOfTheDayDao getRBTPickOfTheDayDao()
	{
		return RbtPickOfTheDayDao.getInstance();
	}
	
	public RbtProvisioningRequestDao getRbtProvisioningRequestDao()
	{
		return RbtProvisioningRequestDao.getInstance();
	}
	
	public RbtSubscriberDao getRbtSubscriberDao()
	{
		return RbtSubscriberDao.getInstance();
	}
	
	public RbtSubscriberDownloadsDao getRbtSubscriberDownloadsDao()
	{
		return RbtSubscriberDownloadsDao.getInstance();
	}
	
	public RbtSubscriberSelectionDao getRbtSubscriberSelectionDao()
	{
		return RbtSubscriberSelectionDao.getInstance();
	}
	
	public RbtViralSmsDao getRbtViralSmsDao()
	{
		return RbtViralSmsDao.getInstance();
	}
	
}
