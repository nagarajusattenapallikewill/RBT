package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

class BSNLSMSImpl implements SMSProcessInterface
{
    private static BSNLSMSImpl m_bsnlSMSImpl = null;
    private static Object m_lock = new Object();
    private String m_countryPrefix = "91";
    ArrayList m_subMsg = null;
    RBTDBManager rbtDBManager = null;
    ArrayList m_rbtKeyword = null;
    
    public Object getInstance() throws Exception
    {
        if (m_bsnlSMSImpl != null)
            return m_bsnlSMSImpl;

        synchronized (m_lock)
        {
            if (m_bsnlSMSImpl != null)
                return m_bsnlSMSImpl;

            m_bsnlSMSImpl = new BSNLSMSImpl();
            m_bsnlSMSImpl.init();
        }

        return m_bsnlSMSImpl;
    }

    private void init() throws Exception
    {
        m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
        rbtDBManager = RBTDBManager.getInstance();
        
        m_subMsg = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "ACTIVATION_KEYWORD", null), null);
        m_rbtKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RBT_KEYWORD", null), null);
        
    }

    public ArrayList preProcess(HashMap z, ArrayList sms)
    {
        if(sms == null || sms.size() <= 0)
        	return sms;
        Subscriber subscriber = null;
        
        isThisFeature(sms, m_rbtKeyword);
        if(z!= null && z.containsKey("SUBSCRIBER_OBJ"))
        	subscriber = (Subscriber)z.get("SUBSCRIBER_OBJ");
        if(subscriber != null && rbtDBManager.isSubActive(subscriber) && sms.size() > 1) 
        	isActivationRequest(sms);
        sms.add(m_rbtKeyword.get(0));
        return sms;
    }

    private boolean isActivationRequest(ArrayList sms)
    {
    	return isThisFeature(sms, m_subMsg);
    }
    public boolean isThisFeature(ArrayList strList, ArrayList featureKeywords)
    {
        //String _method = "isThisFeature()";
        ////Tools.logDetail(_class, _method, "****** parameters are --
        // "+strList + " & "+featureKeywords );
        if (strList == null || featureKeywords == null)
            return false;
        String token = null;
        for (int i = 0; i < strList.size(); i++)
        {
            token = (String) strList.get(i);
            if (featureKeywords.contains(token))
            {
                strList.remove(token);
                return true;
            }
        }

        return false;
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
