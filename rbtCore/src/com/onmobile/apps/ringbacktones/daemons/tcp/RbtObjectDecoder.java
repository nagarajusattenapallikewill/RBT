/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.onmobile.apps.ringbacktones.daemons.tcp.requests.Request;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtObjectDecoder extends OneToOneDecoder
{
	private static Logger logger = Logger.getLogger(RbtObjectDecoder.class);

	/*
	 * (non-Javadoc)
	 * @see
	 * org.jboss.netty.handler.codec.oneone.OneToOneDecoder#decode(org.jboss
	 * .netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel,
	 * java.lang.Object)
	 */
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception
	{
		ChannelBuffer message = (ChannelBuffer) msg;

		int messageTypeID = message.readInt();
		MessageType messageType = MessageType
				.getMessageTypeByOrdinal(messageTypeID);

		if (messageType == MessageType.UNKNOWN)
			return null;

		Request request = messageType.createRequestObject(ctx);
		request.buildRequest(message);

		if (logger.isDebugEnabled())
			logger.debug("request: " + request);

		return request;
	}
}
