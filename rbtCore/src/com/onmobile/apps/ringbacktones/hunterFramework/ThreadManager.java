package com.onmobile.apps.ringbacktones.hunterFramework;

import java.util.HashMap;
import java.util.Vector;

import com.onmobile.apps.ringbacktones.common.ExceptionUtil;
import com.onmobile.apps.ringbacktones.common.ThreadUtil;

public class ThreadManager extends ManagedDaemon
{
    private Vector<ManagedDaemon> managedDaemons = new Vector<ManagedDaemon>();
    private HashMap<String,ManagedDaemon> managedDeamonsMap = new HashMap<String,ManagedDaemon>();

    private static ThreadManager threadManager = new ThreadManager();

    public static ThreadManager getThreadManager()
    {
        return threadManager;
    }

    public ThreadManager()
    {
        setUniqueName("Thread manager");
        makeSureIAmRunning();
    }

    public void addManagedThread(ManagedDaemon managedThread)
    {
        managedThread.makeSureIAmRunning();
        managedDaemons.add(managedThread);
        String uniqueName = managedThread.getUniqueName();
        if(uniqueName != null)
        {
            managedDeamonsMap.put(uniqueName,managedThread);
        }
    }

    public void releaseManagedThread(ManagedDaemon managedDaemon)
    {
        managedDaemons.remove(managedDaemon);
        String uniqueName = managedDaemon.getUniqueName();
        if(uniqueName != null)
        {
            managedDeamonsMap.put(uniqueName,managedDaemon);
        }
        managedDaemon.setContinueExecution(false);
    }

    public String getStackTrace(String uniqueName)
    {
        ManagedDaemon managedDaemon = managedDeamonsMap.get(uniqueName);
        if(managedDaemon != null)
        {
            StackTraceElement stack[] = managedDaemon.getThread().getStackTrace();
            Exception e= new Exception("Thread dump");
            e.setStackTrace(stack);
            String exceptionString = ExceptionUtil.toString(e);
            return exceptionString;
        }
        return null;
    }

    public void releaseAllThreads()
    {
        releaseThreads((Vector<? extends ManagedDaemon>) managedDaemons.clone());
    }

    public void releaseThreads(Vector<? extends ManagedDaemon> toRelease)
    {
        synchronized (toRelease)
        {
            for (ManagedDaemon managedDaemon : toRelease)
            {
                releaseManagedThread(managedDaemon);
            }
        }
    }

    @Override
    public void execute()
    {
        try
        {
            Vector<ManagedDaemon> toRemove = new Vector<ManagedDaemon>();
            Vector<ManagedDaemon> daemons = (Vector<ManagedDaemon>) this.managedDaemons.clone();
            for (ManagedDaemon daemon : daemons)
            {
                if (daemon.isContinueExecution())
                {
                    daemon.makeSureIAmRunning();
                }
                else
                {
                    toRemove.add(daemon);
                }
            }
            daemons.removeAll(toRemove);
        }
        finally
        {
            ThreadUtil.sleepMin(1);// Sleep for 1 min
        }
    }

    @Override
    public Object getLockObject()
    {
        return null;
    }
}
