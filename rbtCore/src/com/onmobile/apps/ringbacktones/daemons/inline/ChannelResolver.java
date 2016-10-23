package com.onmobile.apps.ringbacktones.daemons.inline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;

public class ChannelResolver {

	private static Logger log = Logger.getLogger(ChannelResolver.class);

	private BeanFactoryChannelResolver beanChannelResolver;
	private Map<String, MessageChannel> channelsMap = new ConcurrentHashMap<String, MessageChannel>();
	private int channelCapacity = 5000;

	private static ChannelResolver staticInstance = null;

	public static ChannelResolver getInstance() {
		return staticInstance;
	}

	private ChannelResolver(BeanFactoryChannelResolver beanChannelResolver) {
		this.beanChannelResolver = beanChannelResolver;
		staticInstance = this;
	}

	public MessageChannel getChannel(String channelName) {
		MessageChannel msgChannel = null;

		try {
			if (!channelsMap.containsKey(channelName)) {
				try {
					msgChannel = beanChannelResolver.resolveDestination(channelName);
				} catch (Exception e) {
					log.debug("Channel name : " + channelName
							+ " not resolved.");
				}
				if (msgChannel == null) {
					log.debug("Creating new name : " + channelName);
					msgChannel = new QueueChannel(channelCapacity);
					log.debug("Channel created with queue size : "
							+ ((QueueChannel) msgChannel)
									.getRemainingCapacity());
				}
				log.debug("Putting the key : " + channelName + " in map");
				((QueueChannel) msgChannel).setBeanName(channelName);
				channelsMap.put(channelName, msgChannel);
			} else {
				log.debug("Channel already exists in map : " + channelName);
				msgChannel = channelsMap.get(channelName);
			}
		} catch (Exception e) {
			log.error("Exception occured while creating a channel", e);
		}
		log.debug("Returning channel:"
				+ ((msgChannel == null) ? "NULL" : ((AbstractMessageChannel) msgChannel).getComponentName()));
		return msgChannel;
	}

	/*
	 * public MessageChannel getChannelFromMap(String channelName) {
	 * 
	 * if (channelsMap.containsKey(channelName)) { log.debug("Getting the key:"
	 * + channelName + " from map"); return channelsMap.get(channelName); } else
	 * log.debug("Channel don't exists : " + channelName); return null; }
	 */

	public boolean removeChannelFromMap(String channelName) {

		if (channelsMap.containsKey(channelName)) {
			log.debug("Removing the key : " + channelName + " from map");
			((QueueChannel) channelsMap.remove(channelName)).clear();
			return true;
		} else
			log.debug("Channel don't exists : " + channelName);
		return false;
	}
	
	public boolean clearMessagesFromChannel(String channelName) {

		if (channelsMap.containsKey(channelName)) {
			log.debug("Clearing messages from  channel : " + channelName);
			((QueueChannel) channelsMap.get(channelName)).clear();
			return true;
		} else
			log.debug("Channel don't exists : " + channelName);
		return false;
	}

	public Map<String, MessageChannel> getChannelsMap() {
		return channelsMap;
	}

	public void setChannelsMap(Map<String, MessageChannel> channelsMap) {
		this.channelsMap = channelsMap;
	}

	public void setChannelCapacity(int channelCapacity) {
		this.channelCapacity = channelCapacity;
	}
}
