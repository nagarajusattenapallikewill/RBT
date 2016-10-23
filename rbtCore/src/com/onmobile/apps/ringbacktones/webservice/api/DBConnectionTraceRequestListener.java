package com.onmobile.apps.ringbacktones.webservice.api;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import com.onmobile.common.db.OnMobileDBServices;

/**
 * Application Lifecycle Listener implementation class
 * DBConnectionTraceRequestListener
 */
public class DBConnectionTraceRequestListener implements ServletRequestListener
{
	/**
	 * Default constructor.
	 */
	public DBConnectionTraceRequestListener()
	{

	}

	/**
	 * @see ServletRequestListener#requestInitialized(ServletRequestEvent)
	 */
	public void requestInitialized(ServletRequestEvent servletRequestEvent)
	{

	}

	/**
	 * @see ServletRequestListener#requestDestroyed(ServletRequestEvent)
	 */
	public void requestDestroyed(ServletRequestEvent servletRequestEvent)
	{
		OnMobileDBServices.trackLeaksAndReleaseConnections();
	}
}
