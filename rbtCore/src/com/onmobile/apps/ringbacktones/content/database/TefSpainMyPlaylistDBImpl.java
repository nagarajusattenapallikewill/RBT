package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Access;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;


public class TefSpainMyPlaylistDBImpl extends TelefonicaSelectionDBMgrImpl{
	
	private static Logger logger = Logger.getLogger(TefSpainMyPlaylistDBImpl.class);
	static MiPlaylistDBImpl miPlaylistDBImpl =  null;
	
	static {
		miPlaylistDBImpl = MiPlaylistDBImpl.getInstance();
		String catTypesForAutoRenewalString = RBTParametersUtils.getParamAsString(iRBTConstant.DAEMON, "CAT_TYPES_FOR_AUTO_RENEWAL", null);
		logger.info("CAT_TYPES_FOR_REN_FLOW: " + catTypesForAutoRenewalString);
		catTypesForAutoRenewal = ListUtils.convertToList(catTypesForAutoRenewalString, ",");
		logger.info("catTypesForRenFlow: " + catTypesForAutoRenewal);
	}
	
	@Override
	public int createSubscriberStatus(String subscriberID, String callerID, int categoryID,
			String subscriberWavFile, Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, Date nextChargingDate, String prepaid,
			String classType, boolean changeSubType, int fromTime, int toTime, String sel_status,
			boolean smActivation, HashMap clipMap, int categoryType, boolean useDate,
			char loopStatus, boolean isTata, int nextPlus, int rbtType, String selInterval,
			HashMap extraInfoMap, String refID, boolean isDirectActivation, String circleId,
			Subscriber sub, boolean useUIChargeClass, boolean isFromDownload) {
		
		logger.info("Adding subscriber selections, subscriberId: " + subscriberID + ", classType: "
				+ classType + ", extraInfoMap: " + extraInfoMap);
		
		Connection conn = getConnection();
		if (conn == null)
			return 0;

		int count = 0;
		try {
			if (!isTata) {
				subscriberID = subID(subscriberID);
				callerID = subID(callerID);
			}
			if (isTata)
				smActivation = false;
		
			if (isDirectActivation) {
				Date curDate = new Date();
				nextChargingDate = curDate;
				startTime = curDate;
				nextPlus = 0;
				sel_status = STATE_ACTIVATED;
			}
			SubscriberStatus subscriberStatus = null;

			Category category = getCategory(categoryID); 
			String provRefId = UUID.randomUUID().toString();
			if (category != null && category.getType() == PLAYLIST_ODA_SHUFFLE) {
				extraInfoMap.put("PROV_REF_ID", provRefId);
			}
			String selExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			
			if (category != null && category.getType() == PLAYLIST_ODA_SHUFFLE) {
				
				logger.info("going for ODA Shuffle Selections....");
				Clip[] activeClipsInCategory = RBTCacheManager.getInstance().getActiveClipsInCategory(categoryID); 
				logger.info("Shuffle clips ...="+Arrays.toString(activeClipsInCategory));
				if (activeClipsInCategory != null) {
					boolean isFreemiumSubscriber = false;
					boolean isUpgradeRequired = false;
					if(clipMap.containsKey("FREEMIUM_USER")){
						isFreemiumSubscriber = true;
					}
					
					if(clipMap.remove("UPGRADE_REQUIRED") != null) {
						isUpgradeRequired = true;
					}
					
					//Added for TS-6705
					SubscriberDownloads subDownload = getSubscriberDownloadsByDownloadStatus(subscriberID,categoryID,categoryType,"t");
					if(subDownload!=null){
						classType=RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE, "PLAY_LIST_SONG_CHARGE_CLASS", classType);
						isFreemiumSubscriber = true;
					}
					//End of TS-6705
					insertODAPackProvisioningRequest(subID(subscriberID), category,
							selectedBy, selectionInfo, provRefId,callerID, fromTime,
							toTime, status, selInterval,classType,isFreemiumSubscriber, isUpgradeRequired);
					int selCount = 0;
					for(Clip clip : activeClipsInCategory) {
						if(selCount == 0){
							loopStatus = 'o';
						}else{
						    loopStatus = 'l';
						}
						subscriberWavFile = clip.getClipRbtWavFile();
						String odaSelRefID = UUID.randomUUID().toString();
						sel_status = "W";
						classType="FREE";
					    subscriberStatus = SubscriberStatusImpl.insert(conn, subID(subscriberID),
							callerID, categoryID, subscriberWavFile, setTime, startTime, endTime,
							status, classType, selectedBy, selectionInfo, nextChargingDate,
							prepaid, fromTime, toTime, smActivation, sel_status, null, null,
							categoryType, loopStatus, nextPlus, rbtType, selInterval, selExtraInfo,
							odaSelRefID, circleId,null, null);
					    if(subscriberStatus!=null){
					    	selCount++;
					    }
					}
				}

			} else {
				String contentType = null;
 				if(subscriberWavFile != null && !subscriberWavFile.equals("")){
	 				Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(subscriberWavFile);
	 				if(clip != null){
	 					contentType = clip.getContentType();
	 				}
 				}
				loopStatus= miPlaylistDBImpl.getLoopStatusForNewMiPlayListSelection(subscriberID, status, callerID, categoryType, loopStatus,contentType);
				//Added for TS-6705
				String myPlaylistChargeClass = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE, "MY_PLAY_LIST_FIRST_SONG_CHARGE_CLASS", "DEFAULT");
				SubscriberDownloads subDownload = getSubscriberDownloadsByDownloadStatus(subscriberID,subscriberWavFile,"t");
				if (subDownload != null && classType != null
						&& classType.equalsIgnoreCase(myPlaylistChargeClass)) {
					logger.info("Selection waveFileName:-->"
							+ subscriberWavFile + " subDownload object: "
							+ subDownload.toString() + " classType: "
							+ classType);
					classType = RBTParametersUtils.getParamAsString(
							iRBTConstant.WEBSERVICE,
							"MY_PLAY_LIST_SONG_CHARGE_CLASS", classType);
				}
				//End of TS-6705
				 subscriberStatus = SubscriberStatusImpl.insert(conn, subID(subscriberID),
							callerID, categoryID, subscriberWavFile, setTime, startTime, endTime,
							status, classType, selectedBy, selectionInfo, nextChargingDate,
							prepaid, fromTime, toTime, smActivation, sel_status, null, null,
							categoryType, loopStatus, nextPlus, rbtType, selInterval, selExtraInfo,
							refID, circleId,null, null);
			}

			if (subscriberStatus == null) {
				logger.warn("Selection is not populated into DB, refId: " + refID
						+ ". Returning count: 0");
				return 0;
			} else {
				if (!clipMap.containsKey("RECENT_CLASS_TYPE")) {
					logger.info("Adding Recent class type for selection = " + classType);
					clipMap.put("RECENT_CLASS_TYPE", classType);
				}
				logger.info("Selection is inserted into Selections table" + ". Returning count: 1");
				count = 1;
			}

			int clipID = -1;
			String clipName = null;
			if (clipMap != null) {
				String s = (String) clipMap.get("CLIP_ID");
				try {
					clipID = Integer.parseInt(s);
				} catch (Exception e) {
					clipID = -1;
				}
				clipName = (String) clipMap.get("CLIP_NAME");
			}
			if (clipID != -1) {
				Date currentDate = null;
				if (useDate) {
					Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					currentDate = calendar.getTime();
				}
				Date date = new Date(System.currentTimeMillis());

				DateFormat timeFormatYYYY = new SimpleDateFormat("yyyy");
				String year = timeFormatYYYY.format(date);
				if (currentDate != null)
					year = timeFormatYYYY.format(currentDate);

				DateFormat timeFormatMM = new SimpleDateFormat("MM");
				String month = timeFormatMM.format(date);
				if (currentDate != null)
					month = timeFormatMM.format(currentDate);

				Access access = AccessImpl.getAccess(conn, clipID, year, month, currentDate);
				if (access == null)
					access = AccessImpl.insert(conn, clipID, clipName, year, month, 0, 0, 0,
							currentDate);
				else {
					access.incrementNoOfAccess();
					if (subscriberWavFile != null && subscriberWavFile.indexOf("rbt_ugc_") != -1)
						access.incrementNoOfPlays();
					access.update(conn);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("Returning count: " + count);
		return count;
	
		
	}
	
	
	//RBT-13544 TEF ES - Mi Playlist functionality
	@Override
	public SubscriberStatus[] getActiveNormalSelByCallerIdAndByStatus(String subscriberID, String callerID, int status) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus[] subscriberStatus= null;
		try {
			subscriberStatus = SubscriberStatusImpl.getActiveNormalSelByCallerIdAndByStatus(
					conn, subID(subscriberID), callerID, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}
	
	@Override
	public SubscriberDownloads[] getSubscriberDownloadsByDownloadStatus(String subscriberID, String downloadStatus) {
		SubscriberDownloads[] subscriberDownloads= null;
		subscriberDownloads = miPlaylistDBImpl.getSubscriberDownloadsByDownloadStatus(subID(subscriberID),downloadStatus);
		return subscriberDownloads;
	}
	
	//Added a new method to fetch downloads for TS-6705
	public SubscriberDownloads getSubscriberDownloadsByDownloadStatus(String subscriberID, String wavFileName, String downloadStatus) {
		SubscriberDownloads subscriberDownload= null;
		subscriberDownload = miPlaylistDBImpl.getSubscriberDownloadsByDownloadStatus(subID(subscriberID),wavFileName,downloadStatus);
		return subscriberDownload;
	}
	//End of TS-6705
	
	//Added a new method to fetch downloads for TS-6705
		public SubscriberDownloads getSubscriberDownloadsByDownloadStatus(String subscriberID, int categoryId,int categoryType, String downloadStatus) {
			SubscriberDownloads subscriberDownload= null;
			subscriberDownload = miPlaylistDBImpl.getSubscriberDownloadsByDownloadStatus(subID(subscriberID),categoryId,categoryType,downloadStatus);
			return subscriberDownload;
		}
	//End of TS-6705
		
	@Override
	public boolean updateDownloadStatusByDownloadStatus(String subscriberID,
			String promoID, String deselectedBy, String callerId, int status, String downloadStatus,String oldDownloadStatus,int catID,int catType) {
	
		boolean updated= false;;
		try {
			if(deselectedBy!=null && !deselectedBy.equalsIgnoreCase("SM")) {
			   updated = miPlaylistDBImpl.removeMiPlaylistTrackFromDownload(subscriberID,promoID, catID,catType,callerId,status);
					 /* SubscriberDownloadsImpl.updateDownloadStatusByDownloadStatus(conn,
					subID(subscriberID), promoID, deselectedBy, downloadStatus, oldDownloadStatus,catType);*/
			}
		} catch (Throwable e) {
			logger.error("Exception while removing downloads:", e);
		}
		return updated;
	
		
	}
	
	@Override
	public String addSubscriberDownloadRW(String subscriberId,
			String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy,
			String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass,
			boolean isSmClientModel, HashMap<String, String> responseParams, Subscriber consentSubscriber, int status, String callerId, String downloadStatus) {
		
		//Normal song selection and special caller
		//Special song selection status != 1 and not shuffle
		String updateDownload = extraInfo==null ? "FALSE" : extraInfo.remove("UPDATE_DOWNLOAD");
		if (updateDownload != null && updateDownload.equals("TRUE")
				|| (categories.type() != PLAYLIST_ODA_SHUFFLE 
				&& (((callerId == null || callerId.equalsIgnoreCase("all")) && status == 1) || Utility.isShuffleCategory(categories.type())))) {
			//add to download
			return addSubscriberDownloadRoW(subscriberId, subscriberWavFile, categories, endDate, isSubActive, 
				classType, selBy, selectionInfo, extraInfo, incrSelCount, useUIChargeClass, isSmClientModel, responseParams, null, status, callerId);
		
		}			
		return null;
	}
	
	
	public SubscriberDownloads getActiveSubscriberDownloadByStatus(String subscriberId,
			String promoId, String downloadStatus, int categoryId, int categoryType) {
		SubscriberDownloads activeSubscriberDownloadByStatus = miPlaylistDBImpl.getActiveSubscriberDownloadByStatus(subID(subscriberId), promoId, downloadStatus ,categoryId, categoryType);
		return activeSubscriberDownloadByStatus;
	}
	
	public String addSubscriberDownloadRoW(String subscriberId,
			String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy,
			String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass,
			boolean isSmClientModel, HashMap<String, String> responseParams, Subscriber consentSubscriber, int status, String callerId) {
		Connection conn = getConnection();
		
		if (conn == null)
			return null;

		try {
			ClipMinimal clip = getClipRBT(subscriberWavFile);
			int categoryID = categories.id();
			int categoryType = categories.type();
			if (endDate == null)
				endDate = m_endDate;
			subscriberId = subID(subscriberId);
			String nextClass = classType;

	
			SubscriberDownloads downLoad = getActiveSubscriberDownloadByStatus(subID(subscriberId),subscriberWavFile,"t", categoryID, categoryType);

			if (downLoad == null
					|| (downLoad != null && (downLoad.downloadStatus() == STATE_DOWNLOAD_DEACTIVATED || downLoad
							.downloadStatus() == STATE_DOWNLOAD_BOOKMARK))) {
				if (!isDownloadAllowed(subscriberId)) {
					return "FAILURE:DOWNLOAD_OVERLIMIT";
				}
			}

			String downloadExtraInfo = DBUtility
					.getAttributeXMLFromMap(extraInfo);
			String refId = null;
			if (responseParams != null) {
				refId = responseParams.get("SELECTION_REF_ID");
			}
			if (downLoad != null) {
				char downStat = downLoad.downloadStatus();
				if (downStat == STATE_DOWNLOAD_ACTIVATED
						|| downStat == STATE_DOWNLOAD_CHANGE || downStat == STATE_DOWNLOAD_SEL_TRACK)
					return "SUCCESS:DOWNLOAD_ALREADY_ACTIVE";
				else if (downStat == STATE_DOWNLOAD_BOOKMARK) {
					String response = isContentExpired(clip, categories);
					if (response != null)
						return response;
					deleteSubscriberDownload(subID(subscriberId),
							subscriberWavFile, categoryID, categoryType);
					
					miPlaylistDBImpl.addDownloadRowforTracking(subscriberId,
							subscriberWavFile, categoryID, endDate,
							isSubActive, categoryType, nextClass, selBy,
							selectionInfo, downloadExtraInfo, isSmClientModel,refId);
					
					/*SubscriberDownloads subscriberDownloads = SubscriberDownloadsImpl.insertRW(
							conn, subID(subscriberId), subscriberWavFile,
							categoryID, endDate, isSubActive, categoryType,
							nextClass, selBy, selectionInfo,
							downloadExtraInfo, isSmClientModel, STATE_DOWNLOAD_SEL_TRACK+"", refId);*/
					
					return "SUCCESS:DOWNLOAD_ADDED";
				} 
			} else {

				String response = isContentExpired(clip, categories);
				if (response != null) {
					return response;
				}
				
				String addDownloadRowforTracking = miPlaylistDBImpl.addDownloadRowforTracking(subscriberId,
						subscriberWavFile, categoryID, endDate, isSubActive,
						categoryType, nextClass, selBy, selectionInfo,
						downloadExtraInfo, isSmClientModel, refId);
				
				/*SubscriberDownloads subscriberDownloads = SubscriberDownloadsImpl.insertRW(
						conn, subID(subscriberId), subscriberWavFile,
						categoryID, endDate, isSubActive, categoryType,
						nextClass, selBy, selectionInfo, downloadExtraInfo,
						isSmClientModel, STATE_DOWNLOAD_SEL_TRACK+"", refId);*/
				
				if (addDownloadRowforTracking.equalsIgnoreCase("false"))
					return "FAILURE:INSERTION_FAILED";

				return "SUCCESS:DOWNLOAD_ADDED";
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL_FAULT";
	}
	
	@Override
	public String addSubscriberSelections(String subscriberID, String callerID,
			Categories categories, HashMap clipMap, Date setTime, Date startTime, Date endTime,
			int status, String selectedBy, String selectionInfo, int freePeriod, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime, int toTime,
			String chargeClassType, boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType, String circleID,
			boolean incrSelCount, boolean useDate, String transID, boolean OptIn, boolean isTata,
			boolean inLoop, String subClass, Subscriber sub, int rbtType, String selInterval,
			HashMap extraInfo, boolean useUIChargeClass, String refID, boolean isDirectActivation) {
		
		
		String subscriberWavFile = null;
		if (clipMap.containsKey("CLIP_WAV"))
			subscriberWavFile = (String) clipMap.get("CLIP_WAV");
		SubscriberDownloads downloads =null;
		if(!Utility.isShuffleCategory(categories.type()) && callerID == null && status == 1) {
		  downloads = getActiveSubscriberDownloadByStatus(subscriberID, subscriberWavFile, "t", categories.id(), categories.type());
		}
		if(downloads!=null) {
			incrSelCount =false;
		}
		
		return super.addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime,
				status, selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime,
				chargeClassType, smActivation, doTODCheck, mode,
				regexType, subYes, promoType, circleID,
				incrSelCount, useDate, transID, OptIn, isTata,
				inLoop, subClass, sub, rbtType, selInterval,
				extraInfo, useUIChargeClass, refID, isDirectActivation);
	}
	
	@Override
	public boolean expireSubscriberDownloadAndUpdateExtraInfo(String subscriberId,
			String wavFile, int categoryId, int categoryType,
			String deactivateBy, String deselectionInfo,String extraInfo, boolean isDirectDeactivation) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (catTypesForAutoRenewal != null
					&& catTypesForAutoRenewal
					.contains(String.valueOf(categoryType))) {
				SubscriberDownloads download = getActiveSubscriberDownloadByStatus(subscriberId, null, "t", categoryId, categoryType);
				return deactSelectionsAndDeleteDownloadForRenewalFlow(conn, deactivateBy, download);
			}
			return SubscriberDownloadsImpl
					.removeSubscriberDownload(conn,
							subID(subscriberId), wavFile, categoryId, categoryType);		
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	@Override
	public boolean deactSelectionsAndDeleteDownloadForRenewalFlow(Connection conn, String deactivateBy,
			SubscriberDownloads download) {
		boolean isConnectionLocal = false;
		if (conn == null) {
			conn = getConnection();
			isConnectionLocal = true;
			if (conn == null) {
				return false;
			}
		}
		if (download == null) {
			logger.info("download is null. Returning false");
			return false;
		}
		try {
			SubscriberStatus[] activeSelections = RBTDBManager.getInstance().getAllActiveSubscriberSettings(download.subscriberId());
			boolean isActiveSelFound = false;
			int i=0;
			if (activeSelections != null) {
				logger.info("Number of active selections: " + activeSelections.length);
				for (SubscriberStatus selection : activeSelections) {
					if (selection != null 
							&& selection.categoryID() == download.categoryID() 
							&& !(selection.status() == 90 
							|| selection.status() == 99 
							|| selection.selType() == 2 )) {
						if (!isActiveSelFound && (selection.refID().equals(download.refID()) || i++ == activeSelections.length-1)) {
							isActiveSelFound = true;
							String newExtraInfo = getExtraInfoWithDCTInsert(selection);
							deactivateSubscriberRecordsByRefId(download.subscriberId(), deactivateBy, selection.refID(), newExtraInfo, download.refID(), null, null);
						} else {
							char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
							if (selection.loopStatus() == LOOP_STATUS_EXPIRED)
								newLoopStatus = LOOP_STATUS_EXPIRED;
							deactivateSubscriberRecordsByRefId(download.subscriberId(), deactivateBy, selection.refID(), null, null, newLoopStatus, "X");
						}
					}
				}
			}
			if (!isActiveSelFound) {
				//If no active entries were found, add a new dummy selection entry with status as D.
				Subscriber subscriber = getSubscriber(download.subscriberId());	
				String selExtraInfo = getExtraInfoWithDCTInsert(null);
				Date currDate = new Date();
				SubscriberStatusImpl.insert(conn, download.subscriberId(), null,
						download.categoryID(), download.promoId(), download.setTime(),
						download.startTime(),currDate, 1,
						download.classType(), download.selectedBy(), null,
						currDate, subscriber.prepaidYes() ? "y" : "n", 0,
								2359, false, "D", deactivateBy, null, download.categoryType(),
								LOOP_STATUS_EXPIRED, 0, 0, null, selExtraInfo, download.refID(),
								subscriber.circleID(),null, null);
			}
			return SubscriberDownloadsImpl.directDeactivateSubscriberDownload(conn,
					download.subscriberId(), null, deactivateBy, download.categoryID(),
					download.categoryType(), null, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
			return false;
		} finally {
			if (isConnectionLocal) {
				releaseConnection(conn);
			}
		}
	}


	private String getExtraInfoWithDCTInsert(SubscriberStatus selection) {
		Map<String, String> extraInfoMap = null;
		if (selection != null) {
			String selExtraInfo = selection.extraInfo();
			extraInfoMap = DBUtility.getAttributeMapFromXML(selExtraInfo);
		}
		if (extraInfoMap == null) {
			extraInfoMap = new HashMap<String, String>();
		}
		extraInfoMap.put("DCT_INSERT", "TRUE");
		String newExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
		return newExtraInfo;
	}

	@Override
	public boolean expireSubscriberDownload(String subscriberId,
			String wavFile, int categoryId, int categoryType,
			String deactivateBy, String deselectionInfo, boolean isDirectDeactivation) {
		return expireSubscriberDownloadAndUpdateExtraInfo(subscriberId, wavFile, categoryId, categoryType, deactivateBy, deselectionInfo, null, isDirectDeactivation);
	}
	
	@Override
	public String directDeactivateSubscriberRecordsByRefId(String subscriberID,
			String deactBy, String refId, Character newLoopStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.directDeactivateSubscriberRecordsByRefId(
					conn, subID(subscriberID), deactBy, refId, newLoopStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}
	
	@Override
	public String updateSelectionExtraInfoAndRefId(String subscriberId,
			String newExtraInfo, String oldRefId, String newRefId) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.updateSelectionExtraInfoAndRefId(conn, subscriberId, newExtraInfo, oldRefId, newRefId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}
	
	public void addOldMiplayListSelections(SubscriberStatus subscriberStatus) {
		miPlaylistDBImpl.addOldMiplayListSelections(subscriberStatus);
	}
	
	//RBT-14900	Base Activation failure is not updating provisioning request table
	public boolean deactivateAllPack(Subscriber subscriber,
			HashMap<String, String> packExtraInfoMap) {
		Connection conn = getConnection();
		boolean isDeactivated = false;
		if (conn == null)
			return isDeactivated;
		try {
			
			String packExtraInfo = DBUtility
						.getAttributeXMLFromMap(packExtraInfoMap);
			
			if(!RBTParametersUtils.getParamAsBoolean("COMMON", "DEL_SELECTION_ON_DEACT", "TRUE"))
			{
				isDeactivated = ProvisioningRequestsDao.deactivateAllPacks(
						subscriber.subID(), packExtraInfo,true, false);
			}else {
				isDeactivated = ProvisioningRequestsDao.deactivateAllPacks(
						subscriber.subID(), packExtraInfo,false, false);
			}
			
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return isDeactivated;
	}
}
