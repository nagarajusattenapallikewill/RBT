/**
 * 
 */
package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;

/**
 * @author Sreekar
 */
public class ProvisioningRequests
{

	public ProvisioningRequests(String chargingClass, Date creationTime,
			String extraInfo, String mode,
			String modeInfo, Date nextRetryTime, long requestId,
			String retryCount, int status, String subscriberId,
			String transId, int type)
	{
		super();
		this.chargingClass = chargingClass;
		this.creationTime = creationTime;
		this.extraInfo = extraInfo;
		this.mode = mode;
		this.modeInfo = modeInfo;
		this.nextRetryTime = nextRetryTime;
		this.requestId = requestId;
		this.retryCount = retryCount;
		this.status = status;
		this.subscriberId = subscriberId;
		this.transId = transId;
		this.type = type;
	}
	
	public ProvisioningRequests(String subscriberId, int type, String mode,
			String modeInfo, String transId, String chargingClass, int status)
	{
		super();
		this.chargingClass = chargingClass;
		this.mode = mode;
		this.modeInfo = modeInfo;
		this.status = status;
		this.subscriberId = subscriberId;
		this.transId = transId;
		this.type = type;
	}
	
	public ProvisioningRequests(String subscriberId, int type)
	{
		super();
		this.subscriberId = subscriberId;
		this.type = type;
	}

	private String chargingClass;

	private Date creationTime;

	private String extraInfo;

	private String mode;

	private String modeInfo;

	private Date nextRetryTime;

	private long requestId;

	private String retryCount;

	private int status;

	private String subscriberId;

	private String transId;

	private int type;

	private int numMaxSelections;
	
	private int smStatus;
	
	public int getSmStatus() {
		return smStatus;
	}

	public void setSmStatus(int smStatus) {
		this.smStatus = smStatus;
	}

	public String getChargingClass()
	{
		return chargingClass;
	}

	public Date getCreationTime()
	{
		return creationTime;
	}

	public String getExtraInfo()
	{
		return extraInfo;
	}

	public String getMode()
	{
		return mode;
	}

	public String getModeInfo()
	{
		return modeInfo;
	}

	public Date getNextRetryTime()
	{
		return nextRetryTime;
	}

	public long getRequestId()
	{
		return requestId;
	}

	public String getRetryCount()
	{
		return retryCount;
	}

	public int getStatus()
	{
		return status;
	}

	public String getSubscriberId()
	{
		return subscriberId;
	}

	public String getTransId()
	{
		return transId;
	}

	public int getType()
	{
		return type;
	}

	public void setChargingClass(String chargingClass)
	{
		this.chargingClass = chargingClass;
	}

	public void setCreationTime(Date creationTime)
	{
		this.creationTime = creationTime;
	}

	public void setExtraInfo(String extraInfo)
	{
		this.extraInfo = extraInfo;
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	public void setModeInfo(String modeInfo)
	{
		this.modeInfo = modeInfo;
	}

	public void setNextRetryTime(Date nextRetryTime)
	{
		this.nextRetryTime = nextRetryTime;
	}

	public void setRequestId(long requestId)
	{
		this.requestId = requestId;
	}

	public void setRetryCount(String retryCount)
	{
		this.retryCount = retryCount;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public void setSubscriberId(String subscriberId)
	{
		this.subscriberId = subscriberId;
	}

	public void setTransId(String transId)
	{
		this.transId = transId;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	/**
	 * @return the numMaxSelections
	 */
	public int getNumMaxSelections() {
		return numMaxSelections;
	}

	/**
	 * @param numMaxSelections the numMaxSelections to set
	 */
	public void setNumMaxSelections(int numMaxSelections) {
		this.numMaxSelections = numMaxSelections;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ProvisioningRequests [chargingClass=");
		builder.append(chargingClass);
		builder.append(", creationTime=");
		builder.append(creationTime);
		builder.append(", extraInfo=");
		builder.append(extraInfo);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", modeInfo=");
		builder.append(modeInfo);
		builder.append(", nextRetryTime=");
		builder.append(nextRetryTime);
		builder.append(", requestId=");
		builder.append(requestId);
		builder.append(", retryCount=");
		builder.append(retryCount);
		builder.append(", status=");
		builder.append(status);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", transId=");
		builder.append(transId);
		builder.append(", type=");
		builder.append(type);
		builder.append(", numMaxSelections=");
		builder.append(numMaxSelections);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @author vinayasimha.patil
	 * 
	 */
	public enum Status
	{
		TOBE_PROCESSED(31);

		private int statusCode;

		Status(int statusCode)
		{
			this.statusCode = statusCode;
		}

		/**
		 * @return the statusCode
		 */
		public int getStatusCode()
		{
			return statusCode;
		}
	}

	public enum Type
	{
		SUBSCRIPTION(1),
		SELECTION(2),
		BASE_UPGRADATION(RBTParametersUtils.getParamAsInt("COMMON", "BASE_UPGRADATION_PROVISIONING_TYPE", 3));

		private int typeCode;

		Type(int typeCode)
		{
			this.typeCode = typeCode;
		}

		/**
		 * @return the typeCode
		 */
		public int getTypeCode()
		{
			return typeCode;
		}
	}

	/**
	 * ProvisioningRequest ExtraInfo Key Constants.
	 * 
	 * @author vinayasimha.patil
	 * 
	 */
	public enum ExtraInfoKey
	{
		CALLER_ID,
		CATEGORY_ID,
		CLIP_ID,
		RBT_TYPE
	}
}