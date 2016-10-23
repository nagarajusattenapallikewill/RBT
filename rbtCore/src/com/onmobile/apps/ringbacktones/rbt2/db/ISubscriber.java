package com.onmobile.apps.ringbacktones.rbt2.db;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;

/**
 * 
 * @author md.alam
 *
 */

public interface ISubscriber {
	
	public boolean insert(Subscriber subscriber);
	public boolean updateSubscriber(Subscriber subscriber);
	public boolean deleteSubscriberById(String subId);
	public Subscriber getSubscriber(String msisdn);
	public Consent getConsentObject(String msisdn);

}
