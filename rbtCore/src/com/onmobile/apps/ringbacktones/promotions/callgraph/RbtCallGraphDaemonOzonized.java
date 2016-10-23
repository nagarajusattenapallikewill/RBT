/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.Ozonized;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtCallGraphDaemonOzonized extends Ozonized
{
	private static final String COMPONENT_NAME = "RbtCallGraphDaemon";

	private static CallGraphCreatorExecutor callGraphCreatorExecutor = null;
	private static PromotionConfirmationExecutor promotionConfirmationExecutor = null;
	private static PromotionExecutor promotionExecutor = null;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		startDaemon();
	}

	private static void startDaemon()
	{
		boolean callGraphCreatorEnabled = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.PROMOTION, "CALLGRAPH_CREATOR_ENABLED", "TRUE");
		if (callGraphCreatorEnabled)
			callGraphCreatorExecutor = new CallGraphCreatorExecutor.Builder()
					.build();

		boolean promotionConfirmationEnabled = RBTParametersUtils
				.getParamAsBoolean(
						iRBTConstant.PROMOTION,
						"PROMOTION_CONFIRMATION_ENABLED", "TRUE");
		if (promotionConfirmationEnabled)
			promotionConfirmationExecutor = new PromotionConfirmationExecutor.Builder()
					.build();

		boolean promotionEnabled = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.PROMOTION, "PROMOTION_ENABLED", "TRUE");
		if (promotionEnabled)
			promotionExecutor = new PromotionExecutor.Builder().build();
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.Ozonized#getComponentName()
	 */
	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.Ozonized#startComponent()
	 */
	@Override
	public int startComponent()
	{
		startDaemon();
		return JAVA_COMPONENT_SUCCESS;
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.Ozonized#stopComponent()
	 */
	@Override
	public void stopComponent()
	{
		if (callGraphCreatorExecutor != null)
			callGraphCreatorExecutor.shutdownNow();

		if (promotionConfirmationExecutor != null)
			promotionConfirmationExecutor.shutdownNow();

		if (promotionExecutor != null)
			promotionExecutor.shutdownNow();
	}
}
