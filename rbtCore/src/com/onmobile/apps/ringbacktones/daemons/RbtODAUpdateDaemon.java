package com.onmobile.apps.ringbacktones.daemons;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberDownloadsImpl;
import com.onmobile.apps.ringbacktones.content.database.SubscriberStatusImpl;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoriesDAO;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class RbtODAUpdateDaemon extends Thread {

	private static String _class = "RbtODAUpdateDaemon";
	private static Logger logger = Logger.getLogger(RbtODAUpdateDaemon.class);
	private static Logger cdr_logger = Logger.getLogger("TEF_LIMIT_EXCEED_LOGGER");
	private RBTDaemonManager m_mainDaemonThread;
	public static RBTDBManager dbManager = null;
	static RBTCacheManager rbtCacheManager = null;
	static ParametersCacheManager m_rbtParamCacheManager = null;
	ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
	String sleepTime = "1";
	int downloadLimit = 0;
	boolean addToDownloads = false;

	protected RbtODAUpdateDaemon(RBTDaemonManager mainDaemonThread) {
		if (initParams())
			this.m_mainDaemonThread = mainDaemonThread;
		setName("RbtODAUpdateDaemon");
	}

	private boolean initParams() {
		m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		dbManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
		sleepTime = getParamAsString(iRBTConstant.DAEMON, "ODA_UPDATE_SLEEP_TIME", "1");
		downloadLimit = Integer.parseInt(getParamAsString(iRBTConstant.WEBSERVICE, "USER_DOWNLOAD_LIMIT", "0"));
		addToDownloads = Boolean.parseBoolean(getParamAsString(iRBTConstant.DAEMON, "ADD_TO_DOWNLOADS", "FALSE").toLowerCase());
		this.setName(_class);
		return true;
	}

	@Override
	public void run() {
		try {
			while (m_mainDaemonThread.isAlive()) {
				if (addToDownloads) {
					updateDownloads();
				} else {
					updateSelections();
				}
				TimeUnit.HOURS.sleep(Long.parseLong(sleepTime));
			}
		} catch (Exception e) {
			logger.info("Exception while updating selections: " + e);
			e.printStackTrace();
		}
	}

	private void updateSelections() throws Exception {

		try {
			List<ProvisioningRequests> provisioningRequestList = RBTDBManager.getInstance().getProvReqByStatus(50);
			if (provisioningRequestList != null && provisioningRequestList.size() > 0) {
				Iterator<ProvisioningRequests> provsioningListiterator = provisioningRequestList.iterator();
				while (provsioningListiterator.hasNext()) {
					ProvisioningRequests provReq = provsioningListiterator.next();
					int status = provReq.getStatus();
					String subId = provReq.getSubscriberId();
					int catId = provReq.getType();
					HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(provReq.getExtraInfo());
					String callerId = null;
					if (extraInfoMap != null && extraInfoMap.get("CALLER_ID") != null) {
						callerId = extraInfoMap.get("CALLER_ID");
					}
					String refId = provReq.getTransId();

					Category category = CategoriesDAO.getCategory(String.valueOf(catId));

					if (category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
						List<String> activeClipsWavFileInCategoryList = new ArrayList<String>();
						if (status == 50) {
							Clip[] activeClipsInCategory = rbtCacheManager.getActiveClipsInCategory(catId);

							for (int i = 0; i < activeClipsInCategory.length; i++) {
								activeClipsWavFileInCategoryList.add(activeClipsInCategory[i].getClipRbtWavFile());
							}

							String result = processUpdatedCategoryClips(subId, catId, activeClipsWavFileInCategoryList, callerId,
									refId);
							logger.info("New ODA clips added in updateSelections()... ");

							if (result.equalsIgnoreCase("success")) {

								if (status == 50)
									status = 33;

								boolean updateStatus = RBTDBManager.getInstance().updateProvisioningRequestsStatus(subId,
										provReq.getTransId(), status);

								if (updateStatus) {
									logger.info("Provisioning Request table updated successfully..");
								}

							}
						}

					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
	}

	private String processUpdatedCategoryClips(String subId, int catId, List<String> activeClipsWavFileInCategoryList,
			String callerId, String refId) throws SQLException {
		logger.info("entering processUpdatedCategoryClips()....");

		ArrayList<SubscriberStatus> selectionStatusList = RBTDBManager.getInstance()
				.getSelectionBySubsIdAndCatIdAndCallerIdAndRefId(subId, catId, callerId, refId);
		logger.info("selectionStatusList size: " + selectionStatusList.size() + " and activeClipsWavFileInCategoryList size : "
				+ activeClipsWavFileInCategoryList.size());
		if (selectionStatusList != null && selectionStatusList.size() > 0) {

			String selInterval = null;
			String internalRefID = null;
			int status = 1;
			Date startTime = null;
			Date endTime = null;
			Iterator<SubscriberStatus> selectionListIterator = selectionStatusList.iterator();
			int fromTime = 0000;
			int toTime = 2359;
			String classType = null;
			Date setTime = null;
			String selectionInfo = null;
			boolean prepaidYes;
			int categoryType = 0;
			String extraInfo = null;
			String circleId = null;
			String prepaidString = "n";
			while (selectionListIterator.hasNext()) {
				SubscriberStatus selStatusObj = selectionListIterator.next();
				String wavFileName = selStatusObj.subscriberFile();
				selInterval = selStatusObj.selInterval();
				status = selStatusObj.status();
				internalRefID = selStatusObj.refID();
				startTime = selStatusObj.startTime();
				endTime = selStatusObj.endTime();
				fromTime = selStatusObj.fromTime();
				toTime = selStatusObj.toTime();
				// RBT-15988 Playlist refresh not activating MP
				classType = "FREE";
				// selStatusObj.classType();
				setTime = selStatusObj.setTime();
				selectionInfo = selStatusObj.selectionInfo();
				prepaidYes = selStatusObj.prepaidYes();
				if (prepaidYes)
					prepaidString = "y";
				categoryType = selStatusObj.categoryType();
				extraInfo = selStatusObj.extraInfo();
				circleId = selStatusObj.circleId();

				if (activeClipsWavFileInCategoryList.contains(wavFileName)) {
					activeClipsWavFileInCategoryList.remove(wavFileName);

				} else {
					String deactivatedBy = getParamAsString(iRBTConstant.COMMON, "CALLBACKS_DEACTIVATE_MODE", "DAEMON");
					HashMap<String, String> updateClauseMap = new HashMap<String, String>();
					updateClauseMap.put("DESELECTED_BY", deactivatedBy);
					HashMap<String, String> whereClauseMap = new HashMap<String, String>();
					whereClauseMap.put("REF_ID", internalRefID);
					RBTDBManager.getInstance().deactivateSubscriberSelections(subId, updateClauseMap, whereClauseMap);
				}

			}
			logger.info("activeClipsWavFileInCategoryList size after removing: " + activeClipsWavFileInCategoryList.size());
			for (String newactiveclipWavFile : activeClipsWavFileInCategoryList) {

				internalRefID = UUID.randomUUID().toString();

				RBTDBManager.getInstance().insertNewSelectionsforODA(subId, callerId, catId, newactiveclipWavFile, setTime,
						startTime, endTime, status, classType, "DAEMON", selectionInfo, null, prepaidString, fromTime, toTime,
						true, "A", null, null, categoryType, 'l', 0, 0, selInterval, extraInfo, internalRefID, circleId);
			}

		}
		logger.info("processUpdatedCategoryClips ends successfully");
		return "success";
	}

	private String getParamAsString(String type, String param, String defaultVal) {
		try {
			return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue();
		} catch (Exception e) {
			logger.warn("Unable to get param ->" + param + "  type ->" + type);
			return defaultVal;
		}
	}

	public static void main(String[] args) {
		RBTDaemonManager rbtDaemonManager = new RBTDaemonManager();
		new RbtODAUpdateDaemon(rbtDaemonManager).start();
		System.exit(0);
	}

	private void updateDownloads() throws Exception {

		try {
			List<ProvisioningRequests> provisioningRequestList = RBTDBManager.getInstance().getProvReqByStatus(50);
			if (provisioningRequestList != null && provisioningRequestList.size() > 0) {
				Iterator<ProvisioningRequests> provsioningListiterator = provisioningRequestList.iterator();
				while (provsioningListiterator.hasNext()) {
					ProvisioningRequests provReq = provsioningListiterator.next();
					int status = provReq.getStatus();
					String subId = provReq.getSubscriberId();
					int catId = provReq.getType();
					HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(provReq.getExtraInfo());
					String callerId = null;
					if (extraInfoMap != null && extraInfoMap.get("CALLER_ID") != null) {
						callerId = extraInfoMap.get("CALLER_ID");
					}
					String refId = provReq.getTransId();

					Category category = CategoriesDAO.getCategory(String.valueOf(catId));
					SimpleDateFormat dateFormat = new SimpleDateFormat(iRBTConstant.kDateFormatwithTime);
					String currentTime = dateFormat.format(new Date());

					if (category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
						List<String> activeClipsWavFileInCategoryList = new ArrayList<String>();
						ArrayList<Clip> clipsTobeAdded = (ArrayList<Clip>) dbManager
								.clipstoAddAfterLimitCheck(subId, catId, null);
						String result = null;
						if (status == 50 && clipsTobeAdded != null && !clipsTobeAdded.isEmpty()) {

							Clip[] activeClipsInCategory = rbtCacheManager.getActiveClipsInCategory(catId);
							for (int i = 0; i < activeClipsInCategory.length; i++) {
								activeClipsWavFileInCategoryList.add(activeClipsInCategory[i].getClipRbtWavFile());
							}
							SubscriberDownloads[] subscriberDownloads = dbManager
									.getSubscriberActiveDownloadsByDownloadStatusAndCategory(subId, category.getCategoryId(),
											category.getCategoryTpe());
							SubscriberDownloads subscriberDownload = null;
							for (Clip clip : clipsTobeAdded) {

								String subscriberWavFile = clip.getClipRbtWavFile();

								subscriberDownload = dbManager.insertSubscriberDownloadRow(subId, subscriberWavFile,
										category.getCategoryId(), subscriberDownloads[0].endTime(), true,
										category.getCategoryTpe(), subscriberDownloads[0].classType(),
										subscriberDownloads[0].selectedBy(), subscriberDownloads[0].selectionInfo(),
										subscriberDownloads[0].extraInfo(), false, "n", null);
							}
							if (subscriberDownload != null) {
								result = processUpdatedDownloadCategoryClips(subId, catId, activeClipsWavFileInCategoryList,
										callerId, refId);
								logger.info("New ODA clips added in updateSelections()... ");
							}

						} else {
							cdr_logger.info(currentTime + "," + subId + "," + catId + "," + null
									+ "," + null + "," + "ODA_REFRESH");
							logger.info("Exceeding limit for category :" + catId + " not refreshing playlist"
									+ " for subscriber Id : " + subId);
							result = "success";
						}

						if (result.equalsIgnoreCase("success")) {

							if (status == 50)
								status = 33;

							boolean updateStatus = RBTDBManager.getInstance().updateProvisioningRequestsStatus(subId,
									provReq.getTransId(), status);

							if (updateStatus) {
								logger.info("Provisioning Request table updated successfully..");
							}

						}

					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
	}

	private String processUpdatedDownloadCategoryClips(String subId, int catId, List<String> activeClipsWavFileInCategoryList,
			String callerId, String refId) throws SQLException {
		logger.info("entering processUpdatedCategoryClips()....");

		ArrayList<SubscriberStatus> selectionStatusList = RBTDBManager.getInstance()
				.getSelectionBySubsIdAndCatIdAndCallerIdAndRefId(subId, catId, callerId, refId);
		logger.info("selectionStatusList size: " + selectionStatusList.size() + " and activeClipsWavFileInCategoryList size : "
				+ activeClipsWavFileInCategoryList.size());
		if (selectionStatusList != null && selectionStatusList.size() > 0) {

			String selInterval = null;
			String internalRefID = null;
			int status = 1;
			Date startTime = null;
			Date endTime = null;
			Iterator<SubscriberStatus> selectionListIterator = selectionStatusList.iterator();
			int fromTime = 0000;
			int toTime = 2359;
			String classType = null;
			Date setTime = null;
			String selectionInfo = null;
			boolean prepaidYes;
			int categoryType = 0;
			String extraInfo = null;
			String circleId = null;
			String prepaidString = "n";
			while (selectionListIterator.hasNext()) {
				SubscriberStatus selStatusObj = selectionListIterator.next();
				String wavFileName = selStatusObj.subscriberFile();
				selInterval = selStatusObj.selInterval();
				status = selStatusObj.status();
				internalRefID = selStatusObj.refID();
				startTime = selStatusObj.startTime();
				endTime = selStatusObj.endTime();
				fromTime = selStatusObj.fromTime();
				toTime = selStatusObj.toTime();
				// RBT-15988 Playlist refresh not activating MP
				classType = "FREE";
				// selStatusObj.classType();
				setTime = selStatusObj.setTime();
				selectionInfo = selStatusObj.selectionInfo();
				prepaidYes = selStatusObj.prepaidYes();
				if (prepaidYes)
					prepaidString = "y";
				categoryType = selStatusObj.categoryType();
				extraInfo = selStatusObj.extraInfo();
				circleId = selStatusObj.circleId();

				if (activeClipsWavFileInCategoryList.contains(wavFileName)) {
					activeClipsWavFileInCategoryList.remove(wavFileName);

				} else {
					String deactivatedBy = getParamAsString(iRBTConstant.COMMON, "CALLBACKS_DEACTIVATE_MODE", "DAEMON");
					HashMap<String, String> updateClauseMap = new HashMap<String, String>();
					updateClauseMap.put("DESELECTED_BY", deactivatedBy);
					HashMap<String, String> whereClauseMap = new HashMap<String, String>();
					whereClauseMap.put("REF_ID", internalRefID);
					RBTDBManager.getInstance().deactivateSubscriberSelections(subId, updateClauseMap, whereClauseMap);
				}

			}
			logger.info("activeClipsWavFileInCategoryList size after removing: " + activeClipsWavFileInCategoryList.size());
			for (String newactiveclipWavFile : activeClipsWavFileInCategoryList) {

				internalRefID = UUID.randomUUID().toString();

				RBTDBManager.getInstance().insertNewSelectionsforODA(subId, callerId, catId, newactiveclipWavFile, setTime,
						startTime, endTime, status, classType, "DAEMON", selectionInfo, null, prepaidString, fromTime, toTime,
						true, "W", null, null, categoryType, 'l', 0, 0, selInterval, extraInfo, internalRefID, circleId);
			}

		}
		logger.info("processUpdatedCategoryClips ends successfully");
		return "success";
	}

}