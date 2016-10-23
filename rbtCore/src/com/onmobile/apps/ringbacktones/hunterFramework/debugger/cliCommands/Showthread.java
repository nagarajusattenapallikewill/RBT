/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.debugger.cliCommands;

import java.util.ArrayList;
import java.util.List;

import com.onmobile.apps.ringbacktones.hunterFramework.ManagedDaemon;
import com.onmobile.apps.ringbacktones.hunterFramework.ThreadManager;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLICommand;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLIContext;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLIField;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.CommandException;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.DisplayData;

/**
 * {@link CLICommand} implementation for the <i>showthread</i> command of Hunter
 * Framework. This class has only one field <tt>(threadid)</tt>. Details of the
 * any {@link ManagedDaemon} can be get by passing the <tt>threadid</tt> to this
 * command.
 * 
 * @author vinayasimha.patil
 */
public class Showthread implements CLICommand
{
	/**
	 * Representing threadid parameter of <i>showthread</i> CLICommand. It is
	 * annotated by {@link CLIField}. This field should have the valid unique
	 * name of a {@link ManagedDaemon}.
	 */
	@CLIField
	private String threadid = null;

	/**
	 * Contains the short help message explaining the use of this command. This
	 * is used by {@link #getShortHelpMessage()} method.
	 */
	public static String shortHelpMessage = null;
	
	/**
	 * Contains the long help message explaining the parameters of this command.
	 * This is used by {@link #getLongHelpMessage()} method.
	 */
	public static String longHelpMessage = null;
	
	static
	{
		// Initializing the help messages
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("showthread: By this command thread stack trace of any Managed Daemon can be");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("            viewed.");

		shortHelpMessage = stringBuilder.toString();

		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Format:");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("-------");
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("\t");
		stringBuilder.append("showthread server=<host>:<port> [<parameter>=<value> ...]");

		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Parameters:");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("-----------");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("server: Debug Daemon server IP and port has to given in <host>:<port> format.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("threadid: Unique Name of the managed thread.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Examples:");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("---------");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Getting thread dump");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append("showthread server=localhost:7575 threadid=DirectCopy_LOCAL_Publisher");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		longHelpMessage = stringBuilder.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLICommand#execute
	 * (com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLIContext)
	 */
	/**
	 * Processes the <i>showthread</i> command.
	 */
	public void execute(CLIContext cliContext) throws CommandException
	{
		try
		{
			sendThreadStackTrace(cliContext);
			cliContext.writeLineBreak();
		}
		catch (CommandException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			throw new CommandException("Unable to process the request");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLICommand#
	 * getShortHelpMessage()
	 */
	public String getShortHelpMessage()
	{
		return shortHelpMessage;
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLICommand#
	 * getLongHelpMessage()
	 */
	public String getLongHelpMessage()
	{
		return longHelpMessage;
	}

	/**
	 * Sends the thread stack trace for the requested {@link ManagedDaemon}
	 * thread.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} of this request
	 * @throws CommandException
	 *             If there is no {@link ManagedDaemon} thread exists with name
	 *             of requested <tt>threadid</tt>
	 */
	private void sendThreadStackTrace(CLIContext cliContext)
			throws CommandException
	{
		String stackTrace = ThreadManager.getThreadManager().getStackTrace(
				threadid);
		if (stackTrace == null)
			throw new CommandException("Invalid thread id: " + threadid);

		List<DisplayData> displayData = new ArrayList<DisplayData>();
		displayData.add(new DisplayData(threadid, stackTrace));
		cliContext.writeNextRow(displayData);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * Returns the string representation of this class.
	 * 
	 * @return the string representation of this class
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Showthread[threadid = ");
		builder.append(threadid);
		builder.append("]");
		return builder.toString();
	}
}
