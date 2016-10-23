package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.exception.InvaildCallDurationException;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.exception.InvalidCategoryException;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.exception.InvalidClipException;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.exception.InvalidSubscriberException;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCall;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallLog;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallPrismRetry;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallTxn;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.dao.RBTHibernateDao;
import com.onmobile.apps.ringbacktones.hunterFramework.management.HttpPerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitorFactory;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

/*
 * 1.	The primary job of new task/daemon is send an event charging request to SM for the called_id.
 * 2.	Before sending event charge request it will verify that the max_no_of_calls reached or not.
 * 3.	The max_no_of_calls will be configured in rbt_parameters table.
 * 4.	If executor framework threads are busy, in reject execution case it will insert into new table.
 *      New table will have CALLER_ID, CALLED_ID, TONE_ID, REQUEST_TIMESTAMP columns.
 * 5.	There will be one more thread will pick the entries from the new table and does the same job as in step 1.
 * 6.	And one more, third thread is required to clear the expired records. Expiry time will be configured in rbt_parameters table.
 */
public class RBTChargePerCallService {
	private static final String UNABLE_CALL_TO_SUB_MGR = "Unable to make http call to SubMgr.";
	private static final Logger logger = Logger
			.getLogger(RBTChargePerCallService.class);
	private static final Logger PPU_SM_TXN_LOG = Logger
			.getLogger("PpuSmTransactionLogger");
	private static final Logger TCP_PPU_TXN_LOG = Logger
			.getLogger("TcpPpuTransactionLogger");

	private static final RBTHibernateDao DAO = RBTHibernateDao.getInstance();

	private final int maxCalls;
	private RBTHttpClient rbtHttpClient = null;
	private final String url;
	private static final RBTDBManager rbtdbManager = RBTDBManager.getInstance();
	private static MemCachedClient mc = RBTCache.getMemCachedClient();
	private static int maxRetryCount;
	// RBT-14123:TataDocomo Changes.
	private static int callDurationIntervalMin = RBTParametersUtils
			.getParamAsInt("DAEMON", "PPU_CALL_DURATION_TO_ACCEPT", -2);
	public RBTChargePerCallService() {
		int connectionTimeout = RBTParametersUtils.getParamAsInt("DAEMON",
				"SM_CLIENT_CONNECTION_TIMEOUT_FOR_TCP_DAEMON", 6);
		int soTimeout = RBTParametersUtils.getParamAsInt("DAEMON",
				"SM_CLIENT_CONNECTION_SO_TIMEOUT_FOR_TCP_DAEMON", 6);
		maxCalls = RBTParametersUtils.getParamAsInt("DAEMON",
				"CHARGE_PER_CALL_MAX_CALLS_ALLOWED_FOR_CALLER", 5);
		url = RBTParametersUtils.getParamAsString("DAEMON",
				"SUB_MGR_URL_FOR_CHARGE_PER_CALL_SERVICE", null);
		maxRetryCount = Integer.valueOf(RBTParametersUtils.getParamAsInt("DAEMON",
				"CHARGE_PER_CALL_PRISM_MAX_RETRY_COUNT", 3));

		HttpPerformanceMonitor httpPerformanceMonitor = PerformanceMonitorFactory
				.newHttpPerformanceMonitor("TCP_DAEMON", "TCP_DAEMON_MONITOR",
						PerformanceDataType.LONG, "Milliseconds");
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setMaxTotalConnections(200);
		httpParameters.setMaxHostConnections(200);
		httpParameters.setConnectionTimeout(connectionTimeout * 1000);
		httpParameters.setSoTimeout(soTimeout * 1000);
		httpParameters.setHttpPerformanceMonitor(httpPerformanceMonitor);
		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	public void process(RBTChargePerCallTxn rbtChargePerCallTxn, String refId) {
		long stTime = System.currentTimeMillis();
		boolean isNotRetryRequest = (refId == null);
		/*
		 * In retry cases refId will not be null.
		 */
		if (refId == null) {
			refId = UUID.randomUUID().toString();
		}

		long retryCount = 0;

		RBTChargePerCallPrismRetry rbtChargePerCallPrismRetry = null;

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Processing rbtChargePerCall: "
						+ rbtChargePerCallTxn);
			}
			String callerId = rbtChargePerCallTxn.getCallerId();
			String calledId = rbtChargePerCallTxn.getCalledId();
			String wavFile = rbtChargePerCallTxn.getWavFile();
			Date calledTime = rbtChargePerCallTxn.getCalledTime();
			// RBT-14123: TataDocomo PPU changes
			short callDuration = rbtChargePerCallTxn.getCallDuration();
			if (callDurationIntervalMin > -1) {
				if (isNotRetryRequest) {
					validateCallDuration(callerId, callDuration);
				}
			}
			// Validating caller subscriber and clip
			checkCallerBlackListed(callerId);
			Subscriber calledSubscriber = validateAndGetCalledSubscriber(calledId);
			String clipId = null;
			String clipName = null;
			Category shuffleCategory = null;
			// RBT-14123:TataDocomo Changes.
			String clipPromoId = null;
			String movieName = null;
			String clipInfo = null;
			//for shuffle category coming in wavfile as S:catID
			// RBT-14123:TataDocomo Changes.
			
			
			if (wavFile != null && wavFile.startsWith("S:")) {
				shuffleCategory = getShuffleCatgeory(wavFile);
				if (shuffleCategory != null) {
					clipId = shuffleCategory.getCategoryId() + "";
					clipName = getEncodeName(shuffleCategory.getCategoryName());
					// RBT-14123:TataDocomo Changes.
					clipPromoId = shuffleCategory.getCategoryPromoId();
					movieName = getEncodeName(shuffleCategory.getCategoryName());
					clipInfo = getClipInfo(shuffleCategory.getCategoryInfo());
				}
			}

			if (shuffleCategory == null) {
				Clip clip = validateAndGetClip(wavFile);
				clipId = clip.getClipId() + "";
				clipName = getEncodeName(clip.getClipName());
				// RBT-14123:TataDocomo Changes.
				clipPromoId = clip.getClipPromoId();
				movieName = getEncodeName(clip.getAlbum());
				clipInfo = getClipInfo(clip.getClipInfo());
			}
			
			//getting a selection object to get mode from it.
			String selectionMode = getSelectionMode(calledId,wavFile);
			rbtChargePerCallPrismRetry = RBTHibernateDao.getInstance().getPrismRetryRecord(rbtChargePerCallTxn.getCalledId());
			retryCount = (null != rbtChargePerCallPrismRetry) ? rbtChargePerCallPrismRetry.getRetryCount() : 0;
			retryCount++;

			RBTChargePerCallLog chargePerCallLog = RBTChargePerCallLogAdaptor
					.convert(rbtChargePerCallTxn);
			int callsMade = getCallsMade(chargePerCallLog);
			if (isMaxCallsNotMade(callsMade)) {
				String chargingResponse = sendChargeRequest(calledId,
						clipName, refId, clipId, clipPromoId, movieName, clipInfo, callDuration, selectionMode);

				if (null == chargingResponse) {
					// connection failure, so retry should happen
					if (null == rbtChargePerCallPrismRetry) {
						rbtChargePerCallPrismRetry = new RBTChargePerCallPrismRetry(
								callerId, calledId,
								rbtChargePerCallTxn.getCalledTime(), wavFile,
								new Date(), refId, 0L);
						rbtChargePerCallPrismRetry.setCallDuration(callDuration);
						boolean insertionStatus = RBTHibernateDao.getInstance().save(
								rbtChargePerCallPrismRetry);
						logger.debug("Adding request to retry table. calledId: " + calledId + " refId:" +  refId + ". Insertion status: " + insertionStatus);
					} else {
						if (retryCount == maxRetryCount) {
							// to be deleted.
							logger.debug("maxRetryCount reached and hence, removing the request entry from retry table. calledId: " + calledId + " refId:" +  refId);
							// RBT-14123:TataDocomo Changes.
							TCP_PPU_TXN_LOG.info(rbtChargePerCallTxn
									.getLogString(false,
											"maxRetryCount reached", callDurationIntervalMin, false));
							deletePrismRetryRecord(rbtChargePerCallTxn, rbtChargePerCallPrismRetry.getRefId(), "maxRetryCount reached");
						} else {
							if (isNotRetryRequest) {
								logger.debug("As this is not a retry request request, retryCount is reinitialized to 0. calledId: " + calledId + " refId:" +  refId);
								retryCount = 0;
							}
							logger.debug("Updating exising request entry in retry table. calledId: " + calledId + " refId:" +  refId);
							rbtChargePerCallPrismRetry.setRefId(refId);
							rbtChargePerCallPrismRetry.setRetryCount(retryCount);
							rbtChargePerCallPrismRetry.setRetryTime(new Date());
							rbtChargePerCallPrismRetry.setCallerId(callerId);
							rbtChargePerCallPrismRetry.setCalledTime(calledTime);
							rbtChargePerCallPrismRetry.setCallDuration(callDuration);
							RBTHibernateDao.getInstance().update(rbtChargePerCallPrismRetry);
						}
					}
				} else if ("SUCCESS".equalsIgnoreCase(chargingResponse)) {
					// connection and response are success, so update the
					// request
					updateCallsMade(chargePerCallLog);
					if (rbtChargePerCallPrismRetry != null) {
						logger.debug("Success response. Hence removing the request entry from retry table. calledId: " + calledId + " refId:" +  rbtChargePerCallPrismRetry.getRefId());
						deletePrismRetryRecord(rbtChargePerCallTxn, rbtChargePerCallPrismRetry.getRefId(), "Succesfully charged");
					}
				} else {
					// connection is success but response is failure, suspend
					// the subscriber.
					suspendSubscriber(rbtChargePerCallTxn, calledSubscriber);
					if(rbtChargePerCallPrismRetry != null) {
						logger.debug("Subscriber suspended. Hence removing the request entry from retry table. calledId: " + calledId + " refId:" +  rbtChargePerCallPrismRetry.getRefId());
						deletePrismRetryRecord(rbtChargePerCallTxn, rbtChargePerCallPrismRetry.getRefId(), "Subscriber suspended");
					}
				}
			} else {
				logger.warn("Chargin not happened as the caller has called maximum times."
						+ " rbtChargePerCall: " + rbtChargePerCallTxn);
				// RBT-14123:TataDocomo Changes.
				TCP_PPU_TXN_LOG.info(rbtChargePerCallTxn.getLogString(false,
						"Max calls made", callDurationIntervalMin, false));
				if(rbtChargePerCallPrismRetry != null) {
					logger.debug("Max calls made. Hence removing the request entry from retry table. calledId: " + calledId + " refId:" +  refId);
					deletePrismRetryRecord(rbtChargePerCallTxn, rbtChargePerCallPrismRetry.getRefId(), "Max calls made");
				}
			}
		} catch (InvaildCallDurationException cdle) {// RBT-14123:TataDocomo Changes.
			String errMsg = cdle.getMessage();
			logger.error("Failed to process charging request."
					+ " InvaildCallDurationException: " + errMsg);
			TCP_PPU_TXN_LOG.info(rbtChargePerCallTxn.getLogString(false,
					errMsg, callDurationIntervalMin, false));
		} catch (InvalidSubscriberException nrse) {
			String errMsg = nrse.getMessage();
			logger.error("Failed to process charging request."
					+ " InvalidSubscriberException: " + errMsg);
			// RBT-14123:TataDocomo Changes.
			TCP_PPU_TXN_LOG.info(rbtChargePerCallTxn.getLogString(false,
					errMsg, callDurationIntervalMin, false));
			if (rbtChargePerCallPrismRetry != null) {
				deletePrismRetryRecord(rbtChargePerCallTxn, rbtChargePerCallPrismRetry.getRefId(), nrse.getMessage());
			}

		} catch (InvalidClipException cnfe) {
			logger.error("Failed to process charging request."
					+ " InvalidClipException: " + cnfe.getMessage());
			// RBT-14123:TataDocomo Changes.
			TCP_PPU_TXN_LOG.info(rbtChargePerCallTxn.getLogString(false,
					"Invalid Clip", callDurationIntervalMin, false));
			if (rbtChargePerCallPrismRetry != null) {
				deletePrismRetryRecord(rbtChargePerCallTxn, rbtChargePerCallPrismRetry.getRefId(), cnfe.getMessage());
			}
		} catch (Exception e) {
			logger.error(
					"At the moment failed to process charging request: "
							+ rbtChargePerCallTxn + ". Error message: "
							+ e.getMessage(), e);
			// RBT-14123:TataDocomo Changes.
			TCP_PPU_TXN_LOG.info(rbtChargePerCallTxn.getLogString(false,
					e.getMessage(), callDurationIntervalMin, false));
			if (rbtChargePerCallPrismRetry != null) {
				deletePrismRetryRecord(rbtChargePerCallTxn, rbtChargePerCallPrismRetry.getRefId(), e.getMessage());
			}
		}
		long enTime = System.currentTimeMillis();
		logger.info("Processed request: " + rbtChargePerCallTxn + ", in "
				+ (enTime - stTime) + " milliseconds ");

	}
	/**
	 * Method to delete a prism retry record. Writes transaction log for the same. when deletion happens.
	 * @param rbtChargePerCallTxn
	 * @param refId
	 * @param reason
	 */

	private void deletePrismRetryRecord(
			RBTChargePerCallTxn rbtChargePerCallTxn, String refId, String reason) {
		boolean isDeleted = RBTHibernateDao.getInstance().deleteRBTChargePerCallPrismRetry(rbtChargePerCallTxn.getCalledId(), refId);
		if (isDeleted) {
			PPU_SM_TXN_LOG.info(rbtChargePerCallTxn.getLogString(false, reason,
					callDurationIntervalMin, false));
		}
	}

	/**
	 * Suspend subscriber in case of failure prism responses. Failure responses are the responses other than SUCCESS.
	 *  
	 * @param rbtChargePerCallTxn
	 * @param calledSubscriber
	 */
	private void suspendSubscriber(
			RBTChargePerCallTxn rbtChargePerCallTxn, Subscriber calledSubscriber) {
		if (logger.isDebugEnabled()) {
			logger.debug("Successfully made prism request, since the response is failure suspending subscriber: "
					+ rbtChargePerCallTxn.getCalledId());
		}
		String response = rbtdbManager.suspendSubscription(rbtChargePerCallTxn.getCalledId(),
				calledSubscriber.subscriptionClass(), true);
		if (logger.isDebugEnabled()) {
			logger.debug("Suspended subscriber: "
					+ rbtChargePerCallTxn.getCalledId() + ", response: "
					+ response);
		}
	}

	/**
	 * Verifies the caller is black listed or not. All black listed subscribers
	 * will be uploaded into memcache during initialization.
	 * 
	 * @param callerId
	 * @throws InvalidSubscriberException
	 */
	private void checkCallerBlackListed(String callerId)
			throws InvalidSubscriberException {
		boolean isBlackListed = false;

		// Blacklisted when the subscriber is present in memcache.
		String key = callerId.concat("_BLACKLISTED");
		Object value = mc.get(key);
		
		if(value != null) {
			isBlackListed = true;
		}
		
		String ppuMobileLengthConfig = RBTContentJarParameters.getInstance()
				.getParameter("PPU_MSISDN_PREFIX_LENGTH");
		if(ppuMobileLengthConfig != null && !isBlackListed) {
			String[] ppuMobileArr = ppuMobileLengthConfig.split(",");
			for (int i = 0; i < ppuMobileArr.length; i++) {
				int tempMobileLength = Integer.parseInt(ppuMobileArr[i]);
				String tempCallerId = callerId.substring(0, tempMobileLength);
				key = tempCallerId.concat("_BLACKLISTED");
				value = mc.get(key);
				if (null != value) {
					isBlackListed = true;
					break;
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Checking subscriber is blacklisted in memcache."
					+ " key: " + key + ", value: " + value
					+ ", isBlackListed: " + isBlackListed);
		}
		if (isBlackListed) {
			throw new InvalidSubscriberException(
					"Subscriber is blacklisted. Subscriber: " + callerId);
		}
	}
	// RBT-14123:TataDocomo Changes.
	public void validateCallDuration(String callerId, short callDuration)
			throws InvaildCallDurationException {
		if (callDuration < callDurationIntervalMin) {
			throw new InvaildCallDurationException(
					"Call Duration for the subscriber: "
							+ callerId
							+ " is less than the PPU_CALL_DURATION_TO_ACCEPT configuration value so rejecting the request");
		}
	}
	// RBT-14123:TataDocomo Changes.
	public String sendChargeRequest(String calledId, String clipName,
			String refId, String clipId, String clipPromoId, String movieName,
			String clipInfo, short callDuration,String mode)
			throws InvalidSubscriberException, InvalidClipException {
		if (logger.isDebugEnabled()) {
			logger.debug("Sending charging request to SM. callerId: "
					+ calledId);
		}

		String newUrl = null;
		String prismResponse = null;
		String reason = null;
		long st = System.currentTimeMillis();
		long en = 0L;
		HttpResponse httpResponse = null;
		try {// RBT-14123:TataDocomo Changes.
			newUrl = url
					.replace("subscriber_id", calledId)
					.replace("rclip_name", clipName)
					.replace("rrefid", refId)
					.replace("rclip_id", clipId)
					.replace("rclip_promoid",
							(clipPromoId != null) ? clipPromoId : "")
					.replace("rmovie_name",
							(movieName != null) ? movieName : "")
					.replace("rclip_info", (clipInfo != null) ? clipInfo : "")
					.replace("rmode", (mode != null) ? mode : "");
			
			logger.debug("Sniffer Log: url to prism"+ newUrl);
			httpResponse = rbtHttpClient.makeRequestByGet(newUrl, null);
			String responseStr = httpResponse.getResponse();
			prismResponse = parsePrismResponse(responseStr, "status");

		} catch (HttpException he) {
			reason = he.getMessage();
			logger.error(
					UNABLE_CALL_TO_SUB_MGR + " HttpException: "
							+ he.getMessage(), he);
		} catch (IOException ioe) {
			reason = ioe.getMessage();
			logger.error(UNABLE_CALL_TO_SUB_MGR + newUrl + " IOException: "
					+ ioe.getMessage(), ioe);
		} catch (Exception e) {
			reason = e.getMessage();
			logger.error(
					UNABLE_CALL_TO_SUB_MGR + " Exception: " + e.getMessage(), e);
		} finally {
			en = System.currentTimeMillis();
			
			if (logger.isDebugEnabled()) {
				logger.debug("Got the response for charging "
						+ "request, returning: " + prismResponse
						+ ", httpResponse: " + httpResponse + ", calledId: "
						+ calledId + ", processed in: " + (en - st)
						+ " milliseconds");
			}
			
			if (null != httpResponse) {
				String errorMsg = "PPU_EVENT," + newUrl + ","
						+ httpResponse.getResponseCode() + "|"
						+ httpResponse.getResponse();
				if (callDurationIntervalMin > -1) {
					errorMsg += "|CallDuration:" + callDuration;
				}
				errorMsg += "," + st + "," + (en - st);
				PPU_SM_TXN_LOG.info(errorMsg);
			} else {
				String errorMsg = "PPU_EVENT," + newUrl + ","
						+ "CONNECTION_FAILURE|" + reason;
				if (callDurationIntervalMin > -1) {
					errorMsg += "|CallDuration:" + callDuration;
				}
				errorMsg += "," + st + "," + (en - st);
				PPU_SM_TXN_LOG.info(errorMsg);
			}
		}
		return prismResponse;
	}

	/**
	 * Validating caller
	 * 
	 * @param calledId
	 * @throws InvalidSubscriberException
	 */
	private Subscriber validateAndGetCalledSubscriber(String calledId)
			throws InvalidSubscriberException {

		Subscriber subscriber = rbtdbManager.getSubscriber(calledId);
		if (null == subscriber) {
			throw new InvalidSubscriberException("Non RBT Subscriber: "
					+ calledId);
		}

		// validating subscriber status
		String status = subscriber.subYes();
		if ("D".equals(status) || "P".equals(status) || "F".equals(status)
				|| "x".equals(status) || "X".equals(status)) {
			throw new InvalidSubscriberException(
					"Deactivation in Progress. Subscriber: " + calledId);
		}

		return subscriber;
	}
	
	private String getSelectionMode(String subscriberId, String wavFileName)
			throws InvalidSubscriberException {

		SubscriberStatus selection = rbtdbManager.getSelection(subscriberId, wavFileName);
		if (null == selection) {
			return null;
		}
		// validating subscriber status
		String status = selection.selectedBy();
		if(status == null)
			return "voice";
		return status;
	}

	private Clip validateAndGetClip(String wavFile) throws InvalidClipException {
		Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(
				wavFile);
		if (null == clip) {
			throw new InvalidClipException("Clip not found: " + wavFile);
		}
		return clip;
	}

	private Category getShuffleCatgeory(String wavFile) throws InvalidCategoryException {
		try {
			String catgeoryId = wavFile.substring(1);
			int shuffleCatId = Integer.parseInt(catgeoryId);
			Category category = RBTCacheManager.getInstance().getCategory(shuffleCatId);
			if (category != null) {
				boolean isShuffle = Utility.isShuffleCategory(category.getCategoryTpe());
				if (isShuffle) {
					return category;
				}
			}
		} catch (Exception ex) {
			logger.info("Exception While getting category for shuffle");
		}
		return null;
	}
	
	public int getCallsMade(RBTChargePerCallLog rbtChargePerCallLog) {
		RBTChargePerCallLog r = (RBTChargePerCallLog) DAO
				.get(rbtChargePerCallLog);
		int callsmade = 0;
		if (null != r) {
			callsmade = r.getNoOfCallsMade();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Calls made:  " + callsmade);
		}
		return callsmade;
	}

	/**
	 * Update number of calls made in RBTChargePerCallLog
	 * 
	 * @param rbtChargePerCallLog
	 */
	public void updateCallsMade(RBTChargePerCallLog rbtChargePerCallLog) {
		try {
			int i = update(rbtChargePerCallLog);
			if (i == 0) {
				if (!save(rbtChargePerCallLog)) {
					logger.error("Updating again. rbtChargePerCallLog: "
							+ rbtChargePerCallLog);
					update(rbtChargePerCallLog);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to update calls made. " + rbtChargePerCallLog,
					e);
		}
	}

	public boolean save(RBTChargePerCall chargePerCall) {
		return DAO.save(chargePerCall);
	}

	public void delete(RBTChargePerCall chargePerCall) {
		DAO.delete(chargePerCall);
	}

	public RBTChargePerCall get(RBTChargePerCall chargePerCall) {
		return DAO.get(chargePerCall);
	}

	public int update(RBTChargePerCallLog chargePerCall) {
		return DAO.update(chargePerCall);
	}

	public boolean isMaxCallsNotMade(int callsMade) {
		return callsMade < maxCalls;
	}

	private String parsePrismResponse(String prismResponse, String ele) {

		String response = null;
		try {
			Document document = createDocument(prismResponse);
			response = document.getElementsByTagName(ele).item(0)
					.getFirstChild().getNodeValue();
		} catch (ParserConfigurationException e) {
			logger.error(
					"Unable to parse. ParserConfigurationException: "
							+ e.getMessage(), e);
		} catch (SAXException e) {
			logger.error("Unable to parse. SAXException: " + e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Unable to parse. IOException: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Unable to parse. Exception: " + e.getMessage(), e);
		}
		logger.debug("Parsed the response, returning: " + response
				+ ", prismResponse: " + prismResponse);
		return response;
	}

	private static Document createDocument(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		InputStream is = new ByteArrayInputStream(xml.getBytes());

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document mnpResponse = builder.parse(is);
		return mnpResponse;
	}
	// RBT-14123:TataDocomo Changes.
	private String getEncodeName(String movieName) {
		if(movieName != null && movieName.length() > 20)
			movieName = movieName.substring(0, 20);
		movieName = (movieName != null ? movieName.replaceAll("&", "%26") : movieName);
		return movieName;
	}
	// RBT-14123:TataDocomo Changes.
	private String getClipInfo(String clipOrCatInfo) {
		if (clipOrCatInfo != null) {
			clipOrCatInfo = clipOrCatInfo.replaceAll("=", ":");
			clipOrCatInfo = clipOrCatInfo.replaceAll("&", " ");
		}
		return clipOrCatInfo;
	}

	public static void main(String[] args) {
		RBTChargePerCallService service = new RBTChargePerCallService();
		RBTChargePerCallLog rbtChargePerCallLog = new RBTChargePerCallLog(
				"9986010002", "9008495777", new Date());
		service.updateCallsMade(rbtChargePerCallLog);
	}
}
