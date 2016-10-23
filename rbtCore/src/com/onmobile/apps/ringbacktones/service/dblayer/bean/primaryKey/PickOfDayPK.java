package com.onmobile.apps.ringbacktones.service.dblayer.bean.primaryKey;

import java.io.Serializable;

public class PickOfDayPK implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2616707448425511975L;
	
	private String playDate;
	private String circleId;
	/**
	 * @return the playDate
	 */
	public String getPlayDate() {
		return playDate;
	}
	/**
	 * @param playDate the playDate to set
	 */
	public void setPlayDate(String playDate) {
		this.playDate = playDate;
	}
	/**
	 * @return the circleId
	 */
	public String getCircleId() {
		return circleId;
	}
	/**
	 * @param circleId the circleId to set
	 */
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PickOfDayPK [playDate=").append(playDate)
				.append(", circleId=").append(circleId).append("]");
		return builder.toString();
	}

}
