package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.ArrayList;
import java.util.List;


/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class Library 
{
	private boolean isAlbumUser;
	private String nextSelectionAmount;
	private boolean provideDefaultLoopOption;
	private boolean isMusicboxDefaultSettingPending;
	private boolean isClipDefaultSettingPending;
	private int totalDownloads;
	private int freeDownloadsLeft;
	private Settings settings = null;
	private Downloads downloads = null;
	private RecentSelection recentSelection = null;
	private String nextChargeClass;
	private boolean isRecentSelConsent = false;
	/**
	 * 
	 */
	//static final org.apache.log4j.Logger c_logger = org.apache.log4j.Logger.getLogger(Library.class);
	
	public Library()
	{

	}

	/**
	 * @param isAlbumUser
	 * @param nextSelectionAmount
	 * @param provideDefaultLoopOption
	 * @param isMusicboxDefaultSettingPending
	 * @param isClipDefaultSettingPending
	 * @param totalDownloads
	 * @param settings
	 * @param downloads
	 */
	public Library(boolean isAlbumUser, String nextSelectionAmount,
			boolean provideDefaultLoopOption,
			boolean isMusicboxDefaultSettingPending,
			boolean isClipDefaultSettingPending, int totalDownloads,
			Settings settings, Downloads downloads, String nextChargeClass)
	{
		this.isAlbumUser = isAlbumUser;
		this.nextSelectionAmount = nextSelectionAmount;
		this.provideDefaultLoopOption = provideDefaultLoopOption;
		this.isMusicboxDefaultSettingPending = isMusicboxDefaultSettingPending;
		this.isClipDefaultSettingPending = isClipDefaultSettingPending;
		this.totalDownloads = totalDownloads;
		this.settings = settings;
		this.downloads = downloads;
		this.nextChargeClass = nextChargeClass;
	}

	/**
	 * @return the isAlbumUser
	 */
	public boolean isAlbumUser()
	{
		return isAlbumUser;
	}

	/**
	 * @return the nextSelectionAmount
	 */
	public String getNextSelectionAmount()
	{
		return nextSelectionAmount;
	}

	/**
	 * @return the provideDefaultLoopOption
	 */
	public boolean isProvideDefaultLoopOption()
	{
		return provideDefaultLoopOption;
	}

	/**
	 * @return the isMusicboxDefaultSettingPending
	 */
	public boolean isMusicboxDefaultSettingPending()
	{
		return isMusicboxDefaultSettingPending;
	}

	/**
	 * @return the isClipDefaultSettingPending
	 */
	public boolean isClipDefaultSettingPending()
	{
		return isClipDefaultSettingPending;
	}

	/**
	 * @return the totalDownloads
	 */
	public int getTotalDownloads()
	{
		return totalDownloads;
	}

	/**
	 * @return the settings
	 */
	public Settings getSettings()
	{
		return settings;
	}

	public Settings getSettings(String status){
		//c_logger.info("Entering getDownloads, with String: "+ status);
		Settings statusBasedSettings = new Settings();
		Setting[] allStatusSetArray = settings.getSettings();
		//c_logger.info("Original Value from web service: "+ allStatusDownArray);
		Setting[] statusBasedSetArray = null;
		List<Setting> list = new ArrayList<Setting>();
		if(status==null || status.isEmpty())
			return getSettings();
		for (int i = 0; i < allStatusSetArray.length; i++){
				if(status.equals(allStatusSetArray[i].getSelectionStatus())){
					list.add(allStatusSetArray[i]);
				}
		}
		//c_logger.info("List based on the status: "+ list + " and size is "+ list.size());
		statusBasedSetArray = list.toArray(new Setting[0]);
		statusBasedSettings.setSettings(statusBasedSetArray);
		//c_logger.info("Final Object returned: "+ statusBasedDownloads);
		//downloads.setDownloads(statusBasedDownArray);
		return statusBasedSettings;
	}
	
	/**
	 * @return the downloads
	 */
	public Downloads getDownloads()
	{
		return downloads;
	}

	public Downloads getDownloads(String status){
		//c_logger.info("Entering getDownloads, with String: "+ status);
		Downloads statusBasedDownloads = new Downloads();
		Download[] allStatusDownArray = downloads.getDownloads();
		//c_logger.info("Original Value from web service: "+ allStatusDownArray);
		Download[] statusBasedDownArray = null;
		List<Download> list = new ArrayList<Download>();
		if(status==null || status.isEmpty())
			return getDownloads();
		for (int i = 0; i < allStatusDownArray.length; i++){
				if(status.equals(allStatusDownArray[i].getDownloadStatus())){
					list.add(allStatusDownArray[i]);
				}
		}
		//c_logger.info("List based on the status: "+ list + " and size is "+ list.size());
		statusBasedDownArray = list.toArray(new Download[0]);
		statusBasedDownloads.setNoOfActiveDownloads(list.size());
		statusBasedDownloads.setDownloads(statusBasedDownArray);
		//c_logger.info("Final Object returned: "+ statusBasedDownloads);
		//downloads.setDownloads(statusBasedDownArray);
		return statusBasedDownloads;
	}
	
	/**
	 * @param isAlbumUser the isAlbumUser to set
	 */
	public void setAlbumUser(boolean isAlbumUser)
	{
		this.isAlbumUser = isAlbumUser;
	}

	/**
	 * @param nextSelectionAmount the nextSelectionAmount to set
	 */
	public void setNextSelectionAmount(String nextSelectionAmount)
	{
		this.nextSelectionAmount = nextSelectionAmount;
	}

	/**
	 * @param provideDefaultLoopOption the provideDefaultLoopOption to set
	 */
	public void setProvideDefaultLoopOption(boolean provideDefaultLoopOption)
	{
		this.provideDefaultLoopOption = provideDefaultLoopOption;
	}

	/**
	 * @param isMusicboxDefaultSettingPending the isMusicboxDefaultSettingPending to set
	 */
	public void setMusicboxDefaultSettingPending(
			boolean isMusicboxDefaultSettingPending)
	{
		this.isMusicboxDefaultSettingPending = isMusicboxDefaultSettingPending;
	}

	/**
	 * @param isClipDefaultSettingPending the isClipDefaultSettingPending to set
	 */
	public void setClipDefaultSettingPending(boolean isClipDefaultSettingPending)
	{
		this.isClipDefaultSettingPending = isClipDefaultSettingPending;
	}

	/**
	 * @param totalDownloads the totalDownloads to set
	 */
	public void setTotalDownloads(int totalDownloads)
	{
		this.totalDownloads = totalDownloads;
	}

	/**
	 * @param settings the settings to set
	 */
	public void setSettings(Settings settings)
	{
		this.settings = settings;
	}

	/**
	 * @param downloads the downloads to set
	 */
	public void setDownloads(Downloads downloads)
	{
		this.downloads = downloads;
	}

	/**
	 * @return the freeDownloadsLeft
	 */
	public int getFreeDownloadsLeft() {
		return freeDownloadsLeft;
	}

	/**
	 * @param freeDownloadsLeft the freeDownloadsLeft to set
	 */
	public void setFreeDownloadsLeft(int freeDownloadsLeft) {
		this.freeDownloadsLeft = freeDownloadsLeft;
	}

	/**
	 * @return the nextChargeClass
	 */
	public String getNextChargeClass()
	{
		return nextChargeClass;
	}
	
	/**
	 * @param nextChargeClass the nextChargeClass to set
	 */
	public void setNextChargeClass(String nextChargeClass)
	{
		this.nextChargeClass = nextChargeClass;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((downloads == null) ? 0 : downloads.hashCode());
		result = prime * result + freeDownloadsLeft;
		result = prime * result + (isAlbumUser ? 1231 : 1237);
		result = prime * result + (isClipDefaultSettingPending ? 1231 : 1237);
		result = prime * result
				+ (isMusicboxDefaultSettingPending ? 1231 : 1237);
		result = prime
				* result
				+ ((nextSelectionAmount == null) ? 0 : nextSelectionAmount
						.hashCode());
		result = prime * result + (provideDefaultLoopOption ? 1231 : 1237);
		result = prime * result
				+ ((settings == null) ? 0 : settings.hashCode());
		result = prime * result + totalDownloads;
		result = prime * result + ((nextChargeClass == null) ? 0 : nextChargeClass.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Library other = (Library) obj;
		if (downloads == null) {
			if (other.downloads != null)
				return false;
		} else if (!downloads.equals(other.downloads))
			return false;
		if (freeDownloadsLeft != other.freeDownloadsLeft)
			return false;
		if (isAlbumUser != other.isAlbumUser)
			return false;
		if (isClipDefaultSettingPending != other.isClipDefaultSettingPending)
			return false;
		if (isMusicboxDefaultSettingPending != other.isMusicboxDefaultSettingPending)
			return false;
		if (nextSelectionAmount == null) {
			if (other.nextSelectionAmount != null)
				return false;
		} else if (!nextSelectionAmount.equals(other.nextSelectionAmount))
			return false;
		if (provideDefaultLoopOption != other.provideDefaultLoopOption)
			return false;
		if (settings == null) {
			if (other.settings != null)
				return false;
		} else if (!settings.equals(other.settings))
			return false;
		if (totalDownloads != other.totalDownloads)
			return false;
		if (nextChargeClass == null) {
			if (other.nextChargeClass != null)
				return false;
		} else if (!nextChargeClass.equals(other.nextChargeClass))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Library[downloads=");
		builder.append(downloads);
		builder.append(", freeDownloadsLeft=");
		builder.append(freeDownloadsLeft);
		builder.append(", isAlbumUser=");
		builder.append(isAlbumUser);
		builder.append(", isClipDefaultSettingPending=");
		builder.append(isClipDefaultSettingPending);
		builder.append(", isMusicboxDefaultSettingPending=");
		builder.append(isMusicboxDefaultSettingPending);
		builder.append(", nextSelectionAmount=");
		builder.append(nextSelectionAmount);
		builder.append(", provideDefaultLoopOption=");
		builder.append(provideDefaultLoopOption);
		builder.append(", settings=");
		builder.append(settings);
		builder.append(", totalDownloads=");
		builder.append(totalDownloads);
		builder.append(", nextChargeClass=");
		builder.append(nextChargeClass);
		builder.append(", recentSelection=");
		builder.append(recentSelection);
		builder.append(", isRecentSelConsent=");
		builder.append(isRecentSelConsent);
		builder.append("]");
		return builder.toString();
	}

	public RecentSelection getRecentSelection() {
		return recentSelection;
	}

	public void setRecentSelection(RecentSelection recentSelection) {
		this.recentSelection = recentSelection;
	}
	public boolean isRecentSelConsent() {
		return isRecentSelConsent;
	}

	public void setRecentSelConsent(boolean isRecentSelConsent) {
		this.isRecentSelConsent = isRecentSelConsent;
	}

}
