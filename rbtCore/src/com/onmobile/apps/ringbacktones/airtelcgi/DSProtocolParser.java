/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 *
 */
public class DSProtocolParser implements DSProtocolConstants
{
	private static Logger logger = Logger.getLogger(DSProtocolParser.class);

	private static byte nullByte = 0x00;

	public static DSMessage convertByteBufferToDSMessage(int messageType, int messageLength, ByteBuffer msgDataBuffer)
	{
		DSMessage dsMessage = null;
		int messageID = -1;
		short errorCode = NO_ERROR;
		byte provisioningInterface = nullByte;
		String srcSubscriberID = null;
		String subscriberProvDate = null;
		byte provisioningStatus = nullByte;
		String dstSubscriberID = null;
		String toneID = null;
		String dstRegionID = null;
		String songName = null;

		byte[] bytes = msgDataBuffer.array();
		try
		{
			switch (messageType)
			{
				case DS_PING:
				case DS_PONG:
				case DS_CONNECT_REQ:
				case DS_CONNECT_RES:
				case DS_CONNECT_ERR:
				case DS_DISCONNECT_REQ:
					errorCode = new Integer(msgDataBuffer.getInt()).shortValue();
					byte[] info = new byte[bytes.length - 4];
					System.arraycopy(bytes, 4, info, 0, info.length);
					dsMessage = new ConnectionMessage(messageType, messageLength, messageID, errorCode, info);
					break;

				case DS_SUB_PROF_REQ:
				case DS_SUB_PROF_RES:
				case DS_SUB_PROF_ERR:
					messageID = msgDataBuffer.getInt();
					errorCode = msgDataBuffer.getShort();
					provisioningInterface = bytes[6];
					srcSubscriberID = getString(bytes, 8, 16);
					if(messageType == DS_SUB_PROF_RES)
					{
						subscriberProvDate = getString(bytes, 24, 15);
						provisioningStatus = bytes[7];
					}
					dsMessage = new SubscriberProfileMessage(messageType, messageLength, messageID, errorCode,
							provisioningInterface, provisioningStatus, srcSubscriberID, subscriberProvDate);
					break;

				case DS_TONE_COPY_REQ:
				case DS_TONE_COPY_RES:
				case DS_TONE_COPY_ERR:
					messageID = msgDataBuffer.getInt();
					errorCode = msgDataBuffer.getShort();
					srcSubscriberID = getString(bytes, 6, 16);
					dstSubscriberID = getString(bytes, 22, 16);
					if(messageType == DS_TONE_COPY_RES)
						toneID = getString(bytes, 38, 16);
					dsMessage = new ToneCopyMessage(messageType, messageLength, messageID, errorCode,
							srcSubscriberID, dstSubscriberID, toneID);
					break;

				case DS_TONE_GIFT_REQ:
				case DS_TONE_GIFT_RES:
				case DS_TONE_GIFT_ERR:
					messageID = msgDataBuffer.getInt();
					errorCode = msgDataBuffer.getShort();
					provisioningInterface = bytes[6];
					srcSubscriberID = getString(bytes, 7, 16);
					dstRegionID = getString(bytes, 23, 16);
					toneID = getString(bytes, 39, 16);
					songName = getString(bytes, 55, 30);
					dsMessage = new ToneGiftMessage(messageType, messageLength, messageID, errorCode, 
							provisioningInterface, srcSubscriberID, dstRegionID, toneID, songName);
					break;

				default:
					break;
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return dsMessage;
	}

	public static ByteBuffer convertDSMessageToByteBuffer(DSMessage dsMessage)
	{
		ByteBuffer bytebuffer = null;

		try
		{
			ByteBuffer msgTypeBuf = ByteBuffer.allocate(4);
			msgTypeBuf.putInt(dsMessage.getMessageType());

			ByteBuffer msgLenBuf = ByteBuffer.allocate(4);
			msgLenBuf.putInt(dsMessage.getMessageLength());

			ByteBuffer msgIDBuf = ByteBuffer.allocate(4);
			msgIDBuf.putInt(dsMessage.getMessageID());

			ByteBuffer errBuf = null;
			byte[] bytes = null;

			switch (dsMessage.getMessageType())
			{
				case DS_PING:
				case DS_PONG:
				case DS_CONNECT_REQ:
				case DS_CONNECT_RES:
				case DS_CONNECT_ERR:
				case DS_DISCONNECT_REQ:
					ConnectionMessage connectionMessage = (ConnectionMessage) dsMessage;
					errBuf = ByteBuffer.allocate(4);
					errBuf.putInt(connectionMessage.getErrorCode());
					bytes = new byte[DS_CONNECT_LEN + 8];
					System.arraycopy(msgTypeBuf.array(), 0, bytes, 0, 4);
					System.arraycopy(msgLenBuf.array(), 0, bytes, 4, 4);
					System.arraycopy(errBuf.array(), 0, bytes, 8, 4);
					if(connectionMessage.getInfo() != null)
						System.arraycopy(connectionMessage.getInfo(), 0, bytes, 12, connectionMessage.getInfo().length);
					else
						Arrays.fill(bytes, 12, 31, nullByte);
					bytebuffer = ByteBuffer.wrap(bytes);
					break;

				case DS_SUB_PROF_REQ:
				case DS_SUB_PROF_RES:
				case DS_SUB_PROF_ERR:
					SubscriberProfileMessage subscriberProfileMessage = (SubscriberProfileMessage) dsMessage;
					errBuf = ByteBuffer.allocate(2);
					errBuf.putShort(subscriberProfileMessage.getErrorCode());
					bytes = new byte[DS_SUB_PROF_LEN + 8];
					System.arraycopy(msgTypeBuf.array(), 0, bytes, 0, 4);
					System.arraycopy(msgLenBuf.array(), 0, bytes, 4, 4);
					System.arraycopy(msgIDBuf.array(), 0, bytes, 8, 4);
					System.arraycopy(errBuf.array(), 0, bytes, 12, 2);
					bytes[14] = subscriberProfileMessage.getProvisioningInterface();
					bytes[15] = subscriberProfileMessage.getProvisioningStatus();
					System.arraycopy(getBytes(subscriberProfileMessage.getSrcSubscriberID(), 16), 0, bytes, 16, 16);
					System.arraycopy(getBytes(subscriberProfileMessage.getSubscriberProvDate(), 15), 0, bytes, 32, 15);
					bytebuffer = ByteBuffer.wrap(bytes);
					break;

				case DS_TONE_COPY_REQ:
				case DS_TONE_COPY_RES:
				case DS_TONE_COPY_ERR:
					ToneCopyMessage toneCopyMessage = (ToneCopyMessage) dsMessage;
					errBuf = ByteBuffer.allocate(2);
					errBuf.putShort(toneCopyMessage.getErrorCode());
					bytes = new byte[DS_TONE_COPY_LEN + 8];
					System.arraycopy(msgTypeBuf.array(), 0, bytes, 0, 4);
					System.arraycopy(msgLenBuf.array(), 0, bytes, 4, 4);
					System.arraycopy(msgIDBuf.array(), 0, bytes, 8, 4);
					System.arraycopy(errBuf.array(), 0, bytes, 12, 2);
					System.arraycopy(getBytes(toneCopyMessage.getSrcSubscriberID(), 16), 0, bytes, 14, 16);
					System.arraycopy(getBytes(toneCopyMessage.getDstSubscriberID(), 16), 0, bytes, 30, 16);
					System.arraycopy(getBytes(toneCopyMessage.getToneID(), 16), 0, bytes, 46, 16);
					bytebuffer = ByteBuffer.wrap(bytes);
					break;

				case DS_TONE_GIFT_REQ:
				case DS_TONE_GIFT_RES:
				case DS_TONE_GIFT_ERR:
					ToneGiftMessage toneGiftMessage = (ToneGiftMessage) dsMessage;
					errBuf = ByteBuffer.allocate(2);
					errBuf.putShort(toneGiftMessage.getErrorCode());
					bytes = new byte[DS_TONE_GIFT_LEN + 8];
					System.arraycopy(msgTypeBuf.array(), 0, bytes, 0, 4);
					System.arraycopy(msgLenBuf.array(), 0, bytes, 4, 4);
					System.arraycopy(msgIDBuf.array(), 0, bytes, 8, 4);
					System.arraycopy(errBuf.array(), 0, bytes, 12, 2);
					bytes[14] = toneGiftMessage.getProvisioningInterface();
					System.arraycopy(getBytes(toneGiftMessage.getSrcSubscriberID(), 16), 0, bytes, 15, 16);
					System.arraycopy(getBytes(toneGiftMessage.getDstRegionID(), 16), 0, bytes, 31, 16);
					System.arraycopy(getBytes(toneGiftMessage.getToneID(), 16), 0, bytes, 47, 16);
					System.arraycopy(getBytes(toneGiftMessage.getSongName(), 30), 0, bytes, 63, 30);
					bytebuffer = ByteBuffer.wrap(bytes);
					break;

				default:
					break;
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		return bytebuffer; 
	}

	private static String getString(byte[] bytes, int offset, int length)
	{
		String string = new String(bytes, offset, length);
		string = string.substring(0, string.indexOf((char) nullByte));

		return string;
	}

	private static byte[] getBytes(String string, int byteArrayLength)
	{
		byte[] bytes = new byte[byteArrayLength];
		Arrays.fill(bytes, nullByte);

		if(string != null && !string.equals(""))
		{
			byte[] stringBytes = string.getBytes();
			if(stringBytes.length < byteArrayLength)
				System.arraycopy(stringBytes, 0, bytes, 0, stringBytes.length);
			else
				System.arraycopy(stringBytes, 0, bytes, 0, byteArrayLength - 1);
		}

		return bytes;
	}

	public static String getMessageType(DSMessage dsMessage)
	{
		String messageType = null;
		switch (dsMessage.getMessageType())
		{
			case DS_PING:
			case DS_CONNECT_REQ:
			case DS_DISCONNECT_REQ:
			case DS_SUB_PROF_REQ:
			case DS_TONE_COPY_REQ:
			case DS_TONE_GIFT_REQ:
				messageType = "REQUEST";
				break;

			case DS_PONG:
			case DS_CONNECT_RES:
			case DS_SUB_PROF_RES:
			case DS_TONE_COPY_RES:
			case DS_TONE_GIFT_RES:
				messageType = "RESPONSE";
				break;

			case DS_CONNECT_ERR:
			case DS_SUB_PROF_ERR:
			case DS_TONE_COPY_ERR:
			case DS_TONE_GIFT_ERR:
				messageType = "ERROR_RESPONSE";
				break;

			default:
				break;
		}

		return messageType;
	}

	public static String getSubscriberType(SubscriberProfileMessage subscriberProfileResponse)
	{
		String subscriberType = null;

		if(subscriberProfileResponse == null)
			return subscriberType;

		if(subscriberProfileResponse.getMessageType() == DS_SUB_PROF_RES)
		{
			byte ps =  subscriberProfileResponse.getProvisioningStatus();
			if(ps == 0 || ps == 1 || ps == 5 || ps == 6 || ps == 9 || ps == 10 || ps == 14 ||ps == 15 ||
					ps == 16 || ps == 20 || ps == 21 || ps == 25 || ps == 26 || ps == 28 || ps == 29)
				subscriberType = "ACTIVE";
			else
				subscriberType = "DEACTIVE";
		}
		else if(subscriberProfileResponse.getMessageType() == DSProtocolConstants.DS_SUB_PROF_ERR)
		{
			short errorCode = subscriberProfileResponse.getErrorCode();
			if(errorCode == RECORD_NOT_FOUND || errorCode == SUBSCRIBER_DOES_NOT_EXIST)
				subscriberType = "NEWUSER";
		}

		return subscriberType;
	}
}
