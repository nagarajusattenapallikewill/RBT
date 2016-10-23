package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;

class RBTAnnouncementDaemonThread extends Thread {
	private static Logger logger = Logger.getLogger(RBTAnnouncementDaemonThread.class);

	private final String _threadType;
	private RBTAnnouncementDaemon _mainAnnouncementDaemonThread;

	protected RBTAnnouncementDaemonThread(String threadType, RBTAnnouncementDaemon mainDaemonThread) {
		logger.info("RBTAnn::creating thread with type->" + threadType);
		_threadType = threadType;
		_mainAnnouncementDaemonThread = mainDaemonThread;
	}

	public void run() {
		logger.info("RBTAnn::starting thread->" + this.getName());
		while (_mainAnnouncementDaemonThread != null && _mainAnnouncementDaemonThread.isAlive()) {
			try {
				SubscriberAnnouncements announcement = _mainAnnouncementDaemonThread.getAnnouncement(_threadType);
				if (announcement != null)
					_mainAnnouncementDaemonThread.processAnnouncement(announcement, _threadType);
			}
			catch (InterruptedException e) {
				logger.error("", e);
				break;
			}
			catch (Throwable e) {
				logger.error("", e);
			}
		}
		logger.info("RBTAnn::exiting thread->" + this.getName());
	}
}