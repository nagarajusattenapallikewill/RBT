package com.onmobile.apps.ringbacktones.servlets;

public class ResponseObj {
public boolean responseStatus;
public StringBuffer response;
public ResponseObj(boolean responseStatus,StringBuffer response){
	this.response=response;
	this.responseStatus=responseStatus;
}
public ResponseObj(){
	responseStatus=false;
}
}
