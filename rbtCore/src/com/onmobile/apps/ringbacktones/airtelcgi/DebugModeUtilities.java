/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;

/**
 * @author vinayasimha.patil
 *
 */
public class DebugModeUtilities implements DSProtocolConstants
{
	private static Logger logger = Logger.getLogger(DebugModeUtilities.class);
	
	private static DebugModeUtilities bebugModeUtilities = null;

	private HttpParameters httpParameters;

	private DebugModeUtilities()
	{
		int httpConnectionTimeout = DSServer.configurations.getHttpConnectionTimeout();
		int httpDataTimeout = DSServer.configurations.getHttpDataTimeout();
		boolean useProxy = DSServer.configurations.isUseProxy();
		String proxyHost = DSServer.configurations.getProxyHost();
		int proxyPort = DSServer.configurations.getProxyPort();
		httpParameters = new HttpParameters(null, useProxy, proxyHost, proxyPort, httpConnectionTimeout, httpDataTimeout, false);
	}

	synchronized public static DebugModeUtilities getInstance()
	{
		if(bebugModeUtilities == null)
			bebugModeUtilities = new DebugModeUtilities();

		return bebugModeUtilities;
	}

	public String processSubscriberProfileRequest(String ipAddress, String srcSubscriberID)
	{
		String url = "http://"+ ipAddress +":"+ DSServer.configurations.getPort(ipAddress) +"/"+ DSServer.configurations.getSubscriberProfileURI();

		String response = "RETRY";

		try
		{
			logger.info("RBT:: URL: "+ url);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("subscriber_id", srcSubscriberID);

			logger.info("RBT:: Params: "+ params);

			httpParameters.setUrl(url);
			String httpResponse = RBTHTTPProcessing.postFile(httpParameters, params, null);
			if(httpResponse != null)
			{
				httpResponse = httpResponse.trim();
				if(httpResponse.startsWith("SUCCESS"))
				{
					response = "SUCCESS:";
					byte ps = Byte.parseByte(httpResponse.substring(httpResponse.lastIndexOf(':')+1));
					if(ps == 0 || ps == 1 || ps == 5 || ps == 6 || ps == 9 || ps == 10 || ps == 14 ||ps == 15 ||
							ps == 16 || ps == 20 || ps == 21 || ps == 25 || ps == 26 || ps == 28 || ps == 29)
						response += "ACTIVE";
					else
						response += "DEACTIVE";
				}
				else if(response.startsWith("ERROR"))
				{
					short errorCode = Short.parseShort(response.substring(response.indexOf(':')+1));
					if(errorCode == RECORD_NOT_FOUND || errorCode == SUBSCRIBER_DOES_NOT_EXIST)
						response = "SUCCESS:NEWUSER";
				}
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}

		return response;
	}

	public String processToneCopyRequest(String ipAddress, String srcSubscriberID, String dstSubscriberID)
	{
		String url = "http://"+ ipAddress +":"+ DSServer.configurations.getPort(ipAddress) +"/"+ DSServer.configurations.getToneCopyURI();

		String response = "RETRY";

		try
		{
			logger.info("RBT:: URL: "+ url);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("subscriber_id", dstSubscriberID);
			params.put("caller_id", srcSubscriberID);

			logger.info("RBT:: Params: "+ params);

			httpParameters.setUrl(url);
			String httpResponse = RBTHTTPProcessing.postFile(httpParameters, params, null);
			if(httpResponse != null)
			{
				httpResponse = httpResponse.trim();
				response = httpResponse;
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}

		return response;
	}

	public String processToneGiftRequest(String ipAddress, String srcSubscriberID, String dstRegionID, String toneID)
	{
		String url = "http://"+ ipAddress +":"+ DSServer.configurations.getPort(ipAddress) +"/"+ DSServer.configurations.getToneGiftURI();

		String response = "RETRY";

		try
		{
			logger.info("RBT:: URL: "+ url);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("subscriber_id", srcSubscriberID);
			params.put("caller_id", dstRegionID);
			params.put("wav_file", toneID);

			logger.info("RBT:: Params: "+ params);

			httpParameters.setUrl(url);
			String httpResponse = RBTHTTPProcessing.postFile(httpParameters, params, null);
			if(httpResponse != null)
			{
				httpResponse = httpResponse.trim();
				response = httpResponse;
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}

		return response;
	}
}
