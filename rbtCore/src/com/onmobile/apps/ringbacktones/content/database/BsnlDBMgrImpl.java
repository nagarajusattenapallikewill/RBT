package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class BsnlDBMgrImpl extends VodafoneDBMgrImpl
{
	private static Logger logger = Logger.getLogger(BsnlDBMgrImpl.class);

	public String addSubscriberSelections(String subscriberID, String callerID, Categories categories, HashMap clipMap, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,String selectionInfo, int freePeriod, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime,int toTime, String chargeClassType, boolean smActivation,
			boolean doTODCheck, String mode, String regexType, String subYes,String promoType, String circleID, boolean incrSelCount,
			boolean useDate, String transID, boolean OptIn, boolean isTata,boolean inLoop, String subClass, Subscriber sub, int rbtType,
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
		boolean updateEndDate = false;
		try
		{
			subscriberID = subID(subscriberID);
			callerID = subID(callerID);
			if (subscriberID != null && callerID != null && subscriberID.equals(callerID))
				return SELECTION_FAILED_OWN_NUMBER;

			if (categories != null && com.onmobile.apps.ringbacktones.webservice.common.Utility.isShuffleCategory(categories.type()))
				if (categories.endTime().before(new Date()))
					return SELECTION_FAILED_CATEGORY_EXPIRED;

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
			
			if (sub != null && rbtType != 2)
				rbtType = sub.rbtType();
			if (sub != null && sub.subYes() != null && (sub.subYes().equals("Z") || sub.subYes().equals("z")))
			{
				logger.info(subscriberID + " is suspended. Returning false.");
				return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
			}
			
			if (m_checkForSuspendedSelection && isSelSuspended(subscriberID, callerID))
			{
				logger.info("selection of " + subscriberID + " for " + callerID + " is suspended. Returning false.");
				return SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED;
			}

			if (callerID != null)
			{
				Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn, subscriberID);
				if (groups != null && groups.length > 0)
				{
					int[] groupIDs = new int[groups.length];
					for (int i = 0; i < groups.length; i++)
						groupIDs[i] = groups[i].groupID();
					GroupMembers groupMember = GroupMembersImpl.getMemberFromGroups(conn, callerID, groupIDs);
					if (groupMember != null)
						for (Groups group : groups)
							if (groupMember.groupID() == group.groupID())
								if (group.preGroupID() != null && group.preGroupID().equals("99")) // Blocked Caller
									return SELECTION_FAILED_CALLER_BLOCKED;
				}
			}


			Date endDate = endTime;
			if (endDate == null)
				endDate = m_endDate;

			// If chargeClassType is null, then useUIChargeClass parameter will be ignored
			useUIChargeClass = useUIChargeClass && chargeClassType != null;
			
			String classType = "DEFAULT";
			if (useUIChargeClass)
				classType = chargeClassType;
			else if (categories != null && categories.classType() != null)
				classType = categories.classType();

			Date clipEndTime = null;
			String clipGrammar = null;
			String clipClassType = null;
			String subscriberWavFile = null;
			if (clipMap != null)
			{
				if (clipMap.containsKey("CLIP_CLASS"))
					clipClassType = (String) clipMap.get("CLIP_CLASS");
				if (clipMap.containsKey("CLIP_END"))
					clipEndTime = (Date) clipMap.get("CLIP_END");
				if (clipMap.containsKey("CLIP_GRAMMAR"))
					clipGrammar = (String) clipMap.get("CLIP_GRAMMAR");
				if (clipMap.containsKey("CLIP_WAV"))
					subscriberWavFile = (String) clipMap.get("CLIP_WAV");
			}

			if (subscriberWavFile == null)
			{
				if (status != 90)
					return SELECTION_FAILED_NULL_WAV_FILE;
				subscriberWavFile = "CRICKET";
			}

			if (subYes != null && (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes.equalsIgnoreCase(STATE_EVENT)))
			{
				if (!isPackActivationPendingForContent(sub, categories, subscriberWavFile, status, callerID))
					sel_status = STATE_TO_BE_ACTIVATED;
			}

			if (subClass != null && m_subOnlyChargeClass != null && m_subOnlyChargeClass.containsKey(subClass))
			{
				chargeClassType = (String) m_subOnlyChargeClass.get(subClass);
				updateEndDate = true;
			}
			if (clipEndTime != null)
			{
				if (clipEndTime.getTime() < System.currentTimeMillis())
					return SELECTION_FAILED_CLIP_EXPIRED;
				if (categories != null && (categories.type() == DAILY_SHUFFLE || categories.type() == MONTHLY_SHUFFLE))
				{
					endDate = categories.endTime();
					status = 79;
				}

				if (!useUIChargeClass && clipClassType != null && !clipClassType.equalsIgnoreCase("DEFAULT") && classType != null
						&& !clipClassType.equalsIgnoreCase(classType))
				{
					ChargeClass catCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
					ChargeClass clipCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(clipClassType);

					if (catCharge != null && clipCharge != null && catCharge.getAmount() != null && clipCharge.getAmount() != null)
					{
						try
						{
							int firstAmount = Integer.parseInt(catCharge.getAmount());
							int secondAmount = Integer.parseInt(clipCharge.getAmount());

							if ((firstAmount < secondAmount) || (m_overrideChargeClasses != null && m_overrideChargeClasses
											.contains(clipClassType.toLowerCase())))
								classType = clipClassType;
						}
						catch (Throwable e)
						{
						}
					}
					if (clipClassType.startsWith("TRIAL") && categories != null && categories.id() != 26)
						classType = clipClassType;
				}
			}

			if (!useUIChargeClass && chargeClassType != null)
			{
				ChargeClass first = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
				ChargeClass second = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClassType);

				if (first != null && second != null && first.getAmount() != null && second.getAmount() != null)
				{
					try
					{
						int firstAmount = Integer.parseInt(first.getAmount());
						int secondAmount = Integer.parseInt(second.getAmount());
						if (firstAmount <= secondAmount || secondAmount == 0 || chargeClassType.equalsIgnoreCase("YOUTHCARD")
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
					classType = chargeClassType;
				if (first != null && first.getChargeClass().startsWith("TRIAL") && categories != null && categories.id() != 26)
					classType = first.getChargeClass();
			}

			if (!useUIChargeClass && categories != null && categories.type() == 10 && m_modeChargeClass != null
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
			 * If user enabled UDS , then all his selections should go in Loop
			 */
			if (!inLoop)
			{
				HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub.extraInfo());
				if (subExtraInfoMap != null && subExtraInfoMap.containsKey(UDS_OPTIN))
					inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN)).equalsIgnoreCase("TRUE");
			}

			if (selInterval != null && status != 80)
			{
				if (selInterval.startsWith("W") || selInterval.startsWith("M"))
					status = 75;
				if (selInterval.startsWith("Y"))
				{
					status = 95;
					String date = selInterval.substring(1);
					Date parseDate = null;
					if (date.length() == 8)
					{
						SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
						Date currentDate = new Date();
						parseDate = dateFormat.parse(date);
						if (parseDate.before(currentDate) || parseDate.equals(currentDate))
							return SELECTION_FAILED_INVALID_PARAMETER;
						Calendar cal = Calendar.getInstance();
						cal.setTime(parseDate);
						cal.add(Calendar.DAY_OF_YEAR, 1);
						endDate = cal.getTime();
					}

					if (date.length() == 4)
						endDate = m_endDate;
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
			SubscriberStatus[] subscriberSelections = SubscriberStatusImpl.getSubscriberSelections( conn, subID(subscriberID), 
					subID(callerID), rbtType, udpId);

			SubscriberStatus subscriberStatus = getAvailableSelection(conn, subID(subscriberID), subID(callerID),
					subscriberSelections, categories, subscriberWavFile, status, fromTime, toTime, startDate, endDate,
					doTODCheck, inLoop, rbtType, selInterval, selectedBy);
			if (subscriberStatus == null)
			{
				logger.info("RBT::no matches found");
				if (inLoop && (categories.type() == SHUFFLE || status == 90 || status == 99 || status == 0))
					inLoop = false;
				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;

				subscriberStatus = SubscriberStatusImpl.smSubscriberSelections(conn, subID(subscriberID), subID(callerID), status, rbtType);
				if (subscriberStatus != null)
				{
					if (inLoop && subscriberStatus.categoryType() == SHUFFLE)
						inLoop = false;
				}
//				else
//					inLoop = false; // this else will make all first callerID selection as override :), not needed actually
					

				/**
				 * @added by sreekar if user's last selection is a trail selection his next
				 *        selection should override the old one
				 */
				char loopStatus = getLoopStatusForNewSelection(inLoop, subscriberID, isPrepaid);

				String actBy = null;
				if (sub != null)
					actBy = sub.activatedBy();
				
				if (m_trialChangeSubTypeOnSelection && actBy != null && actBy.equals("TNB") && (subClass != null && subClass.equals("ZERO")))
				{
					if (classType != null && classType.equals("FREE"))
					{
						sel_status = STATE_BASE_ACTIVATION_PENDING;
						if (!convertSubscriptionTypeTrial(subID(subscriberID), subClass, "DEFAULT", sub))
							return SELECTION_FAILED_TNB_TO_DEFAULT_FAILED;
					}
				}

				boolean isPackSel = false;
				String packCosID = null;
				if (m_overridableCategoryTypes.contains("" + categories.type()) || m_overridableSelectionStatus.contains("" + status))
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

					if (nextClass == null)
						return SELECTION_FAILED_INTERNAL_ERROR;
					if (!nextClass.equalsIgnoreCase("DEFAULT"))
						classType = nextClass;
				}

				if (!useUIChargeClass && m_overrideChargeClasses != null && chargeClassType != null 
						&& m_overrideChargeClasses.contains(chargeClassType.toLowerCase()))
					classType = chargeClassType;

				if (transID != null)
				{
					selectInfo += ":transid:" + transID + ":";
					if (sel_status.equals(STATE_TO_BE_ACTIVATED))
						sel_status = STATE_UN;
				}
				
				if (!useUIChargeClass)
				{
					if(status == 80 && rbtType == 2)
						classType = clipClassType;
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
										logger.info("Exception in getting campaign id", e);
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

				String checkSelStatus = checkSelectionLimit(subscriberSelections, subID(callerID), inLoop);
				if (!checkSelStatus.equalsIgnoreCase("SUCCESS"))
					return checkSelStatus;

				//Added the grace selection deact mode for JIRA-RBT-6338
				String graceDeselectedBy = selectedBy;
				Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
				if(parameter != null && parameter.getValue() != null)
					graceDeselectedBy = parameter.getValue();

				SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn, subID(subscriberID), subID(callerID), status, fromTime, toTime, 
						graceDeselectedBy, rbtType);
				//CG Integration Flow - Jira -12806
				count = createSubscriberStatus(subscriberID, callerID, categories.id(), subscriberWavFile, setTime, startDate, endDate, 
						status, selectedBy, selectInfo, nextChargingDate, prepaid, classType, changeSubType, fromTime,
						toTime, sel_status, true, clipMap, categories.type(), useDate, loopStatus,
						isTata, nextPlus, rbtType, selInterval, extraInfo, refID, isDirectActivation, circleID,sub, useUIChargeClass, false);
				logger.info("Checking to update num max selections or not."
						+ " count: " + count + ", isPackSel: " + isPackSel
						+ " incrSelCount: " + incrSelCount);
				if (incrSelCount && isPackSel && count == 1)
					ProvisioningRequestsDao.updateNumMaxSelections(conn, subscriberID, packCosID);
				else if (incrSelCount && count == 1)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberID));

				if (updateEndDate)
					SubscriberImpl.updateEndDate(conn, subID(subscriberID), endDate, null);
			}
			else
				return SELECTION_FAILED_SELECTION_OVERLAP;
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

	//Added to support same subscription class upgradation
	@Override
	public boolean convertSubscriptionType(String subscriberID, String initType, String finalType,
			String strActBy, String strActInfo, boolean concatActInfo, int rbtType, boolean useRbtType, String extraInfo, Subscriber subscriber){
		if(initType == null || finalType == null) 
			return false; 
		Connection conn = getConnection();
		if (conn == null)
			return false;
		
		boolean success = false;
		try
		{
			success = SubscriberImpl.convertSubscriptionType(conn, subID(subscriberID), initType,
				finalType, strActBy, strActInfo, concatActInfo, rbtType, useRbtType, extraInfo, subscriber);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return success;
	}
}