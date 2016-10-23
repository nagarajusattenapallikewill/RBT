package com.onmobile.apps.ringbacktones.content.database;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.daemons.ClearAccessTableTask;
import com.onmobile.apps.ringbacktones.daemons.SMDaemonPerformanceMonitor;
import com.onmobile.apps.ringbacktones.monitor.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

/**
 * @author vinayasimha.patil
 * 
 *         Added SELECTION_INFO column to RBT_SUBSCRIBER_DOWNLOADS table
 * @author laxmankumar
 */
public class SubscriberDownloadsImpl extends RBTPrimitive implements
		SubscriberDownloads, iRBTConstant {
	private static Logger logger = Logger
			.getLogger(SubscriberDownloadsImpl.class);

	private static final String TABLE_NAME = "RBT_SUBSCRIBER_DOWNLOADS";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String PROMO_ID_COL = "PROMO_ID";
	private static final String DOWNLOAD_STATUS_COL = "DOWNLOAD_STATUS";
	//Added for TS-6705
	private static final String DOWNLOAD_PROMO_ID = "PROMO_ID";
	//End of TS-6705
	private static final String SET_TIME_COL = "SET_TIME";
	private static final String START_TIME_COL = "START_TIME";
	private static final String END_TIME_COL = "END_TIME";
	private static final String CATEGORY_ID_COL = "CATEGORY_ID";
	// private static final String CLIP_YES_COL = "CLIP_YES";
	private static final String DEACTIVATION_INFO_COL = "DEACTIVATION_INFO";
	private static final String CATEGORY_TYPE_COL = "CATEGORY_TYPE";
	private static final String CLASS_TYPE_COL = "CLASS_TYPE";
	private static final String SELECTED_BY_COL = "SELECTED_BY";
	private static final String DESELECTED_BY_COL = "DESELECTED_BY";
	private static final String INTERNAL_REF_ID_COL = "INTERNAL_REF_ID";
	private static final String EXTRA_INFO_COL = "EXTRA_INFO";
	private static final String SELECTION_INFO_COL = "SELECTION_INFO";
	private static final String RETRY_COUNT_COL = "RETRY_COUNT";
	private static final String NEXT_RETRY_TIME_COL = "NEXT_RETRY_TIME";
	private static final String LAST_CHARGED_DATE_COL = "LAST_CHARGED_DATE";
	private static final String NEXT_BILLING_DATE = "NEXT_BILLING_DATE";
	// private static final String STATE_DOWNLOAD_TO_BE_ACTIVATED =
	// "DESELECTED_BY";
	// private static final String STATE_DOWNLOAD_TO_BE_DEACTIVATED =
	// "DESELECTED_BY";

	private String m_subscriberID;
	private String m_promoID;
	private char m_downloadStatus;
	private Date m_setTime;
	private Date m_startTime;
	private Date m_endTime;
	private int m_categoryID;
	// private char m_clipYes;
	private String m_deactivatedBy;
	private int m_categoryType;
	private String m_classType;
	private String m_selectedBy;
	private String m_deselectedBy;
	private String m_refID;
	private String m_extraInfo;
	private String m_selectionInfo;
	private String retryCount;
	private Date nextRetryTime;
	private Date lastChargedDate;
	private Date nextBillingDate;

	private static String m_databaseType = getDBSelectionString();

	public SubscriberDownloadsImpl(String subscriberID, String promoID,
			char downloadStatus, Date setTime, Date startTime, Date endTime,
			int categoryID, String deactivatedBy, int categoryType,
			String classType, String selBy, String selectionInfo, String refID,
			String extraInfo) {
		m_subscriberID = subscriberID;
		m_promoID = promoID;
		m_downloadStatus = downloadStatus;
		m_setTime = setTime;
		m_startTime = startTime;
		m_endTime = endTime;
		m_categoryID = categoryID;
		// m_clipYes = clipYes;
		m_deactivatedBy = deactivatedBy;
		m_categoryType = categoryType;
		m_classType = classType;
		m_selectedBy = selBy;
		m_refID = refID;
		m_extraInfo = extraInfo;
		m_selectionInfo = selectionInfo;
	}

	public SubscriberDownloadsImpl(String subscriberID, String promoID,
			char downloadStatus, Date setTime, Date startTime, Date endTime,
			int categoryID, String deactivatedBy, int categoryType,
			String classType, String selBy, String selectionInfo, String refID,
			String extraInfo, String retryCount, Date nextRetryTime, Date lastChargedDate,Date nextBillingDate) {
		m_subscriberID = subscriberID;
		m_promoID = promoID;
		m_downloadStatus = downloadStatus;
		m_setTime = setTime;
		m_startTime = startTime;
		m_endTime = endTime;
		m_categoryID = categoryID;
		// m_clipYes = clipYes;
		m_deactivatedBy = deactivatedBy;
		m_categoryType = categoryType;
		m_classType = classType;
		m_selectedBy = selBy;
		m_refID = refID;
		m_extraInfo = extraInfo;
		m_selectionInfo = selectionInfo;
		this.retryCount = retryCount;
		this.nextRetryTime = nextRetryTime;
		this.lastChargedDate = lastChargedDate;
		this.nextBillingDate = nextBillingDate;
	}

	public char downloadStatus() {
		return m_downloadStatus;
	}

	public String promoId() {
		return m_promoID;
	}

	public String subscriberId() {
		return m_subscriberID;
	}

	public Date setTime() {
		return m_setTime;
	}

	public Date startTime() {
		return m_startTime;
	}

	public Date endTime() {
		return m_endTime;
	}

	public String refID() {
		return m_refID;
	}

	public int categoryID() {
		return m_categoryID;
	}

	/*
	 * public boolean clipYes() { return (m_clipYes == 'y' || m_clipYes == 'Y');
	 * }
	 */

	public String deactivatedBy() {
		return m_deactivatedBy;
	}

	public int categoryType() {
		return m_categoryType;
	}

	public String classType() {
		return m_classType;
	}

	public String selectedBy() {
		return m_selectedBy;
	}

	public String deselectedBy() {
		return m_deselectedBy;
	}

	public String extraInfo() {
		return m_extraInfo;
	}

	public String selectionInfo() {
		return m_selectionInfo;
	}

	public String retryCount() {
		return retryCount;
	}

	public Date nextRetryTime() {
		return nextRetryTime;
	}
	
	public Date lastChargedDate() {
		return lastChargedDate;
	}
	
	public Date nextBillingDate(){
		return nextBillingDate;
	}
	
	public static SubscriberDownloads insert(Connection conn,
			String subscriberID, String promoID, int categoryID, Date endTime,
			boolean downloaded, int categoryType, String classType,
			String selBy, String selectionInfo) {
		String method = "insert";
		logger.info("RBT::inside " + method);

		Calendar cal = Calendar.getInstance();

		Date setTime = cal.getTime();

		if (!downloaded)
			cal.set(2004, 0, 1, 0, 0, 0);
		Date startTime = cal.getTime();

		if (endTime == null) {
			cal.set(2037, 11, 31, 0, 0, 0);
			endTime = cal.getTime();
		}

		String downloadStatus = "n";
		if (downloaded)
			downloadStatus = "y";

		Statement stmt = null;
		SubscriberDownloads download = null;
		int n = -1;

		String query = "INSERT INTO " + TABLE_NAME + "(";
		query += SUBSCRIBER_ID_COL + ", ";
		query += PROMO_ID_COL + ", ";
		query += DOWNLOAD_STATUS_COL + ", ";
		query += SET_TIME_COL + ", ";
		query += START_TIME_COL + ", ";
		query += END_TIME_COL + ", ";
		query += CATEGORY_ID_COL + ", ";
		// query += CLIP_YES_COL + ", " ;
		query += CATEGORY_TYPE_COL;
		query += ", " + CLASS_TYPE_COL;
		query += ", " + SELECTED_BY_COL;
		query += ", " + SELECTION_INFO_COL;
		query += ", " + INTERNAL_REF_ID_COL;
		query += ") VALUES (";

		query += sqlString(subscriberID) + ", ";
		query += sqlString(promoID) + ", ";
		query += sqlString(downloadStatus) + ", ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(setTime) + ", ";
		else
			query += mySQLDateTime(setTime) + ", ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(startTime) + ", ";
		else
			query += mySQLDateTime(startTime) + ", ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(endTime) + ", ";
		else
			query += mySQLDateTime(endTime) + ", ";
		query += categoryID + ", ";
		// query += sqlString(clipYes+"") + ", " ;
		query += categoryType;
		query += ", " + sqlString(classType);
		query += ", " + sqlString(selBy);
		query += ", " + sqlString(selectionInfo);
		query += ", " + sqlString(UUID.randomUUID().toString());
		query += ")";

		logger.info("RBT:: query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
			if (n == 1)
				download = new SubscriberDownloadsImpl(subscriberID, promoID,
						downloadStatus.charAt(0), setTime, startTime, endTime,
						categoryID, null, categoryType, classType, selBy,
						selectionInfo, null, null);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return download;
	}

	public static SubscriberDownloads insertRW(Connection conn,
			String subscriberID, String promoID, int categoryID, Date endTime,
			boolean isSubActive, int categoryType, String classType,
			String selBy, String selectionInfo, String extraInfo,
			boolean isSmClientModel, String downloadStatus, String refID) {
		String method = "insert";
		logger.info("RBT::inside " + method);

		Calendar cal = Calendar.getInstance();

		Date setTime = cal.getTime();
		cal.set(2004, 0, 1, 0, 0, 0);
		Date startTime = cal.getTime();

		if (endTime == null) {
			cal.set(2037, 11, 31, 0, 0, 0);
			endTime = cal.getTime();
		}

		// String downloadStatus = "w";
		if (downloadStatus == null) {
			downloadStatus = "w";
			if (isSubActive)
				downloadStatus = "n";

			if (isSmClientModel)
				downloadStatus = "p";
		}

		Statement stmt = null;
		SubscriberDownloads download = null;
		int n = -1;

		if (refID == null) {
			refID = UUID.randomUUID().toString();
		}

		String query = "INSERT INTO " + TABLE_NAME + "(";
		query += SUBSCRIBER_ID_COL + ", ";
		query += PROMO_ID_COL + ", ";
		query += DOWNLOAD_STATUS_COL + ", ";
		query += SET_TIME_COL + ", ";
		query += START_TIME_COL + ", ";
		query += END_TIME_COL + ", ";
		query += CATEGORY_ID_COL + ", ";
		// query += CLIP_YES_COL + ", " ;
		query += CATEGORY_TYPE_COL;
		query += ", " + CLASS_TYPE_COL;
		query += ", " + SELECTED_BY_COL;
		query += ", " + INTERNAL_REF_ID_COL;
		query += ", " + EXTRA_INFO_COL;
		query += ", " + SELECTION_INFO_COL;
		query += ") VALUES (";

		query += sqlString(subscriberID) + ", ";
		query += sqlString(promoID) + ", ";
		query += sqlString(downloadStatus) + ", ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(setTime) + ", ";
		else
			query += mySQLDateTime(setTime) + ", ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(startTime) + ", ";
		else
			query += mySQLDateTime(startTime) + ", ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(endTime) + ", ";
		else
			query += mySQLDateTime(endTime) + ", ";
		query += categoryID + ", ";
		// query += sqlString(clipYes+"") + ", " ;
		query += categoryType;
		query += ", " + sqlString(classType);
		query += ", " + sqlString(selBy);
		query += ", " + sqlString(refID);
		query += ", " + sqlString(extraInfo);
		query += ", " + sqlString(selectionInfo);
		query += ")";

		logger.info("RBT:: query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
			if (n == 1)
				download = new SubscriberDownloadsImpl(subscriberID, promoID,
						downloadStatus.charAt(0), setTime, startTime, endTime,
						categoryID, null, categoryType, classType, selBy,
						selectionInfo, refID, extraInfo);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return download;
	}

	static SubscriberDownloads getSubscriberDownload(Connection conn,
			String subscriberID, String wavFile, int categoryId,
			int categoryType, boolean doFilterTstatus) {
		String method = "getSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads download = null;

		String query = null;
		if (Utility.isShuffleCategory(categoryType))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + CATEGORY_ID_COL + " = " + categoryId;
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + PROMO_ID_COL + " = " + sqlString(wavFile);
		
		if(doFilterTstatus) {
			if (!Utility.isShuffleCategory(categoryType)) {
			query +=  " AND " + CATEGORY_TYPE_COL + " IN(" + sqlString(String.valueOf(DTMF_CATEGORY)) +","+ sqlString(String.valueOf(SONGS))+ ")";
			}
			query +=  " AND " + DOWNLOAD_STATUS_COL + " != 't'";
		}

		logger.info("RBT::query = " + query);
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next())
				download = getSubscriberDownloadFromRS(rs);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return download;
	}

	static HashMap<String, ArrayList<String>> getRecommendationByCategoryFromDownloads(
			Connection conn, String[] categoryIDs, int recDownloadCount,
			int fetchDays, int maxRecommendationForSubscriber) {
		logger.info("RBT::inside getRecommendationByCategoryFromDownloads");

		HashMap<String, ArrayList<String>> subscriberMap = new HashMap<String, ArrayList<String>>();
		List<String> categoryIDList = Arrays.asList(categoryIDs);

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			query = "SELECT " + CATEGORY_ID_COL + "," + SUBSCRIBER_ID_COL
					+ " FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL
					+ " > TIMESTAMPADD(DAY,-" + fetchDays + ",SYSDATE())";
		} else {
			query = "SELECT " + CATEGORY_ID_COL + "," + SUBSCRIBER_ID_COL
					+ " FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL
					+ " > SYSDATE - " + fetchDays;
		}

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			HashMap<String, Integer> subscriberCategoryCountMap = new HashMap<String, Integer>();
			HashMap<String, Integer> subscriberRecCountMap = new HashMap<String, Integer>();

			while (results.next()) {
				int catID = results.getInt(CATEGORY_ID_COL);
				String subscriberID = results.getString(SUBSCRIBER_ID_COL);
				String categoryID = String.valueOf(catID);

				if (categoryIDList.contains(categoryID)) {
					String key = subscriberID + "_" + categoryID;
					if (subscriberCategoryCountMap.containsKey(key)) {
						int count = subscriberCategoryCountMap.get(key);
						count++;
						subscriberCategoryCountMap.put(key, count);
					} else {
						subscriberCategoryCountMap.put(key, 1);
					}
				}
			}

			int recCountForSubscriber = 0;
			Set<String> subscriberCategorySet = subscriberCategoryCountMap
					.keySet();
			for (String subscriberCategoryKey : subscriberCategorySet) {
				int recCount = subscriberCategoryCountMap
						.get(subscriberCategoryKey);
				String subID = subscriberCategoryKey.substring(0,
						subscriberCategoryKey.indexOf("_"));
				String catID = subscriberCategoryKey
						.substring(subscriberCategoryKey.indexOf("_") + 1);
				if (recCount >= recDownloadCount) {
					if (subscriberRecCountMap.containsKey(subID)) {
						recCountForSubscriber = subscriberRecCountMap
								.get(subID);
						if (recCountForSubscriber == maxRecommendationForSubscriber)
							continue;
						else {
							recCountForSubscriber++;
							subscriberRecCountMap.put(subID,
									recCountForSubscriber);
						}
					} else {
						subscriberRecCountMap.put(subID, 1);
					}

					ArrayList<String> subscriberList = subscriberMap.get(catID);
					if (subscriberList == null) {
						subscriberList = new ArrayList<String>();
						subscriberList.add(subID);
						subscriberMap.put(catID, subscriberList);
					} else {
						if (!subscriberList.contains(subID))
							subscriberList.add(subID);
					}
				}
			}

			if (subscriberMap.size() > 0)
				return subscriberMap;
		} catch (SQLException se) {
			logger.error("", se);
			return null;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return null;

	}

	static HashMap<String, ArrayList<String>> getRecommendationByArtistsFromDownloads(
			Connection conn,
			HashMap<String, ArrayList<String>> artistClipNameMap,
			int fetchDays, int maxRecommendationForSubscriber,
			RBTDBManager rbtDBManager) {
		logger.info("RBT::inside getRecommendationByArtistsFromDownloads");

		HashMap<String, ArrayList<String>> subscriberMap = new HashMap<String, ArrayList<String>>();

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			query = "SELECT " + PROMO_ID_COL + "," + SUBSCRIBER_ID_COL
					+ " FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL
					+ " > TIMESTAMPADD(DAY,-" + fetchDays
					+ ",SYSDATE()) ORDER BY " + SUBSCRIBER_ID_COL;
		} else {
			query = "SELECT " + PROMO_ID_COL + "," + SUBSCRIBER_ID_COL
					+ " FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL
					+ " > SYSDATE - " + fetchDays + " ORDER BY "
					+ SUBSCRIBER_ID_COL;
		}

		logger.info("RBT::query " + query);

		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			int dwnCnt = 0;
			HashMap<String, Integer> subscriberRecCountMap = new HashMap<String, Integer>();
			while (results.next()) {
				String wavFile = results.getString(PROMO_ID_COL);
				String subscriberID = results.getString(SUBSCRIBER_ID_COL);

				ClipMinimal clip = rbtDBManager.getClipRBT(wavFile);

				String artist = null;
				if (clip != null && clip.getArtist() != null)
					artist = clip.getArtist().toLowerCase().trim();

				if (artist != null && artistClipNameMap.containsKey(artist)) {
					if (subscriberRecCountMap.containsKey(subscriberID)) {
						dwnCnt = subscriberRecCountMap.get(subscriberID);
						if (dwnCnt == maxRecommendationForSubscriber)
							continue;
						else {
							dwnCnt++;
							subscriberRecCountMap.put(subscriberID, dwnCnt);
						}
					} else {
						dwnCnt = 1;
						subscriberRecCountMap.put(subscriberID, dwnCnt);
					}

					ArrayList<String> subscriberList = subscriberMap
							.get(artist);
					if (subscriberList == null) {
						subscriberList = new ArrayList<String>();
						subscriberList.add(subscriberID);
						subscriberMap.put(artist, subscriberList);
					} else {
						if (!subscriberList.contains(subscriberID))
							subscriberList.add(subscriberID);
					}
				}
			}

			if (subscriberMap.size() > 0)
				return subscriberMap;
		} catch (SQLException se) {
			logger.error("", se);
			return null;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return null;
	}

	static SubscriberDownloads getActiveSubscriberDownload(Connection conn,
			String subscriberID, String promoID) {
		String method = "getActiveSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads download = null;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + PROMO_ID_COL + " = " + sqlString(promoID)
					+ " AND " + END_TIME_COL + " > SYSDATE";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + PROMO_ID_COL + " = " + sqlString(promoID)
					+ " AND " + END_TIME_COL + " > SYSDATE()";

		logger.info("RBT::query = " + query);

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next())
				download = getSubscriberDownloadFromRS(rs);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return download;
	}

	static SubscriberDownloads[] getActivateNActPendingDownloads(
			Connection conn, String subscriberID) {

		String method = "getActivationPendingDownloads";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads download = null;
		ArrayList downloadList = new ArrayList();
		String query = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " IN ('"
				+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "','"
				+ STATE_DOWNLOAD_ACTIVATION_PENDING + "','"
				+ STATE_DOWNLOAD_BASE_ACT_PENDING + "','"
				+ STATE_DOWNLOAD_ACTIVATED + "')";

		logger.info("RBT::query = " + query);

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next() && rs != null)
				downloadList.add(getSubscriberDownloadFromRS(rs));
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (downloadList.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME
					+ " successful");
			return (SubscriberDownloads[]) downloadList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::No records in " + TABLE_NAME);
			return null;
		}

	}

	static SubscriberDownloads[] getAllVoluntarySuspendedDownloads(
			Connection conn, String subscriberID) {

		String method = "getAllVoluntarySuspendedDownloads";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads download = null;
		ArrayList downloadListTemp = new ArrayList();
		ArrayList downloadList = new ArrayList();
		String query = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " IN ('" + STATE_DOWNLOAD_ACTIVATED
				+ "')";

		logger.info("RBT::query = " + query);

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next() && rs != null)
				downloadListTemp.add(getSubscriberDownloadFromRS(rs));
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (downloadListTemp.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME
					+ " successful");
			for (int count = 0; count < downloadListTemp.size(); count++) {
				SubscriberDownloads downloadLocal = (SubscriberDownloads) downloadListTemp
						.get(count);
				String extraInfo = downloadLocal.extraInfo();
				HashMap<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(extraInfo);
				if (extraInfoMap != null
						&& extraInfoMap.containsKey("VOLUNTARY")) {
					String val = (String) extraInfoMap.get("VOLUNTARY");
					if (val != null && val.equalsIgnoreCase("true")) {
						downloadList.add(downloadLocal);
					}
				}
			}
			if (downloadList.size() > 0) {
				return (SubscriberDownloads[]) downloadList
						.toArray(new SubscriberDownloads[0]);
			} else {
				logger.info("RBT::No Voluntary suspended records in "
						+ TABLE_NAME);
				return null;
			}
		} else {
			logger.info("RBT::No suspended records in " + TABLE_NAME);
			return null;
		}

	}

	static SubscriberDownloads reactivate(Connection conn, String subscriberID,
			String promoID, int categoryID, Date endTime, int categoryType,
			boolean downloaded, String classType, String selBy,
			String selectionInfo) {
		String method = "reactivate";
		logger.info("RBT:: inside " + method);

		Calendar cal = Calendar.getInstance();

		Date setTime = cal.getTime();
		cal.set(2004, 0, 1, 0, 0, 0);
		Date startTime = cal.getTime();

		if (endTime == null) {
			cal.set(2037, 11, 31, 0, 0, 0);
			endTime = cal.getTime();
		}

		String downloadStatus = "n";
		if (downloaded)
			downloadStatus = "y";
		Statement stmt = null;
		SubscriberDownloads download = null;
		int n = -1;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + CATEGORY_ID_COL + " = "
					+ categoryID + ", " + DEACTIVATION_INFO_COL + " = NULL, "
					+ SET_TIME_COL + " = " + sqlTime(setTime) + ", "
					+ START_TIME_COL + " = " + sqlTime(startTime) + ", "
					+ END_TIME_COL + " = " + sqlTime(endTime) + ", "
					+ SELECTED_BY_COL + " = " + sqlString(selBy) + " , "
					+ INTERNAL_REF_ID_COL + " = "
					+ sqlString(UUID.randomUUID().toString()) + " , "
					+ DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
					+ ", " + CLASS_TYPE_COL + " = " + sqlString(classType)
					+ " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + " AND " + PROMO_ID_COL + " = "
					+ sqlString(promoID);
		else
			query = "UPDATE " + TABLE_NAME + " SET " + CATEGORY_ID_COL + " = "
					+ categoryID + ", " + DEACTIVATION_INFO_COL + " = NULL, "
					+ SET_TIME_COL + " = " + mySQLDateTime(setTime) + ", "
					+ START_TIME_COL + " = " + mySQLDateTime(startTime) + ", "
					+ END_TIME_COL + " = " + mySQLDateTime(endTime) + ", "
					+ SELECTED_BY_COL + " = " + sqlString(selBy) + " , "
					+ INTERNAL_REF_ID_COL + " = "
					+ sqlString(UUID.randomUUID().toString()) + " , "
					+ DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
					+ ", " + CLASS_TYPE_COL + " = " + sqlString(classType)
					+ "  WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + " AND " + PROMO_ID_COL + " = "
					+ sqlString(promoID);

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (n == 1)
			download = new SubscriberDownloadsImpl(subscriberID, promoID,
					downloadStatus.charAt(0), setTime, startTime, endTime,
					categoryID, null, categoryType, classType, selBy,
					selectionInfo, null, null);

		return download;
	}

	static SubscriberDownloads reactivateRW(Connection conn,
			String subscriberID, String promoID, int categoryID, Date endTime,
			int categoryType, boolean isSubActive, String classType,
			String selBy, String selectionInfo, String extraInfo,
			boolean isSmClientModel) {
		return reactivateRW(conn, subscriberID, promoID, categoryID, endTime,
				categoryType, isSubActive, classType, selBy, selectionInfo,
				extraInfo, isSmClientModel, null);
	}

	static SubscriberDownloads reactivateRW(Connection conn,
			String subscriberID, String promoID, int categoryID, Date endTime,
			int categoryType, boolean isSubActive, String classType,
			String selBy, String selectionInfo, String extraInfo,
			boolean isSmClientModel, String downloadStatus) {
		String method = "reactivate";
		logger.info("RBT:: inside " + method);

		Calendar cal = Calendar.getInstance();

		Date setTime = cal.getTime();
		cal.set(2004, 0, 1, 0, 0, 0);
		Date startTime = cal.getTime();

		if (endTime == null) {
			cal.set(2037, 11, 31, 0, 0, 0);
			endTime = cal.getTime();
		}

		// String downloadStatus = "w";
		if (downloadStatus == null) {
			downloadStatus = "w";
			if (isSubActive)
				downloadStatus = "n";
			if (isSmClientModel)
				downloadStatus = "p";
		}

		Statement stmt = null;
		SubscriberDownloads download = null;
		int n = -1;

		String query = null;
		String refID = UUID.randomUUID().toString();
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + CATEGORY_ID_COL + " = "
					+ categoryID + ", " + DEACTIVATION_INFO_COL + " = NULL, "
					+ SET_TIME_COL + " = " + sqlTime(setTime) + ", "
					+ START_TIME_COL + " = " + sqlTime(startTime) + ", "
					+ END_TIME_COL + " = " + sqlTime(endTime) + ", "
					+ SELECTED_BY_COL + " = " + sqlString(selBy) + " , "
					+ INTERNAL_REF_ID_COL + " = "
					+ sqlString(refID) + " , "
					+ DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
					+ ", " + CLASS_TYPE_COL + " = " + sqlString(classType)
					+ ", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo)
					+ ", " + SELECTION_INFO_COL + " = "
					+ sqlString(selectionInfo) + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
					+ " = " + sqlString(promoID);
		else
			query = "UPDATE " + TABLE_NAME + " SET " + CATEGORY_ID_COL + " = "
					+ categoryID + ", " + DEACTIVATION_INFO_COL + " = NULL, "
					+ SET_TIME_COL + " = " + mySQLDateTime(setTime) + ", "
					+ START_TIME_COL + " = " + mySQLDateTime(startTime) + ", "
					+ END_TIME_COL + " = " + mySQLDateTime(endTime) + ", "
					+ SELECTED_BY_COL + " = " + sqlString(selBy) + " , "
					+ INTERNAL_REF_ID_COL + " = "
					+ sqlString(refID) + " , "
					+ DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
					+ ", " + CLASS_TYPE_COL + " = " + sqlString(classType)
					+ ", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo)
					+ ", " + SELECTION_INFO_COL + " = "
					+ sqlString(selectionInfo) + "  WHERE " + SUBSCRIBER_ID_COL
					+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
					+ " = " + sqlString(promoID);

		logger.info("RBT::query = " + query);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (n == 1)
			download = new SubscriberDownloadsImpl(subscriberID, promoID,
					downloadStatus.charAt(0), setTime, startTime, endTime,
					categoryID, null, categoryType, classType, selBy,
					selectionInfo, refID, extraInfo);

		return download;
	}

	static boolean updateDownloads(Connection conn, String subscriberID,
			String promoID, char downloadStatus) {
		String method = "updateDownloadStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "' WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
				+ " = " + sqlString(promoID);

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean updateDownloadStatusExtrainfoNChargeclass(Connection conn,
			String subscriberID, String refID, char downloadStatus,
			String extraInfo, String chargeClass) {
		String method = "updateDownloadStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "' , " + EXTRA_INFO_COL + " = "
				+ sqlString(extraInfo);
		if (chargeClass != null)
			query = query + " , " + CLASS_TYPE_COL + " = '" + chargeClass + "'";

		query = query + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + INTERNAL_REF_ID_COL
				+ " = " + sqlString(refID);

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean updateDownloadStatus(Connection conn, String subscriberID,
			String promoID, char downloadStatus) {
		String method = "updateDownloadStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "' WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
				+ " = " + sqlString(promoID);

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean smURLDownloadActivation(Connection conn,
			String subscriberID, String refID, char downloadStatus,
			String classType, String extraInfo) {
		String method = "smURLDownloadActivation";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "'";

		if (classType != null)
			query += ", " + CLASS_TYPE_COL + " = '" + classType + "'";

		if (extraInfo != null) {
			if (extraInfo.equalsIgnoreCase("NULL"))
				query += ", " + EXTRA_INFO_COL + " = NULL";
			else
				query += ", " + EXTRA_INFO_COL + " = '" + extraInfo + "'";
		}

		query = query + " , " + RETRY_COUNT_COL + " = NULL, "
				+ NEXT_RETRY_TIME_COL + " = NULL";

		query += " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + INTERNAL_REF_ID_COL;

		if (refID == null)
			query += " IS NULL";
		else
			query += " = " + sqlString(refID);

		query += " AND " + DOWNLOAD_STATUS_COL + " IN ('"
				+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "', '"
				+ STATE_DOWNLOAD_BASE_ACT_PENDING + "', '"
				+ STATE_DOWNLOAD_ACTIVATION_PENDING + "', '"
				+ STATE_DOWNLOAD_CHANGE + "')";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean smUpdateDownloadtoBeActivated(Connection conn,
			String subscriberID, boolean isPack, List<String> refIDlist) {
		Statement stmt = null;
		int n = -1;

		String refIDStr = "";
		if (refIDlist != null) {
			for (int i = 0; i < refIDlist.size(); i++) {
				if (i < refIDlist.size() - 1)
					refIDStr += "'" + refIDlist.get(i) + "',";
				else
					refIDStr += "'" + refIDlist.get(i) + "'";
			}
		}

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + STATE_DOWNLOAD_TO_BE_ACTIVATED + "' WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " = '"
				+ STATE_DOWNLOAD_BASE_ACT_PENDING + "'";
		;

		if (isPack)
			query += " AND (" + EXTRA_INFO_COL + " NOT LIKE '%PACK%' OR "
					+ EXTRA_INFO_COL + " IS NULL)";

		if (refIDStr.length() > 0)
			query += " AND " + INTERNAL_REF_ID_COL + " IN (" + refIDStr + ")";

		logger.info("RBT:: sql query :" + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
				return false;
			}
		}
		return true;
	}

	static boolean smDownloadDeActivation(Connection conn, String subscriberID,
			String refID, char downloadStatus) {
		String method = "smDownloadDeActivationSuccess";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "' WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
				+ DOWNLOAD_STATUS_COL + " in ('"
				+ STATE_DOWNLOAD_TO_BE_DEACTIVATED + "', '"
				+ STATE_DOWNLOAD_ACTIVATED + "', '"
				+ STATE_DOWNLOAD_DEACTIVATION_PENDING + "', '"
				+ STATE_DOWNLOAD_DEACT_ERROR + "')";
		
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			logger.debug("Executing query: " + query);
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	// JIRA-ID - RBT-7933 : VFQ related changes
	static SubscriberDownloads getSubscriberDownloadByRefID(Connection conn,
			String subscriberID, String refID) {
		String method = "getSubscriberDownloadByRefId";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads download = null;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID);
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID);

		logger.info("RBT::query = " + query);

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next())
				download = getSubscriberDownloadFromRS(rs);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return download;
	}

	static boolean updateSubscriberId(Connection conn, String newSubscriberId,
			String subscriberId) {
		logger.info("RBT::inside updateSubscriberId");

		String query = null;
		Statement stmt = null;
		int n = -1;

		query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIBER_ID_COL + " = '"
				+ newSubscriberId + "' WHERE " + SUBSCRIBER_ID_COL + " = '"
				+ subscriberId + "'";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
			return false;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}

	static boolean smDownloadActivationSuccess(Connection conn,
			String subscriberID, String refID, char downloadStatus,
			String classType) {
		String method = "smDownloadActivationSuccess";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String sysDateStr = "SYSDATE";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "', " + START_TIME_COL + " = "
				+ sysDateStr + ", " + CLASS_TYPE_COL + "= "
				+ sqlString(classType) + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + INTERNAL_REF_ID_COL
				+ " = " + sqlString(refID) + " AND " + DOWNLOAD_STATUS_COL
				+ " in ('" + STATE_DOWNLOAD_TO_BE_ACTIVATED + "', '"
				+ STATE_DOWNLOAD_ACTIVATION_PENDING + "', '"
				+ STATE_DOWNLOAD_ACT_ERROR + "', '" + STATE_DOWNLOAD_GRACE
				+ "')";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean smDownloadActivationSuccess(Connection conn,
			String subscriberID, String refID, char downloadStatus,
			String classType, String extraInfo,String strNextBillingDate) {
		String method = "smDownloadActivationSuccess";
		logger.info("RBT:: inside " + method);

		Date prismNextBillingDate = null;
		if(strNextBillingDate != null) {
			try {
				prismNextBillingDate = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss_a").parse(strNextBillingDate);
			} catch (ParseException e) {
				logger.warn("prism next billing date format is wrong... Expeced Format is : yyyy-MM-dd_hh:mm:ss_a, Actual Data is: "+strNextBillingDate, e);
			}
		}
		
		Statement stmt = null;
		int n = -1;

		String sysDateStr = "SYSDATE";
		
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			if (prismNextBillingDate != null) {
				strNextBillingDate = sqlTime(prismNextBillingDate);
			}
		} else {
			sysDateStr = "SYSDATE()";
			if (prismNextBillingDate != null) {
				strNextBillingDate = mySQLDateTime(prismNextBillingDate);
			}
		}

		 String query = "";
		
		 
		if(prismNextBillingDate!=null){
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = '" + downloadStatus + "', " + START_TIME_COL + " = "
					+ sysDateStr + ", " + CLASS_TYPE_COL + "= "
					+ sqlString(classType) + ", " + EXTRA_INFO_COL + "= "
					+ sqlString(extraInfo) + " , " + RETRY_COUNT_COL + " = NULL, NEXT_BILLING_DATE="+strNextBillingDate+", "
					+ NEXT_RETRY_TIME_COL + " = NULL" + ", " + LAST_CHARGED_DATE_COL + " = " + sysDateStr + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
					+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
					+ DOWNLOAD_STATUS_COL + " in ('"
					+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "', '"
					+ STATE_DOWNLOAD_ACTIVATION_PENDING + "', '"
					+ STATE_DOWNLOAD_ACT_ERROR + "', '" + STATE_DOWNLOAD_GRACE + "', '" + STATE_DOWNLOAD_CHANGE
					+ "')";
		}else{
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = '" + downloadStatus + "', " + START_TIME_COL + " = "
					+ sysDateStr + ", " + CLASS_TYPE_COL + "= "
					+ sqlString(classType) + ", " + EXTRA_INFO_COL + "= "
					+ sqlString(extraInfo) + " , " + RETRY_COUNT_COL + " = NULL, "
					+ NEXT_RETRY_TIME_COL + " = NULL" + ", " + LAST_CHARGED_DATE_COL + " = " + sysDateStr + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
					+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
					+ DOWNLOAD_STATUS_COL + " in ('"
					+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "', '"
					+ STATE_DOWNLOAD_ACTIVATION_PENDING + "', '"
					+ STATE_DOWNLOAD_ACT_ERROR + "', '" + STATE_DOWNLOAD_GRACE + "', '" + STATE_DOWNLOAD_CHANGE
					+ "')";
		}

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}
	
	static boolean smDownloadUpgradationSuccess(Connection conn,
			String subscriberID, String refID, char downloadStatus,
			String classType, String extrainfo) {
		String method = "smDownloadActivationSuccess";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String sysDateStr = "SYSDATE";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "', " + CLASS_TYPE_COL + "= "
				+ sqlString(classType) + EXTRA_INFO_COL + "= "
				+ sqlString(extrainfo) + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + INTERNAL_REF_ID_COL
				+ " = " + sqlString(refID);

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean smDownloadGraceSuccess(Connection conn, String subscriberID,
			String refID, char downloadStatus) {
		String method = "smDownloadGraceSuccess";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "' " + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
				+ DOWNLOAD_STATUS_COL + " IN ('"
				+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "', '"
				+ STATE_DOWNLOAD_ACTIVATION_PENDING + "', '"
				+ STATE_DOWNLOAD_ACT_ERROR + "')";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static SubscriberDownloads getSMDownloadForCallbackOldLogic(
			Connection conn, String subscriberID, String promoID, String setDate) {
		String method = "getSMDownloadForCallbackOldLogic";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;

		SubscriberDownloads subDownload = null;

		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ PROMO_ID_COL + " = " + sqlString(promoID) + " AND "
				+ setTimeCond + " = " + sqlString(setDate);

		logger.info("RBT::query " + query);

		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next())
				subDownload = getSubscriberDownloadFromRS(rs);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (subDownload);
	}

	static boolean updateSMDownloadForCallbackOldLogic(Connection conn,
			String subscriberID, String promoID, String setDate, String refID) {
		String method = "updateSMDownloadForCallbackOldLogic";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;

		SubscriberDownloads subDownload = null;
		int n = -1;
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";

		String query = "UPDATE " + TABLE_NAME + " SET " + INTERNAL_REF_ID_COL
				+ " = " + sqlString(refID) + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
				+ " = " + sqlString(promoID) + " AND " + setTimeCond + " = "
				+ sqlString(setDate);

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}

	static SubscriberDownloads getSMDownloadForCallback(Connection conn,
			String subscriberID, String refID) {
		String method = "getSMDownloadForCallback";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;

		SubscriberDownloads subDownload = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID);

		logger.info("RBT::query " + query);

		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next())
				subDownload = getSubscriberDownloadFromRS(rs);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (subDownload);
	}

	static boolean smURLDownloadDeActivation(Connection conn,
			String subscriberID, String refID, char downloadStatus) {
		String method = "smURLDownloadActivation";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "' WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL;

		if (refID == null)
			query += " IS NULL";
		else
			query += " = " + sqlString(refID);

		query += " AND " + DOWNLOAD_STATUS_COL + " IN ('"
				+ STATE_DOWNLOAD_TO_BE_DEACTIVATED + "', '"
				+ STATE_DOWNLOAD_CHANGE + "')";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean smDownloadActivationFailure(Connection conn,
			String subscriberID, String refID, char downloadStatus) {
		String method = "smDownloadActivationFailure";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String sysDateStr = "SYSDATE";

		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			sysDateStr = "SYSDATE()";
		}

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "', " + START_TIME_COL + " = "
				+ sysDateStr + ", " + END_TIME_COL + " = " + sysDateStr + ", "
				+ DEACTIVATION_INFO_COL + " = 'NA' WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
				+ DOWNLOAD_STATUS_COL + " in ('"
				+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "', '"
				+ STATE_DOWNLOAD_ACTIVATION_PENDING + "', '"
				+ STATE_DOWNLOAD_ACT_ERROR + "','" + STATE_DOWNLOAD_GRACE
				+ "') ";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean smDownloadTNBActivation(Connection conn,
			String subscriberID, String refID, char downloadStatus) {
		String method = "smDownloadActivationFailure";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String sysDateStr = "SYSDATE";

		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			sysDateStr = "SYSDATE()";
		}

		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "', " + START_TIME_COL + " = "
				+ sysDateStr + ", " + END_TIME_COL + " = " + sysDateStr + ", "
				+ DEACTIVATION_INFO_COL + " = 'NA' WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
				+ DOWNLOAD_STATUS_COL + " in ('"
				+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "', '"
				+ STATE_DOWNLOAD_ACTIVATION_PENDING + "', '"
				+ STATE_DOWNLOAD_ACT_ERROR + "','" + STATE_DOWNLOAD_GRACE
				+ "','" + STATE_DOWNLOAD_ACTIVATED + "')";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean smDownloadRenewalFailure(Connection conn,
			String subscriberID, String refID, char downloadStatus, String mode) {
		String method = "smDownloadRenewalFailure";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String sysDateStr = "SYSDATE";

		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		//RBT-14497 - Tone Status Check :Reset the retrycount,retrytime
		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = '" + downloadStatus + "', " + END_TIME_COL + " = "
				+ sysDateStr + ", " + DEACTIVATION_INFO_COL + " = "
				+ sqlString(mode) 
				+ " , " + RETRY_COUNT_COL + " = NULL, "
				+ NEXT_RETRY_TIME_COL + " = NULL" 
				+ " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
				+ DOWNLOAD_STATUS_COL + " in ('" + STATE_DOWNLOAD_ACTIVATED
				+ "','" + STATE_DOWNLOAD_SUSPENSION + "','"
				+ STATE_DOWNLOAD_ACT_ERROR + "','"
				+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "','"
				+ STATE_DOWNLOAD_ACTIVATION_PENDING + "','"
				+ STATE_DOWNLOAD_TO_BE_DEACTIVATED + "','"
				+ STATE_DOWNLOAD_DEACTIVATION_PENDING + "','"
				+ STATE_DOWNLOAD_BOOKMARK + "','" + STATE_DOWNLOAD_DEACT_ERROR
				+ "','" + STATE_DOWNLOAD_BASE_ACT_PENDING + "','"
				+ STATE_DOWNLOAD_GRACE + "','" + STATE_DOWNLOAD_CHANGE + "')";
		

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean updateDownloadStatusToDownloaded(Connection conn,
			String subscriberID, String promoID, Date startTime, int validity) {
		String method = "updateDownloadStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		if (startTime == null)
			startTime = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startTime);
		calendar.add(Calendar.DAY_OF_YEAR, (validity - 1));
		Date endTime = calendar.getTime();

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME
					+ " SET "
					+ DOWNLOAD_STATUS_COL
					// + " = 'y', " + START_TIME_COL + " = " +
					// sqlTime(startTime)
					+ " = 'y', " + START_TIME_COL + " = " + sqlTime(startTime)
					+ " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + " AND " + PROMO_ID_COL + " = "
					+ sqlString(promoID);
		else
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'y', " + START_TIME_COL + " = "
					+ mySQLDateTime(startTime) + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
					+ " = " + sqlString(promoID);

		logger.info("RBT:: query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	public static boolean deactivateSubscriberDownload(Connection conn,
			String subscriberId, String wavFile, int categoryId,
			int categoryType, String deactivateBy) {
		String method = "deactivateSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			if (categoryType == 0)
				query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
						+ " = 'x', " + END_TIME_COL + " = SYSDATE, "
						+ DEACTIVATION_INFO_COL + " = "
						+ sqlString(deactivateBy) + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId)
						+ "AND " + CATEGORY_ID_COL + " = " + categoryId
						+ " AND " + CATEGORY_TYPE_COL + " = 0 AND "
						+ DOWNLOAD_STATUS_COL
						+ " NOT IN ('b', 'x', 'e', 'f', 's','d')";
			else
				query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
						+ " = 'x', " + END_TIME_COL + " = SYSDATE, "
						+ DEACTIVATION_INFO_COL + " = "
						+ sqlString(deactivateBy) + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId)
						+ "AND " + PROMO_ID_COL + " = " + sqlString(wavFile)
						+ " AND " + DOWNLOAD_STATUS_COL
						+ " NOT IN ('b', 'x', 'e', 'f', 's','d')";
		} else {
			if (categoryType == 0)
				query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
						+ " = 'x', " + END_TIME_COL + " = SYSDATE(), "
						+ DEACTIVATION_INFO_COL + " = "
						+ sqlString(deactivateBy) + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId)
						+ "AND " + CATEGORY_ID_COL + " = " + categoryId
						+ " AND " + CATEGORY_TYPE_COL + " = 0 AND "
						+ DOWNLOAD_STATUS_COL
						+ " NOT IN ('b', 'x', 'e', 'f', 's','d')";
			else
				query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
						+ " = 'x', " + END_TIME_COL + " = SYSDATE(), "
						+ DEACTIVATION_INFO_COL + " = "
						+ sqlString(deactivateBy) + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId)
						+ "AND " + PROMO_ID_COL + " = " + sqlString(wavFile)
						+ " AND " + DOWNLOAD_STATUS_COL
						+ " NOT IN ('b', 'x', 'e', 'f', 's','d')";
		}

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean deactivateSubscriberDownload(Connection conn,
			String subscriberID, String promoID, String deactivationInfo) {
		String method = "deactivateSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'x', " + END_TIME_COL + " = SYSDATE, "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ "AND " + PROMO_ID_COL + " = " + sqlString(promoID);
		else
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'x', " + END_TIME_COL + " = SYSDATE(), "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ "AND " + PROMO_ID_COL + " = " + sqlString(promoID);

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean expireAllSubscriberDownload(Connection conn,
			String subscriberID, String deactivationInfo) {
		String method = "expireAllSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'd', " + END_TIME_COL + " = SYSDATE, "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " NOT IN ('x')";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'd', " + END_TIME_COL + " = SYSDATE(), "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " NOT IN ('x')";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}

	static boolean expireAllSubscriberDownloadBaseDct(Connection conn,
			String subscriberID, String deactivationInfo) {
		String method = "expireAllSubscriberDownloadBaseDct";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'x', " + END_TIME_COL + " = SYSDATE, "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " NOT IN ('x','t')";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'x', " + END_TIME_COL + " = SYSDATE(), "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " NOT IN ('x','t')";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}

	static boolean expireAllSubscriberPendingDownload(Connection conn,
			String subscriberID, String deactivationInfo, List<String> refIDList) {
		String method = "expireAllSubscriberPendingDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String refIDStr = "";
		if (refIDList != null) {
			for (int i = 0; i < refIDList.size(); i++) {
				if (i < refIDList.size() - 1)
					refIDStr += "'" + refIDList.get(i) + "',";
				else
					refIDStr += "'" + refIDList.get(i) + "'";
			}
		}
		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'x', " + END_TIME_COL + " = SYSDATE, "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " IN ( 'w', 'p' )";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'x', " + END_TIME_COL + " = SYSDATE(), "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " IN ( 'w', 'p' )";

		if (refIDStr.length() > 0)
			query += " AND " + INTERNAL_REF_ID_COL + " IN (" + refIDStr + ")";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}

	static boolean expireAllSubscriberActivationPendingDownload(
			Connection conn, String subscriberID, String deactivationInfo) {
		String method = "expireAllSubscriberActivationPendingDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'x', " + END_TIME_COL + " = SYSDATE, "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " = 'n'";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'x', " + END_TIME_COL + " = SYSDATE(), "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " =  'n'";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}

	static boolean activateDctPendingDownload(Connection conn,
			String subscriberID) {
		String method = "activateDctPendingDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;
		Date endTime = null;

		Calendar cal = Calendar.getInstance();
		cal.set(2037, 11, 31, 0, 0, 0);
		endTime = cal.getTime();

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'y', " + END_TIME_COL + " = " + sqlTime(endTime)
					+ ", " + DEACTIVATION_INFO_COL + " = NULL" + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " = 'd'";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'y', " + END_TIME_COL + " = "
					+ mySQLDateTime(endTime) + ", " + DEACTIVATION_INFO_COL
					+ " = NULL" + " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + " AND " + DOWNLOAD_STATUS_COL
					+ " =  'd'";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}

	static boolean expireSubscriberDownload(Connection conn,
			String subscriberID, String promoID, String deactivateBy,
			int categoryId, int categoryType, String deselectionInfo,
			String extraInfo, boolean doFilterTstatus, boolean isDirectDeactivation) {
		String method = "expireSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		String enddateStr = MYSQL_SYSDATE;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			enddateStr = SAPDB_SYSDATE;

		String downloadStatus = "d";
		if(isDirectDeactivation) {
			downloadStatus = "x";
		}
		
		query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = " + sqlString(downloadStatus) + ", " + END_TIME_COL + " = " + enddateStr + ", "
				+ DEACTIVATION_INFO_COL + " = " + sqlString(deactivateBy);

		if (deselectionInfo != null) {
			query += ", " + SELECTION_INFO_COL + " = CONCAT("
					+ SELECTION_INFO_COL + ", '|DCT:" + deselectionInfo + "|')";
		}

		if (extraInfo != null) {
			query += " , " + EXTRA_INFO_COL + " = " + sqlString(extraInfo);
		}
		
		

		if (Utility.isShuffleCategory(categoryType)
				|| categoryType == iRBTConstant.DYNAMIC_SHUFFLE) {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + CATEGORY_ID_COL
					+ " = " + categoryId + " AND " + CATEGORY_TYPE_COL + " = "
					+ categoryType + " AND " + END_TIME_COL + " > "
					+ enddateStr;
		} else {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + "AND " + PROMO_ID_COL + " = "
					+ sqlString(promoID) + " AND " + END_TIME_COL + " > " + enddateStr;
			if (categoryId > -1) {
				query += " AND " + CATEGORY_ID_COL + " = " + categoryId;
			}

			if (categoryType > -1) {
				query += " AND " + CATEGORY_TYPE_COL + " = " + categoryType;

			}
		}
		

		if(doFilterTstatus) {
			query += " AND " + DOWNLOAD_STATUS_COL + " NOT IN('b','t')";
		}else {
			query += " AND " + DOWNLOAD_STATUS_COL + " != 'b'";
		}
		
		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean directDeactivateSubscriberDownload(Connection conn,
			String subscriberID, String promoID, String deactivateBy,
			int categoryId, int categoryType, String deselectionInfo,
			String extraInfo) {
		String method = "directDeactivateSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		String enddateStr = MYSQL_SYSDATE;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			enddateStr = SAPDB_SYSDATE;

		query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = 'x', " + END_TIME_COL + " = " + enddateStr + ", "
				+ DEACTIVATION_INFO_COL + " = " + sqlString(deactivateBy);

		if (deselectionInfo != null) {
			query += ", " + SELECTION_INFO_COL + " = CONCAT("
					+ SELECTION_INFO_COL + ", '|DCT:" + deselectionInfo + "|')";
		}

		if (extraInfo != null) {
			query += " , " + EXTRA_INFO_COL + " = " + sqlString(extraInfo);
		}

		if (Utility.isShuffleCategory(categoryType)
				|| categoryType == iRBTConstant.DYNAMIC_SHUFFLE) {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + CATEGORY_ID_COL
					+ " = " + categoryId + " AND " + CATEGORY_TYPE_COL + " = "
					+ categoryType + " AND " + END_TIME_COL + " > "
					+ enddateStr + " AND " + DOWNLOAD_STATUS_COL + " != 'b'";
		} else {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + PROMO_ID_COL + " = "
					+ sqlString(promoID) + " AND " + END_TIME_COL + " > "
					+ enddateStr + " AND " + DOWNLOAD_STATUS_COL + " != 'b'";
		}

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}
	// Physically deleted from RBT_SUBSCRIBER_DOWNLOADS table for TEF-SPain song
	// re-pricing
	static boolean removeSubscriberDownload(Connection conn,
			String subscriberID, String promoID, int categoryId,
			int categoryType) {
		String method = "expireSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		String enddateStr = MYSQL_SYSDATE;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			enddateStr = SAPDB_SYSDATE;

		query = "DELETE FROM " + TABLE_NAME;

		if (Utility.isShuffleCategory(categoryType)
				|| categoryType == iRBTConstant.DYNAMIC_SHUFFLE) {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + CATEGORY_ID_COL
					+ " = " + categoryId + " AND " + CATEGORY_TYPE_COL + " = "
					+ categoryType + " AND " + END_TIME_COL + " > "
					+ enddateStr + " AND " + DOWNLOAD_STATUS_COL + " != 'b'";
		} else {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + PROMO_ID_COL + " = "
					+ sqlString(promoID) + " AND " + END_TIME_COL + " > "
					+ enddateStr + " AND " + DOWNLOAD_STATUS_COL + " != 'b'";
		}

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean expireSubscriberDownload(Connection conn,
			String subscriberID, String refID, String deactivationInfo) {
		String method = "expireSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		String enddateStr = MYSQL_SYSDATE;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			enddateStr = SAPDB_SYSDATE;
		query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " = 'd', " + END_TIME_COL + " = " + enddateStr + " , "
				+ DEACTIVATION_INFO_COL + " = " + sqlString(deactivationInfo)
				+ " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + "AND " + INTERNAL_REF_ID_COL
				+ " = '" + refID + "' AND " + DOWNLOAD_STATUS_COL + " != 'b'";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	public static SubscriberDownloads[] getActiveSubscriberDownloads(
			Connection conn, String subscriberID) {
		String method = "getActiveSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;

		List subscriberDownloadsList = new ArrayList();

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ END_TIME_COL + " > SYSDATE AND " + DOWNLOAD_STATUS_COL
					+ " != 'b' ORDER BY SET_TIME";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ END_TIME_COL + " > SYSDATE() AND " + DOWNLOAD_STATUS_COL
					+ " != 'b' ORDER BY SET_TIME";

		logger.info("RBT::query " + query);

		System.out.println(" the query is  " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(" populating array list");
				subscriberDownloadsList.add(getSubscriberDownloadFromRS(rs));
				System.out.println("size of sdl "
						+ subscriberDownloadsList.size());
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		System.out.println(" did i get here " + subscriberDownloadsList.size());
		if (subscriberDownloadsList.size() > 0) {

			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
			return (SubscriberDownloads[]) subscriberDownloadsList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}

	public static SubscriberDownloads[] getActiveSubscriberDownloads(
			Connection conn, String subscriberID, String protocolID) {
		logger.info("Getting downloads for subscriberID: " + subscriberID
				+ ", protocolID: " + protocolID);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;

		List<SubscriberDownloads> subscriberDownloadsList = new ArrayList<SubscriberDownloads>();

		try {
			if (null == subscriberID && null == protocolID) {
				logger.info("Returning null, subscriberID and protocolID are null");
				return null;
			} else if (null != protocolID && null != subscriberID) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
						+ SELECTION_INFO_COL + " LIKE '%protocolnumber:"
						+ protocolID + "%' AND " + END_TIME_COL
						+ " > SYSDATE() AND " + DOWNLOAD_STATUS_COL
						+ " != 'b' ORDER BY SET_TIME";
			} else {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
						+ END_TIME_COL + " > SYSDATE() AND " + DOWNLOAD_STATUS_COL
						+ " != 'b' ORDER BY SET_TIME";
			}
			logger.info("Excecuting query " + query);

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(" populating array list");
				subscriberDownloadsList.add(getSubscriberDownloadFromRS(rs));
				System.out.println("size of sdl "
						+ subscriberDownloadsList.size());
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (subscriberDownloadsList.size() > 0) {
			logger.info("Fetched RBT_SUBSCRIBER_DOWNLOADS, found: "
					+ subscriberDownloadsList.size());
			return (SubscriberDownloads[]) subscriberDownloadsList
					.toArray(new SubscriberDownloads[0]);
		}
		logger.info("No downloads found in RBT_SUBSCRIBER_DOWNLOADS");
		return null;
	}

	public static SubscriberDownloads[] getAllSubscriberDownloadRecordsNotDeactivated(
			Connection conn) {
		String method = "getAllSubscriberDownloadRecordsNotDeactivated";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;

		List subscriberDownloadsList = new ArrayList();

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ DOWNLOAD_STATUS_COL + " = 'y' AND " + END_TIME_COL
					+ " < SYSDATE ";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ DOWNLOAD_STATUS_COL + " = 'y' AND " + END_TIME_COL
					+ " < SYSDATE() ";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				subscriberDownloadsList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (subscriberDownloadsList.size() > 0) {

			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
			return (SubscriberDownloads[]) subscriberDownloadsList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}

	public static SubscriberDownloads[] getDeactiveSubscriberDownloads(
			Connection conn, String subscriberID) {
		String method = "getActiveSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;

		List subscriberDownloadsList = new ArrayList();

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ END_TIME_COL + " < SYSDATE AND " + DOWNLOAD_STATUS_COL
					+ " != 'b'";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ END_TIME_COL + " < SYSDATE() AND " + DOWNLOAD_STATUS_COL
					+ " != 'b'";

		logger.info("RBT::query " + query);

		System.out.println(" query in deact dl " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				subscriberDownloadsList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (subscriberDownloadsList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
			return (SubscriberDownloads[]) subscriberDownloadsList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}

	public static SubscriberDownloads[] getNonDeactiveSubscriberDownloads(
			Connection conn, String subscriberID) {
		String method = "getNonDeactiveSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;

		List<SubscriberDownloads> subscriberDownloadsList = new ArrayList<SubscriberDownloads>();

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ DOWNLOAD_STATUS_COL + " NOT IN ('b','d','s','x')";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ DOWNLOAD_STATUS_COL + " NOT IN ('b','d','s','x')";

		logger.info("RBT::query " + query);
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				subscriberDownloadsList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			closeStatementAndRS(stmt, rs);
		}

		if (subscriberDownloadsList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
			return (SubscriberDownloads[]) subscriberDownloadsList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}


	public static boolean deleteSubscriberDownloads(Connection conn, String subscriberID) {
		String method = "deleteSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		int n = -1;


		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID);

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);

		} 
		catch (SQLException se) {
			logger.error("", se);
		}
		finally {
			try {
				stmt.close();

			} 
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n >= 0);
	}
	
	public static String deleteDownloadwithTstatus(Connection conn,
			String subscriberID, String promoID) {
		String method = "deleteSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		int n = -1;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID)
				+ " AND " + PROMO_ID_COL +" = " + sqlString(promoID)
				+ " AND " + CATEGORY_TYPE_COL +" IN(" + sqlString(String.valueOf(DTMF_CATEGORY)) +","+ sqlString(String.valueOf(SONGS))+ ")"
				+ " AND " + DOWNLOAD_STATUS_COL +" = 't'";
				

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return n >= 0?"true":"false";
	}
	

	// added for Voda Romania
	public static boolean updateSubscriberDownloadsStatusToD(Connection conn,
			String subscriberID, String deactivationInfo) {

		String method = "updateSubscriberDownloadStatusToD";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'd', " + END_TIME_COL + " = SYSDATE, "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " != 'd'";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
					+ " = 'd', " + END_TIME_COL + " = SYSDATE(), "
					+ DEACTIVATION_INFO_COL + " = "
					+ sqlString(deactivationInfo) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " != 'd'";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}

	public static boolean updateDownloadRenewalSuccess(Connection conn,
			String subscriberID, String refID, String classType) {

		String method = "updateDownloadRenewalSuccess";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;
		String query = null;
		String sysDateStr = "SYSDATE";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		query = "UPDATE " + TABLE_NAME + " SET " + CLASS_TYPE_COL + " = "
				+ sqlString(classType) + "," + DOWNLOAD_STATUS_COL + " = '"
				+ STATE_DOWNLOAD_ACTIVATED + "'"+ ", " +LAST_CHARGED_DATE_COL + " = " + sysDateStr + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
				+ DOWNLOAD_STATUS_COL + " IN ('" + STATE_DOWNLOAD_ACTIVATED
				+ "','" + STATE_DOWNLOAD_SUSPENSION + "','"
				+ STATE_DOWNLOAD_ACT_ERROR + "')";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}
	
	public static boolean updateDownloadClassType(Connection conn,
			String subscriberID, String refID, String classType) {

		String method = "updateDownloadRenewalSuccess";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;
		String query = null;

		query = "UPDATE " + TABLE_NAME + " SET " + CLASS_TYPE_COL + " = "
				+ sqlString(classType) + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID);

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}
	
	public static boolean deleteSubscriberDownload(Connection conn,
			String subscriberID, String wavFile, int categoryID,
			int categoryType) {
		String method = "deleteSubscriberDownload";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		int n = -1;

		if (categoryType == 0)
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + sqlString(subscriberID) + " AND "
					+ CATEGORY_ID_COL + " = " + categoryID;
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
					+ " = " + sqlString(wavFile);

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query,Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n >= 0);
	}

	public static SubscriberDownloads[] getSubscriberActiveDownloads(
			Connection conn, String subscriberID, int categoryType) {
		String method = "getSubscriberActiveDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList downloadList = new ArrayList();

		/*
		 * String clipYesStr = "n"; if(clipYes) clipYesStr = "y";
		 */

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + CATEGORY_TYPE_COL + " = " + categoryType
					+ " AND " + DOWNLOAD_STATUS_COL + " = 'y'" + " AND "
					+ END_TIME_COL + " > SYSDATE";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + CATEGORY_TYPE_COL + " = " + categoryType
					+ " AND " + DOWNLOAD_STATUS_COL + " = 'y'" + " AND "
					+ END_TIME_COL + " > SYSDATE()";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			while (rs != null && rs.next()) {
				downloadList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (downloadList.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME
					+ " successful");
			return (SubscriberDownloads[]) downloadList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::No records in " + TABLE_NAME);
			return null;
		}
	}

	public static SubscriberDownloads[] getSubscriberAllActiveDownloads(
			Connection conn, String subscriberID, int categoryType) {
		String method = "getSubscriberAllActiveDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList downloadList = new ArrayList();

		/*
		 * String clipYesStr = "n"; if(clipYes) clipYesStr = "y";
		 */

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + CATEGORY_TYPE_COL + " = " + categoryType
					+ " AND " + END_TIME_COL + " > SYSDATE AND "
					+ DOWNLOAD_STATUS_COL + " != 'b'";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + CATEGORY_TYPE_COL + " = " + categoryType
					+ " AND " + END_TIME_COL + " > SYSDATE() AND "
					+ DOWNLOAD_STATUS_COL + " != 'b'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			while (rs != null && rs.next()) {
				downloadList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (downloadList.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME
					+ " successful");
			return (SubscriberDownloads[]) downloadList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::No records in " + TABLE_NAME);
			return null;
		}
	}

	public static SubscriberDownloads[] getSubscriberAllActiveDownloads(
			Connection conn, String subscriberID) {
		String method = "getSubscriberAllActiveDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<SubscriberDownloads> downloadList = new ArrayList<SubscriberDownloads>();

		/*
		 * String clipYesStr = "n"; if(clipYes) clipYesStr = "y";
		 */

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + END_TIME_COL + " > SYSDATE AND "
					+ DOWNLOAD_STATUS_COL + " != 'b'";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + END_TIME_COL + " > SYSDATE() AND "
					+ DOWNLOAD_STATUS_COL + " != 'b'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			while (rs != null && rs.next()) {
				downloadList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (downloadList.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME
					+ " successful");
			return (SubscriberDownloads[]) downloadList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::No active records in " + TABLE_NAME
					+ " for subscriber " + subscriberID);
			return null;
		}
	}

	public static SubscriberDownloads[] getSubscriberDownloads(Connection conn,
			String subscriberID) {
		String method = "getSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList downloadList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " != 'b' ORDER BY " + SET_TIME_COL;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			while (rs != null && rs.next()) {
				downloadList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (downloadList.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME
					+ " successful " + " size: " + downloadList.size());
			return (SubscriberDownloads[]) downloadList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::No records in " + TABLE_NAME);
			return null;
		}
	}

	static SubscriberDownloads getOldestActiveSubscriberDownload(
			Connection conn, String subscriberID, int categoryType) {
		String method = "getOldestActiveSubscriberDownload";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads download = null;

		String sysdate = "SYSDATE()";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysdate = "SYSDATE";

		String query = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND " + END_TIME_COL
				+ " > " + sysdate + " AND " + DOWNLOAD_STATUS_COL + " != 'b'";

		if (categoryType != -1) {
			query += " AND " + CATEGORY_TYPE_COL + " = " + categoryType;
		}

		query += " ORDER BY " + SET_TIME_COL;

		logger.info("RBT::query = " + query);

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next())
				download = getSubscriberDownloadFromRS(rs);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return download;
	}

	private static SubscriberDownloads getSubscriberDownloadFromRS(ResultSet rs)
			throws SQLException {
		if (rs != null) {
			String subscriberID = rs.getString(SUBSCRIBER_ID_COL);
			String promoID = rs.getString(PROMO_ID_COL);
			String downloadStatus = rs.getString(DOWNLOAD_STATUS_COL);
			Date setTime = rs.getTimestamp(SET_TIME_COL);
			Date startTime = rs.getTimestamp(START_TIME_COL);
			Date endTime = rs.getTimestamp(END_TIME_COL);
			int categoryID = rs.getInt(CATEGORY_ID_COL);
			// String clipYes = rs.getString(CLIP_YES_COL);
			String deactivationInfo = rs.getString(DEACTIVATION_INFO_COL);
			int categoryType = rs.getInt(CATEGORY_TYPE_COL);

			String classType = rs.getString(CLASS_TYPE_COL);
			String selBy = rs.getString(SELECTED_BY_COL);
			String refID = rs.getString(INTERNAL_REF_ID_COL);
			String extraInfo = rs.getString(EXTRA_INFO_COL);
			String selectionInfo = rs.getString(SELECTION_INFO_COL);
			String retryCount = rs.getString(RETRY_COUNT_COL);
			Date nextRetryTime = rs.getTimestamp(NEXT_RETRY_TIME_COL);
			Date lastChargedDate = rs.getTimestamp(LAST_CHARGED_DATE_COL);
			Date nextBillingDate = rs.getTimestamp(NEXT_BILLING_DATE);
			/*
			 * return new SubscriberDownloadsImpl(subscriberID, promoID,
			 * downloadStatus.charAt(0), setTime, startTime, endTime,
			 * categoryID, clipYes.charAt(0), deactivationInfo, categoryType);
			 */

			return new SubscriberDownloadsImpl(subscriberID, promoID,
					downloadStatus.charAt(0), setTime, startTime, endTime,
					categoryID, deactivationInfo, categoryType, classType,
					selBy, selectionInfo, refID, extraInfo, retryCount,

					nextRetryTime, lastChargedDate, nextBillingDate);
		}
		return null;
	}

	protected static String getStackTrace(Throwable ex) {
		StringWriter stringWriter = new StringWriter();
		String trace = "";
		if (ex instanceof Exception) {
			Exception exception = (Exception) ex;
			exception.printStackTrace(new PrintWriter(stringWriter));
			trace = stringWriter.toString();
			trace = trace.substring(0, trace.length() - 2);
			trace = System.getProperty("line.separator") + " \t" + trace;
		}
		return trace;
	}

	// For Merging Purpose
	public static boolean deleteSelectionToBeDeleted(Connection conn,
			String subscriberID, String promoID, int categoryID) {
		String method = "deleteSelectionToBeDeleted";
		logger.info("RBT:: inside " + method);

		String downloadStatus = "d";

		Statement stmt = null;
		int n = -1;

		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL
					+ " = SYSDATE, " + DOWNLOAD_STATUS_COL + " = '"
					+ downloadStatus + "'" + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + PROMO_ID_COL
					+ " = '" + promoID + "' AND " + CATEGORY_ID_COL + " = "
					+ categoryID;
		else
			query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL
					+ " = SYSDATE(), " + DOWNLOAD_STATUS_COL + " = '"
					+ downloadStatus + "'" + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + PROMO_ID_COL
					+ " = '" + promoID + "' AND " + CATEGORY_ID_COL + " = "
					+ categoryID;

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return (n == 1);
	}

	public static ArrayList getSelectionsToBeDeleted(Connection conn,
			int fetchSize) {
		logger.info("RBT::inside getSelectionsForCharging");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberId = null;
		String promoId = null;
		String downloadStatus = null;
		Date setTime = null;
		Date startTime = null;
		Date endTime = null;
		int categoryId = -1;
		int categoryType = -1;

		SubscriberDownloadsImpl download = null;
		ArrayList selectionsList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + DOWNLOAD_STATUS_COL
				+ " = 'd'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			stmt.setMaxRows(fetchSize);
			results = stmt.executeQuery(query);

			while (results.next()) {
				selectionsList.add(getSubscriberDownloadFromRS(results));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (selectionsList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_TOBE_DELETED_SELECTIONS successful");
			return selectionsList;
		} else {
			logger.info("RBT::no records in RBT_TOBE_DELETED_SELECTIONS");
			return null;
		}
	}

	public static SubscriberDownloads[] smGetDownloadsToBeActivated(
			Connection conn, int fetchSize) {
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		List<SubscriberDownloads> downloadsList = new ArrayList<SubscriberDownloads>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + DOWNLOAD_STATUS_COL
				+ " IN ('" + STATE_DOWNLOAD_TO_BE_ACTIVATED + "', '"
				+ STATE_DOWNLOAD_CHANGE + "')";

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND ROWNUM <="+fetchSize;
		else
			query += " LIMIT "+fetchSize;

		logger.info("Query " + query);

		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			int count = 0;
			int totalCount = 0;
			while (results.next()) {
				if (count < fetchSize) {
					downloadsList.add(getSubscriberDownloadFromRS(results));
					count++;
				}
				totalCount++;
			}
			SMDaemonPerformanceMonitor.recordDbQueueCount(
					"DownloadsActivationQueue", totalCount);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (downloadsList.size() > 0) {
			logger.info("Retrieving records from RBT_DOWNLOADS successful");
			return downloadsList.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("No records in RBT_DOWNLOADS");
			return null;
		}
	}

	public static SubscriberDownloads[] smGetBaseActivationPendingDownloads(
			Connection conn, String subscriberID) {
		logger.info("RBT::inside smGetBaseActivationPendingDownloads");
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		ArrayList<SubscriberDownloads> downloadList = new ArrayList<SubscriberDownloads>();
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = '" + subscriberID + "' AND " + DOWNLOAD_STATUS_COL
				+ " = '" + STATE_DOWNLOAD_BASE_ACT_PENDING + "' ORDER BY "
				+ SET_TIME_COL;

		logger.info("RBT::query " + query);
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				downloadList.add(getSubscriberDownloadFromRS(results));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (downloadList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_DOWNLOADS successful");
			return ((SubscriberDownloads[]) downloadList
					.toArray(new SubscriberDownloads[0]));
		} else {
			logger.info("RBT::no records in RBT_DOWNLOADS");
			return null;
		}
	}

	public static SubscriberDownloads[] smGetDownloadsToBeDeactivated(
			Connection conn, int fetchSize) {
		logger.info("RBT::inside smGetDownloadsToBeDeactivated");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberId = null;
		String promoId = null;
		String downloadStatus = null;
		Date setTime = null;
		Date startTime = null;
		Date endTime = null;
		int categoryId = -1;
		int categoryType = -1;

		SubscriberDownloadsImpl download = null;
		ArrayList selectionsList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + DOWNLOAD_STATUS_COL
				+ " = '" + STATE_DOWNLOAD_TO_BE_DEACTIVATED + "'";

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND ROWNUM <="+fetchSize;
		else
			query += " LIMIT "+fetchSize;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			int count = 0;
			int totalCount = 0;
			while (results.next()) {
				if (count < fetchSize) {
					selectionsList.add(getSubscriberDownloadFromRS(results));
					count++;
				}
				totalCount++;
			}
			SMDaemonPerformanceMonitor.recordDbQueueCount(
					"DownloadsDeactivationQueue", totalCount);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (selectionsList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_DOWNLOADS successful");
			return ((SubscriberDownloads[]) selectionsList
					.toArray(new SubscriberDownloads[0]));
		} else {
			logger.info("RBT::no records in RBT_DOWNLOADS");
			return null;
		}
	}

	public static boolean deleteFromToBeDeletedSelections(Connection conn,
			SubscriberDownloads selectiontobedeleted) {
		logger.info("RBT::inside deleteFromToBeDeletedSelections");

		String query = null;
		Statement stmt = null;
		int results = -1;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = '" + selectiontobedeleted.subscriberId() + "' AND "
				+ PROMO_ID_COL + " = '" + selectiontobedeleted.promoId();

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return (results == 0);
	}

	public static SubscriberDownloads getSubscriberBookMark(Connection conn,
			String subscriberID, String promoID) {
		String method = "getSubscriberBookMark";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
				+ " = " + sqlString(promoID) + " AND " + DOWNLOAD_STATUS_COL
				+ " = 'b'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			if (rs != null && rs.next())
				return (getSubscriberDownloadFromRS(rs));
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		logger.info("RBT::No records in " + TABLE_NAME);
		return null;
	}

	public static SubscriberDownloads[] getSubscriberDownloadsByPromoID(
			Connection conn, String subscriberID, String promoID) {
		String method = "getSubscriberDownloadsByPromoID";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
				+ " = " + sqlString(promoID);

		logger.info("RBT::query " + query);

		ArrayList<SubscriberDownloads> downloadList = new ArrayList<SubscriberDownloads>();
		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			while (rs != null && rs.next()) {
				downloadList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		logger.info("RBT::No records in " + TABLE_NAME);
		return downloadList.toArray(new SubscriberDownloads[0]);
	}

	// JIRA-RBT-6194:Search based on songs in Query Gallery API
	public static SubscriberDownloads getSubscriberDownloadByPromoID(
			Connection conn, String subscriberID, String promoID) {
		String method = "getSubscriberDownloadsByPromoID";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads download = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ PROMO_ID_COL + " = " + sqlString(promoID) + " AND "
				+ DOWNLOAD_STATUS_COL + " NOT IN ('b')";

		logger.info("RBT::query = " + query);
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next())
				download = getSubscriberDownloadFromRS(rs);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return download;
	}

	public static SubscriberDownloads[] getSubscriberBookMarks(Connection conn,
			String subscriberID) {
		String method = "getSubscriberBookMarks";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList downloadList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " = 'b' ORDER BY " + SET_TIME_COL;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			while (rs != null && rs.next()) {
				downloadList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (downloadList.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME
					+ " successful");
			return (SubscriberDownloads[]) downloadList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::No records in " + TABLE_NAME);
			return null;
		}
	}

	public static boolean addSubscriberBookMark(Connection conn,
			String subscriberID, String promoID, int categoryID,
			int categoryType, String selectedBy) {
		logger.info("RBT::inside addSubscriberBookMark");

		String query = null;
		Statement stmt = null;
		int results = -1;

		Date curDate = new Date();

		query = "INSERT INTO " + TABLE_NAME + "(";
		query += SUBSCRIBER_ID_COL + ", ";
		query += PROMO_ID_COL + ", ";
		query += DOWNLOAD_STATUS_COL + ", ";
		query += SET_TIME_COL + ", ";
		query += START_TIME_COL + ", ";
		query += END_TIME_COL + ", ";
		query += CATEGORY_ID_COL + ", ";
		query += CATEGORY_TYPE_COL + ", ";
		query += SELECTED_BY_COL + ") VALUES (";

		query += sqlString(subscriberID) + ", ";
		query += sqlString(promoID) + ", ";
		query += "'b', ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(curDate) + ", ";
		else
			query += mySQLDateTime(curDate) + ", ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(curDate) + ", ";
		else
			query += mySQLDateTime(curDate) + ", ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += "'2037-12-31 00:00:00.0', ";
		else
			query += "TIMESTAMP('2037-12-31 00:00:00'),";
		query += categoryID + ", ";
		query += categoryType + ", ";
		query += sqlString(selectedBy) + ")";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return (results > 0);
	}

	public static boolean updateBookMark(Connection conn, String subscriberID,
			String promoID, Integer categoryID, Integer categoryType,
			String selectedBy, String fromStatus, String toStatus) {
		String query = null;
		Statement stmt = null;
		int results = -1;

		Date curDate = new Date();
		String dateString = m_databaseType.equalsIgnoreCase(DB_SAPDB) ? sqlTime(curDate)
				: mySQLDateTime(curDate);

		query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL + " = "
				+ sqlString(toStatus) + ", " + SET_TIME_COL + " = "
				+ dateString + ", " + START_TIME_COL + " = " + dateString
				+ ", " + CATEGORY_ID_COL + " = " + categoryID + ", "
				+ CATEGORY_TYPE_COL + " = " + categoryType + ", "
				+ SELECTED_BY_COL + " = " + sqlString(selectedBy) + ", "
				+ EXTRA_INFO_COL + " = NULL" + ", " + DEACTIVATION_INFO_COL
				+ " = NULL" + ", " + INTERNAL_REF_ID_COL + " = NULL "
				+ " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + PROMO_ID_COL + " = "
				+ sqlString(promoID);

		logger.info("Executing query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
			logger.info("Updated records: " + results + ", for query " + query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (results > 0);
	}

	public static boolean updateActPendingDownloadToBookMark(Connection conn,
			String subscriberID, String promoID) {
		String query = null;
		Statement stmt = null;
		int results = -1;

		Date curDate = new Date();
		String dateString = m_databaseType.equalsIgnoreCase(DB_SAPDB) ? sqlTime(curDate)
				: mySQLDateTime(curDate);

		query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ "= 'b'" + ", " + SET_TIME_COL + " = " + dateString + ", "
				+ START_TIME_COL + " = " + dateString + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ PROMO_ID_COL + " = " + sqlString(promoID) + " AND "
				+ DOWNLOAD_STATUS_COL + " IN ('" + STATE_DOWNLOAD_ACT_ERROR
				+ "','" + STATE_DOWNLOAD_ACTIVATION_PENDING + "','"
				+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "','" + STATE_DOWNLOAD_GRACE
				+ "')";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (results > 0);
	}

	public static boolean removeAllSubscriberBookMarks(Connection conn,
			String subscriberID) {
		logger.info("RBT::inside removeBookMark");

		String query = null;
		Statement stmt = null;
		int results = -1;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " = 'b'";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return (results > 0);
	}

	public static boolean removeSubscriberBookMark(Connection conn,
			String subscriberID, String promoID) {
		logger.info("RBT::inside removeBookMark");

		String query = null;
		Statement stmt = null;
		int results = -1;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND " + PROMO_ID_COL
				+ " = '" + promoID + "' AND " + DOWNLOAD_STATUS_COL + " = 'b'";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return (results > 0);
	}

	static ArrayList getUpdateToToBeDeletedDownloads(Connection conn,
			int fetchSize) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ DOWNLOAD_STATUS_COL + " = 'y' AND " + END_TIME_COL
				+ " <= SYSDATE";

		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ DOWNLOAD_STATUS_COL + " = 'y' AND " + END_TIME_COL
					+ " <= SYSDATE()";

		logger.info("RBT:: query " + query);

		Statement stmt = null;
		ResultSet rs = null;

		ArrayList list = new ArrayList();

		try {
			stmt = conn.createStatement();
			stmt.setMaxRows(fetchSize);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				list.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
		}

		if (list.size() > 0) {
			logger.info("RBT::retreiving records from " + TABLE_NAME
					+ " successful");
			return list;
		} else {
			logger.info("RBT::no records to retreive from " + TABLE_NAME);
			return null;
		}
	}

	public static boolean deleteDeactivatedDownloads(Connection conn,
			String subscriberId) {

		String query = "DELETE FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId) + " AND "
				+ DOWNLOAD_STATUS_COL + " = 'x'";

		logger.info("RBT::query - " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		Statement stmt = null;
		int n = -1;

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
		}
		return (n > 0);
	}

	public static int deleteDeactivatedDownloads(Connection conn, float duration) {

		int count = 0;
		String query = "DELETE FROM " + TABLE_NAME + " WHERE "
				+ DOWNLOAD_STATUS_COL + " = 'x' AND " + END_TIME_COL
				+ " < (now() - " + duration + ")";

		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE  FROM " + TABLE_NAME + " WHERE "
					+ DOWNLOAD_STATUS_COL + " = 'x' AND " + END_TIME_COL
					+ " <= TIMESTAMPADD(DAY,-" + duration
					+ ",SYSDATE()) LIMIT 1000";

		logger.info("RBT::query - " + query);

		Statement stmt = null;
		int n = -1;
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();

			if (!m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
				int rowCount = 1;
				int runCycle = 0;
				logger.info("before while loop. ");
				while (rowCount > 0) {
					runCycle++;
					logger.info("runCycle is :" + runCycle);
					logger.info("RBT::going to run delete query again as rowCount is greater than 0. rowCount = "
							+ rowCount);
					rowCount = stmt.executeUpdate(query);
					count += rowCount;
					logger.info("After running query. No. of rows deleted is :"
							+ rowCount);
				}
				logger.info("After while loop. As rowCount is :" + rowCount
						+ " and total runCycle is " + runCycle);
			} else {
				stmt.executeUpdate(query);
				n = count = stmt.getUpdateCount();
			}
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {

			}
		}
		return count;
	}

	/**
	 * Suspension is allowed for Download also. If download is in either
	 * ACTIVE/ACT_ERROR state suspending the download
	 * 
	 */
	public static boolean suspendDownload(Connection conn, String subscriberID,
			String refID, String extraInfo) {
		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " ='" + String.valueOf(STATE_DOWNLOAD_SUSPENSION) + "',"
				+ EXTRA_INFO_COL + " = " + sqlString(extraInfo) + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
				+ DOWNLOAD_STATUS_COL + " IN ('"
				+ String.valueOf(STATE_DOWNLOAD_ACTIVATED) + "','"
				+ String.valueOf(STATE_DOWNLOAD_ACT_ERROR) + "')";

		logger.info("RBT::query - " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		int n = -1;
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
		}
		return (n > 0);
	}

	/**
	 * 
	 */
	public static boolean resumeDownload(Connection conn, String subscriberID,
			String refID, String extraInfo) {
		String query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL
				+ " ='" + String.valueOf(STATE_DOWNLOAD_ACTIVATED) + "',"
				+ EXTRA_INFO_COL + " = " + sqlString(extraInfo) + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND "
				+ DOWNLOAD_STATUS_COL + " IN ('"
				+ String.valueOf(STATE_DOWNLOAD_SUSPENSION) + "')";

		logger.info("RBT::query - " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		int n = -1;
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
		}
		return (n > 0);
	}

	static SubscriberDownloads[] getNonFreeDownloads(Connection conn,
			String subscriberID, String chargeClass) {
		logger.info("RBT::inside getNonFreeSelections");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberDownloads subscrDownloads = null;
		List subscriberDownloadList = new ArrayList();

		String classTypeQuery = "";
		String[] tokens = chargeClass.split(",");
		for (String eachToken : tokens) {
			if (classTypeQuery != "")
				classTypeQuery += " AND ";

			classTypeQuery += CLASS_TYPE_COL + " NOT LIKE '" + eachToken.trim()
					+ "%'";
		}

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "'" + " AND "
					+ SET_TIME_COL + " <= SYSDATE AND " + END_TIME_COL
					+ " > SYSDATE AND (" + classTypeQuery + ")";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "'" + " AND "
					+ SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL
					+ " > SYSDATE() AND (" + classTypeQuery + ")";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				subscrDownloads = getSubscriberDownloadFromRS(results);
				subscriberDownloadList.add(subscrDownloads);
			}
		} catch (SQLException se) {
			logger.error("", se);
			return null;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (subscriberDownloadList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_SELECTIONS successful");
			return (SubscriberDownloads[]) subscriberDownloadList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_SELECTIONS");
			return null;
		}
	}

	public static int removeOldBookMarks(Connection conn, float configuredDays) {
		logger.info("RBT::inside removeBookMark");

		String query = null;
		Statement stmt = null;
		int results = -1;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + DOWNLOAD_STATUS_COL
				+ " = 'b' AND " + SET_TIME_COL + " <= (now()-" + configuredDays
				+ ")";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE "
					+ DOWNLOAD_STATUS_COL + " = 'b' AND " + SET_TIME_COL
					+ " <= TIMESTAMPADD(DAY, -" + configuredDays
					+ ", SYSDATE())";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return results;
	}

	/**
	 * Updates the CHARGE_CLASS of DOWNLOAD to DEFAULT & download status to
	 * TO_BE_ACTIVATED on Pack Activation Failure. Currently updating the
	 * CHARGE_CLASS to DEFAULT only assuming that default charge class in most
	 * cases will be DEFAULT
	 * 
	 * @param conn
	 * @param subscriberID
	 * @return
	 * 
	 */
	public static boolean smUpdateSongsToDefaultOnPackActivationFailure(
			Connection conn, String subscriberID) {
		Statement stmt = null;
		int n = -1;

		String query = null;
		query = "UPDATE " + TABLE_NAME + " SET " + CLASS_TYPE_COL
				+ " = 'DEFAULT', " + DOWNLOAD_STATUS_COL + " = '"
				+ STATE_DOWNLOAD_TO_BE_ACTIVATED + "'" + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " = '"
				+ (STATE_DOWNLOAD_BASE_ACT_PENDING) + "'";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}

	static SubscriberDownloads getDownloadToBeDeactivated(Connection conn,
			String subscriberID, String promoID, int categoryId,
			int categoryType, boolean doFilterTstatus) {
		String method = "getDownloadToBeDeactivated";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		String enddateStr = MYSQL_SYSDATE;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			enddateStr = SAPDB_SYSDATE;

		SubscriberDownloads subDownload = null;
		String query = "SELECT * FROM " + TABLE_NAME;

		if (Utility.isShuffleCategory(categoryType)
				|| categoryType == iRBTConstant.DYNAMIC_SHUFFLE) {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + CATEGORY_ID_COL
					+ " = " + categoryId + " AND " + CATEGORY_TYPE_COL + " = "
					+ categoryType + " AND " + END_TIME_COL + " > "
					+ enddateStr ; 
		} else {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + PROMO_ID_COL + " = "
					+ sqlString(promoID) + " AND " + END_TIME_COL + " > "
					+ enddateStr ;
		}
		
		if(doFilterTstatus) {
			query += " AND " + DOWNLOAD_STATUS_COL + " NOT IN('b','t')";
		}else {
			query += " AND " + DOWNLOAD_STATUS_COL + " != 'b'";
		}

		logger.info("RBT::query " + query);
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next())
				subDownload = getSubscriberDownloadFromRS(rs);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (subDownload);
	}

	public static boolean updateRetryCountAndTime(Connection conn,
			String subscriberID, String refID, String retryCount, Date retryTime) {
		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			query = "UPDATE " + TABLE_NAME + " SET " + RETRY_COUNT_COL + " = "
					+ sqlString(retryCount) + " , " + NEXT_RETRY_TIME_COL
					+ " = " + sqlTime(retryTime) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'"
					+ " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID);
		} else {
			query = "UPDATE " + TABLE_NAME + " SET " + RETRY_COUNT_COL + " = "
					+ sqlString(retryCount) + " , " + NEXT_RETRY_TIME_COL
					+ " = " + mySQLDateTime(retryTime) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'"
					+ " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID);
		}

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int count = executeUpdateQuery(conn, query);
		return (count > 0);
	}

	private static int executeUpdateQuery(Connection conn, String query) {
		int updateCount = 0;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			updateCount = stmt.getUpdateCount();
		} catch (SQLException se) {
			logger.error("", se);
			return updateCount;
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return updateCount;
	}
	
	//RBT-12419
	static boolean removeSubscriberDownloadBySubIdAndWavFileAndCatId(Connection conn,
			String subscriberID, String promoID, int categoryId) {

		Statement stmt = null;
		int n = -1;

		String query = null;

		query = "DELETE FROM " + TABLE_NAME;

		if (promoID==null) {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + CATEGORY_ID_COL
					+ " = " + categoryId;
		} else {
			query += " WHERE " + SUBSCRIBER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + PROMO_ID_COL + " = "
					+ sqlString(promoID);
		}

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);
	}
	
	//RBT-13544 added for MiPlaylist feature
	static SubscriberDownloads[] getSubscriberDownloadsByDownloadStatus(Connection conn,
			String subscriberID, String downloadStatus) {
		String method = "getSubscriberDownloadsByDownloadStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		List<SubscriberDownloads> subscriberDownloadsList = new ArrayList<SubscriberDownloads>();

		String query =  "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
					+ " AND " + CATEGORY_TYPE_COL +" IN(" + sqlString(String.valueOf(DTMF_CATEGORY)) +","+ sqlString(String.valueOf(SONGS))+ ")"
					+ " ORDER BY " + SET_TIME_COL+" DESC ";
		
		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				subscriberDownloadsList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (subscriberDownloadsList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
			return (SubscriberDownloads[]) subscriberDownloadsList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}
	
	//Added for fetching downloads list for TS-6705
	static SubscriberDownloads getSubscriberDownloadsByDownloadStatus(Connection conn,
			String subscriberID, String wavFileName, String downloadStatus) {
		String method = "getSubscriberDownloadsByDownloadStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads subscriberDownload = null;

		String query =  "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + DOWNLOAD_PROMO_ID + " = " + sqlString(wavFileName)
					+ " AND " + DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
					+ " AND " + CATEGORY_TYPE_COL +" IN(" + sqlString(String.valueOf(DTMF_CATEGORY)) +","+ sqlString(String.valueOf(SONGS))+ ")"
					+ " ORDER BY " + SET_TIME_COL+" DESC ";
		
		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				subscriberDownload=getSubscriberDownloadFromRS(rs);
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (subscriberDownload!=null) {
			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
			return subscriberDownload;
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}
	//End of TS-6705
	
	//Added for fetching downloads list for TS-6705
		static SubscriberDownloads getSubscriberDownloadsByDownloadStatus(Connection conn,
				String subscriberID, int categoryID,int categoryType, String downloadStatus) {
			String method = "getSubscriberDownloadsByDownloadStatus";
			logger.info("RBT:: inside " + method);

			Statement stmt = null;
			ResultSet rs = null;
			SubscriberDownloads subscriberDownload = null;

			String query =  "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
						+ " AND " + DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
						+ " AND " + CATEGORY_ID_COL +" = " + categoryID
						+ " AND " + CATEGORY_TYPE_COL +" = " + categoryType
						+ " ORDER BY " + SET_TIME_COL+" DESC ";
			
			logger.info("RBT::query = " + query);
			RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
			
			try {
				logger.info("RBT::inside try block");

				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					subscriberDownload=getSubscriberDownloadFromRS(rs);
				}
			} catch (SQLException se) {
				logger.error("", se);
			} finally {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}

			if (subscriberDownload!=null) {
				logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
				return subscriberDownload;
			} else {
				logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
				return null;
			}
		}
	//End of TS-6705
		
	static boolean updateDownloadStatusByDownloadStatus(Connection conn, String subscriberID,
			String promoID, String deselectedBy, String downloadStatus, String oldDownloadStatus, int catType) {
		String method = "updateDownloadStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME 
				+ " SET " + DOWNLOAD_STATUS_COL	+ " = " + sqlString(downloadStatus) 
				+ " , " + END_TIME_COL + " = SYSDATE()" 
				+ " , "+ DEACTIVATION_INFO_COL + " = " + sqlString(deselectedBy)
				+ " WHERE " + SUBSCRIBER_ID_COL	+ " = " + sqlString(subscriberID) 
				+ " AND " + PROMO_ID_COL + " = " + sqlString(promoID)
				+ " AND " + DOWNLOAD_STATUS_COL	+ " = " + sqlString(oldDownloadStatus) 
			    + " AND " + CATEGORY_TYPE_COL + " = " + catType ;

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		 }
		return (n == 1);
	}
	
	static SubscriberDownloads getActiveSubscriberDownloadByStatus(Connection conn,
			String subscriberID, String promoID, String downloadStatus, int categoryId,
			int categoryType) {
		String method = "getActiveSubscriberDownloadByStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads download = null;
		String	query = null;
		if(Utility.isShuffleCategory(categoryType)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + CATEGORY_ID_COL + " = " + categoryId
					+ " AND " + DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
					+ " AND " + END_TIME_COL + " > SYSDATE()";
		}else {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + PROMO_ID_COL + " = " + sqlString(promoID)
					+ " AND " + CATEGORY_TYPE_COL + " = " + categoryType
					+ " AND " + DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
					+ " AND " + END_TIME_COL + " > SYSDATE()";
		}

		logger.info("RBT::query = " + query);

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next())
				download = getSubscriberDownloadFromRS(rs);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return download;
	}
	
	
	public static boolean deleteSubscriberDownloadsByStatusCatIDCatType(Connection conn,
			String subscriberID, String wavFile, int categoryID,
			int categoryType,String status) {
		String method = "deleteSubscriberDownloadsByStatusCatIDCatType";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		int n = -1;
		
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + sqlString(subscriberID) 
					+ " AND "+ CATEGORY_ID_COL + " = " + categoryID
					+ " AND "+ PROMO_ID_COL + " = " + sqlString(wavFile)
					+ " AND "+ CATEGORY_TYPE_COL + " = " + categoryType
					+ " AND "+ DOWNLOAD_STATUS_COL + " = " + sqlString(status);
		

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		
		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n >= 0);
	}
	
	public static boolean removeDownloadsWithTStatus(Connection conn,
			String subscriberID) {
		logger.info("RBT::inside removeDownloadsWithTStatus");

		String query = null;
		Statement stmt = null;
		int results = -1;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " = 't'";

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return (results > 0);
	}
	
	//RBT-2.0
	public static int getActiveSubscriberDownloadsCount(String subscriberId,Connection conn,Map<String,String> whereClauseMap) {
		
		logger.info("RBT:: Inside getActiveSubscriberDownloadsCount");
		Statement stmt = null;
		ResultSet rs = null;
		int rowCount = 0;
		StringBuffer query = new StringBuffer();
		query.append("SELECT COUNT(*) FROM ")
				.append(TABLE_NAME)
				.append(" ")
				.append("WHERE ")
				.append(SUBSCRIBER_ID_COL)
				.append(" = ")
				.append("'")
				.append(subscriberId)
				.append("'")
				.append(" AND ")
				.append(DOWNLOAD_STATUS_COL)
				.append(" IN ('w','n','p','y')");		
		 
		if (whereClauseMap.containsKey("CATEGORY_ID")) {
			query.append(" AND ")
					.append(CATEGORY_ID_COL)
					.append(" = ")
					.append(whereClauseMap.get("CATEGORY_ID"));
		}
		if (whereClauseMap.containsKey("SUBSCRIBER_WAV_FILE")) {
			query.append(" AND ")
					.append(PROMO_ID_COL)
					.append(" = ")
					.append("'")
					.append(whereClauseMap.get("SUBSCRIBER_WAV_FILE"))
					.append("'");
		}
		
		query.append(" AND ")
				.append(END_TIME_COL)
				.append(" > ");
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			query.append(" SYSDATE ");
		}
		else {
			query.append(" SYSDATE() ");
		}

		logger.info("Executing query:: " + query.toString());
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			if (rs.next())
				rowCount = rs.getInt(1);
		} catch(SQLException e)
		{
			logger.error("Exception Occured", e);
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}

		
		return rowCount;
	}
	
	static SubscriberDownloads[] getSubscriberDownloadByClassType(Connection conn,
 String subscriberID, String classType) {
		String method = "getSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList downloadList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " = "+sqlString("w") +" AND " + CLASS_TYPE_COL + " = "+ sqlString(classType) ;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			while (rs != null && rs.next()) {
				downloadList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (downloadList.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME
					+ " successful " + " size: " + downloadList.size());
			return (SubscriberDownloads[]) downloadList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::No records in " + TABLE_NAME);
			return null;
		}
	}

	static boolean smUpdateXbiDownloadtoBeDeActivated(Connection conn, String subscriberID, String fStatus, String tStatus, String mappedChargeClass) {
		String query = null;
		Statement stmt = null;

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL + " = "
					+ sqlString(tStatus) + " , " + START_TIME_COL + " = "
							+ "SYSDATE" + " , " + END_TIME_COL + " = " + "SYSDATE" +  " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + DOWNLOAD_STATUS_COL
					+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
					+ " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE"
					+ " AND " + CLASS_TYPE_COL + " = " + sqlString(mappedChargeClass) ;
		else
			query = "UPDATE " + TABLE_NAME + " SET " + DOWNLOAD_STATUS_COL + " = "
					+ sqlString(tStatus)+ " , " + START_TIME_COL + " = "
					+ "SYSDATE()" + " , " + END_TIME_COL + " = " + "SYSDATE()" + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + DOWNLOAD_STATUS_COL
					+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
					+ " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()"
					+ " AND " + CLASS_TYPE_COL + " = " + sqlString(mappedChargeClass) ;

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return true;
	}

	//RBT-16230	sel_status 't' is also counted in number of song downloads
	public static SubscriberDownloads[] getNonDeactiveAndTrackSubscriberDownloads(
			Connection conn, String subscriberID) {
		String method = "getNonDeactiveSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;

		List<SubscriberDownloads> subscriberDownloadsList = new ArrayList<SubscriberDownloads>();

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ DOWNLOAD_STATUS_COL + " NOT IN ('b','d','s','x','t')";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ DOWNLOAD_STATUS_COL + " NOT IN ('b','d','s','x','t')";

		logger.info("RBT::query " + query);
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				subscriberDownloadsList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			closeStatementAndRS(stmt, rs);
		}

		if (subscriberDownloadsList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
			return (SubscriberDownloads[]) subscriberDownloadsList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}
	public static SubscriberDownloads[] getSubscriberDownloadsWithoutTrack(Connection conn,
			String subscriberID) {
		String method = "getSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList downloadList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " NOT IN('b','t') ORDER BY " + SET_TIME_COL;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			while (rs != null && rs.next()) {
				downloadList.add(getSubscriberDownloadFromRS(rs));
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (downloadList.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME
					+ " successful " + " size: " + downloadList.size());
			return (SubscriberDownloads[]) downloadList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::No records in " + TABLE_NAME);
			return null;
		}
	}
	public static SubscriberDownloads[] getActiveSubscriberDownloadsWithoutTrack(
			Connection conn, String subscriberID) {
		String method = "getActiveSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		ResultSet rs = null;

		List subscriberDownloadsList = new ArrayList();

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ END_TIME_COL + " > SYSDATE AND " + DOWNLOAD_STATUS_COL
					+ " NOT IN('b','t') ORDER BY SET_TIME";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ END_TIME_COL + " > SYSDATE() AND " + DOWNLOAD_STATUS_COL
					+ " NOT IN('b','t') ORDER BY SET_TIME";

		logger.info("RBT::query " + query);

		System.out.println(" the query is  " + query);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(" populating array list");
				subscriberDownloadsList.add(getSubscriberDownloadFromRS(rs));
				System.out.println("size of sdl "
						+ subscriberDownloadsList.size());
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		System.out.println(" did i get here " + subscriberDownloadsList.size());
		if (subscriberDownloadsList.size() > 0) {

			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
			return (SubscriberDownloads[]) subscriberDownloadsList
					.toArray(new SubscriberDownloads[0]);
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}

	// Added for 
	static SubscriberDownloads getActiveSubscriberDownloadByCatIdOrPromoId(Connection conn,
				String subscriberID, String id, boolean isCatId) {
			String method = "getActiveSubscriberDownloadBtCatIdOrPromoId";
			logger.info("RBT:: inside " + method);

			Statement stmt = null;
			ResultSet rs = null;
			SubscriberDownloads download = null;

			String byColumnName = PROMO_ID_COL;
			if(isCatId){
				byColumnName = CATEGORY_ID_COL;
			}
			
			String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
						+ " AND " + byColumnName + " = " + sqlString(id)
						+ " AND " + END_TIME_COL + " > SYSDATE()";

			logger.info("RBT::query = " + query);

			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);
				if (rs.next())
					download = getSubscriberDownloadFromRS(rs);
			} catch (SQLException e) {
				logger.error("", e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
			return download;
		}
	//This method used to delete data which having CATEGORY_TYPE and DOWNLOAD_STATUS=t 
	public static boolean deleteDownloadwithTstatusAndCategoryType(Connection conn,
			String subscriberID, String categoryTypes) {
		String method = "deleteDownloadwithTstatusAndCategoryType";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		int n = -1;
		String inClause="";
		if(categoryTypes!=null && !categoryTypes.isEmpty()){
			inClause = " AND " + CATEGORY_TYPE_COL+" IN(" +categoryTypes+ ")";
		}
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID)
				+ inClause
				+ " AND " + DOWNLOAD_STATUS_COL +" = 't'";
				

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return n >= 0?true:false;
	}
	
	//Added for fetching downloads list
			static SubscriberDownloads[] getSubscriberDownloadsByDownloadStatusAndCategory(Connection conn,
					String subscriberID, int categoryID,int categoryType, String downloadStatus) {
				String method = "getSubscriberDownloadsByDownloadStatus";
				logger.info("RBT:: inside " + method);

				Statement stmt = null;
				ResultSet rs = null;
				ArrayList downloadList = new ArrayList();

				String query =  "SELECT * FROM " + TABLE_NAME + " WHERE "
							+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
							+ " AND " + DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)
							+ " AND " + CATEGORY_ID_COL +" = " + categoryID
							+ " AND " + CATEGORY_TYPE_COL +" = " + categoryType
							+ " ORDER BY " + SET_TIME_COL+" DESC ";
				
				logger.info("RBT::query = " + query);
				RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
				
				try {
					logger.info("RBT::inside try block");

					stmt = conn.createStatement();
					rs = stmt.executeQuery(query);
					while (rs.next() && rs != null)
						downloadList.add(getSubscriberDownloadFromRS(rs));
					
				} catch (SQLException se) {
					logger.error("", se);
				} finally {
					try {
						stmt.close();
					} catch (Exception e) {
						logger.error("", e);
					}
				}

				if (downloadList.size() > 0) {
					logger.info("RBT::Retreiving records from " + TABLE_NAME
							+ " successful");
					return (SubscriberDownloads[]) downloadList
							.toArray(new SubscriberDownloads[0]);
					
				}	else {
					logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
					return null;
				}
			}
			
			
			//Added for fetching downloads list
	static SubscriberDownloads[] getSubscriberActiveDownloadsByDownloadStatusAndCategory(Connection conn, String subscriberID,
			int categoryID, int categoryType) {
		String method = "getSubscriberDownloadsByDownloadStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		ArrayList downloadList = new ArrayList();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ CATEGORY_ID_COL + " = " + categoryID + " AND " + CATEGORY_TYPE_COL + " = " + categoryType + " AND "
				+ DOWNLOAD_STATUS_COL + " IN ('" + (STATE_DOWNLOAD_ACTIVATED) + "','" + (STATE_DOWNLOAD_TO_BE_ACTIVATED)+ "','" + "p" + "','" + "w" + "')"
				+ " ORDER BY " + SET_TIME_COL + " DESC ";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next() && rs != null)
				downloadList.add(getSubscriberDownloadFromRS(rs));

		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (downloadList.size() > 0) {
			logger.info("RBT::Retreiving records from " + TABLE_NAME + " successful");
			return (SubscriberDownloads[]) downloadList.toArray(new SubscriberDownloads[0]);

		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}
	
	
	public static String deleteDownloadwithTstatusAndCategoryId(Connection conn,
			String subscriberID, String promoID , String categoryId) {
		String method = "deleteSubscriberDownloads";
		logger.info("RBT::inside " + method);

		String query = null;
		Statement stmt = null;
		int n = -1;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID)
				+ " AND " + PROMO_ID_COL +" = " + sqlString(promoID)
				+ " AND " + CATEGORY_ID_COL +" = " + sqlString(categoryId)
				+ " AND " + CATEGORY_TYPE_COL +" IN(" + sqlString(String.valueOf(DTMF_CATEGORY)) +","+ sqlString(String.valueOf(SONGS))+ ")"
				+ " AND " + DOWNLOAD_STATUS_COL +" = 't'";
				

		logger.info("RBT::query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return n >= 0?"true":"false";
	}
	
	
	
	// Added for fetching downloads list
	static SubscriberDownloads getSubscriberActiveDownloadsByDownloadStatusAndCategoryAndPromoId(Connection conn,
			String subscriberID, int categoryID, int categoryType, String downloadStatus , String promoId) {
		String method = "getSubscriberDownloadsByDownloadStatus";
		logger.info("RBT:: inside " + method);

		Statement stmt = null;
		ResultSet rs = null;
		SubscriberDownloads subscriberDownload = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ DOWNLOAD_STATUS_COL + " = " + sqlString(downloadStatus)+ " AND "
						+ PROMO_ID_COL + " = " + sqlString(promoId) + " AND " + CATEGORY_ID_COL + " = " + categoryID
				+ " AND " + CATEGORY_TYPE_COL + " = " + categoryType + " ORDER BY " + SET_TIME_COL + " DESC ";

		logger.info("RBT::query = " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		try {
			logger.info("RBT::inside try block");

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				subscriberDownload = getSubscriberDownloadFromRS(rs);
			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (subscriberDownload != null) {
			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_DOWNLOADS successful");
			return subscriberDownload;
		} else {
			logger.info("RBT::no records in RBT_SUBSCRIBER_DOWNLOADS");
			return null;
		}
	}
}
