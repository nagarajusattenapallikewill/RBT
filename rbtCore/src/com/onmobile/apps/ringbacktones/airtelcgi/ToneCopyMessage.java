/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;


/**
 * @author vinayasimha.patil
 *
 */
public class ToneCopyMessage extends DSMessage
{
	private String srcSubscriberID;
	private String dstSubscriberID;
	private String toneID;

	/**
	 * @param messageType
	 * @param messageLength
	 * @param messageID
	 * @param errorCode
	 * @param srcSubscriberID
	 * @param dstSubscriberID
	 * @param toneID
	 */
	public ToneCopyMessage(int messageType, int messageLength, int messageID,
			short errorCode, String srcSubscriberID, String dstSubscriberID,
			String toneID)
	{
		super(messageType, messageLength, messageID, errorCode);
		this.srcSubscriberID = srcSubscriberID;
		this.dstSubscriberID = dstSubscriberID;
		this.toneID = toneID;
	}

	/**
	 * @return the srcSubscriberID
	 */
	public String getSrcSubscriberID()
	{
		return srcSubscriberID;
	}

	/**
	 * @return the dstSubscriberID
	 */
	public String getDstSubscriberID()
	{
		return dstSubscriberID;
	}

	/**
	 * @return the toneID
	 */
	public String getToneID()
	{
		return toneID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.airtelcgi.DSMessage#toString()
	 */
	@Override
	public String toString()
	{
		String messageType = ""+ getMessageType();
		if(getMessageType() == DS_TONE_COPY_REQ)
			messageType = "DS_TONE_COPY_REQ";
		else if(getMessageType() == DS_TONE_COPY_RES)
			messageType = "DS_TONE_COPY_RES";
		else if(getMessageType() == DS_TONE_COPY_ERR)
			messageType = "DS_TONE_COPY_ERR";

		String string = "ToneCopyMessage[" + messageType + "|"
				+ getMessageLength() + "|" + getMessageID() + "|"
				+ getErrorCode() + "|" + srcSubscriberID + "|"
				+ dstSubscriberID + "|" + toneID + "]";
		return (string);
	}
}
