package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/*This class is related to Subscriber*/

public interface Subscriber
{
	/*Returns subscriber ID*/
    public String subID();

	/*Returns activated by*/
    public String activatedBy();
    
    /*Returns deactivated by*/
    public String deactivatedBy();

	/*Returns start date*/
    public Date startDate();

	/*Returns end date*/
    public Date endDate();
	
	/*Returns prepaid yes*/
	public boolean prepaidYes();

	/*Returns last access date*/
	public Date accessDate();
	
	/*Returns next charging date*/
	public Date nextChargingDate();
		
	/*Returns no of access*/
	public int noOfAccess();

	/*Returns activation info*/
	public String activationInfo();
	
	/*Returns subscription class*/
	public String subscriptionClass();
	
	/*Returns subscription or renewal yes*/
	public boolean subscriptionYes();

	/*Return subscription type*/
	public String subYes();
	
	/*Returns last deactivation info*/
	public String lastDeactivationInfo();
	
	/*Returns last deactivation date*/
	public Date lastDeactivationDate();
	
	/*Returns activation date*/
	public Date activationDate();
	
	/*Set prepaid yes*/
	public void setPrepaidYes(boolean prepaid);

    /*Set next charging date*/
    public void setNextChargingDate(Date date);
    
    /*Set deactivation info*/
    public void setLastDeactivationInfo(String lastDeactivationInfo);
    
    /*Set deactivation date*/
    public void setLastDeactivationDate(Date lastDeactivationDate);

    /*Increment no of access*/
    public void incrementNoOfAccess();
     
	/*Converts date to string*/
    public String date(Date date);	

	/*Returns oldClassType*/
	public String oldClassType();

	/*Returns maxSelection*/
	public int maxSelections();

	/*Returns cos id*/
	public String cosID();

	/*Returns activated cos id*/
	public String activatedCosID();
	
	/*Returns rbt type*/ 
    public int rbtType();
    
    /*Returns subscriber language*/ 
    public String language();
    
    /*Set subscriber language in bean (not updating DB)*/
    public void setLanguage(String language);
    
    public String extraInfo();
    
    /*Returns circle id*/
    public String circleID();
    
    /*Returns RefID used in SM charging requests */
	public String refID();
	
	/*Set new cosID in bean (not updating DB)*/
	public void setCosID(String cosID);
	
	/*Set new subYes state in bean */
	public void setSubYes(String subYes);

	/*Set subscriber extraInfo in bean (not updating DB)*/
    public void setExtraInfo(String extraInfo);

    public String retryCount();

    public Date nextRetryTime();
    
    /*Set RefID used requests */
	public void setRefID(String refId);
	
	//Added for prism next billing date
	public Date prismNextBillingDate();
	
	public String operatorName();
}