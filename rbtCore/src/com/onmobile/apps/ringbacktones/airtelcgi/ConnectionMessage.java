/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;


/**
 * @author vinayasimha.patil
 *
 */
public class ConnectionMessage extends DSMessage
{
	private byte[] info;

	/**
	 * @param messageType
	 * @param messageLength
	 * @param messageID
	 * @param errorCode
	 * @param info
	 */
	public ConnectionMessage(int messageType, int messageLength, int messageID,
			short errorCode, byte[] info)
	{
		super(messageType, messageLength, messageID, errorCode);
		this.info = info;
	}

	/**
	 * @return the info
	 */
	public byte[] getInfo()
	{
		return info;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.airtelcgi.DSMessage#toString()
	 */
	@Override
	public String toString()
	{
		String messageType = ""+ getMessageType();
		if(getMessageType() == DS_PING)
			messageType = "DS_PING";
		else if(getMessageType() == DS_PONG)
			messageType = "DS_PONG";
		else if(getMessageType() == DS_CONNECT_REQ)
			messageType = "DS_CONNECT_REQ";
		else if(getMessageType() == DS_CONNECT_RES)
			messageType = "DS_CONNECT_RES";
		else if(getMessageType() == DS_CONNECT_ERR)
			messageType = "DS_CONNECT_ERR";
		else if(getMessageType() == DS_DISCONNECT_REQ)
			messageType = "DS_DISCONNECT_REQ";

		String string = "ConnectionMessage["+ messageType +"|"+ getMessageLength() +"|"+ getErrorCode() +"]";
		return (string);
	}
}
