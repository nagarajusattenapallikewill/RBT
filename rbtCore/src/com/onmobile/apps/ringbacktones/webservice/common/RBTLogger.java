/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import java.io.File;
import java.util.ResourceBundle;

import com.onmobile.common.debug.DebugManager;


/**
 * @author vinayasimha.patil
 *
 */
public class RBTLogger
{
	public static String module = "RBT";

	synchronized public static void initialize(String module)
	{
		String logDir = System.getProperty("ONMOBILE", null);        
		if (logDir == null)
			logDir = System.getProperty("LOG_PATH", null);

		initialize(module, logDir);
	}

	synchronized public static void initialize(String module, String logDir)
	{
		RBTLogger.module = module;

		int logLevel = 7;
		try
		{
			ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
			logLevel = Integer.parseInt(resourceBundle.getString("LOG_LEVEL"));
		}
		catch (Exception e)
		{
		}

		if (logDir == null)
			logDir = ".";

		String logFileName = module + "_trace";
		String errorFileName = module + "_error";
		Object ret = DebugManager.init(logLevel, module, logDir + File.separator + "log", logFileName, errorFileName,
				"size", 10485760L, 20, true);
		if (ret == null)
			System.out.println("The DebugManager couldn't be initialised.");
	}

	public static void logWarning(String className, String method, String message)
	{
		DebugManager.warning(module, className, method, message, Thread.currentThread().getName(), null);
	}

	public static void logStatus(String className, String method, String message)
	{
		DebugManager.status(module, className, method, message, Thread.currentThread().getName(), null);
	}

	public static void logDetail(String className, String method, String message)
	{
		DebugManager.detail(module, className, method, message, Thread.currentThread().getName(), null);
	}

	public static void logTrace(String className, String method, String message)
	{
		DebugManager.trace(module, className, method, message, Thread.currentThread().getName(), null);
	}

	public static void logException(String className, String method, Throwable t)
	{
		DebugManager.exception(module, className, method, t, Thread.currentThread().getName(), null);
	}

	public static void logNonFatalError(String className, String method, String message)
	{
		DebugManager.nonfatalError(module, className, method, message, Thread.currentThread().getName(), null);
	}

	public static void logFatalError(String className, String method, String message)
	{
		DebugManager.fatalError(module, className, method, message, Thread.currentThread().getName(), null);
	}

	public static void logFatalException(String className, String method, Throwable t)
	{
		DebugManager.fatalException(module, className, method, t, Thread.currentThread().getName(), null);
	}
}
