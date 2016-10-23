package com.onmobile.apps.ringbacktones.content;

/*This class is related to StatusType*/

public interface StatusType
{
	/*Returns status code*/
	 public int code();

	/*Returns status description*/
	public String desc();
	
	/*Returns show on GUI*/
	public boolean showGUI();
	
	/*Returns show on VUI*/
	public boolean showVUI();
}