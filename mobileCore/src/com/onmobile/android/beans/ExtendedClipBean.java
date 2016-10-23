package com.onmobile.android.beans;

import com.onmobile.android.utils.ClipUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;

public class ExtendedClipBean extends Clip {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2317404292100062147L;
	private float averageRating;
	private int noOfVotes;
	private int sumOfRatings;
	private int likes;
	private int dislikes;
	private String imageFilePath;
	private String period;
	private String amount;
	private String renewalPeriod;
	private String renewalAmount;
	
	public ExtendedClipBean() {
		super();
	}

	public ExtendedClipBean(Clip clip, ClipRating clipRating) {

		if (clip != null) {
			this.setClipId(clip.getClipId());
			this.setClipName(clip.getClipName());
			this.setClipNameWavFile(clip.getClipNameWavFile());
			this.setClipPreviewWavFile(clip.getClipPreviewWavFile());
			this.setClipRbtWavFile(clip.getClipRbtWavFile());
			this.setClipGrammar(clip.getClipGrammar());
			this.setClipSmsAlias(clip.getClipSmsAlias());
			this.setAddToAccessTable(clip.getAddToAccessTable());
			this.setClipPromoId(clip.getClipPromoId());
			this.setClipSmsAlias(clip.getClipSmsAlias());
			this.setClassType(clip.getClassType());
			this.setClipStartTime(clip.getClipStartTime());
			this.setClipEndTime(clip.getClipEndTime());
			this.setSmsStartTime(clip.getSmsStartTime());
			this.setAlbum(clip.getAlbum());
			this.setLanguage(clip.getLanguage());
			this.setClipDemoWavFile(clip.getClipDemoWavFile());
			this.setArtist(clip.getArtist());
			this.setContentType(clip.getContentType());
			this.setClipInfoMap(clip.getClipInfoMap());
			this.setClipLanguage(clip.getClipLanguage());
			this.setShortLanguage(clip.getShortLanguage());
			this.setClipPreviewWavFilePath(clip.getClipPreviewWavFilePath());
			this.setImageFilePath(clip.getClipInfo(Clip.ClipInfoKeys.IMG_URL));
			//RBT-14626
			this.setClipInfo(clip.getClipInfo());
		}

		if (clipRating != null) {
			this.setNoOfVotes(clipRating.getNoOfVotes());
			this.setSumOfRatings(clipRating.getSumOfRatings());
			this.setAverageRating(ClipUtils.getClipRating(
					clipRating.getNoOfVotes(), clipRating.getSumOfRatings()));
			this.setLikes(clipRating.getLikeVotes());
			this.setDislikes(clipRating.getDislikeVotes());
		}
	}

	public int getSumOfRatings() {
		return sumOfRatings;
	}

	public void setSumOfRatings(int sumOfRatings) {
		this.sumOfRatings = sumOfRatings;
	}

	public int getNoOfVotes() {
		return noOfVotes;
	}

	public void setNoOfVotes(int noOfVotes) {
		this.noOfVotes = noOfVotes;
	}

	public float getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(float rating) {
		this.averageRating = rating;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public int getDislikes() {
		return dislikes;
	}

	public void setDislikes(int dislikes) {
		this.dislikes = dislikes;
	}

	public String getImageFilePath() {
		return imageFilePath;
	}

	public void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getRenewalPeriod() {
		return renewalPeriod;
	}

	public void setRenewalPeriod(String renewalPeriod) {
		this.renewalPeriod = renewalPeriod;
	}

	public String getRenewalAmount() {
		return renewalAmount;
	}

	public void setRenewalAmount(String renewalAmount) {
		this.renewalAmount = renewalAmount;
	}
}
