package com.onmobile.apps.ringbacktones.v2.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
/**
 * 
 * @author lakka.rameswarareddy
 *<p>To perform basic CURD operation on any table using hibernate.
 * And Get data based on the specific column and delete based on specific column data </p> 
 */
public interface FactoryDao {
	/**
	 * @param cls	:	<p>This parameter must me entity name</p>
	 * @param rowStartIdxAndCount	:	<p>range from & To </p>
	 * @return		:	<p>List of matching entities</p>
	 * @throws DataAccessException
	 */
	public <E> List<E> findAll(Class<E> cls, int... rowStartIdxAndCount)
			throws DataAccessException;

	/**
	 * 
	 * @param cls	:	<p>This parameter must me entity name</p>
	 * @param id	:	<p>serializable id / primary key </p>
	 * @return		:	<p>Matching entity object for the specific primary key </p>
	 * @throws DataAccessException
	 */
	public <E> E findEntityById(Class<E> cls, Serializable id)
			throws DataAccessException;

	/**
	 * 
	 * @param cls			:	<p>This parameter must me entity name</p>
	 * @param propertyName	:	<p>key is the propertyName on which property conditions needs to apply, value is the Object type we can pass any type of data</p>
	 * @param rowStartIdxAndCount	:	<p>Specific range values we can pass</p>
	 * @return				:	<p>List of matching entities</p>
	 * @throws DataAccessException
	 */
	public <E> List<E> findByProperty(Class<E> cls,
			Map<String, Object> propertyName, int... rowStartIdxAndCount)
			throws DataAccessException;
	
	/**
	 * 
	 * @param object	:	<p>This parameter must me entity with primary key, we can perform both save and update operation</p>
	 * @throws DataAccessException
	 */
	public <E> void saveOrUpdateEntity(E object)
			throws DataAccessException;

	/**
	 * 
	 * @param cls	:	<p>This parameter must me entity name</p>
	 * @param id	:	<p>serializable id / primary key </p>
	 * @return		:	<p>if deletion is success then returns true, otherwise false</p>
	 * @throws DataAccessException
	 */
	public <E> boolean deletEntityById(Class<E> cls, Serializable id)
			throws DataAccessException;

	/**
	 * 
	 * @param cls	:	<p>This parameter must me entity name</p>
	 * @param propertyNames	:	<p>key is the propertyName on which property conditions needs to apply, value is the Object type we can pass any type of data</p>
	 * @return		:	<p>No of records deleted in database</p>
	 * @throws DataAccessException
	 */
	public <E> int deleteByProperty(Class<E> cls, Map<String, Object> propertyNames)
			throws DataAccessException;
}
