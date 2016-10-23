package com.onmobile.apps.ringbacktones.monitor.common;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;

public class Utility implements iRBTConstant {
	public static HashMap<String, String> getRequestParamsMap(HttpServletRequest request) {
		HashMap<String, String> requestParams = new HashMap<String, String>();

		@SuppressWarnings("unchecked")
		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String key = params.nextElement();
			String value = request.getParameter(key).trim();

			requestParams.put(key, value);
		}

		Logger.getLogger(Utility.class).info("RBT:: requestParams: " + requestParams);
		return requestParams;
	}

	public static String getBaseURLForSubscriber(String msisdn) {
		SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(msisdn));
		SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(
				subscriberDetail.getCircleID());
		if (sitePrefix != null && sitePrefix.getSiteUrl() != null) {
			String url = sitePrefix.getSiteUrl();
			if (url.indexOf("/rbt_sms.jsp?") != -1)
				url = Utility.findNReplaceAll(url, "/rbt_sms.jsp?", "");
			if (url.indexOf("/rbt_sms.jsp") != -1)
				url = Utility.findNReplaceAll(url, "/rbt_sms.jsp", "");
			if (url.indexOf("/rbt_gift_acknowledge.jsp") != -1)
				url = Utility.findNReplaceAll(url, "/rbt_gift_acknowledge.jsp", "");
			if (url.indexOf("/rbt_copy.jsp?") != -1)
				url = Utility.findNReplaceAll(url, "/rbt_copy.jsp?", "");
			if (url.indexOf("/rbt_copy.jsp") != -1)
				url = Utility.findNReplaceAll(url, "/rbt_copy.jsp", "");
			return url;
		}

		return CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "LOCAL_RBT_URL",
				"http://localhost:8080/rbt").getValue();
	}

	public static String findNReplaceAll(String input, String findWhatString,
			String replaceWithString) {
		if (input == null || replaceWithString == null || input.indexOf(findWhatString) == -1)
			return input;
		Logger.getLogger(Utility.class).info(
				"RBT:: findNReplaceAll input=" + input + ",findWhatString=" + findWhatString
						+ ",replaceWithString=" + replaceWithString);
		StringBuffer ret = new StringBuffer();
		boolean keepGoing = true;
		while (keepGoing) {
			int index = input.indexOf(findWhatString);
			if (index == -1) {
				ret.append(input);
				keepGoing = false;
			}
			else {
				ret.append(input.substring(0, index));
				ret.append(replaceWithString);
				input = input.substring(index + findWhatString.length());
			}
		}
		Logger.getLogger(Utility.class).info(
				"RBT:: findNReplaceAll Exit with return ret.toString()=" + ret.toString());
		return ret.toString();
	}
}