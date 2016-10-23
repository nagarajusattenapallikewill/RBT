package com.onmobile.apps.ringbacktones.freemium;

import java.util.Scanner;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

public class FreemiumMemcacheTest {
	
	private static FreemiumMemcacheClient mc = null;

	public static void main(String[] args) {
		mc = FreemiumMemcacheClient.getInstance();
		boolean cacheInitialized = mc.isCacheInitialized();
		System.out.println("Freemium cache Initialised = " + cacheInitialized);
		if (!cacheInitialized) {
			System.out.println("Freemium Memcache is not Initialized");
			return;
		}
		String subscriberId = getSubscriberID();
		while (subscriberId != null) {
			String subscriberBlacklistTime = mc.getSubscriberBlacklistTime(subscriberId);
			System.out.println("BlackListed Time for the SubscriberID = " + subscriberId + " is "
					+ subscriberBlacklistTime);
			subscriberId = getSubscriberID();
		}
	}

	private static String getSubscriberID(){
		System.out.println("Enter here the Mobile number " +
				"to check for Whether it is Blacklisted or Not");
		Scanner scanner = new Scanner(System.in);
		String subscriberId = scanner.next();
		return subscriberId;
	}
	
}
