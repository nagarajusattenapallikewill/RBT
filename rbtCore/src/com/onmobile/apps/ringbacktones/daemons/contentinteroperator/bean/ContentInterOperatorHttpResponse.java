package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean;

import java.util.Arrays;

import org.apache.commons.httpclient.Header;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorHttpResponse
{

	private int httpResponseCode = 0;
	private Header[] httpHeaders = null;
	private String httpResponseString = null;
	private String finalUrl = null;

	/**
	 * @return the httpResponseCode
	 */
	public int getHttpResponseCode()
	{
		return httpResponseCode;
	}

	/**
	 * @param httpResponseCode the httpResponseCode to set
	 */
	public void setHttpResponseCode(int httpResponseCode)
	{
		this.httpResponseCode = httpResponseCode;
	}

	/**
	 * @return the httpResponseString
	 */
	public String getHttpResponseString()
	{
		return httpResponseString;
	}

	/**
	 * @return the httpHeaders
	 */
	public Header[] getHttpHeaders()
	{
		return httpHeaders;
	}

	/**
	 * @param httpHeaders the httpHeaders to set
	 */
	public void setHttpHeaders(Header[] httpHeaders)
	{
		this.httpHeaders = httpHeaders;
	}

	/**
	 * @param httpResponseString the httpResponseString to set
	 */
	public void setHttpResponseString(String httpResponseString)
	{
		this.httpResponseString = httpResponseString;
	}

	/**
	 * @param finalUrl the finalUrl to set
	 */
	public void setFinalUrl(String finalUrl)
	{
		this.finalUrl = finalUrl;
	}

	/**
	 * @return the finalUrl
	 */
	public String getFinalUrl()
	{
		return finalUrl;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ContentInterOperatorHttpResponse [httpResponseCode=")
		.append(httpResponseCode).append(", httpResponseString=")
		.append(httpResponseString).append(", httpHeaders=")
		.append(Arrays.toString(httpHeaders)).append("]");

		return builder.toString();
	}
}
