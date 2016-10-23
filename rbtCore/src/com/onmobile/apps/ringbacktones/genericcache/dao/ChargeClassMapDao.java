package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassMap;

/**
 * CosDetailsDao has API's to persist data in RBT_CHARGE_CLASS_MAP table.
 * 
 * @author bikash.panda
 */
public class ChargeClassMapDao extends BaseDao
{
	public void insertChargeClassMap(ChargeClassMap ccm)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(ccm);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void updateChargeClassMap(ChargeClassMap ccm)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(ccm);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<ChargeClassMap> getAllChargeClassMap()
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(ChargeClassMap.class);
		@SuppressWarnings("unchecked")
		List<ChargeClassMap> ccmList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return ccmList;
	}

	public void removeChargeClassMap(ChargeClassMap chargeClassMap)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.delete(chargeClassMap);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}
}
