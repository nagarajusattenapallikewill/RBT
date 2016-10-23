package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Log4jErrorConnector;
import com.onmobile.apps.ringbacktones.common.Log4jSysOutConnector;
import com.onmobile.apps.ringbacktones.daemons.reliance.RelianceDaemonBootstrap;
import com.onmobile.common.cjni.O3InterfaceHelper;

public class RBTDaemonManagerOzonized extends Ozonized
{
	private static Logger logger = Logger
			.getLogger(RBTDaemonManagerOzonized.class);

	private static final String COMPONENT_NAME = "RBTDaemonManager";
	private static O3InterfaceHelper o3InterfaceHelper = null;

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getComponentName()
	 */
	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int initComponent(O3InterfaceHelper o3InterfaceHelper)
	{
		RBTDaemonManagerOzonized.o3InterfaceHelper = o3InterfaceHelper;
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public int startComponent()
	{

		try
		{
			RBTDaemonManager.m_rbtDaemonManager = new RBTDaemonManager();
			@SuppressWarnings("unused")
			Log4jErrorConnector r = new Log4jErrorConnector();
			@SuppressWarnings("unused")
			Log4jSysOutConnector l = new Log4jSysOutConnector();

			logger.info("m_rbtDaemonManager 1 is "
					+ RBTDaemonManager.m_rbtDaemonManager);
			if (RBTDaemonManager.m_rbtDaemonManager.getConfigValues())
			{
				logger.info("m_rbtDaemonManager 2 is "
						+ RBTDaemonManager.m_rbtDaemonManager);
				RBTDaemonManager.m_rbtDaemonManager.start();

				if (RelianceDaemonBootstrap.canStartDeamon())
					RelianceDaemonBootstrap.start();
			}
			else
			{
				logger.info("Error in config parameters. Exiting...");
			}
		}
		catch (Throwable t)
		{
			logger.error("", t);
		}

		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{
		//RBTDaemonManager.stopThread(RBTDaemonManager.m_rbtDaemonManager);
	}

	public static O3InterfaceHelper getO3InterfaceHelper()
	{
		return o3InterfaceHelper;
	}
}
