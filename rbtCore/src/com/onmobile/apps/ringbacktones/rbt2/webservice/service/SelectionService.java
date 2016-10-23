package com.onmobile.apps.ringbacktones.rbt2.webservice.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

@Service(value="selectionWebService")
@Lazy(value=true)
public class SelectionService implements WebServiceConstants {
	
	
	public void deleteSettingByRefId(WebServiceContext task,String subscriberId) {
		SubscriberStatus subscriberStatus = null;
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (task.containsKey(param_refID))
			subscriberStatus = rbtDBManager.
							   getSelectionByRefId(subscriberId,task.getString(param_refID));
		if (subscriberStatus == null || subscriberStatus.udpId() == null)
			return;
		
		task.remove(param_refID);
		if (subscriberStatus.callerID() != null)
			task.put(param_callerID, subscriberStatus.callerID());
		if (subscriberStatus.selInterval() != null)
			task.put(param_interval,subscriberStatus.selInterval());
		if (subscriberStatus.udpId() != null)
			task.put(param_udpId, subscriberStatus.udpId());
		if (subscriberStatus.status() != -1)
			task.put(param_status, subscriberStatus.status()+"");
	}

}
