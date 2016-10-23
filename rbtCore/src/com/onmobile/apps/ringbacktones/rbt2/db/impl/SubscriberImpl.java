package com.onmobile.apps.ringbacktones.rbt2.db.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.RBTResultSet;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.DBUtils;
import com.onmobile.apps.ringbacktones.rbt2.db.ISubscriber;
import com.onmobile.apps.ringbacktones.v2.dao.constants.TableConstants;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;

/**
 * 
 * @author md.alam
 *
 */

@Repository(value = BeanConstant.SUBSCRIBER_IMPL)
public class SubscriberImpl extends CommonOpsDBImpl implements ISubscriber,TableConstants {

	private static Logger logger = Logger.getLogger(SubscriberImpl.class);

	@Override
	public boolean insert(Subscriber subscriber) {
		boolean isInserted = false;
		
		String prepaidYes = "n";
		if(subscriber.prepaidYes())
			prepaidYes = "y";

		String playerStatus = "B";
		

		StringBuffer queryBuilder = new StringBuffer();
		queryBuilder.append("INSERT INTO ").append(RBT_SUB_TABLE).append(" ( ")
		.append(RBT_SUB_SUBSCRIBER_ID_COL).append(", ").append(RBT_SUB_ACTIVATED_BY_COL).append(", ")
		.append(RBT_SUB_DEACTIVATED_BY_COL).append(", ").append(RBT_SUB_START_DATE_COL).append(", ")
		.append(RBT_SUB_END_DATE_COL).append(", ").append(RBT_SUB_PREPAID_YES_COL).append(", ")
		.append(RBT_SUB_LAST_ACCESS_DATE_COL).append(", ").append(RBT_SUB_NEXT_CHARGING_DATE_COL).append(", ")
		.append(RBT_SUB_NUM_VOICE_ACCESS_COL).append(", ").append(RBT_SUB_ACTIVATION_INFO_COL).append(", ")
		.append(RBT_SUB_SUBSCRIPTION_CLASS_COL).append(", ").append(RBT_SUB_SUBSCRIPTION_YES_COL).append(", ")
		.append(RBT_SUB_LAST_DEACTIVATION_INFO_COL).append(", ").append(RBT_SUB_LAST_DEACTIVATION_DATE_COL).append(", ")
		.append(RBT_SUB_ACTIVATION_DATE_COL).append(", ").append(RBT_SUB_NUM_MAX_SELECTIONS_COL).append(", ")
		.append(RBT_SUB_COS_ID_COL).append(", ").append(RBT_SUB_ACTIVATED_COS_ID_COL).append(", ")
		.append(RBT_SUB_RBT_TYPE_COL).append(", ").append(RBT_SUB_LANGUAGE_COL).append(", ")
		.append(RBT_SUB_PLAYER_STATUS_COL).append(", ").append(RBT_SUB_EXTRA_INFO_COL).append(", ")
		.append(RBT_SUB_CIRCLE_ID_COL).append(", ").append(RBT_SUB_INTERNAL_REF_ID_COL).append(") VALUES ( ")
		.append(subscriber.subID()).append(", ").append(DBUtils.sqlString(subscriber.activatedBy())).append(", ")
		.append(DBUtils.sqlString(subscriber.deactivatedBy())).append(", ").append("SYSDATE()").append(", ")
		.append("TIMESTAMP('2037-12-31')").append(", ").append("'").append(prepaidYes).append("'").append(", ")
		.append(subscriber.accessDate()).append(", ").append(subscriber.nextChargingDate()).append(", ")
		.append(subscriber.noOfAccess()).append(", ").append(DBUtils.sqlString(subscriber.activationInfo())).append(", ")
		.append(DBUtils.sqlString(subscriber.subscriptionClass())).append(", ").append("'").append(subscriber.subYes()).append("'").append(", ")
		.append(DBUtils.sqlString(subscriber.lastDeactivationInfo())).append(", ").append(subscriber.lastDeactivationDate()).append(", ")
		.append(subscriber.activationDate()).append(", ").append(subscriber.maxSelections()).append(", ")
		.append(DBUtils.sqlString(subscriber.cosID())).append(", ").append(DBUtils.sqlString(subscriber.activatedCosID())).append(", ")
		.append(subscriber.rbtType()).append(", ").append(DBUtils.sqlString(subscriber.language())).append(", ")
		.append(DBUtils.sqlString(playerStatus)).append(", ").append(DBUtils.sqlString(subscriber.extraInfo())).append(", ")
		.append(DBUtils.sqlString(subscriber.circleID())).append(", ").append(DBUtils.sqlString(subscriber.refID())).append(" )");


		String finalQuery = queryBuilder.toString();
		Connection connection = RBTDBManager.getInstance().getConnection();

		logger.info("Executing the query: " + finalQuery);
		int n = executeUpdateQuery(connection, finalQuery);
		if (n == 1)
		{
			logger.info("Insertion into RBT_SUBSCRIBER table is SUCCESS for subscriber: " + subscriber.subID());

			isInserted = true;
		}
		else
		{
			logger.info("Insertion into RBT_SUBSCRIBER table is FAILED for subscriber: " + subscriber.subID());
			isInserted = false;
		}
		
		return isInserted;
	}


	@Override
	public boolean updateSubscriber(Subscriber subscriber) {
		boolean isUpdated = false;
		
		// Subscriber subscriber = null;
		StringBuffer queryBuilder = new StringBuffer();
		queryBuilder.append("UPDATE ").append(RBT_SUB_TABLE).append(" SET ")
//		.append(RBT_SUB_ACTIVATED_BY_COL).append(" = ").append(DBUtils.sqlString(subscriber.activatedBy())).append(", ")
//		.append(RBT_SUB_NUM_VOICE_ACCESS_COL).append(" = ").append(subscriber.noOfAccess()).append(", ")
//		.append(RBT_SUB_ACTIVATION_INFO_COL).append(" = ").append(DBUtils.sqlString(subscriber.activationInfo())).append(", ")
		.append(RBT_SUB_SUBSCRIPTION_CLASS_COL).append(" = ").append(DBUtils.sqlString(subscriber.subscriptionClass())).append(", ")
		.append(RBT_SUB_SUBSCRIPTION_YES_COL).append(" = ").append("'").append(subscriber.subYes()).append("'").append(", ")
		.append(RBT_SUB_OLD_CLASS_TYPE_COL).append(" = ").append(DBUtils.sqlString(subscriber.oldClassType())).append(", ")
//		.append(RBT_SUB_COS_ID_COL).append(" = ").append(DBUtils.sqlString(subscriber.cosID())).append(", ")
//		.append(RBT_SUB_ACTIVATED_COS_ID_COL).append(" = ").append(DBUtils.sqlString(subscriber.activatedCosID())).append(", ")
		.append(RBT_SUB_EXTRA_INFO_COL).append(" = ").append(DBUtils.sqlString(subscriber.extraInfo())).append(", ")
		.append(RBT_SUB_ACTIVATION_DATE_COL).append(" = ").append(DBUtils.sqlString(null))
//		.append(RBT_SUB_CIRCLE_ID_COL).append(" = ").append(DBUtils.sqlString(subscriber.circleID()))
		.append(" WHERE ").append(RBT_SUB_SUBSCRIBER_ID_COL).append(" = ").append(subscriber.subID())
		.append(" AND ").append(RBT_SUB_SUBSCRIPTION_YES_COL).append(" IN ('B','z','Z','G','N')");

		String finalUpdateQuery = queryBuilder.toString();
		Connection connection = RBTDBManager.getInstance().getConnection();

		logger.info("Executing the query: " + finalUpdateQuery);
		int n = executeUpdateQuery(connection, finalUpdateQuery);
		if (n == 1)
		{
			logger.info("Updating RBT_SUBSCRIBER table is SUCCESS for subscriber: " + subscriber.subID());

			isUpdated = true;
		}
		else
		{
			logger.info("Updating RBT_SUBSCRIBER table is FAILED for subscriber: " + subscriber.subID());
			isUpdated = false;
		}
		return isUpdated;

	}


	//VOL-947
	@Override
	public boolean deleteSubscriberById(String subId) {

		boolean isDeleted = false;
		
		// Subscriber subscriber = null;
		StringBuffer queryBuilder = new StringBuffer();
		queryBuilder.append("DELETE FROM ").append(RBT_SUB_TABLE).append(" WHERE ")
		.append(RBT_SUB_SUBSCRIBER_ID_COL).append(" = ").append(DBUtils.sqlString(subId));

		String finalDeleteQuery = queryBuilder.toString();
		Connection connection = RBTDBManager.getInstance().getConnection();

		logger.info("Executing the query: " + finalDeleteQuery);
		int n = executeUpdateQuery(connection, finalDeleteQuery);
		if (n == 1)
		{
			logger.info(subId+ " is deleted from RBT_SUBSCRIBER table");

			isDeleted = true;
		}
		else
		{
			logger.info(subId+ " is not present in RBT_SUBSCRIBER table");
			isDeleted = false;
		}
		return isDeleted;
	
	}	
	
	
	public Subscriber getSubscriber(String msisdn) {
		Statement stmt = null;
		ResultSet results = null;
		String query = "SELECT * FROM " + RBT_SUB_TABLE + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + msisdn + "'";
		Connection conn = RBTDBManager.getInstance().getConnection();
		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.first())
			{
				return getSubscriberFromRS(results);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results, conn);
		}
		return null;
	}
	
	private  Subscriber getSubscriberFromRS(ResultSet rs) throws SQLException
	{
		RBTResultSet results = new RBTResultSet(rs);

		String subscriberID = results.getString(SUBSCRIBER_ID_COL);
		String activate = results.getString(RBT_SUB_ACTIVATED_BY_COL);
		String deactivate = results.getString(RBT_SUB_DEACTIVATED_BY_COL);
		Date startDate = results.getTimestamp(RBT_SUB_START_DATE_COL);
		Date endDate = results.getTimestamp(RBT_SUB_END_DATE_COL);
		String prepaid = results.getString(PREPAID_YES_COL);
		Date accessDate = results.getTimestamp(RBT_SUB_LAST_ACCESS_DATE_COL);
		Date nextChargingDate = results.getTimestamp(NEXT_CHARGING_DATE_COL);
		int access = results.getInt(RBT_SUB_NUM_VOICE_ACCESS_COL);
		String info = results.getString(RBT_SUB_ACTIVATION_INFO_COL);
		String subscriptionClass = results.getString(RBT_SUB_SUBSCRIPTION_CLASS_COL);
		String subscription = results.getString(RBT_SUB_SUBSCRIPTION_YES_COL);
		String lastDeactivationInfo = results.getString(RBT_SUB_LAST_DEACTIVATION_INFO_COL);
		Date lastDeactivationDate = results.getTimestamp(RBT_SUB_LAST_DEACTIVATION_DATE_COL);
		Date activationDate = results.getTimestamp(RBT_SUB_ACTIVATION_DATE_COL);
		int maxSelections = results.getInt(RBT_SUB_NUM_MAX_SELECTIONS_COL);
		String cosID = results.getString(RBT_SUB_COS_ID_COL);
		String activatedCosID = results.getString(RBT_SUB_ACTIVATED_COS_ID_COL);
		int rbtType = results.getInt(RBT_SUB_RBT_TYPE_COL);
		String language = results.getString(RBT_SUB_LANGUAGE_COL);
		String strOldClassType = results.getString(OLD_CLASS_TYPE_COL);
		String extraInfo = results.getString(EXTRA_INFO_COL);
		String circleID = results.getString(CIRCLE_ID_COL);
		String refID = results.getString(INTERNAL_REF_ID_COL);
		String retryCount = results.getString(RETRY_COUNT_COL);
		Date nextRetryTime = results.getTimestamp(NEXT_RETRY_TIME_COL);
		//New Column added for prism billing date
		//Date nextBillingDate = results.getTimestamp(NE);
		
		
		com.onmobile.apps.ringbacktones.content.database.SubscriberImpl subscriber = new com.onmobile.apps.ringbacktones.content.database.SubscriberImpl(subscriberID, activate,
				deactivate, startDate, endDate, prepaid, accessDate,
				nextChargingDate, access, info, subscriptionClass,
				subscription, lastDeactivationInfo,
				lastDeactivationDate, activationDate, maxSelections, cosID, activatedCosID, rbtType,
				language,strOldClassType, extraInfo, circleID, refID, retryCount, nextRetryTime); 

		return subscriber; 
	}
	
	public Consent getConsentObject(String msisdn) {

		Consent consentObj = null;
		Statement stmt = null;
		ResultSet results = null;
		String query = "SELECT * FROM RBT_CONSENT WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + msisdn + "'"
				+ " AND " + REQUEST_TYPE_COL + " = '" + REQUEST_TYPE_VALUE + "'";
		Connection conn = RBTDBManager.getInstance().getConnection();
		logger.info("Executing the query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.first())
			{
				consentObj = new Consent();
				consentObj.setMsisdn(results.getString("SUBSCRIBER_ID"));
				consentObj.setSubClass(results.getString("SUBSCRIPTION_CLASS"));
				return consentObj;
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return consentObj;
		}
		finally
		{
			closeStatementAndRS(stmt, results, conn);
		}
		return consentObj;
	
	}

}
