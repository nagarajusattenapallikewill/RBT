package com.onmobile.apps.ringbacktones.v2.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

public class TPTransactionLogger{
	
	
	static Logger logger = Logger.getLogger(TPTransactionLogger.class);
	static Logger selLogger = Logger.getLogger("TRANSACTION_SELECTION_LOG");
	static Logger downloadLogger = Logger.getLogger("TRANSACTION_DOWNLOAD_LOG");
	static Logger transactionLogger = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static String operatorName = null;
	private static Map<String, String> circleIdMap = null;
	private static String circleIdOpsCircleMappingConfig = null;
	
	static void init(){
		operatorName = DBConfigTools.getParameter(iRBTConstant.COMMON,
				"OPERATOR_NAME_FOR_TRANS_LOG", null);
		circleIdOpsCircleMappingConfig = DBConfigTools.getParameter(iRBTConstant.COMMON,
				"CIRCLE_ID_OPERATOR_CIRCLE_MAPPING_FOR_LOG", null);
		getRBTCircleIdOpsCircleIdsMap(circleIdOpsCircleMappingConfig);
	}
	
	public static TPTransactionLogger getTPTransactionLoggerObject(String loggerType){
		
		init();
		if(loggerType.equals("selection")){
			transactionLogger = selLogger;
		}else{
			transactionLogger = downloadLogger;
		}
		logger.info("Returning logger object for TPTransactionLogger: "+ transactionLogger.getName());
		return new TPTransactionLogger();
	}
	
	 public static void writeTPTransLog(String circleId,
				String subscriberID, String callerId, int selectionType,
				int fromTime, int toTime, String interval, int categoryType,
				int status, String subscriberFile, int categoryId,
				int selStatus, String subscriptionClass, Date startTime, Date endDate,
				String loopStatus) {
			// timestamp,operatorname,circlename,msisdn,callerid/Group name,selection type,fromtotime,interval,category
			// type,clipId,categoryId,selection status,subscription class,activation date,deactivation date.
			try {
				if(transactionLogger == null)
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
				transactionLogger.info(strBuilder.toString());

			} catch (Exception ex) {
				logger.info("Exception occured while writing transaction log for RBT 2.0: "+ex, ex);
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

}
