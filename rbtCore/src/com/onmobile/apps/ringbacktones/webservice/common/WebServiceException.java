/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

/**
 * @author vinayasimha.patil
 * 
 */
public class WebServiceException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2902397902057941324L;

	private int responseCode;
	private String responseString = null;

	/**
	 * @param message
	 * @param responseCode
	 * @param responseString
	 */
	public WebServiceException(String message, int responseCode,
			String responseString)
	{
		super(message);
		this.responseCode = responseCode;
		this.responseString = responseString;
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
	 * @return the responseString
	 */
	public String getResponseString()
	{
		return responseString;
	}

	/**
	 * @param responseString
	 *            the responseString to set
	 */
	public void setResponseString(String responseString)
	{
		this.responseString = responseString;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceException [responseCode=");
		builder.append(responseCode);
		builder.append(", responseString=");
		builder.append(responseString);
		builder.append(", getMessage()=");
		builder.append(getMessage());
		builder.append("]");
		return builder.toString();
	}

}
