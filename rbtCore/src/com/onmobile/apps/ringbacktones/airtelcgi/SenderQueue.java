/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vinayasimha.patil
 *
 */
public class SenderQueue
{
	private Map<SocketChannel, List<ByteBuffer>> queueMap;

	public SenderQueue()
	{
		queueMap = new HashMap<SocketChannel, List<ByteBuffer>>();
	}

	synchronized public void add(SocketChannel socketChannel, ByteBuffer byteBuffer)
	{
		synchronized (queueMap)
		{
			List<ByteBuffer> queue = queueMap.get(socketChannel);
			if(queue == null)
			{
				queue = new ArrayList<ByteBuffer>();
				queueMap.put(socketChannel, queue);
			}

			queue.add(byteBuffer);
		}
	}

	synchronized public List<ByteBuffer> get(SocketChannel socketChannel)
	{
		synchronized (queueMap)
		{
			List<ByteBuffer> queue = queueMap.get(socketChannel);
			return queue;
		}
	}

	synchronized public void remove(SocketChannel socketChannel)
	{
		synchronized (queueMap)
		{
			queueMap.remove(socketChannel);
		}
	}
}
