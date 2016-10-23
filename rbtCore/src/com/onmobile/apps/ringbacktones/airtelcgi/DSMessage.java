/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;


/**
 * @author vinayasimha.patil
 *
 */
public abstract class DSMessage implements DSProtocolConstants
{
	private int messageType;
	private int messageLength;
	private int messageID;
	private short errorCode;

	/**
	 * @param messageType
	 * @param messageLength
	 * @param messageID
	 * @param errorCode
	 */
	public DSMessage(int messageType, int messageLength, int messageID,
			short errorCode)
	{
		this.messageType = messageType;
		this.messageLength = messageLength;
		this.messageID = messageID;
		this.errorCode = errorCode;
	}

	/**
	 * @return the messageType
	 */
	public int getMessageType()
	{
		return messageType;
	}

	/**
	 * @return the messageLength
	 */
	public int getMessageLength()
	{
		return messageLength;
	}

	/**
	 * @return the messageID
	 */
	public int getMessageID()
	{
		return messageID;
	}

	/**
	 * @return the errorCode
	 */
	public short getErrorCode()
	{
		return errorCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	abstract public String toString();
}
