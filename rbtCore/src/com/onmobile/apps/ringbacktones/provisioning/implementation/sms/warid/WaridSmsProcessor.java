package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.warid;

import java.util.ArrayList;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;

public class WaridSmsProcessor extends SmsProcessor
{
	public WaridSmsProcessor() throws RBTException
	{
		
	}

	public void preProcess(Task task)
	{
		ArrayList<String> smsList = (ArrayList<String>)task.getObject(param_smsText);
		if(smsList.contains("ssrm") || smsList.contains("sste") || smsList.contains("sstb") ||smsList.contains("sstes"))
		{
			if(param(SMS,CATEGORY_SEARCH_KEYWORD,null) != null)
				smsList.add(tokenizeArrayList(param(SMS,CATEGORY_SEARCH_KEYWORD,null), ",").get(0));
		}
		else if(smsList.contains("ssrc"))
			smsList.remove("ssrc");
		super.preProcess(task);
	}
	
}
