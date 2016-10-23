package com.onmobile.apps.ringbacktones.hunterFramework;

import java.util.Calendar;
import java.util.Date;

/**
 * This class represents One queue component in the queue container.
 * All worker threads works on this queue component and calls the 'execute' method
 * as the execution of this queue component.
 * 
 * @author nandakishore
 *
 */
public abstract class QueueComponent
{
    /**
     * The worker thread reference who is working on this queue component
     */
    private WorkerThread workerThread = null;

    /**
     * The time at which this object was created.
     */
    private Calendar queueComponentCreationTime = Calendar.getInstance();

    /**
     * The time at which this queue object is started to execute.
     */
    private Calendar executionStartTime = null;

    /**
     * This is called by the worker thread as an execution of one queue object.
     * 
     * @param queueContext The context object that holds the queue container reference.
     */
    abstract public void execute(QueueContext queueContext);

    /**
     * This method is called on any failure not handled in the execute method.
     * 
     * @param queContext The context object that holds the queue container reference.
     * @param e The exception object.
     */
    abstract public void failed(QueueContext queContext, Throwable e);

    /**
     * The unique sequence number that represents this queue object uniquely all the time.
     * @return
     */
    abstract public long getSequenceNo();

    /**
     * @return A unique name representing this queue component. Used for Audit. 
     */
    abstract public String getUniqueName();
    
    /**
     * @return The toString() representation of this queue component.
     */
    abstract public String getDisplayName();
    
    /**
     * This method returns the time when this object was actually created in the data source.
     * This method will return null, if no such relevant information available.
     * @return The time at which this record was created in the data source.
     */
    abstract public Date getObjectCreationTime();

    public WorkerThread getWorkerThread()
    {
        return workerThread;
    }

    public void setWorkerThread(WorkerThread workerThread)
    {
        if(workerThread != null)
        {
            executionStartTime = Calendar.getInstance();
        }
        else
        {
            executionStartTime = null;
        }
        this.workerThread = workerThread;
    }

    public Calendar getQueueComponentCreationTime()
    {
        return queueComponentCreationTime;
    }

    public void setQueueComponentCreationTime(Calendar queueComponentCreationTime)
    {
        this.queueComponentCreationTime = queueComponentCreationTime;
    }

    public Calendar getExecutionStartTime()
    {
        return executionStartTime;
    }

    public void setExecutionStartTime(Calendar executionStartTime)
    {
        this.executionStartTime = executionStartTime;
    }

}
