package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
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
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class GrameenDbMgrImpl extends RBTDBManager{
	
	private static String m_success = "SUCCESS";
	private static String m_failure = "FAILURE";

	private static Logger logger = Logger.getLogger(GrameenDbMgrImpl.class);
	private static String cricClassType = RBTParametersUtils.getParamAsString(WEBSERVICE, "BYPASS_CRICKET_CHARGE_CLASS", null);
	
	private static List<String> cricClassList = new ArrayList<String>();
	
	static {
		if(cricClassType != null){
			String[] cricClassArray = cricClassType.split(",");
			cricClassList = Arrays.asList(cricClassArray);
		}
	}
	
	@Override
	public String smRenewalSuccess(String subscriberID, Date nextChargingDate, String type, String classType, String actInfo, 
			String extraInfo, String strNextBillingDate,boolean playstatusA)
	{
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;
		
		boolean success = false;
	
		//For the Suspension of subscriber on RBT side but active on SM side
		boolean isUserVoluntarySuspended = false;
		HashMap <String,String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo); 
		if(extraInfoMap!=null && extraInfoMap.containsKey(VOLUNTARY)){
		   if(extraInfoMap.get(VOLUNTARY)!=null && extraInfoMap.get(VOLUNTARY).equalsIgnoreCase("SM_SUSPENDED")){
        	  extraInfo = DBUtility.setXMLAttribute(extraInfo, VOLUNTARY, "TRUE");        	  
		   }
		   isUserVoluntarySuspended = true;
        }
		try
		{
			success = SubscriberImpl.grameenSmRenewalSuccess(conn, subID(subscriberID), nextChargingDate, type, classType,actInfo, extraInfo,isUserVoluntarySuspended,playstatusA);
		    if(success && !isUserVoluntarySuspended) {
		    	boolean isPack = false;
		    	if(extraInfoMap != null && extraInfoMap.containsKey(PACK)) {
		    		isPack = true;
		    	}// RBT-14301: Uninor MNP changes.
		    	smUpdateSelStatusSubscriptionSuccess(subscriberID, RBTParametersUtils.getParamAsBoolean("DAEMON","REAL_TIME_SELECTIONS", "FALSE"), isPack,false,null);
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
		return success ? m_success : m_failure;
	
	}
	@Override
	public String processSuspendSubscription(String subscriberID, String extraInfo, boolean updateActivationDate)
	{
		Connection conn = getConnection();
		if (conn == null)
		{
			return m_connectionError;
		}
		
		boolean success = false;
		try
		{
			success = SubscriberImpl.processSmSuspendSubscription(conn, subID(subscriberID), extraInfo, updateActivationDate);
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
	public String processSuspendSubscription(String subscriberID, String status, boolean updatePlayerStatus, String extraInfo)
	{
		Connection conn = getConnection();
		if (conn == null)
		{
			return m_connectionError;
		}
		
		boolean success = false;
		
		try
		{
			success = SubscriberImpl.processSmSuspendSubscription(conn, subID(subscriberID), extraInfo,status, updatePlayerStatus);
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
		return addSubscriberSelections(subscriberID, callerID, categories, clipMap,setTime,
				startTime, endTime, status, selectedBy,
				selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime,
				toTime, chargeClassType, smActivation,
				doTODCheck, mode, regexType, subYes,
				promoType, circleID, incrSelCount,
				useDate, transID, OptIn, isTata,
				inLoop, subClass, sub, rbtType,
				selInterval, extraInfo, useUIChargeClass, false, null);
	}
	
	public String smAddSubscriberSelections(String subscriberID,
			String callerID, Categories categories, HashMap clipMap,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType, String circleID, 
			boolean incrSelCount,boolean useDate, String transID, boolean OptIn,
			boolean isTata, boolean inLoop,String subClass, Subscriber sub, int rbtType, String selInterval, HashMap extraInfo, HashMap<String, String> responseParams)
	{
		
		// Added useUIChargeClass & isSmClientModel params and passing as true, true
		String response = addSubscriberSelections(subscriberID, callerID, categories, clipMap,setTime,
				startTime, endTime, status, selectedBy,
				selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime,
				toTime, chargeClassType, smActivation,
				doTODCheck, mode, regexType, subYes,
				promoType, circleID, incrSelCount,
				useDate, transID, OptIn, isTata,
				inLoop, subClass, sub, rbtType,
				selInterval, extraInfo, true, true, responseParams);
		return response;
	}
	
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
			String selInterval, HashMap extraInfo, boolean useUIChargeClass, boolean isSmClientModel, HashMap<String, String> responseParams)
	{
		Connection conn = getConnection();
		if (conn == null)
			return null;

		int count = 0;
		String addResult = null;
		try
		{
			if(sub != null && sub.subYes() != null && sub.subYes().equalsIgnoreCase("Z"))
			{   
				String subscriberExtraInfo = sub.extraInfo();
			    HashMap<String , String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberExtraInfo);
				if(subExtraInfoMap.get("VOLUNTARY")!=null&&subExtraInfoMap.get("VOLUNTARY").equals("SM_SUSPENDED")){
				  logger.info(subscriberID +" is suspended. Returning false.");
				  return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
				}
			}
	
			Date nextChargingDate = null;
			String originalCallerId = null ; 
			if(extraInfo != null && extraInfo.containsKey("CALLER_ID")){
				originalCallerId = (String) extraInfo.get("CALLER_ID") ;
			}
				
				
			Date startDate = startTime;
			String selectInfo = selectionInfo;
			String sel_status = STATE_BASE_ACTIVATION_PENDING;
			int nextPlus = -1;
			boolean updateEndDate = false;
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

			if (callerID != null)
			{
				Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn, subscriberID);
				if (groups != null && groups.length > 0)
				{
					int[] groupIDs = new int[groups.length];
					for (int i = 0; i < groups.length; i++)
					{
						groupIDs[i] = groups[i].groupID();
					}
					GroupMembers groupMember = GroupMembersImpl.getMemberFromGroups(conn, callerID, groupIDs);
					if (groupMember != null)
					{
						for (Groups group : groups)
						{
							if (groupMember.groupID() == group.groupID())
							{
								if (group.preGroupID() != null && group.preGroupID().equals("99")) // Blocked Caller
									return SELECTION_FAILED_CALLER_BLOCKED;
								else if (group.preGroupID() != null && !group.preGroupID().equals("98")) //caller is in caller group. so can be ignored
									return SELECTION_FAILED_CALLER_ALREADY_IN_GROUP;
							}
						}
					}
				}
			}

			if(sub != null && rbtType != 2)
				rbtType = sub.rbtType();
             String subExtraInfo = sub.extraInfo();
             HashMap<String,String> extraInfoMap = DBUtility.getAttributeMapFromXML(subExtraInfo);
             boolean voluntarySuspended = false;
             if(extraInfoMap!=null&&extraInfoMap.containsKey(VOLUNTARY)&&extraInfoMap.get(VOLUNTARY).equalsIgnoreCase("TRUE")){
            	 voluntarySuspended = true;
             }
			if(sub != null && sub.subYes() != null && (sub.subYes().equals("Z") || sub.subYes().equals("z"))&&
					!(sub.subYes().equalsIgnoreCase("Z") && voluntarySuspended))
			{
				logger.info(subscriberID +" is suspended. Returning false.");
				return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
			}

			boolean isSelSuspended = false;
			if (m_checkForSuspendedSelection)
				isSelSuspended = isSelSuspended(subscriberID, callerID);

			if (isSelSuspended)
			{
				logger.info("selection of " + subscriberID + " for " + callerID
						+ " is suspended. Returning false.");
				return SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED;
			} 


			Date endDate = endTime;
			if(endDate == null)
				endDate = m_endDate;

			// If chargeClassType is null, then useUIChargeClass parameter will be ignored
			useUIChargeClass = useUIChargeClass && chargeClassType != null;
			
			String classType = "DEFAULT";
			if (useUIChargeClass)
				classType = chargeClassType;
			else if (categories != null)
				classType = categories.classType();

			Date clipEndTime = null;
			String clipClassType = null;
			String subscriberWavFile = null;
			if(clipMap != null)
			{
				if(clipMap.containsKey("CLIP_CLASS"))
					clipClassType = (String) clipMap.get("CLIP_CLASS");
				if(clipMap.containsKey("CLIP_END"))
					clipEndTime = (Date) clipMap.get("CLIP_END");
				if(clipMap.containsKey("CLIP_WAV"))
					subscriberWavFile = (String)clipMap.get("CLIP_WAV");
			}

			if(subscriberWavFile == null)
			{
				if(status != 90)
					return SELECTION_FAILED_NULL_WAV_FILE;

				subscriberWavFile = "CRICKET";
			}

			if (subYes != null
					&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes.equalsIgnoreCase(STATE_EVENT)))
			{
				if (!isPackActivationPendingForContent(sub, categories, subscriberWavFile, status, callerID))
					sel_status = STATE_TO_BE_ACTIVATED;
			}

			if(subClass != null && m_subOnlyChargeClass != null && m_subOnlyChargeClass.containsKey(subClass))
			{
				chargeClassType = (String) m_subOnlyChargeClass.get(subClass);
				updateEndDate = true;
			}
			if (clipEndTime != null)
			{
				if(categories != null && (categories.type() == DAILY_SHUFFLE
						|| categories.type() == MONTHLY_SHUFFLE))
				{
					endDate = categories.endTime();
					status = 79;
				}

				if (!useUIChargeClass && clipClassType != null
						&& !clipClassType.equalsIgnoreCase("DEFAULT")
						&& classType != null
						&& !clipClassType.equalsIgnoreCase(classType))
				{
					ChargeClass catCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
					ChargeClass clipCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(clipClassType);

					if (catCharge != null && clipCharge != null
							&& catCharge.getAmount() != null && clipCharge.getAmount() != null)
					{
						try
						{
							float firstAmount = Float.parseFloat(catCharge.getAmount());
							float secondAmount = Float.parseFloat(clipCharge.getAmount());

							if ((firstAmount < secondAmount)
									|| (m_overrideChargeClasses != null && m_overrideChargeClasses
											.contains(clipClassType.toLowerCase())))
								classType = clipClassType;
						}
						catch (Throwable e)
						{
						}
					}
					if(clipClassType.startsWith("TRIAL") && categories != null && categories.id() != 26)
						classType = clipClassType;
				}
			}

			if (!useUIChargeClass && chargeClassType != null)
			{
				ChargeClass first = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
				ChargeClass second = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClassType);

				if (first != null && second != null && first.getAmount() != null
						&& second.getAmount() != null)
				{
					try
					{
						float firstAmount = Float.parseFloat(first.getAmount());
						float secondAmount = Float.parseFloat(second.getAmount());

						if (firstAmount <= secondAmount
								|| secondAmount == 0
								|| chargeClassType.equalsIgnoreCase("YOUTHCARD")
								|| chargeClassType.equalsIgnoreCase("DEFAULT_10")
								|| (m_overrideChargeClasses != null && m_overrideChargeClasses.contains(chargeClassType.toLowerCase())))
							classType = chargeClassType;
					}
					catch (Throwable e)
					{
						classType = chargeClassType;
					}
				}
				else
				{
					classType = chargeClassType;
				}

				if (first != null && first.getChargeClass().startsWith("TRIAL") && categories != null && categories.id() != 26)
					classType = first.getChargeClass();
			}

			if (!useUIChargeClass && categories != null
					&& categories.type() == 10 && m_modeChargeClass != null
					&& m_modeChargeClass.containsKey(selectedBy)) 
				classType = (String) m_modeChargeClass.get(selectedBy); 

			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO"))
			{
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(conn, subID(subscriberID), null);
				if (viralSMS != null)
					selectInfo = selectInfo + ":" + "viral";
			}

			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

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
			SubscriberStatus subscriberStatus = getAvailableSelection(conn, subID(subscriberID),
					subID(callerID), subscriberSelections, categories,
					subscriberWavFile, status, fromTime, toTime, startDate, endDate, doTODCheck,
					inLoop, rbtType, selInterval, selectedBy);
			if (subscriberStatus == null)
			{
				logger.info("RBT::no matches found");
				if(inLoop && (categories.type() == SHUFFLE || status == 90 || status == 99 || status == 0)&& !m_putSGSInUGS)
					inLoop = false;
				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;

				subscriberStatus = SubscriberStatusImpl.smSubscriberSelections(conn, subID(subscriberID), subID(callerID), status, rbtType);
				if (subscriberStatus != null)
				{
					if(inLoop && subscriberStatus.categoryType() == SHUFFLE && !m_putSGSInUGS)
						inLoop = false;
				}
//				else // this else will make all first callerID selection as override :), not needed actually
//					inLoop = false;

				/**
				 * @added by sreekar
				 * if user's last selection is a trail selection his next selection should override the old one
				 */
				char loopStatus = getLoopStatusForNewSelection(inLoop, subscriberID, isPrepaid);
				
				//RBT-14044	VF ES - MI Playlist functionality for RBT core
				loopStatus = getLoopStatusForNewMiPlayListSelection(subscriberID,status, callerID, categories.type(), loopStatus);

				String actBy = null;
				if(sub != null)
					actBy = sub.activatedBy();

				if (m_trialChangeSubTypeOnSelection && actBy != null
						&& actBy.equals("TNB")
						&& (subClass != null && subClass.equals("ZERO")))
				{
					if(classType != null && classType.equals("FREE"))					
					{
						sel_status = STATE_BASE_ACTIVATION_PENDING;

						if(!convertSubscriptionTypeTrial(subID(subscriberID), subClass, "DEFAULT", sub))
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

				//Added by Sreekar for Vodafone Spain 20/01/2013
				boolean addSelectionInVoulentarySuspension = false;
				if (CacheManagerUtil.getParametersCacheManager()
						.getParameter(COMMON, "ADD_SELECTION_FOR_VOULENTARY_SUSPENSION_USERS", "FALSE").getValue().equalsIgnoreCase("true"))
					addSelectionInVoulentarySuspension = true;
				
				boolean isPackSel = false;
				String packCosID = null;
				//status 2 has been added by Sreekar for feature RBT-4119 (bug RBT-4291)
				if ((status == 1 || status == 75 || status == 79 || status == 80 || status == 92 || status == 93
						|| status == 95 || status == 2) && rbtType != 2)
				{
					sel_status = STATE_BASE_ACTIVATION_PENDING;

					boolean isSubActive = false;
					if (subYes != null
							&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes.equalsIgnoreCase(STATE_EVENT)))
					{
						if (!isPackActivationPendingForContent(sub, categories, subscriberWavFile, status, callerID))
							isSubActive = true;
					}
					
					if(!isSubActive && sub.subYes().equalsIgnoreCase("Z")){
						String subscriberExtraInfo = sub.extraInfo();
						HashMap<String , String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberExtraInfo);
						if (addSelectionInVoulentarySuspension && subExtraInfoMap != null && subExtraInfoMap.get("VOLUNTARY") != null
								&& subExtraInfoMap.get("VOLUNTARY").equals("TRUE")) {
							isSubActive = true;
						}
					}

					addResult = addSubscriberDownloadRW(subscriberID, subscriberWavFile, categories, null,
							isSubActive, classType, selectedBy, selectInfo, extraInfo, incrSelCount, useUIChargeClass, isSmClientModel, responseParams, null);

					if(addResult.indexOf("SUCCESS:DOWNLOAD_ALREADY_ACTIVE") != -1)
						sel_status = STATE_TO_BE_ACTIVATED;
					else if(addResult.indexOf("SUCCESS:DOWNLOAD_GRACE") != -1)
						sel_status = STATE_BASE_ACTIVATION_PENDING;
					else if(addResult.indexOf("SUCCESS") == -1)
						return addResult;
                    logger.info("ADD RESULT === "+addResult);
					if(addResult.indexOf("SUCCESS")!=-1 && !addResult.equalsIgnoreCase("SUCCESS:DOWNLOAD_ALREADY_ACTIVE")){
						logger.info("Adding Recent Charge Class For Download = "+classType);
					    clipMap.put("RECENT_CLASS_TYPE", classType);
					}
					
					//RBT-16608
					classType=CacheManagerUtil.getParametersCacheManager().getParameter(WEBSERVICE, "SELECTION_DEFAULT_CHARGE_CLASS", "FREE").getValue();
				}
				else
				{
					String subPacks = null;
					HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub.extraInfo());
					if (subExtraInfoMap != null && subExtraInfoMap.containsKey(EXTRA_INFO_PACK))
						subPacks = subExtraInfoMap.get(EXTRA_INFO_PACK);

					String nextClass = null;
					
					if(status == 90){
						nextClass = getNextChargeClass(sub); 
					}
					else if(!useUIChargeClass) {
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
					}

									
					if(nextClass != null && cricClassList.contains(nextClass) && status == 90){
						classType = nextClass;
					}
					else if(nextClass != null && !nextClass.equalsIgnoreCase("DEFAULT") && status != 90){
						classType = nextClass;
					}
					
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
				  
				  if(sub.subYes().equalsIgnoreCase("Z")){
				  String subscriberExtraInfo = sub.extraInfo();
			      HashMap<String , String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberExtraInfo);
			      if (!addSelectionInVoulentarySuspension && subExtraInfoMap.get("VOLUNTARY") != null
							&& subExtraInfoMap.get("VOLUNTARY").equals("TRUE")) {
					 sel_status = STATE_BASE_ACTIVATION_PENDING;
				  }
			    }
				
				if(originalCallerId != null && !originalCallerId.isEmpty()){
					extraInfo.put("CALLER_ID", originalCallerId);
				}  
				count = createSubscriberStatus(subscriberID, callerID, categories.id(), subscriberWavFile,
						setTime, startDate, endDate, status, selectedBy, selectInfo, nextChargingDate,
						prepaid, classType, changeSubType, fromTime, toTime, sel_status,true,
						clipMap, categories.type(),useDate, loopStatus, isTata, nextPlus, rbtType,selInterval, extraInfo, null, false, circleID);

				logger.info("Checking to update num max selections or not. count: "
						+ count + ", isPackSel: " + isPackSel);
				if (isPackSel && count == 1)
					ProvisioningRequestsDao.updateNumMaxSelections(conn, subscriberID, packCosID);
				else if(cricClassList.contains(classType) && incrSelCount && count == 1) {
					SubscriberImpl.updateNumMaxSelections(conn, subscriberID, sub.maxSelections() + 1);
				}

				if(updateEndDate)
				{
					SubscriberImpl.updateEndDate(conn, subID(subscriberID), endDate, null);
				}
			}
			else
			{
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
		else if(count > 0)
			return SELECTION_SUCCESS;
		else
			return SELECTION_FAILED_INTERNAL_ERROR;
	}

	public boolean addSubscriberSelections(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType,
			boolean incrSelCount, boolean OptIn, boolean inLoop,
			String subClass, Subscriber subscriber, String selInterval)
	{
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
		String ret = addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType,
				null, incrSelCount, false, null, OptIn, false, inLoop,
				subClass, subscriber, 0, selInterval);
		if(ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;
	}
	
	//RBT-9873 Added xtraParametersMap for CG flow
	@Override
	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, CosDetails cos,
			boolean isDirectActivation, int rbtType, HashMap extraInfo, String circleId, String refId, boolean isComboRequest, Map<String,String> xtraParametersMap)
	{
		Connection conn = getConnection();
		if(conn == null)
			return null;

		String prepaid = isPrepaid ? "y" : "n";
		Subscriber subscriber = null;
		
		try
		{
			subscriber = SubscriberImpl.getSubscriber(conn, subID(subscriberID));
			if ( !isTNBNewFlow && classType != null
					&& tnbSubscriptionClasses.contains(classType) && endDate == null)
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

			String subscription = STATE_TO_BE_ACTIVATED;
			String activationInfo = actInfo;

			if(isDirectActivation)
				subscription = STATE_ACTIVATED;

			String cosID = null;
			String subscriptionClass = classType;
			if(cos != null)
			{
				cosID = cos.getCosId();
				if ((subscriptionClass == null || subscriptionClass.equalsIgnoreCase("DEFAULT"))
						&& cos.getSubscriptionClass() != null
						&& !cos.getSubscriptionClass().equalsIgnoreCase("DEFAULT"))
					subscriptionClass = cos.getSubscriptionClass();
			}

			if(subscriptionClass == null)
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
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(conn, subID(subscriberID), null);
				if (viralSMS != null)
					activationInfo = activationInfo + ":" + "viral";
			}

			// update ExtraInfo 
			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			String finalRefID = UUID.randomUUID().toString();

			//Added for JIRA-RBT-6321
			if(isDirectActivation && refId != null)
				finalRefID = refId;

			if (subscriber != null)
			{
				String subsciptionYes = subscriber.subYes();
				if (!isDirectActivation
						&& subscriber.endDate().getTime() > getDbTime(conn))
				{
					if (subsciptionYes.equals("B")
							&& (subscriber.rbtType() == TYPE_RBT || subscriber.rbtType() == TYPE_RRBT || subscriber.rbtType() == TYPE_SRBT)
							&& subscriber.rbtType() != rbtType)
					{
						if ((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT)
								|| (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT))
						{
						}
						else
						{
							if (subscriber.rbtType() == TYPE_RBT)
								rbtType = TYPE_RBT_RRBT;
							else if (subscriber.rbtType() == TYPE_SRBT)
								rbtType = TYPE_SRBT_RRBT;
							convertSubscriptionType(subID(subscriberID), subscriber.subscriptionClass(), m_comboSubClass, null, rbtType, true, null, subscriber);
						}
					}
					return subscriber;
				}

				if(!isDirectActivation
						&& (subsciptionYes.equals("D") || subsciptionYes.equals("P")
								|| subsciptionYes.equals("F") || subsciptionYes.equals("x")
								|| subsciptionYes.equals("Z") || subsciptionYes.equals("z")))
					return null;

				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();
				
				boolean success = SubscriberImpl.update(conn, subID(subscriberID), activate,
						null, startDate, endDate, prepaid, null, null, 0,
						activationInfo, subscriptionClass, deactivatedBy,
						deactivationDate, null, subscription, 0, cosID, cosID,
						rbtType, subscriber.language(), subExtraInfo, circleId, finalRefID, isDirectActivation);
				if(startDate == null)
					startDate = new Date(System.currentTimeMillis());
				
				if (success)
				{
					subscriber = new SubscriberImpl(subID(subscriberID), activate,
						null, startDate, m_endDate, prepaid, null, null, 0,
						activationInfo, subscriptionClass, subscription,
						deactivatedBy, deactivationDate, null, 0, cosID, cosID,
						rbtType, subscriber.language(), subscriber.oldClassType(),
						subExtraInfo, circleId, finalRefID);
				}
				else
					subscriber = null;
			}
			else
				subscriber = SubscriberImpl.insert(conn, subID(subscriberID),
						activate, null, startDate, endDate, prepaid, null,
						null, 0, activationInfo, subscriptionClass, null, null,
						null, subscription, 0, cosID, cosID, rbtType, null,
						isDirectActivation, subExtraInfo, circleId, finalRefID);
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
	public boolean isDownloadAllowed(String subscriberId, WebServiceContext task) {
    	RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		ParametersCacheManager parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
    	SubscriberDownloads[] subscriberDownloads = rbtDBManager.getNonDeactiveSubscriberDownloads(subscriberId);
		Parameters downLimitParam = parametersCacheManager.getParameter(
				iRBTConstant.COMMON, "MAX_DOWNLOADS_ALLOWED");
		int noOfDownloadsLimit = 0;
		if(downLimitParam!=null && downLimitParam.getValue()!=null){
			try{
		       noOfDownloadsLimit = Integer.parseInt(downLimitParam.getValue());
			}catch(Exception e){
				return false;
			}
		}
		if(downLimitParam!=null && subscriberDownloads!=null && subscriberDownloads.length >= noOfDownloadsLimit){
			return false;
		}

		return true;
	}
	
	@Override
	public String updateSubscriberId(String newSubscriberId,String subscriberId)
	{
		Connection conn = getConnection();
		if(conn == null)
			return null;
		
		boolean success = false;
		try
		{
			Subscriber sub = getSubscriber(subID(newSubscriberId));
			if(sub != null)
				return "FAILURE:NEW MSISDN ALREADY EXISTS";
			Subscriber subscriber = getSubscriber(subID(subscriberId));
			if(subscriber == null)
				return "FAILURE:MSISDN DOESN'T EXIST";
			if(!isValidPrefix(newSubscriberId))
				return "FAILURE:NEW MSISDN INVALID";
			success = SubscriberImpl.updateSubscriberId(conn,subID(newSubscriberId),subID(subscriberId));
			SubscriberStatusImpl.updateSubscriberId(conn,subID(newSubscriberId),subID(subscriberId));
			SubscriberDownloadsImpl.updateSubscriberId(conn,subID(newSubscriberId),subID(subscriberId));
			GroupsImpl.updateSubscriberId(conn, subID(newSubscriberId), subID(subscriberId));
			ViralSMSTableImpl.updateSubscriberId(conn, subID(newSubscriberId), subID(subscriberId));
			ProvisioningRequestsDao.updateSubscriberID(conn,subID(newSubscriberId),subID(subscriberId));
			if(success)
			{
				Date smsSentdate = new Date();
				ViralSMSTableImpl.insert(conn, subscriberId, smsSentdate, "CHANGEMSISDN", newSubscriberId, null, 0, null, null,null);
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
		return success ? "SUCCESS" : "FAILURE:TECHNICAL FAULT";
	}
	
	@Override
	public String[] getChargeClassForShuffleCatgory(String subscriberId,
			Subscriber consentSubscriber, Categories categories,
			ClipMinimal clip, boolean incrSelCount, String subscriberWavFile,
			boolean isPackSel, String packCosID, String selBy,
			HashMap<String, String> extraInfo, String nextClass,
			String classType) {
		return super.getChargeClassForNonShuffleCatgory(subscriberId, consentSubscriber,
				categories, clip, incrSelCount, subscriberWavFile, isPackSel,
				packCosID, selBy, extraInfo, nextClass, classType);
	}
	
	@Override
	public String getCosChargeClass(Subscriber subscriber, Category category,
			Clip clip, CosDetails cos) {
		String classType= null;
		if (!RBTDBManager.getInstance().isCosToBeIgnored(cos, category, clip)) {
			int selectionCount = 0;
			if (RBTDBManager.getInstance().isSubActive(subscriber))
				selectionCount = subscriber.maxSelections();

			classType = RBTDBManager.getInstance().getChargeClassFromCos(cos,
					selectionCount);
		}
		return classType;
	
	}
}