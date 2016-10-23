package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/*This class is related to ViralBlackList table*/

public interface ViralBlackListTable
{
	/*Returns subscriber ID*/
	 public String subID();

	/*Returns start time*/
	 public Date startTime();
	 
	 /*Returns end time*/
	 public Date endTime();
	 
	/*Returns subscriber type*/
	 public String subType();
}