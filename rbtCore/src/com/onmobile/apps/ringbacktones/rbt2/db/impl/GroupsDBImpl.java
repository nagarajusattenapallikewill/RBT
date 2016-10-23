package com.onmobile.apps.ringbacktones.rbt2.db.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.database.GroupsImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedGroups;


public class GroupsDBImpl{
	
	
	private static Logger logger = Logger.getLogger(GroupsImpl.class);
	
	static Connection connection = null;
    private static final String TABLE_NAME = "RBT_GROUPS";
	private static final String GROUP_ID_COL = "GROUP_ID";
    private static final String PRE_GROUP_ID_COL = "PRE_GROUP_ID";
    private static final String GROUP_NAME_COL = "GROUP_NAME";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String GROUP_PROMO_ID_COL = "GROUP_PROMO_ID";
    private static final String STATUS_COL = "STATUS";
    
    
	
    public static List<ExtendedGroups> getGroups(ExtendedGroups extendedgroup)
    {
        logger.info("RBT::inside getGroups");
        
      	StringBuilder query = new StringBuilder();
		Statement stmt = null;
		ResultSet results = null;
		
		ExtendedGroups extendedGroups = null;
		ArrayList<ExtendedGroups> extendedGroupsList = new ArrayList<ExtendedGroups>();

		query.append("SELECT * FROM ").append(TABLE_NAME) ;
		
		if(extendedgroup != null && extendedgroup.subID() != null){
		
			query.append(" WHERE ").append(SUBSCRIBER_ID_COL).append(" ='").append(extendedgroup.subID()).append("'");
			
			if(extendedgroup.groupID() >  -1){
				query.append(" AND ").append(GROUP_ID_COL).append(" = ").append(extendedgroup.groupID());
			}
			
			if(extendedgroup.groupName() != null){
				query.append(" AND ").append(GROUP_NAME_COL).append(" ='").append(extendedgroup.groupName()).append("'");
			}
			
			if(extendedgroup.preGroupID() != null){
				query.append(" AND ").append(PRE_GROUP_ID_COL).append(" ='").append(extendedgroup.preGroupID()).append("'");
			}
			if(!(extendedgroup.isOnlyActive() && extendedgroup.isOnlyDeactive()) && !(!extendedgroup.isOnlyActive() && !extendedgroup.isOnlyDeactive())){
				
				if(!extendedgroup.isOnlyActive()){
					query.append(" AND ").append(STATUS_COL).append(" != ").append("'B'");
				}
				if(!extendedgroup.isOnlyDeactive()){
					query.append(" AND ").append(STATUS_COL).append(" NOT IN ('D','X')");
				}
			}
		}
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
            connection = RBTDBManager.getInstance().getConnection();
			stmt = connection.createStatement();
			results = stmt.executeQuery(query.toString());
			while (results.next())
			{
				extendedGroups = getExtendedGroupsFromRS(results);
				extendedGroupsList.add(extendedGroups);
			}
		}
        catch(SQLException se)
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
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		
		if(extendedGroupsList.size() > 0)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return extendedGroupsList;
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
    
	private static ExtendedGroups getExtendedGroupsFromRS(ResultSet results) throws SQLException {
		int groupID  = results.getInt(GROUP_ID_COL);
		String preGroupID = results.getString(PRE_GROUP_ID_COL);
		String groupName = results.getString(GROUP_NAME_COL);
		String subscriberID = results.getString(SUBSCRIBER_ID_COL);
		String groupPromoID = results.getString(GROUP_PROMO_ID_COL);
		String status = results.getString(STATUS_COL);
		
		return new ExtendedGroups(groupID, preGroupID, groupName, subscriberID, groupPromoID,
				status);
	}

}
