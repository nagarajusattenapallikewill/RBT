package com.onmobile.apps.ringbacktones.servlets;

public class GiftDetails {
	public String caller;
	public String chargeAmount;
	public String giftState;
	public String senttime;
	public String wavFile;
	public String clipId;
	public String categoryId;
	
	public GiftDetails(String caller,String amnt,String state, String time,String wavfile,String clipId,String catId){
		this.caller=caller;
		this.chargeAmount=amnt;
		this.senttime=time;
		this.giftState=state;
		this.wavFile=wavfile;
		this.categoryId=catId;
		this.clipId=clipId;
	}
}
