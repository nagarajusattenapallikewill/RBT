package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.RBTCopyProcessor.ResponseEnum;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.SubscriptionBean;

public class RTCopyProcessor extends Thread implements iRBTConstant {
	private static Logger logger = Logger.getLogger(RTCopyProcessor.class);
	private String _class = "RTCopyProcessor";

	private RBTDBManager rbtDBManager = null;
	private RBTConnector rbtConnector=null;
	public RBTGatherer m_parentGathererThread = null;

	String m_sms_type = "RTCOPY";
	String m_localType = "INCIRCLE";
	String m_nationalType = "OPERATOR";
	String m_nonOnmobileType = "NON_ONMOBILE";
	String m_crossOperatorType = "CROSS_OPERATOR";
	
	public static Hashtable m_rtOperatorPrefixes = null;
	public static Hashtable m_rtNonOnmobilePrefix = null;
	public static Hashtable m_rtCrossOperatorPrefix = null;
	
	boolean m_redirectNational = false;
	private int copyAmount = 10;
	String crossRTCopyCategory = "26";
	HashMap copyChargeClassMap = new HashMap();
	String defaultClipWavName = null;
	String m_copyClassType = null;

	private String m_transDir = "./Trans";
	private String m_trans_file = null;
	private BufferedWriter m_transbufferWriter = null;
	private int m_runHourForTrans = 0;
	private long m_nextWakeUpTimeForTrans = 0;
	private SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy");

	public static final String FAILURE = "FAILURE";
	static String STATUS_ERROR = "ERROR";
	String m_contentUrl = null;

	ArrayList m_rtCopyThreadPool = new ArrayList();
	public static ArrayList m_pendingRTCopy = new ArrayList();


	String RTCOPY = "RTCOPY";
	String RTCOPIED = "RTCOPIED";
	String RTCOPYFAILED = "RTCOPYFAILED";
	String RTCOPYSENT2CEN = "COPYSENT2CEN";
	String RTCOPYSENT2CENFAILED = "RTCOPYSENT2CENFAILED";
	String RTCOPYCONTENTFOUND = "RTCOPYCONTENTFOUND";
	String RTCOPYCONTENTMISSING = "RTCOPYCONTENTMISSING";
	String RTCOPYCONTENTFAILED = "RTCOPYCONTENTFAILED";
	String RTCOPYCONTENTERROR = "RTCOPYCONTENTERROR";
	String RTCOPYTRANSFERRED = "RTCOPYTRANSFERRED";
	String RTCOPYTRANSFERFAILED = "RTCOPYTRANSFERFAILED";
	public enum ResponseEnum {
	    SUCCESS,
	    FAILURE,
	    RETRY
	}

	protected RTCopyProcessor(RBTGatherer m_gathererThread) throws Exception {
		logger.info("Sree::.....");
		m_parentGathererThread = m_gathererThread;
		if (init())
			logger.info("RBT::inited");
		// start();
		else
			throw new Exception(" In RBTRTCopyProcessor: Cannot init Parameters");
	}

	public boolean init() {
		logger.info("Entering");
		rbtConnector=RBTConnector.getInstance();
		rbtDBManager = RBTDBManager.getInstance();
		
		
//		initRedirectNationalUrl();
//		initNonOnmobileUrl();
//		initRDCUrl();
//		initRTUrl();
		m_rtOperatorPrefixes = initializeTypePrefix("RT_OPERATOR_PREFIX");
		m_rtNonOnmobilePrefix = initializeTypePrefix("RT_NON_ONMOBILE_PREFIX");
		m_rtCrossOperatorPrefix = initializeTypePrefix("RT_CROSS_OPERATOR_PREFIX");
	
		initRTCopyAmountAndChargeClass();

		m_nextWakeUpTimeForTrans = getnexttime(m_runHourForTrans);
		if (getParamAsString("GATHERER_PATH") != null && getParamAsBoolean("RT_WRITE_TRANS", "FALSE")) {
			m_transDir = getParamAsString("GATHERER_PATH") + "/Trans/RTTrans";
			new File(m_transDir).mkdirs();
		}
		return true;
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
			if (m_trans_file != null && new File(m_trans_file + ".csv").isFile()) {
				fileName = m_trans_file;
				logger.info("*** RBT::writing RTCOPY Transaction file (append) : "	+ m_trans_file);
				m_transbufferWriter = new BufferedWriter(new FileWriter(fileName + ".csv", true));
			} else {
				fileName = m_transDir + File.separator + file_prefix + "_" + date;
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
							.write("CALLED,CALLER,SONG,CATEGORY,RT_COPY_TIME,CALLER_TYPE,CALLER_SUBSCRIBED_AT_COPY,RT_COPY_DONE,SMS_TYPE");
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
				// m_transbufferWriter.close();
				// m_transbufferWriter = null;
			} catch (Exception e) {
				logger.error("", e);
				e.printStackTrace();
				// m_transbufferWriter = null;
			}
		}

		try {
			logger.info("*** RBT::checking to create new Trans "
							+ new Date(System.currentTimeMillis() + 10000)
							+ " wakeUp " + new Date(m_nextWakeUpTimeForTrans));
			if ((System.currentTimeMillis() + 10000) >= m_nextWakeUpTimeForTrans) {
				m_trans_file = null;
				m_nextWakeUpTimeForTrans = getnexttime(m_runHourForTrans);
			}
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
		}
	}

	private synchronized void writeTrans(String subid, String callerID,
			String song, String cat, String req_time, String type,
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

	public void run() {
		logger.info("Entering");

		makeThreads();
		while (m_parentGathererThread.isAlive()) {
			try {
				logger.info("Entering while loop.");
				checkThreads();
				if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE"))
					openTrans();
				synchronized (m_pendingRTCopy) {
					if (m_pendingRTCopy.size() == 0)
						processRTCopyBulk();
				}
				if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE"))
					closeTrans();
			} catch (Throwable e) {
				logger.error("", e);
			}
			try {
				Date next_run_time = m_parentGathererThread.roundToNearestInterVal(getParamAsInt("GATHERER_SLEEP_INTERVAL", 5));
				long sleeptime = m_parentGathererThread
						.getSleepTime(next_run_time);
				if (sleeptime < 100)
					sleeptime = 500;
				logger.info(_class
						+ " Thread : sleeping for " + sleeptime + " mSecs.");
				Thread.sleep(sleeptime);
				logger.info(_class
						+ " Thread : waking up.");
				// Thread.sleep(sleeptime);
			} catch (Throwable E) {
				logger.error("", E);
			}
		}
		logger.info("Exiting");
	}

	private boolean isNonRTCopyContent(String clipID, String catID,	Clip clip, int status, String wavFile) {
		logger.info("Entered with params: clipID = "
				+ clipID + ", catID = " + catID + ", status = " + status
				+ ", wavFile = " + wavFile + ", clip = " + clip);
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String currentTime = sdf.format(currentDate);

		if ((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1)) {

			logger.info("Default content/clip id is null");
			return true;

		}
		if (wavFile != null && wavFile.equalsIgnoreCase("MISSING")) {
			logger.info("Missing content.");

			return true;
		}
		if (clipID.toUpperCase().indexOf("DEFAULT_" + currentTime) != -1) {
			logger.info("Polling RBT.");
			return true;
		}
		if ((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1) && !(getParamAsBoolean("RT_COPY_DEFAULT", "FALSE"))) {
			logger.info("default song copy failed");
			return true;
		}
		if (clip == null && status != 90) {
			logger.info("Clip is null for wavFile "
					+ wavFile);
			return true;
		}

		// songStatus == 90 || songStatus == 99 || songStatus == 0 ||
		// categoryType == 0 || categoryType == 4 || categoryType==10 ||
		// categoryType == 12

		if (status == 90) {

			logger.info("cricket rbt");
			return true;
		}

		if (status == 99) {

			logger.info("profile rbt");
			return true;
		}

		if (Arrays.asList(getParamAsString("GATHERER", "RT_COPY_BLOCKED_CATEGORY_IDS", " ").split(",")).contains(catID)) 
		{
			logger.info("category in blocked categories");
			return true;
		}
		if (clip != null && Arrays.asList(getParamAsString("GATHERER", "RT_COPY_BLOCKED_CLIP_IDS", " ").split(",")).contains("" + clip.getClipId())) 
		{
			logger.info("category in blocked clips");
			return true;
		} else if (clip != null && clip.getClipEndTime() != null && clip.getClipEndTime().getTime() < System.currentTimeMillis()) {
			logger.info("clip expired");
			return true;
		} else if (status != 1 && status != 80) {
			logger.info("status is " + status);
			return true;
		}
		return false;
	}

	public boolean isOperatorPrefix(String caller) {
		return isValidTypePrefix(caller, m_rtOperatorPrefixes);
	}

	private void sendSMS(String subscriber, String sms) {
		try {
			if (sms != null)
				Tools.sendSMS(getParamAsString("GATHERER","RT_SENDER_NO", "123456"), subscriber, sms, false);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

/*  private void initCategories() {
		String method = "initCategories";
		logger.info("Entering");
		m_categories = new HashMap();
		Categories[] cat = rbtDBManager.getAllCategories();
		if (cat != null)
			logger.info("Number of categories = "
					+ cat.length);
		if (cat != null && cat.length > 0) {
			for (int i = 0; i < cat.length; i++)
				m_categories.put("" + cat[i].id(), cat[i].name() + ","
						+ cat[i].classType() + "," + cat[i].type());
		}
		logger.info("Exiting. m_categories is "
				+ m_categories);
	}
*/
	private void initRTCopyAmountAndChargeClass() {
		logger.info("Entering");
	
		Category category = rbtConnector.getMemCache().getCategory(26);
//			rbtCacheManager.getCategory(26);
		if(category == null)
			return;
		String amt = null;
	
		ChargeClass chargeClass = rbtConnector.getRbtGenericCache().getChargeClass(category.getClassType());
//			CacheManagerUtil.getChargeClassCacheManager().getChargeClass(category.getClassType());
		if(chargeClass != null)
		{
			amt = chargeClass.getAmount();
			m_copyClassType = category.getClassType();
		}
		else
		{
			logger.info("chargeClass is null for classType >"+category.getClassType());
		}
		try 
		{
			copyAmount = Integer.parseInt(amt.trim());
		}
		catch (Exception e) 
		{
			copyAmount = 10;
		}
		logger.info("Exiting. m_copyClassType = "
				+ m_copyClassType + ", copyAmount = " + copyAmount);
	}

	private boolean isInvalidRTCopy(ViralSMSTable vst) {
		boolean isInvalid = false;
		if (vst.callerID() == null || subID(vst.callerID()) == null
				|| subID(vst.callerID()).equalsIgnoreCase(vst.subID())
				|| subID(vst.callerID()).length() < 7
				|| subID(vst.callerID()).length() < 7)
			isInvalid = true;

		else if (getParamAsBoolean("SHOW_BLACKLIST_TYPE", "FALSE")
				& rbtDBManager.isTotalBlackListSub(vst.callerID()))
			isInvalid = true;
		else if (getParamAsBoolean("SMS", "CORP_SELECTION_BLOCK", "FALSE") && isCorpSub(vst.callerID())) {
			isInvalid = true;
		}
		logger.info("For subscriber "
				+ vst.callerID() + ", isInvalidRTCopy is " + isInvalid);
		return isInvalid;
	}

	public String[] parseText(String s) {
		// //logger.info("****** parameters are -- "+s);
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

	private Hashtable initializeTypePrefix(String paramName) {

		logger.info("Entering with paramName" + paramName);
		String prefixTempStr = null;
		int iPrefixLen = -1;
		ArrayList prefixAList = null;
		String typePrefixes = getParamAsString(paramName);
		Hashtable returnTable = null;
		if (typePrefixes != null && typePrefixes.length() > 0) {
			StringTokenizer stk = new StringTokenizer(typePrefixes, ",");
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

		logger.info(" with param as " + paramName
				+ " returning Table as " + returnTable);
		return returnTable;
	}

/*	public void initRedirectNationalUrl() {
		String method = "initRedirectNationalUrl";
		logger.info("Entering");
		String strredirectnatDetails = getParamAsString("REDIRECT_NATIONAL");
		if (strredirectnatDetails != null && strredirectnatDetails.length() > 0) {
			StringTokenizer stk = new StringTokenizer(strredirectnatDetails,",");
			if (stk.hasMoreTokens()) {
				String token = stk.nextToken();
				m_redirectNational = (token.equalsIgnoreCase("true") || token
						.equalsIgnoreCase("on"));
			}
			if (stk.hasMoreTokens())
				m_redirectNationalURL = stk.nextToken();
			if (stk.hasMoreTokens())
				useProxyForNational = stk.nextToken().trim().equalsIgnoreCase(
						"true");
			if (stk.hasMoreTokens())
				proxyHostForNational = stk.nextToken().trim();
			try {
				if (stk.hasMoreTokens())
					proxyPortForNational = Integer.parseInt(stk.nextToken()
							.trim());
			} catch (Exception e) {
				proxyPortForNational = -1;
			}
			logger.info("m_redirectNational = "
					+ m_redirectNational);
			logger.info("m_redirectNationalURL = "
					+ m_redirectNationalURL);
			logger.info("useProxyForNational = "
					+ useProxyForNational);
			logger.info("proxyHostForNational = "
					+ proxyHostForNational);
			logger.info("proxyPortForNational = "
					+ proxyPortForNational);
		}
		logger.info("Exiting");
	}

	public void initNonOnmobileUrl() {
		String method = "initNonOnmobileUrl";
		logger.info("Entering");
		String strNonOnmobileUrlDetails = getParamAsString("RT_NON_ONMOBILE_URL");
		if (strNonOnmobileUrlDetails != null && strNonOnmobileUrlDetails.length() > 0) {
			StringTokenizer stk = new StringTokenizer(strNonOnmobileUrlDetails,	",");
			if (stk.hasMoreTokens())
				m_NonOnmobileURL = stk.nextToken().trim();
			if (stk.hasMoreTokens())
				useProxyForNonOnmobile = stk.nextToken().trim()
						.equalsIgnoreCase("true");
			if (stk.hasMoreTokens())
				proxyHostForNonOnmobile = stk.nextToken().trim();
			try {
				if (stk.hasMoreTokens())
					proxyPortForNonOnmobile = Integer.parseInt(stk.nextToken()
							.trim());
			} catch (Exception e) {
				proxyPortForNonOnmobile = -1;
			}
			logger.info("m_NonOnmobileURL = "
					+ m_NonOnmobileURL);
			logger.info("useProxyForNonOnmobile = "
					+ useProxyForNonOnmobile);
			logger.info("proxyHostForNonOnmobile = "
					+ proxyHostForNonOnmobile);
			logger.info("proxyPortForNonOnmobile = "
					+ proxyPortForNonOnmobile);
		}
		logger.info("Exiting");
	}

	public void initRDCUrl() {
		String method = "initRDCUrl";
		logger.info("Entering");
		String strRDCDetails = getParamAsString("RT_RDC_URL");
		if (strRDCDetails != null && strRDCDetails.length() > 0) {
			StringTokenizer stk = new StringTokenizer(strRDCDetails, ",");
			if (stk.hasMoreTokens()) {
				String token = stk.nextToken();
				m_isCentralSite = (token.equalsIgnoreCase("true") || token
						.equalsIgnoreCase("on"));
			}
			if (stk.hasMoreTokens())
				m_rdcUrl = stk.nextToken().trim();
			if (stk.hasMoreTokens())
				useProxyForRDC = stk.nextToken().trim()
						.equalsIgnoreCase("true");
			if (stk.hasMoreTokens())
				proxyHostForRDC = stk.nextToken().trim();
			try {
				if (stk.hasMoreTokens())
					proxyPortForRDC = Integer.parseInt(stk.nextToken().trim());
			} catch (Exception e) {
				proxyPortForRDC = -1;
			}
			logger.info("m_isCentralSite = "
					+ m_isCentralSite);
			logger.info("m_rdcUrl = " + m_rdcUrl);
			logger.info("useProxyForRDC = "
					+ useProxyForRDC);
			logger.info("proxyHostForRDC = "
					+ proxyHostForRDC);
			logger.info("proxyPortForRDC = "
					+ proxyPortForRDC);
		}
		logger.info("Exiting");
	}
*/
/*	public void initRTUrl() {
		String method = "initRTUrl";
		logger.info("Entering");
		String strRTDetails = getParamAsString("RT_COPY_URL");
		if (strRTDetails != null && strRTDetails.length() > 0) {
			StringTokenizer stk = new StringTokenizer(strRTDetails, ",");

			if (stk.hasMoreTokens())
				m_redirectRTURL = stk.nextToken().trim();
			if (stk.hasMoreTokens())
				useProxyForRT = stk.nextToken().trim().equalsIgnoreCase("true");
			if (stk.hasMoreTokens())
				proxyHostForRT = stk.nextToken().trim();
			try {
				if (stk.hasMoreTokens())
					proxyPortForRT = Integer.parseInt(stk.nextToken().trim());
			} catch (Exception e) {
				proxyPortForRT = -1;
			}
			logger.info("useProxyForRT = " + useProxyForRT);
			logger.info("proxyHostForRT = " + proxyHostForRT);
			logger.info("proxyPortForRT = "	+ proxyPortForRT);
		}
		logger.info("Exiting");
	}
*/
	public boolean isValidTypePrefix(String subscriberID, Hashtable m_prefixMap) {
		logger.info("Entering with subscriberID = "
				+ subscriberID + " | and m_prefixMap = " + m_prefixMap);
		if (subscriberID == null || subscriberID.length() < 7
				|| subscriberID.length() > 15 || m_prefixMap == null
				|| m_prefixMap.size() <= 0)
			return false;
		else {
			try {
				Long.parseLong(subID(subscriberID));
			} catch (Throwable e) {
				logger.error("", e);
				return false;
			}
		}

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
				logger.info("RBT:prefix true");
				return true;
			}
		}
		logger.info("RBT:prefix false");
		return false;
	}

	public String subID(String strSubID) {
		return rbtDBManager.subID(strSubID);
	}

	public boolean isValidSub(String strSubID) {
		return rbtDBManager.isValidPrefix(strSubID);
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
		return rbtDBManager.getViralSMSByTypeAndLimit(type, count);
	}

	private void removeViralPromotion(String subscriberID, String callerID,
			Date sentTime, String type) {
		rbtConnector.getSubscriberRbtclient().removeViralData(subscriberID, callerID ,type,sentTime);
//		rbtDBManager.removeViralPromotion(subscriberID, callerID, sentTime,
//				type);
	}

	private void updateViralPromotion(String subscriberID, String callerID,
			Date sentTime, String fType, String tType, String extraInfo) {
		rbtConnector.getSubscriberRbtclient().updateViralData(subscriberID, callerID,
				null, sentTime, fType, tType,null,null, extraInfo);
//		rbtDBManager.updateViralPromotion(subscriberID, callerID, sentTime,
//				fType, tType, new Date(System.currentTimeMillis()), null, null);
	}

	private void updateViralPromotion(String subscriberID, String callerID,
			Date sentTime, String fType, String tType, String clipId, String extraInfo) {
		rbtConnector.getSubscriberRbtclient().updateViralData(subscriberID, callerID,
				null, sentTime, fType, tType,clipId,null, extraInfo);
//		rbtDBManager.updateViralPromotion(subscriberID, callerID, sentTime,
//				fType, tType, clipId, null);
	}

	private void setSearchCountRTCopy(String strSubID, int count, String type,
			Date sent, String callerID) {
		logger.info("tryCount=" + count);
		rbtDBManager.setSearchCountRTCopy(strSubID,
				type, count, sent, callerID);
	}

	private ResponseEnum processNonLocalRTCopy(ViralSMSTable vst, String subType) 
	{
		logger.info("subType " + subType + " and subID "
				+ vst.subID() + " and callerID " + vst.callerID()
				+ " and clipID " + vst.clipID() + " and sent time "
				+ vst.sentTime());
		String caller = subID(vst.callerID());
		String called = vst.subID();
		Date sentTime = vst.sentTime();
		String selBy = vst.selectedBy();
		String copyTime = Tools.getFormattedDate(sentTime, "yyyy-MM-dd HH:mm:ss");
		String extraInfoStr = vst.extraInfo();
		HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
		String keypressed = null;
		if(extraInfoMap != null && extraInfoMap.containsKey(KEYPRESSED_ATTR))
			keypressed = extraInfoMap.get(KEYPRESSED_ATTR);
		String url = null;
		boolean useProxy = false;
		String proxyHost = null;
		int proxyPort = -1;
		List<String> successCodes = tokenizeArrayList(getParamAsString("GATHERER" , "NON_LOCAL_COPY_SUCCESS_CODES","0,2,3,4,5,6,7,8,9"), ",");
		List<String> retryCodes = tokenizeArrayList(getParamAsString("GATHERER" , "NON_LOCAL_COPY_RETRY_CODES",null), ",");

		if (subType.equalsIgnoreCase(m_nonOnmobileType))
		{
			String strNonOnmobileUrlDetails = getParamAsString("RT_NON_ONMOBILE_URL");
			if (strNonOnmobileUrlDetails != null && strNonOnmobileUrlDetails.length() > 0) 
			{
				StringTokenizer stk = new StringTokenizer(strNonOnmobileUrlDetails,	",");
				if (stk.hasMoreTokens())
					url = stk.nextToken().trim();
				if (stk.hasMoreTokens())
					useProxy = stk.nextToken().trim()	.equalsIgnoreCase("true");
				if (stk.hasMoreTokens())
					proxyHost = stk.nextToken().trim();
				try {
					if (stk.hasMoreTokens())
						proxyPort = Integer.parseInt(stk.nextToken().trim());
				} catch (Exception e) {
					proxyPort = -1;
				}
				
				String vcode = vst.clipID();
				if(vcode != null && vcode.indexOf(":") != -1)
					vcode = vcode.substring(0,vcode.indexOf(":"));
				url = url + "startcopy.jsp?called="+ vst.subID() + "&caller=" + vst.callerID()+"&clip_id=" + vcode + "&sms_type="+m_sms_type;
				
				logger.info("url for non onmobile type is " + url);
			}	
			else
				logger.info("RT_NON_ONMOBILE_URL param is NULL.. Populate the param in DB");
			
		}
		else if(subType.equalsIgnoreCase(m_crossOperatorType))
		{
			boolean isCentralSite = false;
			
			String strRDCDetails = getParamAsString("RT_RDC_URL");
			if(strRDCDetails == null)
			{
				logger.info("RT_RDC_URL param is NULL.. Populate the param in DB");
				isCentralSite = false;
			}
			else
			{
				StringTokenizer stk = new StringTokenizer(strRDCDetails, ",");
				if (stk.hasMoreTokens())
				{
					String token = stk.nextToken();
					isCentralSite = (token.equalsIgnoreCase("true") || token.equalsIgnoreCase("on"));
				}
				if(isCentralSite)
				{
					if (stk.hasMoreTokens())
						url = stk.nextToken().trim();
					if (stk.hasMoreTokens())
						useProxy = stk.nextToken().trim().equalsIgnoreCase("true");
					if (stk.hasMoreTokens())
						proxyHost = stk.nextToken().trim();
					try {
						if (stk.hasMoreTokens())
							proxyPort = Integer.parseInt(stk.nextToken().trim());
					} catch (Exception e) {
						proxyPort = -1;
					}
					
					String wavFile = null;
					if (vst.clipID() != null)
						wavFile = new StringTokenizer(vst.clipID(), ":").nextToken()
								.trim();
					Clip clip = null;
					if (wavFile != null)
						clip = getClipRBT(wavFile);
					int clipID = -1;
					if (clip != null)
						clipID = clip.getClipId();

					url = url + "rbt_cross_copy.jsp?subscriber_id=" + vst.subID()
							+ "&caller_id=" + vst.callerID() + "&clip_id=" + clipID
							+ "&sms_type=" + m_sms_type;

					logger.info(" Final url for RDC is " + url);
				}
			}
			
			if(!isCentralSite)
			{
				String strredirectnatDetails = getParamAsString("REDIRECT_NATIONAL");
				if (strredirectnatDetails != null && strredirectnatDetails.length() > 0)
				{
					StringTokenizer stk = new StringTokenizer(strredirectnatDetails,",");
					if (stk.hasMoreTokens()) {
						String token = stk.nextToken();
						m_redirectNational = (token.equalsIgnoreCase("true") || token.equalsIgnoreCase("on"));
					}
					if (stk.hasMoreTokens())
						url = stk.nextToken();
					if (stk.hasMoreTokens())
						useProxy = stk.nextToken().trim().equalsIgnoreCase("true");
					if (stk.hasMoreTokens())
						proxyHost = stk.nextToken().trim();
					try {
						if (stk.hasMoreTokens())
							proxyPort = Integer.parseInt(stk.nextToken().trim());
					} catch (Exception e) {
						proxyPort = -1;
					}
					url = Tools.findNReplaceAll(url, "rbt_sms.jsp", "");
					url = Tools.findNReplaceAll(url, "?", "");
					
					url = url + "rbt_cross_copy.jsp?subscriber_id=" + vst.subID()+ "&caller_id=" + vst.callerID() + "&clip_id="	+ vst.clipID() + "&sms_type=" + m_sms_type;
					logger.info("url for cross operator non central type " + url);
				}
				else
				{
					logger.info("REDIRECT_NATIONAL param is NULL.. Populate the param in DB");
				}
			}
		}
		if(keypressed != null && url != null && url.indexOf("<keypressed>") != -1)
			url = url.replaceAll("<keypressed>", keypressed);
		
		Integer statusInt = new Integer(-1);
		StringBuffer result = new StringBuffer();
		boolean success = false;

		success = Tools.callURL(url, statusInt, result, useProxy, proxyHost,
				proxyPort);
		String response = result.toString().trim();
		if (response != null
				&& (response.indexOf("SUCCESS") != -1 || response
						.indexOf("SUCESS") != -1)) {
			logger.info("RTCopy successful for the following url " + url);
			return ResponseEnum.SUCCESS;
		}
		if (response != null && response.length() != -1
				&& successCodes!=null && successCodes.contains(response)) {
			logger.info("Non-onmobile copy successful for the following url "
					+ url);
			return ResponseEnum.SUCCESS;
		}else if(response != null && response.length() != -1
				&& retryCodes!=null && retryCodes.contains(response)){
			return ResponseEnum.RETRY;
		}else {
			logger.info("RTCopy unsuccessful for the following url " + url);
			return ResponseEnum.FAILURE;
		}
		
	}

	private void copyTestFailed(ViralSMSTable vst, String subType) {
		logger.info("Entered with subType = " + subType
				+ ", for subscriber = " + vst.callerID());
		if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE")) {
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					RTCOPY);
			writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-", Tools
					.getFormattedDate(vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
					subType, "-", "RTCOPYTESTFAILED");
		} else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					RTCOPY, "RTCOPYTESTFAILED", null);

	}

	private void copyFailed(ViralSMSTable vst, String reason) {
		logger.info("Entered with reason = " + reason
				+ ", for subscriber = " + vst.callerID());
		if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE")) {
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					RTCOPY);
			writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-", Tools
					.getFormattedDate(vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
					m_localType, "-", reason);
		} else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					RTCOPY, reason, null);

	}

	private ResponseEnum processLocalRTCopyRequest(ViralSMSTable vst) {

		String clipID = vst.clipID();
		String extraInfoStr = vst.extraInfo();
		HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
		String keypressed = null;
		List<String> successCodes = tokenizeArrayList(getParamAsString("GATHERER" , "NON_LOCAL_COPY_SUCCESS_CODES","0,2,3,4,5,6,7,8,9"), ",");
		List<String> retryCodes = tokenizeArrayList(getParamAsString("GATHERER" , "NON_LOCAL_COPY_RETRY_CODES",null), ",");
		if(extraInfoMap != null && extraInfoMap.containsKey(KEYPRESSED_ATTR))
			keypressed = extraInfoMap.get(KEYPRESSED_ATTR);
		logger.info("entered for " + vst.callerID()
				+ vst.subID()+" and keypressed "+keypressed);
		Date sentTime = vst.sentTime();
		String selectedBy = vst.selectedBy();
		int retryCountLimit =  getParamAsInt("RETRY_MAX_LIMIT", 3) ;
		Date currentDate = null;
		if (clipID != null && clipID.toUpperCase().indexOf("DEFAULT_") != -1
				&& clipID.length() > 16) {
			try {
				String currentTime = clipID.substring(8, 16);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				try {
					currentDate = sdf.parse(currentTime);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			} catch (Exception e) {

			}
		}
		if (selectedBy != null && !selectedBy.equalsIgnoreCase("null"))
			selectedBy = selectedBy.trim().toUpperCase();
		else
			selectedBy = "PRESSHASH";
		String copyTime = Tools.getFormattedDate(sentTime,
				"yyyy-MM-dd HH:mm:ss");

		int status = 1;
		String wavFile = null;
		StringBuffer wavFileBuf = new StringBuffer();
		StringBuffer catTokenBuf = new StringBuffer();
		StringBuffer catNameBuf = new StringBuffer();
		StringBuffer classTypeBuffer = new StringBuffer();
		StringBuffer statusBuf = new StringBuffer();
		StringBuffer setForCallerBuf = new StringBuffer();
		String songName = null;
		if (isInvalidRTCopy(vst)) {
			copyFailed(vst, "INVALIDCOPY");
			return ResponseEnum.FAILURE;
		}

		int cat = 26;

		if (clipID != null)
			cat = getClipCopyDetails(clipID, wavFileBuf, catTokenBuf,
					catNameBuf, classTypeBuffer, statusBuf, setForCallerBuf);

		if (clipID != null && clipID.toUpperCase().indexOf("DEFAULT_") != -1
				&& currentDate != null) // this indicates poll
		{
			copyFailed(vst, "INVALIDCOPYPOLL"); // needs to be intimated.
			return ResponseEnum.FAILURE;
		}

		Clip clip = null;

		logger.info("clipID is " + clipID);
		logger.info("wavFileBuf is " + wavFileBuf);
		logger.info("catTokenBuf is " + catTokenBuf);
		logger.info("catNameBuf is " + catNameBuf);
		logger.info("classTypeBuffer is "
				+ classTypeBuffer);
		logger.info("statusBuf is " + statusBuf);
		logger.info("setForCallerBuf is "
				+ setForCallerBuf);

		try {
			status = Integer.parseInt(statusBuf.toString().trim());
		} catch (Exception e) {
			// logger.error("", e);
			status = 1;
		}
		wavFile = wavFileBuf.toString().trim();

		if (wavFile != null && wavFile.length() > 0 && status != 90
				&& status != 99 && status != 80)
			clip = getClipRBT(wavFile);

		boolean isNonRTCopyContent = isNonRTCopyContent(clipID, catTokenBuf
				.toString(), clip, status, wavFile);
		if (isNonRTCopyContent && !getParamAsBoolean("RT_TRANSFER_COPY_CONTENT_MISSING", "FALSE")) {
			logger.info("isNonRTCopyContent is true");
			copyFailed(vst, "NONRTCOPY");
			return ResponseEnum.FAILURE;
		}

		if (clip != null)
			songName = clip.getClipName();


		int finalClipID = -1;
		if (clip != null)
			finalClipID = clip.getClipId();

		
		String finalClip = "" + finalClipID;

		if (isNonRTCopyContent && getParamAsBoolean("RT_TRANSFER_COPY_CONTENT_MISSING", "FALSE")) {

			finalClip = "MISSING:" + finalClipID;
			logger.info(" final clip id is " + finalClip);
		}


		String url = null;
		boolean useProxy = false;
		String proxyHost = null;
		int proxyPort = 80;
		
		String strRTDetails = getParamAsString("RT_COPY_URL");
		if (strRTDetails != null && strRTDetails.length() > 0) {
			StringTokenizer stk = new StringTokenizer(strRTDetails, ",");

			if (stk.hasMoreTokens())
				url = stk.nextToken().trim();
			if (stk.hasMoreTokens())
				useProxy = stk.nextToken().trim().equalsIgnoreCase("true");
			if (stk.hasMoreTokens())
				proxyHost = stk.nextToken().trim();
			try {
				if (stk.hasMoreTokens())
					proxyPort = Integer.parseInt(stk.nextToken().trim());
			} catch (Exception e) {
				proxyPort = -1;
			}
			logger.info("useProxyForRT = " + url);
			logger.info("proxyHostForRT = " + proxyHost);
			logger.info("proxyPortForRT = "	+ proxyPort);
		}
		
		url = url + "msisdn=" + vst.callerID()
		+ "&rbtid=" + finalClip ;
		if(keypressed != null && url != null && url.indexOf("<keypressed>") != -1)
			url = url.replaceAll("<keypressed>", keypressed);
		
		String vcode=null;
		if (clip!=null && getParamAsBoolean("USE_VCODE_FOR_RTCOPY", "TRUE")){
			
			String clipName=clip.getClipRbtWavFile();
			vcode=clipName.substring(clipName.indexOf("rbt_")+4, clipName.indexOf("_rbt"));
			url=url +"&rbtcode=" + vcode;
		}
		


		//url = url + "?SUB_ID=" + vst.subID() + "&CALLER_ID=" + vst.callerID()
			//	+ "&CLIP_ID=" + finalClip;

		logger.info(" Final url for RT is " + url);

		logger.info("url is " + url);
		Integer statusInt = new Integer(-1);
		StringBuffer result = new StringBuffer();
		boolean success = Tools.callURL(url, statusInt, result, useProxy,
				proxyHost, proxyPort);
		String response = result.toString().trim();
		if (!isNonRTCopyContent) {
			if (response != null
					&& (response.indexOf("SUCCESS") != -1 || response
							.indexOf("SUCESS") != -1)) {

				if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE")) {
					removeViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY);
					writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf
							.toString(), Tools.getFormattedDate(vst.sentTime(),
							"yyyy-MM-dd HH:mm:ss"), m_localType, "TRUE",
							RTCOPIED);

				} else
					updateViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY, RTCOPIED, null);

				logger.info("RT Copy successful for the following url " + url);
				return ResponseEnum.SUCCESS;
			}
			if (response != null && response.length() != -1
					&& successCodes!=null && successCodes.contains(response)) {
				logger.info("RT copy successful for the following url "
								+ url);
				return ResponseEnum.SUCCESS;
			} else {
				if (retryCodes != null) {
					if (response != null && response.length() != -1
							&& successCodes != null
							&& successCodes.contains(response)
							&& vst.count() < retryCountLimit) {

						updateSearchCountAndTime(vst);

					}

				}else if (vst.count() < 3) {
					updateSearchCountAndTime(vst);
				} else if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE")) {
					removeViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY);
					writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
							Tools.getFormattedDate(vst.sentTime(),
									"yyyy-MM-dd HH:mm:ss"), "COUNT OVER", "-",
							RTCOPYFAILED);
				} else
					updateViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY, RTCOPYFAILED, null);
				logger.info("RT Copy unsuccessful for the following url " + url);
				return ResponseEnum.FAILURE;
			}
		} else {
			if (response != null
					&& (response.indexOf("SUCCESS") != -1 || response
							.indexOf("SUCESS") != -1)) {

				if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE")) {
					removeViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY);
					writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf
							.toString(), Tools.getFormattedDate(vst.sentTime(),
							"yyyy-MM-dd HH:mm:ss"), m_localType, "FALSE",
							RTCOPYCONTENTMISSING);

				} else
					updateViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY, RTCOPYCONTENTMISSING, null);

				logger.info("RT Copy successful for the following url " + url);
				return ResponseEnum.SUCCESS;
			}
			if (response != null && response.length() != -1
					&& successCodes!=null && successCodes.contains(response)) {
				logger.info("RT copy successful for the following url "
								+ url);
				return ResponseEnum.SUCCESS;
			} else {

				if (retryCodes != null) {
					if (response != null && response.length() != -1
							&& successCodes != null
							&& successCodes.contains(response)
							&& vst.count() < retryCountLimit) {

						updateSearchCountAndTime(vst);

					}

				} else if (vst.count() < 3) {
					updateSearchCountAndTime(vst);
				} else if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE")) {
					removeViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), RTCOPY);
					writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
							Tools.getFormattedDate(vst.sentTime(),
									"yyyy-MM-dd HH:mm:ss"), "COUNT OVER", "-",
							RTCOPYFAILED);
				} else
					updateViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), RTCOPY, RTCOPYFAILED, null);
				logger.info("RT Copy unsuccessful for the following url " + url);
				return ResponseEnum.FAILURE;
			}

		}

	}

	private void updateSearchCountAndTime(ViralSMSTable vst) {
		int attempts = vst.count() + 1;
		setSearchCountCopy(vst.subID(), attempts, RTCOPY,
				vst.sentTime(), vst.callerID());
	}

	private boolean isCorpSub(String strSubID) {
		return isSubAlreadyActiveOnStatus(strSubID, null, 0);
	}

	private boolean isSubAlreadyActiveOnStatus(String strSubID,
			String callerID, int status) {
		SubscriberStatus subStatus = rbtDBManager.getActiveSubscriberRecord(
				strSubID, callerID, status, 0, 2359);

		if (subStatus != null)
			return true;

		return false;
	}

//	public HashMap getExtraInfoMap(Subscriber subscriber) {
//
//		return rbtDBManager.getExtraInfoMap(subscriber);
//	}

	private Subscriber getSubscriber(String strSubID) {
		
		return rbtConnector.getSubscriberRbtclient().getSubscriber(strSubID, "GATHERER");
//		rbtDBManager.getSubscriber(strSubID);
	}

	private int getClipCopyDetails(String clipID, StringBuffer wavFileBuf,
			StringBuffer catTokenBuf, StringBuffer catNameBuf,
			StringBuffer classTypeBuffer, StringBuffer statusBuf,
			StringBuffer setForCallerbuf) {
		int cat = 26;
		StringTokenizer stk = new StringTokenizer(clipID, ":");
		if (stk.hasMoreTokens())
			wavFileBuf.append(stk.nextToken());
		if (stk.hasMoreTokens()) {
			String catToken = stk.nextToken();
			if (catToken.startsWith("S")) {
				catToken = catToken.substring(1);

				try {
					cat = Integer.parseInt(catToken);
				} catch (Exception e) {
					cat = 0;
				}
			}
			catTokenBuf.append(catToken);
		}
		if (stk.hasMoreTokens()) {
			StringTokenizer stkStatus = new StringTokenizer(stk.nextToken(),
					"|");
			if (stkStatus.hasMoreTokens())
				statusBuf.append(stkStatus.nextToken());
			if (stkStatus.hasMoreTokens())
				setForCallerbuf.append(stkStatus.nextToken());
		}
		return cat;
	}

	private Clip getClipRBT(String strWavFile) {
		//return rbtDBManager.getClipRBT(strWavFile);
		return rbtConnector.getMemCache().getClipByRbtWavFileName(strWavFile);
//		 rbtCacheManager.getClipByRbtWavFileName(strWavFile);
	}

	private boolean isSubActive(Subscriber sub){
		int rbtType = 0;
		int rbtTypeSub=0;
		if(sub!=null && sub.getUserType()!=null){
			try {
				rbtTypeSub=Integer.parseInt(sub.getUserType());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				rbtType=0;
				e.printStackTrace();
			}
		}
		rbtType=rbtTypeSub;
		if((rbtType == 0 && (rbtTypeSub == 0 || rbtTypeSub == 3)) || (rbtType == 2 && (rbtTypeSub == 2 || rbtTypeSub == 3 || rbtTypeSub == 4)) || (rbtType == 1 && (rbtTypeSub == 1 || rbtTypeSub == 4)))
    	{
			if(sub.getStatus().equalsIgnoreCase(WebServiceConstants.ACT_PENDING) || sub.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE) || sub.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED )|| sub.getStatus().equalsIgnoreCase(WebServiceConstants.GRACE)){
				return true;
			}
    	}
//		
//		rbtDBManager.isSubActive(sub);
		return false;
	}

	public boolean updateExtraInfoAndPlayerStatus(String subscriberId,
			boolean pollOn) {
		String requestMode="GATHERER";
		String actInfo="GATHERER";
		StringBuffer responseBuff=new StringBuffer();
		SubscriptionBean subBean=new SubscriptionBean();
		subBean.setSubId(subscriberId);
		subBean.setPollOn(pollOn);
		boolean responseStatus=false;
		rbtConnector.getSubscriberRbtclient().updateSubcriberInfo(subBean, responseBuff, requestMode, actInfo);
		if(responseBuff!=null && responseBuff.length()>0){
			String responseTemp=responseBuff.toString();
			if(responseTemp!=null ){
				responseStatus=true;
				responseTemp=responseTemp.trim();
				responseTemp=responseTemp.toUpperCase();
				if(responseTemp.indexOf("SUCCESS")!=-1){
					responseStatus=true;
				}
			}
		}
		return responseStatus;
//		return rbtDBManager.updateExtraInfoAndPlayerStatus(subscriberId, name,
//				value, playerStatus);
	}

	private SubscriberPromo getSubscriberPromo(String strSubID) {
		return rbtDBManager.getSubscriberPromo(strSubID, "YOUTHCARD");
	}

	public void processRTCopy(ViralSMSTable vst) {
		try {
			String caller = subID(vst.callerID());
			int tryCount = vst.count();
			int retryCountLimit =  getParamAsInt("RETRY_MAX_LIMIT", 3) ;
			logger.info("subscriber_id=" + vst.subID()
					+ "|caller_id=" + caller + "|clipID=" + vst.clipID()
					+ "|sentTime=" + vst.sentTime() + "|selBy="
					+ vst.selectedBy() + "|tryCount=" + vst.count());

			String subTypeRegion = "UNKNOWN";
			boolean copyProcessing = false;
			boolean copyTestFailed = false;
			ResponseEnum responseEnum = ResponseEnum.RETRY;
			if (caller.length() < getParamAsInt("PHONE_NUMBER_LENGTH_MIN",10)
					|| caller.length() > getParamAsInt("PHONE_NUMBER_LENGTH_MAX",10)) {
				removeViralPromotion(vst.subID(), vst.callerID(), vst
						.sentTime(), RTCOPY);
			}

			logger.info("operator prefixes "+ m_rtOperatorPrefixes);
			logger.info("crossoperator prefixes "+ m_rtCrossOperatorPrefix);
			
			if(isValidSub(caller))
			{
				logger.info(caller+" is Local sub.");
				subTypeRegion = m_localType;
				if (getParamAsBoolean("IS_LOCAL_RT_COPY_TEST_ON","FALSE") 
						&& !Arrays.asList(getParamAsString("GATHERER", "LOCAL_RT_COPY_TEST_NUMBERS", "").split(",")).contains(caller))
					copyTestFailed = true;
				else
					responseEnum=processLocalRTCopyRequest(vst);
			}
			else if (isValidTypePrefix(caller,m_rtOperatorPrefixes))
			{
				logger.info(caller+" is operator sub.");
				subTypeRegion = m_nationalType;
				if(getParamAsBoolean("IS_NATIONAL_RT_COPY_TEST_ON", "FALSE") 
						&& !Arrays.asList(getParamAsString("GATHERER", "NATIONAL_RT_COPY_TEST_NUMBERS", "").split(",")).contains(caller))
					copyTestFailed = true;
				else
					responseEnum = processLocalRTCopyRequest(vst);
			}
			else if (isValidTypePrefix(caller, m_rtNonOnmobilePrefix)) {
				logger.info(caller
						+ " is non_onmobile sub.");
				subTypeRegion = m_nonOnmobileType;
				if (getParamAsBoolean("IS_NON_ONMOBILE_RT_COPY_TEST_ON","FALSE") 
						&& !Arrays.asList(getParamAsString("GATHERER", "NON_ONMOBILE_RT_COPY_TEST_NUMBERS", "").split(",")).contains(caller))
					copyTestFailed = true;
				else
					responseEnum = processLocalRTCopyRequest(vst);
			} else if ((m_rtCrossOperatorPrefix == null
					|| m_rtCrossOperatorPrefix.size() == 0
					|| isValidTypePrefix(caller, m_rtCrossOperatorPrefix) || (getParamAsBoolean("IS_RT_CROSS_COPY_TEST_ON", "TRUE")
					&& Arrays.asList(getParamAsString("GATHERER", "CROSS_RT_COPY_TEST_NUMBERS", "").split(",")).contains(caller)))
					&& getParamAsBoolean("RT_COPY_CROSS_OPERATOR", "FALSE")) {
				logger.info(caller
						+ " is cross_operator sub.");
				subTypeRegion = m_crossOperatorType;
				responseEnum = processNonLocalRTCopy(vst, subTypeRegion);
			} else {
				logger.info(" m_rtCrossCopy "	+ getParamAsBoolean("RT_COPY_CROSS_OPERATOR", "FALSE"));
				logger.info(caller
						+ " is unrecognized sub.");
				// copyFailed(vst,COPYFAILED);
			}
			// if(copyTestFailed &&
			// !subTypeRegion.equalsIgnoreCase(m_localType))
			if (copyTestFailed) {
				copyTestFailed(vst, subTypeRegion);
				return;
			}
			if(responseEnum.equals(ResponseEnum.SUCCESS)){
				copyProcessing  = true ;	
			}
			
			if (copyProcessing) {
				if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE")) {
					removeViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY);
					writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
							Tools.getFormattedDate(vst.sentTime(),
									"yyyy-MM-dd HH:mm:ss"), subTypeRegion, "-",
							RTCOPIED);
				} else
					updateViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY, RTCOPIED, null);
			} else if (!subTypeRegion.equalsIgnoreCase(m_localType) && !subTypeRegion.equalsIgnoreCase(m_nationalType)) {
				if (responseEnum.equals(ResponseEnum.RETRY) && tryCount < retryCountLimit && !subTypeRegion.equalsIgnoreCase("UNKNOWN"))
					setSearchCountRTCopy(vst.subID(), ++tryCount, RTCOPY, vst
							.sentTime(), vst.callerID());
				else if (getParamAsBoolean("RT_WRITE_TRANS", "FALSE")) {
					removeViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY);
					writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
							Tools.getFormattedDate(vst.sentTime(),
									"yyyy-MM-dd HH:mm:ss"), subTypeRegion, "-",
							RTCOPYFAILED);
				} else
					updateViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), RTCOPY, RTCOPYFAILED, null);
			}

		} catch (Throwable e) {
			logger.error("", e);
		}
	}

	private void processRTCopyBulk() {
		logger.info("Entering");
		ViralSMSTable[] context = getViralSMSTableLimit("RTCOPY",getParamAsInt("RT_COPY_PROCESSING_COUNT", 5000));
		if (context == null || context.length <= 0) {
			logger.info("Context is null or count <= 0");
			return;
		}
		logger.info("Count of rtCopyContext is "+ context.length);
		ArrayList subs = new ArrayList();
		for (int i = 0; i < context.length; i++) {
			if (!subs.contains(context[i].callerID())) {
				subs.add(context[i].callerID());
				m_pendingRTCopy.add(context[i]);
				m_pendingRTCopy.notify();
			}
		}
	}

	private String getSubstituedSMS(String smsText, String str1, String str2) {
		if (smsText == null)
			return null;
		if (str2 == null) {
			if (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
		} else {
			while (smsText.indexOf("%S") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
						+ smsText.substring(smsText.indexOf("%S") + 2);
			}
			while (smsText.indexOf("%C") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
						+ smsText.substring(smsText.indexOf("%C") + 2);
			}
		}

		return smsText;
	}

	private void checkThreads() {
		String method = "checkThreads";
		logger.info("Entering " + method
				+ " with pool size = " + getParamAsInt("RT_COPY_THREAD_POOL_SIZE",1));
		for (int i = 0; i < m_rtCopyThreadPool.size(); i++) {
			RTCopyThread tempThread = (RTCopyThread) m_rtCopyThreadPool.get(i);
			logger.info("Got copy thread " + tempThread);
			if (tempThread == null || !tempThread.isAlive()) {
				tempThread = new RTCopyThread(m_parentGathererThread);
				tempThread.start();
				m_rtCopyThreadPool.set(i, tempThread);
				logger.info("Created copy thread "
						+ tempThread);
			}
		}
	}

	private void makeThreads() {
		String method = "makeThreads";
		logger.info("Entering " + method+ " with copy size = " + getParamAsInt("RT_COPY_THREAD_POOL_SIZE",1));
		
		for (int i = 0; i < getParamAsInt("RT_COPY_THREAD_POOL_SIZE",1); i++) {
			RTCopyThread tempThread = new RTCopyThread(m_parentGathererThread);
			tempThread.start();
			m_rtCopyThreadPool.add(tempThread);
			logger.info("Created copy thread "
							+ tempThread);
		}

	}

	private void setSearchCountCopy(String strSubID, int count, String type,
			Date sent, String callerID) {
		logger.info("tryCount=" + count);
		rbtDBManager.setSearchCountCopy(strSubID, type,
				count, sent, callerID);
	}
	
	private String getParamAsString(String param)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, null);
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return null;
		}
	}

	private String getParamAsString(String type, String param, String defaultValue)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defaultValue);
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultValue;
		}
	}

	private int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}

	private int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal;
		}
	}

	private boolean getParamAsBoolean(String param, String defaultVal)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal).equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
	private boolean getParamAsBoolean(String type, String param, String defaultVal)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal).equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
	
	public static ArrayList<String> tokenizeArrayList(String stringToTokenize,
			String delimiter) {
		if (stringToTokenize == null)
			return null;
		String delimiterUsed = ",";

		if (delimiter != null)
			delimiterUsed = delimiter;

		ArrayList<String> result = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(stringToTokenize,
				delimiterUsed);
		while (tokens.hasMoreTokens())
			result.add(tokens.nextToken().toLowerCase());

		return result;
	}
	
}
