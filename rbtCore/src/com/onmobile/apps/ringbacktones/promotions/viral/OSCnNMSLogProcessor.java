package com.onmobile.apps.ringbacktones.promotions.viral;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.onmobile.apps.ringbacktones.daemons.FtpDownloader;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.promotions.RBTViralMain;
import com.onmobile.apps.ringbacktones.promotions.WorkerThread;

/**
 * @author sridhar.sindiri
 *
 */
public class OSCnNMSLogProcessor 
{
	List<Thread> threads = new ArrayList<Thread>();
	
	private static Logger logger = Logger.getLogger(OSCnNMSLogProcessor.class);
	
	private static Hashtable<String, Long> nmsRecords = new Hashtable<String, Long>();
	private static Vector<String>oscRecords = new Vector<String>();
	private static int maxWorker = 50;
	
	    
	int i = 0;
	static int fileCount = 1;
	
	public static void main(String args[])
	{
		new OSCnNMSLogProcessor().startProcessing();
	}
	
	public void startProcessing() {
		//download files from ftp location
		try
		{
			Parameters parameter = RBTViralMain.m_rbtParamCacheManager.getParameter("VIRAL", "MAX_WORKER_THREAD");
			if(parameter != null && parameter.getValue() != null)
				maxWorker = Integer.parseInt(parameter.getValue().trim());
		}
		catch(Exception e)
		{
			maxWorker = 50;
			logger.error("", e);
		}
		logger.info("Max Worker Threads="+maxWorker);	
		downloadFilesFromFTP();			
		//sort the files according to date in the file name
		File[] files = orderFiles();	
		
		if (files == null)
		{
			logger.info("No files found in the directory");
			return;
		}

		logger.info("Total files to process : " + files.length);
		for(File file : files) {
			processLogFile(file);
		}
		
		int workerCount = 5;
		if ((oscRecords.size() / 100) > 5)
			workerCount = oscRecords.size()/100;
		workerCount = workerCount > maxWorker ? maxWorker : workerCount;
		logger.info("No. of workers created = "+workerCount);
		
		for (int i = 0; i < workerCount; i++)
		{
			WorkerThread worker = new WorkerThread(oscRecords, nmsRecords);
			worker.setName("WORKERTHREAD" + (i + 1));
			threads.add(worker);
			worker.start();
		}
		
		//join on the thread till complete
		for(int i=0; i<threads.size(); i++) {
			try {
				threads.get(i).join();
			} catch(Throwable t) {
				logger.error("", t);
			}
		}
		threads.clear();
	}

	public void downloadFilesFromFTP(){
		String server = RBTViralMain.m_rbtParamCacheManager.getParameter("VIRAL", "FTP_SERVER").getValue();
		int port = Integer.parseInt(RBTViralMain.m_rbtParamCacheManager.getParameter("VIRAL", "FTP_PORT").getValue());
		String user = RBTViralMain.m_rbtParamCacheManager.getParameter("VIRAL", "FTP_USERNAME").getValue();
		String pwd = RBTViralMain.m_rbtParamCacheManager.getParameter("VIRAL", "FTP_PASSWORD").getValue();
        String ftpDir = RBTViralMain.m_rbtParamCacheManager.getParameter("VIRAL", "FTP_DIRECTORY").getValue();
        String localDir = RBTViralMain.m_rbtParamCacheManager.getParameter("VIRAL", "LOCAL_DIRECTORY").getValue();
        
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

				ftpClient.get(localPath, fileNames[j]);
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
	private File[] orderFiles() {
		String fileDir = RBTViralMain.m_rbtParamCacheManager.getParameter("VIRAL", "LOCAL_DIRECTORY").getValue();
		File dir = new File(fileDir);
		File[] files = dir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if(name.startsWith("OSC_NMS_") && name.endsWith(".txt")) {
					return true;
				}
				return false;
			}
		});
		return files;
	}
	
	private void processLogFile(File file) {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(file));
			String record;
			while ((record = br.readLine()) != null) {
				if (record.startsWith("OSC")) {
					oscRecords.add(record.substring(4));
				} else {
					String key = record.substring(4, record.lastIndexOf("|"));
					String value = record.substring(record.lastIndexOf("|") + 1);
					nmsRecords.put(key, Long.parseLong(value));
				}
			}
			br.close();
			file.delete();
		} catch (IOException e) {
			logger.error("", e);
		}
		catch (Exception e){
			logger.error("", e);
		}
	}
}
