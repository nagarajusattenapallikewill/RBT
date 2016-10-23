/**
 * SIPR_RBTBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.onmobile.apps.ringbacktones.thirdparty.tefspain;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.actions.WriteCDRLog;
import com.onmobile.apps.ringbacktones.webservice.bean.LoggerBean;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class SIPR_RBTBindingImpl implements SIPR_RBTPortType {
	private static Logger logger = Logger.getLogger(SIPR_RBTBindingImpl.class);

	private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static RBTHttpClient rbtHttpClient = null;
	static {
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setMaxTotalConnections(200);
		httpParameters.setMaxHostConnections(200);
		String timeout = RBTParametersUtils.getParamAsString("COMMON", "RESERVE_CHARE_TIMEOUT_IN_SECOND", "6");
		int iTimeout = Integer.parseInt(timeout) * 1000;
		httpParameters.setConnectionTimeout(iTimeout);
		httpParameters.setSoTimeout(iTimeout);
		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	public int altaSuscriptorEmpresa(java.lang.String MSISDN, int melodia, int tipoUsuario,
			java.lang.String franjaHoraria) throws java.rmi.RemoteException {
		return -3;
	}

	public java.lang.String[] consultaSuscriptorEmpresa(java.lang.String MSISDN) throws java.rmi.RemoteException {
		return null;
	}

	public int modificaSuscriptorEmpresa(java.lang.String MSISDN, int melodia, int tipoUsuario,
			java.lang.String franjaHoraria) throws java.rmi.RemoteException {
		return -3;
	}

	public int bajaSuscriptorEmpresa(java.lang.String MSISDN, int melodia) throws java.rmi.RemoteException {
		return -3;
	}

	public int confirmAlta(java.lang.String MSISDN) throws java.rmi.RemoteException {
		return -3;
	}

	public int confirmBaja(java.lang.String MSISDN) throws java.rmi.RemoteException {
		return -3;
	}

	public int cambioMSISDN(java.lang.String viejoMSISDN, java.lang.String nuevoMSISDN)
			throws java.rmi.RemoteException {
		return -3;
	}

	public int bajaServicio(java.lang.String MSISDN) throws java.rmi.RemoteException {
		return -3;
	}

	public int consultaEstado(java.lang.String MSISDN) throws java.rmi.RemoteException {
		return -3;
	}

	public int respuestaAlta(java.lang.String MSISDN, int resul) throws java.rmi.RemoteException {
		return -3;
	}

	public int respuestaBaja(java.lang.String MSISDN, int resul) throws java.rmi.RemoteException {
		return -3;
	}

	public int altaRBTCC(java.lang.String MSISDN, java.lang.String id_melodia) throws java.rmi.RemoteException {
		Date startDate = new Date();
		String requestType = "altaRBTCC[ACTIVATION]";
		StringBuilder builder = new StringBuilder();
		builder.append("[msisdn = ").append(MSISDN).append(", id_melodia = ").append(id_melodia).append("]");

		SelectionRequest selectionRequest = new SelectionRequest(MSISDN);
		try {
			String defaultSongConfig = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_SONG_CONFIG_FOR_SOAP_API",
					null);
			if (defaultSongConfig == null) {
				if (logger.isInfoEnabled())
					logger.info(
							"The parameter DEFAULT_SONG_CONFIG_FOR_SOAP_API is not configured, so not processing the request for : "
									+ MSISDN);
				return -1;
			}

			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(MSISDN);
			Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
			if (isUserActive(subscriber.getStatus())) {
				if (logger.isInfoEnabled())
					logger.info("Subscriber is already active, so not processing the request for : " + MSISDN);

				writeTransLog(requestType, builder.toString(), String.valueOf(-1), logDateFormat.format(startDate),
						(System.currentTimeMillis() - startDate.getTime()));
				return -1;
			}
			String[] tokens = defaultSongConfig.split(",");
			String defaultClipID = tokens[0].trim();
			String mode = "VP";
			if (tokens.length > 1 && !tokens[1].trim().isEmpty())
				mode = tokens[1].trim();

			String categoryID = "3";
			if (tokens.length > 2 && !tokens[2].trim().isEmpty())
				categoryID = tokens[2].trim();

			String subscriptionClass = null;
			if (tokens.length > 3 && !tokens[3].trim().isEmpty())
				subscriptionClass = tokens[3].trim();

			String chargeClass = null;
			if (tokens.length > 4 && !tokens[4].trim().isEmpty())
				chargeClass = tokens[4].trim();

			Clip clip = null;
			if (id_melodia != null && !id_melodia.trim().isEmpty() && !id_melodia.equalsIgnoreCase("null")) {
				try {
					clip = RBTCacheManager.getInstance().getClip(id_melodia);
				}
				catch (Exception e) {
					clip = null;
					logger.error(e.getMessage(), e);
				}

			}

			String clipID = null;
			if (clip != null) {
				clipID = String.valueOf(clip.getClipId());
			}
			else {
				clipID = defaultClipID;
				if (logger.isInfoEnabled()) {
					logger.info("id_melodia passed is invalid, falling back to default clipID for the subscriber : "
							+ MSISDN + ", id_melodia : " + id_melodia);
				}
			}

			selectionRequest.setMode(mode);
			selectionRequest.setModeInfo(mode);
			selectionRequest.setCategoryID(categoryID);
			selectionRequest.setClipID(clipID);
			if (chargeClass != null) {
				selectionRequest.setChargeClass(chargeClass);
				selectionRequest.setUseUIChargeClass(true);
			}

			selectionRequest.setSubscriptionClass(subscriptionClass);

			String reserveChargeUrl = RBTParametersUtils.getParamAsString("COMMON", "SM_RESERVE_CHARGE_URL", null);
			if (reserveChargeUrl != null) {
				HashMap<String, String> userInfoMap = new HashMap<String, String>();
				userInfoMap.put("RESERVE_CHARGE", "TRUE");
				selectionRequest.setUserInfoMap(userInfoMap);
			}

			Rbt rbt = RBTClient.getInstance().addSubscriberSelection(selectionRequest);
			String response = selectionRequest.getResponse();

			if (logger.isInfoEnabled())
				logger.info("Response from RBT Client for selection request : " + response);

			if (response.equalsIgnoreCase("SUCCESS") && reserveChargeUrl != null) {
				subscriber = rbt.getSubscriber();

				String srvKey = "RBT_ACT_" + subscriber.getSubscriptionClass();
				String refId = subscriber.getRefID();
				String actInfo = "";
				String content_id = "";
				if (subscriber.getActivationInfo() != null) {
					actInfo = subscriber.getActivationInfo().replaceAll("\\|", "/");
					actInfo = actInfo.replaceAll(":", ";");
				}
				content_id = "actinfo=" + actInfo;
				content_id = content_id + ",cosid:" + subscriber.getCosID() + "|cosid:" + subscriber.getCosID();

				boolean bSendSubTypeUnknown = false;
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager()
						.getSubscriptionClass(subscriber.getSubscriptionClass());

				// m_sendUnknownZeroAct is SEND_UNKNOWN_ZERO_ACT in
				// RBT_PARAMETERS
				if (subClass.getSubscriptionAmount() != null && subClass.getSubscriptionAmount().equalsIgnoreCase("0")
						&& RBTParametersUtils.getParamAsBoolean("DAEMON", "SEND_UNKNOWN_ZERO_ACT", "FALSE"))
					bSendSubTypeUnknown = true;

				String type = "P";
				if (bSendSubTypeUnknown) {
					type = "U";
				}
				else {
					type = RBTParametersUtils.getParamAsString("DAEMON", "SEND_SUB_TYPE", "p/b");
					if ("p/b".equalsIgnoreCase(type)) {
						if (subscriber.isPrepaid()) {
							type = "P";
						}
						else {
							type = "B";
						}
					}
				}

				reserveChargeUrl = reserveChargeUrl + "&msisdn=" + subscriber.getSubscriberID() + "&type=" + type
						+ "&srvkey=" + srvKey + "&mode=" + mode + "&refid=" + refId + "&info=" + content_id;

				logger.info("Reservecharging URL : " + reserveChargeUrl);

				HttpResponse httpResponse = rbtHttpClient.makeRequestByGet(reserveChargeUrl, null);

				if (!httpResponse.getResponse().trim().equalsIgnoreCase("success")) {
					if (logger.isInfoEnabled())
						logger.info("Reservecharging request failed : " + MSISDN + " Response: "
								+ httpResponse.getResponse());

					SubscriptionRequest subscriptionRequest = new SubscriptionRequest(MSISDN);
					subscriptionRequest.setIsDirectDeactivation(true);
					subscriptionRequest.setMode(mode);
					RBTClient.getInstance().deactivateSubscriber(subscriptionRequest);
					return 2;
				}
			}

			if (response.equalsIgnoreCase("SUCCESS")) {
				writeTransLog(requestType, builder.toString(), String.valueOf(0), logDateFormat.format(startDate),
						(System.currentTimeMillis() - startDate.getTime()));
				return 0;
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}

		writeTransLog(requestType, builder.toString(), String.valueOf(-1), logDateFormat.format(startDate),
				(System.currentTimeMillis() - startDate.getTime()));
		return -1;
	}

	public int bajaRBTCC(java.lang.String MSISDN, java.lang.String indEmpresa) throws java.rmi.RemoteException {
		int tefResponse = -1;
		Date startDate = new Date();
		boolean isSubscriberDeactivated = true;

		String deactivationMode = null;
		SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(MSISDN, "TEF-SPAIN"));

		// Valid Subscriber
		if (subscriberDetail != null && subscriberDetail.isValidSubscriber()) {
			deactivationMode = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_MODE_FOR_BI_API", "VP");

			// Hit BI URL
			String BI_Uri = RBTParametersUtils.getParamAsString("COMMON", "CHECK_BI_URI",
					"http://127.0.0.1:8111/SMSChurnPortal/SmsChurnPortal?msisdn=<msisdn>&option=baja&mode=<mode>");
			BI_Uri = BI_Uri.replace("<msisdn>", MSISDN);
			BI_Uri = BI_Uri.replace("<mode>", deactivationMode);

			HttpResponse httpResponse = hitBIUri(BI_Uri, null);
			if (httpResponse != null && httpResponse.getResponseCode() == 200) {
				tefResponse = 0;
			}

			logger.info("bajaRBTCC deactivation request received for MSISDN = " + MSISDN);
		}
		// Invalid Subscriber
		else {
			deactivationMode = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_MODE_FOR_SOAP_API", "VP");

			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(MSISDN);
			try {
				subscriptionRequest.setMode(deactivationMode);

				HashMap<String, String> userInfoMap = new HashMap<String, String>();
				userInfoMap.put(iRBTConstant.HLR_PROV, iRBTConstant.NO);
				subscriptionRequest.setUserInfoMap(userInfoMap);

				RBTClient.getInstance().deactivateSubscriber(subscriptionRequest);

				String response = subscriptionRequest.getResponse();
				logger.info("Response from RBT Client for deactivation request : " + response);
				isSubscriberDeactivated = (response.equalsIgnoreCase("SUCCESS")
						|| response.equalsIgnoreCase("user_not_exists") || response.equalsIgnoreCase("deactive")
						|| response.equalsIgnoreCase("deact_pending"));

				if (isSubscriberDeactivated) {
					tefResponse = 0;
				}
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}

		// Write Transaction Logs
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		long currTimeStamp = System.currentTimeMillis();
		long processedTime = currTimeStamp - startDate.getTime();
		String requestType = "bajaRBTCC[DEACTIVATION]";
		String requestParams = "[msisdn = " + MSISDN + "]";

		writeTransLog(requestType, requestParams, String.valueOf(0), logDateFormat.format(startDate), processedTime);

		// Write CDR Logs
		LoggerBean loggerBean = new LoggerBean();
		loggerBean.setSubscriberId(MSISDN);
		loggerBean.setTimestamp(String.valueOf(currTimeStamp));
		loggerBean.setLdapResponse(subscriberDetail.isValidSubscriber() ? "valid" : "invalid");
		loggerBean.setDeactivationMode(deactivationMode);
		loggerBean.setTefResponse(String.valueOf(tefResponse));
		loggerBean.setDeactivationStatus(isSubscriberDeactivated ? "success" : "failure");

		WriteCDRLog.writeSatPushCDRLog(loggerBean);

		return tefResponse;
	}

	private HttpResponse hitBIUri(String BI_Uri, Map<String, String> requestParams) {
		logger.info("BI url :-->" + BI_Uri);

		HttpResponse httpResponse = null;
		try {
			httpResponse = rbtHttpClient.makeRequestByGet(BI_Uri, requestParams);
		}
		catch (HttpException e) {
			logger.error("BI url exception:---> " + e);
			logger.error("Faild to make HTTP call to BI partner, " + "Exception: " + e.getMessage(), e);
		}
		catch (IOException e) {
			logger.error("BI url exception:---> " + e);
			logger.error("Faild to make HTTP call to BI partner, " + "Exception: " + e.getMessage(), e);
		}
		return httpResponse;
	}

	public int compraRBTCC(java.lang.String MSISDN, java.lang.String fecCompra, java.lang.String id_melodia)
			throws java.rmi.RemoteException {
		return -3;
	}

	public int altaRBTPubliCC(java.lang.String MSISDN) throws java.rmi.RemoteException {
		return -3;
	}

	public int bajaRBTPubliCC(java.lang.String MSISDN) throws java.rmi.RemoteException {
		return -3;
	}

	/*
	 * Returns the content price charged for the particular id (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.thirdparty.tefspain.SIPR_RBTPortType#
	 * consultaRBTPrecio(java.lang.String, java.lang.String)
	 */
	public java.lang.String[] consultaRBTPrecio(java.lang.String MSISDN, java.lang.String id_melodia)
			throws java.rmi.RemoteException {
		Date startDate = new Date();
		String requestType = "consultaRBTPrecio[DOWNLOAD_PRICE]";
		StringBuilder builder = new StringBuilder();
		builder.append("[msisdn = ").append(MSISDN).append(", id_melodia = ").append(id_melodia).append("]");

		Clip clip = RBTCacheManager.getInstance().getClip(id_melodia);
		if (clip == null) {
			logger.info("Clip object is null for the clipId : " + id_melodia);

			writeTransLog(requestType, builder.toString(), null, logDateFormat.format(startDate),
					(System.currentTimeMillis() - startDate.getTime()));
			return null;
		}

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(MSISDN);
		Downloads downloadsObj = RBTClient.getInstance().getDownloads(rbtDetailsRequest);
		if (downloadsObj == null) {
			logger.info("Downloads object is null for the subscriberID : " + MSISDN);

			writeTransLog(requestType, builder.toString(), null, logDateFormat.format(startDate),
					(System.currentTimeMillis() - startDate.getTime()));
			return null;
		}

		Download[] downloads = downloadsObj.getDownloads();
		if (downloads == null) {
			logger.info("Downloads object is null for the subscriberID : " + MSISDN);

			writeTransLog(requestType, builder.toString(), null, logDateFormat.format(startDate),
					(System.currentTimeMillis() - startDate.getTime()));
			return null;
		}

		for (Download download : downloads) {
			logger.info("Download for the subscriberID : " + MSISDN + ", Download : " + download);
			if (download.getToneID() == clip.getClipId()) {
				String chargeClass = download.getChargeClass();
				ChargeClass chargeClassObj = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass);
				if (chargeClassObj != null) {
					String[] contentPrices = { chargeClassObj.getAmount() };

					writeTransLog(requestType, builder.toString(), chargeClassObj.getAmount(),
							logDateFormat.format(startDate), (System.currentTimeMillis() - startDate.getTime()));
					return contentPrices;
				}
			}
		}

		writeTransLog(requestType, builder.toString(), null, logDateFormat.format(startDate),
				(System.currentTimeMillis() - startDate.getTime()));
		return null;
	}

	/*
	 * Returns active clipIDs of the default selections by comma-separated
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.thirdparty.tefspain.SIPR_RBTPortType#
	 * consultaRBTDefecto(java.lang.String)
	 */
	public java.lang.String consultaRBTDefecto(java.lang.String MSISDN) throws java.rmi.RemoteException {
		Date startDate = new Date();
		String requestType = "consultaRBTDefecto[DEFAULT_RBT]";
		StringBuilder builder = new StringBuilder();
		builder.append("[msisdn = ").append(MSISDN).append("]");

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(MSISDN);
		Settings settingsObj = RBTClient.getInstance().getSettings(rbtDetailsRequest);
		if (settingsObj == null) {
			logger.info("Settings object is null for the subscriberID : " + MSISDN);

			writeTransLog(requestType, builder.toString(), null, logDateFormat.format(startDate),
					(System.currentTimeMillis() - startDate.getTime()));
			return null;
		}

		String activeClipIDs = null;
		Setting[] settings = settingsObj.getSettings();

		if (settings == null) {
			logger.info("Settings object is null for the subscriberID : " + MSISDN);

			writeTransLog(requestType, builder.toString(), null, logDateFormat.format(startDate),
					(System.currentTimeMillis() - startDate.getTime()));
			return null;
		}

		for (Setting setting : settings) {
			logger.info("Setting for the subscriberID : " + MSISDN + ", Setting : " + setting);
			if (!setting.getCallerID().equalsIgnoreCase("all"))
				continue;

			if (setting.getStatus() != 1)
				continue;

			Category category = RBTCacheManager.getInstance().getCategory(setting.getCategoryID());
			if (Utility.isShuffleCategory(category.getCategoryTpe()))
				continue;

			String clipID = String.valueOf(setting.getToneID());
			if (activeClipIDs == null)
				activeClipIDs = clipID;
			else
				activeClipIDs += "," + clipID;
		}

		writeTransLog(requestType, builder.toString(), activeClipIDs, logDateFormat.format(startDate),
				(System.currentTimeMillis() - startDate.getTime()));
		return activeClipIDs;
	}

	public int regaloYavoyCC(java.lang.String MSISDN, java.lang.String MSISDNDEST) throws java.rmi.RemoteException {
		return -3;
	}

	private void writeTransLog(String requestType, String requestParams, String response, String startTime,
			long processedTime) {
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(requestType).append(", ").append(startTime).append(", ").append(processedTime).append(", ")
				.append(requestParams).append(", ").append(response);
		RBTEventLogger.logEvent(RBTEventLogger.Event.SOAP_OPERATIONS, logBuilder.toString());
	}

	private boolean isUserActive(String subscriberStatus) {
		if (subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.LOCKED)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.GRACE)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
			return true;

		return false;
	}
}