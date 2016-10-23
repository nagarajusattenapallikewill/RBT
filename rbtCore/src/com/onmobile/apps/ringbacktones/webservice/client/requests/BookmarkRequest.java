/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class BookmarkRequest extends Request
{
	private Integer clipID = null;
	private Integer categoryID = null;

	/**
	 * @param subscriberID
	 * @param clipID
	 */
	public BookmarkRequest(String subscriberID, Integer clipID)
	{
		super(subscriberID);
		this.clipID = clipID;
	}

	/**
	 * @param subscriberID
	 * @param clipID
	 * @param categoryID
	 */
	public BookmarkRequest(String subscriberID, Integer clipID,
			Integer categoryID)
	{
		super(subscriberID);
		this.clipID = clipID;
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

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (clipID != null) requestParams.put(param_clipID, String.valueOf(clipID));
		if (categoryID != null) requestParams.put(param_categoryID, String.valueOf(categoryID));

		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("BookmarkRequest[browsingLanguage = ");
		builder.append(browsingLanguage);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", modeInfo = ");
		builder.append(modeInfo);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", categoryID = ");
		builder.append(categoryID);
		builder.append(", clipID = ");
		builder.append(clipID);
		builder.append("]");
		return builder.toString();
	}
}
