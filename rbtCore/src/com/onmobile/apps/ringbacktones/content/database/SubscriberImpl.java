package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.activemonitoring.core.CounterStats;
import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.daemons.SMDaemonPerformanceMonitor;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.monitor.common.Constants;

public class SubscriberImpl extends RBTPrimitive implements Subscriber, iRBTConstant
{
	@Override
	public String toString() {
		return "SubscriberImpl [m_subscriberID=" + m_subscriberID + ", m_activate=" + m_activate + ", m_deactivate="
				+ m_deactivate + ", m_startDate=" + m_startDate + ", m_endDate=" + m_endDate + ", m_prepaid="
				+ m_prepaid + ", m_accessDate=" + m_accessDate + ", m_nextChargingDate=" + m_nextChargingDate
				+ ", m_access=" + m_access + ", m_info=" + m_info + ", m_subscriptionClass=" + m_subscriptionClass
				+ ", m_subscription=" + m_subscription + ", m_lastDeactivationInfo=" + m_lastDeactivationInfo
				+ ", m_lastDeactivationDate=" + m_lastDeactivationDate + ", m_activationDate=" + m_activationDate
				+ ", m_old_class_type=" + m_old_class_type + ", m_num_max_selections=" + m_num_max_selections
				+ ", m_cosID=" + m_cosID + ", m_activatedCosID=" + m_activatedCosID + ", m_rbt_type=" + m_rbt_type
				+ ", m_language=" + m_language + ", m_extraInfo=" + m_extraInfo + ", m_circleID=" + m_circleID
				+ ", m_refID=" + m_refID + ", retryCount=" + retryCount + ", nextRetryTime=" + nextRetryTime
				+ ", operatorName=" + operatorName + ", m_prism_next_billing_date=" + m_prism_next_billing_date + "]";
	}

	private static Logger logger = Logger.getLogger(SubscriberImpl.class);

	private static final String TABLE_NAME = "RBT_SUBSCRIBER";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String ACTIVATED_BY_COL = "ACTIVATED_BY";
	private static final String DEACTIVATED_BY_COL = "DEACTIVATED_BY";
	private static final String START_DATE_COL = "START_DATE";
	private static final String END_DATE_COL = "END_DATE";
	private static final String PREPAID_YES_COL = "PREPAID_YES";
	private static final String LAST_ACCESS_DATE_COL = "LAST_ACCESS_DATE";
	private static final String NEXT_CHARGING_DATE_COL = "NEXT_CHARGING_DATE";
	private static final String NUM_VOICE_ACCESS_COL = "NUM_VOICE_ACCESS";
	private static final String ACTIVATION_INFO_COL = "ACTIVATION_INFO";
	private static final String SUBSCRIPTION_CLASS_COL = "SUBSCRIPTION_CLASS";
	private static final String SUBSCRIPTION_YES_COL = "SUBSCRIPTION_YES";
	private static final String LAST_DEACTIVATION_INFO_COL = "LAST_DEACTIVATION_INFO";
	private static final String LAST_DEACTIVATION_DATE_COL = "LAST_DEACTIVATION_DATE";
	private static final String ACTIVATION_DATE_COL = "ACTIVATION_DATE";
	private static final String NUM_MAX_SELECTIONS_COL = "NUM_MAX_SELECTIONS";
	private static final String COS_ID_COL = "COS_ID";
	private static final String ACTIVATED_COS_ID_COL = "ACTIVATED_COS_ID";
	private static final String OLD_CLASS_TYPE_COL = "OLD_CLASS_TYPE";
	private static final String RBT_TYPE_COL = "RBT_TYPE";
	private static final String LANGUAGE_COL = "LANGUAGE";
	private static final String GENDER_COL = "GENDER";
	private static final String AGE_COL = "AGE";
	private static final String PLAYER_STATUS_COL = "PLAYER_STATUS";
	private static final String EXTRA_INFO_COL = "EXTRA_INFO";
	private static final String CIRCLE_ID_COL = "CIRCLE_ID";
	private static final String INTERNAL_REF_ID_COL = "INTERNAL_REF_ID";
	private static final String RETRY_COUNT_COL = "RETRY_COUNT";
	private static final String NEXT_RETRY_TIME_COL = "NEXT_RETRY_TIME";
	private static final String NEXT_BILLING_DATE_COL = "NEXT_BILLING_DATE";
	
	private String m_subscriberID;
	private String m_activate;
	private String m_deactivate;
	private Date m_startDate;
	private Date m_endDate;
	private String m_prepaid;
	private Date m_accessDate;
	private Date m_nextChargingDate;
	private int m_access;
	private String m_info;
	private String m_subscriptionClass;
	private String m_subscription;
	private String m_lastDeactivationInfo;
	private Date m_lastDeactivationDate;
	private Date m_activationDate;
	private String m_old_class_type;
	private int m_num_max_selections;
	private String m_cosID;
	private String m_activatedCosID;
	private int m_rbt_type;
	private String m_language;
	private String m_extraInfo;
	private String m_circleID;
	private String m_refID;
	private String retryCount;
	private Date nextRetryTime;
	private String operatorName;
	
	//Added for new column
	private Date m_prism_next_billing_date;

	public void setPrismNextBillingDate(Date prismnextBillingDate)
	{
		m_prism_next_billing_date = prismnextBillingDate;
	}
	
	public Date prismNextBillingDate(){
		return m_prism_next_billing_date;
	}

	private static String m_databaseType = getDBSelectionString();
	

	static boolean convertSubscriptionType(Connection conn,
			String subscriberID, String initClassType, String finalClassType,
			String strActBy, String strActInfo, boolean concatActInfo,
			int rbtType, boolean useRbtType, String extraInfo, Subscriber subscriber)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='C', "
				+ ACTIVATION_DATE_COL + " = NULL, " + SUBSCRIPTION_CLASS_COL + " = "
				+ sqlString(finalClassType) + " , " + OLD_CLASS_TYPE_COL + " = "+ sqlString(initClassType)+","
				+ RBT_TYPE_COL+" = "+rbtType;
		
		if(extraInfo != null)
			query = query + ", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo);
		else 
			query = query + ", " + EXTRA_INFO_COL + " = NULL";
		if(strActBy != null)
			query = query + ", " + ACTIVATED_BY_COL + " = " + sqlString(strActBy);
		if(strActInfo != null)
		{
			if (concatActInfo)
				strActInfo = trimAndConcatActivationInfo(subscriber, strActInfo, true);

			query = query + ", " + ACTIVATION_INFO_COL + " = " + sqlString(strActInfo);
		}
		
		query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
				+ SUBSCRIPTION_CLASS_COL + " = " + sqlString(initClassType) + " AND "
				+ SUBSCRIPTION_YES_COL + " in  ('B','z','Z','G','N')";
		
		logger.info("Executing the query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query,Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	static boolean convertSubscriptionTypeAndEndDate(Connection conn,
			String subscriberID, String initClassType, String finalClassType,
			String strActBy,String extraInfo,Date endDate)
	{
		String enDate = null; 
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
	        enDate = "TO_DATE('20371231','yyyyMMdd')"; 
	        if(endDate != null) 
	        	enDate = sqlTime(endDate); 
		}
        else
        {
	        enDate = "TIMESTAMP('2037-12-31')"; 
	        if(endDate != null) 
	        	enDate = mySQLDateTime(endDate); 
        }
        
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='C', "
				+ ACTIVATION_DATE_COL + " = NULL, " + SUBSCRIPTION_CLASS_COL + " = "
				+ sqlString(finalClassType) + " , " + OLD_CLASS_TYPE_COL + " = "+ sqlString(initClassType)+","
				+ END_DATE_COL + " = "+ enDate;
		
		if(extraInfo != null)
			query = query + ", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo);
		else 
			query = query + ", " + EXTRA_INFO_COL + " = NULL";
		if(strActBy != null)
			query = query + ", " + ACTIVATED_BY_COL + " = " + sqlString(strActBy);
		
		query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
				+ SUBSCRIPTION_CLASS_COL + " = " + sqlString(initClassType) + " AND "
				+ SUBSCRIPTION_YES_COL + " in  ('B','z','Z','G','N')";
		
		logger.info("Executing the query " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query,Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}

	public SubscriberImpl(String subscriberID, String activate,
			String deactivate, Date startDate, Date endDate, String prepaid,
			Date accessDate, Date nextChargingDate, int access, String info,
			String subscriptionClass, String subscription,
			String lastDeactivationInfo, Date lastDeactivationDate,
			Date activationDate, int maxSelections, String cosID, String activatedCosID, 
			int rbtType, String language,String strOldClassType,String extraInfo, String circleID, String refID)
	{
		m_subscriberID = subscriberID;
		m_activate = activate;
		m_deactivate = deactivate;
		m_startDate = startDate;
		m_endDate = endDate;
		m_prepaid = prepaid;
		m_subscription = subscription;
		m_accessDate = accessDate;
		m_nextChargingDate = nextChargingDate;
		m_access = access;
		m_info = info;
		m_subscriptionClass = subscriptionClass;
		m_lastDeactivationInfo = lastDeactivationInfo;
		m_lastDeactivationDate = lastDeactivationDate;
		m_activationDate = activationDate;
		m_num_max_selections = maxSelections;
		m_cosID = cosID;
		m_activatedCosID = activatedCosID;
		m_rbt_type = rbtType;
		m_language = language;
		m_old_class_type = strOldClassType;
		m_extraInfo = extraInfo;
		m_circleID = circleID;
		m_refID = refID;
	}

	public SubscriberImpl(String subscriberID, String activate,
			String deactivate, Date startDate, Date endDate, String prepaid,
			Date accessDate, Date nextChargingDate, int access, String info,
			String subscriptionClass, String subscription,
			String lastDeactivationInfo, Date lastDeactivationDate,
			Date activationDate, int maxSelections, String cosID, String activatedCosID, 
			int rbtType, String language,String strOldClassType,String extraInfo, String circleID, String refID, String retryCount, Date nextRetryTime)
	{
		m_subscriberID = subscriberID;
		m_activate = activate;
		m_deactivate = deactivate;
		m_startDate = startDate;
		m_endDate = endDate;
		m_prepaid = prepaid;
		m_subscription = subscription;
		m_accessDate = accessDate;
		m_nextChargingDate = nextChargingDate;
		m_access = access;
		m_info = info;
		m_subscriptionClass = subscriptionClass;
		m_lastDeactivationInfo = lastDeactivationInfo;
		m_lastDeactivationDate = lastDeactivationDate;
		m_activationDate = activationDate;
		m_num_max_selections = maxSelections;
		m_cosID = cosID;
		m_activatedCosID = activatedCosID;
		m_rbt_type = rbtType;
		m_language = language;
		m_old_class_type = strOldClassType;
		m_extraInfo = extraInfo;
		m_circleID = circleID;
		m_refID = refID;
		this.retryCount = retryCount;
		this.nextRetryTime = nextRetryTime;
	}

	public String subID()
	{
		return m_subscriberID;
	}

	public String activatedBy()
	{
		return m_activate;
	}

	public String deactivatedBy()
	{
		return m_deactivate;
	}

	public Date startDate()
	{
		return m_startDate;
	}

	public Date endDate()
	{
		return m_endDate;
	}

	public boolean prepaidYes()
	{
		if (m_prepaid != null)
			return m_prepaid.equalsIgnoreCase("y");
		else
			logger.info("RBT:: prepaid column is null " + m_subscriberID);

		return false;
	}

	public boolean subscriptionYes()
	{
		if (m_subscription != null)
			return m_subscription.equalsIgnoreCase("y");
		else
			logger.info("RBT:: subscription column is null " + m_subscriberID);

		return false;
	}

	public String subYes()
	{
		return m_subscription;
	}

	public Date accessDate()
	{
		return m_accessDate;
	}

	public Date nextChargingDate()
	{
		return m_nextChargingDate;
	}

	public int noOfAccess()
	{
		return m_access;
	}

	public String activationInfo()
	{
		return m_info;
	}

	public String subscriptionClass()
	{
		return m_subscriptionClass;
	}

	public String lastDeactivationInfo()
	{
		return m_lastDeactivationInfo;
	}

	public Date lastDeactivationDate()
	{
		return m_lastDeactivationDate;
	}

	public Date activationDate()
	{
		return m_activationDate;
	}

	public String oldClassType()
	{
		return m_old_class_type;
	}

	public int maxSelections ()
	{
		return (m_num_max_selections);
	}

	public String cosID()
	{
		return m_cosID;
	}

	public String activatedCosID()
	{
		return m_activatedCosID;
	}

	public void setPrepaidYes(boolean prepaid)
	{
		m_prepaid = "n";
		if (prepaid)
			m_prepaid = "y";
	}

	public void setSubscriptionYes(boolean subscriptionyes)
	{
		m_subscription = "n";
		if (subscriptionyes)
			m_subscription = "y";
	}

	public void setNextChargingDate(Date date)
	{
		m_nextChargingDate = date;
	}

	public void setLastDeactivationInfo(String lastDeactivationInfo)
	{
		m_lastDeactivationInfo = lastDeactivationInfo;
	}

	public void setLastDeactivationDate(Date lastDeactivationDate)
	{
		m_lastDeactivationDate = lastDeactivationDate;
	}

	public void incrementNoOfAccess()
	{
		this.m_access++;
	}

	public String date(Date date)
	{
		DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		return sqlTimeFormat.format(date);
	}

	public int rbtType() 
	{ 
		return (m_rbt_type); 
	}

	public String language()
	{
		return m_language;
	}
	
	public void setLanguage(String language)
	{
		m_language = language;
	}
	
	public void setCosID(String cosID)
	{
		m_cosID = cosID;
	}
	
	public void setSubYes(String subYes)
	{
		m_subscription = subYes;
	}

	public void setExtraInfo(String extraInfo) {
		m_extraInfo = extraInfo;
	}
	
	public String extraInfo()
	{
		return m_extraInfo;
	}
	
	public String circleID()
	{
		return m_circleID;
	}
	
	public String refID()
	{
		return m_refID;
	}

	public String retryCount()
	{
		return retryCount;
	}

	public Date nextRetryTime()
	{
		return nextRetryTime;
	}

	static boolean setPrepaidYes(Connection conn, String subscriberID, boolean prepaidYes)
	{
		String prepaid = "n";
		if (prepaidYes)
			prepaid = "y";

		String query = "UPDATE " + TABLE_NAME + " SET " + PREPAID_YES_COL + " = " + "'" + prepaid + "'" 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return true;
	}
	
	static boolean updateSubscriberId(Connection conn, String newSubscriberId, String subscriberId)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIBER_ID_COL + " = '" + newSubscriberId + "'," 
			+ PLAYER_STATUS_COL + " = 'A' WHERE " + SUBSCRIBER_ID_COL  + " = '" + subscriberId + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n>0);
	}

	static boolean updateEndDate(Connection conn, String subscriberID, Date endDate, String subClass)
	{
		String query = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + END_DATE_COL + " = " + sqlTime(endDate) 
				+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + END_DATE_COL + " = " + mySQLDateTime(endDate) 
				+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		if(subClass != null)
			query = query + " AND SUBSCRIPTION_CLASS IN ('"+subClass+"')";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return true;
	}

	static boolean updateEndDateAndExtraInfo(Connection conn, String subscriberID, Date endDate, String extraInfo)
	{
		String query = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + END_DATE_COL + " = " + sqlTime(endDate)+" , "+ EXTRA_INFO_COL 
			       + " = " +sqlString(extraInfo)+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " 
			       + END_DATE_COL + " > SYSDATE "; 
		else
			query = "UPDATE " + TABLE_NAME + " SET " + END_DATE_COL + " = " + mySQLDateTime(endDate)+" , "+ EXTRA_INFO_COL 
		       + " = " +sqlString(extraInfo)+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " 
		       + END_DATE_COL + " > SYSDATE() "; 

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return true;
	}
	//RBT-13415 - Nicaragua Churn Management.
	static boolean updateEndDateAndExtraInfoOnlyBySubId(Connection conn, String subscriberID, Date endDate, String extraInfo)
	{
		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + END_DATE_COL + " = "
					+ sqlTime(endDate) + " , " + EXTRA_INFO_COL + " = "
					+ sqlString(extraInfo) + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "'";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + END_DATE_COL + " = "
					+ mySQLDateTime(endDate) + " , " + EXTRA_INFO_COL + " = "
					+ sqlString(extraInfo) + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return true;
	}
	
	static void setNextChargingDateAndActDate(Connection conn, String subscriberID, Date date)
	{
		String query = null;
		Date nextChargingDate = null;
		if (date != null)
			nextChargingDate = date;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
				+ " = " + sqlTime(nextChargingDate) + ", " + ACTIVATION_DATE_COL + " = SYSDATE WHERE "
				+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + END_DATE_COL + " > SYSDATE ";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
				+ " = " + mySQLDateTime(nextChargingDate) + ", " + ACTIVATION_DATE_COL + " = SYSDATE() WHERE "
				+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + END_DATE_COL + " > SYSDATE() ";
			
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return;
	}

	static void setNextChargingDate(Connection conn, String subscriberID, Date date)
	{
		String query = null;
		Date nextChargingDate = null;
		if (date != null)
			nextChargingDate = date;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL + " = " + sqlTime(nextChargingDate) + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + END_DATE_COL + " > SYSDATE ";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL + " = " + mySQLDateTime(nextChargingDate) + " WHERE " 
				+  SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + END_DATE_COL + " > SYSDATE() ";
			
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return;
	}

	static void setSubscriptionYes(Connection conn, String subscriberID, String subscriptionYes)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " = " + sqlString(subscriptionYes) 
			+ " WHERE " + SUBSCRIBER_ID_COL  + " = " + sqlString(subscriberID);

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return;
	}

	static boolean updateDeactivatedAtPlayer(Connection conn, String subscriberID, String subscriptionYes)
	{
		String initState = STATE_DEACTIVATED_INIT;
		if(subscriptionYes.equals("Z"))
			initState = "z";
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " = " + sqlString(subscriptionYes) +
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + sqlString(subscriberID) + " AND " + SUBSCRIPTION_YES_COL + " = '"+initState+"'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	static boolean upgradeToSongPack(Connection conn, String subscriberID, String cosId, String extraInfo,String validity)
	{
		String date="";
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	date="SYSDATE + " + validity;
        else
        	date="TIMESTAMPADD(DAY,"+validity+",SYSDATE())";
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " = " + sqlString(STATE_CHANGE) + "," + ACTIVATED_COS_ID_COL + " = " + COS_ID_COL + "," +
		COS_ID_COL + " = " + sqlString(cosId) + "," + NUM_MAX_SELECTIONS_COL + " = 0" + "," + EXTRA_INFO_COL + " = " + sqlString(extraInfo) +  "," + END_DATE_COL + " = " + date +
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + sqlString(subscriberID) + " AND " + SUBSCRIPTION_YES_COL + " IN ('" + STATE_ACTIVATED + "')";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}

	private static int executeUpdateQuery(Connection conn, String query) {
		int updateCount = 0;
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			updateCount = stmt.getUpdateCount();
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return updateCount;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return updateCount;
	}
	
	static void setAccessCount(Connection conn, String subscriberID, int accessCount)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + NUM_VOICE_ACCESS_COL + " = " + accessCount 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return;
	}

	static void setSubscriberLanguage(Connection conn, String subscriberID, String language)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + LANGUAGE_COL + " = " + sqlString(language) 
			+ " WHERE " + SUBSCRIBER_ID_COL  + " = " + sqlString(subscriberID);

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return;
	}

	static boolean setSubscriberGender(Connection conn, String subscriberID, String gender)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + GENDER_COL + " = " + sqlString(gender) 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);

	}
	static boolean setSubscriberAge(Connection conn, String subscriberID, int ageCategory)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + AGE_COL + " = " + ageCategory 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		
		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}
	
	static boolean setSubscriberCOS(Connection conn, String subscriberID, String cosID)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + COS_ID_COL + " = " + sqlString(cosID) 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}

	static Subscriber insert(Connection conn, String subscriberID,
			String activate, String deactivate, Date startDate, Date endDate,
			String prepaid, Date accessDate, Date nextChargingDate, int access,
			String info, String subscriptionClass, String lastDeactivationInfo,
			Date lastDeactivationDate, Date activationDate, String subscription, int maxSelections,
			String cosID, String activatedCosID, int rbtType, String language, boolean isDirectAct, String extraInfo, String circleID, String refId)
	{
		String date = null;
		String actDate = null; 
        String nextDate = null; 
        String enDate = null; 
        String accessDateStr = null;
        String lastDeactivationDateStr = null;
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
        	date = "SYSDATE";
        	actDate = sqlTime(activationDate); 
	        nextDate = sqlTime(activationDate); 
	        enDate = "TO_DATE('20371231','yyyyMMdd')"; 
	        accessDateStr = sqlTime(accessDate);
	        lastDeactivationDateStr = sqlTime(lastDeactivationDate); 
	        if (startDate != null) 
	        	date = sqlTime(startDate); 
	        if(endDate != null) 
	        	enDate = sqlTime(endDate); 
	        else 
	            endDate = new Date(System.currentTimeMillis() + 10000); 
	        if(isDirectAct) 
	        { 
	        	actDate = "SYSDATE"; 
	            nextDate = "SYSDATE"; 
	        } 
		}
        else
        {
        	date = "SYSDATE()";
        	actDate = mySQLDateTime(activationDate); 
	        nextDate = mySQLDateTime(activationDate); 
	        enDate = "TIMESTAMP('2037-12-31')"; 
	        accessDateStr = mySQLDateTime(accessDate);
	        lastDeactivationDateStr = mySQLDateTime(lastDeactivationDate);
	        if (startDate != null) 
	        	date = mySQLDateTime(startDate); 
	        if(endDate != null) 
	        	enDate = mySQLDateTime(endDate); 
	        else 
	            endDate = new Date(System.currentTimeMillis() + 10000); 
	        if(isDirectAct) 
           { 
                   actDate = "SYSDATE()"; 
                   nextDate = "SYSDATE()"; 
           }
        }

		String playerStatus = "B";
		if (isDirectAct)
		{
			boolean informPlayerOnActivation = RBTParametersUtils
					.getParamAsBoolean("DAEMON", "NOT_PLAY_SONG_INACT_USER",
							"FALSE");
			if (informPlayerOnActivation)
				playerStatus = "A";
		}

        String finalRefId = UUID.randomUUID().toString();
        if(refId != null)
        	finalRefId = refId;
		
        Subscriber subscriber = null;

		String query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + ACTIVATED_BY_COL;
		query += ", " + DEACTIVATED_BY_COL;
		query += ", " + START_DATE_COL;
		query += ", " + END_DATE_COL;
		query += ", " + PREPAID_YES_COL;
		query += ", " + LAST_ACCESS_DATE_COL;
		query += ", " + NEXT_CHARGING_DATE_COL;
		query += ", " + NUM_VOICE_ACCESS_COL;
		query += ", " + ACTIVATION_INFO_COL;
		query += ", " + SUBSCRIPTION_CLASS_COL;
		query += ", " + SUBSCRIPTION_YES_COL;
		query += ", " + LAST_DEACTIVATION_INFO_COL;
		query += ", " + LAST_DEACTIVATION_DATE_COL;
		query += ", " + ACTIVATION_DATE_COL;
		query += ", " + NUM_MAX_SELECTIONS_COL;
		query += ", " + COS_ID_COL;
		query += ", " + ACTIVATED_COS_ID_COL;
		query += ", " + RBT_TYPE_COL;
		query += ", " + LANGUAGE_COL;
		query += ", " + PLAYER_STATUS_COL;
		query += ", " + EXTRA_INFO_COL;
		query += ", " + CIRCLE_ID_COL;
		query += ", " + INTERNAL_REF_ID_COL;
		query += ")";

		query += " VALUES ( " + "'" + subscriberID + "'";
		query += ", " + sqlString(activate);
		query += ", " + sqlString(deactivate);
		query += ", " + date;
		query += ", " + enDate;
		query += ", " + "'" + prepaid + "'";
		query += ", " + accessDateStr;
		query += ", " + nextDate;
		query += ", " + access;
		query += ", " + sqlString(info);
		query += ", " + sqlString(subscriptionClass);
		query += ", " + "'" + subscription + "'";
		query += ", " + sqlString(lastDeactivationInfo);
		query += ", " + lastDeactivationDateStr;
		query += ", " + actDate;
		query += ", " + maxSelections;
		query += ", " + sqlString(cosID);
		query += ", " + sqlString(activatedCosID);
		query += ", " + rbtType;
		query += ", " + sqlString(language);
		query += ", " + sqlString(playerStatus);
		query += ", " + sqlString(extraInfo);
		query += ", " + sqlString(circleID);
		query += ", " + sqlString(finalRefId);
		query += ")";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		if (n == 1)
		{
			logger.info("Insertion into RBT_SUBSCRIBER table is SUCCESS for subscriber: " + subscriberID);
			if(startDate == null)
				startDate = new Date();
			subscriber = new SubscriberImpl(subscriberID, activate, deactivate,
					startDate, endDate, prepaid, accessDate, nextChargingDate,
					access, info, subscriptionClass, subscription,
					lastDeactivationInfo, lastDeactivationDate, activationDate, maxSelections,
					cosID, activatedCosID, rbtType, language,null,extraInfo, circleID, finalRefId);
			return subscriber;
		}
		else
		{
			logger.info("Insertion into RBT_SUBSCRIBER table is FAILED for subscriber: " + subscriberID);
			return null;
		}
	}

	static boolean update(Connection conn, String subscriberID,
			String activate, String deactivate, Date startDate, Date endDate,
			String prepaid, Date accessDate, Date nextChargingDate, int access,
			String info, String subscriptionClass, String lastDeactivationInfo,
			Date lastDeactivationDate, Date activationDate,
			String subscription, int maxSelections, int rbtType, boolean isDirectAct, String refID)
	{
		String date = null;
		String actDate = null;
		String nextDate = null;
		String enDate = null; 
		String accessDateStr = null;
        String lastDeactivationDateStr = null;
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE";
			if (startDate != null)
				date = sqlTime(startDate);
			actDate = sqlTime(activationDate); 
			nextDate = sqlTime(activationDate); 
			enDate = "TO_DATE('20371231','yyyyMMdd')"; 
			accessDateStr = sqlTime(accessDate);
			lastDeactivationDateStr = sqlTime(lastDeactivationDate); 
			if (startDate != null) 
        	   date = sqlTime(startDate); 
			if(endDate != null) 
                   enDate = sqlTime(endDate); 
			if(isDirectAct) 
			{ 
				actDate = "SYSDATE"; 
                nextDate = "SYSDATE"; 
           } 
		}
		else
		{
			date = "SYSDATE()";
			if (startDate != null)
				date = mySQLDateTime(startDate);
			actDate = mySQLDateTime(activationDate); 
	        nextDate = mySQLDateTime(activationDate); 
	        enDate = "TIMESTAMP('2037-12-31')"; 
	        accessDateStr = mySQLDateTime(accessDate);
			lastDeactivationDateStr = mySQLDateTime(lastDeactivationDate); 
			if (startDate != null) 
	               date = mySQLDateTime(startDate); 
	         if(endDate != null) 
	               enDate = mySQLDateTime(endDate); 
	         if(isDirectAct) 
	         { 
	        	 actDate = "SYSDATE()"; 
	             nextDate = "SYSDATE()"; 
	         } 
		}
		String query = "UPDATE " + TABLE_NAME + " SET " + ACTIVATED_BY_COL + " = "
		+ sqlString(activate) + ", " + DEACTIVATED_BY_COL + " = "
		+ sqlString(deactivate) + ", " + START_DATE_COL + " = " + date
		+ ", " + END_DATE_COL + " = " + enDate + ", "
		+ PREPAID_YES_COL + " = " + "'" + prepaid + "'" + ", "
		+ LAST_ACCESS_DATE_COL + " = " + accessDateStr + ", "
		+ NEXT_CHARGING_DATE_COL + " = " + nextDate
		+ ", " + NUM_VOICE_ACCESS_COL + " = " + access + ", "
		+ ACTIVATION_INFO_COL + " = " + sqlString(info) + ", "
		+ SUBSCRIPTION_CLASS_COL + " = " + sqlString(subscriptionClass)
		+ ", " + SUBSCRIPTION_YES_COL + " = '" + subscription + "'"
		+ ", " + LAST_DEACTIVATION_INFO_COL + " = "
		+ sqlString(lastDeactivationInfo) + ", "
		+ LAST_DEACTIVATION_DATE_COL + " = "
		+ lastDeactivationDateStr + ", " + ACTIVATION_DATE_COL
		+ " = " + actDate + ", "
		+ NUM_MAX_SELECTIONS_COL + " = " + maxSelections + ", "
		+ RBT_TYPE_COL + " = " + rbtType
		+ ", "+ INTERNAL_REF_ID_COL + " = "+sqlString(refID)
		+ " WHERE "	+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}

	/* subscription manager changes */
	static boolean update(Connection conn, String subscriberID,
			String activate, String deactivate, Date startDate, Date endDate,
			String prepaid, Date accessDate, Date nextChargingDate, int access,
			String info, String subscriptionClass, String lastDeactivationInfo,
			Date lastDeactivationDate, Date activationDate,
			String subscription, int maxSelections, String cosID,
			String activatedCosID, int rbtType, String language,
			String extraInfo, String circleID, String refID,
			boolean isDirectActivation)
	{
		String date = null;
		String enDate = null;
		String accessDateStr = null;
        String lastDeactivationDateStr = null;
        String nextChargingDateStr = null;
        String activationDateStr = null;
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE";
			if (startDate != null)
				date = sqlTime(startDate);
			//Ask Mohsin
			enDate = "TO_DATE('20371231','yyyyMMdd')"; 
	        if(endDate != null) 
	        	enDate = sqlTime(endDate);
	        accessDateStr = sqlTime(accessDate);
	        lastDeactivationDateStr = sqlTime(lastDeactivationDate);
	        nextChargingDateStr = sqlTime(nextChargingDate);
	        activationDateStr = sqlTime(activationDate);
		}
		else
		{
			date = "SYSDATE()";
			if (startDate != null)
				date = mySQLDateTime(startDate);
			//Ask Mohsin
			enDate = "TIMESTAMP('2037-12-31')"; 
	        if(endDate != null) 
	        	enDate = mySQLDateTime(endDate);
	        accessDateStr = mySQLDateTime(accessDate);
	        lastDeactivationDateStr = mySQLDateTime(lastDeactivationDate);
	        nextChargingDateStr = mySQLDateTime(nextChargingDate);
	        activationDateStr = mySQLDateTime(activationDate);
		}

        String playerStatus = "B";
		if (isDirectActivation)
		{
			boolean informPlayerOnActivation = RBTParametersUtils
					.getParamAsBoolean("DAEMON", "NOT_PLAY_SONG_INACT_USER",
							"FALSE");
			if (informPlayerOnActivation)
				playerStatus = "A";

			if (activationDateStr == null && m_databaseType.equalsIgnoreCase(DB_SAPDB))
				activationDateStr = "SYSDATE";
			else if (activationDateStr == null)
				activationDateStr = "SYSDATE()";
		}

		String query = "UPDATE " + TABLE_NAME + " SET " + ACTIVATED_BY_COL + " = "
		+ sqlString(activate) + ", " + DEACTIVATED_BY_COL + " = "
		+ sqlString(deactivate) + ", " + START_DATE_COL + " = " + date
		+ ", " + END_DATE_COL + " = " + enDate/*sqlTime(endDate)*/ + ", "
		+ PREPAID_YES_COL + " = " + "'" + prepaid + "'" + ", "
		+ LAST_ACCESS_DATE_COL + " = " + accessDateStr + ", "
		+ NEXT_CHARGING_DATE_COL + " = " + nextChargingDateStr
		+ ", " + NUM_VOICE_ACCESS_COL + " = " + access + ", "
		+ ACTIVATION_INFO_COL + " = " + sqlString(info) + ", "
		+ SUBSCRIPTION_CLASS_COL + " = " + sqlString(subscriptionClass)
		+ ", " + SUBSCRIPTION_YES_COL + " = '" + subscription + "'"
		+ ", " + LAST_DEACTIVATION_INFO_COL + " = "
		+ sqlString(lastDeactivationInfo) + ", "
		+ LAST_DEACTIVATION_DATE_COL + " = "
		+ lastDeactivationDateStr + ", " + ACTIVATION_DATE_COL
		+ " = " + activationDateStr + ", " + NUM_MAX_SELECTIONS_COL + " = " + maxSelections
		+ ", " + COS_ID_COL + " = " + sqlString(cosID)
		+ ", " + ACTIVATED_COS_ID_COL + " = " + sqlString(activatedCosID) 
		+ ", " + RBT_TYPE_COL + " = "+ rbtType + ", " + LANGUAGE_COL + " = " + sqlString(language)
		+ ", " + PLAYER_STATUS_COL + " = "+ sqlString(playerStatus)
		+ ", " + EXTRA_INFO_COL + " = "+ sqlString(extraInfo)
		+ ", " + CIRCLE_ID_COL + " = "+ sqlString(circleID)
		+ ", " + INTERNAL_REF_ID_COL + " = "+sqlString(refID)
		+ ", " + RETRY_COUNT_COL + " =  NULL"
		+ " WHERE "	+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}
	
	static boolean update(Connection conn, String subscriberID,
			String activate, String deactivate, Date startDate, Date endDate,
			String prepaid, Date accessDate, Date nextChargingDate, int access,
			String info, String subscriptionClass, String lastDeactivationInfo,
			Date lastDeactivationDate, Date activationDate, String subscription, int maxSelections,
			String cosID, String activatedCosID, int rbtType, String language, String extraInfo, boolean isDirectAct, String circleID, String refID)
	{
		String date = null;
		String enDate = null;
		String accessDateStr = null;
        String lastDeactivationDateStr = null;
        String nextChargingDateStr = null;
        String activationDateStr = null;
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE";
			if (startDate != null)
				date = sqlTime(startDate);
			//Ask Mohsin
			enDate = "TO_DATE('20371231','yyyyMMdd')"; 
	        if(endDate != null) 
	        	enDate = sqlTime(endDate);
	        accessDateStr = sqlTime(accessDate);
	        lastDeactivationDateStr = sqlTime(lastDeactivationDate);
	        nextChargingDateStr = sqlTime(nextChargingDate);
	        activationDateStr = sqlTime(activationDate);
	        if(isDirectAct)
	        {
	        	nextChargingDateStr = "SYSDATE";
	        	activationDateStr = "SYSDATE";
	        }
		}
		else
		{
			date = "SYSDATE()";
			if (startDate != null)
				date = mySQLDateTime(startDate);
			//Ask Mohsin
			enDate = "TIMESTAMP('2037-12-31')"; 
	        if(endDate != null) 
	        	enDate = mySQLDateTime(endDate);
	        accessDateStr = mySQLDateTime(accessDate);
	        lastDeactivationDateStr = mySQLDateTime(lastDeactivationDate);
	        nextChargingDateStr = mySQLDateTime(nextChargingDate);
	        activationDateStr = mySQLDateTime(activationDate);
	        if(isDirectAct)
	        {
	        	nextChargingDateStr = "SYSDATE()";
	        	activationDateStr = "SYSDATE()";
	        }
		}
        
        String playerStatus = "B";
		if (isDirectAct)
		{
			boolean informPlayerOnActivation = RBTParametersUtils
					.getParamAsBoolean("DAEMON", "NOT_PLAY_SONG_INACT_USER",
							"FALSE");
			if (informPlayerOnActivation)
				playerStatus = "A";
		}

		String query = "UPDATE " + TABLE_NAME + " SET " + ACTIVATED_BY_COL + " = "
		+ sqlString(activate) + ", " + DEACTIVATED_BY_COL + " = "
		+ sqlString(deactivate) + ", " + START_DATE_COL + " = " + date
		+ ", " + END_DATE_COL + " = " + enDate/*sqlTime(endDate)*/ + ", "
		+ PREPAID_YES_COL + " = " + "'" + prepaid + "'" + ", "
		+ LAST_ACCESS_DATE_COL + " = " + accessDateStr + ", "
		+ NEXT_CHARGING_DATE_COL + " = " + nextChargingDateStr
		+ ", " + NUM_VOICE_ACCESS_COL + " = " + access + ", "
		+ ACTIVATION_INFO_COL + " = " + sqlString(info) + ", "
		+ SUBSCRIPTION_CLASS_COL + " = " + sqlString(subscriptionClass)
		+ ", " + SUBSCRIPTION_YES_COL + " = '" + subscription + "'"
		+ ", " + LAST_DEACTIVATION_INFO_COL + " = "
		+ sqlString(lastDeactivationInfo) + ", "
		+ LAST_DEACTIVATION_DATE_COL + " = "
		+ lastDeactivationDateStr + ", " + ACTIVATION_DATE_COL
		+ " = " + activationDateStr + ", " + NUM_MAX_SELECTIONS_COL + " = " + maxSelections
		+ ", " + COS_ID_COL + " = " + sqlString(cosID)
		+ ", " + ACTIVATED_COS_ID_COL + " = " + sqlString(activatedCosID) 
		+ ", " + RBT_TYPE_COL + " = "+ rbtType + ", " + LANGUAGE_COL + " = " + sqlString(language)
		+ ", " + PLAYER_STATUS_COL + " = "+ sqlString(playerStatus)
		+ ", " + EXTRA_INFO_COL + " = "+ sqlString(extraInfo)
		+ ", " + CIRCLE_ID_COL + " = "+ sqlString(circleID)
		+ ", " + INTERNAL_REF_ID_COL +" = "+ sqlString(refID)
		+ " WHERE "+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}
	
	static boolean update(Connection conn, String subscriberID,
			String activatedBy, String deactivatedBy, Date startDate,
			Date endDate, String prepaidYes, Date lastAccessDate,
			Date nextChargingDate, Integer noOfAccess, String activationInfo,
			String subscriptionClass, String subscriptionYes,
			String lastDeactivationInfo, Date lastDeactivationDate,
			Date activationDate, Integer maxSelections, String cosID,
			String activatedCosID, String oldClassType, Integer rbtType,
			String language, String playerStatus, String extraInfo, String refID)
	{
		String query = null;
        String updateString = "";
        if (activatedBy != null)
			updateString += " "+ ACTIVATED_BY_COL +" = "+ sqlString(activatedBy);

		if (deactivatedBy != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			if (deactivatedBy.equalsIgnoreCase("NULL"))
				updateString += DEACTIVATED_BY_COL + " = NULL";
			else
				updateString += DEACTIVATED_BY_COL +" = "+ sqlString(deactivatedBy);
		}
		if (startDate != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				updateString += START_DATE_COL +" = "+ sqlTime(startDate);
			else
				updateString += START_DATE_COL +" = "+ mySQLDateTime(startDate);
		}
		if (endDate != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				updateString += END_DATE_COL +" = "+ sqlTime(endDate);
			else
				updateString += END_DATE_COL +" = "+ mySQLDateTime(endDate);
		}
		if (prepaidYes != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += PREPAID_YES_COL +" = "+ sqlString(prepaidYes);
		}
		if (lastAccessDate != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				updateString += LAST_ACCESS_DATE_COL +" = "+ sqlTime(lastAccessDate);
			else
				updateString += LAST_ACCESS_DATE_COL +" = "+ mySQLDateTime(lastAccessDate);
		}
		if (nextChargingDate != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				updateString += NEXT_CHARGING_DATE_COL +" = "+ sqlTime(nextChargingDate);
			else
				updateString += NEXT_CHARGING_DATE_COL +" = "+ mySQLDateTime(nextChargingDate);
		}
		if (noOfAccess != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += NUM_VOICE_ACCESS_COL +" = "+ noOfAccess;
		}
		if (activationInfo != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += ACTIVATION_INFO_COL +" = "+ sqlString(activationInfo);
		}
		if (subscriptionClass != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += SUBSCRIPTION_CLASS_COL +" = "+ sqlString(subscriptionClass);
		}
		if (subscriptionYes != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += SUBSCRIPTION_YES_COL +" = "+ sqlString(subscriptionYes);
		}
		if (lastDeactivationInfo != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += LAST_DEACTIVATION_INFO_COL +" = "+ sqlString(lastDeactivationInfo);
		}
		if (lastDeactivationDate != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				updateString += LAST_DEACTIVATION_DATE_COL +" = "+ sqlTime(lastDeactivationDate);
			else
				updateString += LAST_DEACTIVATION_DATE_COL +" = "+ mySQLDateTime(lastDeactivationDate);
		}
		if (activationDate != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				updateString += ACTIVATION_DATE_COL +" = "+ sqlTime(activationDate);
			else
				updateString += ACTIVATION_DATE_COL +" = "+ mySQLDateTime(activationDate);
		}
		if (maxSelections != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += NUM_MAX_SELECTIONS_COL +" = "+ maxSelections;
		}
		if (cosID != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += COS_ID_COL +" = "+ sqlString(cosID);
		}
		if (activatedCosID != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += ACTIVATED_COS_ID_COL +" = "+ sqlString(activatedCosID);
		}
		if (oldClassType != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += OLD_CLASS_TYPE_COL +" = "+ sqlString(oldClassType);
		}
		if (rbtType != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += RBT_TYPE_COL +" = "+ rbtType;
		}
		if (language != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += LANGUAGE_COL +" = "+ sqlString(language);
		}
		if (playerStatus != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += PLAYER_STATUS_COL +" = "+ sqlString(playerStatus);
		}
		if (refID != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += INTERNAL_REF_ID_COL +" = "+ sqlString(refID);
		}
		if (extraInfo != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += EXTRA_INFO_COL +" = "+ sqlString(extraInfo);
		}
		
		if(updateString.equals(""))
			return false;
        
        query = "UPDATE " + TABLE_NAME + " SET " + updateString + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID);

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}

	static boolean updateSubscriberCosId(Connection conn,String subscriberId,String renewalCosId,Date endDate)
	{
		String endTime = null;
		int numMaxSelection = 0;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			endTime = "TO_DATE('20371231','yyyyMMdd')"; 
	        if(endDate != null) 
	        	endTime = sqlTime(endDate);
		}else{
			
			endTime = "TIMESTAMP('2037-12-31')"; 
	        if(endDate != null) 
	        	endTime = mySQLDateTime(endDate);
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " + COS_ID_COL + " = " + sqlString(renewalCosId) + "," + END_DATE_COL + " = " + endTime + "," 
				+ NUM_MAX_SELECTIONS_COL + " = " + numMaxSelection + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberId + "'"; 
		
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}

	/**
	 * @param conn
	 * @param subcriberID
	 * @param attributeMap
	 * @return
	 *         Generic API for updating the subscriber fields.
	 */
	static boolean updateSubscriber(Connection conn, String subcriberID,
			Map<String, String> attributeMap)
	{
		String updateString = "";
		if (attributeMap.containsKey("SUBSCRIPTION_CLASS"))
			updateString = SUBSCRIPTION_CLASS_COL + " = " + sqlString(attributeMap.get("SUBSCRIPTION_CLASS"));

		if (attributeMap.containsKey("OLD_CLASS_TYPE"))
		{
			if (!updateString.equals(""))
				updateString += ", ";

			updateString += OLD_CLASS_TYPE_COL + " = " + sqlString(attributeMap.get("OLD_CLASS_TYPE"));
		}

		if (attributeMap.containsKey("SUBSCRIPTION_YES"))
		{
			if (!updateString.equals(""))
				updateString += ", ";

			updateString += SUBSCRIPTION_YES_COL + " = " + sqlString(attributeMap.get("SUBSCRIPTION_YES"));
		}

		if (attributeMap.containsKey("EXTRA_INFO"))
		{
			if (!updateString.equals(""))
				updateString += ", ";

			updateString += EXTRA_INFO_COL + " = " + sqlString(attributeMap.get("EXTRA_INFO"));
		}
		
//VIKRANT
		if (attributeMap.containsKey("PLAYER_STATUS"))
		{
			if (!updateString.equals(""))
				updateString += ", ";

			updateString += PLAYER_STATUS_COL + " = " + sqlString(attributeMap.get("PLAYER_STATUS"));
		}
		
		
		if (updateString.equals(""))
			return false;

		String query = "UPDATE " + TABLE_NAME + " SET " + updateString
				+ " WHERE " + SUBSCRIBER_ID_COL + "= " + sqlString(subcriberID);

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	static boolean deactivate(Connection conn, String subscriberID,
			String deactivate, Date endDate, boolean sendToHLR,
			boolean smDeactivation, boolean isNewSubscriber,
			boolean isDirectDeact, boolean isMemCachePlayer, String dctInfo,
			Subscriber subscriber, String userInfoXml)

	{
		String query = null;
		String date = null;
		String nextChargingDate = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE";
			if (endDate != null)
				date = sqlTime(endDate);
			nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		}
		else
		{
			date = "SYSDATE()";
			if (endDate != null)
				date = mySQLDateTime(endDate);
			nextChargingDate = "TIMESTAMP('2037-12-31')";
		}
		
		String subscriptionYes = "D";

		if (!sendToHLR && !smDeactivation)
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextChargingDate = "TO_DATE('20361231','yyyyMMdd')";
			else
				nextChargingDate = "TIMESTAMP('2036-12-31')";
		}

		String deactivatedBy = deactivate;
		if (deactivate != null && deactivate.trim().equalsIgnoreCase("DEL"))
		{
			deactivatedBy = "NA";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextChargingDate = "TO_DATE('20361231','yyyyMMdd')";
			else
				nextChargingDate = "TIMESTAMP('2036-12-31')";
		}

		query = "UPDATE " + TABLE_NAME + " SET " + DEACTIVATED_BY_COL + " = "
		+ sqlString(deactivatedBy) + ", " + END_DATE_COL + " = " + date
		+ " , ";
		if (smDeactivation)
		{
			if(isDirectDeact)
			{
				subscriptionYes = STATE_DEACTIVATED;
				if(isMemCachePlayer)
					subscriptionYes = STATE_DEACTIVATED_INIT;
			}
			query = query + SUBSCRIPTION_YES_COL + " = "
			+ sqlString(subscriptionYes) + ", ";
		}

		if(dctInfo != null)
		{
			dctInfo = trimAndConcatActivationInfo(subscriber, dctInfo, false);
            query = query + ACTIVATION_INFO_COL + " = " + sqlString(dctInfo) + ", ";
		}
		//Fix for RBT-12391,RBT-12394
		if(userInfoXml != null)
		{
			if(userInfoXml.equalsIgnoreCase(""))
				userInfoXml = null;
			query = query + EXTRA_INFO_COL + " = " + sqlString(userInfoXml) + ", ";
		}
		
		query = query + NEXT_CHARGING_DATE_COL + " = " + nextChargingDate
		+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
		+ "'";

		if (!isDirectDeact)
		{	
			if(smDeactivation)
			{
				query = query + " AND " + SUBSCRIPTION_YES_COL + " IN ('"
						+ STATE_ACTIVATED + "' ,'" + STATE_EVENT + "' ,'"
						+ STATE_SUSPENDED + "' ,'" + STATE_SUSPENDED_INIT + "','" + STATE_ACTIVATION_GRACE +"','" 
						+ STATE_ACTIVATION_PENDING + "','" + STATE_TO_BE_ACTIVATED + "','" + STATE_ACTIVATION_ERROR + "')";
			}
			else 
			{
				if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
					query = query + " AND " + END_DATE_COL + " > SYSDATE ";
				else
					query = query + " AND " + END_DATE_COL + " > SYSDATE() ";
			}
		}
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		logger.info("Executing the query: " + query + ", updated: " + n);
		return (n == 1);
	}

	
	/* subscription manager changes */
	static boolean smDeactivate(Connection conn, String subscriberID,
			String deactivate, Date endDate, boolean sendToHLR,
			boolean smDeactivation, String type)
	{
		String date = null;
		String nextChargingDate = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE";
			if (endDate != null)
				date = sqlTime(endDate);
	
			nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		}
		else
		{
			date = "SYSDATE()";
			if (endDate != null)
				date = mySQLDateTime(endDate);
	
			nextChargingDate = "TIMESTAMP('2037-12-31')";
		}

		String subscriptionYes = "D";

		if (!sendToHLR && !smDeactivation)
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextChargingDate = "TO_DATE('20361231','yyyyMMdd')";
			else
				nextChargingDate = "TIMESTAMP('2036-12-31')";
			if (smDeactivation)
			{
				subscriptionYes = "N";
			}
		}

		String deactivatedBy = deactivate;
		if (deactivate != null && deactivate.trim().equalsIgnoreCase("DEL"))
		{
			deactivatedBy = "NA";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextChargingDate = "TO_DATE('20361231','yyyyMMdd')";
			else
				nextChargingDate = "TIMESTAMP('2036-12-31')";
		}

		if (smDeactivation)
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
			else
				nextChargingDate = "TIMESTAMP('2037-12-31')";
		}

		String prepaid = "n";
		if (type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		String query = "UPDATE " + TABLE_NAME + " SET " + DEACTIVATED_BY_COL + " = "
		+ sqlString(deactivatedBy) + ", " + END_DATE_COL + " = " + date
		+ " , " + PREPAID_YES_COL + " = '" + prepaid + "',";
		if (smDeactivation)
		{
			query = query + SUBSCRIPTION_YES_COL + " = "
			+ sqlString(subscriptionYes) + ", ";
		}
		query = query + NEXT_CHARGING_DATE_COL + " = " + nextChargingDate
		+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
		+ "' AND SUBSCRIPTION_YES IN ('" + STATE_ACTIVATED + "' ,'" + STATE_EVENT + "')";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}


	static boolean setAccessDate(Connection conn, String subscriberID, Date accessDate)
	{
		try
		{
			return setAccessDate(conn, subscriberID, accessDate, false);
		}
		catch (SQLException se)
		{
			return false;
		}
	}

	public static boolean setAccessDate(Connection conn, String subscriberID,
			Date accessDate, boolean bShouldExceptionBeThrown)
	throws SQLException
	{
		int n = -1;
		Statement stmt = null;
		
		String date = null; 
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE";
			if (accessDate != null)
				date = sqlTime(accessDate);
		}
		else
		{
			date = "SYSDATE()";
			if (accessDate != null)
				date = mySQLDateTime(accessDate);
		}
		String query = "UPDATE " + TABLE_NAME + " SET " + LAST_ACCESS_DATE_COL + " = " + date 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se)
		{
			logger.error("", se);
			if (bShouldExceptionBeThrown)
			{
				throw se;
			}
			else
			{
				return false;
			}
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n == 1);
	}

	static Subscriber getSubscriber(Connection conn, String subID)
	{
		Statement stmt = null;
		ResultSet results = null;
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'";

		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.first())
			{
				return getSubscriberFromRS(results);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return null;
	}
	
	static Subscriber getSubscriber(Connection conn, String subID,
			String protocolNo) {
		Statement stmt = null;
		ResultSet results = null;
		String query = null;

		if (null != subID && null != protocolNo) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
					+ ACTIVATION_INFO_COL + " LIKE '%protocolnumber:"
					+ protocolNo + "%'";
		} else if (null != subID) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'";
		} else if (null != protocolNo) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ ACTIVATION_INFO_COL + " LIKE '%protocolnumber:"
					+ protocolNo + "%'";
		}
		logger.info("Executing the query: " + query);

		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.first()) {
				logger.info("Returning subscriber: " + results + ", subID: "
						+ subID);
				return getSubscriberFromRS(results);
			}
		} catch (SQLException se) {
			logger.error("", se);
			return null;
		} finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Returning null, subID: " + subID);
		return null;
	}

	private static Subscriber[] convertSubscriberListToArray(List<Subscriber> subscriberList) {
		if (subscriberList.size() > 0) {
			return (Subscriber[]) subscriberList.toArray(new Subscriber[0]);
		} else {
			return null;
		}
	}
	
	static String isSubscriberActivated(Connection conn, String subscriberID)
	{
		Subscriber subscriber = getSubscriber(conn, subscriberID);
		if(subscriber != null 
				&& subscriber.endDate().after(new Date())
				&& subscriber.nextChargingDate() != null)
		{
			logger.info("Subscriber " + subscriberID + " is active");
			return "true";
		}
		logger.info("Subscriber " + subscriberID + " is inactive");
		return "false";
	}

	static boolean isSubscriberDeActivated(Connection conn, String subscriberID)
	{
		Subscriber subscriber = getSubscriber(conn, subscriberID);
		if(subscriber != null
				&& subscriber.endDate().before(new Date()))
		{
			logger.info("Subscriber " + subscriberID + " is inactive");
			return true;
		}
		logger.info("Subscriber " + subscriberID + " is new or active");
		return false;
	}

	static Subscriber[] getSubscribersforDeactivation(Connection conn,
			int thresholdPeriod, String classType)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Subscriber subscriber = null;
		List<Subscriber> subscriberList = new ArrayList<Subscriber>();

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ LAST_ACCESS_DATE_COL + " <= (SYSDATE  - " + thresholdPeriod
				+ ") AND " + LAST_ACCESS_DATE_COL + " >= (SYSDATE  - "
				+ (thresholdPeriod + 2) + ") AND " + END_DATE_COL
				+ " > SYSDATE AND " + SUBSCRIPTION_CLASS_COL + " = '"
				+ classType + "'";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
			+ LAST_ACCESS_DATE_COL + " <= TIMESTAMPADD(DAY,-" + thresholdPeriod
			+ ",SYSDATE()) AND " + LAST_ACCESS_DATE_COL + " >= TIMESTAMPADD(DAY,-"
			+ (thresholdPeriod + 2) + ",SYSDATE()) AND " + END_DATE_COL
			+ " > SYSDATE() AND " + SUBSCRIPTION_CLASS_COL + " = '"
			+ classType + "'";

		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriber = getSubscriberFromRS(results);
				subscriberList.add(subscriber);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		return convertSubscriberListToArray(subscriberList); 
	}

	static boolean reactivateSubscriber(Connection conn, String subscriberID)
	{
		String query = null;
		String subscription = "B";

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " 
			+ DEACTIVATED_BY_COL + " = null, " 
			+ END_DATE_COL + " = TO_DATE('20371231','YYYYMMDD'), " 
			+ LAST_ACCESS_DATE_COL + " = SYSDATE, " 
			+ NEXT_CHARGING_DATE_COL + " = SYSDATE , " 
			+ SUBSCRIPTION_YES_COL + " = '" + subscription + "'" 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		else
			query = "UPDATE " + TABLE_NAME + " SET " 
				+ DEACTIVATED_BY_COL + " = null, " 
				+ END_DATE_COL + " = TIMESTAMP('2037-12-31'), " 
				+ LAST_ACCESS_DATE_COL + " = SYSDATE(), " 
				+ NEXT_CHARGING_DATE_COL + " = SYSDATE() , " 
				+ SUBSCRIPTION_YES_COL + " = '" + subscription + "'" 
				+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query); 
		return (n == 1);
	}
	
	static Subscriber[] getSubscribersForDeactivationAlert(Connection conn,
			Date lastTime1, Date lastTime2, String classType)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		String lastTime1String=null;
		String lastTime2String=null;
		Subscriber subscriber = null;
		List<Subscriber> subscriberList = new ArrayList<Subscriber>();

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			if(lastTime1!=null)
				lastTime1String=sqlTime(lastTime1);
			if(lastTime2!=null)
				lastTime2String=sqlTime(lastTime2);
			
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ LAST_ACCESS_DATE_COL + " <= " + lastTime1String + " AND "
				+ LAST_ACCESS_DATE_COL + " >= " + lastTime2String + " AND "
				+ END_DATE_COL + " > SYSDATE AND " + DEACTIVATED_BY_COL
				+ " IS NULL AND " + SUBSCRIPTION_CLASS_COL + " ='" + classType + "'";
		}
		else
		{
			if(lastTime1!=null)
				lastTime1String=mySQLDateTime(lastTime1);
			if(lastTime2!=null)
				lastTime2String=mySQLDateTime(lastTime2);
		
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
			+ LAST_ACCESS_DATE_COL + " <= " + lastTime1String + " AND "
			+ LAST_ACCESS_DATE_COL + " >= " + lastTime2String + " AND "
			+ END_DATE_COL + " > SYSDATE() AND " + DEACTIVATED_BY_COL
			+ " IS NULL AND " + SUBSCRIPTION_CLASS_COL + " ='" + classType
			+ "'";
		}
		
		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriber = getSubscriberFromRS(results);
				subscriberList.add(subscriber);
			}
			if (subscriberList.size() > 0)
			{
				if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
					query = "UPDATE  " + TABLE_NAME + " SET " + DEACTIVATED_BY_COL
					+ " ='AUX' WHERE " + LAST_ACCESS_DATE_COL + " <= "
					+ lastTime1 + " AND " + LAST_ACCESS_DATE_COL + " >= "
					+ lastTime2 + " AND " + END_DATE_COL
					+ " > SYSDATE AND " + DEACTIVATED_BY_COL + " IS NULL";
				else
					query = "UPDATE  " + TABLE_NAME + " SET " + DEACTIVATED_BY_COL
					+ " ='AUX' WHERE " + LAST_ACCESS_DATE_COL + " <= "
					+ lastTime1 + " AND " + LAST_ACCESS_DATE_COL + " >= "
					+ lastTime2 + " AND " + END_DATE_COL
					+ " > SYSDATE() AND " + DEACTIVATED_BY_COL + " IS NULL";
		
				logger.info("RBT::updating alert subscribers " + query);
				RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
				int count = stmt.executeUpdate(query);
				logger.info("RBT::updated alert subscribers = " + count);

				if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
					query = "UPDATE  " + TABLE_NAME + " SET " + DEACTIVATED_BY_COL
						+ " = NULL WHERE " + LAST_ACCESS_DATE_COL + " > "
						+ lastTime1 + " AND " + LAST_ACCESS_DATE_COL + " >= "
						+ lastTime2 + " AND " + END_DATE_COL
						+ " > SYSDATE AND " + DEACTIVATED_BY_COL + " = 'AUX'";
				else
					query = "UPDATE  " + TABLE_NAME + " SET " + DEACTIVATED_BY_COL
					+ " = NULL WHERE " + LAST_ACCESS_DATE_COL + " > "
					+ lastTime1 + " AND " + LAST_ACCESS_DATE_COL + " >= "
					+ lastTime2 + " AND " + END_DATE_COL
					+ " > SYSDATE() AND " + DEACTIVATED_BY_COL + " = 'AUX'";
				logger.info("RBT::updating alert subscribers who have accessed service " + query);
				RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
				int count2 = stmt.executeUpdate(query);
				logger.info("RBT::updating alert subscribers who have accessed service = " + count2);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		return convertSubscriberListToArray(subscriberList); 
	}

	static boolean remove(Connection conn, String subscriberID)
	{
		String query = "DELETE FROM " + TABLE_NAME 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}

	static boolean setActivationInfo(Connection conn, String subscriberID, String actinfo)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + ACTIVATION_INFO_COL + " = '" + actinfo + "' " 
			+ "WHERE " + SUBSCRIBER_ID_COL + " ='" + subscriberID + "'";
		
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}

	static boolean setDeActivationInfo(Connection conn, String subscriberID, String deactinfo)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + LAST_DEACTIVATION_INFO_COL + " = '" + deactinfo + "' " 
			+ "WHERE " + SUBSCRIBER_ID_COL + " ='" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}

	static int removeOldSubscribers(Connection conn, float duration, boolean useSM)
	{
		int count = 0;
		String query = null;
		Statement stmt = null;
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIPTION_YES_COL + " = '" + STATE_DEACTIVATED
					+ "' AND " + END_DATE_COL
				+ " <= ( now() -" + duration + ")";
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIPTION_YES_COL + " = '" + STATE_DEACTIVATED
					+ "' AND " + END_DATE_COL
			+ " <= TIMESTAMPADD( DAY,-" + duration + ",SYSDATE()) LIMIT 1000";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				int rowCount = 1;
				int runCycle = 0;
				logger.info( "before while loop. ");
				while(rowCount > 0)
				{
					runCycle++;
					logger.info("RBT::runCycle is: "+runCycle + ". Going to run delete query again as rowCount is greater than 0. rowCount: "+rowCount);
					rowCount = stmt.executeUpdate(query);
					count += rowCount;	
					logger.info("After running query. No. of rows deleted is :"+rowCount);
				}
				logger.info("After while loop. As rowCount is :"+rowCount + " and total runCycle is "+runCycle);
			}
			else
			{
				count = stmt.executeUpdate(query);
				//n = stmt.getUpdateCount();
			}
			stmt.close();
			logger.info("RBT::deleted " + count + " old selections");
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return -1;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return count;
	}

	static Subscriber[] getFreeActivatedSubscribers(Connection conn, int days)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Subscriber subscriber = null;
		List<Subscriber> subscriberList = new ArrayList<Subscriber>();

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + START_DATE_COL
				+ " <= SYSDATE AND " + END_DATE_COL + " > SYSDATE AND "
				+ NEXT_CHARGING_DATE_COL + "  >= (SYSDATE - " + days
				+ " ) AND " + NEXT_CHARGING_DATE_COL + "  <= (SYSDATE + "
				+ days + ") AND " + ACTIVATION_INFO_COL + " LIKE 'FA%'";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + START_DATE_COL
			+ " <= SYSDATE() AND " + END_DATE_COL + " > SYSDATE() AND "
			+ NEXT_CHARGING_DATE_COL + "  >= TIMESTAMPADD(DAY,- " + days
			+ ",SYSDATE()) AND " + NEXT_CHARGING_DATE_COL + "  <= TIMESTAMPADD(DAY,"
			+ days + ",SYSDATE()) AND " + ACTIVATION_INFO_COL + " LIKE 'FA%'";

		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriber = getSubscriberFromRS(results);
				subscriberList.add(subscriber);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		return convertSubscriberListToArray(subscriberList); 
	}	

	/* subscription manager changes */
	/* Added extraInfo - TRAI changes on 20/07/2009 */
	/*Changed by Senthilraja from upgrade retry callback.*/
	static boolean smSubscriptionSuccess(Connection conn, String subscriberID,
			Date nextChargingDate, Date activationDate, String type,
			String subClass, boolean isPeriodic, CosDetails cos,
			String finalActInfo, boolean updateEndtime,
			boolean updatePlayStatus, String extraInfo, String upgradingCosID, int validity, String subStatus, String oldSub, int rbtType, String strNextPrismBillingDate)
	{
		String nextDate = null;
		String actDate = null;
		String endDate = null;
		
		
		Date prismNextBillingDate = null;
		if(strNextPrismBillingDate != null) {
			try {
				prismNextBillingDate = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss_a").parse(strNextPrismBillingDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				logger.warn("prism next billing date format is wrong...", e);
			}
		}
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE";
			actDate = "SYSDATE";
			if (nextChargingDate != null)
				nextDate = sqlTime(nextChargingDate);
			if (activationDate != null)
				actDate = sqlTime(activationDate);
			if(validity > 0)
				endDate = "SYSDATE + " + validity; 
			
			if(prismNextBillingDate != null){
				strNextPrismBillingDate = sqlTime(prismNextBillingDate);
			}
		}
		else
		{
			nextDate = "SYSDATE()";
			actDate = "SYSDATE()";
			if (nextChargingDate != null)
				nextDate = mySQLDateTime(nextChargingDate);
			if (activationDate != null)
				actDate = mySQLDateTime(activationDate);
			if(validity > 0)
				endDate = "TIMESTAMPADD(DAY,"+validity+",SYSDATE())";
			
			if(prismNextBillingDate != null){
				strNextPrismBillingDate = mySQLDateTime(prismNextBillingDate);
			}
		}
		
		if (subStatus == null) {
			subStatus = "B";

			if (cos != null && cos.renewalAllowed() && cos.acceptRenewal())
				subStatus = "O";
			if (!isPeriodic)
				subStatus = "O";
		}

		String prepaid = "n";
		if (type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		if (type != null && type.equalsIgnoreCase("h")){
			prepaid = "y";
			if(extraInfo!=null && !extraInfo.contains(HYBRID_SUBSCRIBER_TYPE))
				extraInfo = DBUtility.setXMLAttribute(extraInfo, SUBSCRIBER_TYPE, H);
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
		+ " = " + nextDate + ", " + ACTIVATION_DATE_COL + " = "
		+ actDate + ", " + SUBSCRIPTION_YES_COL + " = "
		+ sqlString(subStatus);
		if(updateEndtime && m_databaseType.equalsIgnoreCase(DB_SAPDB)) 
			query = query + " , " + END_DATE_COL + " = TO_DATE('20370101', 'YYYYMMDD')";
		else if (updateEndtime)
			query = query + " , " + END_DATE_COL + " = TIMESTAMP('2037-01-01')";

		if(updatePlayStatus)
             query = query + " , " + PLAYER_STATUS_COL + " = 'A'";
		if(type != null)	
			query = query + " , " + PREPAID_YES_COL + " = " + sqlString(prepaid) ; 
		if(subClass != null) 
			query = query + " , " + SUBSCRIPTION_CLASS_COL + " = " + sqlString(subClass); 
		if(extraInfo != null) 
			query = query + " , " + EXTRA_INFO + " = " + sqlString(extraInfo); 
		if(finalActInfo != null && finalActInfo.length() > 0)
			query = query + " , " + ACTIVATION_INFO_COL + " = " + sqlString(finalActInfo);
		
		if (upgradingCosID != null)
		{
			query += ", " + COS_ID_COL + " = " + sqlString(upgradingCosID);
					
			String confCosID = RBTParametersUtils.getParamAsString(COMMON,
					"NO_UPDATE_FOR_NUM_MAX_FOR_CONFIGURED_COS", "");
			if(!Arrays.asList(confCosID.split(",")).contains(upgradingCosID)){
				query = query + " , " + NUM_MAX_SELECTIONS_COL + " = 0";
			}
			if(validity > 0)
			{
				query = query + " , " + END_DATE_COL + " = " + endDate;	
			}
		}
		
		if(oldSub != null)
		{
			query = query + " , " + OLD_CLASS_TYPE_COL + " = " + sqlString(oldSub);
		}
		if(rbtType !=  -1) {
			query = query + " , " + RBT_TYPE_COL + " = " + rbtType;
		}
		if(strNextPrismBillingDate != null) {
			query = query + " , " + NEXT_BILLING_DATE_COL + " = " + strNextPrismBillingDate;
		}

		query = query + " , " + RETRY_COUNT_COL + " = NULL, " + NEXT_RETRY_TIME_COL + " = NULL";

		query = query +  " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" 
		+ subscriberID + "' AND " + SUBSCRIPTION_YES_COL
		+ " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING + "' ,'" 
		+ STATE_ACTIVATION_ERROR + "', '" + STATE_CHANGE + "', '" + STATE_UN+ "', '"+STATE_ACTIVATION_GRACE+ "', '"+STATE_SUSPENDED+"', '"+STATE_SUSPENDED_INIT+"')";
		
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		logger.info("Executing the query: " + query + ", rows updated: " + n);
		return (n > 0);
	}

	static boolean smSubscriptionSuspend(Connection conn, String subscriberID,
			String subClass) {
		return smSubscriptionSuspend(conn, subscriberID, subClass, false);
	}
	static boolean smSubscriptionSuspend(Connection conn, String subscriberID,
			String subClass, boolean isSuspendInit) {
		String nextDate = "SYSDATE()";
		String actDate = "SYSDATE()";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			nextDate = "SYSDATE";
			actDate = "SYSDATE";
		}
		String subStatus = "Z";
		if(isSuspendInit) {
			subStatus = "z";
		}

		String query = "UPDATE " + TABLE_NAME + " SET "
				+ NEXT_CHARGING_DATE_COL + " = " + nextDate + ", "
				+ ACTIVATION_DATE_COL + " = " + actDate + ", "
				+ SUBSCRIPTION_YES_COL + " = " + sqlString(subStatus);
		query = query + " , " + SUBSCRIPTION_CLASS_COL + " = "
				+ sqlString(subClass);
		query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
				+ subscriberID + "' AND " + SUBSCRIPTION_YES_COL + " IN ('"
				+ STATE_ACTIVATED + "' ,'" + STATE_TO_BE_ACTIVATED + "' ,'"
				+ STATE_ACTIVATION_PENDING + "' ,'" + STATE_ACTIVATION_ERROR
				+ "', '" + STATE_CHANGE + "', '" + STATE_UN + "', '"
				+ STATE_ACTIVATION_GRACE + "', '" + STATE_SUSPENDED + "', '"
				+ STATE_SUSPENDED_INIT + "')";
		
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		logger.info("Executing the query: " + query + ", updated: " + n
				+ " records");
		return (n > 0);
	}

	// Modified for TRAI changes. Added extraInfo
	static boolean smDeactivationSuccess(Connection conn, String subscriberID,
			String subYes, boolean isMemCachePlayer, String extraInfo)
	{
		String query = null;
		String subStat = STATE_DEACTIVATED;
		if(isMemCachePlayer)
			subStat = STATE_DEACTIVATED_INIT;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			if (subYes != null && subYes.equalsIgnoreCase("O"))
			{
				query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
				+ " = TO_DATE('20371231','YYYYMMDD'), " + END_DATE_COL
				+ " = SYSDATE, " + SUBSCRIPTION_YES_COL + " = '" + subStat + "'," + PLAYER_STATUS_COL + " = 'B'";
				

					query += ", "+EXTRA_INFO_COL +" = "+sqlString(extraInfo)+"";
					
				query += ", " + NEXT_BILLING_DATE_COL + " = " + "SYSDATE";

				
				query += " WHERE "+SUBSCRIBER_ID_COL + " = " + "'"+ subscriberID + "' AND " + SUBSCRIPTION_YES_COL + " = '" + STATE_EVENT + "'";
	
			}
			else
			{
				query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
				+ " = TO_DATE('20371231','YYYYMMDD'), "
				+ SUBSCRIPTION_YES_COL + " = '" + subStat + "'," + PLAYER_STATUS_COL + " = 'B'";
				

					query += ", "+EXTRA_INFO_COL +" = "+sqlString(extraInfo)+"";
					
					query += ", " + NEXT_BILLING_DATE_COL + " = " + "SYSDATE()";
				
				query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
			}
		}
		else
		{
			if (subYes != null && subYes.equalsIgnoreCase("O"))
			{
				query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
				+ " = TIMESTAMP('2037-12-31'), " + END_DATE_COL
				+ " = SYSDATE(), " + SUBSCRIPTION_YES_COL + " = '" + subStat + "'," + PLAYER_STATUS_COL + " = 'B'";
				

					query += ", "+EXTRA_INFO_COL +" = "+sqlString(extraInfo)+"";
				
				query += " WHERE "+SUBSCRIBER_ID_COL + " = " + "'"+ subscriberID + "' AND " + SUBSCRIPTION_YES_COL + " = '" + STATE_EVENT + "'";
	
			}
			else
			{
				query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
				+ " = TIMESTAMP('2037-12-31'), "
				+ SUBSCRIPTION_YES_COL + " = '" + subStat + "'," + PLAYER_STATUS_COL + " = 'B'";
				

					query += ", "+EXTRA_INFO_COL +" = "+sqlString(extraInfo)+"";
				
				query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
			}
			
		}	
		
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		logger.info("Result in executing query: " + n + " Rows updated");
		logger.info("Subscriber ID " + subscriberID + " is updated with "+extraInfo+" as extranInfo");
		return (n > 0);
	}

	static boolean smRenewalSuccess(Connection conn, String subscriberID,
			Date nextChargingDate, String type, String classType, String actInfo, String extraInfo, String strNextPrismBillingDate, boolean playstatusA)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;
		String nextDate = null;
		
		
		Date prismNextBillingDate = null;
		if(strNextPrismBillingDate != null) {
			try {
				prismNextBillingDate = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss_a").parse(strNextPrismBillingDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				logger.warn("prism next billing date format is wrong...", e);
			}
		}
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE";
			if (nextChargingDate != null)
				nextDate = sqlTime(nextChargingDate);
			
			if(prismNextBillingDate != null) {
				strNextPrismBillingDate = sqlTime(prismNextBillingDate);
			}
		}
		else
		{
			nextDate = "SYSDATE()";
			if (nextChargingDate != null)
				nextDate = mySQLDateTime(nextChargingDate);
			
			if(prismNextBillingDate != null) {
				strNextPrismBillingDate = mySQLDateTime(prismNextBillingDate);
			}
		}
		
		String prepaid = "n";
		if (type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		if (type != null && type.equalsIgnoreCase("h")){
			prepaid = "y";
			if(extraInfo!=null && !extraInfo.contains(HYBRID_SUBSCRIBER_TYPE))
				extraInfo = DBUtility.setXMLAttribute(extraInfo, SUBSCRIBER_TYPE, H);
		}
		query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
		+ " = " + nextDate + ", " + PREPAID_YES_COL + " = '" + prepaid
		+ "', " + SUBSCRIPTION_CLASS_COL + " = '" + classType + "' "
		+ ", "+EXTRA_INFO_COL + " ="+sqlString(extraInfo);
		
		// RBT-18802
		if (playstatusA) {
			logger.info(":---> Playstatus A true");
			query = query + " , " + PLAYER_STATUS_COL + " = " + sqlString(STATE_TO_BE_ACTIVATED);
		}
				
				
		if(actInfo != null)
			query = query + ", "+ACTIVATION_INFO_COL + " = '"+actInfo+"' ";
		
		if(strNextPrismBillingDate != null) {
			query = query + ", " + NEXT_BILLING_DATE_COL + " = " + strNextPrismBillingDate;
		}
		query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
		+ "' AND " + SUBSCRIPTION_YES_COL + " IN  ('B','D','P','F','E','G')";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();

			if(n == 0)
			{
				query = "UPDATE " + TABLE_NAME + " SET "
						+ NEXT_CHARGING_DATE_COL + " = " + nextDate + ", "
						+ PREPAID_YES_COL + " = '" + prepaid + "', "
						+ SUBSCRIPTION_CLASS_COL + " = '" + classType + "' ," 
						+ SUBSCRIPTION_YES_COL + " = '" + STATE_ACTIVATED + "', " 
						+ PLAYER_STATUS_COL + " = " + sqlString(STATE_TO_BE_ACTIVATED);
				if(actInfo != null) 
                    query = query + ", "+ACTIVATION_INFO_COL + " = '"+actInfo+"' "; 
				query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID 
						+ "' AND " + SUBSCRIPTION_YES_COL + " IN  ('Z','z')";

				logger.info("Executing the query: " + query);
				RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
				n = stmt.getUpdateCount();
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}

	static boolean grameenSmRenewalSuccess(Connection conn, String subscriberID,
			Date nextChargingDate, String type, String classType, String actInfo, String extraInfo, boolean isUserVoluntarySuspended,boolean playstatusA)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;
		String nextDate = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE";
			if (nextChargingDate != null)
				nextDate = sqlTime(nextChargingDate);
		}
		else
		{
			nextDate = "SYSDATE()";
			if (nextChargingDate != null)
				nextDate = mySQLDateTime(nextChargingDate);
		}
		
		String prepaid = "n";
		if (type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		if (type != null && type.equalsIgnoreCase("h")){
			prepaid = "y";
			if(extraInfo!=null && !extraInfo.contains(HYBRID_SUBSCRIBER_TYPE))
				extraInfo = DBUtility.setXMLAttribute(extraInfo, SUBSCRIBER_TYPE, H);
		}
		
		query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
		+ " = " + nextDate + ", " + PREPAID_YES_COL + " = '" + prepaid
		+ "', " + SUBSCRIPTION_CLASS_COL + " = '" + classType + "' "
		+ ", "+EXTRA_INFO_COL + " ="+sqlString(extraInfo);
		
		//v
		//RBT-18802
		if(playstatusA)
		{
			logger.info(":---> Playstatus A true");
			query = query+ " , " + PLAYER_STATUS_COL + " = " +  sqlString(STATE_TO_BE_ACTIVATED);
		}
		if(actInfo != null)
			query = query + ", "+ACTIVATION_INFO_COL + " = '"+actInfo+"' ";
		query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
		+ "' AND " + SUBSCRIPTION_YES_COL + " IN  ('B','D','P','F','E','G')";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();

			if(n == 0)
			{
				query = "UPDATE " + TABLE_NAME + " SET "
						+ NEXT_CHARGING_DATE_COL + " = " + nextDate + ", "
						+ PREPAID_YES_COL + " = '" + prepaid + "', "
						+ SUBSCRIPTION_CLASS_COL + " = '" + classType + "' ," 
				        + EXTRA_INFO_COL + " ="+sqlString(extraInfo)+ ",";
				if(!isUserVoluntarySuspended){
					query += SUBSCRIPTION_YES_COL + " = '" + STATE_ACTIVATED + "', ";				
					query += PLAYER_STATUS_COL + " = " + sqlString(STATE_TO_BE_ACTIVATED) + "," ;
				}
				if(actInfo != null) 
                    query = query + ACTIVATION_INFO_COL + " = '"+actInfo+"' "; 
				query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID 
						+ "' AND " + SUBSCRIPTION_YES_COL + " IN  ('Z','z')";

				logger.info("Executing the query: " + query);
				RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
				n = stmt.getUpdateCount();
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}

	
	static boolean smRenewalSuccessUpgradePending(Connection conn, String subscriberID,
			Date nextChargingDate, String type, String classType, String actInfo, String extraInfo)
	{
		String nextDate = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE";
			if (nextChargingDate != null)
				nextDate = sqlTime(nextChargingDate);
		}
		else
		{
			nextDate = "SYSDATE()";
			if (nextChargingDate != null)
				nextDate = mySQLDateTime(nextChargingDate);
		}

		String prepaid = "n";
		if (type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		if (type != null && type.equalsIgnoreCase("h")){
			prepaid = "y";
			if(extraInfo!=null && !extraInfo.contains(HYBRID_SUBSCRIBER_TYPE))
				extraInfo = DBUtility.setXMLAttribute(extraInfo, SUBSCRIBER_TYPE, H);
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL
				+ " = " + nextDate + ", " + PREPAID_YES_COL + " = '" + prepaid
				+ "', " + OLD_CLASS_TYPE_COL + " = '" + classType
				+ "', "  + EXTRA_INFO_COL + " =" + sqlString(extraInfo) 
				+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
				+ "' AND " + SUBSCRIPTION_YES_COL + " in ('C', 'N', 'E') AND " + OLD_CLASS_TYPE_COL + " IS NOT NULL ";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}

	static boolean smRenewalSuccessActivateUser(Connection conn, String subscriberID,Date nextChargingDate, String type, String classType, String extraInfo, String strNextBillingDate)
	{
		String nextDate = null;
		String activationDate = null;
		Date prismNextBillingDate = null;
		if(strNextBillingDate != null) {
			try {
				prismNextBillingDate = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss_a").parse(strNextBillingDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				logger.warn("prism next billing date format is wrong...", e);
			}
		}
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE";
			activationDate = "SYSDATE";
			if (nextChargingDate != null)
				nextDate = sqlTime(nextChargingDate);
			
			if(prismNextBillingDate != null) {
				strNextBillingDate = sqlTime(prismNextBillingDate);
			}
		}
		else
		{
			nextDate = "SYSDATE()";
			activationDate = "SYSDATE()";
			if (nextChargingDate != null)
				nextDate = mySQLDateTime(nextChargingDate);
			
			if(prismNextBillingDate != null) {
				strNextBillingDate = mySQLDateTime(prismNextBillingDate);
			}
		}

		String prepaid = "n";
		if (type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		if (type != null && type.equalsIgnoreCase("h")){
			prepaid = "y";
			if(extraInfo!=null && !extraInfo.contains(HYBRID_SUBSCRIBER_TYPE))
				extraInfo = DBUtility.setXMLAttribute(extraInfo, SUBSCRIBER_TYPE, H);
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " + NEXT_CHARGING_DATE_COL+ " = " + nextDate + ", " 
				+ PREPAID_YES_COL + " = '" + prepaid
				+ "', " + SUBSCRIPTION_YES_COL + " = '" + STATE_ACTIVATED
				+ "', " + SUBSCRIPTION_CLASS_COL + " = '" + classType
				+ "', " + ACTIVATION_DATE_COL + " = " + activationDate 
				+ ", " + EXTRA_INFO + " = " + sqlString(extraInfo);
		
		if(strNextBillingDate != null && !strNextBillingDate.isEmpty())
			query = query +"," + NEXT_BILLING_DATE_COL + " = "+strNextBillingDate ;
				query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
				+ "' AND " + SUBSCRIPTION_YES_COL + " in ('A', 'N', 'E')"; 

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}


	static boolean smSubscriptionGrace(Connection conn, String subscriberID, String subYes) 
	{ 
		String actDate = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			actDate = "SYSDATE";
		else
			actDate = "SYSDATE()";

		String query = "UPDATE " + TABLE_NAME + " SET " + ACTIVATION_DATE_COL + " = " + actDate + ", " + SUBSCRIPTION_YES_COL + " = 'G'" 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " 
			+ SUBSCRIPTION_YES_COL + " IN ('"+STATE_TO_BE_ACTIVATED+"','"+STATE_ACTIVATION_PENDING+"','"+STATE_ACTIVATION_ERROR+"')"; 

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0); 
	} 

	// Added extraInfo - TRAI changes
	static boolean smSubscriptionRenewalFailure(Connection conn,
			String subscriberID, String deactivatedBy, String type,
			String subClass, boolean isMemCachePlayer, String extraInfo, boolean updateSM, String circleId)
	{
		String query = null;
		String nextDate = null;
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "TO_DATE('20371231','yyyyMMdd')";
		}
		else
		{
			nextDate = "TIMESTAMP('2037-12-31')";
		}
		if (deactivatedBy != null
				&& (deactivatedBy.equalsIgnoreCase("NA") || (deactivatedBy.equalsIgnoreCase("NEF"))))
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextDate = "TO_DATE('20351231','yyyyMMdd')";
			else
				nextDate = "TIMESTAMP('2035-12-31')";
		}

		String prepaid = "n";
		if (type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";
		
		if (type != null && type.equalsIgnoreCase("h")){
			prepaid = "y";
			if(extraInfo!=null && !extraInfo.contains(HYBRID_SUBSCRIBER_TYPE))
				extraInfo = DBUtility.setXMLAttribute(extraInfo, SUBSCRIBER_TYPE, H);
		}
		String subStat = STATE_DEACTIVATED;
		if(isMemCachePlayer)
			subStat = STATE_DEACTIVATED_INIT;

		if (updateSM)
			subStat = STATE_TO_BE_DEACTIVATED;

		String endDate = "SYSDATE()";
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			endDate = "SYSDATE";
		//RBT-14497 - Tone Status Check :Reset the retrycount,retrytime
		query = "UPDATE " + TABLE_NAME + " SET " + END_DATE_COL
		+ " = "+ endDate + ", " + NEXT_CHARGING_DATE_COL + " = " + nextDate
		+ ", " + DEACTIVATED_BY_COL + " = " + sqlString(deactivatedBy)
		+ ", " + SUBSCRIPTION_CLASS_COL + " = '" + subClass + "'";
		if (deactivatedBy != null
				&& (deactivatedBy.equalsIgnoreCase("NA") || deactivatedBy
						.equalsIgnoreCase("AF")))
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = query + ", " + ACTIVATION_DATE_COL + " = SYSDATE ";
			else
				query = query + ", " + ACTIVATION_DATE_COL + " = SYSDATE() ";
		}
		if (circleId != null) {
			query = query + ", " + CIRCLE_ID_COL + " = " + sqlString(circleId)+" ";
		}
		query = query + ", " + SUBSCRIPTION_YES_COL + " = '" + subStat + "', "
		+ EXTRA_INFO_COL + " =" + sqlString(extraInfo) + ", "
		+ PREPAID_YES_COL + " = '" + prepaid 
		+ "' , " + RETRY_COUNT_COL + " = NULL, "
		+ NEXT_RETRY_TIME_COL + " = NULL" 
		+ " WHERE "
		+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
		+ SUBSCRIPTION_YES_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
		+ STATE_ACTIVATION_PENDING + "' ,'" + STATE_ACTIVATED + "' ,'" 
		+ STATE_ACTIVATION_ERROR + "','" + STATE_CHANGE + "', '" + STATE_UN + "', '"+STATE_ACTIVATION_GRACE+"', 'D', 'P', 'F','Z','z' )";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	static boolean smPackSubscriptionFailure(Connection conn, String subscriberID, String cosId, int noMaxSelections, String extraInfo){

		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='B', "
				+ COS_ID_COL +"="+sqlString(cosId)+", "
				+ EXTRA_INFO_COL +"="+sqlString(extraInfo)+", "
				+ NUM_MAX_SELECTIONS_COL+"="+noMaxSelections 
				+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "" + sqlString(subscriberID) + " AND " + SUBSCRIPTION_YES_COL 
				+ " IN ('" + STATE_ACTIVATION_PENDING + "' ,'"+ STATE_ACTIVATION_ERROR + "', '" + STATE_CHANGE + "', '" + STATE_UN + "')";
		
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	static boolean smPackSubscriptionRenewalCallback(Connection conn, String subscriberID, String cosId, int noMaxSelections, String extraInfo)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='B', "
				+ COS_ID_COL +"="+sqlString(cosId)+", "
				+ NUM_MAX_SELECTIONS_COL +"="+noMaxSelections+", "
				+ EXTRA_INFO_COL +"="+sqlString(extraInfo)
				+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "" + sqlString(subscriberID) + " AND " + SUBSCRIPTION_YES_COL 
				+ " IN ('" + STATE_ACTIVATION_PENDING + "' ,'"+ STATE_ACTIVATED + "', '" + STATE_CHANGE + "')";
		
		
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	static boolean smPackSubscriptionSuccess(Connection conn, String subscriberID, String extraInfo){
		String query = "UPDATE " + TABLE_NAME + " SET " 
				+ SUBSCRIPTION_YES_COL +" = '"+STATE_ACTIVATED+"', "
				+ EXTRA_INFO_COL + "="+sqlString(extraInfo)+" "
				+ " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
				+ " AND "+SUBSCRIPTION_YES_COL+ " IN ('"+STATE_CHANGE+"', '"+STATE_ACTIVATION_PENDING+"','"+STATE_ACTIVATED+"')";
		
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	static boolean smUnsubscriptionFailure(Connection conn, String subscriberID, String type)
	{
		String prepaid = "n";
		if (type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL
		+ " = '" + STATE_TO_BE_DEACTIVATED + "'" + ", " + PREPAID_YES_COL + " = '" + prepaid + "'"
		+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
		+ "' AND " + SUBSCRIPTION_YES_COL + " IN ('" + STATE_TO_BE_DEACTIVATED + "','" + STATE_DEACTIVATION_PENDING + "','" + STATE_DEACTIVATION_ERROR + "') ";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}

	/* subscription manager daemon */
	static boolean smURLSubscription(Connection conn, String subscriberID,
			boolean isSuccess, boolean isError, String prevDelayDeactSubYes)
	{
		String nextDate = "SYSDATE()";
		String actDate = "SYSDATE()";
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE";
			actDate = "SYSDATE";
		}
		String subStatus = "N";
		if (isError)
			subStatus = "E";
		else if (!isSuccess)
			subStatus = "B";
		if(prevDelayDeactSubYes!=null && prevDelayDeactSubYes.length()>0){
			subStatus = prevDelayDeactSubYes;
		}
		String query = "UPDATE " + TABLE_NAME + " SET ";
		if (!isSuccess && !isError)
		{
			query = query + NEXT_CHARGING_DATE_COL + " = " + nextDate + ", "
			+ ACTIVATION_DATE_COL + " = " + actDate + ", ";
		}
		query = query + SUBSCRIPTION_YES_COL + " = " + sqlString(subStatus)
		+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
		+ "' AND " + SUBSCRIPTION_YES_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
		+ STATE_CHANGE + "', '" + STATE_UN + "' )";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}

	static boolean smURLUnSubscription(Connection conn, String subscriberID,
			boolean success, boolean isError)
	{
		String subStatus = "P";
		if (isError)
			subStatus = "F";
		else if (!success)
			subStatus = "X";

		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " = "
		+ sqlString(subStatus) + " WHERE " + SUBSCRIBER_ID_COL + " = "
		+ "'" + subscriberID + "' AND " + SUBSCRIPTION_YES_COL
		+ " IN ( '" + STATE_TO_BE_DEACTIVATED + "', '" + STATE_Y + "' )";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}

	static Object smGetActivatedSubscribers(Connection conn, int fetchSize, boolean returnList, ArrayList lowModes, boolean getLowPriority)
	{
		Statement stmt = null;
		ResultSet results = null;
		int count = 0;
		int totalCount = 0;

		Subscriber subscriber = null;
		List<Subscriber> subscriberList = new ArrayList<Subscriber>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIPTION_YES_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "','" + STATE_CHANGE + "' ) ";
		
		int seconds = RBTParametersUtils.getParamAsInt(DAEMON,
				"DELAY_IN_SECONDS_TO_PROCESS_BASE", 1);
		boolean addDelay = RBTParametersUtils.getParamAsBoolean(DAEMON,
				"ADD_DELEAY_IN_DEAMON_TO_PROCESS_RECORD", "false");
		if (addDelay) {
			if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query += " AND ADD_SECONDS(" + START_DATE_COL + "," + seconds
						+ ") < SYSDATE";
			else
				query += " AND TIMESTAMPADD(SECOND," + seconds + ","
						+ START_DATE_COL + ") < SYSDATE() ";
		}

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND ROWNUM <="+fetchSize;
		else
			query += " LIMIT "+fetchSize;


		logger.info("Executing the query: " + query);
		try
		{
			if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
				stmt = conn.createStatement();
			else
			{
				stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				//stmt.setFetchSize(fetchSize);
			}

			results = stmt.executeQuery(query);
			while (results.next())
			{
				if (count < fetchSize )
				{	
					subscriber = getSubscriberFromRS(results);
					if(lowModes != null)
					{
						String selBy = subscriber.activatedBy();
						if(getLowPriority && !lowModes.contains(selBy))
							continue;
						if(!getLowPriority && lowModes.contains(selBy))
							continue;
					}
					subscriberList.add(subscriber);
					count++;
				}
				totalCount++;
			}
			if (RBTDaemonManager.isFcapsEnabled)
				CounterStats.getInstance().setBaseActCount(totalCount, new Date());

			SMDaemonPerformanceMonitor.recordDbQueueCount("BaseActivationQueue", totalCount);
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());

		if (subscriberList.size() > 0 && returnList)
		{
			return subscriberList;
		}
		return convertSubscriberListToArray(subscriberList);
	}

	
	static Object smGetDeactivatedSubscribers(Connection conn, int fetchSize, boolean returnList)
	{
		Statement stmt = null;
		RBTResultSet results = null;
		int count = 0;
		int totalCount = 0;
		
		Subscriber subscriber = null;
		List<Subscriber> subscriberList = new ArrayList<Subscriber>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIPTION_YES_COL + " = '" + STATE_TO_BE_DEACTIVATED+"'";

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND ROWNUM <="+fetchSize;
		else
			query += " LIMIT "+fetchSize;

		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next())
			{
				if (count < fetchSize)
				{	
					subscriber = getSubscriberFromRS(results);
					subscriberList.add(subscriber);
					count++;
				}
				totalCount++;
			}
			if (RBTDaemonManager.isFcapsEnabled)
				CounterStats.getInstance().setBaseDctCount(totalCount, new Date());

			SMDaemonPerformanceMonitor.recordDbQueueCount("BaseDeactivationQueue", totalCount);
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());

		if (subscriberList.size() > 0 && returnList)
		{
			return subscriberList;
		}
		return convertSubscriberListToArray(subscriberList);
	}

	static List<Subscriber> smGetSubscriberToDeactivateInPlayer(Connection conn, int fetchSize, boolean bSuspend, String circleID, boolean isRBT2)
	{
		Statement stmt = null;
		RBTResultSet rs = null;
		int count = 0;
		int totalCount = 0;
		String sub = STATE_DEACTIVATED_INIT;
		if(bSuspend)
			sub = STATE_SUSPENDED_INIT;
		
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "+ SUBSCRIPTION_YES_COL + " = '" + sub + "' ";
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND ROWNUM <="+fetchSize;
		else
			query += " LIMIT "+fetchSize;

		logger.info("Executing the query: " + query);
		
		List<Subscriber> subList = new ArrayList<Subscriber>();
		try
		{
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			while(rs.next()) 
			{
			   if (count < fetchSize)
			   {		
				   String no = rs.getString(SUBSCRIBER_ID_COL);
				   String subCircleID = rs.getString(CIRCLE_ID_COL);
				   
				   //RBT-16004 Added for checking rbt 2
				   if((subCircleID != null && subCircleID.equalsIgnoreCase(circleID)) || (isRBT2 && subCircleID != null && subCircleID.startsWith(circleID)))
				   {
					   Subscriber subscriber = getSubscriberFromRS(rs);
					   subList.add(subscriber);
					   count++;
				   }
				   totalCount ++;
			   }
			   // Sets the Counter statistics
			   if (RBTDaemonManager.isFcapsEnabled)
				   CounterStats.getInstance().setPlayerBaseDctCount(count, new Date());

			   SMDaemonPerformanceMonitor.recordDbQueueCount("PlayerBaseDeactivationQueue", totalCount);
			}
		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
        finally{
        	closeStatementAndRS(stmt, rs);
		}
		return subList;
	}

	static Subscriber[] getActiveSubsToSendTNBSms(Connection conn,
			String classType, Date beginDate, Date lastDate, int days, int subscriptionPeriod)
	{
		Statement stmt = null;
		ResultSet results = null;

		Subscriber subscriber = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE ";

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			if (days > 0)
				query += END_DATE_COL + " > " + sqlTime(beginDate) + " AND "
					+ END_DATE_COL + " < " + sqlTime(lastDate) + " AND ";
			query += SUBSCRIPTION_CLASS_COL + " = '" + classType + "'";
			query += " AND " + END_DATE_COL + " > SYSDATE ";
			query += " AND " + ACTIVATION_INFO_COL + " NOT LIKE ('TNB:" + days
			+ "%') AND " + SUBSCRIPTION_CLASS_COL + " ="+sqlString(classType);
		}
		else
		{
			if (days > 0)
				query += END_DATE_COL + " > " + mySQLDateTime(beginDate) + " AND "
					+ END_DATE_COL + " < " + mySQLDateTime(lastDate) + " AND ";
			query += SUBSCRIPTION_CLASS_COL + " = '" + classType + "'";
			query += " AND " + END_DATE_COL + " > SYSDATE() ";
			query += " AND " + ACTIVATION_INFO_COL + " NOT LIKE ('TNB:" + days
			+ "%') AND " + SUBSCRIPTION_CLASS_COL + " = "+sqlString(classType);
		}

		logger.info("Executing the query: " + query);
		ArrayList<Subscriber> subscriberList = new ArrayList<Subscriber>();
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			while (results.next())
			{
				subscriber = getSubscriberFromRS(results);
				subscriberList.add(subscriber);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}

		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		return convertSubscriberListToArray(subscriberList);
	}

	static Subscriber[] getSubsTobeDeactivated(Connection conn, int max)
	{
		Statement stmt = null;
		ResultSet results = null;
		Subscriber subscriber = null;
		int count =0;
		String query = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query  = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " < SYSDATE AND "
				+ SUBSCRIPTION_YES_COL + " = 'B' ";
		else
			query  = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " < SYSDATE() AND "
			+ SUBSCRIPTION_YES_COL + " = 'B' ";
		
		logger.info("Executing the query: " + query);
		ArrayList<Subscriber> subscriberList = new ArrayList<Subscriber>();
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			while (count < max && results.next()){
				subscriber = getSubscriberFromRS(results);
				subscriberList.add(subscriber);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}

		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		return convertSubscriberListToArray(subscriberList);
	}

	static Subscriber[] getActiveTNBSubsToDeactivate(Connection conn, String classType, int subscriptionPeriod)
	{
		Statement stmt = null;
		ResultSet results = null;
		Subscriber subscriber = null;
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE ";
		query += SUBSCRIPTION_CLASS_COL + " = '" + classType + "'";

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND " + END_DATE_COL + " < SYSDATE + 1 ";
		else
			query += " AND " + END_DATE_COL + " < TIMESTAMPADD(DAY,1,SYSDATE()) ";

		query += " AND SUBSCRIPTION_YES = 'B'"; 
			
		logger.info("Executing the query: " + query);
		ArrayList<Subscriber> subscriberList = new ArrayList<Subscriber>();
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			while (results.next())
			{
				subscriber = getSubscriberFromRS(results);
				subscriberList.add(subscriber);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}

		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		return convertSubscriberListToArray(subscriberList);
	}

	static boolean updateTNBSubscribertoNormal(Connection conn, String subId, boolean useSubMngr, int subscriptionPeriod)
	{
		String query = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + END_DATE_COL
				+ " = TO_DATE('20370101', 'YYYYMMDD') WHERE "
				+ SUBSCRIBER_ID_COL + " = '" + subId + "' AND " + NEXT_CHARGING_DATE_COL
				+ " IS NOT NULL AND " + END_DATE_COL
				+ " < SYSDATE + "+subscriptionPeriod;
		else
			query = "UPDATE " + TABLE_NAME + " SET " + END_DATE_COL
			+ " = TIMESTAMP('2037-01-01') WHERE "
			+ SUBSCRIBER_ID_COL + " = '" + subId + "' AND " + NEXT_CHARGING_DATE_COL
			+ " IS NOT NULL AND " + END_DATE_COL
			+ " < TIMESTAMPADD(DAY,"+subscriptionPeriod+",SYSDATE())";

		if(useSubMngr)
			query = query + " AND SUBSCRIPTION_YES IN ('" + STATE_ACTIVATED + "' ,'" + STATE_EVENT + "')";
		else
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = query + " AND " + NEXT_CHARGING_DATE_COL
					+ " < SYSDATE + "+subscriptionPeriod;
			else
				query = query + " AND " + NEXT_CHARGING_DATE_COL
				+ " < TIMESTAMPADD(DAY,"+subscriptionPeriod+",SYSDATE())";
		}
			
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}

	static void setSelectionCount(Connection conn, String subscriberID)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + NUM_MAX_SELECTIONS_COL + " = "
		+ NUM_MAX_SELECTIONS_COL + " + 1 WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
		+ subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return;
	}
	
	static void decrementSelectionCount(Connection conn, String subscriberID)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + NUM_MAX_SELECTIONS_COL + " = "
		+ NUM_MAX_SELECTIONS_COL + " - 1 WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
		+ subscriberID + "' AND "+NUM_MAX_SELECTIONS_COL+ " > 0";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return;
	}

	
	/*ADDED FOR TATA*/
	static ArrayList smGetActivationPendingSubscribers(Connection conn, String prepaidYes, int fetchSize)
	{
		Statement stmt = null;
		ResultSet results = null;
		int count = 0;
		ArrayList<Subscriber> subscriberList = new ArrayList<Subscriber>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIPTION_YES_COL + " = "
				+ sqlString(STATE_ACTIVATION_PENDING) + " AND " + PREPAID_YES_COL
				+ " = " + sqlString(prepaidYes+"") + " ORDER BY " + START_DATE_COL;

		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			stmt.setMaxRows(fetchSize);
			results = stmt.executeQuery(query);
			while (count < fetchSize && results.next())
			{
				subscriberList.add(getSubscriberFromRS(results));
				count++;
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		if(subscriberList.size() > 0)
		{
			return subscriberList;
		} 
		return null;
	}

	/*ADDED FOR TATA*/
	static ArrayList smGetDeactivationPendingSubscribers(Connection conn, String prepaidYes, int fetchSize)
	{
		Statement stmt = null;
		ResultSet results = null;
		int count = 0;
		ArrayList<Subscriber> subscriberList = new ArrayList<Subscriber>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIPTION_YES_COL + " = "
				+ sqlString(STATE_DEACTIVATION_PENDING) + " AND " + PREPAID_YES_COL
				+ " = " + sqlString(prepaidYes+"") 
				+ " ORDER BY " + START_DATE_COL;

		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			stmt.setMaxRows(fetchSize);
			results = stmt.executeQuery(query);
			while (count < fetchSize && results.next())
			{
				subscriberList.add(getSubscriberFromRS(results));
				count++;
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}

		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		if(subscriberList.size() > 0)
		{
			return subscriberList;
		} 
		return null;
	}

	public static Subscriber[] getBulkPromoSubscribers(Connection conn, String bulkPromoId)
	{
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Subscriber> subscriberList = new ArrayList<Subscriber>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ACTIVATED_BY_COL + " = '" +bulkPromoId + "'";

		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next())
			{
				subscriberList.add(getSubscriberFromRS(rs));
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		return convertSubscriberListToArray(subscriberList);
	}

	public static String[] getBulkPromoAvailedSubscribers(Connection conn, String bulkPromoId)
	{
		Statement stmt = null;
		RBTResultSet rs = null;
		String subscriberID = null;
		ArrayList<String> subscriberList = new ArrayList<String>();

		String query = "SELECT SUBSCRIBER_ID FROM " + TABLE_NAME + " WHERE " 
			+ ACTIVATED_BY_COL + " = '" +bulkPromoId + "' AND "+ SUBSCRIPTION_YES_COL +" = '"+ STATE_ACTIVATED +"'";

		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			rs = new RBTResultSet (stmt.executeQuery(query));
			while(rs.next())
			{
				subscriberID = rs.getString(SUBSCRIBER_ID_COL);
				subscriberList.add(subscriberID);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		if(subscriberList.size() > 0)
		{
			return (String[])subscriberList.toArray(new String[0]);
		}
		return null;
	}

	public static boolean updateNumMaxSelections(Connection conn, String subscriberId, int maxSelections)
	{
		String query = "UPDATE "+ TABLE_NAME +" SET NUM_MAX_SELECTIONS = "+ maxSelections +" WHERE SUBSCRIBER_ID = '" + subscriberId + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n >= 0);
	}

	/*ADDED FOR TATA*/
	static boolean updateDeactivationFailed(Connection conn, String subscriberID)
	{
		String endDate = "TO_DATE('20371231','yyyyMMdd')";
		String nextChargingDate = "TO_DATE('20351231','yyyyMMdd')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			endDate = "TIMESTAMP('2037-12-31')";
			nextChargingDate = "TIMESTAMP('2035-12-31')";
		}
		String query = "UPDATE " + TABLE_NAME + " SET " +
		SUBSCRIPTION_YES_COL  + " = " + sqlString(STATE_ACTIVATED) + ", " +
		DEACTIVATED_BY_COL  + " = NULL, " +
		END_DATE_COL + " = " + endDate + " , " + 
		NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + 
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return(n==1);
	}

	static ArrayList getUpdateToDeactivateSubscribers(Connection conn, int fetchSize)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		ArrayList<Subscriber> subscriberList = new ArrayList<Subscriber>();

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIPTION_YES_COL + " IN ("
				+ sqlString(STATE_EVENT) + ", "
				+ sqlString(STATE_ACTIVATED) + ") AND " + END_DATE_COL +
				" < SYSDATE ORDER BY " + END_DATE_COL;
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIPTION_YES_COL + " IN ("
			+ sqlString(STATE_EVENT) + ", "
			+ sqlString(STATE_ACTIVATED) + ") AND " + END_DATE_COL +
			" < SYSDATE() ORDER BY " + END_DATE_COL;

		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			stmt.setMaxRows(fetchSize);
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberList.add(getSubscriberFromRS(results));
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		if(subscriberList.size() > 0)
		{
			return subscriberList;
		} 
		return null;
	}
	 static Subscriber[] getSubsTosendSMS(Connection conn, String subClass, int smsDay, int fetchSize) {
          Calendar startCal = Calendar.getInstance();
          startCal.add(Calendar.DATE, 1-smsDay);
          startCal.set(Calendar.HOUR_OF_DAY, 0);
          startCal.set(Calendar.MINUTE, 0);
          startCal.set(Calendar.SECOND, 0);

          Calendar endCal = Calendar.getInstance();
          endCal.add(Calendar.DATE, 1-smsDay);
          endCal.set(Calendar.HOUR_OF_DAY, 23);
          endCal.set(Calendar.MINUTE, 59);
          endCal.set(Calendar.SECOND, 59);
          String query = null;
          if(m_databaseType.equalsIgnoreCase(DB_SAPDB)){
        	  query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIPTION_CLASS_COL + " = "
                          + sqlString(subClass) + " AND " + SUBSCRIPTION_YES_COL + " = "
                          + sqlString(STATE_ACTIVATED) + " AND " + START_DATE_COL + " >= "
                          + sqlTime(startCal.getTime()) + " AND " + START_DATE_COL + " <= "
                          + sqlTime(endCal.getTime()) + " AND " + ACTIVATION_INFO_COL + " NOT LIKE "
                          + sqlString(subClass + ":" + smsDay + "%");
          }else{
        	  query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIPTION_CLASS_COL + " = "
              + sqlString(subClass) + " AND " + SUBSCRIPTION_YES_COL + " = "
              + sqlString(STATE_ACTIVATED) + " AND " + START_DATE_COL + " >= "
              + mySQLDateTime(startCal.getTime()) + " AND " + START_DATE_COL + " <= "
              + mySQLDateTime(endCal.getTime()) + " AND " + ACTIVATION_INFO_COL + " NOT LIKE "
              + sqlString(subClass + ":" + smsDay + "%");
          }

          logger.info("Executing the query: " + query);
          ArrayList<Subscriber> subsList = new ArrayList<Subscriber>();
          Statement stmt = null;
          ResultSet rs = null;

          try {
                  stmt = conn.createStatement();
                  stmt.setMaxRows(fetchSize);
                  rs = stmt.executeQuery(query);
                  while(rs.next())
                          subsList.add(getSubscriberFromRS(rs));
          }
          catch (SQLException e) {
                  logger.error("", e);
          }
          finally {
        	  closeStatementAndRS(stmt, rs);
          }

  		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subsList.size());
		return convertSubscriberListToArray(subsList);
	 }

	private static Subscriber getSubscriberFromRS(ResultSet rs) throws SQLException
	{
		RBTResultSet results = new RBTResultSet(rs);

		String subscriberID = results.getString(SUBSCRIBER_ID_COL);
		String activate = results.getString(ACTIVATED_BY_COL);
		String deactivate = results.getString(DEACTIVATED_BY_COL);
		Date startDate = results.getTimestamp(START_DATE_COL);
		Date endDate = results.getTimestamp(END_DATE_COL);
		String prepaid = results.getString(PREPAID_YES_COL);
		Date accessDate = results.getTimestamp(LAST_ACCESS_DATE_COL);
		Date nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
		int access = results.getInt(NUM_VOICE_ACCESS_COL);
		String info = results.getString(ACTIVATION_INFO_COL);
		String subscriptionClass = results.getString(SUBSCRIPTION_CLASS_COL);
		String subscription = results.getString(SUBSCRIPTION_YES_COL);
		String lastDeactivationInfo = results.getString(LAST_DEACTIVATION_INFO_COL);
		Date lastDeactivationDate = results.getTimestamp(LAST_DEACTIVATION_DATE_COL);
		Date activationDate = results.getTimestamp(ACTIVATION_DATE_COL);
		int maxSelections = results.getInt(NUM_MAX_SELECTIONS_COL);
		String cosID = results.getString(COS_ID_COL);
		String activatedCosID = results.getString(ACTIVATED_COS_ID_COL);
		int rbtType = results.getInt(RBT_TYPE_COL);
		String language = results.getString(LANGUAGE_COL);
		String strOldClassType = results.getString(OLD_CLASS_TYPE_COL);
		String extraInfo = results.getString(EXTRA_INFO_COL);
		String circleID = results.getString(CIRCLE_ID_COL);
		String refID = results.getString(INTERNAL_REF_ID_COL);
		String retryCount = results.getString(RETRY_COUNT_COL);
		Date nextRetryTime = results.getTimestamp(NEXT_RETRY_TIME_COL);
		//New Column added for prism billing date
		Date nextBillingDate = results.getTimestamp(NEXT_BILLING_DATE_COL);
		
		SubscriberImpl subscriber = new SubscriberImpl(subscriberID, activate,
				deactivate, startDate, endDate, prepaid, accessDate,
				nextChargingDate, access, info, subscriptionClass,
				subscription, lastDeactivationInfo,
				lastDeactivationDate, activationDate, maxSelections, cosID, activatedCosID, rbtType,
				language,strOldClassType, extraInfo, circleID, refID, retryCount, nextRetryTime);
		subscriber.setPrismNextBillingDate(nextBillingDate);
		return subscriber; 
	}
	/**
	 * This API coverts back an SUBSCRIBER as B if users state was C and SM call back failed.
	 * @param conn
	 * @param subscriberID
	 * @param oldActBy TODO
	 * @return
	 */
	static boolean updateUpgradeFailure(Connection conn, String subscriberID, String actBy, String oldSub, int rbtType, boolean updateType, String newStatus, String extraInfo, String oldActBy){
		if(newStatus == null)
		{
			newStatus = "B";
		}

		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='"+newStatus+"', "
				+ ACTIVATION_DATE_COL + " = " + START_DATE_COL + ", " + SUBSCRIPTION_CLASS_COL
				+ " = " + OLD_CLASS_TYPE_COL + " , " + OLD_CLASS_TYPE_COL + " = NULL ";
		if(updateType) 
			query += "," + RBT_TYPE_COL + " = " + rbtType; 
		if(extraInfo != null)
		{
			if (extraInfo.equalsIgnoreCase("NULL"))
				query += "," + EXTRA_INFO_COL + " = NULL";
			else
				query += "," + EXTRA_INFO_COL + " = '" + extraInfo + "'";
		}

		query = query + " , " + RETRY_COUNT_COL + " = NULL, " + NEXT_RETRY_TIME_COL + " = NULL";
        
		if(oldActBy!=null)
        	query = query + " , " + ACTIVATED_BY_COL + " = " + sqlString(oldActBy);
        
		query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + SUBSCRIPTION_YES_COL 
			+ " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING + "' ,'"
			+ STATE_ACTIVATION_ERROR + "', '" + STATE_CHANGE + "', '" + STATE_UN + "','"+STATE_SUSPENDED+"','"+STATE_SUSPENDED_INIT+"')";
		
		if(actBy != null && actBy.equals("TNB") && oldSub != null && oldSub.equals("ZERO"))
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB)){
				query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='B', "
					+ ACTIVATION_DATE_COL + " = " + START_DATE_COL + ", " + SUBSCRIPTION_CLASS_COL
					//+ " = " + OLD_CLASS_TYPE_COL + " , " + OLD_CLASS_TYPE_COL + " = NULL, " + END_DATE_COL + " = " + START_DATE_COL + " + 29 WHERE "
					+ " = " + OLD_CLASS_TYPE_COL + " , " + OLD_CLASS_TYPE_COL + " = NULL, " + END_DATE_COL + " = " + START_DATE_COL + " + 29 "; 
                
				if(updateType) 
                	 query += "," + RBT_TYPE_COL + " = " + rbtType; 
				if(oldActBy!=null)
				    query = query + " , " + ACTIVATED_BY_COL + " = " + sqlString(oldActBy);
				
				query = query + " , " + RETRY_COUNT_COL + " = NULL, " + NEXT_RETRY_TIME_COL + " = NULL";
                
                query += " WHERE " 
					+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + SUBSCRIPTION_YES_COL
					+ " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING + "' ,'"
					+ STATE_ACTIVATION_ERROR + "', '" + STATE_CHANGE + "', '" + STATE_UN + "')" ;
			}
            else{
				query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='B', "
				+ ACTIVATION_DATE_COL + " = " + START_DATE_COL + ", " + SUBSCRIPTION_CLASS_COL
				//+ " = " + OLD_CLASS_TYPE_COL + " , " + OLD_CLASS_TYPE_COL + " = NULL, " + END_DATE_COL + " = TIMESTAMPADD(DAY,29," + START_DATE_COL + ") WHERE "
				+ " = " + OLD_CLASS_TYPE_COL + " , " + OLD_CLASS_TYPE_COL + " = NULL, " + END_DATE_COL + " = TIMESTAMPADD(DAY,29," + START_DATE_COL + ") " ; 
                
				if(updateType) 
                        query += "," + RBT_TYPE_COL + " = " + rbtType; 
				if(oldActBy!=null)
				    query = query + " , " + ACTIVATED_BY_COL + " = " + sqlString(oldActBy);
				
				query = query + " , " + RETRY_COUNT_COL + " = NULL, " + NEXT_RETRY_TIME_COL + " = NULL";
                
				query += " WHERE " 
				+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + SUBSCRIPTION_YES_COL
				+ " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING + "' ,'"
				+ STATE_ACTIVATION_ERROR + "', '" + STATE_CHANGE + "', '" + STATE_UN + "')";
            }
		}
		
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	// Added extraInfo on 05/08/2009
	static boolean processSuspendSubscription(Connection conn, String subscriberID, String extraInfo, boolean updateActivationDate){
	    String actDate = SAPDB_SYSDATE;
		if(m_databaseType.equalsIgnoreCase(DB_MYSQL)){
			actDate = MYSQL_SYSDATE;
		}

		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='z' " ;
				
		if(updateActivationDate)
			query += ", "+ACTIVATION_DATE_COL +" = "+actDate;
		
		query += ", "+EXTRA_INFO_COL+" ="+sqlString(extraInfo)	
		  	  +" WHERE "+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'" 
			  +" AND " + SUBSCRIPTION_YES_COL	+ " IN ('N', 'C', 'B', 'E', 'A', 'G') ";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	/**
	 * @author deepak.kumar
	 * For RBT-4421
	 * For treating RBT-SM suspension differently.
	 */
	static boolean processSmSuspendSubscription(Connection conn, String subscriberID, String extraInfo, boolean updateActivationDate){
	    String actDate = SAPDB_SYSDATE;
		if(m_databaseType.equalsIgnoreCase(DB_MYSQL)){
			actDate = MYSQL_SYSDATE;
		}
		Subscriber subscriber = getSubscriber(conn, subscriberID);
		String subStatus = null;
		if(subscriber.subYes().equals("Z")){
			subStatus = "Z";
		}else{
			subStatus = "z";
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='"+ subStatus +"' " ;
				
		if(updateActivationDate)
			query += ", "+ACTIVATION_DATE_COL +" = "+actDate;
		
		query += ", "+EXTRA_INFO_COL+" ="+sqlString(extraInfo)	
		  	  +" WHERE "+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'" 
			  +" AND " + SUBSCRIPTION_YES_COL	+ " IN ('N', 'C', 'B', 'E', 'A', 'G','Z','z') ";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}

	/**
	 * @author deepak.kumar
	 * For RBT-4421
	 * For treating RBT-SM suspension differently.
	 */

	static boolean processSmSuspendSubscription(Connection conn, String subscriberID, String extraInfo,String status, boolean updatePlayerStatus){
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " =" + sqlString(status) + ", " ;
		
		if(updatePlayerStatus) {
			query = query + " " + PLAYER_STATUS_COL + " = 'A', ";
		}
		
		query += EXTRA_INFO_COL+" ="+sqlString(extraInfo)	
		  	  +" WHERE "+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'" 
			  +" AND " + SUBSCRIPTION_YES_COL	+ " IN ('N', 'C', 'B', 'E', 'A', 'G','Z','z') ";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}

	// Added for Idea voluntary suspension on 05/08/2009
	static boolean processResumeSubscription(Connection conn, String subscriberID, String subStatus, String extraInfo, Date ncd) {
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + "=" + sqlString(subStatus) + ", " + PLAYER_STATUS_COL
				+ "='A'" + ", " + EXTRA_INFO_COL + " =" + sqlString(extraInfo);

		if(ncd != null) {
			query += ", " + NEXT_CHARGING_DATE_COL + " = ";
			if(m_databaseType.equals(DB_SAPDB)) {
				query += sqlTime(ncd);
			}
			else {
				query += mySqlTime(ncd);
			}
		}
		
		query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	static boolean updateSubUpdatedAtPlayer(Connection conn, String subID) { 
		String query = null;
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "UPDATE " + TABLE_NAME + " SET " + PLAYER_STATUS_COL + " = 'B' WHERE " 
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subID);// + " AND " + END_DATE_COL 
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "UPDATE " + TABLE_NAME + " SET " + PLAYER_STATUS_COL + " = 'B' WHERE " 
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subID);// + " AND " + END_DATE_COL 
		}
    
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
   		int n = executeUpdateQuery(conn, query);
           return n > 0; 
	} 
    
	static List<Subscriber> getSubsToUpdatePlayer(Connection conn, int fetchSize, String circleID ,boolean isRBT2) 
	{ 
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAYER_STATUS_COL + " = 'A' ";

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND ROWNUM <="+fetchSize;
		else
			query += " LIMIT "+fetchSize;

		logger.info("Executing the query: " + query);
		List<Subscriber> subsList = new ArrayList<Subscriber>();

		Statement stmt = null; 
		ResultSet rs = null; 

		int count =0;
		int totalCount = 0;
		try { 
			stmt = conn.createStatement(); 
			rs = new RBTResultSet(stmt.executeQuery(query)); 
			while(rs.next()) 
			{
				if (count < fetchSize)
				{	   
					String sub = rs.getString(SUBSCRIBER_ID_COL);
					String subCircleID = rs.getString(CIRCLE_ID_COL);

					// RBT-16004 Added for checking rbt 2
					if((subCircleID != null && subCircleID.equalsIgnoreCase(circleID)) || (isRBT2 && subCircleID != null && subCircleID.startsWith(circleID)))
					{
						Subscriber subscriber = getSubscriberFromRS(rs);
						subsList.add(subscriber);
						count++;
					}
				}
				totalCount++;
			}
			// Set Counter Statiscts
			if (RBTDaemonManager.isFcapsEnabled)
				CounterStats.getInstance().setPlayerBaseActCount(totalCount, new Date());

			SMDaemonPerformanceMonitor.recordDbQueueCount("PlayerBaseActivationQueue", totalCount);
		} 
		catch(SQLException e) { 
			logger.error("", e); 
		} 
		finally 
		{
			closeStatementAndRS(stmt, rs);
		}
		return subsList;
	}
           
	static boolean updateRBTType(Connection conn, String subID, int rbtType) { 
		String query = "UPDATE " + TABLE_NAME + " SET " + RBT_TYPE_COL + " = " + rbtType + " WHERE " 
			+ SUBSCRIBER_ID_COL + " = " + sqlString(subID); 

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return n > 0; 
	} 

	// RBT-14301: Uninor MNP changes.
	static boolean updateCircleId(Connection conn, String subID,
			String circleId, String refId) {
		String query = "UPDATE " + TABLE_NAME + " SET " + CIRCLE_ID_COL + " = "
				+ sqlString(circleId) + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subID);
		if (refId != null) {
			query += " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refId);
		}

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return n > 0;
	}
           
	static boolean updateRBTTypeAndPlayerStatus(Connection conn, String subID, int rbtType, String playerStatus) { 
		String query = "UPDATE " + TABLE_NAME + " SET " + RBT_TYPE_COL + " = " + rbtType + "," + PLAYER_STATUS_COL + "='" + playerStatus + "' WHERE " 
			+ SUBSCRIBER_ID_COL + " = " + sqlString(subID); 

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return n > 0; 
	} 

	static String[] getOldSubscribers(Connection conn, float duration, boolean useSM){
		String query = null;    
		Statement stmt = null; 
		ResultSet results = null; 
		List<String> subscriberList = new ArrayList<String>(); 

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " <= ( now() -" + duration + ")"; 
			if(useSM) 
				query = query + " AND " + SUBSCRIPTION_YES_COL + " = '" + STATE_DEACTIVATED + "'"; 
		}
		else
		{
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " <= TIMESTAMPADD(DAY,-" + duration + ",SYSDATE())"; 
			if(useSM) 
				query = query + " AND " + SUBSCRIPTION_YES_COL + " = '" + STATE_DEACTIVATED + "'"; 
		}

		logger.info("Executing the query: " + query);
		try 
		{ 
			stmt = conn.createStatement(); 
			results = stmt.executeQuery(query); 
			while (results.next()) 
			{ 
				String sub = new String(results.getString("SUBSCRIBER_ID")); 
				subscriberList.add(sub); 
			} 
		} 
		catch (SQLException se) 
		{ 
			logger.error("", se);
			return null; 
		} 
		finally 
		{
			closeStatementAndRS(stmt, results);
		} 
		
		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subscriberList.size());
		if (subscriberList.size() > 0) 
		{ 
			return (String[]) subscriberList.toArray(new String[0]); 
		} 
		return null; 
	} 
           
	static boolean updateSubscriberToGrace(Connection conn, String subscriberID, Date nextRetryDate, String actInfo) { 
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " = "
			+ sqlString(STATE_GRACE) + ", " + NEXT_CHARGING_DATE_COL + " = "
			+ sqlTime(nextRetryDate) + ", " + ACTIVATION_INFO_COL + " = " + sqlString(actInfo)
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
			+ END_DATE_COL + " > SYSDATE"; 
   
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " = "
			+ sqlString(STATE_GRACE) + ", " + NEXT_CHARGING_DATE_COL + " = "
			+ mySQLDateTime(nextRetryDate) + ", " + ACTIVATION_INFO_COL + " = "
			+ sqlString(actInfo) + " WHERE " + SUBSCRIBER_ID_COL + " = "
			+ sqlString(subscriberID) + " AND " + END_DATE_COL + " > SYSDATE()";
   
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
   		int n = executeUpdateQuery(conn, query);
   		return (n >= 1); 
	} 

	static ArrayList getActivationGraceRecords(Connection conn, int fetchSize) { 
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIPTION_YES_COL + " = " 
			+ sqlString(STATE_GRACE) + " AND " + NEXT_CHARGING_DATE_COL + " <= SYSDATE";
   
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIPTION_YES_COL + " = " 
			+ sqlString(STATE_GRACE) + " AND " + NEXT_CHARGING_DATE_COL + " <= SYSDATE()"; 
	   
		logger.info("Executing the query: " + query);
		Statement stmt = null; 
		ResultSet rs = null; 
		ArrayList<Subscriber> graceSubs = new ArrayList<Subscriber>(); 
		try { 
			stmt = conn.createStatement(); 
			stmt.setMaxRows(fetchSize); 
			rs = stmt.executeQuery(query); 
			while(rs.next()) 
				graceSubs.add(getSubscriberFromRS(rs)); 
		} 
		catch (SQLException e) { 
			logger.error("", e); 
		} 
		finally {
			closeStatementAndRS(stmt, rs);
		}

		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + graceSubs.size());
		if(graceSubs.size() > 0) { 
			return graceSubs; 
		} 
		return null; 
	} 
       
	static boolean updateExtraInfo(Connection conn, String subscriberID, String extraInfo) { 
		String query = "UPDATE " + TABLE_NAME + " SET " + EXTRA_INFO_COL + " = " 
			+ sqlString(extraInfo) + " WHERE " + SUBSCRIBER_ID_COL + " = " 
			+ sqlString(subscriberID);  
   
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
  		int n = executeUpdateQuery(conn, query);
  		return (n >= 1); 
	}

	static boolean updateExtraInfoNStatusNDeactBy(Connection conn, String subscriberID, String extraInfo,String status, String deactBy) { 
		String query = "UPDATE " + TABLE_NAME + " SET " + EXTRA_INFO_COL + " = " 
			+ sqlString(extraInfo)+", "+ SUBSCRIPTION_YES_COL +" = " +sqlString(status);
		if(deactBy != null && deactBy.trim().length() > 0)
			query += ", " + DEACTIVATED_BY_COL + " = " + sqlString(deactBy.trim()); 
		query += " WHERE " + SUBSCRIBER_ID_COL + " = " 
			+ sqlString(subscriberID);  
   
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
  		int n = executeUpdateQuery(conn, query);
  		return (n >= 1); 
	}

	static boolean updateExtraInfoAndPlayerStatus(Connection conn, String subscriberID,String extraInfo,String playerStatus) { 
		String query = "UPDATE " + TABLE_NAME + " SET " + EXTRA_INFO_COL + " = " + sqlString(extraInfo) + "," 
			+ PLAYER_STATUS_COL + " = " + "'" + playerStatus + "'" 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = "	+ sqlString(subscriberID);  
   
			logger.info("Executing the query: " + query);
			RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
     		int n = executeUpdateQuery(conn, query);
     		return (n >= 1); 
	}
       
	static boolean updatePlayerStatus(Connection conn, String subscriberID, String playerStatus) { 
		String query = "UPDATE " + TABLE_NAME + " SET " + PLAYER_STATUS_COL + " = " + "'" + playerStatus + "'" 
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID);  
   
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n >= 1); 
	}
   
	static ArrayList<Subscriber> playerGetActivatedSubs(Connection conn, int fetchSize) {
		String query = "SELECT * FROM RBT_SUBSCRIBER WHERE PLAYER_STATUS = "
				+ sqlString(STATE_TO_BE_ACTIVATED) + " AND " + SUBSCRIPTION_YES_COL + " = "
				+ sqlString(STATE_ACTIVATED);

		logger.info("Executing the query: " + query);
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Subscriber> subsList = new ArrayList<Subscriber>();

		try {
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				subsList.add(getSubscriberFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatementAndRS(stmt, rs);
		}

		logger.info("Retrieved records from RBT_SUBSCRIBER successfully. Total rows: " + subsList.size());
		if (subsList.size() > 0) {
			return subsList;
		}
		return null;
	}

	static boolean updatePlayerStatusAndId(Connection conn, String newSubscriberId, String subscriberId, String playerStatus)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " +
			SUBSCRIBER_ID_COL + " = '" + newSubscriberId + "' , "+
			PLAYER_STATUS_COL + " = '" + playerStatus +
			"' WHERE " + SUBSCRIBER_ID_COL  + " = '" + subscriberId + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n>0);
	}

	static boolean smDeactivationPending(Connection conn, String subscriberID,
			String deactivate, Date endDate, boolean sendToHLR,
			boolean smDeactivation, boolean isNewSubscriber,
			boolean isDirectDeact, boolean isMemCachePlayer, String dctInfo,
			Subscriber subscriber)

	{
		String query = null;
		String date = null;
		String nextChargingDate = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE";
			if (endDate != null)
				date = sqlTime(endDate);
			nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		}
		else
		{
			date = "SYSDATE()";
			if (endDate != null)
				date = mySQLDateTime(endDate);
			nextChargingDate = "TIMESTAMP('2037-12-31')";
		}
		
		String subscriptionYes = STATE_DEACTIVATION_PENDING;
		String deactivatedBy = deactivate;
		if (deactivate != null && deactivate.trim().equalsIgnoreCase("DEL"))
		{
			deactivatedBy = "NA";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextChargingDate = "TO_DATE('20361231','yyyyMMdd')";
			else
				nextChargingDate = "TIMESTAMP('2036-12-31')";
		}

		query = "UPDATE " + TABLE_NAME + " SET " + DEACTIVATED_BY_COL + " = "
		+ sqlString(deactivatedBy) + ", " + END_DATE_COL + " = " + date
		+ " , ";
		if (smDeactivation)
		{
			if(isDirectDeact)
			{
				subscriptionYes = STATE_DEACTIVATED;
				if(isMemCachePlayer)
					subscriptionYes = STATE_DEACTIVATED_INIT;
			}
			query = query + SUBSCRIPTION_YES_COL + " = "
			+ sqlString(subscriptionYes) + ", ";
		}

		if(dctInfo != null)
		{
			dctInfo = trimAndConcatActivationInfo(subscriber, dctInfo, false);
            query = query + ACTIVATION_INFO_COL + " = " + sqlString(dctInfo) + ", ";
		}
		
		query = query + NEXT_CHARGING_DATE_COL + " = " + nextChargingDate
		+ " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
		+ "'";

		if (!isDirectDeact)
		{	
			if(smDeactivation)
			{
				query = query + " AND " + SUBSCRIPTION_YES_COL + " IN ('"
						+ STATE_ACTIVATED + "' ,'" + STATE_EVENT + "' ,'"
						+ STATE_SUSPENDED + "' ,'" + STATE_SUSPENDED_INIT + "','" + STATE_ACTIVATION_GRACE + "')";
			}
			else 
			{
				if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
					query = query + " AND " + END_DATE_COL + " > SYSDATE ";
				else
					query = query + " AND " + END_DATE_COL + " > SYSDATE() ";
			}
		}
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}


	static boolean smConvertSubscriptionType(Connection conn,
			String subscriberID, String initClassType, String finalClassType,
			String strActBy, String strActInfo, int rbtType,
			boolean useRbtType, String extraInfo, Subscriber subscriber)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " ='N', "
				+ ACTIVATION_DATE_COL + " = NULL, " + SUBSCRIPTION_CLASS_COL + " = "
				+ sqlString(finalClassType) + " , " + OLD_CLASS_TYPE_COL + " = "
				+ sqlString(initClassType);
		if(strActBy != null)
			query = query + ", " + ACTIVATED_BY_COL + " = " + sqlString(strActBy);
		if(strActInfo != null)
		{
			strActInfo = trimAndConcatActivationInfo(subscriber, strActInfo, false);
			query = query + ", " + ACTIVATION_INFO_COL + " = " + sqlString(strActInfo);
		}
		
		query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
				+ SUBSCRIPTION_CLASS_COL + " = " + sqlString(initClassType) + " AND "
				+ SUBSCRIPTION_YES_COL + " in  ('B','z','Z')";
		if(useRbtType) 
            query += " AND "+ RBT_TYPE_COL + " = " + rbtType; 

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	static boolean smUpgradeToSongPack(Connection conn, String subscriberID, String cosId, String extraInfo)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIPTION_YES_COL + " = " + sqlString(STATE_ACTIVATION_PENDING) + "," + ACTIVATED_COS_ID_COL + " = " + COS_ID_COL + "," +
		COS_ID_COL + " = " + sqlString(cosId) + "," + NUM_MAX_SELECTIONS_COL + " = 0" + "," + EXTRA_INFO_COL + " = " + sqlString(extraInfo) + 
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + sqlString(subscriberID) + " AND " + SUBSCRIPTION_YES_COL + " IN ('" + STATE_ACTIVATED + "')";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n > 0);
	}
	
	//Added by Sreekar for Reliance ARBT implementation
	static boolean deleteSubscriber(Connection conn, String subscriberId) {
		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId);

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int count = executeUpdateQuery(conn, query);
		return count > 0;
	}
	
	private static String trimAndConcatActivationInfo(Subscriber subscriber, String newActivationInfo, boolean isUpgradeRequest)
	{
		int maxActivationInfoLen = 500;
		int newActivationInfoLen = newActivationInfo.length();

		String requestType = "";
		if (!isUpgradeRequest)
			requestType = "DCT:";

		// newActivationInfoLen should not be more than maxActivationInfoLen,
		// but here we are allowing so that exception will be generated, so that
		// we can identify who is trying to add bigger activationInfo.
		if (subscriber == null || newActivationInfoLen >= maxActivationInfoLen)
			return newActivationInfo;

		String oldActivationInfo = subscriber.activationInfo();
		int oldActivationInfoLen = oldActivationInfo.length();

		if ((oldActivationInfoLen + newActivationInfoLen + 1) <= maxActivationInfoLen)
			return (oldActivationInfo + "|" + requestType + newActivationInfo);

		int requiredSpace = (oldActivationInfoLen + newActivationInfoLen + 1) - maxActivationInfoLen;
		if (requiredSpace >= oldActivationInfoLen)
			return newActivationInfo;

		int trimIndex = oldActivationInfo.indexOf("|" + requestType, requiredSpace -1);
		if (trimIndex == -1)
			trimIndex = requiredSpace - 5;

		newActivationInfo = oldActivationInfo.substring(trimIndex + 1) + "|" + requestType + newActivationInfo;
		return newActivationInfo;
	}

	public static boolean updateRetryCountAndTime(Connection conn, String subscriberID, String retryCount, Date retryTime)
	{
		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			query = "UPDATE " + TABLE_NAME + " SET " + RETRY_COUNT_COL + " = "
					+ sqlString(retryCount) + " , " + NEXT_RETRY_TIME_COL
					+ " = " + sqlTime(retryTime) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		}
		else {
			query = "UPDATE " + TABLE_NAME + " SET " + RETRY_COUNT_COL + " = "
					+ sqlString(retryCount) + " , " + NEXT_RETRY_TIME_COL
					+ " = " + mySQLDateTime(retryTime) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		}

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int count = executeUpdateQuery(conn, query);
		return (count > 0);
	}

	public void setRefID(String refId) {
		m_refID = refId;
	}

	@Override
	public String operatorName() {
		return this.operatorName;
	}


	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
}