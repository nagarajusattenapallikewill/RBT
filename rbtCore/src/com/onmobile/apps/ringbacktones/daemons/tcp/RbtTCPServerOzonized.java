/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtTCPServerOzonized extends Ozonized
{
	private static final String COMPONENT_NAME = "RbtTCPServer";

	private RbtTCPServer rbtTCPServer = null;

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getComponentName()
	 */
	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#startComponent()
	 */
	@Override
	public int startComponent()
	{
		rbtTCPServer = new RbtTCPServer();
		rbtTCPServer.start();
		return JAVA_COMPONENT_SUCCESS;
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#stopComponent()
	 */
	@Override
	public void stopComponent()
	{
		if (rbtTCPServer != null)
			rbtTCPServer.stop();
	}
}
