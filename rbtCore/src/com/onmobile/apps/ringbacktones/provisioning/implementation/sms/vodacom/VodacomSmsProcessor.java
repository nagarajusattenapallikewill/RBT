package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.vodacom;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CopyRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class VodacomSmsProcessor extends SmsProcessor {

	public VodacomSmsProcessor()throws RBTException  {

	}
	
	public void processCOPY(Task task)
	{
		logger.info("SmsProcessor : processCOPY : parameters are : "+task );
		
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		String callerId = task.getString(param_callerid);
		if(callerId == null)
		{
			task.setObject(param_subscriber, subscriber);
			task.setObject(param_responseSms, getSMSTextForID(task,COPY_FAILURE, m_copyFailureSMSDefault,subscriber.getLanguage()));
			return;
		}	

		if(subscriber.getSubscriberID().equals(callerId))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,COPY_FAILURE, m_copyFailureSMSDefault,subscriber.getLanguage()));
			return;
		}	
		
		CopyRequest copyRequest=new CopyRequest(subscriberID, callerId);
		CopyDetails copyDetails = RBTClient.getInstance().getCopyData(copyRequest); 
		CopyData copydata[]= copyDetails.getCopyData();
		String wavFile = null;
		String songName = null;
		Clip clip = null;
		logger.info("Copy request response is : " + copyRequest.getResponse());
		if(copyRequest.getResponse().equalsIgnoreCase(WebServiceConstants.SUCCESS)
				&& copydata != null)
		{
			if(copydata[0] != null)
			{
				logger.info("Copy Data is : " + copydata[0]);
				if(copydata[0].isShuffleOrLoop())
				{
					HashMap<String, String> hashmap = new HashMap<String, String>();
					hashmap.put("CALLER_ID", callerId);
					hashmap.put("SMS_TEXT",  getSMSTextForID(task,COPY_FAILURE_LOOP, m_copyFailureSMSLoop,subscriber.getLanguage()));
					task.setObject(param_responseSms, finalizeSmsText(hashmap));
					return;
				}
			}
			
			if (!copyDetails.isUserHasMultipleSelections()) // If User has multiple selections, not allowing the copy 
			{	
				int clipID = copydata[0].getToneID();
				clip = rbtCacheManager.getClip(clipID);
				if (clip != null)
				{
					wavFile = clip.getClipRbtWavFile();
					songName = clip.getClipName();
				}
			}
		}
		else if (copyRequest.getResponse().equalsIgnoreCase(WebServiceConstants.NOT_RBT_USER))
		{
			logger.info("SmsProcessor : processCOPY : not rbt user ");
			task.setObject(param_responseSms, getSMSTextForID(task,COPY_FAILURE_INACTIVE, getSMSTextForID(task,COPY_FAILURE, m_copyFailureSMSDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			return;
		}
		
		logger.info("SmsProcessor : processCOPY : wavFile >"+wavFile);
		if (wavFile != null)
		{	
			Setting[] setting = getActiveSubSettings(subscriberID, 1);
			if(isOverlap(setting, null, clip.getClipId()+"",subscriber.getLanguage()))
			{
				task.setObject(param_responseSms, getSMSTextForID(task,COPY_OVERLAP_FAILURE, m_copyFailureSMSDefault,subscriber.getLanguage()));
				return;
			}	
			String mode = "SMS";
			if(param(SMS, PROCESS_SMSUI_COPY_AS_OPTIN, true))
				mode = null;
			addViraldata(callerId, subscriber.getSubscriberID(), "COPY", wavFile, mode, 0, null);
			HashMap<String, String> hashmap = new HashMap<String, String>();
			hashmap.put("CALLER_ID", callerId);
			hashmap.put("SONG_NAME", songName);
			hashmap.put("SMS_TEXT",  getSMSTextForID(task,COPY_SUCCESS, m_copySuccessSMSDefault,subscriber.getLanguage()));
			task.setObject(param_responseSms, finalizeSmsText(hashmap));
		}
		else
		{
			task.setObject(param_responseSms, getSMSTextForID(task,COPY_FAILURE, m_copyFailureSMSDefault,subscriber.getLanguage()));
			return;
		}
	}
	
}
