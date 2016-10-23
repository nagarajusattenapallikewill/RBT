package com.onmobile.apps.ringbacktones.daemons;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;

public class GNOCPropertyReader {
	public static String driverName = null;
	public static String dbUrl = null;
	public static String dbUsername = null;
	public static String dbPassword = null;
	public static String buckets = null;
	public static String siteName1 = null;
	public static String siteName2 = null;
	public static String siteName1_devices = null;
	public static String siteName2_devices = null;
	public static String configPath = null;
	public static Long sleepDuration = null;
	public static ResourceBundle resourceBundleGNOC = null;
	public static final String CONFIG_FILE_NAME_GNOC = "GNOCAlarmAggregator";

	private static final Logger LOGGER = Logger.getLogger(GNOCPropertyReader.class);

	public static void readPropValues() {
		LOGGER.info("reading values from properties file");
		try {
			resourceBundleGNOC = ResourceBundle.getBundle(CONFIG_FILE_NAME_GNOC);
			driverName = resourceBundleGNOC.getString("driver_name");
			dbUrl = resourceBundleGNOC.getString("db_url");
			dbUsername = resourceBundleGNOC.getString("db_username");
			dbPassword = resourceBundleGNOC.getString("db_pwd");
			// Changes done for URL Encryption and Decryption
			try {
				if (resourceBundleGNOC.getString("ENCRYPTION_MODEL") != null
						&& resourceBundleGNOC.getString("ENCRYPTION_MODEL")
								.equalsIgnoreCase("yes")) {
					dbUsername = URLEncryptDecryptUtil
							.decryptUserNamePassword(dbUsername);
					dbPassword = URLEncryptDecryptUtil
							.decryptUserNamePassword(dbPassword);
				}
			} catch (MissingResourceException e) {
				LOGGER.error("resource bundle exception: ENCRYPTION_MODEL");
			}
			// End of URL Encryption and Decryption
			siteName1 = resourceBundleGNOC.getString("site_name_1");
			siteName2 = resourceBundleGNOC.getString("site_name_2");
			siteName1_devices = resourceBundleGNOC.getString("site_name_1_devices");
			siteName2_devices = resourceBundleGNOC.getString("site_name_2_devices");
			configPath = resourceBundleGNOC.getString("config_path");
			buckets = resourceBundleGNOC.getString("bucket");
			sleepDuration = Long.valueOf(resourceBundleGNOC.getString("sleep_duration")).longValue();
		} catch (MissingResourceException e) {
			LOGGER.info("Config file " + CONFIG_FILE_NAME_GNOC + " not found in the classpath");
		}
	}

	public static boolean checkNullValues() {
		boolean flag = true;
		GNOCPropertyReader.readPropValues();
		if (GNOCPropertyReader.siteName1 == null || GNOCPropertyReader.siteName1.equals("")) {
			LOGGER.info("site_name_1 is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.siteName2 == null || GNOCPropertyReader.siteName2.equals("")) {
			LOGGER.info("site_name_2 is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.siteName1_devices == null || GNOCPropertyReader.siteName1_devices.equals("")) {
			LOGGER.info("site_name_1_devices is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.siteName2_devices == null || GNOCPropertyReader.siteName2_devices.equals("")) {
			LOGGER.info("site_name_2_devices is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.configPath == null || GNOCPropertyReader.configPath.equals("")) {
			LOGGER.info("config_path is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.driverName == null || GNOCPropertyReader.driverName.equals("")) {
			LOGGER.info("driver_name is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.dbUrl == null || GNOCPropertyReader.dbUrl.equals("")) {
			LOGGER.info("db_url is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.dbPassword == null || GNOCPropertyReader.dbPassword.equals("")) {
			LOGGER.info("db_pwd is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.dbUsername == null || GNOCPropertyReader.dbUsername.equals("")) {
			LOGGER.info("db_username is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.sleepDuration == null || GNOCPropertyReader.sleepDuration.equals("")) {
			LOGGER.info("sleep_duration is not defined in the config");
			flag = false;
		}
		if (GNOCPropertyReader.buckets == null || GNOCPropertyReader.buckets.equals("")) {
			LOGGER.info("bucket is not defined in the config");
			flag = false;
		}
		return flag;
	}
}