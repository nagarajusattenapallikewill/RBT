package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Log4jErrorConnector;
import com.onmobile.apps.ringbacktones.common.Log4jSysOutConnector;

public class UGCFileUploaderOzonized extends Ozonized
{
	private static Logger logger = Logger
			.getLogger(UGCFileUploaderOzonized.class);

	private static final String COMPONENT_NAME = "UGCFileUploadManager";

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent()
	{
		logger.info("RBT:: Clearing ContentMap");
		logger.info("inside startComponent");
		UGCFileUploader instance = new UGCFileUploader();
		@SuppressWarnings("unused")
		Log4jErrorConnector r = new Log4jErrorConnector();
		@SuppressWarnings("unused")
		Log4jSysOutConnector l = new Log4jSysOutConnector();
		logger.info("going to upload files");
		instance.start();
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{

	}
}
