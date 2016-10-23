/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vinayasimha.patil
 *
 */
public class OMRequestHandler
{
	private int messageID;
	private Map<String, Object> lockObjMap;
	private Map<String, DSMessage> responseMap;

	public OMRequestHandler()
	{
		messageID = 0;
		lockObjMap = new HashMap<String, Object>();
		responseMap = new HashMap<String, DSMessage>();
	}

	synchronized public int getNextMessageID()
	{
		if(messageID == Integer.MAX_VALUE)
			messageID = 1;
		else
			messageID++;

		return messageID;
	}

	synchronized public void addToLockObjMap(int messageID, Object lockObj)
	{
		synchronized (lockObjMap)
		{
			lockObjMap.put(String.valueOf(messageID), lockObj);
		}
	}

	synchronized public Object getLockObject(int messageID)
	{
		synchronized (lockObjMap)
		{
			Object lockObj = lockObjMap.get(String.valueOf(messageID));
			lockObjMap.remove(String.valueOf(messageID));
			return(lockObj);
		}
	}

	synchronized public void addToResponseMap(int messageID, DSMessage dsMessage)
	{
		synchronized (responseMap)
		{
			responseMap.put(String.valueOf(messageID), dsMessage);
		}
	}

	synchronized public DSMessage getResponseMessage(int messageID)
	{
		synchronized (responseMap)
		{
			DSMessage dsMessage = responseMap.get(String.valueOf(messageID));
			responseMap.remove(String.valueOf(messageID));
			return(dsMessage);
		}
	}
}
