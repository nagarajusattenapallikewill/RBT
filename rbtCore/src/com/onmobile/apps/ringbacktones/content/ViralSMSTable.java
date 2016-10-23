package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

/*This class is related to ViralSMS table*/

public interface ViralSMSTable
{
    /*Returns subscriber ID*/
	 public String subID();
	 
	/*Returns sent time*/
	 public Date sentTime();
	 
	 /*Returns type*/
	 public String type();
	 
	 /*Returns caller ID*/
	 public String callerID();
	 
	 /*Returns clip ID*/
	 public String clipID();
	 
	 /*Returns count*/
	 public int count();
	
	 /*Returns selected by*/
	 public String selectedBy();

	 /*Returns set time*/
	 public Date setTime();
	 
	 /*Returns extraInfo */
	 public String extraInfo();
	 
	 public String getCallerCircleId();
	 
	 public long getSmsId();
	 public void setSmsId(long id);
	 public String getCircleId();
	 
	 public void setCallerCircleId(String circleId);
	 
	 public String toString();
	 
	 public boolean isTaken();
	 
	 public void setTaken(boolean taken);
	 public Date getStartDate();
	 public void setStartTime(Date date);
	 
	 public Subscriber getSubscriber();
	 public void setSubscriber(Subscriber subscriber);
	
	 /*Return retry time*/
	 public Date retryTime();
	 
	 
}