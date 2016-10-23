package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.onmobile.apps.ringbacktones.common.StringUtil;

/**
 * This class provides the command line interface for the accessing the status
 * information of the RBT hunters.
 * 
 * @author vinayasimha.patil
 */
public class CLIClient
{
	/**
	 * Constant representing the parameter name of server details in CLI
	 * Command.
	 */
	public static final String SERVER_DETAILS = "server";

	/**
	 * Constant represent the End of Server Transmission ('' [\u0004]).
	 */
	// ASCII End of Transmission character
	public static final char END_OF_SERVER_TRANSMISSION = 0x4;

	// Not using the same termination character as Server, because if telnet is
	// used as client its difficult for user to enter the ASCII End of
	// Transmission character
	/**
	 * Constant represent the End of Client Transmission ('\n').
	 */
	public static final char END_OF_CLIENT_TRANSMISSION = '\n';

	/**
	 * Holds the command name and parameters.
	 */
	private String[] commandTokens = null;

	/**
	 * Holds the IP Address of server.
	 */
	private String host = null;

	/**
	 * Holds the port number of server.
	 */
	private int port = -1;

	/**
	 * Client socket instance.
	 */
	private Socket socket = null;

	/**
	 * Output stream connecting client-server.
	 */
	private PrintStream out = null;

	/**
	 * Constructs the CLIClient with the given <tt>commandString</tt>.
	 * 
	 * @param commandTokens
	 *            the command input in the <i>'{&lt;commandName&gt;,
	 *            server=&lt;host&gt;:&ltport&gt, &lt;param1&gt;=&lt;value1&gt;,
	 *            ... , &lt;paramN&gt;=&lt;valueN&gt;}'</i> format
	 */
	public CLIClient(String[] commandTokens)
	{
		this.commandTokens = commandTokens;
	}

	/**
	 * Main method start the CLI client. Command Line Arguments (<tt>args</tt>)
	 * has to be in below format:
	 * <ul>
	 * <li>First argument has to be command name like <i>showdetails</i></li>
	 * <li>Second argument has to be server details in
	 * <i>'server=&lt;host&gt;:&lt;port&gt;'</i> format</li>
	 * <li>And any number of arguments in
	 * <i>'&lt;parameter&gt;=&lt;value&gt;'</i> format</li>
	 * </ul>
	 * 
	 * @param args
	 *            user command details
	 */
	public static void main(String[] args)
	{
		CLIClient cliClient = new CLIClient(args);
		try
		{
			cliClient.execute();
		}
		catch (CLIException e)
		{
			System.out.println("Error in processing the command: "
					+ e.getMessage());
		}
	}

	/**
	 * Takes care of sending the command, receiving the data and printing data
	 * to console. If requested for help then help message will be displayed
	 * without sending the command to server.
	 * 
	 * @throws CLIException
	 *             If failed to send the command to server
	 */
	public void execute() throws CLIException
	{
		try
		{
			if (isHelpRequest())
			{
				diplayHelpMessage();
			}
			else
			{
				resolveServerDetails();
				connect();
				sendCommand();
				receiveOutputData();
			}
		}
		finally
		{
			try
			{
				if (socket != null)
					socket.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if this is a help request. If <i>help</i> parameter
	 * passed with the command or if only command name is passed then this
	 * request will be treated as help request.
	 * 
	 * @return <tt>true</tt> if this is a help request.
	 */
	private boolean isHelpRequest()
	{
		if (commandTokens.length == 0)
		{
			// As only command name(without parameters) passed thats why treating it as help request.
			return true;
		}

		for (String commandToken : commandTokens)
		{
			if (commandToken.toLowerCase().contains("help"))
				return true;
		}

		return false;
	}

	/**
	 * Displays the help message to the console.
	 * 
	 * @throws CLIException
	 *             if CLI Commands does not exist
	 */
	private void diplayHelpMessage() throws CLIException
	{
		try
		{
			String packageName = CLICommandParser.CLI_COMMANDS_PACKAGE;

			if (commandTokens.length == 0
					|| (commandTokens[0].toLowerCase().contains("help")))
			{
				Class<?>[] classes = CLICommand.allCliCommands;
				for (Class<?> classObj : classes)
				{
					Object object = classObj.newInstance();
					if (object instanceof CLICommand)
					{
						System.out.println(((CLICommand) object)
								.getShortHelpMessage());
					}
				}
			}
			else
			{
				String className = commandTokens[0];
				className = StringUtil.toUpperCaseOnlyFirstChar(className);
				className = packageName + "." + className;

				@SuppressWarnings("unchecked")
				Class<CLICommand> cliCommandClass = (Class<CLICommand>) Class
						.forName(className);
				CLICommand cliCommand = cliCommandClass.newInstance();
				System.out.println(cliCommand.getLongHelpMessage());
			}
		}
		catch (Exception e)
		{
			throw new CLIException(e.getMessage());
		}
	}

	/**
	 * Resolves the server details. Populates the details in <tt>host</tt> and
	 * <tt>port</tt> fields.
	 * 
	 * @throws CLIException
	 *             If not able to find the server details or if wrong data
	 *             format of server details
	 */
	private void resolveServerDetails() throws CLIException
	{
		if (commandTokens != null)
		{
			for (String commandToken : commandTokens)
			{
				commandToken = commandToken.toLowerCase().trim();
				if (commandToken.startsWith(SERVER_DETAILS + "="))
				{
					String serverInfo[] = commandToken.split("=");
					if (serverInfo.length != 2)
					{
						throw new CLIException("The '" + SERVER_DETAILS
								+ "' parameter should  be in the format '"
								+ SERVER_DETAILS + "=<host>:<port>'.");
					}

					String serverPort[] = serverInfo[1].split(":");
					if (serverPort.length != 2)
					{
						throw new CLIException("The '" + SERVER_DETAILS
								+ "' parameter should  be in the format '"
								+ "<host name>:<port>'.");
					}

					host = serverPort[0];
					port = StringUtil.getInteger(serverPort[1]);
					if (port == -1)
						throw new CLIException("Invalid port number");

					return;
				}
			}
		}

		throw new CLIException("Could not find the '" + SERVER_DETAILS
				+ "' parameter.");
	}

	/**
	 * Connects to the remove server.
	 * 
	 * @throws CLIException
	 *             If unable to connect to the remote server
	 */
	private void connect() throws CLIException
	{
		try
		{
			socket = new Socket(host, port);
			out = new PrintStream(socket.getOutputStream());
		}
		catch (UnknownHostException e)
		{
			throw new CLIException("Could not connect to '" + host
					+ "' at port " + port);
		}
		catch (IOException e)
		{
			throw new CLIException("Could not connect to '" + host
					+ "' at port " + port);
		}
	}

	/**
	 * Sends the <tt>command</tt> to remote server. And also appends the
	 * {@link #END_OF_CLIENT_TRANSMISSION} to represent the end of transmission.
	 */
	private void sendCommand()
	{
		StringBuilder command = new StringBuilder();
		for (int i = 0; i < commandTokens.length; i++)
		{
			if (commandTokens[i].startsWith(SERVER_DETAILS + "="))
			{
				// Ignoring server details as its not required to send.
				continue;
			}

			if (i != 0)
				command.append(" ");

			command.append(commandTokens[i]);
		}

		out.print(command);
		out.print(END_OF_CLIENT_TRANSMISSION);
	}

	/**
	 * Receives data sent by the server as output for the command sent by this
	 * client and streams the output to console.
	 * 
	 * @throws CLIException
	 *             If failed to
	 */
	private void receiveOutputData() throws CLIException
	{
		try
		{
			PrintStream outputStream = System.out;
			InputStream inputStream = socket.getInputStream();

			byte data[] = new byte[128];
			boolean streamEnded = false;
			while (!streamEnded)
			{
				int length = inputStream.read(data);
				if (length == -1)
					break;

				if (data[length - 1] == END_OF_SERVER_TRANSMISSION)
				{
					length--;
					streamEnded = true;
				}

				outputStream.write(data, 0, length);
			}
		}
		catch (IOException e)
		{
			throw new CLIException("Failed to rescive data");
		}
	}
}
