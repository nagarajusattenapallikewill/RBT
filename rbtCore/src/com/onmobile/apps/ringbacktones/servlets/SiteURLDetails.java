package com.onmobile.apps.ringbacktones.servlets;

public class SiteURLDetails {
	public String URL;
	public boolean use_proxy;
	public String proxy_host;
	public int proxy_port;
	public int time_out;
	public int connection_time_out;
	public String circle_id=null;
 public SiteURLDetails(String URL,boolean use_proxy,String proxy_host,int proxy_port,int time_out,int connection_time_out,String circle_id){
	this.proxy_host=proxy_host;
	this.proxy_port=proxy_port;
	this.time_out=time_out;
	this.use_proxy=use_proxy;
	this.URL=URL;
	this.connection_time_out=connection_time_out;
	this.circle_id=circle_id;
 }
}
