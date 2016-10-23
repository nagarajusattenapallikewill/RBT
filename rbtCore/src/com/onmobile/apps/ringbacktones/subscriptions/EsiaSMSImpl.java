package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ChargePromoTypeMap;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

class EsiaSMSImpl implements SMSProcessInterface
{
    private static EsiaSMSImpl m_esiaSMSImpl = null;
    private static Object m_lock = new Object();
    private String m_xlKeyword = null;
    private ArrayList m_catRBTkeyword = null;
    ArrayList m_rbtKeyword = null;
    ArrayList promoSmsKeywords = new ArrayList();
    RBTDBManager rbtDBManager = null;

    public Object getInstance() throws Exception
    {
        if (m_esiaSMSImpl != null)
            return m_esiaSMSImpl;

        synchronized (m_lock)
        {
            if (m_esiaSMSImpl != null)
                return m_esiaSMSImpl;

            m_esiaSMSImpl = new EsiaSMSImpl();
            m_esiaSMSImpl.init();
        }

        return m_esiaSMSImpl;
    }

    private void init() throws Exception
    {
        rbtDBManager = RBTDBManager.getInstance();
        ChargePromoTypeMap[] ccMap = rbtDBManager.getChargePromoTypeMaps();
        if (ccMap != null && ccMap.length > 0)
        {
            for (int i = 0; i < ccMap.length; i++)
            {
                if (ccMap[i].level() == 1)
                    promoSmsKeywords.add(ccMap[i].smsKeyword());
                if (ccMap[i].promoType().equals("XLCOM"))
                    m_xlKeyword = (String) Tools
                            .tokenizeArrayList(ccMap[i].smsKeyword(), null)
                            .get(0);
            }
        }

        m_rbtKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RBT_KEYWORD", null), null);
        m_catRBTkeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "CATEGORY_SEARCH_KEYWORD", null), null);
    }

    public ArrayList preProcess(HashMap z,ArrayList sms)
    {
        if (!isRBTKeywordPresent(sms))
        {
            boolean promoTypeFound = false;
            for (int i = 0; i < promoSmsKeywords.size(); i++)
            {
                String smsToken = null;
                ArrayList smsKeywordList = Tools
                        .tokenizeArrayList((String) promoSmsKeywords.get(i),
                                           null);
                if (smsKeywordList == null)
                    continue;

                for (int j = 0; j < sms.size(); j++)
                {
                    smsToken = (String) sms.get(j);
                    if (smsKeywordList.contains(smsToken))
                    {
                        promoTypeFound = true;
                        break;
                    }
                }
            }
            if (!promoTypeFound)
                sms.add(m_xlKeyword);
            sms.add(m_rbtKeyword.get(0));
        }
        if(sms.contains("top5") && m_catRBTkeyword != null && m_catRBTkeyword.size() > 0) 
            sms.add((String)m_catRBTkeyword.get(0)); 
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