package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

/**
 * Thrown to indicate that a {@link CLIClient} is not able to send the request
 * to server. Reasons like not able to find the server details.
 * 
 * @author vinayasimha.patil
 */
public class CLIException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3989903782420609457L;

	/**
	 * Constructs a new CLIException with <code>null</code> as its detail
	 * message.
	 */
	public CLIException()
	{
		super();
	}

	/**
	 * Constructs a new CommandException with the specified detail message.
	 * 
	 * @param message
	 *            the detail message
	 */
	public CLIException(String message)
	{
		super(message);
	}
}
