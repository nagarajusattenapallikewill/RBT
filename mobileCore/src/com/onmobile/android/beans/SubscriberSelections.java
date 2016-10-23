package com.onmobile.android.beans;

import java.io.Serializable;
import java.util.Date;


public class SubscriberSelections implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String subscriberId;
	private String songName;
	private String caller;
	private String callerId;
	private String setTime;	
	private String wavFile;
	private String album;
	private String artist;
	private int clipId;
	private int categoryId;
	private int status;
	private int fromTime;
	private int fromTimeMins;
	private int toTime;
	private int toTimeMins;
	private String groupName;
	private boolean loopstatus;
	private String selInterval;
	private boolean artist_image_present;
	private String imageName;
	private String previewFile;
	private String chargingModel;
	private Date selectionTime;
	private String nextBillingDate;
	private boolean isGroupSelection = false;
	private String selStatus= "";
	private String selStatusDispStr = "";
	private String selString = "";
	private String imagePath;
	private boolean imagePresent = false;
	private Date setDate = null;
	private Date endDate = null;

	public boolean isImagePresent() {
		return imagePresent;
	}

	public void setImagePresent(boolean imagePresent) {
		this.imagePresent = imagePresent;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	public int getCategoryId(){
		return categoryId;
	}
	public void setCategoryId(int categId){
		this.categoryId=categId;
	}
	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album){
		this.album=album;
	}

	public String getArtist() {
		if(artist!=null)
		{
			return artist;
		}else {
			return "NA";
		}
	}

	public void setArtist(String artist){
		if(artist!=null)
		{
			this.artist=artist;
		}else {
			this.artist="NA";
		}
	}

	public String getCaller() {
		return caller;
	}
	public void setCaller(String caller) {
		this.caller = caller;
	}
	
	public String getSongName() {
		return songName;
	}
	public void setSongName(String songName) {
		this.songName = songName;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	
	public void setSetTime(String setTime) {
		this.setTime = setTime;
	}

	public void setWavFile(String wavFile) {
		this.wavFile = wavFile;
	}
	
	public String getWavFile(){
		return wavFile;
	}

	public String getTime(){
		return setTime;
	}

	public int getClipId(){
		return clipId;
	}

	public void setClipId(int clipId){
		this.clipId=clipId;
	}
		
	
	public void setStatus(int status) {
		// TODO Auto-generated method stub
		this.status = status;
	}
	/**
	 * @return the status
	 */
	public int getStatus() {
		return this.status;
	}
	/**
	 * set from time.
	 * @param fromTime
	 */
	public void setFromTime(int fromTime) {
		this.fromTime = fromTime;
	}
	
	/**
	 * @return the toTime
	 */
	public int getToTime() {
		return toTime;
	}
	
	/**
	 * @param toTime the toTime to set
	 */
	public void setToTime(int toTime) {
		this.toTime = toTime;
	}
	
	/**
	 * @return the fromTime
	 */
	public int getFromTime() {
		return fromTime;
	}
	
	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}
	
	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		if(groupName==null)
		{
			this.groupName = "NA";
		}else{
			this.groupName = groupName;
		}
	}
	
	/**
	 * @return the selInterval
	 */
	public String getSelInterval() {
		return selInterval;
	}
	
	/**
	 * @param selInterval the selInterval to set
	 */
	public void setSelInterval(String selInterval) {
		if(selInterval==null)
		{
			this.selInterval = "NA";
		}else{
			this.selInterval = selInterval;
		}
	}
	
	/**
	 *  ToString method
	 */
	public String toString(){
		return "Caller " + this.caller + "SongName" + this.songName + "subscriber Id" + subscriberId + "Set Time "
				+ setTime + "Wav File" + wavFile + " STATUS " + this.status + " category id " + this.categoryId
				+ " from time " + this.fromTime + " to time " + this.toTime;
	}
	public boolean isArtist_image_present() {
		return artist_image_present;
	}
	public void setArtist_image_present(boolean artist_image_present) {
		this.artist_image_present = artist_image_present;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public String getCallerId() {
		return callerId;
	}
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	public String getPreviewFile() {
		return previewFile;
	}
	public void setPreviewFile(String previewFile) {
		this.previewFile = previewFile;
	}
	public String getChargingModel() {
		return chargingModel;
	}
	public void setChargingModel(String chargingModel) {
		this.chargingModel = chargingModel;
	}
	public boolean isLoopstatus() {
		return loopstatus;
	}
	public void setLoopstatus(boolean loopstatus) {
		this.loopstatus = loopstatus;
	}
	public Date getSelectionTime() {
		return selectionTime;
	}
	public void setSelectionTime(Date selectionTime) {
		this.selectionTime = selectionTime;
	}
	public String getNextBillingDate() {
		return nextBillingDate;
	}
	public void setNextBillingDate(String nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}
	public void setGroupSelection(boolean isGroupSelection) {
		this.isGroupSelection = isGroupSelection;
	}
	public boolean isGroupSelection() {
		return isGroupSelection;
	}
	public void setSelString(String selString) {
		this.selString = selString;
	}
	public String getSelString() {
		return selString;
	}
	public void setSelStatus(String selStatus) {
		this.selStatus = selStatus;
	}
	public String getSelStatus() {
		return selStatus;
	}
	/**
	 * @param toTimeMins the toTimeMins to set
	 */
	public void setToTimeMins(int toTimeMins) {
		this.toTimeMins = toTimeMins;
	}
	/**
	 * @return the toTimeMins
	 */
	public int getToTimeMins() {
		return toTimeMins;
	}
	/**
	 * @param fromTimeMins the fromTimeMins to set
	 */
	public void setFromTimeMins(int fromTimeMins) {
		this.fromTimeMins = fromTimeMins;
	}
	/**
	 * @return the fromTimeMins
	 */
	public int getFromTimeMins() {
		return fromTimeMins;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param setDate the setDate to set
	 */
	public void setSetDate(Date setDate) {
		this.setDate = setDate;
	}

	/**
	 * @return the setDate
	 */
	public Date getSetDate() {
		return setDate;
	}

	public String getSelStatusDispStr() {
		return selStatusDispStr;
	}

	public void setSelStatusDispStr(String selStatusDispStr) {
		this.selStatusDispStr = selStatusDispStr;
	}
	
}
