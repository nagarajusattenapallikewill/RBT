package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

import java.lang.reflect.Field;

import com.onmobile.apps.ringbacktones.common.StringUtil;

/**
 * Utility class to parse the CLI command and build the appropriate
 * {@link CLICommand}.
 * 
 * @author vinayasimha.patil
 */
public class CLICommandParser
{
	/**
	 * Package name of {@link CLICommand} implementation classes.
	 */
	public static final String CLI_COMMANDS_PACKAGE = "com.onmobile.apps.ringbacktones.hunterFramework.debugger.cliCommands";

	/**
	 * Parses the command, builds the {@link CLICommand} object.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} holding the client socket details
	 * @param command
	 *            the command input from the client
	 * @return the parsed {@link CLICommand} object
	 * @throws CommandException
	 *             If unable to parse the command
	 */
	public static CLICommand parseCommand(CLIContext cliContext, String command)
			throws CommandException
	{
		String commandTokens[] = command.trim().split(" ");
		if (commandTokens != null && commandTokens.length > 0)
		{
			// First token in the command string will be the Command name and
			// the implementation class will be loaded with same name using
			// reflection APIs.
			String className = commandTokens[0];
			className = StringUtil.toUpperCaseOnlyFirstChar(className);
			className = CLI_COMMANDS_PACKAGE + "." + className;

			String[] fieldValue = null;
			try
			{
				@SuppressWarnings("unchecked")
				Class<CLICommand> cliCommandClass = (Class<CLICommand>) Class
						.forName(className);
				CLICommand cliCommand = cliCommandClass.newInstance();

				for (int i = 1; i < commandTokens.length; i++)
				{
					// Each CLICommand will have set of parameters. So, command
					// implementation class should have fields representing the
					// command parameters. Values assigned to the fields using
					// reflection APIs.
					fieldValue = commandTokens[i].split("=");
					Field field = cliCommandClass
							.getDeclaredField(fieldValue[0].toLowerCase());
					CLIField annotation = field.getAnnotation(CLIField.class);
					if (annotation != null)
					{
						// Making sure that field is representing the CLI
						// Command parameter
						field.setAccessible(true);
						field.set(cliCommand, fieldValue[1]);
					}
					else
						throw new CommandException("Unknown Parameter: "
								+ fieldValue[0]);
				}

				return cliCommand;
			}
			catch (ClassNotFoundException e)
			{
				throw new CommandException("Unknown Command: "
						+ commandTokens[0]);
			}
			catch (NoSuchFieldException e)
			{
				String errorMessage = "Unknown Parameter: ";
				if (fieldValue != null && fieldValue.length > 0)
					errorMessage += fieldValue[0];

				throw new CommandException(errorMessage);
			}
			catch (Exception e)
			{
				throw new CommandException("Error in parsing the command");
			}
		}
		else
			throw new CommandException("Unknown Command");
	}
}
