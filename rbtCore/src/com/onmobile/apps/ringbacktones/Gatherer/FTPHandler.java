package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.onmobile.common.debug.DebugManager;

/**
 * @author Shrihari
 */
public class FTPHandler
{
    private static Logger logger = Logger.getLogger(FTPHandler.class);
    
    private FTPClient m_ftpclient;
    private FTPConfig m_ftpconfig;
    private DebugManager Debug;

    /**
     * c'tor
     */
    public FTPHandler(FTPConfig config)
    {
        m_ftpconfig = config;
    }

    /**
     * @param filename
     * @return boolean indicating the success/failure of ftp login and directory
     *         change.
     * @throws FTPException
     * @throws IOException
     */
    private boolean loginToFTPSite() throws FTPException, IOException
    {
        String ftpdir = m_ftpconfig.get_dir();
        logger.info("entered. ftp dir from config:"
                + ftpdir);
        try
        {
            m_ftpclient = new FTPClient(m_ftpconfig.get_host(), m_ftpconfig
                    .get_port());
            m_ftpclient.setTimeout(m_ftpconfig.get_timeout());
            m_ftpclient.login(m_ftpconfig.get_user(), m_ftpconfig.get_pwd());
            m_ftpclient.setConnectMode(FTPConnectMode.PASV);
            m_ftpclient.setType(FTPTransferType.BINARY);
            try
            {
                m_ftpclient.chdir(ftpdir);//cd <ftproot>/IVMReporter
            }
            catch (Exception e)
            {
                m_ftpclient.mkdir(ftpdir);//md <ftproot>/IVMReporter
                m_ftpclient.chdir(ftpdir);//cd <ftproot>/IVMReporter
            }
            String pwd = m_ftpclient.pwd();
            logger.info("ftp present working dir:" + pwd);
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

    public boolean upload(String _sourceFilenameWithCompletePath)
    {
        logger.info("entered with sourcefile: "
                + _sourceFilenameWithCompletePath);
        boolean ftp_success = false;
        int ftp_retries = 0;

        if (m_ftpconfig == null)
        {
            return false;
        }
        while (ftp_retries < m_ftpconfig.get_retries() && ftp_success == false)
        {
            try
            {
                ftp_success = loginToFTPSite();
                if (ftp_success)
                    logger.info("login successful");

                ftp_success = uploadFile(_sourceFilenameWithCompletePath);
                if (ftp_success)
                {

                    logger.info("transfer successful");
                }
            }
            catch (Exception exe)
            {
                logger.error("", exe);
                ftp_success = false;
                logger.info("Sleeping for "
                        + m_ftpconfig.get_waitperiod() + " milliseconds!");
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
        }//no problem if it doesnt quit.
    }

    /**
     * @param path
     * @return boolean indicating success/failure
     * @throws IOException
     * @throws FTPException
     */
    private boolean uploadFile(String path) throws IOException, FTPException
    {
        logger.info("entered.");

        String localpath = path;
        int index = localpath.lastIndexOf(File.separator);
        String remotefile = localpath.substring(index + 1);
        String remoteFileTmp = remotefile.substring(0, remotefile
                .lastIndexOf("."));
        remoteFileTmp = remoteFileTmp + ".tmp";

        File _xmlfile = new File(path);
        if (_xmlfile.exists())
        {
            try
            {
                //upload .xml file as .tmp
                m_ftpclient.put(localpath, remoteFileTmp);
            }
            catch (IOException ioe)
            {
                throw ioe;
            }
            catch (FTPException ftpe)
            {
                throw ftpe;
            }
            //now rename .tmp file to .xml
            try
            {
                m_ftpclient.rename(remoteFileTmp, remotefile);
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

    //for testing.
    public static void main(String[] args)
    {
    }
}