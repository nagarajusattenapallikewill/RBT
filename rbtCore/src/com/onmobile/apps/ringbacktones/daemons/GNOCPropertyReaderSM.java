package com.onmobile.apps.ringbacktones.daemons;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class GNOCPropertyReaderSM {
	
	public static String ftpUrl = null;
	public static String ftpUsername = null;
	public static String ftpPassword = null;
	public static String ftpDirectory = null;
	public static String buckets = null;
	public static String siteName1 = null;
	public static String siteName2 = null;
	public static String siteName1_devices = null;
	public static String siteName2_devices = null;
	public static String configPath = null;
	public static Long sleepDuration = null;
	public static ResourceBundle resourceBundleGNOC = null;
	public static String raiseTicketUrl = null;
	public static String clearTicketUrl = null;
	public static String clearAckUrl = null;
	public static String raiseAckUrl = null;
	public static String useProxy = null;
	public static String proxyHost = null;
	public static String proxyPort = null;
	public static String soTimeout = null;
	public static String connectionTimeout = null;
	public static String maxTotalConnections = null;
	public static String maxHostConnections = null;
	public static String severityMapping = null;
	public static String timestamp_format = null;
	
	public static final String CONFIG_FILE_NAME_GNOC = "GNOCAlarmAggregatorSM";

	private static final Logger LOGGER = Logger.getLogger(GNOCPropertyReaderSM.class);

	public static void readPropValues() {
		LOGGER.info("getting values from returnValue method and assigning to corresponding variables.");
		
			resourceBundleGNOC = ResourceBundle.getBundle(CONFIG_FILE_NAME_GNOC);
			raiseTicketUrl = GNOCPropertyReaderSM.returnValue("raise_ticket_url");
			clearTicketUrl = GNOCPropertyReaderSM.returnValue("clear_ticket_url");
			raiseAckUrl = GNOCPropertyReaderSM.returnValue("raise_ack_url");
			clearAckUrl = GNOCPropertyReaderSM.returnValue("clear_ack_url");
			ftpUrl = GNOCPropertyReaderSM.returnValue("ftp_host_name");
			ftpUsername = GNOCPropertyReaderSM.returnValue("ftp_username");
			ftpPassword = GNOCPropertyReaderSM.returnValue("ftp_pwd");
			ftpDirectory = GNOCPropertyReaderSM.returnValue("ftp_directory");
			timestamp_format = GNOCPropertyReaderSM.returnValue("timestamp_format");
			siteName1 = GNOCPropertyReaderSM.returnValue("site_name_1");
			siteName2 = GNOCPropertyReaderSM.returnValue("site_name_2");
			siteName1_devices = GNOCPropertyReaderSM.returnValue("site_name_1_devices");
			siteName2_devices = GNOCPropertyReaderSM.returnValue("site_name_2_devices");
			configPath = GNOCPropertyReaderSM.returnValue("config_path");
			buckets = GNOCPropertyReaderSM.returnValue("bucket");
			useProxy = GNOCPropertyReaderSM.returnValue("useProxy");
			proxyHost = GNOCPropertyReaderSM.returnValue("proxyHost");
			proxyPort = GNOCPropertyReaderSM.returnValue("proxyPort");
			soTimeout = GNOCPropertyReaderSM.returnValue("soTimeout");
			connectionTimeout = GNOCPropertyReaderSM.returnValue("connectionTimeout");
			maxTotalConnections = GNOCPropertyReaderSM.returnValue("maxTotalConnections");
			maxHostConnections = GNOCPropertyReaderSM.returnValue("maxHostConnections");
			severityMapping = GNOCPropertyReaderSM.returnValue("severity_value_mapping");
			sleepDuration = Long.valueOf(GNOCPropertyReaderSM.returnValue("sleep_duration")).longValue();
		
	}

	public static boolean checkNullValues() {
		boolean flag = true;
		GNOCPropertyReaderSM.readPropValues();
		if (GNOCPropertyReaderSM.siteName1 == null || GNOCPropertyReaderSM.siteName1.equals("")) {
			LOGGER.info("site_name_1 is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.siteName2 == null || GNOCPropertyReaderSM.siteName2.equals("")) {
			LOGGER.info("site_name_2 is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.siteName1_devices == null || GNOCPropertyReaderSM.siteName1_devices.equals("")) {
			LOGGER.info("site_name_1_devices is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.siteName2_devices == null || GNOCPropertyReaderSM.siteName2_devices.equals("")) {
			LOGGER.info("site_name_2_devices is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.configPath == null || GNOCPropertyReaderSM.configPath.equals("")) {
			LOGGER.info("config_path is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.timestamp_format == null || GNOCPropertyReaderSM.timestamp_format.equals("")) {
			LOGGER.info("timestamp_format is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.raiseTicketUrl== null || GNOCPropertyReaderSM.raiseTicketUrl.equals("")) {
			LOGGER.info("raise ticket url is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.clearTicketUrl == null || GNOCPropertyReaderSM.clearTicketUrl.equals("")) {
			LOGGER.info("clear ticket url is not defined in the config");
			flag = false;
		}
//		if (GNOCPropertyReaderSM.raiseAckUrl== null || GNOCPropertyReaderSM.raiseAckUrl.equals("")) {
//			LOGGER.info("raise ack url is not defined in the config");
//			flag = false;
//		}
//		if (GNOCPropertyReaderSM.clearAckUrl == null || GNOCPropertyReaderSM.clearAckUrl.equals("")) {
//			LOGGER.info("clear ack url is not defined in the config");
//			flag = false;
//		}
		if (GNOCPropertyReaderSM.ftpUrl == null || GNOCPropertyReaderSM.ftpUrl.equals("")) {
			LOGGER.info("ftp_url is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.ftpPassword == null || GNOCPropertyReaderSM.ftpPassword.equals("")) {
			LOGGER.info("ftp_pwd is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.ftpUsername == null || GNOCPropertyReaderSM.ftpUsername.equals("")) {
			LOGGER.info("ftp_username is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.ftpDirectory == null || GNOCPropertyReaderSM.ftpDirectory.equals("")) {
			LOGGER.info("ftp_directory is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.sleepDuration == null || GNOCPropertyReaderSM.sleepDuration.equals("")) {
			LOGGER.info("sleep_duration is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReaderSM.buckets == null || GNOCPropertyReaderSM.buckets.equals("")) {
			LOGGER.info("bucket is not defined in the config");
			flag = false;
		}
//		if (GNOCPropertyReaderSM.useProxy == null || GNOCPropertyReaderSM.useProxy.equals("")) {
//			LOGGER.info("use proxy is not defined in the config.");
//			//flag = false;
//		}
		if(Boolean.parseBoolean(useProxy)){
			if (GNOCPropertyReaderSM.proxyHost == null || GNOCPropertyReaderSM.proxyHost.equals("")) {
				LOGGER.info("proxy host is not defined in the config");
				flag = false;
			}
			if (GNOCPropertyReaderSM.proxyPort == null || GNOCPropertyReaderSM.proxyPort.equals("")) {
				LOGGER.info("proxy port is not defined in the config");
				flag = false;
			}
			if (GNOCPropertyReaderSM.connectionTimeout == null || GNOCPropertyReaderSM.connectionTimeout.equals("")) {
				LOGGER.info("connection timeout is not defined in the config");
				flag = false;
			}
			if (GNOCPropertyReaderSM.soTimeout == null || GNOCPropertyReaderSM.soTimeout.equals("")) {
				LOGGER.info("socket time out is not defined in the config");
				flag = false;
			}
			if (GNOCPropertyReaderSM.maxHostConnections == null || GNOCPropertyReaderSM.maxHostConnections.equals("")) {
				LOGGER.info("max host connection is not defined in the config");
				flag = false;
			}
			if (GNOCPropertyReaderSM.maxTotalConnections == null || GNOCPropertyReaderSM.maxTotalConnections.equals("")) {
				LOGGER.info("max total connection is not defined in the config");
				flag = false;
			}
		}else{
			LOGGER.info("use proxy is false or not defined.");
		}
		if (GNOCPropertyReaderSM.severityMapping== null || GNOCPropertyReaderSM.severityMapping.equals("")) {
			LOGGER.info("severity mapping is not defined in the config");
			flag = false;
		}
		
		return flag;
	}
	
	private static String returnValue(String propertyName){
		LOGGER.info("reading value for " + propertyName + " from properties file");
		String value = null;
		try {
			value = resourceBundleGNOC.getString(propertyName);
		} catch (MissingResourceException e) {
			LOGGER.error(propertyName + " not found in the properties file. So returning null");
		}
		return value;
		
	}
}