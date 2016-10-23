package com.onmobile.apps.ringbacktones.hunterFramework;

import java.util.Calendar;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

/**
 * This class is a representation of a queue in runtime.
 * A queueConatiner object contains: 
 * <li>A list of QueueComponents to be executed. </li>
 * <li>A Publisher that loads fills this list.</li>
 * <li>A dynamic set of worker threads that executes QueueComponents.</li>
 * <li>The upper/lower water mark of the queue.</li>
 * <li>The upper worker thread threshold.</li>
 * <li>Holds the runtime queue performance</li>
 * <br>
 * Responsibilities:
 * <li>Compresses and expands the worker thread count based on the need.</li>
 * <li>Notifies the publisher once the queue size goes below lower water mark.</li>
 * <li>Has APIs that gives next max fetch size for the publisher.</li>
 * 
 * @author nandakishore
 *
 */
public class QueueContainer
{
    /**
     * Holds the runtime queue performance related statistics for this queue
     */
    private QueuePerformance queuePerformance = new QueuePerformance();
    
    /**
     * The hunter to which this queue is associated.
     */
    private Hunter hunter = null;
    
    
    private static Vector<QueueContainer> queueContainers = new Vector<QueueContainer>();

    /**
     * @return The list of active queuecontainers in the app.
     */
    public static Vector<QueueContainer> getQueueContainers()
    {
        return queueContainers;
    }

    /**
     * The max allowed queue size. The default is 10000 which gets over ridden 
     * from the configuration.
     */
    private int upperWaterMark = 10000;
    
    /**
     * Once the queue hits the max, the publisher is not allowed to load the queue
     * objects until it hits this lower water mark. The default is 8000 which 
     * gets over ridden from the configuration.
     */
    private int lowerWaterMark = 8000;
    private boolean hasHitUpperThreshHold = false;
    
    /**
     * The list of QueueComponents filled by the publisher
     */
    private Vector<QueueComponent> queElements = new Vector<QueueComponent>();
    
    /**
     * The lock object, used to notify all the worker threads.
     */
    private Object lockObject = new Object();
    
    /**
     * The present runtime worker threads.
     */
    private Vector<WorkerThread> workerThreads = new Vector<WorkerThread>();
    
    /**
     * The abstract publisher reference.
     */
    private Publisher publisher = null;

    /**
     * The queue container name.
     */
    private String queueContainerName = null;
    
    /**
     * The max worker thread threshold which comes from the configuration.
     */
    private int maxAllowedWorkers  = 50;
    
    /**
     * The radio at which the runtime worker thread size is decided 
     */
    private int workerIncrement = 50;

    private Calendar lastPickUpTime = null;

    private Calendar lastProcessTime = null;

    public int getWorkerIncrement()
    {
        return workerIncrement;
    }

    public void setWorkerIncrement(int workerIncrement)
    {
        this.workerIncrement = workerIncrement;
    }

    public int getMaxAllowedWorkers()
    {
        return maxAllowedWorkers;
    }

    public void setMaxAllowedWorkers(int maxAllowedWorkers)
    {
        this.maxAllowedWorkers = maxAllowedWorkers;
    }

    public QueueContainer(Publisher publisherThread)
    {
        super();
        this.publisher = publisherThread;
        queueContainers.add(this);
    }

    private int workerThreadId = 0;

    /**
     * This method is called from the publisher that adds queue components to execute.
     * For every add, the worker thread count is altered if needed.
     * For every add the worker threads are notified so that any hungry threads uses them up.
     * 
     * @param queComponent - The list of queue component objects to be added.
     * @return If any more queue objects can be added to this queue
     */
    public boolean addAllQueueComponent(List<QueueComponent> queComponent)
    {
        queElements.addAll(queComponent);
        updateWorkerThreads();
        notifyQue();
        return canAddMoreQueComponents();
    }

    /**
     * This method is called from the publisher that adds queue components to execute.
     * For every add, the worker thread count is altered if needed.
     * For every add the worker threads are notified so that any hungry threads uses them up.
     * 
     * @param queComponent - The queue component object to be added
     * @return If any more queue objects can be added to this queue
     */
    public boolean addQueueComponent(QueueComponent queComponent)
    {
        queElements.add(queComponent);
        queuePerformance.getIncommingRate().add(1);
        addToOrderedElements(queComponent);
        updateWorkerThreads();
        notifyQue();
        return canAddMoreQueComponents();
    }

    private TreeSet<Long> orderedQueueElements = new TreeSet<Long>();
    
    public void addToOrderedElements(QueueComponent queueComponent)
    {
        synchronized (orderedQueueElements)
        {
            orderedQueueElements.add(queueComponent.getSequenceNo());
        }
    }
    
    public void removeOrderedElements(QueueComponent queueComponent)
    {
        synchronized (orderedQueueElements)
        {
            orderedQueueElements.remove(queueComponent.getSequenceNo());
        }
    }

    public long getOldestSequenceId()
    {
        synchronized (orderedQueueElements)
        {
            Long first = orderedQueueElements.first();
            if(first != null)
            {
                return first.longValue();
            }
        }
        return -1;
    }

    public TreeSet<Long> getOrderedQueueElements()
    {
        return orderedQueueElements;
    }

    public void setOrderedQueueElements(TreeSet<Long> orderedQueueElements)
    {
        this.orderedQueueElements = orderedQueueElements;
    }

    /**
     * This method returns true if more queue components can be added to this queue.
     * The judgement is done based on 3 parameters upperWaterMark,lowerWaterMark and
     * hasHitUpperThreshHold. The flag 'hasHitUpperThreshHold' is marked true 
     * as soon as the queue size hits the upper 'upperWaterMark' and made false 
     * once it goes below 'lowerWaterMark'. 
     * If this flag is on, this method returns false else true.
     * 
     * @return true if you can add more queue components
     */
    public boolean canAddMoreQueComponents()
    {
        int size = getQueueSize();
        if (size >= upperWaterMark)
        {
            hasHitUpperThreshHold = true;
        }
        else if (size < lowerWaterMark)
        {
            hasHitUpperThreshHold = false;
        }
        
        return !hasHitUpperThreshHold;
    }

    public Hunter getHunter()
    {
        return hunter;
    }

    public Object getLockObject()
    {
        return lockObject;
    }
    
	/**
	 * @return the queueContainerName
	 */
	public String getQueueContainerName()
	{
		return queueContainerName;
	}

	/**
	 * @param queueContainerName
	 *            the queueContainerName to set
	 */
	public void setQueueContainerName(String queueContainerName)
	{
		this.queueContainerName = queueContainerName;
	}

    /**
     * Gets the next available queue object at the top. This method removes the 
     * top object and returns. This method is called from the worker thread.
     * If the object could not be executed successfully, then its the app's 
     * responsibility to put it back to the queue.
     * 
     * @return The next available queue component to execute.
     */
    public QueueComponent getNext()
    {
        updateWorkerThreads();
        queuePerformance.getOutgoingRate().add(1);
        if (getQueueSize() == 0)
        {
            return null;
        }
        QueueComponent component = queElements.remove(0);
        notifyPublisherIfRequired();
        return component;
    }
    
	/**
	 * Returns a view of the portion of this Queue between fromIndex,
	 * inclusive, and toIndex, exclusive. (If fromIndex and toIndex are
	 * equal, the returned List is empty.)
	 * 
	 * @param fromIndex
	 *            low endpoint (inclusive) of the subList
	 * @param toIndex
	 *            high endpoint (exclusive) of the subList
	 * @return a view of the specified range within this Queue
	 * @throws IndexOutOfBoundsException
	 *             if an endpoint index value is out of range {@code (fromIndex
	 *             < 0 || toIndex > size)}
	 * @throws IllegalArgumentException
	 *             if the endpoint indices are out of order {@code (fromIndex >
	 *             toIndex)}
	 */
	public List<QueueComponent> queueSubList(int fromIndex, int toIndex)
	{
		return queElements.subList(fromIndex, toIndex);
	}

    /**
     * This method is called on every clear of the queue component.
     * This method notifies the publisher if the queue goesw below the 
     * lower water mark.
     */
    public void notifyPublisherIfRequired()
    {
        if(canAddMoreQueComponents())
        {
            publisher.notifyThread();
        }
    }

    public int getLowerWaterMark()
    {
        return lowerWaterMark;
    }

    public Publisher getPublisher()
    {
        return publisher;
    }

    public QueuePerformance getQueuePerformance()
    {
        return queuePerformance;
    }

    public int getQueueSize()
    {
        return queElements.size();
    }

    public int getUpperWaterMark()
    {
        return upperWaterMark;
    }

	/**
	 * Returns the number of worker threads currently working on this
	 * QueueContainer.
	 * 
	 * @return the number of worker threads currently working on this
	 *         QueueContainer
	 */
	public int getNoOfWorkerThreads()
	{
		return workerThreads.size();
	}
	
	/**
	 * @return the workerThreads
	 */
	public Vector<WorkerThread> getWorkerThreads()
	{
		return workerThreads;
	}

    /**
     * This method return the next max fetch size for the publisher.
     * The publisher can call this method to decide present fetch size.
     * 
     * @return - The runtime max fetch size.
     */
    public int howManyMoreCanIAddNow()
    {
        if (canAddMoreQueComponents())
        {
            int size = getQueueSize();
            return upperWaterMark - size;
        }
        return 0;
    }

    public void start()
    {
        publisher.setQueContainer(this);
        publisher.setUniqueName(hunter.getHunterName()+"_"+getQueueContainerName()+"_Publisher");
        ThreadManager.getThreadManager().addManagedThread(publisher);
    }

    public void stop()
    {
        if(publisher != null)
        {
            ThreadManager.getThreadManager().releaseManagedThread(publisher);
        }
        ThreadManager.getThreadManager().releaseThreads(workerThreads);
    }
    

    public boolean isQueEmpty()
    {
        return getQueueSize() == 0;
    }

    public void notifyQue()
    {
        synchronized (lockObject)
        {
            lockObject.notifyAll();
        }
    }

    public void setHunter(Hunter hunter)
    {
        this.hunter = hunter;
    }

    public void setLowerWaterMark(int lowerThreadhold)
    {
        this.lowerWaterMark = lowerThreadhold;
    }

    public void setPublisher(Publisher publisherThread)
    {
        this.publisher = publisherThread;
    }

    public void setQueuePerformance(QueuePerformance queuePerformance)
    {
        this.queuePerformance = queuePerformance;
    }

    public void setUpperWaterMark(int upperThreashold)
    {
        this.upperWaterMark = upperThreashold;
    }

    /**
     * This is the method that expands and contracts the number of worker
     * threads needed at runtime. 
     */
    private void updateWorkerThreads()
    {
        synchronized (workerThreads)
        {
            int noOfWorkers = getWorkerThreadCount(getQueueSize());
            int presentSize = workerThreads.size();
            if (noOfWorkers == presentSize)
            {
                return;
            }
            if (noOfWorkers > presentSize)
            {
                Vector<WorkerThread> workerThreads = createThreads(this, noOfWorkers - presentSize);
                this.workerThreads.addAll(workerThreads);
            }
            else if (noOfWorkers < presentSize)
            {
                Vector<WorkerThread> toRelease = new Vector<WorkerThread>();
                for (int i = 0; i < presentSize - noOfWorkers; i++)
                {
                    toRelease.add(this.workerThreads.remove(0));
                }
                ThreadManager.getThreadManager().releaseThreads(toRelease);
            }
        }

    }
    
    /**
     * This method returns the presently needed number of worker threads based 
     * on the queue size.
     * 
     * @param queSize - The present queue size
     * @return - The count of number of worker threads needed at run time.
     */
    public int getWorkerThreadCount(int queSize)
    {
        int result = publisher.getWorkerThreadCount(queSize);
        if(result >= 0)
        {
            return result;
        }
        if (queSize == 0)
        {
            return 0;
        }
        result =  Math.min(maxAllowedWorkers, (queSize / workerIncrement) + 1);
        return result;
    }
    
    /**
     * Creates a new worker thread.
     * 
     * @param queueContainer The worker threads context.
     * @return The new worker thread.
     */
    private WorkerThread createThread(QueueContainer queueContainer)
    {
        WorkerThread workerThread = new WorkerThread(queueContainer);
        if (publisher != null)
        {
            workerThread.setUniqueName(publisher.getUniqueName() + "_wt_" + workerThreadId++);
        }
        ThreadManager.getThreadManager().addManagedThread(workerThread);
        return workerThread;
    }

    /**
     * Create a needed list of worker threads.
     * 
     * @param queueContainer - The worker threads context.
     * @param count - Number of worker threads needed
     * @return - The Vector of newly created worker threads.
     */
    public Vector<WorkerThread> createThreads(QueueContainer queueContainer, int count)
    {
        Vector<WorkerThread> result = new Vector<WorkerThread>(count);
        for (int i = 0; i < count; i++)
        {
            result.add(createThread(queueContainer));
        }
        return result;
    }
    
    /**
	 * @return the lastPickUpTime
	 */
	public Calendar getLastPickUpTime()
	{
		return lastPickUpTime;
	}
	
	/**
	 * @param lastPickUpTime the lastPickUpTime to set
	 */
	public void setLastPickUpTime(Calendar lastPickUpTime)
	{
		this.lastPickUpTime = lastPickUpTime;
	}

	/**
	 * 
	 */
	public void setLastPickUpTime()
	{
		setLastPickUpTime(Calendar.getInstance());
	}
	
	/**
	 * @return the lastProcessTime
	 */
	public Calendar getLastProcessTime()
	{
		return lastProcessTime;
	}
	
	/**
	 * @param lastProcessTime the lastProcessTime to set
	 */
	public void setLastProcessTime(Calendar lastProcessTime)
	{
		this.lastProcessTime = lastProcessTime;
	}
	
	/**
	 * 
	 */
	public void setLastProcessTime()
	{
		setLastProcessTime(Calendar.getInstance());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    /**
	 * Returns the string representation of this class.
	 * 
	 * @return the string representation of this class
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("QueueContainer[queueContainerName = ");
		builder.append(queueContainerName);
		builder.append("]");
		return builder.toString();
	}
}
