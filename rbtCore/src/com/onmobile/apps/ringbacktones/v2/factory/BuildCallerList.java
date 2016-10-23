package com.onmobile.apps.ringbacktones.v2.factory;

import java.util.ArrayList;
import java.util.List;

import com.livewiremobile.store.storefront.dto.rbt.Caller;
import com.livewiremobile.store.storefront.dto.rbt.CallerList;
import com.livewiremobile.store.storefront.dto.rbt.Pager;

public class BuildCallerList {

	private CallerList callerList;
	
	public BuildCallerList(){
		callerList = new CallerList();
	}
	
	public BuildCallerList setPager(Pager pager) {
		callerList.setPager(pager);
		return this;
	}
	
	public BuildCallerList setCount(int count) {
		callerList.setCount(count);
		return this;
	}
	
	public BuildCallerList setCallers(List<Caller> callers) {
		callerList.setCallers(callers);
		return this;
	}
	
	public BuildCallerList addCaller(Caller caller) {
		if(callerList.getCallers() != null) {
			callerList.getCallers().add(caller);
		}
		else {
			List<Caller> callers = new ArrayList<Caller>();
			callers.add(caller);
			callerList.setCallers(callers);
		}
		return this;
	}
	
	public CallerList buildCallerList(){
		return callerList;
	}
	
}
