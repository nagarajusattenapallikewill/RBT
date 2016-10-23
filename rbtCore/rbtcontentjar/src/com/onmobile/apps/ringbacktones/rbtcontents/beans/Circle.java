package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;

public class Circle implements Serializable {

	private static final long serialVersionUID = 1L;
	
	String circleName;
	String circleId;
	
	public String getCircleName() {
		return circleName;
	}
	public void setCircleName(String siteName) {
		this.circleName = siteName;
	}
	public String getCircleId() {
		return circleId;
	}
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Circle[circleId = ");
		builder.append(circleId);
		builder.append(", circleName = ");
		builder.append(circleName);
		builder.append("]");
		return builder.toString();
	}
}
