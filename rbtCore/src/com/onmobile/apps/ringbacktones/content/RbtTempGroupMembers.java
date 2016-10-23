package com.onmobile.apps.ringbacktones.content;

public interface RbtTempGroupMembers 
{
	/*Returns group ID*/
	public int groupID();
	
	/*Returns caller ID*/
	public String callerID();
	
	/*Returns Caller Name*/
	public String callerName();
	
	/*Returns status*/
	public String status();
	
	/*Returns subscriberId*/
	public String subscriberId();
	
	/*Returns groupMemberStatus*/
	public int groupMemberStatus();
}

