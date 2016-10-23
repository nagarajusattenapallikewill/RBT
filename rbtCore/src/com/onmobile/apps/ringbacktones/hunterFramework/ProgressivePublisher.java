package com.onmobile.apps.ringbacktones.hunterFramework;

import org.apache.log4j.Logger;

/**
 * All publishers are recommended to work in a progressive way so that the Workers 
 * (Consumers) don�t starve for data. A publisher should fill the queue in parallel 
 * with the consumers consuming it. This can be accomplished by working on 
 * sequence number. Each and every queue component is associated with a unique 
 * auto incrementing sequence number. The publisher fetched from the underlying 
 * data source with this unique sequence Number and ordered ascending on the same. 
 * The publisher remembers the last fetch sequence number every time and gets the 
 * next set of Queue Components after the last remembered sequence number.
 * 
 * @author nandakishore
 *
 */
public abstract class ProgressivePublisher extends Publisher
{
	private static Logger logger = Logger.getLogger(ProgressivePublisher.class);
	
    /**
     * The running sequence number. This number is updated with the 
     * QueueComponent's getSequenceNo on every load to the queue container.
     * queue container. 
     */
    private long presentSequenceId = 0;

    /**
     * The framework business logic implementation as explained in the class header
     */
    @Override
    public void execute()
    {
        /*
         * Go to a wait state if the publisher is inactive.
         */
        if(!isPublisherActive())
        {
            try
            {
                waitOnThread();
            }
            catch (InterruptedException e)
            {
                // TODO Put to logs. This should not happen
                e.printStackTrace();
            }
        }
        if(isPublisherActive())
        {
        	fetchNextCycle();
        }
    }

    public void fetchNextCycle()
    {
        fetchNextCycle(false);
    }

    public void fetchNextCycle(boolean force) 
    {
        QueueContainer queueContainer = getQueueContainer();
        int addCount = queueContainer.howManyMoreCanIAddNow();
        int maxFetchSize = getMaxFetchSize();
        if(maxFetchSize > 0)
        {
            addCount = Math.min(addCount, maxFetchSize);
        }
        int count = 0;
        boolean done = true;
        if(!force && addCount != 0)
        {
            try
            {
                // -1 means no limit to load
                executeQuery(force?-1:addCount);
                while(hasMoreQueueComponents())
                {
                    QueueComponent component = getNextQueueComponent();
                    if (component != null)
                    {
                        long presentSequenceId = component.getSequenceNo();
                        setPresentSequenceId(presentSequenceId);
                        boolean added = addQueueComponent(component);
                        count++;
                        if (!force && !added)
                        {
                            break;
                        }
                    }
                }
            }
            catch (HunterException e)
            {
            	done = false;
                logger.error("", e);
            }
            finally
            {
                finaliseQuery();
            }
            
        }
        if(done)
        {
            setPresentQueryCount(addCount, count);
        }
	}

    /**
     * This method should execute whose sequence id is greater than 'presentSequenceId'.
     * The max allowed fetch size is driven by the paramter 'count'
     * 
     * @param count
     * @throws HunterException
     */
    abstract protected void executeQuery(int count) throws HunterException;

    /**
     * This method is called to finalise any resourced used to execute the 
     * present query.
     */
    abstract protected void finaliseQuery();

    /**
     * This method reads the underlying data source and gets the next queue 
     * component associated with the present executing query.
     * 
     * @return The next available queue component to be published.
     * @throws HunterException
     */
    abstract protected QueueComponent getNextQueueComponent() throws HunterException;

    public long getPresentSequenceId()
    {
        return presentSequenceId;
    }

    /**
     * This method returns true if the executed queuey has any more queue components
     * to lookup.
     * 
     * @return true if any more queue components available for this request.
     * @throws HunterException
     */
    abstract protected boolean hasMoreQueueComponents() throws HunterException;

    /**
     * This method is called at the end of every query execution. This method 
     * passed two parameters 
     * 
     * @param addCount - The actual fetch size for the current query execution.
     * @param count - The total queue components reads for this query execution.
     */
    abstract protected void setPresentQueryCount(int addCount, int count);

    public void setPresentSequenceId(long presentSequenceId)
    {
        if(presentSequenceId <this.presentSequenceId)
        {
            //Tools.logException(null, null, new Exception("Warning: The sequence number is moveing back "+presentSequenceId+"<"+this.presentSequenceId));
        }
        this.presentSequenceId = presentSequenceId;
    }

}
