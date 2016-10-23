/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;


/**
 * @author vinayasimha.patil
 *
 */
public class ToneGiftMessage extends DSMessage
{
	private byte provisioningInterface;
	private String srcSubscriberID;
	private String dstRegionID;
	private String toneID;
	private String songName;

	/**
	 * @param messageType
	 * @param messageLength
	 * @param messageID
	 * @param errorCode
	 * @param provisioningInterface
	 * @param srcSubscriberID
	 * @param dstRegionID
	 * @param toneID
	 * @param songName
	 */
	public ToneGiftMessage(int messageType, int messageLength, int messageID,
			short errorCode, byte provisioningInterface, String srcSubscriberID,
			String dstRegionID, String toneID, String songName)
	{
		super(messageType, messageLength, messageID, errorCode);
		this.provisioningInterface = provisioningInterface;
		this.srcSubscriberID = srcSubscriberID;
		this.dstRegionID = dstRegionID;
		this.toneID = toneID;
		this.songName = songName;
	}

	/**
	 * @return the provisioningInterface
	 */
	public byte getProvisioningInterface()
	{
		return provisioningInterface;
	}

	/**
	 * @return the srcSubscriberID
	 */
	public String getSrcSubscriberID()
	{
		return srcSubscriberID;
	}

	/**
	 * @return the dstRegionID
	 */
	public String getDstRegionID()
	{
		return dstRegionID;
	}

	/**
	 * @return the toneID
	 */
	public String getToneID()
	{
		return toneID;
	}

	/**
	 * @return the songName
	 */
	public String getSongName()
	{
		return songName;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.airtelcgi.DSMessage#toString()
	 */
	@Override
	public String toString()
	{
		String messageType = ""+ getMessageType();
		if(getMessageType() == DS_TONE_GIFT_REQ)
			messageType = "DS_TONE_GIFT_REQ";
		else if(getMessageType() == DS_TONE_GIFT_RES)
			messageType = "DS_TONE_GIFT_RES";
		else if(getMessageType() == DS_TONE_GIFT_ERR)
			messageType = "DS_TONE_GIFT_ERR";

		String string = "ToneGiftMessage[" + messageType + "|"
				+ getMessageLength() + "|" + getMessageID() + "|"
				+ getErrorCode() + "|" + provisioningInterface + "|"
				+ srcSubscriberID + "|" + dstRegionID + "|" + toneID + "|"
				+ songName + "]";
		return (string);
	}
}
