package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class RBTLoginUserQueryImpl implements LoadRBTLoginUser {
	private static Logger logger = Logger
			.getLogger(RBTLoginUserQueryImpl.class);
	static List<String> configuredAppNamesForTPSupport = null;
	static String configuredUserTypesToSupport = null;
	static {
		String configuredAppNamesForTPSupportString = RBTParametersUtils
				.getParamAsString("MOBILEAPP",
						WebServiceConstants.APP_NAMES_FOR_REAL_TP_SUPPORT, null);
		logger.info("APP_NAMES_FOR_REAL_TP_SUPPORT: "
				+ configuredAppNamesForTPSupportString);
		configuredAppNamesForTPSupport = ListUtils.convertToList(
				configuredAppNamesForTPSupportString, ",");
		logger.info("configuredAppNamesForTPSupport: "
				+ configuredAppNamesForTPSupport);
		if (configuredAppNamesForTPSupport == null
				|| configuredAppNamesForTPSupport.isEmpty()) {
			logger.info("APP_NAMES_FOR_REAL_TP_SUPPORT is not configured or is empty. Returning null");
		} else {
			String type = "";
			for (String appName : configuredAppNamesForTPSupport) {
				type += (Utility.getMobileClientTypeWithAppName(appName) + ",");
			}
			if (type.length() > 0)
				type = type.substring(0, type.length() - 1);
			configuredUserTypesToSupport = type;
		}
	}

	@Override
	public Set<String> getRBTLoginUserData(String callerId, String calledId,
			String userId) {
		Set<String> signaleUserSet = new HashSet<String>();
		String subscriberId = (null != callerId) ? callerId + "," : "";
		subscriberId = subscriberId + ((null != calledId) ? calledId : "");
		logger.info("getRBTLoginUserData by query for susbcriberId:"
				+ subscriberId);
		addSignalUser(subscriberId, signaleUserSet);
		return signaleUserSet;
	}

	private void addSignalUser(String subscriberId, Set<String> signaleUserSet) {
		if (configuredAppNamesForTPSupport == null
				|| configuredAppNamesForTPSupport.isEmpty()) {
			logger.info("APP_NAMES_FOR_REAL_TP_SUPPORT is not configured or is empty. Returning null");
		} else {
			logger.info("getRBTLoginUserData by query for susbcriberId:"
					+ subscriberId + ", type:" + configuredUserTypesToSupport);
			RBTLoginUser[] loginUsers = RBTDBManager.getInstance()
					.getRBTLoginUsers(null, null, subscriberId,
							configuredUserTypesToSupport, false);
			logger.info("getRBTLoginUserData by query for susbcriberId:"
					+ subscriberId + ", loginUsers:" + loginUsers);
			if (loginUsers != null) {
				for (RBTLoginUser loginUser : loginUsers) {
					signaleUserSet.add(loginUser.subscriberID());
					logger.info("adding singnal user into the set: "
							+ loginUser.subscriberID());
				}
			}
		}

	}
}
