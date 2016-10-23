package com.onmobile.apps.ringbacktones.service.dblayer.dao;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.ServiceProvider;
import com.onmobile.apps.ringbacktones.service.dblayer.bean.RbtSubscriber;

public class RbtSubscriberDao
{
	private Logger logger = Logger.getLogger(RbtSubscriberDao.class);
	private static RbtSubscriberDao rbtSubscriberDao = null;
	public static RbtSubscriberDao getInstance()
	{
		if (rbtSubscriberDao != null)
			return rbtSubscriberDao;
		synchronized (RbtSubscriberDao.class)
		{
			if (rbtSubscriberDao != null)
				return rbtSubscriberDao;
			rbtSubscriberDao = new RbtSubscriberDao();
			return rbtSubscriberDao;
		}
	}
	
	public RbtSubscriber getSubscriber(String msisdn)
	{
		RbtSubscriber rbtSubscriber = null;
		try
		{
			Session session = ServiceProvider.getDbService().openSession();
			Transaction transaction = session.beginTransaction();
			rbtSubscriber = (RbtSubscriber)session.get(RbtSubscriber.class, msisdn);
			transaction.commit();
			session.close();
			
		}
		catch(Exception e)
		{
			logger.error("Exception in getting RbtSubscriber object for msisdn "+msisdn, e);
		}
		return rbtSubscriber;
	}
	
	public RbtSubscriber updateSubscriber(RbtSubscriber rbtSubscriber)
	{
		try
		{
			Session session = ServiceProvider.getDbService().openSession();
			Transaction transaction = session.beginTransaction();
			session.save(rbtSubscriber);
			transaction.commit();
			session.close();
			
		}
		catch(Exception e)
		{
			logger.error("Exception in getting RbtSubscriber object for rbtSubscriber "+rbtSubscriber, e);
		}
		return rbtSubscriber;
	}
	
	
	
	
}
