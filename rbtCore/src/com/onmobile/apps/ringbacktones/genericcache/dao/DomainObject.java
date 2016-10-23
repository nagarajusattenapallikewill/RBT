package com.onmobile.apps.ringbacktones.genericcache.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

public abstract class DomainObject
{
	abstract public Long getId();

	abstract public void setId(Long id);


	public void insert()
	{
		Session session = null;
		Transaction transaction = null;
		boolean done = false;
		try
		{
			session = BaseDao.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.save(this);
			done = true;
		}
		finally
		{
			commitOrRollBack(transaction, done);
			try 
			{
				session.close();	
			} 
			catch (Exception e) 
			{
			}

		}

	}

	public void update()
	{
		Session session = null;
		Transaction transaction = null;
		boolean done = false;
		try
		{
			session = BaseDao.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.update(this);
			done = true;
		}
		finally
		{
			commitOrRollBack(transaction, done);
			try 
			{
				session.close();	
			} 
			catch (Exception e) 
			{
			}

		}

	}

	public void delete()
	{
		Session session = null;
		Transaction transaction = null;
		boolean done = false;
		try
		{
			session = BaseDao.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.delete(this);
			done = true;
		}
		finally
		{
			commitOrRollBack(transaction, done);
			session.close();
		}
	}

	private void commitOrRollBack(Transaction transaction, boolean done) 
	{
		try 
		{
			if(done)
			{
				transaction.commit();
			}
			else
			{
				transaction.rollback();
			}
		} 
		catch (Exception e) 
		{
			
		}
		
	}

	public String toString()
	{
		return getClass().getSimpleName() + "_" + getId();
	}

	public static <T extends DomainObject> T loadById(Class<T> objectType, String tableName, long id)
	{
		String sql = "select {cust.*} from " + tableName + " cust where cust.id=" + id;
		return loadSingle(objectType, sql);
	}

	public static <T extends DomainObject> T loadSingle(Class<T> objectType, String sql)
	{
		List<T> result = load(objectType, sql);
		if (result == null || result.size() == 0)
		{
			return null;
		}
		else
		{
			return result.get(0);
		}
	}

	public static <T extends DomainObject> List<T> load(Class<T> objectType, String sql)
	{
		Session session = null;
		try
		{
			session = BaseDao.getSessionFactory().openSession();
			List<T> objs = session.createSQLQuery(sql).addEntity("cust", objectType).list();
			return objs;
		}
		finally
		{
			try 
			{
				session.close();	
			} 
			catch (Exception e) 
			{
			}

		}
	}
}
