package com.onmobile.apps.ringbacktones.interfaces.sm.callback.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.onmobile.apps.ringbacktones.interfaces.sm.callback.action.CallbackAction;
public class CallbackFlowConstructor
{
	
	private static Logger logger = Logger.getLogger(CallbackFlowConstructor.class);

	public static void loadCallbacks()
	{
		HashMap<String, ArrayList<String>> callbackActionsMap = getCallbackDefinitions("callback-def.yaml");
		createCallbacks(callbackActionsMap);
	}
	
	public static HashMap<String, ArrayList<String>> getCallbackDefinitions(String fileName)
	{
		InputStream in = CallbackFlowConstructor.class.getClassLoader().getResourceAsStream(fileName);
		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<String>> callbackActionsMap = (HashMap<String, ArrayList<String>>)new Yaml().load(in);
		return callbackActionsMap;
	}
	
	public static void createCallbacks(HashMap<String, ArrayList<String>> callbackActionsMap)
	{
		for(String key : callbackActionsMap.keySet())
		{
			String actionNameStr = null;
			try
			{
				ArrayList<String> actionList = callbackActionsMap.get(key);
			
				CallbackRequest callbackRequest = new CallbackRequest(key);
				
				for(int index = 0; index < actionList.size(); index++)
				{
					actionNameStr = actionList.get(index);
					CallbackAction callbackAction = new CallbackAction(actionNameStr);
					callbackRequest.addCallBackAction(callbackAction);
				}
				CallbackStore.addCallbackRequest(callbackRequest);
			}
			catch(IllegalArgumentException iae)
			{
				logger.fatal("Wrong callback request/action name callbackRequest="+key +", callbackAction="+actionNameStr, iae);
			}
			catch(Exception e)
			{
				logger.fatal("Issue while defining callback", e);
			}
		}
	}
}
