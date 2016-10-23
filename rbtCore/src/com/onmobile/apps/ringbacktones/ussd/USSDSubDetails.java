package com.onmobile.apps.ringbacktones.ussd;

import java.util.ArrayList;

public class USSDSubDetails {
	/*author@Abhinav Anand
	 */
public String subClass=null;
public String userType=null;
public String status=null;
public boolean active=false;
public boolean isPrepaid=false;
public String language=null;
public boolean deactive=false;
public boolean advanceSubscriptionAllowed=false;
public ArrayList downloadList=null;
public ArrayList selectionList=null;

public String toString(){
	StringBuilder strBld = new StringBuilder();
	strBld.append("active = "+this.active);
	strBld.append(";userType = "+this.userType);
	strBld.append(";status = "+this.status);
	strBld.append(";isPrepaid = "+this.isPrepaid);
	strBld.append(";deactive = "+this.deactive);
	strBld.append(";advanceSubscriptionAllowed = "+this.advanceSubscriptionAllowed);
	strBld.append(";subClass = "+this.subClass);

	return strBld.toString();
}
public USSDSubDetails(String subClass,String userType,String status,boolean active,boolean isPrepaid,String language,boolean deactive){
	this.active=active;
	this.isPrepaid=isPrepaid;
	this.status=status;
	this.subClass=subClass;
	this.userType=userType;
	this.language=language;
	this.deactive=deactive;
	if(((active && this.subClass!=null && this.subClass.equalsIgnoreCase("default")) || this.status.equalsIgnoreCase("new_user")||this.status.equalsIgnoreCase("deactivate"))){
		advanceSubscriptionAllowed=true;
	}
}
public boolean isActive() {
	return active;
}
public boolean isAdvanceSubscriptionAllowed() {
	return advanceSubscriptionAllowed;
}
public boolean isDeactive() {
	return deactive;
}
public boolean isPrepaid() {
	return isPrepaid;
}
public String getLanguage() {
	return language;
}
public String getStatus() {
	return status;
}
public String getSubClass() {
	return subClass;
}
public String getUserType() {
	return userType;
}
public void setActive(boolean active) {
	this.active = active;
}
public void setAdvanceSubscriptionAllowed(boolean advanceSubscriptionAllowed) {
	this.advanceSubscriptionAllowed = advanceSubscriptionAllowed;
}
public void setDeactive(boolean deactive) {
	this.deactive = deactive;
}
public void setPrepaid(boolean isPrepaid) {
	this.isPrepaid = isPrepaid;
}
public void setLanguage(String language) {
	this.language = language;
}
public void setStatus(String status) {
	this.status = status;
}
public void setSubClass(String subClass) {
	this.subClass = subClass;
}
public void setUserType(String userType) {
	this.userType = userType;
}
}
