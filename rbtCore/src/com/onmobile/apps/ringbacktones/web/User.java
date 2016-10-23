package com.onmobile.apps.ringbacktones.web;

import java.util.ArrayList;
import java.util.Hashtable;

public class User
{
    String m_subID;
    String m_site;
    String m_url;
    Hashtable m_selections;
    ArrayList m_callers;
    String m_categoryID;
    String m_categoryName;
    String m_clipName;
    String m_clipWav;
    String m_caller;
    String m_lang;
    Hashtable m_search;

    public User(String subID, String site, String url, Hashtable selections,
            ArrayList callers, String categoryID, String categoryName,
            String clipName, String clipWav, String caller, String lang)
    {
        m_subID = subID;
        m_site = site;
        m_url = url;
        m_selections = selections;
        m_callers = callers;
        m_categoryID = categoryID;
        m_categoryName = categoryName;
        m_clipName = clipName;
        m_clipWav = clipWav;
        m_caller = caller;
        m_lang = lang;
    }

    public User(String subID, String site, String url, Hashtable selections,
            ArrayList callers, String categoryID, String categoryName,
            String clipName, String clipWav, String caller, String lang,
            Hashtable search)
    {
        m_subID = subID;
        m_site = site;
        m_url = url;
        m_selections = selections;
        m_callers = callers;
        m_categoryID = categoryID;
        m_categoryName = categoryName;
        m_clipName = clipName;
        m_clipWav = clipWav;
        m_caller = caller;
        m_lang = lang;
        m_search = search;
    }

    public void setSearch(Hashtable search)
    {
        m_search = search;
    }

    public void setSelections(Hashtable selections)
    {
        m_selections = selections;
    }

    public void setSite(String site)
    {
        m_site = site;
    }

    public void setURL(String url)
    {
        m_url = url;
    }

    public void setCallers(ArrayList callers)
    {
        m_callers = callers;
    }

    public void setLanguage(String lang)
    {
        m_lang = lang;
    }

    public void setCategoryID(String categoryID)
    {
        m_categoryID = categoryID;
    }

    public void setCategoryName(String categoryName)
    {
        m_categoryName = categoryName;
    }

    public void setClipName(String clipName)
    {
        m_clipName = clipName;
    }

    public void setClipWav(String clipWav)
    {
        m_clipWav = clipWav;
    }

    public void setCaller(String caller)
    {
        m_caller = caller;
    }

    public String getSubID()
    {
        return m_subID;
    }

    public String getSite()
    {
        return m_site;
    }

    public String getURL()
    {
        return m_url;
    }

    public Hashtable getSelections()
    {
        return m_selections;
    }

    public ArrayList getCallers()
    {
        return m_callers;
    }

    public String getLanguage()
    {
        return m_lang;
    }

    public String getCategoryID()
    {
        return m_categoryID;
    }

    public String getCategoryName()
    {
        return m_categoryName;
    }

    public String getClipName()
    {
        return m_clipName;

    }

    public String getClipWav()
    {
        return m_clipWav;
    }

    public String getCaller()
    {
        return m_caller;
    }

    public Hashtable getSearch()
    {
        return m_search;
    }
}