package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;

/**
 * BulkPromoSMSDao has API's to persist data in RBT_BULK_PROMO_SMS table.
 * 
 * @author bikash.panda
 */
public class BulkPromoSMSDao extends BaseDao
{
	public void insertBulkPromoSMS(BulkPromoSMS sms)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(sms);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void updateBulkPromoSMS(BulkPromoSMS sms)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(sms);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<BulkPromoSMS> getAllBulkPromoSMS()
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(BulkPromoSMS.class);
		@SuppressWarnings("unchecked")
		List<BulkPromoSMS> smsList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return smsList;
	}

	public void removeBulkPromoSMS(BulkPromoSMS sms)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.delete(sms);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}
}
