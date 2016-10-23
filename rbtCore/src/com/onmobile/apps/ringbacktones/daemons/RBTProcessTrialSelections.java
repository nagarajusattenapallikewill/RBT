package com.onmobile.apps.ringbacktones.daemons;

import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;

public class RBTProcessTrialSelections extends Thread{
	
	private static Logger logger = Logger.getLogger(RBTProcessTrialSelections.class);
	
	private RBTDaemonManager m_mainDaemonThread = null; 
	private static RBTDBManager rbtDBManager = null;
	ParametersCacheManager m_rbtParamCacheManager = null;

	protected RBTProcessTrialSelections(RBTDaemonManager mainDaemonThread) 
	{
		try
		{
			setName("RBTProcessTrialSelections");
			m_mainDaemonThread = mainDaemonThread;
			init();
		}
		catch(Exception e)
		{
			logger.error("Issue in creating RBTProcessTrialSelections", e);
		}
	}
	
	public void init()
	{
		m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		rbtDBManager = RBTDBManager.getInstance();
	}
	
	public void run()
	{
		while(m_mainDaemonThread != null && m_mainDaemonThread.isAlive()) 
		{
			
			processSelectionsTrial();
			
			try
			{
				logger.info("Process Trial Selections Thread Sleeping for 5 minutes............");
				Thread.sleep(getParamAsInt("SLEEP_INTERVAL_MINUTES", 5) * 60 * 1000);
			}
			catch(Exception e)
			{
			}
		}
	}

	/*
	 * Gets a list of subscriber selections which are trial and end date is greater than system date and next charging date is less than system date
	 *   
	 * */
	private boolean processSelectionsTrial() {
		SubscriberStatus[] ts = smGetTrialSelections();
		if(ts != null && ts.length > 0) {
			for(int i = 0; i < ts.length; i++) {

				logger.info("RBT::Trial subscription ends today for subscriber " + ts[i].subID());
				boolean OptIn = false;
				Subscriber subscribers = getSubscriber(ts[i].subID());
				if(subscribers == null)
					continue;
				else if(subscribers.activationInfo() != null
						&& subscribers.activationInfo().indexOf(":optin:") != -1)
					OptIn = true;
				smTrialSelectionCharging(ts[i].subID(), ts[i].callerID(), ts[i].status(), ts[i]
						.setTime(), OptIn);
			}
		}
		return true;
	}
	
	private SubscriberStatus[] smGetTrialSelections() {
		return rbtDBManager.smGetTrialSelections(getParamAsInt("FETCH_SIZE", 5));
	}
	
	private Subscriber getSubscriber(String subscriberID) {
		return rbtDBManager.getSubscriber(subscriberID);
	}
	
	private boolean smTrialSelectionCharging(String subscriberID, String callerID, int status,
			Date setDate, boolean OptIn) {
		return rbtDBManager
				.smTrialSelectionCharging(subscriberID, callerID, status, setDate, OptIn);
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
}
