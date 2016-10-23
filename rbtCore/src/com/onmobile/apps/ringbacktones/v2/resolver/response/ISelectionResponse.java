package com.onmobile.apps.ringbacktones.v2.resolver.response;

import java.util.Map;

import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleList;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;

public interface ISelectionResponse {
	
	public PlayRule prepareActivateSongResponse(PlayRule playRule,String subscriberID,String callerId);
	
	public Map<String, String> prepareDeleteSongResponse(String response) throws UserException;

	public PlayRuleList prepareGetPlayRuleListResponse(Setting[] settings) throws UserException;

}
