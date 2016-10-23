package com.onmobile.apps.ringbacktones.hunterFramework;

import com.onmobile.apps.ringbacktones.common.ThreadUtil;

public class TATCalculator
{
    private static final long TurnAroundTimeCalcIntervel = ThreadUtil.OneMin * 2; // 2mins
    private long calulationStartDate = System.currentTimeMillis();
    private int hits = 0;
    private long presentTrunAroundTime = 0;
    public long getCalulationStartDate()
    {
        return calulationStartDate;
    }
    public int getHits()
    {
        return hits;
    }
    public long getPresentTrunAroundTime()
    {
        return presentTrunAroundTime;
    }
    public void setCalulationStartDate(long calulationStartDate)
    {
        this.calulationStartDate = calulationStartDate;
    }
    public void setHits(int hits)
    {
        this.hits = hits;
    }
    public void setPresentTrunAroundTime(long presentTrunAroundTime)
    {
        this.presentTrunAroundTime = presentTrunAroundTime;
    }
    public synchronized void addTrunAroundTime(long newTrunAroundtime)
    {
        long presentTime = System.currentTimeMillis();
        if((presentTime - calulationStartDate )>=TurnAroundTimeCalcIntervel)
        {
            calulationStartDate = presentTime;
            hits = 0;
            presentTrunAroundTime = 0;
        }
        presentTrunAroundTime = (presentTrunAroundTime * hits + newTrunAroundtime)/(hits+1);
        hits++;
    }

}
