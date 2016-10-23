package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.Caller;

public class BuildCaller {

	Caller caller = null;
	
	public BuildCaller(){
		caller = new Caller();
//		caller.setPlayrules(new PlayRuleList(new ArrayList<PlayRule>()));
	}
	
	
	public BuildCaller setName(String name) {
		caller.setName(name);
		return this;
	}
	
	public BuildCaller setPhonenumber(String phonenumber) {
		caller.setPhonenumber(phonenumber);
		return this;
	}
	
	public BuildCaller setId(long callerId){
		caller.setId(callerId);
		return this;
	}
	
	public Caller buildCaller(){		
		return caller;
	}
	
}
