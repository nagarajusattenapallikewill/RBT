/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import org.apache.commons.httpclient.UsernamePasswordCredentials;

import com.onmobile.apps.ringbacktones.hunterFramework.management.HttpPerformanceMonitor;

/**
 * Data holder for the {@link RBTHttpClient} configurations.
 * 
 * @author vinayasimha.patil
 */
public class HttpParameters
{
	/**
	 * Http url.
	 */
	private String url = null;

	/**
	 * Tells whether to use proxy or not for making http hit.
	 */
	private boolean useProxy = false;

	/**
	 * Proxy host name (or IP). This field will have significance only if
	 * {@link #useProxy} is <tt>true</tt>.
	 */
	private String proxyHost = null;

	/**
	 * Proxy port. This field will have significance only if {@link #useProxy}
	 * is <tt>true</tt>.
	 */
	private int proxyPort;

	/**
	 * Http connection timeout in milliseconds.
	 */
	private int connectionTimeout = 6000;

	/**
	 * Socket timeout in milliseconds.
	 */
	private int soTimeout = 6000;

	/**
	 * Maximum number connections can be pooled. This field will have
	 * significance only if http connection pooling is used.
	 */
	private int maxTotalConnections = 20;

	/**
	 * Maximum number connections can be created for any host in the total http
	 * connection pool. This field will have significance only if http
	 * connection pooling is used.
	 */
	private int maxHostConnections = 2;

	/**
	 * Holds the reference of http performance monitor. If this field holds the
	 * non null value then all http hits will be monitored and performance will
	 * be logged.
	 */
	private HttpPerformanceMonitor httpPerformanceMonitor = null;

	/**
	 * Constructs the HttpParameters with default values.
	 */
	
	/**
	 * Holds the reference of authentication credentials. If this field holds the
	 * non null value then htt hits with server authentication.
	 */
	private UsernamePasswordCredentials usernamePasswordCredentials = null; 
	
	public HttpParameters()
	{

	}

	/**
	 * Constructs the HttpParameters with <tt>url</tt>. This constructor can be
	 * used when making the simple http hit with default configuration.
	 * 
	 * @param url
	 *            the http url
	 */
	public HttpParameters(String url)
	{
		this.url = url;
	}

	/**
	 * Constructs the HttpParameters with <tt>url</tt>, <tt>useProxy</tt>,
	 * <tt>proxyHost</tt>, <tt>proxyPort</tt>, <tt>connectionTimeout</tt> and
	 * <tt>soTimeout</tt>. This constructor can be used when making simple http
	 * hit with proxy and timeout configurations.
	 * 
	 * @param url
	 *            the http url
	 * @param useProxy
	 *            whether to use proxy or not
	 * @param proxyHost
	 *            the proxy host name or IP
	 * @param proxyPort
	 *            the proxy port number
	 * @param connectionTimeout
	 *            connection timeout in milliseconds
	 * @param soTimeout
	 *            socket time in milliseconds
	 */
	public HttpParameters(String url, boolean useProxy, String proxyHost,
			int proxyPort, int connectionTimeout, int soTimeout)
	{
		this.url = url;
		this.useProxy = useProxy;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.connectionTimeout = connectionTimeout;
		this.soTimeout = soTimeout;
	}

	/**
	 * Constructs the HttpParameters with <tt>useProxy</tt>, <tt>proxyHost</tt>,
	 * <tt>proxyPort</tt>, <tt>connectionTimeout</tt>, <tt>soTimeout</tt>,
	 * <tt>maxTotalConnections</tt> and <tt>maxHostConnections</tt>. This
	 * constructor can used to create {@link RBTHttpClient} for connection
	 * pooling.
	 * 
	 * @param useProxy
	 *            whether to use proxy or not
	 * @param proxyHost
	 *            the proxy host name or IP
	 * @param proxyPort
	 *            the proxy port number
	 * @param connectionTimeout
	 *            connection timeout in milliseconds
	 * @param soTimeout
	 *            socket time in milliseconds
	 * @param maxTotalConnections
	 *            maximum number of connections can be pooled
	 * @param maxHostConnections
	 *            maximum number of connections per host in the pool
	 */
	public HttpParameters(boolean useProxy, String proxyHost, int proxyPort,
			int connectionTimeout, int soTimeout, int maxTotalConnections,
			int maxHostConnections)
	{
		this.useProxy = useProxy;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.connectionTimeout = connectionTimeout;
		this.soTimeout = soTimeout;
		this.maxTotalConnections = maxTotalConnections;
		this.maxHostConnections = maxHostConnections;
	}

	/**
	 * Constructs the HttpParameters with <tt>useProxy</tt>, <tt>proxyHost</tt>,
	 * <tt>proxyPort</tt>, <tt>connectionTimeout</tt>, <tt>soTimeout</tt>,
	 * <tt>maxTotalConnections</tt>, <tt>maxHostConnections</tt> and
	 * <tt>httpPerformanceMonitor</tt>. This constructor can used to create
	 * {@link RBTHttpClient} for connection pooling and http performance
	 * monitoring.
	 * 
	 * @param useProxy
	 *            whether to use proxy or not
	 * @param proxyHost
	 *            the proxy host name or IP
	 * @param proxyPort
	 *            the proxy port number
	 * @param connectionTimeout
	 *            connection timeout in milliseconds
	 * @param soTimeout
	 *            socket time in milliseconds
	 * @param maxTotalConnections
	 *            maximum number of connections can be pooled
	 * @param maxHostConnections
	 *            maximum number of connections per host in the pool
	 * @param httpPerformanceMonitor
	 *            the HttpPerformanceMonitor
	 */
	public HttpParameters(boolean useProxy, String proxyHost, int proxyPort,
			int connectionTimeout, int soTimeout, int maxTotalConnections,
			int maxHostConnections,
			HttpPerformanceMonitor httpPerformanceMonitor)
	{
		this.useProxy = useProxy;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.connectionTimeout = connectionTimeout;
		this.soTimeout = soTimeout;
		this.maxTotalConnections = maxTotalConnections;
		this.maxHostConnections = maxHostConnections;
		this.httpPerformanceMonitor = httpPerformanceMonitor;
	}

	/**
	 * Returns the url.
	 * 
	 * @return the url
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url)
	{
		this.url = url;
	}

	/**
	 * Returns the useProxy.
	 * 
	 * @return the useProxy
	 */
	public boolean isUseProxy()
	{
		return useProxy;
	}

	/**
	 * Sets the useProxy.
	 * 
	 * @param useProxy
	 *            the useProxy to set
	 */
	public void setUseProxy(boolean useProxy)
	{
		this.useProxy = useProxy;
	}

	/**
	 * Returns the proxyHost.
	 * 
	 * @return the proxyHost
	 */
	public String getProxyHost()
	{
		return proxyHost;
	}

	/**
	 * Sets the proxyHost.
	 * 
	 * @param proxyHost
	 *            the proxyHost to set
	 */
	public void setProxyHost(String proxyHost)
	{
		this.proxyHost = proxyHost;
		if(this.proxyHost != null)
			useProxy = true;
	}

	/**
	 * Returns the proxyPort.
	 * 
	 * @return the proxyPort
	 */
	public int getProxyPort()
	{
		return proxyPort;
	}

	/**
	 * Sets the proxyPort.
	 * 
	 * @param proxyPort
	 *            the proxyPort to set
	 */
	public void setProxyPort(int proxyPort)
	{
		this.proxyPort = proxyPort;
	}

	/**
	 * Returns the connectionTimeout.
	 * 
	 * @return the connectionTimeout
	 */
	public int getConnectionTimeout()
	{
		return connectionTimeout;
	}

	/**
	 * Sets the connectionTimeout.
	 * 
	 * @param connectionTimeout
	 *            the connectionTimeout to set
	 */
	public void setConnectionTimeout(int connectionTimeout)
	{
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * Returns the soTimeout.
	 * 
	 * @return the soTimeout
	 */
	public int getSoTimeout()
	{
		return soTimeout;
	}

	/**
	 * Sets the soTimeout.
	 * 
	 * @param soTimeout
	 *            the soTimeout to set
	 */
	public void setSoTimeout(int soTimeout)
	{
		this.soTimeout = soTimeout;
	}

	/**
	 * Returns the maxTotalConnections.
	 * 
	 * @return the maxTotalConnections
	 */
	public int getMaxTotalConnections()
	{
		return maxTotalConnections;
	}

	/**
	 * Sets the maxTotalConnections.
	 * 
	 * @param maxTotalConnections
	 *            the maxTotalConnections to set
	 */
	public void setMaxTotalConnections(int maxTotalConnections)
	{
		this.maxTotalConnections = maxTotalConnections;
	}

	/**
	 * Returns the maxHostConnections.
	 * 
	 * @return the maxHostConnections
	 */
	public int getMaxHostConnections()
	{
		return maxHostConnections;
	}

	/**
	 * Sets the maxHostConnections.
	 * 
	 * @param maxHostConnections
	 *            the maxHostConnections to set
	 */
	public void setMaxHostConnections(int maxHostConnections)
	{
		this.maxHostConnections = maxHostConnections;
	}

	/**
	 * Returns the httpPerformanceMonitor.
	 * 
	 * @return the httpPerformanceMonitor
	 */
	public HttpPerformanceMonitor getHttpPerformanceMonitor()
	{
		return httpPerformanceMonitor;
	}

	/**
	 * Sets the httpPerformanceMonitor.
	 * 
	 * @param httpPerformanceMonitor
	 *            the httpPerformanceMonitor to set
	 */
	public void setHttpPerformanceMonitor(
			HttpPerformanceMonitor httpPerformanceMonitor)
	{
		this.httpPerformanceMonitor = httpPerformanceMonitor;
	}
	
	
	/**
	 * Returns the usernamePasswordCredentials.
	 * 
	 * @return the usernamePasswordCredentials
	 */
	public UsernamePasswordCredentials getUsernamePasswordCredentials() {
		return usernamePasswordCredentials;
	}

	/**
	 * Sets the usernamePasswordCredentials.
	 * 
	 * @param usernamePasswordCredentials
	 *            the usernamePasswordCredentials to set
	 */
	public void setUsernamePasswordCredentials(
			UsernamePasswordCredentials usernamePasswordCredentials) {
		this.usernamePasswordCredentials = usernamePasswordCredentials;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * Returns the string representation of this class.
	 * 
	 * @return the string representation of this class
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("HttpParameters[connectionTimeout = ");
		builder.append(connectionTimeout);
		builder.append(", httpPerformanceMonitor = ");
		builder.append(httpPerformanceMonitor);
		builder.append(", maxHostConnections = ");
		builder.append(maxHostConnections);
		builder.append(", maxTotalConnections = ");
		builder.append(maxTotalConnections);
		builder.append(", proxyHost = ");
		builder.append(proxyHost);
		builder.append(", proxyPort = ");
		builder.append(proxyPort);
		builder.append(", soTimeout = ");
		builder.append(soTimeout);
		builder.append(", url = ");
		builder.append(url);
		builder.append(", useProxy = ");
		builder.append(useProxy);
		builder.append("]");
		return builder.toString();
	}
}
