/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.smsgateway.accounting.Accounting;

/**
 * @author vinayasimha.patil
 *
 */
public class DSServer implements Runnable
{
	private static Logger logger = Logger.getLogger(DSServer.class);

	// The port combination to listen on
	private int port;

	// The channel on which we'll accept connections
	private ServerSocketChannel serverChannel;

	// The selector we'll be monitoring
	public static Selector selector;

	public static Configurations configurations;
	public static OMRequestHandler omRequestHandler;
	public static ReceiverQueue receiverQueue;
	public static SenderQueue senderQueue;
	public static ChannelPool channelPool;

	private static List<SocketChannel> pendingRequests;

	public static Accounting oglAccounting = null;
	public static Accounting btslAccounting = null;
	public static Accounting connectionAccounting = null;
	public static Accounting oglHttpAccounting = null;
	public static Accounting cgwHttpAccounting = null;

	public static WebServiceStarter webServer;

	@SuppressWarnings("unused")
	public DSServer() throws Exception
	{
		configurations = new Configurations();
		omRequestHandler = new OMRequestHandler();
		receiverQueue = new ReceiverQueue();
		senderQueue = new SenderQueue();
		channelPool = new ChannelPool();

		createAccountings();

		port = configurations.getServerPort();
		selector = this.initSelector();

		pendingRequests = new ArrayList<SocketChannel>();

		for (int i = 0; i < configurations.getNoOfDSMessageProcessors(); i++)
		{
			new DSMessageProcessor(i);
		}
	}

	private Selector initSelector() throws IOException
	{
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(port);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in 
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		logger.info("RBT:: Started Server on port: "+ port);

		return socketSelector;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		while (true)
		{
			try 
			{
				synchronized (pendingRequests)
				{
					Iterator<SocketChannel> requests = pendingRequests.iterator();
					while (requests.hasNext())
					{
						SocketChannel socketChannel = null;
						try
						{
							socketChannel = requests.next();
							SelectionKey key = socketChannel.keyFor(selector);
							key.interestOps(SelectionKey.OP_WRITE);
						}
						catch (Exception e)
						{
							closeSocketChannel(socketChannel);
						}
					}
					pendingRequests.clear();
				}


				logger.info("RBT:: Waiting for an event for "+ configurations.getServerEventWaitTime() +" milliseconds");

				// Wait for an event one of the registered channels
				int events = selector.select(configurations.getServerEventWaitTime());

				if(events > 0)
				{
					logger.info("RBT:: Got "+ events +" event ");
					// Iterate over the set of keys for which events are available
					Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
					while (selectedKeys.hasNext())
					{
						SelectionKey key = selectedKeys.next();
						selectedKeys.remove();

						if (!key.isValid())
							continue;

						// Check what event is available and deal with it
						if (key.isAcceptable())
							accept(key);
						else if (key.isReadable())
							read(key);
						else if (key.isWritable())
							write(key);
					}
				}
				else
					logger.info("RBT:: Did not get any event ");

			}
			catch (Exception e)
			{
				System.exit(0);
				logger.error("", e);
			}
		}
	}

	private void accept(SelectionKey key)
	{
		logger.info("RBT:: inside accept");

		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		try
		{
			// Accept the connection and make it non-blocking
			SocketChannel socketChannel = serverSocketChannel.accept();
			Socket socket = socketChannel.socket();
			socketChannel.configureBlocking(false);

			// Register the new SocketChannel with our Selector, indicating
			// we'd like to be notified when there's data waiting to be read
			socketChannel.register(selector, SelectionKey.OP_READ);

			String ipAddress = socket.getInetAddress().toString();
			ipAddress = ipAddress.substring(ipAddress.indexOf('/')+1);
			logger.info("RBT:: Accepted a connection from "+ ipAddress);
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
	}

	private void read(SelectionKey key)
	{
		logger.info("RBT:: inside read");

		SocketChannel socketChannel = (SocketChannel) key.channel();
		Socket socket = socketChannel.socket();

		// Attempt to read off the channel
		try
		{
			String ipAddress = socket.getInetAddress().toString();
			ipAddress = ipAddress.substring(ipAddress.indexOf('/')+1);
			int clientPort = socket.getPort();

			ByteBuffer msgBuffer = ByteBuffer.allocate(8);
			int readCount = socketChannel.read(msgBuffer);
			logger.info("RBT:: Read Count = "+ readCount);

			if (readCount == -1)
			{
				// Remote entity shut the socket down cleanly. Do the
				// same from our end and cancel the channel.
				closeSocketChannel(socketChannel);
				return;
			}

			msgBuffer.flip();
			int msgType = msgBuffer.getInt();
			int msgLength = msgBuffer.getInt();

			if(msgLength > 200)
				throw new Exception("Invalid message length: "+ msgLength);

			ByteBuffer msgDataBuffer = ByteBuffer.allocate(msgLength);
			readCount = socketChannel.read(msgDataBuffer);
			logger.info("RBT:: Read Count = "+ readCount);

			msgDataBuffer.flip();
			DSMessage dsMessage = DSProtocolParser.convertByteBufferToDSMessage(msgType, msgLength, msgDataBuffer);
			if(dsMessage != null)
			{
				receiverQueue.add(dsMessage, socketChannel);

				String messageType = DSProtocolParser.getMessageType(dsMessage);
				Accounting accounting = (messageType.equals("REQUEST")) ? btslAccounting : oglAccounting;
				addToAccounting(accounting, messageType, "BTSL:"+ ipAddress +":"+ clientPort, "ONMOBILE", dsMessage.toString());
			}
			else
			{
				closeSocketChannel(socketChannel);
				logger.info("RBT:: Invalid  Message, Closing Connection");
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			closeSocketChannel(socketChannel);
		}
	}

	private void write(SelectionKey key)
	{
		SocketChannel socketChannel = (SocketChannel) key.channel();
		List<ByteBuffer> queue = senderQueue.get(socketChannel);

		try
		{
			// Write until there's not more data ...
			while (!queue.isEmpty())
			{
				ByteBuffer buf = queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0)
				{
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty())
			{
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			closeSocketChannel(socketChannel);
		}
	}

	public static void send(SocketChannel socketChannel, DSMessage dsMessage)
	{
		if(!socketChannel.isOpen())
		{
			logger.info("RBT:: SocketChannel Closed, not sending Message: "+ dsMessage);
			return;
		}

		logger.info("RBT:: Sending Message: "+ dsMessage);
		ByteBuffer data = DSProtocolParser.convertDSMessageToByteBuffer(dsMessage);
		if(data == null)
		{
			logger.info("RBT:: got data as null");
			return;
		}

		synchronized (pendingRequests)
		{
			// Indicate we want the interest ops set changed
			pendingRequests.add(socketChannel);

			// And queue the data we want written
			senderQueue.add(socketChannel, data);
		}

		// Finally, wake up our selecting thread so it can make the required changes
		selector.wakeup();

		Socket socket = socketChannel.socket();
		String ipAddress = socket.getInetAddress().toString();
		ipAddress = ipAddress.substring(ipAddress.indexOf('/')+1);
		int clientPort = socket.getPort();

		String messageType = DSProtocolParser.getMessageType(dsMessage);
		Accounting accounting = (messageType.equals("REQUEST")) ? oglAccounting : btslAccounting;
		addToAccounting(accounting, messageType, "ONMOBILE", "BTSL:"+ ipAddress +":"+ clientPort, dsMessage.toString());
	}

	public static void createAccountings()
	{
		String transactionSDRFormat = "TYPE APP_ID PACKET SENDER RECIPIENT TIME";
		oglAccounting = Accounting.getInstance(configurations.getSdrWorkingDir() +"\\ONMOBILE_SDR", configurations.getSdrSize(),
				configurations.getSdrInterval(), configurations.getSdrRotation(), configurations.isSdrBillingOn(), transactionSDRFormat);
		if (oglAccounting == null)
			logger.info("RBT::ONMOBILE Accounting class can not be created");

		btslAccounting = Accounting.getInstance(configurations.getSdrWorkingDir() +"\\BTSL_SDR", configurations.getSdrSize(),
				configurations.getSdrInterval(), configurations.getSdrRotation(), configurations.isSdrBillingOn(), transactionSDRFormat);
		if (btslAccounting == null)
			logger.info("RBT::BTSL Accounting class can not be created");

		String connectionSDRFormat = "TYPE APP_ID IP_ADDRESS PORT TIME AVAILABLE_CONNECTIONS";
		connectionAccounting = Accounting.getInstance(configurations.getSdrWorkingDir() +"\\CONNECTION_SDR", configurations.getSdrSize(),
				configurations.getSdrInterval(), configurations.getSdrRotation(), configurations.isSdrBillingOn(), connectionSDRFormat);
		if (connectionAccounting == null)
			logger.info("RBT::Connection Accounting class can not be created");

		String httpRequestSDRFormat = "TYPE APP_ID REQUEST REPLY REMARKS SENDER REQUEST_TIME RESPONSE_TIME";
		oglHttpAccounting = Accounting.getInstance(configurations.getSdrWorkingDir() +"\\ONMOBILE_HTTP_SDR", configurations.getSdrSize(),
				configurations.getSdrInterval(), configurations.getSdrRotation(), configurations.isSdrBillingOn(), httpRequestSDRFormat);
		if (oglHttpAccounting == null)
			logger.info("RBT::ONMOBILE Http Accounting class can not be created");

		cgwHttpAccounting = Accounting.getInstance(configurations.getSdrWorkingDir() +"\\CGW_HTTP_SDR", configurations.getSdrSize(),
				configurations.getSdrInterval(), configurations.getSdrRotation(), configurations.isSdrBillingOn(), httpRequestSDRFormat);
		if (cgwHttpAccounting == null)
			logger.info("RBT::CGW Http Accounting class can not be created");
	}

	public static void addToAccounting(Accounting accounting, String type, String sender, String recipient, String request)
	{
		try
		{
			if (accounting != null)
			{
				HashMap<String, String> acMap = new HashMap<String, String>();
				acMap.put("TYPE", type);
				acMap.put("APP_ID", "RBT");
				acMap.put("PACKET", request);
				acMap.put("SENDER", sender);
				acMap.put("RECIPIENT", recipient);
				acMap.put("TIME", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format((new Date())));

				accounting.generateSDR("sms", acMap);
				logger.info("RBT::Writing to the accounting file");

				acMap = null;
			}
		}
		catch (Exception e)
		{
			logger.info("RBT::Exception caught " + e.getMessage());
		}
	}

	public static void addToConnectionAccounting(String type, String ipAddress, int port, String availableConnections)
	{
		try
		{
			if (connectionAccounting != null)
			{
				HashMap<String, String> acMap = new HashMap<String, String>();
				acMap.put("TYPE", type);
				acMap.put("APP_ID", "RBT");
				acMap.put("IP_ADDRESS", ipAddress);
				acMap.put("PORT", port+"");
				acMap.put("TIME", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format((new Date())));
				availableConnections = availableConnections.replaceAll("java.nio.channels.SocketChannel", "");
				acMap.put("AVAILABLE_CONNECTIONS", availableConnections);
				if (connectionAccounting != null)
				{
					connectionAccounting.generateSDR("sms", acMap);
					logger.info("RBT::Writing to the accounting file");
				}
				acMap = null;
			}
		}
		catch (Exception e)
		{
			logger.info("RBT::Exception caught " + e.getMessage());
		}
	}

	public static void addToHttpAccounting(Accounting accounting, String type, String request, String reply, String remarks, String sender, Date requestTime, long responseTime)
	{
		try
		{
			if (accounting != null)
			{
				HashMap<String, String> acMap = new HashMap<String, String>();
				acMap.put("TYPE", type);
				acMap.put("APP_ID", "RBT");
				acMap.put("REQUEST", request);
				acMap.put("REPLY", reply);
				acMap.put("REMARKS", remarks);
				acMap.put("SENDER", sender);
				acMap.put("REQUEST_TIME", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(requestTime));
				acMap.put("RESPONSE_TIME", responseTime+"");

				accounting.generateSDR("sms", acMap);
				logger.info("RBT::Writing to the accounting file");

				acMap = null;
			}
		}
		catch (Exception e)
		{
			logger.info("RBT::Exception caught " + e.getMessage());
		}
	}

	public static void closeSocketChannel(SocketChannel socketChannel)
	{
		try
		{
			logger.info("RBT::Closing SocketChannel: "+ socketChannel);
			channelPool.remove(socketChannel);
			senderQueue.remove(socketChannel);
			socketChannel.close();
			socketChannel.keyFor(selector).cancel();
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Tools.init("DSSERVER", true);
		try
		{
			DSServer dsServer = new DSServer();
			Thread thread = new Thread(dsServer);
			thread.setName("DSServer");
			thread.start();

			webServer = new WebServiceStarter(configurations.getHttpServerHostName(), configurations.getHttpServerPort(),
					configurations.getHttpServerMaxConnections(), -1, -1, configurations.getHttpServerAppBase());
			webServer.startWebServer();
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}
}
