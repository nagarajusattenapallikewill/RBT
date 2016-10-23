/**
 * 
 */
package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.utils.Encryption128BitsAES;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;

/**
 * @author vinayasimha.patil
 *
 */
public class RBTLoginUserImpl extends RBTPrimitive implements RBTLoginUser
{
	private static Logger logger = Logger.getLogger(RBTLoginUserImpl.class);

	private static final String TABLE_NAME = "RBT_LOGIN_USER";
	private static final String USER_ID_COL = "USER_ID";
	private static final String PASSWORD_COL = "PASSWORD";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String TYPE_COL = "TYPE";
	private static final String USER_INFO_COL = "USER_INFO";
	private static final String CREATION_TIME_COL = "CREATION_TIME";
	private static final String UPDATE_TIME_COL = "UPDATE_TIME";

	private String userID = null;
	private String password = null;
	private String subscriberID = null;
	private String type = null;
	private HashMap<String, String> userInfo = null;
	private Date creationTime = null;
	private Date updateTime = null;

	/**
	 * @param userID
	 * @param password
	 * @param subscriberID
	 * @param type
	 * @param userInfo
	 * @param creationTime
	 */
	public RBTLoginUserImpl(String userID, String password,
			String subscriberID, String type, HashMap<String, String> userInfo, Date creationTime)
	{
		this.userID = userID;
		this.password = password;
		this.subscriberID = subscriberID;
		this.type = type;
		this.userInfo = userInfo;
		this.creationTime = creationTime;
	}
	
	/**
	 * @param userID
	 * @param password
	 * @param subscriberID
	 * @param type
	 * @param userInfo
	 * @param creationTime
	 */
	public RBTLoginUserImpl(String userID, String password,
			String subscriberID, String type, HashMap<String, String> userInfo, Date creationTime, Date updateTime)
	{
		this.userID = userID;
		this.password = password;
		this.subscriberID = subscriberID;
		this.type = type;
		this.userInfo = userInfo;
		this.creationTime = creationTime;
		this.updateTime = updateTime;
	}
	
	public RBTLoginUserImpl(String userID, String password,
			String subscriberID, String type, HashMap<String, String> userInfo, Date creationTime, Date updateTime, String appName)
	{
		this.userID = userID;
		this.password = password;
		this.subscriberID = subscriberID;
		this.type = type;
		this.userInfo = userInfo;
		this.creationTime = creationTime;
		this.updateTime = updateTime;
	}

	public String userID()
	{
		return userID;
	}

	public String password()
	{
		return password;
	}

	public String subscriberID()
	{
		return subscriberID;
	}

	public String type()
	{
		return type;
	}

	public HashMap<String, String> userInfo()
	{
		return userInfo;
	}

	public Date creationTime()
	{
		return creationTime;
	}
	
	public Date updateTime()
	{
		return updateTime;
	}


	public static RBTLoginUser insert(Connection conn, String userID, String password,
			String subscriberID, String type, HashMap<String, String> userInfo, Date creationTime,Date updateTime)
	{
		logger.info("RBT::inside insert");

		int id = -1;
		String query = null;
		Statement stmt = null;

		String userInfoXML = DBUtility.getAttributeXMLFromMap(userInfo);

		String creationTimeStr = null;
		String updateTimeStr = null;
		if (getDBSelectionString().equalsIgnoreCase(DB_SAPDB)){
			creationTimeStr = sqlTime(creationTime);
			updateTimeStr = sqlTime(updateTime);
		}else{
			creationTimeStr = mySQLDateTime(creationTime);
			updateTimeStr = mySQLDateTime(updateTime);
		}


		query = "INSERT INTO " + TABLE_NAME
		+ " ( " + USER_ID_COL
		+ ", " + PASSWORD_COL
		+ ", " + SUBSCRIBER_ID_COL
		+ ", " + TYPE_COL
		+ ", " + USER_INFO_COL
		+ ", " + CREATION_TIME_COL
		+ ", " + UPDATE_TIME_COL 
		+ ")"
		+ " VALUES ( " + sqlString(userID)
		+ ", " + sqlString(password)
		+ ", " + sqlString(subscriberID)
		+ ", " + sqlString(type)
		+ ", " + sqlString(userInfoXML)
		+ ", " + creationTimeStr 
		+ ", " + updateTimeStr
		+ ")";

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			id = stmt.executeUpdate(query);
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
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if (id > 0)
		{
			logger.info("RBT::insertion to RBT_LOGIN_USER table successful");
			return (new RBTLoginUserImpl(userID, password, subscriberID, type, userInfo, creationTime));
		}
		else
		{
			logger.info("RBT::insertion to RBT_LOGIN_USER table failed");
			return null;
		}
	}

	public static boolean update(Connection conn, String userID,
			String newUserID, String password, String subscriberID,
			String type,  HashMap<String, String> existingUserInfo, HashMap<String, String> userInfo, Date creationTime, String oldPassword)
	{
		logger.info("RBT::inside insert");

		int id = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET ";

		String updateString = "";
		if(newUserID != null)
			updateString += USER_ID_COL + " = "+ sqlString(newUserID);
		if(password != null)
		{
			if(!updateString.equals(""))
				updateString += ", ";
			updateString += PASSWORD_COL + " = "+ sqlString(password);
			String updateTime = null;
			if (getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
				updateTime = "SYSDATE";
			else
				updateTime = "SYSDATE()";
			updateString += ", " + UPDATE_TIME_COL + " = " +updateTime;
		}
		if (subscriberID != null)
		{
			if(!updateString.equals(""))
				updateString += ", ";
			updateString += SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID);
		}
		if (userInfo != null && userInfo.size() > 0)
		{
			if(!updateString.equals(""))
				updateString += ", ";

			String userInfoXML = DBUtility.getAttributeXMLFromMap(existingUserInfo);
			Set<String> keySet = userInfo.keySet();
			for (String key : keySet)
				userInfoXML = DBUtility.setXMLAttribute(userInfoXML, key, userInfo.get(key));

			updateString += USER_INFO_COL + " = " + sqlString(userInfoXML);
		}
		if (creationTime != null)
		{
			if (!updateString.equals(""))
				updateString += ", ";
			updateString += CREATION_TIME_COL + " = " + mySqlTime(creationTime);
		}

		query += updateString;
		query += " WHERE "+ USER_ID_COL +" = "+ sqlString(userID);
		if (type != null)
			query += " AND "+ TYPE_COL +" = "+ sqlString(type);
		
		if(oldPassword != null) {
			query += " AND "+ PASSWORD_COL +" = "+ sqlString(oldPassword);
		}

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			id = stmt.executeUpdate(query);
		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}

		return (id > 0);
	}

	public static boolean deleteByUserID(Connection conn, String userID, String type)
	{
		int id = -1;
		String query = null;
		Statement stmt = null;
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + USER_ID_COL + " = "
		+ sqlString(userID) + " AND " + TYPE_COL + " = "
		+ sqlString(type); 

		logger.info("RBT::query " + query);

		try
		{
			stmt = conn.createStatement();
			id = stmt.executeUpdate(query);
		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}

		return (id > 0);
	}

	public static boolean deleteBySubscriberID(Connection conn, String subscriberID, String type)
	{
		int id = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = "
		+ sqlString(subscriberID); 
		
		if(type != null) {
			query = query+ " AND " + TYPE_COL + " = "+ sqlString(type); 
		}
		logger.info("RBT::query " + query);

		try
		{
			stmt = conn.createStatement();
			id = stmt.executeUpdate(query);
		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}

		return (id > 0);
	
	}
	public static RBTLoginUser[] getRBTLoginUsers(Connection conn, String type)
	{
		logger.info("RBT::inside getRBTLoginUsers");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		query = " SELECT * FROM " + TABLE_NAME;

		if (type != null)
			query += " WHERE "+ TYPE_COL +" = "+ sqlString(type);

		logger.info("RBT::query "+ query);

		ArrayList<RBTLoginUser> rbtLoginUserList = new ArrayList<RBTLoginUser>();
		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			while (results.next())
			{
				rbtLoginUserList.add(getRBTLoginUserFromRS(results));
			}

		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		
		if (rbtLoginUserList.size() > 0)
			return rbtLoginUserList.toArray(new RBTLoginUserImpl[0]);
		
		return null;
	}
	//Changes are done for handling the voldemort issues.
	public static RBTLoginUser[] getRBTLoginUsersByLimit(Connection conn,
			String type, String Limit, String initial) {
		logger.info("RBT::inside getRBTLoginUsersByLimit");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		if (type.contains(",")) {
			String typeArr[] = type.split(",");
			type = "";
			for (String typeStr : typeArr) {
				type += sqlString(typeStr) + ",";
			}
			type = type.substring(0, type.length() - 1);
		} else {
			type = sqlString(type);
		}
		query = " SELECT * FROM " + TABLE_NAME;
		if (type != null)
			query += " WHERE " + TYPE_COL + " IN ( " + type + " )";
		query += " LIMIT " + initial + " , " + Limit;

		logger.info("RBT::query " + query);

		ArrayList<RBTLoginUser> rbtLoginUserList = new ArrayList<RBTLoginUser>();
		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			while (results.next()) {
				rbtLoginUserList.add(getRBTLoginUserFromRS(results));
			}

		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (rbtLoginUserList.size() > 0)
			return rbtLoginUserList.toArray(new RBTLoginUserImpl[0]);

		return null;
	}

	public static RBTLoginUser[] getRBTLoginUsers(Connection conn,
			String userID, String password, String subscriberID, String type,
			boolean encryptPassword) {
		logger.info("RBT::inside getRBTLoginUser");

		Statement stmt = null;
		ResultSet results = null;

		if (encryptPassword) {
			if (password != null && password.length() > 0
					&& !password.equalsIgnoreCase("")) {
				password = Encryption128BitsAES.encryptAES128Bits(password);
			}
		}

		// Changes done for URL Encryption and Decryption
		if (RBTDBManager.isEncryptionModel()) {
			logger.info("Encryption Model is enabled");
			logger.info("before encrypting userId: " + userID
					+ " and password: " + password);
			userID = URLEncryptDecryptUtil.encryptUserNamePassword(userID);
			password = URLEncryptDecryptUtil.encryptUserNamePassword(password);
			logger.info("after encrypting userId: " + userID
					+ " and password: " + password);
		}
		// End of URL Encryption and Decryption
				
		StringBuilder whereClause = new StringBuilder();

		if (userID != null) {
			whereClause.append(USER_ID_COL).append(" = ")
					.append(sqlString(userID));
		}

		if (subscriberID != null) {
			if (whereClause.length() > 0)
				whereClause.append(" AND ");

			if (subscriberID.contains(",")) {
				String subscriberIDs[] = subscriberID.split(",");
				subscriberID = "";
				for (String subscriberId : subscriberIDs) {
					subscriberID += sqlString(subscriberId) + ",";
				}
				subscriberID = subscriberID.substring(0, subscriberID.length() - 1);
			} else {
				subscriberID = sqlString(subscriberID);
			}

			whereClause.append(SUBSCRIBER_ID_COL).append(" IN ( ")
					.append(subscriberID).append(") ");
		}

		if (password != null) {
			if (whereClause.length() > 0)
				whereClause.append(" AND ");
			whereClause.append(PASSWORD_COL).append(" = ")
					.append(sqlString(password));
		}

		if (whereClause.length() > 0 && type != null)
			whereClause.append(" AND ");

		if (type != null) {
			if (type.contains(",")) {
				String typeArr[] = type.split(",");
				type = "";
				for (String typeStr : typeArr) {
					type += sqlString(typeStr) + ",";
				}
				type = type.substring(0, type.length() - 1);
			} else {
				type = sqlString(type);
			}
			whereClause.append(TYPE_COL).append(" IN (").append(type)
					.append(") ");
		}

		String query = " SELECT * FROM " + TABLE_NAME;
		if (whereClause.length() > 0)
			query += " WHERE " + whereClause;

		logger.info("RBT::query " + query);
		ArrayList<RBTLoginUser> rbtLoginUserList = new ArrayList<RBTLoginUser>();
		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			while (results.next()) {
				rbtLoginUserList.add(getRBTLoginUserFromRS(results));
			}

		} catch (SQLException se) {
			logger.error("", se);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (rbtLoginUserList.size() > 0)
			return rbtLoginUserList.toArray(new RBTLoginUserImpl[0]);

		return null;
	}
	
	public static RBTLoginUser getRBTLoginUser(Connection conn, String userID, String password, String subscriberID, String type,boolean encryptPassword)
	{
		logger.info("RBT::inside getRBTLoginUser");

		Statement stmt = null;
		ResultSet results = null;

		RBTLoginUserImpl rbtLoginUser = null;
		if (encryptPassword)
		{
			if (password != null && password.length() > 0
					&& !password.equalsIgnoreCase(""))
			{
				password = Encryption128BitsAES.encryptAES128Bits(password);
			}
		}

		// Changes done for URL Encryption and Decryption
		if (RBTDBManager.isEncryptionModel()) {
			logger.info("Encryption Model is enabled");
			logger.info("before encrypting userId: "+userID+" and password: "+password);
			userID = URLEncryptDecryptUtil.encryptUserNamePassword(userID);
			password = URLEncryptDecryptUtil.encryptUserNamePassword(password);
			logger.info("after encrypting userId: "+userID+" and password: "+password);
		}
		// End of URL Encryption and Decryption
		
		StringBuilder whereClause = new StringBuilder();
		
		if (userID != null)
		{
			whereClause.append(USER_ID_COL).append(" = ").append(sqlString(userID));
		}

		if (subscriberID != null)
		{
			if (whereClause.length() > 0)
				whereClause.append(" AND ");

			whereClause.append(SUBSCRIBER_ID_COL).append(" = ").append(sqlString(subscriberID));
		}

		if (password != null)
		{
			if (whereClause.length() > 0)
				whereClause.append(" AND ");
			whereClause.append(PASSWORD_COL).append(" = ").append(sqlString(password));
		}

		if (whereClause.length() > 0 && type != null)
			whereClause.append(" AND ");

		if (type != null)
			whereClause.append(TYPE_COL).append(" = ").append(sqlString(type));

		String query = " SELECT * FROM " + TABLE_NAME;
		if (whereClause.length() > 0)
			query += " WHERE " + whereClause;

		logger.info("RBT::query "+ query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			if (results.next())
				rbtLoginUser = getRBTLoginUserFromRS(results);

		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if(encryptPassword && rbtLoginUser != null && null != rbtLoginUser.userInfo){
			HashMap<String, String> loginUserInfo=rbtLoginUser.userInfo;
			
			String authPass=loginUserInfo.get("AUTH_PASS");
			if(authPass!=null && authPass.length()>0 && !authPass.equalsIgnoreCase("")){
				authPass=Encryption128BitsAES.decryptAES128Bits(authPass);
				loginUserInfo.put("AUTH_PASS",authPass );
			}
			
			String superPass=loginUserInfo.get("SUPER_PASS");
			if(superPass!=null && superPass.length()>0 && !superPass.equalsIgnoreCase("")){
				superPass=Encryption128BitsAES.decryptAES128Bits(superPass);
				loginUserInfo.put("SUPER_PASS",superPass );
			}
			
			String passwordVal=rbtLoginUser.password();
			if(passwordVal!=null && passwordVal.length()>0 && !passwordVal.equalsIgnoreCase("")){
				passwordVal=Encryption128BitsAES.decryptAES128Bits(passwordVal);
				rbtLoginUser.password=passwordVal;
			}
		}
		return rbtLoginUser;
	}

	private static RBTLoginUserImpl getRBTLoginUserFromRS(ResultSet rs) throws SQLException
	{
		RBTResultSet resultSet = new RBTResultSet(rs);
		String userID = resultSet.getString(USER_ID_COL);
		String password = resultSet.getString(PASSWORD_COL);
		String subscriberID = resultSet.getString(SUBSCRIBER_ID_COL);
		String type = resultSet.getString(TYPE_COL);
		String userInfoXML = resultSet.getString(USER_INFO_COL);
		Date creationTime = resultSet.getTimestamp(CREATION_TIME_COL);
		Date updateTime = resultSet.getTimestamp(UPDATE_TIME_COL);
		// Changes done for URL Encryption and Decryption
		if (RBTDBManager.isEncryptionModel()) {
			logger.info("Encryption Model is enabled");
			logger.info("before encrypting userId: " + userID);
			userID = URLEncryptDecryptUtil.decryptUserNamePassword(userID);
			logger.info("after encrypting userId: " + userID);
			logger.info("before encrypting password: " + password);
			password = URLEncryptDecryptUtil.decryptUserNamePassword(password);
			logger.info("after encrypting password: " + password);
		}
		HashMap<String, String> userInfo = DBUtility.getAttributeMapFromXML(userInfoXML);

		return (new RBTLoginUserImpl(userID, password, subscriberID, type, userInfo, creationTime, updateTime));

	}

	public static boolean expireUserPIN(Connection conn, String userID, String type)
	{
		int id = -1;
		String query = null;
		Statement stmt = null;

		// Changes done for URL Encryption and Decryption
		if (RBTDBManager.isEncryptionModel()) {
			logger.info("Encryption Model is enabled");
			logger.info("before encrypting userId: " + userID);
			userID = URLEncryptDecryptUtil.encryptUserNamePassword(userID);
			logger.info("after encrypting userId: " + userID);
		}
		// End of URL Encryption and Decryption
				
		query = "UPDATE " + TABLE_NAME + " SET " + PASSWORD_COL + " = NULL" +
				" WHERE " + USER_ID_COL + " = " + sqlString(userID);

		if (type != null)
			query += " AND " + TYPE_COL + " = " + sqlString(type);

		query += " AND PASSWORD IS NOT NULL"; 

		logger.info("RBT::query " + query);
		try
		{
			stmt = conn.createStatement();
			id = stmt.executeUpdate(query);
		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}

		return (id > 0);
	}
}
