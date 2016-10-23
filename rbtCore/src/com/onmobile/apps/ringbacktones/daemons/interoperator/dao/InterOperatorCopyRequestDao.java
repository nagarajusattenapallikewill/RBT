package com.onmobile.apps.ringbacktones.daemons.interoperator.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;

import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorCopyRequestBean;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorHibernateUtils;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorUtility;

public class InterOperatorCopyRequestDao
{
	static Logger logger = Logger.getLogger(InterOperatorUtility.class);
	
	public static Long save(InterOperatorCopyRequestBean copyRequestBean)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		Long copyId = null;
		try
		{
			transaction = session.beginTransaction();
			copyId = (Long) session.save(copyRequestBean);
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while saving copyBean : "+copyRequestBean, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Saved copyBean : "+copyRequestBean);
		return copyId;
	}

	public static List<InterOperatorCopyRequestBean> listForStatus(int status)
	{
		logger.info("status="+status);
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<InterOperatorCopyRequestBean> copyRequests = null; 
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(InterOperatorCopyRequestBean.class);
			criteria.add(Expression.eq("status", status));
			criteria.setMaxResults(5000);
			copyRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while getting copyBean for status="+status, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Found "+copyRequests.size()+" copy requests in DB for status "+status);
		return copyRequests;
	}

	public static List<InterOperatorCopyRequestBean> listForStatusAndOperator(int status, int operatorId, int fetchSize)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<InterOperatorCopyRequestBean> copyRequests = null; 
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(InterOperatorCopyRequestBean.class);
			criteria.add(Expression.eq("copierOperatorId", operatorId));
			criteria.add(Expression.eq("status", status));
			if(fetchSize != -1)
				criteria.setMaxResults(fetchSize);
			copyRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while getting copyBean for status="+status+", opetatorId="+operatorId, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Found "+copyRequests.size()+" copy requests in DB for status "+status+ " and operatorId="+operatorId);
		return copyRequests;
	}
	
	public static List<InterOperatorCopyRequestBean> listForStatusAndOperatorAndLessThanTime(int status, int operatorId, int minutes)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<InterOperatorCopyRequestBean> copyrequests = null; 
		Date date = null; 
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(InterOperatorCopyRequestBean.class);
			criteria.add(Expression.eq("status",status));
			criteria.add(Expression.eq("copierOperatorId",operatorId));
			criteria.add(Expression.eq("status",status));
			criteria.setMaxResults(5000);
			if(minutes != -1)
			{
				Calendar nowCal = Calendar.getInstance();
				nowCal.add(Calendar.MINUTE, -minutes);
				date = nowCal.getTime();
				criteria.add(Expression.lt("requestTime", date));
			}
			copyrequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while getting copyBean for status="+status+", opetatorId="+operatorId+", and with requestTime < "+date, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Found "+copyrequests.size()+" copy requests in DB for status "+status+ " and operatorId="+operatorId+", and with requestTime < "+date);
		return copyrequests;
	}
	
	public static List<InterOperatorCopyRequestBean> listForLessThanTime(int minutes, int  fetchSize)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<InterOperatorCopyRequestBean> copyRequests = null;
		Date targetDate = null;
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(InterOperatorCopyRequestBean.class);
			Calendar targetCal = Calendar.getInstance();
			targetCal.add(Calendar.MINUTE, -minutes);
			targetDate = targetCal.getTime();
			criteria.add(Expression.le("requestTime", targetDate));
			if(fetchSize !=1)
				criteria.setMaxResults(fetchSize);
			copyRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while getting copyBean with requestTime < "+targetDate, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Found "+copyRequests.size()+" copy requests in DB for requestTime less than "+targetDate);
		return copyRequests;
	}
	
	public static InterOperatorCopyRequestBean getWithId(long copyId)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		InterOperatorCopyRequestBean copyBean = null;
		try
		{
			transaction = session.beginTransaction();
			copyBean = (InterOperatorCopyRequestBean) session.get(InterOperatorCopyRequestBean.class, copyId);
			transaction.commit();
			
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while getting copyBean with copyId "+copyId, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Got copyBean : "+copyBean);
		return copyBean;
	}

	public static void update(InterOperatorCopyRequestBean interOperatorCopyRequestBean)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();
			session.update(interOperatorCopyRequestBean);
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while updating copyBean : "+interOperatorCopyRequestBean, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Updated copyBean : "+interOperatorCopyRequestBean);
	}

	 

	public static void delete(Long copyId)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		try
		{
			transaction = session.beginTransaction();
			InterOperatorCopyRequestBean copyBean = (InterOperatorCopyRequestBean) session.get(InterOperatorCopyRequestBean.class, copyId);
			session.delete(copyBean);
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while deleting copyBean with copyId="+copyId, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Deleted copyBean with copyId="+copyId);
	}
	
	public static List<InterOperatorCopyRequestBean> listForCopierAndStatus(long copier, int status)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<InterOperatorCopyRequestBean> copyRequests = null;
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(InterOperatorCopyRequestBean.class);
			criteria.add(Expression.eq("status",status));
			criteria.add(Expression.eq("copierMdn",copier));
			copyRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while getting copyRequests for copier="+copier+", and status="+status, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Found copyRequests for copier="+copier+", and status="+status+", copyRequests="+copyRequests);
		return copyRequests;
	}
	
	public static List<InterOperatorCopyRequestBean> listForCopierAndInStatus(long copier, ArrayList<Integer> statusList)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<InterOperatorCopyRequestBean> copyRequests = null;
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(InterOperatorCopyRequestBean.class);
			criteria.add(Expression.eq("copierMdn", new Long(copier)));
			criteria.add(Expression.in("status", statusList));
			criteria.setMaxResults(5000);
			copyRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while getting copyRequests for copier="+copier+", and in status="+statusList, e);
		}
		finally
		{
			session.close();
		}
		logger.error("Got copyRequests for copier="+copier+", and in status="+statusList+", copyRequests="+copyRequests);
		return copyRequests;
	}
	
	public static List<InterOperatorCopyRequestBean> listForOperatorAndInStatus(int operatorId, ArrayList<Integer> statusList, int fetchSize)
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<InterOperatorCopyRequestBean> copyRequests = null;
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(InterOperatorCopyRequestBean.class);
			criteria.add(Expression.eq("copierOperatorId", operatorId));
			criteria.add(Expression.in("status", statusList));
			if(fetchSize != -1)
				criteria.setMaxResults(fetchSize);
			copyRequests = criteria.list();
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			logger.error("Exception while getting copyRequests for operatorId="+operatorId+", and in status="+statusList, e);
		}
		finally
		{
			session.close();
		}
		logger.info("Found "+copyRequests.size()+" copy requests in DB for status list "+statusList+ " and operatorId="+operatorId);
		return copyRequests;
	}

	public static List<InterOperatorCopyRequestBean> list()
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		List<InterOperatorCopyRequestBean> courses = null;
		try
		{
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(InterOperatorCopyRequestBean.class);
			Calendar targetCal = Calendar.getInstance();
			targetCal.add(Calendar.HOUR_OF_DAY, -2);
			Date targetDate = targetCal.getTime();
			System.out.println("targetDate="+targetDate);
			criteria.add(Expression.le("requestTime", targetDate));
			criteria.setMaxResults(5);
			courses = criteria.list();
			for (Iterator<InterOperatorCopyRequestBean> iterator = courses.iterator(); iterator.hasNext();)
			{
				InterOperatorCopyRequestBean copyBean = iterator.next();
				System.out.println("copyBean="+copyBean);
			}
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return courses;
	}

}

