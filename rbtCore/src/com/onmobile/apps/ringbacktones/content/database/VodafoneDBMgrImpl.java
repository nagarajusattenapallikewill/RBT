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
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

public class VodafoneDBMgrImpl extends RBTDBManager{

	private static Logger logger = Logger.getLogger(VodafoneDBMgrImpl.class);
	
	public static int m_MAX_CALLER_BLOCK_LIMIT = 30;
	static Hashtable selectionModeAdRbtClassTypes = null;
	static Hashtable categoryIdAdRbtClassTypes = null;
	static ArrayList<String> udsSelectionModes = null;
	static HashMap<String, String> srbtFreePromotionChargeClassMap = null;
	private List<String> songBaseCosIdsList = Collections.EMPTY_LIST;
	// JIRA-ID: RBT-13626
	private static Map<String, String> udsOptInChargeClassMap = new HashMap<String, String>();
	private static String udsOptInChargeClass = RBTParametersUtils
			.getParamAsString("COMMON", "UDSOPTIN_CHARGECLASS", null);
	public void init()
	{
		logger.info("inside init");
		
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "SELECTION_MODE_ADRBT_CLASS_TYPE");
		if (param != null) {

			selectionModeAdRbtClassTypes = new Hashtable();
			StringTokenizer stk = new StringTokenizer(param.getValue(), ";");
			while(stk.hasMoreTokens())
			{
				String token = stk.nextToken();
				StringTokenizer stk1 = new StringTokenizer(token, ",");
				if(stk1.hasMoreTokens())
				{
					String mode = stk1.nextToken();
					String classType = null;
					if(stk1.hasMoreTokens())
						classType = stk1.nextToken();
					if(mode != null && classType != null)
						selectionModeAdRbtClassTypes.put(mode, classType);
				}
			}
		}
		logger.info("selectionModeAdRbtClassTypes is : "+selectionModeAdRbtClassTypes);
		param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "CATEGORY_ID_ADRBT_CLASS_TYPE");
		if (param != null) {

			categoryIdAdRbtClassTypes = new Hashtable();
			StringTokenizer stk = new StringTokenizer(param.getValue(), ";");
			while(stk.hasMoreTokens())
			{
				String token = stk.nextToken();
				StringTokenizer stk1 = new StringTokenizer(token, ",");
				if(stk1.hasMoreTokens())
				{
					String catId = stk1.nextToken();
					String classType = null;
					if(stk1.hasMoreTokens())
						classType = stk1.nextToken();
					if(catId != null && classType != null)
						categoryIdAdRbtClassTypes.put(catId, classType);
				}
			}
		}
		logger.info("categoryIdAdRbtClassTypes is : "+categoryIdAdRbtClassTypes);
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "UDS_SELECTION_MODES");
		if (param != null) {

			udsSelectionModes = new ArrayList<String>();
			StringTokenizer stk = new StringTokenizer(param.getValue(), ",");
			while(stk.hasMoreTokens())
			{
				String mode = stk.nextToken();
				udsSelectionModes.add(mode);
			}
		}
		logger.info("UDS Selection modes are : "+udsSelectionModes);
		//
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "SRBT_FREE_PROMOTION_CHARGECLASS_MAP");
		if (param != null) {

			srbtFreePromotionChargeClassMap = new HashMap<String, String>();
			StringTokenizer stk = new StringTokenizer(param.getValue(), ";");
			while(stk.hasMoreTokens())
			{
				String token = stk.nextToken();
				StringTokenizer stk1 = new StringTokenizer(token, ",");
				if(stk1.countTokens() == 2)
				{
					String initChargeClass = stk1.nextToken();
					String finalChargeClass = stk1.nextToken();
					if(initChargeClass != null && finalChargeClass != null)
						srbtFreePromotionChargeClassMap.put(initChargeClass, finalChargeClass);
				}
			}
		}

		logger.info("SRBT Free Selection Charge Class Map is : "+srbtFreePromotionChargeClassMap);
		
		String songBasedCosId = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.COMMON, "SONG_BASED_COS_ID",
						null);
		if (songBasedCosId != null) {
			songBaseCosIdsList = Arrays.asList(songBasedCosId.split(","));
		}
		logger.info("Configured songBaseCosIdsList : " + songBaseCosIdsList);
		
		//
		String udsChargeClassMapStr = RBTParametersUtils.getParamAsString(
				iRBTConstant.COMMON, "UDSOPTIN_CHARGECLASS_MAP", null);
		if (null != udsChargeClassMapStr) {
			udsOptInChargeClassMap = MapUtils.convertToMap(
					udsChargeClassMapStr.toUpperCase(), ",", "=", null);
		}
		logger.info("udsOptInChargeClassMap: " + udsOptInChargeClassMap);

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

		Connection conn = getConnection();
		if (conn == null)
			return null;
		int count = 0;
		Date nextChargingDate = null;
		Date startDate = startTime;
		logger.info("The value of status : - "+ status+" and rbttype - "+rbtType);
		String selectInfo = selectionInfo;
		String sel_status = STATE_BASE_ACTIVATION_PENDING;
		int nextPlus = -1;
		boolean updateEndDate = false;
		if(extraInfo.containsKey(UDS_OPTIN)){
			Map<String, String> subMap =DBUtility.getAttributeMapFromXML(sub.extraInfo());
			if(subMap == null)
				subMap = new HashMap<String, String>();
		
			subMap.put(UDS_OPTIN, (String) extraInfo.get(UDS_OPTIN));
			sub.setExtraInfo(DBUtility.getAttributeXMLFromMap(subMap));
		}
		try
		{
			//RBT-12199
			if(!isunBlockedOrDoubleConsentMode(selectedBy))
			{
				logger.info("The mode "+selectedBy+" is blocked. "+subscriberID+" cannot get selection");
				return SELECTION_FAILED_CALLER_BLOCKED;
			}
			subscriberID = subID(subscriberID);
			callerID = subID(callerID);
			if(subscriberID != null && callerID != null && subscriberID.equals(callerID))
				return SELECTION_FAILED_OWN_NUMBER; 
			
			if (categories != null && com.onmobile.apps.ringbacktones.webservice.common.Utility.isShuffleCategory(categories.type()))
			{
				if (categories.endTime().before(new Date()))
					return SELECTION_FAILED_CATEGORY_EXPIRED;
			}
			
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
			
			if(sub != null && rbtType != 2)
			{
				rbtType = sub.rbtType();
			}
			if(sub != null && sub.subYes() != null && (sub.subYes().equals("Z") || sub.subYes().equals("z")))
			{
				logger.info(subscriberID +" is suspended. Returning false.");
				return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
			}
			boolean isSelSuspended = false;
			if (m_checkForSuspendedSelection) {
				isSelSuspended = isSelSuspended(subscriberID, callerID);
			}
			if (isSelSuspended) {
				logger.info("selection of " + subscriberID + " for " + callerID
						+ " is suspended. Returning false.");
				return SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED;
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
								else
									return SELECTION_FAILED_CALLER_ALREADY_IN_GROUP;
							}
						}
					}
				}
			}
			
			/*
			 * if(freePeriod != 0) { nextChargingDate =
			 * Calendar.getInstance().getTime(); selectInfo = "free:" + selectInfo; }
			 */
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
			String clipGrammar = null;
			String clipClassType = null;
			String subscriberWavFile = null;
			if(clipMap != null)
			{
				if(clipMap.containsKey("CLIP_CLASS"))
					clipClassType = (String) clipMap.get("CLIP_CLASS");
				if(clipMap.containsKey("CLIP_END"))
					clipEndTime = (Date) clipMap.get("CLIP_END");
				if(clipMap.containsKey("CLIP_GRAMMAR"))
					clipGrammar = (String) clipMap.get("CLIP_GRAMMAR");
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
					&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
							.equalsIgnoreCase(STATE_EVENT)))
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
				if (clipEndTime.getTime() < System.currentTimeMillis())
				{
					return SELECTION_FAILED_CLIP_EXPIRED;
				}
				/*				if (freePeriod == 0 && status != 99 && clipEndTime != null)
				 {
				 endDate = clipEndTime;
				 }
				 */				
				if(categories != null && (categories.type() == DAILY_SHUFFLE
						|| categories.type() == MONTHLY_SHUFFLE))
				{
					endDate = categories.endTime();
					status = 79;
				}

				/*if (clipGrammar != null
				  && clipGrammar.equalsIgnoreCase("UGC"))
				  if (selectInfo == null)
				  selectInfo = "UGC";
				  else
				  selectInfo += ":UGC";
				 */
				
				if (!useUIChargeClass && clipClassType != null
						&& !clipClassType.equalsIgnoreCase("DEFAULT")
						&& classType != null
						&& !clipClassType.equalsIgnoreCase(classType))
				{
					ChargeClass catCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
					ChargeClass clipCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(clipClassType);

					if (catCharge != null && clipCharge != null
							&& catCharge.getAmount() != null
							&& clipCharge.getAmount() != null)
					{
						try
						{
							int firstAmount = Integer.parseInt(catCharge.getAmount());
							int secondAmount = Integer
							.parseInt(clipCharge.getAmount());

							if ((firstAmount < secondAmount)
									|| (m_overrideChargeClasses != null && m_overrideChargeClasses
											.contains(clipClassType
													.toLowerCase())))
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
						int firstAmount = Integer.parseInt(first.getAmount());
						int secondAmount = Integer.parseInt(second.getAmount());

						if (firstAmount <= secondAmount
								|| secondAmount == 0
								|| chargeClassType
								.equalsIgnoreCase("YOUTHCARD")
								|| chargeClassType
								.equalsIgnoreCase("DEFAULT_10")
								|| (m_overrideChargeClasses != null && m_overrideChargeClasses
										.contains(chargeClassType.toLowerCase())))
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
				{
					classType = first.getChargeClass();
				}
			}

			if (!useUIChargeClass && categories != null
					&& categories.type() == 10 && m_modeChargeClass != null
					&& m_modeChargeClass.containsKey(selectedBy)) 
			{ 
				classType = (String) m_modeChargeClass.get(selectedBy); 
			} 

			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO"))
			{
				ViralSMSTable viralSMS = ViralSMSTableImpl
				.getViralPromotion(conn, subID(subscriberID), null);
				if (viralSMS != null)
				{
					selectInfo = selectInfo + ":" + "viral";
				}
			}

			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			//int count = 0;

			String afterTrialClassType = "DEFAULT"; 
			if(OptIn) 
				afterTrialClassType = "DEFAULT_OPTIN"; 

				/**
				 *  If user enabled UDS , then all his selections should go in Loop
				 */
				if(!inLoop) 
				{	
					HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub.extraInfo());
					if((subExtraInfoMap!= null && subExtraInfoMap.containsKey(UDS_OPTIN)) || 
							(extraInfo != null && extraInfo.containsKey(UDS_OPTIN)))
					{//JIRA-ID: RBT-13626
						String premiumChargeClass = com.onmobile.apps.ringbacktones.webservice.common.Utility
								.isUDSUser(subExtraInfoMap, false);
						inLoop = (premiumChargeClass != null);			
					}
				}

				if (selInterval != null && status != 80) {

					if (selInterval.startsWith("W") || selInterval.startsWith("M")) {
						//RBT-11363
		                 if(!(categories!=null && categories.type() == DAILY_SHUFFLE))
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
			logger.info("The value of status : - "+ status+" and rbttype - "+rbtType);
			subscriberStatus = getAvailableSelection(
					conn,
					subID(subscriberID),
					subID(callerID),
					subscriberSelections,
					categories,
					subscriberWavFile,
					status,
					fromTime,
					toTime, startDate, endDate,
					doTODCheck, inLoop, rbtType,selInterval, selectedBy);
			if (subscriberStatus == null)
			{
				logger.info("RBT::no matches found");
				//System.out.println("111111111111111111");
				if(inLoop && (Utility.isShuffleCategory(categories.type()) || status == 90 || status == 99 || status == 0))
					inLoop = false;
				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;
				
				subscriberStatus = SubscriberStatusImpl
				.smSubscriberSelections(conn, subID(subscriberID),
						subID(callerID), status, rbtType);
				if (subscriberStatus != null)
				{
					if(inLoop && Utility.isShuffleCategory(subscriberStatus.categoryType()))
						inLoop = false;
				}
//				else // this else will make all first callerID selection as override :), not needed actually
//					inLoop = false;
				
				/**
				 * @added by sreekar
				 * if user's last selection is a trail selection his next selection should override the old one
				 */
				char loopStatus = getLoopStatusForNewSelection(inLoop, subscriberID, isPrepaid);

				String actBy = null;
				if(sub != null)
				{
					actBy = sub.activatedBy();
//					oldSubClass = sub.oldClassType();
				}
				if (m_trialChangeSubTypeOnSelection
						&& actBy != null
						&& actBy.equals("TNB")
						&& (subClass != null && subClass.equals("ZERO"))){
					if(classType != null && classType.equals("FREE"))					
					{
						sel_status = STATE_BASE_ACTIVATION_PENDING;

						if(!convertSubscriptionTypeTrial(subID(subscriberID), subClass, "DEFAULT", sub))
							return SELECTION_FAILED_TNB_TO_DEFAULT_FAILED;
					}
				}

				boolean isPackSel = false;
				String packCosID = null;
				if (m_overridableCategoryTypes.contains(""+categories.type()) || m_overridableSelectionStatus.contains(""+ status))
					incrSelCount = false;
				else if (!useUIChargeClass)
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
								if (cosDetail.getFreeSongs() == 0 || cosDetail.getFreeSongs() > selCount)
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

					if (nextClass == null)
						return SELECTION_FAILED_INTERNAL_ERROR;
					if (!nextClass.equalsIgnoreCase("DEFAULT"))
						classType = nextClass;
				}
				
				if (!useUIChargeClass
						&& m_overrideChargeClasses != null
						&& chargeClassType != null
						&& m_overrideChargeClasses.contains(chargeClassType.toLowerCase()))
					classType = chargeClassType;
				
				if(rbtType == 1)
				{
					if(categories.id() == 1)
						return SELECTION_FAILED_ADRBT_FOR_PROFILES_OR_CORPORATE;
					if(categories.type() == 10 || categories.type() == 12 || categories.type() == 0 || categories.type() == 11 || categories.type() == 20)
						return SELECTION_FAILED_ADRBT_FOR_SHUFFLES;
					if(callerID != null)
						return SELECTION_FAILED_ADRBT_FOR_SPECIFIC_CALLER;
					
					if (!useUIChargeClass)
					{
						if(!categories.classType().equals("FREE") && status != 99)
						{
							classType = "ADRBT";
						}
						if(selectionModeAdRbtClassTypes != null && selectedBy != null && selectionModeAdRbtClassTypes.containsKey(selectedBy))
						{
							classType = (String) selectionModeAdRbtClassTypes.get(selectedBy);
						}
						if(categoryIdAdRbtClassTypes != null && categories != null && categoryIdAdRbtClassTypes.containsKey(categories.id()+""))
						{
							classType = (String) categoryIdAdRbtClassTypes.get(categories.id()+"");
						}
					}
				}
				
				if (!useUIChargeClass)
				{
					if(status == 80 && rbtType == 2)
					{
						logger.info("Im in corporate selection");
						classType = clipClassType;
					}
					else
					{
						for(int i=0;subscriberSelections != null && i<subscriberSelections.length;i++)
						{
							logger.info("The value of sel type:- "+subscriberSelections[i].selType());
							if(subscriberSelections[i].selType() == 2)
							{
								HashMap selectionExtraInfo = DBUtility.getAttributeMapFromXML(subscriberSelections[i].extraInfo());
								int campaignId = -1;
								logger.info("The value of selection extrainfo - "+selectionExtraInfo);
								if(selectionExtraInfo != null && selectionExtraInfo.containsKey(iRBTConstant.CAMPAIGN_ID) && selectionExtraInfo.get(iRBTConstant.CAMPAIGN_ID) != null)
								{
									logger.info("The value of selection extrainfo - "+selectionExtraInfo.toString());
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
									logger.info("The value of m_corporateDiscountChargeClass id - "+m_corporateDiscountChargeClass);
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

				if(transID != null)
				{
					selectInfo +=":transid:"+transID+":"; 
					if(sel_status.equals(STATE_TO_BE_ACTIVATED))
						sel_status = STATE_UN;
				}
				
				String checkSelStatus = checkSelectionLimit(subscriberSelections, subID(callerID), inLoop);
				if(!checkSelStatus.equalsIgnoreCase("SUCCESS"))
					return checkSelStatus;

				//For UDS subscriber, if selection amount is not free, the UDS charge class will be overridden
				HashMap<String, String> subExtraInfo = DBUtility.getAttributeMapFromXML(sub.extraInfo());
				
				//If user is song base user, then UDS chargeclass will not be overridden
				boolean isSongBaseSubscriber = songBaseCosIdsList.contains(sub.cosID());

				// Change in logic for UDS User
				// Validation will happen it by the UDS_OPTIN in extra info column
				// If the UDS_OPTIN present in the subscriber extra info column and the
				// key value is either true or the configured charge class then he is a
				// UDS user and the corresponding charge class will be passed & update
				// in the Subscriber table.
				// JIRA-ID: RBT-13626
				String premiumChargeClass = null;
				boolean isUDSSubscriber = false;
				premiumChargeClass = Utility.isUDSUser(subExtraInfo, false);
				isUDSSubscriber = (premiumChargeClass != null);
				
				
				if (!isSongBaseSubscriber && isUDSSubscriber && categories != null 
						&& (categories.type() == 5 || categories.type() == 7) && status != 90 && status != 99)
				{
					if (udsSelectionModes != null && selectedBy != null && udsSelectionModes.contains(selectedBy) && !useUIChargeClass)
					{
						ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
						if (chargeClass != null)
						{
							int amount = Integer.parseInt(chargeClass.getAmount());
							String udsChargeClass = getUDSOPTINChargeClass(
									subExtraInfo, false); 
							//RBTParametersUtils.getParamAsString("COMMON", "UDSOPTIN_CHARGECLASS", null); 
							if (amount != 0 && udsChargeClass != null && !udsChargeClass.equalsIgnoreCase("NULL"))
								classType = udsChargeClass;
						}
					}
					else if(!useUIChargeClass)
						classType = getUDSOPTINChargeClass(subExtraInfo, false);
						//CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "UDSOPTIN_CHARGECLASS").getValue();

				}

				//Added the grace selection deact mode for JIRA-RBT-6338
				String graceDeselectedBy = selectedBy;
				Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
				if(parameter != null && parameter.getValue() != null)
					graceDeselectedBy = parameter.getValue();
				
				if (!com.onmobile.apps.ringbacktones.services.common.Utility.isThirdPartyConfirmationRequired(selectedBy, extraInfo)) {
					SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn, subID(subscriberID), 
							subID(callerID), status, fromTime, toTime, graceDeselectedBy, rbtType);
				}
				
				if(selectedBy != null && selectedBy.equalsIgnoreCase("SRBT") && srbtFreePromotionChargeClassMap != null && srbtFreePromotionChargeClassMap.size() > 0 && srbtFreePromotionChargeClassMap.containsKey(classType))
					classType = getSRBTPromoChargeClass(classType);
				count = createSubscriberStatus(subscriberID, callerID, categories.id(), subscriberWavFile,
						setTime, startDate, endDate, status, selectedBy, selectInfo, nextChargingDate,
						prepaid, classType, changeSubType, fromTime, toTime, sel_status,true,
						clipMap, categories.type(),useDate, loopStatus, isTata, nextPlus, rbtType, selInterval, extraInfo, refID, isDirectActivation, circleID, sub, useUIChargeClass, false);
				logger.info("Checking to update num max selections or not."
						+ " count: " + count + ", isPackSel: " + isPackSel
						+ " incrSelCount: " + incrSelCount);
				if (incrSelCount && isPackSel && count == 1)
					ProvisioningRequestsDao.updateNumMaxSelections(conn, subscriberID, packCosID);
				else if (incrSelCount && count == 1)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberID));
				
				if(updateEndDate)
				{
					SubscriberImpl.updateEndDate(conn, subID(subscriberID), endDate, null);
				}

			}
			else if (subscriberStatus.selType() == 2 && rbtType != 2)
			{
				//FIXME: Temporary fix for vodafone corporate user's selections issue, needs to be changed
				return RBT_CORPORATE_NOTALLOW_SELECTION;
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
		return count > 0 ? SELECTION_SUCCESS : SELECTION_FAILED_INTERNAL_ERROR;
	}

	
	
	private String getSRBTPromoChargeClass(String classType)
	{
		if(classType== null)
			return null;
		String returnClassType =  srbtFreePromotionChargeClassMap.get(classType);
		if(returnClassType != null)
			return returnClassType;
		else
			return classType;
	}

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
		String ret = addSubscriberSelections(subscriberID,
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
	//RBT-9873 Added xtraParametersMap for CG flow
	@Override
	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid, int activationTimePeriod,
			int freePeriod, String actInfo, String classType,
			boolean smActivation, CosDetails cos, boolean isDirectActivation, int rbtType,HashMap extraInfo, String circleId, String refId, boolean isComboRequest, Map<String,String> xtraParametersMap){
		
		Connection conn = getConnection();
		if(conn == null)
			return null;
		
		String prepaid = "n";
		if(isPrepaid)
			prepaid = "y";

		Subscriber subscriber = null;
		try
		{
			 subscriber = SubscriberImpl.getSubscriber(conn, subID(subscriberID));
			 if( !isTNBNewFlow && classType != null
					&& tnbSubscriptionClasses.contains(classType) && endDate == null) {
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(classType); 
                endDate = getNextDate(subClass.getSubscriptionPeriod()); 
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, +1);
				endDate = endCal.getTime();
			}
			else if(m_subOnlyChargeClass != null && m_subOnlyChargeClass.containsKey(classType)) {
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());

			}
			
		//	String circleId = getCircleId(subscriberID); 
			if(cos == null)
				cos = super.getCos(null, subscriberID, subscriber, circleId, isPrepaid?"y":"n", activate, classType);
			
/*			if(cos != null && !cos.isDefault())
				endDate = cos.endDate();
				*/
			if(cos != null && !cos.isDefaultCos()) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, cos.getValidDays()-1);
				endDate = cal.getTime();
				if(endDate.after(cos.getEndDate()))
					endDate = cos.getEndDate();
			}
			String subscription = STATE_TO_BE_ACTIVATED;
			String activationInfo = actInfo;

			if(isDirectActivation) {
				// subscription = "S";
				subscription = STATE_ACTIVATED;
			}

			String cosID = null;

			if(cos != null)
				cosID = cos.getCosId();

			String subscriptionClass = classType;
			if(classType == null)
				subscriptionClass = "DEFAULT";
			if(cos != null && !cos.isDefaultCos())
				subscriptionClass  = cos.getSubscriptionClass();

			SubscriberPromo subscriberPromo = SubscriberPromoImpl.getActiveSubscriberPromo(conn,
					subID(subscriberID), "ICARD");
			if(subscriberPromo != null) {
				if(subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();

				SubscriberPromoImpl.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(
						conn, subID(subscriberID), null);
				if (viralSMS != null) {
					activationInfo = activationInfo + ":" + "viral";
				}
			}
			
			// update ExtraInfo
			if(_preCallPrompt != null && extraInfo != null && !extraInfo.containsKey(EXTRA_INFO_INTRO_PROMPT_FLAG))
				extraInfo.put(EXTRA_INFO_INTRO_PROMPT_FLAG, _preCallPrompt);
				
			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			String finalRefID = UUID.randomUUID().toString();

			//Added for JIRA-RBT-6321
			if(isDirectActivation && refId != null)
				finalRefID = refId;
			
			//RBT-12199
			if (!isunBlockedOrDoubleConsentMode(activate))
			{
				logger.info("The mode "+activate+" is blocked. "+subscriberID+" cannot be activated");
				return null;
			}
			if (subscriber != null) {

				String subsciptionYes = subscriber.subYes();
				if (!isDirectActivation
						&& subscriber.endDate().getTime() > getDbTime(conn)) {
					if (subsciptionYes.equals("B")
							&& (subscriber.rbtType() == TYPE_RBT
									|| subscriber.rbtType() == TYPE_RRBT || subscriber
									.rbtType() == TYPE_SRBT)
							&& subscriber.rbtType() != rbtType) {
						if ((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT)
								|| (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT)) {

						} else {
							if (subscriber.rbtType() == TYPE_RBT)
								rbtType = TYPE_RBT_RRBT;
							else if (subscriber.rbtType() == TYPE_SRBT)
								rbtType = TYPE_SRBT_RRBT;

							convertSubscriptionType(subID(subscriberID), subscriber
									.subscriptionClass(), m_comboSubClass,
									null, rbtType, true, null, subscriber);

						}
					}
					return subscriber;
				}
				if(!isDirectActivation
						&& (subsciptionYes.equals("D") || subsciptionYes.equals("P")
								|| subsciptionYes.equals("F") || subsciptionYes.equals("x")
								|| subsciptionYes.equals("Z") || subsciptionYes.equals("z"))) {
					return null;
				}
				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();
				boolean success = false;
				//RBT-9873 Added xtraParametersMap for CG flow
				Subscriber subscriberConsent = checkModeAndInsertIntoConsent(subID(subscriberID),
						activate, startDate, endDate, isDirectActivation,
						rbtType, conn, prepaid, subscription, activationInfo,
						cosID, subscriptionClass, finalRefID, extraInfo, circleId, isComboRequest,xtraParametersMap);
				if(subscriberConsent != null)
				{
					success = true;
					subscriber = subscriberConsent;
					finalRefID = subscriberConsent.refID();
					boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils.getParamAsBoolean(COMMON, "CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE");
					if(isCGIntegrationFlowForBsnlEast){
						return subscriber;
					}
				}
				else{
					if (null != xtraParametersMap) {
						String consentHit = xtraParametersMap
								.remove("CONSENT_HIT_FAILURE_FOR_BSNL_EAST");
						if (null != consentHit
								&& consentHit.equalsIgnoreCase("true")) {
							logger.info("consent hit failed for bsnl cg east");
							return null;
						}
					}
					success = SubscriberImpl.update(conn, subID(subscriberID), activate, null, startDate,
							endDate, prepaid, null, null, 0, activationInfo, subscriptionClass,
							deactivatedBy, deactivationDate, null, subscription, 0, cosID, cosID,
							rbtType, subscriber.language(), subExtraInfo, isDirectActivation, circleId, finalRefID);
				}
				if(startDate == null)
					startDate = new Date(System.currentTimeMillis());
				
				if (success)
				{
					subscriber = new SubscriberImpl(subID(subscriberID), activate, null, startDate,
						m_endDate, prepaid, null, null, 0, activationInfo, subscriptionClass,
						subscription, deactivatedBy, deactivationDate, null, 0, cosID, cosID,
						rbtType, subscriber.language(), subscriber.oldClassType(), subExtraInfo, circleId, finalRefID);
				}
				else
				{
					subscriber = null;
				}
			}
			else
			{
				//RBT-9873 Added xtraParametersMap for CG flow
				subscriber = checkModeAndInsertIntoConsent(subID(subscriberID),
						activate, startDate, endDate, isDirectActivation,
						rbtType, conn, prepaid, subscription, activationInfo,
						cosID, subscriptionClass, finalRefID, extraInfo, circleId, isComboRequest,xtraParametersMap);

				if(subscriber == null){
					if (null != xtraParametersMap) {
						String consentHit = xtraParametersMap
								.remove("CONSENT_HIT_FAILURE_FOR_BSNL_EAST");
						if (null != consentHit
								&& consentHit.equalsIgnoreCase("true")) {
							logger.info("consent hit failed for bsnl cg east");
							return subscriber;
						}
					}
					subscriber = SubscriberImpl.insert(conn, subID(subscriberID), activate, null,
							startDate, endDate, prepaid, null, null, 0, activationInfo,
							subscriptionClass, null, null, null, subscription, 0, cosID, cosID,
							rbtType, null, isDirectActivation, subExtraInfo, circleId, finalRefID);
					
				}
			}
			if(isDirectActivation)
			{
				boolean isRealTime = false;
				Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON", "REAL_TIME_SELECTIONS");
				if(param != null && param.getValue().equalsIgnoreCase("true"))
					isRealTime = true;
				
				smUpdateSelStatusSubscriptionSuccess(subID(subscriberID), isRealTime);
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
		return subscriber;
	}

	public Subscriber trialActivateSubscriber(String subscriberID,
			String activate, Date date, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, String selClass,
			String subscriptionType, String circleId)
	{
		
		int rbtType = 0;

		Connection conn = getConnection();
		if (conn == null)
			return null;
		
		String prepaid = "n";
		if (isPrepaid)
			prepaid = "y";

		Date endDate = null;
		Subscriber subscriber = null;
		try
		{
			 subscriber = SubscriberImpl.getSubscriber(conn, subID(subscriberID));
			 if (activate.equalsIgnoreCase("TNB") && classType != null
					&& classType.equalsIgnoreCase("ZERO"))
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
				if (chargeClass != null && chargeClass.getSelectionPeriod() != null
						&& chargeClass.getSelectionPeriod().startsWith("D"))
				{
					String selectionPeriod = chargeClass.getSelectionPeriod()
					.substring(1);
					if ("OPTIN".equalsIgnoreCase(subscriptionType))
					{
						Calendar endCal = Calendar.getInstance();
						endCal.add(Calendar.DATE,
								Integer.parseInt(selectionPeriod) - 1);
						endDate = endCal.getTime();
						if(actInfo == null)
							actInfo = ":optin:";
						else
							actInfo = actInfo + ":optin:";

					}
					else if ("OPTOUT".equalsIgnoreCase(subscriptionType))
					{
						endDate = m_endDate;
					}
				}
			}
			else
			{
				endDate = m_endDate;
			}

			Date nextChargingDate = null;
			Date lastAccessDate = null;
			Date activationDate = null;
			String subscription = STATE_TO_BE_ACTIVATED;
			String activationInfo = actInfo;
			CosDetails cos = getCos(null, subscriberID, subscriber, circleId, isPrepaid?"y":"n", activate, classType);
			if(cos != null && !cos.isDefaultCos()) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, cos.getValidDays()-1);
				endDate = cal.getTime();
				if(endDate.after(cos.getEndDate()))
					endDate = cos.getEndDate();
			}
			String cosID = null;
			if(cos != null)
				cosID = cos.getCosId();


			/*
			 * if(freePeriod != 0) { nextChargingDate =
			 * Calendar.getInstance().getTime(); lastAccessDate =
			 * Calendar.getInstance().getTime(); activationDate =
			 * Calendar.getInstance().getTime(); subscription = "N"; activationInfo =
			 * "free:" + activationInfo; }
			 */

			String subscriptionClass = classType;
			if (classType == null)
				subscriptionClass = "DEFAULT";

			SubscriberPromo subscriberPromo = SubscriberPromoImpl
			.getActiveSubscriberPromo(conn, subID(subscriberID), "ICARD");
			if (subscriberPromo != null)
			{
				if (subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();

				SubscriberPromoImpl.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO"))
			{
				ViralSMSTable viralSMS = ViralSMSTableImpl
				.getViralPromotion(conn, subID(subscriberID), null);
				if (viralSMS != null)
				{
					activationInfo = activationInfo + ":" + "viral";
				}
			}

			if (subscriber != null)
			{
				String subsciptionYes = subscriber.subYes();
				if (subscriber.endDate().getTime() > getDbTime(conn)){
					 if(subsciptionYes.equals("B") && (subscriber.rbtType() == TYPE_RBT || subscriber.rbtType() == TYPE_RRBT || subscriber.rbtType() == TYPE_SRBT) && subscriber.rbtType() != rbtType){
						 if((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT) || (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT)){
							 
						 }
						 else{
							 if(subscriber.rbtType() == TYPE_RBT) 
                                 rbtType = TYPE_RBT_RRBT; 
							 else if(subscriber.rbtType() == TYPE_SRBT) 
                                 rbtType = TYPE_SRBT_RRBT; 
							 convertSubscriptionType(subID(subscriberID), subscriber.subscriptionClass(), m_comboSubClass, null, rbtType, true, null, subscriber); 
						 }
					 }
					 return subscriber;
				}
				
				//String subsciptionYes = subscriber.subYes();
				if (subsciptionYes.equals("D") || subsciptionYes.equals("P")
						|| subsciptionYes.equals("F")  || subsciptionYes.equals("x")|| subsciptionYes.equals("Z") || subsciptionYes.equals("z"))
					return null;
				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();
				String refID = UUID.randomUUID().toString();
				SubscriberImpl.update(conn, subID(subscriberID), activate, null,
						date, endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo,
						subscriptionClass, deactivatedBy,
						deactivationDate, activationDate,
						subscription, 0, cosID, cosID, 0, subscriber.language(), null, circleId, refID, false);
				Date startDate = date;
				if (date == null)
					startDate = new Date(System.currentTimeMillis());
				subscriber = new SubscriberImpl(subID(subscriberID), activate,
						null, startDate, m_endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						subscription, deactivatedBy, deactivationDate,
						activationDate, 0, cosID, cosID, rbtType, subscriber.language(),subscriber.oldClassType(),null, circleId, refID);
			}
			else
				subscriber = SubscriberImpl.insert(conn, subID(subscriberID),
						activate, null, date, endDate,
						prepaid, lastAccessDate,
						nextChargingDate, 0,
						activationInfo,
						subscriptionClass, null, null,
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

	public String getNextChargeClass(Subscriber subscriber)
	{
		if(!isSubActive(subscriber) || subscriber.cosID() == null)
			return null;
		String subscriberId = subscriber.subID();
		String subCosId = subscriber.cosID();
		HashMap<String, String> subExtraInfo = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
		// Change in logic for UDS User
		// Validation will happen it by the UDS_OPTIN in extra info column
		// If the UDS_OPTIN present in the subscriber extra info column and the
		// key value is either true or the configured charge class then he is a
		// UDS user and the corresponding charge class will be passed & update
		// in the Subscriber table.
		// JIRA-ID: RBT-13626
		String premiumChargeClass = null;
		boolean isUDSSubscriber = false;
		premiumChargeClass = Utility.isUDSUser(subExtraInfo, false);
		isUDSSubscriber = (premiumChargeClass != null);
		
		//If user is song base user, then UDS chargeclass will not be overridden
		boolean isSongBaseSubscriber = songBaseCosIdsList.contains(subscriber.cosID());
		logger.info("Getting chargeClass for Subscriber: " + subscriberId + ", cosId: " + subCosId
				+ ", isSongBaseSubscriber: " + isSongBaseSubscriber
				+ ", isUDSSubscriber: " + isUDSSubscriber);

		if (isUDSSubscriber && !isSongBaseSubscriber)
		{
			String classType = getUDSOPTINChargeClass(subExtraInfo, false); 
			//CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "UDSOPTIN_CHARGECLASS", null);
			if (classType != null && !classType.equalsIgnoreCase("NULL")) {
				logger.info("Returning chargeClass: " + classType);
				return classType;
			}
		}
		String requestType = null;
		if (subExtraInfo != null && subExtraInfo.containsKey(iRBTConstant.EXTRA_INFO_REQUEST_TYPE)) {
			requestType = (String) subExtraInfo
					.get(iRBTConstant.EXTRA_INFO_REQUEST_TYPE);
		}
		if (requestType != null && requestType.equalsIgnoreCase("UPGRADE")) {
			if (subExtraInfo.containsKey(EXTRA_INFO_COS_ID)) {
				subCosId = subExtraInfo.get(EXTRA_INFO_COS_ID);
			}
		}
		CosDetails cosObject = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subCosId);
		int selCount = subscriber.maxSelections();
		return (getChargeClassFromCos(cosObject,selCount));
	}
	
	private String getUDSOPTINChargeClass(
			HashMap<String, String> subExtraInfoMap,
			boolean donotValidatePremiumParam) {
		String returnString = null;
		if (null == subExtraInfoMap || subExtraInfoMap.isEmpty()
				|| !subExtraInfoMap.containsKey(iRBTConstant.UDS_OPTIN)) {
			return null;
		}
		String UDSType = subExtraInfoMap.get(iRBTConstant.UDS_OPTIN);
		if (!udsOptInChargeClassMap.isEmpty()) {
			UDSType = UDSType.toUpperCase();
			returnString = udsOptInChargeClassMap.get(UDSType);
		} else {
			if (!subExtraInfoMap.get(iRBTConstant.UDS_OPTIN).equalsIgnoreCase(
					"true")) {
				return null;
			}
			// Here we are returning the null string because for other
			// operators there shouldn't be any validation happen
			// so we should consider that as true.
			returnString = donotValidatePremiumParam
					|| udsOptInChargeClass == null ? "NULL"
					: udsOptInChargeClass;
		}
		return returnString;
	}
}