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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.rbt.CallingParty.CallingPartyType;
import com.onmobile.apps.ringbacktones.activemonitoring.core.CounterStats;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.daemons.SMDaemonPerformanceMonitor;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.monitor.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;


public class SubscriberStatusImpl extends RBTPrimitive implements SubscriberStatus, iRBTConstant
{
	private static Logger logger = Logger.getLogger(SubscriberStatusImpl.class);

	private static final String TABLE_NAME = "RBT_SUBSCRIBER_SELECTIONS";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String CALLER_ID_COL = "CALLER_ID";
	private static final String CATEGORY_ID_COL = "CATEGORY_ID";
	private static final String SUBSCRIBER_WAV_FILE_COL = "SUBSCRIBER_WAV_FILE";
	private static final String SET_TIME_COL = "SET_TIME";
	private static final String START_TIME_COL = "START_TIME";
	private static final String END_TIME_COL = "END_TIME";
	private static final String STATUS_COL = "STATUS";
	private static final String CLASS_TYPE_COL = "CLASS_TYPE";
	private static final String SELECTED_BY_COL = "SELECTED_BY";
	private static final String SELECTION_INFO_COL = "SELECTION_INFO";
	private static final String NEXT_CHARGING_DATE_COL = "NEXT_CHARGING_DATE";
	private static final String PREPAID_YES_COL = "PREPAID_YES";
	private static final String FROM_TIME_COL = "FROM_TIME";
	private static final String TO_TIME_COL = "TO_TIME";
	private static final String SEL_STATUS_COL = "SEL_STATUS";
	private static final String DESELECTED_BY_COL = "DESELECTED_BY";
	private static final String OLD_CLASS_TYPE_COL = "OLD_CLASS_TYPE";
	private static final String CATEGORY_TYPE_COL = "CATEGORY_TYPE";
	private static final String LOOP_STATUS_COL = "LOOP_STATUS";
	private static final String SEL_TYPE_COL = "SEL_TYPE";
	private static final String SEL_INTERVAL_COL = "SEL_INTERVAL";
	private static final String INTERNAL_REF_ID_COL = "INTERNAL_REF_ID";
	private static final String EXTRA_INFO_COL = "EXTRA_INFO";
	private static final String CIRCLE_ID_COL = "CIRCLE_ID";
	private static final String RETRY_COUNT_COL = "RETRY_COUNT";
	private static final String NEXT_RETRY_TIME_COL = "NEXT_RETRY_TIME";
	private static final String INLINE_DAEMON_FLAG_COL = "INLINE_FLAG";
	private static final String TRANS_ID = "TRANS_ID";
	private static final String UDP_ID_COL = "UDP_ID";

	private String m_subscriberID;
	private String m_callerID;
	private int m_categoryID;
	private String m_subscriberWavFile;
	private Date m_setTime;
	private Date m_startTime;
	private Date m_endTime;
	private int m_status;
	private String m_classType;
	private String m_selectedBy;
	private String m_selectionInfo;
	private Date m_nextChargingDate;
	private String m_prepaid;
	private int m_fromTime;
	private int m_toTime;
	private String m_sel_status;
	private String m_deselected_by;
	private String m_old_class_type;
	private int m_category_type;
	private char m_loopStatus;
	private int m_sel_type;
	private String m_sel_interval;
	private String m_refID;
	private String m_extraInfo;
	private String m_circleId;
	private String retryCount;
	private Date nextRetryTime;
	private static boolean m_rrbtOn = false;
	private static String m_databaseType=getDBSelectionString();
	private Date requestTime;
	private String udpId = null;

	public SubscriberStatusImpl() {
		
	}
	
	public SubscriberStatusImpl(String subscriberID, String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status, String classType, String selectedBy,
			String selectionInfo, Date nextChargingDate, String prepaid, int fromTime, int toTime,
			String sel_status, String deSelectedBy, String oldClassType, int categoryType, char loopStatus, int selType, String selInterval, String refID, String extraInfo, String circleId, String udpId)
	{
		m_subscriberID = subscriberID;
		m_callerID = callerID;
		m_categoryID = categoryID;
		m_subscriberWavFile = subscriberWavFile;
		m_setTime = setTime;
		m_startTime = startTime;
		m_endTime = endTime;
		m_status = status;
		m_classType = classType;
		m_selectedBy = selectedBy;
		m_selectionInfo = selectionInfo;
		m_nextChargingDate = nextChargingDate;
		m_prepaid = prepaid;
		m_fromTime = fromTime;
		m_toTime = toTime;
		m_sel_status = sel_status;
		m_deselected_by = deSelectedBy;
		m_old_class_type = oldClassType;
		m_category_type = categoryType;
		m_loopStatus = loopStatus;
		m_sel_type = selType;
		m_sel_interval = selInterval;
		m_refID = refID;
		m_extraInfo = extraInfo;
		m_circleId = circleId;
		this.udpId = udpId;
	}

	public SubscriberStatusImpl(String subscriberID, String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status, String classType, String selectedBy,
			String selectionInfo, Date nextChargingDate, String prepaid, int fromTime, int toTime,
			String sel_status, String deSelectedBy, String oldClassType, int categoryType, char loopStatus, int selType, String selInterval, String refID,
			String extraInfo, String circleId, String retryCount, Date nextRetryTime, String udpId)
	{
		m_subscriberID = subscriberID;
		m_callerID = callerID;
		m_categoryID = categoryID;
		m_subscriberWavFile = subscriberWavFile;
		m_setTime = setTime;
		m_startTime = startTime;
		m_endTime = endTime;
		m_status = status;
		m_classType = classType;
		m_selectedBy = selectedBy;
		m_selectionInfo = selectionInfo;
		m_nextChargingDate = nextChargingDate;
		m_prepaid = prepaid;
		m_fromTime = fromTime;
		m_toTime = toTime;
		m_sel_status = sel_status;
		m_deselected_by = deSelectedBy;
		m_old_class_type = oldClassType;
		m_category_type = categoryType;
		m_loopStatus = loopStatus;
		m_sel_type = selType;
		m_sel_interval = selInterval;
		m_refID = refID;
		m_extraInfo = extraInfo;
		m_circleId = circleId;
		this.retryCount = retryCount;
		this.nextRetryTime = nextRetryTime;
		this.udpId = udpId;
	}

	public String subID()
	{
		return m_subscriberID;
	}

	public String callerID()
	{
		return m_callerID;
	}
	public int selType() 
    { 
            return m_sel_type; 
    } 
		
	public int categoryID()
	{
		return m_categoryID;
	}

	public String subscriberFile()
	{
		return m_subscriberWavFile;
	}

	public Date setTime()
	{
		return m_setTime;
	}

	public Date startTime()
	{
		return m_startTime;
	}

	public Date endTime()
	{
		return m_endTime;
	}

	public int status()
	{
		return m_status;
	}

	public String classType()
	{
		return m_classType;
	}

	public String selectedBy()
	{		return m_selectedBy;
	}

	public String selectionInfo()
	{
		return m_selectionInfo;
	}
	
	public String selInterval()
	{
		return m_sel_interval;
	}

	public String refID()
	{
		return m_refID;
	}
	
	public String extraInfo()
	{
		return m_extraInfo;
	}

	public Date nextChargingDate()
	{
		return m_nextChargingDate;
	}

	public boolean prepaidYes()
	{
		if(m_prepaid!= null)
			return m_prepaid.equalsIgnoreCase("y");
		else
			logger.info("RBT:: prepaid column is null" +m_subscriberID);

		return false;
	}

	public int fromTime()
	{
		return m_fromTime;
	}

	public int toTime()
	{
		return m_toTime;
	}

	public String selStatus()
	{
		return m_sel_status;
	}

	public String deSelectedBy()
	{
		return m_deselected_by;
	}

	public String oldClassType()
	{
		return m_old_class_type;
	}

	public int categoryType()
	{
		return m_category_type;
	}
	
	public char loopStatus()
	{
		return m_loopStatus;
	}

	public String circleId()
	{
		return m_circleId;
	}
	
	public void setSubscriberFile(String subscriberWavFile)
	{
		m_subscriberWavFile = subscriberWavFile;
	}

	public void setNextChargingDate(Date date)
	{
		m_nextChargingDate = date;
	}

	public void setPrepaidYes(boolean prepaid)
	{
		m_prepaid = "n";
		if(prepaid)
			m_prepaid = "y";
	}
	
	public static void setRRBT(boolean rRBT){
		m_rrbtOn = rRBT;  
	} 

	public String retryCount()
	{
		return retryCount;
	}

	public Date nextRetryTime()
	{
		return nextRetryTime;
	}

	public String date(Date date)
	{
		DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		return sqlTimeFormat.format(date);
	}

	public Date getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}

	static boolean moreSelectionsAllowed(Connection conn, String subscriberID, String callerID, int count, int rbtType){ 

		int m_count=0;
		Statement stmt = null;
		RBTResultSet rs = null;
		String query="SELECT COUNT(" + SUBSCRIBER_ID_COL + ") FROM " + TABLE_NAME + " WHERE " +
			SUBSCRIBER_ID_COL + " = "+ sqlString(subscriberID) ;
		query += " AND " + STATUS_COL + " IN (1,80) AND " + SEL_STATUS_COL + " IN ('" +
			STATE_TO_BE_ACTIVATED + "', '" + STATE_BASE_ACTIVATION_PENDING + "'," +
			" '" + STATE_ACTIVATED + "', '" + STATE_REQUEST_RENEWAL + "'," +
			" '" + STATE_CHANGE + "', '" + STATE_EVENT + "','" + STATE_ACTIVATION_PENDING + "', '" + STATE_SUSPENDED+"')" ;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			while(rs.next()){
				m_count=rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("", e);
			m_count=-1;
			return true;
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		if(m_count>=count)
			return false;
		else
			return true;
	}
	
	static int countSelectionsBySubscriber(Connection conn, String subscriberID, String callerID, int rbtType){

		int count=0;
		Statement stmt = null;
		RBTResultSet rs = null;
		String query="SELECT COUNT(SUBSCRIBER_ID) FROM "+TABLE_NAME +" WHERE " + 
			SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND "+CALLER_ID_COL + getNullForWhere(callerID);
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND SET_TIME <= SYSDATE AND END_TIME > SYSDATE AND ";
		else
			query += " AND SET_TIME <= SYSDATE() AND END_TIME > SYSDATE() AND ";
		
		query += STATUS_COL + " IN (1,80) AND " + SEL_STATUS_COL + " IN ('" +
			STATE_TO_BE_ACTIVATED + "', '" + STATE_BASE_ACTIVATION_PENDING + "'," +
			" '" + STATE_ACTIVATED + "', '" + STATE_REQUEST_RENEWAL + "'," +
			" '" + STATE_CHANGE + "', '" + STATE_EVENT + "','" + STATE_ACTIVATION_PENDING + "', '"+STATE_SUSPENDED+"')" ;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			while(rs.next()){
				count=rs.getInt(1);
			}
		} catch (SQLException e) {
			
			logger.error("", e);
			count=-1;
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		return count;
	}

	static void setSubscriberFile(Connection conn, String subscriberID, String callerID, Date setTime, String subscriberWavFile, int rbtType)
	{
		String query = null;
		Statement stmt = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " +
				SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subscriberWavFile) +
				" WHERE " + SUBSCRIBER_ID_COL  + getNullForWhere(subscriberID) + " AND " + 
				CALLER_ID_COL + getNullForWhere(callerID)+
				" AND TO_CHAR( " + SET_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(setTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = "UPDATE " + TABLE_NAME + " SET " +
				SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subscriberWavFile) +
				" WHERE " + SUBSCRIBER_ID_COL  + getNullForWhere(subscriberID) + " AND " + 
				CALLER_ID_COL + getNullForWhere(callerID)+
				" AND DATE_FORMAT( " + SET_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(setTime) + ", '%Y %m %d %T')";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

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
			return;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return;
	}
	
	static boolean updateSubscriberId(Connection conn, String newSubscriberId, String subscriberId)
	{
		Statement stmt = null;
		int n = -1;
		String query = "UPDATE " + TABLE_NAME + " SET " +
			SUBSCRIBER_ID_COL + " = '" + newSubscriberId +
			"' WHERE " + SUBSCRIBER_ID_COL  + " = '" + subscriberId + "'";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
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
		return (n>0);
	}
	
	static boolean updateSelStatusBasedOnRefID(Connection conn, String subscriberID,String refID,String selStatus){
		Statement stmt = null;
		String query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL
				+ " = " + sqlString(selStatus) + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID) + " AND " + INTERNAL_REF_ID_COL
				+ " = " + sqlString(refID);
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
	
	static boolean smUpdateSelStatus(Connection conn, String subscriberID, String callerID, String subFile, Date setTime, 
			String fStatus, String tStatus, int rbtType)
	{
		Statement stmt = null;
		String query = "UPDATE " + TABLE_NAME + " SET " +
			SEL_STATUS_COL + " = " + sqlString(tStatus) + 
			" WHERE " + SUBSCRIBER_ID_COL  + getNullForWhere(subscriberID) + " AND " + CALLER_ID_COL + getNullForWhere(callerID)+ " AND ";
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			query += SUBSCRIBER_WAV_FILE_COL + " = '"+subFile 
				+ "' AND TO_CHAR( " + SET_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(setTime) + ", 'YYYY/MM/DD HH24:MI:SS') AND "
				+ SEL_STATUS_COL + " = " + sqlString(fStatus) + " AND " + SET_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE";
		}
		else
		{
			query += SUBSCRIBER_WAV_FILE_COL + " = '"+subFile 
				+ "' AND DATE_FORMAT( " + SET_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(setTime) + ", '%Y %m %d %T') AND "
				+ SEL_STATUS_COL + " = " + sqlString(fStatus) + " AND " + SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";
		}
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

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

	static boolean smUpdateSelStatus(Connection conn, String subscriberID, String fStatus, String tStatus)
	{
		return smUpdateSelStatus(conn,subscriberID,fStatus,tStatus,false, null, null);
	}
	
	static boolean smUpdateSelStatus(Connection conn, String subscriberID, String fStatus, String tStatus,boolean isPack, List<String> refIDList, String chargeClass)
	{
		String query = null;
		Statement stmt = null;
		String refIDStr = "";
		if (refIDList != null)
		{
			for (int i = 0; i < refIDList.size(); i++)
			{
				if (i < refIDList.size() - 1)
					refIDStr += "'" + refIDList.get(i) + "',";
				else
					refIDStr += "'" + refIDList.get(i) + "'";
			}
		}

		query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "+ sqlString(tStatus); 
		if(chargeClass!=null){
			query = query + " , "+ CLASS_TYPE_COL + " = "+sqlString(chargeClass);
		}
		
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)){
			query =	query+ " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
					+ " = " + sqlString(fStatus)+ " AND " + SET_TIME_COL
					+ " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE";
		}else{
//			query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "+ sqlString(tStatus);
			query = query + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
					+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
					+ " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";
		}
		
		if (isPack) {
			query = query + " AND " + STATUS_COL + " != 99 AND (" + EXTRA_INFO_COL + " NOT LIKE '%PACK%' OR " + EXTRA_INFO_COL + " IS NULL)";
		}

		if (refIDStr.length() > 0)
			query += " AND " + INTERNAL_REF_ID_COL + " IN (" + refIDStr + ")";

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

	static boolean smUpdateSelStatusOnBaseSuccess(Connection conn, String subscriberID, String fStatus, String tStatus,boolean isPack, List<String> refIDList, String chargeClass)
	{
		String query = null;
		Statement stmt = null;
		String refIDStr = "";
		if (refIDList != null)
		{
			for (int i = 0; i < refIDList.size(); i++)
			{
				if (i < refIDList.size() - 1)
					refIDStr += "'" + refIDList.get(i) + "',";
				else
					refIDStr += "'" + refIDList.get(i) + "'";
			}
		}

		query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "+ sqlString(tStatus); 
		if(chargeClass!=null){
			query = query + " , "+ CLASS_TYPE_COL + " = "+sqlString(chargeClass);
		}
		
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)){
			query =	query+ " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
					+ " = " + sqlString(fStatus)+ " AND " + SET_TIME_COL
					+ " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE";
		}else{
//			query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "+ sqlString(tStatus);
			query = query + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
					+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
					+ " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";
		}
		
		if (isPack) {
			query = query + " AND " + STATUS_COL + " != 99 AND (" + EXTRA_INFO_COL + " NOT LIKE '%PACK%' AND " + EXTRA_INFO_COL + " NOT LIKE '%PROV_REF_ID%' OR " + EXTRA_INFO_COL + " IS NULL)";
		}else{
			query = query + " AND (" + EXTRA_INFO_COL + " NOT LIKE '%PROV_REF_ID%' OR "+ EXTRA_INFO_COL + " IS NULL )";
		}

		if (refIDStr.length() > 0)
			query += " AND " + INTERNAL_REF_ID_COL + " IN (" + refIDStr + ")";

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

	static boolean smUpdateSelStatusWithNoTransID(Connection conn, String subscriberID, String fStatus, String tStatus, String chargeClass) 
    {
        String query = null; 
        Statement stmt = null; 
        query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = " + sqlString(tStatus);
        
        if(chargeClass!=null){
        	query = query + " , " + CLASS_TYPE_COL + " = " +sqlString(chargeClass);
        }
        
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB)){
        	query = query +" WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL + " = " 
                + sqlString(fStatus) + " AND " + END_TIME_COL + " > SYSDATE AND "+ SELECTION_INFO_COL + " NOT LIKE '%:transid:%'" +
                		" AND (" + EXTRA_INFO_COL + " NOT LIKE '%PACK%' OR " + EXTRA_INFO_COL + " IS NULL)";
        }else{
        	query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL + " = " 
            + sqlString(fStatus) + " AND " + END_TIME_COL + " > SYSDATE() AND "+ SELECTION_INFO_COL + " NOT LIKE '%:transid:%'" +
            		" AND (" + EXTRA_INFO_COL + " NOT LIKE '%PACK%' OR " + EXTRA_INFO_COL + " IS NULL)";
        }
        
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        try 
        { 
            stmt = conn.createStatement(); 
            stmt.executeUpdate(query); 
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
        return true; 
    } 
 
    static boolean smUpdateSelStatusWithTransID(Connection conn, String subscriberID, String fStatus, String tStatus) 
    {
        String query = null; 
        Statement stmt = null; 
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = " 
                + sqlString(tStatus) + " WHERE " + SUBSCRIBER_ID_COL + " = " 
                + "'" + subscriberID + "' AND " + SEL_STATUS_COL + " = " 
                + sqlString(fStatus) + " AND " + END_TIME_COL + " > SYSDATE AND "+ SELECTION_INFO_COL + " LIKE '%:transid:%'" +
                		" AND (" + EXTRA_INFO_COL + " NOT LIKE '%PACK%' OR " + EXTRA_INFO_COL + " IS NULL)"; 
        else
        	query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = " 
            + sqlString(tStatus) + " WHERE " + SUBSCRIBER_ID_COL + " = " 
            + "'" + subscriberID + "' AND " + SEL_STATUS_COL + " = " 
            + sqlString(fStatus) + " AND " + END_TIME_COL + " > SYSDATE() AND "+ SELECTION_INFO_COL + " LIKE '%:transid:%'" +
            		" AND (" + EXTRA_INFO_COL + " NOT LIKE '%PACK%' OR " + EXTRA_INFO_COL + " IS NULL)"; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        try 
        { 
            stmt = conn.createStatement(); 
            stmt.executeUpdate(query); 
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
        return true; 
    } 
	
    static boolean smUpdateSelStatusOfCricketAndCorporateAndProfileSelections(Connection conn, String subscriberID, String fStatus, String tStatus)
	{
		String query = null;
		Statement stmt = null;

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
					+ sqlString(tStatus) + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
					+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
					+ " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE"
					+ " AND ( "+ STATUS_COL + " IN (90, 99) OR "+ SEL_TYPE_COL +" IN (2) )" ;
		else
			query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
					+ sqlString(tStatus) + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
					+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
					+ " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()"
					+ " AND ( "+ STATUS_COL + " IN (90, 99) OR "+ SEL_TYPE_COL +" IN (2) )" ;

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

    static boolean smUpdateSelStatusProfileSelections(Connection conn, String subscriberID, String fStatus, String tStatus , boolean isProfile)
	{
		String query = null;
		Statement stmt = null;

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
					+ sqlString(tStatus) + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
					+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
					+ " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE"
					+ " AND "+STATUS_COL + " = 99 ";
		else
			query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
					+ sqlString(tStatus) + " WHERE " + SUBSCRIBER_ID_COL
					+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
					+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
					+ " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()"
					+ " AND ";
		
		if(isProfile)
			query = query + STATUS_COL + " = 99 ";
		else
			query = query + STATUS_COL + " NOT IN ('99','90') ";

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
    
	static boolean setPrepaidYes(Connection conn, String subscriberID, boolean prepaidYes, int rbtType)
	{
		Statement stmt = null;

		String prepaid = "n";
		if(prepaidYes)
			prepaid = "y";

		String query = "UPDATE " + TABLE_NAME + " SET " +
			PREPAID_YES_COL + " = " + "'" + prepaid + "'" + 
			" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "'";
		
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType;

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

	static SubscriberStatus insert(Connection conn, String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String classType, String selectedBy, String selectionInfo,
			Date nextChargingDate, String prepaid, int fromTime, int toTime,
			boolean smActivation, String sel_status, String deSelectedBy,
			String oldClassType, int categoryType, char loopStatus,
			int nextPlus, int rbtType, String selInterval, String extraInfo,
			String refID, String circleId,String udpId, Integer inlineFlag)
	{
		String query = null;
		Statement stmt = null;

		String setDate = "SYSDATE";
		if(setTime != null)
			setDate = sqlTime(setTime);
		
		String startDate = "SYSDATE";
		if(smActivation)
			startDate = "TO_DATE('20040101','yyyyMMdd')";
		if(startTime != null)
			startDate = sqlTime(startTime);
		if(classType.startsWith("TRIAL"))
			startDate = "SYSDATE";
		
		String nextDate = null;
		if(nextPlus > 0)
			nextDate = "SYSDATE + "+nextPlus;
		if(nextChargingDate != null)
			nextDate = sqlTime(nextChargingDate);
		
		String endTimeStr = sqlTime(endTime);

		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			setDate = "SYSDATE()";
			if(setTime != null)
				setDate = mySQLDateTime(setTime);
			
			startDate = "SYSDATE()";
			if(smActivation)
				startDate = "TIMESTAMP('2004-01-01')";
			if(startTime != null)
				startDate = mySQLDateTime(startTime);
			if(classType.startsWith("TRIAL"))
				startDate = "SYSDATE()";
			
			if(nextPlus > 0)
				nextDate = "TIMESTAMPADD(DAY,"+nextPlus+",SYSDATE())";
			if(nextChargingDate != null)
				nextDate = mySQLDateTime(nextChargingDate);
			
			endTimeStr = mySQLDateTime(endTime);
		}
		
		if (refID == null)
			refID = UUID.randomUUID().toString();
		
		SubscriberStatusImpl subscriberStatus = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + CALLER_ID_COL;
		query += ", " + CATEGORY_ID_COL;
		query += ", " + SUBSCRIBER_WAV_FILE_COL;
		query += ", " + SET_TIME_COL;
		query += ", " + START_TIME_COL;
		query += ", " + END_TIME_COL;
		query += ", " + STATUS_COL;
		query += ", " + CLASS_TYPE_COL;
		query += ", " + SELECTED_BY_COL;		
		query += ", " + SELECTION_INFO_COL;
		query += ", " + NEXT_CHARGING_DATE_COL;
		query += ", " + PREPAID_YES_COL;
		query += ", " + FROM_TIME_COL;
		query += ", " + TO_TIME_COL;
		query += ", " + SEL_STATUS_COL;
		query += ", " + DESELECTED_BY_COL;
		query += ", " + OLD_CLASS_TYPE_COL;
		query += ", " + CATEGORY_TYPE_COL;
		query += ", " + LOOP_STATUS_COL;
		query += ", " + SEL_TYPE_COL;
		query += ", " + SEL_INTERVAL_COL;
		query += ", " + INTERNAL_REF_ID_COL;
		query += ", " + EXTRA_INFO_COL;
		query += ", " + CIRCLE_ID_COL;
		if (udpId != null)
			query += ", " + UDP_ID_COL;
		if(inlineFlag != null)
			query += ", " + INLINE_DAEMON_FLAG_COL;
		query += ")";

		query += " VALUES ( " + "'" + subscriberID + "'";
		if(callerID == null)
			query += ", " + null;
		else
			query += ", " + "'" + callerID + "'";
		query += ", " + sqlInt(categoryID);
		query += ", " + sqlString(subscriberWavFile);
		query += ", " + setDate;
		query += ", " + startDate;
		query += ", " + endTimeStr;
		query += ", " + status;
		query += ", " + sqlString(classType);
		query += ", " + sqlString(selectedBy);
		query += ", " + sqlString(selectionInfo);
		query += ", " + nextDate;
		query += ", " + "'" + prepaid + "'";
		query += ", " + fromTime;
		query += ", " + toTime;
		query += ", " + sqlString(sel_status);
		query += ", " + sqlString(deSelectedBy);
		query += ", " + sqlString(oldClassType);
		query += ", " + categoryType;
		query += ", " + sqlString(String.valueOf(loopStatus));
		query += ", " + rbtType; 
		query += ", " + sqlString(selInterval);
		query += ", "+sqlString(refID);
		query += ", "+sqlString(extraInfo);
		query += ", "+sqlString(circleId);
		if (udpId != null)
			query += ", "+sqlString(udpId);
		if(inlineFlag != null)
			query += ", "+ inlineFlag;
		query += ")";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			if (stmt.executeUpdate(query) > 0)
			{
				subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID, categoryID, subscriberWavFile,
						setTime, startTime, endTime, status, classType, selectedBy, selectionInfo,
						nextChargingDate, prepaid, fromTime, toTime, sel_status, deSelectedBy, oldClassType,
						categoryType, loopStatus,rbtType,selInterval, refID, extraInfo, circleId, udpId);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		
		logger.info("MSISDN: " + subscriberID + ". Insertion into RBT_SUBSCRIBER_SELECTIONS is " + (subscriberStatus != null ? " successful":" failed"));

		return subscriberStatus;
	}
	
	static SubscriberStatus addActiveSubSelections(Connection conn, String subscriberID, String callerID, int categoryID,
			String subscriberWavFile, Date setTime, Date startTime, Date endTime, int status, 
			String classType, String selectedBy, String selectionInfo, Date nextChargingDate,
			String prepaid, int fromTime, int toTime, boolean smActivation, String sel_status,
			String deSelectedBy, String oldClassType, int categoryType, char loopStatus, int rbtType)
	{
		int id = -1;
		String query = null;
		Statement stmt = null;

		String setDate = "SYSDATE";
		if(setTime != null)
			setDate = sqlTime(setTime);

		String startDate = "SYSDATE";
		if(smActivation)
			startDate = "TO_DATE('20040101','yyyyMMdd')";
		if(startTime != null)
			startDate = sqlTime(startTime);
		if(classType.startsWith("TRIAL"))
			startDate = "SYSDATE";
		
		SubscriberStatusImpl subscriberStatus = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + CALLER_ID_COL;
		query += ", " + CATEGORY_ID_COL;
		query += ", " + SUBSCRIBER_WAV_FILE_COL;
		query += ", " + SET_TIME_COL;
		query += ", " + START_TIME_COL;
		query += ", " + END_TIME_COL;
		query += ", " + STATUS_COL;
		query += ", " + CLASS_TYPE_COL;
		query += ", " + SELECTED_BY_COL;		
		query += ", " + SELECTION_INFO_COL;
		query += ", " + NEXT_CHARGING_DATE_COL;
		query += ", " + PREPAID_YES_COL;
		query += ", " + FROM_TIME_COL;
		query += ", " + TO_TIME_COL;
		query += ", " + SEL_STATUS_COL;
		query += ", " + DESELECTED_BY_COL;
		query += ", " + OLD_CLASS_TYPE_COL;
		query += ", " + CATEGORY_TYPE_COL;
		query += ", " + LOOP_STATUS_COL;
		if(m_rrbtOn) 
            query += ", " + SEL_TYPE_COL; 
		query += ")";

		query += " VALUES ( " + "'" + subscriberID + "'";
		if(callerID == null)
			query += ", " + null;
		else
			query += ", " + "'" + callerID + "'";
		query += ", " + sqlInt(categoryID);
		query += ", " + sqlString(subscriberWavFile);
		query += ", " + setDate;
		query += ", " + startDate;
		query += ", " + sqlTime(endTime);
		query += ", " + status;
		query += ", " + sqlString(classType);
		query += ", " + sqlString(selectedBy);
		query += ", " + sqlString(selectionInfo);
		query += ", " + sqlTime(nextChargingDate);
		query += ", " + "'" + prepaid + "'";
		query += ", " + fromTime;
		query += ", " + toTime;
		query += ", " + sqlString(sel_status);
		query += ", " + sqlString(deSelectedBy);
		query += ", " + sqlString(oldClassType);
		query += ", " + categoryType;
		query += ", " + sqlString(String.valueOf(loopStatus));
		if(m_rrbtOn) 
            query += ", " + rbtType; 
		query += ")";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			if (stmt.executeUpdate(query) > 0)
				id = 0;
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		logger.info("MSISDN: " + subscriberID + ". Insertion into RBT_SUBSCRIBER_SELECTIONS is " + (id == 0 ? " successful":" failed"));
		if(id == 0)
		{
			subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID, categoryID, subscriberWavFile,
					setTime, startTime, endTime, status, classType, selectedBy, selectionInfo,
					nextChargingDate, prepaid, fromTime, toTime, sel_status, deSelectedBy, oldClassType,
					categoryType, loopStatus,rbtType, null, null, null, "DUMMY",null);

			return subscriberStatus;
		}
		return null;
	}

	public void update(Connection conn)
	{
		boolean success = update(conn, this.m_subscriberID, this.m_callerID, this.m_categoryID, this.m_subscriberWavFile, this.m_setTime, this.m_startTime, this.m_endTime, this.m_status, this.m_classType, this.m_selectedBy, this.m_selectionInfo, this.m_nextChargingDate, this.m_prepaid, this.m_fromTime, this.m_toTime, this.m_sel_status, this.m_deselected_by, this.m_old_class_type);

		if(success)
			logger.info("RBT::update into RBT_SUBSCRIBER_SELECTIONS successful");
		else
			logger.info("RBT::update into RBT_SUBSCRIBER_SELECTIONS failed");
	}

	static boolean update(Connection conn, String subscriberID, String callerID, int categoryID, String subscriberWavFile, Date setTime, Date startTime, Date endTime, int status, String classType, String selectedBy, String selectionInfo, Date nextChargingDate, String prepaid, int fromTime, int toTime, String sel_status, String deSelectedBy, String oldClassType)
	{
		int n = -1;
		Statement stmt = null;

		String setDate = "SYSDATE";
		if(setTime != null)
			setDate = sqlTime(setTime);

		String startDate = "SYSDATE";
		if(startTime != null)
			startDate = sqlTime(startTime);

		String endTimeStr = sqlTime(endTime);
		String nextChargingDateStr = sqlTime(nextChargingDate);
		String setTimeCond = "TO_CHAR( " + SET_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(setTime) + ", 'YYYY/MM/DD HH24:MI:SS')"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			setDate = "SYSDATE()";
			if(setTime != null)
				setDate = mySQLDateTime(setTime);
			startDate = "SYSDATE()";
			if(startTime != null)
				startDate = mySQLDateTime(startTime);
			endTimeStr = mySQLDateTime(endTime);
			nextChargingDateStr = mySQLDateTime(nextChargingDate);
			setTimeCond = "DATE_FORMAT( " + SET_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(setTime) + ", '%Y %m %d %T')"; 
		}	
		
		String query = "UPDATE " + TABLE_NAME + " SET " +
		CATEGORY_ID_COL + " = " + sqlInt(categoryID) + ", " +
		SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subscriberWavFile) + ", " +
		SET_TIME_COL + " = " + setDate + ", " +
		START_TIME_COL + " = " + startDate + ", " +
		END_TIME_COL + " = " + endTimeStr + ", " +
		STATUS_COL + " = " + status + ", " +
		CLASS_TYPE_COL + " = " + sqlString(classType) + ", " +
		SELECTED_BY_COL + " = " + sqlString(selectedBy) + ", " +
		SELECTION_INFO_COL + " = " + sqlString(selectionInfo) + ", " +
		NEXT_CHARGING_DATE_COL + " = " + nextChargingDateStr + ", " +
		PREPAID_YES_COL + " = " + "'" + prepaid + "'" + ", " +
		FROM_TIME_COL + " = " + fromTime + ", " +
		TO_TIME_COL + " = " + toTime + ", " +
		SEL_STATUS_COL + " = " + sqlString(sel_status) + ", " +  
		DESELECTED_BY_COL + " = " + sqlString(deSelectedBy) + ", " +  
		OLD_CLASS_TYPE_COL + " = " + sqlString(oldClassType) +   
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere(callerID)+ " AND "+setTimeCond;

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}

	static boolean deactivateSubscriberFeedStatus(Connection conn, int status, int rbtType)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " +
				END_TIME_COL + " = SYSDATE, " + 
				SEL_STATUS_COL + " = '" + STATE_DEACTIVATED + "' " + 
				" WHERE " + STATUS_COL + " = " + status + " AND " + END_TIME_COL + " > SYSDATE AND TO_CHAR( " + END_TIME_COL + " , 'YYYY/MM/DD') = TO_CHAR( SYSDATE, 'YYYY/MM/DD')";
		else
			query = "UPDATE " + TABLE_NAME + " SET " +
			END_TIME_COL + " = SYSDATE(), " + 
			SEL_STATUS_COL + " = '" + STATE_DEACTIVATED + "' " + 
			" WHERE " + STATUS_COL + " = " + status + " AND " + END_TIME_COL + " > SYSDATE() AND DATE_FORMAT( " + END_TIME_COL + " , '%Y %m %d') = DATE_FORMAT( SYSDATE(), '%Y %m %d')";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n!=0);
	}

	/*subscription manager changes*/
	 static boolean deactivate(Connection conn, String subscriberID, Date endTime, boolean smDeactivation, 
			 boolean isNewSubscriber, String deactBy, int rbtType)
	{
		int n = -1;
		Statement stmt = null;

		String date = "SYSDATE";
		if(endTime != null)
			date = sqlTime(endTime);

		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE()";
			if(endTime != null)
				date = mySQLDateTime(endTime);
			nextChargingDate = "TIMESTAMP('2037-12-31')";
		}

		String query = "UPDATE " + TABLE_NAME + " SET " +
		END_TIME_COL + " = " + date + ", " + 
		NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", " +
		DESELECTED_BY_COL + " = " + sqlString(deactBy);
		if(smDeactivation)
			query = query + " , " + SEL_STATUS_COL + " = '" + STATE_DEACTIVATED + "', "+ LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED+"' ";
		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' "; 
		if(smDeactivation){
			query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' , '" 
				+ STATE_ACTIVATION_PENDING + "', '" + STATE_ACTIVATED + "' , '" + STATE_ACTIVATION_ERROR + "', '"
				+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "', '" + STATE_REQUEST_RENEWAL + "', '"
				+ STATE_UN + "', '" + STATE_TO_BE_DEACTIVATED + "', '" 
				+ STATE_DEACTIVATION_PENDING + "', '" + STATE_ACTIVATION_GRACE + "' , '"+STATE_SUSPENDED+"' , '"+STATE_SUSPENDED_INIT+"' )";			
		}
		else
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = query + " AND " +SET_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE";
			else
				query = query + " AND " +SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";
		}
		if(m_rrbtOn)
			query += " AND "+ SEL_TYPE_COL + " = " + rbtType;
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}	

	/*subscription manager changes*/
	static boolean smDeactivate(Connection conn, String subscriberID, Date endTime, boolean smDeactivation, String type,String consentId)
	{
		int n = -1;
		Statement stmt = null;

		String date = "SYSDATE";
		if(endTime != null)
			date = sqlTime(endTime);

		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE()";
			if(endTime != null)
				date = mySQLDateTime(endTime);
			nextChargingDate = "TIMESTAMP('2037-12-31')";
		}

		String prepaid = "n";
		if(type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		String query = "UPDATE " + TABLE_NAME + " SET " +
		END_TIME_COL + " = " + date + ", " + 
		NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", " + 
		PREPAID_YES_COL + " = '" + prepaid + "'"; 
		if(smDeactivation)
			query = query + ", " + SEL_STATUS_COL + " = '" + STATE_DEACTIVATED + "' , " + LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED+"' ";
		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND "; 
		if(smDeactivation){
			query = query + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' , '" 
				+ STATE_ACTIVATION_PENDING + "', '" + STATE_ACTIVATED + "' , '" + STATE_ACTIVATION_ERROR + "', '" 
				+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "', '" + STATE_REQUEST_RENEWAL + "', '" 
				+ STATE_UN + "', '"+STATE_SUSPENDED+"')";
		}
		else
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = query + SET_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE";
			else
				query = query + SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";
		}
		if (consentId != null)
			query = query + " AND " + INTERNAL_REF_ID_COL  + " = '" +consentId + "'";
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}

	/*subscription manager changes*/
	static boolean deactivateSubscriberRecords(Connection conn, String subscriberID, String callerID, int status, int fromTime, int toTime, boolean smDeactivation, String deSelectedBy, int rbtType)
	{
		int n = -1;
		Statement stmt = null;

		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		String endTimeStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			endTimeStr = "SYSDATE()";
			nextChargingDate = "TIMESTAMP('2037-12-31')";
		}

		String sel_status = "D";
		SubscriberStatus subscriberStatus = getActiveSubscriberRecord(conn, subscriberID, callerID, status, fromTime, toTime, rbtType);
		if(subscriberStatus != null)
		{
			String classType = "DEFAULT";
			ChargeClass chargeClass = null;
			if(subscriberStatus.classType() != null)
			{
				classType = subscriberStatus.classType();
			}
			chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
			String selectionType = null;
			if(chargeClass != null)
			{
				selectionType = chargeClass.getSelectionType();
			}
			if(!smDeactivation && subscriberStatus.nextChargingDate() == null && selectionType != null && selectionType.equalsIgnoreCase("SELECTIONS"))
				nextChargingDate = null;
			if(subscriberStatus.nextChargingDate() != null)
			{
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				String nextChargeDate = dateFormat.format(subscriberStatus.nextChargingDate());
				if(nextChargeDate != null && nextChargeDate.equalsIgnoreCase("20371130"))
				{
					nextChargingDate = sqlTime(subscriberStatus.nextChargingDate());
					if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
						nextChargingDate = mySQLDateTime(subscriberStatus.nextChargingDate());
				}
			}
		}

		String query = "UPDATE " + TABLE_NAME + " SET " +
		END_TIME_COL + " = "+ endTimeStr + ", " + 
		NEXT_CHARGING_DATE_COL + " = " + nextChargingDate;
		if(smDeactivation)
			query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy);
		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CALLER_ID_COL + getNullForWhere(callerID) + " AND " + STATUS_COL + " = " + status + " AND " + 
		FROM_TIME_COL + " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		if(smDeactivation)
		{
			query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
				+ STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
				+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','"
				+ STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"')";
		}
		else 
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = query + " AND " + SET_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE";
			else
				query = query + " AND " + SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";
		}

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}
	
	static boolean deactivateSubscriberRecords(Connection conn, String subscriberID, String callerID, int status, int fromTime, int toTime, boolean smDeactivation, String deSelectedBy, String wavFile, int rbtType) 
    { 
            int n = -1; 
            Statement stmt = null; 
            
            String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
    		String endTimeStr = "SYSDATE";
    		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
    		{
    			nextChargingDate = "TIMESTAMP('2037-12-31')";
    			endTimeStr = "SYSDATE()";
    		}

            String sel_status = "D"; 
            SubscriberStatus subscriberStatus = getActiveSubscriberRecord(conn, subscriberID, callerID, status, fromTime, toTime, rbtType); 
            if(subscriberStatus != null) 
            { 
                    String classType = "DEFAULT"; 
                    ChargeClass chargeClass = null; 
                    if(subscriberStatus.classType() != null) 
                    { 
                            classType = subscriberStatus.classType(); 
                    } 
                    chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
                    String selectionType = null; 
                    if(chargeClass != null) 
                    { 
                            selectionType = chargeClass.getSelectionType(); 
                    } 
                    if(!smDeactivation && subscriberStatus.nextChargingDate() == null && selectionType != null && selectionType.equalsIgnoreCase("SELECTIONS")) 
                            nextChargingDate = null; 
                    if(subscriberStatus.nextChargingDate() != null)
        			{
        				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        				String nextChargeDate = dateFormat.format(subscriberStatus.nextChargingDate());
        				if(nextChargeDate != null && nextChargeDate.equalsIgnoreCase("20371130"))
        				{
        					nextChargingDate = sqlTime(subscriberStatus.nextChargingDate());
        					if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        						nextChargingDate = mySQLDateTime(subscriberStatus.nextChargingDate());
        				}
        			}
                    
            } 

    		String query = "UPDATE " + TABLE_NAME + " SET " + 
            END_TIME_COL + " = "+endTimeStr + ", " + 
            NEXT_CHARGING_DATE_COL + " = " + nextChargingDate; 
            if(smDeactivation) 
                    query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy); 
            query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
            CALLER_ID_COL + getNullForWhere(callerID)+" AND " + STATUS_COL + " = " + status + " AND " + 
            FROM_TIME_COL + " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime; 
            if(m_rrbtOn) 
                query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 


            if(smDeactivation) 
            { 
                    query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
                            + STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
                            + STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" 
                            + STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"')";
            } 
            else 
    		{
            	if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
    				query = query + " AND " + SET_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE";
    			else
    				query = query + " AND " + SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";
    		}
            
            if(wavFile != null) 
                    query = query + " AND " + SUBSCRIBER_WAV_FILE_COL + " = '"+wavFile+"'"; 

    		logger.info("Executing query: " + query);
    		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
            try 
            { 
                    stmt = conn.createStatement(); 
                    stmt.executeUpdate(query); 
                    n = stmt.getUpdateCount(); 
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
            return(n>=1);
    } 

	static boolean deactivateSubscriberRecordsByRefId(Connection conn,
			String subscriberID, String deSelectedBy, String refId,
			String newExtraInfo, String newRefId, Character newLoopStatus, String sel_status) {
            int n = -1; 
            Statement stmt = null; 
            
            String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
    		String endTimeStr = "SYSDATE";
    		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
    		{
    			nextChargingDate = "TIMESTAMP('2037-12-31')";
    			endTimeStr = "SYSDATE()";
    		}

    		if (sel_status == null) {
    			sel_status = "D";
    		} 
    		
    		String query = "UPDATE " + TABLE_NAME + " SET " + 
            END_TIME_COL + " = "+endTimeStr + ", " + 
            NEXT_CHARGING_DATE_COL + " = " + nextChargingDate; 
            query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy) + " "; 

            if (newExtraInfo != null) {
            	query = query + ", " + EXTRA_INFO_COL + " = " + sqlString(newExtraInfo) + " ";
            }
            
            if (newRefId != null) {
            	query = query + ", " + INTERNAL_REF_ID_COL + " = " + sqlString(newRefId) + " ";
            }
            if (newLoopStatus != null) {
            	query = query + ", " + LOOP_STATUS_COL + " = '" + newLoopStatus + "' ";
            }
            query = query + "WHERE " + SUBSCRIBER_ID_COL  + " = " + sqlString(subscriberID) + " AND " + 
            INTERNAL_REF_ID_COL + " = " +sqlString(refId); 

            logger.info("Executing query: " + query);
            RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
            try 
            { 
                    stmt = conn.createStatement(); 
                    stmt.executeUpdate(query); 
                    n = stmt.getUpdateCount(); 
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
            return(n>=1);
    } 
	
	static boolean deactivateSubscriberRecordsByStatus(Connection conn, String subscriberID, int status, String deSelectedBy) 
    { 
            int n = -1; 
            Statement stmt = null; 
            try{
            	stmt = conn.createStatement();
            	String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
            	String endTimeStr = "SYSDATE";
            	if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
            	{
            		nextChargingDate = "TIMESTAMP('2037-12-31')";
            		endTimeStr = "SYSDATE()";
            	}

            	String sel_status = "D"; 
            	SubscriberStatus subscriberStatus[] = getActiveSelectionsByStatus(conn, subscriberID, status); 
            	if(subscriberStatus!=null && subscriberStatus.length>0)
            	for(int i=0;i<subscriberStatus.length;i++){
            		if(subscriberStatus != null) 
            		{ 
            			if(subscriberStatus[i].nextChargingDate() != null)
            			{
            				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            				String nextChargeDate = dateFormat.format(subscriberStatus[i].nextChargingDate());
            				if(nextChargeDate != null && nextChargeDate.equalsIgnoreCase("20371130"))
            				{
            					nextChargingDate = sqlTime(subscriberStatus[i].nextChargingDate());
            					if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
            						nextChargingDate = mySQLDateTime(subscriberStatus[i].nextChargingDate());
            				}
            			}
            			char oldLoopStatus = subscriberStatus[i].loopStatus();
            			char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
            	    	if(oldLoopStatus == LOOP_STATUS_EXPIRED)
            	    		newLoopStatus = oldLoopStatus;
            	    	else if(oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT || oldLoopStatus == LOOP_STATUS_LOOP_INIT) 
            	            newLoopStatus = LOOP_STATUS_EXPIRED; 

            		String query = "UPDATE " + TABLE_NAME + " SET " + 
            		END_TIME_COL + " = "+endTimeStr + ", " + NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", "+ LOOP_STATUS_COL + " = '" + newLoopStatus +"' " ; 
            		query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy); 
            		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + STATUS_COL + " = " + status ;

            		query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
            		+ STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
            		+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" 
            		+ STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"')";

            		logger.info("Executing query: " + query);
            		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
            		stmt.executeUpdate(query); 
            		n = stmt.getUpdateCount(); 
            		}
            	}
            }
            catch(SQLException e){
            	logger.error("",e);
            }
            finally{
            	closeStatementAndRS(stmt, null);
            }
            return(n>=1);
    } 

	static boolean deactivateSubscriberRecordsByStatusAndAllCaller(Connection conn, String subscriberID, int status, String deSelectedBy)
	{ 
        int n = -1; 
        Statement stmt = null; 
        try{
        	stmt = conn.createStatement();
        	String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
        	String endTimeStr = "SYSDATE";
        	if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	{
        		nextChargingDate = "TIMESTAMP('2037-12-31')";
        		endTimeStr = "SYSDATE()";
        	}

        	String sel_status = "D"; 
        	char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;

        	String query = "UPDATE " + TABLE_NAME + " SET " + 
        			END_TIME_COL + " = "+endTimeStr + ", " + NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", "+ LOOP_STATUS_COL + " = '" + newLoopStatus +"' " ; 
        	query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy); 
        	query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + STATUS_COL + " = " + status ;

        	query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
        			+ STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
        			+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" 
        			+ STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"') AND " + CALLER_ID_COL + " IS NULL";

        	logger.info("Executing query: " + query);
        	RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        	stmt.executeUpdate(query); 
        	n = stmt.getUpdateCount(); 
        }
        catch(SQLException e){
        	logger.error("",e);
        }
        finally{
        	closeStatementAndRS(stmt, null);
        }
        return(n>=1);
}

	static boolean deactivateSubscriberRecordsNotInStatus(Connection conn, String subscriberID, String status, String deSelectedBy) 
    { 
            int n = -1; 
            Statement stmt = null; 
            try{
            	stmt = conn.createStatement();
            	String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
            	String endTimeStr = "SYSDATE";
            	if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
            	{
            		nextChargingDate = "TIMESTAMP('2037-12-31')";
            		endTimeStr = "SYSDATE()";
            	}

            	String sel_status = "D"; 
            	SubscriberStatus subscriberStatus[] = getActiveSelectionsNotInStatus(conn, subscriberID, status); 
            	if(subscriberStatus!=null && subscriberStatus.length>0)
            	for(int i=0;i<subscriberStatus.length;i++){
            		if(subscriberStatus != null) 
            		{ 
            			if(subscriberStatus[i].nextChargingDate() != null)
            			{
            				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            				String nextChargeDate = dateFormat.format(subscriberStatus[i].nextChargingDate());
            				if(nextChargeDate != null && nextChargeDate.equalsIgnoreCase("20371130"))
            				{
            					nextChargingDate = sqlTime(subscriberStatus[i].nextChargingDate());
            					if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
            						nextChargingDate = mySQLDateTime(subscriberStatus[i].nextChargingDate());
            				}
            			}
            			char oldLoopStatus = subscriberStatus[i].loopStatus();
            			char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
            	    	if(oldLoopStatus == LOOP_STATUS_EXPIRED)
            	    		newLoopStatus = oldLoopStatus;
            	    	else if(oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT || oldLoopStatus == LOOP_STATUS_LOOP_INIT) 
            	            newLoopStatus = LOOP_STATUS_EXPIRED; 

            		String query = "UPDATE " + TABLE_NAME + " SET " + 
            		END_TIME_COL + " = "+endTimeStr + ", " + NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", "+ LOOP_STATUS_COL + " = '" + newLoopStatus +"' " ; 
            		query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy); 
            		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + STATUS_COL + " NOT IN ('" + status + "')";
            		query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
            		+ STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
            		+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" 
            		+ STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"')";

            		logger.info("Executing query: " + query);
            		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
            		stmt.executeUpdate(query); 
            		n = stmt.getUpdateCount(); 
            		}
            	}
            }
            catch(SQLException e){
            	logger.error("",e);
            }
            finally{
            	closeStatementAndRS(stmt, null);
            }
            return(n>=1);
    } 
	
	/*UGS changes*/
	static boolean deactivateSubscriberRecordWavFile(Connection conn, String subscriberID,
			String callerID, int status, int fromTime, int toTime, boolean smDeactivation,
			String deSelectedBy, String wavFile, String selInterval, int rbtType)
	{
		int n = -1;
		Statement stmt = null;

		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		String endTimeStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextChargingDate = "TIMESTAMP('2037-12-31')";
			endTimeStr = "SYSDATE()";
		}
		String sel_status = "D";
		
		String selIntervalCond = SEL_INTERVAL_COL;
		if(selInterval == null)
			selIntervalCond += " IS NULL";
		else 
			selIntervalCond += " = "+ sqlString(selInterval); 
			
		String query = "UPDATE " + TABLE_NAME + " SET " +
		END_TIME_COL + " = "+endTimeStr + ", " + 
		NEXT_CHARGING_DATE_COL + " = " + nextChargingDate;
		if(smDeactivation)
			query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy);
		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CALLER_ID_COL + getNullForWhere(callerID)+" AND " + STATUS_COL + " = " + status + " AND " + 
		FROM_TIME_COL + " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime + " AND " +
		SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(wavFile) +" AND "+ selIntervalCond;

		if(smDeactivation)
		{
			query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
				+ STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
				+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','"
				+ STATE_UN + "', '"+STATE_SUSPENDED+ STATE_UN + "', '"+STATE_SUSPENDED+"' , '"+STATE_SUSPENDED_INIT+"' )";
		}
		else 
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = query + " AND " + SET_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE";
			else
				query = query + " AND " + SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";	
		}

		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}
	
	static SubscriberStatus[] getSelectionsToBeDeactivated(Connection conn,
			String subscriberID, Map<String, String> whereClauseMap)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT * FROM ").append(TABLE_NAME);
		
		queryBuilder.append(" WHERE ").append(SUBSCRIBER_ID_COL).append(" = '");
		queryBuilder.append(subscriberID).append("'");
		if (whereClauseMap.containsKey("CALLER_ID"))
		{
			String callerID = whereClauseMap.get("CALLER_ID");
			queryBuilder.append(" AND ").append(CALLER_ID_COL).append(getNullForWhere(callerID));
		}
		if (whereClauseMap.containsKey("STATUS"))
		{
			String status = whereClauseMap.get("STATUS");
			queryBuilder.append(" AND ").append(STATUS_COL).append(" = ").append(status);
		}
		if (whereClauseMap.containsKey("FROM_TIME"))
		{
			String fromTime = whereClauseMap.get("FROM_TIME");
			queryBuilder.append(" AND ").append(FROM_TIME_COL).append(" = ").append(Integer.parseInt(fromTime));
		}
		if (whereClauseMap.containsKey("TO_TIME"))
		{
			String toTime = whereClauseMap.get("TO_TIME");
			queryBuilder.append(" AND ").append(TO_TIME_COL).append(" = ").append(Integer.parseInt(toTime));
		}
		if (whereClauseMap.containsKey("SUBSCRIBER_WAV_FILE"))
		{
			String subscriberWavFile = whereClauseMap.get("SUBSCRIBER_WAV_FILE");
			queryBuilder.append(" AND ").append(SUBSCRIBER_WAV_FILE_COL).append(" = ").append(sqlString(subscriberWavFile));
		}
		if (whereClauseMap.containsKey("CATEGORY_ID"))
		{
			String categoryID = whereClauseMap.get("CATEGORY_ID");
			queryBuilder.append(" AND ").append(CATEGORY_ID_COL).append(" = ").append(categoryID);
		}
		if (whereClauseMap.containsKey("SEL_INTERVAL"))
		{
			String selInterval = whereClauseMap.get("SEL_INTERVAL");
			queryBuilder.append(" AND ").append(SEL_INTERVAL_COL).append(getNullForWhere(selInterval));
		}
		if (whereClauseMap.containsKey("SEL_TYPE"))
		{
			String selType = whereClauseMap.get("SEL_TYPE");
			queryBuilder.append(" AND ").append(SEL_TYPE_COL).append(" = ").append(selType);
		}
		else
		{
			// Not allowing corporate selection deactivation
			queryBuilder.append(" AND ").append(SEL_TYPE_COL).append(" != 2");
		}
		if (whereClauseMap.containsKey("CATEGORY_TYPE"))
		{
			String categoryType = whereClauseMap.get("CATEGORY_TYPE");
			queryBuilder.append(" AND ").append(CATEGORY_TYPE_COL).append(" = ").append(categoryType);
		}
		if (whereClauseMap.containsKey("REF_ID"))
		{
			queryBuilder.append(" AND ").append(INTERNAL_REF_ID_COL).append(" = ").append(sqlString(whereClauseMap.get("REF_ID")));
		}

		try {
			if (whereClauseMap.containsKey("START_TIME"))
			{
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				String startTime = whereClauseMap.get("START_TIME");
				String startTimeStr;
				startTimeStr = sqlTime(dateFormat.parse(startTime));
				if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
				{
					startTimeStr = mySQLDateTime(dateFormat.parse(startTime));
				}

				queryBuilder.append(" AND ").append(START_TIME_COL).append(" = ").append(startTimeStr);
			}

			if (whereClauseMap.containsKey("END_TIME"))
			{
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				String endTime = whereClauseMap.get("END_TIME");
				String endTimeStr;
				endTimeStr = sqlTime(dateFormat.parse(endTime));
				if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
				{
					endTimeStr = mySQLDateTime(dateFormat.parse(endTime));
				}

				queryBuilder.append(" AND ").append(END_TIME_COL).append(" = ").append(endTimeStr);
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}

		queryBuilder.append(" AND ").append(SEL_STATUS_COL).append(" IN ('").append(STATE_TO_BE_ACTIVATED).append("', '");
		queryBuilder.append(STATE_ACTIVATION_PENDING).append("', '").append(STATE_ACTIVATED).append("', '");
		queryBuilder.append(STATE_ACTIVATION_ERROR).append("', '");
		queryBuilder.append(STATE_BASE_ACTIVATION_PENDING).append("', '").append(STATE_CHANGE).append("', '");
		queryBuilder.append(STATE_REQUEST_RENEWAL).append("', '").append(STATE_UN).append("', '");
		queryBuilder.append(STATE_GRACE).append("', '").append(STATE_SUSPENDED);
		queryBuilder.append("', '").append(STATE_SUSPENDED_INIT).append("' )");

		String udpId = null;
		if(whereClauseMap.containsKey("UDP_ID")) {
			udpId = whereClauseMap.get("UDP_ID");
			queryBuilder.append(" AND ").append(UDP_ID_COL).append(" = ").append(sqlString(udpId));
		}
		
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
		logger.info("Executing query: " + queryBuilder.toString());
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(queryBuilder.toString());
			while (rs.next()){
				subscriberStatusList.add(getSubscriberStatusFromRS(rs));
			}
			return convertSubscriberStatusListToArray(subscriberStatusList);
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
	}
	
	
	static boolean deactivateSubscriberSelections(Connection conn,
			String subscriberID, Map<String, String> updateClauseMap,
			Map<String, String> whereClauseMap)
	{
		int updateCount = -1;
		Statement stmt = null;

		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		String endTimeStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextChargingDate = "TIMESTAMP('2037-12-31')";
			endTimeStr = "SYSDATE()";
		}

		String selStatus = STATE_TO_BE_DEACTIVATED;
		if (updateClauseMap.containsKey("SEL_STATUS"))
			selStatus = updateClauseMap.get("SEL_STATUS");

		String deselectedBy = updateClauseMap.get("DESELECTED_BY");

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		queryBuilder.append(END_TIME_COL).append(" = ").append(endTimeStr);
		queryBuilder.append(", ").append(NEXT_CHARGING_DATE_COL).append(" = ");
		queryBuilder.append(nextChargingDate).append(", ");
		queryBuilder.append(SEL_STATUS_COL).append(" = ");
		queryBuilder.append(sqlString(selStatus)).append(", ");
		queryBuilder.append(DESELECTED_BY_COL).append(" = ");
		queryBuilder.append(sqlString(deselectedBy));
		
		if (updateClauseMap.containsKey("LOOP_STATUS"))
		{
			queryBuilder.append(", ").append(LOOP_STATUS_COL);
			queryBuilder.append(" = ").append(sqlString(updateClauseMap.get("LOOP_STATUS")));
		}
		if (updateClauseMap.containsKey("REF_ID"))
		{
			queryBuilder.append(", ").append(INTERNAL_REF_ID_COL);
			queryBuilder.append(" = ").append(sqlString(updateClauseMap.get("REF_ID")));
		}
		if (updateClauseMap.containsKey("DESELECTION_INFO"))
		{
			queryBuilder.append(", ").append(SELECTION_INFO_COL);
			queryBuilder.append(" = CONCAT(").append(SELECTION_INFO_COL);
			queryBuilder.append(", '|DCT:").append(updateClauseMap.get("DESELECTION_INFO")).append("|')");
		}
		//Added extra info column in the update to update the sr_id and originator info 
		// as per the jira id RBT-11962.
		//Fix for RBT-12391,RBT-12394
		if (updateClauseMap.containsKey("EXTRA_INFO"))
		{
			queryBuilder.append(", ").append(EXTRA_INFO);
			queryBuilder.append(" = ").append(sqlString(updateClauseMap.get("EXTRA_INFO")));
		}
		queryBuilder.append(" WHERE ").append(SUBSCRIBER_ID_COL).append(" = '");
		queryBuilder.append(subscriberID).append("'");
		if (whereClauseMap.containsKey("CALLER_ID"))
		{
			String callerID = whereClauseMap.get("CALLER_ID");
			queryBuilder.append(" AND ").append(CALLER_ID_COL).append(getNullForWhere(callerID));
		}
		if (whereClauseMap.containsKey("STATUS"))
		{
			String status = whereClauseMap.get("STATUS");
			queryBuilder.append(" AND ").append(STATUS_COL).append(" = ").append(status);
		}
		if (whereClauseMap.containsKey("FROM_TIME"))
		{
			String fromTime = whereClauseMap.get("FROM_TIME");
			queryBuilder.append(" AND ").append(FROM_TIME_COL).append(" = ").append(Integer.parseInt(fromTime));
		}
		if (whereClauseMap.containsKey("TO_TIME"))
		{
			String toTime = whereClauseMap.get("TO_TIME");
			queryBuilder.append(" AND ").append(TO_TIME_COL).append(" = ").append(Integer.parseInt(toTime));
		}
		if (whereClauseMap.containsKey("SUBSCRIBER_WAV_FILE"))
		{
			String subscriberWavFile = whereClauseMap.get("SUBSCRIBER_WAV_FILE");
			queryBuilder.append(" AND ").append(SUBSCRIBER_WAV_FILE_COL).append(" = ").append(sqlString(subscriberWavFile));
		}
		if (whereClauseMap.containsKey("CATEGORY_ID"))
		{
			String categoryID = whereClauseMap.get("CATEGORY_ID");
			queryBuilder.append(" AND ").append(CATEGORY_ID_COL).append(" = ").append(categoryID);
		}
		if (whereClauseMap.containsKey("SEL_INTERVAL"))
		{
			String selInterval = whereClauseMap.get("SEL_INTERVAL");
			queryBuilder.append(" AND ").append(SEL_INTERVAL_COL).append(getNullForWhere(selInterval));
		}
		if (whereClauseMap.containsKey("SEL_TYPE"))
		{
			String selType = whereClauseMap.get("SEL_TYPE");
			queryBuilder.append(" AND ").append(SEL_TYPE_COL).append(" = ").append(selType);
		}
		else
		{
			// Not allowing corporate selection deactivation
			queryBuilder.append(" AND ").append(SEL_TYPE_COL).append(" != 2");
		}
		if (whereClauseMap.containsKey("CATEGORY_TYPE"))
		{
			String categoryType = whereClauseMap.get("CATEGORY_TYPE");
			queryBuilder.append(" AND ").append(CATEGORY_TYPE_COL).append(" = ").append(categoryType);
		}
		if (whereClauseMap.containsKey("REF_ID"))
		{
			queryBuilder.append(" AND ").append(INTERNAL_REF_ID_COL).append(" = ").append(sqlString(whereClauseMap.get("REF_ID")));
		}

		try {
			if (whereClauseMap.containsKey("START_TIME"))
			{
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				String startTime = whereClauseMap.get("START_TIME");
				String startTimeStr;
				startTimeStr = sqlTime(dateFormat.parse(startTime));
				if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
				{
					startTimeStr = mySQLDateTime(dateFormat.parse(startTime));
				}

				queryBuilder.append(" AND ").append(START_TIME_COL).append(" = ").append(startTimeStr);
			}

			if (whereClauseMap.containsKey("END_TIME"))
			{
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				String endTime = whereClauseMap.get("END_TIME");
				String whereEndTimeStr;
				whereEndTimeStr = sqlTime(dateFormat.parse(endTime));
				if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
				{
					whereEndTimeStr = mySQLDateTime(dateFormat.parse(endTime));
				}

				queryBuilder.append(" AND ").append(END_TIME_COL).append(" = ").append(whereEndTimeStr);
			}
			if (whereClauseMap.containsKey("UDP_ID")) {
				queryBuilder.append(" AND ").append(UDP_ID_COL).append(" = ").append(sqlString(whereClauseMap.get("UDP_ID")));
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}

		queryBuilder.append(" AND ").append(SEL_STATUS_COL).append(" IN ('").append(STATE_TO_BE_ACTIVATED).append("', '");
		queryBuilder.append(STATE_ACTIVATION_PENDING).append("', '").append(STATE_ACTIVATED).append("', '");
		queryBuilder.append(STATE_ACTIVATION_ERROR).append("', '");
		queryBuilder.append(STATE_BASE_ACTIVATION_PENDING).append("', '").append(STATE_CHANGE).append("', '");
		queryBuilder.append(STATE_REQUEST_RENEWAL).append("', '").append(STATE_UN).append("', '");
		queryBuilder.append(STATE_GRACE).append("', '").append(STATE_SUSPENDED);
		queryBuilder.append("', '").append(STATE_SUSPENDED_INIT).append("' )");

		logger.info("Executing query: " + queryBuilder);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, queryBuilder.toString(), Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(queryBuilder.toString());
			updateCount = stmt.getUpdateCount();
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
		return(updateCount > 0);
	}
	
	static boolean isSelSuspended(Connection conn, String subID, String callID, int rbtType) 
    { 
        Statement stmt = null; 
        ResultSet results = null; 
        boolean isSuspendedSel = false; 
       
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
				+ CALLER_ID_COL +  getNullForWhere(callID)+" AND SEL_STATUS IN ('"
				+ STATE_SUSPENDED + "', '"+STATE_SUSPENDED_INIT+"')";

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = query + " AND " + SET_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE";
		else
			query = query + " AND " + SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
        try 
        { 
            stmt = conn.createStatement(); 
            results = stmt.executeQuery(query); 
            if(results.next()) 
            { 
                    isSuspendedSel = true; 
            } 
        } 
        catch(SQLException se) 
        { 
            logger.error("", se); 
            return false; 
        } 
        finally 
        { 
    		closeStatementAndRS(stmt, results);
        } 
        return isSuspendedSel; 
    } 
	
	static boolean deactivateOldProfileSelectionsForCaller(Connection conn, 
            String subscriberID, String callerID, Date setTime , boolean isProfile) 
    { 
        int n = -1; 
        Statement stmt = null; 
        String sysdateStr = "SYSDATE";
        String nextChargingDate = "TO_DATE('20351231','yyyyMMdd')";
        String setTimeStr = sqlTime(setTime);
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
     	   sysdateStr = "SYSDATE()";
     	   nextChargingDate = "TIMESTAMP('2035-12-31')";
     	   setTimeStr = mySQLDateTime(setTime);
        }
         
        String sel_status = "X"; 
        String query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL 
                + " = "+sysdateStr + "," + END_TIME_COL 
                + " = "+sysdateStr + ", " + NEXT_CHARGING_DATE_COL + " = " 
                + nextChargingDate + "," + SEL_STATUS_COL + " = " 
                    + sqlString(sel_status) + " , " + DESELECTED_BY_COL 
                    + " = 'SM'  , " +LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED+"' WHERE " + SUBSCRIBER_ID_COL + " = " + "'" 
                + subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere (callerID)+" AND " ;
        if(isProfile)	
        	query = query + STATUS_COL + " = 99 AND ";
        else
        	query = query + STATUS_COL + " NOT IN ('99','90') AND ";
         
        query = query  + SET_TIME_COL 
                + " < " + setTimeStr + " AND " + SEL_STATUS_COL + " = 'W' ";

        logger.info("Executing query: " + query);
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
            return false; 
        } 
        finally 
        { 
     	   closeStatementAndRS(stmt, null);
        } 
        return (n == 1); 
    } 
	
	static boolean deactivateOldNormalSelectionsForCaller(Connection conn, 
               String subscriberID, String callerID, Date setTime) 
       { 
           int n = -1; 
           Statement stmt = null; 
           String sysdateStr = "SYSDATE";
           String nextChargingDate = "TO_DATE('20351231','yyyyMMdd')";
           String setTimeStr = sqlTime(setTime);
           if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
           {
        	   sysdateStr = "SYSDATE()";
        	   nextChargingDate = "TIMESTAMP('2035-12-31')";
        	   setTimeStr = mySQLDateTime(setTime);
           }
            
           String sel_status = "X"; 
           String query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL 
                   + " = "+sysdateStr + "," + END_TIME_COL 
                   + " = "+sysdateStr + ", " + NEXT_CHARGING_DATE_COL + " = " 
                   + nextChargingDate + "," + SEL_STATUS_COL + " = " 
                       + sqlString(sel_status) + " , " + DESELECTED_BY_COL 
                       + " = 'SM'  , " +LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED+"' WHERE " + SUBSCRIBER_ID_COL + " = " + "'" 
                   + subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere (callerID)+" AND " 
                   + STATUS_COL + " = 1 AND " + SET_TIME_COL 
                   + " < " + setTimeStr + " AND " + SEL_STATUS_COL + " = 'W' ";
 
           logger.info("Executing query: " + query);
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
               return false; 
           } 
           finally 
           { 
        	   closeStatementAndRS(stmt, null);
           } 
           return (n == 1); 
       } 

	static boolean deactivateOldODASelectionsForCaller(Connection conn, 
            String subscriberID, String callerID, Date setTime,String refId) 
    { 
        int n = -1; 
        Statement stmt = null; 
        String sysdateStr = "SYSDATE";
        String nextChargingDate = "TO_DATE('20351231','yyyyMMdd')";
        String setTimeStr = sqlTime(setTime);
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
     	   sysdateStr = "SYSDATE()";
     	   nextChargingDate = "TIMESTAMP('2035-12-31')";
     	   setTimeStr = mySQLDateTime(setTime);
        }
         
        String sel_status = "X"; 
        String query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL 
                + " = "+sysdateStr + "," + END_TIME_COL 
                + " = "+sysdateStr + ", " + NEXT_CHARGING_DATE_COL + " = " 
                + nextChargingDate + "," + SEL_STATUS_COL + " = " 
                    + sqlString(sel_status) + " , " + DESELECTED_BY_COL 
                    + " = 'SM'  , " +LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED+"' WHERE " + SUBSCRIBER_ID_COL + " = " + "'" 
                + subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere (callerID)+" AND " 
                + STATUS_COL + " = 1 AND " + SET_TIME_COL 
                + " < " + setTimeStr + " AND " + SEL_STATUS_COL + " = 'W' ";

        query += " AND "+EXTRA_INFO_COL + " LIKE  '%PROV_REF_ID=\""+refId+"\"%'" ;
        
        logger.info("Executing query: " + query);
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
            return false; 
        } 
        finally 
        { 
     	   closeStatementAndRS(stmt, null);
        } 
        return (n == 1); 
    } 

	static boolean deactivateSettingDownloadFailure(Connection conn, 
            String subscriberID, String subscriberWavFile, char loopStatus) 
    { 
        int n = -1; 
        Statement stmt = null; 
        String sysdateStr = "SYSDATE";
        String nextChargingDate = "TO_DATE('20351231','yyyyMMdd')";
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
     	   sysdateStr = "SYSDATE()";
     	   nextChargingDate = "TIMESTAMP('2035-12-31')";
        }
         
        String sel_status = "X"; 
        String query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL 
                + " = "+sysdateStr + "," + END_TIME_COL 
                + " = "+sysdateStr + ", " + NEXT_CHARGING_DATE_COL + " = " 
                + nextChargingDate + "," + SEL_STATUS_COL + " = " 
                    + sqlString(sel_status) + " , " + DESELECTED_BY_COL 
                    + " = 'SM'  , " +LOOP_STATUS_COL + " = " + "'"+loopStatus+"' WHERE " + SUBSCRIBER_ID_COL + " = " + "'" 
                + subscriberID + "' AND " + SEL_STATUS_COL + " = '"+STATE_BASE_ACTIVATION_PENDING+"' AND SUBSCRIBER_WAV_FILE = " + sqlString(subscriberWavFile); 

		logger.info("Executing query: " + query);
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
            return false; 
        } 
        finally 
        { 
    		closeStatementAndRS(stmt, null);
        } 
        return (n == 1); 
    } 

	static boolean deactivateSettingDownloadRenewalFailure(Connection conn, 
            String subscriberID, String subscriberWavFile) 
    { 
        int n = -1; 
        Statement stmt = null; 
        String sysdateStr = "SYSDATE";
        String nextChargingDate = "TO_DATE('20351231','yyyyMMdd')";
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
     	   sysdateStr = "SYSDATE()";
     	   nextChargingDate = "TIMESTAMP('2035-12-31')";
        }
         
        String sel_status = "X"; 
        String query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL 
                + " = "+sysdateStr + ", " + NEXT_CHARGING_DATE_COL + " = " 
                + nextChargingDate + "," + SEL_STATUS_COL + " = " 
                    + sqlString(sel_status) + " , " + DESELECTED_BY_COL 
                    + " = 'SM'  , " +LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED_INIT+"' WHERE " + SUBSCRIBER_ID_COL + " = " + "'" 
               + subscriberID + "' AND " + SEL_STATUS_COL + " != '"+STATE_DEACTIVATED+"' AND SUBSCRIBER_WAV_FILE = " + sqlString(subscriberWavFile);

		logger.info("Executing query: " + query);
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
            return false; 
        } 
        finally 
        { 
    		closeStatementAndRS(stmt, null);
        } 
        return (n == 1); 
    } 

	static boolean deactivateSettingDownloadDeact(Connection conn, 
            String subscriberID, String subscriberWavFile) 
    { 
        int n = -1; 
        Statement stmt = null; 
        String sysdateStr = "SYSDATE";
        String nextChargingDate = "TO_DATE('20351231','yyyyMMdd')";
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
     	   sysdateStr = "SYSDATE()";
     	   nextChargingDate = "TIMESTAMP('2035-12-31')";
        }
         
        String sel_status = "D"; 
        String query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL 
                + " = "+sysdateStr + ", " + NEXT_CHARGING_DATE_COL + " = " 
                + nextChargingDate + "," + SEL_STATUS_COL + " = " 
                    + sqlString(sel_status) + " , " + DESELECTED_BY_COL 
                    + " = 'SM'  , " +LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED_INIT+"' WHERE " + SUBSCRIBER_ID_COL + " = " + "'" 
                + subscriberID + "' AND SUBSCRIBER_WAV_FILE = " + sqlString(subscriberWavFile)+ " AND " + SEL_STATUS_COL + " NOT IN ('"+STATE_DEACTIVATED+"')"; 

		logger.info("Executing query: " + query);
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
            return false; 
        } 
        finally 
        { 
    		closeStatementAndRS(stmt, null);
        } 
        return (n == 1); 
    } 

	static boolean activateSettingDownloadSuccess(Connection conn, 
            String subscriberID, String subscriberWavFile) 
    { 
        int n = -1; 
        Statement stmt = null; 
         
        String sel_status = "A"; 
        String query = "UPDATE " + TABLE_NAME + " SET " +  SEL_STATUS_COL + " = " 
                + sqlString(sel_status) + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" 
                + subscriberID + "' AND " + SEL_STATUS_COL + " = '"+STATE_BASE_ACTIVATION_PENDING
                + "' AND SUBSCRIBER_WAV_FILE = " + sqlString(subscriberWavFile); 

		logger.info("Executing query: " + query);
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
            return false; 
        } 
        finally 
        { 
    		closeStatementAndRS(stmt, null);
        } 
        return (n == 1); 
    } 

	static boolean deactivateRealTimeSubscriberRecords(Connection conn,
            String subscriberID, String callerID, int status, int fromTime,
            int toTime, String deSelectedBy, String setDate, String refID)
    {
        int n = -1;
        Statement stmt = null;

        String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
        String sysadteStr = "SYSDATE";
        String setTimeCond = " to_char(" + SET_TIME_COL+ ",'yyyyMMddhh24miss') ";
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
        	nextChargingDate = "TIMESTAMP('2037-12-31')";
        	sysadteStr = "SYSDATE()";
        	setTimeCond = " DATE_FORMAT(" + SET_TIME_COL+ ",'%Y%m%d%H%i%s') ";
        }
        	
        String sel_status = "X";
        String query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL + " = "+ sysadteStr+", "  + END_TIME_COL
                + " = "+sysadteStr + ", " + NEXT_CHARGING_DATE_COL + " = "
                + nextChargingDate;
        query = query + "," + SEL_STATUS_COL + " = "
                    + sqlString(sel_status) + " , " + DESELECTED_BY_COL
                    + " = " + sqlString(deSelectedBy)+ " , " +LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED+"' ";
		
        query += "," + INTERNAL_REF_ID_COL + " = " + sqlString(refID);

        query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
                + subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere(callerID)+ " AND "
                + setTimeCond +" = '" + setDate + "' AND " + STATUS_COL + " = " + status + " AND " + FROM_TIME_COL
                + " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime;
        

        query = query + " AND " + SEL_STATUS_COL + " = 'S'";

        logger.info("Executing query: " + query);
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
            return false;
        }
        finally
        {
    		closeStatementAndRS(stmt, null);
        }
        return (n == 1);
    }

	/*ADDED FOR TATA*/
    static boolean deactivateSubscriberRecords(Connection conn, String subscriberID, String callerID, int rbtType)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		String sysDateStr = "SYSDATE()";
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE";
		query = "UPDATE " + TABLE_NAME + " SET " +
			END_TIME_COL + " = "+ sysDateStr + ", " + 
			SEL_STATUS_COL + " = " + sqlString(STATE_DEACTIVATED) + " , " +LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED+"' " + 
			" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere(callerID)+" AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + SEL_STATUS_COL + " = " + sqlString(STATE_ACTIVATED);
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}
    
    static boolean updateSelectionType(Connection conn,String subscriberId,String callerId,String subWavFile,Date setTime,Date endDate,int tStatus,
    			int fromTime,int toTime,String selInterval,char loopStatus){
    	int n = -1;
    	Statement stmt = null;
    	String endTime = null;
    	
    	String setTimeCond = "TO_CHAR( " + SET_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(setTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
    	endTime = sqlTime(endDate);
    	if(!m_databaseType.equalsIgnoreCase(DB_SAPDB)){
    		setTimeCond = "DATE_FORMAT( " + SET_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(setTime) + ", '%Y %m %d %T')";
    		endTime = mySQLDateTime(endDate);
    	}
    	
    	String query = "UPDATE " + TABLE_NAME + " SET " + FROM_TIME_COL + " = " + fromTime + " , " + TO_TIME_COL + " = " + toTime + " , " 
    	+ SEL_INTERVAL_COL + " = " + sqlString(selInterval) + " , " + LOOP_STATUS_COL + " = " + "'" + loopStatus + "'" + " , " +STATUS_COL + " = " + tStatus;
    	
    	if(endDate != null)
    		query += " , " + END_TIME_COL + " = " + endTime;
    	
    	query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberId + "'" + " AND "
    	+ CALLER_ID_COL + getNullForWhere(callerId) + " AND " + setTimeCond;
    	
    	if(subWavFile!=null)
    		query += " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + "'" + subWavFile + "'";
    	
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
    	try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
     }

    static boolean deactivateSubscriberRecordsTrial(Connection conn, String subscriberID, int rbtType, Object beanActive)
	{
		int n = -1;
		Statement stmt = null;
		String nextChargingDate = "TO_DATE('20371231','YYYYMMDD')";
		String sysDateStr = "SYSDATE";
		String deSelectedBy = "SM";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextChargingDate = "TIMESTAMP('2037-12-31')";
			sysDateStr = "SYSDATE()";
		}
		String query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL + " = "+ sysDateStr+" , "  + END_TIME_COL
        + " = "+sysDateStr + ", " + NEXT_CHARGING_DATE_COL + " = "
        + nextChargingDate + "," + SEL_STATUS_COL + " = "
            + sqlString(STATE_DEACTIVATED) + " , " + DESELECTED_BY_COL
            + " = " + sqlString(deSelectedBy) + " , " +LOOP_STATUS_COL + " = " + "'"+LOOP_STATUS_EXPIRED+"'  WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' " +
			"AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + 
			SEL_STATUS_COL + " = " + sqlString(STATE_BASE_ACTIVATION_PENDING);
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
		if(beanActive!=null)
			query += " AND "+ EXTRA_INFO + " LIKE '%UPGRADE_PENDING%'" ; 
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}

	/*time of the day changes*/
    static SubscriberStatus getActiveSubscriberRecord(Connection conn, String subID, String callID, int st, int fTime, int tTime, int rbtType)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		String query = "SELECT * FROM " + TABLE_NAME + 
			" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subID + "' AND " + CALLER_ID_COL + getNullForWhere(callID)+" AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " + FROM_TIME_COL + " = " + fTime + " AND " + TO_TIME_COL + " = " + tTime;
		if (st != -1)
			query += " AND " + STATUS_COL + " = " + st;
		if (m_rrbtOn)
			query += " AND " + SEL_TYPE_COL + " = " + rbtType;
				
		query += " ORDER BY " + SET_TIME_COL + " DESC";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
			{
				//TODO:
				subscriberStatus = getSubscriberStatusFromRS(results);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return subscriberStatus;
	}
    
    static SubscriberStatus getActiveSubscriberRecordByStatus(Connection conn, String subID, int st)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		String query = "SELECT * FROM " + TABLE_NAME + 
			" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subID + "' AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr ;
		if (st != -1)
			query += " AND " + STATUS_COL + " = " + st;
				
		query += " ORDER BY " + SET_TIME_COL + " DESC";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
			{
				//TODO:
				subscriberStatus = getSubscriberStatusFromRS(results);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return subscriberStatus;
	}
    
    static SubscriberStatus[] getActiveSelectionsByType(Connection conn, String subscriberID, int selectionType)
	{
		Statement stmt = null;
		ResultSet results = null;

		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysdate = SAPDB_SYSDATE;
		if (m_databaseType.equals(DB_MYSQL))
			sysdate = MYSQL_SYSDATE;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
				+ END_TIME_COL + " > " + sysdate + " AND " + SEL_TYPE_COL
				+ " = " + selectionType;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatusList.add(getSubscriberStatusFromRS(results));
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}
    
    static SubscriberStatus[] getActiveSelectionsByStatus(Connection conn, String subscriberID, int status)
	{
		Statement stmt = null;
		ResultSet results = null;

		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysdate = SAPDB_SYSDATE;
		if (m_databaseType.equals(DB_MYSQL))
			sysdate = MYSQL_SYSDATE;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
				+ END_TIME_COL + " > " + sysdate + " AND " + STATUS_COL
				+ " = " + status;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatusList.add(getSubscriberStatusFromRS(results));
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

    static SubscriberStatus[] getActiveSelectionsNotInStatus(Connection conn, String subscriberID, String status)
	{
		Statement stmt = null;
		ResultSet results = null;

		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysdate = SAPDB_SYSDATE;
		if (m_databaseType.equals(DB_MYSQL))
			sysdate = MYSQL_SYSDATE;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
				+ END_TIME_COL + " > " + sysdate + " AND " + STATUS_COL
				+ " IN ('" + status + "')";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatusList.add(getSubscriberStatusFromRS(results));
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

    
    static SubscriberStatus getSelection(Connection conn,String subscriberId,String callerId,String subWavFile,Date setTime){

    	Statement stmt = null;
    	ResultSet results = null;
    	SubscriberStatus subscriberStatus = null;
    	
    	String setTimeCond = "TO_CHAR( " + SET_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(setTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
    	if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
    		setTimeCond = "DATE_FORMAT( " + SET_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(setTime) + ", '%Y %m %d %T')";
    		
    	String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberId + "'" + " AND " + CALLER_ID_COL + getNullForWhere(callerId) + " AND " + setTimeCond;
    	
    	if(subWavFile!=null)
    		query += " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + "'" + subWavFile + "'";
    	
		logger.info("Executing query: " + query);
    	try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
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
		return subscriberStatus;
    }
    
    //used in UGCFileUploader only
    static SubscriberStatus getSelection(Connection conn,String subscriberID,String subWavFile){
		
		Statement stmt = null;
		ResultSet results = null;

		String query = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ SUBSCRIBER_WAV_FILE_COL + " like " + sqlString(subWavFile + "%")
				+ " AND " + END_TIME_COL + " > SYSDATE";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
			+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
			+ SUBSCRIBER_WAV_FILE_COL + " like " + sqlString(subWavFile + "%")
			+ " AND " + END_TIME_COL + " > SYSDATE()";
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
				return getSubscriberStatusFromRS(results);
		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return null;
    }
	
static SubscriberStatus getSelectionByRefId(Connection conn,String subscriberID,String refId){
		
		Statement stmt = null;
		ResultSet results = null;

		String query = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " like " + sqlString(refId)
				+ " AND " + END_TIME_COL + " > SYSDATE";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
			+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
			+ INTERNAL_REF_ID_COL + " like " + sqlString(refId)
			+ " AND " + END_TIME_COL + " > SYSDATE()";
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
				return getSubscriberStatusFromRS(results);
		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return null;
    }

static SubscriberStatus getSelectionBySubIdRefId(Connection conn,String subscriberID,String refId){
	
	Statement stmt = null;
	ResultSet results = null;

	String query = null;
	if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		query = "SELECT * FROM " + TABLE_NAME + " WHERE "
			+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
			+ INTERNAL_REF_ID_COL + " like " + sqlString(refId);
	else
		query = "SELECT * FROM " + TABLE_NAME + " WHERE "
		+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
		+ INTERNAL_REF_ID_COL + " like " + sqlString(refId);
	
	logger.info("Executing query: " + query);
	try
	{
		stmt = conn.createStatement();
		results = stmt.executeQuery(query);
		if(results.next())
			return getSubscriberStatusFromRS(results);
	}
	catch(SQLException e)
	{
		logger.error("", e);
	}
	finally
	{
		closeStatementAndRS(stmt, results);
	}
	return null;
}


	/*time of the day changes*/
	static SubscriberStatus getActiveSubscriberStatus(Connection conn, String subID, String callID, String code, int fTime, int tTime)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'" + " AND " + CALLER_ID_COL + getNullForWhere(callID)+" AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " + NEXT_CHARGING_DATE_COL + " IS NOT NULL AND " + FROM_TIME_COL + " = " + fTime + " AND " + TO_TIME_COL + " = " + tTime;
		
		if(code != null)
			query += " AND " + STATUS_COL + " NOT IN (" + code + ")";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
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
		return subscriberStatus;
	}

	static SubscriberStatus [] getSubscriberRecords(Connection conn, String subID, String code, boolean smActivation, int rbtType)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " +  STATUS_COL + " NOT IN (" + code + ")";
		if(smActivation)
		{
			query = query + " AND " + NEXT_CHARGING_DATE_COL + " IS NOT NULL ";
		}
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}
	
	static HashMap<String, ArrayList<String>> getRecommendationByArtistsFromSelections(
			Connection conn,
			HashMap<String, ArrayList<String>> artistClipNameMap,
			int fetchDays, int maxRecommendationForSubscriber,
			RBTDBManager rbtDBManager)
	{
		HashMap<String, ArrayList<String>> subscriberMap = new HashMap<String, ArrayList<String>>();
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			query = "SELECT " + SUBSCRIBER_WAV_FILE_COL + "," + SUBSCRIBER_ID_COL + " FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL + 
					" > TIMESTAMPADD(DAY,-"+fetchDays+",SYSDATE())";
		}
		else
		{
			query = "SELECT " + SUBSCRIBER_WAV_FILE_COL + "," + SUBSCRIBER_ID_COL + " FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL + 
					" > SYSDATE - " + fetchDays;	
		}
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			
			int dwnCnt = 0;
			HashMap<String, Integer> subscriberRecCountMap = new HashMap<String, Integer>();
			while(results.next())
			{
				String wavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
				String subscriberID = results.getString(SUBSCRIBER_ID_COL);
				
				ClipMinimal clip = rbtDBManager.getClipRBT(wavFile);
				
				String artist = null;
				if(clip != null && clip.getArtist() != null)
					artist = clip.getArtist().toLowerCase().trim();
				
				if(artist != null && artistClipNameMap.containsKey(artist))
				{
					if (subscriberRecCountMap.containsKey(subscriberID))
					{
						dwnCnt = subscriberRecCountMap.get(subscriberID);
						if (dwnCnt == maxRecommendationForSubscriber)
							continue;
						else
						{
							dwnCnt++;
							subscriberRecCountMap.put(subscriberID, dwnCnt);
						}
					}
					else
					{
						dwnCnt = 1;
						subscriberRecCountMap.put(subscriberID, dwnCnt);
					}
					
					ArrayList<String> subscriberList = subscriberMap.get(artist);
					if (subscriberList == null)
					{
						subscriberList = new ArrayList<String>();
						subscriberList.add(subscriberID);
						subscriberMap.put(artist, subscriberList);
					}
					else
					{
						if(!subscriberList.contains(subscriberID))
							subscriberList.add(subscriberID);
					}
				}
			}
			logger.info("The size of subscriberMap "+subscriberMap.size());
			if (subscriberMap.size() > 0)
				return subscriberMap;
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
		return null;
	}
	
	static HashMap<String, ArrayList<String>> getRecommendationByCategoryFromSelections(Connection conn, String[] categoryIDs, int recDownloadCount,
			 int fetchDays, int maxRecommendationForSubscriber)
	{
		HashMap<String, ArrayList<String>> subscriberMap = new HashMap<String, ArrayList<String>>();
		List<String> categoryIDList = Arrays.asList(categoryIDs);
		
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			query = "SELECT " + CATEGORY_ID_COL + "," + SUBSCRIBER_ID_COL + " FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL + 
					" > TIMESTAMPADD(DAY,-"+fetchDays+",SYSDATE())";
		}
		else
		{
			query = "SELECT " + CATEGORY_ID_COL + "," + SUBSCRIBER_ID_COL + " FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL + 
					" > SYSDATE - " + fetchDays;	
		}
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			
			HashMap<String, Integer> subscriberCategoryCountMap = new HashMap<String, Integer>();
			HashMap<String, Integer> subscriberRecCountMap = new HashMap<String, Integer>();
			
			while(results.next())
			{
				int catID = results.getInt(CATEGORY_ID_COL);
				String subscriberID = results.getString(SUBSCRIBER_ID_COL);
				
				String categoryID = String.valueOf(catID);
				if(categoryIDList.contains(categoryID))
				{
					String key = subscriberID + "_" + categoryID;
					if(subscriberCategoryCountMap.containsKey(key))
					{
						int count = subscriberCategoryCountMap.get(key);
						count++;
						subscriberCategoryCountMap.put(key, count);
					}
					else
					{
						subscriberCategoryCountMap.put(key, 1);
					}
				}
			}
			
			int recCountForSubscriber = 0;
			Set<String> subscriberCategorySet = subscriberCategoryCountMap.keySet();
			for (String subscriberCategoryKey : subscriberCategorySet)
			{
				int recCount =  subscriberCategoryCountMap.get(subscriberCategoryKey);
				String subID = subscriberCategoryKey.substring(0, subscriberCategoryKey.indexOf("_"));
				String catID = subscriberCategoryKey.substring(subscriberCategoryKey.indexOf("_")+1);
				if(recCount >= recDownloadCount)
				{
					if(subscriberRecCountMap.containsKey(subID))
					{
						recCountForSubscriber = subscriberRecCountMap.get(subID); 
						if(recCountForSubscriber == maxRecommendationForSubscriber)
							continue;
						else
						{
							recCountForSubscriber++;
							subscriberRecCountMap.put(subID, recCountForSubscriber);
						}
					}
					else
					{
						subscriberRecCountMap.put(subID, 1);
					}
					
					ArrayList<String> subscriberList = subscriberMap.get(catID);
					if (subscriberList == null)
					{
						subscriberList = new ArrayList<String>();
						subscriberList.add(subID);
						subscriberMap.put(catID, subscriberList);
					}
					else
					{
						if(!subscriberList.contains(subID))
							subscriberList.add(subID);
					}
				}
			}
			
			if (subscriberMap.size() > 0)
				return subscriberMap;
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
		return null;
	}	

	static SubscriberStatus [] getSubscriberRecords(Connection conn, String subID, String callerID, String code, boolean smActivation, int rbtType)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND " +CALLER_ID_COL + getNullForWhere(callerID) + " AND "+ SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " +  STATUS_COL + " NOT IN (" + code + ")";
		if(smActivation)
		{
			query = query + " AND " + NEXT_CHARGING_DATE_COL + " IS NOT NULL ";
		}
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}	

	static SubscriberStatus [] getActiveSelectionBasedOnCallerId(Connection conn, String subID, String callerID)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID
				+ "' AND " + CALLER_ID_COL + getNullForWhere(callerID) + " AND " + SET_TIME_COL
				+ " <= " + sysDateStr + " AND " + END_TIME_COL + " > " + sysDateStr;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}	

	static SubscriberStatus[] getActiveSelOnCallerIdFromTimeToTime(Connection conn,
			String subscriberID, String callerID, int fromTime, int toTime)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
                + "' AND "+ FROM_TIME_COL + " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime
				+ " AND " + CALLER_ID_COL + getNullForWhere(callerID) + " AND " + SET_TIME_COL
				+ " <= " + sysDateStr + " AND " + END_TIME_COL + " > " + sysDateStr;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS based on caller-catId-fromTime-toTime. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}	

	/***
	 * added by sandeep
	 * */
	public static SubscriberStatus[] getAllAirtelSubscriberSelectionRecords(
			Connection conn, String subID, String startDate, String endDate, int rbtType) 
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
		+NEXT_CHARGING_DATE_COL+" IS NOT NULL ";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}
	/***/
	static SubscriberStatus [] getAllSubscriberSelectionRecords(Connection conn, String subID, String code, int rbtType)
	{
		Statement stmt = null;
		ResultSet results = null;
		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
				+ SET_TIME_COL + " <= "+sysDateStr+" AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "', '"
				+ STATE_BASE_ACTIVATION_PENDING + "', '" + STATE_ACTIVATED + "', '" + STATE_REQUEST_RENEWAL
				+ "', '" + STATE_CHANGE + "', '" + STATE_EVENT + "','"
				+ STATE_ACTIVATION_PENDING + "', '" + STATE_ACTIVATION_ERROR
				+ "', '" + STATE_UN + "', '" + STATE_ACTIVATION_GRACE + "', '"
				+ STATE_SUSPENDED + "')";

		if(code != null)
			query += " AND " +  STATUS_COL + " NOT IN (" + code + ")";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		query += " ORDER BY " + CALLER_ID_COL + " DESC";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	static SubscriberStatus [] getAllSubscriberSelectionRecordsNotDeactivated(Connection conn, String subID, String code, int rbtType)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
		+ SET_TIME_COL + " <= "+sysDateStr+" AND " + SEL_STATUS_COL + "!= " + sqlString(STATE_DEACTIVATED) ;

		if(code != null)
			query += " AND " +  STATUS_COL + " NOT IN (" + code + ")";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
		
		query += " ORDER BY " + CALLER_ID_COL + " DESC";

		logger.info("Executing query: " + query);
		int count = 0;
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next() && count < 5000)
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	/*added for separate player db feature*/
	static SubscriberStatus [] getAllActiveSubSelectionRecords(Connection conn, String subID, int rbtType, boolean checkSelType)
	{
		Statement stmt = null;
		ResultSet results = null;

		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
		+ SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr;
		if(checkSelType && m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
		query += " ORDER BY " + CALLER_ID_COL+ ", " + SET_TIME_COL;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatusList.add(getSubscriberStatusFromRS(results));
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}
	
	// added for tone player for getting all subscriber selections by Sreenadh 
	static SubscriberStatus [] getAllSubSelectionRecordsForTonePlayer(Connection conn, String subID)
	{
		Statement stmt = null;
		ResultSet results = null;

		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = '" + subID + "' ORDER BY " + CALLER_ID_COL + ", " + STATUS_COL + ", " + SET_TIME_COL;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatusList.add(getSubscriberStatusFromRS(results));
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}
	// getAllSubSelectionRecordsForReactivation
	static SubscriberStatus[] getAllSubSelectionRecordsForReactivation(Connection conn, String subscriberID, String callerID, String deSelectedBy, String setDate, int rbtType, int songStatus, int fromTime, int toTime, String refID)
	{
		Statement stmt = null;
		ResultSet results = null;
		
		String setTimeStr = "TO_DATE('" + setDate + "','yyyyMMddhh24miss') ";
		
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
		try
		{
			if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date tempDate = sdf.parse(setDate);
				String tempDateStr = sdf2.format(tempDate);
				setTimeStr = "TIMESTAMP('" + tempDateStr + "') ";
			}

			String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID+"'" 
					+ " AND "+ CALLER_ID_COL+ getNullForWhere(callerID)
					+ " AND "+ SET_TIME_COL + " <= " + setTimeStr + " AND " +FROM_TIME_COL + " = " + fromTime 
					+ " AND " + TO_TIME_COL + " = " + toTime + " AND " + STATUS_COL + " = " + songStatus
					+ " AND "+ INTERNAL_REF_ID_COL +" != '"+refID+"'"; 
				
			if(deSelectedBy != null)
				query += " AND "+DESELECTED_BY_COL +"='"+deSelectedBy+"'";
				
			if(m_rrbtOn) 
				query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
				
			query+=" ORDER BY " + SET_TIME_COL +" DESC";

			logger.info("Executing query: " + query);
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatusList.add(getSubscriberStatusFromRS(results));
			}
		}
		catch(Exception se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}
	
	static SubscriberStatus getSubscriberLatestActiveSelection(Connection conn,String subscriberID){
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		SubscriberStatus latestsubscriberStatus = null;
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
				+ " AND "+ SEL_STATUS_COL +" IN('A','N','G','B') ORDER BY "+ SET_TIME_COL +" DESC";
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next()){
				latestsubscriberStatus = getSubscriberStatusFromRS(results);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return latestsubscriberStatus;
	}

	static SubscriberStatus[] getAllActiveSubscriberSettings(Connection conn, String subscriberID, int rbtType)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
			+ SET_TIME_COL + " <= " + SAPDB_SYSDATE +" AND " + END_TIME_COL + " > "+SAPDB_SYSDATE;
			if(m_rrbtOn) 
                query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

			if (RBTParametersUtils.getParamAsBoolean(COMMON, "GET_SELECTIONS_BY_SET_TIME", "FALSE"))
				query+=" ORDER BY " + SET_TIME_COL + ", "+ CALLER_ID_COL +", "+ STATUS_COL;
			else
				query+=" ORDER BY " + CALLER_ID_COL +", "+ STATUS_COL +", "+ SET_TIME_COL;
			
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
			+ SET_TIME_COL + " <= "+MYSQL_SYSDATE+" AND " + END_TIME_COL + " > "+MYSQL_SYSDATE;
			if(m_rrbtOn) 
                query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

			if (RBTParametersUtils.getParamAsBoolean(COMMON, "GET_SELECTIONS_BY_SET_TIME", "FALSE"))
				query+=" ORDER BY " + SET_TIME_COL + ", "+ CALLER_ID_COL +", "+ STATUS_COL;
			else
				query+=" ORDER BY " + CALLER_ID_COL +", "+ STATUS_COL +", "+ SET_TIME_COL;
		}

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatusList.add(getSubscriberStatusFromRS(results));
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}
	
	//vikrant
	static SubscriberStatus[] getAllActiveSubscriberSettingsbyStatus(Connection conn, String msisdn, String type,String selstatus,String id) throws Exception
	{
		logger.info("--> Inside getAllActiveSubscriberSettingsbyStatus ");
		StringBuffer query = new StringBuffer();
		Statement stmt = null;
		ResultSet results = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
		
		if (id != null && !id.isEmpty() && (type == null || type.isEmpty())) 
			throw new Exception("INVALID_PARAMETER");
		 if(id != null)
		{
			if (type.equalsIgnoreCase(CallingPartyType.GROUP.toString()))	
				id = "G"+id;
		}
		
		if(selstatus != null && !selstatus.isEmpty() && selstatus.equalsIgnoreCase("deactive")){
			selstatus="deactive";
		}else if(selstatus != null && !selstatus.isEmpty() && selstatus.equalsIgnoreCase("all")){
			selstatus="all";
		}else {
			selstatus="active";
		}
		
		
		
		query.append("SELECT * FROM ").append(TABLE_NAME)
		.append(" WHERE ").append(SUBSCRIBER_ID_COL)
		.append(" = ").append("'").append(msisdn).append("'");

		if (id != null) {
			query.append(" AND ").append(CALLER_ID_COL).append(" = ")
			.append("'").append(id).append("'");
		} else if (type != null && !type.equalsIgnoreCase("DEFAULT")) {
			query.append(" AND ").append(CALLER_ID_COL);
			if (type.equalsIgnoreCase("GROUP"))
				query.append(" LIKE ").append("'").append("G%").append("'");
			else
				query.append(" NOT LIKE ").append("'").append("G%").append("'");

			query.append(" AND ").append(CALLER_ID_COL).append(" IS NOT NULL");
		}
		else if(type != null){
			query.append(" AND ").append(CALLER_ID_COL).append(" IS NULL");
		}

	/*	
		if(categoryID > -1){
			query.append(" AND ").append(CATEGORY_ID_COL).append(" = ").append(categoryID);
		}
		
		if(wavFileName != null){
			query.append(" AND ").append(SUBSCRIBER_WAV_FILE_COL).append("'").append(wavFileName).append("'");
		}
		
		if(status > 0){
			query.append(" AND ").append(STATUS_COL).append(" = ").append(status);
		}
		*/
		if(selstatus != null && selstatus.equalsIgnoreCase("all")){
			query.append(" AND ").append(SEL_STATUS_COL).append(" IN ")
			.append("(").append("'W',").append("'A',").append("'N',")
			.append("'B',").append("'D',").append("'P',").append("'X',").append("'G',").append("'Z'").append(" )");
		}else if(selstatus != null && selstatus.equalsIgnoreCase("deactive")){
			query.append(" AND ").append(SEL_STATUS_COL).append(" IN ")
			.append("(").append("'D',").append("'P',").append("'X'").append(" )");
		}else{
			query.append(" AND ").append(SEL_STATUS_COL).append(" IN ")
			.append("(").append("'W',").append("'A',").append("'N',")
			.append("'B'").append(" )");
		}
		
	/*	if(endTime != null){
			query.append(" AND ").append(END_TIME_COL).append(" > SYSDATE() ");
		}
	*/	
		query.append(" ORDER BY ").append(SET_TIME_COL);

		final String finalQuery = query.toString();
		
		logger.info("--> Executing query: " + query);
		
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(finalQuery);
			while(results.next())
			{
				subscriberStatusList.add(getSubscriberStatusFromRS(results));
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}


	/*subscription manager changes*/
	static SubscriberStatus smSubscriberSelections(Connection conn, String subID, String callID, int st, int rbtType)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "+CALLER_ID_COL + getNullForWhere(callID) + " AND ";
		query = query + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " +  STATUS_COL + " = " + st ;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
		query+= " ORDER BY " + SET_TIME_COL + " DESC ";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
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
		return subscriberStatus;
	}

	static SubscriberStatus [] getSubscriberDeactiveRecords(Connection conn, String subID, String code)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID 
			+ "' AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " <= "+sysDateStr
			+ " AND " + STATUS_COL + " NOT IN (" + code + ") ORDER BY " + END_TIME_COL + " DESC";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	static int getSubscriberBouquetCount(Connection conn, String subID, String classType)
	{
		Statement stmt = null;
		RBTResultSet results = null;
		int count = 0;

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "SELECT COUNT(DISTINCT category_id) FROM rbt_subscriber_selections WHERE subscriber_id = '" + subID + 
		"' AND set_time <= "+sysDateStr+" AND end_time >= "+sysDateStr+" AND class_type ='" + classType + "' AND category_id in (SELECT category_id FROM rbt_categories WHERE category_type = 0)";  

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while(results.next())
			{
				count = results.getInt(1);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return 0;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return count;
	}

	static void setSetTime(Connection conn, String subscriberID, Date date, int rbtType)
	{
		Statement stmt = null;
		String startTimeStr = sqlTime(date); 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			startTimeStr = mySQLDateTime(date);

		String query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL
		+ " = " + startTimeStr + " WHERE "
		+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + NEXT_CHARGING_DATE_COL + " IS NULL ";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return;
	}

	static int getSubscriberCount(Connection conn, String subID, String classType)
	{
		Statement stmt = null;
		RBTResultSet results = null;
		int count = 0;
		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "SELECT COUNT(DISTINCT subscriber_wav_file) FROM rbt_subscriber_selections WHERE subscriber_id = '" + subID + 
		"' AND set_time <= "+sysDateStr+" AND end_time >= "+sysDateStr+" AND class_type ='" + classType +
		"' AND category_id in (SELECT category_id FROM rbt_categories WHERE category_type != 0)";  

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while(results.next())
			{
				count = results.getInt(1);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return 0;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return count;
	}

	static int getSubscriberCurrentSelections(Connection conn, String subID, String classType)
	{
		Statement stmt = null;
		RBTResultSet results = null;
		int count = 0;
		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		String query = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID 
			+ "' AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr
			+ " AND " + CLASS_TYPE_COL + " = '" + classType + "'";   

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while(results.next())
			{
				count = results.getInt(1);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return 0;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return count;
	}

	static SubscriberStatus getSubscriberFile(Connection conn, String subID, String callID,
			boolean isPrepaid, String type, String feedFile)
	{
		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		String subscriberID = null;
		String callerID = null;
		int categoryID = -1;
		String subscriberWavFile = null;
		Date setTime = null;
		Date startTime = null;
		Date endTime = null;
		int status = -1;
		String classType = null;
		String selectedBy = null;
		String selectionInfo = null;
		Date nextChargingDate = null;
		String prepaid = null;
		int fromTime = -1;
		int toTime = -1;
		String sel_status = null;
		String deSelectedBy = null;
		String oldClassType = null;
		String selInterval = null;
		String refID = null;
		String circleId = null;
		int categoyType = -1;
		int rbtType = -1;
		char loopStatus;
		String udpId = null;
		SubscriberStatusImpl subscriberStatus = null;
		DateFormat dateFormat = new SimpleDateFormat("HH");
		int currentHour = Integer.parseInt(dateFormat.format(new Date(System.currentTimeMillis())));

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		if(type != null && type.equalsIgnoreCase("ALL"))
		{
			if(callID == null)
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND " + CALLER_ID_COL + " IS NULL AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ END_TIME_COL + " > " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL + " ORDER BY "
						+ STATUS_COL + " DESC," + SET_TIME_COL + " DESC ";
			else
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND ( ( " + CALLER_ID_COL + " = " + "'" + callID
						+ "') OR ( " + CALLER_ID_COL + " IS NULL )) AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ END_TIME_COL + " > " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL + " ORDER BY "
						+ STATUS_COL + " DESC," + SET_TIME_COL + " DESC ";
		}
		else if(type != null && (type.equalsIgnoreCase("PREPAID") || type.equalsIgnoreCase("POSTPAID")))
		{
			if (callID == null)
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND " + CALLER_ID_COL + " IS NULL AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ END_TIME_COL + " > " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL;
			else
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND ( ( " + CALLER_ID_COL + " = " + "'" + callID
						+ "') OR ( " + CALLER_ID_COL + " IS NULL )) AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ END_TIME_COL + " > " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL;

			if (isPrepaid && type != null && type.equalsIgnoreCase("POSTPAID")) {
				query = query + " AND " + NEXT_CHARGING_DATE_COL
						+ " IS NOT NULL ORDER BY " + STATUS_COL + " DESC,"
						+ SET_TIME_COL + " DESC ";
			}
			if (!isPrepaid && type != null && type.equalsIgnoreCase("POSTPAID")) {
				query = query + " ORDER BY " + STATUS_COL + " DESC,"
						+ SET_TIME_COL + " DESC ";
			}
			if (isPrepaid && type != null && type.equalsIgnoreCase("PREPAID")) {
				query = query + " ORDER BY " + STATUS_COL + " DESC,"
						+ SET_TIME_COL + " DESC ";
			}
			if (!isPrepaid && type != null && type.equalsIgnoreCase("PREPAID")) {
				query = query + " AND " + NEXT_CHARGING_DATE_COL
						+ " IS NOT NULL ORDER BY " + STATUS_COL + " DESC,"
						+ SET_TIME_COL + " DESC ";
			}
		}
		else
		{
			if(callID == null)
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'" + " AND " + CALLER_ID_COL + " IS NULL AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " + NEXT_CHARGING_DATE_COL + " IS NOT NULL ORDER BY " + STATUS_COL + " DESC," + SET_TIME_COL + " DESC ";
			else
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'" + " AND ( ( " + CALLER_ID_COL + " = " + "'" + callID + "') OR ( " + CALLER_ID_COL + " IS NULL )) AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " + NEXT_CHARGING_DATE_COL + " IS NOT NULL ORDER BY " + STATUS_COL + " DESC," + SET_TIME_COL + " DESC ";
		}

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while(results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				callerID = results.getString(CALLER_ID_COL);
				categoryID = results.getInt(CATEGORY_ID_COL);
				subscriberWavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				startTime = results.getTimestamp(START_TIME_COL);
				endTime = results.getTimestamp(END_TIME_COL);
				status = results.getInt(STATUS_COL);
				classType = results.getString(CLASS_TYPE_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				selectionInfo = results.getString(SELECTION_INFO_COL);
				nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
				prepaid = results.getString(PREPAID_YES_COL);
				fromTime = results.getInt(FROM_TIME_COL);
				toTime = results.getInt(TO_TIME_COL);
				sel_status = results.getString(SEL_STATUS_COL);
				deSelectedBy = results.getString(DESELECTED_BY_COL);
				oldClassType = results.getString(OLD_CLASS_TYPE_COL);
				categoyType = results.getInt(CATEGORY_TYPE_COL);
				loopStatus = results.getString(LOOP_STATUS_COL).charAt(0);
				selInterval = results.getString(SEL_INTERVAL_COL);
				refID = results.getString(INTERNAL_REF_ID_COL);
				circleId = results.getString(CIRCLE_ID_COL);
				if(m_rrbtOn) 
                    rbtType = results.getInt(SEL_TYPE_COL); 

				udpId = results.getString(UDP_ID_COL);
				if((status == 1 || status == 80) && callerID == null && subscriberStatus == null)
				{
					subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID, categoryID,
							subscriberWavFile, setTime, startTime, endTime, status, classType, selectedBy,
							selectionInfo, nextChargingDate, prepaid, fromTime, toTime, sel_status,
							deSelectedBy, oldClassType, categoyType, loopStatus,rbtType,selInterval, null, null, circleId, udpId);
				}
				if(status == 99)
				{
					subscriberStatus = null;
					break;
				}
				if(status == 90 && feedFile != null)
				{
					subscriberStatus = null;
					break;
				}
				else if(status != 90 && callID == null)
				{
					subscriberStatus = null;
					break;
				}
				else if(status != 90 && callerID != null && callID != null && callID.equals(callerID))
				{
					subscriberStatus = null;
					break;
				}

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
		if(subscriberID == null)
		{
			return null;
		}
		if(subscriberStatus == null)
		{
			if(status == 90)
				subscriberWavFile = feedFile;
			subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID, categoryID,
					subscriberWavFile, setTime, startTime, endTime, status, classType, selectedBy,
					selectionInfo, nextChargingDate, prepaid, fromTime, toTime, sel_status,
					deSelectedBy, oldClassType, categoyType, LOOP_STATUS_OVERRIDE_FINAL,rbtType,selInterval, refID, null, circleId, udpId);
		}
		return subscriberStatus;
	}

	static SubscriberStatus getSubscriberFile(Connection conn, String subID, String callID,
			boolean isPrepaid, String type, boolean isMemCacheModel, String feedFile, int rbtType)
	{
		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		String subscriberID = null;
		String callerID = null;
		int categoryID = -1;
		String subscriberWavFile = null;
		Date setTime = null;
		Date startTime = null;
		Date endTime = null;
		int status = -1;
		String classType = null;
		String selectedBy = null;
		String selectionInfo = null;
		Date nextChargingDate = null;
		String prepaid = null;
		int fromTime = -1;
		int toTime = -1;
		String sel_status = null;
		String selInterval = null;
		String deSelectedBy = null;
		String oldClassType = null;
		String refID = null;
		String extraInfo = null;
		int categoyType = -1;
		char loopStatus;
		String circleId = null;
		String udpId = null;
		
		SubscriberStatusImpl subscriberStatus = null;
		DateFormat dateFormat = new SimpleDateFormat("HH");
		int currentHour = Integer.parseInt(dateFormat.format(new Date(System.currentTimeMillis())));

		String sysDateStr = "SYSDATE"; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";

		if (type != null && type.equalsIgnoreCase("ALL")) {
			if (callID == null)
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND " + CALLER_ID_COL + " IS NULL AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL + " AND "
						+ END_TIME_COL + " > "+sysDateStr;
			else
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND ( ( " + CALLER_ID_COL + " = " + "'" + callID
						+ "') OR ( " + CALLER_ID_COL + " IS NULL )) AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL + " AND "
						+ END_TIME_COL + " > "+sysDateStr;
		} else if (type != null
				&& (type.equalsIgnoreCase("PREPAID") || type
						.equalsIgnoreCase("POSTPAID"))) {
			if (callID == null)
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND " + CALLER_ID_COL + " IS NULL AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL + " AND "
						+ END_TIME_COL + " > "+sysDateStr;
			else
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND ( ( " + CALLER_ID_COL + " = " + "'" + callID
						+ "') OR ( " + CALLER_ID_COL + " IS NULL )) AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL + " AND "
						+ END_TIME_COL + " > "+sysDateStr;

			if (isPrepaid && type != null && type.equalsIgnoreCase("POSTPAID")) {
				query = query + " AND " + NEXT_CHARGING_DATE_COL
						+ " IS NOT NULL";
			}
			if (!isPrepaid && type != null && type.equalsIgnoreCase("PREPAID")) {
				query = query + " AND " + NEXT_CHARGING_DATE_COL
						+ " IS NOT NULL";
			}
		}
		else {
			if (callID == null)
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND " + CALLER_ID_COL + " IS NULL AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL + " AND "
						+ END_TIME_COL + " > "+sysDateStr+ " AND " 
						+ NEXT_CHARGING_DATE_COL + " IS NOT NULL";
			else
				query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
						+ " AND ( ( " + CALLER_ID_COL + " = " + "'" + callID
						+ "') OR ( " + CALLER_ID_COL + " IS NULL )) AND "
						+ SET_TIME_COL + " <= " + sysDateStr + " AND "
						+ FROM_TIME_COL + " <= " + currentHour + " AND "
						+ currentHour + " <= " + TO_TIME_COL + " AND "
						+ END_TIME_COL + " > "+sysDateStr+ " AND "
						+ NEXT_CHARGING_DATE_COL + " IS NOT NULL";
		}
		if(isMemCacheModel)
			query = query + " AND "+ LOOP_STATUS_COL + " NOT IN ('o','O','l','L')";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 		
		query = query + " ORDER BY " + STATUS_COL + " DESC," + SET_TIME_COL + " DESC ";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while(results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				callerID = results.getString(CALLER_ID_COL);
				categoryID = results.getInt(CATEGORY_ID_COL);
				subscriberWavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				startTime = results.getTimestamp(START_TIME_COL);
				endTime = results.getTimestamp(END_TIME_COL);
				status = results.getInt(STATUS_COL);
				classType = results.getString(CLASS_TYPE_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				selectionInfo = results.getString(SELECTION_INFO_COL);
				nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
				prepaid = results.getString(PREPAID_YES_COL);
				fromTime = results.getInt(FROM_TIME_COL);
				toTime = results.getInt(TO_TIME_COL);
				sel_status = results.getString(SEL_STATUS_COL);
				deSelectedBy = results.getString(DESELECTED_BY_COL);
				oldClassType = results.getString(OLD_CLASS_TYPE_COL);
				categoyType = results.getInt(CATEGORY_TYPE_COL);
				loopStatus = results.getString(LOOP_STATUS_COL).charAt(0);
				selInterval = results.getString(SEL_INTERVAL_COL);
				refID = results.getString(INTERNAL_REF_ID_COL);
				extraInfo = results.getString(EXTRA_INFO_COL); 
				circleId = results.getString(CIRCLE_ID_COL);;
				if(m_rrbtOn) 
                    rbtType = results.getInt(SEL_TYPE_COL); 
				udpId = results.getString(UDP_ID_COL);

				if((status == 1 || status == 80) && callerID == null && subscriberStatus == null)
				{
					subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID, categoryID,
							subscriberWavFile, setTime, startTime, endTime, status, classType, selectedBy,
							selectionInfo, nextChargingDate, prepaid, fromTime, toTime, sel_status,
							deSelectedBy, oldClassType, categoyType, loopStatus,rbtType,selInterval, refID, extraInfo, circleId, udpId);
				}
				if(status == 99)
				{
					subscriberStatus = null;
					break;
				}
				if(status == 90 && feedFile != null)
				{
					subscriberStatus = null;
					break;
				}
				else if(status != 90 && callID == null)
				{
					subscriberStatus = null;
					break;
				}
				else if(status != 90 && callerID != null && callID != null && callID.equals(callerID))
				{
					subscriberStatus = null;
					break;
				}

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
		if(subscriberID == null)
		{
			return null;
		}
		if(subscriberStatus == null)
		{
			if(status == 90)
				subscriberWavFile = feedFile;
			subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID, categoryID,
					subscriberWavFile, setTime, startTime, endTime, status, classType, selectedBy,
					selectionInfo, nextChargingDate, prepaid, fromTime, toTime, sel_status,
					deSelectedBy, oldClassType, categoyType, LOOP_STATUS_OVERRIDE_FINAL,rbtType,selInterval, refID, extraInfo, circleId, udpId);
		}
		return subscriberStatus;
	}

	static boolean remove(Connection conn, String subID, String callerID)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		if(callerID == null)
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'" + " AND " + CALLER_ID_COL + " IS NULL ";
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'" + " AND " + CALLER_ID_COL + " = " + "'" + callerID + "'";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}

	 static boolean removeAllSelections(Connection conn, String subID, int rbtType)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n >= 1);
	}

	static int removeOldSelections(Connection conn, float duration, boolean useSM)
	{
		int n = -1;
		Statement stmt = null;
		int count = 0;
		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL
				+ " = '" + STATE_DEACTIVATED + "' AND " + END_TIME_COL
				+ " <= ( now() -" + duration + ") AND " + SET_TIME_COL
				+ "<= (now()-" + duration + ")";
		
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL
			+ " = '" + STATE_DEACTIVATED + "' AND " + END_TIME_COL
			+ " <= TIMESTAMPADD(DAY,-" + duration + ",SYSDATE()) AND " + SET_TIME_COL
			+ "<= TIMESTAMPADD(DAY,-" + duration + ",SYSDATE()) LIMIT 1000";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				int rowCount = 1;
				int runCycle = 0;
				logger.info("before while loop. ");
				while(rowCount > 0)
				{
					runCycle++;
					logger.info("runCycle is :"+runCycle);
					logger.info("RBT::going to run delete query again as rowCount is greater than 0. rowCount = "+rowCount);
					rowCount = stmt.executeUpdate(query);
					count += rowCount;
					logger.info("After running query. No. of rows deleted is :"+rowCount);
				}
				logger.info("After while loop. As rowCount is :"+rowCount+ " and total runCycle is "+runCycle);
			}
			else
			{
				stmt.executeUpdate(query);
				n = stmt.getUpdateCount();
			}
			
			logger.info("RBT::deleted " + n + " old selections");
		}
		catch(SQLException se)
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

	static boolean updateSelection(Connection conn, String subscriberID, String subscriberWavFile, 
			float fromTime, float toTime, String selInterval, String internalRefId)
	{
		if(subscriberWavFile == null)
		{
			return false;
		}
		int n = -1;
		Statement stmt = null;
		String query = "UPDATE " + TABLE_NAME + " SET " +
			SUBSCRIBER_WAV_FILE_COL + " =  '" + subscriberWavFile + "'," + FROM_TIME_COL + " = " + fromTime + "," + TO_TIME_COL + " = " + toTime;
		if(selInterval != null)
			query = query + "," + SEL_INTERVAL_COL + " = '" + selInterval + "'" ;
		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + INTERNAL_REF_ID_COL + " = '" + internalRefId + "'";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}
	

	static boolean updateSelection(Connection conn, String subscriberID,
			String callerID, String endDate, String wavFile, boolean useSM)
	{
		int n = -1;
		Statement stmt = null;
		String endTime = "TO_DATE('20370101','yyyyMMdd')";
		String smString = "";
		String endDateCond = " to_char(" + END_TIME_COL+ ",'yyyy-MM-dd hh24:mi:ss') "; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			endDateCond = " DATE_FORMAT(" + END_TIME_COL+ ",'%Y-%m-%d %H:%i:%s') ";
		
		if (useSM)
		{
			smString = " , " + SEL_STATUS_COL + " = 'W'";
		}
		String query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL + " = "
		+ endTime + ", " + NEXT_CHARGING_DATE_COL + " = NULL"
		+ smString + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
		+ subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere(callerID)+" AND "
		+ SUBSCRIBER_WAV_FILE_COL + " = '" + wavFile
		+ "' and "+endDateCond+" = '" + endDate + "'";
	
		logger.info("Executing query: " + query);
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
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n == 1);
	}

	static SubscriberStatus [] getActiveStatusSubscribers(Connection conn, int st, boolean checkStartTime, int rbtType)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " + STATUS_COL + " = " + st;
		if(checkStartTime)
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + START_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " + STATUS_COL + " = " + st;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	/*subscription manager changes*/
	static boolean smSubscriptionRenewalFailure(Connection conn, String subscriberID, String deactivatedBy, String type, String circleId)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;
		String circleIdQuery = "";
		String nextDate = "TO_DATE('20371231','yyyyMMdd')";
		String sysDateStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = 	"TIMESTAMP('2037-12-31')";
			sysDateStr = "SYSDATE()";
		}
		if(deactivatedBy != null && (deactivatedBy.equalsIgnoreCase("NA") || deactivatedBy.equalsIgnoreCase("NEF") || deactivatedBy.equalsIgnoreCase("AF") || deactivatedBy.equalsIgnoreCase("RF")))
		{
			nextDate = "TO_DATE('20351231','yyyyMMdd')";
			if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextDate = 	"TIMESTAMP('2035-12-31')";
			
		}
		if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
		String prepaid = "n";
		if(type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		  query = "UPDATE " + TABLE_NAME + " SET " ;
		  if (deactivatedBy != null && (deactivatedBy.equalsIgnoreCase("NA") || deactivatedBy.equalsIgnoreCase("AF")))
           {
                   query = query + START_TIME_COL + " = "+ sysDateStr +" , ";
           }

		  query += END_TIME_COL + " = " + sysDateStr + " , " +
		NEXT_CHARGING_DATE_COL + " = " + nextDate + ", " +
		PREPAID_YES_COL + " = '" + prepaid + "' ," +
		 LOOP_STATUS_COL + " = '" + LOOP_STATUS_EXPIRED + "'," +
		SEL_STATUS_COL + " = '" + STATE_DEACTIVATED + "', "  + DESELECTED_BY_COL + " = '"+deactivatedBy+"'" +circleIdQuery+
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' , '" + STATE_ACTIVATION_PENDING + "', '" + STATE_ACTIVATED + "' , '" + STATE_ACTIVATION_ERROR + "', '" + STATE_BASE_ACTIVATION_PENDING + "', '" + STATE_CHANGE + "', '" + STATE_REQUEST_RENEWAL + "', 'D', 'P', 'F', '" + STATE_ACTIVATION_GRACE + "','"+STATE_SUSPENDED+"')";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static SubscriberStatus smGetActiveSelection(Connection conn, String subID, String callID, int st, int fTime, int tTime)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		String sysDateStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		String query = "SELECT * FROM " + TABLE_NAME + 
			" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subID + "' AND " + CALLER_ID_COL + getNullForWhere(callID)+" AND " 
			+ SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " + 
			NEXT_CHARGING_DATE_COL + " IS NOT NULL AND " + FROM_TIME_COL + " = " + fTime + " AND " + 
			TO_TIME_COL + " = " + tTime + " AND " + STATUS_COL + " = " + st;
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
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
		return subscriberStatus;
	}

	static boolean smDeactivateOldSelection(Connection conn, String subscriberID, String callerID, int status, String setDate, int fromTime, int toTime, int rbtType, String selInterval, String refID,boolean isDirectDeactivation)
	{
		int n = -1;
		Statement stmt = null;
		
		String selStatus = STATE_TO_BE_DEACTIVATED;
		char loopStatus = LOOP_STATUS_EXPIRED;
		
		if(isDirectDeactivation){
			selStatus = STATE_DEACTIVATED;
			loopStatus = LOOP_STATUS_EXPIRED_INIT;
		}

		try
		{
		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		String sysDateStr = "SYSDATE";
		String setTimeStr = "TO_DATE('" + setDate + "','yyyyMMddhh24miss') ";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr = "SYSDATE()";
			nextChargingDate = "TIMESTAMP('2037-12-31')";
			if (setDate != null)
			{	
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date tempDate = sdf.parse(setDate);
				String tempDateStr = sdf2.format(tempDate);
				setTimeStr = "TIMESTAMP('" + tempDateStr + "') ";
			}
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " +
		END_TIME_COL + " = "+ sysDateStr + " , " + 
		NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", "+
		DESELECTED_BY_COL + " = 'SM' , "+
		SEL_STATUS_COL + " = '" + selStatus + "', " +
		LOOP_STATUS_COL + " = '" + loopStatus + "' " +
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CALLER_ID_COL + getNullForWhere(callerID); 
		if(setDate != null)
			query = query + " AND " + SET_TIME_COL + " <= "+setTimeStr ;
		
		query = query + " AND " + 
		SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING + "','" + 
		STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" + STATE_BASE_ACTIVATION_PENDING + "','" + 
		STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" + STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"') AND " +
		FROM_TIME_COL + " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime + " AND " + 
		STATUS_COL + " = " + status;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		if(selInterval != null)
			query += " AND " + SEL_INTERVAL_COL + " = " + sqlString(selInterval);
    
		if(refID != null)
			query += " AND (" + INTERNAL_REF_ID_COL + " IS NULL OR " + INTERNAL_REF_ID_COL + " != " + sqlString(refID) + ")";

		String giftChargeClasses = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON, "OPTIN_GIFT_CHARGE_CLASS", null);
		if(giftChargeClasses != null) {
			String classTypes = giftChargeClasses.replace(",", ",'");
			query += "AND " + CLASS_TYPE_COL + " NOT IN ('" + classTypes + "')";
		}
		if( RBTParametersUtils.getParamAsBoolean("COMMON",
				"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")) {
		     query += " AND ("+ EXTRA_INFO_COL + " NOT LIKE '%PROV_REF_ID%' OR "+ EXTRA_INFO_COL + " IS NULL)";
		}
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch(Exception se)
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

	static boolean smDeactivateOldSuspendedSelections(Connection conn, String subscriberID, String callerID, int status, String setDate, int fromTime, int toTime, int rbtType, String selInterval, String refID)
	{
		Statement stmt = null;
		try
		{
			String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
			String sysDateStr = "SYSDATE";
			String setTimeStr = "TO_DATE('" + setDate + "','yyyyMMddhh24miss') ";
			if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				sysDateStr = "SYSDATE()";
				nextChargingDate = "TIMESTAMP('2037-12-31')";
				if (setDate != null)
				{
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date tempDate = sdf.parse(setDate);
					String tempDateStr = sdf2.format(tempDate);
					setTimeStr = "TIMESTAMP('" + tempDateStr + "') ";
				}
			}

			String query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL
					+ " = " + sysDateStr + " , " + NEXT_CHARGING_DATE_COL
					+ " = " + nextChargingDate + ", " + DESELECTED_BY_COL
					+ " = 'SM' , " + SEL_STATUS_COL + " = '"
					+ STATE_TO_BE_DEACTIVATED + "', " + LOOP_STATUS_COL
					+ " = '" + LOOP_STATUS_EXPIRED + "' " + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
					+ CALLER_ID_COL + getNullForWhere(callerID);

			if (setDate != null)
				query = query + " AND " + SET_TIME_COL + " <= " + setTimeStr;

			query = query + " AND " + SEL_STATUS_COL + " IN ('"
					+ STATE_ACTIVATION_GRACE + "', '" + STATE_SUSPENDED_INIT
					+ "' , '" + STATE_SUSPENDED + "') AND " + FROM_TIME_COL
					+ " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime
					+ " AND " + STATUS_COL + " = " + status;

			if (m_rrbtOn)
				query += " AND " + SEL_TYPE_COL + " = " + rbtType;

			if (selInterval != null)
				query += " AND " + SEL_INTERVAL_COL + " = " + sqlString(selInterval);

			if (refID != null)
				query += " AND (" + INTERNAL_REF_ID_COL + " IS NULL OR " + INTERNAL_REF_ID_COL + " != " + sqlString(refID) + ")";

			logger.info("Executing query: " + query);
			RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch (Exception se)
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

	static boolean smDeactivateOldSelectionBasedOnRefID(Connection conn, String subscriberID, String callerID, String setDate, int fromTime, int toTime, int rbtType, String selInterval, String refID, List<String> refIDList)
	{
		int n = -1;
		Statement stmt = null;
		try
		{
			String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
			String sysDateStr = "SYSDATE";
			String setTimeStr = "TO_DATE('" + setDate + "','yyyyMMddhh24miss') ";
			if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				sysDateStr = "SYSDATE()";
				nextChargingDate = "TIMESTAMP('2037-12-31')";
				if (setDate != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date tempDate = sdf.parse(setDate);
					String tempDateStr = sdf2.format(tempDate);
					setTimeStr = "TIMESTAMP('" + tempDateStr + "') ";
				}
			}

			String refIDStr = "";
			for (int i = 0; i < refIDList.size(); i++)
			{
				if (i < refIDList.size() - 1)
					refIDStr += "'" + refIDList.get(i) + "',";
				else
					refIDStr += "'" + refIDList.get(i) + "'";
			}

			String query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL + " = "
					+ sysDateStr + " , " + NEXT_CHARGING_DATE_COL + " = "
					+ nextChargingDate + ", " + DESELECTED_BY_COL
					+ " = 'SM' , " + SEL_STATUS_COL + " = '"
					+ STATE_TO_BE_DEACTIVATED + "', " + LOOP_STATUS_COL
					+ " = '" + LOOP_STATUS_EXPIRED + "' " + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
					+ CALLER_ID_COL + getNullForWhere(callerID);
			if (setDate != null)
				query = query + " AND " + SET_TIME_COL + " <= " + setTimeStr;

			query = query + " AND " + SEL_STATUS_COL + " IN ('"
					+ STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING
					+ "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR
					+ "','" + STATE_BASE_ACTIVATION_PENDING + "','"
					+ STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','"
					+ STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"
					+ STATE_SUSPENDED + "') AND " + FROM_TIME_COL + " = "
					+ fromTime + " AND " + TO_TIME_COL + " = " + toTime;

			if (m_rrbtOn)
				query += " AND " + SEL_TYPE_COL + " = " + rbtType;

			if (selInterval != null)
				query += " AND " + SEL_INTERVAL_COL + " = " + sqlString(selInterval);

			if (refID != null)
				query += " AND " + INTERNAL_REF_ID_COL + " IN (" + refIDStr + ")";

    		logger.info("Executing query: " + query);
    		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (Exception se)
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
		
	static boolean smUpdateAndDeactivateOldSelection(Connection conn, String subscriberID, ArrayList<String> list ,String setDate, int rbtType, String extraInfoStr)
	{
		int n = -1;
		Statement stmt = null;

		try
			{
			String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
			String sysDateStr = "SYSDATE";
			String setTimeStr = "TO_DATE('" + setDate + "','yyyyMMddhh24miss') ";
			if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				sysDateStr = "SYSDATE()";
				nextChargingDate = "TIMESTAMP('2037-12-31')";
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date tempDate = sdf.parse(setDate);
				String tempDateStr = sdf2.format(tempDate);
				setTimeStr = "TIMESTAMP('" + tempDateStr + "') ";
			}
			
	    	String refIDStr = "";
	    	for (int i=0; i<list.size();i++) {
				if(i < list.size()-1 )
					refIDStr += "'"+list.get(i)+"',";
				else
					refIDStr += "'"+list.get(i)+"'";
			}
			
			String query = "UPDATE " + TABLE_NAME + " SET " +
			END_TIME_COL + " = "+ sysDateStr + " , " + 
			NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", "+
			DESELECTED_BY_COL + " = 'SM' , "+
			SEL_STATUS_COL + " = '" + STATE_TO_BE_DEACTIVATED + "', " +
			LOOP_STATUS_COL + " = '" + LOOP_STATUS_EXPIRED + "'";
			
			if (extraInfoStr != null)
				query += ", " + extraInfoStr;

			query += " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND "+
			INTERNAL_REF_ID_COL + " IN ("+refIDStr+")" + " AND " +
			SEL_STATUS_COL + " IN ('A','N','B','W','C','E','R','G','Z')";
				
			if(m_rrbtOn) 
	            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
	
    		logger.info("Executing query: " + query);
    		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
	
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch(Exception se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return(n > 0);
	}
	
	static boolean smDeactivateOtherUGSSelections(Connection conn, String subscriberID, String callerID, String type, int rbtType) 
    { 
            int n = -1; 
            Statement stmt = null; 

            String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')"; 
            String endDate = "SYSDATE";
            if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
    		{
            	nextChargingDate = "TIMESTAMP('2037-12-31')";
            	endDate = "SYSDATE()";
    		}
            String query = "UPDATE " + TABLE_NAME + " SET " + 
            END_TIME_COL + " = " + endDate + ", " + 
            NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", "+ 
            DESELECTED_BY_COL + " = 'SM' , "+ 
            SEL_STATUS_COL + " = '" + STATE_TO_BE_DEACTIVATED + "', " + 
            LOOP_STATUS_COL + " = '" + LOOP_STATUS_EXPIRED + "' " + 
            " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
            CALLER_ID_COL + getNullForWhere(callerID); 
            query = query + " AND " + 
            SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING + "','" + 
            STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" + STATE_BASE_ACTIVATION_PENDING + "','" + 
            STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" + STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"') AND " + 
            CATEGORY_TYPE_COL + " = 11 "; 

            if(m_rrbtOn) 
                    query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

    		logger.info("Executing query: " + query);
    		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
            try 
            { 
                stmt = conn.createStatement(); 
                stmt.executeUpdate(query); 
                n = stmt.getUpdateCount(); 
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
            return(n > 0); 
    } 

	// rajesh:TODO ADD NEW PARAMEETTER TO UPDATE C OR NOT
	static boolean smActivateNewSelection2(Connection conn,
			String subscriberID, String refID, Date nextChargingDate,
			Date startDate, String type, String classType, char newLoopStatus,
			String selInfo, int rbtType, String selInterval, String extraInfo
			, boolean updateSelStatus,String circleId)
	{
		int n = -1;
		Statement stmt = null;

		String nextDate = "SYSDATE";
		if(nextChargingDate != null)
			nextDate = sqlTime(nextChargingDate);
		String circleIdQuery = "";
        if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
		String startTime = "SYSDATE";
		if(startDate != null)
			startTime = sqlTime(startDate);
		String sysDateStr = "SYSDATE";
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE()";
			if(nextChargingDate != null)
				nextDate = mySQLDateTime(nextChargingDate);

			startTime = "SYSDATE()";
			if(startDate != null)
				startTime = mySQLDateTime(startDate);
			sysDateStr = "SYSDATE()";
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";
		}
		
		String prepaid = "n";
		if(type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		String query = "UPDATE " + TABLE_NAME + " SET "
				+ NEXT_CHARGING_DATE_COL + " = " + nextDate + ", "
				+ START_TIME_COL + " = " + startTime + ", " + PREPAID_YES_COL
				+ " = '" + prepaid + "', " + CLASS_TYPE_COL + " = '"
				+ classType + "', ";
		
		if(selInfo != null && selInfo.length() > 0) 
            query = query + SELECTION_INFO_COL + " = '" + selInfo + "', " ; 
		
//		IF NEW BOOLEAN IS TRUE, INSTEAD OF ACTIVATED, SHOULD BE STATE_CHANGE
		if(updateSelStatus) {
			query = query + SEL_STATUS_COL + " = '" + STATE_CHANGE + "', "
					+ LOOP_STATUS_COL + " = '" + newLoopStatus + "'";
		} else {
			query = query + SEL_STATUS_COL + " = '" + STATE_ACTIVATED + "', "
					+ LOOP_STATUS_COL + " = '" + newLoopStatus + "'";
		}

		if (extraInfo != null)
			query += ", " + EXTRA_INFO_COL + " = '" + extraInfo + "'";
		query += circleIdQuery;
		query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID
				+ "' AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID)
				+ " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED
				+ "' , '" + STATE_ACTIVATION_PENDING + "', '"
				+ STATE_ACTIVATION_ERROR + "', '" + STATE_CHANGE + "', '"
				+ STATE_ACTIVATION_GRACE + "') ";

		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		if(selInterval != null) 
            query += " AND " + SEL_INTERVAL_COL + " = " + sqlString(selInterval);

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static boolean smSelectionDeactivationSuccess(Connection conn, String subscriberID,
			String refID,char newLoopStatus, int rbtType,String circleId)
	{
		int n = -1;
		Statement stmt = null;
		String circleIdQuery = "";
        if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";
		
		String query = "UPDATE " + TABLE_NAME + " SET " +
		SEL_STATUS_COL + " = '" + STATE_DEACTIVATED + "', " + LOOP_STATUS_COL + " = " +
		sqlString(String.valueOf(newLoopStatus)) + circleIdQuery + " WHERE " + 
		SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID) +  " AND " + 
		SEL_STATUS_COL + " IN ('" + STATE_TO_BE_DEACTIVATED + "', '" + STATE_DEACTIVATION_PENDING + "', '" + STATE_DEACTIVATION_ERROR + "', '" + STATE_SUSPENDED + "', '" + STATE_SUSPENDED_INIT + "') ";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static boolean smDeactivateEmotionSelectionOnDefaultDeactivation(Connection conn, String subscriberID, String categoryType)
	{
		int n = -1;
		Statement stmt = null;

		String query = "UPDATE " + TABLE_NAME + " SET " +	SEL_STATUS_COL + " = '" + STATE_TO_BE_DEACTIVATED + "', " 
		+ LOOP_STATUS_COL + " = " +	sqlString(String.valueOf(LOOP_STATUS_EXPIRED_INIT)) + " WHERE " 
		+ SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND "+ CATEGORY_TYPE_COL +"= '"+categoryType +"'";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	/*	Deactivates the refunded selection and updates the extraInfo with REFUNDED = TRUE
	 */
	static boolean smDeactivateRefundedSelection(Connection conn, String subscriberID, String refID, char newLoopStatus, String callerID, int rbtType, String extraInfo )
	{
		int n = -1;
		String query = null;
		Statement stmt = null;
		try
		{
			String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
			String sysDateStr = "SYSDATE";
			if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				sysDateStr = "SYSDATE()";
				nextChargingDate = "TIMESTAMP('2037-12-31')";
			
			}
			query = "UPDATE " + TABLE_NAME + " SET " +
			END_TIME_COL + " = "+ sysDateStr + " , " + 
			NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", "+
			DESELECTED_BY_COL + " = '" + "SM" + "', " +
			SEL_STATUS_COL + " = '" + STATE_DEACTIVATED + "', " +
			LOOP_STATUS_COL + " = '" + newLoopStatus + "', " +
			EXTRA_INFO_COL + " = " + sqlString(extraInfo) + " " +
			" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND "+
			CALLER_ID_COL + getNullForWhere(callerID); 

			if(m_rrbtOn) 
				query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
			
			query += " AND " +INTERNAL_REF_ID_COL + " = " + sqlString(refID) ;

    		logger.info("Executing query: " + query);
    		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch(Exception se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return(n > 0);
	}
	
	static boolean smReactivateSelection(Connection conn, String subscriberID, String refID, String callerID, char loopStatus, int rbtType, String extraInfo, String selStatus )
	{
		int n = -1;
		String query = null;
		Statement stmt = null;
		try
		{
			if (selStatus == null)
				selStatus = STATE_TO_BE_ACTIVATED;

			String endDateStr = "TO_DATE('20371231','yyyyMMdd')";
			String sysDateStr = "SYSDATE";
			String startDateStr = "TO_DATE('20040101','yyyyMMdd')";
			if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				sysDateStr = "SYSDATE()";
				endDateStr = "TIMESTAMP('2037-12-31')";
				startDateStr = "TIMESTAMP('2004-01-01')";
			
			}
			query = "UPDATE " + TABLE_NAME + " SET " +
			END_TIME_COL + " = "+ endDateStr + " , " +
			START_TIME_COL + " = "+ startDateStr + " , " +
			NEXT_CHARGING_DATE_COL + " = " + sysDateStr + ", ";
			
			query += EXTRA_INFO_COL + " = " + sqlString(extraInfo) + ", " ;  
			
			query += SEL_STATUS_COL + " = '" + selStatus + "', " +
			LOOP_STATUS_COL + " = '" + loopStatus + "'" +
			" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "'"+
			" AND "+ INTERNAL_REF_ID_COL  +" = '"+refID+"'" ;
			if(m_rrbtOn) 
				query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
			

    		logger.info("Executing query: " + query);
    		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch(Exception se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return(n > 0);
	}

	static boolean smSelectionRenewalSuccess(Connection conn,
			String subscriberID, String refID, Date nextChargingDate,
			String type, String classType, String selectionInfo, int rbtType,
			String loopStatus,String circleId)
	{
		int n = -1;
		Statement stmt = null;

		String nextDate = "SYSDATE";
		if(nextChargingDate != null)
			nextDate = sqlTime(nextChargingDate);
		String circleIdQuery = "";
        if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
		String prepaid = "n";
		if(type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE()";
			if(nextChargingDate != null)
				nextDate = mySQLDateTime(nextChargingDate);
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " +
		SEL_STATUS_COL + " = '" + STATE_ACTIVATED + "', " +
		NEXT_CHARGING_DATE_COL + " = " + nextDate + ", " + 
		PREPAID_YES_COL + " = '" + prepaid + "', " +
		CLASS_TYPE_COL + " = " + sqlString(classType)+circleIdQuery;

		if(selectionInfo != null && selectionInfo.length() > 0)
			query +=  ", " + SELECTION_INFO_COL + " = '" + selectionInfo + "'";

		if(loopStatus != null && loopStatus.length() > 0)
			query +=  ", " + LOOP_STATUS_COL + " = '" + loopStatus + "'";

		query += " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + INTERNAL_REF_ID_COL + " = "+ sqlString(refID)+	
		" AND " + SEL_STATUS_COL + " in ('" + STATE_ACTIVATED + "' ,'" + STATE_REQUEST_RENEWAL + "', '"+STATE_SUSPENDED  + "', '"+STATE_SUSPENDED_INIT+"','"+STATE_ACTIVATION_ERROR+"')";
		
		
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static SubscriberStatus getADRBTSubscriberFile(Connection conn, String subID,String callID)
    {
        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String subscriberID = null;
        String callerID = null;
        int categoryID = -1;
        String subscriberWavFile = null;
        Date setTime = null;
        Date startTime = null;
        Date endTime = null;
        int status = -1;
        String classType = null;
        String selectedBy = null;
        String selectionInfo = null;
        Date nextChargingDate = null;
        String prepaid = null;
        int fromTime = -1;
        int toTime = -1;
        int rbtType = -1;
        String selInterval = null;
        String sel_status = null;
        String deSelectedBy = null;
        String oldClassType = null;
        String refID = null;
		String extraInfo = null;
        int catID  = 5;
        char loop = 'B';
        String circleId = null;
        
        DateFormat dateFormat = new SimpleDateFormat("HH");
        int currentHour = Integer.parseInt(dateFormat.format(new Date(System
                .currentTimeMillis())));
        String sysDateStr = "SYSDATE";
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	sysDateStr = "SYSDATE()";
        if (callID == null)
        {
            return new SubscriberStatusImpl(subscriberID,
                                     callerID, categoryID, "PLAY_AD", setTime,
                                     startTime, endTime, status, classType, selectedBy,
                                     selectionInfo, nextChargingDate, prepaid, fromTime,
                                     toTime, sel_status, deSelectedBy, oldClassType,5,'B',rbtType,selInterval, refID, extraInfo, circleId, null); 
        }
        else
            query = "SELECT * FROM " + TABLE_NAME + " WHERE "
                    + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'"
                    + " AND " + CALLER_ID_COL + " = " + "'" + callID
                    + "' AND " + SET_TIME_COL + " <= "+sysDateStr +" AND " + END_TIME_COL
                    + " > "+sysDateStr+" AND " + FROM_TIME_COL + " <= "
                    + currentHour + " AND " + currentHour + " <= "
                    + TO_TIME_COL + " ORDER BY " + STATUS_COL + " DESC,"
                    + SET_TIME_COL + " DESC ";        

        logger.info("Executing query: " + query);
        try
        {
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            if (results.next())
            {
                subscriberID = results.getString(SUBSCRIBER_ID_COL);
                callerID = results.getString(CALLER_ID_COL);
                categoryID = results.getInt(CATEGORY_ID_COL);
                subscriberWavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
                setTime = results.getTimestamp(SET_TIME_COL);
                startTime = results.getTimestamp(START_TIME_COL);
                endTime = results.getTimestamp(END_TIME_COL);
                status = results.getInt(STATUS_COL);
                classType = results.getString(CLASS_TYPE_COL);
                selectedBy = results.getString(SELECTED_BY_COL);
                selectionInfo = results.getString(SELECTION_INFO_COL);
                nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
                prepaid = results.getString(PREPAID_YES_COL);
                fromTime = results.getInt(FROM_TIME_COL);
                toTime = results.getInt(TO_TIME_COL);
                sel_status = results.getString(SEL_STATUS_COL);
                deSelectedBy = results.getString(DESELECTED_BY_COL);
                oldClassType = results.getString(OLD_CLASS_TYPE_COL);  
                catID = results.getInt(CATEGORY_TYPE_COL);
                loop = results.getString(LOOP_STATUS_COL).charAt(0);
                selInterval = results.getString(SEL_INTERVAL_COL);
                refID = results.getString(INTERNAL_REF_ID_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				circleId = results.getString(CIRCLE_ID_COL);
                if(m_rrbtOn) 
                    rbtType = results.getInt(SEL_TYPE_COL); 

                return new SubscriberStatusImpl(subscriberID, callerID,
                        categoryID, subscriberWavFile, setTime, startTime, endTime,
                        status, classType, selectedBy, selectionInfo,
                        nextChargingDate, prepaid, fromTime, toTime, sel_status,
                        deSelectedBy, oldClassType,catID,loop,rbtType,selInterval, refID, extraInfo, circleId, null);
            }
            else
            {
                return new SubscriberStatusImpl(subscriberID,
                                                callerID, categoryID, "PLAY_AD", setTime,
                                                startTime, endTime, status, classType, selectedBy,
                                                selectionInfo, nextChargingDate, prepaid, fromTime,
                                                toTime, sel_status, deSelectedBy, oldClassType,5,'B',rbtType,selInterval, refID, extraInfo, circleId,null); 
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
    }

	// Added extraInfo - TRAI changes
	static boolean smSelectionActivationRenewalFailure(Connection conn, String subscriberID,
			String refID, String deactivatedBy,
			String type, String classType, char newLoopStatus, int rbtType, String extraInfo, String circleId)
	{
		int n = -1;
		Statement stmt = null;

		String nextDate = "TO_DATE('20371231','yyyyMMdd')";
		String sysDateStr = "SYSDATE"; 
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		String circleIdQuery = "";
        if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "TIMESTAMP('2037-12-31')";
			sysDateStr = "SYSDATE()";
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";
		}
		if(deactivatedBy != null && (deactivatedBy.equalsIgnoreCase("NA") || deactivatedBy.equalsIgnoreCase("NEF") || deactivatedBy.equalsIgnoreCase("AF") || deactivatedBy.equalsIgnoreCase("RF")))
		{
			nextDate = "TO_DATE('20351231','yyyyMMdd')";
			if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextDate = "TIMESTAMP('2035-12-31')";
		}

		String prepaid = "n";
		if(type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		String query = "UPDATE " + TABLE_NAME + " SET ";
		if (deactivatedBy != null && (deactivatedBy.equalsIgnoreCase("NA") || deactivatedBy.equalsIgnoreCase("AF")))
		{
			query = query + START_TIME_COL + " = "+sysDateStr+", ";
		}
		//RBT-14497 - Tone Status Check :Reset the retrycount,retrytime
		query = query + END_TIME_COL + " = "+sysDateStr+", "
				+ NEXT_CHARGING_DATE_COL + " = " + nextDate + ", "
				+ PREPAID_YES_COL + " = '" + prepaid + "', "
				+ CLASS_TYPE_COL + " = '" + classType + "', "
				+ SEL_STATUS_COL + " = '" + STATE_DEACTIVATED + "', "
				+ EXTRA_INFO_COL + " = " + sqlString(extraInfo) + ", "
				+ DESELECTED_BY_COL + " = " + sqlString(deactivatedBy)
				+ circleIdQuery
				+ ", " + LOOP_STATUS_COL + " = "
				+ sqlString(String.valueOf(newLoopStatus)) 
				+ " , " + RETRY_COUNT_COL + " = NULL, "
				+ NEXT_RETRY_TIME_COL + " = NULL" 
				+ " WHERE "
				+ SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND " + SEL_STATUS_COL + " IN ('"
				+ STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING
				+ "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR
				+ "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL
				+ "', 'D', 'P', 'F','G', '"+STATE_SUSPENDED+"','W') ";  //RBT-12906- Resubscription callback
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static boolean smSelectionDeactivationFailure(Connection conn, String subscriberID, String refID, String type, int rbtType, String extraInfo, String circleId)
	{
		int n = -1;
		Statement stmt = null;

		String prepaid = "n";
		if(type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";
		String circleIdQuery = "";
        if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
		String query = "UPDATE " + TABLE_NAME + " SET " +
		SEL_STATUS_COL + " = '" + STATE_TO_BE_DEACTIVATED + "', " + 
		PREPAID_YES_COL + " = '" + prepaid + "'" +circleIdQuery;
		if(extraInfo != null)
			query += " , "+ EXTRA_INFO_COL + " = '" + extraInfo + "'";

		query +=" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + INTERNAL_REF_ID_COL + " = "+ sqlString(refID) + 
		" AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_DEACTIVATED + "','" + STATE_DEACTIVATION_PENDING + "','" + STATE_DEACTIVATION_ERROR + "') ";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType;
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	/*subscription manager daemon*/
	static boolean smURLSelectionActivation(Connection conn, String subscriberID,
			String callerID, int status, Date setDate1, boolean isSuccess,
			 boolean isError, char newLoopStatus, Date startDate, int rbtType, boolean updateRefID, String wavFile, String refID , String extraInfo )
	{
		int n = -1;
		Statement stmt = null;

		String selStatus = "N";
		if(isError)
			selStatus = "E";
		else if(!isSuccess)
			selStatus = "B";
		String startTime = "SYSDATE"; 
        if(startDate != null) 
        	startTime = sqlTime(startDate); 
		
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB)){
			startTime = "SYSDATE()"; 
	        if(startDate != null) 
	        	startTime = mySqlTime(startDate); 
		}

		String setDate = null;
		try
		{
			setDate = new SimpleDateFormat("yyyyMMddHHmmss").format(setDate1);
		}
		catch(Exception e)
		{
		
		}
		
		String sysdateStr = "SYSDATE";
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysdateStr = "SYSDATE()";
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";
		}
	
		String query = "UPDATE " + TABLE_NAME + " SET ";
		if(!isSuccess && !isError)
		{
			query = query + NEXT_CHARGING_DATE_COL + " = "+sysdateStr+", "+ START_TIME_COL + " = "+startTime +", ";
			query = query + LOOP_STATUS_COL + " = " + sqlString(String.valueOf(newLoopStatus)) + ", ";
		}
		
		if (extraInfo != null)
		{
			if (extraInfo.equalsIgnoreCase("NULL"))
				query +=  EXTRA_INFO_COL + " = NULL , ";
			else
				query +=  EXTRA_INFO_COL + " = '" + extraInfo + "' , ";
		}
		

		query = query + SEL_STATUS_COL + " = " + sqlString(selStatus);
		if(updateRefID)
		{
			query += " ," + INTERNAL_REF_ID_COL + " = " + sqlString(refID);
		
			query+= " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
				+ subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere(callerID)+" AND "
				+ SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED
				+ "' ,'" + STATE_CHANGE + "' ,'" + STATE_UN
				+ "', '" + STATE_BASE_ACTIVATION_PENDING + "') AND " + setTimeCond + " = " + sqlString(setDate)
				+ " AND " + STATUS_COL + " = " + status;
		}
		else
		{
			query+= " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
				+ subscriberID + "' AND " + INTERNAL_REF_ID_COL + " = " +sqlString(refID) +" AND "
				+ SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED
				+ "' ,'" + STATE_CHANGE + "' ,'" + STATE_UN
				+ "', '" + STATE_BASE_ACTIVATION_PENDING + "') ";
		
		}
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		if(wavFile != null)
			query += " AND SUBSCRIBER_WAV_FILE = "+sqlString(wavFile);

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}


	static boolean smURLSelectionActivationRetry(Connection conn, String subscriberID,
			String callerID, int status, Date setDate1, Date startDate, int rbtType, String wavFile, String refID)
	{
		int n = -1;
		Statement stmt = null;

		String startTime = "SYSDATE"; 
        if(startDate != null) 
        	startTime = sqlTime(startDate); 
		
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB)){
			startTime = "SYSDATE()"; 
	        if(startDate != null) 
	        	startTime = mySqlTime(startDate); 
		}

		String setDate = null;
		try
		{
			setDate = new SimpleDateFormat("yyyyMMddHHmmss").format(setDate1);
		}
		catch(Exception e)
		{
		
		}
		
		String sysdateStr = "SYSDATE";
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysdateStr = "SYSDATE()";
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";
		}
	
		String query = "UPDATE " + TABLE_NAME + " SET ";
		query += INTERNAL_REF_ID_COL + " = " + sqlString(refID);
		query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
				+ subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere(callerID)+" AND "
				+ SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED
				+ "' ,'" + STATE_CHANGE + "' ,'" + STATE_UN
				+ "', '" + STATE_BASE_ACTIVATION_PENDING + "') AND " + setTimeCond + " = " + sqlString(setDate)
				+ " AND " + STATUS_COL + " = " + status;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		if(wavFile != null)
			query += " AND SUBSCRIBER_WAV_FILE = "+sqlString(wavFile);

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}
	
	static boolean smURLSelectionDeactivation(Connection conn, String subscriberID, String refID, boolean success, boolean isError, int rbtType)
	{
		int n = -1;
		Statement stmt = null;

		String selStatus = "P";
		if(isError)
			selStatus = "F";
		else if(!success)
			selStatus = "X";

		String ncdStr = "TO_DATE('20371231','YYYYMMDD')";
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			ncdStr = "TIMESTAMP('2037-12-31')";
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
			+ sqlString(selStatus)
			+ ", NEXT_CHARGING_DATE = "+ncdStr;
		
		if(selStatus.equals("X"))
			query = query + ", "+LOOP_STATUS_COL+ " = 'x' ";
		 	query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
			+ subscriberID + "' AND " + INTERNAL_REF_ID_COL + " = " +sqlString(refID) +" AND "
			+ SEL_STATUS_COL + " = '" + STATE_TO_BE_DEACTIVATED
			+ "' ";
		
		if(m_rrbtOn) 
			query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static boolean smURLSelectionNotSendSMDeactivation(Connection conn, String subscriberID, String callerID, int status, Date setDate1, boolean success, boolean isError, int rbtType, char newLoopStatus)
	{
		int n = -1;
		Statement stmt = null;

		String setDate = null;
		try
		{
			setDate = new SimpleDateFormat("yyyyMMddHHmmss").format(setDate1);
		}
		catch(Exception e)
		{
		
		}
		String selStatus = "P";
		if(isError)
			selStatus = "F";
		else if(!success)
			selStatus = "X";

		String ncdStr = "TO_DATE('20371231','YYYYMMDD')";
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			ncdStr = "TIMESTAMP('2037-12-31')";
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
			+ sqlString(selStatus)
			+ ", NEXT_CHARGING_DATE = "+ncdStr;
		
		if(selStatus.equals("X"))
			query = query + ", "+LOOP_STATUS_COL+ " = " + sqlString(String.valueOf(newLoopStatus));
		 	query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
			+ subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere(callerID)+" AND "
			+ SEL_STATUS_COL + " = '" + STATE_TO_BE_DEACTIVATED
			+ "' AND "+ setTimeCond + " = "
			+ sqlString(setDate) + " AND " + STATUS_COL + " = "
			+ status;
		
		if(m_rrbtOn) 
			query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static Object smGetActivatedSelections(Connection conn, int fetchSize,
			String prepaidYes, boolean returnList, ArrayList lowModes, boolean getLowPriority)
	{
		Statement stmt = null;
		RBTResultSet results = null;
		
		String subscriberID = null;
        String callerID = null;
        int categoryID = -1;
        String subscriberWavFile = null;
        Date setTime = null;        
        int fromTime = -1;
        int toTime = -1;       

		int count = 0;
		int totalCount = 0;
		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" + STATE_CHANGE + "')";

		if(prepaidYes != null)
			query += " AND " + PREPAID_YES_COL + " = " + sqlString(prepaidYes);

		if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.PROVISIONING, WebServiceConstants.INLINE_PARAMETERS, "false")) {
			query += " AND " + INLINE_DAEMON_FLAG_COL + " IS " + null;
		}

		query += " ORDER BY " +SET_TIME_COL;

		logger.info("Executing query: " + query);
		ArrayList<String> selectionsArray = new ArrayList<String>();
		try
		{
			if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
				stmt = conn.createStatement();
			else
			{
				stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				stmt.setFetchSize(fetchSize);
			}

			results = new RBTResultSet(stmt.executeQuery(query));
			while(results.next())
			{
				if (count < fetchSize)
				{	
					subscriberStatus = getSubscriberStatusFromRS(results);
					subscriberID = subscriberStatus.subID();
					callerID = subscriberStatus.callerID();
					categoryID = subscriberStatus.categoryID();
					subscriberWavFile = subscriberStatus.subscriberFile();
					if(lowModes != null)
					{
						String selBy = subscriberStatus.selectedBy();
						if(selBy != null)
							selBy = selBy.toLowerCase();
						if(selBy != null && getLowPriority && !lowModes.contains(selBy))
							continue;
						if(selBy != null && !getLowPriority && lowModes.contains(selBy))
							continue;
					}
					if (!selectionsArray.contains(subscriberID + callerID
		                         + categoryID + subscriberWavFile + fromTime + toTime
		                         + setTime))
					{
						subscriberStatusList.add(subscriberStatus);
						selectionsArray.add(subscriberID + callerID + categoryID
		                             + subscriberWavFile + fromTime + toTime + setTime);
					}
					else
					{
						logger.info("RBT:: SubscriberID Selections reoccured in resultset "
								+ subscriberID + " " + callerID
								+ " " + categoryID + " "
								+ subscriberWavFile + " "
								+ fromTime + " " + toTime + " "
								+ setTime);
					}
					count++;
				}
				totalCount++;
			}
			if (RBTDaemonManager.isFcapsEnabled)
				CounterStats.getInstance().setSelActCount(totalCount, new Date());

			SMDaemonPerformanceMonitor.recordDbQueueCount("SelectionActivationQueue", totalCount);
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
		if(subscriberStatusList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_SUBSCRIBER_SELECTIONS successful");
			if(returnList)
				return subscriberStatusList;
			else
				return (SubscriberStatus[])subscriberStatusList.toArray(new SubscriberStatus[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_SUBSCRIBER_SELECTIONS");
			return null;
		}
	}

	static SubscriberStatus[] smGetDirectActivatedSelections(Connection conn, 
            int fetchSize) 
    { 
        Statement stmt = null; 
        RBTResultSet results = null; 
 
        String subscriberID = null; 
        String callerID = null; 
        int categoryID = -1; 
        String subscriberWavFile = null; 
        Date setTime = null; 
        int fromTime = -1; 
        int toTime = -1; 
        int count = 0; 
 
        SubscriberStatus subscriberStatus = null; 
        List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>(); 
 
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL 
                + " = 'S' ORDER BY " 
                + SET_TIME_COL; 
 
		logger.info("Executing query: " + query);
        ArrayList<String> selectionsArray = new ArrayList<String>(); 
        try 
        { 
            stmt = conn.createStatement(); 
            results = new RBTResultSet(stmt.executeQuery(query)); 
            while (count < fetchSize && results.next()) 
            {                                 
                subscriberStatus = getSubscriberStatusFromRS(results);
                
                subscriberID = subscriberStatus.subID();
                callerID = subscriberStatus.callerID();
                categoryID = subscriberStatus.categoryID();
                subscriberWavFile = subscriberStatus.subscriberFile();
                fromTime = subscriberStatus.fromTime();
                toTime = subscriberStatus.toTime();
                setTime = subscriberStatus.setTime();
 
                if (!selectionsArray.contains(subscriberID + callerID 
                        + categoryID + subscriberWavFile + fromTime + toTime 
                        + setTime)) 
                { 
                    subscriberStatusList.add(subscriberStatus); 
                    selectionsArray.add(subscriberID + callerID + categoryID 
                            + subscriberWavFile + fromTime + toTime + setTime); 
                } 
                else 
                { 
                   logger.info("RBT:: SubscriberID Selections reoccured in resultset " 
                                                + subscriberID + " " + callerID 
                                                + " " + categoryID + " " 
                                                + subscriberWavFile + " " 
                                                + fromTime + " " + toTime + " " 
                                                + setTime); 
                } 
 
                count++; 
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
    } 
	
	static SubscriberStatus [] smGetRenewalSelections(Connection conn, int fetchSize)
	{
		Statement stmt = null;
		ResultSet results = null;

		int count = 0;
		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL
					+ " IN ('" + STATE_REQUEST_RENEWAL + "')  "
					+ " ORDER BY " + SET_TIME_COL;
	
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(count < fetchSize && results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}


	static SubscriberStatus[] smGetDeactivatedSelections(Connection conn, int fetchSize)
	{
		Statement stmt = null;
		ResultSet results = null;

		int count = 0;
		int totalCount = 0;
		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL
				+ " = '" + STATE_TO_BE_DEACTIVATED + "'";
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND ROWNUM <="+fetchSize;
		else
			query += " LIMIT "+fetchSize;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				if (count < fetchSize)
				{	
					subscriberStatus = getSubscriberStatusFromRS(results);
					subscriberStatusList.add(subscriberStatus);
					count++;
				}
				totalCount++;
			}
			if (RBTDaemonManager.isFcapsEnabled)
				CounterStats.getInstance().setSelDctCount(totalCount, new Date());

			SMDaemonPerformanceMonitor.recordDbQueueCount("SelectionDeactivationQueue", totalCount);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	static boolean smDeactivateOldTrialSelection(Connection conn, String subscriberID, String callerID, int status, int fromTime, int toTime, int rbtType)
	{
		int n = -1;
		Statement stmt = null;

		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";

		String sysDateStr  = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr  = "SYSDATE()";
			nextChargingDate = "TIMESTAMP('2037-12-31')";
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " +
		END_TIME_COL + " = "+ sysDateStr + ", " + 
		NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", "+
		SEL_STATUS_COL + " = '" + STATE_DEACTIVATED + "', " +
		LOOP_STATUS_COL + " = '" + LOOP_STATUS_EXPIRED + "', " +
		DESELECTED_BY_COL + " = 'SM'"+
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + CALLER_ID_COL + getNullForWhere(callerID)+" AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " + 
		FROM_TIME_COL + " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime  + " AND " + STATUS_COL + " = " + status;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static boolean smTrialSelectionCharging(Connection conn, String subscriberID, String callerID, int status, Date setDate1, boolean isOptIn)
	{
		int n = -1;
		Statement stmt = null;

		String setDate = null;
		try
		{
			setDate = new SimpleDateFormat("yyyyMMddHHmmss").format(setDate1);
		}
		catch(Exception e)
		{
		
		}

		String classType = "DEFAULT";
		if(isOptIn)
			classType = "DEFAULT_OPTIN";
		
		String sysDateStr = "SYSDATE";
		String startTimeStr = "TO_DATE('20040101','yyyyMMdd')";
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr = "SYSDATE()";
			startTimeStr = "TIMESTAMP('2004-01-01')";
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";
		}
		String query = "UPDATE " + TABLE_NAME + " SET " + CLASS_TYPE_COL + " = '"
				+ classType + "', " + NEXT_CHARGING_DATE_COL + " = NULL, "
				+ START_TIME_COL + " = "+startTimeStr+", "
				+ SEL_STATUS_COL + " = '" + STATE_TO_BE_ACTIVATED + "'  ";
		
		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CALLER_ID_COL + getNullForWhere(callerID)+" AND "+setTimeCond+" = " + sqlString(setDate) + " AND " + 
		END_TIME_COL + " > "+sysDateStr+" AND " + CLASS_TYPE_COL + " LIKE ('TRIAL%') AND " +  NEXT_CHARGING_DATE_COL + " < "+sysDateStr+" AND " + 
		STATUS_COL + " = " + status;
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static SubscriberStatus [] smGetTrialSelections(Connection conn, int fetchSize)
	{
		Statement stmt = null;
		ResultSet results = null;

		int count = 0;
		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
			
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL + " < "+sysDateStr+" AND " + 
		END_TIME_COL + " > "+sysDateStr+" AND " + CLASS_TYPE_COL + " LIKE ('TRIAL%') AND "  + 
		NEXT_CHARGING_DATE_COL + " < "+sysDateStr; 
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL + " < "+sysDateStr+" AND " + 
			END_TIME_COL + " > "+sysDateStr+" AND " + CLASS_TYPE_COL + " LIKE ('TRIAL%') AND "  + 
			NEXT_CHARGING_DATE_COL + " < "+sysDateStr; 
				
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(count < fetchSize && results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	static SubscriberStatus [] getTimeOfTheDaySelections(Connection conn, String subID, String callID )
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND " + CALLER_ID_COL + getNullForWhere(callID)+" AND " + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND " +  STATUS_COL + " = 80";
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	/*time of the day changes*/
	// Commented callerID. Doing the check in DBManager.. modified for UDS
	static SubscriberStatus[] getSubscriberSelections(Connection conn, String subID, String callID, int rbtType)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND ";
		query = query + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND "+ STATUS_COL + " IN (1,75,94,95,80,79,92) ";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
		query += " ORDER BY " + SET_TIME_COL + " DESC "; 

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}


	/*time of the day changes*/
	// Commented callerID. Doing the check in DBManager.. modified for UDS
	static SubscriberStatus[] getSubscriberSelections(Connection conn, String subID, String callID, int rbtType, String udpId)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND ";
		query = query + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND "+ STATUS_COL + " IN (1,75,94,95,80,79,92) ";
		
		if(udpId != null && !udpId.isEmpty())
			query = query + " AND " + UDP_ID_COL + "="+ sqlString(udpId);
		
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
		query += " ORDER BY " + SET_TIME_COL + " DESC "; 

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	/*weekly monthly changes*/
	static boolean convertSelectionClassType(Connection conn, String subscriberID, String initClassType, String finalClassType, String refID, String mode, int rbtType)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		String sysDateStr = "SYSDATE";
		String startTimeStr =  "TO_DATE('20040101','yyyyMMdd')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr = "SYSDATE()";
			startTimeStr =  "TIMESTAMP('2004-01-01')";
		}
		
		query = "UPDATE " + TABLE_NAME + " SET " +
		START_TIME_COL + " = "+sysDateStr+", " + 
		NEXT_CHARGING_DATE_COL + " = NULL, " +
		CLASS_TYPE_COL + " = '" + finalClassType + "', " +
		SEL_STATUS_COL + " = '" + STATE_CHANGE + "', "+
		OLD_CLASS_TYPE_COL + " = " + sqlString(initClassType);

		if (mode != null)
			query += ", " + SELECTED_BY_COL + " = " + sqlString(mode);

		query += " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CLASS_TYPE_COL + " = " + sqlString(initClassType) + " AND " + 
		SET_TIME_COL + " <= "+sysDateStr+" AND " + SEL_STATUS_COL + " = '" + STATE_ACTIVATED + "' "; 
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType;
		if (refID != null)
			query += " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID);

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return (n != 0);
	}
	
	static boolean updateSelectionClassType(Connection conn, String subscriberID, String initClassType, String finalClassType, String refID, String mode, int rbtType)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		String sysDateStr = "SYSDATE";
		String startTimeStr =  "TO_DATE('20040101','yyyyMMdd')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr = "SYSDATE()";
			startTimeStr =  "TIMESTAMP('2004-01-01')";
		}
		
		query = "UPDATE " + TABLE_NAME + " SET " +
		CLASS_TYPE_COL + " = '" + finalClassType + "' ";
		
		query += " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CLASS_TYPE_COL + " = " + sqlString(initClassType) + " AND " + 
		SET_TIME_COL + " <= "+sysDateStr+" AND " + SEL_STATUS_COL + " = '" + STATE_TO_BE_ACTIVATED + "' "; 
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType;
		if (refID != null)
			query += " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID);

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return (n != 0);
	}

	static boolean smSelectionUpgrade(Connection conn, String subscriberID, String oldClassType, String newClassType, String refID,String circleId, String newLoopStatus)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		String startTimeStr =  "TO_DATE('20040101','yyyyMMdd')";
		String sysDateStr = "SYSDATE";
		String nextChargeDateStr = "SYSDATE";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			startTimeStr =  "TIMESTAMP('2004-01-01')";
			nextChargeDateStr = "SYSDATE()";
			sysDateStr = "SYSDATE()";
		}
		String circleIdQuery = "";
        if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
        String loopStatusQuery = "";
        if (newLoopStatus != null) {
        	loopStatusQuery += ", " + LOOP_STATUS_COL + " = " + sqlString(newLoopStatus);
		}
		query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL + " = "
				+ sysDateStr + ", " + NEXT_CHARGING_DATE_COL + " = " + nextChargeDateStr + ", "
				+ CLASS_TYPE_COL + " = '" + newClassType + "', "
				+ SEL_STATUS_COL + " = '" + STATE_ACTIVATED + "', "
				+ OLD_CLASS_TYPE_COL + " = " + sqlString(oldClassType)
				+ " , " + RETRY_COUNT_COL + " = NULL, " + NEXT_RETRY_TIME_COL + " = NULL"
				+ circleIdQuery+ loopStatusQuery
				+ " WHERE " + SUBSCRIBER_ID_COL  + " = " + sqlString(subscriberID) + " AND "
				+ INTERNAL_REF_ID_COL + " = " + sqlString(refID);

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return (n != 0);
	}

	static SubscriberStatus [] getFreeActiveStatusSubscribers(Connection conn, int days, int day, String trial, int noOfRows)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days + 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date nextTime1 = cal.getTime();
		cal.add(Calendar.DATE, 1);
		Date nextTime2 = cal.getTime();
		int count = 0;
		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
		
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL
					+ " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE AND "
					+ NEXT_CHARGING_DATE_COL + "  >= " + sqlTime(nextTime1)
					+ "  AND " + NEXT_CHARGING_DATE_COL + "  <= "
					+ sqlTime(nextTime2) + " AND " + CLASS_TYPE_COL + " = "
					+ sqlString(trial) + " AND " + SELECTION_INFO_COL
					+ " NOT LIKE '%FA:" + day + "%' ";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SET_TIME_COL
					+ " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE() AND "
					+ NEXT_CHARGING_DATE_COL + "  >= "
					+ mySQLDateTime(nextTime1) + "  AND "
					+ NEXT_CHARGING_DATE_COL + "  <= "
					+ mySQLDateTime(nextTime2) + " AND " + CLASS_TYPE_COL
					+ " = " + sqlString(trial) + " AND " + SELECTION_INFO_COL
					+ " NOT LIKE '%FA:" + day + "%' ";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while( count<noOfRows && results.next() )
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	static boolean smsSentForTrialSubscriber(Connection conn, String subscriberID, Date startDate, int day)
	{
		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		try
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '"+ subscriberID +"' AND "+ SET_TIME_COL +" >= "+ sqlTime(startDate) +" AND "+ SELECTION_INFO_COL +" LIKE '%FA:"+ day +"%'";
			else
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '"+ subscriberID +"' AND "+ SET_TIME_COL +" >= "+ mySQLDateTime(startDate) +" AND "+ SELECTION_INFO_COL +" LIKE '%FA:"+ day +"%'";

			logger.info("Executing query: " + query);
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			if(results.next())
				return true;
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return false;
	}

	static boolean setSelectionInfo(Connection conn, String subscriberID, Date setTime, String selinfo)
	{
		Statement stmt = null;
		String query = "UPDATE " + TABLE_NAME + " SET " + SELECTION_INFO_COL + " = '"+selinfo+"' WHERE " + 
				SUBSCRIBER_ID_COL + " ='" + subscriberID + "'";
		if(setTime != null)
		{ 
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = query + " AND TO_CHAR( " + SET_TIME_COL
            	+ " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( "
            	+ sqlTime(setTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
			else
				query = query + " AND DATE_FORMAT( " + SET_TIME_COL
            	+ " , '%Y%m%d%T') = DATE_FORMAT( "
            	+ mySQLDateTime(setTime) + ", '%Y%m%d%T')";
		}

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch(Exception se)
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

	static SubscriberStatus [] getNonFreeSelections(Connection conn, String subscriberID, String chargeClass)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String classTypeQuery = "";
		String[] tokens = chargeClass.split(",");
		for (String eachToken : tokens) {
			if (classTypeQuery != "")
				classTypeQuery += " AND ";

			classTypeQuery += CLASS_TYPE_COL +" NOT LIKE '" + eachToken.trim() + "%'";
		}

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID +"'" +
				" AND " + SET_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE AND "
				+ NEXT_CHARGING_DATE_COL + "  IS NOT NULL AND ("
				+ classTypeQuery + ")";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID +"'" +
			" AND " + SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE() AND "
			+ NEXT_CHARGING_DATE_COL + "  IS NOT NULL AND ("
				+ classTypeQuery + ")";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	/* For RBTDAEMON MANAGER */
	static int getCountStatus(Connection conn, String status)
	{
		int n = 0;
		Statement stmt = null;
		RBTResultSet rs = null;

		String query = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL + " = "+sqlString(status);

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			if(rs.next())
			{
				n = rs.getInt(1);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return 0;
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		return(n);
	}

	public static boolean deleteClipForSubscriber(Connection conn, String subscriberID, String subWavFile, boolean b, String callerId)
	{
		String query = null;
		Statement stmt = null;
		int results = -1;
		
		if(!b)
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "'" +
				" AND  SUBSCRIBER_WAV_FILE  = '" + subWavFile + "'";
		else{
			if(callerId == null)
				query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + CALLER_ID_COL + " IS NULL AND " + SUBSCRIBER_WAV_FILE_COL + " IN ('" + subWavFile + "')";
			else
				query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + CALLER_ID_COL + " = '" + callerId + "' AND " + SUBSCRIBER_WAV_FILE_COL + " IN ('" + subWavFile + "')";

		}
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (results>=1);
	}


	/*ADDED FOR TATA*/
	static SubscriberStatus [] getSubscriberRecords(Connection conn, String subID)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		ArrayList<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND " + SET_TIME_COL + " <= SYSDATE ORDER BY "+ SET_TIME_COL;
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND " + SET_TIME_COL + " <= SYSDATE() ORDER BY "+ SET_TIME_COL;
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	//TATA
	static boolean checkMBSettingExistsForCallerId(Connection conn, String subID, String callerId, int rbtType)
	{
		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		boolean returnResult = false;

		if(callerId == null || callerId == "")
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND " + CALLER_ID_COL + " IS NULL AND " + SEL_STATUS_COL + " = " + sqlString(STATE_ACTIVATED) + " AND " + CATEGORY_TYPE_COL + " = " + SHUFFLE;
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND " + CALLER_ID_COL + " = '" + callerId + "' AND " + SEL_STATUS_COL + " = " + sqlString(STATE_ACTIVATED) + " AND " + CATEGORY_TYPE_COL + " = " + SHUFFLE;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			if(results.next())
			{
				returnResult = true;
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return returnResult;
	}

	static boolean deactivateSubWavFile(Connection conn, String subscriberID, String subWavFile,
			String selStaus, String deselectedBy, String sendSMS, int rbtType)
	{
		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME  + " SET " + SEL_STATUS_COL + " = " + sqlString(selStaus) +
		", " + DESELECTED_BY_COL + " = " + sqlString(deselectedBy);

		// using status column as send sms column from rbt_tobe_deleted_settings
		// status 4 - 'sendSMS = y' and status 3 - 'sendSMS = n'
		if(selStaus.equalsIgnoreCase("D") && sendSMS!=null)
		{
			if(sendSMS.equalsIgnoreCase("y"))
				query += ", " + STATUS_COL + " = 3";
			else
				query += ", " + STATUS_COL + " = 4";
		}

		query += " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND " +
		SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subWavFile);
		
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException e)
		{
			logger.error("", e);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}

	static boolean deactivateSubWavFileForCaller(Connection conn, String subscriberID, String callerID,
			String subWavFile, String selStaus, String deselectedBy, String sendSMS, boolean checkSelStatus, int rbtType)
	{
		Statement stmt = null;
		int n = -1;
		
		String endTime = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB)){
			endTime = "SYSDATE()";
		}

		String query = "UPDATE " + TABLE_NAME  + " SET " + SEL_STATUS_COL + " = " + sqlString(selStaus) +
		", " + DESELECTED_BY_COL + " = " + sqlString(deselectedBy);

		// using status column as send sms column from rbt_tobe_deleted_settings
		// status 4 - 'sendSMS = y' and status 3 - 'sendSMS = n'
		if(selStaus.equalsIgnoreCase(STATE_TO_BE_DEACTIVATED) && sendSMS!=null)
		{
			if(sendSMS.equalsIgnoreCase("y"))
				query += ", " + STATUS_COL + " = 3";
			else
				query += ", " + STATUS_COL + " = 4";
		}

		if(selStaus.equalsIgnoreCase(STATE_DEACTIVATED))
			query += ", " + END_TIME_COL + " = " + endTime;

		query += " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND " +
		SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subWavFile);
		
		if(checkSelStatus)
		{
			if(selStaus.equalsIgnoreCase(STATE_TO_BE_DEACTIVATED))
				query += " AND " + SEL_STATUS_COL + " = " + sqlString(STATE_ACTIVATED);
			else if(selStaus.equalsIgnoreCase(STATE_DEACTIVATED))
				query += " AND " + SEL_STATUS_COL + " = " + sqlString(STATE_TO_BE_DEACTIVATED);
		}

		if(callerID == null)
			query += " AND " + CALLER_ID_COL + " IS NULL";
		else
			query += " AND " + CALLER_ID_COL + " = " + sqlString(callerID);
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException e)
		{
			logger.error("", e);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}
	
	static SubscriberStatus[] getAllPendingSettings(Connection conn, String subscriberId, int rbtType)
	{
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		ArrayList<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = '" + subscriberId + "' AND " + SEL_STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_ACTIVATED);

		if(m_rrbtOn)
			query += " AND "+ SEL_TYPE_COL + " = " + rbtType;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	/*ADDED FOR TATA*/
	static boolean updateDeactivationFailed(Connection conn, String subscriberID, int rbtType)
	{
		int n = -1;
		Statement stmt = null;

		String endDate = "TO_DATE('20371231','yyyyMMdd')";
		String nextChargingDate = "TO_DATE('20351231','yyyyMMdd')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			endDate = "TIMESTAMP('2037-12-31')";
			nextChargingDate = "TIMESTAMP('2035-12-31')";
		}
		String query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = '" + STATE_ACTIVATED + "' , " +
				END_TIME_COL + " = " + endDate + " , " + 
				NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + 
				" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "'";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}

	public static boolean clipToBeUpdatedForTATA(Connection conn, SubscriberStatus cliptobeadded, int rbtType)
	{
		String query = null;
		Statement stmt = null;
		int results = -1;

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE "
					+ TABLE_NAME
					+ " SET "
					+ SEL_STATUS_COL
					+ " = '"
					+ STATE_ACTIVATED
					+ "' , "
					+ START_TIME_COL
					+ " = SYSDATE, "
					+ NEXT_CHARGING_DATE_COL
					+ " = to_date('31/12/2035 00:00:00', 'DD/MM/YYYY HH24:MI:SS') WHERE  "
					+ SUBSCRIBER_ID_COL + " = '" + cliptobeadded.subID()
					+ "' AND " + CALLER_ID_COL
					+ getNullForWhere(cliptobeadded.callerID()) + " AND "
					+ SUBSCRIBER_WAV_FILE_COL + " = '"
					+ cliptobeadded.subscriberFile() + "' AND "
					+ SEL_STATUS_COL + " = " + sqlString(STATE_TO_BE_ACTIVATED);
		else
			query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = '"
					+ STATE_ACTIVATED + "' , " + START_TIME_COL
					+ " = SYSDATE(), " + NEXT_CHARGING_DATE_COL
					+ " = TIMESTAMP('2035-12-31') WHERE  " + SUBSCRIBER_ID_COL
					+ " = '" + cliptobeadded.subID() + "' AND " + CALLER_ID_COL
					+ getNullForWhere(cliptobeadded.callerID())
					+ " AND " + SUBSCRIBER_WAV_FILE_COL + " = '"
					+ cliptobeadded.subscriberFile() + "' AND "
					+ SEL_STATUS_COL + " = " + sqlString(STATE_TO_BE_ACTIVATED);
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeUpdate(query);
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (results>=1);
	}

	static ArrayList<SubscriberStatus> smGetSettingsToBeDeleted(Connection conn, int fetchSize)
	{
		Statement stmt = null;
		ResultSet results = null;

		int count = 0;
		SubscriberStatus subscriberStatus = null;
		ArrayList<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL
					+ " = '" + STATE_TO_BE_DEACTIVATED + "'"
					+ " ORDER BY "+ END_TIME_COL;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(count < fetchSize && results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		if(subscriberStatusList.size() > 0)
		{
			return subscriberStatusList;
		} 
		return null;
	}

	static SubscriberStatus getSubWavFileForCaller(Connection conn, String subscriberID, String callerID, String subWavFile, int rbtType)
	{
		Statement stmt = null;
		ResultSet results = null;
		SubscriberStatus subSel = null;

		String query = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subWavFile)
				+ " AND " + END_TIME_COL + " > SYSDATE ";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
			+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
			+ SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subWavFile)
			+ " AND " + END_TIME_COL + " > SYSDATE() ";
		
		if(callerID == null)
			query += " AND " + CALLER_ID_COL + " IS NULL";
		else
			query += " AND " + CALLER_ID_COL + " = " + sqlString(callerID);
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
				subSel = getSubscriberStatusFromRS(results);
		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return subSel;
	}

	static SubscriberStatus[] getUnProcessedNormalSelections(Connection conn, String subscriberID) {
		return getUnProcessedNormalSelections(conn, subscriberID, null, true);
	}

	static SubscriberStatus[] getUnProcessedNormalSelections(Connection conn, String subscriberID, String strStatus, boolean doQueryDesc) {
		Statement stmt = null;
		RBTResultSet results = null;

		String callerID = null;
		int categoryID = -1;
		String subscriberWavFile = null;
		Date setTime = null;
		Date startTime = null;
		Date endTime = null;
		int status = -1;
		String classType = null;
		String selectedBy = null;
		String selInterval = null;
		String selectionInfo = null;
		Date nextChargingDate = null;
		String prepaid = null;
		int fromTime = -1;
		int toTime = -1;
		String sel_status = null;
		String deSelectedBy = null;
		String oldClassType = null;
		String refID = null;
		String extraInfo = null;
		int catType = -1;
		int rbtType = -1;
		char loopStatus;
		String circleId = null;
		String udpId = null;
		SubscriberStatusImpl subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
		ArrayList<String> selectionsArray = new ArrayList<String>();
		
		if(strStatus == null) {
			strStatus = "1,79";
		}

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + SEL_STATUS_COL + " = 'W' AND " + STATUS_COL
				+ " IN (" + strStatus + ")  AND " + LOOP_STATUS_COL + " IN ('o', 'O', 'B') ORDER BY " + CALLER_ID_COL + ", " + SET_TIME_COL;
		
		if(doQueryDesc)
				query = query + " DESC";

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next()) {
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				callerID = results.getString(CALLER_ID_COL);
				categoryID = results.getInt(CATEGORY_ID_COL);
				subscriberWavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				startTime = results.getTimestamp(START_TIME_COL);
				endTime = results.getTimestamp(END_TIME_COL);
				status = results.getInt(STATUS_COL);
				classType = results.getString(CLASS_TYPE_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				selectionInfo = results.getString(SELECTION_INFO_COL);
				nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
				prepaid = results.getString(PREPAID_YES_COL);
				fromTime = results.getInt(FROM_TIME_COL);
				toTime = results.getInt(TO_TIME_COL);
				catType = results.getInt(CATEGORY_TYPE_COL);
				loopStatus = results.getString(LOOP_STATUS_COL).charAt(0);
				selInterval = results.getString(SEL_INTERVAL_COL);
				refID = results.getString(INTERNAL_REF_ID_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				circleId = results.getString(CIRCLE_ID_COL);
				if(m_rrbtOn)
					rbtType = results.getInt(SEL_TYPE_COL);

				sel_status = results.getString(SEL_STATUS_COL);
				deSelectedBy = results.getString(DESELECTED_BY_COL);
				oldClassType = results.getString(OLD_CLASS_TYPE_COL);
				udpId = results.getString(UDP_ID_COL);
				
				subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID, categoryID,
						subscriberWavFile, setTime, startTime, endTime, status, classType,
						selectedBy, selectionInfo, nextChargingDate, prepaid, fromTime, toTime,
						sel_status, deSelectedBy, oldClassType, catType, loopStatus, rbtType,selInterval, refID, extraInfo, circleId, udpId);

				if(!selectionsArray.contains(subscriberID + callerID + categoryID
						+ subscriberWavFile + fromTime + toTime + endTime)) {
					subscriberStatusList.add(subscriberStatus);
					selectionsArray.add(subscriberID + callerID + categoryID + subscriberWavFile
							+ fromTime + toTime + endTime);
				}
				else {
					logger.info("RBT:: SubscriberID Selections reoccured in resultset " + subscriberID
									+ " " + callerID + " " + categoryID + " " + subscriberWavFile
									+ " " + fromTime + " " + toTime + " " + endTime);
				}
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	} 
	
	static SubscriberStatus[] getUnProcessedProfileSelections(Connection conn, String subscriberID, boolean isProfile) {
		Statement stmt = null;
		RBTResultSet results = null;

		String callerID = null;
		int categoryID = -1;
		String subscriberWavFile = null;
		Date setTime = null;
		Date startTime = null;
		Date endTime = null;
		int status = -1;
		String classType = null;
		String selectedBy = null;
		String selInterval = null;
		String selectionInfo = null;
		Date nextChargingDate = null;
		String prepaid = null;
		int fromTime = -1;
		int toTime = -1;
		String sel_status = null;
		String deSelectedBy = null;
		String oldClassType = null;
		String refID = null;
		String extraInfo = null;
		int catType = -1;
		int rbtType = -1;
		char loopStatus;
		String circleId = null;
		String udpId = null;
		
		SubscriberStatusImpl subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
		ArrayList<String> selectionsArray = new ArrayList<String>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + SEL_STATUS_COL + " = 'W' AND " ;
		if(isProfile)
			query = query + STATUS_COL + " = 99";
		else
			query = query + STATUS_COL + " NOT IN ('99','90')";
		
		query = query +" ORDER BY " + CALLER_ID_COL + ", " + SET_TIME_COL
				+ " DESC";

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next()) {
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				callerID = results.getString(CALLER_ID_COL);
				categoryID = results.getInt(CATEGORY_ID_COL);
				subscriberWavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				startTime = results.getTimestamp(START_TIME_COL);
				endTime = results.getTimestamp(END_TIME_COL);
				status = results.getInt(STATUS_COL);
				classType = results.getString(CLASS_TYPE_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				selectionInfo = results.getString(SELECTION_INFO_COL);
				nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
				prepaid = results.getString(PREPAID_YES_COL);
				fromTime = results.getInt(FROM_TIME_COL);
				toTime = results.getInt(TO_TIME_COL);
				catType = results.getInt(CATEGORY_TYPE_COL);
				loopStatus = results.getString(LOOP_STATUS_COL).charAt(0);
				selInterval = results.getString(SEL_INTERVAL_COL);
				refID = results.getString(INTERNAL_REF_ID_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				circleId =  results.getString(CIRCLE_ID_COL);
				if(m_rrbtOn)
					rbtType = results.getInt(SEL_TYPE_COL);

				sel_status = results.getString(SEL_STATUS_COL);
				deSelectedBy = results.getString(DESELECTED_BY_COL);
				oldClassType = results.getString(OLD_CLASS_TYPE_COL);
				udpId = results.getString(UDP_ID_COL);
				subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID, categoryID,
						subscriberWavFile, setTime, startTime, endTime, status, classType,
						selectedBy, selectionInfo, nextChargingDate, prepaid, fromTime, toTime,
						sel_status, deSelectedBy, oldClassType, catType, loopStatus, rbtType,selInterval, refID, extraInfo, circleId, null);

				if(!selectionsArray.contains(subscriberID + callerID + categoryID
						+ subscriberWavFile + fromTime + toTime + endTime)) {
					subscriberStatusList.add(subscriberStatus);
					selectionsArray.add(subscriberID + callerID + categoryID + subscriberWavFile
							+ fromTime + toTime + endTime);
				}
				else {
					logger.info("RBT:: SubscriberID Selections reoccured in resultset " + subscriberID
									+ " " + callerID + " " + categoryID + " " + subscriberWavFile
									+ " " + fromTime + " " + toTime + " " + endTime);
				}
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	} 
	
	static SubscriberStatus[] getUnProcessedPackSelections(Connection conn, String subscriberID) {
		Statement stmt = null;
		RBTResultSet results = null;

		String callerID = null;
		int categoryID = -1;
		String subscriberWavFile = null;
		Date setTime = null;
		Date startTime = null;
		Date endTime = null;
		int status = -1;
		String classType = null;
		String selectedBy = null;
		String selInterval = null;
		String selectionInfo = null;
		Date nextChargingDate = null;
		String prepaid = null;
		int fromTime = -1;
		int toTime = -1;
		String sel_status = null;
		String deSelectedBy = null;
		String oldClassType = null;
		String refID = null;
		String extraInfo = null;
		int catType = -1;
		int rbtType = -1;
		char loopStatus;
		String circleId = null;
		String udpId = null;
		
		SubscriberStatusImpl subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
		ArrayList<String> selectionsArray = new ArrayList<String>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + SEL_STATUS_COL + " = 'W' AND " + STATUS_COL
				+ " NOT IN ('90','99')  ORDER BY " + CALLER_ID_COL + ", " + SET_TIME_COL
				+ " DESC";

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next()) {
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				callerID = results.getString(CALLER_ID_COL);
				categoryID = results.getInt(CATEGORY_ID_COL);
				subscriberWavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				startTime = results.getTimestamp(START_TIME_COL);
				endTime = results.getTimestamp(END_TIME_COL);
				status = results.getInt(STATUS_COL);
				classType = results.getString(CLASS_TYPE_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				selectionInfo = results.getString(SELECTION_INFO_COL);
				nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
				prepaid = results.getString(PREPAID_YES_COL);
				fromTime = results.getInt(FROM_TIME_COL);
				toTime = results.getInt(TO_TIME_COL);
				catType = results.getInt(CATEGORY_TYPE_COL);
				loopStatus = results.getString(LOOP_STATUS_COL).charAt(0);
				selInterval = results.getString(SEL_INTERVAL_COL);
				refID = results.getString(INTERNAL_REF_ID_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				if(m_rrbtOn)
					rbtType = results.getInt(SEL_TYPE_COL);

				sel_status = results.getString(SEL_STATUS_COL);
				deSelectedBy = results.getString(DESELECTED_BY_COL);
				oldClassType = results.getString(OLD_CLASS_TYPE_COL);
				circleId =  results.getString(CIRCLE_ID_COL);
				udpId = results.getString(UDP_ID_COL);
				subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID, categoryID,
						subscriberWavFile, setTime, startTime, endTime, status, classType,
						selectedBy, selectionInfo, nextChargingDate, prepaid, fromTime, toTime,
						sel_status, deSelectedBy, oldClassType, catType, loopStatus, rbtType,selInterval, refID, extraInfo, circleId,udpId);

				if(!selectionsArray.contains(subscriberID + callerID + categoryID
						+ subscriberWavFile + fromTime + toTime + endTime)) {
					subscriberStatusList.add(subscriberStatus);
					selectionsArray.add(subscriberID + callerID + categoryID + subscriberWavFile
							+ fromTime + toTime + endTime);
				}
				else {
					logger.info("RBT:: SubscriberID Selections reoccured in resultset " + subscriberID
									+ " " + callerID + " " + categoryID + " " + subscriberWavFile
									+ " " + fromTime + " " + toTime + " " + endTime);
				}
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	} 
    
	public static SubscriberStatus getSubscriberStatusFromRS(ResultSet rs) throws SQLException
	{
		RBTResultSet results = new RBTResultSet(rs);
		String subscriberID = results.getString(SUBSCRIBER_ID_COL);
		String callerID = results.getString(CALLER_ID_COL);
		int categoryID = results.getInt(CATEGORY_ID_COL);
		String subscriberWavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
		Date setTime = results.getTimestamp(SET_TIME_COL);
		Date startTime = results.getTimestamp(START_TIME_COL);
		Date endTime = results.getTimestamp(END_TIME_COL);
		int status = results.getInt(STATUS_COL);
		String classType = results.getString(CLASS_TYPE_COL);
		String selectedBy = results.getString(SELECTED_BY_COL);
		String selectionInfo = results.getString(SELECTION_INFO_COL);
		Date nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
		String prepaid = results.getString(PREPAID_YES_COL);
		int fromTime = results.getInt(FROM_TIME_COL);
		int toTime = results.getInt(TO_TIME_COL);
		String sel_status = results.getString(SEL_STATUS_COL);
		String deSelectedBy = results.getString(DESELECTED_BY_COL);
		String oldClassType = results.getString(OLD_CLASS_TYPE_COL);
		int categoryType = results.getInt(CATEGORY_TYPE_COL);
		char loopStatus = results.getString(LOOP_STATUS_COL).charAt(0);
		String selInterval = results.getString(SEL_INTERVAL_COL);
		String refID = results.getString(INTERNAL_REF_ID_COL);
		String extraInfo = results.getString(EXTRA_INFO_COL);
        int rbtType = results.getInt(SEL_TYPE_COL); 
        String circleId = results.getString(CIRCLE_ID_COL);
        String retryCount = results.getString(RETRY_COUNT_COL);
		Date nextRetryTime = results.getTimestamp(NEXT_RETRY_TIME_COL);
		String udpId = results.getString(UDP_ID_COL);
		//String udpId=null;
		
		return new SubscriberStatusImpl(subscriberID, callerID, categoryID, subscriberWavFile, setTime,
				startTime, endTime, status, classType, selectedBy, selectionInfo, nextChargingDate,
				prepaid, fromTime, toTime, sel_status, deSelectedBy, oldClassType, categoryType, loopStatus,rbtType,selInterval, refID, extraInfo, circleId, retryCount, nextRetryTime, udpId);
	}

	static SubscriberStatus getRefIDSelectionOldLogic(Connection conn, String subscriberID, String callerID, 
			int status, String setDate, int fTime, int tTime, String wavFile)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		SubscriberStatus subscriberStatus = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID 
				+ "' AND " + CALLER_ID_COL +  getNullForWhere(callerID) 
				+ " AND to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss') " + " = " + sqlString(setDate) 
				+ " AND " + STATUS_COL + " = " + status;
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID 
				+ "' AND " + CALLER_ID_COL + getNullForWhere(callerID) 
				+ " AND DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s') " + " = " + sqlString(setDate) 
				+ " AND " + STATUS_COL + " = " + status;
		if(wavFile != null) 
        { 
                query += " AND SUBSCRIBER_WAV_FILE = "+sqlString(wavFile); 
        } 

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
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
		return subscriberStatus;
	}

	static boolean updateRefIDSelectionOldLogic(Connection conn, String subscriberID, String callerID, 
			int status, String setDate, int fTime, int tTime,String wavFile, String refID)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		int n = -1;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + INTERNAL_REF_ID_COL + " = " + " '" + refID 
				+ "' WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID 
				+ "' AND " + CALLER_ID_COL +  getNullForWhere(callerID) 
				+ " AND to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss') " + " = " + sqlString(setDate) 
				+ " AND " + STATUS_COL + " = " + status;
		else
			query = "UPDATE " + TABLE_NAME + " SET " + INTERNAL_REF_ID_COL + " = " + " '" + refID 
				+ "' WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID 
				+ "' AND " + CALLER_ID_COL + getNullForWhere(callerID) 
				+ " AND DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s') " + " = " + sqlString(setDate) 
				+ " AND " + STATUS_COL + " = " + status;
		if(wavFile != null) 
        { 
                query += " AND SUBSCRIBER_WAV_FILE = "+sqlString(wavFile); 
        } 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
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
		return n > 0;
	}

	static SubscriberStatus getRefIDSelection(Connection conn, String subscriberID, String refID)
	{
		Statement stmt = null;
		ResultSet results = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " 
			+ SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + INTERNAL_REF_ID_COL +  " = " + sqlString(refID);

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
			{
				return getSubscriberStatusFromRS(results);
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
		return null;
	}
	
	static boolean updateSelectionExtraInfo(Connection conn, String subscriberID, ArrayList<String> refIdList, String extraInfoStr) 
	{ 
        String refIDStr = "";
	    for (int i=0; i<refIdList.size();i++) 
	    {
			if(i < refIdList.size()-1 )
				refIDStr += "'"+refIdList.get(i)+"',";
			else
				refIDStr += "'"+refIdList.get(i)+"'";
	    }
        
        String query = "UPDATE " + TABLE_NAME + " SET " + EXTRA_INFO_COL + " = " + sqlString(extraInfoStr)
        				+ " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) 
        				+ " AND "+ INTERNAL_REF_ID_COL +" IN ("+refIDStr+")";
        
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        Statement stmt = null; 
        int n = -1; 
        try { 
            stmt = conn.createStatement(); 
            n = stmt.executeUpdate(query); 
        } 
        catch(SQLException e) { 
        	logger.error("", e);
        } 
        finally {
        	closeStatementAndRS(stmt, null);
        } 
        return (n >= 1); 
    }
	
	static boolean updateSelectionStatusNExtraInfo(Connection conn, String subscriberID, String refId, String extraInfo , String status,String classType) 
	{ 
        
        String query = "UPDATE " + TABLE_NAME + " SET " + EXTRA_INFO_COL + " = " + sqlString(extraInfo) + " ," + SEL_STATUS_COL + " = " + sqlString(status);
        
        if(classType!=null)
        	query += " , " + CLASS_TYPE_COL + " = " + sqlString(classType);
        
        query +=   " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) 
        				+ " AND "+ INTERNAL_REF_ID_COL +" = " + sqlString(refId);
        
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        Statement stmt = null; 
        int n = -1; 
        try { 
            stmt = conn.createStatement(); 
            n = stmt.executeUpdate(query); 
        } 
        catch(SQLException e) { 
        	logger.error("", e);
        } 
        finally {
        	closeStatementAndRS(stmt, null);
        } 
        return (n >= 1); 
    }
	
	static boolean smUpdateDeactiveSelectionSuccess(Connection conn, String subscriberID,
			String refID, String type, Date startDate,
			String classType, char newLoopStatus, int rbtType)
	{
		int n = -1;
		Statement stmt = null;

		String startTime = "SYSDATE";
		if(startDate != null)
			startTime = sqlTime(startDate);
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			startTime = "SYSDATE()";
			if(startDate != null)
				startTime = mySQLDateTime(startDate);
		}
		
		String prepaid = "n";
		if(type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		String query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL + " = "
				+ startTime + ", " + LOOP_STATUS_COL + " = "
				+ sqlString(String.valueOf(newLoopStatus)) + ", "
				+ PREPAID_YES_COL + " = '" + prepaid + "', "
				+ CLASS_TYPE_COL + " = '" + classType + "' WHERE " + SUBSCRIBER_ID_COL
				+ " = " + "'" + subscriberID + "' AND " + INTERNAL_REF_ID_COL
				+ " = " + sqlString(refID)+ " AND " + SEL_STATUS_COL
				+ " IN ('D', 'P', 'F', 'X','G', '"+STATE_SUSPENDED+"') ";
		if(m_rrbtOn) 
                 query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static boolean smUpdateDeactiveSelectionFailure(Connection conn, String subscriberID,
			String refID, String type, String classType,
			String deactivatedBy, char newLoopStatus, int rbtType)
	{
		int n = -1;
		Statement stmt = null;

		String nextDate = "TO_DATE('20371231','yyyyMMdd')";
		String sysDateStr = "SYSDATE";
		String setTimeCond = "to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr = "SYSDATE()";
			nextDate = "TIMESTAMP('2037-12-31')";
			setTimeCond = "DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s')";
		}
		if(deactivatedBy != null && (deactivatedBy.equalsIgnoreCase("NA") || deactivatedBy.equalsIgnoreCase("NEF") || deactivatedBy.equalsIgnoreCase("AF") || deactivatedBy.equalsIgnoreCase("RF")))
		{
			nextDate = "TO_DATE('20351231','yyyyMMdd')";
			if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
				nextDate = "TIMESTAMP('2035-12-31')";
		}

		String prepaid = "n";
		if(type != null && type.equalsIgnoreCase("p"))
			prepaid = "y";

		String query = "UPDATE " + TABLE_NAME + " SET " +
		START_TIME_COL + " = "+sysDateStr+", " +
		NEXT_CHARGING_DATE_COL + " = " + nextDate + ", " +
		LOOP_STATUS_COL + " = '" + newLoopStatus + "', " +
		PREPAID_YES_COL + " = '" + prepaid + "', " +
		CLASS_TYPE_COL + " = '" + classType + "', " +
		DESELECTED_BY_COL + " = " + sqlString(deactivatedBy) + " WHERE " +
		SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + INTERNAL_REF_ID_COL + " = "+ sqlString(refID) +  " AND " +
		SEL_STATUS_COL + " IN ('D', 'P', 'F','G','X', '"+STATE_SUSPENDED+"') ";//
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n > 0);
	}

	static SubscriberStatus[] getSelectionsForUGCCharging(Connection conn, int fetchSize)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
	 		
		int count = 0;
	 
		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
	 
		query = "SELECT * FROM " + TABLE_NAME + " WHERE "
			+ SELECTION_INFO_COL + " LIKE '%UGC%' AND SEL_STATUS NOT In ('A','N','F','E','W','R', '"+STATE_SUSPENDED+"')"; 
	 
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (count < fetchSize && results.next())
			{				
				subscriberStatus = getSubscriberStatusFromRS(results);				
				subscriberStatusList.add(subscriberStatus);
				count++;
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}
	
	static int getCountSelectionsOtherCallerID(Connection conn, String subID, String callerID, int rbtType) 
    { 
        String query = null; 
        Statement stmt = null; 
        RBTResultSet results = null; 
 
        int count = 0; 
 
        String sysDateStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysDateStr = "SYSDATE()";
		
		
		if(callerID == null)
			query = "SELECT COUNT(DISTINCT(CALLER_ID)) FROM "+ TABLE_NAME + " WHERE "+ SUBSCRIBER_ID_COL + " = '"+ subID + "' AND " 
				+ CALLER_ID_COL + " IS NOT NULL AND  " + END_TIME_COL + " > "+sysDateStr;
        else
			query = "SELECT COUNT(DISTINCT(CALLER_ID)) FROM "+ TABLE_NAME + " WHERE "+ SUBSCRIBER_ID_COL + " = '"+ subID + "' AND " 
			+ CALLER_ID_COL + " != '" + callerID + "' AND  " + END_TIME_COL + " > "+sysDateStr;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
        try 
        { 
            stmt = conn.createStatement(); 
            results = new RBTResultSet(stmt.executeQuery(query)); 
            while (results.next()) 
            { 
                count = results.getInt(1); 
            } 
        } 
        catch (SQLException se) 
        { 
        	logger.error("", se);
            return 0; 
        } 
        finally 
        {
        	closeStatementAndRS(stmt, results);
        } 
        return count; 
    }
	
	static ArrayList<String> getSelectionsToAddToPlayer(Connection conn, int fetchSize, String circleID , boolean isRBT2)
	{
        String query = null; 
        Statement stmt = null; 
        RBTResultSet results = null;
        ArrayList<String> list = new ArrayList<String>();
        int count = 0;
        int totalCount = 0;
        
        // RBT-16004 isRBT2 Added for checking rbt 2
        if(isRBT2){
        	query = "SELECT DISTINCT(" + SUBSCRIBER_ID_COL + ")"
					+ " FROM " + TABLE_NAME + " WHERE "
					+ LOOP_STATUS_COL + " IN " + "("
					+ sqlString(String.valueOf(LOOP_STATUS_LOOP)) + ", "
					+ sqlString(String.valueOf(LOOP_STATUS_OVERRIDE)) + ")"
					+ " AND " + CIRCLE_ID_COL + " like '" + circleID + "%' AND " + END_TIME_COL + " >= SYSDATE() ";
        	
	    }else{
	
	       if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = "SELECT DISTINCT(" + SUBSCRIBER_ID_COL + ")"
						+ " FROM " + TABLE_NAME + " WHERE "
						+ LOOP_STATUS_COL + " IN " + "("
						+ sqlString(String.valueOf(LOOP_STATUS_LOOP)) + ", "
						+ sqlString(String.valueOf(LOOP_STATUS_OVERRIDE)) + ")"
						+ " AND " + CIRCLE_ID_COL + " = " + sqlString(circleID) + " AND " + END_TIME_COL + " >= SYSDATE ";
						
			else
				query = "SELECT DISTINCT(" + SUBSCRIBER_ID_COL + ") "
						+ " FROM " + TABLE_NAME + " WHERE "
						+ LOOP_STATUS_COL + " IN " + "("
						+ sqlString(String.valueOf(LOOP_STATUS_LOOP)) + ", "
						+ sqlString(String.valueOf(LOOP_STATUS_OVERRIDE)) + ")"
						+ " AND " + CIRCLE_ID_COL + " = " + sqlString(circleID) + " AND " + END_TIME_COL + " >= SYSDATE()";
	       
	        }
    
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND ROWNUM <="+fetchSize;
		else
			query += " LIMIT "+fetchSize;

		logger.info("Executing query: " + query);
        try 
        { 
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next()) 
            {
            	list.add(results.getString(SUBSCRIBER_ID_COL));
            	totalCount++;
            } 
             // Sets the player counter statistics
            if (RBTDaemonManager.isFcapsEnabled)
            	CounterStats.getInstance().setPlayerSelActCount(totalCount, new Date());

            SMDaemonPerformanceMonitor.recordDbQueueCount("PlayerSelectionActivationQueue", totalCount);
        } 
        catch (SQLException se) 
        { 
            logger.error("", se);
            return list;
        } 
        finally 
        { 
            try 
            { 
                stmt.close(); 
            } 
            catch (Exception e) 
            { 
                logger.error("", e);
            } 
        }
        return list;
    }
	
	static ArrayList<String> getSelectionsToRemoveFromPlayer(Connection conn, int fetchSize, String circleID, boolean isRBT2)
	{
        String query = null; 
        Statement stmt = null; 
        RBTResultSet results = null;
        ArrayList<String> list = new ArrayList<String>();
        int count = 0;
        int totalCount = 0;
        /**
         * Following query can be used if we send only the added selections to the player
         */
        // RBT-16004 isRBT2 Added for checking rbt 2
        if(isRBT2){
        	query = "SELECT DISTINCT(" + SUBSCRIBER_ID_COL + ") FROM " + TABLE_NAME + " WHERE " +
					LOOP_STATUS_COL + " = " + sqlString(String.valueOf(LOOP_STATUS_EXPIRED_INIT)) + " AND " + CIRCLE_ID_COL + " like '" + circleID + "%' AND " +
					END_TIME_COL + " < SYSDATE() LIMIT "+fetchSize;
	    }else{
	        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
	        	query = "SELECT DISTINCT(" + SUBSCRIBER_ID_COL + ") FROM " + TABLE_NAME + " WHERE " +
					LOOP_STATUS_COL + " = " + sqlString(String.valueOf(LOOP_STATUS_EXPIRED_INIT)) + " AND " + CIRCLE_ID_COL + " = " + sqlString(circleID) + " AND " +
					END_TIME_COL + " < SYSDATE AND ROWNUM <="+fetchSize;
	        else
	        	query = "SELECT DISTINCT(" + SUBSCRIBER_ID_COL + ") FROM " + TABLE_NAME + " WHERE " +
				LOOP_STATUS_COL + " = " + sqlString(String.valueOf(LOOP_STATUS_EXPIRED_INIT)) + " AND " + CIRCLE_ID_COL + " = " + sqlString(circleID) + " AND " +
				END_TIME_COL + " < SYSDATE() LIMIT "+fetchSize;
	        
	    }
		logger.info("Executing query: " + query);
        try 
        { 
            stmt = conn.createStatement(); 
            results = new RBTResultSet(stmt.executeQuery(query)); 
            while (results.next())
            { 
            	list.add(results.getString(SUBSCRIBER_ID_COL));
            	totalCount++;
            }
            if (RBTDaemonManager.isFcapsEnabled)
            	CounterStats.getInstance().setPlayerSelDctCount(totalCount, new Date());

            SMDaemonPerformanceMonitor.recordDbQueueCount("PlayerSelectionDeactivationQueue", totalCount);
        }
        catch (SQLException se) 
        { 
            logger.error("", se);
        } 
        finally 
        {
        	closeStatementAndRS(stmt, results);
        }
        return list;
    }
	
	static boolean updateAddedSelectionsInPlayer(Connection conn, String subscriberID, char loopStatus, ArrayList<String> refIdList)
	{
		Statement stmt = null;
		int n = -1;
		
		char newLoopStatus = LOOP_STATUS_LOOP_FINAL;
		if(loopStatus == LOOP_STATUS_OVERRIDE)
			newLoopStatus = LOOP_STATUS_OVERRIDE_FINAL;
		
		String refIdStr = "";
		boolean isNullExists = false;
		for(int i=0; i<refIdList.size();i++)
		{
			String refId = refIdList.get(i); 
			if(refId == null){
				isNullExists = true;
				continue;
			}
			refIdStr += "'" + refId + "',";
		}

		if(refIdStr.endsWith(","))
			refIdStr = refIdStr.substring(0,refIdStr.length()-1);
				
		String query = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB)){
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL + " = " +
				sqlString(String.valueOf(newLoopStatus)) + " WHERE " + SUBSCRIBER_ID_COL +
				" = " + sqlString(subscriberID) + " AND " + END_TIME_COL + " > SYSDATE ";
		} else {
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL + " = " +
			sqlString(String.valueOf(newLoopStatus)) + " WHERE " + SUBSCRIBER_ID_COL +
			" = " + sqlString(subscriberID) + " AND " + END_TIME_COL + " > SYSDATE() " ;
		}
			
		if(!refIdStr.equalsIgnoreCase("")) {	
			query += " AND (" + INTERNAL_REF_ID_COL +" IN ("+refIdStr+") ";
		}
		if(isNullExists) {	
			if(refIdStr.equalsIgnoreCase(""))
				query+= " AND  "+INTERNAL_REF_ID_COL + " IS NULL ";
			else
				query+= " OR "+INTERNAL_REF_ID_COL + " IS NULL) ";
		} else {
			query += ")";
		}

		query += " AND "+ LOOP_STATUS_COL + " = " + sqlString(String.valueOf(loopStatus));
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException e)
		{
			logger.error("", e);
			return false;
		}
		finally 
        {
			closeStatementAndRS(stmt, null);
        }
		logger.info("RBT:: returning " + (n >= 0));
		return (n >= 0);
	}
	
	static boolean updateRemovedSelectionsFromPlayer(Connection conn, String subscriberID, ArrayList<String> refIdList)
	{
		Statement stmt = null;
		int n = -1;
		String refIdStr = "";
		boolean isNullExists = false;
		for(int i=0; i<refIdList.size();i++)
		{
			String refId = refIdList.get(i); 
			if(refId == null){
				isNullExists = true;
				continue;
			}
			refIdStr += "'" + refId + "',";
		}
		if(refIdStr.endsWith(","))
			refIdStr = refIdStr.substring(0, refIdStr.length()-1);
		
		String query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL + " = " +
				sqlString(String.valueOf(LOOP_STATUS_EXPIRED)) + " WHERE " + SUBSCRIBER_ID_COL +
				" = " + sqlString(subscriberID)  ;
		
		if("".equals(refIdStr)) {
			query += " AND  " + INTERNAL_REF_ID_COL + " IS NULL ";
		} else {
			if(isNullExists) {
				query += " AND (" + INTERNAL_REF_ID_COL +" IN ("+refIdStr+") OR " +INTERNAL_REF_ID_COL + " IS NULL) ";
			} else {
				query += " AND " + INTERNAL_REF_ID_COL +" IN ("+refIdStr+") ";
			}
		}
		
		query += " AND "+ LOOP_STATUS_COL + " = " + sqlString(String.valueOf(LOOP_STATUS_EXPIRED_INIT));
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		} catch(SQLException e) {
			logger.error("", e);
			return false;
		} finally {
			closeStatementAndRS(stmt, null);
        }
		logger.info("RBT:: returning " + (n >= 0));
		return (n >= 0);
	}
	
	
	/*ADDED FOR TATA*/
	static SubscriberStatus[] getSubscriberStatus(Connection conn, String subID, String subWavFile, int rbtType )
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)){
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subID + "' AND "
					+ SUBSCRIBER_WAV_FILE_COL + " = '" + subWavFile + "' AND "
					+ SET_TIME_COL + " <= SYSDATE  AND " + END_TIME_COL
					+ " > SYSDATE  ";//ORDER BY " + CALLER_ID_COL + " DESC";
			if(m_rrbtOn) 
                query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
			query += " ORDER BY "+ CALLER_ID_COL + " DESC";

		}
		else{
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = '" + subID + "' AND "
					+ SUBSCRIBER_WAV_FILE_COL + " = '" + subWavFile + "' AND "
					+ SET_TIME_COL + " <= SYSDATE()  AND " + END_TIME_COL
					+ " > SYSDATE() ";// ORDER BY " + CALLER_ID_COL + " DESC";
			if(m_rrbtOn) 
                query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
			query += " ORDER BY "+ CALLER_ID_COL + " DESC";
		}

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	 static boolean expireUGCSelections(Connection conn, String wavFile)
	 {
            Statement stmt = null;
            String sysDateStr = "SYSDATE";
            if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
            	sysDateStr = "SYSDATE()";
            String query1 = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIBER_WAV_FILE_COL + " = 'default' ,"
            		+ LOOP_STATUS_COL + " = " + sqlString(String.valueOf(LOOP_STATUS_LOOP))
            		+ " WHERE " + SUBSCRIBER_WAV_FILE_COL + " ='" + wavFile +  "' AND "
            		+ END_TIME_COL + " > "+sysDateStr+" AND " + LOOP_STATUS_COL + " = "
            		+ sqlString(String.valueOf(LOOP_STATUS_LOOP_FINAL));

            String query2 = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIBER_WAV_FILE_COL + " = 'default' ,"
		    		+ LOOP_STATUS_COL + " = " + sqlString(String.valueOf(LOOP_STATUS_OVERRIDE))
		    		+ " WHERE " + SUBSCRIBER_WAV_FILE_COL + " ='" + wavFile +  "' AND "
		    		+ END_TIME_COL + " > "+sysDateStr+" AND " + LOOP_STATUS_COL + " = "
		    		+ sqlString(String.valueOf(LOOP_STATUS_OVERRIDE_FINAL));

			String query3 = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIBER_WAV_FILE_COL + " = 'default' WHERE "
					+ SUBSCRIBER_WAV_FILE_COL + " ='" + wavFile +  "' AND "+ END_TIME_COL + " > "+sysDateStr
					+ " AND " + LOOP_STATUS_COL + " IN (" + sqlString(String.valueOf(LOOP_STATUS_OVERRIDE))
					+ ", " + sqlString(String.valueOf(LOOP_STATUS_OVERRIDE))+ ")";
			
            try
            {
                stmt = conn.createStatement();
    			logger.info("Executing query: " + query3);
    			RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query3, Constants.SQL_TYPE_LOGGER);
                stmt.executeUpdate(query3);
    			logger.info("Executing query: " + query1);
    			RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query1, Constants.SQL_TYPE_LOGGER);
                stmt.executeUpdate(query1);
    			logger.info("Executing query: " + query2);
    			RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query2, Constants.SQL_TYPE_LOGGER);
                stmt.executeUpdate(query2);
            }
            catch (Exception se)
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
	 
	 static SubscriberStatus isShufflePresentSelection(Connection conn, String subID, String callerID, int rbtType)
	 {
        Statement stmt = null; 
        ResultSet results = null;
        SubscriberStatus subscriberStatus = null;

        String sysDateStr = "SYSDATE";
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	sysDateStr = "SYSDATE()";
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE STATUS NOT IN (0,99) AND " +
				END_TIME_COL + " > "+sysDateStr+" AND "+SUBSCRIBER_ID_COL + " = '"+subID + "'";
		if(callerID != null)
       		query += " AND ("+ CALLER_ID_COL + " IS NULL OR "+ CALLER_ID_COL + " = '"+callerID + "')";
		else
			query += " AND "+ CALLER_ID_COL + " IS NULL";

       if(m_rrbtOn) 
           query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

       query += " ORDER BY SET_TIME DESC";
         
		logger.info("Executing query: " + query);
        try 
        { 
            stmt = conn.createStatement(); 
            results = stmt.executeQuery(query); 
            if (results.next()) { 
            	subscriberStatus = getSubscriberStatusFromRS(results); 
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
        
        return subscriberStatus;
    }
    
	 static SubscriberStatus[] getSubscriberCallerSelectionsInLoop(Connection conn, String subID, String callID, int rbtType)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
		
		String sysDateStr = "SYSDATE";
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	sysDateStr = "SYSDATE()";
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "+CALLER_ID_COL + getNullForWhere(callID) + " AND ";

		query = query + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND "+ STATUS_COL + " = 1 AND " + SEL_STATUS_COL + " NOT IN ('E','F','D','P','X','Z') ";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
		query += " ORDER BY SET_TIME";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}

	static SubscriberStatus[] getPersonalCallerIDSelections(Connection conn, String subID,
			String callerID, int rbtType) {
		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String sysDateStr = "SYSDATE";
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	sysDateStr = "SYSDATE()";
        query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subID
				+ "' AND ";
		query = query + CALLER_ID_COL + " != '" + callerID + "' AND " + CALLER_ID_COL
				+ " IS NOT NULL AND ";

		query = query + SET_TIME_COL + " <= "+sysDateStr+" AND " + END_TIME_COL + " > "+sysDateStr+" AND "
				+ STATUS_COL + " = 1 AND " + SEL_STATUS_COL
				+ " NOT IN ('E','F','D','P','X') ";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 
		query += " ORDER BY SET_TIME";
		
		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next()) {
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	}
	
	static void deactivateNewSelections(Connection conn, String subscriberId, String deselectedBy, String callerId, 
			     Date setDate, boolean checkCallerId, int rbtType, List<String> refIDList, String status,String circleId) { 
        
        String endTime = "SYSDATE";
        String setTimeStr = sqlTime(setDate);
        String circleIdQuery = "";
        if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
        if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
        	endTime = "SYSDATE()";
        	setTimeStr = mySQLDateTime(setDate);
        }

        String refIDStr = "";
        if (refIDList != null)
        {
        	for (int i = 0; i < refIDList.size(); i++)
        	{
        		if (i < refIDList.size() - 1)
        			refIDStr += "'" + refIDList.get(i) + "',";
        		else
        			refIDStr += "'" + refIDList.get(i) + "'";
        	}
        }

        String query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL + " = " + endTime +", " 
                        + DESELECTED_BY_COL + " = " + sqlString(deselectedBy) + ", " + SEL_STATUS_COL 
                        + " = " + sqlString(STATE_DEACTIVATED) + ", " + LOOP_STATUS_COL 
                        + " = " + sqlString(String.valueOf(LOOP_STATUS_EXPIRED_INIT))
                        + circleIdQuery
                        + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId) 
                        + " AND "+SEL_STATUS_COL + " = " + sqlString(STATE_BASE_ACTIVATION_PENDING);
        
                    if(status!=null)
                    	query+= " AND " + STATUS_COL + " IN (" + status + ")";
                    
        			if (checkCallerId)
                        query+= " AND "+ CALLER_ID_COL + getNullForWhere(callerId);
        
        			if(setTimeStr != null)
                         query += " AND "+SET_TIME_COL +" < "+ setTimeStr;
        			
        			if(m_rrbtOn) 
        				query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

        			if (refIDStr.length() > 0)
        				query += " AND " + INTERNAL_REF_ID_COL + " IN (" + refIDStr + ")";
        			

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        Statement stmt = null; 
        try { 
            stmt = conn.createStatement(); 
            int n = stmt.executeUpdate(query); 
            logger.info("RBT:: number of rows updated is " + n); 
        } 
        catch (SQLException e) { 
            logger.error("", e); 
        } 
        finally { 
			closeStatementAndRS(stmt, null);
        } 
	} 
	
	static boolean smSelectionGrace(Connection conn, String strSubID, String refID, String type, int rbtType, char stat, String circleId) {

       int n = -1; 
       String query = null; 
       Statement stmt = null; 
       String prepaid = "n"; 
       String circleIdQuery = "";
       if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
       if(type != null && type.equalsIgnoreCase("p")) 
               prepaid = "y"; 

	   	query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL
		+ " = '" + STATE_ACTIVATION_GRACE + "', "
		+PREPAID_YES_COL + " = '" + prepaid + "', " +
         LOOP_STATUS_COL + " = " + sqlString(String.valueOf(stat)) +circleIdQuery+ " WHERE "
		+ SUBSCRIBER_ID_COL + " = " + "'" + strSubID + "' AND "
		+ INTERNAL_REF_ID_COL +  " = " + sqlString(refID) + " AND " + SEL_STATUS_COL
		+ " IN ('" + STATE_TO_BE_ACTIVATED + "','"
		+ STATE_ACTIVATION_PENDING + "','"
		+ STATE_ACTIVATION_ERROR + "') ";

    	if(m_rrbtOn) 
           query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
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
           return false; 
       } 
       finally 
       { 
			closeStatementAndRS(stmt, null);
       } 
       return (n > 0); 
   } 
   
	static boolean deactivateSubscriberGraceRecords(Connection conn, String subscriberID, String callerID, int status, int fromTime, int toTime, String deSelectedBy, int rbtType){
		int n = -1;
		String query = null;
		Statement stmt = null;
		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			nextChargingDate = "TIMESTAMP('2037-12-31')";
		}
		String sel_status = "D";
		if (callerID == null) {
			if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
				query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL
						+ " = SYSDATE " + ", " + NEXT_CHARGING_DATE_COL + " = "
						+ nextChargingDate;
				query = query + "," + SEL_STATUS_COL + " = "
						+ sqlString(sel_status) + " , " + DESELECTED_BY_COL
						+ " = " + sqlString(deSelectedBy);
				query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
						+ subscriberID + "' AND " + CALLER_ID_COL
						+ " IS NULL AND " + STATUS_COL + " = " + status
						+ " AND " + FROM_TIME_COL + " = " + fromTime + " AND "
						+ TO_TIME_COL + " = " + toTime;
			} else {
				query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL
						+ " = SYSDATE() " + ", " + NEXT_CHARGING_DATE_COL
						+ " = " + nextChargingDate;
				query = query + "," + SEL_STATUS_COL + " = "
						+ sqlString(sel_status) + " , " + DESELECTED_BY_COL
						+ " = " + sqlString(deSelectedBy);
				query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
						+ subscriberID + "' AND " + CALLER_ID_COL
						+ " IS NULL AND " + STATUS_COL + " = " + status
						+ " AND " + FROM_TIME_COL + " = " + fromTime + " AND "
						+ TO_TIME_COL + " = " + toTime;
			}
		} else {
			if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
				query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL
						+ " = SYSDATE " + ", " + NEXT_CHARGING_DATE_COL + " = "
						+ nextChargingDate;
				query = query + "," + SEL_STATUS_COL + " = "
						+ sqlString(sel_status) + " , " + DESELECTED_BY_COL
						+ " = " + sqlString(deSelectedBy);
				query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
						+ subscriberID + "' AND " + CALLER_ID_COL + " = '"
						+ callerID + "' AND " + STATUS_COL + " = " + status
						+ " AND " + FROM_TIME_COL + " = " + fromTime + " AND "
						+ TO_TIME_COL + " = " + toTime;
			} else {
				query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL
						+ " = SYSDATE() " + ", " + NEXT_CHARGING_DATE_COL
						+ " = " + nextChargingDate;
				query = query + "," + SEL_STATUS_COL + " = "
						+ sqlString(sel_status) + " , " + DESELECTED_BY_COL
						+ " = " + sqlString(deSelectedBy);
				query = query + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'"
						+ subscriberID + "' AND " + CALLER_ID_COL + " = '"
						+ callerID + "' AND " + STATUS_COL + " = " + status
						+ " AND " + FROM_TIME_COL + " = " + fromTime + " AND "
						+ TO_TIME_COL + " = " + toTime;
			}
		}
		query = query + " AND " + SEL_STATUS_COL + " = '"
				+ STATE_ACTIVATION_GRACE + "' ";
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		} catch (SQLException se) {
			logger.error("", se);
			return false;
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	} 	
	static SubscriberStatus [] getAllSubscriberSelectionRecordsNotDeactivated(Connection conn) 
    { 
        String query = null; 
        Statement stmt = null; 
        ResultSet results = null; 

        SubscriberStatus subscriberStatus = null; 
        List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>(); 

        
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
		query = "SELECT * FROM " + TABLE_NAME + " WHERE "+ END_TIME_COL + " < SYSDATE AND " + SEL_STATUS_COL + "= " + sqlString(STATE_ACTIVATED) ; 
		} else {
		query = "SELECT * FROM " + TABLE_NAME + " WHERE "+ END_TIME_COL + " < SYSDATE() AND " + SEL_STATUS_COL + "= " + sqlString(STATE_ACTIVATED) ; 
		}
		
		logger.info("Executing query: " + query);
        try 
        { 
            stmt = conn.createStatement(); 
            results = stmt.executeQuery(query); 
            while(results.next()) 
            { 
                subscriberStatus = getSubscriberStatusFromRS(results); 
                subscriberStatusList.add(subscriberStatus); 
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
    } 

	static SubscriberStatus [] getSubscriberSelectionsNotDeactivated(Connection conn,String subscriberID,String wavFile) 
    { 
        String query = null; 
        Statement stmt = null; 
        ResultSet results = null; 

        SubscriberStatus subscriberStatus = null; 
        List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>(); 

        
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_TIME_COL + " > SYSDATE AND "
					+ SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(wavFile) + " AND " + SUBSCRIBER_ID_COL + " = " +sqlString(subscriberID); 
		} else {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_TIME_COL + " > SYSDATE() AND "
					+ SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(wavFile) + " AND " + SUBSCRIBER_ID_COL + " = " +sqlString(subscriberID);
		}
		
		logger.info("Executing query: " + query);
        try 
        { 
            stmt = conn.createStatement(); 
            results = stmt.executeQuery(query); 
            while(results.next()) 
            { 
                subscriberStatus = getSubscriberStatusFromRS(results); 
                subscriberStatusList.add(subscriberStatus); 
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
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS for subscriber id "
				+ subscriberID + "WAVFILE = " + wavFile + "successfully. Total rows: "
				+ subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
    } 

	static boolean smSelectionSuspend(Connection conn, String strSubID, String refID, 
			char newLoopStatus, int rbtType, String circleId){

		int n = -1;
		String query = null;
		Statement stmt = null;
		String circleIdQuery = "";
        if (circleId != null) {
			circleIdQuery += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
			query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = '"
			+ STATE_SUSPENDED + "', " + LOOP_STATUS_COL + " = "
			+ sqlString(String.valueOf(newLoopStatus)) +circleIdQuery+ " WHERE "
			+ SUBSCRIBER_ID_COL + " = " + "'" + strSubID + "' AND "
			+ INTERNAL_REF_ID_COL + " = " + sqlString(refID) + " AND " + SEL_STATUS_COL + " = '" + STATE_ACTIVATED
			+ "'";
		
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		} catch (SQLException se) {
			logger.error("", se);
			return false;
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	} 
	
	static boolean updateSelecionToGrace(Connection conn, String subscriberID, String callerID, 
		            String subscriberWavFile, Date nextRetryDate, String selInfo) { 
		
		String query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
			+ sqlString(STATE_GRACE) + ", " + NEXT_CHARGING_DATE_COL + " = "
			+ sqlTime(nextRetryDate) + ", " + SELECTION_INFO_COL + " = " + sqlString(selInfo)
			+ " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "; 
		
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
				+ sqlString(STATE_GRACE) + ", " + NEXT_CHARGING_DATE_COL + " = "
				+ mySQLDateTime(nextRetryDate) + ", " + SELECTION_INFO_COL + " = "
				+ sqlString(selInfo) + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND "; 
		
		if(callerID == null) 
		    query += CALLER_ID_COL + " IS NULL "; 
		else 
		    query += CALLER_ID_COL + " = " + sqlString(callerID); 
		
		query += " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subscriberWavFile); 
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null; 
		int n = -1; 
		try { 
		            stmt = conn.createStatement(); 
		            n = stmt.executeUpdate(query); 
		    } 
		    catch (SQLException e) { 
		            logger.error("", e); 
		    } 
		    finally { 
				closeStatementAndRS(stmt, null);
		    } 
		    return (n >= 1); 
		} 

	static ArrayList getSelectionGraceRecords(Connection conn, int fetchSize) { 
	    String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL + " = " 
	                    + sqlString(STATE_GRACE) + " AND " + NEXT_CHARGING_DATE_COL + " <= SYSDATE"; 
	    if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
	    	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SEL_STATUS_COL + " = " 
            + sqlString(STATE_GRACE) + " AND " + NEXT_CHARGING_DATE_COL + " <= SYSDATE()"; 
	    
		logger.info("Executing query: " + query);
	    Statement stmt = null; 
	    ResultSet rs = null; 
	    ArrayList graceSubs = new ArrayList(); 
	
	    try { 
	            stmt = conn.createStatement(); 
	            stmt.setMaxRows(fetchSize); 
	            rs = stmt.executeQuery(query); 
	            while(rs.next()) 
	                    graceSubs.add(getSubscriberStatusFromRS(rs)); 
	    } 
	    catch (SQLException e) { 
	            logger.error("", e); 
	    }
	    finally
	    {
			closeStatementAndRS(stmt, rs);
	    }
	
	    if(graceSubs.size() > 0) { 
	            logger.info("RBT::retrieving records from " + TABLE_NAME + " successful"); 
	            return graceSubs; 
	    } 
	    else { 
	            logger.info("RBT::no records to retrieve from " + TABLE_NAME); 
	            return null; 
	    } 
	} 
	
	static boolean deleteDeactivatedRecords(Connection conn,String subscriberId){
		
	    String query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId) + " AND " + SEL_STATUS_COL + " = " 
	                    + sqlString(STATE_DEACTIVATED) ; 
	    
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null; 
		int n = -1; 
		try { 
	            stmt = conn.createStatement(); 
	            n = stmt.executeUpdate(query); 
	    } 
	    catch (SQLException e) { 
	            logger.error("", e); 
	    } 
	    finally { 
			closeStatementAndRS(stmt, null);
	    } 
	    return (n > 0); 
	}
	
	static ArrayList<SubscriberStatus> playerGetActivatedSels(Connection conn, int fetchSize) {

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + LOOP_STATUS_COL + " IN ("
				+ sqlString(String.valueOf(LOOP_STATUS_LOOP)) + ", "
				+ sqlString(String.valueOf(LOOP_STATUS_OVERRIDE)) + ") AND " + END_TIME_COL + " > ";
		if(m_databaseType.equals(DB_MYSQL))
			query += MYSQL_SYSDATE;
		else
			query += SAPDB_SYSDATE;
		
		query += " ORDER BY " + SET_TIME_COL;
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		ArrayList<SubscriberStatus> selList = new ArrayList<SubscriberStatus>();
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
			rs = stmt.executeQuery(query);
			while(rs.next())
				selList.add(getSubscriberStatusFromRS(rs));
		}
		catch(SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatementAndRS(stmt, rs);
		}

		if (selList.size() > 0) {
			logger.info("RBT::retrieving records from " + TABLE_NAME
					+ " successful");
			return selList;
		}
		else {
			logger.info("RBT::no records to retrive from " + TABLE_NAME);
			return null;
		}
	}
	
	static ArrayList<SubscriberStatus> playerGetRemovedSels(Connection conn, int fetchSize) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + LOOP_STATUS_COL + " = "
				+ sqlString(String.valueOf(LOOP_STATUS_EXPIRED_INIT)) + " AND " + END_TIME_COL
				+ " <= ";
		if(m_databaseType.equals(DB_MYSQL))
			query += MYSQL_SYSDATE;
		else
			query += SAPDB_SYSDATE;

		query += " ORDER BY " + END_TIME_COL;

		logger.info("Executing query: " + query);
		ArrayList<SubscriberStatus> selList = new ArrayList<SubscriberStatus>();
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
			rs = stmt.executeQuery(query);
			while(rs.next())
				selList.add(getSubscriberStatusFromRS(rs));
		}
		catch(SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatementAndRS(stmt, rs);
		}

		if(selList.size() > 0) {
			logger.info("RBT::retrieving records from " + TABLE_NAME
					+ " successful");
			return selList;
		}
		else {
			logger.info("RBT::no records to retrive from " + TABLE_NAME);
			return null;
		}
	}
	
	static boolean updateLoopStatus(Connection conn, SubscriberStatus setting, char newStatus,
			String selInfo) {
		
		String query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL + " = "
				+ sqlString(String.valueOf(newStatus));
		
		if(selInfo != null)
			query += ", " + SELECTION_INFO_COL + " = " + sqlString(selInfo);
		
		query += " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(setting.subID()) + " AND " + INTERNAL_REF_ID_COL
				+ getNullForWhere(setting.refID());
		
		//commented changed by Sreekar
		/*query += " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(setting.subID()) + " AND " + CALLER_ID_COL
				+ getNullForWhere(setting.callerID());

		DateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		if(m_databaseType.equals(DB_SAPDB))
			query += " AND to_char(" + SET_TIME_COL + ",'yyyyMMddhh24miss') " + " = "
					+ sqlString(timeFormat.format(setting.setTime()));
		else
			query += " AND DATE_FORMAT(" + SET_TIME_COL + ",'%Y%m%d%H%i%s') " + " = "
					+ sqlString(timeFormat.format(setting.setTime()));

		query += " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(setting.subscriberFile())
				+ " AND " + STATUS_COL + " = " + setting.status() + " AND " + FROM_TIME_COL + " = "
				+ setting.fromTime() + " AND " + TO_TIME_COL + " = " + setting.toTime();*/
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		int n = -1;
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatementAndRS(stmt, null);
		}
		
		return n > 0;
	}
	
	static void updateSettingsForDownloadRenewalSuccessCallback(Connection conn, String subscriberID,
			String wavFile, char newLoopStatus, char oldLoopStatus) {
		String query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL + " = "
				+ sqlString(newLoopStatus + "") + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + SEL_STATUS_COL + " = "
				+ sqlString(STATE_ACTIVATED) + " AND " + SUBSCRIBER_WAV_FILE_COL + " = "
				+ sqlString(wavFile) + " AND " + LOOP_STATUS_COL + " = "
				+ sqlString(oldLoopStatus + "");
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch(Exception e) {
			logger.error("", e);
		}
		finally
	    {
			closeStatementAndRS(stmt, null);
	    }
	}
	
  static boolean updateSettingsForDownloadCallback(Connection conn, String subscriberID, String wavFile,
			String selStatus, boolean isSuspension) 
	{
		int n = 0;
		String query = null;
		if(m_databaseType.equals(DB_MYSQL))
		{
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL
				+ " = IF(" + LOOP_STATUS_COL + " IN ('" + LOOP_STATUS_LOOP_INIT
				+ "','" + LOOP_STATUS_LOOP + "','" + LOOP_STATUS_LOOP_FINAL
				+ "'),'" + LOOP_STATUS_LOOP + "','" + LOOP_STATUS_OVERRIDE
				+ "')" + ", " + SEL_STATUS_COL + " = " + sqlString(selStatus)
				+ " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + SUBSCRIBER_WAV_FILE_COL
				+ " = " + sqlString(wavFile);
			if (isSuspension)
				query += " AND " + SEL_STATUS_COL + " IN ('" + STATE_ACTIVATED
						+ "','" + STATE_ACTIVATION_ERROR + "','"
						+ STATE_ACTIVATION_PENDING + "','"
						+ STATE_TO_BE_ACTIVATED + "','"+ STATE_BASE_ACTIVATION_PENDING+"')";
			else
				query += " AND " + SEL_STATUS_COL + " IN ('" + STATE_SUSPENDED +"','"+STATE_SUSPENDED_INIT+"')";
		
		}
		else
		{
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL
				+ " =(CASE WHEN " + LOOP_STATUS_COL + " IN('"
				+ LOOP_STATUS_LOOP_INIT + "','" + LOOP_STATUS_LOOP + "','"
				+ LOOP_STATUS_LOOP_FINAL + "') THEN '" + LOOP_STATUS_LOOP
				+ "' ELSE '" + LOOP_STATUS_OVERRIDE + "' END)" + ", " + SEL_STATUS_COL + " = " + sqlString(selStatus)
				+ " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + SUBSCRIBER_WAV_FILE_COL
				+ " = " + sqlString(wavFile);
			
			if (isSuspension)
				query += " AND " + SEL_STATUS_COL + " IN ('" + STATE_ACTIVATED
						+ "','" + STATE_ACTIVATION_ERROR + "','"
						+ STATE_ACTIVATION_PENDING + "','"
						+ STATE_TO_BE_ACTIVATED + "','"+ STATE_BASE_ACTIVATION_PENDING+"')";
			else
				query += " AND " + SEL_STATUS_COL + " IN ('" + STATE_SUSPENDED +"','"+STATE_SUSPENDED_INIT+"')";
		}

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(Exception e) {
			logger.error("", e);
		}
		finally
	    {
			closeStatementAndRS(stmt, null);
	    }
		return (n > 0);
	}
  
  static boolean updateSettingsForPackSuspensionCallback(Connection conn, String subscriberID, String selStatus , boolean isProfile) 
	{
		int n = 0;
		String query = null;
		if(m_databaseType.equals(DB_MYSQL))
		{
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL
				+ " = IF(" + LOOP_STATUS_COL + " IN ('" + LOOP_STATUS_LOOP_INIT
				+ "','" + LOOP_STATUS_LOOP + "','" + LOOP_STATUS_LOOP_FINAL
				+ "'),'" + LOOP_STATUS_LOOP + "','" + LOOP_STATUS_OVERRIDE
				+ "')" + ", " + SEL_STATUS_COL + " = " + sqlString(selStatus)
				+ " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + STATUS_COL;
			if(isProfile)
				query = query + " IN ('99')";
			else
				query = query + " NOT IN ('99','90')";
			query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_ACTIVATED
				+ "','" + STATE_ACTIVATION_ERROR + "','"
				+ STATE_ACTIVATION_PENDING + "','"
				+ STATE_TO_BE_ACTIVATED + "','"+ STATE_BASE_ACTIVATION_PENDING+"')";
		
		}
		else
		{
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL
				+ " =(CASE WHEN " + LOOP_STATUS_COL + " IN('"
				+ LOOP_STATUS_LOOP_INIT + "','" + LOOP_STATUS_LOOP + "','"
				+ LOOP_STATUS_LOOP_FINAL + "') THEN '" + LOOP_STATUS_LOOP
				+ "' ELSE '" + LOOP_STATUS_OVERRIDE + "' END)" + ", " + SEL_STATUS_COL + " = " + sqlString(selStatus)
				+ " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberID) + " AND " + STATUS_COL;
			if(isProfile)
				query = query + " IN ('99')";
			else
				query = query + " NOT IN ('99','90')";
			query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_ACTIVATED
				+ "','" + STATE_ACTIVATION_ERROR + "','"
				+ STATE_ACTIVATION_PENDING + "','"
				+ STATE_TO_BE_ACTIVATED + "','"+ STATE_BASE_ACTIVATION_PENDING+"')";
		}

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(Exception e) {
			logger.error("", e);
		}
		finally
	    {
			closeStatementAndRS(stmt, null);
	    }
		return (n > 0);
	}

	/**
	 * Activates the suspended selections after getting the Download renewal success call back
	 * Updates selStatus -B , loopStatus - O/L where selStatus in Z/z
	 * @param conn
	 * @param subscriberID
	 * @param wavFile
	 * @param categoryId 
	 * @param categoryType 
	 * @return
	 *
	 */
	static boolean activateSuspendedSettingsForDownloadRenewalCallback(Connection conn, String subscriberID, String wavFile, Integer categoryId, Integer categoryType) 
	{
		int n = 0;
		String query = null;
		if(m_databaseType.equals(DB_SAPDB))
		{
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL
					+ " =(CASE WHEN " + LOOP_STATUS_COL + "IN('"
					+ LOOP_STATUS_LOOP_INIT + "','" + LOOP_STATUS_LOOP + "','"
					+ LOOP_STATUS_LOOP_FINAL + "') THEN '" + LOOP_STATUS_LOOP
					+ "' ELSE '" + LOOP_STATUS_OVERRIDE + "' END)" + ","
					+ SEL_STATUS_COL + " = " + sqlString(STATE_ACTIVATED) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND "; 
			if (categoryType != null && categoryId != null && Utility.isShuffleCategory(categoryType)) {
				query = query + CATEGORY_ID_COL + " = " + sqlString(categoryId.toString());
			} else {
				query = query + SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(wavFile); 
			}
			query = query + " AND " + SEL_STATUS_COL + " IN ("
					+ sqlString(STATE_SUSPENDED) + ","
					+ sqlString(STATE_SUSPENDED_INIT) + ")";
		}
		else
		{
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL
					+ " = IF(" + LOOP_STATUS_COL + " IN ('"
					+ LOOP_STATUS_LOOP_INIT + "','" + LOOP_STATUS_LOOP + "','"
					+ LOOP_STATUS_LOOP_FINAL + "'),'" + LOOP_STATUS_LOOP
					+ "','" + LOOP_STATUS_OVERRIDE + "')" + ","
					+ SEL_STATUS_COL + " = " + sqlString(STATE_ACTIVATED) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND ";
			if (categoryType != null && categoryId != null && Utility.isShuffleCategory(categoryType)) {
				query = query + CATEGORY_ID_COL + " = " + sqlString(categoryId.toString());
			} else {
				query = query + SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(wavFile); 
			}
			
			query = query + " AND " + SEL_STATUS_COL + " IN ("
					+ sqlString(STATE_SUSPENDED) + ","
					+ sqlString(STATE_SUSPENDED_INIT) + ")";

		}

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(Exception e) {
			logger.error("", e);
		}
		finally
	    {
			closeStatementAndRS(stmt, null);
	    }
		return (n > 0);
	}
	
	static boolean activateSuspendedSettingsForPack(Connection conn, String subscriberID , boolean isProfile) 
	{
		int n = 0;
		String query = null;
		if(m_databaseType.equals(DB_SAPDB))
		{
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL
					+ " =(CASE WHEN " + LOOP_STATUS_COL + "IN('"
					+ LOOP_STATUS_LOOP_INIT + "','" + LOOP_STATUS_LOOP + "','"
					+ LOOP_STATUS_LOOP_FINAL + "') THEN '" + LOOP_STATUS_LOOP
					+ "' ELSE '" + LOOP_STATUS_OVERRIDE + "' END)" + ","
					+ SEL_STATUS_COL + " = " + sqlString(STATE_ACTIVATED) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + STATUS_COL ;
				if(isProfile)
					query = query + " IN ('99')";
				else
					query = query + " NOT IN ('99','90')";
			query = query + " AND " + SEL_STATUS_COL + " IN ("
					+ sqlString(STATE_SUSPENDED) + ","
					+ sqlString(STATE_SUSPENDED_INIT) + ")";
		}
		else
		{
			query = "UPDATE " + TABLE_NAME + " SET " + LOOP_STATUS_COL
					+ " = IF(" + LOOP_STATUS_COL + " IN ('"
					+ LOOP_STATUS_LOOP_INIT + "','" + LOOP_STATUS_LOOP + "','"
					+ LOOP_STATUS_LOOP_FINAL + "'),'" + LOOP_STATUS_LOOP
					+ "','" + LOOP_STATUS_OVERRIDE + "')" + ","
					+ SEL_STATUS_COL + " = " + sqlString(STATE_ACTIVATED) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + STATUS_COL ;
			if(isProfile)
				query = query + " IN ('99')";
			else
				query = query + " NOT IN ('99','90')";
			query = query + " AND " + SEL_STATUS_COL + " IN ("
					+ sqlString(STATE_SUSPENDED) + ","
					+ sqlString(STATE_SUSPENDED_INIT) + ")";

		}

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(Exception e) {
			logger.error("", e);
		}
		finally
	    {
			closeStatementAndRS(stmt, null);
	    }
		return (n > 0);
	}

	static boolean removeSelectionByRefID(Connection conn, String subID, String refID)
	{
		if(refID == null) {
			return false;
		}
		int n = -1;
		Statement stmt = null;

		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'" + " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID);

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}

	static boolean smDeactivateSubscriberSelections(Connection conn,
			String subscriberID, String deselectedBy,
			Map<String, String> whereClauseMap)
	{
		int updateCount = -1;
		Statement stmt = null;

		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		String endTimeStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextChargingDate = "TIMESTAMP('2037-12-31')";
			endTimeStr = "SYSDATE()";
		}
		String sel_status = STATE_DEACTIVATION_PENDING;

		String query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL + " = "
				+ endTimeStr + ", " + NEXT_CHARGING_DATE_COL + " = "
				+ nextChargingDate + "," + SEL_STATUS_COL + " = "
				+ sqlString(sel_status) + " , " + DESELECTED_BY_COL + " = "
				+ sqlString(deselectedBy);
		
		query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		if (whereClauseMap.containsKey("CALLER_ID"))
		{
			String callerID = whereClauseMap.get("CALLER_ID");
			query += " AND " + CALLER_ID_COL + getNullForWhere(callerID);
		}
		if (whereClauseMap.containsKey("STATUS"))
		{
			String status = whereClauseMap.get("STATUS");
			query += " AND " + STATUS_COL + " = " + status;
		}
		if (whereClauseMap.containsKey("FROM_TIME"))
		{
			String fromTime = whereClauseMap.get("FROM_TIME");
			query += " AND " + FROM_TIME_COL + " = " + Integer.parseInt(fromTime);
		}
		if (whereClauseMap.containsKey("TO_TIME"))
		{
			String toTime = whereClauseMap.get("TO_TIME");
			query += " AND " + TO_TIME_COL + " = " + Integer.parseInt(toTime);
		}
		if (whereClauseMap.containsKey("SUBSCRIBER_WAV_FILE"))
		{
			String subscriberWavFile = whereClauseMap.get("SUBSCRIBER_WAV_FILE");
			query += " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subscriberWavFile);
		}
		if (whereClauseMap.containsKey("SEL_INTERVAL"))
		{
			String selInterval = whereClauseMap.get("SEL_INTERVAL");
			query += " AND " + SEL_INTERVAL_COL + getNullForWhere(selInterval);
		}
		
		query += " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "', '"
				+ STATE_ACTIVATION_PENDING + "', '" + STATE_ACTIVATED + "', '"
				+ STATE_ACTIVATION_ERROR + "', '"
				+ STATE_BASE_ACTIVATION_PENDING + "', '" + STATE_CHANGE + "', '"
				+ STATE_REQUEST_RENEWAL + "', '" + STATE_UN + "', '"
				+ STATE_GRACE + "', '" + STATE_SUSPENDED
				+ "', '" + STATE_SUSPENDED_INIT + "' )";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			updateCount = stmt.getUpdateCount();
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
		return(updateCount > 0);
	}
	
	static SubscriberStatus smGetSelectionRefID(Connection conn,
			String subscriberID, String deselectedBy,
			Map<String, String> whereClauseMap)
	{
		int updateCount = -1;
		Statement stmt = null;
		ResultSet rs = null;
		
		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		String endTimeStr = "SYSDATE";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			nextChargingDate = "TIMESTAMP('2037-12-31')";
			endTimeStr = "SYSDATE()";
		}
		String sel_status = STATE_DEACTIVATION_PENDING;

		String query = "SELECT " + INTERNAL_REF_ID_COL + " FROM " + TABLE_NAME;
		
		query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		if (whereClauseMap.containsKey("CALLER_ID"))
		{
			String callerID = whereClauseMap.get("CALLER_ID");
			query += " AND " + CALLER_ID_COL + getNullForWhere(callerID);
		}
		if (whereClauseMap.containsKey("STATUS"))
		{
			String status = whereClauseMap.get("STATUS");
			query += " AND " + STATUS_COL + " = " + status;
		}
		if (whereClauseMap.containsKey("FROM_TIME"))
		{
			String fromTime = whereClauseMap.get("FROM_TIME");
			query += " AND " + FROM_TIME_COL + " = " + Integer.parseInt(fromTime);
		}
		if (whereClauseMap.containsKey("TO_TIME"))
		{
			String toTime = whereClauseMap.get("TO_TIME");
			query += " AND " + TO_TIME_COL + " = " + Integer.parseInt(toTime);
		}
		if (whereClauseMap.containsKey("SUBSCRIBER_WAV_FILE"))
		{
			String subscriberWavFile = whereClauseMap.get("SUBSCRIBER_WAV_FILE");
			query += " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(subscriberWavFile);
		}
		if (whereClauseMap.containsKey("SEL_INTERVAL"))
		{
			String selInterval = whereClauseMap.get("SEL_INTERVAL");
			query += " AND " + SEL_INTERVAL_COL + getNullForWhere(selInterval);
		}
		
		query += " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "', '"
				+ STATE_ACTIVATION_PENDING + "', '" + STATE_ACTIVATED + "', '"
				+ STATE_ACTIVATION_ERROR + "', '"
				+ STATE_BASE_ACTIVATION_PENDING + "', '" + STATE_CHANGE + "', '"
				+ STATE_REQUEST_RENEWAL + "', '" + STATE_UN + "', '"
				+ STATE_GRACE + "', '" + STATE_SUSPENDED
				+ "', '" + STATE_SUSPENDED_INIT + "' )";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if(rs.next()){
				return getSubscriberStatusFromRS(rs);
			}			
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		return null;
	}

	static SubscriberStatus[] getSubscriberSelection(Connection conn, String subscriberID, String initClassType, String finalClassType, int rbtType)
	{
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
		int n = -1;
		Statement stmt = null;
		ResultSet rs = null;
		String sysDateStr = "SYSDATE";
		String startTimeStr =  "TO_DATE('20040101','yyyyMMdd')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr = "SYSDATE()";
			startTimeStr =  "TIMESTAMP('2004-01-01')";
		}
		
		String query = "SELECT * FROM " + TABLE_NAME + 
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CLASS_TYPE_COL + " = " + sqlString(initClassType) + " AND " + 
		SET_TIME_COL + " <= "+sysDateStr+" AND " + SEL_STATUS_COL + " = '" + STATE_ACTIVATED + "' "; 
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()){
				SubscriberStatus subStatus = getSubscriberStatusFromRS(rs);
				if(subStatus != null){
					subscriberStatusList.add(subStatus);
				}
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return subscriberStatusList.toArray(new SubscriberStatus[0]);
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		return subscriberStatusList.toArray(new SubscriberStatus[0]);
	}

	static boolean convertSelectionClassType(Connection conn, String subscriberID, String initClassType, String finalClassType, int rbtType, String extraInfo, String selInterval)
	{
		int n = -1;
		Statement stmt = null;

		String sysDateStr = "SYSDATE";
		String startTimeStr =  "TO_DATE('20040101','yyyyMMdd')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr = "SYSDATE()";
			startTimeStr =  "TIMESTAMP('2004-01-01')";
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " +
		START_TIME_COL + " = "+startTimeStr+", " + 
		NEXT_CHARGING_DATE_COL + " = NULL, " +
		CLASS_TYPE_COL + " = '" + finalClassType + "', " +
		SEL_STATUS_COL + " = '" + STATE_ACTIVATION_PENDING + "', "+
		OLD_CLASS_TYPE_COL + " = " + sqlString(initClassType) +
		EXTRA_INFO_COL + " = " + sqlString(extraInfo) +
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CLASS_TYPE_COL + " = " + sqlString(initClassType) + " AND " +
		SEL_INTERVAL_COL + " = " + sqlString(selInterval) + " AND " +
		SET_TIME_COL + " <= "+sysDateStr+" AND " + SEL_STATUS_COL + " = '" + STATE_ACTIVATED + "' "; 
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return (n != 0);
	}
	
	static boolean updateSubscriberSelection(Connection conn, String subscriberID, String initClassType, String finalClassType, String status, int rbtType, String extraInfo, String selInterval)
	{
		int n = -1;
		Statement stmt = null;

		String sysDateStr = "SYSDATE";
		String startTimeStr =  "TO_DATE('20040101','yyyyMMdd')";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr = "SYSDATE()";
			startTimeStr =  "TIMESTAMP('2004-01-01')";
		}
		
		String query = "UPDATE " + TABLE_NAME + " SET " +
		START_TIME_COL + " = "+startTimeStr+", " + 
		NEXT_CHARGING_DATE_COL + " = NULL, " +
		CLASS_TYPE_COL + " = '" + finalClassType + "', " +
		SEL_STATUS_COL + " = '" + status + "', "+
		OLD_CLASS_TYPE_COL + " = " + sqlString(initClassType) +
		EXTRA_INFO_COL + " = " + sqlString(extraInfo) +
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CLASS_TYPE_COL + " = " + sqlString(initClassType) + " AND " +
		SEL_INTERVAL_COL + " = " + sqlString(selInterval) + " AND " +
		SET_TIME_COL + " <= "+sysDateStr+" AND " + SEL_STATUS_COL + " = '" + STATE_ACTIVATED + "' "; 
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return (n != 0);
	}
	
	static boolean updateSubscriberSelectionInlineDaemonFlag(Connection conn, String subscriberID, String refId, Integer flag)
	{
		int n = -1;
		Statement stmt = null;

		String query = "UPDATE " + TABLE_NAME + " SET " +
		INLINE_DAEMON_FLAG_COL + " = "+flag+" WHERE " + SUBSCRIBER_ID_COL  + " = '" + subscriberID + "' AND " + 
		INTERNAL_REF_ID_COL + " = '" + refId + "'";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return (n != 0);
	}
	
	static boolean resetSubscriberSelectionInlineDaemonFlag(Connection conn)
	{
		int n = -1;
		Statement stmt = null;
		
		//IN is not using index in the query, so updating first 0 and then 1
//		String query = "UPDATE " + TABLE_NAME + " SET " +
//		INLINE_DAEMON_FLAG_COL + " = "+null+" WHERE " + INLINE_DAEMON_FLAG_COL  + " IN(0, 1)"; 

		String query = "UPDATE " + TABLE_NAME + " SET " +
		INLINE_DAEMON_FLAG_COL + " = "+null+" WHERE " + INLINE_DAEMON_FLAG_COL  + " = 0";
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		
		query = "UPDATE " + TABLE_NAME + " SET " +
		INLINE_DAEMON_FLAG_COL + " = "+null+" WHERE " + INLINE_DAEMON_FLAG_COL  + " = 1";
		
		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return (n != 0);
	}
	
	//Added by Sreekar for Reliance ARBT implementation
	static boolean deleteSubscriberSlectionsForWavFIle(Connection conn, String subscriberId, String wavFile) {
		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId)
				+ " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(wavFile);

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		int count = -1;
		
		try {
			stmt = conn.createStatement();
			count = stmt.executeUpdate(query);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatementAndRS(stmt, null);
		}
		return count > 0;
	}	

	public static SubscriberStatus[] convertSubscriberStatusListToArray(List<SubscriberStatus> subscriberStatusList) {
		if(subscriberStatusList.size() > 0) {
			return (SubscriberStatus[])subscriberStatusList.toArray(new SubscriberStatus[0]);
		} 
		return null;
	}
	
	static List<String> smGetDeactivateOldSelection(Connection conn, String subscriberID, String callerID, int status, String setDate, int fromTime, int toTime, int rbtType, String selInterval, String refID)
	{
		int n = -1;
		Statement stmt = null;
		ResultSet rs = null;
		List<String> wavFileName = new ArrayList<String>();

		try
		{
		String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		String sysDateStr = "SYSDATE";
		String setTimeStr = "TO_DATE('" + setDate + "','yyyyMMddhh24miss') ";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sysDateStr = "SYSDATE()";
			nextChargingDate = "TIMESTAMP('2037-12-31')";
			if (setDate != null)
			{	
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date tempDate = sdf.parse(setDate);
				String tempDateStr = sdf2.format(tempDate);
				setTimeStr = "TIMESTAMP('" + tempDateStr + "') ";
			}
		}
		
		String query = "SELECT  " + SUBSCRIBER_WAV_FILE_COL + " FROM " + TABLE_NAME +
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CALLER_ID_COL + getNullForWhere(callerID); 
		if(setDate != null)
			query = query + " AND " + SET_TIME_COL + " <= "+setTimeStr ;
		
		query = query + " AND " + 
		SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING + "','" + 
		STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" + STATE_BASE_ACTIVATION_PENDING + "','" + 
		STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" + STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"') AND " +
		FROM_TIME_COL + " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime + " AND " + 
		STATUS_COL + " = " + status;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		if(selInterval != null)
			query += " AND " + SEL_INTERVAL_COL + " = " + sqlString(selInterval);
    
		if(refID != null)
			query += " AND (" + INTERNAL_REF_ID_COL + " IS NULL OR " + INTERNAL_REF_ID_COL + " != " + sqlString(refID) + ")";

		logger.info("Executing query: " + query);

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			
			while(rs.next())
				wavFileName.add(rs.getString(SUBSCRIBER_WAV_FILE_COL));
		}
		catch(Exception se)
		{
			logger.error("", se);
			return wavFileName;
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		return wavFileName;
	}
	
	static List<String> smGetAllDeactivateOldSelection(Connection conn, String subscriberID, String callerID, int status, String setDate, int fromTime, int toTime, int rbtType, String selInterval, String refID)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		List<String> rbtwavFilesList = new ArrayList<String>();

		try
		{
		String setTimeStr = "TO_DATE('" + setDate + "','yyyyMMddhh24miss') ";
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			if (setDate != null)
			{	
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date tempDate = sdf.parse(setDate);
				String tempDateStr = sdf2.format(tempDate);
				setTimeStr = "TIMESTAMP('" + tempDateStr + "') ";
			}
		}
		
		String query = "SELECT " + SUBSCRIBER_WAV_FILE_COL + " FROM " + TABLE_NAME +
		" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + 
		CALLER_ID_COL + getNullForWhere(callerID); 
		if(setDate != null)
			query = query + " AND " + SET_TIME_COL + " <= "+setTimeStr ;
		
		query = query + " AND " + 
		SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" + STATE_ACTIVATION_PENDING + "','" + 
		STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" + STATE_BASE_ACTIVATION_PENDING + "','" + 
		STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" + STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"') AND " +
		FROM_TIME_COL + " = " + fromTime + " AND " + TO_TIME_COL + " = " + toTime + " AND " + 
		STATUS_COL + " = " + status;
		if(m_rrbtOn) 
            query += " AND "+ SEL_TYPE_COL + " = " + rbtType; 

		if(selInterval != null)
			query += " AND " + SEL_INTERVAL_COL + " = " + sqlString(selInterval);
    
		if(refID != null)
			query += " AND (" + INTERNAL_REF_ID_COL + " IS NULL OR " + INTERNAL_REF_ID_COL + " != " + sqlString(refID) + ")";

		
		String giftChargeClasses = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON, "OPTIN_GIFT_CHARGE_CLASS", null);
		if(giftChargeClasses != null) {
			String classTypes = giftChargeClasses.replace(",", ",'");
			query += "AND " + CLASS_TYPE_COL + " NOT IN ('" + classTypes + "')";
		}
		
		logger.info("Executing query: " + query);

			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			
			while(rs.next()) {
				rbtwavFilesList.add(rs.getString(SUBSCRIBER_WAV_FILE_COL));
			}
		}
		catch(Exception se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		return rbtwavFilesList;
	}

	public static boolean updateRetryCountAndTime(Connection conn, String subscriberID, String refID, String retryCount, Date retryTime)
	{
		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			query = "UPDATE " + TABLE_NAME + " SET " + RETRY_COUNT_COL + " = "
					+ sqlString(retryCount) + " , " + NEXT_RETRY_TIME_COL
					+ " = " + sqlTime(retryTime) + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'"
					+ " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(refID);
		}
		else {
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
	
	//RBT-11752
	 static ArrayList<SubscriberStatus> getSelectionBySubsIdAndCatIdAndCallerIdAndRefId(Connection conn,String subscriberID,int categoryID,String callerId,String refId){
			
			ArrayList<SubscriberStatus> selectionsList= new ArrayList<SubscriberStatus>();
			ArrayList<SubscriberStatus> selectionsListByRefId = new ArrayList<SubscriberStatus>();
			Statement stmt = null;
			ResultSet results = null;
			String provRefIdString="PROV_REF_ID=\""+refId+"\"";
			StringBuilder query = new StringBuilder();
			query.append("SELECT * FROM ");
			query.append(TABLE_NAME);
			query.append(" WHERE ");
			query.append(SUBSCRIBER_ID_COL);
			query.append(" = ");
			query.append(sqlString(subscriberID));
			query.append(" AND ");
			query.append(CATEGORY_ID_COL);
			query.append(" = ");
			query.append(categoryID);
			query.append(" AND ");
			query.append(CALLER_ID_COL);
			
			if(callerId!=null && !callerId.equals(""))	{
				query.append(" = ");
				query.append(sqlString(callerId));
			}	else {
				query.append(" IS NULL ");
			}	
			query.append(" AND ");
			query.append(END_TIME_COL);
			query.append(" > ");
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				query.append("SYSDATE");
			}else
			{
				query.append("SYSDATE()");
			}
			
			logger.info("Executing query: " + query);
			try
			{
				stmt = conn.createStatement();
				results = stmt.executeQuery(query.toString());
				while(results.next()) {					
					selectionsList.add(getSubscriberStatusFromRS(results));
				}
				
				Iterator<SubscriberStatus> selListiterator = selectionsList.iterator();
				while(selListiterator.hasNext()) {
					SubscriberStatus subStatus = selListiterator.next();
					if(subStatus.extraInfo().contains(provRefIdString)) {
						selectionsListByRefId.add(subStatus);
					}
				}
			}
			catch(SQLException e)
			{
				logger.error("", e);
			}
			finally
			{
				closeStatementAndRS(stmt, results);
			}
			return selectionsListByRefId;
	    }
	 
	 //RBT-12419
	 public static  SubscriberStatus getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(Connection conn,
				String subscriberID, Map<String, String> whereClauseMap) {
		 
		 Statement stmt = null;
			ResultSet rs = null;
			String query = "SELECT * FROM " + TABLE_NAME;
			
			query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'" +" AND "+ SEL_STATUS_COL +" IN ('A','W','N','B')";
			if (whereClauseMap.containsKey("CATEGORY_ID"))
			{
				String categoryID = whereClauseMap.get("CATEGORY_ID");
				query += " AND " + CATEGORY_ID_COL + " = "+categoryID;
			}
			if (whereClauseMap.containsKey("SUBSCRIBER_WAV_FILE"))
			{
				String waveFileName = whereClauseMap.get("SUBSCRIBER_WAV_FILE");
				query += " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + "'" +  waveFileName + "'" ;
			}
			
			//Added for caller id check
			if (whereClauseMap.containsKey("CALLER_ID"))
			{
				String callerId = whereClauseMap.get("CALLER_ID");
				query +=  " AND " + CALLER_ID_COL + getNullForWhere(callerId) ;
			}
			
			if(whereClauseMap.containsKey("FROM_TIME")) {
				String fromTime = whereClauseMap.get("FROM_TIME");
				query +=  " AND " + FROM_TIME_COL + getNullForWhere(fromTime) ;
			}
			
			if(whereClauseMap.containsKey("TO_TIME")) {
				String toTime = whereClauseMap.get("TO_TIME");
				query +=  " AND " + TO_TIME_COL + getNullForWhere(toTime) ;
			}
			
			if(whereClauseMap.containsKey("STATUS")) {
				String status = whereClauseMap.get("STATUS");
				query +=  " AND " + STATUS_COL + getNullForWhere(status) ;
			}
			
			if(whereClauseMap.containsKey("SEL_INTERVAL")){
				String status = whereClauseMap.get("SEL_INTERVAL");
				query +=  " AND " + SEL_INTERVAL_COL + getNullForWhere(status) ;
				
			}

			query += " AND " + END_TIME_COL + " > ";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				query += " SYSDATE ";
			}else
			{
				query +=" SYSDATE()";
			}
			
			logger.info("Executing query: " + query);
			try
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);
				if(rs.next()){
					return getSubscriberStatusFromRS(rs);
				}	
				
			}
			catch(SQLException se)
			{
				logger.error("", se);
				return null;
			}
			finally
			{
				closeStatementAndRS(stmt, rs);
			}
		 return null;
	 }
	 
	 
	  static SubscriberStatus[] getActiveNormalSelByCallerIdAndByStatus(Connection conn,
				String subscriberID, String callerID, int status)
		{
			String query = null;
			Statement stmt = null;
			ResultSet results = null;

			SubscriberStatus subscriberStatus = null;
			List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();
			
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ " AND " + CALLER_ID_COL + getNullForWhere(callerID) 
					+ " AND " + STATUS_COL + " = " +status 
					+ " AND " + CATEGORY_TYPE_COL +" IN(" + sqlString(String.valueOf(DTMF_CATEGORY)) +","+ sqlString(String.valueOf(SONGS))+ ")" 
					+ " AND " + SET_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()";

			logger.info("Executing query: " + query);
			try
			{
				stmt = conn.createStatement();
				results = stmt.executeQuery(query);
				while(results.next())
				{
					subscriberStatus = getSubscriberStatusFromRS(results);
					subscriberStatusList.add(subscriberStatus);
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
			logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS based on caller-catId-fromTime-toTime. Total rows: " + subscriberStatusList.size());
			return convertSubscriberStatusListToArray(subscriberStatusList);
		}
	  
	  static boolean directDeactivateSubscriberRecordsByRefId(Connection conn, String subscriberID, String deSelectedBy , String refId, Character newLoopStatus) { 
		  int n = -1; 
		  Statement stmt = null; 

		  String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
		  String endTimeStr = "SYSDATE";
		  if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		  {
			  nextChargingDate = "TIMESTAMP('2037-12-31')";
			  endTimeStr = "SYSDATE()";
		  }

		  String sel_status = "X"; 
		  String query = "UPDATE " + TABLE_NAME + " SET " + 
				  END_TIME_COL + " = "+endTimeStr + ", " + 
				  NEXT_CHARGING_DATE_COL + " = " + nextChargingDate; 
		  query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy) + " "; 
		 
		  if (newLoopStatus != null) {
          	query = query + ", " + LOOP_STATUS_COL + " = '" + newLoopStatus + "' ";
          }
		  
		  query = query + "WHERE " + SUBSCRIBER_ID_COL  + " = " + sqlString(subscriberID) + " AND " + 
				  INTERNAL_REF_ID_COL + " = " +sqlString(refId); 

		  logger.info("Executing query: " + query);
		  RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		  try { 
			  stmt = conn.createStatement(); 
			  stmt.executeUpdate(query); 
			  n = stmt.getUpdateCount(); 
		  } catch(SQLException se) { 
			  logger.error("", se); 
			  return false; 
		  } finally { 
			  closeStatementAndRS(stmt, null);
		  } 
		  return(n>=1);
	  }

	  public static boolean updateSelectionExtraInfoAndRefId(Connection conn,
			  String subscriberId, String newExtraInfo, String oldRefId,
			  String newRefId) {
		  Statement stmt = null;
		  int n = -1;
		  if (newExtraInfo == null && oldRefId == null) {
			  logger.info("newExtraInfo and oldRefId null");
			  return false;
		  }
		  String query = "UPDATE " + TABLE_NAME + " SET ";
		  if (newExtraInfo != null) {
			  query = query + EXTRA_INFO_COL + " = " + sqlString(newExtraInfo) + " ";
		  }
		  if (newRefId != null) {
			  query = query + INTERNAL_REF_ID_COL + " = " + sqlString(newRefId) + " ";
		  }
				 
		  query = query + "WHERE " + SUBSCRIBER_ID_COL  + " = '" + subscriberId + "'" 
				  	+ " AND " + INTERNAL_REF_ID_COL + " = " + sqlString(oldRefId);

		  logger.info("Executing query: " + query);
		  RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		  try
		  {
			  stmt = conn.createStatement();
			  n = stmt.executeUpdate(query);
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
		  return (n>0);	
	  }

	// RBT-14301: Uninor MNP changes.
	static boolean updateCircleId(Connection conn, String subID,
			String circleId, String refId, String status) {
		String query = "UPDATE " + TABLE_NAME + " SET " + CIRCLE_ID_COL + " = "
				+ sqlString(circleId);
		query = query + " WHERE " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subID) + " ";
		if (refId != null) {
			query = query + " AND " + INTERNAL_REF_ID_COL + " = "
					+ sqlString(refId) + " ";
		}
		if (status != null) {
			query = query + " AND " + SEL_STATUS_COL + " = "
					+ sqlString(status);
		}
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return n > 0;
	}

	
	  static SubscriberStatus [] getPendingDefaultSubscriberSelections(Connection conn, String subID, String callerID, int status, String shuffleSetTime)
		{
			Statement stmt = null;
			ResultSet results = null;
			SubscriberStatus subscriberStatus = null;
			List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

			String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subID) 
					+ " AND " + CALLER_ID_COL + getNullForWhere(callerID)
					+ " AND " + STATUS_COL + " = "+ status
					+ " AND " + CATEGORY_TYPE_COL +" IN(" + sqlString(String.valueOf(DTMF_CATEGORY)) +","+ sqlString(String.valueOf(SONGS))+ ")" 
					+ " AND " + SET_TIME_COL + " <= TIMESTAMP('" + shuffleSetTime + "')"
					+" AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "', '"+ STATE_BASE_ACTIVATION_PENDING + "', '" 
					+ STATE_ACTIVATION_PENDING + "', '" + STATE_ACTIVATION_ERROR + "')";


			

			logger.info("Executing query: " + query);
			try
			{
				stmt = conn.createStatement();
				results = stmt.executeQuery(query);
				while(results.next())
				{
					subscriberStatus = getSubscriberStatusFromRS(results);
					subscriberStatusList.add(subscriberStatus);
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
			logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
			return convertSubscriberStatusListToArray(subscriberStatusList);
		}

	@Override
	public String udpId() {
		return udpId;
	}

	static boolean smUpdateSelStatusOfXbiSelections(Connection conn, String subscriberID, String fStatus, String tStatus, String wavFileName)
		{
			String query = null;
			Statement stmt = null;

			if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
						+ sqlString(tStatus)+ " , " + START_TIME_COL + " = "
						+ "SYSDATE" + " , " + END_TIME_COL + " = " + "SYSDATE" + " WHERE " + SUBSCRIBER_ID_COL
						+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
						+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
						+ " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE"
						+ " AND " + SUBSCRIBER_WAV_FILE_COL + " = " +sqlString(wavFileName) ;
			else
				query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = "
						+ sqlString(tStatus) + " , " + START_TIME_COL + " = "
						+ "SYSDATE()" + " , " + END_TIME_COL + " = " + "SYSDATE()" + " WHERE " + SUBSCRIBER_ID_COL
						+ " = " + "'" + subscriberID + "' AND " + SEL_STATUS_COL
						+ " = " + sqlString(fStatus) + " AND " + SET_TIME_COL
						+ " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()"
						+ " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + sqlString(wavFileName);

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
	

	//RBT-16453
	 public static  List<SubscriberStatus> getSubscriberActiveSelections(Connection conn,
				String subscriberID, Map<String, String> whereClauseMap) {
		 
		 	Statement stmt = null;
			ResultSet rs = null;
			List<SubscriberStatus> subscriberStatusList = null;
			String query = "SELECT * FROM " + TABLE_NAME;
			
			query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'" +" AND "+ SEL_STATUS_COL +" IN ('A','W','N','B','G')";
			if (whereClauseMap != null && whereClauseMap.containsKey("CATEGORY_ID"))
			{
				String categoryID = whereClauseMap.get("CATEGORY_ID");
				query += " AND " + CATEGORY_ID_COL + " = "+categoryID;
			}
			if (whereClauseMap != null &&  whereClauseMap.containsKey("SUBSCRIBER_WAV_FILE"))
			{
				String waveFileName = whereClauseMap.get("SUBSCRIBER_WAV_FILE");
				query += " AND " + SUBSCRIBER_WAV_FILE_COL + " = " + "'" +  waveFileName + "'" ;
			}
			
			//Added for caller id check
			if (whereClauseMap != null &&  whereClauseMap.containsKey("CALLER_ID"))
			{
				String callerId = whereClauseMap.get("CALLER_ID");
				query +=  " AND " + CALLER_ID_COL + getNullForWhere(callerId) ;
			}
			
			//Added for udp id check
			if (whereClauseMap != null &&  whereClauseMap.containsKey("UDP_ID"))
			{
				String udpId = whereClauseMap.get("UDP_ID");
				query +=  " AND " + UDP_ID_COL + getNullForWhere(udpId) ;
			}

			query += " AND " + END_TIME_COL + " > ";
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			{
				query += " SYSDATE ";
			}else
			{
				query +=" SYSDATE()";
			}
			
			query += " ORDER BY "+ SET_TIME_COL +" DESC";
			
			logger.info("Executing query: " + query);
			try
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);
				
				if(rs != null) {
					subscriberStatusList = new ArrayList<SubscriberStatus>();
					while(rs.next()) {
						subscriberStatusList.add(getSubscriberStatusFromRS(rs));
					}
				}				
			}
			catch(SQLException se)
			{
				logger.error("", se);
				return null;
			}
			finally
			{
				closeStatementAndRS(stmt, rs);
			}
		 return subscriberStatusList;
	 }
	
	     //Added for CDR logging 
		 public static  SubscriberStatus getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(Connection conn,
					String subscriberID, String id, String key) {
			 
			    Statement stmt = null;
				ResultSet rs = null;
				String query = "SELECT * FROM " + TABLE_NAME;
				
				query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'" +" AND "+ SEL_STATUS_COL +" ='B' AND ";
				if (key.equals("CATEGORY_ID"))
				{
					query += CATEGORY_ID_COL + " = "+ "'" +  id + "'";
				}
				else if (key.equals("SUBSCRIBER_WAV_FILE"))
				{
					query +=  SUBSCRIBER_WAV_FILE_COL + " = " + "'" +  id + "'" ;
				}
				else if (key.equals("UDP_ID"))
				{
					query += UDP_ID_COL + " = '" +  id + "'" ;
				}
				else if (key.equals("REF_ID"))
				{
					query += INTERNAL_REF_ID_COL + " = '" +  id + "'" ;
				}
				query += " AND " + END_TIME_COL + " > " + "SYSDATE()";
				
				logger.info("Executing query: " + query);
				try
				{
					stmt = conn.createStatement();
					rs = stmt.executeQuery(query);
					if(rs.next()){
						return getSubscriberStatusFromRS(rs);
					}	
					
				}
				catch(SQLException se)
				{
					logger.error("", se);
					return null;
				}
				finally
				{
					closeStatementAndRS(stmt, rs);
				}
			 return null;
		 }
		 
		 public static List<SubscriberStatus> getDistinctActiveSelections(Connection conn, Map<String, String> whereClauseMap) {
			 	String subscriberID = whereClauseMap.get(WebServiceConstants.param_subscriberID);
		
			 	if(subscriberID == null)
			 		return null;
			 	
			 	String udpId = null;
			 	Statement stmt = null;
				ResultSet rs = null;
				List<SubscriberStatus> subscriberStatusList = null;
				String query = "SELECT DISTINCT "+SUBSCRIBER_ID_COL+","+CALLER_ID_COL+
						","+FROM_TIME_COL+","+TO_TIME_COL+","+STATUS_COL+" FROM " + TABLE_NAME;
				
				query += " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'" +" AND "+ SEL_STATUS_COL +" IN ('A','W','N','B')";
				if (whereClauseMap.containsKey(WebServiceConstants.param_udpId))
				{
					udpId = whereClauseMap.get(WebServiceConstants.param_udpId);
					query += " AND " + UDP_ID_COL + " = "+udpId;
				}
				
				query += " AND " + END_TIME_COL + " > ";
				if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				{
					query += " SYSDATE ";
				}else
				{
					query +=" SYSDATE()";
				}
				
				logger.info("Executing query: " + query);
				try
				{
					stmt = conn.createStatement();
					rs = stmt.executeQuery(query);
					
					if(rs != null) {
						subscriberStatusList = new ArrayList<SubscriberStatus>();
						while(rs.next()) {
							String subId = rs.getString(SUBSCRIBER_ID_COL);
							String callerID = rs.getString(CALLER_ID_COL);
							String fromTime = rs.getString(FROM_TIME_COL);
							String toTime = rs.getString(TO_TIME_COL);
							String status = rs.getString(STATUS_COL);
							subscriberStatusList.add(new SubscriberStatusImpl(subId, callerID, -1, null, null, null, null, Integer.parseInt(status), null, null, null, null, null,
									Integer.parseInt(fromTime),  Integer.parseInt(toTime), null, null, null, -1, '\u0000', -1, null, null, null, null, udpId));
						}
					}				
				}
				catch(SQLException se)
				{
					logger.error("", se);
					return null;
				}
				finally
				{
					closeStatementAndRS(stmt, rs);
				}
			 return subscriberStatusList;
		 }

		public static boolean deactivateSubscriberRecordsByNotCategoryIdNotStatus(Connection conn, String subscriberID,
				int categoryId,int status, String deSelectedBy) {
			
			 
            int n = -1; 
            Statement stmt = null; 
            try{
            	stmt = conn.createStatement();
            	String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
            	String endTimeStr = "SYSDATE";
            	if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
            	{
            		nextChargingDate = "TIMESTAMP('2037-12-31')";
            		endTimeStr = "SYSDATE()";
            	}

            	String sel_status = "D"; 
            	SubscriberStatus subscriberStatus[] = getActiveSelectionsByNotcategoryandNotstatus(conn, subscriberID,categoryId,status); 
            	
            	if(subscriberStatus!=null && subscriberStatus.length>0)
            	for(int i=0;i<subscriberStatus.length;i++){
            		if(subscriberStatus != null) 
            		{ 
            			if(subscriberStatus[i].nextChargingDate() != null)
            			{
            				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            				String nextChargeDate = dateFormat.format(subscriberStatus[i].nextChargingDate());
            				if(nextChargeDate != null && nextChargeDate.equalsIgnoreCase("20371130"))
            				{
            					nextChargingDate = sqlTime(subscriberStatus[i].nextChargingDate());
            					if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
            						nextChargingDate = mySQLDateTime(subscriberStatus[i].nextChargingDate());
            				}
            			}
            			char oldLoopStatus = subscriberStatus[i].loopStatus();
            			char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
            	    	if(oldLoopStatus == LOOP_STATUS_EXPIRED)
            	    		newLoopStatus = oldLoopStatus;
            	    	else if(oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT || oldLoopStatus == LOOP_STATUS_LOOP_INIT) 
            	            newLoopStatus = LOOP_STATUS_EXPIRED; 

            		String query = "UPDATE " + TABLE_NAME + " SET " + 
            		END_TIME_COL + " = "+endTimeStr + ", " + NEXT_CHARGING_DATE_COL + " = " + nextChargingDate + ", "+ LOOP_STATUS_COL + " = '" + newLoopStatus +"' " ; 
            		query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy); 
            		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' "  ;
            		
            		query = query + " AND " + CATEGORY_ID_COL + " != " + categoryId;
            		query = query + " AND " + STATUS_COL + " != " + status;
            		
            		query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
            		+ STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
            		+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" 
            		+ STATE_UN + "','" + STATE_ACTIVATION_GRACE + "','" + STATE_SUSPENDED_INIT+ "', '"+STATE_SUSPENDED+"')";

            		logger.info("Executing query: " + query);
            		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
            		stmt.executeUpdate(query); 
            		n = stmt.getUpdateCount(); 
            		}
            	}
            }
            catch(SQLException e){
            	logger.error("",e);
            }
            finally{
            	closeStatementAndRS(stmt, null);
            }
            return(n>=1);
    
		}
		
		
		
		

/*
		public static boolean deactivateSubscriberCutRbtRecords(Connection conn, String subscriberID,
				 String deSelectedBy) {
				
		 
        int n = -1; 
        Statement stmt = null; 
        try{
        	stmt = conn.createStatement();
        	String sel_status = "D"; 
       

        		String query = "UPDATE " + TABLE_NAME + " SET " ;
        	 
        		query = query  + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , "+ DESELECTED_BY_COL + " = " + sqlString(deSelectedBy); 
        		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' "  ;
        		query = query + " AND " + SUBSCRIBER_WAV_FILE_COL + " LIKE '%_cut_%' ";
        		query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
        		+ STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
        		+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" 
        		+ STATE_UN + "','" + STATE_ACTIVATION_GRACE + "','" + STATE_SUSPENDED_INIT+ "', '"+STATE_SUSPENDED+"')";

        		logger.info("Executing query: " + query);
        		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
        		stmt.executeUpdate(query); 
        		n = stmt.getUpdateCount(); 
        		}
        	
        
        catch(SQLException e){
        	logger.error("",e);
        }
        finally{
        	closeStatementAndRS(stmt, null);
        }
        return(n>=1);

}*/

		 
		 //vikrant-om
		 
		 public static boolean suspendSubscriberRecordsByNotCategoryIdNotStatus(Connection conn, String subscriberID,
					int categoryId,int status) {
				
				 
	            int n = -1; 
	            Statement stmt = null; 
	            try{
	            	stmt = conn.createStatement();
	            	String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
	            	String endTimeStr = "SYSDATE";
	            	if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
	            	{
	            		nextChargingDate = "TIMESTAMP('2037-12-31')";
	            		endTimeStr = "SYSDATE()";
	            	}

	            	String sel_status = "z"; 
	            	SubscriberStatus subscriberStatus[] = getActiveSelectionsByNotcategoryandNotstatus(conn, subscriberID,categoryId,status); 
	            	
	            	if(subscriberStatus!=null && subscriberStatus.length>0)
	            	for(int i=0;i<subscriberStatus.length;i++){
	            		if(subscriberStatus != null) 
	            		{ 
	            			if(subscriberStatus[i].nextChargingDate() != null)
	            			{
	            				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	            				String nextChargeDate = dateFormat.format(subscriberStatus[i].nextChargingDate());
	            				if(nextChargeDate != null && nextChargeDate.equalsIgnoreCase("20371130"))
	            				{
	            					nextChargingDate = sqlTime(subscriberStatus[i].nextChargingDate());
	            					if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
	            						nextChargingDate = mySQLDateTime(subscriberStatus[i].nextChargingDate());
	            				}
	            			}
	            			char oldLoopStatus = subscriberStatus[i].loopStatus();
	            			char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
	            	    	if(oldLoopStatus == LOOP_STATUS_EXPIRED)
	            	    		newLoopStatus = oldLoopStatus;
	            	    	else if(oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT || oldLoopStatus == LOOP_STATUS_LOOP_INIT) 
	            	            newLoopStatus = LOOP_STATUS_EXPIRED; 

	            		String query = "UPDATE " + TABLE_NAME + " SET " + 
	            		 LOOP_STATUS_COL + " = '" + newLoopStatus +"' " ; 
	            		query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status); 
	            		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' "  ;
	            		
	            		query = query + " AND " + CATEGORY_ID_COL + " != " + categoryId;
	            		query = query + " AND " + STATUS_COL + " != " + status;
	            		query = query + " AND " + SEL_STATUS_COL + " = '" + STATE_ACTIVATED+"'";
	            		
	            	/*	query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
	            		+ STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
	            		+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" 
	            		+ STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"')";
*/
	            		logger.info("Executing query: " + query);
	            		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
	            		stmt.executeUpdate(query); 
	            		n = stmt.getUpdateCount(); 
	            		}
	            	}
	            }
	            catch(SQLException e){
	            	logger.error("",e);
	            }
	            finally{
	            	closeStatementAndRS(stmt, null);
	            }
	            return(n>=1);
	    
			}
		 
		 
		 public static boolean activateSubscriberSuspendedRecordsByNotCategoryIdNotStatus(Connection conn, String subscriberID,
					int categoryId,int status) {
				
				 
	            int n = -1; 
	            Statement stmt = null; 
	            try{
	            	stmt = conn.createStatement();
	            	String nextChargingDate = "TO_DATE('20371231','yyyyMMdd')";
	            	String endTimeStr = "SYSDATE";
	            	if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
	            	{
	            		nextChargingDate = "TIMESTAMP('2037-12-31')";
	            		endTimeStr = "SYSDATE()";
	            	}

	            	String sel_status = "B"; 
	            	SubscriberStatus subscriberStatus[] = getActiveSelectionsByNotcategoryandNotstatus(conn, subscriberID,categoryId,status); 
	            	
	            	if(subscriberStatus!=null && subscriberStatus.length>0)
	            	for(int i=0;i<subscriberStatus.length;i++){
	            		if(subscriberStatus != null) 
	            		{ 
	            			if(subscriberStatus[i].nextChargingDate() != null)
	            			{
	            				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	            				String nextChargeDate = dateFormat.format(subscriberStatus[i].nextChargingDate());
	            				if(nextChargeDate != null && nextChargeDate.equalsIgnoreCase("20371130"))
	            				{
	            					nextChargingDate = sqlTime(subscriberStatus[i].nextChargingDate());
	            					if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
	            						nextChargingDate = mySQLDateTime(subscriberStatus[i].nextChargingDate());
	            				}
	            			}
	            			char oldLoopStatus = subscriberStatus[i].loopStatus();
	            			char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
	            	    	if(oldLoopStatus == LOOP_STATUS_EXPIRED)
	            	    		newLoopStatus = oldLoopStatus;
	            	    	else if(oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT || oldLoopStatus == LOOP_STATUS_LOOP_INIT) 
	            	            newLoopStatus = LOOP_STATUS_EXPIRED; 

	            		String query = "UPDATE " + TABLE_NAME + " SET " + 
	            		 LOOP_STATUS_COL + " = '" + newLoopStatus +"' " ; 
	            		query = query + "," + SEL_STATUS_COL + " = " + sqlString(sel_status); 
	            		query = query + " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' "  ;
	            		
	            		query = query + " AND " + CATEGORY_ID_COL + " != " + categoryId;
	            		query = query + " AND " + STATUS_COL + " != " + status;
	            		query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_SUSPENDED_INIT + "', '"+STATE_SUSPENDED+"')";
	            		
	            	/*	query = query + " AND " + SEL_STATUS_COL + " IN ('" + STATE_TO_BE_ACTIVATED + "' ,'" 
	            		+ STATE_ACTIVATION_PENDING + "','" + STATE_ACTIVATED + "' ,'" + STATE_ACTIVATION_ERROR + "','" 
	            		+ STATE_BASE_ACTIVATION_PENDING + "','" + STATE_CHANGE + "','" + STATE_REQUEST_RENEWAL + "','" 
	            		+ STATE_UN + "','" + STATE_ACTIVATION_GRACE + "', '"+STATE_SUSPENDED+"')";
*/
	            		logger.info("Executing query: " + query);
	            		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
	            		stmt.executeUpdate(query); 
	            		n = stmt.getUpdateCount(); 
	            		}
	            	}
	            }
	            catch(SQLException e){
	            	logger.error("",e);
	            }
	            finally{
	            	closeStatementAndRS(stmt, null);
	            }
	            return(n>=1);
	    
			}
		 
		 
		
		
		private static SubscriberStatus[] getActiveSelectionsByNotcategoryandNotstatus(Connection conn, String subscriberID,
				int categoryId,int status) {

			Statement stmt = null;
			ResultSet results = null;

			List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

			String sysdate = SAPDB_SYSDATE;
			if (m_databaseType.equals(DB_MYSQL))
				sysdate = MYSQL_SYSDATE;

			String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND "
					+ END_TIME_COL + " > " + sysdate + " AND " + CATEGORY_ID_COL
					+ " != " + categoryId + " AND " + STATUS_COL
					+ " != " + status;

			logger.info("Executing query: " + query);
			try
			{
				stmt = conn.createStatement();
				results = stmt.executeQuery(query);
				while(results.next())
				{
					subscriberStatusList.add(getSubscriberStatusFromRS(results));
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
			logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: " + subscriberStatusList.size());
			return convertSubscriberStatusListToArray(subscriberStatusList);
		
		}
		 
		 
 static SubscriberStatus [] getAllSubscriberSelectionRecordsBasedOnSelStatus(Connection conn , String selStatus , String subID){
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		SubscriberStatus subscriberStatus = null;
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
					 + END_TIME_COL
					+ " > SYSDATE AND " + SEL_STATUS_COL + "= "
					+ sqlString(selStatus);
		} else {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND "
					+ END_TIME_COL
					+ " > SYSDATE() AND " + SEL_STATUS_COL + "= "
					+ sqlString(selStatus);
		}

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				subscriberStatus = getSubscriberStatusFromRS(results);
				subscriberStatusList.add(subscriberStatus);
			}
		} catch (SQLException se) {
			logger.error("", se);
			return null;
		} finally {
			closeStatementAndRS(stmt, results);
		}
		logger.info("Retrieved records from RBT_SUBSCRIBER_SELECTIONS successfully. Total rows: "
				+ subscriberStatusList.size());
		return convertSubscriberStatusListToArray(subscriberStatusList);
	} 
 
 
 
	static boolean deactivateSettingDownloadDeactBasedOnCategory(Connection conn, String subscriberID, String subscriberWavFile,
			String categoryId, String categoryType) {
		int n = -1;
		Statement stmt = null;
		String sysdateStr = "SYSDATE";
		String nextChargingDate = "TO_DATE('20351231','yyyyMMdd')";
		if (!m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
			sysdateStr = "SYSDATE()";
			nextChargingDate = "TIMESTAMP('2035-12-31')";
		}

		String sel_status = "D";
		String query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL + " = " + sysdateStr + ", " + NEXT_CHARGING_DATE_COL
				+ " = " + nextChargingDate + "," + SEL_STATUS_COL + " = " + sqlString(sel_status) + " , " + DESELECTED_BY_COL
				+ " = 'SM'  , " + LOOP_STATUS_COL + " = " + "'" + LOOP_STATUS_EXPIRED_INIT + "' WHERE " + SUBSCRIBER_ID_COL
				+ " = " + "'" + subscriberID + "' AND SUBSCRIBER_WAV_FILE = " + sqlString(subscriberWavFile)
				+ " AND CATEGORY_TYPE = " + sqlString(categoryType) + " AND CATEGORY_ID = " + sqlString(categoryId) + " AND "
				+ SEL_STATUS_COL + " NOT IN ('" + STATE_DEACTIVATED + "')";

		logger.info("Executing query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		} catch (SQLException se) {
			logger.error("", se);
			return false;
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return (n == 1);
	}
}