/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;


/**
 * @author vinayasimha.patil
 *
 */
public class DSTestClient implements Runnable, DSProtocolConstants
{
	private static Logger logger = Logger.getLogger(DSTestClient.class);

	// The host:port combination to connect to
	private InetAddress hostAddress;
	private int port;

	// The selector we'll be monitoring
	private static Selector selector;

	// A list of PendingRequests
	private static Map<SocketChannel, Integer> pendingRequests;

	public static Configurations configurations;
	public static OMRequestHandler omRequestHandler;
	public static ReceiverQueue receiverQueue;
	public static SenderQueue senderQueue;

	public SocketChannel socketChannel;

	private BufferedReader responereader;

	@SuppressWarnings("unused")
	public DSTestClient() throws IOException
	{
		configurations = new Configurations();
		omRequestHandler = new OMRequestHandler();
		receiverQueue = new ReceiverQueue();
		senderQueue = new SenderQueue();

		this.hostAddress = InetAddress.getByName(configurations.getCommonGatewayIP());
		this.port = configurations.getServerPort();
		selector = initSelector();

		pendingRequests = new HashMap<SocketChannel, Integer>();

		socketChannel = initiateConnection();
		logger.info("RBT::Connection: "+ socketChannel);

		FileReader fileReader = new FileReader("Response.txt");
		responereader = new BufferedReader(fileReader);

		new SenderThread();
	}

	public void run()
	{
		while (true)
		{
			try
			{
				// Process any pending changes
				synchronized (pendingRequests)
				{
					Set<SocketChannel> requestsSet = pendingRequests.keySet();
					Iterator<SocketChannel> requests = requestsSet.iterator();
					while (requests.hasNext())
					{
						SocketChannel socketChannel = null;
						try
						{
							socketChannel = requests.next();
							int ops = (pendingRequests.get(socketChannel)).intValue();
							switch (ops) 
							{
								case SelectionKey.OP_WRITE:
									SelectionKey key = socketChannel.keyFor(selector);
									key.interestOps(SelectionKey.OP_WRITE);
									break;
								case SelectionKey.OP_CONNECT:
									socketChannel.register(selector, SelectionKey.OP_CONNECT);
									break;
							}
						}
						catch (Exception e)
						{
							closeSocketChannel(socketChannel);
						}
					}
					pendingRequests.clear();
				}

				// Wait for an event one of the registered channels
				selector.select();

				// Iterate over the set of keys for which events are available
				Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext())
				{
					SelectionKey key = selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid())
					{
						continue;
					}

					// Check what event is available and deal with it
					if (key.isConnectable())
					{
						this.finishConnection(key);
					}
					else if (key.isReadable())
					{
						this.read(key);
					}
					else if (key.isWritable())
					{
						this.write(key);
					}
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
				System.exit(0);
			}
		}
	}

	private Selector initSelector() throws IOException
	{
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}

	private SocketChannel initiateConnection() throws IOException
	{
		// Create a non-blocking socket channel
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);

		// Kick off connection establishment
		socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));

		// Queue a channel registration since the caller is not the 
		// selecting thread. As part of the registration we'll register
		// an interest in connection events. These are raised when a channel
		// is ready to complete connection establishment.
		synchronized(pendingRequests)
		{
			pendingRequests.put(socketChannel, new Integer(SelectionKey.OP_CONNECT));
		}

		return socketChannel;
	}

	private void finishConnection(SelectionKey key)
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
			System.out.println(e);
			key.cancel();
			return;
		}

		// Register an interest in reading on this channel
		key.interestOps(SelectionKey.OP_READ);
	}

	private void read(SelectionKey key)
	{
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Attempt to read off the channel
		try
		{

			ByteBuffer msgBuffer = ByteBuffer.allocate(8);
			socketChannel.read(msgBuffer);

			msgBuffer.flip();
			int msgType = msgBuffer.getInt();
			int msgLength = msgBuffer.getInt();

			ByteBuffer msgDataBuffer = ByteBuffer.allocate(msgLength);
			socketChannel.read(msgDataBuffer);

			msgDataBuffer.flip();
			DSMessage dsMessage = DSProtocolParser.convertByteBufferToDSMessage(msgType, msgLength, msgDataBuffer);
			if(dsMessage != null)
			{
				logger.info("RBT:: Received Message: "+ dsMessage);
				ProcessRequest(dsMessage);
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
			pendingRequests.put(socketChannel, new Integer(SelectionKey.OP_WRITE));

			// And queue the data we want written
			senderQueue.add(socketChannel, data);
		}

		// Finally, wake up our selecting thread so it can make the required changes
		selector.wakeup();
	}

	public static void closeSocketChannel(SocketChannel socketChannel)
	{
		try
		{
			senderQueue.remove(socketChannel);
			socketChannel.close();
			socketChannel.keyFor(selector).cancel();
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	private void ProcessRequest(DSMessage dsMessage)
	{
		if(!DSProtocolParser.getMessageType(dsMessage).equals("REQUEST"))
			return;

		DSMessage responseMessage = null;

		int messageType;
		short errorCode;

		try
		{
			String response = responereader.readLine();
			if(response != null)
			{
				String status = response.substring(response.indexOf('[')+1, response.indexOf(']'));
				switch (dsMessage.getMessageType())
				{
					case DS_SUB_PROF_REQ:
						SubscriberProfileMessage subProfRequest = (SubscriberProfileMessage) dsMessage;
						messageType = DS_SUB_PROF_RES;
						errorCode = NO_ERROR;
						String provDate = null;
						byte provStatus = 0x00;
						if(status.startsWith("ERROR"))
						{
							messageType = DS_SUB_PROF_ERR;
							errorCode = Short.parseShort(status.substring(status.indexOf(',')+1));
						}
						else
						{
							provStatus = Byte.parseByte(status.substring(0, status.indexOf(',')));
							provDate = status.substring(status.indexOf(',')+1);
						}
						responseMessage = new SubscriberProfileMessage(messageType, DS_SUB_PROF_LEN, subProfRequest.getMessageID(),
								errorCode, subProfRequest.getProvisioningInterface(), provStatus, subProfRequest.getSrcSubscriberID(), provDate);
						break;

					case DS_TONE_COPY_REQ:
						ToneCopyMessage toneCopyRequest = (ToneCopyMessage) dsMessage;
						messageType = DS_TONE_COPY_RES;
						errorCode = NO_ERROR;
						String toneID = null;
						if(status.startsWith("ERROR"))
						{
							messageType = DS_TONE_COPY_ERR;
							errorCode = Short.parseShort(status.substring(status.indexOf(',')+1));
						}
						else
						{
							toneID = status;
						}
						responseMessage = new ToneCopyMessage(messageType, DS_TONE_COPY_LEN, toneCopyRequest.getMessageID(),
								errorCode, toneCopyRequest.getSrcSubscriberID(), toneCopyRequest.getDstSubscriberID(), toneID);
						break;

					case DS_TONE_GIFT_REQ:
						ToneGiftMessage toneGiftRequest = (ToneGiftMessage) dsMessage;
						messageType = DS_TONE_GIFT_RES;
						errorCode = NO_ERROR;
						if(status.startsWith("ERROR"))
						{
							messageType = DS_TONE_GIFT_ERR;
							errorCode = Short.parseShort(status.substring(status.indexOf(',')+1));
						}

						responseMessage = new ToneGiftMessage(messageType, DS_TONE_GIFT_LEN, toneGiftRequest.getMessageID(),
								errorCode, toneGiftRequest.getProvisioningInterface(), toneGiftRequest.getSrcSubscriberID(), 
								toneGiftRequest.getDstRegionID(), toneGiftRequest.getToneID(), toneGiftRequest.getSongName());
						break;

					default:
						break;
				}
			}
		}
		catch (IOException e)
		{
			logger.error("", e);
		}

		if(responseMessage != null)
			send(socketChannel, responseMessage);

	}

	public static void main(String[] args)
	{
		Tools.init("DSCLIENT", true);
		try
		{
			DSTestClient client = new DSTestClient();
			Thread t = new Thread(client);
			t.start();
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	private class SenderThread implements Runnable
	{
		public SenderThread()
		{
			Thread senderThread = new Thread(this);
			senderThread.start();
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			try
			{
				FileReader fileReader = new FileReader("Requests.txt");
				BufferedReader bufferedReader = new BufferedReader(fileReader);

				String request = null;
				while((request = bufferedReader.readLine()) != null)
				{
					request = request.trim();
					DSMessage dsMessage = getDSMessage(request);
					if(dsMessage != null)
					{
						Thread.sleep(5000);
						send(socketChannel, dsMessage);
					}
				}
			}
			catch (FileNotFoundException e) 
			{
				logger.error("", e);
			}
			catch (IOException e)
			{
				logger.error("", e);
			}
			catch (InterruptedException e)
			{
				logger.error("", e);
			}
		}

		private DSMessage getDSMessage(String request)
		{
			DSMessage dsMessage = null;

			try
			{
				if(request.startsWith("ConnectionMessage"))
				{
					int messageType = Integer.parseInt(request.substring(request.indexOf('[')+1, request.indexOf(']')));
					dsMessage = new ConnectionMessage(messageType, DS_CONNECT_LEN, omRequestHandler.getNextMessageID(),
							NO_ERROR, null);
				}
				else if(request.startsWith("SubscriberProfileMessage"))
				{
					String srcSubscriberID = request.substring(request.indexOf('[')+1, request.indexOf(']'));
					dsMessage = new SubscriberProfileMessage(DS_SUB_PROF_REQ, DS_SUB_PROF_LEN, omRequestHandler.getNextMessageID(),
							NO_ERROR, DS_IVR, (byte)0x00, srcSubscriberID, null);
				}
				else if(request.startsWith("ToneCopyMessage"))
				{
					String srcSubscriberID = request.substring(request.indexOf('[')+1, request.indexOf(','));
					String dstSubscriberID = request.substring(request.indexOf(',')+1, request.indexOf(']'));
					dsMessage = new ToneCopyMessage(DS_TONE_COPY_REQ, DS_TONE_COPY_LEN, omRequestHandler.getNextMessageID(),
							NO_ERROR, srcSubscriberID, dstSubscriberID, null);
				}
				else if(request.startsWith("ToneGiftMessage"))
				{
					String data = request.substring(request.indexOf('[')+1, request.indexOf(']'));
					StringTokenizer tokenizer = new StringTokenizer(data, ",");
					String srcSubscriberID = tokenizer.nextToken();
					String dstRegionID = tokenizer.nextToken();
					String toneID = tokenizer.nextToken();
					String songName = tokenizer.nextToken();
					dsMessage = new ToneGiftMessage(DS_TONE_GIFT_REQ, DS_TONE_GIFT_LEN, omRequestHandler.getNextMessageID(),
							NO_ERROR, DS_IVR, srcSubscriberID, dstRegionID, toneID, songName);
				}
			}
			catch (Exception e)
			{

			}

			return dsMessage;
		}
	}
}
