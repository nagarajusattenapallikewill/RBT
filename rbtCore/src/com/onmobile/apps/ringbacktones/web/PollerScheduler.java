/**
 * Class to schedule content sync to RBT sites. The main process will taken care
 * by Poller.java
 *  
 */
package com.onmobile.apps.ringbacktones.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opencms.file.CmsObject;
import org.opencms.main.A_OnspireShell;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.onmobile.JobMonitor.MonitorJob;
import com.onmobile.gprs.base.DBConnection;
import com.onmobile.gprs.base.Utils;

/**
 * @author laxmankumar
 *  
 */
public class PollerScheduler extends A_OnspireShell implements
        I_CmsScheduledJob
{

    public static Logger basicLogger = Logger.getLogger(PollerScheduler.class
            .getName());

    /* Name of the job for monitoring purpose. */
    public static final String JOB_TYPE_POLLER = "POLLER";

    /*
     * Parameter to get the rbt sites to which content sync to be done and
     * respective URLs to hit for content details.
     */
    public static final String RBT_SITES = "rbtsites";

    /* Parameter for holding the input string for 'rbtsites' */
    private String rbtSites = null;

    /* To cache RBT site folder name and URL */
    private HashMap hmRbtSiteURLs = new HashMap();

    /*
     * (non-Javadoc)
     * @see org.opencms.main.A_OnspireShell#start()
     */
    //@Override
    public String start() throws Exception
    {
        Poller poller = new Poller();
        poller.init(getCms(), getRbtSiteURLs(getRbtSites()));
        basicLogger.info("RBT::Processing started");
        poller.populateSites();
        basicLogger.info("RBT::Processing done");
        return "SUCCESS";
    }

    /*
     * (non-Javadoc)
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject,
     *      java.util.Map)
     */
    public String launch(CmsObject cms, Map params) throws Exception
    {
        Map parameters = createNewParamMap(params);
        int iRefId = -1;

        try
        {

            String sStatus = getPollerStatus();

            if ("YES".equalsIgnoreCase(sStatus))
            {
                basicLogger
                        .error("Poller job is already running. So, exiting...");
                return "Poller is already running";
            }
            else if ("ERROR".equalsIgnoreCase(sStatus))
            {
                basicLogger
                        .error("The last run of Poller job is unsuccessful. Please fix that and try again...");
                return "Error in executing Poller job...";
            }

            setPollerStatus("YES");
            basicLogger.info("Poller job has started.");
            long lStart = System.currentTimeMillis();
            String sRBTSites = (String) parameters.get(RBT_SITES);

            if (sRBTSites == null || sRBTSites.trim().length() <= 0)
            {
                basicLogger.info(RBT_SITES
                        + " parameter is not set. so exiting");
                return "parameter " + RBT_SITES + " is null.";
            }

            setContextInfo(cms, parameters);

            parameters.put(A_OnspireShell.MACHINE_ID, Utils
                    .getResource("MACHINE_ID"));
            parameters.put(A_OnspireShell.JOB_TYPE, JOB_TYPE_POLLER);
            parameters.put(A_OnspireShell.JOB_NAME, PollerScheduler.class
                    .getName());
            iRefId = MonitorJob.addJob(JOB_TYPE_POLLER, "Poller Job",
                                       "Onspire", "");
            String command = createCommand(parameters);
            int exitValue = createProcess(command);
            long lEnd = System.currentTimeMillis();
            basicLogger.info("Poller successfully completed job in "
                    + (lEnd - lStart) + " ms");
            MonitorJob.deleteJob(iRefId);
            if (exitValue == 0)
            {
                setPollerStatus("NO");
                return "Success.";
            }
            else
            {
                setPollerStatus("ERROR");
                return "Failed.";
            }
        }
        catch (Exception e)
        {
            if (iRefId != -1)
                MonitorJob.deleteJob(iRefId);
            setPollerStatus("ERROR");
            basicLogger.error("Error in executing Poller job :"
                    + e.getMessage());
            throw e;
        }
        finally
        {
            OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_CLEAR_CACHES,
                    Collections.EMPTY_MAP));
            basicLogger.info("Cache cleared...");
        }
    }

    public void setRbtSites(String rbtSites)
    {
        this.rbtSites = rbtSites;
    }

    public String getRbtSites()
    {
        return rbtSites;
    }

    public HashMap getRbtSiteURLs(String rbtSites)
    {
        String[] pairs = getRbtSites().split(";");
        for (int i = 0; i < pairs.length; i++)
        {
            String[] siteNUrl = pairs[i].split(",");
            if (siteNUrl.length == 2)
            {
                basicLogger.debug("Site = " + siteNUrl[0] + "  URL = "
                        + siteNUrl[1]);
                hmRbtSiteURLs.put(siteNUrl[0], siteNUrl[1]);
            }
            else if (siteNUrl.length == 1)
            {
                basicLogger
                        .error("Error in getting corresponding url for site "
                                + siteNUrl[0]);
            }
            else
            {
                basicLogger.error("Error in parsing the parameter 'rbtsites'");
            }
        }

        return hmRbtSiteURLs;
    }

    public static final String Q_SELECT_STATUS = "SELECT STATUS FROM POLLER_JOB";

    public static final String Q_UPDATE_STATUS = "UPDATE POLLER_JOB SET STATUS=?";

    private String getPollerStatus() throws SQLException
    {
        Connection con = null;
        PreparedStatement stmnt = null;
        ResultSet result = null;
        String sStatus = null;
        try
        {
            con = DBConnection.getConnection();
            stmnt = con.prepareStatement(Q_SELECT_STATUS);
            result = stmnt.executeQuery();
            if (result != null && result.first())
            {
                sStatus = result.getString("STATUS");
            }
            DBConnection.closeConnStmtResultSet(con, stmnt, result);
        }
        catch (SQLException sqle)
        {
            DBConnection.closeConnStmtResultSet(con, stmnt, result);
            throw sqle;
        }
        return sStatus;
    }

    private String setPollerStatus(String sStatus) throws SQLException
    {
        Connection con = null;
        PreparedStatement stmnt = null;
        try
        {
            con = DBConnection.getConnection();
            stmnt = con.prepareStatement(Q_UPDATE_STATUS);
            stmnt.setString(1, sStatus);
            stmnt.execute();
            DBConnection.closeConnStmtResultSet(con, stmnt, null);
        }
        catch (SQLException sqle)
        {
            DBConnection.closeConnStmtResultSet(con, stmnt, null);
            throw sqle;
        }
        return sStatus;
    }

}