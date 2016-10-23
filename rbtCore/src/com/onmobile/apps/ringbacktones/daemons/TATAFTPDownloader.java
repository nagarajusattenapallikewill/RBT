package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.subscriptionsImpl.RBTSubUnsub;

//added by eswar and sreekar
public class TATAFTPDownloader extends TimerTask {
	private static Logger logger = Logger.getLogger(TATAFTPDownloader.class);

	private Timer timer = new Timer();
	
	private String _ftpIPAdd;
	private int _ftpPort;
	private String _userName;
	private String _password;
	private int _timeOut = 1000*60*60;
	private String _remotePath;
	private String _remoteCompletedPath;
	private String _localPath;
	private String _localCompletedPath;
	private Calendar _date;
	private String _time;
	private long _ftpTransferInterval;

	private RBTDBManager _dbManager = null;
	private String _cosID = null;
	private HashMap _sms = new HashMap();
	
	private static TATAFTPDownloader downloader = null;
	
	private TATAFTPDownloader() throws RBTException {
		_ftpIPAdd = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "FTP_IP", null);
		try {
			_ftpPort = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "FTP_PORT", 0);
		}
		catch(Exception e) {
			_ftpPort = 21;
		}
		_userName = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "FTP_USER", null);
		_password = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "FTP_PASSWORD", null);
		_remotePath = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "FTP_REMOTE_PATH", null);
		_remoteCompletedPath = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "FTP_REMOTE_COMPLETED_PATH", null);
		_localPath = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "FTP_LOCAL_PATH", null);
		_localCompletedPath = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "FTP_LOCAL_COMPLETED_PATH", null);
		_time = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "TIME_TO_DOWNLOAD_FTP", null);
		_ftpTransferInterval = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "FTP_TRANSFER_INTERVAL", 24);
		_date = Calendar.getInstance();
		
		StringTokenizer strTok = new StringTokenizer(_time,":");
		if(strTok == null || strTok.countTokens() != 3) {
			logger.info("RBT::Time mentioned is not in right format");
			throw new RBTException("improper start time format");
		}
		_date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strTok.nextToken()));
		_date.set(Calendar.MINUTE, Integer.parseInt(strTok.nextToken()));
		_date.set(Calendar.SECOND, Integer.parseInt(strTok.nextToken()));
		
		_dbManager = RBTDBManager.getInstance();
		_cosID = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "BULK_FTP_COS_ID", null);
		String smsDaysStr = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "BULK_FTP_SMS_DAYS", null);
		StringTokenizer stk = new StringTokenizer(smsDaysStr, ",");
		_sms.put("-1", RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "BULK_PROMO_DEFAULT_WELCOME_SMS", null));
		_sms.put("-2", RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "BULK_PROMO_DEFAULT_TERMINATION_SMS", null));
		while(stk.hasMoreTokens()) {
			try {
				String thisToken = stk.nextToken();
				Integer.parseInt(thisToken);
				_sms.put(thisToken, RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "FTP_SMS_DAY_" + thisToken, null));
			}
			catch(Exception e) {
			}
		}
	}
	
	private FTPClient initFTPClient() {
		FTPClient client = null;
		logger.info("RBT:: Inside initFTPClient");
		try {
			logger.info("RBT:: Inside try block of initFTPClient");
			client = new FTPClient(_ftpIPAdd, _ftpPort);
			client.login(_userName, _password);
			client.setType(FTPTransferType.BINARY);
			client.setConnectMode(FTPConnectMode.PASV);
			client.setTimeout(_timeOut);
		}
		catch (IOException e) {
			logger.error("", e);
			quitFTPClient(client);
			return null;
		}
		catch (FTPException e) {
			logger.error("", e);
			quitFTPClient(client);
			return null;
		}
		logger.info("RBT:: client is " + client);
		return client;
	}
	
	private void quitFTPClient(FTPClient client) {
		logger.info("RBT:: Inside the quitFTPClient");
		
		try {
				if(client != null)
				{
					client.quit();
				}
				else
				{
					logger.info("RBT:: Inside the quitFTPClient value is null for client");
				}
			}
			catch (IOException e) {
				logger.error("", e);
			}
			catch (FTPException e) {
				logger.error("", e);
			}
	}
	
	/**
	 * Initiliaze the FTP Downloader Task
	 */
	public void init(){
		logger.info("RBT:: Initiliazing the TATA FTP DOWNLOADER");
		if(_date.getTime()!=null)
		{
			timer.scheduleAtFixedRate(this,_date.getTime(),_ftpTransferInterval*60*60*1000);
		}
		else
		{
			logger.info("RBT:: Initiliazing the TATA FTP DOWNLOADER failed");
		}
	}
	
	public static TATAFTPDownloader getInstance()
	{
		try
		{
			if(downloader == null)
			downloader = new TATAFTPDownloader();
		}
		catch (RBTException e)
		{
				logger.error("", e);
				return null;
		}
		
		return downloader;
	}
	private void processFTPFiles() {
		logger.info("RBT:: Processing the FTP files started");
		ArrayList downloadedList = downloadLatestFiles();
		if(downloadedList == null) {
			logger.info("RBT::no files to process");
			return;
		}
		transferDownloadedFiles(downloadedList);
		processFiles(downloadedList);
		logger.info("RBT:: Processing the FTP files completed");
	}
	
	/**
	 * copies file back to the FTP to a completed folder
	 * @param list of files to be copied to remote completed dir
	 */
	private void transferDownloadedFiles(ArrayList downloaded) {
		logger.info("RBT:: Inside the Downloaded Files");
		FTPClient client = null;
		try {
			client = initFTPClient();
			logger.info("RBT:: Connection on FTP Successful");
			client.chdir(_remoteCompletedPath);
			for (int i = 0; i < downloaded.size(); i++) {
				logger.info("RBT::starting to copy file "
						+ downloaded.get(i).toString() + " to remote competed path");
				client.put(_localPath + File.separator + downloaded.get(i).toString(),
						downloaded.get(i).toString());
				logger.info("RBT::completed copying file "
						+ downloaded.get(i).toString());
			}
		}
		catch (IOException e) {
			logger.error("", e);
		}
		catch (FTPException e) {
			logger.error("", e);
		}
		finally {
			quitFTPClient(client);
		}
		logger.info("RBT:: Inside the Downloaded Files ended");
	}
	
	/**
	 * 
	 * @return returns the ArrayList of latest downloaded file names
	 */
	private ArrayList downloadLatestFiles() {
		FTPClient client = null;
		ArrayList downloadList = new ArrayList();
		logger.info("RBT:: inside the download latest files");
		try {
			client = initFTPClient();
			//changing to the remote dir
			client.chdir(_remotePath);
			FTPFile[] filesList = client.dirDetails(".");
			if(filesList == null) {
				logger.info("RBT::no files to process");
				return null;
			}
			for(int fc = 0; fc < filesList.length; fc++) {
				if(filesList[fc].isDir()) {
					logger.info("RBT::Not proessing entry "
							+ filesList[fc].getName() + " as it is a directory");
					continue;
				}
				downloadList.add(filesList[fc].getName());
				logger.info("RBT::downloading file " + filesList[fc].getName());
				client.get(_localPath + File.separator + filesList[fc].getName(), filesList[fc].getName());
				logger.info("RBT::downloading complete - file " + filesList[fc].getName());
				//deleting the downloaded file
				client.delete(filesList[fc].getName());
				logger.info("RBT::deleted file " + filesList[fc].getName());
			}
		}
		catch (IOException e) {
			logger.error("", e);
		}
		catch (FTPException e) {
			logger.error("", e);
		}
		catch (ParseException e) {
			logger.error("", e);
		}
		finally {
			quitFTPClient(client);
		}
		if(downloadList.isEmpty())
			return null;
		return downloadList;
	}


	/**
	 * Reads all the files
	 * @param file
	 */
	private void readAllFiles(File dir, ArrayList downloadedfiles) {
		logger.info("RBT::" + downloadedfiles + " files to process");
		String filepath = null;
		for (int i = 0; i < downloadedfiles.size(); i++) {
			filepath = dir.getAbsolutePath() + File.separator + downloadedfiles.get(i).toString();
			File file = new File(filepath);
			if(!file.isDirectory())
				readFile(file);
		}
	}
	
	/**
	 * read the file from the path mentioned
	 * @param file
	 */
	private void readFile(File file) {
		BufferedReader br = null;
		logger.info("RBT::Processing file - " + file.getName());

		CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(_cosID);
		String bulkPromoID = createBulkPromo(cos);
		RBTSubUnsub rbtLogin = RBTSubUnsub.init();
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				rbtLogin.activateSubscriberByBulkPromo(line, bulkPromoID, true, cos, true);
			}
		}
		catch (IOException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(br != null)
					br.close();
			}
			catch (IOException e) {
				logger.error("", e);
			}
			moveCompletedFile(file);
		}
	}
	
	private void moveCompletedFile(File file) {
		logger.info("RBT::moving file " + file.getName() + " to local completed path");
		if(!file.renameTo(new File(_localCompletedPath + File.separator + System.currentTimeMillis()+"-"+file.getName()))) {
			logger.info("RBT:: moving of the file " + System.currentTimeMillis()+"-"+file.getName()
					+ " failed, deleting the same");
			file.delete();
		}
	}
	
	/**
	 * makes entries into RBT_BULK_PROMO & RBT_BULK_PROMO_SMS
	 * @return returns the bulk promo id which has been created
	 */
	private String createBulkPromo(CosDetails cos) {
		String bulkPromoID = "bulk-" + System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, cos.getValidDays());
		Date endDate = calendar.getTime();
		_dbManager.addBulkPromo(bulkPromoID, null, endDate, _cosID);
		logger.info("RBT::creating the bulk promo "+bulkPromoID);

		Iterator itr = _sms.keySet().iterator();
		while (itr.hasNext()) {
			try {
				String smsDay = (String) itr.next();
				_dbManager.addBulkPromoSMS(bulkPromoID, Integer.parseInt(smsDay), (String) _sms
						.get(smsDay));
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}

		return bulkPromoID;
	}

	//Start reading the files from the directory
	private void processFiles(ArrayList downloadedList) {
		logger.info("RBT:: Inside the process Files");
		File file = new File(_localPath);
		if(file.exists() && file.isDirectory())
			readAllFiles(file, downloadedList);
	}
	
	
	/**
	 * Overrided run method for the timertask
	 */
	public void run() {
		try
		{
			processFTPFiles();	
		}
		catch (Exception e)
		{
				logger.error("", e);
		}
	}


	public static void main(String[] args){
		//create a timer object
		Timer timer = new Timer();
		Calendar date = Calendar.getInstance();
		String dateFormat = new String("15:25:00");//Hour:Minute:Second for the configs
		//initialize the time
		StringTokenizer strtok = new StringTokenizer(dateFormat,":");
		
		date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strtok.nextToken()));
		date.set(Calendar.MINUTE, Integer.parseInt(strtok.nextToken()));
		date.set(Calendar.SECOND, Integer.parseInt(strtok.nextToken()));
		//initialize the time for the timer task
		try {
			timer.schedule(new TATAFTPDownloader(),date.getTime());
		}
		catch (RBTException e) {
			logger.error("", e);
		}
	}
}