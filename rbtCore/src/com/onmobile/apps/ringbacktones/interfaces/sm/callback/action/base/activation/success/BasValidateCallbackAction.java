package com.onmobile.apps.ringbacktones.interfaces.sm.callback.action.base.activation.success;

import com.onmobile.apps.ringbacktones.common.workunit.CallbackWorkUnit;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.action.CallbackAction;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.CallbackRequest;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.CallbackStore;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.ServiceProvider;
import com.onmobile.apps.ringbacktones.service.dblayer.bean.RbtSubscriber;

public class BasValidateCallbackAction extends CallbackAction
{
	@Override
	public void execute(CallbackWorkUnit callbackWorkUnit)
	{
	
		CallbackRequest callbackRequest = callbackWorkUnit.getCallbackRequest();
		if( !callbackRequest.equals(CallbackStore.getCallbackRequest("BASE_ACTIVATION_SUCCESS")))
		{
			callbackWorkUnit.setToBeTerminated(true);
			return;
		}	
		
		RbtSubscriber rbtSubscriber = ServiceProvider.getDbService().getRbtSubscriberDao().getSubscriber(callbackWorkUnit.getMsisdn());
		callbackWorkUnit.setRbtSubscriber(rbtSubscriber);
		if(rbtSubscriber == null || rbtSubscriber.getSubscriptionYes() == null)
		{
			callbackWorkUnit.setToBeTerminated(true);
			callbackWorkUnit.setResponseString("ERROR");
			return;
		}
		
		if(rbtSubscriber.getSubscriptionYes().equals("D") || rbtSubscriber.getSubscriptionYes().equals("P") 
				|| rbtSubscriber.getSubscriptionYes().equals("E") || rbtSubscriber.getSubscriptionYes().equals("F")
				|| rbtSubscriber.getSubscriptionYes().equals("C") )
		{
			callbackWorkUnit.setToBeTerminated(true);
			callbackWorkUnit.setResponseString("INVALID|CALLBACK ALREADY RECEIVED");
			return;
		}
		
		if(rbtSubscriber.getSubscriptionYes().equalsIgnoreCase("X"))
		{
			callbackWorkUnit.setToBeTerminated(true);
			callbackWorkUnit.setResponseString("INVALID|SUBSCRIPTION DEACTIVE");
			return;
		}
		if(rbtSubscriber.getSubscriptionYes().equalsIgnoreCase("B"))
		{
			//Some actions
			callbackWorkUnit.setToBeTerminated(true);
			return;
		}
	}
}
