package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorRequestBean;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorHibernateUtils;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorRequestDao
{
	private static Logger logger = Logger.getLogger(ContentInterOperatorRequestDao.class);

	/**
	 * Insert an entry into RBT_THIRD_PARTY_REQUESTS table with the bean.
	 * 
	 * @param contentRequestBean
	 * @return
	 */
	public static Long save(ContentInterOperatorRequestBean contentRequestBean)
	{
		Session session = ContentInterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		Long sequenceID = null;
		try
		{
			transaction = session.beginTransaction();
			sequenceID = (Long) session.save(contentRequestBean);
			transaction.commit();
		}
		catch (HibernateException e)
		{
			if (transaction != null)
				transaction.rollback();

			logger.error("Exception while saving contentRequestBean : " + contentRequestBean, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Saved contentRequestBean : " + contentRequestBean);
		return sequenceID;
	}

	/**
	 * Returns all the beans based on msisdn and status.
	 * 
	 * @param msisdn
	 * @param status
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ContentInterOperatorRequestBean> listForMsisdnAndStatus(String msisdn, int status)
	{
		Session session = ContentInterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<ContentInterOperatorRequestBean> contentRequests = new ArrayList<ContentInterOperatorRequestBean>();
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(ContentInterOperatorRequestBean.class);
			criteria.add(Restrictions.eq("status", status));
			criteria.add(Restrictions.eq("msisdn", msisdn));
			contentRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			if (transaction != null)
				transaction.rollback();

			logger.error("Exception while getting contentRequests for msisdn = " + msisdn + ", and status = " + status, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Found contentRequests for msisdn = " + msisdn + ", and status = " + status + ", contentRequests = " + contentRequests);
		return contentRequests;
	}

	/**
	 * Returns all the beans based on msisdn and statusList.
	 * 
	 * @param msisdn
	 * @param statusList
	 * @return
	 */
	public static List<ContentInterOperatorRequestBean> listForMsisdnAndInStatus(String msisdn, ArrayList<Integer> statusList)
	{
		return listForMsisdnAndInStatus(msisdn, statusList, 5000);
	}

	/**
	 * Returns all the beans based on msisdn, statusList and fetchSize.
	 * 
	 * @param msisdn
	 * @param statusList
	 * @param fetchSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ContentInterOperatorRequestBean> listForMsisdnAndInStatus(String msisdn, ArrayList<Integer> statusList, int fetchSize)
	{
		Session session = ContentInterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<ContentInterOperatorRequestBean> contentRequests = new ArrayList<ContentInterOperatorRequestBean>();
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(ContentInterOperatorRequestBean.class);
			criteria.add(Restrictions.like("msisdn", msisdn));
			criteria.add(Restrictions.in("status", statusList));
			if (fetchSize != -1)
				criteria.setMaxResults(fetchSize);
			contentRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			if (transaction != null)
				transaction.rollback();

			logger.error("Exception while getting contentRequests for msisdn = " + msisdn + ", and in status = " + statusList, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Got contentRequests for msisdn = " + msisdn + ", and in status = " + statusList + ", contentRequests = " + contentRequests);
		return contentRequests;
	}

	/**
	 * Returns all the beans based on operatorID and statusList.
	 * 
	 * @param operatorId
	 * @param statusList
	 * @return
	 */
	public static List<ContentInterOperatorRequestBean> listForOperatorAndInStatus(int operatorId, ArrayList<Integer> statusList)
	{
		return listForOperatorAndInStatus(operatorId, statusList, 5000);
	}

	/**
	 * Returns all the beans based on operatorID, statusList and fetchSize.
	 * 
	 * @param operatorID
	 * @param statusList
	 * @param fetchSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ContentInterOperatorRequestBean> listForOperatorAndInStatus(int operatorID, ArrayList<Integer> statusList, int fetchSize)
	{
		Session session = ContentInterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<ContentInterOperatorRequestBean> contentRequests = new ArrayList<ContentInterOperatorRequestBean>();
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(ContentInterOperatorRequestBean.class);
			criteria.add(Restrictions.eq("operatorID", operatorID));
			criteria.add(Restrictions.in("status", statusList));
			if (fetchSize != -1)
				criteria.setMaxResults(fetchSize);
			contentRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			if (transaction != null)
				transaction.rollback();

			logger.error("Exception while getting contentRequests for operatorID = " + operatorID + ", and in status = " + statusList, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Got contentRequests for operatorID = " + operatorID + ", and in status = " + statusList + ", contentRequests = " + contentRequests);
		return contentRequests;
	}

	/**
	 * Returns all the beans based on status, operatorID and fetchSize.
	 * 
	 * @param status
	 * @param operatorID
	 * @param fetchSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ContentInterOperatorRequestBean> listForStatusAndOperator(int status, int operatorID, int fetchSize)
	{
		Session session = ContentInterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<ContentInterOperatorRequestBean> contentRequests = new ArrayList<ContentInterOperatorRequestBean>(); 
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(ContentInterOperatorRequestBean.class);
			criteria.add(Restrictions.eq("operatorID", operatorID));
			criteria.add(Restrictions.eq("status", status));
			if (fetchSize != -1)
				criteria.setMaxResults(fetchSize);
			contentRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			if (transaction != null)
				transaction.rollback();

			logger.error("Exception while getting contentBean for status = " + status + ", opetatorID = " + operatorID, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Found " + contentRequests.size() + " content requests in DB for status " + status + " and operatorID = " + operatorID);
		return contentRequests;
	}

	/**
	 * Returns all the beans less than 'n' minutes.
	 * 
	 * @param minutes
	 * @param fetchSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ContentInterOperatorRequestBean> listForLessThanTime(int minutes, int  fetchSize)
	{
		Session session = ContentInterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<ContentInterOperatorRequestBean> contentRequests = new ArrayList<ContentInterOperatorRequestBean>();
		Date targetDate = null;
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(ContentInterOperatorRequestBean.class);
			Calendar targetCal = Calendar.getInstance();
			targetCal.add(Calendar.MINUTE, -minutes);
			targetDate = targetCal.getTime();
			criteria.add(Restrictions.le("requestTime", targetDate));
			if (fetchSize != 1)
				criteria.setMaxResults(fetchSize);
			contentRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			if (transaction != null)
				transaction.rollback();

			logger.error("Exception while getting contentBean with requestTime < " + targetDate, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Found " + contentRequests.size() + " content requests in DB for requestTime less than " + targetDate);
		return contentRequests;
	}

	/**
	 * Updates the entry in RBT_THIRD_PARTY_REQUESTS table with the bean object passed.
	 * 
	 * @param contentRequestBean
	 */
	public static void update(ContentInterOperatorRequestBean contentRequestBean)
	{
		Session session = ContentInterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();
			session.update(contentRequestBean);
			transaction.commit();
		}
		catch (HibernateException e)
		{
			if (transaction != null)
				transaction.rollback();

			logger.error("Exception while updating contentRequestBean : " + contentRequestBean, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Updated contentRequestBean : " + contentRequestBean);
	}

	/**
	 * Deletes the entry from RBT_THIRD_PARTY_REQUESTS table based on sequenceID.
	 *  
	 * @param sequenceID
	 */
	public static void delete(Long sequenceID)
	{
		Session session = ContentInterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();
			ContentInterOperatorRequestBean contentBean = (ContentInterOperatorRequestBean) session.get(ContentInterOperatorRequestBean.class, sequenceID);
			session.delete(contentBean);
			transaction.commit();
		}
		catch (HibernateException e)
		{
			if (transaction != null)
				transaction.rollback();

			logger.error("Exception while deleting contentRequestBean with sequenceID = " + sequenceID, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Deleted contentRequestBean with sequenceID = " + sequenceID);
	}
}
