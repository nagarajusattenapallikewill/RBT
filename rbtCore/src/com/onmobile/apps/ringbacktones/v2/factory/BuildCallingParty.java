package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.Caller;
import com.livewiremobile.store.storefront.dto.rbt.CallerDefault;
import com.livewiremobile.store.storefront.dto.rbt.CallerGroup;
import com.livewiremobile.store.storefront.dto.rbt.CallingParty.CallingPartyType;

public class BuildCallingParty {
	
		
	public Object getCaller(CallingPartyType type) {
		
		if (type.toString().equalsIgnoreCase(CallingPartyType.DEFAULT.toString())) {
			return new CallerDefault();
		}
		else if (type.toString().equalsIgnoreCase(CallingPartyType.GROUP.toString())) {
			return new CallerGroup();
		}
		else if (type.toString().equalsIgnoreCase(CallingPartyType.CALLER.toString()))
			return new Caller();
		
		return null;
		
	}
	
	
}
