/**
 * Call log Parser.
 */

package com.onmobile.apps.ringbacktones.callloganalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

public class CallLogParser
{
    protected final String CALLSTATE_START_TAG = "start{";
    protected final String CALLSTATE_END_TAG = "}end";

    protected Vector m_vCallStates;

    public CallLogParser()
    {
        m_vCallStates = new Vector();
    }

    // Parse the file and keep the callstates in the vector.
    public boolean parse(String fileName)
    {
        File f = new File(fileName);
        m_vCallStates.clear();
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException fnfEx)
        {
            System.out.println("File not found - " + fileName);
            return false;
        }

        HashMap hmRecords;
        String strLine;

        try
        {
            while ((strLine = br.readLine()) != null)
            {

                // Skip till a callstate start tag is found.
                if (!CALLSTATE_START_TAG.equals(strLine))
                    continue;

                // Parse the records and put into the HashMap.
                hmRecords = new HashMap();

                while ((strLine = br.readLine()) != null
                        && !CALLSTATE_END_TAG.equals(strLine))
                {
                    String key, value;
                    int iPos;

                    try
                    {
                        // Get the key name.
                        strLine = strLine.trim();
                        iPos = strLine.indexOf(" ");
                        key = strLine.substring(0, iPos);

                        // Get the value String.
                        iPos = strLine.indexOf("=");
                        strLine = strLine.substring(iPos + 1).trim();
                        value = strLine;

                        // Put key value pair into the hashmap.
                        hmRecords.put(key, value);
                    }
                    catch (IndexOutOfBoundsException iobEx)
                    {
                        System.out.println(iobEx.getMessage());
                    }
                }

                // Add the hashmap to the vector.
                m_vCallStates.add(hmRecords);
            }
        }
        catch (IOException ioEx)
        {
            System.out.println("IOException occurred.\n" + ioEx.getMessage());
        }
        return true;
    }

    // To get the number of callstates.
    public int getNumberOfCallstates()
    {
        return m_vCallStates.size();
    }

    // To get the value for a key, for a callstate.
    public String getValue(int callstateID, String key)
    {
        HashMap hmRecords = null;

        try
        {
            hmRecords = (HashMap) m_vCallStates.get(callstateID);
        }
        catch (ArrayIndexOutOfBoundsException arrEx)
        {
            System.out.println("Invalid callstate ID.");
            return null;
        }

        return (String) hmRecords.get(key);
    }
}