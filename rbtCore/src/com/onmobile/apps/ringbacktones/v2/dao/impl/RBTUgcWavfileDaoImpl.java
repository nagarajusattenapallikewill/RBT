package com.onmobile.apps.ringbacktones.v2.dao.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile.UgcFileUploadStatus;
import com.onmobile.apps.ringbacktones.v2.dao.constants.DAOConstants;
import com.onmobile.apps.ringbacktones.v2.dao.util.HibernateUtil;

public class RBTUgcWavfileDaoImpl implements IRbtUgcWavfileDao, DAOConstants{


	private static Logger logger = Logger.getLogger(RBTUgcWavfileDaoImpl.class);
	
	private int maxFetchResult = 100;
	private int maxRetry = 100;
	private int nextRetryTimeInMins = 30;
	

	public int getMaxFetchResult() {
		return maxFetchResult;
	}

	public void setMaxFetchResult(int maxFetchResult) {
		this.maxFetchResult = maxFetchResult;
	}
	
	public int getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}
	
	public int getNextRetryTimeInMins() {
		return nextRetryTimeInMins;
	}

	public void setNextRetryTimeInMins(int nextRetryTimeInMins) {
		this.nextRetryTimeInMins = nextRetryTimeInMins;
	}

	@Override
	public RBTUgcWavfile saveUgcWavfile(RBTUgcWavfile rbtUgcWavfile) throws DataAccessException{
		logger.info("Save UgcWavFile: "+rbtUgcWavfile);
		if (rbtUgcWavfile == null)
			throw new DataAccessException(OBJECT_NULL);

		rbtUgcWavfile.setNextRetryTime(new Date());
		rbtUgcWavfile.setRetryCount(0);
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			long ugcId = (Long) session.save(rbtUgcWavfile);
			rbtUgcWavfile.setUgcId(ugcId);
			tx.commit();
			session.flush();
			return rbtUgcWavfile;
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error("Exception Occured on creating UGC: "+he, he);
			throw new DataAccessException(DUPLICATE_PK,he);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	@Override
	public boolean deleteUgcWavFile(RBTUgcWavfile rbtUgcWavfile)
			throws DataAccessException {
		logger.info("Deleting UGC: "+ rbtUgcWavfile);
		boolean isDeleted = false;

		if(rbtUgcWavfile.getUgcId() <= 0)
			throw new DataAccessException(INVALID_ID);
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			session.delete(rbtUgcWavfile);
			tx.commit();
			session.flush();
			isDeleted =  true;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured on Deleting UGC: ",he);
			throw new DataAccessException(INVALID_ID,he);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		return isDeleted;
	}

	@Override
	public RBTUgcWavfile getUgcWavFile(long ugcId) throws DataAccessException {
		logger.info("Get UGC by ugcId: "+ugcId);

		if(ugcId <= 0)
			throw new DataAccessException(INVALID_ID);
		Session session = null;
		//Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			//tx = session.beginTransaction();
			RBTUgcWavfile rbtUgcWavFile = (RBTUgcWavfile) session.load(RBTUgcWavfile.class, ugcId);
			//tx.commit();
			//session.flush();
			logger.info("UGC Wavfile: " + rbtUgcWavFile);
			return rbtUgcWavFile;
		} catch (HibernateException he) {
			/*if (tx != null)
				tx.rollback();*/
			logger.error("Exception Occured on Deleting UGC: "+he,he);
			throw new DataAccessException(INVALID_ID,he);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}		
	}

	@Override
	public List<RBTUgcWavfile> getUgcWavFiles(long subscriberId)
			throws DataAccessException {
		logger.info("Get UgcWavfile info by subscriber: " + subscriberId);
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			
			Criteria criteria = session.createCriteria(RBTUgcWavfile.class);
			criteria.add(Restrictions.eq("subscriberId", subscriberId));
			List<RBTUgcWavfile> rbtUgcWavFileList = (List<RBTUgcWavfile>) criteria.list();
			
			if(rbtUgcWavFileList == null || rbtUgcWavFileList.size() == 0){
				throw new DataAccessException(RECORD_NOT_FOUND);
			}
			tx.commit();
			session.flush();
			return rbtUgcWavFileList;
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);

		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	@Override
	public RBTUgcWavfile getUgcWavFile(long subscriberId, String wavFile)
			throws DataAccessException {
		logger.info("Get UgcWavfile info by subscriber: " + subscriberId + " and wavfile: " + wavFile);
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			String getHql = "FROM 	RBTUgcWavfile WHERE subscriberId = :subscriberId AND ugcWavFile = :ugcWavFile";			
			Query query = session.createQuery(getHql);
			query.setParameter("subscriberId", subscriberId);
			query.setParameter("ugcWavFile", wavFile);

			List<RBTUgcWavfile> rbtUgcWavFileList = (List<RBTUgcWavfile>) query.list();
			
			if(rbtUgcWavFileList == null || rbtUgcWavFileList.size() == 0){
				throw new DataAccessException(RECORD_NOT_FOUND);
			}
			tx.commit();
			session.flush();
			return rbtUgcWavFileList.get(0);
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);

		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	@Override
	public boolean deleteUgcWavfiles(long subscriberId)
			throws DataAccessException {
		logger.info("Removing UGC by subscriberId: " + subscriberId);
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			String deleteHql = "Delete RBTUgcWavfile where subscriberId = :subscriberId";
			Query query = session.createQuery(deleteHql);
			query.setParameter("subscriberId", subscriberId);
			
			int result = query.executeUpdate();
			if(result <= 0){
				throw new DataAccessException(RECORD_NOT_FOUND);
			}
			tx.commit();
			session.flush();
			return true;
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);

		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	@Override
	public RBTUgcWavfile updateUgcWavfile(RBTUgcWavfile rbtUgcWavfile)
			throws DataAccessException {
		logger.info("Update UgcWavFile: "+rbtUgcWavfile);
		if (rbtUgcWavfile == null)
			throw new DataAccessException(OBJECT_NULL);

		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.MINUTE, nextRetryTimeInMins);
			rbtUgcWavfile.setNextRetryTime(cal.getTime());
			session.update(rbtUgcWavfile);
			tx.commit();
			session.flush();
			return rbtUgcWavfile;
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error("Exception Occured on update UGC: "+he, he);
			throw new DataAccessException(DUPLICATE_PK,he);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	@Override
	public boolean deleteUgcWavfiles(long subscriberId, String wavFile)
			throws DataAccessException {
		logger.info("Removing UGC by subscriberId: " + subscriberId + " and wavfile: " + wavFile);
		boolean isDeleted = false;
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			String deleteHql = "Delete RBTUgcWavfile WHERE subscriberId = :subscriberId AND ugcWavFile = :ugcWavFile";
			Query query = session.createQuery(deleteHql);
			query.setParameter("subscriberId", subscriberId);
			query.setParameter("ugcWavFile", wavFile);
			int result = query.executeUpdate();
			if(result <= 0){
				throw new DataAccessException(RECORD_NOT_FOUND);
			}
			tx.commit();
			session.flush();
			isDeleted = true;
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);

		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		
		return isDeleted;
	}

	@Override
	public List<RBTUgcWavfile> getUgcWavFilesToTransfer(UgcFileUploadStatus status)
			throws DataAccessException {
		logger.info("Get UgcWavfile info by status: " + status);
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			String getHql = "FROM RBTUgcWavfile WHERE uploadStatus = :uploadStatus AND nextRetryTime <= :nextRetryTime AND retryCount < :retryCount ORDER BY nextRetryTime";			
			Query query = session.createQuery(getHql);
			query.setParameter("uploadStatus", status.getUgcDownloadState());
			query.setParameter("nextRetryTime", new Date());
			query.setParameter("retryCount", maxRetry);
			query.setMaxResults(maxFetchResult);

			List<RBTUgcWavfile> rbtUgcWavFileList = (List<RBTUgcWavfile>) query.list();
			
			tx.commit();
			session.flush();
			return rbtUgcWavFileList;
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);

		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(RECORD_NOT_FOUND);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}
	
	
	public static void main(String[] args) {
		RBTUgcWavfileDaoImpl obj =  new RBTUgcWavfileDaoImpl();
		obj.setMaxFetchResult(30);
		try {
			obj.getUgcWavFile(1);
			
			RBTUgcWavfile o = new RBTUgcWavfile();
			o.setSubscriberId(9886064692L);
			o.setUgcWavFile("rbt_ugc_rbt");
			o.setUploadStatus(UgcFileUploadStatus.TO_BE_PROCESS_STATE);
			o.setMode("RBT");
			obj.saveUgcWavfile(o);
			
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
