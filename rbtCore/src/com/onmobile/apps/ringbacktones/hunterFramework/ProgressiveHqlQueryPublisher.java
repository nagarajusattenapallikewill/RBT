package com.onmobile.apps.ringbacktones.hunterFramework;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

public abstract class ProgressiveHqlQueryPublisher extends ProgressivePublisher
{

    private Session session = null;
    private ScrollableResults scrollableResults = null;
    private int counter = 0;
    private static Logger logger = Logger.getLogger(ProgressiveHqlQueryPublisher.class);
    @Override
    protected void executeQuery(int count) throws HunterException
    {
        session = getSession();
        String hql = getHqlQuery(count);
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
	        Query q = session.createSQLQuery(hql).addEntity(getBeanType());
	        scrollableResults = q.scroll();
//	        transaction.commit();
		}
		catch (HibernateException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			throw new HunterException(e);
		}		
    }

    @Override
    protected void finaliseQuery()
    {
        try
        {
            scrollableResults.close();
        }
        catch (Exception e)
        {
            // Nothing to do
        }
        releaseSession(session);
        counter =0;
    }

    public abstract String getHqlQuery(int count);
    public abstract Class getBeanType();

    @Override
    protected QueueComponent getNextQueueComponent() throws HunterException
    {
        Object beanObject = scrollableResults.get(0);
        return getNextQueueComponent(beanObject);
    }
    public abstract QueueComponent getNextQueueComponent(Object beanObject);
    public abstract Session getSession();

    @Override
    public int getWorkerThreadPriority()
    {
        return 5;
    }

    @Override
    protected boolean hasMoreQueueComponents() throws HunterException
    {
        return scrollableResults != null && scrollableResults.next();
    }

    public void releaseSession(Session session)
    {
		try {
			session.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		try {
			session.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
    }


    @Override
    protected void setPresentQueryCount(int addCount, int count)
    {
        // TODO Auto-generated method stub

    }

}
