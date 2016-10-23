package com.onmobile.apps.ringbacktones.hunterFramework;

public class QueuePerformance
{
    private TATCalculator workerTAT = new TATCalculator();
    private TATCalculator totalTAT = new TATCalculator();
    private EPSCalculator incommingRate = new EPSCalculator();
    private EPSCalculator outgoingRate = new EPSCalculator();

    public EPSCalculator getIncommingRate()
    {
        return incommingRate;
    }

    public void setIncommingRate(EPSCalculator incommingRate)
    {
        this.incommingRate = incommingRate;
    }

    public EPSCalculator getOutgoingRate()
    {
        return outgoingRate;
    }

    public void setOutgoingRate(EPSCalculator outgoingRate)
    {
        this.outgoingRate = outgoingRate;
    }

    public TATCalculator getTotalTAT()
    {
        return totalTAT;
    }

    public TATCalculator getWorkerTAT()
    {
        return workerTAT;
    }

    public void setTotalTAT(TATCalculator totalTAT)
    {
        this.totalTAT = totalTAT;
    }

    public void setWorkerTAT(TATCalculator workerTAT)
    {
        this.workerTAT = workerTAT;
    }
}
