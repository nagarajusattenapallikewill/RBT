package com.onmobile.apps.ringbacktones.content.database;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.common.cjni.BootStrap;
import com.onmobile.common.db.OnMobileDBServices;

public class DaoManager {
	private String m_dbURL = null;
	private int m_nConn = 4;
	public static String CCCParameterType="CCC";
	private static DaoManager daoManager = null;
	private static String m_class="DaoManager";
	private Hashtable m_subClasses = new Hashtable();
	static final org.apache.log4j.Logger c_logger = org.apache.log4j.Logger.getLogger(DaoManager.class);
	public static synchronized DaoManager init(String dbURL, int nConn)
	{
		String method="init";
		if (daoManager == null && dbURL != null)
		{
			try
			{
				daoManager = new DaoManager();
				daoManager.initialize(dbURL, true, nConn);
			}
			catch (Exception e)
			{
				c_logger.error(method+"->"+"Got Exception while initializing DaoManager", e);
				daoManager = null;
			}
		}
		return daoManager;
	}
	public void initialize(String dbURL, boolean usePool, int nConn)
	throws Exception
	{
		m_dbURL = dbURL;
		m_nConn = nConn;
		checkDBURL();
	}
	private void checkDBURL() throws Exception
	{
		initOzoneConnectionPool(m_dbURL, m_nConn, "rbt", true, "120");
	}
	private static  boolean initOzoneConnectionPool(String dbUrl, int nConn, String poolName, boolean isDefault, String timeOut) {

		String config = getConfigString(dbUrl, nConn, poolName, isDefault, timeOut);
		BootStrap.initDBServices(config);
		return true;
	}
	public static String getConfigString(String dbUrl, int nConn, String poolName, boolean isDefault, String timeOut){
    	String tempConn = "false";
    	String pool = "name =\""+poolName+"\" default=\"true\"";
    	if(!isDefault)
    	{
    		tempConn = "true";
    		pool = "name =\""+poolName+"\"";
    	}
        String config = "<Database enabletempconnections=\""+tempConn+"\">" +
                "<Instance connectionstring=\"" +dbUrl+
                "\" db-pool-max-size=\"" + nConn +
                "\" "+pool+" db-query-timeout-sec=\""+timeOut+"\" thread-affinity=\"true\"/>" +
                "</Database>" ;
        return config;
    }
	
	public Connection getConnection(){
		String method="getConnection";
		try
		{
			return OnMobileDBServices.getDBConnection();
		}
		catch(Throwable e)
		{
			c_logger.info("Exception in getting conenction", e);
		}
		return null;
	}
	
	public boolean releaseConnection(Connection conn)
	{
		
		try
		{
			OnMobileDBServices.releaseConnection(conn);
		}
		catch (Exception e)
		{
			c_logger.info("Exception in releasing conenction", e);
			return false;
		}
		return true;
	}
	/*public Parameters getParameter(String parameterType,String parameterName)
	{
		Connection conn = getConnection();
		if (conn == null)
		{
			return null;
		}
		Parameters parameters = ParametersImpl.getParameter(conn, parameterType,parameterName);
		releaseConnection(conn);
		return parameters;
	}*/
	public Clips getClip(int id)
	{
		Connection conn = getConnection();
		if (conn == null)
		{
			return null;
		}
		Clips clips = null;
		try
		{
			clips = ClipsImpl.getClip(conn, id);
		}
		catch(Throwable e)
		{
			c_logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return clips;
	}
	public String[] getCCCAdvancedRentalValuesDB()
	{
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(CCCParameterType, "CCC_ADVANCE_PACKS");

		if (parameter != null && parameter.getValue() != null)
		{
			StringTokenizer strToken = new StringTokenizer(parameter.getValue(), ",");
			ArrayList values = new ArrayList();
			String tmp=null;
			while (strToken.hasMoreTokens())
			{
				tmp=strToken.nextToken().trim();
				values.add(tmp);
			}
			if (values.size() > 0){
				return (String[]) values.toArray(new String[0]);
			}
		}
		return null;
	}
}
