package com.onmobile.apps.ringbacktones.hunterFramework;

/**
 * This is a abstract class which represents a publisher in the queue component.
 * All the publishers should derive from this class.
 * This class is kept light weight to increase implementation flaexibility
 * 
 * @author nandakishore
 *
 */
public abstract class Publisher extends ManagedDaemon
{
    /**
     * The publisher should go to a wait state if this flag is on.
     * How ever this is not ensured by this class.
     */
    private boolean publisherActive = true;
    
    private int maxFetchSize = 1000;
    private QueueContainer queContainer = null;

    private Object lockObject = new Object();

    public boolean addQueueComponent(QueueComponent queComponent)
    {
        return queContainer.addQueueComponent(queComponent);
    }
    public boolean canAddMoreQueComponents()
    {
        return queContainer.canAddMoreQueComponents();
    }
    /**
     * This method should be over ridden by all the publisher implementation 
     * that holds all the required business logic to publish the queue components
     * in the queue container.
     */
    @Override
    abstract public void execute();

    @Override
    public Object getLockObject()
    {
        return lockObject;
    }

    public int getMaxFetchSize()
    {
        return maxFetchSize;
    }

    public QueueContainer getQueueContainer()
    {
        return queContainer;
    }

    public int getWorkerThreadCount(int queSize)
    {
        return -1;
    }

    abstract public int getWorkerThreadPriority();
    
    public boolean isPublisherActive()
    {
        return publisherActive;
    }

    /**
     * Notification of the thread works only if the publisher is active.
     */
    @Override
    public void notifyThread()
    {
        if(isPublisherActive())
        {
            super.notifyThread();
        }
    }

    public void setLockObject(Object lockObject)
    {
        this.lockObject = lockObject;
    }

    public void setMaxFetchSize(int maxFetchSize)
    {
        this.maxFetchSize = maxFetchSize;
    }

    public void setPublisherActive(boolean isActive)
    {
        this.publisherActive = isActive;
        if(isActive)
        {
            notifyThread();
        }
    }

    public void setQueContainer(QueueContainer queContainer)
    {
        this.queContainer = queContainer;
    }

}
