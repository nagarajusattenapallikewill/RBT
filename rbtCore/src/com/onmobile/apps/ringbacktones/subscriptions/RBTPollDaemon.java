package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.RBTGatherer;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Poll;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class RBTPollDaemon extends Thread implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(RBTPollDaemon.class);
	
	private static RBTDBManager rbtDBManager = null;
	private ParametersCacheManager m_rbtParamCacheManager = null;
	private static Object initObject = new Object();
	private static RBTPollDaemon rbtPollInputReceiver= null;
	private static RBTGatherer rbtGatherer = null;
	
	private static String POLL = "POLL"; 
	private static String POLLED = "POLLED";
	
	private SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy");
	private String m_poll_file = null;
	private BufferedWriter	m_pollbufferWriter = null;
	private long m_nextWakeUpTimeForPollFile = 0;

	
	private RBTPollDaemon(RBTGatherer rbtGatherer)
	{
		this.rbtGatherer = rbtGatherer;
	}

	public static RBTPollDaemon getInstance(RBTGatherer rbtGatherer)
	{
		if(rbtPollInputReceiver !=null)
			return rbtPollInputReceiver;
	
		synchronized(initObject)
		{
			if(rbtPollInputReceiver != null)
				return rbtPollInputReceiver;
			rbtPollInputReceiver = new RBTPollDaemon(rbtGatherer);
			rbtPollInputReceiver.initialize();
			rbtPollInputReceiver.start();
			return rbtPollInputReceiver;
		}
	}
	private void initialize()
	{
		m_rbtParamCacheManager =  CacheManagerUtil.getParametersCacheManager();
    	rbtDBManager =  RBTDBManager.getInstance();

		m_nextWakeUpTimeForPollFile = getNextTime();
		
	}
	
	public void run()
	{
		while(true)
		{
			if(rbtGatherer != null && rbtGatherer.isAlive())
			{
				ViralSMSTable[] vst = rbtDBManager.getViralSMSByTypeAndLimit(POLL, getParamAsInt("POLL_PROCESS_COUNT", 5000));
				if(vst != null && vst.length > 0)
				{
					logger.info("No of poll records  = "+vst.length);
					processPollBulk(vst);
				}
				else
				{
					logger.info("No poll records found.");
				}
				logger.info("Sleeping..... ");
				try
				{
					Thread.sleep(getParamAsInt("POLL_SLEEP_INTERVAL_SECONDS", 2*60*1000));
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
				logger.info("Waking up..... ");
			}	
		}
	}
	private void processPollBulk(ViralSMSTable[] vst)
	{
		logger.info("Entering.....");
		openTrans();
		for(int i = 0; i < vst.length; i++)
			processPoll(vst[i]);
		closeTrans();
		logger.info("Exiting.....");
	}
	
	private void processPoll(ViralSMSTable vst)
	{
		logger.info("Entering");
		String subscriberID = rbtDBManager.subID(vst.subID());
		String callerID = rbtDBManager.subID(vst.callerID());
		String clipID = vst.clipID();
		StringTokenizer stk = new StringTokenizer(clipID, ":");
		String pollID = stk.nextToken();
		String pollInput = stk.nextToken();
		Poll poll = rbtDBManager.getPoll(pollID);
		if(poll == null)
			poll = rbtDBManager.insertPoll(pollID);
		String caller_type = "UNKNOWN";
		int percentage = 50;
		String reply = "YES";
		Subscriber subscriber = Processor.getSubscriber(callerID);
		
		if(pollInput.equalsIgnoreCase(getParamAsString("POLL_TOKEN_YES")))
		{
			if(subscriber.isValidPrefix())
			{
				poll.incrementNoOfYes_Incircle();
				caller_type = "INCIRCLE";
			}
			else if(subscriber.getCircleID() != null)
			{
				poll.incrementNoOfYes_outcircle();
				caller_type = "OPERATOR";
			}
			else
				poll.incrementNoOfYes_OtherOperator();
			percentage = poll.totalYesCount()*100/(poll.totalYesCount()+poll.totalNoCount());
			//sms = substitutedSms(yesSMS, ""+percentage);
			
		}
		else if(pollInput.equalsIgnoreCase(getParamAsString("POLL_TOKEN_NO")))
		{
			if(subscriber.isValidPrefix())
			{
				poll.incrementNoOfNo_Incircle();
				caller_type = "INCIRCLE";
			}
			else if (subscriber.getCircleID() != null)
			{
				poll.incrementNoOfNo_outcircle();
				caller_type = "OPERATOR";
			}
			else
				poll.incrementNoOfNo_OtherOperator();
			percentage = poll.totalNoCount()*100/(poll.totalYesCount()+poll.totalNoCount());
			reply = "NO";
			//sms = substitutedSms(noSMS,""+percentage);
		}
		else
		{
			logger.info("Invalid input for Poll id"+pollID);
			updateViralTable(vst, caller_type);
			return;
		}
		boolean updatePoll = true;
		if(subscriber.isValidPrefix())
		{
			if (getParamAsBoolean("POLL_SEND_SMS_LOCAL", "FALSE"))
				updatePoll = hitSM(callerID,percentage,reply);
				//sendSMS(callerID, sms);
		}
				
		else if (subscriber.getCircleID() != null)
		{
			if(getParamAsBoolean("POLL_SEND_SMS_NATIONAL", "FALSE"))
				updatePoll = hitSM(callerID,percentage,reply);
			//sendSMS(callerID, sms);
		}
		else if(getParamAsBoolean("POLL_SEND_SMS_OTHEROPERATOR", "FALSE"))
		{
			updatePoll = hitSM(callerID,percentage,reply);
			//sendSMS(callerID, sms);
		}
		else
		{
			
			logger.info("No SMS sent for poll");
		}
		if(updatePoll)
		{
			rbtDBManager.updatePoll(poll);
			updateViralTable( vst, caller_type);
		}
	}
	
	private synchronized void openTrans()
	{
		try
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new java.util.Date(System.currentTimeMillis()));
			String date = m_format.format(calendar.getTime());
			String file_prefix = null;
			String fileName = null;
			
			file_prefix = "POLL_FILE";
			fileName = null;
			if(m_poll_file != null && new File(m_poll_file+".csv").isFile())
			{
				fileName = m_poll_file;
				logger.info("*** RBT::writing Poll file (append) : " +m_poll_file);
				m_pollbufferWriter = new BufferedWriter(new FileWriter(fileName + ".csv", true));
			}
			else
			{
				fileName =  getParamAsString("POLL_INPUTS_FOLDER") + File.separator  +file_prefix + "_" + date ;
				m_poll_file = fileName;
				boolean newFile = false;
				if(!(new File(fileName + ".csv").exists()))
				{
					newFile = true;
				}
				logger.info("*** RBT::writing poll file (append) : " +m_poll_file);
				m_pollbufferWriter = new BufferedWriter(new FileWriter(fileName + ".csv", true));
				if(newFile)
				{		
					m_pollbufferWriter.write("CALLED,CALLER,INPUT,CALLER_TYPE,RESULT");
				}
				m_pollbufferWriter.flush();
			}
			logger.info("*** RBT::writing POLL file with name = "+fileName + ".csv");
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}

	private synchronized void closeTrans()
	{

		if(m_pollbufferWriter != null)
		{
			try
			{
				logger.info("*** RBT::closing POLL file ");
				m_pollbufferWriter.flush();
				m_pollbufferWriter.close();
				m_pollbufferWriter = null;
			}
			catch(Exception e)
			{
				logger.error("", e);  
				e.printStackTrace();
				m_pollbufferWriter = null;
			}  
		}

		try
		{
			logger.info("*** RBT::checking to create new Trans " + new Date (System.currentTimeMillis() + 10000) + " wakeUp " + new Date (m_nextWakeUpTimeForPollFile));
			if( (System.currentTimeMillis()  + 10000) >= m_nextWakeUpTimeForPollFile)
			{
				m_poll_file = null;
				m_nextWakeUpTimeForPollFile = getNextTime();
			}
		}
		catch(Exception e)
		{
			logger.error("", e);  
			e.printStackTrace();
		}
	}

	private synchronized void writeTrans(String subid , String callerID , String input, String caller_type, String result)
	{
		logger.info("RBT::" +subid);  
		try
		{
			m_pollbufferWriter.newLine();
			m_pollbufferWriter.write(subid);
			m_pollbufferWriter.write(","+callerID);
			m_pollbufferWriter.write(","+input);
			m_pollbufferWriter.write(","+caller_type);
			m_pollbufferWriter.write(","+result);
			m_pollbufferWriter.flush();
		}
		catch(Exception e)
		{
			logger.error("", e);  
		}
	}

	private void sendSMS(String subscriber, String sms)
    {
        try
        {
            if(sms != null)
            	Tools.sendSMS(getSenderNumber(subscriber), subscriber, sms, false);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
    }
	
	public String getSenderNumber(String circleID) {
		String senderNumber = getParamAsString("DAEMON", "SENDER_NO", null);
		if(circleID != null && circleID.length() > 0) {
			String operatorName = circleID.indexOf("_") != -1 ? circleID.substring(0, circleID.indexOf("_")) : null;
			if(operatorName != null && operatorName.trim().length() > 0) {
				senderNumber = getParamAsString("GATHERER", operatorName +"_SENDER_NO", senderNumber);
			}
		}
		logger.info("senderNumber :" + senderNumber);
		return senderNumber;
	}
	
	private String substitutedSms(String sms, String token)
	{
		if(sms == null)
			return sms;
		if (token == null)
			token = "";
		if(sms.indexOf("%S") != -1)
			sms = sms.substring(0,sms.indexOf("%S")) + token + sms.substring(sms.indexOf("%S")+2);
		return sms;
	}
	private void removeViralPromotion(String subscriberID, String callerID,
            Date sentTime, String type)
    {
		rbtDBManager.removeViralPromotion(subscriberID, callerID, sentTime, type);
    }

	private void updateViralPromotion(String subscriberID, String callerID,
            Date sentTime, String fType, String tType)
    {
		rbtDBManager.updateViralPromotion(subscriberID, callerID, sentTime, fType,
                                      tType,
                                      new Date(System.currentTimeMillis()),
                                      null,null);
    }
	
    private long getNextTime()
    {
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);
    	cal.set(Calendar.HOUR,0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	
    	return cal.getTime().getTime(); 
    }
	
    private boolean hitSM(String callerID, int percentage,String reply)
    {
    	String url = getParamAsString("POLL_URL") +"percentage:"+percentage+"|reply:"+reply+ "&msisdn="+callerID;
    	Integer statusCode = new Integer(-1);
    	StringBuffer response = new StringBuffer();
    	Tools.callURL(url, statusCode, response);
		String responseStr  = response.toString();
		if(responseStr.indexOf("SUCCESS") > -1)
			return true;
		else
			return false;
    }
	
	private void updateViralTable(ViralSMSTable vst, String caller_type)
	{
			if(getParamAsBoolean("POLL_FILE_WRITE", "FALSE"))
			{
				writeTrans(vst.subID(), vst.callerID(), vst.clipID(), caller_type, POLLED);
				removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), POLL);
			}
			else
				updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), POLL, POLLED);
	}
	
	public String getParamAsString(String param)
	   {
	    	try{
	    		return m_rbtParamCacheManager.getParameter("COMMON", param, null).getValue();
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param );
	    		return null;
	    	}
	    }
	    
	  public String getParamAsString(String type, String param, String defualtVal)
	    {
	    	try{
	    		return m_rbtParamCacheManager.getParameter(type, param, defualtVal).getValue();
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defualtVal;
	    	}
	    }
	    
	    public int getParamAsInt(String param, int defaultVal)
	    {
	    	try{
	    		String paramVal = m_rbtParamCacheManager.getParameter("COMMON", param, defaultVal+"").getValue();
	    		return Integer.valueOf(paramVal);   		
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param );
	    		return defaultVal;
	    	}
	    }
	    
	    public int getParamAsInt(String type, String param, int defaultVal)
	    {
	    	try{
	    		String paramVal = m_rbtParamCacheManager.getParameter(type, param, defaultVal+"").getValue();
	    		return Integer.valueOf(paramVal);   		
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defaultVal;
	    	}
	    }
	    
	    public boolean getParamAsBoolean(String param, String defaultVal)
	    {
	    	try{
	    		return m_rbtParamCacheManager.getParameter("COMMON", param, defaultVal).getValue().equalsIgnoreCase("TRUE");
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param );
	    		return defaultVal.equalsIgnoreCase("TRUE");
	    	}
	    }
	    public boolean getParamAsBoolean(String type, String param, String defaultVal)
	    {
	    	try{
	    		return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defaultVal.equalsIgnoreCase("TRUE");
	    	}
	    }

}
