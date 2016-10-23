package com.onmobile.apps.ringbacktones.v2.bean;

import java.util.Date;
import java.util.List;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;

public class UDPResponseBean{
	
	private int id;

	private String subscriberId;

	private String name;
	
	private String type;

	private String extraInfo;

	private String mode;

	private Date creationTime;

	private Date updationTime;
	
	private boolean isSelActivated;
	
	private List<Clip> clips;
	
	public List<Clip> getClips() {
		return clips;
	}
	public void setClips(List<Clip> clips) {
		this.clips = clips;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public Date getUpdationTime() {
		return updationTime;
	}
	public void setUpdationTime(Date updationTime) {
		this.updationTime = updationTime;
	}
	public boolean isSelActivated() {
		return isSelActivated;
	}
	public void setSelActivated(boolean isSelActivated) {
		this.isSelActivated = isSelActivated;
	}
	
	@Override
	public String toString() {
		return "UDPResponseBean [id=" + id + ", subscriberId=" + subscriberId
				+ ", name=" + name + ", type=" + type + ", extraInfo="
				+ extraInfo + ", mode=" + mode + ", creationTime="
				+ creationTime + ", updationTime=" + updationTime
				+ ", isSelActivated=" + isSelActivated + "]";
	}
}
