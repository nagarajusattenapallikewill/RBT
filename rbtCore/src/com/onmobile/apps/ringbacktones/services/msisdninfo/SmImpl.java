package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.common.Utility;

public class SmImpl implements MSISDNServiceDefinition
{
	private static Logger logger = Logger.getLogger(SmImpl.class);

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

		String validateMsisdnURL = null;
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "VALIDATE_MSISDN_URL");
		if (param != null)
			validateMsisdnURL = param.getValue();

		String url = validateMsisdnURL + subscriberID;
		Integer statusCode = new Integer(0);
		StringBuffer response = new StringBuffer();
		Tools.callURL(url, statusCode, response, false, null, 80);
		if (response.toString().trim().equalsIgnoreCase("SUCCESS"))
			isValidSubscriber = true;
		
		if(!isValidSubscriber)
			return new SubscriberDetail(subscriberID,
				circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);

		SitePrefix sitePrefix = Utility.getPrefix(subscriberID);
		if (sitePrefix != null)
			circleID = sitePrefix.getCircleID();
		else
			logger.info("Could not get circle for " + subscriberID);

		Parameters userTypeParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "DEFAULT_USER_TYPE", "POSTPAID");
		if (userTypeParam != null)
			isPrepaid = userTypeParam.getValue().trim().equalsIgnoreCase("PREPAID");

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID,
				circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		logger.info("RBT:: subscriberDetail: " + subscriberDetail);

		return subscriberDetail;
	}
}
