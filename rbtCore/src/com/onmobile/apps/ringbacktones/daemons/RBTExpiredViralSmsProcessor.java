package com.onmobile.apps.ringbacktones.daemons;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import net.sf.ehcache.CacheManager;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;

public class RBTExpiredViralSmsProcessor{

    private static long intervalPeriod = -1;
    private static int timeTodeleteBeforeCurrentTime = -1;
    private static Logger sms_logger = Logger.getLogger(RBTExpiredViralSmsProcessor.class.getName()+".SMS_EXPIRED");
    private static Logger logger = Logger.getLogger(RBTExpiredViralSmsProcessor.class);
    private RBTDaemonManager m_mainDaemonThread = null;
    ScheduledExecutorService newScheduledThreadPool = null;
    
    static{
    	intervalPeriod = RBTParametersUtils.getParamAsLong("DAEMON", "INTERVAL_AT_WHICH_TO_PROCESS_EXPIRED_VIRAL_SMS", 30);
    	timeTodeleteBeforeCurrentTime=  RBTParametersUtils.getParamAsInt("DAEMON", "TIME_BEFORE_CURRENT_TIME_IN_MIN_TO_DELETE", 180);
    }
    
    public RBTExpiredViralSmsProcessor(RBTDaemonManager m_mainDaemonThread){
    	this.m_mainDaemonThread = m_mainDaemonThread;
    }
    
	public void processExpiredViralSmsRecords(){
		newScheduledThreadPool = Executors.newScheduledThreadPool(1);
		newScheduledThreadPool.scheduleAtFixedRate(new ViralSMSConfirmationRecords(), 0, intervalPeriod, TimeUnit.MINUTES);
		logger.info("Started RBTViralExpiredSms thread ");
	}
	
	
	public class ViralSMSConfirmationRecords implements Runnable {

		@Override
		public void run() {
			if(m_mainDaemonThread!=null && m_mainDaemonThread.isAlive()){
 			 RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			 ViralSMSTable[] viralSMSRecordsByTypeAndTime = rbtDBManager
					.getViralSMSByTypeAndTime("SMSCONFPENDING", timeTodeleteBeforeCurrentTime);
			 if (viralSMSRecordsByTypeAndTime != null) {
				logger.info("Number of Expired Records Fetched = " + viralSMSRecordsByTypeAndTime.length);
				for (ViralSMSTable viralSmsTable : viralSMSRecordsByTypeAndTime) {
					try {
						String subscriberID = viralSmsTable.subID();
						Date sentTime = viralSmsTable.sentTime();
						String type = viralSmsTable.type();
						String callerID = viralSmsTable.callerID();
						String clipID = viralSmsTable.clipID();
						String selectedBy = viralSmsTable.selectedBy();
						Date setTime = viralSmsTable.setTime();
						String extraInfo = viralSmsTable.extraInfo();
						String circleID = viralSmsTable.getCircleId();
						long smsID = viralSmsTable.getSmsId();
						sendSms(clipID, callerID, subscriberID);
						boolean success = RBTDBManager.getInstance().deleteViralPromotionBySMSID(
								smsID);
						if (success) {
							StringBuilder strBuilder = new StringBuilder();
							strBuilder.append(subscriberID + ",").append(sentTime + ",");
							strBuilder.append(type + ",").append(callerID + ",");
							strBuilder.append(clipID + ",").append(selectedBy + ",");
							strBuilder.append(setTime + ",").append(extraInfo + ",");
							strBuilder.append(circleID + ",").append(smsID);
							sms_logger.info(strBuilder.toString());
						}
						logger.info("Result of Deleting Viral SMS Table Record for SMS_ID = "
								+ smsID + " = " + success);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			  }else{
				  logger.info("No Records Found .So sleeping for "+intervalPeriod + "Minutes");
			  }
		   }
		}


		private void sendSms(String clipId, String callerId, String subscriberID) {
			String msg = null;
			com.onmobile.apps.ringbacktones.content.Subscriber subscriber = RBTDBManager
					.getInstance().getSubscriber(subscriberID);
			
			if (clipId != null) {
				if (subscriber == null) {
					msg = CacheManagerUtil.getSmsTextCacheManager().getSmsText("CONFIRMATION",
							"COMBO_EXPIRED_CONFIRMATION_MASSAGE", null);
				} else {
					msg = CacheManagerUtil.getSmsTextCacheManager().getSmsText("CONFIRMATION",
							"SELECTION_EXPIRED_CONFIRMATION_MASSAGE", null);
				}
				Clip clip = RBTCacheManager.getInstance().getClip(clipId);
				String artist ="";
				String album = "";
				String songName ="";
				if(clip!=null){
					if(clip.getArtist()!=null)
					    artist =  clip.getArtist();
					if( clip.getAlbum()!=null)
					    album =  clip.getAlbum();
					if(clip.getClipName()!=null)
					    songName =  clip.getClipName();
				}
				if(callerId == null)
					callerId="";
				if (msg != null) {
					msg = msg.replaceAll("%artist%", artist);
					msg = msg.replaceAll("%album%", album);
					msg = msg.replaceAll("%songname%", songName);
					msg = msg.replaceAll("%callerId%", callerId);
				}
			} else {
				    msg = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
						"CONFIRMATION", "ACTIVATION_EXPIRED_CONFIRMATION_MASSAGE", null);
			}
			String senderNo = RBTParametersUtils.getParamAsString("SMS", "SMS_NO", "SMS_NO");
			UtilsRequest utilsRequest = new UtilsRequest(senderNo, subscriberID, msg);
			RBTClient.getInstance().sendSMS(utilsRequest);
		}

	}
}