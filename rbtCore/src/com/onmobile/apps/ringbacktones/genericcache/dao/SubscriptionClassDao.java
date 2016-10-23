package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;

/**
 * SubscriptionClassDao has API's to persist data in RBT_SUBSCRIPTION_CLASS
 * table.
 * 
 * @author manish.shringarpure
 */
public class SubscriptionClassDao extends BaseDao
{
	private static Logger logger = Logger.getLogger(SubscriptionClassDao.class);

	/**
	 * @param subscriptionClass
	 */
	public void insertSubscriptionClass(SubscriptionClass subscriptionClass)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(subscriptionClass);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	/**
	 * @param subscriptionClass
	 */
	public void updateSubscriptionClass(SubscriptionClass subscriptionClass)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(subscriptionClass);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<SubscriptionClass> getAllSubscriptionClasses()
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(SubscriptionClass.class);
		@SuppressWarnings("unchecked")
		List<SubscriptionClass> subscriptionClassList = criteria.list();

		// get the circle wise subscription classes and return
		subscriptionClassList = getCirclewiseSubscriptionClasses(subscriptionClassList);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return subscriptionClassList;
	}

	/**
	 * @param subscriptionClass
	 */
	public void removeSubscriptionClass(SubscriptionClass subscriptionClass)
	{
		Transaction transaction = null;
		Session session = null;
		try
		{
			session = getHibernateSession();
			transaction = session.beginTransaction();

			session.delete(subscriptionClass);

			session.flush();
			transaction.commit();
		}
		finally
		{
			closeHibernateSession(session);
		}
	}

	/**
	 * @param subscriptionClass
	 */
	public SubscriptionClass getSubscriptionClass(String subscriptionClass)
	{
		Transaction transaction = null;
		Session session = null;
		SubscriptionClass subClass = null;
		try
		{
			session = getHibernateSession();
			transaction = session.beginTransaction();

			subClass = (SubscriptionClass) session.get(SubscriptionClass.class, subscriptionClass);

			session.flush();
			transaction.commit();
		}
		finally
		{
			closeHibernateSession(session);
		}

		return subClass;
	}

	/**
	 * @param subscriptionClassList
	 * @return
	 */
	private List<SubscriptionClass> getCirclewiseSubscriptionClasses(List<SubscriptionClass> subscriptionClassList)
	{
		List<SubscriptionClass> result = new ArrayList<SubscriptionClass>();
		for (SubscriptionClass subscriptionClass : subscriptionClassList)
		{
			String[] circleIDs = subscriptionClass.getCircleID().split(",");
			if (circleIDs != null)
			{
				for (String circleID : circleIDs)
				{
					try
					{
						SubscriptionClass tempSubClass = subscriptionClass.clone();
						tempSubClass.setCircleID(circleID);
						result.add(tempSubClass);
					}
					catch (Exception e)
					{
						logger.error(e.getMessage(), e);
					}
				}
			}
		}

		return result;
	}
}
