package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.services.common.MNPConstants;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class WdsTataCdmaImpl implements MSISDNServiceDefinition
{
	private static Logger logger = Logger.getLogger(WdsTataCdmaImpl.class);

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
		HashMap<String, String> subscriberDetailsMap = new HashMap<String, String>();
		if(!Utility.isValidNumber(subscriberID)) {
			return new SubscriberDetail(subscriberID,circleID,isPrepaid,isValidSubscriber,subscriberDetailsMap);
		}

		String vuiLogPath = null;
		int logRotationSize = 0;
		String wdsHTTPLink = null;

		try
		{
			Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "QUERIED_INTERFACES_VUI_LOG_PATH");
			if (param != null)
				vuiLogPath = param.getValue();

			param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "ROTATION_SIZE");
			if (param != null)
			{
				try
				{
					logRotationSize = Integer.parseInt(param.getValue());
				}
				catch (Exception e)
				{
					logRotationSize = 0;
				}
			}

			param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "WDS_HTTP_LINK");
			if (param != null)
				wdsHTTPLink = param.getValue();

			String wdsHttpQuery = wdsHTTPLink + "&mdn=" + subscriberID;

			RBTHTTPProcessing rbtHTTPProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String wdsResult = rbtHTTPProcessing.makeRequest1(wdsHttpQuery, subscriberID, "RBT_VUI");
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String requestedTimeString = formatter.format(requestedTimeStamp);

			if (wdsResult != null)
			{
				wdsResult = wdsResult.trim();
				StringTokenizer wdsST = new StringTokenizer(wdsResult, "|");
				if (wdsST.countTokens() >= 9)
				{
					// 9 is some junk number, comparing with the max token we
					// need
					String tempString = null;

					for (int tokenCount = 1; wdsST.hasMoreTokens(); tokenCount++)
					{
						tempString = wdsST.nextToken();
						if (tokenCount == 3)
						{
							if (tempString.startsWith("Pre"))
							{
								logger.info("RBT::User is prepaid as from WDS");
								isPrepaid = true;
							}
							else
							{
								logger.info("RBT::User is postpaid as from WDS");
								isPrepaid = false;
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
								subscriberDetailsMap.put(MNPConstants.STATUS, WebServiceConstants.SUSPENDED);
							}
						}
						else if (tokenCount == 8)
						{
							Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "ALLOWED_SUBSCRIBER_CLASSES", "cmo,fwp,ws,walky10,fwt");
							String allowedSubClass = parameter.getValue();
							if (allowedSubClass.indexOf(tempString) == -1)
							{
								logger.info("RBT::subscriber class from WDS->" + tempString + ". Not allowing as allowed cases are " + allowedSubClass);
								isValidSubscriber = false;
								subscriberDetailsMap.put(MNPConstants.STATUS, WebServiceConstants.NOT_ALLOWED);
							}
						}
						else if (tokenCount == 9)
						{
							if (tempString != null)
							{
								circleID = Utility.getMappedCircleID(tempString);
								if (circleID == null)
									isValidSubscriber = false;
							}
							break;
						}
					}

					String userType = isPrepaid ? "PRE_PAID" : "POST_PAID";
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize,
							"RBT_QUERY_WDS", subscriberID, userType,
							"query_wds", "success", requestedTimeString,
							differenceTime + "", "RBT_VUI", wdsHttpQuery,
							wdsResult);
				}
				else
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize,
							"RBT_QUERY_WDS", subscriberID, "unknown",
							"query_wds", "error_response", requestedTimeString,
							differenceTime + "", "RBT_VUI", wdsHttpQuery,
							wdsResult);
					logger.info("RBT::didn't get proper response from WDS even!!");
					isValidSubscriber = false;
				}
			}
			else
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize,
						"RBT_QUERY_WDS", subscriberID, "unknown",
						"query_wds", "null_error_response",
						requestedTimeString, differenceTime + "",
						"RBT_VUI", wdsHttpQuery, wdsResult);
				logger.info("RBT::didn't get response from WDS!!");
				isValidSubscriber = false;
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			isValidSubscriber = false;
		}

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID,
				circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		logger.info("RBT:: subscriberDetail: " + subscriberDetail);

		return subscriberDetail;
	}
}
