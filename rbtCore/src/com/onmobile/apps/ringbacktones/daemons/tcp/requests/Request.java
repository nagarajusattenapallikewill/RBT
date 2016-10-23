/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp.requests;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.onmobile.apps.ringbacktones.daemons.executor.Command;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.daemons.tcp.MessageType;

/**
 * @author vinayasimha.patil
 * 
 */
public abstract class Request extends Command
{
	protected ChannelHandlerContext channelHandlerContext = null;
	protected MessageType messageType = null;

	/**
	 * @param executorContext
	 * @param channelHandlerContext
	 * @param messageType
	 */
	public Request(RbtThreadPoolExecutor executor,
			ChannelHandlerContext channelHandlerContext, MessageType messageType)
	{
		super(executor);
		this.channelHandlerContext = channelHandlerContext;
		this.messageType = messageType;
	}

	/**
	 * @return the channelHandlerContext
	 */
	public ChannelHandlerContext getChannelHandlerContext()
	{
		return channelHandlerContext;
	}

	/**
	 * @param channelHandlerContext
	 *            the channelHandlerContext to set
	 */
	public void setChannelHandlerContext(
			ChannelHandlerContext channelHandlerContext)
	{
		this.channelHandlerContext = channelHandlerContext;
	}

	/**
	 * @return the messageType
	 */
	public MessageType getMessageType()
	{
		return messageType;
	}

	/**
	 * @param messageType
	 *            the messageType to set
	 */
	public void setMessageType(MessageType messageType)
	{
		this.messageType = messageType;
	}

	public abstract void buildRequest(ChannelBuffer message);

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public abstract String toString();
}
