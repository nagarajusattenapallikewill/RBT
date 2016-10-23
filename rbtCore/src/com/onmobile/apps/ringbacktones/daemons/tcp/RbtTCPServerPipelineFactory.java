/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtTCPServerPipelineFactory implements ChannelPipelineFactory
{
	private RbtTCPServerHandler serverHandler = null;

	/**
	 * @param serverHandler
	 */
	public RbtTCPServerPipelineFactory(RbtTCPServerHandler serverHandler)
	{
		super();
		this.serverHandler = serverHandler;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
	 */
	@Override
	public ChannelPipeline getPipeline() throws Exception
	{
		ChannelPipeline pipeline = pipeline();

		int maxFrameLength = 256;
		int lengthFieldOffset = 0;
		int lengthFieldLength = 4;
		int lengthAdjustment = 0;
		int initialBytesToStrip = 4;
		FrameDecoder frameDecoder = new LengthFieldBasedFrameDecoder(
				maxFrameLength, lengthFieldOffset, lengthFieldLength,
				lengthAdjustment, initialBytesToStrip);

		pipeline.addLast("framer", frameDecoder);
		pipeline.addLast("decoder", new RbtObjectDecoder());
		pipeline.addLast("encoder", new RbtObjectEncoder());

		pipeline.addLast("handler", serverHandler);

		return pipeline;
	}
}
