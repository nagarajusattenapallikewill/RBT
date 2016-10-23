package com.onmobile.apps.ringbacktones.daemons.callbackEvents;


import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class CallbackUtils 
{
	private static Logger logger = Logger.getLogger(CallbackUtils.class);
	
	static RBTHttpClient rbtHttpClient = null;
	static ParametersCacheManager rbtParametersCacheManager = null;
	
	static
	{
		rbtParametersCacheManager = CacheManagerUtil.getParametersCacheManager(); 
			
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setMaxTotalConnections(40);
		httpParameters.setMaxHostConnections(40);
		rbtHttpClient = new RBTHttpClient(httpParameters);
	}
	
	public static void executeEvent(RBTCallBackEvent rbtCallBackEvent)
	{
		if (rbtCallBackEvent.getModuleID() == Modules.RTCOPY.moduleID)
		{
			try
			{
				String rtPromoUrl = getParamAsString("DAEMON", "RT_PROMO_URL");
				if (rtPromoUrl != null)
				{
					rtPromoUrl = rtPromoUrl.replaceAll("\\$MSISDN\\$", rbtCallBackEvent.getSubscriberID());
					rtPromoUrl = rtPromoUrl.replaceAll("\\$VCODE\\$", String.valueOf(rbtCallBackEvent.getClipID()));
					rtPromoUrl = rtPromoUrl.replaceAll("\\$CHANNEL\\$", rbtCallBackEvent.getSelectedBy());
					logger.info("RT Promo url :"+rtPromoUrl);
					HttpResponse httpResponse = null;
					try 
					{
						httpResponse = rbtHttpClient.makeRequestByGet(rtPromoUrl, null);
					}
					catch (Exception e) 
					{
						logger.error("", e);
						throw new NullPointerException(e.getLocalizedMessage());
					}
					logger.info("HttpResponse :"+httpResponse);
					if (httpResponse.getResponseCode() == 200)
					{
						logger.info("trying to delete record ");
						// Deletes the record from DB
						rbtCallBackEvent.deleteCallbackEvent(rbtCallBackEvent);
					}
					
				}
			}
			finally
			{
				
			}
		}
	}
	
	public enum Modules
	{
		RTCOPY(1), CROSS_PROMO(2);
		
		final int moduleID;
		Modules(int module)
		{
			this.moduleID = module;
		}
	}
	
	public enum EventTypes
	{
		SEL_ACT(1),	SEL_REN(2),	SEL_DCT(3);
		
		final int eventType;
		EventTypes(int eventType)
		{
			this.eventType = eventType;
		}
	}
	
	public static String getParamAsString(String type, String param)
	{
		try
		{
			return rbtParametersCacheManager.getParameter(type, param, null).getValue();
		}
		catch(Exception e)
		{
			logger.info("Unable to get param ->"+param );
			return null;
		}
	}

}
