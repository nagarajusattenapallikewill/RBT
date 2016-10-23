/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vinayasimha.patil
 *
 */
public class WebServiceContext extends HashMap<String, Object>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public WebServiceContext()
	{
		super();
	}

	public WebServiceContext(Map<String, ?> map)
	{
		super(map);
	}

	/**
	 * @param key
	 * @return the string stored in taskSession
	 */
	public String getString(String key)
	{
		return ((String) get(key));
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends String, ? extends Object> map)
	{
		if (map != null)
			super.putAll(map);
	}
}
