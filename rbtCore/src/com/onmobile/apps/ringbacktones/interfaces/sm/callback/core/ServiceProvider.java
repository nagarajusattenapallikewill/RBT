package com.onmobile.apps.ringbacktones.interfaces.sm.callback.core;

import com.onmobile.apps.ringbacktones.service.configuration.ConfigurationService;
import com.onmobile.apps.ringbacktones.service.dblayer.DBService;

public class ServiceProvider
{
	
	static DBService dbService;
	static ConfigurationService configurationService;
	
	/**
	 * @return the dbService
	 */
	public static DBService getDbService()
	{
		if(dbService != null)
			return dbService;
		synchronized (ServiceProvider.class)
		{
			if(dbService != null)
				return dbService;
			dbService = new DBService();
			return dbService;
		}
	}
	
	/**
	 * @param dbService the dbService to set
	 */
	public static void setDbService(DBService dbService)
	{
		ServiceProvider.dbService = dbService;
	}
	
	/**
	 * @return the configurationService
	 */
	public static ConfigurationService getConfigurationService()
	{
		if(configurationService != null)
			return configurationService;
		synchronized (ServiceProvider.class)
		{
			if(configurationService != null)
				return configurationService;
			configurationService = new ConfigurationService();
			return configurationService;
		}
	}

	/**
	 * @param configurationService the configurationService to set
	 */
	public static void setConfigurationService(ConfigurationService configurationService)
	{
		ServiceProvider.configurationService = configurationService;
	}
	
}
