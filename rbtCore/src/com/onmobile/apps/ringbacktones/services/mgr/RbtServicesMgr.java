package com.onmobile.apps.ringbacktones.services.mgr;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MSISDNServiceDefinition;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;

public class RbtServicesMgr
{
	private static Logger logger = Logger.getLogger(RbtServicesMgr.class);

	private static final String mnpServiceKey = "MNP_SERVICE"; 
	private static HashMap<String, Object> implementationMap = null;

	static
	{
		implementationMap = new HashMap<String, Object>();

		RBTClient client = RBTClient.getInstance();
		MSISDNServiceDefinition mnpService = null;
		ApplicationDetailsRequest request = new ApplicationDetailsRequest();
		request.setType("SERVICE");
		Parameter[] serviceParams = client.getParameters(request);
		if (serviceParams != null)
		{
			for (Parameter parameter : serviceParams)
			{
				try
				{
					String serviceImpl = parameter.getValue();
					Class<?> serviceClass = Class.forName(serviceImpl);
					mnpService = (MSISDNServiceDefinition) serviceClass.newInstance();
					implementationMap.put(parameter.getName(), mnpService);
				}
				catch (ClassNotFoundException e)
				{
					logger.error("", e);
				}
				catch (InstantiationException e)
				{
					logger.error("", e);
				}
				catch (IllegalAccessException e)
				{
					logger.error("", e);
				}
			}
		}

		if (!implementationMap.containsKey(mnpServiceKey))
		{
			try
			{
				String serviceImpl = "com.onmobile.apps.ringbacktones.services.msisdninfo.DbImpl";
				Class<?> serviceClass = Class.forName(serviceImpl);
				mnpService = (MSISDNServiceDefinition) serviceClass.newInstance();
				implementationMap.put(mnpServiceKey, mnpService);
			}
			catch (ClassNotFoundException e)
			{
				logger.error("", e);
			}
			catch (InstantiationException e)
			{
				logger.error("", e);
			}
			catch (IllegalAccessException e)
			{
				logger.error("", e);
			}
		}
	}

	private static MSISDNServiceDefinition getMNPServiceImpl(MNPContext mnpContext)
	{
		String serviceKey = mnpServiceKey;
		if (mnpContext.getMode() != null)
			serviceKey = serviceKey + "_" + mnpContext.getMode();

		MSISDNServiceDefinition mnpService = (MSISDNServiceDefinition) implementationMap.get(serviceKey);
		if (mnpService == null)
			mnpService = (MSISDNServiceDefinition) implementationMap.get(mnpServiceKey);
		
		return mnpService;
	}

	public static SubscriberDetail getSubscriberDetail(MNPContext mnpContext)
	{
		SubscriberDetail subscriberDetail = null;
		MSISDNServiceDefinition mnpService = getMNPServiceImpl(mnpContext);
		if (mnpService != null)
			subscriberDetail = mnpService.getSubscriberDetail(mnpContext);
		else
			logger.info("The Implementation Class for MNP Service not loaded");

		return subscriberDetail;
	}
}
