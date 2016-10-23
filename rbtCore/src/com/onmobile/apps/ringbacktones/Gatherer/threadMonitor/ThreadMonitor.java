package com.onmobile.apps.ringbacktones.Gatherer.threadMonitor;

import java.util.Vector;

import org.apache.log4j.Logger;


/**
 * 
 * This class is a singleton holds the set of registered threads and dumps the thread health periodically. 
 * Its the responsibility of the thread by itself to get registered in the ThreadMonitor component.
 * 
 * @author nandakishore
 *
 */
public class ThreadMonitor extends Thread
{
	private static Logger logger = Logger.getLogger(ThreadMonitor.class);
    /**
     * Holds the list of registered threads whose helth should be dumped periodically
     */
    private Vector<ThreadInfo> registeredThreadInfo = new Vector<ThreadInfo>();
    
    /**
     * The thread monitor's signleton object
     */
    private static ThreadMonitor monitor = new ThreadMonitor();

    /**
     * Get method to get the signleton object
     * @return
     */
    public static ThreadMonitor getMonitor()
    {
        return monitor;
    }

    /**
     * This method dumps all the present registered threads to the log file.
     * This also takes care of cleaning the dead threads from the registry and 
     * also log them as warnings.
     * The information logged are ThreadName,ThreadActivity ThreadStatus,ThreadLoad
     */

	public void run()
	{
		while(true)
		{
			try
			{
				ThreadMonitor.getMonitor().dumpAllThreadInfo();
				Thread.sleep(2000*60);
			}
			catch(Exception e)
			{
					logger.error("", e);
			}
			catch(Throwable t)
			{
				logger.error("", t);
			}
		}
	}
    public void dumpAllThreadInfo()
    {
        logAndRemoveDeadThreads();
        logger.info("************Threads health Dump***********");
        for (ThreadInfo threadInfo : registeredThreadInfo)
        {
                dumpThreadInfo(threadInfo, false);
        }
        logger.info("********************************************");
    }

    private void dumpThreadInfo(ThreadInfo threadInfo, boolean isWarning)
    {
        if (isWarning)
        {
            logger.info("Thread Name:" + threadInfo.getThreadName());
            logger.info("Thread Activity:" + threadInfo.getActivity());
            logger.info("Thread Status:" + threadInfo.getStatus());
            logger.info("Thread Load:" + threadInfo.getLoad());
        }
        else
        {
            logger.info("Thread Name:" + threadInfo.getThreadName());
            logger.info("Thread Activity:" + threadInfo.getActivity());
            logger.info("Thread Status:" + threadInfo.getStatus());
            logger.info("Thread Load:" + threadInfo.getLoad());
        }
    }

    private void logAndRemoveDeadThreads()
    {
        Vector<ThreadInfo> toRemove = new Vector<ThreadInfo>();
        for (ThreadInfo threadInfo : registeredThreadInfo)
        {
            if (!threadInfo.amIAlive())
            {
                toRemove.add(threadInfo);
            }
        }
        if (toRemove.size() != 0)
        {
            logger.info("************Dead Threads recycled***********");
            for (ThreadInfo threadInfo : toRemove)
            {
                dumpThreadInfo(threadInfo, true);
            }
            logger.info("********************************************");
            registeredThreadInfo.removeAll(toRemove);
        }
    }

    /**
     * Registers a new thread for monitoring. 
     * @param threadInfo
     */
    public void register(ThreadInfo threadInfo)
    {
        if(registeredThreadInfo.indexOf(threadInfo) == -1)
        {
            registeredThreadInfo.add(threadInfo);
        }
    }
}
