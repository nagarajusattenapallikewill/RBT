package com.onmobile.android.beans;

public class ClipInfoActionBean {

	private int catId;
	private int offset;
	private boolean BIIndc;
	private String subId;
	private String devicetype;
	private String browsingLanguage;
	private String appName;
	private int maxResults;
	private String circleId;
	private boolean isUserLanguageSelected;
	private boolean isSubscribed;
	private int totalSize;
	private String sessionID;

	public String getCircleId() {
		return circleId;
	}

	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}

	public boolean isUserLanguageSelected() {
		return isUserLanguageSelected;
	}

	public void setUserLanguageSelected(boolean isUserLanguageSelected) {
		this.isUserLanguageSelected = isUserLanguageSelected;
	}

	public boolean isSubscribed() {
		return isSubscribed;
	}

	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public int getCatId() {
		return catId;
	}

	public void setCatId(int catId) {
		this.catId = catId;
	}



	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public boolean isBIIndc() {
		return BIIndc;
	}

	public void setBIIndc(boolean bIIndc) {
		BIIndc = bIIndc;
	}

	public String getSubId() {
		return subId;
	}

	public void setSubId(String subId) {
		this.subId = subId;
	}

	public String getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(String devicetype) {
		this.devicetype = devicetype;
	}

	public String getBrowsingLanguage() {
		return browsingLanguage;
	}

	public void setBrowsingLanguage(String browsingLanguage) {
		this.browsingLanguage = browsingLanguage;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public ClipInfoActionBean(int catId,int offset,
			boolean BIIndc, String subId, String devicetype,
			String browsingLanguage, String appName, int maxResults,
			String circleId, boolean isUserLanguageSelected,
			boolean isSubscribed, int totalSize,
			String sessionID) {
		this.setCatId(catId);
		this.setOffset(offset);
		this.setBIIndc(BIIndc);
		this.setSubId(subId);
		this.setDevicetype(devicetype);
		this.setBrowsingLanguage(browsingLanguage);
		this.setAppName(appName);
		this.setMaxResults(maxResults);
		this.setCircleId(circleId);
		this.setUserLanguageSelected(isUserLanguageSelected);
		this.setSubscribed(isSubscribed);
		this.setTotalSize(totalSize);
		this.setSessionID(sessionID);

	}

}
