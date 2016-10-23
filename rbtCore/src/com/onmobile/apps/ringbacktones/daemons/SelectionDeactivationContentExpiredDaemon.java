package com.onmobile.apps.ringbacktones.daemons;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.provisioning.bean.RBTUpdatedClips;
import com.onmobile.apps.ringbacktones.provisioning.bean.Selection;

public class SelectionDeactivationContentExpiredDaemon extends TimerTask {

	private static final Logger LOGGER = Logger
			.getLogger(SelectionDeactivationContentExpiredDaemon.class);
	private int scheduleTimeHour = 0;
	private int scheduleTimeMin = 0;
	private int intervalTime = 1;
	private int hitCount = 0;
	private static final String TABLE_NAME = "RBT_SUBSCRIBER_SELECTIONS";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String SUBSCRIBER_REF_ID_COL = "INTERNAL_REF_ID";
	private static final String SUBSCRIBER_WAV_FILE_COL = "SUBSCRIBER_WAV_FILE";
	private static final String CATEGORY_ID_COL = "CATEGORY_ID";
	private static final String CATEGORY_TYPE_COL = "CATEGORY_TYPE";
	private static final String END_TIME_COL = "END_TIME";
	private static final String SEL_STATUS_COL = "SEL_STATUS";
	private static final String DEACT_STATUS = "D";
	private static final String DE_SEL_BY_COL = "DESELECTED_BY";
	private static String DE_SEL_BY_VAL = "DAEMON";
	private static String SEL_STATUS_TO_BE_DEACTIVATED = null;
	private static int configuredDays = 0;
	private static long sleepTimeStr = 1;

	public SelectionDeactivationContentExpiredDaemon() {
		scheduleTimeHour = RBTParametersUtils.getParamAsInt(
				iRBTConstant.DAEMON, "SELECTION_DEACT_SCHEDULE_TIME_HOURS", 11);
		scheduleTimeMin = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"SELECTION_DEACT_SCHEDULE_TIME_MIN", 1);
		intervalTime = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"SELECTION_DEACT_BETWEEN_NEXT_RUN", 60);
		hitCount = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"SELECTION_DEACT_REQUEST_HIT_COUNT", 15);
		SEL_STATUS_TO_BE_DEACTIVATED = RBTParametersUtils.getParamAsString(
				iRBTConstant.DAEMON, "SELECTION_STATUS_DEACT", "B");
		configuredDays = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"SELECTION_DEACT_DAYS", 0);
		DE_SEL_BY_VAL = RBTParametersUtils.getParamAsString(
				iRBTConstant.DAEMON, "SELECTION_DEACT_DESELECTED_BY", "DAEMON");
		sleepTimeStr = RBTParametersUtils.getParamAsLong("DAEMON",
				"SLEEP_TIME_FOR_CONTENT_EXPIRED_DAEMON", 1);
	}

	public void start() {
		LOGGER.info("Scheduling SelectionDeactivationContentExpiredDaemon...");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, scheduleTimeHour);
		calendar.set(Calendar.MINUTE, scheduleTimeMin);
		calendar.set(Calendar.SECOND, 0);
		Date startDate = calendar.getTime();
		LOGGER.info("Start date is " + startDate);
		Timer timer = new Timer(
				SelectionDeactivationContentExpiredDaemon.class.getSimpleName());
		long intervalTimeInMilli = intervalTime * 60 * 1000;
		timer.scheduleAtFixedRate(this, startDate, intervalTimeInMilli);

		LOGGER.info("SelectionDeactivationContentExpiredDaemon has been scheduled");
	}

	public void stop() {
		this.cancel();
	}

	@SuppressWarnings("unchecked")
	public void run() {
		Transaction tx = null;
		try {
			Session session = null;
			session = HibernateUtil.getSession();
			boolean listIsEmpty = false;
			Query deltaQuery = null;
			deltaQuery = session.createQuery("FROM RBTUpdatedClips");
			LOGGER.info("Query string for delta object is: "
					+ deltaQuery.getQueryString());
			List<RBTUpdatedClips> deltaTableList = deltaQuery.setMaxResults(
					hitCount).list();
			LOGGER.info("Query executed to get the data from Delta table");
			if (deltaTableList != null && !deltaTableList.isEmpty()) {
				try {
					tx = session.beginTransaction();
					for (RBTUpdatedClips deltaObj : deltaTableList) {
						if (deltaObj != null) {
							try {
								if (deltaObj.getClipName() != null
										&& !deltaObj.getClipName().isEmpty()) {
									updateSelectionEndTimewithClipTime(
											deltaObj.getClipWavFile(),
											deltaObj.getEndTime());
								}
								if (deltaObj.getCategoryId() + "" != null
										&& !(deltaObj.getCategoryId() + "")
												.isEmpty()) {
									Category cat = RBTDBManager.getInstance()
											.getCategory(
													deltaObj.getCategoryId());
									if (cat != null) {
										updateSelectionEndTimewithCategoryTime(
												cat.getID(), cat.getType(),
												cat.getEndTime());
									}
								}
								session.delete(deltaObj);
							} catch (Exception e) {
								LOGGER.info("Exception occured while updating selection "
										+ e);
							}
						}
					}
				} catch (Exception e) {
					LOGGER.info("Exception occured while updating selection "
							+ e);
				} finally {
					session.flush();
					tx.commit();
				}
			} else {
				LOGGER.info("returning from thread since the list is empty");
				listIsEmpty = true;
			}
			Query selectionQuery = null;
			Date currentDatewithDays = dateWithConfiguredDays(new Date(),
					configuredDays);
			selectionQuery = session
					.createQuery("FROM Selection where sel_Status IN (:status) and end_Time <= :currentDate");
			String selDeact[] = SEL_STATUS_TO_BE_DEACTIVATED.split(",");
			selectionQuery.setParameterList("status", Arrays.asList(selDeact));
			selectionQuery.setParameter("currentDate", new Timestamp(
					currentDatewithDays.getTime()));
			LOGGER.info("Query string for selection object is: "
					+ selectionQuery.getQueryString());
			List<Selection> selectionList = selectionQuery.list();
			LOGGER.info("Query executed to get the data from Selection table");
			if (selectionList != null && !selectionList.isEmpty()) {
				LOGGER.info("Returned selections with size: "
						+ selectionList.size());
				for (Selection selectionObj : selectionList) {
					if (selectionObj != null) {
						try {
							deactivateSelection(selectionObj.getId()
									.getSubscriber_Id(), selectionObj.getId()
									.getInternal_Ref_Id(), selectionObj.getId()
									.getSubscriber_Wav_File());
						} catch (Exception e) {
							LOGGER.info("Exception occured while deactivating selection "
									+ e);
						}
					}
				}
			} else {
				LOGGER.info("returning from thread since the list is empty");
			}
			if (listIsEmpty) {
				Thread.sleep(sleepTimeStr * 60 * 1000);
				LOGGER.info("Thread going to sleep for "+sleepTimeStr * 60 * 1000+" millis");
			}
		} catch (HibernateException he) {
			LOGGER.info("Exception occured while getting selections " + he);
			he.printStackTrace();
			if (tx != null) {
				tx.rollback();
			}
		} catch (InterruptedException e) {
			LOGGER.info("Exception occured while thread excecution " + e);
		}
	}

	public void deactivateSelection(String subscriberId, String refId,
			String wavFile) throws Exception {
		Connection conn = null;
		String query = null;
		Statement stmt = null;
		query = "UPDATE " + TABLE_NAME + " SET " + SEL_STATUS_COL + " = '"
				+ DEACT_STATUS + "'," + DE_SEL_BY_COL + "='" + DE_SEL_BY_VAL
				+ "'  WHERE " + SUBSCRIBER_ID_COL + "='" + subscriberId
				+ "' AND " + SUBSCRIBER_REF_ID_COL + "='" + refId + "'"
				+ " AND " + SUBSCRIBER_WAV_FILE_COL + "='" + wavFile + "'";
		LOGGER.info("Executing query: " + query);
		try {
			conn = RBTDBManager.getInstance().getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException se) {
			LOGGER.error("", se);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				RBTDBManager.getInstance().releaseConnection(conn);
			}
		}
	}

	public void updateSelectionEndTimewithCategoryTime(int categoryId,
			int categoryType, Date categoryEndTime) throws Exception {
		Connection conn = null;
		String query = null;
		Statement stmt = null;
		String status = SEL_STATUS_TO_BE_DEACTIVATED;
		status = "'" + status.replace(",", "','").concat("'");
		query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL + " = '"
				+ categoryEndTime + "' WHERE " + CATEGORY_ID_COL + "="
				+ categoryId + " AND " + CATEGORY_TYPE_COL + "=" + categoryType
				+ " AND SEL_STATUS IN(" + status + ")";
		LOGGER.info("Executing query: " + query);
		try {
			conn = RBTDBManager.getInstance().getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException se) {
			LOGGER.error("", se);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				RBTDBManager.getInstance().releaseConnection(conn);
			}
		}
	}

	public void updateSelectionEndTimewithClipTime(String wavFile,
			Timestamp clipEndDate) throws Exception {
		Connection conn = null;
		String query = null;
		Statement stmt = null;
		String status = SEL_STATUS_TO_BE_DEACTIVATED;
		status = "'" + status.replace(",", "','").concat("'");
		query = "UPDATE " + TABLE_NAME + " SET " + END_TIME_COL + " = '"
				+ clipEndDate + "' WHERE " + SUBSCRIBER_WAV_FILE_COL + "='"
				+ wavFile + "' AND SEL_STATUS IN(" + status + ")";
		LOGGER.info("Executing query: " + query);
		try {
			conn = RBTDBManager.getInstance().getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException se) {
			LOGGER.error("", se);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				RBTDBManager.getInstance().releaseConnection(conn);
			}
		}
	}

	public static Date dateWithConfiguredDays(Date date, int configuredDays) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, -configuredDays);
		return cal.getTime();
	}
}
