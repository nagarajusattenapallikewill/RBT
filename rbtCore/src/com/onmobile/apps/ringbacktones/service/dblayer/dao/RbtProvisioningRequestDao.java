package com.onmobile.apps.ringbacktones.service.dblayer.dao;

public class RbtProvisioningRequestDao
{
	private static RbtProvisioningRequestDao rbtProvisioningRequestDao = null;
	
	public static RbtProvisioningRequestDao getInstance()
	{
		if (rbtProvisioningRequestDao != null)
			return rbtProvisioningRequestDao;
		synchronized (RbtProvisioningRequestDao.class)
		{
			if (rbtProvisioningRequestDao != null)
				return rbtProvisioningRequestDao;
			rbtProvisioningRequestDao = new RbtProvisioningRequestDao();
			return rbtProvisioningRequestDao;
		}
	}
}
