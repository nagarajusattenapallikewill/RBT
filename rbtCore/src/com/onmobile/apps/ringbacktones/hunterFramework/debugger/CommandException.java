/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

/**
 * Thrown to indicate that a client has been passed as invalid command.
 * 
 * @author vinayasimha.patil
 */
public class CommandException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6907664181856204611L;

	/**
	 * Constructs a new CommandException with <code>null</code> as its detail
	 * message.
	 */
	public CommandException()
	{
		super();
	}

	/**
	 * Constructs a new CommandException with the specified detail message.
	 * 
	 * @param message
	 *            the detail message
	 */
	public CommandException(String message)
	{
		super(message);
	}
}
