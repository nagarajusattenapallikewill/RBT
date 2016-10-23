package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/**
 * @author vinayasimha.patil
 *
 */
public interface SubscriberDownloads 
{
	/*Returns Subscriber ID*/
	public String subscriberId();
	
	/*Returns promo ID*/
	public String promoId();
	
	/*Returns Download Status*/
	public char downloadStatus();
	
	/*Returns set time*/
	public Date setTime();
	
	/*Returns start time*/
	public Date startTime();
	
	/*Returns end*/
	public Date endTime();
	
	/*Returns category id*/
	public int categoryID();
	
	/*Returns clip yes value*/
	//public boolean clipYes();
	
	/*Returns deactivated by*/
	public String deactivatedBy();
	
	/*Returns category type*/
	public int categoryType();	 

	/*Returns class type*/
	public String classType();	 

	/*Returns selected by*/
	public String selectedBy();	 

	/*Returns refID*/
	public String refID();	 

	/*Returns extraInfo*/
	public String extraInfo();
	
	/*Returns selectionInfo*/
	public String selectionInfo();

	public String retryCount();

    public Date nextRetryTime();
    
    public Date lastChargedDate();
    
    public Date nextBillingDate();
}
