package com.onmobile.apps.ringbacktones.common;

public class InfoXMLBean {
	
private long userSqnId =-1;
private boolean copyAllowed = false;
private boolean giftAllowed = false;
private String omMsg = null;
private String oprtMsg = null;
private String proMsg = null;
private String userMsg = null;
private boolean publishCallerNameAllowed = false;
private long messageId = -1;
private boolean allowPublish = false;
private String oldSubscriberId = null;
private boolean isPreviewMsg = false; 


public boolean isIspreviewMsg() {
	return isPreviewMsg;
}
public void setIspreviewMsg(boolean isPreviewMsg) {
	this.isPreviewMsg = isPreviewMsg;
}
public boolean isCopyAllowed() {
	return copyAllowed;
}
public void setCopyAllowed(boolean copyAllowed) {
	this.copyAllowed = copyAllowed;
}
public boolean isGiftAllowed() {
	return giftAllowed;
}
public void setGiftAllowed(boolean giftAllowed) {
	this.giftAllowed = giftAllowed;
}
public String getOmMsg() {
	return omMsg;
}
public void setOmMsg(String omMsg) {
	this.omMsg = omMsg;
}
public String getOprtMsg() {
	return oprtMsg;
}
public void setOprtMsg(String oprtMsg) {
	this.oprtMsg = oprtMsg;
}
public String getProMsg() {
	return proMsg;
}
public void setProMsg(String proMsg) {
	this.proMsg = proMsg;
}
public String getUserMsg() {
	return userMsg;
}
public void setUserMsg(String userMsg) {
	this.userMsg = userMsg;
}
public long getUserSqnId() {
	return userSqnId;
}
public void setUserSqnId(long userSqnId) {
	this.userSqnId = userSqnId;
}
public boolean isPublishCallerNameAllowed() {
	return publishCallerNameAllowed;
}
public void setPublishCallerNameAllowed(boolean publishCallerNameAllowed) {
	this.publishCallerNameAllowed = publishCallerNameAllowed;
}
public long getMessageId() {
	return messageId;
}
public void setMessageId(long messageId) {
	this.messageId = messageId;
}
public boolean isAllowPublish() {
	return allowPublish;
}
public void setAllowPublish(boolean allowPublish) {
	this.allowPublish = allowPublish;
}
public String getOldSubscriberId() {
	return oldSubscriberId;
}
public void setOldSubscriberId(String oldSubscriberId) {
	this.oldSubscriberId = oldSubscriberId;
}
public String toString(){
	StringBuilder strBld = new StringBuilder();
	strBld.append("userSqnId: "+userSqnId);
	strBld.append("copyAllowed: "+copyAllowed);
	strBld.append("giftAllowed: "+giftAllowed);
	strBld.append("omMsg: "+omMsg);
	strBld.append("oprtMsg: "+oprtMsg);
	strBld.append("userMsg: "+userMsg);
	strBld.append("publishCallerNameAllowed: "+publishCallerNameAllowed);
	strBld.append("messageId: "+messageId);
	strBld.append("allowPublish: "+allowPublish);
	if(oldSubscriberId != null)
		strBld.append(" old subscriberId: "+oldSubscriberId);
	return strBld.toString();
}
}
