package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean;

import java.util.Date;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorRequestBean
{
	private long sequenceID = 0;
	private int status = 0;
	private String msisdn = null;
	private int operatorID = 0;
	private String sourceContentID = null;
	private String sourceContentOperator = null;
	private String targetContentID = null;
	private String addInLoop = null;
	private String subCharge = null;
	private String contentCharge = null;
	private Date requestTime = null;
	private Date mnpRequestTime = null;
	private Date mnpResponseTime = null;
	private Date contentResolveTime = null;
	private Date requestTransferTime = null;
	private String mnpRequestType = null;
	private String mnpResponseType = null;
	private String mode = null;
	private String requestType = null;
	private String extraInfo = null;
    private String modeInfo = null; 
	/**
	 * @return the sequenceID
	 */
	public long getSequenceID()
	{
		return sequenceID;
	}

	/**
	 * @param sequenceID the sequenceID to set
	 */
	public void setSequenceID(long sequenceID)
	{
		this.sequenceID = sequenceID;
	}

	/**
	 * @return the status
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}

	/**
	 * @return the msisdn
	 */
	public String getMsisdn()
	{
		return msisdn;
	}

	/**
	 * @param msisdn the msisdn to set
	 */
	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	/**
	 * @return the operatorID
	 */
	public int getOperatorID()
	{
		return operatorID;
	}

	/**
	 * @param operatorID the operatorID to set
	 */
	public void setOperatorID(int operatorID)
	{
		this.operatorID = operatorID;
	}

	/**
	 * @return the sourceContentID
	 */
	public String getSourceContentID()
	{
		return sourceContentID;
	}

	/**
	 * @param sourceContentID the sourceContentID to set
	 */
	public void setSourceContentID(String sourceContentID)
	{
		this.sourceContentID = sourceContentID;
	}

	/**
	 * @return the sourceContentOperator
	 */
	public String getSourceContentOperator()
	{
		return sourceContentOperator;
	}

	/**
	 * @param sourceContentOperator the sourceContentOperator to set
	 */
	public void setSourceContentOperator(String sourceContentOperator)
	{
		this.sourceContentOperator = sourceContentOperator;
	}

	/**
	 * @return the targetContentID
	 */
	public String getTargetContentID()
	{
		return targetContentID;
	}

	/**
	 * @param targetContentID the targetContentID to set
	 */
	public void setTargetContentID(String targetContentID)
	{
		this.targetContentID = targetContentID;
	}

	/**
	 * @return the addInLoop
	 */
	public String getAddInLoop()
	{
		return addInLoop;
	}

	/**
	 * @param addInLoop the addInLoop to set
	 */
	public void setAddInLoop(String addInLoop)
	{
		this.addInLoop = addInLoop;
	}

	/**
	 * @return the subCharge
	 */
	public String getSubCharge()
	{
		return subCharge;
	}

	/**
	 * @param subCharge the subCharge to set
	 */
	public void setSubCharge(String subCharge)
	{
		this.subCharge = subCharge;
	}

	/**
	 * @return the contentCharge
	 */
	public String getContentCharge()
	{
		return contentCharge;
	}

	/**
	 * @param contentCharge the contentCharge to set
	 */
	public void setContentCharge(String contentCharge)
	{
		this.contentCharge = contentCharge;
	}

	/**
	 * @return the requestTime
	 */
	public Date getRequestTime()
	{
		return requestTime;
	}

	/**
	 * @param requestTime the requestTime to set
	 */
	public void setRequestTime(Date requestTime)
	{
		this.requestTime = requestTime;
	}

	/**
	 * @return the mnpRequestTime
	 */
	public Date getMnpRequestTime()
	{
		return mnpRequestTime;
	}

	/**
	 * @param mnpRequestTime the mnpRequestTime to set
	 */
	public void setMnpRequestTime(Date mnpRequestTime)
	{
		this.mnpRequestTime = mnpRequestTime;
	}

	/**
	 * @return the mnpResponseTime
	 */
	public Date getMnpResponseTime()
	{
		return mnpResponseTime;
	}

	/**
	 * @param mnpResponseTime the mnpResponseTime to set
	 */
	public void setMnpResponseTime(Date mnpResponseTime)
	{
		this.mnpResponseTime = mnpResponseTime;
	}

	/**
	 * @return the contentResolveTime
	 */
	public Date getContentResolveTime()
	{
		return contentResolveTime;
	}

	/**
	 * @param contentResolveTime the contentResolveTime to set
	 */
	public void setContentResolveTime(Date contentResolveTime)
	{
		this.contentResolveTime = contentResolveTime;
	}

	/**
	 * @return the requestTransferTime
	 */
	public Date getRequestTransferTime()
	{
		return requestTransferTime;
	}

	/**
	 * @param requestTransferTime the requestTransferTime to set
	 */
	public void setRequestTransferTime(Date requestTransferTime)
	{
		this.requestTransferTime = requestTransferTime;
	}

	/**
	 * @return the mnpRequestType
	 */
	public String getMnpRequestType()
	{
		return mnpRequestType;
	}

	/**
	 * @param mnpRequestType the mnpRequestType to set
	 */
	public void setMnpRequestType(String mnpRequestType)
	{
		this.mnpRequestType = mnpRequestType;
	}

	/**
	 * @return the mnpResponseType
	 */
	public String getMnpResponseType()
	{
		return mnpResponseType;
	}

	/**
	 * @param mnpResponseType the mnpResponseType to set
	 */
	public void setMnpResponseType(String mnpResponseType)
	{
		this.mnpResponseType = mnpResponseType;
	}

	/**
	 * @return the mode
	 */
	public String getMode()
	{
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode)
	{
		this.mode = mode;
	}

	/**
	 * @return the requestType
	 */
	public String getRequestType()
	{
		return requestType;
	}

	/**
	 * @param requestType the requestType to set
	 */
	public void setRequestType(String requestType)
	{
		this.requestType = requestType;
	}

	/**
	 * @return the extraInfo
	 */
	public String getExtraInfo()
	{
		return extraInfo;
	}

	/**
	 * @param extraInfo the extraInfo to set
	 */
	public void setExtraInfo(String extraInfo)
	{
		this.extraInfo = extraInfo;
	}

	public String getModeInfo() {
		return modeInfo;
	}

	public void setModeInfo(String modeInfo) {
		this.modeInfo = modeInfo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ContentInterOperatorRequestBean [sequenceID=").append(sequenceID)
		.append(", status=").append(status)
		.append(", msisdn=").append(msisdn)
		.append(", operatorID=").append(operatorID)
		.append(", sourceContentID=").append(sourceContentID)
		.append(", sourceContentOperator=").append(sourceContentOperator)
		.append(", targetContentID=").append(targetContentID)
		.append(", addInLoop=").append(addInLoop)
		.append(", subCharge=").append(subCharge)
		.append(", contentCharge=").append(contentCharge)
		.append(", requestTime=").append(requestTime)
		.append(", mnpRequestTime=").append(mnpRequestTime)
		.append(", mnpResponseTime=").append(mnpResponseTime)
		.append(", contentResolveTime=").append(contentResolveTime)
		.append(", requestTransferTime=").append(requestTransferTime)
		.append(", mnpRequestType=").append(mnpRequestType)
		.append(", mnpResponseType=").append(mnpResponseType)
		.append(", mode=").append(mode)
		.append(", requestType=").append(requestType)
		.append(", extraInfo=").append(extraInfo)
		.append(", modeInfo=").append(modeInfo)
		.append("]");

		return builder.toString();
	}
}
