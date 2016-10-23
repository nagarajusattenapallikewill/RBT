package com.onmobile.apps.ringbacktones.genericcache.datatype;

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
public class GCInteger extends IntegerType
{
	private static final long serialVersionUID = -1305657016904820303L;

	private static final Logger log = Logger.getLogger(GCString.class);

	/* (non-Javadoc)
	 * @see org.hibernate.type.IntegerType#get(java.sql.ResultSet, java.lang.String)
	 */
	@Override
	public Object get(ResultSet rs, String name) throws SQLException
	{
		String value = rs.getString(name);
		if (null == value)
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
					if (null != val)
						break;
				}
			}
			catch (SQLException e)
			{
				log.error("Error while resetting the invalid value of column " + name + " value " + value, e);
			}
			Integer zero = new Integer(0);
			log.warn("Overriding column " + name + " value " + value + " with " + zero);
			return zero;
		}
		try
		{
			return new Integer(value);
		}
		catch (NumberFormatException e)
		{
			log.error("Invalid entry in " + name + " value " + value);
			throw e;
		}
	}
}
