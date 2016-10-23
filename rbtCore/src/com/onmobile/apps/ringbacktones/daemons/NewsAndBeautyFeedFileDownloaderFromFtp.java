package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class NewsAndBeautyFeedFileDownloaderFromFtp extends Thread implements iRBTConstant {
	
	private static Logger logger = Logger.getLogger(NewsAndBeautyFeedFileDownloaderFromFtp.class);
	
	private Calendar calendar = Calendar.getInstance();
	private int sleepMins = 5;
	public String m_dateFormat = null;
	public FTPClient ftpClient = null;
	public String localFilesDir = null;
	private static  int m_nConn = 4;
	

	public NewsAndBeautyFeedFileDownloaderFromFtp(){

	}
	public void run() {
		logger.info("inside start");
		RBTDBManager dbManager = RBTDBManager.getInstance();
		System.out.println("localFilesDir = "+localFilesDir);
		if (ftpClient != null && localFilesDir != null ){
			System.out.println("came here!!");
			while(true){
				try {
					//Moves files from third party FTP to local folder.
					System.out.println("downloading files from FTP");
					downloadFilesFromFTP(dbManager);
				} catch (IOException e) {
					e.printStackTrace();
					logger.info("got IOException " +e.getMessage());
					logger.info("Again Initializing ftpClient");
					NewsAndBeautyFeedFileDownloaderFromFtp.initialize();
				} catch (FTPException e) {
					e.printStackTrace();
					logger.info("got FTPException " +e.getMessage());
				}
				sleep();
			}
		}
	}
	private void downloadFilesFromFTP(RBTDBManager dbManager) throws IOException, FTPException {
		String method="downloadFilesFromFTP";
		logger.info("inside "+method);
		String[] fileList;
		fileList = ftpClient.dir();
		if (fileList == null) return;
		File file = new File(localFilesDir);
		File[] files = null;
		if(file.exists())
			files = file.listFiles();
		HashMap dateFileMapFromFtp = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat(m_dateFormat);
		HashMap dateFileMapFromLocalDir = new HashMap();
		String ftpPath = fileList[0].substring(0,fileList[0].lastIndexOf(File.separator)+1);
		for(int i=0;i<fileList.length;i++)
		{
			if(fileList[i].indexOf(".wav")==-1 && fileList[i].indexOf(".WAV")==-1 ) continue;
			String wavFileName = fileList[i].substring(fileList[i].lastIndexOf(File.separator)+1);
			System.out.println("wav file name at ftp = "+wavFileName);
			
			StringTokenizer stk = new StringTokenizer(wavFileName,"-");
			String wavFileFromFtp = null;
			String wavFileDateFromFtp = null;
			if(stk.hasMoreTokens())
				wavFileFromFtp = stk.nextToken();
			if(stk.hasMoreTokens())
			{
				String next = stk.nextToken();
				wavFileDateFromFtp = next.substring(0,next.length()-4);
			}
			if(dateFileMapFromFtp.containsKey(wavFileFromFtp))
			{
				Date prevDate =  (Date) dateFileMapFromFtp.get(wavFileFromFtp);
				Date currentFileDate = null;
				try {
					currentFileDate = sdf.parse(wavFileDateFromFtp);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if(prevDate.before(currentFileDate))
				{
					dateFileMapFromFtp.put(wavFileFromFtp,currentFileDate);
				}
			}
			else
			{
				Date date = null;
				try {
					date = sdf.parse(wavFileDateFromFtp);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				dateFileMapFromFtp.put(wavFileFromFtp,date);
			}
		}
		if(files != null)
		{
			for(int j=0;j<files.length;j++)
			{
				String fileName = files[j].getName();
				if(fileName.indexOf(".wav")==-1 && fileName.indexOf(".WAV")==-1 ) continue;
				System.out.println("wav file name at local = "+fileName);
				String wavFileFromLocal = null;
				String wavFileDateFromLocal = null;
				StringTokenizer stk = new StringTokenizer(fileName,"-");
				if(stk.hasMoreTokens())
					wavFileFromLocal = stk.nextToken();
				if(stk.hasMoreTokens())
				{
					String next = stk.nextToken();
					wavFileDateFromLocal = next.substring(0,next.length()-4);
				}
			
				if(dateFileMapFromLocalDir.containsKey(wavFileFromLocal))
				{
					Date prevDate = (Date) dateFileMapFromLocalDir.get(wavFileFromLocal);
					Date currentFileDate = null;
					try {
						currentFileDate = sdf.parse(wavFileDateFromLocal);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if(prevDate.before(currentFileDate))
					{
						dateFileMapFromLocalDir.put(wavFileFromLocal,currentFileDate);
					}
				}
				else
				{
					Date date = null;
					try {
						date = sdf.parse(wavFileDateFromLocal);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					dateFileMapFromLocalDir.put(wavFileFromLocal,date);
				}
			}
		}
		
		Set<String> wavFiles = dateFileMapFromFtp.keySet();
		
		for(Iterator<String> iterator = wavFiles.iterator();iterator.hasNext();)
		{
			String wavFile = iterator.next();
			Date dateAtFtp = (Date) dateFileMapFromFtp.get(wavFile);
			Date dateAtLocal = (Date) dateFileMapFromLocalDir.get(wavFile);
			
			if(dateAtLocal == null || dateAtLocal.before(dateAtFtp))
			{
				try
				{
					String dateString = sdf.format(dateAtFtp);
					String wavFileNameToDownload = wavFile + "-" + dateString + ".wav";
					String localPath = localFilesDir + File.separator + wavFileNameToDownload;
					String ftpPathwavFile = ftpPath + wavFileNameToDownload;
					if(!file.exists()){
						file.mkdir();
					}
					ftpClient.get(localPath,ftpPathwavFile);
				}
				catch(IOException e)
				{
					e.printStackTrace();
					logger.info("got IOException " +e.getMessage());
					continue;
				}
				catch(FTPException e)
				{
					e.printStackTrace();
					logger.info("got FTPException " +e.getMessage());
					continue;
				}
			}
		}
	}
	
	public static NewsAndBeautyFeedFileDownloaderFromFtp initialize(){
		
		RBTDBManager dbManager = RBTDBManager.getInstance();
		
		NewsAndBeautyFeedFileDownloaderFromFtp fileDownloaderFromFTP = null;
		
		String ftpIPForDownload = null;
		int ftpPortForDownload = 21;
		String ftpUsernameForDownload = null;
		String ftpPasswdForDownload = null;
		String ftpDirForDownload = null;
		String localFilesDirForDownload = null;
		String dateFormatForFeed = "yyyyMMdd_HHmmss";
		
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_FTP_IP_FOR_DOWNLOAD");
		if(param != null && param.getValue() != null)
		{
			ftpIPForDownload = param.getValue().trim();
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_FTP_PORT_FOR_DOWNLOAD");
		if(param != null && param.getValue() != null)
		{
			try
			{
				ftpPortForDownload = Integer.parseInt(param.getValue().trim());
			}
			catch(Exception e)
			{
				ftpPortForDownload = 21;
				e.printStackTrace();
			}
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_FTP_USERNAME_FOR_DOWNLOAD");
		if(param != null && param.getValue() != null)
		{
			ftpUsernameForDownload = param.getValue().trim();
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_FTP_PASSWORD_FOR_DOWNLOAD");
		if(param != null && param.getValue() != null)
		{
			ftpPasswdForDownload = param.getValue().trim();
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_FTP_DIR_FOR_DOWNLOAD");
		if(param != null && param.getValue() != null)
		{
			ftpDirForDownload = param.getValue().trim();
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_LOCAL_FILES_DIR_FOR_DOWNLOAD");
		if(param != null && param.getValue() != null)
		{
			localFilesDirForDownload = param.getValue().trim();
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_DATE_FORMAT");
		if(param != null && param.getValue() != null)
		{
			dateFormatForFeed = param.getValue().trim();
		}
		
		logger.info("ftpIP-D = "+ftpIPForDownload);
		logger.info("ftpPort-D = "+ftpPortForDownload);
		logger.info("ftpUsername-D = "+ftpUsernameForDownload);
		logger.info("ftpPasswd-D = "+ftpPasswdForDownload);
		logger.info("ftpDir-D = "+ftpDirForDownload);
		logger.info("localFilesDir-D = "+localFilesDirForDownload);
		
		if(ftpIPForDownload!=null && ftpUsernameForDownload!=null ){
			fileDownloaderFromFTP=new NewsAndBeautyFeedFileDownloaderFromFtp();
			fileDownloaderFromFTP.ftpClient = new FtpDownloader().ftpConnect(ftpIPForDownload, ftpPortForDownload, 
				ftpUsernameForDownload, ftpPasswdForDownload, ftpDirForDownload);
			fileDownloaderFromFTP.localFilesDir = localFilesDirForDownload;
			fileDownloaderFromFTP.m_dateFormat = dateFormatForFeed;
			if(fileDownloaderFromFTP.ftpClient==null){
				logger.info("ftp client for download is NULL");
				return null;
			}
		}
		return fileDownloaderFromFTP;
	}
	
	private void sleep() {
		long nexttime = getnexttime(sleepMins);
		calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date(nexttime));
		logger.info("RBT::Sleeping till " + calendar.getTime()
				+ " for next processing !!!!!");
		long diff = (calendar.getTime().getTime() - Calendar.getInstance().getTime().getTime());
		try {
			if (diff > 0)
				Thread.sleep(diff);
			else
				Thread.sleep(sleepMins * 60 * 1000);
		}
		catch (InterruptedException e) {
			logger.error("", e);
		}
	}
	
	public long getnexttime(int sleep) {
		Calendar now = Calendar.getInstance();
		now.setTime(new java.util.Date(System.currentTimeMillis()));
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);

		long nexttime = now.getTime().getTime();
		while (nexttime < System.currentTimeMillis()) {
			nexttime = nexttime + (sleep * 60 * 1000);
		}

		logger.info("RBT::getnexttime" + new Date(nexttime));
		return nexttime;
	}
	
	public static void main(String[] args) {
		Tools.init("FileDownloaderFromFTP", true);

		logger.info("inside main");

		NewsAndBeautyFeedFileDownloaderFromFtp instance = new NewsAndBeautyFeedFileDownloaderFromFtp();
		instance  = instance.initialize();
		logger.info("going to start the thread");
		instance.start();
		logger.info("exiting main");
	}
}
