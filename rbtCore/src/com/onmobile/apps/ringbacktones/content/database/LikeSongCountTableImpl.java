package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.webservice.client.beans.TopLikeSong;

public class LikeSongCountTableImpl extends RBTPrimitive implements
		iRBTConstant {
	private static Logger logger = Logger
			.getLogger(LikeSongCountTableImpl.class);

	public static final String TABLE_NAME = "RBT_LIKED_SONG_TABLE";
	private static final String CLIP_ID_COL = "CLIP_ID";
	private static final String COUNT_COL = "COUNT";
	private static final String LAST_MODIFIED_TIME_COL = "LAST_MODIFIED_TIME";
	private int clipId;
	private long count = 0;
	private static String m_databaseType = getDBSelectionString();

	public LikeSongCountTableImpl(int clipId, long count) {
		this.clipId = clipId;
		this.count = count;
	}

	public int getClipId() {
		return clipId;
	}

	public void setClipId(int clipId) {
		this.clipId = clipId;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public static boolean insertLikedSong(Connection conn, int clipId,
			long count) {
		String query = null;
		String startDate = "SYSDATE";
		if (m_databaseType.equals(DB_MYSQL)) {
			startDate = "SYSDATE()";
		}
		query = "INSERT INTO " + TABLE_NAME + " ( " + CLIP_ID_COL;
		query += ", " + COUNT_COL;
		query += ", " + LAST_MODIFIED_TIME_COL;
		query += ")";
		query += " VALUES ( " + clipId + ", 1";
		query += ", " + startDate + ")";
		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger.info("Insertion into RBT_SONG_LIKE_TABLE table is SUCCESS for clipId: "
					+ clipId);
			return true;
		}
		logger.info("Insertion into RBT_SONG_LIKE_TABLE is FAILED for clipId: "
				+ clipId);
		return false;
	}

	public static boolean updateLikedSongCount(Connection conn, int clipId,
			long count) {
		String query = null;
		String lastModifedDate = "SYSDATE";
		if (m_databaseType.equals(DB_MYSQL)) {
			lastModifedDate = "SYSDATE()";
		}
		query = "UPDATE " + TABLE_NAME + " SET " + COUNT_COL + " = " + count
				+ ", " + LAST_MODIFIED_TIME_COL + " = " + lastModifedDate;
		query += " WHERE CLIP_ID = " + clipId;
		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger.info("update RBT_SONG_LIKE_TABLE table is SUCCESS for clipId: "
					+ clipId);
			return true;
		}
		logger.info("update into RBT_SONG_LIKE_TABLE is FAILED for clipId: "
				+ clipId);
		return false;
	}

	public static long getLikedSongCount(Connection conn, int clipId) {
		String query = null;
		query = "SELECT " + COUNT_COL + " FROM " + TABLE_NAME;
		query += " WHERE CLIP_ID = " + clipId;
		logger.info("RBT::inside getCount");
		Statement stmt = null;
		ResultSet results = null;
		logger.info("RBT::query " + query);
		long count = 0;
		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next()) {
				count = results.getInt(COUNT_COL);
				return count;
			}
		} catch (SQLException se) {
			logger.error("", se);
			return 0;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return 0;
	}

	public static List<TopLikeSong> getLikedSongDetails(Connection conn,
			int limit) {
		String query = null;
		query = "SELECT " + CLIP_ID_COL + ", " + COUNT_COL + " FROM "
				+ TABLE_NAME;
		query += " ORDER BY " + COUNT_COL + " DESC";
		if (m_databaseType.equals(DB_SAPDB)) {
			query = query + " ROWNUM <=" + limit;
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = query + " LIMIT " + limit;
		}
		logger.info("RBT::inside getCount");
		Statement stmt = null;
		ResultSet results = null;
		logger.info("RBT::query " + query);
		List<TopLikeSong> topLikeSong = new ArrayList<TopLikeSong>();
		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				int clipId = results.getInt(CLIP_ID_COL);
				long count = results.getLong(COUNT_COL);
				TopLikeSong topLikeSongObj = new TopLikeSong(clipId, count);
				topLikeSong.add(topLikeSongObj);
			}
		} catch (SQLException se) {
			logger.error("", se);
			return null;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return topLikeSong;
	}

	private static int executeUpdateQuery(Connection conn, String query) {
		int updateCount = 0;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			updateCount = stmt.getUpdateCount();
		} catch (SQLException se) {
			logger.error("", se);
			return updateCount;
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return updateCount;
	}

}
