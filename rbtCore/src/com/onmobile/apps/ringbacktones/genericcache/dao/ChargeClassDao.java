package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;

/**
 * ChargeclassDao has API's to persist data in RBT_CHARGE_CLASS table.
 * 
 * @author bikash.panda
 */
public class ChargeClassDao extends BaseDao
{
	public void insertChargeClass(ChargeClass chargeClass)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(chargeClass);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void updateChargeClass(ChargeClass chargeClass)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(chargeClass);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<ChargeClass> getAllChargeClass()
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(ChargeClass.class);
		@SuppressWarnings("unchecked")
		List<ChargeClass> chargeList = criteria.list();
		chargeList = getCirclewiseChargeClasses(chargeList);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return chargeList;
	}

	/**
	 * @param subscriptionClass
	 */
	public ChargeClass getChargeClass(String chargeclass)
	{
		Transaction transaction = null;
		Session session = null;
		ChargeClass chargeClass = null;

		try
		{
			session = getHibernateSession();
			transaction = session.beginTransaction();
			chargeClass = (ChargeClass) session.get(ChargeClass.class, chargeclass);

			session.flush();
			transaction.commit();
		}
		finally
		{
			closeHibernateSession(session);
		}

		return chargeClass;
	}

	public void removeChargeClass(ChargeClass chargeclass)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.delete(chargeclass);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	/**
	 * @param ChargeClassList
	 * @return
	 */
	private List<ChargeClass> getCirclewiseChargeClasses(List<ChargeClass> chargeClassList)
	{
		List<ChargeClass> result = new ArrayList<ChargeClass>();

		for (ChargeClass chargeClass : chargeClassList)
		{
			String[] circleIDs = chargeClass.getCircleID().split(",");
			if (circleIDs != null)
			{
				for (String circleID : circleIDs)
				{
					try
					{
						ChargeClass tempChargeClass = chargeClass.clone();

						tempChargeClass.setCircleID(circleID);
						result.add(tempChargeClass);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		return result;
	}
}
