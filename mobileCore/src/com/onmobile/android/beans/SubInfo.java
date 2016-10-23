package com.onmobile.android.beans;



public class SubInfo
{
	private String defaultCategory = null;//json String for the category
	private String imagePath = null;
	private String previewPath = null;
	private int giftInboxCount = 0;
	private int availableDownloads = 0;
	private int clipRowCount = 0;
	private int musicPackAmount = 0;
	private String giftAmount;
	private String mode;
	private String currentVersion;
	private String mandatoryToUpgrade;
	private String consentReturnYesUrl;
	private String consentReturnNoUrl;
	private String subscriptionAmount;
	private String subscriptionPeriod;
	private String playlistsCategories;
	private String otherPlaylistsCategory;
	private String freemiumCategory;
	private String freemiumClips;
	
	public int getMusicPackAmount() {
		return musicPackAmount;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public String getPreviewPath() {
		return previewPath;
	}
	public void setPreviewPath(String previewPath) {
		this.previewPath = previewPath;
	}

	public void setMusicPackAmount(int musicPackAmount) {
		this.musicPackAmount = musicPackAmount;
	}
	public int getGiftInboxCount() {
		return giftInboxCount;
	}
	public void setGiftInboxCount(int giftInboxCount) {
		this.giftInboxCount = giftInboxCount;
	}
	public int getAvailableDownloads() {
		return availableDownloads;
	}
	public void setAvailableDownloads(int availableDownloads) {
		this.availableDownloads = availableDownloads;
	}
	public int getClipRowCount() {
		return clipRowCount;
	}
	public void setClipRowCount(int clipRowCount) {
		this.clipRowCount = clipRowCount;
	}
	public void setDefaultCategory(String defaultCategory) {
		this.defaultCategory = defaultCategory;
	}
	public String getDefaultCategory() {
		return defaultCategory;
	}
	public String getGiftAmount() {
		return giftAmount;
	}
	public void setGiftAmount(String giftAmount) {
		this.giftAmount = giftAmount;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getCurrentVersion() {
		return currentVersion;
	}
	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}
	public String getMandatoryToUpgrade() {
		return mandatoryToUpgrade;
	}
	public void setMandatoryToUpgrade(String mandatoryToUpgrade) {
		this.mandatoryToUpgrade = mandatoryToUpgrade;
	}
	public String getConsentReturnYesUrl() {
		return consentReturnYesUrl;
	}
	public void setConsentReturnYesUrl(String consentReturnYesUrl) {
		this.consentReturnYesUrl = consentReturnYesUrl;
	}
	public String getConsentReturnNoUrl() {
		return consentReturnNoUrl;
	}
	public void setConsentReturnNoUrl(String consentReturnNoUrl) {
		this.consentReturnNoUrl = consentReturnNoUrl;
	}
	public String getSubscriptionAmount() {
		return subscriptionAmount;
	}
	public void setSubscriptionAmount(String subscriptionAmount) {
		this.subscriptionAmount = subscriptionAmount;
	}
	public String getSubscriptionPeriod() {
		return subscriptionPeriod;
	}
	public void setSubscriptionPeriod(String subscriptionPeriod) {
		this.subscriptionPeriod = subscriptionPeriod;
	}
	public String getPlaylistsCategories() {
		return playlistsCategories;
	}
	public void setPlaylistsCategories(String playlistsCategories) {
		this.playlistsCategories = playlistsCategories;
	}
	public String getOtherPlaylistsCategory() {
		return otherPlaylistsCategory;
	}
	public void setOtherPlaylistsCategory(String otherPlaylistsCategory) {
		this.otherPlaylistsCategory = otherPlaylistsCategory;
	}
	public String getFreemiumCategory() {
		return freemiumCategory;
	}
	public void setFreemiumCategory(String freemiumCategory) {
		this.freemiumCategory = freemiumCategory;
	}
	public String getFreemiumClips() {
		return freemiumClips;
	}
	public void setFreemiumClips(String freemiumClips) {
		this.freemiumClips = freemiumClips;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubInfo [defaultCategory=");
		builder.append(defaultCategory);
		builder.append(", imagePath=");
		builder.append(imagePath);
		builder.append(", previewPath=");
		builder.append(previewPath);
		builder.append(", giftInboxCount=");
		builder.append(giftInboxCount);
		builder.append(", availableDownloads=");
		builder.append(availableDownloads);
		builder.append(", clipRowCount=");
		builder.append(clipRowCount);
		builder.append(", musicPackAmount=");
		builder.append(musicPackAmount);
		builder.append(", giftAmount=");
		builder.append(giftAmount);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", currentVersion=");
		builder.append(currentVersion);
		builder.append(", mandatoryToUpgrade=");
		builder.append(mandatoryToUpgrade);
		builder.append(", consentReturnYesUrl=");
		builder.append(consentReturnYesUrl);
		builder.append(", consentReturnNoUrl=");
		builder.append(consentReturnNoUrl);
		builder.append(", subscriptionAmount=");
		builder.append(subscriptionAmount);
		builder.append(", subscriptionPeriod=");
		builder.append(subscriptionPeriod);
		builder.append(", playlistsCategories=");
		builder.append(playlistsCategories);
		builder.append(", otherPlaylistsCategory=");
		builder.append(otherPlaylistsCategory);
		builder.append(", freemiumCategory=");
		builder.append(freemiumCategory);
		builder.append(", freemiumClips=");
		builder.append(freemiumClips);
		builder.append("]");
		return builder.toString();
	}
}