package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;
import org.javasimon.StopwatchSample;

import com.onmobile.apps.ringbacktones.Gatherer.RBTCopyProcessor;

public class StopwatchThread extends Thread
{
	static long factor = 1000*1000;
	static Logger logger = Logger.getLogger(StopwatchThread.class); 
	@Override
	public void run()
	{
		/*while(true)
        {
                try { Thread.sleep(2000);
                StopwatchSample sample = RBTDaemonManager.smBaseActStopwatch.sampleAndReset();
                logger.error("Sub Mn="+sample.getMean()/factor+", Count="+sample.getCounter() + ", TPS="+(double)sample.getCounter()/(System.currentTimeMillis() - sample.getLastReset())*1000);
                sample = RBTDaemonManager.smBaseDctStopwatch.sampleAndReset();
                logger.error("Sel Mn="+sample.getMean()/factor+", Count="+sample.getCounter() + ", TPS="+(double)sample.getCounter()/(System.currentTimeMillis() - sample.getLastReset())*1000);
                sample = RBTDaemonManager.smSelActStopwatch.sampleAndReset();
                logger.error("DSub Mn="+sample.getMean()/factor+", Count="+sample.getCounter() + ", TPS="+(double)sample.getCounter()/(System.currentTimeMillis() - sample.getLastReset())*1000);
                sample = RBTDaemonManager.smSelDctStopwatch.sampleAndReset();
                logger.error("DSel Mn="+sample.getMean()/factor+", Count="+sample.getCounter() + ", TPS="+(double)sample.getCounter()/(System.currentTimeMillis() - sample.getLastReset())*1000);
                sample = RBTPlayerUpdateDaemon.playerSelActStopwatch.sampleAndReset();
                logger.error("Player Sel Mn="+sample.getMean()/factor+", Count="+sample.getCounter() + ", TPS="+(double)sample.getCounter()/(System.currentTimeMillis() - sample.getLastReset())*1000);
                } catch (InterruptedException e) { }
        }
        */
	}
}
