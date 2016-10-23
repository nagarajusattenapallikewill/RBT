package com.onmobile.apps.ringbacktones.test.unitTest.callbacks.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.* ;

import com.onmobile.apps.ringbacktones.interfaces.sm.callback.action.CallbackAction.CallbackActionName;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.CallbackFlowConstructor;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.CallbackStore;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.CallbackRequest.CallbackName;

import static org.junit.Assert.* ;

public class CallbackFlowConstructorTest
{
	   @Test
	   public void test_getCallbackDefinitions()
	   {
		   String testFile = "callback-def-test.yaml";
	       HashMap<String, ArrayList<String>> callbackActionsMap = CallbackFlowConstructor.getCallbackDefinitions(testFile);
	       assertTrue(callbackActionsMap.containsKey(CallbackName.BaseActivationSuccess.toString()));
	       ArrayList<String> actionList = new ArrayList<String>();
	       actionList.add(CallbackActionName.Basic.name());
	       actionList.add(CallbackActionName.SmsCreator.name());
	       assertTrue(callbackActionsMap.get(CallbackName.BaseActivationSuccess.toString()).equals(actionList));
	   }

	   @Test
	   public void test_createCallbacks()
	   {
		   String testFile = "callback-def-test.yaml";
	       HashMap<String, ArrayList<String>> callbackActionsMap = CallbackFlowConstructor.getCallbackDefinitions(testFile);
		   CallbackFlowConstructor.createCallbacks(callbackActionsMap);
		   assertTrue(CallbackStore.getCallbackRequest("BASE_ACTIVATION_SUCCESS").actionList.size() == 5 );
	   }
}
