/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class CopyRequest extends Request
{
	private String fromSubscriber = null;
	private Integer categoryID = null;
	private Integer clipID = null;
	private Integer status = null;
	private String callerID = null;

	/**
	 * @param subscriberID
	 * @param fromSubscriber
	 */
	public CopyRequest(String subscriberID, String fromSubscriber)
	{
		super(subscriberID);
		this.fromSubscriber = fromSubscriber;
	}

	/**
	 * @param subscriberID
	 * @param fromSubscriber
	 * @param categoryID
	 * @param clipID
	 * @param status
	 * @param callerID
	 */
	public CopyRequest(String subscriberID, String fromSubscriber,
			Integer categoryID, Integer clipID, Integer status, String callerID)
	{
		super(subscriberID);
		this.fromSubscriber = fromSubscriber;
		this.categoryID = categoryID;
		this.clipID = clipID;
		this.status = status;
		this.callerID = callerID;
	}

	/**
	 * @return the fromSubscriber
	 */
	public String getFromSubscriber()
	{
		return fromSubscriber;
	}

	/**
	 * @param fromSubscriber the fromSubscriber to set
	 */
	public void setFromSubscriber(String fromSubscriber)
	{
		this.fromSubscriber = fromSubscriber;
	}

	/**
	 * @return the categoryID
	 */
	public Integer getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(Integer categoryID)
	{
		this.categoryID = categoryID;
	}

	/**
	 * @return the clipID
	 */
	public Integer getClipID()
	{
		return clipID;
	}

	/**
	 * @param clipID the clipID to set
	 */
	public void setClipID(Integer clipID)
	{
		this.clipID = clipID;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus()
	{
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status)
	{
		this.status = status;
	}

	/**
	 * @return the callerID
	 */
	public String getCallerID()
	{
		return callerID;
	}

	/**
	 * @param callerID the callerID to set
	 */
	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (fromSubscriber != null) requestParams.put(param_fromSubscriber, fromSubscriber);
		if (categoryID != null) requestParams.put(param_categoryID, String.valueOf(categoryID));
		if (clipID != null) requestParams.put(param_clipID, String.valueOf(clipID));
		if (status != null) requestParams.put(param_status, String .valueOf(status));
		if (callerID != null) requestParams.put(param_callerID, callerID);

		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CopyRequest[browsingLanguage = ");
		builder.append(browsingLanguage);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", modeInfo = ");
		builder.append(modeInfo);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", callerID = ");
		builder.append(callerID);
		builder.append(", categoryID = ");
		builder.append(categoryID);
		builder.append(", clipID = ");
		builder.append(clipID);
		builder.append(", fromSubscriber = ");
		builder.append(fromSubscriber);
		builder.append(", status = ");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
}
