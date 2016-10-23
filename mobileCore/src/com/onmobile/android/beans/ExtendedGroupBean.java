package com.onmobile.android.beans;

import com.onmobile.apps.ringbacktones.webservice.client.beans.Group;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PredefinedGroup;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;

public class ExtendedGroupBean extends Group implements Comparable<ExtendedGroupBean>{
	private Setting selection;
	public ExtendedGroupBean() {
		super();
	}

	public ExtendedGroupBean(Group group, Setting selection){
		this.setGroupID(group.getGroupID());
		this.setGroupMembers(group.getGroupMembers());
		this.setGroupName(group.getGroupName());
		this.setGroupNamePrompt(group.getGroupNamePrompt());
		this.setGroupPromoID(group.getGroupPromoID());
		this.setGroupStatus(group.getGroupStatus());
		this.setNoOfActiveMembers(group.getNoOfActiveMembers());
		this.setPredefinedGroupID(group.getPredefinedGroupID());
		this.setSubscriberID(group.getSubscriberID());
		this.setSelection(selection);
	}
	
	public ExtendedGroupBean(PredefinedGroup predefinedGroup, Group group){
		this.setGroupID(predefinedGroup.getGroupID());
		this.setGroupName(predefinedGroup.getGroupName());
		this.setGroupNamePrompt(predefinedGroup.getGroupNamePrompt());
		if(group != null)
		this.setGroupMembers(group.getGroupMembers());
	}

	public Setting getSelection() {
		return selection;
	}

	public void setSelection(Setting selection) {
		this.selection = selection;
	}

	@Override
	public int compareTo(ExtendedGroupBean other) {
		if (this.getGroupName() != null && other.getGroupName() != null) {
			return this.getGroupName().compareToIgnoreCase(other.getGroupName());
		}
		return 0;
	}


}
