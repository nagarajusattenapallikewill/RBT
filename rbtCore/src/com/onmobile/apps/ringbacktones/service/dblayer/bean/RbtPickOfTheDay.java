package com.onmobile.apps.ringbacktones.service.dblayer.bean;

import com.onmobile.apps.ringbacktones.service.dblayer.bean.primaryKey.PickOfDayPK;

public class RbtPickOfTheDay
{
	private int categoryId;
	private int clipId;
	private PickOfDayPK pickOfDayPK;
	private char prepaidYes;
	private String profile;
	private String language;
	/**
	 * @return the categoryId
	 */
	public int getCategoryId() {
		return categoryId;
	}
	/**
	 * @param categoryId the categoryId to set
	 */
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	/**
	 * @return the clipId
	 */
	public int getClipId() {
		return clipId;
	}
	/**
	 * @param clipId the clipId to set
	 */
	public void setClipId(int clipId) {
		this.clipId = clipId;
	}
	
	/**
	 * @return the pickOfDayPK
	 */
	public PickOfDayPK getPickOfDayPK() {
		return pickOfDayPK;
	}
	/**
	 * @param pickOfDayPK the pickOfDayPK to set
	 */
	public void setPickOfDayPK(PickOfDayPK pickOfDayPK) {
		this.pickOfDayPK = pickOfDayPK;
	}
	/**
	 * @return the prepaidYes
	 */
	public char getPrepaidYes() {
		return prepaidYes;
	}
	/**
	 * @param prepaidYes the prepaidYes to set
	 */
	public void setPrepaidYes(char prepaidYes) {
		this.prepaidYes = prepaidYes;
	}
	/**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}
	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RbtPickOfTheDay [categoryId=").append(categoryId)
				.append(", clipId=").append(clipId).append(", pickOfDayPK=")
				.append(pickOfDayPK).append(", prepaidYes=").append(prepaidYes)
				.append(", profile=").append(profile).append(", language=")
				.append(language).append("]");
		return builder.toString();
	}
	
}
