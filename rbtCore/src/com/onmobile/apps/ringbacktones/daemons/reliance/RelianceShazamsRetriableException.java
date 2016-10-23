/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.reliance;

/**
 * @author vinayasimha.patil
 * 
 */
public class RelianceShazamsRetriableException extends RelianceShazamsException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3510557433255001349L;

	/**
	 * @param message
	 */
	public RelianceShazamsRetriableException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RelianceShazamsRetriableException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
