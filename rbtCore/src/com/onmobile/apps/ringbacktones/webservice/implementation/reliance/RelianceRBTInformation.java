package com.onmobile.apps.ringbacktones.webservice.implementation.reliance;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;


public class RelianceRBTInformation extends BasicRBTInformation
{
	private static Logger logger = Logger.getLogger(RelianceRBTInformation.class);

	/**
	 * @throws ParserConfigurationException
	 */
	public RelianceRBTInformation() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getWebServiceSubscriberObject(com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected WebServiceSubscriber getWebServiceSubscriberObject(WebServiceContext webServiceContext, Subscriber subscriber)
	{
		WebServiceSubscriber webServiceSubscriber = super.getWebServiceSubscriberObject(webServiceContext, subscriber);
		
		if (rbtDBManager.isSubscriberDeactivated(subscriber))
		{
			String subscriberID = webServiceContext.getString(param_subscriberID);
			List<ProvisioningRequests> provList = ProvisioningRequestsDao.getBySubscriberId(subscriberID);
			logger.info("RBT:: List<ProvisioningRequests>: " + provList);
			if(provList != null && provList.size() > 0)
				webServiceSubscriber.setStatus(ACT_PENDING);
		}

		if (logger.isInfoEnabled())
			logger.info("webServiceSubscriber: " + webServiceSubscriber);
		return webServiceSubscriber;
	}
}
