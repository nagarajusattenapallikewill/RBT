package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.ttml;


import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class TTMLSmsProcessor extends SmsProcessor {

	public TTMLSmsProcessor() throws RBTException
	{
	}

	@Override
	public void processTNBActivation(Task task)
	{
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String userType = subscriber.isPrepaid()?"p":"b";
		try {
			Offer[] offers = RBTSMClientHandler.getInstance().getOffer(
					subscriber.getSubscriberID(), "TNB",
					Offer.OFFER_TYPE_SUBSCRIPTION, userType, "ZERO", null);
			if (offers == null || offers.length == 0)
			{
				task.setObject(param_responseSms, getSMSTextForID(task,TNB_ACTIVATION_OFFERUSED, m_tnbOfferUsed, subscriber.getLanguage()));
				return;
			}
		} catch (RBTException e) {
			logger.error(e.getMessage(), e);
		}
		
		super.processTNBActivation(task);	
	}
}
