package com.onmobile.apps.ringbacktones.v2.resolver.response.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleList;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;

public class SelectionResponseResolver extends AbstractSelectionResponseResolver{
	
	
	public PlayRule prepareActivateSongResponse(PlayRule playRule,String subscriberID,String callerId){
		Asset asset = null;	
		SubscriberStatus subscriberStatus = getSubscriberLatestSelection(subscriberID, callerId);
		if (subscriberStatus != null) {
			asset = getAsset(subscriberStatus);
			playRule.setAsset(asset);
		}
		return playRule;
	}
	
	public Map<String, String> prepareDeleteSongResponse(String response) throws UserException{
		Map<String, String> map = new HashMap<String, String>(2);
		Locale locale = LocaleContextHolder.getLocale();
		if(!response.equalsIgnoreCase("success")) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, response, MessageResource.DELETE_SETTING_MESSAGE);			
		}
		map.put("message",applicationContext.getMessage(MessageResource.DELETE_SETTING_MESSAGE + response.toLowerCase(), null, locale));		
		map.put("code",errorCodeMapping.getErrorCode(response.toLowerCase()).getCode());
		return map;
	}

	@Override
	public PlayRuleList prepareGetPlayRuleListResponse(Setting[] settings) throws UserException {
		// TODO Auto-generated method stub
		List<Setting> settingList = null;
		settingList=Arrays.asList(settings);
		return getPlayRuleList(settingList);
	}
}
