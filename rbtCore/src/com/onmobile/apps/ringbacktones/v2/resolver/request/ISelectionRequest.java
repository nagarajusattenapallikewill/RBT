package com.onmobile.apps.ringbacktones.v2.resolver.request;

import java.util.Map;

import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleList;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public interface ISelectionRequest {
	
	
	public PlayRule activateSong(PlayRule playRule,String subscriberID,
			String mode) throws UserException;
	
	public Map<String, String> deactivateSong(String toneId, String subscriberID, String mode)  throws UserException;
	
	public PlayRuleList getPlayRules(String type, 
			String msisdn, String id, String status) throws UserException;
	
	//Added for delete ephemeral selection
	public String deleteEphemeralRBTSelection(String msisdn, String caller, String wavFileName , String categoryId, int status, String mode);
}
