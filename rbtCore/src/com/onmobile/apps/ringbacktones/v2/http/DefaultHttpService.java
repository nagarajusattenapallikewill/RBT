package com.onmobile.apps.ringbacktones.v2.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class DefaultHttpService implements HttpService {

	private PoolingClientConnectionManager connectionManager;

	public DefaultHttpService() {
		initConnectionManager();
	}

	private void initConnectionManager() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		// HttpConnectionParams.setConnectionTimeout(params, 30000);

		connectionManager = new PoolingClientConnectionManager();
		// HARDCODED: 200 is a default value for tomcat connector maxThreads,
		// that's why we chose it as initial hardcoded value
		connectionManager.setDefaultMaxPerRoute(200);
		// HARDCODED: We have 5 external datasources. 5 * DefaultMaxPerRoute =
		// 1000
		connectionManager.setMaxTotal(1000);
	}

	/* Client builders */

	@Override
	public HttpClient buildHttpClient() {
		return buildHttpClient(null);
	}

	@Override
	public HttpClient buildHttpClient(HttpParams params) {
		return new DefaultHttpClient(connectionManager, params);
	}

	DefaultRedirectStrategy defaultRedirectStrategy = new DefaultRedirectStrategy() {
		@Override
		public boolean isRedirected(HttpRequest request, HttpResponse response,
				HttpContext context) {
			boolean isRedirect = false;
			try {
				isRedirect = super.isRedirected(request, response, context);
			} catch (ProtocolException e) {
				throw new RuntimeException(
						"Can't determine if this request is a redirect request",
						e);
			}
			if (!isRedirect) {
				int responseCode = response.getStatusLine().getStatusCode();
				if (responseCode == 301 || responseCode == 302) {
					return true;
				}
			}
			return isRedirect;
		}
	};

	@Override
	public HttpClient buildHttpClientWithRedirectStrategyOn() {
		DefaultHttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS,
				true);
		// set such strategy to handle redirects automatically and follow them
		client.setRedirectStrategy(defaultRedirectStrategy);
		return client;
	}

	/* Requests */

	private String receiveResponseAsString(HttpRequestBase httpRequest,
			String proxyHost, int proxyPort) throws IOException {
		// prepare http client
		HttpClient httpClient = buildHttpClient();
		// setup proxy, if need to
		if (proxyHost != null && proxyPort >= 0) {
			setupProxy(httpClient, proxyHost, proxyPort);
		}
		try {
			HttpResponse response = httpClient.execute(httpRequest);
			// get received result from response entity
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			// ensure the connection gets released to the manager
			EntityUtils.consume(entity);
			// and only after, return result
			return result;
		} catch (IOException e) {
			httpRequest.abort();
			throw e;
		}
	}

	private void setupProxy(HttpClient httpClient, String proxyHost,
			int proxyPort) {
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
				new HttpHost(proxyHost, proxyPort));
	}

	@Override
	public String executeGetMethodForXml(String url, String proxyHost,
			int proxyPort) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("Accept", "application/xml");
		httpGet.addHeader("Content-Type", "application/xml");
		return receiveResponseAsString(httpGet, proxyHost, proxyPort);
	}

}
