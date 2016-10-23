package com.onmobile.apps.ringbacktones.servlets;

public class DemoClip {
public String caller=null;
public String songName=null;
public int clipId=0;
public int catId=0;
public String artist=null;
public String wavfile=null;
public String sentTime=null;
public String setTimeGuiFormat=null;

public DemoClip(String caller,String songName ,int clipId,int catId,String artist,String wavfile){
	this.artist=artist;
	this.caller=caller;
	this.catId=catId;
	this.clipId=clipId;
	this.songName=songName;
	this.wavfile=wavfile;
}
public DemoClip(String caller,String songName ,int clipId,int catId,String artist,String wavfile,String sentTime){
	this.artist=artist;
	this.caller=caller;
	this.catId=catId;
	this.clipId=clipId;
	this.songName=songName;
	this.wavfile=wavfile;
	this.sentTime=sentTime;
}
}
