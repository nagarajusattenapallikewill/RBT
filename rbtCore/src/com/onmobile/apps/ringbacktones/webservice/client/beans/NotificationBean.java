package com.onmobile.apps.ringbacktones.webservice.client.beans;

public class NotificationBean {
	
	private String contentType;
	private String title;
	private int clipId;
	private String clipName;
	private String album;
	private String artist;
	private String imageFilePath;
	private String clipcontentType;
	private int categoryId;
	private String categoryName;
	
	public NotificationBean() {}
	
	public NotificationBean(String contentType,
			String title, int clipId, String clipName, String album,
			String artist, String imageFilePath, String clipcontentType,
			int categoryId, String categoryName) {
		super();
		this.contentType = contentType;
		this.title = title;
		this.clipId = clipId;
		this.clipName = clipName;
		this.album = album;
		this.artist = artist;
		this.imageFilePath = imageFilePath;
		this.clipcontentType = clipcontentType;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
	}
	
	public NotificationBean(String contentType, String title, int categoryId, 
			String categoryName) {
		super();
		this.contentType = contentType;
		this.title = title;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
	}
	
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
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
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getImageFilePath() {
		return imageFilePath;
	}
	public void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}
	public String getClipcontentType() {
		return clipcontentType;
	}
	public void setClipcontentType(String clipcontentType) {
		this.clipcontentType = clipcontentType;
	}
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
}
