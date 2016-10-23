package com.onmobile.apps.ringbacktones.ussd.tatagsm;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class SessionDeleteDaemon extends TimerTask
{
	private Timer timer = new Timer();
	private int sessiontimeInterval = 2;
	private int deleteSessionInterval = 1;
	private static final Logger logger = Logger.getLogger(SessionDeleteDaemon.class);

	/**
	 * constructor 
	 */
	public SessionDeleteDaemon()
	{
		String method = "SessionDeleteDaemon";
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + method + " Initialization Started");
		String time = USSDConfigParameters.getInstance().getParameter("SESSION_TIME_INTERVAL");
		if (time == null)
			time = "1";
		sessiontimeInterval = Integer.parseInt(time);
		time = USSDConfigParameters.getInstance().getParameter("DELETE_TIME_INTERVAL");
		if (time == null)
			time = "1";
		deleteSessionInterval=Integer.parseInt(time);

	}
	/**
	 * deleteSessions
	 * This method deletes the session stored in hash map
	 * according to session time
	 */
	private void deleteSessions()
	{
		Set<String> keySet = USSDSearchSessionManager.allSessions.keySet();
		List<String> keyList=new ArrayList<String>();

		for (String key : keySet)
		{
			keyList.add(key);
		}
		for (String subscriberId : keyList)
		{
			USSDSession session = USSDSearchSessionManager.getSearchSession(subscriberId);
			if ((System.currentTimeMillis()-session.getSessionCreatedTime()) > (sessiontimeInterval* 60 * 1000)){
				USSDSearchSessionManager.invalidateSearchSession(subscriberId);
				if(logger.isDebugEnabled())
					logger.debug(this.getClass().getName() + " deleteSessions " + " deleted session for "+subscriberId);	
			}


		}
	}
	/**
	 * Start the timer task in specified interval
	 */
	public  void startsessionDeletingDaemon()
	{
		String method = "startsessionDeletingDaemon";
		synchronized (this) {

			if(logger.isDebugEnabled())
				logger.debug(this.getClass().getName() + method + "Starting the timer task...");
			timer.schedule(this, deleteSessionInterval *60*60*1000);
		}
	}
	@Override
	public void run()
	{
		String method = "run";
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + method + "Trying to delete the sessions");
		this.deleteSessions();
	}
}
