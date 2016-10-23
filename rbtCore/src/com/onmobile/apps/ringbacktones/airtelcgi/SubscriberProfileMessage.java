/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;


/**
 * @author vinayasimha.patil
 *
 */
public class SubscriberProfileMessage extends DSMessage
{
	private byte provisioningInterface;
	private byte provisioningStatus;
	private String srcSubscriberID;
	private String subscriberProvDate;

	/**
	 * @param messageType
	 * @param messageLength
	 * @param messageID
	 * @param errorCode
	 * @param provisioningInterface
	 * @param provisioningStatus
	 * @param srcSubscriberID
	 * @param subscriberProvDate
	 */
	public SubscriberProfileMessage(int messageType, int messageLength,
			int messageID, short errorCode, byte provisioningInterface,
			byte provisioningStatus, String srcSubscriberID,
			String subscriberProvDate)
	{
		super(messageType, messageLength, messageID, errorCode);
		this.provisioningInterface = provisioningInterface;
		this.provisioningStatus = provisioningStatus;
		this.srcSubscriberID = srcSubscriberID;
		this.subscriberProvDate = subscriberProvDate;
	}

	/**
	 * @return the provisioningInterface
	 */
	public byte getProvisioningInterface()
	{
		return provisioningInterface;
	}

	/**
	 * @return the provisioningStatus
	 */
	public byte getProvisioningStatus()
	{
		return provisioningStatus;
	}

	/**
	 * @return the srcSubscriberID
	 */
	public String getSrcSubscriberID()
	{
		return srcSubscriberID;
	}

	/**
	 * @return the subscriberProvDate
	 */
	public String getSubscriberProvDate()
	{
		return subscriberProvDate;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.airtelcgi.DSMessage#toString()
	 */
	@Override
	public String toString()
	{
		String messageType = ""+ getMessageType();
		if(getMessageType() == DS_SUB_PROF_REQ)
			messageType = "DS_SUB_PROF_REQ";
		else if(getMessageType() == DS_SUB_PROF_RES)
			messageType = "DS_SUB_PROF_RES";
		else if(getMessageType() == DS_SUB_PROF_ERR)
			messageType = "DS_SUB_PROF_ERR";

		String string = "SubscriberProfileMessage[" + messageType + "|"
				+ getMessageLength() + "|" + getMessageID() + "|"
				+ getErrorCode() + "|" + provisioningInterface + "|"
				+ provisioningStatus + "|" + srcSubscriberID + "|"
				+ subscriberProvDate + "]";
		return (string);
	}
}
