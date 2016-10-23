/**
 * RingBackTone
 * 
 * $Author: gautam.agrawal $
 * $Id: RBTIdeaRetailProcessor.java,v 1.6 2012/12/24 06:37:30 gautam.agrawal Exp $
 * $Revision: 1.6 $
 * $Date: 2012/12/24 06:37:30 $
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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * Receives the Subscriber MSISDN's from the remote file and try to activate for
 * the retail pack.
 * 
 * Case 1: If the subscriber is not active, base RBT subscription will be done
 * for the retail pack.
 * 
 * Case 2: If the subscriber is already active and subscribed to some other pack
 * instead of retail pack, update the subscription to retail pack.
 * 
 * Case 3: If the subscriber is already active and subscribed to the same retail
 * pack, extend the validity for the same pack.
 * 
 * Version: $Revision: 1.6 $
 * 
 * @author rajesh.karavadi
 */
public class RBTIdeaRetailProcessor extends Thread {

	private static RBTClient rbtClient = null;
	private RBTDaemonManager rbtDaemonManager = null;
	private ParametersCacheManager parametersCacheManager;
	private String remoteDirPath;
	private String localDirPath;
	private String mode;
	private String ftpServerIp;
	private String user;
	private String password;
	private int port;
	private String[] tnbSubscriptions;
	private String cosId;
	private CosDetails cosDetails;
	private String subscriptionClassForValidityExtension = null;
	private String chargeClassForValidityExtension = null;
	

	private static Logger LOGGER = Logger
			.getLogger(RBTIdeaRetailProcessor.class);

	public RBTIdeaRetailProcessor(RBTDaemonManager daemonManager)
	{
		try
		{
			setName("RBTIdeaRetailProcessor");
			// Load the configurations from DB to Cache.
			parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
			// Get the values from the Cache.
			remoteDirPath = getParamAsString("RETAIL_TYPE4_REMOTE_DIR_PATH", null);
			localDirPath = getParamAsString("RETAIL_TYPE4_LOCAL_DIR_PATH", "/");
			mode = getParamAsString("MODE_FOR_RETAIL_TYPE4", "RETAIL");
			ftpServerIp = getParamAsString("RETAIL_TYPE4_FTP_SERVER_IP", null);
			user = getParamAsString("RETAIL_TYPE4_FTP_SERVER_USER", null);
			password = getParamAsString("RETAIL_TYPE4_FTP_SERVER_PASSWORD", null);
			port = Integer.parseInt(getParamAsString(
					"RETAIL_TYPE4_FTP_SERVER_PORT", "21"));
			String tnbSubscriberStr = getParamAsString(
					"RETAIL_TYPE4_TNB_SUBSCRIBER", null);
			tnbSubscriptions = tnbSubscriberStr.split(",");
			cosId = getParamAsString("RETAIL_TYPE4_SUBSCRIBER_COS_ID", null);
			cosDetails = (CosDetails) CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(cosId);
			// Configurations for validity extension are optional.
		
			subscriptionClassForValidityExtension = getParamAsString(
					"SUB_CLASS_FOR_BASE_VALIDITY_EXTEND_FOR_KK_RETAIL", null);
			
			// Validating the configured subscription class, if it is invalid
			// reset again to null.
			SubscriptionClass subClass = CacheManagerUtil
					.getSubscriptionClassCacheManager().getSubscriptionClass(
							subscriptionClassForValidityExtension);
			if (null == subClass) {
				LOGGER.warn("Invalid subscription class configured: "
						+ subscriptionClassForValidityExtension);
				subscriptionClassForValidityExtension = null;
			}

			chargeClassForValidityExtension = getParamAsString(
					"CHARGE_CLASS_FOR_SELECTION_VALIDITY_EXTEND_FOR_KK_RETAIL",
					null);
			// Validating the configured charge class, if it is invalid
			// reset again to null.
			ChargeClass chargeClass = CacheManagerUtil
					.getChargeClassCacheManager().getChargeClass(
							chargeClassForValidityExtension);
			if (null == chargeClass) {
				LOGGER.warn("Invalid charge class configured: "
						+ chargeClassForValidityExtension);
				chargeClassForValidityExtension = null;
			}
			rbtClient = RBTClient.getInstance();
			this.rbtDaemonManager = daemonManager;
			LOGGER.info("RBTIdeaRetailProcessor intialized successfully");

		} catch (Exception e) {
			LOGGER.warn("Missing configuration: " + e.getMessage());
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
		while (rbtDaemonManager != null && rbtDaemonManager.isAlive()) {

			try {
				copyRemoteFilesToLocal();
				processFiles();
				int sleepInterval = Integer
						.parseInt(getParamAsString("SLEEP_INTERVAL_MINUTES","5"));
				LOGGER.info("RBIdeaRetailProcessor Thread Sleeping for "
						+ sleepInterval + " minutes............");
				Thread.sleep(sleepInterval * 60 * 1000);
			} catch (Exception e) {
				LOGGER.error("Exception while running RBTIdeaRetailProcessor: "
						+ e.getMessage(), e);
			}
		}
	}

	/**
	 * Makes FTP call, get the files from remote system and copy those to local
	 * file system.
	 * 
	 * @param remoteFileName
	 * @return true/false
	 * @throws IOException
	 */
	private void copyRemoteFilesToLocal() {
		FileOutputStream fos = null;
		FTPClient ftpClient = null;
		FTPFile[] list = null;
		try {
			try {
				ftpClient = getFtpClient();
				LOGGER.debug(" FTPClient :" + ftpClient
						+ "Getting the list of files under: " + remoteDirPath);
				list = ftpClient.listFiles(remoteDirPath);
				LOGGER.debug("Total files in FTP Server: " + list.length);
				for (FTPFile file : list) {
					if (file.isFile()) {
						String fileName = file.getName();
						if (!isAlreadyProcessed(fileName)) {
							fos = new FileOutputStream(
									localDirPath.concat(fileName));
							boolean isCopied = ftpClient.retrieveFile(
									remoteDirPath.concat(fileName), fos);
							LOGGER.debug("File: " + fileName + " isCopied: "
									+ isCopied);
						} else {
							LOGGER.debug(" File: " + fileName
									+ " is already copied");
						}
					}
				}
				LOGGER.debug("Files copy process is done");
			} catch (Exception e) {
				LOGGER.error("Unable to retrieve the file from FTP."
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

	}

	/**
	 * Load all the files from the specified local directory path and check
	 * weather it is already processed or not. If the file is not processed,
	 * process it otherwise it will ignore.
	 */
	private void processFiles() {
		try {
			File localFiles = new File(localDirPath);
			if (localFiles.isDirectory()) {
				File[] files = localFiles.listFiles();
				for (File file : files) {
					// File is not processed
					if (!file.getName().contains("processed")) {
						boolean processed = processFile(file);
						if (processed) {
							renameFile(file);
						}
					}
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
	}

	/**
	 * Process the given file. Read the MSISDNs line by line and tries to
	 * activate/update/extend subscription.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private boolean processFile(File file) throws IOException {
		LOGGER.debug("Started processing file: " + file);
		FileInputStream inputStream = null;
		BufferedReader reader = null;
		boolean processed = false;
		try {
			inputStream = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = reader.readLine()) != null) {
				LOGGER.debug("File: " + file.getName() + ", contains: " + line);
				processMsisdn(line.trim());
			}
			processed = true;
		} catch (FileNotFoundException fnfe) {
			LOGGER.error("Error while fetching the file: " + fnfe.getMessage(),
					fnfe);
		} catch (IOException ioe) {
			LOGGER.error("Error while reading the line" + ioe.getMessage(), ioe);
		} finally {
			inputStream.close();
			reader.close();
		}
		LOGGER.info("File processed in successfully");
		return processed;
	}

	private void processMsisdn(String msisdn) {
		LOGGER.debug("Started processing msisdn: " + msisdn);

		if (!"".equals(msisdn)) {

			Subscriber subscriber = getSubscriber(msisdn);
			String subscriberCosID = subscriber.getCosID();
			String subscriptionClass = subscriber.getSubscriptionClass();

			// Proceed if the subscriber is not a TnB subscriber
			if (!isTnbSubscriber(subscriptionClass)) {

				if (isSubscriberActive(subscriber.getStatus())
						&& cosId.equals(subscriberCosID)) {

					// Extend the validity of the subscriber subscribed to
					// retail pack
					extendValidity(msisdn);
				} else {
					// Activate if the subscriber is not active or update if
					// subscriber is in some other pack
					activateOrUpdateSubscription(msisdn);
				}
			} else {
				LOGGER.warn("Subscriber: " + msisdn + " is TnbSubscriber");
			}
		} else {
			LOGGER.warn("msisdn is empty string");
		}
	}

	/**
	 * Returns false if SubscriptionClass is not configured as TNB subscription
	 * otherwise false.
	 * 
	 * @param subscriptionClass
	 * @return true or false
	 */
	private boolean isTnbSubscriber(String subscriptionClass) {
		boolean result = false;
		if (null != subscriptionClass && null != tnbSubscriptions
				&& tnbSubscriptions.length > 0) {
			LOGGER.debug(" tnbSubscriptions: " + tnbSubscriptions + " length: "
					+ tnbSubscriptions.length + ", subscriptionClass: "
					+ subscriptionClass);
			for (String tnbSubscription : tnbSubscriptions) {
				if (subscriptionClass.equals(tnbSubscription.trim())) {
					LOGGER.debug(" tnbSubscription: " + tnbSubscription
							+ " subscriptionClass: " + subscriptionClass
							+ " returns: true");
					result = true;
				}
			}
		}
		LOGGER.debug(" subscriptionClass: " + subscriptionClass + " returns: "
				+ result);
		return result;
	}

	/**
	 * Extend the subscription validity for the subscriber subscribed to retail
	 * pack. RBT server component send HTTP request to SubMgr for the base
	 * subscription validity extension, once it is success it will send another
	 * HTTP request to SubMgr for the song validity extension.
	 * 
	 * In BasicRBTProcessor update subscription, if the SubMgr response is success
	 * then update subscription end date in DB.
	 * 
	 * @param msisdn
	 */
	private void extendValidity(String msisdn) {

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				msisdn);
		/*
		 * Check for the configuration of the subscription class.If any
		 * configuration is found, set the configured value, otherwise set
		 * default value
		 */
		if (null != subscriptionClassForValidityExtension) {
			subscriptionRequest
					.setSubscriptionClass(subscriptionClassForValidityExtension);
		} else {
			subscriptionRequest.setSubscriptionClass(cosDetails
					.getSubscriptionClass());
		}
		subscriptionRequest.setMode(mode);
		subscriptionRequest.setInfo(WebServiceConstants.UPGRADE_VALIDITY);

		rbtClient.updateSubscription(subscriptionRequest);
		String response = subscriptionRequest.getResponse();
		LOGGER.debug(" Tried to extend validity of the subscriber: " + msisdn
				+ ", response: " + response);

		if (response.equalsIgnoreCase("SUCCESS")) {
			SubscriberStatus[] settings = RBTDBManager.getInstance()
					.getAllActiveSubscriberSettings(msisdn);
			if (settings != null) {
				SubscriberStatus setting = settings[settings.length - 1];

				SelectionRequest selectionRequest = new SelectionRequest(msisdn);
				/*
				 * Check for the charge class configuration.If any configuration
				 * is found, set the configured value, otherwise set default
				 * value.
				 */
				if (null != chargeClassForValidityExtension) {
					selectionRequest
							.setChargeClass(chargeClassForValidityExtension);
				} else {
					selectionRequest.setChargeClass(setting.classType());
				}
				selectionRequest.setRefID(setting.refID());
				rbtClient.upgradeSelectionValidity(selectionRequest);
				response = selectionRequest.getResponse();
				LOGGER.debug(" Tried to extend validity for the selection with" +
						" msisdn: "+ msisdn+ ", refID: "+ setting.refID()+
						", response: " + response);
			} else {
				LOGGER.debug(" No selections for the subscriber: " + msisdn);
			}
		}
	}

	/**
	 * Activate or update base subscription. If the subscriber is not active, it
	 * subscribes to retail pack or if the subscriber is already active, it
	 * upgrade the subscription pack.
	 * 
	 * @param msisdn
	 */
	private void activateOrUpdateSubscription(String msisdn) {
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				msisdn);
		subscriptionRequest.setRentalPack(cosDetails.getSubscriptionClass());
		subscriptionRequest.setCosID(Integer.parseInt(cosId));
		subscriptionRequest.setMode(mode);

		// activate or update subscription
		rbtClient.activateSubscriber(subscriptionRequest);
		String response = subscriptionRequest.getResponse();
		LOGGER.debug(" Tried to activate/update subscriber: " + msisdn
				+ ", response: " + response);
	}

	/**
	 * Returns true if the subscriber status is act_pending, active, locked,
	 * renewal_pending, grace, suspended
	 * 
	 * @param subscriberStatus
	 * @return true/ false
	 */
	public boolean isSubscriberActive(String subscriberStatus) {
		boolean result = false;
		if (subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
				|| subscriberStatus
						.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| subscriberStatus
						.equalsIgnoreCase(WebServiceConstants.LOCKED)
				|| subscriberStatus
						.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.GRACE)
				|| subscriberStatus
						.equalsIgnoreCase(WebServiceConstants.SUSPENDED)) {
			result = true;
		}
		LOGGER.debug(" SubscriberStatus: " + subscriberStatus + " result: "
				+ result);
		return result;
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
				+ ", cosId: " + subscriber.getCosID() + ", status: "
				+ subscriber.getStatus() + ", subscription class: "
				+ subscriber.getSubscriptionClass());
		return subscriber;
	}

	/**
	 * Rename the given file to processed file format and check it with the
	 * local directory path. Returns true if already exits otherwise false.
	 * 
	 * @param filename
	 * @return
	 */
	private boolean isAlreadyProcessed(String filename) {
		String name = getFileNameAsProcessed(filename);
		File file = new File(localDirPath.concat(name));
		boolean fileExists = file.exists();
		LOGGER.debug(" file: " + name + " isAlreadyProcessed: " + fileExists);
		return fileExists;

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
					+ " renamed: " + renamed);
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
	 * Returns the file name as processed ones. For ex: A.txt renamed to
	 * A_processed.txt
	 * 
	 * @param filename
	 * @return filname_processed.extension
	 */
	public String getFileNameAsProcessed(String filename) {
		int dot = filename.lastIndexOf(".");
		StringBuffer sb = new StringBuffer();
		sb.append(filename.substring(0, dot)).append("_processed")
				.append(filename.substring(dot, filename.length()));
		return sb.toString();
	}

	/**
	 * Create FTP client.
	 * 
	 * @return FTPClient
	 * @throws SocketException
	 * @throws IOException
	 */
	private FTPClient getFtpClient() throws SocketException, IOException {
		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(ftpServerIp, port);
		ftpClient.login(user, password);
		int reply = ftpClient.getReplyCode();
		if (FTPReply.isPositiveCompletion(reply)) {
			LOGGER.debug(" FtpClient connection is success");
		} else {
			throw new IOException("FTP connection is not established");
		}
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

	private String getParamAsString(String param, String defaultValue)
			throws Exception {
		String paramValue = null;
		try {
			paramValue = parametersCacheManager.getParameter(
					iRBTConstant.DAEMON, param, defaultValue).getValue();
		} catch (Exception e) {
			LOGGER.error("Unable to get " + param);
			throw e;
		}
		return paramValue;
	}
}
