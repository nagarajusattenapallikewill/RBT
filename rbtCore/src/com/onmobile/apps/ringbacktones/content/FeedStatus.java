package com.onmobile.apps.ringbacktones.content;

/*This class is related to FeedStatus*/

public interface FeedStatus
{
	/*Returns feed type*/
	 public String type();

	/*Returns feed status*/
	public String status();
	
	/*Returns feed file*/
	public String file();
	
	/*Returns sms keyword*/
	public String smsKeyword();
	
	/*Returns sub keyword*/
	public String subKeyword();
	
	/*Return sms for feed on success*/
	public String smsFeedOnSuccess();
	
	/*Return sms for feed on failure*/
	public String smsFeedOnFailure();
	
	/*Returns sms for feed off success*/
	public String smsFeedOffSuccess();
	
	/*Returns sms for feed off failure*/
	public String smsFeedOffFailure();
	
	/*Returns sms for feed failure*/
	public String smsFeedFailure();
	
	/*Returns sms for non active subscriber*/
	public String smsFeedNonActiveSub();
}