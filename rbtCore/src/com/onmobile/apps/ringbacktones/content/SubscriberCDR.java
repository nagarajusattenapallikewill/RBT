package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/*This class is related to SubscriberCDR*/

public interface SubscriberCDR
{
	/*Returns subscriber ID*/
    public String subID();

	/*Returns selection date*/
    public Date selectionDate();
    
    /*Returns caller ID*/
    public String callerID();
    
    /*Returns category ID*/
    public int categoryID();
	
	/*Returns subscriber file*/
	public String subscriberFile();
	
	/*Returns status*/
    public int status();
    
    /*Returns prepaid yes*/
	public boolean prepaidYes();

	/*Returns class type*/
	public String classType();

	/*Return selected by*/
	public String selectedBy();

	/*Return selection info*/
	public String selectionInfo();

	/*Converts date to string*/
    public String date(Date date);	
}