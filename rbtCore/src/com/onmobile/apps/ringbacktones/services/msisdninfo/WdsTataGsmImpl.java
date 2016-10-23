package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.common.MNPConstants;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class WdsTataGsmImpl implements MSISDNServiceDefinition
{
	private static Logger logger = Logger.getLogger(WdsTataGsmImpl.class);

	static ArrayList<String> allowedSubscriberClasses = new ArrayList<String>(); 
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.services.msisdninfo.MSISDNServiceDefinition#getSubscriberDetail(java.lang.String)
	 */

	static
	{
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "ALLOWED_SUBSCRIBER_CLASSES", "cmo,fwp,ws,walky10,fwt");
		String allowedSubClass = parameter.getValue();
		if (allowedSubClass != null)
		{
			StringTokenizer paramTokenizer = new StringTokenizer(allowedSubClass, ",");
			while(paramTokenizer.hasMoreTokens())
				allowedSubscriberClasses.add(paramTokenizer.nextToken());
			logger.info("allowedSubscriberClasses : " + allowedSubscriberClasses);		
		}
	
	}
	@Override
	public SubscriberDetail getSubscriberDetail(MNPContext mnpContext)
	{
		String subscriberID = Utility.trimCountryPrefix(mnpContext.getSubscriberID());

		String circleID = null;
		boolean isPrepaid = false;
		boolean isValidSubscriber = false;
		HashMap<String, String> subscriberDetailsMap = new HashMap<String, String>();
		if(!Utility.isValidNumber(subscriberID)) {
			return new SubscriberDetail(subscriberID,circleID,isPrepaid,isValidSubscriber,subscriberDetailsMap);
		}

		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		try
		{
			startTime = System.currentTimeMillis();
			String wdsResult = queryWDS(subscriberID);
			endTime = System.currentTimeMillis();
			long timeGap = (endTime - startTime);
			if (wdsResult != null)
			{
				wdsResult = wdsResult.trim();
				subscriberDetailsMap.put(MNPConstants.OPERATOR_USER_INFO, wdsResult.replaceAll("\\|", "#") + "|start:" + startTime + "|delay:" + timeGap);
				logger.info("WDSResult:" + wdsResult);
				StringTokenizer wdsST = new StringTokenizer(wdsResult, "|");
				if (wdsST.countTokens() >= 13)
				{
					String tempString = null;
					String operatorName = null;
					for (int tokenCount = 1; wdsST.hasMoreTokens(); tokenCount++)
					{
						tempString = wdsST.nextToken();
						if (tokenCount == 4)
						{
							if (tempString != null && Integer.parseInt(tempString) == 2)
							{
								logger.info("RBT::User is prepaid as from WDS");
								isPrepaid = true;
							}
							else
							{
								if (tempString != null && Integer.parseInt(tempString) == 1)
								{
									logger.info("RBT::User is postpaid as from WDS");
									isPrepaid = false;
								}
								else
								{
									isValidSubscriber = false;
								}
							}
						}
						else if (tokenCount == 5)
						{
							if (tempString.equals("1"))
							{
								logger.info("RBT::can allow subscriber as from WDS");
								isValidSubscriber = true;
							}
							else
							{
								logger.info("RBT::cannot allow subscriber as from WDS");
								isValidSubscriber = false;
							}
						}
						else if (tokenCount == 8)
						{
							if (!allowedSubscriberClasses.contains(tempString))
							{
								logger.info("RBT::subscriber class from WDS->" + tempString + ". Not allowing as allowed cases are " + allowedSubscriberClasses);
								isValidSubscriber = false;
								break;
							}//RBT-12024
							else if(RBTParametersUtils.getParamAsString("COMMON", "SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null)!=null)
 {
								String subClassOperatorNameMapStr = RBTParametersUtils
										.getParamAsString(
												"COMMON",
												"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP",
												null);
								Map<String, String> subClassOperatorMap = MapUtils
										.convertToMap(
												subClassOperatorNameMapStr,
												";", ":", null);
								operatorName = subClassOperatorMap
										.get(tempString);
							}
						}
						else if (tokenCount == 9)
						{
							String tempCircleID = tempString;
							if (tempCircleID != null)
							{
								circleID = Utility.getMappedCircleID(tempString);
								//RBT-12024
								if(operatorName!=null && circleID != null) {
									circleID=operatorName+"_"+circleID;
								}
								
								if (circleID == null)
									isValidSubscriber = false;
								else
								{
									SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
									isValidSubscriber = isValidSubscriber && (sitePrefix != null && sitePrefix.getSiteUrl() == null);
								}
							}
							break;
						}
					}
				}
				else
				{
					isValidSubscriber = false;
					subscriberDetailsMap.put("WDS_STATUS","WDS_ERROR_RESPONSE");
					subscriberDetailsMap.put(MNPConstants.STATUS, WebServiceConstants.ERROR);
					logger.info("WDS query returned less tokens than expected");
				}
			}
			else
			{
				isValidSubscriber = false;
				subscriberDetailsMap.put("WDS_STATUS","WDS_ERROR");
				subscriberDetailsMap.put(MNPConstants.STATUS, WebServiceConstants.ERROR);
				logger.info("WDS query returned null");
			}

			// write event log with Subscriber ID, wds start time, wds end Time,
			// wds Time GAP
		}
		catch (Exception t)
		{
			logger.error("", t);
		}

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID,
				circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		logger.info("RBT:: subscriberDetail: " + subscriberDetail);

		return subscriberDetail;
	}

	private String queryWDS(String subscriberID)
	{
		String wdsResult = null;

		try
		{
			String wdsHttpQuery = null;

			Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "WDS_HTTP_LINK");
			if (param != null)
				wdsHttpQuery = param.getValue().trim();

			logger.info("query is " + wdsHttpQuery);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("MDN", subscriberID);
			HttpParameters httpParameters = new HttpParameters(wdsHttpQuery);
			wdsResult = RBTHTTPProcessing.postFile(httpParameters, params, null);

			logger.info("result for " + subscriberID + "is " + wdsResult);
		}
		catch (Exception e)
		{
			logger.error("", e);
			wdsResult = null;
		}

		// result = "9030055076|2|PREPAID|2|1|013DE58A|-|GCMO|AP|SLEE02||";
		return wdsResult;
	}
}
