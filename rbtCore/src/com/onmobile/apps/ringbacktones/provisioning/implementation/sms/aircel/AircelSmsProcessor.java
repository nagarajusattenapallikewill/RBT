package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.aircel;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;

public class AircelSmsProcessor extends SmsProcessor
{
	public AircelSmsProcessor() throws RBTException
	{
	}
	
	public void preProcess(Task task)
	{
		super.preProcess(task);
		String shortCode = task.getString(param_shortCode);
		if(shortCode != null && tokenizeArrayList(param(COMMON,ONLY_SEARCH_SHORT_CODE,""), ",").contains(shortCode))
		{
			String feature = task.getTaskAction();
			if(feature != null)
			{
				if(!feature.equals(CATEGORY_SEARCH_KEYWORD) && !feature.equals(REQUEST_RBT_KEYWORD) && !feature.equals(REQUEST_MORE_KEYWORD) )
				{
					task.setObject(param_response, getSMSTextForID(task,SERVICE_NOT_ALLOWED, m_serviceNotAllowed,null));
					task.setObject(param_isValid,"INVALID");
					return;
				}
			}	
		}
		if(task.getString(param_TRX_ID) != null)
		{
			String oldActInfo = task.getString(param_actInfo);
			String transId = task.getString(param_TRX_ID);
			String newActInfo = oldActInfo + ":trxid:"+transId+":";
			task.setObject(param_actInfo, newActInfo);
		}
		
	}
	
}
