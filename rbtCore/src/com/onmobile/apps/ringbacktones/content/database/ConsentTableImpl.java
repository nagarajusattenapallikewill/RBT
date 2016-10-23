package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class ConsentTableImpl extends RBTPrimitive implements iRBTConstant {
	private static Logger logger = Logger.getLogger(ConsentTableImpl.class);

	private static final String TABLE_NAME = "RBT_CONSENT";
	private static final String KEY_ID_COL = "TRANS_ID";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String START_DATE_COL = "START_TIME";
	private static final String END_DATE_COL = "END_TIME";
	private static final String PREPAID_YES_COL = "PREPAID_YES";
	private static final String RBT_TYPE_COL = "RBT_TYPE";
	private static final String LANGUAGE_COL = "LANGUAGE";
	private static final String CIRCLE_ID_COL = "CIRCLE_ID";
	private static final String CALLER_ID_COL = "CALLER_ID";
	private static final String CATEGORY_ID_COL = "CATEGORY_ID";
	private static final String CLASS_TYPE_COL = "CLASS_TYPE";
	private static final String SUBSCRIPTION_CLASS_COL = "SUBSCRIPTION_CLASS";
	private static final String MODE_COL = "MODE";
	private static final String STATUS_COL = "STATUS";
	private static final String SEL_INTERVAL_COL = "SEL_INTERVAL";
	private static final String FROM_TIME_COL = "FROM_TIME";
	private static final String TO_TIME_COL = "TO_TIME";
	private static final String COS_ID_COL = "COS_ID";
	private static final String CLIP_ID_COL = "CLIP_ID";
	private static final String SELECTION_INFO_COL = "SELECTION_INFO";
	private static final String EXTRA_INFO_COL = "EXTRA_INFO";
	private static final String REQUEST_TIME_COL = "REQUEST_TIME";
	private static final String REQUEST_TYPE_COL = "REQUEST_TYPE";
	private static final String CONSENT_STATUS_COL = "CONSENT_STATUS";
	private static final String SELECTION_TYPE_COL = "SEL_TYPE";
	private static final String FEED_TYPE_COL = "FEED_TYPE";
	private static final String PURCHASE_TYPE_COL = "PURCHASE_TYPE";
	private static final String USE_UI_CHARGE_CLASS_COL = "USE_UI_CHARGE_CLASS";
	private static final String CATEGORY_TYPE_COL = "CATEGORY_TYPE";
	private static final String PROFILE_HRS_COL = "PROFILE_HRS";
	private static final String WAV_FILE_NAME_COL = "WAV_FILE_NAME";
	private static final String LOOP_STATUS_COL = "IN_LOOP";
	private static final String PACK_COS_ID_COL = "PACK_COS_ID";
	private static final String INLINE_DAEMON_FLAG_COL = "INLINE_FLAG";
	private static final String AGENT_ID = "AGENT_ID";
	private static final Map<String, String> masterSubModesMap = initializeMasterSubModesMapping();
	private static String m_databaseType = getDBSelectionString();
	private static boolean m_rrbtOn = false;
	//RBT-14652
	private static final String RETRY_TIME_COL = "RETRY_TIME";

	//Jira RBT - 13221 - For Upgrade request need update the reqeustType as Upgrade. 
	static boolean insertSubscriptionRecord(Connection conn,
			String subscriberID, String activate, Date startDate, Date endDate, 
			String activationInfo, String prepaid, String subscriptionClass,
			String cosID, int rbtType, boolean isDirectAct, String extraInfo,
			String circleID, String refID, int consentStatus , boolean isUpgrade, Date requestTimeAsDate, String agentId) {
		String date = null;
		String requestTime = null;
		String enDate = null;
		String requestType = "ACT";
		if (isUpgrade)
			requestType = "UPGRADE";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			date = "SYSDATE";
			if (requestTimeAsDate == null) {
				requestTime = "SYSDATE";
			} else {
				requestTime = sqlTime(requestTimeAsDate);
			}
			enDate = "TO_DATE('20371231','yyyyMMdd')";
			if (startDate != null)
				date = sqlTime(startDate);
			if (endDate != null)
				enDate = sqlTime(endDate);
		} else {
			date = "SYSDATE()";
			if (requestTimeAsDate == null) {
				requestTime = "SYSDATE()";
			} else {
				requestTime = mySqlTime(requestTimeAsDate);
			}
			enDate = "TIMESTAMP('2037-12-31')";
			if (startDate != null)
				date = mySQLDateTime(startDate);
			if (endDate != null)
				enDate = mySQLDateTime(endDate);
		}

		String query = "INSERT INTO " + TABLE_NAME + " ( " + KEY_ID_COL;
		query += ", " + SUBSCRIBER_ID_COL;
		query += ", " + MODE_COL;
		query += ", " + START_DATE_COL;
		query += ", " + END_DATE_COL;
		query += ", " + SELECTION_INFO;
		query += ", " + PREPAID_YES_COL;
		query += ", " + SUBSCRIPTION_CLASS_COL;
		// Third Party confirmation chages
		if (null != cosID) {
			query += ", " + COS_ID_COL;
		}
		query += ", " + RBT_TYPE_COL;
		query += ", " + CIRCLE_ID_COL;
		query += ", " + EXTRA_INFO_COL;
		query += ", " + REQUEST_TIME_COL;
		query += ", " + REQUEST_TYPE_COL;
		query += ", " + CONSENT_STATUS_COL;
		query += ", " + AGENT_ID;
		query += ")";

		query += " VALUES ( " + "'" + refID + "'";
		query += ", " + sqlString(subscriberID);
		query += ", " + sqlString(activate);
		query += ", " + date;
		query += ", " + enDate;
		query += ", " + sqlString(activationInfo);
		query += ", " + "'" + prepaid + "'";
		query += ", " + sqlString(subscriptionClass);
		// Third Party confirmation chages
		if (null != cosID) {
			query += ", " + Integer.parseInt(cosID);
		}
		query += ", " + rbtType;
		query += ", " + sqlString(circleID);
		query += ", " + sqlString(extraInfo);
		query += ", " + requestTime;
		query += ", " + sqlString(requestType);
		query += ", " + consentStatus;
		query += ", " + sqlString(agentId);
		query += ")";

		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger
					.info("Insertion into RBT_CONSENT table is SUCCESS for subscriber: "
							+ subscriberID);
			return true;
		}

		logger
				.info("Insertion into RBT_CONSENTtable is FAILED for subscriber: "
						+ subscriberID);
		return false;
	}
	
	static boolean insertSubscriptionRecord(Connection conn,
			String subscriberID, String activate, Date startDate, Date endDate,
			String activationInfo, String prepaid, String subscriptionClass,
			String cosID, int rbtType, boolean isDirectAct, String extraInfo,
			String circleID, String refID, int consentStatus,
			boolean isUpgrade, Date requestTimeAsDate, String agentId,
			String language) {
		String date = null;
		String requestTime = null;
		String enDate = null;
		String requestType = "ACT";
		if (isUpgrade)
			requestType = "UPGRADE";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			date = "SYSDATE";
			if (requestTimeAsDate == null) {
				requestTime = "SYSDATE";
			} else {
				requestTime = sqlTime(requestTimeAsDate);
			}
			enDate = "TO_DATE('20371231','yyyyMMdd')";
			if (startDate != null)
				date = sqlTime(startDate);
			if (endDate != null)
				enDate = sqlTime(endDate);
		} else {
			date = "SYSDATE()";
			if (requestTimeAsDate == null) {
				requestTime = "SYSDATE()";
			} else {
				requestTime = mySqlTime(requestTimeAsDate);
			}
			enDate = "TIMESTAMP('2037-12-31')";
			if (startDate != null)
				date = mySQLDateTime(startDate);
			if (endDate != null)
				enDate = mySQLDateTime(endDate);
		}

		String query = "INSERT INTO " + TABLE_NAME + " ( " + KEY_ID_COL;
		query += ", " + SUBSCRIBER_ID_COL;
		query += ", " + MODE_COL;
		query += ", " + START_DATE_COL;
		query += ", " + END_DATE_COL;
		query += ", " + SELECTION_INFO;
		query += ", " + PREPAID_YES_COL;
		query += ", " + SUBSCRIPTION_CLASS_COL;
		// Third Party confirmation chages
		if (null != cosID) {
			query += ", " + COS_ID_COL;
		}
		query += ", " + RBT_TYPE_COL;
		query += ", " + CIRCLE_ID_COL;
		query += ", " + EXTRA_INFO_COL;
		query += ", " + REQUEST_TIME_COL;
		query += ", " + REQUEST_TYPE_COL;
		query += ", " + CONSENT_STATUS_COL;
		query += ", " + AGENT_ID;
		query += ", " + LANGUAGE_COL;
		query += ")";

		query += " VALUES ( " + "'" + refID + "'";
		query += ", " + sqlString(subscriberID);
		query += ", " + sqlString(activate);
		query += ", " + date;
		query += ", " + enDate;
		query += ", " + sqlString(activationInfo);
		query += ", " + "'" + prepaid + "'";
		query += ", " + sqlString(subscriptionClass);
		// Third Party confirmation chages
		if (null != cosID) {
			query += ", " + Integer.parseInt(cosID);
		}
		query += ", " + rbtType;
		query += ", " + sqlString(circleID);
		query += ", " + sqlString(extraInfo);
		query += ", " + requestTime;
		query += ", " + sqlString(requestType);
		query += ", " + consentStatus;
		query += ", " + sqlString(agentId);
		query += ", " + sqlString(language);
		query += ")";

		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger.info("Insertion into RBT_CONSENT table is SUCCESS for subscriber: "
					+ subscriberID);
			return true;
		}

		logger.info("Insertion into RBT_CONSENTtable is FAILED for subscriber: "
				+ subscriberID);
		return false;
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

	public static String getRequestType(Connection conn, String transId) {
		String requestType = null;
		Statement stmt = null;
		String query = null;
		try {
			int id = Integer.parseInt(transId);
			query = "SELECT REQUEST_TYPE FROM RBT_CONSENT WHERE " + KEY_ID_COL
					+ " = " + id;
			logger.info("Executing the query: " + query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next())
				requestType = rs.getString("REQUEST_TYPE");

		} catch (Exception ex) {
			logger.info("Exception while getting Request Type......");
		}

		return requestType;
	}

	public static SelectionRequest getSelectionRecord(Connection conn,
			String clipID, String categoryID, String subscriberID, String mode,
			String transid,boolean isRMOClip,String requestType) {
		Statement stmt = null;
		String query = null;
		SelectionRequest selectionRequest = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + SUBSCRIBER_ID_COL
					+ " = '" + subscriberID + "'";

			if (transid != null) {
				query += " AND " + KEY_ID_COL + " = " + sqlString(transid);
			} else {
				if (mode != null) {
					if (masterSubModesMap != null
							&& masterSubModesMap.containsKey(mode)) {
						query += " AND " + MODE_COL + " IN ("
								+ masterSubModesMap.get(mode) + ")";
					} else {
						query += " AND " + MODE_COL + " = " + sqlString(mode);
					}
				}

				if (clipID != null && !isRMOClip) {
					query += " AND " + CLIP_ID_COL + " = "
							+ Integer.parseInt(clipID);
				}
               
				if(requestType!=null){
					query += " AND " + REQUEST_TYPE_COL + " = " + sqlString(requestType);
				}
				
				if(isRMOClip){
					query += " ORDER BY "+ REQUEST_TIME_COL +" DESC";
				}
				
			}

			stmt = conn.createStatement();
			logger.info("Query For Selection Matching Record :: " + query);
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				String callerID = rs.getString("CALLER_ID");
				int categoryId = rs.getInt("CATEGORY_ID");
				String subscriptionClass = rs.getString("SUBSCRIPTION_CLASS");
				String mode1 = rs.getString("MODE");
				Date endDate = rs.getTimestamp("END_TIME");
				int status = rs.getInt("STATUS");
				String classType = rs.getString("CLASS_TYPE");
				int cosId = rs.getInt("COS_ID");
				int clipId = rs.getInt("CLIP_ID");
				String selInterval = rs.getString("SEL_INTERVAL");
				int fromTime[] = getTime(rs.getInt("FROM_TIME"));
				int toTime[] = getTime(rs.getInt("TO_TIME"));
				String selInfo = rs.getString("SELECTION_INFO");
				int selType = rs.getInt("SEL_TYPE");
				Boolean inLoop = rs.getString("IN_LOOP") != null ? (rs
						.getString("IN_LOOP").equalsIgnoreCase("y") || rs
						.getString("IN_LOOP").equalsIgnoreCase("l")) : false;
				String purchageType = rs.getString("PURCHASE_TYPE");
				boolean isUseUIChargeClass = rs
						.getString("USE_UI_CHARGE_CLASS") != null ? rs
						.getString("USE_UI_CHARGE_CLASS").equalsIgnoreCase("y")
						: false;
				// int categoryType = rs.getInt("CATEGORY_TYPE");
				String profileHours = rs.getString("PROFILE_HRS");
				boolean isPrepaid = rs.getString("PREPAID_YES") != null ? rs
						.getString("PREPAID_YES").equalsIgnoreCase("y") : false;
				String feedType = rs.getString("FEED_TYPE");
				String wavFileName = rs.getString("WAV_FILE_NAME");
				int rbtType = rs.getInt("RBT_TYPE");
				String circleId = rs.getString("CIRCLE_ID");
				String language = rs.getString("LANGUAGE");
				String extraInfo = rs.getString("EXTRA_INFO");
				String cricketPack = feedType;
				String transID = rs.getString("TRANS_ID");
                
				selectionRequest = new SelectionRequest(subscriberID);
				if (!(rs.getInt("FROM_TIME") == 0)
						&& !(rs.getInt("TO_TIME") == 2359)) {
					selectionRequest.setFromTime(fromTime[0]);
					selectionRequest.setFromTimeMinutes(fromTime[1]);
					selectionRequest.setToTime(toTime[0]);
					selectionRequest.setToTimeMinutes(toTime[1]);
				}
				selectionRequest.setIsPrepaid(isPrepaid);
				selectionRequest.setProfileHours(profileHours);
				selectionRequest.setInLoop(inLoop);
				selectionRequest.setChargeClass(classType);
				selectionRequest.setSubscriptionClass(subscriptionClass);
				selectionRequest.setStatus(status);
				selectionRequest.setInterval(selInterval);
				selectionRequest.setCategoryID(categoryId + "");
				selectionRequest.setMode(mode1);
				selectionRequest.setModeInfo(selInfo);
				selectionRequest.setActivationMode(mode1);
				selectionRequest.setInfo(mode1);
				selectionRequest.setCallerID(callerID);
				selectionRequest.setUseUIChargeClass(isUseUIChargeClass);
				selectionRequest.setRbtType(rbtType);
				selectionRequest.setSelectionType(selType);
				if (cosId != 0 && cosId > 0){
				  selectionRequest.setCosID(cosId);
				}
				if(clipId != 0 && clipId > 0){
				  selectionRequest.setClipID(clipId + "");
				}
				selectionRequest.setCricketPack(cricketPack);
				selectionRequest.setIsPrepaid(isPrepaid);
				selectionRequest.setTransID(transID);
				selectionRequest.setRbtFile(wavFileName);
				if(extraInfo!=null && extraInfo.indexOf(Constants.param_allowPremiumContent)!=-1){
				    selectionRequest.setAllowPremiumContent(true);
				}
				if(extraInfo!=null && extraInfo.indexOf(iRBTConstant.UDS_OPTIN)!=-1
						&&(extraInfo.substring(extraInfo.indexOf(iRBTConstant.UDS_OPTIN)+11,extraInfo.indexOf(iRBTConstant.UDS_OPTIN)+16).equalsIgnoreCase("FALSE"))){
					selectionRequest.setUdsOn(true);
				}
			}

		} catch (Exception ex) {
			logger
					.info("Exception while getting SelectionRequest Object from RBT_CONSENT table");
			logger.info(ex);
		}

		return selectionRequest;
	}

	public static List<DoubleConfirmationRequestBean> getDoubleConfirmationRequestBean(
			Connection conn, String consentStatus, String transId, String subscriberID, String type, boolean isUniqueMsisdnSupported, boolean isRequestTimeCheckRequired, Integer flag) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE ";
			if (transId != null) {
				query += KEY_ID_COL + " = '" + transId + "'";
			} else {
				query += CONSENT_STATUS_COL + " = '" + consentStatus + "'";
			}
			
			if(subscriberID!=null)
				query += " AND SUBSCRIBER_ID = '"+subscriberID+"'";
			
			if(type!=null)
				query += " AND " + REQUEST_TYPE_COL + " = '" + type + "'";
			
			if (isRequestTimeCheckRequired) {
				query += " AND " + REQUEST_TIME_COL + " <= SYSDATE()";
			}
			
			if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.PROVISIONING, WebServiceConstants.INLINE_PARAMETERS, "false")) {
				if(flag == null)
					query += " AND " + INLINE_DAEMON_FLAG_COL + " IS " + null;
				else 
					query += " AND " + INLINE_DAEMON_FLAG_COL + " = " + flag;
			}
			
			query += " ORDER BY REQUEST_TIME ASC " ;
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, isUniqueMsisdnSupported);
			logger.info("pending records found "+requestBeanList.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return requestBeanList;
	}

	public static List<DoubleConfirmationRequestBean> getRequestsByModeNMsisdnNStatus(
			Connection conn, String consentStatus, String subscriberID, String mode,boolean descOrderEnabled) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + CONSENT_STATUS_COL + " = '" + consentStatus + "' AND SUBSCRIBER_ID = "+sqlString( subscriberID );
			
			if(null != mode){
				query += " AND " + MODE_COL + " IN (" + mode +")";
			}

			if(descOrderEnabled){
				query += " ORDER BY REQUEST_TIME DESC " ;				
			}else{
				query += " ORDER BY REQUEST_TIME ASC " ;
			}
			
			
			logger.info("Consent Select Query = "+query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, false);
			logger.info("num of pending records found="+requestBeanList.size());
		}
		catch (Exception ex)
		{
			logger.error("Exception, e");
		}
		return requestBeanList;
	}
	
	public static List<DoubleConfirmationRequestBean> getRequestsByModeNMsisdnNType(
			Connection conn, String consentStatus, String subscriberID, String type) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + CONSENT_STATUS_COL + " = '" + consentStatus + "' AND SUBSCRIBER_ID = '"+subscriberID+"'";
			query += " AND " + REQUEST_TYPE_COL + " = '" + type + "'";
			query += " ORDER BY REQUEST_TIME ASC " ;
			
			logger.info("Consent Select Query = "+query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, true);
			logger.info("num of pending records found="+requestBeanList.size());
		}
		catch (Exception ex)
		{
			logger.error("Exception, e");
		}
		return requestBeanList;
	}
	//CG Integration Flow - Jira -12806
	public static List<DoubleConfirmationRequestBean> getConsentRecordListBySongID(
			Connection conn, String subscriberID, String type,String rbtWaveFileName, String mode) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + CONSENT_STATUS_COL + " IN ('0','1','2') AND SUBSCRIBER_ID = '"+subscriberID+"'";
			query += " AND " + REQUEST_TYPE_COL + " = '" + type + "'";
			if (null != rbtWaveFileName) {
				query += " AND " + WAV_FILE_NAME_COL + " = " + sqlString(rbtWaveFileName);
			}
			if( null != mode) {
				query += " AND " + MODE_COL + " IN ( " + mode + ")";
			}
			
			query += " ORDER BY REQUEST_TIME DESC " ;
			
			logger.info("Consent Select Query = "+query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, true);
			logger.info("num of pending records found="+requestBeanList.size());
		}
		catch (Exception ex)
		{
			logger.error("Exception, e");
		}
		return requestBeanList;
	}
	
	public static List<DoubleConfirmationRequestBean> getPendingRequestsByMsisdnNType(
			Connection conn, String subscriberID, String type) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + CONSENT_STATUS_COL + " IN ('0','1','2') AND SUBSCRIBER_ID = '"+subscriberID+"'";
			query += " AND " + REQUEST_TYPE_COL + " = '" + type + "'";
			query += " ORDER BY REQUEST_TIME ASC " ;
			
			logger.info("Consent Select Query = "+query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, true);
			logger.info("num of pending records found="+requestBeanList.size());
		}
		catch (Exception ex)
		{
			logger.error("Exception, e");
		}
		return requestBeanList;
	}
	
	public static List<DoubleConfirmationRequestBean> getPendingRequestsByAgentId(Connection conn, String subscriberID, String agentId) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + CONSENT_STATUS_COL + " IN ('0','1') AND "+AGENT_ID+" = '"+agentId+"'";
			if(subscriberID!=null && subscriberID.length()>0){
				query +=" AND SUBSCRIBER_ID = '"+subscriberID+"'";
			}
			query += " AND "+REQUEST_TYPE_COL+" ='ACT' ";
			query += " ORDER BY REQUEST_TIME DESC " ;
			
			logger.info("Consent Select Query = "+query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, true);
			logger.info("Num of pending records found="+requestBeanList.size());
		}
		catch (Exception ex)
		{
			logger.error("Exception, e");
		}
		return requestBeanList;
	}
	
	public static boolean deleteLatestPendingConsentRecordsByAgentId(
			Connection con, String subscriberId, String agentId) {

		logger.info("Request to delete latest consent records for subId "
				+ subscriberId + " and Agent Id " + agentId);
		List<DoubleConfirmationRequestBean> consentBaseRecordList = getPendingRequestsByAgentId(
				con, subscriberId, agentId);
		DoubleConfirmationRequestBean consentBaseLatest = null;
		if (consentBaseRecordList != null && consentBaseRecordList.size() >= 1) {
			consentBaseLatest = consentBaseRecordList.get(0);
		}
		String extraInfo = null;
		if (consentBaseLatest != null) {
			extraInfo = consentBaseLatest.getExtraInfo();
		}
		Map extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		String selTransID = null;
		String baseTransId = consentBaseLatest.getTransId();
		if (extraInfoMap != null) {
			selTransID = (String) extraInfoMap.get("TRANS_ID");
		}
		boolean delete = false;
		if (baseTransId != null) {
			delete = deleteConsentTableRecordFromTransID(con, baseTransId);
		}
		if (delete && selTransID != null) {
			delete = deleteConsentTableRecordFromTransID(con, selTransID);
		}

		if (delete) {
			logger.info("Consent pending records delete for SubscriberId "
					+ subscriberId + " Agent Id " + agentId);
		} else {
			logger.info("Consent pending records could not be deleted for SubscriberId "
					+ subscriberId + " Agent Id " + agentId);
		}
		return delete;

	}
	public static List<DoubleConfirmationRequestBean> getPendingRequestsByMsisdnNTypeNRequestTime(
			Connection conn, String subscriberID, String type,String requestFromTime,String requestToTime) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + CONSENT_STATUS_COL
					+ " IN ('0','1','2') AND SUBSCRIBER_ID = '" + subscriberID
					+ "' AND " + REQUEST_TYPE_COL + " = '" + type + "'";
			
			if(requestFromTime!=null && requestToTime!=null){
				query += " AND " + REQUEST_TIME_COL + " BETWEEN '" + requestFromTime
					+ "' AND '" + requestToTime + "'";
			}
			query += " ORDER BY REQUEST_TIME ASC " ;
			logger.info("Consent getPendingRequestsByMsisdnNTypeNRequestTime Query = "+query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, false);
			logger.info("num of pending records found="+requestBeanList.size());
		}
		catch (Exception ex)
		{
			logger.error("Exception, e");
		}
		return requestBeanList;
	}
	
	public static DoubleConfirmationRequestBean getRequestsByTransIdNMsisdnNStatus(
			Connection conn, String consentStatus, String subscriberID, String transId) {
		Statement stmt = null;
		String query = null;
		DoubleConfirmationRequestBean requestBean = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + CONSENT_STATUS_COL + " = '" + consentStatus + "' AND SUBSCRIBER_ID = '"+subscriberID+"'";
			query += " AND " + KEY_ID_COL + " = '" + transId + "'";
			
			logger.info("Consent Select Query = "+query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			requestBean = getRequestBean(rs);
		}
		catch (Exception ex)
		{
			logger.error("Exception, e");
		}
		return requestBean;
	}

	public static DoubleConfirmationRequestBean getRequestsByTransIdNMsisdn(
			Connection conn, String subscriberID, String transId) {
		Statement stmt = null;
		String query = null;
		DoubleConfirmationRequestBean requestBean = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE SUBSCRIBER_ID = '"+subscriberID+"'";
			query += " AND " + KEY_ID_COL + " = '" + transId + "'";
			
			logger.info("Consent Select Query = "+query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			requestBean = getRequestBean(rs);
		}
		catch (Exception ex)
		{
			logger.error("Exception, e");
		}
		return requestBean;
	}

	
	public static List<DoubleConfirmationRequestBean> getConsentRequestForCallBack(
			Connection conn, String transId, String subscriberID) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + KEY_ID_COL + " = '" + transId + "'";
			query += " AND " + CONSENT_STATUS_COL + " IN (0,1,2,3,4)";
			
			if(subscriberID!=null)
				query += " AND SUBSCRIBER_ID = '"+subscriberID+"'";
			
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return requestBeanList;
	}
	
	public static List<DoubleConfirmationRequestBean> getConsentPendingRequests(
			Connection conn, String transId, String subscriberID) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + KEY_ID_COL + " = '" + transId + "'";
			query += " AND " + CONSENT_STATUS_COL + " IN (0,1)";
			
			if(subscriberID!=null)
				query += " AND SUBSCRIBER_ID = '"+subscriberID+"'";
			
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return requestBeanList;
	}
	
	public static List<DoubleConfirmationRequestBean> getRecordsBeforeConfRequestTime(
			Connection conn, String hours) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		long millis = 1000000;
		try {
			if (hours != null){
				long ht = Integer.parseInt(hours);
				millis = ht * 60 * 60 * 1000;
			}
			long millsec = new Date().getTime() - millis;
			Date date = new Date(millsec);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String str = sdf.format(date);
			query = "SELECT * FROM RBT_CONSENT WHERE REQUEST_TIME < TIMESTAMP('"
					+ str + "')";
			// For selection request chargeClass is coming as null some vodafone .Jira id : -RBT-12312
			query += " ORDER BY REQUEST_TIME,request_type ASC " ; 
			logger.info("getRecordsBeforeConfRequestTime Query = "+query);
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, false);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return requestBeanList;
	}
	
	public static List<DoubleConfirmationRequestBean> getExpiredConsentRecords(
			Connection conn, String hours, String consentStatus, int startingFrom, int limit,
			String mode) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		long millis = 1000000;
		try {
			if (hours != null) {
				double ht = Double.parseDouble(hours);
				millis = (long)(ht * 60 * 60 * 1000);
			}
			long millsec = new Date().getTime() - millis;
			Date date = new Date(millsec);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String str = sdf.format(date);
			query = "SELECT * FROM RBT_CONSENT WHERE "
					+ "REQUEST_TIME < TIMESTAMP('" + str
					+ "') AND CONSENT_STATUS = " + consentStatus
					+ " AND MODE IN (" + mode + ") LIMIT " + startingFrom
					+ ", " + limit;
			logger.info("Getting expired consent records. Query: " + query);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, false);
		} catch (Exception ex) {
			logger.error("Unable to get expired consent records. Exception: "
					+ ex.getMessage(), ex);
		}
		logger.info("Returning expired consent records: " + requestBeanList
				+ ", hours: " + hours);
		return requestBeanList;
	}
	
	public static boolean updateConsentStatus(Connection conn,
			String subscriberId, String transId, String consentStatus) {
		boolean isUpdated = false;
		if (subscriberId == null)
			return false;
		String query = "UPDATE RBT_CONSENT SET " + CONSENT_STATUS_COL + " = '"
				+ consentStatus + "' WHERE " + SUBSCRIBER_ID_COL + " = '"
				+ subscriberId + "' AND TRANS_ID = '" + transId + "'";

		logger.debug("Updating consent status. Query: " + query);

		isUpdated = executeQuery(conn, query);
		logger.info("Updated consent status. isUpdated: " + isUpdated
				+ ", Query: " + query);
		return isUpdated;
	}

	private static List<DoubleConfirmationRequestBean> getRequestBean(
			ResultSet rs, boolean isUniqueSupported) {
		if (rs == null)
			return null;
		List<DoubleConfirmationRequestBean> requestBeanList = new ArrayList<DoubleConfirmationRequestBean>();
		Set<String> uniqueMsisdnSet = new HashSet<String>();
		try {
			while (rs.next()) {
				DoubleConfirmationRequestBean reqBean = new DoubleConfirmationRequestBean();
				reqBean.setTransId(rs.getString("TRANS_ID"));
				String subscriberID = rs.getString("SUBSCRIBER_ID");
				if(uniqueMsisdnSet.contains(subscriberID) && isUniqueSupported){
					continue;
				}
				uniqueMsisdnSet.add(subscriberID);
				reqBean.setSubscriberID(subscriberID);
				reqBean.setCallerID(rs.getString("CALLER_ID"));
				reqBean.setCategoryID(rs.getInt("CATEGORY_ID"));
				reqBean
						.setSubscriptionClass(rs
								.getString("SUBSCRIPTION_CLASS"));
				reqBean.setMode(rs.getString("MODE"));
				reqBean.setStartTime(rs.getTimestamp("START_TIME"));
				reqBean.setEndTime(rs.getTimestamp("END_TIME"));
				reqBean.setStatus(rs.getInt("STATUS"));
				reqBean.setClassType(rs.getString("CLASS_TYPE"));
				if(rs.getInt("COS_ID")!=0 && rs.getInt("COS_ID")>0){
				   reqBean.setCosId(rs.getInt("COS_ID"));
				}
				reqBean.setPackCosID(rs.getInt("PACK_COS_ID")); 
				reqBean.setClipID(rs.getInt("CLIP_ID"));
				reqBean.setSelInterval(rs.getString("SEL_INTERVAL"));
				reqBean.setFromTime(rs.getInt("FROM_TIME"));
				reqBean.setToTime(rs.getInt("TO_TIME"));
				reqBean.setSelectionInfo(rs.getString("SELECTION_INFO"));
				reqBean.setSelType(rs.getInt("SEL_TYPE"));
				reqBean.setInLoop(rs.getString("IN_LOOP"));
				reqBean.setPurchaseType(rs.getString("PURCHASE_TYPE"));
				reqBean
						.setUseUIChargeClass(rs
								.getString("USE_UI_CHARGE_CLASS"));
				reqBean.setCategoryType(rs.getString("CATEGORY_TYPE"));
				if(rs.getInt("STATUS") == 99){
				   Date tsEnd = rs.getTimestamp("END_TIME");
				   Date tsStart = rs.getTimestamp("START_TIME");
				   long milliEnd = tsEnd.getTime();
				   long milliStart = tsStart.getTime();
				   String profilehrs = getProfileHrs(milliEnd,milliStart);
				   reqBean.setProfileHrs(profilehrs);
				}
				reqBean.setPrepaidYes(rs.getString("PREPAID_YES"));
				reqBean.setFeedType(rs.getString("FEED_TYPE"));
				reqBean.setWavFileName(rs.getString("WAV_FILE_NAME"));
				reqBean.setRbtType(rs.getInt("RBT_TYPE"));
				reqBean.setCircleId(rs.getString("CIRCLE_ID"));
				reqBean.setLanguage(rs.getString("LANGUAGE"));
				reqBean.setRequestTime(rs.getTimestamp("REQUEST_TIME"));
				reqBean.setExtraInfo(rs.getString("EXTRA_INFO"));
				reqBean.setRequestType(rs.getString("REQUEST_TYPE"));
				reqBean.setConsentStatus(rs.getInt("CONSENT_STATUS"));
				if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.PROVISIONING, WebServiceConstants.INLINE_PARAMETERS, "false")) 
					reqBean.setInlineFlag((Integer)rs.getObject(INLINE_DAEMON_FLAG_COL));
				requestBeanList.add(reqBean);
			}
		} catch (Exception ex) {
			logger
					.info("Exception while getting info from RBT_CONSENT For DoubleConfirmationRequestBean");
		}
		return requestBeanList;

	}

	private static DoubleConfirmationRequestBean getRequestBean(ResultSet rs)
	{
		if (rs == null)
			return null;
		DoubleConfirmationRequestBean reqBean = new DoubleConfirmationRequestBean();
		try
		{
			if (rs.next())
			{
				reqBean.setTransId(rs.getString("TRANS_ID"));
				String subscriberID = rs.getString("SUBSCRIBER_ID");
				reqBean.setSubscriberID(subscriberID);
				reqBean.setCallerID(rs.getString("CALLER_ID"));
				reqBean.setCategoryID(rs.getInt("CATEGORY_ID"));
				reqBean.setSubscriptionClass(rs.getString("SUBSCRIPTION_CLASS"));
				reqBean.setMode(rs.getString("MODE"));
				reqBean.setStartTime(rs.getTimestamp("START_TIME"));
				reqBean.setEndTime(rs.getTimestamp("END_TIME"));
				reqBean.setStatus(rs.getInt("STATUS"));
				reqBean.setClassType(rs.getString("CLASS_TYPE"));
				if(rs.getInt("COS_ID")!=0 && rs.getInt("COS_ID")>0){
				    reqBean.setCosId(rs.getInt("COS_ID"));
				}
				reqBean.setPackCosID(rs.getInt("PACK_COS_ID")); 
				reqBean.setClipID(rs.getInt("CLIP_ID"));
				reqBean.setSelInterval(rs.getString("SEL_INTERVAL"));
				reqBean.setFromTime(rs.getInt("FROM_TIME"));
				reqBean.setToTime(rs.getInt("TO_TIME"));
				reqBean.setSelectionInfo(rs.getString("SELECTION_INFO"));
				reqBean.setSelType(rs.getInt("SEL_TYPE"));
				reqBean.setInLoop(rs.getString("IN_LOOP"));
				reqBean.setPurchaseType(rs.getString("PURCHASE_TYPE"));
				reqBean.setUseUIChargeClass(rs.getString("USE_UI_CHARGE_CLASS"));
				reqBean.setCategoryType(rs.getString("CATEGORY_TYPE"));
				if(rs.getInt("STATUS") == 99){
				   Date tsEnd = rs.getTimestamp("END_TIME");
				   Date tsStart = rs.getTimestamp("START_TIME");
				   long milliEnd = tsEnd.getTime();
				   long milliStart = tsStart.getTime();
				   String profilehrs = getProfileHrs(milliEnd,milliStart);
				   reqBean.setProfileHrs(profilehrs);
				}
				reqBean.setPrepaidYes(rs.getString("PREPAID_YES"));
				reqBean.setFeedType(rs.getString("FEED_TYPE"));
				reqBean.setWavFileName(rs.getString("WAV_FILE_NAME"));
				reqBean.setRbtType(rs.getInt("RBT_TYPE"));
				reqBean.setCircleId(rs.getString("CIRCLE_ID"));
				reqBean.setLanguage(rs.getString("LANGUAGE"));
				reqBean.setRequestTime(rs.getTimestamp("REQUEST_TIME"));
				reqBean.setExtraInfo(rs.getString("EXTRA_INFO"));
				reqBean.setRequestType(rs.getString("REQUEST_TYPE"));
				reqBean.setConsentStatus(rs.getInt("CONSENT_STATUS"));
				if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.PROVISIONING, WebServiceConstants.INLINE_PARAMETERS, "false"))
					reqBean.setInlineFlag((Integer)rs.getObject(INLINE_DAEMON_FLAG_COL));
			}
		}
		catch (Exception ex)
		{
			logger.info("Exception while getting info from RBT_CONSENT For DoubleConfirmationRequestBean");
		}
		return reqBean;
	}

	public static SubscriptionRequest getSubscriptionRecord(Connection conn,
			String subscriberID, String timestamp, String mode, String transid, String consentStatus,String requestType) {
		Statement stmt = null;
		String query = null;
		SubscriptionRequest subscriptionRequest = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE " + SUBSCRIBER_ID_COL
					+ " = '" + subscriberID + "'";

			if (transid != null && !transid.equalsIgnoreCase("")) {
				query += " AND " + KEY_ID_COL + " = " + sqlString(transid);
			} else {
				if (mode != null) {
					if (masterSubModesMap != null
							&& masterSubModesMap.containsKey(mode)) {
						query += " AND " + MODE_COL + " IN ("
								+ masterSubModesMap.get(mode) + ")";
					} else {
						query += " AND " + MODE_COL + " = " + sqlString(mode);
					}
				}

			}
			
			if(consentStatus != null) {
				query = query + " AND " + CONSENT_STATUS_COL + " = " + consentStatus;
			}
            if(requestType!=null){
            	query = query + " AND " + REQUEST_TYPE_COL + " = " + sqlString(requestType);
            }else{
            	query = query + " AND " + REQUEST_TYPE_COL + " = " + sqlString("ACT");
            }
			stmt = conn.createStatement();
			logger.info("Query for getting Matching Subscription Record::"
					+ query);
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {

				String subscriptionClass = rs.getString("SUBSCRIPTION_CLASS");
				Date endDate = rs.getDate("END_TIME");
				int cosID = rs.getInt("COS_ID");
				String prepaidYes = rs.getString("PREPAID_YES");
				int rbtType = rs.getInt("RBT_TYPE");
				String circleID = rs.getString("CIRCLE_ID");
				String language = rs.getString("LANGUAGE");
				String extraInfo = rs.getString("EXTRA_INFO");
				String transID = rs.getString("TRANS_ID");
				String subMode = rs.getString("MODE");
				String activatedBy = rs.getString("SELECTION_INFO");
				subscriptionRequest = new SubscriptionRequest(subscriberID);
				subscriptionRequest.setSubscriptionClass(subscriptionClass);
				subscriptionRequest.setSubscriberEndDate(endDate);
				if (cosID != 0 && cosID > 0){
				  subscriptionRequest.setCosID(cosID);
				}
				subscriptionRequest.setMode(subMode);
				subscriptionRequest.setModeInfo(activatedBy);
				subscriptionRequest.setActivationMode(subMode);
				subscriptionRequest.setIsPrepaid(true);
				subscriptionRequest.setRbtType(rbtType);
				subscriptionRequest.setCircleID(circleID);
				subscriptionRequest.setLanguage(language);
				subscriptionRequest.setInfo(extraInfo);
				subscriptionRequest.setConsentTransId(transID);
			}
		} catch (Exception ex) {
			logger
					.info("Exception while getting SubscriptionRequest Object from RBT_CONSENT table");
			logger.info(ex);
		}

		return subscriptionRequest;
	}

	public static boolean deleteConsentTableRecord(Connection conn,
			String transid, String clipID, String categoryID,
			String subscriberID, String mode, boolean isShuffle) {
		Statement stmt = null;
		String query = null;
		boolean response = false;
		try {
			query = "DELETE FROM RBT_CONSENT WHERE ";

			if (subscriberID != null)
				query += SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID);

			if (transid != null) {
				query += " AND " + KEY_ID_COL + " = " + sqlString(transid);
			} else {
				if( isShuffle ){
					query += " AND " + CATEGORY_TYPE_COL + " = "
							+categoryID;
				}else if( clipID != null ){
						query += " AND " + CLIP_ID_COL + " = "
								+ Integer.parseInt(clipID);
				}
				
				if (mode != null) {
					if (masterSubModesMap.containsKey(mode)) {
						query += " AND " + MODE_COL + " IN ("
								+ masterSubModesMap.get(mode) + ")";
					} else {
						query += " AND " + MODE_COL + " = " + sqlString(mode);
					}
				}
			}
			
			stmt = conn.createStatement();
			logger.info("Query for deleting Consent Record :: " + query);
			int n = stmt.executeUpdate(query);
			if (n > 0)
				response = true;
		} catch (Exception ex) {
			logger
					.info("Exception while Deleting Records From RBT_CONSENT table");
			logger.info(ex);
		}

		return response;
	}

	public static boolean deleteConsentTableRecordFromTransID(Connection conn,
			String transID) {
		Statement stmt = null;
		String query = null;
		boolean response = false;
		try {
			query = "DELETE FROM RBT_CONSENT WHERE " + KEY_ID_COL + " = "
					+ sqlString(transID);
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			int n = stmt.executeUpdate(query);
			if (n > 0)
				response = true;
		} catch (Exception ex) {
			logger
					.info("Exception while Deleting Records From RBT_CONSENT table");
		}

		return response;

	}

	public static boolean deleteRequestOfMsisdnNModeNClass(Connection conn, String msisdn, String mode, String subClass, 
			String chargeClass, String consentStatus)
	{
		Statement stmt = null;
		String query = null;
		boolean response = false;
		try
		{
			query = "DELETE FROM RBT_CONSENT WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(msisdn);
			query += " AND " + MODE_COL + " = " + sqlString(mode);
			query += " AND " + STATUS_COL + " = " + sqlString(consentStatus);
			if(subClass != null)
				query += " AND " + SUBSCRIPTION_CLASS_COL + " = " + sqlString(subClass);
			if(chargeClass != null)
				query += " AND " + CLASS_TYPE_COL + " = " + sqlString(chargeClass);
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			int n = stmt.executeUpdate(query);
			if (n > 0)
				response = true;
		}
		catch (Exception e)
		{
			logger.error("Exception", e);
		}
		return response;
	}

	public static boolean deleteRequestByTransIdAndMSISDN(Connection conn, String transId, String subscriberID)
	{
		Statement stmt = null;
		String query = null;
		boolean response = false;
		try
		{
			query = "DELETE FROM RBT_CONSENT WHERE " + KEY_ID_COL + " = " + sqlString(transId);
			
			if(subscriberID!=null)
				query += " AND " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "'";
			
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			int n = stmt.executeUpdate(query);
			if (n > 0)
				response = true;
		}
		catch (Exception e)
		{
			logger.error("Exception", e);
		}
		return response;
	}

	public static SubscriptionRequest getSubscriptionRecordForTransID(
			Connection conn, String transId) {
		Statement stmt = null;
		String query = null;
		SubscriptionRequest subscriptionRequest = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE TRANS_ID = "
					+ sqlString(transId);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				String subscriberID = rs.getString("SUBSCRIBER_ID");
				String subscriptionClass = rs.getString("SUBSCRIPTION_CLASS");
				String mode = rs.getString("MODE");
				Date subscriberStartDate = rs.getTimestamp("START_TIME");
				Date subscriberEndDate = rs.getTimestamp("END_TIME");
				int cosID = rs.getInt("COS_ID");
				int rbtType = rs.getInt("RBT_TYPE");
				String circleID = rs.getString("CIRCLE_ID");
				String language = rs.getString("LANGUAGE");
				String extraInfo = rs.getString("EXTRA_INFO");
				String activatedBy = rs.getString("SELECTION_INFO");
				Date requestTime = rs.getTimestamp("REQUEST_TIME");
				subscriptionRequest = new SubscriptionRequest(subscriberID);
				if (subscriptionClass != null)
					subscriptionRequest.setSubscriptionClass(subscriptionClass);
				if (mode != null)
					subscriptionRequest.setMode(mode);
				if (cosID != 0 && cosID > 0)
					subscriptionRequest.setCosID(cosID);
				if (circleID != null)
					subscriptionRequest.setCircleID(circleID);
				if (language != null)
					subscriptionRequest.setLanguage(language);
				if (subscriberEndDate != null)
					subscriptionRequest.setSubscriberEndDate(subscriberEndDate);
				if(extraInfo!=null)
					subscriptionRequest.setExtraInfo(extraInfo);
				subscriptionRequest.setModeInfo(activatedBy);
				subscriptionRequest.setRbtType(rbtType);
				subscriptionRequest.setRequestTime(requestTime);

			}

		} catch (Exception ex) {
			logger
					.info("ConsentTableImpl :: Exception While Getting Records for Subscription..");
			logger.info(ex);
		}
		return subscriptionRequest;
	}
	
	static boolean updateConsentRecordForDownload(Connection conn, String subscriberId, String transID, String fromTime, String toTime, String selInterval, int status, String loopStatus, String callerId) {
		
		boolean success = false;
		if (subscriberId == null)
			return false;

		String query = "UPDATE " + TABLE_NAME + " SET " + FROM_TIME_COL + " = " + sqlString(fromTime) + 
				", " + TO_TIME_COL + " = " + sqlString(toTime)  +  ", " + LOOP_STATUS_COL + " = " + sqlString(loopStatus) + ", "+
				SEL_INTERVAL_COL + " = " + sqlString(selInterval) + ", " + STATUS_COL + " = " + status ;
		
		if(callerId != null) {
			query = query + ", " + CALLER_ID_COL + " = " + sqlString(callerId);
		}
		query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId) + " AND " + KEY_ID_COL + " = " + sqlString(transID);
		
		logger.info("Query: " + query);
		
		success = executeQuery(conn, query);
		return success;
 		
	}

	static boolean insertSelectionRecord(Connection conn, String transID,
			String subscriberID, String callerID, String categoryId,
			String subClass, String selectedBy, Date startTime, Date endTime,
			int status, String chargeClassType, String cosID, String packCosId,
			String clipId, String selInterval, int fromTime, int toTime,
			String selectionInfo, int selType, boolean inLoop,
			String purchageType, boolean useUIChargeClass, int categoryType,
			String profileHours, boolean isPrepaid, String feedType,
			String waveFile, int rbtType, String circleID, String language,
			Date requestDate, String extraInfoMap, String requestType,
			int consentSatus, String agentId, Integer inlineFlag) {

		String date = null;
		String requestTime = null;
		String enDate = null;
		String prepaid = null;
		String uiChargeCls = null;
		String loop = null;

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			date = "SYSDATE";
			requestTime = "SYSDATE";
			enDate = "TO_DATE('20371231','yyyyMMdd')";
			if (requestDate != null)
				requestTime = sqlTime(requestDate);
			if (startTime != null)
				date = sqlTime(startTime);
			if (endTime != null)
				enDate = sqlTime(endTime);

		} else {

			date = "SYSDATE()";
			requestTime = "SYSDATE()";
			enDate = "TIMESTAMP('2037-12-31')";
			if (startTime != null)
				date = mySQLDateTime(startTime);
			if (endTime != null)
				enDate = mySQLDateTime(endTime);
			if (requestDate != null)
				requestTime = mySQLDateTime(requestDate);
		}

		if (inLoop) {
			date = "SYSDATE()";
			requestTime = "SYSDATE()";
			enDate = "TIMESTAMP('2037-12-31')";
			if (requestDate != null)
				requestTime = mySQLDateTime(requestDate);
			if (startTime != null)
				date = mySQLDateTime(startTime);
			if (endTime != null)
				enDate = mySQLDateTime(endTime);
		}

		if (inLoop) {

			loop = "l";
		} else {
			loop = "o";
		}

		if (useUIChargeClass) {
			uiChargeCls = "y";
		} else {
			uiChargeCls = "n";
		}

		if (isPrepaid) {
			prepaid = "y";
		} else {
			prepaid = "n";
		}
		
		if(clipId != null) {
			try
			{
				Integer.parseInt(clipId);

			}
			catch (Exception e)
			{
				clipId = null;
			}
		}

		String query = "INSERT INTO " + TABLE_NAME + " ( " + KEY_ID_COL;
		query += ", " + SUBSCRIBER_ID_COL;
		query += ", " + CALLER_ID_COL;
		query += ", " + CATEGORY_ID_COL;
		query += ", " + SUBSCRIPTION_CLASS_COL;
		query += ", " + MODE_COL;
		query += ", " + START_DATE_COL;
		query += ", " + 	 END_DATE_COL;
		query += ", " + STATUS_COL;
		query += ", " + CLASS_TYPE_COL;
		if(cosID != null) {
			query += ", " + COS_ID_COL;
		}
		if(packCosId !=  null) {
			query += ", " + PACK_COS_ID_COL;
		}
		if(clipId != null) {
			query += ", " + CLIP_ID_COL;
		}
		query += ", " + SEL_INTERVAL_COL;
		query += ", " + FROM_TIME_COL;
		query += ", " + TO_TIME_COL;
		query += ", " + SELECTION_INFO_COL;
		query += ", " + SELECTION_TYPE_COL;
		query += ", " + LOOP_STATUS_COL;
		query += ", " + PURCHASE_TYPE_COL;
		query += ", " + USE_UI_CHARGE_CLASS_COL;
		query += ", " + CATEGORY_TYPE_COL;
		query += ", " + PROFILE_HRS_COL;
		query += ", " + PREPAID_YES_COL;
		query += ", " + FEED_TYPE_COL;
		query += ", " + WAV_FILE_NAME_COL;
		query += ", " + RBT_TYPE_COL;
		query += ", " + CIRCLE_ID_COL;
		query += ", " + LANGUAGE_COL;
		query += ", " + REQUEST_TIME_COL;
		query += ", " + EXTRA_INFO_COL;
		query += ", " + REQUEST_TYPE_COL;
		query += ", " + CONSENT_STATUS_COL;
		query += ", " + AGENT_ID;
		if(inlineFlag != null)
			query += ", " + INLINE_DAEMON_FLAG_COL;
		query += ")";

		query += " VALUES ( " + "'" + transID + "'";
		query += ", " + sqlString(subscriberID);
		query += ", " + sqlString(callerID);
		query += ", " + sqlString(categoryId);
		query += ", " + sqlString(subClass);
		query += ", " + sqlString(selectedBy);
		query += ", " + date;
		query += ", " + enDate;
		query += ", " + status;
		query += ", " + sqlString(chargeClassType);
		if(cosID !=  null) {
			query += ", " + sqlString(cosID);
		}
		if(packCosId != null) {
			query += ", " + sqlString(packCosId);
		}
		if(clipId != null) {
			query += ", " + sqlString(clipId);
		}
		query += ", " + sqlString(selInterval);
		query += ", " + fromTime;
		query += ", " + toTime;
		query += ", " + sqlString(selectionInfo);
		query += ", " + selType;
		query += ", " + sqlString(loop);
		query += ", " + sqlString(purchageType);
		query += ", " + sqlString(uiChargeCls);
		query += ", " + categoryType;
		query += ", " + sqlString(profileHours);
		query += ", " + sqlString(prepaid);
		query += ", " + sqlString(feedType);
		query += ", " + sqlString(waveFile);
		query += ", " + rbtType;
		query += ", " + sqlString(circleID);
		query += ", " + sqlString(language);
		query += ", " + requestTime;
		query += ", " + sqlString(extraInfoMap);
		query += ", " + sqlString(requestType);
		query += ", " + consentSatus;
		query += ", " + sqlString(agentId);
		if(inlineFlag != null)
			query += ", " + inlineFlag;
		query += ")";

		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger
					.info("Insertion into RBT_CONSENT table is SUCCESS for selection: "
							+ subscriberID);
			return true;
		} else {
			logger
					.info("Insertion into RBT_CONSENTtable is FAILED for selection: "
							+ subscriberID);
			return false;
		}
	}

	public static boolean updateConsentStatusBySubscriberId(Connection conn, String subscriberId,
			String consentStatus) {
		boolean success = false;
		if (subscriberId == null)
			return false;
		String query = "UPDATE RBT_CONSENT SET " + CONSENT_STATUS_COL + " = '"
				+ consentStatus + "' WHERE " + SUBSCRIBER_ID_COL + "  = '" + subscriberId + "'";
		
		logger.info("Quer: " + query);
		
		success = executeQuery(conn, query);
		return success;

	}
	
	public static boolean updateConsentInlineDaemonFlag(Connection conn, String transId, Integer flag) {
		boolean success = false;
		if (transId == null)
			return false;
		String query = "UPDATE RBT_CONSENT SET " + INLINE_DAEMON_FLAG_COL + " = "
				+ flag + " WHERE " + KEY_ID_COL + " = '" + transId +"'";
		
		logger.info("Quer: " + query);
		
		success = executeQuery(conn, query);
		return success;

	}
	
	public static boolean resetConsentInlineDaemonFlag(Connection conn) {
		boolean success = false;
		
		//IN is not using index in the query, so updating first 0 and then 1
//		String query = "UPDATE RBT_CONSENT SET " + INLINE_DAEMON_FLAG_COL + " = "
//				+ null + " WHERE " + INLINE_DAEMON_FLAG_COL +" IN (0, 1)";
		
		String query = "UPDATE RBT_CONSENT SET " + INLINE_DAEMON_FLAG_COL + " = "
				+ null + " WHERE " + INLINE_DAEMON_FLAG_COL +" = 0";
		
		logger.info("Quer: " + query);
		
		success = executeQuery(conn, query);
		
		query = "UPDATE RBT_CONSENT SET " + INLINE_DAEMON_FLAG_COL + " = "
				+ null + " WHERE " + INLINE_DAEMON_FLAG_COL +" = 1";
		
		logger.info("Quer: " + query);
		
		success = executeQuery(conn, query);
		
		return success;
	}
	
	public static boolean updateConsentStatus(Connection conn, String selectionInfo, String subscriberID,
			String transId, String consentStatus, String oldConsentStatus, String mode, String extraInfo,String  circleId, Integer flag) {
		boolean success = false;
		if (transId == null)
			return false;
		String query = "UPDATE RBT_CONSENT SET " + CONSENT_STATUS_COL + " = '"
				+ consentStatus + "'";
		if (mode != null) {
			query = query + ", " + MODE_COL + " = '" + mode + "'"; 
		}
		
		if (selectionInfo != null) {
			query = query + ", " + SELECTION_INFO_COL + " = '" + selectionInfo + "'"; 
		}
		
		if (extraInfo != null) {
			query = query + ", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo);
		}
		if (circleId != null && !circleId.trim().isEmpty()) {
			query = query + ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
		if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.PROVISIONING, WebServiceConstants.INLINE_PARAMETERS, "false"))
			query = query + ", " + INLINE_DAEMON_FLAG_COL + " = " + flag;
		query = query + " WHERE TRANS_ID = '" + transId + "'";
		
		if(subscriberID!=null)
			query += " AND " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "'";
		
		if(oldConsentStatus != null) {
			query = query + " AND " + CONSENT_STATUS_COL + " = " + sqlString(oldConsentStatus);
		}
		
		logger.info("Query: " + query);
		
		success = executeQuery(conn, query);
		return success;
	}

	public static boolean updateMode(Connection conn, String trxid,
			String subscriberID, String transId, String mode, String circleId) {
		boolean success = false;
		if (transId == null)
			return false;
		String query = "UPDATE RBT_CONSENT SET ";
		if (mode == null && trxid == null && circleId == null) {
			return true;
		}
		boolean appendComma = false;
		if (mode != null) {
			query += MODE_COL + " = " + sqlString(mode);
			appendComma = true;
		}
		if (circleId != null) {
			if (appendComma)
				query += ", ";
			query += CIRCLE_ID_COL + " =  " + sqlString(circleId);
			appendComma = true;
		}
		if (trxid != null) {
			if (appendComma)
				query += ", ";
			query += SELECTION_INFO_COL + " = CONCAT(" + SELECTION_INFO_COL
					+ ",'" + trxid + "')";
		}
		query += " WHERE TRANS_ID = '" + transId + "'";

		if (subscriberID != null)
			query += " AND " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "'";

		logger.info("Query: " + query);

		success = executeQuery(conn, query);
		return success;
	}
	
	public static DoubleConfirmationRequestBean getFirstSelectionRequest(
			Connection conn, String subscriberId , String type) {
		List<DoubleConfirmationRequestBean> reqBeanList = null;
		DoubleConfirmationRequestBean reqBean = null;
		String query = "SELECT * FROM RBT_CONSENT WHERE SUBSCRIBER_ID='"
				+ subscriberId
				+ "' AND " + CONSENT_STATUS_COL	+ " = '0' "; 

		if(type!=null){
			query += " AND REQUEST_TYPE = '"+type+"'";
		}else{
			query += " AND  (REQUEST_TYPE='SEL' OR REQUEST_TYPE='DWN') ";
		}
		
		query +=  " ORDER BY REQUEST_TIME ASC LIMIT 1";
		
		try {
			Statement st = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = st.executeQuery(query);
			reqBeanList = getRequestBean(rs, true);
			if (reqBeanList != null && reqBeanList.size() > 0)
				reqBean = reqBeanList.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reqBean;
	}
	
	public static List<DoubleConfirmationRequestBean> getSelectionRequest(
			Connection conn, String subscriberId, String type, String status, boolean isUniqueMsisdnSupported) {
		List<DoubleConfirmationRequestBean> reqBeanList = null;
		DoubleConfirmationRequestBean reqBean = null;
		String query = "SELECT * FROM RBT_CONSENT WHERE SUBSCRIBER_ID=" + sqlString(subscriberId);
		if(status != null) {
			query = query + " AND "	+ CONSENT_STATUS_COL + " = " + sqlString(status);
		}
			query = query	+ " AND  REQUEST_TYPE= "+ sqlString(type) + " ORDER BY REQUEST_TIME ASC";
		try {
			Statement st = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = st.executeQuery(query);
			reqBeanList = getRequestBean(rs, isUniqueMsisdnSupported);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reqBeanList;
	}

	public static boolean updateExtraInfo(Connection conn, String subscriberID,
			String transId, String extraInfo) {
		return updateExtraInfo(conn, subscriberID, transId, extraInfo, null);
	}

	// RBT-14675- Tata Docomo | Instead of Consent ID we are populating
	// Transaction Id in system
	public static boolean updateExtraInfo(Connection conn, String subscriberID,
			String transId, String extraInfo, String circleID) {
		return updateExtraInfo(conn, subscriberID, transId, extraInfo,
				circleID, null);
	}

	// RBT-14675- Tata Docomo | Instead of Consent ID we are populating
	// Transaction Id in system
	public static boolean updateExtraInfo(Connection conn, String subscriberID,
			String transId, String extraInfo, String circleID,
			String consentStatus) {
		if (extraInfo == null || extraInfo.equalsIgnoreCase("null")) {
			return false;
		}
		String circleIdQuery = "";
		if (circleID != null && !circleID.trim().isEmpty()) {
			circleIdQuery += "," + CIRCLE_ID_COL + " = " + sqlString(circleID);
		}
		String consentStatusClause = "";
		if (consentStatus != null) {
			consentStatusClause += " AND " + CONSENT_STATUS_COL + " = "
					+ sqlString(consentStatus);
		}
		boolean success = false;
		String query = "UPDATE RBT_CONSENT SET " + EXTRA_INFO_COL + " = '"
				+ extraInfo + "' " + circleIdQuery + " WHERE TRANS_ID = '"
				+ transId + "'" + consentStatusClause;

		if (subscriberID != null)
			query += " AND " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		success = executeQuery(conn, query);
		return success;
	}
	
	public static boolean updateExtraInfoAndStatus(Connection conn, String subscriberID,
			String transId, String extraInfo,String consentStatus) {
		if(extraInfo == null || extraInfo.equalsIgnoreCase("null")){
			return false;
		}
		boolean success = false;
		String query = "UPDATE RBT_CONSENT SET " + EXTRA_INFO_COL + " = " + sqlString(extraInfo) ;
		
		if(consentStatus!=null)
		    query += " , " + CONSENT_STATUS_COL + " = " + sqlString(consentStatus);
						
		query += " WHERE TRANS_ID = " + sqlString(transId) ;
		
		if(subscriberID!=null)
			query += " AND " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID);
		
		logger.info("Executing the query: " + query);
		success = executeQuery(conn, query);
		return success;

	}

	public static boolean executeQuery(Connection conn, String query) {
		boolean success = false;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			int n = stmt.executeUpdate(query);
			if (n > 0)
				success = true;
			logger.info("Query Execution Result = " + success);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(stmt !=null) {
				try {
					stmt.close();
				} catch (Throwable e) {
					logger.error("Exception in closing db statement", e);
				}
			}
		}
		return success;
	}

	public static int[] getTime(int itime) {

		String strTime = itime + "";
		int[] times = new int[2];
		if (strTime.length() <= 2) {
			times[0] = 0;
			times[1] = Integer.parseInt(strTime);
			return times;
		}

		String time = strTime.substring(0, strTime.length() - 2);
		String mins = strTime.substring(strTime.length() - 2);

		if (time != null && time.trim().length() > 0)
			times[0] = Integer.parseInt(time);
		if (mins != null && mins.trim().length() > 0)
			times[1] = Integer.parseInt(mins);

		return times;
	}

	public static boolean isSameConsentActivationRequest(Connection conn,String subscriberID,String subClass,String consentStatus){
        String query = "SELECT * FROM RBT_CONSENT WHERE "+ SUBSCRIBER_ID_COL + " = '"+subscriberID+"'";
     
        if(subClass!=null){
        	query += " AND "+ SUBSCRIPTION_CLASS_COL +" = '"+subClass+"'";
        }
       
        if(consentStatus!=null){
        	query +=" AND " + CONSENT_STATUS_COL +" IN ( "+consentStatus+" )";
        }
        
        query += " AND "+ REQUEST_TYPE_COL + " = 'ACT'";
		
        logger.info("Query: " + query);
        
        boolean isSame = false;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next())
				isSame = true;
			logger.info("Query Execution Result = " + isSame);
		} catch (SQLException e) {
			logger.error("Exception", e);
			
		}
		
		return isSame;
	}
	public static Map<String, String> initializeMasterSubModesMapping() {
		// MasterMode:SubMode|MasterMode:SubMode|MasterMode:SubMode|MasterMode:SubMode
		String confMasterSubsModes = RBTParametersUtils.getParamAsString(
				"COMMON", "MAPPING_OF_MASTER_SUB_MODES_FOR_CONSENT", null);
		Map<String, String> masterSubModesMap1 = new HashMap<String, String>();
		if (confMasterSubsModes != null) {
			String modeList[] = confMasterSubsModes.split("\\|");
			for (int i = 0; i < modeList.length; i++) {
				String aa[] = modeList[i].split(":");
				if (aa.length != 2)
					continue;
				String subModes[] = aa[1].split(",");
				StringBuilder sMode = new StringBuilder();
				String st = null;
				for (String str : subModes)
					sMode.append("\"" + str + "\",");
				st = sMode.toString().substring(0, sMode.lastIndexOf(","));
				masterSubModesMap1.put(aa[0], st);
			}
		}
		return masterSubModesMap1;
	}
	
	
	private static String getProfileHrs(long endTime,long startTime){
		   long profileMilliSec = endTime - startTime;
		   String profileHrs = null;
		   int time = 0;
		   if(profileMilliSec > 0){
			   time = (int)(profileMilliSec/(24*60*60*1000));
			   profileHrs = time>0?("D"+time):null;
		   }
		   if(profileHrs == null){
			   time = (int)(profileMilliSec/(60*60*1000));
			   profileHrs = time>0?time+"":null;
		   }
		   if(profileHrs == null){
			   time = (int)(profileMilliSec/(60*1000));
			   profileHrs = time>0?("M"+time):null;
		   }
		   logger.info("Profile hours for consent selection== "+profileHrs);
         return profileHrs;
	}
	
	static boolean convertSubscriptionTypeConsentUpgrde(Connection conn,
			String subscriberID, String activate, Date startDate, Date endDate, 
			String activationInfo, String prepaid, String subscriptionClass,
			String cosID, int rbtType,String extraInfo,
			String circleID, String refID, String consentStatus, Date requestTimeAsDate) {
		String date = null;
		String requestTime = null;
		String enDate = null;
		String requestType = "UPGRADE";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			date = "SYSDATE";
			if (requestTimeAsDate == null) {
				requestTime = "SYSDATE";
			} else {
				requestTime = sqlTime(requestTimeAsDate);
			}
			enDate = "TO_DATE('20371231','yyyyMMdd')";
			if (startDate != null)
				date = sqlTime(startDate);
			if (endDate != null)
				enDate = sqlTime(endDate);
		} else {
			date = "SYSDATE()";
			if (requestTimeAsDate == null) {
				requestTime = "SYSDATE()";
			} else {
				requestTime = mySqlTime(requestTimeAsDate);
			}
			enDate = "TIMESTAMP('2037-12-31')";
			if (startDate != null)
				date = mySQLDateTime(startDate);
			if (endDate != null)
				enDate = mySQLDateTime(endDate);
		}

		String query = "INSERT INTO " + TABLE_NAME + " ( " + KEY_ID_COL;
		query += ", " + SUBSCRIBER_ID_COL;
		query += ", " + MODE_COL;
		query += ", " + START_DATE_COL;
		query += ", " + END_DATE_COL;
		query += ", " + SELECTION_INFO;
		query += ", " + PREPAID_YES_COL;
		query += ", " + SUBSCRIPTION_CLASS_COL;
		// Third Party confirmation chages
		if (null != cosID) {
			query += ", " + COS_ID_COL;
		}
		query += ", " + RBT_TYPE_COL;
		query += ", " + CIRCLE_ID_COL;
		query += ", " + EXTRA_INFO_COL;
		query += ", " + REQUEST_TIME_COL;
		query += ", " + REQUEST_TYPE_COL;
		query += ", " + CONSENT_STATUS_COL;
		query += ")";

		query += " VALUES ( " + "'" + refID + "'";
		query += ", " + sqlString(subscriberID);
		query += ", " + sqlString(activate);
		query += ", " + date;
		query += ", " + enDate;
		query += ", " + sqlString(activationInfo);
		query += ", " + "'" + prepaid + "'";
		query += ", " + sqlString(subscriptionClass);
		// Third Party confirmation chages
		if (null != cosID) {
			query += ", " + Integer.parseInt(cosID);
		}
		query += ", " + rbtType;
		query += ", " + sqlString(circleID);
		query += ", " + sqlString(extraInfo);
		query += ", " + requestTime;
		query += ", " + sqlString(requestType);
		query += ", " + consentStatus;
		query += ")";

		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger
					.info("Insertion into RBT_CONSENT table is SUCCESS for subscriber: "
							+ subscriberID);
			return true;
		}

		logger
				.info("Insertion into RBT_CONSENTtable is FAILED for subscriber: "
						+ subscriberID);
		return false;
	}

	static SubscriberStatus[] getSubscriberSelections(Connection conn,
			String subID, String callID, int rbtType) {
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
				+ CALLER_ID_COL + getNullForWhere(callID)
				+ " AND " + START_DATE_COL + " <= " + sysDateStr + " AND "
				+ END_DATE_COL + " > " + sysDateStr + " AND " + STATUS_COL
				+ " IN (1,75,94,95,80,79) AND " +  CONSENT_STATUS_COL + " IN (0,1,2)";
		
		if (m_rrbtOn)
			query += " AND " + SELECTION_TYPE_COL + " = " + rbtType;
		
		query += " ORDER BY " + REQUEST_TIME_COL + " DESC ";

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
			}
		} catch (SQLException se) {
			logger.error("Returning null. SQLException: " + se.getMessage(), se);
			return null;
		} finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_CONSENT successfully. Total rows: "
				+ subscriberStatusList.size());
		return SubscriberStatusImpl
				.convertSubscriberStatusListToArray(subscriberStatusList);
	}
	
	/* subscription manager changes */
	static SubscriberStatus smSubscriberSelections(Connection conn,
			String subID, String callID, int st, int rbtType) {
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		String sysDateStr = "SYSDATE";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
				+ CALLER_ID_COL + getNullForWhere(callID) + " AND ";
		// TODO: REQUEST_TIME
		query = query + REQUEST_TIME_COL + " <= " + sysDateStr + " AND "
				+ END_DATE_COL + " > " + sysDateStr + " AND " + STATUS_COL
				+ " = " + st;
		if (m_rrbtOn)
			query += " AND " + RBT_TYPE_COL + " = " + rbtType;
		query += " ORDER BY " + REQUEST_TIME_COL + " DESC ";

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next()) {
				subscriberStatus = getSubscriberStatusFromRS(results);
			}
		} catch (SQLException se) {
			logger.error("Returning null, SQLException: " + se.getMessage(), se);
			return null;
		} finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Returning subscriberStatus: " + subscriberStatus
				+ ", subID: " + subID);
		return subscriberStatus;
	}
	
	public static SubscriberStatus getSubscriberStatusFromRS(ResultSet rs)
			throws SQLException {
		RBTResultSet results = new RBTResultSet(rs);
		String subscriberID = results.getString(SUBSCRIBER_ID_COL);
		String callerID = results.getString(CALLER_ID_COL);
		int categoryID = results.getInt(CATEGORY_ID_COL);
		String subscriberWavFile = results.getString(WAV_FILE_NAME_COL);
		Date reqTime = results.getTimestamp(REQUEST_TIME_COL);
		Date startTime = results.getTimestamp(START_DATE_COL);
		Date endTime = results.getTimestamp(END_DATE_COL);
		int status = results.getInt(STATUS_COL);
		String classType = results.getString(CLASS_TYPE_COL);
		String selectedBy = results.getString(MODE_COL);
		String selectionInfo = results.getString(SELECTION_INFO_COL);
//		Date nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
		String prepaid = results.getString(PREPAID_YES_COL);
		int fromTime = results.getInt(FROM_TIME_COL);
		int toTime = results.getInt(TO_TIME_COL);
//		String sel_status = results.getString(SEL_STATUS_COL);
//		String deSelectedBy = results.getString(DESELECTED_BY_COL);
//		String oldClassType = results.getString(OLD_CLASS_TYPE_COL);
		int categoryType = results.getInt(CATEGORY_TYPE_COL);
		char loopStatus = results.getString(LOOP_STATUS_COL).charAt(0);
		String selInterval = results.getString(SEL_INTERVAL_COL);
		String refID = results.getString(KEY_ID_COL);
		String extraInfo = results.getString(EXTRA_INFO_COL);
		int selType = results.getInt(SELECTION_TYPE_COL);
		
		String circleId = results.getString(CIRCLE_ID_COL);
//		String retryCount = results.getString(RETRY_COUNT_COL);
//		Date nextRetryTime = results.getTimestamp(NEXT_RETRY_TIME_COL);
		

		return new SubscriberStatusImpl(subscriberID, callerID, categoryID,
				subscriberWavFile, reqTime, startTime, endTime, status,
				classType, selectedBy, selectionInfo, null,
				prepaid, fromTime, toTime, null, null,
				null, categoryType, loopStatus, selType, selInterval,
				refID, extraInfo, circleId, null, null,null);
	}
	
	public static String getSquenceNumber(Connection conn, String squenceName) {
		String query = "SELECT nextval('" + squenceName + "') AS next_sequence";
		Statement stmt = null;
		ResultSet results = null;
		String squenceNumber = null;
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next()) {
				squenceNumber = results.getString("next_sequence");
			}
		} catch (SQLException se) {
			logger.error("Returning null, SQLException: " + se.getMessage(), se);
			return null;
		} finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Returning SquenceNumber: " + squenceNumber);
		return squenceNumber;
	}
	
	public static void setRRBT(boolean rRBT){
		m_rrbtOn = rRBT;  
	} 
		//RBT-14652
	public static List<DoubleConfirmationRequestBean> getDoubleConfirmationRequestBeanForSAT(
			Connection conn) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE CONSENT_STATUS IN ('0')  AND " + RETRY_TIME_COL + " <= SYSDATE() ORDER BY RETRY_TIME ASC" ;
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, false);
			logger.info("pending records found "+requestBeanList.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return requestBeanList;
	}
	
	public static List<DoubleConfirmationRequestBean> getLatestDoubleConfirmationRequestBeanForSAT(
			Connection conn, String subscriberId) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE CONSENT_STATUS IN ('1')  AND "
					+ SUBSCRIBER_ID
					+ " = "
					+ sqlString(subscriberId);
			
			if (RBTParametersUtils.getParamAsBoolean("COMMON",
					"DOUBLE_OPT_IN_UPDATE_LATEST_REQUEST", "TRUE")) {
				query += " ORDER BY REQUEST_TIME DESC ";
			} else {
				query += " ORDER BY REQUEST_TIME ASC ";
			}
					
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, false);
			logger.info("pending records found " + requestBeanList.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return requestBeanList;
	}
	
	public static boolean updateExtraInfoAndStatusWithReqTime(Connection conn, String subscriberID,
			String transId, String extraInfo,String consentStatus, Date requestTimeAsDate) {
		if(extraInfo == null ){
			logger.info("consent update failed since extraInfo is null:"+ extraInfo);
			return false;
		}
		boolean success = false;
		String requestTime = null;
		if (requestTimeAsDate == null) {
			requestTime = "SYSDATE()";
		} else {
			requestTime = mySqlTime(requestTimeAsDate);
		}
		String query = "UPDATE RBT_CONSENT SET " + EXTRA_INFO_COL + " = " + sqlString(extraInfo) +" , "+ RETRY_TIME_COL + " = " + requestTime;
		
		if(extraInfo.equalsIgnoreCase("null") ){
			 query = "UPDATE RBT_CONSENT SET "+ RETRY_TIME_COL + " = " + requestTime;
		}
		
		if(consentStatus!=null){
		    query += " , " + CONSENT_STATUS_COL + " = " + sqlString(consentStatus);
		}
		
		query += " WHERE TRANS_ID = " + sqlString(transId) ;
		
		if(subscriberID!=null)
			query += " AND " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID);
		
		
		logger.info("Executing the query: " + query);
		success = executeQuery(conn, query);
		return success;

	}

	public static List<DoubleConfirmationRequestBean> getAllDoubleConfirmationRequestBeanForSATUpgrade(
			Connection conn, String subscriberID, String consentStatus,
			String transId) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE ";
			if (consentStatus != null) {
				query += CONSENT_STATUS_COL + " IN ('1',"
						+ sqlString(consentStatus) + ")";
			}
			if (subscriberID != null) {
				query += " AND SUBSCRIBER_ID = '" + subscriberID + "'";
			}
			if (transId != null) {
				query += " AND " + KEY_ID_COL + " = " + sqlString(transId);
			}
			if (RBTParametersUtils.getParamAsBoolean("COMMON",
					"DOUBLE_OPT_IN_UPDATE_LATEST_REQUEST", "TRUE")) {
				query += " ORDER BY REQUEST_TIME DESC ";
			} else {
				query += " ORDER BY REQUEST_TIME ASC ";
			}
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, false);
			logger.info("pending records found " + requestBeanList.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return requestBeanList;
	}

	public static String getNextProtocolNumber(Connection conn,
			String sq_sequence) {
		String query = "SELECT nextval('" + sq_sequence + "') AS next_sequence";
		Statement stmt = null;
		ResultSet results = null;
		String squenceNumber = null;
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next()) {
				squenceNumber = results.getString("next_sequence");
			}
		} catch (SQLException se) {
			logger.error("Returning null, SQLException: " + se.getMessage(), se);
			return null;
		} finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Returning SquenceNumber: " + squenceNumber);
		return squenceNumber;
	}
	
	
	public static List<DoubleConfirmationRequestBean> getDoubleConfirmationRequestBeanByRequestType(
			Connection conn,  String subscriberID, String type, boolean isUniqueMsisdnSupported, boolean isRequestTimeCheckRequired) {
		Statement stmt = null;
		String query = null;
		List<DoubleConfirmationRequestBean> requestBeanList = null;
		try {
			query = "SELECT * FROM RBT_CONSENT WHERE ";
			
			if(subscriberID!=null)
				query += " SUBSCRIBER_ID = '"+subscriberID+"'";
			
			if(type!=null)
				query += " AND " + REQUEST_TYPE_COL + " = '" + type + "'";
			
			if (isRequestTimeCheckRequired) {
				query += " AND " + REQUEST_TIME_COL + " <= SYSDATE()";
			}
			query += " ORDER BY REQUEST_TIME ASC " ;
			stmt = conn.createStatement();
			logger.info("Executing the query: " + query);
			ResultSet rs = stmt.executeQuery(query);
			requestBeanList = getRequestBean(rs, isUniqueMsisdnSupported);
			logger.info("pending records found "+requestBeanList.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return requestBeanList;
	}
	//Added for RBT-18249 for RTO issue
		public static boolean checkRtoinBase(Connection conn, String subscriberID,
				String sdpomtxnId) {
			Statement stmt = null;
			String query = null;
			boolean isExists = false;
			try {
				query = "SELECT * FROM RBT_SUBSCRIBER WHERE " + SUBSCRIBER_ID_COL
						+ " = " + sqlString(subscriberID) ;
				stmt = conn.createStatement();
				logger.info("Query For Selection Matching Record :: " + query);
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					String extraInfo = rs.getString("EXTRA_INFO");
					Map<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					if (extraInfoMap.get("sdpomtxnid").equals(sdpomtxnId)) {
						isExists = true;
						break;
					}
				}

			} catch (Exception ex) {
				logger.info("Exception while getting Base Object from RBT_SUBSCRIBER table");
				logger.info(ex);
			}

			return isExists;
		}

		public static boolean checkRtoinSelection(Connection conn, String wavFile,
				String categoryID, String subscriberID, String sdpomtxnId) {
			Statement stmt = null;
			String query = null;
			boolean isExists = false;
			try {
				query = "SELECT * FROM RBT_SUBSCRIBER_SELECTIONS WHERE "
						+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID);

				if (wavFile != null && !wavFile.isEmpty()) {
					query += " AND SUBSCRIBER_WAV_FILE = " + sqlString(wavFile);
				}
				if (categoryID != null && !categoryID.isEmpty()) {
					query += " AND " + CATEGORY_ID_COL + " ="
							+ Integer.parseInt(categoryID);
				}
				stmt = conn.createStatement();
				logger.info("Query For Selection Matching Record :: " + query);
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					String extraInfo = rs.getString("EXTRA_INFO");
					Map<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					if (extraInfoMap.get("sdpomtxnid").equals(sdpomtxnId)) {
						isExists = true;
						break;
					}
				}

			} catch (Exception ex) {
				logger.info("Exception while getting Selection Object from RBT_SUBSCRIBER_SELECTIONS table");
				logger.info(ex);
			}

			return isExists;
		}

		public static boolean checkRtoforCombo(Connection conn, String wavFile,
				String categoryID, String subscriberID, String sdpomtxnId) {
			boolean isBaseExists = false;
			boolean isSelectionExists = false;
			boolean isExists = false;
			try {
				isBaseExists = checkRtoinBase(conn, subscriberID, sdpomtxnId);
				if (isBaseExists)
					isSelectionExists = checkRtoinSelection(conn, wavFile,
							categoryID, subscriberID, sdpomtxnId);
				if (isBaseExists && isSelectionExists) {
					isExists = true;
				} else
					isExists = false;
			} catch (Exception ex) {
				logger.info(ex);
			}
			return isExists;
		}
		//Ended for RBT-18249
}
