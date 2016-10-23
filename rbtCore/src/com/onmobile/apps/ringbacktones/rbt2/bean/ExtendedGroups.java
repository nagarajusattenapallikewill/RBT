package com.onmobile.apps.ringbacktones.rbt2.bean;

import com.onmobile.apps.ringbacktones.content.Groups;

public class ExtendedGroups implements Groups {
	
	
	
	private int m_groupID;
	private String m_preGroupID;
	private String m_subscriberID;
	private String m_groupName;
	private String m_groupPromoID;
	private String m_status;
	private boolean onlyActive = true;
	private boolean onlyDeactive = true;
	
	public ExtendedGroups(int m_groupID, String m_preGroupID,
			String m_subscriberID, String m_groupName, String m_groupPromoID,
			String m_status, boolean onlyActive, boolean onlyDeactive) {
		super();
		this.m_groupID = m_groupID;
		this.m_preGroupID = m_preGroupID;
		this.m_subscriberID = m_subscriberID;
		this.m_groupName = m_groupName;
		this.m_groupPromoID = m_groupPromoID;
		this.m_status = m_status;
		this.onlyActive = onlyActive;
		this.onlyDeactive = onlyDeactive;
	}
	
	public ExtendedGroups(int m_groupID, String m_preGroupID,
			String m_subscriberID, String m_groupName, String m_groupPromoID,
			String m_status) {
		super();
		this.m_groupID = m_groupID;
		this.m_preGroupID = m_preGroupID;
		this.m_subscriberID = m_subscriberID;
		this.m_groupName = m_groupName;
		this.m_groupPromoID = m_groupPromoID;
		this.m_status = m_status;
	}


	public boolean isOnlyActive() {
		return onlyActive;
	}

	public boolean isOnlyDeactive() {
		return onlyDeactive;
	}

	public int groupID() {
		
		return m_groupID;
	}

	public String preGroupID() {
		
		return m_preGroupID;
	}

	public String groupName() {
		
		return m_groupName;
	}

	public String subID() {
		
		return m_subscriberID;
	}

	public String groupPromoID() {
		
		return m_groupPromoID;
	}

	public String status() {
		
		return m_status;
	}
	
}
