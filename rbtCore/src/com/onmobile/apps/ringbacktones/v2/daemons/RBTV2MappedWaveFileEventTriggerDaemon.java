package com.onmobile.apps.ringbacktones.v2.daemons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.methods.HttpPut;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.IWavFileMappingDAO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.PropertyConfig;
import com.onmobile.apps.ringbacktones.v2.dao.bean.WavFileMapping;
import com.onmobile.apps.ringbacktones.v2.dao.constants.DistributorConstants;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.http.DefaultHttpService;
import com.onmobile.apps.ringbacktones.v2.http.HttpService;
import com.onmobile.apps.ringbacktones.v2.util.MappingSaxHandler;

public class RBTV2MappedWaveFileEventTriggerDaemon extends Thread {

	private static Logger logger = Logger
			.getLogger(RBTV2MappedWaveFileEventTriggerDaemon.class);
	private static Logger eventTriggerLogger = Logger
			.getLogger("MappingDaemonlog");
	PropertyConfig config = (PropertyConfig) ConfigUtil
			.getBean(BeanConstant.PROPERTY_CONFIG);
	private static boolean runDaemon = true;
	private static RBTV2MappedWaveFileEventTriggerDaemon rbtv2MappedWaveFileEventTriggerDaemon = null;
	private static Object syncObj = new Object();
	private HttpService httpService = new DefaultHttpService();
	private String proxyHost = null;
	private String wavFileMappingRssFeedURL = null;
	private String sleepTime = "60";
	HttpPut httpPut = null;
	int proxyPort = 0;
	private static ConcurrentHashMap<String, String> waveFileMap = new ConcurrentHashMap<String, String>();

	public boolean initialize(HashMap<String, String> arg0) {
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"bean_spring.xml");
		RBTV2MappedWaveFileEventTriggerDaemon rbtv2MappedWaveFileEventTriggerDaemon = RBTV2MappedWaveFileEventTriggerDaemon
				.getInstance();
		rbtv2MappedWaveFileEventTriggerDaemon
				.setName("RBTV2MAPPED_WAVFILE_EVENT_TRIGGER_DAEMON_OZONIFIED");
		rbtv2MappedWaveFileEventTriggerDaemon.start();
	}

	/**
	 * @return the Instance of RBTV2MappedWaveFileEventTriggerDaemon
	 */
	public static RBTV2MappedWaveFileEventTriggerDaemon getInstance() {
		if (rbtv2MappedWaveFileEventTriggerDaemon == null) {
			synchronized (syncObj) {
				if (rbtv2MappedWaveFileEventTriggerDaemon == null) {
					try {
						rbtv2MappedWaveFileEventTriggerDaemon = new RBTV2MappedWaveFileEventTriggerDaemon();
					} catch (Throwable e) {
						logger.error("", e);
						rbtv2MappedWaveFileEventTriggerDaemon = null;
					}
				}
			}
		}
		return rbtv2MappedWaveFileEventTriggerDaemon;
	}

	public void run() {
		System.out.println("Entering mapping wavfile ingestor");
		try {
			validateConfiguration();
			httpPut = new HttpPut(wavFileMappingRssFeedURL);
			List<WavFileMapping> wavfileMappingLst = null;
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			MappingSaxHandler handler = null;
			while (runDaemon) {
				try {
					long triggerStartTime = System.currentTimeMillis();
					String response = getHttpService().executeGetMethodForXml(
							wavFileMappingRssFeedURL, proxyHost, proxyPort);
					if (response != null) {
						eventTriggerLogger
								.info(wavFileMappingRssFeedURL
										+ ", "
										+ (System.currentTimeMillis() - triggerStartTime));
						SAXParser saxParser = saxParserFactory.newSAXParser();
						handler = new MappingSaxHandler();
						ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
								response.trim().getBytes("UTF-8"));
						saxParser.parse(byteArrayInputStream, handler);
						if (handler.getWaveFileMapingLst() != null
								&& handler.getWaveFileMapingLst().size() > 0) {
							wavfileMappingLst = handler.getWaveFileMapingLst();
							IWavFileMappingDAO wavFileMappingDAO = (IWavFileMappingDAO) ConfigUtil
									.getBean(BeanConstant.WAV_FILE_MAPPING_DAO);
							int successCount = wavFileMappingDAO
									.saveOrUpdateWavFileMapping(wavfileMappingLst);
							try {
								LoadWavFileMappingToMapping loadWavFileToMapping = (LoadWavFileMappingToMapping) ConfigUtil
										.getBean(BeanConstant.LOAD_WAVE_FILE_MAPPING_FOR_2_0);
								waveFileMap = loadWavFileToMapping
										.getWaveFileMap();
								String wavFile1_0 = null;
								String wavFile2_0 = null;
								String operatorName = null;
								for (WavFileMapping wavFileVerOne : wavfileMappingLst) {
									if (wavFileVerOne != null
											&& wavFileVerOne
													.getWavFileCompositeKey()
													.getWavFileVerTwo() != null
											&& !wavFileVerOne
													.getWavFileCompositeKey()
													.getWavFileVerTwo()
													.isEmpty()) {
										wavFile2_0 = wavFileVerOne
												.getWavFileCompositeKey()
												.getWavFileVerTwo();
										wavFile1_0 = wavFileVerOne
												.getWavFileVerOne();
										operatorName = wavFileVerOne
												.getWavFileCompositeKey()
												.getOperatorName();
									}
									if (wavFile1_0 != null
											&& operatorName != null)
										waveFileMap
												.put((wavFile1_0 + "_" + operatorName)
														.toUpperCase(),
														wavFile2_0);
									wavFile2_0 = null;
									wavFile1_0 = null;
								}
							} catch (Exception e) {
								logger.error("Bean is not configured:"
										+ BeanConstant.LOAD_WAVE_FILE_MAPPING_FOR_2_0);
							}
							eventTriggerLogger.info("Successfull Count:"
									+ successCount + ", Total count:"
									+ wavfileMappingLst.size());
						}
					} else {
						eventTriggerLogger
								.info(wavFileMappingRssFeedURL
										+ ", "
										+ (System.currentTimeMillis() - triggerStartTime));
					}
				} catch (ParserConfigurationException e) {
					logger.info("Exception thrown in the mapping wavfile ingestor"
							+ e);
					stopThread();
				} catch (SAXException e) {
					logger.info("Exception thrown in the mapping wavfile ingestor"
							+ e);
					stopThread();
				}
				try {
					System.out.println("Sleeping for " + sleepTime
							+ " minutes ");
					logger.info("Sleeping for " + sleepTime + " minutes ");
					Thread.sleep(Integer.parseInt(sleepTime) * 60 * 1000);
				} catch (Exception e) {
					logger.info("Exception thrown in the mapping wavfile ingestor"
							+ e);
					stopThread();
				}
			}
		} catch (UserException e) {
			logger.info("ATLANTIS_RSS_WAV_FILE_MAPPING_FEED_URL URL not configured, so stopping the daemon");
			stopThread();
		} catch (IOException e) {
			logger.info("Exception thrown in the mapping wavfile ingestor" + e);
			stopThread();
		}
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

	private void validateConfiguration() throws UserException {
		proxyHost = config
				.getValueFromResourceBundle(DistributorConstants.ATLANTIS_RSS_PROXY_HOST);
		if (config
				.getValueFromResourceBundle(DistributorConstants.ATLANTIS_RSS_PROXY_PORT) != null)
			proxyPort = Integer
					.valueOf(config
							.getValueFromResourceBundle(DistributorConstants.ATLANTIS_RSS_PROXY_PORT));
		wavFileMappingRssFeedURL = config
				.getValueFromResourceBundle(DistributorConstants.ATLANTIS_RSS_WAV_FILE_MAPPING_FEED_URL);
		sleepTime = config
				.getValueFromResourceBundle(DistributorConstants.SLEEP_TIME_IN_MINS);
		if ((wavFileMappingRssFeedURL == null)) {
			throw new UserException(
					"Configurations missing for rss feed for mapping");
		}
	}

	public HttpService getHttpService() {
		return httpService;
	}

	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}
}
