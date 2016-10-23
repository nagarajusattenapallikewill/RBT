package com.onmobile.apps.ringbacktones.common.hibernate;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.type.StringType;

/**
 * User defined string data type for hibernate. In hibernate, if any
 * primary/composite key columns having null in DB then the row data will be
 * returned as null object in result set. To avoid this, replacing null value
 * with empty string.
 * 
 * @author laxmankumar
 */
public class SafeString extends StringType
{

	private static final long serialVersionUID = -1305657016904820303L;

	private static final Logger log = Logger.getLogger(SafeString.class);

	/*
	 * (non-Javadoc)
	 * @see org.hibernate.type.StringType#get(java.sql.ResultSet,
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
						// This will make sure that rs.wasNull() will return
						// false
						break;
					}
				}
			}
			catch (SQLException e)
			{
				log.error("Error while resetting the invalid value of column "
						+ name + " value " + value, e);
			}

			String empty = new String("");
			log.warn("Overriding column " + name + " value " + value + " with "
					+ empty);

			return empty;
		}

		return value;
	}
}
