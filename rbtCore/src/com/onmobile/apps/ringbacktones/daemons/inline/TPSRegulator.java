package com.onmobile.apps.ringbacktones.daemons.inline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.channel.AbstractPollableChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import com.onmobile.apps.ringbacktones.daemons.inline.shutdown.Hook;
import com.onmobile.apps.ringbacktones.daemons.inline.shutdown.ShutdownHook;
import com.onmobile.apps.ringbacktones.services.common.Utility;

public class TPSRegulator {

	private Logger logger = Logger.getLogger(TPSRegulator.class);
	private Map<String, List<RequestChannelConfig>> modeChannelConfigListMap; //To be injected via spring
	private Map<String, Integer> modeTps; //To be injected via spring
	private boolean isShutdownCalled= false;
	private Map<String, TpsHandlerThread> modeTpsThread = new HashMap<String, TpsHandlerThread>();
	private long sendingTimeoutInMillis = 10;
	@Autowired
	private ShutdownHook shutdownService;
	private String type;
	
	public void init(){
		if(logger.isDebugEnabled()){
			logger.debug("Initializing "+ type + " TPSRegulator...");
		}
		Thread thread = null;
		TpsHandlerThread tpsHandlerThread = null;
		
		for(Entry<String,List<RequestChannelConfig>> entry: modeChannelConfigListMap.entrySet()){
			tpsHandlerThread = new TpsHandlerThread(entry.getKey(), entry.getValue());
			thread = new Thread(tpsHandlerThread,"TpsHandlerThread-"+type+"-"+entry.getKey());
			modeTpsThread.put(entry.getKey(), tpsHandlerThread);
			tpsHandlerThread.setHook(shutdownService.createHook(thread));
			thread.start();
		}
	}

	public void destroy(){
		isShutdownCalled = true;
	}

//	public void changeTps(String groupName, float percentage){
//		TpsHandlerThread tpsHandlerThread = hmGroupnameThread.get(groupName);
//		int maxTps = hmGroupnameTps.get(groupName);
//		int currTps = tpsHandlerThread.getTps();
//		int newTps = (int) (currTps + (maxTps * percentage));
//		if(newTps < 0){
//			newTps = 0;
//		}else if (newTps > maxTps){
//			newTps = maxTps;
//		}
//		tpsHandlerThread.setTps(newTps);
//	}


	public Map<String, List<RequestChannelConfig>> getModeChannelConfigListMap() {
		return modeChannelConfigListMap;
	}

	public void setModeChannelConfigListMap(Map<String, List<RequestChannelConfig>> modeChannelConfigListMap) {
		this.modeChannelConfigListMap = modeChannelConfigListMap;
	}

	public Map<String, Integer> getModeTps() {
		return modeTps;
	}

	public void setModeTps(Map<String, Integer> modeTps) {
		this.modeTps = modeTps;
	}

	public long getSendingTimeoutInMillis() {
		return sendingTimeoutInMillis;
	}

	public void setSendingTimeoutInMillis(long sendingTimeoutInMillis) {
		this.sendingTimeoutInMillis = sendingTimeoutInMillis;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private class TpsHandlerThread implements Runnable, ChannelInterceptor {

		private String groupName;
		private List<String> queueNameList;
		private Message[] queueRetryTaskMessage;
		private Map<String, RequestChannelConfig> inputRequestChannelConfigMap = new HashMap<String, RequestChannelConfig>();
		private int currIndex = 0;
		private int prevIndex = -1;
		private int countTakenForQueue = 0;
		private int emptyQueueCount = 0;
		private volatile boolean isNotified = false;
		private volatile int tps;
		private Hook hook;
		
		public TpsHandlerThread(String groupName, List<RequestChannelConfig> requestChannelConfigList) {
			this.groupName = groupName;
			this.tps = modeTps.get(groupName);
			this.queueNameList = new ArrayList<String>();
			String channelName = null;
			for(RequestChannelConfig requestChannelConfig : requestChannelConfigList) { //chk for null cases/error cases
				channelName = requestChannelConfig.getInputChannelName();
				((AbstractPollableChannel)(requestChannelConfig.getInputChannel())).addInterceptor(this);
				inputRequestChannelConfigMap.put(channelName, requestChannelConfig);
				queueNameList.add(channelName);
			}
			this.queueRetryTaskMessage = new Message[queueNameList.size()];
		}

		@Override
		public void run() {
			boolean status;
			Message<Object> taskMessage;
			int count=0;
			long startTime= System.currentTimeMillis();
			while(!isShutdownCalled && hook.keepRunning()){
				taskMessage = getMessageToProcess();
				if(taskMessage == null)
					continue;
				status = sendOverChannel(taskMessage);
				if(!status){
					//insert the received task into the array for retrial.
					if(logger.isDebugEnabled()){
						logger.debug("getMessageToProcess... inserting the received task into the array for retrial idx:"+prevIndex);
					}
					this.queueRetryTaskMessage[prevIndex] = taskMessage;
					continue;
				}
				count++;
				//Throttling, wont be exact tps but workable
				if (count >= tps){
					long currentTime = System.currentTimeMillis();
					long timeDiff = 1000 - (currentTime - startTime);
					if (timeDiff >= 0){
						doze(timeDiff);
						currentTime = System.currentTimeMillis();
					}
					count = 0;
					startTime = currentTime;
				}
			}
		}

		private void doze(long millis){
			try{
				Thread.sleep(millis);
			}catch(InterruptedException e){

			}
		}

		private Message<Object> getMessageToProcess(){
			if(isShutdownCalled || !hook.keepRunning())
				return null;
			int priority = inputRequestChannelConfigMap.get(queueNameList.get(currIndex)).getPriority();
			PollableChannel queue = (PollableChannel) inputRequestChannelConfigMap.get(queueNameList.get(currIndex)).getInputChannel();
			if(logger.isDebugEnabled()){
				logger.debug("getMessageToProcess... currQueue: "+queueNameList.get(currIndex)+" idx:"+currIndex);
			}
			Message<Object> taskObj;
			if(prevIndex >= 0 && (taskObj = (Message<Object>)this.queueRetryTaskMessage[prevIndex])!=null){
				//this task didn't get executed, return this one instead of returning a new one.
				this.queueRetryTaskMessage[prevIndex] = null;
				if(logger.isDebugEnabled()){
					logger.debug("getMessageToProcess... got retry task");
				}
				emptyQueueCount = 0;
				this.isNotified = false;
				return taskObj;
			}else if((taskObj = (Message<Object>) queue.receive(0)) == null){
				currIndex = (currIndex+1)%queueNameList.size();
				countTakenForQueue = 0;
				emptyQueueCount++;
				if(emptyQueueCount >= queueNameList.size()){
					// all queues are empty. so wait someone has to notify this while putting in the queue.
					synchronized(this) {
						if(!this.isNotified){
							try {
								if(logger.isDebugEnabled()){
									logger.debug("Gonna wait");
								}
								this.wait();
							} catch (InterruptedException e) {

							}
						}
						this.isNotified = false;
					}
					emptyQueueCount = 0;
				}
				
				return getMessageToProcess();
			}
			
			prevIndex = currIndex;
			emptyQueueCount = 0;
			this.isNotified = false;
			countTakenForQueue++;
			if (countTakenForQueue >= priority) {
				// have taken enough counts.
				currIndex = (currIndex + 1) % queueNameList.size();
				countTakenForQueue = 0;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Returing task from " + queueNameList.get(prevIndex));
			}
			return taskObj;
		}

		@Override
		public Message<?> preSend(Message<?> msg, MessageChannel mc) {
			return msg;
		}

		@Override
		public void postSend(Message<?> msg, MessageChannel mc, boolean bln) {
			if(!bln) {
				if(Utility.resetInlineFlag(msg))
					logger.info("Falling back to daemon approach for channel : " + mc + msg);
				else
					logger.info("Falling back to daemon approach for channel: " + mc + msg);
			} else {
				if(logger.isDebugEnabled()){
					logger.debug("Adding msg to queue.");
				}
			}
			synchronized(this){
				this.isNotified = true;
				this.notifyAll();
			}
		}

		@Override
		public boolean preReceive(MessageChannel mc) {
			return true;
		}

		@Override
		public Message<?> postReceive(Message<?> msg, MessageChannel mc) {
			return msg;
		}

		@Override
		public void afterReceiveCompletion(Message<?> arg0, MessageChannel arg1, Exception arg2) {
		}

		@Override
		public void afterSendCompletion(Message<?> arg0, MessageChannel arg1, boolean arg2, Exception arg3) {
			if(!arg2) {
				if(Utility.resetInlineFlag(arg0))
					logger.info("Falling back to daemon approach for channel : " + arg1 + arg0);
				else
					logger.info("Falling back to daemon approach for channel: " + arg1 + arg0);
			}
		}
		
		private boolean sendOverChannel(Object obj) {
            Message<Object> springMessage = MessageBuilder.withPayload(obj).build();
            MessageChannel outputChannel = inputRequestChannelConfigMap.get(queueNameList.get(prevIndex)).getOutputChannel();
            String outputChannelName = inputRequestChannelConfigMap.get(queueNameList.get(prevIndex)).getOutputChannelName();
            
            if (logger.isDebugEnabled()) {
            	logger.debug("Sending Obj message (" + obj + ") to channel " + outputChannelName);
            }
            boolean sendStatus = false;
            long startTime = System.currentTimeMillis();
            try {
	            sendStatus = outputChannel.send(springMessage, sendingTimeoutInMillis);
	            if (sendStatus) {
	                if (logger.isInfoEnabled()) {
	                	logger.info("Sent obj: " + obj + " to channel " + outputChannelName + " in " + (System.currentTimeMillis() - startTime) + "ms.");
	                }
	            }
            } catch(Throwable t) {
            	sendStatus = false;
            	logger.error("Error while sending over channel: " + outputChannelName + t);
            }
            if(!sendStatus) {
            	logger.warn("Unable to send obj message " + obj + " over channel " + outputChannelName);
            	if(Utility.resetInlineFlag(obj))
        			logger.info("Falling back to daemon approach in TPS regulator is successful for : " + obj);
        		else
        			logger.info("Falling back to daemon approach in TPS regulator is failed for: " + obj);
            	
            	sendStatus = true;
            }
            return sendStatus;
		}

		public void setHook(Hook hook) {
			this.hook = hook;
		}
	}
}
