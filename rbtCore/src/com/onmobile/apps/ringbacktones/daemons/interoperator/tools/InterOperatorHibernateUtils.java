package com.onmobile.apps.ringbacktones.daemons.interoperator.tools;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class InterOperatorHibernateUtils
{

	private static SessionFactory sessionFactory;
	private static Logger logger = Logger.getLogger(InterOperatorHibernateUtils.class); 
	
	static
	{
		try
		{
			sessionFactory = new Configuration().configure("interoperator.xml").buildSessionFactory();
		}
		catch (Throwable ex)
		{
			logger.error("Initial SessionFactory creation failed." , ex);
		}
	}
	
	public static SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

}