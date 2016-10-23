package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.CallingParty;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleInfo;
import com.livewiremobile.store.storefront.dto.rbt.Schedule;

public class BuildPlayRule {
	
	private PlayRule playRule = null;
	
	public BuildPlayRule() {
		playRule = new PlayRule();
	}
	
	public BuildPlayRule setId(String id) {
		playRule.setId(id);
		return this;
	}
	
	public BuildPlayRule setAsset(Asset asset) {
		playRule.setAsset(asset);
		return this;
	}
	
	public BuildPlayRule setSchedule(Schedule schedule) {
		playRule.setSchedule(schedule);
		return this;
	}
	
	public BuildPlayRule setCallingParty(CallingParty callingParty) {
		playRule.setCallingparty(callingParty);
		return this;
	}
	
	//Added for ephemeral
	public BuildPlayRule setPlayRuleInfo(PlayRuleInfo playruleinfo) {
		playRule.setPlayruleinfo(playruleinfo);
		return this;
	}
	
	public BuildPlayRule setStatus(String status) {
		playRule.setStatus(status);
		return this;
	}
	
	public BuildPlayRule setReverse(boolean reverse) {
		playRule.setReverse(reverse);
		return this;
	}

	public PlayRule buildPlayRule () {
		return playRule;
	}
}
