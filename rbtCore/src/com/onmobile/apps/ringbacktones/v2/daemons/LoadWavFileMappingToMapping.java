package com.onmobile.apps.ringbacktones.v2.daemons;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.IWavFileMappingDAO;
import com.onmobile.apps.ringbacktones.v2.dao.bean.WavFileMapping;

public class LoadWavFileMappingToMapping extends Thread {
	Logger logger = Logger.getLogger(LoadWavFileMappingToMapping.class);
	private boolean running;
	private int batchSize, endIndex = 1000;
	private int startIndex = 0;
	private int threadSleepTime = (1000 * 60 * 60);
	private static ConcurrentHashMap<String, String> waveFileMap = new ConcurrentHashMap<String, String>();

	public boolean isRunning() {
		return this.running;
	}

	public LoadWavFileMappingToMapping() {
		this.setDaemon(true);
		this.setName("LoadWavFileMappingToMapping");
	}

	@Override
	public void run() {
		IWavFileMappingDAO wavFileMappingDAO = (IWavFileMappingDAO) ConfigUtil
				.getBean(BeanConstant.WAV_FILE_MAPPING_DAO);
		List<WavFileMapping> wavFileMappingLst = null;
		String wavFile1_0 = null;
		String wavFile2_0 = null;
		String operatorName = null;
		while (true) {
			try {
				logger.info("LoadWavFileMappingToMapping started and the batchSize is :"
						+ batchSize);
				wavFileMappingLst = wavFileMappingDAO.getWavFileVerTwoByBatch(
						startIndex, endIndex);
				logger.info("WavFileMappingList startIndex: " + startIndex
						+ " ,endIndex: " + endIndex);
				if (null != wavFileMappingLst && !wavFileMappingLst.isEmpty()) {
					logger.info("WavFileMappingLst.size():"
							+ wavFileMappingLst.size());
					for (WavFileMapping wavFileVerOne : wavFileMappingLst) {
						if (wavFileVerOne != null
								&& wavFileVerOne.getWavFileCompositeKey()
										.getWavFileVerTwo() != null
								&& !wavFileVerOne.getWavFileCompositeKey()
										.getWavFileVerTwo().isEmpty()) {
							wavFile2_0 = wavFileVerOne.getWavFileCompositeKey()
									.getWavFileVerTwo();
							wavFile1_0 = wavFileVerOne.getWavFileVerOne();
							operatorName = wavFileVerOne
									.getWavFileCompositeKey().getOperatorName();
						}
						if (wavFile1_0 != null && operatorName != null)
							waveFileMap.put((wavFile1_0 + "_" + operatorName)
									.toUpperCase(), wavFile2_0);
						wavFile2_0 = null;
						wavFile1_0 = null;
					}
					startIndex = endIndex + 1;
					endIndex = (endIndex + batchSize);
				} else {
					logger.info("WavFileMappingLst.size() is empty so we are stopping the load thread.");
					startIndex = 0;
					endIndex = batchSize;
					logger.info("LoadWavFileMappingToMapping is going into sleep mode sleep time is :"
							+ threadSleepTime);
					//Thread.sleep(threadSleepTime);
					//continue;
					break;
				}
				logger.info("waveFileMap:---> "
						+ (waveFileMap != null ? waveFileMap.toString() : null));
			} catch (Exception e) {
				logger.info("Exception" + e);
			}
		}
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		this.endIndex = batchSize;
	}

	public int getThreadSleepTime() {
		return threadSleepTime;
	}

	public void setThreadSleepTime(int threadSleepTime) {
		this.threadSleepTime = (threadSleepTime * 1000 * 60 * 60);
	}

	public static ConcurrentHashMap<String, String> getWaveFileMap() {
		return waveFileMap;
	}

}
