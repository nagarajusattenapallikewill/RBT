package com.onmobile.apps.ringbacktones.daemons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.ResourceReader;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.common.exception.OnMobileException;

public class EmotionRbtUserSmsNotifier extends Thread
{
	private static Logger logger = Logger.getLogger(EmotionRbtUserSmsNotifier.class);
	
	private RBTDaemonManager m_mainDaemonThread = null; 
	private static RBTDBManager rbtDBManager = null;
	ParametersCacheManager rbtParamCacheManager = null;
	
	private String DATABASE_TYPE = "MYSQL";
	private int smsNotificationHour = 10;

	protected EmotionRbtUserSmsNotifier (RBTDaemonManager mainDaemonThread)
	{
		try
		{
			setName("EmotionRbtUserSmsNotifier");
			m_mainDaemonThread = mainDaemonThread;
			init();
		}
		catch(Exception e)
		{
			logger.error("Issue in creating EmotionRbtUserSmsNotifier", e);
		}
	}

	public void init()
	{
		rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		rbtDBManager = RBTDBManager.getInstance();
		DATABASE_TYPE = ResourceReader.getString("rbt", "DB_TYPE", "MYSQL");
		smsNotificationHour = getParamAsInt("DAEMON", "EMOTION_SMS_NOTIFICATION_HOUR",10); 
	}
	
	public void run()
	{
		while(m_mainDaemonThread != null && m_mainDaemonThread.isAlive()) 
		{
			try
			{
				List<SelectionInfoObject> subscriberList = getActiveEmotionSelections();

				for (SelectionInfoObject selectionInfoObject : subscriberList) 
				{
					if (sendSmsNotification(selectionInfoObject.getSubscriberID()))
					{
						updateInfo(selectionInfoObject);
					}
				}
			}
			catch(Exception e)
			{
				logger.error("", e);
			}

			try
			{
				logger.info("EmotionRbtUserSmsNotifier Sleeping for 10 minutes............");
				Thread.sleep(getParamAsInt("DAEMON", "SLEEP_INTERVAL_MINUTES_EMOTIONS", 10) * 60 * 1000);
			}
			catch(Exception e)
			{
			}
		}
	}
	
	public List<SelectionInfoObject> getActiveEmotionSelections()
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		List<SelectionInfoObject> subcriberList = new ArrayList<SelectionInfoObject>();
		
		String sql = "SELECT SUBSCRIBER_ID,INTERNAL_REF_ID,SELECTION_INFO FROM RBT_SUBSCRIBER_SELECTIONS WHERE " +
				"END_TIME >= "+ getDateString(false) +" AND END_TIME < " +getDateString(true)+ " AND "+
				"STATUS ="+94 + " AND " +
				"SELECTION_INFO NOT LIKE '%EMO%'";
		logger.info("Sql query >"+sql);
		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			while(rs.next())
			{
				SelectionInfoObject selectioInfoObject = new SelectionInfoObject();
				selectioInfoObject.setSubscriberID(rs.getString("SUBSCRIBER_ID"));
				selectioInfoObject.setRefID(rs.getString("INTERNAL_REF_ID"));
				selectioInfoObject.setSelectionInfo(rs.getString("SELECTION_INFO"));
				subcriberList.add(selectioInfoObject);
			}
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn, stmt, rs);
		}
		return subcriberList;
	}
	
	private String getDateString(boolean isEndDate)
	{
		int hourDiff = 12 - smsNotificationHour;
		if (hourDiff <= 0)
			hourDiff = 2;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		if (isEndDate)
			cal.set(Calendar.HOUR_OF_DAY, (cal.get(Calendar.HOUR_OF_DAY)+hourDiff));
	
		String dateString = null;
		if(DATABASE_TYPE.equalsIgnoreCase("SAPDB")) 
		{
			dateString = "TO_DATE('"+cal.get(Calendar.YEAR)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH);
			dateString += " "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
			dateString += "', 'YYYY/MM/DD HH24:MI:SS')";
		}
		else
		{
			dateString = "DATE_FORMAT('"+cal.get(Calendar.YEAR)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH);
			dateString += " "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
			dateString += "', '%Y/%m/%d %H:%i:%s')";
		}
		
		return dateString;
	}
	
	public void updateInfo(SelectionInfoObject selectionInfoObject)
	{
		String selectionInfo = null;
		selectionInfo = "EMO:"+ selectionInfoObject.getSelectionInfo();
		
		Connection conn = null;
		Statement stmt = null;
		String sql = "UPDATE RBT_SUBSCRIBER_SELECTIONS SET SELECTION_INFO='"+ selectionInfo + "'" +
				" WHERE SUBSCRIBER_ID ='"+ selectionInfoObject.getSubscriberID()+ "'" +
				" AND INTERNAL_REF_ID='" + selectionInfoObject.getRefID()+ "'";
		
		logger.info("Sql Query >"+sql);
		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn, stmt, null);
		}
	}
	
	public boolean sendSmsNotification(String subscriberID)
	{
		String msg = getParamAsString("DAEMON", "EMOTION_SMS_NOTIFICATION", "Your emotion selection is going to be expired in 1 hr. To set new emotion send sms EMOTION <songcode> to 888");
		boolean sendSms = false;
		try
		{
			Tools.sendSMS(getParamAsString("SMS","SMS_NO","123456"), subscriberID, msg, false);
			sendSms = true;
		}
		catch (OnMobileException e) 
		{
			logger.error("", e);
			sendSms = false;
		}
		return sendSms;
	}

	private Connection getConnection()
	{
		return rbtDBManager.getConnection();
	}
	
	private static boolean releaseConnection(Connection conn, Statement stmt, ResultSet rs)
	{
		try
		{
			if(rs != null)
				rs.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing db resultset", e);
			
		}
		
		try
		{
			if(stmt != null)
				stmt.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing db statement", e);
			
		}
	
		return rbtDBManager.releaseConnection(conn);
	}
	
	private String getParamAsString(String type, String param, String defualtVal)
	{
		try
		{
			return rbtParamCacheManager.getParameter(type, param, defualtVal).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defualtVal;
		}
	}
	
	public int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = rbtParamCacheManager.getParameter(type, param, defaultVal+"").getValue();
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}
	
	private class SelectionInfoObject
	{
		private String subscriberID = null;
		private String refID = null;
		private String selectionInfo = null;
		/**
		 * @return the subscriberID
		 */
		public String getSubscriberID() {
			return subscriberID;
		}
		/**
		 * @param subscriberID the subscriberID to set
		 */
		public void setSubscriberID(String subscriberID) {
			this.subscriberID = subscriberID;
		}
		/**
		 * @return the refID
		 */
		public String getRefID() {
			return refID;
		}
		/**
		 * @param refID the refID to set
		 */
		public void setRefID(String refID) {
			this.refID = refID;
		}
		/**
		 * @return the selectionInfo
		 */
		public String getSelectionInfo() {
			return selectionInfo;
		}
		/**
		 * @param selectionInfo the selectionInfo to set
		 */
		public void setSelectionInfo(String selectionInfo) {
			this.selectionInfo = selectionInfo;
		}
		
		
	}
}
