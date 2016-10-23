package com.onmobile.apps.ringbacktones.logger;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.TimeBasedRollingPolicy;

import com.onmobile.apps.ringbacktones.common.PatternLayoutWithHeader;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;

public class RbtLogger
{
	private static String logDirectory;
	public static enum ROLLING_FREQUENCY { YEARLY , MONTHLY, DAILY, HOURLY, MINUTE};
	
	public static final String rbtWebserverTransactionPrefix = "TRANSACTIONS.RBT_WEB_SERVER."; 
	public static final String smDaemonTransactionPrefix = "TRANSACTIONS.SM_DAEMON.";
	public static final String tonePlayerDaemonTransactionPrefix = "TRANSACTIONS.TONE_PLAYER.";
	public static final String tonePlayerStatisticsPrefix = "STATISTICS.RBT_WEB_SERVER.";
	public static final String smDaemonStatisticsPrefix = "STATISTICS.RBT_WEB_SERVER.";
	public static final String rbtWebserverStatisticsPrefix = "STATISTICS.RBT_WEB_SERVER.";
	public static final String reporterStatisticsPrefix = "STATISTICS.REPORTER.";
	public static final String incomingPrefix = "INCOMING_REQUESTS.";
	public static final String contestInfluencerWhitelistPrefix = "TRANSACTIONS.CONTEST.";
	public static final String consentCallbackTransPrefix = "CONSENT.TRANSACTIONS.CALLBACK.";
	public static final String consentCleanupTransPrefix = "CONSENT.TRANSACTIONS.CLEANUP.";
	public static final String consentDaemonTransPrefix = "CONSENT.TRANSACTIONS.DAEMON.";
	public static final String cgUrlPrefix = "CONSENT.URL.";
	public static final String cgCallbackPrefix = "CONSENT.CALLBACK.";
	public static final String consentTransactionPrefix = "CONSENT";
	
	static
	{
		boolean isRRBTSystem = RBTParametersUtils.getParamAsBoolean("COMMON", "RRBT_SYSTEM", "false");
		
		if (isRRBTSystem) {
			logDirectory = System.getProperty("RRBT_SYSTEM_LOGS");
		} else { 
			logDirectory = System.getProperty("RBT_SYSTEM_LOGS");
		}
		
		if(logDirectory == null)
		{
			String osName = System.getProperty("os.name");
			if(osName==null || osName.trim().length() == 0)
				logDirectory = ".";
			else
			{
				if(osName.toLowerCase().contains("window"))
				{
					if(new File("E:/").exists())
						logDirectory = "E:";
					else if(new File("D:/").exists())
						logDirectory = "D:";
					else if(new File("C:/").exists())
						logDirectory = "C:";
				}
				else
				{
					if(new File("/var").exists())
						logDirectory = "/var";
					else if(new File("/opt:/").exists())
						logDirectory = "/opt";
					else if(new File("/mnt").exists())
						logDirectory = "/mnt";
				}
			}
		}
		
		if (isRRBTSystem) {
			logDirectory += File.separator + "RRBT_SYSTEM_LOGS";
		} else { 
			logDirectory += File.separator + "RBT_SYSTEM_LOGS";
		}

		new File(logDirectory).mkdirs();
	}
	
	/**
	 * @return the logDirectory
	 */
	public static String getLogDirectory()
	{
		return logDirectory;
	}

	public synchronized static Logger createRollingFileLogger(String loggerName, ROLLING_FREQUENCY rollingFrequency)
	{
		Logger logger = null;
		try
		{
			//Added for TTG-14814
			String logHeader=RBTParametersUtils.getParamAsString("LOGGER","RBT_TO_SM_HTTP_URL_LOG_HEADER" , "");
			//End of TTG-14814
			
			loggerName = loggerName.toUpperCase();
			loggerName = loggerName.replaceAll(" ", "_");
			logger = Logger.getLogger(loggerName);
			if(logger.getAllAppenders().hasMoreElements())
				return logger;
			
			logger.setAdditivity(false);
			String logLevel = DBConfigTools.getParameter("LOG", loggerName+".LOG.LEVEL", "INFO");
			logger.setLevel(Level.toLevel(logLevel));
			PatternLayoutWithHeader layout = new PatternLayoutWithHeader(
					"%d{yyyy-MM-dd HH:mm:ss.SSS}%m%n");
			//Added for TTG-14814
			if(loggerName.contains("RBT_TO_SM_HTTP_URL")){
				layout.setHeader(logHeader);	
			}
			//End of TTG-14814
			
			String relativePath = getRelativeFolderName(loggerName);
			String fileName = getFileName(loggerName);
			
			String fileNameComplete = logDirectory + relativePath + File.separator;
			File folder = new File(fileNameComplete);
			if(!folder.exists())
				folder.mkdirs();
			
			String archiveFolderName = fileNameComplete+"ARCHIVE"+File.separator;
			File archiveFolder = new File(archiveFolderName);
			if(!archiveFolder.exists())
				archiveFolder.mkdirs();
			
			RollingFileAppender rfa = new RollingFileAppender();
			TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
			if(rollingFrequency.equals(ROLLING_FREQUENCY.YEARLY))
				tbrp.setFileNamePattern(archiveFolderName+fileName+".%d{yyyy}.LOG.zip");
			else if(rollingFrequency.equals(ROLLING_FREQUENCY.MONTHLY))
				tbrp.setFileNamePattern(archiveFolderName+fileName+".%d{yyyy-MM}.LOG.zip");
			else if(rollingFrequency.equals(ROLLING_FREQUENCY.DAILY))
				tbrp.setFileNamePattern(archiveFolderName+fileName+".%d{yyyy-MM-dd}.LOG.zip");
			else if(rollingFrequency.equals(ROLLING_FREQUENCY.MINUTE))
				tbrp.setFileNamePattern(archiveFolderName+fileName+".%d{yyyy-MM-dd.HH.mm}.LOG.zip");
			else // HOURLY ROLLING
				tbrp.setFileNamePattern(archiveFolderName+fileName+".%d{yyyy-MM-dd.HH}.LOG.zip");
			
			rfa.setFile(fileNameComplete+fileName+".LOG");
			tbrp.activateOptions();
			rfa.setRollingPolicy(tbrp);
			rfa.setLayout(layout);
			rfa.setAppend(true);
			rfa.activateOptions();
			logger.addAppender(rfa);
		}
		catch(Exception e)
		{
		}
		return logger;
	}

	private static String getFileName(String loggerName)
	{
		int lastIndexOfDot = loggerName.lastIndexOf(".");
		String fileName = null;
		if(lastIndexOfDot != -1)
			fileName = loggerName.substring(lastIndexOfDot+1); 
		else
			fileName = loggerName;
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
