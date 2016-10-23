/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class ValidateNumberRequest extends Request
{
	private String number = null;
	private String toneID = null;
	private String categoryID = null;
	private String responseValidNumbers = null;

	/**
	 * @param subscriberID
	 */
	public ValidateNumberRequest(String subscriberID)
	{
		super(subscriberID);
	}

	/**
	 * @param subscriberID
	 * @param number
	 */
	public ValidateNumberRequest(String subscriberID, String number)
	{
		super(subscriberID);
		this.number = number;
	}


	/**
	 * @param subscriberID
	 * @param number
	 * @param toneID
	 * @param categoryID
	 */
	public ValidateNumberRequest(String subscriberID, String number,
			String toneID, String categoryID)
	{
		super(subscriberID);
		this.number = number;
		this.toneID = toneID;
		this.categoryID = categoryID;
	}

	/**
	 * @return the number
	 */
	public String getNumber()
	{
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(String number)
	{
		this.number = number;
	}

	/**
	 * @return the toneID
	 */
	public String getToneID()
	{
		return toneID;
	}

	/**
	 * @param toneID the toneID to set
	 */
	public void setToneID(String toneID)
	{
		this.toneID = toneID;
	}

	/**
	 * @return the categoryID
	 */
	public String getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(String categoryID)
	{
		this.categoryID = categoryID;
	}

	public String getResponseValidNumbers() {
		return responseValidNumbers;
	}

	public void setResponseValidNumbers(String responseValidNumbers) {
		this.responseValidNumbers = responseValidNumbers;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (number != null) requestParams.put(param_number, number);
		if (toneID != null) requestParams.put(param_toneID, toneID);
		if (categoryID != null) requestParams.put(param_categoryID, categoryID);

		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ValidateNumberRequest[browsingLanguage = ");
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
		builder.append(", number = ");
		builder.append(number);
		builder.append(", toneID = ");
		builder.append(toneID);
		builder.append("]");
		return builder.toString();
	}
}
