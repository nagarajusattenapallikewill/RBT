/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.PredefinedGroup;

/**
 * PredefinedGroupDao has API's to persist data in RBT_PREDEFINED_GROUPS table.
 * 
 * @author vinayasimha.patil
 *
 */
public class PredefinedGroupDao extends BaseDao
{
	public List<PredefinedGroup> getAllPredefinedGroups()
	{
		Session session = this.getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(PredefinedGroup.class);
		@SuppressWarnings("unchecked")
		List<PredefinedGroup> predefinedGroupList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return predefinedGroupList;
	}

	public void insertPredefinedGroup(PredefinedGroup predefinedGroup)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(predefinedGroup);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void updatePredefinedGroup(PredefinedGroup predefinedGroup)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(predefinedGroup);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void removePredefinedGroup(PredefinedGroup predefinedGroup)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.delete(predefinedGroup);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}
}
