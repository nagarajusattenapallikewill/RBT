package com.onmobile.android.beans;


public class ExtendedSelectionBean extends ExtendedClipBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String selectionStartTime;
	private String selectionEndTime;
	private String callerId;
	private int status;
	private String selectionStatus;
	private int categoryId;
	private String interval;
	private String fromTime;
	private String toTime;
	private String nextChargingDate;
	private String selectedBy;
	private String categoryType;
	private String inLoop;
	private String ugcRbtFile;
	
	public ExtendedSelectionBean(ExtendedClipBean extendedClipBean, String selectionStartTime, String selectionEndTime, String callerId, int status, String selectionStatus, int categoryId, String interval, String fromTime, String toTime, String catName, String nextChargingDate) {
		this.selectionStartTime = selectionStartTime;
		this.selectionEndTime = selectionEndTime;
		this.callerId = callerId;
		this.status = status;
		this.selectionStatus = selectionStatus;
		this.categoryId = categoryId;
		this.interval = interval;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.nextChargingDate = nextChargingDate;
		this.setClipId(extendedClipBean.getClipId());
		this.setClipPreviewWavFile(extendedClipBean.getClipPreviewWavFile());
		this.setClipPreviewWavFilePath(extendedClipBean.getClipPreviewWavFilePath());
		if (catName != null)
			this.setClipName(catName);
		else
			this.setClipName(extendedClipBean.getClipName());

		this.setArtist(extendedClipBean.getArtist());
		this.setAlbum(extendedClipBean.getAlbum());
		this.setClipRbtWavFile(extendedClipBean.getClipRbtWavFile());
		this.setClipPromoId(extendedClipBean.getClipPromoId());
		this.setImageFilePath(extendedClipBean.getImageFilePath());
		this.setNoOfVotes(extendedClipBean.getNoOfVotes());
		this.setSumOfRatings(extendedClipBean.getSumOfRatings());
		this.setAverageRating(extendedClipBean.getAverageRating());
		this.setLikes(extendedClipBean.getLikes());
		this.setDislikes(extendedClipBean.getDislikes());
		this.setContentType(extendedClipBean.getContentType());
	}

	public void setSelectionStartTime(String selectionStartTime) {
		this.selectionStartTime = selectionStartTime;
	}

	public String getSelectionStartTime() {
		return selectionStartTime;
	}

	public void setSelectionEndTime(String selectionEndTime) {
		this.selectionEndTime = selectionEndTime;
	}

	public String getSelectionEndTime() {
		return selectionEndTime;
	}

	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}

	public String getCallerId() {
		return callerId;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void setSelectionStatus(String selectionStatus) {
		this.selectionStatus = selectionStatus;
	}

	public String getSelectionStatus() {
		return selectionStatus;
	}
	
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getCategoryId() {
		return categoryId;
	}
	
	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public String getFromTime() {
		return fromTime;
	}

	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}

	public String getToTime() {
		return toTime;
	}

	public void setToTime(String toTime) {
		this.toTime = toTime;
	}

	public String getNextChargingDate() {
		return nextChargingDate;
	}

	public void setNextChargingDate(String nextChargingDate) {
		this.nextChargingDate = nextChargingDate;
	}

	public String getSelectedBy() {
		return selectedBy;
	}

	public void setSelectedBy(String selectedBy) {
		this.selectedBy = selectedBy;
	}

	public String getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}

	public String getInLoop() {
		return inLoop;
	}

	public void setInLoop(String inLoop) {
		this.inLoop = inLoop;
	}
	
	public String getUgcRbtFile() {
		return ugcRbtFile;
	}

	public void setUgcRbtFile(String ugcRbtFile) {
		this.ugcRbtFile = ugcRbtFile;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExtendedSelectionBean)) {
			return false;
		}
		ExtendedSelectionBean bean = (ExtendedSelectionBean)obj;
		boolean equals = false;
		equals = ((this.getClipId() == bean.getClipId()) && (this.getSelectionStartTime().equals(bean.getSelectionStartTime())) && (this.getSelectionEndTime().equals(bean.getSelectionEndTime())));
		
		return equals;
	}
	
	@Override
	public int hashCode(){
		int hash = 7;
		hash = 31 * hash + this.getClipId();
		//hash = 31 * hash + (null == this.getCategoryName() ? 0 : this.getCategoryName().hashCode());
		return hash;
	}
}
