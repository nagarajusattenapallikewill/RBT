package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.vodafone;

import java.util.ArrayList;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;

public class VodafoneSmsProcessor extends SmsProcessor
{
	public VodafoneSmsProcessor() throws RBTException
	{
		
	}

	public void preProcess(Task task)
	{
		String requestType = task.getString(param_requesttype);
		if(requestType != null)
		{
			task.setObject(param_isValid,"INVALID");
			if(requestType.equalsIgnoreCase(type_content_validator))
				task.setObject(param_ocg_charge_id, "NOTVALID");
			if(requestType.equalsIgnoreCase(type_song_set))
				task.setObject(param_responseSms, getSMSTextForID(task,HELP_SMS_TEXT, m_helpDefault,null));
			return;
		}
		super.preProcess(task);
		if(requestType != null && requestType.equalsIgnoreCase(type_content_validator))
		{
			String feature = task.getTaskAction();
			if ( !(feature.equalsIgnoreCase(ACTIVATE_N_SELECTION) || feature.equalsIgnoreCase(CRICKET_KEYWORD) || feature.equalsIgnoreCase(PROMOTION1)
				|| feature.equalsIgnoreCase(PROMOTION2) || feature.equalsIgnoreCase(SONG_PROMOTION1) || feature.equalsIgnoreCase(SONG_PROMOTION2)
				|| feature.equalsIgnoreCase(VIRAL_KEYWORD) || feature.equalsIgnoreCase(MGM_ACCEPT_KEY)
				|| feature.equalsIgnoreCase(CATEGORY_SEARCH_KEYWORD) || feature.equalsIgnoreCase(REQUEST_RBT_KEYWORD))) 
				task.setObject(param_ocg_charge_id, "NOTVALID"); 
		}
//		task.setObject(param_transid, task.getString(param_TRANS_ID));
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#getFeature(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	
}
