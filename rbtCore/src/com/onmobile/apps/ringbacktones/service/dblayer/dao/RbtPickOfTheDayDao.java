package com.onmobile.apps.ringbacktones.service.dblayer.dao;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.ServiceProvider;
import com.onmobile.apps.ringbacktones.service.dblayer.bean.RbtPickOfTheDay;
import com.onmobile.apps.ringbacktones.service.dblayer.bean.primaryKey.PickOfDayPK;

public class RbtPickOfTheDayDao
{
	private Logger logger = Logger.getLogger(RbtPickOfTheDayDao.class);
	private static RbtPickOfTheDayDao rbtPickOfTheDayDao = null;
	
	public static RbtPickOfTheDayDao getInstance()
	{
		if (rbtPickOfTheDayDao != null)
			return rbtPickOfTheDayDao;
		synchronized (RbtPickOfTheDayDao.class)
		{
			if (rbtPickOfTheDayDao != null)
				return rbtPickOfTheDayDao;
			rbtPickOfTheDayDao = new RbtPickOfTheDayDao();
			return rbtPickOfTheDayDao;
		}
	}
	
	public RbtPickOfTheDay getPickOfDay(String playDate, String circleId)
	{
		RbtPickOfTheDay rbtPickOfTheDay = null;
		try
		{
			Session session = ServiceProvider.getDbService().openSession();
			Transaction transaction = session.beginTransaction();
			PickOfDayPK pickOfDayPK = new PickOfDayPK();
			pickOfDayPK.setCircleId(circleId);
			pickOfDayPK.setPlayDate(playDate);
			rbtPickOfTheDay = (RbtPickOfTheDay)session.get(RbtPickOfTheDay.class, pickOfDayPK);
			transaction.commit();
			session.close();
			
		}
		catch(Exception e)
		{
			logger.error("Exception in getting RbtPickOfTheDay object for playDate="+playDate+", circleId="+circleId, e);
		}
		return rbtPickOfTheDay;
	}

}
