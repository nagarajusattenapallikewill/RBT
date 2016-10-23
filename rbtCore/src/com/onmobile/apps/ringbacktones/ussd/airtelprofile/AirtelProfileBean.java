package com.onmobile.apps.ringbacktones.ussd.airtelprofile;

import java.util.ArrayList;

public class AirtelProfileBean {
private String lang;
private String langvalue;
private ArrayList<AirtelProfilesClip> profileClips;
private int count=0;
public int getCount() {
	return count;
}
public void setCount(int count) {
	this.count = count;
}
public String getLang() {
	return lang;
}
public void setLang(String lang) {
	this.lang = lang;
}
public String getLangvalue() {
	return langvalue;
}
public void setLangvalue(String langvalue) {
	this.langvalue = langvalue;
}
public ArrayList<AirtelProfilesClip> getProfileClips() {
	return profileClips;
}
public void setProfileClips(ArrayList<AirtelProfilesClip> profileClips) {
	this.profileClips = profileClips;
}
}
