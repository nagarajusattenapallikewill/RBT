package com.onmobile.apps.ringbacktones.v2.factory;

import java.util.ArrayList;
import java.util.List;

import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleList;

public class BuildPlayRuleList {
	
	private PlayRuleList playRuleList = null;
	
	public BuildPlayRuleList () {
		playRuleList = new PlayRuleList();
	}
	
	public BuildPlayRuleList setCount (int count) {
		playRuleList.setCount(count);
		return this;
	}
	
	public BuildPlayRuleList setPlayrules (List<PlayRule> playRules) {
		playRuleList.setPlayrules(playRules);
		return this;
	}
	
	
	public BuildPlayRuleList addPlayRule (PlayRule playRule) {
		if (playRuleList.getPlayrules() != null) {
			playRuleList.getPlayrules().add(playRule);
		} else {
			List<PlayRule> playRules = new ArrayList<PlayRule>();
			playRules.add(playRule);
			playRuleList.setPlayrules(playRules);
		}
		return this;
	}
	
	public PlayRuleList buildPlayRuleList () {
		return playRuleList;
	}

}
