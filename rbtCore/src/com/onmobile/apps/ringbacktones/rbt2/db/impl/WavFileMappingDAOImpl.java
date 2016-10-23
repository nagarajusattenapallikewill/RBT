package com.onmobile.apps.ringbacktones.rbt2.db.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.db.IWavFileMappingDAO;
import com.onmobile.apps.ringbacktones.v2.dao.bean.WavFileMapping;
import com.onmobile.apps.ringbacktones.v2.dao.util.HibernateUtil;

@Repository(value = BeanConstant.WAV_FILE_MAPPING_DAO)
@Lazy(value = true)
public class WavFileMappingDAOImpl implements IWavFileMappingDAO {

	private static Logger logger = Logger
			.getLogger(WavFileMappingDAOImpl.class);

	@Override
	public WavFileMapping getWavFileVerOne(String wavFileVerTwo, String operator) {
		logger.info("Getting WavFileVerOne by Operator: " + operator
				+ " and wavFileVerTwo: " + wavFileVerTwo);
		WavFileMapping wavFileMapping = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(WavFileMapping.class);
			criteria.add(
					Restrictions.eq("wavFileCompositeKey.operatorName",
							operator.toUpperCase())).add(
					Restrictions.eq("wavFileCompositeKey.wavFileVerTwo",
							wavFileVerTwo));
			wavFileMapping = (WavFileMapping) criteria.uniqueResult();
		} catch (HibernateException he) {
			logger.error("Exception Occured: " + he, he);
		} catch (Exception e) {
			logger.error("Exception Occured: " + e, e);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		logger.info("Returning WavFileMapping: " + wavFileMapping);
		return wavFileMapping;
	}

	@Override
	public boolean saveWavFileMapping(WavFileMapping wavFileMapping) {
		logger.info("Saving WavFileMapping");
		boolean isSaved = false;
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			session.save(wavFileMapping);
			tx.commit();
			session.flush();
			isSaved = true;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured: " + he, he);
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured: " + e, e);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		logger.info("WavFileMapping saved: " + isSaved);
		return isSaved;
	}

	@Override
	public int saveOrUpdateWavFileMapping(List<WavFileMapping> wavFileMappings) {
		int count = 0;
		int batchSize = 0;

		logger.info("Saving WavFileMapping");

		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();

			for (WavFileMapping wavFileMapping : wavFileMappings) {
				session.saveOrUpdate(wavFileMapping);
				batchSize++;
				if (batchSize == 20) {
					tx.commit();
					session.flush();
					count += session.getStatistics().getEntityCount();
					session.clear();
					tx = session.beginTransaction();
					batchSize = 0;
				}
			}
			tx.commit();
			session.flush();
			count += session.getStatistics().getEntityCount();

		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured: " + he, he);
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();

			logger.error("Exception Occured: " + e, e);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}

		return count;
	}

	@Override
	public WavFileMapping getWavFileVerTwo(String wavFileVerOne, String operator) {
		logger.info("Getting wavFileVerTwo by Operator: "
				+ operator.toUpperCase() + " and wavFileVerOne: "
				+ wavFileVerOne);
		WavFileMapping wavFileMapping = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(WavFileMapping.class);
			criteria.add(
					Restrictions.eq("wavFileCompositeKey.operatorName",
							operator.toUpperCase())).add(
					Restrictions.eq("wavFileVerOne", wavFileVerOne));
			wavFileMapping = (WavFileMapping) criteria.uniqueResult();
		} catch (HibernateException he) {
			logger.error("Exception Occured: " + he, he);
		} catch (Exception e) {
			logger.error("Exception Occured: " + e, e);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		logger.info("Returning WavFileMapping: " + wavFileMapping);
		return wavFileMapping;
	}

	@Override
	public List<WavFileMapping> getWavFileVerTwoByBatch(int minLimit,
			int maxLimit) {
		logger.info("Getting WavFileVerOne by limit:minLimit--> " + minLimit
				+ " and maxLimit:---> " + maxLimit);
		List<WavFileMapping> wavFileMappingLst = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			Query tmp = session.createQuery("from WavFileMapping");
			tmp.setFirstResult(minLimit);
			tmp.setMaxResults(maxLimit);
			wavFileMappingLst = (List<WavFileMapping>) tmp.list();
		} catch (HibernateException he) {
			logger.error("Exception Occured: " + he, he);
		} catch (Exception e) {
			logger.error("Exception Occured: " + e, e);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		logger.info("Returning WavFileMapping List: "
				+ (null != wavFileMappingLst ? wavFileMappingLst.toString()
						: null));
		return wavFileMappingLst;
	}

}
