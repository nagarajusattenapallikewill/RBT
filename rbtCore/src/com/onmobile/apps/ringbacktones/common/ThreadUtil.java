package com.onmobile.apps.ringbacktones.common;

public class ThreadUtil
{
    public static final long OneMin = 1000 * 60;
    public static final long OneSec = 1000;

    public static void sleepMin(long noOfMins)
    {
        sleep(OneMin * noOfMins);
    }
    public static void sleepSec(long noOfSec)
    {
        sleep(OneSec * noOfSec);
    }
    public static void sleep(long sleepTime)
    {
        try
        {
            Thread.sleep(sleepTime);
        }
        catch (Exception e)
        {
        }

    }

}
