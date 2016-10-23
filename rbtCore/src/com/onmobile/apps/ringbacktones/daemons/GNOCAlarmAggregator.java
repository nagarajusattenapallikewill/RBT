package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class GNOCAlarmAggregator {
	private static DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final Logger LOGGER = Logger.getLogger(GNOCAlarmAggregator.class);

	public static void main(String[] args) {
		LOGGER.info("time before the process starts. " + formatter.format(System.currentTimeMillis()));
		new GNOCAlarmAggregator().process();
	}

	private List<String> bucketList = null;
	private List<String> site1DeviceList = null;
	private List<String> site2DeviceList = null;
	private String toWrite;

	public void process() {
		LOGGER.info("time when entering the process method. " + formatter.format(System.currentTimeMillis()));
		if (GNOCPropertyReader.checkNullValues()) {
			try {
				site1DeviceList = Arrays.asList(GNOCPropertyReader.siteName1_devices.split(","));
				LOGGER.info("site 1 device list is " + site1DeviceList);
				site2DeviceList = Arrays.asList(GNOCPropertyReader.siteName2_devices.split(","));
				LOGGER.info("site 2 device list is " + site2DeviceList);
				bucketList = Arrays.asList(GNOCPropertyReader.buckets.split(","));
				LOGGER.info("bucket list is " + bucketList);
				LOGGER.info("time after reading devices and buckets. " + formatter.format(System.currentTimeMillis()));
				while (true) {
					GNOCDBUtility dbUtility = new GNOCDBUtility();
					dbUtility.initConnAndStmt();
					LOGGER.info("time after initialising conn and stmt. "
							+ formatter.format(System.currentTimeMillis()));
					String statusQuery = "select evid , device , firstTime , severity , summary , message, eventClass, eventKey, component from status";
					BufferedWriter brSite1 = null;
					BufferedWriter brSite2 = null;
					BufferedWriter currentWriter = null;
					try {

						LOGGER.info("executing query " + statusQuery);
						ResultSet statusRSet = dbUtility.getStatusTableStmt().executeQuery(statusQuery);
					
						brSite1 = new BufferedWriter(new FileWriter(GNOCPropertyReader.configPath
								+ GNOCPropertyReader.siteName1 + "_SORIA.txt"));
						
						brSite2 = new BufferedWriter(new FileWriter(GNOCPropertyReader.configPath
								+ GNOCPropertyReader.siteName2 + "_SORIA.txt"));
					
						while (statusRSet.next()) {
							String deviceStr = statusRSet.getString("device");
							if (site1DeviceList.contains(deviceStr))
								currentWriter = brSite1;
							else if (site2DeviceList.contains(deviceStr))
								currentWriter = brSite2;
							else
								continue;

							String eventClass = statusRSet.getString("eventClass");
							if (!bucketList.contains(eventClass)) {
								LOGGER.debug(eventClass + " not present in the bucket list, ignoring the row");
								continue;
							}
							String eventId = statusRSet.getString("evid");
							LOGGER.info("event id is:" + eventId);
							Double firstTime = statusRSet.getDouble("firstTime");
							LOGGER.info("timestamp from DB is " + firstTime);
							if(firstTime <= 0){
								LOGGER.info("alarm creation time is null for evid " + eventId + " ignoring the record.");
								continue;
							}
							Date alarmCreationTime = getJavaDate(firstTime);
							LOGGER.info("alarm creation time is " + alarmCreationTime);
							
							String alarmId = null;
							ResultSet mappingSelectRSet = dbUtility.getMappingSelectStmt().executeQuery(
									"select * from mapping where evid = '" + eventId + "'");
							if (mappingSelectRSet.next()) {
								alarmId = mappingSelectRSet.getString("alarm_id");
								LOGGER.info("alarm id is:" + alarmId);
							} else {
								dbUtility.getLastSequenceIdStmt().executeUpdate("insert into lastseqid values ()");
								ResultSet getLastSeqIdRSet = dbUtility.getLastSequenceIdStmt().executeQuery(
										"select LAST_INSERT_ID() as last_seq_id from lastseqid");
								if (getLastSeqIdRSet.next()) {
									alarmId = getLastSeqIdRSet.getString("last_seq_id");
									LOGGER.debug("last id is : " + alarmId);
									String deleteLastSeqIdQuery = "delete from lastseqid where last_seq_id < "
											+ alarmId;
									LOGGER.info("deleteLastSeqIdQuery is " + deleteLastSeqIdQuery);
									dbUtility.getDeleteLastSequenceIdStmt().executeUpdate(deleteLastSeqIdQuery);
								}
								dbUtility.getMappingInsertStmt().executeUpdate(
										"insert into mapping values ('" + eventId + "'," + alarmId + ")");
							}

							String component = statusRSet.getString("component");

							if (!component.isEmpty() && component != null && !component.equals("")) {
								toWrite = alarmId + "#" + deviceStr + "," + component + "#"
										+ formatter.format(alarmCreationTime) + "#" + statusRSet.getString("severity")
										+ "#" + statusRSet.getString("summary") + "#" + statusRSet.getString("message");
							} else {
								toWrite = alarmId + "#" + deviceStr + "," + statusRSet.getString("eventKey") + "#"
										+ formatter.format(alarmCreationTime) + "#" + statusRSet.getString("severity")
										+ "#" + statusRSet.getString("summary") + "#" + statusRSet.getString("message");
							}

							currentWriter.write(toWrite);
							currentWriter.newLine();
							currentWriter.flush();
						}

					} catch (IOException e1) {
						LOGGER.error("IO Exception in bufferedwriter.", e1);
					} catch (SQLException e) {
						LOGGER.error("error in executing query", e);
					} finally {
						try {
							brSite1.close();
						} catch (IOException e) {
							LOGGER.info("error in closing buffered writer 1", e);
						}
						try {
							brSite2.close();
						} catch (IOException e) {
							LOGGER.info("error in closing buffered writer 2", e);
						}

						try {
							currentWriter.close();
						} catch (IOException e) {
							LOGGER.info("error in closing current writer", e);
						}

						dbUtility.closeStmtAndConn();
					}
					Thread.sleep(GNOCPropertyReader.sleepDuration);
				}

			} catch (InterruptedException e) {
				LOGGER.error("problem in thread", e);
			}
		} else {
			LOGGER.info("corrupt or improper .properties file.");
		}
	}

	public static Date getJavaDate(double unixDate) {
		LOGGER.info("inside date conversion method.");
		Date time = new Date((long) unixDate * 1000);
		return time;
	}
}