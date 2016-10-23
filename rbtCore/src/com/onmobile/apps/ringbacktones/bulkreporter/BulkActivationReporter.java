/**
 * 
 */
package com.onmobile.apps.ringbacktones.bulkreporter;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

/**
 * @author vinayasimha.patil
 *
 */
public class BulkActivationReporter implements Runnable
{
	private static Logger logger = Logger.getLogger(BulkActivationReporter.class);
	
	private Object waitObject = null;
	private Thread bulkActivationReporterThread = null;
	private Timer bulkActivationReporterTimer = null;

	public BulkActivationReporter()
	{
		waitObject = new Object();
		bulkActivationReporterTimer = new Timer();
		bulkActivationReporterThread = new Thread(this);
		bulkActivationReporterThread.start();
	}

	/* (non-Javadoc)
	 * @see java.util.Runnable#run()
	 */
	public void run()
	{
		String mailingTime = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "MAIL_SENDING_TIME", null);
		Date mailingStartTime = new Date();
		if(mailingTime != null)
		{
			String[] time = mailingTime.split(":");
			int hour = Integer.parseInt(time[0]);
			int minute = Integer.parseInt(time[1]);
			Calendar calender = Calendar.getInstance();
			calender.set(Calendar.HOUR_OF_DAY, hour);
			calender.set(Calendar.MINUTE, minute);
			Date curDate = new Date();
			if(curDate.after(calender.getTime()))
				calender.add(Calendar.DAY_OF_YEAR, 1);

			mailingStartTime = calender.getTime();
		}
		logger.info("RBT:: mailingStartTime = "+ mailingStartTime);
		BulkActivationReporterTask bulkActivationReporterTask = new BulkActivationReporterTask();
		bulkActivationReporterTimer.scheduleAtFixedRate(bulkActivationReporterTask, mailingStartTime, 1000*60*60*24);

		synchronized (waitObject) 
		{
			try 
			{
				waitObject.wait();
			} 
			catch (InterruptedException e) 
			{
				logger.error("", e);
			}
		}
	}

	public void stop()
	{
		bulkActivationReporterTimer.cancel();
		bulkActivationReporterThread.interrupt();
		logger.info("RBT:: Stopped BulkActivationReporter");
	}

	private class BulkActivationReporterTask extends TimerTask
	{
		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run()
		{
			logger.info("RBT:: Sending Mail");
			String[] args = new String[0];
			SendBulkActivationReports.main(args);
		}
	}
}


