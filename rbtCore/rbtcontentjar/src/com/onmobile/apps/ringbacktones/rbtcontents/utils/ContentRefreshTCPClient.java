package com.onmobile.apps.ringbacktones.rbtcontents.utils;
/**
 * 
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;

/**
 * @author vinayasimha.patil
 *
 */
public class ContentRefreshTCPClient
{
	private static Logger logger = Logger.getLogger(ContentRefreshTCPClient.class);

	public static void sendMessageToAllClients(String contentIDs)
	{
		String clients = RBTContentJarParameters.getInstance().getParameter("CACHE_CLIENTS");
		if (clients != null)
		{
			StringTokenizer tokenizer = new StringTokenizer(clients, ",");
			while (tokenizer.hasMoreTokens())
			{
				String client = tokenizer.nextToken().trim();
				String hostAddress = client.substring(0, client.indexOf(':'));
				int port = Integer.parseInt(client.substring(client.indexOf(':') + 1));

				sendMessage(hostAddress, port, contentIDs);
			}
		}
	}

	public static void sendMessage(String hostAddress, int port, String contentIDs)
	{
		logger.info("RBT:: sending message to host: "+ hostAddress +":"+ port);
		try
		{
			Selector selector = initSelector();
			SocketChannel socketChannel = initiateConnection(hostAddress, port);

			socketChannel.register(selector, SelectionKey.OP_CONNECT);
			selector.select();
			Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
			if (selectedKeys.hasNext())
			{
				SelectionKey key = selectedKeys.next();
				selectedKeys.remove();

				if (key.isValid() && key.isConnectable())
				{
					finishConnection(key);

					key.interestOps(SelectionKey.OP_WRITE);

					selector.select();
					Iterator<SelectionKey> writeSelectedKeys = selector.selectedKeys().iterator();
					if (writeSelectedKeys.hasNext())
					{
						SelectionKey writableKey = writeSelectedKeys.next();
						writeSelectedKeys.remove();

						if (writableKey.isValid() && writableKey.isWritable())
						{
							write(writableKey, contentIDs);
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private static Selector initSelector() throws IOException
	{
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}

	private static SocketChannel initiateConnection(String hostAddress, int port) throws IOException
	{
		// Create a non-blocking socket channel
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);

		// Kick off connection establishment
		socketChannel.connect(new InetSocketAddress(hostAddress, port));

		return socketChannel;
	}

	private static void finishConnection(SelectionKey key) throws IOException
	{
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try
		{
			socketChannel.finishConnect();
		}
		catch (IOException e)
		{
			// Cancel the channel's registration with our selector
			logger.error(e.getMessage(), e);
			key.cancel();
			return;
		}
	}

	private static void write(SelectionKey key, String contentIDs)
	{
		SocketChannel socketChannel = (SocketChannel) key.channel();

		try
		{
			String message = "REFRESH";
			if (contentIDs != null)
				message += ":"+ contentIDs;
			byte[] messageBytes = message.getBytes();
			ByteBuffer byteBuffer = ByteBuffer.wrap(messageBytes);

			int writeCount = socketChannel.write(byteBuffer);
			logger.info("Sent refresh message to "+ socketChannel +"(writeCount: "+ writeCount +")");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ContentRefreshTCPClient.sendMessage("172.16.25.177", 8000, null);
	}
}
