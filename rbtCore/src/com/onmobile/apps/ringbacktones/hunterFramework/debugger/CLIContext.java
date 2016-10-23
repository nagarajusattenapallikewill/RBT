package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

import java.io.PrintStream;
import java.util.List;

public class CLIContext
{
	/**
	 * Used to print line break in client's CLI.
	 */
	private static final String LINE_BREAK = "===================================================";

	/**
	 * Holds the reference of OutputStream of client socket.
	 */
	private PrintStream out = null;

	/**
	 * Returns the outputStream of client socket.
	 * 
	 * @return the out
	 */
	public PrintStream getOutputStream()
	{
		return out;
	}

	/**
	 * Sets the outputStream of client socket.
	 * 
	 * @param out
	 *            the out to set
	 */
	public void setOutputStream(PrintStream out)
	{
		this.out = out;
	}

	/**
	 * Writes {@link CLIClient#END_OF_SERVER_TRANSMISSION} to the output stream.
	 */
	public void endOfServerTransmission()
	{
		out.print(CLIClient.END_OF_SERVER_TRANSMISSION);
	}

	/**
	 * Writes the {@link #LINE_BREAK} and data to the client socket. Each row is
	 * represented as <i>key: value</i> format.
	 * 
	 * @param displayData
	 *            list of output data in key-value pair
	 */
	public void writeNextRow(List<DisplayData> displayData)
	{
		writeLineBreak();

		for (DisplayData data : displayData)
		{
			out.println(data.getKey() + ": " + data.getValue());
		}
	}

	/**
	 * Writes {@link #LINE_BREAK} to the output stream.
	 */
	public void writeLineBreak()
	{
		out.println(LINE_BREAK);
	}
}
