package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.vfturkey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class VodafoneTurkeySmsProcessor extends SmsProcessor {

	protected static Logger logger = Logger
			.getLogger(VodafoneTurkeySmsProcessor.class);

	public VodafoneTurkeySmsProcessor() throws RBTException {
		super();
	}

	@Override
	public void processActivationRequest(Task task) {

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		boolean isDirectActRequest = false;
		if (task.containsKey(param_isdirectact))
			isDirectActRequest = task.getString(param_isdirectact)
					.equalsIgnoreCase("true");
		Clip clip = null;
		String clipID = null;
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList != null && smsList.size() > 0) {
			for (String promocode : smsList) {
				try {
					int promoId = Integer.parseInt(promocode);
					if (promoId >= param(COMMON, MIN_VALUE_PROMO_ID, 0)) {
						clip = getClipByPromoId(promoId + "");
						if (clip != null) {
							clipID = clip.getClipId() + "";
							task.setObject(CLIP_OBJ, clip);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		CosDetails cosDetail = (CosDetails) task.getObject(param_cos);
		if (cosDetail != null) {
			task.setObject(param_COSID, cosDetail.getCosId());
		}
		// ALLOW COS UPGRADE REQ
		boolean isUpgradeReqest = false;
		if (cosDetail != null
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE)
				&& !(subscriber.getCosID().equals(cosDetail.getCosId()))) {
			isUpgradeReqest = true;
		}
		if (isDirectActRequest
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE)) {
			if (!isUpgradeReqest) {
				logger.info("processActivationRequest:: DirectActivationRequest & user is already ACTIVE");
				task.setObject(param_response, "ALREADYACTIVE");
				return;
			}
		}

		if (isUserActive(subscriber.getStatus()) && !isDirectActRequest
				&& clip == null && !isUpgradeReqest) {

			String smsText = CacheManagerUtil.getSmsTextCacheManager()
					.getSmsText(
							ACTIVATION_FAILURE + "_"
									+ subscriber.getStatus().toUpperCase(),
							subscriber.getLanguage());
			if (smsText == null) {
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, ACTIVATION_FAILURE,
								m_activationFailureDefault,
								subscriber.getLanguage()));
			} else {
				task.setObject(param_responseSms, smsText);
			}
			return;
		}

		String subClass = getParamAsString(SMS, "SUB_CLASS_FOR_DT_SERVICE",
				null);
		if (subClass != null) {
			task.setObject(param_subclass, subClass);
		}

		if (isConfirmationOnWithoutOffer) {
			processForUserConfirmationWithoutOffer(task);
			return;
		}

		boolean isConsentSubscriptionRequest = false;

		if ((!isUserActive(subscriber.getStatus()) || isDirectActRequest || isUpgradeReqest)) {

			if (isConfirmationOn) {
				sendBaseAmoutForUserConfirmation(task);
				return;
			}
			subscriber = processActivation(task);

			if (isDirectActRequest)
				task.setObject(param_response, "SUCCESS");

			if (getParamAsBoolean(SMS,
					"SENDING_CONSENT_SUBSCRIPTION_MESSAGE_ENABLED", "FALSE")
					&& subscriber != null && subscriber.isSubConsentInserted()) {
				logger.info("Consent Subscription Request through SMS : "
						+ isConsentSubscriptionRequest);
				isConsentSubscriptionRequest = true;
			}

			if (isConsentSubscriptionRequest) {
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, CONSENT_ACTIVATION_SUCCESS,
								m_consentActivationSuccessDefault,
								subscriber.getLanguage()));

			} else if (!isDirectActRequest
					&& (subscriber != null && isUserActive(subscriber
							.getStatus())))
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, ACTIVATION_SUCCESS,
								m_activationSuccessDefault,
								subscriber.getLanguage()));
		}

		if ((subscriber == null || !isUserActive(subscriber.getStatus()))
				&& !isConsentSubscriptionRequest) {
			task.setObject(param_responseSms,
					getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault, null));
			return;
		}

		logger.info("processActivationRequest::  smsList is zero, isDirectActRequest >"
				+ isDirectActRequest);
		if (isDirectActRequest) {
			task.setObject(param_response, "SUCCESS");

			if (task.getString(param_response).equals(SUCCESS)) {
				com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass subscriptionClass = getSubscriptionClass(task
						.getString(param_subclass));
				if (subscriptionClass != null) {
					task.setObject(param_Sender, "56789");
					task.setObject(param_Reciver,
							task.getString(param_subscriberID));
					task.setObject(param_Msg,
							subscriptionClass.getSmsOnSubscription());
					sendSMS(task);
				}
			}
		}

	}

	private void sendBaseAmoutForUserConfirmation(Task task) {

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String language = subscriber.getLanguage();
		Date sentTime = new Date();
		String smsText = null;
		String clipName = null;
		String artist = null;
		double base_amount = 0.0;
		double sel_amount = 0.0;
		String smsKeyWord = null;
		String clipId = null;
		CosDetails cosDetail = null;
		Clip clip = (Clip) task.getObject(CLIP_OBJ);
		if (clip == null) {
			if (task.getString(param_clipid) == null) {
				task.setObject(param_clipid, task.getString("CLIPID"));
			}
			if (task.getString(param_clipid) != null) {
				clip = RBTCacheManager.getInstance().getClip(
						task.getString(param_clipid));
			}
		}
		if (clip != null
				&& clip.getClipEndTime().getTime() < System.currentTimeMillis()) {
			clipName = clip.getClipName();
			artist = clip.getArtist();
			smsText = getSMSTextForID(
					task,
					CLIP_EXPIRED_SMS_TEXT,
					getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault,
							language), language);
		} else {
			boolean isBlockViralEntry = false;
			if (task.containsKey("SEL_SMS")
					&& isUserActive(subscriber.getStatus())
					&& task.getObject(CLIP_OBJ) == null) {
				isBlockViralEntry = true;
				smsText = getSMSTextForID(task, "OPT_IN_FAILURE_ACT_SEL_SMS",
						m_optInFailureActSelSMS, language);
			} else if (isUserActive(subscriber.getStatus())) {
				// active user send request for UPGRADE
				smsText = getSMSTextForID(task,
						"OPT_IN_CONFIRMATION_UPGRADE_SMS", m_optInUpgradeSMS,
						language);
			} else if (task.containsKey("SEL_SMS")) {
				// new user or in-active user send request for song selection
				smsText = getSMSTextForID(task,
						"OPT_IN_CONFIRMATION_ACT_BASE_SEL_SMS",
						m_optInConfirmationActBaseSelSMS, language);
			} else {
				// new user or in-active user send activation request
				smsText = getSMSTextForID(task, "OPT_IN_CONFIRMATION_ACT_SMS",
						m_optInConfirmationActSMS, language);
			}

			String selectedBy = task.getString(param_actby);
			if (selectedBy == null) {
				selectedBy = task.getString(param_actMode);
			}
			String subscriberId = null;
			String subscriptionClass = null;
			if (subscriber != null) {
				subscriberId = subscriber.getSubscriberID();
				subscriptionClass = subscriber.getSubscriptionClass();
			}
			String type = "SMSCONFPENDING";

			Map<String, String> extraInfoMap = new HashMap<String, String>();

			cosDetail = (CosDetails) task.getObject(param_cos);
			if (cosDetail != null) {
				extraInfoMap.put(param_COSID, cosDetail.getCosId());
			}

			String smsTextBasedOnOffer = null;
			String offerID = null;

			if (isSupportBasePackageOffer
					&& (subscriber.getStatus().equalsIgnoreCase(
							WebServiceConstants.NEW_USER)
							|| subscriber.getStatus().equalsIgnoreCase(
									WebServiceConstants.DEACTIVE) || (cosDetail != null && subscriber
							.getStatus().equalsIgnoreCase(
									WebServiceConstants.ACTIVE)))) {

				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
						subscriber.getSubscriberID());
				rbtDetailsRequest
						.setOfferType(Offer.OFFER_TYPE_SUBSCRIPTION_STR);
				rbtDetailsRequest.setMode(SMS);
				if(cosDetail!=null){
					rbtDetailsRequest.setClassType(cosDetail.getSubscriptionClass());
				}else{
					String prismDefKey = CacheManagerUtil.getParametersCacheManager().getParameterValue("SMS","DEFAULT_SRVKEY_FOR_NO_OFFER_FROM_PRISM", "OFFER_NOT_REQUIRED");
					rbtDetailsRequest.setClassType(prismDefKey);
				}
				Offer[] offer = RBTClient.getInstance().getPackageOffer(
						rbtDetailsRequest);
				logger.info("offer array obj is: " + Arrays.toString(offer));
				if (offer != null
						&& offer.length > 0
						&& (offer[0].getOfferID() == null || !offer[0]
								.getOfferID().equalsIgnoreCase("-1"))) {
					subscriptionClass = offer[0].getSrvKey();
					base_amount = offer[0].getAmount();
					offerID = offer[0].getOfferID();
					extraInfoMap.put("BASE_OFFERID", offerID);
					// UPGRADE REQ
					extraInfoMap.put("UPGRADE_SUBSCRIPTION_CLASS",
							subscriptionClass);

				}
				if (offer == null || offer != null && offer.length == 0
						|| offer != null && offer.length > 0
						&& offer[0].getOfferID() != null
						&& offer[0].getOfferID().equalsIgnoreCase("-1")) {
					isBlockViralEntry = true;
					smsTextBasedOnOffer = getSMSTextForID(task,
							"TECHNICAL_ERROR_FOR_BASE_OFFER", null);
				}

				if (smsTextBasedOnOffer != null) {
					smsText = smsTextBasedOnOffer;
				}
			}

			if (!isBlockViralEntry) {
				if (task.containsKey("SEL_SMS")) {
					// new user or in-active user send request for song
					// selection
					smsText = getSMSTextForID(task,
							"OPT_IN_CONFIRMATION_ACT_BASE_SEL_SMS_"
									+ subscriptionClass,
							m_optInConfirmationActBaseSelSMS, language);
				} else if (subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.NEW_USER)
						|| subscriber.getStatus().equalsIgnoreCase(
								WebServiceConstants.DEACTIVE)) {
					smsTextBasedOnOffer = getSMSTextForID(
							task,
							"OPT_IN_BASE_CONFIRMATION_SMS_" + subscriptionClass,
							null, language);
				}
				if (subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.NEW_USER)
						|| subscriber.getStatus().equalsIgnoreCase(
								WebServiceConstants.DEACTIVE)) {
					extraInfoMap.put("SUBSCRIPTION_CLASS", subscriptionClass);
				}/* else {
					// UPGRADE REQ
					extraInfoMap.put("UPGRADE_SUBSCRIPTION_CLASS",
							subscriptionClass);
				}*/
			}
			logger.info("SMS Confirmation smsText = " + smsText);

			if (clip != null) {
				clipId = "" + clip.getClipId();
				clipName = clip.getClipName();
				artist = clip.getArtist();
			}
			if (!isBlockViralEntry) {
				String validateDwnldRes = validateDownloadLimitForCos(task,
						clip, cosDetail, extraInfoMap);
				String extraInfo = DBUtility
						.getAttributeXMLFromMap(extraInfoMap);
				if (validateDwnldRes != null) {
					smsText = validateDwnldRes;
				}
				if (validateDwnldRes == null) {
					RBTDBManager.getInstance().insertViralSMSTable(
							subscriberId, sentTime, type,
							task.getString(param_callerid), clipId, 0,
							selectedBy, null, extraInfo);
				}
			}
		}
		if (cosDetail == null) {
			if (subscriber != null) {
				String cosId = subscriber.getCosID();
				cosId = (subscriber.getUserInfoMap() != null && subscriber
						.getUserInfoMap().containsKey("COS_ID")) ? subscriber
						.getUserInfoMap().get("COS_ID") : cosId;
				cosDetail = (CosDetails) CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosId);
				if(cosDetail!=null)
				smsKeyWord = cosDetail.getSmsKeyword();
			}
		} else {
			smsKeyWord = cosDetail.getSmsKeyword();
		}

		String sms = getSubstituedSMS(smsText, clipName, artist, null,
				base_amount + "", sel_amount + "", smsKeyWord);
		task.setObject(param_responseSms, sms);
		sendSMS(task);
		return;

	}

	protected String getSubstituedSMS(String smsText, String str1, String str2,
			String str3, String actAmt, String selAmt, String smsKeyWord) {
		if (smsText == null)
			return null;

		smsText = substitutePackNameValidDays(smsText, smsKeyWord);

		if (actAmt != null) {
			while (smsText.indexOf("%ACT_AMT") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%ACT_AMT"))
						+ actAmt
						+ smsText.substring(smsText.indexOf("%ACT_AMT") + 8);
			}
		}
		if (selAmt != null) {
			while (smsText.indexOf("%SEL_AMT") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%SEL_AMT"))
						+ selAmt
						+ smsText.substring(smsText.indexOf("%SEL_AMT") + 8);
			}
		}

		if (str2 == null) {
			if (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
		} else if (str3 == null) {
			while (smsText.indexOf("%S") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
						+ smsText.substring(smsText.indexOf("%S") + 2);
			}
			while (smsText.indexOf("%C") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
						+ smsText.substring(smsText.indexOf("%C") + 2);
			}
			while (smsText.indexOf("%L") != -1) {
				smsText = smsText.replace(" %L", "");
			}
		} else {
			while (smsText.indexOf("%S") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
						+ smsText.substring(smsText.indexOf("%S") + 2);
			}
			while (smsText.indexOf("%C") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
						+ smsText.substring(smsText.indexOf("%C") + 2);
			}
			while (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str3
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
		}

		return smsText;
	}

	@Override
	public void processActNSel(Task task) {

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		boolean isActOptional = param(SMS, IS_ACT_OPTIONAL, false);
		boolean isActRequest = task.getString(IS_ACTIVATION_REQUEST)
				.equalsIgnoreCase("true");

		boolean isDirectActRequest = false;
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		if (task.containsKey(param_isdirectact))
			isDirectActRequest = task.getString(param_isdirectact)
					.equalsIgnoreCase("true");

		if (isDirectActRequest
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE)) {
			logger.info("processActNSel:: DirectActivationRequest & user is already ACTIVE");
			task.setObject(param_response, "ALREADYACTIVE");
			return;
		}

		if (isUserActive(subscriber.getStatus()) && isActRequest
				&& !isDirectActRequest) {
			String smsText = CacheManagerUtil.getSmsTextCacheManager()
					.getSmsText(
							ACTIVATION_FAILURE + "_"
									+ subscriber.getStatus().toUpperCase(),
							subscriber.getLanguage());
			if (smsText == null) {
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, ACTIVATION_FAILURE,
								m_activationFailureDefault,
								subscriber.getLanguage()));
			} else {
				task.setObject(param_responseSms, smsText);
			}
			return;
		}

		CosDetails cosDetail = (CosDetails) task.getObject(param_cos);
		logger.info("Sms list : " + smsList + " and teh cos detail is : "
				+ cosDetail);

		// ALLOW COS UPGRADE REQ
		boolean isUpgradeReqest = false;
		if (cosDetail != null
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE)
				&& !(subscriber.getCosID().equals(cosDetail.getCosId()))) {
			isUpgradeReqest = true;
		}

		if (cosDetail != null) {
			if (isUserActive(subscriber.getStatus()) && !isUpgradeReqest) {
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, ACTIVATION_FAILURE,
								m_activationFailureDefault,
								subscriber.getLanguage()));
				return;
			}

			task.setObject(param_COSID, cosDetail.getCosId());
		}

		if (!populateFromTimeAndToTime(task)) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, TIME_OF_DAY_FAILURE,
							m_timeOfTheDayFailureDefault,
							subscriber.getLanguage()));
			return;
		}
		String subClass = getParamAsString(SMS, "SUB_CLASS_FOR_DT_SERVICE",
				null);
		if (subClass != null) {
			task.setObject(param_subclass, subClass);
		}
		String response = null;
		Clip clip = null;

		if (smsList != null && smsList.size() > 0) {
			if (smsList.get(0) != null
					&& !smsList.get(0).equalsIgnoreCase("null")) {
				clip = getProfileClip(task);
				if (clip == null
						&& task.containsKey(param_isDefaultProfileHrsByIndex)) {
					task.setObject(
							param_responseSms,
							getSMSTextForID(task, REQUEST_MORE_NO_SEARCH,
									m_reqMoreSMSNoSearchDefault,
									subscriber.getLanguage()));
					return;
				}/*
				 * else if (clip != null) { //CATEGORY_ID 99
				 * processSetTempOverride(task); return; }
				 */

				if (task.getObject(CLIP_OBJ) == null)
					getCategoryAndClipForPromoID(task, smsList.get(0));

				clip = (Clip) task.getObject(CLIP_OBJ);

				if (clip == null
						|| clip.getClipEndTime().getTime() < System
								.currentTimeMillis()) {
					// if CosDetails object present in task object and
					// clipPromoID sent is invalid, then activate the user on
					// the COS
					if (cosDetail != null) {
						if (isConfirmationOn) {
							sendBaseAmoutForUserConfirmation(task);
							return;
						}
						subscriber = processActivation(task);
						boolean isConsentSubscriptionRequest = false;
						if (getParamAsBoolean(SMS,
								"SENDING_CONSENT_SUBSCRIPTION_MESSAGE_ENABLED",
								"FALSE")
								&& subscriber != null
								&& subscriber.isSubConsentInserted()) {
							logger.info("Consent Subscription Request through SMS : "
									+ isConsentSubscriptionRequest);
							isConsentSubscriptionRequest = true;
						}

						if (isConsentSubscriptionRequest) {
							task.setObject(
									param_responseSms,
									getSMSTextForID(task,
											CONSENT_ACTIVATION_SUCCESS,
											m_consentActivationSuccessDefault,
											subscriber.getLanguage()));

						} else if (subscriber != null
								&& isUserActive(subscriber.getStatus())) {
							task.setObject(
									param_responseSms,
									getSMSTextForID(task, ACTIVATION_SUCCESS,
											m_activationSuccessDefault,
											subscriber.getLanguage()));
						}

						if ((subscriber == null || !isUserActive(subscriber
								.getStatus())) && !isConsentSubscriptionRequest) {
							String language = null;
							if (subscriber != null)
								language = subscriber.getLanguage();

							if (clip != null
									&& clip.getClipEndTime().getTime() < System
											.currentTimeMillis())
								task.setObject(
										param_responseSms,
										getSMSTextForID(
												task,
												CLIP_EXPIRED_SMS_TEXT,
												getSMSTextForID(task,
														HELP_SMS_TEXT,
														m_helpDefault, language),
												language));
							else
								task.setObject(
										param_responseSms,
										getSMSTextForID(
												task,
												CLIP_DOES_NOT_EXIST_SMS_TEXT,
												getSMSTextForID(task,
														HELP_SMS_TEXT,
														m_helpDefault, language),
												language));

						}
						return;
					}

					if (!isUserActive(subscriber.getStatus())) {
						if (isConfirmationOn) {
							sendBaseAmoutForUserConfirmation(task);
							return;
						}
						if (isDirectActRequest) {
							smsList.remove(0);
						} else if (isActRequest || isActOptional) {
							if (task.containsKey(param_isSuperHitAlbum))
								task.setObject(param_isPromoIDFailure, "TRUE");

							if (clip != null
									&& clip.getClipEndTime().getTime() < System
											.currentTimeMillis())
								task.setObject(
										param_responseSms,
										getSMSTextForID(
												task,
												CLIP_EXPIRED_SMS_TEXT,
												getSMSTextForID(
														task,
														PROMO_ID_FAILURE,
														m_promoIDFailureDefault,
														subscriber
																.getLanguage()),
												subscriber.getLanguage()));
							else
								task.setObject(
										param_responseSms,
										getSMSTextForID(
												task,
												CLIP_DOES_NOT_EXIST_SMS_TEXT,
												getSMSTextForID(
														task,
														PROMO_ID_FAILURE,
														m_promoIDFailureDefault,
														subscriber
																.getLanguage()),
												subscriber.getLanguage()));

							return;
						}
					}
				}
			} else {
				smsList.remove(0);
			}
		} else if (cosDetail != null) {
			if (isConfirmationOn) {
				sendBaseAmoutForUserConfirmation(task);
				return;
			}
			subscriber = processActivation(task);
			boolean isConsentSubscriptionRequest = false;
			if (getParamAsBoolean(SMS,
					"SENDING_CONSENT_SUBSCRIPTION_MESSAGE_ENABLED", "FALSE")
					&& subscriber != null && subscriber.isSubConsentInserted()) {
				logger.info("Consent Subscription Request through SMS : "
						+ isConsentSubscriptionRequest);
				isConsentSubscriptionRequest = true;
			}

			if (isConsentSubscriptionRequest) {
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, CONSENT_ACTIVATION_SUCCESS,
								m_consentActivationSuccessDefault,
								subscriber.getLanguage()));

			} else if (subscriber != null
					&& isUserActive(subscriber.getStatus())) {
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, ACTIVATION_SUCCESS,
								m_activationSuccessDefault,
								subscriber.getLanguage()));
			}

			if ((subscriber == null || !isUserActive(subscriber.getStatus()))
					&& !isConsentSubscriptionRequest) {
				String language = null;
				if (subscriber != null)
					language = subscriber.getLanguage();

				task.setObject(
						param_responseSms,
						getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault,
								language));
			}
			return;
		} else {
			if (task.getString(param_callerid) != null) {
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, PROMO_ID_FAILURE,
								m_promoIDFailureDefault,
								subscriber.getLanguage()));
				return;
			}
		}

		clip = (Clip) task.getObject(CLIP_OBJ);
		Clip[] clips = rbtCacheManager.getClipsInCategory(99);
		if (clip != null) {
			boolean clipFound = false;
			for (Clip clp : clips) {
				if (clp != null && clip.getClipId() == clp.getClipId()) {
					task.setObject(
							param_responseSms,
							getSMSTextForID(task, CLIP_DOES_NOT_EXIST_SMS_TEXT,
									m_clipNotAvailableDefault,
									subscriber.getLanguage()));
					clipFound = true;
					break;
				}
			}
			if (clipFound) {
				return;
			}
		}
		/*
		 * if((!isUserActive(subscriber.getStatus()) || isDirectActRequest) &&
		 * (isActRequest || (isActOptional && smsList.size() > 0 ))) {
		 * subscriber = processActivation(task);
		 * 
		 * if(isDirectActRequest) task.setObject(param_response, "SUCCESS");
		 * if(!isDirectActRequest && isUserActive(subscriber.getStatus()))
		 * task.setObject(param_responseSms,
		 * getSMSTextForID(task,ACTIVATION_SUCCESS,
		 * m_activationSuccessDefault)); }
		 * if(!isUserActive(subscriber.getStatus())) {
		 * task.setObject(param_responseSms, getSMSTextForID(task,HELP_SMS_TEXT,
		 * m_helpDefault)); return; }
		 */

		if (smsList != null && smsList.size() > 0) {
			if (param(COMMON, ALLOW_LOOPING, false)
					&& param(COMMON, ADD_SEL_TO_LOOP, false))
				task.setObject(param_inLoop, "YES");

			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put(
					"CALLER_ID",
					task.getString(param_callerid) == null ? param(SMS,
							SMS_TEXT_FOR_ALL, "all") : task
							.getString(param_callerid));

			if ((isActRequest || isActOptional)) {
				if (isConfirmationOn
						&& (subscriber == null
								|| !isUserActive(subscriber.getStatus()) || (isUserActive(subscriber
								.getStatus()) && isUpgradeReqest))) {
					task.setObject("SEL_SMS", "TRUE");
					sendBaseAmoutForUserConfirmation(task);
					return;
				} else {
					task.setObject(param_alreadyGetSelOffer, true);
					response = processSetSelection(task);
				}
			}

			setResponseSmsFromSelectionResponse(task, response,
					isDirectActRequest, isActRequest);
		}

	}

	private String validateDownloadLimitForCos(Task task, Clip clip,
			CosDetails cosDetail, Map<String, String> extraInfoMap) {

		String smsText = null;
		boolean isBlockViralEntry = false;

		if (task.getString("SEL_SMS") != null) {

			Subscriber subscriber = (Subscriber) task
					.getObject(param_subscriber);
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					subscriber.getSubscriberID());
			Library library = RBTClient.getInstance().getLibraryHistory(
					rbtDetailsRequest);
			logger.info("Library Obtained === " + library);
			if (library != null) {
				Downloads downloads = library.getDownloads();
				if (downloads != null) {
					int noOfActiveDownloads = downloads
							.getNoOfActiveDownloads();
					int maxAllowedDownloads = RBTParametersUtils.getParamAsInt(
							COMMON, "MAX_DOWNLOADS_ALLOWED", -1);
					if (maxAllowedDownloads > 0
							&& noOfActiveDownloads >= maxAllowedDownloads) {
						logger.info("Maximum downloads limit has already reached");
						String maxLimitSms = getSMSTextForID(task,
								"MAX_DOWNLOAD_LIMIT_REACHED", null);
						if (maxLimitSms != null) {
							isBlockViralEntry = true;
							smsText = maxLimitSms;
						}
					} else {
						Download[] availDownloads = downloads.getDownloads();
						for (Download download : availDownloads) {
							if (download.getEndTime().after(new Date())
									&& !download.getDownloadStatus()
											.equalsIgnoreCase("deactive")
									&& !download.getDownloadStatus()
											.equalsIgnoreCase("deact_pending")
									&& clip != null
									&& download
											.getRbtFile()
											.replaceAll(".wav", "")
											.equalsIgnoreCase(
													clip.getClipRbtWavFile())) {
								String downloadAlreadyExistsSmsText = getSMSTextForID(
										task, "DOWNLOAD_ALREADY_EXISTS", null);
								logger.info("Download already Exists.downloadAlreadyExistsSmsText="
										+ downloadAlreadyExistsSmsText);
								if (downloadAlreadyExistsSmsText != null) {
									isBlockViralEntry = true;
									smsText = downloadAlreadyExistsSmsText;
								}
								break;
							}
						}

						// Checking Charge Class Downloads Limit
						if (cosDetail != null
								&& iRBTConstant.SUB_CLASS
										.equalsIgnoreCase(cosDetail
												.getCosType())) {

							int chargeClassCount = 0;
							String[] chargeClassTokens = cosDetail
									.getFreechargeClass().split(",");
							for (String chargeClassToken : chargeClassTokens) {
								int startIndex = chargeClassToken.indexOf('*');
								// extraInfoMap.put("CHARGE_CLASS",
								// chargeClassToken.substring(0, startIndex));
								extraInfoMap.put(param_alreadyGetSelOffer,
										"true");
								if (startIndex != -1) {
									chargeClassCount = Integer
											.parseInt(chargeClassToken
													.substring(startIndex + 1));
								} else {
									smsText = getSMSTextForID(task,
											"TECHNICAL", null);
									logger.error("CHARGE CLASS IS NOT CONFIGURED SO BY DEFAULT DOWNLOAD COUNT IS CONSIDERING AS ZERO");
								}
							}

							if (subscriber != null
									&& subscriber.getNumMaxSelections() >= chargeClassCount) {
								smsText = getSMSTextForID(
										task,
										"CHARGE_CLS_MAX_DOWNLOAD_LIMIT_REACHED",
										null);
							}
						}
					}
				}
			}

		}
		return smsText;
	}
}
