package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Log4jErrorConnector;
import com.onmobile.apps.ringbacktones.common.Log4jSysOutConnector;

public class FeedClipDownloadNuploaderOzonized extends Ozonized
{
	private static Logger logger = Logger
			.getLogger(FeedClipDownloadNuploaderOzonized.class);

	private static final String COMPONENT_NAME = "FeedClipDownloadNuploader";

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent()
	{
		try
		{
			@SuppressWarnings("unused")
			Log4jErrorConnector r = new Log4jErrorConnector();
			@SuppressWarnings("unused")
			Log4jSysOutConnector l = new Log4jSysOutConnector();
			FeedClipDownloadNuploader.feedClipDownloadNuploader = new FeedClipDownloadNuploader();
			logger.info("starting the thread");
			FeedClipDownloadNuploader.feedClipDownloadNuploader.start();
			logger.info("started the thread");
		}
		catch (Throwable e)
		{
			logger.error("", e);
			return JAVA_COMPONENT_FAILURE;
		}
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{
		try
		{
			FeedClipDownloadNuploader.feedClipDownloadNuploader.stopThread();
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}
}
