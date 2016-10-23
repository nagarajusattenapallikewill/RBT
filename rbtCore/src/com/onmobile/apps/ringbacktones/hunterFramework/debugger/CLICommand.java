package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

import com.onmobile.apps.ringbacktones.hunterFramework.debugger.cliCommands.Showdetails;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.cliCommands.Showthread;

/**
 * Represents interface for all CLI Command implementations.
 * 
 * @author vinayasimha.patil
 */
public interface CLICommand
{
	/**
	 * Processes the command and writes back the result to output stream.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} having the details of this command
	 * @throws CommandException
	 *             If unable to process the request
	 */
	public void execute(CLIContext cliContext) throws CommandException;

	/**
	 * Returns the short help message explaining the use of this command. This
	 * help message is used when requested for the list of available commands.
	 * 
	 * @return the short help message explaining the use of this command
	 */
	public String getShortHelpMessage();

	/**
	 * Returns the long help message explaining the parameters of this command.
	 * This help message is used when requested for the help of this command
	 * only.
	 * 
	 * @return the long help message explaining the parameters of this command
	 */
	public String getLongHelpMessage();

	/**
	 * All implemented CLICommands
	 */
	public static final Class<?>[] allCliCommands = { Showdetails.class,
			Showthread.class };
}
