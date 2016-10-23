package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUpdate;

public class RBTSocialUpdateDao extends BaseDao {
	
	public void insertRBTSocialUpdate(RBTSocialUpdate update)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(update);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void updateRBTSocialUpdate(RBTSocialUpdate update)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(update);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<RBTSocialUpdate> getAllRBTSocialUpdate()
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(RBTSocialUpdate.class);
		@SuppressWarnings("unchecked")
		List<RBTSocialUpdate> updateList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return updateList;
	}

	public void removeRBTSocialUpdate(RBTSocialUpdate update)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.delete(update);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

}
