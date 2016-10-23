/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.monitor.common.Constants;

/**
 * @author Sreekar
 */
public class RBTEventLogger
{

	public static void logEvent(Event event, String message) // will add a type?
	{
		logEvent(event, message, null);
	}

	public static void logEvent(Event event, String message, String logType) {
		Logger eventLogger = Logger.getLogger(event.toString());

		// event will be logged only of the required logger is created
		if (eventLogger.getName().equals(event.toString())) {
			if (logType != null && logType.equalsIgnoreCase(Constants.SQL_TYPE_LOGGER))
				message += ";";
			eventLogger.info(message);
		}
	}

	public static boolean isEventLoggingEnabled(Event event)
	{
		Logger eventLogger = Logger.getLogger(event.toString());
		return (eventLogger.getName().equals(event.toString()));
	}

	/**
	 * @author vinayasimha.patil
	 * 
	 */
	public enum Event
	{
		/**
		 * This event is used for Reliance ARBT.
		 */
		ARBT,
		DAILYREPORTACTIVITY,
		DAILYREPORTCONTENT,

		/**
		 * This event represents the SM Transaction Log.
		 */
		TLOG,
		
		/**
		 * This event represents the pin generation for login user.
		 */
		LOGINUSERPIN,
		
		/**
		 * This event represents the upgrade transaction logs.
		 */
		UPGRADETRANSACTION,
		
		/**
		 * This event is used for logging request sent for promotion confirmation.
		 */
		PROMOTION_CONFIRMATOR,
		
		/**
		 * This event is used for logging message sent for promotion.
		 */
		PROMOTION,

		/**
		 * This event represents the logs for all SOAP API requests.
		 */
		SOAP_OPERATIONS,

		/**
		 * This event represents the logs for Zoomin SMS transactions.
		 */
		ZOOMIN,

		/**
		 * This event represents the logs for Voucher requests through SMS.
		 */
		VOUCHER,

		/**
		 * This event represents the logs for SM daemon transactions.
		 */
		SMDAEMON,

		/**
		 * This event represents the logs for SM daemon transactions.
		 */
		LOTTERY_TRANS,

		/**
		 * This event represents the logs for Voluntary Suspension.
		 */
		VOLUNTARY_SUSPENSION,

		/**
		 * This event represents the logs for Viral OptOut model requests.
		 */
		VIRAL_OPTOUT,

		/**
		 * This event represents the logs for all JMS retail requests.
		 */
		JMS_RETAIL,
		
		/**
		 * This event represents the logs for all AD2C requests
		 */
		AD2C_TRANS_LOG,
		
		/**
		 * This is for FREEMIUM Blacklist Changes
		 */
		FREEMIUM_BLACKLIST,
		
		/**
		 * This is for Backup of DML Changes for base, selection, downloads and provisioning requests logs
		 */
		DML_BACKUP_LOG
	}
}