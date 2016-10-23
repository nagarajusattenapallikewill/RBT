package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.common.exception.OnMobileException;

public class ExpiredContentDeactivationDaemon extends TimerTask /*Thread*/ {

	private static final Logger LOGGER = Logger
			.getLogger(ExpiredContentDeactivationDaemon.class);
//	private RBTDaemonManager rbtDaemonManager = null;
	private static final String DAEMON = "DAEMON";
//	private int sleepTime = 0;
	private int scheduleTimeHour = 0;
	private int scheduleTimeMin = 0;
	private int intervalTime = 0;
	private int noOfDaysInAdvance = 0;
//	private boolean isAlive = false;
	private boolean isVFTurkey = false;
	private boolean isIdeaIndia = false;
	private static final String CONTENT_EXPIRY_FILE_PATH = "CONTENT_EXPIRY_FILE_PATH";
	private static final String CONTENT_EXPIRY_FILE_NAME_FORMAT = "CONTENT_EXPIRY_FILE_NAME_FORMAT";
	private static final String MODE_FOR_CONTENT_EXPIRY_DAEMON = "MODE_FOR_CONTENT_EXPIRY_DAEMON";
//	private static final String SLEEP_TIME_FOR_CONTENT_DAEMON = "SLEEP_TIME_FOR_CONTENT_DAEMON";
	private static final String LOCK_FILE_NAME_FORMAT = "LOCK_FILE_NAME_FORMAT";
	private static final String  CONTENT_EXPIRY_SCHEDULE_TIME_HOURS= "CONTENT_EXPIRY_SCHEDULE_TIME_HOURS";
	private static final String  CONTENT_EXPIRY_SCHEDULE_TIME_MIN= "CONTENT_EXPIRY_SCHEDULE_TIME_MIN";
	private static final String INTERVAL_TIME_BETWEEN_NEXT_RUN = "INTERVAL_TIME_BETWEEN_NEXT_RUN";
	private static final String  NUMBER_OF_DAYS_FOR_RENEWAL = "NUMBER_OF_DAYS_FOR_RENEWAL";
	private static final String  ENABLE_VF_TURKEY_CONTENT_EXPIRY = "ENABLE_VF_TURKEY_CONTENT_EXPIRY";
	private static final String  ENABLE_IDEA_INDIA_CONTENT_EXPIRY = "ENABLE_IDEA_INDIA_CONTENT_EXPIRY";
	
	private static final String  CONTENT_EXPIRY_LOCAL_FILE_PATH = "CONTENT_EXPIRY_LOCAL_FILE_PATH";
//	private static final String  CONTENT_EXPIRY_TARGET_FOLDER_NAME = "CONTENT_EXPIRY_TARGET_FOLDER_NAME";
	private static final String  CONTENT_EXPIRY_DOWNLOAD_FILE_PATH = "CONTENT_EXPIRY_DOWNLOAD_FILE_PATH";
	private static final String  CONTENT_EXPIRY_FTP_IP = "CONTENT_EXPIRY_FTP_IP";
	private static final String  CONTENT_EXPIRY_FTP_USERNAME = "CONTENT_EXPIRY_FTP_USERNAME";
	private static final String  CONTENT_EXPIRY_FTP_PASSWORD = "CONTENT_EXPIRY_FTP_PASSWORD";
	private static final String  CONTENT_EXPIRY_FTP_WORKING_DIR = "CONTENT_EXPIRY_FTP_WORKING_DIR";
	private static final String  CIRCLE_NAME_FOR_CONTENT_EXPIRY = "CIRCLE_NAME_FOR_CONTENT_EXPIRY";
	private static final String  TEXT_FOR_RENEWED_CONTENT = "TEXT_FOR_RENEWED_CONTENT";
	private static final String  SONG_NAME = "%SONG_NAME%";

	private String senderNumber = null;
	private String renewedContentText = null;
	private String sharedFilePath = null;
	private String fileNameFormat = null;
	private String mode = null;
	private String lockFileNameFormat = null;
	private String localFilePath = null;
//	private String targetFolderName = null;
	private String downloadFilePath = null;
	private String FTP_IP = null;
	private String FTP_USERNAME = null;
	private String FTP_PWD = null;
	private String workingDirectory = null;
	private String circleName = null;

	public ExpiredContentDeactivationDaemon() {
		init();
//		isAlive = true;
	}

	public void init() {
		// readFile("E:/test.csv");
		try {
			sharedFilePath = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_FILE_PATH, null);
			fileNameFormat = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_FILE_NAME_FORMAT, null);
			mode = RBTParametersUtils.getParamAsString(DAEMON,
					MODE_FOR_CONTENT_EXPIRY_DAEMON, "CCC");
			lockFileNameFormat = RBTParametersUtils.getParamAsString(DAEMON,
					LOCK_FILE_NAME_FORMAT, null);
			localFilePath = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_LOCAL_FILE_PATH, null);
//			targetFolderName = RBTParametersUtils.getParamAsString(DAEMON,
//					CONTENT_EXPIRY_TARGET_FOLDER_NAME, null);
			downloadFilePath = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_DOWNLOAD_FILE_PATH, null);
			FTP_IP = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_FTP_IP, null);
			FTP_USERNAME = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_FTP_USERNAME, null);
			FTP_PWD = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_FTP_PASSWORD, null);
			workingDirectory = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_FTP_WORKING_DIR, null);
			circleName = RBTParametersUtils.getParamAsString(DAEMON,
					CIRCLE_NAME_FOR_CONTENT_EXPIRY, null);
			String noOfDaysStr = RBTParametersUtils.getParamAsString(DAEMON,
					NUMBER_OF_DAYS_FOR_RENEWAL, "2");
//			String sleepTimeStr = RBTParametersUtils.getParamAsString(DAEMON,
//					SLEEP_TIME_FOR_CONTENT_DAEMON, "36000");
			String scheduleTimeHrStr = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_SCHEDULE_TIME_HOURS, "11");
			String scheduleTimeMinStr = RBTParametersUtils.getParamAsString(DAEMON,
					CONTENT_EXPIRY_SCHEDULE_TIME_MIN, "1");
			String intervalTimeStr = RBTParametersUtils.getParamAsString(DAEMON,
					INTERVAL_TIME_BETWEEN_NEXT_RUN, "60");
			String isVFTurkeyStr = RBTParametersUtils.getParamAsString(DAEMON,
					ENABLE_VF_TURKEY_CONTENT_EXPIRY, "FALSE");
			String isIdeaIndiaStr = RBTParametersUtils.getParamAsString(DAEMON,
					ENABLE_IDEA_INDIA_CONTENT_EXPIRY, "FALSE");
			
			renewedContentText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(TEXT_FOR_RENEWED_CONTENT, null, null);
			senderNumber = RBTParametersUtils.getParamAsString("DAEMON", "SENDER_NO", null); 
//			if (null != sleepTimeStr) {
//				sleepTime = Integer.parseInt(sleepTimeStr);
//			} else {
//				LOGGER.warn("Parameter " + SLEEP_TIME_FOR_CONTENT_DAEMON
//						+ " not configured. So taking default as 36000");
//			}
			
			if(null != scheduleTimeHrStr){
				scheduleTimeHour = Integer.parseInt(scheduleTimeHrStr);
			}else {
				LOGGER.warn("Parameter " + CONTENT_EXPIRY_SCHEDULE_TIME_HOURS
						+ " not configured. So taking default as 11");
			}
			if(null != scheduleTimeMinStr){
				scheduleTimeMin = Integer.parseInt(scheduleTimeMinStr);
			}else {
				LOGGER.warn("Parameter " + CONTENT_EXPIRY_SCHEDULE_TIME_MIN
						+ " not configured. So taking default as 1");
			}
			if(null != intervalTimeStr){
				intervalTime = Integer.parseInt(intervalTimeStr);
			}else {
				LOGGER.warn("Parameter " + INTERVAL_TIME_BETWEEN_NEXT_RUN
						+ " not configured. So taking default as 1 hour");
			}
			if(null != noOfDaysStr){
				noOfDaysInAdvance = Integer.parseInt(noOfDaysStr);
			}else {
				LOGGER.warn("Parameter " + NUMBER_OF_DAYS_FOR_RENEWAL
						+ " not configured. So taking default as 2");
			}
			if(null != isVFTurkeyStr){
				isVFTurkey = Boolean.parseBoolean(isVFTurkeyStr);
			}else {
				LOGGER.warn("Parameter " + ENABLE_VF_TURKEY_CONTENT_EXPIRY
						+ " not configured. So taking default as false");
			}
			if(null != isIdeaIndiaStr){
				isIdeaIndia = Boolean.parseBoolean(isIdeaIndiaStr);
			}else {
				LOGGER.warn("Parameter " + ENABLE_IDEA_INDIA_CONTENT_EXPIRY
						+ " not configured. So taking default as false");
			}
		} catch (NumberFormatException e) {
			LOGGER.error("exception in init() " ,e);
		}
	}
	
	public void start() {
		LOGGER.info("Scheduling ExpiredContentDeactivationDaemon...");
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, scheduleTimeHour);
		calendar.set(Calendar.MINUTE, scheduleTimeMin);
		calendar.set(Calendar.SECOND, 0);
		Date startDate = calendar.getTime();
		LOGGER.info("Start date is " + startDate);
		Timer timer = new Timer(ExpiredContentDeactivationDaemon.class.getSimpleName());
		long intervalTimeInMilli = intervalTime * 60 * 1000;
		timer.scheduleAtFixedRate(this, startDate, intervalTimeInMilli);
		
		LOGGER.info("ExpiredContentDeactivationDaemon has been scheduled");
	}

	public void stop() {
		this.cancel();
	}
//	public ExpiredContentDeactivationDaemon(RBTDaemonManager rbtDaemonManager) {
//		init();
//		this.rbtDaemonManager = rbtDaemonManager;
//	}

	public void run() {
		try {
			if (null != fileNameFormat && !fileNameFormat.isEmpty()
					&& null != lockFileNameFormat
					&& !lockFileNameFormat.isEmpty()) {
				LOGGER.info("content expiry file format is  " + fileNameFormat + " lock file format is " + lockFileNameFormat);
//				while ((rbtDaemonManager != null && rbtDaemonManager.isAlive())
//						|| isAlive) {
				if (isIdeaIndia) {
					LOGGER
							.info("going to invoke Idea India content expiry logic");
					if (null != localFilePath && !localFilePath.isEmpty()
//							&& null != targetFolderName
//							&& !targetFolderName.isEmpty()
							&& null != downloadFilePath
							&& !downloadFilePath.isEmpty() && null != FTP_IP
							&& !FTP_IP.isEmpty() && null != FTP_PWD
							&& !FTP_PWD.isEmpty() && null != FTP_USERNAME
							&& !FTP_USERNAME.isEmpty()
							&& null != workingDirectory
							&& !workingDirectory.isEmpty() && null != circleName
							&& !circleName.isEmpty()){
//						readCSVFileFromFTP(localFilePath, targetFolderName);
						readCSVFileFromFTP(localFilePath);
					}else{
						LOGGER.error("Please check the ParametersCacheManager Logs. Some mandatory config is either null or missing.");
					}
				} else if (isVFTurkey) {
					LOGGER
							.info("going to invoke VF Turkey content expiry logic");
					if (null != sharedFilePath && !sharedFilePath.isEmpty()) {
						LOGGER.info("content expiry file is in the " + sharedFilePath);
							readCSVFileFromPath();
					} else {
						LOGGER
								.error("Unable to process.Cannot retrieve the file as no valid shared path is given.");
					}
						
				} else {
					LOGGER
							.warn("can't invoke any logic as no proper config found");
				}
					//sleep();
				//}
			} else {
				LOGGER
						.error("Unable to process.Cannot retrieve the file name format as no valid file name format is given either for lock file or csv file.");
			}
		} catch (Exception e) {
			LOGGER.error("Error while running daemon", e);
		}
	}

	public void readCSVFileFromPath() {
		DateFormat dateFormat = new SimpleDateFormat(fileNameFormat);
		DateFormat lockFileFormat = new SimpleDateFormat(lockFileNameFormat);
		Date newDate = getProcessDate();
		LOGGER.info("Process date is " + newDate);
		try {
			boolean isFileProcessSuccess = false;
			int numberOfFailedRecords = 0;
			int invalidRecords = 0;
			boolean response = false;
			File file = new File(sharedFilePath);
			// filter to get only *.csv
			File[] files = file.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					String fileName = file.getName();
					boolean accept = false;
					if (file.isFile()) {
						accept = (fileName.endsWith(".csv") || fileName
								.endsWith(".CSV"));
					}
					if (accept) {
						LOGGER.debug("It is a csv file " + fileName + ":"
								+ accept);
					} else {
						LOGGER.debug("It is not a csv file " + fileName + ":"
								+ accept);
					}
					return accept;
				}

			});
			if(files.length == 0 || files == null){
				LOGGER.warn("No csv files in the specified path " + sharedFilePath);
			}
			for (File fl : files) {
				String fileName = fl.toString();
				LOGGER.info("absolute file name is " + fileName);
				String nameOfFileWithExt = fl.getName();
				LOGGER.info("name of file with ext " + nameOfFileWithExt);
				String justNameOfFile = nameOfFileWithExt.substring(0,
						nameOfFileWithExt.lastIndexOf("."));
				LOGGER.info("just name of the file is " + justNameOfFile);

				Date fileNameDate = dateFormat.parse(justNameOfFile);
				LOGGER.info("file name date is " + fileNameDate);
				String lockFileNameDate = lockFileFormat.format(fileNameDate);
				LOGGER.info("date obtained from file name is "
						+ lockFileNameDate);
				String lockFileName = sharedFilePath + lockFileNameDate
						+ ".lock";
				File lockFile = new File(lockFileName);
				boolean isTrue = lockFile.exists();
				LOGGER.info("lock file name is " + lockFileName);
				LOGGER.info("boolean value is " + isTrue);
				if (lockFile.exists()) {
					LOGGER.info("yaaay. Lock file corressponding to "
							+ fileName + " exists.");

					if (fileNameDate.before(newDate)) {
						CSVReader csvFileReader = null;
						try {
							csvFileReader = new CSVReader(new FileReader(
									fileName));
							String[] columns = null;
							int row = 0;
							while ((columns = csvFileReader.readNext()) != null) {
								row++;
								if (columns.length < 24) {
									LOGGER.warn("Invalid line at row " + row);
									continue;
								}
								String msisdn = columns[1];
								String refId = columns[23];
								LOGGER.info("msisdn is " + msisdn);
								LOGGER.info("internal ref id is " + refId);
								RBTDBManager rbtDBManager = RBTDBManager
										.getInstance();

								SubscriberDownloads subDownloadByRefId = rbtDBManager
										.getSubscriberDownloadByRefID(msisdn,
												refId);
								if (subDownloadByRefId != null) {
									String promoId = subDownloadByRefId
											.promoId();
									int catId = subDownloadByRefId.categoryID();
									int catType = subDownloadByRefId
											.categoryType();
									char status = subDownloadByRefId
											.downloadStatus();
									if (status != 'd' && status != 'x'
											&& status != 's') {
										LOGGER.info("This record with ref id : "
												+ refId
												+ " is going for deact.");
										response = rbtDBManager
												.expireSubscriberDownload(
														msisdn, promoId, catId,
														catType, mode, null, false);

										LOGGER.info("resposne for deactivation is "
												+ response);
										if (response) {
											LOGGER.info("webservice response is success.");
										} else {
											LOGGER.info("webservice response is failure.");
											numberOfFailedRecords++;
										}
									} else {
										LOGGER.info("record is already deactive.Ignoring");
									}
								} else {
									LOGGER.info("no valid record obtained from the downloads table for ref id "
											+ refId);
									invalidRecords++;
								}
							}
						} finally {
							if (null != csvFileReader) {
								csvFileReader.close();
							}
						}
					}else{
						LOGGER.info("this file " + fileName + " is after the execution time.");
						continue;
					}
					if (invalidRecords > 0) {
						LOGGER.info("number of invalid records are "
								+ invalidRecords + " from file " + fileName);
					}
					if (numberOfFailedRecords > 0) {
						LOGGER
								.info("number of records whose processing failed "
										+ numberOfFailedRecords);
					} else {
						LOGGER
								.info("entire file processed successfully. number of records that failed to process is "
										+ numberOfFailedRecords);
						isFileProcessSuccess = true;
					}
					if (isFileProcessSuccess) {
						LOGGER.info("going to rename the file.");
						if (DaemonUtility.renameFile(fl)) {
							LOGGER.info("file renamed successfully");
						} else {
							LOGGER
									.error("error in file rename. could not change the extension.");
						}
					} else {
						LOGGER
								.error("file processing failed. will try again after an hour.");
					}
				}
			}

		} catch (FileNotFoundException e) {
			LOGGER.error("File not found ", e);
		} catch (IOException e) {
			LOGGER.error("IO Exception occurred ", e);
		} catch (ParseException e) {
			LOGGER
					.error(
							"Parse Exception occurred.May be File Name Format Mismatch.Please Check ",
							e);
		}

	}

//	private void sleep() {
//		try {
//			LOGGER.info("Sleeping for " + sleepTime);
//			Thread.sleep(sleepTime);
//			LOGGER.info("Continue after sleep");
//		} catch (Exception e) {
//			LOGGER.error("Exception e: " + e.getMessage(), e);
//		}
//	}
	
//	public void readCSVFileFromFTP(String localFilePath,String localFolder){
	public void readCSVFileFromFTP(String localFilePath){
		LOGGER.info("inside method readCSVFileFromFTP.");
		try {
			String finalFileName = getFinalFileName();
			File localFolder = new File(localFilePath);
			File processedFile = new File(localFolder, finalFileName+".processed");
			if (processedFile.exists()) {
				LOGGER.error("Processed file "
						+ processedFile.getAbsolutePath()
						+ " already exists. So not going to process "
						+ finalFileName);
					return;
			}
			File file = getFileForSendingDeactRequest(finalFileName);
			String fileName = file.getName();
	//		String finalFilePath = localFilePath + localFolder;
			LOGGER.info("inside method readCSVFileFromFTP.got file name as " + fileName);
			LOGGER.info("inside method readCSVFileFromFTP.got file path as " + localFolder.getAbsolutePath());
			//LOGGER.info("inside method sendSMSText. final file name is " + finalFileName);
			DateFormat dateFormat = new SimpleDateFormat(fileNameFormat);
			//DateFormat lockFileFormat = new SimpleDateFormat(lockFileNameFormat);
			Date newDate = getProcessDate();
		
			boolean isFileProcessSuccess = false;
			int numberOfFailedRecords = 0;
			int invalidRecords = 0;
			boolean response = false;
//			File files = new File(finalFilePath);
			// filter to get only *.csv
			//TODO: create seperate filter
			File[] directoryFiles = localFolder.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					String fileName = file.getName();
					boolean accept = false;
					if (file.isFile()) {
						accept = (fileName.endsWith(".csv") || fileName
								.endsWith(".CSV"));
					}
					if (accept) {
						LOGGER.debug("It is a csv file " + fileName + ":"
								+ accept);
					} else {
						LOGGER.debug("It is not a csv file " + fileName + ":"
								+ accept);
					}
					return accept;
				}

			});
			if(directoryFiles.length == 0 || directoryFiles == null){
				LOGGER.warn("No csv files in the specified path " + localFolder);
				return;
			}
			for (File fl : directoryFiles) {
				String nameOfFileWithExt = fl.getName();
				LOGGER.info("name of file with ext " + nameOfFileWithExt);

				String curDateFormattedString = dateFormat.format(newDate);
				File finalFile = new File(localFolder, fileName+".csv");
					if (fileName.equalsIgnoreCase(circleName+"_"+curDateFormattedString)) {
						CSVReader csvFileReader = null;
						try {
							csvFileReader = new CSVReader(new FileReader(
									finalFile));
							String[] columns = csvFileReader.readNext();
							int row = 0;
							while (columns != null) {
								row++;
								if (columns.length < 24) {
									LOGGER.warn("Invalid line at row " + row);
									continue;
								}
								String msisdn = columns[1];
								String refId = columns[23];
								LOGGER.info("msisdn is " + msisdn
										+ " internal ref id is " + refId);
								RBTDBManager rbtDBManager = RBTDBManager
										.getInstance();

								SubscriberStatus subSelectionByRefId = rbtDBManager
										.getSelectionByRefId(msisdn, refId);
								
								if (subSelectionByRefId != null) {

									String status = subSelectionByRefId
											.selStatus();

									if (!status.equalsIgnoreCase("D")
											&& !status.equalsIgnoreCase("X")
											&& !status.equalsIgnoreCase("P")) {

										// checking for clip expiry.
										boolean isContentExpired = true;
										Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(subSelectionByRefId.subscriberFile());
										boolean isAfter = clip.getClipEndTime().after(new Date());
										if(clip != null && clip.getClipEndTime() != null && isAfter){
											LOGGER.debug("clip.getClipEndTime() :"
													+ clip.getClipEndTime()
													+ " Today Date:" + new Date()
													+ " and condition:" + isAfter
													+ " CLIPID:" + clip.getClipId());
											isContentExpired = false;
										}
										
										if (isContentExpired) {
											LOGGER
													.info("This record with ref id : "
															+ refId
															+ " is going for deact.");
											response = rbtDBManager
													.expireSubscriberSelection(
															msisdn, refId, mode);
	
											LOGGER
													.info("resposne for deactivation is "
															+ response);
											if (response) {
												LOGGER
														.info("webservice response is success.");
											} else {
												LOGGER
														.info("webservice response is failure.");
												numberOfFailedRecords++;
											}
										} else {
											try {
												
												if(renewedContentText != null && renewedContentText.length() > 0){
													LOGGER.info("The smsText for TEXT_FOR_NON_EXPIRED_CONTENT_FOR_SEL_DCT is :" + renewedContentText);
													boolean smsStatus;
													String finalSMS = renewedContentText.replaceAll(SONG_NAME, clip.getClipName());
													LOGGER.info("The sms text:"+ finalSMS);
													smsStatus = Tools.sendSMS(getSenderNumber(subSelectionByRefId.circleId(), senderNumber), subSelectionByRefId.subID(), finalSMS, false);
													LOGGER.info("The sent sms status = "+ smsStatus);
												}
											} catch (OnMobileException e) {
												LOGGER.info("Something went wrong when trying to send a sms"+ e);
											}
										}
									} else {
										LOGGER
												.info("record is already deactive.Ignoring");
									}
							} else {
								LOGGER.info("no valid record obtained from the selections table for ref id "
										+ refId);
								invalidRecords++;
							}
								columns = csvFileReader.readNext();
							}
						} finally {
							if (csvFileReader != null) {
								csvFileReader.close();
							}
						}
					}else{
						LOGGER.info("this file " + fileName + " is after the execution time.");
						continue;
					}
					if (invalidRecords > 0) {
						LOGGER.info("number of invalid records are "
								+ invalidRecords + " from file " + fileName);
					}
					if (numberOfFailedRecords > 0) {
						LOGGER
								.info("number of records whose processing failed "
										+ numberOfFailedRecords);
					} else {
						LOGGER
								.info("entire file processed successfully. number of records that failed to process is "
										+ numberOfFailedRecords);
						isFileProcessSuccess = true;
					}
					if (isFileProcessSuccess) {
						LOGGER.info("going to rename the file.");
						if (DaemonUtility.renameFile(finalFile)) {
							LOGGER.info("file renamed successfully");
						} else {
							LOGGER
									.error("error in file rename. could not change the extension.");
						}
					} else {
						LOGGER
								.error("file processing failed. will try again after an hour.");
					}
				}
		} catch (FileNotFoundException e) {
			LOGGER.error("File not found ", e);
		} catch (IOException e) {
			LOGGER.error("IO Exception occurred ", e);
		}
	}
	
	public static String getSenderNumber(String circleID, String senderNumber) {
		if(circleID != null && circleID.length() > 0) {
			String operatorName = circleID.indexOf("_") != -1 ? circleID.substring(0, circleID.indexOf("_")) : null;
			if(operatorName != null && operatorName.trim().length() > 0) {
				senderNumber = RBTParametersUtils.getParamAsString("DAEMON", operatorName +"_SENDER_NO", senderNumber);
			}
		}
		LOGGER.info("senderNumber :" + senderNumber);
		return senderNumber;
	}


	//TODO: use same API for getting file 
	//@Deprecated
	public File getFileForSendingDeactRequest(String finalFileName) throws IOException{
		LOGGER.info("inside method getFileForSendingDeactRequest. final file name is " + finalFileName);
		if(DaemonUtility.readZIPFileFromFTP(finalFileName+".zip", FTP_IP, FTP_USERNAME, FTP_PWD, workingDirectory, downloadFilePath)){
			LOGGER.info("got the zip file from FTP");
//			DaemonUtility.unzipFile(localFilePath, finalFileName+".zip", targetFolderName);
			if(DaemonUtility.unzipFile(localFilePath, finalFileName+".zip",downloadFilePath)){
				LOGGER.info("unzipped the given file.");
			}else{
				LOGGER.error("File could not be unzipped.");
			}
		}else{
			LOGGER.info("file download failed.");
			throw new IOException("File couldn't be find with finalFileName:" + finalFileName +" localFilePath:"+localFilePath + " downloadFilePath:"+downloadFilePath);
		}
		return new File(finalFileName);
	}

	private String getFinalFileName() {
		DateFormat dateFormat = new SimpleDateFormat(fileNameFormat);
		Date newDate = getProcessDate();
		String newDateFile = dateFormat.format(newDate);
		String finalFileName = circleName + "_" + newDateFile;
		LOGGER.info("inside method getFileForSendingDeactRequest. newDateFile name is " + newDateFile);
		return finalFileName;
	}

	private Date getProcessDate() {
		long timeInMilliSeconds = System.currentTimeMillis();
		LOGGER.info("inside method getFileForSendingDeactRequest.current time in milli " + timeInMilliSeconds);
		long newTimeInMilliSeconds = timeInMilliSeconds
				+ (noOfDaysInAdvance * 24 * 60 * 60 * 1000);
		LOGGER.info("inside method getFileForSendingDeactRequest. new time in milli " + newTimeInMilliSeconds);
		Date newDate = new Date(newTimeInMilliSeconds);
		return newDate;
	}
}
