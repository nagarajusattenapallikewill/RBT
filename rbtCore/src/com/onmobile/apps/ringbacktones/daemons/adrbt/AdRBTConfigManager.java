package com.onmobile.apps.ringbacktones.daemons.adrbt;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author sridhar.sindiri
 *
 */
public class AdRBTConfigManager 
{
	private ResourceBundle rb = null;
	private static AdRBTConfigManager instance = new AdRBTConfigManager();

	/**
	 * 
	 */
	private AdRBTConfigManager() 
	{
		try
		{
			rb = ResourceBundle.getBundle("adrbt_prompts_upload_config");
		}
		catch (MissingResourceException e)
		{
			AdRBTPromptUploadDaemon.runDaemon = false;
		}
	}

	/**
	 * @return the instance of AdRBTConfigManager
	 */
	public static AdRBTConfigManager getInstance() 
	{
		return instance;
	}

	/**
	 * @param key
	 * @return
	 */
	public String getParameter(String key) 
	{
		return rb.getString(key);
	}
}
