package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;


public class MiPlaylistDBImpl  extends RBTDBManager {
	
	private static Logger logger = Logger.getLogger(MiPlaylistDBImpl.class);
	private static MiPlaylistDBImpl miPlayListImpl = null;
	
	private MiPlaylistDBImpl(){	
	}
	
	private static void initMiPlayListImpl() throws Exception {
		ParametersCacheManager parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		Parameters daemonParam = parametersCacheManager.getParameter("COMMON","MI_PLAYLIST_IMPL_CLASS");
		String miPlaylist_implURL = null;
		if(daemonParam != null) {
		  miPlaylist_implURL = daemonParam.getValue();
		}
		if (miPlaylist_implURL == null) {
			miPlayListImpl = new MiPlaylistDBImpl();
		} else {
			Class implClass = Class.forName(miPlaylist_implURL);
			miPlayListImpl = (MiPlaylistDBImpl) implClass.newInstance();
		}
	}
	
	
	public static MiPlaylistDBImpl getInstance() {
		if (miPlayListImpl == null) {
			synchronized (MiPlaylistDBImpl.class) {
				if (miPlayListImpl == null) {
					try {
						initMiPlayListImpl();
					} catch (Exception e) {
						miPlayListImpl = new MiPlaylistDBImpl();
						logger.info("Could not initialize the MiPlayListImpl properly due to exception occured: "+ e);
					}
				}
			}
		}
		return miPlayListImpl;
	}
	
	public char getLoopStatusForNewMiPlayListSelection(String subscriberID,int status,String callerID,int catType, char loopStatus,String contentType) {
		
		SubscriberStatus[] activeSelAllCallerAllDay = getActiveNormalSelByCallerIdAndByStatus(subscriberID, null, 1);
		boolean isOLAClipActive = false;
		boolean isOLAContentType = false;
		if(activeSelAllCallerAllDay != null){
			for(SubscriberStatus subscriberStatus:activeSelAllCallerAllDay){
				String wavFile = subscriberStatus.subscriberFile();
				if(wavFile != null && !wavFile.equalsIgnoreCase("")){
					Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFile);
					if(clip != null){
						if(clip.getContentType() != null)
							isOLAClipActive = clip.getContentType().equalsIgnoreCase(CONTENT_TYPE_OLA);
						if(isOLAClipActive){
							break;
						}
					}
				}
			}
		}
		if(contentType != null && contentType.equalsIgnoreCase(CONTENT_TYPE_OLA)){
			isOLAContentType= true;
		}
		if(callerID == null && status == 1 &&activeSelAllCallerAllDay!=null && activeSelAllCallerAllDay.length >0 
				&& (!Utility.isShuffleCategory(catType) && !isOLAContentType  && !isOLAClipActive)) {
			if(loopStatus == LOOP_STATUS_OVERRIDE)
			    loopStatus = LOOP_STATUS_LOOP;
			if(loopStatus == LOOP_STATUS_OVERRIDE_INIT)
				loopStatus = LOOP_STATUS_LOOP_INIT;
		}else {
			if(loopStatus == LOOP_STATUS_LOOP)
			    loopStatus = LOOP_STATUS_OVERRIDE;
			if(loopStatus == LOOP_STATUS_LOOP_INIT)
				loopStatus = LOOP_STATUS_OVERRIDE_INIT;
		}
		
		return loopStatus;
	}
	
	@Override
	public char getLoopStatusForNewMiPlayListSelection(String subscriberID,int status,String callerID,int catType, char loopStatus) {
		return getLoopStatusForNewMiPlayListSelection(subscriberID, status, callerID, catType, loopStatus, null);
	}
	
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
	
	public String addDownloadRowforTracking(String subscriberId, String subscriberWavFile, int categoryID, Date endDate, 
			boolean isSubActive, int categoryType, String nextClass, String selBy, String selectionInfo, String downloadExtraInfo, boolean isSmClientModel, String refId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		
		if (endDate == null)
			endDate = m_endDate;
		
		SubscriberDownloads subscriberDownloads = SubscriberDownloadsImpl.insertRW(
				conn, subID(subscriberId), subscriberWavFile,
				categoryID, endDate, isSubActive, categoryType,
				nextClass, selBy, selectionInfo,
				downloadExtraInfo, isSmClientModel, STATE_DOWNLOAD_SEL_TRACK+"", refId);
		
		return subscriberDownloads!=null? "true":"false";
	}
	
	
	public boolean removeMiPlaylistTrackFromDownload(String subscriberID,
			String promoID, int categoryID, int categoryType, String callerId, int status) {
		
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean updated= false;;
		try {
			if((callerId == null || callerId.equalsIgnoreCase("all")) && status == 1 && !Utility.isShuffleCategory(categoryType)) {
			  updated = SubscriberDownloadsImpl.deleteSubscriberDownloadsByStatusCatIDCatType(conn, subscriberID, promoID, categoryID, categoryType, "t");
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return updated;
	
		
	}
	
	@Override
	public SubscriberDownloads[] getSubscriberDownloadsByDownloadStatus(String subscriberID, String downloadStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberDownloads[] subscriberDownloads= null;
		try {
			subscriberDownloads = SubscriberDownloadsImpl.getSubscriberDownloadsByDownloadStatus(conn,
					subID(subscriberID),downloadStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberDownloads;
	}
	
	//Added a new method to get the downloads list for TS-6705
	public SubscriberDownloads getSubscriberDownloadsByDownloadStatus(String subscriberID, String wavFileName, String downloadStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberDownloads subscriberDownload= null;
		try {
			subscriberDownload = SubscriberDownloadsImpl.getSubscriberDownloadsByDownloadStatus(conn,
					subID(subscriberID),wavFileName,downloadStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberDownload;
	}
	//End of TS-6705
	
	//Added a new method to get the downloads list for TS-6705
		public SubscriberDownloads getSubscriberDownloadsByDownloadStatus(String subscriberID, int categoryID,int categoryType, String downloadStatus) {
			Connection conn = getConnection();
			if (conn == null)
				return null;

			SubscriberDownloads subscriberDownload= null;
			try {
				subscriberDownload = SubscriberDownloadsImpl.getSubscriberDownloadsByDownloadStatus(conn,
						subID(subscriberID),categoryID,categoryType,downloadStatus);
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				releaseConnection(conn);
			}
			return subscriberDownload;
		}
	//End of TS-6705
		
	public void addOldMiplayListSelections(SubscriberStatus subscriberStatus) {
		 logger.debug(" calling addOldMiplayListSelections...");
		
		 Map<String,String> classTypeMap = getClassTypeMap();
		 String classType = null;
		 boolean isOLAContent = false;
		 String wavFile = subscriberStatus.subscriberFile();
		 if (wavFile != null && !wavFile.equalsIgnoreCase("")) {
				Clip clipTmp = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFile);
				if (clipTmp != null && clipTmp.getContentType() != null)
						isOLAContent = clipTmp.getContentType().equalsIgnoreCase(CONTENT_TYPE_OLA);
			}
		 if(!isOLAContent && !Utility.isShuffleCategory(subscriberStatus.categoryType())) {
			 if(subscriberStatus.callerID() == null 
					&& (subscriberStatus.loopStatus() == 'O' || subscriberStatus.loopStatus() == 'o') && subscriberStatus.status() == 1){
				// normal song activation..
				SubscriberDownloads[] deactiveSel = getSubscriberDownloadsByDownloadStatus(subscriberStatus.subID(),"t");
				if (deactiveSel != null	&& deactiveSel.length > 0) {
					for (int i = 0; i < deactiveSel.length; i++) {
						SelectionRequest selectionRequest =new SelectionRequest(subscriberStatus.subID());
						selectionRequest.setCategoryID(String.valueOf(deactiveSel[i].categoryID()));
						selectionRequest.setCallerID(null);
						selectionRequest.setInLoop(true);
						Clip toBeActClip = RBTCacheManager.getInstance().getClipByRbtWavFileName(deactiveSel[i].promoId());
						selectionRequest.setClipID(String.valueOf(toBeActClip.getClipId()));
						//RBT-15187 Latest mode is passing for re-activation of songs, which is activated with different modes
						selectionRequest.setMode(deactiveSel[i].selectedBy());
						selectionRequest.setUseUIChargeClass(true);
						if(classTypeMap != null && classTypeMap.size() > 0) {
							classType = classTypeMap.get(deactiveSel[i].classType());
						}
						if(classType == null) {
							classType = deactiveSel[i].classType();
						}
						selectionRequest.setChargeClass(classType);
						RBTClient.getInstance().addSubscriberSelection(selectionRequest);
					}
				}
			}
	    }
		logger.info("All old selections added successfully...");
	}

	private Map<String,String> getClassTypeMap() {
		 ParametersCacheManager parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		 Parameters daemonParam = parametersCacheManager.getParameter("COMMON","MI_PLAYLIST_CLASSTYPE_MAP");
		 Map<String,String> classTypeMap = null;
		 if(daemonParam != null && daemonParam.getValue() != null) {
			 String classTypemapStr = daemonParam.getValue();
			 // <classtype1>:<chargeclass1>;<classtype2>:<chargeclass2>;
			 classTypeMap = MapUtils.convertToMap(classTypemapStr, ";", ":", null);
		 }
		 return classTypeMap;
	}
	
	public SubscriberDownloads getActiveSubscriberDownloadByStatus(String subscriberId,
			String promoId, String downloadStatus, int categoryId, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getActiveSubscriberDownloadByStatus(conn,
					subID(subscriberId), promoId, downloadStatus ,categoryId, categoryType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
}
