package com.onmobile.apps.ringbacktones.daemons.interoperator.bean;

import java.util.Arrays;

import org.apache.commons.httpclient.Header;

public class InterOperatorHttpResponse
{

	private int httpResponseCode = 0;
	private Header[] httpHeaders = null;
	private String httpResponseString = null;
	private String finalUrl = null;
	
	public int getHttpResponseCode() {
		return httpResponseCode;
	}
	public void setHttpResponseCode(int httpResponseCode) {
		this.httpResponseCode = httpResponseCode;
	}
	
	public String getHttpResponseString() {
		return httpResponseString;
	}
	public Header[] getHttpHeaders() {
		return httpHeaders;
	}
	public void setHttpHeaders(Header[] httpHeaders) {
		this.httpHeaders = httpHeaders;
	}
	public void setHttpResponseString(String httpResponseString) {
		this.httpResponseString = httpResponseString;
	}
	public void setFinalUrl(String finalUrl) {
		this.finalUrl = finalUrl;
	}
	public String getFinalUrl() {
		return finalUrl;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InterOperatorHttpResponse [httpResponseCode=").append(
				httpResponseCode).append(", httpResponseString=").append(
				httpResponseString).append(", httpHeaders=").append(
				Arrays.toString(httpHeaders)).append("]");
		return builder.toString();
	}
	
}
	