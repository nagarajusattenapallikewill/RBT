package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.services.common.Utility;

public class AllMsisdnValidImpl implements MSISDNServiceDefinition
{
	private static Logger logger = null;
	private static String circleId = "Default";
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.services.msisdninfo.MSISDNServiceDefinition#getSubscriberDetail(java.lang.String)
	 */
	static
	{
		logger = Logger.getLogger(MSISDNServiceDefinition.class);
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LOCAL_CIRCLE_ID", "Default");
		if(param == null || param.getValue() == null || param.getValue().trim().length() == 0)
			circleId = "Default";
		else
			circleId = param.getValue().trim();
	}
	
	@Override
	public SubscriberDetail getSubscriberDetail(MNPContext mnpContext)
	{
		String subscriberID = Utility.trimCountryPrefix(mnpContext.getSubscriberID());
		
		String circleID = null;
		boolean isPrepaid = false;
		boolean isValidSubscriber = false;
		HashMap<String, String> subscriberDetailsMap = null;

		if(Utility.isValidNumber(subscriberID))
		{	
			isValidSubscriber =	true;
			circleID = circleId;
			Parameters userTypeParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "DEFAULT_USER_TYPE", "POSTPAID");
			if (userTypeParam != null)
				isPrepaid = userTypeParam.getValue().trim().equalsIgnoreCase("PREPAID");
		}

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID,
				circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		logger.info("RBT:: subscriberDetail: " + subscriberDetail);

		return subscriberDetail;
	}
}
