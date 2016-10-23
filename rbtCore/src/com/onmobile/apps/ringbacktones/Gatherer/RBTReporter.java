/*
 * Created on Nov 19, 2004
 *  
 */
package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.callloganalyzer.CallLogAnalyzer;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
/**
 * @author Mohsin
 *  
 */
public class RBTReporter extends Thread implements iRBTConstant
{
    private static Logger logger = Logger.getLogger(RBTReporter.class);
	private DBHandler m_dbhandler;
    
    private RBTDBManager rbtDBManager = null;
    private RBTConnector rbtConnector=null;
    private SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy");
    private FTPConfig m_ftp_config;
    public static String m_cfg_file = "Gatherer.cfg";
    public static ArrayList folders_to_zip = new ArrayList();
    private boolean isThreadAlive = true;
    private CDRReporter m_cdr_collector;
    public static RBTGatherer m_rbtGatherer = null;
    long m_nextCleanupTime = -1;
    private RBTCleanupAutoDeact m_rbtCleanupAutoDeact;
    //long m_nextAutoDeactTime = -1; 
    int cleanup_hour = 3; 
    long m_nextOrganizeTime = -1; 
    private FileOrganizer m_fileOrganizer = null;
        
    public static void main(String[] args)
    {
    	RBTReporter rbtReporter = new RBTReporter();

		try
		{
			rbtReporter.start();
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	
	}
    
    public void run()
    {
    	init();
    		while(true)
    		{
    			try
    	        {
	    	    	makeReportingFiles();
		        	doCleanUp();
		        	//autoDeactivate();
		        	organizeFiles();
		    		Date next_run_time = roundToNearestInterVal(getParamAsInt("REPORTER_SLEEP_INTERVAL", 5));
			    	long sleeptime = getSleepTime(next_run_time);
		            if(sleeptime < 100)
		            	sleeptime = 500;
		            logger.info("Main Thread of Gatherer : sleeping for "+sleeptime + " mSecs.");
		            Thread.sleep(sleeptime);
		            logger.info("Main Thread of Gatherer : waking up.");
    	        }
    			catch (Exception E)
    	        {
    	            logger.error("", E);
    	        }
    			catch (Throwable t)
    	        {
    	            logger.info("Throwable caught. "+t);
    	        }
    		}
        
    }
    private void makeReportingFiles()
	{
		String _method = "makeReportingFiles()";
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
	
    private void gather()
    {
        String method = "gather()";
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
	        
	        createGathererCfgFile();
	
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

    private String getZipFileName()
    {
        String cust = getParamAsString("COMMON", "CUST_NAME", null);
        String site = getParamAsString("COMMON", "SITE_NAME", null);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);//yesterday
        String datename = Tools.getDateAsName(cal.getTime());

        return getParamAsString("GATHERER_PATH") + "/RBTGatherer_" + cust + "_" + site + "_"+ datename + ".zip";
    }

    private String getParamAsString(String param)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, null);
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return null;
		}
	}

	private String getParamAsString(String type, String param, String defaultValue)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defaultValue);
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type +". Returning defVal > "+defaultValue);
			return defaultValue;
		}
	}

	private int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +" returning defaultVal >"+defaultVal);
			return defaultVal;
		}
	}

	private int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type+ " returning defaultVal >"+defaultVal);
			return defaultVal;
		}
	}

	private boolean getParamAsBoolean(String param, String defaultVal)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal).equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +" returning defaultVal >"+defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
	private boolean getParamAsBoolean(String type, String param, String defaultVal)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal).equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type + " returning defaultVal >"+defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
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
	public boolean init()
    {
        String _method = "init";
        Tools.init("RBTGatherer", true);
        isThreadAlive=true;
        m_rbtGatherer = new RBTGatherer();
        m_rbtGatherer.init();
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
        m_nextCleanupTime = getnexttime(getParamAsInt("DATABASE_CLEANUP_HOUR", 3));   
        if (getParamAsBoolean("CLEAN_DB", "FALSE"))
        {
            m_rbtCleanupAutoDeact = new RBTCleanupAutoDeact();
            if (!m_rbtCleanupAutoDeact.init())
            {
                logger.info("WARNING: Couldnt initialize RBTCleanupAutoDeact!");
                return false;
            }
        }
        //m_nextAutoDeactTime = getnexttime(getParamAsInt("AUTODEACTIVATION_HOUR", 3)); 
        cleanup_hour = getParamAsInt("DATABASE_CLEANUP_HOUR", 3);
        m_nextOrganizeTime = getnexttime(getParamAsInt("FILE_ORGANIZE_HOUR", 4));
        logger.info("returning true");
		return true;
    }
    
    public void stopThread() 
    { 
            isThreadAlive=false; 
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
    public long getSleepTime(Date date)
    {
		return (date.getTime() - System.currentTimeMillis());
	}

    private void getParams()
    {
        rbtDBManager = RBTDBManager.getInstance();
    }
    private void doCleanUp()
	{
		String _method = "doCleanUp()";
		logger.info("Entering");
		            
        try
        {
        	if (System.currentTimeMillis()  + 10000 >= m_nextCleanupTime && getParamAsBoolean("CLEAN_DB","FALSE"))
        	{
                logger.info("Database cleanup started");
                m_rbtCleanupAutoDeact.cleanUp();
                m_nextCleanupTime = getnexttime(cleanup_hour);
            }
        }
        catch(Exception e)
        {
        	logger.error("", e);
        }
        logger.info("Exiting");
	}
    /*private void autoDeactivate()
	{
		String _method = "autoDeactivate()";
		logger.info("Entering");
		
        try
        {
        	if (System.currentTimeMillis()  + 10000 >= m_nextAutoDeactTime && getParamAsBoolean("AUTO_DEACTIVATIONS", "FALSE"))
            {
                logger.info("AutoDeactivations started");
                m_rbtCleanupAutoDeact.deactivateSubscribers();
                m_nextAutoDeactTime = getnexttime(getParamAsInt("AUTODEACTIVATION_HOUR", 3));
            }
        }
        catch(Exception e)
        {
        	logger.error("", e);
        }
        logger.info("Exiting");
	}
     */
    private void organizeFiles()
	{
		String _method = "organizeFiles()";
		logger.info("Entering");
		if (System.currentTimeMillis()  + 10000 >= m_nextOrganizeTime && (m_fileOrganizer==null || !m_fileOrganizer.isAlive()))
		{
            try
            {
                boolean organizeFilesRequired = getParamAsBoolean("FILES_CLEANUP", "FALSE");
                if(organizeFilesRequired)
                {
                	logger.info("File organizer started.");
                	m_fileOrganizer = new FileOrganizer();
                	m_fileOrganizer.start();
                }
            }
            catch(Exception e)
            {
            	logger.error("", e);
            }
        }
        logger.info("Exiting");
        
	}
	

    
}
