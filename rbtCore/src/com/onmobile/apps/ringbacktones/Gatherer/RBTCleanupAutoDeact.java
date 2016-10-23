package com.onmobile.apps.ringbacktones.Gatherer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberActivityCountsDAO;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;
import com.onmobile.common.exception.OnMobileException;

public class RBTCleanupAutoDeact implements iRBTConstant
{
    private static Logger logger = Logger.getLogger(RBTCleanupAutoDeact.class);
    static String loggerName = "CLEAN_EXPIRED_DB_RECORDS"; 
    private static Logger cleanUpLogger = RbtLogger.createRollingFileLogger(RbtLogger.reporterStatisticsPrefix+loggerName, ROLLING_FREQUENCY.YEARLY);
    RBTDBManager m_rbtDBManager = null; 
    RbtGenericCacheWrapper rbtGenericCache = null;

    private String m_autodeact_alert_sms = "Your Caller Tune  subscription will be deactivated in couple of days because of inactivity";
    private String m_freeAct_charge_alert_sms = "Your free RingBackTone subscription ends in %N days time. You will be charged for the same from then on";

    protected static final String sqlTimeSpec = "YYYY/MM/DD HH24:MI:SS";
    protected static final DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    private static final String PARAMETER_TYPE = "GATHERER";
    
    public boolean init()
    {
    	rbtGenericCache = RbtGenericCacheWrapper.getInstance();
		m_rbtDBManager = RBTDBManager.getInstance();
		
    	return true;
    }

    public void cleanUp()
    {
    	long start = -1;
    	int countSelDeleted = -1;
        long selDeleteEnd = -1;
    	int countDownloadDeleted = -1;
    	long downloadsDeleteEnd = -1;
    	int countBookmarksDeleted = -1;
    	long bookmarksDeleteEnd = -1;
    	int countSubscibersDeleted = -1;
    	long subscribersDeleteEnd = -1;
    	int countViralsDeleted = -1;
    	long viralDeleteEnd = -1;
    	int countOldScratchCardDeleted = -1;
    	long scratchCardDeleteEnd  = -1;
    	int countTransDeleted = -1;
    	long transDeleteEnd = -1;
    	int countSubscibersActivityCountDeleted = -1;
    	long subActivityCountDeleteEnd = -1;
    	int countPODDeleted = -1;
    	long podDeleteEnd = -1;
    	int countConsentRecordDeleted = -1;
    	long consentDeleteEnd = -1;
    	try
    	{
    		start = System.currentTimeMillis();
	    	countSelDeleted = cleanSelections();
	        selDeleteEnd = System.currentTimeMillis();
	    	countDownloadDeleted = cleanDownloads();
	    	downloadsDeleteEnd = System.currentTimeMillis();
	    	countBookmarksDeleted = cleanBookmarks();
	    	bookmarksDeleteEnd = System.currentTimeMillis();
	    	countSubscibersDeleted = cleanSubscribers();
	    	subscribersDeleteEnd = System.currentTimeMillis();
	    	countViralsDeleted = cleanViralTableEntries();
	    	viralDeleteEnd = System.currentTimeMillis();
	    	countOldScratchCardDeleted = cleanOldScratchCards();
	    	scratchCardDeleteEnd = System.currentTimeMillis();
	    	countTransDeleted = cleanTransData();
	    	transDeleteEnd = System.currentTimeMillis();
	    	countSubscibersActivityCountDeleted = cleanSubscriberActivityCounts();
	    	subActivityCountDeleteEnd = System.currentTimeMillis();
	    	countPODDeleted = cleanPickOfTheDay();
	    	podDeleteEnd = System.currentTimeMillis();
	    	countConsentRecordDeleted = cleanConsentRecords();
	    	consentDeleteEnd = System.currentTimeMillis();
    	}
    	catch(Exception e)
    	{
    		logger.info("Issue while claening DB", e);
    	}
    	StringBuilder strBuilder = new StringBuilder();
    	strBuilder.append(",");
    	strBuilder.append(countSelDeleted);strBuilder.append(",");
    	strBuilder.append((selDeleteEnd-start)/1000);strBuilder.append(",");
    	strBuilder.append(countDownloadDeleted);strBuilder.append(",");
    	strBuilder.append((downloadsDeleteEnd-selDeleteEnd)/1000);strBuilder.append(",");
    	strBuilder.append(countSubscibersDeleted);strBuilder.append(",");
    	strBuilder.append((subscribersDeleteEnd-bookmarksDeleteEnd)/1000);strBuilder.append(",");
    	strBuilder.append(countViralsDeleted);strBuilder.append(",");
    	strBuilder.append((viralDeleteEnd-subscribersDeleteEnd)/1000);strBuilder.append(",");
    	strBuilder.append(countOldScratchCardDeleted);strBuilder.append(",");
    	strBuilder.append((scratchCardDeleteEnd-viralDeleteEnd)/1000);strBuilder.append(",");
    	strBuilder.append(countTransDeleted);strBuilder.append(",");
    	strBuilder.append((transDeleteEnd-scratchCardDeleteEnd)/1000);strBuilder.append(",");
    	strBuilder.append(countBookmarksDeleted);strBuilder.append(",");
    	strBuilder.append((bookmarksDeleteEnd-downloadsDeleteEnd)/1000);strBuilder.append(",");
    	strBuilder.append(countSubscibersActivityCountDeleted);strBuilder.append(",");
    	strBuilder.append((subActivityCountDeleteEnd-transDeleteEnd)/1000);strBuilder.append(",");
    	strBuilder.append(countPODDeleted);strBuilder.append(",");
    	strBuilder.append((podDeleteEnd - subActivityCountDeleteEnd)/1000);
    	strBuilder.append(countConsentRecordDeleted);strBuilder.append(",");
    	strBuilder.append((consentDeleteEnd - podDeleteEnd)/1000);
    	cleanUpLogger.info(strBuilder);
    }

    private int cleanSelections()
    {
        int count = m_rbtDBManager.cleanOldSelections(getParamAsInt("OLDSELECTIONS_CLEANING_PERIOD_IN_DAYS", 2),getParamAsBoolean("COMMON", "USE_SUBSCRIPTION_MANAGER", "FALSE"));
        logger.info("RBT::cleaned selections older than "+ getParamAsInt("OLDSELECTIONS_CLEANING_PERIOD_IN_DAYS", 2) + "days with status");
        return count;
    }

    private int cleanDownloads()
    {
        if(getParamAsBoolean("DAEMON", "PROCESS_DOWNLOADS", "FALSE"))
        	return m_rbtDBManager.cleanOldDownloads(getParamAsInt("OLDSELECTIONS_CLEANING_PERIOD_IN_DAYS", 2));
        return -1;
    }

    private int cleanBookmarks()
    {
    	int count = 0;
    	int bookMarkCleanupDays = getParamAsInt("OLD_BOOKMARKS_CLEANING_PERIOD_IN_DAYS", 7);
        if (bookMarkCleanupDays > 0)
        {
        	logger.info("Cleaning up bookMarks after configuredDays :"+bookMarkCleanupDays);
        	count = m_rbtDBManager.removeOldBookMarks(bookMarkCleanupDays);
        }
        return count;
    }
    private int cleanSubscribers()
    {
		return m_rbtDBManager
				.cleanSubscribers(
						(getParamAsInt(
								"OLDSUBSCRIBERS_CLEANING_PERIOD_IN_DAYS",
								getParamAsInt(
										"OLDSELECTIONS_CLEANING_PERIOD_IN_DAYS",
										2))),
						getParamAsBoolean("COMMON", "USE_SUBSCRIPTION_MANAGER",
								"FALSE"));
    } 

    private int cleanOldScratchCards()
    {
    	//rwemoving old scratch card
    	List<String> stateList = Arrays.asList(getParamAsString(GATHERER, "SCRATCHCARD_STATE_TOBE_CLEANED", "1").split(","));
    	int count = 0;
    	for(String state : stateList) {
		 count += m_rbtDBManager.removeOldScratchCard(state, getParamAsInt("OLDSCRATCHCARDS_CLEANING_PERIOD_DAYS",7));
		 logger.info("RBT::cleaned Scratch Cards older than "
					+ getParamAsInt("OLDSCRATCHCARDS_CLEANING_PERIOD_DAYS",7) + "days");
    	}
		 return count;
		
    }
    private int cleanViralTableEntries()
    {
        int count = 0;
        boolean status = false;
		int clearDays = getParamAsInt("SMS", "DEACTIVATION_CONFIRM_CLEAR_DAYS", 1);
        if(clearDays > 0 && getParamAsBoolean("SMS", "CONFIRM_DEACTIVATION", "FALSE"))
        {
        	count+= m_rbtDBManager.removeOldViralSMS("CAN", clearDays);
        	logger.info("RBT::cleaned CAN contexts older than "+ clearDays + "days with status = " + status);
        }

        int cleaningPeriodInDays = getParamAsInt("OLDCONTEXTS_CLEANING_PERIOD_IN_DAYS", 2);
        if (cleaningPeriodInDays == 0)
        	logger.info("RBT::cleanContexts  Cleaning period not set so returning "+ cleaningPeriodInDays);
        else
        {
        	String defaultContexts = "CHANGEDMSISDN,REQUEST,CATEGORY,ACCEPTED,REJECTED,GIFTFAILED,COPIED" +
        		",RETAILER,RETAILEREXPIRE,EC_PROCESSED,PROFILE,OPTIN,SMSCONFPENDING,DELETE,SEARCH,CAT_SEARCH,CANCEL_OFFER,VIRAL_PENDING,VIRAL_EXPIRED,COPYEXPIRED";
	        List<String> contextList = Arrays.asList(getParamAsString(GATHERER, "CONTEXTS_TOBE_CLEANED", defaultContexts).split(","));
	        for (String context : contextList)
			{
	        	count += m_rbtDBManager.removeOldViralSMS(context, cleaningPeriodInDays);
	            logger.info("RBT::cleaned " + context + " contexts older than "+ cleaningPeriodInDays + " days with status = " + status);
			}
	        
	        if(!getParamAsBoolean("GIFT_CLEAR_GIFTEE_SEND_SMS", "FALSE"))
			{
				count += m_rbtDBManager.removeOldViralSMS("GIFTED", getParamAsInt("OLDCONTEXTS_CLEANING_PERIOD_IN_DAYS", 2));
			    logger.info("RBT::cleaned GIFTED contexts older than "+ getParamAsInt("OLDCONTEXTS_CLEANING_PERIOD_IN_DAYS", 2)+ "days with status =" + status);
			}
        }
	    if (getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2) == 0)
            logger.info("RBT::cleanViral  Cleaning period not set so returning "
							+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2));
	    else
	    {
    	    count += m_rbtDBManager.removeOldViralSMS("BASIC", getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_HRS", 1), true);
		    logger.info("RBT::cleaned Basic viral older than "
					+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_HRS", 1) + "hours");
	        count += m_rbtDBManager.removeOldViralSMS("WEB_REQUEST",
					getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 7));
			logger.info("RBT::cleaned WEB_REQUEST viral older than "
									+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 7) + "days");
	        count += m_rbtDBManager.removeOldViralSMS("CRICKET",
					getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2));
			logger.info("RBT::cleaned Cricket viral older than "
									+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2) + "days ");
			
			//removing GIFTCOPY_SUCCESS
			 count += m_rbtDBManager.removeOldViralSMS("GIFTCOPY_SUCCESS", getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2));
		     logger.info("RBT::cleaned GIFTCOPY_SUCCESS viral older than "
						+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2) + "days");
		     //removing GIFTCOPY_FAILED
			 count+= m_rbtDBManager.removeOldViralSMS("GIFTCOPY_FAILED",getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2));
			 logger.info("RBT::cleaned GIFTCOPY_FAILED viral older than "
										+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2) + "days");
			 // removing rdc selection requests
			 count += m_rbtDBManager.removeOldViralSMS("RDC_SEL_PROCESSED", getParamAsInt("RDC_SELECTIONS_CLEANING_PERIOD_DAYS", 2));
			 logger.info("RBT::cleaned RDC selection requests older than "
						+ getParamAsInt("RDC_SELECTIONS_CLEANING_PERIOD_DAYS", 2) + " days");
			 //removing SELCONFPENDING 
			 count += m_rbtDBManager.removeOldViralSMS("SELCONFPENDING", getParamAsInt("SELCONFPENDING_CLEANING_PERIOD_DAYS", 1));
			 logger.info("RBT::cleaned SELCONFPENDING requests older than "
						+ getParamAsInt("SELCONFPENDING_CLEANING_PERIOD_DAYS", 1) + " days");
	    }
	    if (getParamAsInt("OLDMGM_CLEANING_PERIOD_IN_DAYS", 2) == 0)
            logger.info("RBT::cleanMGM  Cleaning period not set so returning "
                                    + getParamAsInt("OLDMGM_CLEANING_PERIOD_IN_DAYS", 2));
	    else
	    {
	    	count = m_rbtDBManager.removeOldViralSMS("MGM", getParamAsInt("OLDMGM_CLEANING_PERIOD_IN_DAYS", 2));
	    	logger.info("RBT::cleaned MGM viral older than "
	    			+ getParamAsInt("OLDMGM_CLEANING_PERIOD_IN_DAYS", 2) + "days");
	    }
	    
	    ViralSMSTable[] viralSmsTables = m_rbtDBManager.getViralSMSByTypeAndLimitAndTime("DOWNLOAD_DC_CONFIRM_PENDING", RBTParametersUtils.getParamAsInt(
				"COMMON", "DOWNLOAD_DEACTIVATION_TIME", 60), count);
	    if (viralSmsTables != null) {
	    	for(ViralSMSTable viralSmsTable : viralSmsTables) {
	    		Subscriber subscriber =  m_rbtDBManager.getSubscriber(viralSmsTable.callerID());
	    		int retryCount = RBTParametersUtils.getParamAsInt("COMMON", "DOWNLOAD_DC_CONFIRM_PENDING_RETRY", 1);
	    		int triedCount = viralSmsTable.count();			
	    		if(subscriber == null || triedCount >= retryCount) {
	    			if(m_rbtDBManager.deleteViralPromotionBySMSID(viralSmsTable.getSmsId())) {
	    				count += 1;
	    			}
	    		}
	    		else {			
	    			triedCount++;
	    			boolean isUpdate = m_rbtDBManager.updateViralSMSTable(viralSmsTable.subID(), new Date(), viralSmsTable.type(), viralSmsTable.callerID(), 
	    					viralSmsTable.clipID(), triedCount, viralSmsTable.selectedBy(), null, viralSmsTable.extraInfo());
	    			if(isUpdate) {
	    				//Send SMS and update Viral
	    				String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(Constants.DOWNLOAD_DEACT_CONFIRM_PENDING_SUCCESS, subscriber.language());
	    				HashMap<String, String> hashMap = new HashMap<String, String>();
	    				hashMap.put("CALLER_ID", viralSmsTable.subID());
	    				hashMap.put("SMS_TEXT", smsText);					
	    				Clip clip =  RBTCacheManager.getInstance().getClipByPromoId(viralSmsTable.clipID());
	    				String songName = clip != null ? clip.getClipName() : "";
	    				Category category = null;
	    				if(clip == null) {
	    					category = RBTCacheManager.getInstance().getCategoryByPromoId(viralSmsTable.clipID());
	    					songName = category != null ? category.getCategoryName() : "";
	    				}

	    				hashMap.put("SONG_NAME", songName);
	    				hashMap.put("CIRCLE_ID", subscriber.circleID());
	    				hashMap.put("PROMO_ID", viralSmsTable.clipID());
	    				smsText = SmsProcessor.finalizeSmsText(hashMap);
	    				try {
	    					Tools.sendSMS(DBConfigTools.getParameter(SMS,
	    							ConstantsTools.SMS_NO_DCT_DOWNLOAD_CONFIRM, "123456"), subscriber.subID(),
	    							smsText, false);
	    				}
	    				catch(OnMobileException e){
	    					logger.error("Error while sending sms: ", e);
	    				}
	    			}
	    		}
	    	}
	    }
	    
		return count;
    }

    private int cleanViral()
    {
        int count = 0;
        if (getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2) == 0)
        {
            logger.info("RBT::cleanViral  Cleaning period not set so returning "
							+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2));
            return -1;
        }
        count += m_rbtDBManager.removeOldViralSMS("BASIC", (getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_HRS", 336)/24) + 1);
        logger.info("RBT::cleaned Basic viral older than "
				+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2) + "days");
        count += m_rbtDBManager.removeOldViralSMS("WEB_REQUEST",
				getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 7));
		logger.info("RBT::cleaned WEB_REQUEST viral older than "
								+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 7) + "days");
        count += m_rbtDBManager.removeOldViralSMS("CRICKET",
				getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2));
		logger.info("RBT::cleaned Cricket viral older than "
								+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2) + "days ");
		
		//removing GIFTCOPY_SUCCESS
		 count += m_rbtDBManager.removeOldViralSMS("GIFTCOPY_SUCCESS", getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2));
	     logger.info("RBT::cleaned GIFTCOPY_SUCCESS viral older than "
					+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2) + "days");
	     //removing GIFTCOPY_FAILED
		 count+= m_rbtDBManager.removeOldViralSMS("GIFTCOPY_FAILED",getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2));
		 logger.info("RBT::cleaned GIFTCOPY_FAILED viral older than "
									+ getParamAsInt("OLDVIRAL_CLEANING_PERIOD_IN_DAYS", 2) + "days");
		//rwemoving old scratch card
		 count+= m_rbtDBManager.removeOldScratchCard("1", getParamAsInt("OLDSCRATCHCARDS_CLEANING_PERIOD_DAYS",7));
		 logger.info("RBT::cleaned Scratch Cards older than "
					+ getParamAsInt("OLDSCRATCHCARDS_CLEANING_PERIOD_DAYS",7) + "days");
		 
		 // removing rdc selection requests
		 count += m_rbtDBManager.removeOldViralSMS("RDC_SEL_PROCESSED", getParamAsInt("RDC_SELECTIONS_CLEANING_PERIOD_DAYS", 2));
		 logger.info("RBT::cleaned RDC selection requests older than "
					+ getParamAsInt("RDC_SELECTIONS_CLEANING_PERIOD_DAYS", 2) + " days");
		 //removing SELCONFPENDING 
		 count += m_rbtDBManager.removeOldViralSMS("SELCONFPENDING", getParamAsInt("SELCONFPENDING_CLEANING_PERIOD_DAYS", 1));
		 logger.info("RBT::cleaned SELCONFPENDING requests older than "
					+ getParamAsInt("SELCONFPENDING_CLEANING_PERIOD_DAYS", 1) + " days");
		 
		 return count;
	}
    
    private int cleanTransData()
    {
    	int count = 0;
    	if (getParamAsInt("OLD_TRANS_DATA_CLEANING_PERIOD_IN_DAYS", 1) == 0)
    	{
    		logger.info("RBT::cleanTransData  Cleaning period not set so returning "
							+ getParamAsInt("OLD_TRANS_DATA_CLEANING_PERIOD_IN_DAYS", 1));
    		return -1;
    	}
    	count =  m_rbtDBManager.removeOldTransData(getParamAsInt("OLD_TRANS_DATA_CLEANING_PERIOD_IN_DAYS", 1));
    	logger.info("RBT::cleaned TRANS DATA viral older than "+ getParamAsInt("OLD_TRANS_DATA_CLEANING_PERIOD_IN_DAYS", 1)+ "days");
    	return count;
    }

    private int cleanMGM()
    {
        int count = 0;
        if (getParamAsInt("OLDMGM_CLEANING_PERIOD_IN_DAYS", 2) == 0)
        {
            logger.info("RBT::cleanMGM  Cleaning period not set so returning "
                                    + getParamAsInt("OLDMGM_CLEANING_PERIOD_IN_DAYS", 2));
            return -1;
        }
        count = m_rbtDBManager.removeOldViralSMS("MGM", getParamAsInt("OLDMGM_CLEANING_PERIOD_IN_DAYS", 2));
        logger.info("RBT::cleaned MGM viral older than "
				+ getParamAsInt("OLDMGM_CLEANING_PERIOD_IN_DAYS", 2) + "days");
        return count;
    }

    private int cleanSubscriberActivityCounts()
    {
    	int cleanUpDays = getParamAsInt("OLD_SUBSCRIBER_ACTIVITY_COUNTS_CLEANING_PERIOD_IN_DAYS", 1);
    	if (cleanUpDays == 0)
    	{
    		logger.info("RBT::cleanSubscriberActivityCounts Cleaning period not set so returning " + cleanUpDays);
    		return -1;
    	}

    	int count =  SubscriberActivityCountsDAO.deleletSubscriberActivityCountsForDays(cleanUpDays);
    	logger.info("RBT::cleaned SUBSCRIBER_ACTIVITY_COUNTS older than "+ cleanUpDays + "days");
    	return count;
    }

    public void deactivateSubscribers()
    {
        List<SubscriptionClass> subclass = CacheManagerUtil.getSubscriptionClassCacheManager().getAllSubscriptionClasses();
        if (subclass != null && subclass.size() > 0)
        {
            for (int y = 0; y < subclass.size(); y++)
            {
                if (subclass.get(y).getAutoDeactivationPeriod() > 0)
                {
                	Subscriber[] subscribers = getSubscribersForDeactivation(
							subclass.get(y).getAutoDeactivationPeriod(), subclass.get(y)
									.getSubscriptionClass());
                    if (subscribers != null)
                    {
                        for (int i = 0; i < subscribers.length; i++)
                        {
                        	if (getParamAsBoolean("GATHERER", "AUTO_DEACTIVATIONS_FOR_POSTPAID_ONLY", "FALSE") == true && subscribers[i].prepaidYes() == true)
                            {
                        		logger.info("RBT::subscriber "	+ subscribers[i].subID()+ " is prepaid "+ subscribers[i].prepaidYes()
														+ " AUTO_DEACTIVATIONS_FOR_POSTPAID_ONLY is "+ getParamAsBoolean("GATHERER", "AUTO_DEACTIVATIONS_FOR_POSTPAID_ONLY", "FALSE")
														+ " and so skipping auto deactivations");
                        				continue;
                            }

                        	deactivateSubscriber(subscribers[i].subID(), "AU", null, getParamAsBoolean("COMMON", "DEL_SELECTIONS", "FALSE"));
                            logger.info("RBT::subscriber "
													+ subscribers[i].subID()
													+ " successfully deactivated since the user has not accessed the service for the last "
													+ subclass.get(y)
															.getAutoDeactivationPeriod()
													+ "days");
							if (getParamAsString("AUTO_DEACTIVATION_SMS") != null  && getParamAsString("AUTO_DEACTIVATION_SMS").length() > 0)
                            {
                                try
                                {
                                    Tools.sendSMS(com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumber("GATHERER", subscribers[i].subID(), "SENDER_NO"), subscribers[i]
                                            .subID(), getParamAsString("AUTO_DEACTIVATION_SMS"), false);
                                    logger.info("RBT::  Sms Message after auto deactivation sent to subscriber "
                                                               + subscribers[i]
                                                                       .subID()
                                                               + " successfully since the user has not  accessed the service for the last "
                                                               + subclass.get(y)
                                                                       .getAutoDeactivationPeriod()
                                                               + "days");
                                }
                                catch (Exception e)
                                {
                                    logger.error("", e);
                                }

                            }
                        }
                    }

                    if (getParamAsInt("AUTO_DEACTIVATION_NOTIFY_BEFORE_DAYS", 2) != 0
                            && (subclass.get(y).getAutoDeactivationPeriod() - getParamAsInt("AUTO_DEACTIVATION_NOTIFY_BEFORE_DAYS", 2)) > 0)
                    {
                        Calendar calendar = Calendar.getInstance();
                        calendar
                                .add(
                                     Calendar.DAY_OF_YEAR,
                                     -(subclass.get(y).getAutoDeactivationPeriod() - getParamAsInt("AUTO_DEACTIVATION_NOTIFY_BEFORE_DAYS", 2)));
                        Date date1 = calendar.getTime();
                        calendar.add(Calendar.DAY_OF_YEAR, -7);
                        Date date2 = calendar.getTime();
                        subscribers = getSubscribersForDeactivationAlert(date1,date2,subclass.get(y).getSubscriptionClass());
                                                                         
                        if (subscribers != null)
                        {
                            try
                            {

                                for (int i = 0; i < subscribers.length; i++)
                                {
                                    if (getParamAsBoolean("GATHERER", "AUTO_DEACTIVATIONS_FOR_POSTPAID_ONLY", "FALSE") == true
                                            && subscribers[i].prepaidYes() == true)
                                    {
                                        logger.info("RBT::subscriber "
                                                                   + subscribers[i]
                                                                           .subID()
                                                                   + " is prepaid "
                                                                   + subscribers[i]
                                                                           .prepaidYes()
                                                                   + "so skipping auto deactivations alert");
                                        continue;
                                    }
                                    if (getParamAsString("GATHERER", "AUTO_DEACTIVATION_ALERT_MESSAGE", m_autodeact_alert_sms).length() > 0)
                                    {
                                        Tools
                                                .sendSMS(com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumber("GATHERER", subscribers[i].subID(), "SENDER_NO"),subscribers[i].subID(),Tools.findNReplace(getParamAsString("GATHERER", "AUTO_DEACTIVATION_ALERT_MESSAGE", m_autodeact_alert_sms), "%N",""
                                                                                       + getParamAsInt("AUTO_DEACTIVATION_NOTIFY_BEFORE_DAYS", 2)),false);
                                        logger.info("RBT::  Sms Alert for auto deactivation sent to subscriber "
                                                                   + subscribers[i]
                                                                           .subID()
                                                                   + " successfully since the user has not  accessed the service for the last "
                                                                   + (subclass.get(y).getAutoDeactivationPeriod() - getParamAsInt("AUTO_DEACTIVATION_NOTIFY_BEFORE_DAYS", 2))
                                                                   + "days");
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                logger.error("", e);

                            }
                        }
                    }
                }
            }
        }

        Subscriber[] freeSubs = getFreeActivatedSubscribers(getParamAsInt("FREE_ACT_CHARGE_ALERT_PERIOD", 2));

        if (freeSubs != null && freeSubs.length > 0)
        {
            for (int iSubs = 0; iSubs < freeSubs.length; iSubs++)
            {
                if (getParamAsString("GATHERER", "FREE_ACT_CHARGE_ALERT_SMS", m_freeAct_charge_alert_sms).length() > 0)
                {
                    try
                    {
                        Tools.sendSMS(com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumber("GATHERER", freeSubs[iSubs].subID(), "SENDER_NO"),
                                         freeSubs[iSubs].subID(),
                                         Tools
                                                 .findNReplace(
                                                		 getParamAsString("GATHERER", "FREE_ACT_CHARGE_ALERT_SMS", m_freeAct_charge_alert_sms),
                                                               "%N",
                                                               ""
                                                                       + (getParamAsInt("FREE_ACT_CHARGE_ALERT_PERIOD", 2))),
                                         toSQLDate(getSMSTime(), sqlTimeSpec,
                                                   sqlTimeFormat));
                        logger.info("RBT::  Sms Alert for free activation period ending sent to subscriber "
                                                + freeSubs[iSubs].subID()
                                                + " successfully ");

                        setActivationInfoSFA(freeSubs[iSubs].subID(), "S"
                                + freeSubs[iSubs].activationInfo());
                    }
                    catch (Exception e)
                    {
                        logger.error("", e);
                    }
                }
            }
        }
    }

    private Subscriber[] getSubscribersForDeactivation(int autoDeactPeriod,
            String subscriptionClass)
    {
    	return (m_rbtDBManager.getSubscribersForDeactivation(autoDeactPeriod,subscriptionClass));
    }

    private String deactivateSubscriber(String strSubID, String deactBy,
            Date endDate, boolean delSelections)
    {
    	return (m_rbtDBManager.deactivateSubscriber(strSubID, deactBy, endDate,
				delSelections, true, getParamAsBoolean("COMMON", "USE_SUBSCRIPTION_MANAGER", "FALSE"), true));
    }

    private Subscriber[] getSubscribersForDeactivationAlert(Date date1,
            Date date2, String subscriptionClass)
    {
    	return (m_rbtDBManager.getSubscribersForDeactivationAlert(date1, date2,subscriptionClass));
    }

    private String toSQLDate(java.util.Date date, String spec, DateFormat format)
    {
        return "TO_DATE('" + format.format(date) + "', '" + spec + "')";
    }

    private Subscriber[] getFreeActivatedSubscribers(int n)
    {
    	return (m_rbtDBManager.getFreeActivatedSubscribers(n));
    }

    private void setActivationInfoSFA(String strSubID, String actInfo)
    {
    	m_rbtDBManager.setActivationInfo(strSubID, actInfo);
    }

    private Date getSMSTime()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, getParamAsInt("SMS","NUM_CONN", 4));
        return (cal.getTime());
    }

   
    
    public String getParamAsString(String param)
	   {
	    	try{
	    		return rbtGenericCache.getParameter(PARAMETER_TYPE, param, null);
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param );
	    		return null;
	    	}
	    }
	    
	  public String getParamAsString(String type, String param, String defualtVal)
	    {
	    	try{
	    		return rbtGenericCache.getParameter(type, param, defualtVal);
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defualtVal;
	    	}
	    }
	    
	    public int getParamAsInt(String param, int defaultVal)
	    {
	    	try{
	    		String paramVal = rbtGenericCache.getParameter(PARAMETER_TYPE, param, defaultVal+"");
	    		return Integer.valueOf(paramVal);   		
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param );
	    		return defaultVal;
	    	}
	    }
	    
	    public int getParamAsInt(String type, String param, int defaultVal)
	    {
	    	try{
	    		String paramVal = rbtGenericCache.getParameter(type, param, defaultVal+"");
	    		return Integer.valueOf(paramVal);   		
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defaultVal;
	    	}
	    }
	    
	    public boolean getParamAsBoolean(String param, String defaultVal)
	    {
	    	try{
	    		return rbtGenericCache.getParameter(PARAMETER_TYPE, param, defaultVal).equalsIgnoreCase("TRUE");
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param );
	    		return defaultVal.equalsIgnoreCase("TRUE");
	    	}
	    }
	    public boolean getParamAsBoolean(String type, String param, String defaultVal)
	    {
	    	try{
	    		return rbtGenericCache.getParameter(type, param, defaultVal).equalsIgnoreCase("TRUE");
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defaultVal.equalsIgnoreCase("TRUE");
	    	}
	    }
	    private int cleanPickOfTheDay()
	    {
	    	int cleanUpDays = getParamAsInt("OLD_PICK_OF_THE_DAY_CLEANING_PERIOD_IN_DAYS", 7);
	    	if (cleanUpDays == 0)
	    	{
	    		logger.info("RBT::cleanPickOfTheDay Cleaning period not set so returning " + cleanUpDays);
	    		return -1;
	    	}

	    	int count = m_rbtDBManager.deleteOldPickOfTheDayEntries(cleanUpDays);
	    	logger.info("RBT::cleaned PICK_OF_THE_DAY entries older than "+ cleanUpDays + "days ");
	    	return count;
	    }

	    private int cleanConsentRecords(){
	     	int noOfRecordSDeleted = RBTConsentCleanUp.processConsentCleanUp();
	     	return noOfRecordSDeleted;
	    }
}