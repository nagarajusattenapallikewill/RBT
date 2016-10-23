package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.UpgradeObject;
import com.onmobile.apps.ringbacktones.content.VfRBTUpgradeConsent;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.utils.ListUtils;

public class VfRBTUpgardeConsentFeatureImpl implements VfRBTUpgradeConsent {
	private static Logger logger = Logger
			.getLogger(VfRBTUpgardeConsentFeatureImpl.class);

	@Override
	public Subscriber consentUpgradeFlow(UpgradeObject upgradeObject) {
		boolean success = false;
		Subscriber subscriber = null;
		String refId = upgradeObject.getRefID();
		String prepaidYes = upgradeObject.isPrepaid() ? "y" : "n";
		boolean modeCheck = CheckUpgradeModeIsConfigured(upgradeObject
				.getMode());
		String mode = getMappedModeForUpgrade(
				upgradeObject.getOldSubscriptionClass(),
				upgradeObject.getNewSubscriptionClass(),
				upgradeObject.getMode());
		if (modeCheck) {
			String consentUniqueId = Utility.generateConsentIdRandomNumber(upgradeObject.getSubscriberID());
			if (consentUniqueId != null) {
				refId = consentUniqueId;
			}

			if (null == refId) {
				refId = UUID.randomUUID().toString();
			}
			Connection conn = getConnection();
			if (conn == null)
				success = false;
			try {
				Date requestTime = null;
				int secondsToBeAddedInRequestTime = DBUtility
						.secondsToBeAddedInRequestTime(
								upgradeObject.getCircleID(),
								upgradeObject.getMode());
            	if (secondsToBeAddedInRequestTime != -1) {
            		Calendar cal = Calendar.getInstance();
            		cal.add(Calendar.SECOND, secondsToBeAddedInRequestTime);
            		requestTime = cal.getTime();
            	}
				success = ConsentTableImpl
						.convertSubscriptionTypeConsentUpgrde(conn,
								upgradeObject.getSubscriberID(), mode,
								upgradeObject.getStartDate(),
								upgradeObject.getEndDate(),
								upgradeObject.getActivationInfo(), prepaidYes,
								upgradeObject.getNewSubscriptionClass(),
								upgradeObject.getCosID(),
								upgradeObject.getRbtType(),
								upgradeObject.getExtraInfo(),
								upgradeObject.getCircleID(), refId,
								upgradeObject.getConsentStatus(),
								requestTime);
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				releaseConnection(conn);
			}
			logger.info("Updated status changed, update " + "status: "
					+ success + " for subscriber: "
					+ upgradeObject.getSubscriberID() + "subscriptionClass: "
					+ upgradeObject.getNewSubscriptionClass());
		}
		if (success)
			subscriber = new SubscriberImpl(upgradeObject.getSubscriberID(),
					mode, null, upgradeObject.getStartDate(),
					upgradeObject.getEndDate(), prepaidYes, null, null, 0,
					upgradeObject.getActivationInfo(),
					upgradeObject.getNewSubscriptionClass(), null, null, null,
					null, 0, upgradeObject.getCosID(), null,
					upgradeObject.getRbtType(), null, null,
					upgradeObject.getExtraInfo(), upgradeObject.getCircleID(),
					refId);
		return subscriber;
	}

	public Connection getConnection() {
		return (RBTDBManager.getInstance().getConnection());
	}

	public boolean releaseConnection(Connection conn) {
		return (RBTDBManager.getInstance().releaseConnection(conn));
	}

	public boolean CheckUpgradeModeIsConfigured(String mode) {
		String upgradeModes = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.COMMON,
						"VODAFONE_UPGRADE_CONSENT_MODES", null);
		boolean modeCheck = false;
		if (upgradeModes != null) {
			List<String> modesList = Arrays.asList(upgradeModes.split(","));
			modeCheck = (modesList == null || modesList.isEmpty() || !modesList
					.contains(mode));
		}
		return modeCheck;
	}

	public static String getMappedModeForUpgrade(String oldSubscriptionClass,
			String newSubscriptionClass, String mode) {
		String downGradePacks = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.COMMON, "DOWNGRADE_PACK_MAP",
						null);
		String upgradePacks = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.COMMON, "UPGRADE_PACK_MAP",
						null);
		String modeFromReqest = mode;
		mode = checkModeMap(oldSubscriptionClass, newSubscriptionClass,
				upgradePacks, true, mode);
		logger.info("Upgrade mode value: " + mode);
		if (mode.equalsIgnoreCase(modeFromReqest)) {
			mode = checkModeMap(oldSubscriptionClass, newSubscriptionClass,
					downGradePacks, false, mode);
			logger.info("DownGrade mode value: " + mode);
		}
		return mode;
	}

	public static String checkModeMap(String oldSubscriptionClass,
			String newSubscriptionClass, String pack, boolean type, String mode) {
		List<String> list = ListUtils.convertToList(pack, ";");
		for (String s : list) {
			String[] keyValue = s.split(":");
			logger.info("Upgrade mode map list values: " + keyValue);
			if (keyValue != null && keyValue.length == 2) {
				String oldSubclass = keyValue[0];
				String newSubclass = keyValue[1];
				if (null != newSubscriptionClass
						&& null != oldSubscriptionClass
						&& newSubclass.equalsIgnoreCase(newSubscriptionClass)
						&& oldSubclass.equalsIgnoreCase(oldSubscriptionClass)) {
					if (type) {
						String modeConfig = CacheManagerUtil
								.getParametersCacheManager().getParameterValue(
										iRBTConstant.COMMON,
										"UPGRADE_MODE_CONFIG", mode);
						mode = modeConfig.replace("%MODE%", mode);
					} else {
						String modeConfig = CacheManagerUtil
								.getParametersCacheManager().getParameterValue(
										iRBTConstant.COMMON,
										"DOWNGRADE_MODE_CONFIG", mode);
						mode = modeConfig.replace("%MODE%", mode);
					}
				}
			}
		}
		return mode;
	}
}
