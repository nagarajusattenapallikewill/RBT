package com.onmobile.apps.ringbacktones.daemons.nametunes;

import java.io.File;
import java.util.Date;

public class NameTuneConstants {

	static Date date = new Date();
	static PropertiesProvider propertiesProvider = new PropertiesProvider("NameTunesConfig.properties");

	
	public static String LOCAL_BASE_DIRECTORY = null;
	public static String NEW_REQ_FILE_NAME = null;
	public static String NEW_REQ_DIR = null;
	public static String FAILURE_REPORT_LOG_DIR = null;
	public static String FAILURE_REPORT_FILE_NAME = null;
	public static String COMPLETED_REPORT_LOG_DIR = null;
	public static String COMPLETED_REPORT_FILE_NAME = null;
	public static String NEW_REQ_FILE_HEADER = null;
	public static String NAME_TUNE_COMPLTED_FAILURE_HEADER = null;
	
	public static String FTP_UPLOAD_NEW_REQ_SERVER_IP = null;
	public static String FTP_UPLOAD_NEW_REQ_SERVER_USERNAME = null;
	public static String FTP_UPLOAD_NEW_REQ_SERVER_PASSWORD = null;
	public static String FTP_UPLOAD_NEW_REQ_SERVER_PATH = null;

	public static String FTP_UPLOAD_REPORTS_SERVER_IP = null;
	public static String FTP_UPLOAD_REPORTS_SERVER_USERNAME = null;
	public static String FTP_UPLOAD_REPORTS_SERVER_PASSWORD = null;
	public static String FTP_UPLOAD_COMPLETED_REPORTS_SERVER_PATH = null;
	public static String FTP_UPLOAD_FAILURE_REPORTS_SERVER_PATH = null;

	public static String FTP_DOWNLOAD_PROCESSED_FILE_SERVER_IP = null;
	public static String FTP_DOWNLOAD_PROCESSED_FILE_SERVER_USERNAME = null;
	public static String FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PASSWORD = null;
	public static String FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PATH = null;
	public static String FTP_DOWNLOAD_PROCESSED_FILE_SERVER_FILENAME = null;
	
	public static String NEW_REQ_PROCESSED_DIR = null;
	public static String DEFAULT_CATEGORY_ID = null;
	public static String SELECTION_MODE = null;
	public static String MAX_RETRY_COUNT=null;
	public static String MAX_RETRY_TIME = null;
	public static int PROCESSING_THREAD_SLEEP_TIME;
	public static int NEW_REQ_THREAD_SLEEP_TIME;
	static {
		NEW_REQ_THREAD_SLEEP_TIME = propertiesProvider.getPropIntValue("NEW_REQ_THREAD_SLEEP_TIME");
		LOCAL_BASE_DIRECTORY = propertiesProvider.getPropertyValue("LOCAL_BASE_DIRECTORY");
		NEW_REQ_FILE_NAME = propertiesProvider.getPropertyValue("NEW_REQ_FILE");
		NEW_REQ_DIR = propertiesProvider.getPropertyValue("NEW_REQ_DIR");
		FAILURE_REPORT_LOG_DIR = propertiesProvider.getPropertyValue("FAILURE_REPORT_LOG_DIR");
		FAILURE_REPORT_FILE_NAME = propertiesProvider.getPropertyValue("FAILURE_REPORT_FILE_NAME");
		COMPLETED_REPORT_LOG_DIR = propertiesProvider.getPropertyValue("COMPLETED_REPORT_LOG_DIR");
		COMPLETED_REPORT_FILE_NAME = propertiesProvider.getPropertyValue("COMPLETED_REPORT_FILE_NAME");
		NEW_REQ_FILE_HEADER = propertiesProvider.getPropertyValue("NEW_REQ_FILE_HEADER");
		NAME_TUNE_COMPLTED_FAILURE_HEADER = propertiesProvider.getPropertyValue("NAME_TUNE_COMPLTED_FAILURE_HEADER");
		
		FTP_UPLOAD_NEW_REQ_SERVER_IP = propertiesProvider.getPropertyValue("FTP_UPLOAD_NEW_REQ_SERVER_IP");
		FTP_UPLOAD_NEW_REQ_SERVER_USERNAME = propertiesProvider.getPropertyValue("FTP_UPLOAD_NEW_REQ_SERVER_USERNAME");
		FTP_UPLOAD_NEW_REQ_SERVER_PASSWORD = propertiesProvider.getPropertyValue("FTP_UPLOAD_NEW_REQ_SERVER_PASSWORD");
		FTP_UPLOAD_NEW_REQ_SERVER_PATH = propertiesProvider.getPropertyValue("FTP_UPLOAD_NEW_REQ_SERVER_PATH");

		FTP_UPLOAD_REPORTS_SERVER_IP = propertiesProvider.getPropertyValue("FTP_UPLOAD_REPORTS_SERVER_IP");
		FTP_UPLOAD_REPORTS_SERVER_USERNAME = propertiesProvider.getPropertyValue("FTP_UPLOAD_REPORTS_SERVER_USERNAME");
		FTP_UPLOAD_REPORTS_SERVER_PASSWORD = propertiesProvider.getPropertyValue("FTP_UPLOAD_REPORTS_SERVER_PASSWORD");
		FTP_UPLOAD_COMPLETED_REPORTS_SERVER_PATH = propertiesProvider.getPropertyValue("FTP_UPLOAD_COMPLETED_REPORTS_SERVER_PATH");
		FTP_UPLOAD_FAILURE_REPORTS_SERVER_PATH = propertiesProvider.getPropertyValue("FTP_UPLOAD_FAILURE_REPORTS_SERVER_PATH");

		FTP_DOWNLOAD_PROCESSED_FILE_SERVER_IP = propertiesProvider.getPropertyValue("FTP_DOWNLOAD_PROCESSED_FILE_SERVER_IP");
		FTP_DOWNLOAD_PROCESSED_FILE_SERVER_USERNAME = propertiesProvider.getPropertyValue("FTP_DOWNLOAD_PROCESSED_FILE_SERVER_USERNAME");
		FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PASSWORD = propertiesProvider.getPropertyValue("FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PASSWORD");
		FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PATH = propertiesProvider.getPropertyValue("FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PATH");
		FTP_DOWNLOAD_PROCESSED_FILE_SERVER_FILENAME = propertiesProvider.getPropertyValue("FTP_DOWNLOAD_PROCESSED_FILE_SERVER_FILENAME");
		
		
		
		NEW_REQ_PROCESSED_DIR = propertiesProvider.getPropertyValue("NEW_REQ_PROCESSED_DIR");
		DEFAULT_CATEGORY_ID = propertiesProvider.getPropertyValue("CATEGORY_ID");
		SELECTION_MODE = propertiesProvider.getPropertyValue("SELECTION_MODE");
		MAX_RETRY_COUNT = propertiesProvider.getPropertyValue("MAX_RETRY_COUNT");
		MAX_RETRY_TIME  = propertiesProvider.getPropertyValue("MAX_RETRY_TIMEGAP");
		
		PROCESSING_THREAD_SLEEP_TIME = propertiesProvider.getPropIntValue("PROCESSING_THREAD_SLEEP_TIME");
	}

}
