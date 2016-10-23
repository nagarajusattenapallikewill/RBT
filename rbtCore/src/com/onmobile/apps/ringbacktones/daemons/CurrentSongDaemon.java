package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportFactory;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.MemcacheClientForCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.SendData;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.SendHandler;

/**
 * 
 * @author rony.gregory
 */
public class CurrentSongDaemon {

	private static final Logger logger = Logger.getLogger(CurrentSongDaemon.class);

	public static int expiryInSeconds = 5;
	public static int portToListenTo = 9080; 
	public static SendHandler handler;
	public static SendData.Processor<SendHandler> processor;

	static {
		expiryInSeconds = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON, WebServiceConstants.CURRENT_PLAYING_SONG_MEMCACHE_EXPIRATION_LENGTH_IN_SECONDS, expiryInSeconds);
		logger.info(WebServiceConstants.CURRENT_PLAYING_SONG_MEMCACHE_EXPIRATION_LENGTH_IN_SECONDS + ": " + expiryInSeconds);
		
		portToListenTo = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON, WebServiceConstants.CURRENT_PLAYING_SONG_PORT_TO_LISTEN_TO, portToListenTo);
		logger.info(WebServiceConstants.CURRENT_PLAYING_SONG_PORT_TO_LISTEN_TO + ": " + portToListenTo);
	}

	public CurrentSongDaemon() {

	}

	public static void start() {
		try {//Changes are done for handling the voldemort issues.
			MemcacheClientForCurrentPlayingSong.getInstance()
					.checkCacheInitialized();
			boolean isCallLogMemCacheIsUp = MemcacheClientForCurrentPlayingSong
					.getInstance().isCacheAlive();
			if (!isCallLogMemCacheIsUp) {
				logger.info("MemcacheClientForCurrentPlayingSong is not up so we are not starting the daemon: ");
				return;
			}
			handler = new SendHandler();
			processor = new SendData.Processor<SendHandler>(handler);
			RBTCurrentPlayingSongExecutors executor = new RBTCurrentPlayingSongExecutors();
			Runnable simple = new Runnable() {
				public void run() {
					startServer(processor);
				}
			};      
			new Thread(simple).start();
		} catch (Throwable t) {
			logger.error("Error caught: " + t);
		}
	}
	//Changes are done for handling the voldemort issues.
	public static void startServer(SendData.Processor<SendHandler> processor) {
		try {
			/*TServerTransport serverTransport = new TServerSocket(portToListenTo);
			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));*/

			TServerTransport serverTransport = new TServerSocket(portToListenTo);
			TThreadPoolServer server = new TThreadPoolServer(
					new TThreadPoolServer.Args(serverTransport)
							.processor(processor)
							.transportFactory(new TTransportFactory())
							.protocolFactory(new TBinaryProtocol.Factory())
							.minWorkerThreads(10).maxWorkerThreads(1000));
			
			logger.info("Starting the CurrentSongDaemon.");
			server.serve();
		} catch (Exception e) {
			logger.error("Error caught: " + e);
		}
	}
	
	public static void main(String[] args) {
		start();
	}
}