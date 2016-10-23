package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.aircel;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

/**
 * <p>Sub-class of PromoProcessor class for overriding the methods which process
 * the thirdparty requests
 * 
 * @author sridhar.sindiri
 *
 */
public class AircelPromoProcessor extends PromoProcessor
{

	/**
	 * @throws RBTException
	 */
	public AircelPromoProcessor() throws RBTException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processThirdPartyRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processThirdPartyRequest(Task task)
	{
		String msisdn = task.getString(param_MSISDN);
		if (msisdn == null || msisdn.length() == 0 || msisdn.equalsIgnoreCase("null"))
		{
			logger.info("MSISDN parameter not passed or invalid number passed, so not processing the request");
			task.setObject(param_response, "1");
			return;
		}

		String service = task.getString(param_service);
		service = (service != null) ? service.replaceAll("RBT_ACT_", "") : null;
		SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(service);
		if (service == null || subClass == null)
		{
			logger.info("service parameter not passed or invalid serviceID passed, so not processing the request");
			task.setObject(param_response, "1");
			return;
		}

		String transID = task.getString(param_tid);
		if (transID == null || transID.length() == 0 || transID.equalsIgnoreCase("null"))
		{
			logger.info("tid parameter not passed or invalid tid passed, so not processing the request");
			task.setObject(param_response, "1");
			return;
		}

		task.setObject(param_subscriberID, msisdn);
		Subscriber subscriber = getSubscriber(task);
		
		String subStatus = getSubscriberBlockedStatus(subscriber);
		if(subStatus != null)
		{
			logger.info("User is not valid, so not processing the request");
			task.setObject(param_response, "1");
			return;
		}

		if (!Utility.isDeactiveSubscriber(subscriber.getStatus()))
		{
			logger.info("User is already active, so not processing the request");
			task.setObject(param_response, "1");
			return;
		}

		String actInfoStr = "tid:" + transID + "|";
		String mode = RBTParametersUtils.getParamAsString(PROMOTION, "AIRCEL_THIRDPARTY_DEFAULT_MODE", "IVR");
		task.setObject(param_SUBSCRIPTION_CLASS, service);
		task.setObject(param_ACTIVATED_BY, mode);
		task.setObject(param_ACTIVATION_INFO, actInfoStr);
		processActivation(task);

		String response = task.getString(param_response);
		if (response.equalsIgnoreCase("SUCCESS"))
		{
			task.setObject(param_response, "0");
		}
		else
		{
			logger.info("Failure response : " + response);
			task.setObject(param_response, "1");
		}
	}
}
