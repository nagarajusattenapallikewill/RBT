package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.util.Date;

public class UgcClip {

	
	public static final Date DEFAULT_CLIP_END_TIME = new Date();
	
	private int clipId;
	private String clipName;
	private String album;
	private String language;
	private String artist;
	private String clipRbtWavFile;
	private String clipPromoId;
	private Date clipStartTime;
	private Date clipEndTime;
	private String subscriberId;
	private int categoryId;
	private int parentCategoryId;
	private int rightsBody;
	private String publisher;
	private char clipStatus;
	private String clipExtraInfo;
	
	
	public int getClipId() {
		return clipId;
	}
	public void setClipId(int clipId) {
		this.clipId = clipId;
	}
	public String getClipName() {
		return clipName;
	}
	public void setClipName(String clipName) {
		this.clipName = clipName;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getClipRbtWavFile() {
		return clipRbtWavFile;
	}
	public void setClipRbtWavFile(String clipRbtWavFile) {
		this.clipRbtWavFile = clipRbtWavFile;
	}
	public String getClipPromoId() {
		return clipPromoId;
	}
	public void setClipPromoId(String clipPromoId) {
		this.clipPromoId = clipPromoId;
	}
	public Date getClipStartTime() {
		return clipStartTime;
	}
	public void setClipStartTime(Date clipStartTime) {
		this.clipStartTime = clipStartTime;
	}
	public Date getClipEndTime() {
		return clipEndTime;
	}
	public void setClipEndTime(Date clipEndTime) {
		this.clipEndTime = clipEndTime;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public int getParentCategoryId() {
		return parentCategoryId;
	}
	public void setParentCategoryId(int parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}
	public int getRightsBody() {
		return rightsBody;
	}
	public void setRightsBody(int rightsBody) {
		this.rightsBody = rightsBody;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public char getClipStatus() {
		return clipStatus;
	}
	public void setClipStatus(char clipStatus) {
		this.clipStatus = clipStatus;
	}
	public String getClipExtraInfo() {
		return clipExtraInfo;
	}
	public void setClipExtraInfo(String clipExtraInfo) {
		this.clipExtraInfo = clipExtraInfo;
	}
	
	@Override
	public String toString() {
		return "UgcClip [album=" + album + ", artist=" + artist
				+ ", categoryId=" + categoryId + ", clipEndTime=" + clipEndTime
				+ ", clipExtraInfo=" + clipExtraInfo + ", clipId=" + clipId
				+ ", clipName=" + clipName + ", clipPromoId=" + clipPromoId
				+ ", clipRbtWavFile=" + clipRbtWavFile + ", clipStartTime="
				+ clipStartTime + ", clipStatus=" + clipStatus + ", language="
				+ language + ", parentCategoryId=" + parentCategoryId
				+ ", publisher=" + publisher + ", rightsBody=" + rightsBody
				+ ", subscriberId=" + subscriberId + "]";
	}
}
