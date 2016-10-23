/**
 * 
 */
package com.onmobile.apps.ringbacktones.common.hibernate;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

/**
 * @author vinayasimha.patil
 * 
 */
public class CollectionType implements UserType, ParameterizedType
{
	public static final String COLLECTION_TYPE = "collectionType";
	public static final String TYPE = "type";

	private Type collectionType = null;
	private int sqlType = Types.VARCHAR;

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#sqlTypes()
	 */
	@Override
	public int[] sqlTypes()
	{
		return new int[] { sqlType };
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#returnedClass()
	 */
	@Override
	public Class<?> returnedClass()
	{
		return Set.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#equals(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public boolean equals(Object x, Object y)
			throws HibernateException
	{
		if (x == null && y == null)
			return true;
		if (x == null || y == null)
			return false;

		return x.equals(y);
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
	 */
	@Override
	public int hashCode(Object x) throws HibernateException
	{
		if (x == null)
			return 0;

		return x.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet,
	 * java.lang.String[], java.lang.Object)
	 */
	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
			throws HibernateException, SQLException
	{
		Object object = null;

		String value = resultSet.getString(names[0]);
		if (value != null)
		{
			value = value.replaceAll("\"\\[", "[");
			value = value.replaceAll("\\]\"", "]");

			Gson gson = new Gson();
			object = gson.fromJson(value, collectionType);
		}

		return object;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement,
	 * java.lang.Object, int)
	 */
	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index)
			throws HibernateException,
			SQLException
	{
		if (value == null)
		{
			statement.setNull(index, sqlType);
		}
		else
		{
			if (ReflectionUtils.isMapType(collectionType))
			{
				JSONObject jsonObject = new JSONObject((Map<?, ?>) value);
				statement.setString(index, jsonObject.toString());
			}
			else
			{
				JSONArray jsonArray = new JSONArray((Set<?>) value);
				statement.setString(index, jsonArray.toString());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
	 */
	@Override
	public Object deepCopy(Object value) throws HibernateException
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#isMutable()
	 */
	@Override
	public boolean isMutable()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
	 */
	@Override
	public Serializable disassemble(Object value) throws HibernateException
	{
		return (Serializable) value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable,
	 * java.lang.Object)
	 */
	@Override
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException
	{
		return cached;
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#replace(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException
	{
		return original;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.hibernate.usertype.ParameterizedType#setParameterValues(java.util
	 * .Properties)
	 */
	@Override
	public void setParameterValues(Properties parameters)
	{
		String collectionTypeName = parameters.getProperty(COLLECTION_TYPE);

		try
		{
			collectionType = ReflectionUtils.getType(collectionTypeName);
			if (!ReflectionUtils.isCollectionType(collectionType))
			{
				throw new HibernateException(collectionType
						+ " is not Collection Type");
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new HibernateException("Collection class not found", e);
		}

		String type = parameters.getProperty(TYPE);
		if (type != null)
			sqlType = Integer.decode(type);
	}
}
