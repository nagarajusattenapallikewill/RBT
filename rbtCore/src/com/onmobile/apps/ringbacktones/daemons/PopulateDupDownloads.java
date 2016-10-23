package com.onmobile.apps.ringbacktones.daemons;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTPrimitive;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;
import com.onmobile.common.db.OnMobileDBServices;

public class PopulateDupDownloads extends RBTPrimitive
{
    private static Logger logger = Logger.getLogger(PopulateDupDownloads.class);

    private SimpleDateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private String rbtDBUrl = null;
    private String smsDBUrl = null;
    private int insertSize = 100;
    private int fetchDays = 90;
    private int fetchDaysForhotSongs=7;
    private int recDownloadCnt = 3;
    private int recDownloadCntForHotSongs = 1;
    private int maxRecSMSPerSub = 3;

    protected PopulateDupDownloads()
    {
        rbtDBUrl = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "DB_URL", null);
        smsDBUrl = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RBT_SMS_DB_URL", null);
        //Changes done for URL Encryption and Decryption
        ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
		try {
			if (resourceBundle.getString("ENCRYPTION_MODEL") != null
					&& resourceBundle.getString("ENCRYPTION_MODEL")
							.equalsIgnoreCase("yes")) {
				rbtDBUrl = URLEncryptDecryptUtil.decryptAndMerge(rbtDBUrl);
				smsDBUrl = URLEncryptDecryptUtil.decryptAndMerge(smsDBUrl);
			}
		} catch (MissingResourceException e) {
			logger.error("resource bundle exception: ENCRYPTION_MODEL");
		}
		// End of URL Encryption and Decryption
        insertSize = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "DOWNLOAD_INSERT_GROUP_SIZE", 100);
        fetchDays = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "DOWNLOAD_FETCH_DAYS", 90);
        fetchDaysForhotSongs = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "DOWNLOAD_FETCH_DAYS_FOR_HOT_SONGS", 7);
        recDownloadCnt = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "RECOMMENDATION_DOWNLOAD_COUNT", 3); 
        recDownloadCntForHotSongs = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "RECOMMENDATION_DOWNLOAD_COUNT_FOR_HOT_SONGS", 1); 
        maxRecSMSPerSub = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "MAX_RECOMMENDATION_SMS_PER_SUBSCRIBER", 3);

        //loading the driver class
        try
        {
            Class.forName("com.sap.dbtech.jdbc.DriverSapDB");
        }
        catch (ClassNotFoundException e)
        {
            logger.error("", e);
        }
    }

    public Connection getConnection(String dbURL)
    {
        Connection conn = null;

        try
        {
            conn = OnMobileDBServices.getDBConnection(dbURL, null, null);
        }
        catch (Exception e)
        {
        	logger.error("", e);
            return null;
        }

        return conn;
    }

    private void deleteAllEntries(Connection con)
    {

        String query = "DELETE * FROM RBT_SUBSCRIBER_DOWNLOADS_DUMMY";
        try
        {
            con.createStatement().execute(query);
        }
        catch (Exception e)
        {
        	logger.error("", e);
        }
    }

    public boolean runDownloadUpdate()
    {
        logger.info("RBT::inside run");

        if (rbtDBUrl == null || smsDBUrl == null)
        {
            logger.info("RBT:: db url's not configured");
            return false;
        }
        Connection rbtConn = null;
        Connection smsConn = null;
        Date gathererTime = null;
        ResultSet rs = null;
        String query = null;
        Date currentTime = new Date();
        try
        {
            //checking in the gatherer table if the time exceeded
            gathererTime = checkGathererParam();

            rbtConn = getConnection(rbtDBUrl);
            smsConn = getConnection(smsDBUrl);
            PreparedStatement pStmt = smsConn
                    .prepareStatement("insert into RBT_SUBSCRIBER_DOWNLOADS_DUMMY  values (?,?,?,?,?,?,?,?,?)");
            if (gathererTime == null)
            {
                deleteAllEntries(smsConn);
                query = "SELECT * FROM RBT_SUBSCRIBER_DOWNLOADS WHERE SET_TIME > SYSDATE-"
                        + fetchDays
                        + " AND SET_TIME <= "
                        + sqlTime(currentTime);
            }
            else
                query = "SELECT * FROM RBT_SUBSCRIBER_DOWNLOADS where SET_TIME > "
                        + sqlTime(gathererTime)
                        + " AND SET_TIME <= "
                        + sqlTime(currentTime);
            try
            {
                rs = rbtConn.createStatement().executeQuery(query);
            }
            catch (Exception e)
            {
                logger.error("", e);
                return false;
            }
            if (rs.next())
            {
                logger.info("RBT::Got the initial result set");

                try
                {
                    int i = 0;
                    while (true)
                    {
                        ResultSetMetaData metaData = rs.getMetaData();

                        for (int colCount = 1; colCount <= metaData
                                .getColumnCount(); colCount++)
                        {
                            pStmt.setString(colCount, rs.getString(colCount));
                        }

                        pStmt.addBatch();
                        i++;
                        if (i == insertSize)
                        {
                            try
                            {
                                pStmt.executeBatch();
                            }
                            catch (Exception e)
                            {
                                logger.error("", e);
                            }
                            pStmt.clearParameters();
                            i = 0;
                        }
                        if (!rs.next())
                        {
                            if (i != 0)
                            {
                                try
                                {
                                    pStmt.executeBatch();
                                }
                                catch (Exception e)
                                {
                                    logger.error("", e);
                                }
                                pStmt.clearParameters();
                            }
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.error("", e);
                    return false;
                }
                finally
                {
                    try
                    {
                        if (rs != null)
                            rs.close();
                    }
                    catch (Exception e)
                    {
                    }
                    try
                    {
                        if (pStmt != null)
                            pStmt.close();
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
            return false;
        }
        finally
        {
            releaseConnection(rbtConn);
            releaseConnection(smsConn);

            updateGathererParam(currentTime);
        }
        logger.info("RBT::Finished execution");
        return true;
    }

    private Date checkGathererParam()
    {
        Parameters lastUpdateTimeParam = CacheManagerUtil.getParametersCacheManager().getParameter("TATA_RBT_DAEMON", "LAST_DOWNLOAD_UPDATE_DATE");

        Date lastUpdateTime = null;

        try
        {
            lastUpdateTime = formatter.parse(lastUpdateTimeParam.getValue());
        }
        catch (Exception e)
        {

        }
        return lastUpdateTime;
    }

    private void updateGathererParam(Date lastUpdateTime)
    {
        if (lastUpdateTime != null)
        {
        	CacheManagerUtil.getParametersCacheManager().updateParameter("TATA_RBT_DAEMON",
                                     "LAST_DOWNLOAD_UPDATE_DATE",
                                     formatter.format(lastUpdateTime));
        }
    }

    public HashMap getSubscribersForRecommendation(String[] categoryIDs)
    {
        logger.info("RBT::inside getSubscribersForRecommendation");

        HashMap subscriberMap = new HashMap();

        String categoryIDStr = "";
        for (int i = 0; i < categoryIDs.length - 1; i++)
            categoryIDStr += categoryIDs[i] + ", ";
        categoryIDStr += categoryIDs[categoryIDs.length - 1];

        String query = "SELECT SUBSCRIBER_ID, CATEGORY_ID, COUNT(SUBSCRIBER_ID) C "
                + "FROM RBT_SUBSCRIBER_DOWNLOADS_DUMMY WHERE SET_TIME > SYSDATE-"
                + fetchDays
                + " AND CATEGORY_ID IN("
                + categoryIDStr
                + ") HAVING COUNT(SUBSCRIBER_ID) >= "
                + recDownloadCnt
                + " GROUP BY SUBSCRIBER_ID, CATEGORY_ID ORDER BY SUBSCRIBER_ID, C DESC";
        logger.info("RBT:: query = " + query);

        Connection smsConn = null;
        Statement stm = null;
        ResultSet rs = null;

        try
        {
            smsConn = getConnection(smsDBUrl);
            stm = smsConn.createStatement();

            rs = stm.executeQuery(query);

            String subscriberID = null;
            String prevSubscriberID = "";
            int dwnCnt = 0;
            String categoryID = null;

            while (rs.next())
            {
                subscriberID = rs.getString("SUBSCRIBER_ID");
                categoryID = rs.getString("CATEGORY_ID");

                if (subscriberID.equalsIgnoreCase(prevSubscriberID))
                {
                    if (dwnCnt == maxRecSMSPerSub)
                        continue;
                    else
                        dwnCnt++;
                }
                else
                {
                    prevSubscriberID = subscriberID;
                    dwnCnt = 1;
                }

                ArrayList subscriberList = (ArrayList) subscriberMap
                        .get(categoryID);
                if (subscriberList == null)
                {
                    subscriberList = new ArrayList();
                    subscriberList.add(subscriberID);
                    subscriberMap.put(categoryID, subscriberList);
                }
                else
                {
                    subscriberList.add(subscriberID);
                }
            }

            if (subscriberMap.size() > 0)
                return subscriberMap;
        }
        catch (Exception e)
        {
            logger.error("", e);
            return null;
        }
        finally
        {
            try
            {
                stm.close();
            }
            catch (SQLException e)
            {
            }
            releaseConnection(smsConn);
        }

        return null;
    }
    public HashMap getSubscribersForRecommendationForHotSongs(String[] categoryIDs){ 
        
        logger.info("RBT::inside getSubscribersForRecommendation"); 
 
        HashMap subscriberMap = new HashMap(); 
 
        String categoryIDStr = ""; 
        for (int i = 0; i < categoryIDs.length - 1; i++) 
            categoryIDStr += categoryIDs[i] + ", "; 
        categoryIDStr += categoryIDs[categoryIDs.length - 1]; 
 
        String query = "SELECT SUBSCRIBER_ID, CATEGORY_ID, COUNT(SUBSCRIBER_ID) C " 
                + "FROM RBT_SUBSCRIBER_DOWNLOADS_DUMMY WHERE SET_TIME > SYSDATE-" 
                + fetchDaysForhotSongs 
                + " AND CATEGORY_ID IN(" 
                + categoryIDStr 
                + ") HAVING COUNT(SUBSCRIBER_ID) >= " 
                + recDownloadCntForHotSongs 
                + " GROUP BY SUBSCRIBER_ID, CATEGORY_ID ORDER BY SUBSCRIBER_ID, C DESC"; 
        logger.info("RBT:: query = " + query); 
 
        Connection smsConn = null; 
        Statement stm = null; 
        ResultSet rs = null; 
 
        try 
        { 
            smsConn = getConnection(smsDBUrl); 
            stm = smsConn.createStatement(); 
 
            rs = stm.executeQuery(query); 
 
            String subscriberID = null; 
            String prevSubscriberID = ""; 
            int dwnCnt = 0; 
            String categoryID = null; 
 
            while (rs.next()) 
            { 
                subscriberID = rs.getString("SUBSCRIBER_ID"); 
                categoryID = rs.getString("CATEGORY_ID"); 
 
                if (subscriberID.equalsIgnoreCase(prevSubscriberID)) 
                { 
                    if (dwnCnt == maxRecSMSPerSub) 
                        continue; 
                    else 
                        dwnCnt++; 
                } 
                else 
                { 
                    prevSubscriberID = subscriberID; 
                    dwnCnt = 1; 
                } 
 
                ArrayList subscriberList = (ArrayList) subscriberMap 
                        .get(categoryID); 
                if (subscriberList == null) 
                { 
                    subscriberList = new ArrayList(); 
                    subscriberList.add(subscriberID); 
                    subscriberMap.put(categoryID, subscriberList); 
                } 
                else 
                { 
                    subscriberList.add(subscriberID); 
                } 
            } 
 
            if (subscriberMap.size() > 0) 
                return subscriberMap; 
        } 
        catch (Exception e) 
        { 
            logger.error("", e); 
            return null; 
        } 
        finally 
        { 
            try 
            { 
                stm.close(); 
            } 
            catch (SQLException e) 
            { 
            } 
            releaseConnection(smsConn); 
        } 
 
        return null; 
 
    } 

}