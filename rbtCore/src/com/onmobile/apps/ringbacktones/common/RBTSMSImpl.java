package com.onmobile.apps.ringbacktones.common;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.common.db.OnMobileDBServices;

public class RBTSMSImpl implements RBTSMS{
	
	final static String TABLE_NAME = "RBT_SMS_DETAILS";
	
	private static Logger logger = Logger.getLogger(RBTSMSImpl.class);
	
	static String EVENT_TYPE_COL= "EVENT_TYPE";
	static String SUBSCRIBERID_COL = "SUBSCRIBERID";
	static String SUBSCRIBER_TYPE_COL = "SUBSCRIBER_TYPE";
	static String REQUEST_COL = "REQUEST";
	static String RESPONSE_COL = "RESPONSE";
	static String REQUESTED_TIMESTAMP_COL = "REQUESTED_TIMESTAMP";
	static String RESPONSE_TIMEINMS_COL = "RESPONSE_TIMEINMS";
	static String REFERENCE_ID_COL = "REFERENCE_ID";
	static String REQUEST_DETAIL_COL = "REQUEST_DETAIL";
	static String RESPONSE_DETAIL_COL = "RESPONSE_DETAIL";
	
	static String FILE_NAME_COL = "FILE_NAME";
	static String LINE_NO_COL = "LINE_NO";	
	
	static boolean m_usePool = true;
	private static String dbUrl = null;
	
	public RBTSMSImpl(){
		dbUrl = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RBT_SMS_DB_URL", null);
	}
	
	private String makeDBString(String value){
		int index = -1;
		int fromIndex = 0;
		while((index = value.indexOf('\'',fromIndex)) != -1){
			fromIndex = index + 2;
			StringBuffer sb = new StringBuffer(value);
			sb.replace(index, index+1, "\'\'");
			value = sb.toString();
		}
		value = "'" + value + "'";
		
		return value;
	}
	
	public void insert(String eventType,String subscriberID, String subscriberType, 
			String request, String response, String requestedTimestamp, String responseTimeinms, String referenceID,
			String requestDetail, String responseDetail){
		
		logger.info("RBT::Inside insert ");
		
		String requesttime = null;
		try{
			requesttime = "'" + new Timestamp(Long.valueOf(requestedTimestamp).longValue()).toString() + "'";
		}
		catch(Exception e){
			logger.error("", e);
			requesttime = null;
		}
		String responsetime = null;
		try{
			responsetime = "'" + new Timestamp(Long.valueOf(responseTimeinms).longValue()).toString() + "'";
		}
		catch(Exception e){
			logger.error("", e);
			responsetime = null;
		}
		
		request = makeDBString(request);
		response = makeDBString(response);
		
		
		Connection con = getConnection(dbUrl);
		String query = "INSERT INTO " + TABLE_NAME + "(" + EVENT_TYPE_COL + "," + SUBSCRIBERID_COL + "," + 
		SUBSCRIBER_TYPE_COL + "," + REQUEST_COL + "," + RESPONSE_COL + "," + REQUESTED_TIMESTAMP_COL + "," +
		RESPONSE_TIMEINMS_COL + "," + REFERENCE_ID_COL + "," + REQUEST_DETAIL_COL + "," + RESPONSE_DETAIL_COL +
		") " +	" values('" + eventType + "','" + subscriberID + "','" + subscriberType + "'," + request + "," + 
		response + "," + requesttime + "," + responsetime + ",'" + referenceID + "','" +
		requestDetail + "','" + responseDetail + "')";
		Statement st = null;
		logger.info("RBT::Query ==> " + query);
		try{
			st = con.createStatement();
			st.executeUpdate(query);
		}
		catch(Exception e){
			logger.error("", e);
		}
		finally{
			try{
				if(st!=null)
					st.close();				
			}
			catch(Exception e){
				logger.error("", e);
			}
		}
		releaseConnection(con);
		logger.info("RBT::Exit insert ");
	}	
	
	public ArrayList getSubscriberSMS(String subscriberID){
		System.out.println("Inside SDRSMSDetailsImpl");
		logger.info("RBT::Inside getSubscriberSMS ");
		Connection conn = getConnection(dbUrl);
		
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + 
			SUBSCRIBERID_COL + " = '" + subscriberID.trim() + "' ORDER BY " + RESPONSE_TIMEINMS_COL + " DESC ";
		logger.info("RBT::Query ==> " + query);
		Statement st = null;
		ResultSet rs = null;
		ArrayList list = new ArrayList();
		try{
			st = conn.createStatement();
			rs = st.executeQuery(query);			
			while(rs.next()){
				try{
					String[] obj = fillFromRS(rs);
					list.add(obj);
				}
				catch(Exception e){
					logger.error("", e);
				}
			}			
		}
		catch(Exception e){
			logger.error("", e);			
		}
		finally{
			try{
				if(st!=null)
					st.close();
				if(rs!=null)
					rs.close();
			}
			catch(Exception e){
				logger.error("", e);
			}
		}
		
		releaseConnection(conn);
		
		if(list.size() > 0){
			return list;
		}
		logger.info("RBT::Exit getSubscriberSMS ");
		return null;
	}
	
	public void deleteOldEntry(int days){
		System.out.println("Inside deleteOldEntry");
		logger.info("RBT::Inside deleteOldEntry ");
		Connection con = getConnection(dbUrl);
		
		String query = "DELETE from " + TABLE_NAME + " where " + RESPONSE_TIMEINMS_COL +
			" <= (SYSDATE-" + days + ")";
		
		logger.info("RBT::Query ==> " + query);
		
		Statement st = null;
		try{
			st = con.createStatement();
			st.executeUpdate(query);
		}
		catch(Exception e){
			logger.error("", e);
		}
		finally{
			try{
				if(st!=null){
					st.close();
				}
			}
			catch(Exception e){
				logger.error("", e);
			}
		}
		
		releaseConnection(con);
		logger.info("RBT::Exit deleteOldEntry ");
	}	
	
	private String[] fillFromRS(ResultSet rs) throws Exception{
		String recordStr[] = new String[10];
		recordStr[0] = rs.getString(EVENT_TYPE_COL);
		recordStr[1] = rs.getString(SUBSCRIBERID_COL);  
		recordStr[2] = rs.getString(SUBSCRIBER_TYPE_COL);
		recordStr[3] = rs.getString(REQUEST_COL);
		recordStr[4] = rs.getString(RESPONSE_COL);
		Timestamp requesttime =rs.getTimestamp(REQUESTED_TIMESTAMP_COL);;
		if(requesttime == null)
			recordStr[5] = "NA";
		else
			recordStr[5] = "" + requesttime.getTime();
		Timestamp responsetime = rs.getTimestamp(RESPONSE_TIMEINMS_COL);
		if(responsetime == null)
			recordStr[6] = "NA";
		else
			recordStr[6] = "" + responsetime.getTime();
		recordStr[7] = rs.getString(REFERENCE_ID_COL);;
		recordStr[8] = String.valueOf(rs.getInt(REQUEST_DETAIL_COL));
		recordStr[9] = String.valueOf(rs.getInt(RESPONSE_DETAIL_COL));
		
		return recordStr;
		
	}
	
	private static Connection getConnection(String dbURL)
	{
		logger.info("RBT::using Pool "
				+ m_usePool);
		logger.info("RBT::DB URL " + dbURL);

		Connection conn = null;
		if (m_usePool == true)
		{
			try
			{
				if (dbURL.startsWith("jdbc:sapdb://"))
				{
					dbURL = dbURL.substring(13);
				}

				conn = OnMobileDBServices.getDBConnection(dbURL, null, null);//OnMobileDBServices.getDBConnection("rbt");
			}
			catch (Exception e)
			{
				logger.error("", e);
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			try
			{
				if (!dbURL.startsWith("jdbc:sapdb://"))
					dbURL = "jdbc:sapdb://" + dbURL;

				Class.forName("com.sap.dbtech.jdbc.DriverSapDB");

				conn = DriverManager.getConnection(dbURL, null, null);
			}
			catch (Exception e)
			{
				logger.error("", e);
				e.printStackTrace();
				return null;
			}
		}
		if (conn == null)
			logger.info("RBT::connection null");

		return conn;
	}
	
	private static boolean releaseConnection(Connection conn)
	{
		logger.info("RBT::using Pool "
				+ m_usePool);

		if (m_usePool == true)
		{
			try
			{
				OnMobileDBServices.releaseConnection(conn);
			}
			catch (Exception e)
			{
				return false;
			}
		}
		else
		{
			try
			{
				conn.close();
			}
			catch (Exception e)
			{
				return false;
			}

		}
		return true;
	}
	
	public static void main(String args[]){
		long time = System.currentTimeMillis()- (1000*60);
		//int count = 0;
		
		/*try{
			Thread.sleep(6*(1000*60*60));
		}
		catch(Exception e){
			
		}
		long time1 = System.currentTimeMillis();
		System.out.println(time);
		System.out.println(time1);
		System.out.println(time1-time);*/
		
		
		while(true){
			if(((System.currentTimeMillis()-time) >= (1000*60))){
				//count++;
				System.out.println("Hai");
				time = System.currentTimeMillis();
			}
		}
	}
	
	
}
