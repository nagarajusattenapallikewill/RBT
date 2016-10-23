package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUser;

public class RBTSocialUserDao extends BaseDao {
	
	public void insertRBTSocialUser(RBTSocialUser user)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.save(user);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public void updateRBTSocialUser(RBTSocialUser user)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.update(user);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<RBTSocialUser> getAllRBTSocialUser()
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(RBTSocialUser.class);
		@SuppressWarnings("unchecked")
		List<RBTSocialUser> userList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return userList;
	}

	public void removeRBTSocialUser(RBTSocialUser user)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();

		session.delete(user);

		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

}
