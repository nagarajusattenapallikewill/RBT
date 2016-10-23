package com.onmobile.apps.ringbacktones.v2.dao.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.PropertyValueException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.id.IdentifierGenerationException;

import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.FactoryDao;
/**
 * @author lakka.rameswarareddy
 *<p>To perform basic CURD operation on any table using hibernate.
 * And Get data based on the specific column and delete based on specific column data </p> 
 */
public abstract class FactoryDaoImpl implements FactoryDao{
	/**
	 * Preparing logger object
	 */
	private static Logger logger = Logger.getLogger(FactoryDaoImpl.class);
	
	/**
	 * @param cls	:	<p>This parameter must me entity name</p>
	 * @param rowStartIdxAndCount	:	<p>range from & To </p>
	 * @return		:	<p>List of matching entities</p>
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> findAll(Class<E> cls, final int... rowStartIdxAndCount)
			throws DataAccessException {
		logger.info("finding all " + cls.getName() + " instances");
		Query query = null;
		List<E> results = null;
		Session session = null;
		try {
			final String queryString = "FROM " + cls.getName();
			session = HibernateUtil.getSession();
			query = session.createQuery(queryString);
			if (rowStartIdxAndCount != null && rowStartIdxAndCount.length > 0) {
				int rowStartIdx = Math.max(0, rowStartIdxAndCount[0]);
				if (rowStartIdx > 0) {
					query.setFirstResult(rowStartIdx);
				}

				if (rowStartIdxAndCount.length > 1) {
					int rowCount = Math.max(0, rowStartIdxAndCount[1]);
					if (rowCount > 0) {
						query.setMaxResults(rowCount);
					}
				}
			}
			results = query.list();

		}catch (Exception exception) {
			logger.error("Error Trace "
					+ ExceptionUtils.getFullStackTrace(exception));
			throw new DataAccessException(exception.getMessage(),
					exception);
		}finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		logger.debug("DB Results:" + results);
		return results;
	}
	
	/**
	 * 
	 * @param cls	:	<p>This parameter must me entity name</p>
	 * @param id	:	<p>serializable id / primary key </p>
	 * @return		:	<p>Matching entity object for the specific primary key </p>
	 * @throws DataAccessException
	 */
	@Override
	public <E> E findEntityById(Class<E> cls, Serializable id)
			throws DataAccessException {
		E entity = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			entity = (E) session.get(cls, id);
		} catch (Exception exception) {
			logger.error("Error Trace "
					+ ExceptionUtils.getFullStackTrace(exception));
			throw new DataAccessException(exception.getMessage(),
					exception);
		}finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}

		return entity;
	}
	
	/**
	 * 
	 * @param object	:	<p>This parameter must me entity with primary key, we can perform both save and update operation</p>
	 * @throws DataAccessException
	 */
	@Override
	public <E> void saveOrUpdateEntity(E object) throws DataAccessException {
		Session session = null;
		Transaction tx = null;	
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			session.saveOrUpdate(object);
			tx.commit();
		}catch(PropertyValueException exception){
			if(tx != null)
				tx.rollback();
			logger.error("Error Trace "	+ ExceptionUtils.getFullStackTrace(exception));
			throw new DataAccessException("PROPERTY VALUE EXCEPTION, PROPERTY REFERANCES A NULL OR TRANSIENT VALUEL",exception);
		}catch(IdentifierGenerationException exception){
			if(tx != null)
				tx.rollback();
			logger.error("Error Trace "	+ ExceptionUtils.getFullStackTrace(exception));
			throw new DataAccessException("IDENTIFIER GENERATOR EXCEPTION, PK IS NOT PROPERTY MIGHT BE NULL",exception);
		}catch(ConstraintViolationException exception){
			if(tx != null)
				tx.rollback();
			logger.error("Error Trace "	+ ExceptionUtils.getFullStackTrace(exception));
			throw new DataAccessException("CONSTARIN VIOLATION, PK IS NOT PROPERTY MIGHT BE NULL",exception);
		}catch (Exception exception) {
			if(tx != null)
				tx.rollback();
			logger.error("Error Trace "	+ ExceptionUtils.getFullStackTrace(exception));
			throw new DataAccessException(exception.getMessage().toUpperCase(),exception);
		}finally {
			if (session != null) {
				session.flush();
				session.clear();
				session.close();
			}
		}
	}

	/**
	 * 
	 * @param cls	:	<p>This parameter must me entity name</p>
	 * @param id	:	<p>serializable id / primary key </p>
	 * @return		:	<p>if deletion is success then returns true, otherwise false</p>
	 * @throws DataAccessException
	 */
	@Override
	public <E> boolean deletEntityById(Class<E> cls, Serializable id)
			throws DataAccessException {
		boolean detelion;
		Session session = null;
		Transaction tx = null;	
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			session.delete(session.load(cls, id));
			detelion = true;
			tx.commit();
		} catch (Exception exception) {
			if(tx!=null)
				tx.rollback();
			detelion = false;
			logger.error("Error Trace "
					+ ExceptionUtils.getFullStackTrace(exception));
			throw new DataAccessException(exception.getMessage(),
					exception);
		}finally {
			if (session != null) {
				session.flush();
				session.clear();
				session.close();
			}
		}
		return detelion;

	}
	/**
	 * 
	 * @param cls			:	<p>This parameter must me entity name</p>
	 * @param propertyName	:	<p>key is the propertyName on which property conditions needs to apply, value is the Object type we can pass any type of data</p>
	 * @param rowStartIdxAndCount	:	<p>Specific range values we can pass</p>
	 * @return				:	<p>List of matching entities</p>
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> findByProperty(Class<E> cls,
			Map<String, Object> propertyNames, int... rowStartIdxAndCount)
			throws DataAccessException {
		Query query = null;
		logger.info("finding " + cls.getName() + " instance with property: "
				+ propertyNames.entrySet());
		Session session = null;
		List<E> result =null;
		try {
			session = HibernateUtil.getSession();
			String queryString = "FROM " + cls.getName() + " WHERE ";
			Iterator<Map.Entry<String, Object>> itr1 = propertyNames.entrySet()
					.iterator();
			while (itr1.hasNext()) {
				Map.Entry<String, Object> entry = itr1.next();
				queryString += " " + entry.getKey() + " = '"+ entry.getValue()+"'";
			}
			logger.info("CREATE QUERY :" + queryString);
			query = session.createQuery(queryString);
			if (rowStartIdxAndCount != null && rowStartIdxAndCount.length > 0) {
				int rowStartIdx = Math.max(0, rowStartIdxAndCount[0]);
				if (rowStartIdx > 0) {
					query.setFirstResult(rowStartIdx);
				}

				if (rowStartIdxAndCount.length > 1) {
					int rowCount = Math.max(0, rowStartIdxAndCount[1]);
					if (rowCount > 0) {
						query.setMaxResults(rowCount);
					}
				}
			}
			logger.info("FINAL QUERY :" + queryString);
			result = query.list();
		} catch (Exception exception) {
			logger.error("Error Trace "
					+ ExceptionUtils.getFullStackTrace(exception));
			throw new DataAccessException(exception.getMessage(),
					exception);
		}finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		return result;
		
	}

	/**
	 * 
	 * @param cls	:	<p>This parameter must me entity name</p>
	 * @param propertyNames	:	<p>key is the propertyName on which property conditions needs to apply, value is the Object type we can pass any type of data</p>
	 * @return		:	<p>No of records deleted in database</p>
	 * @throws DataAccessException
	 */
	@Override
	public <E> int deleteByProperty(Class<E> cls,
			Map<String, Object> propertyNames)
			throws DataAccessException {
		Query query = null;
		logger.info("finding " + cls.getName() + " instance with property: "
				+ propertyNames.entrySet());
		Session session = null;
		Transaction tx = null;	
		int delCount =0;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			String queryString = "DELETE FROM " + cls.getName() + " WHERE ";
			Iterator<Map.Entry<String, Object>> itr1 = propertyNames.entrySet().iterator();
			while (itr1.hasNext()) {
				Map.Entry<String, Object> entry = itr1.next();
				queryString += " " + entry.getKey() + " LIKE '"	+ entry.getValue()+"'";
				break;
			}
			query = session.createQuery(queryString);
			delCount = query.executeUpdate();
			tx.commit();
			logger.info("QUERY :" + queryString);
		} catch (Exception exception) {
			if(tx != null)
				tx.rollback();
			logger.error("Error Trace "
					+ ExceptionUtils.getFullStackTrace(exception));
			throw new DataAccessException(exception.getMessage(),
					exception);
		}finally {
			if (session != null) {
				session.flush();
				session.clear();
				session.close();
			}
		}
		return delCount;
		
	}
}
