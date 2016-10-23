/*
 * Created on Nov 21, 2004
 *  
 */
package com.onmobile.apps.ringbacktones.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.common.debug.DebugManager;
import com.onmobile.common.exception.OnMobileException;

/**
 * @author shrihari
 *  
 */
public class Tools
{
	private static Logger logger = Logger.getLogger(Tools.class);
	
	private static final String _DATEFORMAT12 = "yyyyMMddhhmmss a";

	private static final String _FILENAME_DATEFORMAT = "yyyy-MMM-dd";

	private static String m_module = "RBT";
	private static String m_smsAppId = "RBT";


	private static String m_gatherer_logfile_name = "Gatherer.log";

	private static StringBuffer sb = new StringBuffer();

	private static HttpClient m_httpClient = null; 
	private static RBTConnector rbtConnector = null;
	private static HttpClient m_httpClient1 = null; 
	private static HttpClient httpClientOfSMSHelper = null; 
	private static HttpClient m_httpClient3 = null;

	private static String m_smsHelperUrl = null;
	
	static URLCodec m_urlEncoder = new URLCodec();
	static boolean useDBForSMS = true; 

	public static boolean init(String module, boolean console){
		
		useDBForSMS = RBTParametersUtils.getParamAsBoolean("COMMON", "USE_DB_FOR_SMS","TRUE");
		rbtConnector = RBTConnector.getInstance();
		boolean init = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "INITIALIZE_DEBUG_MGR", "TRUE");
		if(!init) {
			return true;
		}
		//initializing the common.DebguManger	
		int logLevel = 6;
		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
			logLevel = Integer.parseInt(resourceBundle.getString("LOG_LEVEL"));
		} catch(Exception e) {
			e.printStackTrace();
			logLevel = 6;
		}
		m_module = module;
		
		//Setting default APP ID
		m_smsAppId = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_SMS_APP_ID", module);
		
		String rootdir = System.getProperty("ONMOBILE", null);        
		if (rootdir == null) {
			rootdir = System.getProperty("LOG_PATH", null);
		}
		if (rootdir == null) {
			return false;
		}
		String logFileName = module + "_trace";
		String errorFileName = module + "_error";
		Object ret = DebugManager.init(logLevel, module, rootdir + File.separator + "log", logFileName, errorFileName,
				"size", 10485760L, 20, true);
		if (ret == null) {
			System.out.println("The DebugManager couldn't be initialised.");
			return false;
		}
		return true;
	}

	public static String getFormattedDate(long millis, String pattern)
	{
		return getFormattedDate(new java.util.Date(millis), pattern);
	}

	public static String getFormattedDate(java.util.Date date, String pattern)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String datestr = sdf.format(date);
		if (pattern.equalsIgnoreCase(_DATEFORMAT12))
		{
			String temp = datestr.substring(0, 16)
			+ datestr.substring(datestr.length() - 3, datestr.length());
			datestr = temp;
		}
		return datestr;
	}
	
	public static boolean callURL(String strURL, Integer statusCode,
			StringBuffer response, boolean useProxy, String proxyHost, int proxyPort, boolean toRetry)
	{
		return callURL(strURL, statusCode, response, useProxy, proxyHost, proxyPort, toRetry, 10000);
	}

	public static boolean callURL(String strURL, Integer statusCode,
			StringBuffer response, boolean useProxy, String proxyHost, int proxyPort, boolean toRetry, int timeOut)
	{
		GetMethod get = null;
		int timeOutInMilliSecond = timeOut;
		try
		{
			strURL = strURL.trim();
			URL oSrc = new URL(strURL);
			String strHostIp = oSrc.getHost();
			int iHostPort = oSrc.getPort();

			logger.info("URL: " + strURL 
					+ " Host IP: " + strHostIp
					+ " Host Port: " + iHostPort
					+ " Time out in milli second: " + timeOutInMilliSecond);

			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(strHostIp, iHostPort);

			java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
			String IPAddress = ip.getHostAddress();

			if (!strHostIp.equalsIgnoreCase("localhost") && !strHostIp.equalsIgnoreCase(IPAddress))
				if (useProxy && proxyHost != null && proxyPort != -1)
					ohcfg.setProxy(proxyHost, proxyPort);
			if(m_httpClient == null) 
			{ 
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				connectionManager.getParams().setStaleCheckingEnabled(true);
				connectionManager.getParams().setMaxConnectionsPerHost(ohcfg,10);
				connectionManager.getParams().setMaxTotalConnections(20);
				connectionManager.getParams().setSoTimeout(timeOutInMilliSecond);
				connectionManager.getParams().setConnectionTimeout(timeOutInMilliSecond);
				m_httpClient = new HttpClient(connectionManager);
				DefaultHttpMethodRetryHandler retryhandler =null;
				if(toRetry){
					 retryhandler = new DefaultHttpMethodRetryHandler(3, false);
				}else{
					 retryhandler = new DefaultHttpMethodRetryHandler(0, false);
				}
				m_httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
				m_httpClient.getParams().setSoTimeout(timeOutInMilliSecond);
			}
			
			get = new GetMethod(strURL);
			long startTime = System.currentTimeMillis();
			statusCode = new Integer(m_httpClient.executeMethod(ohcfg, get));
			response.append(get.getResponseBodyAsString());
			long endTime = System.currentTimeMillis();
			logger.info("URL: " + strURL 
					+ " Response time: " + (endTime - startTime)
					+ " Response Code: " + statusCode 
					+ " Response: " + response.toString().trim() + "");
			if (statusCode.intValue() == 200)
				return true;
			else
				return false;
		}
		catch (Throwable e)
		{
			logger.error("", e);
			return false;
		}
		finally
		{
			if (get != null)
				get.releaseConnection();
		}
	
	}

	public static boolean callURL(String strURL, Integer statusCode,
			StringBuffer response, boolean useProxy, String proxyHost, int proxyPort)
	{
		return callURL(strURL, statusCode, response, useProxy, proxyHost, proxyPort, 10000);
	}
	
	public static boolean callURL(String strURL, Integer statusCode,
			StringBuffer response, boolean useProxy, String proxyHost, int proxyPort, int timeoutInMilliSecond)
	{
		GetMethod get = null;
		try
		{
			strURL = strURL.trim();
			URL oSrc = new URL(strURL);
			String strHostIp = oSrc.getHost();
			int iHostPort = oSrc.getPort();

			logger.info("URL: " + strURL 
					+ " Host IP: " + strHostIp
					+ " Host Port: " + iHostPort
					+ " Time out in milli second: " + timeoutInMilliSecond);

			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(strHostIp, iHostPort);

			java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
			String IPAddress = ip.getHostAddress();

			if (!strHostIp.equalsIgnoreCase("localhost") && !strHostIp.equalsIgnoreCase(IPAddress))
				if (useProxy && proxyHost != null && proxyPort != -1)
					ohcfg.setProxy(proxyHost, proxyPort);
			if(m_httpClient == null) 
			{ 
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				connectionManager.getParams().setStaleCheckingEnabled(true);
				connectionManager.getParams().setMaxConnectionsPerHost(ohcfg,10);
				connectionManager.getParams().setMaxTotalConnections(20);
				connectionManager.getParams().setSoTimeout(timeoutInMilliSecond);
				connectionManager.getParams().setConnectionTimeout(timeoutInMilliSecond);
				m_httpClient = new HttpClient(connectionManager);
				DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false);
				m_httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
				m_httpClient.getParams().setSoTimeout(timeoutInMilliSecond);

			}
			
			get = new GetMethod(strURL);
			long startTime = System.currentTimeMillis();
			statusCode = new Integer(m_httpClient.executeMethod(ohcfg, get));
			response.append(get.getResponseBodyAsString());
			long endTime = System.currentTimeMillis();
			logger.info("URL: " + strURL 
					+ " Response time: " + (endTime - startTime)
					+ " Response Code: " + statusCode 
					+ " Response: " + response.toString().trim() + "");
			if (statusCode.intValue() == 200)
				return true;
			else
				return false;
		}
		catch (Throwable e)
		{
			if (e instanceof ConnectTimeoutException || e instanceof SocketTimeoutException)
				response.append("TimeOutException");
			logger.error("", e);
			return false;
		}
		finally
		{
			if (get != null)
				get.releaseConnection();
		}
	}
	
	/*
	 * written by @abhinav.anand
	 * */
	public static boolean callURL(String strURL, Integer statusCode,
			StringBuffer response, boolean useProxy, String proxyHost, int proxyPort, int connectionTimeOut, int timeOut)
	{
		GetMethod get = new GetMethod();
		int timeOutInMilliSecond = timeOut;
		try
		{
			strURL = strURL.trim();
			URL oSrc = new URL(strURL);
			String strHostIp = oSrc.getHost();
			int iHostPort = oSrc.getPort();

			logger.info("URL: " + strURL 
					+ " Host IP: " + strHostIp
					+ " Host Port: " + iHostPort
					+ " Time out in milli second: " + timeOutInMilliSecond);

			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(strHostIp, iHostPort);

			java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
			String IPAddress = ip.getHostAddress();

			if (!strHostIp.equalsIgnoreCase("localhost") && !strHostIp.equalsIgnoreCase(IPAddress))
				if (useProxy && proxyHost != null && proxyPort != -1)
					ohcfg.setProxy(proxyHost, proxyPort);
			if ( m_httpClient1 == null ) {
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				connectionManager.getParams().setStaleCheckingEnabled(true);
				connectionManager.getParams().setMaxConnectionsPerHost(ohcfg,10);
				connectionManager.getParams().setMaxTotalConnections(20);
				connectionManager.getParams().setSoTimeout(timeOutInMilliSecond);
				connectionManager.getParams().setConnectionTimeout(timeOutInMilliSecond);
				m_httpClient1 = new HttpClient(connectionManager);
				DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false);
				m_httpClient1.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
				m_httpClient1.getParams().setSoTimeout(timeOutInMilliSecond);
			}
			get = new GetMethod(strURL);
			long startTime = System.currentTimeMillis();
			statusCode = new Integer(m_httpClient1.executeMethod(ohcfg, get));

			byte[] arrByte=new byte[1024];
			InputStream is=get.getResponseBodyAsStream();
			java.util.zip.GZIPInputStream gIn = new java.util.zip.GZIPInputStream(is);

			int x=0;
			do{
				x=gIn.read(arrByte, 0,arrByte.length);
				if(x<1024&& x!=-1){
					byte[] arrByte1=new byte[x];
					for(int i=0;i<x;i++){
						arrByte1[i]=arrByte[i];
					}
					response.append(new String(arrByte1));
				}
				else if((x==1024) && (x!=-1)){
					response.append(new String(arrByte));
				}
				else
					continue;
			}while(x!=-1);

			gIn.close();
			
			long endTime = System.currentTimeMillis();
			logger.info("URL: " + strURL 
					+ " Response time: " + (endTime - startTime)
					+ " Response Code: " + statusCode 
					+ " Response: " + response.toString().trim() + "");

			if (statusCode.intValue() == 200)
				return true;
			else
				return false;
		}
		catch (Throwable e)
		{
			logger.error("", e);
			return false;
		}
		finally
		{
			if (get != null)
				get.releaseConnection();
		}
	}

	public static void addToLogFile(String str)
	{
		sb.append(new Date(System.currentTimeMillis()) + " ->" + str + "\n");
	}

	public static void writeLogFile()
	{
		writeLogFileInternal(m_gatherer_logfile_name);
	}

	public static void writeLogFile(String path)
	{
		writeLogFileInternal(path + "/" + m_gatherer_logfile_name);
	}

	private static void writeLogFileInternal(String filePath)
	{
		File reportFile = new File(filePath);
		FileOutputStream fout = null;
		try
		{
			reportFile.createNewFile();
			fout = new FileOutputStream(reportFile);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		try
		{
			fout.write(sb.toString().getBytes());
			fout.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		sb.delete(0, sb.length());
	}

	public static Date getFileDate(String cdrfile)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
		Date datestr = new Date();
		try
		{
			datestr = sdf.parse(cdrfile.substring(1, 16));
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		return datestr;
	}

	public static String getChangedFormatDate(Date TmpDate)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date_str = sdf.format(TmpDate);
		return date_str;
	}

	/*FOR other operator not having rbt.xml*/ 
	//  SEND SMS via Voice Portal 
	public static boolean sendSMSForVoicePortal(String db_url, String sender,
			String msisdn, String msg, boolean needtoDuplicate)
	throws OnMobileException { 
		logger.info("Params - MSISDN: " + msisdn + " msg: " + msg + " db_url: " + db_url);

		//added by Sreekar, this url will be read from the RBT.xml sms db url, whatever passes will be overwritten 
		if(msg == null || msisdn == null || msisdn.equals("") || db_url == null) 
		{
			return false;
		}

		Connection con = null; 
		Statement stmt = null; 
		try{ 
			con = RBTDBManager.init(db_url,4).getConnection(); 
			String tempMsg = msg;
			boolean response = useSMSHelper(db_url, sender, msisdn,
					tempMsg, null, null);
			if (response) {
				return response;
			} 
			m_smsAppId = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_SMS_APP_ID", m_module);
			List<String> subString = new ArrayList<String>(); 
			String lastMessage = msg; 
			if(lastMessage.length() > 160) { 
				while(lastMessage.length() > 160) { 
					String finalMessage = lastMessage.substring(0, 160); 
					int lastIndexofStop = finalMessage.lastIndexOf("."); 
					if(lastIndexofStop < 6) 
						lastIndexofStop = 159; 
					lastMessage = lastMessage.substring(lastIndexofStop, lastMessage.length()); 
					finalMessage = finalMessage.substring(0, lastIndexofStop); 
					lastMessage = lastMessage.trim(); 
					finalMessage = finalMessage.trim(); 
					subString.add(finalMessage); 
				} 
				subString.add(lastMessage); 
			} 
			else { 
				subString.add(lastMessage); 
			} 
			logger.info("Number of sub strings = " + subString.size());  
			for(int i = 0; i < subString.size(); i++) { 
				String query = ""; 
				query += "INSERT INTO MMP_SMS_TABLE (" + 
				"SMS_ID, " + 
				"SMS_REFID, " + 
				"RECIPIENT, " + 
				"SENDER, " + 
				"SMS_TYPE, " + 
				"CREATETIME, " + 
				"LASTUPDATETIME, " + 
				"LASTSUBMITTIME, " + 
				"STATUS, " + 
				"RETRY_COUNT, " + 
				"UD_HEADER, " + 
				"UD_DATA, " + 
				"SMS_TEXT, " + 
				"REPLACE_SMS_TEXT, " + 
				"IS_REPLACED, " + 
				"APP_ID, " + 
				"DSR_STATUS_CODE, " + 
				"TRANSACTION_ID) " + "VALUES (";
				query += "SMS_SEQ.nextval, ";
				query += "null, ";
				query += "'" + msisdn + "', ";
				query += "'" + sender + "', ";
				query += "'S', ";
				query += "SYSDATE, ";
				query += "null, ";
				query += "null, ";
				query += "'C', ";
				query += "null, ";
				query += "null, ";
				query += "null, "; 
				if (subString.get(i) == null)
					query += "null, ";
				else
					query += "'" + findNReplaceAll((String) subString.get(i), "'", "''") + "', ";
				query += "null, ";
				query += "null, ";
				query += "'" + m_smsAppId + "', ";
				query += "null, ";
				query += "'" + System.currentTimeMillis() + "'";
				query += ")"; 
				logger.info("query = " + query);
				stmt = con.createStatement();
				int noOfRowsAffected = stmt.executeUpdate(query);
				logger.info("noOfRowsAffected = " + noOfRowsAffected); 
			} 
		} 
		catch (Throwable e)
		{ 
			logger.error("", e); 
			throw new OnMobileException(e.getMessage()); 
		} 
		finally
		{ 
			try
			{ 
				if(stmt != null) 
					stmt.close(); 
			} 
			catch(Throwable e)
			{
				logger.error("Exception in closing db statement", e); 
			}
			try
			{ 
				if(con != null) 
					RBTDBManager.init(db_url,4).releaseConnection(con);  
			} 
			catch(Throwable e)
			{
				logger.error("Exception in releasing connection", e); 
			}
		} 
		logger.info("Exit"); 
		return true; 
	} 

	public static int[] getIntArrayFromStr(String str) {
		if(str == null) {
			return null;
		}
        StringTokenizer daysToken = new StringTokenizer(str, ",");
        int[] value = new int[daysToken.countTokens()];
        for (int i = 0; daysToken.hasMoreTokens(); i++)
        {
            value[i] = Integer.parseInt(daysToken.nextToken());
        }
        return value;
	}

	public static Date getDatewithoutmmss(String time)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
		Date datestr = new Date();
		try
		{
			datestr = sdf.parse(time);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		return datestr;
	}

	public static Date getdate(String time, String pattern)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		Date datestr = new Date();
		try
		{
			datestr = sdf.parse(time);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		return datestr;
	}

	public static Date getNextInterval(Date currhour, String interval)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(currhour);
		if (interval.equalsIgnoreCase("DAILY"))
			cal.add(Calendar.DATE, 1);
		else
			cal.add(Calendar.HOUR_OF_DAY, 1);
		return cal.getTime();
	}

	public static Date getFormattedCDRDate(String time, String interval)
	{
		SimpleDateFormat sdf = null;
		Date datestr = new Date();
		if (interval.equalsIgnoreCase("DAILY"))
		{
			sdf = new SimpleDateFormat("yyyyMMdd");
			time = time.substring(0, 8);
		}
		else
		{
			sdf = new SimpleDateFormat("yyyyMMddHH");
			time = time.substring(0, 10);
		}

		try
		{
			datestr = sdf.parse(time);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		return datestr;
	}

	public static Date changeDateFormat(Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String date_str = sdf.format(date);
		try
		{
			date = sdf.parse(date_str);
		}
		catch (Exception E)
		{

		}
		return date;
	}

	public static String getDateAsName(java.util.Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(_FILENAME_DATEFORMAT);
		return sdf.format(date);
	}

	public static Date getSummaryDate(String str_date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		try
		{
			date = sdf.parse(str_date);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		return date;
	}

	public static long getStartDate()
	{
		//start of today. so pgm runs for yesterday.
		Calendar _cal = Calendar.getInstance();
		_cal.set(Calendar.HOUR_OF_DAY, 0);
		_cal.set(Calendar.MINUTE, 0);
		_cal.set(Calendar.SECOND, 0);
		_cal.set(Calendar.MILLISECOND, 0);
		return _cal.getTime().getTime();
	}

	public static void moveFile(String destdir, File cdrfile)
	{
		File dest = new File(destdir);
		if (!dest.isDirectory())
		{
			logger.info("creating destination dir: " + destdir);
			if (!dest.mkdirs())
			{
				logger.error("could not create dest dir.");
			}
		}

		String destfile = destdir + File.separator + cdrfile.getName();
		try
		{
			copyFile(cdrfile.getAbsolutePath(), destfile);
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
	}

	public static void copyFile(String source, String destination)
	throws IOException
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try
		{
			logger.info("Copying: " + source + " To: " + destination);
			fis = new FileInputStream(source);
			fos = new FileOutputStream(destination);
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1)
			{
				fos.write(buf, 0, i);
			}
			logger.info(destination + " copied.");
		} finally {
			if(fis != null)
			{
				fis.close();
			}
			if(fos != null)
			{
				fos.close();
			}
		}
	}

	public static void deleteFiles(File[] files)
	{
		if(files == null || files.length == 0) 
		{
			return;
		}
		TXTFileFilter Obj = new TXTFileFilter();
		try
		{
			for (int i = 0; i < files.length; i++)
			{
				if (Obj.accept(files[i]))
					files[i].delete();
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	public static void cleanupFiles(String srcdir, String ext, int old_days)
	{
		try
		{
			File dir = new File(srcdir);
			if (! dir.isDirectory())
			{
				logger.warn("Not a directory: " + srcdir);
				return;
			}
			File[] files = dir.listFiles(new OnmFileFilter(old_days, ext));
			if(files == null || files.length == 0) 
			{
				logger.warn("No files in directory: " + srcdir);
				return;
			}
			for (int i = 0; i < files.length; i++)
			{
				logger.info("Deleting file: " + files[i].getAbsolutePath());
				files[i].delete();
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	//SEND SMS via PromoTool
	//http://10.66.7.122:8000/IntelligentPromotion/SMSSender?sender=<%sender%>&receiver=<%msisdn%>&text=<%smstext%>&userID=onmobile&password=onmobile123 
	public static boolean sendSMS(String sender, String msisdn, String sms) 
	{
		String smsUrl=RBTParametersUtils.getParamAsString("GATHERER", "PROMOTOOL_DND_SMS_URL", null);
		if(smsUrl!=null&&!smsUrl.equalsIgnoreCase(""))
		{
			try {
				smsUrl = smsUrl.replaceAll("%sender%", sender);
				smsUrl = smsUrl.replaceAll("%msisdn%", msisdn);
				smsUrl = smsUrl.replaceAll("%smstext%", getEncodedUrlString(sms));
				Integer statusInt = new Integer(-1);
				StringBuffer result = new StringBuffer();
				logger.info("RBT:: smsUrl: " + smsUrl);
				return Tools.callURL(smsUrl, statusInt, result, false , null,-1);

			} catch (Exception e) {
				logger.error("", e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * DND to implement in copy influencer feature.Check for DND Subscriber and
	 * SEND SMS via Voice Portal. - RBT-11688, RBT-11816
	 * 
	 * @param sender
	 * @param msisdn
	 * @param msg
	 * @param isMassPush
	 * @return
	 * @throws OnMobileExceptionn
	 */
	public static boolean checkDNDSubscriberAndSendSMS(String sender,
			String msisdn, String msg, boolean isMassPush) {
		String dndUrl = RBTParametersUtils.getParamAsString("COMMON",
				"DND_SUBSCRIBER_CHECK_URL", null);
		boolean dndCheck = false;
		if (dndUrl != null && !dndUrl.equalsIgnoreCase("")) {
			try {
				logger.info("RBT:: dndUrl: " + dndUrl);
				if (sender != null) {
					dndUrl = dndUrl.replaceAll("%sender%", sender);
				}
				dndUrl = dndUrl.replaceAll("%SUBSCRIBER_ID%", msisdn);
				StringBuffer result = new StringBuffer();
				dndCheck = callURL(dndUrl, new Integer(-1), result);
				if (dndCheck
						&& result.toString().trim().equalsIgnoreCase("TRUE")) {
					logger.info("RBT:: dndCheck is success and response is :"
							+ result);
					return false;
				} else {
					logger.info("RBT:: dndCheck is not success Params - MSISDN: "
							+ msisdn + " msg: " + msg);
					dndCheck = sendSMS(sender, msisdn, msg, isMassPush);
				}

			} catch (Exception e) {
				logger.error("", e);
				e.printStackTrace();
			}
		} else {
			try {
				logger.info("RBT:: dndCheck url is not configured in DB "
						+ msisdn + " msg: " + msg);
				dndCheck = sendSMS(sender, msisdn, msg, isMassPush);
			} catch (Exception e) {
				logger.info("Exception while Sending SMS");
			}

		}
		return dndCheck;
	}
	
	
	public static boolean sendSMS(String sender, String msisdn, String msg,
			String circleId, String operatorName, boolean isMassPush)
			throws OnMobileException {

		logger.info("Params - MSISDN: " + msisdn + " msg: " + msg);
		//RBT-12607
		if(msg == null || msisdn == null || msisdn.equals("") || (msg!=null && msg.trim().equalsIgnoreCase("NA"))) 
		{
			return false;
		}

		m_smsAppId = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_SMS_APP_ID", m_module);
		String db_url = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_DB_URL", null);
		try
		{
			String tempMsg = msg; 
			boolean response = useSMSHelper(db_url,sender,msisdn,tempMsg, circleId, operatorName); 
			if(response)
			{
				return response;
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		if(!useDBForSMS)
		{
			logger.info("Not adding in mmp table");
			return false;
		}
		Connection con = null;
		Statement stmt = null;
		try
		{
			String smsType = "S";
			if (isMassPush)
				smsType = "M";
			RBTDBManager rbtManager = RBTDBManager.init(db_url, 50);
			con = rbtManager.getSMSConnection();
			if (con == null)
				return false;
			String query = "";
			query += "INSERT INTO MMP_SMS_TABLE (" + 
			"SMS_ID, " + 
			"SMS_REFID, " + 
			"RECIPIENT, " + 
			"SENDER, " + 
			"SMS_TYPE, " + 
			"CREATETIME, " + 
			"LASTUPDATETIME, " + 
			"LASTSUBMITTIME, " + 
			"STATUS, " + 
			"RETRY_COUNT, " + 
			"UD_HEADER, " + 
			"UD_DATA, " + 
			"SMS_TEXT, " + 
			"REPLACE_SMS_TEXT, " + 
			"IS_REPLACED, " + 
			"APP_ID, " + 
			"DSR_STATUS_CODE, " + 
			"TRANSACTION_ID) " + "VALUES (";
			query += "SMS_SEQ.nextval, ";
			query += "null, ";
			query += "'" + msisdn + "', ";
			query += "'" + sender + "', ";
			query += "'" + smsType + "', ";
			query += "SYSDATE, ";
			query += "null, ";
			query += "null, ";
			query += "'C', ";
			query += "null, ";
			query += "null, ";
			query += "null, ";
			if (msg == null)
				query += "null, ";
			else
				query += "'" + findNReplaceAll(msg, "'", "''") + "', ";
			query += "null, ";
			query += "null, ";
			query += "'" + m_smsAppId + "', ";
			query += "null, ";
			query += "'" + System.currentTimeMillis() + "'";
			query += ")";
			logger.info("query = " + query);
			stmt = con.createStatement();
			int noOfRowsAffected = stmt.executeUpdate(query);
		}
		catch (Exception e)
		{
			logger.error("", e);
			throw new OnMobileException(e.getMessage());
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
				if (con != null)
					RBTDBManager.init(db_url, 10).releaseConnection(con);
			}
			catch (Exception e)
			{
			}

		}
		return true;
	 
	}
	
	//SEND SMS via Voice Portal
	public static boolean sendSMS(String sender, String msisdn, String msg, boolean isMassPush) 
	throws OnMobileException
	{
		return sendSMS(sender, msisdn, msg, null, null, isMassPush);
		
	}
    //Including mode to send prefix=y or prefix = n or none of these.
	public static boolean sendSMS(String sender, String msisdn,boolean isMassPush,String msg, String prefix ) 
	throws OnMobileException
	{
		logger.info("Params - MSISDN: " + msisdn + " msg: " + msg);
		if(msg == null || msisdn == null || msisdn.equals("") || (msg!=null && msg.trim().equalsIgnoreCase("NA"))) 
		{
			return false;
		}

		m_smsAppId = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_SMS_APP_ID", m_module);
		String db_url = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_DB_URL", null);
		try
		{
			String tempMsg = msg; 
			boolean response = useSMSHelper(db_url,sender,msisdn,tempMsg,prefix); 
			if(response)
			{
				return response;
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		if(!useDBForSMS)
		{
			logger.info("Not adding in mmp table");
			return false;
		}
		Connection con = null;
		Statement stmt = null;
		try
		{
			String smsType = "S";
			if (isMassPush)
				smsType = "M";
			RBTDBManager rbtManager = RBTDBManager.init(db_url, 50);
			con = rbtManager.getSMSConnection();
			if (con == null)
				return false;
			String query = "";
			query += "INSERT INTO MMP_SMS_TABLE (" + 
			"SMS_ID, " + 
			"SMS_REFID, " + 
			"RECIPIENT, " + 
			"SENDER, " + 
			"SMS_TYPE, " + 
			"CREATETIME, " + 
			"LASTUPDATETIME, " + 
			"LASTSUBMITTIME, " + 
			"STATUS, " + 
			"RETRY_COUNT, " + 
			"UD_HEADER, " + 
			"UD_DATA, " + 
			"SMS_TEXT, " + 
			"REPLACE_SMS_TEXT, " + 
			"IS_REPLACED, " + 
			"APP_ID, " + 
			"DSR_STATUS_CODE, " + 
			"TRANSACTION_ID) " + "VALUES (";
			query += "SMS_SEQ.nextval, ";
			query += "null, ";
			query += "'" + msisdn + "', ";
			query += "'" + sender + "', ";
			query += "'" + smsType + "', ";
			query += "SYSDATE, ";
			query += "null, ";
			query += "null, ";
			query += "'C', ";
			query += "null, ";
			query += "null, ";
			query += "null, ";
			if (msg == null)
				query += "null, ";
			else
				query += "'" + findNReplaceAll(msg, "'", "''") + "', ";
			query += "null, ";
			query += "null, ";
			query += "'" + m_smsAppId + "', ";
			query += "null, ";
			query += "'" + System.currentTimeMillis() + "'";
			query += ")";
			logger.info("query = " + query);
			stmt = con.createStatement();
			int noOfRowsAffected = stmt.executeUpdate(query);
		}
		catch (Exception e)
		{
			logger.error("", e);
			throw new OnMobileException(e.getMessage());
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
				if (con != null)
					RBTDBManager.init(db_url, 10).releaseConnection(con);
			}
			catch (Exception e)
			{
			}

		}
		return true;
	}

	public static boolean sendSMS(String sender, String msisdn, String msg,
			boolean isMassPush, String createTime) throws OnMobileException
			{
		String db_url = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_DB_URL", null);
		boolean bSendSMS = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "SEND_SMS", "TRUE");
		logger.info("Params - MSISDN: " + msisdn + " msg: " + msg
				+ " Config - db_url: " + db_url + " send SMS: "+ bSendSMS);
		if(msg == null || msisdn == null || msisdn.equals("") || !bSendSMS || (msg!=null && msg.trim().equalsIgnoreCase("NA"))) 
		{
			return false;
		}
		
		m_smsAppId = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_SMS_APP_ID", m_module);
		String smsType = "S";
		String timeToSendSMS = "SYSDATE";
		if (isMassPush)
		{
			smsType = "M";
			if (createTime != null)
				timeToSendSMS = createTime;
		}

		Connection con = null;
		Statement stmt = null;
		try
		{
			String tempMsg = msg;
			boolean response = useSMSHelper(db_url, sender, msisdn,
					tempMsg, null, null);
			if (response) {
				return response;
			}
			if(!useDBForSMS)
			{
				logger.info("Not adding in mmp table");
				return false;
			}
			
			RBTDBManager rbtManager = RBTDBManager.init(db_url, 10); 
			con = rbtManager.getSMSConnection();
			if (con == null)
				return false;
			List<String> subString = new ArrayList<String>();
			String lastMessage = msg;
			int msgLength = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "SMS_TEXT_LENGTH", 160);
			if(lastMessage.length() > msgLength)
			{
				while(lastMessage.length() > msgLength)
				{
					String finalMessage = lastMessage.substring(0, msgLength); 

					int lastIndexofStop = finalMessage.lastIndexOf(".");

					if(lastIndexofStop < 6)
						lastIndexofStop = msgLength-1;

					lastMessage = lastMessage.substring(lastIndexofStop, lastMessage.length());
					finalMessage = finalMessage.substring(0, lastIndexofStop);

					lastMessage = lastMessage.trim();
					finalMessage = finalMessage.trim();

					subString.add(finalMessage);
				}
				subString.add(lastMessage);
			}
			else
			{
				subString.add(lastMessage);
			}
			
			logger.info("Number of sub strings = " + subString.size());
			for(int i = 0; i < subString.size(); i++)
			{
				String query = "";
				query += "INSERT INTO MMP_SMS_TABLE (" + 
				"SMS_ID, " + 
				"SMS_REFID, " + 
				"RECIPIENT, " + 
				"SENDER, " + 
				"SMS_TYPE, " + 
				"CREATETIME, " + 
				"LASTUPDATETIME, " + 
				"LASTSUBMITTIME, " + 
				"STATUS, " + 
				"RETRY_COUNT, " + 
				"UD_HEADER, " + 
				"UD_DATA, " + 
				"SMS_TEXT, " + 
				"REPLACE_SMS_TEXT, " + 
				"IS_REPLACED, " + 
				"APP_ID, " + 
				"DSR_STATUS_CODE, " + 
				"TRANSACTION_ID) " + "VALUES (";
				query += "SMS_SEQ.nextval, ";
				query += "null, ";
				query += "'" + msisdn + "', ";
				query += "'" + sender + "', ";
				query += "'" + smsType + "', ";
				query += timeToSendSMS + ", ";
				query += "null, ";
				query += "null, ";
				query += "'C', ";
				query += "null, ";
				query += "null, ";
				query += "null, ";
				if (msg == null)
					query += "null, ";
				else
					query += "'" + findNReplaceAll((String)subString.get(i), "'", "''") + "', ";
				query += "null, ";
				query += "null, ";
				query += "'" + m_smsAppId + "', ";
				query += "null, ";
				query += "'" + System.currentTimeMillis() + "'";
				query += ")";
				logger.info("query: " + query);
				stmt = con.createStatement();
				int noOfRowsAffected = stmt.executeUpdate(query);
				logger.info("noOfRowsAffected: " + noOfRowsAffected);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			throw new OnMobileException(e.getMessage());
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
				if (con != null)
					RBTDBManager.init(db_url, 10).releaseConnection(con);
			}
			catch (Exception e)
			{
			}
		}
		return true;
	}

	public static boolean sendSMS(String sender, String msisdn, String msg,
			String createTime) throws OnMobileException
			{
		String db_url = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_DB_URL", null);
		boolean bSendSMS = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "SEND_SMS", "TRUE");
		logger.info("Params - MSISDN: " + msisdn + " msg: " + msg
				+ " Config - db_url: " + db_url + " send SMS: "+ bSendSMS);
		if(msg == null || msisdn == null || msisdn.equals("") || !bSendSMS || (msg!=null && msg.trim().equalsIgnoreCase("NA"))) 
		{
			return false;
		}

		m_smsAppId = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_SMS_APP_ID", m_module);
		Connection con = null;
		Statement stmt = null;

		try
		{
			//TODO:
			String tempMsg = msg;
			boolean response = useSMSHelper(db_url, sender, msisdn,
					tempMsg, null, null);
			if (response) {
				return response;
			} 
			if(!useDBForSMS)
			{
				logger.info("Not adding in mmp table");
				return false;
			}
			RBTDBManager rbtManager = RBTDBManager.init(db_url, 10);
			con = rbtManager.getSMSConnection();
			if (con == null)
				return false;
			String query = "";
			query += "INSERT INTO MMP_SMS_TABLE (" + 
			"SMS_ID, " + 
			"SMS_REFID, " + 
			"RECIPIENT, " + 
			"SENDER, " + 
			"SMS_TYPE, " + 
			"CREATETIME, " + 
			"LASTUPDATETIME, " + 
			"LASTSUBMITTIME, " + 
			"STATUS, " + 
			"RETRY_COUNT, " + 
			"UD_HEADER, " + 
			"UD_DATA, " + 
			"SMS_TEXT, " + 
			"REPLACE_SMS_TEXT, " + 
			"IS_REPLACED, " + 
			"APP_ID, " + 
			"DSR_STATUS_CODE, " + 
			"TRANSACTION_ID) " + "VALUES (";
			query += "SMS_SEQ.nextval, ";
			query += "null, ";
			query += "'" + msisdn + "', ";
			query += "'" + sender + "', ";
			query += "'S', ";
			query += createTime + ", ";
			query += "null, ";
			query += "null, ";
			query += "'C', ";
			query += "null, ";
			query += "null, ";
			query += "null, ";
			if (msg == null)
				query += "null, ";
			else
				query += "'" + findNReplaceAll(msg, "'", "''") + "', ";
			query += "null, ";
			query += "null, ";
			query += "'" + m_smsAppId + "', ";
			query += "null, ";
			query += "'" + System.currentTimeMillis() + "'";
			query += ")";
			logger.info("query: " + query);
			stmt = con.createStatement();
			int noOfRowsAffected = stmt.executeUpdate(query);
			logger.info("noOfRowsAffected: " + noOfRowsAffected);
		}
		catch (Exception e)
		{
			logger.error("", e);
			throw new OnMobileException(e.getMessage());
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
				if (con != null)
					RBTDBManager.init(db_url, 10).releaseConnection(con);

			}
			catch (Exception e)
			{
			}

		}
		return true;
	}

	public static String findNReplace(String input, String findWhatString,
			String replaceWithString)
	{
		logger.info("input: " + input
				+ " findWhatString: " + findWhatString 
				+ " replaceWithString: " + replaceWithString);
		if(input == null) 
			return null; 
		int index = input.indexOf(findWhatString);
		if (index == -1)
			return input;
		String pre = input.substring(0, index);
		StringBuffer ret = new StringBuffer();
		ret.append(pre);
		ret.append(replaceWithString);
		if ((index + findWhatString.length()) < (input.length()))
			ret.append(input.substring(index + findWhatString.length()));
//		logger.info("Exit with return ret.toString(): " + ret.toString());
		return ret.toString();
	}

	public static String findNReplaceAll(String input, String findWhatString,
			String replaceWithString)
	{
		logger.info("input: " + input
				+ " findWhatString: " + findWhatString 
				+ " replaceWithString: " + replaceWithString);
		if(input == null) 
			return null; 
		StringBuffer ret = new StringBuffer();
		boolean keepGoing = true;
		while (keepGoing)
		{
			int index = input.indexOf(findWhatString);
			if (index == -1)
			{
				ret.append(input);
				keepGoing = false;
			}
			else
			{
				ret.append(input.substring(0, index));
				ret.append(replaceWithString);
				input = input.substring(index + findWhatString.length());
			}
		}
//		logger.info("Exit with return ret.toString()="
//				+ ret.toString());
		return ret.toString();
	}

    private static StackTraceElement getCallerInfo(String msg,int index)
    {
        StackTraceElement stackElement = new Exception().getStackTrace()[index];
        return stackElement;
    }

	//LOGGING METHODS
    @Deprecated
	public static void logWarning(String _class, String method, String msg)
	{
	    StackTraceElement element = getCallerInfo(msg,2);
	    _class = element.getClassName();
	    method = element.getMethodName()+":"+element.getLineNumber();
		DebugManager.warning(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logStatus(String _class, String method, String msg)
	{
        StackTraceElement element = getCallerInfo(msg,2);
        _class = element.getClassName();
        method = element.getMethodName()+":"+element.getLineNumber();
	    DebugManager.status(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logDetail(String _class, String method, String msg)
	{
       StackTraceElement element = getCallerInfo(msg,2);
        _class = element.getClassName();
        method = element.getMethodName()+":"+element.getLineNumber();
		DebugManager.detail(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}
	
	@Deprecated
	public static void logTrace(String _class, String method, String msg)
	{
       StackTraceElement element = getCallerInfo(msg,2);
        _class = element.getClassName();
        method = element.getMethodName()+":"+element.getLineNumber();
		DebugManager.trace(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logException(String _class, String method, Throwable t)
	{
        StackTraceElement element = getCallerInfo(t.getLocalizedMessage(),2);
        _class = element.getClassName();
        method = element.getMethodName()+":"+element.getLineNumber();
	    DebugManager.exception(m_module, _class, method, t, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logNonFatalError(String _class, String method, String msg)
	{
       StackTraceElement element = getCallerInfo(msg,2);
        _class = element.getClassName();
        method = element.getMethodName()+":"+element.getLineNumber();
		DebugManager.nonfatalError(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logFatalError(String _class, String method, String msg)
	{
       StackTraceElement element = getCallerInfo(msg,2);
        _class = element.getClassName();
        method = element.getMethodName()+":"+element.getLineNumber();
        msg = element.getLineNumber()+": "+msg;

		DebugManager.fatalError(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logWarning(String msg)
	{
	    StackTraceElement element = getCallerInfo(msg,2);
	    String _class = element.getClassName();
	    String method = element.getMethodName()+":"+element.getLineNumber();
		DebugManager.warning(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logStatus(String msg)
	{
        StackTraceElement element = getCallerInfo(msg,2);
        String _class = element.getClassName();
	    String method = element.getMethodName()+":"+element.getLineNumber();
		DebugManager.status(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logDetail(String msg)
	{
       StackTraceElement element = getCallerInfo(msg,2);
       String _class = element.getClassName();
	   String method = element.getMethodName()+":"+element.getLineNumber();
	   DebugManager.detail(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}
	
	@Deprecated
	public static void logTrace(String msg)
	{
       StackTraceElement element = getCallerInfo(msg,2);
       String _class = element.getClassName();
	   String method = element.getMethodName()+":"+element.getLineNumber();
	   DebugManager.trace(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logException(Throwable t)
	{
        StackTraceElement element = getCallerInfo(t.getLocalizedMessage(),2);
        String _class = element.getClassName();
 	    String method = element.getMethodName()+":"+element.getLineNumber();
 	    DebugManager.exception(m_module, _class, method, t, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logNonFatalError(String msg)
	{
       StackTraceElement element = getCallerInfo(msg,2);
       String _class = element.getClassName();
	   String method = element.getMethodName()+":"+element.getLineNumber();
	   DebugManager.nonfatalError(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logFatalError(String msg)
	{
       StackTraceElement element = getCallerInfo(msg,2);
       String _class = element.getClassName();
	   String method = element.getMethodName()+":"+element.getLineNumber();
	   msg = element.getLineNumber()+": "+msg;

		DebugManager.fatalError(m_module, _class, method, msg, Thread
				.currentThread().getName(), null);
	}

	@Deprecated
	public static void logFatalException(String _class, String method,
			Throwable t)
	{
		DebugManager.fatalException(m_module, _class, method, t, Thread
				.currentThread().getName(), null);
	}

	public static boolean callURL(String strURL, Integer statusCode, StringBuffer response)
	{
		GetMethod get = new GetMethod();
		int timeOutInMilliSecond = 10000;
		try
		{
			strURL = strURL.trim();
			URL oSrc = new URL(strURL);
			String strHostIp = oSrc.getHost();
			int iHostPort = oSrc.getPort();
			logger.info("URL: " + strURL
					+ " Host IP: " + strHostIp
					+ " Host Port: " + iHostPort
					+ " Time out in milli second: " + timeOutInMilliSecond);

			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(strHostIp, iHostPort);

			java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
			String IPAddress = ip.getHostAddress();

			if (!strHostIp.equalsIgnoreCase("localhost")
					&& !strHostIp.equalsIgnoreCase(IPAddress))
			{
				boolean useProxy = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_PROXY", "FALSE");
				ArrayList<String> proxyHostNPort = tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "PROXY_SERVER_PORT", null), null);
				if (useProxy && proxyHostNPort != null
						&& proxyHostNPort.size() >= 2)
				{
					String proxyHost = (String) proxyHostNPort.get(0);
					int proxyPort = Integer.parseInt((String) proxyHostNPort.get(1));
					if (proxyHost != null && proxyPort != -1)
						ohcfg.setProxy(proxyHost, proxyPort);
				}
			}
			if (httpClientOfSMSHelper == null) {
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				connectionManager.getParams().setStaleCheckingEnabled(true);
				connectionManager.getParams().setMaxConnectionsPerHost(ohcfg, 10);
				connectionManager.getParams().setMaxTotalConnections(20);
				connectionManager.getParams().setSoTimeout(timeOutInMilliSecond);
				connectionManager.getParams().setConnectionTimeout(timeOutInMilliSecond);
				httpClientOfSMSHelper = new HttpClient(connectionManager);
				DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false); 
				httpClientOfSMSHelper.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler); 
				httpClientOfSMSHelper.getParams().setSoTimeout(timeOutInMilliSecond); 
			} 

			get = new GetMethod(strURL);
			long startTime = System.currentTimeMillis();
			statusCode = new Integer(httpClientOfSMSHelper.executeMethod(ohcfg, get));
			response.append(get.getResponseBodyAsString());
			long endTime = System.currentTimeMillis();
			logger.info("URL: " + strURL 
					+ " Response time: " + (endTime - startTime)
					+ " Response Code: " + statusCode 
					+ " Response: " + response.toString().trim() + "");

			if (statusCode.intValue() == 200)
				return true;
			else
				return false;
		}
		catch (Throwable e)
		{
			logger.error("", e);
			return false;
		}
		finally
		{
			if (get != null)
				get.releaseConnection();
		}
	}

	public static boolean callURL(String strURL, Integer statusCode,
			StringBuffer response, boolean useProxy, String proxyServerPort)
	{
		return callURL(strURL, statusCode, response, useProxy, proxyServerPort, false);
	}
	
	public static boolean callURL(String strURL, Integer statusCode,
			StringBuffer response, boolean useProxy, String proxyServerPort, boolean toRetry)
	{
		GetMethod get = new GetMethod();
		int timeOutInMilliSecond = 10000;
		try
		{
			strURL = strURL.trim();
			URL oSrc = new URL(strURL);
			String strHostIp = oSrc.getHost();
			int iHostPort = oSrc.getPort();
			logger.info("URL: " + strURL 
					+ " Host IP: " + strHostIp
					+ " Host Port: " + iHostPort
					+ " Time out in milli second: " + timeOutInMilliSecond);

			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(strHostIp, iHostPort);

			java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
			String IPAddress = ip.getHostAddress();

			if (!strHostIp.equalsIgnoreCase("localhost")
					&& !strHostIp.equalsIgnoreCase(IPAddress))
			{
				ArrayList<String> proxyHostNPort = tokenizeArrayList(proxyServerPort, null);
				if (useProxy && proxyHostNPort != null
						&& proxyHostNPort.size() >= 2)
				{
					String proxyHost = (String) proxyHostNPort.get(0);
					int proxyPort = Integer.parseInt((String) proxyHostNPort.get(1));
					if (proxyHost != null && proxyPort != -1)
						ohcfg.setProxy(proxyHost, proxyPort);
				}
			}
			if (m_httpClient3 == null) {
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				connectionManager.getParams().setStaleCheckingEnabled(true);
				connectionManager.getParams().setMaxConnectionsPerHost(ohcfg, 10);
				connectionManager.getParams().setMaxTotalConnections(20);
				connectionManager.getParams().setSoTimeout(timeOutInMilliSecond);
				connectionManager.getParams().setConnectionTimeout(timeOutInMilliSecond);
				m_httpClient3 = new HttpClient(connectionManager);
				DefaultHttpMethodRetryHandler retryhandler =null;
				if(toRetry){
					retryhandler = new DefaultHttpMethodRetryHandler(3, false);
				}else{
					retryhandler = new DefaultHttpMethodRetryHandler(0, false);
				}
				m_httpClient3.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler); 
				m_httpClient3.getParams().setSoTimeout(timeOutInMilliSecond); 
			} 
			get = new GetMethod(strURL);
			long startTime = System.currentTimeMillis();
			statusCode = new Integer(m_httpClient3.executeMethod(ohcfg, get));
			response.append(get.getResponseBodyAsString());
			long endTime = System.currentTimeMillis();
			logger.info("URL: " + strURL 
					+ " Response time: " + (endTime - startTime)
					+ " Response Code: " + statusCode 
					+ " Response: " + response.toString().trim() + "");
			if (statusCode.intValue() == 200)
				return true;
			else
				return false;
		}
		catch (Throwable e)
		{
			logger.error("", e);
			return false;
		}
		finally
		{
			if (get != null)
				get.releaseConnection();
		}
	}


	
	public static ArrayList<String> tokenizeArrayList(String stringToTokenize, String delimiter)
	{
		if (stringToTokenize == null)
			return null;
		String delimiterUsed = ",";

		if (delimiter != null)
			delimiterUsed = delimiter;

		StringTokenizer tokens = new StringTokenizer(stringToTokenize, delimiterUsed);
		ArrayList<String> result = new ArrayList<String>(tokens.countTokens());
		while (tokens.hasMoreTokens())
			result.add(tokens.nextToken().toLowerCase());

		return result;
	}

	public static String getStackTrace(Throwable ex)
	{
		StringWriter traceBuffer = new StringWriter();
		String strTraceContent = "";
		if (ex instanceof Exception)
		{
			Exception oException = (Exception) ex;
			oException.printStackTrace(new PrintWriter(traceBuffer));
			strTraceContent = traceBuffer.toString();
			strTraceContent = strTraceContent.substring(0, strTraceContent
					.length() - 2);
			strTraceContent = System.getProperty("line.separator") + " \t"
			+ strTraceContent;
		}
		return strTraceContent;
	}

	/*FOR TATA*/
//	SEND SMS via Voice Portal
	public static boolean sendSMS(String db_url, String sender, String msisdn, String msg, boolean needtoDuplicate) throws OnMobileException
	{
		logger.info("Params - MSISDN: " + msisdn + " msg: " + msg);
		if(msg == null || msisdn == null || msisdn.equals("") || (msg!=null && msg.trim().equalsIgnoreCase("NA"))) 
		{
			return false;
		}

		//added by Sreekar, this url will be read from the RBT.xml sms db url, whatever passes will be overwritten
		db_url = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_DB_URL", null);

		Connection con = null;
		Statement stmt = null;
		m_smsAppId = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_SMS_APP_ID", m_module);
		int noOfRowsAffected = -1;
		RBTDBManager rbm = null;
		try
		{
			
			String tempMsg = msg;
			boolean response = useSMSHelper(db_url, sender, msisdn,
					tempMsg, null, null);
			if (response) {
				return response;
			} 
			if(!useDBForSMS)
			{
				logger.info("Not adding in mmp table");
				return false;
			}
			rbm = RBTDBManager.init(db_url, 10);
			con = rbm.getSMSConnection();
	
			List<String> subString = new ArrayList<String>();
			String lastMessage = msg;
			if(lastMessage.length() > 160)
			{
				while(lastMessage.length() > 160)
				{
					String finalMessage = lastMessage.substring(0, 160); 

					int lastIndexofStop = finalMessage.lastIndexOf(".");

					if(lastIndexofStop < 6)
						lastIndexofStop = 159;

					lastMessage = lastMessage.substring(lastIndexofStop, lastMessage.length());
					finalMessage = finalMessage.substring(0, lastIndexofStop);

					lastMessage = lastMessage.trim();
					finalMessage = finalMessage.trim();

					subString.add(finalMessage);
				}
				subString.add(lastMessage);
			}
			else
			{
				subString.add(lastMessage);
			}

			logger.info("Number of sub strings = " + subString.size());

			for(int i = 0; i < subString.size(); i++)
			{
				String query = "";
				query += "INSERT INTO MMP_SMS_TABLE (" +
				"SMS_ID, " +
				"SMS_REFID, " +
				"RECIPIENT, " +
				"SENDER, " +
				"SMS_TYPE, " +
				"CREATETIME, " +
				"LASTUPDATETIME, " + 
				"LASTSUBMITTIME, " +
				"STATUS, " +
				"RETRY_COUNT, " +
				"UD_HEADER, " +
				"UD_DATA, " +
				"SMS_TEXT, " +
				"REPLACE_SMS_TEXT, " +
				"IS_REPLACED, " +
				"APP_ID, " +
				"DSR_STATUS_CODE, " +
				"TRANSACTION_ID) " + 
				"VALUES (";
				query += "SMS_SEQ.nextval, ";
				query += "null, ";
				query += "'" + msisdn + "', ";
				query += "'" + sender + "', ";
				query += "'S', ";
				query += "SYSDATE, ";
				query += "null, ";
				query += "null, ";
				query += "'C', ";
				query += "null, ";
				query += "null, ";
				query += "null, ";
				if (subString.get(i) == null)
					query += "null, ";
				else
					query += "'" + findNReplaceAll((String)subString.get(i), "'", "''") + "', ";
				query += "null, ";
				query += "null, ";
				query += "'" + m_module + "', ";
				query += "null, ";
				query += "'" + System.currentTimeMillis() + "'";
				query += ")";
				logger.info("query = "+query);
				stmt = con.createStatement();
				noOfRowsAffected = stmt.executeUpdate( query);
				logger.info("noOfRowsAffected = "+noOfRowsAffected);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			throw new OnMobileException(e.getMessage());
		}
		finally
		{
			try
			{
				if(stmt != null)
					stmt.close();
				if(con != null)
					rbm.releaseConnection(con);					
			}
			catch(Exception e)
			{
			}

			if(needtoDuplicate)
			{
				String dirPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null);
				int rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 8000);;

				String duplicateSMSDir = dirPath + "DUPLICATE_SMS";
				String SMSDetailsDir = dirPath + File.separator +"SMSDetails";

				Subscriber subscriber = RBTDBManager.init(db_url, 10).getSubscriber(msisdn);
				String subscriberType = "POST_PAID";

				if(subscriber == null)
					subscriberType = "unknown";
				else if(subscriber.prepaidYes())
					subscriberType = "PRE_PAID";

				WriteSDR.addToAccounting(duplicateSMSDir, rotationSize, "INSERT_MMP_SMS", msisdn, subscriberType, "INSERT_MMP_SMS", (noOfRowsAffected > 0)+"", (new Date()).toString(), "NA", "NA", msg, "NA");

				StringTokenizer replyTokens = new StringTokenizer(msg, ",");
				int noOfreplyTokens = replyTokens.countTokens();

				WriteSDR.addToAccounting(SMSDetailsDir, rotationSize, "DAEMON_REPLY", msisdn, "NA", "NA", msg, "NA", System.currentTimeMillis()+"", "NA", "1", noOfreplyTokens+"");

				if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "WRITE_SMSSDR_INTO_DB", "FALSE"))
					WriteSDR.writeSDRIntoDB("DAEMON_REPLY", msisdn, "NA", "NA", msg, "NA", System.currentTimeMillis()+"", "NA", "1", noOfreplyTokens+"");
			}
		}
		logger.info("Exit");
		return true;
	}

	/*Added by
		Venkatesh Sreekar
		vsreekar@onmobile.com 
	to write to file to send bulk activation promotion message*/
	public static synchronized void writeTFile(File aFile, String aContents) throws Exception
	{
		Writer output = null;
		try
		{
			if(aFile != null && !aFile.exists())
				aFile.createNewFile();
			aContents = aContents + System.getProperty("line.separator");
			if(aFile != null && aFile.canWrite())
			{
				output = new BufferedWriter(new FileWriter(aFile.toString(), true));
				output.write(aContents);
			}
		}
		finally
		{
			if(output != null)
			{
				output.close();
			}
		}
	}

	/*Added by
	Venkatesh Sreekar
	vsreekar@onmobile.com*/
	//This method will read entire contents from a file
	public static String ReadDFile(File aFile) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(aFile));
			String line = null;
			while((line = br.readLine()) != null)
			{
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if(br!=null)
					br.close();
			}
			catch(IOException e)
			{
				logger.error("", e);
			}
		}
		return sb.toString();
	}

	public static void writeSubscriberToSDRFile(String subscriberID, String subscriberType, String response, Date requestedDate, Date responseDate)
	{
		String reportPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null) + "\\RBT_DUPLICATE_SUB_TABLE";
		int rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String requestedTimeStamp = (requestedDate != null) ? formatter.format(requestedDate) : null;
		String responseTimeStamp = (responseDate != null) ? formatter.format(responseDate) : null;

		WriteSDR.addToAccounting(reportPath, rotationSize, "SUBSCRIBER_ACTIVATION", subscriberID, subscriberType, "SUBSCRIBER_ACTIVATION", response, requestedTimeStamp, responseTimeStamp, "NA", "NA", "NA");
	}

	public static void writeSelectionToSDRFile(String subscriberID, String subscriberType, String response, Date requestedDate, Date responseDate, String callerID, String promoID)
	{
		String reportPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null) + "\\RBT_DUPLICATE_SUB_SEL_TABLE";
		int rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String requestedTimeStamp = (requestedDate != null) ? formatter.format(requestedDate) : null;
		String responseTimeStamp = (responseDate != null) ? formatter.format(responseDate) : null;

		String requestDetails = callerID +":"+ promoID;

		WriteSDR.addToAccounting(reportPath, rotationSize, "SUBSCRIBER_SELECTION", subscriberID, subscriberType, "SUBSCRIBER_SELECTION", response, requestedTimeStamp, responseTimeStamp, "NA", requestDetails, "NA");
	}

	public static void writeToBeDeletedSettingToSDRFile(String subscriberID, String subscriberType, String response, Date requestedDate, Date responseDate, String callerID, String promoID)
	{
		String reportPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null) + "\\RBT_DELETED_SETTINGS";
		int rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String requestedTimeStamp = (requestedDate != null) ? formatter.format(requestedDate) : null;
		String responseTimeStamp = (responseDate != null) ? formatter.format(responseDate) : null;

		String requestDetails = callerID +":"+ promoID;

		WriteSDR.addToAccounting(reportPath, rotationSize, "RBT_DELETED_SETTINGS", subscriberID, subscriberType, "RBT_DELETED_SETTINGS", response, requestedTimeStamp, responseTimeStamp, "NA", requestDetails, "NA");
	}

	public static boolean moveFile(File origin, File dest) 
	{ 
		try 
		{ 
			boolean fileMoved = origin.renameTo(dest); 
			logger.info("Moving file from '"+ origin.getAbsolutePath() +"' to '"+ dest.getAbsolutePath() +"'"
					+ (fileMoved ? " is success." : " is failed"));
			return fileMoved; 
		} 
		catch (Exception e) 
		{ 
			logger.error("", e); 
		} 

		return false; 
	} 
	/**
	 * 
	 * @param prefixURL - http://ip:port/|connTimeout|dataTimeout|proxyHost|proxyport
	 * @return
	 */
	public static HttpParameters getHttpParamsForURL(String prefixURL, String page) {
		if(prefixURL == null)
			return null;
		try {
			HttpParameters httpParams = new HttpParameters();
			StringTokenizer stk = new StringTokenizer(prefixURL, "|");
			StringBuilder url = new StringBuilder(stk.nextToken());
			if(page != null)
				url.append(page);
			httpParams.setUrl(url.toString());
			if(stk.hasMoreTokens()) {
				int connTimeout = 3000;
				try {
					connTimeout = Integer.parseInt(stk.nextToken());
				} catch(NumberFormatException e) {
					connTimeout = 3000;
				}
				httpParams.setConnectionTimeout(connTimeout);
			}// end of if connection timeout exists
			if(stk.hasMoreTokens()) {
				int dataTimeout = 10000;
				try {
					dataTimeout = Integer.parseInt(stk.nextToken());
				}
				catch(NumberFormatException e) {
					dataTimeout = 3000;
				}
				httpParams.setDataTimeout(dataTimeout);
			}// end of if data timeout exists
			if(stk.hasMoreTokens()) {
				httpParams.setProxyHost(stk.nextToken());
			}// end of if proxyhost exists
			if(stk.hasMoreTokens()) {
				int proxyPort = 8000;
				try {
					proxyPort = Integer.parseInt(stk.nextToken());
				}
				catch(NumberFormatException e) {
					proxyPort = 8000;
				}
				httpParams.setProxyPort(proxyPort);
				httpParams.setHasProxy(true);
			}// end of if proxy port exists
			return httpParams;
		}
		catch(Exception e) {
			logger.info("", e);
		}
		return null;
	}

	static public String getParameterAsString(String paramName, String defaultValue, HashMap paramMap) 
	{ 
		String returnValue = null; 
		if(paramMap == null || paramName == null) 
			returnValue = null; 
		else if(!paramMap.containsKey(paramName)) 
			returnValue =  defaultValue; 
		else 
			returnValue = ((String)paramMap.get(paramName)).trim(); 
		logger.info("Param = "+paramName + " , Value = "+returnValue); 
		return returnValue; 
	} 
	static public int getParameterAsInt(String paramName, int defaultValue, HashMap paramMap) 
	{ 
		int returnValue = defaultValue; 
		if(paramMap == null || paramName == null) 
			returnValue = defaultValue; 
		else if(!paramMap.containsKey(paramName)) 
			returnValue = defaultValue; 
		else 
		{ 
			try 
			{ 
				returnValue =  Integer.parseInt(((String)paramMap.get(paramName)).trim()); 
			} 
			catch(Exception e) 
			{ 
				logger.error("", e); 
				returnValue =  defaultValue; 
			} 
		} 
		logger.info("Param = "+paramName + " , Value = "+returnValue); 
		return returnValue; 
	} 
	static public boolean getParameterAsBoolean(String paramName, boolean defaultValue, HashMap paramMap) 
	{ 
		boolean returnValue = defaultValue; 
		if(paramMap == null || paramName == null) 
			returnValue =  defaultValue; 
		else if(!paramMap.containsKey(paramName)) 
			returnValue =  defaultValue; 
		else 
			returnValue = (((String)paramMap.get(paramName)).trim().equalsIgnoreCase("TRUE") || ((String)paramMap.get(paramName)).trim().equalsIgnoreCase("ON")); 
		logger.info("Param = "+paramName + " , Value = "+returnValue); 
		return returnValue; 
	} 

	private static String getParamAsString(String param) {
		try {
			return rbtConnector.getRbtGenericCache().getParameter("GATHERER",
					param, null);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param);
			return null;
		}
	}
	
	private static boolean useSMSHelper(String db_url, String sender, String msisdn, String msg, String circleId, String operatorName ) { 
		if(m_smsHelperUrl == null || m_smsHelperUrl.length() <= 0)
		{
			//initialize the m_smsHelperUrl 
			m_smsHelperUrl = RBTParametersUtils.getParamAsString("COMMON", "SMS_HELPER_URL", null);
		}
		
		String helperUrl = m_smsHelperUrl;
		if(operatorName != null) {
			helperUrl = RBTParametersUtils.getParamAsString("COMMON", "SMS_HELPER_URL_"+operatorName.toUpperCase(), helperUrl);
		}
		
		if(helperUrl != null && helperUrl.length() > 0) 
		{
			boolean convertToHex = RBTParametersUtils.getParamAsBoolean("SMS", "CONVERT_TO_HEX", "FALSE");
			if (convertToHex)
				msg = StringUtil.convertUnicodeToHex(msg);

			msg = getEncodedUrlString(msg);
			String url = helperUrl;
			url = findNReplace(url, "$sender$", sender);
			url = findNReplace(url, "$receiver$", msisdn);
			url = findNReplace(url, "$smstext$", msg);
			url = findNReplaceAll(url, " ", "%20");
			return callURL(url, new Integer(-1), new StringBuffer());
		}
		return false;
	}

	private static boolean useSMSHelper(String db_url, String sender, String msisdn, String msg, String prefix) { 
		if(m_smsHelperUrl == null || m_smsHelperUrl.length() <= 0)
		{
			//initialize the m_smsHelperUrl 
			m_smsHelperUrl = RBTParametersUtils.getParamAsString("COMMON", "SMS_HELPER_URL", null);
		}
		
		if(m_smsHelperUrl != null && m_smsHelperUrl.length() > 0) 
		{
			boolean convertToHex = RBTParametersUtils.getParamAsBoolean("SMS", "CONVERT_TO_HEX", "FALSE");
			if (convertToHex)
				msg = StringUtil.convertUnicodeToHex(msg);

			msg = getEncodedUrlString(msg);
			String url = m_smsHelperUrl;
			url = findNReplace(url, "$sender$", sender);
			url = findNReplace(url, "$receiver$", msisdn);
			url = findNReplace(url, "$smstext$", msg);
			url = findNReplaceAll(url, " ", "%20");
			if(prefix!=null)
				url = findNReplaceAll(url, "$prefix$", prefix);
			return callURL(url, new Integer(-1), new StringBuffer());
		}
		return false;
	}

	
	private static String getEncodedUrlString(String param)
	{
		String ret = null;
		try
		{
			ret = m_urlEncoder.encode(param, "UTF-8");
		}
		catch(Throwable t)
		{
			ret = null;
		}
		return ret;
	}
	
	public static String decodeHTML(String str) {
		str = str.replaceAll("&lt;", "<");
		str = str.replaceAll("&gt;", ">");
		str = str.replaceAll("&amp;", "&");
		str = str.replaceAll("&quot;", "\"");
		str = str.replaceAll("&apos;", "\'");
		return str;
	}

	public static String eHTML(String str) {
		str = str.replaceAll("&lt;", "<");
		str = str.replaceAll("&gt;", ">");
		str = str.replaceAll("&amp;", "&");
		str = str.replaceAll("&quot;", "\"");
		str = str.replaceAll("&apos;", "\'");
		return str;
	}
}

class TXTFileFilter implements FileFilter
{
	private static Logger logger = Logger.getLogger(TXTFileFilter.class);
	
	public boolean accept(File pathname)
	{
		String filename = pathname.getName();
		if(! pathname.isFile()) 
		{
			logger.warn("Skipping directory " + filename);
			return false;
		}
		boolean result = false;
		if (filename.startsWith("RBT") && filename.endsWith(".txt"))
		{
			logger.info(filename + " name ends with .txt");
			result = true;
		}
		else
		{
			logger.info(filename + " name doesnt end with .txt");
			result = false;
		}
		return result;
	}

}

class OnmFileFilter implements FileFilter
{
	private static Logger logger = Logger.getLogger(OnmFileFilter.class);
	long time = 0;
	String extension = null;

	public OnmFileFilter(int old_days, String ext)
	{
		SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy_HHmm");
		extension = ext;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, (-1 * 24 * old_days));
		time = calendar.getTime().getTime();
		Calendar now = Calendar.getInstance();
		now.setTime(new java.util.Date(time));
		logger.info("Filter for files older than " + m_format.format(now.getTime()));
	}

	public boolean accept(File pathname)
	{
		long lastmodified = pathname.lastModified();
		if (lastmodified > time)
			return false;
		String filename = pathname.getName();
		if (pathname.isFile())
		{
			if (extension != null && extension.trim().length() > 0)
				if (!filename.endsWith(extension))
					return false;
		}
		return true;
	}
}
