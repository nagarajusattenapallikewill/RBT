package com.onmobile.apps.ringbacktones.genericcache.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;

/**
 * All the DAO's should extend from the BaseDao. The BaseDao takes care of
 * creating a Hibernate s SessionFactory from the hibernate cfg file.
 * 
 * @author manish.shringarpure
 */
public class BaseDao
{
	/**
	 * Constructor which creates a Hibernate SessionFactory
	 */
	public BaseDao() {
	}

	/**
	 * @return SessionFactory
	 */
	protected SessionFactory getHibernateSessionFactory()
	{
		return HibernateUtil.getSessionFactory();
	}

	/**
	 * Returns a hibernate session.
	 * 
	 * @return Session
	 */
	protected Session getHibernateSession()
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		if (sessionFactory != null)
			return sessionFactory.openSession();

		return null;
	}

	protected void closeHibernateSession(Session session)
	{
		try 
		{
			session.close();	
		} 
		catch (Exception e) 
		{
		}
	}

	public static SessionFactory getSessionFactory() {
		return HibernateUtil.getSessionFactory();
	}
}
