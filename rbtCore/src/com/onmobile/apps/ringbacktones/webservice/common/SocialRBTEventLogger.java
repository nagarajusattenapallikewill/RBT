package com.onmobile.apps.ringbacktones.webservice.common;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.eventlogging.RBTSocialApp.v1_0_0.EventLogger;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.reporting.framework.capture.api.Configuration;
import com.onmobile.reporting.framework.capture.api.ReportingException;

public class SocialRBTEventLogger {
	private static Logger logger = Logger.getLogger(SocialRBTEventLogger.class);
	private static RBTConnector rbtConnector = null;
	
	private static String m_socialAppAccessEventLoggingDir = "."+File.separator+"SocialRBTAccessEventLogs";
	private static String m_socialAppUserValidationEventLoggingDir = "."+File.separator+"SocialRBTUserValidationEventLogs";
	private static String m_socailAppSNGActivationEventLoggingDir = "."+File.separator+"SocialRBTSNGActivationEventLogs";
	private static String m_socailAppSNGDeactivationEventLoggingDir = "."+File.separator+"SocialRBTSNGDeactivationEventLogs";
	private static EventLogger accessEventLogger = null;
	private static EventLogger userValidationEventLogger = null;
	private static EventLogger sngActivationEventLogger = null;
	private static EventLogger sngDeactivationEventLogger = null;


	static
	{
		try
		{
			init();
		}
		catch (Exception e)
		{
			logger.error("RBT:: " + e.getMessage(), e);
		}
	}
	
	private static void init()
	{
		rbtConnector = RBTConnector.getInstance();
		if(userValidationEventLogger==null){
			if (getParamAsString("SOCIAL_APP_USER_VALIDATION_EVENT_LOG_PATH") != null) {
				m_socialAppUserValidationEventLoggingDir = getParamAsString("SOCIAL_APP_USER_VALIDATION_EVENT_LOG_PATH")
						+ File.separator+"SocialRBTUserValidationEventLogs";
				
			}
			File file=new File(m_socialAppUserValidationEventLoggingDir);
			if(!file.isDirectory()){
				new File(m_socialAppUserValidationEventLoggingDir).mkdirs();
			}
			Configuration cfg = new Configuration(m_socialAppUserValidationEventLoggingDir);
			try {
				userValidationEventLogger = new EventLogger(cfg);
			} catch (IOException e) {
				logger.error("", e);
			}
			initializeEventLogger(m_socialAppUserValidationEventLoggingDir,userValidationEventLogger);
		}
		if(accessEventLogger==null){
			if (getParamAsString("SOCIAL_APP_ACCESS_EVENT_LOG_PATH") != null) {
				m_socialAppAccessEventLoggingDir = getParamAsString("SOCIAL_APP_ACCESS_EVENT_LOG_PATH")
						+ File.separator+"SocialRBTAccessEventLogs";
				
			}
			File file=new File(m_socialAppAccessEventLoggingDir);
			if(!file.isDirectory()){
				new File(m_socialAppAccessEventLoggingDir).mkdirs();
			}
			Configuration cfg = new Configuration(m_socialAppAccessEventLoggingDir);
			try {
				accessEventLogger = new EventLogger(cfg);
			} catch (IOException e) {
				logger.error("", e);
			}
			initializeEventLogger(m_socialAppAccessEventLoggingDir,accessEventLogger);
		}
		if(sngActivationEventLogger==null){
			if (getParamAsString("SOCIAL_APP_SNG_ACTIVATION_EVENT_LOG_PATH") != null) {
				m_socailAppSNGActivationEventLoggingDir = getParamAsString("SOCIAL_APP_SNG_ACTIVATION_EVENT_LOG_PATH")
						+ File.separator+"SocialRBTSNGActivationEventLogs";
			}
			File file=new File(m_socailAppSNGActivationEventLoggingDir);
			if(!file.isDirectory()){
				new File(m_socailAppSNGActivationEventLoggingDir).mkdirs();
			}
			Configuration cfg = new Configuration(m_socailAppSNGActivationEventLoggingDir);
			try {
				sngActivationEventLogger = new EventLogger(cfg);
			} catch (IOException e) {
				logger.error("", e);
			}
			initializeEventLogger(m_socailAppSNGActivationEventLoggingDir,sngActivationEventLogger);
		}
		if(sngDeactivationEventLogger==null){
			if (getParamAsString("SOCIAL_APP_SNG_DEACTIVATION_EVENT_LOG_PATH") != null) {
				m_socailAppSNGDeactivationEventLoggingDir = getParamAsString("SOCIAL_APP_SNG_DEACTIVATION_EVENT_LOG_PATH")
						+ File.separator+"SocialRBTSNGDeactivationEventLogs";
			}
			File file=new File(m_socailAppSNGDeactivationEventLoggingDir);
			if(!file.isDirectory()){
				new File(m_socailAppSNGDeactivationEventLoggingDir).mkdirs();
			}
			Configuration cfg = new Configuration(m_socailAppSNGDeactivationEventLoggingDir);
			try {
				sngDeactivationEventLogger = new EventLogger(cfg);
			} catch (IOException e) {
				logger.error("", e);
			}
			initializeEventLogger(m_socailAppSNGDeactivationEventLoggingDir,sngDeactivationEventLogger);
		}
	}
	
	private static void initializeEventLogger(String m_eventLoggingDir,EventLogger eventLogger) {
		try {
			Configuration cfg = new Configuration(m_eventLoggingDir);
			eventLogger = new EventLogger(cfg);
			logger.info(
					"*** RBT::writing COPY EVENT LOGS (append) in directory : "
							+ m_eventLoggingDir);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public static void accessEventLog(String userId,String msisdn,String senderId,String otherInfo,int socialType){
		try {
			Date timestamp = Calendar.getInstance().getTime();
			synchronized (accessEventLogger) {
				accessEventLogger.SocailAppAccessLog(timestamp, userId, msisdn, senderId, otherInfo, socialType);
			}
		} catch (ReportingException e) {
			logger.error("", e);
		}
	}
	
	public static void userValidationEventLog(String userId,String msisdn,String otherInfo,int socialType,String inNetwork){
		try {
			Date timestamp = Calendar.getInstance().getTime();
			synchronized (userValidationEventLogger) {
				userValidationEventLogger.SocailAppUservalidation(timestamp,
						userId, msisdn, otherInfo, socialType, inNetwork);
			}
		} catch (ReportingException e) {
			logger.error("", e);
		}
	}
	
	public static void sngActivationEventLog(String userId,String msisdn,String rbtType,String otherInfo,int socialType, String circleId){
		try {
			Date timestamp = Calendar.getInstance().getTime();
			synchronized (sngActivationEventLogger) {
				sngActivationEventLogger.SocailAppSNGActivationLog(timestamp,
						userId, circleId, msisdn, rbtType, otherInfo, socialType);
			}
		} catch (ReportingException e) {
			logger.error("", e);
		}
	}
	
	public static void sngDeactivationEventLog(String userId,String msisdn,String rbtType,String otherInfo,int socialType, String circleId){
		try {
			Date timestamp = Calendar.getInstance().getTime();
			synchronized (sngDeactivationEventLogger) {
				sngDeactivationEventLogger.SocailAppSNGDeactivationLog(timestamp,userId, circleId, msisdn, rbtType, otherInfo, socialType);
			}
		} catch (ReportingException e) {
			logger.error("", e);
		}
	}
	
	private static  boolean getParamAsBoolean(String param, String defaultVal) {
		try {
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, defaultVal).equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.info(
					"Unable to get param ->" + param
							+ " returning defaultVal >" + defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	private static String getParamAsString(String param) {
		try {
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, null);
		} catch (Exception e) {
			logger.info(
					"Unable to get param ->" + param);
			return null;
		}
	}
	
	public static EventLogger getAccessEventLogger() {
		return accessEventLogger;
	}
	
	public static void setAccessEventLogger(EventLogger accessEventLogger) {
		SocialRBTEventLogger.accessEventLogger = accessEventLogger;
	}
	
	public static EventLogger getUserValidationEventLogger() {
		return userValidationEventLogger;
	}
	
	public static void setUserValidationEventLogger(
			EventLogger userValidationEventLogger) {
		SocialRBTEventLogger.userValidationEventLogger = userValidationEventLogger;
	}
	
	public static EventLogger getSngActivationEventLogger() {
		return sngActivationEventLogger;
	}
	
	public static void setSngActivationEventLogger(
			EventLogger sngActivationEventLogger) {
		SocialRBTEventLogger.sngActivationEventLogger = sngActivationEventLogger;
	}
	
	public static EventLogger getSngDeactivationEventLogger() {
		return sngDeactivationEventLogger;
	}
	
	public static void setSngDeactivationEventLogger(
			EventLogger sngDeactivationEventLogger) {
		SocialRBTEventLogger.sngDeactivationEventLogger = sngDeactivationEventLogger;
	}
}
