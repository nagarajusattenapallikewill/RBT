package com.onmobile.apps.ringbacktones.ussd.common;

import java.util.Date;

public class ClipBean {
	private String songName=null;
	private int clipId=0;
	private int catId=0;
	private String wavfile=null;
	private String smsAlias=null;
	private int status=1;
	private Date startDate;
	private Date endDate;
	private int profilePeriod=1;
	private String promoID;
	private String Album;
	private String Artist;
	public String getSongName() {
		return songName;
	}
	public void setSongName(String songName) {
		this.songName = songName;
	}
	public int getClipId() {
		return clipId;
	}
	public void setClipId(int clipId) {
		this.clipId = clipId;
	}
	public int getCatId() {
		return catId;
	}
	public void setCatId(int catId) {
		this.catId = catId;
	}
	public String getWavfile() {
		return wavfile;
	}
	public void setWavfile(String wavfile) {
		this.wavfile = wavfile;
	}
	public String getSmsAlias() {
		return smsAlias;
	}
	public void setSmsAlias(String smsAlias) {
		this.smsAlias = smsAlias;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public int getProfilePeriod() {
		return profilePeriod;
	}
	public void setProfilePeriod(int profilePeriod) {
		this.profilePeriod = profilePeriod;
	}
	public String getPromoID() {
		return promoID;
	}
	public void setPromoID(String promoID) {
		this.promoID = promoID;
	}
	public String getAlbum() {
		return Album;
	}
	public void setAlbum(String album) {
		Album = album;
	}
	public String getArtist() {
		return Artist;
	}
	public void setArtist(String artist) {
		Artist = artist;
	}
	
	
}
