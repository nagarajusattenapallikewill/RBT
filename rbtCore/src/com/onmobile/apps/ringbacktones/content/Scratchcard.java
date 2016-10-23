package com.onmobile.apps.ringbacktones.content;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.ResourceReader;
import com.onmobile.apps.ringbacktones.content.database.RBTPrimitive;

public class Scratchcard extends RBTPrimitive {
	
	private static Logger logger = Logger.getLogger(Scratchcard.class);
	
	private static String m_databaseType=getDBSelectionString();
    private static final String TABLE_NAME = "RBT_SCRATCH_CARD_TABLE";
    private static final String SCRATCH_CARD_NUMBER="SCRATCH_CARD_NO";
    private static final String SUB_CLASS="SUB_CLASS";
    private static final String CHARGE_CLASS="CHARGE_CLASS";
    private static final String START_DATE="START_DATE";
    private static final String END_DATE="END_DATE";
    private static final String STATE="STATE";
    private static final String PRECHARGE="PRECHARGE";
    private static final String CONTENT_ID="CONTENT_ID";
    private static final String CONTENT_TYPE="CONTENT_TYPE";
	   
	private String scratchNo;
	private String subClass;
	private String chargeClass;
	private Date startDate;
	private Date endDate;
	private String state;
	private String precharge;
	private String contentid;
	private String contentType;

	
	public String getScratchNo() {
		return scratchNo;
	}
	public String getSubClass() {
		return subClass;
	}

	public String getChargeClass() {
		return chargeClass;
	}

	
	

	public String getState() {
		return state;
	}
	public String getPrecharge() {
		return precharge;
	}
	public String getContentid() {
		return contentid;
	}
	public String getContentType() {
		return contentType;
	}
	

	
	public void setState(String state) {
		this.state = state;
	}
	public Scratchcard(String scratchNo, String subClass, String chargeClass,
			Date startDate, Date endDate, String state, String precharge,
			String contentid, String contentType) {
		super();
		this.scratchNo = scratchNo;
		this.subClass = subClass;
		this.chargeClass = chargeClass;
		this.startDate = startDate;
		this.endDate = endDate;
		this.state = state;
		this.precharge = precharge;
		this.contentid = contentid;
		this.contentType = contentType;
	}

	private static Scratchcard getScratchCardFromRS(ResultSet results) throws SQLException {
		String scratchno  = results.getString(SCRATCH_CARD_NUMBER);
		String subclass = results.getString(SUB_CLASS);
		String chargeclass = results.getString(CHARGE_CLASS);
		Date startdate = results.getDate(START_DATE);
		Date enddate = results.getDate(END_DATE);
		String state = results.getString(STATE);
		String precharge = results.getString(PRECHARGE);
		String contentid = results.getString(CONTENT_ID);
		String contenttype = results.getString(CONTENT_TYPE);
		
		return new Scratchcard(scratchno, subclass, chargeclass, startdate, enddate,state,precharge,contentid,contenttype);
	}
	
	
	public static int removeOldScratchCard(Connection conn,int duration, String state)
	{
		 logger.info("RBT::inside removeOldScratchCard");
	        
	      	int count = 0;
	      	String query = null;
			Statement stmt = null;
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = "DELETE FROM " + TABLE_NAME + " WHERE " + STATE + " = " + sqlString(state) + " AND " + START_DATE + " <= ( now() -" + duration + ")";
			else
				query = "DELETE FROM " + TABLE_NAME + " WHERE " + STATE + " = " + sqlString(state) + " AND " + START_DATE + " <= TIMESTAMPADD(DAY, -" + duration + ",SYSDATE())";

			try {
				stmt=conn.createStatement();
				count = stmt.executeUpdate(query);
				return count;
				
			} catch (SQLException e) {
				e.printStackTrace();
				return -1;
			}
			

			
	}

	public static boolean updateScratchCard(Connection conn,String scratchNo,String state)
	{
		 logger.info("RBT::inside updateScratchCard");
	        
	      	String query = null;
			Statement stmt = null;
			query = "UPDATE " + TABLE_NAME + " SET " + STATE + " ='" + state +"'"+" WHERE "+SCRATCH_CARD_NUMBER+" = "+scratchNo;
			try {
				stmt=conn.createStatement();
				stmt.executeUpdate(query);
				return true;
				
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
			

			
	}

	public static String getDBSelectionString()
	{
		return ResourceReader.getString("rbt", "DB_TYPE", "MYSQL");
	}

	public static Scratchcard getDetailsForScratchCard(Connection conn, String scratchNo)
    {
        logger.info("RBT::inside getDetailsForScratchCard");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		Scratchcard scratch = null;
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SCRATCH_CARD_NUMBER + " ='" + scratchNo +"'";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				scratch =getScratchCardFromRS(results);
			}
		}
        catch(SQLException se)
        {
        	logger.error(se);
            return null;
        }
		finally
		{
			try
			{
				stmt.close();
			}
			catch(Exception e)
			{
				logger.error(e);
			}
		}
		
		if(scratch!=null)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return scratch;
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}


}
