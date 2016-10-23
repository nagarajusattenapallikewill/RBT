package com.onmobile.apps.ringbacktones.interfaces.sm.callback.core;

import com.onmobile.apps.ringbacktones.common.workunit.CallbackWorkUnit;

public class CallbackProcessor
{
	public static void processCallback(CallbackWorkUnit callbackWorkUnit)
	{
		CallbackIdentifier.identifyCallback(callbackWorkUnit);
		callbackWorkUnit.getCallbackRequest().process(callbackWorkUnit);
	}
}
