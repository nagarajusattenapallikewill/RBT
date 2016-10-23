package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromo;

/**
 * CosDetailsDao has API's to persist data in RBT_BULK_PROMO_SMS table.
 * 
 * @author bikash.panda
 */
public class BulkPromoDao extends BaseDao
{
	public List<BulkPromo> getAllBulkPromo()
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(BulkPromo.class);
		@SuppressWarnings("unchecked")
		List<BulkPromo> smsList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return smsList;
	}

	public Date getBulkPromoStartDate(String promoID)
	{
		List<BulkPromo> smsList = getAllBulkPromo();
		for (BulkPromo bulkPromo : smsList)
		{
			if (bulkPromo.getBulkPromoID().equalsIgnoreCase(promoID))
				return bulkPromo.getSmsStartDate();
		}

		return null;
	}
}
