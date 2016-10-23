package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorHibernateUtils
{
	private static SessionFactory sessionFactory;
	private static Logger logger = Logger.getLogger(ContentInterOperatorHibernateUtils.class); 

	static
	{
		try
		{
			sessionFactory = new Configuration().configure("interoperator.xml").buildSessionFactory();
		}
		catch (Throwable ex)
		{
			logger.error("Initializing SessionFactory failed" , ex);
		}
	}

	/**
	 * @return the sessionFactory
	 */
	public static SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}
}
