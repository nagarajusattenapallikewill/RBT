package com.onmobile.apps.ringbacktones.v2.dao.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;

public class HibernateUtil {
	
	private static Logger logger = Logger.getLogger(HibernateUtil.class);
	private static final SessionFactory sessionFactory;
	private static SessionFactory d2cMigrationSessionFactory;
	private static ResourceBundle resourceBundle = null;

	static
	{
		try
		{
			resourceBundle = ResourceBundle.getBundle("rbt");
			String dbURL = resourceBundle.getString("DB_URL");
			// Changes done for URL Encryption and Decryption
			try {
				if (resourceBundle.getString("ENCRYPTION_MODEL") != null
						&& resourceBundle.getString("ENCRYPTION_MODEL")
								.equalsIgnoreCase("yes")) {
					dbURL = URLEncryptDecryptUtil.decryptAndMerge(dbURL);
				}
			} catch (MissingResourceException e) {
				logger.error("resource bundle exception: ENCRYPTION_MODEL");
			}
			// End of URL Encryption and Decryption
			logger.debug("dbURL: " + dbURL);
			dbURL = dbURL.replaceAll("amp;", "");
			dbURL = validateDBProtocol(dbURL);
			logger.debug("dbURL: " + dbURL);

			// Create the SessionFactory from hibernate.cfg.xml
			AnnotationConfiguration config = new AnnotationConfiguration();
			sessionFactory = config.configure().buildSessionFactory();
			try {
				logger.debug("Initilizing MIGRACTION_DB Hibernate session");
				String MIGRACTION_DB_URL = resourceBundle.getString("MIGRACTION_DB_URL");
				logger.debug("MIGRACTION_DB_URL: " + MIGRACTION_DB_URL);
				MIGRACTION_DB_URL = MIGRACTION_DB_URL.replaceAll("amp;", "");
				MIGRACTION_DB_URL = validateDBProtocol(MIGRACTION_DB_URL);
				logger.debug("MIGRACTION_DB_URL: " + MIGRACTION_DB_URL);
				System.setProperty("MIGRACTION_DB_URL", MIGRACTION_DB_URL);
				AnnotationConfiguration migrationConfig = new AnnotationConfiguration();
				d2cMigrationSessionFactory = migrationConfig.configure("d2cmigration.hibernate.cfg.xml")
						.buildSessionFactory();
			} catch (Throwable e) {
				logger.error("MIGRACTION_DB Initial SessionFactory creation failed." + e.getMessage());
			}
		}
		catch (Throwable e)
		{
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed." + e);
			logger.fatal(e.getMessage(), e);
			throw new ExceptionInInitializerError(e);
		}
	}

	public static SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

	public static Session getSession()
	{
		return getSessionFactory().openSession();
	}
	
	
	public static SessionFactory getMigrationSessionFactory()
	{
		return d2cMigrationSessionFactory;
	}

	public static Session getMigrationSession()
	{
		return getMigrationSessionFactory().openSession();
	}
	
	private static String validateDBProtocol(String dbURL)
	{
		if (!dbURL.startsWith("jdbc:"))
		{
			String dbType = getString("DB_TYPE", "MYSQL");
			if (dbType.equalsIgnoreCase("SAPDB"))
				dbURL = "jdbc:sapdb://" + dbURL;
			else
				dbURL = "jdbc:mysql://" + dbURL;
		}

		return dbURL;
	}

	private static String getString(String key,String defaultValue) {
		
		try
		{
			return resourceBundle.getString(key);
		}
		catch (MissingResourceException e)
		{
			if (defaultValue == null)
				logger.error(e.getMessage());
			else
				logger.warn(e.getMessage() + " Returning default value: "
						+ defaultValue);
		}

		return defaultValue;
		
	}

	
	
}
