/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;
import com.onmobile.apps.ringbacktones.promotions.callgraph.CallGraph.PromotionStatus;

/**
 * @author vinayasimha.patil
 * 
 */
public class CallGraphDao
{
	private static Logger logger = Logger.getLogger(CallGraphDao.class);

	public static CallGraph save(CallGraph callGraph)
	{
		Session session = HibernateUtil.getSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();

			Long callGraphID = (Long) session.save(callGraph);
			callGraph.setCallGraphID(callGraphID);

			transaction.commit();

			if (logger.isInfoEnabled())
				logger.info("CallGraph saved: " + callGraph);

			return callGraph;
		}
		catch (HibernateException e)
		{
			logger.error(e.getMessage(), e);
			if (transaction != null)
				transaction.rollback();
		}
		finally
		{
			session.close();
		}

		return null;
	}

	public static CallGraph update(CallGraph callGraph)
	{
		Session session = HibernateUtil.getSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();
			session.update(callGraph);
			transaction.commit();

			if (logger.isInfoEnabled())
				logger.info("CallGraph updaled: " + callGraph);

			return callGraph;
		}
		catch (HibernateException e)
		{
			logger.error(e.getMessage(), e);
			if (transaction != null)
				transaction.rollback();
		}
		finally
		{
			session.close();
		}

		return null;
	}

	public static CallGraph getByCallGraphID(long callGraphID)
	{
		Session session = HibernateUtil.getSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();

			CallGraph callGraph = (CallGraph) session.get(CallGraph.class,
					callGraphID);
			transaction.commit();

			return callGraph;
		}
		catch (HibernateException e)
		{
			logger.error(e.getMessage(), e);
			if (transaction != null)
				transaction.rollback();
		}
		finally
		{
			session.close();
		}

		return null;
	}

	public static CallGraph getBySubscriberID(String subscriberID)
	{
		Session session = HibernateUtil.getSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();

			Criteria criteria = session.createCriteria(CallGraph.class);
			criteria = criteria.add(Restrictions.eq("subscriberID",
					subscriberID));
			CallGraph callGraph = (CallGraph) criteria.uniqueResult();

			transaction.commit();

			return callGraph;
		}
		catch (HibernateException e)
		{
			logger.error(e.getMessage(), e);
			if (transaction != null)
				transaction.rollback();
		}
		finally
		{
			session.close();
		}

		return null;
	}

	public static List<CallGraph> getByPromotionStatus(
			PromotionStatus promotionStatus, int fetchSize)
	{
		logger.debug("Getting callgraphs where PromotionStatus is "
				+ promotionStatus);

		Session session = HibernateUtil.getSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();

			Criteria criteria = session.createCriteria(CallGraph.class);
			criteria = criteria.add(Restrictions.eq("promotionStatus",
					promotionStatus));
			criteria.setMaxResults(fetchSize);

			@SuppressWarnings("unchecked")
			List<CallGraph> callGraphs = criteria.list();

			transaction.commit();

			if (logger.isDebugEnabled())
				logger.debug("Got " + callGraphs.size()
						+ " callgraphs where PromotionStatus is "
						+ promotionStatus);

			return callGraphs;
		}
		catch (HibernateException e)
		{
			logger.error(e.getMessage(), e);
			if (transaction != null)
				transaction.rollback();
		}
		finally
		{
			session.close();
		}

		return null;
	}

	public static List<CallGraph> getByPromotionStatusHavingFrequentCallers(
			PromotionStatus promotionStatus, int fetchSize)
	{
		logger.debug("Getting callgraphs where PromotionStatus is "
				+ promotionStatus);

		Session session = HibernateUtil.getSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();

			Criteria criteria = session.createCriteria(CallGraph.class);
			criteria = criteria.add(Restrictions.eq("promotionStatus",
					promotionStatus));
			criteria = criteria.add(Restrictions.isNotNull("frequentCallers"));
			criteria.setMaxResults(fetchSize);

			@SuppressWarnings("unchecked")
			List<CallGraph> callGraphs = criteria.list();

			transaction.commit();

			if (logger.isDebugEnabled())
				logger.debug("Got " + callGraphs.size()
						+ " callgraphs where PromotionStatus is "
						+ promotionStatus);

			return callGraphs;
		}
		catch (HibernateException e)
		{
			logger.error(e.getMessage(), e);
			if (transaction != null)
				transaction.rollback();
		}
		finally
		{
			session.close();
		}

		return null;
	}
}
