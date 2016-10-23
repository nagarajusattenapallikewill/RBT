package com.onmobile.apps.ringbacktones.Gatherer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;

public class ReporterOzonized extends Ozonized
{
	private static Logger logger = Logger.getLogger(ReporterOzonized.class);
	private static final String COMPONENT_NAME = "RBTReporter";
	private static RBTReporter rbtReporter = null;

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent()
	{
		RBTReporter rbtReporter = new RBTReporter();

		try
		{
			rbtReporter.start();
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
		rbtReporter.stopThread();
	}
}
