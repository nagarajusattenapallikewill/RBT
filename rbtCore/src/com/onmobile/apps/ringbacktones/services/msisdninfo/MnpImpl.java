package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.mnp.MnpService;
import com.onmobile.mnp.MnpServiceFactory;
import com.onmobile.mnp.dataStore.Circle;


public class MnpImpl implements MSISDNServiceDefinition
{
	private static Logger logger = Logger.getLogger(MnpImpl.class);
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.services.msisdninfo.MSISDNServiceDefinition#getSubscriberDetail(java.lang.String)
	 */
	@Override
	public SubscriberDetail getSubscriberDetail(MNPContext mnpContext)
	{
		String subscriberID = Utility.trimCountryPrefix(mnpContext.getSubscriberID());
		String circleID = null;
		boolean isPrepaid = false;
		boolean isValidSubscriber = false;
		HashMap<String, String> subscriberDetailsMap = null;
		if(!Utility.isValidNumber(subscriberID)) {
			return new SubscriberDetail(subscriberID,circleID,isPrepaid,isValidSubscriber,subscriberDetailsMap);
		}
		
		//Area Code to be in the form of 11:9,12:8,13:10 etc. i.e. areaCode:length
		Parameters areaParameter = CacheManagerUtil.getParametersCacheManager().
		          					getParameter(iRBTConstant.COMMON, "AREA_CODE_FOR_PHONE_NUMBER_LENGTH", null);
		if(areaParameter!=null && subscriberID != null){
		  String paramVal = areaParameter.getValue();
		  if(paramVal!=null){
			 String token[] = paramVal.split(",");
               for(int i=0;i<token.length;i++){
					String areaCodeToken[] = token[i].split(":");
					if(subscriberID.startsWith(areaCodeToken[0])){
						try{
							int areaCodeLength = Integer.parseInt(areaCodeToken[1]);
							if(subscriberID.length()== (areaCodeLength+areaCodeToken[0].length()))
							  subscriberID = subscriberID.substring(areaCodeToken[0].length());
						}catch(Exception e){
							e.printStackTrace();
						}
 				  }
              }
		   }
		}


		String customerName = null;
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "CUSTOMER_NAME");
		if (parameter != null)
			customerName = parameter.getValue();

		boolean isOnlineDip = false; 
		if(mnpContext.isOnlineDip())
			isOnlineDip = true;
		else
		{
			parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "MNP_ONLINE_DIP");
			if (parameter != null)
				isOnlineDip = parameter.getValue().equalsIgnoreCase("TRUE");
		}

		try
		{
			MnpServiceFactory  mnpServiceFactory = MnpServiceFactory.getInstance();
			MnpService  mnpService = mnpServiceFactory.getMnpService();
			Circle circle = mnpService.getCircle(customerName, subscriberID, isOnlineDip);
			if (circle != null)
			{
				int mnpCircleID = circle.getCircleId();
				circleID = Utility.getMappedCircleID(String.valueOf(mnpCircleID));

				if(circleID != null)
				{
					SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
					if (sitePrefix != null && sitePrefix.getSiteUrl() == null)
					   isValidSubscriber = true;
					
				}
			}
			else
				logger.info("Circle ID not found in MNP");
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "DEFAULT_USER_TYPE", "POSTPAID");
		if (parameter != null)
			isPrepaid = parameter.getValue().trim().equalsIgnoreCase("PREPAID");

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID,
				circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		logger.info("RBT:: subscriberDetail: " + subscriberDetail);

		return subscriberDetail;
	}
}
