package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;

public class DoubleConfirmationDaemonOzonized extends Ozonized {

	private static Logger logger = Logger.getLogger(DoubleConfirmationDaemonOzonized.class);
	private static final String COMPONENT_NAME = "DoubleConfirmationDaemon";

	private DoubleConfirmationDaemon doubleConfirmationDaemon = null;
	
	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent() {

		try {
			logger.info("Starting DoubleConfirmationDaemon..");
			doubleConfirmationDaemon = new DoubleConfirmationDaemon();
			doubleConfirmationDaemon.start();
			logger.info("Successfully started DoubleConfirmationDaemon..");
		} catch (Exception e) {
			logger.error(
					"Unable to start DoubleConfirmationDaemon. Exception: "
							+ e.getMessage(), e);
			return JAVA_COMPONENT_FAILURE;
		} catch (Throwable e) {
			logger.error(
					"Unable to start DoubleConfirmationDaemon. Throwable: "
							+ e.getMessage(), e);
			return JAVA_COMPONENT_FAILURE;
		}

		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent() {
		if(null != doubleConfirmationDaemon) {
			logger.info("Stopping DoubleConfirmationDaemon..");
			doubleConfirmationDaemon.stopThread();
		}
	}

}
