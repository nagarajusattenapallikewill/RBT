package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.webservice.client.beans.TopLikeSubscriberSong;

public class LikeSubscriberSongCountImpl extends RBTPrimitive implements
		iRBTConstant {
	private static Logger logger = Logger
			.getLogger(LikeSongCountTableImpl.class);

	public static final String TABLE_NAME = "RBT_SUBSCRIBER_LIKED_SONG_TABLE";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String CLIP_ID_COL = "CLIP_ID";
	private static final String CAT_ID_COL = "CATEGORY_ID";
	private static final String COUNT_COL = "COUNT";
	private static final String LAST_MODIFIED_TIME_COL = "LAST_MODIFIED_TIME";
	private int clipId;
	private String subscriberId;
	private int catId;
	private long count = 0;
	private static String m_databaseType = getDBSelectionString();

	public LikeSubscriberSongCountImpl(String subscriberId, int clipId,
			int catId, long count) {
		this.clipId = clipId;
		this.subscriberId = subscriberId;
		this.catId = catId;
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

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	public int getCatId() {
		return catId;
	}

	public void setCatId(int catId) {
		this.catId = catId;
	}

	public static boolean insertSubscriberLikedSongCount(Connection conn,
			String subscriberId, int clipId, int catId, long count) {
		String query = null;
		String startDate = "SYSDATE";
		if (m_databaseType.equals(DB_MYSQL)) {
			startDate = "SYSDATE()";
		}
		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL + ", "
				+ CLIP_ID_COL + ", " + CAT_ID_COL;
		query += ", " + COUNT_COL;
		query += ", " + LAST_MODIFIED_TIME_COL;
		query += ")";
		query += " VALUES ( " + sqlString(subscriberId) + ", " + clipId + ", "
				+ catId + ", 1";
		query += ", " + startDate + ")";
		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger.info("Insertion into RBT_SUBCRIBER_LIKED_SONG_TABLE table is SUCCESS for clipId: "
					+ clipId);
			return true;
		}
		logger.info("Insertion into RBT_SUBCRIBER_LIKED_SONG_TABLE is FAILED for clipId: "
				+ clipId);
		return false;
	}

	public static boolean updateSubscriberLikedSongCount(Connection conn,
			String subscriberId, int clipId, int catId, long count) {
		String query = null;
		String startDate = "SYSDATE";
		if (m_databaseType.equals(DB_MYSQL)) {
			startDate = "SYSDATE()";
		}
		query = "UPDATE " + TABLE_NAME + " SET " + COUNT_COL + " = " + count
				+ ", " + LAST_MODIFIED_TIME_COL + " = " + startDate;
		query += " WHERE CLIP_ID = " + clipId + " AND " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberId);
		if (catId != -1)
			query += " AND " + CAT_ID_COL + " = " + catId;
		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger.info("update RBT_SUBCRIBER_LIKED_SONG_TABLE table is SUCCESS for clipId: "
					+ clipId);
			return true;
		}
		logger.info("update into RBT_SUBCRIBER_LIKED_SONG_TABLE is FAILED for clipId: "
				+ clipId);
		return false;
	}

	public static long getLikedSubsciberSongCount(Connection conn,
			String subscriberId, int clipId, int catId) {
		String query = null;
		query = "SELECT " + COUNT_COL + " FROM " + TABLE_NAME;
		query += " WHERE CLIP_ID = " + clipId + " AND " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberId);
		if (catId != -1)
			query += " AND " + CAT_ID_COL + " = " + catId;
		query += " ORDER BY " + COUNT_COL + " DESC";
		if (m_databaseType.equals(DB_SAPDB)) {
			query = query + " ROWNUM <= 1";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = query + " LIMIT 1";
		}
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

	public static List<TopLikeSubscriberSong> getLikedSubscriberSongDetails(
			Connection conn, int limit, String subscriberID) {
		String query = null;
		query = "SELECT " + CLIP_ID_COL + ", " + COUNT_COL + " FROM "
				+ TABLE_NAME;
		if (null != subscriberID) {
			query += " WHERE SUBSCRIBER_ID= " + sqlString(subscriberID);
		}
		query += " GROUP BY CLIP_ID ORDER BY " + COUNT_COL + " DESC";
		if (m_databaseType.equals(DB_SAPDB)) {
			query = query + " ROWNUM <=" + limit;
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = query + " LIMIT " + limit;
		}
		logger.info("RBT::inside getCount");
		Statement stmt = null;
		ResultSet results = null;
		logger.info("RBT::query " + query);
		List<TopLikeSubscriberSong> topLikeSubSong = new ArrayList<TopLikeSubscriberSong>();
		try {
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				int clipId = results.getInt(CLIP_ID_COL);
				long count = results.getLong(COUNT_COL);
				TopLikeSubscriberSong topLikeSongObj = new TopLikeSubscriberSong(
						clipId, count);
				topLikeSubSong.add(topLikeSongObj);
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
		return topLikeSubSong;
	}

	public static boolean deleteSubscriberLikedSong(Connection conn,
			String subscriberId, int clipId, int catId) {
		String query = null;
		query = "DELETE FROM  " + TABLE_NAME;
		query += " WHERE ";
		if (clipId != -1)
			query += " CLIP_ID = " + clipId + " AND ";
		if (catId != -1)
			query += CAT_ID_COL + " = " + catId + " AND ";
		query += SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId);

		logger.info("Executing the query: " + query);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger.info("DELETE RBT_SUBCRIBER_LIKED_SONG_TABLE table is SUCCESS for clipId: "
					+ clipId);
			return true;
		}
		logger.info("DELETE into RBT_SUBCRIBER_LIKED_SONG_TABLE is FAILED for clipId: "
				+ clipId);
		return false;
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
