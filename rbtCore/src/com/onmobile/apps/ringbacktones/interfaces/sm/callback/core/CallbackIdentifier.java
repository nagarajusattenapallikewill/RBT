package com.onmobile.apps.ringbacktones.interfaces.sm.callback.core;

import com.onmobile.apps.ringbacktones.common.workunit.CallbackWorkUnit;

public class CallbackIdentifier
{
	public static void identifyCallback(CallbackWorkUnit workUnit)
	{
		
		String action = workUnit.getHttpServletRequest().getParameter("action");
		String status = workUnit.getHttpServletRequest().getParameter("status");
		String serviceKey = workUnit.getHttpServletRequest().getParameter("srvkey");
		
		String callbackEntityType = null;
		String callbackActionType = null;
		String callbackStatusType = null;
		String callbackType = null;
		if(serviceKey != null && serviceKey.length() > 7 && action != null && status != null)
		{
			String serviceKeyPrefix = serviceKey.substring(0,7);
			if(serviceKeyPrefix.equalsIgnoreCase("RBT_AC"))
				callbackEntityType  = "BASE";
			else if(serviceKeyPrefix.equalsIgnoreCase("RBT_SE"))
				callbackEntityType  = "SELECTION";
			
			if(action.equalsIgnoreCase("ACT"))
				callbackActionType = "ACTIVATION";
			else if (action.equalsIgnoreCase("DCT"))
				callbackActionType = "DEACTIVATION";
			else if (action.equalsIgnoreCase("REN"))
				callbackActionType = "RENEWAL";
			else if (action.equalsIgnoreCase("UPG"))
				callbackActionType = "UPGRADE";
			
			if(status.equalsIgnoreCase("SUCCESS"))
				callbackStatusType = "SUCCESS";
			else if (status.equalsIgnoreCase("FAILURE"))
				callbackStatusType = "FAILURE";
			else if (status.equalsIgnoreCase("GRC"))
				callbackStatusType = "GRACE";
			else if (status.equalsIgnoreCase("SUS"))
				callbackStatusType = "SUSPENSION";
				
			if(callbackEntityType != null && callbackActionType != null && callbackStatusType != null)
			{
				if(callbackStatusType != null && (callbackStatusType.equals("GRACE") || callbackStatusType.equals("SUSPENSION")))	
					callbackType = callbackEntityType + "_" + callbackStatusType;
				else
					callbackType = callbackEntityType + "_" + callbackActionType + "_" + callbackStatusType;
			}
			CallbackRequest callbackRequest = CallbackStore.getCallbackRequest(callbackType);
			workUnit.setCallbackRequest(callbackRequest);
		}
	}
}
