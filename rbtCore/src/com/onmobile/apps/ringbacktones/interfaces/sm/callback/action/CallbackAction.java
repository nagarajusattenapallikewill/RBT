package com.onmobile.apps.ringbacktones.interfaces.sm.callback.action;

import com.onmobile.apps.ringbacktones.common.workunit.CallbackWorkUnit;

public class CallbackAction
{
	private CallbackActionName callbackActionName;
	public enum CallbackActionName
	{
		Basic, SmsCreator, AdrbtNotifier, SrbtNotifier, AddDownloadOfDay, RetailerSmsCreator, DeactivateBaseUpgradeTransaction,
		ActivateBaseUpgradeTransaction, SuspendSelection, RenewCosId 
	}

	public CallbackAction( String callbackActionStr)
	{
		CallbackActionName callbackActionName = CallbackActionName.valueOf(callbackActionStr);
		this.callbackActionName = callbackActionName;
	}
	public CallbackAction()
	{
	}
	
	public void execute(CallbackWorkUnit callbackWorkUnit)
	{
		
	}
	
}
