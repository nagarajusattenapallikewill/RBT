/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
/**
 * @author vinayasimha.patil
 *
 */
public class ChannelPool
{
	private static Logger logger = Logger.getLogger(ChannelPool.class);
	
	Map<String, List<SocketChannel>> channelMap;
	Map<String, Integer> channelIndexMap;

	public ChannelPool()
	{
		channelMap = new HashMap<String, List<SocketChannel>>();
		channelIndexMap = new HashMap<String, Integer>();
	}

	public void add(SocketChannel socketChannel)
	{
		synchronized (channelMap)
		{
			Socket socket = socketChannel.socket();
			String ipAddress = socket.getInetAddress().toString();
			ipAddress = ipAddress.substring(ipAddress.indexOf('/')+1);

			List<SocketChannel> list = channelMap.get(ipAddress);
			if(list == null)
			{
				list = new ArrayList<SocketChannel>();
				channelMap.put(ipAddress, list);
			}

			list.add(socketChannel);

			logger.info("RBT::ChannelMap: "+ channelMap);
			DSServer.addToConnectionAccounting("CONNECT", ipAddress, socket.getPort(), channelMap.toString());
		}
	}

	public void remove(SocketChannel socketChannel)
	{
		synchronized (channelMap)
		{
			Socket socket = socketChannel.socket();
			String ipAddress = socket.getInetAddress().toString();
			ipAddress = ipAddress.substring(ipAddress.indexOf('/')+1);

			List<SocketChannel> list = channelMap.get(ipAddress);
			if(list != null)
			{
				list.remove(socketChannel);
				if(list.isEmpty())
				{
					channelMap.remove(ipAddress);
					channelIndexMap.remove(ipAddress);
				}
			}

			logger.info("RBT::ChannelMap: "+ channelMap);
			DSServer.addToConnectionAccounting("DISCONNECT", ipAddress, socket.getPort(), channelMap.toString());
		}
	}

	public SocketChannel getChannel(String[] ipAddresses)
	{
		if(ipAddresses == null || ipAddresses.length == 0)
			return null;

		synchronized (channelMap)
		{
			for (int i = 0; i < ipAddresses.length; i++)
			{
				List<SocketChannel> list = channelMap.get(ipAddresses[i]);
				if(list != null && !list.isEmpty())
				{
					int index = 0;
					Integer indexObj = channelIndexMap.get(ipAddresses[i]);
					if(indexObj != null)
					{
						index = indexObj.intValue();
						if(index >= list.size())
							index = 0;
					}

					int noOfChannels = list.size();
					while(noOfChannels > 0)
					{
						SocketChannel socketChannel = list.get(index);

						if(socketChannel.isOpen())
						{
							index++;
							channelIndexMap.put(ipAddresses[i], new Integer(index));
							return socketChannel;
						}
						else
						{
							list.remove(index);
						}

						if(list.isEmpty())
						{
							channelMap.remove(ipAddresses[i]);
							channelIndexMap.remove(ipAddresses[i]);
							break;
						}

						noOfChannels--;
						index = index % list.size();
					}
				}
			}
		}

		return null;
	}
}
