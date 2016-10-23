package com.onmobile.apps.ringbacktones.interfaces.sm.callback.core;

import java.util.ArrayList;

import com.onmobile.apps.ringbacktones.common.workunit.CallbackWorkUnit;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.action.CallbackAction;

public class CallbackRequest
{
	public CallbackName callbackName;
	public ArrayList<CallbackAction> actionList = new ArrayList<CallbackAction>(); 
	
	public enum CallbackName
	{
	    BaseActivationSuccess, BaseActivationFailure, BaseRenewalSuccess, BaseRenewalFailure,
	    BaseDeactivationSuccess, BaseDeactivationFailure, BaseGrace, BaseSuspension,
	    BaseUpgradeSuccess, BaseUpgradeFailure,
	    
	    SelectionActivationSuccess, SelectionActivationFailure, SelectionRenewalSuccess, SelectionRenewalFailure,
	    SelectionDeactivationSuccess, SelectionDeactivationFailure, SelectionGrace, SelectionSuspension,
	    SelectionUpgradeSuccess, SelectionUpgradeFailure,
	    
	    DownloadActivationSuccess, DownloadActivationFailure, DownloadRenewalSuccess, DownloadRenewalFailure,
	    DownloadDeactivationSuccess, DownloadDeactivationFailure, DownloadGrace, DownloadSuspension,
	    DownloadUpgradeSuccess, DownloadUpgradeFailure,
	    
	    PackActivationSuccess, PackActivationFailure, PackRenewalSuccess, PackRenewalFailure,
	    PackDeactivationSuccess, PackDeactivationFailure,
	    
	    GiftSuccess, GiftFailure,
	    
	    AnnouncementSuccess, AnnouncementFailure
	}
	
	public CallbackRequest(String callbackNameStr)
	{
		CallbackName callbackName = CallbackName.valueOf(callbackNameStr);
		this.callbackName = callbackName;
	}
	
	public CallbackRequest(CallbackName callbackName)
	{
		this.callbackName = callbackName;
	}
	
	public void addCallBackAction(CallbackAction callbackAction)
	{
		actionList.add(callbackAction);
	}
	
	public void process(CallbackWorkUnit callbackWorkUnit) 
	{
		try
		{
			for(int index=0; index < actionList.size(); index++)
			{
				CallbackAction callbackAction = actionList.get(index);
				callbackAction.execute(callbackWorkUnit);
				if(callbackWorkUnit.isToBeTerminated())
		        	break;
		    }
			CallBackResponseFormatter.formatResponse(callbackWorkUnit);
		}
		catch(Exception se)
		{
			callbackWorkUnit.setResponseString("ERROR");
		}
	}
	
	public static void main(String[] args)
	{
		CallbackName callbackName = CallbackName.valueOf("null");
		System.out.println(callbackName);
	}
	
	@Override
	public int hashCode()
	{
		return callbackName.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof CallbackRequest )
		{
			CallbackRequest callbackRequest = (CallbackRequest) obj;
			if(callbackName == callbackRequest.callbackName)
				return true;
		}
		return false;
	}
}
