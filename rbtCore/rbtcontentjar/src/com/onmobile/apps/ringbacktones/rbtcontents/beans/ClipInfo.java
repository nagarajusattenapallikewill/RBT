package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;

import com.onmobile.apps.ringbacktones.rbtcontents.utils.RBTContentUtils;

public class ClipInfo implements Serializable{
	
	private static final long serialVersionUID = -1278549288652731129L;
	
	private int clipId;
	private String name;
	private String value;
	
	public int getClipId() {
		return clipId;
	}
	public void setClipId(int clipId) {
		this.clipId = clipId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = RBTContentUtils.ignoreJunkCharacters(name);
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = RBTContentUtils.ignoreJunkCharacters(value);
	}
	
	public boolean equals(Object obj) {
		if (! (obj instanceof ClipInfo)) {
			return false;
		}
		ClipInfo clipInfo = (ClipInfo)obj;
		return (this.getClipId()+"_"+this.getName()).equals(clipInfo.getClipId()+"_"+clipInfo.getName());
	}
	
	public int hashCode(){
		int hash = 7;
		hash = 31 * hash + this.getClipId() + this.getName().hashCode();
		//hash = 31 * hash + (null == this.getClipName() ? 0 : this.getClipName().hashCode());
		return hash;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("ClipInfo[clipId = ");
		builder.append(clipId);
		builder.append(", name = ");
		builder.append(name);
		builder.append(", value = ");
		builder.append(value);
		builder.append(" ]");
		return builder.toString();
	}
	
	

}
