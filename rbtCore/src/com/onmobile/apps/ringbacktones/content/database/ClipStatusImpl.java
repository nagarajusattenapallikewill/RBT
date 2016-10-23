package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;

public class ClipStatusImpl extends RBTPrimitive {

	private static Logger logger = Logger.getLogger(ClipStatusImpl.class);
	private static Map<String, String> circleIdsMap = null;
	private static Map<String, String> reverseCircleIdsMap = null;

	private final static String TABLE_NAME = "RBT_CLIPS_STATUS";
	private final static String STATUS = "STATUS";
	private final static String TRANSFERRED_CIRCLES = "TRANSFERRED_CIRCLES";
	private final static String PENDING_CIRCLES = "PENDING_CIRCLES";
	private final static String CLIP_RBT_WAV_FILE = "CLIP_RBT_WAV_FILE";
	private final static String LAST_PROCESSED_DATE = "LAST_PROCESSED_DATE";
	private final static String m_databaseType = getDBSelectionString();
	private static String lastProcessedDate = null;

	/**
	 * 
	 * @param conn
	 * @param rbtWavFile For category entry it will have cat_"categoryId"
	 * @param circleIDStr
	 * @param status Status will be 0 for clip entries. For categories, the status will be -1.
	 * @return
	 */
	static boolean checkAndInsertClipWithStatus(Connection conn,
			String rbtWavFile, String circleIDStr, int status) {
		logger.info("RBT::inside checkAndInsertClipWithStatus .... ");
		String circleID = null;
		if (circleIdsMap == null || circleIdsMap.size() < 1) {
			getCircleIdMap();
		}
		circleID = circleIdsMap.get(circleIDStr);
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		String pendingCircles = null;
		String transferredCircles = null;

		try {
			stmt = conn.createStatement();
			query = "SELECT " + STATUS + "," + TRANSFERRED_CIRCLES + ","
					+ PENDING_CIRCLES + " FROM " + TABLE_NAME + " WHERE "
					+ CLIP_RBT_WAV_FILE + " = " + sqlString(rbtWavFile);
			results = stmt.executeQuery(query);
			logger.debug("Query: " + query + " Successfully executed");
			boolean isRecord = results.next();
			if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
				lastProcessedDate = "SYSDATE";
			} else {
				lastProcessedDate = "SYSDATE()";
			}
			if (isRecord) {
				pendingCircles = results.getString("PENDING_CIRCLES");
				//status = results.getString("STATUS");
				transferredCircles = results.getString("TRANSFERRED_CIRCLES");
				List<String> pendingCirclesList = (pendingCircles != null) ? Arrays
						.asList(pendingCircles.split(","))
						: null;
				List<String> transferredCirclesList = (transferredCircles != null) ? Arrays
						.asList(transferredCircles.split(","))
						: null;
				if ((pendingCirclesList != null && pendingCirclesList
						.contains(circleID))
						|| (transferredCirclesList != null && transferredCirclesList
								.contains(circleID))) {
					return true;
				} else {// one check pendingCircles containd circleid
					if (pendingCircles == null
							|| pendingCircles.equalsIgnoreCase("")
							|| pendingCircles.equalsIgnoreCase("null"))
						pendingCircles = circleID;
					else
						pendingCircles += "," + circleID;

					query = "UPDATE " + TABLE_NAME + " SET " + STATUS
							+ " = '" + status +"' , " + PENDING_CIRCLES + " =  "
							+ sqlString(pendingCircles) 
							+ "  WHERE  " + CLIP_RBT_WAV_FILE + " = "
							+ sqlString(rbtWavFile);
					int n = stmt.executeUpdate(query);
					if (n > 0) {
						logger.debug("Query: " + query
								+ " Successfully updated");
						return true;
					}
				}
			} else {
				query = "INSERT INTO " + TABLE_NAME + " VALUES("
						+ sqlString(rbtWavFile) + ",'" + status + "',NULL,"
						+ sqlString(circleID) + "," + lastProcessedDate + ")";
				int n = stmt.executeUpdate(query);
				logger.info("no. of records inserted: "+n);
				if (n>0) {
					logger.debug("Query: " + query
							+ " Successfully inserted");
					return true;
				}
			}

		} catch (SQLException se) {
			logger.error("", se);
			return false;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		return false;
	}

	static boolean updateStatusAndCircleIds(Connection conn, String status,
			String pendingCircles, String transferredCircles, String clipWavFile) {

		Statement stmt = null;
		int n = -1;


		String query = "UPDATE " + TABLE_NAME + " SET STATUS = "
				+ sqlString(status) + ", " + TRANSFERRED_CIRCLES + " = "
				+ sqlString(transferredCircles) + ", " + PENDING_CIRCLES
				+ " = " + sqlString(pendingCircles)
				+ " WHERE "	+ CLIP_RBT_WAV_FILE + " = " + sqlString(clipWavFile);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
			if (n > 0) {
				logger.debug("Query: " + query + " successfully updated");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	static boolean updateClipStatusByWavFile(Connection conn, String status,
			String clipWavFile) {

		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET STATUS = "
				+ sqlString(status) + " WHERE " + CLIP_RBT_WAV_FILE + " = "
				+ sqlString(clipWavFile);
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
			logger.info("Query For Updating Clip Status By Wav File : "+query);
			if (n > 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	static Map<String, String> getCirclesOfUnprocessedClips(Connection conn, int startFrom, int limit)
			throws Exception {

		String query = "SELECT " + CLIP_RBT_WAV_FILE + "," + PENDING_CIRCLES
				+ "," + TRANSFERRED_CIRCLES + " FROM " + TABLE_NAME
				+ " WHERE STATUS = '0' LIMIT " + startFrom + ", " + limit;
		Map<String, String> pendingCirclesClipMap = new HashMap<String, String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			logger.info("Executing query: " + query);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				String rbtWavFile = rs.getString(CLIP_RBT_WAV_FILE);
				String pendingCircles = rs.getString(PENDING_CIRCLES);
				String transCircles = rs.getString(TRANSFERRED_CIRCLES);
				logger.info("Sucessfully executing query. rbtWavFile: "
						+ rbtWavFile + ", pendingCircles: " + pendingCircles
						+ ", transCircles: " + transCircles);
				pendingCirclesClipMap.put(rbtWavFile, pendingCircles + "_"
						+ transCircles);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return pendingCirclesClipMap;
	}

	static Map<String, String> getPendingCirclesOfCategoryEntries(Connection conn)
			throws Exception {

		String query = "SELECT " + CLIP_RBT_WAV_FILE + "," + PENDING_CIRCLES
				+ " FROM " + TABLE_NAME
				+ " WHERE STATUS = '-1'";
		Map<String, String> pendingCirclesClipMap = new HashMap<String, String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			logger.info("Executing query: " + query);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				String rbtWavFile = rs.getString(CLIP_RBT_WAV_FILE);
				String pendingCircles = rs.getString(PENDING_CIRCLES);
				logger.info("Sucessfully executing query. rbtWavFile: "
						+ rbtWavFile + ", pendingCircles: " + pendingCircles);
				pendingCirclesClipMap.put(rbtWavFile, pendingCircles);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return pendingCirclesClipMap;
	}
	
	private static void getCircleIdMap() {
		circleIdsMap = new HashMap<String, String>();
		String circleIdsIntStr = null;
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setType("COMMON");
		applicationDetailsRequest.setName("CIRCLES_INTEGER_MAPPING_FOR_CLIP_STATUS");
		Parameter param = RBTClient.getInstance().getParameter(applicationDetailsRequest);
		if (param != null) {
			circleIdsIntStr = param.getValue();	
		}
		String str[] = circleIdsIntStr.split(";");
		if (str != null) {
			for (int i = 0; i < str.length; i++) {
				String circleInt[] = str[i].split(":");
				circleIdsMap.put(circleInt[0], circleInt[1]);
			}
		}
	}
	
	public static Map<String,String> getReverseCircleIdMap() {
		if (reverseCircleIdsMap != null) {
			return reverseCircleIdsMap;
		}
		reverseCircleIdsMap = new HashMap<String, String>();
		String circleIdsIntStr = null;
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setType("COMMON");
		applicationDetailsRequest.setName("CIRCLES_INTEGER_MAPPING_FOR_CLIP_STATUS");
		Parameter param = RBTClient.getInstance().getParameter(applicationDetailsRequest);
		if (param != null) {
			circleIdsIntStr = param.getValue();	
		}
		String str[] = circleIdsIntStr.split(";");
		if (str != null) {
			for (int i = 0; i < str.length; i++) {
				String circleInt[] = str[i].split(":");
				reverseCircleIdsMap.put(circleInt[1], circleInt[0]);
			}
		}
		return reverseCircleIdsMap;
	}
	
	//RBT-10215 Added for updating 
	public static boolean checkAndInsertClipWithStatusADRBT(Connection conn,
			String rbtWavFile, String circleIDAdRBTStr) {

		logger.info("RBT::inside checkAndInsertClipWithStatusADRBT .... ");

		List<String> circleNameList = Arrays.asList(circleIDAdRBTStr.split(","));
		List<String> circleIDList = new ArrayList<String>();
		if (circleIdsMap == null || circleIdsMap.size() < 1) {
			getCircleIdMap();
		}

		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		String pendingCircles = null;
		String transferredCircles = null;
		Set<String> pendingCirclesSet=new HashSet<String>();
		
		String status = null;
		try {
			stmt = conn.createStatement();
			query = "SELECT " + STATUS + "," + TRANSFERRED_CIRCLES + ","
					+ PENDING_CIRCLES + " FROM " + TABLE_NAME + " WHERE "
					+ CLIP_RBT_WAV_FILE + " = " + sqlString(rbtWavFile);
			results = stmt.executeQuery(query);
			logger.debug("Query: " + query + " Successfully executed");
			boolean isRecord = results.next();
			if (m_databaseType.equalsIgnoreCase(DB_SAPDB)) {
				lastProcessedDate = "SYSDATE";
			} else {
				lastProcessedDate = "SYSDATE()";
			}
			for (String circleIdstr : circleNameList) {
				circleIDList.add(circleIdsMap.get(circleIdstr));
			}
			logger.info("isRecord :"+isRecord);
			if (isRecord) {
				pendingCircles = results.getString("PENDING_CIRCLES");
				if (pendingCircles != null) {
					for (String pendingCircleIds : pendingCircles.split(",")) {
						pendingCirclesSet.add(pendingCircleIds);
					}
				}
				status = results.getString("STATUS");
				transferredCircles = results.getString("TRANSFERRED_CIRCLES");
				List<String> pendingCirclesList = (pendingCircles != null) ? Arrays
						.asList(pendingCircles.split(",")) : null;
				List<String> transferredCirclesList = (transferredCircles != null) ? Arrays
						.asList(transferredCircles.split(",")) : null;
				if ((pendingCirclesList != null && pendingCirclesList
						.containsAll(circleIDList))
						|| (transferredCirclesList != null && transferredCirclesList
								.containsAll(circleIDList))) {
					logger.info("In if of pendingCirclesList");
					return true;
					
				} else {// one check pendingCircles containd circleid
					circleIDList.removeAll(transferredCirclesList);
					pendingCirclesSet.addAll(circleIDList);
					pendingCircles = collectionToString(pendingCirclesSet);
					query = "UPDATE " + TABLE_NAME + " SET " + STATUS
							+ " = '0' , " + PENDING_CIRCLES + " =  "
							+ sqlString(pendingCircles) + "  WHERE  "
							+ CLIP_RBT_WAV_FILE + " = " + sqlString(rbtWavFile);
					logger.info("query in else for update: "+query);
					int n = stmt.executeUpdate(query);
					conn.commit();
					if (n > 0) {
						logger.info("Query: " + query
								+ " Successfully updated");
						return true;
					}
				}
			} else {
				String circleIdstr=collectionToString(circleIDList);
					query = "INSERT INTO " + TABLE_NAME + " VALUES("
							+ sqlString(rbtWavFile) + ",'0',NULL,"
							+ sqlString(circleIdstr) + "," + lastProcessedDate
							+ ")";
					logger.info("Query: "+query);
					int n = stmt.executeUpdate(query);
					conn.commit();
					logger.info("no. of records inserted: "+n);
					if (n>0) {
						logger.debug("Query: " + query
								+ " Successfully inserted");
						return true;
					}
				}
			
			
		} catch (SQLException se) {
			logger.info("Exception occur:", se);
			return false;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.info("Exception occur while closing: ", e);
			}
		}

		return false;

	}
	//RBT-10215
	private static String collectionToString(Collection<String> c)
	{
		String str="";
		int count=0;
		for(String s: c)
		{
			if(count==0)
				str+=s;
			else
				str+=","+s;
			count++;
		}
		return str;
	}

}
