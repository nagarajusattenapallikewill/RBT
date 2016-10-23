package com.onmobile.apps.ringbacktones.logger;

import java.io.File;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.logger.layout.CopyDaemonLayout;
import com.onmobile.apps.ringbacktones.logger.layout.PromotionJspLayout;
import com.onmobile.apps.ringbacktones.logger.layout.SmsLayout;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;

public class SmsLogger implements iRBTConstant
{
	public static Logger appLogger = Logger.getLogger(SmsLogger.class); 
	
	static
	{
		createSmsLogger();
	}
	
	public static Logger logger = Logger.getLogger(sms_trans_logger); 
		
	public static Logger getLogger()
	{
		return logger;
	}
	
	private static void createSmsLogger()
	{
		Logger logger= Logger.getLogger(sms_trans_logger);
		logger.setAdditivity(false);
		logger.setLevel(Level.toLevel(DBConfigTools.getParameter("LOG", sms_trans_logger+".LOG.LEVEL", "INFO")));
		
		
		
		String logFolderName = DBConfigTools.getParameter("SMS",Constants.SDR_WORKING_DIR,".");
		File logFolder = new File(logFolderName);
		if(!logFolder.exists())
			logFolder.mkdirs();
		
		DailyRollingFileAppender drfa = new AppenderWithHeader();
		drfa.setDatePattern("_ddMMyyyy'.LOG'");
		drfa.setFile(logFolderName+ File.separator + "SMS_REQUEST");
		
		PatternLayout layout = new SmsLayout();
		layout.setConversionPattern("%m%n");
		drfa.setLayout(layout);
		drfa.activateOptions();
		logger.addAppender(drfa);
	}
}
