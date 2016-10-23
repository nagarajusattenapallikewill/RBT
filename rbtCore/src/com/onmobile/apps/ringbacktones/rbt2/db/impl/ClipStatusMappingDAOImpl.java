package com.onmobile.apps.ringbacktones.rbt2.db.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.db.IClipStatusMappingDAO;
import com.onmobile.apps.ringbacktones.v2.dao.bean.ClipStatusMapping;
import com.onmobile.apps.ringbacktones.v2.dao.bean.OperatorCircleMapping;
import com.onmobile.apps.ringbacktones.v2.dao.util.HibernateUtil;

@Repository(value=BeanConstant.CLIP_STATUS_MAPPING_DAO)
@Lazy(value=true)
public class ClipStatusMappingDAOImpl implements IClipStatusMappingDAO {

	private static Logger logger = Logger.getLogger(ClipStatusMappingDAOImpl.class);
	
	@Override
	public ClipStatusMapping getClipStatusMappingByOperatorId(int operatorId, int clipId) {
		logger.info("Getting ClipStatusMapping by OperatorId: "+operatorId+" and ClipId: "+clipId);
		ClipStatusMapping clipStatusMapping = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(ClipStatusMapping.class);
			criteria.add(Restrictions.eq("compositeKey.operatorCircleMapping.id", operatorId))
					.add(Restrictions.eq("compositeKey.clipId", clipId));
			clipStatusMapping = (ClipStatusMapping) criteria.uniqueResult();
		} catch(HibernateException he) {
			logger.error("Exception Occured: "+he,he);
		} catch(Exception e) {
			logger.error("Exception Occured: "+e,e);
		} finally {
			if(session != null) {
				session.clear();
				session.close();
			}
		}
		logger.info("Returning ClipStatusMapping: "+clipStatusMapping);
		return clipStatusMapping;
	}

	@Override
	public boolean saveClipStatusMapping(ClipStatusMapping clipStatusMapping) {

		logger.info("Saving ClipStatusMapping");
		boolean isSaved = false;
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			session.get(OperatorCircleMapping.class, clipStatusMapping.getCompositeKey().getOperatorCircleMapping().getId());
			session.save(clipStatusMapping);
			tx.commit();
			session.flush();
			isSaved = true;
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			logger.error("Exception Occured: "+he,he);
		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			logger.error("Exception Occured: "+e,e);
		} finally {
			if(session != null) {
				session.clear();
				session.close();
			}
		}
		
		logger.info("ClipStatusMapping saved: "+isSaved);
		return isSaved;
	}

	@Override
	public boolean updateClipStatusMapping(ClipStatusMapping clipStatusMapping) {


		logger.info("Updating ClipStatusMapping");
		boolean isUpdated = false;
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			session.update(clipStatusMapping);
			tx.commit();
			session.flush();
			isUpdated = true;
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			logger.error("Exception Occured: "+he,he);
		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			logger.error("Exception Occured: "+e,e);
		} finally {
			if(session != null) {
				session.clear();
				session.close();
			}
		}
		
		logger.info("ClipStatusMapping updated: "+isUpdated);
		return isUpdated;
	
	}

	@Override
	public List<ClipStatusMapping> getClipStatusMappingByStatus(int status) {

		logger.info("Getting ClipStatusMapping by Status: "+status);
		List<ClipStatusMapping> statusMappings = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(ClipStatusMapping.class);
			criteria.add(Restrictions.eq("status", status));
			statusMappings = criteria.list();
		} catch(HibernateException he) {
			logger.error("Exception Occured: "+he,he);
		} catch(Exception e) {
			logger.error("Exception Occured: "+e,e);
		} finally {
			if(session != null) {
				session.clear();
				session.close();
			}
		}
		logger.info("Returning ClipStatusMapping: "+statusMappings);
		return statusMappings;
	
	}
	

}
