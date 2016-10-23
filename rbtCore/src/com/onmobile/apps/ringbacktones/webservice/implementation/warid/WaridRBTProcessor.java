/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.warid;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.ConsentTableImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;
import com.onmobile.common.exception.OnMobileException;

/**
 * @author vinayasimha.patil
 *
 */
public class WaridRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(WaridRBTProcessor.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getCos(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber)
	{
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}
	
	public String processActivation(WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		String isDaemon = task.getString(param_userInfo + "_"
				+ iRBTConstant.EXTRA_INFO_IS_DAEMON);
		if (isDaemon != null && isDaemon.equalsIgnoreCase("true")) {
			return super.processActivation(task);
		} else {
			if (consentPendingForSubscriber(subscriberID)) {
				return CONSENT_PENDING;
			}
			return super.processActivation(task);
		}
	}
	
	public String processSelection(WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		String isDaemon = task.getString(param_userInfo + "_"
				+ iRBTConstant.EXTRA_INFO_IS_DAEMON);
		if (isDaemon != null && isDaemon.equalsIgnoreCase("true")) {
			return super.processSelection(task);
		} else {
			if (consentPendingForSubscriber(subscriberID)) {
				return CONSENT_PENDING;
			}
			return super.processSelection(task);
		}
	}
	
	/**
	 * Boolean true if consent is pending with status 0,1,2 else false
	 * @param subscriberID
	 * @return consent pending status for Subscriber
	 */
	private boolean consentPendingForSubscriber(String subscriberID) {
		try {
			List<DoubleConfirmationRequestBean> requestBeanList = RBTDBManager
					.getInstance().getPendingRequestsByMsisdnNTypeNRequestTime(
							subscriberID, "ACT", null, null);
			if (null != requestBeanList && requestBeanList.size() > 0) {
					return true;
			}
		} catch (OnMobileException e) {
			logger.error("Error while retreiving Consent Pending records for subscriber:"
					+ subscriberID);
		}
		return false;
	}
	
}
