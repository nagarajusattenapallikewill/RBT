package com.onmobile.apps.ringbacktones.daemons;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.IClipStatusMappingDAO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.PropertyConfig;
import com.onmobile.apps.ringbacktones.rbt2.thread.ProcessingClipTransfer;
import com.onmobile.apps.ringbacktones.rbt2.thread.ThreadExecutor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.dao.bean.ClipStatusMapping;
import com.onmobile.apps.ringbacktones.v2.dao.bean.OperatorCircleMapping;

/**
 * 
 * @author md.alam
 *
 */

public class ClipTransferDaemon implements Runnable {

	private static ClipTransferDaemon clipTransferDaemon = null;
	private static Logger logger = Logger.getLogger(ClipTransferDaemon.class);
	private static boolean runDaemon = true;
	private int sleepTime;
	private ResourceBundle resourceBundle = null;

	private ClipTransferDaemon() {
		ApplicationContext context = new ClassPathXmlApplicationContext("bean_spring.xml");

		PropertyConfig config = (PropertyConfig) ConfigUtil.getBean(BeanConstant.PROPERTY_CONFIG);
		resourceBundle = config.loadBundle("daemonConfig");
		try {
			sleepTime = Integer.parseInt(config.getValueFromResourceBundle(
					resourceBundle, "SLEEP_TIME", "3600000"));
		} catch (Exception e) {
			logger.error("Exception Occured: " + e, e);
		}

	}

	@Override
	public void run() {
		while (runDaemon) {
			logger.info("Starting the Clip Transfer Process!");
			startProcess();
			try {
				logger.info("Going to Sleep for: " + sleepTime + " ms");
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.error("Exception Occured:" + e, e);
			}
		}

	}

	public void startProcess() {
		logger.info("startProcess method invoked");
		try {
			List<ClipStatusMapping> statusMappings = getClipStatusMappingList();
			if (statusMappings != null && !statusMappings.isEmpty()) {
				Map<Integer, OperatorCircleMapping> operetaorCircleMap = ProcessingClipTransfer
						.getOperatorcirclemapbyid();
				if (operetaorCircleMap != null && !operetaorCircleMap.isEmpty()) {
					for (ClipStatusMapping statusMapping : statusMappings) {
						if(statusMapping.getStatus() == 1) {
							logger.info("Clip " + statusMapping.getCompositeKey()
								.getClipId() + " is already transferred");
							continue;
						}
						Clip clip = getClipObj(statusMapping.getCompositeKey()
								.getClipId());
						if (clip == null) {
							logger.info("Clip Id: "
									+ statusMapping.getCompositeKey()
											.getClipId()
									+ " is Invalid, so not transferring to griff");
							continue;
						}
						int operatorId = statusMapping.getCompositeKey()
								.getOperatorCircleMapping().getId();
						OperatorCircleMapping circleMapping = operetaorCircleMap
								.get(operatorId);

						ThreadExecutor executor = (ThreadExecutor) ConfigUtil
								.getBean(BeanConstant.THREAD_EXECUTOR);
						ProcessingClipTransfer clipTransfer = new ProcessingClipTransfer(
								statusMapping, circleMapping, clip);
						logger.info("Invoking Clip Transfer Process for clipId: "
								+ clip.getClipId()
								+ " and operatorId: "
								+ operatorId);
						executor.getExecutor().execute(clipTransfer);

					}
				}
			} else {
				logger.info("No such clip is there in DB to transfer, hence going to sleep for a while");
			}

		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: " + e, e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: " + e, e);
		} catch (Exception e) {
			logger.error("Exception Occured: " + e, e);
			stopThread();
		}

	}

	private List<ClipStatusMapping> getClipStatusMappingList() {
		IClipStatusMappingDAO statusMappingDAO = (IClipStatusMappingDAO) ConfigUtil
				.getBean(BeanConstant.CLIP_STATUS_MAPPING_DAO);
		List<ClipStatusMapping> statusMappings = statusMappingDAO
				.getClipStatusMappingByStatus(0);
		return statusMappings;
	}

	private Clip getClipObj(int clipId) {
		RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
		Clip clip = rbtCacheManager.getClip(clipId);
		logger.info("Returning Clip Object: " + clip);
		return clip;
	}

	public static ClipTransferDaemon getClipTransferDaemonInstance() {
		if (clipTransferDaemon == null) {
			synchronized (ClipTransferDaemon.class) {
				if (clipTransferDaemon == null)
					clipTransferDaemon = new ClipTransferDaemon();
			}
		}
		return clipTransferDaemon;
	}

	/**
	 * stops the daemon thread
	 */
	public void stopThread() {
		runDaemon = false;
	}

	/**
	 * starts the daemon thread
	 */
	public void startThread() {
		runDaemon = true;
	}

	public static void main(String[] args) {
		ClipTransferDaemon daemon = getClipTransferDaemonInstance();
		Thread daemonThread = new Thread(daemon);
		daemonThread.setName("CLIP_TRANSFER_DAEMON");
		daemonThread.start();
	}

}
