package com.onmobile.apps.ringbacktones.promotions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.promotions.viral.OSCnNMSLogProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.common.exception.OnMobileException;

public class RBTViralMain extends Thread{

	private static Logger logger = Logger.getLogger(RBTViralMain.class);
	
	private static RBTDBManager m_rbtDBManager= null;
	public static ParametersCacheManager m_rbtParamCacheManager = null;
	private static RBTConnector m_rbtConnector = null;	
	private static boolean m_continue = true;
	private static long m_nextInitTime = -1;
	public static long m_oldestFile = 0;
	public static final String m_paramType = "VIRAL";
	private static final String _class = "RBTViral";
	private static HashMap<String,String> m_playerCdrPathMap = new HashMap<String, String>();
	private static Hashtable<String, Integer> m_week = new Hashtable<String, Integer>();
	private static ArrayList<ArrayList<String>> m_blackListTimes = new ArrayList<ArrayList<String>>();
	private static ArrayList<String> m_blockedClips = new ArrayList<String>();
	private static ArrayList<String> m_blockedMappingClips = new ArrayList<String>();
	private static RBTViralMain m_rbtViral = null; 
	private static Object m_initViral = new Object();
	public static long m_linesToRead = 1000;
	private static ArrayList<String> m_testNos = null;
	public static Hashtable<String, Long> m_nmsRecords = null;
    public static Vector<String> m_oscRecords = null;
    private static URLCodec m_urlEncoder = new URLCodec();		
	private static WriteDailyTrans m_writeTrans = null;
	//player xml params need to ignore RRBT entries
	protected static String _rrbtCalledPartyPrefix = null;
	protected static boolean _playerMultipleCallLegPolicy = true;
	private static Set<String> allowedPrefixesSet = null;


	public static void main(String[] args){

		RBTViralMain viral = RBTViralMain.getInstance(false);
		viral.setName("VIRAL_MAIN");
		viral.start();
	}

	public static RBTViralMain getInstance(boolean menu) {
		if (m_rbtViral == null) {
			synchronized (m_initViral) {
				if (m_rbtViral == null) {
					try {
						m_rbtViral = new RBTViralMain(menu);
					}
					catch (Throwable e) {
						logger.error("", e);
						m_rbtViral = null;
					}
				}
			}
		}
		return m_rbtViral;
	}


	private RBTViralMain(boolean menu){

		Tools.init(_class, false);
		m_rbtDBManager = RBTDBManager.getInstance();

		m_rbtConnector = RBTConnector.getInstance();
		m_rbtParamCacheManager =  CacheManagerUtil.getParametersCacheManager();

		m_week.put("SUN", new Integer(1));
		m_week.put("MON", new Integer(2));
		m_week.put("TUE", new Integer(3));
		m_week.put("WED", new Integer(4));
		m_week.put("THU", new Integer(5));
		m_week.put("FRI", new Integer(6));
		m_week.put("SAT", new Integer(7));

		for(int k=0; k<=7; k++)
			m_blackListTimes.add(new ArrayList<String>());

		initTrans(menu);
		initAllowedPrefixes();
	}

	private static void initAllowedPrefixes()
	{
		String allowedPrefixesStr = RBTParametersUtils.getParamAsString("VIRAL", "ALLOWED_PREFIXES", null);
		if (allowedPrefixesStr != null)
		{
			allowedPrefixesSet = new HashSet<String>();
			String[] prefixes = allowedPrefixesStr.split(",");
			for (String prefix : prefixes)
			{
				prefix = prefix.trim();
				if (!prefix.equals(""))
					allowedPrefixesSet.add(prefix);
			}
		}
	}

	private static void initTrans(boolean createMenuTrans){

		String sdrWorkingDir = m_rbtParamCacheManager.getParameter(m_paramType, "VIRAL_TRANS_PATH", ".").getValue();
		if(createMenuTrans)
			sdrWorkingDir = m_rbtParamCacheManager.getParameter(m_paramType, "USSD_MENU_TRANS_PATH", ".").getValue();

		ArrayList<String> headers = new ArrayList<String> ();
		headers.add("CALLER");
		headers.add("CALLED");
		headers.add("CLIP");
		headers.add("TIME OF CALL");
		m_writeTrans = new WriteDailyTrans(sdrWorkingDir, "VIRAL_REQUEST", headers);
	}

	public void run(){

		int count = -1;
		ArrayList<Thread> threads = new ArrayList<Thread>();
		while(m_continue)
		{
			try{
				Parameters archParam = m_rbtParamCacheManager.getParameter(m_paramType, "CENTRALIZED_ARCHITECTURE", "FALSE"); 
				if (archParam != null && archParam.getValue() != null && archParam.getValue().equalsIgnoreCase("TRUE"))
				{
					logger.info("Viral Server Daemon started with centralized architecture");
					initializeTestNos();
					OSCnNMSLogProcessor processor = new OSCnNMSLogProcessor();
					processor.startProcessing();
				}
				else
				{
					if(m_nextInitTime == -1 || (System.currentTimeMillis()  + 10000 >= m_nextInitTime))
					{
						initialize();
						m_nextInitTime = getnexttime(1);
						if(m_playerCdrPathMap.size() <= 0)
						{
							logger.info("Telephony IPs could not be retrieved exiting !!!!!");
							System.exit(-1);
						}

						logger.info("Telephony servers resolved to "+m_playerCdrPathMap.toString());
					}

					if(!isTimeNowBlackOut())
					{
						logger.info("Checking if workerThreads are still processing");
						for(int i=0; i<threads.size();i++){
							try
							{
								threads.get(i).join();
							}catch(Throwable t)
							{
								logger.error("", t);
							}
						}
						logger.info("All workerThreads dead so initializing processing");
						initializeBlockedContents();
						threads.clear();
						long time = (2 * 60 * 1000);
						try
						{
							int interval = Integer.parseInt(m_rbtParamCacheManager.getParameter(m_paramType, "READ_LAST_N_MINUTES_RECORDS", "2").getValue());
							time = interval * 60 * 1000;
						}
						catch(Exception e)
						{
							time = (2 * 60 * 1000);
						}
						m_oldestFile = System.currentTimeMillis() - time;
						logger.info("Oldest record to be processed in this run "+new Date(m_oldestFile));
						if(m_playerCdrPathMap.size() <=0)
						{
							logger.info("No CDR directories found to be processed");
							System.exit(-1);	            	
						}

						if(++count >= m_playerCdrPathMap.size())
							count = 0;

						Iterator<String> osc = sortIteratorDesc(m_playerCdrPathMap.keySet().iterator());
						int n = 0;
						while(n < count){
							osc.next();
							n++;
						}

						String oscPath = osc.next();
						String nmsPath = m_playerCdrPathMap.get(oscPath);

						File[] oscCdrs = getLatestFiles(oscPath);
						File[] nmsCdrs = getLatestFiles(nmsPath);

						if(oscCdrs == null || nmsCdrs == null || oscCdrs.length <=0 || nmsCdrs.length <=0)
						{
							logger.info("Valid CDRs not obtained for oscPath "+oscPath + "- length [" + oscCdrs.length+ "] nmsPath " +nmsPath + " - length ["+nmsCdrs.length + "]");
							continue;
						}

						logger.info("Valid CDRs obtained for oscPath "+oscPath + "- length [" + oscCdrs.length+ "] nmsPath " +nmsPath + " - length ["+nmsCdrs.length + "]");

						Parameters param = RBTViralMain.m_rbtParamCacheManager.getParameter(RBTViralMain.m_paramType, "LINES_TO_READ", "1000");

						try{
							m_linesToRead = Long.parseLong(param.getValue());
						}
						catch(Exception e){
							m_linesToRead = 1000;
						}

						m_linesToRead = m_linesToRead/oscCdrs.length;

						int nn = oscCdrs.length / nmsCdrs.length;

						m_oscRecords = new Vector<String>();
						for(int i=0; i<oscCdrs.length;i++)
						{
							GetCDRRecords cdr = new GetCDRRecords(oscCdrs[i], m_oscRecords);
							cdr.setName("OSCCDRCOLLECTION"+(i+1));
							threads.add(cdr);
							cdr.start();
						}

						m_nmsRecords = new Hashtable<String, Long>();
						for(int i=0; i<nmsCdrs.length;i++)
						{
							GetCDRRecords cdr = new GetCDRRecords(nmsCdrs[i], m_nmsRecords, nn);
							cdr.setName("NMSCDRCOLLECTION"+(i+1));
							threads.add(cdr);
							cdr.start();
						}

						for(int i=0; i<threads.size();i++){
							try
							{
								threads.get(i).join();
							}catch(Throwable t)
							{
								logger.error("", t);
							}
						}

						GetCDRRecords.v.clear();

						logger.info("CDR collection completed starting processor threads OSC count "+m_oscRecords.size() + " NMS count "+m_nmsRecords.size());

						threads.clear();

						int workerCount = 5;
						if((m_oscRecords.size()/100) > 5)
							workerCount =m_oscRecords.size()/100;

						for(int i=0; i<workerCount;i++)
						{
							WorkerThread work = new WorkerThread(m_oscRecords, m_nmsRecords);
							work.setName("WORKERTHREAD"+(i+1));
							threads.add(work);
							work.start();
						}
					}
					else
						logger.info(" Not processing as blackOutPeriod ");
				}

			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
			finally
			{
				Parameters param = RBTViralMain.m_rbtParamCacheManager.getParameter(RBTViralMain.m_paramType, "SLEEP_INTERVAL_MIN", "5");

				try{
					logger.info("Sleeping for "+param.getValue()+" minutes ");
					Thread.sleep(Integer.parseInt(param.getValue()) * 60 * 1000);
				}
				catch(Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		}

	}

	private static boolean isTestingNo(String caller){
		if(m_testNos != null && !m_testNos.contains(caller))
			return false;

		return true;
	}


	public static boolean addViral(String caller, String called, String clipID)
	{
		ViralData[] viral = m_rbtConnector.getSubscriberRbtclient().getViralData(caller, null, "BASIC", null, null);
		if(viral != null && viral.length > 0)
		{
			Parameters param = m_rbtParamCacheManager.getParameter(m_paramType, "OLDVIRAL_CLEANING_PERIOD_IN_HRS", "336");
			long diff = 336*3600*1000;
			try{
				diff = Integer.parseInt(param.getValue()) *3600 *1000;
			}catch(Throwable t){
				diff = 336*3600*1000;
			}

			if((System.currentTimeMillis() - viral[0].getSentTime().getTime()) < diff)
			{
				logger.info(caller + " already received viral @"+ viral[0].getSentTime());
				return false;
			}
			else
			{
				if(m_rbtDBManager.updateViralSMSTable(caller,new Date(),"BASIC",called,clipID.toString(),0, null, null,null))
					return true;
				else
				{
					logger.info("updation failed "+caller);
					return false;
				}
			}
				
		}

		if(m_rbtDBManager.insertViralSMSTableMap(caller,new Date(),"BASIC",called,clipID.toString(),0, null, null,null) == null)
		{
			logger.info("Adding viral data failed "+caller);
			return false;
		}
		
		return true;
    }

	public static String isCallerValid(String caller)
	{
		if(!isTestingNo(caller))
		{
			logger.info("Testing On but Sub "+caller + " not got among TestNo");
			return null;
		}

		if (allowedPrefixesSet != null)
		{
			boolean isPrefixAllowed = false;
			for (int i = caller.length(); i > 0; i--)
			{
				String callerIDSubStr = caller.substring(0, i);
				if (allowedPrefixesSet.contains(callerIDSubStr))
				{
					isPrefixAllowed = true;
					break;
				}
			}

			if (!isPrefixAllowed)
			{
				logger.info("Caller " + caller + " prefix is not allowed");
				return null;
			}
		}

		SubscriberDetail detail = RbtServicesMgr.getSubscriberDetail(new MNPContext(caller));
		if(detail == null)
		{
			logger.info("Sub "+caller + " details not got ");
			return null;
		}
		else if(!detail.isValidSubscriber())
		{
			logger.info("Sub "+caller + " is not valid prefix in record ");
			return null;
		}

		String circleID = detail.getCircleID();

		Parameters p = m_rbtParamCacheManager.getParameter(m_paramType, "CHK_RBT_DND");
		if(p != null && p.getValue() != null && p.getValue().equalsIgnoreCase("TRUE") && isBlacklistSubscriber(caller))	
		{
			logger.info("Sub "+caller + " is blacklisted in RBT for viral ");
			return null;
		}
		boolean checkStatus = true;
		p = m_rbtParamCacheManager.getParameter(m_paramType, "CHK_RBT_INACTIVE");
		if(p != null && p.getValue() != null && p.getValue().equalsIgnoreCase("FALSE"))
			checkStatus = false;
		if(checkStatus)
		{
			Subscriber sub = m_rbtDBManager.getSubscriber(caller);
			if(sub != null && !sub.subYes().equalsIgnoreCase(iRBTConstant.STATE_DEACTIVATED))
			{
				logger.info("Sub "+caller + " is not inactive subscriber");
				return null;
			}
		}

		return circleID;
	}
	public static boolean isCalledValid(String called)
	{
		boolean userAllowed=true;
		boolean checkStatus = false;
		Parameters p = m_rbtParamCacheManager.getParameter(m_paramType, "CHK_RBT_LITE_USER");
		if(p != null && p.getValue() != null && p.getValue().equalsIgnoreCase("TRUE"))
			checkStatus = true;
		if(checkStatus)
		{
			Subscriber sub = m_rbtDBManager.getSubscriber(called);
			if(sub != null && sub.cosID()!=null)
			{
				String cosType=CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(sub.cosID()).getCosType();
				if(cosType!=null && cosType.equalsIgnoreCase("LITE")){
					userAllowed= false;
				}
				logger.info("Sub "+called + " is a LITE user");
				return userAllowed;
			}
		}
		return userAllowed;
	}
	public static boolean isLiteUser(String caller)
	{
		boolean userAllowed=false;

		Subscriber sub = m_rbtDBManager.getSubscriber(caller);
		if(sub != null && sub.cosID()!=null)
		{
			String cosType=CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(sub.cosID()).getCosType();
			if(cosType!=null && cosType.equalsIgnoreCase("LITE")){
				userAllowed= true;
				logger.info("Sub "+caller + " is a LITE user");
			}else{
				logger.info("Sub "+caller + " is not a LITE user");
			}
		}
		return userAllowed;
	}

	public static boolean sendViralPromotion(String caller, String called, int clipID, String circleID, String time){
        Parameters param = m_rbtParamCacheManager.getParameter(m_paramType, "BASIC_VIRAL_MESSAGE" ,"You just heard %SONG% when u called %CALLED%");
        String sms = param.getValue();
		if(circleID != null){
			param = m_rbtParamCacheManager.getParameter(m_paramType, "BASIC_VIRAL_MESSAGE"+circleID.toUpperCase(), null);
			if(param != null && param.getValue() != null && param.getValue().length() > 0)
				sms = param.getValue();
		}
	 	Clip c = m_rbtConnector.getMemCache().getClip(clipID);
        if(c == null)
        {
			logger.info("Clip Not Found !!!!!"+clipID);
			return false;

		}
		if (sms.indexOf("%SONG%") != -1)
		{
            sms = sms.substring(0, sms.indexOf("%SONG%"))
                    + c.getClipName()
                    + sms.substring(sms.indexOf("%SONG%") + 6);
        }
		if (sms.indexOf("%ARTIST%") != -1)
		{
            sms = sms.substring(0, sms.indexOf("%ARTIST%"))
                    + ((c.getArtist()!=null)?c.getArtist():"")
                    + sms.substring(sms.indexOf("%ARTIST%") + 8);
        }
        if (sms.indexOf("%CALLED%") != -1)
        {
            sms = sms.substring(0, sms.indexOf("%CALLED%"))
                    + called
                    + sms.substring(sms.indexOf("%CALLED%") + 8);
        }

		param = m_rbtParamCacheManager.getParameter(m_paramType, "VIRAL_PROMO_TYPE");
        if(param == null || param.getValue() == null)
        {
			logger.info("No viral type configured !!!!!");
			return false;

		}
		if(param.getValue().equalsIgnoreCase("SMS")){
			if(param == null || param.getValue() == null)
			{
				logger.info("No Basic viral SMS configured !!!!!");
				return false;
			}

			param = m_rbtParamCacheManager.getParameter(m_paramType, "SMS_DND_URL");
			if(param == null || param.getValue() == null)
			{
				logger.info("SMS DND URL not configured");
				try {
					Tools.sendSMS(m_rbtParamCacheManager.getParameter(m_paramType, "SMS_SENDER_NO", "123").getValue(), caller, sms, false);
				} catch (OnMobileException e) {
				}

				return true;
			}
			String url = param.getValue();
			url = url.replaceFirst("\\$sender\\$", m_rbtParamCacheManager.getParameter(m_paramType, "SMS_SENDER_NO", "123").getValue());
			url = url.replaceFirst("\\$receiver\\$", caller);
			url = url.replaceFirst("\\$smstext\\$", getEncodedUrlString(sms));
			Tools.callURL(url, new Integer(-1), new StringBuffer(), false, null, -1, false, 2000);
		}else if(param.getValue().equalsIgnoreCase("USSD")) {
			param = m_rbtParamCacheManager.getParameter(m_paramType, "USSD_URL");
			if(param == null || param.getValue() == null)
			{
				logger.info("USSD url not configured !!!!!");
				return false;
			}
			String url = param.getValue();
			url = url.replaceFirst("\\$sender\\$", m_rbtParamCacheManager.getParameter(m_paramType, "SMS_SENDER_NO", "123").getValue());
			url = url.replaceFirst("\\$receiver\\$", caller);
			url = url.replaceFirst("\\$smstext\\$", getEncodedUrlString(sms));
			Tools.callURL(url, new Integer(-1), new StringBuffer(), false, null, -1, false, 2000);
		} else if (param.getValue().equalsIgnoreCase("OBD")) {// this else added by Sreekar
			return promoteOBD(caller, called, c);
		}

		writeTrans(caller, called, clipID, time);
		return true;
	}
	
	public static String encodeParam(String param) {
		try {
			param = URLEncoder.encode(param, "UTF-8");
		}
		catch(Exception e) {
			
		}
		return param;
	}
	
	private static boolean promoteOBD(String caller, String called, Clip c) {
		Parameters urlParam = m_rbtParamCacheManager.getParameter(m_paramType, "OBD_URL");
		if (urlParam == null || urlParam.getValue() == null || urlParam.getValue().length() == 0) {
			logger.warn("OBD Url not configured, ignoring");
			return false;
		}
		MNPContext mnpContext = new MNPContext(caller, "VIRAL");
		SubscriberDetail subDetail = RbtServicesMgr.getSubscriberDetail(mnpContext);
		if (subDetail == null) {
			logger.warn("Couldn't get mnp sub detail for caller-" + caller);
			return false;
		}
		String url = urlParam.getValue();
		url = url.replace("$dialingNumber$", called);
		url = url.replace("$called$", called);
		url = url.replace("$receiver$", caller);
		url = url.replace("$clipId$", c.getClipId() + "");
		url = url.replace("$clipName$", encodeParam(c.getClipName()));
		url = url.replace("$clipWavFile$", encodeParam(c.getClipRbtWavFile()));

		// setting language from site prefix table
		String language = null;
		SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(subDetail.getCircleID());
		if (sitePrefix != null) {
			String allLanguages = sitePrefix.getSupportedLanguage();
			if (allLanguages != null) {
				String[] langArray = allLanguages.split(",");
				language = langArray[0];
			}
		}
		if (language != null)
			url = url.replace("$language$", language);
		else
			logger.warn("Couldn't get language for caller-" + caller);

		Integer status = new Integer(-1);
		StringBuffer response = new StringBuffer();
		Tools.callURL(url, status, response, false, null, -1, false, 2000);
		logger.debug("url-" + url + ", status-" + status + ", response-" + response.toString());
		return true;
	}
	
	public static int isSongNonPromotional(String wav, long dur){
		if(wav.indexOf("|") > -1)
		{
			String[] songs = wav.split("\\|");
			String validSong = null; 
			for(int i=0; i<songs.length; i++){
				Clip clip = m_rbtConnector.getMemCache().getClipByRbtWavFileName(songs[i]);	
				if(clip == null)
					dur -= 5;
				else
					validSong = songs[i];
			}

			if(validSong != null)
				wav = validSong;
			else
				wav = songs[0];
		}
		Parameters param = m_rbtParamCacheManager.getParameter(m_paramType, "MIN_DURATION_SONG");
		if(param == null || param.getValue() == null)
			logger.info("MIN_DURATION param missing");
		else
		{
			try{
				long duration = Long.parseLong(param.getValue());
				if(duration > dur){
					logger.info("Caller heard song only for "+dur+ " seconds as against min "+duration);
					return -1;
				}
			}catch(Throwable t){

			}
		}
		if(m_blockedMappingClips.contains(wav))
		{
			logger.info("Song "+wav+" is mapped to blocked category so returning false");
			return -1;
		}
		Clip c = m_rbtConnector.getMemCache().getClipByRbtWavFileName(wav);
		if(c == null || c.getClipEndTime().getTime() < System.currentTimeMillis())
		{
			logger.info("Clip not active or clip not present "+wav);
			return -1;
		}
		if(m_blockedClips.contains(c.getClipId())){
			logger.info("Song "+wav+" is blocked clip so returning false");
			return -1;

		}
		return c.getClipId();
	}

	private File[] getLatestFiles(String cdr)
	{
		File cdrDir = new File(cdr);
		logger.info("RBTViral::CDR directory parsed "
				+ cdrDir);

		File[] list = cdrDir.getAbsoluteFile().listFiles(new FilenameFilter()
		{

			public boolean accept(File dir, String name)
			{
				if (name.endsWith(".txt"))
				{
					if (name.startsWith("C"))
					{
						File f = new File(dir.getAbsolutePath()
								+ File.separator + name);
						if (f.lastModified() > m_oldestFile)
							return true;
						return false;
					}
					else
						return false;
				}
				else
					return false;
			}
		});


		return list;
	}

	private static boolean isTimeNowBlackOut()
	{
		Calendar cal = Calendar.getInstance();
		ArrayList<String> black = m_blackListTimes.get(cal.get(Calendar.DAY_OF_WEEK));
		logger.info("BlackOut Chkd against "+black.toString());
		if(black.contains(""+cal.get(Calendar.HOUR_OF_DAY)))
			return true;

		return false;
	}


	public void stopThread()
	{
		/*    	for (int i = 0; parse != null && i < m_numThreads; i++)
        {
            parse[i].stopThread();
        }
		 */  	
		m_continue = false;
	}

	private static long getnexttime(int hour)
	{
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, hour);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);

		long nexttime = now.getTime().getTime();
		if (nexttime < System.currentTimeMillis())
		{
			nexttime = nexttime + (24 * 3600 * 1000);
		}
		return nexttime;
	}


	private static boolean initialize(){

		ArrayList<String> playerIPs = parsePlayerXML();
		if(playerIPs != null){

			logger.info("Player IPs got from Player XML "+playerIPs.toString());

			Parameters param = m_rbtParamCacheManager.getParameter(m_paramType, "OSCCDR_PATH");
			if(param != null && param.getValue() != null){
				String[] cdrPath = param.getValue().split(",");
				for(int i=0; i<cdrPath.length; i++){
					String[] s = cdrPath[i].split(";");
					if(s.length < 2)
						logger.info("OSCCDR config not in ryte format "+cdrPath[i]);
					else
					{
						if(!playerIPs.remove(s[0]))
							logger.info("Telephony ip "+s[0]+ " not present in Player XMLs");
						String nmsIP = getNMSCDRPath(s[0], s[1]);
						if(nmsIP == null)
						{
							logger.info("Could not find NMS CDR path for "+s[0]);
							continue;
						}

						String ivmPath = s[1].substring(0, s[1].lastIndexOf("osccdr")) ;
						m_playerCdrPathMap.put(s[1], ivmPath.replaceFirst(s[0], nmsIP) + "cdr");
					}

				}

			}

			initializeBlackOut();
			initializeTestNos();
			return true;
		}
		else
		{
			logger.info("No Player IPs found from Player XMLs");
		}
		return false;
	}

	public static boolean initializeBlockedContents(){
		Parameters param = m_rbtParamCacheManager.getParameter(m_paramType, "COPY_BLOCKED_CLIP_IDS");
		if(param != null && param.getValue() != null)
		{
			String[] s = param.getValue().split(",");
			for(int i=0;i<s.length;i++)
				m_blockedClips.add(s[i]);
		}
		param = m_rbtParamCacheManager.getParameter(m_paramType, "COPY_BLOCKED_CATEGORY_IDS", "1,99");
		if(param == null || param.getValue() == null)
			logger.info("COPY_BLOCKED_CATEGORY_IDS param missing");
		String[] cat = param.getValue().split(",");
		for(int i=0;i<cat.length;i++){
			int catID=-1;
			try{
				catID = Integer.parseInt(cat[i]);
			}catch(Exception e){
				catID = -1;
			}
			if(catID == -1)
				continue;
			Clip[] clips = m_rbtConnector.getMemCache().getClips(catID);
			if(clips != null){
				for(int j=0; j<clips.length; j++)
					m_blockedMappingClips.add(clips[j].getClipRbtWavFile());
			}
		}

		logger.info(" blocked contents initialized -- clips"+m_blockedClips.toString() + " mapped clips " +m_blockedMappingClips.toString());
		return true;
	}

	private static void initializeTestNos(){
		Parameters param = m_rbtParamCacheManager.getParameter(m_paramType, "TEST_ON");
		if(param == null || param.getValue() == null){
			logger.info("Test Param Missing so taking false");
			return;
		}
		String[] s = param.getValue().split(",");
		if(s.length < 2){
			logger.info("Test Param not proper so taking false");
			return;
		}
		if(s[0].equalsIgnoreCase("TRUE")){
			m_testNos = new ArrayList<String>();
			File testFile = new File(s[1]);
			if(testFile != null && testFile.exists())
			{
				try{
					BufferedReader br = new BufferedReader(new FileReader(testFile));
					while(br.ready())
						m_testNos.add(subID(br.readLine().trim()));
					logger.info("Initialized the test no's");
				}
				catch(Exception e){
					logger.error("", e);
				}
			}
		}
	}

	private static void initializeBlackOut()
	{
		Parameters param = m_rbtParamCacheManager.getParameter(m_paramType, "BLACK_OUT_PERIOD");
		if(param == null || param.getValue() == null){
			logger.info("No BlackOut Configured");
			return;
		}

		String[] black = param.getValue().split(",");
		for(int i=0; i<black.length; i++)
		{
			if(black[i].indexOf("[") <= -1)
			{
				logger.info("No BlackOut Time Configured" + black[i]);
				continue;
			}
			ArrayList<Integer> days = getDays(black[i].substring(0, black[i].indexOf("[")));
			if(days != null && days.size() > 0)
			{
				ArrayList<String> times = getTimes(black[i].substring(black[i].indexOf("[")));
				for(int j=0;j<days.size(); j++)
					m_blackListTimes.add(days.get(j).intValue(), times);
			}
		}

		logger.info("BlackListTimes initialized "+m_blackListTimes.toString());

	}

	private static ArrayList<Integer> getDays(String s)
	{
		ArrayList<Integer> array = new ArrayList<Integer>();
		if(s.indexOf("-") > -1)
		{
			try
			{
				String day1 =  s.substring(0, s.indexOf("-"));
				String day2 = s.substring(s.indexOf("-") + 1);
				if(!m_week.containsKey(day1) || !m_week.containsKey(day2))
				{
					logger.info("Invalid week specified !!!!" + s);
					return null;
				}

				int startDay = m_week.get(day1).intValue();
				int endDay = m_week.get(day2).intValue();

				if(endDay > startDay)
					for(int t = startDay; t<=endDay; t++)
						array.add(new Integer(startDay++));
				else
				{
					for(int t = startDay; t<=7; t++)
						array.add(new Integer(startDay++));

					for(int t = 1; t<=endDay; t++)
						array.add(new Integer(t));

				}

			}
			catch(Throwable t){

			}
		}
		else
		{
			if(m_week.containsKey(s))
				array.add(m_week.get(s));
			else
				logger.info("Invalid week specified !!!!" + s);

		}

		logger.info("" + array.toString());
		return array;
	}

	private static ArrayList<String> getTimes(String s)
	{
		ArrayList<String> array = new ArrayList<String>();
		s = s.substring(1, s.length() -1);
		String[] ss = s.split(";");
		for(int i =0; i<ss.length; i++)
		{
			if(ss[i].indexOf("-") > -1)
			{
				try
				{
					int startTime = Integer.parseInt(ss[i].substring(0, ss[i].indexOf("-")));
					int endTime = Integer.parseInt(ss[i].substring(ss[i].indexOf("-") + 1));

					if(startTime > 23 || endTime > 23)
					{
						logger.info("Invalid time specified !!!!" + s);
						continue;
					}
					else if(endTime > startTime)
						for(int t = startTime; t<=endTime; t++)
							array.add(""+startTime++);
					else
					{
						for(int t = startTime; t<=23; t++)
							array.add(""+startTime++);

						for(int t = 0; t<=endTime; t++)
							array.add(""+t);

					}

				}
				catch(Throwable t){

				}
			}
			else
			{
				try
				{
					int n = Integer.parseInt(ss[i]);
					if(n >=0 && n<=23)
						array.add(ss[i]);
					else
						logger.info("Invalid time specified !!!!" + s);
				}
				catch(Throwable t){
					logger.info("Invalid time specified !!!!" + s);
				}
			}

		}

		return array;

	}


	private static String getNMSCDRPath(String ip, String path)
	{
		String ivmPath = path.substring(0, path.lastIndexOf("osccdr")) ;
		String configPath = ivmPath + "config/ivm.xml";
		String xml = getXMLAsString(configPath);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			Document document = builder.parse(is);
			Element nodeList = (Element)document.getElementsByTagName("Config").item(0);
			NodeList list = nodeList.getElementsByTagName("Component");
			//System.out.println(list.getLength());
			Element agentElement = null;
			Element nmsElement = null;
			for (int i = 0; i < list.getLength(); i++)
			{
				Element ele = (Element) list.item(i);
				String name = ele.getAttribute("name");
				if(name.equalsIgnoreCase("agent"))
					agentElement = ele;
				if(name.equalsIgnoreCase("nms"))
					nmsElement = ele;
			}
			if(agentElement != null)
			{
				Element controllerEle = (Element)agentElement.getElementsByTagName("Controller").item(0);
				String type = controllerEle.getAttribute("type");
				if(type != null && type.equalsIgnoreCase("msp"))
				{
					logger.info("Telephony "+ip + " has MSP cards");
					return ip;
				}
			}
			if(nmsElement != null)
			{
				Element ele = (Element)nmsElement.getElementsByTagName("General").item(0);
				if(ele != null)
				{
					String mode = ele.getAttribute("SignallingMode");
					if(mode == null || (!mode.equalsIgnoreCase("Standalone") && !mode.equalsIgnoreCase("Primary")))
					{
						ele = (Element)nmsElement.getElementsByTagName("Signalling").item(0);
						ele = (Element)ele.getElementsByTagName("Controllers").item(0);
						ele = (Element)ele.getElementsByTagName("Primary").item(0);
						if(ele != null)
							return ele.getAttribute("ip");
					}
					else
						return ip;
				}
			}

		}
		catch (Throwable t) {
			logger.error("", t);
		}

		return null;
	}

	/**
	 * changed by Sreekar to populate RRBT_CALLED_PARTY_PREFIX & MULTIPLE_CALL_LEG_POLICY
	 * @return
	 */
	private static ArrayList<String> parsePlayerXML()
	{
		Parameters xmlPaths = m_rbtParamCacheManager.getParameter(m_paramType, "PLAYER_XML_PATH");

		ArrayList<String> pathArray = new ArrayList<String>();

		if(xmlPaths != null && xmlPaths.getValue() != null)
		{
			String[] paths = xmlPaths.getValue().split(",");

			for(int i=0; i<paths.length; i++)
			{
				String xml = getXMLAsString(paths[i] + "/" + "rbtplayer.xml");
				if(xml == null)
					logger.info("Player xml @ "+paths[i]+" could not be read");
				else
				{
					ArrayList<String> list = getTelephonyIPs(xml);
					if(list != null && list.size() > 0)
						pathArray.addAll(list);
				}
			}

			return pathArray;
		}

		return null;
	}

	private static ArrayList<String> getTelephonyIPs(String xml)
	{
		ArrayList<String> a = new ArrayList<String>();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			Document document = builder.parse(is);
			Element nodeList = (Element)document.getElementsByTagName("RBT").item(0);
			Element element = (Element)nodeList.getElementsByTagName("memcached").item(0);
			//			element = (Element)nodeList.getElementsByTagName("pool").item(0);
			NodeList list = element.getElementsByTagName("pool");
			if(list !=  null && list.getLength() > 0){
				for(int j=0; j<list.getLength(); j++){
					element = (Element) list.item(j);
					if (element != null)
					{
						NodeList propertyNodeList = element.getElementsByTagName("server");
						for (int i = 0; i < propertyNodeList.getLength(); i++)
						{
							Element propertyElement = (Element) propertyNodeList.item(i);
							String ip = propertyElement.getAttribute("ip");
							a.add(ip);
						}
					}
				}
			}
			//populating RRBTParams added by Sreekar
			Element commonElement = (Element)nodeList.getElementsByTagName("Common").item(0);
			NodeList propertyElementList = commonElement.getElementsByTagName("property");
			if(propertyElementList != null && propertyElementList.getLength() > 0) {
				Element tempElement;
				for(int i = 0; i < propertyElementList.getLength(); i++) {
					tempElement = (Element) propertyElementList.item(i);
					String name = tempElement.getAttribute("name");
					if(name != null && name.equals("RRBT_CALLED_PARTY_PREFIX"))
						_rrbtCalledPartyPrefix = tempElement.getAttribute("value");
					else if(name != null && name.equals("MULTIPLE_CALL_LEG_POLICY"))
						_playerMultipleCallLegPolicy = tempElement.getAttribute("value").equalsIgnoreCase("true");
				}
			}
		}
		catch (Exception t) {
			logger.error("", t);
			return null;
		}

		return a;
	}

	private static String getXMLAsString(String path) {
		String response = null;
		try
		{
			FileInputStream fis = new FileInputStream(path);
			int iLength = fis.available();
			byte bTemp[] = new byte[iLength];
			fis.read(bTemp);
			fis.close();

			// Converting byte array to string
			response = new String(bTemp); // Converts the byte array to String
		}
		catch (FileNotFoundException fileExc)
		{
			logger.error("", fileExc);
			return null;
		}
		catch (IOException ioExc)
		{
			logger.error("", ioExc);
			return null;
		}

		return response;
	}

	public static String subID(String subscriberID)
	{
		Parameters param = RBTViralMain.m_rbtParamCacheManager.getParameter(RBTViralMain.m_paramType, "CALLED_PREFIX", null);
		if(param != null && subscriberID != null){
			if(param.getValue() != null &&  param.getValue().length() > 0 && subscriberID.length() > param.getValue().length() && subscriberID.startsWith(param.getValue()))
				subscriberID = subscriberID.substring(param.getValue().length());
		}
		return (m_rbtDBManager.subID(subscriberID));
	}

	private Iterator<String> sortIteratorDesc(Iterator<String> i)
	{
		ArrayList<String> a = new ArrayList<String>();
		while (i.hasNext())
			a.add(i.next());

		Collections.sort(a, new Comparator<Object>()
				{
			public int compare(Object a, Object b)
			{
				String aStr, bStr;
				aStr = (String) a;
				bStr = (String) b;
				int val = aStr.compareTo(bStr);
				if (val != 0)
					return (-val);
				else
					return val;
			}
				});
		return (a.iterator());
	}
	private static boolean isBlacklistSubscriber(String subID)
	{
		return m_rbtDBManager.isViralBlackListSub(subID);
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

	private static boolean writeTrans(String caller, String called, int clip, String time)
	{
		HashMap<String,String> h = new HashMap<String,String> ();
		h.put("CALLER", caller);
		h.put("CALLED", called);
		h.put("CLIP", ""+clip);
		h.put("TIME OF CALL", time);

		if(m_writeTrans != null)
		{
			m_writeTrans.writeTrans(h);
			return true;
		}

		return false;
	}

	public static String getViralUSSDMenu(String caller)
	{
		String ussdMenu = m_rbtParamCacheManager.getParameter(m_paramType, "USSD_MENU", ".").getValue();
		String called = null;
		String time = null;
		int clipID = -1;
		String sms = null;
		if(caller != null)
		{
			SubscriberDetail detail = RbtServicesMgr.getSubscriberDetail(new MNPContext(caller));
			if(detail == null)
			{
				logger.info("Sub "+caller + " details not got ");
				return null;
			}
			else if(!detail.isValidSubscriber())
			{
				logger.info("Sub "+caller + " is not valid prefix in record ");
				return null;
			}

			String circleID = detail.getCircleID();
			ViralData[] viral = m_rbtConnector.getSubscriberRbtclient().getViralData(caller, null, "BASIC", null, null);
			if(viral != null && viral.length > 0)
			{
				Parameters param = m_rbtParamCacheManager.getParameter(m_paramType, "BASIC_USSD_MESSAGE" ,"You just heard %SONG% when u called %CALLED%");
				sms = param.getValue();
				if(circleID != null){
					param = m_rbtParamCacheManager.getParameter(m_paramType, "BASIC_USSD_MESSAGE"+circleID.toUpperCase(), null);
					if(param != null && param.getValue() != null && param.getValue().length() > 0)
						sms = param.getValue();
				}
				ViralData viralData = viral[viral.length - 1];
				called = viralData.getCallerID();
				clipID = Integer.parseInt(viralData.getClipID());
				SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss");
				time = sdf.format(viralData.getSentTime());
				
				String clipLanguage = RBTParametersUtils.getParamAsString("VIRAL", "CLIP_PROMOTION_LANGUAGE", null);
				
				Clip c = m_rbtConnector.getMemCache().getClip(clipID, clipLanguage);
				if(c == null)
				{
					logger.info("Clip Not Found !!!!!"+clipID);
					return null;

				}
				if (sms.indexOf("%SONG%") != -1)
				{
					sms = sms.replace("%SONG%", c.getClipName());
					/*sms = sms.substring(0, sms.indexOf("%SONG%"))
					+ c.getClipName()
					+ sms.substring(sms.indexOf("%SONG%") + 6);*/
				}
				if (sms.indexOf("%CALLED%") != -1)
				{
					sms = sms.replace("%CALLED%", called);
					/*sms = sms.substring(0, sms.indexOf("%CALLED%"))
					+ called
					+ sms.substring(sms.indexOf("%CALLED%") + 8);*/
				}

				if (sms.indexOf("%TIME%") != -1)
				{
					sms = sms.replace("%TIME%", time);
					/*sms = sms.substring(0, sms.indexOf("%TIME%"))
					+ time
					+ sms.substring(sms.indexOf("%TIME%") + 6);*/
				}
			}		
		}
		HashMap<String,String> h = new HashMap<String,String> ();
		h.put("CALLER", caller);
		h.put("CALLED", called);
		h.put("CLIP", ""+clipID);
		h.put("TIME OF CALL", time);

		if(m_writeTrans != null)
			m_writeTrans.writeTrans(h);

		if(sms == null)
			return null;

		ussdMenu = ussdMenu.replaceAll("<%msisdn%>", caller);

		return ussdMenu.replaceFirst("\\$smstext\\$", sms);
	}


	public static String getRatingMenu(String caller)
	{
		String ratingMenu = m_rbtParamCacheManager.getParameter(m_paramType, "RATING_MENU", null).getValue();
		String clipID = null;
		String calledId = null;
		ratingMenu = ratingMenu.replaceAll("<%msisdn%>", caller);
		if(caller != null)
		{
			ViralData[] viral = m_rbtConnector.getSubscriberRbtclient().getViralData(caller, null, "BASIC", null, null);
			if(viral != null && viral.length > 0)
			{
				clipID = viral[viral.length - 1].getClipID();
				calledId = viral[viral.length - 1].getCallerID();
			}
		}

		if(clipID != null)
			ratingMenu = ratingMenu.replaceAll("<%CLIPID%>", clipID);
		if(calledId != null)
			ratingMenu = ratingMenu.replaceAll("<%CALLEDID%>", calledId);

		logger.info("RBT:: httpParameters: " + ratingMenu);
		return ratingMenu;
	}


	public static String getRecommendationMenu(String caller)
	{
		String url = m_rbtParamCacheManager.getParameter(m_paramType, "BI_RECOMM_URL" , null).getValue().trim();
		logger.info("RBT:: RECOMM_URL: " +url);
		String recommMenu = null;
		try 
		{
			url = url.replaceAll("<%msisdn%>", caller);
			HttpParameters httpParameters = new HttpParameters(url);
			logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("RBT:: httpResponse: " + httpResponse);

			String clipsString = httpResponse.getResponse().trim();
			String clips[] = clipsString.split(",");
			String[] clipIds = new String[clips.length];

			for(int i=0;i<clips.length;i++)
			{
				String str[] = clips[i].split("_");
				clipIds[i] = str[0];
			}   
			Parameters param = m_rbtParamCacheManager.getParameter(m_paramType, "RECOMM_MENU" ,null);
			recommMenu = param.getValue();
			int i=1;
			for (String clipId : clipIds) 
			{
				recommMenu = recommMenu.replaceAll("<%CLIPID"+i+"%>", clipId);
				Clip clip = m_rbtConnector.getMemCache().getClip(clipId);
				if(clip != null)
				{
					String songName = clip.getClipName();
					recommMenu = recommMenu.replaceAll("%SONG"+i, songName);
				}
				i++;
			}

			recommMenu = recommMenu.replaceAll("<%msisdn%>", caller);
		} 
		catch (Exception e) 
		{
			logger.error("", e);
		}
		return recommMenu;
	}


}
