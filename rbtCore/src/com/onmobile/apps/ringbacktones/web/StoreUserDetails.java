package com.onmobile.apps.ringbacktones.web;

import java.util.Hashtable;

public class StoreUserDetails
{

    private static Hashtable m_cacheHistory = new Hashtable();
    private static Hashtable m_cacheRecent = new Hashtable();
    private static long m_lastCacheFlush = System.currentTimeMillis();
    private static final int m_cacheClearIntervalMins = 30;

    public static void storeUserDetails(String subID, User user)
            throws Exception
    {
        removeUserDetails(subID);

        if (user != null)
            m_cacheRecent.put(subID, user);

        long now = System.currentTimeMillis();
        long timediffInMins = ((now - m_lastCacheFlush) / (1000 * 60));
        if (timediffInMins > m_cacheClearIntervalMins)
        {
            synchronized (m_cacheHistory)
            {
                m_cacheHistory.clear();
                m_cacheHistory = (Hashtable) m_cacheRecent.clone();
                m_cacheRecent.clear();
                m_lastCacheFlush = now;
            }
        }
    }

    public static User removeUserDetails(String subID) throws Exception
    {
        User user = null;

        user = (User) m_cacheRecent.get(subID);
        if (user != null)
        {
            m_cacheRecent.remove(subID);
            return user;
        }

        user = (User) m_cacheHistory.get(subID);
        if (user != null)
        {
            m_cacheHistory.remove(subID);
            return user;
        }

        return user;
    }

    public static User getUserDetails(String subID) throws Exception
    {
        User user = null;

        user = (User) m_cacheRecent.get(subID);
        if (user != null)
        {
            return user;
        }

        user = (User) m_cacheHistory.get(subID);
        if (user != null)
        {
            m_cacheRecent.put(subID, user);
            m_cacheHistory.remove(subID);
            return user;
        }

        return user;
    }
}