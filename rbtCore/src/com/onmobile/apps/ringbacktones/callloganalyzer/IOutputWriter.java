/**
 * Interface for the output writer class.
 */

package com.onmobile.apps.ringbacktones.callloganalyzer;

public interface IOutputWriter
{
    /*
     * To initialise the Writer and to specify whether or not to write the call
     * log analysis report.
     */
    public boolean init(boolean writeCallLogReport);

    /*
     * To initiallize writer for writing only summary file
     */
    public boolean init();

    /*
     * Functions for writing the call log analysis report. Write the parameters
     * to file.
     */
    public void startFileRecord(String fileName, String callerNum,
            String calledNum);

    /*
     * To start record for a new application
     */
    public void startApplication(String appName);

    /*
     * To append to an applications record.
     */
    public void appendToRecord(String soKey, String status, String nlInt00,
            String confidence0, String uttFileName);

    /*
     * To indicate any problem.
     */
    public void writeProblem(String problem);

    /*
     * To indicate error in the call log.
     */
    public void writeError(String error);

    /*
     * Functions for writing the call log summary report. To write the headers
     * for summary.
     */
    public void startSummary();

    /*
     * Start the summary record for one application.
     */
    public void startAppSummary(String appName, String[] Counters);

    public void writeStateInfo(String state, int[] values);

    public void writeAppInfo(int[] values);

    /*
     * To write the overall Total at the end.
     */
    public void doTotal(int[] values);

    /*
     * Functions for writing the transactions success/failure report.
     */
    public void startTransactionsReport();

    public void addTransactionDetails(String transName, String appName,
            int numSucess, int numFailures);

    /*
     * To end the records for one call log.
     */
    public void endFileRecord();

    /*
     * End the output file,
     */
    public void close();
}