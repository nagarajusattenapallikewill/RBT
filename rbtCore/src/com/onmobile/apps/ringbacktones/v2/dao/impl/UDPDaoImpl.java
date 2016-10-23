package com.onmobile.apps.ringbacktones.v2.dao.impl;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IUDPDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPBean;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPContentMap;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPResponseBeanDO;
import com.onmobile.apps.ringbacktones.v2.dao.constants.DAOConstants;
import com.onmobile.apps.ringbacktones.v2.dao.util.HibernateUtil;

@Repository(value=BeanConstant.UDP_DAO_IMPL)
public class UDPDaoImpl implements IUDPDao, DAOConstants{

	private static Logger logger = Logger.getLogger(UDPDaoImpl.class);
	
	public enum UDPType{SONG,RBTUGC};

	@Override
	public UDPBean createUDP(UDPBean udpBean) throws DataAccessException {
		logger.info("Creating UDP: "+udpBean);
		if (udpBean == null)
			throw new DataAccessException(UDP_OBJECT_NULL);

		udpBean.setCreationTime(new Date());
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			int udpId = (Integer) session.save(udpBean);
			udpBean.setUdpId(udpId);
			tx.commit();
			session.flush();
			return udpBean;
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error("Exception Occured on creating UDP: "+he, he);
			throw new DataAccessException(DUPLICATE_PK,he);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	@Override
	public UDPBean updateUDP(UDPBean udpBean) throws DataAccessException {
		logger.info("Updating UDP: "+udpBean);
		if (udpBean == null)
			throw new DataAccessException(UDP_OBJECT_NULL);
		udpBean.setUpdationTime(new Date());
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			session.update(udpBean);
			tx.commit();
			session.flush();
			return udpBean;
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error("Exception Occured on Updating UDP: "+he,he);
			throw new DataAccessException(UDP_TRANS_ERROR,he);
		} catch (Exception e) {
			logger.error("Exception Occured on Updating UDP: "+e,e);
			throw new DataAccessException(INVALID_UDP_ID);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	@Override
	public boolean deleteUDP(int udpId) throws DataAccessException {
		logger.info("Deleting UDP: "+udpId);

		if(udpId <= 0)
			throw new DataAccessException(INVALID_UDP_ID);
		Session session = null;
		Transaction tx = null;
		try {
			try{
			 removeContentUDP(udpId, -1);
			}catch(DataAccessException e){
				logger.error("Content not found in clip map table");
			}
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			UDPBean udpBean = (UDPBean) session.load(UDPBean.class, udpId);
			session.delete(udpBean);
			tx.commit();
			session.flush();
			return true;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception Occured on Deleting UDP: "+he,he);
			throw new DataAccessException(INVALID_UDP_ID,he);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}		
	}

	@Override
	public List<UDPBean> getAllUDP(String msisdn,int pageNum, int pageSize) throws DataAccessException {
		logger.info("Getting All UDP Details for: "+msisdn);
		List<UDPBean> udpDetails = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			String msisdnName = "subscriberId";
			Criteria udpCriteria = session.createCriteria(UDPBean.class);
			udpCriteria.add(Restrictions.eq(msisdnName, msisdn));	
			
			if (pageNum != -1) {
				int offSet = pageSize*(pageNum-1);
				udpCriteria = udpCriteria.setFirstResult(offSet)
										 .setMaxResults(pageSize);
			}
			udpDetails = udpCriteria.list();
			return udpDetails;
		} catch (HibernateException he) {

			logger.error("Exception Occured on Getting All UDP Details: "+he,he);
			throw new DataAccessException(UDP_TRANS_ERROR);

		} catch (Exception e) {
			logger.error("Exception Occured on Getting All UDP Details: "+e,e);
			throw new DataAccessException(INVALID_UDP_ID);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	@Override
	public UDPBean getUDPById(int udpId) throws DataAccessException {
		logger.info("Getting UDP By Id: "+udpId);
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			UDPBean udpBean = (UDPBean) session.load(UDPBean.class, udpId);
			udpBean.getUdpId();
			return udpBean;
		} catch (HibernateException he) {
			logger.error("Exception Occured on getting UDP: "+he,he);
			throw new DataAccessException(INVALID_UDP_ID);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}

	}	
	
	
	@Override
	public UDPResponseBeanDO getUDPById(int udpId,boolean isContentRequired) throws DataAccessException {
		logger.info("Getting UDP By Id: "+udpId);
		Session session = null;
		UDPResponseBeanDO udpResponseBeanDO = null;
		try {
			UDPBean udpBean = getUDPById(udpId);
			session = HibernateUtil.getSession();
			Criteria udpCriteria = session.createCriteria(UDPContentMap.class);
			udpCriteria.add(Restrictions.eq("contentKeys.udpBean.udpId", udpId));
			List<UDPContentMap> contentMaps = udpCriteria.list();
			udpResponseBeanDO = new UDPResponseBeanDO();
			udpResponseBeanDO.setUdpBean(udpBean);
			udpResponseBeanDO.setUdpContentMaps(contentMaps);
			return udpResponseBeanDO;			
			
		} catch (HibernateException he) {
			logger.error("Exception Occured on getting UDP: "+he,he);
			throw new DataAccessException(INVALID_UDP_ID);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}

	}	

	@Override
	public boolean addContentToUDP(UDPContentMap udpContentMap,String msisdn,boolean isUDPActive)
			throws DataAccessException {
		long toneId = udpContentMap.getContentKeys().getClipId();
		int udpId = udpContentMap.getContentKeys().getUdpBean().getUdpId();
		logger.info("Adding Content: "+toneId+" to UDP: "+udpId+" for: "+msisdn);
		Session session = null;
		Transaction tx = null;		
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			session.save(udpContentMap);
			tx.commit();
			if (isUDPActive) {

			}
			return true;
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(DUPLICATE_PK,he);			
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}


	@Override
	public boolean removeContentUDP(int udpId, int toneId) throws DataAccessException {
		logger.info("Removing Content: "+toneId+" from UDP: "+udpId);
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			String deleteHql = "Delete UDPContentMap where contentKeys.udpBean.udpId = :udpId";
			if (toneId != -1)
				deleteHql = deleteHql +" AND contentKeys.clip.clipId = :toneId";
			Query query = session.createQuery(deleteHql);
			query.setParameter("udpId", udpId);
			
			if (toneId != -1)
				query.setParameter("toneId", toneId);
			int result = query.executeUpdate();
			if(result <= 0){
				throw new DataAccessException(UDP_CONTENT_NOT_FOUND);
			}
			tx.commit();
			session.flush();
			return true;
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(UDP_CONTENT_NOT_FOUND);

		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(UDP_CONTENT_NOT_FOUND);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}
	
	
	@Override
	public boolean removeContentUDP(String subscriberId, long toneId, UDPType type) throws DataAccessException {
		
		if(type == null || type.toString().isEmpty())
			throw new DataAccessException(TYPE_REQUIRED);
		
		Session session = null;
		Transaction tx = null;
		Query query = null;
		try {
			
			String deleteQuery = "DELETE clipMap.* FROM rbt_udp AS udp JOIN rbt_udp_clip_map AS clipMap ON udp.UDP_ID = clipMap.UDP_ID AND "
					+ "udp.subscriber_id = :subId AND clipMap.clip_id= :toneId AND clipMap.Type = :type ";
			session = HibernateUtil.getSession();
			query = session.createSQLQuery(deleteQuery);
			query.setParameter("subId", subscriberId);
			query.setParameter("toneId", toneId);
			query.setParameter("type", type.toString());
			tx = session.beginTransaction();
			int result = query.executeUpdate();
			if(result <= 0){
				throw new DataAccessException(UDP_CONTENT_NOT_FOUND);
			}
			session.flush();
			tx.commit();
			return true;
		} catch (HibernateException he) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(UDP_CONTENT_NOT_FOUND);
		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			throw new DataAccessException(UDP_CONTENT_NOT_FOUND);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		
		
	}

	@Override
	public boolean isValidUDPId(int udpId , String msisdn) throws DataAccessException {
		logger.info("Validating UDP ID: "+udpId);
		if (udpId <= 0)
			throw new DataAccessException(INVALID_UDP_ID);
		UDPBean udpBean = null;
		Session session = null;		
		try {
			session = HibernateUtil.getSession();			
			Criteria udpCriteria = session.createCriteria(UDPBean.class);
			udpCriteria.add(Restrictions.eq("subscriberId", msisdn))
					   .add(Restrictions.eq("udpId", udpId));
			//RBT-15911 IndexOutOfBoundsException error response while selecting invalid udp id
			List list = udpCriteria.list();
			if(list == null || list.size() == 0){
				throw new DataAccessException(INVALID_UDP_ID);
			}
			udpBean = (UDPBean) list.get(0);
			/*UDPBean udpBean = (UDPBean) session.load(UDPBean.class, udpId);*/
			return udpBean.getUdpId() == udpId;
		} catch (HibernateException he ) {
			logger.error("Exception Occured on validating UDPId: "+he,he);
			throw new DataAccessException(INVALID_UDP_ID);
		}catch(DataAccessException de) {
			logger.error("Exception Occured on validating UDPId: "+de);
			throw de;
		}catch (Throwable th) {
			logger.error("Exception Occured on validating UDPId: "+th,th);
			throw new DataAccessException(INVALID_UDP_ID);
		}
		
		finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}

	}

	@Override
	public boolean isUDPActive(int udpId) throws DataAccessException {
		logger.info("Validating UDP ID: "+udpId);

		Session session = null;		
		try {
			session = HibernateUtil.getSession();
			UDPBean udpBean = (UDPBean) session.load(UDPBean.class, udpId);
			return udpBean.isSelActivated();
		} catch (HibernateException he ) {
			logger.error("Exception Occured on validating UDPId: "+he,he);
			throw new DataAccessException(INVALID_UDP_ID);
		} catch (Exception e) {
			logger.error("Exception Occured on validating UDPId: "+e,e);
			throw new DataAccessException(INVALID_UDP_ID);
		}
		finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}
}
