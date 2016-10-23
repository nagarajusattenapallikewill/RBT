package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.services.common.Utility;

public class AllMsisdnInvalidImpl implements MSISDNServiceDefinition
{
	private static Logger logger = null;
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.services.msisdninfo.MSISDNServiceDefinition#getSubscriberDetail(java.lang.String)
	 */
	static
	{
		logger = Logger.getLogger(MSISDNServiceDefinition.class);
	}
	
	@Override
	public SubscriberDetail getSubscriberDetail(MNPContext mnpContext)
	{
		String subscriberID = Utility.trimCountryPrefix(mnpContext.getSubscriberID());
		
		String circleID = null;
		boolean isPrepaid = false;
		boolean isValidSubscriber = false;
		HashMap<String, String> subscriberDetailsMap = null;

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID,circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		logger.info("RBT:: subscriberDetail: " + subscriberDetail);

		return subscriberDetail;
	}
}
