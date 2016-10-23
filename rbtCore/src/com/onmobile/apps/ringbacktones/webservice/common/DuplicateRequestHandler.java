package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sridhar.sindiri
 *
 */
public class DuplicateRequestHandler
{
	/**
	 * Contains the mapping of subscriberID and the threadName corresponding to the pending request
	 */
	private static Map<String, String> pendingRequestsSubIDThreadNameMap = new HashMap<String, String>();

	/**
	 * @param subscriberID
	 * @param threadName
	 * @return
	 */
	public static synchronized boolean addPendingRequestToMap(String subscriberID, String threadName)
	{
		if (pendingRequestsSubIDThreadNameMap.containsKey(subscriberID))
			return false;

		pendingRequestsSubIDThreadNameMap.put(subscriberID, threadName);
		return  true;
	}

	/**
	 * @param subscriberID
	 */
	public static synchronized void removePendingRequestFromMap(String subscriberID)
	{
		pendingRequestsSubIDThreadNameMap.remove(subscriberID);
	}

	/**
	 * @param subscriberID
	 * @return
	 */
	public static synchronized String getThreadNameForRequestPendingSubscriber(String subscriberID)
	{
		return pendingRequestsSubIDThreadNameMap.get(subscriberID);
	}
}
