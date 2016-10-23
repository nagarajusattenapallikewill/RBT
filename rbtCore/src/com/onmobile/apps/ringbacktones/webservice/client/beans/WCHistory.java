package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.io.Serializable;
import java.util.Date;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;

public class WCHistory implements Serializable{
	private static final long serialVersionUID = 1L;
	private String modeInfo = null;
	private String retailerID = null;
	private String mode = null;
	private String requestDate = null;
	private String action = null;
	
	
	public String getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getModeInfo() {
		return modeInfo;
	}
	public void setModeInfo(String modeInfo) {
		this.modeInfo = modeInfo;
	}
	public String getRetailerID() {
		return retailerID;
	}
	public void setRetailerID(String retailerID) {
		this.retailerID = retailerID;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((modeInfo == null) ? 0 : modeInfo.hashCode());
		result = prime * result + ((retailerID == null) ? 0 : retailerID.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((requestDate == null) ? 0 : requestDate.hashCode());
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof WCHistory))
			return false;
		WCHistory other = (WCHistory) obj;
		
		if (modeInfo == null){
			if (other.modeInfo != null)
				return false;
		}else if (!modeInfo.equals(other.modeInfo)){
			return false;
		}
		
		if (retailerID == null){
			if (other.retailerID != null)
				return false;
		}else if (!retailerID.equals(other.retailerID)){
			return false;
		}
		
		if (mode == null){
			if (other.mode != null)
				return false;
		}else if (!mode.equals(other.mode)){
			return false;
		}
		
		if (requestDate == null){
			if (other.requestDate != null)
				return false;
		}else if (!requestDate.equals(other.requestDate)){
			return false;
		}

		if (action == null){
			if (other.action != null)
				return false;
		}else if (!action.equals(other.action)){
			return false;
		}

		
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("WCHistory[modeInfo = ");
		builder.append(modeInfo);
		builder.append(", retailerID = ");
		builder.append(retailerID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", requestDate = ");
		builder.append(requestDate);
		builder.append(", action = ");
		builder.append(action);
		builder.append("]");
		return builder.toString();
	}

}
