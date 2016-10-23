package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;
import com.onmobile.apps.ringbacktones.provisioning.bean.RBTRto;
import com.onmobile.apps.ringbacktones.provisioning.implementation.consent.ConsentProcessor;
import com.onmobile.apps.ringbacktones.webservice.actions.WriteCDRLog;
import com.onmobile.apps.ringbacktones.webservice.bean.SatPushLoggerBean;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class RBTRtoWorkerThread implements Callable<String> {
	static Logger logger = Logger.getLogger(RBTRtoWorkerThread.class);
	static RBTRto rbtRto;

	public RBTRto getRbtRto() {
		return rbtRto;
	}

	public void setRbtRto(RBTRto rbtRto) {
		RBTRtoWorkerThread.rbtRto = rbtRto;
	}

	public RBTRtoWorkerThread(RBTRto rtoObj) {
		RBTRtoWorkerThread.rbtRto = rtoObj;
	}

	static Map<String, String> requestParams = new HashMap<String, String>();
	static int responseCode = 0;
	public static String internal_err = "INTERNAL ERROR";
	public static String conn_err = "CONNECTION ERROR";

	static int maxRetryCount = RBTParametersUtils.getParamAsInt(
			iRBTConstant.DAEMON, "RTO_MAX_RETRY_COUNT", 0);
	static String cpURL = RBTParametersUtils.getParamAsString(
			iRBTConstant.DAEMON, "RTO_CP_URL", null);

	@Override
	public String call() throws Exception {
		logger.info("RTO Worker Thread Started");
		logger.info("Rto Object: " + rbtRto);
		String response = null;
		response = makeSDPConnection();
		return response;
	}

	public String makeSDPConnection() {
		logger.info("Inside SDP connection:trying to hit SDP url "
				+ rbtRto.getUrl());
		String response = null;
		HttpParameters httpParameters = new HttpParameters(rbtRto.getUrl());
		requestParams = addQueryStringToRequestParams(rbtRto.getUrl(),
				requestParams);
		if (rbtRto.getRetry() <= maxRetryCount) {
			SatPushLoggerBean cpLoggerBean = new SatPushLoggerBean();
			cpLoggerBean.setSubscriberId(rbtRto.getSubscriberId());
			cpLoggerBean.setRequestUrl(rbtRto.getUrl());
			try {
				cpLoggerBean.setRequestSentDate(Calendar.getInstance());
				httpParameters.setConnectionTimeout(100000);
				httpParameters.setSoTimeout(100000);
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				response = httpResponse.getResponse();
				responseCode = httpResponse.getResponseCode();
				logger.info("response returned after SDP url hit: "+response);
				cpLoggerBean
						.setTimeTaken(""
								+ (Calendar.getInstance().getTimeInMillis() - cpLoggerBean
										.getRequestSentDate().getTimeInMillis()));
				cpLoggerBean.setResponse(response);
				WriteCDRLog.writeSatPushCDRLog(cpLoggerBean);
				if (response.contains("SUCCESS")) {
					deleteProcessedRecords();
				} else {
					makeCPConnection();
				}
			} catch (Exception e) {
				response = e.getMessage();
				if (response != null) {
					if (response.contains("Read timed out")
							|| response
									.contains("java.net.SocketTimeoutException")) {
						logger.info("encounterd with Read timed out exception");
						RBTRto rtoObj = null;
						rtoObj = getRtoObj(rbtRto.getSdpomtxnid());
						if (rtoObj != null) {
							saveSDPObject(rtoObj);
						}
					} else {
						RBTRto rtoObj = getRtoObj(rbtRto.getSdpomtxnid());
						if (rtoObj != null)
							saveSDPObject(rtoObj);
					}
				}
			}
		} else {
			logger.info("Reached maximum retry counts " + rbtRto.getRetry()
					+ " for SDP url: " + rbtRto.getUrl());
			deleteProcessedRecords();
			response = "Already processed";
		}
		return response;
	}

	private static Map<String, String> addQueryStringToRequestParams(
			String url, Map<String, String> requestParams) {
		if (url != null && url.length() > 0) {
			String[] queryStringTokens = url.split("&");
			for (String queryStringToken : queryStringTokens) {
				String param = null;
				String value = null;

				int index = queryStringToken.indexOf('=');
				if (index != -1) {
					param = queryStringToken.substring(0, index);

					index++;
					if (index < queryStringToken.length())
						value = queryStringToken.substring(index);
				} else
					param = queryStringToken;

				value = (value != null ? (value.replaceAll("%26", "&")) : value);

				if (!requestParams.containsKey(param))
					requestParams.put(param, value);
			}
		}
		return requestParams;
	}

	public static void saveSDPObject(RBTRto rtoObj) {
		Session session = null;
		Transaction tx = null;
		int retry = rtoObj.getRetry();
		String redirectionURL = getConstructedSDPURL(rtoObj.getUrl(), retry);
		try {
			session = HibernateUtil.getSession();
			rtoObj.setRetry(retry + 1);
			rtoObj.setUrl(redirectionURL);
			rtoObj.setRetryTime(new Date());
			tx = session.beginTransaction();
			session.saveOrUpdate(rtoObj);
			tx.commit();
			session.flush();
			logger.info("SDP url " + redirectionURL + " for subscriberID "
					+ rtoObj.getSubscriberId() + " and sdpomtxnId "
					+ rtoObj.getSdpomtxnid());
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error(
					"Unable to save: " + rtoObj + ", Exception: "
							+ he.getMessage(), he);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	public static RBTRto getRtoObj(String sdpomId) {
		Session session = null;
		RBTRto rtoObj = null;
		Query query = null;
		try {
			session = HibernateUtil.getSession();
			query = session
					.createQuery("FROM RBTRto WHERE sdpomtxnid = :sdpomtxnid");
			query.setString("sdpomtxnid", sdpomId);
			rtoObj = (RBTRto) query.uniqueResult();
			if (rtoObj != null)
				logger.info("Rto object is fetched for subscriberID "
						+ rtoObj.getSubscriberId() + " and sdpomtxnid "
						+ rtoObj.getSdpomtxnid() + " : " + rtoObj);
		} catch (HibernateException he) {
			he.printStackTrace();
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
		return rtoObj;
	}

	public static void deleteProcessedRecords() {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			Query query = null;
			query = session
					.createQuery("Delete  from RBTRto where id = :id and subscriberId= :subId and sdpomtxnid= :sdpomtxnid");
			query.setString("subId", rbtRto.getSubscriberId());
			query.setString("id", rbtRto.getId());
			query.setString("sdpomtxnid", rbtRto.getSdpomtxnid());
			tx = session.beginTransaction();
			query.executeUpdate();
			tx.commit();
			logger.info("query string for deleting rto object: "+query.getQueryString());
			logger.info("Rto object " + rbtRto + " is deleted");
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}

	public static String getConstructedSDPURL(String url, int retryCount) {
		url = url.substring(0, url.lastIndexOf("&"));
		url = url.concat("&retry=" + String.valueOf(retryCount));
		return url;
	}

	public static void makeCPConnection() {
		String cpUrl = getConstructedCPUrl();
		logger.info("Inside CP connection:trying to hit CP url " + cpUrl);
		String response = null;
		Map<String, String> cprequestParams = new HashMap<String, String>();
		HttpParameters httpParameters = new HttpParameters(cpUrl);
		cprequestParams = addQueryStringToRequestParams(cpUrl, cprequestParams);
		SatPushLoggerBean cpLoggerBean = new SatPushLoggerBean();
		cpLoggerBean.setSubscriberId(rbtRto.getSubscriberId());
		cpLoggerBean.setRequestUrl(cpUrl);
		try {
			cpLoggerBean.setRequestSentDate(Calendar.getInstance());
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, cprequestParams);
			response = httpResponse.getResponse();
			logger.info("response returned after CP url hit: "+response);
			cpLoggerBean.setTimeTaken(""
					+ (Calendar.getInstance().getTimeInMillis() - cpLoggerBean
							.getRequestSentDate().getTimeInMillis()));
			cpLoggerBean.setResponse(response);
			if (response != null
					&& (response.equalsIgnoreCase(internal_err) || response
							.equalsIgnoreCase(conn_err))) {
				RBTRto cpObj = new RBTRto();
				cpObj = getRtoObj(rbtRto.getSdpomtxnid());
				cpObj.setUrl(cpUrl);
				logger.info("internal error or connection error occured while CP url hit.saving CP object");
				saveCPObject(cpObj);
			} else {
				deleteProcessedRecords();
			}
			WriteCDRLog.writeSatPushCDRLog(cpLoggerBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getConstructedCPUrl() {
		ConsentProcessor cp;
		try {
			cp = new ConsentProcessor();
			String subID = requestParams.get("msisdn");
			String srvKey = requestParams.get("srvkey");
			String transId = requestParams.get("transid");
			String mode = requestParams.get("channelType");
			String orderTypeId = requestParams.get("orderTypeId");
			String reqType = "Sub";
			String sdpomtxnId = requestParams.get("sdpomtxnid");
			cpURL = cpURL.replaceAll("%msisdn%", subID);
			cpURL = cpURL.replaceAll("%statuscode%",
					String.valueOf(responseCode));
			cpURL = cpURL.replaceAll("%srvkey%", srvKey);
			cpURL = cpURL.replaceAll("%Refid%", transId);
			cpURL = cpURL.replaceAll("%mode%", mode);
			cpURL = cpURL.replaceAll("%reqType%", reqType);
			String seapitype = cp.getseapiType(subID, orderTypeId, true, false);
			cpURL = cpURL.replaceAll("%precharge%", "N");
			cpURL = cpURL.replaceAll("%originator%", "ONMOBILE");
			cpURL = cpURL.replaceAll("%seapitype%", seapitype);
			cpURL = cpURL.replaceAll("%sdpomtxnid%", sdpomtxnId);
		} catch (RBTException e) {
			e.printStackTrace();
		}
		return cpURL;
	}

	public static void saveCPObject(RBTRto cpObj) {
		Session session = null;
		Transaction tx = null;
		int retry = cpObj.getRetry();
		try {
			session = HibernateUtil.getSession();
			cpObj.setRetry(retry + 1);
			cpObj.setRetryTime(new Date());
			tx = session.beginTransaction();
			session.saveOrUpdate(cpObj);
			tx.commit();
			session.flush();
			logger.info("CP url " + cpObj.getUrl() + " for subscriberID "
					+ cpObj.getSubscriberId() + " and sdpomtxnId "
					+ cpObj.getSdpomtxnid());
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error(
					"Unable to save: " + cpObj + ", Exception: "
							+ he.getMessage(), he);
		} finally {
			if (session != null) {
				session.clear();
				session.close();
			}
		}
	}
}
