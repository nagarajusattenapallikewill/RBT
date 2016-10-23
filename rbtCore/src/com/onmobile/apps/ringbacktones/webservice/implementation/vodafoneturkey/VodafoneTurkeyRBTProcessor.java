package com.onmobile.apps.ringbacktones.webservice.implementation.vodafoneturkey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.ExtraInfoKey;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Status;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Type;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.grameen.GrameenRBTProcessor;

public class VodafoneTurkeyRBTProcessor extends GrameenRBTProcessor {
	private static Logger logger = Logger
			.getLogger(VodafoneTurkeyRBTProcessor.class);

	@Override
	public String processActivation(WebServiceContext task) {

		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			logger.info("Processing subscriber activation. SubscriberId: "
					+ subscriberID);
			Subscriber subscriber = DataUtils.getSubscriber(task);
			boolean isLimitedPackRequest = false;
			// boolean isMusicPouchPackRequest = false;

			response = isValidUser(task, subscriber);
			if (!response.equals(VALID)) {
				logger.warn("Failed to activate subscriber. Subscriber status: "
						+ response);
				writeEventLog(subscriberID, getMode(task), "104", SUBSCRIPTION,
						null);
				return response;
			}

			HashMap<String, String> userInfoMap = getUserInfoMap(task);
			if (task.containsKey(param_scratchNo)) {
				userInfoMap.put("SCRN", task.getString(param_scratchNo));
				userInfoMap.put("SCRS", "2");

			}
			boolean hitOfferAgain = true;
			String status = Utility.getSubscriberStatus(subscriber);

			Boolean isCosIdPresentInRequestAndIsAPackCosId = false;
			Boolean isCosIdPresentInRequest = false;
			if (task.containsKey(param_cosID)) {

				String cosID = task.getString(param_cosID);
				logger.debug("Validating Cos details for the cosID: " + cosID);
				CosDetails cosDetails = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosID);

				if (cosDetails == null) {
					logger.error("COSID: "
							+ cosID
							+ " is not configured in rbt_cos_details. Returning "
							+ COS_NOT_EXISTS);
					return COS_NOT_EXISTS;
				}

				isCosIdPresentInRequest = true;
				isCosIdPresentInRequestAndIsAPackCosId = cosDetails != null
						&& cosDetails.getCosType() != null
						&& (cosDetails.getCosType().equalsIgnoreCase(
								iRBTConstant.SONG_PACK)
								|| cosDetails.getCosType().equalsIgnoreCase(
										iRBTConstant.LIMITED_DOWNLOADS)
								|| cosDetails.getCosType().equalsIgnoreCase(
										iRBTConstant.UNLIMITED_DOWNLOADS)
								|| cosDetails
										.getCosType()
										.equalsIgnoreCase(
												iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE) || cosDetails
								.getCosType().equalsIgnoreCase(
										iRBTConstant.MUSIC_POUCH));

				if (cosDetails != null) {
					if (iRBTConstant.LIMITED_DOWNLOADS
							.equalsIgnoreCase(cosDetails.getCosType())) {
						isLimitedPackRequest = true;
					} else if (iRBTConstant.MUSIC_POUCH
							.equalsIgnoreCase(cosDetails.getCosType())) {
						// isMusicPouchPackRequest = true;

					} else if (iRBTConstant.SUB_CLASS
							.equalsIgnoreCase(cosDetails.getCosType())) {
						boolean isOfferIdNotExists = !(task
								.containsKey(param_offerID));
						boolean isOfferSubscriptionClassNotExists = !(task
								.containsKey(param_subClass));
						boolean isRentalPackNotExists = !(task
								.containsKey(param_rentalPack));
						if (isRentalPackNotExists && isOfferIdNotExists
								&& isOfferSubscriptionClassNotExists) {
							String offerSubscriptionClass = makeOfferHit(task,
									subscriber, userInfoMap);
							hitOfferAgain = false;
							if (offerSubscriptionClass != null) {
								if (Utility.isUserActive(status)) {
									task.put(param_rentalPack,
											offerSubscriptionClass);
								} else {
									task.put(param_subClass,
											offerSubscriptionClass);
								}
							} else {
								return BASE_OFFER_NOT_AVAILABLE;
							}
						}

					}
				}
				/*
				 * If Cos Id contains in the Task, it will be assumed as music
				 * pack and add it to the task with pack Cos id.
				 */
				if (cosDetails != null
						&& cosDetails.getCosType() != null
						&& (cosDetails.getCosType().equalsIgnoreCase(
								iRBTConstant.SONG_PACK)
								|| cosDetails.getCosType().equalsIgnoreCase(
										iRBTConstant.LIMITED_DOWNLOADS)
								|| cosDetails.getCosType().equalsIgnoreCase(
										iRBTConstant.UNLIMITED_DOWNLOADS) || cosDetails
								.getCosType().equalsIgnoreCase(
										iRBTConstant.MUSIC_POUCH))) {
					String packCosId2 = cosDetails.getCosId();
					task.put(param_packCosId, packCosId2);
					logger.debug("Validated cos details. SubscriberId: "
							+ subscriberID + ", packCosId: " + packCosId2);
				}
			}

			// Added by Sandeep... Verifying packCosID is valid or not
			CosDetails cosDetails = null;
			if (task.containsKey(param_packCosId)) {
				String cosID = task.getString(param_packCosId);
				logger.debug("Validating Cos details for the packCosID: "
						+ cosID);
				cosDetails = CacheManagerUtil.getCosDetailsCacheManager()
						.getCosDetail(cosID, DataUtils.getUserCircle(task));
				if (cosDetails == null
						|| !cosDetails.getEndDate().after(new Date())) {
					logger.warn("Invalid Pack Cos. subscriberId: "
							+ subscriberID + ", cosID: " + cosID);
					return INVALID_PACK_COS_ID;
				}
			}

			// added by sridhar.sindiri
			// adds "PCA=TRUE" in extraInfo when a RRBT request comes for a
			// subscriber which has already active announcements
			if (RBTDeploymentFinder.isRRBTSystem()
					&& getParamAsBoolean(iRBTConstant.COMMON,
							iRBTConstant.PROCESS_ANNOUNCEMENTS, "FALSE")) {
				SubscriberAnnouncements[] subscriberAnnouncements = rbtDBManager
						.getActiveSubscriberAnnouncemets(task
								.getString(param_subscriberID));
				if (subscriberAnnouncements != null
						&& subscriberAnnouncements.length != 0)
					userInfoMap.put(iRBTConstant.EXTRA_INFO_PCA_FLAG, "TRUE");
			}

			boolean useSubManager = true;

			// Upgrade ADRBT user to a charged subscription class
			if (isUpgradeAdRBTAct) {
				if (subscriber != null
						&& subscriber.subYes().equals(
								iRBTConstant.STATE_ACTIVATED)) {
					if (subscriber.rbtType() == 1
							&& subscriber.subscriptionClass() != null
							&& subscriber.subscriptionClass().equalsIgnoreCase(
									getParamAsString(iRBTConstant.COMMON,
											"ADRBT_SUB_CLASS", "ADRBT"))) {
						task.put(
								param_rentalPack,
								getParamAsString(iRBTConstant.COMMON,
										"ADRBT_SUB_CLASS", "ADRBT"));
						task.put(param_rbtType, "1");
					}

				}
			}

			if (task.containsKey(param_rentalPack)) {
				logger.info("Validating rental pack. subscriberId: "
						+ subscriberID);
				if (DataUtils.isSubscriberAllowedForUpgradation(subscriber)) {
					logger.info("Upgrading Subscriber. SubscriberId: "
							+ subscriberID);
					if (!task.containsKey(param_scratchNo)) {
						if (userInfoMap.containsKey("SCRS"))
							userInfoMap.remove("SCRS");
						if (userInfoMap.containsKey("SCRN"))
							userInfoMap.remove("SCRN");
					}

					String subscriptionClass = task.getString(param_rentalPack);
					String activatedBy = task.getString(param_mode);
					String activationInfo = task.getString(param_modeInfo);
					boolean success = false;
					if (isSupportSMClientModel(task, BASE_OFFERTYPE)) {
						success = smConvertSubscription(task,
								subscriptionClass, subscriberID, subscriber,
								activatedBy, activationInfo,
								isLimitedPackRequest);
					} else {
						int newRbtType = 0;
						if (task.containsKey(param_rbtType))
							newRbtType = Integer.parseInt(task
									.getString(param_rbtType));

						int oldRbtType = subscriber.rbtType();

						HashMap<String, String> extraInfoMap = new HashMap<String, String>();
						if (task.containsKey(param_scratchNo)) {
							extraInfoMap.put("SCRN",
									task.getString(param_scratchNo));
							extraInfoMap.put("SCRS", "2");
						}

						if (isLimitedPackRequest) {
							if (task.containsKey(param_mode)) {
								logger.info("task contains mode and it is limited pack request");
								extraInfoMap.put("PACK_MODE",
										task.getString(param_mode));
								activatedBy = null;
							}
						}

						String cosID = task.getString(param_cosID);
						if (cosID != null) {
							CosDetails cos = CacheManagerUtil
									.getCosDetailsCacheManager().getCosDetail(
											cosID);
							if (cos == null) {
								logger.info("Invalid cosID. Returning response: "
										+ COS_NOT_EXISTS);
								return COS_NOT_EXISTS;
							}

							extraInfoMap.put(iRBTConstant.EXTRA_INFO_COS_ID,
									cosID);
						} else {
							String cosForSubClass = getParamAsString(
									iRBTConstant.COMMON,
									iRBTConstant.SUBCLASS_COS_MAPPING, null);
							if (cosForSubClass != null) {
								StringTokenizer stkParent = new StringTokenizer(
										cosForSubClass, ";");
								while (stkParent.hasMoreTokens()) {
									StringTokenizer stkChild = new StringTokenizer(
											stkParent.nextToken(), ",");
									{
										if (stkChild.countTokens() == 2) {
											String pack = stkChild.nextToken()
													.trim();
											String mappedCos = stkChild
													.nextToken().trim();
											if (pack.equalsIgnoreCase(subscriptionClass)) {
												CosDetails cosObj = CacheManagerUtil
														.getCosDetailsCacheManager()
														.getCosDetail(
																mappedCos,
																subscriber
																		.circleID());
												if (cosObj != null
														&& cosObj.getCosId() != null) {
													extraInfoMap
															.put(iRBTConstant.EXTRA_INFO_COS_ID,
																	cosObj.getCosId());
												}
												break;
											}
										}
									}
								}
							}
						}

						if (subscriber.subYes().equals(
								iRBTConstant.STATE_ACTIVATED)) {
							logger.debug("Subscriber is active. SubscriberId: "
									+ subscriberID);
							if (oldRbtType != newRbtType) // If AdRbt
															// upgradation
							{
								newRbtType = ((oldRbtType != newRbtType) ? newRbtType
										: oldRbtType);
								extraInfoMap
										.put((newRbtType == 1 ? iRBTConstant.EXTRA_INFO_ADRBT_ACTIVATION
												: iRBTConstant.EXTRA_INFO_ADRBT_DEACTIVATION),
												"TRUE");
							}

							String extraInfo = subscriber.extraInfo();
							HashMap<String, String> subExtraInfo = DBUtility
									.getAttributeMapFromXML(extraInfo);
							if (subExtraInfo == null)
								subExtraInfo = new HashMap<String, String>();

							if (!task.containsKey(param_scratchNo)
									&& extraInfo != null) {
								subExtraInfo.remove("SCRS");
								subExtraInfo.remove("SCRN");
							}

							subExtraInfo.putAll(extraInfoMap);
							extraInfo = DBUtility
									.getAttributeXMLFromMap(subExtraInfo);

							boolean concatActivationInfo = true;
							success = rbtDBManager.convertSubscriptionType(
									subscriberID,
									subscriber.subscriptionClass(),
									subscriptionClass, activatedBy,
									activationInfo, concatActivationInfo,
									newRbtType, true, extraInfo, subscriber);
							logger.debug("Upgraded subscription type. SubscriberId: "
									+ subscriberID + ", status: " + success);
						} else {
							logger.debug("Creating provisioning request for SubscriberId: "
									+ subscriberID);
							// Subscriber is in pending state, so request will
							// be stored in transaction table.
							SubscriptionClass subClass = CacheManagerUtil
									.getSubscriptionClassCacheManager()
									.getSubscriptionClass(subscriptionClass);
							if (subClass == null)
								return INVALID_SUBSCRIPTION_CLASS;

							extraInfoMap.put(ExtraInfoKey.RBT_TYPE.toString(),
									String.valueOf(newRbtType));
							String extraInfo = DBUtility
									.getAttributeXMLFromMap(extraInfoMap);

							ProvisioningRequests provisioningRequest = new ProvisioningRequests(
									subscriptionClass, new Date(), extraInfo,
									activatedBy, activationInfo, null, 0, null,
									Status.TOBE_PROCESSED.getStatusCode(),
									subscriberID, null,
									Type.BASE_UPGRADATION.getTypeCode());

							provisioningRequest = ProvisioningRequestsDao
									.createProvisioningRequest(provisioningRequest);
							success = (provisioningRequest != null);
							logger.debug("Created provisioning request. SubscriberId: "
									+ subscriberID + ", status: " + success);
						}
					}
					if (success) {
						response = SUCCESS;
						subscriber = rbtDBManager.getSubscriber(subscriberID);

						// Updated Subscriber object is storing in taskSession &
						// it will be used to build the response element
						task.put(param_subscriber, subscriber);
					} else
						response = FAILED;

					logger.info("Upgraded subscriber. Status: " + response);
					return response;
				}
			}

			// Below if block processes the acceptance Gift Service request for
			// already active users.
			// It just updates the Gift(ViralSMSTable) entry.
			// boolean acceptGift =
			// task.getString(param_action).equalsIgnoreCase(action_acceptGift);
			if (task.getString(param_action)
					.equalsIgnoreCase(action_acceptGift)) {
				logger.info("Processing gift request. subscriber: "
						+ subscriber);
				if (subscriber != null
						&& subscriber.subYes().equals(
								iRBTConstant.STATE_ACTIVATED)) {
					String gifterID = task.getString(param_gifterID);
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");
					Date sentTime = dateFormat.parse(task
							.getString(param_giftSentTime));
					String acceptStatus = "ACCEPT_PRE";

					ViralSMSTable gift = rbtDBManager.getViralPromotion(
							gifterID, subscriberID, sentTime, "GIFTED");
					if (gift.clipID() == null) {
						rbtDBManager.updateViralPromotion(gifterID,
								subscriberID, sentTime, "GIFTED", acceptStatus,
								new Date(), null, null);
						logger.info("Sucessfully updated gift request as"
								+ " Viral promotion");
						return SUCCESS;
					}
				}
			}

			String packExtraInfoXml = null;
			boolean isDirectActivation = false;
			if (task.containsKey(param_isDirectActivation)
					&& task.getString(param_isDirectActivation)
							.equalsIgnoreCase(YES))
				isDirectActivation = true;

			String circleID = DataUtils.getUserCircle(task);
			boolean isPrepaid = DataUtils.isUserPrepaid(task);
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(task.getString(param_packCosId));
			if (cos == null
					|| (cos != null && !iRBTConstant.MUSIC_POUCH
							.equalsIgnoreCase(cos.getCosType()))
					&& !iRBTConstant.LIMITED_DOWNLOADS.equalsIgnoreCase(cos
							.getCosType())) {
				if (task.containsKey(param_cosID)) {
					CosDetails tempCos = CacheManagerUtil
							.getCosDetailsCacheManager().getCosDetail(
									task.getString(param_cosID));
					String cosType = (null != tempCos) ? tempCos.getCosType()
							: null;
					String subcriptionCls = task
							.getString(param_subscriptionClass);
					if (tempCos != null
							&& cosType != null
							&& iRBTConstant.SUB_CLASS.equalsIgnoreCase(cosType)
							&& subcriptionCls != null
							&& !tempCos.getSubscriptionClass()
									.equalsIgnoreCase(subcriptionCls)) {
						cos = tempCos;
					}
					logger.info("Since request contains cosID, cosType is validated. Final cos: "
							+ cos + ", subscriberID: " + subscriberID);
				} else {
					cos = getCos(task, subscriber);
				}
			} else {
				Offer[] offer = RBTSMClientHandler.getInstance().getOffer(
						subscriberID, task.getString(param_mode),
						Offer.OFFER_TYPE_PACK, null, cos.getSmsKeyword(), null);
				if (offer == null || offer.length == 0)
					return PACK_OFFER_NOT_AVAILABLE;
				else
					task.put(param_packOfferID, offer[0].getOfferID());
			}

			if (cos != null
					&& cos.getCosType() != null
					&& (cos.getCosType().equalsIgnoreCase(
							iRBTConstant.SONG_PACK)
							|| cos.getCosType().equalsIgnoreCase(
									iRBTConstant.LIMITED_DOWNLOADS)
							|| cos.getCosType().equalsIgnoreCase(
									iRBTConstant.UNLIMITED_DOWNLOADS) || cos
							.getCosType().equalsIgnoreCase(
									iRBTConstant.MUSIC_POUCH))) {
				cosDetails = cos;
				task.put(param_packCosId, cos.getCosId());
			}

			if (task.containsKey(param_packCosId)) {
				/*
				 * If packOfferId is present in the task, then create and put
				 * offerId into the extra info map and again put extra info map
				 * into the task.
				 */
				String offerId = task.getString(param_packOfferID);
				if (offerId != null) {
					Map<String, String> packExtraInfoMap = new HashMap<String, String>();
					packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
							offerId);
					packExtraInfoXml = DBUtility
							.getAttributeXMLFromMap(packExtraInfoMap);
				}

				if (packCosIdCosIdMap.containsKey(task
						.getString(param_packCosId))) {
					if (!isCosIdPresentInRequest
							|| isCosIdPresentInRequestAndIsAPackCosId) {
						String cosId = packCosIdCosIdMap.get(task
								.getString(param_packCosId));
						cos = CacheManagerUtil.getCosDetailsCacheManager()
								.getCosDetail(cosId);

						logger.info("cosId replaced with DB config. New cosId: "
								+ cosId);
					}
				} else {
					cos = CacheManagerUtil.getCosDetailsCacheManager()
							.getDefaultCosDetail(circleID,
									((isPrepaid) ? YES : NO));
				}
			}

			// If user entry is already there in DB, then current status of the
			// user will be returned.
			if (!rbtDBManager.isSubscriberDeactivated(subscriber)) {
				// added by Sandeep for profile pack when user not deactivated
				if (task.containsKey(param_packCosId)) {
					logger.info("Subscriber is not active, checking for profile pack. subscriberID "
							+ subscriberID);
					String cosID = (String) task.get(param_packCosId);
					if (cosDetails != null
							&& cosDetails.getEndDate().after(new Date())) {
						if (rbtDBManager
								.isPackActivated(subscriber, cosDetails)) {
							logger.warn("Subscriber pack is already active. subscriberID "
									+ subscriberID);
							return PACK_ALREADY_ACTIVE;
						} else {

							boolean isNotActive = !Utility.isUserActive(status);
							boolean isSuspended = status
									.equalsIgnoreCase(SUSPENDED);

							logger.info("Checking isAllowPackActivationForInactiveUser: "
									+ isAllowPackActivationForInactiveUser
									+ ", isNotActive: "
									+ isNotActive
									+ ", isSuspended: "
									+ isSuspended
									+ ", status: " + status);
							if (isAllowPackActivationForInactiveUser
									&& (isNotActive || isSuspended)) {
								logger.info("Returning status: "
										+ status
										+ ", subscriber is not allowed to activate pack");
								return status;
							}

							HashMap<String, String> existingExtraInfoMap = rbtDBManager
									.getExtraInfoMap(subscriber);
							String existingPacks = (existingExtraInfoMap != null) ? existingExtraInfoMap
									.get(iRBTConstant.EXTRA_INFO_PACK) : null;
							String newPack = task.getString(param_packCosId);
							String updatedPacks = (existingPacks != null) ? existingPacks
									+ "," + newPack
									: newPack;
							rbtDBManager.updateExtraInfo(subscriber.subID(),
									iRBTConstant.EXTRA_INFO_PACK, updatedPacks);
							logger.info("Updated pack info of Subscriber. subscriberID "
									+ subscriberID
									+ ", updatedPacks: "
									+ updatedPacks);
							task.remove(param_subscriber);
							task.put(param_subscriber,
									DataUtils.getSubscriber(task));

							String chargingClass = (cosDetails != null) ? cosDetails
									.getSmsKeyword() : null;
							int type = Integer.parseInt(cosID);
							int packStatus = rbtDBManager
									.getPackStatusToInsert(subscriber);
							logger.info("Retrieved pack status to insert subscriberID: "
									+ subscriberID
									+ ", packStatus: "
									+ packStatus);
							String mode = getMode(task);
							String modeInfo = getModeInfo(task);
							String transId = UUID.randomUUID().toString();
							ProvisioningRequests provisioningReqs = new ProvisioningRequests(
									subscriber.subID(), type, mode, modeInfo,
									transId, chargingClass, packStatus);
							provisioningReqs.setExtraInfo(packExtraInfoXml);
							if (rbtDBManager
									.insertProvisioningRequestsTable(provisioningReqs) != null) {
								task.put(param_activatedPackNow, YES);
								logger.info("Successfully inserted subscriber into"
										+ " provisioning requests. subscriberID "
										+ subscriberID);
								return SUCCESS;
							}
							return status;
						}
					} else {
						logger.warn("Invalid pack, pack is expired. subscriberID "
								+ subscriberID + ", cosId: " + cosID);
						return INVALID_PACK_COS_ID;
					}

				}
				// If request is Direct Activation then user will be allowed to
				// activate directly
				if (!isDirectActivation) {
					return status;
				}
			}

			String activatedBy = getMode(task);
			if (task.containsKey(param_actMode)) {
				if (task.getString(param_actMode) != null
						|| !(task.getString(param_actMode).equals(""))) {
					activatedBy = task.getString(param_actMode);
				}
			}
			String activationInfo = getModeInfo(task);

			int activationPeriod = 0;

			int freePeriod = 0;
			if (task.containsKey(param_freePeriod))
				freePeriod = Integer.parseInt(task.getString(param_freePeriod));

			int rbtType = 0;
			if (task.containsKey(param_rbtType))
				rbtType = Integer.parseInt(task.getString(param_rbtType));

			Date startDate = null;

			Date endtDate = null;
			if (task.containsKey(param_subscriptionPeriod)) {
				String subscriptionPeriod = task
						.getString(param_subscriptionPeriod);
				int validityPeriod = Utility
						.getValidityPeriod(subscriptionPeriod);
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, validityPeriod - 1);
				endtDate = calendar.getTime();
			}

			String subscriptionClass = getSubscriptionClass(task, cos);

			boolean playDefault = getParamAsBoolean(iRBTConstant.COMMON,
					"PLAY_DEFAULT_SONG_FOR_VOLUNTARY_SUSPENSION", "FALSE");
			if (!playDefault
					&& subscriber != null
					&& subscriber.subYes().equals(
							iRBTConstant.STATE_DEACTIVATED)) {
				HashMap<String, String> nextBillDateMap = Utility
						.getNextBillingDateOfServices(task);
				String refID = subscriber.refID();
				SimpleDateFormat rbtDateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");
				String nextBillDate = nextBillDateMap.get(refID);
				Date dateObj = new Date();
				if (nextBillDate != null)
					dateObj = rbtDateFormat.parse(nextBillDate);

				boolean isSubClassConfiguredForPrismStatusCheck = false;
				if (subClassesForPrismStatusCheck != null
						&& subClassesForPrismStatusCheck
								.contains(subscriptionClass)) {
					isSubClassConfiguredForPrismStatusCheck = true;
				}

				logger.debug("subscriberId: " + subscriberID
						+ ", subscriptionClass (in request): "
						+ subscriptionClass
						+ ", isSubClassConfiguredForPrismStatusCheck: "
						+ isSubClassConfiguredForPrismStatusCheck);

				if (isSubClassConfiguredForPrismStatusCheck) {
					String prismStatus = nextBillDateMap.get(subscriberID
							+ "_substatus");
					logger.debug("subscriberId: " + subscriberID
							+ ", prismStatus: " + prismStatus);
					if (prismStatus != null
							&& prismStatus.equals("RESUBSCRIPTION")) {
						String confCosID = defautltCosForReactivationMap
								.get(cos.getCosId());
						logger.debug("subscriberId: " + subscriberID
								+ ", cosId: " + cos.getCosId()
								+ ", confCosID: " + confCosID);
						if (confCosID != null) {
							CosDetails confCos = CacheManagerUtil
									.getCosDetailsCacheManager().getCosDetail(
											confCosID);
							cos = confCos;
						}
					}
				} else {
					if (dateObj.after(new Date())) { // Next charing date is
														// after current date
						String confCosID = defautltCosForReactivationMap
								.get(cos.getCosId());
						logger.debug("subscriberId: " + subscriberID
								+ ", cosId: " + cos.getCosId()
								+ ", confCosID: " + confCosID);
						if (confCosID != null) {
							CosDetails confCos = CacheManagerUtil
									.getCosDetailsCacheManager().getCosDetail(
											confCosID);
							cos = confCos;
						}
					}
				}
			}

			// add code for SRBT
			HashMap<String, String> selectionInfoMap = getSelectionInfoMap(task);
			if (selectionInfoMap.get("ALLOW_PUBLISH") != null) {
				userInfoMap.put("ALLOW_PUBLISH",
						selectionInfoMap.get("ALLOW_PUBLISH"));
			}

			/*
			 * Added by SenthilRaja Logic for activate the subscriber in SM
			 * model Get the offer from UI, activate the subscriber as P, invoke
			 * the SM request If request fails, remove the subscriber.
			 */
			// if(getParamAsBoolean(iRBTConstant.COMMON,
			// iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE"))
			int offerType;
			if (task.containsKey(param_requestFromSelection)) {
				offerType = COMBO_SUB_OFFERTYPE;
			} else {
				offerType = BASE_OFFERTYPE;
			}

			// for putting retailer id in extra info :
			if (task.containsKey(param_retailerID)) {
				userInfoMap.put("RET", task.getString(param_retailerID));
			}

			if (isSupportSMClientModel(task, offerType)) {
				subscriber = smActivateSubscriber(task, userInfoMap,
						subscriber, subscriberID, activatedBy, startDate,
						endtDate, isPrepaid, activationPeriod, freePeriod,
						activationInfo, subscriptionClass, useSubManager, cos,
						isDirectActivation, rbtType, circleID);
			} else {
				if (!task.containsKey(param_requestFromSelection)
						&& task.containsKey(param_offerID))
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
							task.getString(param_offerID));
				else if (task.containsKey(param_requestFromSelection)
						&& task.containsKey(param_subscriptionOfferID))
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
							task.getString(param_subscriptionOfferID));

				// RBT-10258: 50% offer changes.
				boolean isOfferIdNotExists = !(userInfoMap
						.containsKey(iRBTConstant.EXTRA_INFO_OFFER_ID));

				boolean isAllowBaseOffer = RBTParametersUtils
						.getParamAsBoolean(iRBTConstant.COMMON,
								iRBTConstant.ALLOW_GET_OFFER, "false");

				logger.info("Verifying params to hit offer. "
						+ "isAllowBaseOffer: " + isAllowBaseOffer
						+ ", subscriberId: " + subscriberID
						+ ", isOfferIdNotExists: " + isOfferIdNotExists);
				String offerTypeKey = param_selectionInfo + "_"
						+ OFFER_TYPE.toUpperCase();

				if (task.containsKey(offerTypeKey) && subscriptionClass != null) {
					hitOfferAgain = false;
				}
				if (isAllowBaseOffer && isOfferIdNotExists && hitOfferAgain) {
					task.put(param_subscriptionClass, subscriptionClass);
					String offerSubscriptionCls = makeOfferHit(task,
							subscriber, userInfoMap);
					if (offerSubscriptionCls != null) {
						subscriptionClass = offerSubscriptionCls;
					} else if (task.containsKey(param_cosID)) {
						String cosID = task.getString(param_cosID);
						logger.debug("Validating Cos details for the cosID: "
								+ cosID);
						CosDetails cosDetailObj = CacheManagerUtil
								.getCosDetailsCacheManager()
								.getCosDetail(cosID);
						if (cosDetailObj != null
								&& cosDetailObj.getCosType() != null
								&& cosDetailObj.getCosType().equalsIgnoreCase(
										iRBTConstant.SUB_CLASS))
							return BASE_OFFER_NOT_AVAILABLE;
					}
				}
				// RBT-10258: 50% offer changes ends.

				logger.debug("Activating subscriber. SubscriberId: "
						+ subscriberID + ", subscriptionClass: "
						+ subscriptionClass + ", userInfoMap: " + userInfoMap);
				// RBT-9873 Added null for xtraParametersMap for CG flow
				subscriber = rbtDBManager.activateSubscriber(subscriberID,
						activatedBy, startDate, endtDate, isPrepaid,
						activationPeriod, freePeriod, activationInfo,
						subscriptionClass, useSubManager, cos,
						isDirectActivation, rbtType, userInfoMap, circleID,
						task.getString(param_refID), false, null);

				if (rbtDBManager.isTnbReminderEnabled(subscriber))
					rbtDBManager.insertTNBSubscriber(subscriber.subID(),
							subscriber.circleID(),
							subscriber.subscriptionClass(),
							subscriber.startDate(), 0);
			}

			// Activated Subscriber object is storing in taskSession & it will
			// be used to build the response element
			task.put(param_subscriber, subscriber);

			if (!rbtDBManager.isSubscriberDeactivated(subscriber)) {
				response = SUCCESS;
				task.put(param_activatedNow, YES);

				String language = task.getString(param_language);
				if (language != null
						&& (subscriber.language() == null || !subscriber
								.language().equalsIgnoreCase(language))) {
					rbtDBManager.setSubscriberLanguage(subscriberID, language);
					subscriber.setLanguage(language);
				}

				if (task.getString(param_action).equalsIgnoreCase(
						action_acceptGift)) {
					String gifterID = task.getString(param_gifterID);
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");
					Date sentTime = dateFormat.parse(task
							.getString(param_giftSentTime));
					String acceptStatus = "ACCEPT_ACK";

					ViralSMSTable gift = rbtDBManager.getViralPromotion(
							gifterID, subscriberID, sentTime, "GIFTED");

					// Clip ID null means Gift Service Request & only in case of
					// Gift Service request gift entry will be updated
					if (gift.clipID() == null)
						rbtDBManager.updateViralPromotion(gifterID,
								subscriberID, sentTime, "GIFTED", acceptStatus,
								new Date(), null, null);
				}

				// added by Sandeep for profile Pack
				if (task.containsKey(param_packCosId) && cosDetails != null) {

					HashMap<String, String> existingExtraInfoMap = rbtDBManager
							.getExtraInfoMap(subscriber);
					String existingPacks = (existingExtraInfoMap != null) ? existingExtraInfoMap
							.get(iRBTConstant.EXTRA_INFO_PACK) : null;
					String newPack = task.getString(param_packCosId);
					String updatedPacks = (existingPacks != null) ? existingPacks
							+ "," + newPack
							: newPack;
					rbtDBManager.updateExtraInfo(subscriber.subID(),
							iRBTConstant.EXTRA_INFO_PACK, updatedPacks);
					task.remove(param_subscriber);
					task.put(param_subscriber, DataUtils.getSubscriber(task));

					logger.info("going to make proifile pack entry into provisioning_requests table");
					String cosID = (String) task.get(param_packCosId);
					int type = Integer.parseInt(cosID);
					String chargingClass = cosDetails.getSmsKeyword();
					int packStatus = rbtDBManager
							.getPackStatusToInsert(subscriber);
					String mode = getMode(task);
					String modeInfo = getModeInfo(task);
					String transId = UUID.randomUUID().toString();
					ProvisioningRequests provisioningReqs = new ProvisioningRequests(
							subscriber.subID(), type, mode, modeInfo, transId,
							chargingClass, packStatus);
					provisioningReqs.setExtraInfo(packExtraInfoXml);
					rbtDBManager
							.insertProvisioningRequestsTable(provisioningReqs);
				}
			} else
				response = FAILED;

			if (response.equals(SUCCESS)) {
				sendAcknowledgementSMS(task, "ACTIVATION");
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;

	}

	private String makeOfferHit(WebServiceContext task, Subscriber subscriber,
			Map<String, String> userInfoMap) {
		String subscriptionClass = task.getString(param_subscriptionClass);
		logger.info("task object:"+task);
		if (subscriptionClass == null) {
			subscriptionClass = "DEFAULT";
		}
		String subscriberID = task.getString(param_subscriberID);
		try {
			String mode = task.getString(param_mode);
			String userType = (null != subscriber) ? (subscriber.prepaidYes() ? "p"
					: "b")
					: null;
			logger.debug("Making hit to get offers. " + ", subscriberId: "
					+ subscriberID + ", mode: " + mode
					+ ", subscriptionClass: " + subscriptionClass+", userType: "+userType);
			Offer[] offers = RBTSMClientHandler.getInstance().getOffer(
					subscriberID, task.getString(param_mode),
					Offer.OFFER_TYPE_SUBSCRIPTION, userType, subscriptionClass,
					null);
			
			/* RBTSMClientHandler.getInstance()
			.getOffer(subscriberID, mode,
					Offer.OFFER_TYPE_SUBSCRIPTION, userType);*/
			
			logger.info("Got offers for subscriber. offers: " + offers
					+ ", subscriberId: " + subscriberID);
			if (null != offers && 0 < offers.length) {
				Offer offer = offers[0];
				subscriptionClass = offer.getSrvKey();
				String offerId = offer.getOfferID();
				userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offerId);
				logger.debug("Updated subscriptionClass:: " + subscriptionClass
						+ ", userInfoMap: " + userInfoMap);

			}
			logger.info("SrvKey from offers is: " + subscriptionClass
					+ ", subscriberId: " + subscriberID);
		} catch (Exception e) {
			logger.error("Unable to get offers for subscriberId: "
					+ subscriberID + ". Exception: " + e.getMessage(), e);
		}
		return subscriptionClass;

	}

}
