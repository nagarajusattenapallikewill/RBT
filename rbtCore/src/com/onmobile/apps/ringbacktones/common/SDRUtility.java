package com.onmobile.apps.ringbacktones.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class SDRUtility {
	private static Logger logger = Logger.getLogger(SDRUtility.class);
	
	private static RBTConnector rbtConnector = null;
	static private String m_transSocialRBTDir = "."+File.separator+"SocialRBTTransactionSuccess";
	static TransFileWriter transactionSuccessForSocialRBTWriter = null;
	
	static private String m_transSocialRBTDaemonManagerDir ="."+ File.separator+"SocialRBTDaemonMgrTransactionSuccess";
	static TransFileWriter transactionSuccessForSocialRBTDaemonManagerWriter = null;
	
	static public void initSocialRBTDaemonManagerTransactionFile()
	{
		if(getParamAsString("SOCIAL_RBT_DAEMON_MGR_TRANS") != null )
		{
			m_transSocialRBTDaemonManagerDir = getParamAsString("SOCIAL_RBT_DAEMON_MGR_TRANS") + File.separator+"SocialRBTDaemonMgrTransactionSuccess";
			new File(m_transSocialRBTDaemonManagerDir).mkdirs();
		}
		ArrayList<String> headers = new ArrayList<String> ();
		headers.add("MSISDN");
		headers.add("CALLER");
		headers.add("EVENT_TYPE");
		headers.add("CLIP_ID");
		headers.add("CAT_ID");
		headers.add("START_TIME");
		headers.add("END_TIME");
		headers.add("STATUS");
		headers.add("RBT_TYPE");
		transactionSuccessForSocialRBTDaemonManagerWriter = new TransFileWriter(m_transSocialRBTDaemonManagerDir, "SOCIAL_TRANS", headers);
	}

	
	static public void initSocialRBTTransactionFile()
	{
		if(getParamAsString("SOCIAL_RBT_TRANS") != null )
		{
			m_transSocialRBTDir = getParamAsString("SOCIAL_RBT_TRANS") + File.separator+"SocialRBTTransactionSuccess";
			new File(m_transSocialRBTDir).mkdirs();
		}
		ArrayList<String> headers = new ArrayList<String> ();
		headers.add("MSISDN");
		headers.add("CALLER");
		headers.add("EVENT_TYPE");
		headers.add("CLIP_ID");
		headers.add("CAT_ID");
		headers.add("START_TIME");
		headers.add("END_TIME");
		headers.add("STATUS");
		headers.add("RBT_TYPE");
		transactionSuccessForSocialRBTWriter = new TransFileWriter(m_transSocialRBTDir, "SOCIAL_TRANS", headers);
	}
	
	static public boolean writeSocialRBTDaemonManagerTrans(String subID, String callerID,
			String eventType, int clipId, int catId, String startTime,
			String endTime, String status, String rbtType)
	{
		HashMap<String,String> h = new HashMap<String,String> ();
		h.put("MSISDN", subID);
		h.put("CALLER", callerID);
		h.put("EVENT_TYPE", eventType);
		h.put("CLIP_ID",""+ clipId);
		h.put("CAT_ID",""+ catId);
		h.put("START_TIME", startTime);
		h.put("END_TIME", endTime);
		h.put("STATUS", status);
		h.put("RBT_TYPE", rbtType);
		if(transactionSuccessForSocialRBTWriter != null)
		{
			transactionSuccessForSocialRBTDaemonManagerWriter.writeTrans(h);
			return true;
		}

		return false;
	}
	
	static public boolean writeSocialRBTTrans(String subID, String callerID,
			long eventType, int clipId, int catId, String startTime,
			String endTime, int status, String rbtType)
	{
		HashMap<String,String> h = new HashMap<String,String> ();
		h.put("MSISDN", subID);
		h.put("CALLER", callerID);
		h.put("EVENT_TYPE", ""+eventType);
		h.put("CLIP_ID",""+ clipId);
		h.put("CAT_ID",""+ catId);
		h.put("START_TIME", startTime);
		h.put("END_TIME", endTime);
		h.put("STATUS", ""+status);
		h.put("RBT_TYPE", rbtType);
		if(transactionSuccessForSocialRBTWriter != null)
		{
			transactionSuccessForSocialRBTWriter.writeTrans(h);
			return true;
		}

		return false;
	}
	
	private static  boolean getParamAsBoolean(String param, String defaultVal) {
		try {
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, defaultVal).equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param
							+ " returning defaultVal >" + defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	private static String getParamAsString(String param) {
		try {
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, null);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param);
			return null;
		}
	}
}
