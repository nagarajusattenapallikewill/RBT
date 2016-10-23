package com.onmobile.apps.ringbacktones.service.dblayer.dao;

public class RbtViralSmsDao
{
	private static RbtViralSmsDao rbtViralSmsDao = null;
	public static RbtViralSmsDao getInstance()
	{
		if (rbtViralSmsDao != null)
			return rbtViralSmsDao;
		synchronized (RbtViralSmsDao.class)
		{
			if (rbtViralSmsDao != null)
				return rbtViralSmsDao;
			rbtViralSmsDao = new RbtViralSmsDao();
			return rbtViralSmsDao;
		}
	}
}
