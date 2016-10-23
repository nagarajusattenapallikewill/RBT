package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/*This class is related to SubscriberPromotion*/

public interface SubscriberPromo
{
	/*Returns subscriber ID*/
    public String subID();

	/*Returns Number of free days*/
    public int freedays();

	/*Returns subscriber type*/
    public boolean isPrepaid();

	/*Returns activated by*/
	public String activatedBy();

	/*Returns subscriber type*/
	public String subType();	 
	
	public Date startDate();
	
	public Date endDate();
	
}