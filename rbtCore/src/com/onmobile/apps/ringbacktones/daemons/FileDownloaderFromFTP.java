package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;

public class FileDownloaderFromFTP extends Thread implements iRBTConstant 
{
	private static Logger logger = Logger.getLogger(FileDownloaderFromFTP.class);
	
	private Calendar calendar = Calendar.getInstance();
	private int sleepMins = 5;
	private static String localPrefix = "";
	private static String localCircleId = "";
	public String localFilesDir = null;
	private static  int m_nConn = 4;

	public FileDownloaderFromFTP(){

	}
	public void run() 
	{
		logger.info("inside start");
		RBTDBManager dbManager = RBTDBManager.getInstance();

		while(true)
		{
			try {
				//Moves files from third party FTP to local folder.
				System.out.println("downloading files from FTP");
				downloadFilesFromFTP(dbManager);
			} catch (IOException e) {
				e.printStackTrace();
				logger.info("got IOException " +e.getMessage());
				logger.info("Again Initializing ftpClient");
			} catch (FTPException e) {
				e.printStackTrace();
				logger.info("got FTPException " +e.getMessage());
			}
			sleep();
		}
	}
	private void downloadFilesFromFTP(RBTDBManager dbManager) throws IOException, FTPException
	{
		String method="downloadFilesFromFTP";
		logger.info("inside "+method);
		String[] fileList;
		FTPClient ftpClient = null;
		try {
			ftpClient = getFTPClient(dbManager);
			fileList = ftpClient.dir();
			if (fileList == null) return;
			System.out.println("no of files in ftp = "+fileList.length);
			for (int i=0; i<fileList.length; i++){
				if(fileList[i].indexOf(".wav")==-1 && fileList[i].indexOf(".WAV")==-1 ) {
					//invalid file extension
					continue;
				}
				String subID = null;
				String wavFileName = fileList[i].substring(fileList[i].lastIndexOf(File.separator)+1);
				System.out.println("wav file name at ftp = "+wavFileName);
				
				StringTokenizer st = new StringTokenizer(wavFileName, "-");
				while (st.hasMoreTokens()){
					subID = st.nextToken();
					break;
				}
				if (subID == null) continue;
				subID = dbManager.subID(subID);
			
				Subscriber sub = dbManager.getSubscriber(subID);
				
			//	String pref = subID.substring(0, 4);
			//	logger.info("pref = "+pref);
			//	if (localPrefix.contains(pref))
				
				if (sub != null && sub.circleID().equalsIgnoreCase(localCircleId))
				{
					logger.info("circle contains this prefix");
					try {
						String localPath = localFilesDir+File.separator+wavFileName;
						File file = new File(localFilesDir);
						if (!file.exists()){
							file.mkdir();
						}
						ftpClient.get(localPath, fileList[i]);
						ftpClient.delete(fileList[i]);
						//copyFileToPlayer(localPath, subID);
					} catch (IOException e) {
						e.printStackTrace();
						logger.info("got IOException " +e.getMessage());
						continue;
					} catch (FTPException e) {
						e.printStackTrace();
						logger.info("got FTPException " +e.getMessage());
						continue;
					}
				}
			}
		} finally {
			if(null != ftpClient) {
				//close the ftp connection
				ftpClient.quit();
			}
		}
	}
	
	private FTPClient getFTPClient(RBTDBManager dbManager) 
	{
		String ftpIpForDownload = null;
		String ftpPortForDownload = null;
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.DAEMON, "FTP_IP_FOR_DOWNLOAD");
		if (parameter != null)
			ftpIpForDownload = parameter.getValue(); 
		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.DAEMON, "FTP_PORT_FOR_DOWNLOAD");
		if (parameter != null)
			ftpPortForDownload = parameter.getValue(); 
		
		int tempFtpPortForDownload=21;
		if(ftpPortForDownload!=null && ftpPortForDownload.length()>0){
			try {
				tempFtpPortForDownload=Integer.parseInt(ftpPortForDownload);
			} catch (NumberFormatException e) {
				tempFtpPortForDownload=21;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String ftpUsernameForDownload = null;
		String ftpPasswdForDownload = null;
		String ftpDirForDownload = null;
		String localFilesDirForDownload = null;
		
		CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.DAEMON, "FTP_USERNAME_FOR_DOWNLOAD");
		if (parameter != null)
			ftpUsernameForDownload = parameter.getValue(); 
		
		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.DAEMON, "FTP_PASSWORD_FOR_DOWNLOAD");
		if (parameter != null)
			ftpPasswdForDownload = parameter.getValue(); 
		
		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.DAEMON, "FTP_DIR_FOR_DOWNLOAD");
		if (parameter != null)
			ftpDirForDownload = parameter.getValue(); 
		
		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.DAEMON, "LOCAL_FILES_DIR_FOR_DOWNLOAD");
		if (parameter != null)
			localFilesDirForDownload = parameter.getValue(); 
		
		logger.info("ftpIP-D = "+ftpIpForDownload);
		logger.info("ftpPort-D = "+ftpPortForDownload);
		logger.info("ftpUsername-D = "+ftpUsernameForDownload);
		logger.info("ftpPasswd-D = "+ftpPasswdForDownload);
		logger.info("ftpDir-D = "+ftpDirForDownload);
		logger.info("localFilesDir-D = "+localFilesDirForDownload);
		
		if(ftpIpForDownload!=null && ftpUsernameForDownload!=null ){
//			fileDownloaderFromFTP=new FileDownloaderFromFTP();
			FTPClient ftpClient = new FtpDownloader().ftpConnect(ftpIpForDownload, tempFtpPortForDownload, 
				ftpUsernameForDownload, ftpPasswdForDownload,ftpDirForDownload);
			if(ftpClient==null) {
				logger.info("ftp client for download is NULL");
			}
			return ftpClient;
		}
		return null;
	}
	
//	private void copyFileToPlayer(String localPath, String subID) {
//		String method="moveFileToPlayer";
//		logger.info("inside moveFileToPlayer");
//		
//		String playerURL=null;
//
//		String fileName=localPath;
//		if(fileName.indexOf(".wav")!=-1 || fileName.indexOf(".WAV")!=-1){
//			logger.info("current filename=="+fileName+" contains .wav/.WAV");
//			subID=RBTDBManager.init(m_dbURL,m_nConn).subID(subID);
//			String circleId=RBTDBManager.init(m_dbURL,m_nConn).getCircleId(subID);
//			if(circleId!=null){
//				playerURL=(String)circleIdToPlayerUrlMap.get(circleId.trim());
//				logger.info("subscriber=="+subID+" wavfileName=="+fileName+" playerURL=="+playerURL+" circleID=="+circleId);
//				HttpParameters httpParam = Tools.getHttpParamsForURL(playerURL+"|6000|15000", null);
//				httpParam.setParamsAsParts(true);
//				playerURL = httpParam.getUrl();
//				File[] arrfile = new File[1];
//				arrfile[0] = new File(fileName);
//				HashMap params = new HashMap();
//				params.put(FEED, UGCFILE);
//				String fileResponse =null;
//				try {
//					fileResponse=RBTHTTPProcessing.postFile(httpParam, params, arrfile);
//				} catch (HttpException e) {
//					e.printStackTrace();
//					logger.info("got HttpException " +e.getMessage());
//				} catch (IOException e) {
//					e.printStackTrace();
//					logger.info("got IOException " +e.getMessage());
//				} catch (RBTException e) {
//					e.printStackTrace();
//					logger.info("got RBTException " +e.getMessage());
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					logger.info("got Exception " +e.getMessage());
//				}
//				if(fileResponse!=null){
//					fileResponse = fileResponse.trim();
//				}
//				logger.info(Response -> "
//						+ fileResponse);
//				if(fileResponse!=null && fileResponse.indexOf("SUCCESS")!=-1 ){
//					logger.info("file " +fileName+" sent successfully to tone player.");
////					logger.info("going to delete file " +fileName+"from local dir");
////					boolean fileDeleted = arrfile[0].delete(); 
////					if (fileDeleted){
////						logger.info("Deleted the file " +fileName+"from local dir");
////					}else{
////						logger.info("Error in deleting file " +fileName+"from local dir");
////					}
//				}else{
//					logger.info("moving file " +fileName+" to tone player FAILED");
//				}
//			}
//		}
//		// TODO Auto-generated method stub
//	}
	

	public static FileDownloaderFromFTP initialize()
	{
		List<SitePrefix> prefixes = CacheManagerUtil.getSitePrefixCacheManager().getLocalSitePrefixes();

//		if(prefixes!=null)
//		{
//			for (int i = 0; i<prefixes.length;i++)
//			{
//				FileDownloaderFromFTP.localPrefix += prefixes[i].prefix().trim()+", "; 
//			}
//		}
		
		if (prefixes != null)
			localCircleId = prefixes.get(0).getCircleID(); // Check this
		
		logger.info("local Prefixes = "+FileDownloaderFromFTP.localPrefix);
		FileDownloaderFromFTP fileDownloaderFromFTP = new FileDownloaderFromFTP();
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

		FileDownloaderFromFTP instance = FileDownloaderFromFTP.initialize();
		logger.info("going to start the thread");
		instance.start();
		logger.info("exiting main");
	}
}
