package com.onmobile.apps.ringbacktones.callloganalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * 
 * The ocnfiguration Parser.
 *  
 */
public class ConfigManager
{
    protected final String CONFIG_FILENAME = "config.txt";
    protected final String KEY_APP = "Apps";
    protected final String KEY_TRANS = "Transactions";
    protected final String KEY_FILTERS = "Filters";
    protected final String KEY_OUTPUT = "Output";
    protected final String KEY_COUNTERS = "Counters";

    protected HashMap m_hmApps; // Applications map
    protected HashMap m_hmFilters; // Filters map
    protected ArrayList m_alCounters; // Counters map.

    protected ArrayList m_alTransactions;
    protected String m_strOutputClass;

    protected class Counter
    {
        protected String m_strName; // CounterName.
        protected String m_strValue; // CouterValue;

        public Counter(String name, String value)
        {
            m_strName = name;
            m_strValue = value;
        }

        public String getName()
        {
            return m_strName;
        }

        public String getValue()
        {
            return m_strValue;
        }
    }

    public ConfigManager()
    {
        m_hmApps = new HashMap();
        m_hmFilters = new HashMap();
        m_alCounters = new ArrayList();
        m_alTransactions = new ArrayList();
    }

    // Parse the config file.
    public boolean init()
    {
        BufferedReader br = null;
        File f = new File(CONFIG_FILENAME);
        try
        {
            br = new BufferedReader(new FileReader(f));
        }
        catch (IOException ioEx)
        {
            System.out.println(ioEx.getMessage());
            return false;
        }

        String strItem = "";
        String strCurConfigItem = null;
        String strLine;

        try
        {
            while ((strLine = br.readLine()) != null)
            {
                strLine = strLine.trim();
                // Ignore comments.
                if (strLine.startsWith("#"))
                    continue;

                if (strLine.startsWith("[") && strLine.endsWith("]"))
                {
                    // New config item.
                    strItem = strLine.substring(1, strLine.length() - 1);
                }
                else
                {
                    strLine = strLine.trim();
                    if (strLine.equals(""))
                        continue;
                    if (strItem.equals(KEY_APP))
                        readApps(strLine);
                    else if (strItem.equals(KEY_TRANS))
                        readTransactions(strLine);
                    else if (strItem.equals(KEY_FILTERS))
                        readFilters(strLine);
                    else if (strItem.equals(KEY_OUTPUT))
                        readOutputClass(strLine);
                    else if (strItem.equals(KEY_COUNTERS))
                        readCounters(strLine);
                    else
                    {
                        System.out
                                .println("Unrecognized item in config file - "
                                        + strItem);
                    }
                }
            }
        }
        catch (IOException ioEx)
        {
            System.out.println(ioEx.getMessage());
            return false;
        }
        catch (NullPointerException npEx)
        {
            System.out.println("NullPointerException. Error in config file.");
            return false;
        }

        return true;
    }

    // Read the apps.
    public void readApps(String strLine)
    {
        int iPos = strLine.indexOf("=");
        try
        {
            String appName = strLine.substring(0, iPos).trim();
            String val = strLine.substring(iPos + 1).trim();
            m_hmApps.put(val, appName);
        }
        catch (IndexOutOfBoundsException iobEx)
        {
            // Not matching to format.
        }
    }

    // Read the transactions.
    public void readTransactions(String strLine)
    {
        String[] trans = new String[4];
        int iPos = strLine.indexOf("=");
        try
        {
            trans[0] = strLine.substring(0, iPos).trim();
            String _val = strLine.substring(iPos + 1);
            StringTokenizer st = new StringTokenizer(_val, ",");
            for (int i = 1; i < 4; i++)
            {
                trans[i] = st.nextToken().trim();
            }

            m_alTransactions.add(trans);
        }
        catch (IndexOutOfBoundsException iobEx)
        {
        }
        catch (NoSuchElementException nseEx)
        {
            System.out.println("Error in Syntax for transaction: " + strLine);
        }
    }

    // Read the filters.
    public void readFilters(String strLine)
    {

        int iPos = strLine.indexOf("=");
        try
        {
            String filterName = strLine.substring(0, iPos).trim();
            String filterVal = strLine.substring(iPos + 1).trim();
            m_hmFilters.put(filterName, filterVal);
        }
        catch (IndexOutOfBoundsException iobEx)
        {
        }
    }

    // Get the output format
    public void readOutputClass(String strLine)
    {
        m_strOutputClass = strLine;
    }

    // Read the counters from the config.
    public void readCounters(String strLine)
    {
        int iPos = strLine.indexOf("=");
        try
        {
            String counterName = strLine.substring(0, iPos).trim();
            String counterVal = strLine.substring(iPos + 1).trim();
            m_alCounters.add(new Counter(counterName, counterVal));
        }
        catch (IndexOutOfBoundsException ioEx)
        {
        }
    }

    // Get the counter Names.
    public String[] getCounters()
    {
        if (m_alCounters.isEmpty())
            return null;
        String[] counterNames = new String[m_alCounters.size()];
        for (int i = 0; i < m_alCounters.size(); i++)
            counterNames[i] = ((Counter) m_alCounters.get(i)).getName();

        return counterNames;
    }

    // Get the value for a particular counter.
    public String getCounterValue(String counter)
    {
        Counter _cnt;
        for (int i = 0; i < m_alCounters.size(); i++)
        {
            _cnt = (Counter) m_alCounters.get(i);
            if (counter.equals(_cnt.getName()))
                return _cnt.getValue();
        }

        return null;
    }

    // Get the application Name for a key.
    public String getAppName(String key)
    {
        return (String) m_hmApps.get(key);
    }

    // Return an arrayList of Application Names.
    public ArrayList getAppList()
    {
        if (m_hmApps == null)
        {
            // No applications, or not initialised.
            return null;
        }

        Iterator iterApps = m_hmApps.values().iterator();
        ArrayList _alAppList = new ArrayList();
        while (iterApps.hasNext())
        {
            _alAppList.add(iterApps.next());
        }

        return _alAppList;
    }

    // Return an arrayList of Transactions.
    public ArrayList getTransactionsList()
    {
        return m_alTransactions;
    }

    // Return a filter.
    public String getFilter(String key)
    {
        return (String) m_hmFilters.get(key);
    }

    // Get Output format.
    public String getOutputClass()
    {
        return m_strOutputClass;
    }

    // Display the key value pairs of applications.
    protected void displayAppsMap()
    {
        System.out.println("Applications");
        Iterator iter = m_hmApps.keySet().iterator();
        while (iter.hasNext())
        {
            String key = (String) iter.next();
            System.out.println(key + "\t= " + m_hmApps.get(key));
        }
    }

    // Display transactions.
    protected void displayTransactions()
    {
        System.out.println("Name\tApplication\tBeginState\tEndState");

        for (int i = 0; i < m_alTransactions.size(); i++)
        {
            String[] trans = (String[]) m_alTransactions.get(i);
            System.out.println(trans[0] + "\t" + trans[1] + "\t" + trans[2]);
        }
    }

    // Display filters.
    protected void displayFilters()
    {
        System.out.println("Filters");
        Iterator iter = m_hmFilters.keySet().iterator();
        while (iter.hasNext())
        {
            String filterName = (String) iter.next();
            System.out.println(filterName + "\t= "
                    + m_hmFilters.get(filterName));
        }
    }
}