package com.onmobile.apps.ringbacktones.ussd;

public class USSDInfo {
	/*author@Abhinav Anand
	 */
public String processName=null;
public String URL=null;
public String responseString=null;
public String dynamicURLResponseString=null;
public String parentProcessId=null;
public String childProcessId=null;
public String catId=null;
public String processId=null;
//catId must be populated for categories having clips as child
public String getCatId() {
	return catId;
}
public String getChildProcessId() {
	return childProcessId;
}
public String getDynamicURLResponseString() {
	return dynamicURLResponseString;
}
public String getParentProcessId() {
	return parentProcessId;
}
public String getProcessId() {
	return processId;
}
public String getProcessName() {
	return processName;
}
public String getResponseString() {
	return responseString;
}
public String getURL() {
	return URL;
}
public void setCatId(String catId) {
	this.catId = catId;
}
public void setChildProcessId(String childProcessId) {
	this.childProcessId = childProcessId;
}
public void setDynamicURLResponseString(String dynamicURLResponseString) {
	this.dynamicURLResponseString = dynamicURLResponseString;
}
public void setParentProcessId(String parentProcessId) {
	this.parentProcessId = parentProcessId;
}
public void setProcessId(String processId) {
	this.processId = processId;
}
public void setProcessName(String processName) {
	this.processName = processName;
}
public void setResponseString(String responseString) {
	this.responseString = responseString;
}
public void setURL(String url) {
	URL = url;
}

}
