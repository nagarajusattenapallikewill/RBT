package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;

/**
 * CosDetailsDao has API's to persist data in RBT_COS_DETAILS table.
 * 
 * @author bikash.panda
 */
public class CosDetailsDao extends BaseDao
{
	public void insertCosDetails(CosDetails cos)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(cos);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void updateCosDetails(CosDetails cos)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(cos);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<CosDetails> getAllCosDetails()
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(CosDetails.class);
		@SuppressWarnings("unchecked")
		List<CosDetails> cosList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return cosList;
	}

	public void removeCosDetails(CosDetails cos)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.delete(cos);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}
}
