/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp.requests;


import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.daemons.tcp.MessageType;
import com.onmobile.apps.ringbacktones.daemons.tcp.supporters.ViralPromotion;

/**
 * @author vinayasimha.patil
 * 
 */
public class ViralPromotionRequest extends Request
{
	private static Logger logger = Logger
			.getLogger(ViralPromotionRequest.class);

	private String callerID = null;
	private String calledID = null;
	private long calledTime;
	private short callDuration;
	private String rbtWavFile = null;

	private boolean validationRequired = true;
	private String circleID = null;
	private String callerLanguage = null;
	private static int tps = RBTParametersUtils.getParamAsInt("VIRAL",
			"INCOMING_AND_PENDING_TOTAL_TPS", 0);
	private static Object lock = new Object();
//	private static long intervalStart = ViralPromotion.getIntervalStart();
	private static long intervalStart = System.currentTimeMillis();
	public static volatile int rejectRequestCounter = 0;
	
	
	public ViralPromotionRequest() {
		super(null, null, MessageType.VIRAL_PROMOTION);
	}
	
	/**
	 * @param executor
	 * @param channelHandlerContext
	 * @param messageType
	 */
	public ViralPromotionRequest(RbtThreadPoolExecutor executor,
			ChannelHandlerContext channelHandlerContext, MessageType messageType)
	{
		super(executor, channelHandlerContext, messageType);
	}

	/**
	 * @return the callerID
	 */
	public String getCallerID()
	{
		return callerID;
	}

	/**
	 * @param callerID
	 *            the callerID to set
	 */
	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	/**
	 * @return the calledID
	 */
	public String getCalledID()
	{
		return calledID;
	}

	/**
	 * @param calledID
	 *            the calledID to set
	 */
	public void setCalledID(String calledID)
	{
		this.calledID = calledID;
	}

	/**
	 * @return the calledTime
	 */
	public long getCalledTime()
	{
		return calledTime;
	}

	/**
	 * @param calledTime
	 *            the calledTime to set
	 */
	public void setCalledTime(long calledTime)
	{
		this.calledTime = calledTime;
	}

	/**
	 * @return the callDuration
	 */
	public short getCallDuration()
	{
		return callDuration;
	}

	/**
	 * @param callDuration
	 *            the callDuration to set
	 */
	public void setCallDuration(short callDuration)
	{
		this.callDuration = callDuration;
	}

	/**
	 * @return the rbtWavFile
	 */
	public String getRbtWavFile()
	{
		return rbtWavFile;
	}

	/**
	 * @param rbtWavFile
	 *            the rbtWavFile to set
	 */
	public void setRbtWavFile(String rbtWavFile)
	{
		this.rbtWavFile = rbtWavFile;
	}

	/**
	 * @return the validationRequired
	 */
	public boolean isValidationRequired()
	{
		return validationRequired;
	}

	/**
	 * @param validationRequired
	 *            the validationRequired to set
	 */
	public void setValidationRequired(boolean validationRequired)
	{
		this.validationRequired = validationRequired;
	}

	/**
	 * @return the circleID
	 */
	public String getCircleID()
	{
		return circleID;
	}

	/**
	 * @param circleID
	 *            the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @return the callerLanguage
	 */
	public String getCallerLanguage()
	{
		return callerLanguage;
	}

	/**
	 * @param callerLanguage
	 *            the callerLanguage to set
	 */
	public void setCallerLanguage(String callerLanguage)
	{
		this.callerLanguage = callerLanguage;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.tcp.requests.Request#buildRequest
	 * (org.jboss.netty.buffer.ChannelBuffer)
	 */
	@Override
	public void buildRequest(ChannelBuffer message)
	{
		byte[] bytes = new byte[20];
		message.readBytes(bytes);
		callerID = new String(bytes).trim();

		message.readBytes(bytes);
		calledID = new String(bytes).trim();

		calledTime = message.readLong();
		callDuration = message.readShort();

		bytes = new byte[message.readableBytes()];
		message.readBytes(bytes);
		rbtWavFile = new String(bytes).trim();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if (logger.isInfoEnabled())
			logger.info("Processing : " + this);
		try
		{
			ViralPromotion.sendPromotion(this);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.Command#rejectedExecution
	 * (java.util.concurrent.ThreadPoolExecutor)
	 */
	@Override
	public void rejectedExecution(ThreadPoolExecutor threadPoolExecutor)
	{
		try
		{
			if (ViralPromotion.isBlackOutPeriodNow())
			{
				logger.info("Blackout Period, so not processing the request");
				return;
			}
			
			Set<String> testNumers = ViralPromotion.getTestNumber();
			if (testNumers != null && !testNumers.contains(callerID))
			{
				logger.info("Testing mode is enabled and caller " + callerID
						+ " is not in the test number list.");
				return;
			}

			
			if (tps == 0 || (tps > 0 && !isTpsReached())) {
				ViralPromotion.addViralData(this, "VIRAL_PENDING", null, null,
						null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	
	
	public static boolean isTpsReached()
 {

		if (tps == 0) {
			return false;
		}

		synchronized (lock) {
			if (intervalStart + 1000 > System.currentTimeMillis()) {
				logger.info("rejectRequestCounter is : " + rejectRequestCounter);
				rejectRequestCounter++;
				if ((rejectRequestCounter) > tps) {
					logger.info("rejectRequestCounter is : "
							+ rejectRequestCounter);
					return true;
				} else {
					return false;
				}
			} else {
				intervalStart = System.currentTimeMillis();
				rejectRequestCounter = 1;
				return false;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.Command#getUniqueName()
	 */
	@Override
	public String getUniqueName()
	{
		return callerID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ViralPromotionRequest [messageType=");
		builder.append(messageType);
		builder.append(", callDuration=");
		builder.append(callDuration);
		builder.append(", calledID=");
		builder.append(calledID);
		builder.append(", calledTime=");
		builder.append(calledTime);
		builder.append(", callerID=");
		builder.append(callerID);
		builder.append(", callerLanguage=");
		builder.append(callerLanguage);
		builder.append(", circleID=");
		builder.append(circleID);
		builder.append(", rbtWavFile=");
		builder.append(rbtWavFile);
		builder.append(", validationRequired=");
		builder.append(validationRequired);
		builder.append("]");
		return builder.toString();
	}
}
