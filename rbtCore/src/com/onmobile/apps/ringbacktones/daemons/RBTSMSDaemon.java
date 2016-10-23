/**
 *  
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.common.exception.OnMobileException;

/*
 * import com.onmobile.common.cjni.IJavaComponent; import
 * com.onmobile.common.cjni.O3InterfaceHelper; import
 * com.onmobile.common.message.O3Message; import org.w3c.dom.Node;
 */

/**
 * @author vinayasimha.patil
 * @modified Sreekar
 * @modified abhinav.anand@onmobile.com
 */
public class RBTSMSDaemon extends Thread
{
	private static Logger logger = Logger.getLogger(RBTSMSDaemon.class);
	
	private boolean m_Continue = true;
	private static final String _class = "SMSDaemon";
	private static final String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "Mei","Jun","Jul","Agt", "Sep", "Okt", "Nov", "Des" };

	private RBTDBManager rbtDBManager = null;
	private RBTConnector rbtConnector=null;
	
	private HashMap _subClassActSubs = null;
	
	protected static final String sqlTimeSpec = "YYYY/MM/DD HH24:MI:SS";
	protected static final DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	int tnbDeactivationDay = 30;

	private static WriteDailyTrans m_callBackTrans = null;
	String m_sdrWorkingDir = ".";
	
	Date blackOutStartDate = null;
	Date blackOutEndDate = null;
	
	public RBTSMSDaemon() {
		init();
	}
	public void init()
	{
		Tools.init("SMS_DAEMON", true);
		logger.info("RBT::SMS Daemon started");

		rbtDBManager = RBTDBManager.getInstance();
		rbtConnector=RBTConnector.getInstance();
		
		m_sdrWorkingDir = getParamAsString("DAEMON", "SDR_WORKING_DIR", ".");
		
		ArrayList<String> headers = new ArrayList<String> ();
    	headers.add("TYPE");
    	headers.add("REQUEST");
    	headers.add("RESPONSE");
    	headers.add("TIME DELAY");

    	m_callBackTrans = new WriteDailyTrans(m_sdrWorkingDir, "SMS_DAEMON", headers);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	 public void run() 
	 {
		 Calendar curCal = null;
		 while (m_Continue) 
		 {
			 try
			 {
				 List<String> m_daemonMode = Arrays.asList(getParamAsString("SMS", "SMS_DAEMON_MODE", "SUB").split(","));
					 
				 curCal = Calendar.getInstance();
				 StringTokenizer stTimeTokens = new StringTokenizer(getParamAsString("SMS", "BLACKOUT_START_TIME", null), ":");
				 curCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(stTimeTokens.nextToken()));
				 curCal.set(Calendar.MINUTE, Integer.parseInt(stTimeTokens.nextToken()));
				 curCal.set(Calendar.SECOND, Integer.parseInt(stTimeTokens.nextToken()));

				 blackOutStartDate = curCal.getTime();

				 StringTokenizer enTimeTokens = new StringTokenizer(getParamAsString("SMS", "BLACKOUT_END_TIME", null), ":");
				 curCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(enTimeTokens.nextToken()));
				 curCal.set(Calendar.MINUTE, Integer.parseInt(enTimeTokens.nextToken()));
				 curCal.set(Calendar.SECOND, Integer.parseInt(enTimeTokens.nextToken()));

				 blackOutEndDate = curCal.getTime();
				 if (blackOutEndDate.getTime() < blackOutStartDate.getTime()) 
				 {
					 curCal.add(Calendar.DAY_OF_YEAR, 1);
					 blackOutEndDate = curCal.getTime();
				 }
				int[] smsDays = null;
				String smsDaysString = getParamAsString("SMS", "SMS_DAYS", null);
				if (smsDaysString != null)
				{
						StringTokenizer daysToken = new StringTokenizer(smsDaysString, ",");
						smsDays = new int[daysToken.countTokens()];
						for (int i = 0; daysToken.hasMoreTokens(); i++)
						{
							smsDays[i] = Integer.parseInt(daysToken.nextToken());
						}
				}

				 if(m_daemonMode.contains("SUB")) 
				 {
					 processTNBTaskMode();
				 }
				 if(m_daemonMode.contains("SEL")) 
				 {
					 Hashtable<String, Hashtable<String, String>> smsDaysList = new Hashtable<String, Hashtable<String, String>>();
					 Hashtable<String, ChargeClass> trialsTable = new Hashtable<String, ChargeClass>();
					 
					 ArrayList<ChargeClass> trialChargeClasses = (ArrayList<ChargeClass>) rbtConnector.getRbtGenericCache().getTrialChargeClassList();
					
					 if (trialChargeClasses != null && trialChargeClasses.size() > 0) 
					 {
						 for (int i = 0; i < trialChargeClasses.size(); i++) 
						 {
							 Hashtable<String, String> smsDaysTable = new Hashtable<String, String>();
							 trialsTable.put(trialChargeClasses.get(i).getChargeClass(), trialChargeClasses.get(i));
							
							 List<BulkPromoSMS> bulkPromoSMSes = CacheManagerUtil.getBulkPromoSMSCacheManager().getAllPromoIDSMSes(trialChargeClasses.get(i).getChargeClass());
							 
							 for (BulkPromoSMS bulkPromoSMS : bulkPromoSMSes) {
								 smsDaysTable.put(bulkPromoSMS.getSmsDate(), bulkPromoSMS.getSmsText());
							  }
							 smsDaysList.put(trialChargeClasses.get(i).getChargeClass(), smsDaysTable);
						 }
						 
						 for (int t = 0; t < trialChargeClasses.size(); t++) 
						 {
							 logger.info("RBT::Sending SMSes for Trial : "+ trialChargeClasses.get(t).getChargeClass());
							 if(!smsDaysList.containsKey(trialChargeClasses.get(t).getChargeClass())) 
							 {
								 logger.info("No Bulk PromoSMS for Trial : "+ trialChargeClasses.get(t).getChargeClass());							
								 continue;
							 }
							 ArrayList freeActiveSubscribersStatus = new ArrayList();
							 Hashtable<String, String> smsDaysMap = (Hashtable<String, String>) smsDaysList.get(trialChargeClasses.get(t).getChargeClass());
							 Enumeration<String> smsDaysEnum = smsDaysMap.keys();

							 while (smsDaysEnum.hasMoreElements())
							 {
								 int smsDay = Integer.parseInt((String) smsDaysEnum.nextElement());

								 String selectionPeriod = trialChargeClasses.get(t).getSelectionPeriod();
								 selectionPeriod = selectionPeriod.trim();

								 int freeActivationPeriod = Integer.parseInt(selectionPeriod.substring(1));
								 SubscriberStatus[] freeActiveSubsStatus = rbtDBManager.getFreeActiveStatusSubscribers((freeActivationPeriod - smsDay), smsDay, trialChargeClasses.get(t).getChargeClass(), getParamAsInt("NO_OF_SMS_PER_LOOP",1000));
								 freeActiveSubscribersStatus.add(freeActiveSubsStatus);
							 }

							 smsDaysEnum = smsDaysMap.keys();
							 if((freeActiveSubscribersStatus != null) && (freeActiveSubscribersStatus.size() > 0)) 
							 {
								 for (int i = 0; smsDaysEnum.hasMoreElements(); i++) 
								 {
									 int smsDay = Integer.parseInt((String) smsDaysEnum.nextElement());
									 if (smsDay != 1) 
									 {
										 long curTime = System.currentTimeMillis();
										 if ((curTime >= blackOutStartDate.getTime()) && (curTime <= blackOutEndDate.getTime())) 
										 {
											 long blackOutPeriod = blackOutEndDate.getTime()- curTime;
											 logger.info("RBT::Black Out Time. Sleeping for " + blackOutPeriod + " milli seconds.");

											 try 
											 {
												 Thread.sleep(blackOutPeriod);
											 }
											 catch (InterruptedException ie) 
											 {
												 logger.info("RBT::Interrupted while in blackout period");
												 return;
											 }
											 logger.info("RBT::Black Out period is over.");
											 break;
										 }
									 }

									 SubscriberStatus[] freeActiveSubStatus = (SubscriberStatus[]) freeActiveSubscribersStatus.get(i);
									 if((freeActiveSubStatus != null) && (freeActiveSubStatus.length > 0)) 
									 {
										 logger.info("Trial class >" +trialChargeClasses.get(t).getChargeClass() +" & sms day >"+smsDay +" & No of subscribers >"+freeActiveSubStatus.length);
										 for (int turnCount = 0; turnCount < freeActiveSubStatus.length; turnCount++) 
										 {
											 String subscriberID = null;
											 boolean smsSent = false;
											 try
											 {
												 SubscriberStatus subscriberStatus = freeActiveSubStatus[turnCount];
												 subscriberID = subscriberStatus.subID();
												 Subscriber subscriber = rbtDBManager.getSubscriber(subscriberStatus.subID());
	
												 String selectionInfo = "FA:" + smsDay + ":";
												 String subSelectionInfo = subscriberStatus.selectionInfo();
												 boolean sendSMS = false;
	
												 logger.info("RBT::SelectionInfo:" + subSelectionInfo + ":");
												 if(rbtDBManager.smsSentForTrialSubscriber(subscriberID, subscriber.startDate(), smsDay)) 
												 {
													 logger.info("RBT::SMS already sent for subscriber : " + subscriberID);
													 sendSMS = false;
													 selectionInfo += subSelectionInfo;
													 rbtDBManager.setSelectionInfo(subscriberID,selectionInfo);
												 }
												 else 
												 {
													 sendSMS = true;
													 selectionInfo += subSelectionInfo;
												 }
												 //Date activationDate = subscriber.activationDate();
												 Date nextChargingDate = freeActiveSubStatus[turnCount].nextChargingDate(); 
												 String classTypeTrial = freeActiveSubStatus[turnCount].classType(); 
												 ChargeClass ccTrial = (ChargeClass) trialsTable.get(classTypeTrial);
												 if(nextChargingDate == null || ccTrial == null || ccTrial.getSelectionPeriod() == null) 
												 { 
													 sendSMS = false;
													 logger.info("RBT::nextChargingDate for trial subscriber seelction : " + subscriberID
															 + " is NULL or some other condition is wrong: Not sending SMS");
												 }
												 int selectionPeriodTrial = Integer.parseInt(ccTrial.getSelectionPeriod().trim().substring(1));
	
												 if (sendSMS) 
												 {
													 //String smsText = (String) smsDaysMap.get(String.valueOf(smsDay));
	                                                   String smsText = CacheManagerUtil
															.getSmsTextCacheManager()
															.getSmsText(trialChargeClasses.get(t).getChargeClass(),String.valueOf(smsDay),subscriber.language());
													 int dateIndex;
													 while ((dateIndex = smsText.indexOf("%date")) >= 0) 
													 {
														 int day = Integer.parseInt(smsText.substring(dateIndex + 5, smsText.indexOf('%',dateIndex + 5)));
														 Calendar cal = Calendar.getInstance();
														 cal.setTime(nextChargingDate); 
														 cal.add(Calendar.DAY_OF_YEAR, (1-selectionPeriodTrial)); 
														 cal.add(Calendar.DAY_OF_YEAR, (day - 1));
														 String date = cal.get(Calendar.DATE)
														 + MONTHS[cal.get(Calendar.MONTH)];
														 String tmpSMSText = smsText.substring(0, dateIndex);
														 tmpSMSText += date;
														 tmpSMSText += smsText.substring(smsText.indexOf('%', dateIndex + 5) + 1);
	
														 smsText = tmpSMSText;
													 }
													 if(smsText.indexOf("%PROMO") != -1 )
													 {
														 if(subscriberStatus != null && subscriberStatus.subscriberFile() !=  null)
														 {
															 Clip cm = getClipRBT(subscriberStatus.subscriberFile());
															 if(cm != null && cm.getClipPromoId() != null)
															 {
																 String tempSMSText = smsText.substring(0, smsText.indexOf("%PROMO"));
																 tempSMSText = tempSMSText + " " + cm.getClipPromoId();
																 tempSMSText = tempSMSText + " " +  smsText.substring(smsText.indexOf("%PROMO")+6);
																 smsText =  tempSMSText;
															 }
														 }	
													 }
	
													 logger.info("RBT::SMS Text " + smsText);
	
													 smsSent = Tools.sendSMS(getParamAsString("SMS","SMS_NO","123456"), subscriberID, smsText, false);
													 if (smsSent) 
													 {
														 rbtDBManager.setSelectionInfo(subscriberID, selectionInfo);
														 logger.info("RBT::SMS for " + subscriber.subID() + " for day " + smsDay
																 + " sent successfully");
													 }
													 else 
													 {
														 logger.info("RBT::SMS for " + subscriber.subID() + " for day " + smsDay + " not sent");
													 }
												 }
											 }
											 catch(Throwable e)
											 {
												 logger.info("RBT::Exception occured for subscriber "+subscriberID+" ==> " + getStackTrace(e));
											 }
											 finally
											 {
												 
												 writeTrans("SMS_DAEMON", subscriberID+":"+trialChargeClasses.get(t).getChargeClass()+":"+smsDay , smsSent+"", new Date()+"");
											 }
										 }
									 }
									 else {
										 logger.info("RBT:: No Free Active Subscriber Found for Day "+ smsDay);
									 }
								 }
							 }
							 else 
							 {
								 logger.info("RBT:: No Free Active Subscriber Found.");
							 }
							
							 SubscriptionClass subscriberClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass("DEFAULT");
							 Subscriber[] trailSubsToBeDeactivated = rbtDBManager.getActiveTNBSubsToDeactivate("DEFAULT", subscriberClass.getSubscriptionPeriodInDays());   

							 if(trailSubsToBeDeactivated != null && trailSubsToBeDeactivated.length > 0) 
							 {
								 for (int i = 0; i < trailSubsToBeDeactivated.length; i++) 
								 {
									 SubscriberStatus[] subSel = rbtDBManager.getNonFreeSelections(trailSubsToBeDeactivated[i].subID(), "TRIAL");
									 if (subSel != null && subSel.length > 0) 
									 {
										 Calendar cal = Calendar.getInstance();
										 cal.setTime(trailSubsToBeDeactivated[i].endDate());
										 if (cal.get(Calendar.YEAR) < 2030)
											 updateTNBSubscribertoNormal(trailSubsToBeDeactivated[i], subscriberClass);
									 }
									 else 
									 {
										 StringBuffer strBuff=new StringBuffer();
										 String deactivatedBy = getParamAsString("DEAMON", "DEACT_MODE_OPT_IN", "SMSDaemon");
										 boolean deact = rbtConnector.getSubscriberRbtclient().unsubscribe(trailSubsToBeDeactivated[i].subID(),true, deactivatedBy,"SMSDaemon", strBuff);
										 if (deact)
											 logger.info("RBT::initial deactivation successful for "+ trailSubsToBeDeactivated[i]);
									 }
								 }
							 }
							 else 
							 {
								 logger.info("RBT::No trail"+ " Subscribers to deactivate");
							 }
						 }
					 }
					 else 
					 {
						 logger.info("RBT:: No TRIAL offer found.");
					 }
				 }
				 //Added by Sreekar
				 
				 if(m_daemonMode.contains("SUB_CLASS")) 
				 {
					 String subClassesStr = getParamAsString("SMS", "SUB_CLASSES_TO_SEND_SMS", null);
					 HashMap _subClassSMSDays = new HashMap();
					 ArrayList<String> subClassesList = new ArrayList<String>();
					 if(subClassesStr != null) 
					 {
						 StringTokenizer subClassStk = new StringTokenizer(subClassesStr, ",");
						 while (subClassStk.hasMoreTokens()) 
						 {
							 String subClass = subClassStk.nextToken();
							 subClassesList.add(subClass);
							 int[] thisSubClassSMSDays = Tools.getIntArrayFromStr(getParamAsString("SMS", subClass + "_SMS_DAYS", null));
							 _subClassSMSDays.put(subClass, thisSubClassSMSDays);
						 }
					//	 _subClassesToSendSMS = (String[])subClassesList.toArray(new String[0]);
					 }

					 _subClassActSubs = new HashMap();

					 for (int i = 0; i < subClassesList.size(); i++) 
					 {
						 long curTime = System.currentTimeMillis();
						 if((curTime >= blackOutStartDate.getTime()) && (curTime <= blackOutEndDate.getTime())) 
						 {
							 logger.info("RBT::Black Out Time for day" + " not sending any sub_class promotion SMS for this loop");
							 break;
						 }
						 Object thiSMSDaysObj = _subClassSMSDays.get(subClassesList.get(i));
						 if(thiSMSDaysObj == null)
						 {
							 logger.info("RBT::No SMS configured for sub class " + subClassesList.get(i));
							 break;
						 }
						 else 
						 {
							 int[] smsDaysArray = (int[]) thiSMSDaysObj;
							 for (int j = 0; j < smsDays.length; j++) 
							 {
								 Subscriber[] subList = rbtDBManager.getSubsTosendSMS(subClassesList.get(i), smsDaysArray[j], getParamAsInt("NO_OF_SMS_PER_LOOP",1000));
								 if(subList != null)
									 _subClassActSubs.put(subClassesList.get(i) + ":" + smsDaysArray[j], subList);
							 }
						 }
					 }// end of for all sub classes
				 }// end of if "SUB_CLASS" mode
							

				 //subscription class loop, Added by Sreekar
				 if(_subClassActSubs != null && !_subClassActSubs.isEmpty()) 
				 {
					 Iterator itr = _subClassActSubs.keySet().iterator();
					 while(itr.hasNext()) 
					 {
						 String key = (String)itr.next();
						 String subClass = key.substring(0, key.indexOf(":"));
						 int smsDay = Integer.parseInt(key.substring(key.indexOf(":") + 1));
						
						 //BulkPromoSMS sms = CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMSForDate(subClass, String.valueOf(smsDay));
						 Subscriber[] subs = (Subscriber[])_subClassActSubs.get(key);
							 if(subs!=null){
								 for (int i = 0; i < subs.length; i++) 
								 {
									 String message=null;
									 message=CacheManagerUtil.getSmsTextCacheManager().getSmsText(subClass, String.valueOf(smsDay), subs[i].language());
									 if(message!=null){
										
									 }else{
										 logger.info("RBT:: sms for " + subClass + " for day " + String.valueOf(smsDay)+ " is null, continuing with the next day if any");
										 continue;
									 }
									 Tools.sendSMS(getParamAsString("SMS","SMS_NO","123456"), subs[i].subID(), message,false);
									 String actInfo = subs[i].activationInfo();
									 int index = subs[i].activationInfo().indexOf("/");
									 if(index >= 0)
										 actInfo = subClass + ":" + smsDay + actInfo.substring(index);
									 else
										 actInfo = subClass + ":" + smsDay + "/" + actInfo;
									 rbtDBManager.setActivationInfo(subs[i].subID(), actInfo);
								 }
							 }
					 }
				 }
				 else
					 logger.info("RBT::No sub class SMS to send");

				 logger.info("RBT::SMS Daemon sleeping for " + getParamAsInt("SMS_DAEMON_SLEEP_TIME", 5*60*60) + " milli seconds...");
				 Thread.sleep(getParamAsInt("SMS_DAEMON_SLEEP_TIME", 5*60*60));
			 }
			 catch (InterruptedException ie) {
				 return;
			 }
			 catch (Exception e) {
				 logger.error("", e);
			 }
		 }
	 }

	
	public void processTNBTaskMode()
	{
		String tnbSubClasses = getParamAsString("COMMON", "TNB_SUBSCRIPTION_CLASSES", "ZERO");
		List<String> tnbSubClassList = Arrays.asList(tnbSubClasses.split(","));

		for (String tnbSubClass : tnbSubClassList) 
		{
			int[] smsDays = null;
			String smsDaysString = null; 
			smsDaysString = getParamAsString("SMS", "SMS_DAYS_"+tnbSubClass, null);
			if (smsDaysString == null)
				smsDaysString = getParamAsString("SMS", "SMS_DAYS", null);
			if (smsDaysString != null)
			{
				String[] smsDaysArray = smsDaysString.split(",");
				smsDays = new int[smsDaysArray.length];
				for (int i = 0; i<smsDaysArray.length; i++)
				{
					try {
						smsDays[i] = Integer.parseInt(smsDaysArray[i]);
					} catch(NumberFormatException nfe) {
						//SMS will never be sent if this happens
						smsDays[i] = 0;
						//log this case
					}
				}
			}

			SubscriptionClass subscriptionClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(tnbSubClass);
			if (subscriptionClass == null)
			{
				logger.info("RBT::SubscriptionClass is not configured for tnbSubClass= " + tnbSubClass);
				continue;
			}

			int subscriptionPeriod = subscriptionClass.getSubscriptionPeriodInDays();
			for (int i = 0; i < smsDays.length; i++) 
			{
				Calendar cal1 = Calendar.getInstance();
				Calendar cal2 = Calendar.getInstance();

				cal1.add(Calendar.DATE, (subscriptionPeriod - smsDays[i] - 1));
				cal2.add(Calendar.DATE, (subscriptionPeriod - smsDays[i]));

				cal1.set(Calendar.HOUR_OF_DAY, 0);
				cal1.set(Calendar.MINUTE, 0);
				cal1.set(Calendar.SECOND, 0);

				cal2.set(Calendar.HOUR_OF_DAY, 0);
				cal2.set(Calendar.MINUTE, 0);
				cal2.set(Calendar.SECOND, 0);

				logger.info("RBT::cal1 = " + cal1.getTime());
				logger.info("RBT::cal2 = " + cal2.getTime());

				long curTime = System.currentTimeMillis();
				if((curTime >= blackOutStartDate.getTime()) && (curTime <= blackOutEndDate.getTime())) 
				{
					logger.info("RBT::Black Out Time for day not sending any tnb promotion SMS for this loop");
					break;
				}

				Subscriber[] subList = rbtDBManager.getActiveSubsToSendTNBSms(tnbSubClass, cal1.getTime(), cal2.getTime(), smsDays[i], subscriptionPeriod);
				if (subList == null || subList.length == 0)
				{
					logger.info("RBT::no subs to send on day " + smsDays[i]);
					continue;
				}

				int noOfSmsPerLoop = getParamAsInt("NO_OF_SMS_PER_LOOP",1000);
				for (int counter = 0, smsCounter = 0; (counter < subList.length && smsCounter < noOfSmsPerLoop); counter++) 
				{
					boolean isNonFreeSelectionExists = false;
					if(getParamAsBoolean("SMS", "UPDATE_TNB_TO_NORMAL_ON_DEFAULT_SELECTION", "FALSE")) 
					{	
						if (getParamAsBoolean("COMMON","ADD_TO_DOWNLOADS","FALSE"))
						{
							SubscriberDownloads[] subDownloads = rbtDBManager.getNonFreeDownloads(subList[counter].subID(), "FREE");
							if (subDownloads != null && subDownloads.length > 0)
								isNonFreeSelectionExists = true;
						}
						else
						{
							SubscriberStatus[] subSel = rbtDBManager.getNonFreeSelections(subList[counter].subID(), "FREE");
							if (subSel != null && subSel.length > 0)
								isNonFreeSelectionExists = true;
						}
					}
					if (isNonFreeSelectionExists)
					{
						Calendar cal = Calendar.getInstance();
						cal.setTime(subList[counter].endDate());
						if (cal.get(Calendar.YEAR) < 2030)
						{
							if (updateTNBSubscribertoNormal(subList[counter], subscriptionClass))
							{
								// On updating to normal, inform the user and send unsubscription details also.
								String unsubscriptionMsg = CacheManagerUtil.getSmsTextCacheManager().getSmsText("SMS", "UNSUBSCRIPTION_MSG", subList[counter].language());
								if (unsubscriptionMsg != null) 
								{
									try {
										Tools.sendSMS(getParamAsString("SMS","SMS_NO","123456"), subList[counter].subID(), unsubscriptionMsg, false);
									} catch (OnMobileException e) {	}
								}
							}
						}
						else
							logger.info("RBT::User already active not updating database");
					}
					else if(!subList[counter].activationInfo().startsWith("TNB:" + smsDays[i])) 
					{
						String smsText=null;
						smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(tnbSubClass + "_SMS", String.valueOf(smsDays[i]), subList[counter].language());
						if (smsText == null)
						{
							//get the sms text from the old configuration parameter
							smsText=CacheManagerUtil.getSmsTextCacheManager().getSmsText(getParamAsString("SMS", "BULK_PROMOID", null), String.valueOf(smsDays[i]), subList[counter].language());
							if (smsText == null)
							{
								logger.info("RBT:: sms for " + getParamAsString("SMS", "BULK_PROMOID", null) + " for day " + smsDays[i]+ " is null, continuing with the next day if any");
								continue;
							}
						}
						try
						{
							if (smsText.contains("%date"))
							{
								String date = "";
								Calendar calendar = Calendar.getInstance();
								calendar.setTime(subList[counter].endDate());
								date = calendar.get(Calendar.DAY_OF_MONTH)+"/"+(calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.YEAR);
								
								smsText = smsText.replace("%date", date);
							}

							Tools.sendSMS(getParamAsString("SMS", "SMS_NO", "123456"), subList[counter].subID(), smsText, false);

							String actInfo = subList[counter].activationInfo();
							int index = subList[counter].activationInfo().indexOf("/");
							if (index >= 0)
								actInfo = "TNB:" + smsDays[i] + actInfo.substring(index);
							else
								actInfo = "TNB:" + smsDays[i] + "/" + actInfo;
							rbtDBManager.setActivationInfo(subList[counter].subID(),actInfo);
							smsCounter++;
						}
						catch (OnMobileException e) 
						{
							logger.error("", e);
						}
					}
				}
			}
			// TNB Deactivation
			Subscriber[] tnbSubsToBeDeactivated = rbtDBManager.getActiveTNBSubsToDeactivate(tnbSubClass, subscriptionPeriod);
			if (tnbSubsToBeDeactivated == null || tnbSubsToBeDeactivated.length == 0)
			{
				logger.info("RBT::No TNB activated subscribers to deactivate");
				continue;
			}

			for (int i = 0; i < tnbSubsToBeDeactivated.length; i++) 
			{
				boolean isNonFreeSelectionExists = false;
				if(getParamAsBoolean("SMS", "UPDATE_TNB_TO_NORMAL_ON_DEFAULT_SELECTION", "FALSE")) 
				{	
					if (getParamAsBoolean("COMMON","ADD_TO_DOWNLOADS","FALSE"))
					{
						SubscriberDownloads[] subDownloads = rbtDBManager.getNonFreeDownloads(tnbSubsToBeDeactivated[i].subID(), "FREE");
						if (subDownloads != null && subDownloads.length > 0)
							isNonFreeSelectionExists = true;
					}
					else
					{
						SubscriberStatus[] subSel = rbtDBManager.getNonFreeSelections(tnbSubsToBeDeactivated[i].subID(), "FREE");
						if (subSel != null && subSel.length > 0)
							isNonFreeSelectionExists = true;
					}
				}
				if (isNonFreeSelectionExists) 
				{
					Calendar cal = Calendar.getInstance();
					cal.setTime(tnbSubsToBeDeactivated[i].endDate());
					if (cal.get(Calendar.YEAR) < 2030)
					{
						if (updateTNBSubscribertoNormal(tnbSubsToBeDeactivated[i], subscriptionClass))
						{
							// On updating to normal, inform the user and send unsubscription details also.
							String unsubscriptionMsg = CacheManagerUtil.getSmsTextCacheManager().getSmsText("SMS", "UNSUBSCRIPTION_MSG", tnbSubsToBeDeactivated[i].language());
							if (unsubscriptionMsg != null) 
							{
								try {
									Tools.sendSMS(getParamAsString("SMS","SMS_NO","123456"), tnbSubsToBeDeactivated[i].subID(), unsubscriptionMsg, false);
								} catch (OnMobileException e) {	}
							}
						}
					}
				}
				else 
				{
					StringBuffer strBuff=new StringBuffer();
					String deactivatedBy = getParamAsString("DEAMON", "DEACT_MODE_OPT_IN", "SMSDaemon");
					boolean deact = rbtConnector.getSubscriberRbtclient().unsubscribe(tnbSubsToBeDeactivated[i].subID(),true, deactivatedBy,"SMSDaemon", strBuff);

					if (deact)
						logger.info("RBT::initial deactivation successful for " + tnbSubsToBeDeactivated[i]);
				}
			}
		}
	}
	 
	 
	public void stopThread()
	{
		m_Continue = false;
		interrupt();
	}

	public boolean updateTNBSubscribertoNormal(Subscriber sub, SubscriptionClass subscriptionClass)
	{
		if (subscriptionClass == null)
			return false;
		return rbtDBManager.updateTNBSubscribertoNormal(sub.subID(), getParamAsBoolean("COMMON", "USE_SUBSCRIPTION_MANAGER", "FALSE"), subscriptionClass.getSubscriptionPeriodInDays());
	}
	private static String getStackTrace(Throwable ex) {
		StringWriter stringWriter = new StringWriter();
		String trace = "";
		if (ex instanceof Exception) {
			Exception exception = (Exception) ex;
			exception.printStackTrace(new PrintWriter(stringWriter));
			trace = stringWriter.toString();
			trace = trace.substring(0, trace.length() - 2);
			trace = System.getProperty("line.separator") + " \t" + trace;
		}
		return trace;
	}

	public static void main(String[] args) {
		Tools.init("RBTSMSDaemon", true);
		RBTSMSDaemon daemon = new RBTSMSDaemon();
	//	daemon.init();
		daemon.start();
		//		new SMSDaemon();
	}

		
	private Clip getClipRBT(String rbt_wav) {
		return rbtConnector.getMemCache().getClipByRbtWavFileName(rbt_wav)	;	
//		return rbtCacheManager.getClipByRbtWavFileName(rbt_wav);
	}
	
	private String getParamAsString(String type, String param, String defualtVal)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defualtVal);
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defualtVal;
		}
	}

	
	private int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter("SMS", param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
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
	 public boolean writeTrans(String type, String request, String resp, String diff)
		{
			HashMap<String,String> h = new HashMap<String,String> ();
			h.put("TYPE", type);
			h.put("REQUEST", request);
			h.put("RESPONSE", resp);
			h.put("TIME DELAY", diff);
			
			if(m_callBackTrans != null)
			{
				m_callBackTrans.writeTrans(h);
				return true;
			}
			return false;
		}
	
}