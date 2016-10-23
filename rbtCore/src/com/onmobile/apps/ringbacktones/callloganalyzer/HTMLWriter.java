/**
 * Writes the output in HTML Format.
 */

package com.onmobile.apps.ringbacktones.callloganalyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class HTMLWriter implements IOutputWriter
{
    protected final String OUTPUT_FILE = "CallTrace.htm";
    protected final String SUMMARY_FILE = "CallTrace-summary.htm";
    protected final String HTML_TITLE = "Call Log Analysis Report for Caller Tunes";
    protected boolean m_bWriteCallLogDetails; // Whether to output the call log
    // analysis details.
    protected ArrayList m_alOutputBuffer; // the output buffer.
    protected PrintWriter pw;

    public HTMLWriter()
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
        // Write the startting tags for the HTML File.
        beginDocument();
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
        beginDocument();
        return true;
    }

    // To start the record for a log file.
    public void startFileRecord(String fileName, String callerNum,
            String calledNum)
    {
        // Clear the output buffer and write to the buffer.
        if (!m_bWriteCallLogDetails)
            return;
        m_alOutputBuffer.clear();
        fileName = getFileURL(fileName, fileName);
        String out = "<TR class=blue><TD width=5%>" + callerNum
                + "</TD><TD width=10%>" + calledNum + "</TD><TD>" + fileName
                + "";
        m_alOutputBuffer.add(out);
    }

    // To start the record for an application.
    public void startApplication(String appName)
    {
        if (!m_bWriteCallLogDetails)
            return;
        m_alOutputBuffer
                .add("</TD></TR> <TR><TD>&nbsp;</TD><TD valign=\"top\">"
                        + appName + "</TD><TD>");
    }

    // To append a status record.
    public void appendToRecord(String soKey, String status, String nlInt00,
            String confidence0, String uttFileName)
    {
        // Format the nlInt00 string.
        if (!m_bWriteCallLogDetails)
            return;
        try
        {
            nlInt00 = nlInt00.substring(nlInt00.indexOf("<") + 1, nlInt00
                    .indexOf(">"));
        }
        catch (IndexOutOfBoundsException iobEx)
        {
            // Error in string format.
        }
        catch (NullPointerException npEx)
        {
            // No recognition.
        }

        String out = soKey + "<font color=navy> [";
        if (nlInt00 != null)
            out += "&lt;" + nlInt00 + "&gt;";
        else if (status.equals(CallLogConsts.VAL_STATUS_NORESULT))
            out += "&lt;&lt;" + CallLogConsts.VAL_STATUS_NORESULT + "&gt;&gt;";
        else
            out += status;
        if (confidence0 != null)
        {
            if (uttFileName != null)
                out += ":" + getFileURL(uttFileName, confidence0) + "%";
            else
                out += ":" + confidence0 + "%";
        }
        out += "] </font> ";

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

    // Indicate a problem with a call.
    public void writeProblem(String problem)
    {
        if (!m_bWriteCallLogDetails)
            return;
        m_alOutputBuffer
                .add("</TD></TR><TR class=yellow><TD><font color=red>&nbsp;</font></TD><TD><font color=red>(PROBLEM)</font></TD><TD><font color=red>"
                        + problem + "</font>");
    }

    // Indicate an error with a call log.
    public void writeError(String error)
    {
        if (!m_bWriteCallLogDetails)
            return;
        for (int i = 0; i < m_alOutputBuffer.size(); i++)
            pw.println(m_alOutputBuffer.get(i));

        pw
                .println("</TD></TR><TR class=yellow><TD><font color=red>&nbsp;</font></TD><TD><font color=red>(ERROR)</font></TD><TD><font color=red>"
                        + error + "</font>");
    }

    // To end one log file.
    public void endFileRecord()
    {
        if (!m_bWriteCallLogDetails)
            return;
        for (int i = 0; i < m_alOutputBuffer.size(); i++)
            pw.println(m_alOutputBuffer.get(i));

        // End the last row.
        pw.println("</TD></TR>");
    }

    /**
     * Summart Reporting methods
     */

    // To write The headers for summary.
    public void startSummary()
    {
        pw.println("</TABLE><br><div align=\"center\">");
        if (m_bWriteCallLogDetails)
            pw.print(getAnchorURL("Top", "Goto Top") + " | ");
        pw
                .println(getAnchorURL("transaction", "Transaction Status")
                        + "</div>");
        pw
                .println("<div align=\"center\"><H3><A name=\"Summary\">Summary</A></H3></div>");
        pw.println("<TABLE width=80% align=\"center\" border=1>");
    }

    // To write a line into the file.
    public void startAppSummary(String strAppName, String[] counters)
    {
        pw
                .print("<TR class=green><TD width=20%>Application</TD><TD width=20%>State</TD>");
        int iCellWidth = 60 / counters.length;
        for (int i = 0; i < counters.length; i++)
            pw.print("<TD align=\"right\" width=" + iCellWidth + "%>"
                    + counters[i] + "</TD>");

        pw.println("</TR><TR><TD colspan=6>" + strAppName + "</TD></TR>");
    }

    // To write The details for an application.
    public void writeAppInfo(int[] values)
    {
        pw.println("<TR><TD colspan=2>&nbsp;</TD>");
        for (int i = 0; i < values.length; i++)
            pw.print("<TD align=\"right\"><b>" + values[i] + "</b></TD>");

        pw.println("</TR>");
    }

    // To write The details for a state.
    public void writeStateInfo(String state, int[] values)
    {
        pw.println("<TR><TD>&nbsp;</TD><TD>" + state + "</TD>");
        for (int i = 0; i < values.length; i++)
            pw.print("<TD align=\"right\">" + values[i] + "</TD>");

        pw.println("</TR>");
    }

    // Write The total across all the applications.
    public void doTotal(int[] values)
    {
        pw
                .println("<TR class=yellow><TD colspan=2><b>Total (all Applications)</b></TD>");
        for (int i = 0; i < values.length; i++)
            pw.print("<TD align=\"right\"><b>" + values[i] + "</b></TD>");
        pw.println("</TR></TABLE>");
    }

    /**
     * Transactions Report Methods
     */

    public void startTransactionsReport()
    {
        pw.println("<BR><div align=\"center\">");
        if (m_bWriteCallLogDetails)
            pw.print(getAnchorURL("Top", "Goto Top") + " | ");
        pw.println(getAnchorURL("Summary", "Summary") + "</div>");
        pw
                .println("<div align=\"center\"><H3><A name=\"transaction\">Transaction Status</A></H3></div>");
        pw.println("<TABLE width=60% align=\"center\" border=1>");
        pw
                .println("<TR class=blue><TD width=20%>Transaction</TD><TD width=20%>Application</TD><TD width=10% align=\"right\">Success</TD><TD width=10% align=\"right\">Failures</TD></TR>");
    }

    public void addTransactionDetails(String transName, String appName,
            int numSuccess, int numFailures)
    {
        pw.println("<TR><TD>" + transName + "</TD><TD>" + appName
                + "</TD><TD align=\"right\">" + numSuccess
                + "</TD><TD align=\"right\">" + numFailures + "</TD></TR>");
    }

    // Close The file.
    public void close()
    {
        endDocument();
        pw.close();
    }

    // Writes The initial html tags for the document.
    protected void beginDocument()
    {
        comment("Output File generated by CallLogAnalyzer.");
        pw.println("<HTML>");
        pw.println("<HEAD><TITLE>" + HTML_TITLE + "</TITLE>");
        pw.println("<STYLE> TD { font-family: Verdana; font-size: x-small;}");
        pw.println("TR.green { background-color: green; font-weight: bold;}");
        pw.println("TR.blue { background-color: #99ccff; font-weight: bold;}");
        pw.println("TR.yellow { background-color: #FFFFCC;} </STYLE></HEAD>");
        pw.println("<BODY BGCOLOR=white TEXT=black topmargin=20>");
        pw
                .println("<div align=\"center\"><H3><A name=\"Top\">Call Log Analysis Report</A></H3>");
        if (m_bWriteCallLogDetails)
            pw.println(getAnchorURL("Summary", "Summary") + " | "
                    + getAnchorURL("transaction", "Transaction Status")
                    + "</div><br>");
        pw.println("<TABLE width=100% border=1>");
    }

    // Writes a single line comment.
    protected void comment(String comment)
    {
        pw.println("<!-- " + comment + " -->");
    }

    // Return the file URL.
    protected String getFileURL(String file, String text)
    {
        return "<A HREF=\"file://" + file + "\" target=\"_blank\">" + text
                + "</A>";
    }

    // Reuturn a link to an anchor in te same document.
    protected String getAnchorURL(String anchor, String text)
    {
        return "<A HREF=\"#" + anchor + "\">" + text + "</A>";
    }

    // Writes the ending tags.
    protected void endDocument()
    {
        pw.println("</FONT></BODY>");
        pw.println("</HTML>");
    }
}