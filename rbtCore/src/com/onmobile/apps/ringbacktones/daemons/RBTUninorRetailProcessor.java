/**
 * Ring Back Tone 
 * Copyright OnMobile 2011
 * 
 * $Author: rajesh.karavadi $
 * $Id: RBTUninorRetailProcessor.java,v 1.21 2013/01/23 07:55:39 rajesh.karavadi Exp $
 * $Revision: 1.21 $
 * $Date: 2013/01/23 07:55:39 $
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.FileDetailsImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

/**
 * Pick all the *.processed files from the remote server and process RBT
 * activation, delete files from remote location after processing.
 * 
 * Remote file contains multiple CDRs, each line has one CDR. Each CDR has
 * multiple entries like CDR type, MSISDN, source, service etc are separated by
 * pipe (|).
 * 
 * RBT should process only certain CDRs based on the selection.
 * 
 * 1. CDR type should be SelfcareTopup and Recharge.
 * 
 * 2. Source field should be (VCH, EVDT, EVDFT, EVDS, EVDSI) as configured.
 * 
 * 3. Based on CDR type, circle and voucher type the Subscription class will be
 * decided from the configuration.
 * 
 * 4. Mode is configured for different source (ex: VCH=RETAIL).
 * 
 * RBT activation process.
 * 
 * Case 1: If the subscriber is not active, base will be activated.
 * 
 * Case 2: If the subscriber is already active, then upgrade to rental pack.
 * 
 * Case 3: If the subscriber is already active and is in the same pack updates
 * the pack. In case 2 and case 3 the process is same. It update column
 * subscription_yes. i.e. from B to C.
 * 
 * @Since 06-Dec-2011
 */
public class RBTUninorRetailProcessor extends Thread {

	/* Parameters configured in the DB */
	private ParametersCacheManager parametersCacheManager;
	private String remoteDirPath;
	private String localDirPath;
	private String ftpServerIp;
	private String user;
	private String password;
	private Map<String, String> modesMap;
	private Map<String, List<String>> selfcareTopupSubscriptionClasses;
	private Map<String, List<String>> rechargeSubscriptionClasses;
	private int port;
	private String remoteFileExtension;
	private int sleepInterval;

	/* The components needed to process */
	private static RBTClient rbtClient = null;
	private RBTDaemonManager rbtDaemonManager = null;

	/* Constants */
	private static final String RECHARGE_TYPE = "Recharge";
	private static final String SELFCARE_TOPUP_TYPE = "SelfcareTopup";
	private static final String CDR_SPLIT_REGEX = "\\|";
	private static final int CDR_TYPE_POSITION = 0;
	private static final int MSISDN_POSITION = 4;
	private static final int SELFCARE_TOPUP_SOURCE_POSITION = 8;
	private static final int SELFCARE_TOPUP_VOUCHER_TYPE_POSITION = 9;
	private static final int RECHARGE_SOURCE_POSITION = 10;
	private static final int RECHARGE_VOUCHER_TYPE_POSITION = 11;
	private static final String COULD_NOT_PROCESS = "Could not process subscriber: ";

	private static Logger LOGGER = Logger
			.getLogger(RBTUninorRetailProcessor.class);
	private static final Logger transactionLog = Logger.getLogger("TransactionLogger");
	public RBTUninorRetailProcessor(RBTDaemonManager daemonManager) {
		try {
			// Load the configurations from DB to Cache.
			parametersCacheManager = CacheManagerUtil
					.getParametersCacheManager();
			// Get the values from the Cache.
			remoteDirPath = getParamAsString("RETAIL_TYPE5_REMOTE_DIR_PATH",
					null);
			localDirPath = getParamAsString("RETAIL_TYPE5_LOCAL_DIR_PATH", "/");
			ftpServerIp = getParamAsString("RETAIL_TYPE5_FTP_SERVER_IP", null);
			user = getParamAsString("RETAIL_TYPE5_FTP_SERVER_USER", null);
			password = getParamAsString("RETAIL_TYPE5_FTP_SERVER_PASSWORD",
					null);
			port = Integer.parseInt(getParamAsString(
					"RETAIL_TYPE5_FTP_SERVER_PORT", "21"));
			remoteFileExtension = getParamAsString(
					"RETAIL_TYPE5_REMOTE_FILE_EXTENSION", "process");
			modesMap = getParamAsMap("MODES_FOR_RETAIL_TYPE5", null);
			selfcareTopupSubscriptionClasses = getSubscriptionClassesAsMap(
					"RETAIL_TYPE5_SELFCARE_TOPUP_SUBSCRIPTION_CLASSES", null);
			rechargeSubscriptionClasses = getSubscriptionClassesAsMap(
					"RETAIL_TYPE5_RECHARGE_SUBSCRIPTION_CLASSES", null);
			sleepInterval = getParamAsInt("SLEEP_INTERVAL_MINUTES", "5");
			// Initialise RBT Client after loading all the required parameters
			// from Cache.
			rbtClient = RBTClient.getInstance();
			rbtDaemonManager = daemonManager;
			LOGGER.info("RBTUninorRetailProcessor Intialized successfully");
		} catch (Exception e) {
			LOGGER.error(
					"Failed to initialize RBTUninorRetailProcessor. error message: "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Connect and gets the file from FTP server.
	 * 
	 * Reads the file content and gets the MSISDN's to be processed.
	 * 
	 */
	@Override
	public void run() {
		LOGGER.debug("Getting to start..");
		LOGGER.info(" rbtDaemonManager: " + rbtDaemonManager);
		while (rbtDaemonManager != null && rbtDaemonManager.isAlive()) {
			try {
				LOGGER.debug("started copying remote files..");
				copyRemoteFilesAndDelete();
				processFiles();
				LOGGER.info("Sleeping for " + sleepInterval
						+ " minutes............");
				Thread.sleep(sleepInterval * 60 * 1000);
			} catch (Exception e) {
				LOGGER.error(
						"Exception while running RBTUninorRetailProcessor: "
								+ e.getMessage(), e);
			}
		}
		LOGGER.debug("RBTUninorRetailProcessor Thread is Stopped working..");
	}

	/**
	 * Makes FTP call, copy files from remote directory to local directory from
	 * a specified path.
	 * 
	 * @return true/false
	 * @throws IOException
	 */
	private void copyRemoteFilesAndDelete() {
		FileOutputStream fos = null;
		FTPClient ftpClient = null;
		FTPFile[] list = null;
		long start = System.currentTimeMillis();
		LOGGER.debug("Getting ftpClient ");
		int copyFileCount = 0;
		try {
			try {
				ftpClient = getFtpClient();
				list = ftpClient.listFiles(remoteDirPath);
				LOGGER.debug("Total files in FTP Server: " + list.length);

				for (FTPFile ftpFile : list) {
					if (ftpFile.isFile()) {
						String fileName = ftpFile.getName();
						// Remote file is of configured type and is not
						// processed.
						if (isFileTypeProcessed(fileName)
								&& !isFileAlreadyCopied(fileName)
								&& !isFileAlreadyProcessed(fileName)
								&& !isFileSavedInDB(fileName)) {
							File file = new File(localDirPath.concat(fileName));
							fos = new FileOutputStream(file);
							boolean isCopied = ftpClient.retrieveFile(
									remoteDirPath.concat(fileName), fos);
							if (isCopied) {
								copyFileCount++;
								/*
								 * boolean isDeleted = ftpClient
								 * .deleteFile(remoteDirPath .concat(fileName));
								 * if (!isDeleted) { throw new Exception(
								 * "Unable to delete the file:" + fileName); }
								 */
								LOGGER.debug("FTP File: " + fileName
										+ " copied successfully");
							} else {
								LOGGER.debug("File: " + fileName
										+ " is not copied ");
							}
						} else {
							LOGGER.debug("Not copying File: " + fileName
									+ ". Since it is already processed");
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error("Unable to copy or delete the file from FTP."
						+ " Exception: " + e.getMessage(), e);
			} finally {
				if (null != fos) {
					fos.flush();
					fos.close();
				}
				closeFTPClient(ftpClient);
			}
		} catch (IOException ioe) {
			LOGGER.error("IOException: " + ioe.getMessage(), ioe);
		}
		long end = System.currentTimeMillis();
		LOGGER.info("Moved: " + copyFileCount
				+ " files from remote directory. Time taken to Copy: "
				+ (end - start));

	}

	/**
	 * Fetch the files from local directory and check each file weather it is
	 * already processed or not. If the file is not processed, process it.
	 */
	private void processFiles() {
		LOGGER.info("Started processing local files under : " + localDirPath);
		try {
			File localFiles = new File(localDirPath);
			if (localFiles.isDirectory()) {
				File[] files = localFiles.listFiles();
				for (File file : files) {
					String fileName = file.getName();
					if (!isFileAlreadyProcessed(fileName)
							&& !isFileSavedInDB(fileName)) {
						if (filterAndProcess(file)) {
							if (saveFileDetailsInDB(file)) {
								renameFile(file);
							} else {
								LOGGER.warn("Failed to save File: "
										+ file.getName());
							}
						}
					} else {
						LOGGER.warn("File: " + file.getName()
								+ " already processed");
					}
				}
				if (files.length == 0) {
					LOGGER.warn("No files found under: " + localDirPath);
				}
			}
		} catch (IOException ioe) {
			LOGGER.error(
					"Unable to process file. IOException: " + ioe.getMessage(),
					ioe);
		} catch (Exception e) {
			LOGGER.error(
					"Unable to process the files. Exception: " + e.getMessage(),
					e);
		}
		LOGGER.info("Processed local files successfully");
	}

	/**
	 * Process the given file. Read the MSISDNs from the CDR and tries to
	 * activate,update and extend subscription.
	 * 
	 * @param file
	 * @return true/false
	 * @throws IOException
	 */
	private boolean filterAndProcess(File file) throws IOException {
		long start = System.currentTimeMillis();
		LOGGER.debug("Started processing file: " + file);
		FileInputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader reader = null;
		boolean processed = false;
		try {
			inputStream = new FileInputStream(file);
			inputStreamReader = new InputStreamReader(inputStream);
			reader = new BufferedReader(inputStreamReader);
			String line = null;
			while ((line = reader.readLine()) != null) {
				String cdrTypeStr = null;
				String subscriberId = null;
				String voucherType = null;
				String source = null;
				LOGGER.debug("File: " + file.getName() + ", contains: " + line);
				String[] cdr = line.split(CDR_SPLIT_REGEX);

				if (10 < cdr.length) {
					cdrTypeStr = cdr[CDR_TYPE_POSITION];
					CdrType cdrType = CdrType.getCdrTypeFromType(cdrTypeStr);
					subscriberId = cdr[MSISDN_POSITION];
					if (SELFCARE_TOPUP_TYPE.equals(cdrTypeStr)) {
						voucherType = cdr[SELFCARE_TOPUP_VOUCHER_TYPE_POSITION]
								.trim();
						source = cdr[SELFCARE_TOPUP_SOURCE_POSITION].trim();
					} else if (RECHARGE_TYPE.equals(cdrTypeStr)) {
						voucherType = cdr[RECHARGE_VOUCHER_TYPE_POSITION]
								.trim();
						source = cdr[RECHARGE_SOURCE_POSITION].trim();
					}
					// After reading all the parameters from CDR, process it.
					if (null != cdrType && null != source
							&& null != voucherType && null != subscriberId) {
						String response = processSubscriber(cdrTypeStr, source, voucherType,
								subscriberId);
						transactionLog.info("File= "+file.getName()+"|msisdn|{Type= "+cdrType+",Source= "+source+",voucherType= "+voucherType+",MSISDN= "+subscriberId+"}|MATCHED|SUCCESS|"+response);
					} else {
						LOGGER.debug(" Mandatory values are not present. subscriberId: "
								+ subscriberId
								+ ", serviceType: "
								+ voucherType
								+ ", cdrType: "
								+ cdrType
								+ ", source: " + source);
						transactionLog.info("File= "+file.getName()+"|Msisdn= "+subscriberId+"|Cdrtype= "+cdrTypeStr+"|NON_MATCH");
					}

				} else {
					LOGGER.debug("Cannot process CDR. It has only : "
							+ cdr.length + " entries.");
				}

			}
			processed = true;
		} catch (FileNotFoundException fnfe) {
			LOGGER.error("Error while fetching the file: " + fnfe.getMessage(),
					fnfe);
		} catch (IOException ioe) {
			LOGGER.error("Error while reading the line" + ioe.getMessage(), ioe);
		} finally {
			if (null != inputStream) {
				inputStream.close();
			}
			if (null != inputStreamReader) {
				inputStreamReader.close();
			}
			if (null != reader) {
				reader.close();
			}
		}
		long end = System.currentTimeMillis();
		LOGGER.info("File: " + file.getName() + ", processed: " + processed
				+ " successfully, Time taken to process: " + (end - start));
		return processed;
	}

	/**
	 * Process the CDRs of type Recharge or SelfcareTopup.
	 * 
	 * @param cdr
	 * @param source
	 * @param voucherType
	 * @param subscriberId
	 */
	private String processSubscriber(String cdr, String source,
			String voucherType, String subscriberId) {

		LOGGER.debug("Started processing msisdn: " + subscriberId + ", cdr: "
				+ cdr + ", source: " + source + ", voucherType: " + voucherType);
		CdrType cdrType = CdrType.getCdrTypeFromType(cdr);
		String response ="INVALID_CDR";
		Subscriber subscriber = getSubscriber(subscriberId);
		if(null == subscriber || null == subscriber.getCircleID()) {
			response = "INVALID_SUBSCRIBER";
			LOGGER.debug(response+": " + subscriberId);
			return response;
		}

		switch (cdrType) {
		case RECHARGE:
		case TOPUP:
			String mode = getKeyFromMap(modesMap, source);
			if (null != mode) {
				String subClassFromCache = getSubscriptionClassFromCache(
						cdrType, source, voucherType, subscriber.getCircleID());

				if (null != subClassFromCache && null != mode) {
					// Activate if the subscriber is not active or
					// update if subscriber is in some other pack
					response = activateOrUpdateSubscription(subscriberId,
							subClassFromCache, mode);

				} else {
					LOGGER.debug(COULD_NOT_PROCESS + subscriberId
							+ ", Subscription class or Mode is null."
							+ " subscriptionClass: " + subClassFromCache
							+ ", mode: " + mode);
					String key = subscriber.getCircleID().concat("-").concat(source).concat(
					"-").concat(voucherType);
					response = "SubscriptionClass is not found for: "+key;
				}

			} else {
				LOGGER.debug(COULD_NOT_PROCESS + subscriberId
						+ ", Unknown Source: " + source);
				response = "Mode is not found for: "+source;
			}
			break;
		default:
			LOGGER.debug(COULD_NOT_PROCESS + subscriberId + ", Invalid CDR: "
					+ cdr + ", source: " + source + ", voucherType: "
					+ voucherType);
		}
		return response;
	}

	private String getSubscriptionClassFromCache(CdrType cdrType,
			String source, String voucherType, String circleId) {
		LOGGER.debug("CdrType: " + cdrType.name() + ", circleId: " + circleId
				+ ", source: " + source + ", voucherType: " + voucherType);
		Map<String, List<String>> map = null;
		String subscriptionClass = null;
		String key = null;
		try {
			key = circleId.concat("-").concat(source).concat("-")
					.concat(voucherType);
			switch (cdrType) {
			case RECHARGE:
				map = rechargeSubscriptionClasses;
				break;
			case TOPUP:
				map = selfcareTopupSubscriptionClasses;
				break;
			case INVALID:
				LOGGER.warn("Invalid CDR TYPE");
			}
			LOGGER.debug(" map: " + map);
			for (Entry<String, List<String>> entry : map.entrySet()) {
				if (entry.getValue().contains(key)) {
					subscriptionClass = entry.getKey();
				}
			}
		} catch (Exception e) {
			LOGGER.error("Unable to get for circleAndVoucherType: " + key
					+ ", Exception: " + e.getMessage(), e);
		}
		LOGGER.debug("For SubscriptionClass: " + subscriptionClass
				+ " the circleAndVoucherType: " + key);
		return subscriptionClass;
	}

	/**
	 * Activate or update base subscription. Subscribe to retail pack if the
	 * subscriber is not active or upgrade to rental pack if the subscriber is
	 * already active.
	 * 
	 * @param msisdn
	 */
	private String activateOrUpdateSubscription(String msisdn, String subClass,
			String mode) {
		LOGGER.debug(" Activating subscriber: " + msisdn + ", subClass: "
				+ subClass + ", mode: " + mode);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				msisdn);
		subscriptionRequest.setRentalPack(subClass);
		subscriptionRequest.setMode(mode);
		subscriptionRequest.setUpgradeGraceAndSuspended(true);
		subscriptionRequest.setSuspendedUsersAllowed(true);

		// activate or update subscription
		rbtClient.activateSubscriber(subscriptionRequest);
		String response = subscriptionRequest.getResponse();
		LOGGER.debug(" Tried to activate/update subscriber: " + msisdn
				+ ", response: " + response);
		return response;
	}

	/**
	 * Return the subscriber details from DB.
	 * 
	 * @param msisdn
	 * @return subscriber object
	 */
	private Subscriber getSubscriber(String msisdn) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(msisdn);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		LOGGER.debug(" Subscriber Id: " + subscriber.getSubscriberID()
				+ ", status: " + subscriber.getStatus()
				+ ", subscription class: " + subscriber.getSubscriptionClass());
		return subscriber;
	}

	/**
	 * Return true if the given file is already processed.
	 * 
	 * @param filename
	 * @return
	 */
	private boolean isFileAlreadyProcessed(String filename) {
		boolean fileExists = false;
		if (getFilenameWithoutExt(filename).contains(remoteFileExtension)) {
			LOGGER.debug(" file: " + filename + " isAlreadyProcessed: true");
			return true;
		}
		String name = getFileNameAsProcessed(filename);
		File file = new File(localDirPath.concat(name));
		fileExists = file.exists();
		LOGGER.debug(" file: " + filename + " isAlreadyProcessed: "
				+ fileExists);
		return fileExists;
	}

	/**
	 * Verifies the given file weather it is already copied to the local
	 * directory or not. Returns true if it is already copied.
	 * 
	 * @param filename
	 * @return
	 */
	private boolean isFileAlreadyCopied(String filename) {
		String name = localDirPath.concat(filename);
		File file = new File(name);
		boolean fileExists = file.exists();
		LOGGER.debug(" file: " + file.getName() + " isFileAlreadyCopied: "
				+ fileExists);
		return fileExists;

	}

	private String getKeyFromMap(Map<String, String> map, String value) {
		String result = null;
		for (Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue().contains(value)) {
				result = entry.getKey();
			}
		}
		LOGGER.debug(" Key: " + result + " for value: " + value);
		return result;
	}

	private boolean isFileTypeProcessed(String fileName) {
		String fileExtension = getFileExt(fileName);
		if (fileExtension.equals(remoteFileExtension)) {
			LOGGER.debug(" File: \"" + fileName + "\" type is "
					+ remoteFileExtension + " file, isFileTypeProcessed: true");
			return true;
		} else {
			LOGGER.debug(" Unknown file extension: " + fileExtension
					+ " of Remote file: " + fileName);
			return false;
		}
	}

/**
	 * Rename the given file to processed file. For ex: file A.txt renamed to
	 * A_processed.txt
	 * 
	 * @param file
	 */
	private void renameFile(File file) {
		if (file.exists()) {
			String newFilename = getFileNameAsProcessed(file.getName());
			boolean renamed = file.renameTo(new File(localDirPath
					.concat(newFilename)));
			LOGGER.debug("File: " + file + " renamed to " + newFilename
					+ " rename status: " + renamed);
		}

	}

	/**
	 * Returns file name without extension
	 * 
	 * @param filename
	 * @return
	 */
	public String getFilenameWithoutExt(String filename) {
		int dot = filename.lastIndexOf(".");
		return filename.substring(0, dot);
	}

	/**
	 * Returns file extension
	 * 
	 * @param filename
	 * @return
	 */
	public String getFileExt(String filename) {
		int dot = filename.lastIndexOf(".");
		return filename.substring(dot + 1, filename.length());
	}

	/**
	 * Returns the file name as processed representation. For ex: A.txt renamed
	 * to A_processed.txt
	 * 
	 * @param filename
	 * @return filname_processed.extension
	 */
	public String getFileNameAsProcessed(String filename) {
		int dot = filename.lastIndexOf(".");
		StringBuffer sb = new StringBuffer();
		sb.append(filename.substring(0, dot)).append("_")
				.append(remoteFileExtension)
				.append(filename.substring(dot, filename.length()));
		return sb.toString();
	}

	/**
	 * 
	 * @param file
	 */
	private void deleteFile(File file) {
		if (file.exists()) {
			boolean deleted = file.delete();
			LOGGER.debug("File: " + file.getName() + " deleted: " + deleted);
		}
	}

	private boolean saveFileDetailsInDB(File file) {
		boolean isInserted = false;
		if (file.exists()) {
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			isInserted = rbtDBManager.insertFileDetailsImpl(file.getName());
			LOGGER.debug("File: " + file + " saved in DB. isInserted:"
					+ isInserted);
		}
		return isInserted;
	}

	private boolean isFileSavedInDB(String name) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		FileDetailsImpl fileDetailsImpl = rbtDBManager.getFileDetailsImpl(name);

		if (null != fileDetailsImpl) {
			return true;
		}
		return false;
	}

	/**
	 * Create FTP client.
	 * 
	 * @return FTPClient
	 * @throws SocketException
	 * @throws IOException
	 */
	private FTPClient getFtpClient() throws SocketException, IOException {
		LOGGER.debug("Initailising FtpClient");
		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(ftpServerIp, port);
		LOGGER.debug("Connected FtpClient to IP: " + ftpServerIp
				+ " and Port: " + port);
		ftpClient.login(user, password);
		LOGGER.debug("Login FtpClient as User: " + user + " and Password: "
				+ password);
		int reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			LOGGER.warn(" FtpClient reply code is NOT POSITIVE. reply code: "
					+ reply);
			throw new IOException("FTP connection is not established");
		}
		LOGGER.info(" FtpClient connection is success. ftpClient: " + ftpClient);
		return ftpClient;
	}

	/**
	 * Close FTPClient connection
	 * 
	 * @param ftpClient
	 * @throws IOException
	 */
	private void closeFTPClient(FTPClient ftpClient) throws IOException {
		if (null != ftpClient && ftpClient.isConnected()) {
			ftpClient.logout();
			ftpClient.disconnect();
		}
	}

	private Map<String, List<String>> getSubscriptionClassesAsMap(String param,
			String defaultValue) throws Exception {
		String result = getParamAsString(param, defaultValue);
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> list = new ArrayList<String>();
		String[] subClassCircleVoucherTypes = result.split(";");
		for (String subClassCircleVoucherType : subClassCircleVoucherTypes) {
			String[] voucherTypeAndCircles = subClassCircleVoucherType
					.split(":");
			String[] voucherTypeAndCircle = voucherTypeAndCircles[1].split(",");
			list = new ArrayList<String>();
			for (String vtc : voucherTypeAndCircle) {
				list.add(vtc);
			}
			map.put(voucherTypeAndCircles[0], list);
		}
		LOGGER.debug(" Param: " + param + ", Map: " + map);
		return map;
	}

	private Map<String, String> getParamAsMap(String param, String defaultValue)
			throws Exception {
		String result = getParamAsString(param, defaultValue);
		Map<String, String> map = new HashMap<String, String>();
		String[] keyValuePairs = result.split(":");
		for (String keyValuePair : keyValuePairs) {
			String[] keyValue = keyValuePair.split("=");
			map.put(keyValue[1], keyValue[0]);
		}
		LOGGER.debug(" Param: " + param + ", Map: " + map);
		return map;
	}

	private int getParamAsInt(String param, String defaultValue)
			throws Exception {
		String value = getParamAsString(param, defaultValue);
		return Integer.parseInt(value);
	}

	private String getParamAsString(String param, String defaultValue)
			throws Exception {
		String paramValue = null;
		try {
			paramValue = parametersCacheManager.getParameter(
					iRBTConstant.DAEMON, param, defaultValue).getValue();
		} catch (Exception e) {
			LOGGER.error(
					"Unable to get " + param + ", Exception: " + e.getMessage(),
					e);
			throw e;
		}
		return paramValue;
	}
}

enum CdrType {
	RECHARGE("Recharge"), TOPUP("SelfcareTopup"), INVALID("Invalid");

	private String cdrType;

	CdrType(String type) {
		cdrType = type;
	}

	public String getCdrType() {
		return cdrType;
	}

	public static CdrType getCdrTypeFromType(String type) {
		for (CdrType cType : CdrType.values()) {
			if ((cType.getCdrType()).equals(type)) {
				return cType;
			}
		}
		return CdrType.INVALID;
	}
}
