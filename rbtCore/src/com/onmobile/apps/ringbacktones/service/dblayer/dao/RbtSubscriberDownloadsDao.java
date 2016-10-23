package com.onmobile.apps.ringbacktones.service.dblayer.dao;

public class RbtSubscriberDownloadsDao
{
	private static RbtSubscriberDownloadsDao rbtSubscriberDownloadsDao = null;
	public static RbtSubscriberDownloadsDao getInstance()
	{
		if (rbtSubscriberDownloadsDao != null)
			return rbtSubscriberDownloadsDao;
		synchronized (RbtSubscriberDownloadsDao.class)
		{
			if (rbtSubscriberDownloadsDao != null)
				return rbtSubscriberDownloadsDao;
			rbtSubscriberDownloadsDao = new RbtSubscriberDownloadsDao();
			return rbtSubscriberDownloadsDao;
		}
	}
	
}
