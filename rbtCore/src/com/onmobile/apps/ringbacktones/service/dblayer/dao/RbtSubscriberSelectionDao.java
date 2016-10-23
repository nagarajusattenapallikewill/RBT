package com.onmobile.apps.ringbacktones.service.dblayer.dao;

public class RbtSubscriberSelectionDao
{
	private static RbtSubscriberSelectionDao rbtSubscriberSelectionDao = null;
	public static RbtSubscriberSelectionDao getInstance()
	{
		if (rbtSubscriberSelectionDao != null)
			return rbtSubscriberSelectionDao;
		synchronized (RbtSubscriberSelectionDao.class)
		{
			if (rbtSubscriberSelectionDao != null)
				return rbtSubscriberSelectionDao;
			rbtSubscriberSelectionDao = new RbtSubscriberSelectionDao();
			return rbtSubscriberSelectionDao;
		}
	}
}
