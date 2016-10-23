package com.onmobile.apps.ringbacktones.hunterFramework;

import com.onmobile.apps.ringbacktones.common.ThreadUtil;

/**
 * This calculator calculates the number of entries per sec of any component.
 * The add method must be called time and again for any entries and the entry 
 * count will be piled up for 10 secs and at the end of the 10 sec the incoming 
 * per sec frequency is calculated.
 * 
 * @author nandakishore
 *
 */
public class EPSCalculator
{
    private long startTime = System.currentTimeMillis();
    private long lastExcecutionsPerSec = 0;
    private long presentExecutions = 0;

    public void add(int count)
    {
        updateCountersIfNeeded();
        presentExecutions = presentExecutions +count;
    }

    public long getLastExcecutionsPerSec()
    {
        updateCountersIfNeeded();
        return lastExcecutionsPerSec;
    }
    
    public void setLastExcecutionsPerSec(long lastExcecutionsPerSec)
    {
        this.lastExcecutionsPerSec = lastExcecutionsPerSec;
    }

    public void updateCountersIfNeeded()
    {
        long presentTime = System.currentTimeMillis();
        if(presentTime - startTime > ThreadUtil.OneSec)
        {
            lastExcecutionsPerSec = presentExecutions;
            presentExecutions = 0;
            startTime = presentTime;
        }
        
    }
}
