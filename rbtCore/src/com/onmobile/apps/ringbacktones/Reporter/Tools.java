/*
 * Created on Nov 21, 2004
 *  
 */
package com.onmobile.apps.ringbacktones.Reporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.onmobile.common.debug.DebugManager;

/**
 * @author Mohsin
 *  
 */
public class Tools
{
	private static Logger logger = Logger.getLogger(Tools.class);
	
    private static String _module = "RBTReporter";
    private static String m_logRootDir = ".";
    private static String m_logFileName = "RBTReporterLog"
            + getChangedFileDate(Calendar.getInstance().getTime()) + ".txt";
    private static String m_errorFileName = "RBTReporterLog_error"
            + getChangedFileDate(Calendar.getInstance().getTime()) + ".txt";
    private static boolean m_bConsoleEcho = true;
    private static final String _DATEFORMAT12 = "yyyyMMddhhmmssSS a";
    private static final String _FILENAME_DATEFORMAT = "yyyy-MMM-dd";
    private static DebugManager Debug = null;

    private static String m_workingDir = null;
    private static String m_backUpDir = null;
    private static String m_data_path = "data";
    private static String m_resource_file = "resources/rbtreporter";
    private static ResourceBundle m_bundle = null;

    public static boolean init(ResourceBundle bundle)
    {
        try
        {
            m_bundle = ResourceBundle.getBundle(m_resource_file);
        }
        catch (Exception e)
        {
            System.out.println("error in getting bundle:" + m_resource_file
                    + " !!!");
            e.printStackTrace();
            return false;
        }
        try
        {
            m_workingDir = m_bundle.getString("WORKING_FOLDER");
        }
        catch (Exception E)
        {
            m_workingDir = ".";
        }

        try
        {
            m_backUpDir = m_bundle.getString("BACKUP_DIRECTORY");
        }
        catch (Exception E)
        {
            m_backUpDir = ".";
        }

        try
        {
            DebugManager.init(6, _module, m_logRootDir, m_logFileName,
                              m_errorFileName, "size", 100000,  10,true);
            Debug = DebugManager.getDebugManagerObject(7, m_bConsoleEcho,
                                                       _module);
            if (Debug == null)
            {
                System.out.println("The DebugManager couldn't be initialised.");
                return false;
            }
        }
        catch (Exception e)
        {
            System.out.println("EXCEPTION " + e);
            return false;
        }

        return true;
    }

    public static String getFormattedDate(long millis, String pattern)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String datestr = sdf.format(new java.util.Date(millis));
        if (pattern.equalsIgnoreCase(_DATEFORMAT12))
        {
            String temp = datestr.substring(0, 16)
                    + datestr.substring(datestr.length() - 3, datestr.length());
            datestr = temp;
        }
        return datestr;
    }

    public static void deleteOldLogs(Date delete_logs_last_date)
    {
        File[] logFiles = new File(m_logRootDir + File.separator + "log"
                + File.separator + _module).listFiles();

        if (logFiles != null) {
	        for (int i = 0; i < logFiles.length; i++)
	        {
	            Date date = new Date(logFiles[i].lastModified());
	            if (date.before(delete_logs_last_date))
	                logFiles[i].delete();
	        }
        }
    }
    
    public static void SubdirDelete(String site, Date crntdate)
    {
        String dir = m_workingDir;

        Calendar cal = Tools.getCalendarInstance();
        cal.add(Calendar.DATE, -3);
        Date yest = cal.getTime();

        if (crntdate.before(yest))
            dir = m_backUpDir;

        File[] dir_list = new File(dir + File.separator + m_data_path
                + File.separator + site).listFiles();

        logger.info("crntDate " + crntdate + " yest "
                + yest);

        if (dir_list == null || dir_list.length == 0)
        {
            logger.info("  No sub directories in site:  "
                    + site);
        }
        else if (crntdate.after(yest))
        {
            for (int index = 0; index < dir_list.length; index++)
            {
                Date date = getDate(dir_list[index].getName().trim(),
                                    "yyyy-MMM-dd");
                if (date.before(crntdate))
                {
                    logger.info(" copy true deleting Subdirectories of: "
                                      + dir_list[index].getName());
                    recursiveDelete(dir_list[index], dir, true);
                }
            }
        }
        else
        {
            for (int index = 0; index < dir_list.length; index++)
            {
                Date date = getDate(dir_list[index].getName().trim(),
                                    "yyyy-MMM-dd");
                if (date.before(crntdate))
                {
                    logger.info("copy false deleting Subdirectories of: "
                                      + dir_list[index].getName());
                    recursiveDelete(dir_list[index], dir, false);
                }
            }

        }

    }

    private static void recursiveDelete(File FiletoDelete, String dir,
            boolean copy)
    {
        if (FiletoDelete.isFile())
        {
            if (copy)
            {
                try
                {
                    copyFile(FiletoDelete.toString().trim(), m_backUpDir
                            + File.separator + FiletoDelete.toString().trim());
                }
                catch (Exception e)
                {
                    logger.error("", e);
                }
            }
            FiletoDelete.delete();
            return;
        }
        else
        {
            if (copy)
            {
                File dir_list = new File(m_backUpDir + File.separator
                        + FiletoDelete.toString());
                if (!dir_list.exists())
                    dir_list.mkdirs();
            }
            File[] Subdir = FiletoDelete.listFiles();
            if (Subdir == null || Subdir.length == 0)
            {
                FiletoDelete.delete();
                return;
            }
            for (int i = 0; i < Subdir.length; i++)
            {
                recursiveDelete(Subdir[i], dir, copy);
                FiletoDelete.delete();
            }
        }
    }

    public static String getChangedFormatDate(Date TmpDate)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date_str = sdf.format(TmpDate);
        return date_str;
    }

    public static String getContentDate(Date TmpDate, String pattern)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        String date_str = "";

        try
        {
            date_str = sdf.format(TmpDate);
        }
        catch (Exception e)
        {

        }
        return date_str;
    }

    public static Date getDate(String TmpDate, String pattern)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);


		Calendar _cal = Tools.getCalendarInstance();
        Date date = _cal.getTime();
		
        try
        {
            date = sdf.parse(TmpDate);
        }
        catch (Exception e)
        {

        }
        return date;
    }

    public static String getChangedFileDate(Date TmpDate)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddhh24mmss");
        String date_str = sdf.format(TmpDate);
        return date_str;
    }

    static void addToAuditTable(Connection conn, String site_id,
            String issue_type, String remarks, String remarks_type)
    {
/*
        String _module = "addToAuditTable";

        Tools.logDetail(_class, _module, "inside " + _module);

        int id = -1;
        String query = null;
        Statement stmt = null;
        ResultSet results = null;

        String date = "SYSDATE";

        String TABLE_NAME = "REP_AUDIT_TABLE";
        String SITE_ID_COL = "SITE_ID";
        String PRODUCT_ID_COL = "PRODUCT_ID";
        String REPORT_TIME_COL = "REPORT_TIMESTAMP";
        String ISSUE_TYPE_COL = "ISSUE_TYPE";
        String REMARKS_COL = "REMARKS";
        String REMARKS_TYPE = "REMARKS_TYPE";

        query = "INSERT INTO " + TABLE_NAME + " ( " + SITE_ID_COL;
        query += ", " + PRODUCT_ID_COL;
        query += ", " + REPORT_TIME_COL;
        query += ", " + ISSUE_TYPE_COL;
        query += ", " + REMARKS_COL;
        query += ", " + REMARKS_TYPE;
        query += ")";

        query += " VALUES ( ";
        if (site_id == null)
            query += null;
        else
            query += site_id;
        query += ", 'RBT'";
        query += ", " + date;
        query += ", '" + issue_type + "'";
        query += ", '" + remarks + "'";
        query += ", '" + remarks_type + "'";
        query += ")";

        Tools.logDetail(_class, _module, "Executing Query " + query);

        try
        {
            stmt = conn.createStatement();
            if (stmt.executeUpdate(query) > 0)
                id = 0;
        }
        catch (SQLException se)
        {
            Tools.logFatalError(_class, _module, "Exception " + se);
            return;
        }
        finally
        {
            try
            {
                stmt.close();
            }
            catch (Exception e)
            {
                Tools.logWarning(_class, _module, "Exception " + e);
            }
        }
        if (id != 0)
        {
            Tools.logDetail(_class, _module, "insertion to " + TABLE_NAME
                    + " table failed");
            return;
        }
*/    }

    public static String getDateAsName(java.util.Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(_FILENAME_DATEFORMAT);
        return sdf.format(date);
    }

    public static long getStartDate()
    {
        //start of today. so pgm runs for yesterday.
        Calendar _cal = Tools.getCalendarInstance();
        _cal.set(Calendar.HOUR_OF_DAY, 0);
        _cal.set(Calendar.MINUTE, 0);
        _cal.set(Calendar.SECOND, 0);
        _cal.set(Calendar.MILLISECOND, 0);
        return _cal.getTime().getTime();
    }

    /**
     * @param ziplocation
     * @param zipfilename
     * @param files_to_zip If it is null, zips all the files and subfolders.
     * @return success/failure
     */

    public static String getSpiderDate()
    {
		Calendar cal = Tools.getCalendarInstance();
		 
		 SimpleDateFormat sdf = new SimpleDateFormat("MMMd");
		
	        return (sdf.format(cal.getTime()));
		
    }

    public static boolean chkYesterdayZip(File file)
    {
        StringTokenizer token = new StringTokenizer(file.getName(), "_");
        // eliminate extra tokens if the base file name itself has _s in it
        StringTokenizer prefixToks = new StringTokenizer(RBTReporter.ftpFilePrefix, "_");
        prefixToks.nextToken(); // eliminate the 1 token that is already considered
        while (prefixToks.hasMoreTokens()) {
        	token.nextToken();
        	prefixToks.nextToken();
        }
        
        token.nextToken();
        token.nextToken();
        token.nextToken();
        
        String day = token.nextToken();
        day = day.substring(0, day.lastIndexOf("."));//eliminate extn
        SimpleDateFormat sdf = new SimpleDateFormat(_FILENAME_DATEFORMAT);
        Calendar cal = Tools.getCalendarInstance();
        cal.add(Calendar.DATE, -1);
        try
        {
            Date date = sdf.parse(day);
            if (date.equals(sdf.parse(getDateAsName(cal.getTime()))))
                return true;
        }
        catch (Exception E)
        {
            logger.error("", E);
        }

        return false;
    }

    public static boolean unzipFileToDir(File zipFile, String dirFullPath)
    {
        try
        {
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(zipFile));
            ZipInputStream zis = new ZipInputStream(bis);
            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null)
            {
                if (ze.isDirectory())
                {
                    File dir = new File(dirFullPath + File.separator
                            + ze.getName());
                    dir.mkdirs();
                    continue;
                }
                String name = ze.getName();
                String dirName = null;

                if (name.indexOf("/") > -1)
                {
                    String name1 = name;
                    dirName = name1.substring(0, name1.indexOf("/"));
                    name = dirName + "\\"
                            + name1.substring(name1.indexOf("/") + 1);
                    logger.info("index of file separator: " + name1);
                }

                if (dirName != null)
                {
                    File newDir = new File(dirFullPath + File.separator
                            + dirName);
                    newDir.mkdirs();
                }

                logger.info("Creating File: "
                        + dirFullPath + File.separator + name);

                FileOutputStream out = new FileOutputStream(new File(
                        dirFullPath + File.separator + name));
                int bytes, size = 10240;
                byte[] b = new byte[size];
                logger.info("Creating File: " + name);
                while ((bytes = zis.read(b, 0, size)) != -1)
                {
                    out.write(b, 0, bytes);
					out.flush();
                }
				out.close();
            }
            zis.close();
            bis.close();
            return true;
        }
        catch (NullPointerException e)
        {
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static void moveFile(String destdir, File cdrfile)
    {
        File dest = new File(destdir);
        if (!dest.isDirectory())
        {
            logger.info("creating destination dir: " + destdir);
            if (!dest.mkdir())
            {
                logger.info("could not create dest dir.");
            }
        }

        String destfile = destdir + File.separator + cdrfile.getName();
        try
        {
            copyFile(cdrfile.getAbsolutePath(), destfile);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
    }

    private static void copyFile(String source, String destination)
            throws IOException
    {
        try
        {
            logger.info("Copying: " + source + " To: "
                    + destination);
            FileInputStream fis = new FileInputStream(source);
            FileOutputStream fos = new FileOutputStream(destination);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1)
            {
                fos.write(buf, 0, i);
            }
            fis.close();
            fos.close();
            logger.info(destination + " copied.");
        }
        catch (IOException ioe)
        {
            logger.error("", ioe);
            throw ioe;
        }
    }

    public static void deleteFiles(File[] files)
    {
        for (int i = 0; i < files.length; i++)
        {
            files[i].delete();
        }
    }

    //LOGGING METHODS
//    public static void logWarning(String _class, String method, String msg)
//    {
//        Debug.warning(_class, method, msg, Thread.currentThread().getName(),
//                      null);
//    }
//
//    public static void logStatus(String _class, String method, String msg)
//    {
//        Debug.status(_class, method, msg, Thread.currentThread().getName(),
//                     null);
//    }
//
//    public static void logDetail(String _class, String method, String msg)
//    {
//        Debug.detail(_class, method, msg, Thread.currentThread().getName(),
//                     null);
//    }
//
//    public static void logTrace(String _class, String method, String msg)
//    {
//        Debug
//                .trace(_class, method, msg, Thread.currentThread().getName(),
//                       null);
//    }
//
//    public static void logException(String _class, String method, Throwable t)
//    {
//        Debug.exception(_class, method, t, Thread.currentThread().getName(),
//                        null);
//    }
//
//    public static void logNonFatalError(String _class, String method, String msg)
//    {
//        Debug.nonfatalError(_class, method, msg, Thread.currentThread()
//                .getName(), null);
//    }
//
//    public static void logFatalError(String _class, String method, String msg)
//    {
//        Debug.fatalError(_class, method, msg, Thread.currentThread().getName(),
//                         null);
//    }
//
//    public static void logFatalException(String _class, String method,
//            Throwable t)
//    {
//        Debug.fatalException(_class, method, t, Thread.currentThread()
//                .getName(), null);
//    }
    
    
    
    public static Calendar getCalendarInstance() {
        Calendar cal = Calendar.getInstance();
        if(RBTReporter.maxProcessDate!=null){
            
            cal.setTime(RBTReporter.maxProcessDate);
        }
        return cal;
    }
}