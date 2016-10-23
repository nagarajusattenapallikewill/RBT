package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.common.exception.OnMobileException;


public class SendExpirySMSDaemon extends TimerTask {

	private static final Logger LOGGER = Logger.getLogger(SendExpirySMSDaemon.class);
	
	private static List<List<Integer>> blackoutTimesList = null;
	private int scheduleTimeHour = 0;
	private int scheduleTimeMin = 0;
	private int intervalTime = 0;
	private int noOfDaysInAdvance = 0;
	private int noOfDaysInAdvanceDeactDate = 0;
	private static final String DAEMON = "DAEMON";
	private static final String CONTENT_EXPIRY = "CONTENT_EXPIRY";
	private static final String SEND_EXPIRY_SMS_DAEMON_SCHEDULE_TIME_HOURS= "SEND_EXPIRY_SMS_DAEMON_SCHEDULE_TIME_HOURS";
	private static final String SEND_EXPIRY_SMS_DAEMON_SCHEDULE_TIME_MIN= "SEND_EXPIRY_SMS_DAEMON_SCHEDULE_TIME_MIN";
	private static final String SEND_EXPIRY_SMS_TEXT= "SEND_EXPIRY_SMS_TEXT";
	private static final String SEND_EXPIRY_SMS_FILE_NAME_FORMAT = "SEND_EXPIRY_SMS_FILE_NAME_FORMAT";
	private static final String NUMBER_OF_DAYS_FOR_SEND_EXPIRY_SMS = "NUMBER_OF_DAYS_FOR_SEND_EXPIRY_SMS";
	private static final String NUMBER_OF_DAYS_FOR_RENEWAL = "NUMBER_OF_DAYS_FOR_RENEWAL";
	private static final String INTERVAL_TIME_BETWEEN_NEXT_RUN_SMS_DAEMON = "INTERVAL_TIME_BETWEEN_NEXT_RUN_SMS_DAEMON";
	
	private static final String SEND_EXPIRY_SMS_LOCAL_FILE_PATH = "SEND_EXPIRY_SMS_LOCAL_FILE_PATH";
//	private static final String SEND_EXPIRY_SMS_TARGET_FOLDER_NAME = "SEND_EXPIRY_SMS_TARGET_FOLDER_NAME";
	private static final String SEND_EXPIRY_SMS_DOWNLOAD_FILE_PATH = "SEND_EXPIRY_SMS_DOWNLOAD_FILE_PATH";
	private static final String SEND_EXPIRY_SMS_FTP_IP = "SEND_EXPIRY_SMS_FTP_IP";
	private static final String SEND_EXPIRY_SMS_FTP_USERNAME = "SEND_EXPIRY_SMS_FTP_USERNAME";
	private static final String SEND_EXPIRY_SMS_FTP_PASSWORD = "SEND_EXPIRY_SMS_FTP_PASSWORD";
	private static final String SEND_EXPIRY_SMS_FTP_WORKING_DIR = "SEND_EXPIRY_SMS_FTP_WORKING_DIR";
	private static final String CIRCLE_NAME_FOR_SEND_EXPIRY_SMS = "CIRCLE_NAME_FOR_SEND_EXPIRY_SMS";
	private static final String SENDER_FOR_SEND_EXPIRY_SMS = "SENDER_FOR_SEND_EXPIRY_SMS";
	private static final String SEND_EXPIRY_SMS_DEACT_DATE_FORMAT = "SEND_EXPIRY_SMS_DEACT_DATE_FORMAT";
	private static final String EXPIRY_SMS_FILE_NAME = "EXPIRY_SMS_FILE_NAME";
	private static final String EXPIRY_SMS_PROCESSED_ROW_NUM = "EXPIRY_SMS_PROCESSED_ROW_NUM";
	private static final String CONTENT_EXPIRY_SMS_BLACK_OUT_PERIOD = "CONTENT_EXPIRY_SMS_BLACK_OUT_PERIOD";
	
	private String smsText = null;
	private String fileNameFormat = null;
	private String localFilePath = null;
//	private String targetFolderName = null;
	private String downloadFilePath = null;
	private String FTP_IP = null;
	private String FTP_USERNAME = null;
	private String FTP_PWD = null;
	private String workingDirectory = null;
	private String circleName = null;
	private String sender = null;
	private String deactDateFormat = null;
	
	static {
		initializeBlackOut();
	}

	public SendExpirySMSDaemon() {
		init();
	}
	
	@Override
	public void run() {
		
		try {
			LOGGER.info("Started " + SendExpirySMSDaemon.class.getSimpleName());
			if (isBlackOutPeriodNow()) {
				LOGGER.error(new Date() + " it is blackout time so skipping");
				return;
			}
			LOGGER.info("Not in black-out period so proceeding...");
			
			if (null != smsText && !smsText.isEmpty() && null != fileNameFormat && !fileNameFormat.isEmpty()) {
				LOGGER.info("obtained sms text is " + smsText + " file name format is " + fileNameFormat);
	//			while ((rbtDaemonManager != null /*&& rbtDaemonManager.isAlive()*/)
	//					|| isAlive) {
				if (null != localFilePath && !localFilePath.isEmpty()
	//					&& null != targetFolderName
	//					&& !targetFolderName.isEmpty()
						&& null != downloadFilePath
						&& !downloadFilePath.isEmpty() && null != FTP_IP
						&& !FTP_IP.isEmpty() && null != FTP_PWD
						&& !FTP_PWD.isEmpty() && null != FTP_USERNAME
						&& !FTP_USERNAME.isEmpty()
						&& null != workingDirectory
						&& !workingDirectory.isEmpty() && null != circleName
						&& !circleName.isEmpty()){
	//				sendSMSText(localFilePath,targetFolderName);
					sendSMSText(localFilePath);
				}else{
					LOGGER.error("Please check the ParametersCacheManager Logs. Some mandatory config is either null or missing.");
				}
			//	}
			}else{
				LOGGER.warn("Unable to process. sms text obtained is null");
			}
		} catch (Exception e) {
			LOGGER.error("Error while running daemon", e);
		}
	}
	public void init(){
		try {
			smsText = getSMSText(CONTENT_EXPIRY, SEND_EXPIRY_SMS_TEXT, "", "eng");
			fileNameFormat = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_FILE_NAME_FORMAT, null);
			localFilePath = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_LOCAL_FILE_PATH, null);
//			targetFolderName = RBTParametersUtils.getParamAsString(DAEMON,
//					SEND_EXPIRY_SMS_TARGET_FOLDER_NAME, null);
			downloadFilePath = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_DOWNLOAD_FILE_PATH, null);
			FTP_IP = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_FTP_IP, null);
			FTP_USERNAME = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_FTP_USERNAME, null);
			FTP_PWD = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_FTP_PASSWORD, null);
			workingDirectory = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_FTP_WORKING_DIR, null);
			circleName = RBTParametersUtils.getParamAsString(DAEMON,
					CIRCLE_NAME_FOR_SEND_EXPIRY_SMS, null);
			sender = RBTParametersUtils.getParamAsString(DAEMON,
					SENDER_FOR_SEND_EXPIRY_SMS, "");
			deactDateFormat = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_DEACT_DATE_FORMAT, "dd-MM-yyyy");
			if(null == sender || sender.isEmpty()){
				LOGGER.warn("Parameter " + SENDER_FOR_SEND_EXPIRY_SMS
						+ " not configured. So taking default value");
			}
			String scheduleTimeHrStr = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_DAEMON_SCHEDULE_TIME_HOURS, "11");
			String noOfDaysForSendSMSStr = RBTParametersUtils.getParamAsString(DAEMON,
					NUMBER_OF_DAYS_FOR_SEND_EXPIRY_SMS, "4");
			String noOfDaysForDeactStr = RBTParametersUtils.getParamAsString(DAEMON,
					NUMBER_OF_DAYS_FOR_RENEWAL, "2");
			String scheduleTimeMinStr = RBTParametersUtils.getParamAsString(DAEMON,
					SEND_EXPIRY_SMS_DAEMON_SCHEDULE_TIME_MIN, "1");
			String intervalTimeStr = RBTParametersUtils.getParamAsString(DAEMON,
					INTERVAL_TIME_BETWEEN_NEXT_RUN_SMS_DAEMON, "3600000");
			if(null != scheduleTimeHrStr){
				scheduleTimeHour = Integer.parseInt(scheduleTimeHrStr);
			}else {
				LOGGER.warn("Parameter " + SEND_EXPIRY_SMS_DAEMON_SCHEDULE_TIME_HOURS
						+ " not configured. So taking default as 11");
			}
			if(null != scheduleTimeMinStr){
				scheduleTimeMin = Integer.parseInt(scheduleTimeMinStr);
			}else {
				LOGGER.warn("Parameter " + SEND_EXPIRY_SMS_DAEMON_SCHEDULE_TIME_MIN
						+ " not configured. So taking default as 1");
			}
			if(null != intervalTimeStr){
				intervalTime = Integer.parseInt(intervalTimeStr);
			}else {
				LOGGER.warn("Parameter " + INTERVAL_TIME_BETWEEN_NEXT_RUN_SMS_DAEMON
						+ " not configured. So taking default as 1 hour");
			}
			if(null != noOfDaysForSendSMSStr){
				noOfDaysInAdvance = Integer.parseInt(noOfDaysForSendSMSStr);
			}else {
				LOGGER.warn("Parameter " + NUMBER_OF_DAYS_FOR_SEND_EXPIRY_SMS
						+ " not configured. So taking default as 5");
			}
			if(null != noOfDaysForDeactStr){
				noOfDaysInAdvanceDeactDate = Integer.parseInt(noOfDaysForDeactStr);
			}else {
				LOGGER.warn("Parameter " + NUMBER_OF_DAYS_FOR_RENEWAL
						+ " not configured. So taking default as 2");
			}

		} catch (NumberFormatException e) {
			LOGGER.error("exception in init() " ,e);
			e.printStackTrace();
		}
	}
	public void start() {
		LOGGER.info("Scheduling SendExpirySMSDaemon...");
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, scheduleTimeHour);
		calendar.set(Calendar.MINUTE, scheduleTimeMin);
		calendar.set(Calendar.SECOND, 0);
		Date startDate = calendar.getTime();
		LOGGER.info("Start date is " + startDate);
		Timer timer = new Timer(SendExpirySMSDaemon.class.getSimpleName());
		long intervalTimeInMilli = intervalTime * 60 * 1000;
		timer.scheduleAtFixedRate(this, startDate, intervalTimeInMilli);
		
		LOGGER.info("SendExpirySMSDaemon has been scheduled");
	}

	public void stop() {
		this.cancel();
	}
	
	private File getFileForSendSMS(String finalFileName) throws IOException{
		LOGGER.info("inside method getFileForSendSMS. final file name is " + finalFileName);
		String zipFileName = finalFileName+".zip";
		if(DaemonUtility.readZIPFileFromFTP(zipFileName, FTP_IP, FTP_USERNAME, FTP_PWD, workingDirectory, downloadFilePath)){
			LOGGER.info("got the zip file from FTP");
//			DaemonUtility.unzipFile(localFilePath, zipFileName, targetFolderName);
			if(DaemonUtility.unzipFile(localFilePath, zipFileName,downloadFilePath)){
			LOGGER.info("unzipped the given file.");
			}else{
				LOGGER.error("File could not be unzipped.");
			}
		}else{
			LOGGER.info("file download failed.");
			throw new IOException("File couldn't be find with zipFileName:" + zipFileName +" localFilePath:"+localFilePath + " downloadFilePath:"+downloadFilePath);
		}
		return new File(finalFileName);
	}

	private String getFileName() {
		Date currDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat(fileNameFormat);
		long timeInMilliSeconds = currDate.getTime();
		LOGGER.info("inside method getFileForSendSMS.current time in milli " + timeInMilliSeconds);
		long newTimeInMilliSeconds = timeInMilliSeconds
				+ (noOfDaysInAdvance * 24 * 60 * 60 * 1000);
		LOGGER.info("inside method getFileForSendSMS. new time in milli " + newTimeInMilliSeconds);
		Date newDate = new Date(newTimeInMilliSeconds);
		String newDateFile = dateFormat.format(newDate);
		String finalFileName = circleName + "_" + newDateFile;
		LOGGER.info("inside method getFileForSendSMS. newDateFile name is " + newDateFile);
		return finalFileName;
	}
//	public void sendSMSText(String localFilePath,String localFolder){
	/**
	 * @param localFilePath
	 */
	private void sendSMSText(String localFilePath){
		LOGGER.info("inside method sendSMSText.");
		try {
			String finalFileName = getFileName();
			File processedFile = new File(localFilePath, finalFileName+".processed");
			if (processedFile.exists()) {
				LOGGER.warn("Processed file "
						+ processedFile.getAbsolutePath()
						+ " already exists. So skipping this run");
					return;
			}
			File fileWithoutExtn = getFileForSendSMS(finalFileName);
	//		String finalFileName = localFilePath + localFolder + "/" +file.getName()+".csv";
			String nameWithoutExtn = fileWithoutExtn != null ? fileWithoutExtn.getName() : null;
			File targetFile = new File(localFilePath, nameWithoutExtn+".csv");
			LOGGER.info("inside method sendSMSText. final file name is " + targetFile.getAbsolutePath());
	//		int numberOfFailedRecords = 0;
			int invalidRecords = 0;
			boolean response = false;
			boolean isFileProcessSuccess = true;
			if(targetFile.exists()){
				LOGGER.info("inside method sendSMSText.target file exists");
				CSVReader csvFileReader = null;
				try {
					csvFileReader = new CSVReader(
							new FileReader(targetFile));
					String[] columns = null;
					int row = 0;
					int processedRow = getProcessedRowNum(nameWithoutExtn);
					
					while ((columns = csvFileReader.readNext()) != null) {
						row++;
						if (columns.length < 24) {
							LOGGER.warn("Invalid line at row " + row);
							continue;
						}
						if (row <= processedRow) {
							LOGGER.info("Already processed " + row);
							continue;
						}
						LOGGER.debug("Processing row " + row);
						String msisdn = columns[1];
						String refId = columns[23];
						LOGGER.info("inside method sendSMSText.msisdn is "
								+ msisdn);
						LOGGER.info("inside method sendSMSText.internal ref id is "
								+ refId);
						RBTDBManager rbtDBManager = RBTDBManager.getInstance();

						SubscriberStatus subSelectionByRefId = rbtDBManager
								.getSelectionByRefId(msisdn, refId);
						if (subSelectionByRefId != null) {
							String wavFile = subSelectionByRefId
									.subscriberFile();
							LOGGER.info("inside method sendSMSText. wav file name is "
									+ wavFile);
							Clip clip = RBTCacheManager.getInstance()
									.getClipByRbtWavFileName(wavFile);
							String sms = smsText;
							if (null != clip) {
								sms = sms.replaceAll("%SONG_NAME%",
										clip.getClipName());
								sms = sms.replaceAll("%ARTIST_NAME%",
										clip.getArtist());
								sms = sms.replaceAll("%DEACT_DATE%",
										getDeactDate());
							} else {
								LOGGER.error("clip obtained is null. going for next record.");
								continue;
							}
							LOGGER.info("Sending sms: " + sms + ", msisdn: "
									+ msisdn + ", refId: " + refId
									+ ", wavFile: " + wavFile);
							response = Tools.sendSMS(sender, msisdn, sms,
									null);
							if (response) {
								LOGGER.info("SMS sent successfully to "
										+ msisdn + " Updating file name "
										+ nameWithoutExtn + " & row num " + row
										+ " in params table, sms: " + sms);
								updateProcessedRowDetails(nameWithoutExtn, row);
							} else {
								LOGGER.error("SMS sending failed to "
										+ msisdn
										+ " so stoping process, sms: " + sms);
								isFileProcessSuccess = false;
								break;
//								numberOfFailedRecords++;
							}
						} else {
							LOGGER.warn("no valid record obtained from the selections table for ref id "
									+ refId);
							invalidRecords++;
						}
					}
				} finally {
					if (null != csvFileReader) {
						csvFileReader.close();
					}
				}
				if (invalidRecords > 0) {
					LOGGER.info("number of invalid records are "
							+ invalidRecords + " from file " + targetFile);
				}
//				if(numberOfFailedRecords > 0){
//					LOGGER.warn("SMS sending failed for " + numberOfFailedRecords + " records ");
//				}else {
//					LOGGER
//					.info("entire file processed successfully. number of records for which SMS sending failed are "
//							+ numberOfFailedRecords);
//					isFileProcessSuccess = true;
//				}
				if (isFileProcessSuccess) {
					LOGGER.info("going to rename the file.");
					if (DaemonUtility.renameFile(targetFile)) {
						LOGGER.info("file renamed successfully");
					} else {
						LOGGER
								.error("error in file rename. could not change the extension.");
					}
				} else {
					LOGGER
							.error("file processing failed. will try again in next run.");
				}
			} else {
				LOGGER.warn("sorry could not find the file " + targetFile.getName());
			}
			LOGGER.info("Finished processing");
		} catch (FileNotFoundException e) {
			LOGGER.error("File not found ", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("IO Exception occurred ", e);
			e.printStackTrace();
		} catch (OnMobileException e) {
			LOGGER.error("something wrong with Tools.sendSMS",e);
			e.printStackTrace();
		}
	}

	private void updateProcessedRowDetails(String nameWithoutExtn, int row) {
		RBTParametersUtils.updateParameter(DAEMON,
				EXPIRY_SMS_FILE_NAME, nameWithoutExtn,
				"Content expiry daemon error file name");
		RBTParametersUtils.updateParameter(DAEMON,
				EXPIRY_SMS_PROCESSED_ROW_NUM, String.valueOf(row),
				"Content expiry daemon error row num");
	}

	private int getProcessedRowNum(String nameWithoutExtn) {
		int errorRow = 0;
		String errorFileName = RBTParametersUtils.getParamAsString(
				DAEMON, EXPIRY_SMS_FILE_NAME, null);
		if (null != errorFileName
				&& nameWithoutExtn.equals(errorFileName)) {
			errorRow = RBTParametersUtils.getParamAsInt(DAEMON,
					EXPIRY_SMS_PROCESSED_ROW_NUM, -1);
		}
		return errorRow;
	}
	
	private String getDeactDate() {
		Date curDate = new Date();
		LOGGER.info("current date is " + curDate);
		long timeInMilliSeconds = curDate.getTime();
		LOGGER.info("inside method getDeactDate.current time in milli " + timeInMilliSeconds);
		int daysAfterDeact = noOfDaysInAdvance - noOfDaysInAdvanceDeactDate;
		LOGGER.info("Days after deact " + daysAfterDeact);
		long newTimeInMilliSeconds = timeInMilliSeconds
				+ (daysAfterDeact * 24 * 60 * 60 * 1000);
		LOGGER.info("inside method getDeactDate. new time in milli " + newTimeInMilliSeconds);
		Date newDate = new Date(newTimeInMilliSeconds);
		SimpleDateFormat sdf = new SimpleDateFormat(deactDateFormat);
		String finalDate = sdf.format(newDate);
		LOGGER.info("date obtained for sending in the SMS is " + finalDate);
		return finalDate;
	}

	private String getSMSText(String type, String subType, String defaultValue,
			String language) {
		String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
				type, subType, language);
		if (smsText != null)
			return smsText;
		else
			return defaultValue;
	}

	private static boolean isBlackOutPeriodNow()
	{
		Calendar calendar = Calendar.getInstance();
		List<Integer> blackout = blackoutTimesList.get(calendar
				.get(Calendar.DAY_OF_WEEK));

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("BlackOut checked against " + blackout);

		return (blackout.contains(calendar.get(Calendar.HOUR_OF_DAY)));
	}

	private static void initializeBlackOut()
	{
		blackoutTimesList = new ArrayList<List<Integer>>();
		for (int i = 0; i <= 7; i++)
			blackoutTimesList.add(new ArrayList<Integer>());

		String blackoutTimes = RBTParametersUtils.getParamAsString(DAEMON,
				CONTENT_EXPIRY_SMS_BLACK_OUT_PERIOD, null);
		LOGGER.info("Configured BlackOut " + blackoutTimes);
		if (blackoutTimes == null)
		{
			LOGGER.info("No BlackOut Configured");
			return;
		}

		String[] blackoutTokens = blackoutTimes.split(",");
		for (String blackout : blackoutTokens)
		{
			if (!blackout.contains("["))
			{
				LOGGER.info("No BlackOut Time Configured" + blackout);
				continue;
			}

			List<Integer> days = getDays(blackout.substring(0,
					blackout.indexOf("[")));
			if (days != null && days.size() > 0)
			{
				List<Integer> times = getTimes(blackout.substring(blackout
						.indexOf("[")));
				for (int j = 0; j < days.size(); j++)
					blackoutTimesList.set(days.get(j).intValue(), times);
			}
		}

		LOGGER.info("blackoutTimesList initialized " + blackoutTimesList);
	}

	private static List<Integer> getDays(String string)
	{
		List<Integer> daysList = new ArrayList<Integer>();

		Map<String, Integer> days = new HashMap<String, Integer>();
		days.put("SUN", 1);
		days.put("MON", 2);
		days.put("TUE", 3);
		days.put("WED", 4);
		days.put("THU", 5);
		days.put("FRI", 6);
		days.put("SAT", 7);

		if (string.contains("-"))
		{
			try
			{
				String day1 = string.substring(0, string.indexOf("-"));
				String day2 = string.substring(string.indexOf("-") + 1);
				if (!days.containsKey(day1) || !days.containsKey(day2))
				{
					LOGGER.info("Invalid week specified !!!!" + string);
					return null;
				}

				int startDay = days.get(day1);
				int endDay = days.get(day2);

				if (endDay > startDay)
				{
					for (int t = startDay; t <= endDay; t++)
						daysList.add(t);
				}
				else
				{
					for (int t = startDay; t <= 7; t++)
						daysList.add(t);

					for (int t = 1; t <= endDay; t++)
						daysList.add(t);
				}
			}
			catch (Throwable e)
			{
				LOGGER.debug(e.getMessage(), e);
			}
		}
		else
		{
			if (days.containsKey(string))
				daysList.add(days.get(string));
			else
				LOGGER.info("Invalid week specified !!!!" + string);
		}

		LOGGER.info("DaysList" + daysList);
		return daysList;
	}

	private static List<Integer> getTimes(String string)
	{
		List<Integer> timesList = new ArrayList<Integer>();

		string = string.substring(1, string.length() - 1);
		String[] tokens = string.split(";");
		for (String token : tokens)
		{
			if (token.contains("-"))
			{
				try
				{
					int startTime = Integer.parseInt(token.substring(0,
							token.indexOf("-")));
					int endTime = Integer.parseInt(token.substring(token
							.indexOf("-") + 1));

					if (startTime > 23 || endTime > 23)
					{
						LOGGER.info("Invalid time specified !!!!" + string);
						continue;
					}
					else if (endTime > startTime)
						for (int t = startTime; t <= endTime; t++)
							timesList.add(t);
					else
					{
						for (int t = startTime; t <= 23; t++)
							timesList.add(t);

						for (int t = 0; t <= endTime; t++)
							timesList.add(t);
					}
				}
				catch (Throwable e)
				{
					LOGGER.debug(e.getMessage(), e);
				}
			}
			else
			{
				try
				{
					int n = Integer.parseInt(token);
					if (n >= 0 && n <= 23)
						timesList.add(n);
					else
						LOGGER.info("Invalid time specified !!!!" + string);
				}
				catch (Throwable t)
				{
					LOGGER.info("Invalid time specified !!!!" + string);
				}
			}
		}

		return timesList;
	}
}
