package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

public class VodafoneSpainDbMgrImpl extends GrameenDbMgrImpl{
	
	Logger log= Logger.getLogger(VodafoneSpainDbMgrImpl.class);
	static MiPlaylistDBImpl miPlaylistDBImpl =  null;
	
	static {
		miPlaylistDBImpl = MiPlaylistDBImpl.getInstance();
	}
	
	//RBT-14044	VF ES - MI Playlist functionality for RBT core
	public String removeMiPlaylistDownloadTrack(String subscriberID, String promoID,
			int categoryID, int categoryType, String callerId, int status) {
		
		boolean removed = miPlaylistDBImpl.removeMiPlaylistTrackFromDownload(subscriberID, promoID, categoryID, categoryType, callerId, status);
		log.info("Remove status in removeMiPlaylistDownloadTrack of download is: "+removed);
		return removed?"true":"false";
	}
	
	public String addDownloadForTrackingMiPlaylist(String subID, String promoID, int catID, int catType,String refId,String classType, String selBy,int status, int selType) {
		String resp =null;
		if (!(status == 90 || status == 99 || selType == 2 )
				&& null == getActiveSubscriberDownloadByStatus(subID,promoID,"t", catID, catType)) {
		   resp = miPlaylistDBImpl.addDownloadRowforTracking(subID, promoID, catID, null,
				false, catType, classType, selBy, null, null, false, refId);
		}
		log.info("Download inserted in addDownloadForTracking for miplaylist :"+resp);
		return resp;
	
	}
	@Override
	public char getLoopStatusForNewMiPlayListSelection(String subscriberID,int status,String callerID,int catType, char loopStatus) {
		loopStatus = miPlaylistDBImpl.getLoopStatusForNewMiPlayListSelection(subscriberID, status, callerID, catType, loopStatus);
		return loopStatus;
	}
	
	@Override
	public SubscriberDownloads[] getSubscriberDownloadsByDownloadStatus(String subscriberID, String downloadStatus) {
		SubscriberDownloads[] subscriberDownloads= null;
		subscriberDownloads = miPlaylistDBImpl.getSubscriberDownloadsByDownloadStatus(subID(subscriberID),downloadStatus);
		return subscriberDownloads;
	}

	public void addOldMiplayListSelections(SubscriberStatus subscriberStatus) {
		miPlaylistDBImpl.addOldMiplayListSelections(subscriberStatus);
	}
	
	public SubscriberDownloads getSubscriberDownload(String subscriberId,
			String wavFile, int categoryID, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberDownload(conn,
					subID(subscriberId), wavFile, categoryID, categoryType,true);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	public SubscriberDownloads getActiveSubscriberDownloadByStatus(String subscriberId,
			String promoId, String downloadStatus, int categoryId, int categoryType) {
		SubscriberDownloads activeSubscriberDownloadByStatus = miPlaylistDBImpl.getActiveSubscriberDownloadByStatus(subID(subscriberId), promoId, downloadStatus ,categoryId, categoryType);
		return activeSubscriberDownloadByStatus;
	}
	
	public SubscriberDownloads getDownloadToBeDeactivated(String subscriberID,
			String promoID, int categoryId, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getDownloadToBeDeactivated(conn,
					subID(subscriberID), promoID, categoryId, categoryType,true);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	@Override
	public boolean expireSubscriberDownload(String subscriberId,
			String wavFile, int categoryId, int categoryType,
			String deactivateBy, String deselectionInfo, boolean isDirectDeactivation) {
		return expireSubscriberDownloadAndUpdateExtraInfo(subscriberId, wavFile, categoryId, categoryType, deactivateBy, deselectionInfo, null, isDirectDeactivation);
	}
	
	@Override
	public boolean expireSubscriberDownloadAndUpdateExtraInfo(String subscriberId,
			String wavFile, int categoryId, int categoryType,
			String deactivateBy, String deselectionInfo,String extraInfo, boolean isDirectDeactivation) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.expireSubscriberDownload(conn,
					subID(subscriberId), wavFile, deactivateBy, categoryId,
					categoryType, deselectionInfo, extraInfo,true, isDirectDeactivation);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}
	
	public String deleteDownloadwithTstatus(String subscriberID,String wavFile) {

		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberDownloadsImpl.deleteDownloadwithTstatus(conn, subscriberID, wavFile);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
		
	}
	
	public SubscriberStatus [] getPendingDefaultSubscriberSelections(String subID, String callerID, int status, String shuffleSetTime) {

		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getPendingDefaultSubscriberSelections(conn, subID, callerID, status, shuffleSetTime);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
		
	
	}
	
	public void addTrackingOfPendingSelections(SubscriberStatus ss){

	if (Utility.isShuffleCategory(ss.categoryType())) {
		DateFormat mySqlTimeFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String shuffleSetTime = mySqlTimeFormat.format(ss
				.setTime());
		SubscriberStatus[] pendingSelections = getPendingDefaultSubscriberSelections(
						ss.subID(), null, 1, shuffleSetTime);
		if (pendingSelections != null
				&& pendingSelections.length > 0) {
			for (SubscriberStatus sel : pendingSelections) {
				String resp = addDownloadForTrackingMiPlaylist(
								sel.subID(),
								sel.subscriberFile(),
								sel.categoryID(),
								sel.categoryType(), null,
								sel.classType(),
								sel.selectedBy(), sel.status(),
								sel.selType());
				 log.info("response of tracking added for selections: "+sel.toString()+ " is "+resp);
			}
		}
	}
	}
	
	
	public boolean removeDownloadsWithTStatus(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			boolean deleted = SubscriberDownloadsImpl.removeDownloadsWithTStatus(conn, subscriberID);
			log.info("status of deleting downloads with t status is : "+deleted);
			return deleted;
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}
}