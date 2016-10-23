package com.onmobile.android.beans;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Gift;

/**
 * @author sridhar.sindiri
 *
 */
public class ExtendedGiftBean extends Gift
{
	private String promoID = null;
	private String artist = null;
	private String imageFilePath = null;
	private static Logger logger = Logger.getLogger(ExtendedGiftBean.class);
	/**
	 * 
	 */
	public ExtendedGiftBean() {
		super();
	}

	/**
	 * @param gift
	 */
	public ExtendedGiftBean(Gift gift)
	{
		super(gift.getSender(), gift.getReceiver(), gift.getCategoryID(), gift
				.getToneID(), gift.getToneName(), gift.getToneType(), gift
				.getPreviewFile(), gift.getRbtFile(), gift.getSentTime(), gift
				.getStatus());
		this.setSelectedBy(gift.getSelectedBy());
		this.setGiftExtraInfoMap(gift.getGiftExtraInfoMap());
		Clip clip = RBTCacheManager.getInstance().getClip(gift.getToneID());
		logger.info("clip Id:::"+clip);
		if (clip != null)
		{
			this.promoID = clip.getClipPromoId();
			this.artist = clip.getArtist();
			this.imageFilePath = clip.getClipInfo(Clip.ClipInfoKeys.IMG_URL);
			logger.info("imageFilePath:::"+imageFilePath);
		}
	}

	/**
	 * @return
	 */
	public String getPromoID() {
		return promoID;
	}

	/**
	 * @param promoID
	 */
	public void setPromoID(String promoID) {
		this.promoID = promoID;
	}

	/**
	 * @return
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 * @param artist
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getImageFilePath() {
		return imageFilePath;
	}

	public void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}
	
}
