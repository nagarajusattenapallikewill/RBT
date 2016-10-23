package com.onmobile.apps.ringbacktones.v2.http;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpParams;

public interface HttpService {

	/* Client builders */
	public HttpClient buildHttpClient();

	public HttpClient buildHttpClient(HttpParams httpParams);

	public HttpClient buildHttpClientWithRedirectStrategyOn();

	public String executeGetMethodForXml(String url, String proxyHost, int proxyPort) throws IOException;

}
