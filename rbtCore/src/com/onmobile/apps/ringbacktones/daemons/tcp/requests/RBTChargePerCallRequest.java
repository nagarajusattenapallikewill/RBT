package com.onmobile.apps.ringbacktones.daemons.tcp.requests;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.daemons.tcp.MessageType;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.RBTChargePerCallService;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallTxn;

/*
 * Request format is 
 * <message_type>, <caller_id>, <called_id>,<tone_id>.
 * For ex:
 * 2-9886012345-9986012660-2452493.
 * Here, the message_type should always be 2. 
 */
public class RBTChargePerCallRequest extends Request implements Serializable {

	private static final long serialVersionUID = -3465793368857042943L;

	private static final Logger logger = Logger
			.getLogger(RBTChargePerCallRequest.class);

	private static final Logger TCP_PPU_TXN_LOG = Logger
			.getLogger("TcpPpuTransactionLogger");

	private RBTChargePerCallTxn rbtChargePerCallTxn = null;

	private RBTChargePerCallService rbtChargePerCallService = new RBTChargePerCallService();

	private final RBTDBManager rbtdbManager = RBTDBManager.getInstance();
	// RBT-14123:TataDocomo Changes.
	private static int callDurationIntervalMin = RBTParametersUtils
			.getParamAsInt("DAEMON", "PPU_CALL_DURATION_TO_ACCEPT", -2);

	public RBTChargePerCallRequest(RbtThreadPoolExecutor executor,
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
			// RBT-14123:TataDocomo Changes.
			short callDuration = -1;
			// Read 2 bytes for callDuration Only for TataDocomo
			if (callDurationIntervalMin > -2) {
				callDuration = message.readShort();
			}
			// Variable bytes for toneId
			bytes = new byte[message.readableBytes()];
			message.readBytes(bytes);
			String wavFile = new String(bytes).trim();

			callerId = rbtdbManager.subID(callerId);
			calledId = rbtdbManager.subID(calledId);

			rbtChargePerCallTxn = new RBTChargePerCallTxn(callerId, calledId,
					getTimestamp(), wavFile);
			// RBT-14123: TataDocomo PPU changes
			rbtChargePerCallTxn.setCallDuration(callDuration);
			if (logger.isDebugEnabled()) {
				logger.debug("Received rbtChargePerCallTxn: "
						+ rbtChargePerCallTxn);
			}
		} catch (Exception e) {
			TCP_PPU_TXN_LOG.info(rbtChargePerCallTxn.getLogString(false,
					"Invalid Message",callDurationIntervalMin, false));
			logger.error("Unable to process message: " + message);
			
		}
	}

	@Override
	public String getUniqueName() {
		return rbtChargePerCallTxn.getCallerId();
	}

	@Override
	public void run() {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing: " + rbtChargePerCallTxn);
		}
		rbtChargePerCallService.process(rbtChargePerCallTxn, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.Command#rejectedExecution
	 * (java.util.concurrent.ThreadPoolExecutor)
	 */
	@Override
	public void rejectedExecution(ThreadPoolExecutor threadPoolExecutor) {
		TCP_PPU_TXN_LOG.info(rbtChargePerCallTxn.getLogString(false));
		try {
			logger.warn("Since Executor framework ThreadPool is full, "
					+ "saving the chargePerCall: " + rbtChargePerCallTxn
					+ " to process it in later");
			rbtChargePerCallService.save(rbtChargePerCallTxn);
		} catch (Exception e) {
			logger.error("Unable to save: " + rbtChargePerCallTxn
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
		return "ChargePerCallRequest [chargePerCall=" + rbtChargePerCallTxn
				+ "]";
	}

}
