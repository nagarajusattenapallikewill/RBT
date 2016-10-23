package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * DataHandler is a {@link Thread}. This class processes the client request.
 * 
 * @author vinayasimha.patil
 */
public class DataHandler extends Thread
{
	private static Logger logger = Logger.getLogger(DataHandler.class);
	/**
	 * Client socket instance.
	 */
	private Socket socket = null;

	/**
	 * Constructs the DataHandler and starts the processing of client request.
	 * 
	 * @param socket
	 *            the client socket
	 */
	public DataHandler(Socket socket)
	{
		this.socket = socket;
		start();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	/**
	 * Parses and process the client request.
	 */
	@Override
	public void run()
	{
		CLIContext cliContext = new CLIContext();
		try
		{
			cliContext
					.setOutputStream(new PrintStream(socket.getOutputStream()));

			String command = readCommand();
			CLICommand cliCommand = CLICommandParser.parseCommand(cliContext,
					command);
			cliCommand.execute(cliContext);
		}
		catch (CommandException e)
		{
			PrintStream out = cliContext.getOutputStream();
			if (out != null)
				out.println(e.getMessage());
		}
		catch (Throwable e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				cliContext.endOfServerTransmission();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * Reads the command byte by byte from the client <tt>socket</tt>.
	 * 
	 * @return the command sent by client
	 * @throws IOException
	 *             If IO error occurs while reading from the <tt>socket</tt>
	 */
	private String readCommand() throws IOException
	{
		StringBuilder command = new StringBuilder();

		InputStream inputStream = socket.getInputStream();
		byte data[] = new byte[1];
		while (true)
		{
			int length = inputStream.read(data);
			if (length == -1 || data[0] == CLIClient.END_OF_CLIENT_TRANSMISSION)
				break;

			command.append((char) data[0]);
		}

		return command.toString();
	}
}
