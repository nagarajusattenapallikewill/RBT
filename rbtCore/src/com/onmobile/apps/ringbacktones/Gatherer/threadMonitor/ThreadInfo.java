package com.onmobile.apps.ringbacktones.Gatherer.threadMonitor;

/**
 * This interface should be implemented for all those threads that should be monitored. 
 * 
 * @author nandakishore
 *
 */
public interface ThreadInfo
{
    /**
     * The thread name is a unique name representing this thread
     * @return
     */
    String getThreadName();
    
    void setThreadName(String threadName);
    
    /**
     * This method should return in brief information of this thread's responsibility.
     * @return
     */
    String getActivity();
    
    /**
     * A string representation of the current status the thread is in.
     * @return
     */
    String getStatus();
    
    /**
     * The present quantitative/qualitative load this thread is presently undergoing.
     * @return
     */
    String getLoad();
    
    /**
     * Return true if this thread is alive. If returned false, the monitor removes this 
     * thread from the registory marking this as dead in the log.
     * @return
     */
    boolean amIAlive();
}
