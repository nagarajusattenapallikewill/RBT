/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.reliance;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.ExtraInfoKey;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * @author vinayasimha.patil
 * 
 */
public class RelianceShazamsInterface
{
	private static Logger logger = Logger
			.getLogger(RelianceShazamsInterface.class);

	private static RBTHttpClient rbtHttpClient;

	static
	{
		String proxy = RBTParametersUtils.getParamAsString(iRBTConstant.DAEMON,
				"SHAZAMS_PROXY", null);

		boolean useProxy = false;
		String proxyHost = null;
		int proxyPort = 0;
		if (proxy != null && proxy.length() > 0)
		{
			useProxy = true;
			String[] proxyTokens = proxy.split(":");
			proxyHost = proxyTokens[0];
			proxyPort = Integer.parseInt(proxyTokens[1]);
		}

		int connectionTimeout = RBTParametersUtils.getParamAsInt(
				iRBTConstant.DAEMON, "SHAZAMS_CONNECTION_TOMEOUT", 6000);
		int soTimeout = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"SHAZAMS_SOCKET_TOMEOUT", 6000);
		int maxTotalConnections = RBTParametersUtils.getParamAsInt(
				iRBTConstant.DAEMON, "SHAZAMS_MAX_HTTP_CONNECTIONS", 20);

		// As HTTP connections will be established for only one server, maximum
		// connections per host is also assigned same as maximum total
		// connections.
		int maxHostConnections = maxTotalConnections;

		HttpParameters httpParameters = new HttpParameters(useProxy, proxyHost,
				proxyPort, connectionTimeout, soTimeout, maxTotalConnections,
				maxHostConnections);

		if (logger.isInfoEnabled())
			logger.info("httpParameters: " + httpParameters);

		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	public static boolean downloadTune(RbtProvisioningRequestCommand command)
			throws RelianceShazamsException
	{
		ProvisioningRequests provisioningRequest = command
				.getProvisioningRequest();

		String url = null;
		Date startDate = new Date();
		DownloadTuneStatus downloadTuneStatus = DownloadTuneStatus.UNKNOWN;
		HttpResponse httpResponse = null;
		try
		{
			url = getDownloadTuneURL(provisioningRequest);
			httpResponse = rbtHttpClient.makeRequestByGet(url,
					null);
			if (logger.isInfoEnabled())
				logger.info("httpResponse: " + httpResponse);

			if (httpResponse.getResponseCode() == 200)
			{
				String status = httpResponse.getResponseHeader("_status");
				int statusCode = -1;
				if (status != null)
					statusCode = Integer.parseInt(status);

				downloadTuneStatus = DownloadTuneStatus.getStatus(statusCode);
				if (downloadTuneStatus == DownloadTuneStatus.UNKNOWN
						|| downloadTuneStatus == DownloadTuneStatus.INACTIVE_SUBSCRIBER
						|| downloadTuneStatus == DownloadTuneStatus.CRBT_DOWNLOAD_ERROR)
				{
					throw new RelianceShazamsException(
							"Retryable Error, DownloadTuneStatus: "
									+ downloadTuneStatus);
				}
			}
			else
			{
				throw new RelianceShazamsException(
						"Failed download tune http request, HttpStatusCode: "
								+ httpResponse.getResponseCode());
			}

			return true;
		}
		catch (IOException e)
		{
			logger.debug(e.getMessage(), e);
			throw new RelianceShazamsRetriableException(e.getMessage(), e);
		}
		catch (RelianceShazamsException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			try
			{
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

				/*
				 * Log Format:
				 * subscriberID,action,actionResponse,URL,urResponse|
				 * statusCode|_status,hitTime,responseTime
				 */
				StringBuilder builder = new StringBuilder();
				builder.append(provisioningRequest.getSubscriberId());
				builder.append(",").append(ProvisioningRequests.Type.SELECTION);
				builder.append(",").append(downloadTuneStatus);
				builder.append(",").append(url);
				if (httpResponse != null)
				{
					String response = httpResponse.getResponse();
					response = response != null ? response.trim() : null;

					builder.append(",").append(response).append("|");
					builder.append(httpResponse.getResponseCode()).append("|");
					builder.append(downloadTuneStatus.getStatusCode());
					builder.append(",").append(dateFormat.format(startDate));
					builder.append(",").append(httpResponse.getResponseTime());
				}
				else
				{
					builder.append(",ERROR");
					builder.append(",").append(dateFormat.format(startDate));
					builder.append(",").append(
							(System.currentTimeMillis() - startDate.getTime()));
				}

				RBTEventLogger.logEvent(RBTEventLogger.Event.ARBT,
						builder.toString());
			}
			catch (Exception e)
			{
				logger.warn("Unbable to write ARBT Event log", e);
			}
		}

		return false;
	}

	private static String getDownloadTuneURL(
			ProvisioningRequests provisioningRequest)
	{
		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();

		String url = parametersCacheManager.getParameterValue(
				iRBTConstant.DAEMON, "SHAZAMS_DOWNLOAD_TUNE_URL", null);

		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(provisioningRequest.getExtraInfo());
		int clipID = Integer.parseInt(extraInfoMap
				.get(ExtraInfoKey.CLIP_ID.toString()));
		Clip clip = RBTCacheManager.getInstance().getClip(clipID);

		url = url.replaceAll("%SUBSCRIBER_ID%",
				provisioningRequest.getSubscriberId());
		url = url.replaceAll("%MODE%", "ARBT");
		url = url.replaceAll("%REQUEST_ID%",
				String.valueOf(provisioningRequest.getRequestId()));
		url = url.replaceAll("%SERVICE_ID%",
				"0-0-OM-" + provisioningRequest.getMode());
		url = url.replaceAll("%CLIP_PROMO_ID%", clip.getClipPromoId());

		if (logger.isDebugEnabled())
			logger.debug("url: " + url);
		return url;
	}

	/**
	 * @author vinayasimha.patil
	 * 
	 */
	public enum DownloadTuneStatus
	{
		ARBT_DOWNLOAD_SUCCESS(2000),
		ARBT_DOWNLOAD_FAILED(2100),
		INACTIVE_SUBSCRIBER(2101),
		ARBT_LOW_BALANCE(2102),
		ALREADY_EXISTS(2103),
		INVALID_CONTENT(2104),
		CRBT_DOWNLOAD_SUCCESS(0),
		CRBT_DOWNLOAD_FAILED(2111),
		CRBT_LOW_BALANCE(2112),
		CRBT_DOWNLOAD_ERROR(2113),
		RAPSUB_DOWN(8886),
		MDN_INACTIVE_IN_RAPSUB(8887),
		ARBT_SUB_SUCCESS(1000),
		ARBT_SUB_FAILED(1100),
		ARBT_SUB_ALREADY_EXISTS(1101),
		ARBT_SUB_LOW_BALANCE(1102),
		ARBT_SUB_TEMP_ISSUE(1200),
		UNKNOWN(-1);

		private int statusCode;

		DownloadTuneStatus(int statusCode)
		{
			this.statusCode = statusCode;
		}

		public int getStatusCode()
		{
			return statusCode;
		}

		public static DownloadTuneStatus getStatus(int statusCode)
		{
			switch (statusCode)
			{
				case 1000:
					return ARBT_SUB_SUCCESS;
				case 1100:
					return ARBT_SUB_FAILED;
				case 1101:
					return ARBT_SUB_ALREADY_EXISTS;
				case 1102:
					return ARBT_SUB_LOW_BALANCE;
				case 1200:
					return ARBT_SUB_TEMP_ISSUE;
				case 2000:
					return ARBT_DOWNLOAD_SUCCESS;
				case 2100:
					return ARBT_DOWNLOAD_FAILED;
				case 2101:
					return INACTIVE_SUBSCRIBER;
				case 2102:
					return ARBT_LOW_BALANCE;
				case 2103:
					return ALREADY_EXISTS;
				case 2104:
					return INVALID_CONTENT;
				case 0:
					return CRBT_DOWNLOAD_SUCCESS;
				case 2111:
					return CRBT_DOWNLOAD_FAILED;
				case 2112:
					return CRBT_LOW_BALANCE;
				case 2113:
					return CRBT_DOWNLOAD_ERROR;
				case 8886:
					return RAPSUB_DOWN;
				case 8887:
					return MDN_INACTIVE_IN_RAPSUB;
				default:
					return UNKNOWN;
			}
		}
	}
}
