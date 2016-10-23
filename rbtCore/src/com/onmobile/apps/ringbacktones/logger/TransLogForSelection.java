package com.onmobile.apps.ringbacktones.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.TimeBasedRollingPolicy;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

public class TransLogForSelection {

	static Logger transLoggerForSelection = null;
	static Logger logger = Logger.getLogger(TransLogForSelection.class);
	static Logger selLogger = Logger.getLogger(TransLogForSelection.class);
	private static String operatorName = null;
	private static Map<String, String> circleIdMap = null;
	private static String circleIdOpsCircleMappingConfig = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static String fileNameComplete = null;
	private static String fileName = "rbtselection";
	private static boolean isFileAlreadyExists = false;
	
	static {
		
		operatorName = DBConfigTools.getParameter(iRBTConstant.COMMON,
				"OPERATOR_NAME_FOR_SEL_TRANS_LOG", null);
		circleIdOpsCircleMappingConfig = DBConfigTools.getParameter(iRBTConstant.COMMON,
				"CIRCLE_ID_OPERATOR_CIRCLE_MAPPING_FOR_LOG", null);
		fileNameComplete = DBConfigTools.getParameter("COMMON",
				"SELECTION_LOG_PATH_FOR_REPORTING", null);
		String str = fileNameComplete + File.separator + fileName + "_" + sdf.format(new Date()).toString() + ".csv";
		getRBTCircleIdOpsCircleIdsMap(circleIdOpsCircleMappingConfig);
		transLoggerForSelection = createRollingFileLogger("TRANSACTIONS.SELECTION");
		logger.debug("OPERATOR_NAME_FOR_SEL_TRANS_LOG :" + operatorName + ","
				+ "CIRCLE_ID_OPERATOR_CIRCLE_MAPPING_FOR_LOG"
				+ circleIdOpsCircleMappingConfig + ","
				+ "SELECTION_LOG_PATH_FOR_REPORTING :" +str);
		
	}

 	 public static void writeTransLogForSelection(String circleId,
			String subscriberID, String callerId, int selectionType,
			int fromTime, int toTime, String interval, int categoryType,
			int status, String subscriberFile, int categoryId,
			int selStatus, String subscriptionClass, Date startTime, Date endDate,
			String loopStatus) {
		// timestamp,operatorname,circlename,msisdn,callerid/Group name,selection type,fromtotime,interval,category
		// type,clipId,categoryId,selection status,subscription class,activation date,deactivation date.
		try {
			if(transLoggerForSelection == null)
				return;
			if(interval!=null && interval.contains(",")){
				interval = "\"" + interval + "\"";
			}
			int catType = getCategoryType(subscriberID, categoryType, loopStatus);
			int selType = getSelectionType(fromTime, toTime, callerId, status);
			StringBuilder strBuilder = new StringBuilder();
			Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(subscriberFile);
            String clipId = null;
			if(clip!=null)
			{
				clipId = clip.getClipId()+"";
			}
			String deactivationDate = null;
			if(endDate!=null)
			{
				deactivationDate = sdf.format(endDate);
			}
			circleId = circleIdMap.get(circleId) != null ? circleIdMap.get(circleId) : circleId;
			strBuilder.append(sdf.format(new Date()).toString()).append(",");
			strBuilder.append(operatorName).append(",");
			strBuilder.append(circleId).append(",");
			strBuilder.append(subscriberID).append(",");
			strBuilder.append(callerId).append(",");
			strBuilder.append(selType).append(",");
			strBuilder.append(fromTime + "-" + toTime).append(",");
			strBuilder.append(interval).append(",");
			strBuilder.append(catType).append(",");
			strBuilder.append(clipId).append(",");
			strBuilder.append(categoryId).append(",");
			strBuilder.append(selStatus).append(",");
			strBuilder.append(subscriptionClass).append(",");
			strBuilder.append(sdf.format(startTime)).append(",");
			strBuilder.append(deactivationDate);
			logger.debug("TranslogForSelction values = "+ strBuilder.toString());
			transLoggerForSelection.info(strBuilder.toString());

		} catch (Exception ex) {
			selLogger
					.info("TransLogForSelection :: Exception while writing trans log for Selection...");
			selLogger.info("Exception in the selection file == ", ex);
			ex.printStackTrace();
		}
	}

	private static void getRBTCircleIdOpsCircleIdsMap(String circleIdOpsCircleMappingConfig) {
		// RBT CircleID1,Operator CircleId1;RBT CircleID2,Operator CircleId2
		circleIdMap = new HashMap<String, String>();
		if (circleIdOpsCircleMappingConfig != null) {
			String circleIds[] = circleIdOpsCircleMappingConfig.split(";");
			for (String circleId : circleIds) {
				String str[] = circleId.split(",");
				if (str.length == 2) {
					circleIdMap.put(str[0], str[1]);
				}
			}
		}
	}

	private static int getCategoryType(String subscriberID, int categoryType, String loopStatus) {
		// 2 = System Defined Shuffle; 3 = User Defined Shuffles; 1 = Normal
		// Selection
		if (loopStatus != null && (loopStatus.equalsIgnoreCase("l") || loopStatus
				.equalsIgnoreCase("A"))) {
			return 3;
		}else if (Utility.isShuffleCategory(categoryType)) {
			return 2;
		}
		return 1;
	}

	private static int getSelectionType(int fromTime, int toTime, String callerId, int status) {
		// 1 - All caller selection ; 2 - Time of the day selection
		// 3 - Special caller selection ; 4 - Group selection
		if ((fromTime != 0 || toTime != 2359) && (status == 75||status == 80)) {
			return 2;
		} else if (callerId == null) {
			return 1;
		} else if (callerId.startsWith("G")) {
			return 4;
		} else if (!callerId.equalsIgnoreCase("ALL")) {
			return 3;
		}

		return 1;
	}
	
	public synchronized static Logger createRollingFileLogger(String loggerName) {

		Logger logger = null;
		try {
			if(fileNameComplete == null)
				return null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String str = fileNameComplete + File.separator + fileName + "_" + sdf.format(new Date()).toString() + ".csv";
			if(new File(str).exists()){
				isFileAlreadyExists = true;
			}
			loggerName = loggerName.toUpperCase();
			loggerName = loggerName.replaceAll(" ", "_");
			logger = Logger.getLogger(loggerName);
			if (logger.getAllAppenders().hasMoreElements())
				return logger;

			logger.setAdditivity(false);
			PatternLayout layout = new HeaderLayout();

			File folder = new File(fileNameComplete);
			if (!folder.exists())
				folder.mkdirs();

			String archiveFolderName = fileNameComplete + File.separator;
			File archiveFolder = new File(archiveFolderName);
			if (!archiveFolder.exists())
				archiveFolder.mkdirs();

			RollingFileAppender rfa = new RollingFileAppender();
			TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
			tbrp.setFileNamePattern(archiveFolderName + fileName + "_%d{yyyyMMdd}.csv");
//			rfa.setFile(fileNameComplete + fileName + ".LOG");
			tbrp.activateOptions();
			rfa.setRollingPolicy(tbrp);
			rfa.setLayout(layout);
			rfa.setAppend(true);
			rfa.activateOptions();
			logger.addAppender(rfa);
			logger.setLevel(Level.INFO);
		} catch (Exception e) {
			selLogger.info("Exception while Writing Transaction Log for Reporting......");
			e.printStackTrace();
		}
		return logger;
	}

	public static class HeaderLayout extends PatternLayout {

		public HeaderLayout() {
			super();
		}

		@Override
		public String getHeader() {
			if (!isFileAlreadyExists) {
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append("#product=rbtselection");
				strBuilder.append(System.getProperty("line.separator"));
				strBuilder.append("#source=rbtselection");
				strBuilder.append(System.getProperty("line.separator"));
				strBuilder.append("#version=1.0.0");
				strBuilder.append(System.getProperty("line.separator"));
				strBuilder.append("##timestamp,operatorname,circlename,msisdn,"
						+ "callerid/Group name,selection type,fromtotime,interval,"
						+ "category type,clipId,categoryId,selection status,"
						+ "subscription class,activation date,deactivation date");
				strBuilder.append(System.getProperty("line.separator"));
				return strBuilder.toString();
			}
			return null;
		}
	}

}
