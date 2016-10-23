/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadFactory;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.RBTChargePerCallLogDBCleaner;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.RBTChargePerCallPostProcessor;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.RBTChargePerCallPrismRetryDaemon;
import com.onmobile.apps.ringbacktones.daemons.tcp.supporters.ViralOptOutRequestExecutor;
import com.onmobile.apps.ringbacktones.daemons.tcp.supporters.ViralPendingRequestExecutor;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtTCPServer {
	private static Logger logger = Logger.getLogger(RbtTCPServer.class);
	private int serverPort;

	private Executor bossExecutor = null;
	private Executor workerExecutor = null;

	private ServerBootstrap serverBootstrap = null;
	private Channel serverChannel = null;

	private RbtThreadPoolExecutor handlerExecutor = null;
	private RbtTCPServerHandler serverHandler = null;

	private final List<ExecutorService> supporters = new ArrayList<ExecutorService>();
	public static int expiryInSeconds = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
			WebServiceConstants.CURRENT_PLAYING_SONG_MEMCACHE_EXPIRATION_LENGTH_IN_SECONDS, 5);
	
	public RbtTCPServer() {
		this.serverPort = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"TCP_SERVER_PORT", 8080);

		RbtThreadFactory threadFactory = new RbtThreadFactory("RbtTCPServer");
		bossExecutor = Executors.newCachedThreadPool(threadFactory);

		RbtThreadFactory workerThreadFactory = new RbtThreadFactory(
				"RbtTCPServer-worker");
		workerExecutor = Executors.newCachedThreadPool(workerThreadFactory);

		serverBootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(bossExecutor, workerExecutor));

		handlerExecutor = new RbtTCPServerHandlerExecutor.Builder().build();
		serverHandler = new RbtTCPServerHandler(handlerExecutor);

		serverBootstrap.setPipelineFactory(new RbtTCPServerPipelineFactory(
				serverHandler));
		logger.info(WebServiceConstants.CURRENT_PLAYING_SONG_MEMCACHE_EXPIRATION_LENGTH_IN_SECONDS
				+ ": " + expiryInSeconds);

	}

	/**
	 * 
	 */
	public void start() {
		ApplicationContext context = new ClassPathXmlApplicationContext("bean_spring.xml");
		boolean startViralPromotionSupporters = RBTParametersUtils
				.getParamAsBoolean("DAEMON",
						"START_VIRAL_PROMOTION_SUPPORTERS", "FALSE");
		boolean startChargePerCallSupporters = RBTParametersUtils
				.getParamAsBoolean("DAEMON",
						"START_CHARGE_PER_CALL_SUPPORTERS", "FALSE");
		boolean startChargePerCallPrismRetrySupporters = RBTParametersUtils
				.getParamAsBoolean("DAEMON",
						"START_CHARGE_PER_CALL_PRISM_RETRY_SUPPORTERS", "FALSE");
		if (startViralPromotionSupporters) {
			startSupporters();
		}
		if (startChargePerCallSupporters) {
			startChargePerCallSupportThreads();
		}
		if (startChargePerCallPrismRetrySupporters) {
			startChargePerCallPrismRetrySupportThreads();
		}
		serverChannel = serverBootstrap.bind(new InetSocketAddress(serverPort));
		logger.debug("RbtTCPServer started on port: " + serverPort);
	}

	private void startChargePerCallSupportThreads() {
		int postProcessorThreads = RBTParametersUtils.getParamAsInt("DAEMON",
				"NO_OF_THREADS_FOR_CHARGEPERCALL_POST_PROCESS", 3);
		// This is the first time at which task is to be executed.
		int postProcessorInitTime = RBTParametersUtils.getParamAsInt("DAEMON",
				"INIT_STARTTIME_FOR_CHARGEPERCALL_POST_PROCESS", 0);

		// This is the time between successive task executions.
		// Default value is 5 SECONDS.
		int scheduleInterval = RBTParametersUtils.getParamAsInt("DAEMON",
				"PERIODIC_INTERVAL_FOR_CHARGEPERCALL_POST_PROCESS", 5);
		// Fetch the configured minutes back records. By default it pick a
		// day old records.
		int minsOldRecords = RBTParametersUtils.getParamAsInt("DAEMON",
				"MINS_OLD_RECORDS_FOR_CHARGEPERCALL_POST_PROCESS", 2);

		// This is the first time at which task is to be executed.
		// Default value is 0 MINUTE.
		int clearDBInitTime = RBTParametersUtils.getParamAsInt("DAEMON",
				"INIT_STARTTIME_FOR_DB_CLEAR", 0);

		// This is the time between successive task executions.
		// Default value is 1 MINUTE.
		int clearDBPeriodicTime = RBTParametersUtils.getParamAsInt("DAEMON",
				"PERIODIC_INTERVAL_FOR_DB_CLEAR", 1);

		ScheduledExecutorService scheduler = Executors
				.newScheduledThreadPool(postProcessorThreads);

		RBTChargePerCallPostProcessor postProcessor = new RBTChargePerCallPostProcessor();
		scheduler.scheduleAtFixedRate(postProcessor, postProcessorInitTime,
				scheduleInterval, TimeUnit.SECONDS);

		RBTChargePerCallLogDBCleaner dbClearer = new RBTChargePerCallLogDBCleaner(
				minsOldRecords);
		scheduler.scheduleAtFixedRate(dbClearer, clearDBInitTime,
				clearDBPeriodicTime, TimeUnit.MINUTES);

	}

	private void startSupporters() {
		if (ViralPendingRequestExecutor.canStart()) {
			ExecutorService executorService = new ViralPendingRequestExecutor.Builder()
					.build();
			supporters.add(executorService);
		}

		if (RBTParametersUtils.getParamAsBoolean("DAEMON",
				"PROCESS_VIRAL_OPTOUT_REQUESTS", "FALSE")) {
			ExecutorService executorService = new ViralOptOutRequestExecutor.Builder()
					.build();
			supporters.add(executorService);
		}
	}

	private void startChargePerCallPrismRetrySupportThreads() {
		/*int prismRetryThreadCount = RBTParametersUtils.getParamAsInt("DAEMON",	//TODO enable multi-threading
				"NO_OF_THREADS_FOR_CHARGEPERCALL_PRISM_RETRY", 1);*/
		int prismRetryThreadCount = 1;
		// This is the first time at which task is to be executed.
		int prismRetryInitTime = RBTParametersUtils.getParamAsInt("DAEMON",
				"INIT_STARTTIME_FOR_CHARGEPERCALL_PRISM_RETRY", 0);

		// This is the time between successive task executions.
		// Default value is 5 SECONDS.
		int scheduleInterval = RBTParametersUtils.getParamAsInt("DAEMON",
				"PERIODIC_INTERVAL_FOR_CHARGEPERCALL_PRISM_RETRY", 20);

		ScheduledExecutorService scheduler = Executors
				.newScheduledThreadPool(prismRetryThreadCount);

		RBTChargePerCallPrismRetryDaemon rbtChargePerCallPrismRetryDaemon = new RBTChargePerCallPrismRetryDaemon();
		scheduler.scheduleAtFixedRate(rbtChargePerCallPrismRetryDaemon, prismRetryInitTime,
				scheduleInterval, TimeUnit.SECONDS);
	}
	
	/**
	 * 
	 */
	public void stop() {
		stopSupporters();
		if (serverChannel == null)
			throw new IllegalStateException("RbtTCPServer is not yet started");

		serverBootstrap.releaseExternalResources();
		serverChannel.close();

		handlerExecutor.shutdownNow();
	}

	private void stopSupporters() {
		for (ExecutorService executorService : supporters) {
			executorService.shutdownNow();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new RbtTCPServer().start();
	}
}
