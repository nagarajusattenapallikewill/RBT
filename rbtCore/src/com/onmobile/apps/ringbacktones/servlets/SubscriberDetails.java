package com.onmobile.apps.ringbacktones.servlets;

import java.util.ArrayList;

import com.onmobile.apps.ringbacktones.subscriptions.ClipCacher;

public class SubscriberDetails {

public boolean subActive=false;
public String subId=null;
public String lastBillDate=null;
public String lang=null;
public String subType=null;
public int selCount=0;

public ArrayList defaultSong=null;
public ArrayList specialCallerSongs=null;
public ArrayList giftOutbox=null;
public ArrayList giftSongs=null;
public int giftCount=0;
public boolean blackList=false;
public String subYes=null;
public String subStatus=null;
public long noOfSubsDaysLeft=-1;

public SubscriberDetails(boolean subActive,String subId,String lastBillDate,String lang,String subType,int selCount,ArrayList defaultSong,ArrayList specialCallerSongs,ArrayList giftSongs,int giftCount,boolean blackList,String subYes){
	
	
		this.defaultSong=defaultSong;

	this.giftCount=giftCount;
	
	this.giftSongs=giftSongs;
	this.lang=lang;
	this.lastBillDate=lastBillDate;
	this.selCount=selCount;
	this.specialCallerSongs=specialCallerSongs;
	this.subActive=subActive;
	this.subId=subId;
	this.subType=subType.trim();
		//ClipCacher.getModifiedSubscriptionType(subType.trim());
	this.subYes=subYes;
}
public SubscriberDetails(boolean subActive,String subId){
	this.subActive=subActive;
	this.subId=subId;
}

}
