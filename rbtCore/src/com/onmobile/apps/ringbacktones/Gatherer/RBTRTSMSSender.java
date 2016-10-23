package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;

public class RBTRTSMSSender extends Thread implements iRBTConstant{

	private String _class = "RBTRTSMSSender";
	private static Logger logger = Logger.getLogger(RBTRTSMSSender.class);
	
	public RBTGatherer m_parentGathererThread = null;
	private RBTDBManager rbtDBManager = null;
	private RbtGenericCacheWrapper rbtGenericCacheWrapper = null;

	public static ArrayList m_pendingRTSMS = new ArrayList();
	ArrayList m_RTSMSThreadPool = new ArrayList();
	
	protected RBTRTSMSSender(RBTGatherer m_gathererThread) throws Exception{
		
		logger.info("Entering.....");
		m_parentGathererThread = m_gathererThread;
		if(init())
			logger.info("RBT::inited");
		else
			throw new Exception(" In RBTRTSMSSender: Cannot init Parameters");
	}
	
	public boolean init(){
        logger.info("Entering");
        
        rbtGenericCacheWrapper = RbtGenericCacheWrapper.getInstance();
        rbtDBManager = RBTDBManager.getInstance();
 
        return true;
	}
	
	public void run(){
		
		String _method = "run()";
		logger.info("Entering");
		
		makeThreads();
		while (m_parentGathererThread.isAlive()) 
		{
			try
			{
				logger.info("Entering while loop.");
				checkThreads();
				
				synchronized(m_pendingRTSMS)
				{
					if(m_pendingRTSMS.size() == 0)
						processRTSMSBulk();
				}

			}
			catch(Throwable e)
			{
				logger.error("", e);
			}
			try
			{
				Date next_run_time = m_parentGathererThread.roundToNearestInterVal(getParamAsInt("GATHERER", "GATHERER_SLEEP_INTERVAL", 5));
				long sleeptime = m_parentGathererThread.getSleepTime(next_run_time);
				if(sleeptime < 100)
	            	sleeptime = 500;
	            logger.info(_class + " Thread : sleeping for "+sleeptime + " mSecs.");
	            Thread.sleep(sleeptime);
	            logger.info(_class + " Thread : waking up.");
//				Thread.sleep(sleeptime);
			}
			catch (Throwable E) 
			{
				logger.error("", E);
			}
		}
		logger.info("Exiting");
	}
	
	private void processRTSMSBulk()
    {
    	logger.info("Entering");
    	ViralSMSTable[] context1 = getViralSMSTableLimit(RT_INIT+":SUCCESS",getParamAsInt("GATHERER", "RTSMS_PROCESSING_COUNT", 5000));
    	ViralSMSTable[] context2 = getViralSMSTableLimit(RT_INIT+":FAILURE",getParamAsInt("GATHERER", "RTSMS_PROCESSING_COUNT", 5000));
		if ((context1 == null || context1.length <= 0) && (context2 == null || context2.length <= 0))
		{
			logger.info("Context is null or count <= 0");
			return;
		}
		logger.info("Count of RTSMSContext1success is "+ context1.length);
		logger.info("Count of RTSMSContext2failure is "+ context2.length);
		
		for(int i=0;i<context1.length;i++){
			m_pendingRTSMS.add(context1[i]);
			m_pendingRTSMS.notify();
		}
		for(int i=0;i<context2.length;i++){
			m_pendingRTSMS.add(context2[i]);
			m_pendingRTSMS.notify();
		}
			
	}
	
	public ViralSMSTable[] getViralSMSTableLimit(String type, int count)
	{
		return rbtDBManager.getViralSMSByTypeAndLimit(type, count);
	}
	
	private void makeThreads() 
    {
    	String method = "makeThreads";
    	logger.info("Entering "+method + " with RTSMS size = "+getParamAsInt("GATHERER", "RTSMS_THREAD_POOL_SIZE", 1));
    	for(int i = 0; i < getParamAsInt("GATHERER", "RTSMS_THREAD_POOL_SIZE", 1) ; i++)
		{
    		RBTRTSMSThread tempThread;
			try {
				tempThread = new RBTRTSMSThread(m_parentGathererThread);
				tempThread.start();
				m_RTSMSThreadPool.add(tempThread);
				
				logger.info("Created RTSMS thread "+tempThread);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
		}
	
    }
	
	private void checkThreads()
    {
    	String method = "checkThreads";
    	logger.info("Entering "+method + " with pool size = "+getParamAsInt("GATHERER", "RTSMS_THREAD_POOL_SIZE", 1));
    	for(int i = 0; i < m_RTSMSThreadPool.size() ; i++)
		{
    		RBTRTSMSThread tempThread = (RBTRTSMSThread)m_RTSMSThreadPool.get(i);
			logger.info("Got RTSMS thread "+tempThread);
			if(tempThread == null || !tempThread.isAlive())
			{
				try
				{
					tempThread = new RBTRTSMSThread(m_parentGathererThread);
					tempThread.start();
					m_RTSMSThreadPool.set(i, tempThread);
					logger.info("Created RTSMS thread "+tempThread);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private String getParamAsString(String type, String param, String defaultValue)
	{
		try{
			return rbtGenericCacheWrapper.getParameter(type, param, defaultValue);
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultValue;
		}
	}
	
	private int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = rbtGenericCacheWrapper.getParameter(type, param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal;
		}
	}
	
}
