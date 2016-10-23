/**
 * Writes the output to file in text format.
 */

package com.onmobile.apps.ringbacktones.callloganalyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TextWriter implements IOutputWriter
{

    protected final String OUTPUT_FILE = "CallTrace.txt";
    protected final String SUMMARY_FILE = "CallTrace-summary.txt";
    protected final int ALIGN_LEFT = 0;
    protected final int ALIGN_RIGHT = 1;
    protected int m_iLineLength;

    protected boolean m_bWriteCallLogDetails; // Whether to output the call log
    // analysis details.
    protected ArrayList m_alOutputBuffer; // The output buffer.
    protected PrintWriter pw;

    public TextWriter()
    {
        m_alOutputBuffer = new ArrayList();
    }

    // Initialise the file.
    public boolean init(boolean writeCallLogSummary)
    {
        try
        {
            pw = new PrintWriter(new FileWriter(new File(OUTPUT_FILE)), true);
        }
        catch (IOException ioEx)
        {
            System.out.println("IOException while opening the file "
                    + OUTPUT_FILE);
            return false;
        }
        m_bWriteCallLogDetails = writeCallLogSummary;
        beginCallLogReport();
        return true;
    }

    public boolean init()
    {
        try
        {
            pw = new PrintWriter(new FileWriter(new File(SUMMARY_FILE)), true);
        }
        catch (IOException ioEx)
        {
            System.out.println("IOException while opening the file "
                    + OUTPUT_FILE);
            return false;
        }

        m_bWriteCallLogDetails = false;
        // Write the startting tags for the HTML File.
        beginCallLogReport();
        return true;
    }

    // To write the headers.
    public void beginCallLogReport()
    {
        pw.println("Call Log Analysis Report");
        pw.println("------------------------");
    }

    // To start the record for a log file.
    public void startFileRecord(String fileName, String callerNum,
            String calledNum)
    {
        if (!m_bWriteCallLogDetails)
            return;
        // Clear the output buffer and write to the buffer.
        String strDirName = fileName.substring(0, fileName
                .lastIndexOf(File.separator));
        m_alOutputBuffer.clear();
        m_alOutputBuffer
                .add(callerNum + " : " + calledNum + " : " + strDirName);
    }

    // To start the record for an application.
    public void startApplication(String appName)
    {
        if (!m_bWriteCallLogDetails)
            return;
        m_alOutputBuffer.add("\t" + appName);
    }

    // To append a status record.
    public void appendToRecord(String soKey, String status, String nlInt00,
            String confidence0, String uttFileName)
    {
        if (!m_bWriteCallLogDetails)
            return;
        // Format the nlInt00 string.
        try
        {
            nlInt00 = nlInt00.substring(nlInt00.indexOf("{") + 1, nlInt00
                    .indexOf("}"));
        }
        catch (IndexOutOfBoundsException iobEx)
        {
            // Error in string format.
        }
        catch (NullPointerException npEx)
        {
            // No recognition.
        }

        String out = " :" + soKey + " [";
        if (nlInt00 != null)
            out += nlInt00;
        else if (CallLogConsts.VAL_STATUS_INTERPRETATION.equals(status))
            out += "<<No Result>>";
        else
            out += status;
        if (confidence0 != null)
            out += ":" + confidence0 + "%";
        out += "]";

        try
        {
            int iIndex = m_alOutputBuffer.size() - 1;
            String strLine = (String) m_alOutputBuffer.remove(iIndex);
            strLine += out;
            m_alOutputBuffer.add(strLine);
        }
        catch (IndexOutOfBoundsException iobEx)
        {
            System.out.println(iobEx.getMessage());
        }
    }

    // Indicate problem with a call.
    public void writeProblem(String problem)
    {
        if (!m_bWriteCallLogDetails)
            return;
        m_alOutputBuffer.add("* (PROBLEM : " + problem + ")");
    }

    // Indicate error in a call log.
    public void writeError(String error)
    {
        if (!m_bWriteCallLogDetails)
            return;
        for (int i = 0; i < m_alOutputBuffer.size(); i++)
            pw.println(m_alOutputBuffer.get(i));

        pw.println("* (ERROR : " + error + ")");
    }

    // To end one log file.
    public void endFileRecord()
    {
        if (!m_bWriteCallLogDetails)
            return;
        for (int i = 0; i < m_alOutputBuffer.size(); i++)
            pw.println(m_alOutputBuffer.get(i));
    }

    // To write the headers for summary.
    public void startSummary()
    {
        pw.println();
        pw.println("Summary");
        pw.println("-------");
    }

    // To write a line into the file.
    public void startAppSummary(String strAppName, String[] strCounters)
    {
        m_iLineLength = 45;
        formatAndAppend("Application", 20, ALIGN_LEFT);
        formatAndAppend("State", 25, ALIGN_LEFT);
        for (int i = 0; i < strCounters.length; i++)
        {
            formatAndAppend(strCounters[i], 10, ALIGN_RIGHT);
            m_iLineLength += 10;
        }
        pw.println();
        drawLine("-", m_iLineLength);
        pw.println(strAppName);
    }

    /*
     * Formats the input into strings of width atleast the length specified, by
     * padding with spaces, left or right as specified.
     */
    public void formatAndAppend(String data, int length, int align)
    {
        if (data == null)
            data = "";
        int iDataLength = data.length();
        if (iDataLength >= length)
        {
            pw.print(data);
            return;
        }
        // Pad with spaces.
        String spacer = "";
        int iDiff = length - iDataLength;
        for (int i = 0; i < iDiff; i++)
            spacer += " ";
        if (align == 1)
            data = spacer + data;
        else
            data = data + spacer;

        pw.print(data);
    }

    // To draw a line of the given string in a specified length.
    public void drawLine(String str, int length)
    {
        String strLine = "";
        for (int i = 0; i < length; i++)
            strLine += str;
        pw.println(strLine);
    }

    // To write the details for an application.
    public void writeAppInfo(int[] values)
    {
        drawLine("-", m_iLineLength);
        formatAndAppend("", 45, ALIGN_LEFT);
        for (int i = 0; i < values.length; i++)
            formatAndAppend(Integer.toString(values[i]), 10, ALIGN_RIGHT);

        pw.println();
        drawLine("=", m_iLineLength);
    }

    // To write the details for a state.
    public void writeStateInfo(String state, int[] values)
    {
        formatAndAppend("", 20, ALIGN_LEFT);
        formatAndAppend(state, 25, ALIGN_LEFT);
        for (int i = 0; i < values.length; i++)
            formatAndAppend(Integer.toString(values[i]), 10, ALIGN_RIGHT);

        pw.println();
    }

    // Write the total across all the applications.
    public void doTotal(int[] values)
    {
        formatAndAppend("Total (all Applications)", 45, ALIGN_LEFT);
        for (int i = 0; i < values.length; i++)
            formatAndAppend(Integer.toString(values[i]), 10, ALIGN_RIGHT);
        pw.println();
        drawLine("=", m_iLineLength);
    }

    /**
     * Transactions Report Methods
     */

    public void startTransactionsReport()
    {
        pw.println();
        pw.println("Transaction Status");
        pw.println("------------------");
        formatAndAppend("Transaction", 20, ALIGN_LEFT);
        formatAndAppend("Application", 20, ALIGN_LEFT);
        formatAndAppend("Success", 10, ALIGN_RIGHT);
        formatAndAppend("Failures", 10, ALIGN_RIGHT);
        pw.println();
        drawLine("-", 60);
    }

    public void addTransactionDetails(String transName, String appName,
            int numSuccess, int numFailures)
    {
        formatAndAppend(transName, 20, ALIGN_LEFT);
        formatAndAppend(appName, 20, ALIGN_LEFT);
        formatAndAppend(Integer.toString(numSuccess), 10, ALIGN_RIGHT);
        formatAndAppend(Integer.toString(numFailures), 10, ALIGN_RIGHT);
        pw.println();
    }

    // Close the file.
    public void close()
    {
        pw.close();
    }
}