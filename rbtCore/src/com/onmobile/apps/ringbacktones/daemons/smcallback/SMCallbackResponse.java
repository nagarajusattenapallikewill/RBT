/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.smcallback;

/**
 * @author vinayasimha.patil
 */
public class SMCallbackResponse
{
	private String response = null;

	/**
	 * @param response
	 */
	public SMCallbackResponse(String response)
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
	 * @param response
	 *            the response to set
	 */
	public void setResponse(String response)
	{
		this.response = response;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SMCallbackResponse [response=");
		builder.append(response);
		builder.append("]");
		return builder.toString();
	}
}
