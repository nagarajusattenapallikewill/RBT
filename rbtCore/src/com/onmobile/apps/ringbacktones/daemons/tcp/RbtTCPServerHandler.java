/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.Request;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtTCPServerHandler extends SimpleChannelUpstreamHandler
{
	private static Logger logger = Logger.getLogger(RbtTCPServerHandler.class);

	private RbtThreadPoolExecutor handlerExecutor = null;

	/**
	 * @param handlerExecutor
	 */
	public RbtTCPServerHandler(RbtThreadPoolExecutor handlerExecutor)
	{
		super();
		this.handlerExecutor = handlerExecutor;
	}

	/**
	 * @return the handlerExecutor
	 */
	public RbtThreadPoolExecutor getHandlerExecutor()
	{
		return handlerExecutor;
	}

	/**
	 * @param handlerExecutor
	 *            the handlerExecutor to set
	 */
	public void setHandlerExecutor(RbtThreadPoolExecutor handlerExecutor)
	{
		this.handlerExecutor = handlerExecutor;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#handleUpstream(org
	 * .jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.ChannelEvent)
	 */
	@Override
	public void handleUpstream(
			ChannelHandlerContext ctx, ChannelEvent e) throws Exception
	{
		if (e instanceof ChannelStateEvent)
		{
			if (logger.isInfoEnabled())
				logger.info(e.toString());
		}

		super.handleUpstream(ctx, e);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(
	 * org.jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(
			ChannelHandlerContext ctx, MessageEvent e)
	{
		Request request = (Request) e.getMessage();
		handlerExecutor.execute(request);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(
	 * org.jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionCaught(
			ChannelHandlerContext ctx, ExceptionEvent e)
	{
		logger.warn("Unexpected exception from downstream.", e.getCause());
		e.getChannel().close();
	}
}
