package com.onmobile.apps.ringbacktones.interfaces.sm.callback.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.CallbackRequest.CallbackName;
public class CallbackStore
{
	private static HashMap<String , CallbackRequest> callbackMap = new HashMap<String, CallbackRequest>();

	static
	{
		callbackMap.put("BASE_ACTIVATION_SUCCESS", new CallbackRequest(CallbackName.BaseActivationSuccess));
		callbackMap.put("BASE_ACTIVATION_FAILURE", new CallbackRequest(CallbackName.BaseActivationFailure));
		callbackMap.put("BASE_DEACTIVATION_SUCCESS", new CallbackRequest(CallbackName.BaseDeactivationSuccess));
		callbackMap.put("BASE_DEACTIVATION_FAILURE", new CallbackRequest(CallbackName.BaseDeactivationFailure));
		callbackMap.put("BASE_RENEWAL_SUCCESS", new CallbackRequest(CallbackName.BaseRenewalSuccess));
		callbackMap.put("BASE_RENEWAL_FAILURE", new CallbackRequest(CallbackName.BaseRenewalFailure));
		callbackMap.put("BASE_UPGRADE_SUCCESS", new CallbackRequest(CallbackName.BaseUpgradeSuccess));
		callbackMap.put("BASE_UPGRADE_FAILURE", new CallbackRequest(CallbackName.BaseUpgradeFailure));
		callbackMap.put("BASE_GRACE", new CallbackRequest(CallbackName.BaseGrace));
		callbackMap.put("BASE_SUSPENSION", new CallbackRequest(CallbackName.BaseSuspension));
		
		callbackMap.put("SELECTION_ACTIVATION_SUCCESS", new CallbackRequest(CallbackName.SelectionActivationSuccess));
		callbackMap.put("SELECTION_ACTIVATION_FAILURE", new CallbackRequest(CallbackName.SelectionActivationFailure));
		callbackMap.put("SELECTION_DEACTIVATION_SUCCESS", new CallbackRequest(CallbackName.SelectionDeactivationSuccess));
		callbackMap.put("SELECTION_DEACTIVATION_FAILURE", new CallbackRequest(CallbackName.SelectionDeactivationFailure));
		callbackMap.put("SELECTION_RENEWAL_SUCCESS", new CallbackRequest(CallbackName.SelectionRenewalSuccess));
		callbackMap.put("SELECTION_RENEWAL_FAILURE", new CallbackRequest(CallbackName.SelectionRenewalFailure));
		callbackMap.put("SELECTION_UPGRADE_SUCCESS", new CallbackRequest(CallbackName.SelectionUpgradeSuccess));
		callbackMap.put("SELECTION_UPGRADE_FAILURE", new CallbackRequest(CallbackName.SelectionUpgradeFailure));
		callbackMap.put("SELECTION_GRACE", new CallbackRequest(CallbackName.SelectionGrace));
		callbackMap.put("SELECTION_SUSPENSION", new CallbackRequest(CallbackName.SelectionSuspension));
		
		callbackMap.put("DOWNLOAD_ACTIVATION_SUCCESS", new CallbackRequest(CallbackName.DownloadActivationSuccess));
		callbackMap.put("DOWNLOAD_ACTIVATION_FAILURE", new CallbackRequest(CallbackName.DownloadActivationFailure));
		callbackMap.put("DOWNLOAD_DEACTIVATION_SUCCESS", new CallbackRequest(CallbackName.DownloadDeactivationSuccess));
		callbackMap.put("DOWNLOAD_DEACTIVATION_FAILURE", new CallbackRequest(CallbackName.DownloadDeactivationFailure));
		callbackMap.put("DOWNLOAD_RENEWAL_SUCCESS", new CallbackRequest(CallbackName.DownloadRenewalSuccess));
		callbackMap.put("DOWNLOAD_RENEWAL_FAILURE", new CallbackRequest(CallbackName.DownloadRenewalFailure));
		callbackMap.put("DOWNLOAD_UPGRADE_SUCCESS", new CallbackRequest(CallbackName.DownloadUpgradeSuccess));
		callbackMap.put("DOWNLOAD_UPGRADE_FAILURE", new CallbackRequest(CallbackName.DownloadUpgradeFailure));
		callbackMap.put("DOWNLOAD_GRACE", new CallbackRequest(CallbackName.DownloadGrace));
		callbackMap.put("DOWNLOAD_SUSPENSION", new CallbackRequest(CallbackName.DownloadSuspension));
	}
	
	public static void addCallbackRequest(CallbackRequest callbackRequest)
	{
		if(callbackMap.containsValue(callbackRequest))
		{
			for (Iterator<Entry<String, CallbackRequest>>  iterator = callbackMap.entrySet().iterator(); iterator.hasNext();)
			{
				Entry<String, CallbackRequest> entry = iterator.next();
				String key = entry.getKey();
				CallbackRequest value = entry.getValue();
				if(value.equals(callbackRequest))
					callbackMap.put(key, callbackRequest);
			}
		}
	}
	
	public static CallbackRequest getCallbackRequest(String key)
	{
		return callbackMap.get(key);
	}
}
