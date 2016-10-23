package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.content.ShufflePromo;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class ShufflePromoImpl extends RBTPrimitive implements ShufflePromo {
	private static Logger logger = Logger.getLogger(ShufflePromoImpl.class);

	private static final String TABLE_NAME = "RBT_SHUFFLE_PROMO";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String MODE_COL = "MODE";
	private static final String SERVICE_START_DATE_COL = "SERVICE_START_DATE";
	private static final String SERVICE_END_DATE_COL = "SERVICE_END_DATE";
	private static final String CATEGORY_ID_COL = "CATEGORY_ID";
	private static final String EXTRA_INFO_COL = "EXTRA_INFO";
	
	private String m_subscriberId;
	private String m_mode;
	private Date m_serviceStartDate;
	private Date m_serviceEndDate;
	private int m_categoryId;
	private String m_extraInfo;
	private static String m_databaseType=getDBSelectionString();


	public int categoryId() {
		return m_categoryId;
	}

	public String mode() {
		return m_mode;
	}

	public Date serviceEndDate() {
		return m_serviceEndDate;
	}

	public Date serviceStartDate() {
		return m_serviceStartDate;
	}

	public String subID() {
		return m_subscriberId;
	}

	public String extraInfo() {
		return m_extraInfo;
	}

	public static void deactivateShuffleAndSetSelection(Connection conn, String dummyCatId) {
		String query = "SELECT * FROM " + TABLE_NAME+ " LIMIT 5000";
		logger.info("query is " + query);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rSet = stmt.executeQuery(query);

			while(rSet.next()){
				Date expiryDate = rSet.getDate(4);
				if(new Date().after(expiryDate)){
					if(!deactivateShuffleAndSetSelection(conn,rSet.getString(1),rSet.getInt(5),dummyCatId)) 
						logger.error("Unable to delete expired subscriber(Subscriber Id:"+rSet.getString(1)+" from table "+TABLE_NAME);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private static boolean deactivateShuffleAndSetSelection(Connection conn, String subscriberId, int categoryId,String dummyCatId) {
		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId);
		logger.info("query is " + query);
		
		Statement stmt = null;
		RBTClient rbtClient = null;
		SelectionRequest presentSelectionRequest = null;
		SelectionRequest newSelectionRequest = null;
		int count = -1;
		boolean isAlreadyDeactivated = true;
		
		try {
			stmt = conn.createStatement();
			count = stmt.executeUpdate(query);
			SubscriberStatus[] subActiveSelections = SubscriberStatusImpl.getAllActiveSubscriberSettings(conn, subscriberId, 0);
			logger.info("Starter pack subscriber's(sub Id:"+subscriberId+")active selections: "+subActiveSelections);
			if(subActiveSelections!= null){
				for(int i=0;i<subActiveSelections.length;i++){
					if(subActiveSelections[i].categoryID()==categoryId){
						isAlreadyDeactivated = false;
						break;
					}
				}
				if(!isAlreadyDeactivated){
						rbtClient = RBTClient.getInstance();
						presentSelectionRequest = getPresentSelectionRequest(subscriberId,categoryId);
						newSelectionRequest = getNewSelectionRequest(subscriberId,categoryId,dummyCatId);
						newSelectionRequest.setInLoop(true);
						rbtClient.deleteSubscriberSelection(presentSelectionRequest);
						rbtClient.addSubscriberSelection(newSelectionRequest);
				}else{
					logger.info("Starter pack subscriber(sub Id:"+subscriberId+") with selection(category id:"+categoryId+") already deactivaed before expiry.");
				}
			}else{
				logger.info("Starter pack subscriber(sub Id:"+subscriberId+") has no active selections");
			}
		}
		catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return count>0;
	}

	private static SelectionRequest getPresentSelectionRequest(
			String subscriberId, int categoryId) {
		SelectionRequest selectionRequest = null;
		try{
			selectionRequest = new SelectionRequest(subscriberId);
			selectionRequest.setCategoryID(String.valueOf(categoryId));
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return selectionRequest;
	}

	private static SelectionRequest getNewSelectionRequest(String subscriberId, int categoryId, String dummyCatId) {
		SelectionRequest selectionRequest = null;
		RBTCacheManager rbtCacheManager = null;
		try{
			rbtCacheManager = RBTCacheManager.getInstance();
			selectionRequest = new SelectionRequest(subscriberId);
			Clip[] clips = rbtCacheManager.getActiveClipsInCategory(categoryId);
			selectionRequest.setClipID(String.valueOf(clips[0].getClipId()));
			selectionRequest.setCategoryID(dummyCatId);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return selectionRequest;
	}

	public static void activateShuffle(Connection conn, Category category,
			Subscriber sub, Date endDate) {
		String query = null;
		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + MODE_COL;
		query += ", " + SERVICE_START_DATE_COL;
		query += ", " + SERVICE_END_DATE_COL;
		query += ", " + CATEGORY_ID_COL;
		query += ")";
			
		query += " VALUES ( ";
		query += sqlString(sub.subID()) + ", ";
		query += sqlString("SMS") + ", ";
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += "SYSDATE" + ", ";
		else
			query += "SYSDATE()" + ", ";
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(endDate) + ", ";
		else
			query += mySQLDateTime(endDate) + ", ";
		query += category.getID();
		query += ")";
		
		logger.info("query is " + query);
		
		Statement stmt = null;
		int id = -1;
		try
        {
				stmt = conn.createStatement();
				if (stmt.executeUpdate(query) > 0)
					id = 0;
        }
        catch(SQLException se)
        {
            logger.error("", se);
            return;
        }
		finally
		{
			try
			{
				stmt.close();
			}
			catch(Exception e)
			{
				 logger.error("", e);
			}
		}
        if(id == 0)
        {
            logger.info("RBT::insertion to RBT_SHUFFLE_PROMO table successful");
        } 
		else
        {
            logger.info("RBT::insertion to RBT_SHUFFLE_PROMO table failed");
            return;
        }
	}
	
	
}
