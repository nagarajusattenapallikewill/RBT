package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/*This class is related to ContextTable*/

public interface ContextTable
{
	/*Returns subscriber ID*/
	public String subID();

	/*Returns clip ID*/
	public String clipID();
	
	/*Returns selection date*/
	public Date selectionDate();
}