package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.ResourceReader;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;

public class SubscriberAnnouncementsImpl extends RBTPrimitive implements SubscriberAnnouncements, iRBTConstant {
	private static Logger logger = Logger.getLogger(SubscriberAnnouncementsImpl.class);
	
	private static String SEQUENCE_ID_COL = "SEQUENCE_ID";
	private static String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static String CLIP_ID_COL = "CLIP_ID";
	private static String STATUS_COL = "STATUS";
	private static String START_TIME_COL = "START_TIME";
	private static String END_TIME_COL = "END_TIME";
	private static String FREQUENCY_COL = "FREQUENCY";
	private static String TIME_INTERVAL_COL = "TIME_INTERVAL";
	
	private static String TABLE_NAME = "RBT_SUBSCRIBER_ANNOUNCEMENTS";
	
	private long m_sequenceId;
	private String m_subscriberId;
	private int m_clipId;
	private int m_status;
	private Date m_activationDate;
	private Date m_deactivationDate;
	private String m_timeInterval;
	private String m_frequency;
	private static String m_databaseType = getDBSelectionString();
	
	public Date activationDate() {
		return m_activationDate;
	}

	public int clipId() {
		return m_clipId;
	}

	public Date deActivationDate() {
		return m_deactivationDate;
	}

	public String frequency() {
		return m_frequency;
	}

	public long sequenceId() {
		return m_sequenceId;
	}

	public int status() {
		return m_status;
	}

	public String subscriberId() {
		return m_subscriberId;
	}

	public String timeInterval() {
		return m_timeInterval;
	}

	protected SubscriberAnnouncementsImpl(long sequenceId, String subscriberId, int clipId, int status, Date activationDate, Date deactivationDate, String timeInterval, String frequency){
		m_sequenceId = sequenceId;
		m_subscriberId = subscriberId;
		m_clipId = clipId;
		m_status = status;
		m_activationDate = activationDate;
		m_deactivationDate = deactivationDate;
		m_frequency = frequency;
		m_timeInterval = timeInterval;
	}
	
	private static SubscriberAnnouncements getObjectFromResultSet(ResultSet rs) throws SQLException{
		long sequenceId = rs.getLong(SEQUENCE_ID_COL);
		String subscriberId = rs.getString(SUBSCRIBER_ID_COL);
		int clipId = rs.getInt(CLIP_ID_COL);
		Date actDate = rs.getTimestamp(START_TIME_COL);
		Date dactDate = rs.getTimestamp(END_TIME_COL);
		int status = rs.getInt(STATUS_COL);
		String frequency = rs.getString(FREQUENCY_COL);
		String timeInterval = rs.getString(TIME_INTERVAL_COL);
		return new SubscriberAnnouncementsImpl(sequenceId, subscriberId, clipId, status, actDate, dactDate, timeInterval, frequency);
	}
	
	static SubscriberAnnouncements[] getSubscriberAnnouncemets(Connection connection, String subscriberId)
	{
		List<SubscriberAnnouncements> announcementList = new ArrayList<SubscriberAnnouncements>();

		StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME
				+ " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId));

		logger.info("Query " + queryBuilder.toString());
		
		Statement statement = null;
		ResultSet resultSet = null;
		try
		{
			statement = connection.createStatement();
			resultSet = statement.executeQuery(queryBuilder.toString());
			while (resultSet.next())
			{
				announcementList.add(getObjectFromResultSet(resultSet));
			}
		}
		catch (SQLException sqle)
		{
			logger.error("", sqle);
			return null;
		}
		finally
		{
			try
			{
				if (resultSet != null)
					resultSet.close();
				if (statement != null)
					statement.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}

		return announcementList.toArray(new SubscriberAnnouncements[0]);
	}
	
	static SubscriberAnnouncements[] getActiveSubscriberAnnouncemets(Connection connection, String subscriberId){
		logger.info("RBT::inside getActiveSubscriberAnnouncemets");
		List<SubscriberAnnouncements> announcementList = new ArrayList<SubscriberAnnouncements>();
		StringBuilder queryBuilder = null;		
		Statement stmt = null;
		ResultSet rs = null;
		
//		queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + "=" + sqlString(subscriberId) + " AND " + STATUS_COL + " = " + ANNOUNCEMENT_ACTIVE);
		queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + "=" + sqlString(subscriberId));
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			queryBuilder.append(" AND " + START_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE" );
		else
			queryBuilder.append(" AND " + START_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()" );
		
		queryBuilder.append(" ORDER BY START_TIME");
		logger.info("RBT::query " + queryBuilder.toString());
		try{
			logger.info("RBT::inside try block");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(queryBuilder.toString());
			while(rs.next()){
				announcementList.add(getObjectFromResultSet(rs));
			}			
		}catch(SQLException sqle){
			logger.error("", sqle);
			return null;

		}finally{
			try
			{
				if(null != rs){
					rs.close();
				}
				if(null != stmt){
					stmt.close();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}			
		}
		
		return announcementList.toArray(new SubscriberAnnouncements[0]);

	}
	
	static SubscriberAnnouncements[] getActiveSubscriberAnnouncemetsForCallback(Connection connection, String subscriberId){
		logger.info("RBT::inside getActiveSubscriberAnnouncemetsForCallback");
		List<SubscriberAnnouncements> announcementList = new ArrayList<SubscriberAnnouncements>();
		StringBuilder queryBuilder = null;		
		Statement stmt = null;
		ResultSet rs = null;
		
//		queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + "=" + sqlString(subscriberId) + " AND " + STATUS_COL + " = " + ANNOUNCEMENT_ACTIVE);
		queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + "=" + sqlString(subscriberId));
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			queryBuilder.append(" AND " + START_TIME_COL + " <= SYSDATE " );
		else
			queryBuilder.append(" AND " + START_TIME_COL + " <= SYSDATE() " );
		
		queryBuilder.append(" ORDER BY START_TIME");
		logger.info("RBT::query " + queryBuilder.toString());
		try{
			logger.info("RBT::inside try block");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(queryBuilder.toString());
			while(rs.next()){
				announcementList.add(getObjectFromResultSet(rs));
			}			
		}catch(SQLException sqle){
			logger.error("", sqle);
			return null;

		}finally{
			try
			{
				if(null != rs){
					rs.close();
				}
				if(null != stmt){
					stmt.close();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}			
		}
		
		return announcementList.toArray(new SubscriberAnnouncements[0]);

	}

	
	static SubscriberAnnouncements getAnnouncementsRecord(Connection connection, String subscriberId, int clipId){
		logger.info("RBT::inside getAnnouncementsRecord");
		StringBuilder queryBuilder = null;		
		Statement stmt = null;
		ResultSet rs = null;
		SubscriberAnnouncements sbscriberAnnouncements = null;
		queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + "=" + sqlString(subscriberId) );
		queryBuilder.append(" AND " + CLIP_ID_COL + "=" + clipId);
		logger.info("RBT::query " + queryBuilder.toString());
		try{
			logger.info("RBT::inside try block");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(queryBuilder.toString());
			if(rs.next()){
				sbscriberAnnouncements = getObjectFromResultSet(rs);
				
			}			
		}catch(SQLException sqle){
			logger.error("", sqle);
			return null;

		}finally{
			try
			{
				if(null != rs){
					rs.close();
				}
				if(null != stmt){
					stmt.close();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}			
		}
		
		return sbscriberAnnouncements;

	}
	
	
	public static List<SubscriberAnnouncements> getExpiredAnnouncementsRecords(Connection connection,String fetchSize){
		logger.info("RBT::inside getExpiredAnnouncementsRecords");
		List<SubscriberAnnouncements> announcementList = new ArrayList<SubscriberAnnouncements>();
		String query = null;		
		Statement stmt = null;
		ResultSet rs = null;
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_TIME_COL + " < SYSDATE AND " + STATUS_COL + "=" + ANNOUNCEMENT_ACTIVE + " AND ROWNUM <="+fetchSize;
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_TIME_COL + " < SYSDATE() AND " + STATUS_COL + "=" + ANNOUNCEMENT_ACTIVE + " LIMIT "+fetchSize;;

		logger.info("RBT::query " + query);
		try{
			logger.info("RBT::inside try block");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()){
				announcementList.add(getObjectFromResultSet(rs));
			}			
		}catch(SQLException sqle){
			logger.error("", sqle);
			return null;

		}finally{
			try
			{
				if(null != rs){
					rs.close();
				}
				if(null != stmt){
					stmt.close();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}			
		}
		
		return announcementList;
	}
	static List<SubscriberAnnouncements> getAnnouncementsRecords(Connection connection, int status, long sequenceId){
		logger.info("RBT::inside getAnnouncementsRecords");
		List<SubscriberAnnouncements> announcementList = new ArrayList<SubscriberAnnouncements>();
		StringBuilder queryBuilder = null;		
		Statement stmt = null;
		ResultSet rs = null;
		
		queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + "=" + status );
		if(sequenceId != -1){
			queryBuilder.append(" AND " + SEQUENCE_ID_COL + " >=" + sequenceId);
		}
		logger.info("RBT::query " + queryBuilder.toString());
		try{
			logger.info("RBT::inside try block");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(queryBuilder.toString());
			while(rs.next()){
				announcementList.add(getObjectFromResultSet(rs));
			}			
		}catch(SQLException sqle){
			logger.error("", sqle);
			return null;

		}finally{
			try
			{
				if(null != rs){
					rs.close();
				}
				if(null != stmt){
					stmt.close();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}			
		}
		
		return announcementList;
	}
	
	static boolean update(Connection conn, SubscriberAnnouncements announcement){
		return update(conn, announcement, false);
	}
	
	static boolean update(Connection conn, SubscriberAnnouncements announcement, boolean isDeactiveRequest){
		logger.info("RBT::inside update");

		int id = -1;
		StringBuilder queryBuilder = null;
		Statement stmt = null;

		String actDate = null;
        String deactDate = null; 
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
	        if (announcement.activationDate() != null) 
	        	actDate = sqlTime(announcement.activationDate()); 
	        if(announcement.deActivationDate() != null) 
	        	deactDate = sqlTime(announcement.deActivationDate()); 
		}
        else
        {
	        if (announcement.activationDate() != null) 
	        	actDate = mySQLDateTime(announcement.activationDate()); 
	        if(announcement.deActivationDate() != null) 
	        	deactDate = mySQLDateTime(announcement.deActivationDate()); 
        }

		queryBuilder = new StringBuilder("UPDATE " + TABLE_NAME + " SET " );
		if(null != announcement.subscriberId()){
			queryBuilder.append(SUBSCRIBER_ID_COL  + " = " + sqlString(announcement.subscriberId()) + ", ");			
		}
		if(0 !=  announcement.clipId()){
			queryBuilder.append(CLIP_ID_COL + " = " + announcement.clipId() + ", ");
		}
		if(null != announcement.activationDate()){
			queryBuilder.append(START_TIME_COL + " = " + actDate + ", ");
		}
		if(null != announcement.deActivationDate()){
			queryBuilder.append(END_TIME_COL + " = " + deactDate + ", ");
		}
		if(null != announcement.frequency()){
			queryBuilder.append(FREQUENCY_COL + " = " + sqlString(announcement.frequency()) + ", ");
		}
		if(null !=  announcement.timeInterval()){
			queryBuilder.append(TIME_INTERVAL_COL + " = " + sqlString(announcement.timeInterval()) + ", ");
		}
		queryBuilder.append(STATUS_COL + " = " + announcement.status());
		
		queryBuilder.append(" WHERE " + SEQUENCE_ID_COL + " = " + announcement.sequenceId());
		
		if(isDeactiveRequest){
			queryBuilder.append(" AND " + STATUS_COL + " = " + ANNOUNCEMENT_ACTIVE);
		}

		logger.info("RBT::query " + queryBuilder.toString());

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			if (stmt.executeUpdate(queryBuilder.toString()) > 0)
				id = 0;
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return false;
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
		if (id == 0)
		{
			logger.info("RBT::updation to RBT_SUBSCRIBER_ANNOUNCEMENTS table successful");
			return true;
		}
		else
		{
			logger.info("RBT::updation to RBT_SUBSCRIBER_ANNOUNCEMENTS table failed");
			return false;
		}

	}

	/*
	 * Deactive subscriber only can active the announcement
	 * Update the all columns, which is not null
	 */
	static boolean updateToActiveAnnouncement(Connection conn, SubscriberAnnouncements announcement){
		logger.info("RBT::inside updateToActiveAnnouncement");

		int id = -1;
		StringBuilder queryBuilder = null;
		Statement stmt = null;

		String actDate = null;
        String deactDate = null; 
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
	        if (announcement.activationDate() != null) 
	        	actDate = sqlTime(announcement.activationDate()); 
	        if(announcement.deActivationDate() != null) 
	        	deactDate = sqlTime(announcement.deActivationDate()); 
		}
        else
        {
	        if (announcement.activationDate() != null) 
	        	actDate = mySQLDateTime(announcement.activationDate()); 
	        if(announcement.deActivationDate() != null) 
	        	deactDate = mySQLDateTime(announcement.deActivationDate()); 
        }

		queryBuilder = new StringBuilder("UPDATE " + TABLE_NAME + " SET " );
		if(null != announcement.subscriberId()){
			queryBuilder.append(SUBSCRIBER_ID_COL  + " = " + sqlString(announcement.subscriberId()) + ", ");			
		}
		if(0 !=  announcement.clipId()){
			queryBuilder.append(CLIP_ID_COL + " = " + announcement.clipId() + ", ");
		}
		if(null != announcement.activationDate()){
			queryBuilder.append(START_TIME_COL + " = " + actDate + ", ");
		}
		if(null != announcement.deActivationDate()){
			queryBuilder.append(END_TIME_COL + " = " + deactDate + ", ");
		}
		if(null != announcement.frequency()){
			queryBuilder.append(FREQUENCY_COL + " = " + sqlString(announcement.frequency()) + ", ");
		}
		if(null !=  announcement.timeInterval()){
			queryBuilder.append(TIME_INTERVAL_COL + " = " + sqlString(announcement.timeInterval()) + ", ");
		}
		queryBuilder.append(STATUS_COL + " = " + announcement.status());
		
		queryBuilder.append(" WHERE " + SEQUENCE_ID_COL + " = " + announcement.sequenceId());
		queryBuilder.append(" AND " + STATUS_COL + " = " + ANNOUNCEMENT_DEACTIVE);

		logger.info("RBT::query " + queryBuilder.toString());

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			if (stmt.executeUpdate(queryBuilder.toString()) > 0)
				id = 0;
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return false;
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
		if (id == 0)
		{
			logger.info("RBT::updation to RBT_SUBSCRIBER_ANNOUNCEMENTS table successful");
			return true;
		}
		else
		{
			logger.info("RBT::updation to RBT_SUBSCRIBER_ANNOUNCEMENTS table failed");
			return false;
		}

	}

	
	static SubscriberAnnouncements insert(Connection conn, SubscriberAnnouncements announcement)	{
		logger.info("RBT::inside insert");

		int id = -1;
		String query = null;
		Statement stmt = null;

		String actDate = null;
        String deactDate = null; 
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
	        if (announcement.activationDate() != null) 
	        	actDate = sqlTime(announcement.activationDate()); 
	        if(announcement.deActivationDate() != null) 
	        	deactDate = sqlTime(announcement.deActivationDate()); 
		}
        else
        {
	        if (announcement.activationDate() != null) 
	        	actDate = mySQLDateTime(announcement.activationDate()); 
	        if(announcement.deActivationDate() != null) 
	        	deactDate = mySQLDateTime(announcement.deActivationDate()); 
        }
        	
		SubscriberAnnouncements subscriberAnnouncement = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + CLIP_ID_COL;
		query += ", " + STATUS_COL;
		query += ", " + START_TIME_COL;
		query += ", " + END_TIME_COL;
		query += ", " + FREQUENCY_COL;
		query += ", " + TIME_INTERVAL_COL;
		query += ")";

		query += " VALUES ( " + "'" + announcement.subscriberId() + "'";
		query += ", " + announcement.clipId();
		query += ", " + announcement.status();
		query += ", " + actDate;
		query += ", " + deactDate;
		query += ", " + sqlString(announcement.frequency());
		query += ", " + sqlString(announcement.timeInterval());
		query += ")";

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			if (stmt.executeUpdate(query) > 0)
				id = 0;
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
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
		if (id == 0)
		{
			logger.info("RBT::insertion to RBT_SUBSCRIBER_ANNOUNCEMENTS table successful");
			subscriberAnnouncement = getAnnouncementsRecord(conn, announcement.subscriberId(), announcement.clipId());
			if(null != subscriberAnnouncement){
				logger.info("RBT:: " + subscriberAnnouncement.toString());
			}
			return subscriberAnnouncement;
		}
		else
		{
			logger.info("RBT::insertion to RBT_SUBSCRIBER_ANNOUNCEMENTS table failed");
			return null;
		}
	}
	
	public static String getDBSelectionString(){
		return ResourceReader.getString("rbt", "DB_TYPE", "MYSQL");
    }
	
	protected static String sqlTime(java.util.Date date)
    {
    	DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	
        if (date != null)
            return toSQLDate(date, sqlTimeSpec, sqlTimeFormat);
        return null;
    }

	public String toString() {
		StringBuilder builder = new StringBuilder("[ " + SEQUENCE_ID_COL + " - " +  this.sequenceId() + ", ");
		builder.append(SUBSCRIBER_ID_COL + " - " +  this.subscriberId() + ", ");
		builder.append(CLIP_ID_COL + " - " +  this.clipId() + ", ");
		builder.append(STATUS_COL + " - " +  this.status() + ", ");
		builder.append(START_TIME_COL + " - " +  this.activationDate() + ", ");
		builder.append(END_TIME_COL + " - " +  this.deActivationDate() + ", ");
		builder.append(FREQUENCY_COL + " - " +  this.frequency() + ", ");
		builder.append(TIME_INTERVAL_COL + " - " +  this.timeInterval() + "]");
		return builder.toString();
	}
	
	public void setDeactivationDate(Date deactivationDate){
		m_deactivationDate = deactivationDate;
	}
	
	public void setActivationDate(Date activationDate){
		m_activationDate = activationDate;
	}
	
	public void setStatus(int status){
		m_status = status;
	}
	
	static boolean smUpdateAnnounceToBeActivated(Connection connection, String subscriberId, int status){
		logger.info("RBT::inside smUpdateAnnounceToBeActivated");
		StringBuilder queryBuilder = null;		
		Statement stmt = null;
		ResultSet rs = null;
		boolean updated = false;
		queryBuilder = new StringBuilder("UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = " + status );
		queryBuilder.append( " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId));
		queryBuilder.append(" AND " + STATUS_COL + " IN (" + ANNOUNCEMENT_ACTIVE + ", " + ANNOUNCEMENT_BASE_DEACTIVATION_PENDING + ")");
		logger.info("RBT::query " + queryBuilder.toString());
		try{
			logger.info("RBT::inside try block");
			stmt = connection.createStatement();
			if(stmt.executeUpdate(queryBuilder.toString()) > 0){				
				updated = true;
			}
					
		}catch(SQLException sqle){
			logger.error("", sqle);
			return false;

		}finally{
			try
			{
				if(null != rs){
					rs.close();
				}
				if(null != stmt){
					stmt.close();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}			
		}
		if(updated){
			logger.info("RBT:: Successfully updated");
		}
		return updated;
	}

	static SubscriberAnnouncements[] smGetActiveAndCallbackPendingSubAnnouncemets(Connection connection, String subscriberId){
		logger.info("RBT::inside smGetActiveAndCallbackPendingSubAnnouncemets");
		List<SubscriberAnnouncements> announcementList = new ArrayList<SubscriberAnnouncements>();
		StringBuilder queryBuilder = null;		
		Statement stmt = null;
		ResultSet rs = null;
		
		queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE ");
		queryBuilder.append(SUBSCRIBER_ID_COL + "=" + sqlString(subscriberId) + " AND ");
		queryBuilder.append(STATUS_COL + " IN ( " + ANNOUNCEMENT_ACTIVE + ", " + ANNOUNCEMENT_BASE_DEACTIVATION_PENDING + ")");
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			queryBuilder.append(" AND " + START_TIME_COL + " <= SYSDATE AND " + END_TIME_COL + " > SYSDATE" );
		else
			queryBuilder.append(" AND " + START_TIME_COL + " <= SYSDATE() AND " + END_TIME_COL + " > SYSDATE()" );
		
		logger.info("RBT::query " + queryBuilder.toString());
		try{
			logger.info("RBT::inside try block");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(queryBuilder.toString());
			while(rs.next()){
				announcementList.add(getObjectFromResultSet(rs));
			}			
		}catch(SQLException sqle){
			logger.error("", sqle);
			return null;

		}finally{
			try
			{
				if(null != rs){
					rs.close();
				}
				if(null != stmt){
					stmt.close();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}			
		}
		
		return announcementList.toArray(new SubscriberAnnouncements[0]);

	}

	static boolean deactivateAnnouncements(Connection connection, String subscriberId){
		logger.debug("inside deactivateAnnouncements");

		Statement stmt = null;
		boolean updated = false;
		
		String sysdate = "SYSDATE()";
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sysdate = "SYSDATE";

		StringBuilder queryBuilder = new StringBuilder(128);
		queryBuilder.append("UPDATE ").append(TABLE_NAME);
		queryBuilder.append(" SET ").append(STATUS_COL).append(" = ").append(ANNOUNCEMENT_TO_BE_DEACTIVED);
		queryBuilder.append(", ").append(END_TIME_COL).append(" = ").append(sysdate);
		queryBuilder.append(" WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId));
		queryBuilder.append(" AND ").append(STATUS_COL).append(" = ").append(ANNOUNCEMENT_ACTIVE);
		
		logger.info("query " + queryBuilder.toString());
		try{
			logger.debug("inside try block");
			stmt = connection.createStatement();
			if(stmt.executeUpdate(queryBuilder.toString()) > 0){				
				updated = true;
			}
		}catch(SQLException sqle){
			logger.error("", sqle);
			return false;

		}finally{
			try
			{
				if(null != stmt){
					stmt.close();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}			
		}
		if(updated){
			logger.info("RBT:: Successfully updated");
		}

		return updated;
	}
}
