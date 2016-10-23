package com.onmobile.apps.ringbacktones.common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpException;

import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class RBTHttpUtils {

	public static HttpResponse makeHttpPostRequest(String url, boolean isUseProxy,
			String proxyHostname, int proxyPort, int connectionTimeout,
			int connectionSoTimeout, File file) throws HttpException,
			IOException {
		HttpResponse httpResponse = null;
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setUrl(url);
		httpParameters.setUseProxy(isUseProxy);
		httpParameters.setProxyHost(proxyHostname);
		httpParameters.setProxyPort(proxyPort);
		httpParameters.setSoTimeout(connectionSoTimeout);
		httpParameters.setConnectionTimeout(connectionTimeout);

		HashMap<String, File> fileParams = null;
		if (null != file) {
			fileParams = new HashMap<String, File>();
			fileParams.put("xml", file);
		}

		httpResponse = RBTHttpClient.makeRequestByPost(httpParameters, null,
				fileParams);

		return httpResponse;
	}
}
