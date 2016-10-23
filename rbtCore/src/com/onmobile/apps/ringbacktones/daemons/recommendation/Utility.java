/**  
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.recommendation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

/**
 * @author vinayasimha.patil
 *
 */
public class Utility
{
	private static Logger logger = Logger.getLogger(Utility.class);

	private static Utility utility = null;

	private static String promoSMSMsgID = "BULKSMS_START";
	private static int numOfPromSMSPerSec = 10;
	private static String promoSMSC = null;
	private static String promoSMSStartTime = "0900";
	private static String promoSMSEndTime = "2000";
	private static String promoSMSType = "text";
	protected static String promoSMSFilePath = ".";

	private static String promoToolname = "Promotool";
	private static String promoToolRefKey = "promotool";

	private Utility(ParametersCacheManager parametersCacheManager)
	{
		Parameters promoSMSMsgIDParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "PROMO_SMS_MSG_ID", "BULKSMS_START");
		if(promoSMSMsgIDParam != null)
			promoSMSMsgID = promoSMSMsgIDParam.getValue().trim();

		Parameters numOfPromSMSPerSecParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "NUM_OF_PROMO_SMS_PER_SEC", "10");
		if(numOfPromSMSPerSecParam != null)
		{
			try
			{
				numOfPromSMSPerSec = Integer.parseInt(numOfPromSMSPerSecParam.getValue().trim());
			}
			catch(Exception e)
			{
				numOfPromSMSPerSec = 10;
			}
		}

		Parameters promoSMSCParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "PROMO_SMSC", null);
		if(promoSMSCParam != null)
			promoSMSC = promoSMSCParam.getValue().trim();

		Parameters promoSMSStartTimeParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "PROMO_SMS_START_TIME");
		if(promoSMSStartTimeParam != null)
			promoSMSStartTime = promoSMSStartTimeParam.getValue().trim();

		Parameters promoSMSEndTimeParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "PROMO_SMS_END_TIME");
		if(promoSMSEndTimeParam != null)
			promoSMSEndTime = promoSMSEndTimeParam.getValue().trim();

		Parameters promoSMSTypeParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "PROMO_SMS_TYPE");
		if(promoSMSTypeParam != null)
			promoSMSType = promoSMSTypeParam.getValue().trim();

		Parameters promoSMSFilePathParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "PROMO_SMS_FILE_PATH");
		if(promoSMSFilePathParam != null)
			promoSMSFilePath = promoSMSFilePathParam.getValue().trim();

		Parameters promoToolnameParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "PROMO_TOOL_NAME");
		if(promoToolnameParam != null)
			promoToolname = promoToolnameParam.getValue().trim();

		Parameters refKeyParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "PROMO_TOOL_REF_KEY");
		if(refKeyParam != null)
			promoToolRefKey = refKeyParam.getValue().trim();
	}

	public static Utility getInstance(ParametersCacheManager parametersCacheManager)
	{
		if(utility == null)
			utility = new Utility(parametersCacheManager);

		return utility;
	}

	public boolean sendPromoRecommendationSMS(String subscriberFile, String smsText, String smsNo) 
	{ 
		logger.info("RBT::inside sendPromoRecommendationSMS"); 
		try 
		{ 
			SimpleDateFormat onlyDayformatter = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat fileNameFormatter = new SimpleDateFormat("MMddHHmmssSSS");

			File smsFile = new File(subscriberFile); 
			String cunDateStr = onlyDayformatter.format(new Date()); 
			String promoStartDate = cunDateStr; 

			if(!smsFile.exists()) 
			{ 
				logger.info("RBT:: "+ smsFile.getAbsolutePath() +" file does not exist"); 
				return false; 
			} 

			String messageID = promoSMSMsgID; 
			String data = ""; 

			int numberOfSMSPerSec = numOfPromSMSPerSec; 
			String smsStartTime = promoSMSStartTime; 
			String smsEndTime = promoSMSEndTime; 

			Calendar calendar = Calendar.getInstance(); 
			calendar.add(Calendar.DAY_OF_YEAR, 3); 
			String smsEndDate = onlyDayformatter.format(calendar.getTime()); 


			data += "PROMO_NAME=rec"+ fileNameFormatter.format(new Date()); 
			data += "|PROMO_FILE="+ subscriberFile; 
			if(promoSMSC != null) 
				data += "|PROMO_SMSC="+ promoSMSC; 
			data += "|PROMO_TEXT="+ smsText; 
			data += "|PROMO_START_DATE="+ promoStartDate; 
			data += "|PROMO_START_TIME="+ smsStartTime; 
			data += "|PROMO_END_DATE="+ smsEndDate; 
			data += "|PROMO_END_TIME="+ smsEndTime; 
			data += "|PROMO_SMS_PER_SEC="+ numberOfSMSPerSec; 
			data += "|PROMO_SENDER="+ smsNo; 
			data += "|PROMO_TEST_NUMBERS="; 
			data += "|PROMO_STATUS_INTERVAL=10"; 
			data += "|PROMO_TEST_MODE=FALSE"; 
			data += "|PROMO_MESSAGE_TYPE="+ promoSMSType; 

			return (RecommendationDaemonOzonized.broadcastInfoMessage(messageID, promoToolname, promoToolRefKey, data)); 

		} 
		catch(Exception e) 
		{ 
			logger.error("", e);
		}

		return false; 
	}

	public File createListFile(String fileName, ArrayList<String> subscriberList, RBTDBManager rbtDBManager, boolean checkBlackList)
	{
		SimpleDateFormat fileNameFormatter = new SimpleDateFormat("MMddHHmmssSSS");

		fileName = fileName + "_" + fileNameFormatter.format(new Date()) + ".txt";

		File listFile = null;

		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try
		{
			File dir = new File(promoSMSFilePath);
			if(!dir.exists())
				dir.mkdirs();
			File recDir = new File(dir, "AUTO_RECOMMENDATION");
			if(!recDir.exists())
				recDir.mkdirs();
			listFile = new File(recDir, fileName);

			fileWriter = new FileWriter(listFile.getAbsolutePath());
			bufferedWriter = new BufferedWriter(fileWriter);

			for(Iterator<String> iterator = subscriberList.iterator(); iterator.hasNext();)
			{
				String subscriberID = iterator.next();
				if(!checkBlackList || !isBlackListedForSMS(rbtDBManager, subscriberID));
				{
					bufferedWriter.write(subscriberID);
					bufferedWriter.newLine();
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				bufferedWriter.close();
				fileWriter.close();
			}
			catch (IOException e)
			{
				logger.error("", e);
			}
		}

		return listFile;
	}

	public static boolean isBlackListedForSMS(RBTDBManager rbtDBManager, String subscriberID)
	{
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		if(subscriber == null)
			return true;
		else
		{
			HashMap<String, String> extraInfoMap = rbtDBManager.getExtraInfoMap(subscriber);
			if (extraInfoMap != null)
			{
				String isnewsLetterOn = extraInfoMap.get(iRBTConstant.IS_NEWSLETTER_ON);
				if(isnewsLetterOn != null && isnewsLetterOn.equalsIgnoreCase(iRBTConstant.NEWSLETTER_ON))
					return true;
			}
		}

		return false;
	}
}
