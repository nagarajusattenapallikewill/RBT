package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.UgcClip;

/**
 * @author senthil.raja
 *
 */

public class UgcClipDAO {

	
	private static Logger basicLogger = Logger.getLogger(UgcClipDAO.class);
	
	/**
	 * Saves the clip in RBT_UGC_CLIPS table.
	 * If clipStartTime() is null, then the value is set as {@link UgcClip#DEFAULT_CLIP_CREATION_TIME}
	 * If clipEndTime() is null, then the value is set as {@link UgcClip#DEFAULT_CLIP_END_TIME}
	 * @param ugcClip
	 * @return the UgcClip object saved in the DB. 
	 * @throws DataAccessException
	 */
	public static UgcClip saveClip(UgcClip ugcClip) throws DataAccessException {

		//check the clip end time
		if(null == ugcClip.getClipEndTime()) {
			ugcClip.setClipEndTime(UgcClip.DEFAULT_CLIP_END_TIME);
		}
		if(null == ugcClip.getClipStartTime()){
			ugcClip.setClipStartTime(UgcClip.DEFAULT_CLIP_END_TIME);
		}
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		String clipPromoId = ugcClip.getClipPromoId();
		try {
			tx = session.beginTransaction();
			session.save(ugcClip);
			session.flush();
			session.clear();
			if(clipPromoId == null){
				SQLQuery query = session.createSQLQuery("UPDATE RBT_UGC_CLIPS SET CLIP_PROMO_ID = CLIP_ID WHERE CLIP_ID = " + ugcClip.getClipId());
				ugcClip.setClipPromoId(ugcClip.getClipId()+"");
				query.executeUpdate();
			}
			tx.commit();
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Saved ugcclip " + ugcClip + " in " + (System.currentTimeMillis() - start) + "ms");
			}
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}		
		start = System.currentTimeMillis();
		return ugcClip;
	}
	
	/**
	 * Update the clip in RBT_UGC_CLIPS table.
	 * If clipEndTime() is null, then the value is set as {@link UgcClip#DEFAULT_CLIP_END_TIME}
	 * @param ugcClip
	 * @return the UgcClip object updated in the DB. 
	 * @throws DataAccessException
	 */
	public static UgcClip updateClip(UgcClip ugcClip) throws DataAccessException {

		//check the clip end time
		if(null == ugcClip.getClipEndTime()) {
			ugcClip.setClipEndTime(UgcClip.DEFAULT_CLIP_END_TIME);
		}
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(ugcClip);
			tx.commit();
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated ugcclip " + ugcClip + " in " + (System.currentTimeMillis() - start) + "ms");
			}
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
		start = System.currentTimeMillis();
		return ugcClip;
	}
	
	
	
	/**
	 * Get all the UgcClips as a List
	 * @return List<UgcClip>
	 * @throws Exception
	 */
	public static UgcClip[] getAllUgcClip() throws DataAccessException {
		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(UgcClip.class);
			List<UgcClip> result = (List<UgcClip>)criteria.list();
			tx.commit();
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got all the ugcclips in " + (System.currentTimeMillis() - start) + "ms");
			}
			return (UgcClip[])result.toArray(new UgcClip[0]);
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	/**
	 * Get UgcClip by status
	 * @param status
	 * @return List<UgcClip>
	 * @throws Exception
	 */
	public static UgcClip[] getUgcClipByStatus(char status) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		UgcClip clip = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(UgcClip.class);
			criteria = criteria.add(Restrictions.eq("clipStatus", status));
			List<UgcClip> result = (List<UgcClip>)criteria.list();			
			tx.commit();
			return (UgcClip[])result.toArray(new UgcClip[0]);
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	public static UgcClip[] getUgcClipByStatus(char status, int offset, int rowCount) throws DataAccessException {
		UgcClip[] ugcClipArr = getUgcClipByStatus(status);
		if(ugcClipArr.length <= offset){
			return new UgcClip[0];
		}
		if(rowCount <= 0){
			rowCount = ugcClipArr.length;
		}
		
		int endIndex = offset + rowCount;
		if(endIndex > ugcClipArr.length)
			endIndex = ugcClipArr.length;
		UgcClip[] dest = new UgcClip[endIndex - offset];
		System.arraycopy(ugcClipArr, offset, dest, 0, (endIndex - offset));
		return dest;
	}
	
	/**
	 * Get UgcClip by promo id
	 * @param promoId
	 * @return UgcClip
	 * @throws Exception
	 */
	public static UgcClip getUgcClipByPromoId(String promoId) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		UgcClip ugcClip = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(UgcClip.class);
			criteria = criteria.add(Restrictions.eq("clipPromoId", promoId));
			List<UgcClip> result = (List<UgcClip>)criteria.list();
			if(null != result && result.size() > 0) {
				ugcClip = result.get(0);
			}
			tx.commit();
			return ugcClip;
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	/**
	 * Get UgcClip by rbtWavFile
	 * @param clipRbtWavFile
	 * @return UgcClip
	 * @throws Exception
	 */
	public static UgcClip getUgcClipByWavFile(String clipRbtWavFile) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		UgcClip ugcClip = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(UgcClip.class);
			criteria = criteria.add(Restrictions.eq("clipRbtWavFile", clipRbtWavFile));
			List<UgcClip> result = (List<UgcClip>)criteria.list();
			if(null != result && result.size() > 0) {
				ugcClip = result.get(0);
			}
			tx.commit();
			return ugcClip;
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	/**
	 * Get all active UgcClip where clipStartTime < now and clipEndTime > now 
	 * @return List<UgcClip>
	 * @throws DataAccessException
	 */
	public static UgcClip[] getAllActiveUgcClips() throws DataAccessException {

		//String query = "from Clip clip, CategoryClipMap categoryClipMap where clip.clipId=categoryClipMap.clipId";

		long start = System.currentTimeMillis(); 
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(UgcClip.class);
			criteria = criteria.add(Restrictions.lt("clipStartTime", new Date()));
			criteria = criteria.add(Restrictions.gt("clipEndTime", new Date()));
			//do i have to add the clip id in (select distinct clip id from category_clip_map)
			List<UgcClip> result = (List<UgcClip>)criteria.list();
			tx.commit();
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got all the active clips in " + (System.currentTimeMillis() - start) + "ms");
			}
			return (UgcClip[])result.toArray(new UgcClip[0]);
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	public static UgcClip[] getUgcLiveClipByStatus(char status) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		UgcClip clip = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(UgcClip.class);
			criteria = criteria.add(Restrictions.eq("clipStatus", status));
			criteria = criteria.add(Restrictions.lt("clipStartTime", new Date()));
			criteria = criteria.add(Restrictions.gt("clipEndTime", new Date()));
			List<UgcClip> result = (List<UgcClip>)criteria.list();			
			tx.commit();
			return (UgcClip[])result.toArray(new UgcClip[0]);
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	public static UgcClip getUgcClip(int clipId) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		UgcClip clip = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(UgcClip.class);
			criteria = criteria.add(Restrictions.eq("clipId", clipId));
			List<UgcClip> result = (List<UgcClip>)criteria.list();			
			tx.commit();
			if(result.size() > 0)
				return result.get(0);			
		} catch(HibernateException he) {
			basicLogger.error("",he);
			if(null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
		return null;
	}
}
