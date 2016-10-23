/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;

/**
 * RBTTextDao has API's to persist data in RBT_TEXT table.
 * 
 * @author vinayasimha.patil
 *
 */
public class RBTTextDao extends BaseDao
{
	public void insertRBTText(RBTText rbtText)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(rbtText);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void updateRBTText(RBTText rbtText)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(rbtText);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<RBTText> getAllRBTTexts()
	{
		Session session = this.getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(RBTText.class);
		@SuppressWarnings("unchecked")
		List<RBTText> rbtTextList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return rbtTextList;
	}

	public void removeRBTText(RBTText rbtText)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.delete(rbtText);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}
}
