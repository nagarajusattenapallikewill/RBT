package com.onmobile.apps.ringbacktones.daemons.interoperator.threads;

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
import com.onmobile.apps.ringbacktones.daemons.FtpDownloader;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;

/**
 * @author sridhar.sindiri
 *
 */
public class FTPInterOperatorCopyDaemon  extends Thread
{
	private static Logger logger = Logger.getLogger(FTPInterOperatorCopyDaemon.class);
	private static Set<String> filesInLocalDirSet = new HashSet<String>();
	public static String localDir = null;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmSS");
		
	static
	{
		localDir = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "LOCAL_DIRECTORY").getValue();
		File file = new File(InterOperatorUtility.localDir);
		if (!file.exists())
			file.mkdirs();
		
	}
	
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
					InterOperatorUtility.processXmlFile(file, "FTP");
			}
			String sleepSecs = CacheManagerUtil.getParametersCacheManager().getParameterValue("RDC", "FTP_THREAD_SLEEP_INTERVAL_IN_SECS", "30");
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
		String server = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "FTP_SERVER").getValue();
		int port = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "FTP_PORT").getValue());
		String user = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "FTP_USERNAME").getValue();
		String pwd = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "FTP_PASSWORD").getValue();
		String ftpDir = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "FTP_DIRECTORY").getValue();
		
		FTPClient ftpClient = new FtpDownloader().ftpConnect(server, port, user, pwd, ftpDir);
		String[] fileNames;
		try 
		{
			fileNames = ftpClient.dir();
			refreshFileSetInLocalDir();
			
			for (int j = 0; j < fileNames.length; j++)
			{
				String localPath = localDir + File.separator + fileNames[j];
				logger.info("localPath="+localPath+", fileNames[j]="+fileNames[j]+", isFilePresentInLocalDir(fileNames[j])="+isFilePresentInLocalDir(fileNames[j]));
				if (!isFilePresentInLocalDir(fileNames[j]) && fileNames[j].toLowerCase().startsWith("rbt_mnp"))
				{
					ftpClient.get(localPath, fileNames[j]);
					filesInLocalDirSet.add(fileNames[j]);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (FTPException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	private File[] getLocalFilesToProcess() 
	{
		File dir = new File(localDir);
		File[] files = dir.listFiles(new FilenameFilter() 
		{
			public boolean accept(File dir, String name) {
				if(name.indexOf(".xml") == -1)
					return false;
				if(name.indexOf(".done") == -1)
					return false;
				
				String xmlDateStr = name.substring(name.lastIndexOf("_") + 1, name.indexOf(".xml"));
				try {
					Date xmlDate = sdf.parse(xmlDateStr);
					int noOfDays = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameterValue("RDC", "PROCESS_LAST_N_DAYS_RECORDS", "1"));
					if ((new Date().getTime() - xmlDate.getTime()) >= (noOfDays * 86400000))
						return false;
				}
				catch (ParseException e) {
					logger.error("", e);
				}
				
				if (!name.endsWith(".xml")) {
					return false;
				}
				return true;
			}
		});

		if (files != null)
			logger.info("Total files to process in the Local Directory : " + files.length);

		return files;
	}
	public static void refreshFileSetInLocalDir()
	{
		File[] files = new File(localDir).listFiles(new FilenameFilter() 
		{
			public boolean accept(File dir, String name) {
				if(name.indexOf(".xml")== -1)
					return false;
				String xmlDateStr = name.substring(name.lastIndexOf("_") + 1, name.indexOf(".xml"));
				try {
					Date xmlDate = sdf.parse(xmlDateStr);
					int noOfHours = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameterValue("RDC", "PROCESS_LAST_N_HOURS_RECORDS", "4"));
					if ((new Date().getTime() - xmlDate.getTime()) >= (noOfHours * 3600000))
						return false;
				}
				catch (ParseException e) {
					logger.error("", e);
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
				if(fileName.endsWith(".done"))
					fileName = fileName.substring(0,fileName.length()-5);
				if(fileName.endsWith(".tmp"))
					fileName = fileName.substring(0,fileName.length()-4);
				filesInLocalDirSet.add(fileName);
				//logger.info("File.getname()="+file.getName()+", fileName="+fileName);
			}
		}
		logger.info("No of files Found="+filesInLocalDirSet.size());
		File[] allFiles = new File(localDir).listFiles();
		if(allFiles == null || allFiles.length == 0)
			return;
		for (File file : allFiles)
		{
			try
			{
				String fileName = file.getName();
				if(fileName.endsWith(".done"))
					fileName = fileName.substring(0,fileName.length()-5);
				if(fileName.endsWith(".tmp"))
					fileName = fileName.substring(0,fileName.length()-4);
				if(!filesInLocalDirSet.contains(fileName))
					file.delete();
			}
			catch (Exception e)
			{
				logger.error("Exception", e);
			}
		}
	}
	
	public static boolean isFilePresentInLocalDir(String fileName)
	{
		if (!filesInLocalDirSet.contains(fileName) && !filesInLocalDirSet.contains(fileName + ".done") && !filesInLocalDirSet.contains(fileName + ".tmp"))
			return false;
		else
			return true;
	}
		
}
