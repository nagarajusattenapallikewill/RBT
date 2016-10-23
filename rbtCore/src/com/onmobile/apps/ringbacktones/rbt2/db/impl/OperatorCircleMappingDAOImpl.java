package com.onmobile.apps.ringbacktones.rbt2.db.impl;

import java.util.List;
import java.util.Set;

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
import com.onmobile.apps.ringbacktones.rbt2.db.IOperatorCircleMappingDAO;
import com.onmobile.apps.ringbacktones.v2.dao.bean.OperatorCircleMapping;
import com.onmobile.apps.ringbacktones.v2.dao.util.HibernateUtil;

@Repository(value = BeanConstant.OPERATOR_CIRCLE_MAPPING_DAO)
@Lazy(value=true)
public class OperatorCircleMappingDAOImpl implements IOperatorCircleMappingDAO {

	private static Logger logger = Logger.getLogger(OperatorCircleMappingDAOImpl.class);
	
	@Override
	public OperatorCircleMapping getOprtrCircleMappingByOperatorAndCircle(
			String operatorName, String circleId) {
		logger.info("Getting Operator Circle Mapping for Operator: "+operatorName+" and CircleId: "+circleId);
		
		OperatorCircleMapping circleMapping = null;
		Session session = null;		
		try{
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(OperatorCircleMapping.class);
			criteria.add(Restrictions.eq("operatorName", operatorName))
					.add(Restrictions.eq("circleId", circleId));
			circleMapping = (OperatorCircleMapping) criteria.uniqueResult();
			
		} catch(HibernateException he) {
			logger.error("Exception Occured: "+he, he);
		} catch(Exception e) {
			logger.error("Exception Occured: "+e, e);
		} finally {
			if(session != null) {
				session.clear();
				session.close();
			}
		}
		logger.info("Returning OperatorCircleMapping: "+circleMapping);
		return circleMapping;
	}

	@Override
	public boolean saveOperatorCircleMapping(
			OperatorCircleMapping operatorCircleMapping) {
		logger.info("Saving OperatorCircleMapping");
		boolean isSaved = false;
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			session.save(operatorCircleMapping);
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
	public List<OperatorCircleMapping> getOperatorCircleMappingList(Set<Integer> ids) {

		logger.info("Getting Operator Circle Mapping for ids: "+ids);
		
		List<OperatorCircleMapping> circleMappings = null;
		Session session = null;
		String hql = "From OperatorCircleMapping where id in (:ids)";
		try{
			session = HibernateUtil.getSession();
			Query query = session.createQuery(hql);
			query.setParameterList("ids", ids);
			circleMappings = query.list();			
		} catch(HibernateException he) {
			logger.error("Exception Occured: "+he, he);
		} catch(Exception e) {
			logger.error("Exception Occured: "+e, e);
		} finally {
			if(session != null) {
				session.clear();
				session.close();
			}
		}
		logger.info("Returning OperatorCircleMapping: "+circleMappings);
		return circleMappings;
	
	}

	@Override
	public List<OperatorCircleMapping> getOperatorCircleMapping() {

		logger.info("Getting all the records from Operator Circle Mapping  table");
		
		List<OperatorCircleMapping> circleMappings = null;
		Session session = null;		
		try{
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(OperatorCircleMapping.class);			
			circleMappings = criteria.list();			
		} catch(HibernateException he) {
			logger.error("Exception Occured: "+he, he);
		} catch(Exception e) {
			logger.error("Exception Occured: "+e, e);
		} finally {
			if(session != null) {
				session.clear();
				session.close();
			}
		}
		logger.info("Returning OperatorCircleMapping list");
		return circleMappings;
	}

}
