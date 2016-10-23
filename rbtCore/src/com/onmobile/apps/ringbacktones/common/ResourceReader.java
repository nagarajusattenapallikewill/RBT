/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 * 
 */
public class ResourceReader
{
	private static Logger logger = Logger.getLogger(ResourceReader.class);

	public static String getString(String resource, String property,
			String defaultValue)
	{
		ResourceBundle resourceBundle = ResourceBundle.getBundle(resource);

		try
		{
			return resourceBundle.getString(property);
		}
		catch (MissingResourceException e)
		{
			if (defaultValue == null)
				logger.error(e.getMessage());
			else
				logger.warn(e.getMessage() + " Returning default value: "
						+ defaultValue);
		}

		return defaultValue;
	}
}
