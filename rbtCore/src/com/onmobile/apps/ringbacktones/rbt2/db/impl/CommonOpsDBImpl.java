package com.onmobile.apps.ringbacktones.rbt2.db.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.onmobile.common.db.OnMobileDBServices;

public abstract class CommonOpsDBImpl {
	
	private Logger logger = Logger.getLogger(CommonOpsDBImpl.class);
	
	 protected void closeStatementAndRS(Statement stmt, ResultSet rs, Connection connection) 
	    {
	    	try
			{
				if(rs !=null)
					rs.close();
			}
			catch(Throwable t)
			{
				logger.error("Exception in closing db resultset " + t,t);
			}
			try
			{
				if(stmt !=null)
					stmt.close();
			}
			catch(Throwable t)
			{
				logger.error("Exception in closing db statement " + t, t);
			}	
			
			try {
				if (connection != null)
					OnMobileDBServices.releaseConnection(connection);
			} catch (Throwable t) {
				logger.error("Exception in closing db connection " + t, t);
			}
		}

	 
	 protected int executeUpdateQuery(Connection conn, String query) {
			int updateCount = 0;
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
				updateCount = stmt.getUpdateCount();
			}
			catch(SQLException se)
			{
				logger.error("", se);
				return updateCount;
			}
			finally
			{
				closeStatementAndRS(stmt, null,conn);
			}
			return updateCount;
		}


}
