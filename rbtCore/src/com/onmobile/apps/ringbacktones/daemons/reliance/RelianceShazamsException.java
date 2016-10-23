/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.reliance;

/**
 * @author vinayasimha.patil
 * 
 */
public class RelianceShazamsException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3296861268406479153L;

	/**
	 * @param message
	 */
	public RelianceShazamsException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RelianceShazamsException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
