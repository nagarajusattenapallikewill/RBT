package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMap;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.subscriptions.ClipGui;

public class ClipsImpl extends RBTPrimitive implements Clips {
	private static Logger logger = Logger.getLogger(ClipsImpl.class);

	private static final String TABLE_NAME = "RBT_CLIPS";
	private static final String KEY_ID_COL = "CLIP_ID";
	private static final String CLIP_NAME_COL = "CLIP_NAME";
	private static final String CLIP_NAME_WAV_FILE_COL = "CLIP_NAME_WAV_FILE";
	private static final String CLIP_PREVIEW_WAV_FILE_COL = "CLIP_PREVIEW_WAV_FILE";
	private static final String CLIP_RBT_WAV_FILE_COL = "CLIP_RBT_WAV_FILE";
	private static final String CLIP_GRAMMAR_COL = "CLIP_GRAMMAR";
	private static final String CLIP_SMS_ALIAS_COL = "CLIP_SMS_ALIAS";
	private static final String ADD_TO_ACCESS_TABLE_COL = "ADD_TO_ACCESS_TABLE";
	private static final String CLIP_PROMO_ID_COL = "CLIP_PROMO_ID";
	private static final String CLASS_TYPE_COL = "CLASS_TYPE";
	private static final String CLIP_START_TIME_COL = "CLIP_START_TIME";
	private static final String CLIP_END_TIME_COL = "CLIP_END_TIME";
	private static final String SMS_START_TIME_COL = "SMS_START_TIME";
	private static final String ALBUM_COL = "ALBUM";
	private static final String LANGUAGE_COL = "LANGUAGE";
	private static final String SEQ_NAME = "RBT_CLIPS_SEQ";

	private static final String REFERENCE_TABLE = "RBT_CATEGORY_CLIP_MAP";
	private static final String CATEGORY_ID_COL = "CATEGORY_ID";
	private static final String CLIP_ID_COL = "CLIP_ID";
	private static final String CLIP_IN_LIST_COL = "CLIP_IN_LIST";
	private static final String CATEGORY_CLIP_INDEX_COL = "CATEGORY_CLIP_INDEX";
	private static final String PLAY_TIME_COL = "PLAY_TIME";
	private static final String CLIP_DEMO_WAV_FILE_COL = "CLIP_DEMO_WAV_FILE";
	private static final String ARTIST_COL = "ARTIST";
	private static final String CLIP_INFO_COL = "CLIP_INFO";
	private static final String UGC_SEQ_NAME = "RBT_UGC_CLIPS_SEQ";

	private int m_clipID;
	private String m_name;
	private String m_nameFile;
	private String m_previewFile;
	private String m_wavFile;
	private String m_grammar;
	private String m_alias;
	private String m_addAccess;
	private String m_clipInList;
	private String m_promoID;
	private String _classType;
	private Date m_startTime;
	private Date m_endTime;
	private Date m_smsTime;
	private String m_playTime;
	private String m_album;
	private String m_lang;
	private String m_demoFile;
	private String m_artist;
	private String m_clipInfo;
	private static String m_databaseType=getDBSelectionString();

	public ClipsImpl(int clipID, String name, String nameFile, String previewFile, String wavFile,
			String grammar, String alias, String addAccess, String promoID, String classType,
			Date startTime, Date endTime, Date smsTime, String album, String lang, String demoFile,
			String artist,String clipInfo) {
		m_clipID = clipID;
		m_name = name;
		m_nameFile = nameFile;
		m_previewFile = previewFile;
		m_wavFile = wavFile;
		m_grammar = grammar;
		m_alias = alias;
		m_addAccess = addAccess;
		m_promoID = promoID;
		_classType = classType;
		m_startTime = startTime;
		m_endTime = endTime;
		m_smsTime = smsTime;
		m_album = album;
		m_lang = lang;
		m_demoFile = demoFile;
		m_artist = artist;
		m_clipInfo = clipInfo;
	}

	public int id() {
		return m_clipID;
	}

	public String name() {
		return m_name;
	}

	public String nameFile() {
		return m_nameFile;
	}

	public String previewFile() {
		return m_previewFile;
	}

	public String wavFile() {
		return m_wavFile;
	}

	public String grammar() {
		return m_grammar;
	}

	public String alias() {
		return m_alias;
	}

	public boolean addAccess() {
		if(m_addAccess != null)
			return m_addAccess.equalsIgnoreCase("y");
		return false;
	}

	public String promoID() {
		return m_promoID;
	}

	public String classType() {
		return _classType;
	}

	public Date startTime() {
		return m_startTime;
	}

	public Date endTime() {
		return m_endTime;
	}

	public Date smsTime() {
		return m_smsTime;
	}

	public String album() {
		return m_album;
	}

	public String lang() {
		return m_lang;
	}

	public boolean clipInList() {
		if(m_clipInList != null)
			return m_clipInList.equalsIgnoreCase("y");
		else
			return false;
	}
	
	public void setClipInList(String clipInList) {
		m_clipInList = clipInList;
	}

	public String playTime() {
		return m_playTime;
	}
	
	public void setPlayTime(String playTime) {
		m_playTime = playTime;
	}

	public String demoFile() {
		return m_demoFile;
	}

	public String artist() {
		return m_artist;
	}
	public String clipInfo() {
		return m_clipInfo;
	}

	static Clips insert(Connection conn, String name, String nameFile, String previewFile,
			String wavFile, String grammar, String alias, String addAccess, String promoID,
			String classType, Date startTime, Date endTime, Date smsTime, String album,
			String lang, String demoFile, String artist, String clipInfo) {
		logger.info("RBT::inside insert");

		int id = -1;
		int mysqlId=-1;
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		String startDate = "SYSDATE";
		int clipSeq=-1;
		if(startTime != null){
			if (m_databaseType.equals(DB_SAPDB)) {
				startDate = sqlTime(startTime);
			} else if (m_databaseType.equals(DB_MYSQL)) {
				startDate = mySqlTime(startTime);
			}
			
		}
			
		String endDate=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			endDate = sqlTime(endTime);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			endDate = mySqlTime(endTime);
		}
		
		if(endTime == null){
			if (m_databaseType.equals(DB_SAPDB)) {
				endDate = "TO_DATE('2037/01/01', 'YYYY/MM/DD')";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				endDate = "DATE_FORMAT('2037/01/01','%Y/%m/%d')";
			}
			
		}
			
		String smsDate=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			smsDate = sqlTime(smsTime);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			smsDate = mySqlTime(smsTime);
		}
		
		if(smsTime == null)
			smsDate = startDate;

		ClipsImpl clips = null;
		
		query = "INSERT INTO " + TABLE_NAME + " ( " + KEY_ID_COL;
		query += ", " + CLIP_NAME_COL;
		query += ", " + CLIP_NAME_WAV_FILE_COL;
		query += ", " + CLIP_PREVIEW_WAV_FILE_COL;
		query += ", " + CLIP_RBT_WAV_FILE_COL;
		query += ", " + CLIP_GRAMMAR_COL;
		query += ", " + CLIP_SMS_ALIAS_COL;
		query += ", " + ADD_TO_ACCESS_TABLE_COL;
		query += ", " + CLIP_PROMO_ID_COL;
		query += ", " + CLASS_TYPE_COL;
		query += ", " + CLIP_START_TIME_COL;
		query += ", " + CLIP_END_TIME_COL;
		query += ", " + SMS_START_TIME_COL;
		query += ", " + ALBUM_COL;
		query += ", " + LANGUAGE_COL;
		query += ", " + CLIP_DEMO_WAV_FILE_COL;
		query += ", " + ARTIST_COL;
		query += ", " + CLIP_INFO_COL;
		query += ")";
		if (m_databaseType.equals(DB_SAPDB)) {
			query += " VALUES ( " + SEQ_NAME + ".NEXTVAL";
			
		} else if (m_databaseType.equals(DB_MYSQL)) {
			try{
				synchronized(ClipsImpl.class){
					clipSeq = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameter(com.onmobile.apps.ringbacktones.common.iRBTConstant.COMMON, "CLIP_SEQ").getValue());
					CacheManagerUtil.getParametersCacheManager().updateParameter(com.onmobile.apps.ringbacktones.common.iRBTConstant.COMMON , "CLIP_SEQ", (new Integer(clipSeq+1)).toString());
				}
				
			}
			catch(NumberFormatException nfe){
				logger.error("", nfe);
				return null;
			}
			query += " VALUES ( "+clipSeq+",";
		}
		query += " VALUES ( "+ "'" + name + "'" ;
		query += ", " + "'" + nameFile + "'";
		query += ", " + "'" + previewFile + "'";
		query += ", " + "'" + wavFile + "'";
		query += ", " + "'" + grammar + "'";
		query += ", " + "'" + alias + "'";
		query += ", " + "'" + addAccess.charAt(0) + "'";
		query += ", " + sqlString(promoID);
		query += ", " + sqlString(classType);
		query += ", " + startDate;
		query += ", " + endDate;
		query += ", " + smsDate;
		query += ", " + sqlString(album);
		query += ", " + sqlString(lang);
		query += ", " + sqlString(demoFile);
		query += ", " + sqlString(artist);
		query += ", " + sqlString(clipInfo);
		query += ")";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			if(stmt.executeUpdate(query) > 0){
				id = 0;
				mysqlId=0;
			}
			if(SEQ_NAME != null && m_databaseType.equals(DB_SAPDB)) {
				stmt = conn.createStatement();
				query = "SELECT " + SEQ_NAME + ".CURRVAL FROM DUAL";
				results = stmt.executeQuery(query);
				while (results.next())
					id = results.getInt(1);
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(id > 0) {
			logger.info("RBT::insertion to RBT_CLIPS table successful");
			clips = new ClipsImpl(id, name, nameFile, previewFile, wavFile, grammar, alias,
					addAccess, promoID, classType, startTime, endTime, smsTime, album, lang,
					demoFile, artist,clipInfo);
		}else if(m_databaseType.equals(DB_MYSQL) && mysqlId==0){
			logger.info("RBT::insertion to RBT_CLIPS table successful");
			clips = new ClipsImpl(id, name, nameFile, previewFile, wavFile, grammar, alias,
					addAccess, promoID, classType, startTime, endTime, smsTime, album, lang,
					demoFile, artist, clipInfo);
		}
		return clips;
	}

	/*static void getAllClipsForCaching(Connection conn, Hashtable clip, Hashtable promoMap) {
		logger.info("getClipsByName", "RBT::inside getAllClipsForCaching");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		int clipID = -1;
		String name = null;
		String wavFile = null;
		String grammar = null;
		String promoID = null;
		String classType = null;
		Date endTime = null;
		Date smsTime = null;
		String album = null;
		String lang = null;
		String artist = null;

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT CLIP_ID,CLIP_NAME,CLIP_RBT_WAV_FILE,CLIP_GRAMMAR,CLIP_PROMO_ID,CLASS_TYPE,CLIP_END_TIME,SMS_START_TIME,ALBUM,LANGUAGE,ARTIST  FROM "
				+ TABLE_NAME
				+ " WHERE "
				+ KEY_ID_COL
				+ " IN (SELECT DISTINCT( "
				+ CLIP_ID_COL
				+ " ) FROM "
				+ REFERENCE_TABLE
				+ " ) AND "
				+ CLIP_START_TIME_COL
				+ " <= "+SAPDB_SYSDATE+" "
				+ " AND " + CLIP_END_TIME_COL + " >= "+SAPDB_SYSDATE+" ";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT CLIP_ID,CLIP_NAME,CLIP_RBT_WAV_FILE,CLIP_GRAMMAR,CLIP_PROMO_ID,CLASS_TYPE,CLIP_END_TIME,SMS_START_TIME,ALBUM,LANGUAGE,ARTIST  FROM "
				+ TABLE_NAME
				+ " WHERE "
				+ KEY_ID_COL
				+ " IN (SELECT DISTINCT( "
				+ CLIP_ID_COL
				+ " ) FROM "
				+ REFERENCE_TABLE
				+ " ) AND "
				+ CLIP_START_TIME_COL
				+ " <= "+MYSQL_SYSDATE+" "
				+ " AND " + CLIP_END_TIME_COL + " >= "+MYSQL_SYSDATE+" ";
		}

		logger.info("getAllClipsForCaching", "RBT::query " + query);

		try {
			logger.info("getAllClipsForCaching", "RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next()) {
				clipID = results.getInt(KEY_ID_COL);
				name = results.getString(CLIP_NAME_COL);
				wavFile = results.getString(CLIP_RBT_WAV_FILE_COL);
				grammar = results.getString(CLIP_GRAMMAR_COL);
				promoID = results.getString(CLIP_PROMO_ID_COL);
				classType = results.getString(CLASS_TYPE_COL);
				endTime = results.getTimestamp(CLIP_END_TIME_COL);
				smsTime = results.getTimestamp(SMS_START_TIME_COL);
				album = results.getString(ALBUM_COL);
				lang = results.getString(LANGUAGE_COL);
				artist = results.getString(ARTIST_COL);
				ClipMinimal clipMinimal = new ClipMinimal(clipID, promoID, name, wavFile, grammar,
						classType, smsTime, endTime, album, lang, artist, null);
				if(promoID != null) {
					promoMap.put(promoID.toLowerCase(), "" + clipID);
				}
				clip.put("" + clipID, clipMinimal);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getAllClipsForCaching", "RBT::" + getStackTrace(se));
			return;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getAllClipsForCaching", "RBT::" + getStackTrace(e));
			}
		}
	}*/

	static void getAllClipsForCachingGui(Connection conn, Hashtable clip, HashMap map) {
		logger.info("RBT::inside getAllClipsForCachingGUI");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		int clipID = -1;
		String name = null;
		String wavFile = null;
		String classType = null;
		String album = null;
		String artist = null;

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT " + KEY_ID_COL + "," + CLIP_NAME_COL + "," + CLIP_RBT_WAV_FILE_COL + ","
			+ CLASS_TYPE_COL + "," + ALBUM_COL + "," + ARTIST_COL + " FROM " + TABLE_NAME
			+ " WHERE " + KEY_ID_COL + " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM "
			+ REFERENCE_TABLE + " ) AND " + CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" " + " AND "
			+ CLIP_END_TIME_COL + " >= "+SAPDB_SYSDATE+" ";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT " + KEY_ID_COL + "," + CLIP_NAME_COL + "," + CLIP_RBT_WAV_FILE_COL + ","
			+ CLASS_TYPE_COL + "," + ALBUM_COL + "," + ARTIST_COL + " FROM " + TABLE_NAME
			+ " WHERE " + KEY_ID_COL + " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM "
			+ REFERENCE_TABLE + " ) AND " + CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" " + " AND "
			+ CLIP_END_TIME_COL + " >= "+MYSQL_SYSDATE+" ";
		}

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			long a = System.currentTimeMillis();
			results = new RBTResultSet(stmt.executeQuery(query));
			long b = System.currentTimeMillis();
			logger.info("Time taken for executing "
					+ query + " = " + (b - a));
			logger.info("Query executed succesfully");
			a = System.currentTimeMillis();
			while (results.next()) {

				clipID = results.getInt(KEY_ID_COL);
				name = results.getString(CLIP_NAME_COL);
				wavFile = results.getString(CLIP_RBT_WAV_FILE_COL);
				classType = results.getString(CLASS_TYPE_COL);
				album = results.getString(ALBUM_COL);
				artist = results.getString(ARTIST_COL);
				ClipGui clipMinimal = new ClipGui(clipID, name, wavFile, classType, album, artist);
				clip.put("" + clipID, clipMinimal);
				map.put(wavFile, Integer.toString(clipID));
			}
			b = System.currentTimeMillis();
			logger.info("Time taken for iterating through clips  = " + (b - a));
			logger.info("clips populated ");
		}
		catch (SQLException se) {
			logger.error("", se);
			return;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	static ClipMinimal insertWithID(Connection conn, int clipID, String name, String nameFile,
			String previewFile, String wavFile, String grammar, String alias, String addAccess,
			String promoID, String classType, Date startTime, Date endTime, Date smsTime,
			String album, String lang, String clipDemoWavFile, String artist,String clipInfo) {
		logger.info("RBT::inside insert");

		int id = -1;
		int mysqlId=-1;
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String startDate = "SYSDATE";
		if(startTime != null){
			if (m_databaseType.equals(DB_SAPDB)) {
				startDate = sqlTime(startTime);
			} else if (m_databaseType.equals(DB_MYSQL)) {
				startDate = mySqlTime(startTime);
			}
		}
			
		String endDate=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			endDate = sqlTime(endTime);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			endDate = mySqlTime(endTime);
		}
		
		if(endTime == null){
			if (m_databaseType.equals(DB_SAPDB)) {
				endDate = "TO_DATE('2037/01/01', 'YYYY/MM/DD')";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				endDate = "DATE_FORMAT('2037/01/01', '%Y/%m/%d')";
			}
		}
			
		String smsDate=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			smsDate = sqlTime(smsTime);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			smsDate = mySqlTime(smsTime);
		}
		if(smsTime == null)
			smsDate = startDate;

		ClipMinimal clips = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + KEY_ID_COL;
		query += ", " + CLIP_NAME_COL;
		query += ", " + CLIP_NAME_WAV_FILE_COL;
		query += ", " + CLIP_PREVIEW_WAV_FILE_COL;
		query += ", " + CLIP_RBT_WAV_FILE_COL;
		query += ", " + CLIP_GRAMMAR_COL;
		query += ", " + CLIP_SMS_ALIAS_COL;
		query += ", " + ADD_TO_ACCESS_TABLE_COL;
		query += ", " + CLIP_PROMO_ID_COL;
		query += ", " + CLASS_TYPE_COL;
		query += ", " + CLIP_START_TIME_COL;
		query += ", " + CLIP_END_TIME_COL;
		query += ", " + SMS_START_TIME_COL;
		query += ", " + ALBUM_COL;
		query += ", " + LANGUAGE_COL;
		query += ", " + CLIP_DEMO_WAV_FILE_COL;
		query += ", " + ARTIST_COL;
		query += ", " + CLIP_INFO_COL;
		query += ")";

		query += " VALUES ( " + clipID;
		query += ", " + "'" + name + "'";
		query += ", " + "'" + nameFile + "'";
		query += ", " + "'" + previewFile + "'";
		query += ", " + "'" + wavFile + "'";
		query += ", " + "'" + grammar + "'";
		query += ", " + "'" + alias + "'";
		query += ", " + "'" + addAccess.charAt(0) + "'";
		query += ", " + sqlString(promoID);
		query += ", " + sqlString(classType);
		query += ", " + startDate;
		query += ", " + endDate;
		query += ", " + smsDate;
		query += ", " + sqlString(album);
		query += ", " + sqlString(lang);
		query += ", " + sqlString(clipDemoWavFile);
		query += ", " + sqlString(artist);
		query += ", " + sqlString(clipInfo);
		query += ")";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			if(stmt.executeUpdate(query) > 0){
				id = 0;
				mysqlId=0;
			}
			if(SEQ_NAME != null && m_databaseType.equals(DB_SAPDB)) {
				stmt = conn.createStatement();
				query = "SELECT " + SEQ_NAME + ".CURRVAL FROM DUAL";
				results = stmt.executeQuery(query);
				while (results.next())
					id = results.getInt(1);
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(id > 0 &&  m_databaseType.equals(DB_SAPDB)) {
			logger.info("RBT::insertion to RBT_CLIPS table successful");
			clips = new ClipMinimal(clipID, promoID, name, wavFile, nameFile, previewFile,
					clipDemoWavFile, grammar, classType, smsTime, endTime, album, lang, artist,
					alias, clipInfo);
			/*clips = new ClipsImpl(clipID, name, nameFile, previewFile, wavFile, grammar, alias,
					addAccess, promoID, classType, startTime, endTime, smsTime, album, lang,
					clipDemoWavFile, artist);*/
		}else if(m_databaseType.equals(DB_MYSQL) && mysqlId==0){
			logger.info("RBT::insertion to RBT_CLIPS table successful");
			clips = new ClipMinimal(clipID, promoID, name, wavFile, nameFile, previewFile,
					clipDemoWavFile, grammar, classType, smsTime, endTime, album, lang, artist,
					alias, clipInfo);
			/*clips = new ClipsImpl(clipID, name, nameFile, previewFile, wavFile, grammar, alias,
					addAccess, promoID, classType, startTime, endTime, smsTime, album, lang,
					clipDemoWavFile, artist);*/
		}
		return clips;
	}

	static boolean update(Connection conn, int clipID, String name, String nameFile,
			String previewFile, String wavFile, String grammar, String alias, String addAccess,
			String promoID, String classType, Date startTime, Date endTime, Date smsTime,
			String album, String lang, String demoFile, String artist, String clipInfo) {
		logger.info("RBT::inside update");

		int n = -1;
		String query = null;
		Statement stmt = null;

		String startDate = "SYSDATE";
		if(startTime != null){
			if (m_databaseType.equals(DB_SAPDB)) {
				startDate = sqlTime(startTime);
			} else if (m_databaseType.equals(DB_MYSQL)) {
				startDate = mySqlTime(startTime);
			}
		}
			
		String endDate=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			endDate = sqlTime(endTime);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			endDate = mySqlTime(endTime);
		}
		
		if(endTime == null){
			if (m_databaseType.equals(DB_SAPDB)) {
				endDate = "TO_DATE('2037/01/01', 'YYYY/MM/DD')";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				endDate = "DATE_FORMAT('2037/01/01', '%Y/%m/%d')";
			}
		}	

		String smsDate=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			smsDate = sqlTime(smsTime);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			smsDate = mySqlTime(smsTime);
		}
		if(smsTime == null)
			smsDate = startDate;

		query = "UPDATE " + TABLE_NAME + " SET " + CLIP_NAME_COL + " = " + "'" + name + "'" + ", "
				+ CLIP_NAME_WAV_FILE_COL + " = " + "'" + nameFile + "'" + ", "
				+ CLIP_PREVIEW_WAV_FILE_COL + " = " + "'" + previewFile + "'" + ", "
				+ CLIP_RBT_WAV_FILE_COL + " = " + "'" + wavFile + "'" + ", " + CLIP_GRAMMAR_COL
				+ " = " + "'" + grammar + "'" + ", " + CLIP_SMS_ALIAS_COL + " = " + "'" + alias
				+ "'" + ", " + ADD_TO_ACCESS_TABLE_COL + " = " + "'" + addAccess + "'" + ", "
				+ CLIP_PROMO_ID_COL + " = " + sqlString(promoID) + ", " + CLASS_TYPE_COL + " = "
				+ sqlString(classType) + ", " + CLIP_START_TIME_COL + " = " + startDate + ", "
				+ CLIP_END_TIME_COL + " = " + endDate + ", " + SMS_START_TIME_COL + " = " + smsDate
				+ ", " + ALBUM_COL + " = " + sqlString(album) + ", " + LANGUAGE_COL + " = "
				+ sqlString(lang) + ", " + CLIP_DEMO_WAV_FILE_COL + " = " + "'" + demoFile + "' "
				+ ARTIST_COL + " = " + "'" + artist + "' " + " WHERE " + KEY_ID_COL + " = "
				+ clipID;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	/*static ClipMinimal[] getClipsByName(Connection conn, String start) {
		logger.info("getClipsByName", "RBT::inside getClipsByName");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;
		List clipsList = new ArrayList();

		if(start == null) {
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL
				+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
				+ " ) AND " + CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" " + " AND "
				+ CLIP_END_TIME_COL + " >= "+SAPDB_SYSDATE+" ";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL
				+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
				+ " ) AND " + CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" " + " AND "
				+ CLIP_END_TIME_COL + " >= "+MYSQL_SYSDATE+" ";
			}
		}
		if(start != null) {
			if(start.equals("[1-9]")) {
				if (m_databaseType.equals(DB_SAPDB)) {
					query = "SELECT * FROM " + TABLE_NAME + " WHERE ( LOWER(" + CLIP_NAME_COL
					+ ") LIKE '1%' OR LOWER(" + CLIP_NAME_COL + ") LIKE '2%' OR LOWER("
					+ CLIP_NAME_COL + ") LIKE '3%' OR LOWER(" + CLIP_NAME_COL
					+ ") LIKE '4%' OR LOWER(" + CLIP_NAME_COL + ") LIKE '5%' OR LOWER("
					+ CLIP_NAME_COL + ") LIKE '6%' OR LOWER(" + CLIP_NAME_COL
					+ ") LIKE '7%' OR LOWER(" + CLIP_NAME_COL + ") LIKE '8%' OR LOWER("
					+ CLIP_NAME_COL + ") LIKE '9%') AND " + KEY_ID_COL
					+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
					+ " ) AND " + CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" " + " AND "
					+ CLIP_END_TIME_COL + " >= "+SAPDB_SYSDATE+" ORDER BY " + CLIP_NAME_COL;
				} else if (m_databaseType.equals(DB_MYSQL)) {
					query = "SELECT * FROM " + TABLE_NAME + " WHERE ( LOWER(" + CLIP_NAME_COL
					+ ") LIKE '1%' OR LOWER(" + CLIP_NAME_COL + ") LIKE '2%' OR LOWER("
					+ CLIP_NAME_COL + ") LIKE '3%' OR LOWER(" + CLIP_NAME_COL
					+ ") LIKE '4%' OR LOWER(" + CLIP_NAME_COL + ") LIKE '5%' OR LOWER("
					+ CLIP_NAME_COL + ") LIKE '6%' OR LOWER(" + CLIP_NAME_COL
					+ ") LIKE '7%' OR LOWER(" + CLIP_NAME_COL + ") LIKE '8%' OR LOWER("
					+ CLIP_NAME_COL + ") LIKE '9%') AND " + KEY_ID_COL
					+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
					+ " ) AND " + CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" " + " AND "
					+ CLIP_END_TIME_COL + " >= "+MYSQL_SYSDATE+" ORDER BY " + CLIP_NAME_COL;
				}
			}
			else {
				if (m_databaseType.equals(DB_SAPDB)) {
					query = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER(" + CLIP_NAME_COL
					+ ") LIKE '" + start.toLowerCase() + "%' AND " + KEY_ID_COL
					+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
					+ " ) AND " + CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" " + " AND "
					+ CLIP_END_TIME_COL + " >= "+SAPDB_SYSDATE+" ORDER BY " + CLIP_NAME_COL;
				} else if (m_databaseType.equals(DB_MYSQL)) {
					query = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER(" + CLIP_NAME_COL
					+ ") LIKE '" + start.toLowerCase() + "%' AND " + KEY_ID_COL
					+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
					+ " ) AND " + CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" " + " AND "
					+ CLIP_END_TIME_COL + " >= "+MYSQL_SYSDATE+" ORDER BY " + CLIP_NAME_COL;
				}
			}
		}

		logger.info("getClipsByName", "RBT::query " + query);

		try {
			logger.info("getClipsByName", "RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
				ClipMinimal cM = new ClipMinimal(clips);
				clipsList.add(cM);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClipsByName", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClipsByName", "RBT::" + getStackTrace(e));
			}
		}
		if(clipsList.size() > 0) {
			logger.info("getClipsByName",
					"RBT::retrieving records from RBT_CLIPS successful");
			return (ClipMinimal[])clipsList.toArray(new ClipMinimal[0]);
		}
		else {
			logger.info("getClipsByName", "RBT::no records in RBT_CLIPS");
			return null;
		}
	}

	static Clips getClipByName(Connection conn, String name) {
		logger.info("getClipByName", "RBT::inside getClipByName");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER(" + CLIP_NAME_COL + ") = '"
				+ name.toLowerCase() + "'";

		logger.info("getClipByName", "RBT::query " + query);

		try {
			logger.info("getClipByName", "RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next()) {
				clips = getClipFromRS(results);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClipByName", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClipByName", "RBT::" + getStackTrace(e));
			}
		}
		return clips;
	}

	static int[] getClipIDsInCategory(Connection conn, int categoryID) {
		logger.info("getClipIDsInCategory", "RBT::inside getClipIDsInCategory");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int clipID;
		List clipIDsList = new ArrayList();

		query = "SELECT " + CLIP_ID_COL + " FROM " + REFERENCE_TABLE + " WHERE " + CATEGORY_ID_COL
				+ " = " + categoryID + " ORDER BY " + CATEGORY_CLIP_INDEX_COL;

		logger.info("getClipIDsInCategory", "RBT::query " + query);

		try {
			logger.info("getClipIDsInCategory", "RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clipID = results.getInt(CLIP_ID_COL);
				clipIDsList.add(new Integer(clipID));
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClipIDsInCategory", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClipIDsInCategory", "RBT::" + getStackTrace(e));
			}
		}
		if(clipIDsList.size() > 0) {
			logger.info("getClipIDsInCategory", "RBT::retrieving records successful");
			int[] clipIDs = new int[clipIDsList.size()];
			for(int i = 0; i < clipIDsList.size(); i++)
				clipIDs[i] = ((Integer)clipIDsList.get(i)).intValue();
			return clipIDs;
		}
		else {
			logger.info("getClipIDsInCategory", "RBT::no records");
			return null;
		}
	}*/

	static boolean removeCategoryClips(Connection conn, int categoryID) {
		logger.info("RBT::inside removeCategoryClips");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + REFERENCE_TABLE + " WHERE " + CATEGORY_ID_COL + " = " + categoryID;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();

		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	/*static Clips[] getAllActiveClips(Connection conn) {
		logger.info("getAllActiveClips", "RBT::inside getAllActiveClips");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;
		List clipsList = new ArrayList();

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" "
			+ " AND " + CLIP_END_TIME_COL + " >= "+SAPDB_SYSDATE+" ";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" "
			+ " AND " + CLIP_END_TIME_COL + " >= "+MYSQL_SYSDATE+" ";
		}

		logger.info("getAllActiveClips", "RBT::query " + query);

		try {
			logger.info("getAllActiveClips", "RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
				clipsList.add(clips);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getAllActiveClips", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getAllActiveClips", "RBT::" + getStackTrace(e));
			}
		}
		if(clipsList.size() > 0) {
			logger.info("getAllActiveClips",
					"RBT::retrieving records from RBT_CLIPS successful");
			return (Clips[])clipsList.toArray(new Clips[0]);
		}
		else {
			logger.info("getAllActiveClips", "RBT::no records in RBT_CLIPS");
			return null;
		}
	}*/

	static String[] getClipsNotInCategories(Connection conn, String categoryID) {
		logger.info("RBT::inside getClipsNotInCategories");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		List clipsList = new ArrayList();

		if(categoryID != null) {
			query = "SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE + " WHERE "
					+ CATEGORY_ID_COL + " NOT IN (" + categoryID + ")";
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "SELECT CLIP_ID,CLIP_NAME,ALBUM,ARTIST FROM " + TABLE_NAME + " WHERE "
				+ KEY_ID_COL + " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM "
				+ REFERENCE_TABLE + " WHERE " + CATEGORY_ID_COL + " NOT IN (" + categoryID
				+ ") ) AND " + CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" " + " AND "
				+ CLIP_END_TIME_COL + " >= "+SAPDB_SYSDATE+"";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = "SELECT CLIP_ID,CLIP_NAME,ALBUM,ARTIST FROM " + TABLE_NAME + " WHERE "
				+ KEY_ID_COL + " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM "
				+ REFERENCE_TABLE + " WHERE " + CATEGORY_ID_COL + " NOT IN (" + categoryID
				+ ") ) AND " + CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" " + " AND "
				+ CLIP_END_TIME_COL + " >= "+MYSQL_SYSDATE+"";
			}
		}

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next()) {
				clipsList.add(results.getString(KEY_ID_COL));
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(clipsList.size() > 0) {
			logger.info("RBT::retrieving records from " + REFERENCE_TABLE + " successful");
			return (String[])clipsList.toArray(new String[0]);
		}
		else {
			logger.info("RBT::no records in " + REFERENCE_TABLE);
			return null;
		}
	}

	/*static Clips[] getClipsInCategory(Connection conn, String categoryID) {
		logger.info("getClipsInCategory", "RBT::inside getClipsInCategory");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;
		List<Clips> clipsList = new ArrayList<Clips>();

		if(categoryID != null) {
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL
				+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
				+ " WHERE " + CATEGORY_ID_COL + " IN (" + categoryID + ") ) AND "
				+ CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" " + " AND " + CLIP_END_TIME_COL
				+ " >= "+SAPDB_SYSDATE+" ORDER BY " + CLIP_NAME_COL;
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL
				+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
				+ " WHERE " + CATEGORY_ID_COL + " IN (" + categoryID + ") ) AND "
				+ CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" " + " AND " + CLIP_END_TIME_COL
				+ " >= "+MYSQL_SYSDATE+" ORDER BY " + CLIP_NAME_COL;
			}
		}

		logger.info("getClipsInCategory", "RBT::query " + query);

		try {
			logger.info("getClipsInCategory", "RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
				clipsList.add(clips);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClipsInCategory", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClipsInCategory", "RBT::" + getStackTrace(e));
			}
		}
		if(clipsList.size() > 0) {
			logger.info("getClipsInCategory",
					"RBT::retrieving records from RBT_CLIPS successful");
			return clipsList.toArray(new Clips[0]);
		}
		else {
			logger.info("getClipsInCategory", "RBT::no records in RBT_CLIPS");
			return null;
		}
	}*/

	static Clips getClip(Connection conn, int clipID) {
		logger.info("RBT::inside getClip given the clip ID");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL + " = " + clipID;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return clips;
	}

	/*static Clips getClip(Connection conn, String clipName) {
		logger.info("RBT::inside getClip given the name");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_NAME_COL + " = " + "'" + clipName
				+ "'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClip", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClip", "RBT::" + getStackTrace(e));
			}
		}
		return clips;
	}*/

	/*static Clips getClipPromoID(Connection conn, String promotionID) {
		logger.info("getClipPromoID", "RBT::inside getClipPromoID");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER( " + CLIP_PROMO_ID_COL + " ) = "
				+ "'" + promotionID + "'";

		logger.info("getClipPromoID", "RBT::query " + query);

		try {
			logger.info("getClipPromoID", "RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClipPromoID", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClipPromoID", "RBT::" + getStackTrace(e));
			}
		}
		return clips;
	}*/

	/*static Clips getClipByPromoID(Connection conn, String promotionID) {
		logger.info("getClipByPromoID", "RBT::inside getClipByPromoID");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_PROMO_ID_COL + " = "
				+ sqlString(promotionID);

		logger.info("getClipByPromoID", "RBT::query " + query);

		try {
			logger.info("getClipByPromoID", "RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClipByPromoID", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClipByPromoID", "RBT::" + getStackTrace(e));
			}
		}
		return clips;
	}*/

	/*static Clips getClipRBT(Connection conn, String rbt) {
		try {
			return getClipRBT(conn, rbt, false);
		}
		catch (SQLException se) {
			return null;

		}
	}*/

	/*static Clips getClipRBT(Connection conn, String rbt, boolean bShouldExceptionBeThrown)
			throws SQLException {
		logger.info("getClipRBT", "RBT::inside getClipRBT");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_RBT_WAV_FILE_COL + " = " + "'"
				+ rbt + "'";

		logger.info("getClipRBT", "RBT::query " + query);

		try {
			logger.info("getClipRBT", "RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClipRBT", "RBT::" + getStackTrace(se));
			if(bShouldExceptionBeThrown) {
				throw se;
			}
			else {
				return null;
			}
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClipRBT", "RBT::" + getStackTrace(e));
			}
		}
		return clips;
	}*/

	static boolean remove(Connection conn, int id) {
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL + " = " + id;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static Clips insertCategoryClip(Connection conn, int categoryID, int clipID, String clipInList,
			int index, String playTime) {
		logger.info( "RBT::inside insertCategoryClip");

		int id = -1;
		String query = null;
		Statement stmt = null;

		query = "INSERT INTO " + REFERENCE_TABLE + " ( " + CATEGORY_ID_COL;
		query += ", " + CLIP_ID_COL;
		query += ", " + CLIP_IN_LIST_COL;
		query += ", " + CATEGORY_CLIP_INDEX_COL;
		query += ", " + PLAY_TIME_COL;
		query += ")";

		query += " VALUES ( " + sqlInt(categoryID);
		query += ", " + sqlInt(clipID);
		query += ", " + "'" + clipInList.charAt(0) + "'";
		query += ", " + index;
		query += ", " + sqlString(playTime);
		query += ")";

		logger.info( "RBT::query " + query);

		try {
			logger.info( "RBT::inside try block");
			stmt = conn.createStatement();
			if(stmt.executeUpdate(query) > 0)
				id = 0;
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(id == 0) {
			logger.info("RBT::insertion to RBT_CATEGORY_CLIP_MAP table successful");
			return getClip(conn, clipID);
		}
		else {
			logger.info("RBT::insertion to RBT_CATEGORY_CLIP_MAP table failed");
			return null;
		}
	}

	static boolean updateCategoryClip(Connection conn, int categoryID, int clipID,
			String clipInList, int index, String playTime) {
		logger.info("RBT::inside updateCategoryClip");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + REFERENCE_TABLE + " SET " + CLIP_IN_LIST_COL + " = " + "'" + clipInList
				+ "'" + "," + CATEGORY_CLIP_INDEX_COL + " = " + index + "," + PLAY_TIME_COL + " = "
				+ sqlString(playTime) + " WHERE " + CATEGORY_ID_COL + " = " + categoryID + " AND "
				+ CLIP_ID_COL + "=" + clipID;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean removeCategoryClip(Connection conn, int categoryID, int clipID) {
		logger.info("RBT::inside removeCategoryClip");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + REFERENCE_TABLE + " WHERE " + CATEGORY_ID_COL + " = " + categoryID
				+ " AND " + CLIP_ID_COL + "=" + clipID;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();

		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	/*static Clips[] getActiveCategoryClips(Connection conn, int categoryID, String chargeClasses) {
		try {
			return getActiveCategoryClips(conn, categoryID, chargeClasses, false);
		}
		catch (SQLException se) {
			return null;
		}
	}*/

	static Clips[] getActiveCategoryClipsCCC(Connection conn, int categoryID, String chargeClasses) {
		try {
			return getActiveCategoryClipsCCC(conn, categoryID, chargeClasses, false);
		}
		catch (SQLException se) {
			return null;
		}
	}

	static Clips[] getActiveCategoryClipsCCC(Connection conn, int categoryID, String chargeClasses,
			boolean bShouldExceptionBeThrown) throws SQLException {

		logger.info("RBT::inside getActiveCategoryClips");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String clipInList = null;
		String playTime = null;

		ClipsImpl clips = null;
		List<Clips> clipsList = new ArrayList<Clips>();

		/*
		 * query = "SELECT c.clip_id, c.clip_name, c.clip_name_wav_file,
		 * c.clip_preview_wav_file, " + "c.clip_rbt_wav_file, c.clip_grammar,
		 * c.clip_sms_alias, c.add_to_access_table, c.clip_promo_id, " +
		 * "c.class_type, c.clip_start_time, c.clip_end_time, c.sms_start_time,
		 * c.album, c.language, " + "c.clip_demo_wav_file, c.artist,
		 * cc.clip_in_list, cc.play_time FROM "+ TABLE_NAME+" c , " +
		 * "rbt_category_clip_map cc WHERE c.clip_id = cc.clip_id AND " +
		 * "cc.category_id = " + categoryID + " AND c.clip_start_time <= sysdate
		 * AND " + "c.clip_end_time >= sysdate";
		 */

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT c.clip_id, c.clip_name, c.clip_name_wav_file, c.clip_preview_wav_file, c.clip_rbt_wav_file, "
				+ " c.clip_grammar, c.clip_sms_alias, c.add_to_access_table, c.clip_promo_id, c.class_type, c.clip_start_time,"
				+ " c.clip_end_time, c.sms_start_time, c.album, c.language, c.clip_demo_wav_file, c.artist, c.clip_info, cc.clip_in_list, "
				+ " cc.play_time FROM "+ TABLE_NAME+" c , (select * from "+ REFERENCE_TABLE +" where category_id = "
				+ categoryID
				+ ") "
				+ " cc WHERE c.clip_id = cc.clip_id AND c.clip_start_time <= "+SAPDB_SYSDATE+" ";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT c.clip_id, c.clip_name, c.clip_name_wav_file, c.clip_preview_wav_file, c.clip_rbt_wav_file, "
				+ " c.clip_grammar, c.clip_sms_alias, c.add_to_access_table, c.clip_promo_id, c.class_type, c.clip_start_time,"
				+ " c.clip_end_time, c.sms_start_time, c.album, c.language, c.clip_demo_wav_file, c.artist, c.clip_info, cc.clip_in_list, "
				+ " cc.play_time FROM "+ TABLE_NAME+" c , (select * from "+REFERENCE_TABLE+" where category_id = "
				+ categoryID
				+ ") "
				+ " cc WHERE c.clip_id = cc.clip_id AND c.clip_start_time <= "+MYSQL_SYSDATE+" ";
		}

		if(chargeClasses != null) {
			chargeClasses = sqlString(chargeClasses);
			chargeClasses = Tools.findNReplaceAll(chargeClasses, ",", "','");
			query += " AND c.class_type in (" + chargeClasses + ") ";
		}
		query += " ORDER BY cc.category_clip_index";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clipInList = results.getString(CLIP_IN_LIST_COL);
				playTime = results.getString(PLAY_TIME_COL);

				clips = getClipFromRS(results);
				clips.m_clipInList = clipInList;
				clips.m_playTime = playTime;
				clipsList.add(clips);
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			if(bShouldExceptionBeThrown) {
				throw se;
			}
			else {
				return null;
			}
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(clipsList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_CLIPS table successful");
			return clipsList.toArray(new Clips[0]);
		}
		else {
			logger.info("RBT::no records in RBT_CLIPS");
			return null;
		}

	}

	static Clips[] getClipsToBeUpdated(Connection conn, int clipStartRange, int fetchSize) {
		logger.info("RBT::inside getClipsToBeUpdated");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;
		List<Clips> clipsList = new ArrayList<Clips>();

		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DAY_OF_YEAR, -1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(Calendar.HOUR_OF_DAY, 0);
		calendar2.set(Calendar.MINUTE, 0);
		calendar2.set(Calendar.SECOND, 0);
		calendar2.set(Calendar.MILLISECOND, 0);

		// query = "SELECT * FROM "+ TABLE_NAME +" WHERE "+ CLIP_PROMO_ID_COL +"
		// IS NOT NULL AND "+ SMS_START_TIME_COL +" IS NULL OR ("+
		// SMS_START_TIME_COL +" >= "+ sqlTime(calendar1.getTime()) +" AND "+
		// SMS_START_TIME_COL +" < "+ sqlTime(calendar2.getTime()) +")";
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL + " > " + clipStartRange
				+ " AND " + CLIP_PROMO_ID_COL + " IS NOT NULL ORDER BY " + KEY_ID_COL;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.setMaxRows(fetchSize);
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
				clipsList.add(clips);
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(clipsList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_CLIPS table successful");
			return clipsList.toArray(new Clips[0]);
		}
		else {
			logger.info("RBT::no records in RBT_CLIPS");
			return null;
		}
	}

	static int getCategoryIDFromClipMap(Connection conn, int clipID) {
		logger.info("RBT::inside getCategoryIdFromClipMap");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryId;

		query = "SELECT " + CATEGORY_ID_COL + " FROM " + REFERENCE_TABLE + " WHERE " + CLIP_ID_COL
				+ " = " + clipID + " AND " + CLIP_IN_LIST_COL + " = 'y'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);

			if(results.next()) {
				categoryId = results.getInt(CATEGORY_ID_COL);
				return categoryId;
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return 0;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return -1;
	}

	static boolean updateClipEndDateForTATA(Connection conn, int clipID, Date endTime) {
		logger.info("RBT::inside updateClipEndDateForTATA");

		int n = -1;
		String query = null;
		Statement stmt = null;

		Date endDate = null;
		Date smsDate = null;
		if(endTime != null) {
			endDate = endTime;
			smsDate = endTime;
		}
		else {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, -1);
			endDate = calendar.getTime();
			calendar.set(2037, 0, 1, 0, 0, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			smsDate = calendar.getTime();
		}

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "UPDATE " + TABLE_NAME + " SET " + CLIP_END_TIME_COL + " = " + sqlTime(endDate)
			+ ", " + SMS_START_TIME_COL + " = " + sqlTime(smsDate) + " WHERE " + KEY_ID_COL
			+ " = " + clipID;
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "UPDATE " + TABLE_NAME + " SET " + CLIP_END_TIME_COL + " = " + mySqlTime(endDate)
			+ ", " + SMS_START_TIME_COL + " = " + mySqlTime(smsDate) + " WHERE " + KEY_ID_COL
			+ " = " + clipID;
		}

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
			logger.info("RBT:: Update Count " + n);
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	private static ClipsImpl getClipFromRS(ResultSet rs) throws SQLException {
		RBTResultSet resultSet = new RBTResultSet(rs);
		if(resultSet != null) {
			int clipID = resultSet.getInt(KEY_ID_COL);
			String name = resultSet.getString(CLIP_NAME_COL);
			String nameFile = resultSet.getString(CLIP_NAME_WAV_FILE_COL);
			String previewFile = resultSet.getString(CLIP_PREVIEW_WAV_FILE_COL);
			String wavFile = resultSet.getString(CLIP_RBT_WAV_FILE_COL);
			String grammar = resultSet.getString(CLIP_GRAMMAR_COL);
			String alias = resultSet.getString(CLIP_SMS_ALIAS_COL);
			String addAccess = resultSet.getString(ADD_TO_ACCESS_TABLE_COL);
			String promoID = resultSet.getString(CLIP_PROMO_ID_COL);
			String classType = resultSet.getString(CLASS_TYPE_COL);
			Date startTime = resultSet.getTimestamp(CLIP_START_TIME_COL);
			Date endTime = resultSet.getTimestamp(CLIP_END_TIME_COL);
			Date smsTime = resultSet.getTimestamp(SMS_START_TIME_COL);
			String album = resultSet.getString(ALBUM_COL);
			String lang = resultSet.getString(LANGUAGE_COL);
			String demoFile = resultSet.getString(CLIP_DEMO_WAV_FILE_COL);
			String artist = resultSet.getString(ARTIST_COL);
			String clipInfo = resultSet.getString(CLIP_INFO_COL);

			return (new ClipsImpl(clipID, name, nameFile, previewFile, wavFile, grammar, alias,
					addAccess, promoID, classType, startTime, endTime, smsTime, album, lang,
					demoFile, artist,clipInfo));
		}
		return null;
	}

	static Clips addUGC(Connection conn, String subID, String regionName, String classType,
			String clipName) {
		logger.info("RBT::inside insert");

		int id = -1;
		int clipID = -1;
		String promoID = null;
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		String startDate = "SYSDATE";
		String endDate = "TO_DATE('2004/01/01', 'YYYY/MM/DD')";
		String smsDate = "SYSDATE";
		if (m_databaseType.equals(DB_MYSQL)) {
			startDate = "SYSDATE()";
			endDate = "DATE_FOMRAT('2004/01/01', '%Y/%m/%d')";
			smsDate = "SYSDATE()";
		}
		
		// String clipName = null;
		String clipNameWav = null;
		String clipPreviewWav = null;
		String clipRBTWav = null;
		String demoFile = null;
		String artist = null;
		ClipsImpl clips = null;
		String chargeClass = "DEFAULT";
		if(classType != null)
			chargeClass = classType;

		query = "SELECT " + UGC_SEQ_NAME + ".NEXTVAL FROM DUAL";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
				clipID = results.getInt(1);
			if(clipID == -1)
				throw new Exception("UGC_CLIP_SEQUENCE returned next value as -1");

			promoID = "7" + clipID;
			// clipName = "ugc_" + promoID;
			clipNameWav = "rbt_ugc_" + promoID + "_name";
			clipPreviewWav = "rbt_ugc_" + promoID + "_preview";
			clipRBTWav = "rbt_ugc_" + promoID + "_rbt";

			query = "INSERT INTO " + TABLE_NAME + " ( " + KEY_ID_COL;
			query += ", " + CLIP_NAME_COL;
			query += ", " + CLIP_NAME_WAV_FILE_COL;
			query += ", " + CLIP_PREVIEW_WAV_FILE_COL;
			query += ", " + CLIP_RBT_WAV_FILE_COL;
			query += ", " + CLIP_GRAMMAR_COL;
			query += ", " + CLIP_SMS_ALIAS_COL;
			query += ", " + ADD_TO_ACCESS_TABLE_COL;
			query += ", " + CLIP_PROMO_ID_COL;
			query += ", " + CLASS_TYPE_COL;
			query += ", " + CLIP_START_TIME_COL;
			query += ", " + CLIP_END_TIME_COL;
			query += ", " + SMS_START_TIME_COL;
			query += ", " + ALBUM_COL;
			query += ", " + LANGUAGE_COL;
			query += ", " + CLIP_DEMO_WAV_FILE_COL;
			query += ", " + ARTIST_COL;
			query += ", " + CLIP_INFO_COL;
			query += ")";

			query += " VALUES ( " + clipID;
			query += ", " + "'" + clipName + "'";
			query += ", " + "'" + clipNameWav + "'";
			query += ", " + "'" + clipPreviewWav + "'";
			query += ", " + "'" + clipRBTWav + "'";
			query += ", " + sqlString("UGC");
			query += ", " + "'" + null + "'";
			query += ", " + "'y'";
			query += ", '" + promoID + "'";
			query += ", " + sqlString(chargeClass);
			query += ", " + startDate;
			query += ", " + endDate;
			query += ", " + smsDate;
			query += ", " + sqlString(subID);
			query += ", " + sqlString(regionName);
			query += ", " + null;
			query += ", " + null;
			query += ", " + null;
			query += ")";
			stmt = conn.createStatement();
			if(stmt.executeUpdate(query) > 0)
				id = 0;
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		catch (Exception e) {
			logger.error("", e);
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(id == 0) {
			logger.info("RBT::insertion to RBT_CLIPS table successful");
			clips = new ClipsImpl(clipID, clipName, clipNameWav, clipPreviewWav, clipRBTWav, "UGC",
					null, null, promoID, chargeClass, null, null, null, subID, regionName,
					demoFile, artist,null);
		}
		return clips;

	}

	/*public static Clips[] getClipsByAlbum(Connection conn, String subID) {
		Tools.logFatalError(_class, "RBT:: Enter into getClipsByAlbum");
		Clips[] clips = null;
		ArrayList<Clips> clipsList = new ArrayList<Clips>();

		Statement stmt = null;
		ResultSet results = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ALBUM_COL + " = "
				+ sqlString(subID);

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				Clips clip = getClipFromRS(results);
				clipsList.add(clip);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				if(stmt != null)
					stmt.close();
				if(results != null)
					results.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "RBT::" + getStackTrace(e));
			}
		}
		if(clipsList.size() > 0)
			clips = clipsList.toArray(new Clips[0]);

		return clips;
	}*/

	static Clips[] getLatestUGCClips(Connection conn, int count) {
		try {
			return getLatestUGCClips(conn, count, false);
		}
		catch (SQLException se) {
			return null;
		}
	}

	static Clips[] getLatestUGCClips(Connection conn, int count, boolean bShouldExceptionBeThrown)
			throws SQLException {
		logger.info("RBT::inside getLatestUGCClips");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		int clipID = -1;
		String name = null;
		String nameFile = null;
		String previewFile = null;
		String wavFile = null;
		String grammar = null;
		String alias = null;
		String addAccess = null;
		String promoID = null;
		String classType = null;
		Date startTime = null;
		Date endTime = null;
		Date smsTime = null;
		// String clipInList = null;
		// String playTime = null;
		String album = null;
		String lang = null;
		String demoFile = null;
		String artist = null;
		String clipInfo = null;

		ClipsImpl clips = null;
		List<Clips> clipsList = new ArrayList<Clips>();
		int n = 0;
		if(count <= 0)
			count = 20;

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT c.clip_id, c.clip_name, c.clip_name_wav_file, c.clip_preview_wav_file, c.clip_rbt_wav_file, c.clip_grammar, c.clip_sms_alias, c.add_to_access_table, c.clip_promo_id, c.class_type, c.clip_start_time, c.clip_end_time, c.sms_start_time, c.album, c.language, c.clip_demo_wav_file, c.artist, c.clip_info FROM "+ TABLE_NAME+" c  WHERE c.clip_grammar = 'UGC' AND c.clip_end_time >= "+SAPDB_SYSDATE+" ORDER BY c.clip_start_time desc";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT c.clip_id, c.clip_name, c.clip_name_wav_file, c.clip_preview_wav_file, c.clip_rbt_wav_file, c.clip_grammar, c.clip_sms_alias, c.add_to_access_table, c.clip_promo_id, c.class_type, c.clip_start_time, c.clip_end_time, c.sms_start_time, c.album, c.language, c.clip_demo_wav_file, c.artist, c.clip_info FROM "+ TABLE_NAME+" c  WHERE c.clip_grammar = 'UGC' AND c.clip_end_time >= "+MYSQL_SYSDATE+" ORDER BY c.clip_start_time desc";
		}

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next()) {
				clipID = results.getInt(KEY_ID_COL);
				name = results.getString(CLIP_NAME_COL);
				nameFile = results.getString(CLIP_NAME_WAV_FILE_COL);
				previewFile = results.getString(CLIP_PREVIEW_WAV_FILE_COL);
				wavFile = results.getString(CLIP_RBT_WAV_FILE_COL);
				grammar = results.getString(CLIP_GRAMMAR_COL);
				alias = results.getString(CLIP_SMS_ALIAS_COL);
				addAccess = results.getString(ADD_TO_ACCESS_TABLE_COL);
				promoID = results.getString(CLIP_PROMO_ID_COL);
				classType = results.getString(CLASS_TYPE_COL);
				startTime = results.getTimestamp(CLIP_START_TIME_COL);
				endTime = results.getTimestamp(CLIP_END_TIME_COL);
				smsTime = results.getTimestamp(SMS_START_TIME_COL);
				// clipInList = results.getString(CLIP_IN_LIST_COL);
				// playTime = results.getString(PLAY_TIME_COL);
				album = results.getString(ALBUM_COL);
				lang = results.getString(LANGUAGE_COL);
				demoFile = results.getString(CLIP_DEMO_WAV_FILE_COL);
				artist = results.getString(ARTIST_COL);
				clipInfo = results.getString(CLIP_INFO_COL);

				clips = new ClipsImpl(clipID, name, nameFile, previewFile, wavFile, grammar, alias,
						addAccess, promoID, classType, startTime, endTime, smsTime, album, lang,
						demoFile, artist,clipInfo);
				// clips.m_clipInList = clipInList;
				// clips.m_playTime = playTime;
				if(++n <= count)
					clipsList.add(clips);
				else
					break;
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			if(bShouldExceptionBeThrown) {
				throw se;
			}
			else {
				return null;
			}
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(clipsList.size() > 0) {
			logger.info("RBT::retrieving records from RBT_CLIPS table successful");
			return clipsList.toArray(new Clips[0]);
		}
		else {
			logger.info("RBT::no records in RBT_CLIPS");
			return null;
		}
	}

	public static boolean expireUGCClipsForPromoIDs(Connection conn, String promoID) {
		logger.info("RBT::inside expireUGCClipsForPromoIDs");

		int n = -1;
		String query = null;
		Statement stmt = null;

		String endDate = null;
		if (m_databaseType.equals(DB_SAPDB)) {
			endDate = "SYSDATE";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			endDate = "SYSDATE()";
		}
		

		query = "UPDATE " + TABLE_NAME + " SET " + CLIP_END_TIME_COL + " = " + endDate + " , "
				+ CLIP_SMS_ALIAS_COL + " = 'EXPIRE_SEL' WHERE " + CLIP_PROMO_ID_COL + " = '"
				+ promoID + "'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);

	}

	public static boolean expireUGCClipsOfCreator(Connection conn, String subID) {
		logger.info("RBT::inside expireUGCClipsOfCreator");

		int n = -1;
		String query = null;
		Statement stmt = null;

		String endDate=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			endDate = "SYSDATE";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			endDate = "SYSDATE()";
		}


		query = "UPDATE " + TABLE_NAME + " SET " + CLIP_END_TIME_COL + " = " + endDate + " , "
				+ CLIP_SMS_ALIAS_COL + " = 'EXPIRE_SEL'  WHERE " + ALBUM_COL + " = '" + subID + "'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);

	}

	public static boolean makeUGCClipLive(Connection conn, String promoID) {
		logger.info("RBT::inside makeUGCClipLive");

		int n = -1;
		String query = null;
		Statement stmt = null;

		String endDate = "TO_DATE('2037/01/01', 'YYYY/MM/DD')";

		query = "UPDATE " + TABLE_NAME + " SET " + CLIP_END_TIME_COL + " = " + endDate + " WHERE "
				+ CLIP_PROMO_ID_COL + " = '" + promoID + "'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);

	}

	public static boolean makeUGCClipSemiLive(Connection conn, String promoID) {
		logger.info("RBT::inside makeUGCClipSemiLive");

		int n = -1;
		String query = null;
		Statement stmt = null;

		String endDate = "TO_DATE('2004/01/02', 'YYYY/MM/DD')";

		query = "UPDATE " + TABLE_NAME + " SET " + CLIP_END_TIME_COL + " = " + endDate + " WHERE "
				+ CLIP_PROMO_ID_COL + " = '" + promoID + "'";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);

	}

	public static void fillCategoryClipMaps(Connection conn, String categoryID, int clipId,
			String clipInList, int clipIndex, String playTime) {
		logger.info("RBT::inside fillCategoryClipMaps");

		String query = null;
		Statement stmt = null;

		query = "INSERT INTO " + REFERENCE_TABLE + " ( " + CATEGORY_ID_COL;
		query += ", " + CLIP_ID_COL;
		query += ", " + CLIP_IN_LIST_COL;
		query += ", " + CATEGORY_CLIP_INDEX_COL;
		query += ", " + PLAY_TIME_COL;
		query += ")";

		query += " VALUES ( " + categoryID;
		query += ", " + clipId;
		query += ", " + "'" + clipInList + "'";
		query += ", " + clipIndex;
		query += ", null ";
		query += ")";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch (SQLException se) {
			logger.error("", se);
			return;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}

		return;
	}

	public static boolean clearCategoryClipMaps(Connection conn, String categoryID) {
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		query = "DELETE FROM " + REFERENCE_TABLE + " WHERE " + CATEGORY_ID_COL + " = " + categoryID;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n >= 0);

	}

	public static Clips[] getUGCFilesToTransfer(Connection conn, String endTimeStr) {
		logger.info("RBT:: Enter into getUGCFilesToTransfer");
		Clips[] clips = null;
		ArrayList<Clips> clipsList = new ArrayList<Clips>();

		Statement stmt = null;
		RBTResultSet results = null;

		int clipID = -1;
		String name = null;
		String nameFile = null;
		String previewFile = null;
		String wavFile = null;
		String grammar = null;
		String alias = null;
		String addAccess = null;
		String promoID = null;
		String classType = null;
		Date startTime = null;
		Date endTime = null;
		Date smsTime = null;
		String album = null;
		String lang = null;
		String demoFile = null;
		String artist = null;
		String clipInfo = null;
		
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_GRAMMAR_COL
				+ " = 'UGC' AND " + CLIP_END_TIME_COL + " = " + endTimeStr;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next()) {
				clipID = results.getInt(KEY_ID_COL);
				name = results.getString(CLIP_NAME_COL);
				nameFile = results.getString(CLIP_NAME_WAV_FILE_COL);
				previewFile = results.getString(CLIP_PREVIEW_WAV_FILE_COL);
				wavFile = results.getString(CLIP_RBT_WAV_FILE_COL);
				grammar = results.getString(CLIP_GRAMMAR_COL);
				alias = results.getString(CLIP_SMS_ALIAS_COL);
				addAccess = results.getString(ADD_TO_ACCESS_TABLE_COL);
				promoID = results.getString(CLIP_PROMO_ID_COL);
				classType = results.getString(CLASS_TYPE_COL);
				startTime = results.getTimestamp(CLIP_START_TIME_COL);
				endTime = results.getTimestamp(CLIP_END_TIME_COL);
				smsTime = results.getTimestamp(SMS_START_TIME_COL);
				album = results.getString(ALBUM_COL);
				lang = results.getString(LANGUAGE_COL);
				demoFile = results.getString(CLIP_DEMO_WAV_FILE_COL);
				artist = results.getString(ARTIST_COL);
				clipInfo = results.getString(CLIP_INFO_COL);

				Clips clip = new ClipsImpl(clipID, name, nameFile, previewFile, wavFile, grammar,
						alias, addAccess, promoID, classType, startTime, endTime, smsTime, album,
						lang, demoFile, artist, clipInfo);
				clipsList.add(clip);
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				if(stmt != null)
					stmt.close();
				if(results != null)
					results.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(clipsList.size() > 0)
			clips = clipsList.toArray(new Clips[0]);

		return clips;
	}

	static public HashMap getClipMapByStartTime(Connection conn, Date startDate, Date endDate) {
		logger.info("RBT::inside getClipMapByStartTime");

		HashMap categoryMap = new HashMap();

		Statement stmt = null;
		RBTResultSet results = null;
		String query=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT " + CATEGORY_ID_COL + ", " + CLIP_NAME_COL + " FROM " + TABLE_NAME
			+ " C, " + REFERENCE_TABLE + " CM WHERE C." + KEY_ID_COL + " = CM." + CLIP_ID_COL
			+ " AND " + CLIP_START_TIME_COL + " > " + sqlTime(startDate) + " AND "
			+ CLIP_START_TIME_COL + " <= " + sqlTime(endDate) + " ORDER BY " + CATEGORY_ID_COL;
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT " + CATEGORY_ID_COL + ", " + CLIP_NAME_COL + " FROM " + TABLE_NAME
			+ " C, " + REFERENCE_TABLE + " CM WHERE C." + KEY_ID_COL + " = CM." + CLIP_ID_COL
			+ " AND " + CLIP_START_TIME_COL + " > " + mySqlTime(startDate) + " AND "
			+ CLIP_START_TIME_COL + " <= " + mySqlTime(endDate) + " ORDER BY " + CATEGORY_ID_COL;
		}

		logger.info("RBT::query " + query);

		try {
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));

			String prevCategoryID = "";
			String categoryID = null;
			String clipName = null;
			while (results.next()) {
				categoryID = results.getString(CATEGORY_ID_COL);
				clipName = results.getString(CLIP_NAME_COL);
				if(categoryID.equalsIgnoreCase(prevCategoryID)) {
					ArrayList clipList = (ArrayList)categoryMap.get(categoryID);
					clipList.add(clipName);
				}
				else {
					ArrayList clipList = new ArrayList();
					clipList.add(clipName);
					categoryMap.put(categoryID, clipList);
					prevCategoryID = categoryID;
				}
			}

			if(categoryMap.size() > 0)
				return categoryMap;
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}

		return null;
	}
	
	static public HashMap<String, ArrayList<String>> getClipMapForArtistByStartTime(Connection conn, Date startDate, Date endDate)
	{
		logger.info("RBT::inside getClipMapForArtistByStartTime");

		Statement stmt = null;
		RBTResultSet results = null;
		String query = null;
		if (m_databaseType.equals(DB_SAPDB))
		{
			query = "SELECT " + ARTIST_COL + ", " + CLIP_NAME_COL + " FROM "
					+ TABLE_NAME + " WHERE " + ARTIST_COL + " IS NOT NULL AND "
					+ CLIP_START_TIME_COL + " > " + sqlTime(startDate)
					+ " AND " + CLIP_START_TIME_COL + " <= " + sqlTime(endDate)
					+ " ORDER BY " + ARTIST_COL;
		}
		else if (m_databaseType.equals(DB_MYSQL))
		{
			query = "SELECT " + ARTIST_COL + ", " + CLIP_NAME_COL
						+ " FROM " + TABLE_NAME + " WHERE " + ARTIST_COL
						+ " IS NOT NULL AND " + CLIP_START_TIME_COL + " > "
						+ mySqlTime(startDate) + " AND " + CLIP_START_TIME_COL
						+ " <= " + mySqlTime(endDate) + " ORDER BY "
						+ ARTIST_COL;
		}

		logger.info("RBT::query " + query);

		HashMap<String, ArrayList<String>> artistClipNameMap = new HashMap<String, ArrayList<String>>();
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			String prevArtist = "";
			String artist = null;
			String clipName = null;
			while (results.next())
			{
				artist = results.getString(ARTIST_COL).toLowerCase().trim();
				clipName = results.getString(CLIP_NAME_COL);
				if(artist.equalsIgnoreCase(prevArtist))
				{
					ArrayList<String> clipList = artistClipNameMap.get(artist);
					clipList.add(clipName);
				}
				else
				{
					ArrayList<String> clipList = new ArrayList<String>();
					clipList.add(clipName);
					artistClipNameMap.put(artist, clipList);
					prevArtist = artist;
				}
			}
			logger.info("RBT:: results processed and size is of artistCLipNameMap: "
							+ artistClipNameMap.size());
		}
		catch (SQLException se)
		{
			logger.error("", se);
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
		
		return artistClipNameMap;
	}
	
	public static boolean unmarkUGCExpiredClip(Connection conn, int clipID) {
		logger.info("RBT::inside unmarkUGCExpiredClip");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " + CLIP_SMS_ALIAS_COL + " = null  WHERE "
				+ CLIP_ID_COL + " = " + clipID;

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se) {
			logger.error("", se);
			return false;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		return (n > 0);

	}

	static public Clips[] getExpiredUGCClips(Connection conn) {
		logger.info("RBT:: Enter into getExpiredUGCClips");
		Clips[] clips = null;
		ArrayList<Clips> clipsList = new ArrayList<Clips>();

		Statement stmt = null;
		RBTResultSet results = null;

		int clipID = -1;
		String name = null;
		String nameFile = null;
		String previewFile = null;
		String wavFile = null;
		String grammar = null;
		String alias = null;
		String addAccess = null;
		String promoID = null;
		String classType = null;
		Date startTime = null;
		Date endTime = null;
		Date smsTime = null;
		String album = null;
		String lang = null;
		String demoFile = null;
		String artist = null;
		String clipInfo = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_SMS_ALIAS_COL
				+ " = 'EXPIRE_SEL' ";

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next()) {
				clipID = results.getInt(KEY_ID_COL);
				name = results.getString(CLIP_NAME_COL);
				nameFile = results.getString(CLIP_NAME_WAV_FILE_COL);
				previewFile = results.getString(CLIP_PREVIEW_WAV_FILE_COL);
				wavFile = results.getString(CLIP_RBT_WAV_FILE_COL);
				grammar = results.getString(CLIP_GRAMMAR_COL);
				alias = results.getString(CLIP_SMS_ALIAS_COL);
				addAccess = results.getString(ADD_TO_ACCESS_TABLE_COL);
				promoID = results.getString(CLIP_PROMO_ID_COL);
				classType = results.getString(CLASS_TYPE_COL);
				startTime = results.getTimestamp(CLIP_START_TIME_COL);
				endTime = results.getTimestamp(CLIP_END_TIME_COL);
				smsTime = results.getTimestamp(SMS_START_TIME_COL);
				album = results.getString(ALBUM_COL);
				lang = results.getString(LANGUAGE_COL);
				demoFile = results.getString(CLIP_DEMO_WAV_FILE_COL);
				artist = results.getString(ARTIST_COL);
				clipInfo = results.getString(CLIP_INFO_COL);

				Clips clip = new ClipsImpl(clipID, name, nameFile, previewFile, wavFile, grammar,
						alias, addAccess, promoID, classType, startTime, endTime, smsTime, album,
						lang, demoFile, artist,clipInfo);
				clipsList.add(clip);
			}
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				if(stmt != null)
					stmt.close();
				if(results != null)
					results.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		if(clipsList.size() > 0)
			clips = clipsList.toArray(new Clips[0]);

		return clips;
	}

	/*static ClipMinimal[] getClipsNotInCategories1(Connection conn, String categoryID) {
		logger.info("RBT::inside getClipsNotInCategories");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;
		List clipsList = new ArrayList();

		if(categoryID != null) {
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL
				+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
				+ " WHERE " + CATEGORY_ID_COL + " NOT IN (" + categoryID + ") ) AND "
				+ CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" " + " AND " + CLIP_END_TIME_COL
				+ " >= "+SAPDB_SYSDATE+"";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL
				+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
				+ " WHERE " + CATEGORY_ID_COL + " NOT IN (" + categoryID + ") ) AND "
				+ CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" " + " AND " + CLIP_END_TIME_COL
				+ " >= "+MYSQL_SYSDATE+"";
			}
		}

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
				ClipMinimal cM = new ClipMinimal(clips);
				clipsList.add(cM);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClipsNotInCategories", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClipsNotInCategories", "RBT::" + getStackTrace(e));
			}
		}
		if(clipsList.size() > 0) {
			logger.info("getClipsNotInCategories",
					"RBT::retrieving records from RBT_CLIPS successful");
			return (ClipMinimal[])clipsList.toArray(new ClipMinimal[0]);
		}
		else {
			logger.info("RBT::no records in RBT_CLIPS");
			return null;
		}
	}

	static Clips[] getClipsNotInCategories2(Connection conn, String categoryID) {
		logger.info("getClipsNotInCategories2", "RBT::inside getClipsNotInCategories");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		Clips clips = null;
		List clipsList = new ArrayList();

		if(categoryID != null) {
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL
				+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
				+ " WHERE " + CATEGORY_ID_COL + " NOT IN (" + categoryID + ") ) AND "
				+ CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" " + " AND " + CLIP_END_TIME_COL
				+ " >= "+SAPDB_SYSDATE+"";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_COL
				+ " IN (SELECT DISTINCT( " + CLIP_ID_COL + " ) FROM " + REFERENCE_TABLE
				+ " WHERE " + CATEGORY_ID_COL + " NOT IN (" + categoryID + ") ) AND "
				+ CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" " + " AND " + CLIP_END_TIME_COL
				+ " >= "+MYSQL_SYSDATE+"";
			}
		}

		logger.info("getClipsNotInCategories2", "RBT::query " + query);

		try {
			logger.info("getClipsNotInCategories2", "RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				clips = getClipFromRS(results);
				clipsList.add(clips);
			}
		}
		catch (SQLException se) {
			Tools.logFatalError(_class, "getClipsNotInCategories2", "RBT::" + getStackTrace(se));
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				Tools.logWarning(_class, "getClipsNotInCategories2", "RBT::" + getStackTrace(e));
			}
		}
		if(clipsList.size() > 0) {
			logger.info("getClipsNotInCategories2",
					"RBT::retrieving records from RBT_CLIPS successful");
			return (Clips[])clipsList.toArray(new Clips[0]);
		}
		else {
			logger.info("getClipsNotInCategories2", "RBT::no records in RBT_CLIPS");
			return null;
		}
	}*/

	public static ArrayList getClipDetails(Connection conn, int from, int to, String searchText,
			String searchOption, String sorter) {
		ArrayList clip = new ArrayList();
		logger.info("RBT::inside getClipDetails");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		int clipID = -1;
		String name = null;
		String wavFile = null;
		String classType = null;
		String album = null;
		String artist = null;
		boolean whereFlag = false;
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT " + KEY_ID_COL + "," + CLIP_NAME_COL + "," + CLIP_RBT_WAV_FILE_COL + ","
			+ CLASS_TYPE_COL + "," + ALBUM_COL + "," + ARTIST_COL + " FROM " + TABLE_NAME
			+ " WHERE " + CLIP_START_TIME_COL + " <= "+SAPDB_SYSDATE+" " + " AND " + CLIP_END_TIME_COL
			+ " >= "+SAPDB_SYSDATE+" ";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT " + KEY_ID_COL + "," + CLIP_NAME_COL + "," + CLIP_RBT_WAV_FILE_COL + ","
			+ CLASS_TYPE_COL + "," + ALBUM_COL + "," + ARTIST_COL + " FROM " + TABLE_NAME
			+ " WHERE " + CLIP_START_TIME_COL + " <= "+MYSQL_SYSDATE+" " + " AND " + CLIP_END_TIME_COL
			+ " >= "+MYSQL_SYSDATE+" ";
		}
		// WHERE " + KEY_ID_COL + " IN (SELECT DISTINCT( " + CLIP_ID_COL + " )
		// FROM " + REFERENCE_TABLE + " ) AND " + CLIP_START_TIME_COL + " <=
		// sysdate " + " AND " + CLIP_END_TIME_COL + " >= sysdate " ;
		if(searchOption != null && searchOption.equalsIgnoreCase("vcode") && searchText != null) {
			query = query + " AND LOWER(" + CLIP_RBT_WAV_FILE_COL + ") LIKE '%" + searchText + "%'";
			whereFlag = true;
		}
		if(searchOption != null && searchOption.equalsIgnoreCase("song") && searchText != null) {
			query = query + " AND LOWER(" + CLIP_NAME_COL + ") LIKE '%" + searchText + "%'";
			whereFlag = true;
		}
		if(searchOption != null && searchOption.equalsIgnoreCase("album") && searchText != null) {
			query = query + " AND LOWER(" + ALBUM_COL + ") LIKE '%" + searchText + "%'";
			whereFlag = true;
		}
		if(searchOption != null && searchOption.equalsIgnoreCase("artist") && searchText != null) {
			query = query + " AND LOWER(" + ARTIST_COL + ") LIKE '%" + searchText + "%'";
			whereFlag = true;
		}

		if(m_databaseType.equals(DB_SAPDB)) {
			if (whereFlag) {
				//query = query + " LIMIT <=" + to + " ";
				query = query + " AND ROWNUM <=" + to + " ";
			}else {
				query = query + " AND ROWNUM <=" + to + " ";
			}
		}else if (m_databaseType.equals(DB_MYSQL)) {
			if (whereFlag) {
				//query = query + " LIMIT <=" + to + " ";
				query = query + " LIMIT " +from+", "+ to + " ";
			}else {
				query = query +" LIMIT " +from+", "+ to + " ";
			}
		}
		

		// sorting query
		if(sorter != null && sorter.equalsIgnoreCase("vcode")) {
			query = query + " ORDER BY LOWER(" + CLIP_RBT_WAV_FILE_COL + ") ";
		}
		if(sorter != null && sorter.equalsIgnoreCase("song")) {
			query = query + " ORDER BY LOWER(" + CLIP_NAME_COL + ") ";
		}
		if(sorter != null && sorter.equalsIgnoreCase("album")) {
			query = query + " ORDER BY LOWER(" + ALBUM_COL + ") ";
		}
		if(sorter != null && sorter.equalsIgnoreCase("artist")) {
			query = query + " ORDER BY LOWER(" + ARTIST_COL + ") ";
		}
		// rownum limit

		logger.info("RBT::inside getClipDetails sORTING");

		try {
			logger.info("RBT::inside try block query=" + query);
			stmt = conn.createStatement();
			long a = System.currentTimeMillis();
			results = new RBTResultSet(stmt.executeQuery(query));
			long b = System.currentTimeMillis();
			logger.info("Time taken for executing " + query
					+ " = " + (b - a));
			a = System.currentTimeMillis();
			int k = 0;
			while (results.next()) {
				if(k >= from) {
					clipID = results.getInt(KEY_ID_COL);
					name = results.getString(CLIP_NAME_COL);
					wavFile = results.getString(CLIP_RBT_WAV_FILE_COL);
					classType = results.getString(CLASS_TYPE_COL);
					album = results.getString(ALBUM_COL);
					artist = results.getString(ARTIST_COL);
					ClipGui clipMinimal = new ClipGui(clipID, name, wavFile, classType, album,
							artist);
					clip.add(clipMinimal);
				}
				k++;
			}
			b = System.currentTimeMillis();
			logger.info("clips size  = " + clip.size());
			return clip;
		}
		catch (SQLException se) {
			logger.error("", se);
			return null;
		}
		finally {
			try {
				stmt.close();
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}

	}

	static void getClipMapCache(Connection conn, Hashtable<String, String> clipIDMap,
			Hashtable<String, ArrayList<ClipMap>> clipMap) {
		/*
		 * int id, String promo, String name, String wavFile, String grammar,
		 * String classType, Date smsTime, Date endTime, String album, String
		 * lang, String artist
		 */
		String query = null;
		/*if(m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT " + CLIP_ID_COL + "," + CLIP_PROMO_ID_COL + "," + CLIP_NAME_COL + ","
					+ CLIP_RBT_WAV_FILE_COL + "," + CLIP_GRAMMAR_COL + "," + CLASS_TYPE_COL + ","
					+ SMS_START_TIME_COL + "," + CLIP_END_TIME_COL + "," + ALBUM_COL + ","
					+ LANGUAGE_COL + "," + ARTIST_COL + ", " + CLIP_SMS_ALIAS_COL + " FROM "
					+ TABLE_NAME + " WHERE " + KEY_ID_COL + " IN (SELECT " + KEY_ID_COL + " FROM "
					+ REFERENCE_TABLE + ") AND " + CLIP_START_TIME_COL + " < " + SAPDB_SYSDATE
					+ " AND " + CLIP_END_TIME_COL + " >= " + SAPDB_SYSDATE + " AND "
					+ SMS_START_TIME_COL + " < " + SAPDB_SYSDATE + "";
		}
		else if(m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT " + CLIP_ID_COL + "," + CLIP_PROMO_ID_COL + "," + CLIP_NAME_COL + ","
					+ CLIP_RBT_WAV_FILE_COL + "," + CLIP_GRAMMAR_COL + "," + CLASS_TYPE_COL + ","
					+ SMS_START_TIME_COL + "," + CLIP_END_TIME_COL + "," + ALBUM_COL + ","
					+ LANGUAGE_COL + "," + ARTIST_COL + ", " + CLIP_SMS_ALIAS_COL + " FROM "
					+ TABLE_NAME + " WHERE " + KEY_ID_COL + " IN (SELECT " + KEY_ID_COL + " FROM "
					+ REFERENCE_TABLE + ") AND " + CLIP_START_TIME_COL + " < " + MYSQL_SYSDATE
					+ " AND " + CLIP_END_TIME_COL + " >= " + MYSQL_SYSDATE + " AND "
					+ SMS_START_TIME_COL + " < " + MYSQL_SYSDATE + "";
		}*/
		if(m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT CC." + CATEGORY_ID_COL + ", CC." + CLIP_ID_COL + ", CC."
					+ CLIP_IN_LIST_COL + ", CC." + CATEGORY_CLIP_INDEX_COL + ", CC."
					+ PLAY_TIME_COL + " FROM " + TABLE_NAME + " C, " + REFERENCE_TABLE
					+ " CC WHERE C." + KEY_ID_COL + " = CC." + KEY_ID_COL + " AND C."
					+ CLIP_START_TIME_COL + " < " + SAPDB_SYSDATE + " AND " + CLIP_END_TIME_COL
					+ " >= " + SAPDB_SYSDATE + " AND " + SMS_START_TIME_COL + " < " + SAPDB_SYSDATE;
		}
		else {
			query = "SELECT CC." + CATEGORY_ID_COL + ", CC." + CLIP_ID_COL + ", CC."
					+ CLIP_IN_LIST_COL + ", CC." + CATEGORY_CLIP_INDEX_COL + ", CC."
					+ PLAY_TIME_COL + " FROM " + TABLE_NAME + " C, " + REFERENCE_TABLE
					+ " CC WHERE C." + KEY_ID_COL + " = CC." + KEY_ID_COL + " AND C."
					+ CLIP_START_TIME_COL + " < " + MYSQL_SYSDATE + " AND " + CLIP_END_TIME_COL
					+ " >= " + MYSQL_SYSDATE + " AND " + SMS_START_TIME_COL + " < " + MYSQL_SYSDATE;
		}

		logger.info("RBT:: query is " + query);
		Statement stmt = null;
		RBTResultSet rs = null;

		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			while (rs.next()) {
				int clipID = rs.getInt(CLIP_ID_COL);
				int catID = rs.getInt(CATEGORY_ID_COL);
				int index = rs.getInt(CATEGORY_CLIP_INDEX_COL);
				String clipInListStr = rs.getString(CLIP_IN_LIST_COL);
				char clipInList = (clipInListStr == null) ? 'n' : clipInListStr.charAt(0);
				int playTime = 0;
				
				try
				{
					playTime = rs.getInt(PLAY_TIME_COL);
				}
				catch(Exception e)
				{
					playTime = 0;				
				}
				String strClipID = String.valueOf(clipID);
				String strCatID = String.valueOf(catID);
				
				ArrayList<ClipMap> thisClipList;
				if(clipMap.containsKey(strCatID))
					thisClipList = clipMap.get(strCatID);
				else
					thisClipList = new ArrayList<ClipMap>();

				thisClipList.add(new ClipMap(clipID, catID, index, clipInList, playTime));

				clipMap.put(strCatID, thisClipList);
				clipIDMap.put(strClipID, strCatID);
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch (Exception e) {

			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch (Exception e) {

			}
		}
	}
	

	static void getClipMinimalCache(Connection conn, Hashtable<String,String> promoIDMap, Hashtable<String, ClipMinimal> clipMinimalMap, Hashtable<String,String> clipWavFileMap) 
	{
		String query = null;

		query = "SELECT " + CLIP_ID_COL + "," + CLIP_PROMO_ID_COL + "," + CLIP_NAME_COL + ","
				+ CLIP_RBT_WAV_FILE_COL + "," + CLIP_NAME_WAV_FILE_COL + ","
				+ CLIP_PREVIEW_WAV_FILE_COL + "," + CLIP_DEMO_WAV_FILE_COL + "," + CLIP_GRAMMAR_COL
				+ "," + CLASS_TYPE_COL + "," + SMS_START_TIME_COL + "," + CLIP_END_TIME_COL + ","
				+ ALBUM_COL + "," + LANGUAGE_COL + "," + ARTIST_COL + ", " + CLIP_SMS_ALIAS_COL + ","
				+ CLIP_INFO_COL + " FROM " + TABLE_NAME;
		logger.info("RBT::query " + query);

		Statement stmt = null;
		RBTResultSet rs = null;

		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			while (rs.next()) {
				int id = rs.getInt(CLIP_ID_COL);
				String promo = rs.getString(CLIP_PROMO_ID_COL);
				String name = rs.getString(CLIP_NAME_COL);
				String wavFile = rs.getString(CLIP_RBT_WAV_FILE_COL);
				String nameFile = rs.getString(CLIP_NAME_WAV_FILE_COL);
				String previewFile = rs.getString(CLIP_PREVIEW_WAV_FILE_COL);
				String demoFile = rs.getString(CLIP_DEMO_WAV_FILE_COL);
				String grammar = rs.getString(CLIP_GRAMMAR_COL);
				String classType = rs.getString(CLASS_TYPE_COL);
				Date smsTime = rs.getTimestamp(SMS_START_TIME_COL);
				Date endTime = rs.getTimestamp(CLIP_END_TIME_COL);
				String album = rs.getString(ALBUM_COL);
				String lang = rs.getString(LANGUAGE_COL);
				String artist = rs.getString(ARTIST_COL);
				String smsAlias = rs.getString(CLIP_SMS_ALIAS_COL);
				String clipInfo = rs.getString(CLIP_INFO_COL);
				ClipMinimal temp = new ClipMinimal(id, promo, name, wavFile, nameFile, previewFile,
						demoFile, grammar, classType, smsTime, endTime, album, lang, artist,
						smsAlias,clipInfo);
				String idStr = String.valueOf(id);
				
				clipMinimalMap.put(idStr, temp);
				if(wavFile != null)
					clipWavFileMap.put(wavFile,idStr);
				if(promo != null)
					promoIDMap.put(promo.toLowerCase(), idStr);
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch (Exception e) {

			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch (Exception e) {

			}
		}
	}

	static Date getMaxStartDate(Connection conn) {
		Statement stmt = null;
		ResultSet rs = null;
		String query = "SELECT MAX(" + CLIP_START_TIME_COL + ") from " + TABLE_NAME;
		logger.info("RBT::query is " + query);
		Date date = null;

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if(rs.next())
				date = rs.getTimestamp(1);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		return date;
	}
	
	static boolean doesMapExist(Connection conn, int categoryID, int clipID) {
		String query = "SELECT " + CATEGORY_ID_COL + " FROM " + REFERENCE_TABLE + " WHERE "
				+ CATEGORY_ID_COL + " = " + categoryID + " AND " + KEY_ID_COL + " = " + clipID;
		
		logger.info("RBT:: query is " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		boolean retVal = false;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if(rs.next())
				retVal = true;
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch(Exception e) {
				
			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e) {
				
			}
		}
		return retVal;
	}
	
	static boolean doesMapExist(Connection conn, String categoryIDs, int clipID) {
		String query = "SELECT " + CATEGORY_ID_COL + " FROM " + REFERENCE_TABLE + " WHERE "
				+ CATEGORY_ID_COL + " in " + sqlString(categoryIDs) + " AND " + KEY_ID_COL + " = "
				+ clipID;
		
		logger.info("RBT:: query is " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		boolean retVal = false;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if(rs.next())
				retVal = true;
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch(Exception e) {
				
			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e) {
				
			}
		}
		return retVal;
	}

	static int getClipMinimalCacheForAttribute(Connection conn, int type, String attr,Hashtable<String,String> promoIDMap, Hashtable<String, ClipMinimal> clipMinimalMap, Hashtable<String,String> clipWavFileMap) 
	{
		String query = null;
		int result = -1;
		/*
		type = 0 for clip id
		type = 1 for clip wav file
		type = 2 for promo id
		*/
		

		query = "SELECT " + CLIP_ID_COL + "," + CLIP_PROMO_ID_COL + "," + CLIP_NAME_COL + ","
				+ CLIP_RBT_WAV_FILE_COL + "," + CLIP_NAME_WAV_FILE_COL + ","
				+ CLIP_PREVIEW_WAV_FILE_COL + "," + CLIP_DEMO_WAV_FILE_COL + "," + CLIP_GRAMMAR_COL
				+ "," + CLASS_TYPE_COL + "," + SMS_START_TIME_COL + "," + CLIP_END_TIME_COL + ","
				+ ALBUM_COL + "," + LANGUAGE_COL + "," + ARTIST_COL + ", " + CLIP_SMS_ALIAS_COL + ","
				+ CLIP_INFO_COL + " FROM " + TABLE_NAME;
		switch(type)
		{
			case 0:
				query = query + " WHERE "+CLIP_ID_COL + " = "+attr;
			break;
			case 1:
				query = query + " WHERE "+CLIP_RBT_WAV_FILE_COL + " = '"+attr+"'";
			break;
			case 2:
				query = query + " WHERE "+CLIP_PROMO_ID_COL + " = '"+attr+"'";
			break;
		
		}

		logger.info("RBT::query " + query);

		Statement stmt = null;
		RBTResultSet rs = null;

		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			if (rs.next()) {
				int id = rs.getInt(CLIP_ID_COL);
				String promo = rs.getString(CLIP_PROMO_ID_COL);
				String name = rs.getString(CLIP_NAME_COL);
				String wavFile = rs.getString(CLIP_RBT_WAV_FILE_COL);
				String nameFile = rs.getString(CLIP_NAME_WAV_FILE_COL);
				String previewFile = rs.getString(CLIP_PREVIEW_WAV_FILE_COL);
				String demoFile = rs.getString(CLIP_DEMO_WAV_FILE_COL);
				String grammar = rs.getString(CLIP_GRAMMAR_COL);
				String classType = rs.getString(CLASS_TYPE_COL);
				Date smsTime = rs.getTimestamp(SMS_START_TIME_COL);
				Date endTime = rs.getTimestamp(CLIP_END_TIME_COL);
				String album = rs.getString(ALBUM_COL);
				String lang = rs.getString(LANGUAGE_COL);
				String artist = rs.getString(ARTIST_COL);
				String smsAlias = rs.getString(CLIP_SMS_ALIAS_COL);
				String clipInfo = rs.getString(CLIP_INFO_COL);
				ClipMinimal temp = new ClipMinimal(id, promo, name, wavFile, nameFile, previewFile,
						demoFile, grammar, classType, smsTime, endTime, album, lang, artist,
						smsAlias,clipInfo);
				String idStr = String.valueOf(id);
				
				clipMinimalMap.put(idStr, temp);
				if(wavFile != null)
					clipWavFileMap.put(wavFile,idStr);
				if(promo != null)
					promoIDMap.put(promo.toLowerCase(), idStr);
				result = id;
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch (Exception e) {

			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch (Exception e) {

			}
		}
		return result;
	}

		static boolean getClipMapCacheForID(Connection conn, int id, Hashtable<String, String> clipIDMap,
			Hashtable<String, ArrayList<ClipMap>> clipMap) {
		String method = "getClipMapCacheForAttribute";
		logger.info("RBT::inside " + method);
		
		String query = null;
		boolean result = false;
		if(m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT CC." + CATEGORY_ID_COL + ", CC." + CLIP_ID_COL + ", CC."
					+ CLIP_IN_LIST_COL + ", CC." + CATEGORY_CLIP_INDEX_COL + ", CC."
					+ PLAY_TIME_COL + " FROM " + TABLE_NAME + " C, " + REFERENCE_TABLE
					+ " CC WHERE C." + KEY_ID_COL + " = "+ id + " AND C." + KEY_ID_COL + " = CC." + KEY_ID_COL + " AND C."
					+ CLIP_START_TIME_COL + " < " + SAPDB_SYSDATE + " AND " + CLIP_END_TIME_COL
					+ " >= " + SAPDB_SYSDATE + " AND " + SMS_START_TIME_COL + " < " + SAPDB_SYSDATE;
		}
		else {
			query = "SELECT CC." + CATEGORY_ID_COL + ", CC." + CLIP_ID_COL + ", CC."
					+ CLIP_IN_LIST_COL + ", CC." + CATEGORY_CLIP_INDEX_COL + ", CC."
					+ PLAY_TIME_COL + " FROM " + TABLE_NAME + " C, " + REFERENCE_TABLE
					+ " CC WHERE C." + KEY_ID_COL + " = "+ id + " AND C." + KEY_ID_COL + " = CC." + KEY_ID_COL + " AND C."
					+ CLIP_START_TIME_COL + " < " + MYSQL_SYSDATE + " AND " + CLIP_END_TIME_COL
					+ " >= " + MYSQL_SYSDATE + " AND " + SMS_START_TIME_COL + " < " + MYSQL_SYSDATE;
		}

		logger.info("RBT:: query is " + query);
		Statement stmt = null;
		RBTResultSet rs = null;

		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			while (rs.next()) {
				int clipID = rs.getInt(CLIP_ID_COL);
				int catID = rs.getInt(CATEGORY_ID_COL);
				int index = rs.getInt(CATEGORY_CLIP_INDEX_COL);
				String clipInListStr = rs.getString(CLIP_IN_LIST_COL);
				char clipInList = (clipInListStr == null) ? 'n' : clipInListStr.charAt(0);
				int playTime = 0;
				
				try
				{
					playTime = rs.getInt(PLAY_TIME_COL);
				}
				catch(Exception e)
				{
					playTime = 0;				
				}
				String strClipID = String.valueOf(clipID);
				String strCatID = String.valueOf(catID);
				
				ArrayList<ClipMap> thisClipList;
				if(clipMap.containsKey(strCatID))
					thisClipList = clipMap.get(strCatID);
				else
					thisClipList = new ArrayList<ClipMap>();

				thisClipList.add(new ClipMap(clipID, catID, index, clipInList, playTime));

				clipMap.put(strCatID, thisClipList);
				clipIDMap.put(strClipID, strCatID);
				result = true;
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch (Exception e) {

			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch (Exception e) {

			}
		}
		return result;
	}
	


}