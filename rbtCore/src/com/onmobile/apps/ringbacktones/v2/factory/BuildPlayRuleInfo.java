package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.PlayRuleInfo;

public class BuildPlayRuleInfo {

	PlayRuleInfo playRuleInfo = null;
	
	public BuildPlayRuleInfo(){
		playRuleInfo = new PlayRuleInfo();
	}
	
	public BuildPlayRuleInfo setPlaycount(int playcount){
		playRuleInfo.setPlayCount(playcount);
		return this;
	}
	
	public PlayRuleInfo buildPlayRuleInfo(){		
		return playRuleInfo;
	}
	
}
