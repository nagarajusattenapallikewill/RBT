package com.onmobile.apps.ringbacktones.common.hibernate;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.type.StringType;

/**
 * @author vinayasimha.patil
 * 
 */
public class UTF8String extends StringType
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6931709472267440031L;

	private static Logger logger = Logger.getLogger(UTF8String.class);

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.type.StringType#get(java.sql.ResultSet,
	 * java.lang.String)
	 */
	@Override
	public Object get(ResultSet resultSet, String name) throws SQLException
	{
		byte[] utf8bytes = resultSet.getBytes(name);
		String output = null;
		if (utf8bytes != null)
		{
			try
			{
				output = new String(utf8bytes, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				output = new String(utf8bytes);
				logger.error("Exception converting the bytes " + output
						+ " into String ", e);
			}
		}
		else
		{
			logger.error("RBT Text found as null in one or more rows");
		}
		return output;
	}

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.type.StringType#set(java.sql.PreparedStatement,
	 * java.lang.Object, int)
	 */
	@Override
	public void set(PreparedStatement preparedStatement, Object value, int index)
			throws SQLException
	{
		if (value instanceof String)
		{
			if (logger.isDebugEnabled())
				logger.debug("Converting the string " + value + " into bytes");

			try
			{
				preparedStatement.setBytes(index,
						((String) value).getBytes("UTF-8"));
			}
			catch (UnsupportedEncodingException e)
			{
				logger.error("Exception converting the string " + value
						+ " into bytes ", e);
			}
		}
		else
		{
			logger.error("Object value " + value
					+ ". Expected input type is String. Actual type is "
					+ value.getClass().getName());
		}
	}
}
