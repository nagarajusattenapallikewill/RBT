/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.ttml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 */
public class TTMLRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(TTMLRBTProcessor.class);

	private List<String> blacklistedActivationModes = null;
	private List<String> blacklistedDeactivationModes = null;

	/**
	 * 
	 */
	public TTMLRBTProcessor()
	{
		super();

		String blacklistedActivationModesStr = RBTParametersUtils
				.getParamAsString(iRBTConstant.COMMON,
						"BLACKLISTED_ACTIVATION_MODES", "");
		blacklistedActivationModes = Arrays
				.asList(blacklistedActivationModesStr.toUpperCase().split(","));

		String blacklistedDeactivationModesStr = RBTParametersUtils
				.getParamAsString(iRBTConstant.COMMON,
						"BLACKLISTED_DEACTIVATION_MODES", "");
		blacklistedDeactivationModes = Arrays
				.asList(blacklistedDeactivationModesStr.toUpperCase().split(
						","));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor
	 * #processActivation(com.onmobile.apps.ringbacktones.webservice.common.
	 * WebServiceContext)
	 */
	@Override
	public String processActivation(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		try
		{
			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
			if (subscriber != null
					&& rbtDBManager.isSubscriberDeactivated(subscriber))
			{
				// Subscriber entry is there in table but deactivated.

				String mode = getMode(webServiceContext).toUpperCase();
				if (blacklistedDeactivationModes.contains(subscriber
						.deactivatedBy().toUpperCase())
						&& blacklistedActivationModes.contains(mode))
				{
					long blockedActivationPeriodInDays = RBTParametersUtils
							.getParamAsLong(iRBTConstant.COMMON,
									"BLOCKED_ACTIVATION_PERIOD_IN_DAYS", 30);
					if ((System.currentTimeMillis() - subscriber.endDate()
							.getTime()) <= (blockedActivationPeriodInDays * 24 * 60 * 60 * 1000))
					{
						logger.info("response: " + ACTIVATION_BLOCKED);
						return ACTIVATION_BLOCKED;
					}
				}
			}

			return super.processActivation(webServiceContext);
		}
		catch (Exception e)
		{
			logger.error("", e);
			response = TECHNICAL_DIFFICULTIES;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor
	 * #getCos(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber)
	{
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getUserInfoMap(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected HashMap<String, String> getUserInfoMap(WebServiceContext task)
	{
		HashMap<String, String> userInfoMap = super.getUserInfoMap(task);

		if (task.containsKey(param_operatorUserInfo))
			userInfoMap.put(iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT, task.getString(param_operatorUserInfo));

		logger.info("RBT:: response: " + userInfoMap);
		return userInfoMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getSelectionInfoMap(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected HashMap<String, String> getSelectionInfoMap(WebServiceContext task)
	{
		HashMap<String, String> selectionInfoMap = super.getSelectionInfoMap(task);

		if (task.containsKey(param_operatorUserInfo))
			selectionInfoMap.put(iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT, task.getString(param_operatorUserInfo));

		logger.info("RBT:: response: " + selectionInfoMap);
		return selectionInfoMap;
	}
}
