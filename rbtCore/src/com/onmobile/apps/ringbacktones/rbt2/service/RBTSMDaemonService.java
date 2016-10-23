package com.onmobile.apps.ringbacktones.rbt2.service;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;

public interface RBTSMDaemonService {

	public HashMap<String, String> getSMURL(Subscriber subscriber, SubscriberStatus subscriberStatus,
			SubscriberDownloads subDownload, ProvisioningRequests provisioningRequests, String requestType,
			boolean sendSubTypeUnknown, HashMap<String, String> loggingInfoMap);


}
