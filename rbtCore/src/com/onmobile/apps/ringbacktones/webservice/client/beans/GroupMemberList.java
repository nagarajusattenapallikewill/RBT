package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.List;

public class GroupMemberList {
	
	private List<GroupMember> groupMembers = null;
	private int totalSize;
	private int offset;
	private int count;
	
	public GroupMemberList(List<GroupMember> groupMembers) {
		this.groupMembers = groupMembers;
	}

	public List<GroupMember> getGroupMembers() {
		return groupMembers;
	}

	public void setGroupMembers(List<GroupMember> groupMembers) {
		this.groupMembers = groupMembers;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
}