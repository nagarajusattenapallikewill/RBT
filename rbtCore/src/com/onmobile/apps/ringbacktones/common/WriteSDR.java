package com.onmobile.apps.ringbacktones.common;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTSMS;
import com.onmobile.apps.ringbacktones.common.RBTSMSImpl;
import com.onmobile.smsgateway.accounting.Accounting;

public class WriteSDR implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(WriteSDR.class);
	
    private static Accounting accounting = null;
    private static String m_sdrWorkingDir = null;
    private static int m_rotationSize = 1000;
    private static long m_sdrInterval = 24;
    private static String m_sdrRotation = "size";
    private static boolean m_sdrBillingOn = true;
    private static String tokenList = "EVENT_TYPE SUBSCRIBERID SUBSCRIBER_TYPE REQUEST RESPONSE REQUESTED_TIMESTAMP RESPONSE_TIMEINMS REFERENCE_ID REQUEST_DETAIL RESPONSE_DETAIL";
    
    private static HashMap m_allAccountings = new HashMap();
    
    public WriteSDR()
    { 
    }
    
    private static void createAccounting(String _sdrWorkingDir, int _rotationSize)
    {
		try
		{
			try
			{
	    	    //if (m_sdrWorkingDir == null)
	    		    m_sdrWorkingDir = _sdrWorkingDir;
	    	    //if (rotationSize == null)
	    		    m_rotationSize = _rotationSize;
	    	}
	    	catch (Exception e)
	    	{
			}
	        accounting = Accounting.getInstance(m_sdrWorkingDir, m_rotationSize, m_sdrInterval, m_sdrRotation, m_sdrBillingOn, tokenList);

			if (accounting == null)
				logger.info("RBT::Accounting class can not be created");
			else
				m_allAccountings.put(_sdrWorkingDir, accounting);
		}
		catch (Exception e)
		{
		}
	}
    
    public static synchronized void addToAccounting(String workingDir, int rotationSize, String eventType, String subscriberID , String subscriberType , String request, String response, String requestedTimeStamp, String responseTimeInMillis, String referenceID, String requestDetail, String responseDetail)
    {
    	accounting = null;
    	
    	if(m_allAccountings.containsKey(workingDir))
    		accounting = (Accounting)m_allAccountings.get(workingDir);
    	
	    if(accounting == null)// || (m_sdrWorkingDir != null && !workingDir.equals(m_sdrWorkingDir)))
	    {
	    	createAccounting(workingDir, rotationSize);
	    	logger.info("RBT::Initializing Accounting");
	    }
	    try
	    {
	    	if(accounting != null)
	    	{
	    		 HashMap acMap = new HashMap();
                 
                 acMap.put("EVENT_TYPE",eventType);
                 acMap.put("SUBSCRIBERID", subscriberID);
                 acMap.put("SUBSCRIBER_TYPE", subscriberType);
                 acMap.put("REQUEST", request);
                 acMap.put("RESPONSE",response);
                 acMap.put("REQUESTED_TIMESTAMP", requestedTimeStamp);
                 acMap.put("RESPONSE_TIMEINMS", responseTimeInMillis);
                 acMap.put("REFERENCE_ID", referenceID);
                 acMap.put("REQUEST_DETAIL",requestDetail);
                 acMap.put("RESPONSE_DETAIL",responseDetail);
                     
                 accounting.generateSDR("sms", acMap);
                 
                 logger.info("RBT::Writing to the accounting file");
            }
		}
	    catch(Exception e)
	    {
	    	logger.info("RBT::Exception caught "+e.getMessage());	
	    }
    }  
    
    public static void writeSDRIntoDB(String eventType,String subscriberID, String subscriberType, 
			String request, String response, String requestedTimestamp, String responseTimeinms, String referenceID,
			String requestDetail, String responseDetail){
    	RBTSMS rbtSms = new RBTSMSImpl();
        rbtSms.insert( eventType, subscriberID,  subscriberType, 
    			 request,  response,  requestedTimestamp,  responseTimeinms,  referenceID,
    			 requestDetail,  responseDetail);
    }
}