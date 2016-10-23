/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.executor.Command;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.promotions.callgraph.CallGraph.PromotionStatus;
import com.onmobile.apps.ringbacktones.promotions.contest.ContestUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;

/**
 * @author vinayasimha.patil
 * 
 */
public class Promoter extends Command
{
	private static Logger logger = Logger.getLogger(Promoter.class);

	private CallGraph callGraph = null;

	/**
	 * @param executor
	 * @param callGraph
	 */
	public Promoter(RbtThreadPoolExecutor executor, CallGraph callGraph)
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

			boolean validateNumber = RBTParametersUtils.getParamAsBoolean(
					iRBTConstant.PROMOTION, "VALIDATE_MSISDN", "FALSE");

			int maxNoOfPromotionsByInfluencer = RBTParametersUtils
					.getParamAsInt(iRBTConstant.PROMOTION,
							"MAX_PROMOTIONS_BY_INFLUENCER", 0);

			String sender = callGraph.getSubscriberID();
			Set<String> frequentcallers = callGraph.getFrequentCallers();
			if (frequentcallers != null)
			{
				Clip clip = null;
				if (callGraph.getRbtClipID() != 0)
				{
					clip = RBTCacheManager.getInstance().getClip(
							callGraph.getRbtClipID());
				}

				String sms = null;
				if (clip == null)
				{
					sms = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
							"PROMOTION", "WITHOUT_CONTENT_NAME", null);
				}
				else
				{
					sms = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
							"PROMOTION", "WITH_CONTENT_NAME", null);

					sms = sms.replaceAll("%SONG_NAME%", clip.getClipName());
				}

				if (sms == null)
				{
					logger.error("PROMOTION sms is not configured");
					return;
				}

				String contestEndTime = ContestUtils.getContestEndTime();
				if (contestEndTime != null)
					sms = sms.replaceFirst("%CONTEST_HOUR%", contestEndTime);

				int noOfPromotions = 0;
				for (String receiver : frequentcallers)
				{
					if (validateNumber)
					{
						SubscriberDetail subscriberDetail = RbtServicesMgr
								.getSubscriberDetail(new MNPContext(receiver,
										"PROMOTION"));
						if (subscriberDetail == null
								|| subscriberDetail.getCircleID() == null)
						{
							logger.info(receiver
									+ " is not valid number, so not promoting");
						}
						else if (logger.isDebugEnabled())
						{
							logger.debug(receiver + " circle ID: "
									+ subscriberDetail.getCircleID());
						}
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
					RBTEventLogger.logEvent(RBTEventLogger.Event.PROMOTION,
							builder.toString());

					if (smsSent && maxNoOfPromotionsByInfluencer > 0)
					{
						// If SMS sent successfully and
						// MAX_PROMOTIONS_BY_INFLUENCER is non zero (zero means
						// unlimited), then only number of promotions sent will
						// be checked against maximum number of promotions can
						// be sent.
						noOfPromotions++;
						if (noOfPromotions >= maxNoOfPromotionsByInfluencer)
						{
							if (logger.isDebugEnabled())
							{
								logger.debug("Sent "
										+ noOfPromotions
										+ " number of promotions. Maximum promotions can be sent is  "
										+ maxNoOfPromotionsByInfluencer
										+ ". So stopping promotion from this influencer.");
							}

							break;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			callGraph.setPromotionStatus(PromotionStatus.PROMOTION_SENT);
			callGraph.setPromotedTime(new Date());
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
		builder.append("Promoter [callGraph=");
		builder.append(callGraph);
		builder.append("]");
		return builder.toString();
	}
}
