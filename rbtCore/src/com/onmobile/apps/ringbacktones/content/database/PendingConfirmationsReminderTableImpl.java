/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: rajesh.karavadi $
 * $Id: PendingConfirmationsReminderTableImpl.java,v 1.3 2012/07/17 08:43:04 rajesh.karavadi Exp $
 * $Revision: 1.3 $
 * $Date: 2012/07/17 08:43:04 $
 */
package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class PendingConfirmationsReminderTableImpl extends RBTPrimitive {

	private String subscriberId;
	private int remindersLeft;
	private Date lastReminderSent;
	private Date smsReceivedTime;
	private String reminderText;
	private String sender;
	private long smsId;
	
	private static final String TABLE_NAME = "RBT_PENDING_CONFIRMATIONS_REMINDER";
	private static final String INSERT_INTO = "INSERT INTO ";
	private static final String DELETE_FROM = "DELETE FROM ";
	private static final String SELECT_FROM = "SELECT * FROM ";
	private static final String UPDATE = "UPDATE ";
	private static final String SUBSCRIBER_ID = "SUBSCRIBER_ID";
	private static final String REMINDERS_LEFT = "REMINDERS_LEFT";
	private static final String LAST_REMINDER_SENT = "LAST_REMINDER_SENT";
	private static final String SMS_RECEIVED_TIME = "SMS_RECEIVED_TIME";
	private static final String REMINDER_TEXT = "REMINDER_TEXT";
	private static final String SENDER = "SENDER";
	private static final String SMS_ID = "SMS_ID";
	private static final String WHERE = " WHERE ";
	private static final String EQUALS = " = ";
	private static final String SET = " SET ";
	private static final String COMMA = ", ";
	private static final String OPEN_BRACE = " (";
	private static final String CLOSE_BRACE = ") ";
	private static final String VALUES = " VALUES ";
	private static final String AND = " AND ";
	private static final String CLOSE_PARENTHESIS = " )";
	private static final String LIMIT = " LIMIT ";
	private static final String SYSDATE = " SYSDATE() ";
	private static final String LESS_OR_EQUAL = " <= ";
	private static final String GREATER_THAN = " > ";
	private static final String ZERO = "0";
	private static final String TIMESTAMPADD_MINUTE = " TIMESTAMPADD(MINUTE, ";

	private static final Logger logger = Logger
			.getLogger(PendingConfirmationsReminderTableImpl.class);

	/**
	 * @param subscriberId
	 * @param remindersLeft
	 * @param lastReminderSent
	 * @param smsReceivedTime
	 * @param reminderText
	 * @param sender
	 * @param smsId
	 */
	public PendingConfirmationsReminderTableImpl(String subscriberId,
			int remindersLeft, Date lastReminderSent, Date smsReceivedTime,
			String reminderText, String sender, long smsId) {
		super();
		this.subscriberId = subscriberId;
		this.remindersLeft = remindersLeft;
		this.lastReminderSent = lastReminderSent;
		this.smsReceivedTime = smsReceivedTime;
		this.reminderText = reminderText;
		this.sender = sender;
		this.smsId = smsId;
	}

	public static PendingConfirmationsReminderTableImpl insert(
			Connection connection, String subscriberId, int remindersLeft,
			Date lastreminderSent, Date smsReceivedTime, String sender, String reminderText,
			long smsId) {
		
		Statement stmt = null;
		PendingConfirmationsReminderTableImpl pendingConfirmationsreminderTableImpl = null;

		StringBuffer sb = new StringBuffer();
		sb.append(INSERT_INTO).append(TABLE_NAME).append(OPEN_BRACE);
		sb.append(SUBSCRIBER_ID).append(COMMA);
		sb.append(REMINDERS_LEFT).append(COMMA);
		sb.append(LAST_REMINDER_SENT).append(COMMA);
		sb.append(SMS_RECEIVED_TIME).append(COMMA);
		sb.append(SENDER).append(COMMA);
		sb.append(REMINDER_TEXT).append(COMMA);
		sb.append(SMS_ID).append(CLOSE_BRACE);
		sb.append(VALUES);
		sb.append(OPEN_BRACE);
		sb.append(sqlString(subscriberId)).append(COMMA);
		sb.append(remindersLeft).append(COMMA);
		sb.append(mySQLDateTime(lastreminderSent)).append(COMMA);
		sb.append(mySQLDateTime(smsReceivedTime)).append(COMMA);
		sb.append(sqlString(reminderText)).append(COMMA);
		sb.append(sqlString(sender)).append(COMMA);
		sb.append(smsId).append(CLOSE_BRACE);

		logger.info("Executing query " + sb.toString());

		try {
			stmt = connection.createStatement();
			if (stmt.executeUpdate(sb.toString()) > 0) {
				logger.info("Successfully insertion to "
						+ "rbt_pending_confirmations_reminder table");
				pendingConfirmationsreminderTableImpl = new PendingConfirmationsReminderTableImpl(
						subscriberId, remindersLeft, lastreminderSent,
						smsReceivedTime, reminderText, sender, smsId);
			}
		} catch (SQLException se) {
			logger.error("Unable to insertion to "
					+ "rbt_pending_confirmations_reminder table. Exception: "
					+ se.getMessage(), se);
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return pendingConfirmationsreminderTableImpl;
	}
	
	public static PendingConfirmationsReminderTableImpl[] get(
			Connection connection, String delayInSentTime, String limitFrom,
			String limitTo) {
		Statement stmt = null;
		ResultSet result = null;
		String errorMsg = "Unable to get PendingConfirmationseminderr. Exception: ";
		
		StringBuffer query = new StringBuffer();
		query.append(SELECT_FROM).append(TABLE_NAME);
		query.append(WHERE).append(REMINDERS_LEFT).append(GREATER_THAN).append(ZERO);
		query.append(AND);
		query.append(TIMESTAMPADD_MINUTE).append(delayInSentTime).append(COMMA);
		query.append(SMS_RECEIVED_TIME).append(CLOSE_PARENTHESIS);
		query.append(LESS_OR_EQUAL).append(SYSDATE); 
		query.append(AND);
		query.append(TIMESTAMPADD_MINUTE).append(delayInSentTime).append(COMMA);
		query.append(LAST_REMINDER_SENT).append(CLOSE_PARENTHESIS);
		query.append(LESS_OR_EQUAL).append(SYSDATE); 
		query.append(LIMIT).append(limitFrom).append(COMMA).append(limitTo);
		
		logger.info("Executing query " + query);
		
		try {
			stmt = connection.createStatement();
			result = stmt.executeQuery(query.toString());
			List<PendingConfirmationsReminderTableImpl> list = new ArrayList<PendingConfirmationsReminderTableImpl>();
			
			while (result.next()) {
				String subscriberId = result.getString(SUBSCRIBER_ID);
				int remindersLeft = result.getInt(REMINDERS_LEFT);
				Date lastreminderSent = result.getTimestamp(LAST_REMINDER_SENT);
				Date smsSentTime = result.getTimestamp(SMS_RECEIVED_TIME);
				String reminderText = result.getString(REMINDER_TEXT);
				String sender = result.getString(SENDER);
				long smsId = result.getLong(SMS_ID);
				PendingConfirmationsReminderTableImpl pendingConfirmationsreminderTableImpl = new PendingConfirmationsReminderTableImpl(
						subscriberId, remindersLeft, lastreminderSent,
						smsSentTime, reminderText, sender, smsId);
				list.add(pendingConfirmationsreminderTableImpl);
			}
			int size = list.size();
			logger.debug("Total pending records found are: "+size);
			if(size > 0) {
				return (PendingConfirmationsReminderTableImpl[]) list.toArray(new PendingConfirmationsReminderTableImpl[0]);
			}
		} catch (SQLException se) {
			logger.error(errorMsg.concat(se.getMessage()), se);
		} catch(Exception e) {
			logger.error(errorMsg.concat(e.getMessage()), e);
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return null;
	}
	
	public static int update(
			Connection connection, String subscriberId, int remindersLeft, Date lastReminderSent) {
		Statement stmt = null;
		int count = 0;
		String errorMsg = "Unable to update PendingConfirmationsReminder. Exception: ";
		StringBuffer query = new StringBuffer();
		query.append(UPDATE).append(TABLE_NAME).append(SET);
		query.append(REMINDERS_LEFT).append(EQUALS).append(remindersLeft).append(COMMA);
		query.append(LAST_REMINDER_SENT).append(EQUALS).append(mySQLDateTime(lastReminderSent));
		query.append(WHERE).append(SUBSCRIBER_ID).append(EQUALS).append(subscriberId);
		query.append(AND).append(REMINDERS_LEFT).append(GREATER_THAN).append(ZERO);
		
		logger.info("Executing query " + query);
		
		try {
			stmt = connection.createStatement();
			count = stmt.executeUpdate(query.toString());
			logger.info("Updated " + count + " records");
		} catch (SQLException se) {
			logger.error(errorMsg.concat(se.getMessage()), se);
		} catch (Exception e) {
			logger.error(errorMsg.concat(e.getMessage()), e);
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return count;
	}
	
	public static int delete(
			Connection connection, int limit) {
		Statement stmt = null;
		int count = 0;
		String exceptionMsg = "Unable to delete from PendingConfirmationsReminder. Exception: ";

		StringBuffer query = new StringBuffer();
		query.append(DELETE_FROM).append(TABLE_NAME);
		query.append(WHERE);
		query.append(REMINDERS_LEFT).append(LESS_OR_EQUAL).append(ZERO);
		query.append(LIMIT).append(limit);
		logger.info("Executing query " + query);
		
		try {
			stmt = connection.createStatement();
			count = stmt.executeUpdate(query.toString());
			logger.info("Deleted: " + count + " records");
		} catch (SQLException se) {
			logger.error(exceptionMsg.concat(se.getMessage()), se);
		} catch (Exception e) {
			logger.error(exceptionMsg.concat(e.getMessage()), e);
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return count;
	}
	
	public String getSubscriberId() {
		return subscriberId;
	}

	public int getRemindersLeft() {
		return remindersLeft;
	}

	public Date getLastReminderSent() {
		return lastReminderSent;
	}

	public Date getSmsReceivedTime() {
		return smsReceivedTime;
	}

	public String getReminderText() {
		return reminderText;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public long getSmsId() {
		return smsId;
	}

	@Override
	public String toString() {
		return "PendingConfirmationsReminderTableImpl [lastReminderSent="
				+ lastReminderSent + ", reminderText=" + reminderText
				+ ", remindersLeft=" + remindersLeft + ", sender=" + sender
				+ ", smsId=" + smsId + ", smsReceivedTime=" + smsReceivedTime
				+ ", subscriberId=" + subscriberId + "]";
	}
	
}
