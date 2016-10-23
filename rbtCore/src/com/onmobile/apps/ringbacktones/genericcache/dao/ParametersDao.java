package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

/**
 * ParametersDao has API's to persist data in RBT_PARAMETERS table.
 * 
 * @author manish.shringarpure
 */
public class ParametersDao extends BaseDao
{
	public void insertParameters(Parameters parameter)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();
        try {
        	session.save(parameter);
        }
        catch(Exception e) {}
        finally{
		  session.flush();
		  transaction.commit();
		  closeHibernateSession(session);
        }
	}

	public void updateParameters(Parameters parameter)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();
		try{
		session.update(parameter);
		}
		catch(Exception e){}
		session.flush();
		transaction.commit();
		closeHibernateSession(session);
	}

	public List<Parameters> getAllParameters()
	{
		Session session = this.getHibernateSession();
		Transaction transaction = session.beginTransaction();

		Criteria criteria = session.createCriteria(Parameters.class);
		@SuppressWarnings("unchecked")
		List<Parameters> parametersList = criteria.list();

		session.flush();
		transaction.commit();
		closeHibernateSession(session);

		return parametersList;
	}

	public void removeParameter(Parameters parameter)
	{
		Session session = getHibernateSession();
		Transaction transaction = session.beginTransaction();
		try {
		session.delete(parameter);
		} 
		catch(Exception e) {}
        finally {
			session.flush();
			transaction.commit();
			closeHibernateSession(session);
		}
	}
}
