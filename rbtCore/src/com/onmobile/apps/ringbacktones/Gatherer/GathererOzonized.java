package com.onmobile.apps.ringbacktones.Gatherer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.threadMonitor.ThreadMonitor;
import com.onmobile.apps.ringbacktones.daemons.Ozonized;

public class GathererOzonized extends Ozonized
{
	private static Logger logger = Logger.getLogger(GathererOzonized.class);

	private static final String COMPONENT_NAME = "RBTGatherer";

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
	public int startComponent()
	{
		RBTGatherer.m_rbtGatherer = new RBTGatherer();
		RBTGatherer.m_rbtGatherer.setThreadName("RBTGathererMain");
		ThreadMonitor.getMonitor().register(RBTGatherer.m_rbtGatherer);
		try
		{
			if (RBTGatherer.m_rbtGatherer.init())
				RBTGatherer.m_rbtGatherer.start();
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{
		RBTGatherer.m_rbtGatherer.stopThread();
	}
}
