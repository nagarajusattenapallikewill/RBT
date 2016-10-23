package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;

public class PromoMaster implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int clipId;
	private String promoType;
	private String promoCode;
	
	/**
	 * @return
	 */
	public int getClipId() {
		return clipId;
	}
	
	/**
	 * @param clipPromoId
	 */
	public void setClipId(int clipId) {
		this.clipId = clipId;
	}
	
	/**
	 * @return
	 */
	public String getPromoType() {
		return promoType;
	}
	
	/**
	 * @param promoType
	 */
	public void setPromoType(String promoType) {
		this.promoType = promoType;
	}
	
	/**
	 * @return
	 */
	public String getPromoCode() {
		return promoCode;
	}
	
	/**
	 * @param promoCode
	 */
	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("PromoMaster[clipId = ");
		builder.append(clipId);
		builder.append(", promoCode = ");
		builder.append(promoCode);
		builder.append(", promoType = ");
		builder.append(promoType);
		builder.append("]");
		return builder.toString();
	}	
}
