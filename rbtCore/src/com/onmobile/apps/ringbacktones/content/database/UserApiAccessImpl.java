package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class has been written to fetch the details from the following table
 * rbt_user_api_access_rights for access rights validation.
 * 
 * @author balachandar.p
 * 
 */
public class UserApiAccessImpl extends RBTPrimitive {
	private static Logger logger = Logger.getLogger(AccessImpl.class);

	private static final String TABLE_NAME = "rbt_user_api_access_rights";
	private static final String USER_NAME_COL = "USERNAME";
	private static final String PASS_WORD_COL = "PASSWORD";
	private static final String ACCESS_DETAILS_COL = "API_ACTION_MOD_MAP";

	static HashMap<String, HashMap<String, List<String>>> getAccessDetails(
			Connection conn, String userName, String passWord)
			throws SQLException {
		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		HashMap<String, HashMap<String, List<String>>> urlAccessHashMap = new HashMap<String, HashMap<String, List<String>>>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_NAME_COL
				+ " = '" + userName + "' AND " + PASS_WORD_COL + " = '"
				+ passWord + "'";

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			if (results.next()) {
				String access_details = results.getString(ACCESS_DETAILS_COL);				
				HashMap<String, List<String>> modeHashMap ;//= new HashMap<String, List<String>>();

				if (access_details != null && !access_details.isEmpty()) {
					String[] apiModeAccessDetails = access_details.split(";");
					String[] apiModeDetails = null;
					String[] modeAccessDetails = null;
					String[] accessRights = null;
					String[] allModeAccessDetails = null;
					List<String> lstAccessDetails = null;
					if (apiModeAccessDetails != null
							&& apiModeAccessDetails.length > 0) {
						for (String eachApiAccess : apiModeAccessDetails) {
							modeHashMap = new HashMap<String, List<String>>();
							apiModeDetails = eachApiAccess.split(":");
							if (apiModeDetails.length > 0) {
								allModeAccessDetails = apiModeDetails[1]
										.split("~");
								if (allModeAccessDetails.length > 0) {
									for (String eachModeAccess : allModeAccessDetails) {
										lstAccessDetails = new ArrayList<String>();
										modeAccessDetails = eachModeAccess
												.split("-");
										if (modeAccessDetails.length > 0) {
											accessRights = modeAccessDetails[1]
													.split(",");
											for (String userRights : accessRights) {
												lstAccessDetails
														.add(userRights);
											}
										}
										modeHashMap.put(modeAccessDetails[0],
												lstAccessDetails);
									}
								}
							}

							urlAccessHashMap
									.put(apiModeDetails[0], modeHashMap);
							lstAccessDetails = null;
						}

					}
					apiModeAccessDetails = apiModeDetails = modeAccessDetails = accessRights = null;
				}

			}
		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			closeStatementAndRS(stmt, results);
		}
		return urlAccessHashMap;
	}
}
