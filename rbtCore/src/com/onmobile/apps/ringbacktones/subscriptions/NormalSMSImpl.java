package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.HashMap;

class NormalSMSImpl implements SMSProcessInterface
{
    private static NormalSMSImpl m_normalSMSImpl = null;
    private static Object m_lock = new Object();

    public Object getInstance() throws Exception
    {
        if (m_normalSMSImpl != null)
            return m_normalSMSImpl;

        synchronized (m_lock)
        {
            if (m_normalSMSImpl != null)
                return m_normalSMSImpl;

            m_normalSMSImpl = new NormalSMSImpl();
        }

        return m_normalSMSImpl;
    }

    public ArrayList preProcess(HashMap z, ArrayList sms)
    {
        return (sms);
    }

}