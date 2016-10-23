package com.onmobile.apps.ringbacktones.daemons;

public class SubMinForPlayerUpdate {
	private String m_subID;
	private char m_loopStatus;
	private int m_updateType;
	
	public SubMinForPlayerUpdate(String subID, char loopStatus) {
		m_subID = subID;
		m_loopStatus = loopStatus;
	}
	
	public String subscriberID() {
		return m_subID;
	}
	
	public char loopStatus() {
		return m_loopStatus;
	}
	
	public int updateType() {
		return m_updateType;
	}
	
	public void  setUpdateType(int uType) {
		m_updateType = uType;
	}
	
}