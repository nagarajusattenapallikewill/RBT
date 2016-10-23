package com.onmobile.apps.ringbacktones.common.hibernate;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.type.IntegerType;

/**
 * User defined integer data type for hibernate. In hibernate, if any columns
 * which is treating as primitive data columns having null in DB then an error
 * will be thrown by hibernate saying null value cannot be assigned to primitive
 * variables. To avoid this, replacing null value with 0.
 * 
 * @author laxmankumar
 */
public class SafeInteger extends IntegerType
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5531624215976508693L;

	private static final Logger logger = Logger.getLogger(SafeInteger.class);

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.type.IntegerType#get(java.sql.ResultSet,
	 * java.lang.String)
	 */
	@Override
	public Object get(ResultSet rs, String name) throws SQLException
	{
		String value = rs.getString(name);
		if (value == null)
		{
			// trying to set rs.wasNull() to false
			// if you don't set this to false the returned value will not be
			// considered by hibernate
			int columnIndex = 1;
			try
			{
				while (true)
				{
					String val = rs.getString(columnIndex++);
					if (val != null)
					{
						// This will make sure that rs.wasNull() will return false
						break;
					}
				}
			}
			catch (SQLException e)
			{
				logger.error("Error while resetting the invalid value of column "
						+ name + " value " + value, e);
			}

			Integer zero = new Integer(0);
			logger.warn("Overriding column " + name + " value " + value + " with "
					+ zero);
			
			return zero;
		}

		try
		{
			return new Integer(value);
		}
		catch (NumberFormatException e)
		{
			logger.error("Invalid entry in " + name + " value " + value);
			throw e;
		}
	}
}
