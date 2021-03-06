package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
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
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;


public class AirtelDbMgrImpl extends RBTDBManager{

	private static Logger logger = Logger.getLogger(VirginDbMgrImpl.class);
	
	private static String m_success = "SUCCESS";
	private static String m_failure = "FAILURE";
	
	public void init(){
		
		logger.info("inside init method");
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
		String selectInfo = selectionInfo;
		String sel_status = STATE_BASE_ACTIVATION_PENDING;
		int nextPlus = -1;
		HashMap subscriberExtraInfo = new HashMap();
		boolean updateEndDate = false;
		boolean isVoluntarySuspendedSub = false;
		try
		{
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
				subscriberExtraInfo = getExtraInfoMap(sub);
				if(subscriberExtraInfo != null && subscriberExtraInfo.containsKey(VOLUNTARY))
				{
					isVoluntarySuspendedSub = (""+subscriberExtraInfo.get(VOLUNTARY)).equalsIgnoreCase("true");
				}
				if(!isVoluntarySuspendedSub)
				{
					logger.info(subscriberID +" is suspended. Returning false.");
					return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
				}
				else
				{
					sel_status = STATE_TO_BE_ACTIVATED;
				}
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
				List<String> newViralSMSTypeList = new ArrayList<String>();
				newViralSMSTypeList.add("BASIC");
				newViralSMSTypeList.add("CRICKET");
				ViralSMSTable viralSMS = null;
				if (isViralSmsTypeListForNewTable(newViralSMSTypeList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
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
			/**
			 * Since Sprint 4 RBT 2.0, RBT 15670
			 * One more parameter udpId has been added in
			 * getSubscriberSelections method. If udpId is present then 
			 * query will filter it with udpId also otherwise old flow.
			 */
			String udpId = null;
			/*if(extraInfo.containsKey(WebServiceConstants.param_udpId))
				udpId = (String) extraInfo.get(WebServiceConstants.param_udpId);*/
			SubscriberStatus[] subscriberSelections = SubscriberStatusImpl.getSubscriberSelections(conn, subID(subscriberID), subID(callerID), rbtType, udpId);
			
			if(!inLoop && status == 1) // If user opted for UDS
			{	
				HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub.extraInfo());
				if(subExtraInfoMap!= null && subExtraInfoMap.containsKey(UDS_OPTIN))
					inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN)).equalsIgnoreCase("TRUE");
				if(inLoop)
				{
					if(isShufflePresentSelection(subID(subscriberID), callerID, 0))
						inLoop = false;
					else if(categories.type() == 0 || categories.type() == 10 || categories.type() == 11 || categories.type() == 12 || categories.type() == 20)
								return SELECTION_FAILED_SHUFFLES_FOR_UDA_OPTIN;
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

			/* time of the day changes */
			SubscriberStatus subscriberStatus = null;
			
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
				if(inLoop && (categories.type() == SHUFFLE || status == 90 || status == 99 || status == 0))
					inLoop = false;
				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;
				
				subscriberStatus = SubscriberStatusImpl
				.smSubscriberSelections(conn, subID(subscriberID),
						subID(callerID), status, rbtType);
				if (subscriberStatus != null)
				{
					if(inLoop && subscriberStatus.categoryType() == SHUFFLE)
						inLoop = false;
				}
//				else // this else will make all first callerID selection as override :), not needed actually
//					inLoop = false;
				
				
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

					if(nextClass == null)
						return SELECTION_FAILED_INTERNAL_ERROR;
					if(!nextClass.equalsIgnoreCase("DEFAULT"))
						classType = nextClass;
				}
				
				if (!useUIChargeClass
						&& m_overrideChargeClasses != null
						&& chargeClassType != null
						&& m_overrideChargeClasses.contains(chargeClassType.toLowerCase()))
					classType = chargeClassType;
				
				if(!useUIChargeClass)
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

				//Added by Deepak kumar for IBM Integration
				if(callerID != null){
					if(Utility.isShuffleCategory(categories.type())){
						return SELECTION_FAILED_CALLER_BLOCKED;
					}else{
						inLoop  = false;
						classType = classType+"_CALLERID";
					}
				}else if(Utility.isShuffleCategory(categories.type())){
				 	 classType = classType+"_SHUFFLE";
				}else if(inLoop){
					 classType = classType+"_LOOP";
				}

				String checkSelStatus = checkSelectionLimit(subscriberSelections, subID(callerID), inLoop);
				if(!checkSelStatus.equalsIgnoreCase("SUCCESS"))
					return checkSelStatus;

				/**
				 * @added by sreekar
				 * if user's last selection is a trail selection his next selection should override the old one
				 */
				char loopStatus = getLoopStatusForNewSelection(inLoop, subscriberID, isPrepaid);

				//Added the grace selection deact mode for JIRA-RBT-6338
				String graceDeselectedBy = selectedBy;
				Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
				if(parameter != null && parameter.getValue() != null)
					graceDeselectedBy = parameter.getValue();
				
				SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn, subID(subscriberID), 
						subID(callerID), status, fromTime, toTime, graceDeselectedBy, rbtType);
				
				count = createSubscriberStatus(subscriberID, callerID, categories.id(), subscriberWavFile,
						setTime, startDate, endDate, status, selectedBy, selectInfo, nextChargingDate,
						prepaid, classType, changeSubType, fromTime, toTime, sel_status,true,
						clipMap, categories.type(),useDate, loopStatus, isTata, nextPlus, rbtType, selInterval, extraInfo, refID, isDirectActivation, circleID);
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
	
	
	// Added on 24/12 .Hariharan. 
	
	 
	 
	//RBT-9873 Added xtraParametersMap for CG flow
	@Override
	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid, int activationTimePeriod,
			int freePeriod, String actInfo, String classType,
			boolean smActivation, CosDetails cos, boolean isDirectActivation, int rbtType, HashMap extraInfo, String circleId, String refId, boolean isComboRequest, Map<String,String> xtraParametersMap)
	{
		/*if(!smActivation) {
			return activateSubscriber(subID(subscriberID), activate, startDate, isPrepaid,
					activationTimePeriod, freePeriod, actInfo, classType, rbtType);
		}*/

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

			String subscriptionClass = classType;
			if(cos != null)
			{
				cosID = cos.getCosId();
				if((subscriptionClass == null || subscriptionClass.equalsIgnoreCase("DEFAULT")) && cos.getSubscriptionClass() != null && !cos.getSubscriptionClass().equalsIgnoreCase("DEFAULT"))
					subscriptionClass = cos.getSubscriptionClass();
			}
			
			if(subscriptionClass == null)
				subscriptionClass = "DEFAULT";

			SubscriberPromo subscriberPromo = SubscriberPromoImpl.getActiveSubscriberPromo(conn,
					subID(subscriberID), "ICARD");
			if(subscriberPromo != null) {
				if(subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();

				SubscriberPromoImpl.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO")) {
				List<String> newViralSMSTypeList = new ArrayList<String>();
				newViralSMSTypeList.add("BASIC");
				newViralSMSTypeList.add("CRICKET");
				ViralSMSTable viralSMS = null;
				if (isViralSmsTypeListForNewTable(newViralSMSTypeList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
				if (viralSMS != null) {
					activationInfo = activationInfo + ":" + "viral";
				}
			}
			
			// update ExtraInfo 
			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			String finalRefID = UUID.randomUUID().toString();

			//Added for JIRA-RBT-6321
			if(isDirectActivation && refId != null)
				finalRefID = refId;
			
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
				String refID = UUID.randomUUID().toString();
				boolean success = SubscriberImpl.update(conn, subID(subscriberID), activate, null, startDate,
						endDate, prepaid, null, null, 0, activationInfo, subscriptionClass,
						deactivatedBy, deactivationDate, null, subscription, 0, cosID, cosID,
						rbtType, subscriber.language(), subExtraInfo, circleId, refID, isDirectActivation);
				if(startDate == null)
					startDate = new Date(System.currentTimeMillis());
				
				if (success)
				{
					subscriber = new SubscriberImpl(subID(subscriberID), activate, null, startDate,
						m_endDate, prepaid, null, null, 0, activationInfo, subscriptionClass,
						subscription, deactivatedBy, deactivationDate, null, 0, cosID, cosID,
						rbtType, subscriber.language(), subscriber.oldClassType(), subExtraInfo, circleId, refID);
					
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
					String selectionPeriod = chargeClass.getSelectionPeriod() .substring(1);
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
				List<String> newViralSMSTypeList = new ArrayList<String>();
				newViralSMSTypeList.add("BASIC");
				newViralSMSTypeList.add("CRICKET");
				ViralSMSTable viralSMS = null;
				if (isViralSmsTypeListForNewTable(newViralSMSTypeList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
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

	public Subscriber smActivateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid, int activationTimePeriod,
			int freePeriod, String actInfo, String classType,
			boolean smActivation, CosDetails cos, boolean isDirectActivation, int rbtType, HashMap extraInfo, String circleId)
	{
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
			if(activate.equalsIgnoreCase("TNB") && classType != null
					&& classType.equalsIgnoreCase("ZERO") && endDate == null) {
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



			String subscription = STATE_ACTIVATION_PENDING;
			String activationInfo = actInfo;

			if(isDirectActivation)
				subscription = STATE_ACTIVATED;
			
			SubscriberPromo subscriberPromo = SubscriberPromoImpl.getActiveSubscriberPromo(conn, subID(subscriberID), "ICARD");
			if(subscriberPromo != null)
				SubscriberPromoImpl.endPromo(conn, subID(subscriberID), "ICARD");
			
			if (activate != null && !activate.equalsIgnoreCase("VPO"))
			{
				List<String> newViralSMSTypeList = new ArrayList<String>();
				newViralSMSTypeList.add("BASIC");
				newViralSMSTypeList.add("CRICKET");
				ViralSMSTable viralSMS = null;
				if (isViralSmsTypeListForNewTable(newViralSMSTypeList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
				if (viralSMS != null)
					activationInfo = activationInfo + ":" + "viral";
			}
			
			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			String cosID = cos.getCosId();
			if (subscriber != null)
			{
				String subsciptionYes = subscriber.subYes();
				if (!isDirectActivation && subscriber.endDate().getTime() > getDbTime(conn))
				{
					if (subsciptionYes.equals("B") && (subscriber.rbtType() == TYPE_RBT || subscriber.rbtType() == TYPE_RRBT 
							|| subscriber.rbtType() == TYPE_SRBT) && subscriber.rbtType() != rbtType)
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
							convertSubscriptionType(subID(subscriberID), subscriber.subscriptionClass(), m_comboSubClass,
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
				String refID = UUID.randomUUID().toString();
				SubscriberImpl.update(conn, subID(subscriberID), activate, null, startDate,
						endDate, prepaid, null, null, 0, activationInfo, classType,
						deactivatedBy, deactivationDate, null, subscription, 0, cosID, cosID,
						rbtType, subscriber.language(), subExtraInfo, circleId, refID, isDirectActivation);
				if(startDate == null)
					startDate = new Date(System.currentTimeMillis());
				subscriber = new SubscriberImpl(subID(subscriberID), activate, null, startDate,
						m_endDate, prepaid, null, null, 0, activationInfo, classType,
						subscription, deactivatedBy, deactivationDate, null, 0, cosID, cosID,
						rbtType, subscriber.language(), subscriber.oldClassType(), subExtraInfo, circleId, refID);
			}
			else
				subscriber = SubscriberImpl.insert(conn, subID(subscriberID), activate, null,
						startDate, endDate, prepaid, null, null, 0, activationInfo,
						classType, null, null, null, subscription, 0, cosID, cosID,
						rbtType, null, isDirectActivation, subExtraInfo, circleId, null);
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

	public String smAddSubscriberSelections(String subscriberID,
			String callerID, Categories categories, HashMap clipMap,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType, String circleID, 
			boolean incrSelCount,boolean useDate, String transID, boolean OptIn,
			boolean isTata, boolean inLoop,String subClass, Subscriber sub, int rbtType, String selInterval, HashMap extraInfo, HashMap<String, String> responseParams){
		
		Connection conn = getConnection();
		if (conn == null)
			return null;
		int count = 0;
		Date nextChargingDate = null;
		Date startDate = startTime;
		String selectInfo = selectionInfo;
		String sel_status = STATE_ACTIVATION_PENDING;
		int nextPlus = -1;
		HashMap subscriberExtraInfo = new HashMap();
		boolean updateEndDate = false;
		boolean isVoluntarySuspendedSub = false;
		try
		{
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
			
			if(sub != null && rbtType != 2)
			{
				rbtType = sub.rbtType();
			}
			
			if(sub != null && sub.subYes() != null && (sub.subYes().equals("Z") || sub.subYes().equals("z")))
			{
				subscriberExtraInfo = getExtraInfoMap(sub);
				if(subscriberExtraInfo != null && subscriberExtraInfo.containsKey(VOLUNTARY))
				{
					isVoluntarySuspendedSub = (""+subscriberExtraInfo.get(VOLUNTARY)).equalsIgnoreCase("true");
				}
				if(!isVoluntarySuspendedSub)
				{
					logger.info(subscriberID +" is suspended. Returning false.");
					return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
				}
				else
				{
					sel_status = STATE_ACTIVATION_PENDING;
				}
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
			
			if (subYes != null
					&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
							.equalsIgnoreCase(STATE_EVENT)))
				sel_status = STATE_ACTIVATION_PENDING;
			/*
			 * if(freePeriod != 0) { nextChargingDate =
			 * Calendar.getInstance().getTime(); selectInfo = "free:" + selectInfo; }
			 */
			Date endDate = endTime;
			if(endDate == null)
				endDate = m_endDate;
			
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
				if(categories != null && (categories.type() == 10 || categories.type() == 12))
				 {
					 endDate = categories.endTime();
					 //	if(categories.startTime().getTime() > System.currentTimeMillis())
					 //	startDate = categories.startTime();
					 status = 79;
				 }
				 
			}
			
			
			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO"))
			{
				List<String> newViralSMSTypeList = new ArrayList<String>();
				newViralSMSTypeList.add("BASIC");
				newViralSMSTypeList.add("CRICKET");
				ViralSMSTable viralSMS = null;
				if (isViralSmsTypeListForNewTable(newViralSMSTypeList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
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
			
			if(!inLoop && status == 1) // If user opted for UDS
			{	
				HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub.extraInfo());
				if(subExtraInfoMap!= null && subExtraInfoMap.containsKey(UDS_OPTIN))
					inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN)).equalsIgnoreCase("TRUE");
				if(inLoop)
				{
					if(isShufflePresentSelection(subID(subscriberID), callerID, 0))
						inLoop = false;
					else if(categories.type() == 0 || categories.type() == 10 || categories.type() == 11 || categories.type() == 12 || categories.type() == 20)
								return SELECTION_FAILED_SHUFFLES_FOR_UDA_OPTIN;
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

			/* time of the day changes */
			SubscriberStatus subscriberStatus = null;
			
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
				if(inLoop && (categories.type() == SHUFFLE || status == 90 || status == 99 || status == 0))
					inLoop = false;
				if(fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;
				
				subscriberStatus = SubscriberStatusImpl
				.smSubscriberSelections(conn, subID(subscriberID),
						subID(callerID), status, rbtType);
				if (subscriberStatus != null)
				{
					if(inLoop && subscriberStatus.categoryType() == SHUFFLE)
						inLoop = false;
					
				}
			 //	 else // this else will make all first callerID selection as override :), not needed actually
			//		inLoop = false;
				
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
				if (m_overridableCategoryTypes.contains(""+categories.type()) || m_overridableSelectionStatus.contains(""+ status))
					incrSelCount = false;
				
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
				
				count = smCreateSubscriberStatus(subscriberID, callerID, categories.id(), subscriberWavFile,
						setTime, startDate, endDate, status, selectedBy, selectInfo, nextChargingDate,
						prepaid, chargeClassType, changeSubType, fromTime, toTime, sel_status,true,
						clipMap, categories.type(),useDate, loopStatus, isTata, nextPlus, rbtType, selInterval, extraInfo, responseParams, circleID);
				count++;
				logger.info("RBT::First incrSelCount = "+ incrSelCount);
				if(incrSelCount)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberID));

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
		return count > 0 ? SELECTION_SUCCESS : SELECTION_FAILED_INTERNAL_ERROR;
	}
}

