package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.uninor;

import java.util.HashMap;
import java.util.Map;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * <p>Sub-class of PromoProcessor class for overriding the methods which process
 * the thirdparty requests
 * 
 * @author sridhar.sindiri
 * 
 */
public class UninorPromoProcessor extends PromoProcessor
{
	/**
	 * @throws RBTException
	 */
	public UninorPromoProcessor() throws RBTException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor#getTask(java.util.HashMap)
	 */
	public Task getTask(HashMap<String, String> requestParams)
	{
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);

		if (requestParams.containsKey(param_REQUEST)) {
			return super.getTask(requestParams);
		}

		Task task = new Task(requestParams.get("Action"), taskSession);
		logger.info("RBT:: task: " + task);
		return task;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processThirdPartyRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processThirdPartyRequest(Task task)
	{
		String taskAction = task.getTaskAction();
		logger.info("RBT::action-" + taskAction + "::params-" + task.getTaskSession());
		String response = null;
		if (taskAction.equalsIgnoreCase("Add"))
			response = processThirdPartyActivationRequest(task);
		else if (taskAction.equals("Delete"))
			response = processThirdPartyDeactivationRequest(task);

		if (response != null)
			task.setObject(param_response, response);
	}

	/**
	 * @param task
	 * @return
	 */
	private String processThirdPartyActivationRequest(Task task)
	{
		String msisdn = task.getString(param_MSISDN);
		if (msisdn == null || msisdn.length() == 0 || msisdn.equalsIgnoreCase("null"))
		{
			logger.info("MSISDN parameter not passed or invalid number passed, so not processing the request");
			return RBTParametersUtils.getParamAsString("COMMON", "OMUNINOR_RESP_" + Resp_InvalidPrefix, Resp_InvalidPrefix);
		}

		String productCode = task.getString(param_ProductCode);
		productCode = (productCode != null) ? productCode.replaceAll("RBT_ACT_", "") : null;
		SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(productCode);
		if (productCode == null || subClass == null)
		{
			logger.info("service parameter not passed or invalid serviceID passed, so not processing the request");
			return RBTParametersUtils.getParamAsString("COMMON", "OMUNINOR_RESP_" + Resp_invalidParam, Resp_invalidParam);
		}

		task.setObject(param_subscriberID, msisdn);
		Subscriber subscriber = getSubscriber(task);
		// RBT-14301: Uninor MNP changes.
		boolean validatePrefix = validateCircleIdParam(task, subscriber);
		if (task.containsKey(param_response)
				&& task.getString(param_response).equalsIgnoreCase(
						Resp_invalidParam)) {
			logger.info("RBT:: Invalid CIRCLE_ID For Subscriber. response: "
					+ "INVALID" + " & param_response: "
					+ task.getString(param_response));
			return "INVALID";
		}
		// RBT-14301: Uninor MNP changes.
		if (!validatePrefix) {
			if (task.containsKey(param_CIRCLE_ID)) {
				String redirectURL = getRedirectionURL(task);
				if (redirectURL != null) {
					logger.info("MSISDN invalid prefix");
					return RBTParametersUtils.getParamAsString("COMMON",
							"OMUNINOR_RESP_" + Resp_InvalidPrefix,
							Resp_InvalidPrefix);
				}
			}
		} else if (!subscriber.isValidPrefix()) {
			logger.info("MSISDN invalid prefix");
			return RBTParametersUtils.getParamAsString("COMMON",
					"OMUNINOR_RESP_" + Resp_InvalidPrefix, Resp_InvalidPrefix);
		}
		String subStatus = getSubscriberBlockedStatus(subscriber);
		if(subStatus != null)
		{
			logger.info("User is not valid, so not processing the request");
			return subStatus;
		}

		if (!Utility.isDeactiveSubscriber(subscriber.getStatus()))
		{
			logger.info("User is already active, so not processing the request");
			return RBTParametersUtils.getParamAsString("COMMON", "OMUNINOR_RESP_" + Resp_ActivationBlocked, Resp_ActivationBlocked);
		}

		String agentID = task.getString(param_AgentID);
		String vendorName = task.getString(param_VendorName);
		String actInfoStr = "agentid:" + agentID + "|vendorname:" + vendorName + "|";
		task.setObject(param_SUBSCRIPTION_CLASS, productCode);
		task.setObject(param_ACTIVATED_BY, "CCC");
		task.setObject(param_ACTIVATION_INFO, actInfoStr);
		processActivation(task);
		return task.getString(param_response);
	}
	

	/**
	 * @param task
	 * @return
	 */
	private String processThirdPartyDeactivationRequest(Task task)
	{
		String msisdn = task.getString(param_MSISDN);
		if (msisdn == null || msisdn.length() == 0 || msisdn.equalsIgnoreCase("null"))
		{
			logger.info("MSISDN parameter not passed or invalid number passed, so not processing the request");
			return RBTParametersUtils.getParamAsString("COMMON", "OMUNINOR_RESP_" + Resp_InvalidPrefix, Resp_InvalidPrefix);
		}

		task.setObject(param_subscriberID, msisdn);
		Subscriber subscriber = getSubscriber(task);
		// RBT-14301: Uninor MNP changes.
		boolean validatePrefix = validateCircleIdParam(task, subscriber);
		if (task.containsKey(param_response)
				&& task.getString(param_response).equalsIgnoreCase(
						Resp_invalidParam)) {
			logger.info("RBT:: Invalid CIRCLE_ID For Subscriber. response: "
					+ "INVALID" + " & param_response: "
					+ task.getString(param_response));
			return "INVALID";
		}
		// RBT-14301: Uninor MNP changes.
		if (!validatePrefix) {
			if (task.containsKey(param_CIRCLE_ID)) {
				String redirectURL = getRedirectionURL(task);
				if (redirectURL != null) {
					logger.info("MSISDN invalid prefix");
					return RBTParametersUtils.getParamAsString("COMMON",
							"OMUNINOR_RESP_" + Resp_InvalidPrefix,
							Resp_InvalidPrefix);
				}
			}
		} else if (!subscriber.isValidPrefix()) {
			logger.info("MSISDN invalid prefix");
			return RBTParametersUtils.getParamAsString("COMMON",
					"OMUNINOR_RESP_" + Resp_InvalidPrefix, Resp_InvalidPrefix);
		}
		
		task.setObject(param_subscriber, subscriber);

		String agentID = task.getString(param_AgentID);
		String vendorName = task.getString(param_VendorName);
		String dctInfoStr = "DCT:agentid:" + agentID + "|vendorname:" + vendorName + "|";
		task.setObject(param_DEACTIVATED_BY, "CCC");
		task.setObject(param_DEACTIVATION_INFO, dctInfoStr);
		return processDeactivation(task);
	}
}
