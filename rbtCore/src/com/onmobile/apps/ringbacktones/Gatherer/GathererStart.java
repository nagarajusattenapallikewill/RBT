package com.onmobile.apps.ringbacktones.Gatherer;


import org.apache.log4j.Logger;
import org.tanukisoftware.wrapper.WrapperListener;

import org.tanukisoftware.wrapper.WrapperManager;


public class GathererStart extends Thread implements WrapperListener
{
	private static Logger logger = Logger.getLogger(GathererStart.class);

	private GathererStart m_gathererStart;

	public void run()
	{
		RBTGatherer.m_rbtGatherer  = new RBTGatherer();
		System.out.println("Started ...");
		try
		{
			if (RBTGatherer.m_rbtGatherer .init())
			{
				RBTGatherer.m_rbtGatherer .start();
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//System.exit(0);
		}
	}

	public Integer start(String[] arg0)
	{
		m_gathererStart = new GathererStart();
		m_gathererStart.start();
		return null;
	}



	public int stop(int exitCode)
	{
		m_gathererStart.stop();
		return exitCode;
	}

	public void controlEvent(int event)
	{
		if (WrapperManager.isControlledByNativeWrapper())
		{
			// The Wrapper will take care of this event
		}
		else
		{
			// We are not being controlled by the Wrapper, so
			//  handle the event ourselves.
			if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT)
					|| (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT)
					|| (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT))
			{
				WrapperManager.stop(0);
			}
		}
	}

	public static void main(String[] args)
	{
		// Start the application. If the JVM was launched from the native
		//  Wrapper then the application will wait for the native Wrapper to
		//  call the application's start method. Otherwise the start method
		//  will be called immediately.

		WrapperManager.start(new GathererStart(), args);
	}
}
