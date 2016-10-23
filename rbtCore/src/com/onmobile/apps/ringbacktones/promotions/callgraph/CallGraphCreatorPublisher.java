/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher;
import com.onmobile.apps.ringbacktones.logger.RbtLogger;

/**
 * @author vinayasimha.patil
 * 
 */
public class CallGraphCreatorPublisher extends AbstractQueuePublisher
{
	private static Logger logger = Logger
			.getLogger(CallGraphCreatorPublisher.class);

	private File publishingFile = null;
	private BufferedReader bufferedReader = null;

	/**
	 * @param publishInterval
	 * @param timeUnit
	 */
	public CallGraphCreatorPublisher(long publishInterval, TimeUnit timeUnit)
	{
		super(publishInterval, timeUnit, 0.80f, true);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher
	 * #start()
	 */
	@Override
	public void start()
	{
		Calendar calendar = Calendar.getInstance();

		int publishingStartHour = 2;
		if (calendar.get(Calendar.HOUR_OF_DAY) > publishingStartHour)
		{
			// If 2AM (Up to 2:59, 1 hour grace) is already passed in current
			// day, then publisher will start at 2AM next day otherwise it will
			// be started at 2AM.
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}

		calendar.set(Calendar.HOUR_OF_DAY, 2);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		super.start(calendar.getTime());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher
	 * #publish()
	 */
	@Override
	protected void publish()
	{
		try
		{
			if (bufferedReader == null)
				assignReader();

			if (bufferedReader == null)
				return;

			String cdrLog = null;
			while ((cdrLog = bufferedReader.readLine()) != null)
			{
				cdrLog = cdrLog.trim();
				if (cdrLog.isEmpty())
					continue;

				TonePlayerCDR tonePlayerCDR = TonePlayerCDR
						.buildTonePlayerCDRFromLog(cdrLog);
				if (tonePlayerCDR == null)
					continue;

				CallGraphCreator callGraphCreator = new CallGraphCreator(
						executor, tonePlayerCDR);

				publish(callGraphCreator);

				if (executor.getQueue().remainingCapacity() == 0)
				{
					logger.debug("Queue is full, suspending queue publishing");
					return;
				}
			}
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);

			executor.shutdown();
		}

		closeReader();
		publish();
	}

	private void assignReader()
	{
		try
		{
			String callGraphLogsDir = RbtLogger.getLogDirectory()
					+ File.separator + "CallGraph";
			File tpCDRDir = new File(callGraphLogsDir, "TPCDRs");
			File[] cdrFilesDirs = tpCDRDir
					.listFiles((FileFilter) FileFilterUtils
							.directoryFileFilter());
			if (cdrFilesDirs == null || cdrFilesDirs.length == 0)
			{
				reschedulePublisher();
				return;
			}

			for (File cdrFilesDir : cdrFilesDirs)
			{
				File[] cdrFiles = cdrFilesDir
						.listFiles((FileFilter) FileFilterUtils
								.fileFileFilter());
				if (cdrFiles == null || cdrFiles.length == 0)
				{
					boolean deleted = cdrFilesDir.delete();
					if (!deleted)
					{
						logger.error("Unable to delete the directory "
								+ cdrFilesDir);
					}
					else if (logger.isDebugEnabled())
						logger.debug(cdrFilesDir + " directory deleted");

					continue;
				}

				FileReader fileReader = new FileReader(cdrFiles[0]);
				bufferedReader = new BufferedReader(fileReader);
				publishingFile = cdrFiles[0];
				break;
			}

			if (bufferedReader == null)
				reschedulePublisher();
		}
		catch (FileNotFoundException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private void closeReader()
	{
		try
		{
			if (bufferedReader != null)
			{
				bufferedReader.close();
				bufferedReader = null;
			}

			if (publishingFile != null)
			{
				String callGraphLogsDir = RbtLogger.getLogDirectory()
						+ File.separator + "CallGraph";

				String callGraphProcessedDirName = "Processed_TPCDRs"
						+ File.separator
						+ publishingFile.getParentFile().getName();

				File callGraphProcessedDir = new File(callGraphLogsDir,
						callGraphProcessedDirName);
				if (!callGraphProcessedDir.exists())
				{
					boolean created = callGraphProcessedDir.mkdirs();
					if (!created)
					{
						logger.error("Unable to create the directory "
								+ callGraphProcessedDir);
					}
					else if (logger.isDebugEnabled())
					{
						logger.debug(callGraphProcessedDir
								+ " directory create status: " + created);
					}
				}

				File dest = new File(callGraphProcessedDir,
						System.currentTimeMillis() + "_"
								+ publishingFile.getName());
				boolean renamed = publishingFile.renameTo(dest);

				if (!renamed)
					logger.error("Unable to move the file " + publishingFile
							+ " to processed directory.");
				else if (logger.isDebugEnabled())
					logger.debug(publishingFile + " file moved to " + dest);

				publishingFile = null;
			}
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private void reschedulePublisher()
	{
		Calendar calendar = Calendar.getInstance();

		int publishingStartHour = 2;
		if (calendar.get(Calendar.HOUR_OF_DAY) > publishingStartHour)
		{
			// If 2AM (Up to 2:59, 1 hour grace) is already passed in current
			// day, then publisher will start at 2AM next day.
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 2);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

		}
		else
		{
			// If still 2AM (Up to 2:59, 1 hour grace) not passed, then
			// publisher will start again after 10 minutes.
			calendar.add(Calendar.MINUTE, 10);
		}

		reschedule(calendar.getTime());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher
	 * #clone()
	 */
	@Override
	protected QueuePublisher clone() throws CloneNotSupportedException
	{
		QueuePublisher queuePublisher = new CallGraphCreatorPublisher(
				publishInterval, timeUnit);
		return queuePublisher;
	}
}
