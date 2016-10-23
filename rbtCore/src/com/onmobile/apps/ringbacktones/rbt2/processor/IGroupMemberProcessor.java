package com.onmobile.apps.ringbacktones.rbt2.processor;

import java.util.List;

import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;

public interface IGroupMemberProcessor {
	
	public List<GroupMember> deleteGroupMembers(String msisdn, int groupId,List<GroupMember> groupMembers) throws Exception;
	public List<GroupMember> getAllGroupMembers(String msisdn, int groupId);
	public List<GroupMember> addGroupMembers(String msisdn, int groupId,List<GroupMember> groupMembers, String mode) throws Exception;
}
