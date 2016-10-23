package com.onmobile.apps.ringbacktones.v2.resolver.response.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleList;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.factory.BuildAssetFactory;
import com.onmobile.apps.ringbacktones.v2.common.Constants;

import com.onmobile.apps.ringbacktones.v2.factory.BuildPlayRuleList;
import com.onmobile.apps.ringbacktones.v2.resolver.request.impl.SelectionRequestResolver;
import com.onmobile.apps.ringbacktones.v2.resolver.response.ISelectionResponse;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;

public abstract class AbstractSelectionResponseResolver implements ISelectionResponse{
	
	@Autowired
	protected ApplicationContext applicationContext;
	protected ResponseErrorCodeMapping errorCodeMapping;
	protected static Logger logger = Logger.getLogger(SelectionRequestResolver.class);
	
	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
	
	protected SubscriberStatus getSubscriberLatestSelection(String subscriberId, String callerId) {
		Map<String, String> whereClauseMap = new HashMap<String, String>(1);
		callerId = RBTDBManager.getInstance().subID(callerId);
		whereClauseMap.put("CALLER_ID", callerId);			
		return ServiceUtil.getSubscriberLatestSelection(subscriberId, whereClauseMap);
	}
	
	
	
	
	protected Asset getAsset(SubscriberStatus subscriberStatus){

		return BuildAssetFactory.createBuildAssetFactory().buildAssetFactoryFromSubscriberStatus(subscriberStatus);	
	}

	
	protected PlayRuleList getPlayRuleList(List<Setting> settingList) throws UserException {

		PlayRuleList playRuleList = null;

		if (settingList != null && !settingList.isEmpty()) {

			List<PlayRule> playRules = null;

			if (settingList != null && !settingList.isEmpty()) {
				playRules = ServiceUtil.getPlayRules(settingList);
			}

			if (playRules != null && !playRules.isEmpty()) {
				BuildPlayRuleList playRuleListBuilder = new BuildPlayRuleList().setPlayrules(playRules)
						.setCount(playRules.size());
				playRuleList = playRuleListBuilder.buildPlayRuleList();
			}
		} else {
			
			ServiceUtil.throwCustomUserException(errorCodeMapping, Constants.PLAY_RULE_DONT_EXIST, MessageResource.LIST_PLAY_RULE_MESSAGE);
		}
		return playRuleList;

	}
}
