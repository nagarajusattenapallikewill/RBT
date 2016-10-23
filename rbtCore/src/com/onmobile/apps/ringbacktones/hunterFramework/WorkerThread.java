package com.onmobile.apps.ringbacktones.hunterFramework;

import java.util.Date;

import org.apache.log4j.Logger;


/**
 * An object of this class represents a worker thread at run time.
 * Worker threads consume the queue components from the queue container and 
 * calls its execute method. Apart from that it also added turn around time 
 * statistics to the queue container.
 * 
 * @author nandakishore
 *
 */
public class WorkerThread extends ManagedDaemon
{
	private static Logger logger = Logger.getLogger(WorkerThread.class);

    private QueueContainer queContainer = null;

    private QueueComponent executingComponent = null;

    public WorkerThread(QueueContainer queContainer)
    {
        super();
        this.queContainer = queContainer;
        String workerName = queContainer.getPublisher().getUniqueName() + "_Worker";
        setUniqueName(workerName);
        logger.info("Creating new Worker thread:" + workerName);
    }

    /**
     * The execute method consume the queue components from the queue container and 
     * calls its execute method. Apart from that it also added turn around time 
     * statistics to the queue container.
     */
    @Override
    public void execute()
    {
        try
        {
            executingComponent = queContainer.getNext();
            QueueContext context = new QueueContext();
            context.setQueueContainer(queContainer);
            if (executingComponent != null)
            {
                long start = System.currentTimeMillis();
                try
                {
                    executingComponent.setWorkerThread(this);
                    queContainer.setLastPickUpTime();
                    executingComponent.execute(context);
                }
                catch (Throwable e)
                {
                	logger.error("", e);
                    try
                    {
                        executingComponent.failed(context, e);
                    }
                    catch (Throwable e1)
                    {
                    	logger.error("", e);
                    }
                }
                finally
                {
                	queContainer.setLastProcessTime();
                    try
                    {
                        setQueuePerformance(start);
                    }
                    catch (Exception e2)
                    {
                        // Dont Log
                    }
                    queContainer.removeOrderedElements(executingComponent);
                    executingComponent.setWorkerThread(null);
                    executingComponent = null;
                }
            }
            else
            {
                waitOnThread();
            }
        }
        catch (Throwable e)
        {
        	logger.error("", e);
        }
    }

    private void setQueuePerformance(long start)
    {
        long end = System.currentTimeMillis();
        queContainer.getQueuePerformance().getWorkerTAT().addTrunAroundTime(end - start);
        Date objectCreationTime = executingComponent.getObjectCreationTime();
        if(objectCreationTime != null)
        {
            queContainer.getQueuePerformance().getTotalTAT().addTrunAroundTime(end - objectCreationTime.getTime());
        }
        
    }

    public QueueContainer getQueContainer()
    {
        return queContainer;
    }

    public void setQueContainer(QueueContainer queContainer)
    {
        this.queContainer = queContainer;
        if (!isContinueExecution())
        {
            queContainer.notifyQue();
        }
    }
    
    /**
	 * @return the executingComponent
	 */
	public QueueComponent getExecutingComponent()
	{
		return executingComponent;
	}

    @Override
    public Object getLockObject()
    {
        return queContainer.getLockObject();
    }
}
