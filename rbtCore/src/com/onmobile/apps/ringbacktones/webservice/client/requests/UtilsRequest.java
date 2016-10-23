/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class UtilsRequest extends Request
{
	private String senderID = null;
	private String receiverID = null;
	private String smsText = null;
	private Boolean suspend = null;
	private String info = null;
	private boolean redirectionRequired = false;

	/**
	 * @param subscriberID
	 */
	public UtilsRequest(String subscriberID)
	{
		super(subscriberID);
	}

	/**
	 * @param senderID
	 * @param receiverID
	 * @param smsText
	 */
	public UtilsRequest(String senderID, String receiverID, String smsText)
	{
		super(receiverID);
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.smsText = smsText;
	}

	/**
	 * @param subscriberID
	 * @param suspend
	 * @param mode
	 */
	public UtilsRequest(String subscriberID, Boolean suspend, String mode)
	{
		super(subscriberID);
		this.suspend = suspend;
		this.mode = mode;
	}

	/**
	 * @return the senderID
	 */
	public String getSenderID()
	{
		return senderID;
	}

	/**
	 * @param senderID the senderID to set
	 */
	public void setSenderID(String senderID)
	{
		this.senderID = senderID;
	}

	/**
	 * @return the receiverID
	 */
	public String getReceiverID()
	{
		return receiverID;
	}

	/**
	 * @param receiverID the receiverID to set
	 */
	public void setReceiverID(String receiverID)
	{
		this.receiverID = receiverID;
	}

	/**
	 * @return the smsText
	 */
	public String getSmsText()
	{
		return smsText;
	}

	/**
	 * @param smsText the smsText to set
	 */
	public void setSmsText(String smsText)
	{
		this.smsText = smsText;
	}

	/**
	 * @return the suspend
	 */
	public Boolean getSuspend()
	{
		return suspend;
	}

	/**
	 * @param suspend the suspend to set
	 */
	public void setSuspend(Boolean suspend)
	{
		this.suspend = suspend;
	}

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info)
	{
		this.info = info;
	}

	public boolean isRedirectionRequired() {
		return redirectionRequired;
	}

	public void setRedirectionRequired(boolean redirectionRequired) {
		this.redirectionRequired = redirectionRequired;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (senderID != null) requestParams.put(param_senderID, senderID);
		if (receiverID != null) requestParams.put(param_receiverID, receiverID);
		if (smsText != null) requestParams.put(param_smsText, smsText);
		if (suspend != null) requestParams.put(param_suspend, (suspend ? YES : NO));
		if (info != null) requestParams.put(param_info, info);
		if (redirectionRequired) requestParams.put(param_redirectionRequired, "true");

		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("UtilsRequest[browsingLanguage = ");
		builder.append(browsingLanguage);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", modeInfo = ");
		builder.append(modeInfo);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", info = ");
		builder.append(info);
		builder.append(", receiverID = ");
		builder.append(receiverID);
		builder.append(", senderID = ");
		builder.append(senderID);
		builder.append(", smsText = ");
		builder.append(smsText);
		builder.append(", suspend = ");
		builder.append(suspend);
		builder.append(", redirectionRequired = ");
		builder.append(redirectionRequired);
		builder.append("]");
		return builder.toString();
	}
}
