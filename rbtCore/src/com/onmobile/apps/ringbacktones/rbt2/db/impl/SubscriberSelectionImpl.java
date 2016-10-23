package com.onmobile.apps.ringbacktones.rbt2.db.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberStatusImpl;
import com.onmobile.apps.ringbacktones.monitor.common.Constants;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedSubStatus;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.db.SubscriberSelection;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.constants.TableConstants;

@Repository(BeanConstant.SUBSCRIBER_SELECTION_IMPL)
@Lazy(value = true)
public class SubscriberSelectionImpl extends CommonOpsDBImpl implements SubscriberSelection, TableConstants {

	private static Logger logger = Logger.getLogger(SubscriberSelectionImpl.class);

	@Override
	public int getSubSelectionCountByUDPId(String udpId) {
		int rowCount = 0;
		Connection connection = RBTDBManager.getInstance().getConnection();
		Statement stmt = null;
		ResultSet resultSet = null;
		logger.info("RBT:: Inside getActiveSubscriberDownloadsCount");

		StringBuffer query = new StringBuffer();
		query.append("SELECT COUNT(*) FROM ").append(SUB_SELECTION_TABLE_NAME).append(" ").append("WHERE ")
				.append(UDP_ID_COL).append(" = ").append("'").append(udpId).append("'").append(" AND ")
				.append(SEL_STATUS_COL).append(" IN ").append("(").append("'W',").append("'A',").append("'N',")
				.append("'B',").append("'Z',").append("'G'").append(" )");

		logger.info("Executing query:: " + query.toString());

		try {
			stmt = connection.createStatement();
			resultSet = stmt.executeQuery(query.toString());
			if (resultSet.next())
				rowCount = resultSet.getInt(1);
		}
		catch (SQLException e) {
			logger.error("Exception Occured", e);
		}
		finally {
			closeStatementAndRS(stmt, resultSet, connection);
		}

		return rowCount;
	}

	private SubscriberStatus getSubStatus(ResultSet resultSet) throws SQLException {
		return SubscriberStatusImpl.getSubscriberStatusFromRS(resultSet);
	}

	private ExtendedSubStatus getExtendedSubStatus(ResultSet resultSet)
			throws SQLException, NumberFormatException, DataAccessException {
		ExtendedSubStatus extendedSubStatus = null;

		String subscriberID = resultSet.getString(SUBSCRIBER_ID_COL);
		String callerID = resultSet.getString(CALLER_ID_COL);
		int categoryID = resultSet.getInt(CATEGORY_ID_COL);
		String subscriberWavFile = resultSet.getString(SUBSCRIBER_WAV_FILE_COL);
		Date setTime = resultSet.getTimestamp(SET_TIME_COL);
		Date startTime = resultSet.getTimestamp(START_TIME_COL);
		Date endTime = resultSet.getTimestamp(END_TIME_COL);
		int status = resultSet.getInt(STATUS_COL);
		String classType = resultSet.getString(CLASS_TYPE_COL);
		String selectedBy = resultSet.getString(SELECTED_BY_COL);
		String selectionInfo = resultSet.getString(SELECTION_INFO_COL);
		Date nextChargingDate = resultSet.getTimestamp(NEXT_CHARGING_DATE_COL);
		String prepaid = resultSet.getString(PREPAID_YES_COL);
		int fromTime = resultSet.getInt(FROM_TIME_COL);
		int toTime = resultSet.getInt(TO_TIME_COL);
		String sel_status = resultSet.getString(SEL_STATUS_COL);
		String deSelectedBy = resultSet.getString(DESELECTED_BY_COL);
		String oldClassType = resultSet.getString(OLD_CLASS_TYPE_COL);
		int categoryType = resultSet.getInt(CATEGORY_TYPE_COL);
		char loopStatus = resultSet.getString(LOOP_STATUS_COL).charAt(0);
		String selInterval = resultSet.getString(SEL_INTERVAL_COL);
		String refID = resultSet.getString(INTERNAL_REF_ID_COL);
		String circleId = resultSet.getString(CIRCLE_ID_COL);
		int selType = resultSet.getInt(SEL_TYPE_COL);
		String udpId = resultSet.getString(UDP_ID_COL);
		String extraInfo = resultSet.getString(EXTRA_INFO_COL);

		extendedSubStatus = new ExtendedSubStatus();
		extendedSubStatus.setSubId(subscriberID);
		extendedSubStatus.setCallerId(callerID);
		extendedSubStatus.setCategoryID(categoryID);
		extendedSubStatus.setSubscriberFile(subscriberWavFile);
		extendedSubStatus.setTym(setTime);
		extendedSubStatus.setStartTime(startTime);
		extendedSubStatus.setEndTime(endTime);
		extendedSubStatus.setStatus(status);
		extendedSubStatus.setClassType(classType);
		extendedSubStatus.setSelectedBy(selectedBy);
		extendedSubStatus.setSelectionInfo(selectionInfo);
		extendedSubStatus.setNextChargingDate(nextChargingDate);
		extendedSubStatus.setPrepaidYes(prepaid);
		extendedSubStatus.setFromTime(fromTime);
		extendedSubStatus.setToTime(toTime);
		extendedSubStatus.setSelStatus(sel_status);
		extendedSubStatus.setDeselectedBy(deSelectedBy);
		extendedSubStatus.setOldClassType(oldClassType);
		extendedSubStatus.setCategoryType(categoryType);
		extendedSubStatus.setLoopStatus(loopStatus);
		extendedSubStatus.setSelType(selType);
		extendedSubStatus.setRefId(refID);
		extendedSubStatus.setExtraInfo(extraInfo);
		extendedSubStatus.setCircleId(circleId);
		extendedSubStatus.setSelInterval(selInterval);
		extendedSubStatus.setUdpId(udpId);

		return extendedSubStatus;
	}

	@Override
	public boolean deleteSubSelectionByUdpId(SubscriberStatus subscriberStatus, boolean isDirectDeact) {
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		boolean isDeleted = false;
		try {
			StringBuffer query = new StringBuffer();
			query.append("UPDATE ").append(SUB_SELECTION_TABLE_NAME).append(" ").append(" SET ").append(LOOP_STATUS_COL)
					.append(" = ").append(" x ").append(" , ").append(SEL_STATUS_COL).append(" = ");

			if (isDirectDeact)
				query.append(" X ");
			else
				query.append(" D ");

			query.append(" , ").append(END_TIME_COL).append(" = ").append("SYSDATE()").append(" , ")
					.append(DESELECTED_BY_COL).append(" = ").append(" WAP ").append(" WHERE ").append(UDP_ID_COL)
					.append(" = ").append("'").append(subscriberStatus.udpId()).append("'").append(" AND ")
					.append(SUBSCRIBER_ID_COL).append(" = ").append("'").append(subscriberStatus.subID()).append("'")
					.append(" AND ").append(CALLER_ID_COL).append(" = ").append("'").append(subscriberStatus.callerID())
					.append("'").append(" AND ").append(FROM_TIME_COL).append(" = ").append(subscriberStatus.fromTime())
					.append(" AND ").append(TO_TIME_COL).append(" = ").append(subscriberStatus.toTime());

			logger.info("Executing Query: " + query.toString());

			connection = RBTDBManager.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(query.toString());
			isDeleted = preparedStatement.execute();
		}
		catch (SQLException e) {
			logger.error("Exception Occured: " + e, e);
		}
		catch (Exception e) {
			logger.error("Exception Occured: " + e, e);
		}
		finally {
			closeStatementAndRS(preparedStatement, null, connection);
		}
		return isDeleted;
	}
	
	public static boolean deleteSubscriberSelections(Connection conn, String subscriberId) throws SQLException {
		String query = "DELETE FROM " + SUB_SELECTION_TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + subscriberId;

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);

		int count = conn.createStatement().executeUpdate(query);
		return count > 0;
	}

	@Override
	public SubscriberStatus getSelectionByUdpIdAndClipId(String udpId, int clipId, String msisdn, String cType) {
		SubscriberStatus subscribersStatus = null;
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		ResultSet rs = null;

		try {
			Clip clip = ServiceUtil.getClip(clipId, cType);
			if (clip != null) {
				String subWavFile = clip.getClipRbtWavFile();
				StringBuffer query = new StringBuffer();
				query.append("SELECT * FROM ").append(SUB_SELECTION_TABLE_NAME).append(" ").append("WHERE ")
						.append(UDP_ID_COL).append(" = ").append("'").append(udpId).append("'").append(" AND ")
						.append(SUBSCRIBER_WAV_FILE_COL).append(" = ").append("'").append(subWavFile).append("'")
						.append(" AND ").append(SUBSCRIBER_ID_COL).append(" = ").append("'").append(msisdn).append("'")
						.append(" AND ").append(SEL_STATUS_COL).append(" IN ").append("(").append("'W',").append("'A',")
						.append("'N',").append("'B',").append("'Z',").append("'G'").append(" )").append(" GROUP BY ")
						.append(SUBSCRIBER_ID_COL).append(", ").append(CALLER_ID_COL).append(", ").append(STATUS_COL)
						.append(", ").append(FROM_TIME_COL).append(", ").append(TO_TIME_COL);

				logger.info("Executing Query: " + query.toString());
				connection = RBTDBManager.getInstance().getConnection();
				preparedStatement = connection.prepareStatement(query.toString());
				rs = preparedStatement.executeQuery();
				while (rs.next())
					subscribersStatus = getSubStatus(rs);
			}
		}
		catch (SQLException e) {
			logger.info("Exception Occured: " + e, e);
		}
		catch (Exception e) {
			logger.info("Exception Occured: " + e, e);
		}
		finally {
			closeStatementAndRS(preparedStatement, rs, connection);
		}
		return subscribersStatus;
	}

	@Override
	public List<ExtendedSubStatus> getSelections(ExtendedSubStatus extendedSubStatus) {

		String msisdn = extendedSubStatus.subID();
		String id = extendedSubStatus.callerID();
		String type = extendedSubStatus.getType();

		List<ExtendedSubStatus> extendedSubStatusList = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			StringBuffer query = new StringBuffer();
			query.append("SELECT * FROM ").append(SUB_SELECTION_TABLE_NAME).append(" WHERE ").append(SUBSCRIBER_ID_COL)
					.append(" = ").append("'").append(msisdn).append("'");

			if (id != null) {
				query.append(" AND ").append(CALLER_ID_COL).append(" = ").append("'").append(id).append("'");
			}
			else if (type != null && !type.equalsIgnoreCase("DEFAULT")) {
				query.append(" AND ").append(CALLER_ID_COL);
				if (type.equalsIgnoreCase("GROUP"))
					query.append(" LIKE ").append("'").append("G%").append("'");
				else
					query.append(" NOT LIKE ").append("'").append("G%").append("'");

				query.append(" AND ").append(CALLER_ID_COL).append(" IS NOT NULL");
			}
			else if (type != null) {
				query.append(" AND ").append(CALLER_ID_COL).append(" IS NULL");
			}

			query.append(" AND ").append(SEL_STATUS_COL).append(" IN ").append("(").append("'W',").append("'A',")
					.append("'N',").append("'B',").append("'Z',").append("'G'").append(" )").append(" ORDER BY ")
					.append(SET_TIME_COL);

			final String finalQuery = query.toString();
			logger.info("Executing Query: " + finalQuery);

			connection = RBTDBManager.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(finalQuery);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				if (extendedSubStatusList == null)
					extendedSubStatusList = new ArrayList<ExtendedSubStatus>();
				extendedSubStatusList.add(getExtendedSubStatus(resultSet));
			}
		}
		catch (SQLException e) {
			logger.error("SQL Exception Occured: " + e, e);
		}
		catch (Exception e) {
			logger.error("Exception Occured: " + e, e);
		}
		finally {
			closeStatementAndRS(preparedStatement, resultSet, connection);
		}

		return extendedSubStatusList;
	}

	// Added for ephemeral rbt
	@Override
	public List<ExtendedSubStatus> getAllSelectionsByRestrictions(ExtendedSubStatus extendedSubStatus) {

		String msisdn = extendedSubStatus.subID();
		String id = extendedSubStatus.callerID();
		String type = extendedSubStatus.getType();
		int categoryID = extendedSubStatus.categoryID();
		String selStatus = extendedSubStatus.selStatus();
		String wavFileName = extendedSubStatus.subscriberFile();
		Date endTime = extendedSubStatus.endTime();
		int status = extendedSubStatus.status();

		List<ExtendedSubStatus> extendedSubStatusList = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			StringBuffer query = new StringBuffer();
			query.append("SELECT * FROM ").append(SUB_SELECTION_TABLE_NAME).append(" WHERE ").append(SUBSCRIBER_ID_COL)
					.append(" = ").append("'").append(msisdn).append("'");

			if (id != null) {
				query.append(" AND ").append(CALLER_ID_COL).append(" = ").append("'").append(id).append("'");
			}
			else if (type != null && !type.equalsIgnoreCase("DEFAULT")) {
				query.append(" AND ").append(CALLER_ID_COL);
				if (type.equalsIgnoreCase("GROUP"))
					query.append(" LIKE ").append("'").append("G%").append("'");
				else
					query.append(" NOT LIKE ").append("'").append("G%").append("'");

				query.append(" AND ").append(CALLER_ID_COL).append(" IS NOT NULL");
			}
			else if (type != null) {
				query.append(" AND ").append(CALLER_ID_COL).append(" IS NULL");
			}

			if (categoryID > -1) {
				query.append(" AND ").append(CATEGORY_ID_COL).append(" = ").append(categoryID);
			}

			if (wavFileName != null) {
				query.append(" AND ").append(SUBSCRIBER_WAV_FILE_COL).append("'").append(wavFileName).append("'");
			}

			if (status > 0) {
				query.append(" AND ").append(STATUS_COL).append(" = ").append(status);
			}

			if (selStatus != null && selStatus.equalsIgnoreCase("all")) {
				query.append(" AND ").append(SEL_STATUS_COL).append(" IN ").append("(").append("'W',").append("'A',")
						.append("'N',").append("'B',").append("'D',").append("'P',").append("'X',").append("'G',")
						.append("'Z'").append(" )");
			}
			else if (selStatus != null && selStatus.equalsIgnoreCase("deactive")) {
				query.append(" AND ").append(SEL_STATUS_COL).append(" IN ").append("(").append("'D',").append("'P',")
						.append("'X'").append(" )");
			}
			else {
				query.append(" AND ").append(SEL_STATUS_COL).append(" IN ").append("(").append("'W',").append("'A',")
						.append("'N',").append("'B'").append(" )");
			}

			if (endTime != null) {
				query.append(" AND ").append(END_TIME_COL).append(" > SYSDATE() ");
			}

			query.append(" ORDER BY ").append(SET_TIME_COL);

			final String finalQuery = query.toString();
			logger.info("Executing Query: " + finalQuery);

			connection = RBTDBManager.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(finalQuery);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				if (extendedSubStatusList == null)
					extendedSubStatusList = new ArrayList<ExtendedSubStatus>();
				extendedSubStatusList.add(getExtendedSubStatus(resultSet));
			}
		}
		catch (SQLException e) {
			logger.error("SQL Exception Occured: " + e, e);
		}
		catch (Exception e) {
			logger.error("Exception Occured: " + e, e);
		}
		finally {
			closeStatementAndRS(preparedStatement, resultSet, connection);
		}

		return extendedSubStatusList;
	}

	@Override
	public boolean deactivateSubSelection(ExtendedSubStatus extendedSubStatus, boolean isDirectDeact) {

		boolean deleted = false;
		String msisdn = extendedSubStatus.subID();
		String id = extendedSubStatus.callerID();
		int categoryID = extendedSubStatus.categoryID();
		int status = extendedSubStatus.status();
		String wavFileName = extendedSubStatus.subscriberFile();
		String deSelectedBy = extendedSubStatus.deSelectedBy();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			StringBuffer query = new StringBuffer();

			query.append("UPDATE ").append(SUB_SELECTION_TABLE_NAME).append(" SET ").append(END_TIME_COL)
					.append(" = SYSDATE(),");

			if (isDirectDeact) {
				query.append(SEL_STATUS_COL).append(" = 'X',").append(LOOP_STATUS_COL).append(" = 'x',");
			}
			else {
				query.append(SEL_STATUS_COL).append(" = 'D',");
			}
			query.append(DESELECTED_BY_COL).append(" = '").append(deSelectedBy).append("'").append(" WHERE ")
					.append(SUBSCRIBER_ID_COL).append(" = ").append("'").append(msisdn).append("'").append(" AND ")
					.append(END_TIME_COL).append(" > SYSDATE()");

			if (id != null) {
				query.append(" AND ").append(CALLER_ID_COL).append(" = ").append("'").append(id).append("'");
			}
			else {
				query.append(" AND ").append(CALLER_ID_COL).append(" IS NULL");
			}

			if (categoryID > -1) {
				query.append(" AND ").append(CATEGORY_ID_COL).append(" = ").append(categoryID);
			}

			if (wavFileName != null) {
				query.append(" AND ").append(SUBSCRIBER_WAV_FILE_COL).append(" = ").append("'").append(wavFileName)
						.append("'");
			}

			if (status > 0) {
				query.append(" AND ").append(STATUS_COL).append(" = ").append(status);
			}

			final String finalQuery = query.toString();
			logger.info("Executing Query: " + finalQuery);

			connection = RBTDBManager.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(finalQuery);
			int executeUpdate = preparedStatement.executeUpdate();
			if (executeUpdate > 0) {
				deleted = true;
			}
		}
		catch (Exception e) {
			logger.info("Exception occured while deleting :" + e);
		}

		return deleted;
	}

	/*@Override
	public List<SubscriberStatus> getAllActiveSelections(String msisdn) {
		List<SubscriberStatus> subscriberStatusList = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			StringBuffer query = new StringBuffer();
			query.append("SELECT * FROM ").append(SUB_SELECTION_TABLE_NAME).append(" ").append("WHERE ")
					.append(SUBSCRIBER_ID_COL).append(" = ").append("'").append(msisdn).append("'").append(" AND ")
					.append(SEL_STATUS_COL).append(" IN ").append("(").append("'W',").append("'A',").append("'N',")
					.append("'B',").append("'Z',").append("'G'").append(" )").append(" GROUP BY ").append(UDP_ID_COL);

			final String finalQuery = query.toString();
			logger.info("Executing Query: " + finalQuery);

			connection = RBTDBManager.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(finalQuery);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				if (subscriberStatusList == null)
					subscriberStatusList = new ArrayList<SubscriberStatus>();
				subscriberStatusList.add(getSubStatus(resultSet));
			}
		}
		catch (SQLException e) {
			logger.error("SQL Exception Occured: " + e, e);
		}
		catch (Exception e) {
			logger.error("Exception Occured: " + e, e);
		}
		finally {
			closeStatementAndRS(preparedStatement, resultSet, connection);
		}
		return subscriberStatusList;
	}*/
}