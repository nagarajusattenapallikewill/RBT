/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.vodacom;

import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.RWSmsResponseEncoder;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;

public class VodacomSmsResponseEncoder extends RWSmsResponseEncoder
{
	
	public VodacomSmsResponseEncoder() throws Exception
	{
    }

	public String encode(Task task)
	{
		String smsText = super.encode(task);
		if(smsText == null || smsText.trim().length() <= 0)
			return "";
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber) ;
		String subId = null;
		if(subscriber != null)
			subId = subscriber.getSubscriberID();
		UtilsRequest utilsRequest=new UtilsRequest(param("SMS",SMS_NO, "123456"), subId ,smsText);
		rbtClient.sendSMS(utilsRequest);
		return "";
	}
}
