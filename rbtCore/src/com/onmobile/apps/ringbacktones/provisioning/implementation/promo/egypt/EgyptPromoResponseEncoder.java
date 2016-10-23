package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.egypt;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoResponseEncoder;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class EgyptPromoResponseEncoder extends PromoResponseEncoder {
	
	/**
	 * @throws Exception
	 */
	public EgyptPromoResponseEncoder() throws Exception
	{
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.promo.egypt.EgyptPromoResponseEncoder#encode(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String encode(Task task) {
		String response = super.encode(task);
		String reqType = task.getTaskAction();
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String lang = null;
		if (null != subscriber) {
			lang = subscriber.getLanguage();
		}
		String xmlRequired = task.getString(param_XML_REQUIRED);
		if (!"true".equalsIgnoreCase(xmlRequired)) {
			String type = ("PROMOTION_USSD_RESPONSE" + "_" + response).toUpperCase();		
			String responseText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(type, null, lang);
			if(responseText != null) {
				response = responseText;
			}		
		}

		return response;		
	}
}
