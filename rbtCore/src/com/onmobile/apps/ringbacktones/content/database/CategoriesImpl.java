package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.CategoryMap;
import com.onmobile.apps.ringbacktones.content.Categories;

public class CategoriesImpl extends RBTPrimitive implements Categories {
	private static Logger logger = Logger.getLogger(CategoriesImpl.class);
	
	private static final String TABLE_NAME = "RBT_CATEGORIES";
	private static final String KEY_ID_COL = "CATEGORY_ID";
	private static final String CATEGORY_NAME_COL = "CATEGORY_NAME";
	private static final String CATEGORY_NAME_WAV_FILE_COL = "CATEGORY_NAME_WAV_FILE";
	private static final String CATEGORY_PREVIEW_WAV_FILE_COL = "CATEGORY_PREVIEW_WAV_FILE";
	private static final String CATEGORY_GRAMMAR_COL = "CATEGORY_GRAMMAR";
	private static final String CATEGORY_TYPE_COL = "CATEGORY_TYPE";
	private static final String CATEGORY_INDEX_COL = "CATEGORY_INDEX";
	private static final String CATEGORY_ASK_MOBILE_NUMBER_COL = "CATEGORY_ASK_MOBILE_NUMBER";
	private static final String CATEGORY_GREETING_COL = "CATEGORY_GREETING";
	private static final String CATEGORY_START_TIME_COL = "CATEGORY_START_TIME";
	private static final String CATEGORY_END_TIME_COL = "CATEGORY_END_TIME";
	private static final String PARENT_CATEGORY_ID_COL = "PARENT_CATEGORY_ID";
	private static final String CLASS_TYPE_COL = "CLASS_TYPE";
	private static final String CATEGORY_PROMO_ID_COL = "CATEGORY_PROMO_ID";
	private static final String CIRCLE_ID_COL = "CIRCLE_ID";
	private static final String PREPAID_YES_COL = "PREPAID_YES";
	private static final String CATEGORY_LANGUAGE_COL = "CATEGORY_LANGUAGE";
	private static final String CATEGORY_SMS_ALIAS_COL = "CATEGORY_SMS_ALIAS";
	private static final String MM_NUMBER_COL = "MM_NUMBER";
	private static final String SEQ_NAME = "RBT_CATEGORIES_SEQ";
	/*private static final String SELECT_ALL_CATEGORIES_QRY="SELECT B.CATEGORY_ID, A.CATEGORY_NAME, B.CATEGORY_LANGUAGE, A.CATEGORY_NAME_WAV_FILE, A.CATEGORY_PREVIEW_WAV_FILE, A.CATEGORY_GRAMMAR " +
			"A.CATEGORY_TYPE, B.CATEGORY_INDEX, A.CATEGORY_AS__MOBILE_NUMBER, A.CATEGORY_GREETING, A.CATEGORY_START_TIME, A.CATEGORY_END_TIME " +
			"B.PARENT_CATEGORY_ID, A.CLASS_TYPE, A.CATEGORY_PROMO_ID, B.CIRCLE_ID, B.PREPAID_YES, A.CATEGORY_SMS_ALIAS, A.MM_NUMBER FROM RBT_CATEGORIES A, RBT_CATEGORY_CIRCLE_MAP B ";
	private static final String WHERE_CLAUSE_FOR_CIRCLE_MAP=" B.CATEGORY_ID=A.CATEGORY_ID ";*/
	
	private static final String REF_TABLE_NAME = "RBT_CATEGORY_CIRCLE_MAP";

	private int m_categoryID;
	private String m_name;
	private String m_nameFile;
	private String m_previewFile;
	private String m_grammar;
	private int m_type;
	private int m_index;
	private String m_askMobileNumber;
	private String m_greeting;
	private Date m_startTime;
	private Date m_endTime;
	private int m_parentID;
	private String m_classType;
	private String m_promoID;
	private String m_circleID;
	private char m_prepaidYes;
	private String m_alias;
	private String m_mmNumber;
	private String m_language;
	private HashMap<String, String> m_languageGrammarMap;
	private static String m_databaseType=getDBSelectionString();
	
	public static final Comparator INDEX_COMPARATOR = new Comparator()
	{
		public int compare(Object a1, Object a2) {
			return ((Categories)a1).index() - ((Categories)a2).index();
		}
	};
	public static final Comparator NAME_COMPARATOR = new Comparator()
	{
		public int compare(Object a1, Object a2) {
			return ((Categories)a1).name().compareToIgnoreCase(((Categories)a2).name());
		}
	};
	public static final Comparator CIRCLE_ID_INDEX_COMPARATOR = new Comparator()
	{
		public int compare(Object a1, Object a2) {
			int circleComp = ((Categories)a1).circleID().compareToIgnoreCase(((Categories)a2).circleID());
			int indexComp = ((Categories)a1).index() - ((Categories)a2).index();
			return circleComp + indexComp;
		}
	};

	public CategoriesImpl(int categoryID, String name, String nameFile,
			String previewFile, String grammar, int type, int index,
			String askMobileNumber, String greeting, Date startTime,
			Date endTime, int parentID, String classType, String promoID,
			String circleID, char prepaidYes, String alias, String mmNumber,
			String language, HashMap<String, String> languageGrammarMap)
	{
		m_categoryID = categoryID;
		m_name = name;
		m_nameFile = nameFile;
		m_previewFile = previewFile;
		m_grammar = grammar;
		m_type = type;
		m_index = index;
		m_askMobileNumber = askMobileNumber;
		m_greeting = greeting;
		m_startTime = startTime;
		m_endTime = endTime;
		m_parentID = parentID;
		m_classType = classType;
		m_promoID = promoID;
		m_circleID = circleID;
		m_prepaidYes = prepaidYes;
		m_alias = alias;
		m_mmNumber = mmNumber;
		m_language = language;
		m_languageGrammarMap = languageGrammarMap;
	}

	public int id() {
		return m_categoryID;
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

	public String grammar() {
		return m_grammar;
	}

	public int type() {
		return m_type;
	}

	public int index() {
		return m_index;
	}
	
	public String language() {
		return m_language;
	}

	public boolean askMobileNumber() {
		if(m_askMobileNumber != null)
			return m_askMobileNumber.equalsIgnoreCase("y");
		else
			return false;
	}

	public String greeting() {
		return m_greeting;
	}

	public Date startTime() {
		return m_startTime;
	}

	public Date endTime() {
		return m_endTime;
	}

	public int parentID() {
		return m_parentID;
	}

	public String classType() {
		return m_classType;
	}

	public String promoID() {
		return m_promoID;
	}

	public String circleID() {
		return m_circleID;
	}

	public char prepaidYes() {
		return m_prepaidYes;
	}

	public String alias() {
		return m_alias;
	}

	public String mmNumber() {
		return m_mmNumber;
	}
	
	public HashMap<String, String> languageGrammarMap() {
		return m_languageGrammarMap;
	}

	public String date(Date date) {
		
		DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		return sqlTimeFormat.format(date);
	}
	
	static Categories insertWithId(Connection conn, int categoryID, String name, String nameFile,
			String previewFile, String grammar, int type, int index, String askMobileNumber,
			String greeting, Date startTime, Date endTime, int parentID, String classType,
			String promoID, String circleID, char prepaidYes, String alias, String mmNumber, String language) {
		logger.info("RBT::inside insertWithId");

		int id = -1;
		String query = null;
		String query2 = null;
		Statement stmt = null;
		Statement stmt2 = null;

		CategoriesImpl categories = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + KEY_ID_COL;
		query += ", " + CATEGORY_NAME_COL;
		query += ", " + CATEGORY_NAME_WAV_FILE_COL;
		query += ", " + CATEGORY_PREVIEW_WAV_FILE_COL;
		query += ", " + CATEGORY_GRAMMAR_COL;
		query += ", " + CATEGORY_TYPE_COL;
		query += ", " + CATEGORY_ASK_MOBILE_NUMBER_COL;
		query += ", " + CATEGORY_GREETING_COL;
		query += ", " + CATEGORY_START_TIME_COL;
		query += ", " + CATEGORY_END_TIME_COL;
		query += ", " + CLASS_TYPE_COL;
		query += ", " + CATEGORY_PROMO_ID_COL;
		query += ", " + CATEGORY_SMS_ALIAS_COL;
		query += ", " + MM_NUMBER_COL;
		query += ")";
		
//		 inserting in to the rbt_category_circle_map 
		query2 = "INSERT INTO " + REF_TABLE_NAME + " ( " + KEY_ID_COL;
		query2 += ", " + CIRCLE_ID_COL;
		query2 += ", " + CATEGORY_INDEX_COL;
		query2 += ", " + CATEGORY_LANGUAGE_COL;
		query2 += ", " + PARENT_CATEGORY_ID_COL;
		query2 += ", " + PREPAID_YES_COL;
		query2+= ")";

		if (m_databaseType.equals(DB_SAPDB)) {
			query += " VALUES ( " + categoryID;
			query += ", " + "'" + name + "'";
			query += ", " + "'" + nameFile + "'";
			query += ", " + sqlString(previewFile);
			query += ", " + "'" + grammar + "'";
			query += ", " + type;
			query += ", " + "'" + askMobileNumber + "'";
			query += ", " + sqlString(greeting);
			query += ", " + sqlTime(startTime);
			query += ", " + sqlTime(endTime);
			query += ", " + sqlString(classType);
			query += ", " + sqlString(promoID);
			query += ", " + sqlString(alias);
			query += ", " + sqlString(mmNumber);
			query += ")";
			
			query2 += " VALUES ( " + SEQ_NAME + ".CURRVAL";
			query2 += ", " + "'" + circleID + "'";
			query2 += ", " + index;
			query2 += ", " + sqlString(language);
			query2 += ", " + parentID;
			query2 += ", " + "'" + prepaidYes + "'";
			query2 += ")";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query += " VALUES ( " + categoryID;
			query += ", " + "'" + name + "'";
			query += ", " + "'" + nameFile + "'";
			query += ", " + sqlString(previewFile);
			query += ", " + "'" + grammar + "'";
			query += ", " + type;
			query += ", " + "'" + askMobileNumber + "'";
			query += ", " + sqlString(greeting);
			query += ", " + mySqlTime(startTime);
			query += ", " + mySqlTime(endTime);
			query += ", " + sqlString(classType);
			query += ", " + sqlString(promoID);
			query += ", " + sqlString(alias);
			query += ", " + sqlString(mmNumber);
			query += ")";
			
			query2 += " VALUES ( " +categoryID ;
			query2 += ", " + "'" + circleID + "'";
			query2 += ", " + index;
			query2 += ", " + sqlString(language);
			query2 += ", " + parentID;
			query2 += ", " + "'" + prepaidYes + "'";
			query2 += ")";
		}

		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt2 = conn.createStatement();
			id = stmt.executeUpdate(query);
			stmt2.executeUpdate(query2);
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
			logger.info("RBT::insertion to RBT_CATEGORIES table successful");
			categories = new CategoriesImpl(categoryID, name, nameFile, previewFile, grammar, type,
					index, askMobileNumber, greeting, startTime, endTime, parentID, classType,
					promoID, circleID, prepaidYes, alias, mmNumber, language, null);
		}
		return categories;
	}

	/***
	 * added by sandeep
	 * */
	static public HashMap cacheCategories(Connection conn) {
		HashMap hm = new HashMap();
		logger.info("RBT::inside cachecategories");
		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;
		query = "SELECT " + KEY_ID_COL + " , " + CATEGORY_NAME_COL + " FROM " + TABLE_NAME;
		logger.info("RBT::query " + query);

		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			long a = System.currentTimeMillis();
			results = new RBTResultSet(stmt.executeQuery(query));
			long b = System.currentTimeMillis();
			logger.info("Time for execute query "
					+ (b - a));
			while (results.next()) {
				int cat_id = results.getInt(KEY_ID_COL);
				String catID = new Integer(cat_id).toString();
				String name = results.getString(CATEGORY_NAME_COL);
				hm.put(catID, name);
				//categoryNameIDMap.put(name,catID);
			}
			a = System.currentTimeMillis();
			logger.info("Time for iterating and caching " + (a - b));
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

		return hm;
	}

	private static CategoriesImpl getCategoryFromRS(ResultSet rs) throws SQLException {
		RBTResultSet resultSet = new RBTResultSet(rs);
		if(resultSet != null) {
			int categoryID = resultSet.getInt(KEY_ID_COL);
			String name = resultSet.getString(CATEGORY_NAME_COL);
			String nameFile = resultSet.getString(CATEGORY_NAME_WAV_FILE_COL);
			String previewFile = resultSet.getString(CATEGORY_PREVIEW_WAV_FILE_COL);
			String grammar = resultSet.getString(CATEGORY_GRAMMAR_COL);
			int type = resultSet.getInt(CATEGORY_TYPE_COL);
			int index = resultSet.getInt(CATEGORY_INDEX_COL);
			String askMobileNumber = resultSet.getString(CATEGORY_ASK_MOBILE_NUMBER_COL);
			String greeting = resultSet.getString(CATEGORY_GREETING_COL);
			Date startTime = resultSet.getTimestamp(CATEGORY_START_TIME_COL);
			Date endTime = resultSet.getTimestamp(CATEGORY_END_TIME_COL);
			int parentID = resultSet.getInt(PARENT_CATEGORY_ID_COL);
			String classType = resultSet.getString(CLASS_TYPE_COL);
			String promoID = resultSet.getString(CATEGORY_PROMO_ID_COL);
			String circleID = resultSet.getString(CIRCLE_ID_COL);
			String prepaidYes = resultSet.getString(PREPAID_YES_COL);
			String alias = resultSet.getString(CATEGORY_SMS_ALIAS_COL);
			String mmNumber = resultSet.getString(MM_NUMBER_COL);
			String language= resultSet.getString(CATEGORY_LANGUAGE_COL);

			return (new CategoriesImpl(categoryID, name, nameFile, previewFile, grammar, type,
					index, askMobileNumber, greeting, startTime, endTime, parentID, classType,
					promoID, circleID, prepaidYes.charAt(0), alias, mmNumber, language, null));
		}
		return null;
	}

	 static Integer getParentCategoryId(Connection conn , int catID)
	 {
		 logger.info("Getting Parent Category Id");
		 String query = null;
		 Statement stmt = null;
		 ResultSet results = null;
		 Integer parentID = null ;
		 String TABLE = "RBT_CATEGORY_CIRCLE_MAP";
		 
		 query = "SELECT PARENT_CATEGORY_ID FROM " + TABLE + " WHERE CATEGORY_ID " + " = " + catID;
		 logger.info("RBT::query " + query);

		 try {
			 logger.info("RBT::inside try block");
			 stmt = conn.createStatement();
			 results = stmt.executeQuery(query);
			 while (results.next()) {
				 parentID = results.getInt(1);
				 if(parentID!=null)
				 {
					 return parentID;
				 }
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
		return null;
		
			
	 }


	static Date getMaxStartDate(Connection conn) {
		Statement stmt = null;
		ResultSet rs = null;
		String query = "SELECT MAX(" + CATEGORY_START_TIME_COL + ") from " + TABLE_NAME;
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
	
	static void cacheCategoriesTable(Connection conn, Hashtable categoryTable) {
		String query = "SELECT * FROM " + TABLE_NAME;
		
		logger.info("query " + query);
		
		Statement stmt = null;
		RBTResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			while(rs.next()) {
				int id = rs.getInt(KEY_ID_COL);
				String name = rs.getString(CATEGORY_NAME_COL);
				String nameWavFile = rs.getString(CATEGORY_NAME_WAV_FILE_COL);
				String previewWavFile = rs.getString(CATEGORY_PREVIEW_WAV_FILE_COL);
				String grammar = rs.getString(CATEGORY_GRAMMAR_COL);
				int type = rs.getInt(CATEGORY_TYPE_COL);
				String askMobileNumber = rs.getString(CATEGORY_ASK_MOBILE_NUMBER_COL);
				String greeting = rs.getString(CATEGORY_GREETING_COL);
				Date startTime = rs.getTimestamp(CATEGORY_START_TIME_COL);
				Date endTime = rs.getTimestamp(CATEGORY_END_TIME_COL);
				String classType = rs.getString(CLASS_TYPE_COL);
				String promoID = rs.getString(CATEGORY_PROMO_ID_COL);
				String smsAlias = rs.getString(CATEGORY_SMS_ALIAS_COL);
				String mmNumber = rs.getString(MM_NUMBER_COL);
				
				categoryTable.put(new Integer(id), new Category(id, name, nameWavFile,
						previewWavFile, grammar, type, (askMobileNumber == null) ? 'n'
								: askMobileNumber.charAt(0), greeting, startTime, endTime,
						classType, promoID, smsAlias, mmNumber, null));
			}
		}
		catch(Exception e) {
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
	}
	
	static void cacheCategoryMapTable(Connection conn, Hashtable mapTable) {
		String query = "SELECT * FROM " + REF_TABLE_NAME;
		
		logger.info("query " + query);
		
		Statement stmt = null;
		RBTResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			while(rs.next()) {
				int id = rs.getInt(KEY_ID_COL);
				int index = rs.getInt(CATEGORY_INDEX_COL);
				int parentCatID = rs.getInt(PARENT_CATEGORY_ID_COL);
				String prepaidYesStr = rs.getString(PREPAID_YES_COL);
				String language = rs.getString(CATEGORY_LANGUAGE_COL);
				String circleID = rs.getString(CIRCLE_ID_COL);
				
				char prepaidYes=  'b';
				if(prepaidYesStr != null)
					prepaidYes = prepaidYesStr.charAt(0);
				
				String key = circleID + ":" + prepaidYes;
				ArrayList al;
				if(mapTable.containsKey(key))
					al = (ArrayList)mapTable.get(key);
				else
					al = new ArrayList();
				al.add(new CategoryMap(id, index, parentCatID, prepaidYes, language, circleID));
				
				mapTable.put(key, al);
			}
		}
		catch(Exception e) {
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
	}
	
	static boolean remove(Connection conn, int categoryID, String circleID, char prepaidYes) {
		String query = "DELETE FROM " + REF_TABLE_NAME + " WHERE " + KEY_ID_COL + " = "
				+ categoryID;
		if(circleID != null)
			query += " AND " + CIRCLE_ID_COL + " = " + sqlString(circleID);
		if(prepaidYes != 'b')
			query += " AND " + PREPAID_YES_COL + " = " + sqlString(String.valueOf(prepaidYes));
		
		logger.info("RBT::query is " + query);

		Statement stmt = null;
		int n = -1;

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(stmt != null)
					stmt.close();
			}
			catch (Exception e) {

			}
		}
		logger.info("RBT:: No. of rows afftcted " + n);
		return (n != -1);
	}
	
	 static Categories getActiveCategoriesForCatSearch(Connection conn,String smsAlias, String circleID, char prepaidYes){ 
         
		 logger.info("RBT::inside getActiveCategories by smsAlias"); 
         String query = null; 
         Statement stmt = null; 
         ResultSet results = null; 

         Categories categories = null; 
         List categoriesList = new ArrayList(); 

         if(m_databaseType.equalsIgnoreCase(DB_SAPDB)){
        	 query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CATEGORY_START_TIME_COL 
                         + " <= sysdate AND " + CATEGORY_END_TIME_COL + " >= sysdate AND " 
                         + CATEGORY_TYPE_COL + " > 0 "; 
         }else{
        	 query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CATEGORY_START_TIME_COL 
             + " <= sysdate() AND " + CATEGORY_END_TIME_COL + " >= sysdate() AND " 
             + CATEGORY_TYPE_COL + " > 0 "; 
    	 }         
         if(circleID != null) 
                 query += " AND " + CIRCLE_ID_COL + " = " + sqlString(circleID); 
         if(prepaidYes != 'b') 
                 query += " AND " + PREPAID_YES_COL + " = '" + prepaidYes + "'"; 
         if(smsAlias != null){ 
                 query += " AND " + " UPPER(" +CATEGORY_SMS_ALIAS_COL + ") = " +  sqlString(smsAlias) ; 
         } 
         else{ 
                 return null; 
         } 
         query += " ORDER BY " + CATEGORY_INDEX_COL; 

         logger.info("RBT::query " + query); 

         try { 
                 logger.info("RBT::inside try block"); 
                 stmt = conn.createStatement(); 
                 results = stmt.executeQuery(query); 
                 while (results.next()) { 
                         categories = getCategoryFromRS(results); 
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
         if(categories!=null) { 
                 logger.info("RBT::retrieving records from RBT_CATEGORIES successful"); 
                 return categories; 
         } 
         else { 
                 logger.info("RBT::no records in RBT_CATEGORIES"); 
                 return null; 
         } 
	 }
	 
	 public static Categories[] getChildCategories(Connection conn,
			String categoryID, String circleId, char perpadiYes) { 
		    
         Categories[] categories = null; 
         ArrayList categoryList = new ArrayList(); 
         Statement stmt = null; 
         ResultSet results = null; 
         String query = null;
         	
         if(m_databaseType.equalsIgnoreCase(DB_SAPDB)){
		     query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PARENT_CATEGORY_ID_COL + " =  " 
		     + categoryID+ " AND "+ CATEGORY_START_TIME_COL 
		     + " <= sysdate AND " + CATEGORY_END_TIME_COL + " >= sysdate AND " 
		     + CATEGORY_TYPE_COL + " > 0"; 
         }else{
        	 query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PARENT_CATEGORY_ID_COL + " =  " 
		     + categoryID+ " AND "+ CATEGORY_START_TIME_COL 
		     + " <= sysdate() AND " + CATEGORY_END_TIME_COL + " >= sysdate() AND " 
		     + CATEGORY_TYPE_COL + " > 0";
         }
         
                         if(circleId != null) 
                                 query += " AND " + CIRCLE_ID_COL + " = " + sqlString(circleId); 
                         if(perpadiYes != 'b') 
                                 query += " AND " + PREPAID_YES_COL + " = '" + perpadiYes + "'" 

                         +"  ORDER BY " + CATEGORY_INDEX_COL; 
         logger.info("RBT::query " + query); 
         try { 
                 logger.info("RBT:: Inside try block"); 
                 stmt = conn.createStatement(); 
                 results = stmt.executeQuery(query); 
                 int count=0; 
                 while (results.next()) { 

                         Categories category = (Categories)getCategoryFromRS(results); 
                         categoryList.add(category); 
                 } 
         } 
         catch (Exception e) { 
                 logger.error("", e);
         } 
         finally { 
                 try { 
                         stmt.close(); 
                 } 
                 catch (Exception e) { 
                	 logger.error("", e);
                 } 
         } 
         if(categoryList.size() > 0) 
                 categories = (Categories[])categoryList.toArray(new Categories[0]); 
         return categories; 
	 } 

	 static Categories[] getOverrideShuffles(Connection conn, String circleID, char prepaidYes) {
			logger.info("RBT::inside getBouquet");

			String query = null;
			Statement stmt = null;
			ResultSet results = null;

			Categories categories = null;
			List categoriesList = new ArrayList();

	         if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			 		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CATEGORY_TYPE_COL + " = 10 AND " + CATEGORY_END_TIME_COL + " > SYSDATE ";
			 else
					query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CATEGORY_TYPE_COL + " = 10 AND " + CATEGORY_END_TIME_COL + " > SYSDATE() ";
			 if(circleID != null)
				query += " AND " + CIRCLE_ID_COL + " = " + sqlString(circleID) ;
			if(prepaidYes != 'b')
				query += " AND " + PREPAID_YES_COL + " = '" + prepaidYes + "'";

			logger.info("RBT::query " + query);

			try {
				logger.info("RBT::inside try block");
				stmt = conn.createStatement();
				results = stmt.executeQuery(query);
				while (results.next()) {
					categories = getCategoryFromRS(results);
					categoriesList.add(categories);
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
			if(categoriesList.size() > 0) {
				logger.info("RBT::retrieving records from RBT_CATEGORIES successful");
				return (Categories[])categoriesList.toArray(new Categories[0]);
			}
			else {
				logger.info("RBT::no records in RBT_CATEGORIES");
				return null;
			}
		}

	 public static Categories getCategory(com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category)
	 {
		 Categories categoriesObj = new CategoriesImpl(category.getCategoryId(),
					category.getCategoryName(), category.getCategoryNameWavFile(),
					category.getCategoryPreviewWavFile(),
					category.getCategoryGrammar(), category.getCategoryTpe(), -1,
					String.valueOf(category.getCategoryAskMobileNumber()),
					category.getCategoryGreeting(),
					category.getCategoryStartTime(), category.getCategoryEndTime(),
					-1, category.getClassType(), category.getCategoryPromoId(),
					null, 'b', category.getCategorySmsAlias(),
					category.getMmNumber(), null, null);
		 
		 return categoriesObj;
	 }
}