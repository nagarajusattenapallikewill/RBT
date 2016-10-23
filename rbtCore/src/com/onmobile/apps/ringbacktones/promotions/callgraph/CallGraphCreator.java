/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.executor.Command;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;

/**
 * @author vinayasimha.patil
 * 
 */
public class CallGraphCreator extends Command
{
	private static Logger logger = Logger.getLogger(CallGraphCreator.class);

	private TonePlayerCDR tonePlayerCDR = null;

	/**
	 * @param executor
	 * @param tonePlayerCDR
	 */
	public CallGraphCreator(RbtThreadPoolExecutor executor,
			TonePlayerCDR tonePlayerCDR)
	{
		super(executor);

		if (tonePlayerCDR == null)
			throw new IllegalArgumentException("tonePlayerCDR can not be null");

		this.tonePlayerCDR = tonePlayerCDR;
	}

	/**
	 * @return the tonePlayerCDR
	 */
	public TonePlayerCDR getTonePlayerCDR()
	{
		return tonePlayerCDR;
	}

	/**
	 * @param tonePlayerCDR
	 *            the tonePlayerCDR to set
	 */
	public void setTonePlayerCDR(TonePlayerCDR tonePlayerCDR)
	{
		this.tonePlayerCDR = tonePlayerCDR;
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
				logger.info("Processing : " + tonePlayerCDR);

			String subscriberID = tonePlayerCDR.getCalledID();
			CallGraph callGraph = CallGraphDao.getBySubscriberID(subscriberID);
			if (logger.isDebugEnabled())
				logger.debug("Updating callGraph: " + callGraph);

			if (callGraph == null)
			{
				callGraph = CallGraphUtils
						.createCallGraphFromTonePlayerCDR(tonePlayerCDR);
			}
			else
			{
				CallGraphUtils.updateCallGraphWithTonePlayerCDR(callGraph,
						tonePlayerCDR);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
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
		return tonePlayerCDR.getCalledID();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CallGraphCreator [tonePlayerCDR=");
		builder.append(tonePlayerCDR);
		builder.append("]");
		return builder.toString();
	}
}
