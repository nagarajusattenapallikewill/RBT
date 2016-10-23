package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;

public class UGCFileUploader extends Thread implements iRBTConstant {
	private static Logger logger = Logger.getLogger(UGCFileUploader.class);

	private Calendar calendar = Calendar.getInstance();
	private int sleepMins = 1;
	public boolean createdSuccessfully=false;

	private FTPClient ftpClient = null;
	private FileDownloaderFromFTP fileDownloaderFromFTP = null;
	private RBTDBManager dbManager = null;
	private ParametersCacheManager m_rbtParamCacheManager = null;
	

	public static void main(String[] args) {
		Tools.init("UGCFileUploader",  true);

		logger.info("inside main");

		UGCFileUploader instance = new UGCFileUploader();
		logger.info("going to start the thread");
		instance.start();
		logger.info("exiting main");
	}
	
	public  UGCFileUploader() {
		logger.info("inside FileUploader  constructor");
		m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		
		dbManager = RBTDBManager.getInstance();
		
		if(getParamAsString("DAEMON", "LOCAL_DIR_FOR_UGC", null)!=null){
			logger.info("RBT::localDirForUGC== " + getParamAsString("DAEMON", "LOCAL_DIR_FOR_UGC", null));
			createdSuccessfully=true;
			fileDownloaderFromFTP=FileDownloaderFromFTP.initialize();
			if(getParamAsBoolean("DAEMON", "UPLOAD_TO_FTP", "FALSE")){
				initializeFTPClient();
			}
		}else{
			if(getParamAsString("DAEMON", "LOCAL_DIR_FOR_UGC", null)==null){
				logger.info("localDirForUGC is null, exiting daemon");
			}
		}
		logger.info("exiting UGCFileUploader constructor");
	}
	public void run() {
		logger.info("inside start");
		if(createdSuccessfully){
			if(fileDownloaderFromFTP!=null){
				logger.info("going to start fileDownloaderFromFTP thread");
				fileDownloaderFromFTP.start();
			}
			while (true) {
				logger.info("going inside uploadWavFilesToPlayers");
				if(getParamAsBoolean("DAEMON", "UPLOAD_TO_FTP", "FALSE") && ftpClient!=null){
					uploadFilesToFTP();
				}
				logger.info("came out of uploadWavFilesToPlayers");
				sleep();
			}
		}

	}
	
	private void uploadFilesToFTP() {
		logger.info("inside uploadFilesToFTP");
		File localFile=new File(getParamAsString("DAEMON", "LOCAL_DIR_FOR_UGC", null));
		File[] files=localFile.getAbsoluteFile().listFiles();		
		
		logger.info("No of files in local Dir ="+ files.length);
		
		if (dbManager == null){
			logger.info("RBT DB MANAGER is null - got null connection.... exiting");
			return;
		}
		for(int count=0; count < files.length; count++){
			logger.info("current filename=="+files[count].getName());
			if(!files[count].isDirectory()){
				String fileName=files[count].getName();
				if(fileName.indexOf(".wav")!=-1 || fileName.indexOf(".WAV")!=-1 ||
						fileName.indexOf(".TXT")!=-1 || fileName.indexOf(".txt")!=-1){
					//check whether file to be copied on FTP or not
					//check whether file to be moved in back up dir
					String wavFileName = fileName.substring(fileName.lastIndexOf(File.separator)+1);
					logger.info("wavFileName/txtFileName = "+wavFileName);
					SubscriberStatus subStatus = getSubscriberStatus(wavFileName.substring(0,wavFileName.lastIndexOf(".")));
					File backUpFileDir = new File(getParamAsString("DAEMON", "LOCAL_BACK_UP_DIR", null));
					if (!backUpFileDir.exists()){
						backUpFileDir.mkdir();
					}
					File backUpFile = new File(getParamAsString("DAEMON", "LOCAL_BACK_UP_DIR", null)+File.separator+wavFileName);
					boolean transferDone = false;	
					boolean isPending = false;
					if (subStatus == null || dbManager.isSelectionDeactivated(subStatus)){
						if(((System.currentTimeMillis() - files[count].lastModified()) / (60 * 1000)) >= 60){
							transferDone=files[count].renameTo(backUpFile);
							logger.info("subStatus is null OR user is deactive, moving this file to backup directory");
						}else{
							isPending = true;
						}
					}else if (isActiveWavFile(subStatus)){
						logger.info("is active wav file, copying this file to third party FTP and moving to backup dir");
						//copy this file to FTP and then move to backup directory
						try {
							ftpClient.put(getParamAsString("DAEMON", "LOCAL_DIR_FOR_UGC", null)+File.separator+fileName, wavFileName);
						} catch (IOException e) {
							e.printStackTrace();
							logger.info("got IOException " +e.getMessage());
							logger.info("Again Initializing ftpClient");
							initializeFTPClient();
							continue;
						} catch (FTPException e) {
							e.printStackTrace();
							logger.info("got FTPException " +e.getMessage());
							continue;
						}
						transferDone=files[count].renameTo(backUpFile);
					}else if(!isPendingWavFile(subStatus)){
						//move this file to backup directory
						logger.info("is not pending wav file, moving this file to backup directory");
						transferDone=files[count].renameTo(backUpFile);
					}else{
						isPending = true;//no need to move this file, as it is in pending state
						logger.info("is pending wav file, not doing anything");
					}
					if (transferDone) {
						logger.info("transfer of file "+ files[count].getName()+ " from  "+ localFile.getAbsolutePath()+ " to "+ new File(getParamAsString("DAEMON", "LOCAL_BACK_UP_DIR", null)).getAbsolutePath()+ " SUCCESSFUL");
					}else if (!isPending){
						logger.info("transfer of file "+ files[count].getName()+ " from  "+ localFile.getAbsolutePath()+ " to "+ new File(getParamAsString("DAEMON", "LOCAL_BACK_UP_DIR", null)).getAbsolutePath()+ " FAILED ONCE... RETRYING AGAIN");
						transferDone=files[count].renameTo(backUpFile);
						if (transferDone){
							logger.info("transfer of file "+ files[count].getName()+ " from  "+ localFile.getAbsolutePath()+ " to "+ new File(getParamAsString("DAEMON", "LOCAL_BACK_UP_DIR", null)).getAbsolutePath()+ " SUCCESSFUL");
						}else{
							logger.info("transfer of file "+ files[count].getName()+ " from  "+ localFile.getAbsolutePath()+ " to "+ new File(getParamAsString("DAEMON", "LOCAL_BACK_UP_DIR", null)).getAbsolutePath()+ " FAILED TWICE... RETRYING AGAIN");
							try {
								Tools.copyFile(files[count].getAbsolutePath(), backUpFile.getAbsolutePath());
								transferDone = true;
							} catch (IOException e) {
								logger.info("copy of file "+ files[count].getName()+ " from  "+ localFile.getAbsolutePath()+ " to "+ new File(getParamAsString("DAEMON", "LOCAL_BACK_UP_DIR", null)).getAbsolutePath()+ " FAILED");
								
							}
							if (transferDone){
								boolean delStatus = files[count].delete();
								if (delStatus)
									logger.info("transfer of file "+ files[count].getName()+ " from  "+ localFile.getAbsolutePath()+ " to "+ new File(getParamAsString("DAEMON", "LOCAL_BACK_UP_DIR", null)).getAbsolutePath()+ " SUCCESSFUL");
								else
									logger.info("transfer of file "+ files[count].getName()+ " from  "+ localFile.getAbsolutePath()+ " to "+ new File(getParamAsString("DAEMON", "LOCAL_BACK_UP_DIR", null)).getAbsolutePath()+ " FAILED");
							}
						}							
					}
				}
			}else{
				logger.info("current filename=="+files[count].getName()+" is a directory");
			}
		}
		logger.info("exiting uploadFilesToFTP");
	}
	/**
	 * File Format : subscriberID-clipID-yyyyMMdd-HHmmss.wav
	 * @param wavFileName
	 * @param dbManager
	 * @return
	 */
	private SubscriberStatus getSubscriberStatus(String wavFileName) {
		logger.info("inside getSubscriberStatus");
		String subID = null;
		StringTokenizer st = new StringTokenizer(wavFileName, "-");
		while (st.hasMoreTokens()){
			subID = st.nextToken();
			break;
		}
		if (subID == null) return null;
		logger.info("subID = "+subID);
		SubscriberStatus subStatus = dbManager.getSelection(subID, wavFileName);
		logger.info("exiting getSubscriberStatus");
		return subStatus;
	}

	
	private boolean isPendingWavFile(SubscriberStatus subStatus) {
		logger.info("inside isPendingWavFile");
		if (subStatus == null) return false;
		String status = subStatus.selStatus();
		if (status != null ){
			if (status.equals(STATE_TO_BE_ACTIVATED) || status.equals(STATE_ACTIVATION_PENDING) || status.equals(STATE_BASE_ACTIVATION_PENDING) || status.equals(STATE_GRACE) || status.equals(STATE_ACTIVATION_ERROR)){
				return true;
			}
		}
		return false;
	}
	
	private boolean isActiveWavFile(SubscriberStatus subStatus) {
		logger.info("inside isActiveWavFile");
		if (subStatus == null) return false;
		if (subStatus.selStatus() != null && subStatus.selStatus().equals(STATE_ACTIVATED)) return true;
		return false;
	}
	
	public  void initializeFTPClient(){
		String ftpIpForUpload = null;
		String strFtpPortForUpload = null;

		ftpIpForUpload = getParamAsString("DAEMON", "FTP_IP_FOR_UPLOAD", null); 
		strFtpPortForUpload = getParamAsString("DAEMON", "FTP_PORT_FOR_UPLOAD", null); 
		
		int tempFtpPortForUpload=21;
		if(strFtpPortForUpload!=null && strFtpPortForUpload.length()>0){
			try {
				tempFtpPortForUpload=Integer.parseInt(strFtpPortForUpload);
			} catch (NumberFormatException e) {
				tempFtpPortForUpload=21;
				// TODO Auto-generated catch block

				e.printStackTrace();
			}
		}
		String ftpUsernameForUpload = null;
		String ftpPasswdForUpload = null;
		String ftpDirForUpload = null;
		
		ftpUsernameForUpload = getParamAsString("DAEMON", "FTP_USERNAME_FOR_UPLOAD", null) ; 
		ftpPasswdForUpload = getParamAsString("DAEMON", "FTP_PASSWORD_FOR_UPLOAD", null); 
		ftpDirForUpload = getParamAsString("DAEMON", "FTP_DIR_FOR_UPLOAD", null); 
		
		logger.info("ftpIP-UP = "+ftpIpForUpload);
		logger.info("ftpPort-UP = "+strFtpPortForUpload);
		logger.info("ftpUsername-UP = "+ftpUsernameForUpload);
		logger.info("ftpPasswd-UP = "+ftpPasswdForUpload);
		logger.info("ftpDir-UP = "+ftpDirForUpload);
		
		if(ftpIpForUpload!=null && ftpUsernameForUpload!=null ){
			ftpClient=new FtpDownloader().ftpConnect(ftpIpForUpload, tempFtpPortForUpload, 
					ftpUsernameForUpload, ftpPasswdForUpload,ftpDirForUpload);

		}
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
	
	private String getParamAsString(String type, String param, String defualtVal)
	{
		try{
			return m_rbtParamCacheManager.getParameter(type, param, defualtVal).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defualtVal;
		}
	}
	
	private boolean getParamAsBoolean(String type, String param, String defaultVal)
	{
		try{
			return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
	
	private int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = m_rbtParamCacheManager.getParameter(type, param, defaultVal+"").getValue();
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal;
		}
	}
}

