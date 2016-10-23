package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/*This class is related to FeedSchedule*/

public interface FeedSchedule
{
	/*Returns feedID*/
	 public int feedID();
	 
	/*Returns feed type*/
	 public String type();

	/*Returns schedule name*/
	public String name();
		
	/*Returns sub keyword*/
	public String subKeyword();
	
	/*Return start time*/
	public Date startTime();
	
	/*Return end time*/
	public Date endTime();
		
	/*Returns class type*/
	public String classType();
	
	/*Returns sms for feed on success*/
	public String smsFeedOnSuccess();
	
	/*Returns sms for feed on failure*/
	public String smsFeedOnFailure();
	
	/* Returns pack type */
	public String packType();

	/* Returns status */
	public int status();
}