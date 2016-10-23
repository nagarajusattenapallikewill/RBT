package com.onmobile.apps.ringbacktones.daemons.genericftp;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.onmobile.apps.ringbacktones.daemons.FtpDownloader;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.FTPConfig;

/**
 * @author sridhar.sindiri
 *
 */
public class FTPCampaignManager extends Thread
{
	private static Logger logger = Logger.getLogger(FTPCampaignManager.class);
	private RBTDaemonManager mainDaemonThread = null;
	private RbtThreadPoolExecutor handlerExecutor = null;

	private static long sleepTime = 0L;
	private static List<FTPCampaign> ftpCampaignsList = new ArrayList<FTPCampaign>();

	/**
	 * @param rbtDaemonManager
	 */
	public FTPCampaignManager(RBTDaemonManager rbtDaemonManager)
	{
		try
		{
			setName("FTPCampaignManager");
			this.mainDaemonThread = rbtDaemonManager;
			init();
		}
		catch(Exception e)
		{
			logger.error("Issue in creating FTPCampaignManager", e);
		}
	}

	/**
	 * @return
	 */
	public static long getSleepTime() {
		return sleepTime;
	}

	/**
	 * @param sleepTime
	 */
	public static void setSleepTime(long sleepTime) {
		FTPCampaignManager.sleepTime = sleepTime;
	}

	/**
	 * 
	 */
	public void init()
	{
		handlerExecutor = new FTPCampaignExecutor.Builder().build();
		ftpCampaignsList = CampaignXmlParser.getCampaignsFromXml();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		while (mainDaemonThread != null && mainDaemonThread.isAlive()) 
		{
			try
			{
				for (FTPCampaign ftpCampaign : ftpCampaignsList)
				{
					downloadFilesFromFTP(ftpCampaign.getFtpConfig(), ftpCampaign.getOutputRequired(), ftpCampaign.getMoveToFolder());
					File[] files = orderFiles(ftpCampaign.getFtpConfig());
					if (files == null)
					{
						logger.info("No files found in the directory");
						continue;
					}

					logger.info("Total files to process : " + files.length);
					for (File file : files)
					{
						try
						{
							handlerExecutor.execute(new CampaignThread(ftpCampaign, file));
						}
						catch (RejectedExecutionException e)
						{
							logger.error(e.getMessage(), e);
							break;
						}
					}
				}

				while (handlerExecutor.getActiveCount() > 0)
				{
					try
					{
						logger.debug("Sleeping the main thread for 1 sec since executors are still processing");
						Thread.sleep(1000L);
					}
					catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
			finally
			{
				try
				{
					logger.info("Sleeping for " + sleepTime + " minutes ");
					Thread.sleep(sleepTime * 60 * 1000);
				}
				catch (InterruptedException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 
	 */
	public void downloadFilesFromFTP(FTPConfig ftpConfig, String outputRequired, String moveToFolder)
	{
		if (ftpConfig == null)
		{
			logger.warn("FTP config is null");
			return;
		}

		String server = ftpConfig.getIpaddress();
		int port = Integer.parseInt(ftpConfig.getPort());
		String user = ftpConfig.getUsername();
		String pwd = ftpConfig.getPassword();
        String ftpDir = ftpConfig.getPath();
        String localDir = ftpConfig.getLocalPath();
        String extension = ftpConfig.getFileExtension();
        
        FTPClient ftpClient = new FtpDownloader().ftpConnect(server, port, user, pwd, ftpDir);
        String[] fileNames;
		try {
			fileNames = ftpClient.dir();
			
			File file = new File(localDir);
			if (!file.exists()){
				file.mkdirs();
			}
			
			for (int j = 0; j < fileNames.length; j++)
			{
				String localPath = localDir + File.separator + fileNames[j];
				if (!fileNames[j].endsWith(extension))
					continue;

				ftpClient.get(localPath, fileNames[j]);

				if (outputRequired.equalsIgnoreCase("move"))
					ftpClient.rename(fileNames[j], moveToFolder + fileNames[j]);
				else
					ftpClient.delete(fileNames[j]);
			}
			logger.info("Total files downloaded from FTP : " + fileNames.length);
			
		} catch (IOException e) {
			logger.error("", e);
		} catch (FTPException e) {
			logger.error("", e);
		}
	}

	/**
	 * @return the sorted file names
	 */
	private File[] orderFiles(FTPConfig ftpConfig)
	{
		if (ftpConfig == null)
		{
			logger.warn("FTP config is null");
			return null;
		}

		final String extension = ftpConfig.getFileExtension();
		File dir = new File(ftpConfig.getLocalPath());
		File[] files = dir.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name) {
				if (name.endsWith(extension)) {
					return true;
				}
				return false;
			}
		});
		return files;
	}
}
