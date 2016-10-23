package com.onmobile.apps.ringbacktones.content;

import java.sql.Connection;
import java.util.Date;

/*This class is related to SubscriberSelections*/

public interface SubscriberStatus
{
	/*Returns subscriber ID*/
    public String subID();

	/*Returns caller ID*/
    public String callerID();

	/*Returns category ID*/
    public int categoryID();
	
	/*Returns subscriber file*/
	public String subscriberFile();
	
	/*Returns set time*/
    public Date setTime();
	
	/*Returns start time*/
    public Date startTime();

	/*Returns end time*/
    public Date endTime();

	/*Returns status*/
    public int status();

	/*Returns class type*/
    public String classType();

	/*Returns selected by*/
	public String selectedBy();

	/*Returns selection info*/
	public String selectionInfo();
	
	/*Returns next charging date*/
	public Date nextChargingDate();
	
    /*Returns prepaid yes*/
    public boolean prepaidYes();
    
    /*Returns from time*/
    public int fromTime();
    
    /*Returns to time*/
    public int toTime();

	/*Set subscriber file*/
    public void setSubscriberFile(String subscriberWavFile);
    
    /*Set next charging date*/
    public void setNextChargingDate(Date date);
    
    /*Set prepaid yes*/
    public void setPrepaidYes(boolean prepaid);
	       
	/*To update record in the database*/
	public void update(Connection conn);

	/*Converts date to string*/
    public String date(Date date);	
	
	/*Returns Sel Status*/
	public String selStatus();

	/*Returns DeselectedBy*/
	public String deSelectedBy();

	/*Returns oldClassType*/
	public String oldClassType();

	/*Returns categoryType*/
	public int categoryType();

	/*Returns categoryType*/
	public char loopStatus();
	
	/*Returns selType*/ 
    public int selType();
    
    /*Returns selInterval*/
    public String selInterval();

    /*Returns RefID used in SM charging requests */
	public String refID();
	
	/*Returns Misc extraInfo of the Selection */
	public String extraInfo();

	public String circleId();

	public String retryCount();

    public Date nextRetryTime();
    
    public void setRequestTime(Date date);
    
    public Date getRequestTime();
    
    public String udpId();
}