package com.onmobile.apps.ringbacktones.daemons.inline;

import org.apache.log4j.Logger;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.common.exception.OnMobileException;

public class MessageSenderInlineImpl implements IMessageSender {
	private IProvisioning queueProvisioning;
	private IProvisioning realtimeProvisioning;
	private Logger logger = Logger.getLogger(MessageSenderInlineImpl.class);
	private String tpActChannelName = "chn.tp.act";
	private String tpDctChannelName = "chn.tp.dct";
	private String consentToSelectionChannelName = "chn.consent.to.sel";
	private MessageChannel tpActChannel;
	private MessageChannel tpDctChannel;
	private MessageChannel consentToSelectionChannel;
	private ChannelResolver channelResolver;
	private long sendingTimeoutInMillis = 10;
	
	private MessageSenderInlineImpl(ChannelResolver channelResolver) {
		this.channelResolver = channelResolver;
	}
	
	@Override
	public void send(Object obj, WebServiceContext parameters) throws Throwable {
		if(obj == null || parameters == null) {
			logger.error("Incorrect input for inline processing, obj is: " + obj + " and parameters are: " + parameters);
			logger.info("Falling back to daemon approach for selection...");
			throw new OnMobileException(WebServiceConstants.INLINE_PARAMETERS_INVALID);
		}
		
		String provisioningFlow = parameters.getString(WebServiceConstants.param_provisioning);
			
		if(provisioningFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_SMR))
			realtimeProvisioning.provision(obj, parameters);
		else if(provisioningFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_TPACT)) {
			if(!sendOverChannel(obj, tpActChannel))
				throw new OnMobileException(WebServiceConstants.SPRING_MESSAGE_SENDING_FAILURE);
		} else if(provisioningFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_TPDCT)) {
			if(!sendOverChannel(obj, tpDctChannel))
				throw new OnMobileException(WebServiceConstants.SPRING_MESSAGE_SENDING_FAILURE);
		} else if(provisioningFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_CONSENT_TO_SELECTION)) {
			if(!sendOverChannel(obj, consentToSelectionChannel))
				throw new OnMobileException(WebServiceConstants.SPRING_MESSAGE_SENDING_FAILURE);
		} else
			queueProvisioning.provision(obj, parameters);
	}

	public IProvisioning getQueueProvisioning() {
		return queueProvisioning;
	}

	public void setQueueProvisioning(IProvisioning queueProvisioning) {
		this.queueProvisioning = queueProvisioning;
	}

	public IProvisioning getRealtimeProvisioning() {
		return realtimeProvisioning;
	}

	public void setRealtimeProvisioning(IProvisioning realtimeProvisioning) {
		this.realtimeProvisioning = realtimeProvisioning;
	}

	public String getTpActChannelName() {
		return tpActChannelName;
	}

	public void setTpActChannelName(String tpActChannelName) {
		this.tpActChannelName = tpActChannelName;
		tpActChannel = channelResolver.getChannel(tpActChannelName);
	}

	public String getTpDctChannelName() {
		return tpDctChannelName;
	}

	public void setTpDctChannelName(String tpDctChannelName) {
		this.tpDctChannelName = tpDctChannelName;
		tpDctChannel = channelResolver.getChannel(tpDctChannelName);
	}

	public ChannelResolver getChannelResolver() {
		return channelResolver;
	}

	public void setChannelResolver(ChannelResolver channelResolver) {
		this.channelResolver = channelResolver;
	}
	
	public long getSendingTimeoutInMillis() {
		return sendingTimeoutInMillis;
	}

	public void setSendingTimeoutInMillis(long sendingTimeoutInMillis) {
		this.sendingTimeoutInMillis = sendingTimeoutInMillis;
	}
	
	public String getConsentToSelectionChannelName() {
		return consentToSelectionChannelName;
	}

	public void setConsentToSelectionChannelName(String consentToSelectionChannelName) {
		this.consentToSelectionChannelName = consentToSelectionChannelName;
		consentToSelectionChannel = channelResolver.getChannel(consentToSelectionChannelName);
	}

	private boolean sendOverChannel(Object obj, MessageChannel outputChannel) {
        Message<Object> springMessage = MessageBuilder.withPayload(obj).build();
        if (logger.isDebugEnabled()) {
                logger.debug("Sending Obj message (" + obj + ") to channel " + outputChannel);
        }
        
        long startTime = System.currentTimeMillis();
        boolean sendStatus = outputChannel.send(springMessage, sendingTimeoutInMillis);
        if (sendStatus) {
            if (logger.isInfoEnabled()) {
                    logger.info("Sent obj: " + obj + " to channel " + outputChannel + " in " + (System.currentTimeMillis() - startTime) + "ms.");
            }
        }
        return sendStatus;
	}
}
