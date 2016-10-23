package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ChargeClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.common.debug.DebugManager;

public class RBTGiftDaemon 
{
	private static Logger logger = Logger.getLogger(RBTGiftDaemon.class);
	
	RBTDBManager dbmanager = null;
	ParametersCacheManager rbtParamCacheManager = null;
	RBTCacheManager rbtCacheManager = null;
	ChargeClassCacheManager chargeClassCacheManager = null;
	
	static int STATUS_SUCCESS = 1;
	static int STATUS_ALREADY_ACTIVE = 2;
	static int STATUS_ALREADY_CANCELLED = 3;
	static int STATUS_NOT_AUTHORIZED = 4;
	static int STATUS_TECHNICAL_FAILURE = 5;
	static String STATUS_ERROR = "ERROR";
	String _class = "RBTGiftDaemon";

	public RBTGiftDaemon() throws Exception 
	{
		rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
    	dbmanager = RBTDBManager.getInstance();

    	rbtCacheManager = RBTCacheManager.getInstance();
    	chargeClassCacheManager = CacheManagerUtil.getChargeClassCacheManager();
	}

	public static void IntializeDebugger() {
		String strComponentName = "RBTGiftDaemon";
		String strPrefix = String.valueOf((Calendar.getInstance()).get(Calendar.DATE)) + "-";
		String strTraceFile = strPrefix + "RBTGiftDaemonTrace.log";
		String strErrorFile = strPrefix + "RBTGiftDaemonError.log";
		String strPerformanceFile = null;
		boolean bConsoleEcho = false;
		long lTraceFileSizeLimitBytes = 10000 * 1024;
		int iTraceFileTimeLimitHrs = 10000;
		int iLogFileDebugLevel = 2;
		int iDebugLevel = 6;
		int iLogFileRotationLevel = 5;
		DebugManager.init(iDebugLevel, strComponentName, "..", strTraceFile, strErrorFile,
				strPerformanceFile, bConsoleEcho, lTraceFileSizeLimitBytes, iTraceFileTimeLimitHrs,
				iLogFileDebugLevel, iLogFileRotationLevel);
	}

	public boolean isUserSubscribed(String mobileNumber) throws Exception {
		String response = connectToRemote(mobileNumber, "rbt_status.jsp?subscriber_id="
				+ mobileNumber);
		if(response != null && response.indexOf("INACTIVE") != -1) {
			logger.info(mobileNumber + " is a not subscribed.");
			return false;
		}
		else if(response != null && response.indexOf("ACTIVE") != -1) {
			logger.info(mobileNumber + " is a already subscribed.");
			return true;
		}
		else {
			throw new Exception("Cannot find subscriber status for :" + mobileNumber);
		}
	}

	public boolean isRemoteSub(String strSubID) {
		if(strSubID == null)
			return false;
		/*
		 * if (m_remotePrefix == null) return false;
		 */
		String subscriber = subID(strSubID);
		if(dbmanager.isValidPrefix(subscriber))
			return false;
		// if(RBTDBManager.init(m_dbURL, m_usePool,
		// m_countryPrefix).isValidOperatorPrefix(subscriber))
		if (RBTMOHelper.init().getURL(subscriber) != null)
			return true;

		return false;
	}

	public void processRecords() {
		String viralType = "GIFT";
		String acceptedGift = "ACCEPT_ACK";
		String rejectedGift = "REJECT_ACK";
		while (true) {
			System.out.println("Started processing.....");
			// Gift RBT
			String cdrFileName = null;
			if(getParamAsString("GIFT", "GIFT_WRITE_CDR","TRUE").equalsIgnoreCase("TRUE")
					|| getParamAsString("GIFT", "GIFT_WRITE_CDR","YES").equalsIgnoreCase("YES")) {
				String cdrPrefix = getParamAsString("GIFT","GIFT_CDR_PREFIX", null);
				String cdrExtension =getParamAsString("GIFT","GIFT_CDR_EXTENSION", null);
				String cdrDateFormat = getParamAsString("GIFT","GIFT_CDR_DATE_FORMAT", null);
				cdrFileName = cdrPrefix + getCDRDate(getSysDate(null), cdrDateFormat)
						+ cdrExtension;
			}
			logger.info("Getting subscribers of type GIFT...");
			ViralSMSTable[] viral = dbmanager.getViralSMSByType(viralType);
			for(int i = 0; viral != null && i < viral.length; i++) {
				if(viral[i].count() < getParamAsInt("GIFT", "GIFT_MAX_RETRY_COUNT", 3)) {
					boolean status = false;
					try {
						status = giftRBT(viral[i].subID(), viral[i].callerID(), viral[i].clipID(),
								viral[i].sentTime());
					}
					catch (Exception e) {
						status = false;
					}
					if(status) {
						logger.info("Successfully processed record.");
						int amount = 10;
						if(getParamAsString("GIFT", "GIFT_WRITE_CDR","TRUE").equalsIgnoreCase("TRUE")
								|| getParamAsString("GIFT", "GIFT_WRITE_CDR","YES").equalsIgnoreCase("YES")) {
							logger.info("Going to write the CDR....");
							
							if(viral[i].clipID().startsWith("C")) 
							{
								amount = 30;
								try {
									Category category =rbtCacheManager.getCategory(Integer.parseInt(viral[i].clipID().substring(1))); // TODO
									if(category != null) 
									{
										ChargeClass cClass = chargeClassCacheManager.getChargeClass(category.getClassType());
										if(cClass == null) 
										{
											logger.info("Charge class not found for type :"
															+ category.getClassType());
										}
										else 
										{
											amount = Integer.parseInt(cClass.getAmount());
										}
									}
								}
								catch (Exception e) {
									e.printStackTrace();
									logger.error("", e);
								}
							}
							else {
								try {
									Clip clips = rbtCacheManager.getClip(Integer.parseInt(viral[i].clipID()));
									if(clips != null) {
										ChargeClass cClass = chargeClassCacheManager.getChargeClass(clips.getClassType());
										if(cClass == null) {
											logger.info("Charge class not found for type :"
															+ clips.getClassType());
										}
										else {
											amount = Integer.parseInt(cClass.getAmount());
										}
									}
								}
								catch (Exception e) {
									e.printStackTrace();
									logger.error("", e);
								}
							}
							try {
								if(isUserSubscribed(viral[i].callerID())) {
									amount = getParamAsInt("GIFT", "GIFT_SUBSCRIBED_USER_COST",1) + amount;
								}
								else {
									amount = getParamAsInt("GIFT", "GIFT_UNSUBSCRIBED_USER_COST",1) + amount;
								}
							}
							catch (Exception e) {
								amount = getParamAsInt("GIFT", "GIFT_UNSUBSCRIBED_USER_COST",1) + amount;
								e.printStackTrace();
								logger.info("ERROR::could not get the subscription status for "
												+ viral[i].callerID());
								logger.error("", e);
							}

							logger.info("Total amount to be charged :" + amount);

							// find the class type of each amount;
							String classType = "UNKNOWN";
							writeCDR(cdrFileName, viral[i].subID(), viral[i].callerID(), viral[i]
									.clipID(), viral[i].sentTime(), getSysDate(null), classType,
									amount);
						}
						dbmanager.updateViralPromotion(viral[i].subID(), viral[i].callerID(),
								viral[i].sentTime(), viral[i].type(), "GIFTED", viral[i].setTime(),
								viral[i].selectedBy() + ":" + amount,null);

					}
					else {
						dbmanager.setSearchCount(viral[i].subID(), viral[i].type(), (viral[i]
								.count() + 1));
					}
				}
				else {
					logger.info("Retry count exceeded the max retry count."
							+ viral[i].subID() + "->" + viral[i].callerID());
				}
			}
			// POST Accept Acknowledge
			logger.info("Getting subscribers of type ACCEPT_ACK...");
			viral = dbmanager.getViralSMSByType(acceptedGift);
			for(int i = 0; viral != null && i < viral.length; i++) {
				if(viral[i].count() < getParamAsInt("GIFT", "GIFT_MAX_RETRY_COUNT", 3)) {
					boolean status = false;
					try {
						String response = connectToRemote(viral[i].subID(),
								"rbt_gift_acknowledge.jsp?subscriber_id=" + viral[i].subID()
										+ "&gifted_to=" + viral[i].callerID() + "&clip_id="
										+ viral[i].clipID() + "&status=" + viral[i].type()
										+ "&requested_timestamp=" + viral[i].sentTime().getTime());
						if(response != null && response.indexOf("SUCCESS") != -1) {
							status = true;
						}
					}
					catch (Exception e) {
						logger.error("", e);
						e.printStackTrace();
						status = false;
					}
					try {
						if(status) {
							dbmanager.updateViralPromotion(viral[i].subID(), viral[i].callerID(),
									viral[i].sentTime(), viral[i].type(), "ACCEPTED", viral[i]
											.setTime(), null,null);
						}
						else {
							dbmanager.setSearchCount(viral[i].subID(), viral[i].type(), (viral[i]
									.count() + 1));
						}
					}
					catch (Exception e) {
						logger.error("", e);
						e.printStackTrace();
					}
				}
				else {
					logger.info("Retry count exceeded the max retry count."
							+ viral[i].subID() + "->" + viral[i].callerID());
				}
			}
			// POST Reject Acknowledge
			logger.info("Getting subscribers of type REJECT_ACK...");
			viral = dbmanager.getViralSMSByType(rejectedGift);
			for(int i = 0; viral != null && i < viral.length; i++) {
				if(viral[i].count() < getParamAsInt("GIFT", "GIFT_MAX_RETRY_COUNT", 3)) {
					boolean status = false;
					try {
						String response = connectToRemote(viral[i].subID(),
								"rbt_gift_acknowledge.jsp?subscriber_id=" + viral[i].subID()
										+ "&gifted_to=" + viral[i].callerID() + "&clip_id="
										+ viral[i].clipID() + "&status=" + viral[i].type()
										+ "&requested_timestamp=" + viral[i].sentTime().getTime());
						if(response != null && response.indexOf("SUCCESS") != -1) {
							status = true;
						}
					}
					catch (Exception e) {
						logger.error("", e);
						e.printStackTrace();
						status = false;
					}
					try {
						if(status) {
							dbmanager.updateViralPromotion(viral[i].subID(), viral[i].callerID(),
									viral[i].sentTime(), viral[i].type(), "REJECTED", viral[i]
											.setTime(), null,null);
						}
						else {
							dbmanager.setSearchCount(viral[i].subID(), viral[i].type(), (viral[i]
									.count() + 1));
						}
					}
					catch (Exception e) {
						logger.error("", e);
						e.printStackTrace();
					}
				}
				else {
					logger.info("Retry count exceeded the max retry count."
							+ viral[i].subID() + "->" + viral[i].callerID());
				}
			}

			// FTP the CDR files.
//			ftpFiles();

			// sleep
			try {
				logger.info("Sleeping for "+ getParamAsInt("GIFT", "GIFT_SLEEP_INTERVAL", 2) + " minutes....");
				Thread.sleep(getParamAsInt("GIFT", "GIFT_SLEEP_INTERVAL", 2) * 1000 * 60);
			}
			catch (InterruptedException e) {
				return;
			}
		}
	}

	/*private void ftpFiles() {
		String fName = "ftpFiles";
		logger.info("Now coming to ftp files...");
		File f = new File(m_cdrPath);
		File[] files = f.listFiles();

		String ftpHost = rbtGiftDaemonConfig.getParameter("GIFT_CDR_FTP_HOST");
		String ftpPort = rbtGiftDaemonConfig.getParameter("GIFT_CDR_FTP_PORT");
		String ftpUsername = rbtGiftDaemonConfig.getParameter("GIFT_CDR_FTP_USERNAME");
		String ftpPassword = rbtGiftDaemonConfig.getParameter("GIFT_CDR_FTP_PASSWORD");
		String ftpMode = rbtGiftDaemonConfig.getParameter("GIFT_CDR_FTP_MODE");
		String ftpTarget = rbtGiftDaemonConfig.getParameter("GIFT_CDR_FTP_TARGET");

		int mode = 0;

		if(ftpMode != null && ftpMode.trim().equalsIgnoreCase("ACTIVE"))
			mode = 1;

		com.onmobile.apps.ringbacktones.common.FTPHandler.init();
		for(int i = 0; files != null && i < files.length; i++) {
			if(!files[i].getName().endsWith("comp")) {
				if(FTPHandler.upload(ftpHost, Integer.parseInt(ftpPort), ftpUsername, ftpPassword,
						mode, m_cdrPath, ftpTarget, files[i].getName()))
					files[i].renameTo(new File(files[i].getAbsolutePath() + ".comp"));
			}
		}
	}*/

	private void writeCDR(String cdrFilename, String subscriberId, String callerId, String clipId,
			Date requestedTimestamp, Date deliveredTimestamp, String classType, int amount) {
		try {
			String cdrPath = getParamAsString("GIFT", "GIFT_CDR_PATH", null);
			String subscriberIdPrefix = getParamAsString("GIFT","GIFT_CDR_SUBSCRIBERID_PREFIX", null);
			File f = new File(cdrPath);
			if(!f.exists())
				f.mkdirs();
			String cdrFile = cdrPath + File.separatorChar + cdrFilename;
			f = new File(cdrFile);
			StringBuffer file = new StringBuffer();
			int count = 0;
			if(f.exists()) {
				FileReader fr = new FileReader(cdrFile);
				BufferedReader br = new BufferedReader(fr);
				String line = br.readLine();
				if(line != null)
					count = Integer.parseInt(line.trim());
				else
					line = br.readLine();

				int temp = 0;
				while (line != null) {
					file.append(line);
					file.append("\r\n");
					line = br.readLine();
					temp++;
				}
				br.close();
				fr.close();
				if(temp != count) {
					count = temp;
				}
			}
			String record = subscriberIdPrefix + subscriberId + ","
					+ getCDRDate(requestedTimestamp, "yyMMddHHmmss") + ","
					+ getCDRDate(requestedTimestamp, "yyMMddHHmmss") + ","
					+ getCDRDate(deliveredTimestamp, "yyMMddHHmmss") + "," + subscriberIdPrefix
					+ callerId + "," + clipId + "," + classType + "," + amount;
			file.append(record);
			file.append("\r\n");
			count++;

			try {
				FileWriter fw = new FileWriter(cdrFile);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("" + count);
				bw.newLine();
				bw.flush();
				bw.write(file.toString());
				bw.flush();
				bw.close();
				fw.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.error("", e);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("", e);
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.error("", e);
		}
	}

	private boolean giftRBT(String gifter, String giftee, String clipId, Date requestedTimestamp)
			throws Exception {
		String response = connectToRemote(giftee, "rbt_gift.jsp?subscriber_id=" + giftee
				+ "&gifted_by=" + gifter + "&clip_id=" + clipId + "&requested_timestamp="
				+ requestedTimestamp.getTime());
		if(response != null && response.indexOf("SUCCESS") != -1) {
			logger.info("Gift successful for the following request gifter "
					+ gifter + " to " + giftee + " for clip Id " + clipId);
			return true;
		}
		else {
			logger.info("Gift failed for the following request gifter " + gifter
					+ " to " + giftee + " for clip Id " + clipId);
			return false;
		}
	}

	/*
	 * private Prefix getURL(String strPrefix) { return
	 * (RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix)
	 * .getURL(strPrefix)); }
	 */

	public String connectToRemote(String strSubID, String strMsg) {
		try {
			String strURL = null;
			// Prefix sitePrefix = getURL(strSubID.substring(0, 4));
			String siteURL = RBTMOHelper.init().getURL(strSubID);
			// if (sitePrefix != null && sitePrefix.url() != null)
			if(siteURL != null) {
				// String siteURL = sitePrefix.url();
				siteURL = Tools.findNReplaceAll(siteURL, "rbt_sms.jsp", "");
				siteURL = Tools.findNReplaceAll(siteURL, "?", "");
				// strURL = siteURL + URLEncoder.encode(strMsg);
				strURL = siteURL + strMsg;
			}
			else
				return STATUS_ERROR;
			if(strURL != null) {
				return callURL(strURL);
			}
			return STATUS_ERROR;
		}
		catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			return STATUS_ERROR;
		}
	}

	private String callURL(String strURL) {
		GetMethod get = new GetMethod();
		String response = null;
		int m_timeOutInMilliSecond = 20000;
		try {
			System.out.println(strURL);
			URL oSrc = new URL(strURL);
			String m_strHostIp = oSrc.getHost();
			int m_iHostPort = oSrc.getPort();
			logger.info("URL = " + strURL);
			logger.info("Host IP = " + m_strHostIp);
			logger.info("Host Port = " + m_iHostPort);
			logger.info("Time out in milli second = " + m_timeOutInMilliSecond);
			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(m_strHostIp, m_iHostPort);
			HttpClient client = new HttpClient();
			get = new GetMethod(strURL);
			client.setTimeout(m_timeOutInMilliSecond);
			long startTime = System.currentTimeMillis();
			logger.info("Start time in millisecond: " + startTime);
			int statusCode = client.executeMethod(ohcfg, get);
			response = get.getResponseBodyAsString();
			long endTime = System.currentTimeMillis();
			logger.info("End time in millisecond: " + endTime);
			logger.info("Diff in millisecond: " + (endTime - startTime));
			logger.info("Response Code:" + statusCode);
			logger.info("Response:[" + response + "]");
			if(statusCode == 200)
				return response;
			else
				return STATUS_ERROR;
		}
		catch (MalformedURLException e) {
			logger.error("", e);
			e.printStackTrace();
			return STATUS_ERROR;
		}
		catch (HttpException e) {
			logger.error("", e);
			e.printStackTrace();
			return STATUS_ERROR;
		}
		catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
			return STATUS_ERROR;
		}
		finally {
			if(get != null)
				get.releaseConnection();
		}
	}

	/*
	 * public int isValidSub(String strSubID, Hashtable reason) { String
	 * subscriber = subID(strSubID); if (strSubID.length() < 10) {
	 * reason.put("Reason", m_invalidPrefix); return STATUS_NOT_AUTHORIZED; } if
	 * (subscriber.startsWith("0")) { subscriber = subscriber.substring(1); } if
	 * (subscriber.startsWith("+91")) { subscriber = subscriber.substring(3); }
	 * if (subscriber.startsWith("91")) { subscriber = subscriber.substring(2); }
	 * try { Long.parseLong(strSubID); } catch (Exception e) {
	 * reason.put("Reason", m_invalidPrefix); return STATUS_NOT_AUTHORIZED; }
	 * for (int i = 0; i < m_validPrefix.length; i++) { if
	 * (subscriber.substring(0, 4).equalsIgnoreCase(m_validPrefix[i])) return
	 * STATUS_SUCCESS; } reason.put("Reason", m_invalidPrefix); return
	 * STATUS_NOT_AUTHORIZED; }
	 * 
	 */private String subID(String subscriberID) {
		return (dbmanager.subID(subscriberID));
	}

	public static Date getSysDate(Date d) {
		Calendar cal = Calendar.getInstance();
		if(d != null)
			cal.setTime(d);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public String getCDRDate(Date dt, String format) {
		if(dt == null)
			dt = Calendar.getInstance().getTime();
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(dt);
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			String tmpdt = sdf.format(dt);
			return tmpdt;
		}
		catch (Exception ex1) {
			logger.error("", ex1);
			return null;
		}
	}

	public static void main(String[] args) {
		try {
			RBTGiftDaemon giftDaemon = new RBTGiftDaemon();
			giftDaemon.processRecords();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

		
	public boolean getParamAsBoolean(String type, String param, String defaultVal)
    {
    	try{
    		return rbtParamCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
    	}catch(Exception e){
    		logger.info("Unable to get param ->"+param +"  type ->"+type);
    		return defaultVal.equalsIgnoreCase("TRUE");
    	}
    }
	public String getParamAsString(String type, String param, String defaultVal)
	{
	    	try{
	    		return rbtParamCacheManager.getParameter(type, param, defaultVal).getValue();
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defaultVal;
	    	}
	}
	public int getParamAsInt(String type, String param, int defaultVal)
	{
	    	try{
	    		String paramVal = rbtParamCacheManager.getParameter(type, param, defaultVal+"").getValue();
	    		return Integer.valueOf(paramVal);   		
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defaultVal;
	    	}
	}

}