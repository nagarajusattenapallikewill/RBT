/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtObjectEncoder extends OneToOneEncoder
{
	/*
	 * (non-Javadoc)
	 * @see
	 * org.jboss.netty.handler.codec.oneone.OneToOneEncoder#encode(org.jboss
	 * .netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel,
	 * java.lang.Object)
	 */
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception
	{
		return msg;
	}
}
