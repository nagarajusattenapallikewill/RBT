/**
 * 
 */
package com.onmobile.apps.ringbacktones.common.hibernate;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import com.onmobile.apps.ringbacktones.common.ResourceReader;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;

/**
 * @author vinayasimha.patil
 * 
 */
public class HibernateUtil
{
	private static Logger logger = Logger.getLogger(HibernateUtil.class);
	private static final SessionFactory sessionFactory;

	static
	{
		try
		{
			ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
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
			dbURL = dbURL.replaceAll("amp;", "");
			dbURL = validateDBProtocol(dbURL);
			logger.debug("dbURL: " + dbURL);
			logger.debug("dbURL: " + dbURL);

			System.setProperty("RBT_DB_URL", dbURL);

			// Create the SessionFactory from hibernate.cfg.xml
			AnnotationConfiguration config = new AnnotationConfiguration();
			sessionFactory = config.configure().buildSessionFactory();
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
	
	private static String validateDBProtocol(String dbURL)
	{
		if (!dbURL.startsWith("jdbc:"))
		{
			String dbType = ResourceReader.getString("rbt", "DB_TYPE", "MYSQL");
			if (dbType.equalsIgnoreCase("SAPDB"))
				dbURL = "jdbc:sapdb://" + dbURL;
			else
				dbURL = "jdbc:mysql://" + dbURL;
		}

		return dbURL;
	}
}
