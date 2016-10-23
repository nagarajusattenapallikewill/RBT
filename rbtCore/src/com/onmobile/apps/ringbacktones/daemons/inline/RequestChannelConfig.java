package com.onmobile.apps.ringbacktones.daemons.inline;

import org.springframework.messaging.MessageChannel;

public class RequestChannelConfig {

	private String inputChannelName;
	private String outputChannelName;
	private int priority;
	private MessageChannel inputChannel;
	private MessageChannel outputChannel;
	private ChannelResolver channelResolver;
	
	private RequestChannelConfig(ChannelResolver channelResolver) {
		this.channelResolver = channelResolver;
	}
	
	public String getInputChannelName() {
		return inputChannelName;
	}

	public void setInputChannelName(String inputChannelName) {
		this.inputChannelName = inputChannelName;
		inputChannel = channelResolver.getChannel(inputChannelName);
	}

	public String getOutputChannelName() {
		return outputChannelName;
	}

	public void setOutputChannelName(String outputChannelName) {
		this.outputChannelName = outputChannelName;
		outputChannel = channelResolver.getChannel(outputChannelName);
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public MessageChannel getInputChannel() {
		return inputChannel;
	}

	public MessageChannel getOutputChannel() {
		return outputChannel;
	}
}
