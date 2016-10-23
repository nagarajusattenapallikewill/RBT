/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.Gatherer.RBTCopyProcessor;
import com.onmobile.apps.ringbacktones.common.RBTHttpUtils;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author rajesh.karavadi
 * @Since Sep 16, 2013
 */
public class RBTConsentExpiredProcessor extends Thread {

	private static final String ERROR_STATUS = "4";
    //private static final String RRBT_CONSENT_XPIRED_STATUS = "5";
    
	private static Logger LOGGER = Logger
			.getLogger(RBTConsentExpiredProcessor.class);
    
	private RBTDaemonManager rbtDaemonManager = null;

	private RBTDBManager rbtDBManager = null;

	private String url = null;
	private boolean isUseProxy = false;
	private String proxyHostname = null;
	private int proxyPort = 80;
	private int connectionTimeout = 6000;
	private int connectionSoTimeout = 6000;
	private String expiredHours;
	private int limit;
	private String mode;
	private String countryPrefix;
	private int sleepInterval;
    private boolean isRRBTSuppotForConsentXpired = false;
    
	public RBTConsentExpiredProcessor(RBTDaemonManager rbtDaemonManager) {

		this.rbtDaemonManager = rbtDaemonManager;

		rbtDBManager = RBTDBManager.getInstance();

		url = RBTParametersUtils.getParamAsString("DAEMON",
				"CONSENT_EXPIRED_URL", null);
		isUseProxy = RBTParametersUtils.getParamAsBoolean("DAEMON",
				"CONSENT_EXPIRED_URL_IS_USE_PROXY", "false");
		proxyHostname = RBTParametersUtils.getParamAsString("DAEMON",
				"CONSENT_EXPIRED_URL_PROXY_HOST", null);
		proxyPort = RBTParametersUtils.getParamAsInt("DAEMON", "CONSENT_URL_PROXY_PORT",
				80);
		connectionTimeout = RBTParametersUtils.getParamAsInt("DAEMON",
				"CONSENT_EXPIRED_URL_CONNECTION_TIMEOUT", 6000);
		connectionSoTimeout = RBTParametersUtils.getParamAsInt("DAEMON",
				"CONSENT_EXPIRED_URL_SO_CONNECTION_TIMEOUT", 6000);
		countryPrefix = RBTParametersUtils.getParamAsString("COMMON",
				"COUNTRY_PREFIX", "91");
		expiredHours = RBTParametersUtils.getParamAsString("DAEMON",
				"CONSENT_EXPIRED_HOURS_BEFORE_CONFIRMATION", "2");
		limit = RBTParametersUtils.getParamAsInt("DAEMON",
				"CONSENT_EXPIRED_FETCH_LIMIT", 50);
		String modeStr = RBTParametersUtils.getParamAsString("DAEMON",
				"CONSENT_EXPIRED_MODE", null);
		mode = splitAndAppendSingleQuotes(modeStr);
		
		sleepInterval = RBTParametersUtils.getParamAsInt("DAEMON","SLEEP_INTERVAL_MINUTES", 5);

		isRRBTSuppotForConsentXpired = RBTParametersUtils.getParamAsBoolean(
				"DAEMON", "RRBT_SUPPORT_FOR_CONSENT_EXPIRED", "FALSE");
	    
		LOGGER.info("RBTConsentExpiredProcessor Intialized successfully");
	}

	public void run() {

		LOGGER.info("Starting RBTConsentExpiredProcessor. rbtDaemonManager: "
				+ rbtDaemonManager);
		while (rbtDaemonManager != null && rbtDaemonManager.isAlive()) {
			try {

				if (null == url) {
					LOGGER.error("Unable to hit request, CONSENT_EXPIRED_URL"
							+ " is not configured");
					throw new Exception("CONSENT_EXPIRED_URL is not configured");
				}
				if (null == mode) {
					LOGGER.error("Unable to hit request, CONSENT_EXPIRED_MODE"
							+ " is not configured");
					throw new Exception("CONSENT_EXPIRED_MODE is not configured");
				}
				
				String consentStatus = "1";
				int startingFrom = 0;
				List<DoubleConfirmationRequestBean> list = rbtDBManager
						.getExpiredConsentRecords(expiredHours, consentStatus, startingFrom, limit, mode);
				
				while (null != list && !list.isEmpty()) {

					int size = list.size();
					LOGGER.info("Processing expired consent records list size: "
							+ size);
                   // List<String> alreadySentRequestForRRBTList = new ArrayList<String>();
					for (DoubleConfirmationRequestBean bean : list) {
						try {
							boolean isProcessed = false;
							if (isRRBTSuppotForConsentXpired) {
								String xtraInfo = bean.getExtraInfo();
								Map<String,String> xtraInfoMap = DBUtility.getAttributeMapFromXML(xtraInfo);
								if ((xtraInfoMap != null && xtraInfoMap.containsKey("rrbt_activated"))) {
									continue;
								}
								isProcessed = processExpiredRecordForRRBT(bean);
								if (isProcessed) {
									//updateRecordStatus(bean,RRBT_CONSENT_XPIRED_STATUS);
									if(xtraInfoMap == null){
										xtraInfoMap = new HashMap<String,String>();
									}
									xtraInfoMap.put("rrbt_activated", "true");
									xtraInfo = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
									// RBT-14675- Tata Docomo | Instead of
									// Consent ID we are populating Transaction
									// Id in system
									rbtDBManager.updateConsentExtrInfo(
											bean.getSubscriberID(),
											bean.getTransId(), xtraInfo, null,
											consentStatus);
								}
							} else {
								processExpiredRecord(bean);
							}
						} catch (Exception e) {
							LOGGER.error("Exception while processing expired. "
									+ "transId: " + bean.getTransId()
									+ ", Exception: " + e.getMessage(), e);
						} catch (Throwable e) {
							LOGGER.error("Exception while processing expired. "
									+ "transId: " + bean.getTransId()
									+ ", Throwable: " + e.getMessage(), e);
						}
					}

					LOGGER.debug("Processed expired records: " + size
							+ ", and fetching again");
					startingFrom = startingFrom + limit;
					list = rbtDBManager.getExpiredConsentRecords(expiredHours,
							consentStatus, startingFrom, limit, mode);
					LOGGER.debug("Processed expired records: " + size
							+ ", again fetched expired records size: "
							+ list.size());

				}
				LOGGER.info("No pending expired records found. fetch list is empty");

			} catch (Exception e) {
				LOGGER.error("Unable to process expired content. Exception: "
						+ e.getMessage(), e);
			} catch (Throwable e) {
				LOGGER.error("Unable to process expired content. Throwable: "
						+ e.getMessage(), e);
			}

			LOGGER.info("Sleeping " + this.getName() + " for " + sleepInterval
					+ " minutes............");
			try {
				Thread.sleep(sleepInterval * 60 * 1000);
			} catch (InterruptedException e) {
				LOGGER.error("Unable to process expired content. Exception: "
						+ e.getMessage(), e);
			} catch (Throwable e) {
				LOGGER.error("Unable to process expired content. Throwable: "
						+ e.getMessage(), e);
			}
		}
		LOGGER.info("Processed expired content records.");
	}

	private boolean hitUrl(File file) throws HttpException, IOException {
		HttpResponse httpResponse = RBTHttpUtils.makeHttpPostRequest(url,
				isUseProxy, proxyHostname, proxyPort, connectionTimeout,
				connectionSoTimeout, file);
		if (httpResponse != null && httpResponse.getResponseCode() == 200) {
			LOGGER.debug("Returning true, successfully hit url: " + url
					+ ", response code success," + " responseCode: "
					+ httpResponse.getResponseCode());
			return true;
		}

		LOGGER.info("Failed to hit url: " + url + ", response: "
				+ httpResponse);
		return false;
	}

	private String constructXml(DoubleConfirmationRequestBean bean, Subscriber subscriber, DoubleConfirmationRequestBean selBeanInCombo) {

		if (null == bean) {
			LOGGER.error("Unable to construct XML from null bean");
			return null;
		}

		org.w3c.dom.Document document = XMLUtils.createDocument();

		String xml = null;
		// Create <rbt></rbt> tag
		Element rbt = XMLUtils.createElement(document, "rbt");
		// Create <timestamp> tag and append to <rbt> tag.
		// <rbt><timestamp time="20130816123034"></timestamp></rbt>
		XMLUtils.createElementAndSetAttribute(document, rbt, "timestamp",
				"time", new Date().toString());

		String subscriberId = countryPrefix.concat(bean.getSubscriberID());
		String requestTime = String.valueOf(bean.getRequestTime());

		// Check the subscriber status
		String status = (null != subscriber) ? subscriber.getStatus() : "";
		String subStatus = null;

		if ( WebServiceConstants.ACTIVE.equalsIgnoreCase(status)) {
			subStatus = WebServiceConstants.ACTIVE;
		}else if(WebServiceConstants.ACT_PENDING.equalsIgnoreCase(status)
				|| WebServiceConstants.ACT_ERROR.equalsIgnoreCase(status)){
			subStatus = WebServiceConstants.ACT_PENDING;
		}else if(WebServiceConstants.DEACT_PENDING.equalsIgnoreCase(status)
				|| WebServiceConstants.DEACT_ERROR.equalsIgnoreCase(status)){
			subStatus = WebServiceConstants.DEACT_PENDING;
		}else if(WebServiceConstants.SUSPENDED.equalsIgnoreCase(status)){
			subStatus = WebServiceConstants.SUSPENDED;
		}else if(WebServiceConstants.GRACE.equalsIgnoreCase(status)){
			subStatus = WebServiceConstants.GRACE;
		}else {
			subStatus = WebServiceConstants.DEACTIVE;
		}
		
		
		Integer clipId = bean.getClipID();
		String mode = bean.getMode();
		String promoId = null;
		String rbtWavFile = bean.getWavFileName();
		Clip clip = null;
		
		String serviceId = null;
		String classString = null;

		boolean isActAndSelRequest = false;
		boolean isActRequest = false;
		boolean isSelRequest = true;
		String chargeClass = bean.getClassType();
		String subClass = bean.getSubscriptionClass();
		Category category = null;
		Integer categoryId  = bean.getCategoryID();
		if(bean.getRequestType().equalsIgnoreCase("ACT") || bean.getRequestType().equalsIgnoreCase("UPGRADE")) {
			isActRequest = true;
			if (selBeanInCombo != null) {
				isActAndSelRequest = true;
				LOGGER.info("combo sel bean found = "+selBeanInCombo);
				chargeClass = selBeanInCombo.getClassType();
				rbtWavFile = selBeanInCombo.getWavFileName();
				categoryId = selBeanInCombo.getCategoryID();
				LOGGER.info("Combo Charge class = "+chargeClass);
			}
		}
		if(bean.getRequestType().equalsIgnoreCase("SEL")) {
			isSelRequest = true;
			subClass = subscriber.getSubscriptionClass();
		}
		
		if (bean.getRequestType() == null || !(bean.getRequestType().equalsIgnoreCase("ACT") || bean.getRequestType().equalsIgnoreCase("UPGRADE")) || isActAndSelRequest) {
			if (rbtWavFile != null) {
				clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(rbtWavFile);
				// Fetch clip by clip id file, if clip is not found by
				// clipWavFile
				if (null == clip) {
					clip = RBTCacheManager.getInstance().getClip(clipId);
				}
			} else if (null != clipId) {
				clip = RBTCacheManager.getInstance().getClip(clipId);
				if (null == clip) {
					clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(rbtWavFile);
				}
			}
			// Modified for VD-107130
			if (categoryId != null) {
				category = RBTCacheManager.getInstance()
						.getCategory(categoryId);
			}
		}
		// Modified for VD-107130
		if (null != category
				&& Utility.isShuffleCategory(category.getCategoryTpe())) {
			promoId = category.getCategoryPromoId();
		} else if (null != clip) {
			promoId = clip.getClipPromoId();
		} else {
			LOGGER.warn("Since clip is null, setting promoid as empty");
		}
		// Modifications ended for VD-107130
		serviceId = DoubleConfirmationConsentPushThread
				.getServiceValue("SERVICE_ID",
						subClass, chargeClass,
						subscriber.getCircleID(), isActRequest, isSelRequest,
						isActAndSelRequest);
		classString = DoubleConfirmationConsentPushThread
				.getServiceValue("SERVICE_CLASS",
						subClass, chargeClass,
						subscriber.getCircleID(), isActRequest, isSelRequest,
						isActAndSelRequest);
		Map<String, String> attribs = new HashMap<String, String>();

		attribs.put("msisdn", subscriberId);
		attribs.put("status", subStatus);
		attribs.put("time", requestTime);
		// pass promo id and mode.
		if (promoId != null) {
			attribs.put("promoid", promoId);
		}
		attribs.put("mode", mode);
		if (serviceId == null) {
			serviceId = "";
		}
		if (classString == null) {
			classString = "";
		}
		attribs.put("serviceId", serviceId);
		attribs.put("class", classString);
		attribs.put("circle", subscriber.getCircleID());

		// <rbt>
		// <timestamp time="20130816123034"></timestamp>
		// <request msisdn="9986030880" status="active"></request>
		// </rbt>
		XMLUtils.createElementAndSetAttributes(document, rbt, "request",
				attribs);

		xml = XMLUtils.getStringFromDocument(document);

		LOGGER.info("Constructed XML successfully. xml: " + xml);
		return xml;
	}

	private boolean updateRecordStatus(DoubleConfirmationRequestBean bean,String status) {
		boolean isUpdated = false;
		try {
			LOGGER.debug("Updating consent status for subscriberId: "
					+ bean.getSubscriberID() + ", transId: "
					+ bean.getTransId() +", statuc = "+status);

			isUpdated = rbtDBManager.updateConsentStatus(
					bean.getSubscriberID(), bean.getTransId(), status);
			LOGGER.info("Updated consent status, isUpdated: " + isUpdated);
			return isUpdated;
		} catch (Exception e) {
			LOGGER.error(
					"Unable to update expired content. Exception: "
							+ e.getMessage(), e);
		}
		return false;
	}

	private boolean processExpiredRecord(DoubleConfirmationRequestBean bean)
			throws IOException {
		boolean isProcessed = false;

		// Get the subscriber from database
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				bean.getSubscriberID());
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(
				rbtDetailsRequest);
		if (bean.getRequestType().equals("SEL") && !RBTCopyProcessor.isSubActive(subscriber)) {
			LOGGER.info("Processed expired record. transId: " + bean.getTransId()
					+ ", subscriberId: " + bean.getSubscriberID()
					+ ", isProcessed: " + isProcessed + ", selection record belongs to a combo request. Returning without processing.");

		} else {
			DoubleConfirmationRequestBean selBeanInCombo = null;
			Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(bean.getExtraInfo());
			if(extraInfoMap != null && extraInfoMap.containsKey("TRANS_ID")) {
				String selTrasId = extraInfoMap.get("TRANS_ID");
				List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = RBTDBManager
						.getInstance()
						.getDoubleConfirmationRequestBeanForStatus(null, selTrasId,
								bean.getSubscriberID(), null, true);
				if (doubleConfirmReqBeans != null && doubleConfirmReqBeans.size() > 0) {
					selBeanInCombo = doubleConfirmReqBeans.get(0);
				}
			}
			if (bean.getRequestType().equalsIgnoreCase("SEL") && selBeanInCombo != null && selBeanInCombo.getRequestType().equalsIgnoreCase("UPGRADE")) {
				return isProcessed;
			}
			
			String xml = constructXml(bean, subscriber, selBeanInCombo);

			if(null != xml) {
				File file = createAndGetFile(xml, "temp", "txt");

				isProcessed = hitUrl(file);
			}

			if (isProcessed) {
				updateRecordStatus(bean,ERROR_STATUS);
				if (selBeanInCombo != null) {
					updateRecordStatus(selBeanInCombo, ERROR_STATUS);
				}
			}
			LOGGER.info("Processed expired record. transId: " + bean.getTransId()
					+ ", subscriberId: " + bean.getSubscriberID()
					+ ", isProcessed: " + isProcessed);
		}
		return isProcessed;
	}

	private boolean processExpiredRecordForRRBT(DoubleConfirmationRequestBean bean){
		boolean isProcessed = false;
		String rrbtSupportUrl = RBTParametersUtils.getParamAsString("DAEMON",
			        	"RRBT_URL_FOR_CONSENT_EXPIRED", null);
		if(rrbtSupportUrl == null){
			LOGGER.info("The Parameter RRBT_URL_FOR_CONSENT_EXPIRED is not configured");
			return false; 
		}
		HttpParameters httpParameters =new HttpParameters();
		rrbtSupportUrl = rrbtSupportUrl.replaceAll("%MSISDN%", bean.getSubscriberID());
		httpParameters.setUrl(rrbtSupportUrl);
		httpParameters.setUseProxy(isUseProxy);
		httpParameters.setProxyHost(proxyHostname);
		httpParameters.setProxyPort(proxyPort);
		httpParameters.setConnectionTimeout(connectionTimeout);
		Map<String,String> requestParams = new HashMap<String,String>();
		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, requestParams);
			if (httpResponse != null && httpResponse.getResponseCode() == 200
					&& httpResponse.getResponse() != null
					&&  (httpResponse.getResponse().indexOf("success")!=-1 || httpResponse.getResponse().indexOf("already")!=-1)) {
				//alreadySentRequestForRRBTList.add(bean.getSubscriberID());
				isProcessed = true;
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return isProcessed;
	}
	
	private static File createAndGetFile(String content, String fileName,
			String fileExt) throws IOException {
		File file = null;
		FileOutputStream fos = null;
		try {
			file = File.createTempFile(fileName, fileExt);
			file.deleteOnExit();
			fos = new FileOutputStream(file);
			fos.write(content.getBytes());
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch (IOException e) {
					throw e;
				}
			}
		}
		String msg = "Successfully created temporary file. File: " + file;
		LOGGER.info(msg);
		return file;
	}
	
	private String splitAndAppendSingleQuotes(String str) {
		if(null != str) {
			String[] arr = str.split(",");
			StringBuilder sb = new StringBuilder("'");
			for(int i = 0; i< arr.length;i++) {
				sb.append(arr[i].trim());
				sb.append("'");
				if(arr.length - 1 > i) {
					sb.append(",'");	
				}
			}
			LOGGER.info("Replaced quotes and returning mode: " + mode);
			return sb.toString();
		}
		LOGGER.info("Returning mode: " + mode);
		return str;
	}

	public static void main(String[] args) {
		DoubleConfirmationRequestBean bean = new DoubleConfirmationRequestBean();
		bean.setSubscriberID("9886030893");
		bean.setStatus(1);
		DoubleConfirmationRequestBean bean1 = new DoubleConfirmationRequestBean();
		bean1.setSubscriberID("9886030891");
		bean1.setStatus(1);
		DoubleConfirmationRequestBean bean2 = new DoubleConfirmationRequestBean();
		bean2.setSubscriberID("9888030880");
		bean2.setStatus(1);

		RBTConsentExpiredProcessor r = new RBTConsentExpiredProcessor(new RBTDaemonManager());
		r.run();
		
		List<DoubleConfirmationRequestBean> list = new ArrayList<DoubleConfirmationRequestBean>();
		
		list.add(bean);
		list.add(bean1);
//		list.add(bean2);
		
//		System.out.println(" >> " + r.updateRecordStatus(list));
		
// 		System.out.println(" >> " + r.constructXml(list));
	}
}
