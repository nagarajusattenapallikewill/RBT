/**
 * The CallLogAnalyzer class gets the call logs, parses them and writes the
 * output.
 */

package com.onmobile.apps.ringbacktones.callloganalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;


public class CallLogAnalyzer
{
	private static Logger logger = Logger.getLogger(CallLogAnalyzer.class);
    protected final String CALL_LOG_FILENAME = "LOG";
    protected final String TEXT_OUTPUT_CLASS = "TextWriter";
    protected final String HTML_OUTPUT_CLASS = "HTMLWriter";
    protected final String CMD_ONLYSUMMARY = "-summary";

    protected HashMap m_hmAppInfoMap; // Map of AppInfo objects.Key is appname
                                      // and value is an AppInfo object.

    protected CallLogParser m_Parser; // The call log parser.
    protected ConfigManager m_confMgr; // Configuration Parser.
    protected IOutputWriter m_Writer; // Writes to file.

    protected boolean m_bWriteCallLogDetails; // Whether to output the call log
                                              // analysis details.

    protected boolean m_bFilterCallers; // Whether the caller filter is set.
    protected boolean m_bFilterCalled; // Whether the called number filter is
                                       // set.
    protected boolean m_bFilterApps; // Whether the app filter is set.

    protected String[] m_arrCallers; // List of Numbers for the caller number
                                     // filter.
    protected String[] m_arrCalled; // List of Numbers for the called number
                                    // filter.
    protected String[] m_arrApps; // List of Apps for the app filter.
    protected String[] m_arrCounters; // List of Counter Headers.

    public CallLogAnalyzer()
    {
        m_hmAppInfoMap = new HashMap();
        m_Parser = new CallLogParser();
        m_confMgr = new ConfigManager();
        m_arrCallers = null;
        m_arrApps = null;

        m_bWriteCallLogDetails = true;

        m_bFilterCallers = false;
        m_bFilterCalled = false;
        m_bFilterApps = false;
    }

    // Process all the calllogs.
    public void processCallLogs(String[] args)
    {

        String fileName[] = processCommandLine(args);

        Tools.addToLogFile("Call log Analysis Started");

        if (fileName == null)
        {
            System.out.println("Parameter missing.");
            return;
        }

        if (!m_confMgr.init())
        {
            // Unable to initialise the config parser.
            return;
        }

        // Get the counter Headers.
        if ((m_arrCounters = m_confMgr.getCounters()) == null)
        {
            // Unable to get the list of counters.
            //System.out.println("Unable to get the list of Counters from the
            // config.");
            return;
        }

        // Get the outpult file class from the config.
        String strOutputClass = m_confMgr.getOutputClass();
        if (strOutputClass == null)
        {
            //System.out.println("No output filetype specified.\nThe output
            // will be generated in text format.");
            strOutputClass = TEXT_OUTPUT_CLASS;
        }

        strOutputClass = "com.onmobile.apps.ringbacktones.callloganalyzer."
                + strOutputClass;

        try
        {
            m_Writer = (IOutputWriter) (Class.forName(strOutputClass)
                    .newInstance());
        }
        catch (ClassNotFoundException cnfEx)
        {
            //System.out.println("Output class " + strOutputClass + " not
            // found.");
            return;
        }
        catch (Exception Ex)
        {
            System.out.println(Ex.getMessage());
            return;
        }
        // Initialise the output class.
        if (!m_Writer.init(m_bWriteCallLogDetails))
        {
            return;
        }

        // Initialise the Applications.
        ArrayList _alAppNamesList;
        if ((_alAppNamesList = m_confMgr.getAppList()) == null)
        {
            //System.out.println("No Applications.");
            return;
        }

        for (int i = 0; i < _alAppNamesList.size(); i++)
        {
            String _appName = (String) _alAppNamesList.get(i);
            m_hmAppInfoMap.put(_appName, new AppInfo(_appName));
        }

        initTransactions();

        // Read the Callers Filter.
        if ((m_arrCallers = initFilter(CallLogConsts.FILTER_CALLERS)) != null)
        {
            //System.out.println("CallerFilter On.");
            m_bFilterCallers = true;
        }

        // Read the Called Numbers Filter.
        if ((m_arrCalled = initFilter(CallLogConsts.FILTER_CALLED)) != null)
        {
            //System.out.println("CalledFilter On.");
            m_bFilterCalled = true;
        }

        // Read the Apps filters.
        if ((m_arrApps = initFilter(CallLogConsts.FILTER_APPS)) != null)
        {
            //System.out.println("AppFilter On.");
            m_bFilterApps = true;
        }

        boolean flag = true;
        for (int j = 0; j < fileName.length; j++)
        {
            // Check if the file or directory exists.
            if (!(new File(fileName[j])).exists())
            {
                logger.info("Calllog File " + fileName[j] + " not found!");
                Tools.addToLogFile("Calllog File " + fileName[j]
                        + " not found!");
                continue;
            }
            flag = false;
            // Traverse the directory.
            recTraverseDir(fileName[j]);
        }
        if (flag)
            return;

        m_Writer.close();

        if (m_bWriteCallLogDetails)
        {
            try
            {
                m_Writer = (IOutputWriter) (Class.forName(strOutputClass)
                        .newInstance());
            }
            catch (ClassNotFoundException cnfEx)
            {
                //System.out.println("Output class " + strOutputClass + " not
                // found.");
                return;
            }
            catch (Exception Ex)
            {
                System.out.println(Ex.getMessage());
                return;
            }

            if (!m_Writer.init())
            {
                return;
            }

            // Write the summary.
            writeCallLogSummary();

            // Write Transactions status report.
            writeTransactionsReport();

            m_Writer.close();
        }

        Tools.addToLogFile("Call log Analysis Ended Successfully");
    }

    // Traverse the directory recursively and process call logs.
    public void recTraverseDir(String fileName)
    {
        File file = new File(fileName);
        if (!file.isDirectory())
        {
            if (file.getName().equals(CALL_LOG_FILENAME))
            {
                // Process the file.
                // displayCallLog(file.toString());
                processFile(file.toString());
            }
        }
        else
        {
            //System.out.println("Traversing into " + file.getName());
            String[] dir = file.list();
            for (int i = 0; i < dir.length; i++)
            {
                recTraverseDir(file.toString() + File.separator + dir[i]);
            }
        }
    }

    // To do the processing of one file.
    public void processFile(String fileName)
    {

        //System.out.println("Processing file " + fileName + "...");
        if (fileName == null)
            return;
        if (!m_Parser.parse(fileName))
        {
            //	System.out.println("Error in parsing file: " + fileName);
            return;
        }

        // Check if there are any callstates in the file.
        int numStates = m_Parser.getNumberOfCallstates();
        if (numStates < 1)
        {
            //System.out.println("No CallStates exist.");
            return;
        }

        // Reset all the AppInfo objects. And initilise the call process flag.
        resetApps();

        // Get the caller number from the 1st call state && Filter.
        String strCallerNum = m_Parser
                .getValue(0, CallLogConsts.KEY_CALLER_NUMBER);
        if (m_bFilterCallers && !processCaller(strCallerNum))
            return;

        // Get the caller number from the 1st call state && Filter.
        String strCalledNum = m_Parser
                .getValue(0, CallLogConsts.KEY_CALLED_NUMBER);
        if (m_bFilterCalled && !processCalled(strCalledNum))
            return;

        AppInfo curAppInfo = null;
        String curApp = "";
        String curSOKey = "";
        String prevStatus = "";
        for (int i = 0; i < numStates; i++)
        {
            String soKey = m_Parser.getValue(i, CallLogConsts.KEY_ENT_SOKEY); // Get
                                                                              // the
                                                                              // ENTERING_SOKEY
            if (soKey == null)
                soKey = m_Parser.getValue(i, CallLogConsts.KEY_SOKEY); // Get
                                                                       // the
                                                                       // SOKEY
            if (soKey == null)
                soKey = m_Parser.getValue(i, CallLogConsts.KEY_EXIT_SOKEY); // Get
                                                                            // the
                                                                            // EXITING_SOKEY.
            if (soKey != null)
            {
                curSOKey = soKey;
            }

            String appName = m_confMgr.getAppName(curSOKey);
            if (appName == null)
            {
                appName = curApp;
            }

            boolean processApp = !m_bFilterApps
                    || (m_bFilterApps && checkAppFilter(appName));
            if (!appName.equals(curApp))
            {
                if (curAppInfo != null)
                {
                    String _trName = curAppInfo.anyFailedTrans();
                    if (_trName != null && processApp)
                        m_Writer.writeProblem("Transaction " + _trName
                                + " failed.");
                }

                if (processApp)
                {
                    m_Writer.startFileRecord(fileName, strCallerNum,
                                             strCalledNum);
                    m_Writer.startApplication(appName);
                }
                curApp = appName;
                prevStatus = "";
                curAppInfo = (AppInfo) m_hmAppInfoMap.get(curApp);
            }

            try
            {
                curAppInfo.processTransaction(curSOKey);
            }
            catch (NullPointerException npEx)
            {
                //	System.out.println("Error in Call Log.");
                //m_Writer.writeError("SOKeys not found. Cannot process
                // file.");
                return;
            }

            // Get other Information
            String status = m_Parser.getValue(i, CallLogConsts.KEY_STATUS);
            String nlInt00 = m_Parser.getValue(i, CallLogConsts.KEY_NL_INT_00);
            String conf0 = m_Parser.getValue(i, CallLogConsts.KEY_CONFIDENCE_0);
            String uttFileName = m_Parser
                    .getValue(i, CallLogConsts.UTTERANCE_FILENAME);

            if (uttFileName != null)
            {
                try
                {
                    uttFileName = fileName.substring(0, fileName
                            .lastIndexOf(File.separator))
                            + uttFileName.substring(uttFileName
                                    .lastIndexOf(File.separator));
                }
                catch (IndexOutOfBoundsException iobEx)
                {
                }
            }

            try
            {
                if (CallLogConsts.VAL_STATUS_INTERPRETATION.equals(status))
                {
                    // Status = Interpretation.
                    if ((nlInt00 = m_Parser
                            .getValue(i, CallLogConsts.KEY_NL_SLOT_00)) == null)
                    {
                        // No result... so dont increment the count.
                        status = CallLogConsts.VAL_STATUS_NORESULT;
                    }
                }

                curAppInfo.addInfo(curSOKey, status);

                if (CallLogConsts.VAL_STATUS_UNRESOLVED.equals(status))
                {
                    // Do nothing.
                    continue;
                }

                if (processApp)
                    m_Writer.appendToRecord(curSOKey, status, nlInt00, conf0,
                                            uttFileName);

                // Stop processing if the status is HANGUP
                if (CallLogConsts.VAL_STATUS_HANGUP.equals(status))
                {
                    break;
                }

                // Increment the callrejection count.
                if (CallLogConsts.VAL_STATUS_REJ.equals(status)
                        && !CallLogConsts.VAL_STATUS_REJ.equals(prevStatus))
                {
                    curAppInfo.incrementCallRejections(curSOKey, 1);
                }

                prevStatus = status;
            }
            catch (NullPointerException npEx)
            {
                // Null status.
            }
        }

        boolean processApp = !m_bFilterApps
                || (m_bFilterApps && checkAppFilter(curAppInfo.getAppName()));

        // Check if there are any unfinished transactions in the last
        // application.
        if (curAppInfo != null)
        {
            String _trName = curAppInfo.anyFailedTrans();
            if (_trName != null && processApp)
                m_Writer.writeProblem("Transaction " + _trName + " failed.");
        }

        // If the last state before HANGUP is REJECT, indicate PROBLEM at the
        // end.
        if (CallLogConsts.VAL_STATUS_REJ.equals(prevStatus) && processApp)
            m_Writer.writeProblem(CallLogConsts.VAL_STATUS_REJ
                    + " just before " + CallLogConsts.VAL_STATUS_HANGUP + ".");

        // Check process call flag and write output to file.
        m_Writer.endFileRecord();
    }

    // Process the command line parameters.
    protected String[] processCommandLine(String[] args)
    {
        int str_size = args.length;
        if (args[0].startsWith("-"))
            str_size--;
        String[] fileName = new String[str_size];

        int j = 0;
        int numArgs = args.length;
        if (numArgs != 0)
        {
            for (int i = 0; i < args.length; i++)
            {
                if (!args[i].startsWith("-"))
                    fileName[j++] = args[i];
                else
                {
                    if (args[i].equals(CMD_ONLYSUMMARY))
                        m_bWriteCallLogDetails = false;
                    else
                        System.out
                                .println("Unrecognized parameter: " + args[i]);
                }
            }
        }
        return fileName;
    }

    // Put the transactions into the corresponding apps.
    public void initTransactions()
    {
        ArrayList _alTransactions = m_confMgr.getTransactionsList();
        String[] _trans;
        AppInfo app;
        for (int i = 0; i < _alTransactions.size(); i++)
        {
            _trans = (String[]) _alTransactions.get(i);
            app = (AppInfo) m_hmAppInfoMap.get(_trans[1]);
            if (app != null)
            {
                app.addTransaction(_trans[0], _trans[2], _trans[3]);
            }
        }
    }

    // Return a string array of values for the specified filter.
    public String[] initFilter(String filterName)
    {
        String[] arrValues = null;
        String strValue = m_confMgr.getFilter(filterName);
        if (strValue != null)
        {
            StringTokenizer st = new StringTokenizer(strValue, ",");
            int iCount = st.countTokens();
            if (iCount > 0)
            {
                arrValues = new String[iCount];
                for (int i = 0; i < iCount; i++)
                {
                    arrValues[i] = st.nextToken().trim();
                    //					System.out.println(arrValues[i]);
                }
            }
        }
        return arrValues;
    }

    // To write the summary at the end of the file.
    public void writeCallLogSummary()
    {
        m_Writer.startSummary();
        // Find out the total number of recognitions, rejections, hangups in an
        // app.
        Iterator iterApps = m_hmAppInfoMap.values().iterator();
        int numCounters = m_arrCounters.length;
        AppInfo app;
        String appName;

        int[] arrTotal = new int[numCounters]; // Total across all the
                                               // applications.
        for (int i = 0; i < numCounters; i++)
            arrTotal[i] = 0; // Initialise to 0.

        while (iterApps.hasNext())
        {
            app = (AppInfo) iterApps.next();
            appName = app.getAppName();
            if (!checkAppFilter(appName))
                continue;

            m_Writer.startAppSummary(appName, m_arrCounters);

            int[] arrAppTotal = new int[numCounters]; // Total across all states
                                                      // for an app.
            for (int i = 0; i < numCounters; i++)
                arrAppTotal[i] = 0; // Initialise to 0.

            String[] arrStateNames = app.getStateNames();
            String strStateName;
            String counterVal;

            StringTokenizer st;

            // Process for eac state of an app.
            for (int i = 0; i < app.getNumStates(); i++)
            {

                int[] values = new int[numCounters];
                boolean bWriteFlag = false; // Whether to write that state or
                                            // not.
                strStateName = arrStateNames[i];

                for (int j = 0; j < numCounters; j++)
                {
                    counterVal = m_confMgr.getCounterValue(m_arrCounters[j]);
                    st = new StringTokenizer(counterVal, ",");
                    int iTotal = 0;
                    while (st.hasMoreTokens())
                    {
                        iTotal += app.getInfo(strStateName, st.nextToken()
                                .trim());
                    }
                    values[j] = iTotal;
                    if (iTotal != 0)
                        bWriteFlag = true;

                    // Add to the total for the app.
                    arrAppTotal[j] += iTotal;
                    arrTotal[j] += iTotal;
                }

                if (bWriteFlag)
                    m_Writer.writeStateInfo(strStateName, values);
            }

            m_Writer.writeAppInfo(arrAppTotal);
        }
        m_Writer.doTotal(arrTotal);
    }

    // Write the transactions report.
    public void writeTransactionsReport()
    {
        ArrayList alTrans = m_confMgr.getTransactionsList();
        if (alTrans.isEmpty())
            return;

        m_Writer.startTransactionsReport();

        String[] arrTrans;
        AppInfo app;
        String appName, transName;
        int iNumSuccess = 0, iNumFailures = 0;

        for (int i = 0; i < alTrans.size(); i++)
        {
            arrTrans = (String[]) alTrans.get(i);
            transName = arrTrans[0];
            appName = arrTrans[1];
            if ((app = (AppInfo) m_hmAppInfoMap.get(appName)) != null)
            {
                iNumSuccess = app.getNumCompletedTransactions(transName);
                iNumFailures = app.getNumFailedTransactions(transName);
            }

            if (checkAppFilter(appName))
                m_Writer.addTransactionDetails(transName, appName, iNumSuccess,
                                               iNumFailures);
        }
    }

    // Reset all the applications objects.
    protected void resetApps()
    {
        Iterator iterApps = m_hmAppInfoMap.values().iterator();
        AppInfo _app;
        while (iterApps.hasNext())
        {
            _app = (AppInfo) iterApps.next();
            _app.resetAll();
        }
    }

    // Check whether the caller exists in the filters' list of callers.
    protected boolean processCaller(String callerNum)
    {
        if (!m_bFilterCallers)
            return true;
        if (callerNum == null || m_arrCallers == null)
            return false;
        int iPos;

        for (int i = 0; i < m_arrCallers.length; i++)
        {
            String curPattern = m_arrCallers[i];
            if (callerNum.equals(curPattern))
                return true;
            else
            { // Check for *.
                if ((iPos = curPattern.indexOf("*")) == -1)
                    return false;
                curPattern = curPattern.substring(0, iPos);
                if (!callerNum.equals(curPattern)
                        && callerNum.startsWith(curPattern))
                    return true;
            }
        }
        return false;
    }

    // Check whether the called number exists in the filters' list.
    protected boolean processCalled(String calledNum)
    {
        if (!m_bFilterCalled)
            return true;
        if (calledNum == null || m_arrCalled == null)
            return false;
        int iPos;

        for (int i = 0; i < m_arrCalled.length; i++)
        {
            String curPattern = m_arrCalled[i];
            if (curPattern.equals(calledNum))
                return true;
            else
            { // Check for *.
                if ((iPos = curPattern.indexOf("*")) == -1)
                    return false;
                curPattern = curPattern.substring(0, iPos);
                if (!calledNum.equals(curPattern)
                        && calledNum.startsWith(curPattern))
                    return true;
            }
        }
        return false;
    }

    // Check whether the app exists in the filters' list of applications.
    protected boolean checkAppFilter(String appName)
    {
        if (appName == null || m_arrApps == null)
            return false;
        for (int i = 0; i < m_arrApps.length; i++)
        {
            if (appName.equals(m_arrApps[i]))
            {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException
    {
        CallLogAnalyzer cla = new CallLogAnalyzer();
        cla.processCallLogs(args);
    }
}