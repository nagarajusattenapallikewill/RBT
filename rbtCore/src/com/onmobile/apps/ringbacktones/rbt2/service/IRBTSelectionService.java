package com.onmobile.apps.ringbacktones.rbt2.service;

import java.util.List;

import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;

public interface IRBTSelectionService {
	
	public List<Setting> getSettings(String type, 
			String msisdn, String id, String status) throws UserException;

}
