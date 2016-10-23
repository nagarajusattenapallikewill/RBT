package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRatingTransaction;

public class ClipRatingTransactionDAO
{
	private static Logger basicLogger = Logger.getLogger(ClipRatingDAO.class);

	public static ClipRatingTransaction saveClipRatingTransaction(
			ClipRatingTransaction clipRatingTransaction)
			throws DataAccessException
	{
		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try
		{
			tx = session.beginTransaction();

			session.save(clipRatingTransaction);
			tx.commit();

			if (basicLogger.isDebugEnabled())
			{
				basicLogger.debug("Saved clipRatingTransaction "
						+ clipRatingTransaction + " in "
						+ (System.currentTimeMillis() - start));
			}

			return clipRatingTransaction;
		}
		catch (ConstraintViolationException cve)
		{
			basicLogger.error("", cve);
			if (null != tx)
				tx.rollback();

			throw cve;
		}
		catch (HibernateException he)
		{
			basicLogger.error("", he);
			if (null != tx)
				tx.rollback();

			throw new DataAccessException(he);
		}
		finally
		{
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static ClipRatingTransaction getClipRatingTransaction(int clipId,
			Date ratingDate) throws DataAccessException
	{
		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		List<ClipRatingTransaction> result = null;
		Transaction tx = null;
		try
		{
			tx = session.beginTransaction();
			Criteria criteria = session
					.createCriteria(ClipRatingTransaction.class);
			criteria.add(Restrictions.eq("clipId", new Integer(clipId)));
			criteria.add(Restrictions.eq("ratingDate", ratingDate));
			result = criteria.list();
			tx.commit();
			
			if (basicLogger.isDebugEnabled())
			{
				basicLogger.debug("ClipRatingTransaction object for clip id "
						+ clipId + " Rating date " + ratingDate + " in "
						+ (System.currentTimeMillis() - start));
			}

			return result.size() > 0 ? result.get(0) : null;
		}
		catch (HibernateException he)
		{
			basicLogger.error("", he);
			if (null != tx)
				tx.rollback();

			throw new DataAccessException(he);
		}
		finally
		{
			session.close();
		}
	}

	public static boolean rateClip(int clipId, int rating)
			throws DataAccessException
	{
		boolean isUpdated = false;
		long start = System.currentTimeMillis();

		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try
		{
			tx = session.beginTransaction();

			Date ratingDate = new Date();
			String hqlUpdate = "update ClipRatingTransaction set sumOfRatings = sumOfRatings + :rating, noOfVotes = noOfVotes + 1 where clipId = :clipId and ratingDate = :ratingDate";

			Query query = session.createQuery(hqlUpdate);
			query.setInteger("rating", rating);
			query.setInteger("clipId", clipId);
			query.setDate("ratingDate", ratingDate);

			int result = query.executeUpdate();
			isUpdated = (result > 0);

			tx.commit();
			if (basicLogger.isDebugEnabled())
			{
				basicLogger.debug("Updated clipRatingTransaction with clipId "
						+ clipId + " in "
						+ (System.currentTimeMillis() - start));
			}

			return isUpdated;
		}
		catch (HibernateException he)
		{
			basicLogger.error("", he);
			if (null != tx)
				tx.rollback();

			throw new DataAccessException(he);
		}
		finally
		{
			session.close();
		}
	}

	public static boolean likeClip(int clipId)
			throws DataAccessException
	{
		boolean isUpdated = false;
		long start = System.currentTimeMillis();

		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try
		{
			tx = session.beginTransaction();

			Date ratingDate = new Date();
			String hqlUpdate = "update ClipRatingTransaction set likeVotes = likeVotes + 1 where clipId = :clipId and ratingDate = :ratingDate";

			Query query = session.createQuery(hqlUpdate);
			query.setInteger("clipId", clipId);
			query.setDate("ratingDate", ratingDate);

			int result = query.executeUpdate();
			isUpdated = (result > 0);

			tx.commit();
			if (basicLogger.isDebugEnabled())
			{
				basicLogger.debug("Incremented likeVotes for clip "
						+ clipId + " in "
						+ (System.currentTimeMillis() - start));
			}

			return isUpdated;
		}
		catch (HibernateException he)
		{
			basicLogger.error("", he);
			if (null != tx)
				tx.rollback();

			throw new DataAccessException(he);
		}
		finally
		{
			session.close();
		}
	}

	public static boolean dislikeClip(int clipId)
			throws DataAccessException
	{
		boolean isUpdated = false;
		long start = System.currentTimeMillis();

		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try
		{
			tx = session.beginTransaction();

			Date ratingDate = new Date();
			String hqlUpdate = "update ClipRatingTransaction set dislikeVotes = dislikeVotes + 1 where clipId = :clipId and ratingDate = :ratingDate";

			Query query = session.createQuery(hqlUpdate);
			query.setInteger("clipId", clipId);
			query.setDate("ratingDate", ratingDate);

			int result = query.executeUpdate();
			isUpdated = (result > 0);

			tx.commit();
			if (basicLogger.isDebugEnabled())
			{
				basicLogger.debug("Incremented dislikeVotes for clip "
						+ clipId + " in "
						+ (System.currentTimeMillis() - start));
			}

			return isUpdated;
		}
		catch (HibernateException he)
		{
			basicLogger.error("", he);
			if (null != tx)
				tx.rollback();

			throw new DataAccessException(he);
		}
		finally
		{
			session.close();
		}
	}
}
