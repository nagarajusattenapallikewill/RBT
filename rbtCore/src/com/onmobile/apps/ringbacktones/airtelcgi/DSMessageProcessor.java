/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;

/**
 * @author vinayasimha.patil
 *
 */
public class DSMessageProcessor implements Runnable, DSProtocolConstants
{
	private static Logger logger = Logger.getLogger(DSMessageProcessor.class);

	private HttpParameters httpParameters;

	public DSMessageProcessor(int threadID)
	{
		int httpConnectionTimeout = DSServer.configurations.getHttpConnectionTimeout();
		int httpDataTimeout = DSServer.configurations.getHttpDataTimeout();
		boolean useProxy = DSServer.configurations.isUseProxy();
		String proxyHost = DSServer.configurations.getProxyHost();
		int proxyPort = DSServer.configurations.getProxyPort();
		httpParameters = new HttpParameters(null, useProxy, proxyHost, proxyPort, httpConnectionTimeout, httpDataTimeout, false);

		Thread dsMessageProcessorThread = new Thread(this);
		dsMessageProcessorThread.setName("DSMessageProcessor-"+threadID);
		dsMessageProcessorThread.start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		while(true)
		{
			try
			{
				DSMessage dsMessage = null;
				SocketChannel socketChannel = null;
				while(dsMessage == null)
				{
					dsMessage = DSServer.receiverQueue.getRequest();
					if(dsMessage == null)
					{
						try
						{
							synchronized (DSServer.receiverQueue)
							{
								DSServer.receiverQueue.wait();
							}
						} 
						catch (InterruptedException e)
						{
							logger.error("", e);
						}
					}
				}

				socketChannel = DSServer.receiverQueue.getChannel();
				int messageType = dsMessage.getMessageType();
				switch (messageType)
				{
					case DS_PING:
						processPingRequest((ConnectionMessage) dsMessage, socketChannel);
						break;

					case DS_CONNECT_REQ:
						processConnectionRequest((ConnectionMessage) dsMessage, socketChannel);
						break;

					case DS_DISCONNECT_REQ:
						processDisConnectionRequest((ConnectionMessage) dsMessage, socketChannel);
						break;

					case DS_SUB_PROF_REQ:
						processSubscriberProfileRequest((SubscriberProfileMessage) dsMessage, socketChannel);
						break;

					case DS_TONE_COPY_REQ:
						processToneCopyRequest((ToneCopyMessage) dsMessage, socketChannel);
						break;

					case DS_TONE_GIFT_REQ:
						processToneGiftRequest((ToneGiftMessage) dsMessage, socketChannel);
						break;

					case DS_SUB_PROF_RES:
					case DS_SUB_PROF_ERR:
					case DS_TONE_COPY_RES:
					case DS_TONE_COPY_ERR:
					case DS_TONE_GIFT_RES:
					case DS_TONE_GIFT_ERR:
						processResponseMessage(dsMessage);
						break;

					default:
						break;
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
	}

	private void processPingRequest(ConnectionMessage connectionMessage, SocketChannel socketChannel)
	{
		DSMessage pongResponse = new ConnectionMessage(DS_PONG,	connectionMessage.getMessageLength(), 
				connectionMessage.getMessageID(), NO_ERROR, connectionMessage.getInfo());

		DSServer.send(socketChannel, pongResponse);
	}

	private void processConnectionRequest(ConnectionMessage connectionMessage, SocketChannel socketChannel)
	{
		Socket socket = socketChannel.socket();
		String ipAddress = socket.getInetAddress().toString();
		ipAddress = ipAddress.substring(ipAddress.indexOf('/')+1);
		String site = DSServer.configurations.getSite(ipAddress);

		DSMessage connectionResponse = null;
		if(site != null && site.equals(BTSL_SITES))
		{
			DSServer.channelPool.add(socketChannel);
			connectionResponse = new ConnectionMessage(DS_CONNECT_RES, connectionMessage.getMessageLength(), 
					connectionMessage.getMessageID(), NO_ERROR, connectionMessage.getInfo());
		}
		else
		{
			connectionResponse = new ConnectionMessage(DS_CONNECT_ERR, connectionMessage.getMessageLength(), 
					connectionMessage.getMessageID(), SYNTAX_ERROR, connectionMessage.getInfo());
		}

		DSServer.send(socketChannel, connectionResponse);
	}

	private void processDisConnectionRequest(ConnectionMessage connectionMessage, SocketChannel socketChannel)
	{
		DSServer.channelPool.remove(socketChannel);
		DSServer.closeSocketChannel(socketChannel);
	}

	private void processSubscriberProfileRequest(SubscriberProfileMessage profileMessage, SocketChannel socketChannel)
	{
		String url = getURL(profileMessage.getSrcSubscriberID(), profileMessage.getMessageType());

		int messageType = DS_SUB_PROF_ERR;
		short errorCode = DATABASE_DOWN;
		String subscriberProvDate = null;
		byte provisioningStatus = 0x00;

		if(url.equals("INVALID_SITE"))
		{
			url = null;
			errorCode = INVALID_PACKET;
		}

		if(url != null)
		{
			try
			{
				logger.info("RBT:: URL: "+ url);

				HashMap<String, String> params = new HashMap<String, String>();
				params.put("subscriber_id", profileMessage.getSrcSubscriberID());

				logger.info("RBT:: Params: "+ params);

				httpParameters.setUrl(url);

				Date requestTime = new Date();
				String response = RBTHTTPProcessing.postFile(httpParameters, params, null);
				long responseTime = System.currentTimeMillis() - requestTime.getTime();

				String responseText = (response != null) ? response.trim() : "NULL"; 
				DSServer.addToHttpAccounting(DSServer.cgwHttpAccounting, "SUBSCRIBER_PROFILE", url+params, responseText, "NA", "CGW", requestTime, responseTime);

				if(response != null)
				{
					response = response.trim();
					if(response.startsWith("SUCCESS"))
					{
						messageType = DS_SUB_PROF_RES;
						errorCode = NO_ERROR;
						subscriberProvDate = response.substring(response.indexOf(':')+1, response.lastIndexOf(':'));
						provisioningStatus = Byte.parseByte(response.substring(response.lastIndexOf(':')+1));
					}
					else if(response.startsWith("ERROR"))
					{
						errorCode = Short.parseShort(response.substring(response.indexOf(':')+1));
					}
				}
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}

		DSMessage dsMessage = new SubscriberProfileMessage(messageType, DS_SUB_PROF_LEN, profileMessage.getMessageID(), errorCode,
				profileMessage.getProvisioningInterface(), provisioningStatus, profileMessage.getSrcSubscriberID(), subscriberProvDate);
		DSServer.send(socketChannel, dsMessage);
	}

	private void processToneCopyRequest(ToneCopyMessage copyMessage, SocketChannel socketChannel)
	{
		String url = getURL(copyMessage.getDstSubscriberID(), copyMessage.getMessageType());

		int messageType = DS_TONE_COPY_ERR;
		short errorCode = DATABASE_DOWN;
		String toneID = null;

		if(url.equals("INVALID_SITE"))
		{
			url = null;
			errorCode = INVALID_PACKET;
		}

		if(url != null)
		{
			try
			{
				logger.info("RBT:: URL: "+ url);

				HashMap<String, String> params = new HashMap<String, String>();
				params.put("subscriber_id", copyMessage.getDstSubscriberID());
				params.put("caller_id", copyMessage.getSrcSubscriberID());

				logger.info("RBT:: Params: "+ params);

				httpParameters.setUrl(url);

				Date requestTime = new Date();
				String response = RBTHTTPProcessing.postFile(httpParameters, params, null);
				long responseTime = System.currentTimeMillis() - requestTime.getTime();

				String responseText = (response != null) ? response.trim() : "NULL"; 
				DSServer.addToHttpAccounting(DSServer.cgwHttpAccounting, "TONE_COPY", url+params, responseText, "NA", "CGW", requestTime, responseTime);

				if(response != null)
				{
					response = response.trim();
					if(response.startsWith("SUCCESS"))
					{
						messageType = DS_TONE_COPY_RES;
						errorCode = NO_ERROR;
						toneID = response.substring(response.indexOf(':')+1);
					}
					else if(response.startsWith("ERROR"))
					{
						errorCode = Short.parseShort(response.substring(response.indexOf(':')+1));
					}
				}
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}

		DSMessage dsMessage = new ToneCopyMessage(messageType, DS_TONE_COPY_LEN, copyMessage.getMessageID(), errorCode,
				copyMessage.getSrcSubscriberID(), copyMessage.getDstSubscriberID(), toneID);
		DSServer.send(socketChannel, dsMessage);
	}

	private void processToneGiftRequest(ToneGiftMessage giftMessage, SocketChannel socketChannel)
	{
		String url = getURL(giftMessage.getDstRegionID(), giftMessage.getMessageType());

		int messageType = DS_TONE_GIFT_ERR;
		short errorCode = DATABASE_DOWN;

		if(url.equals("INVALID_SITE"))
		{
			url = null;
			errorCode = INVALID_PACKET;
		}

		if(url != null)
		{
			try
			{
				logger.info("RBT:: URL: "+ url);

				HashMap<String, String> params = new HashMap<String, String>();
				params.put("subscriber_id", giftMessage.getSrcSubscriberID());
				params.put("caller_id", giftMessage.getDstRegionID());
				params.put("wav_file", giftMessage.getToneID());

				logger.info("RBT:: Params: "+ params);

				httpParameters.setUrl(url);

				Date requestTime = new Date();
				String response = RBTHTTPProcessing.postFile(httpParameters, params, null);
				long responseTime = System.currentTimeMillis() - requestTime.getTime();

				String responseText = (response != null) ? response.trim() : "NULL"; 
				DSServer.addToHttpAccounting(DSServer.cgwHttpAccounting, "TONE_GIFT", url+params, responseText, "NA", "CGW", requestTime, responseTime);

				if(response != null)
				{
					response = response.trim();
					if(response.startsWith("SUCCESS"))
					{
						messageType = DS_TONE_GIFT_RES;
						errorCode = NO_ERROR;
					}
					else if(response.startsWith("ERROR"))
					{
						errorCode = Short.parseShort(response.substring(response.indexOf(':')+1));
					}
				}
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}

		DSMessage dsMessage = new ToneGiftMessage(messageType, DS_TONE_GIFT_LEN, giftMessage.getMessageID(), errorCode,
				giftMessage.getProvisioningInterface(), giftMessage.getSrcSubscriberID(), giftMessage.getDstRegionID(), giftMessage.getToneID(), giftMessage.getSongName());
		DSServer.send(socketChannel, dsMessage);
	}

	private void processResponseMessage(DSMessage dsMessage)
	{
		try
		{
			DSServer.omRequestHandler.addToResponseMap(dsMessage.getMessageID(), dsMessage);
			Object lockObj = DSServer.omRequestHandler.getLockObject(dsMessage.getMessageID());
			if(lockObj != null)
			{
				synchronized (lockObj)
				{
					lockObj.notify();
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	private String getURL(String subscriberID, int messageType)
	{
		String url = null;
		String uri = null;

		String[] ipAddresses = DSServer.configurations.getIPAddresses(subscriberID);
		if(ipAddresses == null || ipAddresses.length == 0)
			return "INVALID_SITE";

		String site = DSServer.configurations.getSite(ipAddresses[0]);
		if(site == null || !site.equals(OM_SITES))
			return "INVALID_SITE";

		String port = DSServer.configurations.getPort(ipAddresses[0]);

		switch (messageType)
		{
			case DS_SUB_PROF_REQ:
				uri = DSServer.configurations.getSubscriberProfileURI();
				break;

			case DS_TONE_COPY_REQ:
				uri = DSServer.configurations.getToneCopyURI();
				break;

			case DS_TONE_GIFT_REQ:
				uri = DSServer.configurations.getToneGiftURI();
				break;

			default:
				break;
		}

		if(uri != null)
			url = "http://"+ ipAddresses[0] +":"+ port +"/"+ uri;

		return url;	
	}
}
