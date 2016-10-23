package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class RBTRetailer extends Thread implements WebServiceConstants {

	private RBTClient rbtClient = null;
	private RBTDaemonManager m_mainDaemonThread = null; 
	private RBTDBManager m_rbtDBManager = null;
	private RBTCacheManager rbtCacheManager = null;
	private ParametersCacheManager m_rbtParamCacheManager = null;
	private static Logger logger = Logger.getLogger(RBTRetailer.class);
	private List<String> m_siteNameList = null;

	protected RBTRetailer(RBTDaemonManager mainDaemonThread) 
	{
		try
		{
			setName("RBTRetailer");
			m_mainDaemonThread = mainDaemonThread;
			init();
		}
		catch (Exception e)
		{
			logger.error("Issue in creating RBTRetailer", e);
		}
	}
	
	private void init() {
		rbtClient = RBTClient.getInstance();
		m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		
		m_rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
		List<SitePrefix> sitePrefixList = CacheManagerUtil.getSitePrefixCacheManager().getLocalSitePrefixes();
		m_siteNameList = new ArrayList<String>(sitePrefixList.size());
		for(SitePrefix sitePrefix : sitePrefixList) {
			m_siteNameList.add(sitePrefix.getSiteName().toUpperCase());			
		}

	}
	
	public void run() {
		while(m_mainDaemonThread != null && m_mainDaemonThread.isAlive()) 
		{
			processRetailerRequest();
			try
			{
				logger.info("RBTRetailer Thread Sleeping for 5 minutes............");
				Thread.sleep(getParamAsInt("SLEEP_INTERVAL_MINUTES",5) * 60 * 1000);
			}
			catch(Exception e)
			{
			}
		}
	}
	
	private void  processRetailerRequest(){
		
		String ftpInfo = getParamAsString("RETAILER_FTP_PATH_INFO");
		String localPath = getParamAsString("RETAILER_LOCAL_PATH");
		if(localPath == null) {
			logger.info("RETAILER_LOCAL_PATH is not configured, please check DB");
			return;
		}
		logger.debug("Local path: " + localPath);
		File inputFilePath = new File(localPath);
		if(!inputFilePath.exists()) {
			logger.info("FTP local directory is not exists, please check your local path, what you configured in DB");
			return;
		}
		FtpServerInfo ftpServerInfo = null;
		if(ftpInfo != null) {
			ftpServerInfo = new FtpServerInfo(ftpInfo);
		}
		if(ftpServerInfo != null) {
			logger.info(ftpServerInfo.toString());
			try {
				getFilesFromFTP(ftpServerInfo, localPath);
			}
			catch(Exception e) {
				logger.error("", e);
			}
		}
		
		File[] files = getInputFiles(inputFilePath,".txt");
		
		if(files == null || files.length == 0) {
			logger.error("No files are exist");
//			return;
		}
		
		if(files != null && files.length > 0) {
			for( File inputFile : files) {
				processFile(inputFile, inputFilePath);
			}
		}
		
		uploadFilesToFtp(ftpServerInfo, localPath);
	}
	
	private void getFilesFromFTP(FtpServerInfo ftpServerInfo, String localPath) throws Exception{
		FTPClient ftpClient = getFtpClient(ftpServerInfo, localPath);
		if(ftpClient == null) 
			return;
		try {
			FTPFile[] ftpFiles = ftpClient.listFiles();
			logger.debug("Ftp File list size: " + ftpFiles.length);
			for(int i=0;i<ftpFiles.length;i++){				
				if(!ftpFiles[i].isDirectory()){
					FileOutputStream fout = null;
					boolean isFileCopied = false;
					try{
						String fileName = ftpFiles[i].getName();
						String siteName = null;
						try {
							siteName = fileName.substring(fileName.indexOf("_") + 1, fileName.lastIndexOf("_"));
						}
						catch(Exception e ) { continue; }
						if(fileName.endsWith(".txt") && m_siteNameList.contains(siteName.toUpperCase())) {
							fout = new FileOutputStream(localPath + File.separator +  ftpFiles[i].getName());						
							ftpClient.retrieveFile(fileName, fout);
							fout.flush();
							isFileCopied = true;
						}
					}
					finally{
						if(fout != null){
							fout.close();
							fout = null;
						}
					}					
					if(isFileCopied) {
						logger.info("File downloaded: " + ftpFiles[i].getName());
						ftpClient.rename(ftpFiles[i].getName(), ftpFiles[i].getName() + ".copied");
//						ftpClient.dele(ftpServerInfo.getFtpPath() + ftpFiles[i].getName());
					}
				}			
			}
		}
		finally {
			closeFtpClient(ftpClient);
		}
	}
	
	private FTPClient getFtpClient(FtpServerInfo ftpServerInfo, String localPath){
		FTPClient ftpClient = null;
		try{
			ftpClient = new FTPClient();
			ftpClient.connect(ftpServerInfo.getFtpServer());
			ftpClient.login(ftpServerInfo.getFtpUserName(), ftpServerInfo.getFtpPassword());
			int reply = ftpClient.getReplyCode();
			if(FTPReply.isPositiveCompletion(reply)){
				logger.info(" FTP to File Server connection Success");
            }else {
            	logger.error("FTP to File Server connection Failed");
                throw new Exception("Could not connect to the FTP machine");
            }
			ftpClient.changeWorkingDirectory(ftpServerInfo.getFtpPath());  //ex //spider
		}
		catch(Exception e) {
			logger.error("",e);
		}
		return ftpClient;
	}
	
	private void uploadFilesToFtp(FtpServerInfo ftpServerInfo, String localPath){		
		File[] files = getInputFiles(new File(localPath),".output");
		if(files == null)
			return;
		FTPClient ftpClient = getFtpClient(ftpServerInfo, localPath);
		if(ftpClient == null) {
			return;
		}
		for(File file : files) {
			boolean isFileMovedToFTP = false;
			FileInputStream fin = null;
			try{
				fin = new FileInputStream(file);				
				ftpClient.storeFile(file.getName(), fin);
				logger.debug(file.getName() + " successfully moved into FTP");
				isFileMovedToFTP = true;
			}
			catch(Exception e) {
				logger.error("",e);
			}
			finally{
				try{
					if(fin != null)
						fin.close();
				}catch(Exception e) { }
			}			
			if(isFileMovedToFTP && file.delete()) {
				logger.debug(file.getName() + " get successfully deleted");;
			}
		}
		closeFtpClient(ftpClient);
	}
	
	private void closeFtpClient(FTPClient ftpClient) {
		try{
			if(ftpClient != null){
				ftpClient.disconnect();
			}
		}
		catch(Exception e) {}

	}
	
	private File[] getInputFiles(File inputFile, final String fileFormat) {		
		File[] files = inputFile.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if(name.endsWith(fileFormat)) {
					return true;
				}
				return false;
			}
		});
		if(null == files || files.length <= 0) {
			return null;
		}		
		Arrays.sort(files, new Comparator<File>() {
		    public int compare(File f1, File f2)
		    {
		    	String f1Name = f1.getName();
		    	String f2Name = f2.getName();
		    	return f1Name.compareTo(f2Name);
		    } 
		});
		return files;
	}
	
	private void processFile(File inputFile, File outputFilePath) {
		
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		PrintWriter printWriter = null;
		boolean isfileProcessed = false;
		try{
			fileReader = new FileReader(inputFile);
			bufferedReader = new BufferedReader(fileReader);
			String inputFileName = inputFile.getName();
			String outputFileName = inputFileName + ".output";
			printWriter = new PrintWriter(new FileWriter(new File(outputFilePath, outputFileName)));
			String line = null;
			String mode = getParamAsString(iRBTConstant.DAEMON, "MODE_FOR_RETAILER_DT", "RET");
			while((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				String[] values = line.split(",");
				if(values.length != 5) {
					String response = getParamAsString(iRBTConstant.DAEMON, "RETAILER_MISSING_PARAMETER", "PARAMETER_MISSING");
					line = line + "," + response;
					//write into output file
					printWriter.println(line);
					continue;
				}
				String subId = values[0].trim();
				String pack = values[1].trim();
				String promoId = values[2].trim();
				String chargeClass = values[3].trim();
				String retailerNo = values[4].trim();				
				
				boolean isNewUser = false;
				String response = null;
				
				Subscriber subscriber = m_rbtDBManager.getSubscriber(subId);
				
				if (m_rbtDBManager.isSubscriberDeactivated(subscriber)) {
					isNewUser = true;
				}

				if(!"".equals(promoId) && (chargeClass == null || "".equals(chargeClass))) {
					response = getParamAsString(iRBTConstant.DAEMON, "RETAILER_CHARGECLASS_MISSING", "CHARGECLASS_MISSING");
				}
				else if(promoId == null || "".equals(promoId)) {
					
					if("".equals(pack)) {
						response = getParamAsString(iRBTConstant.DAEMON, "RETAILER_MISSING_SUBPACK_TO_ACT", "PACK_MISSING");
					}
					else if(!isNewUser) {
						//Do the upgrade validity
						logger.info("User is an active user, will do validity extension. SubId: " + subId);
						response = upgradeSubscription(subId, mode, pack, retailerNo);
						if(response.equalsIgnoreCase(SUCCESS)) {
							sendSms(subId, retailerNo);
						}
					}
					else{
						//Do the process activation
						logger.debug("User is new user, will do base active. SubId: " + subId);
						SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subId);
						subscriptionRequest.setSubscriptionClass(pack);
						subscriptionRequest.setMode(mode);
						subscriptionRequest.setModeInfo("RET:" + retailerNo);
						rbtClient.activateSubscriber(subscriptionRequest);
						response = subscriptionRequest.getResponse();
					}
				}
				else{
					Clip clip = rbtCacheManager.getClipByPromoId(promoId);
					if(clip == null) {
						logger.info("Clipnot exist. SubId: " + subId);
						response = getParamAsString(iRBTConstant.DAEMON, "RETAILER_CLIP_NOT_EXIST", "CLIP_NOT_EXIST");
					}
					else if(!isNewUser && !"".equals(pack)) {
						//Do the upgrade validity
						logger.info("user is an active user and pack is available in retailer request, will do validity extension. SubId: " + subId);
						response = upgradeSubscription(subId, mode, pack, retailerNo);
						logger.debug("response of validity extension " + response + " subId: " + subId);
						if(response.equalsIgnoreCase(SUCCESS)) {
							sendSms(subId, retailerNo);
						}
					}
					
					//if user is new user, but pack has not given, then should return error
					if(isNewUser && "".equals(pack)) {
						response = getParamAsString(iRBTConstant.DAEMON, "RETAILER_MISSING_SUBPACK_TO_ACT", "PACK_MISSING");
						logger.info("user is new user and pack is not available in retailer request, will send error message. SubId: " + subId);
					}
					else if(clip != null && (response == null || response.equalsIgnoreCase(SUCCESS))) {
						//Do the selection
						logger.debug("Selection request. SubId: " + subId);
						SelectionRequest selectionRequest = new SelectionRequest(subId);						
						String categoryId = getParamAsString(iRBTConstant.DAEMON,"RETAILER_CATEGORY_ID","3");
						selectionRequest.setClipID(clip.getClipId() + "");
						selectionRequest.setCategoryID(categoryId);
						selectionRequest.setModeInfo("RET:" + retailerNo);
						if(chargeClass != null && (chargeClass = chargeClass.trim()).length() != 0) {
							selectionRequest.setChargeClass(chargeClass);
							selectionRequest.setUseUIChargeClass(true);
						}
						selectionRequest.setMode(mode);
						if(isNewUser) {
							selectionRequest.setSubscriptionClass(pack);
						}
						rbtClient.addSubscriberSelection(selectionRequest);
						response = (response != null? response + "_" : "") + selectionRequest.getResponse();
						logger.debug("subscriber selection response " + selectionRequest.getResponse() + " subId: " + subId);
					}
				}
				logger.info(". SubId: " + subId + " Response: " + response);
				line = line + "," + response;
				printWriter.println(line);
				printWriter.flush();
			}
			isfileProcessed = true;
		}		
		catch(Throwable t) {
			logger.error("",t);
		}
		finally{
			if(printWriter != null) {
				printWriter.flush();
				printWriter.close();
			}
			try{
				if(bufferedReader != null) {
					bufferedReader.close();
				}
				if(fileReader != null) {
					fileReader.close();
				}
			}
			catch(IOException e) { /*Ignore*/ }
		}

		if(!isfileProcessed) {
			createErrorFile(inputFile);
		}

		if(inputFile.delete()) {
			if(isfileProcessed) {
				logger.info(inputFile.getName() + " successfully processed and deleted");
			}
		}
	}

	private void createErrorFile(File inputFile) {
		BufferedReader bufferedReader = null;
		PrintWriter printWriter = null;
		String errorFileName = inputFile.getName().replace(".txt", ".error");
		File errorFile = new File(inputFile.getParent(), errorFileName);
		try{
			bufferedReader = new BufferedReader(new FileReader(inputFile));
			printWriter = new PrintWriter(errorFile);
			char[] cbuf = new char[1024];
			int pointer = -1;
			while((pointer = bufferedReader.read(cbuf)) != -1) {
				printWriter.write(cbuf, 0, pointer);
				printWriter.flush();
			}
			logger.debug("Error file created: " + errorFile.getName());
		}
		catch(Exception e) {
			logger.error("",e);
		}
		finally{
			try{
				if(bufferedReader != null) bufferedReader.close();
				if(printWriter != null) { 
					printWriter.flush();
					printWriter.close();
				}
			}
			catch(Exception e) { /* Ignore */}
		}

	}
	
	private String getParamAsString(String param)
	{
		try{
			return m_rbtParamCacheManager.getParameter(iRBTConstant.DAEMON, param, null).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return null;
		}
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
	
	private int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = m_rbtParamCacheManager.getParameter(iRBTConstant.DAEMON, param, defaultVal+"").getValue();
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}
	
	private String upgradeSubscription(String subId, String mode, String pack,String retailerNo ) {
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subId);
		subscriptionRequest.setSubscriptionClass(pack);
		subscriptionRequest.setMode(mode);
		subscriptionRequest.setModeInfo("RETAILER:" + retailerNo);
		subscriptionRequest.setInfo(UPGRADE_VALIDITY);
		rbtClient.updateSubscription(subscriptionRequest);
		return subscriptionRequest.getResponse();
	}
	
	public static void main(String args[]) {
//		RBTRetailer retailer = new RBTRetailer();
//		try{
//			retailer.main();
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//		}
		String fileName = "RETAILER_CIRCLENAME_260711000000.txt";
		String siteName = fileName.substring(fileName.indexOf("_")+1, fileName.lastIndexOf("_"));
		System.out.println(siteName);
	}
	
	private void main() throws Exception{
		FtpServerInfo serverInfo = new FtpServerInfo("10.9.11.16|onmobile|qwerty12#|/senthilraja/test/");
		String localpath = "D:/rbt_products/work/rbt/jiraid_rbt_3090/ftp/testftp";
		getFilesFromFTP(serverInfo, localpath);
		System.out.println("Copied");
//		uploadFilesToFtp(serverInfo, localpath);
//		File[] files = getInputFiles(new File(localpath), ".txt");
//		for(File file : files) {
//			logger.info(file.getName());
//		}
	}

	
	protected class FtpServerInfo {
		private String ftpServer = null;
		private String ftpUserName = null;
		private String ftpPassword = null;
		private String ftpPath = null;
		
		public FtpServerInfo(String ftpServerInfo) {
			String[] ftpInfoArr = ftpServerInfo.split("\\|");
			if(ftpInfoArr.length == 4) {
				ftpServer = ftpInfoArr[0];
				ftpUserName = ftpInfoArr[1];
				ftpPassword = ftpInfoArr[2];
				ftpPath = ftpInfoArr[3];
			}			
		}

		public String getFtpServer() {
			return ftpServer;
		}

		public void setFtpServer(String ftpServer) {
			this.ftpServer = ftpServer;
		}

		public String getFtpUserName() {
			return ftpUserName;
		}

		public void setFtpUserName(String ftpUserName) {
			this.ftpUserName = ftpUserName;
		}

		public String getFtpPassword() {
			return ftpPassword;
		}

		public void setFtpPassword(String ftpPassword) {
			this.ftpPassword = ftpPassword;
		}

		public String getFtpPath() {
			return ftpPath;
		}

		public void setFtpPath(String ftpPath) {
			this.ftpPath = ftpPath;
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder("Ftp Server info: ");
			builder.append("FtpServer: " );
			builder.append(ftpServer);
			builder.append("FtpUserName: " );
			builder.append(ftpUserName);
			builder.append("FtpPassword: " );
			builder.append(ftpPassword);
			builder.append("FtpPath: " );
			builder.append(ftpPath);
			return builder.toString();
		}
	}
	
	private void sendSms(String subscriberId, String retailerId) {
		if (getParamAsBoolean("SEND_SMS_TO_RETAILER","FALSE"))
		{
			String retailerMode = getParamAsString(iRBTConstant.DAEMON, "MODE_FOR_RETAILER_DT", "RET");
			String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText("RETAILER_VALIDITY_EXTN","SUCCESS","eng");
			smsText = smsText.replaceAll("%SUBID%", subscriberId);
			try{
				Tools.sendSMS(getParamAsString("SENDER_NO"), retailerId, smsText, getParamAsBoolean("SEND_SMS_MASS_PUSH","FALSE"), null);
			}
			catch(Exception e) {
				logger.error("",e);
			}
		}
	}
	
	private boolean getParamAsBoolean(String param, String defaultVal)
    {
    	return getParamAsBoolean("DAEMON", param, defaultVal);
    }

    private boolean getParamAsBoolean(String type, String param, String defaultVal)
    {
    	try{
    		return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
    	}catch(Exception e){
    		logger.warn("Unable to get param ->"+param +"  type ->"+type);
    		return defaultVal.equalsIgnoreCase("TRUE");
    	}
    }
}
