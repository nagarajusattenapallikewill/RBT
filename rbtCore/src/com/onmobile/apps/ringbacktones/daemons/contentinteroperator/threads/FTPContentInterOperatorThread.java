package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.FtpDownloader;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorUtility;

/**
 * @author sridhar.sindiri
 *
 */
public class FTPContentInterOperatorThread extends Thread
{
	private static Logger logger = Logger.getLogger(FTPContentInterOperatorThread.class);
	private static Set<String> filesInLocalDirSet = new HashSet<String>();
	private static String localDir = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmSS");

	static
	{
		localDir = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "MNP_LOCAL_DIRECTORY", null);
		File file = new File(localDir);
		if (!file.exists())
			file.mkdirs();
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		while (true)
		{
			// Download files from ftp location
			downloadFilesFromFTP();	

			Iterator<String> localFileNameIterator = filesInLocalDirSet.iterator(); 
			while(localFileNameIterator.hasNext())
			{
				File file = new File(localDir + File.separator + localFileNameIterator.next());
				if(file.exists())
					ContentInterOperatorUtility.processXmlFile(file, "FTP");
			}
			String sleepSecs = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "FTP_THREAD_SLEEP_INTERVAL_IN_SECS", "30");
			try
			{
				logger.info("Sleeping for " + sleepSecs + " seconds ");
				Thread.sleep(Integer.parseInt(sleepSecs) * 1000);
			}
			catch(Exception e){
				logger.error("", e);
			}
		}
	}

	/**
	 * 
	 */
	private void downloadFilesFromFTP()
	{
		String server = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "FTP_SERVER", null);
		int port = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "FTP_PORT", 0);
		String user = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "FTP_USERNAME", null);
		String pwd = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "FTP_PASSWORD", null);
		String ftpDir = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "FTP_DIRECTORY", null);

		FTPClient ftpClient = new FtpDownloader().ftpConnect(server, port, user, pwd, ftpDir);
		String[] fileNames;
		try 
		{
			fileNames = ftpClient.dir();
			refreshFileSetInLocalDir();

			for (int j = 0; j < fileNames.length; j++)
			{
				String localPath = localDir + File.separator + fileNames[j];
				if (!isFilePresentInLocalDir(fileNames[j]))
				{
					ftpClient.get(localPath, fileNames[j]);
					filesInLocalDirSet.add(fileNames[j]);
				}
			}
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
		catch (FTPException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 */
	public static void refreshFileSetInLocalDir()
	{
		File[] files = new File(localDir).listFiles(new FilenameFilter() 
		{
			public boolean accept(File dir, String name)
			{
				if(name.indexOf(".xml")== -1)
					return false;
				String xmlDateStr = name.substring(name.lastIndexOf("_") + 1, name.indexOf(".xml"));
				try
				{
					Date xmlDate = sdf.parse(xmlDateStr);
					int noOfDays = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "PROCESS_LAST_N_DAYS_RECORDS", 1);
					if ((new Date().getTime() - xmlDate.getTime()) >= (noOfDays * 86400000))
						return false;
				}
				catch (ParseException e)
				{
					logger.error(e.getMessage(), e);
				}
				return true;
			}
		});

		filesInLocalDirSet = new HashSet<String>(); 
		if (files != null)
		{
			for (File file : files)
			{
				String fileName = file.getName();
				if (fileName.endsWith(".done"))
					fileName = fileName.substring(0, fileName.length() - 5);
				if (fileName.endsWith(".tmp"))
					fileName = fileName.substring(0, fileName.length() - 4);

				filesInLocalDirSet.add(fileName);
			}
		}
	}

	/**
	 * @param fileName
	 * @return
	 */
	public static boolean isFilePresentInLocalDir(String fileName)
	{
		if (!filesInLocalDirSet.contains(fileName) && !filesInLocalDirSet.contains(fileName + ".done") && !filesInLocalDirSet.contains(fileName + ".tmp"))
			return false;
		else
			return true;
	}
}
