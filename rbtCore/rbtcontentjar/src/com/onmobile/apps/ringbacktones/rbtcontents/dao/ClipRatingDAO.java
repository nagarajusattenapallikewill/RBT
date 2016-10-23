package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;

public class ClipRatingDAO
{

	private static Logger basicLogger = Logger.getLogger(ClipRatingDAO.class);

	public static ClipRating getClipRating(int clipId)
			throws DataAccessException
	{
		long start = System.currentTimeMillis();

		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try
		{
			tx = session.beginTransaction();
			ClipRating clipRating = (ClipRating) session.get(ClipRating.class,
					clipId);
			tx.commit();

			if (basicLogger.isDebugEnabled())
			{
				basicLogger.debug("Got ClipRating of clipId " + clipId + " in "
						+ (System.currentTimeMillis() - start));
			}

			if (clipRating == null)
				clipRating = new ClipRating(clipId, 0, 0, 0, 0, 0);

			return clipRating;
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

	public static Map<Integer, ClipRating> getClipsRatings(List<Integer> clipIds)
			throws DataAccessException
	{
		long start = System.currentTimeMillis();

		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try
		{
			tx = session.beginTransaction();

			Criteria criteria = session.createCriteria(ClipRating.class);
			criteria = criteria.add(Restrictions.in("clipId", clipIds));

			@SuppressWarnings("unchecked")
			List<ClipRating> clipRatings = criteria.list();
			tx.commit();

			if (basicLogger.isDebugEnabled())
			{
				basicLogger.debug("Got all the clipsRating in "
						+ (System.currentTimeMillis() - start));
			}

			Map<Integer, ClipRating> clipRatingMap = new HashMap<Integer, ClipRating>();
			for (ClipRating clipRating : clipRatings)
			{
				clipRatingMap.put(clipRating.getClipId(), clipRating);
			}
			
			Map<Integer, ClipRating> result = new LinkedHashMap<Integer, ClipRating>();
			for (Integer clipId : clipIds)
			{
				if (clipRatingMap.containsKey(clipId))
					result.put(clipId, clipRatingMap.get(clipId));
				else
					result.put(clipId, new ClipRating(clipId, 0, 0, 0, 0, 0));
			}

			return result;
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
