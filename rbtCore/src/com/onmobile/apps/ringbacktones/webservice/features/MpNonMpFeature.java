package com.onmobile.apps.ringbacktones.webservice.features;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPack;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * Feature added for TEF/VF/OR Spain. 
 * Jira Id: RBT-13163 - MP Non MP TEF/VF/OR Spain
 * @author rony.gregory
 *
 */
public class MpNonMpFeature {

	private static Logger logger = Logger.getLogger(MpNonMpFeature.class);
	private static List<String> mPNonMpChargeClassesList = new ArrayList<String>(); 
	private static String mPNonMpMpCosId = null; 

	private static MpNonMpFeature instance = null;
	private static boolean isClassCheckDone = false;
	private static String mpNonMpMpDeactvationMode = null;

	private MpNonMpFeature () {

	}
	static {
		String mPNonMpChargeClassesString = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, iRBTConstant.MP_NON_MP_CHARGE_CLASSES, null);
		logger.info("mPNonMpChargeClassesString: " + mPNonMpChargeClassesString);
		mPNonMpChargeClassesList = ListUtils.convertToList(mPNonMpChargeClassesString, ",");
		logger.info("mPNonMpChargeClassesList: " + mPNonMpChargeClassesList);

		mPNonMpMpCosId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, iRBTConstant.MP_NON_MP_MP_COSID, null);
		logger.info("mPNonMpMpCosId: " + mPNonMpMpCosId);
		
		mpNonMpMpDeactvationMode = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, iRBTConstant.MP_NON_MP_MP_DEACTIVATION_MODE, iRBTConstant.DAEMON);
		logger.info("mpNonMpMpDeactvationMode: " + mpNonMpMpDeactvationMode);
	}

	/**
	 * Returns the instance of the MPNonMpFeature class based on configuration
	 * @return
	 */
	public static MpNonMpFeature getMpNonMpFeatureClassInstance() {
		if (!isClassCheckDone) {
			synchronized (MpNonMpFeature.class) {
				String mpNonMpFeatureClassString = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, iRBTConstant.MP_NON_MP_FEATURE_CLASS, null);
				if (mpNonMpFeatureClassString == null || mpNonMpFeatureClassString.trim().length() == 0) {
					logger.info("MpNonMpFeature not enabled. mpNonMpFeatureClassString: " + mpNonMpFeatureClassString);
					isClassCheckDone = true;
					return null;
				}
				logger.info("MpNonMpFeature enabled. mpNonMpFeatureClassString: " + mpNonMpFeatureClassString);
				MpNonMpFeature mpNonMpFeatureClass = null;
				try {
					mpNonMpFeatureClass = (MpNonMpFeature)Class.forName(mpNonMpFeatureClassString).newInstance();
				} catch (InstantiationException e) {
					logger.error("InstantiationException caught." + e, e);
				} catch (IllegalAccessException e) {
					logger.error("IllegalAccessException caught." + e, e);
				} catch (ClassNotFoundException e) {
					logger.error("ClassNotFoundException caught." + e, e);
				} finally {
					isClassCheckDone = true;
				}
				instance = mpNonMpFeatureClass;
			}
		}
		logger.info("mpNonMpFeatureClassInstance: " + instance);
		return instance;
	}

	/**
	 * If subscriber is not active on the configured music pack, it will be activated on the same, provided the chargeClass is configured. 
	 * @param mpNonMpFeatureBean
	 */
	public void checkAndActivateMusicPack(MpNonMpFeatureBean mpNonMpFeatureBean) {
		logger.info("checkAndActivateMusicPack: " + mpNonMpFeatureBean);
		boolean preCheckStatus = doPreCheck(mpNonMpFeatureBean, true);
		if (!preCheckStatus) {
			logger.info("Precheck failed. Returning without doing any operation. mpNonMpFeatureBean: " + mpNonMpFeatureBean);
			return;
		}
		if (mPNonMpChargeClassesList.contains(mpNonMpFeatureBean.getChargeClass())) {
			logger.info("chargeClass configured for Mp-Non MP Feature. mpNonMpFeatureBean: " + mpNonMpFeatureBean);
			if (!isPackActivated(mpNonMpFeatureBean.getSubscriberId())) {
				SubscriptionRequest subRequest = new SubscriptionRequest(mpNonMpFeatureBean.getSubscriberId());
				subRequest.setCosID(Integer.parseInt(mPNonMpMpCosId));
				subRequest.setMode(mpNonMpFeatureBean.getMode());
				subRequest.setModeInfo(mpNonMpFeatureBean.getMode());
				RBTClient.getInstance().upgradeSubscriber(subRequest);
				logger.info("subReq: " + subRequest+ ". Activate pack response: " + subRequest.getResponse());
			}
		}
	}
	/**

	 * If subscriber has active selections on the configured charge class, the music pack won't be deactivated. Else will be deactivated.
	 * @param mpNonMpFeatureBean
	 */
	public void checkAndDeactivateMusicPack(MpNonMpFeatureBean mpNonMpFeatureBean) {
		logger.info("checkAndDeactivateMusicPack: " + mpNonMpFeatureBean);
		boolean preCheckStatus = doPreCheck(mpNonMpFeatureBean, false);
		if (!preCheckStatus) {
			logger.info("Precheck failed. Returning without doing any operation. mpNonMpFeatureBean: " + mpNonMpFeatureBean);
			return;
		}
		if (!isPackActivated(mpNonMpFeatureBean.getSubscriberId())) {
			logger.info("Subscriber already not in music pack. Returning without doing any operation. mpNonMpFeatureBean: " + mpNonMpFeatureBean);
			return;
		}
		boolean isMpToBeDeactivated = true;
		if (!mpNonMpFeatureBean.isDownloadsModel()) {
			SubscriberStatus[] selections = RBTDBManager.getInstance().getAllActiveSubSelectionRecords(mpNonMpFeatureBean.getSubscriberId()); 
			logger.debug("Selections checked. Active selections returned: " + selections);
			if (selections == null || selections.length == 0) {
				logger.info("No active selections. MP to be deactivated.");
				isMpToBeDeactivated = true;
			} else {
				for (SubscriberStatus selection : selections) {
					if (mPNonMpChargeClassesList.contains(selection.classType())) {
						logger.info("Active selection with configured chargeClass found. MP not to be deactivated. mpNonMpFeatureBean: "
								+ mpNonMpFeatureBean + ", selection refId: " + selection.refID());
						isMpToBeDeactivated = false;
						break;
					}
				}
			}
		} else {
			SubscriberDownloads[] downloads = RBTDBManager.getInstance().getActiveSubscriberDownloads(mpNonMpFeatureBean.getSubscriberId());
			logger.debug("Downloads checked. Active downloads returned: " + downloads);
			if (downloads == null || downloads.length == 0) {
				logger.info("No active downloads. MP to be deactivated.");
				isMpToBeDeactivated = true;
			} else {
				for (SubscriberDownloads download : downloads) {
					if (mPNonMpChargeClassesList.contains(download.classType())) {
						logger.info("Active download with configured chargeClass found. MP not to be deactivated. mpNonMpFeatureBean: "
								+ mpNonMpFeatureBean + ", download refId: " + download.refID());
						isMpToBeDeactivated = false;
						break;
					}
				}
			}
		}
		logger.debug("isMpToBeDeactivated: " + isMpToBeDeactivated);
		if (isMpToBeDeactivated) {
			SubscriptionRequest subReq = new SubscriptionRequest(mpNonMpFeatureBean.getSubscriberId());
			Integer packCosId = Integer.parseInt(mPNonMpMpCosId);
			subReq.setPackCosId(packCosId);
			subReq.setMode(mpNonMpMpDeactvationMode);
			subReq.setModeInfo(mpNonMpMpDeactvationMode);
			RBTClient.getInstance().deactivatePack(subReq);
			logger.info("subReq: " + subReq+ ". Deactivate pack response: " + subReq.getResponse());
		}
	}

	/**
	 * Util method added for validating members of mpNonMpFeatureBean 
	 * @param mpNonMpFeatureBean
	 * @param isActivation
	 * @return
	 */
	protected boolean doPreCheck(MpNonMpFeatureBean mpNonMpFeatureBean, boolean isActivation) {
		if (mPNonMpChargeClassesList.size() == 0 || mPNonMpMpCosId == null) {
			logger.info("Configuration(s) missing for this feature. Returning without doing any operation. mPNonMpChargeClassesList: "
					+ mPNonMpChargeClassesList + ", mPNonMpMpCosId: " + mPNonMpMpCosId);
			return false;
		}
		if (mpNonMpFeatureBean == null) {
			logger.info("mpNonMpFeatureBean is null. Returning without doing any operation.");
			return false;
		}
		if (mpNonMpFeatureBean.getSubscriberId() == null) {
			logger.info("mpNonMpFeatureBean.subscriberId is null. Returning without doing any operation.");
			return false;
		}
		if (isActivation && mpNonMpFeatureBean.getChargeClass() == null) {
			logger.info("mpNonMpFeatureBean.chargeClass is null. Mandaotory for activation. Returning without doing any operation.");
			return false;
		}
		return true;
	}

	public boolean isPackActivated(String subId) {
		boolean isPackActivated = false;
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subId, null);
		rbtRequest.setInfo(WebServiceConstants.SUBSCRIBER_PACKS);
		Rbt rbt = RBTClient.getInstance().getRBTUserInformation(rbtRequest);
		if (rbt != null) {
			SubscriberPack[] packs = rbt.getSubscriberPacks();
			for (int i = 0; packs != null && i < packs.length; i++) {
				String packCosType = packs[i].getCosType();
				if (packCosType != null
						&& (packCosType.equalsIgnoreCase(iRBTConstant.LIMITED_DOWNLOADS)
								|| packCosType.equalsIgnoreCase(iRBTConstant.UNLIMITED_DOWNLOADS)
								|| packCosType.equalsIgnoreCase(iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE)
								|| packCosType.equalsIgnoreCase(iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT) 
								|| packCosType.equalsIgnoreCase(iRBTConstant.SONG_PACK))
								&& packs[i].getCosId().equals(mPNonMpMpCosId)) {
					isPackActivated = true;
					break;
				}
			}
		}
		logger.info("subId: " + subId + ", isPackActivated: " + isPackActivated);
		return isPackActivated;
	}
}	