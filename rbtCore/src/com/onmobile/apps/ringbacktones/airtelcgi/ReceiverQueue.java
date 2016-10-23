/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 *
 */
public class ReceiverQueue
{
	private static Logger logger = Logger.getLogger(ReceiverQueue.class);

	private List<DSMessage> queue;
	private List<SocketChannel> channelQueue;

	public ReceiverQueue()
	{
		queue = new ArrayList<DSMessage>();
		channelQueue = new ArrayList<SocketChannel>();
	}

	synchronized public void add(DSMessage dsMessage, SocketChannel socketChannel)
	{
		synchronized (queue)
		{
			queue.add(dsMessage);
			channelQueue.add(socketChannel);
			logger.info("RBT:: Added Message: "+ dsMessage);
			logger.info("RBT:: Queue Length: "+ queue.size());
			synchronized (this)
			{
				this.notify();
			}
		}
	}

	synchronized public DSMessage getRequest()
	{
		synchronized (queue)
		{
			if(queue.isEmpty())
				return null;

			DSMessage dsMessage = queue.get(0);
			queue.remove(0);

			logger.info("RBT:: Removed Message: "+ dsMessage);
			logger.info("RBT:: Queue Length: "+ queue.size());
			return dsMessage;
		}
	}

	synchronized public SocketChannel getChannel()
	{
		synchronized (channelQueue)
		{
			if(channelQueue.isEmpty())
				return null;

			SocketChannel socketChannel = channelQueue.get(0);
			channelQueue.remove(0);
			return socketChannel;
		}
	}
}
