package com.onmobile.apps.ringbacktones.hunterFramework;

import org.apache.log4j.Logger;

/**
 * As the name suggest this is a class that should be extended by all the 
 * long running threads. This class is managed by the ThreadMonitor and makes 
 * sure that any managed daemon is up and running given any time.
 * 
 * @author nandakishore
 *
 */
public abstract class ManagedDaemon implements Runnable
{
	private static Logger logger = Logger.getLogger(ManagedDaemon.class);
    /**
     * The threads object associated with this daemon.
     */
    private Thread thread = null;
    
    /**
     * The unique name of this daemon
     */
    private String uniqueName = null;
    
    /**
     * This flag is on my default true. This controls the availability of the 
     * managed daemon. If this flag is false, this threads stop execution.
     */
    private boolean continueExecution = true;

    /**
     * This method is called on a infinite loop until the 'continueExecution'
     * flag is true. This method is need to have the necessary business logic 
     * for one loop.
     */
    protected abstract void execute();

    public String getUniqueName()
    {
        return uniqueName;
    }

    public boolean isContinueExecution()
    {
        return continueExecution;
    }

    /**
     * As the name suggests, this method makes sure that this daemon is always 
     * running. If it comes done for any reason and if the continueExecution is 
     * true, this daemon is restarted.
     */
    public void makeSureIAmRunning()
    {
        if (continueExecution && (thread == null || !thread.isAlive()))
        {
            thread = new Thread(this);
            String uniqueName = getUniqueName();
            thread.setName(uniqueName);
            logger.info("Starting thread:" + uniqueName);
            thread.start();
        }
    }
    
    /**
     * A lock object. This object is used as a semaphore for this thread.
     * The wait and notify works on the object returned by this method.
     * 
     * @return
     */
    public abstract Object getLockObject();
    
    /**
     * This flag is turned on if this thread is waiting on the semaphore.
     */
    private boolean presentlyWaiting = false;
    
    /**
     * This method is called to wait on any event for this daemon.
     * This method waits on the semaphore given by getLockObject method 
     * and if it is not null.
     * 
     * @throws InterruptedException
     */
    public void waitOnThread() throws InterruptedException
    {
        Object lockObject = getLockObject();
        if(lockObject == null)
        {
            return;
        }
        synchronized (lockObject)
        {
            try
            {
                presentlyWaiting = true;
                lockObject.wait(3 * 60 * 1000);// 3 mins
            }
            finally
            {
                presentlyWaiting = false;
            }
        }
    }

    /**
     * This method is used to notify on the semaphore.
     */
    public void notifyThread()
    {
        if(presentlyWaiting)
        {
            Object lockObject = getLockObject();
            if(lockObject != null)
            {
                synchronized (lockObject)
                {
                    lockObject.notifyAll();
                }
            }
        }
    }

    /**
     * The run is a final method, that runs in a infinite loop based on the flag
     * 'continueExecution' and calls the execute() method.
     * This method loops does not stop on any exceptions and errors.
     */
    final public void run()
    {
        while (continueExecution)
        {
            try
            {
                execute();
            }
            catch (Throwable th)
            {
                logger.error("", th);
            }
        }
        logger.info("Stopping thread:"+getUniqueName());
    }

    public void setContinueExecution(boolean continueExecution)
    {
        boolean previous = this.continueExecution;
        this.continueExecution = continueExecution;
        if(previous != continueExecution)
        {
            if(continueExecution)
            {
                makeSureIAmRunning();
            }
        }
    }

    public void setUniqueName(String uniqueName)
    {
        this.uniqueName = uniqueName;
    }

    public Thread getThread()
    {
        return thread;
    }

    public void setThread(Thread thread)
    {
        this.thread = thread;
    }


}
