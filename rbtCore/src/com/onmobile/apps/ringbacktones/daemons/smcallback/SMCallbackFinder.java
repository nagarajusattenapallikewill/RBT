/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.smcallback;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

/**
 * @author vinayasimha.patil
 */
public class SMCallbackFinder implements SMCallbackConstants
{
	private static Logger logger = Logger.getLogger(SMCallbackFinder.class);

	private static Map<Class<? extends SMCallback>, SMCallback> smCallbacksMap = new HashMap<Class<? extends SMCallback>, SMCallback>();
	
	public static SMCallback findCallback(SMCallbackContext callbackContext)
	{
		SMCallback smCallback = null;

		String action = callbackContext.getAction();
		if (action == null)
			return null;

		if (action.equalsIgnoreCase(ACTION_EVENT))
		{
			String eventKey = callbackContext.getEventkey();
			if (eventKey != null)
			{
				String hlrTickEventKey = RBTParametersUtils.getParamAsString(iRBTConstant.DAEMON, "HLR_TICK_EVENT_KEY", "");
				if (eventKey.equalsIgnoreCase(hlrTickEventKey))
				{
					smCallback = getSMCallback(HLRTickCallback.class);
				}
				else
				{
					String hlrUntickEventKey = RBTParametersUtils.getParamAsString(iRBTConstant.DAEMON, "HLR_UNTICK_EVENT_KEY", "");
					if (eventKey.equalsIgnoreCase(hlrUntickEventKey))
						smCallback = getSMCallback(HLRUntickCallback.class);
				}
			}
		}
		/*else if (action.equalsIgnoreCase(ACTION_DEACTIVATION))
		{
			String mode = callbackContext.getMode();
			String smInittedDeactAllowedModes = RBTParametersUtils.getParamAsString(iRBTConstant.DAEMON, "SM_INIT_DEACT_ALLOWED_MODES", "");
			if(smInittedDeactAllowedModes.trim().length() > 0) 
			{
				List<String> smInitiatedDeactAllowedModes = Arrays.asList(smInittedDeactAllowedModes.split(","));
				if(mode != null & smInitiatedDeactAllowedModes.contains(mode)) 
				{
					smCallback = getSMCallback(SMInitiatedDeactCallback.class);
				}
			}
		}*/
		else if (callbackContext.getRefid() != null && callbackContext.getRefid().startsWith("RBTGIFT"))
		{
			smCallback = getSMCallback(SMGiftCallback.class);
		}

		logger.info("smCallback: " + smCallback);
		return smCallback;
	}

	private static SMCallback getSMCallback(Class<? extends SMCallback> smCallbackClass)
	{
		SMCallback smCallback = smCallbacksMap.get(smCallbackClass);
		if (smCallback == null)
		{
			try
			{
				smCallback = smCallbackClass.newInstance();
				smCallbacksMap.put(smCallbackClass, smCallback);
			}
			catch (Exception e)
			{
				logger.error("Unbale to create instance for " + smCallbackClass.getName(), e);
			}
		}

		return smCallback;
	}
}
