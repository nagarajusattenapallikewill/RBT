package com.onmobile.android.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;

public class ExtendedDownloadBean {
	private String subscriberID = null;
	private int toneID;
	private String shuffleID;
	private String toneName = null;
	private String toneType = null;
	private String previewFile = null;
	private String rbtFile = null;
	private String ugcRbtFile = null;
	private String downloadStatus = null;
	private String downloadType = null;
	private int categoryID;
	private String chargeClass = null;
	private String selectedBy = null;
	private String deselectedBy = null;
	private Date setTime = null;
	private Date endTime = null;
	private HashMap<String, String> downloadInfoMap = null;
	private boolean isSetForAll;
	private Date nextBillingDate;
	private String refID = null;
	private String selectionInfo = null;
	private String deselectionInfo = null;
	private Date nextChargingDate;
	private String lastChargeAmount = null;
	
	String album;
	String artist;
	String clipRbtWavFile;
	String imageFilePath;
	
	private List<CallerIdGroupBean> callerIdGroup;
	
	//RBT-14626 Signal app requirement - Mobile app server API enhancement (phase 2)
	private String clipInfo = null;
	private String ODP = null;
	
	public String getClipInfo() {
		return clipInfo;
	}

	public void setClipInfo(String clipInfo) {
		this.clipInfo = clipInfo;
	}
	
	public String getODP() {
		return ODP;
	}

	public void setODP(String ODP) {
		this.ODP = ODP;
	}

	public ExtendedDownloadBean(Download downloadObj) {
		
		this.subscriberID = downloadObj.getSubscriberID();
		this.toneID= downloadObj.getToneID();
		this.shuffleID = downloadObj.getShuffleID();
		this.toneName = downloadObj.getToneName();
		this.toneType = downloadObj.getToneType();
		this.previewFile = downloadObj.getPreviewFile();
		this.rbtFile = downloadObj.getRbtFile();
		this.ugcRbtFile = downloadObj.getUgcRbtFile();
		this.downloadStatus = downloadObj.getDownloadStatus();
		this.downloadType = downloadObj.getDownloadType();
		this.categoryID = downloadObj.getCategoryID();
		this.chargeClass = downloadObj.getChargeClass();
		this.selectedBy = downloadObj.getSelectedBy();
		this.deselectedBy = downloadObj.getDeselectedBy();
		this.setTime = downloadObj.getSetTime();
		this.endTime = downloadObj.getEndTime();
		this.nextBillingDate = downloadObj.getNextBillingDate();
		this.refID = downloadObj.getRefID();
		this.selectionInfo = downloadObj.getSelectionInfo();
		this.deselectionInfo = downloadObj.getDeselectionInfo();
		this.nextChargingDate= downloadObj.getNextChargingDate();
		this.lastChargeAmount = downloadObj.getLastChargeAmount();
		
	}
	
	public ExtendedDownloadBean(Download downloadObj, boolean isRestricted) {

		this.subscriberID = downloadObj.getSubscriberID();
		this.toneID= downloadObj.getToneID();
		this.toneName = downloadObj.getToneName();
		this.toneType = downloadObj.getToneType();
		this.previewFile = downloadObj.getPreviewFile();
		this.rbtFile = downloadObj.getRbtFile();
		this.categoryID = downloadObj.getCategoryID();
		if (!isRestricted) {
			this.shuffleID = downloadObj.getShuffleID();
			this.ugcRbtFile = downloadObj.getUgcRbtFile();
			this.downloadStatus = downloadObj.getDownloadStatus();
			this.downloadType = downloadObj.getDownloadType();
			this.chargeClass = downloadObj.getChargeClass();
			this.selectedBy = downloadObj.getSelectedBy();
			this.deselectedBy = downloadObj.getDeselectedBy();
			this.setTime = downloadObj.getSetTime();
			this.endTime = downloadObj.getEndTime();
			this.nextBillingDate = downloadObj.getNextBillingDate();
			this.refID = downloadObj.getRefID();
			this.selectionInfo = downloadObj.getSelectionInfo();
			this.deselectionInfo = downloadObj.getDeselectionInfo();
			this.nextChargingDate= downloadObj.getNextChargingDate();
			this.lastChargeAmount = downloadObj.getLastChargeAmount();
		}
	}
	public ExtendedDownloadBean(Setting setting) {
		this.subscriberID = setting.getSubscriberID();
		this.toneID= setting.getToneID();
		this.toneName = setting.getToneName();
		this.toneType = setting.getToneType();
		this.previewFile = setting.getPreviewFile();
		this.rbtFile = setting.getRbtFile();
		this.categoryID = setting.getCategoryID();
		this.refID = setting.getRefID();
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
	public String getClipRbtWavFile() {
		return clipRbtWavFile;
	}
	public void setClipRbtWavFile(String clipRbtWavFile) {
		this.clipRbtWavFile = clipRbtWavFile;
	}
	public String getImageFilePath() {
		return imageFilePath;
	}
	public void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}

	public String getSubscriberID() {
		return subscriberID;
	}

	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}

	public int getToneID() {
		return toneID;
	}

	public void setToneID(int toneID) {
		this.toneID = toneID;
	}

	public String getShuffleID() {
		return shuffleID;
	}

	public void setShuffleID(String shuffleID) {
		this.shuffleID = shuffleID;
	}

	public String getToneName() {
		return toneName;
	}

	public void setToneName(String toneName) {
		this.toneName = toneName;
	}

	public String getToneType() {
		return toneType;
	}

	public void setToneType(String toneType) {
		this.toneType = toneType;
	}

	public String getPreviewFile() {
		return previewFile;
	}

	public void setPreviewFile(String previewFile) {
		this.previewFile = previewFile;
	}

	public String getRbtFile() {
		return rbtFile;
	}

	public void setRbtFile(String rbtFile) {
		this.rbtFile = rbtFile;
	}

	public String getUgcRbtFile() {
		return ugcRbtFile;
	}

	public void setUgcRbtFile(String ugcRbtFile) {
		this.ugcRbtFile = ugcRbtFile;
	}

	public String getDownloadStatus() {
		return downloadStatus;
	}

	public void setDownloadStatus(String downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public String getDownloadType() {
		return downloadType;
	}

	public void setDownloadType(String downloadType) {
		this.downloadType = downloadType;
	}

	public int getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}

	public String getChargeClass() {
		return chargeClass;
	}

	public void setChargeClass(String chargeClass) {
		this.chargeClass = chargeClass;
	}

	public String getSelectedBy() {
		return selectedBy;
	}

	public void setSelectedBy(String selectedBy) {
		this.selectedBy = selectedBy;
	}

	public String getDeselectedBy() {
		return deselectedBy;
	}

	public void setDeselectedBy(String deselectedBy) {
		this.deselectedBy = deselectedBy;
	}

	public Date getSetTime() {
		return setTime;
	}

	public void setSetTime(Date setTime) {
		this.setTime = setTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public HashMap<String, String> getDownloadInfoMap() {
		return downloadInfoMap;
	}

	public void setDownloadInfoMap(HashMap<String, String> downloadInfoMap) {
		this.downloadInfoMap = downloadInfoMap;
	}

	public boolean isSetForAll() {
		return isSetForAll;
	}

	public void setSetForAll(boolean isSetForAll) {
		this.isSetForAll = isSetForAll;
	}

	public Date getNextBillingDate() {
		return nextBillingDate;
	}

	public void setNextBillingDate(Date nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}

	public String getRefID() {
		return refID;
	}

	public void setRefID(String refID) {
		this.refID = refID;
	}

	public String getSelectionInfo() {
		return selectionInfo;
	}

	public void setSelectionInfo(String selectionInfo) {
		this.selectionInfo = selectionInfo;
	}

	public String getDeselectionInfo() {
		return deselectionInfo;
	}

	public void setDeselectionInfo(String deselectionInfo) {
		this.deselectionInfo = deselectionInfo;
	}

	public Date getNextChargingDate() {
		return nextChargingDate;
	}

	public void setNextChargingDate(Date nextChargingDate) {
		this.nextChargingDate = nextChargingDate;
	}

	public String getLastChargeAmount() {
		return lastChargeAmount;
	}

	public void setLastChargeAmount(String lastChargeAmount) {
		this.lastChargeAmount = lastChargeAmount;
	}
	
	public List<CallerIdGroupBean> getCallerIdGroup() {
		return callerIdGroup;
	}
	public void setCallerIdGroup(List<CallerIdGroupBean> callerIdGroup) {
		this.callerIdGroup = callerIdGroup;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExtendedDownloadBean [subscriberID=");
		builder.append(subscriberID);
		builder.append(", toneID=");
		builder.append(toneID);
		builder.append(", shuffleID=");
		builder.append(shuffleID);
		builder.append(", toneName=");
		builder.append(toneName);
		builder.append(", toneType=");
		builder.append(toneType);
		builder.append(", previewFile=");
		builder.append(previewFile);
		builder.append(", rbtFile=");
		builder.append(rbtFile);
		builder.append(", ugcRbtFile=");
		builder.append(ugcRbtFile);
		builder.append(", downloadStatus=");
		builder.append(downloadStatus);
		builder.append(", downloadType=");
		builder.append(downloadType);
		builder.append(", categoryID=");
		builder.append(categoryID);
		builder.append(", chargeClass=");
		builder.append(chargeClass);
		builder.append(", selectedBy=");
		builder.append(selectedBy);
		builder.append(", deselectedBy=");
		builder.append(deselectedBy);
		builder.append(", setTime=");
		builder.append(setTime);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", downloadInfoMap=");
		builder.append(downloadInfoMap);
		builder.append(", isSetForAll=");
		builder.append(isSetForAll);
		builder.append(", nextBillingDate=");
		builder.append(nextBillingDate);
		builder.append(", refID=");
		builder.append(refID);
		builder.append(", selectionInfo=");
		builder.append(selectionInfo);
		builder.append(", deselectionInfo=");
		builder.append(deselectionInfo);
		builder.append(", nextChargingDate=");
		builder.append(nextChargingDate);
		builder.append(", lastChargeAmount=");
		builder.append(lastChargeAmount);
		builder.append(", album=");
		builder.append(album);
		builder.append(", artist=");
		builder.append(artist);
		builder.append(", clipRbtWavFile=");
		builder.append(clipRbtWavFile);
		builder.append(", imageFilePath=");
		builder.append(imageFilePath);
		builder.append(", callerIdGroup=");
		builder.append(callerIdGroup);
		builder.append("]");
		return builder.toString();
	}	
}