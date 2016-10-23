package com.onmobile.apps.ringbacktones.daemons;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

public class GNOCDBUtility {
	private static final Logger LOGGER = Logger.getLogger(GNOCDBUtility.class);
	private Connection conn = null;
	private Connection conn2 = null;
	private Connection conn3 = null;
	private Connection conn4 = null;
	private Connection conn5 = null;
	private Statement deleteLastSequenceIdStmt = null;
	private Statement lastSequenceIdStmt = null;
	private Statement mappingInsertStmt = null;
	private Statement mappingSelectStmt = null;
	private Statement statusTableStmt = null;

	public void closeStmtAndConn() {
		try {
			statusTableStmt.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
		try {
			lastSequenceIdStmt.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
		try {
			mappingInsertStmt.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
		try {
			mappingSelectStmt.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
		try {
			deleteLastSequenceIdStmt.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
		try {
			conn.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
		try {
			conn2.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
		try {
			conn3.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
		try {
			conn4.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
		try {
			conn5.close();
		} catch (Exception e) {
			LOGGER.error("error in statement close.",e);
		}
	}

	public Statement getDeleteLastSequenceIdStmt() {
		return deleteLastSequenceIdStmt;
	}

	public Statement getLastSequenceIdStmt() {
		return lastSequenceIdStmt;
	}

	public Statement getMappingInsertStmt() {
		return mappingInsertStmt;
	}

	public Statement getMappingSelectStmt() {
		return mappingSelectStmt;
	}

	public Statement getStatusTableStmt() {
		return statusTableStmt;
	}

	public void initConnAndStmt() {
		try {
			Class.forName(GNOCPropertyReader.driverName);
			LOGGER.debug("Connecting to DB.");
			conn = DriverManager.getConnection(GNOCPropertyReader.dbUrl, GNOCPropertyReader.dbUsername, GNOCPropertyReader.dbPassword);
			conn2 = DriverManager.getConnection(GNOCPropertyReader.dbUrl, GNOCPropertyReader.dbUsername, GNOCPropertyReader.dbPassword);
			conn3 = DriverManager.getConnection(GNOCPropertyReader.dbUrl, GNOCPropertyReader.dbUsername, GNOCPropertyReader.dbPassword);
			conn4 = DriverManager.getConnection(GNOCPropertyReader.dbUrl, GNOCPropertyReader.dbUsername, GNOCPropertyReader.dbPassword);
			conn5 = DriverManager.getConnection(GNOCPropertyReader.dbUrl, GNOCPropertyReader.dbUsername, GNOCPropertyReader.dbPassword);
			LOGGER.info("Got db connection to DB.");
			statusTableStmt = conn.createStatement();
			lastSequenceIdStmt = conn2.createStatement();
			mappingInsertStmt = conn3.createStatement();
			mappingSelectStmt = conn4.createStatement();
			deleteLastSequenceIdStmt = conn5.createStatement();
			LOGGER.debug("Created all statements.");
		} catch (ClassNotFoundException e) {
			LOGGER.debug("connection class not found.", e);
		} catch (SQLException e) {
			LOGGER.debug("SQL exception in making connection.", e);
		}
	}
}