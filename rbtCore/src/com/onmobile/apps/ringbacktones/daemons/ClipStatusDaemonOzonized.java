/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;


/**
 * @author rajesh.karavadi
 * @Since Sep 26, 2013
 */
public class ClipStatusDaemonOzonized extends Ozonized {
	
	private static Logger logger = Logger.getLogger(ClipStatusDaemonOzonized.class);
	private static final String COMPONENT_NAME = "ClipStatusDaemon";
	
	private ClipStatusDaemon clipStatusDaemon = null;

	

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.Ozonized#getComponentName()
	 */
	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.Ozonized#startComponent()
	 */
	@Override
	public int startComponent() {
		try {
			logger.info("Starting ClipStatusDaemon..");
			clipStatusDaemon = new ClipStatusDaemon();
			clipStatusDaemon.start();
			logger.info("Successfully started ClipStatusDaemon..");
		} catch (Exception e) {
			logger.error(
					"Unable to start ClipStatusDaemon. Exception: "
							+ e.getMessage(), e);
			return JAVA_COMPONENT_FAILURE;
		} catch (Throwable e) {
			logger.error(
					"Unable to start ClipStatusDaemon. Throwable: "
							+ e.getMessage(), e);
			return JAVA_COMPONENT_FAILURE;
		}

		return JAVA_COMPONENT_SUCCESS;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.Ozonized#stopComponent()
	 */
	@Override
	public void stopComponent() {
		if(null != clipStatusDaemon) {
			logger.info("Stopping ClipStatusDaemon..");
			clipStatusDaemon.stopThread();
		}
	}

}
