package com.onmobile.apps.ringbacktones.web;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class ReadResource
{
    public static Logger m_logger = Logger.getLogger("RBTWAP");;
    private ResourceBundle m_bundle = null;
    private String m_resource = "resources/RBTWap";
    private static ReadResource m_readResource = null;

    private ReadResource() {
        try
        {
            m_bundle = ResourceBundle
                    .getBundle(m_resource, Locale.getDefault());
        }
        catch (Exception e)
        {
            m_logger.error("RBT::Bundle couldnot be created and hence returning");
        }
    }
    
    public static ReadResource getInstance()
    {
        if (m_readResource == null)
        {
        	synchronized (ReadResource.class) {
                if (m_readResource == null) {
                	m_readResource = new ReadResource();
                }
			}
        }

        return m_readResource;
    }

    public String getLangText(String text, String lang)
    {
        String temp = m_bundle.getString(text + "_" + lang);;

        if (temp == null)
        {
            m_logger.warn("RBT::" + text + "_" + lang + " missing in bundle");
        }

        return temp;
    }

    public String getModifiedText(String param, String lang, String str1,
            String str2)
    {
        String temp = m_bundle.getString(param + "_" + lang);;

        if (temp == null)
        {
            m_logger.warn("RBT::" + param + "_" + lang + " missing in bundle");
            return temp;
        }

        while (temp.indexOf("%1") != -1)
        {
            temp = temp.substring(0, temp.indexOf("%1")) + str1
                    + temp.substring(temp.indexOf("%1") + 2);
        }
        while (temp.indexOf("%2") != -1)
        {
            temp = temp.substring(0, temp.indexOf("%2")) + str2
                    + temp.substring(temp.indexOf("%2") + 2);
        }

        return temp;
    }

}