package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.daemons.implementation.VodaRomaniaPlayerImpl;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class RomaniaDBMgrImpl extends RBTDBManager{

	private static Logger logger = Logger.getLogger(RomaniaDBMgrImpl.class);
	
	public static List<String> m_DEFAULT_DOWNLOAD = null;
	public static int m_MAX_DOWNLOADS_ALLOWED = 15;
	public static int m_MAX_GROUPS_ALLOWED = 10;
	public static int m_MAX_CALLER_ALLOWED_IN_GROUPS = 10;
	private static String m_success = "SUCCESS";
	private static String m_failure = "FAILURE";
	
	private static String entryDN = null;
	private static Hashtable<String, String> ldapEnvironment;
	private static String[] countryPrefixes;

	VodaRomaniaPlayerImpl _vodaPlayerImpl = null;

	@Override
	public void init(){

		logger.info("inside init method");
		
		ParametersCacheManager parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		Parameters parameter = parametersCacheManager.getParameter("COMMON","MAX_DOWNLOADS_ALLOWED", "15");
		m_MAX_DOWNLOADS_ALLOWED = Integer.parseInt(parameter.getValue());

		parameter = parametersCacheManager.getParameter("COMMON","DEFAULT_DOWNLOAD");
		if (parameter != null && parameter.getValue() != null)
			m_DEFAULT_DOWNLOAD = Arrays.asList(parameter.getValue().split(","));

		parameter = parametersCacheManager.getParameter("COMMON","MAX_GROUPS_ALLOWED", "10");
		m_MAX_GROUPS_ALLOWED = Integer.parseInt(parameter.getValue());

		parameter = parametersCacheManager.getParameter("COMMON","MAX_CALLER_ALLOWED_IN_GROUPS", "10");
		m_MAX_CALLER_ALLOWED_IN_GROUPS = Integer.parseInt(parameter.getValue());
	}

	@Override
	public String canBeGifted(String subscriberId,String callerId,String contentID)
	{
		String method = "canBeGifted";
		logger.info("inside "+method);
		String canBeGifted = null;
		subscriberId = subID(subscriberId);
		callerId = subID(callerId);
		Subscriber sub = getSubscriber(subscriberId);
		if(!isSubscriberActivated(sub))
			return GIFT_FAILURE_GIFTER_NOT_ACT;
		
		HashMap<String, String> callerInfo = getSubscriberInfo(callerId);
		if(!callerInfo.get("STATUS").equalsIgnoreCase("VALID"))
			return GIFT_FAILURE_GIFTEE_INVALID;

		Subscriber caller = getSubscriber(callerId);
		if(isSubscriberActivationPending(caller))
			return GIFT_FAILURE_ACT_PENDING;
		if(isSubscriberDeactivationPending(caller))
			return GIFT_FAILURE_DEACT_PENDING;
		if (caller != null
				&& (caller.subYes().equals(STATE_ACTIVATION_GRACE)
						|| caller.subYes().equals(STATE_SUSPENDED_INIT)
						|| caller.subYes().equals(STATE_SUSPENDED)))
			return GIFT_FAILURE_TECHNICAL_DIFFICULTIES;

		if(contentID == null && serviceGiftisSongGiftInUse(caller))
			return GIFT_FAILURE_GIFT_IN_USE;


		ViralSMSTable[] vst = getViralSMSByCaller(callerId);

		if(vst != null)
		{

			for(int i = 0; i < vst.length; i++)
			{
				if(contentID == null)
				{
					if(serviceGiftIsGiftPending(vst[i], caller))
						return GIFT_FAILURE_ACT_GIFT_PENDING;

					if(!isSubscriberDeactivated(caller) && serviceGiftisServiceGiftInUse(vst[i], caller))
						return GIFT_FAILURE_GIFT_IN_USE;
				}
				else
				{
					if(songGiftIsGiftPending(contentID, vst[i], caller))
						return GIFT_FAILURE_SONG_GIFT_PENDING;

					if(isSubscriberDeactivated(caller) && serviceGiftIsGiftPending(vst[i], caller))
						return GIFT_FAILURE_ACT_GIFT_PENDING;
				}
			}
		}


		boolean isClip = true;
		ClipMinimal cMin = null;
		String clipName = null;
		int catID = -1;
		if(contentID != null && !contentID.equals("null"))
		{
			if(contentID.startsWith("C"))
			{
				isClip = false;
				contentID = contentID.substring(1);
				catID = Integer.parseInt(contentID);
			}
			else
			{
				int clipId = Integer.parseInt(contentID);
				cMin = super.getClipMinimal(clipId, true);
				clipName = cMin.getWavFile();
			}	

		}

		if(contentID !=null && !contentID.equals("null") && !isSubscriberDeactivated(caller))
		{
			SubscriberDownloads[] subDownloads = getSubscriberDownloads(callerId);
			if(subDownloads != null)
			{
				for(int i = 0; i < subDownloads.length; i++)
				{
					char downloadStatus = subDownloads[i].downloadStatus();
					if((isClip && subDownloads[i].promoId().equals(clipName)) || subDownloads[i].categoryID() == catID)
					{
						if(downloadStatus == 'n' || downloadStatus == 'p' || downloadStatus == 'y' || downloadStatus == 'd' || downloadStatus == 's' || downloadStatus == 'e' || downloadStatus == 'f')
						{
							return GIFT_FAILURE_SONG_PRESENT_IN_DOWNLOADS;
						}
					}
				}
			}
		}

		if(isSubscriberDeactivated(caller))
			canBeGifted = GIFT_SUCCESS_GIFTEE_NEW_USER;
		else
			canBeGifted = GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE;
		return canBeGifted;
	}

	public boolean songGiftIsGiftPending(String contentID, ViralSMSTable vst, Subscriber caller)
	{
		if(vst != null)
		{
			String clipID = vst.clipID();
			String type = vst.type();
			if(clipID != null && clipID.equals(contentID))
			{
				if(type.equals(GIFT) || type.equals(GIFTCHRGPENDING) || type.equals(GIFT_CHARGED) || type.equals(GIFTED))
					return true;
			}
		}
		return false;
	}

	public boolean serviceGiftIsGiftPending(ViralSMSTable vst, Subscriber caller)
	{
		if(vst != null)
		{
			String type = vst.type();
			if(type.equals(GIFT) || type.equals(GIFTCHRGPENDING) || type.equals(GIFT_CHARGED) ||type.equals(GIFTED))
			{
				if(isSubscriberDeactivated(caller) || (isSubscriberActivated(caller) && (vst.clipID()== null || vst.clipID().equals("null"))))
					return true;
			}
		}

		return false;
	}

	public boolean serviceGiftisSongGiftInUse(Subscriber caller)
	{
		if(!isSubscriberDeactivated(caller) && caller.activatedBy().equalsIgnoreCase("GIFT"))
		{
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH, -30);
			Date compareDate = calendar.getTime();
			if(caller.startDate().after(compareDate))
				return true;
		}
		return false;
	}

	public boolean serviceGiftisServiceGiftInUse(ViralSMSTable vst, Subscriber caller)
	{
		if(vst != null)
		{
			String type = vst.type();
			if((type.equals(ACCEPT_ACK) || type.equals(ACCEPTED) || type.equals(ACCEPT_PRE)) && vst.clipID()==null)
			{
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_MONTH,-30);
				Date compareDate = calendar.getTime();
				if(vst.sentTime().after(compareDate))
					return true;
			}
		}

		return false;
	}




	@Override
	public boolean isDownloadAllowed(String subscriberId, WebServiceContext task){

		String method = "isDownloadAllowed";
		logger.info("inside "+method);
		SubscriberDownloads[] subDownloads = getNonDeactiveSubscriberDownloads(subscriberId);
		if(subDownloads==null || subDownloads.length<m_MAX_DOWNLOADS_ALLOWED)
			return true;
		else
			return false;
	}


	@Override
	public String smSubscriptionSuccess(String subscriberID, Date nextChargingDate,Date activationDate, 
			String type, String classType, boolean isPeriodic,String finalActInfo, boolean updateEndtime, 
			boolean updatePlayStatus, String extraInfo, String upgradingCosID, int validity, String subscriptionYes, String strNextBillingDate) {

		subscriberID = subID(subscriberID);
		// Added extraInfo - TRAI changes
		String ret = super.smSubscriptionSuccess(subscriberID,nextChargingDate,activationDate, 
				type,classType,isPeriodic,finalActInfo,updateEndtime,updatePlayStatus, extraInfo, upgradingCosID, validity, subscriptionYes, strNextBillingDate);

		com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = RBTCacheManager.getInstance().getCategory(2);
		
		Categories categoriesObj = CategoriesImpl.getCategory(category);
		
		if(ret.equals(m_success) && m_DEFAULT_DOWNLOAD != null && m_DEFAULT_DOWNLOAD.size() > 0)
		{
			for(int i = 0; i < m_DEFAULT_DOWNLOAD.size() ; i++)
				addSubscriberDownloadRW(subscriberID,(String)m_DEFAULT_DOWNLOAD.get(i),categoriesObj,null,true,"FREE","SYS",null, null, true, false, false, null, null);
			updateAllGroupsStatusForSubscriber(subscriberID, "A", "W");
		}


		return ret; 
	}

	@Override
	public String addSubscriberSelections(String subscriberID, String callerID,
			Categories categories, HashMap clipMap, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, int freePeriod, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime,
			int toTime, String chargeClassType, boolean smActivation,
			boolean doTODCheck, String mode, String regexType, String subYes,
			String promoType, String circleID, boolean incrSelCount,
			boolean useDate, String transID, boolean OptIn, boolean isTata,
			boolean inLoop, String subClass, Subscriber sub, int rbtType,
			String selInterval, HashMap extraInfo, boolean useUIChargeClass, String refID, boolean isDirectActivation)
	{

		String method = "addSubscriberSelections";
		Connection conn = getConnection();
		if (conn == null)
			return null;
		
		int count = 0;
		Date nextChargingDate = null;
		Date startDate = startTime;
		String selectInfo = selectionInfo;
		String sel_status = STATE_BASE_ACTIVATION_PENDING;
		int nextPlus = -1;
		String addResult = null;
		boolean updateEndDate = false;
		try {
			subscriberID = subID(subscriberID);
			callerID = subID(callerID);
			if(subscriberID != null && callerID != null && subscriberID.equals(callerID))
				return SELECTION_FAILED_OWN_NUMBER; 
				
			if(selInterval != null && selInterval.indexOf(",") != -1)
			{
				List days = new ArrayList();
				StringTokenizer stk = new StringTokenizer(selInterval, ",");
				while(stk.hasMoreTokens())
					days.add(stk.nextToken());
				
				if(days.size() == 7)
				{
					selInterval = null;
				}
				else
				{	Collections.sort(days);
					selInterval = "";
					for(int i=0;i<days.size();i++)
					{
						selInterval = selInterval + days.get(i);
						if(i != days.size() - 1)
							selInterval = selInterval + ",";
					}
				}
			}
			
			if (sub != null && rbtType != 2) {
				rbtType = sub.rbtType();
			}
			if (sub != null && sub.subYes() != null
					&& (sub.subYes().equals("Z") || sub.subYes().equals("z"))) {
				logger.info(subscriberID + " is suspended. Returning false.");
				return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
			}
			boolean isSelSuspended = false;
			if (m_checkForSuspendedSelection) {
				isSelSuspended = isSelSuspended(subscriberID, callerID);
			}
			if (isSelSuspended) {
				logger.info("selection of " + subscriberID + " for " + callerID + " is suspended. Returning false.");
				return SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED;
			}
			
			if (callerID != null)
			{
				Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn, subscriberID);
				if (groups != null)
				{
					for (Groups group : groups)
					{
						GroupMembers[] groupMembers = GroupMembersImpl.getMembersForGroupID(conn, group.groupID());
						if (groupMembers != null)
						{
							for (GroupMembers groupMember : groupMembers)
							{
								if (groupMember.callerID().equalsIgnoreCase(callerID))
									return SELECTION_FAILED_CALLER_ALREADY_IN_GROUP;
							}
						}
					}
				}
			}
			
			/*
			 * if(freePeriod != 0) { nextChargingDate =
			 * Calendar.getInstance().getTime(); selectInfo = "free:" +
			 * selectInfo; }
			 */
			Date endDate = endTime;
			if (endDate == null)
				endDate = m_endDate;

			// If chargeClassType is null, then useUIChargeClass parameter will be ignored
			useUIChargeClass = useUIChargeClass && chargeClassType != null;
			
			String classType = "DEFAULT";
			if (useUIChargeClass)
				classType = chargeClassType;
			else if (categories != null)
				classType = categories.classType();

			// Clips clips = ClipsImpl.getClipRBT(conn, subscriberWavFile);
			Date clipEndTime = null;
			String clipGrammar = null;
			String clipClassType = null;
			String subscriberWavFile = null;
			if (clipMap != null) {
				if (clipMap.containsKey("CLIP_CLASS"))
					clipClassType = (String) clipMap.get("CLIP_CLASS");
				if (clipMap.containsKey("CLIP_END"))
					clipEndTime = (Date) clipMap.get("CLIP_END");
				if (clipMap.containsKey("CLIP_GRAMMAR"))
					clipGrammar = (String) clipMap.get("CLIP_GRAMMAR");
				if (clipMap.containsKey("CLIP_WAV"))
					subscriberWavFile = (String) clipMap.get("CLIP_WAV");
			}

			if (subscriberWavFile == null) {
				if (status != 90)
					return SELECTION_FAILED_NULL_WAV_FILE;

				subscriberWavFile = "CRICKET";
			}

			if (subYes != null
					&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
							.equalsIgnoreCase(STATE_EVENT)))
			{
				if (!isPackActivationPendingForContent(sub, categories, subscriberWavFile, status, callerID))
					sel_status = STATE_TO_BE_ACTIVATED;
			}

			if (subClass != null && m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(subClass)) {
				chargeClassType = (String) m_subOnlyChargeClass.get(subClass);
				updateEndDate = true;
			}
			if (clipEndTime != null) {
				
				/*
				 * if (freePeriod == 0 && status != 99 && clipEndTime != null) {
				 * endDate = clipEndTime; }
				 */
				if (categories != null && (categories.type() == DAILY_SHUFFLE
						|| categories.type() == MONTHLY_SHUFFLE))
				{
					endDate = categories.endTime();
					status = 79;
				}

				/*
				 * if (clipGrammar != null &&
				 * clipGrammar.equalsIgnoreCase("UGC")) if (selectInfo == null)
				 * selectInfo = "UGC"; else selectInfo += ":UGC";
				 */
				if (!useUIChargeClass && clipClassType != null
						&& !clipClassType.equalsIgnoreCase("DEFAULT")
						&& classType != null
						&& !clipClassType.equalsIgnoreCase(classType))
				{
					ChargeClass catCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
					ChargeClass clipCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(clipClassType);

					if (catCharge != null && clipCharge != null && catCharge.getAmount() != null
							&& clipCharge.getAmount() != null) {
						try {
							float firstAmount = Float.parseFloat(catCharge.getAmount());
							float secondAmount = Float.parseFloat(clipCharge.getAmount());

							if ((firstAmount < secondAmount)
									|| (m_overrideChargeClasses != null && m_overrideChargeClasses
											.contains(clipClassType.toLowerCase())))
								classType = clipClassType;
						}
						catch (Throwable e) {
						}
					}
					if (clipClassType.startsWith("TRIAL") && categories != null
							&& categories.id() != 26)
						classType = clipClassType;
				}
			}

			if (!useUIChargeClass &&chargeClassType != null) {
				ChargeClass first = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
				ChargeClass second = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClassType);

				if (first != null && second != null && first.getAmount() != null
						&& second.getAmount() != null) {
					try {
						float firstAmount = Float.parseFloat(first.getAmount());
						float secondAmount = Float.parseFloat(second.getAmount());

						if (firstAmount <= secondAmount
								|| secondAmount == 0
								|| chargeClassType.equalsIgnoreCase("YOUTHCARD")
								|| chargeClassType.equalsIgnoreCase("DEFAULT_10")
								|| (m_overrideChargeClasses != null && m_overrideChargeClasses
										.contains(chargeClassType.toLowerCase())))
							classType = chargeClassType;
					}
					catch (Throwable e) {
						classType = chargeClassType;
					}
				}
				else {
					classType = chargeClassType;
				}

				if (first != null && first.getChargeClass().startsWith("TRIAL") && categories != null
						&& categories.id() != 26) {
					classType = first.getChargeClass();
				}
			}

			if (!useUIChargeClass && categories != null
					&& categories.type() == 10 && m_modeChargeClass != null
					&& m_modeChargeClass.containsKey(selectedBy))
			{
				classType = (String) m_modeChargeClass.get(selectedBy);
			}

			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
						subID(subscriberID), null);
				if (viralSMS != null) {
					selectInfo = selectInfo + ":" + "viral";
				}
			}

			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			// int count = 0;

			String afterTrialClassType = "DEFAULT";
			if (OptIn)
				afterTrialClassType = "DEFAULT_OPTIN";

				/**
				 *  If user enabled UDS , then all his selections should go in Loop
				 */
				if(!inLoop) 
				{	
					HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub.extraInfo());
					if(subExtraInfoMap!= null && subExtraInfoMap.containsKey(UDS_OPTIN))
					{
						inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN)).equalsIgnoreCase("TRUE");
					}
				}

				if (selInterval != null && status != 80) {

					if (selInterval.startsWith("W") || selInterval.startsWith("M")) {

						status = 75;
					}

					if (selInterval.startsWith("Y")) {

						status = 95;
						String date = selInterval.substring(1);
						Date parseDate = null;
						if (date.length() == 8) {

							SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
							Date currentDate = new Date();
							parseDate = dateFormat.parse(date);
							if (parseDate.before(currentDate) || parseDate.equals(currentDate)) {
								return SELECTION_FAILED_INVALID_PARAMETER;
							}
							Calendar cal = Calendar.getInstance();
							cal.setTime(parseDate);
							// parseDate.setDate(parseDate.getDate()+1);
							cal.add(Calendar.DAY_OF_YEAR, 1);
							endDate = cal.getTime();
						}

						if (date.length() == 4) {

							endDate = m_endDate;
						}
					}
				}

			// Added for checking the selection limit
				/**
				 * Since Sprint 4 RBT 2.0, RBT 15670
				 * One more parameter udpId has been added in
				 * getSubscriberSelections method. If udpId is present then 
				 * query will filter it with udpId also otherwise old flow.
				 */
				String udpId = null;
				/*if(extraInfo.containsKey(WebServiceConstants.param_udpId))
					udpId = (String) extraInfo.get(UDP_ID);*/
			SubscriberStatus[] subscriberSelections = SubscriberStatusImpl.getSubscriberSelections(conn, subID(subscriberID), subID(callerID), rbtType, udpId);
							
			/* time of the day changes */
			SubscriberStatus subscriberStatus = null;
			
			subscriberStatus = getAvailableSelection(conn, subID(subscriberID), subID(callerID),subscriberSelections,
					categories, subscriberWavFile, status, fromTime, toTime, startDate, endDate, doTODCheck, inLoop,
					rbtType, selInterval, selectedBy);
			if (subscriberStatus == null) {
				logger.info("RBT::no matches found");
				// System.out.println("111111111111111111");
				if (inLoop
						&& (categories.type() == SHUFFLE || status == 90 || status == 99 || status == 0))
					inLoop = false;

				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;
				
				subscriberStatus = SubscriberStatusImpl.smSubscriberSelections(conn,
						subID(subscriberID), subID(callerID), status, rbtType);
				if (subscriberStatus != null) {
					if (inLoop && subscriberStatus.categoryType() == SHUFFLE)
						inLoop = false;
				}
//				else
//					// this else will make all first callerID selection as
//					// override :), not needed actually
//					inLoop = false;
				
				/**
				 * @added by sreekar if user's last selection is a trail
				 *        selection his next selection should override the old
				 *        one
				 */
				char loopStatus = getLoopStatusForNewSelection(inLoop, subscriberID, isPrepaid);

				String actBy = null;
				if (sub != null) {
					actBy = sub.activatedBy();
					// oldSubClass = sub.oldClassType();
				}
				if (m_trialChangeSubTypeOnSelection && actBy != null && actBy.equals("TNB")
						&& (subClass != null && subClass.equals("ZERO"))) {
					if (classType != null && classType.equals("FREE")) {
						sel_status = STATE_BASE_ACTIVATION_PENDING;

						if (!convertSubscriptionTypeTrial(subID(subscriberID), subClass, "DEFAULT", sub))
							return SELECTION_FAILED_TNB_TO_DEFAULT_FAILED;
					}
				}
				
				if (!useUIChargeClass)
				{
					if(status == 80 && rbtType == 2)
					{
						classType = clipClassType;
					}
					else
					{
						for(int i=0;subscriberSelections != null && i<subscriberSelections.length;i++)
						{
							if(subscriberSelections[i].selType() == 2)
							{
								HashMap selectionExtraInfo = DBUtility.getAttributeMapFromXML(subscriberSelections[i].extraInfo());
								int campaignId = -1;

								if(selectionExtraInfo != null && selectionExtraInfo.containsKey(iRBTConstant.CAMPAIGN_ID) && selectionExtraInfo.get(iRBTConstant.CAMPAIGN_ID) != null)
								{

									try
									{
										campaignId = Integer.parseInt(""+selectionExtraInfo.get(iRBTConstant.CAMPAIGN_ID));
									}
									catch(Exception e)
									{
										campaignId = -1;
									}
								}
								logger.info("The value of campaign id - "+campaignId);
								if(campaignId != -1)
								{
									RBTBulkUploadTask bulkUploadTask = RBTBulkUploadTaskDAO.getRBTBulkUploadTask(campaignId);

									if(m_corporateDiscountChargeClass != null && 
											m_corporateDiscountChargeClass.containsKey(bulkUploadTask.getTaskMode()))
									{
										logger.info("The value of m_corporateDiscountChargeClass id - "+m_corporateDiscountChargeClass.toString());
										HashMap discountClassMap = (HashMap) m_corporateDiscountChargeClass.get(bulkUploadTask.getTaskMode());
										if(discountClassMap != null && discountClassMap.containsKey(classType))
											classType = (String) discountClassMap.get(classType);
									}
								}
								break;
							}	

						}
					}
				}

				boolean isPackSel = false;
				String packCosID = null;
				if ((status == 1 || status == 75 || status == 79 || status == 80 || status == 92 || status == 93 || status == 95) && rbtType != 2)
				{
					sel_status = STATE_BASE_ACTIVATION_PENDING;

					boolean isSubActive = false;
					if (subYes != null
							&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
									.equalsIgnoreCase(STATE_EVENT)))
					{
						if (!isPackActivationPendingForContent(sub, categories, subscriberWavFile, status, callerID))
							isSubActive = true;
					}

					addResult = addSubscriberDownloadRW(subscriberID, subscriberWavFile,
							categories, null, isSubActive, classType, selectedBy,
							selectInfo, extraInfo, incrSelCount, useUIChargeClass, false, null, null);
					if (addResult.indexOf("SUCCESS") == -1)
						return addResult;
					else if (addResult.indexOf("SUCCESS:DOWNLOAD_ALREADY_ACTIVE") != -1)
						sel_status = STATE_TO_BE_ACTIVATED;
					else if(addResult.indexOf("SUCCESS:DOWNLOAD_GRACE") != -1)
						sel_status = STATE_BASE_ACTIVATION_PENDING;
					
					classType = "SETTING";
				}
				else
				{
					String subPacks = null;
					HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub.extraInfo());
					if (subExtraInfoMap != null && subExtraInfoMap.containsKey(EXTRA_INFO_PACK))
						subPacks = subExtraInfoMap.get(EXTRA_INFO_PACK);

					String nextClass = null;
					if (subPacks != null)
					{
						com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
								.getInstance().getCategory(categories.id());
						Clip clipObj = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
								.getInstance().getClipByRbtWavFileName(
										subscriberWavFile);
						CosDetails cosDetail = getCosDetailsForContent(
								subscriberID, subPacks, category, clipObj, status, callerID);
						List<ProvisioningRequests> packList = null;
						if (cosDetail != null)
						{
							packList = ProvisioningRequestsDao
								.getBySubscriberIDTypeAndNonDeactivatedStatus(
										subscriberID, Integer
												.parseInt(cosDetail.getCosId()));
						}
						if (packList != null
								&& (isSubscriberPackActivated(packList
										.get(0)) || isSubscriberPackActivationPending(packList
										.get(0))))
						{
							int selCount = sub.maxSelections();
							if (isPackRequest(cosDetail))
							{
								selCount = packList.get(0).getNumMaxSelections();
								if (cosDetail.getFreeSongs() > selCount)
									isPackSel = true;
							}

							nextClass = getChargeClassFromCos(cosDetail, selCount);
							packCosID = cosDetail.getCosId();
						}
						else
						{
							nextClass = getNextChargeClass(sub);
						}
					}
					else
					{
						nextClass = getNextChargeClass(sub);
					}

					if (nextClass != null && !nextClass.equalsIgnoreCase("DEFAULT"))
						classType = nextClass;
				}

				String checkSelStatus = checkSelectionLimit(subscriberSelections, subID(callerID), inLoop);
				if(!checkSelStatus.equalsIgnoreCase("SUCCESS"))
					return checkSelStatus;

				//Added the grace selection deact mode for JIRA-RBT-6338
				String graceDeselectedBy = selectedBy;
				Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
				if(parameter != null && parameter.getValue() != null)
					graceDeselectedBy = parameter.getValue();
				
				SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn, subID(subscriberID),
						subID(callerID), status, fromTime, toTime, graceDeselectedBy, rbtType);
				
				count = createSubscriberStatus(subscriberID, callerID, categories.id(),
						subscriberWavFile, setTime, startDate, endDate, status, selectedBy,
						selectInfo, nextChargingDate, prepaid, classType, changeSubType, fromTime,
						toTime, sel_status, true, clipMap, categories.type(), useDate, loopStatus,
						isTata, nextPlus, rbtType, selInterval, extraInfo, refID, isDirectActivation, circleID);

				logger.info("Checking to update num max selections or not."
						+ " count: " + count + " isPackSel: " + isPackSel);

				if (isPackSel && count == 1)
					ProvisioningRequestsDao.updateNumMaxSelections(conn, subscriberID, packCosID);

				if (updateEndDate) {
					SubscriberImpl.updateEndDate(conn, subID(subscriberID), endDate, null);
				}

			}
			else {
				return SELECTION_FAILED_SELECTION_OVERLAP;
			}
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		if(count > 0 && addResult != null && addResult.indexOf("SUCCESS:DOWNLOAD_ALREADY_ACTIVE") != -1)
			return SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS;
		else if (count > 0)
			return SELECTION_SUCCESS;
		else
			return SELECTION_FAILED_INTERNAL_ERROR;
	}

	@Override
	public boolean isValidPrefix(String subscriberID){

		if (subscriberID == null || subscriberID.length() < 7 || subscriberID.length() > 15)
			return false;
		else
		{
			try
			{
				Long.parseLong(subID(subscriberID));
			}
			catch (Throwable e)
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String updateSubscriberSelection(String subscriberId,String callerId,String subWavFile,Date setTime,int fromTime,
			int toTime,String selInterval,String selectedBy){

		//Normal sel to special sel allowed
		//Special sel 1 to special sel 2 allowed (TOD overlap considering)
		// Special sel to normal not allowed
		int status = -1;
		SubscriberStatus[] selections = null;
		Date endDate = null;
		String success = null;
		if(fromTime==0 && toTime==2359 && (selInterval==null || selInterval.equalsIgnoreCase("") || selInterval.equalsIgnoreCase("null")))
			return "INVALID PARAMETERS";
		if((selInterval != null && !selInterval.equalsIgnoreCase("") && !selInterval.equalsIgnoreCase("null")) && !(fromTime == 0 && toTime == 2359))
			return "INVALID PARAMETERS";
		/*if(fromTime==0 && toTime==23 && (selInterval==null || selInterval.equalsIgnoreCase(""))){
			if(fromTime == 0 && toTime == 23)
				return false;
		}else{

			if(fromTime !=0 && toTime !=23)
				return false;
		}*/
		subscriberId = subID(subscriberId);
		SubscriberStatus subscriberStatus = getSelection(subscriberId,callerId,subWavFile,setTime);
		Date date = new Date();
		if(subscriberStatus == null || subscriberStatus.endTime().before(date))
		{
			return "SELECTION_FAILED_SELECTION_DOES_NOT_EXIST";
		}
//		String circleID=getCircleId(subscriberId);
		Subscriber sub = getSubscriber(subscriberId);
		char prepaidYes = 'n';
		boolean isPrepaid = false;
		if(sub.prepaidYes())
		{
			prepaidYes = 'y';
			isPrepaid = true;
		}
		Categories categories = getCategory(subscriberStatus.categoryID(), sub.circleID(), prepaidYes);
		ClipMinimal clips = getClipRBT(subWavFile);
		HashMap clipMap = new HashMap();
		if(clips != null)
		{
			clipMap.put("CLIP_CLASS", clips.getClassType());
			clipMap.put("CLIP_END", clips.getEndTime());
			clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
			clipMap.put("CLIP_WAV", clips.getWavFile());
			clipMap.put("CLIP_ID", ""+clips.getClipId());
			clipMap.put("CLIP_NAME", clips.getClipName());
		}
		else
		{
			clipMap.put("CLIP_WAV", subWavFile);
		}
		if(selInterval != null && !selInterval.equalsIgnoreCase("null"))
		{
			if(selInterval.startsWith("W"))
			{
				status = 75;
			}
			else if(selInterval.startsWith("Y"))
				status = 95;
			else
				return "INVALID PARAMETERS";
		}
		else
		{
			status = 80;
		}
		
		deactivateSubscriberRecords(subscriberId,callerId,subscriberStatus.status(),subscriberStatus.fromTime(),subscriberStatus.toTime(),
				true, selectedBy,subWavFile,0);
		
		String ret =  addSubscriberSelections(subscriberId,callerId, categories, clipMap,
				setTime, null,  null, status, selectedBy, subscriberStatus.selectionInfo(), 0, isPrepaid, false, null,
				fromTime, toTime, null, true, true, "VUI", null, sub.subYes(), null, null,
				true,false, null, false, false, false,sub.subscriptionClass(), sub, 0,selInterval,null, false, null, false);
			
		return ret;
	}

	@Override
	public boolean addSubscriberSelections(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType, boolean incrSelCount,
			boolean OptIn, boolean inLoop,String subClass, Subscriber subscriber,String selInterval)
	{
		subscriberID = subID(subscriberID);
		/*String circleID=getCircleId(subscriberID);
		Subscriber sub = getSubscriber(subscriberID);*/
		char prepaidYes = 'n';
		if(subscriber.prepaidYes())
			prepaidYes = 'y';
		Categories categories = getCategory(categoryID, subscriber.circleID(), prepaidYes);
		ClipMinimal clips = getClipRBT(subscriberWavFile);
		HashMap clipMap = new HashMap();
		if(clips != null)
		{
			clipMap.put("CLIP_CLASS", clips.getClassType());
			clipMap.put("CLIP_END", clips.getEndTime());
			clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
			clipMap.put("CLIP_WAV", clips.getWavFile());
			clipMap.put("CLIP_ID", ""+clips.getClipId());
			clipMap.put("CLIP_NAME", clips.getClipName());
		}
		else
		{
			clipMap.put("CLIP_WAV", subscriberWavFile);
		}
		String ret =  addSubscriberSelections(subscriberID,
				callerID, categories, clipMap,
				setTime, startTime,  endTime, status,
				selectedBy, selectionInfo, freePeriod,
				isPrepaid, changeSubType, messagePath,
				fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode,
				regexType, subYes, promoType, null,
				incrSelCount,false, null, OptIn, false, inLoop,subClass, subscriber, 0,selInterval);
		if(ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;
	}


	// Added on 24/12 .Hariharan. 



	@Override
	public int cleanOldSelections(float duration, boolean m_useSM)
	{
		return -1;
	}


	@Override
	public String deactivateSubscriber(Connection conn, String subscriberID, String deactivate, Date date, boolean delSelections, 
			boolean sendToHLR, boolean smDeactivation, boolean isDirectDeact, boolean checkSubClass, int rbtType, Subscriber sub, 
			String dctInfo, String userInfoXml) 
	{ 
		String ret = null;
		if (conn == null)
			return null;
		try
		{
			if(deactivate.equalsIgnoreCase("CC") || deactivate.equalsIgnoreCase("CCC"))
				checkSubClass = false;
		
			if(sub == null || sub.subYes() == null)
				return null;
			String subYes = sub.subYes();
			if (!(subYes.equalsIgnoreCase("B") || subYes.equalsIgnoreCase("O") || subYes.equals("Z") || subYes.equals("z") || subYes.equals("G")))
				ret = "ACT_PENDING";
			else if(checkSubClass)
			{
				SubscriptionClass temp = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(sub.subscriptionClass());
				if(temp != null && temp.isDeactivationNotAllowed())
					return "DCT_NOT_ALLOWED";
			}

			if(sub.rbtType() == TYPE_RBT_RRBT || sub.rbtType() == TYPE_SRBT_RRBT) 
				convertSubscriptionType(subID(subscriberID), sub.subscriptionClass(), "DEFAULT", null, rbtType, true, null, sub);
			else
			{
				SubscriberImpl.deactivate(conn, subID(subscriberID), deactivate, date, sendToHLR, smDeactivation, false, isDirectDeact, 
						m_isMemCachePlayer,dctInfo, sub, userInfoXml); // dctInfo= null for all apart from airtel
				SubscriberDownloadsImpl.expireAllSubscriberPendingDownload(conn, subID(subscriberID), "GATHERER", null);
				SubscriberDownloadsImpl.expireAllSubscriberActivationPendingDownload(conn, subID(subscriberID), "GATHERER");
				SubscriberDownloadsImpl.activateDctPendingDownload(conn, subID(subscriberID));
			}
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return ret == null ? "SUCCESS" : ret;
	}
	
	public String smSubscriptionRenewalFailure(String subscriberID,
			String deactivatedBy, String type, String classType, boolean isRenewal)
	{
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;
		
		boolean success = false;
		try
		{
			if(!m_retainDownloadsSubDct || !m_addToDownloads || !isRenewal)
				SubscriberStatusImpl.smSubscriptionRenewalFailure(conn, subID(subscriberID), deactivatedBy, type, null);
			SubscriberDownloadsImpl.expireAllSubscriberActivationPendingDownload(conn, subID(subscriberID), "SM");
			SubscriberDownloadsImpl.activateDctPendingDownload(conn, subID(subscriberID));
			
			// Added extraInfo - TRAI changes 
			success = SubscriberImpl.smSubscriptionRenewalFailure(conn, subID(subscriberID), deactivatedBy, type, classType,
					m_isMemCachePlayer, null, false, null);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	@Override
	public String[] getOldSubscribers(float duration, boolean m_useSM) 
	{ 
		Connection conn = getConnection(); 
		if (conn == null) 
			return null; 
		String[] subscribers = null;
		try
		{
			subscribers = SubscriberImpl.getOldSubscribers(conn, duration, m_useSM);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return subscribers; 
	} 


	public boolean updateSubscriberDownloadsStatusToD(String subscriberID,String deactivationInfo)
	{
		Connection conn = getConnection();
		boolean results = false;
		if(conn == null)
			return results;
		try
		{
			subscriberID = subID(subscriberID);
			results = SubscriberDownloadsImpl.updateSubscriberDownloadsStatusToD(conn, subscriberID,deactivationInfo);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return results;
	}
	public boolean deleteDeactivatedDownloads(String subscriberId){

		Connection conn=getConnection();
		if(conn==null)
			return false;
		
		boolean result=false;
		try
		{
			subscriberId = subID(subscriberId);
			result=SubscriberDownloadsImpl.deleteDeactivatedDownloads(conn, subscriberId);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return result;
	}


	public boolean deleteDeactivatedRecords(String subscriberId){

		Connection conn=getConnection();
		if(conn==null)
			return false;
	
		boolean result=false;
		try
		{
			result=SubscriberStatusImpl.deleteDeactivatedRecords(conn, subscriberId);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return result;
	}

	@Override
	public SubscriberDownloads[] getDeactiveSubscriberDownloads(String subscriberId)
	{
		Connection conn = getConnection();
		if (conn == null)
			return null;
		
		SubscriberDownloads[] subscriberDownloads = null;
		try
		{
			subscriberDownloads = SubscriberDownloadsImpl.getDeactiveSubscriberDownloads(conn,subscriberId);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return subscriberDownloads;

	}
	//RBT-9873 Added xtraParametersMap for CG flow
	@Override
	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid, int activationTimePeriod,
			int freePeriod, String actInfo, String classType,
			boolean smActivation, CosDetails cos, boolean isDirectActivation, int rbtType,HashMap extraInfo, String circleId, String refId, boolean isComboRequest, Map<String,String> xtraParametersMap)
	{
		Connection conn = getConnection();
		if(conn == null)
			return null;
		Subscriber subscriber = null;
		try
		{
			String prepaid = "n";
			if(isPrepaid)
				prepaid = "y";
			subscriberID = subID(subscriberID);
			subscriber = SubscriberImpl.getSubscriber(conn, subID(subscriberID));
			
			if(!isTNBNewFlow && classType != null && tnbSubscriptionClasses.contains(classType) && endDate == null)
			{
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(classType); 
				endDate = getNextDate(subClass.getSubscriptionPeriod()); 
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, +1);
				endDate = endCal.getTime();
			}
			else if(m_subOnlyChargeClass != null && m_subOnlyChargeClass.containsKey(classType))
			{
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
			}
			if(cos == null)
				cos = super.getCos(null, subscriberID, subscriber, circleId, isPrepaid?"y":"n", activate, classType);

			if(cos != null && !cos.isDefaultCos())
			{
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, cos.getValidDays()-1);
				endDate = cal.getTime();
				if(endDate.after(cos.getEndDate()))
					endDate = cos.getEndDate();
			}

			String activationInfo = actInfo;

			String subscription = STATE_TO_BE_ACTIVATED;
			if(isDirectActivation)
				subscription = STATE_ACTIVATED;
			
			String cosID = null;
			if(cos != null)
				cosID = cos.getCosId();

			String subscriptionClass = classType;
			if(classType == null)
				subscriptionClass = "DEFAULT";

			SubscriberPromo subscriberPromo = SubscriberPromoImpl.getActiveSubscriberPromo(conn, subID(subscriberID), "ICARD");
			if(subscriberPromo != null)
			{
				if(subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();
				SubscriberPromoImpl.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO"))
			{
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion( conn, subID(subscriberID), null);
				if (viralSMS != null)
					activationInfo = activationInfo + ":" + "viral";
			}
			
			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			String finalRefID = UUID.randomUUID().toString();

			//Added for JIRA-RBT-6321
			if(isDirectActivation && refId != null)
				finalRefID = refId;
			
			if (subscriber != null)
			{
				String subsciptionYes = subscriber.subYes();
				if (!isDirectActivation && subscriber.endDate().getTime() > getDbTime(conn))
				{
					if (subsciptionYes.equals("B") && (subscriber.rbtType() == TYPE_RBT || subscriber.rbtType() == TYPE_RRBT 
							|| subscriber.rbtType() == TYPE_SRBT) && subscriber.rbtType() != rbtType)
					{
						if ((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT) || (subscriber.rbtType() == TYPE_SRBT 
								&& rbtType != TYPE_RBT))
						{
						}
						else
						{
							if (subscriber.rbtType() == TYPE_RBT)
								rbtType = TYPE_RBT_RRBT;
							else if (subscriber.rbtType() == TYPE_SRBT)
								rbtType = TYPE_SRBT_RRBT;
							convertSubscriptionType(subID(subscriberID), subscriber.subscriptionClass(), m_comboSubClass, null,
									rbtType, true, null, subscriber);
						}
					}
					return subscriber;
				}
				if(!isDirectActivation && (subsciptionYes.equals("D") || subsciptionYes.equals("P") || subsciptionYes.equals("F") 
						|| subsciptionYes.equals("x") || subsciptionYes.equals("Z") || subsciptionYes.equals("z")))
					return null;
				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();
				
				boolean success = SubscriberImpl.update(conn, subID(subscriberID), activate, null, startDate,
						endDate, prepaid, null, null, 0, activationInfo, subscriptionClass,
						deactivatedBy, deactivationDate, null, subscription, 0, cosID, cosID,
						rbtType, subscriber.language(), subExtraInfo, circleId, finalRefID, isDirectActivation);
				if(startDate == null)
					startDate = new Date(System.currentTimeMillis());
				
				if (success)
				{
					subscriber = new SubscriberImpl(subID(subscriberID), activate, null, startDate,
							m_endDate, prepaid, null, null, 0, activationInfo, subscriptionClass,
							subscription, deactivatedBy, deactivationDate, null, 0, cosID, cosID,
							rbtType, subscriber.language(), subscriber.oldClassType(),subExtraInfo, circleId, finalRefID);
				}
				else
				{
					subscriber = null;
				}
			}
			else
				subscriber = SubscriberImpl.insert(conn, subID(subscriberID), activate, null,
						startDate, endDate, prepaid, null, null, 0, activationInfo,
						subscriptionClass, null, null, null, subscription, 0, cosID, cosID,
						rbtType, null, isDirectActivation, subExtraInfo, circleId, finalRefID);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return subscriber;
	}
	@Override
	public  int cleanSubscribers(float period, boolean useSM)
	{
		int count = 0;
		String[] subscribers = getOldSubscribers(period,useSM);
		for(int i = 0; subscribers != null && i < subscribers.length ; i++ )
		{
			try
			{ 
				count++;
				String subID = subscribers[i].trim(); 
				removeSubscriberBookMark(subID);
				expireAllSubscriberDownload(subID, "GATHERER");
				deleteDeactivatedDownloads(subID);
				deleteDeactivatedRecords(subID);
				deleteGroupMembersOfSubscriber(subID);
				deleteGroupsOfSubscriber(subID);
				if(getSubscriberDownloads(subID) == null && getSubscriberRecords(subID) == null && getGroupsForSubscriberID(subID) == null)
					cleanOldSubscriber(subID(subID)); 
			} 
			catch(Exception e)
			{ 
				logger.error("", e);
			} 
		}
		logger.info("RBT::cleaned subscribers older than "+ period);
		return count;
	} 


	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.database.RBTDBManager#getSubscriberInfo(java.lang.String)
	 */
	@Override
	public HashMap<String, String> getSubscriberInfo(String subscriberID)
	{
		HashMap<String, String> subscriberInfoMap = new HashMap<String, String>();
		subscriberInfoMap.put("STATUS", "INVALID");

		try
		{
			Hashtable<String, String> env = getLdapEnvironment();
			LdapContext ldapContext = new InitialLdapContext(env, null);

			String subEntryDN = getEntryDN(subscriberID);

			String[] attrIDs = {"vfProvider"};
			Parameters attrIDParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_ATTRIBUTE_LIST");
			if(attrIDParam != null)
				attrIDs[0] = attrIDParam.getValue().trim();

			Attributes attributes = ldapContext.getAttributes(subEntryDN, attrIDs);
			subscriberInfoMap.put("STATUS", "VALID");

			Attribute attribute = attributes.get(attrIDs[0]);
			if(attribute != null)
			{
				String vfProvider = (String) attribute.get();
				logger.info("RBT:: vfProvider = "+ vfProvider);
				vfProvider = vfProvider.trim();
				if(vfProvider.equalsIgnoreCase("ABP"))
					subscriberInfoMap.put("USER_TYPE", "POSTPAID");
				else
					subscriberInfoMap.put("USER_TYPE", "PREPAID");
			}
			else
			{
				logger.info("RBT:: Attribute vfProvider does not exist");
				subscriberInfoMap.put("USER_TYPE", "PREPAID");
			}

			ldapContext.close();
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		logger.info("RBT:: subscriberInfoMap = "+ subscriberInfoMap);
		return subscriberInfoMap;
	}

	private Hashtable<String, String> getLdapEnvironment()
	{
		if(ldapEnvironment == null)
		{
			ParametersCacheManager parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
			
			Parameters providerURLParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "LDAP_PROVIDER_URL");
			String providerURL = providerURLParam.getValue();
			
			Parameters securiryPrincipalParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "LDAP_SECURITY_PRINCIPAL");
			String securiryPrincipal = securiryPrincipalParam.getValue();
			
			Parameters securiryCredentialsParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "LDAP_SECURITY_CREDENTIALS");
			String securiryCredentials = securiryCredentialsParam.getValue();

			ldapEnvironment = new Hashtable<String, String>();
			ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			ldapEnvironment.put(Context.PROVIDER_URL, providerURL);

			ldapEnvironment.put(Context.SECURITY_AUTHENTICATION, "simple");
			ldapEnvironment.put(Context.SECURITY_PRINCIPAL, securiryPrincipal);
			ldapEnvironment.put(Context.SECURITY_CREDENTIALS, securiryCredentials);
		}

		logger.info("RBT:: ldapEnvironment = "+ ldapEnvironment);
		return ldapEnvironment;
	}

	private String getEntryDN(String subscriberID)
	{
		if(entryDN == null)
		{
			entryDN = "vfTelephoneNumber=%SUBSCRIBER_ID%,ou=GSM,ou=subscriber,ou=vodafonero,c=ro,o=vodafone";
			Parameters entryDNParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_ENTRY_DN");
			if(entryDNParam != null)
				entryDN = entryDNParam.getValue().trim();

			String countryPrefix = getCountryPrefix();
			if(countryPrefix != null && !countryPrefix.equalsIgnoreCase(""))
				countryPrefixes = countryPrefix.split(",");
		}

		subscriberID = subID(subscriberID);
		subscriberID = countryPrefixes[0] + subscriberID;

		String subEntryDN = entryDN.replaceAll("%SUBSCRIBER_ID%", subscriberID);

		logger.info("RBT:: subEntryDN = "+ subEntryDN);
		return subEntryDN;
	}

	@Override
	public Subscriber trialActivateSubscriber(String subscriberID, String activate, Date date, boolean isPrepaid, int activationTimePeriod,
			int freePeriod, String actInfo, String classType, boolean smActivation, String selClass, String subscriptionType, String circleId)
	{
		Connection conn = getConnection();
		if (conn == null)
			return null;
		
		int rbtType = 0;
		Subscriber subscriber = null;
		try
		{
			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			subscriberID = subID(subscriberID);
			Date endDate = null;
			subscriber = SubscriberImpl.getSubscriber(conn, subID(subscriberID));
			
			if (activate.equalsIgnoreCase("TNB") && classType != null && classType.equalsIgnoreCase("ZERO"))
			{
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(classType); 
				endDate = getNextDate(subClass.getSubscriptionPeriod()); 
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, -1);
				endDate = endCal.getTime();

			}
			else if(m_subOnlyChargeClass != null && m_subOnlyChargeClass.containsKey(classType))
			{
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
			}
			else if (selClass != null && selClass.startsWith("TRIAL"))
			{
				ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(selClass);
				if (chargeClass != null && chargeClass.getSelectionPeriod() != null && chargeClass.getSelectionPeriod().startsWith("D"))
				{
					String selectionPeriod = chargeClass.getSelectionPeriod().substring(1);
					if ("OPTIN".equalsIgnoreCase(subscriptionType))
					{
						Calendar endCal = Calendar.getInstance();
						endCal.add(Calendar.DATE, Integer.parseInt(selectionPeriod) - 1);
						endDate = endCal.getTime();
						if(actInfo == null)
							actInfo = ":optin:";
						else
							actInfo = actInfo + ":optin:";
					}
					else if ("OPTOUT".equalsIgnoreCase(subscriptionType))
						endDate = m_endDate;
				}
			}
			else
				endDate = m_endDate;
			
			Date nextChargingDate = null;
			Date lastAccessDate = null;
			Date activationDate = null;
			String subscription = STATE_TO_BE_ACTIVATED;
			String activationInfo = actInfo;
			CosDetails cos = getCos(null, subscriberID, subscriber, circleId, isPrepaid?"y":"n", activate, classType);
			if(cos != null && !cos.isDefaultCos())
			{
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, cos.getValidDays()-1);
				endDate = cal.getTime();
				if(endDate.after(cos.getEndDate()))
					endDate = cos.getEndDate();
			}
			String cosID = null;
			if(cos != null)
				cosID = cos.getCosId();

			String subscriptionClass = classType;
			if (classType == null)
				subscriptionClass = "DEFAULT";

			SubscriberPromo subscriberPromo = SubscriberPromoImpl.getActiveSubscriberPromo(conn, subID(subscriberID), "ICARD");
			if (subscriberPromo != null)
			{
				if (subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();
				SubscriberPromoImpl.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO"))
			{
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(conn, subID(subscriberID), null);
				if (viralSMS != null)
					activationInfo = activationInfo + ":" + "viral";
			}

			if (subscriber != null)
			{
				String subsciptionYes = subscriber.subYes();
				if (subscriber.endDate().getTime() > getDbTime(conn))
				{
					if(subsciptionYes.equals("B") && (subscriber.rbtType() == TYPE_RBT || subscriber.rbtType() == TYPE_RRBT || subscriber.rbtType() == TYPE_SRBT) && subscriber.rbtType() != rbtType)
					{
						if((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT) || (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT))
						{
						}
						else
						{
							if(subscriber.rbtType() == TYPE_RBT) 
								rbtType = TYPE_RBT_RRBT; 
							else if(subscriber.rbtType() == TYPE_SRBT) 
								rbtType = TYPE_SRBT_RRBT; 
							convertSubscriptionType(subID(subscriberID), subscriber.subscriptionClass(), m_comboSubClass, null, rbtType, true, null, subscriber); 
						}
					}
					return subscriber;
				}
				if (subsciptionYes.equals("D") || subsciptionYes.equals("P") || subsciptionYes.equals("F")  || subsciptionYes.equals("x")
						|| subsciptionYes.equals("Z") || subsciptionYes.equals("z"))
					return null;
				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();
				String refID = UUID.randomUUID().toString();
				
				SubscriberImpl.update(conn, subID(subscriberID), activate, null, date, endDate, prepaid, lastAccessDate, nextChargingDate,
						0, activationInfo, subscriptionClass, deactivatedBy, deactivationDate, activationDate,
						subscription, 0, cosID, cosID, 0, subscriber.language(), null, circleId, refID, false);
				Date startDate = date;
				if (date == null)
					startDate = new Date(System.currentTimeMillis());
				subscriber = new SubscriberImpl(subID(subscriberID), activate, null, startDate, m_endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass, subscription, deactivatedBy, deactivationDate,
						activationDate, 0, cosID, cosID, rbtType, subscriber.language(),subscriber.oldClassType(),null, circleId, refID);
			}
			else
				subscriber = SubscriberImpl.insert(conn, subID(subscriberID), activate, null, date, endDate,
						prepaid, lastAccessDate, nextChargingDate, 0, activationInfo, subscriptionClass, null, null,
						activationDate, subscription, 0, cosID, cosID, rbtType, null, false, null, circleId, null);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return subscriber;
	}

	@Override
	public String getBackEndSubscriberStatus(String subscriberID, boolean isPrepaid) {
		if(_vodaPlayerImpl == null) {
			try {
				_vodaPlayerImpl = new VodaRomaniaPlayerImpl(this);
			}
			catch(RBTException e) {
				logger.error("", e);
			}
		}

		if(_vodaPlayerImpl != null)
			return _vodaPlayerImpl.getSubscriberStatus(subscriberID, isPrepaid);
		return "ERROR";
	}

	@Override
	public String addCallerInGroup(String subscriberID, int groupID, String callerID, String callerName, String preGroupID, String preGroupName)
	{
		String result = null;
		String res = null;
		subscriberID=subID(subscriberID);
		if(groupID == -1)
		{
			if(preGroupID != null)
			{
				result = addGroupForSubscriberID(preGroupID, preGroupName, subscriberID, null);
			}
			else
			{
				return CALLER_NOT_ADDED_INTERNAL_ERROR;
			}

			if(result.equals(SAME_PREGROUP_EXISTS_FOR_CALLER) || result.equals(GROUP_ADDED_SUCCESFULLY))
			{
				Groups group = getGroupByPreGroupID(preGroupID, subscriberID);
				if(group != null && group.status() != null && !(group.status().equals("X") || group.status().equals("D")))
				{
					res = addCallerInGroup(subscriberID, group.groupID(), callerID, callerName);
				}
			}
			else
			{
				return result;
			}
		}
		else
		{
			res = addCallerInGroup(subscriberID, groupID, callerID, callerName);
		}

		return res;
	}

	@Override
	public String addCallerInGroup(String subscriberID, int groupID, String callerID, String callerName)
	{
		Connection conn = getConnection();
		if(conn == null)
			return null;
		try
		{
			subscriberID=subID(subscriberID);
			SubscriberStatus[] subscriberStatus = null;

			Groups[] groups = null;
			GroupMembers[] groupMembers = null;

			groups = GroupsImpl.getActiveGroupsForSubscriberID(conn,subscriberID);
			if(groups != null)
			{
				for(int i=0;i<groups.length;i++)
				{
					groupMembers = GroupMembersImpl.getActiveMembersForGroupID(conn, groups[i].groupID());

					if(groupMembers != null)
					{
						for(int j=0;j<groupMembers.length;j++)
						{
							if(callerID != null && groupMembers[j] != null && groupMembers[j].callerID() != null && callerID.equals(groupMembers[j].callerID()))
							{
								return CALLER_ALREADY_PRESENT_IN_GROUP;
							}
						}
					}
				}
			}
			GroupMembers[] groupMem = null;
			int count=0;
			groupMem = GroupMembersImpl.getMembersForGroupID(conn, groupID);

			if(groupMem != null )
			{
				for(int i=0;i<groupMem.length;i++)
				{
					if(groupMem[i] != null && groupMem[i].status() != null && !(groupMem[i].status().equals("D") || groupMem[i].status().equals("X")))
					{
						count++;
					}
				}
			}

			if(count >= m_MAX_CALLER_ALLOWED_IN_GROUPS)
			{
				return MAX_CALLER_PRESENT_IN_GROUP;
			}
			subscriberStatus = SubscriberStatusImpl.getAllSubscriberSelectionRecordsNotDeactivated(conn, subscriberID, null, 0);
			if(subscriberStatus != null)
			{
				for(int i=0;i<subscriberStatus.length;i++)
				{
					if(subscriberStatus[i] != null && subscriberStatus[i].callerID() != null && callerID != null && subscriberStatus[i].callerID().equals(callerID))
					{
						return ALREADY_PERSONALIZED_SELECTION_FOR_CALLER;
					}
				}
			}
			String status = null;
			Groups group = GroupsImpl.getGroup(conn, groupID);
			if(group != null)
			{
				if(group.status() != null && (group.status().equals("W") || group.status().equals("A")))
				{
					status = "W";
				}
				else if(group.status() != null && (group.status().equals("B")))
				{
					status = "A";
				}
				else
				{
					return CALLER_NOT_ADDED_INTERNAL_ERROR;
				}
			}
			else
			{
				return CALLER_NOT_ADDED_INTERNAL_ERROR;
			}
			boolean update = false;
			if(groupMem != null)
			{
				for(int i=0;i<groupMem.length;i++)
				{
					if(groupMem[i] != null && callerID!= null && groupMem[i].callerID() != null && groupMem[i].callerID().equals(callerID))
					{
						update = true;
					}
				}
			}
			boolean res = false;
			GroupMembers groupMember = null;
			if(update)
			{
				res = GroupMembersImpl.updateGroupMember(conn, groupID, callerID, callerName, status);
			}
			else
			{
				groupMember = GroupMembersImpl.insert(conn, groupID, callerID, callerName, status);
				if(groupMember != null)
					res = true;
				else
					res = false;
			}
			return res ? CALLER_ADDED_TO_GROUP : CALLER_NOT_ADDED_INTERNAL_ERROR;
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return CALLER_NOT_ADDED_INTERNAL_ERROR;
	}

	@Override
	public String addGroupForSubscriberID(String preGroupID, String groupName,
			String subscriberID, String groupPromoID)
	{
		Connection conn = getConnection();
		if(conn == null)
			return null;
		boolean res = false;
		try
		{
			Subscriber sub = getSubscriber(subscriberID);
			if(sub == null || !isSubActive(sub))
				return USER_NOT_ACTIVE;
			if(groupName == null || groupName.equalsIgnoreCase("null") || groupName.equals(""))
			{
				return GROUP_ADD_FAILED_GROUPNAME_NULL;
			}
			if(preGroupID != null)
			{
				Groups preGroup = getGroupByPreGroupID(preGroupID, subscriberID);
				if(preGroup != null && preGroup.status() != null && !preGroup.status().equals("X"))
				{
					return SAME_PREGROUP_EXISTS_FOR_CALLER;
				}
			}
			String status = null;
			int count=0;
			Groups[] groups = GroupsImpl.getActiveGroupsForSubscriberID(conn, subscriberID);
			if(groups != null)
			{
				for(int i=0;i<groups.length;i++)
				{
					if(groups[i].groupName() != null && groups[i].groupName().equalsIgnoreCase(groupName))
					{
						return SAME_GROUP_NAME_EXISTS_FOR_CALLER;
					}
					count++;
				}
			}
			if(count >= m_MAX_GROUPS_ALLOWED)
				return MAX_GROUP_PRESENT_FOR_SUBSCRIBER;
	
	
			if(sub != null && (sub.subYes().equals(STATE_ACTIVATED) || sub.subYes().equals(STATE_EVENT)))
				status = STATE_TO_BE_ACTIVATED;
			else
				status = STATE_BASE_ACTIVATION_PENDING;
			res = GroupsImpl.insert(conn, preGroupID, groupName, subscriberID, null, status);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}	
		return res ? GROUP_ADDED_SUCCESFULLY : GROUP_ADD_FAILED_INTERNAL_ERROR;
	}
	
	@Override
	public boolean deleteGroup(String subscriberID, int groupID, String deactivatedBy)
	{
		Connection conn = getConnection();
		if(conn == null)
			return false;
		
		boolean result = false;
		try
		{
			result = GroupsImpl.deactivateGroup(conn,groupID);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return result;
	}
	
	@Override
	public boolean removeCallerFromGroup(String subscriberID, int groupID, String callerID)
	{
		Connection conn = getConnection();
		if (conn == null)
			return false;
		
		boolean result = false;
		try
		{
			result = GroupMembersImpl.removeCallerFromGroup(conn, groupID, callerID);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return result;
	}

	@Override
	public boolean changeGroupForCaller(String subscriberID, String callerID, int fromGroupID, int toGroupID)
	{
		Connection conn = getConnection();
		if(conn == null)
			return false;
		
		boolean res = false;
		try
		{
			boolean result = false;
			GroupMembers[] groupMembers = null;
			String callerName = null;
			groupMembers = GroupMembersImpl.getActiveMembersForGroupID(conn, fromGroupID);
			if(groupMembers == null)
				return false;
			
			for(int i=0;i<groupMembers.length;i++)
			{
				if(groupMembers[i] != null && groupMembers[i].callerID() != null && groupMembers[i].callerID().equals(callerID))
					callerName = groupMembers[i].callerName();
			}
			result = GroupMembersImpl.removeCallerFromGroup(conn, fromGroupID, callerID);
			GroupMembers groupMember = null;
			GroupMembers[] groupMem = null;
			int count=0;
			groupMem = GroupMembersImpl.getMembersForGroupID(conn, toGroupID);
	
			if(groupMem != null )
				for(int i=0;i<groupMem.length;i++)
					if(groupMem[i] != null && groupMem[i].status() != null && !(groupMem[i].status().equals("D") || groupMem[i].status().equals("X")))
						count++;
			
			if(count >= m_MAX_CALLER_ALLOWED_IN_GROUPS)
				return false;
			boolean update = false;
			if(result)
			{
				String status = null;
				Groups group = GroupsImpl.getGroup(conn, toGroupID);
				if(group != null)
				{
					if(group.status() != null && (group.status().equals("W") || group.status().equals("A")))
						status = "W";
					else if(group.status() != null && (group.status().equals("B")))
						status = "A";
					else
						return false;
				}
				else
					return false;
				if(groupMem != null)
					for(int i=0;i<groupMem.length;i++)
						if(groupMem[i] != null && callerID!= null && groupMem[i].callerID() != null && groupMem[i].callerID().equals(callerID))
							update = true;
				
				if(update)
					res = GroupMembersImpl.updateGroupMember(conn, toGroupID, callerID, callerName, status);
				else
				{
					groupMember = GroupMembersImpl.insert(conn, toGroupID, callerID, callerName, status);
					if(groupMember != null)
						res = true;
					else
						res = false;
				}
			}
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.database.RBTDBManager#updateSettingsForDownloadRenewalSuccessCallback(java.sql.Connection, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean updateSettingsForDownloadRenewalSuccessCallback(Connection conn, String subscriberID, String wavFile)
	{
		SubscriberStatusImpl.updateSettingsForDownloadRenewalSuccessCallback(conn, subscriberID, wavFile, LOOP_STATUS_OVERRIDE, 
				LOOP_STATUS_OVERRIDE_FINAL);
		SubscriberStatusImpl.updateSettingsForDownloadRenewalSuccessCallback(conn, subscriberID, wavFile, LOOP_STATUS_LOOP, 
				LOOP_STATUS_LOOP_FINAL);
		return true;
	}
	
	private SubscriberStatus isFutureDateOverlap(SubscriberStatus[] subscriberStatus, Date startDate, Date endDate, String callerID)
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.database.RBTDBManager#updateSubscriberId(java.lang.String, java.lang.String)
	 */
	@Override
	public String updateSubscriberId(String newSubscriberId,String subscriberId)
	{
		Connection conn = getConnection();
		if(conn == null)
			return null;
		try
		{
			Subscriber sub = getSubscriber(subID(newSubscriberId));
			if (sub != null)
				return "FAILURE:NEW MSISDN ALREADY EXISTS";
			Subscriber subscriber = getSubscriber(subID(subscriberId));
			if (subscriber == null)
				return "FAILURE:MSISDN DOESN'T EXIST";
			if (!isValidPrefix(newSubscriberId))
				return "FAILURE:NEW MSISDN INVALID";

			boolean success = SubscriberImpl.updateSubscriberId(conn,subID(newSubscriberId),subID(subscriberId));
			SubscriberStatusImpl.updateSubscriberId(conn,subID(newSubscriberId),subID(subscriberId));
			SubscriberDownloadsImpl.updateSubscriberId(conn,subID(newSubscriberId),subID(subscriberId));
			GroupsImpl.updateSubscriberId(conn, subID(newSubscriberId), subID(subscriberId));
			ViralSMSTableImpl.updateSubscriberId(conn, subID(newSubscriberId), subID(subscriberId));
			ProvisioningRequestsDao.updateSubscriberID(conn,subID(newSubscriberId),subID(subscriberId));
			if(success)
				ViralSMSTableImpl.insert(conn, subscriberId, new Date(), "CHANGEMSISDN", newSubscriberId, null, 0, null, null, null);
			return success ? "SUCCESS" : "FAILURE:TECHNICAL FAULT";
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL FAULT";
	}
}