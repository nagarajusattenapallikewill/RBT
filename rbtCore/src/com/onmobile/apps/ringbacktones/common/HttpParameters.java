package com.onmobile.apps.ringbacktones.common;

public class HttpParameters
{
	private String strUrl;
	private boolean bHasProxy = false;
	private String strProxyHost;
	private int iProxyPort;
	private int iConnectionTimeout = 6000;
	private int iDataTimeout = 6000;
	private boolean paramsAsParts = false;

	/**
	 * 
	 */
	public HttpParameters()
	{

	}
	
	/**
	 * @param strUrl
	 */
	public HttpParameters(String strUrl)
	{
		super();
		this.strUrl = strUrl;
	}

	/**
	 * @param strUrl
	 * @param hasProxy
	 * @param strProxyHost
	 * @param proxyPort
	 * @param connectionTimeout
	 * @param dataTimeout
	 * @param paramsAsParts
	 */
	public HttpParameters(String strUrl, boolean hasProxy, String strProxyHost,
			int proxyPort, int connectionTimeout, int dataTimeout,
			boolean paramsAsParts) {
		this.strUrl = strUrl;
		bHasProxy = hasProxy;
		this.strProxyHost = strProxyHost;
		iProxyPort = proxyPort;
		iConnectionTimeout = connectionTimeout;
		iDataTimeout = dataTimeout;
		this.paramsAsParts = paramsAsParts;
	}

	public String getUrl() {
		return strUrl;
	}

	public void setUrl(String url) {
		this.strUrl = url;
	}

	public boolean getHasProxy() {
		return bHasProxy;
	}

	public void setHasProxy(boolean hasProxy) {
		this.bHasProxy = hasProxy;
	}

	public String getProxyHost() {
		return strProxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.strProxyHost = proxyHost;
	}

	public int getProxyPort() {
		return iProxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.iProxyPort = proxyPort;
	}

	public int getConnectionTimeout() {
		return iConnectionTimeout;
	}

	public void setConnectionTimeout(int timeout) {
		this.iConnectionTimeout = timeout;
	}

	public int getDataTimeout() {
		return iDataTimeout;
	}

	public void setDataTimeout(int datatimeout) {
		this.iDataTimeout = datatimeout;
	}

	public boolean getParamsAsParts() {
		return paramsAsParts;
	}

	public void setParamsAsParts(boolean value) {
		paramsAsParts = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String string = "HttpParameters{strUrl = " + strUrl + ", bHasProxy = "
				+ bHasProxy + ", strProxyHost = " + strProxyHost
				+ ", iProxyPort = " + iProxyPort + ", iConnectionTimeout = "
				+ iConnectionTimeout + ", iDataTimeout = " + iDataTimeout
				+ ", paramsAsParts = " + paramsAsParts + "}";

		return string;
	}
}