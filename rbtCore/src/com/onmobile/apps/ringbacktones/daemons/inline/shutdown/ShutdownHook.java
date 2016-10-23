package com.onmobile.apps.ringbacktones.daemons.inline.shutdown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import com.onmobile.apps.ringbacktones.daemons.inline.ChannelResolver;
import com.onmobile.apps.ringbacktones.services.common.Utility;

public class ShutdownHook {
	private static final Logger logger = Logger.getLogger(ShutdownHook.class);
	private final List<Hook> hooks;
	
	public ShutdownHook() {
		logger.debug("Creating shutdown service");
		hooks = new ArrayList<Hook>();
	}

	public void destroy() {
		logger.info("Running shutdown sync service");
		System.out.println("Running shutdown sync service"); //To keep posted on console...
		while (hooks.size() > 0) {
			Hook hook = hooks.remove(0);
			hook.shutdown();
		}
		logger.info("saving channels");
		updateChannelsMsgs();
		logger.info("Shutdown sync service completed");
		System.out.println("Shutdown sync service completed"); //To keep posted on console...
	}

	public void updateChannelsMsgs() {
		ChannelResolver channelResolver = ChannelResolver.getInstance();
		Map<String, MessageChannel> channelsMap = channelResolver
				.getChannelsMap();
		try {
			Iterator<String> iter = channelsMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				logger.info("key = " + key);
				MessageChannel channel = channelsMap.get(key);
				if (channel instanceof QueueChannel) {
					List<Message<?>> msgList;
					msgList = ((QueueChannel) channel).clear();
					for (Message<?> msg : msgList) {
						Object obj = msg.getPayload();
						if(Utility.resetInlineFlag(obj))
							logger.info("Falling back to daemon approach is successful for : " + obj);
						else
							logger.info("Falling back to daemon approach is failed for : " + obj);
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception: ", e);
		} finally {
		}
	}

	public Hook createHook(Thread thread) {
		thread.setDaemon(true);
		Hook retVal = new Hook(thread);
		hooks.add(retVal);
		return retVal;
	}

	public void removeHook(Hook hook) {
		hooks.remove(hook);
	}

	List<Hook> getHooks() {
		return hooks;
	}
}
