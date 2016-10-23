/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.vodafone;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;
import com.onmobile.reporting.framework.capture.api.ReportingException;

/**
 * @author vinayasimha.patil
 * 
 */
public class VodafoneRBTProcessor extends BasicRBTProcessor {
	private static Logger logger = Logger.getLogger(VodafoneRBTProcessor.class);

	private List<String> songBaseCosIdList = new ArrayList<String>();

	public VodafoneRBTProcessor() {
		String songBaseCosId = getParamAsString(iRBTConstant.COMMON,
				"SONG_BASED_COS_ID", null);
		if (songBaseCosId != null) {
			songBaseCosIdList = Arrays.asList(songBaseCosId.split("\\,"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor
	 * #
	 * getCos(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext
	 * )
	 */
	@Override
	protected CosDetails getCos(WebServiceContext webserviceContext,
			Subscriber subscriber) {
		CosDetails cos = DataUtils.getCos(webserviceContext, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}

	@Override
	public String processActivation(WebServiceContext webserviceContext) {
		try {
			Subscriber subscriber = DataUtils.getSubscriber(webserviceContext);
			String subscriberId = null;
			if (null != subscriber) {
				subscriberId = subscriber.subID();
			}
			String subscriptionClass=null;
			if (subscriber != null){			
				subscriptionClass=subscriber.subscriptionClass();				
			}else {
				if(webserviceContext.getString(param_subscriptionClass)!=null){
					subscriptionClass=webserviceContext.getString(param_subscriptionClass);					
				}
		    }
		
			boolean isCosIdExists = webserviceContext.containsKey(param_cosID);
			boolean isSubNotActive = !(rbtDBManager.isSubActive(subscriber));
			boolean isRentalPack = webserviceContext
					.containsKey(param_rentalPack);
			if(subscriptionClass!=null && isRentalPack){
				String mode = getMode(webserviceContext);
				List<String> modes=ListUtils.convertToList(CacheManagerUtil
						.getParametersCacheManager().getParameterValue(	iRBTConstant.COMMON, "VODAFONE_UPGRADE_CONSENT_MODES",""),",");
				if(modes!=null && modes.contains(mode)){			
					return UPGRADE_NOT_ALLOWED;
				}
			}			
			boolean isReqFromSelection = webserviceContext
					.containsKey(param_requestFromSelection);
			String cosId = webserviceContext.getString(param_cosID);
			logger.info("Processing activation. subscriber: " + subscriberId
					+ ", isSubNotActive: " + isSubNotActive
					+ ", isRentalPack: " + isRentalPack
					+ ", isReqFromSelection: " + isReqFromSelection
					+ ", isCosIdExists: " + isCosIdExists);

			if (isCosIdExists && isSubNotActive
					&& !(isRentalPack || isReqFromSelection)) {
				logger.info("Checking cos for new user. subscriber: "
						+ subscriberId + ", cosId: " + cosId);
				if (isSongBasedCos(cosId)) {
					webserviceContext.put(param_userInfo + "_"
							+ iRBTConstant.UDS_OPTIN, "TRUE");
					return COSID_BLOCKED_FOR_NEW_USER;
				}

			}

			if (isCosIdExists && isRentalPack) {
				logger.info("Checking rental pack user. subscriberId: "
						+ subscriberId + ", cosId: " + cosId);
				if (isSongBasedCos(cosId)) {
					// It should reject the request when existing user on
					// the configured cos when he does not have any default
					// selections
					if (!hasAllCallerSelections(subscriberId)) {
						logger.error("Could not process, subscriber "
								+ "is activated on SONG BASE and does "
								+ "not have all caller selection. "
								+ "subscriberId: " + subscriberId);
						return COSID_BLOCKED_FOR_USER;
					}
				}
			}

			return super.processActivation(webserviceContext);
		} catch (RBTException e) {
			logger.error(e.getMessage(), e);
		}
		logger.info("Returning response : " + ERROR);
		return ERROR;
	}

	private boolean isSongBasedCos(String cosId) {
		if (songBaseCosIdList != null && songBaseCosIdList.contains(cosId)) {
			logger.info("Checking the cos is belong to Song base"
					+ " cosId. cosId: " + cosId + ", songBaseCosIdList: "
					+ songBaseCosIdList);
			if (songBaseCosIdList.contains(cosId)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * 1. Get all active selection 2. declare boolean vairable for
	 * isUserHasAllCallerSelection as false 3. execute the loop till find status
	 * 0 and callerID null or all 4. assign value true to
	 * isUserHasAllCallerSelection and break the loop 5. if
	 * isUserHasAllCallerSelection is false return error (COSID_BLOCKED)
	 * 
	 * @param isRentalPack
	 * @param subscriberId
	 * @return
	 */
	private boolean hasAllCallerSelections(String subscriberId) {
		boolean isUserHasAllCallerSelection = false;
		if (null != subscriberId) {
			SubscriberStatus[] selections = rbtDBManager
					.getAllActiveSubscriberSettings(subscriberId);
			logger.info("Got all subscriber selections. subscriber: "
					+ subscriberId + ", selections: " + selections);
			if (selections != null && selections.length > 0) {
				for (SubscriberStatus subscriberStatus : selections) {
					String callerId = subscriberStatus.callerID();
					int status = subscriberStatus.status();
					boolean isSetForAll = (callerId == null || callerId
							.equalsIgnoreCase("all")) ? true : false;
					logger.info("Checking selection is all caller "
							+ "and status 1. subscriber: " + subscriberId
							+ ", selection refId: " + subscriberStatus.refID()
							+ ", caller: " + subscriberStatus.callerID());
					if (status == 1 && isSetForAll) {
						isUserHasAllCallerSelection = true;
						break;
					}
				}
			}
		}
		logger.error("Verified subscriber "
				+ "has all caller selection. subscriber: " + subscriberId
				+ ", isUserHasAllCallerSelection: "
				+ isUserHasAllCallerSelection);
		return isUserHasAllCallerSelection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor
	 * #processSelection(com.onmobile.apps.ringbacktones.webservice.common.
	 * WebServiceContext)
	 */
	@Override
	public String processSelection(WebServiceContext webserviceContext) {
		try {
			logger.info("WebServiceContext = " + webserviceContext);

			String subscriberID = webserviceContext
					.getString(param_subscriberID);
			Subscriber subscriber = DataUtils.getSubscriber(webserviceContext);
			
	        String subscriptionClass=null;
			HashMap<String, String> subExtraInfoMap = null;
	
			if (subscriber != null){
				subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriber
						.extraInfo());
				subscriptionClass=subscriber.subscriptionClass();				
			}else {
				if(webserviceContext.getString(param_subscriptionClass)!=null){
					subscriptionClass=webserviceContext.getString(param_subscriptionClass);					
				}
			
			//RBT 14835 PPL blocking content cosid :: support for combo selection request
				String udsValFromRequest = webserviceContext.getString(param_userInfo+"_UDS_OPTIN");
				if(udsValFromRequest!=null){
					subExtraInfoMap = new HashMap<String, String>();
					subExtraInfoMap.put("UDS_OPTIN",udsValFromRequest);
				}
		    }
			boolean isUDSUser = false;
			// Change in logic for UDS User Validation will happen it by the
			// UDS_OPTIN in extra info column If the UDS_OPTIN present in the
			// subscriber extra info column and the key value is either true or
			// the configured charge class then he is a UDS user and the
			// corresponding charge class will be passed & update in the
			// Subscriber table.//JIRA-ID: RBT-13626:
			String premiumChargeClass = null;
			if(subscriptionClass!=null && isTNBuser(subscriptionClass) ){
				String mode = getMode(webserviceContext);
				List<String> modes=ListUtils.convertToList(CacheManagerUtil
						.getParametersCacheManager().getParameterValue(	iRBTConstant.COMMON, "VODAFONE_UPGRADE_CONSENT_MODES",""),",");
				if(modes!=null && modes.contains(mode)){
				
					return TNB_SONG_SELECTON_NOT_ALLOWED;
				}
			}	
			
			//RBT-14022
			Map<String,String> selectionInfoMap = getSelectionInfoMap(webserviceContext);
//			if (rbtDBManager.isSubActive(subscriber)) {
				premiumChargeClass = Utility.isUDSUser(subExtraInfoMap,false,selectionInfoMap);
				isUDSUser = (premiumChargeClass != null);
//			}
			
			String blockedContentTypesStr = CacheManagerUtil
					.getParametersCacheManager().getParameterValue(
							iRBTConstant.COMMON, "UDS_BLOCKED_CONTENT_TYPES",
							"");
			if (blockedContentTypesStr != null) {
				List<String> blockedContentTypesList = Arrays
						.asList(blockedContentTypesStr.split(","));
				Clip clip = getClip(webserviceContext);

				boolean allowPremiumContent = webserviceContext
						.getString(param_allowPremiumContent) != null
						&& webserviceContext.getString(
								param_allowPremiumContent)
								.equalsIgnoreCase(YES);

				if (isUDSUser
						&& clip != null
						&& blockedContentTypesList.contains(clip
								.getContentType())) {
			
					// RBT-14835 Blocking PPL content for specific service
					String udsTypeStr = Utility.getUdsType(subExtraInfoMap,
							selectionInfoMap);
					List<String> currentContentType = new ArrayList<String>();
					currentContentType.add(clip.getContentType());

					if (rbtDBManager.isContentTypeBlockedForCosIdorUdsType(
							udsTypeStr, currentContentType)) {
						logger.info("Content type ::" + currentContentType
								+ " is blocked for udsType:: " + udsTypeStr);
						logger.info("VodafoneRBTProcessor :: processSelection() response :"
								+ LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED);
						return LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED;
					}
					if (!allowPremiumContent) {
						if (RBTParametersUtils.getParamAsBoolean(
								iRBTConstant.COMMON,
								"IS_PREMIUM_CONTENT_ALLOWED_FOR_UDS_USER", "FALSE")) {
							webserviceContext.put(param_info, VIRAL_DATA);
							webserviceContext.put(param_type, "SELCONFPENDING");
							webserviceContext.put(param_info + "_CATEGORY_ID",
									webserviceContext.getString(param_categoryID));
							if (!RBTParametersUtils.getParamAsBoolean(
									iRBTConstant.COMMON,
									"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED",
									"FALSE")) {
								removeData(webserviceContext);
							}

							String interval = webserviceContext.getString(param_interval);
							String inLoop = webserviceContext.getString(param_inLoop);
							String categoryId = webserviceContext.getString(param_categoryID);
							String clipId = webserviceContext.getString(param_clipID);
							String fromTime = webserviceContext.getString(param_fromTime);
							String toTime = webserviceContext.getString(param_toTime);
							String status = webserviceContext.getString(param_status);
							int currStatus = 1;
							int currFromTime = 0;
							int currToTime = 2359;
							boolean currInLoop = false;
							String wavFile = null;
							Category currCategory = null;
							Categories categoriesObj = null;
							if (inLoop != null) {
								currInLoop = true;
							}
							if (fromTime != null) {
								currFromTime = Integer.parseInt(fromTime);
							}
							if (toTime != null) {
								currToTime = Integer.parseInt(toTime);
							}
							if (status != null) {
								currStatus = Integer.parseInt(status);
							}
							Clip clip2 = rbtCacheManager.getClip(clipId);
							if (clip2 != null) {
								wavFile = clip2.getClipRbtWavFile();
							}
							categoryId = categoryId != null ? categoryId : "3";
							currCategory = rbtCacheManager.getCategory(Integer.parseInt(categoryId));
							if (currCategory != null) {
								categoriesObj = CategoriesImpl.getCategory(currCategory);
							}
							Connection connection = RBTDBManager.getInstance().getConnection();
							String callerID = webserviceContext.getString(param_callerID);
							SubscriberStatus[] subscriberSelections = RBTDBManager.getInstance()
									.getAllActiveSubscriberSettings(subscriberID);

							SubscriberStatus availableSelection = RBTDBManager.getInstance()
									.getAvailableSelection(connection, subscriberID, callerID,
											subscriberSelections, categoriesObj, wavFile, currStatus,
											currFromTime, currToTime, null, null, true, currInLoop, 0,
											interval, null);
							if (availableSelection != null) {
								logger.info("VodafoneRBTProcessor :: processSelection() response :"
										+ ALREADY_EXISTS);
								return ALREADY_EXISTS;
							}

							String modeInfo = webserviceContext
									.getString(param_modeInfo);
							if (modeInfo != null) {
								webserviceContext.put(param_info + "_SEL_INFO",
										modeInfo);
							}
							String subscriptionCls = webserviceContext
									.getString(param_subscriptionClass);
							if (subscriptionCls != null) {
								webserviceContext.put(param_info + "_SUBSCRIPTION_CLASS",
										subscriptionCls);
							}
							String cosID = webserviceContext
									.getString(param_cosID);
							if (cosID != null) {
								webserviceContext.put(param_info + "_COS_ID",
										cosID);
							}
							
							String udsType = selectionInfoMap.get("UDS_OPTIN");
							if (udsType != null) {
								webserviceContext.put(param_info + "_UDS_OPTIN",
										udsType);
							}
							String response = addData(webserviceContext);
							//RBT-12982
							if (response.equalsIgnoreCase(SUCCESS)) {
								ViralSMSTable[] viralSMSTables = getLatestSelConfPendingViralSMSes(subscriberID);
								if (viralSMSTables != null && viralSMSTables.length > 0) {
									webserviceContext.put(VIRAL_SMS_TABLE_ARRAY, viralSMSTables);
								}
							}
						}
						
						Category category = null;
						if (webserviceContext.getString(param_categoryID) != null) {
							category = rbtCacheManager.getCategory(Integer
									.parseInt(webserviceContext
											.getString(param_categoryID)));
						}

						if (pplContentRejectionLogger != null) {
							if (category != null
									&& Utility.isShuffleCategory(category
											.getCategoryTpe()))
								pplContentRejectionLogger
										.PPLContentRejectionTransaction(
												subscriberID,
												getMode(webserviceContext), "-1",
												category.getCategoryId() + "",
												new Date());
							else
								pplContentRejectionLogger
										.PPLContentRejectionTransaction(
												subscriberID,
												getMode(webserviceContext),
												clip.getClipId() + "", "-1",
												new Date());
						}

						logger.info("Clip's content type is UDS Blocked ContentType, so returning failure");
						return LITE_USER_PREMIUM_BLOCKED;
					} else {		//Reaches here if allowPremiumContent=true
						String callerId = webserviceContext.getString(param_callerID);
						String clipId = webserviceContext.getString(param_clipID);
						
						String classType = null;
						//Change in logic for UDS User
						//JIRA-ID: RBT-13626:
						if (premiumChargeClass != null
								&& !premiumChargeClass.equalsIgnoreCase("NULL")) {
							classType = premiumChargeClass;
						} else if (RBTParametersUtils.getParamAsBoolean(
								iRBTConstant.COMMON,
								PREMIUM_SELECTION_IS_CHARGE_CLASS_FROM_CLIP,
								"FALSE")) {
							classType = clip.getClassType();
						}
						
						if (classType != null) {
							webserviceContext.put(param_chargeClass, classType);
							webserviceContext.put(param_useUIChargeClass, YES);
						}
					
						ViralSMSTable vstForProcessing = null;
						String allowDirectPremiumSel = webserviceContext.getString(WebServiceConstants.param_allowDirectPremiumSelection);
						logger.debug("subscriberId: " + subscriberID + ", allowDirectPremiumSel: " + allowDirectPremiumSel);
	
						//If allowDirectPremiumSel is not null or is not set to true, viral sms table entry has to be there.
						if (allowDirectPremiumSel == null || !allowDirectPremiumSel.equalsIgnoreCase(iRBTConstant.YES)) {
							logger.debug("subscriberId: " + subscriberID + ": About to check for viral sms table entries.");
							ViralSMSTable[] ViralSMSTables = getLatestSelConfPendingViralSMSes(subscriberID);
							if (ViralSMSTables != null && ViralSMSTables.length > 0) { 
								if (!RBTParametersUtils.getParamAsBoolean(
										iRBTConstant.COMMON,
										"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED",
										"FALSE")) {
									vstForProcessing = ViralSMSTables[0];
								} else {
									for (ViralSMSTable viralSMSTable : ViralSMSTables) {
										if (clipId != null && clipId.equals(viralSMSTable.clipID()) 
												&& areCallerIdsMatching(callerId, viralSMSTable.callerID())) {
											vstForProcessing = viralSMSTable;
											break;
										}
									}
								}
							}
							if (vstForProcessing == null) {
								logger.info("subscriberId: " + subscriberID + ". Viral SMS table entry not found. Returning " + ERROR);
								return ERROR;
							}
						}
						String response = super.processSelection(webserviceContext);
						if (vstForProcessing != null && response.equalsIgnoreCase(SUCCESS)) {
							boolean isRemoved = rbtDBManager.deleteViralPromotionBySMSID(vstForProcessing.getSmsId());
							logger.debug("viralSMS entry: " + vstForProcessing + ", isRemoved: " + isRemoved);
						}
						logger.debug("subscriberId: " + subscriberID + ", response: " + response);
						return response;
					}
				}
			}

			return super.processSelection(webserviceContext);
		} catch (RBTException e) {
			logger.error(e.getMessage(), e);
		} catch (ReportingException e1) {
			logger.error(e1.getMessage(), e1);
		}
		logger.info("Returning response : " + ERROR);
		return ERROR;
	}

	private boolean areCallerIdsMatching(String callerId, String vstCallerId) {
		if (callerId == null) {
			return vstCallerId == null || vstCallerId.equalsIgnoreCase("all"); 
		}
		if (callerId.equalsIgnoreCase("all")) {
			return vstCallerId == null || vstCallerId.equalsIgnoreCase("all");
		}
		return callerId.equalsIgnoreCase(vstCallerId);
	}

	private ViralSMSTable[] getLatestSelConfPendingViralSMSes(
			String subscriberID) {
		int duration = RBTParametersUtils.getParamAsInt(
				iRBTConstant.COMMON, "SEL_WAIT_TIME_DOUBLE_CONFIRMATION",
				30);

		ViralSMSTable[] viralSMSTables = rbtDBManager
				.getLatestViralSMSesByTypeSubscriberAndTime(subscriberID,
						"SELCONFPENDING", duration);
		return viralSMSTables;
	}
	
	private boolean isTNBuser(String subscriptionClass){
		List<String> tnbSubscriptionClasses=ListUtils.convertToList(CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON","TNB_SUBSCRIPTION_CLASSES","ZERO"),",");
		boolean isTnBUser=false;
		if(!tnbSubscriptionClasses.isEmpty() && tnbSubscriptionClasses.contains(subscriptionClass)){
			isTnBUser=true;
		}
		return isTnBUser;
	}
	
	
}
