package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;

/**
 * ChargeclassDao has API's to persist data in RBT_SITE_PREFIX table.
 * 
 * @author bikash.panda
 */
public class SitePrefixDao extends BaseDao
{
	public void insertSitePrefix(SitePrefix sitePrefix) throws Exception
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(sitePrefix);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void updateSitePrefix(SitePrefix sitePrefix) throws Exception
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(sitePrefix);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<SitePrefix> getAllSitePrefixes()
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(SitePrefix.class);
		@SuppressWarnings("unchecked")
		List<SitePrefix> prefixList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return prefixList;
	}

	public void removeSitePrefix(SitePrefix sitePrefix) throws Exception
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.delete(sitePrefix);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}
}
