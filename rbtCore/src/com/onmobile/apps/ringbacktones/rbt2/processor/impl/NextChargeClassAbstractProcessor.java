package com.onmobile.apps.ringbacktones.rbt2.processor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
//import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.converter.ConverterHelper;
import com.onmobile.apps.ringbacktones.rbt2.processor.INextChargeClassProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NewChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NewSubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NextServiceCharge;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public abstract class NextChargeClassAbstractProcessor
implements INextChargeClassProcessor, WebServiceConstants, Constants {

	private static Logger logger = Logger.getLogger(NextChargeClassAbstractProcessor.class);

	public NextChargeClassAbstractProcessor() {
	}

	public NextServiceCharge getNextChargeAndServiceClass(WebServiceContext webServiceContext) throws UserException {

		String categoryID = webServiceContext.getString(param_categoryID);
		String clipID = webServiceContext.getString(param_clipID);
		String subscriberId = webServiceContext.getString(param_subscriberID);
		String mode = webServiceContext.getString(param_mode);
		String chargeClass = webServiceContext.getString(param_chargeclass);
		String subscriptionClass = webServiceContext.getString(param_subClass);
		String inLoop = webServiceContext.getString(param_inLoop);
		String callerId = webServiceContext.getString(param_callerID);
		return getNextChargeAndServiceClass(subscriberId, chargeClass, categoryID, clipID, subscriptionClass, mode,
				inLoop, callerId);
	}

	public NextServiceCharge getNextChargeAndServiceClass(String subscriberId, String chargeClass, String categoryID,
			String clipID, String subscriptionClass, String mode, String inLoop, String callerId) throws UserException {
		// RBT-18671
		// RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		// Subscriber subscriber = rbtdbManager.getSubscriber(subscriberId);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber clientsubscriber = RBTClient.getInstance()
				.getSubscriber(rbtDetailsRequest);

		// RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager()
				.getSubscriptionClass(subscriptionClass);
		logger.info("subClass from request param : " + subClass);
		HashMap<String, String> requestParams = new HashMap<String, String>();
		requestParams.put(WebServiceConstants.param_subscriberID, subscriberId);
		requestParams.put(WebServiceConstants.param_chargeClass, chargeClass);
		requestParams.put(WebServiceConstants.param_mode, mode);
		requestParams.put(WebServiceConstants.param_clipID, clipID);
		requestParams.put(WebServiceConstants.param_categoryID, categoryID);
		if (callerId != null) {
			requestParams.put(param_callerID, callerId);
		}
		if (inLoop != null) {
			requestParams.put(param_inLoop, inLoop);
		}

		boolean isRenewalForChargeClass = false;
		boolean isRenewalForSubClass = false;
		int subscriptionClassOfferId = -1;
		int chargeClassOfferId = -1;
		WebServiceContext task = Utility.getTask(requestParams);

		String subStatus = "";
		if (subscriberId.equalsIgnoreCase("-1") || subscriberId.equalsIgnoreCase("0")) {
			subStatus = UNKNOWN_USER;
		} else {

			subStatus = clientsubscriber.getStatus();
			logger.info("\n\n :---> clientsubscriber" + clientsubscriber.toString());
			validateRequest(task);
		}

		// RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
		// subscriberId);
		rbtDetailsRequest.setMode(mode);
		if (((clientsubscriber != null && clientsubscriber.getStatus().equals("new_user"))
				|| (clientsubscriber != null && clientsubscriber.getStatus().equalsIgnoreCase("deactive")))
				&& subClass == null) {
			logger.info("subscriber is null and subClass: " + subClass);
			boolean allowBaseOffer = getBoolean(mode, iRBTConstant.ALLOW_BASE_OFFER)
					|| getBoolean("ALL", iRBTConstant.ALLOW_BASE_OFFER);
			logger.info("Allow base offer : " + allowBaseOffer);
			if (allowBaseOffer) {

				rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SUBSCRIPTION_STR);

				Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);

				if (offers != null && offers.length > 0) {
					subscriptionClass = offers[0].getSrvKey();
					subscriptionClassOfferId = Integer.parseInt(offers[0].getOfferID());
				}
				subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subscriptionClass);
				logger.info("SubClass from base offer  : " + subClass);
			}

			if (subClass == null) {

				ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
				Subscriber subscriber = helper.convertClientSubscriberToContentSubscriber(clientsubscriber);
				if (subscriber != null) {
					logger.info("\n\n :---> convertClientSubscriberToContentSubscriber : " + subscriber.toString()
					+ "\n\n");
				}

				CosDetails cos = DataUtils.getCos(task, subscriber);
				logger.info(" cos for new user  : " + cos);
				subscriptionClass = cos.getSubscriptionClass();
				subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subscriptionClass);
				logger.info("SubClass from cos for new user  : " + subClass);
			}

			if (subClass == null) {
				throw new UserException(SUBSCRIPTION_CLASS_NOT_FOUND);
			}

		} else if (subClass == null) {
			ArrayList<String> tnbUpgradeSubClassLst = DBConfigTools.getParameter("COMMON",
					"TNB_UPGRADE_SUBSCRIPTION_CLASSES", "ZERO", ",");

			String newSubClassType = null;
			// RBT-18671
			// String subClassType = subscriber.subscriptionClass();
			String subClassType = clientsubscriber.getSubscriptionClass();
			logger.info(":---> subClassType" + subClassType);
			String modeAndSubClass = mode + "_" + subClassType;
			for (String tnbUpgradeSubClass : tnbUpgradeSubClassLst) {
				String[] split = tnbUpgradeSubClass.split("\\:");
				if (split == null || split.length != 2) {
					continue;
				}

				if (modeAndSubClass.equalsIgnoreCase(split[0])) {
					newSubClassType = split[1];
					logger.debug(
							"Mode based new subscription class is found from TNB_UPGRADE_SUBSCRIPTION_CLASSES. modeAndSubClass key: "
									+ modeAndSubClass + ", newSubClassType: " + newSubClassType);
					break;
				} else {
					logger.debug(
							"Mode based new subscription class is not found from TNB_UPGRADE_SUBSCRIPTION_CLASSES. modeAndSubClass key: "
									+ modeAndSubClass + " from " + tnbUpgradeSubClass);
				}
			}

			if (null == newSubClassType) {
				logger.debug("Fetching old to new subscription class wise configuration");
				for (String tnbUpgradeSubClass : tnbUpgradeSubClassLst) {
					String[] split = tnbUpgradeSubClass.split("\\:");
					if (split == null || split.length != 2) {
						continue;
					}

					if (subClassType.equalsIgnoreCase(split[0])) {
						newSubClassType = split[1];
						break;
					}
				}
				logger.debug("Fetched old to new subscription class wise configuration. newSubClassType: "
						+ newSubClassType);
			}
			logger.info("SubClass for Tnb User   : " + newSubClassType);
			subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(newSubClassType);
			logger.info("SubClass object for Tnb User   : " + subClass);
			if (newSubClassType != null && !newSubClassType.isEmpty()) {
				if (subClass == null) {
					throw new UserException(SUBSCRIPTION_CLASS_NOT_FOUND);
				}

			}

		}

		ChargeClass chargeClassNew = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass);

		logger.info("Charge form request param  : " + chargeClassNew);
		if (chargeClassNew == null) {
			boolean allowSelOffer = getBoolean(mode, iRBTConstant.ALLOW_SEL_OFFER)
					|| getBoolean("ALL", iRBTConstant.ALLOW_SEL_OFFER);
			logger.info("allowSelOffer : " + allowSelOffer);
			rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SELECTION_STR);
			if (allowSelOffer) {
				Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
				logger.info("SelOffer : " + offers);

				if (offers != null && offers.length > 0) {
					chargeClass = offers[0].getSrvKey();
					chargeClassOfferId = Integer.parseInt(offers[0].getOfferID());
				}
				logger.info("ChargeClass from offer : " + offers);
				chargeClassNew = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass);
				logger.info("ChargeClass Object from offer : " + chargeClassNew);
			}
		}
		if (chargeClassNew == null) {

			chargeClassNew = getNextChargeClass(task);

			logger.info("ChargeClass from getNextChargeClass : " + chargeClassNew);
		}

		if (chargeClassNew == null) {
			throw new UserException(CHARGE_CLASS_NOT_FOUND);

		}

		if (chargeClassNew != null && chargeClassNew.getRenewalAmount() != null) {
			Double amount = Double.parseDouble(chargeClassNew
					.getRenewalAmount().replace(",", "."));
			if (amount == 0) {
				isRenewalForChargeClass = true;
			}
		}

		if (subClass != null && subClass.getRenewalAmount() != null) {
			Double amount = Double.parseDouble(subClass.getRenewalAmount().replace(",", "."));
			if (amount == 0) {
				isRenewalForSubClass = true;
			}
		}

		NewChargeClass newChargeClass = new NewChargeClass();
		NewSubscriptionClass newSubscriptionClass = new NewSubscriptionClass();
		if (chargeClassNew != null) {
			if (chargeClassNew.getAmount() != null)
				newChargeClass.setAmount(chargeClassNew.getAmount().replace(",", "."));
			if (chargeClassNew.getChargeClass() != null)
				newChargeClass.setServiceKey(chargeClassNew.getChargeClass());
			if (chargeClassNew.getSelectionPeriod() != null)
				newChargeClass.setValiditiy(chargeClassNew.getSelectionPeriod());
			if (chargeClassNew.getRenewalAmount() != null)
				newChargeClass.setRenewalAmount(chargeClassNew.getRenewalAmount().replace(",", "."));
			if (chargeClassNew.getRenewalPeriod() != null)
				newChargeClass.setRenewalValidity(chargeClassNew.getRenewalPeriod());
			newChargeClass.setOfferID(chargeClassOfferId);
			newChargeClass.setIsRenewal(isRenewalForChargeClass);

		}

		if (subClass != null) {
			if (subClass.getSubscriptionAmount() != null)
				newSubscriptionClass.setAmount(subClass.getSubscriptionAmount().replace(",", "."));
			if (subClass.getSubscriptionClass() != null)
				newSubscriptionClass.setServiceKey(subClass.getSubscriptionClass());
			if (subClass.getSubscriptionPeriod() != null)
				newSubscriptionClass.setValiditiy(subClass.getSubscriptionPeriod());
			if (subClass.getRenewalAmount() != null)
				newSubscriptionClass.setRenewalAmount(subClass.getRenewalAmount().replace(",", "."));
			if (subClass.getRenewalPeriod() != null)
				newSubscriptionClass.setRenewalValidity(subClass.getRenewalPeriod());

			newSubscriptionClass.setOfferID(subscriptionClassOfferId);
			newSubscriptionClass.setIsRenewal(isRenewalForSubClass);
		}

		NextServiceCharge nextServiceCharge = new NextServiceCharge();
		nextServiceCharge.setSubscriberStatus(subStatus);
		if (subClass != null) {
			nextServiceCharge.setSubscriptionClass(newSubscriptionClass);
		}
		if (chargeClassNew != null) {
			nextServiceCharge.setChargeClass(newChargeClass);
		}

		return nextServiceCharge;
	}

	private boolean getBoolean(String mode, String key) {
		return RBTParametersUtils.getParamAsBoolean(mode, key, "FALSE");
	}

	private ChargeClass getNextChargeClass(WebServiceContext webServiceContext) throws UserException {

		ChargeClass chargeClass = null;

		String categoryID = webServiceContext.getString(WebServiceConstants.param_categoryID);
		Category category = null;
		if (categoryID != null)
			category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(categoryID));

		if (categoryID != null && category == null) {
			throw new UserException(CATEGORY_NOT_EXIST);
		}
		if (category != null && Utility.isShuffleCategory(category.getCategoryTpe())
				&& category.getCategoryEndTime().getTime() < System.currentTimeMillis()) {
			throw new UserException(CATEGORY_EXPIRED);
		}
		List<String> catTypeList = Arrays.asList(RBTParametersUtils
				.getParamAsString("COMMON", "CATEGORY_TYPES_FOR_CATEGORY_CHARGE_CLASSES", "").split(","));
		String shuffleChargeClass = webServiceContext.getString(WebServiceConstants.param_shuffleChargeClass);
		logger.info("shuffleChargeClass : " + shuffleChargeClass);
		if ((shuffleChargeClass != null && shuffleChargeClass.equalsIgnoreCase(WebServiceConstants.YES))
				|| (category != null && catTypeList.contains(category.getCategoryTpe() + ""))) {
			chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(category.getClassType());

			return chargeClass;
		}

		String clipID = webServiceContext.getString(WebServiceConstants.param_clipID);
		Clip clip = null;
		String[] clipIDs = null;
		if (clipID != null) {
			if (clipID.contains(","))
				clipIDs = clipID.split(",");
			else if (!clipID.endsWith(".3gp") && !clipID.endsWith(".wav"))
				clip = RBTCacheManager.getInstance().getClip(clipID);
		}
		// RBT-18671
		// RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		// Subscriber subscriber = rbtdbManager.getSubscriber(webServiceContext
		// .getString(param_subscriberID));
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(webServiceContext.getString(param_subscriberID));
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber clientsubscriber = RBTClient.getInstance()
				.getSubscriber(rbtDetailsRequest);
		ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
		Subscriber subscriber = helper.convertClientSubscriberToContentSubscriber(clientsubscriber);
		if (subscriber != null) {
			logger.info("\n :---> Subscriber subscriber" + subscriber.toString());
		}

		if (clipIDs == null || clipIDs.length <= 1) {
			if (clipID != null && clip == null) {
				int selType = -1;
				if (webServiceContext.containsKey(WebServiceConstants.param_selectionType)) {
					String strSelType = webServiceContext.getString(WebServiceConstants.param_selectionType);
					try {
						selType = Integer.parseInt(strSelType);
					} catch (NumberFormatException ne) {
					}
				}

				if (selType != iRBTConstant.PROFILE_SEL_TYPE && category.getCategoryTpe() != iRBTConstant.RECORD
						&& category.getCategoryTpe() != iRBTConstant.KARAOKE) {
					throw new UserException(CLIP_NOT_EXIST);
				} else {
					String defClassType = RBTParametersUtils.getParamAsString("COMMON",
							"DEFAULT_CHARGE_CLASS_FOR_RECORDED_CLIPS", "DEFAULT");
					chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(defClassType);

					return chargeClass;
				}
			}
			if (clip != null && clip.getClipEndTime().getTime() < System.currentTimeMillis()) {
				throw new UserException(Constants.CLIP_EXPIRED);
			}

			/*
			 * Added below code to return getNextChargeClass as FREE for first
			 * song through MOBILEAPP
			 */
			if (subscriber != null) {
				logger.info("\n:---> subscriber.extraInfo() " + subscriber.extraInfo());
			}
			HashMap<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML((subscriber != null) ? subscriber.extraInfo() : null);
			if (extraInfoMap == null)
				extraInfoMap = new HashMap<String, String>();

			String mode = webServiceContext.getString(WebServiceConstants.param_mode);
			List<String> supportedModesList = Arrays
					.asList(RBTParametersUtils.getParamAsString("MOBILEAPP", "mobileapp.free.sel.mode", "").split(","));
			if (supportedModesList.contains(mode) && !extraInfoMap.containsKey("MOBILE_APP_FREE")) {
				chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(
						RBTParametersUtils.getParamAsString("MOBILEAPP", "mobileapp.free.chargeclass", "DEFAULT"));
				if (chargeClass != null) {
					return chargeClass;
				}
			}

			chargeClass = DataUtils.getNextChargeClassForSubscriber(webServiceContext, subscriber, category, clip);

		} else {
			for (String eachClipID : clipIDs) {
				webServiceContext.put(WebServiceConstants.param_clipID, eachClipID);
				clip = RBTCacheManager.getInstance().getClip(eachClipID);
				if (clip == null) {
					logger.debug("Clip not exists, clipID : " + eachClipID);
					continue;
				}

				if (clip != null && clip.getClipEndTime().getTime() < System.currentTimeMillis()) {
					logger.debug("Clip Expired, clipID : " + eachClipID);
					continue;
				}

				chargeClass = DataUtils.getNextChargeClassForSubscriber(webServiceContext, subscriber, category, clip);

			}

		}

		return chargeClass;

	}

	private void validateRequest(WebServiceContext webServiceContext) throws UserException {
		String subscriberID = webServiceContext.getString(WebServiceConstants.param_subscriberID);
		if (subscriberID == null || subscriberID.isEmpty())
			throw new UserException(INVALID_SUBSCRIBER);
		// RBT-18671
		// RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		// Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber clientsubscriber = RBTClient.getInstance()
				.getSubscriber(rbtDetailsRequest);
		ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
		Subscriber subscriber = helper.convertClientSubscriberToContentSubscriber(clientsubscriber);
		if (subscriber != null) {

			logger.info("\n:---> validateRequest ContentSubscriber" + subscriber.toString());
		}
		// RBT-18671
		String validResponse = DataUtils.isValidUser(webServiceContext, subscriber);

		logger.info("\n :---> VALID RESP " + validResponse);
		if (subscriber != null) {
			logger.info("\n :---> subscriber.subYes() " + subscriber.subYes());
		}

		if (!validResponse.equals(WebServiceConstants.VALID))
			throw new UserException(INVALID_SUBSCRIBER);

		if (subscriber != null && (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_ERROR)
				|| clientsubscriber.getStatus().equalsIgnoreCase("deact_pending"))) {
			throw new UserException(INVALID_SUBSCRIBER_STATE);
		}
	}

}
