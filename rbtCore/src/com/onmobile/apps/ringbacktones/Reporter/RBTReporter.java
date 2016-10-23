/*
 * Created on Nov 30, 2004
 *  
 */
package com.onmobile.apps.ringbacktones.Reporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
/**
 * @author Mohsin
 *  
 */
public class RBTReporter
{
	private static Logger logger = Logger.getLogger(RBTReporter.class);
    String _class = "RBTReporter";

    private static MorpheusUpdater m_morpheus = null;
	private static int TATA_ROAMING_CIRCLE_ID = 219;
	private String m_resource_file = "resources/rbtreporter"; 
//  private String m_resource_file = "C:\\tata-reporter\\resources\\rbtreporter_en_US";
    private String m_extract_path = "data";
    private static String m_strDBURL;
    private static String m_strCustDBURL;
    private String m_download_path =null;
    private ResourceBundle m_bundle = null;
    private FTPConfig m_ftp_config = null;
    private static Connection m_conn = null;
    private Connection m_sap_conn = null;
    private static String m_customer = "vodafone";
    private static String m_zipfile = "All";
    // private static String m_spiderPath ="c:\\tata-reporter\\spider";
    private static String m_spiderPath = "\\\\10.9.11.16\\d$\\onmobile\\xdrive\\software\\distframework\\ftphome\\spider\\local\\uploads";
    private Statement m_stmt1 = null;
    private Statement m_stmt2 = null;
    private Statement m_stmt3 = null;
    private ResultSet m_rs1 = null;
    private ResultSet m_rs2 = null;
    private Date m_delete_folder_last_date;
    private Date m_delete_logs_last_date;
    private Date m_reporter_start;
    private static String ISSUE_DETAIL = "DETAIL";
    private static String ISSUE_CRITICAL = "CRITICAL";
    private static String ISSUE_WARNING = "WARNING";

    private static String m_bakDir = null;

    private static String REMARKS_TYPE_UNZIP_ERROR = "UNZIP_ERROR";
    private static String REMARKS_TYPE_GATHERER_ERROR = "GATHERER_ERROR";
    private static String REMARKS_TYPE_REPORT_ERROR = "REPORTER_ERROR";
    private static String REMARKS_TYPE_DATA_MISMATCH = "DATA_MISMATCH";
    private static String REMARKS_TYPE_DATA_ERROR = "DATA_ERROR";
    private static String REMARKS_TYPE_DB_ERROR = "DB_ERROR";

    private static String REMARKS_STARTED = "RBTReporter Started";
    private static String REMARKS_ENDED = "RBTReporter Ended";
    private static String REMARKS_ZIP_CORRUPT = "Zip File Corrupt";
    private static String REMARKS_ZIP_NOT_IN_SPIDER = "Zip File Not in Spider Directory";
    private static String REMARKS_DATA_ERROR_ZIP = "Invalid Gatherer Data";
    private static String REMARKS_ACTIVITY = "Error in Generating Activity Report For ";
    private static String REMARKS_TUNE_SELECTION = "Error in Generating Tunes Selection Report For ";
    private static String REMARKS_SELECTION_COUNT = "Error in Generating Selection Count Report For ";
    private static String REMARKS_SMS_SELECTION_COUNT = "Error in Generating SMS Selection Report";
    private static String REMARKS_PREMIUM_TUNE_SELECTION = "Error in Generating Premium Selections Report";
	private static ServerSocket rbt_daemon_socket = null;
	private static int daemon_port = 0;
    private boolean m_takeBackup = false;
    private boolean flag = true;
    private boolean m_reconcile_content = true;
    private String m_db_root = "data";
    private String m_db_end = "db";
    private String m_cdr_dir = "cdr";
    private static String m_ftp_dir = null;
    private int m_db_collection_days = 1;
	private Hashtable hash_tuneDate = new Hashtable();
	private Hashtable hash_premiumDate = new Hashtable();
    private Date tuneDate = null;
    private Date premiumDate = null;
    private int count = 0;
	private static int m_SleepInterval = 0;
	private Hashtable ht_siteFileStream = new Hashtable();
	private	 static	long _lConnectionCreationTime = 0;
	private static int _CONNECTION_VARIABLE = 5;
	private	 static	int _TIMEOUT_DB_IN_SECS	=	600;
	private	 static	int _VERIFY_TIME_DB_IN_SECS = 420;
	private	 static	Connection _Connection	=	null;
	public static Date maxProcessDate = null;
	private static boolean flagSysDate = false;
	private static String m_strZipFileName ="none";
	public static String m_strSite = "all";
	public static String m_no_of_days_toload = "1";
	public static String ftpFilePrefix = "RBTGatherer_";
	public static boolean bKeepRunning = true;
	
    public RBTReporter()
    {
    }

    public boolean init()
    {
        String _method = "init";
        return getResource();
    }

    public boolean populateMainFile(boolean tataFlag)
    {
    	 String _method = "populateMainFile";
        logger.info("TESTINGGGGG FOR NEW JARRRRRRRRRRRRRR");
        Calendar cal = Calendar.getInstance();
		Date dateSys= cal.getTime();
       if(RBTReporter.maxProcessDate!=null)

		{ 
			dateSys=RBTReporter.maxProcessDate;
            cal.setTime(RBTReporter.maxProcessDate);
	   }
	   
        cal.add(Calendar.DATE, -1);
        //getMorpheusConnection:-
        Connection morp_conn1 = null;
        Statement morp_stmt1 = null;
        ResultSet morp_rs1 = null;

        try
        {
            logger.info("Entered in populateTata method:m_reporter_start:"+ dateSys);
            morp_conn1 = getMorpheusConnection();
            if (morp_conn1 == null)
            {
                logger.info("morp_conn1 null Exiting ....");
                return false;
            }

            String query = "select CUST_NAME,SITE_NAME from rbt_sites where CUST_NAME = '"+ m_customer + "' and ACTIVATION = 0 ";//make query
            logger.info("Executing Query:" + query);
            morp_stmt1 = morp_conn1.createStatement();
			long lPreTime = System.currentTimeMillis();
            morp_rs1 = morp_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           

            String dir = null;
            String site_id = null;
            String cust = null;
            String site = null;
            String date = Tools.getSpiderDate();
			System.out.println("date"+date);
            while (morp_rs1.next())
            {
                cust = morp_rs1.getString("CUST_NAME");
                site = morp_rs1.getString("SITE_NAME");
                //String siteid = getSiteId(cust, site);
                dir = cust + "-" + site;
            }
			//if not yesterdays delete the files and keep polling until we get todays file
            boolean download = downloadFromFTPLocation(dir);
			logger.info("DOWNLOAD VALUE FOR "+download);
            if (!download)
            {
				logger.info("DOWNLOAD FAILED FOR  "+dir);
				return false;
            }

            File downloadFile = new File("./" + m_download_path);
			//get all downloaded files
            File[] list = downloadFile.listFiles();
            if (list == null || list.length == 0)
            {
                logger.info("No Gatherer Zip files found in " + dir);
            }

            logger.info("puja : dir:" + dir);
         // File[] list = getZipFileList(m_spiderPath+File.separator+dir+File.separator+date);
            for (int i = 0; i < list.length; i++)
            {
				logger.info("OBTAINED (DOWNLOADED) ZIP FILE ::: "+list[0].getName());
                String day = Tools.getDateAsName(cal.getTime());
                //filename =     list[0].getAbsolutePath();
               if (extractZipFiles(list[0].getAbsolutePath()) == false)
                {
                    Tools.addToAuditTable(morp_conn1, site_id, ISSUE_CRITICAL,REMARKS_ZIP_CORRUPT,REMARKS_TYPE_UNZIP_ERROR);
                    list[i].delete();
                    continue;
                }

                //get the number of collection days from the db folder of the customer
				File[] files = new File("./" + m_db_root + File.separator+ cust + "-" + site + File.separator + day + File.separator + m_db_end + File.separator).listFiles();
                logger.info("day :" + m_db_root + File.separator + cust + "-" + site + File.separator + day + File.separator + m_db_end);
                for (int j = 0; j < files.length; j++)
                {
                    String name = files[j].getName();
                    if (name.startsWith("RBT_SUBSCRIBER")&& (!name.equalsIgnoreCase("RBT_SUBSCRIBER.txt")) && (!name.equalsIgnoreCase("RBT_SUBSCRIBER_TEMP.txt")) && (!name.startsWith("RBT_SUBSCRIBER_SELECTIONS")) && (!name.equalsIgnoreCase("RBT_SUBSCRIBER_CHARGING_CLASS.txt")))
                    {
                        m_db_collection_days = Integer.parseInt(name.substring(name.lastIndexOf("_") + 1, name.indexOf(".")));
						break;
                    }

                }
                System.out.println("The value of the m_db_collection_days (taken from the files in the db is )"+m_db_collection_days);
                logger.info("m_db_collection_days :"+ m_db_collection_days);
                updateRBTSites(cust, site, day);
				//IF rename fails for the below files , then only system.exit is there
                if (!splitRBTFile(cust, site, day,"RBT_SUBSCRIBER_"+m_db_collection_days+".txt"))
                    System.exit(1);
                if (!splitRBTFile(cust, site, day,"RBT_SUBSCRIBER_SELECTIONS_"+m_db_collection_days+".txt"))
                    System.exit(1);
                if (m_customer.equalsIgnoreCase("tata"))
                {
                    if(!populateTataPromoMaster(cust, site, day)){
                        logger.info(" promo failed for the "+cust+" "+site+" "+day);
                    }
                    if (!splitRBTFile(cust, site, day,"RBT_DEACTIVATED_SUBSCRIBERS_"+m_db_collection_days+".txt"))
                        System.exit(1);
                    if (!splitRBTFile(cust, site, day,"RBT_DELETED_SELECTIONS_"+m_db_collection_days+".txt"))
                        System.exit(1);
                    if (!splitRBTCategories(cust, site, day))
                        System.exit(1);
                    
					if(!populateSdrFile(cust,site,day))
                        continue;

                }
                list[i].delete();
                logger.info("Zip file " + list[i]+ " deleted from " + dir);
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
		finally 
		{
			try
			{
				if (morp_rs1 != null)
					morp_rs1.close();
				if (morp_stmt1 != null)
					morp_stmt1.close();
				if (morp_conn1 != null)
					morp_conn1.close();
			}
			catch (Exception e)
			{
			}
		}
        return true;
    }

    private boolean downloadFromFTPLocation(String folderName)
    {
		String _method = "downloadFromFTPLocation";
		boolean downloadFromFTPFlag = false;
		try{
			String date = Tools.getSpiderDate();
			m_download_path = "download\\"+folderName;
			logger.info("Processing Spider Tata dir "+ m_spiderPath + File.separator + folderName + File.separator + date);
		//  File[] list =getZipFileList(m_spiderPath+File.separator+folderName+File.separator+date);
			String dirName = "./download";
			boolean flagDirectoryDeletion = DirDelMethod(dirName);
			logger.info("flagDirectoryDeletion Main :::  "+flagDirectoryDeletion);
			m_ftp_config.set_dir(m_ftp_dir + File.separator + folderName + File.separator + date);
			FTPHandler ftphandler = new FTPHandler(m_ftp_config);
			downloadFromFTPFlag = ftphandler.downloadAll("./" + m_download_path,folderName);
			return downloadFromFTPFlag;
		}catch(Exception e)
		{
			logger.error("", e);
		}
		return downloadFromFTPFlag;
    }
	public boolean DirDelMethod(String dir)
	{
		boolean DirDeletionFlag = false;
		String _method = "DirDelMethod";
		try{
			File file = new File(dir);
			File[] fileslist = file.listFiles();
			if (fileslist != null && fileslist.length > 0)
			{
				for (int i = 0; i < fileslist.length; i++)
				{
					if(fileslist[i].isDirectory())
					{

						DirDelMethod(fileslist[i].getAbsolutePath());
					}else{

						boolean DirDeletion = fileslist[i].delete();
					}
				}
			}
			DirDeletionFlag = file.delete();
		}catch(Exception e)
		{
			logger.error("", e);
		}
		return DirDeletionFlag;
	}
	public Date getCurrentDate()
	{
		String _method = "getCurrentDate";
		Date sysCalDate = null;
		try{
			Calendar sysCal = Tools.getCalendarInstance();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			String sysCalStr = sdf1.format(sysCal.getTime());
			sysCalDate = sdf1.parse(sysCalStr);
		}catch(Exception e)
		{
			 logger.error("", e);
		}
		return sysCalDate;
	}
	/*
		update the prefixes of the particular site basing on the RBT_SITE_PREFIX.txt file obtained being collected by the gatherer
		created/modified by ??????
	*/
    private void updateRBTSites(String cust, String site, String day)
    {
        String _method = "updateRBTSites";
        String SITE_NAME = null;
        String CIRCLE_ID = null;
        //		String SITE_PREFIX = null;
        ArrayList al = null;
        Connection morp_conn3 = null;
        Statement morp_stmt3 = null;
        try
        {
            morp_conn3 = getMorpheusConnection();
            morp_stmt3 = morp_conn3.createStatement();
            File file = new File("./" + m_db_root + File.separator + cust + "-"
                    + site + File.separator + day + File.separator + m_db_end
                    + File.separator + "RBT_SITE_PREFIX.txt");
            FileReader objFile = new FileReader(file);
            BufferedReader br = new BufferedReader(objFile);
            String str = null;
            StringTokenizer strToken = null;
            while ((str = br.readLine()) != null)
            {
                String SITE_PREFIX_STR = null;
                String SITE_PREFIX = new String();
                if (str.startsWith("S"))
                    continue;
                if (-1 == str.indexOf("'"))
                {
                    strToken = new StringTokenizer(str, ",");
                    if (strToken.hasMoreTokens())
                    {
                        SITE_NAME = strToken.nextToken();
                        if (!SITE_NAME.equalsIgnoreCase("null"))
                            SITE_NAME = "'" + SITE_NAME + "'";
                    }
                    if (strToken.hasMoreTokens())
                    {
                        SITE_PREFIX = strToken.nextToken();
                        if (!SITE_PREFIX.equalsIgnoreCase(""))
                        {
                            //						SITE_PREFIX = SITE_PREFIX.substring(0,4);
                            SITE_PREFIX = "'" + SITE_PREFIX + "'";
                        }
                    }
                    if (m_customer.equalsIgnoreCase("tata"))
                    {
                        if (strToken.hasMoreTokens())
                            strToken.nextToken();
                        if (strToken.hasMoreTokens())
                            CIRCLE_ID = strToken.nextToken();
                    }
                }
                else
                {
                    SITE_NAME = str.substring(0, str.indexOf(","));
                    if (!SITE_NAME.equalsIgnoreCase("null"))
                        SITE_NAME = "'" + SITE_NAME + "'";

                    if (-1 == SITE_NAME.indexOf("Default"))
                    {
                    }
                    else
                    {
                        continue;
                    }

                    int i1 = str.indexOf("'");
                    String strSub = str.substring(i1 + 1);
                    int i2 = strSub.indexOf("'");

                    int len = strSub.length();
                    SITE_PREFIX_STR = strSub.substring(0, i2);
                    SITE_PREFIX = "'" + SITE_PREFIX_STR + "'";
                    /*
                     * ? StringTokenizer stk = new
                     * StringTokenizer(SITE_PREFIX_STR,","); al = new
                     * ArrayList(); while(stk.hasMoreTokens()) { String pre =
                     * stk.nextToken().substring(0,4); if(!al.contains(pre)) {
                     * al.add(pre); } }
                     * 
                     * for(int a =0;a <al.size();a++) { String valPre=
                     * (String)al.get(a); if(SITE_PREFIX.equals("")) SITE_PREFIX =
                     * valPre; else SITE_PREFIX = SITE_PREFIX +","+ valPre; }
                     * 
                     * if(!SITE_PREFIX.equalsIgnoreCase(""))
                     * SITE_PREFIX="'"+SITE_PREFIX+"'";
                     */
                    if (m_customer.equalsIgnoreCase("tata"))
                    {
                        String tmp = strSub.substring(i2 + 2);
                        int start = tmp.indexOf(",");
                        String circlesub = tmp.substring(start + 1);
                        CIRCLE_ID = circlesub.substring(0, circlesub
                                .indexOf(","));
                    }
                }
                String query = null;
                if (m_customer.equalsIgnoreCase("tata"))
                    query = "update rbt_sites set SITE_PREFIX=" + SITE_PREFIX
                            + ",CIRCLE_ID='" + CIRCLE_ID
                            + "' where CUST_NAME='" + cust
                            + "' and Lower(SITE_NAME)="
                            + SITE_NAME.toLowerCase() + " ";
                else
                    query = "update rbt_sites set SITE_PREFIX=" + SITE_PREFIX
                            + " where CUST_NAME='" + cust
                            + "' and Lower(SITE_NAME)="
                            + SITE_NAME.toLowerCase() + " ";
                logger.info("puja : query" + query);
				long lPreTime = System.currentTimeMillis();
				morp_stmt3.executeUpdate(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
               
            }

        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        try
        {
            if (morp_stmt3 != null)
                morp_stmt3.close();
            if (morp_conn3 != null)
                morp_conn3.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
	       }

    }
	//For spliting the file 
    private boolean splitRBTFile(String cust, String site, String day, String FileName)
    {
        String _method = "splitRBTFile";
		Hashtable h_sitePrefix = new Hashtable();
		h_sitePrefix = getSitePrefixMapping(cust);
		logger.info("POOJA POOJA POOJA :::"+h_sitePrefix.size());
        boolean flagRename = true;
        String siteName = null;
        try
        {
			//Rename to be done 
			File mainFilePath = new File("./" + m_db_root + File.separator + cust + "-" + site + File.separator + day + File.separator+ m_db_end);
            File file = new File(mainFilePath,FileName);
			String fileNewName = "ALL_"+FileName;
            File fileSub = new File(mainFilePath, fileNewName);
			try{
				logger.info("SourceFile=>" + file.toString());
				logger.info("DestinationFile=>" + fileSub.toString());
				flagRename = file.renameTo(fileSub);
				logger.info("file Rename:" + flagRename);
			}catch(Exception e)
			{
				logger.error("", e);
			}
			logger.info("fileKKKKKKKKKKKKKKKKKKKKKKKKKKKK Rename:" + flagRename);
			if(flagRename==false)
				return flagRename;
			//Reading the main Common File 
            FileReader objFileSub = new FileReader(fileSub);
            BufferedReader brSub = new BufferedReader(objFileSub);
            String strReadLine = null;
            while ((strReadLine = brSub.readLine()) != null)
            {
				//for header
				if (strReadLine.startsWith("S"))
				{
					//For Heading to be present in each file.
					appendIntoGivenSite(strReadLine,"all",day,FileName);
					continue;
				}
				//for data
				else
				{
					//For Geting the site w.r.t prefix
					siteName = getSiteOfReadLine(strReadLine ,h_sitePrefix);
					//For writing to the file w.r.t site/prefix
					appendIntoGivenSite(strReadLine,siteName,day,FileName);
				}
			}
			closeSiteFileOutputStream(ht_siteFileStream);
			brSub.close();
			objFileSub.close();
          }
        catch (Exception e)
        {
            flagRename = false;
            logger.error("", e);
        }
        return flagRename;
    }
	//WRITING to the resp file 
	private void appendIntoGivenSite(String strReadLine, String siteName,String day,String FileName)
	{
		String _method = "appendIntoGivenSite";
		boolean checkAllFlag = false;
		try{
			if(siteName.equalsIgnoreCase("All"))
			{
				checkAllFlag = true;
				Hashtable h_sitePrefix = new Hashtable();
				h_sitePrefix = getSitePrefixMapping(m_customer);
				Enumeration enum1 = h_sitePrefix.keys();
				while(enum1.hasMoreElements())
				{
					String name = (String)enum1.nextElement();
					if(siteName.equalsIgnoreCase("All"))
					{
						siteName = name;
						ht_siteFileStream = (Hashtable)getSiteFileOutputStream(strReadLine, name,day,FileName);
					}else
					{
						siteName = siteName + "," + name;
						ht_siteFileStream = (Hashtable)getSiteFileOutputStream(strReadLine, name,day,FileName);
						
					}
				}//while loop end
				ht_siteFileStream = (Hashtable)getSiteFileOutputStream(strReadLine, "Roaming",day,FileName);
			}// if condition with siteName "all" end

//Writing to  the file:-
			StringTokenizer strToken = new StringTokenizer(siteName,",");
			while(strToken.hasMoreTokens())
			{
				String name = (String)strToken.nextToken();

				//Creating folder 
				try{
					FileOutputStream fout =(FileOutputStream)ht_siteFileStream.get(name.toUpperCase());
					if(checkAllFlag==false)
						strReadLine = "\n" + strReadLine;
					fout.write(strReadLine.getBytes());
					fout.flush();
				}catch(Exception e)
				{
//					System.out.println("e ::"+e);
		            logger.error("", e);
				}

			}

		}
		catch(Exception e)
		{
            logger.error("", e);
		}
	}
	private Hashtable getSiteFileOutputStream(String strReadLine, String name,String day,String FileName)
	{
		String _method = "getSiteFileOutputStream";
		try{
			File createFolder = new File("./" + m_db_root + File.separator + m_customer + "-" + name + File.separator + day + File.separator + m_db_end);
			if (!createFolder.exists())
			{
				createFolder.mkdirs();
			}
			File loadFile = new File(createFolder, FileName);
			if (!loadFile.exists())
				loadFile.createNewFile();
			FileOutputStream fout = new FileOutputStream(loadFile,true);
			ht_siteFileStream.put(name.toUpperCase(),fout);
		}catch(Exception e)
		{
            logger.error("", e);
		}
		return ht_siteFileStream;
	}
	private void closeSiteFileOutputStream(Hashtable ht_siteFileStream)
	{
		String _method = "closeSiteFileOutputStream";
		try{
			Enumeration enum1 = ht_siteFileStream.keys();
			while(enum1.hasMoreElements())
			{
				String name = (String)enum1.nextElement();
				FileOutputStream fout = (FileOutputStream)ht_siteFileStream.get(name.toUpperCase());
				fout.close();
			}
		}catch(Exception e){
			logger.error("", e);
		}
	}
	//Geting the siteName w.r.t prefix 
	private String getSiteOfReadLine(String strReadLine, Hashtable h_sitePrefix)
	{
		String _method = "getSiteOfReadLine";
		String siteName =null;
		try{
			StringTokenizer strToken = new StringTokenizer(strReadLine, ",");
			//Geting the subscriberId
			if (strToken.hasMoreTokens())
			{
				String subscriber = (String)strToken.nextToken();
				Enumeration enum1 = h_sitePrefix.keys();
				boolean checkFlag = false;
				//Enum - for site
				while(enum1.hasMoreElements())
				{
					siteName = (String)enum1.nextElement();
					String sitePrefix = (String)h_sitePrefix.get(siteName);
					StringTokenizer prefixToken = new StringTokenizer(sitePrefix, ",");
					//loop - for prefix for each site
					while (prefixToken.hasMoreTokens())
					{
						String subs_prefix = (String)prefixToken.nextToken();
						if (subscriber.startsWith(subs_prefix))
						{
							checkFlag = true;
							break;
						}else
							continue;
					}
					if(checkFlag == true)
					{
						return siteName;
					}
				}
				if(checkFlag == false)
					return "Roaming";
			}
			return "false";
		}catch(Exception e)
		{
//			System.out.println("getSiteOfReadLine ::"+e);
            logger.error("", e);
			e.printStackTrace();
			return "false";
		}
	}
	//Returns Hashtable containing site_name and prefix for resp customer
	private Hashtable getSitePrefixMapping(String customer)
	{
		String _method = "getSitePrefixMapping";
        Connection morp_conn3 = null;
        Statement morp_stmt3 = null;
        ResultSet morp_rs3 = null;
		Hashtable h_sitePrefix = new Hashtable();
        try
        {
            morp_conn3 = getMorpheusConnection();
            morp_stmt3 = morp_conn3.createStatement();
            String query = "select SITE_NAME, SITE_PREFIX from rbt_sites where CUST_NAME ='"+ m_customer+ "' and site_name != 'Roaming' order by site_name";
			long lPreTime = System.currentTimeMillis();
			morp_rs3 = morp_stmt3.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            logger.info("query" + query);
            while (morp_rs3.next())
            {
				h_sitePrefix.put(morp_rs3.getString("SITE_NAME"), morp_rs3.getString("SITE_PREFIX"));
			}
		}catch(Exception e)
		{
//			System.out.println("Exception e");
            logger.error("", e);
		}
		finally
		{
			try{
				if(morp_rs3!=null)
					morp_rs3.close();
				if(morp_stmt3!=null)
					morp_stmt3.close();
				if(morp_conn3!=null)
					morp_conn3.close();
			}catch(Exception ex)
			{
	            logger.error("", ex);
//				System.out.println("ex ::::: "+ex);
			}
		}
		return h_sitePrefix;
	}

    //splitRBTCategories
    private boolean splitRBTCategories(String cust, String site, String day)
    {
        String _method = "splitRBTCategories";
        String siteName = null;
        String circleId = null;
        Connection morp_conn3 = null;
        Statement morp_stmt3 = null;
        ResultSet morp_rs3 = null;
        boolean flagRename = true;
        try
        {
            morp_conn3 = getMorpheusConnection();
            morp_stmt3 = morp_conn3.createStatement();
            File file = new File("./" + m_db_root + File.separator + cust + "-"+ site + File.separator + day + File.separator + m_db_end + File.separator + "RBT_CATEGORIES.txt");
            File fileSub = new File("./" + m_db_root + File.separator + cust + "-" + site + File.separator + day + File.separator + m_db_end + File.separator + "C_RBT_CATEGORIES_ALL.txt");
            flagRename = file.renameTo(fileSub);
            logger.info("file Rename:" + flagRename);
            FileReader objFileSub = null;
            BufferedReader brSub = null;

            String query = "select SITE_NAME, CIRCLE_ID from rbt_sites where CUST_NAME ='tata' order by site_name";
			long lPreTime = System.currentTimeMillis();
			morp_rs3 = morp_stmt3.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            while (morp_rs3.next())
            {

                siteName = morp_rs3.getString("SITE_NAME");
                circleId = morp_rs3.getString("CIRCLE_ID");
                StringBuffer sbSubscriber = new StringBuffer();
                String strLineSub = null;
                objFileSub = new FileReader(fileSub);
                brSub = new BufferedReader(objFileSub);
                while ((strLineSub = brSub.readLine()) != null)
                {
                    if (strLineSub.startsWith("C"))
                    {
                        sbSubscriber.append(strLineSub + "\n");
                        continue;
                    }

                    StringTokenizer strTokenSub = new StringTokenizer(strLineSub, ",");
                    for (int i = 0; i < 14; i++)
                    {
                        if (strTokenSub.hasMoreTokens())
                            strTokenSub.nextToken();
                    }
                    if (strTokenSub.hasMoreTokens())
                    {
                        String cirId = strTokenSub.nextToken();
                        if (cirId.equalsIgnoreCase(circleId))
                        {
                            sbSubscriber.append(strLineSub + "\n");
                        }
                    }
                }
                File createFolder = new File("./" + m_db_root + File.separator + cust + "-" + siteName + File.separator + day + File.separator + m_db_end);
                if (!createFolder.exists())
                {
                    createFolder.mkdirs();
                }
                File loadFile = new File(createFolder, "RBT_CATEGORIES.txt");
                if (!loadFile.exists())
                    loadFile.createNewFile();
                FileOutputStream fout = new FileOutputStream(loadFile);
                fout.write(sbSubscriber.toString().getBytes());
                fout.close();
            }
            brSub.close();
            objFileSub.close();
//            System.out.println("splitRBTCategories ALL deleted " + fileSub.delete());
            logger.info("spliting of Category");

        }
        catch (Exception e)
        {
            flagRename = false;
            logger.error("", e);
        }
        try
        {
            if (morp_rs3 != null)
                morp_rs3.close();
            if (morp_stmt3 != null)
                morp_stmt3.close();
            if (morp_conn3 != null)
                morp_conn3.close();
        }
        catch (Exception e)
        {
			logger.error("", e);
        }
        return flagRename;
    }
	private boolean populateSdrFile(String cust, String site, String day)
	{
		String _method = "populateSdrFile";
		try{
			String strReadLine=null;
			Hashtable h_sitePrefix = new Hashtable();
			h_sitePrefix = getSitePrefixMapping(cust);
			Hashtable h_failureResp = new Hashtable();
			h_failureResp = getFailureResponse();
			Date maxSdrDate = getMaxReportDate(""+213, "rbt_activity_report");
			File file = new File("./" + m_db_root + File.separator + cust + "-"+ site + File.separator + day + File.separator + "sdr");
			if(!file.exists()){
				logger.info("SDR FOLDER DOES NOT EXISTS ");
				return false;
			}
			File[] list = file.listFiles();
			if (list == null || list.length == 0)
			{
				logger.info("NO FILE FOUND IN SDR");
				return false;
			}
			for (int i = 0; i < list.length; i++)
			{
				logger.info("SDR OBTAINED  FILE :::"+list[i].getName());
				String name = list[i].getName();
				Date fileDate = null;
				try{
					name = name.substring(1,name.indexOf("_"));
					fileDate = new SimpleDateFormat("yyyyMMddHHmmss").parse(name);
				}catch(Exception e){
					logger.error("", e);
					continue;
				}
				if(fileDate.compareTo(maxSdrDate)>=0)
				{
					FileReader objFileSub = new FileReader(list[i]);
					BufferedReader brSub = new BufferedReader(objFileSub);		
					while((strReadLine = brSub.readLine()) != null)
					{
						if(strReadLine.startsWith("E"))
							continue;
						String[] strArray = getParsedSDRValue(strReadLine,9,h_sitePrefix, h_failureResp);
						String query = "update RBT_SOBD_FAILSTATUS_REPORT set RESPONSE_ID="+strArray[4]+" where SITE_ID = "+strArray[0]+" and SUBSCRIBERID = "+strArray[1]+" and REQUESTED_TIMESTAMP = "+strArray[5];
						long lPreTime = System.currentTimeMillis();
						int result = m_stmt1.executeUpdate(query);
						long lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

						
					//	logger.info("POOJA :: POOJA UPDATE RBT_SOBD_FAILSTATUS_REPORT ::"+query+" AND RESULT :"+result);
						if(result==0)
						{
							query = "insert into RBT_SOBD_FAILSTATUS_REPORT (SITE_ID,SUBSCRIBERID,SUBSCRIBER_TYPE,REQUEST,RESPONSE_ID,REQUESTED_TIMESTAMP,RESPONSE_TIMEINMS,REFERENCE_ID,REQUEST_DETAIL) VALUES("+strArray[0]+","+strArray[1]+","+strArray[2]+","+strArray[3]+","+strArray[4]+","+strArray[5]+","+strArray[6]+","+strArray[7]+","+strArray[8]+")";
						//	logger.info("POOJA :: POOJA INSERT RBT_SOBD_FAILSTATUS_REPORT ::"+query);
						lPreTime = System.currentTimeMillis();
						m_stmt1.executeUpdate(query);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
							
							result =0;
						}

					}//while loop for reading file 
					brSub.close();
					objFileSub.close();
				}//File reading over
			}//list of File 
		}catch(Exception e)
		{
			logger.error("", e);
			return false;
		}
		return true;
	}
	public String[] getParsedSDRValue(String str,int no,Hashtable h_sitePrefix,Hashtable h_failureResp)
	{
		String _method = "getParsedSDRValue";
		int iIndex = -1;
		int count =0;
		String[] strArray = new String[no];
		StringTokenizer token = new StringTokenizer(str,",");
		while(token.hasMoreTokens())
		{
			iIndex++;
			switch(iIndex)
			{
				case 0:{
					System.out.println("OMITTED :::"+token.nextToken().toString());
					break;
				}
				case 1:
				{
					count++;
					try
					{
						strArray[count]=(String)token.nextToken();
						if(strArray[count]!=null && !strArray[count].equalsIgnoreCase("UNKNOWN"))
						{
							String siteName = getSiteOfReadLine(strArray[count] ,h_sitePrefix);
							strArray[0] = getSiteId(m_customer, siteName);
						}else 
							strArray[0] = ""+TATA_ROAMING_CIRCLE_ID;
					strArray[count]="'"+strArray[count]+"'";
					}catch(Exception e)
					{
						logger.error("", e);
						strArray[0] = ""+TATA_ROAMING_CIRCLE_ID;
						strArray[count] = "'UNKNOWN'";
					}
					break;
				}
				case 2:{
					count++;
					try{
						strArray[count]=(String)token.nextToken();
						if(strArray[count].equalsIgnoreCase("POSTPAID")){
							strArray[count] = "'"+"n"+"'";
						}else if(strArray[count].trim().equalsIgnoreCase("PREPAID")){
							strArray[count] = "'"+"y"+"'";
						}else if(strArray[count].trim().equalsIgnoreCase("UNKNOWN"))
							strArray[count] = null;
					}catch(Exception e)
					{
						logger.error("", e);
						strArray[count] = null;
					}
					break;
				}
				case 4:{
					count++;
					try{
						strArray[count]=(String)token.nextToken();
						strArray[count]=(String)h_failureResp.get(strArray[count]);
					}catch(Exception e){
						logger.error("", e);
						strArray[count] = null;
					}
					break;
				}
				case 5:
				case 6:{
//					System.out.println("iIndex :"+iIndex+" and count:"+count);
					count++;
					try{
						strArray[count]=(String)token.nextToken();
						if(strArray[count]!=null && !strArray[count].trim().equalsIgnoreCase("NA"))
						{
							strArray[count] = "'"+strArray[count]+"'";
						}
						else
							strArray[count]=null;
						strArray[count] = "to_date(" + strArray[count]+ ",'yyyy-MM-dd hh24:mi:ss')";
					
					}catch(Exception e)
					{
						logger.error("", e);
						strArray[count] = null;
					}
					break;
				}
				case 9:{
					break;
				}
				default:
				{
					count++;
					try{
						strArray[count]=(String)token.nextToken();
						if(strArray[count]!=null)
							strArray[count] ="'"+strArray[count]+"'";
						else
							strArray[count]=null;
					}catch(Exception e)
					{
						logger.error("", e);
						strArray[count]=null;
					}
					break;
				}
			}
		}
		return ((String[])strArray);
	}
	private Hashtable getFailureResponse()
	{
		String _method = "getFailureResponse";
		Hashtable h_failureResp = new Hashtable();
		try{
			String query = "select RESPONSE_ID, RESPONSE from RBT_SOBD_FAILSTATUS_MASTER ";
			long lPreTime = System.currentTimeMillis();
			m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
			
			while(m_rs1.next())
			{
				h_failureResp.put(m_rs1.getString("RESPONSE"),m_rs1.getString("RESPONSE_ID"));
			}
		}catch(Exception e)
		{
			logger.error("", e);

		}
		return h_failureResp;
	}

	 public void begin()
		{
				String _method = "begin";
				ArrayList all_siteidsArray = new ArrayList();
				Hashtable all_siteidsCheck = new Hashtable();
				Connection morp_conn1 = null;
				Statement morp_stmt1 = null;
				ResultSet morp_rs1 = null;
				boolean flagRetry = true;
				int count = 0;
				try
				{
					
					//extractZipFiles(filename);
					
					boolean flag = true;
					boolean retry = true;
					Hashtable all_siteids = new Hashtable();
					Hashtable all_sites = new Hashtable();

					Hashtable hutch_categories=null;
					Hashtable idea_categories=null;

					Hashtable hutch_clips=null;
					Hashtable idea_clips=null;

					Hashtable hutch_category_clip_map=null;
					Hashtable idea_category_clip_map=null;

					Date hutch_content_release=null;
					Date idea_content_release=null;

					m_reporter_start = Calendar.getInstance().getTime();
					try
					{
					   
						morp_conn1 = getMorpheusConnection();
						if (morp_conn1 == null)
						{
							morp_conn1 = getMorpheusConnection();
							logger.info("Got Morpheus Connection");
						}
						morp_stmt1 = morp_conn1.createStatement();

						Tools.addToAuditTable(morp_conn1, null, ISSUE_DETAIL,REMARKS_STARTED, ISSUE_DETAIL);
						Tools.addToAuditTable(morp_conn1,null,ISSUE_DETAIL,"SUSBCRIBER TABLE SIZE - "+ getTableSize("RBT_SUBSCRIBER_REPORT"),ISSUE_DETAIL);
						Tools.addToAuditTable(morp_conn1,null,ISSUE_DETAIL,"SELECTION TABLE SIZE - "+ getTableSize("RBT_SUBS_SELECTIONS_REPORT"),ISSUE_DETAIL);
						 if(m_strSite.equalsIgnoreCase("all"))
		                   {
		                        
		                   
	    					String query = "select CUST_NAME,SITE_NAME from rbt_sites where CUST_NAME = '"+ m_customer + "'";//make query
	    					if (m_customer.equalsIgnoreCase("tata"))
	    						query = query+ " and site_name != 'Mumbai' order by site_id ";
	    					else if (m_customer.equalsIgnoreCase("esia"))
	    						query = query+ " and site_name != 'Jakarta' order by site_id ";
	    					else
	    						query = query + " order by site_id ";
	    					logger.info("BEGING :::: Executing Query:" + query);
	    
	    					long lPreTime = System.currentTimeMillis();
	    					morp_rs1 = morp_stmt1.executeQuery(query);
	    					long lPostTime = System.currentTimeMillis();
	    					logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
	    					while (morp_rs1.next())
	                        {
	                            String cust = morp_rs1.getString("CUST_NAME");
	                            String site = morp_rs1.getString("SITE_NAME");
	                            String siteid = getSiteId(cust, site);
	                            all_sites.put(cust + "-" + site, siteid);
	                            all_siteids.put(siteid, cust + "-" + site);
	                           
	                        }
	    	              }
						 else
						 {
						     String siteid = getSiteId(m_customer, m_strSite);
						     all_sites.put(m_customer+"-"+m_strSite,siteid);
						     all_siteids.put(siteid,m_customer+"-"+m_strSite);
						 }
						 if (m_customer.equalsIgnoreCase("tata"))
	                         all_siteids.put("213", "tata-Mumbai");
	                     else if (m_customer.equalsIgnoreCase("esia"))
	                         all_siteids.put("150", "esia-Jakarta");
						
						logger.info("POOJA POOJA POOJA all_sites :::: "+all_sites.size());
						logger.info("POOJA POOJA POOJA all_siteids :::: "+all_siteids.size());
					}
					catch (SQLException sqle)
					{
						logger.error("", sqle);
					}
					try
					{
						if (morp_rs1 != null)
							morp_rs1.close();
						if (morp_stmt1 != null)
							morp_stmt1.close();
						if (morp_conn1 != null)
							morp_conn1.close();
					}
					catch (SQLException sqle)
					{
						logger.error("", sqle);
					}

					Iterator it = null;

					while (!all_sites.isEmpty() && retry) //for every site unprocessed in the array list && retry which is not useful
					{
						if(flagSysDate){
//							maxProcessDate = getSysDate(true);
							logger.info("PROCESSING DATE OF THE REPORTER ::: maxProcessDate ::: "+maxProcessDate);
							flagSysDate=false;
							count =0;
						}
						it = sortIteratorDesc((Iterator) all_sites.keys());
						String dir = null;
						boolean flagFolder = true;
						if (m_customer.equalsIgnoreCase("tata"))
						{
							//run for tata mumbai
							dir = "tata-Mumbai";
						}
						else if (m_customer.equalsIgnoreCase("esia"))
						{
							//run for esia - jakarta
							dir = "esia-Jakarta";
						}
					    retry = false;
						
						while (it.hasNext()) // for every site as registered in the database no speecific activity for tata-mumbai & esia-jakarta
						{
							count++;
							System.out.println("%%%%%%%%%%%%%%%%%%%%%%% COUNT %%%%%%%%%%%%%%%%%%%%%%%%%%%"+count+" and all_sites.size() :::"+all_sites.size());
							if (dir == null || dir.equalsIgnoreCase("null"))
							{
								dir = (String) it.next();
								if(flagRetry==false)
								{
									if (!bKeepRunning) {
										return;
									}
									else
									{
										return;
									}
									/*flagRetry = true;
									count =1;
									//	 For reseting the value of all_sites as time setted to next date " while dir == null again " while time of downloadFromFTPLocation failure .
									Date sysCalDate = getSysDate(false);
									//logger.info("POOJA POOJA reconciled SYS TIME IS ::: AT THE END OF WHILE LOOP false ::"+sysCalDate+" and maxProcessDate "+maxProcessDate);
									if(sysCalDate.after(maxProcessDate))
									{
										Enumeration index = all_siteids.keys();
										while(index.hasMoreElements())
										{
											String name = (String)index.nextElement();
											String value = (String)all_siteids.get(name);
											if(!all_sites.contains(value))
												all_sites.put(value,name);
											flagSysDate=true;
//											logger.info("POOJA POOJA reconciled ::: all_sites AT THE END OF WHILE LOOP::"+all_sites.size());
										}
										if(flagSysDate){
//											maxProcessDate = getSysDate(true);
											logger.info("PROCESSING DATE OF THE REPORTER IN STAGING WHILE LOOP ::: maxProcessDate ::: "+maxProcessDate+" AND all_sites.size()::"+all_sites.size());
											flagSysDate=false;
											count =1;
										}
									}*/
								}
							}
							String HtValue = (String) all_sites.get(dir);
							if (HtValue != null && HtValue.trim().equalsIgnoreCase("activity"))
							{
								dir = null;
								continue;
							}

							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.DATE, -1);
							String day = new SimpleDateFormat("yyyy-MMM-dd").format(cal.getTime());
							String siteName = dir.substring(dir.indexOf("-") + 1);
							String site_id = (String) all_sites.get(dir);
							String date = Tools.getSpiderDate();
							//String filename = ftpFilePrefix + m_customer + "_" + siteName + "_" + day + ".zip";
							String filename = "none";
							if (m_strZipFileName != null && !m_strZipFileName.equals("none")){
								filename = m_strZipFileName;
							}
							System.out.println("Processing for : "+m_customer+" - "+siteName);
							//understood
							if ((!m_customer.equalsIgnoreCase("tata"))   && (!m_customer.equalsIgnoreCase("esia")))
							{
							    File downloadFile  = null;
							    File[] list = null;
							    if(filename.equalsIgnoreCase("none"))
							    {
	    							if (!downloadFromFTPLocation(dir))
	    							{
	    								dir = null;
	    //								logger.info("POOJA POOJA ::: dir :::"+dir);
	    								if(count==all_sites.size())
	    								{
	    									flagRetry=false;
	    								}
	    							}
	    
	    							downloadFile = new File("./" + m_download_path);
	    							list = downloadFile.listFiles();
							    }
							    else
							    {
								//	 	File[] list =
								// getZipFileList(m_spiderPath+File.separator+dir+File.separator+date);
							        File fZipFile = new File(filename);
							        list = fZipFile.getParentFile().listFiles();
							    }
								if (list == null || list.length == 0)
								{
									logger.info("No Gatherer Zip files found in " + dir);
									dir = null;
									continue;
								}
								//int j = 0;
								boolean bBreakFromLoop = false;
								for (int j =0 ; j < list.length ; j++)
								{
									logger.info("OBTAINED (DOWNLOADED) ZIP FILE ::: "+list[0].getName());
									try
									{
										morp_conn1 = getMorpheusConnection();
										//BABU - Changed from list[0] to list [j]
										filename =     list[0].getAbsolutePath();
										File f = new File(filename);
										if(filename!=null )
										{
	    									if(f.getName().equalsIgnoreCase(list[0].getName()))
	    									{
	    									    bBreakFromLoop = true;
	    									}
	    									else
	    									{
	    									    continue;   
	    									}
										}
										if (extractZipFiles(filename) == false)
										{
											Tools.addToAuditTable(morp_conn1,site_id,ISSUE_CRITICAL,REMARKS_ZIP_CORRUPT,REMARKS_TYPE_UNZIP_ERROR);
											boolean check = list[j].delete();
											if (check == false)
											{
												File corruptStr = new File(ftpFilePrefix + m_customer+ "_" + siteName + "_"+ day + "_CorruptedFile.zip");
												File fileIncomplete = new File("./"+ corruptStr);
												check = list[j].renameTo(fileIncomplete);
												logger.info("Trying to Remove corrupted file :"+ list[j].getName()+ " and check :"+ check);
												if (check == false)
													continue;
											}
											dir = null;
											flagFolder = true;
											continue;
										}
										else
											flagFolder = false;
									}
									catch (Exception sqle)
									{
										logger.error("", sqle);
									}
									try
									{
										if (morp_conn1 != null)
											morp_conn1.close();
									}
									catch (Exception sqle)
									{
										logger.error("", sqle);
									}

									list[j].delete();
									logger.info("Zip file "+ list[j] + " deleted from " + dir);
								
									if(bBreakFromLoop)
									    break;
								}
							}
							else
							//for tata and esia. this else block is to generate correct value for filename
							{
								filename = ftpFilePrefix + m_customer + "_" + siteName + "_" + day + ".zip";
								flagFolder = false;
							}
							if (flagFolder == true)
							{
								dir = null;
								continue;
							}
							logger.info("puja : fileName:"+ filename+"flagFolder :::::: "+flagFolder);
							File[] files = new File(getCompleteExtractPath(new File(filename))).listFiles();
							//File[] files = new File("./" + m_db_root + File.separator + m_customer + "-" + siteName + File.separator + day + File.separator + m_db_end + File.separator).listFiles();
							if (files == null)
							{
								dir = null;
								continue;
							}
							logger.info("puja : files.length:"+ files.length);
							int i = 0;
							File[]dblist = null;
							for (; i < files.length; i++)
							{
							    
								String name = files[i].getName();
								if(name.equalsIgnoreCase("db"))
								{
								    File fdb = files[i];
								    dblist = fdb.listFiles();
								    break;
								}
								

							}
							i = 0 ;
	                        for(;i<dblist.length;i++)
	                        {
	                            String name = dblist[i].getName();
	                            if (name.startsWith("RBT_SUBSCRIBER") && (!name.equalsIgnoreCase("RBT_SUBSCRIBER.txt")) && (!name.equalsIgnoreCase("RBT_SUBSCRIBER_TEMP.txt")) && (!name.startsWith("RBT_SUBSCRIBER_SELECTIONS")) && (!name.equalsIgnoreCase("RBT_SUBSCRIBER_CHARGING_CLASS.txt")))
	                                //              if(name.startsWith("RBT_SUBSCRIBER_"))
	                                {
	                                    try
	                                    {
	                                        String strDays = name.substring(name.lastIndexOf("_") + 1, name.indexOf("."));
	                                        m_db_collection_days = Integer.parseInt(strDays); 
	                                        System.out.println("The value of the m_db_collection_days (taken from the files in the db is )  ---->>"+m_db_collection_days);
	                                        logger.info("puja : To be Transferred m_db_collection_days:"+ m_db_collection_days);
	                                        break;
	                                    }
	                                    catch (Exception e)
	                                    {
	                                        logger.info("Incremental Subscriber File Not found hence exiting ...");
	                                        break;
	                                    }
	                                }
	                            
	                        }
	                        //if the db folder is having subscriber file or not
			                if(!m_customer.equalsIgnoreCase("esia"))   
			                {
									if (i < files.length)
										return;
			                }
			                //System.out.println("Before ittt.....");
							int ret = -2;
							File f = new File(filename);
							ret = updateToMorpheus(f.getName());
							//System.out.println("After ittt....."+ret);
							if (ret == -1)
							{
						// PRONEEL Temp remove, dont populate summary reports
								if(!populateActivityReportSummary(dir,site_id))
								{
									logger.info("populateActivityReportSummary has failed for "+dir);
								}
	 							populateActivityReports(dir);
								if(m_customer.equalsIgnoreCase("esia"))
									populateSelectionClassMapReport(dir);
								populateTotalSelReports(dir);
								if (!m_customer.equalsIgnoreCase("tata"))
									populateSMSSelectionReports(dir);  
								all_sites.put(dir, "activity");
							}
							else
							{
								logger.info("Zip file not loaded from "+ dir + " due to morpheusUpdater failure");
								if (ret == 0)
								{
									logger.info("puja :Removing site from all_sites:"+ dir + ":" + ret);
									//							Tools.addToAuditTable(morp_conn1, site_id,
									// ISSUE_CRITICAL, REMARKS_DATA_ERROR_ZIP,
									// REMARKS_TYPE_GATHERER_ERROR);
								}
								all_sites.remove(dir);
								if (!all_sites.isEmpty())
									it = sortIteratorDesc((Iterator) all_sites.keys());
								logger.info("puja all_sites :"+ all_sites.size());
							}
							dir = null;
						}

						/*
						 * it = sortIteratorDesc((Iterator)all_sites.keys());
						 * 
						 * while (it.hasNext()) { String dir = (String) it.next();
						 * String status = (String) all_sites.get(dir);
						 * 
						 * if(status.equalsIgnoreCase("uploaded")) {
						 * populateActivityReports(dir); all_sites.put(dir, "activity"); } }
						 */
						
						if (!all_sites.isEmpty())
							it = sortIteratorDesc((Iterator) all_sites.keys());
						else
							it = null;
						
						System.out.println("Going to populate the summary tables ... ");
						while (it != null && it.hasNext())
						{
							dir = (String) it.next();
							String status = (String) all_sites.get(dir);
							if (status.equalsIgnoreCase("activity"))
							{
							    backup_summaryAndcalllog(dir);
							    //BABU - Comment This if required. 
								populateSelectionCountReports(dir);
								all_sites.put(dir, "selectionCount");
							}
						}

		/*				Calendar cal = Calendar.getInstance();
						int date = cal.get(Calendar.DATE); */
						if (!all_sites.isEmpty())
							it = sortIteratorDesc((Iterator) all_sites.keys());
						else
							it = null;
						while (it != null && it.hasNext())
						{
							dir = (String) it.next();
							String status = (String) all_sites.get(dir);
							if (status.equalsIgnoreCase("selectionCount"))
							{
							    //BABU - Comment This if required
								updateTunesSelectionReports(dir);
								all_sites.put(dir, "tunes");
							}
						}

						if (!all_sites.isEmpty())
							it = sortIteratorDesc((Iterator) all_sites.keys());
						else
							it = null;
						while (it != null && it.hasNext())
						{
							dir = (String) it.next();
							String status = (String) all_sites.get(dir);
							if (status.equalsIgnoreCase("tunes"))
							{
								if (!m_customer.equalsIgnoreCase("tata"))
								    //BABU Comment this if required
									populatePremiumSelectionReports(dir);
								all_sites.put(dir, "premium");
							}
						}

						// recon code
						if (!all_sites.isEmpty())
							it = sortIteratorDesc((Iterator) all_sites.keys());
						else
							it = null;
						while (it != null && it.hasNext())
						{
							dir = (String) it.next();
							String status = (String) all_sites.get(dir);
							if (status.equalsIgnoreCase("premium"))
							{
								if (!m_customer.equalsIgnoreCase("tata"))
								{
								//	compareSubSel(dir);
								//	createDeactiveSelFile(dir);
								}
								all_sites.put(dir, "reconciled");
							}

						}

						if (!all_sites.isEmpty())
							it = sortIteratorDesc((Iterator) all_sites.keys());
						else
							it = null;
						while (it != null && it.hasNext())
						{
							dir = (String) it.next();
							String status = (String) all_sites.get(dir);
							if (status.equalsIgnoreCase("reconciled"))
							{
								Date sysCalDate = getSysDate(false);
	 							if(sysCalDate.after(maxProcessDate))
								{
									Enumeration index = all_siteids.keys();
									while(index.hasMoreElements())
									{
										String name = (String)index.nextElement();
										String value = (String)all_siteids.get(name);
										if(!all_sites.contains(value))
											all_sites.put(value,name);
										flagSysDate=true;
//										logger.info("POOJA POOJA reconciled ::: all_sites AT THE END OF WHILE LOOP::"+all_sites.size());
									}
								}
								else
									all_sites.remove(dir);

								if (!all_sites.isEmpty()){
									it = sortIteratorDesc((Iterator) all_sites.keys());
									logger.info("PROCESSING DATE OF THE REPORTER AT THE END OF MAIN WHILE LOOP ::: maxProcessDate ::: "+maxProcessDate+" AND all_sites.size()::"+all_sites.size());
								}
								else{
									//normal exit to be removed to make it a daemon
								}
							}
						}
					}
					if (m_reconcile_content)
					{
						hutch_categories = getBaseCategories("vodafone");
						idea_categories = getBaseCategories("idea");

						hutch_clips = getBaseClips("vodafone");
						idea_clips = getBaseClips("idea");

						hutch_category_clip_map = getBaseCategoryClipMap("vodafone");
						idea_category_clip_map = getBaseCategoryClipMap("idea");

						hutch_content_release = getBaseContentReleaseDate("vodafone");
						idea_content_release = getBaseContentReleaseDate("idea");
					}

					it = (Iterator) all_siteids.keys();

					StringBuffer sb = null;
					if (m_reconcile_content)
					{
						sb = new StringBuffer();
						sb.append("SITE_NAME, REMARKS\n");
					}

					while (it.hasNext())
					{
						String site_id = (String) it.next();
						String custSite = (String) all_siteids.get(site_id);
	    			         updateMOU(site_id);
						if (m_reconcile_content)
						{
							if (custSite.startsWith("vodafone"))
								reconcileRBTContent(site_id, hutch_categories,hutch_clips,hutch_category_clip_map,hutch_content_release, sb);
							else if (custSite.startsWith("idea"))
								reconcileRBTContent(site_id, idea_categories,idea_clips, idea_category_clip_map,idea_content_release, sb);
						}
						if (m_takeBackup)
						{
							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.DATE, -2);
							Date yes = cal.getTime();
							Tools.SubdirDelete(custSite, yes);
							Tools.SubdirDelete(custSite, m_delete_folder_last_date);
						}
					}

					if (m_reconcile_content)
					{
						String reconcile_file_name = "./log/reconcile"
								+ Tools.getContentDate(Calendar.getInstance().getTime(), "yyyyMMdd") + ".csv";
						File reconcile = new File(reconcile_file_name);
						if (!reconcile.exists())
							reconcile.createNewFile();
						FileOutputStream fout = null;
						fout = new FileOutputStream(reconcile);
						fout.write(sb.toString().getBytes());
						fout.close();
					}
					// PRONEEL Temp remove, dont bother about content

					Tools.deleteOldLogs(m_delete_logs_last_date);
					if (!m_customer.equalsIgnoreCase("tata"))
						checkSQLLDRLogsAndBadFiles();
					try
					{
						morp_conn1 = getMorpheusConnection();

						if (!all_sites.isEmpty())
						{
							logger.info("Gatherer files not found for all sites");

							it = sortIteratorDesc((Iterator) all_sites.keys());

							while (it.hasNext())
							{
								String cust_site = (String) it.next();
								StringTokenizer stk = new StringTokenizer(cust_site,
										"-");
								String site_id = getSiteId(stk.nextToken(), stk
										.nextToken());

								Tools.addToAuditTable(morp_conn1, site_id,ISSUE_CRITICAL, REMARKS_ZIP_NOT_IN_SPIDER,REMARKS_TYPE_GATHERER_ERROR);

	    					   // UpdateFileStatus(site_id, 'N');
								logger.info("Gatherer zip missing in " + cust_site);
							}
						}
					}
					catch (Exception e)
					{
					}
					try
					{
						if (morp_conn1 != null)
						{
							morp_conn1.close();
							morp_conn1.commit();
						}
					}
					catch (Exception sqle)
					{
					}
				}
				catch (Exception e)
				{
					logger.error("", e);
				}
				//BABU TEMP removed - Not required. 
				try
				{
					
					if (m_stmt1 != null)
						m_stmt1.close();
					if (m_stmt2 != null)
						m_stmt2.close();
					if (m_stmt3 != null)
						m_stmt3.close();
					if (m_rs1 != null)
						m_rs1.close();
					if (m_rs2 != null)
						m_rs2.close();
					if (m_conn != null)
					{
						m_conn.commit();
						m_conn.close();
					}
					if (m_sap_conn != null)
						m_sap_conn.close();
				}
				catch (Exception e)
				{
					logger.error("", e);
				}
				logger.info("************* RBTReporter BEGING METHOD FINISHED *******************");
		}
	private Date getSysDate(boolean processDate)
	{
		String _method="getSysDate";
		Date dateSys = null;
		SimpleDateFormat sdf1 = null;
		Calendar maxCal = Tools.getCalendarInstance();
		if(processDate){
			maxCal.add(Calendar.DATE,1);
			sdf1 = new SimpleDateFormat("yyyy/MM/dd");
		}
		else{
			sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		}
		String maxCalStr =sdf1.format(maxCal.getTime());
		try{
			dateSys = sdf1.parse(maxCalStr);
		}catch(Exception e)
		{
			logger.error("", e);
		}
//		logger.info("POOJA POOJA ::: maxProcessDate"+maxProcessDate);
		return dateSys;
	}
    private void createDeactiveSelFile(String dir)
    {
        String _method = "createDeactiveSelFile";
        Calendar cal = Tools.getCalendarInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

        Date end_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            end_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        cal.add(Calendar.DATE, -1);

        File file = new File("./data/" + dir + File.separator
                + sdf.format(cal.getTime())
                + "/db/RBT_SUBSCRIBER_SELECTIONS.TXT");
        Date start_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            start_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        logger.info("Collecting data from "
                + start_data_collection + " to " + end_data_collection);

        StringBuffer sb = new StringBuffer();

        try
        {
            LineNumberReader fin = new LineNumberReader(new FileReader(file));
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String str;

            while ((str = fin.readLine()) != null)
            {
                if (str.startsWith("S"))
                {
                    sb.append(str + "\n");
                    continue;
                }

                StringTokenizer tokens = new StringTokenizer(str, ",");

                for (int i = 0; i < 6; i++)
                {
                    if (tokens.hasMoreTokens())
                        tokens.nextToken();
                }

                Date end_date = new Date();

                try
                {
                    if (tokens.hasMoreTokens())
                        end_date = sdf1.parse(tokens.nextToken());
                    else
                        end_date = null;
                }
                catch (Throwable e)
                {
                    end_date = null;
                }
                // 			logger.info("end_date "+end_date);
                if (end_date != null && end_date.after(start_data_collection)
                        && end_date.before(end_data_collection))
                {
                    sb.append(str + "\n");
                    continue;
                }

            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }

        //write to rbt_subscriber_selections.txt
        try
        {
            File selections = new File(m_bakDir + File.separator + dir
                    + File.separator + "RBT_SUBSCRIBER_SELECTIONS"
                    + sdf.format(cal.getTime()) + ".txt");
            if (!selections.exists())
                selections.createNewFile();

            FileOutputStream fout = new FileOutputStream(selections);
            fout.write(sb.toString().getBytes());
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }

    }

    private void compareSubSel(String cust_site)
    {
        String _method = "compareSubSel";
        Connection morp_conn2 = null;
        StringTokenizer stk = new StringTokenizer(cust_site, "-");
        String site_id = getSiteId(stk.nextToken(), stk.nextToken());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Tools.getCalendarInstance();
        String today = sdf.format(cal.getTime());

        cal.add(Calendar.DATE, -1);
        String yesterday = sdf.format(cal.getTime());

        String query = "select prepaid_yes,active_subscribers from rbt_activity_report where site_id = '"
                + site_id
                + "' and report_date = to_date ('"
                + yesterday
                + "', 'yyyy-MM-dd')";

        logger.info("Executing Query:" + query);

        try
        {
            morp_conn2 = getMorpheusConnection();
			long lPreTime = System.currentTimeMillis();
			 m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            while (m_rs1.next())
            {
                String pre = m_rs1.getString(1);
                int subs = m_rs1.getInt(2);

                if (pre != null && pre.length() > 0)
                {
                    query = "select count(SUBSCRIBER_ID) from RBT_SUBSCRIBER_REPORT "
                            + "where ACTIVATION_DATE <= to_date('"
                            + today
                            + "','YYYY-MM-DD') and END_DATE > to_date('"
                            + today
                            + "','YYYY-MM-DD') and site_id = '"
                            + site_id + "' and prepaid_yes = '" + pre + "'";
                    logger.info("Executing Query:"
                                    + query);

				lPreTime = System.currentTimeMillis();
				m_rs2 = m_stmt2.executeQuery(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

                  
                    if (m_rs2.next())
                    {
                        int count = m_rs2.getInt(1);
                        if (count != subs)
                        {
                            if (pre.equalsIgnoreCase("y"))
                                Tools
                                        .addToAuditTable(
                                                         morp_conn2,
                                                         site_id,
                                                         ISSUE_WARNING,
                                                         "Active Subscribers Count Mismatch Value database - "
                                                                 + count
                                                                 + " for prepaid",
                                                         REMARKS_TYPE_DATA_MISMATCH);
                            else
                                Tools
                                        .addToAuditTable(
                                                         morp_conn2,
                                                         site_id,
                                                         ISSUE_WARNING,
                                                         "Active Subscribers Count Mismatch Value database - "
                                                                 + count
                                                                 + " for postpaid",
                                                         REMARKS_TYPE_DATA_MISMATCH);
                        }
                    }
                }
            }

        }
        catch (Exception e)
        {
            logger.error("", e);
        }

        query = "select prepaid_yes,active_selections from rbt_activity_report where site_id = '"
                + site_id
                + "' and report_date = to_date ('"
                + yesterday
                + "', 'yyyy-MM-dd')";

        logger.info("Executing Query:" + query);

        try
        {
			long lPreTime = System.currentTimeMillis();
				 m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            while (m_rs1.next())
            {
                String pre = m_rs1.getString(1);
                int subs = m_rs1.getInt(2);

                if (pre != null && pre.length() > 0)
                {
                    query = "select count(SUBSCRIBER_ID) from RBT_SUBS_SELECTIONS_REPORT "
                            + "where START_TIME <= to_date('"
                            + today
                            + "','YYYY-MM-DD') and END_TIME > to_date('"
                            + today
                            + "','YYYY-MM-DD') and site_id = '"
                            + site_id + "' and prepaid_yes = '" + pre + "'";

                    logger.info("Executing Query:"
                                    + query);

				lPreTime = System.currentTimeMillis();
				m_rs2 = m_stmt2.executeQuery(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

                   
                    if (m_rs2.next())
                    {
                        int count = m_rs2.getInt(1);
                        if (count != subs)
                        {
                            if (pre.equalsIgnoreCase("y"))
                                Tools
                                        .addToAuditTable(
                                                         morp_conn2,
                                                         site_id,
                                                         ISSUE_WARNING,
                                                         "Active Selections Count Mismatch Value database - "
                                                                 + count
                                                                 + " for prepaid",
                                                         REMARKS_TYPE_DATA_MISMATCH);
                            else
                                Tools
                                        .addToAuditTable(
                                                         morp_conn2,
                                                         site_id,
                                                         ISSUE_WARNING,
                                                         "Active Selections Count Mismatch Value database - "
                                                                 + count
                                                                 + " for postpaid",
                                                         REMARKS_TYPE_DATA_MISMATCH);
                        }
                    }

                }
            }

        }

        catch (Exception e)
        {
            logger.error("", e);
        }
        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }

    private Iterator sortIteratorDesc(Iterator i)
    {
        ArrayList a = new ArrayList();
        while (i.hasNext())
            a.add(i.next());

        Collections.sort(a, new Comparator()
        {
            public int compare(Object a, Object b)
            {
                String aStr, bStr;
                aStr = (String) a;
                bStr = (String) b;
                int val = aStr.compareTo(bStr);
                if (val != 0)
                    return (-val);
                else
                    return val;
            }
        });
        return (a.iterator());
    }

    private void checkSQLLDRLogsAndBadFiles()
    {
        String _method = "checkSQLLDRLogsAndBadFiles";
        Connection morp_conn2 = null;
        try
        {
            morp_conn2 = getMorpheusConnection();
            File[] bad_file_list = getSQLLDRfile(".", ".bad");

            int i = 0;
            for (; i < bad_file_list.length; i++)
            {
                String filename = bad_file_list[i].getName();
                String table_name = "rbt_"
                        + filename.substring(filename.indexOf("_") + 1,
                                             filename.lastIndexOf("_"))
                        + ".txt";

                String site_id = filename
                        .substring(filename.lastIndexOf("_") + 1, filename
                                .indexOf(".bad"));

                Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,
                                      "SQL*Loader Found Invalid Records in "
                                              + table_name + ", see "
                                              + filename,
                                      REMARKS_TYPE_DATA_ERROR);
            }

            //log files
            logger.info("getting log files");

            File[] log_file_list = getSQLLDRfile("./log", ".log");

            i = 0;
            for (; log_file_list != null && i < log_file_list.length; i++)
            {
                boolean write = false;
                boolean records_read = false;
                String filename = log_file_list[i].getName();
                String table_name = "rbt_"
                        + filename.substring(filename.indexOf("_") + 1,
                                             filename.lastIndexOf("_"));

                String site_id = filename
                        .substring(filename.lastIndexOf("_") + 1, filename
                                .indexOf(".log"));

                LineNumberReader fin = new LineNumberReader(new FileReader(
                        log_file_list[i]));
                String str;
                while ((str = fin.readLine()) != null)
                {
                    if (str.trim().startsWith("ORA-01631")
                            || str.trim()
                                    .startsWith("0 Rows successfully loaded"))
                    {
                        write = true;
                        break;
                    }

                    if (str.trim().startsWith("Total logical records read"))
                        records_read = true;
                }

                if (write || !records_read)
                    Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,
                                          "SQL*Loader Failed for Table "
                                                  + table_name,
                                          REMARKS_TYPE_DB_ERROR);
            }

        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }

    private File[] getSQLLDRfile(String file, final String fileExtension)
    {
        String _method = "getSQLLDRfile";
        return (new File(file).listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                String _method = "accept";

                if (name.endsWith(fileExtension))
                {
                    Date date = new Date(new File(dir.getAbsolutePath()
                            + File.separator + name).lastModified());
                    if (date.after(m_reporter_start))
                    {
                        logger.info("file:" + name
                                + " accepted");
                        return true;
                    }
                    else
                        return false;
                }
                else
                {
                    return false;
                }
            }
        }));

    }

    private void reconcileRBTContent(String site_id, Hashtable categories,
            Hashtable clips, Hashtable cat_clip_map, Date content_release,
            StringBuffer sb)
    {
        String _method = "reconcileRBTContent";
        if (compareContentReleaseDates(site_id, content_release))
        {
            Hashtable site_categories = getSiteDetails("category", site_id);
            Hashtable site_clips = getSiteDetails("clip", site_id);
            Hashtable site_cat_clip_map = getSiteCatClipMap(site_id);
            if (site_categories != null && categories != null)
                compare("Category", site_id, site_categories, categories, sb);
            if (site_clips != null && clips != null)
                compare("Clip", site_id, site_clips, clips, sb);
            if (site_cat_clip_map != null && cat_clip_map != null)
                compareCategoryClipMap(site_id, cat_clip_map,
                                       site_cat_clip_map, sb);

        }
    }

    private void compareCategoryClipMap(String site_id, Hashtable base,
            Hashtable site, StringBuffer sb)
    {
        Iterator it = (Iterator) site.keys();
        Connection morp_conn2 = null;
        try
        {
            morp_conn2 = getMorpheusConnection();
        }
        catch (Exception e)
        {
        }
        int count = 0;
        while (it.hasNext())
        {
            int id = ((Integer) it.next()).intValue();
            if (base.containsKey(new Integer(id)))
            {
                ArrayList base_clips = (ArrayList) base.get(new Integer(id));
                ArrayList site_clips = (ArrayList) site.get(new Integer(id));
                for (int i = 0; i < site_clips.size(); i++)
                {
                    if (!base_clips.contains(site_clips.get(i)))
                    {
                        sb.append(getCustomerSite(site_id)
                                + ", Invalid Clip ID " + site_clips.get(i)
                                + " present in Category ID " + id + " \n");
                        count++;
                    }
                }
            }
        }

        if (count > 0)
        {
            Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,
                                  "No. of Invalid Clips Mapped to Categories - "
                                          + count, REMARKS_TYPE_DATA_MISMATCH);
        }
        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }

    private Hashtable getSiteCatClipMap(String site_id)
    {
        String _method = "getSiteCatClipMap";
        Hashtable categoryClips = new Hashtable();
        String query = "select category_id,clip_id from rbt_category_clip_map_report where site_id = "
                + site_id + " order by category_id";
        logger.info("Executing query " + query);
        try
        {
			long lPreTime = System.currentTimeMillis();
			 m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            int prev_cat = 0;
            ArrayList clips = new ArrayList();
            while (m_rs1.next())
            {
                int category_id = m_rs1.getInt("category_id");
                int clip_id = m_rs1.getInt("clip_id");
                if (prev_cat != category_id && prev_cat != 0)
                {
                    categoryClips.put(new Integer(prev_cat), clips);
                    clips = new ArrayList();
                }
                clips.add(new Integer(clip_id));
                prev_cat = category_id;

            }//end of while
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
        return categoryClips;
    }

    private Hashtable getSiteDetails(String type, String site_id)
    {

        String _method = "getSiteDetails";

        /*
         * if(m_morpheusConn == null) { m_morpheusConn =
         * getMorpheusConnection();
         * logger.info("GotConnection"); }
         */
        String table = "rbt_categories_report";

        if (type.equalsIgnoreCase("clip"))
        {
            table = "rbt_clips_report";
        }
        Hashtable array = new Hashtable();

        String query = "select distinct(" + type + "_id), " + type
                + "_name  from " + table + " where site_id ='" + site_id + "'";

        logger.info("Executing Query:" + query);

        try
        {
				long lPreTime = System.currentTimeMillis();
				 m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           

            while (m_rs1.next())
            {
                array.put(new Integer(m_rs1.getInt(1)), m_rs1.getString(2));
            }
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }

        return array;
    }

    private boolean compareContentReleaseDates(String site_id,
            Date base_content_date)
    {
        String _method = "compareContentReleaseDates";
        Connection morp_conn2 = null;
        try
        {
            morp_conn2 = getMorpheusConnection();
        }
        catch (Exception e)
        {
        }
        boolean retVal = true;
        String date = Tools.getContentDate(base_content_date, "ddMMyyyy");

        String query = "select * from rbt_content_release_report where site_id ='"
                + site_id + "' and site_name like '%" + date + "%'";

        logger.info("Executing Query:" + query);

        try
        {
			long lPreTime = System.currentTimeMillis();
				 m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

           

            if (m_rs1.next())
            {
            }
            else
            {
                Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,
                                      "Content Release Done On " + date
                                              + " Not Applied at site",
                                      REMARKS_TYPE_DATA_MISMATCH);
                retVal = false;
            }
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
        return retVal;
    }

    private void compare(String type, String site_id, Hashtable site,
            Hashtable base, StringBuffer sb)
    {
        String _method = "compare";
        int count = 0;
        Connection morp_conn2 = null;
        try
        {
            morp_conn2 = getMorpheusConnection();
        }
        catch (Exception e)
        {
        }
        Iterator it = (Iterator) site.keys();

        while (it.hasNext())
        {
            int id = ((Integer) it.next()).intValue();
            if (!base.containsKey(new Integer(id)))
            {
                sb.append(getCustomerSite(site_id) + ", Invalid " + type + " "
                        + site.get(new Integer(id)) + " with ID " + id
                        + " present \n");
                count++;
            }
        }

        if (count > 0)
        {
            Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,
                                  "No. of Invalid " + type + " - " + count,
                                  REMARKS_TYPE_DATA_MISMATCH);
        }
        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }

    /*
     * getsiteName private String getsiteName(String id) {
     * 
     * String _method ="getsiteName";
     * 
     * if(m_morpheusConn == null) { m_morpheusConn = getMorpheusConnection();
     * logger.info("GotConnection"); } String site_name =
     * null;
     * 
     * String query = "select SITE_NAME from rbt_sites where SITE_ID =
     * '"+id+"'";
     * 
     * logger.info("Executing Query:"+query);
     * 
     * try{ m_rs2= m_morpheusStmt2.executeQuery(query);
     * 
     * while(m_rs2.next()){ site_name = m_rs2.getString("SITE_NAME"); }
     * }catch(SQLException sqle){ logger.error("", sqle); }
     * return site_name; }
     */
    private String getSiteId(String cust, String site)
    {

        String _method = "getSiteId";
        Connection morp_conn2 = null;
        Statement morp_stmt2 = null;
        ResultSet morp_rs2 = null;
        /*
         * if(m_morpheusConn == null) { m_morpheusConn =
         * getMorpheusConnection();
         * logger.info("GotConnection"); }
         */
        String site_id = null;

        String query = "select SITE_ID from rbt_sites where cust_name = '"
                + cust + "' and Lower(site_name) = '" + site.toLowerCase()
                + "'";

        logger.info("Executing Query:" + query);

        try
        {
            morp_conn2 = getMorpheusConnection();
            morp_stmt2 = morp_conn2.createStatement();
			long lPreTime = System.currentTimeMillis();
				 m_rs2 = morp_stmt2.executeQuery(query);

				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            while (m_rs2.next())
            {
                site_id = m_rs2.getString("SITE_ID");
            }
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        try
        {
            if (morp_rs2 != null)
                morp_rs2.close();
            if (morp_stmt2 != null)
                morp_stmt2.close();
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }

        return site_id;
    }

    private void UpdateFileStatus(String site_id, char status)
    {

        String _method = "UpdateFileStatus";
        Connection morp_conn2 = null;
        /*
         * if(morp_conn2 == null) { morp_conn2 = getMorpheusConnection();
         * logger.info("Got Morpheus Connection"); }
         */
        Statement morp_stmt2 = null;
        Calendar cal = Tools.getCalendarInstance();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

        String query = "update reporter_file_status set report_date = to_date('"
                + date
                + "','yyyy-MM-dd') , status = '"
                + status
                + "' , updated_time = sysdate where  cust_id ='" + site_id + "' and ( status != 'Y' or report_date != to_date('"
                + date + "','yyyy-MM-dd'))";

        logger.info("Going to execute Query " + query);

        try
        {
            morp_conn2 = getMorpheusConnection();
            morp_stmt2 = morp_conn2.createStatement();
			long lPreTime = System.currentTimeMillis();
		    morp_stmt2.executeUpdate(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           

        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        try
        {
            if (morp_stmt2 != null)
                morp_stmt2.close();
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }

    private Date getMaxReportDate(String site_id, String table)
    {

        String _method = "getMaxReportDate";

        Date date = null;
        Connection conn = null;
        Statement stmt = null;

        String query = "select max(REPORT_DATE) from " + table
                + " where site_id ='" + site_id + "'";

        logger.info("Executing Query:" + query);

        try
        {
        	conn  = getConnection();
        	stmt = conn.createStatement();
			long lPreTime = System.currentTimeMillis();
		    m_rs1 = stmt.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            

            while (m_rs1.next())
            {
                date = m_rs1.getDate(1);
                System.out.println("the max date is )))))"+date);
                if (date == null)
                {	
                	System.out.println("Entered null in getMaxDate()");
                	Calendar cal = Tools.getCalendarInstance();
                    cal.add(Calendar.DATE, -2);
                    String dateStr = new SimpleDateFormat("yyyy-MM-dd")
                            .format(cal.getTime());
                    try
                    {
                        date = new SimpleDateFormat("yyyy-MM-dd")
                                .parse(dateStr);
                    }
                    catch (ParseException pe)
                    {
                        logger.error("", pe);
                    }
                }
            }
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }

        return date;
    }

    private void populateSelectionCount(String site_id, String cust,
            String report_date)
    {

        String _method = "populateSelectionCount";
        Connection morp_conn2 = null;
        int sub_1_sel = 0;
        int sub_2_sel = 0;
        int sub_3_sel = 0;
        int sub_4_sel = 0;
        int sub_5_sel = 0;
        int sub_more_than5_sel = 0;

        String query = null;
        //In idea query changed by yuva added conditon start_time != end_time
        if (cust.equalsIgnoreCase("idea"))
            query = "SELECT DISTINCT(subscriber_id) , COUNT(subscriber_wav_file) FROM rbt_subs_selections_report "+
                    " WHERE site_id = '"+site_id+"' and "+
					"start_time <= TO_DATE('"+ report_date+ " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss PM') AND "+
					"end_time >= TO_DATE('"+ report_date+ " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss PM') AND "+
					" (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time " +
					"and start_time > ('01-Jan-2005') "+
					"GROUP BY subscriber_id ";
        else if (cust.equalsIgnoreCase("bsnl"))
            query = "SELECT DISTINCT(subscriber_id) , COUNT(subscriber_wav_file) FROM rbt_subs_selections_report "+
                    " WHERE site_id = '"+site_id+"' and "+
					"start_time <= TO_DATE('"+ report_date+ " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss PM') AND "+
					"end_time >= TO_DATE('"+ report_date+ " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss PM') " +
					" and (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time AND "+
					"start_time > ('01-Jan-2005') "+
					"GROUP BY subscriber_id ";
		else
            query = "SELECT DISTINCT(subscriber_id) , COUNT(distinct(subscriber_wav_file)) FROM rbt_subs_selections_report "
                    + " WHERE start_time <= TO_DATE('"
                    + report_date
                    + " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss PM') AND end_time >= TO_DATE('"
                    + report_date
                    + " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss PM') " +
                    "and (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time" +
                    " AND site_id = '"
                    + site_id + "' " + " GROUP BY subscriber_id ";

        logger.info("STARTED POPULATION OF SELECTION COUNT TABLE Going to execute Query " + query);

        try
        {
            morp_conn2 = getMorpheusConnection();
			long lPreTime = System.currentTimeMillis();
		    m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            while (m_rs1.next())
            {
                int no_sel = m_rs1.getInt(2);
                if (no_sel == 1)
                    sub_1_sel++;
                else if (no_sel == 2)
                    sub_2_sel++;
                else if (no_sel == 3)
                    sub_3_sel++;
                else if (no_sel == 4)
                    sub_4_sel++;
                else if (no_sel == 5)
                    sub_5_sel++;
                else
                    sub_more_than5_sel++;
            }
            query = "insert into rbt_selection_count values ('" + site_id
                    + "',to_date('" + report_date + "','yyyy-MM-dd'),'"
                    + sub_1_sel + "','" + sub_2_sel + "','" + sub_3_sel + "','"
                    + sub_4_sel + "','" + sub_5_sel + "','"
                    + sub_more_than5_sel + "')";

            //			logger.info("Executing Query:"+query);

			 morp_conn2 = getMorpheusConnection();
			lPreTime = System.currentTimeMillis();
		    m_stmt1.executeUpdate(query);
			lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            

         
	        logger.info("POOJA POOJA ::: ENDED POPULATION OF SELECTION COUNT TABLE");

	        try {
				m_conn.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
            Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,
                                  REMARKS_SELECTION_COUNT + report_date,
                                  REMARKS_TYPE_REPORT_ERROR);
        }
        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }

    private void updateMOU(String site_id)
    {
        String _method = "updateMOU";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Tools.getCalendarInstance();
        String today = sdf.format(c.getTime());
        c.add(Calendar.DATE, -1);
        String yesterday = sdf.format(c.getTime());
//      getMOU(site_id, yesterday, today);
        String date = "2005-06-20";
        String query = "select report_date from rbt_activity_report where MOU=0 and report_date > to_date('"
                + date + "', 'yyyy-MM-dd') and site_id='" + site_id + "'";

        logger.info("Executing query " + query);
        try
        {
			long lPreTime = System.currentTimeMillis();
		     m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            boolean done = true;
            while (m_rs1.next())
            {
                Date report_date = m_rs1.getDate("report_date");
                c.setTime(report_date);
                c.add(Calendar.DATE, 1);
                String rpt_date = sdf.format(report_date);
                String next_date = sdf.format(c.getTime());
                getMOU(site_id, rpt_date, next_date);
            }
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
    }

    private void updateCDR_Data(String site_id, String report_date)
    {

        String _method = "updateCDR_Data";
        try
        {
            Calendar cal = Calendar.getInstance();
            Date repDate = new SimpleDateFormat("yyyy-MM-dd")
                    .parse(report_date);
            cal.setTime(repDate);
            String date = new SimpleDateFormat("yyyy-MMM-dd").format(cal.getTime());
            String query = "select sum(Active_subscribers) from RBT_ACTIVITY_REPORT where REPORT_DATE = to_date('"+ report_date+ "','yyyy-MM-dd') and site_id = '"+ site_id +"'";
			long lPreTime = System.currentTimeMillis();
			m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            while (m_rs1.next())
            {
                String subs = m_rs1.getString(1);
                query = "update rbt_cdr_entries set active_subscribers ='"+ subs + "' where cdr_date like '" + date+ "%' and site_id ='" + site_id + "'";
				lPreTime = System.currentTimeMillis();
				m_stmt2.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
            }
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
    }

    private void updateTunesSelectionReports(String dir)
    {
        String _method = "updateTunesSelectionReports";

        StringTokenizer stk = new StringTokenizer(dir, "-");
        String cust = stk.nextToken();
        String site = stk.nextToken();
        String site_id = getSiteId(cust, site);
        /*
         * Calendar cal = Calendar.getInstance(); SimpleDateFormat sdf = new
         * SimpleDateFormat("yyyy-MM-dd"); String next_date =
         * sdf.format(cal.getTime()); cal.add(Calendar.DATE,-1); String
         * report_date = sdf.format(cal.getTime());
         * updateSelectionsReport(site_id,report_date);
         */
		Date tuneDateObt = (Date)hash_tuneDate.get(site_id);
        logger.info("tuneDateObt" + tuneDateObt);
        Calendar next = Calendar.getInstance();
        next.setTime(tuneDateObt);
        next.add(Calendar.DATE, 2);
        Calendar cal = Calendar.getInstance();
        cal.setTime(tuneDateObt);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        cal.add(Calendar.DATE, 1);
//        while (next.getTime().before(Calendar.getInstance().getTime()))
		//while (next.getTime().before(maxProcessDate))
        while (next.getTime().compareTo(maxProcessDate) <= 0)
        {
            String report_date = sdf.format(cal.getTime());
            String next_date = sdf.format(next.getTime());
            updateSelectionsReport(site_id, report_date);
            cal.add(Calendar.DATE, 1);
            next.add(Calendar.DATE, 1);
        }
    }

    private void populateTotalSelReports(String dir)
    {
        String _method = "populateTotalSelReports";

        StringTokenizer stk = new StringTokenizer(dir, "-");
        String cust = stk.nextToken();
        String site = stk.nextToken();
        String site_id = getSiteId(cust, site);
        /*
         * Calendar cal = Calendar.getInstance(); SimpleDateFormat sdf = new
         * SimpleDateFormat("yyyy-MM-dd"); String next_date =
         * sdf.format(cal.getTime()); cal.add(Calendar.DATE,-1); String
         * report_date = sdf.format(cal.getTime());
         * populateSelectionsReport(site_id,report_date,next_date);
         */
        tuneDate = getMaxReportDate(site_id, "rbt_tunes_selections_report");
		hash_tuneDate.put(site_id,tuneDate);
        logger.info("hash_tuneDate : "+hash_tuneDate+" and tuneDate : " + tuneDate);
        Calendar next = Calendar.getInstance();
        next.setTime(tuneDate);
        next.add(Calendar.DATE, 2);
        Calendar cal = Calendar.getInstance();
        cal.setTime(tuneDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        cal.add(Calendar.DATE, 1);
//      while (next.getTime().before(Calendar.getInstance().getTime()))
		//while (next.getTime().before(maxProcessDate))
        while (next.getTime().compareTo(maxProcessDate) <= 0)
        {
            String report_date = sdf.format(cal.getTime());
            String next_date = sdf.format(next.getTime());
            populateSelectionsReport(site_id, report_date, next_date);
            cal.add(Calendar.DATE, 1);
            next.add(Calendar.DATE, 1);
        }
    }

    private void populateSelectionCountReports(String dir)
    {
        String _method = "populateSelectionCountReports";

        StringTokenizer stk = new StringTokenizer(dir, "-");
        String cust = stk.nextToken();
        String site = stk.nextToken();
        String site_id = getSiteId(cust, site);
        Date max_date = getMaxReportDate(site_id, "rbt_selection_count");
        logger.info("max date" + max_date);
        Calendar next = Calendar.getInstance();
        next.setTime(max_date);
        next.add(Calendar.DATE, 2);
        Calendar cal = Calendar.getInstance();
        cal.setTime(max_date);
        cal.add(Calendar.DATE, 1);
//      while (next.getTime().before(Calendar.getInstance().getTime()))
		//while (next.getTime().before(maxProcessDate))
        while (next.getTime().compareTo(maxProcessDate) <= 0)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String report_date = sdf.format(cal.getTime());
            populateSelectionCount(site_id, cust, report_date);
            cal.add(Calendar.DATE, 1);
            next.add(Calendar.DATE, 1);
        }
    }

	private void populateSelectionClassMapReport(String dir)
	{
		String _method = "populateSelectionClassMapReport";
		try{
			StringTokenizer stk = new StringTokenizer(dir, "-");
			String cust = stk.nextToken();
			String site = stk.nextToken();
			String site_id = getSiteId(cust, site);

			Date max_date = getMaxReportDate(site_id, "RBT_SELECTION_CLASS_MAP_REPORT");
			logger.info("max date" + max_date);
			Calendar next = Calendar.getInstance();
			next.setTime(max_date);
			next.add(Calendar.DATE, 2);
			Calendar cal = Calendar.getInstance();
			cal.setTime(max_date);
			cal.add(Calendar.DATE, 1);
            
			while (cal.getTime().compareTo(RBTReporter.maxProcessDate!=null?RBTReporter.maxProcessDate:Calendar.getInstance().getTime())<0)
			{

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String report_date = sdf.format(cal.getTime());
				String next_date = sdf.format(next.getTime());

				String query = "select prepaid_yes,SELECTED_BY, CLASS_TYPE, count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING "+
				"where site_id = '"+ site_id+ "' and "+
				"start_time >= to_date ('"+ report_date+ "','YYYY-MM-DD') and start_time < to_date ('"+ next_date+ "','YYYY-MM-DD') and  "+
				"SEL_STATUS = 'B' and next_charging_date is not null group by prepaid_yes,SELECTED_BY,CLASS_TYPE";
				logger.info("SELECTION MODE - CLASS MAIN QUERY ::"+query);
				long lPreTime = System.currentTimeMillis();
				m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
               
				while(m_rs1.next())
				{
                    char prepaid = m_rs1.getString("PREPAID_YES").charAt(0);
                    String selBy = m_rs1.getString("SELECTED_BY");
					String classType = m_rs1.getString("CLASS_TYPE");
                    int subs = m_rs1.getInt(4);
					String strquery = "insert into RBT_SELECTION_CLASS_MAP_REPORT(SITE_ID,PREPAID_YES,REPORT_DATE,SELECTED_BY,CLASS_TYPE,SELACT_COUNT) "+
					"VALUES("+site_id+",'"+prepaid+"',to_date('"+ report_date+ "','YYYY-MM-DD'),'"+selBy+"','"+classType+"',"+subs+")";
					logger.info("SELECTION MODE - CLASS MAIN QUERY ::"+strquery);
					lPreTime = System.currentTimeMillis();
					m_stmt2.executeQuery(strquery);
					lPostTime = System.currentTimeMillis();
					logger.info("PERFMON: Query=>"+ strquery+"&&&TimeTaken="+((lPostTime - lPreTime)));
					
				}
				cal.add(Calendar.DATE, 1);
				next.add(Calendar.DATE, 1);
			}
		}catch(Exception e)
		{
             logger.error("", e);
		}
	}
//Started:-
	private boolean populateActivityReportSummary(String dir, String site_id)
	{
		String _method = "populateActivityReportSummary";
		boolean flagSummary = true;
		try
		{
			Calendar calDay = Tools.getCalendarInstance();
			calDay.add(Calendar.DATE, -1);
			String day = new SimpleDateFormat("yyyy-MMM-dd").format(calDay.getTime());
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String report_date = sdf.format(calDay.getTime());
 			File dbTemp_act = new File("./" + m_db_root + File.separator+ dir + File.separator + day+ File.separator + m_db_end+ File.separator+"DLY_ACT_"+m_db_collection_days+".txt");
			File dbTemp_deact = new File("./" + m_db_root + File.separator+ dir + File.separator + day+ File.separator + m_db_end+ File.separator+"DLY_DACT_"+m_db_collection_days+".txt");
			if (!dbTemp_act.exists() || !dbTemp_deact.exists())
			{
				return false;
			}
			populateDailyActivitySummary(site_id,report_date,dbTemp_act,"A");
			populateDailyActivitySummary(site_id,report_date,dbTemp_deact,"D");

 		}
		catch(Exception e)
		{
//			System.out.println("ERROR e:::"+e);
            logger.error("", e);
			flagSummary = false;
		}
		return flagSummary;
	}
	private void populateDailyActivitySummary(String site_id,String report_date, File dbTemp,String type)
	{
		String _method = "populateDailyActivitySummary";
		FileReader objFileReader =null;
		BufferedReader brFile =null;
		try
		{
			objFileReader = new FileReader(dbTemp);
			brFile = new BufferedReader(objFileReader);
			String strReadLine=null;
			String mode = null;
			int value = 0;
			String query = null;
			while ((strReadLine = brFile.readLine()) != null)
			{
				strReadLine = strReadLine.trim();
				if(strReadLine.startsWith("ACTIVATED_BY") || strReadLine.startsWith("DEACTIVATED_BY")) //if header
					continue;
				StringTokenizer strToken = new StringTokenizer(strReadLine,",");
				if (strToken.hasMoreTokens())
				{
					try{
						mode = (String)strToken.nextToken();
					}catch(Exception e)
					{
						logger.error("", e);
						mode = "unknown";
					}
				}
				if (strToken.hasMoreTokens())
				{
					try{
					value = Integer.parseInt((String)strToken.nextToken());
					}catch(Exception parseE)
					{
						logger.error("", parseE);
						value = 0;
					}
				}
				query = "update rbt_activity_summary_report set VALUE="+value+" where "+ 
				"site_id = '"+site_id+"' and report_date=to_date('"+report_date+"','YYYY-MM-DD') and "+ 
				"prepaid_yes = 'n' and TYPE_PARAM='"+type+"' and MODE_PARAM='"+mode+"' ";
				long lPreTime = System.currentTimeMillis();
				int result = m_stmt1.executeUpdate(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
				
				if(result == 0)
				{
					query = "insert into rbt_activity_summary_report(site_id,report_date,prepaid_yes,TYPE_PARAM,MODE_PARAM,VALUE) "+
					"values("+site_id+",to_date('"+report_date+"','YYYY-MM-DD'),'n','"+type+"','"+mode+"',"+value+")";
					lPreTime = System.currentTimeMillis();
					m_stmt1.executeUpdate(query);
					lPostTime = System.currentTimeMillis();
					logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
					
				}
			}
		}catch(Exception e)
		{
            logger.error("", e);
		}
		finally 
		{
			try{
				brFile.close();
				objFileReader.close();
			}catch(Exception e)
			{
				logger.error("", e);
			}
		}
	}
	//ENDED Completely.
	
	//yuvaaaa
    private void populateActivityReports(String dir)
    {
        String _method = "populateActivityReports";
        String query;
        String queryUpdate = null;
        String queryParam = null;
        int len = 0;
        StringTokenizer stk = new StringTokenizer(dir, "-");
        String cust = stk.nextToken();
        String site = stk.nextToken();
        boolean flagAct = true;
        boolean flagDeact = true;
        boolean flagSelect = true;
        boolean flagClassSel = true;
        String site_id = getSiteId(cust, site);
        Calendar calDay =Tools.getCalendarInstance();
		calDay.add(Calendar.DATE, -1);
        String day = new SimpleDateFormat("yyyy-MMM-dd").format(calDay
                .getTime());

  		UpdateFileStatus(site_id, 'Y');

        Date max_date = getMaxReportDate(site_id, "rbt_activity_report");
        //    	max_date = getMaxReportDate(site_id, "rbt_activity_report");

        logger.info("max date" + max_date);
        Calendar next = Calendar.getInstance();

        next.setTime(max_date);

        next.add(Calendar.DATE, 2);

        Calendar cal = Calendar.getInstance();
        cal.setTime(max_date);

        cal.add(Calendar.DATE, 1);

     //   while (next.getTime().before(Calendar.getInstance().getTime()))
		  while (next.getTime().compareTo(maxProcessDate) <= 0)
        {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String report_date = sdf.format(cal.getTime());
            String next_date = sdf.format(next.getTime());
            // creating prepaid/postpaid entries
            try
            {
                query = "insert into RBT_ACTIVITY_REPORT (site_id, report_date, prepaid_yes) values("+ site_id+ ",to_date('"+ report_date+ "','YYYY-MM-DD'),'y')";
                //				logger.info("Going to execute Query
                // "+query);
				long lPreTime = System.currentTimeMillis();
				m_stmt1.executeUpdate(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
                query = "insert into RBT_ACTIVITY_REPORT (site_id, report_date, prepaid_yes) values("
                        + site_id
                        + ",to_date('"
                        + report_date
                        + "','YYYY-MM-DD'),'n')";
                //				logger.info("Going to execute Query
                // "+query);long lPreTime = System.currentTimeMillis();
				lPreTime = System.currentTimeMillis();
				m_stmt1.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
            }
            catch (SQLException sqle)
            {
                logger.error("", sqle);
                //				Tools.addToAuditTable(m_morpheusConn, site_id, ISSUE_WARNING,
                // REMARKS_ACTIVITY+report_date, REMARKS_TYPE_REPORT_ERROR);
            }

            //updateActivations
            int totalPreAct = 0;
            int totalPostAct = 0;
            //Changed Staging
            if (m_customer.equalsIgnoreCase("tata"))
                query = "select PREPAID_YES,ACTIVATED_BY,count(SUBSCRIBER_ID) from RBT_SUBSCRIBER_staging "
                        + "where (start_DATE >= to_date('"
                        + report_date
                        + "','YYYY-MM-DD') "
                        + "and start_DATE < to_date('"
                        + next_date
                        + "','YYYY-MM-DD')) and "
                        + "(DEACTIVATED_BY IS NULL or DEACTIVATED_BY != 'NA') and site_id = '"
                        + site_id + "' group by prepaid_yes,activated_by";
            else if (m_customer.equalsIgnoreCase("esia"))
                query = "select prepaid_yes,ACTIVATED_BY, count(SUBSCRIBER_ID) from rbt_subscriber_staging where "
                        + "subscription_yes = 'B' and site_id = '"
                        + site_id
                        + "' and activation_date is not null and "
                        + "(deactivated_by is null or (deactivated_by != 'NA' and deactivated_by != 'AF')) "
                        + "and activation_date is not null and ((activation_date >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') "
                        + "and activation_date < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD'))) group by prepaid_yes,activated_by";

            else
                query = "select PREPAID_YES,ACTIVATED_BY,count(SUBSCRIBER_ID) from RBT_SUBSCRIBER_staging "
                        + "where activation_date is not null and (ACTIVATION_DATE >= to_date('"
                        + report_date
                        + "','YYYY-MM-DD') "
                        + "and ACTIVATION_DATE < to_date('"
                        + next_date
                        + "','YYYY-MM-DD')) and "
                        + "(DEACTIVATED_BY IS NULL or (deactivated_by != 'NA' and deactivated_by != 'AF')) and site_id = '"
                        + site_id + "' group by prepaid_yes,activated_by";

            logger.info("MAIN ACTIVATION QUERY: " + query);
            try
            {
				long lPreTime = System.currentTimeMillis();
				m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                

                int pre_other_act = 0;
                int post_other_act = 0;
                while (m_rs1.next())
                {
                    char prepaid = m_rs1.getString("PREPAID_YES").charAt(0);
                    String actBy = m_rs1.getString("ACTIVATED_BY");
                    int subs = m_rs1.getInt(3);
                    if (prepaid == 'y' || prepaid == 'n')
                    {
                        if (prepaid == 'y')
                            totalPreAct = totalPreAct + subs;
                        else
                            totalPostAct = totalPostAct + subs;

                        queryParam = "select PARAMETER from RBT_ACTIVATION_PARAMETERS where TYPE like '%Act%'";
						lPreTime = System.currentTimeMillis();
						m_rs2 = m_stmt2.executeQuery(queryParam);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ queryParam+"&&&TimeTaken="+((lPostTime - lPreTime)));
                       
                        while (m_rs2.next())
                        {
                            String name = m_rs2.getString("PARAMETER");
                            String delimeter = ",";
                            StringTokenizer st = new StringTokenizer(name,
                                    delimeter);
                            len = st.countTokens();
                            boolean flag = true;
                            String firstParameters = null;
                            if (len > 1)
                            {
                                while (st.hasMoreTokens())
                                {
                                    String param = st.nextToken();
                                    if (flag == true)
                                    {
                                        String first = param;
                                        first = first.replaceAll("-", "_");
                                        firstParameters = first;
                                        flag = false;
                                    }
                                    if (actBy.equalsIgnoreCase(param))
                                    {
                                        flagAct = false;
                                        queryUpdate = "update RBT_ACTIVITY_REPORT set "
                                                + firstParameters
                                                + "_ACTIVATIONS= "
                                                + subs
                                                + " where site_id= "
                                                + site_id
                                                + " and report_date=to_date('"
                                                + report_date
                                                + "','YYYY-MM-DD') and prepaid_yes ='"
                                                + prepaid + "'";
                                        //										Tools.logDetail(_class, _method,
                                        // "QUERY UPDATE
                                        // ACTIVATIONS:"+queryUpdate);
										lPreTime = System.currentTimeMillis();
										m_stmt3.executeUpdate(queryUpdate);
										lPostTime = System.currentTimeMillis();
										logger.info("PERFMON: Query=>"+ queryUpdate+"&&&TimeTaken="+((lPostTime - lPreTime)));
                                        

                                    }
                                }
                            }
                            else if (len == 1 && actBy.equalsIgnoreCase(name))
                            {
                                flagAct = false;
                                actBy = actBy.replaceAll("-", "_");
                                queryUpdate = "update RBT_ACTIVITY_REPORT set "
                                        + actBy + "_ACTIVATIONS= " + subs
                                        + " where site_id= " + site_id
                                        + " and report_date=to_date('"
                                        + report_date
                                        + "','YYYY-MM-DD') and prepaid_yes ='"
                                        + prepaid + "'";
                                logger.info("QUERY UPDATE ACTIVATIONS:"
                                                        + queryUpdate);
						lPreTime = System.currentTimeMillis();
						m_stmt3.executeUpdate(queryUpdate);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ queryUpdate+"&&&TimeTaken="+((lPostTime - lPreTime)));
                               

                            }

                        } //while ended of rbt_activity_parameters
                        if (flagAct == true)
                        {
                            if (prepaid == 'y')
                                pre_other_act += subs;
                            else if (prepaid == 'n')
                                post_other_act += subs;

                        }
                        flagAct = true;

                    }

                }//end of while

                query = "update RBT_ACTIVITY_REPORT set OTHER_ACTIVATIONS= "
                        + pre_other_act + " where site_id= " + site_id
                        + " and report_date=to_date('" + report_date
                        + "','YYYY-MM-DD') and prepaid_yes ='y'";
						lPreTime = System.currentTimeMillis();
						m_stmt1.executeUpdate(query);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

               
				logger.info("POOJA pre_other_act :"+pre_other_act);

                query = "update RBT_ACTIVITY_REPORT set OTHER_ACTIVATIONS= "
                        + post_other_act + " where site_id= " + site_id
                        + " and report_date=to_date('" + report_date
                        + "','YYYY-MM-DD') and prepaid_yes ='n'";
				lPreTime = System.currentTimeMillis();
				m_stmt1.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
               
				logger.info("POOJA post_other_act :"+post_other_act);

            }
            catch (SQLException sqle)
            {
                logger.error("", sqle);
            }
            catch (Exception E)
            {
                logger.error("", E);
            }

			//updateDeactivations
            int totalPreDeact = 0;
            int totalPostDeact = 0;
            //changed Staging
            if (m_customer.equalsIgnoreCase("esia"))
                query = "select prepaid_yes,DEACTIVATED_BY,count(SUBSCRIBER_ID) from rbt_subscriber_staging "
                        + "where subscription_yes = 'X' and site_id = '"
                        + site_id
                        + "' and activation_date is not null and "
                        + "(deactivated_by is not null and deactivated_by != 'NA' and deactivated_by != 'AF') "
                        + "and ((end_date >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') and "
                        + "end_date < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD'))) group by prepaid_yes,deactivated_by";
            else if (m_customer.equalsIgnoreCase("tata"))
                query = "select PREPAID_YES,DEACTIVATED_BY,count(SUBSCRIBER_ID) from RBT_SUBSCRIBER_staging "
                        + "where DEACTIVATED_BY is not NULL and DEACTIVATED_BY != 'AUX' and DEACTIVATED_BY != 'NEFX' and "
                        + "DEACTIVATED_BY != 'NA' and "
                        + "(END_DATE >= to_date('"
                        + report_date
                        + "','YYYY-MM-DD') and END_DATE <to_date('"
                        + next_date
                        + "','YYYY-MM-DD')) "
                        + "and site_id = '"
                        + site_id
                        + "' group by prepaid_yes,deactivated_by";
            else
                query = "select PREPAID_YES,DEACTIVATED_BY,count(SUBSCRIBER_ID) from RBT_SUBSCRIBER_staging "
                        + "where DEACTIVATED_BY is not NULL and DEACTIVATED_BY != 'AUX' and DEACTIVATED_BY != 'NEFX' and "
                        + "DEACTIVATED_BY != 'NA' and DEACTIVATED_BY != 'AF' and activation_date is not null and "
                        + "(END_DATE >= to_date('"
                        + report_date
                        + "','YYYY-MM-DD') and END_DATE <to_date('"
                        + next_date
                        + "','YYYY-MM-DD')) "
                        + "and site_id = '"
                        + site_id
                        + "' group by prepaid_yes,deactivated_by";

            logger.info("MAIN DEACTIVATION QUERY:"
                            + query);

            try
            {

                long lPreTime = System.currentTimeMillis();
			    m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
               
                int pre_other_deact = 0;
                int post_other_deact = 0;
                while (m_rs1.next())
                {
                    char prepaid = m_rs1.getString("PREPAID_YES").charAt(0);
                    String deActBy = m_rs1.getString("DEACTIVATED_BY");
                    int subs = m_rs1.getInt(3);
                    if (prepaid == 'y' || prepaid == 'n')
                    {
                        if (prepaid == 'y')
                            totalPreDeact = totalPreDeact + subs;
                        else
                            totalPostDeact = totalPostDeact + subs;

                        queryParam = "select PARAMETER from RBT_ACTIVATION_PARAMETERS where TYPE like '%Deact%'";
						lPreTime = System.currentTimeMillis();
						m_rs2 = m_stmt2.executeQuery(queryParam);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ queryParam+"&&&TimeTaken="+((lPostTime - lPreTime)));
                       
                        while (m_rs2.next())
                        {
                            String name = m_rs2.getString(1);
                            String delimeter = ",";
                            StringTokenizer st = new StringTokenizer(name,
                                    delimeter);
                            len = st.countTokens();
                            boolean flag = true;
                            String firstParameters = null;
                            if (len > 1)
                            {
                                while (st.hasMoreTokens())
                                {
                                    String param = st.nextToken();
                                    if (flag == true)
                                    {
                                        firstParameters = param;
                                        firstParameters = firstParameters
                                                .replaceAll("-", "_");
                                        flag = false;
                                    }
                                    if (deActBy.equalsIgnoreCase(param))
                                    {
                                        flagDeact = false;
                                        queryUpdate = "update RBT_ACTIVITY_REPORT set "
                                                + firstParameters
                                                + "_DEACTIVATIONS= "
                                                + subs
                                                + " where site_id= "
                                                + site_id
                                                + " and report_date=to_date('"
                                                + report_date
                                                + "','YYYY-MM-DD') and prepaid_yes ='"
                                                + prepaid + "'";
                                        //										Tools.logDetail(_class, _method,
                                        // "QUERY UPDATE
                                        // DEACTIVATION:"+queryUpdate);
										lPreTime = System.currentTimeMillis();
										m_stmt3.executeUpdate(queryUpdate);
										lPostTime = System.currentTimeMillis();
										logger.info("PERFMON: Query=>"+ queryUpdate+"&&&TimeTaken="+((lPostTime - lPreTime)));
                                        

                                    }
                                }
                            }
                            else if (len == 1 && deActBy.equalsIgnoreCase(name))
                            {
                                flagDeact = false;
                                name = name.replaceAll("-", "_");
                                queryUpdate = "update RBT_ACTIVITY_REPORT set "
                                        + name + "_DEACTIVATIONS= " + subs
                                        + " where site_id= " + site_id
                                        + " and report_date=to_date('"
                                        + report_date
                                        + "','YYYY-MM-DD') and prepaid_yes ='"
                                        + prepaid + "'";
                                //								logger.info("QUERY
                                // UPDATE DEACTIVATION:"+queryUpdate);
										lPreTime = System.currentTimeMillis();
										m_stmt3.executeUpdate(queryUpdate);
										lPostTime = System.currentTimeMillis();
										logger.info("PERFMON: Query=>"+ queryUpdate+"&&&TimeTaken="+((lPostTime - lPreTime)));
                              

                            }
                        }//while end of rbt_activity_parameters
                        if (flagDeact == true)
                        {
                            if (prepaid == 'y')
                                pre_other_deact += subs;
                            else if (prepaid == 'n')
                                post_other_deact += subs;
                        }
                        flagDeact = true;

                    }

                }//end of while

                query = "update RBT_ACTIVITY_REPORT set OTHER_DEACTIVATIONS = "
                        + pre_other_deact + " where site_id = " + site_id
                        + " and report_date=to_date('" + report_date
                        + "','YYYY-MM-DD') and prepaid_yes ='y'";
				lPreTime = System.currentTimeMillis();
				m_stmt1.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
               
				logger.info("POOJA pre_other_deact :"+pre_other_deact);
                query = "update RBT_ACTIVITY_REPORT set OTHER_DEACTIVATIONS = "
                        + post_other_deact + " where site_id = " + site_id
                        + " and report_date=to_date('" + report_date
                        + "','YYYY-MM-DD') and prepaid_yes ='n'";
						lPreTime = System.currentTimeMillis();
						m_stmt1.executeUpdate(query);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
				logger.info("POOJA post_other_deact :"+post_other_deact);

            }
            catch (SQLException sqle)
            {
                logger.error("", sqle);
            }
            catch (Exception E)
            {
                logger.error("", E);
            }
 /*update DeActivation count for subscriber who got act - deact -act (case - first activation not on the same day ):- 
			int pre_other_value =0;
			int post_other_value =0;
			if(m_customer.equalsIgnoreCase("esia") || m_customer.equalsIgnoreCase("hutch"))
			{
				if(m_customer.equalsIgnoreCase("esia"))
					query = "select prepaid_yes, count(SUBSCRIBER_ID) from rbt_subscriber_staging where "
                        + "subscription_yes = 'B' and site_id = '"
                        + site_id
                        + "' and activation_date is not null and "
                        + "(LAST_DEACTIVATION_INFO is not null and LAST_DEACTIVATION_INFO != 'NA' and LAST_DEACTIVATION_INFO != 'AF') "
                        + "and ((LAST_DEACTIVATION_DATE >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') "
                        + "and LAST_DEACTIVATION_DATE < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD'))) group by prepaid_yes";
				if(m_customer.equalsIgnoreCase("hutch"))
					query = "select prepaid_yes, count(SUBSCRIBER_ID) from rbt_subscriber_staging where "
                        + "site_id = '"
                        + site_id
                        + "' and activation_date is not null and "
                        + "(LAST_DEACTIVATION_INFO is not null and LAST_DEACTIVATION_INFO != 'NA' and LAST_DEACTIVATION_INFO != 'AF') "
                        + "and ((LAST_DEACTIVATION_DATE >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') "
                        + "and LAST_DEACTIVATION_DATE < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD'))) group by prepaid_yes";


	            logger.info("MAIN ACTIVATION WITH OTHER COUNT ONLY QUERY:"+ query);
				try
				{

					m_rs1 = m_stmt1.executeQuery(query);
					while (m_rs1.next())
					{
						char prepaid = m_rs1.getString("PREPAID_YES").charAt(0);
						logger.info("!!!!!!!!!!!!!!!!!!!!!!!!! POOJA POOJA !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					 	if(prepaid == 'y')
						{
							pre_other_value=m_rs1.getInt(2);
							logger.info("POOJA POOJA ::: pre_other_value ::: "+pre_other_value);
//							query ="update rbt_activity_report set OTHER_ACTIVATIONS=OTHER_ACTIVATIONS+"+pre_other_value+" where site_id= " + site_id + " and "+
//									"report_date=to_date('"+ report_date+ "','YYYY-MM-DD') and prepaid_yes ='"+ prepaid + "'";
//							m_stmt2.executeUpdate(query);
//							logger.info("POOJA POOJA ::: OTHER_ACTIVATIONS :: pre_other_value ::: "+query);
//							totalPreAct=totalPreAct+pre_other_value;  
							query ="update rbt_activity_report set OTHER_DEACTIVATIONS=OTHER_DEACTIVATIONS+"+pre_other_value+" where site_id= " + site_id + " and "+
									"report_date=to_date('"+ report_date+ "','YYYY-MM-DD') and prepaid_yes ='"+ prepaid + "'";
							m_stmt2.executeUpdate(query);
							logger.info("POOJA POOJA ::: OTHER_DEACTIVATIONS :: pre_other_value ::: "+query);
							totalPreDeact=totalPreDeact+pre_other_value;  
						}
						else
						{
							post_other_value=m_rs1.getInt(2);
							logger.info("POOJA POOJA ::: post_other_value ::: "+post_other_value);
 	//						query ="update rbt_activity_report set OTHER_ACTIVATIONS=OTHER_ACTIVATIONS+"+post_other_value+" where site_id= " + site_id + " and "+
	//								"report_date=to_date('"+ report_date+ "','YYYY-MM-DD') and prepaid_yes ='"+ prepaid + "'";
	//						m_stmt2.executeUpdate(query);
	//						logger.info("POOJA POOJA ::: OTHER_ACTIVATIONS:::pre_other_value ::: "+query);
	//						totalPostAct=totalPostAct+post_other_value;  
 
							query ="update rbt_activity_report set OTHER_DEACTIVATIONS=OTHER_DEACTIVATIONS+"+post_other_value+" where site_id= " + site_id + " and "+
									"report_date=to_date('"+ report_date+ "','YYYY-MM-DD') and prepaid_yes ='"+ prepaid + "'";
							m_stmt2.executeUpdate(query);
							logger.info("POOJA POOJA :::OTHER_DEACTIVATIONS::: pre_other_value ::: "+query);
							totalPostDeact=totalPostDeact+post_other_value;  
						} 
					}
				}catch(Exception e)
				{
					logger.error("", e);
				}

			} */

            // Biranchi - updateDaysSelections(site_id,report_date,next_date);
            int preSelAdded = 0;
            int postSelAdded = 0;
            if (m_customer.equalsIgnoreCase("esia"))
            {
                query = "select prepaid_yes,SELECTED_BY, count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING where "
                        + "start_time >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') and start_time < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD') "
                        + "and site_id = '"
                        + site_id
                        + "' and (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time and " +
                        		"SEL_STATUS = 'B' and next_charging_date is not null group by prepaid_yes,SELECTED_BY";
            }
            else if (m_customer.equalsIgnoreCase("idea"))
            {
               /* query = "select prepaid_yes,SELECTED_BY, count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING where "+
                        "site_id = '"+site_id+"' and start_time >= to_date ('"+ report_date+ "','YYYY-MM-DD') and "+
						"start_time < to_date ('"+ next_date+ "','YYYY-MM-DD') and start_time > ('01-Jan-2005') and "+
						"((end_time - start_time)>'0.125') and next_charging_date is not null group by prepaid_yes,SELECTED_BY";*/
            	
            	 query = "select prepaid_yes,SELECTED_BY, count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING where "+
                 "site_id = '"+site_id+"' and start_time >= to_date ('"+ report_date+ "','YYYY-MM-DD') and "+
					"start_time < to_date ('"+ next_date+ "','YYYY-MM-DD') and start_time > ('01-Jan-2005') and "+
					" (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time and " +
					" next_charging_date is not null group by prepaid_yes,SELECTED_BY";
            	
            }
            else if (m_customer.equalsIgnoreCase("bsnl"))
            {
                query = "select prepaid_yes,SELECTED_BY, count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING where "
                        + "start_time >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') and start_time < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD') "
                        + "and site_id = '"
                        + site_id
                        + "' and (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time and " +
                        		"start_time > ('01-Jan-2005') group by prepaid_yes,SELECTED_BY";

            }
            else
            {
                query = "select prepaid_yes,SELECTED_BY, count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING where "
                        + "start_time >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') and start_time < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD') "
                        + "and (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time and " +
                        		"site_id = '"
                        + site_id + "' group by prepaid_yes,SELECTED_BY";
            }
            logger.info("MAIN SELECTIONS QUERY:" + query);

            try
            {
				long lPreTime = System.currentTimeMillis();
				m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
                int pre_other_select = 0;
                int post_other_select = 0;
                while (m_rs1.next())
                {

                    String pre = m_rs1.getString(1);
                    char prepaid = 'n';
                    if (pre != null && pre.length() > 0)
                        prepaid = pre.charAt(0);
                    String selectedBy = m_rs1.getString("SELECTED_BY");

                    int subs = m_rs1.getInt(3);
                    if (prepaid == 'y' || prepaid == 'n')
                    {
                        if (prepaid == 'y')
                            preSelAdded = preSelAdded + subs;
                        else
                            postSelAdded = postSelAdded + subs;
                        queryParam = "select PARAMETER from RBT_ACTIVATION_PARAMETERS where TYPE like '%Sel%'";
						lPreTime = System.currentTimeMillis();
						m_rs2 = m_stmt2.executeQuery(queryParam);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ queryParam+"&&&TimeTaken="+((lPostTime - lPreTime)));
                        
                        while (m_rs2.next())
                        {
                            String name = m_rs2.getString(1);
                            String delimeter = ",";
                            StringTokenizer st = new StringTokenizer(name,
                                    delimeter);
                            len = st.countTokens();
                            boolean flag = true;
                            String firstParameters = null;
                            if (len > 1)
                            {
                                while (st.hasMoreTokens())
                                {
                                    String param = st.nextToken();
                                    if (flag == true)
                                    {
                                        firstParameters = param;
                                        firstParameters = firstParameters
                                                .replaceAll("-", "_");
                                        flag = false;
                                    }
                                    if (selectedBy.equalsIgnoreCase(param))
                                    {
                                        flagSelect = false;
                                        queryUpdate = "update RBT_ACTIVITY_REPORT set "
                                                + firstParameters
                                                + "_SELECTION_VAL= "
                                                + subs
                                                + " where site_id= "
                                                + site_id
                                                + " and report_date=to_date('"
                                                + report_date
                                                + "','YYYY-MM-DD') and prepaid_yes ='"
                                                + prepaid + "'";
                                        //										Tools.logDetail(_class, _method,
                                        // "BIRANCHI:QUERY UPDATE
                                        // SELECTION:"+queryUpdate);
										lPreTime = System.currentTimeMillis();
										m_stmt3.executeUpdate(queryUpdate);
										lPostTime = System.currentTimeMillis();
										logger.info("PERFMON: Query=>"+ queryUpdate+"&&&TimeTaken="+((lPostTime - lPreTime)));
                                        

                                    }
                                }
                            }
                            else if (len == 1
                                    && selectedBy.equalsIgnoreCase(name))
                            {
                                flagSelect = false;
                                name = name.replaceAll("-", "_");
                                queryUpdate = "update RBT_ACTIVITY_REPORT set "
                                        + name + "_SELECTION_VAL= " + subs
                                        + " where site_id= " + site_id
                                        + " and report_date=to_date('"
                                        + report_date
                                        + "','YYYY-MM-DD') and prepaid_yes ='"
                                        + prepaid + "'";
                                //								logger.info("QUERY
                                // UPDATE SELECTION:"+queryUpdate);
										lPreTime = System.currentTimeMillis();
										m_stmt3.executeUpdate(queryUpdate);
										lPostTime = System.currentTimeMillis();
										logger.info("PERFMON: Query=>"+ queryUpdate+"&&&TimeTaken="+((lPostTime - lPreTime)));
                                

                            }
                        }//while end of rbt_activity_parameters
                        if (flagSelect == true)
                        {
                            if (prepaid == 'y')
                                pre_other_select += subs;
                            else if (prepaid == 'n')
                                post_other_select += subs;
                        }
                        flagSelect = true;

                    }

                }//end of while

                query = "update RBT_ACTIVITY_REPORT set OTHER_SELECTION_VAL = "
                        + pre_other_select + " where site_id = '" + site_id
                        + "' " + "and report_date=to_date('" + report_date
                        + "','YYYY-MM-DD') and prepaid_yes ='y'";
						lPreTime = System.currentTimeMillis();
						m_stmt1.executeUpdate(query);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                

                query = "update RBT_ACTIVITY_REPORT set OTHER_SELECTION_VAL = "
                        + post_other_select + " where site_id = '" + site_id
                        + "' " + "and report_date=to_date('" + report_date
                        + "','YYYY-MM-DD') and prepaid_yes ='n'";
            lPreTime = System.currentTimeMillis();
			m_stmt1.executeUpdate(query);
			lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
				
            }
            catch (SQLException sqle)
            {
                logger.error("", sqle);
            }
            catch (Exception E)
            {
                logger.error("", E);
            }

            //get

            //	updateActivations(site_id,report_date,next_date);
            //	updateDeactivations(site_id,report_date,next_date);

            /*
             * updateDaysSelections(site_id,report_date,next_date); int
             * preSelAdded = 0; int postSelAdded = 0; //Changed Staging query =
             * "select PREPAID_YES,count(SUBSCRIBER_ID) from
             * RBT_SELECTIONS_staging where START_TIME between
             * to_date('"+report_date+"','YYYY-MM-DD') and
             * to_date('"+next_date+"','YYYY-MM-DD') and site_id = '"+site_id+"'
             * group by PREPAID_YES"; Tools.logDetail(_class, _method,
             * "Executing Query:"+query);
             * 
             * try{ m_rs1=m_stmt1.executeQuery(query); while(m_rs1.next()){
             * 
             * String pre = m_rs1.getString(1); char prepaid = 'n'; if(pre !=
             * null && pre.length() > 0) prepaid = pre.charAt(0);
             * 
             * int subs = m_rs1.getInt(2); if(prepaid =='y' || prepaid == 'n'){
             * if(prepaid == 'y') preSelAdded = preSelAdded + subs; else
             * postSelAdded = postSelAdded + subs; query = "update
             * RBT_ACTIVITY_REPORT set DAYS_SELECTIONS= "+subs+" where site_id=
             * "+site_id+ " and
             * report_date=to_date('"+report_date+"','YYYY-MM-DD') and
             * prepaid_yes ='"+prepaid+"'"; // Tools.logDetail(_class, _method,
             * "Executing Query:"+query); m_stmt2.executeUpdate(query); }
             * 
             * }//end of while }catch(SQLException sqle) {
             * logger.error("", sqle); }catch(Exception E) {
             * logger.error("", E); }
             *  
             */
            //updateDaysSelections(site_id,report_date,next_date);
            int preSelDel = 0;
            int postSelDel = 0;
            //CHANGED STAGING
            if (m_customer.equalsIgnoreCase("esia"))
            {
                query = "select prepaid_yes, count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING where "
                        + "end_time >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') and end_time < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD') "
                        + "and site_id = '"
                        + site_id
                        + "' and SEL_STATUS = 'X' and next_charging_date is not null group by prepaid_yes";
            }
            else if (m_customer.equalsIgnoreCase("idea"))
            {
               /* query = "select PREPAID_YES,count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING "+
                        "where site_id = '"+site_id+"' and END_TIME  >= to_date ('"+ report_date+ "','YYYY-MM-DD') and "+
						"END_TIME  < to_date ('"+ next_date+ "','YYYY-MM-DD') and start_time > ('01-Jan-2005') and "+
                        "((end_time - start_time)>'0.125') and next_charging_date is not null group by PREPAID_YES";*/
            	 query = "select PREPAID_YES,count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING "+
                 "where site_id = '"+site_id+"' and END_TIME  >= to_date ('"+ report_date+ "','YYYY-MM-DD') and "+
					"END_TIME  < to_date ('"+ next_date+ "','YYYY-MM-DD') and start_time > ('01-Jan-2005') "+
                 " and start_time != end_time  and  (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and  " +
                 " next_charging_date is not null  group by PREPAID_YES";
            }
            else if (m_customer.equalsIgnoreCase("bsnl"))
            {
                query = "select PREPAID_YES,count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING "
                        + "where END_TIME  >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') and END_TIME  < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD') "
                        + "and site_id = '"
                        + site_id
                        + "' and start_time > to_date('01-Jan-2005') group by PREPAID_YES";

            }
            else
            {
                query = "select PREPAID_YES,count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING "
                        + "where END_TIME  >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') and END_TIME  < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD') "
                        + "and site_id = '"
                        + site_id + "' group by PREPAID_YES";
            }

            logger.info("MAIN SELECTION WITH END_TIME :"
                    + query);

            try
            {
				long lPreTime = System.currentTimeMillis();
				 m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
               
                while (m_rs1.next())
                {

                    String pre = m_rs1.getString(1);
                    char prepaid = 'n';
                    if (pre != null && pre.length() > 0)
                        prepaid = pre.charAt(0);

                    int subs = m_rs1.getInt(2);
                    if (prepaid == 'y' || prepaid == 'n')
                    {
                        if (prepaid == 'y')
                            preSelDel = preSelDel + subs;
                        else
                            postSelDel = postSelDel + subs;
                        logger.info("MAIN SELECTIONS WITH END DATE:" + pre
                                                + " and count:" + subs);
                        /*
                         * query = "update RBT_ACTIVITY_REPORT set
                         * DAYS_SELECTIONS= "+subs+" where site_id= "+site_id+ "
                         * and
                         * report_date=to_date('"+report_date+"','YYYY-MM-DD')
                         * and prepaid_yes ='"+prepaid+"'";
                         * 
                         * logger.info("Executing
                         * Query:"+query);
                         * 
                         * stmt = m_conn.createStatement();
                         * stmt.executeUpdate(query); stmt.close();
                         */
                    }

                }//end of while
            }
            catch (SQLException sqle)
            {
                logger.error("", sqle);
            }
            catch (Exception E)
            {
                logger.error("", E);
            }

            //UPDATION FOR ESIA - SELECTION ON THE BASIS OF CLASS-TYPE :-
            if (m_customer.equalsIgnoreCase("esia"))
            {
                query = "select prepaid_yes,CLASS_TYPE, count(SUBSCRIBER_ID) from RBT_SELECTIONS_STAGING where "
                        + "start_time >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') and start_time < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD') "
                        + "and site_id = '"
                        + site_id
                        + "' and SEL_STATUS = 'B' and next_charging_date is not null group by prepaid_yes,CLASS_TYPE";

                logger.info("MAIN CLASS-TYPE SELECTIONS QUERY:" + query);
                try
                {
					long lPreTime = System.currentTimeMillis();
					 m_rs1 = m_stmt1.executeQuery(query);
					long lPostTime = System.currentTimeMillis();
					logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

                    
                    int pre_other_classSel = 0;
                    int post_other_classSel = 0;
                    while (m_rs1.next())
                    {

                        String pre = m_rs1.getString(1);
                        char prepaid = 'n';
                        if (pre != null && pre.length() > 0)
                            prepaid = pre.charAt(0);
                        String classType = m_rs1.getString("CLASS_TYPE");
						if(classType.equalsIgnoreCase("TRIAL"))
						{
							classType = "TRIAL30";
						}

                        int subs = m_rs1.getInt(3);
                        if (prepaid == 'y' || prepaid == 'n')
                        {
                            queryParam = "select PARAMETER from RBT_ACTIVATION_PARAMETERS where TYPE like '%Classsel%'";
							lPreTime = System.currentTimeMillis();
							m_rs2 = m_stmt2.executeQuery(queryParam);
							lPostTime = System.currentTimeMillis();
							logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

                           
                            while (m_rs2.next())
                            {
                                String name = m_rs2.getString(1);
                                String delimeter = ",";
                                StringTokenizer st = new StringTokenizer(name,
                                        delimeter);
                                len = st.countTokens();
                                boolean flag = true;
                                String firstParameters = null;
                                if (len > 1)
                                {
                                    while (st.hasMoreTokens())
                                    {
                                        String param = st.nextToken();
                                        if (flag == true)
                                        {
                                            firstParameters = param;
                                            firstParameters = firstParameters
                                                    .replaceAll("-", "_");
                                            flag = false;
                                        }
                                        if (classType.equalsIgnoreCase(param))
                                        {
                                            flagClassSel = false;
                                            queryUpdate = "update RBT_ACTIVITY_REPORT set "
                                                    + firstParameters
                                                    + "_CLASS_SEL= "
                                                    + subs
                                                    + " where site_id= "
                                                    + site_id
                                                    + " and report_date=to_date('"
                                                    + report_date
                                                    + "','YYYY-MM-DD') and prepaid_yes ='"
                                                    + prepaid + "'";
                                            //										Tools.logDetail(_class, _method,
                                            // "CLASS-TYPE:QUERY UPDATE
                                            // SELECTION:"+queryUpdate);
											lPreTime = System.currentTimeMillis();
											m_stmt3.executeUpdate(queryUpdate);
											lPostTime = System.currentTimeMillis();
											logger.info("PERFMON: Query=>"+ queryUpdate+"&&&TimeTaken="+((lPostTime - lPreTime)));
                                           
                                        }
                                    }
                                }
                                else if (len == 1
                                        && classType.equalsIgnoreCase(name))
                                {
                                    flagClassSel = false;
                                    name = name.replaceAll("-", "_");
                                    queryUpdate = "update RBT_ACTIVITY_REPORT set "
                                            + name
                                            + "_CLASS_SEL= "
                                            + subs
                                            + " where site_id= "
                                            + site_id
                                            + " and report_date=to_date('"
                                            + report_date
                                            + "','YYYY-MM-DD') and prepaid_yes ='"
                                            + prepaid + "'";
                                    //								logger.info("QUERY
                                    // UPDATE CLASS-TYPE
                                    // SELECTION:"+queryUpdate);
									lPreTime = System.currentTimeMillis();
									m_stmt3.executeUpdate(queryUpdate);
									lPostTime = System.currentTimeMillis();
									logger.info("PERFMON: Query=>"+ queryUpdate+"&&&TimeTaken="+((lPostTime - lPreTime)));
                                    

                                }
                            }//while end of rbt_activity_parameters
                            if (flagClassSel == true)
                            {
                                if (prepaid == 'y')
                                    pre_other_classSel += subs;
                                else if (prepaid == 'n')
                                    post_other_classSel += subs;
                            }
                            flagClassSel = true;

                        }

                    }//end of while

                    query = "update RBT_ACTIVITY_REPORT set OTHER_CLASS_SEL = "
                            + pre_other_classSel + " where site_id = '"
                            + site_id + "' " + "and report_date=to_date('"
                            + report_date
                            + "','YYYY-MM-DD') and prepaid_yes ='y'";
							lPreTime = System.currentTimeMillis();
							m_stmt1.executeUpdate(query);
							lPostTime = System.currentTimeMillis();
							logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                    

                    query = "update RBT_ACTIVITY_REPORT set OTHER_CLASS_SEL = "
                            + post_other_classSel + " where site_id = '"
                            + site_id + "' " + "and report_date=to_date('"
                            + report_date
                            + "','YYYY-MM-DD') and prepaid_yes ='n'";
							lPreTime = System.currentTimeMillis();
							m_stmt1.executeUpdate(query);
							lPostTime = System.currentTimeMillis();
							logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                   
                }
                catch (SQLException sqle)
                {
                    logger.error("", sqle);
                }
                catch (Exception E)
                {
                    logger.error("", E);
                }
            }

            //FOR ESIA - CHARGED_SUBSCRIBER AND ACTIVE_TRIAL_SUBSCRIBER
            if (m_customer.equalsIgnoreCase("esia"))
            {
                //update charged subscriber
                query = "select prepaid_yes, count(SUBSCRIBER_ID) from rbt_subs_selections_report where class_type NOT LIKE 'TRIAL%' and site_id = '"
                        + site_id
                        + "' AND "
                        + "next_charging_date is not null and ((next_charging_date >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') "
                        + "and next_charging_date < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD')) "
                        + "or (start_time >= to_date ('"
                        + report_date
                        + "','YYYY-MM-DD') and start_time < to_date ('"
                        + next_date
                        + "','YYYY-MM-DD') "
                        + "and next_charging_date > to_date ('2036-01-01','yyyy-MM-dd'))) group by prepaid_yes";

                logger.info("MAIN CHARGED SELECTION QUERY:" + query);
                try
                {
					long lPreTime = System.currentTimeMillis();
							  m_rs1 = m_stmt1.executeQuery(query);
							long lPostTime = System.currentTimeMillis();
							logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                    
                    while (m_rs1.next())
                    {
                        String pre = m_rs1.getString(1);
                        char prepaid = 'n';
                        if (pre != null && pre.length() > 0)
                            prepaid = pre.charAt(0);
                        int subs = m_rs1.getInt(2);
                        query = "update RBT_ACTIVITY_REPORT set CHARGED_SUBSCRIBERS= "
                                + subs
                                + " where SITE_ID= "
                                + site_id
                                + " and REPORT_DATE=to_date('"
                                + report_date
                                + "','YYYY-MM-DD') and PREPAID_YES ='"
                                + prepaid + "'";

                        //			logger.info("Executing
                        // Query:"+query);
						lPreTime = System.currentTimeMillis();
						m_stmt2.executeUpdate(query);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                       
                    }//end of while
                }
                catch (SQLException sqle)
                {
                    logger.error("", sqle);
                }
                catch (Exception E)
                {
                    logger.error("", E);
                }

                //update active trial subscribers

                query = "select PREPAID_YES, count(DISTINCT(SUBSCRIBER_ID)) from RBT_SUBS_SELECTIONS_REPORT where END_TIME > to_date ('"
                        + next_date
                        + "','YYYY-MM-DD') and CLASS_TYPE LIKE 'TRIAL%' AND SITE_ID = '"
                        + site_id + "' group by PREPAID_YES";
                logger.info("MAIN TRIAL SELECTION :"
                        + query);
                try
                {
					long lPreTime = System.currentTimeMillis();
							  m_rs1 = m_stmt1.executeQuery(query);
							long lPostTime = System.currentTimeMillis();
							logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                  
                    while (m_rs1.next())
                    {
                        String pre = m_rs1.getString(1);
                        char prepaid = 'n';
                        if (pre != null && pre.length() > 0)
                            prepaid = pre.charAt(0);
                        int subs = m_rs1.getInt(2);
                        query = "update RBT_ACTIVITY_REPORT set ACTIVE_TRIAL_SUBSCRIBERS = "
                                + subs
                                + " where SITE_ID= "
                                + site_id
                                + " and REPORT_DATE=to_date('"
                                + report_date
                                + "','YYYY-MM-DD') and PREPAID_YES ='"
                                + prepaid + "'";

                        //			logger.info("Executing
                        // Query:"+query);
						lPreTime = System.currentTimeMillis();
						m_stmt2.executeUpdate(query);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                        
                    }//end of while
                }
                catch (SQLException sqle)
                {
                    logger.error("", sqle);
                }
                catch (Exception E)
                {
                    logger.error("", E);
                }
            }

            //getDaysDeactiveSelections(site_id,report_date,next_date);

            updateActiveSelections(site_id, report_date, preSelAdded
                    - preSelDel, postSelAdded - postSelDel);
			logger.info("POOJA POOJA updateActiveSelections site_id :"+site_id+" AND report_date:"+report_date+" AND preSelAdded:"+preSelAdded+" AND preSelDel:"+preSelDel+" AND postSelAdded:"+postSelAdded+" AND postSelDel :"+postSelDel);
            updateActiveSubscribers(site_id, report_date, totalPreAct
                    - totalPreDeact, totalPostAct - totalPostDeact);
			logger.info("POOJA POOJA updateActiveSubscribers site_id :"+site_id+" AND report_date:"+report_date+" AND totalPreAct:"+totalPreAct+" AND totalPreDeact:"+totalPreDeact+" AND totalPostAct:"+totalPostAct+" AND totalPostDeact :"+totalPostDeact);

            getMOU(site_id, report_date, next_date);
            File cTemp = new File("./" + m_db_root + File.separator
                    + m_customer + "-" + site + File.separator + day
                    + File.separator + m_cdr_dir);
            if (cTemp.exists())
            {
                updateCDR_Data(site_id, report_date);
            }

            cal.add(Calendar.DATE, 1);
            next.add(Calendar.DATE, 1);
        }
		File subTemp = new File("./" + m_db_root + File.separator+ m_customer + "-" + site + File.separator + day + File.separator + m_db_end + File.separator +"TOTAL_ACT_"+m_db_collection_days+".txt");
		logger.info("subTemp ::"+subTemp.getAbsolutePath());
		if(subTemp.exists())
		{
			logger.info("updation of Active Subscriber based on summary file");
			updateActiveSubscribersSummary(site_id, subTemp);
		}
		
		try {
			//if(!m_conn.isClosed() && m_conn != null)
			m_conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	private void updateActiveSubscribersSummary(String site_id, File subTemp)
	{
		String _method ="updateActiveSubscribersSummary";
		logger.info("updateActiveSubscribersSummary Started::");
		FileReader objFileReader =null;
		BufferedReader brFile =null;
		try
		{
			Calendar calDay = Tools.getCalendarInstance();
			calDay.add(Calendar.DATE, -1);
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String report_date = sdf.format(calDay.getTime());

			objFileReader = new FileReader(subTemp);
			brFile = new BufferedReader(objFileReader);
			String strReadLine=null;
			int subCount = 0;
			String query = null;
			while ((strReadLine = brFile.readLine()) != null)
			{
				strReadLine = strReadLine.trim();
				if(strReadLine.startsWith("EXPRESSION1")) //if header
					continue;
				StringTokenizer strToken = new StringTokenizer(strReadLine,",");
				if (strToken.hasMoreTokens())
				{
					try{
						subCount = Integer.parseInt((String)strToken.nextToken());
					}catch(Exception parseE)
					{
						logger.error("", parseE);
						subCount = 0;
					}
				}
				query = "update rbt_activity_report set Active_subscribers="+subCount+" where "+ 
				"site_id = '"+site_id+"' and report_date=to_date('"+report_date+"','YYYY-MM-DD') and "+ 
				"prepaid_yes = 'n' ";
				logger.info("Summary Active Subscriber : y ::"+query);
				long lPreTime = System.currentTimeMillis();
				 m_stmt1.executeUpdate(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
				
				query = "update rbt_activity_report set Active_subscribers=0 where "+ 
				"site_id = '"+site_id+"' and report_date=to_date('"+report_date+"','YYYY-MM-DD') and "+ 
				"prepaid_yes = 'y' ";
				logger.info("Summary Active Subscriber : n ::"+query);
				lPreTime = System.currentTimeMillis();
				m_stmt1.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
				
			}
		}catch(Exception e)
		{
            logger.error("", e);
		}
		finally 
		{
			try{
				brFile.close();
				objFileReader.close();
			}catch(Exception e)
			{
				logger.error("", e);
			}
		}
	}


    /*
     * private void updateActivations(String site_id,String report_date, String
     * next_date){ String _method ="updateActivations"; if(m_conn == null) {
     * m_conn = getConnection();
     * logger.info("GotConnection"); }
     * 
     * Statement _stmt=null; ResultSet _rs=null;
     * 
     * 
     * String query = "select PREPAID_YES,ACTIVATED_BY,count(SUBSCRIBER_ID) from
     * RBT_SUBSCRIBER_STAGING " + "where ACTIVATION_DATE between
     * to_date('"+report_date+"','YYYY-MM-DD') " + "and
     * to_date('"+next_date+"','YYYY-MM-DD') and (DEACTIVATED_BY IS NULL or
     * DEACTIVATED_BY != 'NA') and site_id = '"+site_id+"' group by
     * prepaid_yes,activated_by";
     * 
     * 
     * logger.info("Executing Query "+query); try{
     * _stmt=m_conn.createStatement(); _rs=_stmt.executeQuery(query); int
     * pre_other_act =0; int post_other_act =0; Statement stmt = null;
     * 
     * while(_rs.next()){ char prepaid = _rs.getString("PREPAID_YES").charAt(0);
     * String actBy = _rs.getString("ACTIVATED_BY"); int subs = _rs.getInt(3);
     * if(prepaid =='y' || prepaid == 'n'){ if(actBy.equalsIgnoreCase("VP"))
     * query = "update RBT_ACTIVITY_REPORT set VOICE_ACTIVATIONS= "+subs+" where
     * site_id= "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='"+prepaid+"'"; else if(actBy.equalsIgnoreCase("SMS")) query = "update
     * RBT_ACTIVITY_REPORT set SMS_ACTIVATIONS= "+subs+" where site_id=
     * "+site_id+ " and report_date=to_date('"+report_date+"','YYYY-MM-DD') and
     * prepaid_yes ='"+prepaid+"'"; else if(actBy.equalsIgnoreCase("CC")) query =
     * "update RBT_ACTIVITY_REPORT set CC_ACTIVATIONS= "+subs+" where site_id=
     * "+site_id+ " and report_date=to_date('"+report_date+"','YYYY-MM-DD') and
     * prepaid_yes ='"+prepaid+"'"; else if(actBy.equalsIgnoreCase("OP")) query =
     * "update RBT_ACTIVITY_REPORT set OP_ACTIVATIONS= "+subs+" where site_id=
     * "+site_id+ " and report_date=to_date('"+report_date+"','YYYY-MM-DD') and
     * prepaid_yes ='"+prepaid+"'"; else if(actBy.equalsIgnoreCase("WAP")) query =
     * "update RBT_ACTIVITY_REPORT set WAP_ACTIVATIONS= "+subs+" where site_id=
     * "+site_id+ " and report_date=to_date('"+report_date+"','YYYY-MM-DD') and
     * prepaid_yes ='"+prepaid+"'"; else if(actBy.equalsIgnoreCase("VPO")) query =
     * "update RBT_ACTIVITY_REPORT set VPO_ACTIVATIONS= "+subs+" where site_id=
     * "+site_id+ " and report_date=to_date('"+report_date+"','YYYY-MM-DD') and
     * prepaid_yes ='"+prepaid+"'"; else if(actBy.equalsIgnoreCase("N4N") ||
     * actBy.equalsIgnoreCase("FN4N")) query = "update RBT_ACTIVITY_REPORT set
     * N4N_ACTIVATIONS= "+subs+" where site_id= "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='"+prepaid+"'"; else { if(prepaid =='y') pre_other_act += subs; else
     * if(prepaid == 'n') post_other_act += subs; continue; }
     * 
     * stmt = m_conn.createStatement();
     * 
     * stmt.executeUpdate(query); stmt.close(); }
     * 
     * }//end of while
     * 
     * stmt = m_conn.createStatement(); query = "update RBT_ACTIVITY_REPORT set
     * OTHER_ACTIVATIONS= "+pre_other_act+" where site_id= "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='y'"; stmt.executeUpdate(query); stmt.close(); stmt =
     * m_conn.createStatement(); query = "update RBT_ACTIVITY_REPORT set
     * OTHER_ACTIVATIONS= "+post_other_act+" where site_id= "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='n'"; stmt.executeUpdate(query); stmt.close();
     * 
     * 
     * }catch(SQLException sqle) { logger.error("", sqle);
     * }catch(Exception E) { logger.error("", E); } try{
     * _rs.close(); _stmt.close(); }catch(Exception E){
     * logger.error("", E); } }
     */

    /*
     * private void updateDeactivations(String site_id,String report_date,String
     * next_date){ String _method ="updateDeactivations"; String queryParam =
     * null; Statement _stmtParam = null; ResultSet _rsParam = null; if(m_conn ==
     * null) { m_conn = getConnection();
     * logger.info("GotConnection"); }
     * 
     * Statement _stmt=null; ResultSet _rs=null;
     * 
     * 
     * String query = "select PREPAID_YES,DEACTIVATED_BY,count(SUBSCRIBER_ID)
     * from RBT_SUBSCRIBER_STAGING " + "where DEACTIVATED_BY is not NULL and
     * DEACTIVATED_BY != 'AUX' and DEACTIVATED_BY != 'NEFX' and DEACTIVATED_BY !=
     * 'NA' and END_DATE between to_date('"+report_date+"','YYYY-MM-DD') and
     * to_date('"+next_date+"','YYYY-MM-DD') and site_id = '"+site_id+"' group
     * by prepaid_yes,deactivated_by";
     * 
     * logger.info("Executing Query:"+query);
     * 
     * try{ _stmt=m_conn.createStatement(); _rs=_stmt.executeQuery(query); int
     * pre_other_deact =0; int post_other_deact =0; Statement stmt = null;
     * while(_rs.next()){ char prepaid = _rs.getString("PREPAID_YES").charAt(0);
     * String deActBy = _rs.getString("DEACTIVATED_BY"); int subs =
     * _rs.getInt(3); if(prepaid =='y' || prepaid == 'n'){ stmt =
     * m_conn.createStatement(); if(deActBy.equalsIgnoreCase("VP")) query =
     * "update RBT_ACTIVITY_REPORT set VOICE_DEACTIVATIONS = "+subs+" where
     * site_id = "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='"+prepaid+"'"; else if(deActBy.equalsIgnoreCase("SMS")) query = "update
     * RBT_ACTIVITY_REPORT set SMS_DEACTIVATIONS = "+subs+" where site_id =
     * "+site_id+ " and report_date=to_date('"+report_date+"','YYYY-MM-DD') and
     * prepaid_yes ='"+prepaid+"'"; else if(deActBy.equalsIgnoreCase("CC"))
     * query = "update RBT_ACTIVITY_REPORT set CC_DEACTIVATIONS = "+subs+" where
     * site_id = "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='"+prepaid+"'"; else if(deActBy.equalsIgnoreCase("OP")) query = "update
     * RBT_ACTIVITY_REPORT set OP_DEACTIVATIONS = "+subs+" where site_id =
     * "+site_id+ " and report_date=to_date('"+report_date+"','YYYY-MM-DD') and
     * prepaid_yes ='"+prepaid+"'"; else if(deActBy.equalsIgnoreCase("WAP"))
     * query = "update RBT_ACTIVITY_REPORT set WAP_DEACTIVATIONS = "+subs+"
     * where site_id = "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='"+prepaid+"'"; else if(deActBy.equalsIgnoreCase("AU")) query = "update
     * RBT_ACTIVITY_REPORT set AU_DEACTIVATIONS = "+subs+" where site_id =
     * "+site_id+ " and report_date=to_date('"+report_date+"','YYYY-MM-DD') and
     * prepaid_yes ='"+prepaid+"'"; else if(deActBy.equalsIgnoreCase("NEF"))
     * query = "update RBT_ACTIVITY_REPORT set NEF_DEACTIVATIONS = "+subs+"
     * where site_id = "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='"+prepaid+"'"; else { if(prepaid =='y') pre_other_deact +=subs; else
     * if(prepaid == 'n') post_other_deact +=subs;
     * 
     * continue; } stmt.executeUpdate(query); stmt.close(); }
     * 
     * }//end of while
     * 
     * stmt = m_conn.createStatement(); query = "update RBT_ACTIVITY_REPORT set
     * OTHER_DEACTIVATIONS = "+pre_other_deact+" where site_id = "+site_id+ "
     * and report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='y'"; stmt.executeUpdate(query); stmt.close();
     * 
     * stmt = m_conn.createStatement(); query = "update RBT_ACTIVITY_REPORT set
     * OTHER_DEACTIVATIONS = "+post_other_deact+" where site_id = "+site_id+ "
     * and report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='n'"; stmt.executeUpdate(query); stmt.close();
     * 
     * }catch(SQLException sqle) { logger.error("", sqle);
     * }catch(Exception E) { logger.error("", E); } try{
     * _rs.close(); _stmt.close(); }catch(Exception E){ } }
     */

    /*
     * private void updateActiveSelections(String site_id,String report_date) {
     * String _method ="updateActiveSelections";
     * 
     * if(m_conn == null) { m_conn = getConnection();
     * logger.info("GotConnection"); }
     * 
     * Statement _stmt=null; ResultSet _rs=null;
     * 
     * String query = "select PREPAID_YES,count(SUBSCRIBER_ID) from
     * RBT_SUBS_SELECTIONS_REPORT where END_TIME> to_date
     * ('"+report_date+"','YYYY-MM-DD') and SITE_ID= '"+site_id+"' group by
     * PREPAID_YES";
     * 
     * logger.info("Executing Query:"+query);
     * 
     * try{ _stmt=m_conn.createStatement(); _rs=_stmt.executeQuery(query);
     * while(_rs.next()) { String pre = _rs.getString(1); char prepaid = 'n';
     * if(pre != null && pre.length() > 0) prepaid = pre.charAt(0); int subs =
     * _rs.getInt(2); Statement stmt = null; if(prepaid =='y' || prepaid ==
     * 'n'){
     * 
     * query = "update RBT_ACTIVITY_REPORT set ACTIVE_SELECTIONS= "+subs+" where
     * site_id= "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='"+prepaid+"'"; stmt = m_conn.createStatement();
     * stmt.executeUpdate(query); stmt.close(); }
     * 
     * }//end of while }catch(SQLException sqle) {
     * logger.error("", sqle); }catch(Exception E) {
     * logger.error("", E); } try{ _rs.close(); _stmt.close();
     * }catch(Exception E){ logger.error("", E); } }
     */

    private void updateActiveSelections(String site_id, String report_date,int preAddition, int postAddition)
    {
        String _method = "updateActiveSelections";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date reportDate = new Date();
        if(RBTReporter.maxProcessDate!=null){
            reportDate= RBTReporter.maxProcessDate;
        }
        try
        {
            reportDate = sdf.parse(report_date);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }

        Date prevDate = new Date(reportDate.getTime()
                - new Long(24 * 3600 * 1000).longValue());
        String prev = sdf.format(prevDate);
        String query = "select PREPAID_YES,active_selections from RBT_ACTIVITY_REPORT where SITE_ID= '"
                + site_id
                + "' and REPORT_DATE=to_date('"
                + prev
                + "','yyyy-MM-dd')";

        logger.info("Executing Query:" + query);

        try
        {
						long lPreTime = System.currentTimeMillis();
							   m_rs1 = m_stmt1.executeQuery(query);
							long lPostTime = System.currentTimeMillis();
							logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            while (m_rs1.next())
            {
                String pre = m_rs1.getString(1);
                char prepaid = 'n';
                if (pre != null && pre.length() > 0)
                    prepaid = pre.charAt(0);
                int subs = m_rs1.getInt(2);
                if (prepaid == 'y' || prepaid == 'n')
                {

                    if (prepaid == 'y')
                        subs = subs + preAddition;
                    else
                        subs = subs + postAddition;
                    query = "update RBT_ACTIVITY_REPORT set ACTIVE_SELECTIONS= "
                            + subs
                            + " where site_id= "
                            + site_id
                            + " and report_date=to_date('"
                            + report_date
                            + "','YYYY-MM-DD') and prepaid_yes ='"
                            + prepaid
                            + "'";

                    //				logger.info("Executing
                    // Query:"+query);
					lPreTime = System.currentTimeMillis();
					m_stmt2.executeUpdate(query);
					lPostTime = System.currentTimeMillis();
					logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                    
                }

            }//end of while
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
    }

    private void updateActiveSubscribers(String site_id, String report_date,
            int preAddition, int postAddition)
    {
        String _method = "updateActiveSubscribers";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date reportDate = new Date();
        if(RBTReporter.maxProcessDate!=null){
            reportDate= RBTReporter.maxProcessDate;
        }
        try
        {
            reportDate = sdf.parse(report_date);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }

        Date prevDate = new Date(reportDate.getTime()
                - new Long(24 * 3600 * 1000).longValue());
        String prev = sdf.format(prevDate);

        String query = "select PREPAID_YES,active_subscribers from RBT_ACTIVITY_REPORT where SITE_ID= '"
                + site_id
                + "' and REPORT_DATE=to_date('"
                + prev
                + "','yyyy-MM-dd')";

        logger.info("Executing Query:" + query);

        try
        {
			long lPreTime = System.currentTimeMillis();
				 m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            while (m_rs1.next())
            {
                String pre = m_rs1.getString(1);
                char prepaid = 'n';
                if (pre != null && pre.length() > 0)
                    prepaid = pre.charAt(0);
                int subs = m_rs1.getInt(2);
                if (prepaid == 'y' || prepaid == 'n')
                {

                    if (prepaid == 'y')
                        subs = subs + preAddition;
                    else
                        subs = subs + postAddition;
                    query = "update RBT_ACTIVITY_REPORT set ACTIVE_SUBSCRIBERS= "
                            + subs
                            + " where site_id= "
                            + site_id
                            + " and report_date=to_date('"
                            + report_date
                            + "','YYYY-MM-DD') and prepaid_yes ='"
                            + prepaid
                            + "'";

                    //			logger.info("Executing
                    // Query:"+query);
				lPreTime = System.currentTimeMillis();
				m_stmt2.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                    
                }

            }//end of while
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }

    }

    /*
     * private int updateDaysSelections(String site_id,String report_date,String
     * next_date) { String _method ="updateDaysSelections";
     * 
     * if(m_conn == null) { m_conn = getConnection();
     * logger.info("GotConnection"); }
     * 
     * int sel = 0; Statement _stmt=null; ResultSet _rs=null;
     * 
     * String query = "select PREPAID_YES,count(SUBSCRIBER_ID) from
     * RBT_SELECTIONS_STAGING where START_TIME between
     * to_date('"+report_date+"','YYYY-MM-DD') and
     * to_date('"+next_date+"','YYYY-MM-DD') and site_id = '"+site_id+"' group
     * by PREPAID_YES"; logger.info("Executing
     * Query:"+query);
     * 
     * try{ _stmt=m_conn.createStatement(); _rs=_stmt.executeQuery(query);
     * while(_rs.next()){
     * 
     * String pre = _rs.getString(1); char prepaid = 'n'; if(pre != null &&
     * pre.length() > 0) prepaid = pre.charAt(0);
     * 
     * int subs = _rs.getInt(2); Statement stmt = null; if(prepaid =='y' ||
     * prepaid == 'n'){
     * 
     * sel = sel + subs; query = "update RBT_ACTIVITY_REPORT set
     * DAYS_SELECTIONS= "+subs+" where site_id= "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='"+prepaid+"'";
     * 
     * logger.info("Executing Query:"+query);
     * 
     * stmt = m_conn.createStatement(); stmt.executeUpdate(query); stmt.close(); }
     * 
     * }//end of while }catch(SQLException sqle) {
     * logger.error("", sqle); }catch(Exception E) {
     * logger.error("", E); } try{ _rs.close(); _stmt.close();
     * }catch(Exception E){ logger.error("", E); }
     * 
     * return sel; }
     */

    /*
     * private int getDaysDeactiveSelections(String site_id,String
     * report_date,String next_date) { String _method
     * ="getDaysDeactiveSelections";
     * 
     * if(m_conn == null) { m_conn = getConnection();
     * logger.info("GotConnection"); }
     * 
     * int sel = 0; Statement _stmt=null; ResultSet _rs=null;
     * 
     * String query = "select PREPAID_YES,count(SUBSCRIBER_ID) from
     * RBT_SELECTIONS_STAGING where END_TIME between
     * to_date('"+report_date+"','YYYY-MM-DD') and
     * to_date('"+next_date+"','YYYY-MM-DD') and site_id = '"+site_id+"' group
     * by PREPAID_YES";
     * 
     * logger.info("Executing Query:"+query);
     * 
     * try{ _stmt=m_conn.createStatement(); _rs=_stmt.executeQuery(query);
     * while(_rs.next()){
     * 
     * String pre = _rs.getString(1); char prepaid = 'n'; if(pre != null &&
     * pre.length() > 0) prepaid = pre.charAt(0);
     * 
     * int subs = _rs.getInt(2); Statement stmt = null; if(prepaid =='y' ||
     * prepaid == 'n'){
     * 
     * sel = sel + subs; query = "update RBT_ACTIVITY_REPORT set
     * DAYS_SELECTIONS= "+subs+" where site_id= "+site_id+ " and
     * report_date=to_date('"+report_date+"','YYYY-MM-DD') and prepaid_yes
     * ='"+prepaid+"'";
     * 
     * logger.info("Executing Query:"+query);
     * 
     * stmt = m_conn.createStatement(); stmt.executeUpdate(query); stmt.close(); }
     * 
     * }//end of while }catch(SQLException sqle) {
     * logger.error("", sqle); }catch(Exception E) {
     * logger.error("", E); } try{ _rs.close(); _stmt.close();
     * }catch(Exception E){ logger.error("", E); }
     * 
     * return sel; }
     */

    private void getMOU(String site_id, String report_date, String next_date)
    {
        String _method = "getMOU";
        Connection morp_conn2 = null;
        Statement morp_stmt2 = null;
        ResultSet morp_rs2 = null;
        String query = null;
        if (m_customer.equalsIgnoreCase("tata"))
            query = " select sum(bad.USAGE) from B_APPDATA bad, rbt_sites rs, B_CUST_CIRCLE bcc where bad.CUST_ID=bcc.CUST_ID and bcc.SPIDER_CIRCLE = rs.MORPHEUS_CIRCLE_NAME and"
                    + " bcc.cust=rs.MORPHEUS_CUST_NAME and bad.B_DATE between to_date('"
                    + report_date
                    + "','yyyy-MM-dd') and  to_date('"
                    + next_date
                    + "','yyyy-MM-dd') and bad.APPNAME IN ('RBT','RBTPROMO') and rs.site_id = '"
                    + site_id + "' ";
        else
            query = " select sum(bad.USAGE) from B_APPDATA bad, rbt_sites rs, B_CUST_CIRCLE bcc where bad.CUST_ID=bcc.CUST_ID and bcc.SPIDER_CIRCLE = rs.MORPHEUS_CIRCLE_NAME and"
                    + " bcc.cust=rs.MORPHEUS_CUST_NAME and bad.B_DATE between to_date('"
                    + report_date
                    + "','yyyy-MM-dd') and  to_date('"
                    + next_date
                    + "','yyyy-MM-dd') and bad.APPNAME='RingBackTone' and rs.site_id = '"
                    + site_id + "' ";
        logger.info("Executing query " + query);
        try
        {
            morp_conn2 = getMorpheusConnection();
            morp_stmt2 = morp_conn2.createStatement();
			long lPreTime = System.currentTimeMillis();
							    morp_rs2 = morp_stmt2.executeQuery(query);
							long lPostTime = System.currentTimeMillis();
							logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            int val = 1;
            boolean done = true;
            while (morp_rs2.next())
            {
                int mou = morp_rs2.getInt(1);
                query = "update RBT_ACTIVITY_REPORT set MOU= " + mou
                        + " where site_id= " + site_id
                        + " and report_date=to_date('" + report_date
                        + "','YYYY-MM-DD')";
                //			logger.info("Executing query "+query);
				lPreTime = System.currentTimeMillis();
				m_stmt2.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

            }//end of while
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
        try
        {
            if (morp_rs2 != null)
                morp_rs2.close();
            if (morp_stmt2 != null)
                morp_stmt2.close();
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }

 /*   private String renameFile(String dir, String fileName)
    {
        String _method = "renameFile";
        StringTokenizer stk = new StringTokenizer(dir, "-");
        String cust = stk.nextToken();
        String site = stk.nextToken();
        StringTokenizer token = new StringTokenizer(fileName, "_");
        token.nextToken();
        token.nextToken();
        token.nextToken();
        String day = token.nextToken();
        String file = "RBTGatherer_" + cust + "_" + site + "_" + day;
        new File("./" + m_download_path + File.separator + fileName)
                .renameTo(new File("./" + m_download_path + File.separator
                        + file));
        //		if (m_takeBackup)
        //		{
        //			Tools.SubdirDelete(dir, Calendar.getInstance().getTime());
        //		}
        logger.info("puja : renameFile :" + file);
        return file;
    }*/

    private int updateToMorpheus(String zipfile)
    {
        if (m_morpheus == null)
        {
            m_morpheus = new MorpheusUpdater(m_customer);
            if (!m_morpheus.init(m_bundle))
            {
                logger.info("could not initialize morpheus updater");
                return 2;
            }
        }
        //written by yuva to check
       /* int iResult  = 1;
        if(!m_no_of_days_toload.equalsIgnoreCase("1"))
        {
        	m_db_collection_days = Integer.parseInt(m_no_of_days_toload);
        	iResult = m_morpheus.startUpdate_ConnInAutoCommitFale(zipfile, m_db_collection_days, m_customer);
        }
        else
        {*/
		int iResult = m_morpheus.startUpdate_ConnInAutoCommitFale(zipfile, m_db_collection_days, m_customer);
        //}
	 //	int iResult = m_morpheus.startUpdate(zipfile, m_db_collection_days);
		//log
        return iResult;

    }

    /*
     * private void updateToMorpheus() { String _method="updateToMorpheus";
     * MorpheusUpdater morpheus=new MorpheusUpdater();
     * if(!morpheus.init(m_bundle)){ logger.info("could
     * not initialize morpheus updater"); return; } if(m_zipfile.equals("All")){
     * morpheus.startUpdate(); }else { morpheus.startUpdate(m_zipfile); } }
     *  
     */private boolean extractZipFiles()
    {
        String _method = "extractZipFiles";
        File currentdir = new File("./" + m_download_path);
        File[] zipfiles = currentdir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                String _method = "accept";
                if (name.endsWith(".zip") && name.startsWith("db_"))
                {
                    logger.info("file:" + name
                            + " accepted");
                    return true;
                }
                else
                {
                    logger.info("file:" + name
                            + " NOT accepted");
                    return false;
                }
            }
        });
        if (zipfiles != null)
            logger.info("no. of files selected:"
                    + zipfiles.length);
        for (int i = 0; i < zipfiles.length; i++)
        {
            //for each of these zip files, parse the file names and extract.
            String extractpath = getCompleteExtractPath(zipfiles[i]);
            File extractdir = new File(extractpath);
            if (!extractdir.isDirectory())
                extractdir.mkdir();
            //now extract
            boolean done = Tools.unzipFileToDir(zipfiles[i], extractpath);
            if (done)
            {
                boolean check = zipfiles[i].delete();
                logger.info("zip file deleted:"
                        + zipfiles[i] + " delete returns " + check);
            }
            else
            {
                logger.info("extraction of "
                        + zipfiles[i] + " unsuccessful");
                return false;
            }
        }
        return true;
    }

    private void backup_summaryAndcalllog(String dir)
    {
        String _method = "backup_summaryAndcalllog";
		logger.info("POOJA ::: POOJA : STARTED backup_summaryAndcalllog ");
        Calendar cal = Tools.getCalendarInstance();
        cal.add(Calendar.DATE, -1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
        String folder = "./data" + File.separator + dir + File.separator
                + sdf.format(cal.getTime());
        File extract_dir = new File(folder);
        //		String backUpFolder = "Q:\\RBTDataBackUp\\SummarySDRFiles";
        logger.info("dir " + folder);
        cal.add(Calendar.DATE, 1);
        try
        {
        	if (extract_dir != null) {
                File[] list = extract_dir.listFiles();
                if (list != null) {
                    for (int j = 0; j < list.length; j++)
                    {
                        if (list[j].getName().endsWith(".htm")
                                || list[j].getName().equals("summary")
                                || list[j].getName().equals("sdr"))
                        {
                            if (list[j].isDirectory())
                            {
                                File[] summary = list[j].listFiles();
                                for (int k = 0; k < summary.length; k++)
                                    Tools.moveFile(m_bakDir + File.separator + dir,
                                                   summary[k]);
                            }
                            else
                            {
                                Tools.moveFile(m_bakDir + dir, list[j]);
                                new File(m_bakDir + File.separator + dir
                                        + File.separator + list[j].getName())
                                        .renameTo(new File(m_bakDir + File.separator
                                                + dir + File.separator
                                                + sdf.format(cal.getTime())
                                                + list[j].getName()));
                            }
                        }
                    }
                }
        	}
			logger.info("POOJA ::: POOJA : ENDED backup_summaryAndcalllog ");
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
    }

    private boolean extractZipFiles(String zipfile)
    {
        String _method = "extractZipFiles";
        if (zipfile.equals("All"))
        {
            return (extractZipFiles());
        }
        else
        {
            File extract_file = new File(zipfile);
            if (!extract_file.exists())
            {
                logger.info(zipfile + " not present");
                return false;
            }
            logger.info("extract one zipfile" + zipfile);
            String extractpath = getCompleteExtractPath(extract_file);
            File extractdir = new File(extractpath);
            if (!extractdir.isDirectory())
                extractdir.mkdir();
            //now extract
            boolean done = Tools.unzipFileToDir(extract_file, extractpath);
            if (done)
            {
            	/*
                boolean check = extract_file.delete();
                logger.info("zip file deleted:"
                        + extract_file + " delete returns " + check);
                 */
            }
            else
            {
                logger.info("extraction of "
                        + extract_file + " unsuccessful");
                return false;
            }
        }
        return true;
    }

    private Connection getConnection()
    {
        String _method = "getConnection";
        Connection conn = null;
        try
        {
            conn = DriverManager.getConnection(m_strCustDBURL);
			conn.setAutoCommit(false);

        }
        catch (Exception e)
        {
            logger.info("Exception	Caught	while getting Connection Exception=>"
                                           + e.getMessage());
            return null;
        }
        return conn;
    }
//Connection

/*		public Connection getMorpheusConnection()
		{
			String _method = "getMorpheusConnection";
				boolean bMakeConnection = false;
				try
				{
					
					if (_Connection	==	null	||	_Connection.isClosed() ||	( System.currentTimeMillis() -  _lConnectionCreationTime ) / 1000 > _TIMEOUT_DB_IN_SECS)
					{
						try
						{
							if(!_Connection.isClosed())
							{
								_Connection.close();
								_Connection = null;
							}
						}
						catch (Exception ex)
						{
						}
						bMakeConnection = true;
					}
				}
				catch(Exception e)
				{
					bMakeConnection = true;
				}
				if (!bMakeConnection && ((System.currentTimeMillis() - _lConnectionCreationTime ) / 1000 > _VERIFY_TIME_DB_IN_SECS))
				{
					try
					{
						Statement st = _Connection.createStatement();
						ResultSet rs = st.executeQuery(" Select 1 from dual ");
						rs.next();
						if(rs != null)
							rs.close();
						if(st!=null)
							st.close();
					}
					catch (Exception e)
					{
						bMakeConnection = true;
					}
				}
				if (bMakeConnection)
				{
					getConnection(true);
					_lConnectionCreationTime = System.currentTimeMillis();
				}
				return _Connection;
		}
		private void getConnection(boolean bUseLess)
		{
			String _method = "getConnection";
			_CONNECTION_VARIABLE = 5;
			while(_CONNECTION_VARIABLE > 0 )
			{
				try 
				{
					DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
					_Connection = DriverManager.getConnection(m_strDBURL);
				}
				catch (Exception exe) 
				{
					exe.printStackTrace();
					_CONNECTION_VARIABLE--;
		            logger.info("Exception	Caught	while getting MORPHEUS Connection Exception=>"+ exe.getMessage());
				}
				if(_Connection != null)
					break;
			}
			System.out.println("Connection Obj is "+_Connection.toString());
			return ;
		}
*/

     private Connection getMorpheusConnection()
    {
        String _method = "getConnection";
        Connection conn = null;
        try
        {
        	
            conn = DriverManager.getConnection(m_strDBURL);
            //System.out.println("Got Connection for the url : "+m_strDBURL);
			conn.setAutoCommit(false);

        }
        catch (Exception e)
        {
            logger.info("Exception Caught	while getting Connection Exception=>"+ e.getMessage());
            return null;
        }
        return conn;
    } 

    private String getCompleteExtractPath(File zipfile)
    {
        String _method = "getCompleteExtractPath";
        String absPath = "./" + m_extract_path;
        String filename = zipfile.getName();
        logger.info("zip file name:" + filename);
        StringTokenizer underscores = new StringTokenizer(filename, "_");

        // eliminate extra tokens if the base file name itself has _s in it
        StringTokenizer prefixToks = new StringTokenizer(RBTReporter.ftpFilePrefix, "_");
        for (int ii = 0; ii < prefixToks.countTokens()-1; ++ii) {
        	underscores.nextToken();
        }
        
        ArrayList list = new ArrayList();
        while (underscores.hasMoreTokens())
        {
            list.add(underscores.nextToken());
        }
        String cust = (String) list.get(1);
        String site = (String) list.get(2);
        String day = (String) list.get(3);
        day = day.substring(0, day.lastIndexOf("."));//eliminate extn
        String path = absPath + "/" + cust.toLowerCase() + "-"
                + site.toLowerCase() + "/" + day;
        logger.info("path returned:" + path);
        new File(path).mkdirs();
        return path;
    }

/*    private boolean downloadDataFiles(String zipfile)
    {
        String _method = "downloadDataFiles";
        boolean downloaded = false;
        
     //   * String s = "\\\\192.168.21.83\\E$\\test-gatherer\\temp"; File tmp =
     //    * new File(s); File[] list = tmp.listFiles(); for(int i=0;i
     //    * <list.length;i++) Tools.moveFile("./"+m_download_path, list[i]);
         
        FTPHandler ftphandler = new FTPHandler(m_ftp_config);
        if (zipfile.equals("All"))
        {
            downloaded = ftphandler.downloadAll("./" + m_download_path);
            if (downloaded)
            {
                logger.info("zip files downloaded from ftp location");
            }
            else
            {
                Tools.logWarning(_class, _method, "zip files not downloaded");
            }
        }
        else
        {
            downloaded = ftphandler.download("./" + m_download_path, zipfile);
        }
        return downloaded;
    }
*/
    private File[] getZipFileList(String path)
    {

        File spider_dir = new File(path);
        if (spider_dir == null)
        {
            logger.info("Spider dir not found "
                    + path);
            return null;
        }
        File[] list = new File(path).listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                if (name.startsWith(ftpFilePrefix) && name.endsWith(".zip"))
                    return true;
                else
                    return false;
            }
        });

        return list;
    }

    private boolean getResource()
    {
        String _method = "getResource";
        try
        {
            m_bundle = ResourceBundle.getBundle(m_resource_file);
			Tools.init(m_bundle);
        }
        catch (Exception e)
        {
          System.out.println("error in getting bundle:" + m_resource_file+ " !!!");
            e.printStackTrace();
			logger.error("", e);
            return false;
        }
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        }
        catch (SQLException e)
        {
            logger.error("", e);
            return false;
        }
        catch (ClassNotFoundException cnfe)
        {
            logger.error("", cnfe);
            return false;
        }
        try
        {
            m_strDBURL = m_bundle.getString("M0RPHEUS_ORACLE_DB_URL");
		//  m_strDBURL ="jdbc:oracle:thin:mmptest/mmptest@172.16.18.33:1525:mmptest";
        }
        catch (Exception E)
        {
            logger.info("DB URL Not in properties file exiting");
            return false;
        }

        try
        {
            m_strCustDBURL = m_bundle.getString("M0RPHEUS_ORACLE_"+ m_customer.toUpperCase() + "_DB_URL");
//			m_strCustDBURL ="jdbc:oracle:thin:mmptest/mmptest@172.16.18.33:1525:mmptest";
        }
        catch (Exception E)
        {
            logger.info("Customer DB URL Not in properties file exiting");
            return false;
        }

        try
        {
            DriverManager.registerDriver(new com.sap.dbtech.jdbc.DriverSapDB());
            String url = m_bundle.getString("MORPHEUS_SAP_DB_URL");
	//		String url ="jdbc:sapdb://10.9.11.16/mmp?sqlmode=ORACLE&user=onmobile&password=onmobile";
            m_sap_conn = DriverManager.getConnection(url);
        }
        catch (Exception e)
        {
            logger.error("", e);
            return false;
        }

        try
        {
            m_bakDir = m_bundle.getString("SUMMARY_SDR_DIR");
//			m_bakDir ="c:\\tata-reporter\\bak";
        }
        catch (Exception e)
        {

        }
        try
        {
            String spider = m_bundle.getString("SPIDER_DIR");
//			String spider ="c:\\tata-reporter\\spider";
            if (spider != null)
                m_spiderPath = spider;
        }
        catch (Exception E)
        {
        }

        try
        {
            String str = m_bundle.getString("RECONCILE_CONTENT");
//			 String str ="FALSE";
            if (str.equalsIgnoreCase("false"))
                m_reconcile_content = false;
        }
        catch (Exception E)
        {
            logger.info("RECONCILE_CONTENT Not in properties file");
        }
        int tmp = 0;
        try
        {
            tmp = Integer.parseInt(m_bundle.getString("NO_DAYS_DATA_FILE_2DELETE"));
//			tmp = Integer.parseInt(""+30);
        }
        catch (Exception E)
        {
            tmp = 15;
        }

        Calendar cal = Tools.getCalendarInstance();
        cal.add(Calendar.DATE, -tmp);

        m_delete_folder_last_date = cal.getTime();

        try
        {
            tmp = Integer.parseInt(m_bundle.getString("NO_DAYS_LOG_FILE_2DELETE"));
//			tmp = Integer.parseInt(""+7);
        }
        catch (Exception E)
        {
            tmp = 7;
        }

        Calendar c = Tools.getCalendarInstance();
        c.add(Calendar.DATE, -tmp);

        m_delete_logs_last_date = c.getTime();

        try
        {
            String backUp = m_bundle.getString("TAKE_BACKUP");
//			String backUp = m_bundle.getString("NO");
            if (backUp.equalsIgnoreCase("YES") || backUp.equalsIgnoreCase("TRUE"))
                m_takeBackup = true;
        }
        catch (Exception e)
        {

        }
		try{
			m_SleepInterval = Integer.parseInt(m_bundle.getString("SLEEP_INTERVAL"));
//			m_SleepInterval = Integer.parseInt(""+180000);
		}catch(Exception e)
		{
             logger.error("", e);
		}

        try
        {
            String prefix = m_bundle.getString("FTP_FILE_PREFIX");
            if (prefix != null && prefix.trim().length() > 0) {
            	ftpFilePrefix = prefix.trim();
            	logger.info("NOTE: Ftp files will be searched for non-default prefix " + ftpFilePrefix);
            } else {
            	logger.info("NOTE: Ftp files will be searched for default prefix " + ftpFilePrefix);
            }
		//  m_strDBURL ="jdbc:oracle:thin:mmptest/mmptest@172.16.18.33:1525:mmptest";
        }
        catch (Exception E)
        {
        	logger.info("NOTE: Ftp files will be searched for default prefix " + ftpFilePrefix);
        }

       // Tools.init(m_bundle);
        if (m_conn == null)
        {
            m_conn = getConnection();
            logger.info("GotConnection");
        }

        try
        {
            m_stmt1 = m_conn.createStatement();
            m_stmt2 = m_conn.createStatement();
            m_stmt3 = m_conn.createStatement();
        }
        catch (Exception e)
        {
            logger.error("", e);
            return false;
        }

        //      	m_morpheus=new MorpheusUpdater(m_customer);
        return createFTPConfig();
    }

    private boolean createFTPConfig()
    {
        String _method = "createFTPConfig";
        String server, user, pwd;
        int port, wait, retry, timeout;

        try
        {      
/*			server = "10.9.11.16";
            user = "onmobile";
            pwd = "qwerty12#";
            m_ftp_dir = "spider\\local\\uploads";
            port = Integer.parseInt(""+21);
            wait = Integer.parseInt(""+300000));
            retry = Integer.parseInt(""+1);
            timeout = Integer.parseInt(""+7200000); */

            server = m_bundle.getString("FTP_SERVER");
            user = m_bundle.getString("FTP_USER");
            pwd = m_bundle.getString("FTP_PWD");
            m_ftp_dir = m_bundle.getString("FTP_DIR");
            port = Integer.parseInt(m_bundle.getString("FTP_PORT"));
            wait = Integer.parseInt(m_bundle.getString("FTP_WAITPERIOD"));
            retry = Integer.parseInt(m_bundle.getString("FTP_RETRIES"));
            timeout = Integer.parseInt(m_bundle.getString("FTP_TIMEOUT")); 
        }
        catch (Exception e)
        {
            logger.error("", e);
            return false;
        }
        m_ftp_config = new FTPConfig(server, port, user, pwd, m_ftp_dir, wait,retry, timeout);
        return true;
    }

    private Hashtable getBaseCategories(String customer)
    {
        String _method = "getBaseCategories";

        int customer_id = 1;
        if (customer.equalsIgnoreCase("idea"))
            customer_id = 2;
        Statement _stmtSap = null;
        ResultSet _rsSap = null;

        Hashtable categories = new Hashtable();
        String query = "select distinct(category_id),category_name from dp_rbt_categories where customer_id = "+ customer_id;
        logger.info("Executing query " + query);
        try
        {
            _stmtSap = m_sap_conn.createStatement();
			long lPreTime = System.currentTimeMillis();
				 _rsSap = _stmtSap.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            while (_rsSap.next())
            {
                int category_id = _rsSap.getInt("category_id");
                String category_name = _rsSap.getString("category_name");
                categories.put(new Integer(category_id), category_name);
            }//end of while
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
        try
        {
            _rsSap.close();
            _stmtSap.close();
        }
        catch (Exception E)
        {
            logger.error("", E);
        }

        return categories;
    }

    private Hashtable getBaseClips(String customer)
    {
        String _method = "getBaseClips";

        int customer_id = 1;
        if (customer.equalsIgnoreCase("idea"))
            customer_id = 2;
        Statement _stmtSap = null;
        ResultSet _rsSap = null;

        Hashtable clips = new Hashtable();
        String query = "select clip_id,clip_name from dp_rbt_clips where customer_id = "+ customer_id;
        logger.info("Executing query " + query);
        try
        {
            _stmtSap = m_sap_conn.createStatement();
			long lPreTime = System.currentTimeMillis();
				  _rsSap = _stmtSap.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            while (_rsSap.next())
            {
                int clip_id = _rsSap.getInt("clip_id");
                String clip_name = _rsSap.getString("clip_name");
                clips.put(new Integer(clip_id), clip_name);
            }//end of while
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
        try
        {
            _rsSap.close();
            _stmtSap.close();
        }
        catch (Exception E)
        {
            logger.error("", E);
        }

        return clips;
    }

    private Hashtable getBaseCategoryClipMap(String customer)
    {
        String _method = "getBaseCategoryClipMap";

        int customer_id = 1;
        if (customer.equalsIgnoreCase("idea"))
            customer_id = 2;
        Statement _stmtSap = null;
        ResultSet _rsSap = null;

        Hashtable categoryClips = new Hashtable();
        String query = "select category_id,clip_id from dp_rbt_category_clip_map where customer_id = "
                + customer_id + " order by category_id";
        logger.info("Executing query " + query);
        try
        {
            _stmtSap = m_sap_conn.createStatement();
			long lPreTime = System.currentTimeMillis();
				   _rsSap = _stmtSap.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            int prev_cat = 0;
            ArrayList clips = new ArrayList();
            while (_rsSap.next())
            {
                int category_id = _rsSap.getInt("category_id");
                int clip_id = _rsSap.getInt("clip_id");
                if (prev_cat != category_id && prev_cat != 0)
                {
                    categoryClips.put(new Integer(prev_cat), clips);
                    clips = new ArrayList();
                }
                clips.add(new Integer(clip_id));
                prev_cat = category_id;

            }//end of while
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
        try
        {
            _rsSap.close();
            _stmtSap.close();
        }
        catch (Exception E)
        {
            logger.error("", E);
        }

        return categoryClips;
    }

    private Date getBaseContentReleaseDate(String customer)
    {
        String _method = "getBaseContentReleaseDate";

        int customer_id = 1;
        if (customer.equalsIgnoreCase("idea"))
            customer_id = 2;

        Statement _stmtSap = null;
        ResultSet _rsSap = null;

        Date date = null;
        String query = "select max(release_date) from dp_rbt_release where customer_id = "
                + customer_id;
        logger.info("Executing query " + query);
        try
        {
            _stmtSap = m_sap_conn.createStatement();
			long lPreTime = System.currentTimeMillis();
				   _rsSap = _stmtSap.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            while (_rsSap.next())
            {
                date = _rsSap.getTime(1);
            }//end of while
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
        try
        {
            _rsSap.close();
            _stmtSap.close();
        }
        catch (Exception E)
        {
            logger.error("", E);
        }

        return date;
    }

    private String getCustomerSite(String site_id)
    {
        String _method = "getCustomerSite";
        Connection morp_conn3 = null;
        Statement morp_stmt3 = null;
        ResultSet morp_rs3 = null;
        String custSite = null;
        String query = "select CUST_NAME,SITE_NAME from rbt_sites where site_id = "
                + site_id;//make query
        //  	logger.info("Executing Query:"+query);
        try
        {
            morp_conn3 = getMorpheusConnection();
            morp_stmt3 = morp_conn3.createStatement();
			long lPreTime = System.currentTimeMillis();
				   morp_rs3 = morp_stmt3.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            while (morp_rs3.next())
            {
                custSite = morp_rs3.getString("CUST_NAME") + "-"
                        + morp_rs3.getString("SITE_NAME");
            }
        }
        catch (SQLException e)
        {
            logger.error("", e);
        }
        try
        {
            if (morp_rs3 != null)
                morp_rs3.close();
            if (morp_stmt3 != null)
                morp_stmt3.close();
            if (morp_conn3 != null)
                morp_conn3.close();
        }
        catch (Exception e)
        {
        }
        return custSite;
    }

    private double getTableSize(String table_name)
    {
        String _method = "getTableSize";

        long bytes = 0;
        String query = "select bytes from user_segments where segment_name = '"
                + table_name + "'";
        System.out.println("Going to execute Query " + query + " at "
                + Calendar.getInstance().getTime());

        try
        {
long lPreTime = System.currentTimeMillis();
				    m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            boolean done = true;
            while (m_rs1.next())
            {
                bytes = m_rs1.getLong(1);
                System.out.println(bytes);
            }//end of while
        }
        catch (SQLException sqle)
        {
            return 0;
        }
        return ((double) bytes / (1024 * 1024));
    }

    private void populateSMSSelectionReports(String dir)
    {
        String _method = "populateSMSSelectionReports";

        StringTokenizer stk = new StringTokenizer(dir, "-");
        String cust = stk.nextToken();
        String site = stk.nextToken();
        String site_id = getSiteId(cust, site);
        /*
         * Calendar cal = Calendar.getInstance(); SimpleDateFormat sdf = new
         * SimpleDateFormat("yyyy-MM-dd"); String next_date =
         * sdf.format(cal.getTime()); cal.add(Calendar.DATE,-1); String
         * report_date = sdf.format(cal.getTime());
         * populateSMSSelections(site_id,report_date,next_date);
         */

        premiumDate = getMaxReportDate(site_id, "rbt_premium_selections_report");
		hash_premiumDate.put(site_id,premiumDate);
        logger.info("premiumDate" + premiumDate);
        Calendar next = Calendar.getInstance();
        next.setTime(premiumDate);
        next.add(Calendar.DATE, 2);
        Calendar cal = Calendar.getInstance();
        cal.setTime(premiumDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        cal.add(Calendar.DATE, 1);
//        while (next.getTime().before(Calendar.getInstance().getTime()))
		// while (next.getTime().before(maxProcessDate))
        while (next.getTime().compareTo(maxProcessDate) <= 0)
			
        {
            String report_date = sdf.format(cal.getTime());
            String next_date = sdf.format(next.getTime());
            populateSMSSelections(site_id, report_date, next_date);
            cal.add(Calendar.DATE, 1);
            next.add(Calendar.DATE, 1);
        }
    }

    private void populateSMSSelections(String site_id, String report_date,
            String next_date)
    {
        String _method = "populateSMSSelections";
        Connection morp_conn2 = null;
        String query = null;
		if(m_customer.equalsIgnoreCase("idea"))
		        /*query = "select selected_by, count(subscriber_id) from RBT_SELECTIONS_STAGING  "+
                "where site_id = "+ site_id+ " and category_id = 3 and "+
				"(start_time >= to_date('"+ report_date+ "', 'yyyy-MM-dd') and start_time<to_date('"+ next_date+ "', 'yyyy-MM-dd')) "+
				"and start_time > ('01-Jan-2005') and ((end_time - start_time)>'0.125') and next_charging_date is not null "+
				"group by selected_by";*/
			query = "select selected_by, count(subscriber_id) from RBT_SELECTIONS_STAGING  "+
            "where site_id = "+ site_id+ " and category_id = 3 and "+
			"(start_time >= to_date('"+ report_date+ "', 'yyyy-MM-dd') and start_time<to_date('"+ next_date+ "', 'yyyy-MM-dd')) "+
			"and start_time > ('01-Jan-2005') and next_charging_date is not null "+
			"group by selected_by";
		else if(m_customer.equalsIgnoreCase("bsnl"))
		        query = "select selected_by, count(subscriber_id) from RBT_SELECTIONS_STAGING  "+
                "where site_id = "+ site_id+ " and category_id = 3 and "+
				"(start_time >= to_date('"+ report_date+ "', 'yyyy-MM-dd') and start_time<to_date('"+ next_date+ "', 'yyyy-MM-dd')) "+
				"and start_time > ('01-Jan-2005') group by selected_by";
		else 
				query = "select selected_by, count(subscriber_id) from RBT_SELECTIONS_STAGING  "+
                "where site_id = "+ site_id+ " and category_id = 3 and "+
				"(start_time >= to_date('"+ report_date+ "', 'yyyy-MM-dd') and start_time<to_date('"+ next_date+ "', 'yyyy-MM-dd')) "+
				"group by selected_by";

        logger.info("STARTED PREMIUM SELECTION TABLE BASED ON STAGING TABLE Going to execute Query " + query);

        try
        {
            morp_conn2 = getMorpheusConnection();
			long lPreTime = System.currentTimeMillis();
             m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            boolean done = true;
            while (m_rs1.next())
            {
                String activated_by = m_rs1.getString(1);
                if (activated_by == null)
                    activated_by = "PROMO";

                if (activated_by.equalsIgnoreCase("VP"))
                    continue;
                int num_selections = m_rs1.getInt(2);
                //query for Activation with Song (sms) and Song Change(Promo ,
                // pro)
                query = "insert into RBT_PREMIUM_SELECTIONS_REPORT values('"
                        + site_id + "',null,null,to_date('" + report_date
                        + "','YYYY-MM-DD')," + num_selections + ",null,'"
                        + activated_by + "')";
				lPreTime = System.currentTimeMillis();
				m_stmt2.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

               
            }//end of while
        logger.info("POOJA POOJA ::: ENDED PREMIUM SELECTION TABLE BASED ON STAGING TABLE");
        try {
        	morp_conn2.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
            Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,
                                  REMARKS_SMS_SELECTION_COUNT + report_date,
                                  REMARKS_TYPE_REPORT_ERROR);
        }
        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }

    private void populatePremiumSelectionReports(String dir)
    {
        String _method = "populatePremiumSelectionReports";

        StringTokenizer stk = new StringTokenizer(dir, "-");
        String cust = stk.nextToken();
        String site = stk.nextToken();
        String site_id = getSiteId(cust, site);
        /*
         * Calendar cal = Calendar.getInstance(); SimpleDateFormat sdf = new
         * SimpleDateFormat("yyyy-MM-dd"); String next_date =
         * sdf.format(cal.getTime()); cal.add(Calendar.DATE,-1); String
         * report_date = sdf.format(cal.getTime());
         * populatePremiumReport(site_id,report_date);
         */
        Date premiumDateObt = (Date)hash_premiumDate.get(site_id);
        //Date premiumDateObt = getMaxReportDate(site_id, "rbt_premium_selections_report");
        logger.info("*** premiumDate:" + premiumDateObt);
        Calendar next = Calendar.getInstance();
        next.setTime(premiumDateObt);
        next.add(Calendar.DATE, 2);
        Calendar cal = Calendar.getInstance();
        cal.setTime(premiumDateObt);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        cal.add(Calendar.DATE, 1);
      //  while (next.getTime().before(Calendar.getInstance().getTime()))
	     //while (next.getTime().before(maxProcessDate))
        while (next.getTime().compareTo(maxProcessDate) <= 0)
        {
            String report_date = sdf.format(cal.getTime());
            String next_date = sdf.format(next.getTime());
            populatePremiumReport(site_id, report_date);
            cal.add(Calendar.DATE, 1);
            next.add(Calendar.DATE, 1);
        }
        
        

    }

    private void populatePremiumReport(String site_id, String report_date)
    {
        String _method = "populatePremiumReport";
        Connection morp_conn2 = null;
		String query =null;
        Hashtable categories = null;
        if (m_customer.equalsIgnoreCase("esia"))
            categories = getCategories("" + 150);
        else
            categories = getCategories(site_id);

        Hashtable shuffleCat = null;
        if (m_customer.equalsIgnoreCase("tata"))
            shuffleCat = getTataShuffleCategories(site_id);
        Hashtable promo = getPromoId(site_id);
//      start_time != end_time and 
		if(m_customer.equalsIgnoreCase("idea"))
			query = "select category_id, class_type, count(subscriber_id) "
                + "from rbt_subs_selections_report where " + "site_id='"
                + site_id + "' "
                + "and class_type is not null and class_type != 'DEFAULT' and "
                + "start_time <= to_date('" + report_date
                + " 11:59:59 pm', 'yyyy-MM-dd hh:mi:ss am') and "
                + "end_time >= to_date('" + report_date
                + " 11:59:59 pm', 'yyyy-MM-dd hh:mi:ss pm') and"
                + " (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) " +
                " and start_time != end_time  and start_time > ('01-Jan-2005') group by category_id, class_type";
		else if( m_customer.equalsIgnoreCase("bsnl"))
			query = "select category_id, class_type, count(subscriber_id) "
                + "from rbt_subs_selections_report where " + "site_id='"
                + site_id + "' "
                + "and class_type is not null and class_type != 'DEFAULT' and "
                + "start_time <= to_date('" + report_date
                + " 11:59:59 pm', 'yyyy-MM-dd hh:mi:ss am') and "
                + "end_time >= to_date('" + report_date
                + " 11:59:59 pm', 'yyyy-MM-dd hh:mi:ss pm') "
                + " and (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time" +
                		"and start_time > ('01-Jan-2005') group by category_id, class_type";
		else
			query = "select category_id, class_type, count(subscriber_id) "
                + "from rbt_subs_selections_report where " + "site_id='"
                + site_id + "' "
                + "and class_type is not null and class_type != 'DEFAULT' and "
                + "start_time <= to_date('" + report_date
                + " 11:59:59 pm', 'yyyy-MM-dd hh:mi:ss am') and "
                + "end_time >= to_date('" + report_date
                + " 11:59:59 pm', 'yyyy-MM-dd hh:mi:ss pm') and " +
                "(deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time"
                + "group by category_id, class_type";

        logger.info("STARTED PREMIUM SELECTION REPORT BASED ON DATAWARE HOUSE puja: Main query for Premium Selections :" + query);

        try
        {
            morp_conn2 = getMorpheusConnection();

				long lPreTime = System.currentTimeMillis();
				   m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
//            boolean done = true;
            while (m_rs1.next())
            {
                if (m_rs1.getString(1) == null)
                    continue;
                //         String category_name = (String)
                // categories.get(m_rs1.getString(1));
                String category_name = null;
                if ((m_customer.equalsIgnoreCase("tata"))
                        && categories.get(m_rs1.getString(1)) == null)
                {
                    category_name = (String) shuffleCat.get(m_rs1.getString(1));
                }
                else
                {
                    category_name = (String) categories.get(m_rs1.getString(1));
                }

                String promo_id = (String) promo.get(m_rs1.getString(1));

                if (promo_id == null)
                    promo_id = " ";
                String class_type = m_rs1.getString(2);
                int num_selections = m_rs1.getInt(3);

                //for premium selection
                query = "insert into RBT_PREMIUM_SELECTIONS_REPORT values('"
                        + site_id + "','" + category_name + "','" + class_type
                        + "',to_date('" + report_date + "','YYYY-MM-DD'),"
                        + num_selections + ",'" + promo_id + "', null)";
				lPreTime = System.currentTimeMillis();
				m_stmt2.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
            }//end of while
	        logger.info("POOJA POOJA ::: ENDED POPULATION OF PREMIUM SELECTION REPORT BASED ON DATAWARE HOUSE");

	        try {
				m_conn.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
            Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,
                                  REMARKS_PREMIUM_TUNE_SELECTION + report_date,
                                  REMARKS_TYPE_REPORT_ERROR);
        }
        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }

    //String for getting category_id for shuffle :-
    private String getShuffleCatId(String site_id)
    {
        String _method = "getShuffleCatId";
        String catId = null;
        int no = 0;
        try
        {
			String query = "select category_id from RBT_CATEGORIES_REPORT where site_id = " + site_id + " and category_type =0";
            
				long lPreTime = System.currentTimeMillis();
				 m_rs1 = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            while (m_rs1.next())
            {
                String shuffleCatId = m_rs1.getString("category_id");
                if (no == 0)
                {
                    catId = shuffleCatId;
                    no++;
                }
                else
                    catId += "," + shuffleCatId;
            }
            logger.info("puja : catId :" + catId);
        }
        catch (Exception ee)
        {
            logger.error("", ee);
        }
        return catId;
    }

    // String for getting clip_id for shuffleCategoryId
    private Hashtable getClipFrCatShuffleId(String site_id, String catId)
    {
        String _method = "getClipFrCatShuffleId";
        Hashtable m_clipId = new Hashtable();
        String clipId = new String();
        String id = null;
        int no = 0;
        String query = "select clip_id,category_id from rbt_category_clip_map_report where site_id = '"
                + site_id + "' and  category_id IN (" + catId + ")";
        try
        {
			long lPreTime = System.currentTimeMillis();
			 m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
	   
            logger.info("Get Clip id from category id QUERY : " + query);
            while (m_rs1.next())
            {
                id = m_rs1.getString("category_id");
                String value = (String) m_clipId.get(id);
                if (value == null || value.equalsIgnoreCase("null"))
                {
                    no = 0;
                }
                if (no == 0)
                {
                    clipId = m_rs1.getString("clip_id");
                    no++;
                }
                else
                {
                    clipId = (String) m_clipId.get(id);
                    clipId += "," + m_rs1.getString("clip_id");
                }
                m_clipId.put(id, clipId);
            }
            logger.info("puja : getClipFrCatShuffleId : m_clipId :"
                                    + m_clipId.size());
        }
        catch (Exception ee)
        {
            logger.error("", ee);
        }
        return m_clipId;
    }

    //ArrayList for getting Tunes (wav_file) w.r.t clip_id
    private ArrayList getTunes(String site_id, String ClipNo)
    {
        String _method = "getTunes";
        StringTokenizer st = new StringTokenizer(ClipNo, ",");
        ArrayList al = new ArrayList();
        try
        {
            while (st.hasMoreTokens())
            {
                String clipId = st.nextToken();
                String query = "select clip_rbt_wav_file from rbt_clips_Report where site_id = '"
                        + site_id + "' and clip_ID = '" + clipId + "' ";
				long lPreTime = System.currentTimeMillis();
				m_rs2 = m_stmt2.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
                while (m_rs2.next())
                {
                    String clipName = m_rs2.getString("clip_rbt_wav_file");
                    al.add(clipName);
                }
            }
            logger.info("Tunes count Wrt clip id for resp category_id :"
                                    + al.size());
        }
        catch (Exception ee)
        {
            logger.error("", ee);
        }
        return al;
    }

    //Hashtable for getting category_id and category_name
    private Hashtable getCategories(String site_id)
    {
		String _method = "getCategories";
        Hashtable hCat = new Hashtable();
        try
        {
			String query ="select category_id,category_name from rbt_categories_report where site_id = '" + site_id + "'";
			long lPreTime = System.currentTimeMillis();
			m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
            
            while (m_rs1.next())
            {
                String id = m_rs1.getString(1);
                String name = m_rs1.getString(2);
                hCat.put(id, name);
            }
        }
        catch (Exception eee)
        {
//            System.out.println(eee);
			logger.error("", eee);
        }

        return hCat;
    }

    //Hashtable for getting category_id and category_name
    private Hashtable getTataShuffleCategories(String site_id)
    {
		String _method = "getTataShuffleCategories";
        Hashtable hShCat = new Hashtable();
        try
        {
            //	m_rs1=m_stmt1.executeQuery("select MUSICBOX_ID,MUSICBOX_NAME from
            // RBT_MUSICBOXES where site_id = '"+site_id+"'");
			String query="select MUSICBOX_ID,MUSICBOX_NAME from RBT_MUSICBOXES";
			long lPreTime = System.currentTimeMillis();
			m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
			
            
            while (m_rs1.next())
            {
                String id = m_rs1.getString(1);
                String name = m_rs1.getString(2);
                hShCat.put(id, name);
            }
        }
        catch (Exception eee)
        {
//            System.out.println(eee);
			logger.error("", eee);
        }

        return hShCat;
    }

    //Hashtable for getting categories_id and promo_id
    private Hashtable getPromoId(String site_id)
    {
        Hashtable hPromo = new Hashtable();
		String _method = "getPromoId";
        try
        {
			String query="select category_id,CATEGORY_PROMO_ID from rbt_categories_report where site_id = '"
                            + site_id + "'";
           
							
			long lPreTime = System.currentTimeMillis();
			 m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            while (m_rs1.next())
            {
                String id = m_rs1.getString(1);
                String name = m_rs1.getString(2);
                if (name == null)
                    name = " ";
                hPromo.put(id, name);
            }
        }
        catch (Exception eee)
        {
//            System.out.println(eee);
			logger.error("", eee);
        }

        return hPromo;
    }

    private void updateSelectionsReport(String site_id, String report_date)
    {
        String _method = "updateSelectionsReport";
        Connection morp_conn2 = null;
		String query =null;
        Hashtable categories = null;
        if (m_customer.equalsIgnoreCase("esia"))
            categories = getCategories("" + 150);
        else
            categories = getCategories(site_id);
        Hashtable shuffleCat = null;
        if (m_customer.equalsIgnoreCase("tata"))
            shuffleCat = getTataShuffleCategories(site_id);
        //start_time != end_time and 
		if(m_customer.equalsIgnoreCase("idea"))
        query = "select CLASS_TYPE,category_id, subscriber_wav_file, count(subscriber_id) as numSel from rbt_subs_selections_report "
                + "where site_id = '"+site_id+"' and (class_type is null or class_type = 'DEFAULT' or class_type='SMS') and "
                + "start_time <= to_date('"
                + report_date
                + " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss am') and end_time >= to_date('"
                + report_date
                + " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss am') "
                + "and (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time " +
                "and start_time > ('01-Jan-2005') group by CLASS_TYPE,category_id, subscriber_wav_file";
		else if(m_customer.equalsIgnoreCase("bsnl"))
	        query = "select CLASS_TYPE,category_id, subscriber_wav_file, count(subscriber_id) as numSel from rbt_subs_selections_report "
	                + "where site_id = '"+site_id+"' and (class_type is null or class_type = 'DEFAULT' or class_type='SMS') and "
	                + "start_time <= to_date('"
	                + report_date
	                + " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss am') and end_time >= to_date('"
	                + report_date
	                + " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss am') "
	                + "and (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time " +
	                		"and start_time > ('01-Jan-2005') group by CLASS_TYPE,category_id, subscriber_wav_file";
		else
        query = "select CLASS_TYPE,category_id, subscriber_wav_file, count(subscriber_id) as numSel from rbt_subs_selections_report "
                + "where (class_type is null or class_type = 'DEFAULT' or class_type='SMS') and "
                + "start_time <= to_date('"
                + report_date
                + " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss am') and end_time >= to_date('"
                + report_date
                + " 11:59:59 PM', 'YYYY-MM-DD hh:mi:ss am') "
                + " and (deselected_by IS NULL or (deselected_by != 'NA' and deselected_by != 'AF')) and start_time != end_time " +
                		"and site_id= '"
                + site_id
                + "' group by CLASS_TYPE,category_id, subscriber_wav_file";
        logger.info("STARTED POPULATION OF TUNES SELECTION TABLE BASED ON DATEWARE HOUSE Going to execute Query " + query);

        try
        {
            morp_conn2 = getMorpheusConnection();
			long lPreTime = System.currentTimeMillis();
            m_rs1 = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            boolean done = true;
            while (m_rs1.next())
            {
                String wav_file = m_rs1.getString("subscriber_wav_file");
                int categoryID = m_rs1.getInt("category_id");
                if (m_rs1.getString("category_id") == null)
                    continue;
                String category = null;
                if ((m_customer.equalsIgnoreCase("tata"))
                        && categories.get(m_rs1.getString("category_id")) == null)
                {
                    category = (String) shuffleCat.get(m_rs1
                            .getString("category_id"));
                }
                else
                {
                    category = (String) categories.get(m_rs1
                            .getString("category_id"));
                }
                int subs = m_rs1.getInt("numSel");
                if (wav_file == null || wav_file.startsWith("9")
                        || wav_file.startsWith("null"))
                {
                    //		logger.info("puja :wav_file:
                    // "+wav_file);
                    continue;
                }
                String classType = m_rs1.getString("CLASS_TYPE");
                query = "update RBT_TUNES_SELECTIONS_REPORT set NUM_SELECTIONS = "
                        + subs
                        + " where SITE_ID = "
                        + site_id
                        + " and WAV_FILE = '"
                        + wav_file
                        + "'  and REPORT_DATE = to_date('"
                        + report_date
                        + "','YYYY-MM-DD') and CATEGORY_ID = "
                        + categoryID
                        + " and CLASS_TYPE = '" + classType + "' ";

				lPreTime = System.currentTimeMillis();
				int records = m_stmt2.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

              
                //			logger.info("puja : Executing
                // Query:"+query);

                if (records == 0)
                {
                    query = "insert into RBT_TUNES_SELECTIONS_REPORT(Site_id,WAV_FILE,CATEGORY_NAME,REPORT_DATE,NUM_SELECTIONS,TOTAL_SELECTIONS,category_ID,CLASS_TYPE) "
                            + "values("
                            + site_id
                            + ",'"
                            + wav_file
                            + "','"
                            + category
                            + "',to_date('"
                            + report_date
                            + "','YYYY-MM-DD'),"
                            + subs
                            + ",0,"
                            + categoryID
                            + ",'" + classType + "')";

                    //		logger.info("puja : Executing insert
                    // Query:"+query);
				lPreTime = System.currentTimeMillis();
				m_stmt3.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                    

                }

            }//end of while
        logger.info("POOJA POOJA ::: ENDED POPULATION OF TUNES SELECTION TABLE BASED ON DATEWARE HOUSE ");
        
        try {
			m_conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
            Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,
                                  REMARKS_TUNE_SELECTION + report_date,
                                  REMARKS_TYPE_REPORT_ERROR);
        }
        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    }
     private void populateSelectionsReport(String site_id, String report_date,
            String next_date)
    {
        String _method = "populateSelectionsReport";
        Connection morp_conn2 = null;
        String query = null;
        Hashtable shuffleCat = null;
        Hashtable categories = null;
        String shuffleCatId = null;
        if (m_customer.equalsIgnoreCase("esia"))
            categories = getCategories("" + 150);
        else
            categories = getCategories(site_id);
		
		StringBuffer queryBuffer = new StringBuffer();
		if(m_customer.equalsIgnoreCase("tata"))
			shuffleCat = getTataShuffleCategories(site_id);
	
		if(!m_customer.equalsIgnoreCase("esia") && !m_customer.equalsIgnoreCase("tata"))
			shuffleCatId = getShuffleCatId(site_id);
		logger.info("POOJA POOJA ::: shuffleCatId :::::: "+shuffleCatId);
		queryBuffer = queryBuffer.append("select CLASS_TYPE,category_id, subscriber_wav_file, count(SUBSCRIBER_ID) as totSel from rbt_SELECTIONS_STAGING ");
		if(m_customer.equalsIgnoreCase("esia")){
			queryBuffer.append("where class_type NOT LIKE 'TRIAL%' and ");
			queryBuffer.append("SITE_ID ='"+ site_id+ "' AND next_charging_date is not null ");
			queryBuffer.append("and start_time != end_time and ((next_charging_date >= to_date ('"+ report_date+ "','YYYY-MM-DD') "+ "and next_charging_date < to_date ('"+ next_date+ "','YYYY-MM-DD')) ");
			queryBuffer.append("or (start_time >= to_date ('"+ report_date+ "','YYYY-MM-DD') and start_time < to_date ('"+ next_date+ "','YYYY-MM-DD') and next_charging_date > to_date ('2036-01-01','yyyy-MM-dd'))) ");
	   }else 
		{
		   //tata and all
			queryBuffer.append("where SITE_ID ='"+ site_id+ "' AND (start_time >= to_date('"+ report_date+ "', 'yyyy-MM-dd') and start_time < to_date('"+ next_date+ "', 'yyyy-MM-dd')) ");
			if(m_customer.equalsIgnoreCase("vodafone")){
				queryBuffer.append(" and start_time != end_time and category_id not in ("+shuffleCatId+") ");
			}
			else if(m_customer.equalsIgnoreCase("idea")){
				//queryBuffer.append("and category_id not in ("+shuffleCatId+")  and start_time > ('01-Jan-2005') and ((end_time - start_time)>'0.125') and next_charging_date is not null ");
				queryBuffer.append("and category_id not in ("+shuffleCatId+") and start_time != end_time and start_time > ('01-Jan-2005') and next_charging_date is not null ");
			}else if(m_customer.equalsIgnoreCase("bsnl")){
				queryBuffer.append("and category_id not in ("+shuffleCatId+") and start_time != end_time and start_time > ('01-Jan-2005') ");
			}
		}
		queryBuffer.append("group by CLASS_TYPE,category_id, subscriber_wav_file");
		logger.info("STARTED TUNES SELECTION POPULATION BASED ON STAGING TABLE Going to execute Query " + queryBuffer.toString());

        try
        {
            morp_conn2 = getMorpheusConnection();
			long lPreTime = System.currentTimeMillis();
			m_rs1 = m_stmt1.executeQuery(queryBuffer.toString());
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ queryBuffer.toString()+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            boolean done = true;
            while (m_rs1.next())
            {
                String wav_file = m_rs1.getString("subscriber_wav_file");
                int categoryID = m_rs1.getInt("category_id");
                if (m_rs1.getString("category_id") == null)
                    continue;
                String category = null;
                if ((m_customer.equalsIgnoreCase("tata")) && categories.get(m_rs1.getString("category_id")) == null)
                {
                    category = (String) shuffleCat.get(m_rs1.getString("category_id"));
                }
                else
                {
                    category = (String) categories.get(m_rs1.getString("category_id"));
                }
                String classType = m_rs1.getString("CLASS_TYPE");
                int subs = m_rs1.getInt("totSel");
                if (wav_file == null || wav_file.startsWith("9") || wav_file.startsWith("null"))
                    continue;
                query = "insert into RBT_TUNES_SELECTIONS_REPORT values("
                        + site_id + ",'" + wav_file + "','" + category
                        + "',to_date('" + report_date + "','YYYY-MM-DD'),0,"
                        + subs + "," + categoryID + ",'" + classType + "')";
						lPreTime = System.currentTimeMillis();
						m_stmt2.executeUpdate(query);
						lPostTime = System.currentTimeMillis();
						logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
               

            }//end of First while Loop
            if (!m_customer.equalsIgnoreCase("tata") && !m_customer.equalsIgnoreCase("esia"))
            {
				queryBuffer = new StringBuffer();
				queryBuffer = queryBuffer.append("select CLASS_TYPE,category_id,subscriber_wav_file,count(subscriber_id) as totSel from rbt_SELECTIONS_STAGING ");
				queryBuffer.append("where (start_time >= to_date('"+ report_date+ "', 'yyyy-MM-dd') and start_time < to_date('"+ next_date+ "', 'yyyy-MM-dd')) ");
				queryBuffer.append("and start_time != end_time  and site_id = '"+site_id+"' and category_id in ("+shuffleCatId+") ");
				if(m_customer.equalsIgnoreCase("idea"))
					//queryBuffer.append("and start_time > ('01-Jan-2005') and ((end_time - start_time)>'0.125') and next_charging_date is not null ");
					queryBuffer.append(" and start_time != end_time and start_time > ('01-Jan-2005') and next_charging_date is not null ");
				if(m_customer.equalsIgnoreCase("bsnl"))
					queryBuffer.append("and start_time != end_time and start_time > ('01-Jan-2005') ");
				queryBuffer.append("group by CLASS_TYPE, category_id,subscriber_wav_file");
                logger.info("Shuffle Tunes strQuery :"+ queryBuffer.toString());
				lPreTime = System.currentTimeMillis();
				m_rs1 = m_stmt1.executeQuery(queryBuffer.toString());
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON:Query=>"+ queryBuffer.toString()+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
				while(m_rs1.next())
				{
			        int categoryID = m_rs1.getInt("category_id");
                    if (m_rs1.getString("category_id") == null)
                        continue;
                    String category = (String) categories.get(m_rs1.getString("category_id"));
                    String classType = m_rs1.getString("CLASS_TYPE");
                    int subs = m_rs1.getInt("totSel");
					String wav_file = m_rs1.getString("subscriber_wav_file");
                    query = "insert into RBT_TUNES_SELECTIONS_REPORT values("+ site_id+ ",'"+ wav_file+ "','"+ category+ "',to_date('"+ report_date+ "','YYYY-MM-DD'),0,"+ subs+ ","+ categoryID + ",'" + classType + "_SHUFFLE')";
					lPreTime = System.currentTimeMillis();
					m_stmt2.executeUpdate(query);
					lPostTime = System.currentTimeMillis();
					logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
                
					
				}
            }
	        logger.info("POOJA POOJA :::: ENDED TUNES SELECTION POPULATION BASED ON STAGING TABLE");
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
            Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,REMARKS_TUNE_SELECTION + report_date,REMARKS_TYPE_REPORT_ERROR);
        }

        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    } 
 
/*    private void populateSelectionsReport(String site_id, String report_date,
            String next_date)
    {
        String _method = "populateSelectionsReport";
        Connection morp_conn2 = null;
        String query = null;
        Hashtable shuffleCat = null;
        Hashtable categories = null;
        String shuffleCatId = null;
        if (m_customer.equalsIgnoreCase("esia"))
            categories = getCategories("" + 150);
        else
            categories = getCategories(site_id);
		
        if (m_customer.equalsIgnoreCase("tata"))
        {
            shuffleCat = getTataShuffleCategories(site_id);
            query = "select CLASS_TYPE, category_id, subscriber_wav_file, count(subscriber_id) as totSel from rbt_SUBS_selections_REPORT "
                    + "where (start_time >= to_date('"
                    + report_date
                    + "', 'yyyy-MM-dd') and start_time < to_date('"
                    + next_date
                    + "', 'yyyy-MM-dd')) "
                    + "and site_id= '"
                    + site_id
                    + "' group by CLASS_TYPE,category_id, subscriber_wav_file";
        }
        else if (m_customer.equalsIgnoreCase("esia"))
            query = "select CLASS_TYPE,category_id, subscriber_wav_file, count(SUBSCRIBER_ID) as totSel from rbt_SELECTIONS_STAGING where class_type NOT LIKE 'TRIAL%' and "
                    + "SITE_ID ='"
                    + site_id
                    + "' AND next_charging_date is not null and ((next_charging_date >= to_date ('"
                    + report_date
                    + "','YYYY-MM-DD') "
                    + "and next_charging_date < to_date ('"
                    + next_date
                    + "','YYYY-MM-DD')) "
                    + "or (start_time >= to_date ('"
                    + report_date
                    + "','YYYY-MM-DD') and start_time < to_date ('"
                    + next_date
                    + "','YYYY-MM-DD') "
                    + "and next_charging_date > to_date ('2036-01-01','yyyy-MM-dd'))) group by CLASS_TYPE,category_id, subscriber_wav_file";
        else
        {
            shuffleCatId = getShuffleCatId(site_id);
            query = "select CLASS_TYPE,category_id, subscriber_wav_file, count(subscriber_id) as totSel from rbt_SELECTIONS_STAGING "
                    + "where (start_time >= to_date('"
                    + report_date
                    + "', 'yyyy-MM-dd') and start_time < to_date('"
                    + next_date
                    + "', 'yyyy-MM-dd')) "
                    + "and site_id= '"
                    + site_id
                    + "' and category_id not in ("
                    + shuffleCatId
                    + ") group by CLASS_TYPE, category_id, subscriber_wav_file";
        }
        logger.info("STARTED TUNES SELECTION POPULATION BASED ON STAGING TABLE Going to execute Query " + query);

        try
        {
            morp_conn2 = getMorpheusConnection();
            m_rs1 = m_stmt1.executeQuery(query);
            boolean done = true;
            while (m_rs1.next())
            {
                String wav_file = m_rs1.getString("subscriber_wav_file");
                int categoryID = m_rs1.getInt("category_id");
                if (m_rs1.getString("category_id") == null)
                    continue;
                String category = null;
                if ((m_customer.equalsIgnoreCase("tata"))
                        && categories.get(m_rs1.getString("category_id")) == null)
                {
                    category = (String) shuffleCat.get(m_rs1
                            .getString("category_id"));
                }
                else
                {
                    category = (String) categories.get(m_rs1
                            .getString("category_id"));
                }
                String classType = m_rs1.getString("CLASS_TYPE");
                int subs = m_rs1.getInt("totSel");
                if (wav_file == null || wav_file.startsWith("9")
                        || wav_file.startsWith("null"))
                    continue;
                query = "insert into RBT_TUNES_SELECTIONS_REPORT values("
                        + site_id + ",'" + wav_file + "','" + category
                        + "',to_date('" + report_date + "','YYYY-MM-DD'),0,"
                        + subs + "," + categoryID + ",'" + classType + "')";
                m_stmt2.executeUpdate(query);

            }//end of First while Loop
            if (!m_customer.equalsIgnoreCase("tata")
                    && !m_customer.equalsIgnoreCase("esia"))
            {
                String strQuery = "select CLASS_TYPE,category_id,count(subscriber_id) as totSel from rbt_SELECTIONS_STAGING "
                        + "where (start_time >= to_date('"
                        + report_date
                        + "', 'yyyy-MM-dd') and start_time < to_date('"
                        + next_date
                        + "', 'yyyy-MM-dd')) "
                        + "and site_id= '"
                        + site_id
                        + "' and category_id in ("
                        + shuffleCatId
                        + ") group by CLASS_TYPE, category_id";
                logger.info("Shuffle Tunes strQuery :"
                        + strQuery);

                Hashtable clipId = getClipFrCatShuffleId(site_id, shuffleCatId);
                ArrayList clipName = new ArrayList();
                m_rs1 = m_stmt1.executeQuery(strQuery);
                int count = 0;
                while (m_rs1.next())
                {
                    int categoryID = m_rs1.getInt("category_id");
                    if (m_rs1.getString("category_id") == null)
                        continue;
                    String category = (String) categories.get(m_rs1
                            .getString("category_id"));
                    String classType = m_rs1.getString("CLASS_TYPE");
                    int subs = m_rs1.getInt("totSel");
                    String clipNo = (String) clipId.get(m_rs1
                            .getString("category_id"));
                    logger.info("PUJA : TESTING : &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& :category"
                                            + categoryID + " and clipNo :"
                                            + clipNo);
                    if (clipNo == null || clipNo.equalsIgnoreCase("null"))
                        continue;
                    clipName = getTunes(site_id, clipNo);
                    for (int no = 0; no < clipName.size(); no++)
                    {
                        String wav_file = (String) clipName.get(no);
                        query = "insert into RBT_TUNES_SELECTIONS_REPORT values("
                                + site_id
                                + ",'"
                                + wav_file
                                + "','"
                                + category
                                + "',to_date('"
                                + report_date
                                + "','YYYY-MM-DD'),0,"
                                + subs
                                + ","
                                + categoryID + ",'" + classType + "')";
                        count++;
                        m_stmt2.executeUpdate(query);
                    }
                }
                logger.info("Total records under Shuffle in tune_selections table :" + count);
            }
	        logger.info("POOJA POOJA :::: ENDED TUNES SELECTION POPULATION BASED ON STAGING TABLE");
        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
            Tools.addToAuditTable(morp_conn2, site_id, ISSUE_WARNING,REMARKS_TUNE_SELECTION + report_date,REMARKS_TYPE_REPORT_ERROR);
        }

        try
        {
            if (morp_conn2 != null)
                morp_conn2.close();
        }
        catch (Exception e)
        {
        }
    } */

 
    public static void main(String[] args) throws ParseException
    {
        String _method = "main";
        RBTReporter rbtreporter = new RBTReporter();
        if (args == null || args.length < 3)
        {
            System.out.println(" Usage: RBTReporter Customer daemonport daemon [reportdate zipfilepath/none sitename/all noofdaystoload]");
            System.exit(1);
        }
		//yyyy/MM/dd HH:mm:ss
		try
		{
			m_customer = args[0].trim().toLowerCase();
		}
		catch (Exception e)
		{
			System.out.println("Fundamental Error...");
			e.printStackTrace();
			System.exit(1);
		}
	 	try
		{
				daemon_port = Integer.parseInt(args[1].trim());
				
		}
		catch(Exception e)
		{
			
			
		}  
		boolean bShouldRunAsDaemon = false;
		if(args[2].equalsIgnoreCase("daemon"))
		{
			bShouldRunAsDaemon = true;
			try {
				rbt_daemon_socket = new ServerSocket (daemon_port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("The port for "+m_customer+" and port is "+daemon_port+ " is in use or another rbt daemon is already running");
				System.exit(-1);
			}
		}
 
		if (args.length > 3) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			maxProcessDate =  sdf.parse(args[3].trim());
			
		}

		if (args.length > 4) {
			m_strZipFileName = (String)args[4];
		}
		
		if (args.length > 5) {
			m_strSite = (String)args[5];
		}
		
		if(args.length > 6)
		{
		    m_no_of_days_toload = (String)args[6];
		}
		
		if(maxProcessDate== null)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			try {
				Date date  = new Date(System.currentTimeMillis());
				String strDate = sdf.format(date);
				maxProcessDate= sdf.parse(strDate);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		System.out.println(" In the arguments not there . The  value fo the max date is "+maxProcessDate);
		while(bKeepRunning)
		{
			System.out.println("ENTERED INTO WHILE LOOP ::::::: ///////////////////////////////////////////");
			flagSysDate=true;
			bKeepRunning = bShouldRunAsDaemon;
			System.out.println("ENTERED INTO bKeepRunning LOOP :::::::"+bKeepRunning+" AND bShouldRunAsDaemon ::"+bShouldRunAsDaemon);
			try
			{
				if (!rbtreporter.init())
				{
					System.out.println("Could not initialize RBTReporter");
// 					System.exit(1);
				}
				if (m_customer.equalsIgnoreCase("tata") || m_customer.equalsIgnoreCase("esia"))
				{
					while (!rbtreporter.populateMainFile(false))
					{
						System.out.println("populateMainFile returned False sleep and ping after an interval");
						System.out.println("In the main itself returned false....So exiting the main method");
						bKeepRunning = false;
						break;
						/*try
						{
							System.out.println("The daemon is going to sleep for " + m_SleepInterval+" as no files are there to process....");
							Thread.sleep(m_SleepInterval);
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
							try {
								Date date  = new Date(System.currentTimeMillis());
								String strDate = sdf.format(date);
								maxProcessDate= sdf.parse(strDate);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						System.out.println(" ********After sleeeping the max process date is *********" +maxProcessDate);*/
					}
				}
				rbtreporter.begin();
				if(!m_customer.equalsIgnoreCase("esia"))
				{
					if(bKeepRunning)
					{
						System.out.println("The daemon is going to sleep for " + m_SleepInterval+" as no files are there to process....");
						Thread.sleep(m_SleepInterval);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
						try {
							Date date  = new Date(System.currentTimeMillis());
							String strDate = sdf.format(date);
							maxProcessDate= sdf.parse(strDate);
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.out.println(" ********After sleeeping the max process date is *********" +maxProcessDate);
						
					}
				}
				System.out.println("The valur of the m_morpheus "+m_morpheus);
				//m_morpheus.clean();
				
				m_morpheus = null;
				m_conn =null;
			}
			catch (Exception e)
			{
				logger.error("", e);
				e.printStackTrace();
			}
		}
       
		// normal exiting to be rmeoved to make it a daemon
		System.out.println("Finished Processing for customer "+ m_customer + " for Zip File :" + m_strZipFileName);
        System.exit(0);
    }
    
    
    private boolean populateTataPromoMaster(String cust, String site, String day)
    {
		 String _method = "populateTataPromoMaster";
		 logger.info(" entered the method populateTataPromoMaster ");
       
        try{
			logger.info("inside the try ./" + m_db_root + File.separator + cust + "-"+ site + File.separator + day + File.separator + "RBT_PROMO_MASTER.txt");
            String strReadLine=null;
           File file = new File("./" + m_db_root + File.separator + cust + "-"+ site + File.separator + day + File.separator+m_db_end+File.separator +"RBT_PROMO_MASTER.txt");
           logger.info(" goin to check for the file "+file.getAbsolutePath());

			if(!file.exists()){
                logger.info("RBT_PROMO_MASTER.txt DOES NOT EXISTS ");
                return false;
            }
            FileReader objFileSub = new FileReader(file);
            BufferedReader brSub = new BufferedReader(objFileSub);     
            brSub.readLine();//to skip the header
            while((strReadLine = brSub.readLine()) != null)
             {
                //System.out.println( linesoftext.readLine() );
                insertintoPromoMaster(strReadLine);
            }
            // Close the input stream
            brSub.close();
            objFileSub.close();
        }
            catch (Exception e) {
                e.printStackTrace();
            }
			return true;
    }
    String[] splitted=null;
   private void insertintoPromoMaster(String linesoftext){
	   // logger.info("entered the method");
       if(linesoftext==null){
           return;
       }
	   splitted=null;
       splitted= linesoftext.split(",");
       if(splitted==null){
           return;
       }
	   String query=null;
       try{ if (splitted.length == 3) {
       
             query= "insert into RBT_PROMO_MASTER (CLIP_PROMO_ID,PROMO_TYPE,PROMO_CODE) VALUES ('"+splitted[0]+"','"+splitted[1]+"','"+splitted[2]+"')";
             m_stmt1.executeUpdate(query);
	   }}
	   catch (Exception e) {
		   query = "update  RBT_PROMO_MASTER  set CLIP_PROMO_ID='"+splitted[0]+"',PROMO_TYPE= '"+splitted[1]+"' ,PROMO_CODE= '"+splitted[2]+"' where CLIP_PROMO_ID='"+splitted[0]+"'";
           try{ m_stmt1.executeUpdate(query);
            logger.info("PERFMON: insertion failed so updated with Query=>"+ query);
             
		   }
		   catch (Exception ex) {
                ex.printStackTrace();
            }
            }
          //  logger.info("PERFMON: Query=>"+ query);
            
        //  logger.info("POOJA :: POOJA UPDATE RBT_SOBD_FAILSTATUS_REPORT ::"+query+" AND RESULT :"+result);
           
		
   }
}

