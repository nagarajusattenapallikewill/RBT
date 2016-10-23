/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto.rules;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.onmobile.apps.ringbacktones.dncto.DNCTOContext;
import com.onmobile.apps.ringbacktones.dncto.DNCTOConstants.SubscriberStatus;
import com.onmobile.dnctoservice.exception.DNCTOException;
import com.onmobile.dnctoservice.plugin.util.DNCTOChannel;
import com.onmobile.dnctoservice.plugin.util.DNCTOPluginUtil;

/**
 * RBT DNCTO Rule based on the subscriber status.
 * 
 * @author vinayasimha.patil
 */
public class RbtStatusRule implements Rule
{
	private static Logger logger = Logger.getLogger(RbtStatusRule.class);

	/**
	 * Holds the subscriber status for which this rule applicable.
	 */
	@RuleAttribute
	private String subscriberStatus = null;

	/**
	 * Holds the period value of this rule.
	 */
	@RuleAttribute
	private String period = null;

	/**
	 * Holds the maximum number of contacts allowed in this rule.
	 */
	@RuleAttribute
	private String noOfContacts = null;

	/**
	 * Holds the reason to be written into a file when request is rejected.
	 */
	@RuleAttribute
	private String reason = null;

	/**
	 * Holds the subscriber status for which this rule applicable.
	 */
	private List<SubscriberStatus> subscriberStatusList = null;

	/**
	 * Holds the period start time in milliseconds.
	 */
	private long periodStartTime = 0;

	/**
	 * Holds the maximum number of contacts allowed in this rule.
	 */
	private int maxNoOfContacts = -1;

	/**
	 * Returns the subscriberStatus.
	 * 
	 * @return the subscriberStatus
	 */
	public String getSubscriberStatus()
	{
		return subscriberStatus;
	}

	/**
	 * Sets the subscriberStatus.
	 * 
	 * @param subscriberStatus
	 *            the subscriberStatus to set
	 */
	public void setSubscriberStatus(String subscriberStatus)
	{
		this.subscriberStatus = subscriberStatus;
		subscriberStatusList = getSubscriberStatusList();
	}

	/**
	 * Returns the period.
	 * 
	 * @return the period
	 */
	public String getPeriod()
	{
		return period;
	}

	/**
	 * Sets the period.
	 * 
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(String period)
	{
		this.period = period;
		periodStartTime = getPeriodStartTimestamp();
	}

	/**
	 * Returns the noOfContacts.
	 * 
	 * @return the noOfContacts
	 */
	public String getNoOfContacts()
	{
		return noOfContacts;
	}

	/**
	 * Sets the noOfContacts.
	 * 
	 * @param noOfContacts
	 *            the noOfContacts to set
	 */
	public void setNoOfContacts(String noOfContacts)
	{
		this.noOfContacts = noOfContacts;
		maxNoOfContacts = Integer.parseInt(noOfContacts);
	}

	/**
	 * @return
	 */
	public String getReason()
	{
		return reason;
	}

	/**
	 * @param reason
	 */
	public void setReason(String reason)
	{
		this.reason = reason;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.dncto.rules.Rule#applyRule(com.onmobile
	 * .apps.ringbacktones.dncto.DNCTOContext)
	 */
	/**
	 * Returns true if subscriber is not contacted <tt>noOfContacts</tt> time in
	 * the given <tt>period</tt>.
	 */
	@Override
	public boolean applyRule(DNCTOContext dnctoContext) throws DNCTOException
	{
		try
		{
			if (!subscriberStatusList.contains(dnctoContext
					.getSubscriberStatus()))
				return false;

			if (maxNoOfContacts == 0)
			{
				// No need to do any operation as its not allowed to contact the
				// user.
				dnctoContext.setReason(getReason());
				return false;
			}
			if (maxNoOfContacts == -1)
			{
				// Subscriber can be contacted any number of times.
				return true;
			}


			int noOfTimesContacted = 0;
			JSONObject dnctoJsonObject = dnctoContext.getDnctoJsonObject();
			List<DNCTOChannel> dnctoChannels = DNCTOPluginUtil
					.listOfDeliverableChannel();

			for (DNCTOChannel dnctoChannel : dnctoChannels)
			{
				JSONArray jsonArray = dnctoJsonObject.optJSONArray(dnctoChannel
						.getName());
				if (jsonArray != null)
				{
					for (int i = 0; i < jsonArray.length(); i++)
					{
						long contactTime = jsonArray.getLong(i);
						if (contactTime >= periodStartTime)
							noOfTimesContacted++;

						if (noOfTimesContacted == maxNoOfContacts) {
							dnctoContext.setReason(getReason());
							return false;
						}
					}
				}
			}

			return true;
		}
		catch (Exception e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(e.getMessage());
			dnctoException.initCause(e);
			throw dnctoException;
		}
	}

	private List<SubscriberStatus> getSubscriberStatusList()
	{
		List<SubscriberStatus> subscriberStatusList = new ArrayList<SubscriberStatus>();

		String[] statusTokens = subscriberStatus.split("\\|");
		for (String status : statusTokens)
		{
			subscriberStatusList.add(SubscriberStatus.valueOf(status));
		}

		return subscriberStatusList;
	}

	private long getPeriodStartTimestamp()
	{
		if (period == null)
			return 0;

		Calendar calendar = Calendar.getInstance();

		char ch = period.charAt(0);
		if (ch == 'D' || ch == 'd')
		{
			int noOfDays = Integer.parseInt(period.substring(1));
			calendar.add(Calendar.DAY_OF_YEAR, -(noOfDays - 1));
		}
		else if (ch == 'M' || ch == 'm')
		{
			int noOfMonths = Integer.parseInt(period.substring(1));
			calendar.add(Calendar.MONTH, -(noOfMonths - 1));
			calendar.set(Calendar.DAY_OF_MONTH, 1);
		}
		else if (ch == 'Y' || ch == 'y')
		{
			int noOfYears = Integer.parseInt(period.substring(1));
			calendar.add(Calendar.YEAR, -(noOfYears - 1));
			calendar.set(Calendar.DAY_OF_YEAR, 1);
		}
		else if (Character.isLetter(ch))
		{
			// If the first character is invalid, then it will be trimmed and
			// considered as Number Of Days.
			int noOfDays = Integer.parseInt(period.substring(1));
			calendar.add(Calendar.DAY_OF_YEAR, -(noOfDays - 1));
		}
		else
		{
			int noOfDays = Integer.parseInt(period);
			calendar.add(Calendar.DAY_OF_YEAR, -(noOfDays - 1));
		}

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("RbtStatusRule[subscriberStatus = ");
		builder.append(subscriberStatus);
		builder.append(", period = ");
		builder.append(period);
		builder.append(", noOfContacts = ");
		builder.append(noOfContacts);
		builder.append(", subscriberStatusList = ");
		builder.append(subscriberStatusList);
		builder.append(", periodStartTime = ");
		builder.append(periodStartTime);
		builder.append(", maxNoOfContacts = ");
		builder.append(maxNoOfContacts);
		builder.append(", reason = ");
		builder.append(reason);
		builder.append("]");
		return builder.toString();
	}
}
