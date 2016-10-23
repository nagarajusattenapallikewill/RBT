package com.onmobile.apps.ringbacktones.daemons;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.smsgateway.accounting.Accounting;

/**
 * Last Modified on Aug07,2009 by Sreenadh
 * Content API's migrated to rbtcontent
 */

public class RBTProcessUGCCharging extends Thread{
	
	private static Logger logger = Logger.getLogger(RBTProcessUGCCharging.class);
	
	private RBTDaemonManager m_mainDaemonThread = null; 
	
	private static RBTDBManager rbtDBManager = null;
	private static RBTCacheManager rbtCacheManager = null;
	private static ParametersCacheManager m_rbtParamCacheManager = null;
	
	SimpleDateFormat actOrSelFailSdf = new SimpleDateFormat("yyyyMMdd");
	private static final String PARAMETER_TYPE_DAEMON = "DAEMON";
	private String UGC_TRANSACTION_ID = "UGC_TRANSACTION_ID";
	
	HashMap m_ugcCreditMap = new HashMap();
	private SimpleDateFormat ugcDateFormat = new SimpleDateFormat("ddMMyyHHmmss");
	public static Accounting ugcAccounting = null;
	static String m_sdrUGCWorkingDir = "./ugc";
	static int m_sdrUGCSize = 1000;
	static long m_sdrUGCInterval = 24;
	static String m_sdrUGCRotation = "size";
	static boolean m_sdrUGCBillingOn = true;
	private String m_SmsTextForUgcCreator = "%CALLER_ID has set %PROMO_ID as his ringbacktone.";

	protected RBTProcessUGCCharging(RBTDaemonManager mainDaemonThread)
	{
		try
		{
			setName("RBTProcessUGCCharging");
			m_mainDaemonThread = mainDaemonThread;
			init();
		}
		catch(Exception e)
		{
			logger.error("Issue in creating RBTProcessUGCCharging", e);
		}
	}
	
	public void init()
	{
		m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();

		rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
		m_sdrUGCWorkingDir =getParamAsString("DAEMON","UGC_SDR_WORKING_DIR", "./ugc");
		
		createUGCAccounting();
		getUGCCreditTable();
	}
	
	public void run()
	{
		while(m_mainDaemonThread != null && m_mainDaemonThread.isAlive()) 
		{
			
			processUGCCharging();
			
			try
			{
				logger.info("Process Trial Selections Thread Sleeping for 5 minutes............");
				Thread.sleep(getParamAsInt("DAEMON","SLEEP_INTERVAL_MINUTES", 5) * 60 * 1000);
			}
			catch(Exception e)
			{
			}
		}
	}

	/*
	 * Gets selections which have selection info like UGC and status not in A,N,F,W,E,R,Z. 
	 * 
	 * 
	 * */
	private void processUGCCharging() {
		SubscriberStatus[] ss = getSelectionsForUGCCharging();
		if(ss == null || ss.length <= 0) {
			logger.info("RBT::No selections found for UGC charging. ");
			return;
		}
		for(int i = 0; i < ss.length; i++) {
			String selectionInfo = ss[i].selectionInfo();
			Date selNCDate = ss[i].nextChargingDate();
			String selNCDateStr = null;
			if(selNCDate != null)
				selNCDateStr = actOrSelFailSdf.format(selNCDate);
			String selSStatus = ss[i].selStatus();
			String deSelectedBy = ss[i].deSelectedBy();
			boolean canProcessSelection = true;
			if (selSStatus != null
					&& selSStatus.equalsIgnoreCase("X")
					&& ((selNCDateStr != null && selNCDateStr
							.equalsIgnoreCase("20351231")) || (deSelectedBy != null && deSelectedBy
							.equalsIgnoreCase("BLACKLIST"))))
				canProcessSelection = false;

			if(canProcessSelection) {
				String nToken = null;
				String transactionID = null;
				boolean transactionIdExisted = false;
				StringTokenizer sTokenizer = new StringTokenizer(selectionInfo, ":");

				while (sTokenizer.hasMoreTokens()) {
					nToken = sTokenizer.nextToken();
					if(nToken.toUpperCase().startsWith("UGC_")) {
						transactionID = nToken.substring(4);
						transactionIdExisted = true;
						break;
					}
				}
				if(ss[i].subscriberFile() != null
						&& ss[i].subscriberFile().equalsIgnoreCase("default")) {
					String newSelectionInfo = Tools.findNReplace(selectionInfo, ":UGC", "");
					newSelectionInfo = Tools.findNReplace(newSelectionInfo, "UGC", "");
					newSelectionInfo = Tools
							.findNReplace(newSelectionInfo, "_" + transactionID, "");
					rbtDBManager.setSelectionInfo(ss[i].subID(), ss[i].setTime(), newSelectionInfo);
					continue;
				}
				if(transactionID == null) {
					long lastTransactionID = getTransactionIDFromTable();
					if(lastTransactionID == -1)
						continue;
					transactionID = Long.toString(lastTransactionID + 1, 32);
					updateTransactionIdinDB(lastTransactionID + 1);
				}
				boolean success = makeHTTPHitForUGCCharging(ss[i], transactionID);
				if(success) {
					String newSelectionInfo = Tools.findNReplace(selectionInfo, ":UGC", "");
					newSelectionInfo = Tools.findNReplace(newSelectionInfo, "UGC", "");
					newSelectionInfo = Tools
							.findNReplace(newSelectionInfo, "_" + transactionID, "");
					rbtDBManager.setSelectionInfo(ss[i].subID(), ss[i].setTime(), newSelectionInfo);
				}
				else {
					logger.info("RBT::UGC Charging failed for subscriberID "
							+ ss[i].subID() + " for UGC clip " + ss[i].subscriberFile()
							+ " and transactionID " + transactionID);
					if(!transactionIdExisted) {
						String newSelectionInfo = Tools.findNReplace(selectionInfo, "UGC", "UGC_"
								+ transactionID);
						rbtDBManager.setSelectionInfo(ss[i].subID(), ss[i].setTime(),
								newSelectionInfo);
					}
				}
			}
			else {
				logger.info("RBT::not crediting as user "
						+ ss[i].subID() + " is deactive");
				String newSelectionInfo = Tools.findNReplace(selectionInfo, "UGC", "Didnt Credit");
				rbtDBManager.setSelectionInfo(ss[i].subID(), ss[i].setTime(), newSelectionInfo);
			}
		}
	}
	
	private long getTransactionIDFromTable() {
		String ugcTranID = getParamAsString(PARAMETER_TYPE_DAEMON, UGC_TRANSACTION_ID, null);
		long transactionIDinDB = 0;
		if(ugcTranID == null) {
			logger.info("RBT:: " + UGC_TRANSACTION_ID + " is null.");
			return -1;
		}
		try {
			transactionIDinDB = Long.parseLong(ugcTranID.trim());
		}
		catch (Exception e) {
			logger.info("RBT:: " + UGC_TRANSACTION_ID + " is invalid number.");
			return -1;
		}
		return transactionIDinDB;
	}

	private SubscriberStatus[] getSelectionsForUGCCharging() {
		return rbtDBManager.getSelectionsForUGCCharging(getParamAsInt(PARAMETER_TYPE_DAEMON, "FETCH_SIZE", 5000));
	}
	
	private void updateTransactionIdinDB(long lastTransactionID) {
		CacheManagerUtil.getParametersCacheManager().updateParameter(PARAMETER_TYPE_DAEMON, UGC_TRANSACTION_ID, ""+ lastTransactionID);
	}
	
	private boolean makeHTTPHitForUGCCharging(SubscriberStatus ss, String transactionID){
		if(getParamAsString("DAEMON","UGC_CREDIT_URL", null) == null) {
			logger.info("RBT:: UGC Credit URL is null");
			return false;
		}

	//	ClipMinimal ugcClip = getClipRBT(ss.subscriberFile());
		Clip ugcClip = getClipRBT(ss.subscriberFile());
		if(ugcClip == null || ugcClip.getAlbum() == null) {
			logger.info("RBT:: The clip (set for subscriber " + ss.subID()
					+ ") with wav file name " + ss.subscriberFile()
					+ " not found OR doesnot have a creator(column ALBUM.)");
			return false;
		}

		String amtToCredit = null; 
        String amtChargedStr = null; 
        String ssInfo  = ss.selectionInfo(); 
        if (ssInfo != null && ssInfo.indexOf("|AMT:") > -1
				&& ssInfo.indexOf(":AMT|") > -1) {
			try {
				int firstIndex = ssInfo.indexOf("|AMT:");
				int secondIndex = ssInfo.indexOf(":AMT|");
				amtChargedStr = ssInfo.substring(firstIndex + 5, secondIndex)
						.trim();
				amtToCredit = (String) m_ugcCreditMap.get(amtChargedStr);
				logger.info("Amount charged for selection is " + amtChargedStr);
				logger.info("Amount to Credit is "
						+ amtToCredit);
				Integer.parseInt(amtToCredit);
				
			} catch (Exception e) {
				logger.info("RBT:: Credit amount not a valid number: "
								+ amtToCredit);
				logger.error("", e);
				return false;
			}
		} 
        if(amtToCredit == null) 
        { 
                logger.info("RBT:: Credit amount is null." ); 
                return false; 
        } 


        /*
		 * ChargeClass cc = (ChargeClass)
		 * m_chargeClassTable.get(ss.classType());
		 * 
		 * if(cc == null || cc.operatorCode3() == null) {
		 * logger.info("RBT:: Invalid classType or credit
		 * amount missing(OPERATOR_CODE_1) : " + ss.classType()); return false; }
		 * try { Integer.parseInt(cc.operatorCode3()); } catch (Exception e) {
		 * logger.info("RBT:: Credit amount
		 * (OPERATOR_CODE_1) not a valid number: " + cc.operatorCode3() + " for
		 * classtype" + cc.classType()); return false; }
		 */
		String url = getParamAsString("DAEMON","UGC_CREDIT_URL", null);
		url = url + "mdn=" + ugcClip.getAlbum() + "&partyMdn=" + ss.subID() + "&amount="
		+ amtToCredit + "&dateOfTransaction=" + ugcDateFormat.format(ss.setTime())
				+ "&transactionId=" + transactionID;
		Integer statusCode = new Integer(-1);
		StringBuffer response = new StringBuffer();
		addToUGCAccounting("UGC-REQUEST", url, "NA", null);
		Tools.callURL(url, statusCode, response);
		addToUGCAccounting("UGC-RESPONSE", url, "RESPONSE:" + response.toString(), null);
		if(response.toString().startsWith("ERROR=0000")) {
			logger.info("RBT:: Successsful charging done for : "
					+ ugcClip.getAlbum());
			if(getParamAsBoolean("DAEMON","SEND_SMS_TO_UGC_CREATOR","FALSE")) {
				String smsText = Tools.findNReplaceAll(getParamAsString("DAEMON","SMS_TEXT_FOR_UGC_CREATOR",m_SmsTextForUgcCreator), "%CALLER_ID", ss
						.subID());
				String promoId = ss.subscriberFile().substring(8, 17);
				smsText = Tools.findNReplaceAll(smsText, "%PROMO_ID", promoId);
				smsText = Tools.findNReplaceAll(smsText, "%AMOUNT", amtToCredit);
				try {
					if(smsText != null)
						Tools.sendSMS(getParamAsString("DAEMON","SENDE_NO",null), ugcClip.getAlbum(), smsText, getParamAsString("DAEMON","SEND_SMS_MASS_PUSH", "FALSE"));
				}
				catch (Exception e) {
					logger.error("", e);
				}
			}
			return true;
		}
		else if(response.toString().startsWith("ERROR=1111")) {
			logger.info("RBT:: HTTP response is : Wrong Username or password.");
			return false;
		}
		else if(response.toString().startsWith("ERROR=4444")) {
			logger.info("RBT:: HTTP response is : Server Error");
			return false;
		}
		else if(response.toString().startsWith("ERROR=0220")) {
			logger.info("RBT:: HTTP response is : Server Timeout");
			return false;
		}
		else {
			logger.info("RBT:: Unknown HTTP Response");
			return false;
		}
	}
	
	private Clip getClipRBT(String rbt_wav) {
	/*	while(!rbtDBManager.isCacheInitialized())
		{
			logger.info("Caching Being Done so waiting");
			try
			{
				Thread.sleep(5*1000);
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		*/
	//	return rbtDBManager.getClipRBT(rbt_wav);
		return rbtCacheManager.getClipByRbtWavFileName(rbt_wav);
		}
	
	private void getUGCCreditTable() 
    { 
		String ugcCreditMap = null;
		ugcCreditMap = getParamAsString("COMMON","UGC_CREDIT_AMT_MAP", null);
		
        if(ugcCreditMap != null && ugcCreditMap.length() > 0) 
        { 
                StringTokenizer stUgc = new StringTokenizer(ugcCreditMap, ","); 
                String chargeAmt = null; 
                String creditAmt = null; 
                while(stUgc.hasMoreTokens()) 
                { 
                        chargeAmt = stUgc.nextToken().trim(); 
                        if(stUgc.hasMoreTokens()) 
                                creditAmt = stUgc.nextToken().trim(); 
                        if(chargeAmt != null && creditAmt != null) 
                                m_ugcCreditMap.put(chargeAmt, creditAmt); 
                        chargeAmt = null; 
                        creditAmt = null; 
                } 
        } 
        logger.info("m_ugcCreditMap is "+m_ugcCreditMap); 
    } 
	
	public static void addToUGCAccounting(String type, String request, String response, String ip) {
		try {
			if(ugcAccounting != null) {
				HashMap acMap = new HashMap();
				acMap.put("APP_ID", "RBT");
				acMap.put("TYPE", type);
				acMap.put("SENDER", "NA");
				acMap.put("RECIPIENT", "NA");
				acMap.put("REQUEST_TS", request);
				acMap.put("RESPONSE_TIME_IN_MS", response);
				acMap.put("CALLING_MODE", "NA");
				acMap.put("CALLBACK_MODE", "NA");
				acMap.put("DATA_VOLUME", "NA");
				acMap.put("SMSC_MESSAGE_ID", ip);
				acMap.put("STATUS", (new SimpleDateFormat("yyyyMMddHHmmssms")).format((new Date(
						System.currentTimeMillis()))));
				if(ugcAccounting != null) {
					ugcAccounting.generateSDR("sms", acMap);
					logger.info("RBT::Writing to the accounting file");
				}
				acMap = null;
			}
		}
		catch (Exception e) {
			logger.info("RBT::Exception caught "
					+ e.getMessage());
		}
	}
	
	public static void createUGCAccounting() {
		ugcAccounting = Accounting.getInstance(m_sdrUGCWorkingDir, m_sdrUGCSize, m_sdrUGCInterval,
				m_sdrUGCRotation, m_sdrUGCBillingOn);
		if(ugcAccounting == null)
			logger.info("RBT::Accounting class can not be created");
	}
	
	 private int getParamAsInt(String type, String param, int defaultVal)
	 {
	    	try{
	    		String paramVal = m_rbtParamCacheManager.getParameter(type, param, defaultVal+"").getValue();
	    		return Integer.valueOf(paramVal);   		
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defaultVal;
	    	}
	 }
	 private String getParamAsString(String type, String param, String defualtVal)
	 {
	    	try{
	    		return m_rbtParamCacheManager.getParameter(type, param, defualtVal).getValue();
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defualtVal;
	    	}
	 }
	 
	 private boolean getParamAsBoolean(String type, String param, String defaultVal)
	  {
	    	try{
	    		return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defaultVal.equalsIgnoreCase("TRUE");
	    	}
	  }

}
