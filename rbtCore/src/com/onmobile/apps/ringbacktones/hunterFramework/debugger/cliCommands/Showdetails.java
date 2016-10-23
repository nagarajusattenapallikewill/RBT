/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.debugger.cliCommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.onmobile.apps.ringbacktones.common.StringUtil;
import com.onmobile.apps.ringbacktones.hunterFramework.Hunter;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterContainer;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressivePublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.Publisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContainer;
import com.onmobile.apps.ringbacktones.hunterFramework.QueuePerformance;
import com.onmobile.apps.ringbacktones.hunterFramework.WorkerThread;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLICommand;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLIContext;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.CLIField;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.CommandException;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.DisplayData;

/**
 * {@link CLICommand} implementation for the <i>showdetails</i> command of
 * Gatherer. This class handles five kinds of <i>showdetails</i> request:
 * <ul>
 * <li>Get Hunters List</li>
 * <li>Get details of all QueueContainer of a particular Hunter</li>
 * <li>Get details of particular QueueContainer</li>
 * <li>Get queue dump of a particular QueueContainer</li>
 * <li>Get worker threads dump of a particular QueueContainer</li>
 * </ul>
 * This class has five fields <tt>(hunter, site, dump, from and to)</tt>
 * representing the parameters of the <i>showdetails</i> command.
 * 
 * @author vinayasimha.patil
 */
public class Showdetails implements CLICommand
{
	/**
	 * Representing hunter parameter of <i>showdetails</i> CLICommand. It is
	 * annotated by {@link CLIField}. This field should have the valid Hunter
	 * name.
	 */
	@CLIField
	private String hunter = null;

	/**
	 * Representing site parameter of <i>showdetails</i> CLICommand. It is
	 * annotated by {@link CLIField}. This field should have the valid
	 * QueueContainer name.
	 */
	@CLIField
	private String site = null;

	/**
	 * Representing dump parameter of <i>showdetails</i> CLICommand. It is
	 * annotated by {@link CLIField}. Possible values for this field are
	 * <i>QUEUE</i> and <i>THREAD</i>.
	 */
	@CLIField
	private String dump = null;

	/**
	 * Representing from parameter of <i>showdetails</i> CLICommand. It is
	 * annotated by {@link CLIField}. This field should have the valid integer
	 * value.
	 */
	@CLIField
	private String from = null;

	/**
	 * Representing to parameter of <i>showdetails</i> CLICommand. It is
	 * annotated by {@link CLIField}. This field should have the valid integer
	 * value.
	 */
	@CLIField
	private String to = null;

	/**
	 * Constant for <tt>dump</tt> parameter, representing to get the queue dump.
	 */
	public static final String QUEUE_DUMP = "QUEUE";

	/**
	 * Constant for <tt>dump</tt> parameter, representing to get the worker
	 * threads dump.
	 */
	public static final String THREAD_DUMP = "THREAD";

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
		stringBuilder
				.append("showdetails: By this command all Hunters and its QueueContainers runtime");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("             information can be viewed.");

		shortHelpMessage = stringBuilder.toString();

		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Format:");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("-------");
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("\t");
		stringBuilder
				.append("showdetails server=<host>:<port> [<parameter>=<value> ...]");

		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Parameters:");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("-----------");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder
				.append("server: Debug Daemon server IP and port has to given in <host>:<port> format.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("hunter: Name of the Hunter.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder
				.append("site: Name of the QueueConatiner(site) of the Hunter specified in the 'hunter'");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder
				.append("      parameters. This parameter is valid only if 'hunter' parameter is passed.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder
				.append("dump: To get queue dump value has to be passed as 'QUEUE' and 'THREAD' for");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder
				.append("      getting thread dump. This parameter is valid only if 'site' parameter is");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("      passed.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder
				.append("from: By default queue dump returns first 50 components. If other than first 50");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder
				.append("      queue components required then start and end indexes has to be passed by");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder
				.append("      using 'from' and 'to' parameters. This parameter is valid only if");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("      'dump=QUEUE' parameter is passed.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder
				.append("to: By default queue dump returns first 50 components. If other than first 50");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder
				.append("    queue components required then start and end indexes has to be passed by");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder
				.append("    using 'from' and 'to' parameters. This parameter is valid only if 'dump=QUEUE'");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("    parameter is passed.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Examples:");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("---------");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Getting list of Hunters");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append("showdetails server=172.16.29.253:7575");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Getting details of a Hunter");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder
				.append("showdetails server=172.16.29.253:7575 hunter=DirectCopy");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Getting details of a QueueConatiner of a Hunter");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder
				.append("showdetails server=172.16.29.253:7575 hunter=DirectCopy site=LOCAL");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Getting Queue Dump");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder
				.append("showdetails server=172.16.29.253:7575 hunter=DirectCopy site=LOCAL dump=QUEUE");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder
				.append("showdetails server=172.16.29.253:7575 hunter=DirectCopy site=LOCAL dump=QUEUE from=51 to=75");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(System.getProperty("line.separator"));

		stringBuilder.append("Getting Thread Dump");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder
				.append("showdetails server=172.16.29.253:7575 hunter=DirectCopy site=LOCAL dump=THREAD");
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
	 * Processes the <i>showdetails</i> command.
	 */
	public void execute(CLIContext cliContext) throws CommandException
	{
		try
		{
			validateCommand(cliContext);

			if (hunter == null)
				sendHunterList(cliContext);
			else if (site == null)
				sendHunterDetails(cliContext);
			else if (dump == null)
				sendQueueContainerDetails(cliContext);
			else if (dump.equalsIgnoreCase(QUEUE_DUMP))
				sendQueueDump(cliContext);
			else if (dump.equalsIgnoreCase(THREAD_DUMP))
				sendThreadDump(cliContext);
			else
				throw new CommandException("Invalid 'dump' parameter: " + dump
						+ ". Possible values are '" + QUEUE_DUMP + "' and '"
						+ THREAD_DUMP + "'");

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
	 * Validates the command parameters.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} of this request
	 * @throws CommandException
	 *             If the parameters are invalid
	 */
	private void validateCommand(CLIContext cliContext) throws CommandException
	{
		if (hunter == null
				&& (site != null || dump != null || from != null || to != null))
		{
			throw new CommandException(
					"'site', 'dump', 'from' and 'to' parameters are invalid if 'hunter' parameter is missing");
		}
		else if (site == null && (dump != null || from != null || to != null))
		{
			throw new CommandException(
					"'dump', 'from' and 'to' parameters are invalid if 'site' parameter is missing");
		}
		else if ((dump == null || !dump.equalsIgnoreCase(QUEUE_DUMP))
				&& (from != null || to != null))
		{
			throw new CommandException(
					"'from' and 'to' parameters are invalid if 'dump' parameter is missing or if 'dump' parameter value is not '"
							+ QUEUE_DUMP + "'");
		}
		else if (from == null && to != null)
		{
			throw new CommandException(
					"'to' parameter is invalid if 'from' parameter is missing");
		}
	}

	/**
	 * Sends the list of hunter names to client.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} of this request
	 */
	private void sendHunterList(CLIContext cliContext)
	{
		HunterContainer hunterContainer = HunterContainer.getHunterContainer();
		HashMap<String, Hunter> huntersMap = hunterContainer.getHunters();

		List<DisplayData> displayData = new ArrayList<DisplayData>();

		Collection<Hunter> hunters = huntersMap.values();
		int i = 1;
		for (Hunter hunter : hunters)
		{
			displayData.add(new DisplayData(String.valueOf(i++), hunter
					.getHunterName()));
		}

		cliContext.writeNextRow(displayData);
	}

	/**
	 * Sends the all QueueContainer details of the requested hunter.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} of this request
	 * @throws CommandException
	 *             If the requested hunter does not exist
	 */
	private void sendHunterDetails(CLIContext cliContext)
			throws CommandException
	{
		Hunter hunterObj = getHunter(cliContext);

		QueueContainer cidQueueContainer = hunterObj.getCidQueue();
		if (cidQueueContainer != null)
		{
			List<DisplayData> displayData = getDiplayDetailsForQueueContainer(cidQueueContainer);
			cliContext.writeNextRow(displayData);
		}

		HashMap<String, QueueContainer> siteQueueContainerMap = hunterObj
				.getSiteQueContainer();
		Collection<QueueContainer> queueContainers = siteQueueContainerMap
				.values();
		for (QueueContainer queueContainer : queueContainers)
		{
			List<DisplayData> displayData = getDiplayDetailsForQueueContainer(queueContainer);
			cliContext.writeNextRow(displayData);
		}
	}

	/**
	 * Sends the details of requested QueueContainer.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} of this request
	 * @throws CommandException
	 *             If the requested hunter does not exist
	 */
	private void sendQueueContainerDetails(CLIContext cliContext)
			throws CommandException
	{
		QueueContainer queueContainer = getQueueContainer(cliContext);
		List<DisplayData> displayData = getDiplayDetailsForQueueContainer(queueContainer);
		cliContext.writeNextRow(displayData);
	}

	/**
	 * Sends the queue dump (at maximum 50 elements) from the requested
	 * QueueContainer.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} of this request
	 * @throws CommandException
	 *             If the requested hunter or site does not exist
	 */
	private void sendQueueDump(CLIContext cliContext) throws CommandException
	{
		QueueContainer queueContainer = getQueueContainer(cliContext);

		int queueSize = queueContainer.getQueueSize();
		int fromIndex = 0;
		int toIndex = 50;
		if (from != null)
		{
			fromIndex = StringUtil.getInteger(from);
			if (fromIndex == -1)
				throw new CommandException(
						"'from' parameter has to be integer: " + to);
			if (fromIndex < 0 || fromIndex >= queueSize)
				throw new CommandException("Out Of Range: from: " + from);
		}

		if (to != null)
		{
			toIndex = StringUtil.getInteger(to);
			if (toIndex == -1)
				throw new CommandException("'to' parameter has to be integer: "
						+ to);
			if (toIndex < 0 || toIndex < fromIndex || toIndex > queueSize)
				throw new CommandException("Out Of Range: to: " + to);
		}

		if (toIndex < fromIndex || (toIndex - fromIndex) > 50)
			toIndex = fromIndex + 50;

		if (toIndex > queueSize)
			toIndex = queueSize;

		List<DisplayData> displayData = new ArrayList<DisplayData>();

		List<QueueComponent> queueComponents = queueContainer.queueSubList(
				fromIndex, toIndex);
		for (QueueComponent queueComponent : queueComponents)
		{
			displayData.clear();

			displayData.add(new DisplayData("Sequence No", String
					.valueOf(queueComponent.getSequenceNo())));
			displayData.add(new DisplayData("Data", queueComponent
					.getDisplayName()));
			displayData.add(new DisplayData("Creation Time", queueComponent
					.getQueueComponentCreationTime().getTime().toString()));

			cliContext.writeNextRow(displayData);
		}
	}

	/**
	 * Sends the status of all worker threads from the requested
	 * QueueContainer.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} of this request
	 * @throws CommandException
	 *             If the requested hunter or site does not exist
	 */
	private void sendThreadDump(CLIContext cliContext) throws CommandException
	{
		QueueContainer queueContainer = getQueueContainer(cliContext);

		List<DisplayData> displayData = new ArrayList<DisplayData>();

		Vector<WorkerThread> workerThreads = queueContainer.getWorkerThreads();
		for (WorkerThread workerThread : workerThreads)
		{
			displayData.clear();

			displayData.add(new DisplayData("Worker Thread", workerThread
					.getUniqueName()));

			QueueComponent queueComponent = workerThread
					.getExecutingComponent();
			if (queueComponent != null)
			{
				displayData.add(new DisplayData("Sequence No", String
						.valueOf(queueComponent.getSequenceNo())));
				displayData.add(new DisplayData("Executing Component",
						queueComponent.getDisplayName()));
				displayData.add(new DisplayData("Creation Time", queueComponent
						.getQueueComponentCreationTime().getTime().toString()));
				displayData.add(new DisplayData("Execution Start Time",
						queueComponent.getExecutionStartTime().getTime()
								.toString()));
			}

			cliContext.writeNextRow(displayData);
		}
	}

	/**
	 * Returns the required hunter.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} of this request
	 * @return the required hunter
	 * @throws CommandException
	 *             If the requested hunter does not exist
	 */
	private Hunter getHunter(CLIContext cliContext) throws CommandException
	{
		HunterContainer hunterContainer = HunterContainer.getHunterContainer();
		HashMap<String, Hunter> huntersMap = hunterContainer.getHunters();
		Hunter hunterObj = huntersMap.get(hunter);

		if (hunterObj == null)
			throw new CommandException("Hunter '" + hunter + "' does not exist");

		return hunterObj;
	}

	/**
	 * Returns the required QueueContainer.
	 * 
	 * @param cliContext
	 *            the {@link CLIContext} of this request
	 * @return the required QueueContainer
	 * @throws CommandException
	 *             If the requested hunter or site does not exist
	 */
	private QueueContainer getQueueContainer(CLIContext cliContext)
			throws CommandException
	{
		Hunter hunterObj = getHunter(cliContext);

		QueueContainer queueContainer = null;
		QueueContainer cidQueueContainer = hunterObj.getCidQueue();
		if (cidQueueContainer != null
				&& cidQueueContainer.getQueueContainerName().equalsIgnoreCase(
						site))
		{
			queueContainer = cidQueueContainer;
		}
		else
		{
			HashMap<String, QueueContainer> siteQueueContainerMap = hunterObj
					.getSiteQueContainer();
			queueContainer = siteQueueContainerMap.get(site);
		}

		if (queueContainer == null)
			throw new CommandException("Site '" + site + "' does not exist");

		return queueContainer;
	}

	/**
	 * Generates the QueueContainer details. Information includes following
	 * details:
	 * <ul>
	 * <li>Queue Container Name</li>
	 * <li>Queue Size</li>
	 * <li>No of Worker Threads</li>
	 * <li>Publisher Status</li>
	 * <li>Average Turnaround Time</li>
	 * </ul>
	 * 
	 * @param queueContainer
	 *            the QueueContainer for which display details has to be
	 *            generated
	 * @return the map containing the details of QueueContainer in key-value
	 *         pair
	 */
	private List<DisplayData> getDiplayDetailsForQueueContainer(
			QueueContainer queueContainer)
	{
		List<DisplayData> displayData = new ArrayList<DisplayData>();

		displayData.add(new DisplayData("Queue Container Name", queueContainer
				.getQueueContainerName()));
		displayData.add(new DisplayData("Queue Size", String
				.valueOf(queueContainer.getQueueSize())));
		displayData.add(new DisplayData("No of Worker Threads", String
				.valueOf(queueContainer.getNoOfWorkerThreads())));

		String publisherStatus = "Not Exists";
		String publisherThreadID = "Not Exists";
		String presentSequenceID = null;
		Publisher publisher = queueContainer.getPublisher();
		if (publisher != null)
		{
			publisherThreadID = publisher.getUniqueName();
			publisherStatus = publisher.isPublisherActive() ? "Active"
					: "Passive";

			if (publisher instanceof ProgressivePublisher)
			{
				presentSequenceID = String
						.valueOf(((ProgressivePublisher) publisher)
								.getPresentSequenceId());
			}
		}
		displayData.add(new DisplayData("Publisher Status", publisherStatus));
		displayData.add(new DisplayData("Publisher Thread", publisherThreadID));

		if (presentSequenceID != null)
		{
			displayData.add(new DisplayData("Present Sequence ID",
					presentSequenceID));
		}

		QueuePerformance queuePerformance = queueContainer
				.getQueuePerformance();
		displayData.add(new DisplayData("Average Turnaround Time", String
				.valueOf(queuePerformance.getWorkerTAT()
						.getPresentTrunAroundTime())
				+ "ms"));

		displayData.add(new DisplayData("Incoming Rate(per sec)", String
				.valueOf(queuePerformance.getIncommingRate()
						.getLastExcecutionsPerSec())));

		displayData.add(new DisplayData("Outgoing Rate(per sec)", String
				.valueOf(queuePerformance.getOutgoingRate()
						.getLastExcecutionsPerSec())));

		if (queueContainer.getLastPickUpTime() != null)
		{
			displayData.add(new DisplayData("Last Pickup Time", queueContainer
					.getLastPickUpTime().getTime().toString()));
		}

		if (queueContainer.getLastProcessTime() != null)
		{
			displayData.add(new DisplayData("Last Processed Time", queueContainer
					.getLastProcessTime().getTime().toString()));
		}

		return displayData;
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
		builder.append("Showdetails[dump = ");
		builder.append(dump);
		builder.append(", from = ");
		builder.append(from);
		builder.append(", hunter = ");
		builder.append(hunter);
		builder.append(", site = ");
		builder.append(site);
		builder.append(", to = ");
		builder.append(to);
		builder.append("]");
		return builder.toString();
	}
}
