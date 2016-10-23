/*
 * 
 * Created on Sep 3, 2004
 */
package com.onmobile.apps.ringbacktones.Reporter;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.onmobile.common.debug.DebugManager;

/**
 * @author Mohsin
 */
public class FTPHandler
{
	private static Logger logger = Logger.getLogger(FTPHandler.class);
	
    private FTPClient m_ftpclient;
    private FTPConfig m_ftpconfig;
    private DebugManager Debug;
    //private String m_remoteFTPSubDir="IVMReporter";
    private String _dateformat = "yyyy-MM-dd";

    /**
     * c'tor
     */
    public FTPHandler(FTPConfig config)
    {
        m_ftpconfig = config;
    }

    public boolean upload(String _sourceFilenameWithCompletePath)
    {
        logger.info("entered.");
        boolean ftp_success = false;
        int ftp_retries = 0;

        while (ftp_retries < m_ftpconfig.get_retries() && ftp_success == false)
        {
            try
            {
                ftp_success = loginToFTPSite();
                if (ftp_success)
                    logger.info("login successful");
                ftp_success = chdirToFTPDir();
                if (ftp_success)
                    logger.info("chdir successful");
                ftp_success = uploadFile(_sourceFilenameWithCompletePath);
                if (ftp_success)
                    logger.info("transfer successful");
            }
            catch (Exception exe)
            {
                logger.error("", exe);
                ftp_success = false;
                try
                {
                    Thread.sleep(m_ftpconfig.get_waitperiod());
                }
                catch (Exception e)
                {
                }
            }
            ftp_retries++;
        } //end while

        return ftp_success;
    }

    /**
     * @return boolean success or failure.
     * @throws IOException
     * @throws FTPException
     */
    private boolean chdirToFTPDir() throws IOException, FTPException
    {
        boolean chdir_success = true;
        String remotedir = m_ftpconfig.get_dir();
        logger.info("entered.");
        logger.info("remote dir=" + remotedir);

        try
        {
            m_ftpclient.chdir(remotedir);
        }
        catch (IOException ioe)
        {
            throw ioe;
        }
        catch (FTPException ftpe)
        {
            throw ftpe;
        }

        return chdir_success;
    }

    /**
     * @param _targetFileNameWithCompletePath
     * @return Downloads the file from ftp dir(given in VMFTPConfig) to target
     *         file path.
     */
    public boolean download(String _targetFileNameWithCompletePath)
    {
        logger.info("entered.");
        boolean ftp_success = false;
        int ftp_retries = 0;

        while (ftp_retries < m_ftpconfig.get_retries() && ftp_success == false)
        {
            try
            {
                ftp_success = loginToFTPSite();
                if (ftp_success)
                    logger.info("login successful");
                ftp_success = chdirToFTPDir();
                if (ftp_success)
                    logger.info("chdir successful");

                //separate local and remote file paths
                String localpath = _targetFileNameWithCompletePath;
                int index = localpath.lastIndexOf(File.separator);
                String remotefile = localpath.substring(index);

                ftp_success = downloadFile(localpath, remotefile);
                if (ftp_success)
                    logger.info("transfer successful");
            }
            catch (Exception exe)
            {
                logger.error("", exe);
                ftp_success = false;
                try
                {
                    logger.info("transfer failed: Sleeping for "
                                           + m_ftpconfig.get_waitperiod()
                                           / 1000 + " seconds");
                    Thread.sleep(m_ftpconfig.get_waitperiod());
                }
                catch (Exception e)
                {
                }
            }
            ftp_retries++;
        } //end while

        return ftp_success;
    }

    public boolean download(String _localDownloadDir, String zipfile)
    {
        String cust, site;
        boolean ftp_success = false, zip_file_found = false;
        int ftp_retries = 0;
        while (ftp_retries < m_ftpconfig.get_retries() && ftp_success == false)
        {
            try
            {
                ftp_success = loginToFTPSite();
                if (ftp_success)
                    logger.info("login successful");
                ftp_success = chdirToFTPDir();
                if (ftp_success)
                    logger.info("chdir successful");
                String[] files = getRemoteFilesList();
                if (files == null)
                {
                    logger.info("No files in FTP Download unsuccessful");
                    return false;
                }
                else
                {
                    for (int i = 0; i < files.length; i++)
                    {
                        if (files[i].equalsIgnoreCase(zipfile))
                        {
                            zip_file_found = true;
                            break;
                        }
                    }
                }
                if (zip_file_found)
                {
                    logger.info("zip file found : downloading " + zipfile);
                    String localpath = _localDownloadDir + File.separator
                            + zipfile;
                    //check if local download dir exists or not.
                    File localdir = new File(_localDownloadDir);
                    if (!localdir.isDirectory())
                    {
                        localdir.mkdir();
                    }
                    ftp_success = downloadFile(localpath, files[0]);
                    if (ftp_success)
                    {
                        StringTokenizer underscores = new StringTokenizer(
                                zipfile, "_");
                        underscores.nextToken();// first is db
                        cust = underscores.nextToken();
                        site = underscores.nextToken();
                        //MorpheusUpdater Obj = new MorpheusUpdater();
                        //Obj.SubdirDelete(cust+"-"+site);

                    }
                    logger.info("download successful: localpath="
                                           + localpath + ", remotefile="
                                           + zipfile);
                    m_ftpclient.delete(zipfile);
                }
                else
                {
                    logger.info(zipfile
                            + " not in FTP SITE : Download Unsuccessful");
                    return false;
                }

            }
            catch (Exception exe)
            {
                logger.error("", exe);
                ftp_success = false;
                try
                {
                    Thread.sleep(m_ftpconfig.get_waitperiod());
                }
                catch (Exception e)
                {
                }
            }
            ftp_retries++;
        } //end while
        quitFTP();
        return ftp_success;
    }

    public boolean downloadAll(String _localDownloadDir,String dir)
    {
        logger.info("entered.");
        Hashtable htsite = new Hashtable();
        boolean ftp_success = false;
        int ftp_retries = 0;
        int nDownloaded = 0;
         while (ftp_retries < m_ftpconfig.get_retries() && ftp_success == false)
        {
            try
            {
                 ftp_success = loginToFTPSite();
                if (ftp_success)
                    logger.info("login successful");
				else{
					logger.info("login unsuccessful");
					return false;
				}
                ftp_success = chdirToFTPDir();
                if (ftp_success)
                    logger.info("chdir successful");
				else
				{
					logger.info("chdir unsuccessful");
					return false;
				}
				ftp_success = false;
                String[] files = getRemoteFilesList();
                if (files == null)
                {
                    logger.info("Didn't get files to download. Download unsuccessful");
                    return false;
                }

                for (int i = 0; i < files.length; i++)
                {
                    if (!files[i].startsWith(RBTReporter.ftpFilePrefix) || !files[i].endsWith(".zip"))
                    {
                    	// Proneel temp addition to rename bad files automatically
                    	if (!RBTReporter.bKeepRunning && files[i].startsWith("DONE_" + RBTReporter.ftpFilePrefix)) {
                    		// rename and use
                    		String newFileName = files[i].substring("DONE_".length());
							m_ftpclient.rename(files[i],newFileName);
                            logger.info(files[i]+ " Used after renaming");
							files[i] = newFileName;
                    	} else {
                            logger.info(files[i]+ " File not rbt gatherer's");
                            continue;
                    	}
                    }
					File file = new File(files[i]);
					if (!Tools.chkYesterdayZip(file))
					{
						logger.info("RBT Gatherer zip file is of wrong Date "+files[i]);
						//alarm
//						m_ftpclient.delete(files[i]);
	                    continue;
		            }
					//This is to get the Appropriate File Name
					Calendar cal = Tools.getCalendarInstance();
					cal.add(Calendar.DATE,-1);
					String day = Tools.getDateAsName(cal.getTime());
					String cust = dir.substring(0,dir.indexOf("-"));
					String siteName = dir.substring(dir.indexOf("-")+1);

					String fileName = RBTReporter.ftpFilePrefix + cust+"_"+siteName+ "_" + day+".zip";

 					logger.info("RENAMING THE GATHERER ZIP FILE - "+files[i]+" TO file Name :"+fileName);
					m_ftpclient.rename(files[i],fileName);
					               
				    String localpath = _localDownloadDir + File.separator+ fileName;
                    //check if local download dir exists or not.
                    File localdir = new File(_localDownloadDir);
                    if (!localdir.isDirectory())
                    {
                        boolean dirFlag = localdir.mkdirs();
                    }
                    ftp_success = downloadFile(localpath, fileName);
                    if (ftp_success)
                    {
                        /*
                         * StringTokenizer underscores=new
                         * StringTokenizer(files[i], "_"); ArrayList list=new
                         * ArrayList(); while(underscores.hasMoreTokens()){
                         * list.add(underscores.nextToken()); } String
                         * cust=(String)list.get(1); String
                         * site=(String)list.get(2);
                         * if(!htsite.containsValue(site)) {
                         * htsite.put(String.valueOf(i),site); //
                         * MorpheusUpdater Obj = new MorpheusUpdater(); //
                         * Obj.SubdirDelete(cust+"-"+site); }
                         */
                        logger.info("download successful: localpath=" + localpath + ", remotefile=" +fileName);
                        nDownloaded++;

//                        m_ftpclient.delete(fileName);
						 String  donefileName = "DONE_" + RBTReporter.ftpFilePrefix + cust+"_"+siteName+ "_" + day+".zip";
						  m_ftpclient.rename(fileName,donefileName);
                    }
                }
            }
            catch (Exception exe)
            {
                logger.error("", exe);
                ftp_success = false;
                try
                {
                    Thread.sleep(m_ftpconfig.get_waitperiod());
                }
                catch (Exception e)
                {
                }
            }
             ftp_retries++;
        } //end while
        logger.info("No. of zip files downloaded= " + nDownloaded);
        quitFTP();
        return ftp_success;
    }

    private void quitFTP()
    {
        try
        {
            m_ftpclient.quit();
        }
        catch (IOException e)
        {
        }
        catch (FTPException e)
        {
        }
    }

    private boolean loginToFTPSite() throws FTPException, IOException
    {
        logger.info("entered.");
        try
        {
            m_ftpclient = new FTPClient(m_ftpconfig.get_host(), m_ftpconfig
                    .get_port());
            m_ftpclient.setTimeout(m_ftpconfig.get_timeout());
            m_ftpclient.login(m_ftpconfig.get_user(), m_ftpconfig.get_pwd());
            m_ftpclient.setConnectMode(FTPConnectMode.PASV);
            m_ftpclient.setType(FTPTransferType.BINARY);
        }
        catch (IOException ioe)
        {
            throw ioe;
        }
        catch (FTPException ftpe)
        {
            throw ftpe;
        }
        logger.info("logged in, changed to appropriate dir... returning.");
        return true;
    }

    private boolean uploadFile(String path) throws FTPException, IOException
    {
        logger.info("entered.");
        String localpath = path;
        int index = localpath.lastIndexOf(File.separator);
        String remotefile = localpath.substring(index);
        File _xmlfile = new File(path);
        if (_xmlfile.exists())
        {
            try
            {
                m_ftpclient.put(localpath, remotefile);
            }
            catch (IOException ioe)
            {
                throw ioe;
            }
            catch (FTPException ftpe)
            {
                throw ftpe;
            }
        }
        return true;
    }

    private boolean downloadFile(String localpath, String remotefile)
            throws FTPException, IOException
    {
		boolean flag = false;
        logger.info("entered.");
        try
        {
            m_ftpclient.get(localpath, remotefile);
			flag= true;
        }
        catch (IOException ioe)
        {
             throw ioe;
        }
        catch (FTPException ftpe)
        {
             throw ftpe;
        }
         return flag;
    }

    /* helper methods */

    /**
     * @return list of filenames in the remote ftp location.
     * @throws IOException
     * @throws FTPException
     */
    private String[] getRemoteFilesList() throws IOException, FTPException
    {
        String[] fileslist = null;
        try
        {
            fileslist = m_ftpclient.dir();
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (FTPException e)
        {
            throw e;
        }

        return fileslist;
    }
    //for testing.
    public static void main(String[] args)
    {
    }
}