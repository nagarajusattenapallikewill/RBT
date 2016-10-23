/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;

import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.RBTChargePerCallRequest;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.RBTRealTimeSongInfoRequest;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.Request;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.ViralPromotionRequest;

/**
 * @author vinayasimha.patil
 * 
 */
public enum MessageType {
	UNKNOWN(null), VIRAL_PROMOTION(ViralPromotionRequest.class), CHARGE_PER_CALL(
			RBTChargePerCallRequest.class), REAL_TIME_SONG_INFO(
			RBTRealTimeSongInfoRequest.class);

	private static Logger logger = Logger.getLogger(MessageType.class);

	private Class<? extends Request> requestClass = null;

	MessageType(Class<? extends Request> requestClass) {
		this.requestClass = requestClass;
	}

	public static MessageType getMessageTypeByOrdinal(int ordinal) {
		MessageType[] messageTypes = values();
		logger.debug("messageTypes:" + messageTypes.toString() + "ordinal:"
				+ ordinal);
		logger.debug("messageTypes.length:" + messageTypes.length);
		if (ordinal < 0 || ordinal >= messageTypes.length)
			return UNKNOWN;

		return messageTypes[ordinal];
	}

	public Request createRequestObject(ChannelHandlerContext ctx) {
		try {
			Constructor<? extends Request> requestConstructor = requestClass
					.getConstructor(RbtThreadPoolExecutor.class,
							ChannelHandlerContext.class, MessageType.class);

			ChannelPipeline pipeline = ctx.getPipeline();
			RbtTCPServerHandler handler = (RbtTCPServerHandler) pipeline
					.get("handler");
			RbtThreadPoolExecutor executor = handler.getHandlerExecutor();

			Request request = requestConstructor.newInstance(executor, ctx,
					this);
			return request;
		} catch (SecurityException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			logger.error(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (InstantiationException e) {
			logger.error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
}
