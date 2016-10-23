package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RSSFeedScheduler;

public class RSSFeedScheduleImpl extends RBTPrimitive {
	private static Logger logger = Logger.getLogger(RSSFeedScheduleImpl.class);

	private static final String TABLE_NAME = "RBT_RSS_FEED_SCHEDULER";
	private static final String RSS_FEED_DAY_COL = "FEED_DAY";
	private static final String RSS_FEED_WEEK_ID_COL = "FEED_WEEK_ID";
	private static final String RSS_FEED_CIRCLE_GROUP_COL = "FEED_CIRCLE_GROUP";
	private static final String RSS_FEED_GROUP_ID_COL = "FEED_GROUP_ID";
	private static final String RSS_FEED_MODULE_COL = "FEED_MODULE";
	private static final String RSS_FEED_MODULE_ID_COL = "FEED_MODULE_ID";
	private static final String RSS_FEED_CATEGORY_COL = "FEED_CATEGORY";
	private static final String RSS_FEED_CATEGORY_ID_COL = "FEED_CATEGORY_ID";
	private static final String RSS_FEED_CP_NAME_COL = "FEED_CP_NAME";
	private static final String RSS_FEED_CP_ID_COL = "FEED_CP_ID";
	private static final String RSS_FEED_POSITION_COL = "FEED_POSITION";
	private static final String RSS_FEED_TIME_SLOT_COL = "FEED_TIME_SLOT";
	private static final String RSS_FEED_TIME_SLOT_ID_COL = "FEED_TIME_SLOT_ID";
	private static final String RSS_FEED_OM_CATEGORY_ID_COL = "FEED_OM_CATEGORY_ID";
	private static final String RSS_FEED_OM_CONTENT_NAME_COL = "FEED_OM_CONTENT_NAME";
	private static final String RSS_FEED_TYPE_COL = "FEED_TYPE";
	private static final String RSS_FEED_PUBLISH_DATE_COL = "FEED_PUBLISH_DATE";
	private static final String RSS_FEED_RELEASE_DATE_COL = "FEED_RELEASE_DATE";
	
	private static String m_databaseType = getDBSelectionString();

	static boolean insert(Connection conn, String feedDay, String feedWeekId,
			String feedCircleGroup, String feedGroupId, String feedModule, String moduleId,
			String feedCategory, String feedCategoryId, String feedCPName, String feedCPId,
			String feedPosition, String feedTimeSlot, String feedTimeSlotId,
			String feedOMCategoryId, String feedOMContentName, String feedType, String feedPubDate,
			String feedReleaseDate) {
		int id = -1;
		String query = null;
		Statement stmt = null;

		FeedSchedule feedSchedule = null;

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "INSERT INTO " + TABLE_NAME + " ( " + RSS_FEED_DAY_COL;
			query += ", " + RSS_FEED_WEEK_ID_COL;
			query += ", " + RSS_FEED_CIRCLE_GROUP_COL;
			query += ", " + RSS_FEED_GROUP_ID_COL;
			query += ", " + RSS_FEED_MODULE_COL;
			query += ", " + RSS_FEED_MODULE_ID_COL;
			query += ", " + RSS_FEED_CATEGORY_COL;
			query += ", " + RSS_FEED_CATEGORY_ID_COL;
			query += ", " + RSS_FEED_CP_NAME_COL;
			query += ", " + RSS_FEED_CP_ID_COL;
			query += ", " + RSS_FEED_POSITION_COL;
			query += ", " + RSS_FEED_TIME_SLOT_COL;
			query += ", " + RSS_FEED_TIME_SLOT_ID_COL;
			query += ", " + RSS_FEED_OM_CATEGORY_ID_COL;
			query += ", " + RSS_FEED_OM_CONTENT_NAME_COL;
			query += ", " + RSS_FEED_TYPE_COL;
			query += ", " + RSS_FEED_PUBLISH_DATE_COL;
			query += ", " + RSS_FEED_RELEASE_DATE_COL;
			query += ")";

			query += " VALUES ( " + sqlString(feedDay);
			query += ", " + sqlString(feedWeekId);
			query += ", " + sqlString(feedCircleGroup);
			query += ", " + sqlString(feedGroupId);
			query += ", " + sqlString(feedModule);
			query += ", " + sqlString(moduleId);
			query += ", " + sqlString(feedCategory);
			query += ", " + sqlString(feedCategoryId);
			query += ", " + sqlString(feedCPName);
			query += ", " + sqlString(feedCPId);
			query += ", " + sqlString(feedPosition);
			query += ", " + sqlString(feedTimeSlot);
			query += ", " + sqlString(feedTimeSlotId);
			query += ", " + sqlString(feedOMCategoryId);
			query += ", " + sqlString(feedOMContentName);
			query += ", " + sqlString(feedType);
			query += ", " + sqlString(feedPubDate);
			query += ", " + sqlString(feedReleaseDate);
			query += ")";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "INSERT INTO " + TABLE_NAME + " ( " + RSS_FEED_DAY_COL;
			query += ", " + RSS_FEED_WEEK_ID_COL;
			query += ", " + RSS_FEED_CIRCLE_GROUP_COL;
			query += ", " + RSS_FEED_GROUP_ID_COL;
			query += ", " + RSS_FEED_MODULE_COL;
			query += ", " + RSS_FEED_MODULE_ID_COL;
			query += ", " + RSS_FEED_CATEGORY_COL;
			query += ", " + RSS_FEED_CATEGORY_ID_COL;
			query += ", " + RSS_FEED_CP_NAME_COL;
			query += ", " + RSS_FEED_CP_ID_COL;
			query += ", " + RSS_FEED_POSITION_COL;
			query += ", " + RSS_FEED_TIME_SLOT_COL;
			query += ", " + RSS_FEED_TIME_SLOT_ID_COL;
			query += ", " + RSS_FEED_OM_CATEGORY_ID_COL;
			query += ", " + RSS_FEED_OM_CONTENT_NAME_COL;
			query += ", " + RSS_FEED_TYPE_COL;
			query += ", " + RSS_FEED_PUBLISH_DATE_COL;
			query += ", " + RSS_FEED_RELEASE_DATE_COL;
			query += ")";

			query += " VALUES ( " + sqlString(feedDay);
			query += ", " + sqlString(feedWeekId);
			query += ", " + sqlString(feedCircleGroup);
			query += ", " + sqlString(feedGroupId);
			query += ", " + sqlString(feedModule);
			query += ", " + sqlString(moduleId);
			query += ", " + sqlString(feedCategory);
			query += ", " + sqlString(feedCategoryId);
			query += ", " + sqlString(feedCPName);
			query += ", " + sqlString(feedCPId);
			query += ", " + sqlString(feedPosition);
			query += ", " + sqlString(feedTimeSlot);
			query += ", " + sqlString(feedTimeSlotId);
			query += ", " + sqlString(feedOMCategoryId);
			query += ", " + sqlString(feedOMContentName);
			query += ", " + sqlString(feedType);
			query += ", " + sqlString(feedPubDate);
			query += ", " + sqlString(feedReleaseDate);
			query += ")";
		}
		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			if (stmt.executeUpdate(query) > 0)
				id = 0;
		} catch (SQLException se) {
			logger.error("", se);
			return false;
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return true;
	}
	
	
	static List<RSSFeedScheduler> getRSSFeedRecords(Connection conn,String feedType){
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + RSS_FEED_TYPE_COL + " = "
				        + sqlString(feedType); 
		Statement stmt = null;
		List<RSSFeedScheduler> listRSSFeedScheduler = null;
		try{
			listRSSFeedScheduler = new ArrayList<RSSFeedScheduler>();
			logger.info("Query being run = "+query);
			Statement createStatement = conn.createStatement();
			ResultSet rs = createStatement.executeQuery(query); 
			while(rs.next()){
				String feedDay = rs.getString(RSS_FEED_DAY_COL);
				String feedWeekId = rs.getString(RSS_FEED_WEEK_ID_COL);
				String feedCircleGroup = rs.getString(RSS_FEED_CIRCLE_GROUP_COL);
				String feedGroupId = rs.getString(RSS_FEED_GROUP_ID_COL);
				String feedModule = rs.getString(RSS_FEED_MODULE_COL);
				String feedModuleId = rs.getString(RSS_FEED_MODULE_ID_COL);
				String feedCategory = rs.getString(RSS_FEED_CATEGORY_COL);
				String feedCategoryId = rs.getString(RSS_FEED_CATEGORY_ID_COL);
				String feedCPName = rs.getString(RSS_FEED_CP_NAME_COL);
				String feedCPId = rs.getString(RSS_FEED_CP_ID_COL);
				String feedPosition = rs.getString(RSS_FEED_POSITION_COL);
				String feedSlotTime = rs.getString(RSS_FEED_TIME_SLOT_COL);
				String feedTimeSlotId = rs.getString(RSS_FEED_TIME_SLOT_ID_COL);
				String feedOmCategoryId = rs.getString(RSS_FEED_OM_CATEGORY_ID_COL);
				String feedOmContentName = rs.getString(RSS_FEED_OM_CONTENT_NAME_COL);
				String feedTypeStr = rs.getString(RSS_FEED_TYPE_COL);
				String feedPublishDate = rs.getString(RSS_FEED_PUBLISH_DATE_COL);
				String feedReleaseDate = rs.getString(RSS_FEED_RELEASE_DATE_COL);
				RSSFeedScheduler rssFeedScheduler = new RSSFeedScheduler();
				rssFeedScheduler.setFeedDay(feedDay);
				rssFeedScheduler.setFeedWeekId(feedWeekId);
				rssFeedScheduler.setFeedCircleGroup(feedCircleGroup);
				rssFeedScheduler.setFeedGroupId(feedGroupId);
				rssFeedScheduler.setFeedModule(feedModuleId);;
				rssFeedScheduler.setFeedModuleId(feedModuleId);
				rssFeedScheduler.setFeedCategory(feedCategoryId);
				rssFeedScheduler.setFeedCategoryId(feedCategoryId);
				rssFeedScheduler.setFeedCPId(feedCPId);
				rssFeedScheduler.setFeedCPName(feedCPName);;
				rssFeedScheduler.setFeedPosition(feedPosition);
				rssFeedScheduler.setFeedTimeSlot(feedSlotTime);
				rssFeedScheduler.setFeedTimeSlotId(feedTimeSlotId);
				rssFeedScheduler.setFeedOMCategoryId(feedOmCategoryId);
				rssFeedScheduler.setFeedOMContentName(feedOmContentName);
				rssFeedScheduler.setFeedType(feedTypeStr);
				rssFeedScheduler.setFeedPublishDate(feedPublishDate);
				rssFeedScheduler.setFeedReleaseDate(feedReleaseDate);
				listRSSFeedScheduler.add(rssFeedScheduler);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return listRSSFeedScheduler ;
	}
	
	
}