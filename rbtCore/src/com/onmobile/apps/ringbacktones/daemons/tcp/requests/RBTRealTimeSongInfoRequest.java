package com.onmobile.apps.ringbacktones.daemons.tcp.requests;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.daemons.tcp.MessageType;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.RBTRealTimeSongInfoService;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;

public class RBTRealTimeSongInfoRequest extends Request implements Serializable {
	private static final long serialVersionUID = -3465793368857042945L;

	private static final Logger logger = Logger
			.getLogger(RBTRealTimeSongInfoRequest.class);

	private CurrentPlayingSongBean currentPlayingSongBean = null;

	private RBTRealTimeSongInfoService rbtRealTimeSongInfoService = new RBTRealTimeSongInfoService();

	private final RBTDBManager rbtdbManager = RBTDBManager.getInstance();

	public RBTRealTimeSongInfoRequest(RbtThreadPoolExecutor executor,
			ChannelHandlerContext channelHandlerContext, MessageType messageType) {
		super(executor, channelHandlerContext, messageType);
	}

	@Override
	public void buildRequest(ChannelBuffer message) {
		try {
			byte[] bytes = new byte[20];
			// Read first 20 bytes for callerId
			message.readBytes(bytes);
			String callerId = new String(bytes).trim();
			// Read next 20 bytes for calledId
			message.readBytes(bytes);
			String calledId = new String(bytes).trim();
			
			int categoryId = message.readInt();
			
			// Variable bytes for toneId
			bytes = new byte[message.readableBytes()];
			message.readBytes(bytes);
			String wavFile = new String(bytes).trim();

			callerId = rbtdbManager.subID(callerId);
			calledId = rbtdbManager.subID(calledId);

			currentPlayingSongBean = new CurrentPlayingSongBean(callerId,
					calledId, wavFile, categoryId);
			if (logger.isDebugEnabled()) {
				logger.debug("Received currentPlayingSongBean: "
						+ currentPlayingSongBean);
			}
		} catch (Exception e) {
			logger.error("Unable to process message: " + message);

		}
	}

	@Override
	public String getUniqueName() {
		return currentPlayingSongBean.getCallerId();
	}

	@Override
	public void run() {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing: " + currentPlayingSongBean);
		}
		rbtRealTimeSongInfoService.process(currentPlayingSongBean);
	}

	@Override
	public void rejectedExecution(ThreadPoolExecutor threadPoolExecutor) {
		try {
			logger.warn("Since Executor framework ThreadPool is full, "
					+ "saving the chargePerCall: " + currentPlayingSongBean
					+ " to process it in later");
			rbtRealTimeSongInfoService.process(currentPlayingSongBean);
		} catch (Exception e) {
			logger.error("Unable to save: " + currentPlayingSongBean
					+ ". Exception: " + e.getMessage(), e);
		}
	}

	/**
	 * @return current time in ddMMyyyyHHmmSSSS format
	 */
	private Date getTimestamp() {
		return new Date();
	}

	@Override
	public String toString() {
		return "currentPlayingSongBean [currentPlayingSongBean="
				+ currentPlayingSongBean + "]";
	}
}
