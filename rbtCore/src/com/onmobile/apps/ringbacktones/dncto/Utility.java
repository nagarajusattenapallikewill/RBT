/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.onmobile.apps.ringbacktones.dncto.DNCTOConstants.SubscriberStatus;
import com.onmobile.dnctoservice.exception.DNCTOException;

/**
 * Utility class for the RBT DNCTO implementation.
 * 
 * @author vinayasimha.patil
 */
public class Utility
{
	/**
	 * Log4j logger object
	 */
	private static Logger logger = Logger.getLogger(Utility.class);

	/**
	 * Creates the data in the format required by the DNCTO framework i.e.
	 * subscriberID and data in JSONObject notation. Format:
	 * <tt>subscriberID,{"rbtChannelName":{"subscriberStatus":[time]}}</tt>
	 * 
	 * @param subscriberID
	 *            the MSISDN
	 * @param channelName
	 *            channel name of RBT assigned by DNCTO framework
	 * @param subscriberStatus
	 *            current status of the subscriber
	 * @param time
	 *            time in milliseconds, when subscriber moved to the status
	 *            mentioned in <tt>subscriberStatus</tt>
	 * @return the data in the format required by the DNCTO framework
	 * @throws DNCTOException
	 *             if unable to format the data
	 */
	public static String getDNCTOFormattedData(String subscriberID,
			String channelName, SubscriberStatus subscriberStatus, long time)
			throws DNCTOException
	{
		try
		{
			JSONObject rbtJSONObject = new JSONObject();
			rbtJSONObject.put(subscriberStatus.toString(), Collections
					.singletonList(time));

			JSONObject jsonObject = new JSONObject();
			jsonObject.put(channelName, rbtJSONObject);

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(subscriberID);
			stringBuilder.append(",");
			stringBuilder.append(jsonObject.toString());

			if (logger.isDebugEnabled())
				logger.debug("Formatted Data: " + stringBuilder);

			return stringBuilder.toString();
		}
		catch (JSONException e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(
					"Error in formatting the data.");
			dnctoException.initCause(e);
			throw dnctoException;
		}
	}

	/**
	 * Finds the subscriber status by parsing the TLOG.
	 * 
	 * @param logLine
	 *            a line from TLOG
	 * @param logTokens
	 *            TLOG tokens
	 * @return the {@link SubscriberStatus} if subscriber status is in
	 *         predefined list, otherwise <tt>null</tt>
	 */
	public static SubscriberStatus getSubscriberStatus(String logLine,
			String[] logTokens)
	{
		if (isUserInError(logTokens))
			return SubscriberStatus.ERR;
		if (isUserActivate(logLine, logTokens))
			return SubscriberStatus.ACT;
		if (isUserDectivated(logLine, logTokens))
			return SubscriberStatus.DCT;
		if (isUserInGrace(logTokens))
			return SubscriberStatus.GRC;
		if (isUserInSuspension(logTokens))
			return SubscriberStatus.SUS;

		return null;
	}

	/**
	 * Returns <tt>true</tt> if user in ACTIVE state, otherwise <tt>false</tt>.
	 * 
	 * @param logLine
	 *            a line from TLOG
	 * @param logTokens
	 *            TLOG tokens
	 * @return returns <tt>true</tt> if user in ACTIVE state, otherwise
	 *         <tt>false</tt>
	 */
	private static boolean isUserActivate(String logLine, String[] logTokens)
	{
		/*
		 * -> Token - 7 equals to 'A' means activation request and if log
		 * contains 'CHG=1' or 'CHG=10' or 'CHG=11' or 'CHG=12' means
		 * successfully activated.
		 * -> Token - 19 equals to 'K' means moved from GRACE to ACTIVE state.
		 * -> Token - 19 equals to 'L' means moved from SUSPENSION to ACTIVE
		 * state.
		 */
		return ((logTokens[7].equalsIgnoreCase("A") && (logLine
				.contains("CHG=1,")
				|| logLine.contains("CHG=10,") || logLine.contains("CHG=11,") || logLine
				.contains("CHG=12,")))
				|| logTokens[19].equalsIgnoreCase("K") || logTokens[19]
				.equalsIgnoreCase("L"));
	}

	/**
	 * Returns <tt>true</tt> if user in GRACE state, otherwise <tt>false</tt>.
	 * 
	 * @param logTokens
	 *            TLOG tokens
	 * @return returns <tt>true</tt> if user in GRACE state, otherwise
	 *         <tt>false</tt>
	 */
	private static boolean isUserInGrace(String[] logTokens)
	{
		/*
		 * -> Token - 19 equals to 'N' means moved from ACTIVE to GRACE state.
		 * -> Token - 19 equals to 'W' means moved from ACTIVATION_PENDING to
		 * GRACE state.
		 * -> Token - 17 equals to '0' means its the state transition log, not
		 * the retrying request.
		 */
		return ((logTokens[19].equalsIgnoreCase("N") || logTokens[19]
				.equalsIgnoreCase("W")) && logTokens[17].equalsIgnoreCase("0"));
	}

	/**
	 * Returns <tt>true</tt> if user in SUSPENSION state, otherwise
	 * <tt>false</tt>.
	 * 
	 * @param logTokens
	 *            TLOG tokens
	 * @return returns <tt>true</tt> if user in SUSPENSION state, otherwise
	 *         <tt>false</tt>
	 */
	private static boolean isUserInSuspension(String[] logTokens)
	{
		/*
		 * -> Token - 19 equals to 'M' means moved from ACTIVE to SUSPENSION
		 * state.
		 * -> Token - 19 equals to 'T' means moved from GRACE to SUSPENSION
		 * state.
		 */
		return (logTokens[19].equalsIgnoreCase("M") || logTokens[19]
				.equalsIgnoreCase("T"));
	}

	/**
	 * Returns <tt>true</tt> if user in DEACTIVE state, otherwise <tt>false</tt>
	 * .
	 * 
	 * @param logLine
	 *            a line from TLOG
	 * @param logTokens
	 *            TLOG tokens
	 * @return returns <tt>true</tt> if user in DEACTIVE state, otherwise
	 *         <tt>false</tt>
	 */
	private static boolean isUserDectivated(String logLine, String[] logTokens)
	{
		/*
		 * -> Token - 7 equals to 'D' means deactivation request and if log
		 * contains 'DCT=1' or 'DCT=10' means successfully deactivated.
		 * -> Token - 19 equals to 'U' means moved from GRACE to DEACTIVE state.
		 * -> Token - 19 equals to 'X' means moved from ACTIVATION_GRACE to
		 * DEACTIVE state.
		 * -> Token - 19 equals to 'Y' means moved from ACTIVATION_PENDING to
		 * DEACTIVE state.
		 * -> Token - 19 equals to 'V' means moved from SUSPENSION to DEACTIVE
		 * state.
		 */
		return ((logTokens[7].equalsIgnoreCase("D") && (logLine
				.contains("DCT=1,") || logLine.contains("DCT=10,")))
				|| (logTokens[19].equalsIgnoreCase("U") || logTokens[19]
						.equalsIgnoreCase("X"))
				|| logTokens[19].equalsIgnoreCase("Y") || logTokens[19]
				.equalsIgnoreCase("V"));
	}

	/**
	 * Returns <tt>true</tt> if user in ERROR state, otherwise <tt>false</tt>.
	 * 
	 * @param logTokens
	 *            TLOG tokens
	 * @return returns <tt>true</tt> if user in ERROR state, otherwise
	 *         <tt>false</tt>
	 */
	private static boolean isUserInError(String[] logTokens)
	{
		/*
		 * -> Token - 7 equals to 'E' means ERROR.
		 */
		return (logTokens[7].equalsIgnoreCase("E"));
	}
}
