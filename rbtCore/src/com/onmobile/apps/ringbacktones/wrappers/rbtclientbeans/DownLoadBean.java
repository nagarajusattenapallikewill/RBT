package com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans;

public class DownLoadBean {
private String subId;
private String downloadCatId;
private String downloadClipId;
private String downloadChargeClass;
private boolean isPrepaid=true;

public String getDownloadChargeClass() {
	return downloadChargeClass;
}
public void setDownloadChargeClass(String downloadChargeClass) {
	this.downloadChargeClass = downloadChargeClass;
}
public boolean isPrepaid() {
	return isPrepaid;
}
public void setPrepaid(boolean isPrepaid) {
	this.isPrepaid = isPrepaid;
}
public String getSubId() {
	return subId;
}
public void setSubId(String subId) {
	this.subId = subId;
}
public String getDownloadCatId() {
	return downloadCatId;
}
public void setDownloadCatId(String downloadCatId) {
	this.downloadCatId = downloadCatId;
}
public String getDownloadClipId() {
	return downloadClipId;
}
public void setDownloadClipId(String downloadClipId) {
	this.downloadClipId = downloadClipId;
}

}
