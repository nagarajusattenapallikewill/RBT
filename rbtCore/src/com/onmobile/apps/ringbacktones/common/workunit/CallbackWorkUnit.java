package com.onmobile.apps.ringbacktones.common.workunit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.CallbackRequest;

public class CallbackWorkUnit extends WorkUnit
{
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	private CallbackRequest callbackRequest;
	
	
	public CallbackWorkUnit(HttpServletRequest request, HttpServletResponse response)
	{
		super();
		setHttpServletRequest(request);
		setHttpServletResponse(response);
	}
	
	
	/**
	 * @return the httpServletRequest
	 */
	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}
	/**
	 * @param httpServletRequest the httpServletRequest to set
	 */
	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}
	/**
	 * @return the httpServletResponse
	 */
	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}
	/**
	 * @param httpServletResponse the httpServletResponse to set
	 */
	public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
	}
	/**
	 * @return the callbackRequest
	 */
	public CallbackRequest getCallbackRequest() {
		return callbackRequest;
	}
	/**
	 * @param callbackRequest the callbackRequest to set
	 */
	public void setCallbackRequest(CallbackRequest callbackRequest) {
		this.callbackRequest = callbackRequest;
	}
	
}
