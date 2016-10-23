package com.onmobile.apps.ringbacktones.sync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipBoundary;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;

public class PopulateClipsToWAWDB {

	public static Logger basicLogger = Logger.getLogger(PopulateClipsToWAWDB.class);
	static int insertCount = 0;
	static int updateCount = 0;
	
	static int NO_OF_CLIP_PER_ITERATION = 10000;

	
	public static void updateClips(Connection connection) throws Exception{
		int noOfClipsPerIteration = NO_OF_CLIP_PER_ITERATION;
		try{
			noOfClipsPerIteration = Integer.parseInt(RBTContentJarParameters.getInstance()
				.getParameter("no_of_clips_per_iteration"));
		}
		catch(Exception e){
			noOfClipsPerIteration = NO_OF_CLIP_PER_ITERATION;
		}
		try{
			TreeSet<ClipBoundary> clipBoundaries = ClipsDAO.getClipBoundariesUsingBinaryAlg(noOfClipsPerIteration);
//			Iterator<ClipBoundary> itr = clipBoundaries.iterator();
			List<Clip> clipList = null;			
			for(ClipBoundary clipBoundary : clipBoundaries){
//				ClipBoundary clipBoundary = itr.next();
				clipList = ClipsDAO.getClipsInBetween(clipBoundary.getStartIndex(), clipBoundary.getEndIndex());
				if(clipList == null || clipList.size() == 0){
					continue;
				}
				for(Clip clip : clipList){
					if(updateClip(connection, clip) <= 0){
						basicLogger.info("[updateClips] Successfully not populated : " + clip.toString());
					}				
				}
			}
		}catch(SQLException sqle){
			basicLogger.error("[updateClips] Exception ", sqle);
			throw sqle;
		}
		catch(Exception e){
			basicLogger.error("[updateClips] Exception ", e);
			throw e;
		}
	}
	
	public static void main(String[] args) throws Exception{
		basicLogger.info("****************************************************************************************************");
		Connection connection = null;		
		try{
			connection = getConnectionForWapDB();
			if(connection == null){
				basicLogger.info("[main] connection not opened, please check the rbtcontentjar.properties file  ");
				return;
			}			
//			List<Clip> clipList = ClipsDAO.getAllClips();
//			for(Clip clip : clipList){				
//				if(updateClip(connection, clip) <= 0){
//					basicLogger.info("[main] Successfully not populated : " + clip.toString());
//				}
//			}
			
			updateClips(connection);
			
			basicLogger.info(updateCount + " number of records successfully updated");
			basicLogger.info(insertCount + " number of records successfully inserted");
			
		}
		catch(SQLException sqle){
			basicLogger.error("[main] Exception ", sqle);
		}
		catch(Exception e){
			basicLogger.error("[main] Exception ", e);
		}
		finally{
			if(connection != null){
				connection.close();
				connection = null;
			}
		}
	}
	
	private static int updateClip(Connection connection, Clip clip) throws Exception{
		PreparedStatement pstmt = null;
		PreparedStatement selectPstmt = null;
		ResultSet rs = null;
		int count = 0;
		String updateQuery = "UPDATE RBT_CLIPS SET CLIP_NAME = ?, CLIP_PREVIEW_WAV_FILE = ?, CLIP_RBT_WAV_FILE = ?, "; 
		updateQuery += "CLIP_PROMO_ID = ?, CLIP_START_TIME = ?, CLIP_END_TIME = ?, CLASS_TYPE = ?, ALBUM = ?, LANGUAGE = ?, ARTIST = ? ";
		updateQuery += " WHERE CLIP_ID = ?";
		
		String createQuery = "INSERT INTO RBT_CLIPS ( CLIP_NAME, CLIP_PREVIEW_WAV_FILE, CLIP_RBT_WAV_FILE, CLIP_PROMO_ID, ";
		createQuery += "CLIP_START_TIME, CLIP_END_TIME, CLASS_TYPE, ALBUM, LANGUAGE, ARTIST, CLIP_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		
		String selectQuery = "SELECT COUNT(*) FROM RBT_CLIPS WHERE CLIP_ID = ?";
		try{
			selectPstmt = connection.prepareStatement(selectQuery);
			
			int clipId = clip.getClipId();
			String clipName = clip.getClipName();
			String clipPreview = clip.getClipPreviewWavFile();
			String clipRBT = clip.getClipRbtWavFile();
			String promoId = clip.getClipPromoId();
			Timestamp clipStartTime = new Timestamp(clip.getClipStartTime().getTime());
			Timestamp clipEndTime = new Timestamp(clip.getClipEndTime().getTime());
			String classType = clip.getClassType();
			String album = clip.getAlbum();
			String language = clip.getLanguage();
			String artist = clip.getArtist();
			selectPstmt.setInt(1, clipId);
			rs = selectPstmt.executeQuery();
			if(rs.next() && rs.getInt(1) > 0){
				pstmt = connection.prepareStatement(updateQuery);
				updateCount++;
			}
			else{
				pstmt = connection.prepareStatement(createQuery);
				insertCount++;
			}
			
			pstmt.setString(1, clipName);
			pstmt.setString(2, clipPreview);
			pstmt.setString(3, clipRBT);
			pstmt.setString(4, promoId);
			pstmt.setTimestamp(5, clipStartTime);			
			pstmt.setTimestamp(6, clipEndTime);
			pstmt.setString(7, classType);
			pstmt.setString(8, album);
			pstmt.setString(9, language);
			pstmt.setString(10, artist);
			pstmt.setInt(11, clipId);
			
			count = pstmt.executeUpdate();
			basicLogger.info("Record successfully populated " + clip);
		}
		catch(SQLException sqle){
			basicLogger.info("[updateClip]" + sqle.getMessage());
			basicLogger.error("[updateClip] Exception ", sqle);
			sqle.printStackTrace();
		}
		finally{
			if(rs != null) rs.close();
			if(pstmt != null) pstmt.close();
			if(selectPstmt != null) selectPstmt.close();
		}
		return count;
	}
	
	private static Connection getConnectionForWapDB() throws Exception {
		Connection connection = null;
		try {
			String dbType = (String)RBTContentJarParameters.getInstance().getParameter("DB_TYPE");
			String dbURL = RBTContentJarParameters.getInstance().getParameter("DB_URL_WAP");
			basicLogger.info("[getConnectionForWapDB] DB Type : " + dbType);
			basicLogger.info("[getConnectionForWapDB] Wap DB URL : " + dbURL);
			if(dbType == null || dbURL == null || (dbURL = dbURL.trim()).length() <= 0) {
				connection = null;
				basicLogger.info("[getConnectionForWapDB] Missing value for the param DB_URL_WAP in the config file");
			}
			else if(dbType.equalsIgnoreCase("mysql")) {
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection("jdbc:mysql://" + dbURL.trim() + "&jdbcCompliantTruncation=false");
			} else if(dbType.equalsIgnoreCase("sapdb")) {
				Class.forName("com.sap.dbtech.jdbc.DriverSapDB");
				connection = DriverManager.getConnection("jdbc:sapdb://" + dbURL.trim());
			}
		} catch(Exception e) {
			basicLogger.info("[getConnectionForWapDB]" + e.getMessage());
			throw e;
		}
		return connection;
	}
}
