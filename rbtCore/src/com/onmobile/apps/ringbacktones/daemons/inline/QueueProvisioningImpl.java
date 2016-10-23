package com.onmobile.apps.ringbacktones.daemons.inline;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.common.exception.OnMobileException;

public class QueueProvisioningImpl implements IProvisioning {
	private Logger logger = Logger.getLogger(QueueProvisioningImpl.class);
	
	private MessageChannel outputChannel;
	private String outputChannelName = "chn.toRouter";
	private ChannelResolver channelResolver;
	private String modeHeaderName = "mode";
	private String defaultModeHeaderName = "default-mode";
	private String defaultModeValue = "DEFAULT";
	private List<String> smModeList;
	private List<String> cgModeList;
	private long sendingTimeoutInMillis = 10;
	private String defaultOutputChannelName = "chn.toDefaultRouter";
	@SuppressWarnings("unused")
	private MessageChannel defaultOutputChannel;
	
	private QueueProvisioningImpl(ChannelResolver channelResolver) {
		this.channelResolver = channelResolver;
	}
	
	@Override
	public void provision(Object obj, WebServiceContext parameters) throws Throwable {
		StringBuilder sb = new StringBuilder();
		String mode = (String)parameters.get(WebServiceConstants.param_mode);
		List<String> modeList = null;
		
		if(parameters.get(WebServiceConstants.param_action).equals(WebServiceConstants.CONSENT))
			modeList = cgModeList;
		else 
			modeList = smModeList;
		
		if(!modeList.contains(mode))
			mode = defaultModeValue;
		
		sb.append(mode).append('-').append(parameters.get(WebServiceConstants.param_api)).append('-').append(parameters.get(WebServiceConstants.param_action)).append('-').append(parameters.get(WebServiceConstants.param_provisioning_type));
		if(!sendOverChannel(obj, sb.toString()))
			throw new OnMobileException(WebServiceConstants.SPRING_MESSAGE_SENDING_FAILURE);
	}
	
	public String getOutputChannelName() {
		return outputChannelName;
	}

	public void setOutputChannelName(String outputChannelName) {
		this.outputChannelName = outputChannelName;
		outputChannel = channelResolver.getChannel(outputChannelName);
	}

	public ChannelResolver getChannelResolver() {
		return channelResolver;
	}

	public void setChannelResolver(ChannelResolver channelResolver) {
		this.channelResolver = channelResolver;
	}

	public String getModeHeaderName() {
		return modeHeaderName;
	}

	public void setModeHeaderName(String modeHeaderName) {
		this.modeHeaderName = modeHeaderName;
	}

	public String getDefaultModeHeaderName() {
		return defaultModeHeaderName;
	}

	public void setDefaultModeHeaderName(String defaultModeHeaderName) {
		this.defaultModeHeaderName = defaultModeHeaderName;
	}

	public String getDefaultModeValue() {
		return defaultModeValue;
	}

	public void setDefaultModeValue(String defaultModeValue) {
		this.defaultModeValue = defaultModeValue;
	}

	public List<String> getSmModeList() {
		return smModeList;
	}

	public void setSmModeList(List<String> smModeList) {
		this.smModeList = smModeList;
	}

	public List<String> getCgModeList() {
		return cgModeList;
	}

	public void setCgModeList(List<String> cgModeList) {
		this.cgModeList = cgModeList;
	}

	public long getSendingTimeoutInMillis() {
		return sendingTimeoutInMillis;
	}

	public void setSendingTimeoutInMillis(long sendingTimeoutInMillis) {
		this.sendingTimeoutInMillis = sendingTimeoutInMillis;
	}

	
	public String getDefaultOutputChannelName() {
		return defaultOutputChannelName;
	}

	public void setDefaultOutputChannelName(String defaultOutputChannelName) {
		this.defaultOutputChannelName = defaultOutputChannelName;
		defaultOutputChannel = channelResolver.getChannel(defaultOutputChannelName);
	}

	private boolean sendOverChannel(Object obj, String headerValue) {
        Message<Object> springMessage = MessageBuilder.withPayload(obj).setHeader(modeHeaderName, headerValue).setHeader(defaultModeHeaderName, headerValue).build();
        if (logger.isDebugEnabled()) {
                logger.debug("Sending Obj message (" + obj + ") to channel " + outputChannelName);
        }
        
        long startTime = System.currentTimeMillis();
        boolean sendStatus = outputChannel.send(springMessage, sendingTimeoutInMillis);
        if (sendStatus) {
            if (logger.isInfoEnabled()) {
                    logger.info("Sent obj: " + obj + " to channel " + outputChannelName + " in " + (System.currentTimeMillis() - startTime) + "ms.");
            }
        }
        return sendStatus;
	}
}
