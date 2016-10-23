/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.content;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 *
 */
public class ContentRefreshTCPServer implements Runnable
{
	private static Logger logger = Logger.getLogger(ContentRefreshTCPServer.class);

	private RBTContentCacheManager rbtContentCacheManager = null;

	// The port combination to listen on
	private int port;

	// The channel on which we'll accept connections
	private ServerSocketChannel serverChannel;

	// The selector we'll be monitoring
	public Selector selector;

	// The eventWaitTime selector will wait for events
	public int eventWaitTime;

	private boolean runServer = true;
	private Thread contentTCPServerThread = null;

	/**
	 * @param rbtContentCacheManager
	 * @param port
	 * @param eventWaitTime
	 */
	public ContentRefreshTCPServer(RBTContentCacheManager rbtContentCacheManager, int port, int eventWaitTime)
	{
		this.rbtContentCacheManager = rbtContentCacheManager;
		this.port = port;
		this.eventWaitTime = eventWaitTime;

		try
		{
			selector = initSelector();

			runServer = true;
			contentTCPServerThread = new Thread(this);
			contentTCPServerThread.start();
		}
		catch (IOException e)
		{
			logger.error("", e);
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

		logger.info("RBT:: Started ContentRefreshTCPServer on port: " + port);

		return socketSelector;
	}

	public final void stop()
	{
		logger.info("RBT:: Stopping ContentRefreshTCPServer");
		runServer = false;
		contentTCPServerThread.interrupt();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		while (runServer)
		{
			try 
			{
				// Wait for an event one of the registered channels
				int events = selector.select(eventWaitTime);

				if (events > 0)
				{
					logger.info("RBT:: Got " + events + " events");

					// Iterate over the set of keys for which events are available
					Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
					while (selectedKeys.hasNext())
					{
						SelectionKey selectionKey = selectedKeys.next();
						selectedKeys.remove();

						if (!selectionKey.isValid())
							continue;

						// Check what event is available and deal with it
						if (selectionKey.isAcceptable())
							accept(selectionKey);
						else if (selectionKey.isReadable())
							read(selectionKey);
					}
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
	}

	private void accept(SelectionKey key)
	{
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
			logger.info("RBT:: Accepted a connection from " + ipAddress);
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
	}

	private void read(SelectionKey key)
	{
		SocketChannel socketChannel = (SocketChannel) key.channel();
		Socket socket = socketChannel.socket();

		// Attempt to read off the channel
		try
		{
			String clientIPAddress = socket.getInetAddress().toString();
			clientIPAddress = clientIPAddress.substring(clientIPAddress.indexOf('/')+1);
			int clientPort = socket.getPort();

			ByteBuffer msgByteBuffer = ByteBuffer.allocate(10);
			int readCount = socketChannel.read(msgByteBuffer);
			logger.info("RBT:: Read Count = " + readCount);

			if (readCount == -1)
				return;

			msgByteBuffer.flip();
			String message = new String(msgByteBuffer.array(), 0, readCount);
			logger.info("RBT:: Got '" + message + "' message from " + clientIPAddress + ":" + clientPort);

			if (message.equalsIgnoreCase("REFRESH"))
			{
				RBTContent.getInstance().clearContentMap();
				rbtContentCacheManager.broadcastInfoMessage("RBT_CACHE_REFRESH", RBTContentCacheManager.COMPONENT_NAME, null, "ALL");
			}
			if (message.startsWith("REFRESH"))
			{
				String contentIDs = message.substring(8);
				String[] contentIDArray = contentIDs.split(",");
				for (String contentIDStr : contentIDArray)
				{
					int contentID = Integer.parseInt(contentIDStr);
					RBTContent.getInstance().clearContentMap(contentID);
				}

				rbtContentCacheManager.broadcastInfoMessage("RBT_CACHE_REFRESH", RBTContentCacheManager.COMPONENT_NAME, null, contentIDs);
			}
			else if (message.equalsIgnoreCase("RESTART"))
			{
				RBTContent.createRBTContent();
				rbtContentCacheManager.broadcastInfoMessage("RBT_CACHE_REFRESH", RBTContentCacheManager.COMPONENT_NAME, null, "RESTART");
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				logger.info("RBT::Closing SocketChannel: " + socketChannel);
				socketChannel.close();
				socketChannel.keyFor(selector).cancel();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		new ContentRefreshTCPServer(null, 8000, 1000);
	}
}
