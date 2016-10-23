/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.executor.Command;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.promotions.callgraph.CallGraph.PromotionStatus;

/**
 * @author vinayasimha.patil
 * 
 */
public class PromotionConfirmator extends Command
{
	private static Logger logger = Logger.getLogger(PromotionConfirmator.class);

	private CallGraph callGraph = null;

	/**
	 * @param executor
	 * @param callGraph
	 */
	public PromotionConfirmator(RbtThreadPoolExecutor executor,
			CallGraph callGraph)
	{
		super(executor);

		this.callGraph = callGraph;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			if (logger.isInfoEnabled())
				logger.info("Processing: " + callGraph);

			String sender = RBTParametersUtils.getParamAsString(
					iRBTConstant.PROMOTION, "CONFIRMATION_SMS_SENDER", "RBT");
			String receiver = callGraph.getSubscriberID();
			String sms = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
					"PROMOTION", "ASKING_FOR_CONFIRMATION", null);
			if (sms == null)
			{
				logger.error("PROMOTION - ASKING_FOR_CONFIRMATION sms is not configured");
				return;
			}

			boolean smsSent = Tools.sendSMS(sender, receiver, sms,
					false);

			StringBuilder builder = new StringBuilder();
			builder.append(sender).append(",");
			builder.append(receiver).append(",");
			builder.append(sms).append(",");
			builder.append(smsSent);

			/*
			 * Log Format: SENDER,RECEIVER,SMS_TEXT,SMS_SENT_STATUS
			 */
			RBTEventLogger.logEvent(RBTEventLogger.Event.PROMOTION_CONFIRMATOR,
					builder.toString());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			callGraph.setPromotionStatus(PromotionStatus.CONFIRMATION_PENDING);
			CallGraphDao.update(callGraph);
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
		return callGraph.getSubscriberID();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("PromotionConfirmator [callGraph=");
		builder.append(callGraph);
		builder.append("]");
		return builder.toString();
	}
}
