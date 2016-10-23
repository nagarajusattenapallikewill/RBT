package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.smsgateway.accounting.Accounting;

public class RTCommonCopyHelper extends Thread implements iRBTConstant {
	private static Logger logger = Logger.getLogger(RTCommonCopyHelper.class);
	
	private static String _class = "RTCommonCopyHelper";

	//String CASE1 = "TRUE;VODAFONE,9876,9887,9123;IDEA,9653,9432,9656;TRUE;9812312312,9834123543;DEFAULT_30;TRUE,FALSE";

	private RBTDBManager rbtDBManager = null;

	private boolean m_writeTrans = false;

	private String m_transDir = "./Trans";

	private String m_trans_file = null;
	 private String m_site = null;
      private String m_cust = null;
      private String m_datename = null;

	private BufferedWriter m_transbufferWriter = null;

	private int m_rt_runHourForTrans = 0;

	private long m_nextWakeUpTimeForTrans = 0;

	private SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy");

	private Hashtable m_rdcParameters = new Hashtable();

	private String m_gathererPath = "./";

	private Hashtable m_operatorCombo = new Hashtable();

	private Hashtable m_operatorProp = new Hashtable();

	String m_ContentUrl = null;

	boolean useProxyContent = false;

	String proxyHostContent = null;

	int proxyPortContent = -1;

	Hashtable m_sitePrefixTable = new Hashtable();

	private int m_maxCopyProcessingCount = 5000;

	private int m_sleepInterval = 5;

	String RTCOPY = "RTCOPY";

	String RTCOPYFAILED = "RTCOPYFAILED";
	
	String RTCOPYCONTENTFOUND = "RTCOPYCONTENTFOUND";

	String RTCOPYCONTENTMISSING = "RTCOPYCONTENTMISSING";

	String RTCOPYCONTENTFAILED = "RTCOPYCONTENTFAILED";

	String RTCOPYCONTENTERROR = "RTCOPYCONTENTERROR";

	String RTCOPYTRANSFERRED = "RTCOPYTRANSFERRED";

	String RTCOPYTRANSFERFAILED = "RTCOPYTRANSFERFAILED";

	String RTCOPYDEF = "RTCOPYDEF";
	
	String RTCOPYMISSING = "RTCOPYMISSING";
	
	String RDC = "RDC";
	
	private int m_data_collection_days = 5;
    public static ArrayList folders_to_zip = new ArrayList();
	public static Accounting m_oprAccounting = null; 
    public static Accounting m_contentAccounting = null; 
    public static String m_sdrWorkingDir = "D:/rbt/sdr/rtCopy"; 
    private static HttpClient m_httpClient = null; 
    private String m_spider_dir;
    private int noOfThreads = 1;
    public ArrayList i_rtCopyRequestList = new ArrayList();
    public ArrayList i_threadPool = new ArrayList();
    public static RTCommonCopyHelper i_rtCCHelper = null;
    private static Object i_object = new Object();

	public boolean init() {
		Tools.init("RTCommonCopyHelper", false);
		logger.info("Entered.");
		rbtDBManager = RBTDBManager.getInstance();

		Parameters[] parameters = getParameters();
		logger.info("parameters is null : "+(parameters == null));
		if (parameters != null && parameters.length > 0) 
		{
			for (int i = 0; i < parameters.length; i++)
			{
				try
				{
					if (parameters[i].getType() != null
							&& parameters[i].getType().equalsIgnoreCase(RDC))
						m_rdcParameters.put(parameters[i].getParam(),
								parameters[i].getValue().trim());
				}
				catch (Exception e)
				{
					logger.info("RBT:: Parameter "
							+ parameters[i].getParam() + " with type "
							+ parameters[i].getType() + " has value as null");
				}
			}
		}
		logger.info("m_rdcParameters is : "+m_rdcParameters);

		m_writeTrans = getParameterAsBoolean("RT_WRITE_TRANS", false);
		m_maxCopyProcessingCount = getParameterAsInt("RT_COPY_PROCESSING_COUNT",
				5000);
		m_sleepInterval = getParameterAsInt("RT_GATHERER_SLEEP_INTERVAL", 5);
		m_nextWakeUpTimeForTrans = getnexttime(m_rt_runHourForTrans);
		m_gathererPath = getParameterAsString("RT_GATHERER_PATH", null);
		m_data_collection_days  = getParameterAsInt("RT_DATA_COLLECTION_DAYS", 5);
        m_spider_dir = getParameterAsString("SPIDER_DIR", null);
        noOfThreads = getParameterAsInt("RT_NO_OF_THREADS", 1);
		if (m_gathererPath != null && m_writeTrans) {
			m_transDir = m_gathererPath + "/Trans";
			new File(m_transDir).mkdirs();
		}
		 if(m_writeTrans);
              //folders_to_zip.add(m_gathererPath + "/RTCopyTrans");
		initSitePrefixTable();
		initOperatorProps();
		initOperatorMaps();
		initContentUrl();
		createAccounting(); 
        initHttp(); 
		return true;
	}

	public static RTCommonCopyHelper getInstance()
	{
		if(i_rtCCHelper != null)
			return i_rtCCHelper;
		synchronized(i_object)
		{
			if(i_rtCCHelper != null)
				return i_rtCCHelper;
			i_rtCCHelper = new RTCommonCopyHelper(); 	
		}
		return i_rtCCHelper;
	}
	
	private synchronized void openTrans() {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new java.util.Date(System.currentTimeMillis()));
			String date = m_format.format(calendar.getTime());
			String file_prefix = null;
			String fileName = null;
			file_prefix = "RT_COPY_TRANS";
			fileName = null;
			if (m_trans_file != null
					&& new File(m_trans_file + ".csv").isFile()) {
				fileName = m_trans_file;
				logger.info("*** RBT::writing RTCOPY Transaction file (append) : "
								+ m_trans_file);
				m_transbufferWriter = new BufferedWriter(new FileWriter(
						fileName + ".csv", true));
			} else {
				fileName = m_transDir + File.separator + file_prefix + "_"
						+ date;
				m_trans_file = fileName;
				boolean newFile = false;
				if (!(new File(fileName + ".csv").exists())) {
					newFile = true;
				}
				logger.info("*** RBT::writing RTCOPY Transaction file (append) : "
								+ m_trans_file);
				m_transbufferWriter = new BufferedWriter(new FileWriter(
						fileName + ".csv", true));
				if (newFile) {
					m_transbufferWriter
							.write("CALLED,CALLER,SONG,CATEGORY,COPY_TIME,CALLER_TYPE,COPY_DONE,SMS_TYPE");
				}
				m_transbufferWriter.flush();
			}

			logger.info("*** RBT::writing Transaction of RTCOPY file with name = "
							+ fileName + ".csv");
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private synchronized void closeTrans() {

		if (m_transbufferWriter != null) {
			try {
				logger.info("*** RBT::closing RTCOPY Trans files file ");
				m_transbufferWriter.flush();
				//m_transbufferWriter.close();
				//m_transbufferWriter = null;
			} catch (Exception e) {
				logger.error("", e);
				e.printStackTrace();
				//m_transbufferWriter = null;
			}
		}

		try {
			logger.info("*** RBT::checking to create new Trans "
							+ new Date(System.currentTimeMillis() + 10000)
							+ " wakeUp " + new Date(m_nextWakeUpTimeForTrans));
			if ((System.currentTimeMillis() ) >= m_nextWakeUpTimeForTrans) {
				m_trans_file = null;
				m_nextWakeUpTimeForTrans = getnexttime(m_rt_runHourForTrans);
			}
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
		}
	}

	private synchronized void  writeTrans(String subid, String callerID, String song,
			String cat, String req_time,  String type,
			String success, String smsType) {
		logger.info("RBT::" + subid);
		try {
			m_transbufferWriter.newLine();
			m_transbufferWriter.write(subid);
			m_transbufferWriter.write("," + callerID);
			m_transbufferWriter.write("," + song);
			m_transbufferWriter.write("," + cat);
			m_transbufferWriter.write("," + req_time);
			m_transbufferWriter.write("," + type);
			m_transbufferWriter.write("," + success);
			m_transbufferWriter.write("," + smsType);
			m_transbufferWriter.flush();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void run() 
	{
		logger.info("Entering");
		makeThreads();
		while (true)
		{
			try
			{
				logger.info("Entering while loop.");
				if (m_writeTrans)
					openTrans();
				checkThreads();
				makeReportingFiles();
				resolveContentBulk();
				processBulk();

				processContentMissingBulk();
				if (m_writeTrans)
					closeTrans();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				logger.info("Exception caught: "+t);
			}
			try
			{
				Date next_run_time = roundToNearestInterVal(m_sleepInterval);
				long sleeptime = getSleepTime(next_run_time);
				if(sleeptime < 100)
	            	sleeptime = 500;
	            logger.info(_class + " Thread : sleeping for "+sleeptime + " mSecs.");
	            Thread.sleep(sleeptime);
	            logger.info(_class + " Thread : waking up.");
				Thread.sleep(sleeptime);
			}
			catch (Throwable E)
			{
				logger.error("", E);
			}
		}
	}


	public String[] parseText(String s) {
		////logger.info("****** parameters are -- "+s);
		int index = 160;
		ArrayList list = new ArrayList();
		String t = null;
		while (s.length() != 0) {
			index = 160;
			if (s.length() <= 160) {
				t = s;
				s = "";
			} else {
				while (index >= 0 && s.charAt(index) != ' ')
					index--;
				t = s.substring(0, index);
				s = s.substring(index + 1);
			}
			list.add(t);
		}

		if (list.size() > 0) {
			String[] smsTexts = (String[]) list.toArray(new String[0]);
			return smsTexts;
		} else {
			return null;
		}

	}

	public String getParameterAsString(String paramName, String defaultValue) {
		
		String retVal = null;
		if (m_rdcParameters == null || paramName == null)
			retVal = defaultValue;
		else if (!m_rdcParameters.containsKey(paramName))
			retVal = defaultValue;
		else
			retVal = ((String) m_rdcParameters.get(paramName)).trim();
		logger.info("paramName is: "+paramName + " and value is: "+retVal);
		return retVal;
	}

	public boolean getParameterAsBoolean(String paramName, boolean defaultValue) 
	{
		boolean retVal = defaultValue;
		if (m_rdcParameters == null || paramName == null)
			retVal = defaultValue;
		else if (!m_rdcParameters.containsKey(paramName))
			retVal = defaultValue;
		else 
			retVal = ((String) m_rdcParameters.get(paramName)).trim()
				.equalsIgnoreCase("TRUE");
		logger.info("paramName is: "+paramName + " and value is: "+retVal);
		return retVal;
	}

	public int getParameterAsInt(String paramName, int defaultValue) 
	{
		int retVal = defaultValue;
		if (m_rdcParameters == null || paramName == null)
			retVal = defaultValue;
		else if (!m_rdcParameters.containsKey(paramName))
			retVal = defaultValue;
		else
		{
			try 
			{
				retVal =  Integer.parseInt(((String) m_rdcParameters.get(paramName)).trim());
			}
			catch (Exception e) 
			{
				retVal = defaultValue;
			}
		}
		logger.info("paramName is: "+paramName + " and value is: "+retVal);
		return retVal;
	}

	public void initContentUrl() {
		String strContentDetails = getParameterAsString("CONTENT_URL", null);
		if (strContentDetails != null && strContentDetails.length() > 0) {
			StringTokenizer stk = new StringTokenizer(strContentDetails, ",");
			if (stk.hasMoreTokens())
				m_ContentUrl = stk.nextToken().trim();
			if (stk.hasMoreTokens())
				useProxyContent = stk.nextToken().trim().equalsIgnoreCase(
						"true");
			if (stk.hasMoreTokens())
				proxyHostContent = stk.nextToken().trim();
			try {
				if (stk.hasMoreTokens())
					proxyPortContent = Integer.parseInt(stk.nextToken().trim());
			} catch (Exception e) {
				proxyPortContent = -1;
			}
		}
	}

	public boolean isValidTypePrefix(String subscriberID, Hashtable m_prefixMap) {
		if (subscriberID == null || subscriberID.length() < 7
				|| subscriberID.length() > 15 || m_prefixMap == null)
			return false;
		else {
			try {
				Long.parseLong(subID(subscriberID));
			} catch (Throwable e) {
				return false;
			}
		}
		if(m_prefixMap.size()==0) 
            return true; 
		int prefixLength = -1;
		String thisPrefix = null;
		ArrayList prefixArrayListTemp = null;
		Integer prefixKey = null;
		Iterator prefixIteror = m_prefixMap.keySet().iterator();
		while (prefixIteror.hasNext()) {
			prefixKey = (Integer) prefixIteror.next();
			prefixLength = prefixKey.intValue();
			thisPrefix = subscriberID.substring(0, prefixLength);
			prefixArrayListTemp = (ArrayList) m_prefixMap.get(prefixKey);
			if (prefixArrayListTemp.contains(thisPrefix)) {
				//logger.info("RBT:prefix true");
				return true;
			}
		}
		//logger.info("RBT:prefix false");
		return false;
	}

	public long getnexttime(int hour) {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, hour);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);

		long nexttime = now.getTime().getTime();
		if (nexttime < System.currentTimeMillis()) {
			nexttime = nexttime + (24 * 3600 * 1000);
		}
		return nexttime;
	}

	public ViralSMSTable[] getViralSMSTableLimit(String type, int count) {
		return (rbtDBManager.getViralSMSByTypeAndLimit(type, count));
	}

	private void removeViralPromotion(String subscriberID, String callerID,
			Date sentTime, String type) {
		rbtDBManager.removeViralPromotion(subscriberID, callerID, sentTime,
				type);
	}

	private void updateViralPromotion(String subscriberID, String callerID,
			Date sentTime, String fType, String tType) {
		rbtDBManager.updateViralPromotion(subscriberID, callerID, sentTime,
				fType, tType, new Date(System.currentTimeMillis()), null,null);
	}

	private void setSearchCountCopy(String strSubID, int count, String type,
			Date sent, String callerID) {
		RBTDBManager.getInstance()
				.setSearchCountCopy(strSubID, type, count, sent, callerID);
	}


	private Parameters[] getParameters() {
		return CacheManagerUtil.getParametersCacheManager().getAllParameters().toArray(new Parameters[0]);
	}

	private void initOperatorProps() {
		logger.info("Entered");
		if (m_rdcParameters == null || m_rdcParameters.size() <= 0)
			return;
		Iterator itrParams = m_rdcParameters.keySet().iterator();
		while (itrParams.hasNext()) {
			String paramName = (String) itrParams.next();
			if (paramName != null && paramName.trim().length() > 0
					&& paramName.trim().startsWith("RT_PROP")) {
				OperatorProp oP = getOperatorProp((String) m_rdcParameters
						.get(paramName));
				if (oP != null)
					m_operatorProp.put(oP.name, oP);
			}
		}
		logger.info("Exiting");
	}

	private void initOperatorMaps() {
		logger.info("entered");
		if (m_rdcParameters==null | m_rdcParameters.size()<=0){
			logger.info("M-rdc  parameter issues " + m_rdcParameters.size());
			return ;
			}
		Iterator it=m_rdcParameters.keySet().iterator();
		String op1 = null;
		String op2 = null;
		Hashtable copierList = new Hashtable();
		Hashtable copieeList = new Hashtable();
		OperatorCombo oc=null;
		ArrayList op=new ArrayList();
		boolean isLive = false;
		String classType = null;
		boolean isTestOn = true;
		ArrayList testNumList = new ArrayList();
		String operators=null;
		String[] operatorPrefixes=null;
		
		while (it.hasNext()) {
			String parameterName=(String)it.next();
			logger.info(" iterator values " +parameterName ) ;
			if (parameterName.startsWith("RT_MAP")
					&& parameterName.contains("CALLER_PREFIX")) {
				if (m_rdcParameters.get(parameterName) != null) {
					operators = m_rdcParameters.get(parameterName).toString();
					logger.info(" operators " + operators ) ;
					String operator = parameterName.toString().split("_")[2];
					logger.info("operator : " + operator);
					if (operator != null) {
						String testNumbers = (String) m_rdcParameters
								.get("RT_MAP_" + operator + "_TESTING_NUMBERS");
						logger.info("testNumbers : " + testNumbers);
						String copyList = (String) m_rdcParameters
								.get("RT_MAP_" + operator + "_PREFIX");
						logger.info("copierList : " + copyList);
						if (testNumbers != null)
							testNumList = Tools.tokenizeArrayList(testNumbers,	
									null);
						if (copyList != null)
							copierList = getPrefixTable(copyList);
						if (m_rdcParameters.containsKey("RT_MAP_" + operator
								+ "_TESTING_ON") && m_rdcParameters.get("RT_MAP_" + operator
										+ "_TESTING_ON")!=null)
							isTestOn = m_rdcParameters.get("RT_MAP_" + operator
									+ "_TESTING_ON").toString().equalsIgnoreCase("TRUE");
						logger.info("isTestOn : " + isTestOn);
						if (operators != null) {
							operatorPrefixes = operators.split(":");
							if (operatorPrefixes != null) {
								for (int i = 0; i < operatorPrefixes.length; i++) {

									String toOperator = operatorPrefixes[i]
											.split(",")[0];
									logger.info("toOperator : " + toOperator);
									copieeList = getPrefixTable(operatorPrefixes[i]
											.substring(operatorPrefixes[i]
													.indexOf(",")));
									logger.info("copieeList : " + copieeList);
									String name = operator + "-" + toOperator;
									OperatorCombo oC = new OperatorCombo(name,
											isLive, copierList, copieeList,
											isTestOn, testNumList, classType);
									m_operatorCombo.put(name, oC);
								}
							}

						}

					}

				}
			}

		}
		logger.info("leaving with " + m_operatorCombo);
		
	}

	private OperatorProp getOperatorProp(String oCValue) 
	{
		logger.info("Entered with oPvalue"+oCValue);
		StringTokenizer stkParent = new StringTokenizer(oCValue, ";");
		String name = null;
		boolean isLive = false;
		String url = null;
		boolean useProxy = false;
		String proxyHost = null;
		int proxyPort = -1;
		Hashtable prefixList = new Hashtable();
		boolean transferMissingContent = false;
		boolean sendMissingContentSMS = false;
		String missingContentSMSText = "The content copied is not available currently. Plz try later.";
		String classType = "DEFAULT";
		ArrayList ipList = null;
		boolean copyDefault = false;
		String senderNo = "123456";
		
		
		if (stkParent.hasMoreTokens())
			name = stkParent.nextToken().trim();
		if (!m_sitePrefixTable.containsKey(name)){
			logger.info("m_sitePrefix table does not contain key ");
			return null;
			}
		
		if (stkParent.hasMoreTokens())
			url = stkParent.nextToken();
		if (stkParent.hasMoreTokens())
			useProxy = stkParent.nextToken().trim().equalsIgnoreCase("true");
		if (stkParent.hasMoreTokens())
			proxyHost = stkParent.nextToken().trim();
		try {
			if (stkParent.hasMoreTokens())
				proxyPort = Integer.parseInt(stkParent.nextToken().trim());
		} catch (Exception e) {
			proxyPort = -1;
		}
		if (stkParent.hasMoreTokens())
			isLive = stkParent.nextToken().trim().equalsIgnoreCase("true");
		if (stkParent.hasMoreTokens())
			transferMissingContent = stkParent.nextToken().trim()
					.equalsIgnoreCase("true");
		if (stkParent.hasMoreTokens())
			sendMissingContentSMS = stkParent.nextToken().trim()
					.equalsIgnoreCase("true");
		if (stkParent.hasMoreTokens())
			missingContentSMSText = stkParent.nextToken().trim();
		if (stkParent.hasMoreTokens())
			classType = stkParent.nextToken().trim();
		if (stkParent.hasMoreTokens())
			ipList = Tools
					.tokenizeArrayList(stkParent.nextToken().trim(), null);
		if (stkParent.hasMoreTokens())
			copyDefault = stkParent.nextToken().trim()
					.equalsIgnoreCase("true");
		if (stkParent.hasMoreTokens())
			senderNo = stkParent.nextToken().trim();
		
		prefixList = getPrefixTable(((SitePrefix) m_sitePrefixTable.get(name)).getSitePrefix());
		
		OperatorProp oP = new OperatorProp(name, isLive, url, useProxy,
				proxyHost, proxyPort, prefixList, transferMissingContent,
				sendMissingContentSMS, missingContentSMSText,classType,  ipList,copyDefault,senderNo);
		logger.info("OP is :"+oP);
		return oP;
	}

	private Hashtable getPrefixTable(String prefixList)
	{
		String prefixTempStr = null;
		int iPrefixLen = -1;
		ArrayList prefixAList = null;
		Hashtable returnTable = null;
		if (prefixList != null && prefixList.length() > 0) {
			StringTokenizer stk = new StringTokenizer(prefixList, ",");
			returnTable = new Hashtable();
			while (stk.hasMoreTokens()) {
				prefixTempStr = stk.nextToken().trim();
				iPrefixLen = prefixTempStr.length();
				prefixAList = null;
				if (iPrefixLen <= 0)
					continue;
				if (returnTable.containsKey(new Integer(iPrefixLen)))
					prefixAList = (ArrayList) returnTable.get(new Integer(
							iPrefixLen));
				else
					prefixAList = new ArrayList();
				prefixAList.add(prefixTempStr);
				returnTable.put(new Integer(iPrefixLen), prefixAList);
			}
		}
		//logger.info(" with param as "
			//	+ prefixList + " returning Table as " + returnTable);
		return returnTable;

	}

	public void initSitePrefixTable() 
	{
		logger.info("Entered");
		List<SitePrefix> prefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (prefixes == null || prefixes.size() <= 0)
			return;
		for (int i = 0; i < prefixes.size(); i++)
			m_sitePrefixTable.put(prefixes.get(i).getSiteName(), prefixes.get(i));
		logger.info("Exiting with m_sitePrefixTable as "+m_sitePrefixTable);
	}

	public String subID(String strSubID) {
		return (rbtDBManager.subID(strSubID));
	}

	public Date roundToNearestInterVal(int interval) {
		Calendar cal = Calendar.getInstance();
		int n = 60 / interval;
		for (int i = 1; i <= n; i++) {
			if (cal.get(Calendar.MINUTE) < (interval * (i))
					&& cal.get(Calendar.MINUTE) >= (interval * (i - 1))) {
				cal.set(Calendar.SECOND, 0);
				if (i < n)
					cal.set(Calendar.MINUTE, (interval * (i)));
				else {
					cal.set(Calendar.MINUTE, 0);
					cal.add(Calendar.HOUR_OF_DAY, 1);
				}
				break;
			}
		}
		return cal.getTime();
	}

	public long getSleepTime(Date date) {
		return (date.getTime() - System.currentTimeMillis());
	}

	private void processBulk()
	{
		synchronized(i_rtCCHelper.i_rtCopyRequestList)
		{
			if(i_rtCCHelper.i_rtCopyRequestList.size() > 0)
				return;
			ViralSMSTable[] context = getViralSMSTableLimit(RTCOPYCONTENTFOUND,
					m_maxCopyProcessingCount);
			if (context == null || context.length <= 0) {
				logger.info("Context is null or count <= 0 for " + RTCOPYCONTENTFOUND);
				
			}
			else
			{
				logger.info("Count of copyContext is "
						+ context.length + " for " + RTCOPYCONTENTFOUND);
				for (int i = 0; i < context.length; i++)
					i_rtCCHelper.i_rtCopyRequestList.add(context[i]);
				logger.info("request copy list " + i_rtCCHelper.i_rtCopyRequestList);
				i_rtCCHelper.i_rtCopyRequestList.notifyAll();
			}
			
		}
	}
	
	private void processContentMissingBulk()
	{
		synchronized(i_rtCCHelper.i_rtCopyRequestList)
		{
			if(i_rtCCHelper.i_rtCopyRequestList.size() > 0)
				return;
			ViralSMSTable[] context = getViralSMSTableLimit(RTCOPYCONTENTMISSING,
					m_maxCopyProcessingCount);
			if (context == null || context.length <= 0) {
				logger.info("Context is null or count <= 0 for " + RTCOPYCONTENTMISSING);
				
			}
			else
			{
				logger.info("Count of copyContext is "
						+ context.length + " for " + RTCOPYCONTENTMISSING);
				for (int i = 0; i < context.length; i++)
					i_rtCCHelper.i_rtCopyRequestList.add(context[i]);
				logger.info("request copy list " + i_rtCCHelper.i_rtCopyRequestList);
				i_rtCCHelper.i_rtCopyRequestList.notifyAll();
			}
			
		}
	}
	
	
	private void resolveContentBulk()
	{
		logger.info("Entering");
		ViralSMSTable[] context = getViralSMSTableLimit(RTCOPY,
				m_maxCopyProcessingCount);
		if (context == null || context.length <= 0) {
			logger.info("Context is null or count <= 0 for " + RTCOPY);
			return;
		}
		logger.info("Count of copyContext is "
				+ context.length + " for " + RTCOPY);

		for (int i = 0; i < context.length; i++)
		{
			try
			{
				logger.info("subscriber_id=" + context[i].subID()
						+ "|caller_id=" + context[i].callerID() + "|clipID=" + context[i].clipID()
						+ "|sentTime=" + context[i].sentTime() + "|selBy=" + context[i].selectedBy());
				String caller = subID(context[i].callerID());
				String called = subID(context[i].subID());
				String selBy = context[i].selectedBy();
				String clipID = context[i].clipID();
				String callerOpr = getSubscriberOperator(caller, null);
				String calledOpr = getSubscriberOperator(called, selBy);
				String comboName = callerOpr+"-"+calledOpr;
				logger.info("comboName is "+comboName);
				OperatorCombo oC = null;
				if(m_operatorCombo.containsKey(comboName))
					oC = (OperatorCombo)m_operatorCombo.get(comboName);
				if(oC == null )
				{
					logger.info(" operator combo is null");
					processFailure(context[i],false,RTCOPYFAILED);
					continue;
				}
				
				
				boolean isTestMode = oC.isTestOn;
				ArrayList testNumbers = oC.testNumList;
				boolean isTypePrefix = isValidTypePrefix(caller, oC.copierList);
				if(!( isTypePrefix || (isTestMode && testNumbers != null && testNumbers.contains(caller))))
				{
					processFailure(context[i],false,RTCOPYFAILED);
					continue;
				}
				if( clipID == null || clipID.toUpperCase().indexOf("DEFAULT")  != -1 || getWavFile(clipID).equalsIgnoreCase("-1") || clipID.toLowerCase().indexOf("missing") != -1){ // do we need to write trans here??
					updateViralPromotion(context[i].subID(), context[i].callerID(), context[i].sentTime(), RTCOPY, RTCOPYCONTENTMISSING, "MISSING:"+clipID);
					continue;                                                  
				}		
				processContentFindingRequestHttp(context[i]);
			}
			catch(Exception e)
			{
				logger.error("", e);
				//continue;
			}
		}
	}
	
	public boolean processTransfer(ViralSMSTable vst) {
		logger.info(" Started.");

		OperatorProp oP = (OperatorProp) m_operatorProp
				.get(getSubscriberOperator(vst.callerID(), null));

		boolean transferMissingContent = true;

		String classType = getClassType(vst);
		logger.info("subType " + oP.name + " and subID "
				+ vst.subID() + " and callerID " + vst.callerID()
				+ " and clipID " + vst.clipID() + " and sent time "
				+ vst.sentTime() + " and classType " + classType);
		String caller = subID(vst.subID());
		String called = vst.subID();
		Date sentTime = vst.sentTime();
		String clipId = vst.clipID();
		String smsType = RTCOPY;
		String copyTime = Tools.getFormattedDate(sentTime,
				"yyyy-MM-dd HH:mm:ss");
		String url = null;
		//Changes done for RL-27488
		String operatorUrl = RBTParametersUtils.getParamAsString("RDC",
				"CROSS_OPERATOR_URL_PARAM_MAP", null);
		if (operatorUrl != null && !operatorUrl.isEmpty())
			operatorUrl = getReplacedRdcURL(operatorUrl, vst,oP.name);
		//Changes ended for RL-27488
		try {
			if (oP != null) {
				url = oP.url + operatorUrl;
			}

			transferMissingContent = oP.transferMissingContent;
			boolean useProxy = oP.useProxy;
			String proxyHost = oP.proxyHost;
			int proxyPort = oP.proxyPort;

			StringBuffer statusInt = new StringBuffer();
			StringBuffer result = new StringBuffer();

			addToAccounting("OPR_REQUEST", url, "-", m_oprAccounting);
			boolean success = false;

			if (vst.type().equalsIgnoreCase(RTCOPYCONTENTFOUND)
					|| (transferMissingContent && vst.type().equalsIgnoreCase(
							RTCOPYCONTENTMISSING)))
				success = callURL(url, statusInt, result, useProxy, proxyHost,
						proxyPort);

			addToAccounting("OPR_RESPONSE", url, result.toString().trim(),
					m_oprAccounting);

			String response = result.toString().trim();
			if (success
					&& response != null
					&& (response.indexOf("SUCCESS") != -1
							|| response.equalsIgnoreCase("0") || response
							.indexOf("Successfully") != -1)) {
				logger.info("Copy successful for the following url " + url);
				logger.info("Transfer to operator successful");
				processSuccess(vst, RTCOPYTRANSFERRED);
				return true;
			} else if (success) {
				logger.info("Copy unsuccessful for the following url " + url);
				logger.info("Transfer success but copy failed");
				processFailure(vst, false, RTCOPYTRANSFERFAILED);
				return false;
			} else {
				logger.info("Copy unsuccessful for the following url " + url);
				logger.info("Transfer to operator failed");
				processFailure(vst, true, RTCOPYTRANSFERFAILED);
				return false;
			}
		} catch (Exception e) {
			logger.error("", e);
		}

		return false;
	}

	private void processContentFindingRequestHttp(ViralSMSTable vst) {
		String caller = subID(vst.callerID());
		String called = subID(vst.subID());
		String clipID = vst.clipID();
		String selBy = vst.selectedBy();
		String callerOpr = getSubscriberOperator(caller, null);
		String calledOpr = getSubscriberOperator(called, selBy);
		/*
		 * String wavFileSrc = getWavFile(clipID); if(wavFileSrc != null &&
		 * wavFileSrc.indexOf(".wav") == -1) wavFileSrc = wavFileSrc+".wav";
		 * logger.info("wavFileSrc = "+wavFileSrc+
		 * ", callerOpr = "+callerOpr+", calledOpr = "+calledOpr); String url =
		 * m_ContentUrl + "sourceWAVFileName="+ wavFileSrc + "&sourceOperator="
		 * + calledOpr+"&targetOperator=" + callerOpr;
		 */
		String clipIDSrc = getWavFile(clipID);
		logger.info("sourceCLIPID = " + clipIDSrc
				+ ", callerOpr = " + callerOpr + ", calledOpr = " + calledOpr);
		String url = m_ContentUrl + "sourceCLIPID=" + clipIDSrc
				+ "&sourceOperator=" + calledOpr + "&targetOperator="
				+ callerOpr + "&extraLogInfo=" + caller + "," + called;
		StringBuffer xtraInfo = new StringBuffer();
		addToAccounting("CONTENT_REQUEST", url, "-", m_contentAccounting);
		String response = callContentURL(url, xtraInfo);
		if (response != null)
			response = response.trim();
		addToAccounting("CONTENT_RESPONSE", url, response, m_contentAccounting);

		if (response == null || response.equalsIgnoreCase("RETRY")) {
        	logger.info("Content to be retried for the following url "+url);
        	processFailure(vst, true, RTCOPYCONTENTERROR);
        }
        else if (response.equalsIgnoreCase(RTCOPYCONTENTMISSING))
        {
            logger.info("Content missing for the following url "+url);
            updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type(),RTCOPYCONTENTMISSING, "MISSING:"+clipID);
            
        }
        else
        {
        	logger.info("Content found for the following url "+url);
        	updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type(),RTCOPYCONTENTFOUND, response);
        }
    }

	private String getSubscriberOperator(String subID, String ip) {
		if(m_operatorProp == null || m_operatorProp.size() <= 0)
			return null;
		Iterator oprPropItr = m_operatorProp.keySet().iterator();
		while(oprPropItr.hasNext())
		{
			String oprName = ((String)oprPropItr.next());
			OperatorProp oprProp = (OperatorProp)m_operatorProp.get(oprName);
			if(subID != null)
			{
				if(isValidTypePrefix(subID, oprProp.prefixList))
					return oprName;
			}
			else if (ip != null && oprProp.ipList != null)
			{
				if(oprProp.ipList.contains(ip))
					return oprName;
			}	
		}
		return "UNKNOWN";
	}
	private void updateViralPromotion(String subscriberID, String callerID,Date sentTime, String fType, String tType, String clipId)
    {
        rbtDBManager.updateViralPromotion(subscriberID, callerID, sentTime, fType,tType, clipId,null);
    }
	
	private void processFailure(ViralSMSTable vst, boolean checkCount, String updateType)
	{
		if (checkCount && vst.count() < 3)
			setSearchCountCopy(vst.subID(),	vst.count() + 1, vst.type(), vst.sentTime(), vst.callerID());
		else if (m_writeTrans) 
		{
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type());
			writeTrans(vst.subID(), vst.callerID(), getWavFile(vst.clipID()), "-", Tools.getFormattedDate(vst.sentTime(),"yyyy-MM-dd HH:mm:ss"),  getSubscriberOperator(subID(vst.callerID()), null), "-",updateType);
		}
		else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type(),	updateType);
	}
	
	private void processSuccess(ViralSMSTable vst, String updateType)
	{
		if(m_writeTrans)
		{
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type());
			writeTrans(vst.subID(), vst.callerID(), getWavFile(vst.clipID()), "-", Tools.getFormattedDate(vst.sentTime(),
					"yyyy-MM-dd HH:mm:ss"),getSubscriberOperator(subID(vst.callerID()), null), "-", updateType);
		}
		else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type(), updateType);
	
	}
	
	
	public String callContentURL(String strURL, StringBuffer xtraInfo )
    {
        String fName = "callContentURL";
        GetMethod get = null;
        try
        {
            strURL = strURL.trim();
            logger.info("URL = " + strURL);
            URL urlObj = new URL(strURL);
            HostConfiguration ohcfg = new HostConfiguration();
            ohcfg.setHost(urlObj.getHost(), urlObj.getPort());
            get = new GetMethod(strURL);
            long startTime = System.currentTimeMillis();
            logger.info("Start time in millisecond: " + startTime);
            
            int httpResponseCode = m_httpClient.executeMethod(ohcfg, get);
            String response = get.getResponseBodyAsString();
            	
            long endTime = System.currentTimeMillis();
            logger.info("End time in millisecond: " + endTime);
            logger.info("Diff in millisecond: "
                    + (endTime - startTime));
            logger.info("Response Code:" + httpResponseCode);
            logger.info("Response ->" + response);
            if (httpResponseCode  == 204){
            	xtraInfo.append("-1");
            	return RTCOPYCONTENTMISSING;
            }
            else if (httpResponseCode == 200) {
				Header headerWav = get.getResponseHeader("TARGET_CLIPID");
				String wavFile = null;
				if (headerWav != null && headerWav.getValue() != null)
					wavFile = headerWav.getValue().trim();
				logger.info("wavFile in header ->"
								+ wavFile);
				if (wavFile == null)
					throw new Exception(
							"Content Problem. No wavFile in header for status code 200.");
				if (wavFile != null && wavFile.endsWith(".wav"))
					wavFile = wavFile.substring(0, wavFile.indexOf(".wav"));
				return wavFile;
			} 
            else
            	return "RETRY";
        }
        catch (Throwable e)
        {
            logger.error("", e);
            return "RETRY";
        }
        finally
        {
            if (get != null)
                get.releaseConnection();
        }
    }
    
    
    private String getWavFile(String clipID)
    {
    	if(clipID == null)
    		return null;
    	clipID = (new StringTokenizer(clipID, ":")).nextToken().trim();
    	return clipID;
    }
    
    private String getClassType(ViralSMSTable vst)
    {
    	logger.info("Entering");
    	String classType = "DEFAULT";
    	String caller = subID(vst.callerID());
    	String called = subID(vst.subID());
    	String selBy = vst.selectedBy();
    	String callerOpr = getSubscriberOperator(caller, null);
    	String calledOpr = getSubscriberOperator(called, selBy);
    	OperatorCombo oC = (OperatorCombo)m_operatorCombo.get(callerOpr+"-"+calledOpr);
    	OperatorProp oP=  (OperatorProp)m_operatorProp.get(callerOpr);
    	if(oC != null && oC.classType != null)
    		classType =  oC.classType;
    	else if(oP != null && oP.classType != null)
    		classType = oP.classType;
    	logger.info("Exiting with classType = "+classType);
    	return classType;
    }
    
    public static void main(String[] args)
    {
    	try
        {
        	new ServerSocket (14000);
    	}
        catch(Exception e)
        {
			System.err.println("The port 16000 is in use or another rbt copier is already running");
			System.out.println("The port 16000 is in use or another rbt copier is already running");
			System.exit(-1);
		}
        
        RTCommonCopyHelper m_rtccHelper = RTCommonCopyHelper.getInstance();
        System.out.println("Started ...");
        try
        {
            if (m_rtccHelper.init())
            	m_rtccHelper.start();
        }
        catch (Throwable e)
        {
            logger.info("Exception occured :"+e);
            e.printStackTrace();
        }
        finally
        {
//            System.exit(0);
        }
    }

    public void addToAccounting(String type, 
            String request, String response, Accounting accObj ) 
    { 
//        String _method = "addToAccounting()"; 
        //logger.info("****** parameters are -- "+type + " 
        // & "+subscriberID + " & "+request+ " & "+response+" & "+ip ); 
        try 
        { 
            if (accObj != null) 
            { 
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
                acMap.put("SMSC_MESSAGE_ID", "NA"); 
                acMap.put("STATUS", (new SimpleDateFormat("yyyyMMddHHmmssms")) 
                    .format((new Date(System.currentTimeMillis())))); 
                if (accObj != null) 
                { 
                        accObj.generateSDR("sms", acMap); 
                } 
                acMap = null; 
            } 
        } 
        catch (Exception e) 
        { 
            logger.error("", e);
        } 
    } 
 
    private void createAccounting() { 
    	
//        String _method = "createAccounting()"; 
        //logger.info("****** blank" ); 
        m_oprAccounting = Accounting.getInstance(m_sdrWorkingDir + File.separator + "opr", 1000, 24, "size", true); 
        m_contentAccounting = Accounting.getInstance(m_sdrWorkingDir + File.separator + "content", 1000, 24, "size", true); 
        if (m_oprAccounting == null) 
            logger.info("RBT::Accounting class can not be created"); 
        if (m_contentAccounting == null) 
            logger.info("RBT::Accounting class can not be created"); 
 
    }
    
    private void initHttp() {
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setStaleCheckingEnabled(true);
		connectionManager.getParams().setMaxConnectionsPerHost(
				HostConfiguration.ANY_HOST_CONFIGURATION, 10);
		connectionManager.getParams().setMaxTotalConnections(20);
		connectionManager.getParams().setConnectionTimeout(10000);
		m_httpClient = new HttpClient(connectionManager);
		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
				0, false);
		m_httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				retryhandler);
		m_httpClient.getParams().setSoTimeout(10000);
	}

	public boolean callURL(String strURL, StringBuffer statusCode,
			StringBuffer response, boolean useProxy, String proxyHost,
			int proxyPort) {
		String fName = "callURL";
		GetMethod get = null;
		try {
			strURL = strURL.trim();
			logger.info("URL = " + strURL);

			URL urlObj = new URL(strURL);
			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(urlObj.getHost(), urlObj.getPort());
			if (useProxy && proxyHost != null && proxyPort != -1)
				ohcfg.setProxy(proxyHost, proxyPort);

			get = new GetMethod(strURL);
			long startTime = System.currentTimeMillis();
			logger.info("Start time in millisecond: "
					+ startTime);
			int httpResponseCode = m_httpClient.executeMethod(ohcfg, get);
			statusCode.append(httpResponseCode);
			response.append(get.getResponseBodyAsString());
			long endTime = System.currentTimeMillis();
			logger.info("End time in millisecond: "
					+ endTime);
			logger.info("Diff in millisecond: "
					+ (endTime - startTime));
			logger.info("Response Code:" + statusCode);
			logger.info("Response ->"
					+ response.toString().trim() + "");
			if (httpResponseCode == 200)
				return true;
			else
				return false;
		} catch (Throwable e) {
			logger.error("", e);
			return false;
		} finally {
			if (get != null)
				get.releaseConnection();
		}
	} 
	private void collectCopyTransFiles()
     {

      File copyTrans_dir = null;

      if (m_transDir != null)
          copyTrans_dir = new File(m_transDir);

      if (copyTrans_dir.exists())
      {
          File[] copyTrans_list = copyTrans_dir.listFiles(new FilenameFilter()
          {
              public boolean accept(File file, String name)
              {
                  Calendar cal = Calendar.getInstance();
                  cal.add(Calendar.DATE, -1);
                  Date today = cal.getTime();
                  cal.add(Calendar.DATE, -m_data_collection_days);
                  Date yest = cal.getTime();
                  if (name.startsWith("COPY_TRANS"))
                  {
                      try
                      {

                          String dateStr = name.substring(11,19);
                          Date file_date = m_format.parse(dateStr);
                          if (file_date.before(yest))
                          {
                              //                              file.delete();
                              return false;
                          }
                          else if (file_date.before(today))
                          {
                              return true;
                          }
                          else
                              return false;
                      }
                      catch(Exception e)
                      {
                          return false;
                      }
                  }
                  else
                   {
                       return false;
                   }
                }

          });

          if (copyTrans_list != null && copyTrans_list.length > 0)
          {
              Tools.addToLogFile("Copy Trans File collection Started...");
              File copy = new File(m_gathererPath + "/copyTrans");

              if (!copy.exists())
              {
                  copy.mkdirs();
              }

              for (int i = 0; i < copyTrans_list.length; i++)
              {
                  Tools.moveFile(m_gathererPath + "/copyTrans", copyTrans_list[i]);
              }
              Tools.addToLogFile("CopyTrans File collection Ended...");
          }
      }
     }

     private void makeReportingFiles()
 {
             logger.info("Entering");
             int gather_hour = getParameterAsInt("GATHERER_HOUR", 1);
             Calendar cal = Calendar.getInstance();
         int current_hour = cal.get(Calendar.HOUR_OF_DAY);
         if (current_hour >= gather_hour)
         {
             String zip_file = getZipFileName();
             if (! (new File(zip_file).exists()))
             {
                 logger.info("yesterday's zip " +zip_file+ " not found. So calling gather()");
                 gather();
             }
         }
         logger.info("Exiting");
 }

         private void gather()
 {
     logger.info("Entering");
     try
     {

             if(m_writeTrans)
                     collectCopyTransFiles();

             String zipfilename = createzip(null);

             logger.info("zipfilename " + zipfilename);
             if (zipfilename == null)
             {
                 logger.info("No Zip file created");
                 return;
             }

         uploadZipFilesToSpiderDir(zipfilename);

         }
         catch(Exception e)
         {
             logger.error("", e);
         }
     logger.info("Exiting");
 }
 private void uploadZipFilesToSpiderDir(String zipfile)
 {
     File file_to_copy = new File(zipfile);
     Tools.moveFile(m_spider_dir, file_to_copy);
     File[] zip_files = getZipFileList();
     if (zip_files == null || zip_files.length <= 0)
         return;
     for (int i = 0; i < zip_files.length; i++)
     {
         String date = "";
         StringTokenizer tokens = new StringTokenizer(zip_files[i].getName(), "_");
         while (tokens.hasMoreTokens())
             date = tokens.nextToken();
         Date zipFileDate = Tools.getdate(date.substring(0, date.indexOf(".")), "yyyy-MMM-dd");
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.DATE, -2);
         Date compDate = Tools.getdate(Tools.getChangedFormatDate(cal.getTime()), "yyyy-MM-dd");
         logger.info(" zipfile date " + zipFileDate + " cal.getTime() "+ cal.getTime());

         if (compDate.after(zipFileDate))
             zip_files[i].delete();
     }
 }
 private String getZipFileName()
 {
     String cust = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CUST_NAME", null);
     String site = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SITE_NAME", null);
     Calendar cal = Calendar.getInstance();
     cal.add(Calendar.DATE, -1);//yesterday
     String datename = Tools.getDateAsName(cal.getTime());

     return m_gathererPath + "/RBTGatherer_" + cust + "_" + site + "_"+ datename + ".zip";
 }
 private File[] getZipFileList()
 {
     File[] list = new File(m_gathererPath + "/").listFiles(new FilenameFilter()
     {
             public boolean accept(File dir, String name)
         {
             if (name.endsWith(".zip"))
                 return true;
             else
                 return false;
         }
     });
     return list;
 }

         public String createzip(String fileNamePrefix)
 {

     if (folders_to_zip.size() == 0)
         return null;

     getparams();

     boolean zipped = false;
     String zipFileName = "";
     if (fileNamePrefix != null)
         zipFileName = zipFileName.trim() + fileNamePrefix + "-";

     zipFileName = m_gathererPath + "/" + zipFileName.trim()
             + "RBTGatherer_" + m_cust + "_" + m_site + "_" + m_datename
             + ".zip";

     try
     {
         ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                 zipFileName));
         out.setLevel(Deflater.BEST_COMPRESSION);

         if (fileNamePrefix == null)
         {
             File[] files = new File(m_gathererPath).listFiles();
             if (files != null && files.length > 0)
             {
                 for (int j = 0; j < files.length; j++)
                 {
                     if (files[j].getName().endsWith(".htm")
                             || files[j].getName().endsWith(".cfg")
                             || files[j].getName().endsWith(".log")
                             || files[j].getName().endsWith(".xml"))
                     {
                         add2zip(out, m_gathererPath + "/"
                                 + files[j].getName(), null);
                         files[j].delete();
                     }
                 }
             }
             for (int i = 0; i < folders_to_zip.size(); i++)
             {
                 String zip_folder = String
                         .valueOf(folders_to_zip.get(i));
                 logger.info("Adding to zip folder : " + zip_folder);
                 zipped = dir2zip(out, zip_folder);
                 if (zipped)
                     delete(zip_folder);
                 zipped = false;
             }

         }
         else
         {
             zipped = dir2zip(out, m_gathererPath + "/"
                     + "db-Full");
             if (zipped)
                 delete(m_gathererPath + "/" + "db-Full");
         }
         out.close();
     }
     catch (FileNotFoundException fnfe)
     {
         fnfe.printStackTrace();
     }
     catch (IOException ioe)
     {
         ioe.printStackTrace();
     }

     return zipFileName;
 }
 private void delete(String dir)
 {
     File delete_folder = new File(dir);
     String[] files_to_delete = delete_folder.list();
     for (int i = 0; i < files_to_delete.length; i++)
     {
         new File(dir + File.separator + files_to_delete[i]).delete();
     }
     delete_folder.delete();
 }
 private void getparams()
 {
     m_cust = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CUST_NAME", null);
     if (m_cust == null)
     {
         logger.info(" cust_name not present");
     }

     m_site = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SITE_NAME", null);
     if (m_site == null)
     {
         logger.info(" site_name not present");
     }
     Calendar cal = Calendar.getInstance();
     cal.add(Calendar.DATE, -1);//yesterday
     m_datename = Tools.getDateAsName(cal.getTime());
 }
 public static boolean add2zip(ZipOutputStream out, String fname,
         String dname)
 {
     try
     {
         String name = null;
         if (dname != null)
         {
             name = new File(dname).getName() + "/"
                     + new File(fname).getName();
         }
         else
         {
             name = new File(fname).getName();
         }

         out.putNextEntry(new ZipEntry(name));
         FileInputStream in = new FileInputStream(new File(fname));
         int len;
         byte[] buffer = new byte[18024];
         while ((len = in.read(buffer)) > 0)
         {
             out.write(buffer, 0, len);
         }
         out.closeEntry();
         in.close();
         return true;
     }
     catch (IllegalArgumentException iae)
     {
         iae.printStackTrace();
     }
     catch (FileNotFoundException fnfe)
     {
         fnfe.printStackTrace();
     }
     catch (IOException ioe)
     {
         ioe.printStackTrace();
     }
     return false;
 }

public static boolean dir2zip(ZipOutputStream out, String dname)
 {
     File dir = new File(dname);
     String[] filesToZip = dir.list();
     if (filesToZip == null || filesToZip.length <= 0)
     {
         logger.info(dname
                 + " does not have any files");
         return false;
     }
     filesToZip = dir.list();
     File tmp_file;
     /*
      * try { out.putNextEntry(new ZipEntry (dname + "/")); } catch
      * (IOException ioe) { ioe.printStackTrace(); }
      */for (int i = 0; i < filesToZip.length; i++)
     {
         String fname = dname + File.separator + filesToZip[i];
         logger.info("RBTGatherer zip file trying to be zipped "
                                    + fname);
         tmp_file = new File(fname);
         if (tmp_file.isDirectory())
         {
             /*
              * File[] f = tmp_file.listFiles(); for(int j=0; j
              * <f.length;j++) { Tools.logDetail("_class", "_class",
              * "RBTGatherer zip file CHECKING is directory
              * "+f[j].getName()); if(f[j].isDirectory()) return false; }
              */dir2zip(out, fname);
         }
         else
         {
             add2zip(out, fname, dname);
         }
     }

     return true;
 }
	private void makeThreads()
	{
		logger.info("entering..");
		for(int i = 0; i < noOfThreads;i++)
		{
			logger.info("Making Thread "+ i);
			RTCommonCopyThread rtCCThread = new RTCommonCopyThread(i_rtCCHelper);
			rtCCThread.start();
			i_threadPool.add(rtCCThread);
			logger.info("Made Thread "+ i);
		}	
	}
	
	private void checkThreads()
	{
		logger.info("entering..");
		for(int i = 0; i < noOfThreads; i++)
		{
			RTCommonCopyThread rtCCThread = (RTCommonCopyThread)i_threadPool.get(i);
			if(rtCCThread == null || !rtCCThread.isAlive())
			{
				logger.info("A ccThread had died. Remaking it.");
				rtCCThread = new RTCommonCopyThread(i_rtCCHelper);
				rtCCThread.start();
				i_threadPool.add(i,rtCCThread);
				logger.info("Add replacemet ccThread.");
			}
		}
	}
	//Added for RL-27488
	private String getReplacedRdcURL(String url, ViralSMSTable vst,
			String operatorName) {
		String operatorUrl = null;
		HashMap<String, String> urlMap = new HashMap<String, String>();
		String[] opUrls = url.split(";");
		if (opUrls != null && opUrls.length > 0) {
			for (String opUrl : opUrls) {
				String[] urlParams = opUrl.split(":");
				if (urlParams != null && urlParams.length > 0) {
					urlMap.put(urlParams[0], urlParams[1]);
				}
			}
		}
		if (urlMap != null && !urlMap.isEmpty() && urlMap.size() > 0) {
			if (urlMap.containsKey(operatorName)) {
				operatorUrl = urlMap.get(operatorName);
			} else {
				operatorUrl = urlMap.get("ALL");
			}
		}
		operatorUrl.replaceAll("%subscriber_id%", vst.subID());
		operatorUrl.replaceAll("%caller_id%", vst.callerID());
		operatorUrl.replaceAll("%clip_id%", vst.clipID());
		operatorUrl.replaceAll("%sel_by%", getClassType(vst));
		operatorUrl.replaceAll("%opr_flag%", "1");
		operatorUrl.replaceAll("%sms_type%", RTCOPY);
		return operatorUrl;
	}
	//Ended for RL-27488
}
