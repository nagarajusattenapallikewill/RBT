package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RetryableException;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.common.CurrencyUtil;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.TransFileWriter;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.eventlogging.EventLogger;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.hunterFramework.management.HttpPerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitorFactory;
import com.onmobile.apps.ringbacktones.logger.CopyTransLogger;
import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Cos;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Feed;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.PPLContentRejectionLogger;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.SelectionRequestBean;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.SubscriptionBean;
import com.onmobile.reporting.framework.capture.api.Configuration;
import com.onmobile.reporting.framework.capture.api.ReportingException;

public class Utility implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(Utility.class);
	
	static RBTConnector rbtConnector = null; 
	static public RBTDBManager rbtDBManager = null;
	static String m_copyClassType = null;
	static int copyAmount = 10;
	static String defaultClipWavName = null;
	static private EventLogger eventLogger = null;
	static private String m_eventLoggingDir = "./EventLogs";
	static HashMap<String, String> copyChargeClassMap = new HashMap<String, String>();
	static HashMap<String, String> m_RdcNovaOperatorMap = new HashMap<String, String>();
	
	static boolean m_redirectNational = false;
	static String m_nationalUrl = null;
	static boolean m_nationalUseProxy = false;
	static String m_nationalProxyHost = null;
	static int m_nationalProxyPort = -1;

	static boolean m_redirectBuddyNet = false;
	static String m_buddynetUrl = null;
	static boolean m_buddynetUseProxy = false;
	static String m_buddynetProxyHost = null;
	static int m_buddynetProxyPort = -1;

	static boolean m_redirectNonOnmobile = false;
	static String m_nonOnmobileUrl = null;
	static boolean m_nonOnmobileUseProxy = false;
	static String m_nonOnmobileProxyHost = null;
	static int m_nonOnmobileProxyPort = -1;

	static boolean m_redirectCrossOperator = false;
	static String m_crossOperatorUrl = null;
	static boolean m_crossOperatorUseProxy = false;
	static String m_crossOperatorProxyHost = null;
	static int m_crossOperatorProxyPort = -1;
	static public Feed schedule = null;
	static private String m_transDir = "./Trans";
	static TransFileWriter copyTransactionWriter = null;
	static SimpleDateFormat statsDateFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss");
	static String m_localType = "INCIRCLE";
	static String m_virtualType = "VIRTUAL_NUMBER";
	static String m_nationalType= "OPERATOR";
	static String m_nonOnmobileType = "NON_ONMOBILE";
	static String m_crossOperatorType = "CROSS_OPERATOR";

	public static String COPY = "COPY";
	public static String COPIED = "COPIED";
	public static String COPYCONFIRM = "COPYCONFIRM";
	public static String COPYFAILED = "COPYFAILED";
	public static String COPYCONFIRMED = "COPYCONFIRMED";
	public static String COPYSTAR = "COPYSTAR";
	public static String COPYCONFPENDING = "COPYCONFPENDING";
	public static String COPYEXPIRED = "COPYEXPIRED";
	public static String DUPLICATE = "DUPLICATE";
	public static String RRBTCOPYFAILED = "RRBTCOPYFAILED";
	public static String RRBTCOPYREQUESTED = "RRBTCOPYREQUESTED";
	public static String PREMIUM_CONTENT = "PREMIUM_CONTENT";
	public static String LOCAL_OPERATOR = "LOCAL_OPERATOR";

	
	static String activeclassTypeParam = null;
	static String inactiveclassTypeParam = null;

	static String m_copySelSMS = "The selection %S copied from %C has been set as your RingBackTone";
	static String m_copyActSMS = "You will be activated on RingBackTones in the next 24 hrs. The selection %S copied from %C has been set as your RingBackTone";
	static String m_pressStarConfirmationSMS = "You have pressed star to copy the selection %S. If you don't want to copy send RBT CANCEL within %C min";
	static String m_optInConfirmationActSMS = "You have pressed star to copy the selection %S. If you want to copy send %RBT_CONFIRM within %C min. The Subscription charge is %ACT_AMT Rs. And Song Selection Charge is %SEL_AMT Rs";
	static String m_optInConfirmationSelSMS = "You have pressed star to copy the selection %S. If you want to copy send %RBT_CONFIRM within %C min. The Song Selection Charge is %SEL_AMT Rs";
	static String smsTextBlockCopyForShuffleSubscriber = "You have a shuffle as your present selection. So copy is not allowed";
	static String m_crossCopyContentMissingSmsText = "The song copied from subscriber %CALLED% belonging to a different operator is not available with this operator";
	static String m_nonCopyContentSMS = "Sorry this selection cannot be copied";
	static String m_nonCopyExpiredClipSMS = "Sorry this selection cannot be copied";
	static String m_corpCopyContentSMS = "Song Selection cannot be changed on your number";
	static String m_nonCopyNonCircleSMS = "RingBacktones can be copied only from your own circle";
	static String m_crossCopyNotSupportedSMS = "You are not allowed to use this service";
	static String m_optInSameSongSms="Hi! You have requested to set %S as your Caller tune bt this song is already active as your Caller tune. Hence your request is not being processed. Thank You!";

	static RBTHttpClient rbtHttpClient = null;
	
	public static String GATHERER = "GATHERER";
	public static String COMMON = "COMMON";
	public static String IS_OPT_IN = "IS_OPT_IN";
	public static String IS_STAR_OPT_IN_ALLOWED = "IS_STAR_OPT_IN_ALLOWED";
	public static String PRESS_STAR_DOUBLE_CONFIRMATION = "PRESS_STAR_DOUBLE_CONFIRMATION";
	public static String COPY_ONLINE_DIP = "COPY_ONLINE_DIP";
	static PPLContentRejectionLogger pplContentRejectionLogger = null;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	static URLCodec m_urlEncoder = new URLCodec();

	public static final String HunterNameDirectCopy = "DirectCopy"; 
	public static final String HunterNameOptinCopy  =  "OptinCopy";
	public static final String HunterNameRRBTCopy  =  "RRBTCopy";
	public static final String HunterNameOptoutCopy = "OptoutCopy";
	public static final String HunterNameStarCopy   =  "StarCopy";
	public static final String HunterNameConfirmedCopy = "ConfirmedCopy";
	public static final String HunterNameConfirmOptoutCopy = "ConfirmOptoutCopy";
	public static final String HunterNameExpiredCopy = "ExpiredCopy";
	public static final String HunterNameFailedCopy = "FailedCopy";
	static String HunterNameFailedProcessCopy = "FailedProcessCopy";	
	public static final String Cid = "Cid";
	
	private static final HashMap<String, String> hunterCopyTypeMap = new HashMap<String, String>();
    public static final String JUNK = "UNKNOWN";
	

	public static ArrayList<String> sitesList = new ArrayList<String>();
	public static HashMap<String, String> circleMap = new HashMap<String, String>();
	public static HashMap<String, String> urlMap = new HashMap<String, String>();
	public static ArrayList<String> crossPrefixes = new ArrayList<String>();
	public static String CIRCLE_RESOLVE_RETRY_ERROR = "CIRCLE_RESOLVE_RETRY_ERROR"; 
	public static String CIRCLE_RESOLVE_NON_RETRY_ERROR = "CIRCLE_RESOLVE_NON_RETRY_ERROR";
	static ArrayList<String> normalCopyKeys = null;
	static List<String> obdCopyKeys = new ArrayList<String>();
	static ArrayList<String> starCopyKeys = null;
	static List<Integer> categoryTypeList = null;
	static HashSet<String> virtualNumbers=null;
	public static  String DIRECTCOPY="D";
	public static  String OPTINCOPY="N";
	public static  String DEFAULTCOPY="-";

	static HashSet<String> copyVirtualNumbers = null;
	
	private static Map<String, String> chargeClassMapForAllSubInLoop = null;
	private static Map<String, String> chargeClassMapForSpecialSub = null;
	private static Map<String, String> chargeClassMapForSpecialSubInLoop = null;

	static
	{
		rbtConnector = RBTConnector.getInstance();
		init();
		initCopyAmountAndChargeClass();
		getDefaultClip();
		initializeEventLogger();
		
		ParametersCacheManager parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		Parameters parameter = parametersCacheManager.getParameter("GATHERER",
				"pir.httpHits.enable", "false");
		String virtualnos=getParamAsString("RRBT","VIRTUAL_NUMBERS", null);
		virtualNumbers=new HashSet<String>();
		String[] Virtual_no=null;
		if(virtualnos!=null)
		{
			Virtual_no=virtualnos.split(",");
		}
		if(Virtual_no!=null && Virtual_no.length>0)
		{
			for(int z=0;z<Virtual_no.length;z++)
			virtualNumbers.add(Virtual_no[z]);
		}
		if(getParamAsBoolean("IS_RRBT_COPY_ON", "FALSE"))
		{
			 DIRECTCOPY="RRBT_D";
			 OPTINCOPY="RRBT_N";
			 DEFAULTCOPY="RRBT-";
		}
		
		copyVirtualNumbers = new HashSet<String>();
		List<Parameters> virtualNoParameters = CacheManagerUtil.getParametersCacheManager().getParameters("VIRTUAL_NUMBERS");
		if (virtualNoParameters != null)
		{
			for (Parameters virtualNoParameter : virtualNoParameters)
			{
				copyVirtualNumbers.add(virtualNoParameter.getParam());
			}
			logger.info("The set of copy virtual numbers are : " + copyVirtualNumbers);
		}

		HttpPerformanceMonitor httpPerformanceMonitor = null;
		if (parameter.getValue().equalsIgnoreCase("true"))
		{
			String componentName = CopyBootstrapOzonized.COMPONENT_NAME;
			httpPerformanceMonitor = PerformanceMonitorFactory
				.newHttpPerformanceMonitor(componentName,
						"Http Performance Monitor", PerformanceDataType.LONG,
						"Milliseconds");
		}
		
		HttpParameters httpParameters = new HttpParameters(false, null, 8080, 5000, 5000,100, 10, httpPerformanceMonitor);
		rbtHttpClient = new RBTHttpClient(httpParameters);
		getSites();
	}
	
	//Added for airtel to specify mode
	private static String getSpecialCopyMode(Subscriber sub , Subscriber called)
	{
		try {
			logger.info("Getting the caller type for "+ sub.getSubscriberID() + " copying from " + called.getSubscriberID());	
			if(sub == null || called == null)
				return "";
			
			if(called.getCircleID() == null)
				return "";
			
			String virtualNumberConfig = RBTParametersUtils.getParamAsString("VIRTUAL_NUMBERS", called.getSubscriberID(), null);
			if (virtualNumberConfig != null) {
				String circleID = null;
				String[] tokens = virtualNumberConfig.split(","); 
				if (tokens.length >= 3)
					circleID = tokens[2];
				if (sub.getCircleID() != null
						&& sub.getCircleID().equalsIgnoreCase(circleID)) {
					// subscriber called a virtual number and
					// belongs to the same circle. 
					return "|CALLER_TYPE:VN";
				}
			}
			if(sub.getCircleID().equalsIgnoreCase(called.getCircleID()))
				return "|CALLER_TYPE:P2P";
			
			if(called.getCircleID().equalsIgnoreCase("NON_ONMOBILE"))
				return "|CALLER_TYPE:COM";
			if(!sub.getCircleID().equalsIgnoreCase(called.getCircleID()))
					return "|CALLER_TYPE:ONM";
			
			return "";
		}catch(Exception e){
			//safety check
			logger.error("Error while getting the caller type ", e);
			return "";
		}
	
	}

	static public String processLocalCopyRequest(ViralSMSTable vst, boolean isProcess, Subscriber subscriber) throws Exception
	{
		
		try
		{
			boolean isActivated = false;
			String caller = vst.callerID();
			boolean isOptinCopy=false;
			Subscriber sub = getSubscriber(vst.subID());
			String called = vst.subID();
			String setForCaller = null;
			String clipID = vst.clipID();
			String selectedBy = vst.selectedBy();
			String selectedForSMSBy = vst.selectedBy();
			Date currentDate = null;
			String response = null;
			String crossOperatorName = null;
			String extraInfoStr = vst.extraInfo();
			HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
			String sourceClipName = "";
			String keyPressed = "NA";
			String copyType=DEFAULTCOPY;
			String confMode="-";
			if(vst.type().equalsIgnoreCase(COPY) || vst.type().equalsIgnoreCase(COPYCONFIRM))
				{
				copyType=DIRECTCOPY;
				keyPressed = "s9";
				}
			else if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
				{
				copyType=OPTINCOPY;
				keyPressed = "s";
				}
			if(viralInfoMap != null && viralInfoMap.containsKey(SOURCE_WAV_FILE_ATTR))
				sourceClipName = viralInfoMap.get(SOURCE_WAV_FILE_ATTR);
			if(viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
				keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
			if(viralInfoMap != null && viralInfoMap.containsKey(COPY_CONFIRM_MODE_KEY))
				confMode = viralInfoMap.get(COPY_CONFIRM_MODE_KEY);
			
			if(getParamAsBoolean("SHOW_OPERATOR_NAME_CROSS_OPERATOR_SMS","FALSE") && selectedBy != null && selectedBy.indexOf("XCOPY") != -1)
			{
				crossOperatorName = selectedBy.substring(0,selectedBy.indexOf("_"));
			}
			if(selectedBy != null && !selectedBy.equalsIgnoreCase("null"))
				selectedBy = selectedBy.trim().toUpperCase();
			else 
				selectedBy = "PRESSSTAR";

			int cat = 26;
			int status = 1;
			String wavFile = null;
			StringBuffer wavFileBuf = new StringBuffer();
			StringBuffer catTokenBuf = new StringBuffer();
			StringBuffer catNameBuf = new StringBuffer();
			StringBuffer classTypeBuffer = new StringBuffer();
			StringBuffer statusBuf = new StringBuffer();
			StringBuffer setForCallerBuf = new StringBuffer();
			String songName = null;
			String classType = m_copyClassType; // TODO change 
			boolean isCallerSubscribed = true;
			boolean isCopyDone = false;
			boolean isPollRBTCopy = false;
	
			String callerCircleID = null;
	
			if (subscriber == null)
				subscriber = getSubscriber(caller);
			callerCircleID = subscriber.getCircleID();
	
			if (isInvalidCopy(subscriber, vst , isProcess))
			{
				copyFailed(vst, "INVALIDCOPY",keyPressed,confMode);
				return "INVALIDCOPY";
			}
			if(clipID != null && clipID.toUpperCase().indexOf("DEFAULT_") != -1 && currentDate !=null)
			{
				isPollRBTCopy = true;
				if(isProcess && getParamAsBoolean("ALLOW_POLL_COPY","FALSE"))
				{
					wavFile = clipID;
	
					//	Commented by Sree
					//	Subscriber sub = getSubscriber(caller);  
	
					if(subscriber == null || !isSubActive(subscriber)){
						boolean prepaid = getParamAsString("GATHERER","DEFAULT_SUBTYPE","pre").equalsIgnoreCase("pre");
						String copytype = "";
						if(RBTParametersUtils.getParamAsBoolean("GATHERER", "ALLOW_SPECIAL_COPY_MODE", "FALSE"))
						{
							copytype = getSpecialCopyMode(getSubscriber(caller),getSubscriber(called));
						}
						subscriber = activateSubscriber(caller, callerCircleID, called, "COPY", prepaid,getParamAsString("GATHERER","COPY_SUB_CLASS", "COPY"),copytype + "|CP:"+selectedBy+"-"+vst.subID()+":CP|", vst.selectedBy(), null, null,confMode);
						updateExtraInfoAndPlayerStatus(caller,true);
						isActivated = true;
					}else{
						StringBuffer strBuff=new StringBuffer();
						SelectionRequestBean selBean=new SelectionRequestBean();
						selBean.setSubscriberId(caller);
						selBean.setStatus(1);
						selBean.setFromTimeOfTheDay(0);
						selBean.setToTimeOfTheDay(23);
						rbtConnector.getSubscriberRbtclient().deleteSelections(selBean, "GATHERER", "DAEMON", strBuff);
						//					rbtDBManager.deactivateSubscriberRecords(caller,null,1,0,23,true,"DAEMON");
						//					if(pollExtraInfo == null || pollExtraInfo.equals(PLAY_POLL_STATUS_OFF))
						updateExtraInfoAndPlayerStatus(caller,true);
						isCopyDone = true;
					}
				}
			}

			boolean isVirtualNo = false;
			if (copyVirtualNumbers != null && copyVirtualNumbers.size() > 0)
			{
				if (copyVirtualNumbers.contains(called))
				{
					isVirtualNo = true;
					logger.info("match found");
				}
			}

			Clip clip = null;
			Category category = null; 
			if(!isPollRBTCopy)
			{
				if(clipID != null)
					category = getClipCopyDetails(clipID,wavFileBuf,catTokenBuf,catNameBuf,classTypeBuffer,  statusBuf, setForCallerBuf, isVirtualNo);
				if(setForCallerBuf != null && setForCallerBuf.length() > 0)
					setForCaller = setForCallerBuf.toString();
				if(wavFileBuf != null && wavFileBuf.toString().trim().length() > 0 && wavFileBuf.toString().trim().indexOf(">") != -1)
				{
					cat = Integer.parseInt(wavFileBuf.toString().substring(wavFileBuf.indexOf(">")+1));
					wavFileBuf.delete(wavFileBuf.indexOf(">"),wavFileBuf.length());
					category = rbtConnector.getMemCache().getCategory(cat);
					if(category != null)
					{
						catNameBuf = new StringBuffer(category.getCategoryName());
					}
				}
				if(category != null)
					cat = category.getCategoryId();
				if((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1) && defaultClipWavName != null)  // TODO defaultClipWavName
				{
					wavFileBuf = new StringBuffer(defaultClipWavName);
				}	
	
	
				if(classTypeBuffer != null && classTypeBuffer.toString().trim().length() > 0)
					classType = classTypeBuffer.toString().trim();
	
				try
				{
					status = Integer.parseInt(statusBuf.toString().trim());
				}
				catch(Exception e)
	
				{
					status  = 1;
				}
				wavFile = wavFileBuf.toString().trim();
	
				if (wavFile != null && wavFile.length() > 0 && status != 90 && status != 99)
					clip = getClipRBT(wavFile);
			}	
	
			if (clip != null && clip.getContentType() != null)
			{
				List<String> contentTypes = Arrays.asList(getParamAsString(GATHERER, "COPY_NON_SUPPORTED_CONTENT_TYPES", "").toUpperCase().split(","));
				if (contentTypes.contains(clip.getContentType().toUpperCase()))
				{
					String language = subscriber.getLanguage();
					String smsText = getSMSText(GATHERER,
							"NON_SUPPORTED_CONTENT_TYPE_SMS" + "_" + clip.getContentType().toUpperCase(),
							null, language);
					if (smsText == null) {
						smsText = getSMSText(GATHERER,
								"NON_SUPPORTED_CONTENT_TYPE_SMS", m_nonCopyContentSMS, language);
					}

					HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
					smsTextmap.put("SMS_TEXT", smsText);
					smsTextmap.put("CALLED_ID", called);
					smsTextmap.put("SONG_NAME", clip.getClipName());
					smsTextmap.put("CLIP_OBJECT", clip);
					smsTextmap.put("CATEGORY_OBJECT", category);
					if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE") && !isProcess)
						sendSMSviaPromoTool(subscriber, finalizeSmsText(smsTextmap));
					else
						sendSMS(subscriber, finalizeSmsText(smsTextmap));

					copyFailed(vst, "NONCOPY", keyPressed,confMode);
					return "NONCOPY";
				}
			}

			if (isNonCopyContent(clipID, catTokenBuf.toString(), clip, status, wavFile,category, isVirtualNo))
			{
				String language=subscriber.getLanguage();
				if(getParamAsBoolean("SEND_CROSS_COPY_CONTENT_MISSING_SMS","FALSE") && wavFile != null && wavFile.indexOf("MISSING") != -1){
					if(getParamAsBoolean("USE_DND_SMS_URL","FALSE") && !isProcess)
						sendSMSviaPromoTool(subscriber, prepareCrossOperatorContentMissingSmsText(
								called, crossOperatorName, sourceClipName, language));
					else
						sendSMS(subscriber, prepareCrossOperatorContentMissingSmsText(called, crossOperatorName, sourceClipName,language));
				}
				else if (getParamAsBoolean("NON_COPY_SENT_SMS","FALSE"))
				{ 
					if(clip != null && clip.getClipEndTime() != null && clip.getClipEndTime().getTime() < System.currentTimeMillis()) 
					{
						HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
						smsTextmap.put("SMS_TEXT", getSMSText("GATHERER","NON_COPY_EXPIRED_CLIP_SMS",m_nonCopyExpiredClipSMS,language));
						smsTextmap.put("CALLED_ID", called);
						smsTextmap.put("SONG_NAME", clip.getClipName());
						smsTextmap.put("CLIP_OBJECT", clip);
						smsTextmap.put("CATEGORY_OBJECT", category);
						
						if(getParamAsBoolean("USE_DND_SMS_URL","FALSE") && !isProcess)
							sendSMSviaPromoTool(subscriber, finalizeSmsText(smsTextmap));
						else
							sendSMS(subscriber, finalizeSmsText(smsTextmap)); 
					}
					else {
						if(getParamAsBoolean("USE_DND_SMS_URL","FALSE") && !isProcess)
							sendSMSviaPromoTool(subscriber, getSMSText("GATHERER","NON_COPY_CONTENT_SMS","Sorry this selection cannot be copied",language));
						else
							sendSMS(subscriber, getSMSText("GATHERER","NON_COPY_CONTENT_SMS","Sorry this selection cannot be copied",language));
					}
						
				}
				copyFailed(vst, "NONCOPY",keyPressed,confMode);
				return "NONCOPY";
			}
				else if (category != null&& categoryTypeList.contains(category.getCategoryTpe())
				&& category.getCategoryEndTime().after(new Date())&&getParamAsBoolean("COPY_SHUFFLE", "FALSE")&&!getParamAsBoolean("COPY_SHUFFLE_SONG_ONLY", "FALSE"))
			songName=category.getCategoryName();
	
			else if (clip != null)
				songName = clip.getClipName();
			else if(status == 90)
				songName = "Cricket Feed";
	
			String finalSelectedBy="PRESSSTAR";
			String actAmt = null;
			String selAmt = null;
			if(!isPollRBTCopy && isProcess)
			{
	
				boolean prepaid = subscriber.isPrepaid();
				if (getParamAsBoolean("DIFFERNTIAL_COPY","FALSE") && getParamAsString("DIFFERNTIAL_COPY_TYPE") != null)
					if (getSubscriberPromo(caller) != null && getSubscriberPromo(called) != null)
						classType = "YOUTHCARD";
	
				if(copyChargeClassMap != null && copyChargeClassMap.size() > 0 && copyChargeClassMap.containsKey(selectedBy))
					classType = (String)copyChargeClassMap.get(selectedBy);
	
				String subClass = getParamAsString("GATHERER","COPY_SUB_CLASS", "COPY");
	
				// If user is not deactive overriding the subscription class
				if (subscriber != null
						&& subscriber.getSubscriptionClass() != null
						&& !subscriber.getStatus().equals(WebServiceConstants.DEACTIVE))
					subClass = subscriber.getSubscriptionClass();
				
	
				boolean makeSel = true;
				logger.info("Reached here makesel is "+makeSel);
				if (!getParamAsBoolean("INSERT_DEFAULT_SEL", "FALSE")
						&& defaultClipWavName != null && defaultClipWavName.equalsIgnoreCase(wavFile))
				{
					makeSel=false;
					logger.info("Adding default selections is not allowed, makeSel: " + makeSel);
				}

				if (defaultClipWavName != null
						&& defaultClipWavName.equalsIgnoreCase(wavFile)
						&& !isUserHavingAllCallerIDSelection(subscriber, wavFile))
				{
					makeSel = false;
					response = "ALREADY_EXISTS";
					logger.info("User is active with no selections so not adding the selection, makeSel: " + makeSel);
				}

				if (status == 90
						&& getParamAsBoolean("COPY_CRICKET_SEL", "FALSE")
						&& !isSubAlreadyActiveOnStatus(subscriber.getSubscriberID(), setForCaller, 90)
						&& schedule != null
						&& schedule.getEndDate() != null
						&& schedule.getEndDate().after(new Date(System.currentTimeMillis()))
						&& schedule.getChargeClass() != null
						&& schedule.getChargeClass().trim().length() > 0
						&& !schedule.getChargeClass().trim().equalsIgnoreCase("null"))
				{
					classType = schedule.getChargeClass().trim().toUpperCase();
				}
				else if (status == 90)
				{
					makeSel = false;
				}
	
				if(selectedBy != null && selectedBy.indexOf("_XCOPY")== -1)
					finalSelectedBy = selectedBy;
				if(getParamAsBoolean("USE_DEFAULT_ACT_SEL_BY", "TRUE"))
					finalSelectedBy  = "COPY";
				String selByOptInCopy = getParamAsString("MODE_FOR_OPTIN_COPY");
				if(selByOptInCopy != null)
				{
					if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
						finalSelectedBy = selByOptInCopy;
				}
				String copytype = "";
				if(RBTParametersUtils.getParamAsBoolean("GATHERER", "ALLOW_SPECIAL_COPY_MODE", "FALSE"))
				{
					copytype = getSpecialCopyMode(getSubscriber(caller),getSubscriber(called));
				}
				selectedBy = copytype + "|CP:"+selectedBy+"-"+vst.subID()+":CP|";
	
				boolean useUIChargeClass = false ;
				if(Utility.isSubActive(subscriber) && activeclassTypeParam != null)
				{
					logger.info("User is active using active user charge class");
					useUIChargeClass = getParamAsBoolean("USE_DIFFERENTIAL_UI_CHARGE_CLASS", "false");
					classType = activeclassTypeParam;
				}
				else if(!Utility.isSubActive(subscriber) && inactiveclassTypeParam != null)
				{
					logger.info("User is inactive using inactive user charge class");
					useUIChargeClass = getParamAsBoolean("USE_DIFFERENTIAL_UI_CHARGE_CLASS", "false");
					classType = inactiveclassTypeParam;
				}				
				
				actAmt = getActivationAmount(caller, subscriber, finalSelectedBy);
				selAmt = getSelectionAmount(caller, subscriber, finalSelectedBy,classType, useUIChargeClass);
	
				if(getParamAsBoolean("BLOCK_COPY_SHUFFLE_SUBSCRIBER","FALSE") && isShufflePresentSelection(caller, setForCaller))
				{
					String language=subscriber.getLanguage();
					String smsText = getSMSText("GATHERER", "SMS_TEXT_BLOCK_COPY_SHUFFLE_SUBSCRIBER", smsTextBlockCopyForShuffleSubscriber,language);
					if(getParamAsBoolean("SEND_SMS_BLOCK_COPY_SHUFFLE_SUBSCRIBER","TRUE") 
							&& smsText != null && smsText.length() > 0)
						sendSMS(subscriber, smsText);
					return "SHUFFLE_SELECTION_EXISTS";
				}	
				else if (makeSel)
				{   
					Clip clipTemp=rbtConnector.getMemCache().getClipByRbtWavFileName(wavFile);
					HashMap<String, String> currentSubscriberExtraInfo = null;
					if(isSubActive(subscriber))
						currentSubscriberExtraInfo = subscriber.getUserInfoMap();
					boolean isCallerBuddyNetUser=false;
					boolean isCalledBuddyNetUser=false;
					int buddySelCount = 0;
					int buddynetFreeSelLimit = getParamAsInt("BUDDYNET_FREE_SEL_COUNT", 0);
					if(currentSubscriberExtraInfo == null)
						currentSubscriberExtraInfo = new HashMap<String, String>();
					if(clipTemp!=null)
					{
						if(subscriber != null && subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.LOCKED))
						{
							response = "COPY_SELECTION_USER_LOCKED";
						}
						else
						{
							if(vst.selectedBy() == null || vst.selectedBy().contains("PRESSSTAR"))
							{	
								if(m_redirectBuddyNet){
									if (subscriber == null)
										subscriber = getSubscriber(caller);
									Subscriber calledSubscriber=getSubscriber(called);
									isCallerBuddyNetUser=isBuddyNetUsers(caller,subscriber);
									isCalledBuddyNetUser=isBuddyNetUsers(called,calledSubscriber);
									if(isCallerBuddyNetUser && isCalledBuddyNetUser)
									{
										if(buddynetFreeSelLimit > 0)
										{
											if(currentSubscriberExtraInfo != null && currentSubscriberExtraInfo.containsKey("BUDDY_SEL"))
												buddySelCount = getIntegerValue(currentSubscriberExtraInfo.get("BUDDY_SEL"));
											buddySelCount++ ;
											currentSubscriberExtraInfo.put("BUDDY_SEL", ""+buddySelCount);
											if(buddySelCount <= buddynetFreeSelLimit)
											{
												String classTypeTemp=getParamAsString("PRESSSTAR_BUDDY_NET_CHARGE_CLASS");
												if(classTypeTemp!=null && !classTypeTemp.equalsIgnoreCase("null") && !classTypeTemp.equalsIgnoreCase(""))
												{
													classType=classTypeTemp;
												}
											}
										}
									}
								}
							}
							
							
							String virtualNumberConfig = getParamAsString(
									"VIRTUAL_NUMBERS", vst.subID(), null);
							if (virtualNumberConfig != null) {
								useUIChargeClass = false;
								String circleID = null;
								String subClassStr = null;
								String chargeclass=null;
								String[] tokens = virtualNumberConfig
										.split(","); // value :
								// wavFile,SubscriptionClass,circleId

								if (tokens.length >= 2)
									subClassStr = tokens[1];
								if (tokens.length >= 3)
									circleID = tokens[2];
								if (tokens.length >= 4)
									chargeclass = tokens[3];

								if (subscriber.getCircleID() != null
										&& circleID != null
										&& subscriber.getCircleID()
												.equalsIgnoreCase(circleID)) {
									// subscriber called a virtual number and
									// belongs to the same circle. hence
									// overriding the charge class
									if (!isSubActive(subscriber))
									{
										subClass = subClassStr;
									}
									if(chargeclass!=null)
									{
										String[] chargeClassSplit = chargeclass.split(":");
										String activeChargeClass = null;
										String inactiveChargeClass = null;
										
										if(chargeClassSplit.length > 1){
											
											activeChargeClass = chargeClassSplit[0];
											inactiveChargeClass = chargeClassSplit[1];
											
										}else if(chargeClassSplit.length == 1){
											
											activeChargeClass = chargeClassSplit[0];
											
										}
										
										if(Utility.isSubActive(subscriber) && activeChargeClass != null && !activeChargeClass.equalsIgnoreCase("")){
											
											useUIChargeClass=true;
											classType = activeChargeClass;
											selAmt = getSelectionAmount(caller, subscriber,
													finalSelectedBy, classType, useUIChargeClass);
										}
										else if(!Utility.isSubActive(subscriber) && inactiveChargeClass != null && !inactiveChargeClass.equalsIgnoreCase("")){
											
											useUIChargeClass=true;
											classType = inactiveChargeClass;
											selAmt = getSelectionAmount(caller, subscriber ,
													finalSelectedBy, classType, useUIChargeClass);
										}
										
										logger.info("The called no is virtual no & use ui is " + useUIChargeClass + " and class type = " + classType );
										
									}
								}
								
							}


							if(!isSubActive(subscriber))
							{
								isCallerSubscribed = false;
								isActivated = true;
								if(selectedBy == null || selectedBy.contains("PRESSSTAR") || selectedBy.contains("XCOPY"))
								{	
									currentSubscriberExtraInfo.put(REFUND, "TRUE");
								}
								if(vst.type() != null && vst.type().equalsIgnoreCase(COPYCONFIRMED))
								{
									currentSubscriberExtraInfo.put(EXTRA_INFO_COPY_TYPE, EXTRA_INFO_COPY_TYPE_OPTIN);
								}
								if(confMode!=null&&!confMode.equalsIgnoreCase("-"))
								{
									currentSubscriberExtraInfo.put(EXTRA_INFO_COPY_MODE, confMode);

								}

							}
							response = addSelections(caller, setForCaller, prepaid, cat,
									clipTemp, finalSelectedBy, selectedBy, classType,
									subClass, vst.selectedBy(), vst.type(),isCallerSubscribed , subscriber.getUserType(), subscriber.getCosID(), status, currentSubscriberExtraInfo,confMode,useUIChargeClass);
						}
	
						if(response.indexOf("SUCCESS")!=-1)
						{
							isCopyDone = true;
							if(isSubActive(subscriber) && isCallerBuddyNetUser && isCalledBuddyNetUser && buddySelCount <= buddynetFreeSelLimit)
							{
								updateSubscription(subscriber.getSubscriberID(), buddySelCount);
							}	
						}
						else
						{
							if(!isSubActive(subscriber))
								isActivated = false;
							
						}	
					}
				}
				else if (subscriber == null || !isSubActive(subscriber))
				{
					isCallerSubscribed = false;
					String actBy = "COPY";
					String cosId = null;
	
					if(finalSelectedBy != null && finalSelectedBy.indexOf("_XCOPY") != -1 )
						actBy = finalSelectedBy;
					if(getParamAsBoolean("USE_DEFAULT_ACT_SEL_BY","TRUE"))
						actBy  = "COPY";
					String modeOptInCopy = getParamAsString("MODE_FOR_OPTIN_COPY");
					if(modeOptInCopy != null)
					{
						if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
							actBy = modeOptInCopy;
					}
					boolean prepaid1 = getParamAsString("GATHERER","DEFAULT_SUBTYPE","pre").equalsIgnoreCase("pre");
	
					// To support Lite feature in copy. To pass cosId in the activation request.
					Clip clipTemp=rbtConnector.getMemCache().getClipByRbtWavFileName(wavFile);
					if(clipTemp != null && clipTemp.getContentType().equalsIgnoreCase(WebServiceConstants.COS_TYPE_LITE))
					{
						String circleID = null;
						MNPContext mnpContext = new MNPContext(caller, "COPY");
						mnpContext.setOnlineDip(getParamAsBoolean("COPY_ONLINE_DIP", "FALSE"));
						SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(mnpContext);
						if (subscriberDetail != null)
							circleID = subscriberDetail.getCircleID();
						List<CosDetails> cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetailsByCosType(WebServiceConstants.COS_TYPE_LITE, circleID, prepaid1?"y":"n");
						if(cos != null && cos.size() > 0)
							cosId = cos.get(0).getCosId();
					}
					if(selectedBy.indexOf("CP:") == -1){
						
						String callertype = "";
						if(RBTParametersUtils.getParamAsBoolean("GATHERER", "ALLOW_SPECIAL_COPY_MODE", "FALSE"))
						{
							callertype = getSpecialCopyMode(getSubscriber(caller),getSubscriber(called));
						}
						selectedBy = callertype + "|CP:"+selectedBy+"-"+vst.subID()+":CP|";
					}
						
					subscriber = activateSubscriber(caller, callerCircleID, called, actBy, prepaid1,getParamAsString("GATHERER","COPY_SUB_CLASS", "COPY"), selectedBy , vst.selectedBy(), cosId, vst.type(),confMode);
					if (subscriber == null)
					{
						copyFailed(vst, "NA",keyPressed,confMode);
						return "NA";
					}
					isActivated = true;
				}
			
			}
	
			if(isProcess)
			{
				String subTypeRegion = m_localType;
				if(selectedBy.contains("VN")){
					subTypeRegion = m_virtualType;
				}
				if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
					try {
						if(response != null && response.equals("ALREADY_EXISTS")) {
							eventLogger.copyTrans(vst.subID(), vst.callerID(), isActivated ? "YES" : "NO", subTypeRegion, catNameBuf.toString(), isCopyDone ? "TRUE" : "FALSE",
								vst.sentTime(),copyType, keyPressed,DUPLICATE, wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
						} else if(response != null && response.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)) {
							eventLogger.copyTrans(vst.subID(), vst.callerID(), isActivated ? "YES" : "NO", subTypeRegion, catNameBuf.toString(), isCopyDone ? "TRUE" : "FALSE",
								vst.sentTime(),copyType, keyPressed,PREMIUM_CONTENT, wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
						}else if(response != null && response.contains(WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED)) {
							eventLogger.copyTrans(vst.subID(), vst.callerID(), isActivated ? "YES" : "NO", subTypeRegion, catNameBuf.toString(), isCopyDone ? "TRUE" : "FALSE",
									vst.sentTime(),copyType, keyPressed,"COS_MISMATCH", wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
						}else if(response != null && response.equalsIgnoreCase(WebServiceConstants.OFFER_NOT_FOUND)) {
							eventLogger.copyTrans(vst.subID(), vst.callerID(), isActivated ? "YES" : "NO", subTypeRegion, catNameBuf.toString(), isCopyDone ? "TRUE" : "FALSE",
								vst.sentTime(),copyType, keyPressed,"OFFER_NOT_FOUND", wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
						} else {
							eventLogger.copyTrans(vst.subID(), vst.callerID(), isActivated ? "YES" : "NO", subTypeRegion, catNameBuf.toString(), isCopyDone ? "TRUE" : "FALSE",
								vst.sentTime(),copyType, keyPressed,COPIED, wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
						}
					} catch (ReportingException e) {
					}
				}
				if(getParamAsBoolean("WRITE_TRANS","FALSE"))
				{
					//If selection is already exist, put the status as DUPLICATE
					if(response != null && response.equals("ALREADY_EXISTS"))
					{
						writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf.toString(), Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), subTypeRegion, isActivated ? "YES" : "NO",  isCopyDone ? "TRUE" : "FALSE",DUPLICATE,keyPressed,copyType,confMode);
					}
					else if(response != null && response.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED))
					{
						writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf.toString(), Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), subTypeRegion, isActivated ? "YES" : "NO",  isCopyDone ? "TRUE" : "FALSE",PREMIUM_CONTENT,keyPressed,copyType,confMode);
					}
					else if(response != null  && response.equalsIgnoreCase(WebServiceConstants.OFFER_NOT_FOUND))
					{
						writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf.toString(), Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), subTypeRegion, isActivated ? "YES" : "NO",  isCopyDone ? "TRUE" : "FALSE","OFFER_NOT_FOUND",keyPressed,copyType,confMode);
					}
					else
					{
						writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf.toString(), Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), subTypeRegion, isActivated ? "YES" : "NO",  isCopyDone ? "TRUE" : "FALSE",COPIED,keyPressed,copyType,confMode);
					}
					removeCopyViralPromotion(vst.subID(), vst.callerID(), vst.sentTime());
				}
				else
				{
					//If selection is already exist, put the status as DUPLICATE
					if(response != null && response.equals("ALREADY_EXISTS"))
					{
						updateCopyViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), DUPLICATE, null);
					}
					else if(response != null && response.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED))
					{
						updateCopyViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), PREMIUM_CONTENT, null);
					}
					else if(response != null && response.contains(WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED))
					{
						updateCopyViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), "COS_MISMATCH", null);
					}
					else
					{
						updateCopyViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), COPIED, null);
					}
				}
			}
	
			if (songName == null && isPollRBTCopy)
				songName = "RBT POLL";
			else if(songName == null)
				songName = "Default Tune";
			boolean sensPressStarSMS=false;
			String sms = null;
			String language=subscriber.getLanguage();
			if(isProcess)
			{
				if(crossOperatorName != null)
					crossOperatorName = crossOperatorName + " No.";
				String strSmsText = null;
				if (isActivated)
				{
					strSmsText = getSMSText("GATHERER", "COPY_ACT_SMS", m_copyActSMS,language);
				}
				else if(isCopyDone)
				{
					strSmsText = getSMSText("GATHERER", "COPY_SEL_SMS", m_copySelSMS,language);
				}
				else if(response != null && response.equals("SELECTION_SUSPENDED"))
				{
					strSmsText = getSMSText("GATHERER","COPY_SELECTION_SUSPENDED_TEXT",null,language);
				}
				else if(response != null && response.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED))
				{
					strSmsText = getSMSText("LITE_USER","PREMIUM_BLOCKED",null,language);
				}
				else if(response != null && response.contains(WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED))
				{
					String cosContent = response.substring(response.indexOf(WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED)+WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED.length());
					strSmsText = getSMSText("COPY_COS_MISMATCH","CONTENT_BLOCKED_"+cosContent.toUpperCase(), getSMSText("LITE_USER","PREMIUM_BLOCKED",null,language),language);
				}
				else if(response != null && response.equals("ALREADY_EXISTS"))
				{
					strSmsText = getSMSText("GATHERER","COPY_SAME_SEL_SMS",null,language);
				}
				else if(response != null && response.equals(WebServiceConstants.OFFER_NOT_FOUND))
				{
					strSmsText = getSMSText("OFFER_NOT_FOUND",null,null,language);
				}
				else if(response != null && response.equals("SUCCESS_DOWNLOAD_EXISTS"))
				{
					strSmsText = getSMSText("GATHERER","SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT",null,language);
				}
				else if(response != null && response.indexOf("OVERLIMIT") != -1)
				{
					strSmsText = getSMSText("GATHERER","COPY_SELECTION_OVERLIMIT_TEXT",null,language);
				}
				else if(response != null && response.equalsIgnoreCase("COPY_SELECTION_USER_LOCKED"))
				{
					strSmsText = getSMSText("LOCK_COPY","FAILURE",null,language);
				}
				else
				{
					strSmsText = getSMSText("GATHERER","COPY_TECHNICAL_FAILURE_TEXT",null,language);
				}
				HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
				smsTextmap.put("SMS_TEXT", strSmsText);
				smsTextmap.put("CALLED_ID", called);
				smsTextmap.put("SONG_NAME", songName);
				if(actAmt != null)
					actAmt = getInLocalCurrencyFormat(actAmt);
				smsTextmap.put("ACT_AMOUNT", actAmt);
				if(selAmt != null)
					selAmt = getInLocalCurrencyFormat(selAmt);
				smsTextmap.put("SEL_AMOUNT", selAmt);
				if(crossOperatorName != null)
					smsTextmap.put("OPERATOR_NAME", crossOperatorName);	
				smsTextmap.put("CLIP_OBJECT", clip);
				smsTextmap.put("CATEGORY_OBJECT", category);
				sms = finalizeSmsText(smsTextmap);
				
			}
			else
			{
				if(getParamAsBoolean("IS_STAR_OPT_IN_ALLOWED","FALSE") && vst.type().equals(COPYSTAR))
				{
					String virtualNumberConfig = getParamAsString("VIRTUAL_NUMBERS", vst.subID(), null);
					String circleID = null;
					if(virtualNumberConfig != null)
					{
						String[] tokens = virtualNumberConfig.split(","); // value : wavFile,SubscriptionClass,circleId
		
						if (tokens.length >= 3)
							circleID = tokens[2];
		
					}
					if ((virtualNumberConfig != null && circleID == null) || (circleID != null && callerCircleID != null  && circleID.equalsIgnoreCase(callerCircleID)))
					{
						//subscriber called a virtual number and belongs to the same circle. hence copy will be processed as direct copy
						sms = null;
						updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFIRMED, null);
	
					}
					else
					{	
						sensPressStarSMS=true;
						boolean isActive = isSubActive(subscriber);
						boolean liteCondition = false;
						if(subscriber != null && isActive && clip != null)
						{
							String cosStr = subscriber.getCosID();
							String cosType = null;
							if(cosStr != null)
								cosType = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.getCosID()).getCosType();
							if(cosType!=null && cosType.equalsIgnoreCase(WebServiceConstants.COS_TYPE_LITE) && clip.getContentType()!=null && !clip.getContentType().equalsIgnoreCase(WebServiceConstants.COS_TYPE_LITE))
							{
								liteCondition = true;
								sms = getSMSText("LITE_USER","PREMIUM_BLOCKED",null,language);
								if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
									try {
										eventLogger.copyTrans(vst.subID(), vst.callerID(), isActivated ? "YES" : "NO", m_localType, catNameBuf.toString(), isCopyDone ? "TRUE" : "FALSE",
								vst.sentTime(),copyType, keyPressed,PREMIUM_CONTENT, wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
									} catch (ReportingException e) {
									}
								}
								if (getParamAsBoolean("WRITE_TRANS","FALSE")){
									writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf.toString(), Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), m_localType, isActivated ? "YES" : "NO",  isCopyDone ? "TRUE" : "FALSE",PREMIUM_CONTENT,keyPressed,copyType,confMode);
								}
								removeCopyViralPromotion(vst.subID(), vst.callerID(), vst.sentTime());
							}
								
						}	
						
						if(!liteCondition)
						{
							String smsText = getSMSText("GATHERER", "OPT_IN_CONFIRMATION_ACT_SMS", m_optInConfirmationActSMS,language);
							isOptinCopy=true;
							if (isActive)
								smsText = getSMSText("GATHERER", "OPT_IN_CONFIRMATION_SEL_SMS", m_optInConfirmationSelSMS,language);
	
							HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
							smsTextmap.put("SMS_TEXT", smsText);
							smsTextmap.put("SONG_NAME", songName);
							smsTextmap.put("TIMEOUT", getParamAsString("GATHERER","WAIT_TIME_DOUBLE_CONFIRMATION",30+""));
							if(actAmt != null)
								actAmt = getInLocalCurrencyFormat(actAmt);
							smsTextmap.put("ACT_AMOUNT", actAmt);
							if(selAmt != null)
								selAmt = getInLocalCurrencyFormat(selAmt);
							smsTextmap.put("SEL_AMOUNT", selAmt);
							smsTextmap.put("COPY_CONFIRM_KEYWORD", getParamAsString("SMS","COPY_CONFIRM_KEYWORD","COPYYES"));
							smsTextmap.put("COPY_CANCEL_KEYWORD", getParamAsString("SMS","COPY_CANCEL_KEYWORD","COPYNO"));
							smsTextmap.put("CLIP_OBJECT", clip);
							smsTextmap.put("CATEGORY_OBJECT", category);
							sms = finalizeSmsText(smsTextmap);
							updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFPENDING, null);
						}
					}
				}else
					if (getParamAsBoolean("IS_OPT_IN","FALSE")) 
					{
						sensPressStarSMS=true;
						if (isSubActive(subscriber) && !getParamAsBoolean("IS_OPT_IN_FOR_ACTIVE_SUB","FALSE")){
							updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFIRMED, null);
						}else{
							String smsText = getSMSText("GATHERER", "OPT_IN_CONFIRMATION_ACT_SMS", m_optInConfirmationActSMS,language);
							isOptinCopy=true;
							if (isSubActive(subscriber))
								smsText = getSMSText("GATHERER", "OPT_IN_CONFIRMATION_SEL_SMS", m_optInConfirmationSelSMS,language);
	
							HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
							smsTextmap.put("SMS_TEXT", smsText);
							smsTextmap.put("SONG_NAME", songName);
							smsTextmap.put("TIMEOUT", getParamAsString("GATHERER","WAIT_TIME_DOUBLE_CONFIRMATION",30+""));
							if(actAmt != null)
								actAmt = getInLocalCurrencyFormat(actAmt);
							smsTextmap.put("ACT_AMOUNT", actAmt);
							if(selAmt != null)
								selAmt = getInLocalCurrencyFormat(selAmt);
							smsTextmap.put("SEL_AMOUNT", selAmt);
							smsTextmap.put("COPY_CONFIRM_KEYWORD", getParamAsString("SMS","COPY_CONFIRM_KEYWORD","COPYYES"));
							smsTextmap.put("COPY_CANCEL_KEYWORD", getParamAsString("SMS","COPY_CANCEL_KEYWORD","COPYNO"));
							smsTextmap.put("CLIP_OBJECT", clip);
							smsTextmap.put("CATEGORY_OBJECT", category);
							sms = finalizeSmsText(smsTextmap);
							updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFPENDING, null);
						}
					}else{
						HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
						smsTextmap.put("SMS_TEXT", getSMSText("GATHERER","PRESS_STAR_CONFIRMATION_SMS",m_pressStarConfirmationSMS,language));
						smsTextmap.put("SONG_NAME", songName);
						smsTextmap.put("TIMEOUT", getParamAsString("GATHERER","WAIT_TIME_DOUBLE_CONFIRMATION",30+""));
						smsTextmap.put("CLIP_OBJECT", clip);
						smsTextmap.put("CATEGORY_OBJECT", category);
						sms = finalizeSmsText(smsTextmap);
						
						updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFIRM, null);
					}
			}
			if(sms != null && sms.trim().length() > 0 && (getParamAsBoolean("SEND_COPY_PRE_SMS_ALL_MODES","TRUE") || selectedForSMSBy == null))
			{
				if(isOptinCopy)
				{	//deactive caller case
					if (songName != null && !songName.equalsIgnoreCase(""))
					{	
						com.onmobile.apps.ringbacktones.content.Subscriber sub1=rbtDBManager.getSubscriber(caller);
						SubscriberStatus[] substatus = null;
						SubscriberStatus substatus1 = null;
						if (sub1 != null)
						{
							substatus = rbtDBManager.getAllActiveSubSelectionRecords(caller);
							substatus1 = rbtDBManager.getAvailableSelection(null, 
								caller,null, substatus,rbtDBManager.getCategory(category.getCategoryId(), callerCircleID,'b'), 
								wavFile, status, 0,2359, clip.getClipStartTime(), clip.getClipEndTime(),false,
								(rbtDBManager.allowLooping() && rbtDBManager
										.isDefaultLoopOn()) , sub1.rbtType() , null, vst.selectedBy());
						}
						if(substatus1!=null || (substatus1 == null && !isUserHavingAllCallerIDSelection(subscriber, wavFile)))
						{	
							HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
							smsTextmap.put("SMS_TEXT", getSMSText("GATHERER", "OPT_IN_SAME_SONG_FAILURE", m_optInSameSongSms, language));
							smsTextmap.put("SONG_NAME", songName);
							smsTextmap.put("CLIP_OBJECT", clip);
							smsTextmap.put("CATEGORY_OBJECT", category);
							sms = finalizeSmsText(smsTextmap);
							
							sendPressStarSMS(subscriber, sms);
								if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
									try {
										eventLogger.copyTrans(vst.subID(), vst.callerID(), isActivated ? "YES" : "NO", m_localType, catNameBuf.toString(), "FALSE",
								vst.sentTime(),copyType, keyPressed,DUPLICATE, wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
									} catch (ReportingException e) {
									}
								}
								if (getParamAsBoolean("WRITE_TRANS","FALSE")){
									writeTrans(vst.subID(), vst.callerID(), wavFile,
										catNameBuf.toString(), Tools
										.getFormattedDate(vst.sentTime(),
												"yyyy-MM-dd HH:mm:ss"),
												m_localType, isActivated ? "YES" : "NO",
														 "FALSE",DUPLICATE,keyPressed,copyType,confMode);
								}
								removeViralPromotion(vst.subID(), vst.callerID(), vst
										.sentTime(),COPYCONFPENDING);
								return response;
							}

						}
					
					
				}
				
				logger.info("Is optin Copy is : " + isOptinCopy );
				if(getParamAsBoolean("USE_UMP_URL","FALSE") && isOptinCopy)
				{	
					logger.info("Using UMP Url to send the SMS ");

						String umpUrl=getParamAsString("UMP_GATEWAY_URL");//Gathrer
						if(umpUrl!=null&&!umpUrl.equalsIgnoreCase(""))
						{
							try {
								umpUrl = umpUrl.replaceAll("<%msisdn%>", caller);
								umpUrl = umpUrl.replaceAll("<%smstext%>", getEncodedUrlString(sms));
								Integer statusInt = new Integer(-1);
								StringBuffer result = new StringBuffer();
								logger.info("RBT:: UmpUrl: " + umpUrl);
								Tools.callURL(umpUrl, statusInt, result, false , null,-1);
								return response;

							} catch (Exception e) {
								logger.error("", e);
								e.printStackTrace();
							}
						}
				}
				else if(getParamAsBoolean("USE_DND_SMS_URL","FALSE") && isOptinCopy ) // To send optin confirmation request through promo tool to check DND users
				{
					logger.info("Using DND Url to send the SMS ");
					sendSMSviaPromoTool(subscriber, sms);
				}
				else if(sensPressStarSMS){
					sendPressStarSMS(subscriber, sms);
				}else{
					sendSMS(subscriber, sms);
				}
			}
	
			if(getParamAsBoolean("COPIEE_SEND_SMS","FALSE") && isProcess)
			{
				HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
				smsTextmap.put("SMS_TEXT", getSMSText("GATHERER","COPIEE_SMS_TEXT", null,language));
				smsTextmap.put("SONG_NAME", songName);
				smsTextmap.put("CALLED_ID", caller);
				smsTextmap.put("CLIP_OBJECT", clip);
				smsTextmap.put("CATEGORY_OBJECT", category);
				String copieeSMS = finalizeSmsText(smsTextmap); 
				if(copieeSMS != null && copieeSMS.length() > 0 && called != null && called.length() > 0){
					if(sensPressStarSMS){
						sendPressStarSMS(subscriber, copieeSMS);
					}else{
						sendSMS(sub, copieeSMS);
					}
				}
			}
			
			return response;
		}
		catch(Exception e)
		{
			logger.error("", e);
			throw new Exception(e.getMessage());
		}
	}
	
	static public String getParamAsString(String param)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, null);
		}catch(Exception e){
			return null;
		}
	}

	static public String getParamAsString(String type, String param, String defaultValue)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defaultValue);
		}catch(Exception e){
			return defaultValue;
		}
	}

	static public int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			return defaultVal;
		}
	}

	static public int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			return defaultVal;
		}
	}
 
	static public boolean getParamAsBoolean(String param, String defaultVal)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal).equalsIgnoreCase("TRUE");
		}catch(Exception e){
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
	static public boolean getParamAsBoolean(String type, String param, String defaultVal)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal).equalsIgnoreCase("TRUE");
		}catch(Exception e){
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	static public void initCopyAmountAndChargeClass()
	{
		String amt = null;
		Category category = rbtConnector.getMemCache().getCategory(26);
		if(category == null)
			return;

		ChargeClass  chargeClass = rbtConnector.getRbtGenericCache().getChargeClass(category.getClassType());
		if(chargeClass != null)
		{
			amt = chargeClass.getAmount();
			m_copyClassType = category.getClassType();
			logger.info("m_copyClassType="+m_copyClassType);
			try
			{
				copyAmount = Integer.parseInt(amt.trim());
			}
			catch(Exception e)
			{
				copyAmount = 10;
			}

		}
		logger.info("copyAmount="+copyAmount);
	}	
	
	static public Subscriber getSubscriber(String strSubID)
	{
		return rbtConnector.getSubscriberRbtclient().getSubscriber(strSubID,"GATHERER");
	}
	
	static public boolean isSubActive(Subscriber sub)
	{
		if(sub.getStatus().equalsIgnoreCase(WebServiceConstants.ACT_PENDING) || sub.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE) || 	
				sub.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED )|| sub.getStatus().equalsIgnoreCase(WebServiceConstants.GRACE) 
				|| sub.getStatus().equalsIgnoreCase(WebServiceConstants.LOCKED))
			return true;
		else
			return false;
	}
	
	static private boolean isInvalidCopy(Subscriber subscriber, ViralSMSTable vst , boolean isProcess)
	{
		boolean isInvalid = false;
		if (subscriber.getSubscriberID().equalsIgnoreCase(vst.subID())
				|| subscriber.getSubscriberID().length() < 7
				|| subscriber.getSubscriberID().length() < 7)
			isInvalid =  true;
		else if ((subscriber == null || !isSubActive(subscriber)) && !getParamAsBoolean("IS_ACT_OPTIONAL_PRESSSTAR","TRUE"))
			isInvalid = true;
		else if (getParamAsBoolean("SHOW_BLACKLIST_TYPE","FALSE") && !subscriber.isCanAllow())
			isInvalid =  true;
		else if (getParamAsBoolean("CORP_CHANGE_SELECTION_ALL_BLOCK", "FALSE")
				&& subscriber.getUserType().equalsIgnoreCase(WebServiceConstants.CORPORATE))
		{
			String language=subscriber.getLanguage();
			Subscriber callerSub = getSubscriber(vst.callerID());
			if(getParamAsBoolean("USE_DND_SMS_URL","FALSE") && !isProcess)
				sendSMSviaPromoTool(callerSub, getSMSText("GATHERER",
					"CORP_COPY_CONTENT_SMS", m_corpCopyContentSMS, language));
			else
				sendSMS(callerSub, getSMSText("GATHERER", "CORP_COPY_CONTENT_SMS",m_corpCopyContentSMS,language));
			isInvalid =  true;
		}

		return isInvalid;
	}
	
	static public Subscriber activateSubscriber(String strSubID, String subCircleID, String calledTo, String strActBy,
			boolean isPrepaid, String classType, String strActInfo, String selectedBy, String cosId, String viralSmsType,String confMode)
	{

		String virtualNumberConfig = getParamAsString("VIRTUAL_NUMBERS", calledTo, null);
		if(virtualNumberConfig != null)
		{
			String circleID = null;
			String subClass = null;
			String[] tokens = virtualNumberConfig.split(","); // value : wavFile,SubscriptionClass,circleId

			if (tokens.length >= 2)
				subClass = tokens[1];  
			if (tokens.length >= 3)
				circleID = tokens[2];

			if (subCircleID == null)
			{
				MNPContext mnpContext = new MNPContext(strSubID, "COPY");
				mnpContext.setOnlineDip(getParamAsBoolean("COPY_ONLINE_DIP", "FALSE"));
				SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(mnpContext);
				if (subscriberDetail != null)
					subCircleID = subscriberDetail.getCircleID();
			}
			if (subCircleID != null && subCircleID.equalsIgnoreCase(circleID))
			{
				//subscriber called a virtual number and belongs to the same circle. hence overriding the charge class
				classType = subClass;
			}
		}

		HashMap<String, String> extraInfo = new HashMap<String, String>();
		if(selectedBy == null || selectedBy.contains("PRESSSTAR") || selectedBy.contains("XCOPY"))
		{	
			extraInfo.put(REFUND, "TRUE");
		}
		if(viralSmsType != null && viralSmsType.equalsIgnoreCase(COPYCONFIRMED))
		{
			extraInfo.put(EXTRA_INFO_COPY_TYPE, EXTRA_INFO_COPY_TYPE_OPTIN);
		}
		if(confMode!=null&&!confMode.equalsIgnoreCase("-"))
		{
			extraInfo.put(EXTRA_INFO_COPY_MODE, confMode);

		}
		SubscriptionBean subBean=new SubscriptionBean();
		subBean.setSubId(strSubID);
		subBean.setIsPrepaid(isPrepaid);
		subBean.setExtraInfo(extraInfo);
		if(cosId != null)
			subBean.setCosId(cosId);
		else 
			subBean.setSubcriptionClass(classType);
		StringBuffer strBuff=new StringBuffer();


		return rbtConnector.getSubscriberRbtclient().activateSubscriber(subBean, strBuff, strActBy, strActInfo);
	}
	static public boolean updateExtraInfoAndPlayerStatus(String subscriberId,boolean pollOn){
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
	}

	static private Category getClipCopyDetails(String clipID, StringBuffer wavFileBuf, StringBuffer catTokenBuf, StringBuffer catNameBuf, StringBuffer classTypeBuffer, StringBuffer statusBuf, StringBuffer setForCallerbuf, boolean isVirtualNo )
	{
		logger.info("clipID="+clipID+", wavFileBuf"+wavFileBuf+", catTokenBuf="+catTokenBuf+", catNameBuf"+catNameBuf+", classTypeBuffer="+classTypeBuffer+", statusBuf="+statusBuf+", setForCallerbuf="+setForCallerbuf);
		Category category = null;
		StringTokenizer stk = new StringTokenizer(clipID, ":");
		if (stk.hasMoreTokens())
			wavFileBuf.append(stk.nextToken());
		if (stk.hasMoreTokens())
		{
			String catToken = stk.nextToken();
			if (catToken.startsWith("S"))
				catToken = catToken.substring(1);
			try
			{
				category = rbtConnector.getMemCache().getCategory(Integer.valueOf(catToken));
				if(!isVirtualNo && getParamAsBoolean("COPY_SHUFFLE_SONG_ONLY","FALSE"))
				{
					if(category != null)
					{
						 if(category.getCategoryTpe() != iRBTConstant.SHUFFLE || category.getCategoryTpe() != iRBTConstant.WEEKLY_SHUFFLE 
								 || category.getCategoryTpe() != iRBTConstant.MONTHLY_SHUFFLE || category.getCategoryTpe() != iRBTConstant.DAILY_SHUFFLE 
								 || category.getCategoryTpe() != iRBTConstant.DYNAMIC_SHUFFLE || category.getCategoryTpe() != iRBTConstant.ODA_SHUFFLE
								 || category.getCategoryTpe() != iRBTConstant.TIME_OF_DAY_SHUFFLE || category.getCategoryTpe() != iRBTConstant.BOX_OFFICE_SHUFFLE
								 || category.getCategoryTpe() != iRBTConstant.FESTIVAL_SHUFFLE || category.getCategoryTpe() != iRBTConstant.FEED_SHUFFLE
								 || category.getCategoryTpe() != iRBTConstant.MONTHLY_ODA_SHUFFLE || category.getCategoryTpe() != iRBTConstant.OVERRIDE_MONTHLY_SHUFFLE 
								 || category.getCategoryTpe() != iRBTConstant.PLAYLIST_ODA_SHUFFLE)
						 {
							 category = null;
						 }
					}
				}
				if(category == null || category.getCategoryEndTime() == null || category.getCategoryEndTime().before(Calendar.getInstance().getTime()))
					category = rbtConnector.getMemCache().getCategory(26);
			}
			catch(Exception e)
			{
				category = null;
			}
			catTokenBuf.append(catToken);
			getCategoryCharge(catToken, classTypeBuffer, catNameBuf,category);
		}
		if (stk.hasMoreTokens())
		{
			StringTokenizer stkStatus = new StringTokenizer(stk.nextToken(),"|");
			if(stkStatus.hasMoreTokens())
				statusBuf.append(stkStatus.nextToken());
			if(stkStatus.hasMoreTokens())
				setForCallerbuf.append(stkStatus.nextToken());
		}
		return category;
	}
	static private void getCategoryCharge(String categoryID, StringBuffer classType, StringBuffer catName, Category category)
	{
		int amount = 0;
		String amt = null;
		if(category == null)
			return;
		if(category.getCategoryTpe() == 5 || category.getCategoryTpe() == 7 )
		{
			ChargeClass chargeClass = rbtConnector.getRbtGenericCache().getChargeClass(category.getClassType());

			if(chargeClass != null)
				amt = chargeClass.getAmount();

			try
			{
				amount = Integer.parseInt(amt.trim());
			}
			catch(Exception e)
			{
				amount = 0;
			}
			if(amount > copyAmount)
				classType.append(category.getClassType());
		} 
		logger.info("classType="+classType);
		return;
	}
	static private Clip getClipRBT(String strWavFile)
	{
		// return rbtDBManager.getClipRBT(strWavFile);
		return rbtConnector.getMemCache().getClipByRbtWavFileName(strWavFile);
	}
	
	static private boolean isNonCopyContent(String clipID, String catID, Clip clip, int status, String wavFile,Category category, boolean isVirtualNo)
	{
		logger.info("clipID="+clipID+", catID="+catID+", clip="+clip+", status="+status+", wavFile="+wavFile);
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String currentTime = sdf.format(currentDate);
		if(wavFile != null && wavFile.equalsIgnoreCase("MISSING"))
		{
			return true;
		}
		if(clipID != null && clipID.toUpperCase().indexOf("DEFAULT_" + currentTime) != -1)
		{
			if(!getParamAsBoolean("ALLOW_POLL_COPY","FALSE")){
				return true;
			}else{
				return false;
			}
		}
		if((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1) && !(getParamAsBoolean("COPY_DEFAULT","FALSE") || getParamAsBoolean("INSERT_DEFAULT_SEL","FALSE")))
		{
			return true;
		}	
		if (clip == null && status != 90)
		{
			return true;
		}
		if(Arrays.asList(getParamAsString("GATHERER", "COPY_BLOCKED_CATEGORY_IDS", "1,99").split(",")).contains(catID))
			return true;
		if(clip != null && Arrays.asList(getParamAsString("GATHERER", "COPY_BLOCKED_CLIP_IDS", "").split(",")).contains(""+clip.getClipId()))
			return true;
		else if (clip != null && clip.getClipEndTime() != null && clip.getClipEndTime().getTime() < System .currentTimeMillis())
			return true;
		else if (category!= null && !getParamAsBoolean("COPY_SHUFFLE","FALSE") && !isVirtualNo && (category.getCategoryTpe() == iRBTConstant.SHUFFLE || 
				category.getCategoryTpe() == iRBTConstant.WEEKLY_SHUFFLE 
				 || category.getCategoryTpe() == iRBTConstant.MONTHLY_SHUFFLE || category.getCategoryTpe() == iRBTConstant.DAILY_SHUFFLE 
				 || category.getCategoryTpe() != iRBTConstant.DYNAMIC_SHUFFLE || category.getCategoryTpe() == iRBTConstant.ODA_SHUFFLE
				 || category.getCategoryTpe() == iRBTConstant.TIME_OF_DAY_SHUFFLE || category.getCategoryTpe() == iRBTConstant.FESTIVAL_SHUFFLE
				 || category.getCategoryTpe() != iRBTConstant.FEED_SHUFFLE || category.getCategoryTpe() == iRBTConstant.BOX_OFFICE_SHUFFLE
				 || category.getCategoryTpe() == iRBTConstant.MONTHLY_ODA_SHUFFLE || category.getCategoryTpe() != iRBTConstant.PLAYLIST_ODA_SHUFFLE))
			return true;
		else if (clipID!=null && !isShuffleCategory(catID) && !getParamAsBoolean("IS_NORMAL_COPY_ALLOWED","TRUE"))
			return true;
		else if (clip != null && "EMOTION_UGC".equalsIgnoreCase(clip.getContentType()))
		{
			return true;
		}
		else if (status != 1 && status != 75 && status != 79 && status != 80 && status != 81 && status != 90
				&& status != 91 && status != 92 && status != 95)
			return true;
		return false;
	}

	static public void getDefaultClip()
	{
		int defaultClipId = -1;
		Clip clip = null;
		defaultClipId = getParamAsInt("COMMON", "DEFAULT_CLIP", -1);
		if( defaultClipId > -1 )
			clip = rbtConnector.getMemCache().getClip(defaultClipId);
		if(clip != null)
			defaultClipWavName = clip.getClipRbtWavFile();
	}

	static private void copyFailed(ViralSMSTable vst, String reason, String keyPressed,String confMode)
	{	
		String copyType=DEFAULTCOPY;
		Subscriber sub = getSubscriber(vst.subID());
		if(vst.type().equalsIgnoreCase(COPY) || vst.type().equalsIgnoreCase(COPYCONFIRM))
		copyType=DIRECTCOPY;
		
		else if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
			copyType=OPTINCOPY;
		if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
			int clipId = -1;
			try{
				clipId = Integer.parseInt(vst.clipID());
			}catch(NumberFormatException e){
				clipId = -1;
			}
			try {
				eventLogger.copyTrans(vst.subID(), vst.callerID(), "-", m_localType, 
						"-", "-", vst.sentTime(),copyType, keyPressed,reason,  vst.clipID(),confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
			} catch (ReportingException e) {
			}
		}
		if(getParamAsBoolean("WRITE_TRANS","FALSE"))
		{
			writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-", Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), m_localType, " - ", "-",reason,keyPressed,copyType,confMode);
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type());
		}
		else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type(), reason, null);
	}
	
	static private void initializeEventLogger(){
		try
		{
			Configuration cfg = new Configuration(m_eventLoggingDir);
			eventLogger = new EventLogger(cfg);
		
		}
		catch(Exception e)
		{
	
		}
	}
	
	static public boolean init()
	{
		rbtConnector=RBTConnector.getInstance();

		ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
		String m_dbURL = resourceBundle.getString("DB_URL");
		// Changes done for URL Encryption and Decryption
		try {
			if (resourceBundle.getString("ENCRYPTION_MODEL") != null
					&& resourceBundle.getString("ENCRYPTION_MODEL")
							.equalsIgnoreCase("yes")) {
				m_dbURL = URLEncryptDecryptUtil.decryptAndMerge(m_dbURL);
			}
		} catch (MissingResourceException e) {
			logger.error("resource bundle exception: ENCRYPTION_MODEL");
		}
		// End of URL Encryption and Decryption
		String poolSizeStr = resourceBundle.getString("DB_POOL_SIZE");
		int poolSize = 4;
		if (poolSizeStr != null)
		{
			try
			{
				poolSize = Integer.parseInt(poolSizeStr);
			}
			catch (Exception e)
			{
				poolSize = 4;
			}
		}
		rbtDBManager = RBTDBManager.init(m_dbURL, poolSize);
		String path = getParamAsString(iRBTConstant.WEBSERVICE, "PPL_CONTENT_REJECTION_LOG_PATH", null);
		if(path != null)
		{
			File file = new File(path);
			if(!file.exists())
				file.mkdirs();
			try {
				pplContentRejectionLogger = new PPLContentRejectionLogger(new Configuration(path));
			} catch (IOException e) {
				logger.error("Error while creating PPL Content Rejection logs",e);
			}
		}
		
		activeclassTypeParam = getParamAsString("GATHERER", "DEFAULT_COPY_CHARGECLASS_FOR_ACTIVE_SUBS", null);
		inactiveclassTypeParam = getParamAsString("GATHERER", "DEFAULT_COPY_CHARGECLASS_FOR_INACTIVE_SUBS", null);
		initCopyChargeClassMap();
		initNovaRdcOperatorMap();
		getDefaultClip();
		initCopyAmountAndChargeClass();
		initCommonGateway();
		initInterCircleurl();
		initInterOperator();
		initBuddyNetUrlConfig();
		initCrossOperator();
		initShuffles();
		
		if(getParamAsBoolean("COPY_CRICKET_SEL", "FALSE"))
			initializeFeed();

		if(getParamAsString("GATHERER_PATH") != null && getParamAsBoolean("WRITE_TRANS", "FALSE"))
		{
			m_transDir = getParamAsString("GATHERER_PATH") + "/Trans";
			new File(m_transDir).mkdirs();
		}
		if(getParamAsString("GATHERER_PATH") != null && getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE"))
		{
			m_eventLoggingDir = getParamAsString("GATHERER_PATH") + "/EventLogs";
			new File(m_eventLoggingDir).mkdirs();
		}
		normalCopyKeys = tokenizeArrayList(getParamAsString("COMMON","NORMALCOPY_KEY", null), ",");
		starCopyKeys = tokenizeArrayList(getParamAsString("COMMON","STARCOPY_KEY", null), ",");
		return true;
	}
	
	private static void initShuffles()
	{
		categoryTypeList = new ArrayList<Integer>();
		categoryTypeList.add(SHUFFLE);
		categoryTypeList.add(WEEKLY_SHUFFLE);
		categoryTypeList.add(MONTHLY_SHUFFLE);
		categoryTypeList.add(DAILY_SHUFFLE);
		categoryTypeList.add(DYNAMIC_SHUFFLE);
		categoryTypeList.add(ODA_SHUFFLE);
		categoryTypeList.add(TIME_OF_DAY_SHUFFLE);
		categoryTypeList.add(BOX_OFFICE_SHUFFLE);
		categoryTypeList.add(FESTIVAL_SHUFFLE);
		categoryTypeList.add(FEED_SHUFFLE);
		categoryTypeList.add(MONTHLY_ODA_SHUFFLE);
		categoryTypeList.add(PLAYLIST_ODA_SHUFFLE);
	}

	static public void initCopyChargeClassMap()
	{
		String strCopyChargeClassMap = getParamAsString("COPY_CHARGE_CLASS_MAP");
		logger.info("strCopyChargeClassMap="+strCopyChargeClassMap);
		if (strCopyChargeClassMap != null && strCopyChargeClassMap.length() > 0) 
		{
			strCopyChargeClassMap = strCopyChargeClassMap.trim().toUpperCase();
			StringTokenizer stkMap = new StringTokenizer(strCopyChargeClassMap,";");
			while(stkMap.hasMoreTokens())
			{
				String singleMapping = stkMap.nextToken().trim();
				StringTokenizer singleClassMap = new StringTokenizer(singleMapping, ",");
				String classType = null;
				if(singleClassMap.hasMoreTokens())
					classType = singleClassMap.nextToken().trim();
				if(classType == null && rbtConnector.getRbtGenericCache().getChargeClass(classType) == null)
				{
					continue;
				}
				while(singleClassMap.hasMoreTokens())
					copyChargeClassMap.put(singleClassMap.nextToken().trim(), classType);
			}	
		}
	}
	
	public static void initNovaRdcOperatorMap() {
		logger.info("Entering initNovaRdcOperatorMap");
		String operatorMap = getParamAsString("RDC_NOVA_OPERATOR_MAP");
		if (operatorMap != null && operatorMap.length() > 0) {
			operatorMap = operatorMap.trim().toUpperCase();
			StringTokenizer stkMap = new StringTokenizer(operatorMap,";");
			while (stkMap.hasMoreTokens()) {
				String rdcNovaOperator = stkMap.nextToken().trim();
				StringTokenizer singleClassMap = new StringTokenizer(
						rdcNovaOperator, ",");
				String rdcOperator = null;
				if (singleClassMap.hasMoreTokens())
					rdcOperator = singleClassMap.nextToken().trim();
				
				while (singleClassMap.hasMoreTokens())
					m_RdcNovaOperatorMap.put(rdcOperator,singleClassMap.nextToken().trim());
			}
		}
		logger.info("Exiting. operatorMap is "
				+ m_RdcNovaOperatorMap);
	}
	
	static public void initBuddyNetUrlConfig(){
		String urlInfoString = getParamAsString("BUDDY_NET_URL");
		if(urlInfoString != null)
		{	
			StringTokenizer strTokenizer = new StringTokenizer(urlInfoString, ",");
			if (strTokenizer.hasMoreTokens()) 
			{
				String token = strTokenizer.nextToken();
				m_redirectBuddyNet = (token!=null && (token.equalsIgnoreCase("true") || token.equalsIgnoreCase("on")));
			}

			//http://:PORT/mca_buddynet_det/GetBuddyDet.do?platform=CRBT
			if (strTokenizer.hasMoreTokens())
				m_buddynetUrl = strTokenizer.nextToken();
			if (strTokenizer.hasMoreTokens())
				m_buddynetUseProxy = strTokenizer.nextToken().trim().equalsIgnoreCase("true");
			if (strTokenizer.hasMoreTokens())
				m_buddynetProxyHost = strTokenizer.nextToken().trim();
			try 
			{
				if (strTokenizer.hasMoreTokens())
					m_buddynetProxyPort = Integer.parseInt(strTokenizer.nextToken().trim());
			}
			catch (Exception e) 
			{	
				m_buddynetProxyPort = -1;
			}
		}
	}
	static private void initializeFeed()
	{
		schedule=rbtConnector.getSubscriberRbtclient().getFeed("CRICKET","SP", "GATHERER");
		if (schedule == null)
		{
			ArrayList<Feed> fs = rbtConnector.getSubscriberRbtclient().getFeeds("CRICKET","SP");
			if(fs != null && fs.size() > 0)
				schedule = fs.get(0);
		}
	}
	
	static public void writeTrans(String subid, String callerID,
			String song, String cat, String req_time, String type,
			String isSubscribed, String success, String smsType, String keyPressed ,String copyType,String confMode)
	{
		try
		{
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(subid).append(",");
			strBuilder.append(callerID).append(",");
			strBuilder.append(song).append(",");
			strBuilder.append(cat).append(",");
			strBuilder.append(req_time).append(",");
			strBuilder.append(type).append(",");
			strBuilder.append(isSubscribed).append(",");
			strBuilder.append(success).append(",");
			strBuilder.append(smsType).append(",");
			strBuilder.append(keyPressed).append(",");
			strBuilder.append(copyType).append(",");
			strBuilder.append(confMode);
			
			CopyTransLogger.getLogger().info(strBuilder.toString());
		}
		catch(Exception e)
		{
			logger.error("Error in logging copy trans", e);
		}
	}

	static public String removeViralPromotion(String subscriberID, String callerID,
			Date sentTime, String type) 
	{
		boolean result = rbtConnector.getSubscriberRbtclient().removeViralData(subscriberID, callerID, type, sentTime);
		String resultStr = "FAILURE";
		if(result)
			resultStr = "SUCCESS";
		return resultStr;
	}
	
	static public void updateViralPromotion(String subscriberID, String callerID,
			Date sentTime, String fType, String tType, String extraInfo)
	{
		rbtConnector.getSubscriberRbtclient().updateViralData(subscriberID, callerID,
				null, sentTime, fType, tType,null,null, extraInfo);
	}

	static public String getSMSText(String type,String subType,String defaultValue,String language){
		String smsText=CacheManagerUtil.getSmsTextCacheManager().getSmsText(type, subType, language);
		if(smsText!=null)
			return smsText;
		else
			return defaultValue;
	}

	static private void sendSMS(Subscriber subscriber, String sms)
	{
		try
		{
			if(sms != null){
				String subscriptionClassOperatorNameMap = getParamAsString(COMMON, "SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);
				String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumberbyType("GATHERER", subscriber.getCircleID(), "SENDER_NO");
				String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getBrandName(subscriber.getCircleID());
				if(subscriptionClassOperatorNameMap != null){
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%SENDER_NO", senderNumber);
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%BRAND_NAME", brandName);
				}				
				Tools.sendSMS(senderNumber, subscriber.getSubscriberID(), sms, false);
			}
		}
		catch (Exception e)
		{
	
		}
	}

	static private String prepareCrossOperatorContentMissingSmsText(String called, String crossOperatorName, String clipName,String language)
	{
		String sms = getSMSText("GATHERER","CROSS_COPY_CONTENT_MISSING_SMS_TEXT",m_crossCopyContentMissingSmsText,language);
		if(called == null || called.length() <= 0)
			called = "";
		sms = Tools.findNReplace(sms, "CALLED_ID", called);
		if(clipName != null)
			sms = Tools.findNReplace(sms, "SONG_NAME", clipName);
		else
			sms = Tools.findNReplace(sms, "SONG_NAME", "");
		if(crossOperatorName != null)
			sms = Tools.findNReplace(sms, "OPERATOR_NAME", crossOperatorName);
		else
			sms = sms.replace("OPERATOR_NAME","");
		return sms;
	}
	
	
	static private SubscriberPromo getSubscriberPromo(String strSubID)
	{
		return rbtDBManager.getSubscriberPromo(strSubID,"YOUTHCARD");
	}
	
	static private boolean isSubAlreadyActiveOnStatus(String strSubID,
			String callerID, int status)
	{
		SubscriberStatus subStatus =rbtDBManager.getActiveSubscriberRecord(strSubID, callerID, status, 0, 2359);

		if (subStatus != null)
			return true;

		return false;
	}

	static private String getActivationAmount(String subscriberID, Subscriber subscriber, String mode) {
		String subscriptionClass = "DEFAULT";
		String prepaidYes = "n";
		if (getParamAsString("GATHERER","DEFAULT_SUBTYPE","pre").equalsIgnoreCase("pre"))
			prepaidYes = "y";
		CosDetails cosDetail = rbtDBManager.getCos(subscriberID, subscriber.getCircleID(),prepaidYes , mode);
		if (cosDetail == null)
		{
			//	Subscriber subscriber = rbtConnector.getSubscriberRbtclient().getSubscriber(subscriberID, "GATHERER");
			if (subscriber != null)
				subscriptionClass = subscriber.getSubscriptionClass();
		}
		else
		{
			subscriptionClass = cosDetail.getSubscriptionClass();
		}
		if(getParamAsString("GATHERER","COPY_SUB_CLASS", "COPY") != null)
			subscriptionClass = getParamAsString("GATHERER", "COPY_SUB_CLASS", "COPY");
		SubscriptionClass sClass = rbtConnector.getRbtGenericCache().getSubscriptionClassByName(subscriptionClass);
		return sClass.getSubscriptionAmount();
	}

	static private String getSelectionAmount(String subscriberID, Subscriber subscriber, String mode, String classType) {
		
		return getSelectionAmount(subscriberID, subscriber, mode, classType, false) ;
	}
	
	static private String getSelectionAmount(String subscriberID, Subscriber subscriber, String mode, String classType , boolean useUiChargeclass) {
		String chargeClass = rbtDBManager.getNextChargeClass(subscriberID);
		if(classType == null)
			classType = "DEFAULT"; 
		if (chargeClass == null){
			boolean prepaidYes = false;
			if (getParamAsString("GATHERER","DEFAULT_SUBTYPE","pre").equalsIgnoreCase("pre"))
				prepaidYes = true;

			Cos cosDetail =rbtConnector.getSubscriberRbtclient().getCos(subscriber.getCircleID(), prepaidYes, subscriberID, mode);
			chargeClass = getChargeClassFromCos(cosDetail, 0);
		}
		if (chargeClass == null){
			chargeClass = classType;
		}
		else if (chargeClass.equalsIgnoreCase("DEFAULT"))
			chargeClass = classType;
		if(chargeClass == null)
			chargeClass = "DEFAULT";
		if(useUiChargeclass)
			chargeClass = classType;
		ChargeClass cClass = rbtConnector.getRbtGenericCache().getChargeClass(chargeClass);
		return cClass.getAmount();
	}

	static public String getChargeClassFromCos(Cos cosObject, int selCount)
	{
		logger.info("cosObject="+cosObject+", selcount="+selCount);
		if(cosObject == null || cosObject.getChargeClass() == null)
			return null;
		StringTokenizer stk = new StringTokenizer(cosObject.getChargeClass(), ",");
		int countTokens = stk.countTokens();
		selCount = selCount % countTokens;
		for(int i = 0; i < selCount; i++)
			stk.nextToken();
		return stk.nextToken();	
	}

	static private boolean isShufflePresentSelection(String subID, String callerID)
	{
		boolean isShufflePresent = rbtDBManager.isShufflePresentSelection(subID,callerID);
		logger.info("isShufflePresent="+isShufflePresent);
		return isShufflePresent;
	}

	static public boolean isBuddyNetUsers(String subscriberId,Subscriber sub){
		boolean returnFlag=false;
		if(subscriberId!=null){
			if(m_redirectBuddyNet){
				try {
					HttpParameters httpParameters = new HttpParameters();
					//http://:PORT/mca_buddynet_det/GetBuddyDet.do?platform=CRBT&mdn=9241034453&chain=TC06OT
					//m_buddynetUrl
					String url=m_buddynetUrl+"&mdn="+subscriberId.trim();
					String wds=null;
					String wdsFinal=null;
					if(sub!=null){
						wds=sub.getOperatorUserInfo();
							if(wds==null){
								HashMap<String,String> extraInfoStr=sub.getUserInfoMap();
								if(extraInfoStr!=null && extraInfoStr.size()>0 && extraInfoStr.containsKey(EXTRA_INFO_WDS_QUERY_RESULT)){
									wds=extraInfoStr.get(EXTRA_INFO_WDS_QUERY_RESULT);
								}
							}
							if(wds!=null){
								wds=wds.trim();
								if(wds.indexOf("|")!=-1){
									StringTokenizer st=new StringTokenizer(wds,"#");
									int count=0;
									while(st.hasMoreTokens()){
										++count;
										String temp=st.nextToken();
										if(count==11){
											wdsFinal=temp;
											break;
										}
									}
								}
								if(wdsFinal!=null){
									wdsFinal=wdsFinal.trim();
									url=url+"&chain="+wdsFinal;
								}else{
									url=url+"&chain=";
								}
							}
						
					}
					if(wdsFinal==null){
						url=url+"&chain=";
					}
					httpParameters.setUrl(url);
					httpParameters.setUseProxy(m_buddynetUseProxy);
					httpParameters.setProxyHost(m_buddynetProxyHost);
					httpParameters.setProxyPort(m_buddynetProxyPort);
					httpParameters.setSoTimeout(15000);
					httpParameters.setConnectionTimeout(15000);
					HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParameters, null, null); // Check with Response String
					if(httpResponse!=null){
					}else{
					}
					if(httpResponse != null && httpResponse.getResponseCode() == 200 && httpResponse.getResponse()!=null ){
						String responseStr=httpResponse.getResponse().trim();
						if(responseStr!=null){
							if(responseStr.indexOf("|")!=-1){
								StringTokenizer st=new StringTokenizer(responseStr,"|");
								int count=0;
								String buddyNetRes=null;
								while(st.hasMoreTokens()){
									String temp=st.nextToken();
									++count;
									if(count==3){
										buddyNetRes=temp;
										break;
									}
								}
								if(buddyNetRes!=null && !buddyNetRes.trim().equalsIgnoreCase("") && !buddyNetRes.trim().equalsIgnoreCase("null") && buddyNetRes.trim().indexOf(";")!=-1){
									returnFlag=true;
								}
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return returnFlag;
	}
	
	static private void sendPressStarSMS(Subscriber subscriber, String sms)
	{
		try
		{
			if(sms != null){
				String subscriptionClassOperatorNameMap = getParamAsString(COMMON, "SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);
				String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumberbyType("GATHERER", subscriber.getCircleID(), "STAR_OBTAIN_SENDER_NO");
				String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getBrandName(subscriber.getCircleID());
				if(subscriptionClassOperatorNameMap != null){
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%SENDER_NO", senderNumber);
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%BRAND_NAME", brandName);
				}
				Tools.sendSMS(senderNumber, subscriber.getSubscriberID(), sms, false);
			}
		}
		catch (Exception e)
		{
			
		}
	}
	
	static private void sendSMSviaPromoTool(Subscriber subscriber, String sms) 
	{
		try {
			if (sms != null){
				String subscriptionClassOperatorNameMap = getParamAsString(COMMON, "SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);
				String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumberbyType("GATHERER", subscriber.getCircleID(), "STAR_OBTAIN_SENDER_NO");
				String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getBrandName(subscriber.getCircleID());
				if(subscriptionClassOperatorNameMap != null){
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%SENDER_NO", senderNumber);
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%BRAND_NAME", brandName);
				}
								
				Tools.sendSMS(senderNumber, subscriber.getSubscriberID(), sms);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	static public int getIntegerValue(String countStr)
	{
		int count = 0;
		if(countStr == null)
			return count;
		try
		{
			count = Integer.parseInt(countStr);
		}
		catch(Exception e)
		{
			count = 0;
		}
		return count;
	}

	static public String addSelections(String strSubID, String strCallerID, boolean isPrepaid, int categoryID, 
			Clip clip, String strSelectedBy, String strSelectionInfo, String classType,  
			String subClass, String selBy, String viralSmsType, boolean isCallerSubscribed, String rbtTypeStr, String cosIdStr, 
			int status, HashMap<String, String> subscriberInfoMap, String confMode,boolean useUiChargeClass)
	{
		logger.info("strSubID="+strSubID+", strCallerID="+strCallerID+", isPrepaid="+isPrepaid+", categoryID="+
				categoryID+", clip="+clip+", strSelectedBy="+strSelectedBy+", strSelectionInfo="+strSelectionInfo
				+", classType="+classType+", subClass="+subClass+", selBy="+selBy+", viralSmsType="+viralSmsType
				+", isCallerSubscribed="+isCallerSubscribed+", rbtTypeStr="+rbtTypeStr+", cosIdStr="+cosIdStr+", status="+status+
				", subscriberInfoMap="+subscriberInfoMap);
		HashMap<String, String> extraInfo = new HashMap<String, String>();
		if(selBy == null || selBy.contains("PRESSSTAR") || selBy.contains("XCOPY"))
		{	
			extraInfo.put(REFUND, "TRUE");
		}
		if(viralSmsType != null && viralSmsType.equalsIgnoreCase(COPYCONFIRMED))
		{	
			extraInfo.put(EXTRA_INFO_COPY_TYPE, EXTRA_INFO_COPY_TYPE_OPTIN);
		}
		if(confMode!=null&&!confMode.equalsIgnoreCase("-"))
		{
			extraInfo.put(EXTRA_INFO_COPY_MODE, confMode);

		}


		SelectionRequestBean selBean=new SelectionRequestBean();
		if(useUiChargeClass)
			selBean.setUseUIChargeClass(true);
		selBean.setSubscriberId(strSubID);
		selBean.setCallerId(strCallerID);
		selBean.setPrepaid(isPrepaid);
		selBean.setCatId(""+categoryID);
		selBean.setToneId(""+clip.getClipId());
		selBean.setStatus(1);
		selBean.setChargeClass(classType);
		selBean.setSubscriptionClass(subClass);
		selBean.setExtraInfo(extraInfo);
		selBean.setSubscriberExtraInfo(subscriberInfoMap);
		if(rbtDBManager.allowLooping() && getParamAsBoolean("ADD_COPY_SEL_IN_LOOP", "FALSE")){
			selBean.setSetInLoop("true");
		}
		if (clip != null && clip.getContentType() != null && clip.getContentType().equalsIgnoreCase("EMOTION_RBT")) // Default emotion song also should have contentType 'EMOTION_RBT'
		{
			selBean.setMmContext(WebServiceConstants.EMOTION_RBT);
		}
		if (status == 94)
		{
			selBean.setMmContext(WebServiceConstants.EMOTION_RBT);
			selBean.setSetInLoop("false"); // Emotion song will be added in override mode
			selBean.setProfileHour(String.valueOf(12));
		}
		
		StringBuffer responseBuff=new StringBuffer();
		rbtConnector.getSubscriberRbtclient().makeSelection(selBean, strSelectedBy, strSelectionInfo, responseBuff,isCallerSubscribed, rbtTypeStr, cosIdStr, strSelectedBy);
		String responseTemp="FAILURE";
		if(responseBuff!=null && responseBuff.length()>0){
			responseTemp=responseBuff.toString();
			if(responseTemp!=null ){
				responseTemp=responseTemp.trim();
				responseTemp=responseTemp.toUpperCase();
			}
		}

		return responseTemp;
	}

	static public void updateSubscription(String strSubID, int buddySelCount)
	{
		SubscriptionBean subBean=new SubscriptionBean();
		subBean.setSubId(strSubID);
		HashMap<String, String> infoMap = new HashMap<String, String>();
		infoMap.put("BUDDY_SEL", ""+buddySelCount);
		subBean.setExtraInfo(infoMap);
		rbtConnector.getSubscriberRbtclient().updateSubscription(subBean);
	}

	static private void removeCopyViralPromotion(String subscriberID, String callerID,
			Date sentTime)
	{
		logger.info("subscriberID="+subscriberID+", callerID="+callerID+", sentTime="+sentTime);
		rbtDBManager.removeCopyViralPromotion(subscriberID, callerID, sentTime);
	}
	
	static private void updateCopyViralPromotion(String subscriberID, String callerID,
			Date sentTime, String tType, String extraInfo)
	{
		logger.info("subscriberID="+subscriberID+", callerID="+callerID+", sentTime="+sentTime+", tType="+tType+", extraInfo="+extraInfo);
		rbtDBManager.updateCopyViralPromotion(subscriberID, callerID, sentTime,
				tType,
				new Date(System.currentTimeMillis()),
				null, extraInfo);
	}

	public static String processExpiredCopy(ViralSMSTable vst) {
		
		if(getParamAsBoolean("UPLOAD_PENDING_COPY_REQUESTS", "FALSE"))
		{
				prepareAndSendXml(vst);
				//logRecord();
		}
		else
		{	
				copyExpired(vst, m_localType);
			
		}
		return null;
	}

	public static String processFailedCopy(String line) {

		String[] strAr = line.split(",");
		if (strAr.length < 2) {
			return null;
		}
		String strTime = strAr[1];
		Date time = null;
		try {
			time = sdf.parse(strTime);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		String copyContent = strAr[0];
		StringTokenizer st = new StringTokenizer(copyContent, ":");
		String subscriberID = null;
		String callerID = null;
		String rbtWavFile = null;
		String catId = null;
		String status = null;
		if (st.hasMoreTokens())
			subscriberID = st.nextToken();
		if (st.hasMoreTokens())
			callerID = st.nextToken();
		if (st.hasMoreTokens())
			rbtWavFile = st.nextToken();
		if (st.hasMoreTokens())
			catId = st.nextToken();
		if (st.hasMoreTokens())
			status = st.nextToken();
		
		// checking whether subscriber (callerID) belongs to circle or not
		Subscriber subscriber = getSubscriber(callerID);
		if (subscriber == null) {
			return null;
		}
		boolean isValidSub = subscriber.isValidPrefix();
		if (!isValidSub) {
			return null;
		}
		// checking whether subscriber (callerID) made another selection
		// after this request.
		Settings settings = rbtConnector.getSubscriberRbtclient().getSettings(callerID);
		Setting[] settingsArr = settings.getSettings();
		boolean hasMadeAnotherSelection = false;
		for (Setting setting : settingsArr) {
			if (WebServiceConstants.ALL.equalsIgnoreCase(setting.getCallerID())) {
				Date setTime = setting.getSetTime();
				if (setTime.after(time) || setTime.equals(time)) {
					hasMadeAnotherSelection = true;
					break;
				}
			}
		}
		if (hasMadeAnotherSelection) {
			return null;
		}
		String clipId = rbtWavFile + ":" + catId + ":" + status;
		rbtConnector.getSubscriberRbtclient().addViralData(subscriberID, callerID, "COPY", clipId, "RETRY",null);

		return null;
	}
	public static void processRRBTCopy(ViralSMSTableImpl vst) 
	{
		vst.setStartTime(Calendar.getInstance().getTime());
		RBTNode node = RBTMonitorManager.getInstance().startNode(
				vst.callerID(), RBTNode.NODE_COPY_PROCESSOR);
		String nodeResponse = RBTNode.RESPONSE_FAILURE;
		Subscriber sub = getSubscriber(vst.subID());
		String extraInfoStr = vst.extraInfo();
		HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
		String keyPressed = "NA";
		String copyType=DEFAULTCOPY;
		String confMode="-";

		if(viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
			keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
		try {
			String caller = vst.callerID();
			logger.info("RRBT subscriber_id=" + vst.subID()
					+ "|caller_id=" + caller + "|clipID=" + vst.clipID()
					+ "|sentTime=" + vst.sentTime() + "|selBy="
					+ vst.selectedBy() + "|tryCount=" + vst.count());

			String subTypeRegion = "UNKNOWN";
			boolean isVirtualNo=false;
			if(virtualNumbers!=null && virtualNumbers.size()>0)
			{
				if(virtualNumbers.contains(vst.subID()))
				{		isVirtualNo=true;
					logger.info("match found");
				}	
			}
			else
			{
				logger.info("No Virtual numbers found");
			}
			logger.info("is Virtal no "+isVirtualNo+" caller "+caller+" subsc" + vst.subID()+" key "+keyPressed);
			if (!isVirtualNo||caller == null
					|| caller.length() < getParamAsInt(
							"PHONE_NUMBER_LENGTH_MIN", 10)
							|| caller.length() > getParamAsInt(
									"PHONE_NUMBER_LENGTH_MAX", 10)||keyPressed==null||vst.subID()==null) {
				removeViralPromotion(vst.subID(), vst.callerID(), vst
						.sentTime(), vst.type());
				if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
					try {
						eventLogger.copyTrans(vst.subID(), vst.callerID(), "-", subTypeRegion, "-", "-",
								vst.sentTime(),copyType, keyPressed,RRBTCOPYFAILED, vst.clipID(),confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
					} catch (ReportingException e) {
					}
				}
				if (getParamAsBoolean("WRITE_TRANS","FALSE")){
									writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
						Tools.getFormattedDate(vst.sentTime(),
						"yyyy-MM-dd HH:mm:ss"), subTypeRegion, " - ",
						"-", RRBTCOPYFAILED, keyPressed,copyType,confMode);
				}
				return;
			}

			String rrbtUrl=getParamAsString("RRBT_SYSTEM_URL");//Gathrer
			if(rrbtUrl!=null&&!rrbtUrl.equalsIgnoreCase(""))
			{
				try {
					rrbtUrl = rrbtUrl.replaceAll("<%keypressed%>", keyPressed);
					String details=vst.subID()+":"+vst.callerID()+":"+vst.clipID();
					rrbtUrl = rrbtUrl.replaceAll("<%details%>", details);
					Integer statusInt = new Integer(-1);
					StringBuffer result = new StringBuffer();
					logger.info("RBT:: UmpUrl: " + rrbtUrl);
					boolean success=Tools.callURL(rrbtUrl, statusInt, result, false , null,-1);
					if(!success)
						return;
					removeViralPromotion(vst.subID(), vst.callerID(), vst
							.sentTime(), vst.type());
					if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(), "-", subTypeRegion, "-", "-",
								vst.sentTime(),copyType, keyPressed,RRBTCOPYREQUESTED, vst.clipID(),confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
						} catch (ReportingException e) {
						}
					}
					if (getParamAsBoolean("WRITE_TRANS","FALSE")){
						writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
							Tools.getFormattedDate(vst.sentTime(),
							"yyyy-MM-dd HH:mm:ss"), subTypeRegion,
							" - ", "-", RRBTCOPYREQUESTED, keyPressed,copyType,confMode);
					}
					return ;

				} catch (Exception e) {
					if (vst.count() == 0)
						logger.error("", e);
					else
						logger.info(e.getMessage());
					vst.setCount(vst.count() + 1);
					rbtDBManager.setSearchCountCopy(vst.subID(), vst.type(), vst.count(), vst.sentTime(), vst.callerID());
					if(getParamAsInt("MAX_COPY_RETRY_COUNT", -1) == -1 || vst.count() <= getParamAsInt("MAX_COPY_RETRY_COUNT", -1))
						throw new RetryableException(e.getMessage());
					else
						throw new Exception(e.getMessage());
				}
			}	


		} catch (Throwable e) {
			logger.error("", e);
		} finally {
			RBTMonitorManager.getInstance().endNode(vst.callerID(), node,
					nodeResponse);
			
		}
	}


	public static String processOptinCopy(ViralSMSTable vst)
	{
		logger.info("vst="+vst);
		Subscriber subscriber = getSubscriber(vst.callerID());
		String language = subscriber.getLanguage();
		String sms = null;
		String callerCircleID = subscriber.getCircleID();
		Clip clip = null;
		Category category = null; 
		String clipID = vst.clipID();
		int cat = 26;
		int status = 1;
		String wavFile = null;
		boolean optin=false;
		boolean useUiChargeClass = false;
		String selectedBy = vst.selectedBy();
		Subscriber sub = getSubscriber(vst.subID());
		StringBuffer wavFileBuf = new StringBuffer();
		StringBuffer catTokenBuf = new StringBuffer();
		StringBuffer catNameBuf = new StringBuffer();
		StringBuffer classTypeBuffer = new StringBuffer();
		StringBuffer statusBuf = new StringBuffer();
		StringBuffer setForCallerBuf = new StringBuffer();
		String songName = "";
		String classType = m_copyClassType; // TODO change 
		String actAmt = null;
		String selAmt = null;
		String caller = vst.callerID();
		String called = vst.subID();
		String crossOperatorName = null;
		String extraInfoStr = vst.extraInfo();
		
		HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
		String sourceClipName = "";
		String keyPressed = "NA";
		String copyType=DEFAULTCOPY;
		String confMode="-";
		if(vst.type().equalsIgnoreCase(COPY) || vst.type().equalsIgnoreCase(COPYCONFIRM))
			{
			copyType=DIRECTCOPY;
			keyPressed = "s9";
			}
		else if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
			{
			copyType=OPTINCOPY;
			keyPressed = "s";
			}
		if(viralInfoMap != null && viralInfoMap.containsKey(SOURCE_WAV_FILE_ATTR))
			sourceClipName = viralInfoMap.get(SOURCE_WAV_FILE_ATTR);
		if(viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
			keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
		if(viralInfoMap != null && viralInfoMap.containsKey(COPY_CONFIRM_MODE_KEY))
			confMode = viralInfoMap.get(COPY_CONFIRM_MODE_KEY);
		if (isInvalidCopy(subscriber, vst , true))
		{
			copyFailed(vst, "INVALIDCOPY",keyPressed,confMode);
			return "INVALIDCOPY";
		}
		
		if(getParamAsBoolean("SHOW_OPERATOR_NAME_CROSS_OPERATOR_SMS","FALSE") && selectedBy != null && selectedBy.indexOf("XCOPY") != -1)
		{
			crossOperatorName = selectedBy.substring(0,selectedBy.indexOf("_"));
		}
		
		if(selectedBy != null && !selectedBy.equalsIgnoreCase("null"))
			selectedBy = selectedBy.trim().toUpperCase();
		else 
			selectedBy = "PRESSSTAR";

		boolean isVirtualNo = false;
		if (copyVirtualNumbers != null && copyVirtualNumbers.size() > 0)
		{
			if (copyVirtualNumbers.contains(called))
			{
				isVirtualNo = true;
				logger.info("match found");
			}
		}

		if(clipID != null)
			category = getClipCopyDetails(clipID,wavFileBuf,catTokenBuf,catNameBuf,classTypeBuffer,  statusBuf, setForCallerBuf, isVirtualNo );
		if(wavFileBuf != null && wavFileBuf.toString().trim().length() > 0 && wavFileBuf.toString().trim().indexOf(">") != -1)
		{
			cat = Integer.parseInt(wavFileBuf.toString().substring(wavFileBuf.indexOf(">")+1));
			wavFileBuf.delete(wavFileBuf.indexOf(">"),wavFileBuf.length());
			category = rbtConnector.getMemCache().getCategory(cat);
			if(category != null)
			{
				catNameBuf = new StringBuffer(category.getCategoryName());
			}
		}
		if(category != null)
			cat = category.getCategoryId();
		if((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1) && defaultClipWavName != null)  // TODO defaultClipWavName
		{
			wavFileBuf = new StringBuffer(defaultClipWavName);
		}	


		if(classTypeBuffer != null && classTypeBuffer.toString().trim().length() > 0)
			classType = classTypeBuffer.toString().trim();

		try
		{
			status = Integer.parseInt(statusBuf.toString().trim());
		}
		catch(Exception e)

		{
			status  = 1;
		}
		wavFile = wavFileBuf.toString().trim();

		if (wavFile != null && wavFile.length() > 0 && status != 90 && status != 99)
			clip = getClipRBT(wavFile);
		String finalSelectedBy="PRESSSTAR";
		if(copyChargeClassMap != null && copyChargeClassMap.size() > 0 && copyChargeClassMap.containsKey(selectedBy))
			classType = (String)copyChargeClassMap.get(selectedBy);
		if(selectedBy != null && selectedBy.indexOf("_XCOPY")== -1)
			finalSelectedBy = selectedBy;
		if(getParamAsBoolean("USE_DEFAULT_ACT_SEL_BY", "TRUE"))
			finalSelectedBy  = "COPY";
		String selByOptInCopy = getParamAsString("MODE_FOR_OPTIN_COPY");
		if(selByOptInCopy != null)
		{
			if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
				finalSelectedBy = selByOptInCopy;
		}
		selectedBy = "|CP:"+selectedBy+"-"+vst.subID()+":CP|";

		if(Utility.isSubActive(subscriber) && activeclassTypeParam != null)
		{
			useUiChargeClass = true;
			classType = activeclassTypeParam;
		}
		else if(!Utility.isSubActive(subscriber) && inactiveclassTypeParam != null)
		{
			useUiChargeClass = true;
			classType = inactiveclassTypeParam;
		}
		
		actAmt = getActivationAmount(caller, subscriber, finalSelectedBy);
		selAmt = getSelectionAmount(caller, subscriber, finalSelectedBy,classType, useUiChargeClass);

		if(isNonCopyContent(clipID, catTokenBuf.toString(), clip, status, wavFile, category, isVirtualNo))
		{
			if(getParamAsBoolean("SEND_CROSS_COPY_CONTENT_MISSING_SMS","FALSE") && wavFile != null && wavFile.indexOf("MISSING") != -1){
				if(getParamAsBoolean("USE_DND_SMS_URL","FALSE"))
					sendSMSviaPromoTool(subscriber, prepareCrossOperatorContentMissingSmsText(
							called, crossOperatorName, sourceClipName, language));
				else
					sendSMS(subscriber, prepareCrossOperatorContentMissingSmsText(called, crossOperatorName, sourceClipName,language));
			}
			else if (getParamAsBoolean("NON_COPY_SENT_SMS","FALSE"))
			{ 
				if(clip != null && clip.getClipEndTime() != null && clip.getClipEndTime().getTime() < System.currentTimeMillis()) 
				{
					HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
					smsTextmap.put("SMS_TEXT", getSMSText("GATHERER","NON_COPY_EXPIRED_CLIP_SMS",m_nonCopyExpiredClipSMS,language));
					smsTextmap.put("CALLED_ID", called);
					smsTextmap.put("SONG_NAME", clip.getClipName());
					smsTextmap.put("CLIP_OBJECT", clip);
					smsTextmap.put("CATEGORY_OBJECT", category);

					if(getParamAsBoolean("USE_DND_SMS_URL","FALSE"))
						sendSMSviaPromoTool(subscriber, finalizeSmsText(smsTextmap));
					else
						sendSMS(subscriber, finalizeSmsText(smsTextmap)); 
				}
				else {
					if(getParamAsBoolean("USE_DND_SMS_URL","FALSE"))
						sendSMSviaPromoTool(subscriber, getSMSText("GATHERER","NON_COPY_CONTENT_SMS","Sorry this selection cannot be copied",language));
					else
						sendSMS(subscriber, getSMSText("GATHERER","NON_COPY_CONTENT_SMS","Sorry this selection cannot be copied",language));
				}
			}
			copyFailed(vst, "NONCOPY",keyPressed,confMode);
			return "NONCOPY";
		}	
	
		if(clip != null && clip.getClipName() != null)
			songName  = clip.getClipName();
		if(getParamAsBoolean("COPY_SHUFFLE_SONG_ONLY","FALSE") && category != null && isShuffleCategory(""+category.getCategoryId()))
			songName = category.getCategoryName();
		if(status == 90)
			songName = "Cricket Feed";
	    if (category != null&& categoryTypeList.contains(category.getCategoryTpe())
				&& category.getCategoryEndTime().after(new Date())&&getParamAsBoolean("COPY_SHUFFLE", "FALSE")&&!getParamAsBoolean("COPY_SHUFFLE_SONG_ONLY", "FALSE"))
			songName=category.getCategoryName();
	
		if (isSubActive(subscriber) && !getParamAsBoolean("IS_OPT_IN_FOR_ACTIVE_SUB","FALSE")){
			updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFIRMED, null);
		}else{
			optin=true;
			HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
			smsTextmap.put("SMS_TEXT", getSMSText("GATHERER","NON_COPY_EXPIRED_CLIP_SMS",m_nonCopyExpiredClipSMS,language));
			smsTextmap.put("CALLED_ID", called);
			smsTextmap.put("SONG_NAME", clip.getClipName());
			smsTextmap.put("CLIP_OBJECT", clip);
			smsTextmap.put("CATEGORY_OBJECT", category);
			if(actAmt != null)
					actAmt = getInLocalCurrencyFormat(actAmt);
			smsTextmap.put("ACT_AMOUNT", actAmt);
			if(selAmt != null)
					selAmt = getInLocalCurrencyFormat(selAmt);
			smsTextmap.put("SEL_AMOUNT", selAmt);
			smsTextmap.put("COPY_CONFIRM_KEYWORD", getParamAsString("SMS","COPY_CONFIRM_KEYWORD","COPYYES"));
			smsTextmap.put("COPY_CANCEL_KEYWORD", getParamAsString("SMS","COPY_CANCEL_KEYWORD","COPYNO"));
			smsTextmap.put("TIMEOUT", getParamAsString("GATHERER","WAIT_TIME_DOUBLE_CONFIRMATION",30+""));
			sms = finalizeSmsText(smsTextmap);
			
			updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFPENDING, null);
		}			
			
			if(sms != null && sms.trim().length() > 0 )
			{
				if(optin)
				{	//deactive caller case
					if (songName != null && !songName.equalsIgnoreCase(""))
					{
						com.onmobile.apps.ringbacktones.content.Subscriber sub1=rbtDBManager.getSubscriber(caller);
						SubscriberStatus[] substatus = null;
						SubscriberStatus substatus1 = null;
						if (sub1 != null)
						{
							substatus = rbtDBManager.getAllActiveSubSelectionRecords(caller);
							substatus1 = rbtDBManager.getAvailableSelection(null, 
								caller,null, substatus,rbtDBManager.getCategory(category.getCategoryId(), callerCircleID,'b'), 
								wavFile, status, 0,2359, clip.getClipStartTime(), clip.getClipEndTime(),false,
								(rbtDBManager.allowLooping() && rbtDBManager
										.isDefaultLoopOn()) , sub1.rbtType() , null, vst.selectedBy());
						}
						if(substatus1!=null || (substatus1 == null && !isUserHavingAllCallerIDSelection(subscriber, wavFile)))
						{	
							HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
							smsTextmap.put("SMS_TEXT", getSMSText("GATHERER", "OPT_IN_SAME_SONG_FAILURE", m_optInSameSongSms, language));
							smsTextmap.put("SONG_NAME", clip.getClipName());
							smsTextmap.put("CLIP_OBJECT", clip);
							smsTextmap.put("CATEGORY_OBJECT", category);
							sms = finalizeSmsText(smsTextmap);
							
							sendPressStarSMS(subscriber, sms);
							if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
								try {
									eventLogger.copyTrans(vst.subID(), vst.callerID(), isSubActive(subscriber)? "YES" : "NO", m_localType, catNameBuf.toString(), "FALSE",
								vst.sentTime(),copyType, keyPressed,DUPLICATE, wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
								} catch (ReportingException e) {
								}
							}
							if (getParamAsBoolean("WRITE_TRANS","FALSE")){
								writeTrans(vst.subID(), vst.callerID(), wavFile,
										catNameBuf.toString(), Tools
										.getFormattedDate(vst.sentTime(),
												"yyyy-MM-dd HH:mm:ss"),
												m_localType, isSubActive(subscriber)? "YES" : "NO",
														 "FALSE",DUPLICATE,keyPressed,copyType,confMode);
							}
								removeViralPromotion(vst.subID(), vst.callerID(), vst
										.sentTime(),COPYCONFPENDING);
								return "SUCCESS";
							}

						}
					if (!isUserHavingAllCallerIDSelection(subscriber, wavFile))
					{
						logger.info("User not having all callerID selection and the current selection is default");
						HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
						smsTextmap.put("SMS_TEXT", getSMSText("GATHERER", "OPT_IN_SAME_SONG_FAILURE", m_optInSameSongSms, language));
						smsTextmap.put("CALLED_ID", "");
						smsTextmap.put("SONG_NAME", songName);
						smsTextmap.put("CLIP_OBJECT", clip);
						smsTextmap.put("CATEGORY_OBJECT", category);
						sms = finalizeSmsText(smsTextmap);
						
						if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
							try {
								eventLogger.copyTrans(vst.subID(), vst.callerID(), isSubActive(subscriber)? "YES" : "NO", m_localType, catNameBuf.toString(), "FALSE",
								vst.sentTime(),copyType, keyPressed,DUPLICATE, wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
							} catch (ReportingException e) {
							}
						}
						if (getParamAsBoolean("WRITE_TRANS","FALSE")){
							writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf.toString(), 
								Tools.getFormattedDate(vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								m_localType, isSubActive(subscriber) ? "YES" : "NO", "FALSE", DUPLICATE, keyPressed, copyType, confMode);
						}
						removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), COPYCONFPENDING);
					}
					
					
				}

				if(getParamAsBoolean("USE_UMP_URL","FALSE"))
				{	
					if(optin)
					{
						
						String umpUrl=getParamAsString("UMP_GATEWAY_URL");//Gathrer
						if(umpUrl!=null&&!umpUrl.equalsIgnoreCase(""))
						{
							try {
								umpUrl = umpUrl.replaceAll("<%msisdn%>", caller);
								umpUrl = umpUrl.replaceAll("<%smstext%>", getEncodedUrlString(sms));
								Integer statusInt = new Integer(-1);
								StringBuffer result = new StringBuffer();
								logger.info("RBT:: UmpUrl: " + umpUrl);
								Tools.callURL(umpUrl, statusInt, result, false , null,-1);
								return "SUCCESS";

							} catch (Exception e) {
								logger.error("", e);
								e.printStackTrace();
							}
						}	
					}
				}	
				sendPressStarSMS(subscriber, sms);
			}
			return "SUCCESS";
	}

	public static String processOptoutConfirmCopy(ViralSMSTable vst)
	{
		return null;
	}
	

	
	private static boolean isUserHavingAllCallerIDSelection(Subscriber subscriber, String wavFile)
	{
		boolean allowMakeSelection = true; 
		SubscriberStatus[] subscriberStatus = rbtDBManager.getAllActiveSubscriberSettings(subscriber.getSubscriberID());
		boolean isHavingAllCallerIDSelection = false;
		if (subscriberStatus != null && subscriberStatus.length != 0)
		{
			for(SubscriberStatus subStatus : subscriberStatus)
			{
				if (subStatus.callerID() == null || subStatus.callerID().equalsIgnoreCase(WebServiceConstants.ALL))
				{
					isHavingAllCallerIDSelection = true;
					break;
				}
			}
		}

		if (isSubActive(subscriber) 
				&& (subscriberStatus == null || subscriberStatus.length == 0 || !isHavingAllCallerIDSelection)
				&& getParamAsBoolean("INSERT_DEFAULT_SEL", "FALSE")
				&& defaultClipWavName != null && wavFile.equalsIgnoreCase(defaultClipWavName))
		{
			allowMakeSelection = false;
		}
		return allowMakeSelection;
	}

	public static String processOptoutCopy(ViralSMSTable vst)
	{
		HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
		smsTextmap.put("SMS_TEXT", getSMSText("GATHERER","PRESS_STAR_CONFIRMATION_SMS",m_pressStarConfirmationSMS,null));
		smsTextmap.put("TIMEOUT", getParamAsString("GATHERER","WAIT_TIME_DOUBLE_CONFIRMATION",30+""));
		String sms = finalizeSmsText(smsTextmap);
		
		updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFIRM, null);
		
		if(sms != null && sms.trim().length() > 0 )
		{
				sendPressStarSMS(getSubscriber(vst.callerID()), sms);
		}
		return "SUCCESS";
	}

	public static String processStarCopy(ViralSMSTable vst)
	{
		logger.info("vst="+vst);

		Subscriber subscriber = getSubscriber(vst.callerID());
		String circleID = null;
		String callerCircleID = subscriber.getCircleID();
		String virtualNumberConfig = getParamAsString("VIRTUAL_NUMBERS", vst.subID(), null);
		String language = subscriber.getLanguage();
		String sms = null;
		Clip clip = null;
		Category category = null; 
		boolean optin=false;
		boolean useUiChargeClass = false;
		String clipID = vst.clipID();
		int cat = 26;
		int status = 1;
		String wavFile = null;
		String selectedBy = vst.selectedBy();
		StringBuffer wavFileBuf = new StringBuffer();
		StringBuffer catTokenBuf = new StringBuffer();
		StringBuffer catNameBuf = new StringBuffer();
		Subscriber sub = getSubscriber(vst.subID());
		StringBuffer classTypeBuffer = new StringBuffer();
		StringBuffer statusBuf = new StringBuffer();
		StringBuffer setForCallerBuf = new StringBuffer();
		String setForCaller = null;
		String songName = "";
		String classType = m_copyClassType; // TODO change 
		String actAmt = null;
		String selAmt = null;
		String caller = vst.callerID();
		String called = vst.subID();
		String crossOperatorName = null;
		String extraInfoStr = vst.extraInfo();
		
		HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
		String sourceClipName = "";
		String confMode="-";
		String keyPressed = "NA";
		String copyType=DEFAULTCOPY;
		if(vst.type().equalsIgnoreCase(COPY) || vst.type().equalsIgnoreCase(COPYCONFIRM))
			keyPressed = "s";
		else if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
			keyPressed = "s9";
		if(viralInfoMap != null && viralInfoMap.containsKey(SOURCE_WAV_FILE_ATTR))
			sourceClipName = viralInfoMap.get(SOURCE_WAV_FILE_ATTR);
		if(viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
			keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
		if(viralInfoMap != null && viralInfoMap.containsKey(COPY_CONFIRM_MODE_KEY))
			confMode = viralInfoMap.get(COPY_CONFIRM_MODE_KEY);
		
		if (isInvalidCopy(subscriber, vst , false))
		{
			copyFailed(vst, "INVALIDCOPY",keyPressed,confMode);
			return "INVALIDCOPY";
		}
		
		if(getParamAsBoolean("SHOW_OPERATOR_NAME_CROSS_OPERATOR_SMS","FALSE") && selectedBy != null && selectedBy.indexOf("XCOPY") != -1)
		{
			crossOperatorName = selectedBy.substring(0,selectedBy.indexOf("_"));
		}
		
		if(selectedBy != null && !selectedBy.equalsIgnoreCase("null"))
			selectedBy = selectedBy.trim().toUpperCase();
		else 
			selectedBy = "PRESSSTAR";

		boolean isVirtualNo = false;
		if (copyVirtualNumbers != null && copyVirtualNumbers.size() > 0)
		{
			if (copyVirtualNumbers.contains(called))
			{
				isVirtualNo = true;
				logger.info("match found");
			}
		}

		if(clipID != null)
			category = getClipCopyDetails(clipID,wavFileBuf,catTokenBuf,catNameBuf,classTypeBuffer,  statusBuf, setForCallerBuf, isVirtualNo );
		if(setForCallerBuf != null && setForCallerBuf.length() > 0)
			setForCaller = setForCallerBuf.toString();
		if(wavFileBuf != null && wavFileBuf.toString().trim().length() > 0 && wavFileBuf.toString().trim().indexOf(">") != -1)
		{
			cat = Integer.parseInt(wavFileBuf.toString().substring(wavFileBuf.indexOf(">")+1));
			wavFileBuf.delete(wavFileBuf.indexOf(">"),wavFileBuf.length());
			/*	String circleID = rbtDBManager.getCircleId(caller);
				char prepaidYes = 'n';
				if(m_prepaid)
					prepaidYes = 'y';
				Categories category = rbtDBManager.getCategory(cat, circleID, prepaidYes);
			 */
			category = rbtConnector.getMemCache().getCategory(cat);
			if(category != null)
			{
				catNameBuf = new StringBuffer(category.getCategoryName());
			}
		}
		if(category != null)
			cat = category.getCategoryId();
		if((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1) && defaultClipWavName != null)  // TODO defaultClipWavName
		{
			wavFileBuf = new StringBuffer(defaultClipWavName);
		}	


		if(classTypeBuffer != null && classTypeBuffer.toString().trim().length() > 0)
			classType = classTypeBuffer.toString().trim();

		try
		{
			status = Integer.parseInt(statusBuf.toString().trim());
		}
		catch(Exception e)

		{
			status  = 1;
		}
		wavFile = wavFileBuf.toString().trim();

		if (wavFile != null && wavFile.length() > 0 && status != 90 && status != 99)
			clip = getClipRBT(wavFile);
		String finalSelectedBy="PRESSSTAR";
		if(copyChargeClassMap != null && copyChargeClassMap.size() > 0 && copyChargeClassMap.containsKey(selectedBy))
			classType = (String)copyChargeClassMap.get(selectedBy);
		if(selectedBy != null && selectedBy.indexOf("_XCOPY")== -1)
			finalSelectedBy = selectedBy;
		if(getParamAsBoolean("USE_DEFAULT_ACT_SEL_BY", "TRUE"))
			finalSelectedBy  = "COPY";
		String selByOptInCopy = getParamAsString("MODE_FOR_OPTIN_COPY");
		if(selByOptInCopy != null)
		{
			if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
				finalSelectedBy = selByOptInCopy;
		}
		selectedBy = "|CP:"+selectedBy+"-"+vst.subID()+":CP|";


		if(Utility.isSubActive(subscriber) && activeclassTypeParam != null)
		{
			useUiChargeClass = true;
			classType = activeclassTypeParam;
		}
		else if(!Utility.isSubActive(subscriber) && inactiveclassTypeParam != null)
		{
			useUiChargeClass = true;
			classType = inactiveclassTypeParam;
		}
		
		actAmt = getActivationAmount(caller, subscriber, finalSelectedBy);
		selAmt = getSelectionAmount(caller, subscriber, finalSelectedBy,classType, useUiChargeClass);
		
		if(isNonCopyContent(clipID, catTokenBuf.toString(), clip, status, wavFile, category, isVirtualNo))
		{
			if(getParamAsBoolean("SEND_CROSS_COPY_CONTENT_MISSING_SMS","FALSE") && wavFile != null && wavFile.indexOf("MISSING") != -1)
				sendSMS(subscriber, prepareCrossOperatorContentMissingSmsText(called, crossOperatorName, sourceClipName,language));
			else if (getParamAsBoolean("NON_COPY_SENT_SMS","FALSE"))
			{ 
				if(clip != null && clip.getClipEndTime() != null && clip.getClipEndTime().getTime() < System.currentTimeMillis()) 
				{
					HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
					smsTextmap.put("SMS_TEXT", getSMSText("GATHERER","NON_COPY_EXPIRED_CLIP_SMS",m_nonCopyExpiredClipSMS,language));
					smsTextmap.put("CALLED_ID", "");
					smsTextmap.put("SONG_NAME",  clip.getClipName());
					smsTextmap.put("CLIP_OBJECT", clip);
					smsTextmap.put("CATEGORY_OBJECT", category);
					sms = finalizeSmsText(smsTextmap);
					
					sendSMS(subscriber, sms); 
				}
				else 
					sendSMS(subscriber, getSMSText("GATHERER","NON_COPY_CONTENT_SMS","Sorry this selection cannot be copied",language));
			}
			copyFailed(vst, "NONCOPY",keyPressed,confMode);
			return "NONCOPY";
		}	
		if(clip != null && clip.getClipName() != null)
			songName  = clip.getClipName();
		if(getParamAsBoolean("COPY_SHUFFLE_SONG_ONLY","FALSE") && category != null && isShuffleCategory(""+category.getCategoryId()))
			songName = category.getCategoryName();
		if(status == 90)
			songName = "Cricket Feed";
	    if (category != null&& categoryTypeList.contains(category.getCategoryTpe())
				&& category.getCategoryEndTime().after(new Date())&&getParamAsBoolean("COPY_SHUFFLE", "FALSE")&&!getParamAsBoolean("COPY_SHUFFLE_SONG_ONLY", "FALSE"))
			songName=category.getCategoryName();
	
		if(virtualNumberConfig != null)
		{
			String[] tokens = virtualNumberConfig.split(","); // value : wavFile,SubscriptionClass,circleId

			if (tokens.length >= 3)
				circleID = tokens[2];

		}
		if ((virtualNumberConfig != null && circleID == null) || (circleID != null && callerCircleID != null  && circleID.equalsIgnoreCase(callerCircleID)))
		{
			//subscriber called a virtual number and belongs to the same circle. hence copy will be processed as direct copy
			sms = null;
			updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFIRMED, null);

		}
		else
		{	
			boolean isActive = isSubActive(subscriber);
			boolean liteCondition = false;
			if(subscriber != null && isActive && clip != null)
			{
				String cosStr = subscriber.getCosID();
				String cosType = null;
				if(cosStr != null)
					cosType = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.getCosID()).getCosType();
				
				boolean isLite = (cosType != null
						&& cosType.equalsIgnoreCase(WebServiceConstants.COS_TYPE_LITE)
						&& clip.getContentType() != null
						&& !clip.getContentType().equalsIgnoreCase(WebServiceConstants.COS_TYPE_LITE));
				
				boolean isUDS = false;
				if(subscriber.getUserInfoMap() != null) {
					Map<String, String> userInfoMap = subscriber.getUserInfoMap();
					isUDS = userInfoMap.containsKey(UDS_OPTIN) && userInfoMap.get(UDS_OPTIN).equalsIgnoreCase("true");
					String blockedContentTypesStr = CacheManagerUtil.getParametersCacheManager()
							.getParameterValue(iRBTConstant.COMMON, "UDS_BLOCKED_CONTENT_TYPES", "");
					List<String> blockedContentTypesList = Arrays.asList(blockedContentTypesStr.split(","));
					isUDS = isUDS && clip.getContentType() != null
							&& blockedContentTypesList.contains(clip.getContentType());
				}
				
				if (isLite || isUDS)
				{
					if(vst.type().equalsIgnoreCase(COPY) || vst.type().equalsIgnoreCase(COPYCONFIRM))
						{
						copyType=DIRECTCOPY;
						keyPressed = "s9";
						}
					else if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
						{
						copyType=OPTINCOPY;
						keyPressed = "s";
						}
					if(viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
						keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
					if(viralInfoMap != null && viralInfoMap.containsKey(COPY_CONFIRM_MODE_KEY))
						confMode = viralInfoMap.get(COPY_CONFIRM_MODE_KEY);
					liteCondition = true;
					sms = getSMSText("LITE_USER","PREMIUM_BLOCKED",null,null);
					if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(), "NO", m_localType, "", "FALSE",
								vst.sentTime(),"N", keyPressed,PREMIUM_CONTENT, wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
						} catch (ReportingException e) {
						}
					}
					if (getParamAsBoolean("WRITE_TRANS","FALSE")){
						writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf.toString(), Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), m_localType, "NO",   "FALSE",PREMIUM_CONTENT,keyPressed,copyType,confMode);
					}
					removeCopyViralPromotion(vst.subID(), vst.callerID(), vst.sentTime());
				}
					
			}	
			
			if(!liteCondition)
			{
				String smsText = getSMSText("GATHERER", "OPT_IN_CONFIRMATION_ACT_SMS", m_optInConfirmationActSMS,null);
				optin=true;
				if (isActive)
					smsText = getSMSText("GATHERER", "OPT_IN_CONFIRMATION_SEL_SMS", m_optInConfirmationSelSMS,null);

				HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
				smsTextmap.put("SMS_TEXT", smsText);
				smsTextmap.put("CALLED_ID", called);
				smsTextmap.put("SONG_NAME",  clip.getClipName());
				smsTextmap.put("CLIP_OBJECT", clip);
				smsTextmap.put("CATEGORY_OBJECT", category);
				if(actAmt != null)
					actAmt = getInLocalCurrencyFormat(actAmt);
				smsTextmap.put("ACT_AMOUNT", actAmt);
				if(selAmt != null)
					selAmt = getInLocalCurrencyFormat(selAmt);
				smsTextmap.put("SEL_AMOUNT", selAmt);
				smsTextmap.put("COPY_CONFIRM_KEYWORD", getParamAsString("SMS","COPY_CONFIRM_KEYWORD","COPYYES"));
				smsTextmap.put("COPY_CANCEL_KEYWORD", getParamAsString("SMS","COPY_CANCEL_KEYWORD","COPYNO"));
				smsTextmap.put("TIMEOUT", getParamAsString("GATHERER","WAIT_TIME_DOUBLE_CONFIRMATION",30+""));
				sms = finalizeSmsText(smsTextmap);
				
				updateViralPromotion(vst.subID(),vst.callerID(),vst.sentTime(), vst.type(), COPYCONFPENDING, null);
			}
			else
			{
				if(pplContentRejectionLogger != null)
				{
					
					try {
						if(category != null && isShuffleCategory(category.getCategoryId()+""))
							pplContentRejectionLogger.PPLContentRejectionTransaction(vst.subID(), finalSelectedBy, "-1", category.getCategoryId()+"", new Date());
						else if(clip != null)
							pplContentRejectionLogger.PPLContentRejectionTransaction(vst.subID(), finalSelectedBy, clip.getClipId()+"" , "-1" , new Date());
					} catch (ReportingException e) {
						logger.error(e.getMessage(),e);
					}

				}
				if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER", "FALSE"))
				{
					DataRequest dataRequest = new DataRequest(caller, setForCaller, "SELCONFPENDING");
					HashMap<String, String> infoMap = new HashMap<String, String>();
					infoMap.put("CATEGORY_ID", String.valueOf(cat));
					dataRequest.setInfoMap(infoMap);
					if (!RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
							"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED", "FALSE"))
					{
						RBTClient.getInstance().removeViralData(dataRequest);
					}

					String OptInCopyMode = getParamAsString("MODE_FOR_OPTIN_COPY");
					if (OptInCopyMode != null)
					{
						finalSelectedBy = OptInCopyMode;
					}
					dataRequest.setMode(finalSelectedBy);
					Clip clipTemp = rbtConnector.getMemCache().getClipByRbtWavFileName(wavFile);
					if (clipTemp != null)
					{
						dataRequest.setClipID(String.valueOf(clipTemp.getClipId()));
					}
					RBTClient.getInstance().addViralData(dataRequest);
				}
			}
		}
		if(sms != null && sms.trim().length() > 0 )
		{	
			if(optin)
			{	//deactive caller case
				if (songName != null && !songName.equalsIgnoreCase(""))
				{	
					com.onmobile.apps.ringbacktones.content.Subscriber sub1 = rbtDBManager.getSubscriber(caller);
					SubscriberStatus[] substatus = null;
					SubscriberStatus substatus1 = null;
					if (sub1 != null)
					{
						substatus = rbtDBManager.getAllActiveSubSelectionRecords(caller);
						substatus1 = rbtDBManager.getAvailableSelection(null, 
			                caller,null, substatus,rbtDBManager.getCategory(category.getCategoryId(), callerCircleID,'b'), 
			                 wavFile, status, 0,2359, clip.getClipStartTime(), clip.getClipEndTime(),false,
			                 (rbtDBManager.allowLooping() && rbtDBManager
										.isDefaultLoopOn()) , sub1.rbtType() , null, vst.selectedBy());
					}
					if(substatus1!=null || (substatus1 == null && !isUserHavingAllCallerIDSelection(subscriber, wavFile)))
					{	
						HashMap<String, Object> smsTextmap = new HashMap<String, Object>();
						smsTextmap.put("SMS_TEXT", getSMSText("GATHERER", "OPT_IN_SAME_SONG_FAILURE", m_optInSameSongSms, language));
						smsTextmap.put("SONG_NAME", songName);
						smsTextmap.put("CLIP_OBJECT", clip);
						smsTextmap.put("CATEGORY_OBJECT", category);
						sms = finalizeSmsText(smsTextmap);
						
						sendPressStarSMS(subscriber, sms);
						if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
							try {
								eventLogger.copyTrans(vst.subID(), vst.callerID(), "NO", m_localType,"", "FALSE",
								vst.sentTime(),"N", keyPressed,DUPLICATE, wavFile,confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
							} catch (ReportingException e) {
							}
						}
						if (getParamAsBoolean("WRITE_TRANS","FALSE")){
							writeTrans(vst.subID(), vst.callerID(), wavFile,
									catNameBuf.toString(), Tools
									.getFormattedDate(vst.sentTime(),
											"yyyy-MM-dd HH:mm:ss"),
											m_localType, isSubActive(subscriber)? "YES" : "NO",
													 "FALSE",DUPLICATE,keyPressed,copyType,confMode);
						}
							removeViralPromotion(vst.subID(), vst.callerID(), vst
									.sentTime(),COPYCONFPENDING);
							return "SUCCESS";
						}

					}
				
				
			}
			if(getParamAsBoolean("USE_UMP_URL","FALSE"))
			{	
				if(optin)
				{
					
					String umpUrl=getParamAsString("UMP_GATEWAY_URL");//Gathrer
					if(umpUrl!=null&&!umpUrl.equalsIgnoreCase(""))
					{
						try {
							umpUrl = umpUrl.replaceAll("<%msisdn%>", caller);
							umpUrl = umpUrl.replaceAll("<%smstext%>", getEncodedUrlString(sms));
							Integer statusInt = new Integer(-1);
							StringBuffer result = new StringBuffer();
							logger.info("RBT:: UmpUrl: " + umpUrl);
							Tools.callURL(umpUrl, statusInt, result, false , null,-1);
							return "SUCCESS";

						} catch (Exception e) {
							logger.error("", e);
							e.printStackTrace();
						}
					}	
				}
			}
			sendPressStarSMS(subscriber, sms);
		}
		return "SUCCESS";
	}

	public static String processCommonGatewayCopyRequest(ViralSMSTableImpl vst,
			boolean b, Object object) throws Exception 
	{

		try
		{
			String url = m_nonOnmobileUrl;
			String vcode = vst.clipID();
			if(vcode != null && vcode.indexOf(":") != -1)
				vcode = vcode.substring(0,vcode.indexOf(":"));
			Clip clip = null; 
			if(vcode != null) 
				clip = getClipRBT(vcode); 
			String clipName = "";
			if(clip != null)
				clipName = clip.getClipName();
	
			url = url + "startcopy.jsp?called="+ vst.subID() + "&caller=" + vst.callerID()+"&clip_id=" + vcode+"&sms_type="+vst.type();
			if(url != null && clipName != null && !clipName.equalsIgnoreCase(""))
				url = url + "&songname="+getEncodedUrlString(clipName);
			String extraInfoStr = vst.extraInfo();
			HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
			String keypressed =  null;
			if(vst.type().equalsIgnoreCase(COPY))
				keypressed = "s";
			else if(vst.type().equalsIgnoreCase(COPYSTAR))
				keypressed = "s9";
			if(viralInfoMap != null)
				keypressed = viralInfoMap.get(KEYPRESSED_ATTR);
			
			if(keypressed != null && url != null)
			{
				if(keypressed.length() > 0 && !keypressed.equalsIgnoreCase("null"))
					url = url + "&keypressed="+getEncodedUrlString(keypressed);
			}	
	
			String sourceClipName = "";
			if(viralInfoMap != null && viralInfoMap.containsKey(SOURCE_WAV_FILE_ATTR))
				sourceClipName = viralInfoMap.get(SOURCE_WAV_FILE_ATTR);
			if(url != null && sourceClipName != null && !sourceClipName.equalsIgnoreCase("") && url.indexOf("songname") == -1)
				url = url + "&songname="+getEncodedUrlString(sourceClipName);
			logger.info("url is:"+url);
			rbtHttpClient.makeRequestByGet(url, null);
			logCopyrequest(vst,keypressed, m_nonOnmobileType);
			return "SUCCESS";
		}
		catch(Exception e)
		{
			if (vst.count() == 0)
				logger.error("", e);
			else
				logger.info(e.getMessage());
			vst.setCount(vst.count() + 1);
			rbtDBManager.setSearchCountCopy(vst.subID(), vst.type(), vst.count(), vst.sentTime(), vst.callerID());
			if(getParamAsInt("MAX_COPY_RETRY_COUNT", -1) == -1 || vst.count() <= getParamAsInt("MAX_COPY_RETRY_COUNT", -1))
				throw new RetryableException(e.getMessage());
			else
				throw new Exception(e.getMessage());
			}
	}

	public static void logCopyrequest(ViralSMSTable vst, String keyPressed, String subscriberType)
	{	
		String copyType=DEFAULTCOPY;
		String confMode="-";
		Subscriber sub = getSubscriber(vst.subID());
		if(vst.type().equalsIgnoreCase(COPY) || vst.type().equalsIgnoreCase(COPYCONFIRM))
			{
			copyType=DIRECTCOPY;
			}
		else if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
			{
			copyType=OPTINCOPY;
			}
		if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
			try {
				eventLogger.copyTrans(vst.subID(), vst.callerID(), "-", subscriberType, 
						"-", "-", vst.sentTime(),copyType, keyPressed,COPYFAILED,  vst.clipID(),confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
			} catch (ReportingException e) {
			}
		}
		if(getParamAsBoolean("WRITE_TRANS","FALSE"))
		{
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type());
			writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-", Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), subscriberType, " - ", "-",COPYFAILED,keyPressed,copyType,confMode);
		}
		else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), vst.type(), COPIED, null);	
	}

	public static String processIntraOperatorCopyRequest(ViralSMSTableImpl vst,
			boolean b, Object object) throws Exception
	{
		try
		{
			String url = urlMap.get(vst.getCircleId());
			String selBy = vst.selectedBy();
			url = Tools.findNReplaceAll(url,
				"rbt_sms.jsp", "");
			url = Tools.findNReplaceAll(url, "?", "");
			url = url + "rbt_copy.jsp?subscriber_id="+ vst.subID() + "&caller_id=" + vst.callerID()+"&clip_id=" + vst.clipID()+"&sel_by="+selBy+"&sms_type="+ vst.type();
			String extraInfoStr = vst.extraInfo();
			HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
			String keypressed =  null;
			if(vst.type().equalsIgnoreCase(COPY))
				keypressed = "s";
			else if(vst.type().equalsIgnoreCase(COPYSTAR))
				keypressed = "s9";
			if(viralInfoMap != null)
				keypressed = viralInfoMap.get(KEYPRESSED_ATTR);
			if(keypressed != null && url != null)
			{
				if(keypressed != null && keypressed.length() > 0 && !keypressed.equalsIgnoreCase("null"))
					url = url + "&keypressed="+getEncodedUrlString(keypressed);
			}	
	
			String sourceClipName = "";
			if(viralInfoMap != null && viralInfoMap.containsKey(SOURCE_WAV_FILE_ATTR))
				sourceClipName = viralInfoMap.get(SOURCE_WAV_FILE_ATTR);
			if(url != null && sourceClipName != null && !sourceClipName.equalsIgnoreCase("") && url.indexOf("songname") == -1)
				url = url + "&songname="+getEncodedUrlString(sourceClipName);
			logger.info("url is:"+url);
			rbtHttpClient.makeRequestByGet(url, null);
			logCopyrequest(vst,keypressed,m_nationalType);
			return "SUCCESS";
		}
		catch(Exception e)
		{
			if (vst.count() == 0)
				logger.error("", e);
			else
				logger.info(e.getMessage());
			vst.setCount(vst.count() + 1);
			rbtDBManager.setSearchCountCopy(vst.subID(), vst.type(), vst.count(), vst.sentTime(), vst.callerID());
			if(getParamAsInt("MAX_COPY_RETRY_COUNT", -1) == -1 || vst.count() <= getParamAsInt("MAX_COPY_RETRY_COUNT", -1))
				throw new RetryableException(e.getMessage());
			else
				throw new Exception(e.getMessage());}
	}

	public static String processInterOperatorCopyRequest(ViralSMSTableImpl vst,
			boolean b, Object object) throws Exception
	{
		try
		{
			String url = m_crossOperatorUrl;
			String selBy = vst.selectedBy();
			
			boolean isCentral = getParamAsBoolean("IS_CENTRAL_SITE","FALSE");
			String wavFile = null; 
			if(vst.clipID() != null) 
				wavFile = new StringTokenizer(vst.clipID(),":").nextToken().trim(); 
			Clip clip = null; 
			if(wavFile != null) 
				clip = getClipRBT(wavFile); 
			int clipID = -1; 
			if(clip != null) 
				clipID = clip.getClipId(); 
			String clipName = "";
			if(clip != null)
				clipName = clip.getClipName();
			String promoCode = "";
			if(clip != null)
				promoCode = clip.getClipPromoId();
			String finalClipID = vst.clipID(); 
			String extraInfoStr = vst.extraInfo();
			HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);

			if(isCentral)
			{

				finalClipID = clipID + ":" + wavFile;
				String sourceOp = getParamAsString(GATHERER,"SOURCE_OPERATOR", null);
				finalClipID=finalClipID + "&source_op=" + sourceOp;
				url = url + "rbt_rdc_copy_transfer.jsp?subscriber_id=" + vst.subID()
				+ "&caller_id=" + vst.callerID() + "&clip_id="
				+ finalClipID + "&sms_type=" + vst.type() ;
				
				String keypressed =  null;
				if(vst.type().equalsIgnoreCase(COPY))
					keypressed = "s";
				else if(vst.type().equalsIgnoreCase(COPYSTAR)) 
					keypressed = "s9";
				if(viralInfoMap != null)
					keypressed = viralInfoMap.get(KEYPRESSED_ATTR);
				
				if(keypressed != null && url != null)
				{
					if(keypressed.length() > 0 && !keypressed.equalsIgnoreCase("null"))
						url = url + "&keypressed="+getEncodedUrlString(keypressed);
					if(keypressed != null && keypressed.toLowerCase().indexOf("s") == -1)
					{
						logger.info("Inter-operator copy request failing for "+vst.callerID() + " as keypressed "+keypressed+" does not contain s");
						logCopyrequest(vst, keypressed, m_crossOperatorType);
						return "SUCCESS";
					}
				}	
				
				logger.info("url is:"+url);
				rbtHttpClient.makeRequestByGet(url, null);
				logCopyrequest(vst, keypressed, m_crossOperatorType);
				return "SUCCESS";	
				
			
			}
	
			url = url + "rbt_cross_copy.jsp?subscriber_id="+ vst.subID() + "&caller_id=" + vst.callerID()+"&clip_id=" + finalClipID+"&sms_type="+ vst.type()+"&sel_by="+selBy;
			if(url != null && clipName != null && !clipName.equalsIgnoreCase(""))
				url = url + "&songname="+getEncodedUrlString(clipName);
			if(url != null && promoCode != null)
				url = url + "&tonecode="+getEncodedUrlString(promoCode);
			String keypressed =  null;
			if(vst.type().equalsIgnoreCase(COPY))
				keypressed = "s";
			else if(vst.type().equalsIgnoreCase(COPYSTAR))
				keypressed = "s9";
			if(viralInfoMap != null)
				keypressed = viralInfoMap.get(KEYPRESSED_ATTR);
			
			if(keypressed != null && url != null)
			{
				if(keypressed.length() > 0 && !keypressed.equalsIgnoreCase("null"))
					url = url + "&keypressed="+getEncodedUrlString(keypressed);
			}	
	
			String sourceClipName = "";
			if(viralInfoMap != null && viralInfoMap.containsKey(SOURCE_WAV_FILE_ATTR))
				sourceClipName = viralInfoMap.get(SOURCE_WAV_FILE_ATTR);
			if(url != null && sourceClipName != null && !sourceClipName.equalsIgnoreCase("") && url.indexOf("songname") == -1)
				url = url + "&songname="+getEncodedUrlString(sourceClipName);
			logger.info("url is:"+url);
			rbtHttpClient.makeRequestByGet(url, null);
			logCopyrequest(vst, keypressed, m_crossOperatorType);
			return "SUCCESS";	
		}
		catch(Exception e)
		{
			if (vst.count() == 0)
				logger.error("", e);
			else
				logger.info(e.getMessage());
			vst.setCount(vst.count() + 1);
			rbtDBManager.setSearchCountCopy(vst.subID(), vst.type(), vst.count(), vst.sentTime(), vst.callerID());
			if(getParamAsInt("MAX_COPY_RETRY_COUNT", -1) == -1 || vst.count() <= getParamAsInt("MAX_COPY_RETRY_COUNT", -1))
				throw new RetryableException(e.getMessage());
			else
				throw new Exception(e.getMessage());}
	}
	
	static public boolean prepareAndSendXml(ViralSMSTable context)
	{
		String followupKeys = rbtConnector.getRbtGenericCache().getParameter("COMMON","OBD_FOLLOWUP_KEYS",null);
		if (followupKeys != null)
		{
			obdCopyKeys = Arrays.asList(followupKeys.split(","));
		}
		String fileName = System.currentTimeMillis()+"_copypending"+".xml";
		File file = null;
		FileOutputStream fos = null;

		try 
		{
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			Element element = document.createElement("rbt");
			document.appendChild(element);

			Element timestampElement = document.createElement("timestamp");
			timestampElement.setAttribute("time", new Date().toString());
			element.appendChild(timestampElement);	

			String countryPrefix = getParamAsString("COMMON", "COUNTRY_PREFIX", "91");
			
			boolean send=false;
			String keypressed=null;
			String extraInfoStr = context.extraInfo();
			HashMap<String, String> viralInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfoStr);
			String wavFileName = context.clipID();
			if (viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
			{
				keypressed=viralInfoMap.get(KEYPRESSED_ATTR);
				for(String key : obdCopyKeys)
				{
					if(keypressed.indexOf(key) != -1)
						send=true;
				}

			}
			if(!send)
			{
				return false;
			}
				if(wavFileName == null)
					return false;

				StringTokenizer tokenizer = new StringTokenizer(wavFileName, ":");
				if(tokenizer.hasMoreTokens())
					wavFileName = tokenizer.nextToken();

				Clip clip = getClipRBT(wavFileName);
				if(clip == null || clip.getClipPromoId() == null)
					return false;

				String subStatus = "active";
				Subscriber sub = rbtConnector.getSubscriberRbtclient().getSubscriber(context.callerID(),"GATHERER");
				if(sub.getStatus()!=null &&( sub.getStatus().equalsIgnoreCase(WebServiceConstants.ACT_PENDING) || sub.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE)))
					subStatus = "active";
				else
					subStatus = "deactive";

				Element requestElement = document.createElement("request");
				requestElement.setAttribute("msidn", countryPrefix+ context.callerID());
				requestElement.setAttribute("promoid", clip.getClipPromoId());
				if(keypressed!=null)
				{
					requestElement.setAttribute("keypressed", keypressed);
				}
				requestElement.setAttribute("time", context.sentTime()+"");
				requestElement.setAttribute("status", subStatus);

				element.appendChild(requestElement);	


			String xml = XMLUtils.getStringFromDocument(document);

			file = new File(getParamAsString("COPY_PENDING_FILE_PATH") + File.separator +fileName);
			
			byte[] bytes = xml.getBytes();
			int offset =0;
			int length = 8;
			fos = new FileOutputStream(file);
			while(offset < bytes.length)
			{
				fos.write(bytes, offset, length);
				offset = offset+8;
				if((offset + length) >= bytes.length)
					length = bytes.length - offset;
			}

			HttpParameters httpParameters = new HttpParameters();
			httpParameters.setUrl(getParamAsString("COPY_PENDING_UPLOAD_URL"));
			httpParameters.setUseProxy(getParamAsBoolean("COPY_PENDING_USE_PROXY", "FALSE"));
			httpParameters.setProxyHost(getParamAsString("COPY_PENDING_PROXY_HOST"));
			httpParameters.setProxyPort(getParamAsInt("COPY_PENDING_PROXY_PORT", 80));
			httpParameters.setSoTimeout(getParamAsInt("COPY_PENDING_CONNECTION_TIMEOUT",15000));
			httpParameters.setConnectionTimeout(getParamAsInt("COPY_PENDING_CONNECTION_TIMEOUT",15000));

			HashMap<String, File> fileParams = new HashMap<String, File>();
			fileParams.put("xml", file);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParameters, null, fileParams); // Check with Response String

			if(httpResponse != null && httpResponse.getResponseCode() == 200 && httpResponse.getResponse().trim().equalsIgnoreCase("200"))
				return true;

		} 
		catch (ParserConfigurationException e)
		{
		}
		catch(Exception e)
		{
		}
		catch(Error e)
		{
			throw e;
		}
		finally
		{
			//	if(file != null && file.exists())
			//		file.delete();
			try
			{
				if(fos != null)
					fos.close();
			}catch(Exception e){}
		}
		return false;
	}
	
	static public String getCalleeOperator(Subscriber subscriber , String selectedBy)
	{
		String circleId = null;
		String response = "NA";
		if(m_RdcNovaOperatorMap == null || (subscriber == null && selectedBy == null))
			return response;
		
		if (subscriber != null)
		{
			circleId = subscriber.getCircleID();
			if(circleId != null)
				response = m_RdcNovaOperatorMap.get(LOCAL_OPERATOR);
		}
		
		if(selectedBy != null && selectedBy.contains("XCOPY"))
		{
			String operator = selectedBy.split("_")[0];
			if(m_RdcNovaOperatorMap.containsKey(operator))
				response = m_RdcNovaOperatorMap.get(operator);
			else
				response = operator;
		}
		
		return response;
			
	}
	
	static public void copyExpired(ViralSMSTable vst, String subType)
	{
		Subscriber subscriber = getSubscriber(vst.callerID());
		String wasActive = "YES";
		Subscriber sub = getSubscriber(vst.subID());
		if(isSubActive(subscriber))
			wasActive = "NO";
		HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(vst.extraInfo());
		String keyPressed = "NA";
		String copyType=DEFAULTCOPY;
		String confMode="-";
		if(vst.type().equalsIgnoreCase(COPY) || vst.type().equalsIgnoreCase(COPYCONFIRM))
			{
			copyType=DIRECTCOPY;
			keyPressed = "s9";
			}
		else if(vst.type().equalsIgnoreCase(COPYCONFIRMED))
			{
			copyType=OPTINCOPY;
			keyPressed = "s";
			}
		if(viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
			keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
		if(viralInfoMap != null && viralInfoMap.containsKey(COPY_CONFIRM_MODE_KEY))
			confMode = viralInfoMap.get(COPY_CONFIRM_MODE_KEY);
		if(getParamAsBoolean("EVENT_MODEL_GATHERER","FALSE")){
			try {
				String wavFile = "-";
				int clipId = -1;
				Clip clip = null;
				StringTokenizer stk = new StringTokenizer(vst.clipID(), ":");
				if (stk.hasMoreTokens())
					wavFile = stk.nextToken();
				if (wavFile != null && wavFile.length() > 0 && !wavFile.equalsIgnoreCase("-"))
					clip = getClipRBT(wavFile);
				if (clip != null)
					clipId = clip.getClipId();
				eventLogger.copyTrans(vst.subID(), vst.callerID(), wasActive, subType, 
						"-", "FALSE", vst.sentTime(),copyType, keyPressed,COPYEXPIRED,  vst.clipID(),confMode,getCalleeOperator(sub,vst.selectedBy()),new Date());
			} catch (ReportingException e) {
			}
		}
		if(getParamAsBoolean("WRITE_TRANS","FALSE"))
		{
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), COPYCONFPENDING);
			writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-", Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), subType,wasActive, "-",COPYEXPIRED,keyPressed,copyType,confMode);
		}
		else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), COPYCONFPENDING, COPYEXPIRED, null);
	}
	static private String getEncodedUrlString(String param)
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
	
	static void getSites()
	{
		sitesList.add(JUNK);
		initCommonGateway();
		initInterCircleurl();
		initOperator();
		logger.info("sitesList="+sitesList);
		logger.info("circleMap="+circleMap);
		logger.info("urlMap="+urlMap);
	}

	private static void initInterCircleurl()
	{
		String urlInfoString = getParamAsString("REDIRECT_NATIONAL_COPY");
		logger.info("urlInfoString="+urlInfoString);
		if(urlInfoString == null)
			return;
		StringTokenizer strTokenizer = new StringTokenizer(urlInfoString, ",");
		if (strTokenizer.hasMoreTokens()) 
		{
			String token = strTokenizer.nextToken();
			m_redirectNational = (token.equalsIgnoreCase("true") || token.equalsIgnoreCase("on"));
		}
		if (strTokenizer.hasMoreTokens())
			m_nationalUrl = strTokenizer.nextToken().trim();
		if (strTokenizer.hasMoreTokens())
			m_nationalUseProxy = strTokenizer.nextToken().trim().equalsIgnoreCase("true");
		if (strTokenizer.hasMoreTokens())
			m_nationalProxyHost = strTokenizer.nextToken().trim();
		try 
		{
			if (strTokenizer.hasMoreTokens())
				m_nationalProxyPort = Integer.parseInt(strTokenizer.nextToken().trim());
		}
		catch (Exception e) 
		{	
			m_nationalProxyPort = -1;
		}
	}

	private static void initOperator() 
	{
		List<SitePrefix> prefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		for (int i = 0; prefixes != null && prefixes.size() > 0 && i < prefixes.size(); i++)
		{
			if (prefixes.get(i).getSiteUrl() == null)
			{
				circleMap.put(prefixes.get(i).getCircleID(), "LOCAL");
				if(!sitesList.contains("LOCAL"))
					sitesList.add("LOCAL");
			}
			else 
			{
				if(m_nationalUrl != null && m_nationalUrl.equalsIgnoreCase(prefixes.get(i).getSiteUrl()))
				{
					circleMap.put(prefixes.get(i).getCircleID(), "OPERATOR");
					urlMap.put("OPERATOR", m_nationalUrl);
					if(!sitesList.contains(OPERATOR))
						sitesList.add(OPERATOR);
				}
				else
				{
					sitesList.add(prefixes.get(i).getCircleID());
					urlMap.put(prefixes.get(i).getCircleID(), prefixes.get(i).getSiteUrl());
				}
			}	
		}
		if(m_nationalUrl != null)
		{
			urlMap.put("OPERATOR", m_nationalUrl);
			if(!sitesList.contains(OPERATOR))
				sitesList.add(OPERATOR);
		}
		
	}

	private static void initInterOperator()
	{
		String urlInfoString = getParamAsString("RDC_URL");
		if(urlInfoString == null)
			return;
		StringTokenizer stk = new StringTokenizer(urlInfoString, ",");
		if(stk.hasMoreTokens())
		{
			String redirect = stk.nextToken();
			m_redirectCrossOperator = redirect.equalsIgnoreCase("TRUE") || redirect.equalsIgnoreCase("YES");
		}
		if(stk.hasMoreTokens())
			m_crossOperatorUrl = stk.nextToken().trim();
		if(stk.hasMoreTokens())
		{
			String useProxy = stk.nextToken();
			m_crossOperatorUseProxy = useProxy.equalsIgnoreCase("TRUE") || useProxy.equalsIgnoreCase("YES");
		}
		if(stk.hasMoreTokens())
			m_crossOperatorProxyHost = stk.nextToken().trim();
		if(stk.hasMoreTokens())
		{
			String proxyport = stk.nextToken();
			try
			{
				m_crossOperatorProxyPort = Integer.parseInt(proxyport);
			}
			catch(Exception e)
			{
				
			}
		}
		sitesList.add("CROSS");
	}

	private static void initCommonGateway()
	{
		String urlInfoString = getParamAsString("NON_ONMOBILE_URL");
		if(urlInfoString == null)
			return;
		StringTokenizer stk = new StringTokenizer(urlInfoString, ",");
		if(stk.hasMoreTokens())
		{
			String redirect = stk.nextToken();
			m_redirectNonOnmobile = redirect.equalsIgnoreCase("TRUE") || redirect.equalsIgnoreCase("YES");
		}
		if(stk.hasMoreTokens())
			m_nonOnmobileUrl = stk.nextToken().trim();
		if(stk.hasMoreTokens())
		{
			String useProxy = stk.nextToken();
			m_nonOnmobileUseProxy = useProxy.equalsIgnoreCase("TRUE") || useProxy.equalsIgnoreCase("YES");
		}
		if(stk.hasMoreTokens())
			m_nonOnmobileProxyHost = stk.nextToken().trim();
		if(stk.hasMoreTokens())
		{
			String proxyport = stk.nextToken();
			try
			{
				m_nonOnmobileProxyPort = Integer.parseInt(proxyport);
			}
			catch(Exception e)
			{
				
			}
		}
		sitesList.add("CGATE");
		logger.info("m_redirectNonOnmobile="+m_redirectNonOnmobile+", m_nonOnmobileUseProxy="
				+m_nonOnmobileUseProxy+", m_nonOnmobileProxyHost="+m_nonOnmobileProxyHost+", m_nonOnmobileProxyPort="+
				m_nonOnmobileProxyPort);
	}

	public static void updateCircleIdForCopyRecord(CIDQueueComponent cidQueueComponent, String circleId)
	{
		logger.info("cidQueueComponent="+cidQueueComponent+", circleId="+circleId);
		ViralSMSTableImpl vst =  cidQueueComponent.getViralSMSTableImpl();
		rbtConnector.getSubscriberRbtclient().updateViralData(vst.getSmsId(), circleId);	
		
	}

	public static String getCopyCircleId(String circleId, String subId)
	{
	    logger.info("circleId="+circleId+", subId="+subId);
		if(circleId == null)
	    {
			if(getParamAsBoolean("GATHERER", "CONSIDER_CROSS_OPR_AS_NON_ONMOBILE", "FALSE"))
				circleId = "CGATE";
			else if(isCrossOperatorAllowedPrefix(subId))
	        	circleId =  "CROSS";
	        else
	        	circleId =  JUNK;
	    }
	    else if (circleId.equalsIgnoreCase("NON_ONMOBILE"))
	    		circleId = "CGATE";
	    else if (circleId.equalsIgnoreCase("CENTRAL"))
    		circleId = OPERATOR;
    	if(circleId != null && circleMap.containsKey(circleId))
			return circleMap.get(circleId);
		logger.info("circleId="+circleId);
    	return circleId;
	}
	private static boolean isCrossOperatorAllowedPrefix(String subscriberId)
	{
		logger.info("subscriberId="+subscriberId);
		if(crossPrefixes.size() == 0)
			return true;
		if(subscriberId == null || subscriberId.trim().length() == 0)
			return false;
		for(int i = 0; i < crossPrefixes.size(); i++)
			if(subscriberId.startsWith(crossPrefixes.get(i)))
				return true;
		return false;
		
	}
	private static void initCrossOperator()
	{
		String crossPrefix = getParamAsString("CROSS_OPERATOR_PREFIX");
		logger.info("crossPrefix="+crossPrefix);
		if(crossPrefix != null)
		{
			StringTokenizer stk = new StringTokenizer(crossPrefix, ",");
			while(stk.hasMoreTokens())
				crossPrefixes.add(stk.nextToken());
		}
		logger.info("crossPrefixes="+crossPrefixes);
	}

	public static void markForManualReconcile(
			CIDQueueComponent cidQueueComponent)
	{
		logger.info("cidQueueComponent="+cidQueueComponent);
		ViralSMSTableImpl vst =  cidQueueComponent.getViralSMSTableImpl();
		rbtConnector.getSubscriberRbtclient().updateViralData(vst.subID(), vst.callerID(), null, vst.sentTime(), vst.type(), vst.type()+"_RECONCILE",null, null, null);
		
	}

    public static void markForManualReconciliation(ViralSMSTableImpl vst)
    {
        logger.info("vst="+vst);
    	String smsType = vst.type()+"_RECONCILE";
        rbtConnector.getSubscriberRbtclient().updateViralSmsType(vst.getSmsId(), smsType);
    }

	public static String addFailedCopy(ViralSMSTable vst, HashMap<String, String> infoMap)
	{
		logger.info("vst="+vst);
		String result = "FAILURE";
		String callerID = vst.callerID();
		Date time = vst.sentTime();
		Subscriber subscriber = rbtConnector.getSubscriberRbtclient().getSubscriber(callerID, null);
		if (subscriber == null)
			return result;
		
		boolean isValidSub = subscriber.isValidPrefix();
		if (!isValidSub)
			return result;
		
		Settings settings = rbtConnector.getSubscriberRbtclient().getSettings(callerID);
		Setting[] settingsArr = settings.getSettings();
		boolean hasMadeAnotherSelection = false;
		for (Setting setting : settingsArr) {
			if (WebServiceConstants.ALL.equalsIgnoreCase(setting.getCallerID())) {
				Date setTime = setting.getSetTime();
				if (setTime.after(time) || setTime.equals(time)) {
					hasMadeAnotherSelection = true;
					break;
				}
			}
		}
		if (hasMadeAnotherSelection)
			return "SUCCESS";
		String keyPressed = infoMap.get(iRBTConstant.KEYPRESSED_ATTR);
		String copyType=getCopyType(keyPressed);
		if(copyType != null)rbtConnector.getSubscriberRbtclient().addViralData(vst.subID(), vst.callerID(), copyType,
				vst.clipID(), vst.selectedBy(),infoMap);
		return "SUCCESS";
}

	public static String processFailedProcessCopyRequest(ViralSMSTable vst,
			boolean b, Object object)
	{
		logger.info("vst="+vst+", b="+b+", object="+object);
		String result = "FAILURE";
		String callerID = vst.callerID();
		Date time = vst.sentTime();
		Subscriber subscriber = rbtConnector.getSubscriberRbtclient().getSubscriber(callerID, null);
		if (subscriber == null)
			return result;
		
		boolean isValidSub = subscriber.isValidPrefix();
		if (!isValidSub)
			return result;
		
		Settings settings = rbtConnector.getSubscriberRbtclient().getSettings(callerID);
		Setting[] settingsArr = settings.getSettings();
		boolean hasMadeAnotherSelection = false;
		for (Setting setting : settingsArr) {
			if (WebServiceConstants.ALL.equalsIgnoreCase(setting.getCallerID())) {
				Date setTime = setting.getSetTime();
				if (setTime.after(time) || setTime.equals(time)) {
					hasMadeAnotherSelection = true;
					break;
				}
			}
		}
		if (hasMadeAnotherSelection)
			return "SUCCESS";
		
		rbtConnector.getSubscriberRbtclient().addViralData(vst.subID(), callerID, "COPYFTP", vst.clipID(), "RETRY",null);
		return "SUCCESS";
	}
	
	/**
	 * Returns the copy type of the given hunter.
	 * 
	 * @param hunterName
	 *            the hunter name for which copy type is required
	 * @return the copy type of the given hunter
	 */
	public static String getCopyTypeForHunter(String hunterName)
	{
		if (hunterCopyTypeMap.size() == 0)
		{
			hunterCopyTypeMap.put(HunterNameDirectCopy, "COPY");
			hunterCopyTypeMap.put(HunterNameOptinCopy, "COPY");
			hunterCopyTypeMap.put(HunterNameOptoutCopy, "COPY");
			hunterCopyTypeMap.put(HunterNameStarCopy, "COPYSTAR");
			hunterCopyTypeMap.put(HunterNameConfirmedCopy, "COPYCONFIRMED");
			hunterCopyTypeMap.put(HunterNameConfirmOptoutCopy, "COPYCONFIRM");
			hunterCopyTypeMap.put(HunterNameExpiredCopy, "COPYCONFPENDING");
		}
		
		logger.info("hunterCopyTypeMap="+hunterCopyTypeMap);
		return hunterCopyTypeMap.get(hunterName);
	}

	public static void updateCircleIdForType(String copyType)
	{
		logger.info("copyType="+copyType);
		rbtDBManager.updateCircleIdForType(copyType);
	}
	public static ArrayList<String> tokenizeArrayList(String stringToTokenize, String delimiter)
	{
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
	private static String getCopyType(String keyPressed)
	{
		logger.info("keyPressed is :"+keyPressed);
		if(keyPressed == null)
			keyPressed = "s";
		if(normalCopyKeys != null)
		{
			for(int i = 0; i < normalCopyKeys.size(); i++)
				if(keyPressed.indexOf(normalCopyKeys.get(i)) != -1)
				{
					return "COPY";
				}
		}
		if(starCopyKeys != null)
		{
			for(int i = 0; i < starCopyKeys.size(); i++)
				if(keyPressed.indexOf(starCopyKeys.get(i)) != -1)
				{
					return "COPYSTAR";
				}
		}
		return null;
	}
	private static boolean isShuffleCategory(String catToken) {
		boolean response = false;
//		if (catToken.toUpperCase().startsWith("S")) {
//			catToken = catToken.substring(1);
//		}
		try{
			int catId = Integer.parseInt(catToken);
			Category category = rbtConnector.getMemCache().getCategory(catId);
			if (category != null
					&& categoryTypeList.contains(category.getCategoryTpe())
					&& category.getCategoryEndTime().after(new Date())) {
				response = true;
			}
		} catch (NumberFormatException nfe) {
			response = false;
		}
		return response;		
	}
	private static String getInLocalCurrencyFormat(String selAmt)
	{
		String returnValue = selAmt; 
		try
		{
			double amount = Double.parseDouble(selAmt);
			returnValue = CurrencyUtil.getFormattedCurrency(null, amount);
		}
		catch(Exception e)
		{
			logger.warn("In correct value of charge class amout", e);
		}
		return returnValue;
	}
	
	
	public static String finalizeSmsText(HashMap<String, Object> smsTextMap)
	{
		if(smsTextMap == null || smsTextMap.size() == 0 )
			return null;
		String smsText = (String)smsTextMap.get("SMS_TEXT");
		if(smsText == null || smsText.trim().length() == 0)
			return null;
		if(smsText.indexOf("CALLED_ID") != -1 && smsTextMap.get("CALLED_ID") != null)
			smsText = smsText.replaceAll("CALLED_ID", (String)smsTextMap.get("CALLED_ID"));
		if(smsText.indexOf("CATEGORY_NAME") != -1 && smsTextMap.get("CATEGORY_NAME") != null )
			smsText = smsText.replaceAll("CATEGORY_NAME", (String)smsTextMap.get("CATEGORY_NAME"));
		if(smsText.indexOf("TIMEOUT") != -1 && smsTextMap.get("TIMEOUT") != null)
			smsText = smsText.replaceAll("TIMEOUT", (String)smsTextMap.get("TIMEOUT"));
		if(smsText.indexOf("OPERATOR_NAME") != -1 && smsTextMap.get("OPERATOR_NAME") != null)
			smsText = smsText.replaceAll("OPERATOR_NAME", (String)smsTextMap.get("OPERATOR_NAME"));
		if(smsText.indexOf("PROMO_CODE") != -1 && smsTextMap.get("PROMO_CODE") != null )
			smsText = smsText.replaceAll("PROMO_CODE", (String)smsTextMap.get("PROMO_CODE"));
		if(smsText.indexOf("SEL_AMOUNT") != -1 && smsTextMap.get("SEL_AMOUNT") != null )
			smsText = smsText.replaceAll("SEL_AMOUNT", (String)smsTextMap.get("SEL_AMOUNT"));
		if(smsText.indexOf("ACT_AMOUNT") != -1 && smsTextMap.get("ACT_AMOUNT") != null )
			smsText = smsText.replaceAll("ACT_AMOUNT", (String)smsTextMap.get("ACT_AMOUNT"));
		if(smsText.indexOf("COPY_CONFIRM_KEYWORD") != -1 && smsTextMap.get("COPY_CONFIRM_KEYWORD") != null )
			smsText = smsText.replaceAll("COPY_CONFIRM_KEYWORD", (String)smsTextMap.get("COPY_CONFIRM_KEYWORD"));
		if(smsText.indexOf("COPY_CANCEL_KEYWORD") != -1 && smsTextMap.get("COPY_CANCEL_KEYWORD") != null )
			smsText = smsText.replaceAll("COPY_CANCEL_KEYWORD", (String)smsTextMap.get("COPY_CANCEL_KEYWORD"));
		Clip clip = (Clip) smsTextMap.get("CLIP_OBJECT");
		Category category = (Category) smsTextMap.get("CATEGORY_OBJECT");
		String artistName = "";
		String contentName = "";
		if(category != null && isShuffleCategory(category.getCategoryId()+""))
		{
			if(clip != null)
				artistName = clip.getArtist();
			contentName = category.getCategoryName();
		}
		else if(clip != null)
		{
			artistName = clip.getArtist();
			contentName = clip.getClipName();
		}
		
		if(smsText.indexOf("SONG_NAME") != -1 && smsTextMap.get("SONG_NAME") != null)
			smsText = smsText.replaceAll("SONG_NAME", contentName);
		if(smsText.indexOf("ARTIST") != -1 && artistName != null)
			smsText = smsText.replaceAll("ARTIST", artistName);
		smsText = cleansSMSTExt(smsText);
		return smsText;
	}

	private static String cleansSMSTExt(String smsText)
	{
		smsText = smsText.replaceAll("SONG_NAME", "");
		smsText = smsText.replaceAll("ACT_AMOUNT", "");
		smsText = smsText.replaceAll("SEL_AMOUNT", "");
		smsText = smsText.replaceAll("CALLED_ID", "");
		smsText = smsText.replaceAll("OPERATOR_NAME", "");
		smsText = smsText.replaceAll("COPY_CONFIRM_KEYWORD", "");
		smsText = smsText.replaceAll("COPY_CANCEL_KEYWORD", "");
		smsText = smsText.replaceAll("TIMEOUT", "");
		smsText = smsText.replaceAll("ARTIST", "");
		smsText = smsText.replaceAll("  ", " ");
		return smsText;
	}
	
	public static boolean checkDNDUser(String callerID) {
		String umpDNDUrl = RBTParametersUtils.getParamAsString(GATHERER,
				"DND_URL_FOR_PROMOTION", null);
		if (umpDNDUrl != null) {
			umpDNDUrl = umpDNDUrl.replaceFirst("%SUBSCRIBER_ID%", callerID);
			StringBuffer response = new StringBuffer();
			boolean success = Tools.callURL(umpDNDUrl, new Integer(-1),
					response, false, null, -1, false, 2000);

			if (Boolean.parseBoolean(response.toString().trim()) || !success) {
				// if UMP url returns 'TRUE' or error status code or if UMP
				// server is down, number is considered as DND
				logger.info("Not promoting as the number " + callerID + " is DND");
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public static void initParams(){		
		String chrgClassForAllSubLoop = RBTParametersUtils.getParamAsString(
				"COMMON", "ALL_CALLER_COS_CHARGE_CLASS_MAP_FOR_INLOOP", null);

		String chrgClassForSpecialSub = RBTParametersUtils.getParamAsString(
				"COMMON", "SPECIAL_CALLER_COS_CHARGE_CLASS_MAP", null);

		String chrgClassForSpecialSubLoop = RBTParametersUtils
				.getParamAsString("COMMON",
						"SPECIAL_CALLER_COS_CHARGE_CLASS_MAP_FOR_INLOOP", null);				

		chargeClassMapForAllSubInLoop = MapUtils.convertIntoMap(
				chrgClassForAllSubLoop, ";", "=", null);

		logger.info("chargeClassMapForAllSubInLoop = "
				+ chargeClassMapForAllSubInLoop);

		chargeClassMapForSpecialSub = MapUtils.convertIntoMap(
				chrgClassForSpecialSub, ";", "=", null);

		logger.info("chargeClassMapForSpecialSub = "
				+ chargeClassMapForSpecialSub);

		chargeClassMapForSpecialSubInLoop = MapUtils.convertIntoMap(
				chrgClassForSpecialSubLoop, ";", "=", null);

		logger.info("chargeClassMapForSpecialSubInLoop = "
				+ chargeClassMapForSpecialSubInLoop);
	}
	
	public static boolean isUserCDTNDT(String cosId){
		initParams();
		if(cosId!=null && (chargeClassMapForAllSubInLoop.containsKey(cosId)
				||chargeClassMapForSpecialSub.containsKey(cosId)
				||chargeClassMapForSpecialSubInLoop.containsKey(cosId))){
			return true;
		}
		return false;
	}
}
