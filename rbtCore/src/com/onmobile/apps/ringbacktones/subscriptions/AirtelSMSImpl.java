package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

class AirtelSMSImpl implements SMSProcessInterface
{
    private static AirtelSMSImpl m_airtelSMSImpl = null;
    private static Object m_lock = new Object();
    ArrayList m_rbtKeyword = null;
    ArrayList m_rbtRequestKeyword = null;
    ArrayList m_rbtSerachOnKeyword = null;
    RBTDBManager rbtDBManager = null;

    public Object getInstance() throws Exception
    {
        if (m_airtelSMSImpl != null)
            return m_airtelSMSImpl;

        synchronized (m_lock)
        {
            if (m_airtelSMSImpl != null)
                return m_airtelSMSImpl;

            m_airtelSMSImpl = new AirtelSMSImpl();
            m_airtelSMSImpl.init();
        }

        return m_airtelSMSImpl;
    }

    private void init() throws Exception
    {
        rbtDBManager = RBTDBManager.getInstance();
        
        m_rbtKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RBT_KEYWORD", null), null);
        m_rbtRequestKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_RBT_KEYWORD", null), null);
        m_rbtSerachOnKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_SEARCH_ON", null), null);
    }

    public ArrayList preProcess(HashMap z,ArrayList sms)
    {
        if(sms == null || sms.size() <= 0)
        	return sms;
        if (!isRBTKeywordPresent(sms))
        {
           sms.add(m_rbtKeyword.get(0));
        }
        boolean isRequest = false;
        boolean isRequestOn = false;
        String tokenToRemove1 = null;
        String tokenToRemove2 = null;
        String requestOn = "song";
        for(int i = 0; i < sms.size(); i++)
        {
        	String smsToken = ((String)sms.get(i)).trim();
        	if(m_rbtRequestKeyword != null && m_rbtRequestKeyword.contains(smsToken))
        	{
        		tokenToRemove1 = smsToken;
        		isRequest = true;
        		continue;
        	}
        	if(m_rbtSerachOnKeyword != null && m_rbtSerachOnKeyword.contains(smsToken))
        	{
        		tokenToRemove2 = smsToken;
        		requestOn = smsToken;
        		isRequestOn = true;
        		continue;
        	}
        }	
        if(isRequest || isRequestOn)
        {
        	if(tokenToRemove1 != null)
        		sms.remove(tokenToRemove1);
        	if(tokenToRemove2 != null)
        		sms.remove(tokenToRemove2);
        	
        	ArrayList smsTemp = new ArrayList();
        	smsTemp.add(requestOn);
        	smsTemp.add(m_rbtRequestKeyword.get(0));
        	
        	for(int i = 0; i < sms.size(); i++)
        		smsTemp.add(sms.get(i));
        	sms = smsTemp;
        }	
        return sms;
    }

    private boolean isRBTKeywordPresent(ArrayList sms)
    {
        if (sms != null && sms.size() > 0)
        {
            for (int i = 0; i < sms.size(); i++)
            {
                if (m_rbtKeyword.contains(sms.get(i)))
                    return true;
            }
        }
        return false;
    }
    
}
