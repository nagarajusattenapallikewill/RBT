package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.FtpDownloader;
import com.onmobile.apps.ringbacktones.provisioning.common.CopyProcessorUtils;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;
import com.onmobile.apps.ringbacktones.wrappers.SubscriberRbtClientWrapper;

public class RBTFailedCopyThread extends Thread implements iRBTConstant{

	private static Logger logger = Logger.getLogger(RBTFailedCopyThread.class);
	private static final String _class = "RBTFailedCopyThread";

	private RbtGenericCacheWrapper rbtGenericCacheWrapper = null;
	private SubscriberRbtClientWrapper subRbtClient = null;
	private RBTGatherer m_parentGathererThread = null;
	private String ftpIp = null;
	private int ftpPort = 21;
	private String ftpUsername = null;
	private String ftpPasswd = null;
	private String ftpCircleDir = null;
	private String localDir = null;
	private int sleepTimeInMin = 5;
//	ArrayList<String> normalCopy= null;
//	ArrayList<String>  starCopy=null;
	
	public RBTFailedCopyThread(RBTGatherer m_gathererThread) {
		logger.info("initting...");
		m_parentGathererThread = m_gathererThread;
	}

	public boolean initialize() {
		rbtGenericCacheWrapper = RbtGenericCacheWrapper.getInstance();
		subRbtClient = SubscriberRbtClientWrapper.getInstance();
		ftpIp = getParamAsString("DAEMON", "FTP_IP_FOR_RETRY_COPY", null);
		ftpPort = getParamAsInt("DAEMON", "FTP_PORT_FOR_RETRY_COPY", 21);
		ftpUsername = getParamAsString("DAEMON", "FTP_USERNAME_FOR_RETRY_COPY",	null);
		ftpPasswd = getParamAsString("DAEMON", "FTP_PASSWORD_FOR_RETRY_COPY", null);
		ftpCircleDir = getParamAsString("DAEMON", "FTP_CIRCLE_DIR_FOR_RETRY_COPY", null);
		localDir = getParamAsString("DAEMON", "LOCAL_DIR_FOR_RETRY_COPY", null);
		sleepTimeInMin = getParamAsInt("GATHERER", "GATHERER_SLEEP_INTERVAL", 5);
		
		if(ftpIp == null || (ftpIp = ftpIp.trim()).length() == 0 || ftpIp.equalsIgnoreCase("null")){
			logger.info("Please check the configuration of FTP_IP_FOR_RETRY_COPY [" + ftpIp + "]");
			return false;
		}
		
		if(localDir == null || (localDir = localDir.trim()).length() == 0 || localDir.equalsIgnoreCase("null")){
			logger.info("Please check the configuration of LOCAL_DIR_FOR_RETRY_COPY [" + localDir + "]");
			return false;
		}
		
		File file = new File(localDir);
		if (!file.exists()) {
			// local directory is does not exist
			file.mkdirs();
		}
		StringBuilder builder = new StringBuilder();
		builder.append("ftpIP = ");
		builder.append(ftpIp);
		builder.append(" , ftpPort = ");
		builder.append(ftpPort);
		builder.append(" , ftpUsername = ");
		builder.append(ftpUsername);
		builder.append(" , ftpPasswd = ");
		builder.append(ftpPasswd);
		builder.append(" , ftpCircleDir = ");
		builder.append(ftpCircleDir);
		builder.append(" , localDir = ");
		builder.append(localDir);
		logger.info(builder.toString());
		builder = null;
		if (localDir == null) {
			logger.info("local directory for retry copy is NULL");
		}
//		normalCopy= tokenizeArrayList(getParamAsString("COMMON","NORMALCOPY_KEY",null), ",");
//		starCopy=tokenizeArrayList(getParamAsString("COMMON","STARCOPY_KEY",null), ",");
		
		return true;
	}

	public void run() {
		while (m_parentGathererThread.isAlive()) {
			try {
				getAllFilesFromFTPServer();
				processAllFiles();
				try {
					Date next_run_time = m_parentGathererThread.roundToNearestInterVal(sleepTimeInMin);
					long sleeptime = m_parentGathererThread.getSleepTime(next_run_time);
					if (sleeptime < 1000) {
						sleeptime = 5000;
					}
					sleeptime = (3 * sleeptime);
					logger.info(_class + " Thread : sleeping for " + sleeptime + " mSecs.");
					Thread.sleep(sleeptime);
					logger.info(_class + " Thread : waking up.");
				} catch (InterruptedException ie) {
					logger.error("", ie);
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.info("got IOException " + e.getMessage());
				logger.info("Again Initializing parameters for ftpclient");
				initialize();
			} catch (FTPException e) {
				e.printStackTrace();
				logger.info("got FTPException "	+ e.getMessage());
				logger.info("Again Initializing parameters for ftpclient");
				initialize();
			}
		}
	}

	private void getAllFilesFromFTPServer() throws IOException, FTPException {
		FTPClient ftpClient = null;
		if (ftpIp != null && ftpUsername != null) {
			ftpClient = new FtpDownloader().ftpConnect(ftpIp, ftpPort, ftpUsername, ftpPasswd, ftpCircleDir);
			if (ftpClient == null) {
				StringBuilder builder = new StringBuilder("Unable to initialize the FTPClient ");
				builder.append(ftpIp + " ");
				builder.append(ftpPort + " ");
				builder.append(ftpUsername + " ");
				builder.append(ftpPasswd + " ");
				logger.info(builder.toString());
				builder = null;
				return;
			}
		}

		String[] fileList = ftpClient.dir();
		if (fileList == null || fileList.length == 0) {
			// no files are there in the FTP dir close the ftp client and return
			ftpClient.quit();
			return;
		}
		try {
			for (int i = 0; i < fileList.length; i++) {
				String fileName = fileList[i];
				fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
				fileName = fileName.toUpperCase();
				if (fileName.indexOf("RBT_COPY_RETRY_CDR") == -1 || fileName.indexOf(".TXT") == -1) {
					// file name format is wrong
					continue;
				}
				// copying the file from FTP server to local dir. Changing the
				// file name in the local dir to upper case.
				ftpClient.get(localDir + File.separator + fileName, fileList[i]);
				ftpClient.delete(fileList[i]);
			}
		} finally {
			// close the FTP connection
			ftpClient.quit();
		}
	}

	private void processAllFiles() throws IOException {
		// get all files
		// prcess
		File file = new File(localDir);
		File[] files = file.listFiles();
		if (files.length == 0) {
			// No files are exist in local dir
			// write the log
			logger.info("No files are exist in local dir " + localDir);
			return;
		}
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getAbsolutePath();
			processFile(fileName);
			files[i].delete();
		}
	}

	private void processFile(String localFilePath) throws IOException {
		// Timestamp format : yyyyMMddHHmmss
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		File file = new File(localFilePath);
		BufferedReader bf = new BufferedReader(new FileReader(file));
		String line = null;
		String keyPressed = "s";
		
		try {
			while ((line = bf.readLine()) != null) {
				logger.info("Processing copy line  : " + line);
//				String copyType = "COPY";
				String[] strAr = line.split(",");
				if (strAr.length < 2) {
					// write the line into the log file
					logger.info("Record is in incorrect format : " + line);
					continue;
				}
				String strTime = strAr[1];

//				if(strAr.length > 2)
//				{
//					keyPressed = strAr[2].trim();
//					copyType = getCopyType(strAr[2].trim());
//				}
				
				Date time = null;
				try {
					time = sdf.parse(strTime);
				} catch (ParseException e) {
					e.printStackTrace();
					logger.info("Wrong date format, could not parse timestamp "	+ strTime);
					logger.info("Date format incorrect in this line : " + line);
					continue;
				}
				String copyContent = strAr[0];
				StringTokenizer st = new StringTokenizer(copyContent, ":");
				String subscriberID = null;
				String callerID = null;
				String rbtWavFile = null;
				String catId = null;
				String status = null;
				String type = COPY;
				if (st.hasMoreTokens())
					subscriberID = st.nextToken();
				if (st.hasMoreTokens())
					callerID = st.nextToken();
				if (st.hasMoreTokens())
					rbtWavFile = st.nextToken();
				if (st.hasMoreTokens())
					catId = st.nextToken();
				if (st.hasMoreTokens())
					status = st.nextToken();
				
				// checking whether subscriber (callerID) belongs to circle or not
				Subscriber subscriber = subRbtClient.getSubscriber(callerID, null);
				if (subscriber == null) {
					continue;
				}
				boolean isValidSub = subscriber.isValidPrefix();
				if (!isValidSub) {
					continue;
				}
				// checking whether subscriber (callerID) made another selection
				// after this request.
				Settings settings = subRbtClient.getSettings(callerID);
				Setting[] settingsArr = settings.getSettings();
				boolean hasMadeAnotherSelection = false;
				for (Setting setting : settingsArr) {
					if (WebServiceConstants.ALL.equalsIgnoreCase(setting.getCallerID())) {
						Date setTime = setting.getSetTime();
						if (setTime.after(time) || setTime.equals(time)) {
							hasMadeAnotherSelection = true;
							break;
						}
					}
				}
				if (hasMadeAnotherSelection) {
					continue;
				}
				String clipId = rbtWavFile + ":" + catId + ":" + status;

				
				//Get the DTMF type from file
				if(strAr.length > 2){
					keyPressed = strAr[2].trim();
					type = CopyProcessorUtils.findSMSTypeFromDTMF(keyPressed, clipId);
					if(type == null || (!type.equalsIgnoreCase(COPY) && !type.equalsIgnoreCase(COPYSTAR))){
						logger.info("Invalid request clipId : " + clipId + " Request Type : "+ type );
						continue;
					}
				}
				HashMap<String, String> hashMap = new HashMap<String,String>();
				if(keyPressed != null && keyPressed.length() > 0 && !keyPressed.equalsIgnoreCase("null"))
					hashMap.put(iRBTConstant.KEYPRESSED_ATTR, keyPressed);
				subRbtClient.addViralData(subscriberID, callerID, type, clipId, "RETRY",hashMap);

//				subRbtClient.addViralData(subscriberID, callerID, copyType, clipId, "RETRY",hashMap);
			}
		} finally {
			if (bf != null) {
				try {
					bf.close();
				} catch (IOException ioe) {
					// ignore
				}
			}
		}
	}

//	private String getCopyType(String keyPressed)
//	{
//		logger.info("keyPressed is :"+keyPressed);
//		if(normalCopy != null)
//		{
//			for(int i = 0; i < normalCopy.size(); i++)
//				if(keyPressed.indexOf(normalCopy.get(i)) != -1)
//				{
//					return "COPY";
//				}
//		}
//		if(starCopy != null)
//		{
//			for(int i = 0; i < starCopy.size(); i++)
//				if(keyPressed.indexOf(starCopy.get(i)) != -1)
//				{
//					return "COPYSTAR";
//				}
//		}
//		return "COPY";
//	}

	public static void main(String[] args) throws Exception{
		RBTGatherer gatherer = new RBTGatherer();
		RBTFailedCopyThread rbtFailedCopyThread = new RBTFailedCopyThread(gatherer);
//		rbtFailedCopyThread.initialize();
		rbtFailedCopyThread.ftpIp = "10.9.11.27";
		rbtFailedCopyThread.ftpPort = 21;
		rbtFailedCopyThread.ftpUsername = "onspire";
		rbtFailedCopyThread.ftpPasswd = "poi@$iop^*";
		rbtFailedCopyThread.ftpCircleDir = "copyRequest";
		rbtFailedCopyThread.localDir = null; //"D:/rbt_products/work/report_rbtpath";
		System.out.println("Execute...");
//		rbtFailedCopyThread.start();
//		rbtFailedCopyThread.processAllFiles();
		rbtFailedCopyThread.getAllFilesFromFTPServer();
	}

	private String getParamAsString(String type, String param, String defualtVal) {
		try {
			return rbtGenericCacheWrapper.getParameter(type, param, defualtVal);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type);
			return defualtVal;
		}
	}

	private int getParamAsInt(String type, String param, int defaultVal) {
		try {
			String paramVal = rbtGenericCacheWrapper.getParameter(type, param,
					defaultVal + "");
			return Integer.valueOf(paramVal);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type);
			return defaultVal;
		}
	}
//	public static ArrayList<String> tokenizeArrayList(String stringToTokenize, String delimiter)
//	{
//		if (stringToTokenize == null)
//			return null;
//		String delimiterUsed = ",";
//
//		if (delimiter != null)
//			delimiterUsed = delimiter;
//
//		ArrayList<String> result = new ArrayList<String>();
//		StringTokenizer tokens = new StringTokenizer(stringToTokenize,
//				delimiterUsed);
//		while (tokens.hasMoreTokens())
//			result.add(tokens.nextToken().toLowerCase());
//
//		return result;
//	}


}
