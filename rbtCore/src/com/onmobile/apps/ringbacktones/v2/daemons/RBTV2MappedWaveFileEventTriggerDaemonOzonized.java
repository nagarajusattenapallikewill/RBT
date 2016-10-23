package com.onmobile.apps.ringbacktones.v2.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;

public class RBTV2MappedWaveFileEventTriggerDaemonOzonized extends Ozonized {

	private static Logger logger = Logger
			.getLogger(RBTV2MappedWaveFileEventTriggerDaemonOzonized.class);

	private RBTV2MappedWaveFileEventTriggerDaemon rbtV2WavFileEventTriggerDaemon = null;
	private static final String COMPONENT_NAME = "RBTV2MappedWaveFileEventTriggerDaemonOzonized";

	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent() {
		try {
			rbtV2WavFileEventTriggerDaemon = RBTV2MappedWaveFileEventTriggerDaemon
					.getInstance();
			rbtV2WavFileEventTriggerDaemon
					.setName("RBTV2MAPPED_WAVFILE_EVENT_TRIGGER_DAEMON_OZONIFIED");
			rbtV2WavFileEventTriggerDaemon.start();
			return JAVA_COMPONENT_SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
		}

		return JAVA_COMPONENT_FAILURE;
	}

	@Override
	public void stopComponent() {
		rbtV2WavFileEventTriggerDaemon.stopThread();
	}

}
