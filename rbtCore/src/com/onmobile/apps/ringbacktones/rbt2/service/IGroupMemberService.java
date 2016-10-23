package com.onmobile.apps.ringbacktones.rbt2.service;

import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public interface IGroupMemberService {
	
	public Object addMemberToGroup(String msisdn, String mode, int groupId, Long callerId, String callerName) throws UserException;
	public Object removeMemberFromGroup(String msisdn, String mode ,int groupId,String callerId) throws UserException;
	public Object getAllGroupMember(String msisdn,int groupId,int pagesize,int offset) throws UserException;
	
	
}
