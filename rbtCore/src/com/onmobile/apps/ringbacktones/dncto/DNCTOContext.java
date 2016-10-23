/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onmobile.apps.ringbacktones.dncto.DNCTOConstants.SubscriberStatus;
import com.onmobile.apps.ringbacktones.dncto.rules.Rule;
import com.onmobile.dnctoservice.exception.DNCTOException;
import com.onmobile.dnctoservice.plugin.util.DNCTOChannel;
import com.onmobile.dnctoservice.plugin.util.DNCTOPluginUtil;

/**
 * <tt>DNCTOContext</tt> holds the data required for the applying the RBT DNCTO
 * Rules. <tt>DNCTOContext</tt> will be passed to the
 * {@link Rule#applyRule(DNCTOContext)} by the {@link RBTRuleEngine}.
 * 
 * @author vinayasimha.patil
 */
public class DNCTOContext
{
	/**
	 * Log4j logger object
	 */
	private static Logger logger = Logger.getLogger(DNCTOContext.class);

	/**
	 * Holds MSISND for which rules has to be applied.
	 */
	private String subscriberID = null;

	/**
	 * Holds the current status of the subscriber.
	 */
	private SubscriberStatus subscriberStatus = null;

	/**
	 * Holds the DNCTO details of the subscriber.
	 */
	JSONObject dnctoJsonObject = null;

	/**
	 * Holds the reason of rule violation.
	 */
	private String reason = null;
	
	//RBT-10224
	private String line=null;
	
	/**
	 * @return the line
	 */
	public String getLine() {
		return line;
	}

	/**
	 * @param line the line to set
	 */
	public void setLine(String line) {
		this.line = line;
	}

	 /** Constructs the DNCTOContext.
	 */
	public DNCTOContext()
	{
	}

	/**
	 * Constructs the DNCTOContext with <tt>subscriberID</tt> and
	 * <tt>dnctoJsonObject</tt>.
	 * 
	 * @param subscriberID
	 * @param dnctoJsonObject
	 * @throws DNCTOException
	 */
	public DNCTOContext(String subscriberID, JSONObject dnctoJsonObject)
			throws DNCTOException
	{
		this.subscriberID = subscriberID;
		this.dnctoJsonObject = dnctoJsonObject;
		findSubscriberStatus();
	}

	/**
	 * Returns the subscriberID.
	 * 
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * Sets the subscriberID.
	 * 
	 * @param subscriberID
	 *            the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * Returns the subscriberStatus.
	 * 
	 * @return the subscriberStatus
	 */
	public SubscriberStatus getSubscriberStatus()
	{
		return subscriberStatus;
	}

	/**
	 * Sets the subscriberStatus.
	 * 
	 * @param subscriberStatus
	 *            the subscriberStatus to set
	 */
	public void setSubscriberStatus(SubscriberStatus subscriberStatus)
	{
		this.subscriberStatus = subscriberStatus;
	}

	/**
	 * Returns the dnctoJsonObject.
	 * 
	 * @return the dnctoJsonObject
	 */
	public JSONObject getDnctoJsonObject()
	{
		return dnctoJsonObject;
	}

	/**
	 * Sets the dnctoJsonObject.
	 * 
	 * @param dnctoJsonObject
	 *            the dnctoJsonObject to set
	 * @throws DNCTOException
	 */
	public void setDnctoJsonObject(JSONObject dnctoJsonObject)
			throws DNCTOException
	{
		this.dnctoJsonObject = dnctoJsonObject;
		findSubscriberStatus();
	}

	/**
	 * @return
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * Finds the current subscriber status.
	 * 
	 * @throws DNCTOException
	 *             if unable to read the {@link #dnctoJsonObject}
	 */
	private void findSubscriberStatus() throws DNCTOException
	{
		try
		{
			DNCTOChannel dnctoChannel = DNCTOPluginUtil
					.listOfApplicationChannel().get(0);
			//RBT-10224
			JSONObject rbtJsonObject =null;
			if (dnctoJsonObject != null) {
				rbtJsonObject = dnctoJsonObject.optJSONObject(dnctoChannel
						.getName());
			}
			//end
			if (rbtJsonObject == null)
			{
				subscriberStatus = SubscriberStatus.NEW;
				return;
			}

			String status = null;
			long actionTime = 0;

			@SuppressWarnings("unchecked")
			Iterator<String> iterator = rbtJsonObject.keys();
			while (iterator.hasNext())
			{
				String tempStatus = iterator.next();
				JSONArray actionTimes = rbtJsonObject.optJSONArray(tempStatus);
				long tempActionTime = actionTimes
						.getLong(actionTimes.length() - 1);
				if (tempActionTime > actionTime)
				{
					actionTime = tempActionTime;
					status = tempStatus;
				}
			}

			subscriberStatus = SubscriberStatus.valueOf(status);
		}
		catch (JSONException e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(e.getMessage());
			dnctoException.initCause(e);
			throw dnctoException;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * Returns the string representation of the object.
	 * 
	 * @return the string representation of the object
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DNCTOContext[subscriberID = ");
		builder.append(subscriberID);
		builder.append(", subscriberStatus = ");
		builder.append(subscriberStatus);
		builder.append(", dnctoJsonObject = ");
		builder.append(dnctoJsonObject);
		builder.append(", reason = ");
		builder.append(reason);
		builder.append("]");
		return builder.toString();
	}
}
