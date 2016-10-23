package com.onmobile.apps.ringbacktones.daemons;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberActivityCounts;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberActivityCountsDAO;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager.MappedSiteIdNotFoundException;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SmsTextCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CopyRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.RBTLogger;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.common.exception.OnMobileException;

public class RBTGift extends Thread implements WebServiceConstants
{
	private static Logger logger = Logger.getLogger(RBTGift.class);
	
	private static Logger giftLogger = Logger.getLogger("COMVIVA_GIFT_REQUEST");

	final static int RBTGIFT_SUCCESS = 1;
	final static int RBTGIFT_ACTIVE = 2;
	final static int RBTGIFT_INACTIVE = 3;
	final static int RBTGIFT_ERROR = 4;
	final static int RBTGIFT_FAILURE = 5;
	final static String STATUS_ERROR = "ERROR";
	final static String STATUS_RETRY = "RETRY";

	private HttpClient m_httpClient = new HttpClient();
	private int m_timeOutSec = 60;

	private static ArrayList m_OperatorPrefixes= null;
	private ArrayList m_nonOnmobilePrefix = new ArrayList();

	private RBTDaemonManager m_mainDaemonThread = null; 
	private RBTDBManager m_rbtDBManager = null;
	private RBTCacheManager rbtCacheManager = null;
	private ParametersCacheManager m_rbtParamCacheManager = null;
	private SmsTextCacheManager smsTextCacheManager=null;


	private HashMap m_modeChrgMap = null;
	private HashMap<String,String> m_contentTypeChrgMap = null;
	private HashMap m_modeSubClassMap = null;
	SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	URLCodec m_urlEncoder = new URLCodec();

	private String defaultNonGiftableContentSMS = "The content %SONG_NAME can not be gifted.";
	private String defaultGIFTConfirmationSMS = "Please send the RING GIFT <giftee number> to 888";
	private String defaultGiftClipExpiredSMS = "The clip you are trying to gift is expired ";
	private String defaultGiftCategoryExpiredSMS = "The category you are trying to gift is expired ";
	private String defaultGifteeNotificationSMS = "Dear user, you have received the content %SONG_NAME as gift from %GIFTER";
	private String defaultGifteeEmotionNotificationSMS = "Dear user, you have emotion rbt service as gift from %GIFTER";
	// RBT-14301: Uninor MNP changes.
	private static Map<String, String> circleIdToSiteIdMapping = null;
	protected RBTGift(RBTDaemonManager mainDaemonThread) 
	{
		try
		{
			setName("RBTGift");
			m_mainDaemonThread = mainDaemonThread;
			init();
		}
		catch (Exception e)
		{
			logger.error("Issue in creating RBTGift daemon", e);
		}
	}


	public void init()
	{
		m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		smsTextCacheManager=CacheManagerUtil.getSmsTextCacheManager();

		m_rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();

		String tmp = null;
		tmp = getParamAsString("MODE_GIFT_CHRG_CLASSES");
		if (tmp != null && tmp.length() > 0)
		{
			m_modeChrgMap = new HashMap();
			StringTokenizer stk = new StringTokenizer(tmp, ";");
			while(stk.hasMoreTokens())
			{
				String token = stk.nextToken();
				StringTokenizer stk1 = new StringTokenizer(token, ",");
				if(stk1.hasMoreTokens())
				{
					String mode = stk1.nextToken();
					String chrg = null;
					if(stk1.hasMoreTokens())
						chrg = stk1.nextToken();
					if(mode != null && chrg != null)
						m_modeChrgMap.put(mode, chrg);
				}
			}
		}
		tmp = getParamAsString("CONTENT_TYPE_GIFT_CHRG_CLASS_MAP");
		if (tmp != null && tmp.length() > 0)
		{
			m_contentTypeChrgMap = new HashMap<String,String>();
			StringTokenizer stk = new StringTokenizer(tmp, ";");
			while(stk.hasMoreTokens())
			{
				String token = stk.nextToken();
				StringTokenizer stk1 = new StringTokenizer(token, ",");
				if(stk1.hasMoreTokens())
				{
					String mode = stk1.nextToken();
					String chrg = null;
					if(stk1.hasMoreTokens())
						chrg = stk1.nextToken();
					if(mode != null && chrg != null)
						m_contentTypeChrgMap.put(mode, chrg);
				}
			}
		}
		tmp = getParamAsString("MODE_GIFT_SUB_CLASSES");
		if (tmp != null && tmp.length() > 0)
		{
			m_modeSubClassMap = new HashMap();
			StringTokenizer stk = new StringTokenizer(tmp, ";");
			while(stk.hasMoreTokens())
			{
				String token = stk.nextToken();
				StringTokenizer stk1 = new StringTokenizer(token, ",");
				if(stk1.hasMoreTokens())
				{
					String mode = stk1.nextToken();
					String chrg = null;
					if(stk1.hasMoreTokens())
						chrg = stk1.nextToken();
					if(mode != null && chrg != null)
						m_modeSubClassMap.put(mode, chrg);
				}
			}
		}

		String param = getParamAsString("GATHERER", "NON_ONMOBILE_PREFIX", null);
		if(param != null)
		{
			StringTokenizer stk = new StringTokenizer(param, ",");
			while(stk.hasMoreTokens())
			{
				m_nonOnmobilePrefix.add(stk.nextToken().trim());
			}
		}

		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager(); 
		connectionManager.getParams().setStaleCheckingEnabled(true); 
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(10); 
		connectionManager.getParams().setMaxTotalConnections(20); 
		m_httpClient = new HttpClient(connectionManager); 

		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false); 
		m_httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler); 
		m_httpClient.getParams().setSoTimeout(m_timeOutSec * 1000); 	 
		// RBT-14301: Uninor MNP changes.
		circleIdToSiteIdMapping = MapUtils.convertToMap(
				CacheManagerUtil.getParametersCacheManager().getParameterValue(
						"COMMON", "CIRCLEID_TO_SITEID_MAPPING", null), ";",
				":", null);
		// logger.info("circleIdToSiteIdMapping= " + circleIdToSiteIdMapping);
	}

	public void run()
	{
		while(m_mainDaemonThread != null && m_mainDaemonThread.isAlive()) 
		{
			String tmp = getParamAsString("GATHERER","OPERATOR_PREFIX", null);
			if(tmp != null)
			{
				m_OperatorPrefixes = Tools.tokenizeArrayList(tmp, null);
			}
			processSelfGifts();
			processCopyGifts();
			processGifts();
			processChargedGifts();
			processGiftAcknowledge();
			if(getParamAsString("GIFT_SERVICE_ACCEPT_URL") != null)
				processGiftsAcceptPre();
			try
			{
				logger.info("Gift Thread Sleeping for 5 minutes............");
				Thread.sleep(getParamAsInt("SLEEP_INTERVAL_MINUTES",5) * 60 * 1000);
			}
			catch(Exception e)
			{
			}
		}
	}



	private void processGiftAcknowledge()
	{
		try{	
			logger.info("Getting subscribers of type ACCEPT_ACK...");
			ViralSMSTable[] viral = m_rbtDBManager.getViralSMSByType("ACCEPT_ACK");
			if(viral != null )
			{
				for(int i=0 ;i<viral.length;i++){
					boolean status = false;
					try
					{

						String response = connectToRemote(viral[i].subID(), "rbt_gift_acknowledge.jsp?subscriber_id=" +  viral[i].subID() + "&gifted_to=" + viral[i].callerID() + "&clip_id=" + viral[i].clipID() + "&status=" + viral[i].type() + "&requested_timestamp=" + viral[i].sentTime().getTime());
						if (response != null && response.indexOf("SUCCESS") != -1)
						{
							status = true;
						}
					}
					catch (Throwable e)
					{
						logger.error("",e);  
						status = false;
					}
					try
					{
						if (status || viral[i].count() > 2)
						{
							m_rbtDBManager.updateViralPromotion(viral[i].subID(), viral[i].callerID(), viral[i].sentTime(), viral[i].type(), "ACCEPTED", viral[i].setTime(), null, null);
						}
						else
						{
							m_rbtDBManager.setSearchCount(viral[i].subID(), viral[i].type(), (viral[i].count() + 1));
						}
					}
					catch (Throwable e)
					{
						logger.error("",e);  
					}

				}
			}

			logger.info("Getting subscribers of type REJECT_ACK...");
			viral =  m_rbtDBManager.getViralSMSByType("REJECT_ACK");
			if(viral != null)
			{
				for(int i=0 ;i<viral.length;i++){
					boolean status = false;
					try
					{
						String response = connectToRemote(viral[i].subID(), "rbt_gift_acknowledge.jsp?subscriber_id=" +  viral[i].subID() + "&gifted_to=" + viral[i].callerID() + "&clip_id=" + viral[i].clipID() + "&status=" + viral[i].type() + "&requested_timestamp=" + viral[i].sentTime().getTime());
						if (response != null && response.indexOf("SUCCESS") != -1)
						{
							status = true;
						}
					}
					catch (Throwable e)
					{
						logger.error("", e);  
						status = false;
					}
					try
					{
						if (status || viral[i].count() > 2)
						{
							m_rbtDBManager.updateViralPromotion(viral[i].subID(), viral[i].callerID(), viral[i].sentTime(), viral[i].type(), "REJECTED", viral[i].setTime(), null, null);
						}
						else
						{
							m_rbtDBManager.setSearchCount(viral[i].subID(), viral[i].type(), (viral[i].count() + 1));
						}
					}
					catch (Throwable e)
					{
						logger.error("",e);  
					}

				}
			}

		}catch(Throwable e1){
			logger.error("",e1);  

		}

	}

	private void processGifts()
	{
		try
		{
			ViralSMSTable[] viral = m_rbtDBManager.getViralSMSByType("GIFT");
			logger.info("Got "+ (viral == null ? 0 : viral.length) +" subscribers of type GIFT");

			if (viral != null)
			{
				for (int i = 0; i < viral.length; i++)
				{
					boolean giftFailed = false;
					try
					{
						String songName = null;
						String subID = viral[i].subID();
						String contentID = viral[i].clipID();
						if (contentID != null)
						{
							Date sysdate = new Date();
							if (contentID.startsWith("C"))
							{
								// checking if the category is expired
								Category category = rbtCacheManager.getCategory(Integer.parseInt(contentID.substring(1)));
								songName = (category != null) ? category.getCategoryName() : null;
								if (category != null && category.getCategoryEndTime().before(sysdate))
								{
									String sms = getSMSText("DAEMON",
											"GIFT_CATEGORY_EXPIRED_TEXT", subID,
											defaultGiftCategoryExpiredSMS);
									sms = sms.replaceAll("%SONG_NAME",
											(songName == null) ? "" : songName);
									Tools.sendSMS(getParamAsString("DAEMON",
											"GIFT_SENDER_NUMBER", "123456"), subID,
											sms, getParamAsBoolean(
													"SEND_SMS_MASS_PUSH", "FALSE"));

									String gifteeSms = getSMSText("DAEMON",
											"GIFT_CATEGORY_EXPIRED_TEXT_TO_GIFTEE", viral[i].callerID(),
											null);
									if (gifteeSms != null)
									{
										gifteeSms = gifteeSms.replaceAll("%SONG_NAME",
												(songName == null) ? "" : songName);
										gifteeSms = gifteeSms.replaceAll("%GIFTER",
												viral[i].subID());
										Tools.sendSMS(getParamAsString("DAEMON",
												"GIFT_SENDER_NUMBER", "123456"), viral[i].callerID(),
												gifteeSms, getParamAsBoolean(
														"SEND_SMS_MASS_PUSH", "FALSE"));
									}
									m_rbtDBManager.updateViralPromotion(viral[i].subID(), viral[i].callerID(), viral[i].sentTime()
											, viral[i].type(), "GIFTFAILED", viral[i].setTime(), null, null);

									giftFailed = true;
									continue;
								}
							}
							else
							{
								// checking if the clip is expired
								Clip clip = rbtCacheManager.getClip(contentID);
								songName = (clip != null) ? clip.getClipName() : null;
								if (clip != null
										&& clip.getClipEndTime().before(sysdate))
								{
									String sms = getSMSText("DAEMON",
											"GIFT_CLIP_EXPIRED_TEXT", subID,
											defaultGiftClipExpiredSMS);

									sms = sms.replaceAll("%SONG_NAME",
											(songName == null) ? "" : songName);
									Tools.sendSMS(getParamAsString("DAEMON",
											"GIFT_SENDER_NUMBER", "123456"), subID,
											sms, getParamAsBoolean(
													"SEND_SMS_MASS_PUSH", "FALSE"));

									String gifteeSms = getSMSText("DAEMON",
											"GIFT_CLIP_EXPIRED_TEXT_TO_GIFTEE", viral[i].callerID(),
											null);
									if (gifteeSms != null)
									{
										gifteeSms = gifteeSms.replaceAll("%SONG_NAME",
												(songName == null) ? "" : songName);
										gifteeSms = gifteeSms.replaceAll("%GIFTER",
												viral[i].subID());
										Tools.sendSMS(getParamAsString("DAEMON",
												"GIFT_SENDER_NUMBER", "123456"), viral[i].callerID(),
												gifteeSms, getParamAsBoolean(
														"SEND_SMS_MASS_PUSH", "FALSE"));
									}

									m_rbtDBManager.updateViralPromotion(viral[i].subID(), viral[i].callerID(), viral[i].sentTime()
											, viral[i].type(), "GIFTFAILED", viral[i].setTime(), null, null);

									giftFailed = true;
									continue;
								}
							}
						}

						String refID = "RBTGIFT:" + viral[i].callerID() + ":"
								+ viral[i].clipID() + ":"
								+ m_sdf.format(viral[i].sentTime());

						String ret = doesSubHaveGiftAmount(viral[i].subID(),
								viral[i].callerID(), viral[i].clipID(),
								viral[i].selectedBy(), refID, viral[i]
										.extraInfo());
						if (ret != null && ret.equals(STATUS_ERROR))
						{
							String gifterSms = getSMSText("DAEMON",
									"GIFT_FAILED_LOW_BALANCE_TEXT", viral[i].subID(),
									null);
							if (gifterSms != null)
							{
								gifterSms = gifterSms.replaceAll("%SONG_NAME",
										(songName == null) ? "" : songName);
								gifterSms = gifterSms.replaceAll("%GIFTEE",
										viral[i].callerID());
								Tools.sendSMS(getParamAsString("DAEMON",
										"GIFT_SENDER_NUMBER", "123456"), viral[i].subID(),
										gifterSms, getParamAsBoolean(
												"SEND_SMS_MASS_PUSH", "FALSE"));
							}

							String gifteeSms = getSMSText("DAEMON",
									"GIFT_FAILED_LOW_BALANCE_TEXT_TO_GIFTEE", viral[i].callerID(),
									null);
							if (gifteeSms != null)
							{
								gifteeSms = gifteeSms.replaceAll("%SONG_NAME",
										(songName == null) ? "" : songName);
								gifteeSms = gifteeSms.replaceAll("%GIFTER",
										viral[i].subID());
								Tools.sendSMS(getParamAsString("DAEMON",
										"GIFT_SENDER_NUMBER", "123456"), viral[i].callerID(),
										gifteeSms, getParamAsBoolean(
												"SEND_SMS_MASS_PUSH", "FALSE"));
							}

							m_rbtDBManager.updateViralPromotion(viral[i]
									.subID(), viral[i].callerID(), viral[i]
									.sentTime(), viral[i].type(), "GIFTFAILED",
									viral[i].setTime(), viral[i].selectedBy(),
									null);
							
							giftFailed = true;
						}
						else if (ret != null && !ret.equals(STATUS_RETRY))
						{
							m_rbtDBManager.updateViralPromotion(viral[i]
									.subID(), viral[i].callerID(), viral[i]
									.sentTime(), viral[i].type(),
									"GIFTCHRGPENDING", viral[i].setTime(),
									viral[i].selectedBy(), null);
						}
					}
					catch (Throwable e)
					{
						logger.error("", e);
					}
					finally
					{
						if (giftFailed)
						{
							Parameters giftLimitParam = null;
							ParametersCacheManager parametersCacheManager = CacheManagerUtil
									.getParametersCacheManager();
							if (viral[i].clipID() == null)
							{
								// Service Gift
								giftLimitParam = parametersCacheManager
										.getParameter(iRBTConstant.COMMON,
												"SERVICE_GIFT_LIMIT");
							}
							else
							{
								// Tone Gift
								giftLimitParam = parametersCacheManager
										.getParameter(iRBTConstant.COMMON,
												"TONE_GIFT_LIMIT");
							}

							int giftLimit = 0;
							if (giftLimitParam != null)
							{
								giftLimit = Integer.parseInt(giftLimitParam
										.getValue());
							}

							if (giftLimit > 0)
							{
								SubscriberActivityCounts subscriberActivityCounts = SubscriberActivityCountsDAO
										.getSubscriberActivityCountsForDate(
												viral[i].subID(), viral[i]
														.sentTime());
								if (subscriberActivityCounts != null)
								{
									if (viral[i].clipID() == null)
									{
										subscriberActivityCounts
												.decrementServiceGiftsCount();
									}
									else
									{
										subscriberActivityCounts
												.decrementToneGiftsCount();
									}

									subscriberActivityCounts.update();
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}
	
	private void processSelfGifts() {

		try{	
			logger.info("Getting subscribers of type SELF_GIFT...");
			ViralSMSTable[] viral = m_rbtDBManager.getViralSMSByType("SELF_GIFT");
			if(viral != null)
			{
				for(int i = 0 ; i < viral.length; i++)
				{
					try
					{	
						Boolean success=false;
						String subid = viral[i].subID();
						SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(subid, "GIFT"));
						if (subscriberDetail.isValidSubscriber())
						{	
							logger.info("Subscriber is valid");
							CopyRequest copyRequest=new CopyRequest(viral[i].callerID(),viral[i].subID());
							CopyDetails copyDetails = RBTClient.getInstance().getCopyData(copyRequest); 
							CopyData copydata[]= copyDetails.getCopyData();

							String wavFile = null;
							if(copyRequest.getResponse().equalsIgnoreCase(WebServiceConstants.SUCCESS)&& copydata != null)
							{              
								logger.info("subscriber is rbt user ");
								int categoryID = copydata[0].getCategoryID();
								int clipID=copydata[0].getToneID();
								int selectionStatus=copydata[0].getStatus();
								String strSelectionStatus=selectionStatus+"";
								Clip clip = rbtCacheManager.getClip(clipID);
								if (clip != null)
								{	success=true;
									logger.info("subscriber clip is not null ");
									wavFile = clip.getClipRbtWavFile();
									String strClip = clipID+"";
									String nonGiftableCategoryIds = "";
									nonGiftableCategoryIds = getParamAsString("DAEMON", "NON_GIFTABLE_CATEGORIES", "");
									List nonGiftableCategoryIdList = Tools.tokenizeArrayList(nonGiftableCategoryIds, null);

									if (!(strSelectionStatus.trim().equals("1") || strSelectionStatus.trim().equals("80")))
									{
										success=false;
										logger.info(" Gift cannot be sent invalid selection status");

									}
														
									if (nonGiftableCategoryIdList != null && nonGiftableCategoryIdList.contains(String.valueOf(categoryID))){
										logger.info(" Gift cannot be sent invalid category id");
										success=false;
									}


									if(success)
									{
										m_rbtDBManager.updateViralPromotion1(viral[i].subID(), viral[i].callerID(), viral[i].sentTime(), viral[i].type(), "GIFT", viral[i].setTime(), viral[i].selectedBy(),null,strClip );
	
									}

									//m_rbtDBManager.updateViralPromotion(viral[i].subID(), viral[i].callerID(), viral[i].sentTime(),strClip);
									
								} else {
									logger.info("A party : "+viral[i].subID()+" B party : "+viral[i].callerID()+" Clip does not exist clip id : "+clipID);
								}

							}
						}
						if(!success)
						{	
							m_rbtDBManager.updateViralPromotion(viral[i].subID(), viral[i].callerID(), viral[i].sentTime(), viral[i].type(), "GIFTFAILED", viral[i].setTime(), viral[i].selectedBy(),null );
							logger.info(" Gift cannot be sent");
						}

					}catch(Exception e2){
						logger.error("",e2);  
					}
				}
			}

		}catch(Exception e3){
			logger.error("",e3);  

		}	

	}
	
	private void processCopyGifts() {
		try{	

			logger.info("Getting subscribers of type GIFTCOPY...");
			ViralSMSTable[] viral = m_rbtDBManager.getViralSMSByType("GIFTCOPY");

			if(viral != null)
			{
				for(int i = 0 ; i < viral.length; i++)
				{

					try
					{
						String subid = viral[i].subID();
						Date sentTime = viral[i].sentTime();
						SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(subid, "GIFT"));

						if (subscriberDetail.isValidSubscriber())
						{
//							boolean isValidSubscriber = checkForValidPrefix(subid);
							boolean isValidSubscriber =true;
							boolean status = false;
							String clipId = "-1";
							StringTokenizer tokens = new StringTokenizer(viral[i].clipID(),":");
							String strSubscriberWavFile = tokens.nextToken();
							int iCategoryID = Integer.parseInt(tokens.nextToken());
							String strSelectionStatus = tokens.nextToken();
							String songName = null;
							boolean containsS = false;
							if (tokens.hasMoreTokens()&& tokens.nextToken().equalsIgnoreCase("S"))
								containsS = true;		

							if (!(strSelectionStatus.trim().equals("1") || strSelectionStatus.trim().equals("80")))
							{
								GiftCopyFailure(subid, songName,sentTime);//remove viral data
								continue;
							}

							if (!isValidSubscriber)
							{
								GiftCopyFailure(subid, songName,sentTime);//remove viral data
								continue;
							}

							String nonGiftableCategoryIds = "";
							nonGiftableCategoryIds = getParamAsString("DAEMON", "NON_GIFTABLE_CATEGORIES", "");
							List nonGiftableCategoryIdList = Tools.tokenizeArrayList(nonGiftableCategoryIds, null);

							if (nonGiftableCategoryIdList != null && nonGiftableCategoryIdList.contains(String.valueOf(iCategoryID))){
								Category category = rbtCacheManager.getCategory(iCategoryID);
								if (category != null)
									songName = category.getCategoryName();
								GiftCopyFailure(subid, songName,sentTime);//remove viral data
								continue;
							}

							
							//checking if the clip is expired
							Clip clip =rbtCacheManager.getClipByRbtWavFileName(strSubscriberWavFile);
							
							if (containsS)
							{
								clipId = "C"+iCategoryID;
								Category category = rbtCacheManager.getCategory(iCategoryID);
								if (category != null)
									songName = category.getCategoryName();
								Date sysdate=new Date();
								if (category != null && category.getCategoryEndTime().before(sysdate)) {

									GiftClipExpired(subid,songName,sentTime);
									continue;
								}
							}
							else
							{
								clip = rbtCacheManager.getClipByRbtWavFileName(strSubscriberWavFile);
								if (clip == null)
								{
									GiftCopyFailure(subid, songName,sentTime);//remove viral data
									continue;
								}
								Date sysdate=new Date();
								if (clip != null && clip.getClipEndTime().before(sysdate)) {

									songName = clip.getClipName();
									GiftClipExpired(subid,songName,sentTime);
									continue;
								}
								clipId = String.valueOf(clip.getClipId());	
								songName = clip.getClipName();
							}
							String sms =defaultGIFTConfirmationSMS;
							sms=getSMSText("DAEMON", "GIFTCOPY_CONFIRMATION_TEXT", subid, defaultGIFTConfirmationSMS);
							
							String extraInfo = null;
							if (clip != null && "EMOTION_RBT".equalsIgnoreCase(clip.getContentType()))
							{	
								
								if (viral[i].extraInfo() != null)
									extraInfo = DBUtility.setXMLAttribute(viral[i].extraInfo(), "EMOTION_RBT", "TRUE");

								sms=getSMSText("DAEMON", "GIFTCOPY_CONFIRMATION_TEXT_EMOTIONS", subid, defaultGIFTConfirmationSMS);
							}	
							sms = Tools.findNReplaceAll(sms, "%SONG_NAME", (songName == null) ? "":songName);
							Tools.sendSMS(getParamAsString("DAEMON","GIFT_SENDER_NUMBER","123456"), subid, sms, getParamAsBoolean("SEND_SMS_MASS_PUSH","FALSE"));

//							String extraInfo = null;
//							if (viral[i].extraInfo() != null)
//								extraInfo = DBUtility.setXMLAttribute(viral[i].extraInfo(), "SEL_STATUS", strSelectionStatus);

							m_rbtDBManager.updateViralPromotion(subid, viral[i].callerID(), viral[i].sentTime(), "GIFTCOPY", "GIFTCOPY_PENDING", clipId, extraInfo);
							//after receiving SMS, change type to GIFT		
						}else{
							String responseXML = null;
							try 
							{
								String url = null;
								if(subscriberDetail==null || subscriberDetail.getCircleID()==null){
									m_rbtDBManager.deleteViralPromotion(subid, viral[i].callerID(), "GIFTCOPY", viral[i].sentTime());
								}
								if (subscriberDetail.getCircleID().equalsIgnoreCase("CENTRAL"))
								{
									Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("GATHERER", "REDIRECT_NATIONAL");
									if (parameter != null)
									{
										String[] urlParams = parameter.getValue().split(",");
										url = urlParams[1];
									}
									
								}
								else
								{
									String circleID = subscriberDetail.getCircleID();
									SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
									url = sitePrefix.getSiteUrl();
								}
								url = url.replaceAll("rbt_sms.jsp", "");
								url = url.replaceAll("\\?", "");
								url = url+"Data.do";
								logger.info("url=="+url);
								HttpParameters httpParameters = new HttpParameters(url);
								HashMap<String, String> requestParams = new HashMap<String, String>();
								requestParams.put(param_subscriberID, subid);
								requestParams.put(param_callerID, viral[i].callerID());
								requestParams.put(param_action, action_add);
								requestParams.put(param_info, VIRAL_DATA);
								requestParams.put(param_type, "GIFTCOPY");
								requestParams.put(param_clipID, viral[i].clipID());
								requestParams.put(param_mode, viral[i].selectedBy());
//								requestParams.put(param_extraInfo, viral[i].selectedBy());
								HashMap<String,String> infoMap=DBUtility.getAttributeMapFromXML(viral[i].extraInfo());
								if (infoMap != null)
								{
									Set<Entry<String, String>> entryMap = infoMap.entrySet();
									for (Entry<String, String> entry : entryMap)
										requestParams.put(param_info + "_" + entry.getKey(), entry.getValue());
								}
								HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, requestParams);
								responseXML = httpResponse.getResponse();
								logger.info(responseXML);
								logger.info("response=="+responseXML );
								if(responseXML!=null && (responseXML.indexOf("Success")!=-1 || responseXML.indexOf("success")!=-1 || responseXML.indexOf("SUCCESS")!=-1)){
									m_rbtDBManager.deleteViralPromotion(subid, viral[i].callerID(), "GIFTCOPY", viral[i].sentTime());
								}
							}
							catch (Exception e) 
							{
								RBTLogger.logException("RBTGift", "processCopyGifts", e);
							}
						}
					}catch(Throwable e2){
						logger.error("",e2);
					}

				}
			}

		}catch(Exception e3){
			logger.error("",e3);  

		}	
	}
	private void GiftCopyFailure(String subId, String songName , Date sentTime) throws OnMobileException {
		//String sms= getParamAsString("DAEMON", "GIFTCOPY_FAILURE_TEXT", defaultNonGiftableContentSMS);
		String sms= getSMSText("DAEMON", "GIFTCOPY_FAILURE_TEXT", subId, defaultNonGiftableContentSMS);
		sms = Tools.findNReplaceAll(sms, "%SONG_NAME", (songName == null) ? "":songName);
		logger.info("Sending sms = "+sms +" to subscriber:"+subId);
		Tools.sendSMS(getParamAsString("DAEMON","GIFT_SENDER_NUMBER","123456"), subId, sms, getParamAsBoolean("SEND_SMS_MASS_PUSH","FALSE"));
		//removing viral data
		logger.info("Removing viral data for subId = "+subId +" and type = GIFTCOPY");
		m_rbtDBManager.removeViralSMS(subId, "GIFTCOPY",sentTime);
	}
	
	private void GiftClipExpired(String subId, String songName , Date sentTime) throws OnMobileException {
		//String sms= getParamAsString("DAEMON", "GIFTCOPY_FAILURE_TEXT", defaultNonGiftableContentSMS);
		String sms = defaultGiftClipExpiredSMS;
		sms = getSMSText("DAEMON", "GIFT_CLIP_EXPIRED_TEXT", subId, defaultGiftClipExpiredSMS);
		sms = Tools.findNReplaceAll(sms, "%SONG_NAME", (songName == null) ? "" : songName);
		logger.info("Sending sms = "+sms +" to subscriber:"+subId);
		Tools.sendSMS(getParamAsString("DAEMON", "GIFT_SENDER_NUMBER", "123456"), subId, sms,
				getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
		//removing viral data
		logger.info("Removing viral data for subId = "+subId +" and type = GIFTCOPY");
		m_rbtDBManager.removeViralSMS(subId, "GIFTCOPY",sentTime);
	}


	private void processGiftsAcceptPre() {

		try {

			logger.info("Getting subscribers of type ACCEPT_PRE...");
			ViralSMSTable[] viral = m_rbtDBManager.getViralSMSByType(
			"ACCEPT_PRE");
			if (viral != null) {
				for (int i = 0; i < viral.length; i++) {
					try {
						String subid = viral[i].callerID();
						boolean status = false;
						String ret = callGiftServiceExtension(viral[i]);
						int retStatus = 600;
						try {
							retStatus = Integer.parseInt(ret);
						}
						catch(Exception e) {
							logger.info("RBT::error while parsing status-" + ret);
						}
						// ,viral[i].subID(), viral[i].callerID(),
						// viral[i].clipID(), viral[i].selectedBy(), refID);
						//						if (ret != null && ret.equals(STATUS_ERROR)) {
						if(retStatus >= 700) {
							m_rbtDBManager.updateViralPromotion(
									viral[i].subID(), viral[i].callerID(), viral[i].sentTime(),
									viral[i].type(), "GIFTFAILED", viral[i].setTime(),
									viral[i].selectedBy(), null);
						}
						//						else if (ret != null && !ret.equals(STATUS_RETRY)) {
						else if (retStatus == 200) {
							/*Date ncDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ret
									.substring(8));
							m_rbtDBManager.setNextChargingDate(subid, ncDate);*/
							m_rbtDBManager.updateViralPromotion(
									viral[i].subID(), viral[i].callerID(), viral[i].sentTime(),
									viral[i].type(), "ACCEPT_ACK", viral[i].setTime(),
									viral[i].selectedBy(), null);
						}
					}
					catch (Throwable e2) {
						logger.error("", e2);
					}
				}
			}
		}
		catch (Exception e3) {
			logger.error("", e3);
		}
	}

	private String getAmountCharged(String resp)
	{
		double d = 0.0;
		if(resp != null)
		{
			try
			{
				StringTokenizer stk = new StringTokenizer(resp, "|");
				while(stk.hasMoreTokens())
				{
					String s = stk.nextToken();
					int index = s.indexOf("-");
					if( index != -1)
					{
						d = d + Double.parseDouble(s.substring(index+1));
					}
				}
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}

		if(d < 1.0)
			return null;
		else 
			return ""+d;
	}

	private void processChargedGifts()
	{

		try
		{	
			logger.info("Getting subscribers of type GIFT_CHARGED...");
			ViralSMSTable[] viral = m_rbtDBManager.getViralSMSByType("GIFT_CHARGED");

			if(viral != null)
			{
				for(int i = 0 ; i < viral.length; i++)
				{
					logger.info("Got subscribers of type GIFT_CHARGED...");
					HashMap<String, String> map = DBUtility.getAttributeMapFromXML(viral[i].extraInfo());
					if (map != null && map.containsKey(iRBTConstant.GIFTTYPE_ATTR))
					{
						String giftType = map.get(iRBTConstant.GIFTTYPE_ATTR);
						logger.info("gifttype:"+giftType);

						if (giftType != null && giftType.equalsIgnoreCase("direct"))
						{
							String gifteeConfirmationAllowed=CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "COPY_GIFT_GIFTEE_CONFIRM_ALLOWED","false");
							if(gifteeConfirmationAllowed!=null && gifteeConfirmationAllowed.equalsIgnoreCase("false")){
								processChargedCopyGift(viral[i].subID(),viral[i].callerID(),viral[i].clipID(),viral[i].sentTime(),viral[i].extraInfo());
								return;
							}
						}
					}
					try{
						String subid = viral[i].subID();
						boolean status = false;
						
						Map<String, String> giftExtraInfoMap = DBUtility.getAttributeMapFromXML(viral[i].extraInfo());
						if(giftExtraInfoMap != null && giftExtraInfoMap.containsKey("isComvivaCircle") && giftExtraInfoMap.get("isComvivaCircle").equalsIgnoreCase("TRUE")) {
							String url = null;
							String gifteeHub = giftExtraInfoMap.get(WebServiceConstants.param_GifteeHub);
							if(gifteeHub != null) {
								String comViviaUrl = getParamAsString("COMVIVA_GIFT_REQUEST_API_" + viral[i].selectedBy());
								String[] ComViviaUrls = comViviaUrl.split(";");
								for(String temp : ComViviaUrls) {
									
									if(temp.indexOf(":") == -1) {
										continue;
									}
									
									String hubName = temp.substring(0,temp.indexOf(":"));
									String tempUrl = temp.substring(temp.indexOf(":")+1);

									if(hubName.equalsIgnoreCase(gifteeHub)) {
										url = tempUrl;
										break;
									}
								}
							}
							
							if(url != null) {
								String vCode = "";
								Clip clip = rbtCacheManager.getClip(viral[i].clipID());
								if (clip != null) {
									String rbtWavFile = clip.getClipRbtWavFile();
									vCode = rbtWavFile!=null?rbtWavFile.replaceAll("rbt_", "").replaceAll("_rbt", ""):"";
								}
								
								url += "msisdn=" + viral[i].subID() + "&transactionID=" + giftExtraInfoMap.get("TPCGID") + "&vCode=" + vCode 
										+ "&msisdnGiftReceiver=" + viral[i].callerID() +
										"&channelName=" + viral[i].selectedBy();
								
								String selMappedStr = getParamAsString(iRBTConstant.COMMON, "SEL_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
								if (selMappedStr != null) {
									String str[] = selMappedStr.split(";");
									for (int urlCount = 0; urlCount < str.length; urlCount++) {
										String s[] = str[urlCount].split(",");
										if (s.length == 2 && giftExtraInfoMap.containsKey(s[1])){
											url += "&" + s[1] + "=" + giftExtraInfoMap.get(s[1]);
										}
									}
								}
								
								StringBuffer strBuf = new StringBuffer();
								StringBuffer httpStatusBuf = new StringBuffer();
								callURL(url, false, strBuf, httpStatusBuf);
								if(strBuf.toString().toUpperCase().indexOf("SUCCESS") != -1) {						
									giftLogger.info(m_sdf.format(new Date()) + ":" + url + ":" + httpStatusBuf.toString() + ":" + strBuf.toString());
									m_rbtDBManager.deleteViralPromotionBySMSID(viral[i].getSmsId(), viral[i].type());
								}
							}							
							continue;
						}
						
						
						
						try
						{
							//viral[i].subID(), viral[i].callerID(), viral[i].clipID(), viral[i].sentTime(),viral[i].extraInfo()
							switch(giftRBT(viral[i]))
							{
							case RBTGIFT_SUCCESS:
								status = true;
								break;
							case RBTGIFT_FAILURE:
								status = false;
								break;
							case RBTGIFT_ERROR:
								if(viral[i].count() > 2)
								{
									m_rbtDBManager.updateViralPromotion(viral[i].subID(), viral[i].callerID(), viral[i].sentTime(), viral[i].type(), "GIFTFAILED", viral[i].setTime(), viral[i].selectedBy(), viral[i].extraInfo());
								}
								else
								{
									m_rbtDBManager.setSearchCount(viral[i].subID(), viral[i].type(), (viral[i].count() + 1));
								}
								continue;
							default:
								logger.info("error while gifting rbt ....will retry next time");
							continue;
							}
						}
						catch (Throwable e1)
						{
							logger.error("",e1);
							continue;
						}
						if (status)
						{
							logger.info("Successfully processed record :subid "+viral[i].subID()+" caller : "+viral[i].callerID());

							m_rbtDBManager.updateViralPromotion(viral[i].subID(), viral[i].callerID(), viral[i].sentTime(), viral[i].type(), "GIFTED", viral[i].setTime(), viral[i].selectedBy(),viral[i].extraInfo());
						}
						else
						{
							m_rbtDBManager.updateViralPromotion(viral[i].subID(), viral[i].callerID(), viral[i].sentTime(), viral[i].type(), "GIFTFAILED", viral[i].setTime(), viral[i].selectedBy(),viral[i].extraInfo() );
						}

					}catch(Throwable e2){
						logger.error("",e2);  
					}
				}
			}

		}catch(Exception e3){
			logger.error("",e3);  

		}	
	}

	private void processChargedCopyGift(String subscriberID, String callerID,
			String contentID,Date giftSentTime, String extraInfo) throws Exception {
		String method = "processChargedCopyGift";
		logger.info("processing esia gift copy");
		int clipId = -1;
		int categoryId = -1;
		String songName = "";
		String promoId = "";
		String wavFile = null;
		int catId = 23;
		if (contentID.startsWith("C")){
			categoryId = Integer.parseInt(contentID.substring(1));
			Category category= rbtCacheManager.getCategory(categoryId);
			if (category != null){
				songName = category.getCategoryName();
				catId = categoryId;
				promoId = category.getCategoryPromoId();
				Clip[] clips = rbtCacheManager.getClipsInCategory(catId);
				if (clips.length > 0){
					wavFile = clips[0].getClipRbtWavFile();
				}
			}

		}else{
			clipId = Integer.parseInt(contentID);
			Clip clip = rbtCacheManager.getClip(clipId);
			if (clip != null){
				songName = clip.getClipName();
				wavFile = clip.getClipRbtWavFile();
				promoId = clip.getClipPromoId();
			}
		}
		logger.info("clipId:"+clipId);
		String callID = subID(callerID);
		Subscriber sub = m_rbtDBManager.getSubscriber(callID);
		String activationInfo = "GIFT COPY";

		String subscriptionClass = "DEFAULT";
		String chargeClass = "FREE";
		String giftTransID = null;

		HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		if(extraInfoMap != null )
		{
			if(extraInfoMap.containsKey(iRBTConstant.SUBSCRIPTION_CLASS))
				subscriptionClass = extraInfoMap.get(iRBTConstant.SUBSCRIPTION_CLASS);
			if(extraInfoMap.containsKey(iRBTConstant.CHARGE_CLASS))
				chargeClass = extraInfoMap.get(iRBTConstant.CHARGE_CLASS);
			if(extraInfoMap.containsKey(iRBTConstant.GIFT_TRANSACTION_ID))
				giftTransID = extraInfoMap.get(iRBTConstant.GIFT_TRANSACTION_ID);
		}

		/**
		 * Emotions Rbt changes. User can gift emotion rbt service. Make default emotion rbt selection to giftee
		 */
		if (extraInfoMap != null
				&& extraInfoMap.containsKey("EMOTION_RBT"))
		{
			String defaultConfig = getParamAsString("COMMON", "EMOTION_RBT_DEFAULT_CONFIG", null);
			if (defaultConfig != null)
			{
				String[] tokens = defaultConfig.split(",");
				String clipIDStr = tokens[0];

				if (tokens.length >= 3)
					catId = Integer.parseInt(tokens[2]);

				Clip clip = rbtCacheManager.getClip(clipIDStr);
				if (clip != null)
				{
					songName = clip.getClipName();
					wavFile = clip.getClipRbtWavFile();
					promoId = clip.getClipPromoId();
				}
			}
		}

		if (m_rbtDBManager.isSubscriberDeactivated(sub))
		{
			logger.info("giftee is not active");
			boolean isPrepaid = true;
			if (sub != null) 
				isPrepaid = sub.prepaidYes();

			HashMap<String, String> userInfoMap = new HashMap<String, String>();
			if(giftTransID != null)
				userInfoMap.put("GIFT_TRANSACTION_ID", giftTransID);

			SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
			String circleId = subscriberDetail.getCircleID();
			String prepaidYes = YES;
			if(sub!=null){
			     prepaidYes = sub.prepaidYes() ? YES : NO;
			}
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getDefaultCosDetail(circleId, prepaidYes);
			m_rbtDBManager.activateSubscriber(callerID, "GIFT", new Date(), null, isPrepaid,
					0, 0, activationInfo, subscriptionClass, true, cos,
					false, 0, userInfoMap, circleId, null, false, null);
		}
		//esia only
		HashMap<String, String> selExtraInfoMap = new HashMap<String, String>();
		if(giftTransID != null)
			selExtraInfoMap.put("GIFT_TRANSACTION_ID", giftTransID);

		logger.info("making selection with catid:"+catId+", and wavFile:"+wavFile);
		sub = m_rbtDBManager.getSubscriber(callID);
		boolean success = false;
		
		if(getParamAsString(iRBTConstant.GIFT, "ADD_ONLY_TO_DOWNLOADS_WHILE_GIFTING", "FALSE").equalsIgnoreCase("TRUE")){
			 Category category= rbtCacheManager.getCategory(catId);
			 Categories categoriesObj = CategoriesImpl.getCategory(category);
			 boolean isSubActive =Utility.isSubActive(callerID, categoriesObj, wavFile, 1);
		     String response = m_rbtDBManager.addSubscriberDownloadRW(callerID, wavFile, categoriesObj, null,
		    		            isSubActive, chargeClass, "GIFT", "GIFTCOPY", selExtraInfoMap, true, true, 
					            false, null, null);
		     success = response.indexOf("SUCCESS")!= -1;
		}else{
			 success = m_rbtDBManager.addSubscriberSelections(callerID,
					  null, catId, wavFile,null, null, null,1,"GIFT", "GIFTCOPY", 0,
					  sub.prepaidYes(), true, null,0, 2359, chargeClass,
					  true, true, "VUI",
					  null, sub.subYes(), null, true,
					  false, true,sub.subscriptionClass(), sub,null, selExtraInfoMap, true);
		}
		if(success)
		{
			boolean incrSelCountParam = getParamAsBoolean(iRBTConstant.COMMON, "INCREMENT_SEL_COUNT_FOR_GIFT", "FALSE");
			if(incrSelCountParam)
				m_rbtDBManager.setSelectionCount(callerID);

			String sms = defaultGifteeNotificationSMS;
			sms=getSMSText("DAEMON", "GIFTCOPY_GIFTEE_SMS_TEXT" , callerID, defaultGifteeNotificationSMS);
			// Emotions message
			if (extraInfoMap != null & extraInfoMap.containsKey("SEL_STATUS") && extraInfoMap.get("SEL_STATUS").equals("94"))
				sms=getSMSText("DAEMON", "GIFTCOPY_GIFTEE_SMS_TEXT_EMOTION" , callerID, defaultGifteeEmotionNotificationSMS);

			sms = Tools.findNReplaceAll(sms, "%SONG_NAME", songName);
			sms = Tools.findNReplaceAll(sms, "%GIFTER", subscriberID);
			sms = Tools.findNReplaceAll(sms, "%PROMO_ID", promoId);
			Tools.sendSMS(getParamAsString("DAEMON","GIFT_SENDER_NUMBER","123456"), callerID, sms, getParamAsBoolean("SEND_SMS_MASS_PUSH","FALSE"));
			logger.info("sms for giftee:"+callerID+" is '"+sms+"'");
			
			String gifterSms = getSMSText("DAEMON", "GIFTCOPY_GIFTER_SMS_TEXT", subscriberID, null);
			if (gifterSms != null)
			{
				sms = Tools.findNReplaceAll(gifterSms, "%CALLER_ID", callerID);
				Tools.sendSMS(getParamAsString("DAEMON", "GIFT_SENDER_NUMBER", "123456"), subscriberID, sms,
						getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
				logger.info("sms for gifter:" + subscriberID + " is '" + sms + "'");
			}
			m_rbtDBManager.updateViralPromotion(subscriberID, callerID, giftSentTime, "GIFT_CHARGED", "GIFTCOPY_SUCCESS", contentID,extraInfo);
		}
		else
		{
			m_rbtDBManager.updateViralPromotion(subscriberID, callerID, giftSentTime, "GIFT_CHARGED", "GIFTCOPY_FAILURE", contentID,extraInfo);
			logger.info("Gift Selection failed for gifterId : "+subscriberID+" for giftee : "+callerID);
		}
	}

	private String callGiftServiceExtension(ViralSMSTable vst)//String gifter, String giftedTo, String song, String selectedBy, String refID)
	{
		try
		{
			ChargeClass cClass = null;
			if(getParamAsString("GIFT_SERVICE_ACCEPT_URL") != null )
			{
				String subClass = null;
				Subscriber sub = m_rbtDBManager.getSubscriber(vst.callerID());
				if(sub == null)
				{
					logger.info("Subscriber not present in db");
					return null;
				}

				subClass = sub.subscriptionClass();

				String refID = "RBTGIFTSRV:"+vst.callerID()+":"+vst.clipID()+":"+m_sdf.format(vst.sentTime());
				String url = getParamAsString("GIFT_SERVICE_ACCEPT_URL") + "msisdn="+vst.callerID()+"&mode="+vst.selectedBy()+"&reqrefid="+refID+"&triggerkey=RBT_ACT_"+subClass;
				StringBuffer strBuf = new StringBuffer();
				StringBuffer httpStatus = new StringBuffer();
				long startTime = System.currentTimeMillis();
				String result = callURL(url, true,strBuf, httpStatus);
				long endTime =System.currentTimeMillis();
				String httpStatusStr = httpStatus.toString();

				RBTDaemonManager.writeTrans("SUBMGR-RESPONSE", url, 0, "STATUSCODE("+ httpStatus.toString() + ")|" + strBuf.toString(), (endTime-startTime));
				if(m_mainDaemonThread.m_writeTrans != null)
				{
					try
					{
						m_mainDaemonThread.writeTrans("GIFT_SRV_EXTN", url, Integer.parseInt(httpStatusStr), strBuf.toString(), 0);
					}
					catch(Exception e)
					{

					}
				}
				return httpStatusStr;
			}
		}
		catch(Throwable e)
		{

		}
		return STATUS_ERROR;
	}
	private String doesSubHaveGiftAmount(String gifter, String giftedTo, String song, String selectedBy, String refID, String extraInfo)
	{
		String user_info = "&info=";
		try
		{
			ChargeClass cClass = null;
			Clip clip = null;

			boolean isOnlyBaseGift = isOnlyBaseGiftSupported(giftedTo);

			boolean bGiftSrv = false;
			if(isOnlyBaseGift || (song == null && getParamAsString("GIFT_SERVICE_ACCEPT_URL") != null))
				bGiftSrv = true;
			else if(getParamAsString("GIFT_CHARGE_URL") != null && song != null)
			{
				if (song.startsWith("C"))
				{
					try
					{
						Category category = rbtCacheManager.getCategory(Integer.parseInt(song.substring(1)));
						if (category != null)
						{
							cClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(category.getClassType());
						}
					}
					catch (Throwable e1)
					{
					}
				}
				else
				{
					try
					{
						clip = rbtCacheManager.getClip(Integer.parseInt(song), "eng");
						if (clip != null)
						{
							cClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(clip.getClassType());
						}
					}
					catch (Throwable e1)
					{
					}
				}
			}
			
			String contentType = null;
			if(clip != null)
				contentType = clip.getContentType();

			if(bGiftSrv || cClass != null)
			{
				boolean isNonOnmobilePrefix = isNonOnmobilePrefix(giftedTo);
				logger.info(" isNonOnmobilePrefix(giftedTo) " + isNonOnmobilePrefix + " gifter " + gifter + " giftedTo " + giftedTo);


				String url = getParamAsString("GIFT_CHARGE_URL");
				if(getParamAsBoolean("CHECK_SUBSCRIPTION_STATUS", "FALSE") && isNonOnmobilePrefix)
				{
					String checkURL = getParamAsString("CHECK_SUBSCRIPTION_STATUS_URL");
					checkURL = checkURL + "srcSubscriberID="+giftedTo;
					logger.info("m_checkSubscriptionStatusURL "+ checkURL);
					StringBuffer strBuf = new StringBuffer();
					StringBuffer httpStatus = new StringBuffer();
					String result = callURL(checkURL, true,strBuf, httpStatus);
					if(result == null || result.trim().equals(STATUS_RETRY))
					{
						return STATUS_RETRY;
					}
					else if (result != null && result.trim().startsWith("SUCCESS"))
					{
						url = getParamAsString("GIFT_CHARGE_URL") + "giftee_status=A&";
					}
					else if (result != null && result.trim().equals(STATUS_ERROR))
					{
						url = getParamAsString("GIFT_CHARGE_URL") + "giftee_status=D&";
					}
					else
						return STATUS_RETRY;
				}	

				String chrgClass = null;
				if(cClass != null)
				{
					chrgClass = cClass.getChargeClass();
					if(chrgClass.equals("DEFAULT"))
						chrgClass = getParamAsString("DAEMON", "NORMAL_GIFT_CHRG_CLASS", "DEFAULT");
					if(selectedBy != null && m_modeChrgMap != null && m_modeChrgMap.containsKey(selectedBy))
						chrgClass = (String) m_modeChrgMap.get(selectedBy);
					if(contentType != null && m_contentTypeChrgMap != null && m_contentTypeChrgMap.containsKey(contentType))
						chrgClass = (String) m_contentTypeChrgMap.get(contentType);
				}
				user_info = "&info=";

				/**
				 *  Emotions Rbt Change. 
				 *  If user gifts emotion song, gifter will be charged for default emotion song selection (i.e for base emotion)
				 * 
				 */
				HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				if (extraInfoMap != null && extraInfoMap.containsKey("EMOTION_RBT"))
				{
					String defaultConfig = getParamAsString("COMMON", "EMOTION_RBT_DEFAULT_CONFIG", null);
					if (defaultConfig != null)
					{
						String[] tokens = defaultConfig.split(",");
						String clipIDStr = tokens[0];

						if (tokens.length >= 2)
							chrgClass = tokens[1];

						clip = rbtCacheManager.getClip(clipIDStr, "eng");
					}

				}

				if (clip != null && !isOnlyBaseGift) 
				{
					String id = clip.getClipPromoId();
					String name = clip.getClipName();
					int clipID = clip.getClipId();
					String movieName = clip.getAlbum();
					String clipInfo=clip.getClipInfo();
					if(movieName != null && movieName.length() > 20)
						movieName = movieName.substring(0,20);
					if (name != null)
						name = name.replaceAll("'", "");
					if (clipInfo!=null && !clipInfo.equalsIgnoreCase("null"))
						clipInfo=clipInfo.replaceAll("=", ":");
					else
						clipInfo = "";

					if(clip.getClipGrammar()!= null && clip.getClipGrammar().equalsIgnoreCase("ugc"))
						user_info += getEncodedUrlString("ugccreator:"+clip.getAlbum() +"|");

					user_info += getEncodedUrlString("CONTENT_ID:contentid="+clipID+",wavfile="+clip.getClipRbtWavFile()+",songname:" + name + "|songcode:" + id+"|"+clipInfo+"|moviename:"+movieName);
				}
				else {
					user_info += getEncodedUrlString("songname:|songcode:|moviename:");
				}

				String countryPrefix = getParamAsString("SM_MSISDN_PREFIX");
				if (countryPrefix != null)
				{
					gifter = countryPrefix + gifter;
					giftedTo = countryPrefix + giftedTo;
				}
				
				url = url + "gifter_mdn="+gifter+"&giftee_mdn="+giftedTo+"&refid="+refID+user_info;
				//RBT-14301 Uninor Mnp Changes.
				SubscriberDetail subscriberDetail = RbtServicesMgr
						.getSubscriberDetail(new MNPContext(gifter, "GIFT"));
				String circleID = null;
				if (subscriberDetail != null) {
					circleID = subscriberDetail.getCircleID();
					logger.info("Subscriber circleId: " + circleID
							+ " subscriberID: " + giftedTo
							+ " valid subscriber: "
							+ subscriberDetail.isValidSubscriber());
				}
				try {
					url = m_mainDaemonThread.appendSiteId(circleID, url);
				} catch (MappedSiteIdNotFoundException e) {
					logger.error("", e);
					return null;
				}
				String newUserchrgClass = getGiftChargeClassForNewUser(giftedTo);
				if (newUserchrgClass != null)
					chrgClass = newUserchrgClass;

				Subscriber gifteeObj = RBTDBManager.getInstance().getSubscriber(giftedTo);
				
				String gifteeCosChargeClass = getGiftChargeClassBasedOnGifteeCos(gifteeObj);
				if(gifteeCosChargeClass != null)
					chrgClass = gifteeCosChargeClass;
				
				//Added for Grameen. This 'if' means that user is in re-activation state in SM as we are gifting only song
				if(!bGiftSrv && RBTDBManager.getInstance().isSubDeactive(gifteeObj)) {
					String deactiveSubscriberChargeClass = getReactiveGifteeChargeClass(gifteeObj);
					if(deactiveSubscriberChargeClass != null)
						chrgClass = deactiveSubscriberChargeClass;
				}

				if(chrgClass != null && (extraInfoMap == null || !extraInfoMap.containsKey(iRBTConstant.CHARGE_CLASS))) {
					url += "&eventkey=RBT_SEL_"+chrgClass + "_GIFT";
				}					
				else if(extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.CHARGE_CLASS)) {
					url += "&eventkey=RBT_SEL_"+extraInfoMap.get(iRBTConstant.CHARGE_CLASS) + "_GIFT";
				}
				if(url.indexOf("srvkey") == -1)
				{
					String subClass = "DEFAULT";
					if((extraInfoMap == null || !extraInfoMap.containsKey(iRBTConstant.SUBSCRIPTION_CLASS)) && m_modeSubClassMap != null && selectedBy != null && m_modeSubClassMap.containsKey(selectedBy)) {
						subClass = (String)m_modeSubClassMap.get(selectedBy);
					}else if(extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.SUBSCRIPTION_CLASS)) {
						subClass = extraInfoMap.get(iRBTConstant.SUBSCRIPTION_CLASS);
					}
					url += "&srvkey=RBT_ACT_"+subClass;
				}

				if(selectedBy != null)
					url +="&mode="+selectedBy;
				
				String selMappedStr = getParamAsString(iRBTConstant.COMMON, "SEL_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
				if (selMappedStr != null && extraInfoMap != null) {
					String str[] = selMappedStr.split(";");
					for (int i = 0; i < str.length; i++) {
						String s[] = str[i].split(",");
						if (s.length == 2 && extraInfoMap.containsKey(s[1])){
							url += "&" + s[1] + "=" + extraInfoMap.get(s[1]);
						}
					}
				}
				
				if(extraInfoMap != null && extraInfoMap.containsKey("preCharge")) {
					url += "&precharge="+extraInfoMap.get("preCharge");
				}

				StringBuffer strBuf = new StringBuffer();
				StringBuffer httpStatus = new StringBuffer();
				long startTime = System.currentTimeMillis();
				String result = callURL(url, true,strBuf, httpStatus);
				long endTime = System.currentTimeMillis();

				RBTDaemonManager.writeTrans("SUBMGR-RESPONSE", url, 0, "STATUSCODE("+ httpStatus.toString() + ")|" + strBuf.toString(), (endTime-startTime));
				if(m_mainDaemonThread.m_writeTrans != null)
				{
					try
					{
						m_mainDaemonThread.writeTrans("GIFT", url, Integer.parseInt(httpStatus.toString()), strBuf.toString(), 0);

					}
					catch(Exception e)
					{
						logger.error("Exception: ", e);
					}
				}	
				return result;

			}
		}
		catch(Throwable e)
		{
			logger.error("Exception: ", e);
		}
		return STATUS_ERROR;
	}

	private boolean isOnlyBaseGiftSupported(String giftedTo)
	{
		boolean baseGift = false;
		if (!RBTParametersUtils.getParamAsBoolean("DAEMON", "GIFT_ONLY_BASE_IF_NEWUSER", "FALSE"))
		{
			return false;
		}

		try
		{
			Subscriber subscriber = m_rbtDBManager.getSubscriber(giftedTo);
			if (subscriber == null)
			{
				baseGift = true;
			}
			else if (subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED))
			{
				WebServiceContext task = new WebServiceContext();
				task.put(param_subscriberID, giftedTo);
				Map<String,String> nextBillDateMap = com.onmobile.apps.ringbacktones.webservice.common.Utility.getNextBillingDateOfServices(task);
				String subRefID = subscriber.refID();
				SimpleDateFormat rbtDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				String nextBillDate = nextBillDateMap.get(subRefID);
				Date  dateObj = null;
				if (nextBillDate != null)
					dateObj = rbtDateFormat.parse(nextBillDate);
				if (dateObj != null && dateObj.before(new Date()))
				{
					baseGift = true;
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return baseGift;
	}
	
	private String getReactiveGifteeChargeClass(Subscriber gifteeObj) {
		String reactivatedChargeClassParam = getParamAsString("DAEMON", "REACTIVE_USER_GIFT_CHRG_CLASS", null);
		if(reactivatedChargeClassParam == null)
			return null;
		String reactivatedChargeClass = null;
		/**
		 * This if means based on num max selections of giftee the charge class has to be defined.
		 * Param will be defined as 0:FREE,>1:DEFAULT
		 */
		if(reactivatedChargeClassParam.contains(":")) {
			StringTokenizer stk = new StringTokenizer(reactivatedChargeClassParam, ",");
			while(stk.hasMoreTokens()) {
				String[] split = stk.nextToken().split(":");
				int selCount = -1;
				boolean isGreater = false;
				
				if(split[0].startsWith(">")) {
					selCount = Integer.parseInt(split[0].substring(1));
					isGreater = true;
				}
				else
					selCount = Integer.parseInt(split[0]);
				
				if((isGreater && selCount <= gifteeObj.maxSelections()) || (!isGreater && selCount == gifteeObj.maxSelections())) {
					reactivatedChargeClass = split[1];
					break;
				}
			}
		}
		else {
			reactivatedChargeClass = reactivatedChargeClassParam;
		}
		
		return reactivatedChargeClass;
	}
	
	private String getGiftChargeClassBasedOnGifteeCos(Subscriber gifteeObj) {
		if(getParamAsBoolean("CHARGE_GIFTER_BASED_ON_GIFTEE_COS", "FALSE")) {
			return m_rbtDBManager.getNextChargeClass(gifteeObj);
		}
		return null;
	}

	private String getGiftChargeClassForNewUser(String giftee)
	{
		if(getParamAsBoolean("CHECK_GIFTEE_STATUS", "FALSE"))
		{
			Subscriber subscriber = m_rbtDBManager.getSubscriber(giftee);
			if (m_rbtDBManager.isSubscriberDeactivated(subscriber) )
			{
				return getParamAsString("DAEMON", "NEW_USER_GIFT_CHRG_CLASS", null);
			}
		}
		return null;
	}

	public String connectToRemote(String strSubID, String strMsg)
	{
		try
		{
			int prefixIndex = m_rbtDBManager.getPrefixIndex();
			String strURL = null;
			
			SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(strSubID, "GIFT"));			
			String circleID = null;
			if(subscriberDetail != null) {
				circleID = subscriberDetail.getCircleID();
				logger.info("Subscriber circleId: " + circleID + " subscriberID: " + strSubID + " valid subscriber: " + subscriberDetail.isValidSubscriber());
			}

//			if(checkForValidPrefix(strSubID))
			if(subscriberDetail.isValidSubscriber())
			{
				String jBossIP = getParamAsString("SMS","JBOSS_IP", null);
				String port = ":8080";
				if(jBossIP != null && jBossIP.indexOf(":") == -1)
					jBossIP += port;
				strURL = "http://"+jBossIP+"/rbt/" + strMsg;	
			}
//			else if (m_OperatorPrefixes.contains(subID(strSubID).substring(0,prefixIndex)) && getParamAsString("NATIONAL_URL") != null)
			else if ((circleID != null) && getParamAsString("NATIONAL_URL") != null)
			{
				String siteURL = getParamAsString("NATIONAL_URL");
				siteURL = Tools.findNReplaceAll(siteURL, "rbt_sms.jsp", "");
				siteURL = Tools.findNReplaceAll(siteURL, "?", "");

				strURL = siteURL + strMsg;
			}
			else
				return STATUS_ERROR;
			if (strURL != null)
			{
				return callURL(strURL, false,null,null);
			}
			return STATUS_ERROR;
		}
		catch (Throwable e)
		{
			logger.error("",e);  
			e.printStackTrace();
			return null;
		}
	}
	private String callURL(String url, boolean isChargededuction, StringBuffer strBuf, StringBuffer httpStatusBuf)
	{
		String response = null;
		int statusCode = 0;
		Integer intStat = new Integer(-1);
		StringBuffer sb = new StringBuffer();

		url = url.replaceAll(" ", "%20");
		url = Tools.decodeHTML(url);

		HostConfiguration hcfg = new HostConfiguration();
		PostMethod postMethod = null;
		try
		{
			logger.info("RBT:: url to be called " + url);
			if(url != null)
				url.replaceAll(" ", "%20");
			HttpURL httpURL = new HttpURL(url);
			hcfg.setHost(httpURL);
			postMethod = new PostMethod(url);
			DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
					0, false);
			m_httpClient.getParams()
			.setParameter(HttpMethodParams.RETRY_HANDLER,
					retryhandler);
			m_httpClient.setTimeout(m_timeOutSec * 1000);
			statusCode = m_httpClient.executeMethod(hcfg, postMethod);

			if(sb != null)
				response = postMethod.getResponseBodyAsString();
			if (response != null)
				response = response.trim();
			if(strBuf != null)
				strBuf.append(response);
			if(httpStatusBuf != null)
				httpStatusBuf.append(statusCode);
			logger.info("RBT:: response " + response);

			logger.info("RBT:: statusCode recieved "
					+ statusCode);
			if(!isChargededuction)
			{
				if (statusCode == 200)
					return response;
				else
					return STATUS_ERROR;
			}
			else
			{
				if (response != null && response.startsWith("SUCCESS"))
				{
					if(response.equals("SUCCESS:DEACTIVE") || response.equals("SUCCESS:NEWUSER"))
						return STATUS_ERROR;
					else
						return response;
				}
				else if (response != null && response.startsWith("RETRY"))
					return STATUS_RETRY;
				else
					return STATUS_ERROR;
			}

		}
		catch (Throwable e)
		{
			logger.error("",e);  
			e.printStackTrace();
			return null;
		}
		finally
		{
			if (postMethod != null)
				postMethod.releaseConnection();
		}
	}

	private int giftRBT(ViralSMSTable viral)
	{
		////viral[i].subID(), viral[i].callerID(), viral[i].clipID(), viral[i].sentTime(),viral[i].extraInfo()
		String gifter=viral.subID();
		String giftee=viral.callerID();
		String clipId=viral.clipID();
		Date requestedTimestamp=viral.sentTime();
		String extraInfo=viral.extraInfo();
		String mode = viral .selectedBy();
		try{
			
			boolean isNonOnmobilePrefix = isNonOnmobilePrefix(giftee);
			logger.info(" isNonOnmobilePrefix(giftee) " + isNonOnmobilePrefix + " gifter " + gifter + " giftee " + giftee + " clipId " + clipId );
			if(getParamAsBoolean("CHECK_SUBSCRIPTION_STATUS", "FALSE") && isNonOnmobilePrefix)
			{
				int id = -1;
				try
				{
					id = Integer.parseInt(clipId);
				}
				catch(Exception e)
				{
					id = -1;
				}

				if(id == -1)
					return RBTGIFT_FAILURE;

				Clip clip = rbtCacheManager.getClip(id);
				if(clip == null)
					return RBTGIFT_FAILURE;

				String toneID = null;
				if(clip.getClipRbtWavFile() != null)
					toneID = clip.getClipRbtWavFile().substring(4, clip.getClipRbtWavFile().length() - 4);

				String nonOMgiftURL =  getParamAsString("NON_ONMOBILE_GIFT_URL");

				nonOMgiftURL = nonOMgiftURL + "srcSubscriberID=" + gifter + "&dstRegionID=" + giftee + "&toneID=" + URLEncoder.encode(toneID, "UTF-8") + "&songName=" +URLEncoder.encode(clip.getClipName(), "UTF-8");
				if(extraInfo!=null){
					nonOMgiftURL=nonOMgiftURL+"&extraInfo="+URLEncoder.encode(extraInfo,"UTF-8");
				}
				StringBuffer strBuf = new StringBuffer();
				StringBuffer httpStatus = new StringBuffer();
				String result = callURL(nonOMgiftURL, true,strBuf, httpStatus);

				if(result == null || result.trim().equals(STATUS_RETRY))
				{
					return -1;
				}
				else if (result != null && result.trim().startsWith("SUCCESS"))
				{
					return RBTGIFT_SUCCESS;
				}
				else
					return RBTGIFT_FAILURE;
			}	
			String redirectUrl="rbt_gift.jsp?subscriber_id=" + giftee + "&gifted_by=" + gifter + "&mode=" + mode + "&clip_id=" + clipId + "&requested_timestamp=" + (requestedTimestamp.getTime());
			if(extraInfo!=null){
				redirectUrl=redirectUrl+"&extraInfo="+URLEncoder.encode(extraInfo,"UTF-8");
			}
			String response = connectToRemote(giftee,redirectUrl);
			if (response != null && response.indexOf("SUCCESS") != -1)
			{
				logger.info("Gift successful for the following request gifter " + gifter + " to " + giftee + " for clip Id " + clipId);
				return RBTGIFT_SUCCESS;
			}
			else if (response != null)
			{
				logger.info("Gift failed for the following request gifter " + gifter + " to " + giftee + " for clip Id " + clipId);
				return RBTGIFT_FAILURE;
			}else{
				logger.info("Gift failed for the following request gifter " + gifter + " to " + giftee + " for clip Id " + clipId);
				return RBTGIFT_ERROR;
			}
		}catch(Throwable e){
			logger.info("Gift failed for the following request gifter " + gifter + " to " + giftee + " for clip Id " + clipId);
			logger.error("",e);  
			return RBTGIFT_ERROR;
		}

	}

	private String subID(String strSubID)
	{
		return (m_rbtDBManager.subID(strSubID));
	}

	private boolean checkForValidPrefix(String subid){
		return(m_rbtDBManager.isValidPrefix(subid));
	}

	private boolean isNonOnmobilePrefix(String subID)
	{	
//		if(subID != null && subID.length() > 3)
//		{
//			int prefixIndex = m_rbtDBManager.getPrefixIndex(); 
//			if(m_nonOnmobilePrefix.contains(subID.substring(0, prefixIndex))) 
//				return true;
//		}
//		return false;
		
		SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(subID, "GIFT"));
		String circleID = null;
		if(subscriberDetail != null) {
			circleID =  subscriberDetail.getCircleID();
			logger.info("Subscriber circleId: " + circleID + " subscriberID: " + subID + " valid subscriber: " + subscriberDetail.isValidSubscriber());
		}
		return circleID != null && circleID.equalsIgnoreCase("NON_ONMOBILE");
	}

	private String getParamAsString(String param)
	{
		try{
			return m_rbtParamCacheManager.getParameter("DAEMON", param, null).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return null;
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

	/**
	 * Method to get language specific SMS text from rbt_text table 
	 * @param type
	 * @param subType
	 * @param subID
	 * @param defualtVal
	 * @return smsText
	 */
	private String getSMSText(String type, String subType, String subID,String defualtVal)
	{
		String language=null;

		try{
			Subscriber sub = m_rbtDBManager.getSubscriber(subID);
			if(sub!=null&&sub.language()!=null)
				language=sub.language();
			String smsText= smsTextCacheManager.getSmsText(type, subType, language);
			logger.info("  smstext is ->"+smsText+" for  type ->"+type+" subType -> "+subType+" language -> "+language);
			if(smsText!=null)
				return smsText;
			else
				return defualtVal;
		}catch(Exception e){
			logger.info(" Unable to get SMSText ->"+"  type ->"+type+" subType -> "+subType+" language -> "+language);
			return defualtVal;
		}
	}

	private String getEncodedUrlString(String param)
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

	private int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = m_rbtParamCacheManager.getParameter("DAEMON", param, defaultVal+"").getValue();
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}

	private boolean getParamAsBoolean(String param, String defaultVal)
	{
		try{
			return m_rbtParamCacheManager.getParameter("DAEMON", param, defaultVal).getValue().equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	private boolean getParamAsBoolean(String type, String param, String defaultVal)
	{
		try{
			return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
}