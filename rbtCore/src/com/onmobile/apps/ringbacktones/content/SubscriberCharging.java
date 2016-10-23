package com.onmobile.apps.ringbacktones.content;

import java.sql.Connection;

/*This class is related to SubscriberCharging*/

public interface SubscriberCharging
{
	/*Returns subscriber ID*/
	public String subID();

	/*Returns class type*/
	public String classType();

	/*Returns max selections*/
	public int maxSelections();
	
	/*Set max selections*/
	public void setMaxSelections(Connection conn, String subscriberID, String classType, int maxSelections);

}        