package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

public interface ShufflePromo {
	/*Returns subscriber ID*/
    public String subID();

	/*Returns mode*/
    public String mode();
    
	/*Returns service start date*/
    public Date serviceStartDate();

	/*Returns service end date*/
    public Date serviceEndDate();
	
	/*Returns categoryId*/
	public int categoryId();
	
	/*Returns extra info*/
	public String extraInfo();
	
}
