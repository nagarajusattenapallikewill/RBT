/*
 * Created on Nov 19, 2004
 *  
 */
package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;  
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.callloganalyzer.CallLogAnalyzer;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.TransFileWriter;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.subscriptions.RBTPollDaemon;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.apps.ringbacktones.Gatherer.threadMonitor.*;
/**
 * @author Mohsin
 *  
 */
public class RBTGatherer extends Thread implements iRBTConstant, ThreadInfo
{
	private static Logger logger = Logger.getLogger(RBTGatherer.class);
	
	RBTRTSMSSender rtSMSSender = null;
    private DBHandler m_dbhandler;
    private CDRReporter m_cdr_collector;
    private FTPConfig m_ftp_config;
    public static RBTGatherer m_rbtGatherer = null;
    public RBTCopyProcessor rbtCopyProcessor = null;
    public RBTLikeRequestExecutors rbtLikeProcessor = null;
    public RTCopyProcessor rtCopyProcessor = null;
    public RBTPollDaemon rbtPollDaemon = null;
    private boolean isThreadAlive = true;
    SynchronizeIntraOperatorPrefixes rbtPrefixSyncer = null;
    UGCProcessor uGCProcessor = null;

    private SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy");
    private SimpleDateFormat m_formatEvent = new SimpleDateFormat("yyyyMMdd");
    public static ArrayList folders_to_zip = new ArrayList();
	
	static String STATUS_ERROR = "ERROR";
	public static String m_cfg_file = "Gatherer.cfg";

	private RBTDBManager rbtDBManager = null;
    private RBTConnector rbtConnector=null;
    
    int loopCount = 0;        
    boolean isSleeping = false;    
    Date gathererStartTime = Calendar.getInstance().getTime();
    Date lastLoopStartTime = Calendar.getInstance().getTime();
    long sleepInterval = 0;
    String threadName = null;
    private String m_reconDir = "./Recon";
    TransFileWriter copyCopyCallerWriter = null;
    TransFileWriter copyCopyExceptionWriter = null;
    // RBT-14671 - # like
 	private String hashLikeKeys = null;
 	
    public boolean init()
    {
        Tools.init("RBTGatherer", true);
        isThreadAlive=true;
        rbtConnector=RBTConnector.getInstance();
    	getParams();
        
    	/*if (getParamAsBoolean("DB_DATA_COLLECTION","FALSE"))
        {
    		m_dbhandler = new DBHandler();
    		if(!m_dbhandler.init())
	        {
	            logger.info("WARNING: Couldnt initialize DBHandler!");
	            return false;
	        }
    		folders_to_zip.add(getParamAsString("GATHERER_PATH") + "/db");
        }*/

        if (getParamAsBoolean("CDR_COLLECTION", "FALSE"))
        {
			m_cdr_collector = new CDRReporter();
            folders_to_zip.add(getParamAsString("GATHERER_PATH") + "/cdr");
            logger.info("callind reporter initilize()");
            m_cdr_collector.initialize(m_rbtGatherer);
        }
		
		if (getParamAsBoolean("COLLECT_SUMMARY", "FALSE"))
            folders_to_zip.add(getParamAsString("GATHERER_PATH") + "/summary");
        if (getParamAsBoolean("COLLECT_SDR", "FALSE"))
            folders_to_zip.add(getParamAsString("GATHERER_PATH") + "/sdr");
        if(getParamAsBoolean("WRITE_TRANS", "FALSE"))
        	folders_to_zip.add(getParamAsString("GATHERER_PATH") + "/copyTrans");
        if(getParamAsBoolean("WRITE_TRANS", "FALSE"))
        	folders_to_zip.add(getParamAsString("GATHERER_PATH") + "/Trans/RTTrans");
        //if(getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE"))
        	//folders_to_zip.add(getParamAsString("GATHERER_PATH") + "/EventLogs");
        if(getParamAsString(COMMON, "ADRBT_EVENT_LOGGING_DIR", null) != null)
        	folders_to_zip.add(getParamAsString("GATHERER_PATH") + "/ADRBTEventTransit");

        if(getParamAsString("GATHERER_PATH") != null && getParamAsBoolean("WRITE_TRANS", "FALSE"))
		{
        	m_reconDir = getParamAsString("GATHERER_PATH") + "/CopyRecon";
			new File(m_reconDir).mkdirs();
		}
		initCopyCallerFile();
		initCopyExceptionFile();
		// RBT-14671 - # like
		hashLikeKeys = getParamAsString("COMMON", TOLIKE_KEY, null);
		logger.info("returning true");
		return true;
    }
    
    public void stopThread() 
    { 
            isThreadAlive=false; 
    } 


	private void getParams()
    {
        rbtDBManager = RBTDBManager.getInstance();
	}
	
	public void run()
    {
        try
        {
            if (!getParamAsBoolean("DAEMON_MODE", "FALSE"))
            {
            	gather();
            	return;
            }
        }
        catch(Exception e)
        {
        	logger.error("", e);
        }
        catch(Throwable t)
        {
        	logger.info(""+t);
        }
        while (isThreadAlive)
        {
			try
			{
				
				lastLoopStartTime = Calendar.getInstance().getTime();
				isSleeping = false;
				loopCount++;
				if (getParamAsBoolean("PROCESS_COPY", "FALSE")
						|| getParamAsBoolean("COPY_NON_ONMOBILE", "FALSE")
						|| getParamAsBoolean("COPY_CROSS_OPERATOR", "FALSE")) {
					startCopyProcessor(m_rbtGatherer);
				}//RBT-14671 - # like
				if (hashLikeKeys != null && !hashLikeKeys.isEmpty())
					startLikeProcessor(m_rbtGatherer);
				if(getParamAsBoolean("SYNCHRONIZE_PREFIX", "FALSE"))
					startPrefixSyncer(m_rbtGatherer);
				if (getParamAsBoolean("PROCESS_UGC", "FALSE"))
					startUGCProcessing(m_rbtGatherer);
			    if(getParamAsBoolean("COMMON", "POLL_LIVE", "FALSE"))
		             startPollDaemon(m_rbtGatherer);
				if(getParamAsBoolean("RT_SMS", "FALSE")){
					startRTSMSSender(m_rbtGatherer);
				}
				if(getParamAsBoolean("PROCESS_RT_COPY", "FALSE")){
					startRTCopyProcessor(m_rbtGatherer);
				}
				if(getParamAsBoolean("GIFT_CLEAR_GIFTEE_SEND_SMS", "FALSE") && getParamAsString("OLDGIFT_INBOX_CLEAR_SMS") != null)
					processOldGiftedEntries();
				if (CacheManagerUtil.getSmsTextCacheManager().getSmsText("GATHERER", "PREGIFT_EXPIRY_SMS_TO_GIFTER", null) != null
						|| CacheManagerUtil.getSmsTextCacheManager().getSmsText("GATHERER", "PREGIFT_EXPIRY_SMS_TO_GIFTEE", null) != null)
					processOldPreGiftedEntries();

				if(!getParamAsBoolean("SEPARATE_REPORTING_MODULE", "FALSE"))
					makeReportingFiles();
					
					
				if(getParamAsInt("RETAILER_EXPIRE_DAYS", 0) > 0)
					sendSMS2RetailerForExpiredRequests();
		        Date next_run_time = roundToNearestInterVal(getParamAsInt("GATHERER_SLEEP_INTERVAL", 5));
		        long sleeptime = getSleepTime(next_run_time);
		        try
		        {
		            if(sleeptime < 100)
		            	sleeptime = 500;
		            logger.info("Main Thread of Gatherer : sleeping for "+sleeptime + " mSecs.");
		            isSleeping = true;
		            sleepInterval = sleeptime;
		            Thread.sleep(sleeptime);
		            isSleeping = false;
		            logger.info("Main Thread of Gatherer : waking up.");
		        }
		        catch (Throwable E)
		        {
		            logger.error("", E);
		        }
			}
			catch(Exception e)
			{
		        logger.error("", e);
			}
			catch (Throwable th)
		    {	
		        logger.error("", th);
		    }
        }
	}
    
	private boolean processOldGiftedEntries()
	{
		ViralSMSTable[] vst = rbtDBManager.getGiftInboxToBeCleared(getParamAsInt("OLDGIFT_INBOX_CLEANING_PERIOD_IN_DAYS", 7), "GIFTED");

		if(vst != null && vst.length > 0)
		{
			for(int i =0; i< vst.length; i++)
			{
				int cID = -1;
				try
				{
					cID = Integer.parseInt(vst[i].clipID());
				}
				catch(Exception e)
				{
					cID = -1;
				}
				if(cID < 0)
					continue;
				//ClipMinimal c = rbtDBManager.getClipMinimal(cID, false);
//				Clip c = rbtCacheManager.getClip(cID);
				Clip c =rbtConnector.getMemCache().getClip(cID);
				if(c != null && vst[i].callerID() != null)
				{
					String sms = getParamAsString("GATHERER","OLDGIFT_INBOX_CLEAR_SMS","");
					sms = Tools.findNReplace(sms, "%S", c.getClipName());
					sms = Tools.findNReplace(sms, "%G", vst[i].callerID());
					sendSMS(vst[i].subID(), sms);
				}
				rbtConnector.getSubscriberRbtclient().removeViralData(vst[i].subID(), vst[i].callerID(), "GIFTED",  vst[i].sentTime());
//				rbtDBManager.removeViralPromotion(vst[i].subID(), vst[i].callerID(), vst[i].sentTime(), "GIFTED");
			}
		}

		return true;
	}

	/**
	 * added by sridhar.sindiri
	 * 
	 * Cleans all the PRE_GIFT records older than the configured amount of time
	 * and sends an SMS to both giftee and gifter informing the same.
	 * 
	 * @return
	 */
	private boolean processOldPreGiftedEntries()
	{
		ViralSMSTable[] vst = rbtDBManager.getGiftInboxToBeCleared(getParamAsInt("OLD_PREGIFT_INBOX_CLEANING_PERIOD_IN_DAYS", 7), "PRE_GIFT");

		if (vst != null && vst.length > 0)
		{
			for (int i =0; i< vst.length; i++)
			{
				int cID = -1;
				boolean isShuffle = false;
				if (vst[i].clipID() == null)
					continue;

				String contentID = vst[i].clipID();
				if (contentID.startsWith("C"))
				{
					isShuffle = true;
					contentID = contentID.substring(1);
				}

				try
				{
					cID = Integer.parseInt(contentID);
				}
				catch(Exception e)
				{
					cID = -1;
				}
				if(cID < 0)
					continue;

				String songName = "";
				if (isShuffle)
					songName = RBTCacheManager.getInstance().getCategory(cID).getCategoryName();
				else
					songName = RBTCacheManager.getInstance().getClip(cID).getClipName();

				if (vst[i].callerID() != null)
				{
					String gifterSms = CacheManagerUtil.getSmsTextCacheManager().getSmsText("GATHERER", "PREGIFT_EXPIRY_SMS_TO_GIFTER", null);
					if (gifterSms != null)
					{
						gifterSms = Tools.findNReplace(gifterSms, "%SONG_NAME", songName);
						gifterSms = Tools.findNReplace(gifterSms, "%GIFTEE", vst[i].callerID());
						sendSMS(vst[i].subID(), gifterSms);
					}
					
					String gifteeSms = CacheManagerUtil.getSmsTextCacheManager().getSmsText("GATHERER", "PREGIFT_EXPIRY_SMS_TO_GIFTEE", null);
					if (gifteeSms != null)
					{
						gifteeSms = Tools.findNReplace(gifteeSms, "%SONG_NAME", songName);
						gifteeSms = Tools.findNReplace(gifteeSms, "%GIFTER", vst[i].subID());
						sendSMS(vst[i].callerID(), gifteeSms);
					}
				}

				DataRequest dataRequest = new DataRequest(vst[i].subID(), vst[i].callerID(), "PRE_GIFT");
				dataRequest.setSentTime(vst[i].sentTime());
				RBTClient.getInstance().removeViralData(dataRequest);
			}
		}

		return true;
	}

    private void sendSMS(String subscriber, String sms)
    {
        try
        {
            if(sms != null)
            	Tools.sendSMS(com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumber("GATHERER", subscriber, "SENDER_NO"), subscriber, sms, false);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
    }

    private void gather()
    {
        logger.info("Entering");
    	try
    	{
    		//if (getParamAsBoolean("DB_DATA_COLLECTION","FALSE"))
    			//m_dbhandler.parseDB(null);

	        if (getParamAsBoolean("CDR_COLLECTION","FALSE"))
	        	(new CDRReporter()).process();
	        
	        /*if (m_collectSummary)
	            collectSummary();*/
	        
	        if (getParamAsBoolean("COLLECT_SDR", "FALSE"))
	            collectSDRFiles();
	        
	        if (getParamAsBoolean("CALLLOG_ANALYZE", "FALSE"))
	            CallLogAnalysis();
	        
	        if(getParamAsBoolean("WRITE_TRANS", "FALSE"))
	        	collectCopyTransFiles();
	        
	        //if(getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE"))
	        	//collectEventLoggingFiles();

	        if(getParamAsString(COMMON, "ADRBT_EVENT_LOGGING_DIR", null) != null)
	        	collectADRBTEventLoggingFiles();

	        createGathererCfgFile();
	
	        copyXML();
	
	        Tools.writeLogFile(getParamAsString("GATHERER_PATH"));
	
	        zipcreator zc = new zipcreator();
	        String zipfilename = zc.createzip(null);
	
	        logger.info("zipfilename " + zipfilename);
	        if (zipfilename == null)
	        {
	            logger.info("No Zip file created");
	            return;
	        }
	
	        if (getParamAsString("GATHERER","TRANSFER_MODEL","").equalsIgnoreCase("FTP"))
	            uploadZipFilesToFTP(zipfilename);
	        else if (getParamAsString("GATHERER","TRANSFER_MODEL","").equalsIgnoreCase("SPIDER"))
	            uploadZipFilesToSpiderDir(zipfilename);
	        
	        /*if (!getParamAsString("GATHERER","DB_BACKUP","full").equalsIgnoreCase("full") && getParamAsBoolean("COLLECT_FULL_INCR", "TRUE"))
	        {
	            m_dbhandler.parseDB(getParamAsString("GATHERER_PATH") + "/db-Full");
	            zipfilename = zc.createzip("FullBackUP");
	        }*/
	    }
	    catch(Exception e)
	    {
	    	logger.error("", e);
	    }
        logger.info("Exiting");
    }

    private void copyXML()
    {
        String path = System.getProperty("ONMOBILE", null);
        String file = "RBT.xml";
        if (path == null)
            path = "." + File.separator + file;
        else
            path = path + File.separator + "config" + File.separator + file;
        Tools.moveFile(getParamAsString("GATHERER_PATH"), new File(path));
    }

    private void uploadZipFilesToFTP(String zipfile)
    {
        FTPHandler ftphandler = new FTPHandler(m_ftp_config);
        File file_to_zip = new File(zipfile);

        boolean success = ftphandler.upload(file_to_zip.getAbsolutePath());
        if (success)
        {
            File[] zip_files = getZipFileList();

            if (zip_files == null || zip_files.length <= 0)
                return;
            for (int i = 0; i < zip_files.length; i++)
            {
                if (zip_files[i].getName().equalsIgnoreCase(zipfile) == false)
                    zip_files[i].delete();
            }
        }
    }

    private void CallLogAnalysis()
    {
        
        String IPAddresses = getParamAsString("IP_ADDRESSES");
        String callogdir = getParamAsString("CALLLOG_DIR");
        StringTokenizer token = new StringTokenizer(IPAddresses, ",");
        String[] dirs = new String[token.countTokens()];
        int i = 0;
        while (token.hasMoreTokens())
            dirs[i++] = "\\\\" + token.nextToken() + "\\" + callogdir.replace(':', '$');
        CallLogAnalyzer cla = new CallLogAnalyzer();
        logger.info("generating calllog files");
        cla.processCallLogs(dirs);
    }

    private void uploadZipFilesToSpiderDir(String zipfile)
    {
        File file_to_copy = new File(zipfile);
        Tools.moveFile(getParamAsString("SPIDER_DIR"), file_to_copy);
        File[] zip_files = getZipFileList();
        if (zip_files == null || zip_files.length <= 0)
            return;
        for (int i = 0; i < zip_files.length; i++)
        {
            String date = "";
            StringTokenizer tokens = new StringTokenizer(zip_files[i].getName(), "_");
            while (tokens.hasMoreTokens())
                date = tokens.nextToken();
            Date zipFileDate = Tools.getdate(date.substring(0, date.indexOf(".")), "yyyy-MMM-dd");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -2);
            Date compDate = Tools.getdate(Tools.getChangedFormatDate(cal.getTime()), "yyyy-MM-dd");
            logger.info(" zipfile date " + zipFileDate + " cal.getTime() "+ cal.getTime());

            if (compDate.after(zipFileDate))
                zip_files[i].delete();
        }
    }

    private void createGathererCfgFile()
    {
    	try
        {
	        File reportFile = new File(getParamAsString("GATHERER_PATH") + "/" + m_cfg_file);
	        FileOutputStream fout = null;
	        reportFile.createNewFile();
	        fout = new FileOutputStream(reportFile);
	        StringBuffer sb = new StringBuffer();
	        sb.append("CUST_NAME=" + getParamAsString("COMMON", "CUST_NAME", null) + "\n");
	        sb.append("SITE_NAME=" + getParamAsString("COMMON", "SITE_NAME", null) + "\n");
	        sb.append("SUBS_MODEL=" + getParamAsString("COMMON", "DB_BACKUP", "full") + "\n");
	        sb.append("DB_COLLECTION_DAYS=" + getParamAsInt("DB_COLLECTION_DAYS", 5)+"\n");
	        sb.append("DATA_COLLECTION_DAYS=" + getParamAsInt("DATA_COLLECTION_DAYS", 5)+"\n");
	        sb.append("SITE_ID=" + getParamAsInt("SITE_ID", 0));
	        fout.write(sb.toString().getBytes());
	        fout.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        logger.info("created cfg file");
    }

    private void collectSDRFiles()
    {

        String sdr_path = getParamAsString("SMS","SDR_WORKING_DIR", null);// RBTSMSConfig.getInstance().getParameter("SDR_WORKING_DIR");
        File sdr_file = null;

        if (sdr_path != null)
            sdr_file = new File(sdr_path + File.separator + "smssdr");

        if (sdr_file.exists())
        {
            logger.info("sdr_file exists");
            File[] sdr_list = sdr_file.listFiles(new FilenameFilter()
            {
                public boolean accept(File file, String name)
                {
                    Calendar cal = Calendar.getInstance();
                    Date today = Tools.changeDateFormat(cal.getTime());
                    cal.add(Calendar.DATE, -getParamAsInt("DB_COLLECTION_DAYS", 5));
                    Date yest = Tools.changeDateFormat(cal.getTime());
                    if (!name.startsWith("A"))
                    {
                        File sdrFile = new File(file.getAbsolutePath()+ File.separator + name);
                        Date file_date = new Date(sdrFile.lastModified());
                        if (file_date.before(yest))
                            return false;
                        else if (file_date.after(yest) && file_date.before(today))
                            return true;
                        else
                            return false;
                    }
                    else
                        return false;
                }
            });

            if (sdr_list != null && sdr_list.length > 0)
            {
                Tools.addToLogFile("SDR File collection Started...");
                File sdr = new File(getParamAsString("GATHERER_PATH") + "/sdr");

                if (!sdr.exists())
                    sdr.mkdirs();
                
                for (int i = 0; i < sdr_list.length; i++)
                    Tools.moveFile(getParamAsString("GATHERER_PATH") + "/sdr", sdr_list[i]);
                Tools.addToLogFile("SDR File collection Ended...");
            }
        }
    }
  
    private void startPollDaemon(RBTGatherer rbtGathererThread)
     {
             logger.info("Entering");
             if(rbtPollDaemon == null || !rbtPollDaemon.isAlive())
             {
                     logger.info("Staring PollDaemon.");
                     try
                     {
                             rbtPollDaemon = RBTPollDaemon.getInstance(rbtGathererThread);
                     }
                     catch(Exception e)
                     {
                             logger.error("", e);
                     }
             }
             logger.info("Exiting");
     }
    protected static String getStackTrace(Throwable ex) 
    { 
        StringWriter stringWriter = new StringWriter(); 
        String trace = ""; 
        ex.printStackTrace(new PrintWriter(stringWriter)); 
        trace = stringWriter.toString(); 
        trace = trace.substring(0, trace.length() - 2); 
        trace = System.getProperty("line.separator") + " \t" + trace; 
        return trace; 
    } 


    /*private void collectSummary()
    {
        String _method = "collectSummary";
        String cdr_prefix = rbtDaemonConfig.getParameter("CDR_SUMMARY_PREFIX");
        String cdr_dir = rbtDaemonConfig.getParameter("CDRSUMMARY_DIR");
        String act_dir = rbtDaemonConfig.getParameter("ACTSUMMARY_DIR");
        String act_prefix = rbtDaemonConfig.getParameter("ACT_SUMMARY_PREFIX");

        Tools.addToLogFile("Summary File collection Started...");

        boolean cdr_return = false;
        boolean act_return = false;
        if ((cdr_dir != null && cdr_prefix != null) || (act_dir != null && act_prefix != null))
        {
            CollectSummaryFiles summary = new CollectSummaryFiles();
            if (cdr_dir != null)
                cdr_return = summary.getFiles(cdr_dir, cdr_prefix, m_db_collection_days);

            if (act_dir != null)
                act_return = summary.getFiles(act_dir, act_prefix, m_db_collection_days);
        }
        else
        {
            Tools.logNonFatalError(_class, _method, "Both cdr and act summary parameters not in RBT.XML so no summary files got");
            return;
        }

        if (cdr_return || act_return)
            Tools.addToLogFile("Summary File Collected");
    }*/

    public static void main(String[] args)
    {
    	try
        {
        	new ServerSocket (16000);
    	}
        catch(Exception e)
        {
			System.err.println("The port 16000 is in use or another rbt gatherer is already running");
			System.out.println("The port 16000 is in use or another rbt gatherer is already running");
			System.exit(-1);
		}
        
        m_rbtGatherer = new RBTGatherer();
        System.out.println("Started ...");
        try
        {
            if (m_rbtGatherer.init())
                m_rbtGatherer.start();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
//            System.exit(0);
        }
    }

    public long getSleepTime(Date date)
    {
		return (date.getTime() - System.currentTimeMillis());
	}

    private String getZipFileName()
    {
        String cust = getParamAsString("COMMON", "CUST_NAME", null);
        String site = getParamAsString("COMMON", "SITE_NAME", null);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);//yesterday
        String datename = Tools.getDateAsName(cal.getTime());

        return getParamAsString("GATHERER_PATH") + "/RBTGatherer_" + cust + "_" + site + "_"+ datename + ".zip";
    }

    private File[] getZipFileList()
    {
        File[] list = new File(getParamAsString("GATHERER_PATH") + "/").listFiles(new FilenameFilter()
        {
        	public boolean accept(File dir, String name)
            {
                if (name.endsWith(".zip"))
                    return true;
                else
                    return false;
            }
        });
        return list;
    }

    public Date roundToNearestInterVal(int interval)
    {
        Calendar cal = Calendar.getInstance();
        int n = 60 / interval;
        for (int i = 1; i <= n; i++)
        {
            if (cal.get(Calendar.MINUTE) < (interval * (i)) && cal.get(Calendar.MINUTE) >= (interval * (i - 1)))
            {
                cal.set(Calendar.SECOND, 0);
                if (i < n)
                    cal.set(Calendar.MINUTE, (interval * (i)));
                else
                {
                    cal.set(Calendar.MINUTE, 0);
                    cal.add(Calendar.HOUR_OF_DAY, 1);
                }
                break;
            }
        }
        return cal.getTime();
    }

    public long getnexttime(int hour)
    {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, hour);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);

        long nexttime = now.getTime().getTime();
        if (nexttime < System.currentTimeMillis())
        {
            nexttime = nexttime + (24 * 3600 * 1000);
        }
        return nexttime;
    }

    /*private Parameters[] getParameters(String parameterType)
    {
        return rbtDBManager.getParameters(parameterType);
    }*/
    
    private Parameters[] getParameters()
    {
    	List<Parameters> paramList=rbtConnector.getRbtGenericCache().getAllParamaters();
    	if(paramList!=null && paramList.size()>0){
    		return (Parameters[])paramList.toArray(new Parameters[0]);
    	}
    	return null;
    }

    /*private String getSubstituedSMS(String smsText, String str1, String str2)
    {
        if (str2 == null)
        {
            if (smsText.indexOf("%L") != -1)
            {
                smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
                        + smsText.substring(smsText.indexOf("%L") + 2);
            }
        }
        else
        {
            while (smsText.indexOf("%S") != -1)
            {
                smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
                        + smsText.substring(smsText.indexOf("%S") + 2);
            }
            while (smsText.indexOf("%C") != -1)
            {
                smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
                        + smsText.substring(smsText.indexOf("%C") + 2);
            }
        }

        return smsText;
    }

    private void updateViralPromotion(String subscriberID, String callerID,Date sentTime, String fType, String tType)
    {
    	rbtDBManager.updateViralPromotion(subscriberID, callerID, sentTime, fType,tType,new Date(System.currentTimeMillis()),null);
    }*/

    private void collectCopyTransFiles()
	{
	 
         File copyTrans_dir = null;
 
         if (getParamAsString("GATHERER_PATH") != null)
             copyTrans_dir = new File(getParamAsString("GATHERER_PATH")+"/Trans");
 
         if (copyTrans_dir.exists())
         {
             File[] copyTrans_list = copyTrans_dir.listFiles(new FilenameFilter()
             {
                 public boolean accept(File file, String name)
                 {
                     Calendar cal = Calendar.getInstance();
                     cal.add(Calendar.DATE, -1);
                     Date today = cal.getTime();
                     cal.add(Calendar.DATE, -getParamAsInt("DATA_COLLECTION_DAYS", 5));
                     Date yest = cal.getTime();
                     if (name.startsWith("COPY_TRANS"))
                     {
                         try
                         {
 
                             String dateStr = name.substring(11,19);
                             Date file_date = m_format.parse(dateStr);
                             if (file_date.before(yest))
                             {
                                 //                              file.delete();
                                 return false;
                             }
                             else if (file_date.before(today))
                             {
                                 return true;
                             }
                             else
                                 return false;
                         }
                         catch(Exception e)
                         {
                             return false;
                         }
                     }
                     else
                      {
                          return false;
                      }
                   }
 
             });
 
             if (copyTrans_list != null && copyTrans_list.length > 0)
             {
                 Tools.addToLogFile("Copy Trans File collection Started...");
                 File copy = new File(getParamAsString("GATHERER_PATH") + "/copyTrans");
 
                 if (!copy.exists())
                 {
                     copy.mkdirs();
                 }
 
                 for (int i = 0; i < copyTrans_list.length; i++)
                 {
                     Tools.moveFile(getParamAsString("GATHERER_PATH") + "/copyTrans", copyTrans_list[i]);
                 }
                 Tools.addToLogFile("CopyTrans File collection Ended...");
             }
         }
	}

    
    private void collectEventLoggingFiles()
	{
	 
         File eventLogs_dir = null;
 
         if (getParamAsString("GATHERER_PATH") != null)
        	 eventLogs_dir = new File(getParamAsString("GATHERER_PATH")+" /EventLogs");
 
         if (eventLogs_dir.exists())
         {
             File[] eventLogs_list = eventLogs_dir.listFiles(new FilenameFilter()
             {
                 public boolean accept(File file, String name)
                 {
                     Calendar cal = Calendar.getInstance();
                     cal.add(Calendar.DATE, -1);
                     Date today = cal.getTime();
                     cal.add(Calendar.DATE, -getParamAsInt("DATA_COLLECTION_DAYS", 5));
                     Date yest = cal.getTime();
                     if (name.startsWith("copytrans"))
                     {
                         try
                         {
 
                             String dateStr = name.substring(11,19);
                             Date file_date = m_format.parse(dateStr);
                             if (file_date.before(yest))
                             {
                                 //                              file.delete();
                                 return false;
                             }
                             else if (file_date.before(today))
                             {
                                 return true;
                             }
                             else
                                 return false;
                         }
                         catch(Exception e)
                         {
                             return false;
                         }
                     }
                     else
                      {
                          return false;
                      }
                   }
 
             });
 
             if (eventLogs_list != null && eventLogs_list.length > 0)
             {
                 Tools.addToLogFile("EventLogs File collection Started...");
                 File copy = new File(getParamAsString("GATHERER_PATH") + "/EventLogs");
 
                 if (!copy.exists())
                 {
                     copy.mkdirs();
                 }
 
                 for (int i = 0; i < eventLogs_list.length; i++)
                 {
                     Tools.moveFile(getParamAsString("GATHERER_PATH") + "/EventLogs", eventLogs_list[i]);
                 }
                 Tools.addToLogFile("EventLogs File collection Ended...");
             }
         }
	}

    	
    	private void makeReportingFiles()
    	{
    		logger.info("Entering");
    		int gather_hour = getParamAsInt("GATHERER_HOUR", 1);
    		Calendar cal = Calendar.getInstance();
            int current_hour = cal.get(Calendar.HOUR_OF_DAY);
            if (current_hour >= gather_hour)
            {
                String zip_file = getZipFileName();
                if (! (new File(zip_file).exists()))
                {
                    logger.info("yesterday's zip " +zip_file+ " not found. So calling gather()");
                    gather();
                }
            }
            logger.info("Exiting");
    	}
		
    	private void startCopyProcessor(RBTGatherer rbtGathererThread)
		{
			logger.info("Entering");
			if(rbtCopyProcessor == null || !rbtCopyProcessor.isAlive())
			{
				logger.info("Staring RBTCopyProcessor.");
				try
				{
					rbtCopyProcessor = new RBTCopyProcessor(rbtGathererThread);
					rbtCopyProcessor.setThreadName("RBTCopyMain");
					ThreadMonitor.getMonitor().register(rbtCopyProcessor);
					rbtCopyProcessor.start();
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
			logger.info("Exiting");
		}

	// RBT-14671 - # like
	/**
	 * This Method will run the like daemon thread
	 * 
	 * @param rbtGathererThread
	 */
	private void startLikeProcessor(RBTGatherer rbtGathererThread) {
		logger.info("Entering startLikeProcessor");
		if (rbtLikeProcessor == null || !rbtLikeProcessor.isAlive()) {
			logger.info("Staring RBTCopyProcessor.");
			try {
				rbtLikeProcessor = new RBTLikeRequestExecutors(
						rbtGathererThread);
				rbtLikeProcessor.setName("RBTLikeExecutorThread");
				rbtLikeProcessor.start();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	private void startRTCopyProcessor(RBTGatherer rbtGathererThread)
		{
			logger.info("Entering");
			if(rtCopyProcessor == null || !rtCopyProcessor.isAlive())
			{
				logger.info("Staring RTCopyProcessor.");
				try
				{
					rtCopyProcessor = new RTCopyProcessor(rbtGathererThread);
					rtCopyProcessor.start();
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
			logger.info("Exiting");
		}

		
		
		private void startRTSMSSender(RBTGatherer rbtGathererThread){
			logger.info("Entering");
			if(rtSMSSender == null || !rtSMSSender.isAlive())
			{
				logger.info("Starting RBTRTSMSSender.");
				try{
					
					rtSMSSender = new RBTRTSMSSender(rbtGathererThread);
					rtSMSSender.start();
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
			logger.info("Exiting");
		}
		
		private void startPrefixSyncer(RBTGatherer rbtGathererThread)
		{
			logger.info("Entering");
			if(rbtPrefixSyncer == null || !rbtPrefixSyncer.isAlive())
			{
				logger.info("Staring SynchronizeIntraOperatorPrefixes.");
				try
				{
					rbtPrefixSyncer = new SynchronizeIntraOperatorPrefixes(rbtGathererThread);
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
			logger.info("Exiting");
		}
		private void startUGCProcessing(RBTGatherer rbtGathererThread)
		{
			logger.info("Entering");
			if(uGCProcessor == null || !uGCProcessor.isAlive())
			{
				logger.info("Staring UGCProcessor.");
				try
				{
					uGCProcessor = new UGCProcessor(rbtGathererThread);
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
			logger.info("Exiting");
		}
		private void sendSMS2RetailerForExpiredRequests()
		{
			logger.info("Entering");
			ViralSMSTable[] vst = rbtDBManager.getViralSMSByTypeAndLimitAndTime("RETAILER", getParamAsInt("RETAILER_EXPIRE_DAYS", 1)*24*60, 5000); 
			if(vst == null || vst.length <= 0)
			{
				logger.info("Found no RETAILER entries in Viral table. So exit.");
				return;
			}
			logger.info("Found entries in ");
			for(int i = 0; i < vst.length; i++)
			{
				try
				{
					String retailerID = vst[i].callerID();
					String retaileeID = vst[i].subID();
					String smsText = getParamAsString("GATHERER","RETAILER_EXPIRE_TEXT", "The RBT request you sent has not been accepted by %CALLER_ID.");
					smsText = smsText.replaceAll("%CALLER_ID", retaileeID);
					sendSMS(retailerID, smsText);
					rbtConnector.getSubscriberRbtclient().updateViralData(retaileeID,
							retailerID, null, vst[i].sentTime(), "RETAILER", "RETAILEREXPIRE", null, null,null);
//					rbtDBManager.updateViralPromotion(retaileeID, retailerID,
//							vst[i].sentTime(), "RETAILER", "RETAILEREXPIRE", null,
//							null);
				}
				catch(Exception e)
				{
					logger.info("Error in processing expired retailer request");
				}
			}
			logger.info("Exiting");
		}
		
		public String getParamAsString(String param)
		   {
		    	try{
		    		return rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, null);
		    	}catch(Exception e){
		    		logger.info("Unable to get param ->"+param );
		    		return null;
		    	}
		    }
		    
		  public String getParamAsString(String type, String param, String defualtVal)
		    {
		    	try{
		    		return rbtConnector.getRbtGenericCache().getParameter(type, param, defualtVal);
		    	}catch(Exception e){
		    		logger.info("Unable to get param ->"+param +"  type ->"+type);
		    		return defualtVal;
		    	}
		    }
		    
		    public int getParamAsInt(String param, int defaultVal)
		    {
		    	try{
		    		String paramVal = rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal+"");
		    		return Integer.valueOf(paramVal);   		
		    	}catch(Exception e){
		    		logger.info("Unable to get param ->"+param );
		    		return defaultVal;
		    	}
		    }
		    
		    public int getParamAsInt(String type, String param, int defaultVal)
		    {
		    	try{
		    		String paramVal =  rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal+"");
		    		return Integer.valueOf(paramVal);   		
		    	}catch(Exception e){
		    		logger.info("Unable to get param ->"+param +"  type ->"+type);
		    		return defaultVal;
		    	}
		    }
		    
		    public boolean getParamAsBoolean(String param, String defaultVal)
		    {
		    	try{
		    		return  rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal).equalsIgnoreCase("TRUE");
		    	}catch(Exception e){
		    		logger.info("Unable to get param ->"+param );
		    		return defaultVal.equalsIgnoreCase("TRUE");
		    	}
		    }
		    public boolean getParamAsBoolean(String type, String param, String defaultVal)
		    {
		    	try{
		    		return  rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal).equalsIgnoreCase("TRUE");
		    	}catch(Exception e){
		    		logger.info("Unable to get param ->"+param +"  type ->"+type);
		    		return defaultVal.equalsIgnoreCase("TRUE");
		    	}
		    }

			public boolean amIAlive()
			{
				return this.isAlive();
			}

			public String getActivity()
			{
				return null; 
			}

			public String getLoad()
			{
				String loadStr = "Gatherer's run Count is  "+loopCount;
				long curTime = System.currentTimeMillis();
				long diff = curTime - gathererStartTime.getTime();
				if(loopCount > 0)
					loadStr += ". Avg loopTime is "+(diff/loopCount); 
				return loadStr;
			}

			public String getStatus()
			{
				String statusStr = null;
				if(isSleeping)
					statusStr = "Is in Sleeping mode. Sleep interval is "+sleepInterval+" ms.";
				else
					statusStr = "Gatherer thread is not sleeping.";
				return statusStr;
			}

			public String getThreadName() {
				return threadName;
			}
			
			public void setThreadName(String name) {
				threadName = name;
			}
			
			public void initCopyCallerFile()
			{
				ArrayList<String> headers = new ArrayList<String> ();
				headers.add("MESSAGE");
				copyCopyCallerWriter = new TransFileWriter(m_reconDir, "COPY_CALLER", headers);
			}
			
			public boolean writeCopyCaller(String message)
			{
				HashMap<String,String> h = new HashMap<String,String> ();
				h.put("MESSAGE", message);
				if(copyCopyCallerWriter != null)
				{
					copyCopyCallerWriter.writeTrans(h);
					return true;
				}

				return false;
			}

			public void initCopyExceptionFile()
			{
				ArrayList<String> headers = new ArrayList<String> ();
				headers.add("MESSAGE");
				copyCopyExceptionWriter = new TransFileWriter(m_reconDir, "COPY_EXCEPTION", headers);
			}
			
			public boolean writeCopyException(String message)
			{
				HashMap<String,String> h = new HashMap<String,String> ();
				h.put("MESSAGE", message);
				if(copyCopyExceptionWriter != null)
				{
					copyCopyExceptionWriter.writeTrans(h);
					return true;
				}

				return false;
			}
		    private void collectADRBTEventLoggingFiles()
			{
			 
		        String adrbtStr =  getParamAsString(COMMON, "ADRBT_EVENT_LOGGING_DIR", null);
		        
		        if(adrbtStr == null)
		       	 return;
		        File adrbteventLogs_dir = new File(adrbtStr);
		        
		        if (adrbteventLogs_dir.exists())
		         {
		        	File[] eventLogs_list = adrbteventLogs_dir.listFiles(new FilenameFilter()
		             {
		                 public boolean accept(File file, String name)
		                 {
		                     Calendar cal = Calendar.getInstance();
		                     cal.add(Calendar.DATE, -1);
		                     Date today = cal.getTime();
		                     cal.add(Calendar.DATE, -getParamAsInt("DATA_COLLECTION_DAYS", 5));
		                     Date yest = cal.getTime();
		                     if (name.contains("adrbt"))
		                     {
		                         try
		                         {
		 
		                             String dateStr = name.substring(6,14);
		                        	 Date file_date = m_formatEvent.parse(dateStr);
		                             if (file_date.before(yest))
		                             {
		                                 //                              file.delete();
		                                 return false;
		                             }
		                             else if (file_date.before(today))
		                             {
		                                 return true;
		                             }
		                             else
		                                 return false;
		                         }
		                         catch(Exception e)
		                         {
		                             return false;
		                         }
		                     }
		                     else
		                      {
		                          return false;
		                      }
		                   }
		 
		             });
		 
		        	if (eventLogs_list != null && eventLogs_list.length > 0)
		             {
		                 Tools.addToLogFile("ADRBTEventLogs File collection Started...");
		                 File copy = new File(getParamAsString("GATHERER_PATH") + "/ADRBTEventTransit");
		 
		                 if (!copy.exists())
		                 {
		                     copy.mkdirs();
		                 }
		 
		                 for (int i = 0; i < eventLogs_list.length; i++)
		                 {
		                	 Tools.moveFile(getParamAsString("GATHERER_PATH") + "/ADRBTEventTransit", eventLogs_list[i]);
		                 }
		                 Tools.addToLogFile("ADRBTEventLogs File collection Ended...");
		             }
		         }
			}
	}
