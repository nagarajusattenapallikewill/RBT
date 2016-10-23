/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.Header;

/**
 * {@link RBTHttpClient} will return the data of http hit in the format of this
 * class. This class will hold the http status code, http response, http headers
 * and time
 * taken for the hit.
 * 
 * @author vinayasimha.patil
 */
public class HttpResponse
{
	/**
	 * Represents the http status code.
	 */
	private int responseCode;

	/**
	 * Holds the http response or absolute path of the file if a file downloaded
	 * by the hit.
	 */
	private String response = null;

	/**
	 * Holds the response headers.
	 */
	private Header[] responseHeaders = null;

	/**
	 * Time taken to complete the http hit in milliseconds.
	 */
	private long responseTime;

	/**
	 * Constructs the HttpResponse with <tt>responseCode</tt>, <tt>response</tt>
	 * and <tt>responseTime</tt>.
	 * 
	 * @param responseCode
	 * @param response
	 * @param responseHeaders
	 * @param responseTime
	 */
	public HttpResponse(int responseCode, String response,
			Header[] responseHeaders, long responseTime)
	{
		this.responseCode = responseCode;
		this.response = response;
		this.responseHeaders = responseHeaders;
		this.responseTime = responseTime;
	}

	/**
	 * @return the responseCode
	 */
	public int getResponseCode()
	{
		return responseCode;
	}

	/**
	 * @param responseCode
	 *            the responseCode to set
	 */
	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
	}

	/**
	 * @return the response
	 */
	public String getResponse()
	{
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(String response)
	{
		this.response = response;
	}

	/**
	 * @return the responseHeaders
	 */
	public Header[] getResponseHeaders()
	{
		return responseHeaders;
	}

	/**
	 * Returns the specified response header value. Note that header-name matching is
	 * case insensitive.
	 * 
	 * @param headerName
	 *            The name of the header to be returned.
	 * @return The specified response header value. If the response contained
	 *         multiple instances of the header, its values will be combined
	 *         using the ',' separator as specified by RFC2616.
	 */
	public String getResponseHeader(String headerName)
	{
		Header[] headers = getResponseHeaders(headerName);

		if (headers.length == 0)
		{
			return null;
		}
		else if (headers.length == 1)
		{
			return headers[0].getValue();
		}
		else
		{
			StringBuilder valueBuffer = new StringBuilder(headers[0].getValue());
			for (int i = 1; i < headers.length; i++)
			{
				valueBuffer.append(", ");
				valueBuffer.append(headers[i].getValue());
			}

			return valueBuffer.toString();
		}
	}

	/**
	 * Returns the response headers with the given name. Note that header-name
	 * matching is
	 * case insensitive.
	 * 
	 * @param headerName
	 *            the name of the headers to be returned.
	 * @return an array of zero or more headers
	 */
	public Header[] getResponseHeaders(String headerName)
	{
		List<Header> headersFound = new ArrayList<Header>();

		for (Header header : responseHeaders)
		{
			if (header.getName().equalsIgnoreCase(headerName))
				headersFound.add(header);
		}

		return headersFound.toArray(new Header[headersFound.size()]);
	}

	/**
	 * @param responseHeaders
	 *            the responseHeaders to set
	 */
	public void setResponseHeaders(Header[] responseHeaders)
	{
		this.responseHeaders = responseHeaders;
	}

	/**
	 * @return the responseTime
	 */
	public long getResponseTime()
	{
		return responseTime;
	}

	/**
	 * @param responseTime
	 *            the responseTime to set
	 */
	public void setResponseTime(long responseTime)
	{
		this.responseTime = responseTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("HttpResponse[response = ");
		builder.append(response);
		builder.append(", responseCode = ");
		builder.append(responseCode);
		builder.append(", responseHeaders = ");
		builder.append(Arrays.toString(responseHeaders));
		builder.append(", responseTime = ");
		builder.append(responseTime);
		builder.append("]");
		return builder.toString();
	}
}
