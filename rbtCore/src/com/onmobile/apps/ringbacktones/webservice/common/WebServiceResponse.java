package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.HashMap;
import java.util.Map;

import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;

/**
 * @author sridhar.sindiri
 *
 */
public class WebServiceResponse 
{
	private String response = null;
	private String contentType = null;
	private ResponseWriter responseWriter = null;
	private Map<String, String> responseHeaders = new HashMap<String, String>();

	/**
	 * @param response
	 */
	public WebServiceResponse(String response)
	{
		this.response = response;
	}

	/**
	 * @return the response
	 */
	public String getResponse()
	{
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(String response)
	{
		this.response = response;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() 
	{
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	/**
	 * @return the responseWriter
	 */
	public ResponseWriter getResponseWriter() 
	{
		return responseWriter;
	}

	/**
	 * @param responseWriter the responseWriter to set
	 */
	public void setResponseWriter(ResponseWriter responseWriter) 
	{
		this.responseWriter = responseWriter;
	}

	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	public void addResponseHeader(String header, String headerValue) {
		responseHeaders.put(header, headerValue);
	}
	
	public void removeHeader(String header) {
		responseHeaders.remove(header);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceResponse [response=");
		builder.append(response);
		builder.append(", contentType=");
		builder.append(contentType);
		builder.append(", responseWriter=");
		builder.append(responseWriter);
		builder.append("]");
		return builder.toString();
	}
}
