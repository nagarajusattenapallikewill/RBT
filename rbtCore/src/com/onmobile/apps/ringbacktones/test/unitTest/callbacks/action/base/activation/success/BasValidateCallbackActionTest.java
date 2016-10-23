package com.onmobile.apps.ringbacktones.test.unitTest.callbacks.action.base.activation.success;


import org.junit.Before;
import org.junit.Test;

import com.onmobile.apps.ringbacktones.common.workunit.CallbackWorkUnit;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.action.base.activation.success.BasValidateCallbackAction;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.CallbackStore;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.ServiceProvider;
import com.onmobile.apps.ringbacktones.service.dblayer.bean.RbtSubscriber;
import com.onmobile.apps.ringbacktones.test.unitTest.callbacks.EnvironmentInitializer;

import static org.mockito.Mockito.*;

import org.junit.* ;

public class BasValidateCallbackActionTest
{
	@Before
    public void setUp()
	{
        EnvironmentInitializer.init();
        RbtSubscriber deactivationPendingRbtSubscriber = new RbtSubscriber();
        deactivationPendingRbtSubscriber.setSubscriptionYes("D");
        RbtSubscriber deactiveRbtSubscriber = new RbtSubscriber();
        deactiveRbtSubscriber.setSubscriptionYes("X");
        
        when(ServiceProvider.getDbService().getRbtSubscriberDao().getSubscriber("NEW_USER")).thenReturn(null);
        when(ServiceProvider.getDbService().getRbtSubscriberDao().getSubscriber("DEACTIVATION_PENDING_USER")).thenReturn(deactivationPendingRbtSubscriber);
        when(ServiceProvider.getDbService().getRbtSubscriberDao().getSubscriber("DEACTIVE_USER")).thenReturn(deactiveRbtSubscriber);
    }

	@Test
	public void test_executeNewUser()
	{
		BasValidateCallbackAction basValidateCallbackAction = new BasValidateCallbackAction();
		CallbackWorkUnit callbackWorkUnit = new CallbackWorkUnit(null, null);
		callbackWorkUnit.setCallbackRequest(CallbackStore.getCallbackRequest("BASE_ACTIVATION_SUCCESS"));
		callbackWorkUnit.setRbtSubscriber(ServiceProvider.getDbService().getRbtSubscriberDao().getSubscriber("NEW_USER"));
		basValidateCallbackAction.execute(callbackWorkUnit);
		Assert.assertTrue(callbackWorkUnit.getResponseString().equals("ERROR"));
	}
	
	@Test
	public void test_executeDeactivationPendingUser()
	{
		BasValidateCallbackAction basValidateCallbackAction = new BasValidateCallbackAction();
		CallbackWorkUnit callbackWorkUnit = new CallbackWorkUnit(null, null);
		callbackWorkUnit.setCallbackRequest(CallbackStore.getCallbackRequest("BASE_ACTIVATION_SUCCESS"));
		callbackWorkUnit.setRbtSubscriber(ServiceProvider.getDbService().getRbtSubscriberDao().getSubscriber("DEACTIVATION_PENDING_USER"));
		basValidateCallbackAction.execute(callbackWorkUnit);
		Assert.assertTrue(callbackWorkUnit.getResponseString().equals("INVALID|CALLBACK ALREADY RECEIVED"));
	}
	
	@Test
	public void test_executeDeactiveUser()
	{
		BasValidateCallbackAction basValidateCallbackAction = new BasValidateCallbackAction();
		CallbackWorkUnit callbackWorkUnit = new CallbackWorkUnit(null, null);
		callbackWorkUnit.setCallbackRequest(CallbackStore.getCallbackRequest("BASE_ACTIVATION_SUCCESS"));
		callbackWorkUnit.setRbtSubscriber(ServiceProvider.getDbService().getRbtSubscriberDao().getSubscriber("DEACTIVE_USER"));
		basValidateCallbackAction.execute(callbackWorkUnit);
		Assert.assertTrue(callbackWorkUnit.getResponseString().equals("INVALID|SUBSCRIPTION DEACTIVE"));
	}
}
