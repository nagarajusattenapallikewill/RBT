package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/*This class is related to Access*/

public interface DeactivatedSubscribers
{
	/*Returns subscriber MSISDN*/
	public String subscriberId();
	 
	/*Returns deactivated time*/
	public Date deactivatedTime();
	 
	/*Returns deactivated by*/
	public String deactivatedBy();
	 
	/*Returns cosID(cos to which the subscriber was subscriber previously)*/
	public String cosID();
}