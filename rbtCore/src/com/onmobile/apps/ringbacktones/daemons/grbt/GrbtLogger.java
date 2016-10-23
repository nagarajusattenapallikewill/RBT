package com.onmobile.apps.ringbacktones.daemons.grbt;

import java.io.File;
import java.net.InetAddress;
import java.util.ResourceBundle;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.TimeBasedRollingPolicy;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;

public class GrbtLogger
{
	private static String logDirectory;
	public static final String grbtTransactionPrefix = "GRBT.";
	public static Logger downloadsLogger = null;
	public static Logger tpLogger = null;
	private static Logger logger = Logger.getLogger(GrbtLogger.class);
	static
	{
		try
		{
			logger.info("Entered in to static Block");
			logDirectory = System.getProperty("RBT_SYSTEM_LOGS");
			if(logDirectory == null)
				logDirectory = "/var";
			logDirectory += File.separator + "RBT_SYSTEM_LOGS";
			new File(logDirectory).mkdirs();
			String operatorName = RBTParametersUtils.getParamAsString("GRBT", "OPERATOR_NAME", null);
			if(operatorName != null)
				operatorName = operatorName.trim();
			logger.info("Trimmed operator is" + operatorName);
			downloadsLogger = createRollingFileLogger(grbtTransactionPrefix+operatorName+"_DOWNLOADS");
			logger.info("initialized downloadslogger");
			tpLogger = createRollingFileLogger(grbtTransactionPrefix+operatorName+"_TP");
			logger.info("initialized tplogger");
		}
		catch(Exception e)
		{
			logger.error("Error initializing grbtlogger", e);
		}
	}
	
	public static String getLogDirectory()
	{
		return logDirectory;
	}

	public synchronized static Logger createRollingFileLogger(String loggerName)
	{
		Logger logger = null;
		try
		{
			loggerName = loggerName.toUpperCase();
			loggerName = loggerName.replaceAll(" ", "_");
			logger = Logger.getLogger(loggerName);
			if(logger.getAllAppenders().hasMoreElements())
				return logger;
			
			logger.setAdditivity(false);
			String logLevel = DBConfigTools.getParameter("LOG", loggerName+".LOG.LEVEL", "INFO");
			logger.setLevel(Level.toLevel(logLevel));
			Layout layout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss.SSS,}%m%n");
			
			String relativePath = getRelativeFolderName(loggerName);
			String hostname = InetAddress.getLocalHost().getHostName();
			
			try {
				hostname = ResourceBundle.getBundle("rbt").getString("GRBT_LOGGER_TYPE");
			}
			catch(Exception e) {
				
			}
			
			String fileName = getFileName(loggerName,hostname);
			
			String fileNameComplete = logDirectory + relativePath + File.separator;
			File folder = new File(fileNameComplete);
			if(!folder.exists())
				folder.mkdirs();
			
			
			RollingFileAppender rfa = new RollingFileAppender();
			TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
			tbrp.setFileNamePattern(fileNameComplete+fileName+"_%d{yyyyMMddHH}.csv");
			
			rfa.setFile(fileNameComplete+fileName+".csv");
			tbrp.activateOptions();
			rfa.setRollingPolicy(tbrp);
			rfa.setLayout(layout);
			rfa.setAppend(true);
			rfa.activateOptions();
			logger.addAppender(rfa);
		}
		catch(Exception e)
		{
			logger.error("Got exception" , e);
		}
		return logger;
	}

	private static String getFileName(String loggerName,String hostname)
	{
		int lastIndexOfDot = loggerName.lastIndexOf(".");
		String fileName = null;
		if(lastIndexOfDot != -1)
			fileName = loggerName.substring(lastIndexOfDot+1); 
		else
			fileName = loggerName;
		fileName = fileName + "_" + hostname;
		return fileName;
	}

	private static String getRelativeFolderName(String loggerName)
	{
		String[] tokens = loggerName.split("\\.");
		String folderPath = "";
		for(String token : tokens)
			folderPath += File.separator + token;
		return folderPath;
	}
}
