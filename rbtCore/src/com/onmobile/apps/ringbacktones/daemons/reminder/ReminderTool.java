package com.onmobile.apps.ringbacktones.daemons.reminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.TransFileWriter;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.SubscriberDownloadsImpl;
import com.onmobile.apps.ringbacktones.content.database.TnbSubscriberImpl;
import com.onmobile.apps.ringbacktones.content.database.TrialSelectionImpl;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.tools.IntegerTools;
import com.onmobile.apps.ringbacktones.tools.Toolbox;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.common.exception.OnMobileException;

public class ReminderTool implements ConstantsTools
{
	private static Logger logger = Logger.getLogger(ReminderTool.class);
	
	private static final String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "Mei","Jun","Jul","Agt", "Sep", "Okt", "Nov", "Des" };
	private static TransFileWriter tnbOptinTransWriter = null;
	private static TransFileWriter tnbOptoutTransWriter = null;
	private static TransFileWriter trialTransWriter = null;
	private static String tnbOptinDir = ".";
	private static String tnbOptoutDir = ".";
	private static String trialDir = ".";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private static SimpleDateFormat timestampFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	public static HashMap<String,ArrayList<Integer>> tnbOptinMap = new HashMap<String, ArrayList<Integer>>(); 
	public static HashMap<String,ArrayList<Integer>> tnbOldOptinMap = new HashMap<String, ArrayList<Integer>>();
	public static HashMap<String,ArrayList<Integer>> tnbOptoutMap = new HashMap<String, ArrayList<Integer>>(); 
	public static HashMap<String,ArrayList<Integer>> esiaTrialMap = new HashMap<String, ArrayList<Integer>>();
	public static ArrayList<String> siteList = null;
	public static Calendar blackOutStartCal = null;
	public static Calendar blackOutEndCal = null;
	public static Object lock = new Object();
	public static ArrayList<String> reminderDaemonModes = null;
	public static void init()
	{
		try
		{
			if(siteList != null )
				return;
			synchronized (lock)
			{
				if(siteList != null )
					return;
				getReminderModes();
				getSites();
				getTNBOptinClasses();
				getOldTNBOptinClasses();
				getTNBOptoutClasses();
				getTrialClassesToProcess();
			}
		}
		catch(Throwable t)
		{
			logger.error("Exception in ReminderToold init", t);
		}
	}
	private static void getReminderModes()
	{
		reminderDaemonModes = DBConfigTools.getParameter("SMS", "SMS_DAEMON_MODE", "SUB",",");
	}
	public static void processTNBOptin(TnbSubscriberImpl tnbSubscriberImpl, int smsDay)
	{
		logger.info("Start process for:"+tnbSubscriberImpl);
		if(isBlackOutPeriod())
		{
			logger.info("Not processing seqId="+tnbSubscriberImpl.seqID()+", for sub="+tnbSubscriberImpl.subID()+" due to black-out");
			return;
		}	
		int iterId = tnbSubscriberImpl.iterID();
		String subId = tnbSubscriberImpl.subID();
		long seqId = tnbSubscriberImpl.seqID();
		boolean isNonFreeSelectionExists = false;
		String chargePack = tnbSubscriberImpl.chargepack();
		SubscriptionClass subscriptionClass = DBConfigTools.getSubscriptionClass(chargePack);
		if(subscriptionClass == null)
			return;
		Subscriber subscriber  = DBConfigTools.getSubscriber(subId);
		if(subscriber == null || subscriber.subscriptionClass() == null || !subscriber.subscriptionClass().equalsIgnoreCase(chargePack))
		{
			logAndRemove(tnbSubscriberImpl, smsDay, "DEACTERROR");
			return;
		}	
			
		boolean isTNBNewFlow = DBConfigTools.getParameter(DAEMON, SUPPORT_TNB_NEW_FLOW, false);
		boolean is121TnbEnabled = Boolean.parseBoolean(RBTParametersUtils.getParamAsString("COMMON","121_TNB_SUBSCRIPTION_CLASS_ENABLED", "FALSE"));
		if(isTNBNewFlow && is121TnbEnabled) {
			isTNBNewFlow = false;
		}
		if(DBConfigTools.getParameter(SMS, UPDATE_TNB_TO_NORMAL_ON_DEFAULT_SELECTION, false) && !isTNBNewFlow) 
		{	
			if (DBConfigTools.getParameter(COMMON,ADD_TO_DOWNLOADS,false))
			{
				SubscriberDownloads[] subDownloads = DBConfigTools.rbtDBManager.getNonFreeDownloads(subId, RBTParametersUtils
						.getParamAsString("DAEMON",
								"TNB_FREE_CHARGE_CLASS",
								ConstantsTools.FREE));
				if (subDownloads != null && subDownloads.length > 0)
					isNonFreeSelectionExists = true;
			}
			else
			{
				SubscriberStatus[] subSel = DBConfigTools.rbtDBManager.getNonFreeSelections(subId, RBTParametersUtils
						.getParamAsString("DAEMON",
								"TNB_FREE_CHARGE_CLASS",
								ConstantsTools.FREE));
				if (subSel != null && subSel.length > 0)
					isNonFreeSelectionExists = true;
			}
		}
		if (isNonFreeSelectionExists)
		{
			if (updateTNBSubscribertoNormal(subId, subscriptionClass.getSubscriptionPeriodInDays()))
			{
				String unsubscriptionMsg = DBConfigTools.getSmsText(SMS, UPGRADE_TNB_MSG, subscriber.language());
				sendSms(subId, unsubscriptionMsg);
				logAndRemove(tnbSubscriberImpl, smsDay, "UPGRADE");
				if (DBConfigTools.getParameter(COMMON,ADD_TO_DOWNLOADS,false))
					DBConfigTools.rbtDBManager.smUpdateDownloadTNBCallback(subscriber);				
			}
		}
		else 
		{
			if(isLastDayForTNBOptin(tnbSubscriberImpl))
			{
				if(!isTNBNewFlow)
					deactivateTNBSubscriber(tnbSubscriberImpl);
				logAndRemove(tnbSubscriberImpl, smsDay, "DEACT");
				return;
			}
			try
			{
				String smsText = prepareTNBOptinSms(tnbSubscriberImpl, subscriber, smsDay);
				sendSms(subId, smsText);
				log(tnbOptinTransWriter, tnbSubscriberImpl, smsDay, "REMINDER", smsText);
				updateTnbSubscriberIterId(seqId, ++iterId);
				
			}
			catch (OnMobileException e) 
			{
				logger.error("", e);
			}
		}
	}
	
	private static void log(TransFileWriter tfw, TnbSubscriberImpl tnbSubscriberImpl, int smsDay, String action, String smsText)
	{
		writeTrans(tfw, tnbSubscriberImpl.seqID(), tnbSubscriberImpl.subID(), tnbSubscriberImpl.circleID(), tnbSubscriberImpl.chargepack(),
				tnbSubscriberImpl.startDate(), tnbSubscriberImpl.iterID(),smsDay , action, smsText);
	}
	
	private static void log(TransFileWriter tfw, TrialSelectionImpl trialSelection, int smsDay, String action, String smsText)
	{
		writeTrans(tfw, trialSelection.seqID(), trialSelection.subID(), trialSelection.circleID(), trialSelection.chargepack(),
				trialSelection.startDate(), trialSelection.iterID(),smsDay , action, smsText);
	}
	
	private static void logAndRemove(TnbSubscriberImpl tnbSubscriberImpl, int smsDay, String action)
	{
		
		try
		{
			deleteTnbSubscriber(tnbSubscriberImpl.seqID());
			log(tnbOptinTransWriter, tnbSubscriberImpl, smsDay, action, "-");
		}
		catch(OnMobileException oe)
		{
			logger.error("", oe);
		}
		
	}

	private static void deactivateTNBSubscriber(TnbSubscriberImpl tnbSubscriberImpl)
	{
		StringBuffer strBuff=new StringBuffer();
		String deactivatedBy = DBConfigTools.getParameter(DAEMON, DEACT_MODE_OPT_IN, "SMSDaemon");
		RBTConnector.getInstance().getSubscriberRbtclient().unsubscribe(tnbSubscriberImpl.subID(), true, deactivatedBy,"SMSDaemon", strBuff);
	}

	private static boolean isLastDayForTNBOptin( TnbSubscriberImpl tnbSubscriberImpl)
	{
		String chargePack = tnbSubscriberImpl.chargepack();
		SubscriptionClass subscriptionClass = DBConfigTools.getSubscriptionClass(chargePack);
		int subscriptionPeriod = subscriptionClass.getSubscriptionPeriodInDays();
		
		Date startDate = tnbSubscriberImpl.startDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.add(Calendar.DAY_OF_YEAR, subscriptionPeriod);
		calendar.set(Calendar.HOUR_OF_DAY,0);calendar.set(Calendar.MINUTE,0);calendar.set(Calendar.SECOND,0);
		Date calculatedEndDate = calendar.getTime();
		if(calculatedEndDate.before(Calendar.getInstance().getTime()))
			return true;
		return false;
	}

	private static boolean updateTnbSubscriberIterId(long seqId, int iterId) throws OnMobileException
	{
		
		return DBConfigTools.rbtDBManager.updateTNBSubscriberIterId(seqId, iterId);
	}

	private static boolean updateTrialSelectionIterId(long seqId, int iterId) throws OnMobileException
	{
		
		return DBConfigTools.rbtDBManager.updateTrialSelectionIterId(seqId, iterId);
	}
	
	private static boolean deleteTnbSubscriber(long seqId ) throws OnMobileException
	{
		
		return DBConfigTools.rbtDBManager.deleteTNBSubscriber(seqId);
	}

	public static boolean updateTNBSubscribertoNormal(String subId, int subscriptionPeriod)
	{
		return DBConfigTools.rbtDBManager.updateTNBSubscribertoNormal(subId,true, subscriptionPeriod);
	}
	
	public static void sendSms(String subId, String sms)
	{
		if(sms == null || sms.trim().length() == 0)
			return;
		if(subId == null || subId.trim().length() == 0)
			return;
		try
		{
			Tools.sendSMS(DBConfigTools.getParameter(SMS,SMS_NO,"123456"), subId, sms, false);
		}
		catch (OnMobileException e)
		{
			
		}
	}
	
	public static String prepareTNBOptinSms(TnbSubscriberImpl tnbSubscriber, Subscriber subscriber, int smsDay)
	{
		String smsText= DBConfigTools.getSmsText(tnbSubscriber.chargepack() + "_SMS", smsDay+"", subscriber.language());
		if (smsText == null)
		{
			smsText = DBConfigTools.getSmsText(DBConfigTools.getParameter(SMS, BULK_PROMOID, null), smsDay+"", subscriber.language());
			if (smsText == null)
			{
				logger.info("SMS Text not found. No sms being sent");
				return null;
			}
		}
		if (smsText.contains("%date"))
		{
			String date = "";
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(subscriber.endDate());
			date = calendar.get(Calendar.DAY_OF_MONTH)+"/"+(calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.YEAR);
			smsText = smsText.replace("%date", date);
		}
		return smsText;
	}

	public static void processTNBOptout(TnbSubscriberImpl tnbSubscriberImpl, int smsDay) 
	{
		logger.info("Start process for:"+tnbSubscriberImpl);
		if(isBlackOutPeriod())
		{
			logger.info("Not processing seqId="+tnbSubscriberImpl.seqID()+", for sub="+tnbSubscriberImpl.subID()+" due to black-out");
			return;
		}	
		int iterId = tnbSubscriberImpl.iterID();
		String subId = tnbSubscriberImpl.subID();
		long seqId = tnbSubscriberImpl.seqID();
		String chargePack = tnbSubscriberImpl.chargepack();
		SubscriptionClass subscriptionClass = DBConfigTools.getSubscriptionClass(chargePack);
		if(subscriptionClass == null)
			return;
		Subscriber subscriber  = DBConfigTools.getSubscriber(subId);
		if(subscriber == null)
			return;
		
		String sms=prepareTNBOptoutSms(tnbSubscriberImpl, subscriber, smsDay);
		sendSms(subId, sms);
		try
		{
			updateTnbSubscriberIterId(seqId, ++iterId);
			log(tnbOptoutTransWriter, tnbSubscriberImpl, smsDay, "REMINDER", sms);
		}
		catch (OnMobileException oe)
		{
			logger.error("", oe);
		}
			
	}
	
	public static String prepareTNBOptoutSms(TnbSubscriberImpl tnbSubscriber, Subscriber subscriber, int smsDay)
	{
		String smsText= DBConfigTools.getSmsText(tnbSubscriber.chargepack(), smsDay+"", subscriber.language());
		if (smsText == null)
		{
			logger.info("SMS Text not found. No sms being sent");
			return null;
			
		}
		return smsText;
	}

	public static void processTrialSelection(TrialSelectionImpl trialSelectionImpl, int smsDay)
	{

		logger.info("Start process for:"+trialSelectionImpl);
		if(isBlackOutPeriod())
		{
			logger.info("Not processing seqId="+trialSelectionImpl.seqID()+", for sub="+trialSelectionImpl.subID()+" due to black-out");
			return;
		}	
		int iterId = trialSelectionImpl.iterID();
		String subId = trialSelectionImpl.subID();
		long seqId = trialSelectionImpl.seqID();
		String chargePack = trialSelectionImpl.chargepack();
		SubscriptionClass subscriptionClass = DBConfigTools.getSubscriptionClass(chargePack);
		if(subscriptionClass == null)
			return;
		Subscriber subscriber  = DBConfigTools.getSubscriber(subId);
		if(subscriber == null)
			return;
		try
		{
			String smsText = prepareTrialSms(trialSelectionImpl, subscriber, smsDay); 
			sendSms(subId, smsText);
			updateTnbSubscriberIterId(seqId, iterId);
			log(trialTransWriter, trialSelectionImpl, smsDay, "REMINDER", smsText);
		}
		catch(Throwable e)
		{
			logger.info("exception for sub="+subId+" ==> " + Toolbox.getStackTrace(e));
		}
		/*finally
		{
			 
			 writeTrans("SMS_DAEMON", subscriberID+":"+trialChargeClasses.get(t).getChargeClass()+":"+smsDay , smsSent+"", new Date()+"");
		}*/
	 	
	}
	
	public static String prepareTrialSms(TrialSelectionImpl trialSelectionImpl, Subscriber subscriber, int smsDay)
	{
		String smsText = DBConfigTools.getSmsText(trialSelectionImpl.chargepack(),smsDay+"",subscriber.language());
		if(smsText == null)
			return null;
		int dateIndex = smsText.indexOf("%date");
		if (dateIndex >= 0) 
		{
			int day = Integer.parseInt(smsText.substring(dateIndex + 5, smsText.indexOf('%',dateIndex + 5)));
			Calendar cal = Calendar.getInstance();
			ChargeClass trialClass = DBConfigTools.getChargeClass(trialSelectionImpl.chargepack());
			int selectionPeriodTrial = Integer.parseInt(trialClass.getSelectionPeriod().trim().substring(1));
			cal.setTime(trialSelectionImpl.startDate()); 
			cal.add(Calendar.DAY_OF_YEAR, selectionPeriodTrial); // To get at next charging date
			cal.add(Calendar.DAY_OF_YEAR, (day-selectionPeriodTrial)); 
			 
			String date = cal.get(Calendar.DATE)+ MONTHS[cal.get(Calendar.MONTH)];
			String tmpSMSText = smsText.substring(0, dateIndex);
			tmpSMSText += date;
			tmpSMSText += smsText.substring(smsText.indexOf('%', dateIndex + 5) + 1);
			smsText = tmpSMSText;
		}
		if(smsText.indexOf("%PROMO") != -1 )
		{
			SubscriberStatus subscriberStatus = DBConfigTools.getTrialSelection(trialSelectionImpl.subID(), trialSelectionImpl.chargepack());
			if(subscriberStatus != null && subscriberStatus.subscriberFile() !=  null)
			{
				Clip clip = DBConfigTools.rbtCacheManager.getClipByRbtWavFileName(subscriberStatus.subscriberFile());
				if(clip != null && clip.getClipPromoId() != null)
				{
					String tempSMSText = smsText.substring(0, smsText.indexOf("%PROMO"));
					tempSMSText = tempSMSText + " " + clip.getClipPromoId();
					tempSMSText = tempSMSText + " " +  smsText.substring(smsText.indexOf("%PROMO")+6);
					smsText =  tempSMSText;
				}
			}	
		}
		logger.info("smsText=" + smsText);
		return smsText;
	}
	
	public static void initTnbOptinTransactionFile()
	{
		tnbOptinDir = DBConfigTools.getParameter(DAEMON, TNB_OPTIN_REMINDER_TRANS_DIR, ".");
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("SEQ_ID");
		headers.add("MSISDN");
		headers.add("CIRCLE_ID");
		headers.add("CHARGE_PACK");
		headers.add("START_DATE");
		headers.add("ITER_ID");
		headers.add("SMS_DAY");
		headers.add("ACTION");
		headers.add("TRANS_TIME");
		headers.add("SMS_TEXT");
		tnbOptinTransWriter = new TransFileWriter(tnbOptinDir, "TNB_OPTIN_TRANS", headers);
	}
	
	public static void initTnbOptoutTransactionFile()
	{
		tnbOptoutDir = DBConfigTools.getParameter(DAEMON, TNB_OPTOUT_REMINDER_TRANS_DIR, ".");
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("SEQ_ID");
		headers.add("MSISDN");
		headers.add("CIRCLE_ID");
		headers.add("CHARGE_PACK");
		headers.add("START_DATE");
		headers.add("ITER_ID");
		headers.add("SMS_DAY");
		headers.add("ACTION");
		headers.add("TRANS_TIME");
		headers.add("SMS_TEXT");
		tnbOptoutTransWriter = new TransFileWriter(tnbOptoutDir, "TNB_OPTOUT_TRANS", headers);
	}
	
	public static void initTrialTransactionFile()
	{
		trialDir = DBConfigTools.getParameter(DAEMON, TRIAL_REMINDER_TRANS_DIR, ".");
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("SEQ_ID");
		headers.add("MSISDN");
		headers.add("CIRCLE_ID");
		headers.add("CHARGE_PACK");
		headers.add("START_DATE");
		headers.add("ITER_ID");
		headers.add("SMS_DAY");
		headers.add("ACTION");
		headers.add("TRANS_TIME");
		headers.add("SMS_TEXT");
		trialTransWriter = new TransFileWriter(trialDir, "TNB_OPTIN_TRANS", headers);
	}
	
	public static void writeTrans(TransFileWriter tfw, long seqId, String msisdn, String circleId, String chargePack, Date startDate,
			int iterId, int smsDay, String action, String smsText)
	{
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("SEQ_ID", seqId+"");
		h.put("MSISDN", msisdn);
		h.put("CIRCLE_ID", circleId);
		h.put("CHARGE_PACK", chargePack);
		h.put("START_DATE", dateFormat.format(startDate));
		h.put("ITER_ID", iterId+"");
		h.put("SMS_DAY", smsDay+"");
		h.put("ACTION", action);
		h.put("TRANS_TIME", timestampFormat.format(Calendar.getInstance().getTime()));
		h.put("SMS_TEXT", smsText);
		logger.info("h=" + h);
		if (tfw != null)
			tfw.writeTrans(h);
	}
	
	public static void getSites()
	{
		logger.info("Entering");
		List<SitePrefix> localSites = CacheManagerUtil.getSitePrefixCacheManager().getLocalSitePrefixes();
		if(localSites == null || localSites.size() == 0)
			return;
		siteList = new ArrayList<String>();
		for(SitePrefix sitePrefix : localSites)
			siteList.add(sitePrefix.getCircleID());
		logger.info("Exit With siteList="+siteList);
	}
	
	public static void getOldTNBOptinClasses()
	{
		ArrayList<String> tnbSubClasses = DBConfigTools.getParameter("COMMON", "OLD_TNB_SUBSCRIPTION_CLASSES", "ZERO",",");
		if(tnbSubClasses == null || tnbSubClasses.size() == 0)
		{
			logger.info("No TNB OLD OPTIN classes configured");
			return;
		}
		logger.info("TNB OLD OPTIN sub classes for reminders ="+tnbSubClasses);
		for (String tnbSubClass : tnbSubClasses) 
		{
			String smsDaysString = DBConfigTools.getParameter("SMS", "SMS_DAYS_"+tnbSubClass, null);
			if (smsDaysString == null)
				smsDaysString = DBConfigTools.getParameter("SMS", "SMS_DAYS", null);
			ArrayList<Integer> list = IntegerTools.getIntegers(smsDaysString, null, ",");
			Toolbox.reorderNumbers(list);
			if(list == null || list.size() == 0)
			{
				logger.info("No reminder days configured for OLD TNB OPTIN sub class "+tnbSubClass);
				continue;
			}
			tnbOldOptinMap.put(tnbSubClass, list);
		}
	}
	
	public static void getTNBOptinClasses()
	{
		ArrayList<String> tnbSubClasses = DBConfigTools.getParameter("COMMON", "TNB_SUBSCRIPTION_CLASSES", "ZERO",",");
		if(tnbSubClasses == null || tnbSubClasses.size() == 0)
		{
			logger.info("No TNB OPTIN classes configured");
			return;
		}
		logger.info("TNB OPTIN sub classes for reminders ="+tnbSubClasses);
		for (String tnbSubClass : tnbSubClasses) 
		{
			String smsDaysString = DBConfigTools.getParameter("SMS", "SMS_DAYS_"+tnbSubClass, null);
			if (smsDaysString == null)
				smsDaysString = DBConfigTools.getParameter("SMS", "SMS_DAYS", null);
			ArrayList<Integer> list = IntegerTools.getIntegers(smsDaysString, null, ",");
			Toolbox.reorderNumbers(list);
			if(list == null || list.size() == 0)
			{
				logger.info("No reminder days configured for TNB OPTIN sub class "+tnbSubClass);
				continue;
			}
			tnbOptinMap.put(tnbSubClass, list);
		}
	}
	
	public static void getTNBOptoutClasses()
	{
		ArrayList<String> tnbSubClasses = DBConfigTools.getParameter("SMS", "SUB_CLASSES_TO_SEND_SMS", null,",");
		if(tnbSubClasses == null || tnbSubClasses.size() == 0)
		{
			logger.info("No TNB OPTOUT sub classes for reminders found.");
			return;
		}
		logger.info("TNB OPTOUT sub classes for reminders ="+tnbSubClasses);
		for (String tnbSubClass : tnbSubClasses) 
		{
			String smsDaysString = DBConfigTools.getParameter("SMS", tnbSubClass+"_SMS_DAYS", null);
			ArrayList<Integer> list = IntegerTools.getIntegers(smsDaysString, null, ",");
			Toolbox.reorderNumbers(list);
			if(list == null || list.size() == 0)
			{
				logger.info("No reminder days configured for TNB OPTIN sub class "+tnbSubClass);
				continue;
			}
			tnbOptoutMap.put(tnbSubClass, list);
		}
	}

	public static void getTrialClassesToProcess()
	{
		List<ChargeClass> trialChargeClasses =  DBConfigTools.getTrialChargeClasses();
		
		if (trialChargeClasses == null && trialChargeClasses.size() == 0)
		{
			logger.info("No trial classes found. Trial reminder hunters will not be set.");
			return ;
		}
		logger.info(trialChargeClasses.size() + " trial charge classes found.");
		for (ChargeClass trialClass : trialChargeClasses) 
		{
			ArrayList<Integer> smsDays = new ArrayList<Integer>();
			List<BulkPromoSMS> bulkPromoSMSes = CacheManagerUtil.getBulkPromoSMSCacheManager().getAllPromoIDSMSes(trialClass.getChargeClass());
			for (BulkPromoSMS bulkPromoSMS : bulkPromoSMSes)
				smsDays.add(IntegerTools.getInteger(bulkPromoSMS.getSmsDate()));
			Toolbox.reorderNumbers(smsDays);
			if(smsDays == null || smsDays.size() == 0)
				logger.info("No reminder days configured for trial class "+trialClass.getChargeClass());
			if(trialClass.getChargeClass() != null && smsDays!= null && smsDays.size() > 0)
				esiaTrialMap.put(trialClass.getChargeClass(), smsDays);
		}
	}
	
	private static void getBlackOutDates()
	{
		 Calendar curCal = Calendar.getInstance();
		 String blackOutStartConfig = DBConfigTools.getParameter("SMS", "BLACKOUT_START_TIME", null);
		 if(blackOutStartConfig == null)
			 blackOutStartConfig = "00:00:00";
		 String blackOutEndConfig = DBConfigTools.getParameter("SMS", "BLACKOUT_END_TIME", null);
		 if(blackOutEndConfig == null)
			 blackOutEndConfig = "00:00:01";
		 StringTokenizer stTimeTokens = new StringTokenizer(blackOutStartConfig, ":");
		 if(stTimeTokens.countTokens() != 3)
			 stTimeTokens = new StringTokenizer("00:00:00", ":");
		 curCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(stTimeTokens.nextToken()));
		 curCal.set(Calendar.MINUTE, Integer.parseInt(stTimeTokens.nextToken()));
		 curCal.set(Calendar.SECOND, Integer.parseInt(stTimeTokens.nextToken()));
		 blackOutStartCal = curCal;

		 curCal = Calendar.getInstance();
		 StringTokenizer enTimeTokens = new StringTokenizer(blackOutEndConfig, ":");
		 if(enTimeTokens.countTokens() != 3)
			 enTimeTokens = new StringTokenizer("00:00:01", ":");
		 curCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(enTimeTokens.nextToken()));
		 curCal.set(Calendar.MINUTE, Integer.parseInt(enTimeTokens.nextToken()));
		 curCal.set(Calendar.SECOND, Integer.parseInt(enTimeTokens.nextToken()));
		 blackOutEndCal = curCal;
		 
		 if (blackOutEndCal.getTime().before(blackOutStartCal.getTime())) 
			 blackOutEndCal.add(Calendar.DAY_OF_YEAR, 1);
	}
	
	private static boolean isBlackOutPeriod()
	{
		getBlackOutDates();
		Date currDate = new Date(System.currentTimeMillis());
		logger.info("Checking black-out. Blackout Start Date="+blackOutStartCal +"; Blackout End Date="+blackOutEndCal+"; Current date="+currDate);
		boolean isBlackOut = false;
		if(currDate.after(blackOutStartCal.getTime()) && currDate.before(blackOutEndCal.getTime()))
			isBlackOut = true;
		logger.info("isBlackOut="+isBlackOut);
		return isBlackOut;
	}


	

}
